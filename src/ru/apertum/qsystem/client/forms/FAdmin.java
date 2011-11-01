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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Properties;
import java.util.ServiceLoader;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;
import org.dom4j.DocumentException;
import org.jdesktop.application.Action;
import ru.apertum.qsystem.common.NetCommander;
import ru.apertum.qsystem.client.model.QTray;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.model.INetProperty;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import ru.apertum.qsystem.QSystem;
import ru.apertum.qsystem.client.Locales;
import ru.apertum.qsystem.client.help.Helper;
import ru.apertum.qsystem.client.common.WelcomeParams;
import ru.apertum.qsystem.common.cmd.RpcGetServerState.ServiceInfo;
import ru.apertum.qsystem.common.exceptions.ClientException;
import ru.apertum.qsystem.common.exceptions.ClientWarning;
import ru.apertum.qsystem.common.model.ATalkingClock;
import ru.apertum.qsystem.common.model.QCustomer;
import ru.apertum.qsystem.extra.IDataExchange;
import ru.apertum.qsystem.extra.IPing;
import ru.apertum.qsystem.reports.model.QReportsList;
import ru.apertum.qsystem.server.MainBoard;
import ru.apertum.qsystem.server.ServerProps;
import ru.apertum.qsystem.server.Spring;
import ru.apertum.qsystem.server.model.ISailListener;
import ru.apertum.qsystem.server.model.QAdvanceCustomer;
import ru.apertum.qsystem.server.model.QPlanService;
import ru.apertum.qsystem.server.model.schedule.QSchedule;
import ru.apertum.qsystem.server.model.QService;
import ru.apertum.qsystem.server.model.QServiceTree;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.QUserList;
import ru.apertum.qsystem.server.model.calendar.CalendarTableModel;
import ru.apertum.qsystem.server.model.calendar.QCalendar;
import ru.apertum.qsystem.server.model.calendar.QCalendarList;
import ru.apertum.qsystem.server.model.calendar.TableСell;
import ru.apertum.qsystem.server.model.calendar.FreeDay;
import ru.apertum.qsystem.server.model.infosystem.QInfoItem;
import ru.apertum.qsystem.server.model.infosystem.QInfoTree;
import ru.apertum.qsystem.server.model.postponed.QPostponedList;
import ru.apertum.qsystem.server.model.response.QRespItem;
import ru.apertum.qsystem.server.model.response.QResponseList;
import ru.apertum.qsystem.server.model.results.QResult;
import ru.apertum.qsystem.server.model.results.QResultList;
import ru.apertum.qsystem.server.model.schedule.QScheduleList;

/**
 * Created on 1 Декабрь 2008 г., 18:51
 * @author Evgeniy Egorov
 */
public class FAdmin extends javax.swing.JFrame {

    private static ResourceMap localeMap = null;

    public static String getLocaleMessage(String key) {
        if (localeMap == null) {
            localeMap = Application.getInstance(QSystem.class).getContext().getResourceMap(FAdmin.class);
        }
        return localeMap.getString(key);
    }
    /**
     * Константы хранения параметров в файле.
     */
    private static final String SERVER_ADRESS = "server_adress";
    private static final String SERVER_PORT = "server_port";
    private static final String SERVER_AUTO_REQUEST = "server_auto_request";
    private static final String CLIENT_ADRESS = "client_adress";
    private static final String CLIENT_PORT = "client_port";
    private static final String CLIENT_AUTO_REQUEST = "client_auto_request";
    private final QTray tray;
    //******************************************************************************************************************
    //******************************************************************************************************************
    //***************************************** таймер автоматического запроса******************************************
    private static final int DELAY_BLINK = 30000;
    /**
     * Таймер опроса компонент системы.
     */
    private final StartTimer timer = new StartTimer(DELAY_BLINK, new TimerPrinter());

    private class StartTimer extends Timer {

        public StartTimer(int delay, ActionListener listener) {
            super(delay, listener);
        }

        public void startTimer() {
            if (checkBoxServerAuto.isSelected()) {
                checkServer();
            }
            if (checkBoxClientAuto.isSelected()) {
                checkWelcome(null);
            }
            start();
        }
    }

    /**
     * Собыите автосканирования сервера и пункта регистрации на таймер.
     */
    private class TimerPrinter implements ActionListener {

        /**
         * Обеспечение автоматизации запроса.
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (checkBoxServerAuto.isSelected()) {
                checkServer();
            }
            if (checkBoxClientAuto.isSelected()) {
                checkWelcome(null);
            }
        }
    };

    /**
     * Этим методом запускаем таймер автоматического опроса
     */
    private void startTimer() {
        if (checkBoxServerAuto.isSelected() || checkBoxClientAuto.isSelected()) {
            if (!timer.isRunning()) {
                timer.startTimer();
            }
        } else {
            timer.stop();
        }
    }
    //***************************************** таймер автоматического запроса  *************************************************

