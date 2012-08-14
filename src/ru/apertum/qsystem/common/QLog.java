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
package ru.apertum.qsystem.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;
import ru.apertum.qsystem.common.exceptions.ClientException;

/**
 * Собственно, логер лог4Ж
 * Это синглтон. Тут в место getInstance() для короткого написания используется l()
 * @author Evgeniy Egorov
 */
public class QLog {

        // Ключи выполнения программы

    private static final String KEY_DEBUG = "DEBUG";
    private static final String KEY_LOG_INFO = "LOGINFO";
    private static final String KEY_DEMO = "DEMO";

    private Logger logger = Logger.getLogger("server.file");//**.file.info.trace

    public Logger logger() {
        return logger;
    }
    /**
     * Пользуемся этой константой для работы с логом для отчетов
     */
    private Logger logRep = Logger.getLogger("reports.file");

    public Logger logRep() {
        return logRep;
    }
    /**
     * Режим отладки
     */
    private final boolean isDebug;

    public boolean isDebug() {
        return isDebug;
    }
    /**
     * Режим демонстрации. При нем не надо прятать мышку и убирать шапку формы.
     */
    private final boolean isDemo;

    public boolean isDemo() {
        return isDemo;
    }

    private QLog() {

        boolean isDebugin = false;
        boolean isDem = false;
        logger = isServer1 ? Logger.getLogger("server.file") : Logger.getLogger("client.file");

        //бежим по параметрам, смотрим, выполняем что надо
        for (int i = 0; i < args1.length; i++) {
            // ключ, отвечающий за логирование
            if (KEY_DEBUG.equalsIgnoreCase(args1[i])) {
                logger = isServer1 ? Logger.getLogger("server.file.info.trace") : Logger.getLogger("client.file.info.trace");
                isDebugin = true;
            }
            // ключ, отвечающий за логирование
            if (KEY_LOG_INFO.equalsIgnoreCase(args1[i])) {
                isDebugin = true;
                logger = isServer1 ? Logger.getLogger("server.file.info") : Logger.getLogger("client.file.info");
            }
            // ключ, отвечающий за режим демонстрации. При нем не надо прятать мышку и убирать шапку формы
            if (KEY_DEMO.equalsIgnoreCase(args1[i])) {
                isDem = true;
            }
        }
        if (!isDebugin) {
            final Properties settings = new Properties();
            final InputStream inStream = settings.getClass().getResourceAsStream("/ru/apertum/qsystem/common/version.properties");
            try {
                settings.load(inStream);
            } catch (IOException ex) {
                throw new ClientException("Проблемы с чтением версии. " + ex);
            }
        }
        isDebug = isDebugin;
        isDemo = isDem;


        if ("server.file.info.trace".equalsIgnoreCase(logger.getName())) {
            logRep = Logger.getLogger("reports.file.info.trace");
        }
        // ключ, отвечающий за логирование
        if ("server.file.info".equalsIgnoreCase(logger.getName())) {
            logRep = Logger.getLogger("reports.file.info");
        }
    }

    public static QLog l() {
        return LogerHolder.INSTANCE;
    }
    private static String[] args1 = new String[0];
    public static boolean isServer1 = false;

    public static QLog initial(String[] args, boolean isServer) {
        args1 = args;
        isServer1 = isServer;
        final QLog log = LogerHolder.INSTANCE;
        QLog.l().logger.info("СТАРТ ЛОГИРОВАНИЯ. Логгер: " + QLog.l().logger().getName());
        QLog.l().logRep.info("СТАРТ ЛОГИРОВАНИЯ для отчетов. Логгер: " + QLog.l().logRep().getName());
        QLog.l().logger.info("Mode: " + (QLog.l().isDebug() ? KEY_DEBUG : (QLog.l().isDemo() ? KEY_DEMO : "FULL")) );

        return log;
    }

    private static class LogerHolder {

        private static final QLog INSTANCE = new QLog();
    }
}
