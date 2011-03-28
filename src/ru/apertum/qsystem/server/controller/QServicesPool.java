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
package ru.apertum.qsystem.server.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ru.apertum.qsystem.common.SoundPlayer;
import java.io.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import org.dom4j.DocumentException;
import ru.apertum.qsystem.common.model.IProperty;
import ru.apertum.qsystem.common.model.QCustomer;
import ru.apertum.qsystem.common.model.ICustomer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.cmd.CmdParams;
import ru.apertum.qsystem.common.cmd.JsonRPC20;
import ru.apertum.qsystem.common.model.ATalkingClock;
import ru.apertum.qsystem.reports.model.CurrentStatistic;
import ru.apertum.qsystem.reports.model.WebServer;
import ru.apertum.qsystem.server.model.AServerPropertyBuilder;
import ru.apertum.qsystem.server.model.IPoolSaver;
import ru.apertum.qsystem.server.model.ISailListener;
import ru.apertum.qsystem.server.model.IServerGetter;
import ru.apertum.qsystem.server.model.NetProperty;
import ru.apertum.qsystem.server.model.QAdvanceCustomer;
import ru.apertum.qsystem.server.model.QAuthorizationCustomer;
import ru.apertum.qsystem.server.model.QService;
import ru.apertum.qsystem.server.model.QServiceTree;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.QUserList;
import ru.apertum.qsystem.server.model.calendar.CalendarTableModel;
import ru.apertum.qsystem.server.model.calendar.FreeDay;
import ru.apertum.qsystem.server.model.calendar.QCalendarList;
import ru.apertum.qsystem.server.model.infosystem.QInfoItem;
import ru.apertum.qsystem.server.model.infosystem.QInfoTree;
import ru.apertum.qsystem.server.model.response.QRespEvent;
import ru.apertum.qsystem.server.model.response.QResponseList;
import ru.apertum.qsystem.server.model.results.QResult;
import ru.apertum.qsystem.server.model.results.QResultList;
import ru.apertum.qsystem.server.model.schedule.QSchedule;
import ru.apertum.qsystem.server.model.schedule.QScheduleList;

/**
 * Пул очередей.
 * Пул очередей - главная структура управления очередями.
 * В системе существуют несколько очередей, например для оказания разных услуг.
 * Пул получает XML-задания из сети, определяет требуемое действие.
 * Выполняет действия по организации пула.
 * Выполняет задания, касающиеся нескольких очередей.
 * Работает как singleton.
 * @author Evgeniy Egorov
 */
public final class QServicesPool {

    /**
     * Дерево услуг.
     */
    private QServiceTree serviceTree;

    public QServiceTree getServices() {
        return serviceTree;
    }
    /**
     * Дерево информационной системы
     */
    private QInfoTree infoTree;

    public QInfoTree getInfoTree() {
        return infoTree;
    }
    /**
     * Список юзеров.
     */
    private QUserList userList;
    /**
     * Список удаленных объектов
     */
    private final ArrayList<Object> killed = new ArrayList<Object>();

    public void addKilled(Object dead) {
        killed.add(dead);
    }

    public ArrayList getKilled() {
        return killed;
    }

    public QUserList getUserList() {
        return userList;
    }
    /**
     * Список ответов для обратной связи
     */
    private QResponseList responseList;

    public QResponseList getResponseList() {
        return responseList;
    }
    /**
     * Список результатов для выбора пользователем после работы с клиентом
     */
    private QResultList resultsList;

    public QResultList getResultsList() {
        return resultsList;
    }
    /**
     * Список ответов для списка планов
     */
    private QScheduleList scheduleList;

    public QScheduleList getScheduleList() {
        return scheduleList;
    }
    /**
     * Список календарец работы услуг
     */
    private QCalendarList calendarList;

    public QCalendarList getCalendarList() {
        return calendarList;
    }
    /**
     * сетевае настройки сервера
     */
    private NetProperty netProp;

    public NetProperty getNetPropetry() {
        return netProp;
    }

    private void setNetPropetry(NetProperty netProperty) {
        netProp = netProperty;
    }

    /**
     * имеется ли интеграция с видеонаблюдением.
     * @return есть или нету
     */
    private boolean isObservation() {
        return Uses.spring.factory.containsBean("observation");
    }
    /**
     * Текущая статистика
     */
    private final CurrentStatistic statistic;
    /**
     * Клиентское табло.
     */
    private static IIndicatorBoard indicatorBoard;

    public IIndicatorBoard getIndicatorBoard() {
        return indicatorBoard;
    }
    //
    //*******************************************************************************************************
    //**************************  ОБРАБОТЧИКИ ЗАДАНИЙ *******************************************************
    //*******************************************************************************************************
    //
    // задния, доступны по их именам
    private final HashMap<String, Task> tasks = new HashMap<String, Task>();

    /**
     * 
     * @author Evgeniy Egorov
     * Базовый класс обработчиков заданий.
     * сам себя складывает в HashMap<String, ATask> tasks.
     * метод process исполняет задание.
     */
    private class Task {

        protected final String name;
        protected CmdParams cmdParams;