    /**
     * Creates new form FAdmin 
     */
    public FAdmin() {
        addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                timer.stop();
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
        initComponents();


        try {
            setIconImage(ImageIO.read(FAdmin.class.getResource("/ru/apertum/qsystem/client/forms/resources/admin.png")));
        } catch (IOException ex) {
            System.err.println(ex);
        }

        // Отцентирируем
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((Math.round(kit.getScreenSize().width - getWidth()) / 2), (Math.round(kit.getScreenSize().height - getHeight()) / 2));
        // Поставим эконку
        tray = QTray.getInstance(this, "/ru/apertum/qsystem/client/forms/resources/admin.png", getLocaleMessage("tray.caption"));
        tray.addItem(getLocaleMessage("tray.caption"), new ActionListener() {

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
        tray.addItem(getLocaleMessage("tray.exit"), new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                System.exit(0);
            }
        });

        int ii = 1;
        final ButtonGroup bg = new ButtonGroup();
        final String currLng = Locales.getInstance().getLangCurrName();
        for (String lng : Locales.getInstance().getAvailableLocales()) {
            final JRadioButtonMenuItem item = new JRadioButtonMenuItem(org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getActionMap(FAdmin.class, this).get("setCurrentLang"));
            bg.add(item);
            item.setSelected(lng.equals(currLng));
            item.setText(lng); // NOI18N
            item.setName("QRadioButtonMenuItem" + (ii++)); // NOI18N
            menuLangs.add(item);
        }

        // Определим события выбора итемов в списках.
        listUsers.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                userListChange();
            }
        });
        // Определим события выбора итемов в списках.
        listResponse.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                responseListChange();
            }
        });
        listSchedule.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                scheduleListChange();
            }
        });
        listCalendar.addListSelectionListener(new ListSelectionListener() {

            private int oldSelectedValue = 0;
            private int tmp = 0;

            public int getOldSelectedValue() {
                return oldSelectedValue;
            }

            public void setOldSelectedValue(int oldSelectedValue) {
                this.oldSelectedValue = tmp;
                this.tmp = oldSelectedValue;
            }
            private boolean canceled = false;

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (canceled) {
                    canceled = false;
                } else {
                    if (tableCalendar.getModel() instanceof CalendarTableModel) {
                        final CalendarTableModel model = (CalendarTableModel) tableCalendar.getModel();
                        if (!model.isSaved()) {
                            final int res = JOptionPane.showConfirmDialog(null, getLocaleMessage("calendar.change.title"), getLocaleMessage("calendar.change.caption"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                            switch (res) {
                                case 0:   // сохранить и переключиться
                                    model.save();
                                    calendarListChange();
                                    setOldSelectedValue(listCalendar.getSelectedIndex());
                                    break;
                                case 1: // переключаемся без сохранения

                                    calendarListChange();
                                    setOldSelectedValue(listCalendar.getSelectedIndex());

                                    break;
                                case 2: // не сохранять и остаться на прежнем уровне
                                    canceled = true;
                                    listCalendar.setSelectedIndex(getOldSelectedValue());
                                    break;
                            }
                        } else {
                            calendarListChange();
                            setOldSelectedValue(listCalendar.getSelectedIndex());
                        }
                    } else {
                        calendarListChange();
                        setOldSelectedValue(listCalendar.getSelectedIndex());
                    }
                }
            }
        });
        // Определим события выбора сайта в списках.
        treeServices.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeInfo.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        /*
        treeServices.setCellRenderer(new DefaultTreeCellRenderer() {
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        setText(((Element) value).attributeValue(Uses.TAG_NAME));
        return this;
        }
        });*/
        treeServices.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                serviceListChange();
            }
        });
        treeInfo.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                infoListChange();
            }
        });

        textFieldStartTime.setInputVerifier(DateVerifier);
        textFieldFinishTime.setInputVerifier(DateVerifier);


        //Загрузим настройки
        loadSettings();
        // Старт таймера автоматических запросов.
        startTimer();
        // Грузим конфигурацию
        loadConfig();

        /*
        userListChange();
        serviceListChange();
        siteListChange();
         */
        spinnerPropServerPort.getModel().addChangeListener(new ChangeNet());
        spinnerPropClientPort.getModel().addChangeListener(new ChangeNet());
        spinnerWebServerPort.getModel().addChangeListener(new ChangeNet());

        spinnerServerPort.getModel().addChangeListener(new ChangeSettings());
        spinnerClientPort.getModel().addChangeListener(new ChangeSettings());
        spinnerUserRS.getModel().addChangeListener(new ChangeUser());

        //привязка помощи к форме.
        final Helper helper = Helper.getHelp("ru/apertum/qsystem/client/help/admin.hs");
        helper.setHelpListener(menuItemHelp);
        helper.enableHelpKey(jPanel1, "introduction");
        helper.enableHelpKey(jPanel3, "monitoring");
        helper.enableHelpKey(jPanel4, "configuring");
        helper.enableHelpKey(jPanel8, "net");


        helper.enableHelpKey(jPanel17, "schedulers");
        helper.enableHelpKey(jPanel19, "calendars");
        helper.enableHelpKey(jPanel2, "infoSystem");
        helper.enableHelpKey(jPanel13, "responses");
        helper.enableHelpKey(jPanel18, "results");

        Uses.closeSplash();
    }

    /**
     * Сохранять спинедиты сетевых настроек
     */
    private class ChangeNet implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            saveNet();
        }
    }

    /**
     * Сохранять настройки спинедита мониторинга
     */
    private class ChangeSettings implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            saveSettings();
        }
    }

    /**
     * Сохранять настройки спинедита юзера
     */
    private class ChangeUser implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            saveUser();
        }
    }
    /**
     * вспомогательные для отсечения событий сохранения
     */
    private boolean changeSite = true;
    private boolean changeUser = true;

    /**
     * Действия по смене выбранного итема в списке пользоватеолей.
     */
    private void userListChange() {
        if (listUsers.getLastVisibleIndex() == -1) {
            listUserService.setListData(new Object[0]);
            textFieldUserName.setText("");
            textFieldUserIdent.setText("");
            passwordFieldUser.setText("");
            return;
        }
        final QUser user = (QUser) listUsers.getSelectedValue();
        if (user == null) {
            return;
        }
        changeUser = false;
        try {
            textFieldUserName.setText(user.getName());
            textFieldUserIdent.setText(user.getPoint());
            passwordFieldUser.setText(user.getPassword());
            spinnerUserRS.setValue(user.getAdressRS());
            checkBoxAdmin.setSelected(user.getAdminAccess());
            checkBoxReport.setSelected(user.getReportAccess());
            listUserService.setModel(user.getPlanServiceList());
            if (listUserService.getLastVisibleIndex() != -1) {
                listUserService.setSelectedIndex(0);
            }
        } finally {
            changeUser = true;
        }
    }

    /**
     * Действия по смене выбранного итема в списке отзывов.
     */
    private void responseListChange() {
        if (listResponse.getLastVisibleIndex() == -1) {
            textFieldResponse.setText("");
            textPaneResponse.setText("");
            labelRespinse.setText("");
            return;
        }
        final QRespItem item = (QRespItem) listResponse.getSelectedValue();
        if (item == null) {
            return;
        }
        textFieldResponse.setText(item.getName());
        textPaneResponse.setText(item.getHTMLText());
        labelRespinse.setText(item.getHTMLText());
    }

    /**
     * Действия по смене выбранного итема в списке планов расписания.
     */
    private void scheduleListChange() {
        if (listSchedule.getLastVisibleIndex() == -1) {
            textFieldScheduleName.setText("");
            labelSchedule.setText("");
            return;
        }
        final QSchedule item = (QSchedule) listSchedule.getSelectedValue();
        if (item == null) {
            return;
        }
        textFieldScheduleName.setText(item.getName());
        String str = "<HTML>"
                + "<span style='font-size:12.0pt;color:blue;'>"
                + "<b>" + getLocaleMessage("calendar.plan_params") + ":</b>"
                + "<table  border='0'>"
                + (item.getType() == 0
                ? (((item.getTime_begin_1() == null || item.getTime_end_1() == null) ? "" : "<tr><td>" + getLocaleMessage("calendar.day.monday") + "</td><td>с " + Uses.format_HH_mm.format(item.getTime_begin_1()) + "</td><td>до " + Uses.format_HH_mm.format(item.getTime_end_1()) + "</td></tr>")
                + ((item.getTime_begin_2() == null || item.getTime_end_2() == null) ? "" : "<tr><td>" + getLocaleMessage("calendar.day.tuesday") + "</td><td>с " + Uses.format_HH_mm.format(item.getTime_begin_2()) + "</td><td>до " + Uses.format_HH_mm.format(item.getTime_end_2()) + "</td></tr>")
                + ((item.getTime_begin_3() == null || item.getTime_end_3() == null) ? "" : "<tr><td>" + getLocaleMessage("calendar.day.wednesday") + "</td><td>с " + Uses.format_HH_mm.format(item.getTime_begin_3()) + "</td><td>до " + Uses.format_HH_mm.format(item.getTime_end_3()) + "</td></tr>")
                + ((item.getTime_begin_4() == null || item.getTime_end_4() == null) ? "" : "<tr><td>" + getLocaleMessage("calendar.day.thursday") + "</td><td>с " + Uses.format_HH_mm.format(item.getTime_begin_4()) + "</td><td>до " + Uses.format_HH_mm.format(item.getTime_end_4()) + "</td></tr>")
                + ((item.getTime_begin_5() == null || item.getTime_end_5() == null) ? "" : "<tr><td>" + getLocaleMessage("calendar.day.friday") + "</td><td>с " + Uses.format_HH_mm.format(item.getTime_begin_5()) + "</td><td>до " + Uses.format_HH_mm.format(item.getTime_end_5()) + "</td></tr>")
                + ((item.getTime_begin_6() == null || item.getTime_end_6() == null) ? "" : "<tr><td>" + getLocaleMessage("calendar.day.saturday") + "</td><td>с " + Uses.format_HH_mm.format(item.getTime_begin_6()) + "</td><td>до " + Uses.format_HH_mm.format(item.getTime_end_6()) + "</td></tr>")
                + ((item.getTime_begin_7() == null || item.getTime_end_7() == null) ? "" : "<tr><td>" + getLocaleMessage("calendar.day.sunday") + "</td><td>с " + Uses.format_HH_mm.format(item.getTime_begin_7()) + "</td><td>до " + Uses.format_HH_mm.format(item.getTime_end_7()) + "</td></tr>")) : ((item.getTime_begin_1() == null || item.getTime_end_1() == null) ? "" : "<tr><td>" + getLocaleMessage("calendar.even") + "</td><td>" + getLocaleMessage("calendar.time.from") + " " + Uses.format_HH_mm.format(item.getTime_begin_1()) + "</td><td>" + getLocaleMessage("calendar.time.to") + " " + Uses.format_HH_mm.format(item.getTime_end_1()) + "</td></tr>"
                + ((item.getTime_begin_2() == null || item.getTime_end_2() == null) ? "" : "<tr><td>" + getLocaleMessage("calendar.even") + "</td><td>" + getLocaleMessage("calendar.time.from") + " " + Uses.format_HH_mm.format(item.getTime_begin_2()) + "</td><td>" + getLocaleMessage("calendar.time.to") + " " + Uses.format_HH_mm.format(item.getTime_end_2()) + "</td></tr>"))) + "</table>" + "</span>";
        labelSchedule.setText(str);
    }

    /**
     * Действия по смене выбранного итема в списке планов расписания.
     */
    private void calendarListChange() {
        if (listCalendar.getLastVisibleIndex() == -1) {
            textFieldCalendarName.setText("");
            return;
        }
        final QCalendar item = (QCalendar) listCalendar.getSelectedValue();
        if (item == null) {
            return;
        }
        textFieldCalendarName.setText(item.getName());

        tableCalendar.setModel(new CalendarTableModel(item.getId()));
        tableCalendar.setDefaultRenderer(FreeDay.class, new TableСell());
        tableCalendar.setDefaultRenderer(Object.class, new TableСell());
        tableCalendar.getColumnModel().getColumn(0).setPreferredWidth(500);
    }

    /**
     * Действия по смене выбранного итема в списке услуг.
     */
    private void serviceListChange() {
        final TreePath selectedPath = treeServices.getSelectionPath();
        if (selectedPath == null) {
            return;
        } else {
            showServiceInfo((QService) selectedPath.getLastPathComponent());
        }
    }

    private void showServiceInfo(QService service) {
        labelServiceInfo.setText("<html><body text=\"#336699\"> " + getLocaleMessage("service.service") + ": \""+ "<font color=\"#000000\">" + service.getName() + "\"    " + "</font>" 
                + "<font color=\"#"
                + (service.getStatus() == 1
                ? "00AA00\">"+getLocaleMessage("service.kind.active") 
                : (service.getStatus() == 0 ?  "CCAA00\">" + getLocaleMessage("service.kind.not_active") : "DD0000\">" + getLocaleMessage("service.kind.unavailable")))
                + "</font>"
                + ";    " + getLocaleMessage("service.prefix") + ": " + "<font color=\"#DD0000\">" + service.getPrefix() + "</font>" + ";  " + getLocaleMessage("service.description") + ": " + service.getDescription()
                + ";<br>" + getLocaleMessage("service.restrict_day_reg") + ": " + (service.getDayLimit() == 0 ? getLocaleMessage("service.work_calendar.no") : service.getDayLimit())
                + ";<br>" + getLocaleMessage("service.restrict_adv_reg") + ": " + service.getAdvanceLimit()
                + ";<br>  " + getLocaleMessage("service.restrict_adv_period") + ": " + service.getAdvanceLimitPeriod()
                + ";<br>" + getLocaleMessage("service.work_calendar") + ": " + "<font color=\"#" + (service.getCalendar() == null ? "DD0000\">" +getLocaleMessage("service.work_calendar.no") : "000000\">" +service.getCalendar().toString()) + "</font>" + ";  " + getLocaleMessage("service.work_calendar.plan") + ": " + "<font color=\"#" + (service.getSchedule() == null ? "DD0000\">" + getLocaleMessage("service.work_calendar.no") : "000000\">" + service.getSchedule().toString()) + "</font>" + ";<br>"
                + (service.getInput_required() ? getLocaleMessage("service.required_client_data") + ": \"" + service.getInput_caption() + "\"(" + service.getPersonDayLimit() + ")" : getLocaleMessage("service.required_client_data.not")) + ";<br>   "
                + (service.getResult_required() ? getLocaleMessage("service.required_result") : getLocaleMessage("service.required_result.not")) + ";");
        labelButtonCaption.setText(service.getButtonText());
    }

    /**
     * Действия по смене выбранного итема в дереве инфоузлов.
     */
    private void infoListChange() {
        final TreePath selectedPath = treeInfo.getSelectionPath();
        if (selectedPath == null) {
            return;
        } else {
            showInfoInfo((QInfoItem) selectedPath.getLastPathComponent());
        }
    }

    private void showInfoInfo(QInfoItem item) {
        textFieldInfoItemName.setText(item.getName());
        labelInfoItem.setText(item.getHTMLText());
        textPaneInfoItem.setText(item.getHTMLText());
        textPaneInfoPrint.setText(item.getTextPrint());
    }
    /**
     * Ограничение ввода время начала и конце работы системы.
     */
    private InputVerifier DateVerifier = new InputVerifier() {

        @Override
        public boolean verify(JComponent input) {
            final DateFormat dateFormat = new SimpleDateFormat("HH:mm");
            try {
                if (input == textFieldStartTime) {
                    dateFormat.parse(textFieldStartTime.getText());
                }
                if (input == textFieldFinishTime) {
                    dateFormat.parse(textFieldFinishTime.getText());
                }
            } catch (ParseException ex) {
                System.err.println("Незапарсилась дата " + textFieldStartTime.getText() + " или" + textFieldFinishTime.getText());
                return false;
            }
            saveNet();
            return true;
        }
    };

    /**
     * Загрузим настройки.
     */
    private void loadSettings() {
        final Properties settings = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream("config" + File.separator + "admin.properties");
        } catch (FileNotFoundException ex) {
            throw new ClientException(getLocaleMessage("error.file_not_read") + ". " + ex);
        }
        try {
            settings.load(in);
        } catch (IOException ex) {
            throw new ClientException(getLocaleMessage("error.params_not_read") + ". " + ex);
        }
        textFieldServerAddr.setText(settings.getProperty(SERVER_ADRESS));
        spinnerServerPort.setValue(Integer.parseInt(settings.getProperty(SERVER_PORT)));
        checkBoxServerAuto.setSelected("1".equals(settings.getProperty(SERVER_AUTO_REQUEST)));
        textFieldClientAdress.setText(settings.getProperty(CLIENT_ADRESS));
        spinnerClientPort.setValue(Integer.parseInt(settings.getProperty(CLIENT_PORT)));
        checkBoxClientAuto.setSelected("1".equals(settings.getProperty(CLIENT_AUTO_REQUEST)));
    }

    /**
     * Сохраним настройки.
     */
    private void saveSettings() {
        final Properties settings = new Properties();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream("config" + File.separator + "admin.properties");
        } catch (FileNotFoundException ex) {
            throw new ClientException(getLocaleMessage("error.file_not_save") + ". " + ex);
        }
        settings.put(SERVER_ADRESS, textFieldServerAddr.getText());
        settings.put(SERVER_PORT, String.valueOf(spinnerServerPort.getValue()));
        settings.put(SERVER_AUTO_REQUEST, checkBoxServerAuto.isSelected() ? "1" : "0");
        settings.put(CLIENT_ADRESS, textFieldClientAdress.getText());
        settings.put(CLIENT_PORT, String.valueOf(spinnerClientPort.getValue()));
        settings.put(CLIENT_AUTO_REQUEST, checkBoxClientAuto.isSelected() ? "1" : "0");
        try {
            settings.store(out, "Settings of admining and monitoring");
        } catch (IOException ex) {
            throw new ClientException(getLocaleMessage("error.file_output") + ". " + ex);
        }
    }

    /**
     * Загрузим конфигурацию системы.
     */
    private void loadConfig() {
        listUsers.setModel(QUserList.getInstance());
        listResponse.setModel(QResponseList.getInstance());
        listResults.setModel(QResultList.getInstance());
        treeServices.setModel(QServiceTree.getInstance());
        treeInfo.setModel(QInfoTree.getInstance());
        listSchedule.setModel(QScheduleList.getInstance());
        listCalendar.setModel(QCalendarList.getInstance());
        listReposts.setModel(QReportsList.getInstance());

        spinnerPropServerPort.setValue(ServerProps.getInstance().getProps().getServerPort());
        spinnerPropClientPort.setValue(ServerProps.getInstance().getProps().getClientPort());
        spinnerWebServerPort.setValue(ServerProps.getInstance().getProps().getWebServerPort());
        textFieldStartTime.setText(Uses.format_HH_mm.format(ServerProps.getInstance().getProps().getStartTime()));
        textFieldFinishTime.setText(Uses.format_HH_mm.format(ServerProps.getInstance().getProps().getFinishTime()));
        textFieldURLWebService.setText(ServerProps.getInstance().getProps().getSkyServerUrl());

        textFieldZonBoadrServAddr.setText(ServerProps.getInstance().getProps().getZoneBoardServAddr());
        spinnerZonBoadrServPort.setValue(ServerProps.getInstance().getProps().getZoneBoardServPort());

        spinnerBranchId.setValue(ServerProps.getInstance().getProps().getBranchOfficeId());
        spinnerFirstNumber.setValue(ServerProps.getInstance().getProps().getFirstNumber());
        spinnerLastNumber.setValue(ServerProps.getInstance().getProps().getLastNumber());
        rbKindCommon.setSelected(!ServerProps.getInstance().getProps().getNumering());
        rbKindPersonal.setSelected(ServerProps.getInstance().getProps().getNumering());
        switch (ServerProps.getInstance().getProps().getPoint()) {
            case 0:
                rbPointOffice.setSelected(true);
                break;
            case 1:
                rbPointWindow.setSelected(true);
                break;
            case 2:
                rbPointStoika.setSelected(true);
                break;
        }
        switch (ServerProps.getInstance().getProps().getSound()) {
            case 0:
                rbNotificationNo.setSelected(true);
                break;
            case 1:
                rbNotificationGong.setSelected(true);
                break;
            case 2:
                rbNotificationGongVoice.setSelected(true);
                break;
        }

        // выставим начальные позиции в списках
        if (listUsers.getLastVisibleIndex() != -1) {
            listUsers.setSelectedIndex(0);
        }

        if (listResponse.getLastVisibleIndex() != -1) {
            listResponse.setSelectedIndex(0);
        }

        if (listSchedule.getLastVisibleIndex() != -1) {
            listSchedule.setSelectedIndex(0);
        }

        if (listCalendar.getLastVisibleIndex() != -1) {
            listCalendar.setSelectedIndex(0);
        }

        if (treeServices.getModel().getRoot() != null) {
            treeServices.setSelectionPath(new TreePath(treeServices.getModel().getRoot()));
        }

        if (treeInfo.getModel().getRoot() != null) {
            treeInfo.setSelectionPath(new TreePath(treeInfo.getModel().getRoot()));
        }

        if (listUserService.getLastVisibleIndex() != -1) {
            listUserService.setSelectedIndex(0);
        }

    }

    private class ServerNetProperty implements INetProperty {

        @Override
        public Integer getPort() {
            return (Integer) spinnerServerPort.getValue();
        }

        @Override
        public InetAddress getAddress() {
            InetAddress adr = null;
            try {
                adr = InetAddress.getByName(textFieldServerAddr.getText());
            } catch (UnknownHostException ex) {
                throw new ClientException("Error! " + ex);
            }
            return adr;
        }
    }

    protected void checkServer() {
        QLog.l().logger().info("Запрос о состоянии на сервер.");
        //элемент ответа.
        final LinkedList<ServiceInfo> srvs;
        try {
            srvs = NetCommander.getServerState(new ServerNetProperty());
            listPostponed.setModel(QPostponedList.getInstance().loadPostponedList(NetCommander.getPostponedPoolInfo(new ServerNetProperty())));
        } catch (Exception ex) {
            listPostponed.setModel(QPostponedList.getInstance().loadPostponedList(new LinkedList<QCustomer>()));
            labelServerState.setText("<HTML><b><span style='font-size:20.0pt;color:red;'>" + getLocaleMessage("admin.message.server_not_start") + "</span></b>");
            QLog.l().logger().error("Сервер ответил на запрос о состоянии: \"" + ex + "\"");
            tray.showMessageTray(getLocaleMessage("tray.server"), getLocaleMessage("tray.message.stop_server"), QTray.MessageType.WARNING);
            return;
        }
        //Сформируем ответ
        final String red = "<td align=\"center\"><span style='font-size:12.0pt;color:red;'>";
        final String green = "<td align=\"center\"><span style='font-size:12.0pt;color:green;'>";
        int col = 0;
        String html = "";
        for (ServiceInfo inf : srvs) {
            col = +inf.getCountWait();
            html = html
                    + "<tr><td>" + (inf.getServiceName().length() > 80 ? inf.getServiceName().substring(0, 80) + "..." : inf.getServiceName()) + "</span></td>"
                    + (0 == inf.getCountWait() ? green : red) + inf.getCountWait() + "</span></td>"
                    + "<td align=\"center\">" + inf.getFirstNumber() + "</span></td></tr>";
        }
        final String first = "<html>" + getLocaleMessage("admin.info.total_clients") + ": " + (0 == col ? "<span style='font-size:12.0pt;color:green;'>" : "<span style='font-size:12.0pt;color:red;'>") + col + "</span>";
        labelServerState.setText(first
                + "<table border=\"1\"><tr>  <td align=\"center\"<span style='font-size:16.0pt;color:red;'>" + getLocaleMessage("service.service") + "</span></td>  <td align=\"center\"><span style='font-size:16.0pt;color:red;'>" + getLocaleMessage("admin.info.total_wait") + "</span></td>  <td align=\"center\"><span style='font-size:16.0pt;color:red;'>" + getLocaleMessage("admin.info.next_number") + "</span></td></tr>"
                + html
                + "</table></html>");
    }

    protected void checkWelcome(String command) {
        QLog.l().logger().info("Запрос о состоянии на пункт регистрации.");
        command = command == null ? "Empty" : command;
        final String result;
        try {
            result = NetCommander.getWelcomeState(netPropWelcome(), command);
        } catch (Exception ex) {
            labelWelcomeState.setText("<HTML><b><span style='font-size:20.0pt;color:red;'>" + getLocaleMessage("admin.message.welcome_not_start") + "</span></b>");
            QLog.l().logger().error("Пункт регистрации не ответил на запрос о состоянии или поризошла ошибка. \"" + ex + "\"");
            tray.showMessageTray(getLocaleMessage("tray.message_stop_server.title"), getLocaleMessage("tray.message_stop_server.caption"), QTray.MessageType.WARNING);
            return;
        }
        labelWelcomeState.setText("<HTML><span style='font-size:20.0pt;color:green;'>" + getLocaleMessage("admin.welcome") + " \"" + result + "\"</span>");
    }

    protected INetProperty netPropWelcome() {
        return new INetProperty() {

            @Override
            public Integer getPort() {
                return (Integer) spinnerClientPort.getValue();
            }

            @Override
            public InetAddress getAddress() {
                InetAddress adr = null;
                try {
                    adr = InetAddress.getByName(textFieldClientAdress.getText());
                } catch (UnknownHostException ex) {
                    throw new ClientException("Error! " + ex);
                }
                return adr;
            }
        };
    }

    /**
     * Сохранение данных о юзере, повесим на потерю фокуса элементов ввода.
     */
    public void saveUser() {
        if (changeUser) {
            final QUser user = (QUser) listUsers.getSelectedValue();
            user.setName(textFieldUserName.getText());
            user.setPoint(textFieldUserIdent.getText());
            user.setPassword(new String(passwordFieldUser.getPassword()));
            user.setAdressRS((Integer) spinnerUserRS.getValue());
            user.setAdminAccess(checkBoxAdmin.isSelected());
            user.setReportAccess(checkBoxReport.isSelected());
        }
    }

    /**
     * Сохранение данных о сетевых настройках, повесим на нажатие кнопок элементов ввода.
     */
    public void saveNet() {

        ServerProps.getInstance().getProps().setServerPort((Integer) spinnerPropServerPort.getValue());
        ServerProps.getInstance().getProps().setClientPort((Integer) spinnerPropClientPort.getValue());
        ServerProps.getInstance().getProps().setWebServerPort((Integer) spinnerWebServerPort.getValue());
        if ((Integer) spinnerFirstNumber.getValue() > (Integer) spinnerLastNumber.getValue()) {
            spinnerFirstNumber.setValue(1);
            spinnerLastNumber.setValue(999);
        }
        ServerProps.getInstance().getProps().setZoneBoardServPort((Integer) spinnerZonBoadrServPort.getValue());
        ServerProps.getInstance().getProps().setZoneBoardServAddr(textFieldZonBoadrServAddr.getText());

        ServerProps.getInstance().getProps().setFirstNumber((Integer) spinnerFirstNumber.getValue());
        ServerProps.getInstance().getProps().setBranchOfficeId((Long) spinnerBranchId.getValue());
        ServerProps.getInstance().getProps().setSkyServerUrl(textFieldURLWebService.getText());
        ServerProps.getInstance().getProps().setLastNumber((Integer) spinnerLastNumber.getValue());
        ServerProps.getInstance().getProps().setNumering(rbKindPersonal.isSelected());
        ServerProps.getInstance().getProps().setPoint(rbPointOffice.isSelected() ? 0 : rbPointWindow.isSelected() ? 1 : 2);
        ServerProps.getInstance().getProps().setSound(rbNotificationNo.isSelected() ? 0 : rbNotificationGong.isSelected() ? 1 : 2);
        try {
            ServerProps.getInstance().getProps().setStartTime(Uses.format_HH_mm.parse(textFieldStartTime.getText()));
            ServerProps.getInstance().getProps().setFinishTime(Uses.format_HH_mm.parse(textFieldFinishTime.getText()));
        } catch (ParseException ex) {
            QLog.l().logger().error("Проблемы с сохранение сетевых настроек. ", ex);
        }
    }

    /**
     * @param args the command line arguments
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        Locale.setDefault(Locales.getInstance().getLangCurrent());
        Uses.startSplash();
        QLog.initial(args, false);
        // Определим кто работает на данном месте.
        FLogin.logining(QUserList.getInstance(), null, true, 3, FLogin.LEVEL_ADMIN);
        Uses.showSplash();
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new FAdmin().setVisible(true);
            }
        });
    }

    @Action
    public void hideWindow() {
    }

    @Action
    public void addUser() {
        // Запросим название юзера и если оно уникально, то примем
        String userName = "";
        boolean flag = true;
        while (flag) {
            userName = (String) JOptionPane.showInputDialog(this, getLocaleMessage("admin.add_user_dialog.title"), getLocaleMessage("admin.add_user_dialog.caption"), 3, null, null, userName);
            if (userName == null) {
                return;
            }
            if ("".equals(userName)) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_user_dialog.err1.title"), getLocaleMessage("admin.add_user_dialog.err1.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (QUserList.getInstance().hasByName(userName)) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_user_dialog.err2.title"), getLocaleMessage("admin.add_user_dialog.err2.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (userName.indexOf('\"') != -1) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_user_dialog.err3.title"), getLocaleMessage("admin.add_user_dialog.err3.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (userName.length() > 150) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_user_dialog.err4.title"), getLocaleMessage("admin.add_user_dialog.err4.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else {
                flag = false;
            }
        }
        QLog.l().logger().debug("Добавляем пользователя \"" + userName + "\"");
        final QUser user = new QUser();
        user.setName(userName);
        user.setPassword("");
        user.setPoint("");
        user.setAdressRS(32);
        QUserList.getInstance().addElement(user);
        listUsers.setSelectedValue(user, true);
    }

    @Action
    public void renameUser() {
        if (listUsers.getSelectedIndex() != -1) {
            final QUser user = (QUser) listUsers.getSelectedValue();
            String userName = user.getName();
            boolean flag = true;
            while (flag) {
                userName = (String) JOptionPane.showInputDialog(this, getLocaleMessage("admin.rename_user_dialog.title"), getLocaleMessage("admin.rename_user_dialog.caption"), 3, null, null, userName);
                if (userName == null) {
                    return;
                }
                if ("".equals(userName)) {
                    JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.rename_user_dialog.err1.title"), getLocaleMessage("admin.rename_user_dialog.err1.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                } else if (QUserList.getInstance().hasByName(userName)) {
                    JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.rename_user_dialog.err2.title"), getLocaleMessage("admin.rename_user_dialog.err2.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                } else if (userName.indexOf('\"') != -1) {
                    JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.rename_user_dialog.err3.title"), getLocaleMessage("admin.rename_user_dialog.err3.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                } else if (userName.length() > 150) {
                    JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.rename_user_dialog.err4.title"), getLocaleMessage("admin.rename_user_dialog.err4.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                } else {
                    flag = false;
                }
            }
            user.setName(userName);
            textFieldUserName.setText(userName);
            listUsers.setSelectedValue(user, true);
        }
    }

    @Action
    public void deleteUser() {
        if (listUsers.getSelectedIndex() != -1) {
            if (JOptionPane.showConfirmDialog(this,
                    getLocaleMessage("admin.remove_user_dialog.title") + " \"" + ((QUser) listUsers.getSelectedValue()).getName() + "\"?",
                    getLocaleMessage("admin.remove_user_dialog.caption"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
            QLog.l().logger().debug("Удаляем пользователя \"" + ((QUser) listUsers.getSelectedValue()).getName() + "\"");

            final int del = listUsers.getSelectedIndex();
            final QUserList m = (QUserList) listUsers.getModel();
            final int col = m.getSize();

            final QUser user = (QUser) listUsers.getSelectedValue();
            //проверим не последний ли это админ
            if (user.getAdminAccess()) {
                int cnt = 0;
                for (int i = 0; i < listUsers.getModel().getSize(); i++) {
                    if (((QUser) listUsers.getModel().getElementAt(i)).getAdminAccess()) {
                        cnt++;
                    }
                }
                if (cnt == 1) {
                    JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.remove_user_dialog.err.title"), getLocaleMessage("admin.remove_user_dialog.err.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
            QUserList.getInstance().removeElement(user);

            if (col != 1) {
                if (col == del + 1) {
                    listUsers.setSelectedValue(m.getElementAt(del - 1), true);
                } else if (col > del + 1) {
                    listUsers.setSelectedValue(m.getElementAt(del), true);
                }
            }
        }
    }

    @Action
    public void addService() throws DocumentException {
        // Запросим название услуги и если оно уникально и не пусто, то примем
        String serviceName = "";
        boolean flag = true;
        while (flag) {
            serviceName = (String) JOptionPane.showInputDialog(this, getLocaleMessage("admin.add_service_dialog.title"), getLocaleMessage("admin.add_service_dialog.caption"), 3, null, null, serviceName);
            if (serviceName == null) {
                return;
            }
            if ("".equals(serviceName)) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_service_dialog.err1.title"), getLocaleMessage("admin.add_service_dialog.err1.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (QServiceTree.getInstance().hasByName(serviceName)) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_service_dialog.err2.title"), getLocaleMessage("admin.add_service_dialog.err2.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (serviceName.indexOf('\"') != -1) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_service_dialog.err3.title"), getLocaleMessage("admin.add_service_dialog.err2.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (serviceName.length() > 2001) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_service_dialog.err4.title"), getLocaleMessage("admin.add_service_dialog.err2.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else {
                flag = false;
            }
        }
        // Созданим новую услугу и добавим ее в модель
        final QService newService = new QService();
        newService.setName(serviceName);
        newService.setDescription(serviceName);
        newService.setStatus(1);
        newService.setCalendar(QCalendarList.getInstance().getById(1));
        newService.setButtonText("<html><b><p align=center><span style='font-size:20.0pt;color:red'>" + serviceName + "</span></b>");
        final QService parentService = (QService) treeServices.getLastSelectedPathComponent();
        QServiceTree.getInstance().insertNodeInto(newService, parentService, parentService.getChildCount());
        final TreeNode[] nodes = QServiceTree.getInstance().getPathToRoot(newService);
        final TreePath path = new TreePath(nodes);
        treeServices.scrollPathToVisible(path);
        treeServices.setSelectionPath(path);
        // родительскую услугу к новой услуге нужно исключить из списка привязанных к юзерам, т.к. она стала группой
        deleteServiceFromUsers(parentService);

        QLog.l().logger().debug("Добавлена услуга \"" + serviceName + "\" в группу \"" + parentService.getName() + "\"");
    }

    @Action
    public void renameService() {
        final QService service = (QService) treeServices.getLastSelectedPathComponent();
        if (service != null) {
            String serviceName = service.getName();
            boolean flag = true;
            while (flag) {
                serviceName = (String) JOptionPane.showInputDialog(this, getLocaleMessage("admin.rename_service_dialog.title"), getLocaleMessage("admin.rename_service_dialog.caption"), 3, null, null, serviceName);
                if (serviceName == null) {
                    return;
                }
                if ("".equals(serviceName)) {
                    JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.rename_service_dialog.err1.title"), getLocaleMessage("admin.rename_service_dialog.err1.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                } else if (QServiceTree.getInstance().hasByName(serviceName)) {
                    JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.rename_service_dialog.err2.title"), getLocaleMessage("admin.rename_service_dialog.err2.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                } else if (serviceName.indexOf('\"') != -1) {
                    JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.rename_service_dialog.err3.title"), getLocaleMessage("admin.rename_service_dialog.err3.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                } else if (serviceName.length() > 2001) {
                    JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.rename_service_dialog.err4.title"), getLocaleMessage("admin.rename_service_dialog.err4.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                } else {
                    flag = false;
                }
            }
            service.setName(serviceName);
        }
    }

    /**
     * Из привязок к услугам всех юзеров убрать привязку к данной услуге и всех ее вложенных.
     * @param service удаляемая услуга
     */
    private void deleteServicesFromUsers(QService service) {

        QServiceTree.sailToStorm(service, new ISailListener() {

            @Override
            public void actionPerformed(TreeNode service) {
                deleteServiceFromUsers((QService) service);
            }
        });
    }

    /**
     * Из привязок к услугам всех юзеров убрать привязку к данной услуге.
     * @param service удаляемая услуга
     */
    private void deleteServiceFromUsers(QService service) {
        for (QUser user : QUserList.getInstance().getItems()) {
            if (user.hasService(service.getId())) {
                user.deletePlanService(service.getId());
            }
        }
    }

    @Action
    public void deleteService() {
        final QService service = (QService) treeServices.getLastSelectedPathComponent();
        if (service != null && !service.isRoot()) {
            if (JOptionPane.showConfirmDialog(this,
                    getLocaleMessage("admin.remove_service_dialog.title") + " " + (service.isLeaf() ? getLocaleMessage("admin.remove_service_dialog.title_1") : getLocaleMessage("admin.remove_service_dialog.title_2")) + "\n\"" + (service.getName().length() > 85 ? service.getName().substring(0, 85) + " ..." : service.getName()) + "\"?",
                    getLocaleMessage("admin.remove_service_dialog.caption"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
            // Удалим эту услугу привязанную у пользователей
            deleteServicesFromUsers(service);
            // Удалим саму услугу
            final int del = service.getParent().getIndex(service);
            final int col = service.getParent().getChildCount();
            ((QServiceTree) treeServices.getModel()).removeNodeFromParent(service);
            // Выделение в услуги в дереве
            if (col == 1) {
                treeServices.setSelectionPath(new TreePath(((QServiceTree) treeServices.getModel()).getPathToRoot(service.getParent())));
            } else if (col == del + 1) {
                treeServices.setSelectionPath(new TreePath(((QServiceTree) treeServices.getModel()).getPathToRoot(service.getParent().getChildAt(del - 1))));
            } else if (col > del + 1) {
                treeServices.setSelectionPath(new TreePath(((QServiceTree) treeServices.getModel()).getPathToRoot(service.getParent().getChildAt(del))));
            }
            QLog.l().logger().debug("Удалена услуга \"" + service.getName() + "\" из группы \"" + service.getParent().getName() + "\"");
        }
    }

    @Action
    public void addInfoItem() {
        // Запросим название инфоузла и если оно уникально и не пусто, то примем
        String infoName = getLocaleMessage("admin.add_info_dialog.info");
        boolean flag = true;
        while (flag) {
            infoName = (String) JOptionPane.showInputDialog(this, getLocaleMessage("admin.add_info_dialog.title"), getLocaleMessage("admin.add_info_dialog.caption"), 3, null, null, infoName);
            if (infoName == null) {
                return;
            }
            if ("".equals(infoName)) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_info_dialog.err1.title"), getLocaleMessage("admin.add_info_dialog.err1.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (infoName.indexOf('\"') != -1) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_info_dialog.err2.title"), getLocaleMessage("admin.add_info_dialog.err2.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (infoName.length() > 100) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_info_dialog.err3.title"), getLocaleMessage("admin.add_info_dialog.err3.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else {
                flag = false;
            }
        }
        // Созданим новую услугу и добавим ее в модель
        final QInfoItem newItem = new QInfoItem();
        newItem.setName(infoName);
        newItem.setHTMLText("<html><b><p align=center><span style='font-size:20.0pt;color:green'>" + infoName + "</span></b>");
        newItem.setTextPrint("");
        final QInfoItem parentItem = (QInfoItem) treeInfo.getLastSelectedPathComponent();
        ((QInfoTree) treeInfo.getModel()).insertNodeInto(newItem, parentItem, parentItem.getChildCount());
        final TreeNode[] nodes = ((QInfoTree) treeInfo.getModel()).getPathToRoot(newItem);
        final TreePath path = new TreePath(nodes);
        treeInfo.scrollPathToVisible(path);
        treeInfo.setSelectionPath(path);
        textFieldInfoItemName.setEnabled(true);
        textPaneInfoItem.setEnabled(true);
        textPaneInfoPrint.setEnabled(true);

        QLog.l().logger().debug("Добавлен инфоузел \"" + infoName + "\" в группу \"" + parentItem.getName() + "\"");
    }

    @Action
    public void deleteInfoItem() {
        final QInfoItem item = (QInfoItem) treeInfo.getLastSelectedPathComponent();
        if (item != null && !item.isRoot()) {
            if (JOptionPane.showConfirmDialog(this,
                    getLocaleMessage("admin.remove_info_dialog.title") + " " + (item.isLeaf() ? getLocaleMessage("admin.remove_info_dialog.title_1") : getLocaleMessage("admin.remove_info_dialog.title_2")) + "\"" + (item.getName().length() > 85 ? item.getName().substring(0, 85) + " ..." : item.getName()) + "\"?",
                    getLocaleMessage("admin.remove_info_dialog.caption"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
            // Удалим сам узел
            final int del = item.getParent().getIndex(item);
            final int col = item.getParent().getChildCount();
            QInfoTree.getInstance().removeNodeFromParent(item);
            // Выделение в узла в дереве
            if (col == 1) {
                treeInfo.setSelectionPath(new TreePath(((QInfoTree) treeInfo.getModel()).getPathToRoot(item.getParent())));
            } else if (col == del + 1) {
                treeInfo.setSelectionPath(new TreePath(((QInfoTree) treeInfo.getModel()).getPathToRoot(item.getParent().getChildAt(del - 1))));
            } else if (col > del + 1) {
                treeInfo.setSelectionPath(new TreePath(((QInfoTree) treeInfo.getModel()).getPathToRoot(item.getParent().getChildAt(del))));
            }
            QLog.l().logger().debug("Удален инфоузел \"" + item.getName() + "\" из группы \"" + item.getParent().getName() + "\"");
        }
    }

    /**
     * @see http://static.springsource.org/spring/docs/3.0.x/reference/transaction.html#transaction-programmatic
     */
    @Action
    public void saveConfiguration() {
        saveNet();
        final Exception res;
        try {
            res = (Exception) Spring.getInstance().getTt().execute(new TransactionCallback() {

                @Override
                public Exception doInTransaction(TransactionStatus status) {
                    try {
                        //Сохраняем сетевые настройки
                        Spring.getInstance().getHt().saveOrUpdate(ServerProps.getInstance().getProps());
                        // Сохраняем планы расписания
                        QScheduleList.getInstance().save();
                        // Сохраняем календари услуг
                        QCalendarList.getInstance().save();
                        // Сохраняем услуги
                        QServiceTree.getInstance().save();
                        // Сохраняем пользователей
                        QUserList.getInstance().save();
                        // Сохраняем инфоузлы
                        QInfoTree.getInstance().save();
                        // Сохраняем отзывы
                        QResponseList.getInstance().save();
                        // Сохраняем результаты работы пользователя с клиентами
                        QResultList.getInstance().save();
                        QLog.l().logger().debug("Сохранили конфигурацию.");
                    } catch (Exception ex) {
                        QLog.l().logger().error("Ошибка при сохранении \n" + ex.toString() + "\n" + ex.getStackTrace());
                        status.setRollbackOnly();
                        return ex;
                    }
                    return null;
                }
            });
        } catch (RuntimeException ex) {
            throw new ClientException("Ошибка выполнения операции изменения данных в БД(JDBC). Возможно введенные вами параметры не могут быть сохранены.\n(" + ex.toString() + ")");
        }
        if (res == null) {
            JOptionPane.showMessageDialog(this, getLocaleMessage("admin.save.title"), getLocaleMessage("admin.save.caption"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            throw new ClientException("Ошибка выполнения операции изменения данных в БД(JDBC). Возможно введенные вами параметры не могут быть сохранены.\n[" + res.getLocalizedMessage() + "]\n(" + res.toString() + ")\nSQL: ");
        }
    }

    @Action
    public void addServiceToUser() {
        final QUser user = (QUser) listUsers.getSelectedValue();
        final QService service = (QService) treeServices.getLastSelectedPathComponent();
        if (service != null && service.isLeaf() && listUsers.getSelectedIndex() != -1 && !(user.hasService(service))) {
            user.addPlanService(service);
            if (listUserService.getLastVisibleIndex() != -1) {
                listUserService.setSelectedIndex(listUserService.getLastVisibleIndex());
                QLog.l().logger().debug("Пользователю \"" + user.getName() + "\" назначили услугу \"" + service.getName() + "\".");
            }
        }
        if (service != null && !service.isLeaf() && listUsers.getSelectedIndex() != -1 && !(user.hasService(service))) {
            QServiceTree.sailToStorm(service, new ISailListener() {

                @Override
                public void actionPerformed(TreeNode service) {
                    if (service.isLeaf() && !user.hasService((QService) service)) {
                        user.addPlanService((QService) service);
                        QLog.l().logger().debug("Пользователю \"" + ((QUser) listUsers.getSelectedValue()).getName() + "\" назначили услугу \"" + ((QService) service).getName() + "\".");
                    }
                }
            });
            if (listUserService.getLastVisibleIndex() != -1) {
                listUserService.setSelectedIndex(listUserService.getLastVisibleIndex());
            }
        }
    }

    @Action
    public void deleteServiseFromUser() {
        if (listUserService.getSelectedIndex() != -1) {
            if (JOptionPane.showConfirmDialog(this,
                    getLocaleMessage("admin.remove_service_from_user.title") + " \"" + listUserService.getSelectedValue().toString() + "\" " + getLocaleMessage("admin.remove_service_from_user.title_1") + " \"" + listUsers.getSelectedValue().toString() + "\"?",
                    getLocaleMessage("admin.remove_service_from_user.caption"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
            final int ind = listUserService.getSelectedIndex();
            ((QUser) listUsers.getSelectedValue()).deletePlanService(((QPlanService) listUserService.getSelectedValue()).getService().getId());
            if (listUserService.getLastVisibleIndex() != -1) {
                listUserService.setSelectedIndex(listUserService.getLastVisibleIndex() < ind ? listUserService.getLastVisibleIndex() : ind);
            }
        }
    }

    @Action
    public void getTicket() {
        final QService service = (QService) treeServices.getLastSelectedPathComponent();
        if (service != null && service.isLeaf()) {
            //Если услуга требует ввода данных пользователем, то нужно получить эти данные из диалога ввода
            String inputData = null;
            if (service.getInput_required()) {
                inputData = FInputDialog.showInputDialog(this, true, FWelcome.netProperty, false, WelcomeParams.getInstance().delayBack, service.getInput_caption());
                if (inputData == null || inputData.isEmpty()) {
                    return;
                }
            }

            final QCustomer customer;
            try {
                customer = NetCommander.standInService(new ServerNetProperty(), service.getId(), "1", 1, inputData);
            } catch (Exception ex) {
                throw new ClientException(getLocaleMessage("admin.print_ticket_error") + " " + ex);
            }
            FWelcome.printTicket(customer, ((QService) treeServices.getModel().getRoot()).getName());
            String pref = customer.getPrefix();
            pref = "".equals(pref) ? "" : pref + "-";
            JOptionPane.showMessageDialog(this, getLocaleMessage("admin.print_ticket.title") + " \"" + service.getName() + "\". " + getLocaleMessage("admin.print_ticket.title_1") + " \"" + pref + customer.getNumber() + "\".", getLocaleMessage("admin.print_ticket.captionru"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroupSource = new javax.swing.ButtonGroup();
        popupUser = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem20 = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        jMenuItem10 = new javax.swing.JMenuItem();
        popupServices = new javax.swing.JPopupMenu();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItem21 = new javax.swing.JMenuItem();
        jMenuItem22 = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        jMenuItem12 = new javax.swing.JMenuItem();
        jMenuItem16 = new javax.swing.JMenuItem();
        jMenuItem24 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        jMenuItem13 = new javax.swing.JMenuItem();
        popupServiceUser = new javax.swing.JPopupMenu();
        jMenuItem14 = new javax.swing.JMenuItem();
        jMenuItem38 = new javax.swing.JMenuItem();
        jMenuItem17 = new javax.swing.JMenuItem();
        jMenuItem18 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        jMenuItem15 = new javax.swing.JMenuItem();
        popupInfo = new javax.swing.JPopupMenu();
        jMenuItem26 = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        jMenuItem27 = new javax.swing.JMenuItem();
        popupResponse = new javax.swing.JPopupMenu();
        jMenuItem28 = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        jMenuItem29 = new javax.swing.JMenuItem();
        popupResults = new javax.swing.JPopupMenu();
        jMenuItem30 = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        jMenuItem31 = new javax.swing.JMenuItem();
        popupPlans = new javax.swing.JPopupMenu();
        jMenuItem32 = new javax.swing.JMenuItem();
        jMenuItem33 = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        jMenuItem34 = new javax.swing.JMenuItem();
        popupCalendar = new javax.swing.JPopupMenu();
        jMenuItem35 = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        jMenuItem36 = new javax.swing.JMenuItem();
        buttonGroupKindNum = new javax.swing.ButtonGroup();
        buttonGroupPoint = new javax.swing.ButtonGroup();
        buttonGroupVoice = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        tabbedPaneMain = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        textFieldClientAdress = new javax.swing.JTextField();
        spinnerClientPort = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        labelWelcomeState = new javax.swing.JLabel();
        checkBoxClientAuto = new javax.swing.JCheckBox();
        buttonClientRequest = new javax.swing.JButton();
        buttonLock = new javax.swing.JButton();
        buttonUnlock = new javax.swing.JButton();
        buttonRestart = new javax.swing.JButton();
        buttonShutDown = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        textFieldServerAddr = new javax.swing.JTextField();
        spinnerServerPort = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        checkBoxServerAuto = new javax.swing.JCheckBox();
        buttonServerRequest = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        labelServerState = new javax.swing.JLabel();
        buttonRestartServer = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        listPostponed = new javax.swing.JList();
        buttonResetMainTablo = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel25 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        treeServices = new javax.swing.JTree();
        jButton5 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel26 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        listUserService = new javax.swing.JList();
        jButton6 = new javax.swing.JButton();
        jSplitPane3 = new javax.swing.JSplitPane();
        jPanel11 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        textFieldUserName = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        textFieldUserIdent = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        spinnerUserRS = new javax.swing.JSpinner();
        jLabel20 = new javax.swing.JLabel();
        passwordFieldUser = new javax.swing.JPasswordField();
        checkBoxReport = new javax.swing.JCheckBox();
        checkBoxAdmin = new javax.swing.JCheckBox();
        jPanel27 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listUsers = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane18 = new javax.swing.JScrollPane();
        labelServiceInfo = new javax.swing.JLabel();
        jScrollPane19 = new javax.swing.JScrollPane();
        jScrollPane6 = new javax.swing.JScrollPane();
        labelButtonCaption = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        jScrollPane12 = new javax.swing.JScrollPane();
        listSchedule = new javax.swing.JList();
        jLabel21 = new javax.swing.JLabel();
        textFieldScheduleName = new javax.swing.JTextField();
        buttonScheduleAdd = new javax.swing.JButton();
        buttonSchedulleDelete = new javax.swing.JButton();
        labelSchedule = new javax.swing.JLabel();
        jPanel19 = new javax.swing.JPanel();
        jScrollPane14 = new javax.swing.JScrollPane();
        listCalendar = new javax.swing.JList();
        jScrollPane15 = new javax.swing.JScrollPane();
        tableCalendar = new javax.swing.JTable();
        jLabel23 = new javax.swing.JLabel();
        textFieldCalendarName = new javax.swing.JTextField();
        buttonAddCalendar = new javax.swing.JButton();
        buttonDeleteCalendar = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        spinnerPropServerPort = new javax.swing.JSpinner();
        spinnerWebServerPort = new javax.swing.JSpinner();
        spinnerPropClientPort = new javax.swing.JSpinner();
        jPanel10 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        textFieldStartTime = new javax.swing.JTextField();
        textFieldFinishTime = new javax.swing.JTextField();
        jPanel16 = new javax.swing.JPanel();
        spinnerFirstNumber = new javax.swing.JSpinner();
        spinnerLastNumber = new javax.swing.JSpinner();
        jPanel20 = new javax.swing.JPanel();
        rbPointOffice = new javax.swing.JRadioButton();
        rbPointWindow = new javax.swing.JRadioButton();
        rbPointStoika = new javax.swing.JRadioButton();
        jPanel21 = new javax.swing.JPanel();
        rbNotificationNo = new javax.swing.JRadioButton();
        rbNotificationGong = new javax.swing.JRadioButton();
        rbNotificationGongVoice = new javax.swing.JRadioButton();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jPanel22 = new javax.swing.JPanel();
        rbKindPersonal = new javax.swing.JRadioButton();
        rbKindCommon = new javax.swing.JRadioButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        listReposts = new javax.swing.JList();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel23 = new javax.swing.JPanel();
        textFieldURLWebService = new javax.swing.JTextField();
        spinnerBranchId = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        buttonCloudTest = new javax.swing.JButton();
        buttonSendDataToSky = new javax.swing.JButton();
        jPanel24 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        textFieldZonBoadrServAddr = new javax.swing.JTextField();
        spinnerZonBoadrServPort = new javax.swing.JSpinner();
        buttonCheckZoneBoardServ = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        treeInfo = new javax.swing.JTree();
        jScrollPane9 = new javax.swing.JScrollPane();
        textPaneInfoItem = new javax.swing.JTextPane();
        jPanel14 = new javax.swing.JPanel();
        jScrollPane17 = new javax.swing.JScrollPane();
        labelInfoItem = new javax.swing.JLabel();
        textFieldInfoItemName = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jScrollPane16 = new javax.swing.JScrollPane();
        textPaneInfoPrint = new javax.swing.JTextPane();
        jPanel13 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        listResponse = new javax.swing.JList();
        jScrollPane11 = new javax.swing.JScrollPane();
        textPaneResponse = new javax.swing.JTextPane();
        jPanel15 = new javax.swing.JPanel();
        labelRespinse = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        textFieldResponse = new javax.swing.JTextField();
        jPanel18 = new javax.swing.JPanel();
        jScrollPane13 = new javax.swing.JScrollPane();
        listResults = new javax.swing.JList();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        menuLangs = new javax.swing.JMenu();
        jSeparator15 = new javax.swing.JPopupMenu.Separator();
        jMenuItem25 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem19 = new javax.swing.JMenuItem();
        menuUsers = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JPopupMenu.Separator();
        jMenuItem37 = new javax.swing.JMenuItem();
        menuServices = new javax.swing.JMenu();
        jMenuItem7 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem23 = new javax.swing.JMenuItem();
        menuAbout = new javax.swing.JMenu();
        menuItemHelp = new javax.swing.JMenuItem();
        menuItemAbout = new javax.swing.JMenuItem();

        popupUser.setName("popupUser"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getActionMap(FAdmin.class, this);
        jMenuItem1.setAction(actionMap.get("addUser")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        popupUser.add(jMenuItem1);

        jMenuItem20.setAction(actionMap.get("renameUser")); // NOI18N
        jMenuItem20.setName("jMenuItem20"); // NOI18N
        popupUser.add(jMenuItem20);

        jSeparator7.setName("jSeparator7"); // NOI18N
        popupUser.add(jSeparator7);

        jMenuItem10.setAction(actionMap.get("deleteUser")); // NOI18N
        jMenuItem10.setName("jMenuItem10"); // NOI18N
        popupUser.add(jMenuItem10);

        popupServices.setComponentPopupMenu(popupServices);
        popupServices.setName("popupServices"); // NOI18N

        jMenuItem11.setAction(actionMap.get("addService")); // NOI18N
        jMenuItem11.setName("jMenuItem11"); // NOI18N
        popupServices.add(jMenuItem11);

        jMenuItem21.setAction(actionMap.get("renameService")); // NOI18N
        jMenuItem21.setName("jMenuItem21"); // NOI18N
        popupServices.add(jMenuItem21);

        jMenuItem22.setAction(actionMap.get("editService")); // NOI18N
        jMenuItem22.setName("jMenuItem22"); // NOI18N
        popupServices.add(jMenuItem22);

        jSeparator8.setName("jSeparator8"); // NOI18N
        popupServices.add(jSeparator8);

        jMenuItem12.setAction(actionMap.get("addServiceToUser")); // NOI18N
        jMenuItem12.setName("jMenuItem12"); // NOI18N
        popupServices.add(jMenuItem12);

        jMenuItem16.setAction(actionMap.get("getTicket")); // NOI18N
        jMenuItem16.setName("jMenuItem16"); // NOI18N
        popupServices.add(jMenuItem16);

        jMenuItem24.setAction(actionMap.get("standAdvance")); // NOI18N
        jMenuItem24.setName("jMenuItem24"); // NOI18N
        popupServices.add(jMenuItem24);

        jSeparator5.setName("jSeparator5"); // NOI18N
        popupServices.add(jSeparator5);

        jMenuItem13.setAction(actionMap.get("deleteService")); // NOI18N
        jMenuItem13.setName("jMenuItem13"); // NOI18N
        popupServices.add(jMenuItem13);

        popupServiceUser.setName("popupServiceUser"); // NOI18N

        jMenuItem14.setAction(actionMap.get("changeServicePriority")); // NOI18N
        jMenuItem14.setName("jMenuItem14"); // NOI18N
        popupServiceUser.add(jMenuItem14);

        jMenuItem38.setAction(actionMap.get("changeFlexiblePriorityAbility")); // NOI18N
        jMenuItem38.setName("jMenuItem38"); // NOI18N
        popupServiceUser.add(jMenuItem38);

        jMenuItem17.setAction(actionMap.get("setUpdateServiceFire")); // NOI18N
        jMenuItem17.setName("jMenuItem17"); // NOI18N
        popupServiceUser.add(jMenuItem17);

        jMenuItem18.setAction(actionMap.get("deleteUpdateServiceFire")); // NOI18N
        jMenuItem18.setName("jMenuItem18"); // NOI18N
        popupServiceUser.add(jMenuItem18);

        jSeparator6.setName("jSeparator6"); // NOI18N
        popupServiceUser.add(jSeparator6);

        jMenuItem15.setAction(actionMap.get("deleteServiseFromUser")); // NOI18N
        jMenuItem15.setName("jMenuItem15"); // NOI18N
        popupServiceUser.add(jMenuItem15);

        popupInfo.setName("popupInfo"); // NOI18N

        jMenuItem26.setAction(actionMap.get("addInfoItem")); // NOI18N
        jMenuItem26.setName("jMenuItem26"); // NOI18N
        popupInfo.add(jMenuItem26);

        jSeparator9.setName("jSeparator9"); // NOI18N
        popupInfo.add(jSeparator9);

        jMenuItem27.setAction(actionMap.get("deleteInfoItem")); // NOI18N
        jMenuItem27.setName("jMenuItem27"); // NOI18N
        popupInfo.add(jMenuItem27);

        popupResponse.setName("popupResponse"); // NOI18N

        jMenuItem28.setAction(actionMap.get("addRespItem")); // NOI18N
        jMenuItem28.setName("jMenuItem28"); // NOI18N
        popupResponse.add(jMenuItem28);

        jSeparator10.setName("jSeparator10"); // NOI18N
        popupResponse.add(jSeparator10);

        jMenuItem29.setAction(actionMap.get("deleteRespItem")); // NOI18N
        jMenuItem29.setName("jMenuItem29"); // NOI18N
        popupResponse.add(jMenuItem29);

        popupResults.setName("popupResults"); // NOI18N

        jMenuItem30.setAction(actionMap.get("addResult")); // NOI18N
        jMenuItem30.setName("jMenuItem30"); // NOI18N
        popupResults.add(jMenuItem30);

        jSeparator11.setName("jSeparator11"); // NOI18N
        popupResults.add(jSeparator11);

        jMenuItem31.setAction(actionMap.get("deleteResult")); // NOI18N
        jMenuItem31.setName("jMenuItem31"); // NOI18N
        popupResults.add(jMenuItem31);

        popupPlans.setName("popupPlans"); // NOI18N

        jMenuItem32.setAction(actionMap.get("addSchedule")); // NOI18N
        jMenuItem32.setName("jMenuItem32"); // NOI18N
        popupPlans.add(jMenuItem32);

        jMenuItem33.setAction(actionMap.get("editSchedule")); // NOI18N
        jMenuItem33.setName("jMenuItem33"); // NOI18N
        popupPlans.add(jMenuItem33);

        jSeparator12.setName("jSeparator12"); // NOI18N
        popupPlans.add(jSeparator12);

        jMenuItem34.setAction(actionMap.get("deleteSchedule")); // NOI18N
        jMenuItem34.setName("jMenuItem34"); // NOI18N
        popupPlans.add(jMenuItem34);

        popupCalendar.setName("popupCalendar"); // NOI18N

        jMenuItem35.setAction(actionMap.get("addCalendar")); // NOI18N
        jMenuItem35.setName("jMenuItem35"); // NOI18N
        popupCalendar.add(jMenuItem35);

        jSeparator13.setName("jSeparator13"); // NOI18N
        popupCalendar.add(jSeparator13);

        jMenuItem36.setAction(actionMap.get("deleteCalendar")); // NOI18N
        jMenuItem36.setName("jMenuItem36"); // NOI18N
        popupCalendar.add(jMenuItem36);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getResourceMap(FAdmin.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        jPanel1.setBackground(resourceMap.getColor("jPanel1.background")); // NOI18N
        jPanel1.setBorder(new javax.swing.border.MatteBorder(null));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        tabbedPaneMain.setName("tabbedPaneMain"); // NOI18N
        tabbedPaneMain.setPreferredSize(new java.awt.Dimension(1050, 550));
        tabbedPaneMain.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneMainStateChanged(evt);
            }
        });
        tabbedPaneMain.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tabbedPaneMainFocusLost(evt);
            }
        });

        jPanel3.setBorder(new javax.swing.border.MatteBorder(null));
        jPanel3.setAutoscrolls(true);
        jPanel3.setName("jPanel3"); // NOI18N

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel5.border.title"))); // NOI18N
        jPanel5.setName("jPanel5"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        textFieldClientAdress.setText(resourceMap.getString("textFieldClientAdress.text")); // NOI18N
        textFieldClientAdress.setName("textFieldClientAdress"); // NOI18N
        textFieldClientAdress.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                textFieldClientAdressFocusLost(evt);
            }
        });

        spinnerClientPort.setName("spinnerClientPort"); // NOI18N
        spinnerClientPort.setValue(3128);

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        labelWelcomeState.setText(resourceMap.getString("labelWelcomeState.text")); // NOI18N
        labelWelcomeState.setName("labelWelcomeState"); // NOI18N

        checkBoxClientAuto.setText(resourceMap.getString("checkBoxClientAuto.text")); // NOI18N
        checkBoxClientAuto.setName("checkBoxClientAuto"); // NOI18N
        checkBoxClientAuto.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                checkBoxClientAutoStateChanged(evt);
            }
        });
        checkBoxClientAuto.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                checkBoxClientAutoFocusLost(evt);
            }
        });

        buttonClientRequest.setText(resourceMap.getString("buttonClientRequest.text")); // NOI18N
        buttonClientRequest.setName("buttonClientRequest"); // NOI18N
        buttonClientRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonClientRequestActionPerformed(evt);
            }
        });

        buttonLock.setText(resourceMap.getString("buttonLock.text")); // NOI18N
        buttonLock.setName("buttonLock"); // NOI18N
        buttonLock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLockActionPerformed(evt);
            }
        });

        buttonUnlock.setText(resourceMap.getString("buttonUnlock.text")); // NOI18N
        buttonUnlock.setName("buttonUnlock"); // NOI18N
        buttonUnlock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonUnlockActionPerformed(evt);
            }
        });

        buttonRestart.setText(resourceMap.getString("buttonRestart.text")); // NOI18N
        buttonRestart.setName("buttonRestart"); // NOI18N
        buttonRestart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRestartActionPerformed(evt);
            }
        });

        buttonShutDown.setText(resourceMap.getString("buttonShutDown.text")); // NOI18N
        buttonShutDown.setName("buttonShutDown"); // NOI18N
        buttonShutDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonShutDownActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(spinnerClientPort)
                                    .addComponent(textFieldClientAdress, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(checkBoxClientAuto)
                            .addComponent(buttonClientRequest))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(buttonUnlock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonRestart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonShutDown, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonLock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(labelWelcomeState, javax.swing.GroupLayout.DEFAULT_SIZE, 922, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(textFieldClientAdress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spinnerClientPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBoxClientAuto)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonClientRequest))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(buttonLock)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonUnlock)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonRestart)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonShutDown)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelWelcomeState, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel6.border.title"))); // NOI18N
        jPanel6.setName("jPanel6"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        textFieldServerAddr.setText(resourceMap.getString("textFieldServerAddr.text")); // NOI18N
        textFieldServerAddr.setName("textFieldServerAddr"); // NOI18N
        textFieldServerAddr.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                textFieldServerAddrFocusLost(evt);
            }
        });

        spinnerServerPort.setName("spinnerServerPort"); // NOI18N
        spinnerServerPort.setValue(3128);

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        checkBoxServerAuto.setText(resourceMap.getString("checkBoxServerAuto.text")); // NOI18N
        checkBoxServerAuto.setName("checkBoxServerAuto"); // NOI18N
        checkBoxServerAuto.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                checkBoxServerAutoStateChanged(evt);
            }
        });
        checkBoxServerAuto.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                checkBoxServerAutoFocusLost(evt);
            }
        });

        buttonServerRequest.setText(resourceMap.getString("buttonServerRequest.text")); // NOI18N
        buttonServerRequest.setName("buttonServerRequest"); // NOI18N
        buttonServerRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonServerRequestActionPerformed(evt);
            }
        });

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Информация о состоянии сервера"));
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        labelServerState.setText(resourceMap.getString("labelServerState.text")); // NOI18N
        labelServerState.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelServerState.setName("labelServerState"); // NOI18N
        jScrollPane2.setViewportView(labelServerState);

        buttonRestartServer.setText(resourceMap.getString("buttonRestartServer.text")); // NOI18N
        buttonRestartServer.setName("buttonRestartServer"); // NOI18N
        buttonRestartServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRestartServerActionPerformed(evt);
            }
        });

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        listPostponed.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("listPostponed.border.title"))); // NOI18N
        listPostponed.setName("listPostponed"); // NOI18N
        jScrollPane5.setViewportView(listPostponed);

        buttonResetMainTablo.setText(resourceMap.getString("buttonResetMainTablo.text")); // NOI18N
        buttonResetMainTablo.setName("buttonResetMainTablo"); // NOI18N
        buttonResetMainTablo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetMainTabloActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(textFieldServerAddr, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(spinnerServerPort)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxServerAuto)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(buttonServerRequest)
                                .addGap(18, 18, 18)
                                .addComponent(buttonRestartServer)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(buttonResetMainTablo)))
                        .addContainerGap(301, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 689, Short.MAX_VALUE))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(textFieldServerAddr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxServerAuto))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(spinnerServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonServerRequest)
                    .addComponent(buttonRestartServer)
                    .addComponent(buttonResetMainTablo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabbedPaneMain.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jPanel4.setBorder(new javax.swing.border.MatteBorder(null));
        jPanel4.setAutoscrolls(true);
        jPanel4.setName("jPanel4"); // NOI18N

        jSplitPane1.setDividerLocation(380);
        jSplitPane1.setContinuousLayout(true);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jSplitPane2.setDividerLocation(210);
        jSplitPane2.setContinuousLayout(true);
        jSplitPane2.setName("jSplitPane2"); // NOI18N

        jPanel25.setName("jPanel25"); // NOI18N

        jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jScrollPane3.border.title"))); // NOI18N
        jScrollPane3.setName("jScrollPane3"); // NOI18N

        treeServices.setModel(null);
        treeServices.setAutoscrolls(true);
        treeServices.setComponentPopupMenu(popupServices);
        treeServices.setName("treeServices"); // NOI18N
        treeServices.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeServicesMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(treeServices);

        jButton5.setAction(actionMap.get("addServiceToUser")); // NOI18N
        jButton5.setName("jButton5"); // NOI18N

        jButton3.setAction(actionMap.get("deleteService")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N

        jButton4.setAction(actionMap.get("addService")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel25Layout.createSequentialGroup()
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3))
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel25Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5)
                    .addComponent(jButton3)
                    .addComponent(jButton4)))
        );

        jSplitPane2.setRightComponent(jPanel25);

        jPanel26.setName("jPanel26"); // NOI18N

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jScrollPane4.border.title"))); // NOI18N
        jScrollPane4.setName("jScrollPane4"); // NOI18N

        listUserService.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listUserService.setComponentPopupMenu(popupServiceUser);
        listUserService.setName("listUserService"); // NOI18N
        listUserService.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listUserServiceMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(listUserService);

        jButton6.setAction(actionMap.get("deleteServiseFromUser")); // NOI18N
        jButton6.setName("jButton6"); // NOI18N

        javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
        jPanel26.setLayout(jPanel26Layout);
        jPanel26Layout.setHorizontalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel26Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton6)
                .addGap(10, 10, 10))
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
        );
        jPanel26Layout.setVerticalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel26Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6))
        );

        jSplitPane2.setLeftComponent(jPanel26);

        jSplitPane1.setRightComponent(jSplitPane2);

        jSplitPane3.setDividerLocation(170);
        jSplitPane3.setContinuousLayout(true);
        jSplitPane3.setName("jSplitPane3"); // NOI18N
        jSplitPane3.setPreferredSize(new java.awt.Dimension(40, 25));

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Свойства пользователя"));
        jPanel11.setMinimumSize(new java.awt.Dimension(5, 5));
        jPanel11.setName("jPanel11"); // NOI18N

        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        textFieldUserName.setEditable(false);
        textFieldUserName.setText(resourceMap.getString("textFieldUserName.text")); // NOI18N
        textFieldUserName.setName("textFieldUserName"); // NOI18N

        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        textFieldUserIdent.setText(resourceMap.getString("textFieldUserIdent.text")); // NOI18N
        textFieldUserIdent.setName("textFieldUserIdent"); // NOI18N
        textFieldUserIdent.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textFieldUserIdentKeyReleased(evt);
            }
        });

        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        spinnerUserRS.setEditor(new javax.swing.JSpinner.NumberEditor(spinnerUserRS, ""));
        spinnerUserRS.setName("spinnerUserRS"); // NOI18N

        jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N

        passwordFieldUser.setText(resourceMap.getString("passwordFieldUser.text")); // NOI18N
        passwordFieldUser.setName("passwordFieldUser"); // NOI18N
        passwordFieldUser.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                passwordFieldUserKeyReleased(evt);
            }
        });

        checkBoxReport.setText(resourceMap.getString("checkBoxReport.text")); // NOI18N
        checkBoxReport.setName("checkBoxReport"); // NOI18N
        checkBoxReport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                checkBoxReportMouseClicked(evt);
            }
        });

        checkBoxAdmin.setText(resourceMap.getString("checkBoxAdmin.text")); // NOI18N
        checkBoxAdmin.setName("checkBoxAdmin"); // NOI18N
        checkBoxAdmin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                checkBoxAdminMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(checkBoxAdmin)
                        .addContainerGap())
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(checkBoxReport)
                        .addContainerGap())
                    .addComponent(jLabel17)
                    .addComponent(jLabel20)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                        .addGap(86, 86, 86))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addContainerGap(65, Short.MAX_VALUE))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(textFieldUserIdent, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
                            .addComponent(spinnerUserRS, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE))
                        .addGap(72, 72, 72))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(passwordFieldUser, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                            .addComponent(textFieldUserName, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textFieldUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(passwordFieldUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxAdmin)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxReport)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textFieldUserIdent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spinnerUserRS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(121, Short.MAX_VALUE))
        );

        jSplitPane3.setLeftComponent(jPanel11);

        jPanel27.setName("jPanel27"); // NOI18N

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jScrollPane1.border.title"))); // NOI18N
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        listUsers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listUsers.setComponentPopupMenu(popupUser);
        listUsers.setName("listUsers"); // NOI18N
        jScrollPane1.setViewportView(listUsers);

        jButton1.setAction(actionMap.get("addUser")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jButton2.setAction(actionMap.get("deleteUser")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                .addComponent(jButton2))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel27Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)))
        );

        jSplitPane3.setRightComponent(jPanel27);

        jSplitPane1.setLeftComponent(jSplitPane3);

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jScrollPane18.setName("jScrollPane18"); // NOI18N

        labelServiceInfo.setText(resourceMap.getString("labelServiceInfo.text")); // NOI18N
        labelServiceInfo.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelServiceInfo.setName("labelServiceInfo"); // NOI18N
        jScrollPane18.setViewportView(labelServiceInfo);

        jTabbedPane1.addTab(resourceMap.getString("jScrollPane18.TabConstraints.tabTitle"), jScrollPane18); // NOI18N

        jScrollPane19.setName("jScrollPane19"); // NOI18N

        jScrollPane6.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jScrollPane6.border.title"))); // NOI18N
        jScrollPane6.setName("jScrollPane6"); // NOI18N

        labelButtonCaption.setText(resourceMap.getString("labelButtonCaption.text")); // NOI18N
        labelButtonCaption.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelButtonCaption.setName("labelButtonCaption"); // NOI18N
        jScrollPane6.setViewportView(labelButtonCaption);

        jScrollPane19.setViewportView(jScrollPane6);

        jTabbedPane1.addTab(resourceMap.getString("jScrollPane19.TabConstraints.tabTitle"), jScrollPane19); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 954, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 954, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName(resourceMap.getString("jTabbedPane1.AccessibleContext.accessibleName")); // NOI18N

        tabbedPaneMain.addTab(resourceMap.getString("jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        jPanel17.setAutoscrolls(true);
        jPanel17.setName("jPanel17"); // NOI18N

        jScrollPane12.setName("jScrollPane12"); // NOI18N

        listSchedule.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("listSchedule.border.title"))); // NOI18N
        listSchedule.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        listSchedule.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listSchedule.setComponentPopupMenu(popupPlans);
        listSchedule.setName("listSchedule"); // NOI18N
        listSchedule.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listScheduleMouseClicked(evt);
            }
        });
        jScrollPane12.setViewportView(listSchedule);
        listSchedule.getAccessibleContext().setAccessibleName(resourceMap.getString("jList1.AccessibleContext.accessibleName")); // NOI18N

        jLabel21.setText(resourceMap.getString("jLabel21.text")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N

        textFieldScheduleName.setText(resourceMap.getString("textFieldScheduleName.text")); // NOI18N
        textFieldScheduleName.setName("textFieldScheduleName"); // NOI18N
        textFieldScheduleName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textFieldScheduleNameKeyReleased(evt);
            }
        });

        buttonScheduleAdd.setAction(actionMap.get("addSchedule")); // NOI18N
        buttonScheduleAdd.setText(resourceMap.getString("buttonScheduleAdd.text")); // NOI18N
        buttonScheduleAdd.setName("buttonScheduleAdd"); // NOI18N

        buttonSchedulleDelete.setAction(actionMap.get("deleteSchedule")); // NOI18N
        buttonSchedulleDelete.setText(resourceMap.getString("buttonSchedulleDelete.text")); // NOI18N
        buttonSchedulleDelete.setName("buttonSchedulleDelete"); // NOI18N

        labelSchedule.setText(resourceMap.getString("labelSchedule.text")); // NOI18N
        labelSchedule.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelSchedule.setName("labelSchedule"); // NOI18N

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labelSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)
                            .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textFieldScheduleName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)))
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(buttonScheduleAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSchedulleDelete)))
                .addContainerGap())
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFieldScheduleName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE))
                    .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonScheduleAdd)
                    .addComponent(buttonSchedulleDelete))
                .addContainerGap())
        );

        tabbedPaneMain.addTab(resourceMap.getString("jPanel17.TabConstraints.tabTitle"), jPanel17); // NOI18N

        jPanel19.setAutoscrolls(true);
        jPanel19.setName("jPanel19"); // NOI18N

        jScrollPane14.setName("jScrollPane14"); // NOI18N

        listCalendar.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Общий календарь", " " };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        listCalendar.setComponentPopupMenu(popupCalendar);
        listCalendar.setName("listCalendar"); // NOI18N
        jScrollPane14.setViewportView(listCalendar);

        jScrollPane15.setName("jScrollPane15"); // NOI18N

        tableCalendar.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Январь", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Февраль", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Март", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Апрель", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Май", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Июнь", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Июль", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Август", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Сентябрь", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Октябрь", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Ноябрь", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {"Декабрь", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                " ", " 1", " 2", " 3", " 4", " 5", " 6", " 7", " 8", " 9", " 10", " 11", " 12", " 13", " 14", " 15", " 16", " 17", " 18", " 19", " 20", " 21", " 22", " 23", " 24", " 25", " 26", " 27", " 28", " 29", " 30", "31"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableCalendar.setCellSelectionEnabled(true);
        tableCalendar.setName("tableCalendar"); // NOI18N
        tableCalendar.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tableCalendar.getTableHeader().setReorderingAllowed(false);
        tableCalendar.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tableCalendarFocusGained(evt);
            }
        });
        jScrollPane15.setViewportView(tableCalendar);

        jLabel23.setText(resourceMap.getString("jLabel23.text")); // NOI18N
        jLabel23.setName("jLabel23"); // NOI18N

        textFieldCalendarName.setText(resourceMap.getString("textFieldCalendarName.text")); // NOI18N
        textFieldCalendarName.setName("textFieldCalendarName"); // NOI18N
        textFieldCalendarName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textFieldCalendarNameKeyReleased(evt);
            }
        });

        buttonAddCalendar.setAction(actionMap.get("addCalendar")); // NOI18N
        buttonAddCalendar.setText(resourceMap.getString("buttonAddCalendar.text")); // NOI18N
        buttonAddCalendar.setName("buttonAddCalendar"); // NOI18N

        buttonDeleteCalendar.setAction(actionMap.get("deleteCalendar")); // NOI18N
        buttonDeleteCalendar.setText(resourceMap.getString("buttonDeleteCalendar.text")); // NOI18N
        buttonDeleteCalendar.setName("buttonDeleteCalendar"); // NOI18N

        jButton18.setAction(actionMap.get("checkSundays")); // NOI18N
        jButton18.setName("jButton18"); // NOI18N

        jButton16.setAction(actionMap.get("checkSaturday")); // NOI18N
        jButton16.setName("jButton16"); // NOI18N

        jButton17.setAction(actionMap.get("dropCalendarSelection")); // NOI18N
        jButton17.setText(resourceMap.getString("jButton17.text")); // NOI18N
        jButton17.setName("jButton17"); // NOI18N

        jButton15.setAction(actionMap.get("saveCalendar")); // NOI18N
        jButton15.setText(resourceMap.getString("jButton15.text")); // NOI18N
        jButton15.setName("jButton15"); // NOI18N

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addComponent(buttonAddCalendar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonDeleteCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel19Layout.createSequentialGroup()
                                .addComponent(jButton18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton16)
                                .addGap(18, 18, 18)
                                .addComponent(jButton17)
                                .addGap(18, 18, 18)
                                .addComponent(jButton15))
                            .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 735, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel19Layout.createSequentialGroup()
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(textFieldCalendarName, javax.swing.GroupLayout.DEFAULT_SIZE, 594, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel19Layout.createSequentialGroup()
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(textFieldCalendarName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton18)
                            .addComponent(jButton16)
                            .addComponent(jButton17)
                            .addComponent(jButton15)))
                    .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonAddCalendar)
                    .addComponent(buttonDeleteCalendar))
                .addContainerGap())
        );

        tabbedPaneMain.addTab(resourceMap.getString("jPanel19.TabConstraints.tabTitle"), jPanel19); // NOI18N

        jPanel8.setAutoscrolls(true);
        jPanel8.setName("jPanel8"); // NOI18N

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel9.border.title"))); // NOI18N
        jPanel9.setName("jPanel9"); // NOI18N
        jPanel9.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(19, 16, 0, 0);
        jPanel9.add(jLabel3, gridBagConstraints);

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 16, 0, 0);
        jPanel9.add(jLabel8, gridBagConstraints);

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 16, 0, 0);
        jPanel9.add(jLabel9, gridBagConstraints);

        spinnerPropServerPort.setName("spinnerPropServerPort"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 35;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 14, 0, 16);
        jPanel9.add(spinnerPropServerPort, gridBagConstraints);

        spinnerWebServerPort.setName("spinnerWebServerPort"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 35;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 14, 0, 16);
        jPanel9.add(spinnerWebServerPort, gridBagConstraints);

        spinnerPropClientPort.setName("spinnerPropClientPort"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 35;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 14, 18, 16);
        jPanel9.add(spinnerPropClientPort, gridBagConstraints);

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel10.border.title"))); // NOI18N
        jPanel10.setName("jPanel10"); // NOI18N
        jPanel10.setLayout(new java.awt.GridBagLayout());

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(19, 16, 0, 0);
        jPanel10.add(jLabel10, gridBagConstraints);

        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 16, 0, 0);
        jPanel10.add(jLabel14, gridBagConstraints);

        textFieldStartTime.setText(resourceMap.getString("textFieldStartTime.text")); // NOI18N
        textFieldStartTime.setName("textFieldStartTime"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 37;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 9, 0, 16);
        jPanel10.add(textFieldStartTime, gridBagConstraints);

        textFieldFinishTime.setText(resourceMap.getString("textFieldFinishTime.text")); // NOI18N
        textFieldFinishTime.setName("textFieldFinishTime"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 37;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 9, 18, 16);
        jPanel10.add(textFieldFinishTime, gridBagConstraints);

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel16.border.title"))); // NOI18N
        jPanel16.setName("jPanel16"); // NOI18N
        jPanel16.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        spinnerFirstNumber.setModel(new javax.swing.SpinnerNumberModel(1, 1, 10000, 1));
        spinnerFirstNumber.setName("spinnerFirstNumber"); // NOI18N
        jPanel16.add(spinnerFirstNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(122, 16, 90, -1));

        spinnerLastNumber.setModel(new javax.swing.SpinnerNumberModel(99, 99, 10000, 1));
        spinnerLastNumber.setName("spinnerLastNumber"); // NOI18N
        jPanel16.add(spinnerLastNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(122, 42, 90, -1));

        jPanel20.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel20.border.title"))); // NOI18N
        jPanel20.setName("jPanel20"); // NOI18N

        buttonGroupPoint.add(rbPointOffice);
        rbPointOffice.setText(resourceMap.getString("rbPointOffice.text")); // NOI18N
        rbPointOffice.setName("rbPointOffice"); // NOI18N

        buttonGroupPoint.add(rbPointWindow);
        rbPointWindow.setText(resourceMap.getString("rbPointWindow.text")); // NOI18N
        rbPointWindow.setName("rbPointWindow"); // NOI18N

        buttonGroupPoint.add(rbPointStoika);
        rbPointStoika.setText(resourceMap.getString("rbPointStoika.text")); // NOI18N
        rbPointStoika.setName("rbPointStoika"); // NOI18N

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbPointOffice)
                    .addComponent(rbPointWindow)
                    .addComponent(rbPointStoika))
                .addContainerGap(53, Short.MAX_VALUE))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addComponent(rbPointOffice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbPointWindow)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbPointStoika))
        );

        jPanel16.add(jPanel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 10, 140, -1));

        jPanel21.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel21.border.title"))); // NOI18N
        jPanel21.setName("jPanel21"); // NOI18N

        buttonGroupVoice.add(rbNotificationNo);
        rbNotificationNo.setText(resourceMap.getString("rbNotificationNo.text")); // NOI18N
        rbNotificationNo.setName("rbNotificationNo"); // NOI18N

        buttonGroupVoice.add(rbNotificationGong);
        rbNotificationGong.setText(resourceMap.getString("rbNotificationGong.text")); // NOI18N
        rbNotificationGong.setName("rbNotificationGong"); // NOI18N

        buttonGroupVoice.add(rbNotificationGongVoice);
        rbNotificationGongVoice.setText(resourceMap.getString("rbNotificationGongVoice.text")); // NOI18N
        rbNotificationGongVoice.setName("rbNotificationGongVoice"); // NOI18N

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbNotificationNo)
                    .addComponent(rbNotificationGong)
                    .addComponent(rbNotificationGongVoice))
                .addContainerGap(71, Short.MAX_VALUE))
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addComponent(rbNotificationNo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbNotificationGong)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbNotificationGongVoice)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanel16.add(jPanel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 68, 200, -1));

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N
        jPanel16.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 19, -1, -1));

        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N
        jPanel16.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(16, 45, -1, -1));

        jPanel22.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel22.border.title"))); // NOI18N
        jPanel22.setName("jPanel22"); // NOI18N

        buttonGroupKindNum.add(rbKindPersonal);
        rbKindPersonal.setText(resourceMap.getString("rbKindPersonal.text")); // NOI18N
        rbKindPersonal.setName("rbKindPersonal"); // NOI18N

        buttonGroupKindNum.add(rbKindCommon);
        rbKindCommon.setText(resourceMap.getString("rbKindCommon.text")); // NOI18N
        rbKindCommon.setName("rbKindCommon"); // NOI18N

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbKindPersonal)
                    .addComponent(rbKindCommon))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addComponent(rbKindPersonal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbKindCommon))
        );

        jPanel16.add(jPanel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 110, 140, -1));

        jScrollPane7.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jScrollPane7.border.title"))); // NOI18N
        jScrollPane7.setName("jScrollPane7"); // NOI18N

        listReposts.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        listReposts.setName("listReposts"); // NOI18N
        jScrollPane7.setViewportView(listReposts);

        jTabbedPane2.setName("jTabbedPane2"); // NOI18N

        jPanel23.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel23.border.title"))); // NOI18N
        jPanel23.setName("jPanel23"); // NOI18N

        textFieldURLWebService.setText(resourceMap.getString("textFieldURLWebService.text")); // NOI18N
        textFieldURLWebService.setName("textFieldURLWebService"); // NOI18N

        spinnerBranchId.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(0L), Long.valueOf(0L), null, Long.valueOf(1L)));
        spinnerBranchId.setName("spinnerBranchId"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        buttonCloudTest.setText(resourceMap.getString("buttonCloudTest.text")); // NOI18N
        buttonCloudTest.setName("buttonCloudTest"); // NOI18N
        buttonCloudTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCloudTestActionPerformed(evt);
            }
        });

        buttonSendDataToSky.setText(resourceMap.getString("buttonSendDataToSky.text")); // NOI18N
        buttonSendDataToSky.setName("buttonSendDataToSky"); // NOI18N
        buttonSendDataToSky.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSendDataToSkyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addGroup(jPanel23Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(spinnerBranchId, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 136, Short.MAX_VALUE)
                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(buttonCloudTest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonSendDataToSky, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)))
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(textFieldURLWebService, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerBranchId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13))
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addComponent(buttonCloudTest)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSendDataToSky)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textFieldURLWebService, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane2.addTab(resourceMap.getString("jPanel23.TabConstraints.tabTitle"), jPanel23); // NOI18N

        jPanel24.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel24.border.title"))); // NOI18N
        jPanel24.setName("jPanel24"); // NOI18N

        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        jLabel24.setText(resourceMap.getString("jLabel24.text")); // NOI18N
        jLabel24.setName("jLabel24"); // NOI18N

        textFieldZonBoadrServAddr.setText(resourceMap.getString("textFieldZonBoadrServAddr.text")); // NOI18N
        textFieldZonBoadrServAddr.setName("textFieldZonBoadrServAddr"); // NOI18N

        spinnerZonBoadrServPort.setModel(new javax.swing.SpinnerNumberModel(Long.valueOf(500L), Long.valueOf(500L), Long.valueOf(60000L), Long.valueOf(1L)));
        spinnerZonBoadrServPort.setName("spinnerZonBoadrServPort"); // NOI18N

        buttonCheckZoneBoardServ.setText(resourceMap.getString("buttonCheckZoneBoardServ.text")); // NOI18N
        buttonCheckZoneBoardServ.setName("buttonCheckZoneBoardServ"); // NOI18N
        buttonCheckZoneBoardServ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCheckZoneBoardServActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jLabel24))
                .addGap(18, 18, 18)
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addComponent(spinnerZonBoadrServPort, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 125, Short.MAX_VALUE)
                        .addComponent(buttonCheckZoneBoardServ, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(textFieldZonBoadrServAddr, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(textFieldZonBoadrServAddr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(spinnerZonBoadrServPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonCheckZoneBoardServ))
                .addContainerGap(134, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab(resourceMap.getString("jPanel24.TabConstraints.tabTitle"), jPanel24); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jTabbedPane2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel16, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel8Layout.createSequentialGroup()
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)))
                .addContainerGap())
        );

        tabbedPaneMain.addTab(resourceMap.getString("jPanel8.TabConstraints.tabTitle"), jPanel8); // NOI18N

        jPanel2.setAutoscrolls(true);
        jPanel2.setName("jPanel2"); // NOI18N

        jScrollPane8.setName("jScrollPane8"); // NOI18N

        treeInfo.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("treeInfo.border.title"))); // NOI18N
        treeInfo.setComponentPopupMenu(popupInfo);
        treeInfo.setName("treeInfo"); // NOI18N
        treeInfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeInfoMouseClicked(evt);
            }
        });
        jScrollPane8.setViewportView(treeInfo);

        jScrollPane9.setName("jScrollPane9"); // NOI18N

        textPaneInfoItem.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("textPaneInfoItem.border.title"))); // NOI18N
        textPaneInfoItem.setEnabled(false);
        textPaneInfoItem.setName("textPaneInfoItem"); // NOI18N
        textPaneInfoItem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textPaneInfoItemKeyReleased(evt);
            }
        });
        jScrollPane9.setViewportView(textPaneInfoItem);

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel14.border.title"))); // NOI18N
        jPanel14.setName("jPanel14"); // NOI18N

        jScrollPane17.setName("jScrollPane17"); // NOI18N

        labelInfoItem.setText(resourceMap.getString("labelInfoItem.text")); // NOI18N
        labelInfoItem.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelInfoItem.setName("labelInfoItem"); // NOI18N
        jScrollPane17.setViewportView(labelInfoItem);

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane17, javax.swing.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addComponent(jScrollPane17, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                .addContainerGap())
        );

        textFieldInfoItemName.setText(resourceMap.getString("textFieldInfoItemName.text")); // NOI18N
        textFieldInfoItemName.setEnabled(false);
        textFieldInfoItemName.setName("textFieldInfoItemName"); // NOI18N
        textFieldInfoItemName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textFieldInfoItemNameKeyReleased(evt);
            }
        });

        jLabel22.setText(resourceMap.getString("jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N

        jButton9.setAction(actionMap.get("addInfoItem")); // NOI18N
        jButton9.setName("jButton9"); // NOI18N

        jButton10.setAction(actionMap.get("deleteInfoItem")); // NOI18N
        jButton10.setName("jButton10"); // NOI18N

        jScrollPane16.setName("jScrollPane16"); // NOI18N

        textPaneInfoPrint.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("textPaneInfoPrint.border.title"))); // NOI18N
        textPaneInfoPrint.setName("textPaneInfoPrint"); // NOI18N
        textPaneInfoPrint.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textPaneInfoPrintKeyReleased(evt);
            }
        });
        jScrollPane16.setViewportView(textPaneInfoPrint);
        textPaneInfoPrint.getAccessibleContext().setAccessibleName(resourceMap.getString("jTextPane1.AccessibleContext.accessibleName")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton10))
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane16, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 593, Short.MAX_VALUE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 593, Short.MAX_VALUE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textFieldInfoItemName, javax.swing.GroupLayout.DEFAULT_SIZE, 593, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFieldInfoItemName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton9)
                            .addComponent(jButton10))))
                .addContainerGap())
        );

        tabbedPaneMain.addTab(resourceMap.getString("jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        jPanel13.setAutoscrolls(true);
        jPanel13.setName("jPanel13"); // NOI18N

        jScrollPane10.setName("jScrollPane10"); // NOI18N

        listResponse.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("listResponse.border.title"))); // NOI18N
        listResponse.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        listResponse.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listResponse.setComponentPopupMenu(popupResponse);
        listResponse.setName("listResponse"); // NOI18N
        jScrollPane10.setViewportView(listResponse);

        jScrollPane11.setName("jScrollPane11"); // NOI18N

        textPaneResponse.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("textPaneResponse.border.title"))); // NOI18N
        textPaneResponse.setName("textPaneResponse"); // NOI18N
        textPaneResponse.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textPaneResponseKeyReleased(evt);
            }
        });
        jScrollPane11.setViewportView(textPaneResponse);

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel15.border.title"))); // NOI18N
        jPanel15.setName("jPanel15"); // NOI18N

        labelRespinse.setText(resourceMap.getString("labelRespinse.text")); // NOI18N
        labelRespinse.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelRespinse.setName("labelRespinse"); // NOI18N

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelRespinse, javax.swing.GroupLayout.DEFAULT_SIZE, 659, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addComponent(labelRespinse, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                .addContainerGap())
        );

        jButton7.setAction(actionMap.get("deleteRespItem")); // NOI18N
        jButton7.setName("jButton7"); // NOI18N

        jButton8.setAction(actionMap.get("addRespItem")); // NOI18N
        jButton8.setName("jButton8"); // NOI18N

        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        textFieldResponse.setText(resourceMap.getString("textFieldResponse.text")); // NOI18N
        textFieldResponse.setName("textFieldResponse"); // NOI18N
        textFieldResponse.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textFieldResponseKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jButton8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton7))
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 691, Short.MAX_VALUE)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel16)
                    .addComponent(textFieldResponse, javax.swing.GroupLayout.DEFAULT_SIZE, 691, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton8)
                            .addComponent(jButton7)))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFieldResponse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        tabbedPaneMain.addTab(resourceMap.getString("jPanel13.TabConstraints.tabTitle"), jPanel13); // NOI18N

        jPanel18.setAutoscrolls(true);
        jPanel18.setName("jPanel18"); // NOI18N

        jScrollPane13.setName("jScrollPane13"); // NOI18N

        listResults.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("listResults.border.title"))); // NOI18N
        listResults.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        listResults.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listResults.setComponentPopupMenu(popupResults);
        listResults.setName("listResults"); // NOI18N
        jScrollPane13.setViewportView(listResults);

        jButton11.setAction(actionMap.get("addResult")); // NOI18N
        jButton11.setText(resourceMap.getString("jButton11.text")); // NOI18N
        jButton11.setName("jButton11"); // NOI18N

        jButton12.setAction(actionMap.get("deleteResult")); // NOI18N
        jButton12.setName("jButton12"); // NOI18N

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 956, Short.MAX_VALUE)
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jButton11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton12)))
                .addContainerGap())
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton11)
                    .addComponent(jButton12))
                .addContainerGap())
        );

        tabbedPaneMain.addTab(resourceMap.getString("jPanel18.TabConstraints.tabTitle"), jPanel18); // NOI18N

        jPanel1.add(tabbedPaneMain);

        getContentPane().add(jPanel1);

        jMenuBar1.setName("jMenuBar1"); // NOI18N

        menuFile.setText(resourceMap.getString("menuFile.text")); // NOI18N
        menuFile.setName("menuFile"); // NOI18N

        menuLangs.setText(resourceMap.getString("menuLangs.text")); // NOI18N
        menuLangs.setName("menuLangs"); // NOI18N
        menuFile.add(menuLangs);

        jSeparator15.setName("jSeparator15"); // NOI18N
        menuFile.add(jSeparator15);

        jMenuItem25.setAction(actionMap.get("sendMessage")); // NOI18N
        jMenuItem25.setName("jMenuItem25"); // NOI18N
        menuFile.add(jMenuItem25);

        jMenuItem8.setAction(actionMap.get("saveConfiguration")); // NOI18N
        jMenuItem8.setName("jMenuItem8"); // NOI18N
        menuFile.add(jMenuItem8);

        jMenuItem4.setAction(actionMap.get("hideWindow")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        menuFile.add(jMenuItem4);

        jSeparator1.setName("jSeparator1"); // NOI18N
        menuFile.add(jSeparator1);

        jMenuItem3.setAction(actionMap.get("quit")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        menuFile.add(jMenuItem3);

        jMenuBar1.add(menuFile);

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItem19.setAction(actionMap.get("editMainBoard")); // NOI18N
        jMenuItem19.setName("jMenuItem19"); // NOI18N
        jMenu1.add(jMenuItem19);

        jMenuBar1.add(jMenu1);

        menuUsers.setText(resourceMap.getString("menuUsers.text")); // NOI18N
        menuUsers.setName("menuUsers"); // NOI18N

        jMenuItem5.setAction(actionMap.get("addUser")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        menuUsers.add(jMenuItem5);

        jSeparator2.setName("jSeparator2"); // NOI18N
        menuUsers.add(jSeparator2);

        jMenuItem2.setAction(actionMap.get("deleteUser")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        menuUsers.add(jMenuItem2);

        jSeparator14.setName("jSeparator14"); // NOI18N
        menuUsers.add(jSeparator14);

        jMenuItem37.setAction(actionMap.get("changePriority")); // NOI18N
        jMenuItem37.setName("jMenuItem37"); // NOI18N
        menuUsers.add(jMenuItem37);

        jMenuBar1.add(menuUsers);

        menuServices.setText(resourceMap.getString("menuServices.text")); // NOI18N
        menuServices.setName("menuServices"); // NOI18N

        jMenuItem7.setAction(actionMap.get("addService")); // NOI18N
        jMenuItem7.setName("jMenuItem7"); // NOI18N
        menuServices.add(jMenuItem7);

        jSeparator3.setName("jSeparator3"); // NOI18N
        menuServices.add(jSeparator3);

        jMenuItem6.setAction(actionMap.get("deleteService")); // NOI18N
        jMenuItem6.setName("jMenuItem6"); // NOI18N
        menuServices.add(jMenuItem6);

        jSeparator4.setName("jSeparator4"); // NOI18N
        menuServices.add(jSeparator4);

        jMenuItem9.setAction(actionMap.get("getTicket")); // NOI18N
        jMenuItem9.setName("jMenuItem9"); // NOI18N
        menuServices.add(jMenuItem9);

        jMenuItem23.setAction(actionMap.get("standAdvance")); // NOI18N
        jMenuItem23.setName("jMenuItem23"); // NOI18N
        menuServices.add(jMenuItem23);

        jMenuBar1.add(menuServices);

        menuAbout.setText(resourceMap.getString("menuAbout.text")); // NOI18N
        menuAbout.setName("menuAbout"); // NOI18N

        menuItemHelp.setAction(actionMap.get("getHelp")); // NOI18N
        menuItemHelp.setName("menuItemHelp"); // NOI18N
        menuAbout.add(menuItemHelp);

        menuItemAbout.setAction(actionMap.get("getAbout")); // NOI18N
        menuItemAbout.setName("menuItemAbout"); // NOI18N
        menuAbout.add(menuItemAbout);

        jMenuBar1.add(menuAbout);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void checkBoxServerAutoStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_checkBoxServerAutoStateChanged
    buttonServerRequest.setEnabled(!checkBoxServerAuto.isSelected());
    if (timer.isRunning() && checkBoxServerAuto.isSelected()) {
        checkServer();
    }
    startTimer();
}//GEN-LAST:event_checkBoxServerAutoStateChanged

private void buttonLockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLockActionPerformed

    checkWelcome(Uses.WELCOME_LOCK);

}//GEN-LAST:event_buttonLockActionPerformed

private void buttonUnlockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonUnlockActionPerformed
//GEN-LAST:event_buttonUnlockActionPerformed
        checkWelcome(Uses.WELCOME_UNLOCK);
    }

//**********************************************************************************************************************
//**********************************************************************************************************************
//*****************************************Сохранение*******************************************************************
private void checkBoxServerAutoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_checkBoxServerAutoFocusLost
    saveSettings();
}//GEN-LAST:event_checkBoxServerAutoFocusLost

