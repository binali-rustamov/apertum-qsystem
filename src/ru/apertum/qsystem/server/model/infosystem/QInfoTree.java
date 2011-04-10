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
package ru.apertum.qsystem.server.model.infosystem;

import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import org.dom4j.Element;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.exceptions.ServerException;

/**
 *
 * @author Evgeniy Egorov
 */
public class QInfoTree extends DefaultTreeModel {

    private QInfoTree(QInfoItem root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
        addChildren(root);
    }

    private QInfoTree(QInfoItem root) {
        super(root);
        addChildren(root);
    }
    /**
     * Singleton.
     */
    private static QInfoTree instance = null;

    /**
     * Доступ до Singleton
     * @return класс - деерво услуг.
     */
    public static QInfoTree getInfoTree() {
        if (instance == null) {
            resetInfoTree();
        }
        return instance;
    }

    /**
     * Перегрузка принудительно. Доступ до Singleton
     * @return класс - деерво услуг.
     */
    public static QInfoTree resetInfoTree() {

        //Получение корневого элемента. Он будет самодельный, т.к. корневых элементов в информационной системе можнт быто несколько.
        // И все корневые имеют парентИД = нулл.
        final QInfoItem root = new QInfoItem();
        root.setName("Информационная система");
        root.setHTMLText("Информационная система");
        root.setId(null);
        root.setParent(null);
        instance = new QInfoTree(root);
        Uses.log.logger.debug("Создали дерево информационной системы.");
        return instance;
    }
    /**
     * Все информационные узлы
     */
    protected final List<QInfoItem> items = loadInfoItems();

    /**
     * Загрузка из базы всех информационных узлов
     * @return список всех информационных узлов выкаченных из базы
     * @throws DataAccessException
     */
    private List<QInfoItem> loadInfoItems() throws DataAccessException {

        return (List<QInfoItem>) Uses.getSessionFactory().execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                return (List<QInfoItem>) session.createCriteria(QInfoItem.class).list();
            }
        });
    }

    /**
     * Строит все дерево рекурсивно.
     * @param parent корень(для рекурсии), первый запуск с root
     */
    protected final void addChildren(QInfoItem parent) {
        for (QInfoItem item : items) {
            if (((parent.getId() == null && item.getParentId() == null) || (parent.getId() != null && item.getParentId() != null)) && (parent.getId() == item.getParentId() || parent.getId().equals(item.getParentId()))) {
                parent.insert(item, parent.getChildCount());
                addChildren(item);
            }
        }
    }
    private QInfoItem itemByName;
    private String itemByNameName;
    private int count;

    /**
     * Получение инфоузла по ее имени
     * @param name имя инфоузла. Имена инфоузлов в системе уникальны.
     * @return найденная инфоузел с требуемым и менем. Если инфоузла с таким имененм не существует, то генерируется исключение.
     */
    public QInfoItem getByName(String name) {
        itemByName = null;
        itemByNameName = name;
        go(getRoot(), 2);
        if (itemByName == null) {
            throw new ServerException("Не найден информационный узел по имени \"" + name + "\"");
        }
        return itemByName;
    }

    /**
     * Проверка наличия инфоузла по имени
     * @param name имя проверяемого инфоузла
     * @return есть или нет
     */
    public boolean hasByName(String name) {
        itemByName = null;
        itemByNameName = name;
        go(getRoot(), 2);
        return itemByName != null;
    }

    public int size() {
        count = 0;
        go(getRoot(), 1);
        return count;
    }

    /**
     * Сохранить деорево услуг в виде XML
     * @return Element - корень всех услуг.
     * @deprecated
     */
    public Element getXML() {
        final Element rootServ = getRoot().getXML();
        setXML(rootServ, getRoot());
        return rootServ;
    }

    @Deprecated
    private void setXML(Element elRoot, QInfoItem infoRoot) {
        for (QInfoItem item : infoRoot.getChildren()) {
            final Element child = item.getXML();
            elRoot.add(child);
            setXML(child, item);
        }
    }

    /**
     * Пробежка по всем инфоузлам и вызов work для каких-то действий.
     * @param parent для рекурсии.
     */
    private void go(QInfoItem parent, int regim) {
        work(parent, regim);
        for (QInfoItem service : parent.getChildren()) {
            go(service, regim);
        }
    }

    /**
     * Что-то делаем с инфоузлом при обходе всего дерева инфоузлов.
     * @param item Вот эта сейчас в фокусе.
     * @param regim определяет что делать. 1 - подсчет количества инфоузлов. 2 - поиск по имени.
     */
    private void work(QInfoItem item, int regim) {
        switch (regim) {
            case 1:
                count++;
                break;
            case 2:
                if (item.getName().equals(itemByNameName)) {
                    itemByName = item;
                }
                break;
        }
    }

    /**
     * Перебор всех услуг до одной включая корень и узлы
     * @param root
     * @param listener
     */
    public void sailToStorm(QInfoItem root, ISailListener listener) {
        seil(root, listener);
    }

    private void seil(QInfoItem parent, ISailListener listener) {
        listener.actionPerformed(parent);
        for (QInfoItem item : parent.getChildren()) {
            seil(item, listener);
        }
    }

    @Override
    public QInfoItem getRoot() {
        return (QInfoItem) super.getRoot();
    }

    @Override
    public QInfoItem getChild(Object parent, int index) {
        return (QInfoItem) ((TreeNode) parent).getChildAt(index);
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

