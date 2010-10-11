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
package ru.apertum.qsystem.reports.common;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import net.sf.jasperreports.engine.JRDataSource;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.reports.formirovators.IFormirovator;
import ru.apertum.qsystem.reports.model.AGenerator;

/**
 * Класс описания аналитических отчетов.
 * @author Evgeniy Egorov
 */
@Entity
@Table(name = "reports")
public class Report extends AGenerator implements Serializable {

    private Long id;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    private String className;

    @Column(name = "className")
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
        try {
            formirovator = (IFormirovator) Class.forName(getClassName()).newInstance();
        } catch (InstantiationException ex) {
            throw new Uses.ReportException("Чет не в порядке \"" + className + "\". " + ex);
        } catch (IllegalAccessException ex) {
            throw new Uses.ReportException("Нет доступа \"" + className + "\". " + ex);

        } catch (ClassNotFoundException ex) {
            throw new Uses.ReportException("Класс не найден \"" + className + "\". " + ex);
        }
    }
    private IFormirovator formirovator;
    private String name;

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected JRDataSource getDataSource(String inputData) {
        return formirovator.getDataSource(Uses.spring.driverClassName, Uses.spring.url, Uses.spring.username, Uses.spring.password, inputData);
    }

    @Override
    protected byte[] preparation(String inputData) {
        return formirovator.preparation(Uses.spring.driverClassName, Uses.spring.url, Uses.spring.username, Uses.spring.password, inputData);
    }

    @Override
    protected Map getParameters(String inputData) {
        return formirovator.getParameters(Uses.spring.driverClassName, Uses.spring.url, Uses.spring.username, Uses.spring.password, inputData);
    }

    @Override
    protected Connection getConnection(String inputData) {
        return formirovator.getConnection(Uses.spring.driverClassName, Uses.spring.url, Uses.spring.username, Uses.spring.password, inputData);
    }
}
