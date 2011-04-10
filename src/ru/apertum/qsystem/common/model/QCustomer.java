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
package ru.apertum.qsystem.common.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import org.dom4j.Element;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.server.model.QService;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.results.QResult;

/**
 * @author Evgeniy Egorov
 * Реализация клиета
 * Наипростейший "очередник".
 * Используется для организации простой очереди.
 * Если используется СУБД, то сохранение происходит при смене ссостояния.
 * ВАЖНО! Всегда изменяйте статус кастомера при его изменении, особенно при его удалении.
 * 
 */
@Entity
@Table(name = "clients")
public class QCustomer extends ACustomer implements Serializable {

    public QCustomer() {
    }
    /**
     * К какой услуге стоит. Нужно для статистики.
     */
    @Expose
    @SerializedName("to_service")
    private QService service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    public QService getService() {
        return service;
    }

    /**
     * Кастомеру проставим атрибуты услуги включая имя, описание, префикс. 
     * Причем префикс ставится раз и навсегда.
     * При добавлении кастомера в услугу addCustomer() происходит тоже самое + выставляется префикс, если такой
     * атрибут не добавлен в XML-узел кастомера
     * @param service не передавать тут NULL
     */
    public void setService(QService service) {
        this.service = service;
        setServiceName(service.getName());
        setServiceDescription(service.getDescription());
        // Префикс для кастомера проставится при его создании, один раз и на всегда.
        if (getPrefix() == null) {
            setPrefix(service.getPrefix());
        }
        Uses.log.logger.debug("Клиента \"" + getPrefix() + getNumber() + "\" поставили к услуге \"" + service.getName() + "\"");
    }
    /**
     * Результат работы с пользователем
     */
    private QResult result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id")
    public QResult getResult() {
        return result;
    }

    public void setResult(QResult result) {
        this.result = result;
        if (result == null) {
            Uses.log.logger.debug("Обозначать результат работы с кастомером не требуется");
        } else {
            Uses.log.logger.debug("Обозначили результат работы с кастомером: \"" + result.getName() + "\"");
        }
    }
    /**
     * Кто его обрабатывает. Нужно для статистики.
     */
    @Expose
    @SerializedName("from_user")
    private QUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public QUser getUser() {
        return user;
    }

    public void setUser(QUser user) {
        this.user = user;
        Uses.log.logger.debug("Клиенту \"" + getPrefix() + getNumber() + (user == null ? " юзера нету, еще он его не вызывал\"" : " опредилили юзера \"" + user.getName() + "\""));
    }

    /**
     * @param number int номер с которым кастомер встает в очередь
     */
    public QCustomer(int number) {
        super(number);
        Uses.log.logger.debug("Создали кастомера с номером " + number);
    }

    /**
     * Создаем клиента имея его XML-представление
     * @param element XML-представление кастомера
     * @deprecated
     */
    public QCustomer(Element element) {
        super(element);
        Uses.log.logger.debug("Создали кастомера по его описанию\n" + element.asXML());
    }
    /**
     * Префикс услуги, к которой стоит кастомер.
     * @return Строка префикса.
     */
    @Expose
    @SerializedName("prefix")
    private String prefix;

    @Column(name = "service_prefix")
    @Override
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix == null ? "" : prefix;
    }
    /**
     * Название услуги, к которой стоит кастомер.
     * @return Строка названия.
     */
    @Expose
    @SerializedName("service_name")
    private String serviceName;

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Transient
    @Override
    public String getServiceName() {
        return serviceName;
    }
    /**
     * Описание услуги, к которой стоит кастомер.
     * @return Строка описания.
     */
    @Expose
    @SerializedName("service_description")
    private String serviceDescription;

    @Transient
    @Override
    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }
    @Expose
    @SerializedName("stand_time")
    private Date standTime;

    @Column(name = "stand_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Override
    public Date getStandTime() {
        return standTime;
    }

    @Override
    public void setStandTime(Date date) {
        this.standTime = date;
    }
    @Expose
    @SerializedName("start_time")
    private Date startTime;

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(Date date) {
        this.startTime = date;
    }
    private Date callTime;

    @Override
    public void setCallTime(Date date) {
        this.callTime = date;
    }

    @Transient
    @Override
    public Date getCallTime() {
        return callTime;
    }
    @Expose
    @SerializedName("finish_time")
    private Date finishTime;

    @Column(name = "finish_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Override
    public Date getFinishTime() {
        return finishTime;
    }

    @Override
    public void setFinishTime(Date date) {
        this.finishTime = date;
    }
    @Expose
    @SerializedName("input_data")
    private String input_data;

    /**
     * Введенные кастомером данные на пункте регистрации.
     * @return
     */
    @Column(name = "input_data")
    public String getInput_data() {
        return input_data;
    }

    public void setInput_data(String input_data) {
        this.input_data = input_data;
    }
    /**
     * Список услуг в которые необходимо вернуться после редиректа
     * Новые услуги для возврата добвляются в начало списка.
     * При возврате берем первую из списка и удаляем ее.
     */
    private final LinkedList<QService> serviceBack = new LinkedList<QService>();

    /**
     * При редиректе если есть возврат. то добавим услугу для возврата
     * @param service в эту услугу нужен возврат
     */
    public void addServiceForBack(QService service) {
        serviceBack.addFirst(service);
        needBack = !serviceBack.isEmpty();
    }

    /**
     * Куда вернуть если работу закончили но кастомер редиректенный
     * @return вернуть в эту услугу
     */
    @Transient
    public QService getServiceForBack() {
        needBack = serviceBack.size() > 1;
        return serviceBack.pollFirst();
    }
    @Expose
    @SerializedName("need_back")
    private boolean needBack = false;

    public boolean needBack() {
        return needBack;
    }
    /**
     * Комментариии юзеров о кастомере при редиректе и отправки в отложенные
     */
    @Expose
    @SerializedName("temp_comments")
    private String tempComments = "";

    @Transient
    public String getTempComments() {
        return tempComments;
    }

    public void setTempComments(String tempComments) {
        this.tempComments = tempComments;
    }
    /**
     *
     */
    @Expose
    @SerializedName("post_atatus")
    private String postponedStatus = "";

    @Transient
    public String getPostponedStatus() {
        return postponedStatus;
    }

    public void setPostponedStatus(String postponedStatus) {
        this.postponedStatus = postponedStatus;
    }

    /**
     * Вернет XML-строку, описывающую кастомера
     */
    @Override
    public String toString() {
        return prefix + getNumber() + " " + postponedStatus;
    }
}
