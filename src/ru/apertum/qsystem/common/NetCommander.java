/*
 *  Copyright (C) 2010 {Apertum}Projects. web: www.apertum.ru email: info@apertum.ru
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
package ru.apertum.qsystem.common;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import ru.apertum.qsystem.common.cmd.CmdParams;
import ru.apertum.qsystem.common.cmd.AJsonRPC20;
import ru.apertum.qsystem.common.cmd.JsonRPC20;
import ru.apertum.qsystem.common.cmd.JsonRPC20Error;
import ru.apertum.qsystem.common.cmd.JsonRPC20OK;
import ru.apertum.qsystem.common.cmd.RpcBanList;
import ru.apertum.qsystem.common.cmd.RpcGetAdvanceCustomer;
import ru.apertum.qsystem.common.cmd.RpcGetAllServices;
import ru.apertum.qsystem.common.cmd.RpcGetAuthorizCustomer;
import ru.apertum.qsystem.common.cmd.RpcGetBool;
import ru.apertum.qsystem.common.cmd.RpcGetGridOfDay;
import ru.apertum.qsystem.common.cmd.RpcGetGridOfWeek;
import ru.apertum.qsystem.common.cmd.RpcGetInfoTree;
import ru.apertum.qsystem.common.cmd.RpcGetInt;
import ru.apertum.qsystem.common.cmd.RpcGetPostponedPoolInfo;
import ru.apertum.qsystem.common.cmd.RpcGetRespList;
import ru.apertum.qsystem.common.cmd.RpcGetResultsList;
import ru.apertum.qsystem.common.cmd.RpcGetSelfSituation;
import ru.apertum.qsystem.common.cmd.RpcGetServerState;
import ru.apertum.qsystem.common.cmd.RpcGetServerState.ServiceInfo;
import ru.apertum.qsystem.common.cmd.RpcGetServiceState;
import ru.apertum.qsystem.common.cmd.RpcGetServiceState.ServiceState;
import ru.apertum.qsystem.common.cmd.RpcGetSrt;
import ru.apertum.qsystem.common.cmd.RpcGetUsersList;
import ru.apertum.qsystem.common.cmd.RpcGetStandards;
import ru.apertum.qsystem.common.cmd.RpcInviteCustomer;
import ru.apertum.qsystem.common.cmd.RpcStandInService;
import ru.apertum.qsystem.common.exceptions.ClientException;
import ru.apertum.qsystem.common.exceptions.QException;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.apertum.qsystem.common.model.INetProperty;
import ru.apertum.qsystem.common.model.QCustomer;
import ru.apertum.qsystem.server.model.QAdvanceCustomer;
import ru.apertum.qsystem.server.model.QAuthorizationCustomer;
import ru.apertum.qsystem.server.model.QStandards;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.infosystem.QInfoItem;
import ru.apertum.qsystem.server.model.response.QRespItem;
import ru.apertum.qsystem.server.model.results.QResult;

/**
 * Содержит статические методы отправки и получения заданий на сервер. любой метод возвращает XML-узел ответа сервера.
 *
 * @author Evgeniy Egorov
 */
public class NetCommander {

    private static final JsonRPC20 jsonRpc = new JsonRPC20();

    /**
     * основная работа по отсылки и получению результата.
     *
     * @param netProperty параметры соединения с сервером
     * @param commandName
     * @param params
     * @return XML-ответ
     * @throws ru.apertum.qsystem.common.exceptions.QException
     */
    synchronized public static String send(INetProperty netProperty, String commandName, CmdParams params) throws QException {
        jsonRpc.setMethod(commandName);
        jsonRpc.setParams(params);
        return sendRpc(netProperty, jsonRpc);
    }

