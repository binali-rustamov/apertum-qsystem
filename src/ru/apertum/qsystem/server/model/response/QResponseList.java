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
package ru.apertum.qsystem.server.model.response;

import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.exceptions.ServerException;

/**
 *
 * @author Evgeniy Egorov
 */
public class QResponseList extends DefaultListModel {

   /**
     * Singleton.
     */
    private static QResponseList instance = null;

    /**
     * Доступ до Singleton
     * @return класс - список для обратной связи.
     */
    public static QResponseList getUserList() {
        if (instance == null) {
            resetResponseList();
        }
        return instance;
    }

    /**
     * Принудительное пересоздание списка для обратной связи. Доступ до Singleton
     * @return класс - список для обратной связи.
     */
    public static QResponseList resetResponseList() {
        instance = new QResponseList();
        for (QRespItem item : instance.items) {
            instance.addElement(item);
        }
        Uses.log.logger.debug("Создали список для обратной связи.");
        return instance;
    }
    /**
     * Все информационные узлы
     */
    protected final List<QRespItem> items = loadRespItems();

    /**
     * Загрузка из базы всех отзывов
     * @return список всех отзывов выкаченных из базы
     * @throws DataAccessException
     */
    private List<QRespItem> loadRespItems() throws DataAccessException {

        return (List<QRespItem>) Uses.getSessionFactory().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                return (List<QRespItem>) session.createCriteria(QRespItem.class).list();
            }
        });
    }

    @Deprecated
    public Element getXML() {
        // Соберем xml список для обратной связи
        Uses.log.logger.debug("Формируется XML-список для обратной связи.");
        // Найдем корень
        final Element rootItem = DocumentHelper.createElement(Uses.TAG_INFO_ITEM);
        for (Object o : toArray()) {
            rootItem.add(((QRespItem) o).getXML());
        }
        return rootItem;
    }

    public LinkedList<QRespItem> getQRespItems(){
        final LinkedList<QRespItem> list = new LinkedList<QRespItem>();
        CollectionUtils.addAll(list, elements());
        return list;
    }

    public QRespItem getByName(String name) {
        QRespItem res = null;
        for (Object o : toArray()) {
            if (name.equals(((QRespItem) o).getName())) {
                res = (QRespItem) o;
            }
        }
        if (res == null) {
            throw new ServerException("Не найден отзыв по имени: \"" + name + "\"");
        }
        return res;
    }

    public boolean hasByName(String name) {
        QRespItem res = null;
        for (Object o : toArray()) {
            if (name.equals(((QRespItem) o).getName())) {
                res = (QRespItem) o;
            }
        }
        return res != null;
    }
}
