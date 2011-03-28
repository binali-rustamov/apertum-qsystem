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
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Scanner;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import ru.apertum.qsystem.common.GsonPool;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.cmd.CmdParams;
import ru.apertum.qsystem.common.cmd.JsonRPC20;

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
    synchronized private static Element send(INetProperty netProperty, String commandName, CmdParams params) throws IOException, DocumentException {
        jsonRpc.setMethod(commandName);
        jsonRpc.setParams(params);

        final String message;
        final Gson gson = GsonPool.getInstance().borrowGson();
        try {
            message = gson.toJson(jsonRpc);
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        Uses.log.logger.trace("Задание \"" + commandName + "\" на " + netProperty.getServerAddress().getHostAddress() + ":" + netProperty.getServerPort() + "#\n" + message);
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
        final String data = URLDecoder.decode(sb.toString(), "utf-8");
        socket.close();
        writer.close();
        in.close();
        // преобразуем ответ в XML
        Uses.log.logger.trace("Ответ:\n" + data);
        return DocumentHelper.parseText(data).getRootElement();
    }

    /**
     * Получение возможных услуг.
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     */
    public static Element getServiсes(INetProperty netProperty) {
        Uses.log.logger.info("Получение возможных услуг.");
        // загрузим ответ
        Element res = null;
        try {
            res = send(netProperty, Uses.TASK_GET_SERVICES, null);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        } finally {
            if (res == null) {
                return null;
            }
        }
        return res;
    }

    /**
     * Постановка в очередь.
     * @param netProperty netProperty параметры соединения с сервером.
     * @param service услуга, в которую пытаемся встать.
     * @param password пароль того кто пытается выполнить задание.
     * @param priority приоритет.
     * @param inputData
     * @return XML-ответ.
     */
    public static Element standInService(INetProperty netProperty, String service, String password, int priority, String inputData) {
        Uses.log.logger.info("Встать в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceName = service;
        params.password = password;
        params.priority = priority;
        params.textData = inputData;
        try {
            return send(netProperty, Uses.TASK_STAND_IN, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Узнать сколько народу стоит к услуге и т.д.
     * @param netProperty параметры соединения с сервером.
     * @param serviceName название услуги о которой получаем информацию
     * @return XML-ответ.
     * @throws IOException
     * @throws DocumentException
     */
    public static Element aboutService(INetProperty netProperty, String serviceName) throws IOException, DocumentException {
        Uses.log.logger.info("Встать в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceName = serviceName;
        try {
            return send(netProperty, Uses.TASK_ABOUT_SERVICE, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }

    }

    /**
     * Получение описания залогинившегося юзера.
     * @param netProperty параметры соединения с сервером
     * @param password
     * @return XML-ответ
     */
    public static Element getSelf(INetProperty netProperty, String password) {
        Uses.log.logger.info("Получение описания залогинившегося юзера.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.password = password;
        Element res = null;
        try {
            res = send(netProperty, Uses.TASK_GET_SELF, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        } finally {
            if (res == null) {
                System.exit(1);
            }
        }
        return res;
    }

    /**
     * Получение описания всех юзеров для выбора себя.
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ все юзеры системы
     */
    public static Element getUsers(INetProperty netProperty) {
        Uses.log.logger.info("Получение описания всех юзеров для выбора себя.");
        // загрузим ответ
        Element res = null;
        try {
            res = send(netProperty, Uses.TASK_GET_USERS, null);
        } catch (IOException e) {// вывод исключений
            Uses.closeSplash();
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            Uses.closeSplash();
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        } finally {
            if (res == null) {
                System.exit(1);
            }
        }
        return res;
    }

    /**
     * Получение описания очередей для юзера.
     * @param netProperty параметры соединения с сервером
     * @param userName имя пользователя для которого идет опрос
     * @return XML-ответ
     */
    public static Element getSelfServices(INetProperty netProperty, String userName) {
        Uses.log.logger.info("Получение описания очередей для юзера.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        try {
            return send(netProperty, Uses.TASK_GET_SELF_SERVICES, params);
        } catch (IOException e) {// вывод исключений
            Uses.closeSplash();
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            Uses.closeSplash();
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Получение описания очередей для юзера.
     * @param netProperty параметры соединения с сервером
     * @param userName имя пользователя для которого идет опрос
     * @return XML-ответ
     */
    public static Element getSelfServicesCheck(INetProperty netProperty, String userName) {
        Uses.log.logger.info("Получение описания очередей для юзера.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        try {
            return send(netProperty, Uses.TASK_GET_SELF_SERVICES_CHECK, params);
        } catch (IOException e) {// вывод исключений
            Uses.closeSplash();
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            Uses.closeSplash();
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Получение слкдующего юзера из очередей, обрабатываемых юзером.
     * @param netProperty параметры соединения с сервером
     * @param userName
     * @return XML-ответ
     */
    public static Element inviteNextCustomer(INetProperty netProperty, String userName) {
        Uses.log.logger.info("Получение следующего юзера из очередей, обрабатываемых юзером.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        try {
            return send(netProperty, Uses.TASK_INVITE_NEXT_CUSTOMER, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Удаление вызванного юзером кастомера.
     * @param netProperty параметры соединения с сервером
     * @param userName
     * @return XML-ответ
     */
    public static Element killNextCustomer(INetProperty netProperty, String userName) {
        Uses.log.logger.info("Удаление вызванного юзером кастомера.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        try {
            return send(netProperty, Uses.TASK_KILL_NEXT_CUSTOMER, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Начать работу с вызванным кастомером.
     * @param netProperty параметры соединения с сервером
     * @param userName
     * @return XML-ответ
     */
    public static Element getStartCustomer(INetProperty netProperty, String userName) {
        Uses.log.logger.info("Начать работу с вызванным кастомером.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        try {
            return send(netProperty, Uses.TASK_START_CUSTOMER, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Закончить работу с вызванным кастомером.
     * @param netProperty параметры соединения с сервером
     * @param userName
     * @param resultId
     * @return XML-ответ
     */
    public static Element getFinishCustomer(INetProperty netProperty, String userName, Long resultId) {
        Uses.log.logger.info("Закончить работу с вызванным кастомером.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        params.resultId = resultId;
        try {
            return send(netProperty, Uses.TASK_FINISH_CUSTOMER, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Переадресовать клиента в другую очередь.
     * @param netProperty параметры соединения с сервером
     * @param userName
     * @param service
     * @param requestBack
     * @return XML-ответ
     */
    public static Element redirectCustomer(INetProperty netProperty, String userName, String service, boolean requestBack) {
        Uses.log.logger.info("Переадресовать клиента в другую очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        params.serviceName = service;
        params.requestBack = requestBack;
        try {
            return send(netProperty, Uses.TASK_REDIRECT_CUSTOMER, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
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
        try {
            // загрузим ответ
            return send(netProperty, Uses.TASK_I_AM_LIVE, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Получение описания состояния сервера.
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     * @throws IOException
     */
    public static Element getServerState(INetProperty netProperty) throws IOException {
        Uses.log.logger.info("Получение описания состояния сервера.");
        // загрузим ответ
        try {
            return send(netProperty, Uses.TASK_SERVER_STATE, null);
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Получение описания состояния пункта регистрации.
     * @param netProperty параметры соединения с пунктом регистрации
     * @param message 
     * @return XML-ответ
     * @throws IOException
     */
    public static Element getWelcomeState(INetProperty netProperty, String message) throws IOException {
        Uses.log.logger.info("Получение описания состояния пункта регистрации.");
        // загрузим ответ
        try {
            return send(netProperty, message, null);
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Добавить сервис в список обслуживаемых юзером использую параметры.
     * Используется при добавлении на горячую.
     * @param netProperty параметры соединения с пунктом регистрации
     * @param serviceName 
     * @param userName 
     * @param coeff 
     * @return XML-ответ. Текстовый узел корня содержить строковое сообщение о результате.
     * @throws IOException
     */
    public static Element setServiseFire(INetProperty netProperty, String serviceName, String userName, int coeff) throws IOException {
        Uses.log.logger.info("Привязка услуги пользователю на горячую.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        params.serviceName = serviceName;
        params.coeff = coeff;
        try {
            return send(netProperty, Uses.TASK_SET_SERVICE_FIRE, params);
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Удалить сервис из списока обслуживаемых юзером использую параметры.
     * Используется при добавлении на горячую.
     * @param netProperty параметры соединения с пунктом регистрации
     * @param serviceName 
     * @param userName 
     * @return XML-ответ. Текстовый узел корня содержить строковое сообщение о результате.
     * @throws IOException
     */
    public static Element deleteServiseFire(INetProperty netProperty, String serviceName, String userName) throws IOException {
        Uses.log.logger.info("Удаление услуги пользователю на горячую.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userName = userName;
        params.serviceName = serviceName;
        try {
            return send(netProperty, Uses.TASK_DELETE_SERVICE_FIRE, params);
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Получение конфигурации главного табло - ЖК или плазмы.
     * Это XML-файл лежащий в папку приложения mainboard.xml
     * @param netProperty параметры соединения с сервером
     * @return корень XML-файла mainboard.xml
     * @throws java.io.IOException отправка/получение может глючить
     */
    public static Element getBoardConfig(INetProperty netProperty) throws IOException {
        Uses.log.logger.info("Получение конфигурации главного табло - ЖК или плазмы.");
        // загрузим ответ
        try {
            return send(netProperty, Uses.TASK_GET_BOARD_CONFIG, null);
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Сохранение конфигурации главного табло - ЖК или плазмы.
     * Это XML-файл лежащий в папку приложения mainboard.xml
     * @param netProperty параметры соединения с сервером
     * @param boardConfig
     * @return проигнорить
     * @throws java.io.IOException отправка/получение может глючить
     */
    public static Element saveBoardConfig(INetProperty netProperty, Element boardConfig) throws IOException {
        Uses.log.logger.info("Сохранение конфигурации главного табло - ЖК или плазмы.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.textData = boardConfig.asXML();
        try {
            return send(netProperty, Uses.TASK_SAVE_BOARD_CONFIG, params);
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Получение недельной таблици с данными для предварительной записи.
     * @param netProperty netProperty параметры соединения с сервером.
     * @param service услуга, в которую пытаемся встать.
     * @param date первый день недели за которую нужны данные.
     * @param advancedCustomer ID авторизованного кастомера
     * @return XML-ответ.
     */
    public static Element getGridOfWeek(INetProperty netProperty, String service, Date date, long advancedCustomer) {
        Uses.log.logger.info("Получить таблицу");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceName = service;
        params.date = date.getTime();
        params.customerId = advancedCustomer;
        try {
            return send(netProperty, Uses.TASK_GET_GRID_OF_WEEK, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Предварительная запись в очередь.
     * @param netProperty netProperty параметры соединения с сервером.
     * @param service услуга, в которую пытаемся встать.
     * @param date
     * @param advancedCustomer ID авторизованного кастомера
     * @return XML-ответ.
     */
    public static Element standInServiceAdvance(INetProperty netProperty, String service, Date date, long advancedCustomer) {
        Uses.log.logger.info("Записать предварительно в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceName = service;
        params.date = date.getTime();
        params.customerId = advancedCustomer;
        try {
            return send(netProperty, Uses.TASK_ADVANCE_STAND_IN, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Предварительная запись в очередь.
     * @param netProperty netProperty параметры соединения с сервером.
     * @param advanceID идентификатор предварительно записанного.
     * @return XML-ответ.
     */
    public static Element standAndCheckAdvance(INetProperty netProperty, Long advanceID) {
        Uses.log.logger.info("Постановка предварительно записанных в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.customerId = advanceID;
        try {
            return send(netProperty, Uses.TASK_ADVANCE_CHECK_AND_STAND, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Рестарт сервера.
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     */
    public static Element restartServer(INetProperty netProperty) {
        Uses.log.logger.info("Команда на рестарт сервера.");
        try {
            // загрузим ответ
            return send(netProperty, Uses.TASK_RESTART, null);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Получение списка отзывов
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     */
    public static Element getResporseList(INetProperty netProperty) {
        Uses.log.logger.info("Команда на получение списка отзывов.");
        try {
            // загрузим ответ
            return send(netProperty, Uses.TASK_GET_RESPONSE_LIST, null);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Оставить отзыв.
     * @param netProperty параметры соединения с сервером.
     * @param respID идентификатор выбранного отзыва
     * @return XML-ответ.
     */
    public static Element setResponseAnswer(INetProperty netProperty, Long respID) {
        Uses.log.logger.info("Отправка выбранного отзыва.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.responseId = respID;
        try {
            return send(netProperty, Uses.TASK_SET_RESPONSE_ANSWER, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Получение информационного дерева
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     */
    public static Element getInfoTree(INetProperty netProperty) {
        Uses.log.logger.info("Команда на получение информационного дерева.");
        try {
            // загрузим ответ
            return send(netProperty, Uses.TASK_GET_INFO_TREE, null);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Получение описания залогинившегося юзера.
     * @param netProperty параметры соединения с сервером
     * @param id
     * @return XML-ответ
     */
    public static Element getClientAuthorization(INetProperty netProperty, String id) {
        Uses.log.logger.info("Получение описания авторизованного пользователя.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.clientAuthId = id;
        Element res = null;
        try {
            res = send(netProperty, Uses.TASK_GET_CLIENT_AUTHORIZATION, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
        return res;
    }

    /**
     * Получение списка возможных результатов работы с клиентом
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     */
    public static Element getResultsList(INetProperty netProperty) {
        Uses.log.logger.info("Команда на получение списка возможных результатов работы с клиентом.");
        try {
            // загрузим ответ
            return send(netProperty, Uses.TASK_GET_RESULTS_LIST, null);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Изменение приоритета кастомеру
     * @param netProperty параметры соединения с сервером
     * @param prioritet
     * @param customer
     * @return XML-ответ
     */
    public static Element setCustomerPriority(INetProperty netProperty, int prioritet, String customer) {
        Uses.log.logger.info("Команда на повышение приоритета кастомеру.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.priority = prioritet;
        params.clientAuthId = customer;
        Element res = null;
        try {
            res = send(netProperty, Uses.TASK_SET_CUSTOMER_PRIORITY, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
        return res;
    }

    /**
     * Узнать есть ли информация по услуге, которая должна быть предоставлена кастомеру перед постановкой в очередь
     * @param netProperty параметры соединения с сервером.
     * @param serviceName название услуги о которой получаем информацию
     * @return XML-ответ.
     * @throws IOException
     * @throws DocumentException
     */
    public static Element getPreInfoForService(INetProperty netProperty, String serviceName) throws IOException, DocumentException {
        Uses.log.logger.info("Узнать есть ли информация по услуге.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceName = serviceName;
        return send(netProperty, Uses.TASK_GET_SERVICE_PREINFO, params);
    }

    /**
     * Узнать есть ли информация для печати информационного узла
     * @param netProperty параметры соединения с сервером.
     * @param infoItemName название информационного узла о котором получаем информацию
     * @return XML-ответ.
     */
    public static Element getPintForInfoItem(INetProperty netProperty, String infoItemName) {
        Uses.log.logger.info("Узнать есть ли что напечатать для информации.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.infoItemName = infoItemName;
        Element res = null;
        try {
            res = send(netProperty, Uses.TASK_GET_INFO_PRINT, params);
        } catch (IOException e) {// вывод исключений
            throw new Uses.ClientException("Невозможно получить ответ от сервера. " + e.toString());
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
        return res;
    }
}