    synchronized public static String sendRpc(INetProperty netProperty, AJsonRPC20 jsonRpc) throws QException {
        final String message;
        Gson gson = GsonPool.getInstance().borrowGson();
        try {
            message = gson.toJson(jsonRpc);
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        QLog.l().logger().trace("Задание \"" + jsonRpc.getMethod() + "\" на " + netProperty.getAddress().getHostAddress() + ":" + netProperty.getPort() + "#\n" + message);
        final String data;
        try {
            final Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(netProperty.getAddress(), netProperty.getPort()), 15000);
            } catch (IOException ex) {
                throw new QException("Невозможно подключиться к серверу. ", ex);
            }
            QLog.l().logger().trace("Создали Socket.");
            final PrintWriter writer;
            final Scanner in;
            try {
                writer = new PrintWriter(socket.getOutputStream());
                writer.print(URLEncoder.encode(message, "utf-8"));
                QLog.l().logger().trace("Высылаем задание.");
                writer.flush();
                QLog.l().logger().trace("Читаем ответ ...");
                StringBuilder sb = new StringBuilder();
                in = new Scanner(socket.getInputStream());
                while (in.hasNextLine()) {
                    sb = sb.append(in.nextLine()).append("\n");
                }
                data = URLDecoder.decode(sb.toString(), "utf-8");
                writer.close();
                in.close();
            } finally {
                socket.close();
            }
            QLog.l().logger().trace("Ответ:\n" + data);
        } catch (IOException ex) {
            throw new QException("Невозможно получить ответ от сервера. ", ex);
        }
        gson = GsonPool.getInstance().borrowGson();
        try {
            final JsonRPC20Error rpc = gson.fromJson(data, JsonRPC20Error.class);
            if (rpc == null) {
                throw new QException("Ошибка на сервере не позволила сформировать ответ.");
            }
            if (rpc.getError() != null) {
                throw new QException("Выполнение задания произошло с ошибкой. " + rpc.getError().getCode() + ":" + rpc.getError().getMessage());
            }
        } catch (JsonSyntaxException ex) {
            throw new QException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return data;
    }

