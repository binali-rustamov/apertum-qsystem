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
import java.util.LinkedList;
import java.util.List;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.DataException;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.reports.common.Report;
import ru.apertum.qsystem.server.model.calendar.QCalendar;
import ru.apertum.qsystem.server.model.infosystem.QInfoItem;
import ru.apertum.qsystem.server.model.response.QRespItem;
import ru.apertum.qsystem.server.model.results.QResult;
import ru.apertum.qsystem.server.model.schedule.QSchedule;

/**
 * Builder паттерна Builder (Строитель) для его создания свойств сервера из БД.
 * Паттерн Builder (Строитель)
 * Билдер свойства сервера на основе БД.
 * @author Evgeniy Egorov
 * @see http://www.javenue.info/post/58
 */
public class FromDBBuilder extends AServerPropertyBuilder {

    public FromDBBuilder() {
    }
    /**
     * Эта переменная управляет сессией хибера.
     */
    private static HibernateTemplate hibernateTemplate;

    /**
     * Так через Spring мы установим фабрику сессий.
     * @param sessionFactory этот рапаметр определяется в Spring
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        hibernateTemplate = new HibernateTemplate(sessionFactory);
        Uses.setSessionFactory(hibernateTemplate);
    }

    @Override
    public void buildNetProperty() {
        try {
            serverGetter.setNetProperty(loadNetByID(new Long(1)));
        } catch (Exception ex) {
            throw new Uses.ServerException("Не созданы сетевые настройки." + ex.toString());
        }
    }

    private NetProperty loadNetByID(final Long id) throws DataAccessException {

        return (NetProperty) hibernateTemplate.execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                return session.get(NetProperty.class, id);
            }
        });
    }

    @Override
    public void buildUsersGetter() {
        try {
            serverGetter.setUsersGetter(new UsersGetter());
        } catch (Exception ex) {
            throw new Uses.ServerException("Не созданы настройки пользователей." + ex.toString());
        }
    }

    @Override
    public void buildPoolGetter() {
        try {
            serverGetter.setPoolGetter(new PoolGetter());
        } catch (Exception ex) {
            throw new Uses.ServerException("Не созданы настройки сервисов." + ex.toString());
        }
    }

    @Override
    public void buildReports() {
        serverGetter.setReports(loadReports());
    }

    private List<Report> loadReports() throws DataAccessException {

        return (List<Report>) hibernateTemplate.execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                return (List<Report>) session.createCriteria(Report.class).list();
            }
        });
    }

    @Override
    public void buildPoolSaver() throws Uses.ClientException {
        serverGetter.setPoolSaver(new IPoolSaver() {

            @Override
            public void save(QServicesPool pool) {
                final long start = System.currentTimeMillis();
                Uses.log.logger.info("Сохранение конфигурации.");

                final Session session = Uses.getSessionFactory().getSessionFactory().openSession();
                session.beginTransaction();
                try {
                    // Убиваем убиенных
                    for (Object dead : pool.getKilled()) {
                        session.delete(dead);
                    }
                    pool.getKilled().clear();
                    // Сохраняем услуги
                    pool.getServices().sailToStorm(pool.getServices().getRoot(), new ISailListener() {

                        @Override
                        public void actionPerformed(QService service) {
                            session.saveOrUpdate(service);
                        }
                    });
                    // Сохраняем пользователей
                    for (Object o : pool.getUserList().toArray()) {
                        final QUser user = (QUser) o;
                        session.saveOrUpdate(user);
                        for (Object servicePlan : user.getServiceList().toArray()) {
                            ((QPlanService) servicePlan).setUserId(user.getId());
                            session.saveOrUpdate(servicePlan);
                        }
                    }
                    // Сохраняем сайты
                    for (Object o : pool.getSiteList().toArray()) {
                        final QSite site = (QSite) o;
                        session.saveOrUpdate(site);
                    }

                    //Сохраняем сетевые настройки
                    session.saveOrUpdate(pool.getNetPropetry());

                    // Сохраняем инфоузлы
                    pool.getInfoTree().sailToStorm(pool.getInfoTree().getRoot(), new ru.apertum.qsystem.server.model.infosystem.ISailListener() {

                        @Override
                        public void actionPerformed(QInfoItem item) {
                            if (item.getId() != null) {
                                session.saveOrUpdate(item);
                            }
                        }
                    });

                    // Сохраняем отзывы
                    for (Object o : pool.getResponseList().toArray()) {
                        final QRespItem item = (QRespItem) o;
                        session.saveOrUpdate(item);
                    }

                    // Сохраняем результаты работы пользователя с клиентами
                    for (Object o : pool.getResultsList().toArray()) {
                        final QResult item = (QResult) o;
                        session.saveOrUpdate(item);
                    }

                    // Сохраняем планы расписания
                    for (Object o : pool.getScheduleList().toArray()) {
                        final QSchedule item = (QSchedule) o;
                        session.saveOrUpdate(item);
                    }

                    // Сохраняем календари услуг
                    for (Object o : pool.getCalendarList().toArray()) {
                        final QCalendar item = (QCalendar) o;
                        session.saveOrUpdate(item);
                    }

                    session.getTransaction().commit();
                } catch (DataException ex) {
                    session.getTransaction().rollback();
                    throw new Uses.ClientException("Ошибка выполнения операции изменения данных в БД(JDBC). Возможно введенные вами параметры не могут быть сохранены.\n[" + ex.getLocalizedMessage() + "]\n(" + ex.toString() + ")\nSQL: " + ex.getSQL());
                } catch (HibernateException ex) {
                    session.getTransaction().rollback();
                    String ss = "";
                    for (String s : ex.getMessages()) {
                        ss = ss + "\n" + s;
                    }
                    throw new Uses.ClientException("Ошибка системы взаимодействия с БД(Hibetnate). Возможно некорректное поведение соответствующей библиотеки.\n[" + ex.getLocalizedMessage() + "]\n(" + ex.toString() + ")\nMessages: " + ss);
                } catch (Exception ex) {
                    session.getTransaction().rollback();
                    throw new Uses.ClientException("Ошибка при сохранении \n[" + ex.getLocalizedMessage() + "]\n(" + ex.toString() + "\n" + ex.getStackTrace() + ")");
                } finally {
                    session.close();
                }


                Uses.log.logger.info("Состояние сохранено. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
            }
        });
    }
//********************************************************************************
//****************************  PoolGetter  ************************************   

    static class PoolGetter implements IPoolGetter {

        final private List<IServiceProperty> services;
        /**
         * XML-элемент корень дерева, описывающего всю иерархию услуг
         */
        private final Element rootPoolProperty;

