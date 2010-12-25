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

import java.awt.AWTException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.dom4j.DocumentException;
import org.jdesktop.application.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.model.INetProperty;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import ru.apertum.qsystem.QSystem;
import ru.apertum.qsystem.client.help.Helper;
import ru.apertum.qsystem.common.model.NetCommander;
import ru.apertum.qsystem.client.model.QPanel;
import ru.apertum.qsystem.client.model.QTray;
import ru.apertum.qsystem.client.model.QTray.MessageType;
import ru.apertum.qsystem.common.AUDPServer;
import ru.apertum.qsystem.common.model.QCustomer;

/**
 * Created on 11 Сентябрь 2008 г., 16:57
 * @author Evgeniy Egorov
 */
public class FClient extends javax.swing.JFrame {

    /**
     * Информация для взаимодействия по сети.
     * Формируется по данным из командной строки.
     */
    private final INetProperty netProperty;
    /**
     * Системный трей.
     */
    private final QTray tray;
    /**
     * Это удобный списочек всех услуг, обслуживаемых юзером
     */
    private final ArrayList<String> myServices = new ArrayList<String>();
    /**
     * Кастомер, с которым работает юзер.
     */
    private QCustomer customer = null;

    public void setCustomer(QCustomer customer) {
        Uses.log.logger.trace("Установливаем кастомера работающему клиенту и выводем его.");
        this.customer = customer;
        // выведем на экран некую инфу о приглашенном кастомере
        final String textCust = customer.getPrefix() + customer.getNumber();
        labelNextNumber.setText(textCust);
        // Выведем номер вызванного.
        printCustomerNumber(textCust, 0);

        final String priority;
        switch (customer.getPriority().get()) {
            case 0: {
                priority = getLocaleMessage("messages.priority.low");
                break;
            }
            case 1: {
                priority = getLocaleMessage("messages.priority.standart");
                break;
            }
            case 2: {
                priority = getLocaleMessage("messages.priority.hi");
                break;
            }
            case 3: {
                priority = getLocaleMessage("messages.priority.vip");
                break;
            }
            default: {
                priority = getLocaleMessage("messages.priority.strange");
            }
        }
        String s = customer.toXML().attributeValue(Uses.TAG_PROP_INPUT_CAPTION);
        if (s == null) {
            s = "";
        } else {
            s = "<br>" + s + "<br>" + customer.getInput_data();
        }
        labelNextCustomerInfo.setText("<html><b><span style='color:blue'> " + getLocaleMessage("messages.service") + ": " + customer.getServiceName() + "<br>" + getLocaleMessage("messages.priority") + ": " + priority + s + "</span></b>");
        // прикроем кнопки, которые недоступны на этом этапе работы с кастомером.
        // тут в зависимости от состояния кастомера открываем разные наборы кнопок
        switch (customer.getState()) {
            case Uses.STATE_INVITED: {
                setBlinkBoard(true);
                setKeyRegim(KEYS_INVITED);
                break;
            }
            case Uses.STATE_WORK: {
                setBlinkBoard(false);
                setKeyRegim(KEYS_STARTED);
                break;
            }
            default: {
                throw new Uses.ClientException("Не известное состояние клиента \"" + customer.getState() + "\" для данного случая.");
            }
        }
    }

    /**
     * Заставляем мигать все табло.
     * @param blinked мигаем или нет.
     */
    private void setBlinkBoard(boolean blinked) {
        if (indicatorBoard != null) {
            FIndicatorBoard.printRecord(0, customer.getPrefix() + customer.getNumber(), blinked ? 0 : -1);
        }
    }

    public QCustomer getCustomer() {
        return customer;
    }

