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

import ru.apertum.qsystem.common.Uses;

/**
 * Класс Director паттерна Builder (Строитель)
 * Паттерн Builder (Строитель)
 * Класс Director делает комплексное построение продукта (в нашем случае свойства)
 * и не заботиться о том, как именно создаются его части.
 * @author Evgeniy Egorov
 * @see http://www.javenue.info/post/58
 */
public class DirectorServerProperty {

    /**
     * Класс билдера, который используется при построении свойств.
     */
    private AServerPropertyBuilder serverPropertyBuilder;

    /**
     * Инициализация билдера, именно им и будет строится структура свойств
     * @param serverPropertyBuilder передаем билдер директору.
     */
    public void setServerPropertyBuilder(AServerPropertyBuilder serverPropertyBuilder) {
        this.serverPropertyBuilder = serverPropertyBuilder;
    }

    /**
     * Выдаем результат построения свойств
     * @return IServerGetter - интерфейс получчения свойств.
     */
    public IServerGetter getServerProperty() {
        return serverPropertyBuilder.getServerGetter();
    }

    /**
     * Само констрирование свойств билдером.
     */
    public void constructServerProperty() {
        final long start = System.currentTimeMillis();
        serverPropertyBuilder.createNewServerGetter();
        serverPropertyBuilder.buildNetProperty();
        serverPropertyBuilder.buildPoolGetter();
        serverPropertyBuilder.buildUsersGetter();
        serverPropertyBuilder.buildPoolSaver();
        serverPropertyBuilder.buildReports();
        Uses.log.logger.debug("Объекты настроек готовы. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
    }
}