        public PoolGetter() throws DocumentException {
            services = loadServices();
            // Соберем xml-дерево услуг
            // Найдем корень
            final IServiceProperty srv = getRoot();
            rootPoolProperty = srv.getXML();
            // Построем рекурентно
            bildTree(srv, rootPoolProperty);
        }

        private void bildTree(IServiceProperty service, Element xmlServ) {
            for (IServiceProperty srv : services) {
                if (service.getId().equals(srv.getParentId())) {
                    xmlServ.add(srv.getXML());
                    bildTree(srv, srv.getXML());
                }
            }
        }

        private List<IServiceProperty> loadServices() throws DataAccessException {

            return (List) hibernateTemplate.execute(new HibernateCallback() {

                @Override
                public Object doInHibernate(Session session) {
                    return (List) session.createCriteria(QService.class).list();
                }
            });
        }

        /**
         * Получчить корень описения всех услуг в xml виде для передачи на клиента.
         * @return XML-элемент корень дерева, описывающего всю иерархию услуг
         */
        @Override
        public Element getXML() {
            return rootPoolProperty;
        }

        @Override
        public IServiceProperty getRoot() {
            IServiceProperty srv = null;
            for (IServiceProperty service : services) {
                if (service.getParentId() == null) {
                    srv = service;
                    break;
                }
            }
            if (srv == null) {
                throw new Uses.ServerException("Невозможно определить корень описчания услуг.");
            }
            return srv;
        }

        @Override
        public LinkedList<IServiceProperty> getChildren(IServiceProperty parent) {
            LinkedList<IServiceProperty> list = new LinkedList<IServiceProperty>();
            for (IServiceProperty srv : services) {
                if (parent.getId().equals(srv.getParentId())) {
                    list.add(srv);
                }
            }
            return list;
        }
    }
    //*********************************************************************************
    //****************************  UsersGetter  ************************************  

    static class UsersGetter implements IUsersGetter {

        final private List<IUserProperty> users;
        /**
         * XML-элемент корень дерева, описывающего всю иерархию пользователей
         */
        private final Element rootUsersProperty;

        public UsersGetter() {
            users = loadUsers();
            // Соберем xml дерево услуг
            // Найдем корень
            rootUsersProperty = DocumentHelper.createElement(Uses.TAG_PROP_USERS);
            for (IUserProperty user : users) {
                rootUsersProperty.add(user.getXML());
            }
        }

        private List<IUserProperty> loadUsers() throws DataAccessException {

            return (List) hibernateTemplate.execute(new HibernateCallback() {

                @Override
                public Object doInHibernate(Session session) {
                    return (List) session.createCriteria(QUser.class).list();
                }
            });
        }

        @Override
        public Element getXML() {
            return rootUsersProperty;
        }

        @Override
        public Iterator<IUserProperty> iterator() {
            return users.iterator();
        }
    }
}
