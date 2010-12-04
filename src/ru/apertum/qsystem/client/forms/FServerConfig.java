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

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.ActionMap;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.QSystem;
import ru.apertum.qsystem.common.CodepagePrintStream;

/**
 * Created on 25 Май 2009 г., 13:11
 * Форма конфигурирования подключения к СУБД и COM-порту
 * @author Evgeniy Egorov
 */
public class FServerConfig extends javax.swing.JFrame {

    private Element root = null;

    /** Creates new form FServerConfig */
    public FServerConfig() {
        initComponents();
        final SAXReader reader = new SAXReader(false);

        try {
            root = reader.read(filePath).getRootElement();
        } catch (DocumentException ex) {
            JOptionPane.showMessageDialog(this, "Невозможно прочитать файл \"" + filePath + "\"" + ex, "Ошибка.", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        final ArrayList<Element> com = Uses.elementsByAttr(root, "id", "serialPort");
        isCOM = !com.isEmpty();
        if (isCOM) {
            /*<constructor-arg index="0" value="COM3" />
            <property name="speed" value="9600" />
            <property name="dataBits" value="8" />
            <property name="parity" value="0" />
            <property name="stopBits" value="2" />*/
            comName = Uses.elementsByAttr(com.get(0), "index", "0").get(0);
            setComboValue(comboBoxPortName, comName.attributeValue("value"));
            comSpeed = Uses.elementsByAttr(com.get(0), "name", "speed").get(0);
            setComboValue(comboBoxSpeed, comSpeed.attributeValue("value"));
            comDatabits = Uses.elementsByAttr(com.get(0), "name", "dataBits").get(0);
            setComboValue(comboBoxDataBits, comDatabits.attributeValue("value"));
            comParity = Uses.elementsByAttr(com.get(0), "name", "parity").get(0);
            switch (Integer.parseInt(comParity.attributeValue("value"))) {
                /*PARITY_NONE  = 0;
                PARITY_ODD = 1;
                PARITY_EVEN = 2;
                PARITY_MARK = 3;
                PARITY_SPACE = 4;*/
                case 0:
                    comboBoxEven.setSelectedIndex(0);
                    break;
                case 1:
                    comboBoxEven.setSelectedIndex(1);
                    break;
                case 2:
                    comboBoxEven.setSelectedIndex(2);
                    break;
                case 3:
                    comboBoxEven.setSelectedIndex(3);
                    break;
                case 4:
                    comboBoxEven.setSelectedIndex(4);
                    break;
            }
            comStopBits = Uses.elementsByAttr(com.get(0), "name", "stopBits").get(0);
            setComboValue(comboBoxStopBits, comStopBits.attributeValue("value"));
        }
        final ArrayList<Element> db = Uses.elementsByAttr(root, "id", "c3p0DataSource");
        isDB = !db.isEmpty();
        if (isDB) {
            /*<property name="url" value="jdbc:mysql://192.168.0.251/qsystem?characterEncoding=UTF-8"/>
            <property name="username" value="admin"/>
            <property name="password" value="123"/>*/
            dbUrl = Uses.elementsByAttr(db.get(0), "name", "jdbcUrl").get(0);
            final String s = dbUrl.attributeValue("value");
            int st = s.indexOf("://") + 3; // начало адреса
            int ssh = s.indexOf("/", st);
            int ask = s.indexOf("?", st);
            if (ssh > ask || ssh == -1) {
                ssh = ask;
            }

            textFieldServerAdress.setText(s.substring(st, ssh));
            if (s.indexOf("/", st) == -1) {
                ssh = ask;
            } else {
                ssh++;
            }
            textFieldBaseName.setText(s.substring(ssh, ask));
            dbUser = Uses.elementsByAttr(db.get(0), "name", "user").get(0);
            textFieldUserName.setText(dbUser.attributeValue("value"));
            dbPassword = Uses.elementsByAttr(db.get(0), "name", "password").get(0);
            textFieldPassword.setText(dbPassword.attributeValue("value"));
        }
    }
    private boolean isCOM;
    private boolean isDB;
    private Element comName;
    private Element comSpeed;
    private Element comDatabits;
    private Element comParity;
    private Element comStopBits;
    private Element dbUrl;
    private Element dbUser;
    private Element dbPassword;

    private void setComboValue(JComboBox cb, String value) {
        for (int i = 0; i < cb.getModel().getSize(); i++) {
            if (value.equals(cb.getModel().getElementAt(i))) {
                cb.setSelectedIndex(i);
                return;
            }
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        textFieldServerAdress = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        textFieldBaseName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        textFieldUserName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        textFieldPassword = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        comboBoxPortName = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        comboBoxSpeed = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        comboBoxEven = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        comboBoxDataBits = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        comboBoxStopBits = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        ResourceMap resourceMap = Application.getInstance(QSystem.class).getContext().getResourceMap(FServerConfig.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        textFieldServerAdress.setText(resourceMap.getString("textFieldServerAdress.text")); // NOI18N
        textFieldServerAdress.setName("textFieldServerAdress"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        textFieldBaseName.setText(resourceMap.getString("textFieldBaseName.text")); // NOI18N
        textFieldBaseName.setName("textFieldBaseName"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        textFieldUserName.setText(resourceMap.getString("textFieldUserName.text")); // NOI18N
        textFieldUserName.setName("textFieldUserName"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        textFieldPassword.setText(resourceMap.getString("textFieldPassword.text")); // NOI18N
        textFieldPassword.setName("textFieldPassword"); // NOI18N

        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(textFieldBaseName, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                    .addComponent(textFieldUserName, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                    .addComponent(textFieldPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                    .addComponent(textFieldServerAdress, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(45, 45, 45))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(134, Short.MAX_VALUE)
                .addComponent(jButton3)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(textFieldServerAdress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldBaseName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(textFieldUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton3)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        comboBoxPortName.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "/dev/ttyS0", "/dev/ttyS1", "/dev/ttyS2", "/dev/ttyS3", "/dev/ttyS4", "/dev/ttyS5", "/dev/ttyS6", "/dev/ttyS7", "/dev/ttyS8", "/dev/ttyS9" }));
        comboBoxPortName.setName("comboBoxPortName"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        comboBoxSpeed.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "75", "110", "134", "150", "300", "600", "1200", "1800", "2400", "4800", "7200", "9600", "14400", "19200", "38400", "57600", "115200", "128000" }));
        comboBoxSpeed.setName("comboBoxSpeed"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        comboBoxEven.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Нет", "Нечет", "Чет", "Маркер", "Пробел" }));
        comboBoxEven.setName("comboBoxEven"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        comboBoxDataBits.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "5", "6", "7", "8" }));
        comboBoxDataBits.setName("comboBoxDataBits"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        comboBoxStopBits.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "1.5", "2" }));
        comboBoxStopBits.setName("comboBoxStopBits"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5))
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(comboBoxStopBits, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(comboBoxDataBits, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(comboBoxEven, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(comboBoxSpeed, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(comboBoxPortName, 0, 103, Short.MAX_VALUE))
                .addContainerGap(88, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(comboBoxPortName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(comboBoxSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(comboBoxEven, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(comboBoxDataBits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(comboBoxStopBits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        ActionMap actionMap = Application.getInstance(QSystem.class).getContext().getActionMap(FServerConfig.class, this);
        jButton2.setAction(actionMap.get("quit")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(122, Short.MAX_VALUE)
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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

    final ComboPooledDataSource cpds = new ComboPooledDataSource();
    try {
        cpds.setDriverClass("com.mysql.jdbc.Driver"); //loads the jdbc driver
    } catch (PropertyVetoException ex) {
        System.err.println(ex);
        JOptionPane.showMessageDialog(this, "Соединение с базой данных не удачно.\n" + ex.getMessage() + "\n" + ex, "Проверки соединения с БД", JOptionPane.WARNING_MESSAGE);
        throw new RuntimeException("Соединение с базой данных не удачно", ex);
    }
    cpds.setJdbcUrl("jdbc:mysql://" + textFieldServerAdress.getText() + ("".equals(textFieldBaseName.getText()) ? "" : "/" + textFieldBaseName.getText()) + "?autoReconnect=true&amp;characterEncoding=UTF-8");
    cpds.setUser(textFieldUserName.getText());
    cpds.setPassword(textFieldPassword.getText());
    cpds.setCheckoutTimeout(2000);

    Connection con = null;
    try {
        con = cpds.getConnection();
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Соединение с базой данных не удачно.\nНомер ошибки: " + ex + "\n" + ex, "Проверки соединения с БД", JOptionPane.WARNING_MESSAGE);
        throw new RuntimeException("Соединение с базой данных не удачно", ex);
    } catch (Exception ex) {
        System.err.println(ex);
        JOptionPane.showMessageDialog(this, "Соединение с базой данных не удачно. " + ex + "\n" + ex, "Проверки соединения с БД", JOptionPane.WARNING_MESSAGE);
        throw new RuntimeException("Соединение с базой данных не удачно", ex);
    } finally {
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Соединение с базой данных закрыто не удачно.\nНомер ошибки: " + ex.getSQLState() + "\n" + ex, "Проверки соединения с БД", JOptionPane.WARNING_MESSAGE);
            throw new RuntimeException("Соединение с базой данных не удачно", ex);
        } finally {
            cpds.close();
        }
    }
    JOptionPane.showMessageDialog(this, "Соединение с базой прошло успешно.", "Проверки соединения с БД", JOptionPane.INFORMATION_MESSAGE);
}//GEN-LAST:event_jButton3ActionPerformed

private void onClickOK(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onClickOK
    if (isCOM) {
        /*<constructor-arg index="0" value="COM3" />
        <property name="speed" value="9600" />
        <property name="dataBits" value="8" />
        <property name="parity" value="0" />
        <property name="stopBits" value="2" />*/
        comName.addAttribute("value", comboBoxPortName.getSelectedItem().toString());

        comSpeed.addAttribute("value", comboBoxSpeed.getSelectedItem().toString());
        comDatabits.addAttribute("value", comboBoxDataBits.getSelectedItem().toString());

        String str = "0";
        switch (comboBoxEven.getSelectedIndex()) {
            /*PARITY_NONE  = 0;
            PARITY_ODD = 1;
            PARITY_EVEN = 2;
            PARITY_MARK = 3;
            PARITY_SPACE = 4;*/
            case 0:
                str = "0";
                break;
            case 1:
                str = "1";
                break;
            case 2:
                str = "2";
                break;
            case 3:
                str = "3";
                break;
            case 4:
                str = "4";
                break;
        }
        comParity.addAttribute("value", str);
        comStopBits.addAttribute("value", comboBoxStopBits.getSelectedItem().toString());
    }
    if (isDB) {
        /*<property name="url" value="jdbc:mysql://192.168.0.251/qsystem?characterEncoding=UTF-8"/>
        <property name="username" value="admin"/>
        <property name="password" value="123"/>*/
        dbUrl.addAttribute("value", "jdbc:mysql://" + textFieldServerAdress.getText() + ("".equals(textFieldBaseName.getText()) ? "" : "/" + textFieldBaseName.getText()) + "?characterEncoding=UTF-8");
        dbUser.addAttribute("value", textFieldUserName.getText());
        dbPassword.addAttribute("value", textFieldPassword.getText());
    }
    // в темповый файл
    final FileOutputStream fos;
    try {
        fos = new FileOutputStream(filePath);
    } catch (FileNotFoundException ex) {
        throw new Uses.ClientException("Не возможно создать временный файл состояния. " + ex.getMessage());
    }
    try {
        fos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"spring-beans-2.0.dtd\">\n" + root.asXML()).getBytes("UTF-8"));
        fos.flush();
        fos.close();
    } catch (IOException ex) {
        throw new Uses.ClientException("Не возможно сохранить изменения в поток." + ex.getMessage());
    }
    System.exit(0);
}//GEN-LAST:event_onClickOK
    private static String filePath;

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

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

        if (args.length == 0) {
            System.out.println("No param file context.");
            return;
        }
        final File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("File context not exist.");
            return;
        }
        filePath = args[0];

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
    private javax.swing.JComboBox comboBoxDataBits;
    private javax.swing.JComboBox comboBoxEven;
    private javax.swing.JComboBox comboBoxPortName;
    private javax.swing.JComboBox comboBoxSpeed;
    private javax.swing.JComboBox comboBoxStopBits;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTextField textFieldBaseName;
    private javax.swing.JTextField textFieldPassword;
    private javax.swing.JTextField textFieldServerAdress;
    private javax.swing.JTextField textFieldUserName;
    // End of variables declaration//GEN-END:variables
}
