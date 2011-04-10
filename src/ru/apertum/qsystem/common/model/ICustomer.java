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

import java.util.Date;
import org.dom4j.Element;

/**
 * Интерфейс структуры, отвечающей за элемент, которым оперирует очередь.
 * @author Evgeniy Egorov
 *
 */
public interface ICustomer extends Comparable<ICustomer> {

    /**
     * Установка приоритета кастомера в очереди
     * @param priority - int, чем больше это значение, тем выше приоритет
     */
    public void setPriority(int priority);

    /**
     * Получение приоритета кастомера
     * @return IPriority - целочисленное выражение приоритета
     */
    public IPriority getPriority();

    /**
     * этод метод должен возвращать XML узел описания очередника
     * @return строка - правильный XML-документ
     */
    @Override
    public String toString();

    /**
     * этод метод должен возвращать XML - описание очередника
     * @return XML элемент, описывающий очередника
     * @deprecated 
     */
    public Element toXML();

    /**
     * Возвращает время постановки клиента в очередь
     * @return время постановки в очередь
     */
    public Date getStandTime();

    /**
     * Установливает время постановки клиента в очередь
     * @param date время когда клиент всятал в очередь
     */
    public void setStandTime(Date date);

    /**
     * Установливает время начала работы с клиентом
     * @param date время нвчала работы
     */
    public void setStartTime(Date date);

    /**
     * Возвращает время начала работы с клиентом
     * @return время начала работы с клиентом
     */
    public Date getStartTime();

    /**
     * Установливает время вызова клиента
     * @param date вревя вызова клиента
     */
    public void setCallTime(Date date);

    /**
     * Возвращает время вызова клиента
     * @return время вызова клиента
     */
    public Date getCallTime();

    /**
     * Установливает время окончания работы с клиентом
     * @param date время окончания работы
     */
    public void setFinishTime(Date date);

    /**
     * Возвращает время окончания работы с клиентом
     * @return время постановки в очередь
     */
    public Date getFinishTime();

    /**
     * Префикс услуги, к которой стоит кастомер.
     * @return Строка префикса.
     */
    public String getPrefix();

    /**
     * Название услуги, к которой стоит кастомер.
     * @return Строка названия.
     */
    public String getServiceName();

    /**
     * Описание услуги, к которой стоит кастомер.
     * @return Строка описания.
     */
    public String getServiceDescription();

    /**
     * Номер, с которым стоит кастомер.
     * @return номер int.
     */
    public int getNumber();

    /**
     * Установить состояние кастомера
     * @param state Новое состояние, например Uses.STATE_REDIRECT
     */
    public void setState(int state);

    /**
     * Получить состояние кастомера.
     * @return номер int.
     */
    public int getState();

    /**
     * Уникальный идентификатор юзера.
     * @return
     */
    public Long getId();
}
