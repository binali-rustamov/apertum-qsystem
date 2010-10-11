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
 * Интерфейс, обеспечивающий все необходимые методы для инициализации работы при старте.
 * @author Evgeniy Egorov
 */
public interface IServerGetter {

    /**
     * Получение сетевых параметров
     * @return
     */
    public INetProperty getNetProperty();

    /**
     * Получение параметров пользователей сисатемы
     * @return
     */
    public IUsersGetter getUsersGetter();

    /**
     * Получение параметров описыавающих услуги системы
     * @return
     */
    public IPoolGetter getPoolGetter();
    
    /**
     * Класс, ответственный за сохранение конфигурации системы. Юзеры, сеть, услуги и прочее.
     * @return IPoolSaver
     */
    public IPoolSaver getPoolSaver();
    
    /**
     * Нужно получить список аналитических отчетов при использовании БД. Иначе список должен возвращаться пустой.
     * @return список аналитических отчетов.
     */
    public List<Report> getReports();
}
