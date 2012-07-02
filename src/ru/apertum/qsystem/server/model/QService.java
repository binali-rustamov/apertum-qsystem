/*
 *  Copyright (C) 2010 {Apertum}Projects. web: www.apertum.ru email: info@apertum.ru
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
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Id;
import java.util.PriorityQueue;
import java.util.ServiceLoader;
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
import ru.apertum.qsystem.common.CustomerState;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.apertum.qsystem.common.model.QCustomer;
import ru.apertum.qsystem.extra.ICustomerChangePosition;
import ru.apertum.qsystem.server.ServerProps;
import ru.apertum.qsystem.server.Spring;
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
public class QService extends DefaultMutableTreeNode implements ITreeIdGetter, Serializable {

    /**
     * множество кастомеров, вставших в очередь к этой услуге
     */
    @Transient
    private final PriorityQueue<QCustomer> customers = new PriorityQueue<>();

    private PriorityQueue<QCustomer> getCustomers() {
        return customers;
    }
    @Transient
    //@Expose
    //@SerializedName("clients")
    private final LinkedList<QCustomer> clients = new LinkedList<>(customers);

    /**
     * Это все кастомеры стоящие к этой услуге в виде списка
     * Только для бакапа на диск
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
    /**
     * Состояние услуги. 1 - доступна, 0 - недоступна, -1 - невидима.
     */
    @Column(name = "status")
    @Expose
    @SerializedName("status")
    private Integer status;

    public Integer getStatus() {
        return status;
    }
    /**
     * Пунктов регистрации может быть много.
     * Наборы кнопок на разных киосках могут быть разные.
     * Указание для какого пункта регистрации услуга, 0-для всех, х-для киоска х.
     */
    @Column(name = "point")
    @Expose
    @SerializedName("point")
    private Integer point = 0;

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public final void setStatus(Integer status) {
        this.status = status;
    }
    @Column(name = "advance_limit")
    @Expose
    @SerializedName("advance_limit")
    private Integer advanceLimit = 1;

    public Integer getAdvanceLimit() {
        return advanceLimit;
    }

    public void setAdvanceLinit(Integer advanceLimit) {
        this.advanceLimit = advanceLimit;
    }
    @Column(name = "day_limit")
    @Expose
    @SerializedName("day_limit")
    private Integer dayLimit = 0;

    public Integer getDayLimit() {
        return dayLimit;
    }

    public void setDayLimit(Integer dayLimit) {
        this.dayLimit = dayLimit;
    }
    @Column(name = "person_day_limit")
    @Expose
    @SerializedName("person_day_limit")
    private Integer personDayLimit = 0;

    public Integer getPersonDayLimit() {
        return personDayLimit;
    }

    public void setPersonDayLimit(Integer personDayLimit) {
        this.personDayLimit = personDayLimit;
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
     * Способ вызова клиента юзером
     * 1 - стандартно
     * 2 - backoffice, т.е. вызов следующего без табло и звука, запершение только редиректом
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
    private int lastNumber = Integer.MIN_VALUE;
    /**
     * последний номер, выданный последнему кастомеру при номерировании клиентов общем рядом для всех услуг.
     * Ограничение самого минимально возможного номера клиента при сквозном нумерировании
     * происходит при определении параметров нумерации.
     */
    @Transient
    private static int lastStNumber = 0;

    public QService() {
        super();
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Получить номер для сделующего кастомера. Произойдет инкремент счетчика номеров.
     * @return
     */
    synchronized public int getNextNumber() {
        if (lastNumber == Integer.MIN_VALUE) {
            lastNumber = ServerProps.getInstance().getProps().getFirstNumber() - 1;
        }
        if (lastNumber >= ServerProps.getInstance().getProps().getLastNumber()) {
            clearNextNumber();
        }
        if (lastStNumber >= ServerProps.getInstance().getProps().getLastNumber()) {
            clearNextStNumber();
        }
        // учтем вновь поставленного. прибавим одного к количеству сегодня пришедших к данной услуге
        final int today = new GregorianCalendar().get(GregorianCalendar.DAY_OF_YEAR);
        if (today != day) {
            day = today;
            countPerDay = 0;
        }
        countPerDay++;

        // 0 - общая нумерация, 1 - для каждой услуги своя нумерация 
        if (ServerProps.getInstance().getProps().getNumering()) {
            return ++lastNumber;
        } else {
            return ++lastStNumber;
        }
    }

    /**
     * Иссяк лимит на одинаковые введенные данные в день по услуге или нет
     * @return true - превышен, в очередь становиться нельзя; false - можно в очередь встать
     */
    public boolean isLimitPersonPerDayOver(String data) {
        return getPersonDayLimit() != 0 && getDay() == new GregorianCalendar().get(GregorianCalendar.DAY_OF_YEAR) && getPersonDayLimit() <= getCountPersonsPerDay(data);
    }

    private int getCountPersonsPerDay(String data) {
        int cnt = 0;
        for (QCustomer customer : customers) {
            if (data.equalsIgnoreCase(customer.getInput_data())) {
                cnt++;
            }
        }
        if (getPersonDayLimit() <= cnt) {
            return cnt;
        }
        QLog.l().logger().trace("Загрузим уже обработанных кастомеров с такими же данными \"" + data + "\"");
        // Загрузим уже обработанных кастомеров
        final GregorianCalendar gc = new GregorianCalendar();
        gc.set(GregorianCalendar.HOUR, 0);
        gc.set(GregorianCalendar.MINUTE, 0);
        gc.set(GregorianCalendar.SECOND, 0);
        final Date start = gc.getTime();
        gc.add(GregorianCalendar.DAY_OF_YEAR, 1);
        final Date finish = gc.getTime();
        final List<QCustomer> custs = Spring.getInstance().getHt().find("FROM QCustomer a WHERE "
                + " start_time >'" + Uses.format_for_rep.format(start) + "' "
                + " and start_time <= '" + Uses.format_for_rep.format(finish) + "' "
                + " and  input_data = '" + data + "' "
                + " and service_id = " + getId());
        return cnt + custs.size();
    }

    /**
     * Иссяк лимит на возможных обработанных в день по услуге или нет
     * @return true - превышен, в очередь становиться нельзя; false - можно в очередь встать
     */
    public boolean isLimitPerDayOver() {
        return getDayLimit() != 0 && getDay() == new GregorianCalendar().get(GregorianCalendar.DAY_OF_YEAR) && getDayLimit() <= getCountPerDay();
    }
    /**
     * Сколько кастомеров уже прошло услугу сегодня
     */
    @Transient
    @Expose
    @SerializedName("countPerDay")
    private int countPerDay = 0;

    public void setCountPerDay(int countPerDay) {
        this.countPerDay = countPerDay;
    }

    public int getCountPerDay() {
        return countPerDay;
    }
    /**
     * Текущий день, нужен для учета количества кастомеров обработанных в этой услуге в текущий день
     */
    @Transient
    @Expose
    @SerializedName("day")
    private int day = 0;

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void clearNextNumber() {
        lastNumber = ServerProps.getInstance().getProps().getFirstNumber() - 1;
    }

    public static void clearNextStNumber() {
        lastStNumber = ServerProps.getInstance().getProps().getFirstNumber() - 1;
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
            if (CustomerState.STATE_REDIRECT != customer.getState()) {
                if (number > lastNumber) {
                    lastNumber = number;
                }
                if (number > lastStNumber) {
                    lastStNumber = number;
                }
            }
        }
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

        // поддержка расширяемости плагинами/ определим куда влез клиент
        QCustomer before = null;
        QCustomer after = null;
        for (Iterator<QCustomer> itr = getCustomers().iterator(); itr.hasNext();) {
            final QCustomer c = itr.next();
            if (!customer.getId().equals(c.getId())) {
                if (customer.compareTo(c) == 1) {
                    // c - первее, определяем before
                    if (before == null) {
                        before = c;
                    } else {
                        if (before.compareTo(c) == -1) {
                            before = c;
                        }
                    }
                } else {
                    if (customer.compareTo(c) != 0) {
                        // c - после, определяем after
                        if (after == null) {
                            after = c;
                        } else {
                            if (after.compareTo(c) == 1) {
                                after = c;
                            }
                        }
                    }
                }
            }
        }
        // поддержка расширяемости плагинами
        for (final ICustomerChangePosition event : ServiceLoader.load(ICustomerChangePosition.class)) {
            QLog.l().logger().info("Вызов SPI расширения. Описание: " + event.getDescription());
            event.insert(customer, before, after);
        }

        clients.clear();
        clients.addAll(getCustomers());
    }

    /**
     * Всего хорошего, все свободны!
     */
    public void freeCustomers() {
        // поддержка расширяемости плагинами
        for (final ICustomerChangePosition event : ServiceLoader.load(ICustomerChangePosition.class)) {
            QLog.l().logger().info("Вызов SPI расширения. Описание: " + event.getDescription());
            for (Iterator<QCustomer> itr = getCustomers().iterator(); itr.hasNext();) {
                event.remove(itr.next());
            }
        }
        getCustomers().clear();
        clients.clear();
        clients.addAll(getCustomers());
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

        // поддержка расширяемости плагинами
        for (final ICustomerChangePosition event : ServiceLoader.load(ICustomerChangePosition.class)) {
            QLog.l().logger().info("Вызов SPI расширения. Описание: " + event.getDescription());
            event.remove(customer);
        }

        clients.clear();
        clients.addAll(getCustomers());
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
        if (customer != null) {
            // поддержка расширяемости плагинами
            for (final ICustomerChangePosition event : ServiceLoader.load(ICustomerChangePosition.class)) {
                QLog.l().logger().info("Вызов SPI расширения. Описание: " + event.getDescription());
                event.remove(customer);
            }
        }

        clients.clear();
        clients.addAll(getCustomers());
        return customer;
    }

    /**
     * Удалить любого в очереди кастомера. 
     * @param customer удаляемый кастомер
     * @return может вернуть false при неудаче
     */
    public boolean removeCustomer(QCustomer customer) {
        final Boolean res = getCustomers().remove(customer);
        if (customer != null && res) {
            // поддержка расширяемости плагинами
            for (final ICustomerChangePosition event : ServiceLoader.load(ICustomerChangePosition.class)) {
                QLog.l().logger().info("Вызов SPI расширения. Описание: " + event.getDescription());
                event.remove(customer);
            }
        }
        clients.clear();
        clients.addAll(getCustomers());
        return res;
    }

    /** 
     *  Получение количества кастомеров, стоящих в очереди.
     * @return количество кастомеров в этой услуге
     */
    public int getCountCustomers() {
        return getCustomers().size();
    }

    public boolean changeCustomerPriorityByNumber(String number, int newPriority) {
        for (QCustomer customer : getCustomers()) {
            if (number.equals(customer.getPrefix() + customer.getNumber())) {
                customer.setPriority(newPriority);
                removeCustomer(customer); // убрать из очереди
                addCustomer(customer);// перепоставили чтобы очередность переинлексиловалась
                return true;
            }
        }
        return false;
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
    private LinkedList<QService> childrenOfService = new LinkedList<>();

    public LinkedList<QService> getChildren() {
        return childrenOfService;
    }

    @Override
    public void addChild(ITreeIdGetter child) {
        childrenOfService.add((QService) child);
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
        this.childrenOfService.remove(index);
    }

    @Override
    public void remove(MutableTreeNode node) {
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
