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

import java.io.Serializable;
import java.util.Date;
import java.text.ParseException;
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
     * @param service
     */
    public void setService(QService service) {
        this.service = service;
        xmlElement.addAttribute(Uses.TAG_SERVICE, service.getName());
        xmlElement.addAttribute(Uses.TAG_DESCRIPTION, service.getDescription());
        // Префикс для кастомера проставится при его создании, один раз и на всегда.
        if (xmlElement.attributeValue(Uses.TAG_PREFIX) == null) {
            xmlElement.addAttribute(Uses.TAG_PREFIX, service.getPrefix());
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
     * @param elemment XML-представление кастомера
     */
    public QCustomer(Element element) {
        super(element);
        Uses.log.logger.debug("Создали кастомера по его описанию\n" + element.asXML());
    }

    /**
     * Префикс услуги, к которой стоит кастомер.
     * @return Строка префикса.
     */
    @Column(name = "service_prefix")
    @Override
    public String getPrefix() {
        return xmlElement.attributeValue(Uses.TAG_PREFIX);
    }

    public void setPrefix(String prefix) {
        xmlElement.addAttribute(Uses.TAG_PREFIX, prefix == null ? "" : prefix);
    }

    /**
     * Название услуги, к которой стоит кастомер.
     * @return Строка названия.
     */
    @Transient
    @Override
    public String getServiceName() {
        return xmlElement.attributeValue(Uses.TAG_SERVICE);
    }

    /**
     * Описание услуги, к которой стоит кастомер.
     * @return Строка описания.
     */
    @Transient
    @Override
    public String getServiceDescription() {
        return xmlElement.attributeValue(Uses.TAG_DESCRIPTION);
    }

    @Column(name = "stand_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Override
    public Date getStandTime() {
        try {
            return loadAttrDate(Uses.TAG_STAND_TIME);
        } catch (ParseException ex) {
            throw new Uses.ServerException("Проблема с преобразованием даты в строку. " + ex.getMessage());
        }
    }

    @Override
    public void setStandTime(Date date) {
        saveAttrDate(date, Uses.TAG_STAND_TIME);
    }

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Override
    public Date getStartTime() {
        try {
            return loadAttrDate(Uses.TAG_START_TIME);
        } catch (ParseException ex) {
            throw new Uses.ServerException("Проблема с преобразованием даты в строку. " + ex.getMessage());
        }
    }

    @Override
    public void setStartTime(Date date) {
        saveAttrDate(date, Uses.TAG_START_TIME);
    }

    @Override
    public void setCallTime(Date date) {
        saveAttrDate(date, Uses.TAG_CALL_TIME);
    }

    @Transient
    @Override
    public Date getCallTime() {
        try {
            return loadAttrDate(Uses.TAG_CALL_TIME);
        } catch (ParseException ex) {
            throw new Uses.ServerException("Проблема с преобразованием даты в строку. " + ex.getMessage());
        }
    }

    @Column(name = "finish_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Override
    public Date getFinishTime() {
        try {
            return loadAttrDate(Uses.TAG_FINISH_TIME);
        } catch (ParseException ex) {
            throw new Uses.ServerException("Проблема с преобразованием даты в строку. " + ex.getMessage());
        }
    }

    @Override
    public void setFinishTime(Date date) {
        saveAttrDate(date, Uses.TAG_FINISH_TIME);
    }

    /**
     * Введенные кастомером данные на пункте регистрации.
     */
    @Column(name = "input_data")
    public String getInput_data() {
        return xmlElement.attributeValue(Uses.TAG_INPUT_DATA);
    }

    public void setInput_data(String input_data) {
        xmlElement.addAttribute(Uses.TAG_INPUT_DATA, input_data);
    }
}
