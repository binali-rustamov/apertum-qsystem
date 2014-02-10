/*
 * Copyright (C) 2014 Evgeniy Egorov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.apertum.qsystem.extra;

import java.awt.Frame;
import ru.apertum.qsystem.common.model.INetProperty;

/**
 *
 * @author Evgeniy Egorov
 */
public interface IWelcome extends IExtra {

    /**
     * Mетод который показывает модально диалог с информацией для клиентов.
     *
     * @param parent фрейм относительно которого будет модальность
     * @param netProperty свойства работы с сервером
     * @param htmlText текст для прочтения
     * @param printText текст для печати
     * @param modal модальный диалог или нет
     * @param fullscreen растягивать форму на весь экран и прятать мышку или нет
     * @param delay задержка перед скрытием диалога. если 0, то нет автозакрытия диалога
     * @return продолжат сравить кастомера в очередь или нет
     */
    public boolean showPreInfoDialog(Frame parent, INetProperty netProperty, String htmlText, String printText, boolean modal, boolean fullscreen, int delay);

    /**
     * Статический метод который показывает модально диалог ввода строки.
     *
     * @param parent фрейм относительно которого будет модальность
     * @param modal модальный диалог или нет
     * @param netProperty свойства работы с сервером
     * @param fullscreen растягивать форму на весь экран и прятать мышку или нет
     * @param delay задержка перед скрытием диалога. если 0, то нет автозакрытия диалога
     * @param caption
     * @return XML-описание результата предварительной записи, по сути это номерок. если null, то отказались от предварительной записи
     */
    public String showInputDialog(Frame parent, boolean modal, INetProperty netProperty, boolean fullscreen, int delay, String caption);

}