private void textFieldClientAdressFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textFieldClientAdressFocusLost
    saveSettings();
}//GEN-LAST:event_textFieldClientAdressFocusLost

private void checkBoxClientAutoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_checkBoxClientAutoFocusLost
    saveSettings();
}//GEN-LAST:event_checkBoxClientAutoFocusLost

//*****************************************Сохранение*******************************************************************
//**********************************************************************************************************************
//*************************************** Запрос в ручную **************************************************************
private void buttonServerRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonServerRequestActionPerformed
    checkServer();
}//GEN-LAST:event_buttonServerRequestActionPerformed

private void buttonClientRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonClientRequestActionPerformed
    checkWelcome(null);
}//GEN-LAST:event_buttonClientRequestActionPerformed

private void checkBoxClientAutoStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_checkBoxClientAutoStateChanged
    buttonClientRequest.setEnabled(!checkBoxClientAuto.isSelected());
    if (timer.isRunning() && checkBoxClientAuto.isSelected()) {
        checkWelcome(null);
    }
    startTimer();
}//GEN-LAST:event_checkBoxClientAutoStateChanged

private void buttonShutDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonShutDownActionPerformed
    // Уточним намерения
    if (JOptionPane.showConfirmDialog(this,
            getLocaleMessage("admin.close_welcame.title"),
            getLocaleMessage("admin.close_welcame.caption"),
            JOptionPane.YES_NO_OPTION) == 1) {
        return;
    }
    checkWelcome(Uses.WELCOME_OFF);
}//GEN-LAST:event_buttonShutDownActionPerformed

