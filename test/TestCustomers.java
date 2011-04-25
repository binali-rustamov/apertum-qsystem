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

import java.util.Date;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import ru.apertum.qsystem.common.model.QCustomer;
import static org.junit.Assert.*;

/**
 * Тестируем работу класса кастомера. Проверяеся сравнение.
 * @author Evgeniy Egorov
 */
public class TestCustomers {

    public static QCustomer c1 = new QCustomer(1);
    public static QCustomer c2 = new QCustomer(2);

    /**
     * новая анотация @BeforeClass, раньше метод инициализации носил имя protected void setUp() throws Exception {},
     * теперь указал анотации и называй как хочешь.
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        // иницализируем
        c1.setStandTime(new Date());
        c1.setPriority(1);
        Thread.sleep(1100);
        c2.setStandTime(new Date());
        c2.setPriority(1);
    }

    /**
     * новая анотация @Test, раньше тестовый метод (до v.4.0) начинался с testxxx,
     * сейчас указал анотацию и называй как хочешь
     * Аннотация @Ignore("this regular expression isn't working yet") - Игнорирование теста
     * используется отношение "обслужится позднее"(сравнение дает ответ на вопрос "я обслужусь позднее чем тот в параметре?")
     * @throws java.lang.Exception
     */
    @Test
    public void testCompareCustomers() throws Exception {
        // собственно тест
        //используется отношение "обслужится позднее"(сравнение дает ответ на вопрос "я обслужусь позднее чем тот в параметре?")
        assertTrue("Приоритет один, время разное", c1.compareTo(c2) == -1);
        assertTrue("Приоритет один, время разное", c2.compareTo(c1) == 1);

        c1.setPriority(2);
        c2.setPriority(1);
        assertTrue("Приоритет разный, время разное", c2.compareTo(c1) == 1);
        assertTrue("Приоритет разный, время разное", c1.compareTo(c2) == -1);

        c1.setPriority(1);
        c2.setPriority(2);
        assertTrue("Приоритет разный, время разное", c2.compareTo(c1) == -1);
        assertTrue("Приоритет разный, время разное", c1.compareTo(c2) == 1);
    /*
    c1.setPriority(1);
    c2.setPriority(1);
    c1.setStandTime();
    c2.setStandTime();
    assertTrue("Приоритет один, время одно", c2.compareTo(c1) == 0);
    assertTrue("Приоритет один, время одно", c1.compareTo(c2) == 0);
     */
    }

    @Ignore("this regular expression isn't working yet")
    @Test
    public void test1() throws Exception {
        // собственно тест
        assertTrue("Pattern did not validate zip code", true);

    }
}