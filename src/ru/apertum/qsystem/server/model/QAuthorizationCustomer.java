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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
@Table(name = "clients_authorization")
public class QAuthorizationCustomer implements Serializable {

    public QAuthorizationCustomer() {
    }
    @Id
    @Column(name = "id")
    //@GeneratedValue(strategy = GenerationType.AUTO)
    @Expose
    @SerializedName("id")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    @Column(name = "name")
    @Expose
    @SerializedName("name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @Column(name = "surname")
    @Expose
    @SerializedName("surname")
    private String surname;

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
    @Column(name = "otchestvo")
    @Expose
    @SerializedName("otchestvo")
    private String otchestvo;

    public String getOtchestvo() {
        return otchestvo;
    }

    public void setOtchestvo(String otchestvo) {
        this.otchestvo = otchestvo;
    }
    @Column(name = "birthday")
    @Expose
    @SerializedName("birthday")
    @Temporal(TemporalType.TIMESTAMP)
    private Date birthday;

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
    /* для примера если делать один ко многим
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "service_id")
    private QService service;

    public QService getService() {
    return service;
    }

    public void setService(QService service) {
    this.service = service;
    }
     */

    /**
     *
     * @return
     * @deprecated 
     */
    public Element getXML() {
        final Element user = DocumentHelper.createElement(Uses.TAG_CUSTOMER);
        user.addAttribute(Uses.TAG_ID, getId().toString());
        user.addAttribute(Uses.TAG_NAME, getName());
        user.addAttribute(Uses.TAG_SURNAME, getSurname());
        user.addAttribute(Uses.TAG_OTCHESTVO, getOtchestvo());
        user.addAttribute(Uses.TAG_BIRTHDAY, Uses.format_dd_MM_yyyy.format(getBirthday() == null ? new Date() : getBirthday()));
        return user;
    }
}