private void buttonRestartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRestartActionPerformed
    checkWelcome(Uses.WELCOME_REINIT);
    final ATalkingClock clock = new ATalkingClock(1000, 1) {

        @Override
        public void run() {
            checkWelcome(null);
            JOptionPane.showConfirmDialog(null, getLocaleMessage("admin.server_reinit.title"), getLocaleMessage("admin.server_reinit.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
        }
    };
    clock.start();
}//GEN-LAST:event_buttonRestartActionPerformed

private void textFieldServerAddrFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textFieldServerAddrFocusLost

    saveSettings();
}//GEN-LAST:event_textFieldServerAddrFocusLost

private void passwordFieldUserKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordFieldUserKeyReleased

    saveUser();
}//GEN-LAST:event_passwordFieldUserKeyReleased

private void tabbedPaneMainStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneMainStateChanged

    // это событие переключения закладок на табе.
    menuServices.setEnabled(tabbedPaneMain.getSelectedIndex() == 1);
    menuUsers.setEnabled(tabbedPaneMain.getSelectedIndex() == 1);
}//GEN-LAST:event_tabbedPaneMainStateChanged

private void textFieldUserIdentKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldUserIdentKeyReleased

    saveUser();
}//GEN-LAST:event_textFieldUserIdentKeyReleased

