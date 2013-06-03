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
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;
import ru.apertum.qsystem.server.Spring;

/**
 * Дерево услуг.
 * Годится для отображения в JTree.
 * Наследует DefaultTreeModel и содердит свою модель.
 * Singleton.
 * @author Evgeniy Egorov
 */
public class QServiceTree extends ATreeModel<QService> {

    public static QServiceTree getInstance() {
        return QServiceTreeHolder.INSTANCE;
    }

    @Override
    protected LinkedList<QService> load() {
        return new LinkedList<>(Spring.getInstance().getHt().findByCriteria(DetachedCriteria.forClass(QService.class).
                setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY).
                addOrder(Property.forName("seqId").asc()).
                addOrder(Property.forName("id").asc())));
    }

    private static class QServiceTreeHolder {

        private static final QServiceTree INSTANCE = new QServiceTree();
    }

    private QServiceTree() {
        super();
    }
}
