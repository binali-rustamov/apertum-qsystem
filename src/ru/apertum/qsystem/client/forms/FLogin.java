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

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.dom4j.Element;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import ru.apertum.qsystem.client.help.Helper;
import ru.apertum.qsystem.common.model.NetCommander;
import ru.apertum.qsystem.client.model.QPanel;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.model.INetProperty;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.QUserList;
import ru.apertum.qsystem.QSystem;

/**
 * Created on 27 Февраль 2009 г., 14:32
 * @author Evgeniy Egorov
 */
public class FLogin extends javax.swing.JDialog {

    /**
     * Результат
     */
    private static boolean ok = false;
    /**
     * Количество неудачных попыток, если 0 то бесконечно
     */
    private static int count = 0;
    private static int was = 0;
    /**
     * Параметры соединения.
     */
    private INetProperty netProperty;
    private QUserList userList;
    private JFrame parent;
    /**
     * Описание юзера по его названию.
     */
    private HashMap<String, Element> usersEls = new HashMap<String, Element>();
    /**
     * Уровни логирования
     */
    public static final int LEVEL_USER = 1;
    public static final int LEVEL_REPORT = 2;
    public static final int LEVEL_ADMIN = 3;
    /**
     * текущий уровень доступа для диалога
     */
    private int level = LEVEL_USER;

    final public int getLevel() {
        return level;
    }

    final public void setLevel(int level) {
        this.level = level;
        passwordField.setText("");
        switch (level) {
            case LEVEL_USER:
                labelLavel.setText(LABEL_USER);
                break;
            case LEVEL_REPORT:
                labelLavel.setText(LABEL_REPORT);
                break;
            case LEVEL_ADMIN:
                labelLavel.setText(LABEL_ADMIN);
                break;
            default:
                level = LEVEL_USER;
                labelLavel.setText(LABEL_USER);
        }
    }
    private static ResourceMap localeMap = null;

    private static String getLocaleMessage(String key) {
        if (localeMap == null) {
            localeMap = Application.getInstance(QSystem.class).getContext().getResourceMap(FLogin.class);
        }
        return localeMap.getString(key);
    }
    /**
     * Надпись о доступе
     */
    private static final String LABEL_USER = " " + getLocaleMessage("messages.access.work");
    private static final String LABEL_REPORT = " " + getLocaleMessage("messages.access.report");
    private static final String LABEL_ADMIN = " " + getLocaleMessage("messages.access.admin");
    /**
     * Используемая ссылка на диалоговое окно.
     */
    private static FLogin loginForm;

