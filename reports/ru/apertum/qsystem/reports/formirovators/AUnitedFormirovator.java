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
package ru.apertum.qsystem.reports.formirovators;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import org.apache.http.HttpRequest;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.reports.model.ReportGenerator;
import ru.apertum.qsystem.server.model.QSite;

/**
 * Базовый класс для формироваторов консолидированных отчетов.
 * Умеет опрашивать все сайты домена на предмет получения части общего отчета.
 * @author Evgeniy Egorov
 */
public abstract class AUnitedFormirovator extends AFormirovator {

    /**
     * Формирование части консолидированного отчета на сайте.
     * @param driverClassName имя драйвера используемого для подключения к СУБД
     * @param password пароль с которым пользователь соединяется с базой
     * @param url использыемай база в СУБД
     * @param request строка пришедшая от браузера
     * @param username пользователь СУБД
     * @return Часть консолидированного отчета в XML-виде. Имя корневого элементо произвольное.
     */
    public abstract Element getSiteReport(String driverClassName, String url, String username, String password, HttpRequest request);

    /**
     * То же самое что и preparation(), только для консоледированных.
     * @param driverClassName имя драйвера используемого для подключения к СУБД
     * @param password пароль с которым пользователь соединяется с базой
     * @param url использыемай база в СУБД
     * @param request строка пришедшая от браузера
     * @param username пользователь СУБД
     * @return подготовительные данные, например страничка с формой ввода каких-то данных.
     */
    public abstract byte[] unitedPreparation(String driverClassName, String url, String username, String password, HttpRequest request);

