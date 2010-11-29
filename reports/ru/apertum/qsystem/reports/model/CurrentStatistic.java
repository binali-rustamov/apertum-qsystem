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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.model.IProperty;
import ru.apertum.qsystem.reports.common.Report;
import ru.apertum.qsystem.server.model.IUserProperty;
import ru.apertum.qsystem.server.model.IUsersGetter;
import ru.apertum.qsystem.server.model.QSiteList;

/**
 * Сбор, обработка и храненение текущей статистики
 * Класс имеет интерфейс полного цикла работы с текущей информацией.
 * Внесение статистики, хранение, обработка, подготовка отчетов.
 * Этот класс - singleton.
 * Он запускает весь отчетный механизм.
 * @author Evgeniy Egorov
 */
public class CurrentStatistic {

    /**
     * В этой структуре храним статистику, в виде готовым для применения JasperReports
     */
    private final Element statistic;
    /**
     * вот она, статистика.
     * Для обращения к этому полю использовать getCurrentStatistic()
     */
    private static CurrentStatistic currentStatistic = null;

    /**
     * Обнулить текущую статистику. Нужно для рестарта.
     */
    public static void closeCurrentStatistic() {
        WebServer.stopWebServer();
        currentStatistic = null;
    }

    private static CurrentStatistic getCurrentStatistic() {
        if (currentStatistic == null) {
            throw new Uses.ReportException("Сервер обработки статистики не запущен.");
        }
        return currentStatistic;
    }

