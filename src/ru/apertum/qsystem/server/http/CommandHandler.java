/*
 * Copyright (C) 2010 {Apertum}Projects. web: www.apertum.ru email: info@apertum.ru
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.apertum.qsystem.server.http;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import ru.apertum.qsystem.client.forms.FAbout;
import ru.apertum.qsystem.common.GsonPool;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.cmd.JsonRPC20;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.apertum.qsystem.server.ServerProps;
import ru.apertum.qsystem.server.controller.Executer;

/**
 *
 * @author Evgeniy Egorov
 */
public class CommandHandler extends AbstractHandler {

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        final String result;
        final int status;
        switch (target) {
            case "/qsystem/ws":
                // этот урл нужно учесть, что бы запрос в jetty дальше пробросился по хандлерам.
                return;
            case "/qsystem/info":
                QLog.l().logger().trace("HTTP Задание: /qsystem/info");
                final Properties settings = new Properties();
                //"/ru/apertum/qsystem/reports/web/"
                final InputStream inStream = this.getClass().getResourceAsStream("/ru/apertum/qsystem/common/version.properties");

                try {
                    settings.load(inStream);
                } catch (IOException ex) {
                    throw new ServerException("Проблемы с чтением версии. " + ex);
                }
                result = "<h1>QSystem<hr><br>Welcome to server QSystem!<br><br>Добро пожаловать на сервер QSystem!</h1><br>"
                        + FAbout.getLocaleMessage("about.version") + " : " + settings.getProperty(FAbout.VERSION)
                        + "<br>"
                        + FAbout.getLocaleMessage("about.db_version") + " : " + ServerProps.getInstance().getProps().getVersion()
                        + "<br>"
                        + FAbout.getLocaleMessage("about.data") + " : " + settings.getProperty(FAbout.DATE);
                status = HttpServletResponse.SC_OK;
                break;
            case "/qsystem/command":
                final String data;
                try {
                    data = new String(Uses.readInputStream(request.getInputStream()));
                } catch (IOException ex) {
                    status = HttpServletResponse.SC_BAD_REQUEST;
                    result = "<h1>Ошибка чтения входных данных по http.</h1>";
                    QLog.l().logger().error("Ошибка чтения входных данных по http.", ex);
                    break;
                    //throw new ServerException("Ошибка чтения входных данных по http. ", ex);
                }
                if (data == null || data.isEmpty()) {
                    status = HttpServletResponse.SC_BAD_REQUEST;
                    result = "<h1>Не получен текст коменды по http.</h1>";
                    QLog.l().logger().error("Не получен текст коменды по http..");
                    break;
                }
                QLog.l().logger().trace("HTTP Задание:\n" + data);

                String answer;
                final JsonRPC20 rpc;
                final Gson gson = GsonPool.getInstance().borrowGson();
                boolean f = false;
                try {
                    rpc = gson.fromJson(data, JsonRPC20.class);
                    // полученное задание передаем в пул
                    final Object res = Executer.getInstance().doTask(rpc, request.getRemoteAddr(), request.getRemoteAddr().getBytes());
                    answer = gson.toJson(res);

                } catch (Exception ex) {
                    answer = "Произошла ошибка обработки задания. " + ex;
                    f = true;
                    QLog.l().logger().error("Произошла ошибка обработки задания. ", ex);
                } finally {
                    GsonPool.getInstance().returnGson(gson);
                }
                result = answer;
                // выводим данные:
                QLog.l().logger().trace("HTTP Ответ:\n" + answer);
                status = f ? HttpServletResponse.SC_INTERNAL_SERVER_ERROR : HttpServletResponse.SC_OK;
                break;
            default:
                status = HttpServletResponse.SC_OK;
                result = "<h1>QSystem<hr><br><br>URL не поддерживается // URL not supply</h1>";
        }
        //System.out.println(status + "/n" + result);
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(status);
        //response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        try {
            response.getWriter().println(result);
        } catch (IOException ex) {
            throw new ServerException("Накрылась сборка ответа от сервера по HTTP.", ex);
        }
    }
}