    /**
     * Получение возможных услуг.
     *
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     */
    public static RpcGetAllServices.ServicesForWelcome getServiсes(INetProperty netProperty) {
        QLog.l().logger().info("Получение возможных услуг.");
        // загрузим ответ
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_GET_SERVICES, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
        if (res == null) {
            return null;
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetAllServices rpc;
        try {
            rpc = gson.fromJson(res, RpcGetAllServices.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Постановка в очередь.
     *
     * @param netProperty netProperty параметры соединения с сервером.
     * @param serviceId услуга, в которую пытаемся встать.
     * @param password пароль того кто пытается выполнить задание.
     * @param priority приоритет.
     * @param inputData
     * @return Созданный кастомер.
     */
    public static QCustomer standInService(INetProperty netProperty, long serviceId, String password, int priority, String inputData) {
        QLog.l().logger().info("Встать в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        params.password = password;
        params.priority = priority;
        params.textData = inputData;
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_STAND_IN, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcStandInService rpc;
        try {
            rpc = gson.fromJson(res, RpcStandInService.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Постановка в очередь.
     *
     * @param netProperty netProperty параметры соединения с сервером.
     * @param servicesId услуги, в которые пытаемся встать. Требует уточнения что это за трехмерный массив. 
     * Это пять списков. Первый это вольнопоследовательные услуги. Остальные четыре это зависимопоследовательные услуги, т.е. пока один не закончится на другой не переходить.
     * Что такое элемент списка. Это тоже список. Первый элемент это та самая комплексная услуга(ID). А остальные это зависимости, т.е. если есть еще не оказанные услуги
     * но назначенные, которые в зависимостях, то их надо оказать.
     * @param password пароль того кто пытается выполнить задание.
     * @param priority приоритет.
     * @param inputData
     * @return Созданный кастомер.
     */
    public static QCustomer standInSetOfServices(INetProperty netProperty, LinkedList<LinkedList<LinkedList<Long>>> servicesId, String password, int priority, String inputData) {
        QLog.l().logger().info("Встать в очередь комплексно.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.complexId = servicesId;
        params.password = password;
        params.priority = priority;
        params.textData = inputData;
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_STAND_COMPLEX, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcStandInService rpc;
        try {
            rpc = gson.fromJson(res, RpcStandInService.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Сделать услугу временно неактивной или разблокировать временную неактивность
     *
     * @param netProperty netProperty параметры соединения с сервером.
     * @param serviceId услуга, которую пытаемся править
     * @param reason
     * @ret
     */
    public static void changeTempAvailableService(INetProperty netProperty, long serviceId, String reason) {
        QLog.l().logger().info("Сделать услугу временно неактивной/активной.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        params.textData = reason;
        try {
            send(netProperty, Uses.TASK_CHANGE_TEMP_AVAILABLE_SERVICE, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
    }

    /**
     * Узнать сколько народу стоит к услуге и т.д.
     *
     * @param netProperty параметры соединения с сервером.
     * @param serviceId id услуги о которой получаем информацию
     * @return количество предшествующих.
     * @throws QException
     */
    public static ServiceState aboutService(INetProperty netProperty, long serviceId) throws QException {
        QLog.l().logger().info("Встать в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_ABOUT_SERVICE, params);
        } catch (QException ex) {// вывод исключений
            throw new QException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetServiceState rpc;
        try {
            rpc = gson.fromJson(res, RpcGetServiceState.class);
        } catch (JsonSyntaxException ex) {
            throw new QException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получить всю очередь к услуге и т.д.
     *
     * @param netProperty параметры соединения с сервером.
     * @param serviceId id услуги о которой получаем информацию
     * @return количество предшествующих.
     * @throws QException
     */
    public static ServiceState getServiceConsistency(INetProperty netProperty, long serviceId) throws QException {
        QLog.l().logger().info("Встать в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_GET_SERVICE_CONSISANCY, params);
        } catch (QException ex) {// вывод исключений
            throw new QException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetServiceState rpc;
        try {
            rpc = gson.fromJson(res, RpcGetServiceState.class);
        } catch (JsonSyntaxException ex) {
            throw new QException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Узнать можно ли вставать в услугу с такими введенными данными
     *
     * @param netProperty параметры соединения с сервером.
     * @param serviceId id услуги о которой получаем информацию
     * @param inputData введенная ботва
     * @return 1 - превышен, 0 - можно встать. 2 - забанен
     * @throws QException
     */
    public static int aboutServicePersonLimitOver(INetProperty netProperty, long serviceId, String inputData) throws QException {
        QLog.l().logger().info("Узнать можно ли вставать в услугу с такими введенными данными.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        params.textData = inputData;
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_ABOUT_SERVICE_PERSON_LIMIT, params);
        } catch (QException ex) {// вывод исключений
            throw new QException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetInt rpc;
        try {
            rpc = gson.fromJson(res, RpcGetInt.class);
        } catch (JsonSyntaxException ex) {
            throw new QException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение описания всех юзеров для выбора себя.
     *
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ все юзеры системы
     */
    public static LinkedList<QUser> getUsers(INetProperty netProperty) {
        QLog.l().logger().info("Получение описания всех юзеров для выбора себя.");
        // загрузим ответ
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_GET_USERS, null);
        } catch (QException e) {// вывод исключений
            Uses.closeSplash();
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } finally {
            if (res == null || res.isEmpty()) {
                System.exit(1);
            }
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetUsersList rpc;
        try {
            rpc = gson.fromJson(res, RpcGetUsersList.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение описания очередей для юзера.
     *
     * @param netProperty параметры соединения с сервером
     * @param userId id пользователя для которого идет опрос
     * @return список обрабатываемых услуг с количеством кастомеров в них стоящих и обрабатываемый кастомер если был
     */
    public static RpcGetSelfSituation.SelfSituation getSelfServices(INetProperty netProperty, long userId) {
        QLog.l().logger().info("Получение описания очередей для юзера.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.textData = pointId;
        String res;
        try {
            res = send(netProperty, Uses.TASK_GET_SELF_SERVICES, params);
        } catch (QException e) {// вывод исключений
            Uses.closeSplash();
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetSelfSituation rpc;
        try {
            rpc = gson.fromJson(res, RpcGetSelfSituation.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Подпорочка, нужна для того чтоб настроить маркеровку окна приема на клиенте и переслать на сервер, чтоб заменить значение из БД. Это значение, если есть,
     * передается в строке параметров при старке клиентской проги и засовывается сюда, вот такая мегаинициализация.
     */
    static public String pointId = null;

    /**
     * Проверка на то что такой юзер уже залогинен в систему
     *
     * @param netProperty параметры соединения с сервером
     * @param userId id пользователя для которого идет опрос
     * @return false - запрешено, true - новый
     */
    public static boolean getSelfServicesCheck(INetProperty netProperty, long userId) {
        QLog.l().logger().info("Получение описания очередей для юзера.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.textData = pointId;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_SELF_SERVICES_CHECK, params);
        } catch (QException e) {// вывод исключений
            Uses.closeSplash();
            throw new ServerException("Невозможно получить ответ от сервера. " + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetBool rpc;
        try {
            rpc = gson.fromJson(res, RpcGetBool.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение слeдующего юзера из очередей, обрабатываемых юзером.
     *
     * @param netProperty параметры соединения с сервером
     * @param userId
     * @return ответ-кастомер следующий по очереди
     */
    public static QCustomer inviteNextCustomer(INetProperty netProperty, long userId) {
        QLog.l().logger().info("Получение следующего юзера из очередей, обрабатываемых юзером.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_INVITE_NEXT_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcInviteCustomer rpc;
        try {
            rpc = gson.fromJson(res, RpcInviteCustomer.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Удаление вызванного юзером кастомера.
     *
     * @param netProperty параметры соединения с сервером
     * @param userId
     */
    public static void killNextCustomer(INetProperty netProperty, long userId) {
        QLog.l().logger().info("Удаление вызванного юзером кастомера.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        try {
            send(netProperty, Uses.TASK_KILL_NEXT_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
    }

    /**
     * Перемещение вызванного юзером кастомера в пул отложенных.
     *
     * @param netProperty параметры соединения с сервером
     * @param userId
     * @param status просто строка. берется из возможных состояний завершения работы
     * @param postponedPeriod
     */
    public static void сustomerToPostpone(INetProperty netProperty, long userId, String status, int postponedPeriod) {
        QLog.l().logger().info("Перемещение вызванного юзером кастомера в пул отложенных.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.textData = status;
        params.postponedPeriod = postponedPeriod;
        try {
            send(netProperty, Uses.TASK_CUSTOMER_TO_POSTPON, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
    }

    /**
     * Изменение отложенному кастомеру статеса
     *
     * @param netProperty параметры соединения с сервером
     * @param postponCustomerId меняем этому кастомеру
     * @param status просто строка. берется из возможных состояний завершения работы
     */
    public static void postponeCustomerChangeStatus(INetProperty netProperty, long postponCustomerId, String status) {
        QLog.l().logger().info("Перемещение вызванного юзером кастомера в пул отложенных.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.customerId = postponCustomerId;
        params.textData = status;
        try {
            send(netProperty, Uses.TASK_POSTPON_CHANGE_STATUS, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
    }

    /**
     * Начать работу с вызванным кастомером.
     *
     * @param netProperty параметры соединения с сервером
     * @param userId
     */
    public static void getStartCustomer(INetProperty netProperty, long userId) {
        QLog.l().logger().info("Начать работу с вызванным кастомером.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        try {
            send(netProperty, Uses.TASK_START_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
    }

    /**
     * Закончить работу с вызванным кастомером.
     *
     * @param netProperty параметры соединения с сервером
     * @param userId
     * @param resultId
     * @param comments это если закончили работать с редиректенным и его нужно вернуть
     * @return
     */
    public static QCustomer getFinishCustomer(INetProperty netProperty, long userId, Long resultId, String comments) {
        QLog.l().logger().info("Закончить работу с вызванным кастомером.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.resultId = resultId;
        params.textData = comments;
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_FINISH_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcStandInService rpc;
        try {
            rpc = gson.fromJson(res, RpcStandInService.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Переадресовать клиента в другую очередь.
     *
     * @param netProperty параметры соединения с сервером
     * @param userId
     * @param serviceId
     * @param requestBack
     * @param resultId
     * @param comments комментарии при редиректе
     */
    public static void redirectCustomer(INetProperty netProperty, long userId, long serviceId, boolean requestBack, String comments, Long resultId) {
        QLog.l().logger().info("Переадресовать клиента в другую очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.serviceId = serviceId;
        params.requestBack = requestBack;
        params.resultId = resultId;
        params.textData = comments;
        try {
            send(netProperty, Uses.TASK_REDIRECT_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
    }

    /**
     * Подтверждение живости клиентом для сервера.
     *
     * @param netProperty параметры соединения с сервером
     * @param userId
     * @return XML-ответ
     */
    public static Element setLive(INetProperty netProperty, long userId) {
        QLog.l().logger().info("Ответим что живы и здоровы.");
        final CmdParams params = new CmdParams();
        params.userId = userId;
        /*
         try {
         // загрузим ответ
         return send(netProperty, Uses.TASK_I_AM_LIVE, params);
         } catch (IOException e) {// вывод исключений
         throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
         } catch (DocumentException e) {
         throw new ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
         }
         *
         */

        return null;
    }

    /**
     * Получение описания состояния сервера.
     *
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     */
    public static LinkedList<ServiceInfo> getServerState(INetProperty netProperty) {
        QLog.l().logger().info("Получение описания состояния сервера.");
        // загрузим ответ
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_SERVER_STATE, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetServerState rpc;
        try {
            rpc = gson.fromJson(res, RpcGetServerState.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение описания состояния пункта регистрации.
     *
     * @param netProperty параметры соединения с пунктом регистрации
     * @param message что-то вроде названия команды для пункта регистрации
     * @return некий ответ от пункта регистрации, вроде прям как строка для вывода
     */
    public static String getWelcomeState(INetProperty netProperty, String message) {
        QLog.l().logger().info("Получение описания состояния пункта регистрации.");
        // загрузим ответ
        String res = null;
        try {
            res = send(netProperty, message, null);
        } catch (QException e) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetSrt rpc;
        try {
            rpc = gson.fromJson(res, RpcGetSrt.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Добавить сервис в список обслуживаемых юзером использую параметры. Используется при добавлении на горячую.
     *
     * @param netProperty параметры соединения с пунктом регистрации
     * @param serviceId
     * @param userId
     * @param coeff
     * @return содержить строковое сообщение о результате.
     */
    public static String setServiseFire(INetProperty netProperty, long serviceId, long userId, int coeff) {
        QLog.l().logger().info("Привязка услуги пользователю на горячую.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.serviceId = serviceId;
        params.coeff = coeff;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_SET_SERVICE_FIRE, params);
        } catch (QException e) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetSrt rpc;
        try {
            rpc = gson.fromJson(res, RpcGetSrt.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Удалить сервис из списока обслуживаемых юзером использую параметры. Используется при добавлении на горячую.
     *
     * @param netProperty параметры соединения с пунктом регистрации
     * @param serviceId
     * @param userId
     * @return содержить строковое сообщение о результате.
     */
    public static String deleteServiseFire(INetProperty netProperty, long serviceId, long userId) {
        QLog.l().logger().info("Удаление услуги пользователю на горячую.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.serviceId = serviceId;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_DELETE_SERVICE_FIRE, params);
        } catch (QException e) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetSrt rpc;
        try {
            rpc = gson.fromJson(res, RpcGetSrt.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение конфигурации главного табло - ЖК или плазмы. Это XML-файл лежащий в папку приложения mainboard.xml
     *
     * @param netProperty параметры соединения с сервером
     * @return корень XML-файла mainboard.xml
     * @throws DocumentException принятый текст может не преобразоваться в XML
     */
    public static Element getBoardConfig(INetProperty netProperty) throws DocumentException {
        QLog.l().logger().info("Получение конфигурации главного табло - ЖК или плазмы.");
        // загрузим ответ
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_BOARD_CONFIG, null);
        } catch (QException e) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetSrt rpc;
        try {
            rpc = gson.fromJson(res, RpcGetSrt.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return DocumentHelper.parseText(rpc.getResult()).getRootElement();
    }

    /**
     * Сохранение конфигурации главного табло - ЖК или плазмы. Это XML-файл лежащий в папку приложения mainboard.xml
     *
     * @param netProperty параметры соединения с сервером
     * @param boardConfig
     */
    public static void saveBoardConfig(INetProperty netProperty, Element boardConfig) {
        QLog.l().logger().info("Сохранение конфигурации главного табло - ЖК или плазмы.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.textData = boardConfig.asXML();
        try {
            send(netProperty, Uses.TASK_SAVE_BOARD_CONFIG, params);
        } catch (QException e) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Получение дневной таблици с данными для предварительной записи включающими информацию по занятым временам и свободным.
     *
     * @param netProperty netProperty параметры соединения с сервером.
     * @param serviceId услуга, в которую пытаемся встать.
     * @param date день недели за который нужны данные.
     * @param advancedCustomer ID авторизованного кастомера
     * @return класс с параметрами и списком времен
     */
    public static RpcGetGridOfDay.GridDayAndParams getPreGridOfDay(INetProperty netProperty, long serviceId, Date date, long advancedCustomer) {
        QLog.l().logger().info("Получить таблицу дня");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        params.date = date.getTime();
        params.customerId = advancedCustomer;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_GRID_OF_DAY, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetGridOfDay rpc;
        try {
            rpc = gson.fromJson(res, RpcGetGridOfDay.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение недельной таблици с данными для предварительной записи.
     *
     * @param netProperty netProperty параметры соединения с сервером.
     * @param serviceId услуга, в которую пытаемся встать.
     * @param date первый день недели за которую нужны данные.
     * @param advancedCustomer ID авторизованного кастомера
     * @return класс с параметрами и списком времен
     */
    public static RpcGetGridOfWeek.GridAndParams getGridOfWeek(INetProperty netProperty, long serviceId, Date date, long advancedCustomer) {
        QLog.l().logger().info("Получить таблицу");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        params.date = date.getTime();
        params.customerId = advancedCustomer;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_GRID_OF_WEEK, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetGridOfWeek rpc;
        try {
            rpc = gson.fromJson(res, RpcGetGridOfWeek.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Предварительная запись в очередь.
     *
     * @param netProperty netProperty параметры соединения с сервером.
     * @param serviceId услуга, в которую пытаемся встать.
     * @param date
     * @param advancedCustomer ID авторизованного кастомер. -1 если нет авторизации
     * @param inputData введеные по требованию услуги данные клиентом, может быть null если не вводили
     * @param comments комментарий по предварительно ставящемуся клиенту если ставят из админки или приемной
     * @return предварительный кастомер
     */
    public static QAdvanceCustomer standInServiceAdvance(INetProperty netProperty, long serviceId, Date date, long advancedCustomer, String inputData, String comments) {
        QLog.l().logger().info("Записать предварительно в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        params.date = date.getTime();
        params.customerId = advancedCustomer;
        params.textData = inputData;
        params.comments = comments;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_ADVANCE_STAND_IN, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetAdvanceCustomer rpc;
        try {
            rpc = gson.fromJson(res, RpcGetAdvanceCustomer.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Предварительная запись в очередь.
     *
     * @param netProperty netProperty параметры соединения с сервером.
     * @param advanceID идентификатор предварительно записанного.
     * @return XML-ответ.
     */
    public static RpcStandInService standAndCheckAdvance(INetProperty netProperty, Long advanceID) {
        QLog.l().logger().info("Постановка предварительно записанных в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.customerId = advanceID;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_ADVANCE_CHECK_AND_STAND, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcStandInService rpc;
        try {
            rpc = gson.fromJson(res, RpcStandInService.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc;
    }
    
    /**
     * Удаление предварительной записи в очередь.
     *
     * @param netProperty netProperty параметры соединения с сервером.
     * @param advanceID идентификатор предварительно записанного.
     * @return XML-ответ.
     */
    public static JsonRPC20OK removeAdvancedCustomer(INetProperty netProperty, Long advanceID) {
        QLog.l().logger().info("Удаление предварительно записанных в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.customerId = advanceID;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_REMOVE_ADVANCE_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final JsonRPC20OK rpc;
        try {
            rpc = gson.fromJson(res, JsonRPC20OK.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc;
    }

    /**
     * Рестарт сервера.
     *
     * @param netProperty параметры соединения с сервером
     */
    public static void restartServer(INetProperty netProperty) {
        QLog.l().logger().info("Команда на рестарт сервера.");
        try {
            send(netProperty, Uses.TASK_RESTART, null);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
    }

    /**
     * Получение списка отзывов
     *
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     */
    public static LinkedList<QRespItem> getResporseList(INetProperty netProperty) {
        QLog.l().logger().info("Команда на получение списка отзывов.");
        String res = null;
        try {
            // загрузим ответ
            res = send(netProperty, Uses.TASK_GET_RESPONSE_LIST, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
        if (res == null) {
            return null;
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetRespList rpc;
        try {
            rpc = gson.fromJson(res, RpcGetRespList.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Оставить отзыв.
     *
     * @param netProperty параметры соединения с сервером.
     * @param serviceID услуга, может быть null
     * @param userID оператор, может быть null
     * @param customerID
     * @param clientData номер талона, не null
     * @param respID идентификатор выбранного отзыва
     */
    public static void setResponseAnswer(INetProperty netProperty, Long respID, Long userID, Long serviceID, Long customerID, String clientData) {
        QLog.l().logger().info("Отправка выбранного отзыва.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.responseId = respID;
        params.serviceId = serviceID;
        params.userId = userID;
        params.customerId = customerID;
        params.textData = clientData;
        try {
            send(netProperty, Uses.TASK_SET_RESPONSE_ANSWER, params);
        } catch (QException ex) {// вывод исключений
            throw new ServerException("Проблема с командой. ", ex);
        }
    }

    /**
     * Получение информационного дерева
     *
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     */
    public static QInfoItem getInfoTree(INetProperty netProperty) {
        QLog.l().logger().info("Команда на получение информационного дерева.");
        String res = null;
        try {
            // загрузим ответ
            res = send(netProperty, Uses.TASK_GET_INFO_TREE, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
        if (res == null) {
            return null;
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetInfoTree rpc;
        try {
            rpc = gson.fromJson(res, RpcGetInfoTree.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение описания залогинившегося юзера.
     *
     * @param netProperty параметры соединения с сервером
     * @param id
     * @return XML-ответ
     */
    public static QAuthorizationCustomer getClientAuthorization(INetProperty netProperty, String id) {
        QLog.l().logger().info("Получение описания авторизованного пользователя.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.clientAuthId = id;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_CLIENT_AUTHORIZATION, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetAuthorizCustomer rpc;
        try {
            rpc = gson.fromJson(res, RpcGetAuthorizCustomer.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение списка возможных результатов работы с клиентом
     *
     * @param netProperty параметры соединения с сервером
     * @return свисок возможных завершений работы
     */
    public static LinkedList<QResult> getResultsList(INetProperty netProperty) {
        QLog.l().logger().info("Команда на получение списка возможных результатов работы с клиентом.");
        final String res;
        try {
            // загрузим ответ RpcGetResultsList
            res = send(netProperty, Uses.TASK_GET_RESULTS_LIST, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetResultsList rpc;
        try {
            rpc = gson.fromJson(res, RpcGetResultsList.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Изменение приоритета кастомеру
     *
     * @param netProperty параметры соединения с сервером
     * @param prioritet
     * @param customer
     * @return Текстовый ответ о результате
     */
    public static String setCustomerPriority(INetProperty netProperty, int prioritet, String customer) {
        QLog.l().logger().info("Команда на повышение приоритета кастомеру.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.priority = prioritet;
        params.clientAuthId = customer;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_SET_CUSTOMER_PRIORITY, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetSrt rpc;
        try {
            rpc = gson.fromJson(res, RpcGetSrt.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Пробить номер клиента. Стоит в очереди или отложен или вообще не найден.
     *
     * @param netProperty параметры соединения с сервером
     * @param customerNumber
     * @return Текстовый ответ о результате
     */
    public static String checkCustomerNumber(INetProperty netProperty, String customerNumber) {
        QLog.l().logger().info("Команда проверки номера кастомера.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.clientAuthId = customerNumber;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_CHECK_CUSTOMER_NUMBER, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetSrt rpc;
        try {
            rpc = gson.fromJson(res, RpcGetSrt.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получить список отложенных кастомеров
     *
     * @param netProperty
     * @return список отложенных кастомеров
     */
    public static LinkedList<QCustomer> getPostponedPoolInfo(INetProperty netProperty) {
        QLog.l().logger().info("Команда на обновление пула отложенных.");
        // загрузим ответ
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_POSTPONED_POOL, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetPostponedPoolInfo rpc;
        try {
            rpc = gson.fromJson(res, RpcGetPostponedPoolInfo.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получить список забаненных введенных данных
     *
     * @param netProperty
     * @return список отложенных кастомеров
     */
    public static LinkedList<String> getBanedList(INetProperty netProperty) {
        QLog.l().logger().info("Команда получение списка забаненных.");
        // загрузим ответ
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_BAN_LIST, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcBanList rpc;
        try {
            rpc = gson.fromJson(res, RpcBanList.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getBanList();
    }

    /**
     * Вызов отложенного кастомера
     *
     * @param netProperty
     * @param userId id юзера который вызывает
     * @param id это ID кастомера которого вызываем из пула отложенных, оно есть т.к. с качстомером давно работаем
     */
    public static void invitePostponeCustomer(INetProperty netProperty, long userId, Long id) {
        QLog.l().logger().info("Команда на вызов кастомера из пула отложенных.");
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.customerId = id;
        // загрузим ответ
        try {
            send(netProperty, Uses.TASK_INVITE_POSTPONED, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
    }

    /**
     * Рестарт главного табло
     *
     * @param serverNetProperty
     */
    public static void restartMainTablo(INetProperty serverNetProperty) {
        QLog.l().logger().info("Команда на рестарт главного табло.");
        // загрузим ответ
        try {
            send(serverNetProperty, Uses.TASK_RESTART_MAIN_TABLO, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
    }

    /**
     * Изменение приоритетов услуг оператором
     *
     * @param netProperty
     * @param userId id юзера который вызывает
     * @param smartData
     */
    public static void changeFlexPriority(INetProperty netProperty, long userId, String smartData) {
        QLog.l().logger().info("Изменение приоритетов услуг оператором.");
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.textData = smartData;
        // загрузим ответ
        try {
            send(netProperty, Uses.TASK_CHANGE_FLEX_PRIORITY, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
    }

    /**
     * Изменение бегущей строки на табло из админской проги
     *
     * @param netProperty параметры соединения с сервером
     * @param text новая строка
     * @param nameSection
     */
    public static void setRunningText(INetProperty netProperty, String text, String nameSection) {
        QLog.l().logger().info("Получение описания авторизованного пользователя.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.textData = text;
        params.infoItemName = nameSection;
        try {
            send(netProperty, Uses.TASK_CHANGE_RUNNING_TEXT_ON_BOARD, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
    }

    /**
     * Получить норрмативы
     *
     * @param netProperty
     * @return класс нормативов
     */
    public static QStandards getStandards(INetProperty netProperty) {
        QLog.l().logger().info("Команда получение нормативов.");
        // загрузим ответ
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_STANDARDS, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetStandards rpc;
        try {
            rpc = gson.fromJson(res, RpcGetStandards.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return (QStandards) rpc.getResult();
    }
    
    /**
     * Изменение приоритетов услуг оператором
     *
     * @param netProperty
     * @param userId id юзера который вызывает
     * @param lock
     * @return 
     */
    public static boolean setBussy(INetProperty netProperty, long userId, boolean lock) {
        QLog.l().logger().info("Изменение приоритетов услуг оператором.");
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.requestBack = lock;
        // загрузим ответ
        final String res;
        try {
            res = send(netProperty, Uses.TASK_SET_BUSSY, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetBool rpc;
        try {
            rpc = gson.fromJson(res, RpcGetBool.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }
}
