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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.QSystem;
import ru.apertum.qsystem.client.Locales;
import ru.apertum.qsystem.common.CodepagePrintStream;
import ru.apertum.qsystem.common.GsonPool;
import ru.apertum.qsystem.common.exceptions.ClientException;
import ru.apertum.qsystem.hibernate.SqlServers;
import ru.apertum.qsystem.hibernate.SqlServers.SqlServer;
import ru.apertum.qsystem.server.ChangeContext;

/**
 * Created on 25 Май 2009 г., 13:11 Форма конфигурирования подключения к СУБД и COM-порту
 *
 * @author Evgeniy Egorov
 */
public class FServerConfig extends javax.swing.JFrame {

    private static ResourceMap localeMap = null;

    private static String getLocaleMessage(String key) {
        if (localeMap == null) {
            localeMap = Application.getInstance(QSystem.class).getContext().getResourceMap(FServerConfig.class);
        }
        return localeMap.getString(key);
    }

    /**
     * Creates new form FServerConfig
     */
    public FServerConfig() {
        initComponents();

        try {
            setIconImage(ImageIO.read(FAdmin.class.getResource("/ru/apertum/qsystem/client/forms/resources/client.png")));
        } catch (IOException ex) {
            System.err.println(ex);
        }

        final File conff = new File(ChangeContext.filePath);
        final LinkedList<SqlServer> servs;
        if (conff.exists()) {
            String str = "";
            try (FileInputStream fis = new FileInputStream(conff); Scanner s = new Scanner(new InputStreamReader(fis, "UTF-8"))) {
                while (s.hasNextLine()) {
                    final String line = s.nextLine().trim();
                    str += line;
                }
            } catch (IOException ex) {
                System.err.println(ex);
                throw new RuntimeException(ex);
            }
            Gson gson = GsonPool.getInstance().borrowGson();
            try {
                servs = gson.fromJson(str, SqlServers.class).getServers();
                if (servs == null) {
                    throw new RuntimeException("File error.");
                }
            } catch (JsonSyntaxException ex) {
                throw new RuntimeException("Data error. " + ex.toString());
            } finally {
                GsonPool.getInstance().returnGson(gson);
            }
        } else {
            servs = new LinkedList<>();
        }

        final DefaultListModel<SqlServer> model = new DefaultListModel<>();
        for (SqlServer s : servs) {
            model.addElement(s);
        }
        listServs.setModel(model);
        panelParams.setVisible(false);

        listServs.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                final SqlServer ser = (SqlServer) listServs.getSelectedValue();
                if (ser != null) {
                    panelParams.setVisible(true);
                    final String s = ser.getUrl();
                    int st = s.indexOf("://") + 3; // начало адреса   jdbc:mysql://localhost/qsystem?autoReconnect
                    int ssh = s.indexOf("/", st);
                    int ask = s.indexOf("?", st);
                    textFieldServerAdress.setText(s.substring(st, ssh));
                    textFieldBaseName.setText(ask == -1 ? s.substring(ssh + 1) : s.substring(ssh + 1, ask));
                    textFieldUserName.setText(ser.getUser());
                    textFieldPassword.setText(ser.getPassword());
                    cbCurrent.setSelected(ser.isCurrent());
                    cbMain.setSelected(ser.isMain());
                }
            }
        });
        if (servs.size() > 0) {
            listServs.setSelectedIndex(0);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        popListServs = new javax.swing.JPopupMenu();
        miAdd = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        miRemove = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        panelParams = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        textFieldPassword = new javax.swing.JTextField();
        textFieldUserName = new javax.swing.JTextField();
        textFieldBaseName = new javax.swing.JTextField();
        textFieldServerAdress = new javax.swing.JTextField();
        cbCurrent = new javax.swing.JCheckBox();
        cbMain = new javax.swing.JCheckBox();
        buttonSaveServer = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        listServs = new javax.swing.JList();
        buttonRemove = new javax.swing.JButton();
        buttonAdd = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        popListServs.setName("popListServs"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getResourceMap(FServerConfig.class);
        miAdd.setText(resourceMap.getString("miAdd.text")); // NOI18N
        miAdd.setName("miAdd"); // NOI18N
        miAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miAddActionPerformed(evt);
            }
        });
        popListServs.add(miAdd);

        jSeparator1.setName("jSeparator1"); // NOI18N
        popListServs.add(jSeparator1);

        miRemove.setText(resourceMap.getString("miRemove.text")); // NOI18N
        miRemove.setName("miRemove"); // NOI18N
        miRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miRemoveActionPerformed(evt);
            }
        });
        popListServs.add(miRemove);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        panelParams.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("panelParams.border.title"))); // NOI18N
        panelParams.setName("panelParams"); // NOI18N

        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        textFieldPassword.setText(resourceMap.getString("textFieldPassword.text")); // NOI18N
        textFieldPassword.setName("textFieldPassword"); // NOI18N

        textFieldUserName.setText(resourceMap.getString("textFieldUserName.text")); // NOI18N
        textFieldUserName.setName("textFieldUserName"); // NOI18N

        textFieldBaseName.setText(resourceMap.getString("textFieldBaseName.text")); // NOI18N
        textFieldBaseName.setName("textFieldBaseName"); // NOI18N

        textFieldServerAdress.setText(resourceMap.getString("textFieldServerAdress.text")); // NOI18N
        textFieldServerAdress.setName("textFieldServerAdress"); // NOI18N

        cbCurrent.setText(resourceMap.getString("cbCurrent.text")); // NOI18N
        cbCurrent.setName("cbCurrent"); // NOI18N

        cbMain.setText(resourceMap.getString("cbMain.text")); // NOI18N
        cbMain.setName("cbMain"); // NOI18N

        buttonSaveServer.setText(resourceMap.getString("buttonSaveServer.text")); // NOI18N
        buttonSaveServer.setName("buttonSaveServer"); // NOI18N
        buttonSaveServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveServerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelParamsLayout = new javax.swing.GroupLayout(panelParams);
        panelParams.setLayout(panelParamsLayout);
        panelParamsLayout.setHorizontalGroup(
            panelParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelParamsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelParamsLayout.createSequentialGroup()
                        .addComponent(cbMain)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbCurrent))
                    .addGroup(panelParamsLayout.createSequentialGroup()
                        .addGroup(panelParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(textFieldBaseName, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                            .addComponent(textFieldUserName, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                            .addComponent(textFieldPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                            .addComponent(textFieldServerAdress, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jButton3)
                    .addComponent(buttonSaveServer))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelParamsLayout.setVerticalGroup(
            panelParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelParamsLayout.createSequentialGroup()
                .addGroup(panelParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(textFieldServerAdress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldBaseName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(textFieldUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbMain)
                    .addComponent(cbCurrent))
                .addGap(18, 18, 18)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSaveServer)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        listServs.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("listServs.border.title"))); // NOI18N
        listServs.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listServs.setComponentPopupMenu(popListServs);
        listServs.setName("listServs"); // NOI18N
        jScrollPane1.setViewportView(listServs);

        buttonRemove.setText(resourceMap.getString("buttonRemove.text")); // NOI18N
        buttonRemove.setName("buttonRemove"); // NOI18N
        buttonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveActionPerformed(evt);
            }
        });

        buttonAdd.setText(resourceMap.getString("buttonAdd.text")); // NOI18N
        buttonAdd.setName("buttonAdd"); // NOI18N
        buttonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(352, Short.MAX_VALUE)
                        .addComponent(buttonAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonRemove))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 626, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelParams, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelParams, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonRemove)
                    .addComponent(buttonAdd))
                .addContainerGap())
        );

        jPanel3.setBorder(new javax.swing.border.MatteBorder(null));
        jPanel3.setName("jPanel3"); // NOI18N

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onClickOK(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getActionMap(FServerConfig.class, this);
        jButton2.setAction(actionMap.get("quit")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(700, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(17, 17, 17)
                .addComponent(jButton2)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

    /*
    final ComboPooledDataSource cpds = new ComboPooledDataSource();
    try {
    cpds.setDriverClass("com.mysql.jdbc.Driver"); //loads the jdbc driver
    } catch (PropertyVetoException ex) {
    System.err.println(ex);
    JOptionPane.showMessageDialog(this, getLocaleMessage("servercfg.dialog2.title") + "\n" + ex.getMessage() + "\n" + ex, getLocaleMessage("servercfg.dialog2.caption"), JOptionPane.WARNING_MESSAGE);
    throw new RuntimeException(getLocaleMessage("servercfg.bd.fail"), ex);
    }
     * *
     */

    // Название драйвера
    final String driverName = "com.mysql.jdbc.Driver";
    try {
        Class.forName(driverName);
    } catch (ClassNotFoundException ex) {
        System.err.println(ex);
        JOptionPane.showMessageDialog(this, getLocaleMessage("servercfg.dialog2.title") + "\n" + ex.getMessage() + "\n" + ex, getLocaleMessage("servercfg.dialog2.caption"), JOptionPane.WARNING_MESSAGE);
        throw new RuntimeException(getLocaleMessage("servercfg.bd.fail"), ex);
    }

    final String url = "jdbc:mysql://" + textFieldServerAdress.getText() + ("".equals(textFieldBaseName.getText()) ? "" : "/" + textFieldBaseName.getText());// + "?autoReconnect=true&amp;characterEncoding=UTF-8";
    System.out.println(url + "\n" + textFieldUserName.getText() + "\n" + textFieldPassword.getText());

    /*
    cpds.setJdbcUrl(url);
    cpds.setUser(textFieldUserName.getText());
    cpds.setPassword(textFieldPassword.getText());
    cpds.setCheckoutTimeout(20000);
     * *
     */
    Connection con = null;

    try {
        //con = cpds.getConnection();
        con = DriverManager.getConnection(url, textFieldUserName.getText(), textFieldPassword.getText());
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, getLocaleMessage("servercfg.dialog2.title") + ".\nНомер ошибки: " + ex + "\n" + ex, getLocaleMessage("servercfg.dialog2.caption"), JOptionPane.WARNING_MESSAGE);
        throw new RuntimeException(getLocaleMessage("servercfg.bd.fail"), ex);
    } catch (Exception ex) {
        System.err.println(ex);
        JOptionPane.showMessageDialog(this, getLocaleMessage("servercfg.dialog2.title") + ". " + ex + "\n" + ex, getLocaleMessage("servercfg.dialog2.caption"), JOptionPane.WARNING_MESSAGE);
        throw new RuntimeException(getLocaleMessage("servercfg.bd.fail"), ex);
    } finally {
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, getLocaleMessage("servercfg.dialog2.title") + ".\nНомер ошибки: " + ex.getSQLState() + "\n" + ex, getLocaleMessage("servercfg.dialog2.caption"), JOptionPane.WARNING_MESSAGE);
            throw new RuntimeException(getLocaleMessage("servercfg.bd.fail"), ex);
        } finally {
            //cpds.close();
        }
    }
    JOptionPane.showMessageDialog(this, getLocaleMessage("servercfg.dialog3.title"), getLocaleMessage("servercfg.dialog3.caption"), JOptionPane.INFORMATION_MESSAGE);
}//GEN-LAST:event_jButton3ActionPerformed

