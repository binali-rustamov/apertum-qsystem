package ru.apertum.qsystem.reports.formirovators;

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
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpRequest;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.reports.common.Response;

/**
 *
 * @author Igor Savin
 */
public class DistributionMedDayServices extends AFormirovator {

    /**
     * Метод формирования параметров для отчета.
     * В отчет нужно передать некие параметры. Они упаковываются в Мар.
     * Если параметры не нужны, то сформировать пустой Мар.
     * @return
     */
    @Override
    public Map getParameters(String driverClassName, String url, String username, String password, HttpRequest request) {
        return paramMap;
    }
    /**
     * Для параметров
     */
    final private HashMap<String, Object> paramMap = new HashMap<String, Object>();

    /**
     * Метод получения коннекта к базе если отчет строится через коннект.
     * Если отчет строится не через коннект, а формироватором, то выдать null.
     * @return коннект соединения к базе или null.
     */
    @Override
    public Connection getConnection(String driverClassName, String url, String username, String password, HttpRequest request) {
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
/*
    @Override
    public byte[] preparation(String driverClassName, String url, String username, String password, HttpRequest request) {
        // если в запросе не содержаться введенные параметры, то выдыем форму ввода
        // иначе выдаем null.
        final String data = NetUtil.getEntityContent(request);
        final String tmp = NetUtil.getUrl(request);
        //Uses.log.logger.trace("Принятые параметры \""+data+"\".");
        //Uses.log.logger.trace("subject \""+tmp+"\".");
        // флаг введенности параметров
        boolean flag = false;
        String mess = "";
        if ("".equals(data)) {
            flag = true;
        } else {
            // проверка на корректность введенных параметров
            final String[] ss = data.split("&");
            if (ss.length == 3) {
                final String[] ss0 = ss[0].split("=");
                final String[] ss1 = ss[1].split("=");
                final String[] ss2 = ss[2].split("=");
                Date date = null;
                String sdate = null;
                long service_id = -1;
                String service = null;
                try {
                    date = Uses.format_dd_MM_yyyy.parse(ss0[1]);
                    sdate = (new java.text.SimpleDateFormat("yyyy-MM-dd")).format(date);
                    service_id = Long.parseLong(ss1[1]);
                    service = ss2[1];
                } catch (Exception ex) {
                    mess = "<br>Ошибка ввода параметров! Не все параметры введены корректно (дд.мм.гггг).";
                    flag = true;
                }
                if (!flag) {
                    paramMap.put("sdate", sdate);
                    paramMap.put(ss0[0], date);
                    paramMap.put(ss1[0], new Long(service_id));
                    paramMap.put(ss2[0], service);
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
            final InputStream inStream = getClass().getResourceAsStream("/ru/apertum/qsystem/reports/web/get_date_distribution_services.html");
            String result = null;
            String services_select = "";
            try {
                result = new String(Uses.readInputStream(inStream), "UTF-8");
            } catch (IOException ex) {
                throw new Uses.ReportException("Ошибка чтения ресурса для диалога ввода сервиса. " + ex);
            }
            try {
                Connection conn = getConnection(driverClassName, url, username, password, request);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT id, name FROM services WHERE id NOT IN (SELECT DISTINCT prent_id FROM services WHERE prent_ID IS NOT NULL) ORDER BY name");
                Long id;
                String svcname;
                services_select = "";
                while (rs.next()) {
                    id = rs.getLong(1);
                    svcname = rs.getString(2);
                    services_select += "<option value=" + id + ">" + svcname + "\n";
                }
            } catch (SQLException ex) {
                throw new Uses.ReportException("Ошибка выполнения запроса для диалога ввода сервиса. " + ex);
            }
            result = result.replaceFirst(Uses.ANCHOR_DATA_FOR_REPORT, request.getRequestLine().getUri()).replaceFirst(Uses.ANCHOR_ERROR_INPUT_DATA, mess).replaceFirst("#DATA_FOR_TITLE#", "Отчет предварительно зарегистрированных клиентов по услуге на дату:").replaceFirst("#DATA_FOR_SERVICES#", services_select);
            try {
                return result.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                return result.getBytes();
            }
        } else {
            return null;
        }
    }
*/
    @Override
    public Response getDialog(String driverClassName, String url, String username, String password, HttpRequest request, String errorMessage) {
        final Response result = getDialog("/ru/apertum/qsystem/reports/web/get_date_distribution_services.html", request, errorMessage);
        final StringBuilder services_select = new StringBuilder();
        final Connection conn = getConnection(driverClassName, url, username, password, request);
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT id, name FROM services WHERE id NOT IN (SELECT DISTINCT prent_id FROM services WHERE prent_ID IS NOT NULL) ORDER BY name");
            while (rs.next()) {
                services_select.append("<option value=").append(rs.getLong(1)).append(">").append(rs.getString(2)).append("\n");
            }
        } catch (SQLException ex) {
            services_select.setLength(0);
            throw new Uses.ReportException("Ошибка выполнения запроса для диалога ввода пользователя. " + ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                throw new Uses.ReportException("Ошибка выполнения запроса для диалога ввода пользователя. " + ex);
            }
        }
        try {
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            throw new Uses.ReportException("Ошибка закрытия запроса для диалога ввода пользователя. " + ex);
        }

        result.setData(new String(result.getData()).replaceFirst("#DATA_FOR_TITLE#", "Отчет предварительно зарегистрированных клиентов по услуге на дату:").replaceFirst("#DATA_FOR_SERVICES#", services_select.toString()).getBytes());
        services_select.setLength(0);
        return result;
    }

    @Override
    public String validate(String driverClassName, String url, String username, String password, HttpRequest request, HashMap<String, String> params) {
        // проверка на корректность введенных параметров
        Uses.log.logger.trace("Принятые параметры \"" + params.toString() + "\".");
        if (params.size() == 3) {
            // date/service_id/service
            Date date = null;
            String sdate = null;
            long service_id = -1;
            String service = null;
            try {
                date = Uses.format_dd_MM_yyyy.parse(params.get("date"));
                sdate = (new java.text.SimpleDateFormat("yyyy-MM-dd")).format(date);
                service_id = Long.parseLong(params.get("service_id"));
                service = params.get("service");
            } catch (Exception ex) {
                return "<br>Ошибка ввода параметров! Не все параметры введены корректно (дд.мм.гггг).";
            }
            paramMap.put("sdate", sdate);
            paramMap.put("date", date);
            paramMap.put("service_id", new Long(service_id));
            paramMap.put("service", service);
        } else {
            return "<br>Ошибка ввода параметров!";
        }
        return null;
    }
}
