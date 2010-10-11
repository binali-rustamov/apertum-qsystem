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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.model.IProperty;
import ru.apertum.qsystem.reports.common.Report;

/**
 * Builder паттерна Builder (Строитель) для его создания свойств сервера из файла.
 * Паттерн Builder (Строитель)
 * Билдер свойства сервера на основе файла.
 * Грузятся из XML-файла, переданного параметрически.
 * После этого создаются все классы, реализующие соответствующие интерфейсы.
 * @author Evgeniy Egorov
 * @see http://www.javenue.info/post/58
 */
public class FromFileBuilder extends AServerPropertyBuilder {

    /**
     * Конфигурационный файл, в котором содержаться все настройки
     */
    final private String configFileName;
    /**
     * Корень XML-дерева с настройками.
     */
    final private Element root;

    /**
     * Конструктор.
     * Сдесь грузится конфигурационный файл с диска.
     * @param configFileName полное имя конфигурационного файла.
     */
    public FromFileBuilder(String configFileName) throws DocumentException {
        // Параметр приезжает из конфига спринга.
        String path = configFileName.replace("\\", File.separator);
        File configFile = new File(path);
        if (!configFile.exists()) {
            path = configFileName.replace("/", File.separator);
            configFile = new File(path);
            if (!configFile.exists()) {
                throw new Uses.ServerException("Не найден файл настроек. \"" + configFileName + "\". Необходимо передать полное имя настроечного файла в качестве параметра.");
            }

        }
        this.configFileName = path;

        Uses.log.logger.debug("Настроечный файл: " + configFile.getAbsolutePath());

        final SAXReader reader = new SAXReader(true);
        final Document doc = reader.read(configFile);
        root = doc.getRootElement();
        // Атрибуты "Наименование" для связи услуг с пользователями нужно изменить заменив "_" пробелом.
        // Требование отсетствия пробелов связано с DTD, в нем нельзя использовать для связей идентификаторы с пробелами.
        // Пробелы в зависимых атрибутах "Наименование" нужно заменить "_", это для работы связи в DTD
        for (Element el : Uses.elements(root, Uses.TAG_SERVICE)) {
            el.addAttribute(Uses.TAG_PROP_NAME, el.attributeValue(Uses.TAG_PROP_NAME).replaceAll("_", " ").replaceAll("123456", ","));
        }
        for (Element el : Uses.elements(root, Uses.TAG_PROP_OWN_SRV)) {
            el.addAttribute(Uses.TAG_PROP_NAME, el.attributeValue(Uses.TAG_PROP_NAME).replaceAll("_", " ").replaceAll("123456", ","));
        }

    }

    @Override
    public void buildNetProperty() {
        try {
            serverGetter.setNetProperty(new NetProperty((Element) root.elements(Uses.TAG_PROP_CONNECTION).get(0)));
        } catch (Exception ex) {
            throw new Uses.ServerException("Не созданы сетевые настройки." + ex.toString());
        }
    }

    @Override
    public void buildUsersGetter() {
        try {
            serverGetter.setUsersGetter(new UsersProperty((Element) root.elements(Uses.TAG_PROP_USERS).get(0), Uses.TAG_PROP_USER));
        } catch (Exception ex) {
            throw new Uses.ServerException("Не созданы настройки пользователей." + ex.toString());
        }
    }

    @Override
    public void buildPoolGetter() {
        try {
            serverGetter.setPoolGetter(new PoolProperty((Element) root.elements(Uses.TAG_PROP_SERVICES).get(0), Uses.TAG_PROP_SERVICE));
        } catch (Exception ex) {
            throw new Uses.ServerException("Не созданы настройки сервисов." + ex.toString());
        }
    }

    @Override
    public void buildReports() {
        serverGetter.setReports(new ArrayList<Report>());
    }

