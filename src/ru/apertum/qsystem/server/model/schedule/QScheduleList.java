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
package ru.apertum.qsystem.server.model.schedule;

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
public class QScheduleList extends DefaultListModel implements ComboBoxModel {

    /**
     * Singleton.
     */
    private static QScheduleList instance = null;

    /**
     * Доступ до Singleton
     * @return класс - список для обратной связи.
     */
    public static QScheduleList getScheduleList() {
        if (instance == null) {
            resetScheduleList();
        }
        return instance;
    }

    /**
     * Принудительное пересоздание списка для обратной связи. Доступ до Singleton
     * @return класс - список для обратной связи.
     */
    public static QScheduleList resetScheduleList() {
        instance = new QScheduleList();
        for (QSchedule item : instance.items) {
            instance.addElement(item);
        }
        Uses.log.logger.debug("Создали список для обратной связи.");
        return instance;
    }
    /**
     * Все информационные узлы
     */
    protected final List<QSchedule> items = loadScheduleItems();

    /**
     * Загрузка из базы всех отзывов
     * @return список всех отзывов выкаченных из базы
     * @throws DataAccessException
     */
    private LinkedList<QSchedule> loadScheduleItems() throws DataAccessException {

        return (LinkedList<QSchedule>) Uses.getSessionFactory().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                return new LinkedList<QSchedule>(session.createCriteria(QSchedule.class).list());
            }
        });
    }

    public QSchedule getByName(String name) {
        QSchedule res = null;
        for (Object o : toArray()) {
            if (name.equals(((QSchedule) o).getName())) {
                res = (QSchedule) o;
            }
        }
        if (res == null) {
            throw new ServerException("Не найден план по имени: \"" + name + "\"");
        }
        return res;
    }

    public boolean hasByName(String name) {
        QSchedule res = null;
        for (Object o : toArray()) {
            if (name.equals(((QSchedule) o).getName())) {
                res = (QSchedule) o;
            }
        }
        return res != null;
    }

    private QSchedule selected;

    @Override
    public void setSelectedItem(Object anItem) {
        selected = (QSchedule) anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }
}
