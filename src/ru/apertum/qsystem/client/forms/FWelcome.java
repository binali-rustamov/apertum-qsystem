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
package ru.apertum.qsystem.client.forms;

import gnu.io.SerialPortEvent;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.MemoryImageSource;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.output.OutputException;
import org.dom4j.Element;
import ru.apertum.qsystem.common.model.NetCommander;
import ru.apertum.qsystem.client.model.QButton;
import ru.apertum.qsystem.client.model.QPanel;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.model.ATalkingClock;
import ru.apertum.qsystem.common.model.INetProperty;
import ru.evgenic.rxtx.serialPort.IReceiveListener;
import ru.evgenic.rxtx.serialPort.ISerialPort;
import ru.evgenic.rxtx.serialPort.RxtxSerialPort;

/**
 * Модуль показа окна выбора услуги для постановки в очередь.
 * Created on 8 Сентябрь 2008 г., 16:07
 * Класс, который покажит форму с кнопками, соответствующими услуга.
 * При нажатии на кнопку, кастомер пытается встать в очередь.
 * @author Evgeniy Egorov
 */
public class FWelcome extends javax.swing.JFrame {
    // Состояния пункта регистрации

    public static final String LOCK = "Заблокирован";
    public static final String UNLOCK = "Готов к работе";
    public static final String OFF = "Выключен";
    public static final String LOCK_MESSAGE = "<HTML><p align=center><b><span style='font-size:40.0pt;color:red'>Временно не активно. Обратитесь к администратору.</span></b></p>";
    /**
     * Константы хранения параметров в файле.
     */
    private static final String LEFT_MARGIN = "left_margin";
    private static final String TOP_MARGIN = "top_margin";
    private static final String LINE_HEIGTH = "line_heigth";
    private static final String LINE_LENGTH = "line_length";
    private static final String SCALE_VERTICAL = "scale_vertical";
    private static final String SCALE_HORIZONTAL = "scale_horizontal";
    private static final String LOGO = "logo";
    private static final String BARCODE = "barcode";
    private static final String LOGO_LEFT = "logo_left";
    private static final String LOGO_TOP = "logo_top";
    private static final String DELAY_PRINT = "delay_print";
    private static final String DELAY_BACK = "delay_back";
    private static final String LOGO_IMG = "logo_img";
    private static final String PROMO_TXT = "promo_text";
    private static final String BOTTOM_TXT = "bottom_text";
    private static final String ASK_LIMIT = "ask_limit";
    private static final String INFO_BUTTON = "info_button";// кнопка информационной системы на пункте регистрации
    private static final String RESPONSE_BUTTON = "response_button";// - кнопка обратной связи на пункте регистрации
    private static final String ADVANCE_BUTTON = "advance_button";// - кнопка предварительной записи на пункте регистрации
    public static Element root;
    /**
     * XML-список отзывов. перврначально null, грузится при первом обращении. Использовать через геттер.
     */
    private static Element response = null;

    public static Element getResponse() {
        if (response == null) {
            response = NetCommander.getResporseList(netProperty);
        }
        return response;
    }
    /**
     * XML- дерево информации. перврначально null, грузится при первом обращении. Использовать через геттер.
     */
    private static Element infoTree = null;

    public static Element getInfoTree() {
        if (infoTree == null) {
            infoTree = NetCommander.getInfoTree(netProperty);
        }
        return infoTree;
    }
    protected static Element current;
    /**
     * это печатаем под картинкой если без домена
     */
    public static String caption;
    /**
     * время блокировки/разблокировки пункта регистрации
     */
    protected static Date startTime;
    protected static Date finishTime;
    /**
     * Информация для взаимодействия по сети.
     * Формируется по данным из командной строки.
     */
    public static INetProperty netProperty;
    /**
     * Режим работы с тачевым блоком(true) или блоком кнопок(false)
     */
    public static boolean isTach = true;
    /**
     * Режим предварительной записи в поликлинике
     */
    public static boolean isMed = false;
    //******************************************************************************************************************
    //******************************************************************************************************************
    //*****************************************Сервер удаленного управления ********************************************
    /**
     * 
     */
    private final Thread server = new Thread(new CommandServer());
    /**
     * Флаг завершения сервера удаленного управления
     */
    boolean exitServer = false;

    private class CommandServer implements Runnable {

        @Override
        public void run() {
            // привинтить сокет на локалхост, порт 3129
            final ServerSocket server;
            try {
                server = new ServerSocket(netProperty.getClientPort());
                server.setSoTimeout(500);
            } catch (IOException e) {
                throw new Uses.ClientException("Ошибка при создании серверного сокета: " + e);
            }

            Uses.writeRus("Сервер управления пунктом регистрации запущен.\n");
            Uses.log.logger.info("Сервер управления пунктом регистрации запущен.");

            // слушаем порт
            while (!exitServer) {
                // ждём нового подключения, после чего запускаем обработку клиента
                // в новый вычислительный поток и увеличиваем счётчик на единичку
                try {
                    doCommand(server.accept());
                } catch (IOException e) {
                }
            }
        }