    @Override
    public void buildPoolSaver() {
        serverGetter.setPoolSaver(new IPoolSaver() {

            private String filePath = configFileName;

            @Override
            public void save(QServicesPool pool) {


                final long start = System.currentTimeMillis();
                Uses.log.logger.info("Сохранение конфигурации.");
                final Element root = DocumentHelper.createElement("QSystem");// создаем корневой элемент для пула

                // Пробелы в зависимых атрибутах "Наименование" нужно заменить "_", это для работы связи в DTD
                final Element services = pool.getServices().getXML();
                for (Element el : Uses.elements(services, Uses.TAG_SERVICE)) {
                    el.addAttribute(Uses.TAG_PROP_NAME, el.attributeValue(Uses.TAG_PROP_NAME).replaceAll(" ", "_").replaceAll(",", "123456"));
                }
                root.add(services);
                final Element users = pool.getUserList().getXML();
                for (Element el : Uses.elements(users, Uses.TAG_PROP_OWN_SRV)) {
                    el.addAttribute(Uses.TAG_PROP_NAME, el.attributeValue(Uses.TAG_PROP_NAME).replaceAll(" ", "_").replaceAll(",", "123456"));
                }
                root.add(users);
                root.add(pool.getNetPropetry().getXML());

                // в конфигурационный файл
                final FileOutputStream fos;
                try {
                    fos = new FileOutputStream(filePath);
                } catch (FileNotFoundException ex) {
                    throw new Uses.ServerException("Не возможно создать временный файл состояния. " + ex.getMessage());
                }
                try {
                    fos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE QSystem SYSTEM \"qsystem.dtd\">\n<?xml-stylesheet type=\"text/xsl\" href=\"qsystem.xsl\"?>\n" + root.asXML()).getBytes("UTF-8"));
                    fos.flush();
                    fos.close();
                } catch (IOException ex) {
                    throw new Uses.ServerException("Не возможно сохранить изменения в поток." + ex.getMessage());
                }
                Uses.log.logger.info("Состояние сохранено. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.");
            }
        });
    }

