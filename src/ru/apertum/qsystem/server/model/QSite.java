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
import org.dom4j.Element;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.model.INetProperty;

/**
 * Класс сайта домена. Сайта хранятся тока в БД. По этому никакого XML.
 * @author Evgeniy Egorov
 */
@Entity
@Table(name = "sites")
public class QSite implements INetProperty, Serializable {

    public QSite() {
    }
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    @Column(name = "server_port")
    private Integer serverPort;

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public Integer getServerPort() {
        return serverPort;
    }
    @Column(name = "address")
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public InetAddress getServerAddress() {
        return Uses.getInetAddress(address);
    }

    @Deprecated
    @Override
    public InetAddress getClientAddress() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Column(name = "web_server_port")
    private Integer webServerPort;

    public void setWebServerPort(Integer webServerPort) {
        this.webServerPort = webServerPort;
    }

    @Override
    public Integer getWebServerPort() {
        return webServerPort;
    }

    @Deprecated
    @Override
    public Date getStartTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    @Override
    public Date getFinishTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    @Override
    public Element getXML() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean IsSuperSite() {
        return false;
    }
    @Column(name = "button_text")
    private String buttonText;

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }
    @Column(name = "description")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    @Column(name = "name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getAddress() + ":" + getServerPort();
    }

    @Deprecated
    @Override
    public Integer getClientPort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
