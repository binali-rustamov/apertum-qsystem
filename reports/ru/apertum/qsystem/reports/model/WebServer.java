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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import ru.apertum.qsystem.common.Uses;

/**
 * Отчетный сервер, выступающий в роли вэбсервера, обрабатывающего запросы на выдачу отчетов
 * Класс потоков, обрабатывающих запросы HTTP, для выдачи отчетов
 * @author Evgeniy Egorov
 */
public class WebServer extends Thread {

    /**
     * состояние вэбсервера
     */
    volatile private static boolean isActive = false;
    /**
     *  Сокет, принявший сообщение или запрос
     */
    private final Socket socket;
    /**
     * Поток вебсервера.
     */
    private static Thread webTread = null;
    /**
     * Сокет вебсервера.
     */
    private static ServerSocket reportSocket = null;
    /**
     * Часть html текста для reportList.html которая содержит список аналитических отчетов.
     */
    public static String repList = "";
    /**
     * Список паролей пользователей
     * имя -> пароль
     */
    public static HashMap<String, String> passMap = new HashMap<String, String>();
    /**
     * Часть html текста для login.html которая содержит список аналитических отчетов.
     */
    public static String usrList = "";

    /**
     * запуск вэбсервера
     * @param port На каком порту
     * @param reports С какими аналитическими отчетами.
     */
    synchronized public static void startWebServer(int port) {
        if (!isActive) {
            isActive = true;
        } else {
            return;
        }

        // привинтить сокет на локалхост, порт port
        Uses.log.logger.info("Отчетный сервер захватывает порт \"" + port + "\".");
        try {
            reportSocket = new ServerSocket(port, 0);
        } catch (Exception e) {
            throw new Uses.ReportException("Ошибка при создании серверного сокета для вэбсервера: " + e);
        }
        // поток вэбсервера, весит параллельно и обслуживает запросы
        webTread = new Thread() {

            public WebServer webServer;

            @Override
            public void run() {
                Uses.writeRus("Отчетный сервер системы 'Очередь' запущен.\n");
                Uses.logRep.logger.info("Отчетный вэбсервер системы 'Очередь' запущен.");
                while (isActive) {
                    // ждём нового подключения, после чего запускаем обработку клиента
                    // в новый вычислительный поток и увеличиваем счётчик на единичку
                    try {
                        webServer = new WebServer(reportSocket.accept());
                    } catch (IOException ex) {
                    }
                }
                Uses.logRep.logger.info("Отчетный вэбсервер системы 'Очередь' остановлен.");

            }
        };
        // и запускаем новый вычислительный поток (см. ф-ю run())
        webTread.setDaemon(true);
        webTread.setPriority(NORM_PRIORITY);
        webTread.start();

    }

    /**
     * Останов вэбсервера
     */
    synchronized public static void stopWebServer() {
        if (isActive) {
            isActive = false;
        }
        if (webTread != null) {
            if (reportSocket != null && !reportSocket.isClosed()) {
                try {
                    reportSocket.close();
                } catch (IOException ex) {
                    throw new Uses.ServerException("Ошибка закрытия сокета отчетного вебсервера." + ex);
                }
            }
            webTread.interrupt();
        }
    }

    /**
     * Конструктор потоков
     */
    public WebServer(Socket socket) {
        this.socket = socket;

        // и запускаем новый вычислительный поток (см. ф-ю run())
        setDaemon(true);
        setPriority(NORM_PRIORITY);
        start();
    }

