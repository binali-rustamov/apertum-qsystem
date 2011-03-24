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
package ru.apertum.qsystem.server;

import java.io.*;
import java.net.*;
import java.util.Locale;
import java.util.Properties;
import ru.apertum.qsystem.client.Locales;
import ru.apertum.qsystem.common.CodepagePrintStream;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.reports.model.CurrentStatistic;
import ru.apertum.qsystem.reports.model.WebServer;
import ru.apertum.qsystem.server.controller.QServicesPool;

/**
 * Класс старта и exit
 * инициализации сервера. Организация потоков выполнения заданий.
 * @author Evgeniy Egorov
 */
public class QServer extends Thread {

    private final Socket socket;
    private final QServicesPool managersPool;

    /**
     * @param args - первым параметром передается полное имя настроечного XML-файла
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locales.getInstance().getLangCurrent());

        //Установка вывода консольных сообщений в нужной кодировке
        if ("\\".equals(File.separator)) {
            try {
                String consoleEnc = System.getProperty("console.encoding", "Cp866");
                System.setOut(new CodepagePrintStream(System.out, consoleEnc));
                System.setErr(new CodepagePrintStream(System.err, consoleEnc));
            } catch (UnsupportedEncodingException e) {
                System.out.println("Unable to setup console codepage: " + e);
            }
        }


        System.out.println("Welcome to the QSystem server. Your MySQL mast be prepared.");
        final Properties settings = new Properties();
        final InputStream inStream = settings.getClass().getResourceAsStream("/ru/apertum/qsystem/common/version.properties");
        try {
            settings.load(inStream);
        } catch (IOException ex) {
            throw new Uses.ClientException("Проблемы с чтением версии. " + ex);
        }
        System.out.println("Server version: " + settings.getProperty("version") + "-community QSystem Server (GPL)");
        System.out.println("Database version: " + settings.getProperty("version_db") + " for MySQL 5.1-community Server (GPL)");
        System.out.println("Released : " + settings.getProperty("date"));

        System.out.println("Copyright (c) 2010, Apertum project and/or its affiliates. All rights reserved.");
        System.out.println("This software comes with ABSOLUTELY NO WARRANTY. This is free software,");
        System.out.println("and you are welcome to modify and redistribute it under the GPL v3 license");
        System.out.println("Text of this license on your language located in the folder with the program.");

        System.out.println("Type 'exit' to stop work and close server.");
        System.out.println();


        System.out.println("Добро пожаловать на сервер QSystem. Для работы необходим MySQL5.1 или выше.");
        System.out.println("Версия сервера: " + settings.getProperty("version") + "-community QSystem Server (GPL)");
        System.out.println("Версия базы данных: " + settings.getProperty("version_db") + " for MySQL 5.1-community Server (GPL)");
        System.out.println("Дата выпуска : " + settings.getProperty("date"));
        System.out.println("Copyright (c) 2010, Проект Apertum. Все права защищены.");
        System.out.println("QSystem является свободным программным обеспечением, вы можете");
        System.out.println("распространять и/или изменять его согласно условиям Стандартной Общественной");
        System.out.println("Лицензии GNU (GNU GPL), опубликованной Фондом свободного программного");
        System.out.println("обеспечения (FSF), либо Лицензии версии 3, либо более поздней версии.");

        System.out.println("Вы должны были получить копию Стандартной Общественной Лицензии GNU вместе");
        System.out.println("с этой программой. Если это не так, напишите в Фонд Свободного ПО ");
        System.out.println("(Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA)");

        System.out.println("Набирите 'exit' чтобы штатно остановить работу сервера.");
        System.out.println();



        final long start = System.currentTimeMillis();
        Uses.isDebug = Uses.setLogining(args, true);
        Uses.setServerContext();



        /* remander 
         * // Данные в кодировке КОИ-8
        byte[] koi8Data = ...;
        // Преобразуем из КОИ-8 в Unicode
        String string = new String(koi8Data,"KOI8_R");
        // Преобразуем из Unicode в Windows-1251
        byte[] winData = string.getBytes("Cp1251");
         */