private void onClickOK(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onClickOK
    // в темповый файл
    try {
        try (FileOutputStream fos = new FileOutputStream(ChangeContext.filePath)) {
            final String message;
            Gson gson = GsonPool.getInstance().borrowGson();
            try {
                final LinkedList<SqlServer> servs = new LinkedList<>();
                for (int i = 0; i < listServs.getModel().getSize(); i++) {
                    servs.add((SqlServer) (listServs.getModel().getElementAt(i)));
                }
                message = gson.toJson(new SqlServers(servs));
            } finally {
                GsonPool.getInstance().returnGson(gson);
            }
            fos.write(message.getBytes("UTF-8"));
            fos.flush();
        }
    } catch (Exception ex) {
        throw new ClientException(ex);
    }
    System.exit(0);
}//GEN-LAST:event_onClickOK

    private void buttonSaveServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveServerActionPerformed
        final SqlServer ser = (SqlServer) listServs.getSelectedValue();
        if (ser != null) {
            String url = ser.getUrl();
            int st = url.indexOf("://") + 3; // начало адреса   jdbc:mysql://localhost/qsystem?autoReconnect
            int ssh = url.indexOf("/", st);
            int ask = url.indexOf("?", st);
            url = url.replace(url.substring(st, ssh), textFieldServerAdress.getText());

            st = url.indexOf("://") + 3; // начало адреса   jdbc:mysql://localhost/qsystem?autoReconnect
            ssh = url.indexOf("/", st);
            ask = url.indexOf("?", st);
            url = url.replace((ask == -1 ? url.substring(ssh + 1) : url.substring(ssh + 1, ask)), textFieldBaseName.getText());

            ser.setUrl(url);
            ser.setUser(textFieldUserName.getText());
            ser.setPassword(textFieldPassword.getText());
            if (cbCurrent.isSelected()) {
                for (int i = 0; i < listServs.getModel().getSize(); i++) {
                    ((SqlServer) (listServs.getModel().getElementAt(i))).setCurrent(false);
                }
            }
            ser.setCurrent(cbCurrent.isSelected());
            if (cbMain.isSelected()) {
                for (int i = 0; i < listServs.getModel().getSize(); i++) {
                    ((SqlServer) (listServs.getModel().getElementAt(i))).setMain(false);
                }
            }
            ser.setMain(cbMain.isSelected());

            final int i = listServs.getSelectedIndex();
            listServs.setModel(listServs.getModel());
            listServs.setSelectedIndex(i);
        }
    }//GEN-LAST:event_buttonSaveServerActionPerformed

    private void miAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miAddActionPerformed
        final String inputData = (String) JOptionPane.showInputDialog(this, "Название нового сервера:", "Добавление нового сервера БД", 3);
        if (inputData == null || inputData.isEmpty()) {
            return;
        }
        ((DefaultListModel) (listServs.getModel())).addElement(new SqlServer(inputData, "root", "root", "jdbc:mysql://127.0.0.1/qsystem?autoReconnect=true&amp;characterEncoding=UTF-8", false, false));
        listServs.setSelectedIndex(listServs.getModel().getSize() - 1);
    }//GEN-LAST:event_miAddActionPerformed

    private void miRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miRemoveActionPerformed
        final SqlServer ser = (SqlServer) listServs.getSelectedValue();
        if (ser != null) {
            ((DefaultListModel) (listServs.getModel())).removeElement(ser);
            listServs.setModel(listServs.getModel());
            if (listServs.getModel().getSize() > 0) {
                listServs.setSelectedIndex(0);
            } else {
                panelParams.setVisible(false);
            }
        }
    }//GEN-LAST:event_miRemoveActionPerformed

    private void buttonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddActionPerformed
        miAddActionPerformed(null);
    }//GEN-LAST:event_buttonAddActionPerformed

    private void buttonRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveActionPerformed
        miRemoveActionPerformed(null);
    }//GEN-LAST:event_buttonRemoveActionPerformed
    static boolean ide = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                //System.out.println(info.getName());
                        /*Metal Nimbus CDE/Motif Windows   Windows Classic  */
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        }

        Locale.setDefault(Locales.getInstance().getLangCurrent());

        for (String str : args) {
            if (str.startsWith("ide")) {
                ide = true;
                break;
            }
        }
        //Установка вывода консольных сообщений в нужной кодировке
        if ("\\".equals(File.separator)) {
            try {
                String consoleEnc = System.getProperty("console.encoding", "Cp866");
                System.setOut(new CodepagePrintStream(System.out, consoleEnc));
                System.setErr(new CodepagePrintStream(System.err, consoleEnc));
            } catch (UnsupportedEncodingException e) {
                System.out.println("Unable to setup console codepage: " + e);
            }
        }

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                final FServerConfig sc = new FServerConfig();
                Uses.setLocation(sc);
                sc.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAdd;
    private javax.swing.JButton buttonRemove;
    private javax.swing.JButton buttonSaveServer;
    private javax.swing.JCheckBox cbCurrent;
    private javax.swing.JCheckBox cbMain;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JList listServs;
    private javax.swing.JMenuItem miAdd;
    private javax.swing.JMenuItem miRemove;
    private javax.swing.JPanel panelParams;
    private javax.swing.JPopupMenu popListServs;
    private javax.swing.JTextField textFieldBaseName;
    private javax.swing.JTextField textFieldPassword;
    private javax.swing.JTextField textFieldServerAdress;
    private javax.swing.JTextField textFieldUserName;
    // End of variables declaration//GEN-END:variables
}
