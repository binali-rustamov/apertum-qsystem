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
package ru.apertum.qsystem.server.model.calendar;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableCellRenderer;

/**
 * Ячейка сетки календаря
 * @author Evgeniy Egorov
 */
public class TableСell extends JLabel implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (column == 0) {
            // setBackground(Color.lightGray);
            switch (row) {
                case 0:
                    setText("Январь");
                    break;
                case 1:
                    setText("Февраль");
                    break;
                case 2:
                    setText("Март");
                    break;
                case 3:
                    setText("Апрель");
                    break;
                case 4:
                    setText("Май");
                    break;
                case 5:
                    setText("Июнь");
                    break;
                case 6:
                    setText("Июль");
                    break;
                case 7:
                    setText("Август");
                    break;
                case 8:
                    setText("Сентябрь");
                    break;
                case 9:
                    setText("Октябрь");
                    break;
                case 10:
                    setText("Ноябрь");
                    break;
                case 11:
                    setText("Декабрь");
                    break;
            }
            final GridLayout gl = new GridLayout(1, 1);
            final JPanel panel = new JPanel(gl);
            panel.setBorder(new BevelBorder(0, panel.getBackground(), panel.getBackground()));
            panel.add(this);
            return panel;
        } else {
            setText("");
            // залочим несуществующие даты, таблица все же прямоугольная
            if (checkDate(row, column)) {
                if (isSelected && table.hasFocus()) {
                    setBackground(((CalendarTableModel) table.getModel()).addDay(getDate(row, column), /*!hasFocus*/ !(table.getSelectedColumnCount() == 1 && table.getSelectedRowCount() == 1)) ? Color.lightGray : getWorkColor(row));
                } else {
                    setBackground(((CalendarTableModel) table.getModel()).isFreeDate(getDate(row, column)) != null ? Color.lightGray : getWorkColor(row));
                }
            } else {
                setBackground(Color.black);
            }
        }
        setOpaque(true);
        table.getColumnModel().getColumn(column).setResizable(false);
        return this;
    }

    private Color getWorkColor(int row){
       return row % 2 == 1 ? new Color(247, 247, 255) : Color.WHITE;
    }

    private static boolean checkDate(int month, int day) {
        final GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date());
        gc.set(GregorianCalendar.MONTH, month);
        gc.set(GregorianCalendar.DAY_OF_MONTH, day);
        return month == gc.get(GregorianCalendar.MONTH);
    }

    private static Date getDate(int month, int day) {
        final GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(new Date());
        gc.set(GregorianCalendar.MONTH, month);
        gc.set(GregorianCalendar.DAY_OF_MONTH, day);
        return gc.getTime();
    }
}
