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
package ru.apertum.qsystem.reports.generators;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.HashMap;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import org.apache.http.HttpRequest;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.reports.common.Response;
import ru.apertum.qsystem.reports.model.AGenerator;
import ru.apertum.qsystem.reports.model.CurrentStatistic;

/**
 *
 * @author Evgeniy Egorov
 */
public class ReportCurrentServices extends AGenerator {

    public ReportCurrentServices(String href, String resourceNameTemplate) {
        super(href, resourceNameTemplate);
    }

    @Override
    protected JRDataSource getDataSource(HttpRequest request) {
        try {
            return CurrentStatistic.getDataSourceCurrentServices();
        } catch (JRException ex) {
            throw new Uses.ReportException("Ошибка генерации. " + ex);
        } catch (UnsupportedEncodingException ex) {
            throw new Uses.ReportException("Ошибка генерации. Не поддерживается кодировка. " + ex);
        }
    }

    @Override
    protected HashMap getParameters(HttpRequest request) {
        return new HashMap();
    }

    @Override
    protected Connection getConnection(HttpRequest request) {
        return null;
    }

    @Override
    protected Response preparationReport(HttpRequest request) {
        return null;
    }

    @Override
    protected Response getDialog(HttpRequest request, String errorMessage) {
        return null;
    }

    @Override
    protected String validate(HttpRequest request, HashMap<String, String> params) {
        return null;
    }
}