private void listUserServiceMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listUserServiceMouseClicked

    // назначение приоритета услуге.
    if (evt.getClickCount() == 2) {
        changeServicePriority();
    }

}//GEN-LAST:event_listUserServiceMouseClicked

private void checkBoxReportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_checkBoxReportMouseClicked

    saveUser();
}//GEN-LAST:event_checkBoxReportMouseClicked

private void checkBoxAdminMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_checkBoxAdminMouseClicked
    //проверим не последний ли это админ
    final QUser user = (QUser) listUsers.getSelectedValue();
    if (user.getAdminAccess()) {
        int cnt = 0;
        for (int i = 0; i < listUsers.getModel().getSize(); i++) {
            if (((QUser) listUsers.getModel().getElementAt(i)).getAdminAccess()) {
                cnt++;
            }
        }
        if (cnt == 1) {
            JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.edit_user_err.title"), getLocaleMessage("admin.edit_user_err.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
            checkBoxAdmin.setSelected(true);
            return;
        }
    }
    saveUser();
}//GEN-LAST:event_checkBoxAdminMouseClicked

private void treeServicesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeServicesMouseClicked
    // Редактирование услуги.
    if (evt.getClickCount() == 2) {
        final TreePath selectedPath = treeServices.getSelectionPath();
        if (selectedPath == null) {
            return;
        } else {
            editService();
        }
    }

}//GEN-LAST:event_treeServicesMouseClicked

