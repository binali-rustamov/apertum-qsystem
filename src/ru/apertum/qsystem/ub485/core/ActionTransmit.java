/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.apertum.qsystem.ub485.core;

/**
 * Класс содержит код для распараллеливаия обработки пришедшего пакета
 * @author Evgeniy Egorov
 */
public class ActionTransmit implements Runnable {

    private byte[] bytes;

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public ActionTransmit() {
        this.bytes = new byte[0];
    }

    @Override
    public void run() {
        if (bytes.length == 4 && bytes[0] == 0x01 && bytes[3] == 0x07) {
            // должно быть 4 байта, иначе коллизия
            final ButtonDevice dev = AddrProp.getInstance().getAddrByRSAddr(bytes[1]);
            if (dev == null) {
                throw new RuntimeException("Anknown address from user device.");
            }
            dev.doAction(bytes[2]);
        } else {
            System.err.println("Collision! Package lenght not 4 bytes.");
        }
    }
}
