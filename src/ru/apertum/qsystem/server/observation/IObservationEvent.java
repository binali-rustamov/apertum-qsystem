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

import ru.apertum.qsystem.common.model.ICustomer;
import ru.apertum.qsystem.server.model.IServiceProperty;
import ru.apertum.qsystem.server.model.IUserProperty;
import ru.apertum.qsystem.server.model.QAdvanceCustomer;

/**
 * Интерфейс событий для взаимодействия с системами видеонаблюдения.
 * Классы реализации определяются через Spring.
 * Каждый класс реализации предназначен для сопряжения с отдельной системой видеонаблюдения.
 * @author Evgeniy Egorov
 */
public interface IObservationEvent {

    /**
     * Событие регистрации клиента в очередь
     * @param customer вновь испеченый клиент
     */
    public void standCustomer(ICustomer customer);

    /**
     *
     * @param customer
     * @param user
     */
    public void inviteCustomer(ICustomer customer, IUserProperty user);

    /**
     *
     * @param customer
     * @param user
     */
    public void startCustomer(ICustomer customer, IUserProperty user);

    /**
     *
     * @param customer
     * @param user
     */
    public void finishCustomer(ICustomer customer, IUserProperty user);

    /**
     *
     * @param customer
     * @param user
     */
    public void deleteCustomer(ICustomer customer, IUserProperty user);

    /**
     *
     * @param customer
     * @param user
     * @param oldService
     * @param newService
     */
    public void redirectCustomer(ICustomer customer, IUserProperty user, IServiceProperty oldService, IServiceProperty newService);

    /**
     *
     * @param advCustomer
     */
    public void advanceCustomer(QAdvanceCustomer advCustomer);

    /**
     * 
     * @param advCustomer
     * @param customer
     */
    public void asvanceStandCustomer(QAdvanceCustomer advCustomer, ICustomer customer);
}