private void buttonRestartServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRestartServerActionPerformed

    NetCommander.restartServer(new ServerNetProperty());
    final ATalkingClock clock = new ATalkingClock(4000, 1) {

        @Override
        public void run() {
            JOptionPane.showConfirmDialog(null, getLocaleMessage("admin.server_restart.title"), getLocaleMessage("admin.server_restart.caption"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
            checkServer();
        }
    };
    clock.start();

}//GEN-LAST:event_buttonRestartServerActionPerformed

private void textFieldInfoItemNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldInfoItemNameKeyReleased
    final QInfoItem item = (QInfoItem) treeInfo.getLastSelectedPathComponent();
    if (item != null && !item.isRoot()) {
        item.setName(textFieldInfoItemName.getText());
    }
}//GEN-LAST:event_textFieldInfoItemNameKeyReleased

private void textPaneInfoItemKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textPaneInfoItemKeyReleased
    final QInfoItem item = (QInfoItem) treeInfo.getLastSelectedPathComponent();
    if (item != null && !item.isRoot()) {
        item.setHTMLText(textPaneInfoItem.getText());
        labelInfoItem.setText(textPaneInfoItem.getText());
    }
}//GEN-LAST:event_textPaneInfoItemKeyReleased

private void treeInfoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeInfoMouseClicked
    final QInfoItem item = (QInfoItem) treeInfo.getLastSelectedPathComponent();
    if (item != null) {
        textFieldInfoItemName.setEnabled(!item.isRoot());
        textPaneInfoItem.setEnabled(!item.isRoot());
        textPaneInfoPrint.setEnabled(!item.isRoot());
    }
}//GEN-LAST:event_treeInfoMouseClicked