    @Override
    public void run() {
        try {
            Uses.logRep.logger.debug("Старт потока обработки запроса.");

            // из сокета клиента берём поток входящих данных
            InputStream is;
            try {
                is = socket.getInputStream();
            } catch (IOException e) {
                throw new Uses.ReportException("Ошибка при получении входного потока: " + e.getStackTrace());
            }
            // и оттуда же - поток данных от сервера к клиенту
            OutputStream os;
            try {
                os = socket.getOutputStream();
            } catch (IOException e) {
                throw new Uses.ReportException("Ошибка при получении выходного потока: " + e.getStackTrace());
            }

            final String inputData;
            try {
                // подождать пока хоть что-то приползет из сети, но не более 10 сек.
                int i = 0;
                while (is.available() == 0 && i < 100) {
                    Thread.sleep(100);//бля
                    i++;
                }
                Thread.sleep(100);//бля
                inputData = new String(Uses.readInputStream(is));
            } catch (IOException ex) {
                throw new Uses.ServerException("Ошибка при чтении из входного потока: " + ex.getStackTrace());
            } catch (InterruptedException ex) {
                throw new Uses.ServerException("Проблема со сном: " + ex.getStackTrace());
            }
            Uses.logRep.logger.trace("Запрос:\n" + inputData);
            if ("".equals(inputData)) {
                Uses.logRep.logger.trace("Запрос пустой, завершаем выполнение запроса.");
                return;
            }
            // разберем полученный запрос от праузера исформируем соотв. map
            /* эт для примера
            "GET / HTTP/1.1
            Host: 127.0.0.1:8088
            User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.9.0.5) Gecko/2008120122 Firefox/3.0.5
            Accept: text/html,application/xhtml+xml,application/xml;q=0.9,* /*;q=0.8
            Accept-Language: ru,en-us;q=0.7,en;q=0.3
            Accept-Encoding: gzip,deflate
            Accept-Charset: windows-1251,utf-8;q=0.7,*;q=0.7
            Keep-Alive: 300
            Connection: keep-alive
            
            "
            
            "POST /statistic_period_services.html HTTP/1.1
            Accept: image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, 
            application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword,
            application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, 
            application/x-ms-application, * /*
            Referer: http://192.168.0.43:8088/statistic_period_services.html
            Accept-Language: ru
            Content-Type: application/x-www-form-urlencoded
            Accept-Encoding: gzip, deflate
            User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)
            Host: 192.168.0.43:8088
            Content-Length: 27
            Connection: Keep-Alive
            Cache-Control: no-cache
            
            sd=20.01.2009&ed=28.01.2009
            
            "              
             */

            //final String[] pears = inpunData.split("\r\n");
            //for (String line : pears) {
            //}

            byte[] result = null;

            // теперь определимся, генерить отчет или выдать ресурс
            // Отчет имеет определенное имя, и чтоб его сгенерить, надо передать его имя.
            // Если иседи имен отчетов нет такого, то , возможно, это ресурс. А если не ресурс, то попробуем выдать файл.
            // это нужно для того, что если совместно с html генеряться картинки, например графики,
            // то их сбросить на диск и потом с диска выдать по запросу броузера.
            // для этого нужно чтоб вебсервер умел выдовать и файлы с диска и файлы из временных папок.
            // генерируем отчет
            result = ReportGenerator.generate(inputData);
            if (result == null) {
                String subject = Uses.getRequestTarget(inputData);
                if ("".equals(subject)) {
                    subject = "login.html";
                }
                // Выдаем ресурс  "/ru/apertum/qsystem/reports/web/"
                final InputStream inStream = getClass().getResourceAsStream("/ru/apertum/qsystem/reports/web/" + subject);
                if (inStream == null) {
                    Uses.logRep.logger.warn("Ресурс не найден: \"" + subject + "\"");
                    // не в ресурсах, ищем в файлах
                    // в нормальных файлах
                    File anyFile = new File(subject);
                    if (anyFile.exists()) {
                        Uses.logRep.logger.info("Выдаем файл: \"" + subject + "\"");
                        final FileInputStream fInStream;
                        try {
                            fInStream = new FileInputStream(anyFile);
                        } catch (FileNotFoundException ex) {
                            throw new Uses.ReportException("Ошибка при чтении файла \"" + subject + "\"");
                        }
                        try {

                            result = Uses.readInputStream(fInStream);
                        } catch (IOException ex) {
                            throw new Uses.ReportException("Ошибка при чтении файла из потока \"" + subject + "\"");
                        }
                    } else {
                        // во временных файлах
                        anyFile = new File(Uses.TEMP_FOLDER + File.separator + "temphtml.html_files" + File.separator + subject);
                        if (anyFile.exists()) {
                            Uses.logRep.logger.info("Выдаем временный файл: \"" + Uses.TEMP_FOLDER + File.separator + "temphtml.html_files" + File.separator + subject + "\"");
                            final FileInputStream fInStream;
                            try {
                                fInStream = new FileInputStream(anyFile);
                            } catch (FileNotFoundException ex) {
                                throw new Uses.ReportException("Ошибка при чтении файла \"" + Uses.TEMP_FOLDER + File.separator + "temphtml.html_files" + File.separator + subject + "\"");
                            }
                            try {

                                result = Uses.readInputStream(fInStream);
                            } catch (IOException ex) {
                                throw new Uses.ReportException("Ошибка при чтении файла из потока \"" + Uses.TEMP_FOLDER + File.separator + "temphtml.html_files" + File.separator + subject + "\"");
                            }
                            anyFile.delete();
                        } else {
                            // ваще ничего нет. наверное битый адрес или ресурс пропал(не сформировался)
                            Uses.logRep.logger.error("Ресурс не найден во временных файлах: \"" + Uses.TEMP_FOLDER + File.separator + "temphtml.html_files" + File.separator + subject + "\"");
                            final String s = "<html><head><meta http-equiv = \"Content-Type\" content = \"text/html; charset=windows-1251\" ></head><p align=center>Ресурс не найден.</p></html>";
                            result = s.getBytes();
                        }
                    }

                } else {
                    Uses.logRep.logger.info("Выдаем ресурс: \"" + subject + "\"");
                    try {
                        result = Uses.readInputStream(inStream);
                        if ("login.html".equals(subject)) {
                            result = new String(result).replaceFirst(Uses.ANCHOR_USERS_FOR_REPORT, usrList).getBytes(); //"Cp1251"
                        }
                    } catch (IOException ex) {
                        throw new Uses.ReportException("Ошибка чтения ресурса. " + ex);
                    }
                }
            }

            // выводим данные:
            Uses.logRep.logger.trace("Выдаем результат " + result.length + " байт.");

            try {
                os.write(result);
                os.flush();
            } catch (IOException e) {
                throw new Uses.ReportException("Ошибка при записи в поток: " + e.getStackTrace());
            }
        } finally {
            // завершаем соединение
            try {
                //оборативаем close, т.к. он сам может сгенерировать ошибку IOExeption. Просто выкинем Стек-трейс
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