//********************************************************************************  
//********************************************************************************
//********************************************************************************
    /**
     * Абстрактный класс - предок классов для получения настроек и свойств,
     * которые имеют итератор по коллекции свойств
     * 
     */
    abstract static class Property<T> implements Cloneable {

        private Integer curIndx;
        protected Element current; // узел, содержащий инфу по текущей проперти
        final protected Element root; // корневой узел, содержащий инфу по всем пропертям в иерархии
        private final List<Element> listProps;
        protected final Integer count;

        public Integer getCount() {
            return count;
        }
        ;

        /**
         * Метод необходим для завершения "глубокого клонирования".
         * Поле current надо переопределить клоном.
         * @return клон объекта свойств для выдачи с помощью итератора
         * @throws CloneNotSupportedException
         */
        abstract protected T getClone() throws CloneNotSupportedException;

        @SuppressWarnings("unchecked")
        public Property(Element rootProp, String tagName) {
            current = rootProp;
            root = rootProp;
            listProps = Uses.elements(rootProp, tagName);
            count = listProps.size();
            if (count == 0) {
                Uses.log.logger.warn("Список свойств пуст. Возможно необходимо иметь свойства в разделе \"" + rootProp.getName() + "\" с именем \"" + tagName + "\"");
            }

        }

        public Iterator<T> iterator() {

            return new Iterator<T>() {

                {
                    curIndx = -1;
                    current = count == 0 ? null : (Element) listProps.get(0);
                }

                @Override
                public boolean hasNext() {
                    return (count > curIndx + 1);
                }

                @Override
                public T next() {
                    current = (Element) listProps.get(++curIndx);
                    try {
                        return getClone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                /**
                 * @deprecated не реализовано
                 */
                @Override
                public void remove() {
                }
            };
        }

        /**
         * Получение наименования, нужно символ "_" заменить пробелом
         * "_" нужет для нормальной работы зависимостей, описанных в DTD
         * @return Наименование в нормальном виде
         */
        public String getName() {
            return current.attributeValue(Uses.TAG_PROP_NAME);
        }

        /**
         * @deprecated всегда возвращает null или глючит
         */
        public Object getValue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Element getXML() {
            return current;
        }
    }

//*********************************************************************************
//****************************  UsersProperty  ************************************  
    abstract static class TwinProp extends Property<IProperty> implements IProperty, Iterable<IProperty> {

        public TwinProp(Element netProp, String tagName) {
            super(netProp, tagName);
        }
    }

    static class UsersProperty extends Property<IUserProperty> implements IUsersGetter, IUserProperty {

        public UsersProperty(Element usersProp, String tagName) throws Exception {
            super(usersProp, tagName);
        }

        @Override
        protected IUserProperty getClone() throws CloneNotSupportedException {
            final UsersProperty cloned = (UsersProperty) clone();
            cloned.current = (Element) current.clone();
            return cloned;
        }

        @Override
        public String getPassword() {
            return current.attributeValue(Uses.TAG_PROP_PASSWORD);
        }

        /**
         * Возвращает объект с итератором по свойствам причастия пользователей
         * к обработки очередей.
         * Вернет null, если пользователь не ведет обработку очереди, это
         * могут быть администраторы или начальники. 
         * @throws Exception 
         */
        @Override
        public Iterator<IProperty> getUserPlan() {
            return (new TwinProp(current, Uses.TAG_PROP_OWN_SRV) {

                @Override
                protected IProperty getClone()
                        throws CloneNotSupportedException {
                    final TwinProp cloned = (TwinProp) clone();
                    cloned.current = (Element) current.clone();
                    return cloned;
                }

                @Override
                public Object getValue() {
                    return Integer.parseInt(current.attributeValue(Uses.TAG_PROP_KOEF));
                }

                @Deprecated
                @Override
                public Object getInstance() {
                    return null;
                }

                @Override
                public String toString() {
                    return getName();
                }

                @Override
                public Long getId() {
                    return new Long(-1);
                }
            }).iterator();
        }

        @Override
        public String getPoint() {
            return current.attributeValue(Uses.TAG_USER_IDENTIFIER);
        }

        @Override
        public Object getInstance() {
            try {
                return new QUser(this.getClone());
            } catch (CloneNotSupportedException ex) {
                throw new Uses.ServerException("Класс пользователя не клонируемый. " + ex.toString());
            }
        }

        @Override
        public Integer getAdressRS() {
            return Integer.parseInt(current.attributeValue(Uses.TAG_USER_ADRESS_RS));
        }

        @Override
        public Long getId() {
            return new Date().getTime();
        }

        @Override
        public Boolean getAdminAccess() {
            final String param = current.attributeValue(Uses.TAG_USER_ADMIN_ACCESS);
            return param != null && ("1".equals(param) || "true".equals(param.toLowerCase()));
        }

        @Override
        public Boolean getReportAccess() {
            final String param = current.attributeValue(Uses.TAG_USER_REPORT_ACCESS);
            return param != null && ("1".equals(param) || "true".equals(param.toLowerCase()));
        }
    }

//********************************************************************************
//****************************  PoolProperty  ************************************   
    static class PoolProperty extends Property<IServiceProperty> implements IPoolGetter, IServiceProperty {

        //protected final Element rootPoolProperty;
        public PoolProperty(Element poolProp, String tagName) throws Exception {
            super(poolProp, tagName);
        //rootPoolProperty = netProp;
        }

        @Override
        protected IServiceProperty getClone() {
            final PoolProperty cloned;
            try {
                cloned = (PoolProperty) clone();
            } catch (CloneNotSupportedException ex) {
                throw new Uses.ServerException(" Проблема с клонированием объекта при получении свойств " + ex);
            }
            cloned.current = (Element) current.clone();
            return cloned;
        }

        @Override
        public String getDescription() {
            return current.attributeValue(Uses.TAG_PROP_DESCRIPTION);
        }

        @Override
        public String getPrefix() {
            return current.attributeValue(Uses.TAG_PROP_PREFIX);
        }

        @Override
        public Object getInstance() {
            return new QService(this.getClone());
        }

        @Deprecated
        @Override
        public Long getParentId() {
            return new Long(0);
        }

        @Deprecated
        @Override
        public Long getId() {
            return new Long(0);
        }

        @Override
        public String getButtonText() {
            return current.getTextTrim();
        }

        @Override
        public IServiceProperty getRoot() {
            current = root;
            return this.getClone();
        }

        @Override
        public LinkedList<IServiceProperty> getChildren(IServiceProperty parent) {
            LinkedList<IServiceProperty> list = new LinkedList<IServiceProperty>();
            finded = null;
            getNodeByNameSevice(root, parent.getName());
            if (finded == null) {
                throw new Uses.ServerException("Не найдена услуга по имени \"" + parent.getName() + "\"");
            }
            for (Object o : finded.elements()) {
                current = (Element) o;
                list.add(this.getClone());
            }
            return list;
        }
        private Element finded;

        private void getNodeByNameSevice(Element el, String serviceName) {
            if (serviceName.equals(el.attributeValue(Uses.TAG_NAME))) {
                finded = el;
            } else {
                for (Object o : el.elements()) {
                    getNodeByNameSevice((Element) o, serviceName);
                }
            }
        }

        @Override
        public Integer getStatus() {
            return null == current.attributeValue(Uses.TAG_PROP_STATUS) || "".equals(current.attributeValue(Uses.TAG_PROP_STATUS)) ? 1 : Integer.parseInt(current.attributeValue(Uses.TAG_PROP_STATUS));
        }

        @Override
        public Integer getAdvanceLinit() {
            return null == current.attributeValue(Uses.TAG_PROP_ADVANCE_LIMIT) || "".equals(current.attributeValue(Uses.TAG_PROP_ADVANCE_LIMIT)) ? 1 : Integer.parseInt(current.attributeValue(Uses.TAG_PROP_ADVANCE_LIMIT));
        }
    }
}    