private void textFieldResponseKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldResponseKeyReleased
    final QRespItem item = (QRespItem) listResponse.getSelectedValue();
    if (item != null) {
        item.setName(textFieldResponse.getText());
    }
}//GEN-LAST:event_textFieldResponseKeyReleased

private void textPaneResponseKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textPaneResponseKeyReleased
    final QRespItem item = (QRespItem) listResponse.getSelectedValue();
    if (item != null) {
        item.setHTMLText(textPaneResponse.getText());
        labelRespinse.setText(textPaneResponse.getText());
    }
}//GEN-LAST:event_textPaneResponseKeyReleased

private void textFieldScheduleNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldScheduleNameKeyReleased
    final QSchedule item = (QSchedule) listSchedule.getSelectedValue();
    if (item != null) {
        item.setName(textFieldScheduleName.getText());
    }
}//GEN-LAST:event_textFieldScheduleNameKeyReleased

private void listScheduleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listScheduleMouseClicked
    // Редактирование услуги.
    if (evt.getClickCount() == 2) {
        final QSchedule item = (QSchedule) listSchedule.getSelectedValue();
        if (item != null) {
            editSchedule();
        }
    }
}//GEN-LAST:event_listScheduleMouseClicked

private void textFieldCalendarNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldCalendarNameKeyReleased
    final QCalendar item = (QCalendar) listCalendar.getSelectedValue();
    if (item != null) {
        item.setName(textFieldCalendarName.getText());
    }
}//GEN-LAST:event_textFieldCalendarNameKeyReleased

private void tabbedPaneMainFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tabbedPaneMainFocusLost
    final CalendarTableModel model = (CalendarTableModel) tableCalendar.getModel();
    if (!model.isSaved()) {
        if (0 == JOptionPane.showConfirmDialog(null, getLocaleMessage("admin.calendar_change.message"), getLocaleMessage("admin.calendar_change.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
            model.save();
        }
    }
}//GEN-LAST:event_tabbedPaneMainFocusLost
    private int inGrid = 0;

private void tableCalendarFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tableCalendarFocusGained
    if (inGrid > 0) {
        inGrid = 0;
        return;
    }
    // проверка на то что перед редактированием сетки нужно сохранать сам список календарей
    // инече не будет известно с каким ID привязки к календарю сохранять выходные дни
    inGrid++;
    for (int i = 0; i < listCalendar.getModel().getSize(); i++) {
        boolean flag = false;
        for (QCalendar calendar : QCalendarList.getInstance().getItems()) {
            if (((QCalendar) listCalendar.getModel().getElementAt(i)).getId().equals(calendar.getId())) {
                flag = true;
                continue;
            }
        }
        if (!flag) {
            JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.calendar_warn.message"), getLocaleMessage("admin.calendar_warn.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    }
}//GEN-LAST:event_tableCalendarFocusGained

private void textPaneInfoPrintKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textPaneInfoPrintKeyReleased
    final QInfoItem item = (QInfoItem) treeInfo.getLastSelectedPathComponent();
    if (item != null && !item.isRoot()) {
        item.setTextPrint(textPaneInfoPrint.getText());
    }
}//GEN-LAST:event_textPaneInfoPrintKeyReleased

