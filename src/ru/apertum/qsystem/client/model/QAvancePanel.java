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
package ru.apertum.qsystem.client.model;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import ru.apertum.qsystem.common.Uses;

/**
 * Класс панели для нажатия при выборе время предварительной записи.
 * @author Evgeniy Egorov
 */
public class QAvancePanel extends QPanel {

    private final JLabel label = new JLabel();
    private final IAdviceEvent event;
    private final Date data;

    public QAvancePanel(IAdviceEvent advanceEvent, Date adviceDate, final boolean enable) {
        event = advanceEvent;
        data = adviceDate;
        //Элементы на ячейке
        setBorder(BorderFactory.createMatteBorder(0, 1, 1, 0, Color.blue));
        setLayout(new GridLayout(1, 1));
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setText("<html><b><p align=center><span style='font-size:12.0pt;color:black'>" + ( "00:00".equals(Uses.format_HH_mm.format(data)) ? "24:00" : Uses.format_HH_mm.format(data) ) + "</span><br/><span style='font-size:13.0pt;color:" + (enable ? "green'>Свободно" : "red'>Занято"));
        add(label);
        //Реакция на нажатие мышки
        addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (enable && evt.getClickCount() == 1) {
                    event.eventPerformed(data);
                }
            }
        });
        //Градиент
        setStartPoint(new Point(30, 0));
        setEndPoint(enable ? new Point(400, 400) :  new Point(80, 10));
        setStartColor(Color.white);
        final GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(data);
        if (gc.get(GregorianCalendar.DAY_OF_WEEK) == 1 || gc.get(GregorianCalendar.DAY_OF_WEEK) == 7) {
            setEndColor(Color.red);
        } else {
            setEndColor(Color.green);
        }
        if (!enable) {
            setEndColor(Color.DARK_GRAY);
        }
        setGradient(true);

    }

    public void setCaption(String caption) {
        label.setText(caption);
    }
}