        public Task(String name) {
            this.name = name;
            tasks.put(name, this);
        }

        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            Uses.log.logger.debug("Выполняем : \"" + name + "\"");
            this.cmdParams = cmdParams;
            return "";
        }
    }
    /**
     * Ключ блокировки для манипуляции с кстомерами
     */
    private final Lock clientTaskLock = new ReentrantLock();
    /**
     * Ставим кастомера в очередь.  
     */
    final Task addCustomerTask = new Task(Uses.TASK_STAND_IN) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final QService service = serviceTree.getByName(cmdParams.serviceName);
            final QCustomer customer;
            final Element elCustomer;
            // синхронизируем работу с клиентом
            clientTaskLock.lock();
            try {
                // Создадим вновь испеченного кастомера
                customer = new QCustomer(service.getNextNumber());
                // Определим кастомера в очередь
                customer.setService(service);
                // время постановки проставляется автоматом при создании кастомера.
                // Приоритет "как все"
                customer.setPriority(cmdParams.priority);
                // Введенные кастомером данные
                customer.setInput_data(cmdParams.textData);
                // Состояние у него "Стою, жду".
                customer.setState(Uses.STATE_WAIT);
                //добавим нового пользователя
                elCustomer = service.addCustomer(customer);
                // костыль. если услуга требует ввода пользователем, то на пичать отправлять не просто кастомера,
                // а еще и с капшеном того что он вводил для печати на номерке
                if (service.getInput_required()) {
                    elCustomer.addAttribute(Uses.TAG_PROP_INPUT_CAPTION, service.getInput_caption());
                }
            } catch (Exception ex) {
                throw new Uses.ServerException("Ошибка при постановке клиента в очередь" + ex);
            } finally {
                clientTaskLock.unlock();
            }
            Uses.log.logger.trace("С приоритетом " + customer.getPriority().get() + " К услуге \"" + cmdParams.serviceName + "\" -> " + service.getPrefix() + '\'' + service.getName() + '\'');
            // если кастомер добавился, то его обязательно отправить в ответ
            // он уже есть в системе
            try {
                // сохраняем состояния очередей.
                savePool();
                //Запишим в статистику этот момент
                statistic.processingSetWaitCustomers(cmdParams.serviceName, service.getCountCustomers());
                //разослать оповещение о том, что появился посетитель
                //рассылаем широковещетельно по UDP на определенный порт
                Uses.sendUDPBroadcast(cmdParams.serviceName, netProp.getClientPort());
            } finally {
                return elCustomer.asXML();
            }
        }
    };
    /**
     * Пригласить кастомера, первого в очереди.
     */
    final Task inviteCustomerTask = new Task(Uses.TASK_INVITE_NEXT_CUSTOMER) {

        /**
         * Cинхронизируем, ато вызовут одного и того же.
         * А еще сдесь надо вызвать метод, который "проговорит" кого и куда вазвали.
         * Может случиться ситуация когда двое вызывают последнего кастомера, первому достанется, а второму нет.
         */
        @Override
        synchronized String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            // Определить из какой очереди надо выбрать кастомера.
            // Пока без учета коэфициента.
            // Для этого смотрим первых кастомеров во всех очередях и ищем первого среди первых.
            final QUser user = userList.getByName(cmdParams.userName); // юзер
            final boolean isRecall = user.getCustomer() != null;

            // есть ли у юзера вызванный кастомер? Тогда поторный вызов
            if (isRecall) {
                Uses.log.logger.debug("Повторный вызов кастомера №" + user.getCustomer().getPrefix() + user.getCustomer().getNumber() + " пользователем " + cmdParams.userName);

                // просигналим звуком
                //SoundPlayer.play("/ru/apertum/qsystem/server/sound/sound.wav");
                SoundPlayer.inviteClient(user.getCustomer().getPrefix() + user.getCustomer().getNumber(), user.getPoint());

                //разослать оповещение о том, что посетитель вызван повторно
                //рассылаем широковещетельно по UDP на определенный порт. Должно высветитьсяна основном табло
                indicatorBoard.inviteCustomer(user, user.getCustomer());

                return user.getCustomer().toString();
            }

            // бежим по очередям юзера и ищем первого из первых кастомера
            ICustomer customer = null;
            int servPriority = -1;// временная переменная для приоритета услуг
            // синхронизация работы с клиентом
            clientTaskLock.lock();
            try {
                for (Iterator<IProperty> i = user.getUserPlan(); i.hasNext();) {
                    IProperty plan = i.next();
                    final QService serv = serviceTree.getByName(plan.getName()); // очередная очередь

                    final ICustomer cust = serv.peekCustomer(); // первый в этой очереди
                    // если очередь пуста

                    if (cust == null) {
                        continue;
                    }

                    // учтем приоритетность кастомеров и приоритетность очередей для юзера в которые они стоят
                    final Integer prior = (Integer) plan.getValue();
                    if (prior > servPriority || (prior == servPriority && customer.compareTo(cust) == 1)) {
                        servPriority = prior;
                        customer = cust;
                    }
                }
                //Найденного самого первого из первых кастомера переносим на хранение юзеру, при этом удалив его из общей очереди.
                // Случай, когда всех разобрали, но вызов сделан
                //При приглашении очередного клиента пользователем очереди оказались пустые.
                if (customer == null) {
                    return "<" + Uses.TAG_EMPTY + "/>";
                }
                customer = serviceTree.getByName(customer.getServiceName()).polCustomer();
            } catch (Exception ex) {
                throw new Uses.ServerException("Ошибка при постановке клиента в очередь" + ex);
            } finally {
                clientTaskLock.unlock();
            }
            if (customer == null) {
                throw new Uses.ServerException("Странная проблема с получением кастомера и удалением его из очереди.");
            }
            // определим юзеру кастомера, которого он вызвал.
            user.setCustomer(customer);
            // Поставил кастомеру юзера, который его вызвал.
            if (customer instanceof QCustomer) {
                ((QCustomer) customer).setUser(user);
            } else {
                throw new Uses.ServerException("Если это не QCustomer, то кто???. Возможно появилась новая реализация ICustomer.");
            }
            // ставим время вызова
            customer.setCallTime(new Date());
            // кастомер переходит в состояние "приглашенности"
            customer.setState(Uses.STATE_INVITED);

            // если кастомер вызвался, то его обязательно отправить в ответ
            // он уже есть у юзера
            try {
                // просигналим звуком
                //SoundPlayer.play("/ru/apertum/qsystem/server/sound/sound.wav");
                SoundPlayer.inviteClient(user.getCustomer().getPrefix() + user.getCustomer().getNumber(), user.getPoint());
                // сохраняем состояния очередей.
                savePool();
                //разослать оповещение о том, что появился вызванный посетитель
                // Должно высветитьсяна основном табло
                indicatorBoard.inviteCustomer(user, user.getCustomer());
                //разослать оповещение о том, что посетителя вызвали, состояние очереди изменилось
                //рассылаем широковещетельно по UDP на определенный порт
                Uses.sendUDPBroadcast(customer.getServiceName(), netProp.getClientPort());
            } finally {
                if (serviceTree.getByName(customer.getServiceName()).getResult_required()) {
                    customer.toXML().addAttribute(Uses.TAG_PROP_RESULT_REQUIRED, "1");
                }
                return customer.toString();

            }
        }
    };
    /**
     * Получить перечень услуг
     */
    final Task getServicesTask = new Task(Uses.TASK_GET_SERVICES) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final Element el = serviceTree.getXML();
            el.addAttribute(Uses.TAG_START_TIME, String.valueOf(netProp.getStartTime().getTime()));
            el.addAttribute(Uses.TAG_FINISH_TIME, String.valueOf(netProp.getFinishTime().getTime()));
            return el.asXML();
        }
    };
    /**
     * Получить описание состояния услуги
     */
    final Task aboutTask = new Task(Uses.TASK_ABOUT_SERVICE) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            // Проверим оказывается ли сейчас эта услуга
            int min = Uses.LOCK_INT;
            final Date day = new Date();
            final QService srv = serviceTree.getByName(cmdParams.serviceName);
            // Если нет расписания, календаря или выходной то отказ по расписанию
            if (srv.getSchedule() == null || checkFreeDay(day, new Long(1)) || (srv.getCalendar() != null && checkFreeDay(day, srv.getCalendar().getId()))) {
                min = Uses.LOCK_FREE_INT;
            } else {
                // Определим время начала и нонца работы на этот день
                final QSchedule sch = srv.getSchedule();
                final GregorianCalendar gc_day = new GregorianCalendar();
                gc_day.setTime(day);
                Date start = null;
                Date end = null;
                if (sch.getType() == 1) {
                    if (0 == (gc_day.get(GregorianCalendar.DAY_OF_MONTH) % 2)) {
                        start = sch.getTime_begin_1();
                        end = sch.getTime_end_1();
                    } else {
                        start = sch.getTime_begin_2();
                        end = sch.getTime_end_2();
                    }
                } else {
                    switch (gc_day.get(GregorianCalendar.DAY_OF_WEEK)) {
                        case 2:
                            start = sch.getTime_begin_1();
                            end = sch.getTime_end_1();
                            break;
                        case 3:
                            start = sch.getTime_begin_2();
                            end = sch.getTime_end_2();
                            break;
                        case 4:
                            start = sch.getTime_begin_3();
                            end = sch.getTime_end_3();
                            break;
                        case 5:
                            start = sch.getTime_begin_4();
                            end = sch.getTime_end_4();
                            break;
                        case 6:
                            start = sch.getTime_begin_5();
                            end = sch.getTime_end_5();
                            break;
                        case 7:
                            start = sch.getTime_begin_6();
                            end = sch.getTime_end_6();
                            break;
                        case 1:
                            start = sch.getTime_begin_7();
                            end = sch.getTime_end_7();
                            break;
                        default:
                            ;
                    }
                }// Определили надало и конец рабочего дня на сегодня
                // Если работаем в этот день то определим попадает ли "сейчас" в рабочий промежуток
                if (!(start == null || end == null)) {
                    final int h = gc_day.get(GregorianCalendar.HOUR_OF_DAY);
                    final int m = gc_day.get(GregorianCalendar.MINUTE);
                    gc_day.setTime(start);
                    final int sh = gc_day.get(GregorianCalendar.HOUR_OF_DAY);
                    final int sm = gc_day.get(GregorianCalendar.MINUTE);
                    gc_day.setTime(end);
                    final int eh = gc_day.get(GregorianCalendar.HOUR_OF_DAY);
                    final int em = gc_day.get(GregorianCalendar.MINUTE);
                    if (!(sh * 60 + sm <= h * 60 + m && h * 60 + m <= eh * 60 + em) && (!((sh == eh) && (sm == em)))) {
                        min = Uses.LOCK_FREE_INT;
                    }
                } else {
                    min = Uses.LOCK_FREE_INT;
                }
            }
            // Если не работаем, то отправим ответ и прекратим выполнение
            if (min == Uses.LOCK_FREE_INT) {
                Uses.log.logger.warn("Услуга \"" + cmdParams.serviceName + "\" не обрабатывается исходя из рабочего расписания.");
                return "<Ответ " + Uses.TAG_DESCRIPTION + "=\"" + min + "\"/>";
            }
            // бежим по юзерам и смотрим обрабатывают ли они услугу
            // если да, то возьмем все услуги юзера и  сложим всех кастомеров в очередях
            // самую маленькую сумму отправим в ответ по запросу.
            for (Object o : userList.toArray()) {
                final QUser user = (QUser) o;
                if (user.hasService(cmdParams.serviceName)) {
                    // теперь по услугам юзера
                    final Iterator<IProperty> itr = user.getUserPlan();
                    int sum = 0;
                    for (Iterator<IProperty> i = itr; i.hasNext();) {
                        final String servName = i.next().getName();
                        final QService service = serviceTree.getByName(servName);
                        sum = sum + service.getCountCustomers();
                    }
                    if (min > sum) {
                        min = sum;
                    }
                }
            }
            if (min == Uses.LOCK_INT) {
                Uses.log.logger.warn("Услуга \"" + cmdParams.serviceName + "\" не обрабатывается ни одним пользователем.");
            }
            return "<Ответ " + Uses.TAG_DESCRIPTION + "=\"" + min + "\"/>";
        }
    };
    /**
     * Получить описание пользователя по паролю
     */
    final Task getSelfTask = new Task(Uses.TASK_GET_SELF) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            for (Object o : userList.toArray()) {
                if (((QUser) o).getPassword().equals(cmdParams.password)) {
                    return ((QUser) o).getXML().asXML();
                }
            }
            return null;
        }
    };
    /**
     * Получить описание пользователей для выбора
     */
    final Task getUsersTask = new Task(Uses.TASK_GET_USERS) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            checkUserLive.refreshUsers();
            return userList.getXML().asXML();
        }
    };
    /**
     * Получить состояние сервера.
     */
    private final Task getServerState = new Task(Uses.TASK_SERVER_STATE) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            count = 0;
            super.process(cmdParams, ipAdress, IP);
            final Element root;
            try {
                root = DocumentHelper.parseText("<" + Uses.TAG_PROP_SERVICES + "/>").getRootElement();
            } catch (DocumentException ex) {
                throw new Uses.ServerException("Ошибка при формировании корня XML документа. " + ex);
            }

            serviceTree.sailToStorm(serviceTree.getRoot(), new ISailListener() {

                @Override
                public void actionPerformed(QService service) {

                    final Element serv = service.getXML();
                    serv.addAttribute(Uses.TAG_REP_SERVICE_WAIT, String.valueOf(service.getCountCustomers()));
                    final ICustomer customer = service.peekCustomer();
                    serv.addAttribute(Uses.TAG_CUSTOMER, customer != null ? customer.getPrefix() + customer.getNumber() : "Ожидающих нет");
                    count = count + service.getCountCustomers();
                    root.add(serv);
                }
            });


            root.addAttribute(Uses.TAG_REP_SERVICE_WAIT, String.valueOf(count));
            count = 0;
            return root.asXML();
        }
        private int count = 0;
    };
    /**
     * Получить подтверждение о живучести.
     */
    private final LiveTask checkUserLive = new LiveTask(Uses.TASK_I_AM_LIVE);

    private class LiveTask extends Task {

        public LiveTask(String name) {
            super(name);
        }
        /**
         * Имя пользователя -> его адрес
         */
        private final HashMap<String, String> addrByName = new HashMap<String, String>();
        /**
         * Адрес пользователя -> его имя
         */
        private final HashMap<String, String> nameByAddr = new HashMap<String, String>();
        /**
         * Адрес пользователя -> его байтовое прдставление
         */
        private final HashMap<String, byte[]> ipByAddr = new HashMap<String, byte[]>();

        public boolean hasName(String userName) {
            return addrByName.get(userName) != null;
        }

        /**
         * Опросим всю сетку на предмет пользователей.
         */
        public void refreshUsers() {
            // подотрем все списки
            final int i = ipByAddr.size();
            ipByAddr.clear();
            nameByAddr.clear();
            addrByName.clear();
            // полная рассылка
            Uses.sendUDPBroadcast(Uses.HOW_DO_YOU_DO, netProp.getClientPort());
            try {
                int k = 0;
                while (ipByAddr.size() < i && k < 8) {
                    k++;
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ex) {
                throw new Uses.ServerException("Таймер. " + ex.toString());
            }
        }

        /**
         * Проверка залогиневшегося чела по имени
         * @param userName имя чеда для проверки
         * @return есть юзер с таким именем или нет
         */
        public boolean checkUserName(String userName) {
            if (addrByName.get(userName) != null) {
                final byte[] ip = ipByAddr.get(addrByName.get(userName));
                Uses.log.logger.debug("Отправить запрос на подтверждение активности на \"" + addrByName.get(userName) + "\" пользователя \"" + userName + "\".");
                // подотрем перед проверкой
                nameByAddr.remove(addrByName.get(userName));
                ipByAddr.remove(addrByName.get(userName));
                addrByName.remove(userName);
                // проверим
                try {
                    Uses.sendUDPMessage(Uses.HOW_DO_YOU_DO, InetAddress.getByAddress(ip), netProp.getClientPort());
                } catch (UnknownHostException ex) {
                    throw new Uses.ServerException("Че адрес не существует??? " + new String(ip) + " " + ex);
                }
                // подождем ответа
                int i = 0;
                while (addrByName.get(userName) == null && i < 70) {
                    i++;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        throw new Uses.ServerException("Таймер. " + ex.toString());
                    }
                }
                return addrByName.get(userName) != null;
            } else {
                return false;
            }
        }

        /**
         * Проверка залогиневшегося чела по адресу
         * @param ipAdress адрес для проверки
         * @return есть там юзер или нет
         */
        public boolean checkUserAddress(String ipAdress) throws UnknownHostException {
            if (nameByAddr.get(ipAdress) != null) {
                final byte[] ip = ipByAddr.get(ipAdress);
                Uses.log.logger.debug("Отправить запрос на подтверждение активности на \"" + ipAdress + "\" пользователя \"" + nameByAddr.get(ipAdress) + "\".");
                // подотрем перед проверкой
                addrByName.remove(nameByAddr.get(ipAdress));
                nameByAddr.remove(ipAdress);
                ipByAddr.remove(ipAdress);
                // проверим
                try {
                    Uses.sendUDPMessage(Uses.HOW_DO_YOU_DO, InetAddress.getByAddress(ip), netProp.getClientPort());
                } catch (UnknownHostException ex) {
                    throw new Uses.ServerException("Че адрес не существует??? " + ipAdress + " " + ex);
                }
                // подождем ответа
                int i = 0;
                while (nameByAddr.get(ipAdress) == null && i < 70) {
                    i++;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        throw new Uses.ServerException("Таймер. " + ex.toString());
                    }
                }
                return nameByAddr.get(ipAdress) != null;
            } else {
                return false;
            }
        }

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            addrByName.put(cmdParams.userName, ipAdress);
            nameByAddr.put(ipAdress, cmdParams.userName);
            ipByAddr.put(ipAdress, IP);
            return "<Ответ>\n</Ответ>";
        }
    };
    /**
     * Получить описание состояния очередей для пользователя.
     */
    final Task getSelfServicesTask = new SelfServicesTask(Uses.TASK_GET_SELF_SERVICES);
    /**
     * Получить описание состояния очередей для пользователя и проверить
     * Отсечем дубляжи запуска от одних и тех же юзеров. но с разных компов
     */
    final Task getSelfServicesCheckTask = new SelfServicesCheckTask(Uses.TASK_GET_SELF_SERVICES_CHECK);

    private class SelfServicesTask extends Task {

        public SelfServicesTask(String name) {
            super(name);
        }
        /**
         * Шаблон XML-ответа, это правильный XML-документ.
         */
        private static final String NAME = "%SERVICENAME%";
        private static final String COUNT = "%COUNT%";
        private static final String TEMP = "    <" + Uses.TAG_SERVICE + " " + Uses.TAG_NAME + "=\"" + NAME + "\" " + Uses.TAG_DESCRIPTION + "=\"" + COUNT + "\"/>\n";

        @Override
        protected String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            String res = "";
            final QUser user = userList.getByName(cmdParams.userName);

            //*************************************************************************
            final Iterator<IProperty> itr = user.getUserPlan();
            for (Iterator<IProperty> i = itr; itr.hasNext();) {
                final IProperty prop = i.next();
                final String serviceName = prop.getName();
                final QService service = serviceTree.getByName(serviceName);
                res = res + TEMP;
                res = res.replaceFirst(NAME, serviceName);
                res = res.replaceFirst(COUNT, String.valueOf(service.getCountCustomers()));
            }
            // нужно сделать вставочку приглашенного юзера, если он есть
            return "<Ответ>\n" + res + (user.getCustomer() == null ? "" : user.getCustomer().toString()) + "\n</Ответ>";
        }
    };

    private class SelfServicesCheckTask extends SelfServicesTask {

        public SelfServicesCheckTask(String name) {
            super(name);
        }

        @Override
        protected String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            // Отсечем дубляжи запуска от одних и тех же юзеров. но с разных компов
            // пришло с запросом от юзера имеющегося в региных
            //System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" + userName);
            if (checkUserLive.hasName(cmdParams.userName)) {
                Uses.log.logger.debug(Uses.ACCESS_DENY);
                return "<Ответ>\n" + Uses.ACCESS_DENY + "\n</Ответ>";
            }

            return super.process(cmdParams, ipAdress, IP);
        }
    }
    /**
     * Удалить вызванного юзером кастомера по неявке.
     */
    final Task killCustomerTask = new Task(Uses.TASK_KILL_NEXT_CUSTOMER) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final QUser user = userList.getByName(cmdParams.userName);
            // кастомер переходит в состояние "умерщвленности"
            user.getCustomer().setState(Uses.STATE_DEAD);

            // Кое-что для статистики
            final String serviceName = user.getCustomer().getServiceName();
            final int size = serviceTree.getByName(serviceName).getCountCustomers();

            try {
                user.setCustomer(null);//бобик сдох и медальки не осталось

                // сохраняем состояния очередей.
                savePool();
                //Запишим в статистику этот момент
                statistic.processingKillCustomer(serviceName, cmdParams.userName);
                statistic.processingSetWaitCustomers(serviceName, size);
                //разослать оповещение о том, что посетитель откланен
                //рассылаем широковещетельно по UDP на определенный порт. Должно высветитьсяна основном табло
                indicatorBoard.killCustomer(user);
            } finally {
                return "<Ответ>\n</Ответ>";
            }
        }
    };
    /**
     * Начать работу с вызванноым кастомером.
     */
    final Task getStartCustomerTask = new Task(Uses.TASK_START_CUSTOMER) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final QUser user = userList.getByName(cmdParams.userName);
            // Время старта работы с юзера с кастомером.
            user.getCustomer().setStartTime(new Date());
            // редиректенный ли или возвращенный
            final boolean isRedirected = user.getCustomer().getState() == Uses.STATE_REDIRECT || user.getCustomer().getState() == Uses.STATE_BACK;
            // кастомер переходит в состояние "Начала обработки"
            user.getCustomer().setState(Uses.STATE_WORK);
            //Запишим в статистику этот момент
            statistic.processingSetWaitCustomers(user.getCustomer().getServiceName(), serviceTree.getByName(user.getCustomer().getServiceName()).getCountCustomers());
            if (!isRedirected) {
                statistic.processingAvgTimeWait(user.getCustomer().getServiceName(),
                        new Double(new Double(user.getCustomer().getStartTime().getTime()
                        - user.getCustomer().getStandTime().getTime()) / 1000 / 60));
            }
            indicatorBoard.workCustomer(user);
            // сохраняем состояния очередей.
            savePool();
            return "<Ответ>\n</Ответ>";
        }
    };
    /**
     * Закончить работу с вызванноым кастомером.
     */
    final Task getFinishCustomerTask = new Task(Uses.TASK_FINISH_CUSTOMER) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            // вот он все это творит
            final QUser user = userList.getByName(cmdParams.userName);
            // вот над этим пациентом
            final ICustomer customer = user.getCustomer();
            // надо посмотреть не требует ли этот кастомер возврата в какую либо очередь.
            final List<Element> list = user.getCustomer().toXML().elements(Uses.TAG_REQUEST_BACK);
            if (!list.isEmpty()) {
                Uses.log.logger.debug("Требуется возврат после редиректа.");
                // получить пункт возврата
                Element first = list.get(0);
                for (Element punkt : list) {
                    if (Integer.parseInt(punkt.attributeValue(Uses.TAG_REQUEST_BACK))
                            > Integer.parseInt(first.attributeValue(Uses.TAG_REQUEST_BACK))) {
                        first = punkt;
                    }
                }
                // удалить запись об этом пункте возврата
                final String serviceName = first.attributeValue(Uses.TAG_SERVICE);

                customer.toXML().remove(first);
                // действия по завершению работы юзера над кастомером
                customer.setFinishTime(new Date());
                // тут еще и в базу скинется, если надо.
                customer.setState(Uses.STATE_FINISH);
                // переставить кастомера в очередь к пункту возврата
                serviceTree.getByName(serviceName).addCustomer(customer);
                // надо кастомера инициализить др. услугой

                // Поставил кастомеру юзера, который его вызвал.
                if (customer instanceof QCustomer) {
                    // юзер в другой очереди наверное другой
                    ((QCustomer) customer).setUser(null);
                    // теперь стоит к новой услуги.
                    ((QCustomer) customer).setService(serviceTree.getByName(serviceName));
                } else {
                    throw new Uses.ServerException("Если это не QCustomer, то кто???. Возможно появилась новая реализация ICustomer.");
                }

                // кастомер переходит в состояние "возврата"
                user.getCustomer().setState(Uses.STATE_BACK);
                //разослать оповещение о том, что появился посетитель после редиректа
                //рассылаем широковещетельно по UDP на определенный порт
                Uses.sendUDPBroadcast(serviceName, netProp.getClientPort());
                Uses.log.logger.info("Клиент \"" + user.getCustomer().getPrefix() + user.getCustomer().getNumber() + "\" возвращен к услуге \"" + serviceName + "\"");
            } else {
                Uses.log.logger.debug("В морг пациента.");

                // в этом случае завершаем с пациентом
                //"все что хирург забыл в вас - ваше"
                // но сначала обозначим результат работы юзера с кастомером, если такой результат найдется в списке результатов
                final QResult result = getResultsList().getByID(cmdParams.resultId);
                ((QCustomer) customer).setResult(result);
                customer.setFinishTime(new Date());
                // кастомер переходит в состояние "Завершенности", но не "мертвости"
                customer.setState(Uses.STATE_FINISH);
            }
            // Кое-что для статистики
            final String serviceName = user.getCustomer().getServiceName();
            final long startTime = user.getCustomer().getStartTime().getTime();

            try {
                user.setCustomer(null);//бобик сдох и медальки не осталось
                //какие-то манипуляции по сохранению статистики
                //Запишим в статистику этот момент
                statistic.processingFinishCustomerOrRedirect(serviceName,
                        cmdParams.userName,
                        new Double(new Double(System.currentTimeMillis()
                        - startTime)
                        / 1000 / 60));
                // сохраняем состояния очередей.
                savePool();
                //разослать оповещение о том, что посетитель откланен
                //рассылаем широковещетельно по UDP на определенный порт. Должно высветитьсяна основном табло
                indicatorBoard.killCustomer(user);
            } finally {
                return "<Ответ>\n</Ответ>";
            }
        }
    };
    /**
     * Переадресовать клиента к другой услуге.
     */
    final Task redirectCustomerTask = new Task(Uses.TASK_REDIRECT_CUSTOMER) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final QUser user = userList.getByName(cmdParams.userName);
            final ICustomer customer = user.getCustomer();
            // Переставка в другую очередь
            // Название новой очереди
            final String newServiceName = cmdParams.serviceName;
            // Название старой очереди
            final String oldServiceName = customer.getServiceName();
            // требует ли возврата в прежнюю очередь
            final boolean requestBack = cmdParams.requestBack;
            // вот она новая очередь.
            final QService newService = serviceTree.getByName(newServiceName);
            // действия по завершению работы юзера над кастомером
            customer.setFinishTime(new Date());
            // тут еще и в базу скинется, если надо.
            customer.setState(Uses.STATE_FINISH);
            // надо кастомера инициализить др. услугой
            if (customer instanceof QCustomer) {
                // юзер в другой очереди наверное другой
                ((QCustomer) customer).setUser(null);
                // теперь стоит к новой услуги.
                ((QCustomer) customer).setService(newService);
            } else {
                throw new Uses.ServerException("Если это не QCustomer, то кто???. Возможно появилась новая реализация ICustomer.");
            }
            // кастомер переходит в состояние "перенаправленности"
            customer.setState(Uses.STATE_REDIRECT);
            // если редиректят в прежнюю услугу, то это по факту не ридирект(иначе карусель)
            // по этому в таком случае кастомера отправляют в конец очереди к этой же услуге.
            // для этого просто не учитываем смену приоритета и галку возврата. 
            if (!oldServiceName.equals(newServiceName)) {
                // т.к. переставленный, то надо поменять ему приоритет.
                customer.setPriority(Uses.PRIORITY_HI);
                // при редиректе надо убрать у кастомера признак старого юзера, время начала обработки.
                //это произойдет далее при вызове setCustomer(null).
                // и добавить, если надо, пункт возврата.
                // теперь пункт возврата
                if (requestBack) {
                    final Element backService = customer.toXML().addElement(Uses.TAG_REQUEST_BACK);
                    // куда нужно вернуться
                    backService.addAttribute(Uses.TAG_SERVICE, oldServiceName);
                    // в каком порядке нужно вернуться
                    backService.addAttribute(Uses.TAG_REQUEST_BACK, String.valueOf(customer.toXML().elements(Uses.TAG_REQUEST_BACK).size() + 1));
                }
            } else {
                // только что встал типо
                customer.setStandTime(new Date());
            }

            //какие-то манипуляции по сохранению статистики
            final long startTime = customer.getStartTime().getTime();

            //С НАЧАЛА ПОДОТРЕМ ПОТОМ ПЕРЕСТАВИМ!!!
            //с новым приоритетом ставим в новую очередь, приоритет должет
            //позволить вызваться ему сразу за обрабатываемыми кастомерами
            newService.addCustomer(customer);
            user.setCustomer(null);//бобик сдох и медальки не осталось, воскрес вместе со старой медалькой в соседней очереди

            try {
                //Запишим в статистику этот момент
                statistic.processingFinishCustomerOrRedirect(oldServiceName, cmdParams.userName,
                        new Double(new Double(System.currentTimeMillis()
                        - startTime)
                        / 1000 / 60));
                statistic.processingSetWaitCustomers(oldServiceName, serviceTree.getByName(oldServiceName).getCountCustomers());
                // сохраняем состояния очередей.
                savePool();
                //разослать оповещение о том, что появился посетитель
                //рассылаем широковещетельно по UDP на определенный порт
                Uses.sendUDPBroadcast(newServiceName, netProp.getClientPort());
                //разослать оповещение о том, что посетитель откланен
                //рассылаем широковещетельно по UDP на определенный порт. Должно подтереться на основном табло
                indicatorBoard.killCustomer(user);
            } finally {
                return "<Ответ>\n</Ответ>";
            }
        }
    };
    /**
     * Привязка услуги пользователю на горячую по команде. Это обработчик этой команды.
     */
    final Task setServiceFire = new Task(Uses.TASK_SET_SERVICE_FIRE) {

        @Override
        synchronized String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            if (cmdParams.userName == null || cmdParams.serviceName == null) {
                return "<Ответ>\nНеверные попараметры запроса.\n</Ответ>";
            }
            if (!serviceTree.hasByName(cmdParams.serviceName)) {
                return "<Ответ>\nТребуемая услуга не присутствует в текущей загруженной конфигурации сервера.\n</Ответ>";
            }
            final QService service = serviceTree.getByName(cmdParams.serviceName);
            if (!userList.hasByName(cmdParams.userName)) {
                return "<Ответ>\nТребуемый пользователь не присутствует в текущей загруженной конфигурации сервера.\n</Ответ>";
            }
            final QUser user = userList.getByName(cmdParams.userName);

            if (user.getServiceList().hasByName(cmdParams.serviceName)) {
                return "<Ответ>\nТребуемая услуга уже назначена этому пользователю.\n</Ответ>";
            }
            user.addPlanService(service, cmdParams.coeff);
            //разослать оповещение о том, что у пользователя поменялась конфигурация услуг
            //рассылаем широковещетельно по UDP на определенный порт
            Uses.sendUDPBroadcast(cmdParams.userName, netProp.getClientPort());
            return "<Ответ>\nУслуга \"" + cmdParams.serviceName + "\" назначена пользователю \"" + cmdParams.userName + "\" успешно.\n</Ответ>";
        }
    };
    /**
     * Удаление привязка услуги пользователю на горячую по команде. Это обработчик этой команды.
     */
    final Task deleteServiceFire = new Task(Uses.TASK_DELETE_SERVICE_FIRE) {

        @Override
        synchronized String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            if (cmdParams.userName == null || cmdParams.serviceName == null) {
                return "<Ответ>\nНеверные попараметры запроса.\n</Ответ>";
            }
            if (!serviceTree.hasByName(cmdParams.serviceName)) {
                return "<Ответ>\nТребуемая услуга не присутствует в текущей загруженной конфигурации сервера.\n</Ответ>";
            }
            if (!userList.hasByName(cmdParams.userName)) {
                return "<Ответ>\nТребуемый пользователь не присутствует в текущей загруженной конфигурации сервера.\n</Ответ>";
            }
            final QUser user = userList.getByName(cmdParams.userName);

            if (!user.getServiceList().hasByName(cmdParams.serviceName)) {
                return "<Ответ>\nТребуемая услуга не назначена этому пользователю.\n</Ответ>";
            }
            user.deletePlanService(cmdParams.serviceName);
            //разослать оповещение о том, что у пользователя поменялась конфигурация услуг
            //рассылаем широковещетельно по UDP на определенный порт
            Uses.sendUDPBroadcast(cmdParams.userName, netProp.getClientPort());
            return "<Ответ>\nУслуга \"" + cmdParams.serviceName + "\" удалена у пользователя \"" + cmdParams.userName + "\" успешно.\n</Ответ>";
        }
    };
    /**
     * Получение конфигурации главного табло - ЖК или плазмы.
     * Это XML-файл лежащий в папку приложения mainboard.xml
     */
    final Task getBoardConfig = new Task(Uses.TASK_GET_BOARD_CONFIG) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            return getIndicatorBoard().getConfig().asXML();
        }
    };
    /**
     * Сохранение конфигурации главного табло - ЖК или плазмы.
     * Это XML-файл лежащий в папку приложения mainboard.xml
     */
    final Task saveBoardConfig = new Task(Uses.TASK_SAVE_BOARD_CONFIG) {

        @Override
        synchronized String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            try {
                getIndicatorBoard().saveConfig(DocumentHelper.parseText(cmdParams.textData).getRootElement());
            } catch (DocumentException ex) {
                Uses.log.logger.error("Не сохранилась конфигурация табло.", ex);
            }
            return "<Ответ></Ответ>";
        }
    };
    /**
     * Получение таблици записанных ранее клиентов на неделю.
     */
    final Task getGridOfWeek = new Task(Uses.TASK_GET_GRID_OF_WEEK) {

        @Override
        String process(final CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            //Определим услугу
            final QService service = serviceTree.getByName(cmdParams.serviceName);
            final QSchedule sch = service.getSchedule();
            if (sch == null) {
                return "<Ответ>\nТребуемая услуга не имеет расписания.\n</Ответ>";
            }

            final Date startWeek = new Date(cmdParams.date);
            final GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(startWeek);
            gc.set(GregorianCalendar.DAY_OF_YEAR, gc.get(GregorianCalendar.DAY_OF_YEAR) + 7);
            final Date endWeek = gc.getTime();

            Uses.log.logger.trace("Загрузим уже занятых позиций ранее записанными кастомерами от " + Uses.format_for_rep.format(startWeek) + " до " + Uses.format_for_rep.format(endWeek));
            // Загрузим уже занятых позиций ранее записанными кастомерами
            List<QAdvanceCustomer> advCustomers = (List<QAdvanceCustomer>) Uses.getSessionFactory().execute(new HibernateCallback() {

                @Override
                public Object doInHibernate(Session session) {
                    //Criteria crit = session.createCriteria(QAdvanceCustomer.class);

                    //crit.add(Restrictions.between("advance_time", startWeek, endWeek));
                    //crit.add(Restrictions.ge("advance_time", startWeek));
                    //crit.add(Restrictions.ge("advance_time", endWeek));
                    //crit = crit.createCriteria("child1").add(Restrictions.ge("advance_time", endWeek));
                    //crit.add(Restrictions.ge("id", new Long(3)));
                    //Criterion user_id = Restrictions.isNull("advance_time");
                    //Criterion name = Restrictions.like("name","P%");
                    //LogicalExpression orExp = Restrictions.or(price,name);
                    //crit.add(orExp);
                    //crit.add(user_id);
                    //crit.add(Restrictions.ilike("description","for%"));
                    return session.createQuery("FROM QAdvanceCustomer a WHERE advance_time >'" + Uses.format_for_rep.format(startWeek) + "' and advance_time <= '" + Uses.format_for_rep.format(endWeek) + "' and service_id = " + service.getId()).list();
                    //return crit.list();
                }
            });
            final Element advCusts = DocumentHelper.createElement(Uses.TAG_CUSTOMER);
            advCusts.addAttribute(Uses.TAG_START_TIME, Uses.format_HH_mm.format(netProp.getStartTime()));
            advCusts.addAttribute(Uses.TAG_FINISH_TIME, Uses.format_HH_mm.format(netProp.getFinishTime()));
            advCusts.addAttribute(Uses.TAG_PROP_ADVANCE_LIMIT, service.getAdvanceLinit().toString());
            advCusts.addAttribute(Uses.TAG_PROP_ADVANCE_PERIOD_LIMIT, service.getAdvanceLimitPeriod() == null ? "0" : service.getAdvanceLimitPeriod().toString());
            // сформируем список доступных времен
            Date day = startWeek;
            while (day.before(endWeek)) {
                final GregorianCalendar gc_day = new GregorianCalendar();
                gc_day.setTime(day);
                // Определим по календарю рабочий ли день.
                // Календаря может быть два, общий с id=1 и персонально настроенный
                // Если день определяется как выходной(присутствует в БД в таблице выходных дней), то переходим к следующему дню
                if (!checkFreeDay(day, new Long(1)) && !(service.getCalendar() != null && checkFreeDay(day, service.getCalendar().getId()))) {
                    // Определим время начала и нонца работы на этот день
                    Date start = null;
                    Date end = null;
                    if (sch.getType() == 1) {
                        if (0 == (gc_day.get(GregorianCalendar.DAY_OF_MONTH) % 2)) {
                            start = sch.getTime_begin_1();
                            end = sch.getTime_end_1();
                        } else {
                            start = sch.getTime_begin_2();
                            end = sch.getTime_end_2();
                        }
                    } else {
                        switch (gc_day.get(GregorianCalendar.DAY_OF_WEEK)) {
                            case 2:
                                start = sch.getTime_begin_1();
                                end = sch.getTime_end_1();
                                break;
                            case 3:
                                start = sch.getTime_begin_2();
                                end = sch.getTime_end_2();
                                break;
                            case 4:
                                start = sch.getTime_begin_3();
                                end = sch.getTime_end_3();
                                break;
                            case 5:
                                start = sch.getTime_begin_4();
                                end = sch.getTime_end_4();
                                break;
                            case 6:
                                start = sch.getTime_begin_5();
                                end = sch.getTime_end_5();
                                break;
                            case 7:
                                start = sch.getTime_begin_6();
                                end = sch.getTime_end_6();
                                break;
                            case 1:
                                start = sch.getTime_begin_7();
                                end = sch.getTime_end_7();
                                break;
                            default:
                                ;
                        }

                    }
                    // Если работаем в этот день то определим часы на которые еще можно записаться
                    if (!(start == null || end == null)) {

                        // бежим по часам внутри дня
                        while (start.before(end)) {
                            int cnt = 0;
                            // пробигаем по кастомерам записанным
                            for (QAdvanceCustomer advCustomer : advCustomers) {
                                gc.setTime(start);
                                final int s = gc.get(GregorianCalendar.HOUR_OF_DAY);
                                gc.setTime(advCustomer.getAdvanceTime());
                                final int e = gc.get(GregorianCalendar.HOUR_OF_DAY);
                                // Если совпел день и час, то увеличим счетчик записавшихся на этот час
                                if (gc.get(GregorianCalendar.DAY_OF_YEAR) == gc_day.get(GregorianCalendar.DAY_OF_YEAR) && s == e) {
                                    cnt++;
                                    // Защита от того чтобы один и тодже клиент не записался предварительно в одну услугу на одну дату.
                                    // данный предв.кастомер не должен быть таким же как и авторизовавшийся на этот час
                                    if (cmdParams.customerId != -1
                                            && advCustomer.getAuthorizationCustomer() != null
                                            && advCustomer.getAuthorizationCustomer().getId() != null
                                            && advCustomer.getAuthorizationCustomer().getId().equals(cmdParams.customerId)) {
                                        cnt = 1999999999;
                                        break;
                                    }
                                }
                            }
                            // если еще количество записавшихся не привысило ограничение по услуге, то добавил этот час как доступный для записи
                            if (cnt < service.getAdvanceLinit()) {
                                final Element tim = DocumentHelper.createElement(Uses.TAG_STAND_TIME);
                                gc.setTime(day);
                                final GregorianCalendar gc2 = new GregorianCalendar();
                                gc2.setTime(start);
                                gc.set(GregorianCalendar.HOUR_OF_DAY, gc2.get(GregorianCalendar.HOUR_OF_DAY));
                                tim.addAttribute(Uses.TAG_START_TIME, Uses.format_for_trans.format(gc.getTime()));
                                advCusts.add(tim);
                            }
                            // перейдем на следующий час
                            gc.setTime(start);
                            gc.set(GregorianCalendar.HOUR_OF_DAY, gc.get(GregorianCalendar.HOUR_OF_DAY) + 1);
                            start = gc.getTime();
                        }


                    }
                } // проверка на нерабочий день календаря
                // переход на следующий день
                gc_day.set(GregorianCalendar.DAY_OF_YEAR, gc_day.get(GregorianCalendar.DAY_OF_YEAR) + 1);
                day = gc_day.getTime();
            }
            return advCusts.asXML();
        }
    };

    /**
     * Проверка даты на нерабочую в определенном календаре
     * @param date проверяемая дата, важен месяц и день
     * @param calcId в каком календаре будем проверять
     * @return Выходной день в этом календаре или нет
     */
    private static boolean checkFreeDay(Date date, Long calcId) {
        final GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        final int m = gc.get(GregorianCalendar.MONTH);
        final int d = gc.get(GregorianCalendar.DAY_OF_MONTH);
        for (FreeDay day : CalendarTableModel.getFreeDays(calcId)) {
            gc.setTime(day.getDate());
            if (m == gc.get(GregorianCalendar.MONTH) && d == gc.get(GregorianCalendar.DAY_OF_MONTH)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Записать кастомера предварительно в услугу.
     */
    final Task standAdvanceInService = new Task(Uses.TASK_ADVANCE_STAND_IN) {

        @Override
        synchronized String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);

            final QService service = serviceTree.getByName(cmdParams.serviceName);
            Uses.log.logger.trace("Предварительно записываем к услуге \"" + cmdParams.serviceName + "\" -> " + service.getPrefix() + ' ' + service.getName() + '\'');
            // Создадим вновь испеченного кастомера
            final QAdvanceCustomer customer = new QAdvanceCustomer();

            // Определим ID авторизованного пользователя, если небыло авторизации, то оно = -1
            final Long authCustonerID = cmdParams.customerId;
            // выкачаем из базы зарегинова
            final Session session = Uses.getSessionFactory().getSessionFactory().openSession();
            customer.setAuthorizationCustomer((QAuthorizationCustomer) session.get(QAuthorizationCustomer.class, authCustonerID));
            // Определим дату и время для кастомера
            final Date startTime = new Date(cmdParams.date);
            //хорошо бы отсекать повторную запись к этому же специалиста на этот же день
            customer.setAdvanceTime(startTime);
            customer.setService(service);
            // время постановки проставляется автоматом при создании кастомера.
            // Приоритет "как все"
            customer.setPriority(2);

            //сохраним нового предварительного пользователя
            Uses.log.logger.debug("Старт сохранения предварительной записи в СУБД.");
            //Uses.getSessionFactory().merge(this);
            session.beginTransaction();
            try {
                session.save(customer);
                session.getTransaction().commit();
                Uses.log.logger.debug("Сохранили.");
            } catch (Exception ex) {
                Uses.log.logger.error("Ошибка при сохранении \n" + ex.toString() + "\n" + ex.getStackTrace());
                session.getTransaction().rollback();
            } finally {
                session.close();
            }
            return customer.getXML().asXML();
        }
    };
    /**
     * Поставить кастомера в очередь предварительно записанного. Проверить бронь, поставить или отказать.
     */
    final Task standAdvanceCheckAndStand = new Task(Uses.TASK_ADVANCE_CHECK_AND_STAND) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);

            // Вытащим из базы предварительного кастомера
            final Session session = Uses.getSessionFactory().getSessionFactory().openSession();
            final QAdvanceCustomer advCust = (QAdvanceCustomer) session.get(QAdvanceCustomer.class, cmdParams.customerId);
            final GregorianCalendar gc = new GregorianCalendar();
            if (advCust != null) {
                gc.setTime(advCust.getAdvanceTime());
                gc.set(GregorianCalendar.HOUR_OF_DAY, gc.get(GregorianCalendar.HOUR_OF_DAY) - 1);
            }
            if (advCust != null && new Date().before(advCust.getAdvanceTime()) && new Date().after(gc.getTime())) {
                // Ставим кастомера
                //трем запись в таблице предварительных записей
                session.beginTransaction();
                try {
                    session.delete(advCust);
                    session.getTransaction().commit();
                    Uses.log.logger.debug("Удалили предварителньную запись о кастомере.");

                } catch (Exception ex) {
                    session.getTransaction().rollback();
                    throw new Uses.ServerException("Ошибка при удалении \n" + ex.toString() + "\n" + ex.getStackTrace());
                } finally {
                    session.close();
                }
                // создаем кастомера вызвав задание по созданию кастомера
                // загрузим задание
                final CmdParams params = new CmdParams();
                params.serviceName = advCust.getService().getName();
                params.password = "";
                params.priority = advCust.getPriority();
                final String txtCustomer = tasks.get(Uses.TASK_STAND_IN).process(params, ipAdress, IP);
                try {
                    final Element taskForCustomer = DocumentHelper.parseText(txtCustomer).getRootElement();
                } catch (Exception e) {
                    throw new Uses.ServerException("Не возможно интерпритировать ответ от для местного использования при постановке ранее раписанного.\n" + e.getMessage());
                }

                return txtCustomer;
            } else {
                String answer;
                if (advCust == null) {
                    Uses.log.logger.trace("Не найдена предварительная запись по введеному коду ID = " + cmdParams.customerId);
                    answer = "Не найдена предварительная запись по введеному коду";
                } else {
                    Uses.log.logger.trace("Предваритело записанный клиент пришел не в свое время");
                    answer = "Предваритело записанный клиент пришел не в свое время";
                }
                // Шлем отказ
                return "<Ответ>\n" + answer + "\n</Ответ>";
            }
        }
    };
    /**
     * Получение списка отзывов.
     */
    final Task getResponseList = new Task(Uses.TASK_GET_RESPONSE_LIST) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            return getResponseList().getXML().asXML();
        }
    };
    /**
     * Регистрация отзыва.
     */
    final Task setResponseAnswer = new Task(Uses.TASK_SET_RESPONSE_ANSWER) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final QRespEvent event = new QRespEvent();
            event.setDate(new Date());
            event.setRespID(cmdParams.responseId);
            Session session = Uses.getSessionFactory().getSessionFactory().openSession();
            session.beginTransaction();
            try {
                session.save(event);
                session.getTransaction().commit();
                Uses.log.logger.debug("Сохранили отзыв в базе.");
            } catch (Exception ex) {
                Uses.log.logger.error("Ошибка при сохранении \n" + ex.toString() + "\n" + ex.getStackTrace());
                session.getTransaction().rollback();
            } finally {
                session.close();
            }
            return "<Ответ></Ответ>";
        }
    };
    /**
     * Получение информационного дерева.
     */
    final Task getInfoTree = new Task(Uses.TASK_GET_INFO_TREE) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            return getInfoTree().getXML().asXML();
        }
    };
    /**
     * Идентифицировать кастомера по его ID.
     */
    final Task getClientAuthorization = new Task(Uses.TASK_GET_CLIENT_AUTHORIZATION) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final Long authCustID = Long.parseLong(cmdParams.clientAuthId);
            // Вытащим из базы предварительного кастомера
            final Session session = Uses.getSessionFactory().getSessionFactory().openSession();
            QAuthorizationCustomer authCust;
            String result;
            try {
                authCust = (QAuthorizationCustomer) session.get(QAuthorizationCustomer.class, authCustID);
                if (authCust != null) {
                    result = authCust.getXML().asXML();
                } else {
                    authCust = (QAuthorizationCustomer) session.get(QAuthorizationCustomer.class, 7700000000000000L + authCustID);
                    if (authCust != null) {
                        result = authCust.getXML().asXML();
                    } else {
                        // Шлем отказ
                        result = "<Ответ>\n" + "<![CDATA[<html><b><p align=center><span style='font-size:40.0pt;color:red'>Номер не обнаружен.</span><br><span style='font-size:60.0pt;color:purple'>Обратитесь в регистратуру.</span>]]>" + "\n</Ответ>";
                    }
                }
            } finally {
                session.clear();
                session.close();
            }
            return result;
        }
    };
    /**
     * Получение списка результатов по окончанию работы пользователя с клиентом.
     */
    final Task getResultsList = new Task(Uses.TASK_GET_RESULTS_LIST) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            return getResultsList().getXML().asXML();
        }
    };
    /**
     * Изменение приоритета кастомеру
     */
    final Task setCustomerPriority = new Task(Uses.TASK_SET_CUSTOMER_PRIORITY) {

        @Override
        String process(final CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            // Вытащим из базы предварительного кастомера
            final String num = cmdParams.clientAuthId.trim();
            final StringBuilder sb = new StringBuilder("");

            getServices().sailToStorm(getServices().getRoot(), new ISailListener() {

                @Override
                public void actionPerformed(QService service) {
                    for (ICustomer customer : service.getCustomers()) {
                        if (num.equals(customer.getPrefix() + customer.getNumber())) {
                            customer.setPriority(cmdParams.priority);
                            service.removeCustomer(customer); // убрать из очереди
                            service.addCustomer(customer);// перепоставили чтобы очередность переинлексиловалась
                            sb.append("<Ответ>Клиенту с номером \"").append(num).append("\" в услуге \"").append(customer.getServiceName()).append("\" изменен приоритет.</Ответ>");
                        }
                    }
                }
            });
            final String s = sb.toString();
            return "".equals(s) ? "<Ответ>Клиент по введенному номеру \"" + num + "\" не найден.</Ответ>" : s;
        }
    };
    /**
     * Получить описание состояния услуги
     */
    final Task getpreinfoForServiceTask = new Task(Uses.TASK_GET_SERVICE_PREINFO) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final String serviceName = cmdParams.serviceName;
            // Проверим оказывается ли сейчас эта услуга
            final QService srv = serviceTree.getByName(serviceName);

            return "<Ответ>\n<html>\n<![CDATA[" + srv.getPreInfoHtml() + "]]>\n</html>\n<print>\n<![CDATA[" + srv.getPreInfoPrintText().replace("\n", "<brk>") + "]]>\n</print>\n</Ответ>";
        }
    };
    /**
     * Получение текста для печати информационного узла по его имени
     */
    final Task getPtintInfoItem = new Task(Uses.TASK_GET_INFO_PRINT) {

        @Override
        String process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final QInfoItem item = infoTree.getByName(cmdParams.infoItemName);
            return "<Ответ><![CDATA[" + item.getTextPrint() + "]]></Ответ>";
        }
    };

