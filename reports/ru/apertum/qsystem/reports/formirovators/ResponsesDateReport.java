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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.apache.commons.lang.time.DateUtils;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import ru.apertum.qsystem.common.Uses;

/**
 * Статистический отчет в разрезе персонала за период
 * @author Igor Savin
 */
public class ResponsesDateReport extends AFormirovator {

    private class ResponsesDateDataSource implements JRDataSource {

        public ResponsesDateDataSource(Connection conn, Date sd, Date ed) {
            index = -1;
            Vector<Vector<Object>> data_tmp = new Vector<Vector<Object>>();
            // fill array
            try {
                String ssd = (new java.text.SimpleDateFormat("yyyy-MM-dd")).format(sd);
                ssd = "'" + ssd + "'";
                String sed = (new java.text.SimpleDateFormat("yyyy-MM-dd")).format(ed);
                sed = "'" + sed + "'";
                Statement stmt = conn.createStatement();
                String query = "SELECT " +
                        "r.id, " +
                        "r.`name` AS responses_name, " +
                        "DATE_FORMAT(IFNULL(e.`resp_date`,#sd#),'%Y-%m-%d') AS responses_date, " +
                        "count(e.`resp_date`) AS responses_count, " +
                        "(SELECT count(*) " +
                        "FROM " +
                        "`responses` r JOIN `response_event` e ON r.`id` = e.`response_id` " +
                        "WHERE " +
                        "e.`resp_date` >= #sd#  AND  e.`resp_date` <= #ed#) AS all_count, " +
                        "IFNULL((SELECT count(e.`resp_date`) " +
                        "FROM `response_event` e WHERE r.`id` = e.`response_id` " +
                        "AND " +
                        "e.`resp_date` >= #sd#  AND  e.`resp_date` <= #ed# " +
                        "GROUP BY r.id), 0) AS all_count_period " +
                        "FROM " +
                        "`responses` r LEFT JOIN `response_event` e " +
                        "ON r.`id` = e.`response_id` AND " +
                        "e.`resp_date` >= #sd#  AND  e.`resp_date` <= #ed# " +
                        "GROUP BY r.id, responses_date";
                query = query.replaceAll("#sd#", ssd).replaceAll("#ed#", sed);
                ResultSet rs = stmt.executeQuery(query);
                int id, prev_id = -1;
                Vector<HashMap<String, Integer>> id_set = new Vector<HashMap<String, Integer>>();
                int ind = 0;
                while (rs.next()) {
                    id = rs.getInt(1);
                    if (id != prev_id) {
                        HashMap<String, Integer> hash_id = new HashMap<String, Integer>();
                        hash_id.put("id", new Integer(id));
                        hash_id.put("ind", new Integer(ind));
                        id_set.add(hash_id);
                    }
                    Vector<Object> line = new Vector<Object>(6, 0);
                    line.add(new java.lang.Integer(rs.getInt(1)));
                    line.add(rs.getString(2));
                    line.add(rs.getDate(3));
                    line.add(new java.lang.Integer(rs.getInt(4)));
                    line.add(new java.lang.Integer(rs.getInt(5)));
                    line.add(new java.lang.Integer(rs.getInt(6)));
                    data_tmp.add(line);
                    prev_id = id;
                    ind++;
                }
                data = new Vector<Vector<Object>>();
                int isd = (int) (sd.getTime() / 86400000L) + 1;	// дата в количестве дней с 1970 года
                int ied = (int) (ed.getTime() / 86400000L) + 1;
                int i;
                int idt;
                java.util.Date dt = new java.util.Date();
                Vector<Object> ext_line;
                HashMap<String, Integer> hash_id;
                int need_id;
                int need_ind;
                String name;
                java.util.Date date;
                int idate;
                int count;
                int all_count;
                int all_count_period;
                for (i = 0; i < id_set.size(); i++) {
                    try {
                        hash_id = id_set.get(i);
                        need_id = hash_id.get("id");
                        need_ind = hash_id.get("ind");
                        ind = need_ind;
                        for (idt = isd; idt <= ied; idt++) // idt - счетчик дней
                        {
                            dt = new java.util.Date();
                            dt.setTime(idt * 86400000L);
                            if (ind >= data_tmp.size()) {
                                ind = data_tmp.size() - 1;
                            }
                            ext_line = data_tmp.get(ind);
                            id = ((Integer) ext_line.get(0)).intValue();
                            if (need_id == id) {
                                name = (String) ext_line.get(1);
                                date = (java.util.Date) ext_line.get(2);
                                idate = (int) (date.getTime() / 86400000L) + 1;
                                count = ((Integer) ext_line.get(3)).intValue();
                                all_count = ((Integer) ext_line.get(4)).intValue();
                                all_count_period = ((Integer) ext_line.get(5)).intValue();
                                if (idt != idate) // ins_before, ins_after
                                {
                                    Vector<Object> new_line = new Vector<Object>(6, 0);
                                    new_line.add(new Integer(id));
                                    new_line.add(name);
                                    new_line.add(dt);
                                    new_line.add(new Integer(0));
                                    new_line.add(new Integer(all_count));
                                    new_line.add(new Integer(all_count_period));
                                    data.add(new_line);
                                } else if (idt == idate) // copy, next
                                {
                                    @SuppressWarnings("unchecked")
                                    Vector<Object> copy_line = (Vector<Object>) ext_line.clone();
                                    data.add(copy_line);
                                    ind++;
                                }
                            } else {
                                ext_line = data_tmp.get(need_ind);
                                name = (String) ext_line.get(1);
                                all_count = ((Integer) ext_line.get(4)).intValue();
                                all_count_period = ((Integer) ext_line.get(5)).intValue();
                                Vector<Object> new_line = new Vector<Object>(6, 0);
                                new_line.add(new Integer(need_id));
                                new_line.add(name);
                                new_line.add(dt);
                                new_line.add(new Integer(0));
                                new_line.add(new Integer(all_count));
                                new_line.add(new Integer(all_count_period));
                                data.add(new_line);
                                ind++;
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        ;
                    }
                }
            } catch (SQLException ex) {
                throw new Uses.ReportException("Ошибка выполнения запроса ResponsesDateDataSource" + ex);
            } catch (Exception ex) {
                throw new Uses.ReportException("Ошибка обработки запроса ResponsesDateDataSource" + ex);
            }
        }
        private Vector<Vector<Object>> data;
        private int index;
        /*public void moveFirst()
        {
        index = -1;
        }*/

        public boolean next() throws JRException {
            index++;
            return (index < data.size());
        }

        public Object getFieldValue(JRField field) throws JRException {
            Object value = null;

            String fieldName = field.getName();
            Vector line = null;
            try {
                line = (Vector) data.get(index);
                if ("id".equals(fieldName)) {
                    value = line.get(0);
                } else if ("responses_name".equals(fieldName)) {
                    value = line.get(1);
                } else if ("responses_date".equals(fieldName)) {
                    value = line.get(2);
                } else if ("responses_count".equals(fieldName)) {
                    value = line.get(3);
                } else if ("all_count".equals(fieldName)) {
                    value = line.get(4);
                } else if ("all_count_period".equals(fieldName)) {
                    value = line.get(5);
                } else {
                    value = new String("unk_field");
                }
            } catch (Exception e) {
                value = new String("ResponsesDateDataSource: index is out of range!");
                return value;
            }
            return value;
        }
    }

    /**
     * Получение источника данных для отчета.
     * @return Готовая структура для компилирования в документ.
     */
    @Override
    public JRDataSource getDataSource(String driverClassName, String url, String username, String password, String inputData) {
        Connection conn = connect_to_db(driverClassName, url, username, password, inputData);
        Date sd = paramMap.get("sd");
        Date ed1 = paramMap.get("ed1");
        return new ResponsesDateDataSource(conn, sd, ed1);
    }

    /**
     * Метод формирования параметров для отчета.
     * В отчет нужно передать некие параметры. Они упаковываются в Мар.
     * Если параметры не нужны, то сформировать пустой Мар.
     * @return
     */
    @Override
    public Map getParameters(String driverClassName, String url, String username, String password, String inputData) {
        return paramMap;
    }
    /**
     * Для параметров
     */
    final private HashMap<String, Date> paramMap = new HashMap<String, Date>();

    private Connection connect_to_db(String driverClassName, String url, String username, String password, String inputData) {
        final Connection connection;
        try {
            Class.forName(driverClassName);
            connection = DriverManager.getConnection(url + (url.indexOf("?") == -1 ? "" : "&") + "user=" + username + "&password=" + password);
        } catch (SQLException ex) {
            throw new Uses.ReportException(StatisticServices.class.getName() + " " + ex);
        } catch (ClassNotFoundException ex) {
            throw new Uses.ReportException(StatisticServices.class.getName() + " " + ex);
        }
        return connection;
    }

    /**
     * Метод получения коннекта к базе если отчет строится через коннект.
     * Если отчет строится не через коннект, а формироватором, то выдать null.
     * @return коннект соединения к базе или null.
     */
    @Override
    public Connection getConnection(String driverClassName, String url, String username, String password, String inputData) {
        return null;
    }

    @Override
    public byte[] preparation(String driverClassName, String url, String username, String password, String inputData) {
        // если в запросе не содержаться введенные параметры, то выдыем форму ввода
        // иначе выдаем null.
        final String data = Uses.getRequestData(inputData);
        Uses.log.logger.trace("Принятые параметры \"" + data + "\".");
        // флаг введенности параметров
        boolean flag = false;
        String mess = "";
        if ("".equals(data)) {
            flag = true;
        } else {
            //sd=20.01.2009&ed=28.01.2009
            // проверка на корректность введенных параметров
            final String[] ss = data.split("&");
            if (ss.length == 2) {
                final String[] ss0 = ss[0].split("=");
                final String[] ss1 = ss[1].split("=");

                Date sd = null;
                Date fd = null;
                Date fd1 = null;

                flag = !(ss0.length == 2 && ss1.length == 2);
                if (!flag) {
                    try {
                        sd = Uses.format_dd_MM_yyyy.parse(ss0[1]);
                        fd = Uses.format_dd_MM_yyyy.parse(ss1[1]);
                        fd1 = DateUtils.addDays(Uses.format_dd_MM_yyyy.parse(ss1[1]), 1);

                    } catch (ParseException ex) {
                        mess = "<br>Ошибка ввода параметров! Не все параметры введены корректно(дд.мм.гггг).";
                        flag = true;
                    }
                } else {
                    mess = "<br>Ошибка ввода параметров! Не все параметры введены корректно(дд.мм.гггг).";
                }
                if (!flag) {
                    if (!sd.after(fd)) {
                        paramMap.put(ss0[0], sd);
                        paramMap.put(ss1[0], fd);
                        paramMap.put("ed1", fd1);
                    } else {
                        mess = "<br>Ошибка ввода параметров! Дата начала больше даты завершения.";
                        flag = true;
                    }
                }

            } else {
                mess = "<br>Ошибка ввода параметров!";
                flag = true;
            }
        }
        if (flag) {
            // вставим необходимую ссылку на отчет в форму ввода
            // и выдадим ее клиенту на заполнение.
            // после заполнения вызовется нужный отчет с введенными параметрами и этот метод вернет null,
            // что продолжет генерить отчет методом getDataSource с нужными параметрами.
            // А здесь мы просто знаем какой формироватор должен какие формы выдавать пользователю. На то он и формироватор, индивидуальный для каждого отчета.
            // get_period_for_statistic_services.html
            final InputStream inStream = getClass().getResourceAsStream("/ru/apertum/qsystem/reports/web/get_period_for_statistic_date_responses.html");
            String result = null;
            try {
                result = new String(Uses.readInputStream(inStream), "UTF-8");
            } catch (IOException ex) {
                throw new Uses.ReportException("Ошибка чтения ресурса для диалогового ввода периода. " + ex);
            }
            final String subject = Uses.getRequestTarget(inputData);
            result = result.replaceFirst(Uses.ANCHOR_DATA_FOR_REPORT, subject).replaceFirst(Uses.ANCHOR_ERROR_INPUT_DATA, mess);
            try {
                return result.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                return result.getBytes();
            }
        } else {
            return null;
        }
    }
}
