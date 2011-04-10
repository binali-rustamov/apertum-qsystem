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
package ru.apertum.qsystem.hibernate;

/**
 * Класс - фабрика сессий для Hibernate.
 * @author Evgeniy Egorov
 */
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.exceptions.ServerException;

/**
 * Класс - фабрика сессий для Hibernate.
 * Добавлено поле для регистрации аннорированных классов.
 * @author Evgeniy Egorov
 */
public class AnnotationSessionFactoryBean extends LocalSessionFactoryBean {

    /**
     * Поле для регистрации аннорированных классов.
     */
    private List<String> annotatedClasses_;

    /** 
     * @return the classes. 
     */
    public List getAnnotatedClasses() {
        return annotatedClasses_;
    }

    /** 
     * @param classes The classes to set. 
     */
    public void setAnnotatedClasses(List<String> classes) {
        annotatedClasses_ = classes;
    }

    /**
     * Этод метод переопределяется для регистрации аннотированных классов.
     * @param config
     * @throws org.hibernate.HibernateException
     */
    @Override
    protected void postProcessConfiguration(Configuration config) throws HibernateException {
        super.postProcessConfiguration(config);

        if (!(config instanceof AnnotationConfiguration)) {
            throw new ServerException("Конфигурация должна быть типа AnnotationConfiguration.");
        }

        if (annotatedClasses_ == null) {
            Uses.log.logger.info("Нет аннотированных классов для регистрации Hibernate.");
            return;
        }

        for (String className : annotatedClasses_) {
            try {
                Class clazz = config.getClass().getClassLoader().loadClass(className);
                ((AnnotationConfiguration) config).addAnnotatedClass(clazz);
                Uses.log.logger.debug("Класс \"" + className + "\" добавлен в конфигурацию Hibernate.");
            } catch (MappingException e) {
                throw new ServerException("Класс \"" + className + "\" не замаплен. " + e.toString());
            } catch (ClassNotFoundException e) {
                throw new ServerException("Класс \"" + className + "\" не найден. " + e.toString());
            }
        }
    }
}
