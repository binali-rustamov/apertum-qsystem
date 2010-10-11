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

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import org.dom4j.Element;
import ru.apertum.qsystem.common.Uses;

/**
 * Дерево услуг.
 * Годится для отображения в JTree.
 * Наследует DefaultTreeModel и содердит свою модель.
 * Singleton.
 * @author Evgeniy Egorov
 */
public class QServiceTree extends DefaultTreeModel {

    private QServiceTree(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
    }

    private QServiceTree(TreeNode root) {
        super(root);
    }
    /**
     * Singleton.
     */
    private static QServiceTree instance = null;

    /**
     * Доступ до Singleton
     * @param poolGetter свойства услуг, по которым построится дерево услуг.
     * @return класс - деерво услуг.
     */
    public static QServiceTree getServiceTree(IPoolGetter poolGetter) {
        if (instance == null) {
            resetServiceTree(poolGetter);
        }
        return instance;
    }

    /**
     * Перегрузка принудительно. Доступ до Singleton
     * @param poolGetter свойства услуг, по которым построится дерево услуг.
     * @return класс - деерво услуг.
     */
    public static QServiceTree resetServiceTree(IPoolGetter poolGetter) {

        IServiceProperty serv = poolGetter.getRoot();
        final QService root = serv instanceof QService ? (QService) serv : new QService(serv);
        root.setParent(null);
        for (IServiceProperty serviceProperty : poolGetter.getChildren(root)) {
            final QService childService = serviceProperty instanceof QService ? (QService) serviceProperty : new QService(serviceProperty);
            root.insert(childService, root.getChildCount());
            addChildren(childService, poolGetter);
        }
        instance = new QServiceTree(root);
        Uses.log.logger.debug("Создали дерево услуг.");

        return instance;
    }

    protected static void addChildren(QService parent, IPoolGetter poolGetter) {
        for (IServiceProperty serviceProperty : poolGetter.getChildren(parent)) {
            final QService childService = serviceProperty instanceof QService ? (QService) serviceProperty : new QService(serviceProperty);
            parent.insert(childService, parent.getChildCount());
            addChildren(childService, poolGetter);
        }
    }
    private QService serviceByName;
    private String serviceByNameName;

    /**
     * Получение услуги по ее имени
     * @param name имя получаемой услуги. Имена услуг в системе уникальны.
     * @return найденная услуга с требуемым и менем. Если услуги с таким имененм не существует, то генерируется исключение.
     */
    public QService getByName(String name) {
        serviceByName = null;
        serviceByNameName = name;
        go(getRoot(), 2);
        if (serviceByName == null) {
            throw new Uses.ServerException("Не найдена услуга по имени \"" + name + "\"");
        }
        return serviceByName;
    }

    /**
     * Проверка наличия услуги по имени
     * @param name имя проверяемой услуги
     * @return есть или нет
     */
    public boolean hasByName(String name) {
        serviceByName = null;
        serviceByNameName = name;
        go(getRoot(), 2);
        return serviceByName != null;
    }
    private int count;

    public int size() {
        count = 0;
        go(getRoot(), 1);
        return count;
    }

    /**
     * Сохранить деорево услуг в виде XML
     * @return Element - корень всех услуг.
     */
    public Element getXML() {
        final Element rootServ = getRoot().getXML();
        setXML(rootServ, getRoot());

        return rootServ;
    }

    private void setXML(Element elRoot, QService servRoot) {
        for (QService service : servRoot.getChildren()) {
            final Element child = service.getXML();
            elRoot.add(child);
            setXML(child, service);
        }
    }

    /**
     * Пробежка по всем услугам и вызов work для каких-то действий.
     * @param parent для рекурсии.
     */
    private void go(QService parent, int regim) {
        work(parent, regim);
        for (QService service : parent.getChildren()) {
            go(service, regim);
        }
    }

    /**
     * Что-то делаем с услугой при обходе всего дерева услуг.
     * @param service Вот эта сейчас в фокусе.
     * @param regim определяет что делать. 1 - подсчет количества услуг. 2 - поиск по имени.
     */
    private void work(QService service, int regim) {
        switch (regim) {
            case 1:
                count++;
                break;
            case 2:
                if (service.getName().equals(serviceByNameName)) {
                    serviceByName = service;
                }
                break;
        }
    }

    /**
     * Перебор всех услуг до одной включая корень и узлы
     */
    public void sailToStorm(QService root, ISailListener listener) {
        seil(root, listener);
    }

    private void seil(QService parent, ISailListener listener) {
        listener.actionPerformed(parent);
        for (QService service : parent.getChildren()) {
            seil(service, listener);
        }
    }

    @Override
    public QService getRoot() {
        return (QService) super.getRoot();
    }

    @Override
    public QService getChild(Object parent, int index) {
        return (QService) ((TreeNode) parent).getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((TreeNode) parent).getChildCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((TreeNode) node).isLeaf();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((TreeNode) parent).getIndex((TreeNode) child);
    }
}