    /** Creates new form FLogin
     * @param netProperty
     * @param parent
     * @param modal
     * @param level
     */
    public FLogin(INetProperty netProperty, JFrame parent, boolean modal, int level) {
        super(parent, modal);
        initComponents();
        setSize(500, 375);
        setAlwaysOnTop(true);
        this.netProperty = netProperty;
        this.parent = parent;
        this.userGetter = new GetUserFromServer();
        setLevel(level);

        final Element users = NetCommander.getUsers(netProperty);

        final List<Element> elUsers = users.elements(Uses.TAG_USER);

        String str = "";
        final HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < elUsers.size(); i++) {
            final Element usr = elUsers.get(i);
            final String adm = usr.attributeValue(Uses.TAG_USER_ADMIN_ACCESS);
            final String rpt = usr.attributeValue(Uses.TAG_USER_REPORT_ACCESS);
            boolean flag = false;
            switch (getLevel()) {
                case LEVEL_ADMIN:
                    flag = "1".equals(adm) || "true".equals(adm.toLowerCase());
                    break;
                case LEVEL_REPORT:
                    flag = "1".equals(rpt) || "true".equals(rpt.toLowerCase());
                    break;
                case LEVEL_USER:
                    flag = !Uses.elements(usr, Uses.TAG_PROP_OWN_SRV).isEmpty();
                    break;
            }

            if (flag) {
                final String s = elUsers.get(i).attributeValue(Uses.TAG_NAME);
                str = str + s + '#';
                map.put(s, i);
                usersEls.put(s, elUsers.get(i));
            }
        }
        afterCreate(str);
    }

    /**
     * Чтоб не дублировать код
     * @param str список пользователей
     */
    private void afterCreate(String str) {
        DefaultComboBoxModel m = new DefaultComboBoxModel(str.split("#"));
        comboBoxUser.setModel(m);
        //привязка помощи к форме.
        final Helper helper = Helper.getHelp(level == LEVEL_ADMIN ? "ru/apertum/qsystem/client/help/admin.hs" : "ru/apertum/qsystem/client/help/client.hs");
        helper.enableHelpKey(jPanel1, "loginning");
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {
                labelLavel.setLocation(2, getHeight() - 15);
            }
        });
    }

    /** Creates new form FLogin
     * @param userList
     * @param parent
     * @param modal
     * @param level 
     */
    public FLogin(QUserList userList, JFrame parent, boolean modal, int level) {
        super(parent, modal);
        initComponents();
        setSize(500, 375);
        setAlwaysOnTop(true);
        this.userList = userList;
        this.parent = parent;
        this.userGetter = new GetUserFromList();
        setLevel(level);

        String str = "";
        for (int i = 0; i < userList.size(); i++) {
            final QUser usr = (QUser) userList.get(i);
            boolean flag = false;
            switch (getLevel()) {
                case LEVEL_ADMIN:
                    flag = usr.getAdminAccess();
                    break;
                case LEVEL_REPORT:
                    flag = usr.getReportAccess();
                    break;
                case LEVEL_USER:
                    flag = !usr.getPlanServices().isEmpty();
                    break;
            }

            if (flag) {
                str = str + usr.getName() + '#';
            }
        }
        afterCreate(str);
    }

    /**
     * Логирование без предварительно созданного списка пользователей.
     * Этот список определяется путем отправки задания на сервер.
     * @param netProperty свойства коннекта
     * @param owner относительно этого контрола модальность и позиционирование
     * @param modal режим модальности
     * @param count количество неудачных попыток, если 0 то бесконечно
     * @param level Уровень доступа, см. LEVEL_USER, LEVEL_REPORT, LEVEL_ADMIN
     * @return XML-описание залогиневшегося юзера.
     */
    public static Element logining(INetProperty netProperty, JFrame owner, boolean modal, int count, int level) {
        Uses.log.logger.info("Вход в систему.");
        if (loginForm == null) {
            loginForm = new FLogin(netProperty, owner, modal, level);
        } else {
            if (loginForm.netProperty != netProperty || loginForm.parent != owner || loginForm.getLevel() != level) {
                loginForm = new FLogin(netProperty, owner, modal, level);
            }
        }
        FLogin.count = count;
        if (owner == null) {
            // Отцентирируем
            final Toolkit kit = Toolkit.getDefaultToolkit();
            loginForm.setLocation((Math.round(kit.getScreenSize().width - loginForm.getWidth()) / 2),
                    (Math.round(kit.getScreenSize().height - loginForm.getHeight()) / 2));
        }
        Uses.closeSplash();
        loginForm.setVisible(true);
        if (!ok) {
            System.exit(0);
        }
        Uses.log.logger.info("Вход в систему выполнен. Пользователь \"" + loginForm.comboBoxUser.getSelectedItem() + "\", уровень доступа \"" + level + "\".");
        return loginForm.usersEls.get((String) loginForm.comboBoxUser.getSelectedItem());
    }

    /**
     * Логирование имея уже готовый список возможных пользователей для логирования.
     * @param userList список пользователей
     * @param owner относительно этого контрола модальность и позиционирование
     * @param modal режим модальности
     * @param count количество неудачных попыток, если 0 то бесконечно
     * @param level Уровень доступа, см. LEVEL_USER, LEVEL_REPORT, LEVEL_ADMIN
     * @return XML-описание залогиневшегося юзера.
     */
    public static Element logining(QUserList userList, JFrame owner, boolean modal, int count, int level) {
        Uses.log.logger.info("Вход в систему.");
        if (loginForm == null) {
            loginForm = new FLogin(userList, owner, modal, level);
        } else {
            if (loginForm.userList != userList || loginForm.parent != owner || loginForm.getLevel() != level) {
                loginForm = new FLogin(userList, owner, modal, level);
            }
        }
        FLogin.count = count;
        if (owner == null) {
            // Отцентирируем
            final Toolkit kit = Toolkit.getDefaultToolkit();
            loginForm.setLocation((Math.round(kit.getScreenSize().width - loginForm.getWidth()) / 2),
                    (Math.round(kit.getScreenSize().height - loginForm.getHeight()) / 2));
        }
        Uses.closeSplash();
        loginForm.setVisible(true);
        if (!ok) {
            System.exit(0);
        }
        Uses.log.logger.info("Вход в систему выполнен. Пользователь \"" + loginForm.comboBoxUser.getSelectedItem() + "\", уровень доступа \"" + level + "\".");
        return loginForm.userGetter.getUser();
    }

    /**
     * Получить выбранного юзера по его имени для разных случаев.
     */
    private interface IGetUser {

        public Element getUser();
    }
    private IGetUser userGetter;

    private class GetUserFromServer implements IGetUser {

        @Override
        public Element getUser() {
            return usersEls.get((String) comboBoxUser.getSelectedItem());
        }
    }

    private class GetUserFromList implements IGetUser {

        @Override
        public Element getUser() {
            return userList.getByName((String) comboBoxUser.getSelectedItem()).getXML();
        }
    }

    private boolean checkLogin() {
        final Element user = userGetter.getUser();
        final String adm = user.attributeValue(Uses.TAG_USER_ADMIN_ACCESS);
        final String rpt = user.attributeValue(Uses.TAG_USER_REPORT_ACCESS);
        switch (getLevel()) {
            case LEVEL_ADMIN:
                if (!("1".equals(adm) || "true".equals(adm.toLowerCase()))) {
                    JOptionPane.showMessageDialog(this, getLocaleMessage("messages.noAccess.mess"), getLocaleMessage("messages.noAccess.caption"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
            case LEVEL_REPORT:
                if (!("1".equals(rpt) || "true".equals(rpt.toLowerCase()))) {
                    JOptionPane.showMessageDialog(this, getLocaleMessage("messages.noAccess.mess"), getLocaleMessage("messages.noAccess.caption"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
            case LEVEL_USER:
                break;
            default:
                throw new Uses.ClientException("Нет такого уровня доступа.");

        }

        final String userPass = user.attributeValue(Uses.TAG_PASSWORD);
        if (!userPass.equals(new String(passwordField.getPassword()))) {
            JOptionPane.showMessageDialog(this, getLocaleMessage("messages.noAccessUser.mess"), getLocaleMessage("messages.noAccess.caption"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    @Action
    public void pressOK() {
        ok = checkLogin();
        if (ok) {
            this.setVisible(false);
        } else {
            was++;
            if (was == count) {
                System.exit(0);
            }
        }
    }

    @Action
    public void pressCancel() {
        System.exit(0);
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new QPanel("/ru/apertum/qsystem/client/forms/resources/fon_login.jpg");
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        comboBoxUser = new javax.swing.JComboBox();
        passwordField = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        buttonEnter = new javax.swing.JButton();
        buttonExit = new javax.swing.JButton();
        labelLavel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N
        setResizable(false);
        setUndecorated(true);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                keyPress(evt);
            }
        });

        jPanel1.setBorder(new javax.swing.border.MatteBorder(null));
        jPanel1.setName("jPanel1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getResourceMap(FLogin.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        comboBoxUser.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboBoxUser.setName("comboBoxUser"); // NOI18N
        comboBoxUser.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                comboBoxUserKeyPressed(evt);
            }
        });

        passwordField.setText(resourceMap.getString("passwordField.text")); // NOI18N
        passwordField.setName("passwordField"); // NOI18N
        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                passwordFieldKeyPressed(evt);
            }
        });

        jLabel3.setIcon(resourceMap.getIcon("jLabel3.icon")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getActionMap(FLogin.class, this);
        buttonEnter.setAction(actionMap.get("pressOK")); // NOI18N
        buttonEnter.setName("buttonEnter"); // NOI18N
        buttonEnter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                buttonEnterKeyPressed(evt);
            }
        });

        buttonExit.setAction(actionMap.get("pressCancel")); // NOI18N
        buttonExit.setName("buttonExit"); // NOI18N
        buttonExit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                buttonExitKeyPressed(evt);
            }
        });

        labelLavel.setFont(resourceMap.getFont("labelLavel.font")); // NOI18N
        labelLavel.setForeground(resourceMap.getColor("labelLavel.foreground")); // NOI18N
        labelLavel.setText(resourceMap.getString("labelLavel.text")); // NOI18N
        labelLavel.setName("labelLavel"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passwordField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboBoxUser, javax.swing.GroupLayout.Alignment.LEADING, 0, 193, Short.MAX_VALUE))
                .addContainerGap(135, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(171, 171, 171)
                .addComponent(buttonEnter, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonExit)
                .addContainerGap(251, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(69, 69, 69)
                .addComponent(labelLavel, javax.swing.GroupLayout.PREFERRED_SIZE, 373, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(126, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(122, 122, 122)
                        .addComponent(jLabel3))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(comboBoxUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(50, 50, 50)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonExit)
                    .addComponent(buttonEnter))
                .addGap(32, 32, 32)
                .addComponent(labelLavel)
                .addContainerGap(83, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(37, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void keyPress(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keyPress

    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        pressOK();
    }
    if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
        System.exit(0);
    }
}//GEN-LAST:event_keyPress

private void passwordFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordFieldKeyPressed

    keyPress(evt);
}//GEN-LAST:event_passwordFieldKeyPressed

private void comboBoxUserKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_comboBoxUserKeyPressed

    keyPress(evt);
}//GEN-LAST:event_comboBoxUserKeyPressed

private void buttonEnterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_buttonEnterKeyPressed

    keyPress(evt);
}//GEN-LAST:event_buttonEnterKeyPressed

private void buttonExitKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_buttonExitKeyPressed

    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        pressCancel();
    }
    if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
        System.exit(0);
    }
}//GEN-LAST:event_buttonExitKeyPressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonEnter;
    private javax.swing.JButton buttonExit;
    private javax.swing.JComboBox comboBoxUser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel labelLavel;
    private javax.swing.JPasswordField passwordField;
    // End of variables declaration//GEN-END:variables
}
