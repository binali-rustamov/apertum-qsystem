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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.exceptions.ReportException;
import ru.apertum.qsystem.reports.net.RunnableSocket;

/**
 * Отчетный сервер, выступающий в роли вэбсервера, обрабатывающего запросы на выдачу отчетов
 * Класс потоков, обрабатывающих запросы HTTP, для выдачи отчетов
 * @author Evgeniy Egorov
 */
public class WebServer /*extends Thread*/ {

    /**
     * состояние вэбсервера
     */
    volatile private static boolean isActive = false;
    /**
     *  Сокет, принявший сообщение или запрос
     */
    ///private final Socket socket;
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
            throw new ReportException("Ошибка при создании серверного сокета для вэбсервера: " + e);
        }
        // поток вэбсервера, весит параллельно и обслуживает запросы
        webTread = new Thread() {

            public WebServer webServer;

            @Override
            public void run() {
                System.out.println("Report server for QSystem started.");
                Uses.logRep.logger.info("Отчетный вэбсервер системы 'Очередь' запущен.");
                try {
                    reportSocket.setSoTimeout(1000);
                } catch (SocketException ex) {
                }
                while (isActive && !webTread.isInterrupted()) {
                    // ждём нового подключения, после чего запускаем обработку клиента
                    // в новый вычислительный поток и увеличиваем счётчик на единичку
                    final Socket socket;
                    try {
                        socket = reportSocket.accept();
                        final RunnableSocket rs = new RunnableSocket();
                        rs.setSocket(socket);
                        final Thread thread = new Thread(rs);
                        thread.start();
                    } catch (SocketTimeoutException ex) {
                    } catch (IOException ex) {
                        throw new ReportException("Ошибка при работе сокета для вэбсервера: " + ex);
                    }
                }
                try {
                    if (reportSocket != null && !reportSocket.isClosed()) {
                        reportSocket.close();
                    }
                } catch (IOException ex) {
                }
                Uses.logRep.logger.info("Отчетный вэбсервер системы 'Очередь' остановлен.");

            }
        };
        // и запускаем новый вычислительный поток (см. ф-ю run())
        webTread.setDaemon(true);
        webTread.setPriority(Thread.NORM_PRIORITY);
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
            webTread.interrupt();
        }
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
        }
    }
}
