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

import java.net.InetAddress;
import java.util.Date;
import org.dom4j.Element;

/**
 * @author Evgeniy Egorov
 * Получение необходимых данных для организации работы с сетью.
 */
public interface INetProperty {

    /**
     * Порт сервера системы для приемы сообщений от клиентских модулей по протоколу TCP
     * @return номер порта
     */
    public Integer getServerPort();

    /**
     * Порт клиентских модулей для приема широковещательных сообщений от сервера системы по протоколу UDP
     * @return номер порта
     */
    public Integer getClientPort();

    /**
     * Адрес машины, где работает сервер системы
     * @return сетевой адрес
     */
    public InetAddress getServerAddress();

    /**
     * Адрес машины, где выполняется клиентское приложение системы.
     * Можно возвращать всегда "localhost"
     * @return сетевой адрес
     */
    public InetAddress getClientAddress();

    /**
     * Порт вебсервера отчетов для приема запросов от клиентов по протоколу HTTP
     * @return номер порта
     */
    public Integer getWebServerPort();

    /**
     * Время начала приема заявок на постановку в очередь
     * @return время.
     */
    public Date getStartTime();

    /**
     * Время завершения приема заявок на постановку в очередь
     * @return время.
     */
    public Date getFinishTime();
    
    /**
     * Описание параметра в виде XML.
     * @return XML-элемент корень параметра
     */
    public Element getXML();
    
    /**
     * Версия БД или конфигурационного файла. Для определения совместимости и возможности вариантов ардейта.
     * @return Версия БД или конфигурационного файла.
     */
    public String getVersion();
}