private void buttonResetMainTabloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetMainTabloActionPerformed
    NetCommander.restartMainTablo(new ServerNetProperty());
    JOptionPane.showConfirmDialog(null, getLocaleMessage("admin.main_tablo_restart.message"), getLocaleMessage("admin.main_tablo_restart.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
}//GEN-LAST:event_buttonResetMainTabloActionPerformed

private void buttonCloudTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCloudTestActionPerformed
    // поддержка расширяемости плагинами
    final StringBuilder sb = new StringBuilder(getLocaleMessage("admin.cloud_test_dialog.results") + ":\n");
    try {
        for (final IPing event : ServiceLoader.load(IPing.class)) {
            if (event.getUID() == 01L) {
                QLog.l().logger().info("Вызов SPI расширения. Описание: " + event.getDescription());
                sb.append(event.getDescription()).append(" ").append(getLocaleMessage("admin.cloud_test_dialog.result")).append(": ").append(event.ping()).append("\n");
            }
        }
    } catch (Throwable ex) {
        sb.append(getLocaleMessage("admin.cloud_test_dialog.error"));
    }
    final String res = sb.toString();
    sb.setLength(0);
    JOptionPane.showConfirmDialog(null, res, getLocaleMessage("admin.cloud_test_dialog.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
}//GEN-LAST:event_buttonCloudTestActionPerformed

private void buttonSendDataToSkyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSendDataToSkyActionPerformed
    // поддержка расширяемости плагинами
    final Thread th = new Thread(new Runnable() {

        @Override
        public void run() {
            int all = 0;
            for (QService service : QServiceTree.getInstance().getNodes()) {
                if (service.isLeaf()) {
                    all++;
                }
            }
            all += QUserList.getInstance().getSize();
            int tmp = 0;
            try {
                for (final IDataExchange event : ServiceLoader.load(IDataExchange.class)) {
                    QLog.l().logger().info("Вызов SPI расширения. Описание: " + event.getDescription());
                    for (QService service : QServiceTree.getInstance().getNodes()) {
                        if (service.isLeaf()) {
                            event.sendServiceName(ServerProps.getInstance().getProps().getBranchOfficeId(), service.getId(), service.getName());
                            tmp++;
                            final String s = "" + tmp + "/" + all + "  " + tmp * 100 / all + "%";
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    buttonSendDataToSky.setText(s);
                                }
                            });
                        }
                    }
                    for (QUser user : QUserList.getInstance().getItems()) {
                        event.sendUserName(ServerProps.getInstance().getProps().getBranchOfficeId(), user.getId(), user.getName());
                        tmp++;
                        final String s = "" + tmp + "/" + all + "  " + tmp * 100 / all + "%";
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                buttonSendDataToSky.setText(s);
                            }
                        });
                    }
                }
            } catch (Throwable ex) {
                QLog.l().logger().error("Не отослали названия в облако.", ex);
                JOptionPane.showConfirmDialog(null, getLocaleMessage("admin.cloud_senddata_dialog.message_err") + "\n" + ex.getMessage(), getLocaleMessage("admin.cloud_senddata_dialog.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
            JOptionPane.showConfirmDialog(null, getLocaleMessage("admin.cloud_senddata_dialog.message") + " " + tmp + "/" + all, getLocaleMessage("admin.cloud_senddata_dialog.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    buttonSendDataToSky.setText(getLocaleMessage("buttonSendDataToSky.text"));
                }
            });

        }
    });
    th.start();

}//GEN-LAST:event_buttonSendDataToSkyActionPerformed

    private void buttonCheckZoneBoardServActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCheckZoneBoardServActionPerformed
        // поддержка расширяемости плагинами
        final StringBuilder sb = new StringBuilder(getLocaleMessage("admin.zoneboard_test_dialog.results") + ":\n");
        try {
            for (final IPing event : ServiceLoader.load(IPing.class)) {
                if (event.getUID() == 02L) {
                    QLog.l().logger().info("Вызов SPI расширения. Описание: " + event.getDescription());
                    sb.append(event.getDescription()).append(" ").append(getLocaleMessage("admin.zoneboard_test_dialog.result")).append(": ").append(event.ping()).append("\n");
                }
            }
        } catch (Throwable ex) {
            QLog.l().logger().error("Ошибка при пинговании зонального сервера. ", ex);
            sb.append(getLocaleMessage("admin.zoneboard_test_dialog.error"));
        }
        final String res = sb.toString();
        sb.setLength(0);
        JOptionPane.showConfirmDialog(null, res, getLocaleMessage("admin.zoneboard_test_dialog.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

    }//GEN-LAST:event_buttonCheckZoneBoardServActionPerformed

    @Action
    public void changeServicePriority() {
        final QPlanService plan = (QPlanService) listUserService.getSelectedValue();
        if (plan == null) {
            return;
        }
        // тут надо фокус перекинуть, чтоб названия услуги изменилось с учетом приоритета.
        listUserService.requestFocus();
        listUserService.requestFocusInWindow();
        final String name = (String) JOptionPane.showInputDialog(this,
                getLocaleMessage("admin.select_priority.message"),
                getLocaleMessage("admin.select_priority.title"),
                JOptionPane.QUESTION_MESSAGE,
                null,
                Uses.COEFF_WORD.values().toArray(),
                Uses.COEFF_WORD.values().toArray()[plan.getCoefficient()]);
        //Если не выбрали, то выходим
        if (name != null) {
            for (int i = 0; i < Uses.COEFF_WORD.size(); i++) {
                if (name.equals(Uses.COEFF_WORD.get(i))) {
                    plan.setCoefficient(i);
                }
            }
        }
    }

    @Action
    public void changeFlexiblePriorityAbility() {
        final QPlanService plan = (QPlanService) listUserService.getSelectedValue();
        if (plan == null) {
            return;
        }
        // тут надо фокус перекинуть, чтоб названия услуги изменилось с учетом приоритета.
        listUserService.requestFocus();
        listUserService.requestFocusInWindow();
        plan.setFlexible_coef(!plan.getFlexible_coef());
    }

    @Action
    public void setUpdateServiceFire() {
        final QPlanService plan = (QPlanService) listUserService.getSelectedValue();
        if (plan == null) {
            return;
        }
        final String res = NetCommander.setServiseFire(new ServerNetProperty(), plan.getService().getId(), plan.getUser().getId(), plan.getCoefficient());
        JOptionPane.showMessageDialog(this, res, getLocaleMessage("admin.add_service_to_user.title"), JOptionPane.INFORMATION_MESSAGE);
    }

    @Action
    public void deleteUpdateServiceFire() {
        final QPlanService plan = (QPlanService) listUserService.getSelectedValue();
        if (plan == null) {
            return;
        }
        final String res = NetCommander.deleteServiseFire(new ServerNetProperty(), plan.getService().getId(), plan.getUser().getId());
        JOptionPane.showMessageDialog(this, res, getLocaleMessage("admin.remove_service_to_user.title"), JOptionPane.INFORMATION_MESSAGE);
    }

    @Action
    public void getHelp() {
    }

    @Action
    public void editService() {
        final TreePath selectedPath = treeServices.getSelectionPath();
        if (selectedPath == null) {
            return;
        } else {
            final QService service = (QService) selectedPath.getLastPathComponent();
            FServiceChangeDialod.changeService(this, true, service, (ComboBoxModel) listSchedule.getModel(), (ComboBoxModel) listCalendar.getModel());
            showServiceInfo(service);
        }
    }

    @Action
    public void getAbout() {
        FAbout.showAbout(this, true, ServerProps.getInstance().getProps().getVersion());
    }

    @Action
    public void standAdvance() {
        final QService service = (QService) treeServices.getLastSelectedPathComponent();
        if (service != null && service.isLeaf()) {
            final QAdvanceCustomer res;
            try {
                res = FAdvanceCalendar.showCalendar(this, true, new ServerNetProperty(), service, false, 0, -1);
            } catch (Exception ex) {
                throw new ClientException(getLocaleMessage("admin.send_cmd_adv.err") + " " + ex);
            }
            if (res == null) {
                return;
            }
            // печатаем результат
            new Thread(new Runnable() {

                @Override
                public void run() {
                    FWelcome.printTicketAdvance(res, ((QService) treeServices.getModel().getRoot()).getName());
                }
            }).start();

            JOptionPane.showMessageDialog(this, getLocaleMessage("admin.client_adv_dialog.msg_1") + " \"" + service.getName() + "\". " + getLocaleMessage("admin.client_adv_dialog.msg_2") + " \"" + res.getAdvanceTime() + "\".", getLocaleMessage("admin.client_adv_dialog.title"), JOptionPane.INFORMATION_MESSAGE);
        }
    }
    /**
     * Редоктор для главного табло. Singleton.
     */
    private AFBoardRedactor board = null;
    private static String adr;
    private static Integer port;

    @Action
    public void editMainBoard() throws IOException {
        QLog.l().logger().info("Открыть редактор главного табло.");
        final ServerNetProperty servProp = new ServerNetProperty();
        try {
            if (board == null) {
                adr = servProp.getAddress().getHostAddress();
                port = servProp.getPort();
                board = MainBoard.getInstance().getRedactor();
                board.setParams(servProp);
            } else {
                if (!servProp.getAddress().getHostAddress().equals(adr)
                        || !servProp.getPort().equals(port)) {
                    board.setParams(servProp);
                    adr = servProp.getAddress().getHostAddress();
                    port = servProp.getPort();
                }
            }
        } catch (Exception e) {
            board = null;
            ClientWarning.showWarning(getLocaleMessage("admin.open_editor.wern") + "\n" + e);
            return;
        }
        // Отцентирируем
        Uses.setLocation(board);
        // Покажем
        board.setVisible(true);
    }    //*****************************************Запрос в ручную*******************************************************************

    @Action
    public void sendMessage() {
        FMessager.getMessager(this, ServerProps.getInstance().getProps().getClientPort(), listUsers.getModel(), treeServices.getModel());
    }

    @Action
    public void addRespItem() {
        // Запросим название юзера и если оно уникально, то примем
        String respName = getLocaleMessage("admin.add_resp_dialog.info");
        boolean flag = true;
        while (flag) {
            respName = (String) JOptionPane.showInputDialog(this, getLocaleMessage("admin.add_resp_dialog.message"), getLocaleMessage("admin.add_resp_dialog.title"), 3, null, null, respName);
            if (respName == null) {
                return;
            }
            if ("".equals(respName)) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_resp_dialog.err1.message"), getLocaleMessage("admin.add_resp_dialog.err1.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (respName.indexOf('\"') != -1) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_resp_dialog.err2.message"), getLocaleMessage("admin.add_resp_dialog.err2.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (respName.length() > 100) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_resp_dialog.err3.message"), getLocaleMessage("admin.add_resp_dialog.err3.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else {
                flag = false;
            }
        }
        QLog.l().logger().debug("Добавляем отзыв \"" + respName + "\"");
        final QRespItem item = new QRespItem();
        item.setName(respName);
        item.setHTMLText("<html><b><p align=center><span style='font-size:20.0pt;color:green'>" + respName + "</span></b>");
        QResponseList.getInstance().addElement(item);
        listResponse.setSelectedValue(item, true);
    }

    @Action
    public void deleteRespItem() {
        if (listResponse.getSelectedIndex() != -1) {
            if (JOptionPane.showConfirmDialog(this,
                    getLocaleMessage("admin.resp_delete.message") + " \"" + ((QRespItem) listResponse.getSelectedValue()).getName() + "\"?",
                    getLocaleMessage("admin.resp_delete.title"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
            QLog.l().logger().debug("Удаляем отзыв \"" + ((QRespItem) listResponse.getSelectedValue()).getName() + "\"");


            final int del = listResponse.getSelectedIndex();
            final QResponseList m = (QResponseList) listResponse.getModel();
            final int col = m.getSize();

            final QRespItem item = (QRespItem) listResponse.getSelectedValue();
            QResponseList.getInstance().removeElement(item);

            if (col != 1) {
                if (col == del + 1) {
                    listResponse.setSelectedValue(m.getElementAt(del - 1), true);
                } else if (col > del + 1) {
                    listResponse.setSelectedValue(m.getElementAt(del), true);
                }
            }
        }
    }

    @Action
    public void addSchedule() {
        // Запросим название плана и если оно уникально, то примем
        String scheduleName = getLocaleMessage("admin.add_work_plan_dialog.info");
        boolean flag = true;
        while (flag) {
            scheduleName = (String) JOptionPane.showInputDialog(this, getLocaleMessage("admin.add_work_plan_dialog.message"), getLocaleMessage("admin.add_work_plan_dialog.title"), 3, null, null, scheduleName);
            if (scheduleName == null) {
                return;
            }
            if ("".equals(scheduleName)) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_work_plan_dialog.err1.message"), getLocaleMessage("admin.add_work_plan_dialog.err1.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (scheduleName.indexOf('\"') != -1) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_work_plan_dialog.err2.message"), getLocaleMessage("admin.add_work_plan_dialog.err2.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (scheduleName.length() > 150) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_work_plan_dialog.err3.message"), getLocaleMessage("admin.add_work_plan_dialog.err3.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else {
                flag = false;
            }
        }
        QLog.l().logger().debug("Добавляем отзыв \"" + scheduleName + "\"");
        final QSchedule item = new QSchedule();
        item.setName(scheduleName);
        item.setType(0);
        QScheduleList.getInstance().addElement(item);
        listSchedule.setSelectedValue(item, true);
    }

    @Action
    public void deleteSchedule() {
        if (listSchedule.getSelectedIndex() != -1) {
            if (JOptionPane.showConfirmDialog(this,
                    getLocaleMessage("admin.work_plan_delete.message") + " \"" + ((QSchedule) listSchedule.getSelectedValue()).getName() + "\"?",
                    getLocaleMessage("admin.work_plan_delete.title"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
            QLog.l().logger().debug("Удаляем план \"" + ((QSchedule) listSchedule.getSelectedValue()).getName() + "\"");


            final int del = listSchedule.getSelectedIndex();
            final QScheduleList m = (QScheduleList) listSchedule.getModel();
            final int col = m.getSize();

            final QSchedule item = (QSchedule) listSchedule.getSelectedValue();

            // Уберем удаленный план у услуг
            for (QService service : QServiceTree.getInstance().getNodes()) {
                if (item.equals(service.getSchedule())) {
                    service.setSchedule(null);
                }
            }

            QScheduleList.getInstance().removeElement(item);

            if (col != 1) {
                if (col == del + 1) {
                    listSchedule.setSelectedValue(m.getElementAt(del - 1), true);
                } else if (col > del + 1) {
                    listSchedule.setSelectedValue(m.getElementAt(del), true);
                }
            }
        }
    }

    @Action
    public void editSchedule() {
        final QSchedule item = (QSchedule) listSchedule.getSelectedValue();
        if (item != null) {
            FScheduleChangeDialod.changeSchedule(this, true, item);
            scheduleListChange();
        }
    }

    @Action
    public void addResult() {
        String resultText = "";
        boolean flag = true;
        while (flag) {
            resultText = (String) JOptionPane.showInputDialog(this, getLocaleMessage("admin.add_result_dialog.message"), getLocaleMessage("admin.add_result_dialog.title"), 3, null, null, resultText);
            if (resultText == null) {
                return;
            }
            if ("".equals(resultText)) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_result_dialog.err1.message"), getLocaleMessage("admin.add_result_dialog.err1.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (resultText.indexOf('\"') != -1) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_result_dialog.err2.message"), getLocaleMessage("admin.add_result_dialog.err2.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (resultText.length() > 150) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_result_dialog.err3.message"), getLocaleMessage("admin.add_result_dialog.err3.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else {
                flag = false;
            }
        }
        QLog.l().logger().debug("Добавляем результат \"" + resultText + "\"");
        final QResult item = new QResult();
        item.setName(resultText);
        QResultList.getInstance().addElement(item);
        listResults.setSelectedValue(item, true);
    }

    @Action
    public void deleteResult() {
        if (listResults.getSelectedIndex() != -1) {
            if (JOptionPane.showConfirmDialog(this,
                    getLocaleMessage("admin.result_delete.message") + " \"" + ((QResult) listResults.getSelectedValue()).getName() + "\"?",
                    getLocaleMessage("admin.result_delete.title"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
            QLog.l().logger().debug("Удаляем результат \"" + ((QResult) listResults.getSelectedValue()).getName() + "\"");


            final int del = listResults.getSelectedIndex();
            final QResultList m = (QResultList) listResults.getModel();
            final int col = m.getSize();

            final QResult item = (QResult) listResults.getSelectedValue();
            QResultList.getInstance().removeElement(item);

            if (col != 1) {
                if (col == del + 1) {
                    listResults.setSelectedValue(m.getElementAt(del - 1), true);
                } else if (col > del + 1) {
                    listResults.setSelectedValue(m.getElementAt(del), true);
                }
            }
        }
    }

    @Action
    public void addCalendar() {
        // Запросим название календаря и если оно уникально, то примем
        String calendarName = getLocaleMessage("admin.add_calendar_dialog.info");
        boolean flag = true;
        while (flag) {
            calendarName = (String) JOptionPane.showInputDialog(this, getLocaleMessage("admin.add_calendar_dialog.message"), getLocaleMessage("admin.add_calendar_dialog.title"), 3, null, null, calendarName);
            if (calendarName == null) {
                return;
            }
            if ("".equals(calendarName)) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_calendar_dialog.err1.message"), getLocaleMessage("admin.add_calendar_dialog.err1.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (calendarName.indexOf('\"') != -1) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_calendar_dialog.err2.message"), getLocaleMessage("admin.add_calendar_dialog.err2.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else if (calendarName.length() > 150) {
                JOptionPane.showConfirmDialog(this, getLocaleMessage("admin.add_calendar_dialog.err3.message"), getLocaleMessage("admin.add_calendar_dialog.err3.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            } else {
                flag = false;
            }
        }
        QLog.l().logger().debug("Добавляем календарь \"" + calendarName + "\"");
        final QCalendar item = new QCalendar();
        item.setName(calendarName);
        QCalendarList.getInstance().addElement(item);
        listCalendar.setSelectedValue(item, true);
    }

    @Action
    public void deleteCalendar() {
        if (listCalendar.getSelectedIndex() != -1
                && (((QCalendar) listCalendar.getSelectedValue()).getId() == null || ((QCalendar) listCalendar.getSelectedValue()).getId() != 1)) {
            if (JOptionPane.showConfirmDialog(this,
                    getLocaleMessage("admin.calendar_delete.message") + " \"" + ((QCalendar) listCalendar.getSelectedValue()).getName() + "\"?",
                    getLocaleMessage("admin.calendar_delete.title"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
            QLog.l().logger().debug("Удаляем календарь \"" + ((QCalendar) listCalendar.getSelectedValue()).getName() + "\"");


            final int del = listCalendar.getSelectedIndex();
            final QCalendarList m = (QCalendarList) listCalendar.getModel();
            final int col = m.getSize();

            final QCalendar item = (QCalendar) listCalendar.getSelectedValue();

            // Уберем удаленный календарь у услуг
            for (QService service : QServiceTree.getInstance().getNodes()) {
                if (item.equals(service.getCalendar())) {
                    service.setCalendar(null);
                }
            }

            QCalendarList.getInstance().removeElement(item);

            if (col != 1) {
                if (col == del + 1) {
                    listCalendar.setSelectedValue(m.getElementAt(del - 1), true);
                } else if (col > del + 1) {
                    listCalendar.setSelectedValue(m.getElementAt(del), true);
                }
            }
        }
    }

    @Action
    public void dropCalendarSelection() {
        final CalendarTableModel model = (CalendarTableModel) tableCalendar.getModel();
        model.dropCalendar();
    }

    @Action
    public void checkSaturday() {
        final CalendarTableModel model = (CalendarTableModel) tableCalendar.getModel();
        model.checkSaturday();
    }

    @Action
    public void checkSundays() {
        final CalendarTableModel model = (CalendarTableModel) tableCalendar.getModel();
        model.checkSunday();
    }

    @Action
    public void saveCalendar() {
        final CalendarTableModel model = (CalendarTableModel) tableCalendar.getModel();
        model.save();
        JOptionPane.showMessageDialog(this, getLocaleMessage("admin.action.save_calensar.message"), getLocaleMessage("admin.action.save_calensar.title"), JOptionPane.INFORMATION_MESSAGE);
    }

    @Action
    public void changePriority() {
        final String num = (String) JOptionPane.showInputDialog(this, getLocaleMessage("admin.action.change_priority.num.message"), getLocaleMessage("admin.action.change_priority.num.title"), 3, null, null, "");
        if (num != null) {


            final String name = (String) JOptionPane.showInputDialog(this,
                    getLocaleMessage("admin.action.change_priority.get.message"),
                    getLocaleMessage("admin.action.change_priority.get.title"),
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    Uses.PRIORITYS_WORD.values().toArray(),
                    Uses.PRIORITYS_WORD.values().toArray()[1]);
            //Если не выбрали, то выходим
            if (name != null) {
                for (int i = 0; i < Uses.PRIORITYS_WORD.size(); i++) {
                    if (name.equals(Uses.PRIORITYS_WORD.get(i))) {
                        JOptionPane.showMessageDialog(this, NetCommander.setCustomerPriority(new ServerNetProperty(), i, num), getLocaleMessage("admin.action.change_priority.title"), JOptionPane.INFORMATION_MESSAGE);

                    }
                }
            }

        }

    }

    @Action
    public void setCurrentLang() {
        for (int i = 0; i < menuLangs.getItemCount(); i++) {
            if (((JRadioButtonMenuItem) menuLangs.getItem(i)).isSelected()) {
                Locales.getInstance().setLangCurrent(((JRadioButtonMenuItem) menuLangs.getItem(i)).getText());
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAddCalendar;
    private javax.swing.JButton buttonCheckZoneBoardServ;
    private javax.swing.JButton buttonClientRequest;
    private javax.swing.JButton buttonCloudTest;
    private javax.swing.JButton buttonDeleteCalendar;
    private javax.swing.ButtonGroup buttonGroupKindNum;
    private javax.swing.ButtonGroup buttonGroupPoint;
    private javax.swing.ButtonGroup buttonGroupSource;
    private javax.swing.ButtonGroup buttonGroupVoice;
    private javax.swing.JButton buttonLock;
    private javax.swing.JButton buttonResetMainTablo;
    private javax.swing.JButton buttonRestart;
    private javax.swing.JButton buttonRestartServer;
    private javax.swing.JButton buttonScheduleAdd;
    private javax.swing.JButton buttonSchedulleDelete;
    private javax.swing.JButton buttonSendDataToSky;
    private javax.swing.JButton buttonServerRequest;
    private javax.swing.JButton buttonShutDown;
    private javax.swing.JButton buttonUnlock;
    private javax.swing.JCheckBox checkBoxAdmin;
    private javax.swing.JCheckBox checkBoxClientAuto;
    private javax.swing.JCheckBox checkBoxReport;
    private javax.swing.JCheckBox checkBoxServerAuto;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem14;
    private javax.swing.JMenuItem jMenuItem15;
    private javax.swing.JMenuItem jMenuItem16;
    private javax.swing.JMenuItem jMenuItem17;
    private javax.swing.JMenuItem jMenuItem18;
    private javax.swing.JMenuItem jMenuItem19;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem20;
    private javax.swing.JMenuItem jMenuItem21;
    private javax.swing.JMenuItem jMenuItem22;
    private javax.swing.JMenuItem jMenuItem23;
    private javax.swing.JMenuItem jMenuItem24;
    private javax.swing.JMenuItem jMenuItem25;
    private javax.swing.JMenuItem jMenuItem26;
    private javax.swing.JMenuItem jMenuItem27;
    private javax.swing.JMenuItem jMenuItem28;
    private javax.swing.JMenuItem jMenuItem29;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem30;
    private javax.swing.JMenuItem jMenuItem31;
    private javax.swing.JMenuItem jMenuItem32;
    private javax.swing.JMenuItem jMenuItem33;
    private javax.swing.JMenuItem jMenuItem34;
    private javax.swing.JMenuItem jMenuItem35;
    private javax.swing.JMenuItem jMenuItem36;
    private javax.swing.JMenuItem jMenuItem37;
    private javax.swing.JMenuItem jMenuItem38;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane19;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JPopupMenu.Separator jSeparator15;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JLabel labelButtonCaption;
    private javax.swing.JLabel labelInfoItem;
    private javax.swing.JLabel labelRespinse;
    private javax.swing.JLabel labelSchedule;
    private javax.swing.JLabel labelServerState;
    private javax.swing.JLabel labelServiceInfo;
    private javax.swing.JLabel labelWelcomeState;
    private javax.swing.JList listCalendar;
    private javax.swing.JList listPostponed;
    private javax.swing.JList listReposts;
    private javax.swing.JList listResponse;
    private javax.swing.JList listResults;
    private javax.swing.JList listSchedule;
    private javax.swing.JList listUserService;
    private javax.swing.JList listUsers;
    private javax.swing.JMenu menuAbout;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem menuItemAbout;
    private javax.swing.JMenuItem menuItemHelp;
    private javax.swing.JMenu menuLangs;
    private javax.swing.JMenu menuServices;
    private javax.swing.JMenu menuUsers;
    private javax.swing.JPasswordField passwordFieldUser;
    private javax.swing.JPopupMenu popupCalendar;
    private javax.swing.JPopupMenu popupInfo;
    private javax.swing.JPopupMenu popupPlans;
    private javax.swing.JPopupMenu popupResponse;
    private javax.swing.JPopupMenu popupResults;
    private javax.swing.JPopupMenu popupServiceUser;
    private javax.swing.JPopupMenu popupServices;
    private javax.swing.JPopupMenu popupUser;
    private javax.swing.JRadioButton rbKindCommon;
    private javax.swing.JRadioButton rbKindPersonal;
    private javax.swing.JRadioButton rbNotificationGong;
    private javax.swing.JRadioButton rbNotificationGongVoice;
    private javax.swing.JRadioButton rbNotificationNo;
    private javax.swing.JRadioButton rbPointOffice;
    private javax.swing.JRadioButton rbPointStoika;
    private javax.swing.JRadioButton rbPointWindow;
    private javax.swing.JSpinner spinnerBranchId;
    private javax.swing.JSpinner spinnerClientPort;
    private javax.swing.JSpinner spinnerFirstNumber;
    private javax.swing.JSpinner spinnerLastNumber;
    private javax.swing.JSpinner spinnerPropClientPort;
    private javax.swing.JSpinner spinnerPropServerPort;
    private javax.swing.JSpinner spinnerServerPort;
    private javax.swing.JSpinner spinnerUserRS;
    private javax.swing.JSpinner spinnerWebServerPort;
    private javax.swing.JSpinner spinnerZonBoadrServPort;
    private javax.swing.JTabbedPane tabbedPaneMain;
    private javax.swing.JTable tableCalendar;
    private javax.swing.JTextField textFieldCalendarName;
    private javax.swing.JTextField textFieldClientAdress;
    private javax.swing.JTextField textFieldFinishTime;
    private javax.swing.JTextField textFieldInfoItemName;
    private javax.swing.JTextField textFieldResponse;
    private javax.swing.JTextField textFieldScheduleName;
    private javax.swing.JTextField textFieldServerAddr;
    private javax.swing.JTextField textFieldStartTime;
    private javax.swing.JTextField textFieldURLWebService;
    private javax.swing.JTextField textFieldUserIdent;
    private javax.swing.JTextField textFieldUserName;
    private javax.swing.JTextField textFieldZonBoadrServAddr;
    private javax.swing.JTextPane textPaneInfoItem;
    private javax.swing.JTextPane textPaneInfoPrint;
    private javax.swing.JTextPane textPaneResponse;
    private javax.swing.JTree treeInfo;
    private javax.swing.JTree treeServices;
    // End of variables declaration//GEN-END:variables
}
