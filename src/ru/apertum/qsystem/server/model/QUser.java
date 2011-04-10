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
import java.util.Enumeration;
import javax.persistence.Id;
import ru.apertum.qsystem.common.model.IProperty;
import java.util.Iterator;
import java.util.LinkedList;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.model.QCustomer;

/**
 * Это пользователь. По большому счету роль и пользователь совпадают в системе.
 * Класс пользователя системы.
 * @author Evgeniy Egorov
 */
@Entity
@Table(name = "users")
@XmlRootElement(name = Uses.TAG_USER)
public class QUser implements IUserProperty, Serializable {

    @Expose
    @SerializedName("id")
    private Long id;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    /**
     * Удаленный или нет.
     * Нельзя их из базы гасить чтоб констрейнты не поехали.
     * 0 - удаленный
     * 1 - действующий
     * Только для БД.
     */
    @Expose
    @SerializedName("enable")
    private Integer enable = 1;

    @Column(name = "enable")
    public Integer getEnable() {
        return enable;
    }

    public void setEnable(Integer enable) {
        this.enable = enable;
    }
    /**
     * Параметр доступа к администрированию системы.
     */
    @Expose
    @SerializedName("is_admin")
    private Boolean adminAccess = false;

    public final void setAdminAccess(Boolean adminAccess) {
        this.adminAccess = adminAccess;
    }

    @Column(name = "admin_access")
    //@XmlAttribute(name = Uses.TAG_USER_ADMIN_ACCESS, required = true)
    @Override
    public Boolean getAdminAccess() {
        return adminAccess;
    }
    /**
     * Параметр доступа к отчетам системы.
     */
    @Expose
    @SerializedName("is_report_access")
    private Boolean reportAccess = false;

    public final void setReportAccess(Boolean reportAccess) {
        this.reportAccess = reportAccess;
    }

    @Column(name = "report_access")
    //@XmlAttribute(name = Uses.TAG_USER_ADMIN_ACCESS, required = true)
    @Override
    public Boolean getReportAccess() {
        return reportAccess;
    }
    //******************************************************************************************************************
    //******************************************************************************************************************
    //************************************** Услуги юзера **************************************************************
    /**
     * Множество услуг, которые обрабатывает юзер.
     * По наименованию услуги получаем Класс - описалово участия юзера в этой услуге/
     * Имя услуги -> IProperty
     */
    private QServiceList serviceList = new QServiceList();
    /**
     * Количество услуг, которые обрабатывает юзер. // едет на коиента при логине
     */
    @Expose
    @SerializedName("services_cnt")
    private int servicesCnt = 0;

    /**
     * Количество услуг, которые обрабатывает юзер. // едет на коиента при логине
     * @return
     */
    @Transient
    public int getServicesCnt() {
        return servicesCnt;
    }

    /**
     * Множество услуг, которые обрабатывает юзер.
     * По наименованию услуги получаем Класс - описалово участия юзера в этой услуге/
     * Имя услуги -> IProperty
     * @return Множество услуг, которые обрабатывает юзер.
     */
    @Transient
    public final QServiceList getServiceList() {

        // определим сервисы пользователя
        // Их могли уже определить в конструкторе из файла.
        // Если из базы, то они еще пустые.
        if (flag && Uses.isDBconnected() && serviceList.size() == 0) {
            flag = false;
            serviceList = new QServiceList(getPlanServices());
        }
        servicesCnt = serviceList.size();
        return serviceList;
    }
    private boolean flag = true;

    /**
     * Добавить сервис в список обслуживаемых юзером.
     * Помнить про ДБ.
     * @param planService добавляемый сервис.
     */
    public void addPlanService(IProperty planService) {
        // в список услуг
        final QPlanService planServ = new QPlanService();
        planServ.setCoefficient((Integer) planService.getValue());
        planServ.setService((QService) planService);
        planServ.setUser(this);
        getServiceList().addElement(planServ);
    }

