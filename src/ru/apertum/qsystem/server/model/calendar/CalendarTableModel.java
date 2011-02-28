/*
 *  Copyright (C) 2010 Apertum project. web: www.apertum.ru email: info@apertum.ru
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.apertum.qsystem.server.model.calendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.DataException;
import org.springframework.orm.hibernate3.HibernateCallback;
import ru.apertum.qsystem.common.Uses;

/**
 * Модель для отображения сетки календаля
 * @author Evgeniy Egorov
 */
public class CalendarTableModel extends AbstractTableModel {

    private final List<FreeDay> days;
    private List<FreeDay> days_del;

    public CalendarTableModel(long calcId) {
        Uses.log.logger.debug("Создаем модель для календаря");
        this.calcId = calcId;
        days = getFreeDays(calcId);
        days_del = new ArrayList<FreeDay>(days);
    }
    /**
     * В каком календаре сейчас работаем
     */
    final private long calcId;

    /**
     * Выборка из БД требуемых данных.
     * @param calcId id календаря
     * @return список выходных дней определенного календаря
     */
    public static List<FreeDay> getFreeDays(final Long calcId) {

        return (List<FreeDay>) Uses.getSessionFactory().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                Criteria crit = session.createCriteria(FreeDay.class);
                Criterion calendar_id = Restrictions.eq("calendarId", calcId);
                crit.add(calendar_id);
                return crit.list();
            }
        });
    }

    /**
     * Добавляем дату. Если Такая уже есть то инвертируем
     * @param date
     * @param noInvert true - при обнаружении выходного оставлять его выходным
     * @return Добавлена как свободная или как рабочая/ true = свободная
     */
    public boolean addDay(Date date, boolean noInvert) {
        final FreeDay day = isFreeDate(date);
        if (day != null) {
            if (noInvert) {
                return true;
            } else {
                days.remove(day);
            }
            return false;
        } else {
            days.add(new FreeDay(date, calcId));
            return true;
        }
    }

    /**
     * Проверяем добавлена ли в выходные уже
     * @param date
     * @return
     */
    public FreeDay isFreeDate(Date date) {
        for (FreeDay day : days) {
            if (day.equals(date)) {
                return day;
            }
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return 12;
    }

    @Override
    public int getColumnCount() {
        return 32;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return "X";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? super.getColumnClass(columnIndex) : FreeDay.class;
    }

    @Override
    public String getColumnName(int column) {
        return column == 0 ? "" : Integer.toString(column);
    }

    /**
     * Сбросить выделенные дни в календаре
     */
    public void dropCalendar() {
        Uses.log.logger.debug("Сбросим календарь");
        days.clear();
        fireTableDataChanged();
    }

    /**
     * Пометить все субботы выходными
     */
    public void checkSaturday() {
        Uses.log.logger.debug("Пометив все субботы");
        final GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date());
        final int ye = gc.get(GregorianCalendar.YEAR) % 4 == 0 ? 366 : 365;
        for (int d = 1; d <= ye; d++) {
            gc.set(GregorianCalendar.DAY_OF_YEAR, d);
            if (gc.get(GregorianCalendar.DAY_OF_WEEK) == 7) {
                addDay(gc.getTime(), true);
            }
        }
        fireTableDataChanged();
    }

    /**
     * Пометить все воскресенья выходными
     */
    public void checkSunday() {
        Uses.log.logger.debug("Пометим все воскресенья");
        final GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date());
        final int ye = gc.get(GregorianCalendar.YEAR) % 4 == 0 ? 366 : 365;
        for (int d = 1; d <= ye; d++) {
            gc.set(GregorianCalendar.DAY_OF_YEAR, d);
            if (gc.get(GregorianCalendar.DAY_OF_WEEK) == 1) {
                addDay(gc.getTime(), true);
            }
        }
        fireTableDataChanged();
    }

    /**
     * Сохранить календарь.
     */
    public void save() {
        Uses.log.logger.info("Сохраняем календарь ID = " + calcId);
        final Session session = Uses.getSessionFactory().getSessionFactory().openSession();
        session.beginTransaction();
        try {
            //Удаляем старые
            for (FreeDay day : days_del) {
                session.delete(day);
            }

            Uses.log.logger.debug("Удалили старый календарь");
            // Сохраняем помеченные
            for (FreeDay day : days) {
                day.setId(null);
                session.saveOrUpdate(day);
            }
            session.getTransaction().commit();
            Uses.log.logger.debug("Сохранили новый календарь");
        } catch (DataException ex) {
            session.getTransaction().rollback();
            throw new Uses.ClientException("Ошибка выполнения операции изменения данных в БД(JDBC). Возможно введенные вами параметры не могут быть сохранены.\n[" + ex.getLocalizedMessage() + "]\n(" + ex.toString() + ")\nSQL: " + ex.getSQL());
        } catch (HibernateException ex) {
            session.getTransaction().rollback();
            String ss = "";
            for (String s : ex.getMessages()) {
                ss = ss + "\n" + s;
            }
            throw new Uses.ClientException("Ошибка системы взаимодействия с БД(Hibetnate).\n Возможно существуют вновь добавленные календари которые еще не сохранены.\nПопробуйте предварительно сохранить конфигурацию.\n[" + ex.getLocalizedMessage() + "]\n(" + ex.toString() + ")\nMessages: " + ss);
        } catch (Exception ex) {
            session.getTransaction().rollback();
            throw new Uses.ClientException("Ошибка при сохранении \n[" + ex.getLocalizedMessage() + "]\n(" + ex.toString() + "\n" + ex.getStackTrace() + ")");
        } finally {
            session.close();
        }
        //типо чтоб были актуальные внутренние данные
        days_del = new ArrayList<FreeDay>(days);
    }

    /**
     * Проверка на сохраненность календаря
     * @return
     */
    public boolean isSaved() {
        for (FreeDay day : days) {
            if (day.getId() == null) {
                return false;
            }
        }
        if (days_del.size() != days.size()) {
            return false;
        }
        return true;
    }
}
