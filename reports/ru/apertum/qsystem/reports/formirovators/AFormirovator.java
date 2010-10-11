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
package ru.apertum.qsystem.reports.formirovators;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRDataSource;

/**
 * Формирует источник данных для отчета.
 * Сделан для удобства. чтоб не делать каждый раз ненужные методы.
 * @author Evgeniy Egorov
 */
abstract public class AFormirovator implements IFormirovator {
    
    /** 
     * Получение источника данных для отчета.
     * @return Готовая структура для компилирования в документ.
     */
    @Override
    public JRDataSource getDataSource(String driverClassName, String url, String username, String password, String inputData){
        return null;
    }
  
    /**
     * Метод формирования параметров для отчета.
     * В отчет нужно передать некие параметры. Они упаковываются в Мар.
     * Если параметры не нужны, то сформировать пустой Мар. Иначе перекрыть и сформировать Map с параметрами.
     * Перекрыть при необходимости.
     * @return
     */
    @Override
    public Map getParameters(String driverClassName, String url, String username, String password, String inputData) {
        return new HashMap();
    }

    /**
     * Метод получения коннекта к базе если отчет строится через коннект.
     * Если отчет строится не через коннект, а формироватором, то выдать null.
     * Перекрыть при необходимости.
     * @return коннект соединения к базе или null.
     */
    @Override
    public Connection getConnection(String driverClassName, String url, String username, String password, String inputData) {
        return null;
    }
}
