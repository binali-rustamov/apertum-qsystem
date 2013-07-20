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

package ru.apertum.qsystem.server;

import ru.apertum.qsystem.server.model.QNet;
import ru.apertum.qsystem.server.model.QStandards;

/**
 *
 * @author Evgeniy Egorov
 */
public class ServerProps {

    private final QNet netProp = new QNet();
    private final QStandards standards = new QStandards();

    public QNet getProps() {
        return netProp;
    }

    public QStandards getStandards() {
        return standards;
    }

    private ServerProps() {
        Spring.getInstance().getHt().load(netProp, new Long(1));
        Spring.getInstance().getHt().load(standards, new Long(1));
    }

    public static ServerProps getInstance() {
        return ServerPropsHolder.INSTANCE;
    }

    private static class ServerPropsHolder {
        private static final ServerProps INSTANCE = new ServerProps();
    }
 }
