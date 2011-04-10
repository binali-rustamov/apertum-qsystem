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
package ru.apertum.qsystem.common.model;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import ru.apertum.qsystem.common.GsonPool;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.cmd.CmdParams;
import ru.apertum.qsystem.common.cmd.JsonRPC20;
import ru.apertum.qsystem.common.cmd.RpcGetAdvanceCustomer;
import ru.apertum.qsystem.common.cmd.RpcGetAllServices;
import ru.apertum.qsystem.common.cmd.RpcGetAuthorizCustomer;
import ru.apertum.qsystem.common.cmd.RpcGetBool;
import ru.apertum.qsystem.common.cmd.RpcGetGridOfWeek;
import ru.apertum.qsystem.common.cmd.RpcGetInfoTree;
import ru.apertum.qsystem.common.cmd.RpcGetInt;
import ru.apertum.qsystem.common.cmd.RpcGetPostponedPoolInfo;
import ru.apertum.qsystem.common.cmd.RpcGetRespList;
import ru.apertum.qsystem.common.cmd.RpcGetResultsList;
import ru.apertum.qsystem.common.cmd.RpcGetSelfSituation;
import ru.apertum.qsystem.common.cmd.RpcGetServerState;
import ru.apertum.qsystem.common.cmd.RpcGetServerState.ServiceInfo;
import ru.apertum.qsystem.common.cmd.RpcGetSrt;
import ru.apertum.qsystem.common.cmd.RpcGetUsersList;
import ru.apertum.qsystem.common.cmd.RpcInviteCustomer;
import ru.apertum.qsystem.common.cmd.RpcStandInService;
import ru.apertum.qsystem.common.exceptions.ClientException;
import ru.apertum.qsystem.common.exceptions.QException;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.apertum.qsystem.server.model.QAdvanceCustomer;
import ru.apertum.qsystem.server.model.QAuthorizationCustomer;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.infosystem.QInfoItem;
import ru.apertum.qsystem.server.model.response.QRespItem;
import ru.apertum.qsystem.server.model.results.QResult;

/**
 * Содержит статические методы отправки и получения заданий на сервер.
 * любой метод возвращает XML-узел ответа сервера.
 * @author Evgeniy Egorov
 */
public class NetCommander {

    private static final JsonRPC20 jsonRpc = new JsonRPC20();

