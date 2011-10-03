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
package ru.apertum.qsystem.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import ru.apertum.qsystem.common.QLog;

/**
 *
 * @author Evgeniy Egorov
 */
public class Locales {

    private Locales() {
        config = new PropertiesConfiguration();
        config.setEncoding("utf8");
        File f = new File(configFileName);
        if (f.exists()) {
            config.setFileName(configFileName);
        } else {
            configFileName = "../" + configFileName;
            f = new File(configFileName);
            if (f.exists()) {
                config.setFileName(configFileName);
            } else {
                QLog.l().logger().error(new FileNotFoundException(configFileName));
                throw new RuntimeException(new FileNotFoundException(configFileName));
            }
        }

        try {
            config.load();
        } catch (ConfigurationException ex) {
            QLog.l().logger().error(ex);
            throw new RuntimeException(ex);
        }
        config.setAutoSave(true);

        for (Iterator<String> itr = config.getKeys(); itr.hasNext();) {
            String s = itr.next();
            s = s.substring(s.indexOf(".") + 1);
            if (s.indexOf(".") != -1) {
                s = s.substring(0, s.indexOf("."));
                if (locales.get(s) == null) {
                    locales.put(s, new Locale(config.getString("locale." + s + ".lng"), config.getString("locale." + s + ".country")));
                    lngs.put(config.getString("locale." + s + ".name"), s);
                    lngs_names.put(s, config.getString("locale." + s + ".name"));
                }
            }
        }
    }
    private String configFileName = "config/langs.properties";
    private final PropertiesConfiguration config;
    /**
     * eng -> Locale(eng)
     */
    private final HashMap<String, Locale> locales = new HashMap<String, Locale>();
    /**
     * English -> eng
     */
    private final HashMap<String, String> lngs = new HashMap<String, String>();
    /**
     * eng -> English 
     */
    private final HashMap<String, String> lngs_names = new HashMap<String, String>();

    public static Locales getInstance() {
        return LocalesHolder.INSTANCE;
    }

    private static class LocalesHolder {

        private static final Locales INSTANCE = new Locales();
    }
    private final String LANG_CURRENT = "locale.current";

    public Locale getLangCurrent() {
        if (isJoke) {
            return new Locale("uk", "UA");
        }
        return locales.get(config.getString(LANG_CURRENT)) == null ? Locale.getDefault() : locales.get(config.getString(LANG_CURRENT));
    }

    private static final boolean isJoke = (new GregorianCalendar()).get(GregorianCalendar.MONTH) == GregorianCalendar.APRIL && (new GregorianCalendar()).get(GregorianCalendar.DAY_OF_MONTH) == 1;

    public String getLangCurrName() {
        if (isJoke) {
            return lngs_names.get("ukr");
        }
        return "".equals(config.getString(LANG_CURRENT)) ? lngs_names.get("eng") : lngs_names.get(config.getString(LANG_CURRENT));
    }

    /**
     *
     * @param name English к примеру
     */
    public void setLangCurrent(String name) {
        config.setProperty(LANG_CURRENT, lngs.get(name));
    }

    public ArrayList<String> getAvailableLocales() {
        final ArrayList<String> res = new ArrayList<String>(lngs.keySet());
        return res;
    }
}
