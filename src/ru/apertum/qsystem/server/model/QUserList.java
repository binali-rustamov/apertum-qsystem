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
package ru.apertum.qsystem.server.model;

import java.util.Iterator;
import javax.swing.DefaultListModel;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import ru.apertum.qsystem.common.Uses;

/**
 * Список пользователей системы
 * Класс, управляющий пользователями системы.
 * @author Evgeniy Egorov
 */
public class QUserList extends DefaultListModel {

    /**
     * Singleton.
     */
    private static QUserList instance = null;

    /**
     * Доступ до Singleton
     * @param poolGetter свойства услуг, по которым построится дерево услуг.
     * @return класс - деерво услуг.
     */
    public static QUserList getUserList(IUsersGetter usersGetter) {
        if (instance == null) {
            resetUserList(usersGetter);
        }
        return instance;
    }

    /**
     * Принудительное пересоздание списка пользователей. Доступ до Singleton
     * @param poolGetter свойства услуг, по которым построится дерево услуг.
     * @return класс - деерво услуг.
     */
    public static QUserList resetUserList(IUsersGetter usersGetter) {

        instance = new QUserList();
        for (Iterator<IUserProperty> itr = usersGetter.iterator(); itr.hasNext();) {
            IUserProperty user = itr.next();
            instance.addElement(user instanceof QUser ? user : new QUser(user));
        }
        Uses.log.logger.debug("Создали список пользователей.");

        return instance;
    }

    public Element getXML() {
        // Соберем xml дерево пользователей
        Uses.log.logger.debug("Формируется XML-дерево пользователей.");
        // Найдем корень
        final Element rootUsers = DocumentHelper.createElement(Uses.TAG_PROP_USERS);
        for (Object o : toArray()) {
            rootUsers.add(((QUser) o).getXML());
        }
        return rootUsers;
    }

    public QUser getByName(String name) {
        QUser res = null;
        for (Object o : toArray()) {
            if (name.equals(((QUser) o).getName())) {
                res = (QUser) o;
            }
        }
        if (res == null) {
            throw new Uses.ServerException("Не найден пользователь по имени: \"" + name + "\"");
        }
        return res;
    }

    public boolean hasByName(String name) {
        QUser res = null;
        for (Object o : toArray()) {
            if (name.equals(((QUser) o).getName())) {
                res = (QUser) o;
            }
        }
        return res != null;
    }
}
