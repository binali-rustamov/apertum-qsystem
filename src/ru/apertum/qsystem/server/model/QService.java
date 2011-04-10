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
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import javax.persistence.Id;
import java.util.PriorityQueue;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.apertum.qsystem.common.model.QCustomer;
import ru.apertum.qsystem.server.model.calendar.QCalendar;
import ru.apertum.qsystem.server.model.schedule.QSchedule;

/**
 * Модель данных для функционирования очереди
 * включает в себя:
 * - структуру хранения
 * - методы доступа
 * - методы манипулирования
 * - логирование итераций 
 * Главный класс модели данных.
 * Содержит объекты всех кастомеров в очереди к этой услуге.
 * Имеет все необходимые методы для манипулирования кастомерами в пределах одной очереди
 * @author Evgeniy Egorov
 *
 */
@Entity
@Table(name = "services")
/*
@javax.persistence.TableGenerator(
name="EMP_GEN",
table="services",
pkColumnName = "id",
allocationSize=50
)
 */
public class QService extends DefaultMutableTreeNode implements IServiceProperty, MutableTreeNode, Serializable {

    /**
     * множество кастомеров, вставших в очередь к этой услуге
     */
    @Transient
    private final PriorityQueue<QCustomer> customers = new PriorityQueue<QCustomer>();

    public PriorityQueue<QCustomer> getCustomers() {
        return customers;
    }
    @Transient
    //@Expose
    //@SerializedName("clients")
    private final LinkedList<QCustomer> clients = new LinkedList<QCustomer>(customers);

    /**
     * Это все кастомеры стоящие к этой услуге в виде списка
     * @return
     */
    public LinkedList<QCustomer> getClients() {
        return clients;
    }

    @Id
    @Column(name = "id")
    //@GeneratedValue(strategy = GenerationType.AUTO) авто нельзя, т.к. id нужны для формирования дерева
    @Expose
    @SerializedName("id")
    private Long id = new Date().getTime();

    @Override
    public Long getId() {
        return id;
    }

    public final void setId(Long id) {
        this.id = id;
    }
    @Column(name = "status")
    @Expose
    @SerializedName("status")
    private Integer status;

    @Override
    public Integer getStatus() {
        return status;
    }

    public final void setStatus(Integer status) {
        this.status = status;
    }
    @Column(name = "advance_limit")
    @Expose
    @SerializedName("advance_limit")
    private Integer advanceLimit = 1;

    @Override
    public Integer getAdvanceLimit() {
        return advanceLimit;
    }

    public void setAdvanceLinit(Integer advanceLimit) {
        this.advanceLimit = advanceLimit;
    }

    /**
     * Это ограничение в днях, в пределах которого можно записаться вперед при предварительной записи
     * может быть null или 0 если нет ограничения
     */
    @Column(name = "advance_limit_period")
    @Expose
    @SerializedName("advance_limit_period")
    private Integer advanceLimitPeriod = 0;

    public Integer getAdvanceLimitPeriod() {
        return advanceLimitPeriod;
    }

    public void setAdvanceLimitPeriod(Integer advanceLimitPeriod) {
        this.advanceLimitPeriod = advanceLimitPeriod;
    }


    /**
     * Удаленный или нет.
     * Нельзя их из базы гасить чтоб констрейнты не поехали.
     * 0 - удаленный
     * 1 - действующий
     *  Только для БД.
     */
    @Column(name = "enable")
    @Expose
    @SerializedName("enable")
    private Integer enable = 1;

    public Integer getEnable() {
        return enable;
    }

    public void setEnable(Integer enable) {
        this.enable = enable;
    }
    /**
     * Требовать или нет от пользователя после окончания работы с клиентом по этой услуге
     * обозначить результат этой работы выбрав пункт из словаря результатов
     */
    @Column(name = "result_required")
    @Expose
    @SerializedName("result_required")
    private Boolean result_required = false;

    public Boolean getResult_required() {
        return result_required;
    }

