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
package ru.apertum.qsystem.client.forms;

import ru.apertum.qsystem.client.common.WelcomeParams;
import com.google.gson.Gson;
import java.awt.Cursor;
import java.awt.Dimension;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Scanner;
import java.util.ServiceLoader;
import javax.imageio.ImageIO;
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
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import ru.apertum.qsystem.QSystem;
import ru.apertum.qsystem.client.Locales;
import ru.apertum.qsystem.client.common.ClientNetProperty;
import ru.apertum.qsystem.common.NetCommander;
import ru.apertum.qsystem.client.model.QButton;
import ru.apertum.qsystem.client.model.QPanel;
import ru.apertum.qsystem.common.GsonPool;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.cmd.JsonRPC20;
import ru.apertum.qsystem.common.cmd.RpcGetAllServices;
import ru.apertum.qsystem.common.cmd.RpcGetSrt;
import ru.apertum.qsystem.common.cmd.RpcStandInService;
import ru.apertum.qsystem.common.exceptions.ClientException;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.apertum.qsystem.common.model.ATalkingClock;
import ru.apertum.qsystem.common.model.IClientNetProperty;
import ru.apertum.qsystem.common.model.QCustomer;
import ru.apertum.qsystem.extra.IPrintTicket;
import ru.apertum.qsystem.server.model.QAdvanceCustomer;
import ru.apertum.qsystem.server.model.QAuthorizationCustomer;
import ru.apertum.qsystem.server.model.QService;
import ru.apertum.qsystem.server.model.infosystem.QInfoItem;
import ru.apertum.qsystem.server.model.response.QRespItem;
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

    private static ResourceMap localeMap = null;

    public static String getLocaleMessage(String key) {
        if (localeMap == null) {
            localeMap = Application.getInstance(QSystem.class).getContext().getResourceMap(FWelcome.class);
        }
        return localeMap.getString(key);
    }
    // Состояния пункта регистрации
    public static final String LOCK = "Заблокирован";
    public static final String UNLOCK = "Готов к работе";
    public static final String OFF = "Выключен";
    public static String LOCK_MESSAGE = "<HTML><p align=center><b><span style='font-size:40.0pt;color:red'>" + getLocaleMessage("messages.lock_messages") + "</span></b></p>";
    public static QService root;
    public static int pageNumber = 0;// на одном уровне может понадобиться листать услуги, не то они расползуться. Это вместо скрола.
    /**
     * XML-список отзывов. перврначально null, грузится при первом обращении. Использовать через геттер.
     */
    private static LinkedList<QRespItem> response = null;

    public static LinkedList<QRespItem> getResponse() {
        if (response == null) {
            response = NetCommander.getResporseList(netProperty);
        }
        return response;
    }
    /**
     * XML- дерево информации. перврначально null, грузится при первом обращении. Использовать через геттер.
     */
    private static QInfoItem infoTree = null;

    public static QInfoItem getInfoTree() {
        if (infoTree == null) {
            infoTree = NetCommander.getInfoTree(netProperty);
        }
        return infoTree;
    }
    protected static QService current;
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
    public static IClientNetProperty netProperty;
    /**
     * Режим предварительной записи в поликлинике
     */
    public static boolean isMed = false;
    /**
     * Режим инфокиоска, когда получить всю инфу с пункта регистрации можно, а встать в очередь нельзя
     */
    public static boolean isInfo = false;
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
                throw new ClientException("Ошибка при создании серверного сокета: " + e);
            }

            System.out.println("Server for managment of registration point started.\n");
            QLog.l().logger().info("Сервер управления пунктом регистрации запущен.");

            // слушаем порт
            while (!exitServer) {
                // ждём нового подключения, после чего запускаем обработку клиента
                // в новый вычислительный поток и увеличиваем счётчик на единичку
                try {
                    doCommand(server.accept());
                } catch (SocketTimeoutException e) {
                } catch (IOException e) {
                    QLog.l().logger().error("Управлялка пунктом чет подглючила.", e);
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
                    throw new ServerException("Ошибка при получении входного потока: " + e.getStackTrace());
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
                    throw new ServerException("Ошибка при чтении из входного потока: " + ex.getStackTrace());
                } catch (InterruptedException ex) {
                    throw new ServerException("Проблема со сном: " + ex.getStackTrace());
                }
                QLog.l().logger().trace("Задание:\n" + data);

                final JsonRPC20 rpc;
                final Gson gson = GsonPool.getInstance().borrowGson();
                try {
                    rpc = gson.fromJson(data, JsonRPC20.class);
                } finally {
                    GsonPool.getInstance().returnGson(gson);
                }

                // Обрабатываем задание
                //С рабочего места администратора должна быть возможность заблокировать пункт постановки в очередь, 
                //разблокировать, выключить, провести инициализация заново.
                // В любом другом случае будет выслано состояние.
                if (Uses.WELCOME_LOCK.equals(rpc.getMethod())) {
                    lock(LOCK_MESSAGE);
                }
                if (Uses.WELCOME_UNLOCK.equals(rpc.getMethod())) {
                    unlock();
                }
                if (Uses.WELCOME_OFF.equals(rpc.getMethod())) {
                    off();
                }
                if (Uses.WELCOME_REINIT.equals(rpc.getMethod())) {
                    reinit();
                }

                // выводим данные:
                QLog.l().logger().trace("Ответ:\n" + stateWindow);
                final String rpc_resp;
                final Gson gson_resp = GsonPool.getInstance().borrowGson();
                try {
                    rpc_resp = gson.toJson(new RpcGetSrt(stateWindow));
                } finally {
                    GsonPool.getInstance().returnGson(gson_resp);
                }
                try {
                    // Передача данных ответа
                    final PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    writer.print(rpc_resp);
                    writer.flush();
                } catch (IOException e) {
                    throw new ServerException("Ошибка при записи в поток: " + e.getStackTrace());
                }
            } finally {
                // завершаем соединение
                try {
                    //оборативаем close, т.к. он сам может сгенерировать ошибку IOExeption. Просто выкинем Стек-трейс
                    socket.close();
                } catch (IOException e) {
                    QLog.l().logger().error(e);
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
    public ATalkingClock clockBack = new ATalkingClock(WelcomeParams.getInstance().delayBack, 1) {

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
    public ATalkingClock clockUnlockBack = new ATalkingClock(WelcomeParams.getInstance().delayPrint, 1) {

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
     * @throws Exception 
     */
    public static void main(final String args[]) throws Exception {
        QLog.initial(args, false);
        // Загрузка плагинов из папки plugins
        Uses.loadPlugins("./plugins/");
        Locale.setDefault(Locales.getInstance().getLangCurrent());
        LOCK_MESSAGE = "<HTML><p align=center><b><span style='font-size:40.0pt;color:red'>" + getLocaleMessage("messages.lock_messages") + "</span></b></p>";
        netProperty = new ClientNetProperty(args);
        // определим режим пользовательского интерфейса
        for (String s : args) {
            if ("med".equals(s)) {
                isMed = true;
                if (!"".equals(WelcomeParams.getInstance().buttons_COM)) {
                    serialPort = new RxtxSerialPort(WelcomeParams.getInstance().buttons_COM);
                    serialPort.setDataBits(WelcomeParams.getInstance().buttons_databits);
                    serialPort.setParity(WelcomeParams.getInstance().buttons_parity);
                    serialPort.setSpeed(WelcomeParams.getInstance().buttons_speed);
                    serialPort.setStopBits(WelcomeParams.getInstance().buttons_stopbits);
                }
            }
            if ("info".equals(s)) {
                isInfo = true;
            }
        }
        final RpcGetAllServices.ServicesForWelcome servs = NetCommander.getServiсes(netProperty);
        root = servs.getRoot();
        FWelcome.startTime = servs.getStartTime();
        FWelcome.finishTime = servs.getFinishTime();
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                final FWelcome w = new FWelcome(root);
                w.setVisible(true);
            }
        });
    }

    public FWelcome(QService root) {
        QLog.l().logger().info("Создаем окно приглашения.");
        if (!QLog.l().isDebug()) {
            if (!QLog.l().isDemo()) {
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

        if (QLog.l().isDemo()) {
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowOpened(WindowEvent e) {
                    setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
            });
        }
        initComponents();
        if (WelcomeParams.getInstance().topSize != 0) {
            panelCaption.setPreferredSize(new Dimension(panelCaption.getWidth(), WelcomeParams.getInstance().topSize));
        }
        try {
            setIconImage(ImageIO.read(FAdmin.class.getResource("/ru/apertum/qsystem/client/forms/resources/checkIn.png")));
        } catch (IOException ex) {
            System.err.println(ex);
        }
        if (QLog.l().isDebug()) {
            setSize(1280, 1024);
        }
        FWelcome.root = root;
        FWelcome.current = root;
        FWelcome.response = null;
        FWelcome.infoTree = null;
        try {
            loadRootParam();
        } catch (Exception ex) {
            QLog.l().logger().error(ex);
            System.exit(0);
        }
        server.start();
        if (!(Uses.format_HH_mm.format(finishTime).equals(Uses.format_HH_mm.format(startTime)))) {
            lockWelcome.start();
        }
        /*
         * Кнопки открываются по настройке
         */
        buttonInfo.setVisible(WelcomeParams.getInstance().info);
        buttonResponse.setVisible(WelcomeParams.getInstance().response);
        buttonAdvance.setVisible(WelcomeParams.getInstance().advance);
        buttonStandAdvance.setVisible(WelcomeParams.getInstance().advance);
        showMed();
        // Если режим инфокиоска, то не показываем кнопки предвариловки
        // Показали информацию и все
        if (FWelcome.isInfo) {
            buttonAdvance.setVisible(false);
            buttonStandAdvance.setVisible(false);
        }
    }

    /**
     * Загрузка и инициализация неких параметров из корня дерева описания для старта или реинициализации.
     */
    private void loadRootParam() {
        FWelcome.caption = root.getName();
        labelCaption.setText(Uses.prepareAbsolutPathForImg(root.getButtonText()));
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

    public final void showMed() {
        if (isMed) {
            final ATalkingClock cl = new ATalkingClock(10, 1) {

                @Override
                public void run() {
                    if (!FMedCheckIn.isShowen()) {
                        final QAuthorizationCustomer customer = FMedCheckIn.showMedCheckIn(null, true, netProperty, false, serialPort);
                        if (customer != null) {
                            advancedCustomer = customer.getId();
                            setAdvanceRegim(true);
                            labelCaption.setText("<html><b><p align=center><span style='font-size:35.0pt;color:green'>" + customer.getSurname() + " " + customer.getName() + " " + customer.getOtchestvo() + "<br></span><span style='font-size:30.0pt;color:red'>" + getLocaleMessage("messages.select_adv_servece"));
                        } else {
                            throw new ClientException("Нельзя выбирать услугу если не идентифицирован клиент.");
                        }
                    }
                }
            };
            cl.start();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        off();
        lockWelcome.stop();
        try {
            if (serialPort != null) {
                serialPort.free();
            }
        } catch (Exception ex) {
            throw new ClientException("Ошибка освобождения порта. " + ex);
        }
        super.finalize();
    }

    /**
     * Создаем и расставляем кнопки по форме.
     * @param current уровень отображения кнопок.
     * @param panel
     */
    public void showButtons(QService current, JPanel panel) {

        QLog.l().logger().info("Показываем набор кнопок уровня: " + current.getName());
        if (current != FWelcome.current) { // если смена уровней то страница уровня становится нулевая
            pageNumber = 0;
        }
        if (current != root && current.getParent() == null) {
            current.setParent(FWelcome.current);
        }
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
            case 1920:
                delta = 60;
                break;
        }
        int cols = 3;
        int rows = 5;

        // посмотрим сколько реальных кнопок нужно отобразить
        // тут есть невидимые услуги и услуги не с того киоска
        int childCount = 0;
        for (QService service : current.getChildren()) {
            if (service.getStatus() != -1 && (WelcomeParams.getInstance().point == 0 || (service.getPoint() == 0 || service.getPoint() == WelcomeParams.getInstance().point))) {
                childCount++;
            }
        }

        if (childCount < 4) {
            cols = 1;
            rows = 3;
        }
        if (childCount > 3 && childCount < 11) {
            cols = 2;
            rows = Math.round(new Float(childCount) / 2);
        }
        if (childCount > 10) {
            cols = 3;
            rows = Math.round(new Float(0.3) + (float) childCount / 3);
        }

        // поправка на то что если кнопок на уровне много и они уже в три колонки, то задействуем ограничение по линиям, а то расползутся
        if (rows > WelcomeParams.getInstance().linesButtonCount && cols >= 3) {
            rows = WelcomeParams.getInstance().linesButtonCount;
            panelForPaging.setVisible(true);
        } else {
            panelForPaging.setVisible(false);
        }

        final GridLayout la = new GridLayout(rows, cols, delta, delta / 2);
        panel.setLayout(la);
        int i = 0;
        for (QService service : current.getChildren()) {
            boolean f = true;
            if (i / (cols * rows) != pageNumber) { // смотрим каая страница из текущего уровня отображается
                f = false;
            }

            final QButton button = new QButton(service, this, panelMain, WelcomeParams.getInstance().buttonType);
            if (button.isIsVisible() && (WelcomeParams.getInstance().point == 0 || (service.getPoint() == 0 || service.getPoint() == WelcomeParams.getInstance().point))) {
                if (f) {
                    panel.add(button);
                    buttonForwardPage.setEnabled((i + 1) != childCount); // это чтоб кнопки листания небыли доступны когда листать дальше некуда
                }
                i++;
            }
        }
        buttonBackPage.setEnabled(pageNumber > 0); // это чтоб кнопки листания небыли доступны когда листать дальше некуда

        setVisible(true);
        buttonBack.setVisible(current != root);
        buttonToBegin.setVisible(current != root);
    }

    public void clearPanel(JPanel panel) {
        panel.removeAll();
        panel.repaint();
    }

    public static void printTicket(QCustomer customer, String caption) {
        FWelcome.caption = caption;
        printTicket(customer);
    }

    public static synchronized void printTicket(final QCustomer customer) {

        // поддержка расширяемости плагинами
        boolean flag = false;
        for (final IPrintTicket event : ServiceLoader.load(IPrintTicket.class)) {
            QLog.l().logger().info("Вызов SPI расширения. Описание: " + event.getDescription());
            try {
                flag = event.printTicket(customer, FWelcome.caption);
            } catch (Throwable tr) {
                QLog.l().logger().error("Вызов SPI расширения завершился ошибкой. Описание: " + tr);
            }
            // раз напечатили и хорошь
            if (flag) {
                return;
            }
        }

        final Printable canvas = new Printable() {

            private int write(String text, int line, int x, double kx, double ky) {
                g2.scale(kx, ky);
                final int y = (int) Math.round((WelcomeParams.getInstance().topMargin + line * WelcomeParams.getInstance().lineHeigth) / ky);
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
                if (WelcomeParams.getInstance().logo) {
                    g2.drawImage(Uses.loadImage(this, WelcomeParams.getInstance().logoImg, "/ru/apertum/qsystem/client/forms/resources/logo_ticket_a.png"), WelcomeParams.getInstance().logoLeft, WelcomeParams.getInstance().logoTop, null);
                }
                g2.scale(WelcomeParams.getInstance().scaleHorizontal, WelcomeParams.getInstance().scaleVertical);
                //позиционируем начало координат 
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                int line = 1;
                write(caption, line, WelcomeParams.getInstance().leftMargin, 1.5, 1.5);
                line++;
                write(getLocaleMessage("ticket.your_number"), ++line, 115, 1, 1);

                int x;
                final String num = ("".equals(customer.getPrefix()) ? "" : customer.getPrefix() + "-") + customer.getNumber();
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

                write(getLocaleMessage("ticket.service"), ++line, WelcomeParams.getInstance().leftMargin, 1.5, 1);
                String name = customer.getService().getName();
                while (name.length() != 0) {
                    String prn;
                    if (name.length() > WelcomeParams.getInstance().lineLenght) {
                        int fl = 0;
                        for (int i = WelcomeParams.getInstance().lineLenght; i > 0; i--) {

                            if (" ".equals(name.substring(i - 1, i))) {
                                fl = i;
                                break;
                            }
                        }
                        int pos = fl == 0 ? WelcomeParams.getInstance().lineLenght : fl;
                        prn = name.substring(0, pos);
                        name = name.substring(pos, name.length());
                    } else {
                        prn = name;
                        name = "";
                    }
                    write(prn, ++line, WelcomeParams.getInstance().leftMargin, 1, 1);
                }

                write(getLocaleMessage("ticket.time"), ++line, WelcomeParams.getInstance().leftMargin, 1.5, 1);

                write(Uses.format_for_label.format(customer.getStandTime()), ++line, WelcomeParams.getInstance().leftMargin, 1, 1);
                // если клиент что-то ввел, то напечатаем это на его талоне
                if (customer.getService().getInput_required()) {
                    write(customer.getService().getInput_caption().replaceAll("<.*?>",""), ++line, WelcomeParams.getInstance().leftMargin, 1, 1);
                    write(customer.getInput_data(), ++line, WelcomeParams.getInstance().leftMargin, 1, 1);
                }
                // если в услуге есть что напечатать на талоне, то напечатаем это на его талоне
                if (customer.getService().getTicketText() != null && !customer.getService().getTicketText().isEmpty()) {
                    String tt = customer.getService().getTicketText();
                    while (tt.length() != 0) {
                        String prn;
                        if (tt.length() > WelcomeParams.getInstance().lineLenght) {
                            int fl = 0;
                            for (int i = WelcomeParams.getInstance().lineLenght; i > 0; i--) {

                                if (" ".equals(tt.substring(i - 1, i))) {
                                    fl = i;
                                    break;
                                }
                            }
                            int pos = fl == 0 ? WelcomeParams.getInstance().lineLenght : fl;
                            prn = tt.substring(0, pos);
                            tt = tt.substring(pos, tt.length());
                        } else {
                            prn = tt;
                            tt = "";
                        }
                        write(prn, ++line, WelcomeParams.getInstance().leftMargin, 1, 1);
                    }
                }
                write(getLocaleMessage("ticket.wait"), ++line, WelcomeParams.getInstance().leftMargin, 1.8, 1);
                write(WelcomeParams.getInstance().promoText, ++line, WelcomeParams.getInstance().leftMargin, 0.7, 0.4);
                int y = write("", ++line, 0, 1, 1);
                if (WelcomeParams.getInstance().barcode) {
                    Barcode barcode = null;
                    try {

                        barcode = BarcodeFactory.createCode128B(customer.getId().toString());
                    } catch (BarcodeException ex) {
                        QLog.l().logger().error("Ошибка создания штрихкода. " + ex);
                    }
                    barcode.setBarHeight(5);
                    barcode.setBarWidth(1);
                    barcode.setDrawingText(false);
                    barcode.setDrawingQuietSection(false);
                    try {
                        barcode.draw(g2, WelcomeParams.getInstance().leftMargin * 2, y - 7);
                    } catch (OutputException ex) {
                        QLog.l().logger().error("Ошибка вывода штрихкода. " + ex);
                    }
                }
                //Напечатаем текст внизу билета
                line = line + 2;
                name = WelcomeParams.getInstance().bottomText;
                while (name.length() != 0) {
                    String prn;
                    if (name.length() > WelcomeParams.getInstance().lineLenght) {
                        int fl = 0;
                        for (int i = WelcomeParams.getInstance().lineLenght; i > 0; i--) {

                            if (" ".equals(name.substring(i - 1, i))) {
                                fl = i;
                                break;
                            }
                        }
                        int pos = fl == 0 ? WelcomeParams.getInstance().lineLenght : fl;
                        prn = name.substring(0, pos);
                        name = name.substring(pos, name.length());
                    } else {
                        prn = name;
                        name = "";
                    }
                    write(prn, ++line, WelcomeParams.getInstance().leftMargin, 1, 1);
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
            QLog.l().logger().error("Ошибка печати: ", ex);
        }
    }

    public static void printTicketAdvance(QAdvanceCustomer advCustomer, String caption) {
        FWelcome.caption = caption;
        printTicketAdvance(advCustomer);
    }

    public static synchronized void printTicketAdvance(final QAdvanceCustomer advCustomer) {

        // поддержка расширяемости плагинами
        boolean flag = false;
        for (final IPrintTicket event : ServiceLoader.load(IPrintTicket.class)) {
            QLog.l().logger().info("Вызов SPI расширения. Описание: " + event.getDescription());
            try {
                flag = event.printTicketAdvance(advCustomer, FWelcome.caption);
            } catch (Throwable tr) {
                QLog.l().logger().error("Вызов SPI расширения завершился ошибкой. Описание: " + tr);
            }
            // раз напечатили и хорошь
            if (flag) {
                return;
            }
        }


        final Printable canvas = new Printable() {

            private int write(String text, int line, int x, double kx, double ky) {
                g2.scale(kx, ky);
                final int y = (int) Math.round((WelcomeParams.getInstance().topMargin + line * WelcomeParams.getInstance().lineHeigth) / ky);
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
                if (WelcomeParams.getInstance().logo) {
                    g2.drawImage(Uses.loadImage(this, WelcomeParams.getInstance().logoImg, "/ru/apertum/qsystem/client/forms/resources/logo_ticket_a.png"), WelcomeParams.getInstance().logoLeft, WelcomeParams.getInstance().logoTop, null);
                }
                g2.scale(WelcomeParams.getInstance().scaleHorizontal, WelcomeParams.getInstance().scaleVertical);
                //позиционируем начало координат
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                int line = 1;
                write(caption, line, WelcomeParams.getInstance().leftMargin, 1.5, 1.5);
                line++;
                write(getLocaleMessage("ticket.adv_purpose"), ++line, 20, 1, 1);

                final GregorianCalendar gc_time = new GregorianCalendar();
                gc_time.setTime(advCustomer.getAdvanceTime());
                int t = gc_time.get(GregorianCalendar.HOUR_OF_DAY);
                String t_m = ("" + gc_time.get(GregorianCalendar.MINUTE) + "0000").substring(0, 2);
                if (t == 0) {
                    t = 24;
                    gc_time.add(GregorianCalendar.HOUR_OF_DAY, -1);
                }
                write(Uses.format_dd_MMMM_yyyy.format(gc_time.getTime()), ++line + 1, WelcomeParams.getInstance().leftMargin, 2, 1);
                //write(FWelcome.getLocaleMessage("qbutton.take_adv_ticket_from") + " " + (t) + ":00 " + FWelcome.getLocaleMessage("qbutton.take_adv_ticket_to") + " " + (t + 1) + ":00", ++line + 1, WelcomeParams.getInstance().leftMargin, 2, 1);
                write(FWelcome.getLocaleMessage("qbutton.take_adv_ticket_come_to") + " " + (t) + ":" + t_m, ++line + 1, WelcomeParams.getInstance().leftMargin, 2, 1);


                line = line + 2;

                write(getLocaleMessage("ticket.service"), ++line, WelcomeParams.getInstance().leftMargin, 1.5, 1);
                String name = advCustomer.getService().getName();
                while (name.length() != 0) {
                    String prn;
                    if (name.length() > WelcomeParams.getInstance().lineLenght) {
                        int fl = 0;
                        for (int i = WelcomeParams.getInstance().lineLenght; i > 0; i--) {

                            if (" ".equals(name.substring(i - 1, i))) {
                                fl = i;
                                break;
                            }
                        }
                        int pos = fl == 0 ? WelcomeParams.getInstance().lineLenght : fl;
                        prn = name.substring(0, pos);
                        name = name.substring(pos, name.length());
                    } else {
                        prn = name;
                        name = "";
                    }
                    write(prn, ++line, WelcomeParams.getInstance().leftMargin, 1, 1);
                }

                write(getLocaleMessage("ticket.reg_time"), ++line, WelcomeParams.getInstance().leftMargin, 1.5, 1);

                write(Uses.format_for_label.format(new Date()), ++line, WelcomeParams.getInstance().leftMargin, 1, 1);

                // если клиент что-то ввел, то напечатаем это на его талоне
                if (advCustomer.getService().getInput_required()) {
                    write(advCustomer.getService().getInput_caption().replaceAll("<.*?>",""), ++line, WelcomeParams.getInstance().leftMargin, 1, 1);
                    write(advCustomer.getAuthorizationCustomer().getName(), ++line, WelcomeParams.getInstance().leftMargin, 1, 1); // тут кривовато передали введеные дпнные
                }

                // если в услуге есть что напечатать на талоне, то напечатаем это на его талоне
                if (advCustomer.getService().getTicketText() != null && !advCustomer.getService().getTicketText().isEmpty()) {
                    String tt = advCustomer.getService().getTicketText();
                    while (tt.length() != 0) {
                        String prn;
                        if (tt.length() > WelcomeParams.getInstance().lineLenght) {
                            int fl = 0;
                            for (int i = WelcomeParams.getInstance().lineLenght; i > 0; i--) {

                                if (" ".equals(tt.substring(i - 1, i))) {
                                    fl = i;
                                    break;
                                }
                            }
                            int pos = fl == 0 ? WelcomeParams.getInstance().lineLenght : fl;
                            prn = tt.substring(0, pos);
                            tt = tt.substring(pos, tt.length());
                        } else {
                            prn = tt;
                            tt = "";
                        }
                        write(prn, ++line, WelcomeParams.getInstance().leftMargin, 1, 1);
                    }
                }

                write(getLocaleMessage("ticket.adv_code"), ++line, WelcomeParams.getInstance().leftMargin, 1.3, 1);
                int y = write("", ++line, 0, 1, 1);
                if (WelcomeParams.getInstance().barcode) {
                    Barcode barcode = null;
                    try {

                        barcode = BarcodeFactory.createCode128B(advCustomer.getId().toString());
                    } catch (BarcodeException ex) {
                        QLog.l().logger().error("Ошибка создания штрихкода. " + ex);
                    }
                    barcode.setBarHeight(5);
                    barcode.setBarWidth(1);
                    barcode.setDrawingText(true);
                    barcode.setDrawingQuietSection(true);
                    try {
                        barcode.draw(g2, WelcomeParams.getInstance().leftMargin * 2, y - 7);
                    } catch (OutputException ex) {
                        QLog.l().logger().error("Ошибка вывода штрихкода. " + ex);
                    }
                    line = line + 3;
                } else {
                    write(advCustomer.getId().toString(), ++line, WelcomeParams.getInstance().leftMargin, 2.0, 1.7);
                }

                write(WelcomeParams.getInstance().promoText, ++line, WelcomeParams.getInstance().leftMargin, 0.7, 0.4);
                //Напечатаем текст внизу билета

                name = WelcomeParams.getInstance().bottomText;
                while (name.length() != 0) {
                    String prn;
                    if (name.length() > WelcomeParams.getInstance().lineLenght) {
                        int fl = 0;
                        for (int i = WelcomeParams.getInstance().lineLenght; i > 0; i--) {

                            if (" ".equals(name.substring(i - 1, i))) {
                                fl = i;
                                break;
                            }
                        }
                        int pos = fl == 0 ? WelcomeParams.getInstance().lineLenght : fl;
                        prn = name.substring(0, pos);
                        name = name.substring(pos, name.length());
                    } else {
                        prn = name;
                        name = "";
                    }
                    write(prn, ++line, WelcomeParams.getInstance().leftMargin, 1, 1);
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
            QLog.l().logger().error("Ошибка печати: ", ex);
        }
    }

    public static synchronized void printPreInfoText(final String preInfo) {

        Printable canvas = new Printable() {

            private int write(String text, int line, int x, double kx, double ky, int pageIndex) {

                if (line <= pageIndex * WelcomeParams.getInstance().pageLinesCount || line > (pageIndex + 1) * WelcomeParams.getInstance().pageLinesCount) {
                    return 0;
                }
                System.out.println(text);
                g2.scale(kx, ky);
                final int y = (int) Math.round((WelcomeParams.getInstance().topMargin + (line - 1) % (WelcomeParams.getInstance().pageLinesCount) * WelcomeParams.getInstance().lineHeigth) / ky);
                g2.drawString(text, x, y);
                g2.scale(1 / kx, 1 / ky);
                return y;
            }
            Graphics2D g2;

            private LinkedList<String> splitText(String text) {
                final LinkedList<String> strings = new LinkedList<>();
                while (text.length() != 0) {
                    String prn;
                    if (text.length() > WelcomeParams.getInstance().lineLenght) {
                        int fl = 0;
                        for (int i = WelcomeParams.getInstance().lineLenght; i > 0; i--) {

                            if (" ".equals(text.substring(i - 1, i))) {
                                fl = i;
                                break;
                            }
                        }
                        int pos = fl == 0 ? WelcomeParams.getInstance().lineLenght : fl;
                        prn = text.substring(0, pos);
                        text = text.substring(pos, text.length());
                    } else {
                        prn = text;
                        text = "";
                    }
                    strings.add(prn);
                }
                return strings;
            }

            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

                g2 = (Graphics2D) graphics;
                if (pageIndex == 0 && WelcomeParams.getInstance().logo) {
                    g2.drawImage(Uses.loadImage(this, WelcomeParams.getInstance().logoImg, "/ru/apertum/qsystem/client/forms/resources/logo_ticket_a.png"), WelcomeParams.getInstance().logoLeft, WelcomeParams.getInstance().logoTop, null);
                }
                g2.scale(WelcomeParams.getInstance().scaleHorizontal, WelcomeParams.getInstance().scaleVertical);
                //позиционируем начало координат
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                int line = 1;


                write(caption, ++line, WelcomeParams.getInstance().leftMargin, 1.5, 1.5, pageIndex);
                ++line;
                // напечатаем текст подсказки
                final LinkedList<String> strings = new LinkedList<>();
                final Scanner sc = new Scanner(preInfo.replace("<brk>", "\n"));
                while (sc.hasNextLine()) {
                    final String w = sc.nextLine();
                    strings.addAll(splitText(w));
                    //line = writeText(w, line, WelcomeParams.getInstance().leftMargin, 1, 1, pageIndex);
                }
                for (String string : strings) {
                    write(string, ++line, WelcomeParams.getInstance().leftMargin, 1, 1, pageIndex);
                }

                write(WelcomeParams.getInstance().promoText, ++line, WelcomeParams.getInstance().leftMargin, 0.7, 0.4, pageIndex);
                //Напечатаем текст внизу билета

                //line = writeText(WelcomeParams.getInstance().bottomText, line, WelcomeParams.getInstance().leftMargin, 1, 1, pageIndex);
                strings.clear();
                strings.addAll(splitText(WelcomeParams.getInstance().bottomText));
                for (String string : strings) {
                    write(string, ++line, WelcomeParams.getInstance().leftMargin, 1, 1, pageIndex);
                }


                write(".", line + 2, 0, 1, 1, pageIndex);

                if ((pageIndex + 0) * WelcomeParams.getInstance().pageLinesCount > line) {
                    return Printable.NO_SUCH_PAGE;
                } else {
                    return Printable.PAGE_EXISTS;
                }
            }
        };
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(canvas);
        PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
        attr.add(MediaSizeName.EXECUTIVE);
        try {
            job.print(attr);
        } catch (PrinterException ex) {
            QLog.l().logger().error("Ошибка печати: ", ex);
        }
    }

    public void setVisibleButtons(boolean visible) {
        buttonBack.setVisible(visible && current != root);
        buttonToBegin.setVisible(visible && current != root);

        buttonStandAdvance.setVisible(WelcomeParams.getInstance().advance && visible);
        buttonAdvance.setVisible(WelcomeParams.getInstance().advance && visible);

        buttonInfo.setVisible(WelcomeParams.getInstance().info && visible);
        buttonResponse.setVisible(WelcomeParams.getInstance().response && visible);
        
        int cols = 3;
        int rows = 5;

        // посмотрим сколько реальных кнопок нужно отобразить
        // тут есть невидимые услуги и услуги не с того киоска
        int childCount = 0;
        for (QService service : current.getChildren()) {
            if (service.getStatus() != -1 && (WelcomeParams.getInstance().point == 0 || (service.getPoint() == 0 || service.getPoint() == WelcomeParams.getInstance().point))) {
                childCount++;
            }
        }

        if (childCount < 4) {
            cols = 1;
            rows = 3;
        }
        if (childCount > 3 && childCount < 11) {
            cols = 2;
            rows = Math.round(new Float(childCount) / 2);
        }
        if (childCount > 10) {
            cols = 3;
            rows = Math.round(new Float(0.3) + (float) childCount / 3);
        }

        // поправка на то что если кнопок на уровне много и они уже в три колонки, то задействуем ограничение по линиям, а то расползутся
        if (visible && rows > WelcomeParams.getInstance().linesButtonCount && cols >= 3) {
            rows = WelcomeParams.getInstance().linesButtonCount;
            panelForPaging.setVisible(true);
        } else {
            panelForPaging.setVisible(false);
        }
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
        QLog.l().logger().info("Пункт регистрации заблокирован. Состояние \"" + stateWindow + "\"");
    }

    /**
     * Разблокировать пункт постановки в очередь.
     */
    public void unlock() {
        setStateWindow(UNLOCK);
        setVisibleButtons(true);
        QLog.l().logger().info("Пункт регистрации готов к работе. Состояние \"" + stateWindow + "\"");
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
            QLog.l().logger().error("Проблемы с таймером. ", ex);
        }
        QLog.l().logger().info("Пункт регистрации выключен. Состояние \"" + stateWindow + "\"");
    }

    /**
     * Инициализация заново пункта постановки в очередь.
     */
    public void reinit() {
        final RpcGetAllServices.ServicesForWelcome servs = NetCommander.getServiсes(netProperty);
        final QService reroot = servs.getRoot();
        FWelcome.root = reroot;
        FWelcome.current = reroot;
        FWelcome.response = null;
        FWelcome.infoTree = null;
        FWelcome.startTime = servs.getStartTime();
        FWelcome.finishTime = servs.getFinishTime();
        loadRootParam();
        QLog.l().logger().info("Пункт регистрации реинициализирован. Состояние \"" + stateWindow + "\"");
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

        panelBackground = new QPanel(WelcomeParams.getInstance().backgroundImg);
        panelCaption = new QPanel(WelcomeParams.getInstance().topImg);
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
        panelForPaging = new javax.swing.JPanel();
        buttonBackPage = new javax.swing.JButton();
        buttonForwardPage = new javax.swing.JButton();
        labelBackPage = new javax.swing.JLabel();
        labelForwardPage = new javax.swing.JLabel();

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
        panelCaption.setPreferredSize(new java.awt.Dimension(1008, 150));

        labelCaption.setFont(resourceMap.getFont("labelCaption.font")); // NOI18N
        labelCaption.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelCaption.setText(resourceMap.getString("labelCaption.text")); // NOI18N
        labelCaption.setName("labelCaption"); // NOI18N

        javax.swing.GroupLayout panelCaptionLayout = new javax.swing.GroupLayout(panelCaption);
        panelCaption.setLayout(panelCaptionLayout);
        panelCaptionLayout.setHorizontalGroup(
            panelCaptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelCaption, javax.swing.GroupLayout.DEFAULT_SIZE, 1058, Short.MAX_VALUE)
        );
        panelCaptionLayout.setVerticalGroup(
            panelCaptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelCaption, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
        );

        panelButtons.setBorder(new javax.swing.border.MatteBorder(null));
        panelButtons.setName("panelButtons"); // NOI18N
        panelButtons.setOpaque(false);
        panelButtons.setLayout(new java.awt.GridLayout(1, 0, 10, 0));

        buttonAdvance.setFont(resourceMap.getFont("buttonAdvance.font")); // NOI18N
        buttonAdvance.setText(resourceMap.getString("buttonAdvance.text")); // NOI18N
        buttonAdvance.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED))));
        buttonAdvance.setName("buttonAdvance"); // NOI18N
        buttonAdvance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAdvanceActionPerformed(evt);
            }
        });
        panelButtons.add(buttonAdvance);

        buttonStandAdvance.setFont(resourceMap.getFont("buttonStandAdvance.font")); // NOI18N
        buttonStandAdvance.setText(resourceMap.getString("buttonStandAdvance.text")); // NOI18N
        buttonStandAdvance.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED))));
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
        buttonToBegin.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED))));
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
        buttonBack.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED))));
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

        panelMain.setBorder(new javax.swing.border.MatteBorder(null));
        panelMain.setFont(resourceMap.getFont("panelMain.font")); // NOI18N
        panelMain.setName("panelMain"); // NOI18N
        panelMain.setOpaque(false);

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 953, Short.MAX_VALUE)
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 355, Short.MAX_VALUE)
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
                .addComponent(labelLock, javax.swing.GroupLayout.DEFAULT_SIZE, 953, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelLockLayout.setVerticalGroup(
            panelLockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLockLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelLock, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
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
                .addGroup(panelCentreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelCentreLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(panelLock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(panelCentreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(buttonInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonResponse, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        panelCentreLayout.setVerticalGroup(
            panelCentreLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCentreLayout.createSequentialGroup()
                .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(panelLock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(panelCentreLayout.createSequentialGroup()
                .addComponent(buttonInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonResponse, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
        );

        panelForPaging.setBorder(new javax.swing.border.MatteBorder(null));
        panelForPaging.setName("panelForPaging"); // NOI18N
        panelForPaging.setOpaque(false);

        buttonBackPage.setFont(resourceMap.getFont("buttonForwardPage.font")); // NOI18N
        buttonBackPage.setIcon(resourceMap.getIcon("buttonBackPage.icon")); // NOI18N
        buttonBackPage.setText(resourceMap.getString("buttonBackPage.text")); // NOI18N
        buttonBackPage.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED))));
        buttonBackPage.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        buttonBackPage.setName("buttonBackPage"); // NOI18N
        buttonBackPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonBackPageActionPerformed(evt);
            }
        });

        buttonForwardPage.setFont(resourceMap.getFont("buttonForwardPage.font")); // NOI18N
        buttonForwardPage.setIcon(resourceMap.getIcon("buttonForwardPage.icon")); // NOI18N
        buttonForwardPage.setText(resourceMap.getString("buttonForwardPage.text")); // NOI18N
        buttonForwardPage.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED))));
        buttonForwardPage.setName("buttonForwardPage"); // NOI18N
        buttonForwardPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonForwardPageActionPerformed(evt);
            }
        });

        labelBackPage.setFont(resourceMap.getFont("labelBackPage.font")); // NOI18N
        labelBackPage.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelBackPage.setText(resourceMap.getString("labelBackPage.text")); // NOI18N
        labelBackPage.setName("labelBackPage"); // NOI18N

        labelForwardPage.setFont(resourceMap.getFont("labelForwardPage.font")); // NOI18N
        labelForwardPage.setText(resourceMap.getString("labelForwardPage.text")); // NOI18N
        labelForwardPage.setName("labelForwardPage"); // NOI18N

        javax.swing.GroupLayout panelForPagingLayout = new javax.swing.GroupLayout(panelForPaging);
        panelForPaging.setLayout(panelForPagingLayout);
        panelForPagingLayout.setHorizontalGroup(
            panelForPagingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelForPagingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelBackPage, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(buttonBackPage)
                .addGap(18, 18, 18)
                .addComponent(buttonForwardPage)
                .addGap(18, 18, 18)
                .addComponent(labelForwardPage, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelForPagingLayout.setVerticalGroup(
            panelForPagingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelForPagingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelForPagingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonBackPage, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                    .addComponent(labelForwardPage, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                    .addComponent(labelBackPage, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                    .addComponent(buttonForwardPage, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout panelBackgroundLayout = new javax.swing.GroupLayout(panelBackground);
        panelBackground.setLayout(panelBackgroundLayout);
        panelBackgroundLayout.setHorizontalGroup(
            panelBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelCaption, javax.swing.GroupLayout.DEFAULT_SIZE, 1060, Short.MAX_VALUE)
            .addGroup(panelBackgroundLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, 1040, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(panelForPaging, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelCentre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelBackgroundLayout.setVerticalGroup(
            panelBackgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBackgroundLayout.createSequentialGroup()
                .addComponent(panelCaption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelCentre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelForPaging, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
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
            labelCaption.setText("<html><p align=center><span style='font-size:55.0;color:#DC143C'>" + getLocaleMessage("messages.select_adv_servece1")
                    + "</span><br><span style='font-size:45.0;color:#DC143C'><i>" + getLocaleMessage("messages.select_adv_servece2") + "</i>");
            buttonAdvance.setText("<html><p align=center>" + getLocaleMessage("lable.reg_calcel"));
            //возврат в начальное состояние из диалога предварительной записи.
            if (clockBack.isActive()) {
                clockBack.stop();
            }
            clockBack.start();
        } else {
            if (clockBack.isActive()) {
                clockBack.stop();
            }
            labelCaption.setText(root.getButtonText());
            buttonAdvance.setText("<html><p align=center>" + getLocaleMessage("lable.adv_reg"));
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
     * @return
     */
    public ATalkingClock showDelayFormPrint(String text, String imagePath) {
        setVisibleButtons(false);
        ATalkingClock clock = new ATalkingClock(WelcomeParams.getInstance().delayPrint, 1) {

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
    final RpcStandInService res = FStandAdvance.showAdvanceStandDialog(this, true, FWelcome.netProperty, true, WelcomeParams.getInstance().delayBack * 2);

    if (res != null) {

        if (res.getMethod() == null) {// костыль. тут приедет текст запрета если нельзя встать в очередь

            showDelayFormPrint("<HTML><b><p align=center><span style='font-size:50.0pt;color:green'>" + getLocaleMessage("ticket.get_caption") + "<br></span>"
                    + "<span style='font-size:60.0pt;color:blue'>" + getLocaleMessage("ticket.get_caption_number") + "<br></span>"
                    + "<span style='font-size:100.0pt;color:blue'>" + res.getResult().getPrefix() + res.getResult().getNumber() + "</span></p>",
                    "/ru/apertum/qsystem/client/forms/resources/getTicket.png");

            QLog.l().logger().info("Печать этикетки.");

            new Thread(new Runnable() {

                @Override
                public void run() {
                    FWelcome.printTicket(res.getResult());
                }
            }).start();
        } else {
            showDelayFormPrint("<HTML><b><p align=center><span style='font-size:60.0pt;color:red'>" + res.getMethod(),
                    "/ru/apertum/qsystem/client/forms/resources/noActive.png");
        }
    }

}//GEN-LAST:event_buttonStandAdvanceActionPerformed

private void buttonResponseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResponseActionPerformed
    final Long res = FResponseDialog.showResponseDialog(this, getResponse(), true, true, WelcomeParams.getInstance().delayBack * 2);
    if (res != null) {
        NetCommander.setResponseAnswer(netProperty, res);
    }
}//GEN-LAST:event_buttonResponseActionPerformed

private void buttonInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonInfoActionPerformed
    FInfoDialog.showResponseDialog(this, getInfoTree(), true, true, WelcomeParams.getInstance().delayBack * 3);
}//GEN-LAST:event_buttonInfoActionPerformed

    private void buttonBackPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonBackPageActionPerformed
        if (pageNumber > 0) {
            pageNumber--;
            showButtons(current, panelMain);
            buttonBackPage.setEnabled(pageNumber > 0);
        }

    }//GEN-LAST:event_buttonBackPageActionPerformed

    private void buttonForwardPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonForwardPageActionPerformed
        pageNumber++;
        showButtons(current, panelMain);
        buttonBackPage.setEnabled(pageNumber > 0);
    }//GEN-LAST:event_buttonForwardPageActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAdvance;
    private javax.swing.JButton buttonBack;
    private javax.swing.JButton buttonBackPage;
    private javax.swing.JButton buttonForwardPage;
    private javax.swing.JButton buttonInfo;
    private javax.swing.JButton buttonResponse;
    private javax.swing.JButton buttonStandAdvance;
    private javax.swing.JButton buttonToBegin;
    private javax.swing.JLabel labelBackPage;
    private javax.swing.JLabel labelCaption;
    private javax.swing.JLabel labelForwardPage;
    private javax.swing.JLabel labelLock;
    private javax.swing.JPanel panelBackground;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel panelCaption;
    private javax.swing.JPanel panelCentre;
    private javax.swing.JPanel panelForPaging;
    private javax.swing.JPanel panelLock;
    private javax.swing.JPanel panelMain;
    // End of variables declaration//GEN-END:variables
}
