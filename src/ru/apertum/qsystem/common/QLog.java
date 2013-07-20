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
    private static final String KEY_NOPLUGINS = "-noplugins";
    private static final String KEY_PAUSE = "-pause";
    private static final String KEY_TERMINAL = "-terminal";
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
    private final boolean plaginable;

    public boolean isPlaginable() {
        return plaginable;
    }
    private final boolean terminal;

    public boolean isTerminal() {
        return terminal;
    }
    

    private QLog() {

        boolean isDebugin = false;
        boolean isDem = false;
        boolean isPlug = true;
        boolean isTerminal = false;
        switch (loggerType) {
            case 0://сервер
                logger = Logger.getLogger("server.file");
                break;
            case 1://клиент
                logger = Logger.getLogger("client.file");
                break;
            case 2://приемная
                logger = Logger.getLogger("reception.file");
                break;
            case 3://админка
                logger = Logger.getLogger("admin.file");
                break;
            case 4://админка
                logger = Logger.getLogger("welcome.file");
                break;
            default:
                throw new AssertionError();
        }

        //бежим по параметрам, смотрим, выполняем что надо
        for (int i = 0; i < args1.length; i++) {
            // ключ, отвечающий за логирование
            if (KEY_DEBUG.equalsIgnoreCase(args1[i])) {
                switch (loggerType) {
                    case 0://сервер
                        logger = Logger.getLogger("server.file.info.trace");
                        break;
                    case 1://клиент
                        logger = Logger.getLogger("client.file.info.trace");
                        break;
                    case 2://приемная
                        logger = Logger.getLogger("reception.file.info.trace");
                        break;
                    case 3://админка
                        logger = Logger.getLogger("admin.file.info.trace");
                        break;
                    case 4://админка
                        logger = Logger.getLogger("welcome.file.info.trace");
                        break;
                    default:
                        throw new AssertionError();
                }
                isDebugin = true;
            }
            // ключ, отвечающий за логирование
            if (KEY_LOG_INFO.equalsIgnoreCase(args1[i])) {
                isDebugin = true;
                switch (loggerType) {
                    case 0://сервер
                        logger = Logger.getLogger("server.file.info");
                        break;
                    case 1://клиент
                        logger = Logger.getLogger("client.file.info");
                        break;
                    case 2://приемная
                        logger = Logger.getLogger("reception.file.info");
                        break;
                    case 3://админка
                        logger = Logger.getLogger("admin.file.info");
                        break;
                    case 4://админка
                        logger = Logger.getLogger("welcome.file.info");
                        break;
                    default:
                        throw new AssertionError();
                }
            }
            // ключ, отвечающий за режим демонстрации. При нем не надо прятать мышку и убирать шапку формы
            if (KEY_DEMO.equalsIgnoreCase(args1[i])) {
                isDem = true;
            }
            // ключ, отвечающий за возможность загрузки плагинов. 
            if (KEY_NOPLUGINS.equalsIgnoreCase(args1[i])) {
                isPlug = false;
            }
            // ключ, отвечающий за возможность работы клиента на терминальном сервере. 
            if (KEY_TERMINAL.equalsIgnoreCase(args1[i])) {
                isTerminal = true;
            }
            // ключ, отвечающий за паузу на старте. 
            if (KEY_PAUSE.equalsIgnoreCase(args1[i])) {
                if (i < args1.length - 1 && args1[i + 1].matches("^-?\\d+$")) {
                    try {
                        Thread.sleep(Integer.parseInt(args1[i + 1]) * 1000);
                    } catch (InterruptedException ex) {
                    }
                }
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
        plaginable = isPlug;
        terminal = isTerminal;


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
    public static int loggerType = 0; // 0-сервер,1-клиент,2-приемная,3-админка,4-киоск

    /**
     * 
     * @param args
     * @param loggerType  0-сервер,1-клиент,2-приемная,3-админка,4-киоск
     * @return 
     */
    public static QLog initial(String[] args, int type) {
        args1 = args;
        loggerType = type;
        isServer1 = type == 0;
        final QLog log = LogerHolder.INSTANCE;
        QLog.l().logger.info("СТАРТ ЛОГИРОВАНИЯ. Логгер: " + QLog.l().logger().getName());
        if (isServer1) {
            QLog.l().logRep.info("СТАРТ ЛОГИРОВАНИЯ для отчетов. Логгер: " + QLog.l().logRep().getName());
        }
        QLog.l().logger.info("Mode: " + (QLog.l().isDebug() ? KEY_DEBUG : (QLog.l().isDemo() ? KEY_DEMO : "FULL")));
        QLog.l().logger.info("Plugins: " + (QLog.l().isPlaginable() ? "YES" : "NO"));

        return log;
    }

    private static class LogerHolder {

        private static final QLog INSTANCE = new QLog();
    }
}