    /**
     * Добавить сервис в список обслуживаемых юзером использую параметры.
     * Используется при добавлении на горячую.
     * @param planService добавляемый сервис.
     * @param coefficient приоритет обработки
     */
    public void addPlanService(QService planService, int coefficient) {
        // в список услуг
        final QPlanService planServ = new QPlanService();
        planServ.setCoefficient(coefficient);
        planServ.setService(planService);
        planServ.setUser(this);
        getServiceList().addElement(planServ);
    }

    /**
     * Переименовать сервис из списка обслуживаемых юзером.
     * Помнить про ДБ.
     * @param oldServiceName
     * @param newServiceName
     */
    public void renamePlanService(String oldServiceName, String newServiceName) {
        final QPlanService planService = getServiceList().getByName(oldServiceName);
        planService.getService().setName(newServiceName);
    }

    /**
     * Удалить сервис из списка обслуживаемых юзером.
     * Помнить про ДБ.
     * @param serviceName удаляемый сервис.
     */
    public void deletePlanService(String serviceName) {
        final QPlanService planService = getServiceList().getByName(serviceName);
        serviceList.removeElement(planService);
        servicesCnt = serviceList.size();
    }
    //************************************** Услуги юзера **************************************************************
    /**
     * Customer, который попал на обработку к этому юзеру.
     * При вызове следующего, первый в очереди кастомер, выдерается из этой очереди совсем и
     * попадает сюда. Сдесь он живет и переживает все интерпритации, которые с ним делает юзер.
     * При редиректе в другую очередь юзером, данный кастомер отправляется в другую очередь,
     * возможно, с другим приоритетом, а эта ссылка становится null.
     */
    //@Transient
    private QCustomer customer = null;

