/*
 *  Copyright (C) 2010 {Apertum}Projects. web: www.apertum.ru email: info@apertum.ru
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

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ru.apertum.qsystem.client.Locales;
import ru.apertum.qsystem.common.CodepagePrintStream;
import ru.apertum.qsystem.common.GsonPool;
import ru.apertum.qsystem.common.Mailer;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.cmd.JsonRPC20;
import ru.apertum.qsystem.common.cmd.RpcGetAdvanceCustomer;
import ru.apertum.qsystem.common.exceptions.ClientException;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.apertum.qsystem.common.model.ATalkingClock;
import ru.apertum.qsystem.common.model.QCustomer;
import ru.apertum.qsystem.reports.model.QReportsList;
import ru.apertum.qsystem.reports.model.WebServer;
import ru.apertum.qsystem.server.controller.Executer;
import ru.apertum.qsystem.server.http.JettyRunner;
import ru.apertum.qsystem.server.model.QPlanService;
import ru.apertum.qsystem.server.model.QService;
import ru.apertum.qsystem.server.model.QServiceTree;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.QUserList;
import ru.apertum.qsystem.server.model.postponed.QPostponedList;

/**
 * Класс старта и exit
 * инициализации сервера. Организация потоков выполнения заданий.
 * @author Evgeniy Egorov
 */
public class QServer extends Thread {

