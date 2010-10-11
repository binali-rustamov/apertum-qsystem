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

import java.util.List;
import javax.swing.DefaultListModel;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import ru.apertum.qsystem.common.Uses;

/**
 *
 * Список сайтов домена.
 * @author Evgeniy Egorov
 */
public class QSiteList extends DefaultListModel {

    /**
     * Singleton.
     */
    private static QSiteList instance = null;

    /**
     * Доступ до Singleton
     * @param poolGetter свойства услуг, по которым построится дерево услуг.
     * @return класс - деерво услуг.
     */
    public static QSiteList getSiteList() {
        if (instance == null) {
            resetSiteList();
        }
        return instance;
    }

    /**
     * Принудительное пересоздание. Доступ до Singleton
     * @param poolGetter свойства услуг, по которым построится дерево услуг.
     * @return класс - деерво услуг.
     */
    public static QSiteList resetSiteList() {
        instance = new QSiteList();
        for (QSite site : instance.loadSites()) {
            instance.addElement(site);
            Uses.log.logger.trace("Добавили сайт \"" + site + "\".");
        }
        Uses.log.logger.debug("Создали список сайтов.");
        return instance;
    }

    private List<QSite> loadSites() throws DataAccessException {

        return (List<QSite>) Uses.getSessionFactory().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                return (List<QSite>) session.createCriteria(QSite.class).list();
            }
        });
    }

    public QSite getByMark(String siteMark) {
        QSite res = getSite(siteMark);
        if (res == null) {
            throw new Uses.ServerException("Не найден сайт с маркировкой: \"" + siteMark + "\"");
        }
        return res;
    }

    public boolean hasByMark(String siteMark) {
        return getSite(siteMark) != null;
    }

    private QSite getSite(String siteMark) {
        QSite res = null;
        for (Object o : toArray()) {
            if (siteMark.equals(o.toString())) {
                res = (QSite) o;
            }
        }
        return res;
    }
}