    public void setResult_required(Boolean result_required) {
        this.result_required = result_required;
    }
    /**
     * Требовать или нет на пункте регистрации ввода от клиента каких-то
     * данных перед постановкой в очередь после выбора услуги.
     */
    @Column(name = "input_required")
    @Expose
    @SerializedName("input_required")
    private Boolean input_required = false;

    public Boolean getInput_required() {
        return input_required;
    }

    public void setInput_required(Boolean input_required) {
        this.input_required = input_required;
    }
    /**
     * Заголовок окна при вводе на пункте регистрации клиентом каких-то
     * данных перед постановкой в очередь после выбора услуги.
     * Также печатается на талоне рядом с введенными данными.
     */
    @Column(name = "input_caption")
    @Expose
    @SerializedName("input_caption")
    private String input_caption = "";

    public String getInput_caption() {
        return input_caption;
    }

    public void setInput_caption(String input_caption) {
        this.input_caption = input_caption;
    }
    /**
     * html текст информационного сообщения перед постановкой в очередь
     * Если этот параметр пустой, то не требуется показывать информационную напоминалку на пункте регистрации
     */
    @Column(name = "pre_info_html")
    @Expose
    @SerializedName("pre_info_html")
    private String preInfoHtml = "";

    public String getPreInfoHtml() {
        return preInfoHtml;
    }

    public void setPreInfoHtml(String preInfoHtml) {
        this.preInfoHtml = preInfoHtml;
    }
    /**
     * текст для печати при необходимости перед постановкой в очередь
     */
    @Column(name = "pre_info_print_text")
    @Expose
    @SerializedName("pre_info_print_text")
    private String preInfoPrintText = "";

    public String getPreInfoPrintText() {
        return preInfoPrintText;
    }

    public void setPreInfoPrintText(String preInfoPrintText) {
        this.preInfoPrintText = preInfoPrintText;
    }
    /**
     * последний номер, выданный последнему кастомеру при номерировании клиентов обособлено в услуге.
     * тут такой замут. когда услугу создаешь из json где-то на клиенте, то там же спринг-контекст не поднят
     * да и нужно это только в качестве данных.
     */
    @Transient
    private int lastNumber = Uses.spring.factory == null ? 0 : Uses.getNumeration().getFirstNumber() - 1;
    /**
     * последний номер, выданный последнему кастомеру при номерировании клиентов общем рядом для всех услуг.
     * Ограничение самого минимально возможного номера клиента при сквозном нумерировании
     * происходит при определении параметров нумерации.
     */
    @Transient
    private static int lastStNumber = 0;

