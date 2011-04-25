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

import java.util.LinkedList;
import javax.swing.AbstractListModel;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.apertum.qsystem.server.Spring;
import ru.apertum.qsystem.server.controller.IServerListener;
import ru.apertum.qsystem.server.controller.ServerEvents;

/**
 *
 * @param <T>
 * @author Evgeniy Egorov
 */
public abstract class ATListModel<T extends IidGetter> extends AbstractListModel/*DefaultListModel*/ {

    protected ATListModel() {
        createList();
        ServerEvents.getInstance().registerListener(new IServerListener() {

            @Override
            public void restartEvent() {
                createList();
            }
        });
    }
    private LinkedList<T> items;

    protected abstract LinkedList<T> load();

    private void createList() {
        items = load();
        QLog.l().logger().info("Создали список.");
    }

    public LinkedList<T> getItems() {
        return items;
    }

    public T getById(long id) throws ServerException {
        for (T item : items) {
            if (id == item.getId()) {
                return item;
            }
        }
        throw new ServerException("Не найден элемент по ID: \"" + id + "\"");
    }

    public boolean hasById(long id) {
        for (T item : items) {
            if (id == item.getId()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasByName(String name) {
        for (T item : items) {
            if (name != null && name.equals(item.getName())) {
                return true;
            }
        }
        return false;
    }

    protected final LinkedList<T> deleted = new LinkedList<T>();

    public boolean removeElement(T obj) {
        deleted.add(obj);
        final int index = items.indexOf(obj);
        final boolean res = items.remove(obj);
        fireIntervalRemoved(this, index, index);
        return res;
    }

    public void addElement(T obj) {
        final int index = items.size();
        items.add(obj);
        fireIntervalAdded(this, index, index);
    }

    public void clear() {
        final int index1 = items.size() - 1;

        deleted.addAll(items);
        items.clear();

        if (index1 >= 0) {
            fireIntervalRemoved(this, 0, index1);
        }
    }

    @Override
    public T getElementAt(int index) {
        return items.get(index);
    }

    @Override
    public int getSize() {
        return items.size();
    }

    public void save() {
        Spring.getInstance().getHt().deleteAll(deleted);
        deleted.clear();
        Spring.getInstance().getHt().saveOrUpdateAll(items);
    }
}
