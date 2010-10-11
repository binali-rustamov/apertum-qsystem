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
package ru.apertum.qsystem.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Сервер, принимающий сообщения по протоколу UDP
 * Имеет абстрактный метод, который выполняется при получении сообщения.
 * @author Evgeniy Egorov
 */
abstract public class AUDPServer implements Runnable {

    /**
     * порт, который слушает сервер
     */
    private final int port;
    private final Thread thread;
    private DatagramSocket socket;
    private boolean isActive = true;

    public boolean isActivate() {
        return isActive;
    }

    public AUDPServer(int port) {
        this.port = port;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        byte[] buffer = new byte[1024];

        Uses.log.logger.trace("Старт UDP сервера на порту \"" + port + "\"");
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException ex) {
            isActive = false;
            throw new Uses.ServerException("Невозможно создать UDP-сокет на порту " + port + ". " + ex.getMessage());
        }
        while (true) {
            //Receive request from client
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            } catch (IOException ex) {
                throw new Uses.ServerException("Невозможно отослать UDP-сообщение. " + ex.getMessage());
            }
            InetAddress client = packet.getAddress();
            int client_port = packet.getPort();
            final String message = new String(buffer, packet.getOffset(), packet.getLength());
            Uses.log.logger.trace("Приём UDP сообшение \"" + message + "\" ОТ адреса \"" + client.getHostName() + "\" с порта \"" + port + "\"");
            getData(message, client, client_port);
        }

    }

    /**
     * Обработка события получения сообщения
     * @param data Текст сообщения
     * @param clientAddress адрес, откуда пришло сообщение
     * @param clientPort порт, с которого послали сообщение
     */
    abstract protected void getData(String data, InetAddress clientAddress, int clientPort);

    /**
     * Остонавливаем сервер
     */
    @SuppressWarnings("static-access")
    public void stop() {
        thread.interrupted();
        socket.close();
        Uses.log.logger.trace("Остановка UDP сервера на порту \"" + port + "\"");
    }
}    

