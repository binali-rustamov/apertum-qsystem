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

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.model.INetProperty;

/**
 * Сетевые настройки системы.
 * Класс работает как с XML, так и с hibernate.
 * @author Evgeniy Egorov
 */
@Entity
@Table(name = "net")
public class NetProperty implements INetProperty, Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
    /**
     * Порт сервера для приема команд.
     */
    @Column(name = "server_port")
    private Integer serverPort;

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public Integer getServerPort() {
        return serverPort;
    }
    /**
     * Порт сервера через который передается web содержимое отчетов.
     */
    @Column(name = "web_server_port")
    private Integer webServerPort;

    public void setWebServerPort(Integer webServerPort) {
        this.webServerPort = webServerPort;
    }

    @Override
    public Integer getWebServerPort() {
        return webServerPort;
    }
    /**
     * UDP Порт клиента, на который идет рассылка широковещательных пакетов.
     */
    @Column(name = "client_port")
    private Integer clientPort;

    public void setClientPort(Integer clientPort) {
        this.clientPort = clientPort;
    }

    @Override
    public Integer getClientPort() {
        return clientPort;
    }
    /**
     * Время начала приема заявок на постановку в очередь
     */
    @Column(name = "start_time")
    @Temporal(javax.persistence.TemporalType.TIME)
    private Date startTime;

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }
    /**
     * Время завершения приема заявок на постановку в очередь
     */
    @Column(name = "finish_time")
    @Temporal(javax.persistence.TemporalType.TIME)
    private Date finishTime;

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    @Override
    public Date getFinishTime() {
        return finishTime;
    }
    /**
     * Адрес сервера.
     */
    @Transient
    private final InetAddress serverAddress = Uses.getInetAddress("127.0.0.1");
    /**
     * Адрес клиентта.
     */
    @Transient
    private final InetAddress clientAddress = null;

    public NetProperty() {
    }

    @Deprecated
    public NetProperty(Element netProp) throws Exception {
        // выберем параметры коннекта
        serverPort = Integer.parseInt(netProp.attributeValue(Uses.TAG_PROP_SERV_PORT));
        webServerPort = Integer.parseInt(netProp.attributeValue(Uses.TAG_PROP_WEB_SERV_PORT));
        clientPort = Integer.parseInt(netProp.attributeValue(Uses.TAG_PROP_CLIENT_PORT));
        startTime = Uses.format_HH_mm.parse(netProp.attributeValue(Uses.TAG_PROP_START_TIME));
        finishTime = Uses.format_HH_mm.parse(netProp.attributeValue(Uses.TAG_PROP_FINISH_TIME));
        version = netProp.attributeValue(Uses.TAG_PROP_VERSION) == null ? "Не присвоена" : netProp.attributeValue(Uses.TAG_PROP_VERSION);
        /* Не используется.
        serverAddress = InetAddress.getByName(netProp.attributeValue(Uses.TAG_PROP_SERV_ADDRESS));
        clientAddress = InetAddress.getByName(netProp.getAttributeNode(Uses.TAG_PROP_CLIENT_ADDRESS).getNodeValue());
         */
    }

    public NetProperty(INetProperty netProp) {
        // выберем параметры коннекта
        this.serverPort = netProp.getServerPort();
        this.webServerPort = netProp.getWebServerPort();
        this.clientPort = netProp.getClientPort();
        this.startTime = netProp.getStartTime();
        this.finishTime = netProp.getFinishTime();
        this.version = netProp.getVersion();
    }

    /**
     * @deprecated не требуется
     * @return null
     */
    @Override
    public InetAddress getClientAddress() {
        return clientAddress;
    }

    /**
     * @return локальный адрес.
     */
    @Override
    public InetAddress getServerAddress() {
        return serverAddress;
    }

    @Deprecated
    @Override
    public Element getXML() {
        //<Сеть ПортСервера="3128" ПортВебСервера="8080" ПортКлиента="3129" АдресСервера="localhost" ВремяНачалаРаботы="8:45" ВремяЗавершенияРаботы="20:45"/>

        final Element net = DocumentHelper.createElement(Uses.TAG_PROP_CONNECTION);
        net.addAttribute(Uses.TAG_PROP_SERV_PORT, getServerPort().toString());
        net.addAttribute(Uses.TAG_PROP_WEB_SERV_PORT, getWebServerPort().toString());
        net.addAttribute(Uses.TAG_PROP_CLIENT_PORT, getClientPort().toString());
        net.addAttribute(Uses.TAG_PROP_SERV_ADDRESS, "127.0.0.1");
        net.addAttribute(Uses.TAG_PROP_START_TIME, Uses.format_HH_mm.format(getStartTime()));
        net.addAttribute(Uses.TAG_PROP_FINISH_TIME, Uses.format_HH_mm.format(getFinishTime()));
        net.addAttribute(Uses.TAG_PROP_VERSION, getVersion());

        return net;
    }
    /**
     *Версия БД или конфигурационного файла. Для определения совместимости и возможности вариантов ардейта.
     */
    @Column(name = "version")
    private String version = "Не присвоена";

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
