/*
 * Copyright (C) 2010 {Apertum}Projects. web: www.apertum.ru email: info@apertum.ru
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
package ru.apertum.qsystem.server.http;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.exceptions.ServerException;

/**
 * Класс старта и останова сервера Jetty.
 * При старте создается новый поток и в нем стартует Jetty
 * @author Evgeniy Egorov
 */
public class JettyRunner implements Runnable {

    /**
     * Страт Jetty
     * @param port порт на котором стартует сервер 
     */
    public static void start(int port) {
        servetPort = port;
        if (jetthread != null && jetthread.isInterrupted() == false) {
            try {
                if (jetty.isRunning()) {
                    jetty.stop();
                }
            } catch (Exception ex) {
                QLog.l().logger().error("Ошибка остановки сервера Jetty.", ex);
            }
            jetthread.interrupt();
        }
        jetthread = new Thread(new JettyRunner());
        jetthread.setDaemon(true);
        jetthread.start();
    }

    /**
     * Остановить сервер Jetty
     */
    public static void stop() {
        if (jetthread != null && jetthread.isInterrupted() == false) {
            try {
                if (jetty.isRunning()) {
                    jetty.stop();
                }
            } catch (Exception ex) {
                throw new ServerException("Ошибка остановки сервера Jetty.", ex);
            }
            jetthread.interrupt();
        }
        QLog.l().logger().info("Сервер Jetty успешно остановлен.");
    }
    private static volatile Server jetty = null;
    private static int servetPort = 8081;
    private static Thread jetthread = null;

    @Override
    public void run() {
        QLog.l().logger().info("Старт сервера Jetty на порту " + servetPort);
        jetty = new Server();

        //org.eclipse.jetty.io.nio.AsyncConnection d;
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(8443);
        http_config.setOutputBufferSize(32768);
        http_config.setRequestHeaderSize(8192);
        http_config.setResponseHeaderSize(8192);
        http_config.setSendServerVersion(true);
        http_config.setSendDateHeader(false);
        ServerConnector http_connector = new ServerConnector(jetty, new HttpConnectionFactory(http_config));
        http_connector.setIdleTimeout(30000);
        http_connector.setPort(servetPort);
        jetty.addConnector(http_connector);

        final ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{"index.html"});
        resource_handler.setResourceBase("www");

        /*
        // WebSocket: Регистрируем ChatWebSocketHandler в сервере Jetty
        QWebSocketHandler qWebSocketHandler = new QWebSocketHandler();
        // Это вариант хэндлера для WebSocketHandlerContainer
        qWebSocketHandler.setHandler(new DefaultHandler());
         * 
         */
         

        final HandlerList handlers = new HandlerList();

        // Важный момент - поряд следования хандлеров
        // по этому порядку будет передоваться запрос, если он еще не обработан
        // т.е. с начала ищется файл, если не найден, то урл передается на исполнение команды,
        // в комаедах учтено что урл для вебсокета нужно пробросить дальше, его поймает хандлер вебсокетов
        //handlers.setHandlers(new Handler[]{resource_handler, new CommandHandler(), qWebSocketHandler});
        handlers.setHandlers(new Handler[]{resource_handler, new CommandHandler()});
        jetty.setHandler(handlers);
        

        /*
        String jetty_home = "";
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(jetty_home + "/etc/keystore");
        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
        sslContextFactory.setTrustStorePath(jetty_home + "/etc/keystore");
        sslContextFactory.setTrustStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());
        ServerConnector sslConnector = new ServerConnector(jetty, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https_config));
        sslConnector.setPort(8443);
        jetty.addConnector(sslConnector);
         * 
         */

        try {
            jetty.start();
        } catch (Exception ex) {
            throw new ServerException("Ошибка запуска сервера Jetty. ", ex);
        }
        QLog.l().logger().info("Join сервера Jetty на порту " + servetPort);
        try {
            jetty.join();
        } catch (InterruptedException ex) {
            QLog.l().logger().warn("Jetty прекратил работу");
        }
        QLog.l().logger().info("Сервер Jetty остановлен.");
    }
}