    private final Socket socket;
    private static final String KEY_HTML = "-HTTP";

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
            throw new ClientException("Проблемы с чтением версии. " + ex);
        }
        System.out.println("Server version: " + settings.getProperty("version") + "-community QSystem Server (GPL)");
        System.out.println("Database version: " + settings.getProperty("version_db") + " for MySQL 5.1-community Server (GPL)");
        System.out.println("Released : " + settings.getProperty("date"));

        System.out.println("Copyright (c) 2010-2012, Apertum Projects and/or its affiliates. All rights reserved.");
        System.out.println("This software comes with ABSOLUTELY NO WARRANTY. This is free software,");
        System.out.println("and you are welcome to modify and redistribute it under the GPL v3 license");
        System.out.println("Text of this license on your language located in the folder with the program.");

        System.out.println("Type 'exit' to stop work and close server.");
        System.out.println();


        System.out.println("Добро пожаловать на сервер QSystem. Для работы необходим MySQL5.1 или выше.");
        System.out.println("Версия сервера: " + settings.getProperty("version") + "-community QSystem Server (GPL)");
        System.out.println("Версия базы данных: " + settings.getProperty("version_db") + " for MySQL 5.1-community Server (GPL)");
        System.out.println("Дата выпуска : " + settings.getProperty("date"));
        System.out.println("Copyright (c) 2010, Apertum Projects. Все права защищены.");
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
        QLog.initial(args, true);

        WebServer.getInstance().startWebServer(ServerProps.getInstance().getProps().getWebServerPort());
        // запускаем движок индикации сообщения для кастомеров
        MainBoard.getInstance().showBoard();

        loadPool();

        if (!(Uses.format_HH_mm.format(ServerProps.getInstance().getProps().getStartTime()).equals(Uses.format_HH_mm.format(ServerProps.getInstance().getProps().getFinishTime())))) {
            /**
             * Таймер, по которому будем Очистка всех услуг и рассылка спама с дневным отчетом.
             */
            ATalkingClock clearServices = new ATalkingClock(Uses.DELAY_CHECK_TO_LOCK, 0) {

                @Override
                public void run() {
                    // это обнуление
                    if (Uses.format_HH_mm.format(new Date(new Date().getTime() + 10 * 60 * 1000)).equals(Uses.format_HH_mm.format(ServerProps.getInstance().getProps().getStartTime()))) {
                        QLog.l().logger().info("Очистка всех услуг.");
                        // почистим все услуги от трупов кастомеров с прошлого дня
                        QServer.clearAllQueue();
                    }

                    // это рассылка дневного отчета
                    if (("true".equalsIgnoreCase(Mailer.fetchConfig().getProperty("mailing")) || "1".equals(Mailer.fetchConfig().getProperty("mailing")))
                            && Uses.format_HH_mm.format(new Date(new Date().getTime() - 30 * 60 * 1000)).equals(Uses.format_HH_mm.format(ServerProps.getInstance().getProps().getFinishTime()))) {
                        QLog.l().logger().info("Рассылка дневного отчета.");
                        // почистим все услуги от трупов кастомеров с прошлого дня
                        for (QUser user : QUserList.getInstance().getItems()) {
                            if (user.getReportAccess()) {
                                final HashMap<String, String> p = new HashMap<>();
                                p.put("date", Uses.format_dd_MM_yyyy.format(new Date()));
                                final byte[] result = QReportsList.getInstance().generate(user, "/distribution_job_day.pdf", p);
                                try {
                                    try (FileOutputStream fos = new FileOutputStream("temp/distribution_job_day.pdf")) {
                                        fos.write(result);
                                        fos.flush();
                                    }
                                    Mailer.sendReporterMailAtFon("temp/distribution_job_day.pdf");
                                } catch (Exception ex) {
                                    QLog.l().logger().error("Какой-то облом с дневным отчетом", ex);
                                }
                                break;
                            }
                        }
                    }
                }
            };
            clearServices.start();
        }
        // посмотрим не нужно ли стартануть jetty
        // для этого нужно запускать с ключом http
        // если етсь ключ http, то запускаем сервер и принимаем на нем команды серверу суо
        for (int i = 0; i < args.length; i++) {
            if (KEY_HTML.equalsIgnoreCase(args[i]) && i != args.length - 1) {
                QLog.l().logger().info("Запустим Jetty.");
                try {
                    int port = Integer.parseInt(args[i + 1]);
                    JettyRunner.start(port);
                } catch (NumberFormatException ex) {
                    QLog.l().logger().error("Номер порта для Jetty в параметрах запуска не является числом. Формат параметра для порта 8081 '-http 8081'.", ex);
                }
                break;
            }
        }

        // привинтить сокет на локалхост, порт 3128
        final ServerSocket server;
        try {
            QLog.l().logger().info("Сервер системы захватывает порт \"" + ServerProps.getInstance().getProps().getServerPort() + "\".");
            server = new ServerSocket(ServerProps.getInstance().getProps().getServerPort());
        } catch (IOException e) {
            throw new ServerException("Ошибка при создании серверного сокета: " + e);
        } catch (Exception e) {
            throw new ServerException("Ошибка сети: " + e);
        }
        server.setSoTimeout(500);
        System.out.println("Server QSystem started.\n");
        QLog.l().logger().info("Сервер системы 'Очередь' запущен.");
        int pos = 0;
        boolean exit = false;
        // слушаем порт
        while (!exit) {
            // ждём нового подключения, после чего запускаем обработку клиента
            // в новый вычислительный поток и увеличиваем счётчик на единичку

            try {
                final QServer qServer = new QServer(server.accept());
                qServer.start();
                if (QLog.l().isDebug()) {
                    System.out.println();
                }
            } catch (SocketTimeoutException e) {
                // ничего страшного, гасим исключение стобы дать возможность отработать входному/выходному потоку
            } catch (Exception e) {
                throw new ServerException("Ошибка сети: " + e);
            }


            if (!QLog.l().isDebug()) {
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
                if (bytesAvailable == 5
                        && data[0] == 101
                        && data[1] == 120
                        && data[2] == 105
                        && data[3] == 116
                        && ((data[4] == 10) || (data[4] == 13))) {
                    // набрали команду "exit" и нажали ENTER
                    QLog.l().logger().info("Завершение работы сервера.");
                    exit = true;
                }
            }
        }// while

        QLog.l().logger().debug("Закрываем серверный сокет.");
        server.close();
        QLog.l().logger().debug("Останов Jetty.");
        JettyRunner.stop();
        QLog.l().logger().debug("Останов отчетного вэбсервера.");
        WebServer.getInstance().stopWebServer();
        QLog.l().logger().debug("Выключение центрального табло.");
        MainBoard.getInstance().close();

        deleteTempFile();
        Thread.sleep(1500);
        QLog.l().logger().info("Сервер штатно завершил работу. Время работы: " + Uses.roundAs(new Double(System.currentTimeMillis() - start) / 1000 / 60, 2) + " мин.");
        System.exit(0);
    }

    /**
     * @param socket
     */
    public QServer(Socket socket) {
        this.socket = socket;
        // и запускаем новый вычислительный поток (см. ф-ю run())
        setDaemon(true);
        setPriority(NORM_PRIORITY);
    }

    @Override
    public void run() {
        try {
            QLog.l().logger().debug("Старт потока приема задания.");

            // из сокета клиента берём поток входящих данных
            InputStream is;
            try {
                is = socket.getInputStream();
            } catch (IOException e) {
                throw new ServerException("Ошибка при получении входного потока: " + e.getStackTrace());
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
                while (is.available() != 0) {
                    sb = sb.append(new String(Uses.readInputStream(is)));
                    Thread.sleep(150);//бля
                }
                data = URLDecoder.decode(sb.toString(), "utf-8");
            } catch (IOException ex) {
                throw new ServerException("Ошибка при чтении из входного потока: " + ex);
            } catch (InterruptedException ex) {
                throw new ServerException("Проблема со сном: " + ex);
            } catch (IllegalArgumentException ex) {
                throw new ServerException("Ошибка декодирования сетевого сообщения: " + ex);
            }
            QLog.l().logger().trace("Задание:\n" + data);

            final String answer;
            final JsonRPC20 rpc;
            final Gson gson = GsonPool.getInstance().borrowGson();
            try {
                rpc = gson.fromJson(data, JsonRPC20.class);
                // полученное задание передаем в пул
                final Object result = Executer.getInstance().doTask(rpc, socket.getInetAddress().getHostAddress(), socket.getInetAddress().getAddress());
                answer = gson.toJson(result);
            } finally {
                GsonPool.getInstance().returnGson(gson);
            }

            // выводим данные:
            QLog.l().logger().trace("Ответ:\n" + answer);
            try {
                // Передача данных ответа
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.print(URLEncoder.encode(answer, "utf-8"));
                writer.flush();
            } catch (IOException e) {
                throw new ServerException("Ошибка при записи в поток: " + e.getStackTrace());
            }
        } catch (ServerException | JsonParseException ex) {
            final StringBuilder sb = new StringBuilder("\nStackTrace:\n");
            for (StackTraceElement bag : ex.getStackTrace()) {
                sb.append("    at ").append(bag.getClassName()).append(".").append(bag.getMethodName()).append("(").append(bag.getFileName()).append(":").append(bag.getLineNumber()).append(")\n");
            }
            final String err = sb.toString() + "\n";
            sb.setLength(0);
            throw new ServerException("Ошибка при выполнении задания.\n" + ex + err);
        } finally {
            // завершаем соединение
            try {
                //оборачиваем close, т.к. он сам может сгенерировать ошибку IOExeption. Просто выкинем Стек-трейс
                socket.close();
            } catch (IOException e) {
                QLog.l().logger().trace(e);
            }
            QLog.l().logger().trace("Ответ завершен");
        }
    }

    /**
     * Сохранение состояния пула услуг в xml-файл на диск
     */
    public static void savePool() {
        final long start = System.currentTimeMillis();
        final Lock saveLock = new ReentrantLock();
        saveLock.lock();
        try {
            QLog.l().logger().info("Сохранение состояния.");
            final LinkedList<QCustomer> backup = new LinkedList<>();// создаем список сохраняемых кастомеров

            for (QService service : QServiceTree.getInstance().getNodes()) {
                backup.addAll(service.getClients());
            }

            for (QUser user : QUserList.getInstance().getItems()) {
                if (user.getCustomer() != null) {
                    backup.add(user.getCustomer());
                }
            }
            // в темповый файл
            final FileOutputStream fos;
            try {
                (new File(Uses.TEMP_FOLDER)).mkdir();
                fos = new FileOutputStream(new File(Uses.TEMP_FOLDER + File.separator + Uses.TEMP_STATE_FILE));
            } catch (FileNotFoundException ex) {
                throw new ServerException("Не возможно создать временный файл состояния. " + ex.getMessage());
            }
            Gson gson = null;
            try {
                gson = GsonPool.getInstance().borrowGson();
                fos.write(gson.toJson(new TempList(backup, QPostponedList.getInstance().getPostponedCustomers())).getBytes("UTF-8"));
                fos.flush();
                fos.close();
            } catch (IOException ex) {
                throw new ServerException("Не возможно сохранить изменения в поток." + ex.getMessage());
            } finally {
                GsonPool.getInstance().returnGson(gson);
            }
        } finally {
            saveLock.unlock();
        }
        QLog.l().logger().info("Состояние сохранено. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
    }

    static class TempList {

        public TempList() {
        }

        public TempList(LinkedList<QCustomer> backup, LinkedList<QCustomer> postponed) {
            this.backup = backup;
            this.postponed = postponed;
        }
        @Expose
        @SerializedName("backup")
        LinkedList<QCustomer> backup;
        @Expose
        @SerializedName("postponed")
        LinkedList<QCustomer> postponed;
    }

    /**
     * Загрузка состояния пула услуг из временного json-файла
     */
    static public void loadPool() {
        final long start = System.currentTimeMillis();
        // если есть временный файлик сохранения состояния, то надо его загрузить.
        // все ошибки чтения и парсинга игнорить.
        QLog.l().logger().info("Пробуем восстановить состояние системы.");
        File recovFile = new File(Uses.TEMP_FOLDER + File.separator + Uses.TEMP_STATE_FILE);
        if (recovFile.exists()) {
            QLog.l().logger().warn("Восстановление состояние системы после вчерашнего... нештатного завершения работы сервера.");
            //восстанавливаем состояние


            final FileInputStream fis;
            try {
                fis = new FileInputStream(recovFile);
            } catch (FileNotFoundException ex) {
                throw new ServerException(ex);
            }
            final Scanner scan = new Scanner(fis, "utf8");
            boolean flag = true;
            String rec_data = "";
            while (scan.hasNextLine()) {
                rec_data += scan.nextLine();
            }
            try {
                fis.close();
            } catch (IOException ex) {
                throw new ServerException(ex);
            }



            final TempList recList;
            final Gson gson = GsonPool.getInstance().borrowGson();
            final RpcGetAdvanceCustomer rpc;
            try {
                recList = gson.fromJson(rec_data, TempList.class);
            } catch (JsonParseException ex) {
                throw new ServerException("Не возможно интерпритировать сохраненные данные.\n" + ex.toString());
            } finally {
                GsonPool.getInstance().returnGson(gson);
            }



            try {
                QPostponedList.getInstance().loadPostponedList(recList.postponed);
                for (QCustomer recCustomer : recList.backup) {
                    // в эту очередь он был
                    final QService service = QServiceTree.getInstance().getById(recCustomer.getService().getId());
                    service.setCountPerDay(recCustomer.getService().getCountPerDay());
                    service.setDay(recCustomer.getService().getDay());
                    // так зовут юзера его обрабатываюшего
                    final QUser user = recCustomer.getUser();
                    // кастомер ща стоит к этой услуге к какой стоит
                    recCustomer.setService(service);
                    // смотрим к чему привязан кастомер. либо в очереди стоит, либо у юзера обрабатыватся
                    if (user == null) {
                        // сохраненный кастомер стоял в очереди и ждал, но его еще никто не звал
                        QServiceTree.getInstance().getById(recCustomer.getService().getId()).addCustomer(recCustomer);
                        QLog.l().logger().debug("Добавили клиента \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" к услуге \"" + recCustomer.getService().getName() + "\"");
                    } else {
                        // сохраненный кастомер обрабатывался юзером с именем userId
                        QUserList.getInstance().getById(user.getId()).setCustomer(recCustomer);
                        recCustomer.setUser(QUserList.getInstance().getById(user.getId()));
                        QLog.l().logger().debug("Добавили клиента \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" к юзеру \"" + user.getName() + "\"");
                    }
                }
            } catch (ServerException ex) {
                System.err.println("Востановление состояния сервера после изменения конфигурации. " + ex);
                clearAllQueue();
                QLog.l().logger().error("Востановление состояния сервера после изменения конфигурации. Для выключения сервера используйте команду exit. ", ex);
            }
        }
        QLog.l().logger().info("Восстановление состояния системы завершено. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
    }

    static public void clearAllQueue() {
        // почистим все услуги от трупов кастомеров
        for (QService service : QServiceTree.getInstance().getNodes()) {
            service.clearNextNumber();
            QService.clearNextStNumber();
            service.freeCustomers();
        }

        QPostponedList.getInstance().clear();
        MainBoard.getInstance().clear();


        // Сотрем временные файлы
        deleteTempFile();
        QLog.l().logger().info("Очистка всех пользователей от привязанных кастомеров.");
        for (QUser user : QUserList.getInstance().getItems()) {
            user.setCustomer(null);
            for (QPlanService plan : user.getPlanServices()) {
                plan.setAvg_wait(0);
                plan.setAvg_work(0);
                plan.setKilled(0);
                plan.setWorked(0);
            }
        }
    }

    public static void deleteTempFile() {
        File file = new File(Uses.TEMP_FOLDER + File.separator + Uses.TEMP_STATE_FILE);
        if (file.exists()) {
            file.delete();
        }
        file = new File(Uses.TEMP_FOLDER + File.separator + Uses.TEMP_STATATISTIC_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}