        QServicesPool pool = null;
        while (restart) {
            CurrentStatistic.closeCurrentStatistic();

            // класс управления системой
            pool = QServicesPool.recreateServicesPool(false);


            // привинтить сокет на локалхост, порт 3128
            final ServerSocket server;
            try {
                Uses.log.logger.info("Сервер системы захватывает порт \"" + pool.getNetPropetry().getServerPort() + "\".");
                server = new ServerSocket(pool.getNetPropetry().getServerPort());
            } catch (IOException e) {
                throw new Uses.ServerException("Ошибка при создании серверного сокета: " + e);
            } catch (Exception e) {
                throw new Uses.ServerException("Ошибка сети: " + e);
            }
            server.setSoTimeout(500);
            System.out.println("Server QSystem started.\n");
            Uses.log.logger.info("Сервер системы 'Очередь' запущен.");
            int pos = 0;
            boolean exit = false;
            restart = false;
            // слушаем порт
            while (!exit && !restart) {
                // ждём нового подключения, после чего запускаем обработку клиента
                // в новый вычислительный поток и увеличиваем счётчик на единичку

                try {
                    final QServer qServer = new QServer(server.accept(), pool);
                    if (Uses.isDebug) {
                        System.out.println();
                    }
                } catch (SocketTimeoutException e) {
                    // ничего страшного, гасим исключение стобы дать возможность отработать входному/выходному потоку
                } catch (Exception e) {
                    throw new Uses.ServerException("Ошибка сети: " + e);
                }


                if (!Uses.isDebug) {
                    final char ch = '*';
                    String progres = "Process: " + ch;
                    final int len = 5;
                    for (int i = 0; i < pos; i++) {
                        progres = progres + ch;
                    }
                    for (int i = 0; i < len; i++) {
                        progres = progres + ' ';
                    }
                    if (++pos == len) {
                        pos = 0;
                    }
                    System.out.print(progres);
                    System.out.write(13);// '\b' - возвращает корретку на одну позицию назад

                }

                // Попробуем считать нажатую клавишу
                // если нажади ENTER, то завершаем работу сервера
                // и затираем файл временного состояния Uses.TEMP_STATE_FILE
                //BufferedReader r = new BufferedReader(new StreamReader(System.in));

                int bytesAvailable = System.in.available();
                if (bytesAvailable > 0) {
                    byte[] data = new byte[bytesAvailable];
                    System.in.read(data);
                    //for (int i = 0; i < bytesAvailable; i++) {
                    //    System.out.println(data[i]);
                    //}
                    if (bytesAvailable == 5
                            && data[0] == 101
                            && data[1] == 120
                            && data[2] == 105
                            && data[3] == 116
                            && ((data[4] == 10) || (data[4] == 13))) {
                        // набрали команду "exit" и нажали ENTER
                        Uses.log.logger.info("Завершение работы сервера.");
                        exit = true;
                    }
                    if (bytesAvailable == 8
                            && data[0] == 114
                            && data[1] == 101
                            && data[2] == 115
                            && data[3] == 116
                            && data[4] == 97
                            && data[5] == 114
                            && data[6] == 116
                            && ((data[7] == 10) || (data[7] == 13))) {
                        // набрали команду "restart" и нажали ENTER
                        Uses.log.logger.info("Рестарт сервера.");
                        restart = true;
                    }
                }
            }// while

            Uses.log.logger.debug("Закрываем серверный сокет.");
            server.close();
            Uses.log.logger.debug("Останов отчетного вэбсервера.");
            WebServer.stopWebServer();
            Uses.log.logger.debug("Выключение центрального табло.");
            pool.getIndicatorBoard().close();
        }// конец блока рестарта
        Uses.deleteTempFile();
        Thread.sleep(1500);
        Uses.log.logger.info("Сервер штатно завершил работу. Время работы: " + Uses.roundAs(new Double(System.currentTimeMillis() - start) / 1000 / 60, 2) + " мин.");
        System.exit(0);
    }
    /**
     * Признак необходимости рестарта сервера. Меняется из консоли и по сети.
     */
    private static boolean restart = true;

    /**
     * @param socket
     * @param managersPool
     * 
     */
    public QServer(Socket socket, QServicesPool managersPool) {
        this.socket = socket;
        this.managersPool = managersPool;

        // и запускаем новый вычислительный поток (см. ф-ю run())
        setDaemon(true);
        setPriority(NORM_PRIORITY);
        start();
    }

    @Override
    public void run() {
        try {
            Uses.log.logger.debug("Старт потока приема задания.");

            // из сокета клиента берём поток входящих данных
            InputStream is;
            try {
                is = socket.getInputStream();
            } catch (IOException e) {
                throw new Uses.ServerException("Ошибка при получении входного потока: " + e.getStackTrace());
            }

            final String data;
            try {
                // подождать пока хоть что-то приползет из сети, но не более 10 сек.
                int i = 0;
                while (is.available() == 0 && i < 100) {
                    Thread.sleep(100);//бля
                    i++;
                }

                StringBuilder sb = new StringBuilder(new String(Uses.readInputStream(is)));
                Thread.sleep(250);//бля
                while (is.available() != 0) {
                    sb = sb.append(new String(Uses.readInputStream(is)));
                    Thread.sleep(250);//бля
                }
                data = URLDecoder.decode(sb.toString(), "utf-8");
            } catch (IOException ex) {
                throw new Uses.ServerException("Ошибка при чтении из входного потока: " + ex);
            } catch (InterruptedException ex) {
                throw new Uses.ServerException("Проблема со сном: " + ex);
            } catch (IllegalArgumentException ex) {
                throw new Uses.ServerException("Ошибка декодирования сетевого сообщения: " + ex);
            }
            Uses.log.logger.trace("Задание:\n" + data);
            // полученное задание передаем в пул, если это не признак жизни
            final String answer;

            // Проверка на задание для рестарта
            if (Uses.TASK_RESTART.equals(data)) {
                Uses.log.logger.debug("Пришла команда на рестарт сервера");
                restart = true;
                answer = "<Ответ>\n</Ответ>";
            } else {
                answer = managersPool.doTask(data, socket.getInetAddress().getHostAddress(), socket.getInetAddress().getAddress());
            }

            // выводим данные:
            Uses.log.logger.trace("Ответ:\n" + answer);
            try {
                // Передача данных ответа
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.print(URLEncoder.encode(answer, "utf-8"));
                writer.flush();
            } catch (IOException e) {
                throw new Uses.ServerException("Ошибка при записи в поток: " + e.getStackTrace());
            }
        } catch (Exception ex) {
            final StringBuilder sb = new StringBuilder("\nStackTrace:\n");
            for (StackTraceElement bag : ex.getStackTrace()) {
                sb.append("    at ").append(bag.getClassName()).append(".").append(bag.getMethodName()).append("(").append(bag.getFileName()).append(":").append(bag.getLineNumber()).append(")\n");
            }
            final String err = sb.toString() + "\n";
            sb.setLength(0);
            throw new Uses.ServerException("Ошибка при выполнении задания.\n" + ex + err);
        } finally {
            // завершаем соединение
            try {
                //оборачиваем close, т.к. он сам может сгенерировать ошибку IOExeption. Просто выкинем Стек-трейс
                socket.close();
            } catch (IOException e) {
                Uses.log.logger.trace(e);
            }
            Uses.log.logger.trace("Ответ завершен");
        }
    }
}