    /**
     *  основная работа по отсылки и получению результата.
     * @param netProperty параметры соединения с сервером
     * @param message отсылаемое сообщение.
     * @return XML-ответ
     */
    synchronized private static String send(INetProperty netProperty, String commandName, CmdParams params) throws QException {
        jsonRpc.setMethod(commandName);
        jsonRpc.setParams(params);

        final String message;
        Gson gson = GsonPool.getInstance().borrowGson();
        try {
            message = gson.toJson(jsonRpc);
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        Uses.log.logger.trace("Задание \"" + commandName + "\" на " + netProperty.getServerAddress().getHostAddress() + ":" + netProperty.getServerPort() + "#\n" + message);
        final String data;
        try {
            // открываем сокет и коннектимся к localhost:3128
            // получаем сокет сервера
            final Socket socket = new Socket(netProperty.getServerAddress(), netProperty.getServerPort());
            Uses.log.logger.trace("Создали Socket.");
            // Передача данных запроса
            final PrintWriter writer = new PrintWriter(socket.getOutputStream());
            writer.print(URLEncoder.encode(message, "utf-8"));
            Uses.log.logger.trace("Высылаем задание.");
            writer.flush();
            // Чтение ответа.
            Uses.log.logger.trace("Читаем ответ ...");
            StringBuilder sb = new StringBuilder();
            final Scanner in = new Scanner(socket.getInputStream());
            while (in.hasNextLine()) {
                sb = sb.append(in.nextLine()).append("\n");
            }
            data = URLDecoder.decode(sb.toString(), "utf-8");
            socket.close();
            writer.close();
            in.close();
            Uses.log.logger.trace("Ответ:\n" + data);
        } catch (IOException ex) {
            throw new QException("Невозможно получить ответ от сервера. ", ex);
        }
        gson = GsonPool.getInstance().borrowGson();
        try {
            JsonRPC20 rpc = gson.fromJson(data, JsonRPC20.class);
            if (rpc == null) {
                throw new QException("Ошибка на сервере не позволила сформировать ответ.");
            }
            if (rpc.getError() != null) {
                throw new QException("Выполнение задания произошло с ошибкой. " + rpc.getError().getCode() + ":" + rpc.getError().getMessage());
            }
        } catch (JsonParseException ex) {
            throw new QException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return data;
    }

    /**
     * Получение возможных услуг.
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     */
    public static RpcGetAllServices.ServicesForWelcome getServiсes(INetProperty netProperty) {
        Uses.log.logger.info("Получение возможных услуг.");
        // загрузим ответ
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_GET_SERVICES, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        } finally {
            if (res == null) {
                return null;
            }
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetAllServices rpc;
        try {
            rpc = gson.fromJson(res, RpcGetAllServices.class);
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Постановка в очередь.
     * @param netProperty netProperty параметры соединения с сервером.
     * @param service услуга, в которую пытаемся встать.
     * @param password пароль того кто пытается выполнить задание.
     * @param priority приоритет.
     * @param inputData
     * @return Созданный кастомер.
     */
    public static QCustomer standInService(INetProperty netProperty, String service, String password, int priority, String inputData) {
        Uses.log.logger.info("Встать в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceName = service;
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Узнать сколько народу стоит к услуге и т.д.
     * @param netProperty параметры соединения с сервером.
     * @param serviceName название услуги о которой получаем информацию
     * @return количество предшествующих.
     * @throws QException
     */
    public static int aboutService(INetProperty netProperty, String serviceName) throws QException {
        Uses.log.logger.info("Встать в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceName = serviceName;
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_ABOUT_SERVICE, params);
        } catch (QException ex) {// вывод исключений
            throw new QException("Проблема с командой. ", ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetInt rpc;
        try {
            rpc = gson.fromJson(res, RpcGetInt.class);
        } catch (JsonParseException ex) {
            throw new QException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение описания всех юзеров для выбора себя.
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ все юзеры системы
     */
    public static LinkedList<QUser> getUsers(INetProperty netProperty) {
        Uses.log.logger.info("Получение описания всех юзеров для выбора себя.");
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение описания очередей для юзера.
     * @param netProperty параметры соединения с сервером
     * @param userName имя пользователя для которого идет опрос
     * @return список обрабатываемых услуг с количеством кастомеров в них стоящих и обрабатываемый кастомер если был
     */
    public static RpcGetSelfSituation.SelfSituation getSelfServices(INetProperty netProperty, String userName) {
        Uses.log.logger.info("Получение описания очередей для юзера.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Проверка на то что такой юзер уже залогинен в систему
     * @param netProperty параметры соединения с сервером
     * @param userName имя пользователя для которого идет опрос
     * @return false - запрешено, true - новый
     */
    public static boolean getSelfServicesCheck(INetProperty netProperty, String userName) {
        Uses.log.logger.info("Получение описания очередей для юзера.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение слкдующего юзера из очередей, обрабатываемых юзером.
     * @param netProperty параметры соединения с сервером
     * @param userName
     * @return ответ-кастомер следующий по очереди
     */
    public static QCustomer inviteNextCustomer(INetProperty netProperty, String userName) {
        Uses.log.logger.info("Получение следующего юзера из очередей, обрабатываемых юзером.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Удаление вызванного юзером кастомера.
     * @param netProperty параметры соединения с сервером
     * @param userName
     */
    public static void killNextCustomer(INetProperty netProperty, String userName) {
        Uses.log.logger.info("Удаление вызванного юзером кастомера.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        try {
            send(netProperty, Uses.TASK_KILL_NEXT_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
    }

    /**
     * Перемещение вызванного юзером кастомера в пул отложенных.
     * @param netProperty параметры соединения с сервером
     * @param userName
     * @param status просто строка. берется из возможных состояний завершения работы
     */
    public static void сustomerToPostpone(INetProperty netProperty, String userName, String status) {
        Uses.log.logger.info("Перемещение вызванного юзером кастомера в пул отложенных.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        params.textData = status;
        try {
            send(netProperty, Uses.TASK_CUSTOMER_TO_POSTPON, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
    }

    /**
     * Изменение отложенному кастомеру статеса
     * @param netProperty параметры соединения с сервером
     * @param postponCustomerId меняем этому кастомеру
     * @param status просто строка. берется из возможных состояний завершения работы
     */
    public static void postponeCustomerChangeStatus(INetProperty netProperty, long postponCustomerId, String status) {
        Uses.log.logger.info("Перемещение вызванного юзером кастомера в пул отложенных.");
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
     * @param netProperty параметры соединения с сервером
     * @param userName
     */
    public static void getStartCustomer(INetProperty netProperty, String userName) {
        Uses.log.logger.info("Начать работу с вызванным кастомером.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        try {
            send(netProperty, Uses.TASK_START_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
    }

    /**
     * Закончить работу с вызванным кастомером.
     * @param netProperty параметры соединения с сервером
     * @param userName
     * @param resultId
     * @param comments это если закончили работать с редиректенным и его нужно вернуть
     */
    public static void getFinishCustomer(INetProperty netProperty, String userName, Long resultId, String comments) {
        Uses.log.logger.info("Закончить работу с вызванным кастомером.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        params.resultId = resultId;
        params.textData = comments;
        try {
            send(netProperty, Uses.TASK_FINISH_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
    }

    /**
     * Переадресовать клиента в другую очередь.
     * @param netProperty параметры соединения с сервером
     * @param userName
     * @param service
     * @param requestBack
     * @param comments комментарии при редиректе
     */
    public static void redirectCustomer(INetProperty netProperty, String userName, String service, boolean requestBack, String comments) {
        Uses.log.logger.info("Переадресовать клиента в другую очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        params.serviceName = service;
        params.requestBack = requestBack;
        params.textData = comments;
        try {
            send(netProperty, Uses.TASK_REDIRECT_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
    }

    /**
     * Подтверждение живости клиентом для сервера.
     * @param netProperty параметры соединения с сервером
     * @param userName
     * @return XML-ответ
     */
    public static Element setLive(INetProperty netProperty, String userName) {
        Uses.log.logger.info("Ответим что живы и здоровы.");
        final CmdParams params = new CmdParams();
        params.userName = userName;
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
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     * @throws IOException
     */
    public static LinkedList<ServiceInfo> getServerState(INetProperty netProperty) throws IOException {
        Uses.log.logger.info("Получение описания состояния сервера.");
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение описания состояния пункта регистрации.
     * @param netProperty параметры соединения с пунктом регистрации
     * @param message что-то вроде названия команды для пункта регистрации
     * @return некий ответ от пункта регистрации, вроде прям как строка для вывода
     */
    public static String getWelcomeState(INetProperty netProperty, String message) {
        Uses.log.logger.info("Получение описания состояния пункта регистрации.");
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Добавить сервис в список обслуживаемых юзером использую параметры.
     * Используется при добавлении на горячую.
     * @param netProperty параметры соединения с пунктом регистрации
     * @param serviceName 
     * @param userName 
     * @param coeff 
     * @return содержить строковое сообщение о результате.
     */
    public static String setServiseFire(INetProperty netProperty, String serviceName, String userName, int coeff) {
        Uses.log.logger.info("Привязка услуги пользователю на горячую.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        params.serviceName = serviceName;
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Удалить сервис из списока обслуживаемых юзером использую параметры.
     * Используется при добавлении на горячую.
     * @param netProperty параметры соединения с пунктом регистрации
     * @param serviceName 
     * @param userName 
     * @return содержить строковое сообщение о результате.
     */
    public static String deleteServiseFire(INetProperty netProperty, String serviceName, String userName) {
        Uses.log.logger.info("Удаление услуги пользователю на горячую.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        params.serviceName = serviceName;
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение конфигурации главного табло - ЖК или плазмы.
     * Это XML-файл лежащий в папку приложения mainboard.xml
     * @param netProperty параметры соединения с сервером
     * @return корень XML-файла mainboard.xml
     * @throws DocumentException принятый текст может не преобразоваться в XML
     */
    public static Element getBoardConfig(INetProperty netProperty) throws DocumentException {
        Uses.log.logger.info("Получение конфигурации главного табло - ЖК или плазмы.");
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return DocumentHelper.parseText(rpc.getResult()).getRootElement();
    }

    /**
     * Сохранение конфигурации главного табло - ЖК или плазмы.
     * Это XML-файл лежащий в папку приложения mainboard.xml
     * @param netProperty параметры соединения с сервером
     * @param boardConfig
     */
    public static void saveBoardConfig(INetProperty netProperty, Element boardConfig) {
        Uses.log.logger.info("Сохранение конфигурации главного табло - ЖК или плазмы.");
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
     * Получение недельной таблици с данными для предварительной записи.
     * @param netProperty netProperty параметры соединения с сервером.
     * @param service услуга, в которую пытаемся встать.
     * @param date первый день недели за которую нужны данные.
     * @param advancedCustomer ID авторизованного кастомера
     * @return класс с параметрами и списком времен
     */
    public static RpcGetGridOfWeek.GridAndParams getGridOfWeek(INetProperty netProperty, String service, Date date, long advancedCustomer) {
        Uses.log.logger.info("Получить таблицу");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceName = service;
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Предварительная запись в очередь.
     * @param netProperty netProperty параметры соединения с сервером.
     * @param service услуга, в которую пытаемся встать.
     * @param date
     * @param advancedCustomer ID авторизованного кастомера
     * @return предварительный кастомер
     */
    public static QAdvanceCustomer standInServiceAdvance(INetProperty netProperty, String service, Date date, long advancedCustomer) {
        Uses.log.logger.info("Записать предварительно в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceName = service;
        params.date = date.getTime();
        params.customerId = advancedCustomer;
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Предварительная запись в очередь.
     * @param netProperty netProperty параметры соединения с сервером.
     * @param advanceID идентификатор предварительно записанного.
     * @return XML-ответ.
     */
    public static RpcStandInService standAndCheckAdvance(INetProperty netProperty, Long advanceID) {
        Uses.log.logger.info("Постановка предварительно записанных в очередь.");
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc;
    }

    /**
     * Рестарт сервера.
     * @param netProperty параметры соединения с сервером
     */
    public static void restartServer(INetProperty netProperty) {
        Uses.log.logger.info("Команда на рестарт сервера.");
        try {
            send(netProperty, Uses.TASK_RESTART, null);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
    }

    /**
     * Получение списка отзывов
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     */
    public static LinkedList<QRespItem> getResporseList(INetProperty netProperty) {
        Uses.log.logger.info("Команда на получение списка отзывов.");
        String res = null;
        try {
            // загрузим ответ
            res = send(netProperty, Uses.TASK_GET_RESPONSE_LIST, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        } finally {
            if (res == null) {
                return null;
            }
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetRespList rpc;
        try {
            rpc = gson.fromJson(res, RpcGetRespList.class);
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Оставить отзыв.
     * @param netProperty параметры соединения с сервером.
     * @param respID идентификатор выбранного отзыва
     */
    public static void setResponseAnswer(INetProperty netProperty, Long respID) {
        Uses.log.logger.info("Отправка выбранного отзыва.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.responseId = respID;
        try {
            send(netProperty, Uses.TASK_SET_RESPONSE_ANSWER, params);
        } catch (QException ex) {// вывод исключений
            throw new ServerException("Проблема с командой. ", ex);
        }
    }

    /**
     * Получение информационного дерева
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     */
    public static QInfoItem getInfoTree(INetProperty netProperty) {
        Uses.log.logger.info("Команда на получение информационного дерева.");
        String res = null;
        try {
            // загрузим ответ
            res = send(netProperty, Uses.TASK_GET_INFO_TREE, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        } finally {
            if (res == null) {
                return null;
            }
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetInfoTree rpc;
        try {
            rpc = gson.fromJson(res, RpcGetInfoTree.class);
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение описания залогинившегося юзера.
     * @param netProperty параметры соединения с сервером
     * @param id
     * @return XML-ответ
     */
    public static QAuthorizationCustomer getClientAuthorization(INetProperty netProperty, String id) {
        Uses.log.logger.info("Получение описания авторизованного пользователя.");
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение списка возможных результатов работы с клиентом
     * @param netProperty параметры соединения с сервером
     * @return свисок возможных завершений работы
     */
    public static LinkedList<QResult> getResultsList(INetProperty netProperty) {
        Uses.log.logger.info("Команда на получение списка возможных результатов работы с клиентом.");
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Изменение приоритета кастомеру
     * @param netProperty параметры соединения с сервером
     * @param prioritet
     * @param customer
     * @return Текстовый ответ о результате
     */
    public static String setCustomerPriority(INetProperty netProperty, int prioritet, String customer) {
        Uses.log.logger.info("Команда на повышение приоритета кастомеру.");
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получить список отложенных кастомеров
     * @param netProperty
     * @return список отложенных кастомеров
     */
    public static LinkedList<QCustomer> getPostponedPoolInfo(INetProperty netProperty) {
        Uses.log.logger.info("Команда на обновление пула отложенных.");
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
        } catch (JsonParseException ex) {
            throw new ClientException("Не возможно интерпритировать ответ.\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Вызов отложенного кастомера
     * @param netProperty
     * @param userName название юзера который вызывает
     * @param id это ID кастомера которого вызываем из пула отложенных, оно есть т.к. с качстомером давно работаем
     */
    public static void invitePostponeCustomer(INetProperty netProperty, String userName, Long id) {
        Uses.log.logger.info("Команда на вызов кастомера из пула отложенных.");
        final CmdParams params = new CmdParams();
        params.userName = userName;
        params.customerId = id;
        // загрузим ответ
        try {
            send(netProperty, Uses.TASK_INVITE_POSTPONED, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException("Проблема с командой. ", ex);
        }
    }
}
