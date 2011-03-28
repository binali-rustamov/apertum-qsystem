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
package ru.apertum.qsystem.server.model;

import java.io.Serializable;

import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import ru.apertum.qsystem.common.Uses;

/**
 * Класс предварительно записанного кастомера.
 * Должен уметь работать с БД, генерировать XML. И прочая логика.
 * @author Evgeniy Egorov
 */
@Entity
@Table(name = "advance")
public class QAdvanceCustomer implements Serializable {

    public QAdvanceCustomer() {
    }
    @Id
    @Column(name = "id")
    //@GeneratedValue(strategy = GenerationType.AUTO)
    private Long id = new Date().getTime();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    @Column(name = "advance_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date advanceTime;

    public Date getAdvanceTime() {
        return advanceTime;
    }

    public void setAdvanceTime(Date advanceTime) {
        this.advanceTime = advanceTime;
    }
    @Column(name = "priority")
    private Integer priority;

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "service_id")
    private QService service;

    public QService getService() {
        return service;
    }

    public void setService(QService service) {
        this.service = service;
    }
    /**
     * Связь с таблицей клиентов(фамилии, имена, адреса...) если клиент авторизовался перед тем как записаться на будующее время
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "clients_authorization_id")
    private QAuthorizationCustomer authorizationCustomer;

    public QAuthorizationCustomer getAuthorizationCustomer() {
        return authorizationCustomer;
    }

    public void setAuthorizationCustomer(QAuthorizationCustomer authorizationCustomer) {
        this.authorizationCustomer = authorizationCustomer;
    }

    public Element getXML() {
        final Element user = DocumentHelper.createElement(Uses.TAG_CUSTOMER);
        user.addAttribute(Uses.TAG_START_TIME, Uses.format_for_trans.format(getAdvanceTime()));
        user.addAttribute(Uses.TAG_PRIORITY, getPriority().toString());
        user.addAttribute(Uses.TAG_ID, getId().toString());
        user.addAttribute(Uses.TAG_SERVICE, getService().getName());
        user.addAttribute(Uses.TAG_AUTH_CUSTOMER_ID, getAuthorizationCustomer() == null ? "-1" : getAuthorizationCustomer().getId().toString());
        return user;
    }
}
