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

import ru.apertum.qsystem.common.model.IProperty;

/**
 * @author Evgeniy Egorov
 * Интерфейс получения настроек пула очередей.
 * Возвращает Итератор по списку услуг, по которым можно получить наименование и описание
 *
 */
public interface IServiceProperty extends IProperty {

    /**
     * Описение услуги
     * @return строка описания
     */
    public String getDescription();

    /**
     * Префикс услуги
     * @return строка префикса
     */
    public String getPrefix();

    /**
     * Идентификатор вложенности.
     * Нужен для построения дерева услуг при чтении из базы.
     * @return Идентификатор вложенности
     */
    public Long getParentId();
    
    /**
     * Надпись на кнопке выбора этой услуги.
     * Имеет формат HTML.
     * @return строка надписи.
     */
    public String getButtonText();

    /**
     * Нужно для Формирования списка услуг
     * @return Наименование услуги.
     */
    @Override
    public String toString();
    
    /**
     * Состояние услуги
     * @return -1 - невидима, 0 - недоступна, 1 - активна
     */
    public Integer getStatus();

    /**
     * Ограничение по количеству предварительно регистрировшихся в час
     * @return 1 - по умолчанию в базе
     */
    public Integer getAdvanceLinit();
    
}
