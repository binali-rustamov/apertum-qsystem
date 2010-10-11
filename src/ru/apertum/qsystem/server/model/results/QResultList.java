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
package ru.apertum.qsystem.server.model.results;

import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import ru.apertum.qsystem.common.Uses;

/**
 *
 * @author Evgeniy Egorov
 */
public class QResultList extends DefaultListModel implements ComboBoxModel {

    /**
     * Singleton.
     */
    private static QResultList instance = null;

    /**
     * Доступ до Singleton
     * @return класс - список для обратной связи.
     */
    public static QResultList getResultList() {
        if (instance == null) {
            resetResultList();
        }
        return instance;
    }

     /**
     * Принудительное пересоздание списка результатов работы с клиентами. Доступ до Singleton
     * @return класс - список для обратной связи.
     */
    public static QResultList resetResultList() {
        instance = new QResultList();
        for (QResult item : instance.items) {
            instance.addElement(item);
        }
        Uses.log.logger.debug("Создали список результатов работы с клиентами для отмечания пользовотелем.");
        return instance;
    }
    /**
     * Все информационные узлы
     */
    protected final List<QResult> items = loadResultsItems();

    /**
     * Загрузка из базы всех результатов
     * @return список всех отзывов выкаченных из базы
     * @throws DataAccessException
     */
    private List<QResult> loadResultsItems() throws DataAccessException {

        return (List<QResult>) Uses.getSessionFactory().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                return (List<QResult>) session.createCriteria(QResult.class).list();
            }
        });
    }

    public Element getXML() {
        // Соберем xml список для плана
        Uses.log.logger.debug("Формируется XML-список для результатов.");
        // Найдем корень
        final Element rootItem = DocumentHelper.createElement(Uses.TAG_RESULT_ITEM);
        for (Object o : toArray()) {
            rootItem.add(((QResult) o).getXML());
        }
        return rootItem;
    }

    public QResult getByName(String text) {
        QResult res = null;
        for (Object o : toArray()) {
            if (text.equals(((QResult) o).getName())) {
                res = (QResult) o;
            }
        }
        if (res == null) {
            throw new Uses.ServerException("Не найден результат с текстом: \"" + text + "\"");
        }
        return res;
    }

    public QResult getByID(long id) {
        QResult res = null;
        for (Object o : toArray()) {
            if ( ((QResult) o).getId() == id ) {
                res = (QResult) o;
                break;
            }
        }
        return res;
    }

    public boolean hasByName(String name) {
        QResult res = null;
        for (Object o : toArray()) {
            if (name.equals(((QResult) o).getName())) {
                res = (QResult) o;
            }
        }
        return res != null;
    }

    private QResult selected;

    @Override
    public void setSelectedItem(Object anItem) {
        selected = (QResult) anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }
}
