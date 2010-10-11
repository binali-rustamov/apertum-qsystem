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
package ru.apertum.qsystem.reports.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.util.HashMap;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.server.model.QSiteList;

/**
 * Генератор отчетов.
 * Класс, имеющий механизм генерации отчетов.
 * По названию отчета получает массив байт, хранящий отчет.
 * Так же в классе есть парара отчетов по текущему состоянию.
 * @author Evgeniy Egorov
 */
public class ReportGenerator {

    // задания, доступны по их именам
    private final static HashMap<String, IGenerator> generators = new HashMap<String, IGenerator>();

    public static void addGenerator(IGenerator generator) {
        generators.put(generator.getHref().toLowerCase(), generator);
    }
    /**
     * Это список сайтов домена.
     */
    private static QSiteList siteList = null;

    public static void addSiteList(QSiteList siteList) {
        ReportGenerator.siteList = siteList;
    }

    public static QSiteList getSiteList() {
        return siteList;
    }

    public static boolean isSuperSite() {
        return siteList != null;
    }
    /**
     * Это не отчет. это генератор списка отчетов, который проверяет пароль и пользователя и формирует
     * coocies для браузера, чтоб далее браузер подставлял жти куки в запрос и тем самым сервак "узнавал пользователя".
     * Сдесь нужен только метод preparation(), т.к. никакой генерации нет.
     */
    private final static IGenerator getReportsList = new AGenerator("reportList", "") {

        @Override
        protected JRDataSource getDataSource(String inputData) {
            throw new Uses.ReportException("Ошибочное обращение к методу.");
        }

        @Override
        protected byte[] preparation(String inputData) {
            // в запросе должен быть пароль и пользователь, если нету, то отказ на вход
            final String data = Uses.getRequestData(inputData);
            Uses.log.logger.trace("Принятые параметры \"" + data + "\".");
            // ресурс для выдачи в браузер. это либо список отчетов при корректном логининге или отказ на вход
            String res = "/ru/apertum/qsystem/reports/web/error_login.html";
            String usr = "err";
            String pwd = "err";
            // разбирем параметры
            final String[] ss = data.split("&");
            if (ss.length == 2) {
                final String[] ss0 = ss[0].split("=");
                final String[] ss1 = ss[1].split("=");
                if ("username".equals(ss0[0]) && ("password".equals(ss1[0]))) {
                    usr = ss0[1];
                    pwd = (ss1.length == 1 ? "" : ss1[1]);

                    if (pwd.equals(WebServer.passMap.get(usr))) {
                        res = "/ru/apertum/qsystem/reports/web/reportList.html";
                    }
                }
            }
            final InputStream inStream = getClass().getResourceAsStream(res);
            byte[] result = null;
            try {
                result = Uses.readInputStream(inStream);
                if ("/ru/apertum/qsystem/reports/web/reportList.html".equals(res)) {
                    // добавим список аналитических отчетов
                    result = new String(result).replaceFirst(Uses.ANCHOR_REPORT_LIST, WebServer.repList).getBytes(); //"Cp1251"
                    // Добавим кукисы сессии
                    //<META HTTP-EQUIV="Set-Cookie" CONTENT="NAME=value; EXPIRES=date; DOMAIN=domain_name; PATH=path; SECURE">
                    final String coocie = "<META HTTP-EQUIV=\"Set-Cookie\" CONTENT=\"username=" + URLEncoder.encode(usr, "utf-8") + "\">\n<META HTTP-EQUIV=\"Set-Cookie\" CONTENT=\"password=" + URLEncoder.encode(pwd, "utf-8") + "\">";
                    result = new String(result).replaceFirst(Uses.ANCHOR_COOCIES, coocie).getBytes(); //"Cp1251"
                }
            } catch (IOException ex) {
                throw new Uses.ReportException("Ошибка чтения ресурса для диалогового выбора отчета. " + ex);
            }
            return result;
        }

        @Override
        protected HashMap getParameters(String inputData) {
            throw new Uses.ReportException("Ошибочное обращение к методу.");
        }

        @Override
        protected Connection getConnection(String inputData) {
            throw new Uses.ReportException("Ошибочное обращение к методу.");
        }
    };
    /**
     * Отчет по текущему состоянию в разрее услуг
     */
    private final static IGenerator getReportCurrentServices = new AGenerator(Uses.REPORT_CURRENT_SERVICES.toLowerCase(), "/ru/apertum/qsystem/reports/templates/currentStateServices.jasper") {

        @Override
        protected JRDataSource getDataSource(String inputData) {
            try {
                return CurrentStatistic.getDataSourceCurrentServices();
            } catch (JRException ex) {
                throw new Uses.ReportException("Ошибка генерации. " + ex);
            } catch (UnsupportedEncodingException ex) {
                throw new Uses.ReportException("Ошибка генерации. Не поддерживается кодировка. " + ex);
            }
        }

        @Override
        protected byte[] preparation(String inputData) {
            return null;
        }

        @Override
        protected HashMap getParameters(String inputData) {
            return new HashMap();
        }

        @Override
        protected Connection getConnection(String inputData) {
            return null;
        }
    };
    /**
     * Отчет по текущему состоянию в разрезе пользователей
     */
    private final static IGenerator getRepCurrentUsers = new AGenerator(Uses.REPORT_CURRENT_USERS.toLowerCase(), "/ru/apertum/qsystem/reports/templates/currentStateUsers.jasper") {

        @Override
        protected JRDataSource getDataSource(String inputData) {
            try {
                return CurrentStatistic.getDataSourceCurrentUsers();
            } catch (JRException ex) {
                throw new Uses.ReportException("Ошибка генерации. " + ex);
            } catch (UnsupportedEncodingException ex) {
                throw new Uses.ReportException("Ошибка генерации. Не поддерживается кодировка. " + ex);
            }
        }

        @Override
        protected byte[] preparation(String inputData) {
            return null;
        }

        @Override
        protected HashMap getParameters(String inputData) {
            return new HashMap();
        }

        @Override
        protected Connection getConnection(String inputData) {
            return null;
        }
    };

