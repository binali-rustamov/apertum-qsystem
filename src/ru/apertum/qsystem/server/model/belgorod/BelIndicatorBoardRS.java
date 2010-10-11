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
package ru.apertum.qsystem.server.model.belgorod;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import ru.apertum.qsystem.client.forms.AFBoardRedactor;
import ru.apertum.qsystem.client.forms.FParamsEditor;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.server.model.AIndicatorBoard.Record;
import ru.apertum.qsystem.server.model.AIndicatorBoardRS;

/**
 * Для Белгорода
 * @author Evgeniy Egorov
 */
public class BelIndicatorBoardRS extends AIndicatorBoardRS {

    /**
     * Поле для адреса многострочного табло.
     */
    private Integer adress;

    /** 
     * @return the adresses. 
     */
    public Integer getAdress() {
        return adress;
    }

    /** 
     * @param adresses The adresses to set. 
     */
    public void setAdress(Integer adress) {
        this.adress = adress;
        Uses.log.logger.trace("Определили адрес общего табло \"" + adress + "\"");
    }
    /**
     * Конфигурационный файл для табло. Приезжает из Spring
     */
    private String configFile;

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
        setPause(Integer.parseInt(getProperties().getProperty(PAUSE)));
        setAdress(Integer.parseInt(getProperties().getProperty(ADRESS)));
    }
    private final byte blinkNo = 0;
    private final byte blinkAll = 125;
    private final byte blink2 = 2;
    private final byte blink3 = 3;

    @Override
    public void close() {
        sendMessage(StringUtils.right("----------------------------------------", 18), adress.byteValue(), blinkNo, null);
    }

    /**
     * Отсылаем сообщение на табло.
     * В отдельным потоке, чтоб не тормозил вывод выполнение основного.
     * Синхронизированно, т.к. команды шлются по одной и с интервалом 0.3 секунды, чтобы успевало все выполняться.
     * К тому же можно настроить задержку вывода на табло.
     * @param message Строка которая отобразиться на табло.
     * @param adress адрес устройства вывода строки.
     * @param blink режим мигания 0-не мигаем, 1-мигаем, n-мигаем n раз
     * @param list список для маркировки отвисевшести
     */
    private void sendMessage(final String message, final byte adress, final byte blink, final Collection<Record> list) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    byte[] mess = ("123" + message + "e").getBytes();
                    mess[0] = 1;
                    mess[1] = adress;
                    switch (blink) {
                        case 0:
                            mess[2] = 0;
                            break;
                        case 1:
                            mess[2] = 1;
                            break;
                        case 2:
                            mess[2] = 2;
                            break;
                        case 3:
                            mess[2] = 3;
                            break;
                        case 4:
                            mess[2] = 4;
                            break;
                        case 5:
                            mess[2] = 5;
                            break;
                        case 6:
                            mess[2] = 6;
                            break;
                        case 7:
                            mess[2] = 7;
                            break;
                        case 8:
                            mess[2] = 8;
                            break;
                        case 9:
                            mess[2] = 9;
                            break;
                        case 10:
                            mess[2] = 10;
                            break;
                        case 11:
                            mess[2] = 11;
                            break;
                        case 125:
                            mess[2] = 125;
                            break;
                        default:
                            mess[2] = blink;
                    }
                    //mess[2] = 2;//blink;
                    mess[mess.length - 1] = 7;
                    Uses.log.logger.trace("Сформировано сообщение \"" + message + "\" с адресом \"" + adress + "\", мигание \"" + mess[2] + "\".");
                    send(mess, adress, list);
                } catch (Exception ex) {
                    throw new Uses.ServerException("Не возможно отправить сообщение. " + ex);
                }
            }
        }).start();
    }

    /**
     * Конкретное отправление на девайс.
     * @param mess
     * @param adress
     * @throws java.lang.Exception
     */
    synchronized private void send(byte[] mess, byte adress, final Collection<Record> list) throws Exception {
        getSerialPort().send(mess);
        // обязательный вызов для маркировки отвисевшисти. Надо тут, а то потоки долго могут стоять в очереди, а таймер будет запущен.
        markShowed(list);
        Uses.log.logger.trace("Вывод на табло с адресом \"" + adress + "\", время " + Uses.format_HH_mm_ss.format(new Date()));
        // Подождем пока отработает СОМ и табло.

        try {
            Thread.sleep(200/* + (adress == 32 ? getPause() : 0) * 1000*/);
        } catch (InterruptedException ex) {
        }

    }

    @Override
    protected void showOnBoard(LinkedHashSet<Record> records) {
        boolean flag = false;
        String m = "";
        for (Record rec : records) {
            m = m.concat(StringUtils.right("         " + rec.customerNumber, 3) + StringUtils.right("        " + rec.point, 3));
            if (/*!rec.isShowed() && */(rec.getState() == Uses.STATE_INVITED || rec.getState() == Uses.STATE_REDIRECT)) {
                flag = true;
            }
        }
        sendMessage(StringUtils.left(m + "                      ", 18), adress.byteValue(), flag ? blink2 : blinkNo, records);
    }

    @Override
    protected void showToUser(Record record) {
        sendMessage(StringUtils.right(record.getState() == Uses.STATE_DEAD ? "        " : "        " + record.customerNumber, 3), record.adressRS.byteValue(), (record.getState() == Uses.STATE_INVITED || record.getState() == Uses.STATE_REDIRECT) ? blinkAll : blinkNo, null);
    }
    
    private Properties getProperties(){
        final Properties settings = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(getConfigFile());
        } catch (FileNotFoundException ex) {
            throw new Uses.ServerException("Проблемы с файлом конфигурации главного табло \"" + getConfigFile() + "\" при чтении. " + ex);
        }
        try {
            settings.load(in);
        } catch (IOException ex) {
            throw new Uses.ServerException("Проблемы с чтением параметров. " + ex);
        }
        return settings;
    }

    //<!-- Задержка обновления главного табло в секундах -->
    @Override
    public Element getConfig() {
        final Properties settings = getProperties();
        final Element root = DocumentHelper.createElement(Uses.TAG_BOARD_PROPS);
        // Параметр задержки строки на главном табло
        Element param = root.addElement(Uses.TAG_BOARD_PROP);
        param.addAttribute(Uses.TAG_BOARD_NAME, Uses.BOARD_VALUE_PAUSE);
        param.addAttribute(Uses.TAG_BOARD_TYPE, String.valueOf(Uses.BOARD_TYPE_INT));
        param.addAttribute(Uses.TAG_BOARD_VALUE, settings.getProperty(PAUSE));
        // Параметр - адрес главного табло в гирлянде RS
        param = root.addElement(Uses.TAG_BOARD_PROP);
        param.addAttribute(Uses.TAG_BOARD_NAME, Uses.BOARD_ADRESS_MAIN_BOARD);
        param.addAttribute(Uses.TAG_BOARD_TYPE, String.valueOf(Uses.BOARD_TYPE_INT));
        param.addAttribute(Uses.TAG_BOARD_VALUE, settings.getProperty(ADRESS));
        return root;
    }
    private final static String PAUSE = "pause";
    private final static String ADRESS = "adress";

    @Override
    public void saveConfig(Element element) {
        final Properties settings = new Properties();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(getConfigFile());
        } catch (FileNotFoundException ex) {
            throw new Uses.ClientException("Проблемы с файлом при сохранении. " + ex);
        }
        settings.put(PAUSE, Uses.elementsByAttr(element, Uses.TAG_BOARD_NAME, Uses.BOARD_VALUE_PAUSE).get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        settings.put(ADRESS, Uses.elementsByAttr(element, Uses.TAG_BOARD_NAME, Uses.BOARD_ADRESS_MAIN_BOARD).get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        try {
            settings.store(out, "Settings of Belgorod main board");
        } catch (IOException ex) {
            throw new Uses.ServerException("Проблемы с выводом в файл \"" + getConfigFile() + "\". " + ex);
        }
    }

    @Override
    public AFBoardRedactor getRedactor() {
        if (paramsEditor == null) {
            paramsEditor = FParamsEditor.getParamsEditor(null, false);
        }
        return paramsEditor;
    }
    /**
     * Используемая ссылка на диалоговое окно. Singleton
     */
    private static FParamsEditor paramsEditor;

    @Override
    public void showBoard() {
       // тут нечего запускать или показывать.
    }
}
