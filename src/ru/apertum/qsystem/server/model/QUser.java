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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import javax.persistence.Id;
import javax.xml.bind.JAXBException;
import ru.apertum.qsystem.common.model.IProperty;
import java.util.Iterator;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.model.ICustomer;

/**
 * Это пользователь. По большому счету роль и пользователь совпадают в системе.
 * Класс пользователя системы.
 * @author Evgeniy Egorov
 */
@Entity
@Table(name = "users")
@XmlRootElement(name = Uses.TAG_USER)
public class QUser implements IUserProperty, Serializable {

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
    private final QServiceList serviceList = new QServiceList();

    @Transient
    public final QServiceList getServiceList() {
        // определим сервисы пользователя
        // Их могли уже определить в конструкторе из файла.
        // Если из базы, то они еще пустые.
        if (flag && Uses.isDBconnected() && serviceList.size() == 0) {
            flag = false;
            for (Iterator<QPlanService> itr = getPlanServices().iterator(); itr.hasNext();) {
                final QPlanService planService = itr.next();
                serviceList.addElement(planService);
                Uses.log.logger.trace("\"" + name + "\" участвует в \"" + planService.getName() + "\" с коэфициентом \'" + planService.getValue() + "\'");
            }
        }
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
    private ICustomer customer = null;

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
    public List<QPlanService> getPlanServices() {
        //return new LinkedList<QPlanService>();

        return (List<QPlanService>) Uses.getSessionFactory().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                Criteria crit = session.createCriteria(QPlanService.class);
                Criterion user_id = Restrictions.eq("userId", id);
                //Criterion name = Restrictions.like("name","P%");
                //LogicalExpression orExp = Restrictions.or(price,name);
                //crit.add(orExp);
                crit.add(user_id);
                //crit.add(Restrictions.ilike("description","for%"));
                return crit.list();
            }
        });
        //return planServices;

    }

    public void setCustomer(ICustomer customer) {
        if (customer == null && this.customer == null) {
            return;
        }
        if (customer == null && this.customer != null) {
            // если убирается кастомер, то надо убрать признак юзера, который работает с кастомером
            Attribute attr = this.customer.toXML().attribute(Uses.TAG_USER);
            if (attr != null) {
                this.customer.toXML().remove(attr);
            }
            // раз юзера убрали, то и время начала работы этого юзера тож убирать
            attr = this.customer.toXML().attribute(Uses.TAG_START_TIME);
            if (attr != null) {
                this.customer.toXML().remove(attr);
            }
        } else {
            // иначе кастомеру, определившимуся к юзеру, надо поставить признак работы с опред. юзером.
            customer.toXML().addAttribute(Uses.TAG_USER, getName());
        }
        this.customer = customer;
    }

    @Transient
    @XmlTransient
    public ICustomer getCustomer() {
        return customer;
    }

    public boolean hasService(String serviceName) {
        return getServiceList().hasByName(serviceName);
    }

    /**
     * Сохранение обрабатываемого кастомера.
     * Кастомер в xml-виде помещаются в узел root.
     * @param root узел к которому помещается xml-описание кастомера в виде дочернего элемента
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
    private String password = "";

    /**
     * Расшифрует
     * @param password - зашифрованное слово
     */
    public final void setPassword(String password) {
        /*final byte[] b = password.getBytes();
        final byte col = b[b.length - 1];
        for (byte i = 0; i < b.length - 2; i++) {
        b[i] = (byte) ((-1) * (b[i] - (i + col * (i % 3))));
        }
        String res = new String(b);
        this.password = res.substring(0, res.length() - 1 - col);*/
        this.password = password;
    }

    /**
     * Зашифрует
     * @return пароль в зашифрованном виде.
     */
    @Override
    @Column(name = "password")
    //@XmlAttribute(name = Uses.TAG_PASSWORD, required = true)
    public String getPassword() {
        /*String up = this.password;
        for (int i = 0; i < 21; i++) {
        if (i > up.length()) {
        up = up + '~';
        }
        }
        final byte col;
        if (password.length() > 20) {
        col = 0;
        } else {
        col = (byte) (20 - password.length());
        }
        up = up + 'n';
        final byte[] b = up.getBytes();
        b[b.length - 1] = col;
        
        for (byte i = 0; i < b.length - 2; i++) {
        b[i] = (byte) ((-1) * b[i] + (i + col * (i % 3)));
        }
        return new String(b);*/
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
    //@Column(name = "point")
    private String point;

    public final void setPoint(String point) {
        this.point = point;
    }

    @Override
    @Column(name = "point")
    //@XmlAttribute(name = Uses.TAG_USER_IDENTIFIER, required = true)
    public String getPoint() {
        return point;
    }
    /**
     * Название пользователя.
     */
    //@Column(name = "name")
    private String name;

    public final void setName(String name) {
        this.name = name;
    }

    @Override
    @Column(name = "name")
    //@XmlAttribute(name = Uses.TAG_USER_REPORT_ACCESS, required = true)
    public String getName() {
        return name;
    }

    @Deprecated
    @Transient
    @Override
    public Object getValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

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
    final static private JAXBContext AXB;

    static {
        try {
            AXB = JAXBContext.newInstance(new Class[]{QUser.class});
        } catch (JAXBException ex) {
            throw new Uses.ServerException(ex);
        }
    }

    public synchronized void marshal(OutputStream outputStream) {
        try {
            final Marshaller m = AXB.createMarshaller();
            m.setProperty("jaxb.formatted.output", true);
            m.marshal(this, outputStream);
        } catch (JAXBException ex) {
            throw new Uses.ServerException(ex);
        }
    }

    public synchronized String marshal() {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        marshal(os);
        try {
            return os.toString("utf8");
        } catch (UnsupportedEncodingException ex) {
            throw new Uses.ServerException(ex);
        }
    }

    public static synchronized QUser unmarshal(InputStream inputStream) {
        final Unmarshaller u;
        try {
            u = AXB.createUnmarshaller();
            return (QUser) u.unmarshal(inputStream);
        } catch (JAXBException ex) {
            throw new Uses.ServerException(ex);
        }
    }

    public static synchronized QUser unmarshal(String data) {
        final Unmarshaller u;
        try {
            u = AXB.createUnmarshaller();
            return (QUser) u.unmarshal(new ByteArrayInputStream(data.getBytes("uts8")));
        } catch (JAXBException ex) {
            throw new Uses.ServerException(ex);
        } catch (UnsupportedEncodingException ex) {
            throw new Uses.ServerException(ex);
        }
    }

    @Transient
    @Override
    public Object getInstance() {
        return this;
    }
    private Integer adressRS;

    public final void setAdressRS(Integer adressRS) {
        this.adressRS = adressRS;
    }

    @Override
    @Column(name = "adress_rs")
    //@XmlAttribute(name = Uses.TAG_USER_ADRESS_RS, required = true)
    public Integer getAdressRS() {
        return adressRS;
    }
}
