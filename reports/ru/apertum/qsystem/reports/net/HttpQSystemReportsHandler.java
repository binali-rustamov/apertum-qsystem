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
package ru.apertum.qsystem.reports.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.reports.common.Response;
import ru.apertum.qsystem.reports.model.ReportGenerator;
import ru.apertum.qsystem.reports.model.WebServer;

/**
 *
 * @author Evgeniy Egorov
 */
public class HttpQSystemReportsHandler implements HttpRequestHandler {

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        final String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }
        // пытаемся сгенерировать отчет
        Response result = ReportGenerator.generate(request);
        // действуем по результатам генерации
        if (result == null) {

            String subject = NetUtil.getUrl(request);
            if ("/".equals(subject)) {
                subject = "/login.html";
            }
            int dot = subject.lastIndexOf(".");
            final String ext = subject.substring(dot + 1);
            String contentType = "; charset=UTF-8";
            if ("htm".equals(ext) || "html".equals(ext)) {
                contentType = "text/" + ext + contentType;
            } else {
                if ("pdf".equals(ext) || "rtf".equals(ext) || "doc".equals(ext)) {
                    contentType = "application/" + ext + contentType;
                } else {
                    if ("gif".equals(ext) || "jpeg".equals(ext) || "jpg".equals(ext) || "ico".equals(ext) || "xpm".equals(ext)) {
                        contentType = "image/" + ext + contentType;
                    }
                }
            }

            // Выдаем ресурс  "/ru/apertum/qsystem/reports/web/"
            final InputStream inStream = getClass().getResourceAsStream("/ru/apertum/qsystem/reports/web" + subject);
            if (inStream == null) {
                Uses.logRep.logger.warn("Ресурс не найден: \"/ru/apertum/qsystem/reports/web" + subject + "\"");
                // не в ресурсах, ищем в файлах
                // в нормальных файлах
                subject = subject.substring(1);
                File anyFile = new File(subject);
                if (anyFile.exists()) {
                    Uses.logRep.logger.info("Выдаем файл: \"" + subject + "\"");
                    FileInputStream fInStream = null;
                    try {
                        fInStream = new FileInputStream(anyFile);
                    } catch (FileNotFoundException ex) {
                        final String err = "Ошибка при чтении файла \"" + subject + "\" ";
                        Uses.logRep.logger.error("err " + ex);
                        result = new Response((err + ex).getBytes(), contentType);
                        response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    }
                    try {
                        result = new Response(Uses.readInputStream(fInStream), contentType);
                        response.setStatusCode(HttpStatus.SC_OK);
                    } catch (IOException ex) {
                        final String err = "Ошибка при чтении файла из потока \"" + subject + "\" ";
                        Uses.logRep.logger.error("err " + ex);
                        result = new Response((err + ex).getBytes(), contentType);
                        response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    }
                } else {
                    // во временных файлах
                    anyFile = new File(Uses.TEMP_FOLDER + File.separator + "temphtml.html_files" + File.separator + subject);
                    if (anyFile.exists()) {
                        Uses.logRep.logger.info("Выдаем временный файл: \"" + Uses.TEMP_FOLDER + File.separator + "temphtml.html_files" + File.separator + subject + "\"");
                        FileInputStream fInStream = null;
                        try {
                            fInStream = new FileInputStream(anyFile);
                        } catch (FileNotFoundException ex) {
                            final String err = "Ошибка при чтении файла \"" + Uses.TEMP_FOLDER + File.separator + "temphtml.html_files" + File.separator + subject + "\" ";
                            Uses.logRep.logger.error("err " + ex);
                            result = new Response((err + ex).getBytes(), contentType);
                            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        }
                        try {
                            result = new Response(Uses.readInputStream(fInStream), contentType);
                            response.setStatusCode(HttpStatus.SC_OK);
                        } catch (IOException ex) {
                            final String err = "Ошибка при чтении файла из потока \"" + Uses.TEMP_FOLDER + File.separator + "temphtml.html_files" + File.separator + subject + "\" ";
                            Uses.logRep.logger.error("err " + ex);
                            result = new Response((err + ex).getBytes(), contentType);
                            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        }
                        anyFile.delete();
                    } else {
                        // ваще ничего нет. наверное битый адрес или ресурс пропал(не сформировался)
                        Uses.logRep.logger.error("Ресурс не найден во временных файлах: \"" + Uses.TEMP_FOLDER + File.separator + "temphtml.html_files" + File.separator + subject + "\"");
                        final String s = "<html><head><meta http-equiv = \"Content-Type\" content = \"text/html; charset=utf-8\" ></head><p align=center>Ресурс не найден.</p></html>";
                        result = new Response(s.getBytes());
                    }
                }

            } else {
                Uses.logRep.logger.info("Выдаем ресурс: \"" + subject + "\"");
                try {
                    result = new Response(Uses.readInputStream(inStream));
                    if ("/login.html".equals(subject)) {
                        result.setData(new String(result.getData()).replaceFirst(Uses.ANCHOR_USERS_FOR_REPORT, WebServer.usrList).getBytes()); //"Cp1251"
                    }
                } catch (IOException ex) {
                    Uses.logRep.logger.error("Ошибка чтения ресурса. " + ex);
                    result = new Response(("Ошибка чтения ресурса. " + ex).getBytes());
                    response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                }
                response.setStatusCode(HttpStatus.SC_OK);
            }
        }

        // выводим данные:
        Uses.logRep.logger.trace("Выдаем результат " + result.getData().length + " байт на запрос \"" + request.getRequestLine().getUri() + "\".");

        final byte[] result2 = result.getData();
        final EntityTemplate body = new EntityTemplate(new ContentProducer() {

            @Override
            public void writeTo(final OutputStream outstream) throws IOException {
                outstream.write(result2);
                outstream.flush();
            }
        });

        body.setContentType(result.getContentType());
        response.setEntity(body);
        NetUtil.freeEntityContent(request);
    }
}
