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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.model.IProperty;

/**
 * Это класс для загрузки набора сервисов обслуживаемых юзером.
 * Ничего хитрого, связь многие-ко-многим + коэффициент участия.
 * Так сделано потому что у сервисов нет привязки к юзерам, эта привязка вроде как односторонняя и
 * еще имеется поле "коэффициент участия", которое будет игнориться при связи "многие-ко-многим".
 * Текстовое название услуги подтягиваеццо отдельно.
 * @author Evgeniy Egorov
 */
@Entity
@Table(name = "services_users")
public class QPlanService implements IProperty, Serializable {

    public QPlanService() {
    }
    //@Id
    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    /**
     * Соответствие услуги.
     */
    protected Long serviceId;

    @Column(name = "service_id")
    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }
    /**
     * Соответствие пользователя.
     */
    protected Long userId;

    @Column(name = "user_id")
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    /*
    private QUser user;
    
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.EAGER)// cascade = CascadeType.ALL - убрал из-зи ошибки при сохранении org.hibernate.NonUniqueObjectException: a different object with the same identifier value was already associated with the session: [ru.apertum.qsystem.server.model.QService#2]
    @JoinTable(name = "services_users",
    joinColumns = @JoinColumn(name = "id"),
    inverseJoinColumns = @JoinColumn(name = "user_id"))
    public QUser getUser() {
    return user;
    }
    
    public void setUser(QUser user) {
    setUserId(user.getId());
    this.user = user;
    }
     */
    private QUser user;

    @Transient
    public QUser getUser() {
        if (user == null) {
            return (QUser) Uses.getSessionFactory().execute(new HibernateCallback() {

                @Override
                public Object doInHibernate(Session session) {
                    return session.get(QUser.class, getUserId());
                }
            });
        } else {
            return user;
        }
    }

    public void setUser(QUser user) {
        this.userId = user.getId();
        this.user = user;
    }
    /**
     * Коэфф. степени участия. По умолчанию основной.
     */
    protected Integer coefficient = 1;

    @Column(name = "coefficient", insertable = true, updatable = true)
    public Integer getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(Integer coefficient) {
        // выставим корректные параметры приоритета обслуживаемой услуге
        // по умолчанию "норма"
        if (coefficient >= Uses.SERVICE_EXCLUDE && coefficient <= Uses.SERVICE_VIP) {
            this.coefficient = coefficient;
        } else {
            this.coefficient = 1;
        }
    }
    private QService service;
    /*
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.EAGER)// cascade = CascadeType.ALL - убрал из-зи ошибки при сохранении org.hibernate.NonUniqueObjectException: a different object with the same identifier value was already associated with the session: [ru.apertum.qsystem.server.model.QService#2]
    @JoinTable(name = "services_users",
    joinColumns = @JoinColumn(name = "id"),
    inverseJoinColumns = @JoinColumn(name = "service_id"))
     */

    @Transient
    public QService getService() {
        if (service == null) {
            final QService srv = (QService) Uses.getSessionFactory().execute(new HibernateCallback() {

                @Override
                public Object doInHibernate(Session session) {
                    return session.get(QService.class, getServiceId());
                }
            });
            service = srv;
            return srv;
        } else {
            return service;
        }
    }

    public void setService(QService service) {
        setServiceId(service.getId());
        this.service = service;
    }
    //************************************************************************************
    //************************************************************************************
    //*********************** Реализация интерфейса **************************************
    @Transient
    @Override
    public String getName() {
        return getService().getName();
    }

    @Transient
    @Override
    public Object getValue() {
        return getCoefficient();
    }

    @Transient
    @Override
    /**
     * @deprecated 
     */
    public Element getXML() {
        //<Услуга Наименование="Секретарь" КоэффициентУчастия="100"/>
        final Element onePlan = DocumentHelper.createElement(Uses.TAG_SERVICE);
        onePlan.addAttribute(Uses.TAG_NAME, getService().getName());
        onePlan.addAttribute(Uses.TAG_PROP_KOEF, String.valueOf(getCoefficient()));
        return onePlan;
    }

    @Transient
    @Override
    public Object getInstance() {
        return this;
    }

    @Override
    public String toString() {
        return getName() + " [" + Uses.COEFF_WORD.get(getCoefficient()) + "]";
    }
}
