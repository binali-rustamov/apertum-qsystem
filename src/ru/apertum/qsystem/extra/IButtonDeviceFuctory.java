/*
 * Copyright (C) 2014 Evgeniy Egorov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.apertum.qsystem.extra;

/**
 *
 * @author Evgeniy Egorov
 */
public abstract interface IButtonDeviceFuctory {
    
    public Assd sd = new Assd();
    
    public class Assd{

        public Assd() {
            System.out.println("Hi");
        }
        
        
        void Hi(String st){
            System.out.println("ss");
            
        }
        
        String ss = "asd";
    }

    public static interface IButtonDevice {
        
        public Assd sd2 = sd;

        /**
         * Приняли от устройства и что-то делаем с этим
         *
         * @param b
         */
        public void doAction(byte b);

        /**
         * Опросить устройство
         */
        public void getFeedback();

        /**
         * Сменить адрес устройству
         */
        public void changeAdress();

        /**
         * Маякнуть
         */
        public void check();

    }
    
    public IButtonDevice getButtonDevice();

}