    public QService() {
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Конструктор услуги, к которой строится очередь.
     * @param service параметры по которым создатся услуга.
     */
    public QService(IServiceProperty service) {
        this.setName(service.getName());
        this.setDescription(service.getDescription());
        this.setPrefix(service.getPrefix());
        this.setButtonText(service.getButtonText());
        this.setId(service.getId());
        this.setStatus(service.getStatus());
        Uses.log.logger.trace("Очередь: \"" + service.getPrefix() + "\" \" " + service.getName() + "\" \" " + service.getDescription() + "\"");
    }

    /**
     * Получить номер для сделующего кастомера. Произойдет инкремент счетчика номеров.
     * @return
     */
    synchronized public int getNextNumber() {
        if (lastNumber >= Uses.getNumeration().getLastNumber()) {
            clearNextNumber();
        }
        if (lastStNumber >= Uses.getNumeration().getLastNumber()) {
            clearNextStNumber();
        }
        // 0 - общая нумерация, 1 - для каждой услуги своя нумерация 
        if (Uses.getNumeration().getNumering() == 0) {
            return ++lastStNumber;
        } else {
            return ++lastNumber;
        }
    }

    public void clearNextNumber() {
        lastNumber = Uses.getNumeration().getFirstNumber() - 1;
    }

    public static void clearNextStNumber() {
        lastStNumber = Uses.getNumeration().getFirstNumber() - 1;
    }

    /**
     * Дополнит XML узел атрибутами об услуге.
     * Если обрабатываемый кастомер уже имеет префикс, то этот префикс остается у него без изменения,
     * а если этот префикс совпадает с префиксом самой услуги,
     * то при номере кастомера больше последнего номера ведущегося в услуге,
     * нужно сменить номер ведущийся в системе на кастомеровский.
     * @param element XML-представление чего-либо требующее внесения в него информации
     * о услуге.
     */
    private void updateInfo(QCustomer customer) {
        final int number = customer.getNumber();
        if (customer.getPrefix() == null) {
            customer.setPrefix(getPrefix());
        } else {
            // тут бы не нужно проверять последний выданный если это происходит с редиректенныйм
            if (Uses.STATE_REDIRECT != customer.getState()) {
                if (number > lastNumber) {
                    lastNumber = number;
                }
                if (number > lastStNumber) {
                    lastStNumber = number;
                }
            }
        }
        customer.setServiceName(getName());
        customer.setServiceDescription(getDescription());
    }

    // ***************************************************************************************
    // ********************  МЕТОДЫ УПРАВЛЕНИЯ ЭЛЕМЕНТАМИ И СТРУКТУРЫ ************************  
    // ***************************************************************************************
    /**
     * Добавить в очередь
     * при этом проставится название сервиса, в который всрал, и его описание,
     * если у кастомера нету префикса, то проставится и префикс.
     * @param customer это кастомер которого добавляем в очередь к услуге
     */
    public void addCustomer(QCustomer customer) {
        updateInfo(customer);
        if (!getCustomers().add(customer)) {
            throw new ServerException("Невозможно добавить нового кастомера в хранилище кастомеров.");
        }
        clients.clear();
        clients.addAll(customers);
    }

    /**
     * Всего хорошего, все свободны!
     */
    public void freeCustomers() {
        getCustomers().clear();
        clients.clear();
        clients.addAll(customers);
    }

    /**
     * Получить, но не удалять.  NoSuchElementException при неудаче
     * @return первого в очереди кастомера
     */
    public QCustomer getCustomer() {
        return getCustomers().element();
    }

    /**
     * Получить и удалить.  NoSuchElementException при неудаче
     * @return первого в очереди кастомера
     */
    public QCustomer removeCustomer() {
        final QCustomer customer = getCustomers().remove();
        clients.clear();
        clients.addAll(customers);
        return customer;
    }

    /**
     * Получить но не удалять. null при неудаче
     * @return первого в очереди кастомера
     */
    public QCustomer peekCustomer() {
        return getCustomers().peek();
    }

    /** 
     * Получить и удалить. может вернуть null при неудаче
     * @return первого в очереди кастомера
     */
    public QCustomer polCustomer() {
        final QCustomer customer = getCustomers().poll();
        clients.clear();
        clients.addAll(customers);
        return customer;
    }

    /**
     * Удалить любого в очереди кастомера. 
     * @param customer удаляемый кастомер
     * @return может вернуть false при неудаче
     */
    public boolean removeCustomer(QCustomer customer) {
        final Boolean res = getCustomers().remove(customer);
        clients.clear();
        clients.addAll(customers);
        return res;
    }

    /** 
     *  Простая очистка очереди
     */
    public void clear() {
        getCustomers().clear();
        clients.clear();
        clients.addAll(customers);
    }

    /** 
     *  Получение количества кастомеров, стоящих в очереди.
     * @return количество кастомеров в этой услуге
     */
    public int getCountCustomers() {
        return getCustomers().size();
    }

    /**
     * Сохранение всех кастомеров из очереди.
     * Все кастомеры в xml-виде помещаются в узел root.
     * @param root узел к которому помещаются xml-описания кастомеров в виде дочерних элементов
     * @deprecated 
     */
    public void saveService(Element root) {
        for (QCustomer customer : getCustomers()) {
            root.add((Element) customer.toXML().clone());
        }
    }
    /**
     * Описание услуги.
     */
    @Expose
    @SerializedName("description")
    @Column(name = "description")
    private String description;

    public final void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }
    /**
     * Префикс услуги.
     */
    @Expose
    @SerializedName("service_prefix")
    @Column(name = "service_prefix")
    private String prefix = "";

