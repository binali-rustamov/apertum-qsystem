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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import org.dom4j.*;
import org.hibernate.Session;
import ru.apertum.qsystem.common.Uses;

/**
 * Абстракстный класс, обеспечивает работу с некоторыми основными данными
 * Абстрактный класс "очередника".
 * Содержит некий набор атрибутов и методов для получения возможности использовать его наследника в
 * менеджере очереди. 
 * @author Evgeniy Egorov
 *
 */
@MappedSuperclass
public abstract class ACustomer implements ICustomer {

    public ACustomer() {
        xmlElement = DocumentHelper.createElement(Uses.TAG_CUSTOMER);
        setId(new Date().getTime());
    }
    private Long id = new Date().getTime();

    @Id
    @Column(name = "id")
    //@GeneratedValue(strategy = GenerationType.AUTO) простаяляем уникальный номер времени создания.
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        xmlElement.addAttribute(Uses.TAG_ID, id.toString());
        this.id = id;
    }
    /**
     * ЭЛЕМЕНТ "ОЧЕРЕДНИКА"
     */
    protected final Element xmlElement;

    /**
     *  АТРИБУТЫ "ОЧЕРЕДНИКА"
     *  персональный номер, именно по нему система ведет учет и управление очередниками 
     */
    public void setNumber(Integer number) {
        xmlElement.addAttribute(Uses.TAG_NUMBER, String.valueOf(number));
    }

    @Column(name = "number")
    @Override
    public int getNumber() {
        return new Integer(Integer.parseInt(xmlElement.attributeValue(Uses.TAG_NUMBER)));
    }

    /**
     * АТРИБУТЫ "ОЧЕРЕДНИКА"
     *  состояние кастомера, именно по нему система знает что сейчас происходит с кастомером
     * Это состояние менять только если кастомер уже готов к этому и все другие параметры у него заполнены.
     * Если данные пишутся в БД, то только по состоянию завершенности обработки над ним.
     * Так что если какая-то итерация закончена и про кастомера должно занестись в БД, то как и надо выставлять что кастомер ЗАКОНЧИЛ обрабатываться,
     * а уж потом менять , если надо, его атрибуты и менять состояние, например на РЕДИРЕКТЕННОГО.
     * @param state - состояние клиента
     * @see ru.apertum.qsystem.common.Uses
     */
    @Override
    public void setState(int state) {
        xmlElement.addAttribute(Uses.TAG_STATE, String.valueOf(state));
        // сохраним кастомера в базе
        if (Uses.isDBconnected() && state == Uses.STATE_FINISH) {
            Uses.log.logger.debug("Старт сохранения кастомера с номером \"" + getPrefix() + getNumber() + "\" в СУБД при статусе \"" + state + "\"");
            //Uses.getSessionFactory().merge(this);
            Session session = Uses.getSessionFactory().getSessionFactory().openSession();
            session.beginTransaction();
            try {
                session.saveOrUpdate(this);
                session.getTransaction().commit();
                Uses.log.logger.debug("Сохранили.");
            } catch (Exception ex) {
                Uses.log.logger.error("Ошибка при сохранении \n" + ex.toString() + "\n" + ex.getStackTrace());
                session.getTransaction().rollback();
            } finally {
                session.close();
            }
        }
    }

    @Transient
    @Override
    public int getState() {
        return Integer.parseInt(xmlElement.attributeValue(Uses.TAG_STATE));
    }

    /**
     * создаем клиента имея только его номер в очереди. Префикс не определен, т.к. еще не знаем об услуге
     * куда его поставить. Присвоем кастомену услугу - присвоются и ее атрибуты.
     * @param number номер клиента в очереди
     */
    public ACustomer(int number) {
        try {
            this.xmlElement = DocumentHelper.createElement(Uses.TAG_CUSTOMER);// создаем корневой элемент для кастомера

        } catch (Exception e) {
            e.printStackTrace();
            throw new Uses.ServerException("Не создан XML-элемент для кастомера.");
        }
        setNumber(number);
        setId(new Date().getTime());
        setStandTime(new Date()); // действия по инициализации при постановке
    // все остальные всойства кастомера об услуге куда попал проставятся в самой услуге при помещении кастомера в нее

    }

    /**
     * Создаем клиента имея его XML-представление
     * @param elemment XML-представление кастомера
     */
    public ACustomer(Element elemment) {
        this.xmlElement = elemment;
        setId(Long.parseLong(elemment.attributeValue(Uses.TAG_ID)));
    }

    /**
     *  ПРИОРИТЕТ "ОЧЕРЕДНИКА"  
     */
    @Override
    public void setPriority(int priority) {
        xmlElement.addAttribute(Uses.TAG_PRIORITY, String.valueOf(priority));
    }

    @Transient
    @Override
    public IPriority getPriority() {
        return new Priority(Integer.parseInt(xmlElement.attributeValue(Uses.TAG_PRIORITY)));
    }

    /**
     *  Сравнение очередников для выбора первого. Участвует приоритет очередника.
     *  сравним по приоритету, потом по времени
     * @param customer
     * @return используется отношение "обслужится позднее"(сравнение дает ответ на вопрос "я обслужусь позднее чем тот в параметре?")
     *         1 - "обслужится позднее" чем кастомер в параметре, -1 - "обслужится раньше"  чем кастомер в параметре, 0 - одновременно
     *         -1 - быстрее обслужится чем кастомер из параметров, т.к. встал раньше
     *         1 - обслужится после чем кастомер из параметров, т.к. встал позднее
     */
    @Override
    public int compareTo(ICustomer customer) {
        int result = -1 * getPriority().compareTo(customer.getPriority()); // (-1) - т.к.  больший приоритет быстрее обслужится

        if (result == 0) {
            if (this.getStandTime().before(customer.getStandTime())) {
                result = -1;
            } else if (this.getStandTime().after(customer.getStandTime())) {
                result = 1;
            }
        }
        if (result == 0) {
            Uses.log.logger.warn("Клиенты не могут быть равны.");
            result = -1;
        }
        return result;
    }

    protected void saveAttrDate(Date date, String attrName) {
        DateFormat dateFormat = new SimpleDateFormat(Uses.DATE_FORMAT);
        xmlElement.addAttribute(attrName, dateFormat.format(date));
    }

    protected Date loadAttrDate(String attrName) throws ParseException {
        String df = xmlElement.attributeValue(attrName);
        if (df == null) {
            return null;
        }
        DateFormat dateFormat = new SimpleDateFormat(Uses.DATE_FORMAT);
        return dateFormat.parse(df);
    //java.util.Date formatedDate = dateFormat.parse(df);
    //Date date = new Date(formatedDate.getTime());
    //return date;
    }

    /**
     * Вернет XML-строку, описывающую кастомера
     */
    @Override
    public String toString() {
        return xmlElement.asXML();
    }

    /**
     * Вернет XML, описывающую кастомера.
     * @return описание кастомера в XML виде
     */
    @Override
    public Element toXML() {
        return xmlElement;
    }
}