    /**
     * Обозначим результат работы с клиентом если требуется
     */
    private Long setResult() {
        Long rs = new Long(-1);
        if (customer.toXML().attributeValue(Uses.TAG_PROP_RESULT_REQUIRED) != null
                && ("1".equals(customer.toXML().attributeValue(Uses.TAG_PROP_RESULT_REQUIRED))
                || "true".equals(customer.toXML().attributeValue(Uses.TAG_PROP_RESULT_REQUIRED).toLowerCase()))) {
            getResults();
            Object res = null;
            res = JOptionPane.showInputDialog(this, "Выберите результат", "Результат работы с клиентом", JOptionPane.QUESTION_MESSAGE, null, getResults(), null);
            rs = res == null ? null : results.get((String) res);
        }
        return rs;
    }
    private final LinkedHashMap<String, Long> results = new LinkedHashMap<String, Long>();

    private Object[] getResults() {
        if (results.isEmpty()) {
            for (Object o : NetCommander.getResultsList(netProperty).elements()) {
                final Element el = (Element) o;
                results.put(el.attributeValue(Uses.TAG_PROP_NAME), Long.parseLong(el.attributeValue(Uses.TAG_ID)));
            }
        }
        return results.keySet().toArray();
    }

    /**
     * UDP Сервер. Обнаруживает изменение состояния очередей.
     */
    protected final class UDPServer extends AUDPServer {

        public UDPServer(int port) {
            super(port);
        }

        @Override
        synchronized protected void getData(String data, InetAddress clientAddress, int clientPort) {
            //Определяем, по нашей ли услуге пришел кастомер
            //Если кастомер встал в очередь, обрабатываемую этим юзером, то апдейтим состояние очередей.
            if (myServices.contains(data) || userName.equals(data)) {
                //Получаем состояние очередей для юзера
                final Element usersPlan = NetCommander.getSelfServices(netProperty, user.attributeValue(Uses.TAG_NAME));
                setSituation(usersPlan);
            }
            if (Uses.HOW_DO_YOU_DO.equals(data)) {
                //Отправим по TCP/IP
                NetCommander.setLive(netProperty, user.attributeValue(Uses.TAG_NAME));
            }
            if (data.startsWith("message#") && (data.startsWith("message#ALL##") || isMyMessage(data))) {
                final String mess = data.substring(data.indexOf("##") + 2);
                tray.showMessageTray("Информационное сообщение", mess, MessageType.INFO);

                labelMessage.setText(labelMessage.getText() + "<b><span style='color:black'>" + Uses.format_HH_mm.format(new Date()) + " Сообщение:</span></b><br><span style='color:blue'>" + mess.replaceAll("\n", "<br>") + "</span><br>");
            }
        }

        private boolean isMyMessage(String txt) {
            final String adr = txt.substring(0, txt.indexOf("##"));
            if (adr.indexOf("@" + userName + "@") != -1) {
                return true;
            }
            for (String serv : myServices) {
                if (adr.indexOf("@" + serv + "@") != -1) {
                    return true;
                }
            }
            return false;
        }
    };
    private final UDPServer udpServer;
    /**
     * Описание того, кто залогинелся.
     */
    private final Element user;
    private final String userName;
    /**
     * Описание того, сколько народу стоит в очередях к этому юзеру, ну и прочее(потом)mess
     * Не использовать на прямую.
     * @see setSituation(Element plan)
     */
    private Element userPlan;
    private static ResourceMap localeMap = null;

    private static String getLocaleMessage(String key) {
        if (localeMap == null) {
            localeMap = Application.getInstance(QSystem.class).getContext().getResourceMap(FClient.class);
        }
        return localeMap.getString(key);
    }

