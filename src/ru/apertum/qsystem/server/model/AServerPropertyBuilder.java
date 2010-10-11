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

/**
 * Паттерн Builder (Строитель)
 * Абстрактый Builder паттерна Builder (Строитель) для его создания свойств сервера.
 * @author Evgeniy Egorov
 * @see http://www.javenue.info/post/58
 */
abstract public class AServerPropertyBuilder {

    public AServerPropertyBuilder() {
    }
    protected QServerGetter serverGetter;

    public IServerGetter getServerGetter() {
        return serverGetter;
    }

    public void createNewServerGetter() {
        serverGetter = new QServerGetter();
    }

    public abstract void buildNetProperty();

    public abstract void buildUsersGetter();

    public abstract void buildPoolGetter();
    
    public abstract void buildPoolSaver();
    
    public abstract void buildReports();
}