        private void doCommand(Socket socket) {
            // из сокета клиента берём поток входящих данных
            try {
                InputStream is;
                try {
                    is = socket.getInputStream();
                } catch (IOException e) {
                    throw new Uses.ServerException("Ошибка при получении входного потока: " + e.getStackTrace());
                }

                final String data;
                try {
                    // подождать пока хоть что-то приползет из сети, но не более 10 сек.
                    int i = 0;
                    while (is.available() == 0 && i < 100) {
                        Thread.sleep(100);//бля
                        i++;
                    }
                    Thread.sleep(100);//бля
                    data = URLDecoder.decode(new String(Uses.readInputStream(is)).trim(), "utf-8");
                } catch (IOException ex) {
                    throw new Uses.ServerException("Ошибка при чтении из входного потока: " + ex.getStackTrace());
                } catch (InterruptedException ex) {
                    throw new Uses.ServerException("Проблема со сном: " + ex.getStackTrace());
                }
                Uses.log.logger.trace("Задание:\n" + data);

                // Обрабатываем задание
                //С рабочего места администратора должна быть возможность заблокировать пункт постановки в очередь, 
                //разблокировать, выключить, провести инициализация заново.
                // В любом другом случае будет выслано состояние.
                if (Uses.WELCOME_LOCK.equals(data)) {
                    lock(LOCK_MESSAGE);
                }
                if (Uses.WELCOME_UNLOCK.equals(data)) {
                    unlock();
                }
                if (Uses.WELCOME_OFF.equals(data)) {
                    off();
                }
                if (Uses.WELCOME_REINIT.equals(data)) {
                    reinit();
                }

                // выводим данные:
                Uses.log.logger.trace("Ответ:\n" + stateWindow);
                try {
                    // Передача данных ответа
                    final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    writer.print("<Ответ>" + stateWindow + "</Ответ>");
                    writer.flush();
                } catch (IOException e) {
                    throw new Uses.ServerException("Ошибка при записи в поток: " + e.getStackTrace());
                }
            } finally {
                // завершаем соединение
                try {
                    //оборативаем close, т.к. он сам может сгенерировать ошибку IOExeption. Просто выкинем Стек-трейс
                    socket.close();
                } catch (IOException e) {
                    Uses.log.logger.error(e);
                }
            }
            //Если команда была "выключить"
            if (OFF.equals(stateWindow)) {
                System.exit(0);
            }
        }
    }
    //*****************************************Сервер удаленного управления ********************************************
    /**
     * Таймер, по которому будем выходить в корень меню.
     */
    public ATalkingClock clockBack = new ATalkingClock(welcomeParams.delayBack, 1) {

        @Override
        public void run() {
            setAdvanceRegim(false);
            buttonToBeginActionPerformed(null);
            showMed();
        }
    };
    /**
     * Таймер, по которому будем разблокировать и выходить в корень меню.
     */
    public ATalkingClock clockUnlockBack = new ATalkingClock(welcomeParams.delayPrint, 1) {

        @Override
        public void run() {
            unlock();
            setAdvanceRegim(false);
            buttonToBeginActionPerformed(null);
        }
    };
    private static ISerialPort serialPort;

    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) throws Exception {
        Uses.isDebug = Uses.setLogining(args, false);
        netProperty = Uses.getClientNetProperty(args);
        // определим режим пользовательского интерфейса
        for (String s : args) {
            if ("buttons".equals(s)) {
                isTach = false;
            }
            if ("med".equals(s)) {
                isMed = true;
                if (!"".equals(welcomeParams.buttons_COM)) {
                    serialPort = new RxtxSerialPort(welcomeParams.buttons_COM);
                    serialPort.setDataBits(welcomeParams.buttons_databits);
                    serialPort.setParity(welcomeParams.buttons_parity);
                    serialPort.setSpeed(welcomeParams.buttons_speed);
                    serialPort.setStopBits(welcomeParams.buttons_stopbits);
                }
            }
        }
        root = NetCommander.getServiсes(netProperty);
        if (isTach) {
            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final FWelcome w = new FWelcome(root);
                    w.setVisible(true);
                }
            });
        } else {
            //грузим параметры
            loadRootParamSimple();
            //и запускаем поток прослушки СОМ-порта
            java.awt.EventQueue.invokeLater(new Runnable() {

                private final ISerialPort port = new RxtxSerialPort(welcomeParams.buttons_COM);
                private final IReceiveListener listener = new IReceiveListener() {

                    @Override
                    public void actionPerformed(SerialPortEvent event, byte[] data) {
                        result = new String(data).substring(0, data.length - 2); //+ " - " + Uses.format_HH_mm_ss.format(new Date());
                    }

                    @Override
                    public void actionPerformed(SerialPortEvent event) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
                private String result = null;

                @Override
                public void run() {
                    port.setDataBits(welcomeParams.buttons_databits);
                    port.setParity(welcomeParams.buttons_parity);
                    port.setSpeed(welcomeParams.buttons_speed);
                    port.setStopBits(welcomeParams.buttons_stopbits);
                    try {
                        port.bind(listener);
                    } catch (Exception ex) {
                        throw new Uses.ClientException("Не возможно захватить порт. " + ex);
                    }
                    int pos = 0;
                    while (true) {
                        if (result == null) {
                            if (!Uses.isDebug) {
                                final char ch = '*';
                                String progres = "Активность: " + ch;
                                final int len = 5;
                                for (int i = 0; i < pos; i++) {
                                    progres = progres + ch;
                                }
                                for (int i = 0; i < len; i++) {
                                    progres = progres + ' ';
                                }
                                if (++pos == len) {
                                    pos = 0;
                                }
                                Uses.writeRus(progres);
                                System.out.write(13);// '\b' - возвращает корретку на одну позицию назад

                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                                Uses.log.logger.error(ex);
                            }
                        } else {
                            Uses.log.logger.debug("Приняли сообщение в СОМ-порт \"" + result + "\"");
                            // Определим имя услуги
                            final ArrayList<Element> list = Uses.elementsByCData(root, result);
                            final Element element = list.get(0);
                            // Определим маркировку сайта
                            final String siteMark;
                            final String ss = element.attributeValue(Uses.TASK_FOR_SITE);
                            siteMark = (ss == null) ? "" : ss;
                            // Отрправим запрос на постановку в очередь
                            final Element res;
                            try {
                                res = NetCommander.standInService(FWelcome.netProperty, element.attributeValue(Uses.TAG_NAME), "1", 1, siteMark, "");
                            } catch (Exception ex) {
                                throw new Uses.ClientException("Загнулась команда постановки кента в очередь. " + ex);
                            }
                            // Напечатаем квитанцию
                            Uses.log.logger.info("Печать этикетки.");

                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        // Для доменной системы на этикетке под картинкой выводим наименование с своего сайта
                                        port.free();

                                        final String cap = FWelcome.captions.get(siteMark);
                                        if (cap == null) {
                                            FWelcome.printTicket(res);
                                        } else {
                                            FWelcome.printTicket(res, cap);
                                        }
                                    } catch (Exception ex) {
                                        Uses.log.logger.error("При печати чека возникла ошибка. " + ex);
                                    } finally {
                                        boolean sucses = false;
                                        while (!sucses) {
                                            sucses = true;
                                            try {
                                                Thread.sleep(welcomeParams.delayPrint);
                                                port.bind(listener);
                                                Uses.writeRus("Прием команд активен\n");
                                            } catch (Exception ex) {
                                                Uses.log.logger.error("При захвате СОМ-порта после печати чека возникла ошибка . " + ex);
                                                sucses = false;
                                            }
                                        }
                                    }
                                }
                            }).start();
                            result = null;
                        }
                    }
                }
            });
        }
    }

    /** 
     * Creates new form FWelcome
     * @deprecated не создадутся кнопки выбора.
     * @see public FWelcome(Element root)
     */
    public FWelcome() {
        initComponents();
        FWelcome.root = null;
        FWelcome.current = null;
        FWelcome.caption = "";
    }

    public FWelcome(Element root) {
        Uses.log.logger.info("Создаем окно приглашения.");
        if (!Uses.isDebug) {
            if (!Uses.isDemo) {
                setUndecorated(true);
                //setAlwaysOnTop(true);
                setResizable(false);

                // спрячем курсор мыши
                int[] pixels = new int[16 * 16];
                Image image = Toolkit.getDefaultToolkit().createImage(
                        new MemoryImageSource(16, 16, pixels, 0, 16));
                Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
                setCursor(transparentCursor);
            }
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowOpened(WindowEvent e) {
                    setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
            });
        }

        if (Uses.isDemo) {
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowOpened(WindowEvent e) {
                    setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
            });
        }
        initComponents();
        if (Uses.isDebug) {
            setSize(1280, 1024);
        }
        FWelcome.root = root;
        FWelcome.current = root;
        FWelcome.response = null;
        FWelcome.infoTree = null;
        loadRootParam();
        server.start();
        if (!(Uses.format_HH_mm.format(finishTime).equals(Uses.format_HH_mm.format(startTime)))) {
            lockWelcome.start();
        }
        /*
         * Кнопки открываются по настройке
         */
        buttonInfo.setVisible(welcomeParams.info);
        buttonResponse.setVisible(welcomeParams.response);
        buttonAdvance.setVisible(welcomeParams.advance);
        buttonStandAdvance.setVisible(welcomeParams.advance);
        showMed();
    }
    /**
     * переменная доступа до настроек пункта регистрации.
     */
    public static WelcomeParams welcomeParams = new WelcomeParams();

    /**
     * Класс загрузки и предоставления настроек пункта регистрации
     */
    public static class WelcomeParams {

        public WelcomeParams() {
            loadSettings();
        }
        public int leftMargin; // отступ слева
        public int topMargin; // отступ сверху
        public int lineHeigth = 12; // Ширина строки
        public int lineLenght = 40; // Длинна стоки на квитанции
        public double scaleVertical = 0.8; // маштабирование по вертикале
        public double scaleHorizontal = 0.8; // машcтабирование по горизонтали
        public boolean logo = true; // присутствие логотипа на квитанции
        public boolean barcode = true; // присутствие штрихкода на квитанции
        public boolean info = true;// кнопка информационной системы на пункте регистрации
        public boolean response = true;// - кнопка обратной связи на пункте регистрации
        public boolean advance = true; // - кнопка предварительной записи на пункте регистрации
        public int logoLeft = 50; // Отступ печати логотипа слева
        public int logoTop = -5; // Отступ печати логотипа сверху
        public String logoImg = "/ru/apertum/qsystem/client/forms/resources/logo_ticket.png"; // Отступ печати логотипа сверху
        public String promoText = "© ЗАО \"ККС\". Тел./факс: ---, e-mail: info@aperum.ru"; // промотекст, печатающийся мелким шрифтом перед штрихкодом.
        public String bottomText = "Приятного ожидания. Спасибо."; // произвольный текст, печатающийся в конце квитанции после штрихкода
        public int askLimit = 3; // Критический размер очереди после которого спрашивать клиентов о готовности встать в очередь
        /**
         * Задержка заставки при печати в мсек.
         */
        public int delayPrint = 3000;
        public int delayBack = 40000;
        //параметры СОМ-порта для кнопок кнопочного интерфейса
        public String buttons_COM = "COM1";
        public int buttons_databits = 8;
        public int buttons_speed = 9600;
        public int buttons_parity = 0;
        public int buttons_stopbits = 1;

        /**
         * Загрузим настройки.
         */
        private void loadSettings() {
            Uses.log.logger.debug("Загрузим параметры из файла \"config" + File.separator + "welcome.property\"");
            final Properties settings = new Properties();
            FileInputStream in = null;
            InputStreamReader inR = null;
            try {
                in = new FileInputStream("config" + File.separator + "welcome.properties");
                inR = new InputStreamReader(in, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw new Uses.ClientException("Проблемы с кодировкой при чтении. " + ex);
            } catch (FileNotFoundException ex) {
                throw new Uses.ClientException("Проблемы с файлом при чтении. " + ex);
            }
            try {
                settings.load(inR);
            } catch (IOException ex) {
                throw new Uses.ClientException("Проблемы с чтением параметров. " + ex);
            }
            leftMargin = Integer.parseInt(settings.getProperty(LEFT_MARGIN)); // отступ слева
            topMargin = Integer.parseInt(settings.getProperty(TOP_MARGIN)); //  отступ сверху
            lineHeigth = Integer.parseInt(settings.getProperty(LINE_HEIGTH)); // Ширина строки
            lineLenght = Integer.parseInt(settings.getProperty(LINE_LENGTH)); // Длинна стоки на квитанции
            scaleVertical = Double.parseDouble(settings.getProperty(SCALE_VERTICAL)); // маштабирование по вертикале
            scaleHorizontal = Double.parseDouble(settings.getProperty(SCALE_HORIZONTAL)); // машcтабирование по вертикале
            logo = "1".equals(settings.getProperty(LOGO)) || "true".equals(settings.getProperty(LOGO)); // присутствие логотипа на квитанции
            barcode = "1".equals(settings.getProperty(BARCODE)) || "true".equals(settings.getProperty(BARCODE)); // присутствие штрихкода на квитанции
            logoLeft = Integer.parseInt(settings.getProperty(LOGO_LEFT)); // Отступ печати логотипа слева
            logoTop = Integer.parseInt(settings.getProperty(LOGO_TOP)); // Отступ печати логотипа сверху
            delayPrint = Integer.parseInt(settings.getProperty(DELAY_PRINT)); // Задержка заставки при печати в мсек.
            delayBack = Integer.parseInt(settings.getProperty(DELAY_BACK)); // Задержка заставки при печати в мсек.
            logoImg = settings.getProperty(LOGO_IMG);
            promoText = settings.getProperty(PROMO_TXT);
            bottomText = settings.getProperty(BOTTOM_TXT);
            askLimit = Integer.parseInt(settings.getProperty(ASK_LIMIT)); // Критический размер очереди после которого спрашивать клиентов о готовности встать в очередь
            buttons_COM = settings.getProperty("buttons_COM");
            buttons_databits = Integer.parseInt(settings.getProperty("buttons_databits"));
            buttons_speed = Integer.parseInt(settings.getProperty("buttons_speed"));
            buttons_parity = Integer.parseInt(settings.getProperty("buttons_parity"));
            buttons_stopbits = Integer.parseInt(settings.getProperty("buttons_stopbits"));
            info = "1".equals(settings.getProperty(INFO_BUTTON)) || "true".equals(settings.getProperty(INFO_BUTTON)); // кнопка информационной системы на пункте регистрации
            response = "1".equals(settings.getProperty(RESPONSE_BUTTON)) || "true".equals(settings.getProperty(RESPONSE_BUTTON));// - кнопка обратной связи на пункте регистрации
            advance = "1".equals(settings.getProperty(ADVANCE_BUTTON)) || "true".equals(settings.getProperty(ADVANCE_BUTTON)); // - кнопка предварительной записи на пункте регистрации
        }
    }

    /**
     * Загрузка и инициализация неких параметров из корня дерева описания для старта или реинициализации.
     */
    private void loadRootParam() {
        loadRootParamSimple();
        labelCaption.setText(root.getTextTrim());
        setStateWindow(UNLOCK);
        showButtons(root, panelMain);
    }
    /**
     * Это когда происходит авторизация клиента при постановке в очередь,
     * например перед выбором услуге в регистуре, то сюда попадает ID этого авторизованного пользователя.
     * Дальше этот ID передать в команду постановки предварительного и там если по нему найдется этот клиент, то он
     * должен попасть в табличку предварительно зарегиных.
     */
    public long advancedCustomer = -1;

    public void showMed() {
        if (isMed) {
            final ATalkingClock cl = new ATalkingClock(10, 1) {

                @Override
                public void run() {
                    if (!FMedCheckIn.isShowen()) {
                        final Element customer = FMedCheckIn.showMedCheckIn(null, true, netProperty, false, serialPort);
                        if (customer != null) {
                            advancedCustomer = Long.parseLong(customer.attributeValue(Uses.TAG_ID));
                            setAdvanceRegim(true);
                            labelCaption.setText("<html><b><p align=center><span style='font-size:35.0pt;color:green'>" + customer.attributeValue(Uses.TAG_SURNAME) + " " + customer.attributeValue(Uses.TAG_NAME) + " " + customer.attributeValue(Uses.TAG_OTCHESTVO) + "<br></span><span style='font-size:30.0pt;color:red'>Выберите услугу для предварительной записи");
                        } else {
                            throw new Uses.ClientException("Нельзя выбирать услугу если не идентифицирован клиент.");
                        }
                    }
                }
            };
            cl.start();
        }
    }

    /**
     * Загрузка и инициализация без отображения на форме неких параметров из корня дерева описания для старта или реинициализации.
     */
    private static void loadRootParamSimple() {
        // Блокировку пункта регистрации проводить по настройкам суперсайта если работаем с таковым
        if (netProperty.IsSuperSite()) {
            for (Object o : root.elements()) {
                final Element el = (Element) o;
                final String markSite = el.attributeValue(Uses.TASK_FOR_SITE);
                if (markSite.equals(netProperty.getServerAddress().getHostAddress() + ":" + netProperty.getServerPort())
                        || markSite.equals(netProperty.getServerAddress().getHostName() + ":" + netProperty.getServerPort())
                        || markSite.equals("127.0.0.1:" + netProperty.getServerPort())
                        || markSite.equals("localhost:" + netProperty.getServerPort())) {
                    FWelcome.startTime = new Date(Long.parseLong(el.attributeValue(Uses.TAG_START_TIME)));
                    FWelcome.finishTime = new Date(Long.parseLong(el.attributeValue(Uses.TAG_FINISH_TIME)));
                }
                //сложим наименования, печатаемые под картинкой
                captions.put(markSite, el.attributeValue(Uses.TAG_NAME));
            }
        } else {
            FWelcome.startTime = new Date(Long.parseLong(root.attributeValue(Uses.TAG_START_TIME)));
            FWelcome.finishTime = new Date(Long.parseLong(root.attributeValue(Uses.TAG_FINISH_TIME)));
        }
        FWelcome.caption = root.attributeValue(Uses.TAG_NAME);
    }
    /**
     * Список названий, которые печатаем под картинкой в случае домена.
     */
    public static final HashMap<String, String> captions = new HashMap<String, String>();

    @Override
    protected void finalize() throws Throwable {
        off();
        lockWelcome.stop();
        try {
            if (serialPort != null) {
                serialPort.free();
            }
        } catch (Exception ex) {
            throw new Uses.ClientException("Ошибка освобождения порта. " + ex);
        }
        super.finalize();
    }

    /**
     * Создаем и расставляем кнопки по форме.
     * @param current уровень отображения кнопок.
     */
    public void showButtons(Element current, JPanel panel) {

        Uses.log.logger.info("Показываем набор кнопок уровня: " + current.getTextTrim());
        FWelcome.current = current;
        clearPanel(panel);
        int delta = 10;
        switch (Toolkit.getDefaultToolkit().getScreenSize().width) {
            case 640:
                delta = 10;
                break;
            case 800:
                delta = 20;
                break;
            case 1024:
                delta = 30;
                break;
            case 1280:
                delta = 40;
                break;
            case 1600:
                delta = 50;
                break;
        }
        int cols = 3;
        int rows = 5;
        if (current.elements().size() < 4) {
            cols = 1;
            rows = 3;
        }
        if (current.elements().size() > 3 && current.elements().size() < 11) {
            cols = 2;
            rows = Math.round(new Float(current.elements().size()) / 2);
        }
        if (current.elements().size() > 10) {
            cols = 3;
            rows = Math.round(new Float(0.3) + current.elements().size() / 3);
        }

        GridLayout la = new GridLayout(rows, cols, delta, delta / 2);
        panel.setLayout(la);
        for (Object o : current.elements()) {
            Element el = (Element) o;
            QButton button = new QButton(el, this, panelMain, "");
            panel.add(button);
        }
        setVisible(true);
    }

    public void clearPanel(JPanel panel) {
        panel.removeAll();
        panel.repaint();
    }

    public static void printTicket(Element elTicket, String caption) {
        FWelcome.caption = caption;
        printTicket(elTicket);
    }

    public static synchronized void printTicket(Element elTicket) {
        final Element el = elTicket;
        Printable canvas = new Printable() {

            private int write(String text, int line, int x, double kx, double ky) {
                g2.scale(kx, ky);
                final int y = (int) Math.round((welcomeParams.topMargin + line * welcomeParams.lineHeigth) / ky);
                g2.drawString(text, x, y);
                g2.scale(1 / kx, 1 / ky);
                return y;
            }
            Graphics2D g2;

            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex >= 1) {
                    return Printable.NO_SUCH_PAGE;
                }
                g2 = (Graphics2D) graphics;
                if (welcomeParams.logo) {
                    g2.drawImage(Uses.loadImage(this, welcomeParams.logoImg), welcomeParams.logoLeft, welcomeParams.logoTop, null);
                }
                g2.scale(welcomeParams.scaleHorizontal, welcomeParams.scaleVertical);
                //позиционируем начало координат 
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                int line = 1;
                write(caption, line, welcomeParams.leftMargin, 1.5, 1.5);
                line++;
                write("Ваш номер:", ++line, 115, 1, 1);

                int x;
                final String suff = el.attributeValue(Uses.TAG_NUMBER);
                String pref = el.attributeValue(Uses.TAG_PREFIX);
                pref = "".equals(pref) ? "" : pref + "-";
                final String num = pref + suff;
                switch (num.length()) {
                    case 1:
                        x = 21;
                        break;
                    case 2:
                        x = 18;
                        break;
                    case 3:
                        x = 15;
                        break;
                    case 4:
                        x = 12;
                        break;
                    case 5:
                        x = 9;
                        break;
                    case 6:
                        x = 6;
                        break;
                    case 7:
                        x = 3;
                        break;
                    default: {
                        x = 0;
                    }
                }
                write(num, ++line + 2, x, 6, 3);


                line = line + 3;

                write("Услуга:", ++line, welcomeParams.leftMargin, 1.5, 1);
                String name = el.attributeValue(Uses.TAG_SERVICE);
                while (name.length() != 0) {
                    String prn;
                    if (name.length() > welcomeParams.lineLenght) {
                        int fl = 0;
                        for (int i = welcomeParams.lineLenght; i > 0; i--) {

                            if (" ".equals(name.substring(i - 1, i))) {
                                fl = i;
                                break;
                            }
                        }
                        int pos = fl == 0 ? welcomeParams.lineLenght : fl;
                        prn = name.substring(0, pos);
                        name = name.substring(pos, name.length());
                    } else {
                        prn = name;
                        name = "";
                    }
                    write(prn, ++line, welcomeParams.leftMargin, 1, 1);
                }

                write("Время:", ++line, welcomeParams.leftMargin, 1.5, 1);

                write(el.attributeValue(Uses.TAG_STAND_TIME), ++line, welcomeParams.leftMargin, 1, 1);
                if (el.attributeValue(Uses.TAG_PROP_INPUT_CAPTION) != null) {
                    write(el.attributeValue(Uses.TAG_PROP_INPUT_CAPTION), ++line, welcomeParams.leftMargin, 1, 1);
                    write(el.attributeValue(Uses.TAG_INPUT_DATA), ++line, welcomeParams.leftMargin, 1, 1);
                }
                write("Ждите вызова на табло", ++line, welcomeParams.leftMargin, 1.8, 1);
                write(welcomeParams.promoText, ++line, welcomeParams.leftMargin, 0.7, 0.4);
                int y = write("", ++line, 0, 1, 1);
                if (welcomeParams.barcode) {
                    Barcode barcode = null;
                    try {

                        barcode = BarcodeFactory.createCode128B(el.attributeValue(Uses.TAG_ID));
                    } catch (BarcodeException ex) {
                        Uses.log.logger.error("Ошибка создания штрихкода. " + ex);
                    }
                    barcode.setBarHeight(5);
                    barcode.setBarWidth(1);
                    barcode.setDrawingText(false);
                    barcode.setDrawingQuietSection(false);
                    try {
                        barcode.draw(g2, welcomeParams.leftMargin * 2, y - 7);
                    } catch (OutputException ex) {
                        Uses.log.logger.error("Ошибка вывода штрихкода. " + ex);
                    }
                }
                //Напечатаем текст внизу билета
                line = line + 2;
                name = welcomeParams.bottomText;
                while (name.length() != 0) {
                    String prn;
                    if (name.length() > welcomeParams.lineLenght) {
                        int fl = 0;
                        for (int i = welcomeParams.lineLenght; i > 0; i--) {

                            if (" ".equals(name.substring(i - 1, i))) {
                                fl = i;
                                break;
                            }
                        }
                        int pos = fl == 0 ? welcomeParams.lineLenght : fl;
                        prn = name.substring(0, pos);
                        name = name.substring(pos, name.length());
                    } else {
                        prn = name;
                        name = "";
                    }
                    write(prn, ++line, welcomeParams.leftMargin, 1, 1);
                }
                write(".", ++line + 2, 0, 1, 1);

                return Printable.PAGE_EXISTS;
            }
        };
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(canvas);
        PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
        attr.add(MediaSizeName.EXECUTIVE);
        // размер области
        /*
        int[] insets = {5, 0, 200, 200};
        attr.add(new MediaPrintableArea(
        insets[0], // отсуп слева 
        insets[1], // отсуп сверху 
        insets[2], // ширина 
        insets[3], // высота 
        MediaPrintableArea.MM));
         */
        try {
            job.print(attr);
            //job.print();
        } catch (PrinterException ex) {
            Uses.log.logger.error("Ошибка печати: ", ex);
        }
    }

    public static void printTicketAdvance(Element elTicket, String caption) {
        FWelcome.caption = caption;
        printTicketAdvance(elTicket);
    }

    public static synchronized void printTicketAdvance(Element elTicket) {
        final Element el = elTicket;
        Printable canvas = new Printable() {

            private int write(String text, int line, int x, double kx, double ky) {
                g2.scale(kx, ky);
                final int y = (int) Math.round((welcomeParams.topMargin + line * welcomeParams.lineHeigth) / ky);
                g2.drawString(text, x, y);
                g2.scale(1 / kx, 1 / ky);
                return y;
            }
            Graphics2D g2;

            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex >= 1) {
                    return Printable.NO_SUCH_PAGE;
                }
                g2 = (Graphics2D) graphics;
                if (welcomeParams.logo) {
                    g2.drawImage(Uses.loadImage(this, welcomeParams.logoImg), welcomeParams.logoLeft, welcomeParams.logoTop, null);
                }
                g2.scale(welcomeParams.scaleHorizontal, welcomeParams.scaleVertical);
                //позиционируем начало координат
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                int line = 1;
                write(caption, line, welcomeParams.leftMargin, 1.5, 1.5);
                line++;
                write("Талон предварительной записи на:", ++line, 20, 1, 1);

                final String time = el.attributeValue(Uses.TAG_START_TIME);
                final Date date;
                try {
                    date = Uses.format_for_trans.parse(time);
                } catch (ParseException ex) {
                    throw new Uses.ServerException("Неправильный парсинг даты начала недели для определения ранее записавшихся.");
                }
                final GregorianCalendar gc_time = new GregorianCalendar();
                gc_time.setTime(date);
                int t = gc_time.get(GregorianCalendar.HOUR_OF_DAY);
                if (t == 0) {
                    t = 24;
                    gc_time.add(GregorianCalendar.HOUR_OF_DAY, -1);
                }
                write(Uses.format_dd_MMMM_yyyy.format(gc_time.getTime()), ++line + 1, welcomeParams.leftMargin, 2, 1);
                write("c " + (t) + ":00 до " + (t + 1) + ":00", ++line + 1, welcomeParams.leftMargin, 2, 1);


                line = line + 2;

                write("Услуга:", ++line, welcomeParams.leftMargin, 1.5, 1);
                String name = el.attributeValue(Uses.TAG_SERVICE);
                while (name.length() != 0) {
                    String prn;
                    if (name.length() > welcomeParams.lineLenght) {
                        int fl = 0;
                        for (int i = welcomeParams.lineLenght; i > 0; i--) {

                            if (" ".equals(name.substring(i - 1, i))) {
                                fl = i;
                                break;
                            }
                        }
                        int pos = fl == 0 ? welcomeParams.lineLenght : fl;
                        prn = name.substring(0, pos);
                        name = name.substring(pos, name.length());
                    } else {
                        prn = name;
                        name = "";
                    }
                    write(prn, ++line, welcomeParams.leftMargin, 1, 1);
                }

                write("Время регистрации:", ++line, welcomeParams.leftMargin, 1.5, 1);

                write(Uses.format_for_label.format(new Date()), ++line, welcomeParams.leftMargin, 1, 1);

                write("Код предварительной записи:", ++line, welcomeParams.leftMargin, 1.3, 1);
                int y = write("", ++line, 0, 1, 1);
                if (welcomeParams.barcode) {
                    Barcode barcode = null;
                    try {

                        barcode = BarcodeFactory.createCode128B(el.attributeValue(Uses.TAG_ID));
                    } catch (BarcodeException ex) {
                        Uses.log.logger.error("Ошибка создания штрихкода. " + ex);
                    }
                    barcode.setBarHeight(5);
                    barcode.setBarWidth(1);
                    barcode.setDrawingText(true);
                    barcode.setDrawingQuietSection(true);
                    try {
                        barcode.draw(g2, welcomeParams.leftMargin * 2, y - 7);
                    } catch (OutputException ex) {
                        Uses.log.logger.error("Ошибка вывода штрихкода. " + ex);
                    }
                }
                line = line + 3;
                write(welcomeParams.promoText, ++line, welcomeParams.leftMargin, 0.7, 0.4);
                //Напечатаем текст внизу билета

                name = welcomeParams.bottomText;
                while (name.length() != 0) {
                    String prn;
                    if (name.length() > welcomeParams.lineLenght) {
                        int fl = 0;
                        for (int i = welcomeParams.lineLenght; i > 0; i--) {

                            if (" ".equals(name.substring(i - 1, i))) {
                                fl = i;
                                break;
                            }
                        }
                        int pos = fl == 0 ? welcomeParams.lineLenght : fl;
                        prn = name.substring(0, pos);
                        name = name.substring(pos, name.length());
                    } else {
                        prn = name;
                        name = "";
                    }
                    write(prn, ++line, welcomeParams.leftMargin, 1, 1);
                }
                write(".", ++line + 2, 0, 1, 1);

                return Printable.PAGE_EXISTS;
            }
        };
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(canvas);
        PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
        attr.add(MediaSizeName.EXECUTIVE);
        // размер области
        /*
        int[] insets = {5, 0, 200, 200};
        attr.add(new MediaPrintableArea(
        insets[0], // отсуп слева
        insets[1], // отсуп сверху
        insets[2], // ширина
        insets[3], // высота
        MediaPrintableArea.MM));
         */
        try {
            job.print(attr);
            //job.print();
        } catch (PrinterException ex) {
            Uses.log.logger.error("Ошибка печати: ", ex);
        }
    }

    public static synchronized void printPreInfoText(final String preInfo) {

        Printable canvas = new Printable() {

            private int writeText(String text, int line, int x, double kx, double ky) {
                while (text.length() != 0) {
                    String prn;
                    if (text.length() > welcomeParams.lineLenght) {
                        int fl = 0;
                        for (int i = welcomeParams.lineLenght; i > 0; i--) {

                            if (" ".equals(text.substring(i - 1, i))) {
                                fl = i;
                                break;
                            }
                        }
                        int pos = fl == 0 ? welcomeParams.lineLenght : fl;
                        prn = text.substring(0, pos);
                        text = text.substring(pos, text.length());
                    } else {
                        prn = text;
                        text = "";
                    }
                    write(prn, ++line, x, kx, ky);
                }
                return line;
            }

            private int write(String text, int line, int x, double kx, double ky) {
                g2.scale(kx, ky);
                final int y = (int) Math.round((welcomeParams.topMargin + line * welcomeParams.lineHeigth) / ky);
                g2.drawString(text, x, y);
                g2.scale(1 / kx, 1 / ky);
                return y;
            }
            Graphics2D g2;

            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex >= 1) {
                    return Printable.NO_SUCH_PAGE;
                }
                g2 = (Graphics2D) graphics;
                if (welcomeParams.logo) {
                    g2.drawImage(Uses.loadImage(this, welcomeParams.logoImg), welcomeParams.logoLeft, welcomeParams.logoTop, null);
                }
                g2.scale(welcomeParams.scaleHorizontal, welcomeParams.scaleVertical);
                //позиционируем начало координат
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                int line = 1;
                write(caption, line, welcomeParams.leftMargin, 1.5, 1.5);
                // напечатаем текст подсказки
                final Scanner sc = new Scanner(preInfo.replace("<brk>", "\n"));
                while (sc.hasNextLine()) {
                    final String w = sc.nextLine();
                    line = writeText(w, line, welcomeParams.leftMargin, 1, 1);
                }
                write(welcomeParams.promoText, ++line, welcomeParams.leftMargin, 0.7, 0.4);
                //Напечатаем текст внизу билета

                line = writeText(welcomeParams.bottomText, line, welcomeParams.leftMargin, 1, 1);
                write(".", line + 2, 0, 1, 1);

                return Printable.PAGE_EXISTS;
            }
        };
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(canvas);
        PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
        attr.add(MediaSizeName.EXECUTIVE);
        try {
            job.print(attr);
        } catch (PrinterException ex) {
            Uses.log.logger.error("Ошибка печати: ", ex);
        }
    }

    public void setVisibleButtons(boolean visible) {
        buttonBack.setVisible(visible);
        buttonToBegin.setVisible(visible);

        buttonStandAdvance.setVisible(welcomeParams.advance && visible);
        buttonAdvance.setVisible(welcomeParams.advance && visible);

        buttonInfo.setVisible(welcomeParams.info && visible);
        buttonResponse.setVisible(welcomeParams.response && visible);
    }
    //==================================================================================================================
    //С рабочего места администратора должна быть возможность заблокировать пункт постановки в очередь, 
    //разблокировать, выключить, провести инициализация заново.
    private String stateWindow = UNLOCK;

    public String getStateWindow() {
        return stateWindow;
    }

    public void setStateWindow(String state) {
        this.stateWindow = state;
        panelLock.setVisible(LOCK.equals(state));
        panelMain.setVisible(UNLOCK.equals(state));
        if (isMed) {
            if (LOCK.equals(state)) {
                FMedCheckIn.setBlockDialog(true);
            }
            if (UNLOCK.equals(state)) {
                FMedCheckIn.setBlockDialog(false);
            }
        }
    }

    /**
     * Заблокировать пункт постановки в очередь.
     * @param message Сообщение, которое выведется на экран пункта.
     */
    public void lock(String message) {
        labelLock.setText(message);
        setStateWindow(LOCK);
        setVisibleButtons(false);
        Uses.log.logger.info("Пункт регистрации заблокирован. Состояние \"" + stateWindow + "\"");
    }

    /**
     * Разблокировать пункт постановки в очередь.
     */
    public void unlock() {
        setStateWindow(UNLOCK);
        setVisibleButtons(true);
        Uses.log.logger.info("Пункт регистрации готов к работе. Состояние \"" + stateWindow + "\"");
    }

    /**
     * Выключить пункт постановки в очередь.
     */
    public void off() {
        setStateWindow(OFF);
        exitServer = true;
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Uses.log.logger.error("Проблемы с таймером. ", ex);
        }
        Uses.log.logger.info("Пункт регистрации выключен. Состояние \"" + stateWindow + "\"");
    }

    /**
     * Инициализация заново пункта постановки в очередь.
     */
    public void reinit() {
        final Element reroot = NetCommander.getServiсes(netProperty);
        FWelcome.root = reroot;
        FWelcome.current = reroot;
        FWelcome.response = null;
        FWelcome.infoTree = null;
        loadRootParam();
        Uses.log.logger.info("Пункт регистрации реинициализирован. Состояние \"" + stateWindow + "\"");
    }
    /**
     * Таймер, по которому будем включать и выключать пункт регистрации.
     */
    public ATalkingClock lockWelcome = new ATalkingClock(Uses.DELAY_CHECK_TO_LOCK, 0) {

        @Override
        public void run() {
            // если время начала и завершения совпадают, то игнор блокировки.
            if (Uses.format_HH_mm.format(finishTime).equals(Uses.format_HH_mm.format(startTime))) {
                return;
            }
            if (Uses.format_HH_mm.format(new Date()).equals(Uses.format_HH_mm.format(finishTime))) {
                lock("<HTML><p align=center><b><span style='font-size:40.0pt;color:red'>Регистрация клиентов остановлена.</span></b></p>");
            }
            if (Uses.format_HH_mm.format(new Date()).equals(Uses.format_HH_mm.format(startTime))) {
                unlock();
            }
        }
    };
    //==================================================================================================================

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelBackground = new QPanel("/ru/apertum/qsystem/client/forms/resources/fon_welcome.jpg");
        panelCaption = new javax.swing.JPanel();
        labelCaption = new javax.swing.JLabel();
        panelButtons = new javax.swing.JPanel();
        buttonAdvance = new javax.swing.JButton();
        buttonStandAdvance = new javax.swing.JButton();
        buttonToBegin = new javax.swing.JButton();
        buttonBack = new javax.swing.JButton();
        panelCentre = new javax.swing.JPanel();
        panelMain = new javax.swing.JPanel();
        panelLock = new javax.swing.JPanel();
        labelLock = new javax.swing.JLabel();
        buttonInfo = new javax.swing.JButton();
        buttonResponse = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getResourceMap(FWelcome.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(640, 480));
        setName("Form"); // NOI18N

        panelBackground.setBorder(new javax.swing.border.MatteBorder(null));
        panelBackground.setName("panelBackground"); // NOI18N

        panelCaption.setBorder(new javax.swing.border.MatteBorder(null));
        panelCaption.setName("panelCaption"); // NOI18N
        panelCaption.setOpaque(false);

        labelCaption.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelCaption.setText(resourceMap.getString("labelCaption.text")); // NOI18N
        labelCaption.setName("labelCaption"); // NOI18N

        javax.swing.GroupLayout panelCaptionLayout = new javax.swing.GroupLayout(panelCaption);
        panelCaption.setLayout(panelCaptionLayout);
        panelCaptionLayout.setHorizontalGroup(
            panelCaptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelCaption, javax.swing.GroupLayout.DEFAULT_SIZE, 929, Short.MAX_VALUE)
        );
        panelCaptionLayout.setVerticalGroup(
            panelCaptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelCaption, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
        );

        panelButtons.setBorder(new javax.swing.border.MatteBorder(null));
        panelButtons.setName("panelButtons"); // NOI18N
        panelButtons.setOpaque(false);
        panelButtons.setLayout(new java.awt.GridLayout(1, 0));

        buttonAdvance.setFont(resourceMap.getFont("buttonAdvance.font")); // NOI18N
        buttonAdvance.setText(resourceMap.getString("buttonAdvance.text")); // NOI18N
        buttonAdvance.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        buttonAdvance.setName("buttonAdvance"); // NOI18N
        buttonAdvance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAdvanceActionPerformed(evt);
            }
        });
        panelButtons.add(buttonAdvance);

        buttonStandAdvance.setFont(resourceMap.getFont("buttonStandAdvance.font")); // NOI18N
        buttonStandAdvance.setText(resourceMap.getString("buttonStandAdvance.text")); // NOI18N
        buttonStandAdvance.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        buttonStandAdvance.setName("buttonStandAdvance"); // NOI18N
        buttonStandAdvance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStandAdvanceActionPerformed(evt);
            }
        });
        panelButtons.add(buttonStandAdvance);

        buttonToBegin.setFont(resourceMap.getFont("buttonToBegin.font")); // NOI18N
        buttonToBegin.setIcon(resourceMap.getIcon("buttonToBegin.icon")); // NOI18N
        buttonToBegin.setText(resourceMap.getString("buttonToBegin.text")); // NOI18N
        buttonToBegin.setActionCommand(resourceMap.getString("buttonToBegin.actionCommand")); // NOI18N
        buttonToBegin.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        buttonToBegin.setName("buttonToBegin"); // NOI18N
        buttonToBegin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonToBeginActionPerformed(evt);
            }
        });
        panelButtons.add(buttonToBegin);

        buttonBack.setFont(resourceMap.getFont("buttonBack.font")); // NOI18N
        buttonBack.setIcon(resourceMap.getIcon("buttonBack.icon")); // NOI18N
        buttonBack.setText(resourceMap.getString("buttonBack.text")); // NOI18N
        buttonBack.setActionCommand(resourceMap.getString("buttonBack.actionCommand")); // NOI18N
        buttonBack.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        buttonBack.setName("buttonBack"); // NOI18N
        buttonBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonBackActionPerformed(evt);
            }
        });
        panelButtons.add(buttonBack);

        panelCentre.setBorder(new javax.swing.border.MatteBorder(null));
        panelCentre.setName("panelCentre"); // NOI18N
        panelCentre.setOpaque(false);

        panelMain.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelMain.setFont(resourceMap.getFont("panelMain.font")); // NOI18N
        panelMain.setName("panelMain"); // NOI18N
        panelMain.setOpaque(false);

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 854, Short.MAX_VALUE)
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 366, Short.MAX_VALUE)
        );

        panelLock.setBorder(new javax.swing.border.MatteBorder(null));
        panelLock.setName("panelLock"); // NOI18N
        panelLock.setOpaque(false);

        labelLock.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelLock.setText(resourceMap.getString("labelLock.text")); // NOI18N
        labelLock.setName("labelLock"); // NOI18N

        javax.swing.GroupLayout panelLockLayout = new javax.swing.GroupLayout(panelLock);
        panelLock.setLayout(panelLockLayout);
        panelLockLayout.setHorizontalGroup(
            panelLockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLockLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelLock, javax.swing.GroupLayout.DEFAULT_SIZE, 836, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelLockLayout.setVerticalGroup(
            panelLockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLockLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelLock, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                .addContainerGap())
        );

        buttonInfo.setFont(resourceMap.getFont("buttonInfo.font")); // NOI18N
        buttonInfo.setText(resourceMap.getString("buttonInfo.text")); // NOI18N
        buttonInfo.setActionCommand(resourceMap.getString("buttonInfo.actionCommand")); // NOI18N
        buttonInfo.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        buttonInfo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonInfo.setName("buttonInfo"); // NOI18N
        buttonInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonInfoActionPerformed(evt);
            }
        });

        buttonResponse.setFont(resourceMap.getFont("buttonResponse.font")); // NOI18N
        buttonResponse.setText(resourceMap.getString("buttonResponse.text")); // NOI18N
        buttonResponse.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED)));
        buttonResponse.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonResponse.setName("buttonResponse"); // NOI18N
        buttonResponse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResponseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelCentreLayout = new javax.swing.GroupLayout(panelCentre);
        panelCentre.setLayout(panelCentreLayout);
        panelCentreLayout.setHorizontalGroup(
            panelCentreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCentreLayout.createSequentialGroup()
                .addGroup(panelCentreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelLock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelMain, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCentreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(buttonInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonResponse, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        panelCentreLayout.setVerticalGroup(
            panelCentreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCentreLayout.createSequentialGroup()
                .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelLock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(panelCentreLayout.createSequentialGroup()
                .addComponent(buttonInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonResponse, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelBackgroundLayout = new javax.swing.GroupLayout(panelBackground);
        panelBackground.setLayout(panelBackgroundLayout);
        panelBackgroundLayout.setHorizontalGroup(
            panelBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelCaption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, 931, Short.MAX_VALUE)
            .addComponent(panelCentre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelBackgroundLayout.setVerticalGroup(
            panelBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBackgroundLayout.createSequentialGroup()
                .addComponent(panelCaption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelCentre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelBackground, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelBackground, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void buttonBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonBackActionPerformed
    if (!current.equals(root)) {
        showButtons(current.getParent(), panelMain);
    }
}//GEN-LAST:event_buttonBackActionPerformed

private void buttonToBeginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonToBeginActionPerformed
    if (!current.equals(root)) {
        showButtons(root, panelMain);
        current = root;
    }
}//GEN-LAST:event_buttonToBeginActionPerformed
    private static boolean advanceRegim = false;

    public static boolean isAdvanceRegim() {
        return advanceRegim;
    }

    /**
     * Переключение режима постановки в очередь и предварительной записи
     * @param advanceRegim true - предварительная запись, false - встать в очередь
     */
    public void setAdvanceRegim(boolean advanceRegim) {
        FWelcome.advanceRegim = advanceRegim;
        buttonToBeginActionPerformed(null);
        if (advanceRegim) {
            labelCaption.setText("<html><b><p align=center><span style='font-size:30.0pt;color:red'>Выберите услугу для предварительной записи");
            buttonAdvance.setText("<html><p align=center>Отменить<br/>запись");
            //возврат в начальное состояние из диалога предварительной записи.
            if (clockBack.isActive()) {
                clockBack.stop();
            }
            clockBack.start();
        } else {
            if (clockBack.isActive()) {
                clockBack.stop();
            }
            labelCaption.setText(root.getTextTrim());
            buttonAdvance.setText("<html><p align=center>Предварительная<br/>запись");
        }
        //кнопка регистрации пришедших которые записались давно видна только в стандартном режиме и вместе с кнопкой предварительной записи
        if (buttonAdvance.isVisible()) {
            buttonStandAdvance.setVisible(!advanceRegim);
        }
    }

    /**
     * Заставка на некоторый таймаут
     * @param text текст на заставке
     * @param imagePath картинка на заставке
     */
    public ATalkingClock showDelayFormPrint(String text, String imagePath) {
        setVisibleButtons(false);
        ATalkingClock clock = new ATalkingClock(FWelcome.welcomeParams.delayPrint, 1) {

            @Override
            public void run() {
                setVisibleButtons(true);
                showButtons(root, panelMain);
                showMed();
            }
        };
        clock.start();
        clearPanel(panelMain);
        panelMain.setLayout(new GridLayout(1, 1, 1, 1));
        labelInfo.setText(text);
        labelInfo.setHorizontalAlignment(JLabel.CENTER);
        labelInfo.setIcon(new ImageIcon(getClass().getResource(imagePath)));
        panelMain.add(labelInfo);
        labelInfo.setBounds(0, 0, 200, 200);
        panelMain.repaint();
        labelInfo.repaint();
        return clock;
    }
    private JLabel labelInfo = new JLabel();

private void buttonAdvanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAdvanceActionPerformed
    setAdvanceRegim(!isAdvanceRegim());
    if (isMed && !isAdvanceRegim()) {
        showMed();
    }
}//GEN-LAST:event_buttonAdvanceActionPerformed

private void buttonStandAdvanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStandAdvanceActionPerformed
    final Element res = FStandAdvance.showAdvanceStandDialog(this, true, FWelcome.netProperty, true, FWelcome.welcomeParams.delayBack * 2);

    if (res != null) {

        if (res.attributeValue(Uses.TAG_ID) != null) {

            showDelayFormPrint("<HTML><b><p align=center><span style='font-size:30.0pt;color:green'>Пожалуйста возьмите талон!<br></span>"
                    + "<span style='font-size:20.0pt;color:blue'>ваш номер<br></span>"
                    + "<span style='font-size:100.0pt;color:blue'>" + res.attributeValue(Uses.TAG_PREFIX) + res.attributeValue(Uses.TAG_NUMBER) + "</span></p>",
                    "/ru/apertum/qsystem/client/forms/resources/getTicket.png");

            Uses.log.logger.info("Печать этикетки.");

            new Thread(new Runnable() {

                @Override
                public void run() {
                    // Для доменной системы на этикетке под картинкой выводим наименование с своего сайта
                    FWelcome.printTicket(res);
                }
            }).start();
        } else {
            showDelayFormPrint("<HTML><b><p align=center><span style='font-size:30.0pt;color:red'>" + res.getTextTrim(),
                    "/ru/apertum/qsystem/client/forms/resources/noActive.png");
        }
    }

}//GEN-LAST:event_buttonStandAdvanceActionPerformed

private void buttonResponseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResponseActionPerformed
    final Long res = FResponseDialog.showResponseDialog(this, getResponse(), true, true, FWelcome.welcomeParams.delayBack * 2);
    if (res != null) {
        NetCommander.setResponseAnswer(netProperty, res);
    }
}//GEN-LAST:event_buttonResponseActionPerformed

private void buttonInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonInfoActionPerformed
    FInfoDialog.showResponseDialog(this, getInfoTree(), true, true, FWelcome.welcomeParams.delayBack * 3);
}//GEN-LAST:event_buttonInfoActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAdvance;
    private javax.swing.JButton buttonBack;
    private javax.swing.JButton buttonInfo;
    private javax.swing.JButton buttonResponse;
    private javax.swing.JButton buttonStandAdvance;
    private javax.swing.JButton buttonToBegin;
    private javax.swing.JLabel labelCaption;
    private javax.swing.JLabel labelLock;
    private javax.swing.JPanel panelBackground;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel panelCaption;
    private javax.swing.JPanel panelCentre;
    private javax.swing.JPanel panelLock;
    private javax.swing.JPanel panelMain;
    // End of variables declaration//GEN-END:variables
}
