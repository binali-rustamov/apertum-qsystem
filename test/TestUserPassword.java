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

import org.junit.BeforeClass;
import org.junit.Test;
import ru.apertum.qsystem.server.model.QUser;
import static org.junit.Assert.*;

/**
 * Тестируем юзеров.
 * @author Evgeniy Egorov
 */
public class TestUserPassword {
    
    public static QUser user = new QUser();
    public static String pass = "ая-АЯ190 №;%:?-+/\\!*_az AZ";
    
    @BeforeClass
    public static void setUp() throws Exception {
        // иницализируем
        user.recoverAccess(pass);
    }
    
    @Test
    public void testCompareCustomers() throws Exception {
        user.setPassword( user.getPassword());
        assertTrue("Получили пароль, снова его туда загнали, ставнили", user.isCorrectPassword(pass) );
        pass = "";
        user.recoverAccess(pass);
        user.setPassword( user.getPassword());
        assertTrue("Теперь с пустой строкой.", user.isCorrectPassword(pass) );
        pass = "1";
        user.recoverAccess(pass);
        user.setPassword( user.getPassword());
        assertTrue("Теперь с пустой строкой.", user.isCorrectPassword(pass) );
    }

}