    /**
     * Конструктор для формирования из БД.
     */
    public QUser() {
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Конструктор для формирования пользователя из XML-файла.
     * @param user свойства по которым строим пользователя
     */
    public QUser(IUserProperty user) {
        Uses.log.logger.debug("Пользователь: \"" + user.getName() + "\"");
        // определим параметры пользователя
        setName(user.getName());
        setPassword(user.getPassword());
        setPoint(user.getPoint());
        setAdressRS(user.getAdressRS());
        setReportAccess(user.getReportAccess());
        setAdminAccess(user.getAdminAccess());
        // определим сервисы пользователя
        for (Iterator<IProperty> itr = user.getUserPlan(); itr.hasNext();) {
            final IProperty prop = itr.next();
            final QPlanService plan = new QPlanService();
            //plan.setUser(this);
            plan.setCoefficient((Integer) prop.getValue());
            final QService service = new QService();
            service.setName(prop.getName());
            plan.setService(service);
            getServiceList().addElement(plan);
            Uses.log.logger.trace("Участвует в \"" + prop.getName() + "\" с коэфициентом \'" + prop.getValue() + "\'");
        }
    }

    /*
    @Transient
    private Set<QService> servicePlan = new HashSet<QService>(0);
    
    public void setServicePlan(Set<QService> servicePlan) {
    this.servicePlan = servicePlan;
    }
     */    //@Transient
    //@OneToMany(mappedBy = "userId", fetch = FetchType.EAGER, targetEntity = QPlanService.class, cascade = CascadeType.ALL)//cascade=CascadeType.ALL, fetch=FetchType.LAZY, targetEntity=QPlanService.class)
    /*
    private Set<QPlanService> planServices = new LinkedHashSet<QPlanService>();
    public void setPlanServices(Set<QPlanService> planServices) {
    this.planServices = planServices == null ? new HashSet<QPlanService>() : planServices;
    }
     */
    //@OneToMany(mappedBy = "userId", fetch = FetchType.EAGER, targetEntity = QPlanService.class, cascade = CascadeType.ALL)
    //@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    //@JoinColumn(name = "user_id")
    /**
     * Выборка из БД требуемых данных.
     * @return список услуг пользователя
     */
    @Transient
    public LinkedList<QPlanService> getPlanServices() {

        return (LinkedList<QPlanService>) Uses.getSessionFactory().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                Criteria crit = session.createCriteria(QPlanService.class);
                Criterion user_id = Restrictions.eq("userId", id);
                crit.add(user_id);
                return new LinkedList<QPlanService>(crit.list());
            }
        });
    }

    public void setCustomer(QCustomer customer) {
        if (customer == null && this.customer == null) {
            return;
        }
        if (customer == null && this.customer != null) {
            // если убирается кастомер, то надо убрать признак юзера, который работает с кастомером
            if (getCustomer().getUser() != null) {
                getCustomer().setUser(null);
            }
            // раз юзера убрали, то и время начала работы этого юзера тож убирать
            if (getCustomer().getStartTime() != null) {
                getCustomer().setStartTime(null);
            }
        } else {
            // иначе кастомеру, определившимуся к юзеру, надо поставить признак работы с опред. юзером.
            customer.setUser(this);
        }
        this.customer = customer;
    }

    @Transient
    @XmlTransient
    public QCustomer getCustomer() {
        return customer;
    }

    public boolean hasService(String serviceName) {
        return getServiceList().hasByName(serviceName);
    }

    /**
     * Сохранение обрабатываемого кастомера.
     * Кастомер в xml-виде помещаются в узел root.
     * @param root узел к которому помещается xml-описание кастомера в виде дочернего элемента
     * @deprecated 
     */
    public void saveCastomer(Element root) {
        if (customer != null) {
            root.add((Element) customer.toXML().clone());
        }
    }
    /**
     * Пароль пользователя. В программе хранится открыто.
     * В базе и xml зашифрован.
     */
    //@Column(name = "password")
    @Expose
    @SerializedName("pass")
    private String password = "";

    /**
     * Расшифрует
     * @param password - зашифрованное слово
     */
    public final void setPassword(String password) {
        this.password = password;
    }

    /**
     * Зашифрует
     * @return пароль в зашифрованном виде.
     */
    @Override
    @Column(name = "password")
    public String getPassword() {
        return password;
    }

    public boolean isCorrectPassword(String password) {
        return this.password.equals(password);
    }

    public void recoverAccess(String access) {
        this.password = access;
    }

    /**
     * Перебор всех услуг итератором, которые юзер обслуживает.
     * @return
     * @deprecated 
     */
    @Override
    @Transient
    public Iterator<IProperty> getUserPlan() {
        //return serviceList.toArray().  getPlan().values().iterator();
        return new Iterator() {

            private final Enumeration<QPlanService> en = (Enumeration<QPlanService>) getServiceList().elements();

            @Override
            public boolean hasNext() {
                return en.hasMoreElements();
            }

            @Override
            public QPlanService next() {
                return en.nextElement();
            }

            @Deprecated
            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
    /**
     * Идентификатор рабочего места пользователя.
     */
    @Expose
    @SerializedName("point")
    private String point;

    public final void setPoint(String point) {
        this.point = point;
    }

    @Override
    @Column(name = "point")
    public String getPoint() {
        return point;
    }
    /**
     * Название пользователя.
     */
    @Expose
    @SerializedName("name")
    private String name;

    public final void setName(String name) {
        this.name = name;
    }

    @Override
    @Column(name = "name")
    public String getName() {
        return name;
    }

    @Deprecated
    @Transient
    @Override
    public Object getValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @return
     * @deprecated 
     */
    @Transient
    @Override
    public Element getXML() {

        final Element user = DocumentHelper.createElement(Uses.TAG_USER);
        user.addAttribute(Uses.TAG_NAME, getName());
        user.addAttribute(Uses.TAG_PASSWORD, getPassword());
        user.addAttribute(Uses.TAG_USER_IDENTIFIER, getPoint());
        user.addAttribute(Uses.TAG_USER_ADRESS_RS, String.valueOf(getAdressRS()));
        user.addAttribute(Uses.TAG_USER_ADMIN_ACCESS, getAdminAccess().toString());
        user.addAttribute(Uses.TAG_USER_REPORT_ACCESS, getReportAccess().toString());
        user.add(getServiceList().getXML());
        return user;
    }

    @Deprecated
    @Transient
    @Override
    public Object getInstance() {
        return this;
    }
    @Expose
    @SerializedName("adress_rs")
    private Integer adressRS;

    public final void setAdressRS(Integer adressRS) {
        this.adressRS = adressRS;
    }

    @Override
    @Column(name = "adress_rs")
    public Integer getAdressRS() {
        return adressRS;
    }
}
