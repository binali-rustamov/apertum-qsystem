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
package ru.apertum.qsystem.server.observation;

import java.net.InetAddress;
import ru.apertum.qsystem.common.Uses;

/**
 * Обстрактный класс оповещение системы видеонаблюдения о событиях по UDP
 * Весь функционал работы с UDP зашит сдесь. Наследники реализовывают только методы событий.
 * @author Evgeniy Egorov
 */
public class SenderUDP implements IEventSender {

    private InetAddress adress = null;

    public String getAdress() {
        return adress.getHostAddress();
    }

    public int getPort() {
        return port;
    }
    private int port = -1;

    @Override
    public void send(String message, int port) {
        Uses.log.logger.debug("Отправка события в систему видеонаблюдения");
        if (adress == null || port == -1) {
            throw new Uses.ServerException("Не определены параметры отправки UDP сообщений в систему видеонаблюдения.");
        }
        Uses.sendUDPMessage(message, adress, port);
    }

    @Override
    public void setAdress(String adress) {
        this.adress = Uses.getInetAddress(adress);
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }
}
