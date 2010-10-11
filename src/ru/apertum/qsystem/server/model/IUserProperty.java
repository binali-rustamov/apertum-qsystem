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

import java.util.Iterator;
import ru.apertum.qsystem.common.model.IProperty;

/**
 * Интерфейс получения свойств всех юзеров итератором
 * @author Evgeniy Egorov
 */
//, Iterable<IUserProperty>
public interface IUserProperty extends IProperty {

    /**
     * Пароль идентификации пользователя
     * @return пароль пользователя
     */
    public String getPassword();

    /**
     * Почение перечисления услуг, обрабатываемых пльзователем
     * @return класс, который может вернуть итератор
     * @throws java.lang.Exception
     */
    public Iterator<IProperty> getUserPlan();

    /**
     * Идентификация пользователя(типа, номер кабинета)
     * @return строка идентификации
     */
    public String getPoint();

    /**
     * Идентификация пользователя(типа, номер кабинета)
     * @return строка идентификации
     */
    public Integer getAdressRS();

    /**
     * Параметр доступа к администрированию системы.
     */
    public Boolean getAdminAccess();

    /**
     * Параметр доступа к отчетам системы.
     */
    public Boolean getReportAccess();
}    
