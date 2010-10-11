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

import java.util.List;
import ru.apertum.qsystem.common.model.INetProperty;
import ru.apertum.qsystem.reports.common.Report;

/**
 * Паттерн Builder (Строитель)
 * Паттерн Builder является паттерном создания объектов (creational pattern). 
 * Суть его заключается в том, чтобы отделить процесс создания некоторого сложного объекта
 * от его представления. Таким образом, можно получать различные представления объекта,
 * используя один и тот же “технологический” процесс.
 * @author Evgeniy Egorov
 */
public final class QServerGetter implements IServerGetter {

    // интерфейсные объекты со свойствами
    private INetProperty netProp;

    @Override
    public INetProperty getNetProperty() {
        return netProp;
    }
    private IUsersGetter usersGetter;

    @Override
    public IUsersGetter getUsersGetter() {
        return usersGetter;
    }
    private IPoolGetter poolGetter;

    @Override
    public IPoolGetter getPoolGetter() {
        return poolGetter;
    }

    protected void setNetProperty(INetProperty netProp) {
        this.netProp = netProp;
    }

    protected void setUsersGetter(IUsersGetter usersGetter) {
        this.usersGetter = usersGetter;
    }

    protected void setPoolGetter(IPoolGetter poolGetter) {
        this.poolGetter = poolGetter;
    }
    
    private IPoolSaver poolSaver;

    public void setPoolSaver(IPoolSaver poolSaver) {
        this.poolSaver = poolSaver;
    }

    @Override
    public IPoolSaver getPoolSaver() {
        return poolSaver;
    }

    private List<Report> reports;

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }
    
    @Override
    public List<Report> getReports() {
        return reports;
    }
}