    /**
     * Генерация отчета по его имени.
     * @param name Имя отчета.
     * @return Отчет в виде массива байт.
     */
    public static byte[] generate(String inputData) {
        final long start = System.currentTimeMillis();
        String GETString = Uses.getRequestTarget(inputData);
        final int pos = GETString.indexOf(".");
        final String nameReport;
        if (pos == -1) {
            nameReport = GETString;
        } else {
            nameReport = GETString.substring(0, pos);
        }

        final IGenerator generator = generators.get(nameReport.toLowerCase());
        // если нет такого отчета
        if (generator == null) {
            return null;
        }
        // Значит такой отчет есть и его можно сгенерировать
        // но если запрошен отчет, то должны приехать пароль и пользователь в куках
        // для определения доступа к отчетам.
        // Cookie: username=%D0%90%D0%B4%D0%BC%D0%B8%D0%BD%D0%B8%D1%81%D1%82%D1%80%D0%B0%D1%82%D0%BE%D1%80; password=
        // Проверим правильность доступа, и если все нормально сгенерируем отчет.
        // Иначе выдадим страничку запрета доступа
        // Но есть нюанс, формирование списка отчетов - тоже формироватор, и к нему доступ не по кукисам,
        // а по введеному паролю и пользователю. По этому надо проверить если приехали параметры пароля и пользователя,
        // введенные юзером, то игнорировать проверку кукисов. Т.е. если гениратор reportList, то не проверяем кукисы
        if (!"reportList.html".equals(Uses.getRequestTarget(inputData))) {
            final HashMap<String, String> coocies = Uses.getCoocies(inputData);
            final String pass = coocies.get("password");
            final String usr = coocies.get("username");
            if (pass == null || usr == null) {
                // если не нашлось в куках
                return getLoginPage();
            }
            if (!pass.equals(WebServer.passMap.get(usr))) {
                // если не совпали пароли
                return getLoginPage();
            }
        }
        Uses.writeRus("Генерация отчета: '" + nameReport + "'\n");
        Uses.logRep.logger.info("Генерация отчета: '" + nameReport + "'");
        final byte[] result;

        /*
         * Вот сама генерация отчета. 
         */
        result = generator.process(inputData);

        Uses.logRep.logger.info("Генерация завершено. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
        return result;
    }

    /**
     * Загрузим страничку ввода пароля и пользователя
     * @return страница в виде массива байт.
     */
    private static byte[] getLoginPage() {
        byte[] result = null;
        // Выдаем ресурс  "/ru/apertum/qsystem/reports/web/"
        final InputStream inStream = new ReportGenerator().getClass().getResourceAsStream("/ru/apertum/qsystem/reports/web/login.html");
        if (inStream != null) {
            try {
                result = Uses.readInputStream(inStream);
            } catch (IOException ex) {
                throw new Uses.ReportException("Ошибка чтения ресурса логирования. " + ex);
            }
        } else {
            final String s = "<html><head><meta http-equiv = \"Content-Type\" content = \"text/html; charset=windows-1251\" ></head><p align=center>Ресурс для входа не найден.</p></html>";
            return s.getBytes();
        }
        return new String(result).replaceFirst(Uses.ANCHOR_USERS_FOR_REPORT, WebServer.usrList).getBytes(); //"Cp1251"
    }

    private static boolean checkLogin(String inputData) {
        // в запросе должен быть пароль и пользователь, если нету, то отказ на вход
        final String data = Uses.getRequestData(inputData);
        // ресурс для выдачи в браузер. это либо список отчетов при корректном логининге или отказ на вход
        boolean res = false;
        String usr = "err";
        String pwd = "err";
        // разбирем параметры
        final String[] ss = data.split("&");
        if (ss.length == 2) {
            final String[] ss0 = ss[0].split("=");
            final String[] ss1 = ss[1].split("=");
            if ("username".equals(ss0[0]) && ("password".equals(ss1[0]))) {
                usr = ss0[1];
                pwd = (ss1.length == 1 ? "" : ss1[1]);

                if (pwd.equals(WebServer.passMap.get(usr))) {
                    res = true;
                }
            }
        }
        return res;
    }
}
