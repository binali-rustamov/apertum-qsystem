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
        id = new Date().getTime();
    }
    @Expose
    @SerializedName("id")
    private Long id = new Date().getTime();

    @Id
    @Column(name = "id")
    //@GeneratedValue(strategy = GenerationType.AUTO) простаяляем уникальный номер времени создания.
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    /**
     *  АТРИБУТЫ "ОЧЕРЕДНИКА"
     *  персональный номер, именно по нему система ведет учет и управление очередниками 
     * @param number новер - целое число
     */
    @Expose
    @SerializedName("number")
    private Integer number;

    public void setNumber(Integer number) {
        this.number = number;
    }

    @Column(name = "number")
    @Override
    public int getNumber() {
        return number;
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
    @Expose
    @SerializedName("state")
    private Integer state;

    @Override
    public void setState(int state) {
        this.state = state;
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
        return state;
    }

    /**
     * создаем клиента имея только его номер в очереди. Префикс не определен, т.к. еще не знаем об услуге
     * куда его поставить. Присвоем кастомену услугу - присвоются и ее атрибуты.
     * @param number номер клиента в очереди
     */
    public ACustomer(int number) {
        this.number = number;
        id = new Date().getTime();
        setStandTime(new Date()); // действия по инициализации при постановке
        // все остальные всойства кастомера об услуге куда попал проставятся в самой услуге при помещении кастомера в нее

    }

    /**
     * Создаем клиента имея его XML-представление
     * @param elemment XML-представление кастомера
     * @deprecated 
     */
    public ACustomer(Element elemment) {
    }
    /**
     *  ПРИОРИТЕТ "ОЧЕРЕДНИКА"  
     */
    @Expose
    @SerializedName("priority")
    private Integer priority;

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Transient
    @Override
    public IPriority getPriority() {
        return new Priority(priority);
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

    /**
     * Вернет XML, описывающую кастомера.
     * @return описание кастомера в XML виде
     * @deprecated 
     */
    @Override
    public Element toXML() {
        return null;
    }
}