    /**
     * Запуск отчетного механизма.
     * Метод генерации класса статистики. Класс обработки статистики существует в системе в одном экземпляре.
     * @param usesrProp Описание набора пользователей в системе, по ним строим статистику
     * @param port Порт, на котором работает вэбсервер выдачи статистики по HTTP
     * @param reports Список аналитических отчетов.
     * @param siteList Список сайтов домена, если сайт является суперсайтом. Иначе передать null.
     * @return Возвращает класс <b>CurrentStatistic</b> для манипуляций со статистикой.
     * @see CurrentStatistic
     */
    public static CurrentStatistic startCurrentStatistic(IUsersGetter usersGetter, Integer port, List<Report> reports, QSiteList siteList) {
        // паттерн Singleton
        if (currentStatistic == null) {
            // определим логирование для отчетов
            Uses.setRepLogining();
            Uses.logRep.logger.debug("Загружено из базы " + reports.size() + " отчетов.");
            // На случай суперсайта
            ReportGenerator.addSiteList(siteList);
            // создадим механизм сбора статистики
            currentStatistic = new CurrentStatistic(usersGetter);
            // добавим аналитические отчеты
            String list = "";
            for (Report report : reports) {
                ReportGenerator.addGenerator(report);
                list = list.concat(
                        "<tr>\n"
                        + "<td style=\"text-align: left; padding-left: 60px;\">\n"
                        + "<a href=\"" + report.getHref() + ".html\" target=\"_blank\">" + report.getName() + "</a>\n"
                        + "<a href=\"" + report.getHref() + ".rtf\" target=\"_blank\">[RTF]</a>\n"
                        + "<a href=\"" + report.getHref() + ".pdf\" target=\"_blank\">[PDF]</a>\n"
                        + "</td>\n"
                        + "</tr>\n");

            }
            try {
                // список аналитических отчетов.
                WebServer.repList = new String(list.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
            }
            String usrList = "";
            String sel = " selected";
            for (Iterator<IUserProperty> i = usersGetter.iterator(); i.hasNext();) {
                // список пользователей, допущенных до отчетов
                final IUserProperty user = i.next();
                if (user.getReportAccess()) {
                    usrList = usrList.concat("<option" + sel + ">").concat(user.getName()).concat("</option>\n");
                    WebServer.passMap.put(user.getName(), user.getPassword());
                    sel = "";
                }
            }
            try {
                WebServer.usrList = new String(usrList.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
            }
            // стартанем вебсервер
            WebServer.startWebServer(port);
        }
        // вернем класс сбора статиситки
        return currentStatistic;
    }

    /**
     * Конструктор статистики состояни.
     * Создавем структуру статистики, либо новую. либо из файла
     */
    private CurrentStatistic(IUsersGetter userGetter) {
        final long start = System.currentTimeMillis();
        // если есть временный файлик сохранения статистики, то надо его загрузить.
        // все ошибки чтения и парсинга игнорить.
        Uses.logRep.logger.info("Создание статистики.");
        Element stat = null;
        // Из временного файла
        (new File(Uses.TEMP_FOLDER)).mkdir();
        File statFile = new File(Uses.TEMP_FOLDER + File.separator + Uses.TEMP_STATATISTIC_FILE);
        if (statFile.exists()) {
            Uses.logRep.logger.info("Восстановление состояние статистики.");
            //восстанавливаем состояние
            final SAXReader reader = new SAXReader(false);
            try {
                stat = reader.read(statFile).getRootElement();
            } catch (DocumentException ex) {
                Uses.logRep.logger.warn("Невозможно прочитать временный файл статистики. " + ex.getMessage());
            }
        }
        // С нуля.
        if (stat == null) {
            try {
                //создаем корневой элемент для статистики
                stat = DocumentHelper.createElement(Uses.TAG_REP_STATISTIC);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Uses.ReportException("Не создан XML-элемент для статистики.");
            }
            // Дорабатываем по количеству услуг и пользователям их обрабатывающих.
            for (Iterator<IUserProperty> i = userGetter.iterator(); i.hasNext();) {
                final IUserProperty user = i.next();
                final String userName = user.getName();
                for (Iterator<IProperty> itr = user.getUserPlan(); itr.hasNext();) {
                    final String serviceName = itr.next().getName();
                    // Создадим запись и прикрепим к корню списка записей
                    final Element record = stat.addElement(Uses.TAG_REP_RECORD);
                    Element el = record.addElement(Uses.TAG_SERVICE);
                    el.setText(serviceName);
                    el = record.addElement(Uses.TAG_USER);
                    el.setText(userName);
                    // добавим статистические поля
                    record.addElement(Uses.TAG_REP_SERVICE_WORKED).addText("0");
                    record.addElement(Uses.TAG_REP_SERVICE_WAIT).addText("0");
                    record.addElement(Uses.TAG_REP_SERVICE_AVG_WAIT).addText("0 мин.");
                    record.addElement(Uses.TAG_REP_SERVICE_AVG_WORK).addText("0 мин.");
                    record.addElement(Uses.TAG_REP_SERVICE_KILLED).addText("0");

                    record.addElement(Uses.TAG_REP_USER_WORKED).addText("0");
                    record.addElement(Uses.TAG_REP_USER_AVG_WORK).addText("0 мин.");
                    record.addElement(Uses.TAG_REP_USER_KILLED).addText("0");

                    record.addElement(Uses.TAG_REP_WORKED).addText("0");
                    record.addElement(Uses.TAG_REP_AVG_TIME_WORK).addText("0 мин.");
                    record.addElement(Uses.TAG_REP_KILLED).addText("0");
                }
            }
        }
        statistic = stat;
        Uses.logRep.logger.info("Создание статистики завершено. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
    }

    /**
     * Сортировка записей статистики и подготовка групповых данных
     * @param tagName по какому полю сортировать
     */
    private void sortBy(String tagName) {
        Uses.logRep.logger.trace("Сортируем статистику по узлу \"" + tagName + "\"");
        final List<Element> list = ((Element) statistic.clone()).elements(Uses.TAG_REP_RECORD);
        statistic.clearContent();
        final ArrayList<String> arr = new ArrayList<String>();
        //бежтим по всем записям
        for (Element elRec : list) {
            // это тип записи
            final String typeRec = ((Element) elRec.elements(tagName).get(0)).getTextTrim();
            if (!arr.contains(typeRec)) {
                //зная тип записей снова бежим по всем записям, выбирая только нужного типа
                for (Element currElRec : list) {
                    final String currTypeRec = ((Element) currElRec.elements(tagName).get(0)).getTextTrim();
                    // если тип нужный, то добавляем в коллекцию записей, иначе либо уже добавляли, либо в др. раз
                    if (typeRec.equals(currTypeRec)) {
                        currElRec.setParent(null);
                        statistic.add(currElRec);
                    }
                }//вложенный цикл
                arr.add(typeRec);// запоминаем уже добавленные типы записей
            }
        }
    }

    /**
     * Сохранение статистики во временный файл
     * @see Uses.TEMP_STATATISTIC_FILE
     */
    private void saveToTempFile() {
        final long start = System.currentTimeMillis();
        final FileOutputStream fos;
        try {
            fos = new FileOutputStream(Uses.TEMP_FOLDER + File.separator + Uses.TEMP_STATATISTIC_FILE);
        } catch (FileNotFoundException ex) {
            throw new Uses.ReportException("Не возможно создать временный файл состояния. " + ex.getMessage());
        }
        try {
            fos.write(statistic.asXML().getBytes("UTF-8"));
            fos.flush();
            fos.close();
        } catch (IOException ex) {
            throw new Uses.ReportException("Не возможно сохранить изменения в поток." + ex.getMessage());
        }
        Uses.logRep.logger.debug("Статистика сохранена. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
    }

//******************************************************************************************************************
//******************************************************************************************************************
//*******************************  Методы формирования статистики  *************************************************    
    /**
     * Порверка записи на присутствие в ней соов. полей названий услуги и юзера.
     * @param record сама запись в XML виде
     * @param serviceName низвание услуги
     * @param userName название юзера
     * @return нужная строка или нет.
     */
    private boolean checkRecords(Element record, String serviceName, String userName) {
        List<Element> nodeList = record.elements(Uses.TAG_SERVICE);
        if (nodeList.size() != 1) {
            throw new Uses.ReportException("Ошибка идентификации записи по услуге \"" + serviceName + "\"");
        }
        if (!(serviceName.equals(nodeList.get(0).getTextTrim()) || "".equals(serviceName))) {
            return false;
        }
        nodeList = record.elements(Uses.TAG_USER);
        if (nodeList.size() != 1) {
            throw new Uses.ReportException("Ошибка идентификации записи по пользователю \"" + userName + "\"");
        }
        if (userName.equals(nodeList.get(0).getTextTrim()) || "".equals(userName)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Установить строковый параметр в определенную запись.
     * @param serviceName - Название услуги в записи.
     * @param userName - Название юзера в записи
     * @param paramName - Название изменяемого параметра в записи.
     * @param value - новое значение параметра в записи.
     */
    synchronized public void setParam(String serviceName, String userName, String paramName, String value) {
        final List<Element> recordsList = getCurrentStatistic().statistic.elements(Uses.TAG_REP_RECORD);
        for (Element record : recordsList) {
            if (checkRecords(record, serviceName, userName)) {
                final List<Element> nodeList = record.elements(paramName);
                if (nodeList.size() != 1) {
                    throw new Uses.ReportException("Ошибка поиска поля \"" + paramName + "\" в записи с услугой \"" + serviceName + "\" с юзером \"" + userName + "\"");
                }
                nodeList.get(0).setText(value);
            }
        }
    }

    public void setParam(String serviceName, String userName, String paramName, int value) {
        setParam(serviceName, userName, paramName, String.valueOf(value));
    }

    /**
     * Установка среднего значения в минутах
     * @param serviceName - Название услуги в записи.
     * @param userName - Название юзера в записи
     * @param paramName - Название изменяемого параметра в записи.
     * @param min добавляемое время к среднему
     */
    synchronized public void setParam(String serviceName, String userName, String paramName, Double min) {
        final List<Element> recordsList = getCurrentStatistic().statistic.elements(Uses.TAG_REP_RECORD);
        for (Element record : recordsList) {
            if (checkRecords(record, serviceName, userName)) {
                final List<Element> nodeList = record.elements(paramName);
                if (nodeList.size() != 1) {
                    throw new Uses.ReportException("Ошибка поиска поля \"" + paramName + "\" в записи");
                }
                final Element param = nodeList.get(0);
                final String cnt = param.attributeValue(Uses.TAG_REP_PARAM_COUNT);
                final int count;
                if (cnt != null && !"".equals(cnt)) {
                    count = Integer.parseInt(cnt);
                } else {
                    count = 0;
                }
                final String avg = param.attributeValue(Uses.TAG_REP_PARAM_AVG);
                final Double oldValue;
                if (avg != null && !"".equals(avg)) {
                    oldValue = Double.parseDouble(avg);
                } else {
                    oldValue = new Double(0);
                }
                final Double newValue = (count * oldValue + min) / (count + 1);
                param.addAttribute(Uses.TAG_REP_PARAM_COUNT, String.valueOf(count + 1));
                double dbl = Uses.roundAs(newValue, 2);
                dbl = Math.round(dbl - 0.5) + ((dbl - Math.round(dbl - 0.5)) * 60) / 100;
                final String res = String.valueOf(Uses.roundAs(dbl, 2));
                param.setText(res + " мин.");
                param.addAttribute(Uses.TAG_REP_PARAM_AVG, res);
            }
        }
    }

    /**
     * Установка среднего значения в минутах
     * @param serviceName - Название услуги в записи.
     * @param userName - Название юзера в записи
     * @param paramName - Название изменяемого параметра в записи.
     * @param increase прибывить или отнять от значения параметра.
     * @param count изменяем на это число
     */
    synchronized public void setParam(String serviceName, String userName, String paramName, boolean increase, int count) {
        final List<Element> recordsList = getCurrentStatistic().statistic.elements(Uses.TAG_REP_RECORD);
        for (Element record : recordsList) {
            if (checkRecords(record, serviceName, userName)) {
                final List<Element> nodeList = record.elements(paramName);
                if (nodeList.size() != 1) {
                    throw new Uses.ReportException("Ошибка поиска поля \"" + paramName + "\" в записи");
                }
                nodeList.get(0).setText(String.valueOf(Integer.parseInt(nodeList.get(0).getTextTrim()) + (increase ? 1 * count : -1 * count)));
            }
        }
    }

    /**1
     * Регистрация статистики при завершении юзером работы с кастомером или его редиректе.
     * @param serviceName название услуги
     * @param userName название юзера
     * @param min время обработки кастомера в минутах
     */
    public void processingFinishCustomerOrRedirect(String serviceName, String userName, Double min) {
        // Обслужено юзером
        setParam(serviceName, userName, Uses.TAG_REP_WORKED, true, 1);
        // Среднее время обработки юзером
        setParam(serviceName, userName, Uses.TAG_REP_AVG_TIME_WORK, min);
        // Обслужено по услуге
        setParam(serviceName, "", Uses.TAG_REP_SERVICE_WORKED, true, 1);
        // Среднее время обработки по услуге
        setParam(serviceName, "", Uses.TAG_REP_SERVICE_AVG_WORK, min);
        // Обслужено по юзеру
        setParam("", userName, Uses.TAG_REP_USER_WORKED, true, 1);
        // Среднее время обработки по юзеру
        setParam("", userName, Uses.TAG_REP_USER_AVG_WORK, min);
        //Сохраним в темповый файлик
        saveToTempFile();
    }

    /**2
     * Регистрация статистики при отклонении юзером работы с кастомером при неявке.
     * @param serviceName название услуги
     * @param userName название юзера
     */
    public void processingKillCustomer(String serviceName, String userName) {
        // отклонено по неявке юзером
        setParam(serviceName, userName, Uses.TAG_REP_KILLED, true, 1);
        // отклонено по неявке по услуге
        setParam(serviceName, "", Uses.TAG_REP_SERVICE_KILLED, true, 1);
        // отклонено по неявке по юзеру
        setParam("", userName, Uses.TAG_REP_USER_KILLED, true, 1);
        //Сохраним в темповый файлик
        saveToTempFile();
    }

    /**3
     * Регистрация статистики при:
     * - постановке кастомера в очередь
     * - удалении по неявке
     * - старте кастомера в обработку
     * - редиректе
     * @param serviceName название услуги
     * @param count количество кастомеров в очереди к услуге
     */
    public void processingSetWaitCustomers(String serviceName, int count) {
        // ожидают по услуге
        setParam(serviceName, "", Uses.TAG_REP_SERVICE_WAIT, count);
        //Сохраним в темповый файлик
        saveToTempFile();
    }

    /**4
     * Регистрация статистики при старте обработки юзером кастомера.
     * @param serviceName название услуги
     * @param userName название юзера
     * @param min время ожидания кастомера в минутах
     */
    public void processingAvgTimeWait(String serviceName, Double min) {
        // Среднее время ожидания по услуге
        setParam(serviceName, "", Uses.TAG_REP_SERVICE_AVG_WAIT, min);
        //Сохраним в темповый файлик
        saveToTempFile();
    }
//******************************************************************************************************************
//******************************************************************************************************************
//*******************************  Получение структур данных для внесения их в отчет  ******************************

    /**
     * Получение структуры данных для внесения их в отчет
     * Текущее состояние в разрезе услуг
     * @return JRXmlDataSource
     * @throws net.sf.jasperreports.engine.JRException
     */
    public static JRXmlDataSource getDataSourceCurrentServices() throws JRException, UnsupportedEncodingException {
        //Отсортируем по услуге
        getCurrentStatistic().sortBy(Uses.TAG_SERVICE);
        final InputStream is = new ByteArrayInputStream(getCurrentStatistic().statistic.asXML().getBytes("UTF-8"));
        return new JRXmlDataSource(is, "/Статистика/Запись");
    }

    /**
     * Получение структуры данных для внесения их в отчет
     * Текущее состояние в разрезе персонала
     * @return JRXmlDataSource
     * @throws net.sf.jasperreports.engine.JRException
     */
    public static JRXmlDataSource getDataSourceCurrentUsers() throws JRException, UnsupportedEncodingException {
        //Отсортируем по услуге
        getCurrentStatistic().sortBy(Uses.TAG_USER);
        final InputStream is = new ByteArrayInputStream(getCurrentStatistic().statistic.asXML().getBytes("UTF-8"));
        return new JRXmlDataSource(is, "/Статистика/Запись");
    }
}
