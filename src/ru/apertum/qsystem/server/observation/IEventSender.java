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

/**
 * Интерфейс отправки сообщений для системы видеонаблюдения
 * @author Evgeniy Egorov
 */
public interface IEventSender {
    /**
     * Отослать сообщение
     * @param message отсылаемые данные.
     * @param port соответствующий порт соответствующей видеокамеры
     */
    public void send(String message, int port);
    /**
     * Установить параметры взаимодействия
     * @param params некое хранилище параметров, определить в классах реализации
     */
    public void setAdress(String param);
    /**
     * Установить параметры взаимодействия
     * @param params некое хранилище параметров, определить в классах реализации
     */
    public void setPort(int param);

}
