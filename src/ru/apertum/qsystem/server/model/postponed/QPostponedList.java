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
package ru.apertum.qsystem.server.model.postponed;

import java.util.LinkedList;
import javax.swing.DefaultListModel;
import org.apache.commons.collections.CollectionUtils;
import ru.apertum.qsystem.common.model.QCustomer;

/**
 *
 * @author Evgeniy Egorov
 */
public class QPostponedList extends DefaultListModel {

    public QPostponedList loadPostponedList(LinkedList<QCustomer> customers) {
        clear();
        for (QCustomer cust : customers) {

            boolean fl = true;
            for (int i = 0; i < size(); i++) {
                final QCustomer inn = (QCustomer) get(i);
                if (inn.getPostponedStatus().compareTo(cust.getPostponedStatus()) > 0) {
                    add(i, cust);
                    fl = false;
                    break;
                }
            }
            if (fl) {
                addElement(cust);
            }

        }
        return this;
    }

    private QPostponedList() {
    }

    public static QPostponedList getInstance() {
        return QPostponedListHolder.INSTANCE;
    }

    private static class QPostponedListHolder {

        private static final QPostponedList INSTANCE = new QPostponedList();
    }

    public LinkedList<QCustomer> getPostponedCustomers() {
        final LinkedList<QCustomer> list = new LinkedList<QCustomer>();
        CollectionUtils.addAll(list, elements());
        return list;
    }
    /**
     * Может вернуть NULL если не нашлось
     * @param id
     * @return
     */
    public QCustomer getById(long id){
        for (Object object : toArray()) {
            QCustomer c = (QCustomer) object;
            if (id==c.getId()) {
                return c;
            }
        }
        return null;
    }
}
