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
import java.util.HashMap;
import org.apache.http.HttpRequest;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.reports.common.Response;
import ru.apertum.qsystem.reports.generators.RepCurrentUsers;
import ru.apertum.qsystem.reports.generators.ReportCurrentServices;
import ru.apertum.qsystem.reports.generators.ReportsList;
import ru.apertum.qsystem.reports.net.NetUtil;
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
    private final static IGenerator getReportsList = new ReportsList("reportList", "");
    /**
     * Отчет по текущему состоянию в разрее услуг
     */
    private final static IGenerator getReportCurrentServices = new ReportCurrentServices(Uses.REPORT_CURRENT_SERVICES.toLowerCase(), "/ru/apertum/qsystem/reports/templates/currentStateServices.jasper");
    /**
     * Отчет по текущему состоянию в разрезе пользователей
     */
    private final static IGenerator getRepCurrentUsers = new RepCurrentUsers(Uses.REPORT_CURRENT_USERS.toLowerCase(), "/ru/apertum/qsystem/reports/templates/currentStateUsers.jasper");

    /**
     * Генерация отчета по его имени.
     * @param request запрос пришедший от клиента
     * @return Отчет в виде массива байт.
     */
    public static synchronized Response generate(HttpRequest request) {
        final long start = System.currentTimeMillis();
        String url = NetUtil.getUrl(request);
        final String nameReport = url.lastIndexOf(".") == -1 ? url.substring(1) : url.substring(1, url.lastIndexOf("."));

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
        if (!"/reportList.html".equals(url)) {

            if (request.getFirstHeader("Cookie") == null) {
                // если куков нет
                return getLoginPage();
            }
            final HashMap<String, String> cookie = NetUtil.getCookie(request.getFirstHeader("Cookie").getValue(), "; ");
            final String pass = cookie.get("password");
            final String usr = cookie.get("username");
            if (pass == null || usr == null) {
                // если не нашлось в куках
                return getLoginPage();
            }
            if (!pass.equals(WebServer.passMap.get(usr))) {
                // если не совпали пароли
                return getLoginPage();
            }
        }
        System.out.println("Report build: '" + nameReport + "'\n");
        Uses.logRep.logger.info("Генерация отчета: '" + nameReport + "'");
        /*
         * Вот сама генерация отчета. 
         */
        final Response result = generator.process(request);

        Uses.logRep.logger.info("Генерация завершено. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
        return result;
    }

    /**
     * Загрузим страничку ввода пароля и пользователя
     * @return страница в виде массива байт.
     */
    private static Response getLoginPage() {
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
            return new Response(s.getBytes());
        }
        Response res = null;
        try {
            res = new Response(new String(result, "UTF-8").replaceFirst(Uses.ANCHOR_USERS_FOR_REPORT, WebServer.usrList).getBytes("UTF-8")); //"Cp1251"
        } catch (UnsupportedEncodingException ex) {
        }
        return res;
    }

    private static boolean checkLogin(HttpRequest request) {
        boolean res = false;
        // в запросе должен быть пароль и пользователь, если нету, то отказ на вход
        String entityContent = NetUtil.getEntityContent(request);
        Uses.log.logger.trace("Принятые параметры \"" + entityContent + "\".");
        // ресурс для выдачи в браузер. это либо список отчетов при корректном логининге или отказ на вход
        // разбирем параметры
        final HashMap<String, String> cookie = NetUtil.getCookie(entityContent, "&");
        if (cookie.containsKey("username") && cookie.containsKey("password")) {
            if (cookie.get("password").equals(WebServer.passMap.get(cookie.get("username")))) {
                res = true;
            }
        }
        return res;
    }
}
