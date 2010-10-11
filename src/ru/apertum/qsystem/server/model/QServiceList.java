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

import javax.swing.DefaultListModel;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import ru.apertum.qsystem.common.Uses;

/**
 * Список услуг, обрабатываемых пользователем.
 * Класс - рулит списком услуг юзера.
 * Должен строиться для каждого юзера и он же должен отображаться в админской проге.
 * Элементы списка это QPlanService.
 * Пока пустой. Нужен для того чтобы при необходимости чот-то переопределить.
 * @author Evgeniy Egorov
 */
public class QServiceList extends DefaultListModel {

    public QPlanService getByName(String name) {
        QPlanService res = null;
        for (Object o : toArray()) {
            if (name.equals(((QPlanService) o).getName())) {
                res = (QPlanService) o;
            }
        }
        if (res == null) {
            throw new Uses.ServerException("Не найдена услуга пользователя по имени: \"" + name + "\"");
        }
        return res;
    }

    public boolean hasByName(String name) {
        QPlanService res = null;
        for (Object o : toArray()) {
            if (name.equals(((QPlanService) o).getName())) {
                res = (QPlanService) o;
            }
        }
        return res != null;
    }
    
    public Element getXML() {
        // Соберем xml дерево пользователей
        Uses.log.logger.debug("Формируется XML-список услуг пользователя.");
        // Найдем корень
        final Element rootServices = DocumentHelper.createElement(Uses.TAG_PROP_OWN_SERVS);
        for (Object o : toArray()) {
            final QPlanService plan = (QPlanService) o;
            final Element elPlan = DocumentHelper.createElement(Uses.TAG_PROP_OWN_SRV);
            elPlan.addAttribute(Uses.TAG_NAME, plan.getName());
            elPlan.addAttribute(Uses.TAG_PROP_KOEF, plan.getCoefficient().toString());
            rootServices.add(elPlan);
        }
        return rootServices;
    }
}
