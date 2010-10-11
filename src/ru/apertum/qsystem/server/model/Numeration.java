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

/**
 * Для настроек нурациии из спринг.
 * Сдесь будут имеццо настройки для ведения нумерирования клиентов и формирования
 * для них индикации на табло.
 * @author Evgeniy Egorov
 */
public class Numeration {

    /**
     * Раздетитель на общем табло.
     */
    private String delimiter;

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }
    /**
     * Ограничение по максимально возможному номеру.
     */
    private Integer lastNumber;

    public Integer getLastNumber() {
        return lastNumber;
    }

    public void setLastNumber(Integer lastNumber) {
        this.lastNumber = lastNumber;
    }
    /**
     * Ограничение по минимально возможному номеру.
     */
    private Integer firstNumber;

    public Integer getFirstNumber() {
        return firstNumber;
    }

    public void setFirstNumber(Integer firstNumber) {
        this.firstNumber = firstNumber;
    }
    /**
     * 0 - общая нумерация, 1 - для каждой услуги своя нумерация 
     */
    private Integer numering;

    public Integer getNumering() {
        return numering;
    }

    public void setNumering(Integer numering) {
        this.numering = numering;
    }
    /**
     * 0 - кабинет, 1 - окно, 2 - стойка
     */
    private Integer point;

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }
    /** <!-- 0 - нет оповещения, 1 - только сигнал, 2 - сигнал + голос -->
     *   <property name="sound" value="2"/>
     */
    private Integer sound;

    public Integer getSound() {
        return sound;
    }

    public void setSound(Integer sound) {
        this.sound = sound;
    }
}
