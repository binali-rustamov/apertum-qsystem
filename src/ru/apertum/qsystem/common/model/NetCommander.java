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
import ru.apertum.qsystem.common.Uses;

/**
 * Содержит статические методы отправки и получения заданий на сервер.
 * любой метод возвращает XML-узел ответа сервера.
 * @author Evgeniy Egorov
 */
public class NetCommander {

    public final static String PARAM_SUPER_SITE = "%SUPER_SITE%";
    public final static String PARAM_SITE_MARK = "%PARAM_SITE_MARK%";
    public final static String PARAM_INPUT_DATA = "%INPUT_DATA%";
    public final static String PARAM_SERVICE = "%SERVICE%";
    private final static String PARAM_ID = "%ID%";
    private final static String PARAM_AUTH_CUSTOMER_ID = "%AUTH_CUSTOMER_ID%";
    public final static String PARAM_PASSWORD = "%PASSWORD%";
    private final static String PARAM_USERNAME = "%USERNAME%";
    private final static String PARAM_RESULT_ID = "%RESULT_ID%";
    public final static String PARAM_PRIORITY = "%PRIORITY%";
    private final static String PARAM_REQUEST_BACK = "%REQUEST_BACK%";
    private final static String PARAM_KOEFFICIENT = "%KOEFFICIENT%";
    private final static String PARAM_DATE = "%DATE%";
    private final static String TASK_GET_SERVICES = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_GET_SERVICES + "\"/>";
    private final static String TASK_GET_INFO_TREE = "<" + Uses.TASK_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_GET_INFO_TREE + "\"/>";
    private final static String TASK_GET_RESULTS_LIST = "<" + Uses.TASK_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_GET_RESULTS_LIST + "\"/>";
    private final static String TASK_GET_RESPONSE_LIST = "<" + Uses.TASK_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_GET_RESPONSE_LIST + "\"/>";
    private final static String TASK_SET_RESPONSE_ANSWER = "<" + Uses.TASK_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_SET_RESPONSE_ANSWER + "\" " + Uses.TAG_ID + "=\"" + PARAM_ID + "\"/>";
    private final static String TASK_I_AM_LIVE = "<" + Uses.TASK_SITE + " " + Uses.TAG_USER + "=\"" + PARAM_USERNAME + "\" " + Uses.TAG_NAME + "=\"" + Uses.TASK_I_AM_LIVE + "\"/>";
    private final static String TASK_ABOUT_SERVICE = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_ABOUT_SERVICE + "\" " + Uses.TAG_SERVICE + "=\"" + PARAM_SERVICE + "\" " + Uses.TASK_FOR_SITE + "=\"" + PARAM_SITE_MARK + "\"/>";
    private final static String TASK_GET_SERVICE_PREINFO = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_GET_SERVICE_PREINFO + "\" " + Uses.TAG_SERVICE + "=\"" + PARAM_SERVICE + "\" " + Uses.TASK_FOR_SITE + "=\"" + PARAM_SITE_MARK + "\"/>";
    private final static String TASK_GET_USERS = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_GET_USERS + "\"/>";
    public final static String TASK_STAND_IN = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_STAND_IN + "\" " + Uses.TAG_SERVICE + "=\"" + PARAM_SERVICE + "\" " + Uses.TAG_PASSWORD + "=\"" + PARAM_PASSWORD + "\" " + Uses.TAG_PRIORITY + "=\"" + PARAM_PRIORITY + "\" " + Uses.TAG_INPUT_DATA + "=\"" + PARAM_INPUT_DATA + "\" " + Uses.TASK_FOR_SITE + "=\"" + PARAM_SITE_MARK + "\"/>";
    private final static String TASK_ADVANCE_STAND_IN = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_ADVANCE_STAND_IN + "\" " + Uses.TAG_SERVICE + "=\"" + PARAM_SERVICE + "\" " + Uses.TAG_START_TIME + "=\"" + PARAM_DATE + "\" " + Uses.TAG_AUTH_CUSTOMER_ID + "=\"" + PARAM_AUTH_CUSTOMER_ID + "\" " + Uses.TASK_FOR_SITE + "=\"" + PARAM_SITE_MARK + "\"/>";
    private final static String TASK_ADVANCE_CHECK_AND_STAND = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_ADVANCE_CHECK_AND_STAND + "\" " + Uses.TAG_ID + "=\"" + PARAM_ID + "\"/>";
    private final static String TASK_GET_SELF = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_GET_SELF + "\" " + Uses.TAG_PASSWORD + "=\"" + PARAM_PASSWORD + "\"/>";
    private final static String TASK_GET_SELF_SERVICES = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_GET_SELF_SERVICES + "\" " + Uses.TAG_USER + "=\"" + PARAM_USERNAME + "\"/>";
    private final static String TASK_GET_SELF_SERVICES_CHECK = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_GET_SELF_SERVICES_CHECK + "\" " + Uses.TAG_USER + "=\"" + PARAM_USERNAME + "\"/>";
    private final static String TASK_INVITE_NEXT_CUSTOMER = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_INVITE_NEXT_CUSTOMER + "\" " + Uses.TAG_USER + "=\"" + PARAM_USERNAME + "\"/>";
    private final static String TASK_KILL_NEXT_CUSTOMER = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_KILL_NEXT_CUSTOMER + "\" " + Uses.TAG_USER + "=\"" + PARAM_USERNAME + "\"/>";
    private final static String TASK_START_CUSTOMER = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_START_CUSTOMER + "\" " + Uses.TAG_USER + "=\"" + PARAM_USERNAME + "\"/>";
    private final static String TASK_FINISH_CUSTOMER = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_FINISH_CUSTOMER + "\" " + Uses.TAG_USER + "=\"" + PARAM_USERNAME + "\" " + Uses.TAG_RESULT_ITEM + "=\"" + PARAM_RESULT_ID + "\"/>";
    private final static String TASK_REDIRECT_CUSTOMER = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_REDIRECT_CUSTOMER + "\" " + Uses.TAG_USER + "=\"" + PARAM_USERNAME + "\" " + Uses.TAG_SERVICE + "=\"" + PARAM_SERVICE + "\" " + Uses.TAG_REQUEST_BACK + "=\"" + PARAM_REQUEST_BACK + "\"/>";
    private final static String TASK_GET_SERVER_STATE = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_SERVER_STATE + "\"/>";
    private final static String TASK_SET_SERVICE_FIRE = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_SET_SERVICE_FIRE + "\" " + Uses.TAG_USER + "=\"" + PARAM_USERNAME + "\" " + Uses.TAG_SERVICE + "=\"" + PARAM_SERVICE + "\" " + Uses.TAG_PROP_KOEF + "=\"" + PARAM_KOEFFICIENT + "\"/>";
    private final static String TASK_DELETE_SERVICE_FIRE = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_DELETE_SERVICE_FIRE + "\" " + Uses.TAG_USER + "=\"" + PARAM_USERNAME + "\" " + Uses.TAG_SERVICE + "=\"" + PARAM_SERVICE + "\"/>";
    private final static String TASK_GET_BOARD_CONFIG = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_GET_BOARD_CONFIG + "\"/>";
    private final static String TASK_GET_GRID_OF_WEEK = "<" + PARAM_SUPER_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_GET_GRID_OF_WEEK + "\" " + Uses.TAG_SERVICE + "=\"" + PARAM_SERVICE + "\" " + Uses.TAG_START_TIME + "=\"" + PARAM_DATE + "\" " + Uses.TAG_AUTH_CUSTOMER_ID + "=\"" + PARAM_AUTH_CUSTOMER_ID + "\" " + Uses.TASK_FOR_SITE + "=\"" + PARAM_SITE_MARK + "\"/>";
    private final static String TASK_GET_CLIENT_AUTHORIZATION = "<" + Uses.TASK_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_GET_CLIENT_AUTHORIZATION + "\" " + Uses.TAG_ID + "=\"" + PARAM_ID + "\"/>";
    private final static String TASK_SET_CUSTOMER_PRIORITY = "<" + Uses.TASK_SITE + " " + Uses.TAG_NAME + "=\"" + Uses.TASK_SET_CUSTOMER_PRIORITY + "\" " + Uses.TAG_PRIORITY + "=\"" + PARAM_PRIORITY + "\" " + Uses.TAG_NUMBER + "=\"" + PARAM_USERNAME + "\"/>";
    /**
     * Ответ о живости
     */
    public static final String I_AM_LIVE = "<Действие " + Uses.TAG_NAME + "=\"" + Uses.TASK_I_AM_LIVE + "\"/>";

