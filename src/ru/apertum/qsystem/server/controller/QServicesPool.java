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

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
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
import ru.apertum.qsystem.common.model.QCustomer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.dom4j.DocumentHelper;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import ru.apertum.qsystem.common.GsonPool;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.cmd.CmdParams;
import ru.apertum.qsystem.common.cmd.JsonRPC20;
import ru.apertum.qsystem.common.cmd.JsonRPC20Error;
import ru.apertum.qsystem.common.cmd.RpcGetAdvanceCustomer;
import ru.apertum.qsystem.common.cmd.RpcGetAllServices;
import ru.apertum.qsystem.common.cmd.RpcGetAuthorizCustomer;
import ru.apertum.qsystem.common.cmd.RpcGetBool;
import ru.apertum.qsystem.common.cmd.RpcGetGridOfWeek;
import ru.apertum.qsystem.common.cmd.RpcGetGridOfWeek.GridAndParams;
import ru.apertum.qsystem.common.cmd.RpcGetInfoTree;
import ru.apertum.qsystem.common.cmd.RpcGetInt;
import ru.apertum.qsystem.common.cmd.RpcGetPostponedPoolInfo;
import ru.apertum.qsystem.common.cmd.RpcGetRespList;
import ru.apertum.qsystem.common.cmd.RpcGetResultsList;
import ru.apertum.qsystem.common.cmd.RpcGetSelfSituation;
import ru.apertum.qsystem.common.cmd.RpcGetServerState;
import ru.apertum.qsystem.common.cmd.RpcGetSrt;
import ru.apertum.qsystem.common.cmd.RpcGetUsersList;
import ru.apertum.qsystem.common.cmd.RpcInviteCustomer;
import ru.apertum.qsystem.common.cmd.RpcStandInService;
import ru.apertum.qsystem.common.exceptions.ServerException;
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
import ru.apertum.qsystem.server.model.QPlanService;
import ru.apertum.qsystem.server.model.QService;
import ru.apertum.qsystem.server.model.QServiceTree;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.QUserList;
import ru.apertum.qsystem.server.model.calendar.CalendarTableModel;
import ru.apertum.qsystem.server.model.calendar.FreeDay;
import ru.apertum.qsystem.server.model.calendar.QCalendarList;
import ru.apertum.qsystem.server.model.infosystem.QInfoTree;
import ru.apertum.qsystem.server.model.postponed.QPostponedList;
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

        public Object process(CmdParams cmdParams, String ipAdress, byte[] IP) {
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
    final AddCustomerTask addCustomerTask = new AddCustomerTask(Uses.TASK_STAND_IN);

    class AddCustomerTask extends Task {

        public AddCustomerTask(String name) {
            super(name);
        }

        @Override
        public RpcStandInService process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final QService service = serviceTree.getByName(cmdParams.serviceName);
            final QCustomer customer;
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
                service.addCustomer(customer);
            } catch (Exception ex) {
                throw new ServerException("Ошибка при постановке клиента в очередь", ex);
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
                return new RpcStandInService(customer);
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
        synchronized public RpcInviteCustomer process(CmdParams cmdParams, String ipAdress, byte[] IP) {
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

                return new RpcInviteCustomer(user.getCustomer());
            }

            // бежим по очередям юзера и ищем первого из первых кастомера
            QCustomer customer = null;
            int servPriority = -1;// временная переменная для приоритета услуг
            // синхронизация работы с клиентом
            clientTaskLock.lock();
            try {
                for (QPlanService plan : user.getServiceList().getPlanServices()) {
                    final QService serv = serviceTree.getByName(plan.getName()); // очередная очередь

                    final QCustomer cust = serv.peekCustomer(); // первый в этой очереди
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
                    return new RpcInviteCustomer(null);
                }
                customer = serviceTree.getByName(customer.getServiceName()).polCustomer();
            } catch (Exception ex) {
                throw new ServerException("Ошибка при постановке клиента в очередь" + ex);
            } finally {
                clientTaskLock.unlock();
            }
            if (customer == null) {
                throw new ServerException("Странная проблема с получением кастомера и удалением его из очереди.");
            }
            // определим юзеру кастомера, которого он вызвал.
            user.setCustomer(customer);
            // Поставил кастомеру юзера, который его вызвал.
            customer.setUser(user);
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
                return new RpcInviteCustomer(customer);
            }
        }
    };
    /**
     * Пригласить кастомера из пула отложенных
     */
    final Task invitePostponedTask = new Task(Uses.TASK_INVITE_POSTPONED) {

        /**
         * Cинхронизируем, ато вызовут одного и того же.
         * А еще сдесь надо вызвать метод, который "проговорит" кого и куда вазвали.
         * Может случиться ситуация когда двое вызывают последнего кастомера, первому достанется, а второму нет.
         */
        @Override
        synchronized public JsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            // Определить из какой очереди надо выбрать кастомера.
            // Пока без учета коэфициента.
            // Для этого смотрим первых кастомеров во всех очередях и ищем первого среди первых.
            final QUser user = userList.getByName(cmdParams.userName); // юзер

            // выберем отложенного кастомера по ид
            final QCustomer customer = QPostponedList.getInstance().getById(cmdParams.customerId);
            if (customer == null) {
                return new JsonRPC20(new JsonRPC20Error(JsonRPC20Error.POSTPONED_NOT_FOUND, cmdParams.customerId));
            } else {
                QPostponedList.getInstance().removeElement(customer);
            }
            // определим юзеру кастомера, которого он вызвал.
            user.setCustomer(customer);
            // Поставил кастомеру юзера, который его вызвал.
            customer.setUser(user);
            // ставим время вызова
            customer.setCallTime(new Date());
            // кастомер переходит в состояние "приглашенности"
            customer.setState(Uses.STATE_INVITED);
            // ну и услугу определим
            customer.setService(serviceTree.getByName(((QPlanService)user.getServiceList().firstElement()).getName()));

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
                //разослать оповещение о том, что отложенного вызвали, состояние очереди изменилось не изменилось, но пул отложенных изменился
                //рассылаем широковещетельно по UDP на определенный порт
                Uses.sendUDPBroadcast(Uses.TASK_REFRESH_POSTPONED_POOL, netProp.getClientPort());
            } finally {
                return new RpcInviteCustomer(customer);
            }
        }
    };
    /**
     * Получить перечень услуг
     */
    final Task getServicesTask = new Task(Uses.TASK_GET_SERVICES) {

        @Override
        public RpcGetAllServices process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            return new RpcGetAllServices(new RpcGetAllServices.ServicesForWelcome(serviceTree.getRoot(), netProp.getStartTime(), netProp.getFinishTime()));
        }
    };
    /**
     * Получить описание состояния услуги
     */
    final Task aboutTask = new Task(Uses.TASK_ABOUT_SERVICE) {

        @Override
        public RpcGetInt process(CmdParams cmdParams, String ipAdress, byte[] IP) {
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
                return new RpcGetInt(min);
            }
            // бежим по юзерам и смотрим обрабатывают ли они услугу
            // если да, то возьмем все услуги юзера и  сложим всех кастомеров в очередях
            // самую маленькую сумму отправим в ответ по запросу.
            for (Object o : userList.toArray()) {
                final QUser user = (QUser) o;
                if (user.hasService(cmdParams.serviceName)) {
                    // теперь по услугам юзера
                    int sum = 0;
                    for (QPlanService planServ : user.getServiceList().getPlanServices()) {
                        final QService service = serviceTree.getByName(planServ.getName());
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
            return new RpcGetInt(min);
        }
    };
    /**
     * Получить описание пользователей для выбора
     */
    final Task getUsersTask = new Task(Uses.TASK_GET_USERS) {

        @Override
        public RpcGetUsersList process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            checkUserLive.refreshUsersFon();
            return new RpcGetUsersList(userList.getUsers());
        }
    };
    /**
     * Получить состояние сервера.
     */
    private final Task getServerState = new Task(Uses.TASK_SERVER_STATE) {

        @Override
        public RpcGetServerState process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final LinkedList<RpcGetServerState.ServiceInfo> srvs = new LinkedList<RpcGetServerState.ServiceInfo>();

            QServiceTree.sailToStorm(serviceTree.getRoot(), new ISailListener() {

                @Override
                public void actionPerformed(QService service) {
                    if (service.isLeaf()) {
                        final QCustomer customer = service.peekCustomer();
                        srvs.add(new RpcGetServerState.ServiceInfo(service, service.getCountCustomers(), customer != null ? customer.getPrefix() + customer.getNumber() : "Ожидающих нет"));
                    }
                }
            });
            return new RpcGetServerState(srvs);
        }
    };
    /**
     * Получить подтверждение о живучести.
     */
    private final LiveTask checkUserLive = new LiveTask(Uses.TASK_I_AM_LIVE);
    private static final Object forRefr = new Object();

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
         * Адрес пользователя -> его байтовое прeдставление
         */
        private final HashMap<String, byte[]> ipByAddr = new HashMap<String, byte[]>();

        public boolean hasName(String userName) {
            synchronized (forRefr) {
                return addrByName.get(userName) != null;
            }
        }

        /**
         * Опросим всю сетку на предмет пользователей параллельно происходящему.
         */
        public void refreshUsersFon() {
            Thread th = new Thread(new Runnable() {

                @Override
                public void run() {
                    refreshUsers();
                }
            });
            th.start();
        }

        /**
         * Опросим всю сетку на предмет пользователей.
         */
        public void refreshUsers() {
            synchronized (forRefr) {
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
                    throw new ServerException("Таймер. " + ex.toString());
                }
            }
        }

        /**
         * Проверка залогиневшегося чела по имени
         * @param userName имя чела для проверки
         * @return есть юзер с таким именем или нет
         */
        public boolean checkUserName(String userName) {
            synchronized (forRefr) {
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
                        throw new ServerException("Че адрес не существует??? " + new String(ip) + " " + ex);
                    }
                    // подождем ответа
                    int i = 0;
                    while (addrByName.get(userName) == null && i < 70) {
                        i++;
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            throw new ServerException("Таймер. " + ex.toString());
                        }
                    }
                    return addrByName.get(userName) != null;
                } else {
                    return false;
                }
            }
        }

        /**
         * Проверка залогиневшегося чела по адресу
         * @param ipAdress адрес для проверки
         * @return есть там юзер или нет
         */
        public boolean checkUserAddress(String ipAdress) throws UnknownHostException {
            synchronized (forRefr) {
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
                        throw new ServerException("Че адрес не существует??? " + ipAdress + " " + ex);
                    }
                    // подождем ответа
                    int i = 0;
                    while (nameByAddr.get(ipAdress) == null && i < 70) {
                        i++;
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            throw new ServerException("Таймер. " + ex.toString());
                        }
                    }
                    return nameByAddr.get(ipAdress) != null;
                } else {
                    return false;
                }
            }
        }

        @Override
        public JsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            synchronized (forRefr) {
                super.process(cmdParams, ipAdress, IP);
                addrByName.put(cmdParams.userName, ipAdress);
                nameByAddr.put(ipAdress, cmdParams.userName);
                ipByAddr.put(ipAdress, IP);
            }
            return new JsonRPC20();
        }
    };
    /**
     * Получить описание состояния очередей для пользователя.
     */
    final Task getSelfServicesTask = new Task(Uses.TASK_GET_SELF_SERVICES) {

        @Override
        public RpcGetSelfSituation process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final QUser user = userList.getByName(cmdParams.userName);
            final LinkedList<RpcGetSelfSituation.SelfService> servs = new LinkedList<RpcGetSelfSituation.SelfService>();
            for (QPlanService planService : user.getServiceList().getPlanServices()) {
                final QService service = serviceTree.getByName(planService.getName());
                servs.add(new RpcGetSelfSituation.SelfService(service, service.getCountCustomers()));
            }
            // нужно сделать вставочку приглашенного юзера, если он есть
            return new RpcGetSelfSituation(new RpcGetSelfSituation.SelfSituation(servs, user.getCustomer(), QPostponedList.getInstance().getPostponedCustomers()));
        }
    };
    /**
     * Получить описание состояния очередей для пользователя и проверить
     * Отсечем дубляжи запуска от одних и тех же юзеров. но с разных компов
     */
    final Task getCheckSelfTask = new Task(Uses.TASK_GET_SELF_SERVICES_CHECK) {

        @Override
        public synchronized RpcGetBool process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            // Отсечем дубляжи запуска от одних и тех же юзеров. но с разных компов
            // пришло с запросом от юзера имеющегося в региных
            //System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" + userName);
            if (checkUserLive.hasName(cmdParams.userName)) {
                Uses.log.logger.debug(cmdParams.userName + " ACCESS_DENY");
                return new RpcGetBool(false);
            }
            // чтоб вперед не влез если одновременно два новых
            checkUserLive.process(cmdParams, ipAdress, IP);
            return new RpcGetBool(true);
        }
    };
    /**
     * Получить состояние пула отложенных
     */
    final Task getPostponedPoolInfo = new Task(Uses.TASK_GET_POSTPONED_POOL) {

        @Override
        public synchronized RpcGetPostponedPoolInfo process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            return new RpcGetPostponedPoolInfo(QPostponedList.getInstance().getPostponedCustomers());
        }
    };
    /**
     * Удалить вызванного юзером кастомера по неявке.
     */
    final Task killCustomerTask = new Task(Uses.TASK_KILL_NEXT_CUSTOMER) {

        @Override
        public JsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] IP) {
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
                return new JsonRPC20();
            }
        }
    };
    /**
     * Начать работу с вызванноым кастомером.
     */
    final Task getStartCustomerTask = new Task(Uses.TASK_START_CUSTOMER) {

        @Override
        public JsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] IP) {
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
            return new JsonRPC20();
        }
    };
    /**
     * Перемещение вызванного юзером кастомера в пул отложенных.
     */
    final Task customerToPostponeTask = new Task(Uses.TASK_CUSTOMER_TO_POSTPON) {

        @Override
        public JsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            // вот он все это творит
            final QUser user = userList.getByName(cmdParams.userName);
            // вот над этим пациентом
            final QCustomer customer = user.getCustomer();
            // статус
            customer.setPostponedStatus(cmdParams.textData);
            // в этом случае завершаем с пациентом
            //"все что хирург забыл в вас - в пул отложенных"
            // но сначала обозначим результат работы юзера с кастомером, если такой результат найдется в списке результатов
            customer.setFinishTime(new Date());
            // кастомер переходит в состояние "Завершенности", но не "мертвости"
            customer.setState(Uses.STATE_FINISH);
            // Кое-что для статистики
            final String serviceName = user.getCustomer().getServiceName();
            final long startTime = user.getCustomer().getStartTime().getTime();

            try {
                user.setCustomer(null);//бобик сдох но медалька осталось, отправляем в пулл
                //customer.setService(null); - нельзя так делать. кастомер пришел к услуге и пусть в какой-то остается
                customer.setServiceName("");
                customer.setServiceDescription("");
                customer.setUser(null);
                QPostponedList.getInstance().addElement(customer);

                //какие-то манипуляции по сохранению статистики
                //Запишим в статистику этот момент
                statistic.processingFinishCustomerOrRedirect(serviceName,
                        cmdParams.userName,
                        new Double(new Double(System.currentTimeMillis()
                        - startTime)
                        / 1000 / 60));
                // сохраняем состояния очередей.
                savePool();
                //разослать оповещение о том, что посетитель отложен
                Uses.sendUDPBroadcast(Uses.TASK_REFRESH_POSTPONED_POOL, netProp.getClientPort());
                //рассылаем широковещетельно по UDP на определенный порт. Должно высветитьсяна основном табло
                indicatorBoard.killCustomer(user);
            } catch (Throwable t) {
                Uses.log.logger.error("Загнулось под конец.", t);
            } finally {
                return new JsonRPC20();
            }
        }
    };
    /**
     * Изменение отложенному кастомеру статуса
     */
    final Task postponCustomerChangeStatusTask = new Task(Uses.TASK_POSTPON_CHANGE_STATUS) {

        @Override
        public JsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final QCustomer cust = QPostponedList.getInstance().getById(cmdParams.customerId);
            if (cust != null) {
                cust.setPostponedStatus(cmdParams.textData);
                //разослать оповещение о том, что посетителя вызвали, состояние очереди изменилось
                //рассылаем широковещетельно по UDP на определенный порт
                Uses.sendUDPBroadcast(Uses.TASK_REFRESH_POSTPONED_POOL, netProp.getClientPort());
                return new JsonRPC20();
            } else {
                return new JsonRPC20(new JsonRPC20Error(JsonRPC20Error.POSTPONED_NOT_FOUND, cmdParams.customerId));
            }

        }
    };
    /**
     * Закончить работу с вызванноым кастомером.
     */
    final Task getFinishCustomerTask = new Task(Uses.TASK_FINISH_CUSTOMER) {

        @Override
        public JsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            // вот он все это творит
            final QUser user = userList.getByName(cmdParams.userName);
            // вот над этим пациентом
            final QCustomer customer = user.getCustomer();
            // комменты
            customer.setTempComments(cmdParams.textData);
            // надо посмотреть не требует ли этот кастомер возврата в какую либо очередь.
            final QService backSrv = user.getCustomer().getServiceForBack();
            if (backSrv != null) {
                Uses.log.logger.debug("Требуется возврат после редиректа.");
                // действия по завершению работы юзера над кастомером
                customer.setFinishTime(new Date());
                // тут еще и в базу скинется, если надо.
                customer.setState(Uses.STATE_FINISH);
                // переставить кастомера в очередь к пункту возврата
                backSrv.addCustomer(customer);
                // надо кастомера инициализить др. услугой

                // Поставил кастомеру юзера, который его вызвал.
                // юзер в другой очереди наверное другой
                customer.setUser(null);
                // теперь стоит к новой услуги.
                customer.setService(backSrv);

                // кастомер переходит в состояние "возврата"
                user.getCustomer().setState(Uses.STATE_BACK);
                //разослать оповещение о том, что появился посетитель после редиректа
                //рассылаем широковещетельно по UDP на определенный порт
                Uses.sendUDPBroadcast(backSrv.getName(), netProp.getClientPort());
                Uses.log.logger.info("Клиент \"" + user.getCustomer().getPrefix() + user.getCustomer().getNumber() + "\" возвращен к услуге \"" + backSrv.getName() + "\"");
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
                return new JsonRPC20();
            }
        }
    };
    /**
     * Переадресовать клиента к другой услуге.
     */
    final Task redirectCustomerTask = new Task(Uses.TASK_REDIRECT_CUSTOMER) {

        @Override
        public JsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final QUser user = userList.getByName(cmdParams.userName);
            final QCustomer customer = user.getCustomer();
            // комменты по редиректу
            customer.setTempComments(cmdParams.textData);
            // Переставка в другую очередь
            // Название новой очереди
            final String newServiceName = cmdParams.serviceName;
            // Название старой очереди
            final QService oldService = customer.getService();
            // вот она новая очередь.
            final QService newService = serviceTree.getByName(newServiceName);
            // действия по завершению работы юзера над кастомером
            customer.setFinishTime(new Date());
            // тут еще и в базу скинется, если надо.
            customer.setState(Uses.STATE_FINISH);
            // надо кастомера инициализить др. услугой
            // юзер в другой очереди наверное другой
            customer.setUser(null);
            // теперь стоит к новой услуги.
            customer.setService(newService);
            // кастомер переходит в состояние "перенаправленности"
            customer.setState(Uses.STATE_REDIRECT);
            // если редиректят в прежнюю услугу, то это по факту не ридирект(иначе карусель)
            // по этому в таком случае кастомера отправляют в конец очереди к этой же услуге.
            // для этого просто не учитываем смену приоритета и галку возврата. 
            if (!oldService.getName().equals(newServiceName)) {
                // т.к. переставленный, то надо поменять ему приоритет.
                customer.setPriority(Uses.PRIORITY_HI);
                // при редиректе надо убрать у кастомера признак старого юзера, время начала обработки.
                //это произойдет далее при вызове setCustomer(null).
                // и добавить, если надо, пункт возврата.
                // теперь пункт возврата
                if (cmdParams.requestBack) { // требует ли возврата в прежнюю очередь
                    customer.addServiceForBack(oldService);
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
                statistic.processingFinishCustomerOrRedirect(oldService.getName(), cmdParams.userName,
                        new Double(new Double(System.currentTimeMillis()
                        - startTime)
                        / 1000 / 60));
                statistic.processingSetWaitCustomers(oldService.getName(), oldService.getCountCustomers());
                // сохраняем состояния очередей.
                savePool();
                //разослать оповещение о том, что появился посетитель
                //рассылаем широковещетельно по UDP на определенный порт
                Uses.sendUDPBroadcast(newServiceName, netProp.getClientPort());
                //разослать оповещение о том, что посетитель откланен
                //рассылаем широковещетельно по UDP на определенный порт. Должно подтереться на основном табло
                indicatorBoard.killCustomer(user);
            } finally {
                return new JsonRPC20();
            }
        }
    };
    /**
     * Привязка услуги пользователю на горячую по команде. Это обработчик этой команды.
     */
    final Task setServiceFire = new Task(Uses.TASK_SET_SERVICE_FIRE) {

        @Override
        synchronized public RpcGetSrt process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            if (cmdParams.userName == null || cmdParams.serviceName == null) {
                return new RpcGetSrt("Неверные попараметры запроса.");
            }
            if (!serviceTree.hasByName(cmdParams.serviceName)) {
                return new RpcGetSrt("Требуемая услуга не присутствует в текущей загруженной конфигурации сервера.");
            }
            final QService service = serviceTree.getByName(cmdParams.serviceName);
            if (!userList.hasByName(cmdParams.userName)) {
                return new RpcGetSrt("Требуемый пользователь не присутствует в текущей загруженной конфигурации сервера.");
            }
            final QUser user = userList.getByName(cmdParams.userName);

            if (user.getServiceList().hasByName(cmdParams.serviceName)) {
                return new RpcGetSrt("Требуемая услуга уже назначена этому пользователю.");
            }
            user.addPlanService(service, cmdParams.coeff);
            //разослать оповещение о том, что у пользователя поменялась конфигурация услуг
            //рассылаем широковещетельно по UDP на определенный порт
            Uses.sendUDPBroadcast(cmdParams.userName, netProp.getClientPort());
            return new RpcGetSrt("Услуга \"" + cmdParams.serviceName + "\" назначена пользователю \"" + cmdParams.userName + "\" успешно.");
        }
    };
    /**
     * Удаление привязка услуги пользователю на горячую по команде. Это обработчик этой команды.
     */
    final Task deleteServiceFire = new Task(Uses.TASK_DELETE_SERVICE_FIRE) {

        @Override
        synchronized public RpcGetSrt process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            if (cmdParams.userName == null || cmdParams.serviceName == null) {
                return new RpcGetSrt("Неверные попараметры запроса.");
            }
            if (!serviceTree.hasByName(cmdParams.serviceName)) {
                return new RpcGetSrt("Требуемая услуга не присутствует в текущей загруженной конфигурации сервера.");
            }
            if (!userList.hasByName(cmdParams.userName)) {
                return new RpcGetSrt("Требуемый пользователь не присутствует в текущей загруженной конфигурации сервера.");
            }
            final QUser user = userList.getByName(cmdParams.userName);

            if (!user.getServiceList().hasByName(cmdParams.serviceName)) {
                return new RpcGetSrt("Требуемая услуга не назначена этому пользователю.");
            }
            user.deletePlanService(cmdParams.serviceName);
            //разослать оповещение о том, что у пользователя поменялась конфигурация услуг
            //рассылаем широковещетельно по UDP на определенный порт
            Uses.sendUDPBroadcast(cmdParams.userName, netProp.getClientPort());
            return new RpcGetSrt("Услуга \"" + cmdParams.serviceName + "\" удалена у пользователя \"" + cmdParams.userName + "\" успешно.");
        }
    };
    /**
     * Получение конфигурации главного табло - ЖК или плазмы.
     * Это XML-файл лежащий в папку приложения mainboard.xml
     */
    final Task getBoardConfig = new Task(Uses.TASK_GET_BOARD_CONFIG) {

        @Override
        public RpcGetSrt process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            return new RpcGetSrt(getIndicatorBoard().getConfig().asXML());
        }
    };
    /**
     * Сохранение конфигурации главного табло - ЖК или плазмы.
     * Это XML-файл лежащий в папку приложения mainboard.xml
     */
    final Task saveBoardConfig = new Task(Uses.TASK_SAVE_BOARD_CONFIG) {

        @Override
        synchronized public JsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            try {
                getIndicatorBoard().saveConfig(DocumentHelper.parseText(cmdParams.textData).getRootElement());
            } catch (DocumentException ex) {
                Uses.log.logger.error("Не сохранилась конфигурация табло.", ex);
            }
            return new JsonRPC20();
        }
    };
    /**
     * Получение таблици записанных ранее клиентов на неделю.
     */
    final Task getGridOfWeek = new Task(Uses.TASK_GET_GRID_OF_WEEK) {

        @Override
        public RpcGetGridOfWeek process(final CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            //Определим услугу
            final QService service = serviceTree.getByName(cmdParams.serviceName);
            final QSchedule sch = service.getSchedule();
            if (sch == null) {
                return new RpcGetGridOfWeek(new RpcGetGridOfWeek.GridAndParams("Требуемая услуга не имеет расписания."));
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

            final GridAndParams advCusts = new GridAndParams();
            advCusts.setStartTime(netProp.getStartTime());
            advCusts.setFinishTime(netProp.getFinishTime());
            advCusts.setAdvanceLimit(service.getAdvanceLimit());
            advCusts.setAdvanceLimitPeriod(service.getAdvanceLimitPeriod() == null ? 0 : service.getAdvanceLimitPeriod());
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
                            if (cnt < service.getAdvanceLimit()) {
                                gc.setTime(day);
                                final GregorianCalendar gc2 = new GregorianCalendar();
                                gc2.setTime(start);
                                gc.set(GregorianCalendar.HOUR_OF_DAY, gc2.get(GregorianCalendar.HOUR_OF_DAY));
                                gc.set(GregorianCalendar.MINUTE, 0);
                                advCusts.addTime(gc.getTime());
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
            return new RpcGetGridOfWeek(advCusts);
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
        synchronized public RpcGetAdvanceCustomer process(CmdParams cmdParams, String ipAdress, byte[] IP) {
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
            return new RpcGetAdvanceCustomer(customer);
        }
    };
    /**
     * Поставить кастомера в очередь предварительно записанного. Проверить бронь, поставить или отказать.
     */
    final Task standAdvanceCheckAndStand = new Task(Uses.TASK_ADVANCE_CHECK_AND_STAND) {

        @Override
        public RpcStandInService process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);

            // Вытащим из базы предварительного кастомера
            final Session session = Uses.getSessionFactory().getSessionFactory().openSession();
            final QAdvanceCustomer advCust = (QAdvanceCustomer) session.get(QAdvanceCustomer.class, cmdParams.customerId);
            final GregorianCalendar gc = new GregorianCalendar();
            if (advCust != null) {
                gc.setTime(advCust.getAdvanceTime());
                gc.set(GregorianCalendar.HOUR_OF_DAY, gc.get(GregorianCalendar.HOUR_OF_DAY) - 1);
            }
            final GregorianCalendar gc1 = new GregorianCalendar();
            if (advCust != null) {
                gc1.setTime(advCust.getAdvanceTime());
                gc1.set(GregorianCalendar.HOUR_OF_DAY, gc1.get(GregorianCalendar.HOUR_OF_DAY) + 1);
            }
            if (advCust != null && new Date().before(gc1.getTime()) && new Date().after(gc.getTime())) {
                // Ставим кастомера
                //трем запись в таблице предварительных записей
                session.beginTransaction();
                try {
                    session.delete(advCust);
                    session.getTransaction().commit();
                    Uses.log.logger.debug("Удалили предварителньную запись о кастомере.");

                } catch (Exception ex) {
                    session.getTransaction().rollback();
                    throw new ServerException("Ошибка при удалении \n" + ex.toString() + "\n" + ex.getStackTrace());
                } finally {
                    session.close();
                }
                // создаем кастомера вызвав задание по созданию кастомера
                // загрузим задание
                final CmdParams params = new CmdParams();
                params.serviceName = advCust.getService().getName();
                params.password = "";
                params.priority = advCust.getPriority();
                final RpcStandInService txtCustomer = addCustomerTask.process(params, ipAdress, IP);

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
                return new RpcStandInService(null, answer);
            }
        }
    };
    /**
     * Получение списка отзывов.
     */
    final Task getResponseList = new Task(Uses.TASK_GET_RESPONSE_LIST) {

        @Override
        public RpcGetRespList process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            return new RpcGetRespList(getResponseList().getQRespItems());
        }
    };
    /**
     * Регистрация отзыва.
     */
    final Task setResponseAnswer = new Task(Uses.TASK_SET_RESPONSE_ANSWER) {

        @Override
        public JsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final JsonRPC20 rpc = new JsonRPC20();
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
                rpc.setError(new JsonRPC20Error(JsonRPC20Error.RESPONCE_NOT_SAVE, ex));
                Uses.log.logger.error("Ошибка при сохранении \n" + ex.toString() + "\n" + ex.getStackTrace());
                session.getTransaction().rollback();
            } finally {
                session.close();
            }
            return rpc;
        }
    };
    /**
     * Получение информационного дерева.
     */
    final Task getInfoTree = new Task(Uses.TASK_GET_INFO_TREE) {

        @Override
        public RpcGetInfoTree process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            return new RpcGetInfoTree(getInfoTree().getRoot());
        }
    };
    /**
     * Идентифицировать кастомера по его ID.
     */
    final Task getClientAuthorization = new Task(Uses.TASK_GET_CLIENT_AUTHORIZATION) {

        @Override
        public RpcGetAuthorizCustomer process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            final Long authCustID = Long.parseLong(cmdParams.clientAuthId);
            // Вытащим из базы предварительного кастомера
            final Session session = Uses.getSessionFactory().getSessionFactory().openSession();
            QAuthorizationCustomer authCust;
            try {
                authCust = (QAuthorizationCustomer) session.get(QAuthorizationCustomer.class, authCustID);
                if (authCust == null) {
                    authCust = (QAuthorizationCustomer) session.get(QAuthorizationCustomer.class, 7700000000000000L + authCustID);
                }
            } finally {
                session.clear();
                session.close();
            }
            return new RpcGetAuthorizCustomer(authCust);
        }
    };
    /**
     * Получение списка результатов по окончанию работы пользователя с клиентом.
     */
    final Task getResultsList = new Task(Uses.TASK_GET_RESULTS_LIST) {

        @Override
        public RpcGetResultsList process(CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            return new RpcGetResultsList(getResultsList().getItems());
        }
    };
    /**
     * Изменение приоритета кастомеру
     */
    final Task setCustomerPriority = new Task(Uses.TASK_SET_CUSTOMER_PRIORITY) {

        @Override
        public RpcGetSrt process(final CmdParams cmdParams, String ipAdress, byte[] IP) {
            super.process(cmdParams, ipAdress, IP);
            // Вытащим из базы предварительного кастомера
            final String num = cmdParams.clientAuthId.trim();
            final StringBuilder sb = new StringBuilder("");

            QServiceTree.sailToStorm(getServices().getRoot(), new ISailListener() {

                @Override
                public void actionPerformed(QService service) {
                    for (QCustomer customer : service.getCustomers()) {
                        if (num.equals(customer.getPrefix() + customer.getNumber())) {
                            customer.setPriority(cmdParams.priority);
                            service.removeCustomer(customer); // убрать из очереди
                            service.addCustomer(customer);// перепоставили чтобы очередность переинлексиловалась
                            sb.append("Клиенту с номером \"").append(num).append("\" в услуге \"").append(customer.getServiceName()).append("\" изменен приоритет.");
                        }
                    }
                }
            });
            final String s = sb.toString();
            return new RpcGetSrt("".equals(s) ? "Клиент по введенному номеру \"" + num + "\" не найден." : s);
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
            throw new ServerException("Не определен контекст Spring.");
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
            final LinkedList<QCustomer> backup = new LinkedList<QCustomer>();// создаем список сохраняемых кастомеров

            QServiceTree.sailToStorm(serviceTree.getRoot(), new ISailListener() {

                @Override
                public void actionPerformed(QService service) {
                    backup.addAll(service.getClients());
                }
            });

            for (QUser user : userList.getUsers()) {
                if (user.getCustomer() != null) {
                    backup.add(user.getCustomer());
                }
            }
            // в темповый файл
            final FileOutputStream fos;
            try {
                (new File(Uses.TEMP_FOLDER)).mkdir();
                fos = new FileOutputStream(new File(Uses.TEMP_FOLDER + File.separator + Uses.TEMP_STATE_FILE));
            } catch (FileNotFoundException ex) {
                throw new ServerException("Не возможно создать временный файл состояния. " + ex.getMessage());
            }
            Gson gson = null;
            try {
                gson = GsonPool.getInstance().borrowGson();
                fos.write(gson.toJson(new TempList(backup, QPostponedList.getInstance().getPostponedCustomers())).getBytes("UTF-8"));
                fos.flush();
                fos.close();
            } catch (IOException ex) {
                throw new ServerException("Не возможно сохранить изменения в поток." + ex.getMessage());
            } finally {
                GsonPool.getInstance().returnGson(gson);
            }
        } finally {
            saveLock.unlock();
        }
        Uses.log.logger.info("Состояние сохранено. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
    }

    static class TempList {

        public TempList() {
        }

        public TempList(LinkedList<QCustomer> backup, LinkedList<QCustomer> postponed) {
            this.backup = backup;
            this.postponed = postponed;
        }
        @Expose
        @SerializedName("backup")
        LinkedList<QCustomer> backup;
        @Expose
        @SerializedName("postponed")
        LinkedList<QCustomer> postponed;
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


            final FileInputStream fis;
            try {
                fis = new FileInputStream(recovFile);
            } catch (FileNotFoundException ex) {
                throw new ServerException(ex);
            }
            final Scanner scan = new Scanner(fis, "utf8");
            boolean flag = true;
            String rec_data = "";
            while (scan.hasNextLine()) {
                rec_data += scan.nextLine();
            }
            try {
                fis.close();
            } catch (IOException ex) {
                throw new ServerException(ex);
            }



            final TempList recList;
            final Gson gson = GsonPool.getInstance().borrowGson();
            final RpcGetAdvanceCustomer rpc;
            try {
                recList = gson.fromJson(rec_data, TempList.class);
            } catch (JsonParseException ex) {
                throw new ServerException("Не возможно интерпритировать сохраненные данные.\n" + ex.toString());
            } finally {
                GsonPool.getInstance().returnGson(gson);
            }



            try {
                QPostponedList.getInstance().loadPostponedList(recList.postponed);
                for (QCustomer recCustomer : recList.backup) {
                    // в эту очередь он был
                    final QService service = serviceTree.getByName(recCustomer.getServiceName());
                    // так зовут юзера его обрабатываюшего
                    final QUser user = recCustomer.getUser();
                    // кастомер ща стоит к этой услуге к какой стоит
                    recCustomer.setService(service);
                    // смотрим к чему привязан кастомер. либо в очереди стоит, либо у юзера обрабатыватся
                    if (user == null) {
                        // сохраненный кастомер стоял в очереди и ждал, но его еще никто не звал
                        serviceTree.getByName(recCustomer.getServiceName()).addCustomer(recCustomer);
                        Uses.log.logger.debug("Добавили клиента \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" к услуге \"" + recCustomer.getServiceName() + "\"");
                    } else {
                        // сохраненный кастомер обрабатывался юзером с именем userName
                        userList.getByName(user.getName()).setCustomer(recCustomer);
                        recCustomer.setUser(userList.getByName(user.getName()));
                        Uses.log.logger.debug("Добавили клиента \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" к юзеру \"" + user.getName() + "\"");
                    }
                }
            } catch (ServerException ex) {
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
        QServiceTree.sailToStorm(serviceTree.getRoot(), new ISailListener() {

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
     * @return объект результата выполнения задания
     */
    public Object doTask(JsonRPC20 rpc, String ipAdress, byte[] IP) {
        final long start = System.currentTimeMillis();
        if (!Uses.isDebug) {
            System.out.println("Task processing: '" + rpc.getMethod());
        }
        Uses.log.logger.info("Обработка задания: '" + rpc.getMethod() + "'");
        if (tasks.get(rpc.getMethod()) == null) {
            throw new ServerException("В задании не верно указано название действия: '" + rpc.getMethod() + "'");
        }

        final Object result;
        // Вызов обработчика задания не синхронизирован
        // Синхронизация переехала внутрь самих обработчиков с помощью блокировок
        // Это сделано потому что появилось много заданий, которые не надо синхронизировать.
        // А то что необходимо синхронизировать, то синхронизится в самих обработчиках.
        result = tasks.get(rpc.getMethod()).process(rpc.getParams(), ipAdress, IP);

        Uses.log.logger.info("Задание завершено. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
        return result;
    }
}