    /**
     * Разруливает все вопросы по подготовке данных для консолидированных отчетов.
     * @param driverClassName имя драйвера используемого для подключения к СУБД
     * @param request строка пришедшая от браузера
     * @param password пароль с которым пользователь соединяется с базой
     * @param url использыемай база в СУБД
     * @param username пользователь СУБД
     * @return результат подготовки данных на сайте. Для суперсайта это может быть страничка с формой ввода, для простого
     * сайта это часть консолидированного отчета в XML-виде.
     /
    @Override
    final public byte[] preparation(String driverClassName, String url, String username, String password, HttpRequest request) {
        final byte[] prepare = unitedPreparation(driverClassName, url, username, password, request);
        if (prepare != null) {
            return prepare;
        } else {
            if (request.getRequestLine().getUri().indexOf(Uses.TASK_SUPER_REQUEST) == -1) {
                return null;
            } else {
                return getSiteReport(driverClassName, url, username, password, request).asXML().getBytes();
            }
        }
    }
*/
    @Override
    final public JRDataSource getDataSource(String driverClassName, String url, String username, String password, HttpRequest request) {
        Uses.log.logger.debug("Сбор частей отчетов со всех сайтов.");
        // В строку HTTP-запроса вставить "Super: xxx"
        final String superInputData = request.getRequestLine().getUri().replaceFirst("Host: ", Uses.TASK_SUPER_REQUEST + "\r\n" + "Host: ");
        // Разослать всем сайтам этот запрос и получить от них ответы
        if (!ReportGenerator.isSuperSite()) {
            throw new Uses.ReportException("Попытка обращения к консолидированному отчету не через суперсайт.");
        }
        // вот тут надо разослать всем сайтам и собрать общий ответ
        // все задания на сайты шлем в разных патоках и синхронизируемокончание их выполнения

        // Создадим барьер по числу сайтов в домене с событием синхронизации результатов
        final CyclicBarrier barrier = new CyclicBarrier(ReportGenerator.getSiteList().size());
        // Создадим блокирующую очередь для результатов
        final LinkedBlockingDeque<Element> result = new LinkedBlockingDeque<Element>();
        // отсылаем в потоках
        for (Object o : ReportGenerator.getSiteList().toArray()) {
            new SendToSiteThread((QSite) o, superInputData, barrier, result).start();
        }
        // Консолидировать ответы и получить готовый DataSource.
        // Это консолидированный ответ.
        final Element answers = DocumentHelper.createElement(Uses.TASK_SUPER_REPORT);
        // Принимаем из блокирующей очереди. Поставил тайм аут на случай не отклика.
        // Если отклика нет, то и не надо, будем считать что сайт недоступен.
        for (Object o : ReportGenerator.getSiteList().toArray()) {
            try {
                final Element res = result.poll(15, TimeUnit.SECONDS);
                if (res != null) {
                    // Перетащим все запписи в результат
                    for (Object obj : res.elements()) {
                        answers.add((Element) ((Element) obj).clone());
                    }
                } else {
                    Uses.log.logger.error("Не получен ответ от сайта.");
                }
            } catch (InterruptedException ex) {
                Uses.log.logger.error("Истек таймаут ожидания ответа от сайтов. " + ex);
            }
        }
        final InputStream is;
        final String rec;
        try {
            is = new ByteArrayInputStream(answers.asXML().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new Uses.ReportException("Проблема с кодировкой консолидированного отчета. " + ex);
        }
        if (answers.elements().isEmpty()) {
            rec = "Пусто";
        } else {
            rec = ((Element) answers.elements().get(0)).getName();
        }
        try {
            return new JRXmlDataSource(is, "/" + Uses.TASK_SUPER_REPORT + "/" + rec);
        } catch (JRException ex) {
            throw new Uses.ReportException("Проблема с формированием данных консолидированного отчета. " + ex);
        }

    }

    /**
     *  основная работа по отсылки и получению редиректа заданий и результата.
     * @param site  параметры соединения с сервером
     * @param inputData отсылаемый HTTP-запрос.
     * @return XML-ответ
     */
    private static String send(QSite site, String inputData) throws IOException {
        // открываем сокет и коннектимся к localhost:3128, главное чтоб на отчетный порт
        // получаем сокет сервера
        Socket socket = new Socket(site.getServerAddress(), site.getWebServerPort());
        // Передача данных запроса
        final PrintWriter writer = new PrintWriter(socket.getOutputStream());
        final Scanner in = new Scanner(socket.getInputStream());
        StringBuilder sb = new StringBuilder();
        writer.print(inputData);
        writer.flush();
        // Чтение ответа.
        while (in.hasNextLine()) {
            sb = sb.append(in.nextLine()).append("\n");
        }
        final String data = sb.toString();
        socket.close();
        // преобразуем ответ в XML
        return data;
        /* а можно так. из форума взял
HttpURLConnection httpURLConn = (HttpURLConnection) url.openConnection();
httpURLConn.setUseCaches(false);
httpURLConn.setDoOutput(true);
httpURLConn.setDoInput(true);
httpURLConn.setRequestMethod("POST");
httpURLConn.setAllowUserInteraction(true);
httpURLConn.setRequestProperty("User-Agent:", "Yandex.MONEY Autoclient");
httpURLConn.setRequestProperty("Content-Type:", "application/x-www-form-urlencoded");
httpURLConn.getOutputStream().write(data.getBytes());
         */ 
    }

    /**
     * Класс потока для широковещательной рассылки заданий на сайты домена.
     * Ответ от сайта возвращается в виде XML.
     */
    private class SendToSiteThread extends Thread {

        // этот поток засылает на этот сайт задание
        private final QSite site;
        // это задание шлеццо на сайт в этом потоке
        private final String inputData;
        // способ синхронизации.
        private final CyclicBarrier barrier;
        private LinkedBlockingDeque<Element> result;

        public SendToSiteThread(QSite site, String inputData, CyclicBarrier barrier, LinkedBlockingDeque<Element> result) {
            this.site = site;
            this.inputData = inputData;
            this.barrier = barrier;
            this.result = result;
        }

        @Override
        public void run() {
            Uses.log.logger.trace("Часть широковещательного редиректа для сайта \"" + site + "\".");
            final String answer;
            try {
                answer = send(site, inputData);
            } catch (IOException ex) {
                throw new Uses.ServerException("Ошибка отправки задания на сайт \"" + site + "\". " + ex);
            }
            try {
                final Element res = DocumentHelper.parseText(answer).getRootElement();
                result.put(res);
            } catch (InterruptedException ex) {
                throw new Uses.ServerException("Ошибка разрушения потока. " + ex);
            } catch (DocumentException ex) {
                throw new Uses.ClientException("Не возможно интерпритировать ответ с сайта \"" + site + "\". \n" + ex);
            }
            try {
                barrier.await();
            } catch (InterruptedException ex) {
                throw new Uses.ServerException("Ошибка разрушения потока. " + ex);
            } catch (BrokenBarrierException ex) {
                throw new Uses.ServerException("Ошибка разрушения барьера синхронизации. " + ex);
            }
        }
    }
}
