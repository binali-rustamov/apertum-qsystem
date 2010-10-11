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
package ru.apertum.qsystem.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Утилита изменения Spring-контекста в инсталлированном приложении
 * Класс изменения Spring-контекста в инсталлированном приложении.
 * Консольный классик простого редактирования XML-файла
 * @author Evgeniy Egorov
 */
public class ChangeContext {

    static private HashMap<String, Element> elBeans = new HashMap<String, Element>();
    static private HashMap<String, Element> elProps = new HashMap<String, Element>();

    public static void main(String args[]) throws DocumentException, IOException {
        final SAXReader reader = new SAXReader(false);
        final Element root = reader.read(args[0]).getRootElement();
        String str = "asd";
        while (!"".equals(str)) {
            str = getBeans(root);
            final Element bean = elBeans.get(str);
            if (bean != null) {
                str = "asd";
                // редактируем бин.
                while (!"".equals(str)) {
                    str = getBean(bean);
                    final Element prop = elProps.get(str);
                    if (prop != null) {
                        // редактируем проперти.
                        System.out.println();
                        System.out.println(prop.attributeValue("index") == null ? "property[" + prop.attributeValue("name") + "]" : "constructor-arg[" + prop.attributeValue("index") + "]");
                        System.out.println("Old value: " + prop.attributeValue("value"));
                        System.out.print("New value: ");
                        prop.addAttribute("value", read());
                    }
                }
                str = "asd";
            }
        }
        System.out.println();
        System.out.print("Save context(1 - yes, any key - no): ");
        if ("1".equals(read())) {
            // в файл
            final FileOutputStream fos = new FileOutputStream(args[0]);
            fos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\"\n   \"spring-beans-2.0.dtd\">\n" + root.asXML()).getBytes("UTF-8"));
            fos.flush();
            fos.close();
            System.out.println("Save and Exit");
        } else {
            System.out.println("Exit without save");
        }
    }

    /**
     * Читаем введеную строку в консоли
     * @return введеная строка
     */
    private static String read() {
        Scanner d = new Scanner(new InputStreamReader(System.in));
        return d.nextLine();
    }

    /**
     * Вывод списка обнаруженных бинов и выбор бина из списка для редактирования.
     * @param root корень для поиска бинов
     * @return введеный номер бина, по пустой строки выход
     */
    private static String getBeans(Element root) {
        System.out.println();
        System.out.println("===========================");
        System.out.println("List of avalable beans");
        System.out.println();
        elBeans.clear();
        List beans = root.elements("bean");
        int i = 1;
        for (Object o : beans) {
            final Element bean = (Element) o;
            elBeans.put(String.valueOf(i), bean);
            System.out.println("" + (i++) + " - " + bean.attributeValue("id"));
        }
        System.out.print("Choose the element(enter - exit): ");
        return read();
    }

    /**
     * Вывод списка обнаруженных параметров бина и выбор одного параметра из списка для редактирования.
     * @param bean бин для редактированя
     * @return введеный номер параметра, по пустой строки выход
     */
    private static String getBean(Element bean) {
        System.out.println();
        System.out.println("===========================");
        System.out.println("Edit bean \"" + bean.attributeValue("id") + "\"");
        System.out.println();
        elProps.clear();
        int i = 1;
        List params = bean.elements("constructor-arg");
        for (Object o : params) {
            final Element param = (Element) o;
            elProps.put(String.valueOf(i), param);
            System.out.println("" + (i++) + " - constructor-arg index=\"" + param.attributeValue("index") + "\" value=\"" + param.attributeValue("value") + "\"");
        }
        params = bean.elements("property");
        for (Object o : params) {
            final Element param = (Element) o;
            elProps.put(String.valueOf(i), param);
            System.out.println("" + (i++) + " - property name=\"" + param.attributeValue("name") + "\" value=\"" + param.attributeValue("value") + "\"");
        }
        System.out.print("Choose the property(enter - return from property): ");
        return read();
    }
}