    /**
     * Creates new form FClient
     */
    public FClient(Element user, Element usersPlan, INetProperty netProperty) throws AWTException {
        this.user = user;
        this.netProperty = netProperty;
        initComponents();
        // отрехтуем дизайн формы.
        jPanel1.setVisible(false);
        jPanel4.setVisible(false);
        //menuBar.setVisible(false);
        labelNextNumber.setText(getLocaleMessage("messages.noCall"));
        printCustomerNumber("", -1);
        /*
        // Фича. По нажатию Escape закрываем форму
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(escapeKey, new javax.swing.Action(){});
         */
        // инициализим trayIcon, т.к. setSituation() требует работу с tray
        tray = QTray.getInstance(this, "/ru/apertum/qsystem/client/forms/resources/client.png", getLocaleMessage("messages.tray.hint"));
        tray.addItem(getLocaleMessage("messages.tray.showClient"), new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(true);
                setState(JFrame.NORMAL);
            }
        });
        tray.addItem("-", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        tray.addItem(getLocaleMessage("messages.tray.close"), new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                System.exit(0);
            }
        });
        userName = user.attributeValue(Uses.TAG_NAME);
        labelUser.setText(userName + " - " + user.attributeValue(Uses.TAG_USER_IDENTIFIER));
        setSituation(usersPlan);

        // стартуем UDP сервер для обнаружения изменения состояния очередей
        udpServer = new UDPServer(netProperty.getClientPort());
        //привязка помощи к форме.
        final Helper helper = Helper.getHelp("ru/apertum/qsystem/client/help/client.hs");
        helper.setHelpListener(menuItemHelp);
        helper.enableHelpKey(panelDown, "client");
        Uses.closeSplash();
    }

    /**
     * Создадим форму, спозиционируем, сконфигурируем и покажем
     * @param configFilePath файл конфигурации табло, приезжает из Spring
     */
    private static void initIndicatorBoard(String cfgFile) throws DocumentException {
        File f = new File(cfgFile);
        if (indicatorBoard == null && f.exists()) {

            indicatorBoard = FIndicatorBoard.getIndicatorBoard(new SAXReader(false).read(cfgFile).getRootElement(), false);
            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (indicatorBoard != null) {
                        indicatorBoard.setVisible(true);
                    }
                }
            });
        }
    }

    /**
     * Определяет какова ситуация в очереди к пользователю.
     * @param plan - ситуация в XML
     * @return html - описание очередей.
     */
    protected void setSituation(Element plan) {
        Uses.log.logger.trace("Обновляем видимую ситуацию.");
        /**
         * На первую закладку.
         */
        String temp = "";
        /**
         * На вторую закладку
         */
        String tempAll = "";
        String temp1 = "";
        String color = "blue";
        int inCount = 0;
        myServices.clear();// очистим удобный списочек

        // построим новую html с описанием состояния очередей
        for (Object ob : plan.elements(Uses.TAG_SERVICE)) {
            final Element el = (Element) ob;
            final int count = Integer.parseInt(el.attributeValue(Uses.TAG_DESCRIPTION));
            final String serviceName = el.attributeValue(Uses.TAG_NAME);
            myServices.add(serviceName);//заполним удобный списочек

            final String people = " " + getLocaleMessage("messages.people");// множественное
            if (count != 0) {
                temp = temp + "<span style='color:" + (0 == count ? "green" : "red") + "'> - " + serviceName + ": " + count + people
                        + ((((count % 10) >= 2) && ((count % 10) <= 4)) ? "a" : "") + "</span><br>";
                temp1 = temp1 + " - " + serviceName + ": " + count + people
                        + ((((count % 10) >= 2) && ((count % 10) <= 4)) ? "a" : "") + "<br>";
            }
            tempAll = tempAll + "<span style='color:" + (0 == count ? "green" : "red") + "'> - " + serviceName + ": " + count + people
                    + ((((count % 10) >= 2) && ((count % 10) <= 4)) ? "a" : "") + "</span><br>";
            if (count != 0) {
                color = "purple";
            }
            inCount = inCount + count;
        }
        final String allClients = getLocaleMessage("messages.allClients") + ": ";
        labelResume.setText("<html><span style='color:" + color + "'>" + allClients + inCount + "</span>");
        temp1 = temp1 + allClients + inCount;

        // определим количество уже стоящих кастомеров в наших очередях
        int count = 0;
        if (userPlan != null) {
            for (Object ob : userPlan.elements(Uses.TAG_SERVICE)) {
                final Element el = (Element) ob;
                count = count + Integer.parseInt(el.attributeValue(Uses.TAG_DESCRIPTION));
            }
        }
        // покажим сообщение в трее если очередь была пуста и кто-то приперся
        if (count == 0 && inCount != 0) {
            tray.showMessageTray(getLocaleMessage("messages.tray.messCaption"), temp1.replaceAll("<br>", "\n"), MessageType.INFO);
        }
        // посмотрим, не приехал ли кастомер, который уже вызванный
        // если приехал, то его надо учесть
        final List<Element> l = plan.elements(Uses.TAG_CUSTOMER);
        if (l.size() == 1) {
            Uses.log.logger.trace("От сервера приехал кастомер, который обрабатывается юзером.");
            setCustomer(new QCustomer(l.get(0)));
        } else {
            if (inCount == 0) {
                setKeyRegim(KEYS_OFF);/* нет клиентов, нечеого вызывать*/
            } else {
                setKeyRegim(KEYS_MAY_INVITE); /*в очереди кто-то есть, можно вызвать*/
            }
            labelNextNumber.setText(getLocaleMessage("messages.noCall"));
            printCustomerNumber("", -1);
            labelNextCustomerInfo.setText("");
        }
        //теперь описание очередей новое
        userPlan = plan;
        labelSituation.setText("<html>" + temp);
        labelSituationAll.setText("<html>" + tempAll);

    }
    /**
     * Возможный состояния кнопок
     * 1 - доступна кнопка, 0 - не доступна
     */
    private static final String KEYS_OFF = "00000";
    private static final String KEYS_ALL = "11111";
    private static final String KEYS_MAY_INVITE = "10000";
    private static final String KEYS_INVITED = "11100";
    private static final String KEYS_STARTED = "00011";

    /**
     * Механизм включения/отключения кнопок
     */
    public void setKeyRegim(String regim) {
        Uses.log.logger.trace("Конфигурация кнопок \"" + regim + "\".");
        buttonInvite.setEnabled('1' == regim.charAt(0));
        buttonKill.setEnabled('1' == regim.charAt(1));
        buttonStart.setEnabled('1' == regim.charAt(2));
        buttonRedirect.setEnabled('1' == regim.charAt(3));
        buttonFinish.setEnabled('1' == regim.charAt(4));

        menuItemInvite.setEnabled('1' == regim.charAt(0));
        menuItemKill.setEnabled('1' == regim.charAt(1));
        menuItemStart.setEnabled('1' == regim.charAt(2));
        menuItemRedirect.setEnabled('1' == regim.charAt(3));
        menuItemFinish.setEnabled('1' == regim.charAt(4));
    }

    /**
     * Убирем иконку из трея по завершению программы,
     * остановим UDP сервер.
     * @throws java.lang.Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        // остановим UDP сервер 
        udpServer.stop();
        // и чЁ там в предке ...
        super.finalize();
    }

    /*******************************************************************************************************************
    /*******************************************************************************************************************
    /************************************      Обработчики кнопок      ************************************************/
    private long go() {
        Uses.log.logger.trace("Начало действия");
        return System.currentTimeMillis();
    }

    private void end(long start) {
        Uses.log.logger.trace("Действие завершено. Затрачено времени: " + new Double(System.currentTimeMillis() - start) / 1000 + " сек.\n");
    }

    /**
     * Действие по нажатию кнопки "Вызов"
     * @param evt
     */
    @Action
    public void inviteNextCustomer(ActionEvent evt) {
        final long start = go();
        // Вызываем кастомера
        NetCommander.inviteNextCustomer(netProperty, user.attributeValue(Uses.TAG_NAME));
        // Показываем обстановку
        setSituation(NetCommander.getSelfServices(netProperty, user.attributeValue(Uses.TAG_NAME)));
        end(start);
    }

    /**
     * Действие по нажатию кнопки "Отклонить"
     * @param evt
     */
    @Action
    public void killCustomer(ActionEvent evt) {
        final long start = go();
        // Уточним намерения
        if (JOptionPane.showConfirmDialog(this,
                getLocaleMessage("messages.kill.ask"),
                getLocaleMessage("messages.kill.caption"),
                JOptionPane.YES_NO_OPTION) == 1) {
            return;
        }
        // Убиваем пользователя
        NetCommander.killNextCustomer(netProperty, user.attributeValue(Uses.TAG_NAME));
        // Получаем новую обстановку
        //Получаем состояние очередей для юзера
        setSituation(NetCommander.getSelfServices(netProperty, user.attributeValue(Uses.TAG_NAME)));
        end(start);
    }

    /**
     * Действие по нажатию кнопки "Начать прием"
     * @param evt
     */
    @Action
    public void getStartCustomer(ActionEvent evt) {
        final long start = go();
        // Переводим кастомера в разряд обрабатываемых
        NetCommander.getStartCustomer(netProperty, user.attributeValue(Uses.TAG_NAME));
        // Получаем новую обстановку
        //Получаем состояние очередей для юзера
        setSituation(NetCommander.getSelfServices(netProperty, user.attributeValue(Uses.TAG_NAME)));
        end(start);
    }

    /**
     * Действие по нажатию кнопки "Завершить прием"
     * @param evt
     */
    @Action
    public void getStopCustomer(ActionEvent evt) {
        //Обозначим результат если требуется
        final long start = go();
        // Переводим кастомера в разряд обрабатанных
        // это должно выкинуть кастомера нафиг, но как обработанного
        final Long res = setResult();
        if (res == null) {
            return;
        }
        NetCommander.getFinishCustomer(netProperty, user.attributeValue(Uses.TAG_NAME), res);
        // Получаем новую обстановку
        //Получаем состояние очередей для юзера
        setSituation(NetCommander.getSelfServices(netProperty, user.attributeValue(Uses.TAG_NAME)));
        end(start);
    }
    protected FRedirect servicesForm = null;

    /**
     * Действие по нажатию кнопки "Перенаправить"
     * @param evt
     */
    @Action
    public void redirectCustomer(ActionEvent evt) {
        final long start = go();
        // Переводим кастомера в другую услугу
        // это должно выкинуть кастомера в другую очередь с приоритетом "переведенный"
        //Диалог выбора очереди для редиректа
        final FRedirect dlg = FRedirect.getService(netProperty, this);
        if (dlg == null) {
            //Если не выбрали, то выходим
            return;
        }

        NetCommander.redirectCustomer(netProperty, user.attributeValue(Uses.TAG_NAME), dlg.getServiceName(), dlg.getRequestBack());
        // Получаем новую обстановку
        //Получаем состояние очередей для юзера
        setSituation(NetCommander.getSelfServices(netProperty, user.attributeValue(Uses.TAG_NAME)));
        end(start);
    }

    //*******************************    Конец обработчиков кнопок    ***************************************************
    //*******************************************************************************************************************
    @Action
    public void showAboutBox() {
        FAbout.showAbout(this, true);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popupMenuTray = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem5 = new javax.swing.JMenuItem();
        panelDown = new QPanel("/ru/apertum/qsystem/client/forms/resources/fon_client.jpg");
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        labelUser = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        labelResume = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        labelSituation = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        labelSituationAll = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        labelMessage = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        buttonInvite = new javax.swing.JButton();
        buttonKill = new javax.swing.JButton();
        buttonStart = new javax.swing.JButton();
        buttonFinish = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        labelNextNumber = new javax.swing.JLabel();
        buttonRedirect = new javax.swing.JButton();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        labelNextCustomerInfo = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        labelStatus = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        menuItemRefresh = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        menuItemInvite = new javax.swing.JMenuItem();
        menuItemKill = new javax.swing.JMenuItem();
        menuItemStart = new javax.swing.JMenuItem();
        menuItemRedirect = new javax.swing.JMenuItem();
        menuItemFinish = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        menuItemHelp = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        popupMenuTray.setName("popupMenuTray"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getActionMap(FClient.class, this);
        jMenuItem1.setAction(actionMap.get("getNextCustomer")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        popupMenuTray.add(jMenuItem1);

        jMenuItem2.setAction(actionMap.get("killCustomer")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        popupMenuTray.add(jMenuItem2);

        jMenuItem3.setAction(actionMap.get("getStartCustomer")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        popupMenuTray.add(jMenuItem3);

        jMenuItem4.setAction(actionMap.get("getStopCustomer")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        popupMenuTray.add(jMenuItem4);

        jSeparator1.setName("jSeparator1"); // NOI18N
        popupMenuTray.add(jSeparator1);

        jMenuItem5.setAction(actionMap.get("quit")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        popupMenuTray.add(jMenuItem5);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getResourceMap(FClient.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setIconImage(getIconImage());
        setIconImages(getIconImages());
        setMinimumSize(new java.awt.Dimension(400, 400));
        setName("Form"); // NOI18N

        panelDown.setBorder(new javax.swing.border.MatteBorder(null));
        panelDown.setName("panelDown"); // NOI18N

        jPanel4.setBorder(new javax.swing.border.MatteBorder(null));
        jPanel4.setName("jPanel4"); // NOI18N

        jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(163, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setOpaque(false);

        jLabel3.setIcon(resourceMap.getIcon("jLabel3.icon")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        labelUser.setForeground(resourceMap.getColor("labelUser.foreground")); // NOI18N
        labelUser.setText(resourceMap.getString("labelUser.text")); // NOI18N
        labelUser.setName("labelUser"); // NOI18N

        jLabel4.setIcon(resourceMap.getIcon("jLabel4.icon")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        labelResume.setText(resourceMap.getString("labelResume.text")); // NOI18N
        labelResume.setName("labelResume"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setOpaque(false);

        jScrollPane2.setName("jScrollPane2"); // NOI18N
        jScrollPane2.setOpaque(false);

        labelSituation.setText(resourceMap.getString("labelSituation.text")); // NOI18N
        labelSituation.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelSituation.setName("labelSituation"); // NOI18N
        jScrollPane2.setViewportView(labelSituation);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setOpaque(false);

        jScrollPane3.setName("jScrollPane3"); // NOI18N
        jScrollPane3.setOpaque(false);

        labelSituationAll.setText(resourceMap.getString("labelSituationAll.text")); // NOI18N
        labelSituationAll.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelSituationAll.setName("labelSituationAll"); // NOI18N
        jScrollPane3.setViewportView(labelSituationAll);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel6.TabConstraints.tabTitle"), jPanel6); // NOI18N

        jPanel7.setName("jPanel7"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        labelMessage.setText(resourceMap.getString("labelMessage.text")); // NOI18N
        labelMessage.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelMessage.setName("labelMessage"); // NOI18N
        jScrollPane1.setViewportView(labelMessage);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel7.TabConstraints.tabTitle"), jPanel7); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .addComponent(labelUser, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                    .addComponent(labelResume))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelUser)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelResume)
                .addContainerGap())
        );

        jPanel3.setBorder(new javax.swing.border.MatteBorder(null));
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);

        buttonInvite.setAction(actionMap.get("inviteNextCustomer")); // NOI18N
        buttonInvite.setName("buttonInvite"); // NOI18N

        buttonKill.setAction(actionMap.get("killCustomer")); // NOI18N
        buttonKill.setText(resourceMap.getString("buttonKill.text")); // NOI18N
        buttonKill.setToolTipText(resourceMap.getString("buttonKill.toolTipText")); // NOI18N
        buttonKill.setName("buttonKill"); // NOI18N

        buttonStart.setAction(actionMap.get("getStartCustomer")); // NOI18N
        buttonStart.setName("buttonStart"); // NOI18N

        buttonFinish.setAction(actionMap.get("getStopCustomer")); // NOI18N
        buttonFinish.setName("buttonFinish"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        labelNextNumber.setFont(resourceMap.getFont("labelNextNumber.font")); // NOI18N
        labelNextNumber.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelNextNumber.setText(resourceMap.getString("labelNextNumber.text")); // NOI18N
        labelNextNumber.setName("labelNextNumber"); // NOI18N

        buttonRedirect.setAction(actionMap.get("redirectCustomer")); // NOI18N
        buttonRedirect.setName("buttonRedirect"); // NOI18N

        jLayeredPane1.setBorder(new javax.swing.border.MatteBorder(null));
        jLayeredPane1.setName("jLayeredPane1"); // NOI18N

        labelNextCustomerInfo.setText(resourceMap.getString("labelNextCustomerInfo.text")); // NOI18N
        labelNextCustomerInfo.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelNextCustomerInfo.setName("labelNextCustomerInfo"); // NOI18N
        labelNextCustomerInfo.setBounds(0, 0, 193, 320);
        jLayeredPane1.add(labelNextCustomerInfo, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLayeredPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                    .addComponent(buttonInvite, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                    .addComponent(buttonKill, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                    .addComponent(buttonStart, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                    .addComponent(buttonFinish, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                    .addComponent(buttonRedirect, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                    .addComponent(labelNextNumber, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(buttonInvite)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonKill)
                .addGap(8, 8, 8)
                .addComponent(buttonStart)
                .addGap(8, 8, 8)
                .addComponent(buttonRedirect)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonFinish)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelNextNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setName("jPanel1"); // NOI18N

        labelStatus.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelStatus.setText(resourceMap.getString("labelStatus.text")); // NOI18N
        labelStatus.setName("labelStatus"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(466, Short.MAX_VALUE)
                .addComponent(labelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelStatus)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelDownLayout = new javax.swing.GroupLayout(panelDown);
        panelDown.setLayout(panelDownLayout);
        panelDownLayout.setHorizontalGroup(
            panelDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDownLayout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelDownLayout.setVerticalGroup(
            panelDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDownLayout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        menuItemRefresh.setAction(actionMap.get("refreshClient")); // NOI18N
        menuItemRefresh.setName("menuItemRefresh"); // NOI18N
        fileMenu.add(menuItemRefresh);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText(resourceMap.getString("editMenu.text")); // NOI18N
        editMenu.setName("editMenu"); // NOI18N

        menuItemInvite.setAction(actionMap.get("inviteNextCustomer")); // NOI18N
        menuItemInvite.setName("menuItemInvite"); // NOI18N
        editMenu.add(menuItemInvite);

        menuItemKill.setAction(actionMap.get("killCustomer")); // NOI18N
        menuItemKill.setName("menuItemKill"); // NOI18N
        editMenu.add(menuItemKill);

        menuItemStart.setAction(actionMap.get("getStartCustomer")); // NOI18N
        menuItemStart.setName("menuItemStart"); // NOI18N
        editMenu.add(menuItemStart);

        menuItemRedirect.setAction(actionMap.get("redirectCustomer")); // NOI18N
        menuItemRedirect.setName("menuItemRedirect"); // NOI18N
        editMenu.add(menuItemRedirect);

        menuItemFinish.setAction(actionMap.get("getStopCustomer")); // NOI18N
        menuItemFinish.setName("menuItemFinish"); // NOI18N
        editMenu.add(menuItemFinish);

        menuBar.add(editMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        menuItemHelp.setAction(actionMap.get("help")); // NOI18N
        menuItemHelp.setText(resourceMap.getString("menuItemHelp.text")); // NOI18N
        menuItemHelp.setName("menuItemHelp"); // NOI18N
        helpMenu.add(menuItemHelp);

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelDown, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelDown, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-540)/2, (screenSize.height-523)/2, 540, 523);
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed
    /**
     * Табло вывода кастомера.
     */
    private static FIndicatorBoard indicatorBoard = null;

    /**
     * Если есть монитор для вывода номера клиента, то выведет текст нома на него.
     * Если текст для вывода пуст, то тобло перестанет моргать и отчистиццо.
     * @param text текст нома клиента.
     * @param blinkCount  0 - постоянное мигание, -1 не мигает. число - количество миганий
     */
    private void printCustomerNumber(String text, int blinkCount) {
        if (indicatorBoard != null) {
            FIndicatorBoard.printRecord(0, text, blinkCount);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws DocumentException {
        Uses.startSplash();
        Uses.isDebug = Uses.setLogining(args, false);
        final INetProperty netProperty = Uses.getClientNetProperty(args);
        // это заплата на баг с коннектом.
        // без предконнекта из main в дальнейшем сокет не хочет работать,
        // долго висит и вываливает минут через 15-20 эксепшн java.net.SocketException: Malformed reply from SOCKS server  
        Socket skt = null;
        try {
            skt = new Socket(netProperty.getServerAddress(), 61111);
            skt.close();
        } catch (IOException ex) {
        }

        // Отсечем вторую копию.
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(netProperty.getClientPort());
        } catch (SocketException ex) {
            Uses.log.logger.error("Сервер UDP не запустился, вторая копия не позволяется.");
            JOptionPane.showMessageDialog(null, getLocaleMessage("messages.restart.mess"), getLocaleMessage("messages.restart.caption"), JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
        socket.close();
        // Определим кто работает на данном месте.
        final Element user = FLogin.logining(netProperty, null, true, 3, FLogin.LEVEL_USER);
        Uses.showSplash();
        try {
            //Определим, надо ли выводить кастомера на второй экран.
            // Для этого должны быть переданы две координаты для определения этого монитора
            // -posx x -posy y
            String cfgFile = "";
            for (Integer i = 0; i < args.length - 1; i++) {
                if ("-cfg".equals(args[i])) {
                    cfgFile = args[i + 1];
                }
            }
            initIndicatorBoard(cfgFile);

            //Получаем состояние очередей для юзера
            final Element usersPlan = NetCommander.getSelfServicesCheck(netProperty, user.attributeValue(Uses.TAG_NAME));
            // Посмотрим, не пытались ли влезть под уже имеющейся в системе ролью
            if (Uses.ACCESS_DENY.equals(usersPlan.getTextTrim()) || Uses.LICENSE_DENY.equals(usersPlan.getTextTrim())) {
                Uses.closeSplash();
                JOptionPane.showMessageDialog(null, usersPlan.getTextTrim(), getLocaleMessage("messages.stop.mess"), JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
            //Показываем форму и передаем в нее описание того кто залогинился
            new FClient(user, usersPlan, netProperty).setVisible(true);
        } catch (AWTException ex) {
            Uses.log.logger.error("Ошибка работы с tray: ", ex);
            System.exit(0);
        } catch (Exception ex) {
            Uses.log.logger.error("Ошибка при старте: ", ex);
            System.exit(0);
        }
        //     }
        //  });
    }

    @Action
    public void refreshClient() {
        //Получаем состояние очередей для юзера
        setSituation(NetCommander.getSelfServices(netProperty, user.attributeValue(Uses.TAG_NAME)));
    }

    @Action
    public void help() {
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton buttonFinish;
    private javax.swing.JButton buttonInvite;
    private javax.swing.JButton buttonKill;
    private javax.swing.JButton buttonRedirect;
    private javax.swing.JButton buttonStart;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelMessage;
    private javax.swing.JLabel labelNextCustomerInfo;
    private javax.swing.JLabel labelNextNumber;
    private javax.swing.JLabel labelResume;
    private javax.swing.JLabel labelSituation;
    private javax.swing.JLabel labelSituationAll;
    private javax.swing.JLabel labelStatus;
    private javax.swing.JLabel labelUser;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem menuItemFinish;
    private javax.swing.JMenuItem menuItemHelp;
    private javax.swing.JMenuItem menuItemInvite;
    private javax.swing.JMenuItem menuItemKill;
    private javax.swing.JMenuItem menuItemRedirect;
    private javax.swing.JMenuItem menuItemRefresh;
    private javax.swing.JMenuItem menuItemStart;
    private javax.swing.JPanel panelDown;
    private javax.swing.JPopupMenu popupMenuTray;
    // End of variables declaration//GEN-END:variables
}
