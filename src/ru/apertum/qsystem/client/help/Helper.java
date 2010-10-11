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
package ru.apertum.qsystem.client.help;

import java.net.URL;
import java.util.HashMap;
import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.AbstractButton;
import javax.swing.JComponent;

/**
 * Организация помощи.
 * Менеджер системы помощи JavaHelp.
 * Позволяет привязывать помощь и работать с ней. Все классы разных конфигураций хранит в Мар.
 * Каждый Helper привязан к одной конфигурации и все может предоставить для работы с ней.
 * @author Evgeniy Egorov
 */
public class Helper {

    /**
     * Список хелперов, чтоб не создавать заново каждый раз. Своего рода Singleton, только для каждого описания ресурса хелпа hs
     * имя ресурса HelpSet -> хелпер
     */
    private static final HashMap<String, Helper> helpers = new HashMap<String, Helper>();

    /**
     * Получение класса-помошника. 
     * Этот класс должен работать с одной конфигурацией помощи и предоставлять все необходимые методы.
     * Все хелперы складываются в мар и достаются от туда по необходимости.
     * @return Helper - класс-помишник
     */
    public static Helper getHelp(String helpSetRes) {
        Helper helper = helpers.get(helpSetRes);
        if (helper == null) {
            helper = new Helper(helpSetRes);
            helpers.put(helpSetRes, helper);
        }
        return helper;
    }
    /**
     * Имя ресурса HelpSet
     */
    private final String helpSetRes;
    /**
     * Набор параметров для организации помощи
     */
    private final HelpSet helpSet;
    /**
     * Брокер помощи. Позволяет привязывать контекстныю помощь к контролам.
     */
    private final HelpBroker broker;

    /**
     * Создаем хелпер и все необходимое для его работы.
     * @param loader
     * @param helpSetRes
     */
    public Helper(String helpSetRes) {
        this.helpSetRes = helpSetRes;
        try {
            URL hsURL = HelpSet.findHelpSet(Helper.class.getClassLoader(), helpSetRes);
            this.helpSet = new HelpSet(null, hsURL);
        } catch (Exception ex) {
            // Say what the exception really is
            throw new RuntimeException("Ошибка формирования параметров справочной системы. ",  ex);
        }
        this.broker = helpSet.createHelpBroker();
    }

    /**
     * Оказание общей помощи. Той что для профилактики.
     * Привязка показа помощи к кнопку или пункту меню.
     * @param button К этой кнопке или пункту меню будем привязывать вызов помощи.
     */
    public void setHelpListener(AbstractButton button) {
        button.addActionListener(new CSH.DisplayHelpFromSource(broker));
    }
    /**
     * Оказание первой медицинской помощи. Той что до морга.
     * Привязка показа контекстной помощи к контролу.
     * @param component контрол, к которому привязывается контекстная помощь
     * @param tagID идентификатор привязко определенной html-странички. Идентификаторы описываются в Map.xml
     */
    public void enableHelpKey(JComponent component, String tagID){
        broker.enableHelpKey(component, tagID, helpSet);
    }
}