//****************************************************************************
//********************* КОНЕЦ добавления в мап обработчиков заданий  *********
//****************************************************************************
    /**
     * Конструктор пула очередей
     * Также нужно оперделить способ вывода информации для клиентов на табло.
     * @param property свойства и настройки по которым строим пул
     * @param ignoreWork создавать или нет статистику и табло.
     */
    private QServicesPool(IServerGetter property, boolean ignoreWork) {
        final long start = System.currentTimeMillis();
        Uses.log.logger.debug("Формирование пула очередей.");
        setNetPropetry(property.getNetProperty() instanceof NetProperty ? (NetProperty) property.getNetProperty() : new NetProperty(property.getNetProperty()));
        serviceTree = QServiceTree.resetServiceTree(property.getPoolGetter());
        infoTree = QInfoTree.resetInfoTree();
        userList = QUserList.resetUserList(property.getUsersGetter());
        responseList = QResponseList.resetResponseList();
        resultsList = QResultList.resetResultList();
        scheduleList = QScheduleList.resetScheduleList();
        calendarList = QCalendarList.resetCalendarList();

        if (ignoreWork) {
            statistic = null;
        } else {
            //пробуем восстановить состояние системы
            loadPool();
            // создаем ведение текущей статистики
            statistic = CurrentStatistic.startCurrentStatistic(property.getUsersGetter(), netProp.getWebServerPort(), property.getReports());
            // запускаем движок индикации сообщения для кастомеров
            indicatorBoard.showBoard();
        }
        if (!(Uses.format_HH_mm.format(netProp.getStartTime()).equals(Uses.format_HH_mm.format(netProp.getFinishTime())))) {
            clearServices.start();
        }
        Uses.log.logger.debug("Пул очередей сформирован. Очередей " + serviceTree.size() + "; пользователей " + userList.getSize() + ". Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
    }
    /**
     * Для Singleton/
     */
    private static QServicesPool instance = null;

    /**
     * Получение объекта. Для Singleton.
     * @param ignoreWork
     * @return QServicesPool
     */
    public static QServicesPool getServicesPool(boolean ignoreWork) {
        if (instance == null) {
            recreateServicesPool(ignoreWork);
        }
        return instance;
    }

    /**
     * Получение объекта принудительно пересоздав его. Для Singleton.
     * @param ignoreWork
     * @return QServicesPool
     */
    public static QServicesPool recreateServicesPool(boolean ignoreWork) {

        if (Uses.spring.factory == null) {
            throw new Uses.ServerException("Не определен контекст Spring.");
        }
        //final DirectorServerProperty director = new DirectorServerProperty();
        final AServerPropertyBuilder propertyBuilder = (AServerPropertyBuilder) Uses.spring.factory.getBean("serverProperty");
        // создаем движок индикации сообщения для кастомеров
        indicatorBoard = (IIndicatorBoard) Uses.spring.factory.getBean("indicatorBoard");
        // Для прерывания звука в роликах при звуковом оповещении.
        if (indicatorBoard instanceof QIndicatorBoardMonitor) {
            SoundPlayer.setStartListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    ((QIndicatorBoardMonitor) indicatorBoard).setMute(true);
                }
            });
            SoundPlayer.setFinishListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    ((QIndicatorBoardMonitor) indicatorBoard).setMute(false);
                }
            });
        }
        //director.setServerPropertyBuilder(propertyBuilder);
        //director.constructServerProperty();
        //final IServerGetter property = director.getServerProperty();
        constructServerProperty(propertyBuilder);
        final IServerGetter property = propertyBuilder.getServerGetter();
        // остановим отчетный вебсервер и потом перестартанем во время создания статистики
        WebServer.stopWebServer();
        instance = new QServicesPool(property, ignoreWork);
        instance.poolSaver = property.getPoolSaver();

        return instance;
    }

    /**
     * Само констрирование свойств билдером.
     */
    private static void constructServerProperty(AServerPropertyBuilder serverPropertyBuilder) {
        final long start = System.currentTimeMillis();
        serverPropertyBuilder.createNewServerGetter();
        serverPropertyBuilder.buildNetProperty();
        serverPropertyBuilder.buildPoolGetter();
        serverPropertyBuilder.buildUsersGetter();
        serverPropertyBuilder.buildPoolSaver();
        serverPropertyBuilder.buildReports();
        Uses.log.logger.debug("Объекты настроек готовы. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
    }
    /**
     * Интерфейс созранения конфигурации.
     */
    private IPoolSaver poolSaver = null;

    /**
     * Этим методом сохраняем всю конфигурацию в файл или БД, без разници.
     * Надо сохранить че наисправлял в пуле - вызывай.
     */
    public void savePoolConfig() {
        poolSaver.save(this);
    }

    protected void addUserToPool(String name, QUser user) {
        userList.addElement(user);
        Uses.log.logger.trace("Пользователя в мап: '" + name + "' -> '" + user.getName() + '\'');
    }

    /**
     * Сохранение состояния пула услуг в xml-файл на диск
     */
    public void savePool() {
        final long start = System.currentTimeMillis();
        final Lock saveLock = new ReentrantLock();
        saveLock.lock();
        try {
            Uses.log.logger.info("Сохранение состояния.");
            final Element backup = DocumentHelper.createElement("BACKUP");// создаем корневой элемент для пула

            serviceTree.sailToStorm(serviceTree.getRoot(), new ISailListener() {

                @Override
                public void actionPerformed(QService service) {
                    service.saveService(backup);
                }
            });

            for (Object o : userList.toArray()) {
                ((QUser) o).saveCastomer(backup);
            }
            // в темповый файл
            final FileOutputStream fos;
            try {
                (new File(Uses.TEMP_FOLDER)).mkdir();
                fos = new FileOutputStream(new File(Uses.TEMP_FOLDER + File.separator + Uses.TEMP_STATE_FILE));
            } catch (FileNotFoundException ex) {
                throw new Uses.ServerException("Не возможно создать временный файл состояния. " + ex.getMessage());
            }
            try {
                fos.write(backup.asXML().getBytes("UTF-8"));
                fos.flush();
                fos.close();
            } catch (IOException ex) {
                throw new Uses.ServerException("Не возможно сохранить изменения в поток." + ex.getMessage());
            }
        } finally {
            saveLock.unlock();
        }
        Uses.log.logger.info("Состояние сохранено. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
    }

    /**
     * Загрузка состояния пула услуг из временного xml-файла
     */
    final protected void loadPool() {
        final long start = System.currentTimeMillis();
        // если есть временный файлик сохранения состояния, то надо его загрузить.
        // все ошибки чтения и парсинга игнорить.
        Uses.log.logger.info("Пробуем восстановить состояние системы.");
        File recovFile = new File(Uses.TEMP_FOLDER + File.separator + Uses.TEMP_STATE_FILE);
        if (recovFile.exists()) {
            Uses.log.logger.warn("Восстановление состояние системы после вчерашнего... нештатного завершения работы сервера.");
            //восстанавливаем состояние
            final SAXReader reader = new SAXReader(false);
            final Element root;
            try {
                root = reader.read(recovFile).getRootElement();
            } catch (DocumentException ex) {
                Uses.log.logger.warn("Невозможно прочитать временный файл. " + ex.getMessage());
                return;
            }
            try {
                for (Object ob : root.elements(Uses.TAG_CUSTOMER)) {
                    final QCustomer customer = new QCustomer((Element) ob);
                    // в эту очередь он был
                    final QService service = serviceTree.getByName(customer.getServiceName());
                    // так зовут юзера его обрабатываюшего
                    final String userName = customer.toXML().attributeValue(Uses.TAG_USER);
                    // кастомер ща стоит к этой услуге к какой стоит
                    customer.setService(service);
                    // смотрим к чему привязан кастомер. либо в очереди стоит, либо у юзера обрабатыватся
                    if (userName == null) {
                        // сохраненный кастомер стоял в очереди и ждал, но его еще никто не звал
                        serviceTree.getByName(customer.getServiceName()).addCustomer(customer);
                        Uses.log.logger.debug("Добавили клиента \"" + customer.getPrefix() + customer.getNumber() + "\" к услуге \"" + customer.getServiceName() + "\"");
                    } else {
                        // сохраненный кастомер обрабатывался юзером с именем userName
                        userList.getByName(userName).setCustomer(customer);
                        customer.setUser(userList.getByName(userName));
                        Uses.log.logger.debug("Добавили клиента \"" + customer.getPrefix() + customer.getNumber() + "\" к юзеру \"" + userName + "\"");
                    }
                }
            } catch (Uses.ServerException ex) {
                System.err.println("Востановление состояния сервера после изменения конфигурации. " + ex);
                clearAllQueue();
                Uses.log.logger.error("Востановление состояния сервера после изменения конфигурации. Для выключения сервера используйте команду exit. ", ex);
            }
        }
        Uses.log.logger.info("Восстановление состояния системы завершено. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
    }
    /**
     * Таймер, по которому будем Очистка всех услуг.
     */
    public ATalkingClock clearServices = new ATalkingClock(Uses.DELAY_CHECK_TO_LOCK, 0) {

        @Override
        public void run() {
            if (Uses.format_HH_mm.format(new Date()).equals(Uses.format_HH_mm.format(netProp.getStartTime()))) {
                Uses.log.logger.info("Очистка всех услуг.");
                // почистим все услуги от трупов кастомеров с прошлого дня
                clearAllQueue();
            }
        }
    };

    private void clearAllQueue() {
        // почистим все услуги от трупов кастомеров
        serviceTree.sailToStorm(serviceTree.getRoot(), new ISailListener() {

            @Override
            public void actionPerformed(QService service) {
                service.clearNextNumber();
                QService.clearNextStNumber();
                service.freeCustomers();
            }
        });
        // Сотрем временные файлы
        Uses.deleteTempFile();
        Uses.log.logger.info("Очистка всех пользователей от привязанных кастомеров.");
        for (Object o : userList.toArray()) {
            QUser user = (QUser) o;
            user.setCustomer(null);
        }
    }

//**********************************************************************************************
//**********************************   ОБРАБОТКА ЗАДАНИЙ  **************************************
//**********************************************************************************************    
    /**
     * Выполнение всех заданий, пришедших на обработку
     * @param rpc объект задания
     * @param ipAdress адрес того кто прислал задание
     * @param IP  адрес того кто прислал задание
     * @return xml-строку результата выполнения задания
     */
    public String doTask(JsonRPC20 rpc, String ipAdress, byte[] IP) {
        final long start = System.currentTimeMillis();
        if (!Uses.isDebug) {
            System.out.println("Task processing: '" + rpc.getMethod());
        }
        Uses.log.logger.info("Обработка задания: '" + rpc.getMethod() + "'");
        if (tasks.get(rpc.getMethod()) == null) {
            throw new Uses.ServerException("В задании не верно указано название действия: '" + rpc.getMethod() + "'");
        }

        final String result;


        // Вызов обработчика задания не синхронизирован
        // Синхронизация переехала внутрь самих обработчиков с помощью блокировок
        // Это сделано потому что появилось много заданий, которые не надо синхронизировать.
        // А то что необходимо синхронизировать, то синхронизится в самих обработчиках.
        result = tasks.get(rpc.getMethod()).process(rpc.getParams(), ipAdress, IP);

        Uses.log.logger.info("Задание завершено. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
        return result;
    }
}