    /**
     *  основная работа по отсылки и получению результата.
     * @param netProperty параметры соединения с сервером
     * @param message отсылаемое сообщение.
     * @return XML-ответ
     */
    synchronized private static Element send(INetProperty netProperty, String message) throws IOException, DocumentException {
        // Пойдет ли команда на суперсайт или нет зависит от параметров.
        final String superSite;
        if (netProperty.IsSuperSite()) {
            superSite = Uses.TASK_SUPER_SITE;
        } else {
            superSite = Uses.TASK_SITE;
        }
        message = message.replaceFirst(PARAM_SUPER_SITE, superSite);
        Uses.log.logger.trace("Задание на " + netProperty.getServerAddress().getHostAddress() + ":" + netProperty.getServerPort() + "#\n" + message);
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
            res = send(netProperty, TASK_GET_SERVICES);
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
     * @param siteMark маркировка сайта для доменной работы если требуется, иначе "".
     * @param inputData
     * @return XML-ответ.
     */
    public static Element standInService(INetProperty netProperty, String service, String password, int priority, String siteMark, String inputData) {
        Uses.log.logger.info("Встать в очередь.");
        // загрузим ответ
        String mes = TASK_STAND_IN;
        mes = mes.replaceFirst(PARAM_SERVICE, service);
        mes = mes.replaceFirst(PARAM_PASSWORD, password);
        mes = mes.replaceFirst(PARAM_PRIORITY, String.valueOf(priority));
        mes = mes.replaceFirst(PARAM_SITE_MARK, siteMark);
        mes = mes.replaceFirst(PARAM_INPUT_DATA, inputData == null ? "" : inputData);
        try {
            return send(netProperty, mes);
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
     * @param siteMark маркировка сайта для доменной работы если требуется, иначе "".
     * @return XML-ответ.
     * @throws IOException
     * @throws DocumentException
     */
    public static Element aboutService(INetProperty netProperty, String serviceName, String siteMark) throws IOException, DocumentException {
        Uses.log.logger.info("Встать в очередь.");
        // загрузим ответ
        String mes = TASK_ABOUT_SERVICE;
        mes = mes.replaceFirst(PARAM_SERVICE, serviceName);
        mes = mes.replaceFirst(PARAM_SITE_MARK, siteMark);
        return send(netProperty, mes);

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
        String mes = TASK_GET_SELF;
        mes = mes.replaceFirst(PARAM_PASSWORD, password);
        Element res = null;
        try {
            res = send(netProperty, mes);
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
            res = send(netProperty, TASK_GET_USERS);
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
        String mes = TASK_GET_SELF_SERVICES;
        mes = mes.replaceFirst(PARAM_USERNAME, userName);
        try {
            return send(netProperty, mes);
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
        String mes = TASK_GET_SELF_SERVICES_CHECK;
        mes = mes.replaceFirst(PARAM_USERNAME, userName);
        try {
            return send(netProperty, mes);
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
        String mes = TASK_INVITE_NEXT_CUSTOMER;
        mes = mes.replaceFirst(PARAM_USERNAME, userName);
        try {
            return send(netProperty, mes);
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
        String mes = TASK_KILL_NEXT_CUSTOMER;
        mes = mes.replaceFirst(PARAM_USERNAME, userName);
        try {
            return send(netProperty, mes);
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
        String mes = TASK_START_CUSTOMER;
        mes = mes.replaceFirst(PARAM_USERNAME, userName);
        try {
            return send(netProperty, mes);
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
        String mes = TASK_FINISH_CUSTOMER;
        mes = mes.replaceFirst(PARAM_USERNAME, userName);
        mes = mes.replaceFirst(PARAM_RESULT_ID, resultId.toString());
        try {
            return send(netProperty, mes);
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
        String mes = TASK_REDIRECT_CUSTOMER;
        mes = mes.replaceFirst(PARAM_USERNAME, userName);
        mes = mes.replaceFirst(PARAM_SERVICE, service);
        mes = mes.replaceFirst(PARAM_REQUEST_BACK, requestBack ? "1" : "0");
        try {
            return send(netProperty, mes);
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
        String mes = TASK_I_AM_LIVE;
        mes = mes.replaceFirst(PARAM_USERNAME, userName);
        try {
            // загрузим ответ
            return send(netProperty, mes);
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
        String mes = TASK_GET_SERVER_STATE;
        try {
            return send(netProperty, mes);
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
            return send(netProperty, message);
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
        String mes = TASK_SET_SERVICE_FIRE;
        mes = mes.replaceFirst(PARAM_USERNAME, userName);
        mes = mes.replaceFirst(PARAM_SERVICE, serviceName);
        mes = mes.replaceFirst(PARAM_KOEFFICIENT, String.valueOf(coeff));
        try {
            return send(netProperty, mes);
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
        String mes = TASK_DELETE_SERVICE_FIRE;
        mes = mes.replaceFirst(PARAM_USERNAME, userName);
        mes = mes.replaceFirst(PARAM_SERVICE, serviceName);
        try {
            return send(netProperty, mes);
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
        final String mes = TASK_GET_BOARD_CONFIG;
        try {
            return send(netProperty, mes);
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
        boardConfig.addAttribute(Uses.TAG_NAME, Uses.TASK_SAVE_BOARD_CONFIG);
        final String mes = boardConfig.asXML();
        try {
            return send(netProperty, mes);
        } catch (DocumentException e) {
            throw new Uses.ClientException("Не возможно интерпритировать ответ.\n" + e.toString());
        }
    }

    /**
     * Получение недельной таблици с данными для предварительной записи.
     * @param netProperty netProperty параметры соединения с сервером.
     * @param service услуга, в которую пытаемся встать.
     * @param date первый день недели за которую нужны данные.
     * @param siteMark маркировка сайта для доменной работы если требуется, иначе "".
     * @param advancedCustomer ID авторизованного кастомера
     * @return XML-ответ.
     */
    public static Element getGridOfWeek(INetProperty netProperty, String service, Date date, String siteMark, long advancedCustomer) {
        Uses.log.logger.info("Получить таблицу");
        // загрузим ответ
        String mes = TASK_GET_GRID_OF_WEEK;
        mes = mes.replaceFirst(PARAM_SERVICE, service);
        mes = mes.replaceFirst(PARAM_DATE, Uses.format_dd_MM_yyyy.format(date));
        mes = mes.replaceFirst(PARAM_SITE_MARK, siteMark);
        mes = mes.replaceFirst(PARAM_AUTH_CUSTOMER_ID, String.valueOf(advancedCustomer));
        try {
            return send(netProperty, mes);
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
     * @param siteMark маркировка сайта для доменной работы если требуется, иначе "".
     * @param advancedCustomer ID авторизованного кастомера
     * @return XML-ответ.
     */
    public static Element standInServiceAdvance(INetProperty netProperty, String service, Date date, String siteMark, long advancedCustomer) {
        Uses.log.logger.info("Записать предварительно в очередь.");
        // загрузим ответ
        String mes = TASK_ADVANCE_STAND_IN;
        mes = mes.replaceFirst(PARAM_SERVICE, service);
        mes = mes.replaceFirst(PARAM_DATE, Uses.format_for_trans.format(date));
        mes = mes.replaceFirst(PARAM_SITE_MARK, siteMark);
        mes = mes.replaceFirst(PARAM_AUTH_CUSTOMER_ID, String.valueOf(advancedCustomer));
        try {
            return send(netProperty, mes);
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
        String mes = TASK_ADVANCE_CHECK_AND_STAND;
        mes = mes.replaceFirst(PARAM_ID, advanceID.toString());
        try {
            return send(netProperty, mes);
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
            return send(netProperty, Uses.TASK_RESTART);
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
            return send(netProperty, TASK_GET_RESPONSE_LIST);
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
        String mes = TASK_SET_RESPONSE_ANSWER;
        mes = mes.replaceFirst(PARAM_ID, respID.toString());
        try {
            return send(netProperty, mes);
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
            return send(netProperty, TASK_GET_INFO_TREE);
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
        String mes = TASK_GET_CLIENT_AUTHORIZATION;
        mes = mes.replaceFirst(PARAM_ID, id);
        Element res = null;
        try {
            res = send(netProperty, mes);
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
            return send(netProperty, TASK_GET_RESULTS_LIST);
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
        String mes = TASK_SET_CUSTOMER_PRIORITY;
        mes = mes.replaceFirst(PARAM_PRIORITY, String.valueOf(prioritet));
        mes = mes.replaceFirst(PARAM_USERNAME, customer);
        Element res = null;
        try {
            res = send(netProperty, mes);
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
     * @param siteMark маркировка сайта для доменной работы если требуется, иначе "".
     * @return XML-ответ.
     * @throws IOException
     * @throws DocumentException
     */
    public static Element getPreInfoForService(INetProperty netProperty, String serviceName, String siteMark) throws IOException, DocumentException {
        Uses.log.logger.info("Встать в очередь.");
        // загрузим ответ
        String mes = TASK_GET_SERVICE_PREINFO;
        mes = mes.replaceFirst(PARAM_SERVICE, serviceName);
        mes = mes.replaceFirst(PARAM_SITE_MARK, siteMark);
        return send(netProperty, mes);

    }
}
