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

import java.util.LinkedList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.exceptions.ServerException;

/**
 *
 * @author Evgeniy Egorov
 */
public class QCalendarList extends DefaultListModel implements ComboBoxModel {

    /**
     * Singleton.
     */
    private static QCalendarList instance = null;

    /**
     * Доступ до Singleton
     * @return класс - список для обратной связи.
     */
    public static QCalendarList getCalendarList() {
        if (instance == null) {
            resetCalendarList();
        }
        return instance;
    }

    /**
     * Принудительное пересоздание списка для обратной связи. Доступ до Singleton
     * @return класс - список для обратной связи.
     */
    public static QCalendarList resetCalendarList() {
        instance = new QCalendarList();
        for (QCalendar item : instance.items) {
            instance.addElement(item);
        }
        Uses.log.logger.debug("Создали список календарей.");
        return instance;
    }
    /**
     * Все информационные узлы
     */
    protected final List<QCalendar> items = loadCalendarItems();

    /**
     * Загрузка из базы всех календарей
     * @return список всех календарей выкаченных из базы
     * @throws DataAccessException
     */
    public static LinkedList<QCalendar> loadCalendarItems() throws DataAccessException {

        return (LinkedList<QCalendar>) Uses.getSessionFactory().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                return new LinkedList<QCalendar>(session.createCriteria(QCalendar.class).list());
            }
        });
    }

    public QCalendar getByName(String name) {
        QCalendar res = null;
        for (Object o : toArray()) {
            if (name.equals(((QCalendar) o).getName())) {
                res = (QCalendar) o;
            }
        }
        if (res == null) {
            throw new ServerException("Не найден календарь по имени: \"" + name + "\"");
        }
        return res;
    }

    public boolean hasByName(String name) {
        QCalendar res = null;
        for (Object o : toArray()) {
            if (name.equals(((QCalendar) o).getName())) {
                res = (QCalendar) o;
            }
        }
        return res != null;
    }

    private QCalendar selected;

    @Override
    public void setSelectedItem(Object anItem) {
        selected = (QCalendar) anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }
}
