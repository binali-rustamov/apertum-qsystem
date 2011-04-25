/*
 *  Copyright (C) 2010 {Apertum}Projects. web: www.apertum.ru email: info@apertum.ru
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
import javax.swing.AbstractListModel;

/**
 * Список услуг, обрабатываемых пользователем.
 * Класс - рулит списком услуг юзера.
 * Должен строиться для каждого юзера и он же должен отображаться в админской проге.
 * Элементы списка это QPlanService.
 * Пока пустой. Нужен для того чтобы при необходимости чот-то переопределить.
 * @author Evgeniy Egorov
 */
public class QPlanServiceList extends AbstractListModel {

    private final List<QPlanService> services;

    public QPlanServiceList(List<QPlanService> services) {
        this.services = services;
    }

    @Override
    public int getSize() {
        return services.size();
    }

    @Override
    public Object getElementAt(int index) {
        return services.get(index);
    }

    public boolean removeElement(QPlanService obj) {
        final int index = services.indexOf(obj);
        final boolean res = services.remove(obj);
        fireIntervalRemoved(this, index, index);
        return res;
    }

    public void addElement(QPlanService obj) {
        final int index = services.size();
        services.add(obj);
        fireIntervalAdded(this, index, index);
    }
}