    public final void setPrefix(String prefix) {
        this.prefix = prefix == null ? "" : prefix;
    }

    @Override
    public String getPrefix() {
        return prefix == null ? "" : prefix;
    }
    /**
     * Наименование услуги.
     */
    @Expose
    @SerializedName("name")
    @Column(name = "name")
    private String name;

    public final void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
    /**
     * Надпись на кнопке услуги.
     */
    @Expose
    @SerializedName("buttonText")
    @Column(name = "button_text")
    private String buttonText;

    @Override
    public String getButtonText() {
        return buttonText;
    }

    public final void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }
    /**
     * Группировка услуг.
     */
    @Column(name = "prent_id")
    private Long parentId;

    @Override
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "schedule_id")
    private QSchedule schedule;

    public QSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(QSchedule schedule) {
        this.schedule = schedule;
    }
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "calendar_id")
    private QCalendar calendar;

    public QCalendar getCalendar() {
        return calendar;
    }

    public void setCalendar(QCalendar calendar) {
        this.calendar = calendar;
    }

    /**
     * Всегда возвращает 100. при привязке к юзеру этот коэфициент пока не учитывается.
     * @return
     */
    @Override
    public Object getValue() {
        return new Integer(100);
    }

    @Deprecated
    @Override
    public Element getXML() {

        String tagName;
        if (isRoot()) {
            tagName = Uses.TAG_PROP_SERVICES;
        } else {
            if (isLeaf()) {
                tagName = Uses.TAG_PROP_SERVICE;
            } else {
                tagName = Uses.TAG_GROUP;
            }
        }
        final Element service = DocumentHelper.createElement(tagName);
        service.addAttribute(Uses.TAG_NAME, getName());
        service.addAttribute(Uses.TAG_DESCRIPTION, getDescription());
        service.addAttribute(Uses.TAG_PREFIX, getPrefix());
        service.addAttribute(Uses.TAG_PROP_STATUS, String.valueOf(getStatus()));
        service.addAttribute(Uses.TAG_PROP_INPUT_REQUIRED, getInput_required() ? "1" : "0");
        service.addAttribute(Uses.TAG_PROP_RESULT_REQUIRED, getResult_required() ? "1" : "0");
        service.addAttribute(Uses.TAG_PROP_INPUT_CAPTION, getInput_caption());
        service.addCDATA(getButtonText());
        return service;
    }

    @Override
    public Object getInstance() {
        return this;
    }
    //*******************************************************************************************************************
    //*******************************************************************************************************************
    //********************** Реализация методов узла в дереве *********************************************************** 
    /**
     * По сути группа объединения услуг или коернь всего дерева.
     * То во что включена данныя услуга.
     */
    @Transient
    private QService parentService;
    @Transient
    @Expose
    @SerializedName("inner_services")
    private LinkedList<QService> childrenOfService = new LinkedList<QService>();

    public LinkedList<QService> getChildren() {
        return childrenOfService;
    }

    @Override
    public QService getChildAt(int childIndex) {
        return childrenOfService.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return childrenOfService.size();
    }

    @Override
    public QService getParent() {
        return parentService;
    }

    @Override
    public int getIndex(TreeNode node) {
        return childrenOfService.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return getChildCount() == 0;
    }

    @Override
    public Enumeration children() {
        return Collections.enumeration(childrenOfService);
    }

    @Override
    public void insert(MutableTreeNode child, int index) {
        child.setParent(this);
        this.childrenOfService.add(index, (QService) child);
    }

    @Override
    public void remove(int index) {
        this.setEnable(0);
        this.childrenOfService.remove(index);
    }

    @Override
    public void remove(MutableTreeNode node) {
        ((QService) node).setEnable(0);
        this.childrenOfService.remove((QService) node);
    }

    @Override
    public void removeFromParent() {
        getParent().remove(getParent().getIndex(this));
    }

    @Override
    public void setParent(MutableTreeNode newParent) {
        parentService = (QService) newParent;
        if (parentService != null) {
            setParentId(parentService.id);
        } else {
            parentId = null;
        }
    }
}
