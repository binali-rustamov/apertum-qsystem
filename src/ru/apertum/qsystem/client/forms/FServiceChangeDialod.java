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

import java.awt.Frame;
import javax.swing.ComboBoxModel;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import ru.apertum.qsystem.QSystem;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.exceptions.ClientException;
import ru.apertum.qsystem.server.model.QService;
import ru.apertum.qsystem.server.model.calendar.QCalendar;
import ru.apertum.qsystem.server.model.schedule.QSchedule;

/**
 * Created on 27.08.2009, 11:13:04
 * @author Evgeniy Egorov
 */
public class FServiceChangeDialod extends javax.swing.JDialog {

    private static ResourceMap localeMap = null;

    private static String getLocaleMessage(String key) {
        if (localeMap == null) {
            localeMap = Application.getInstance(QSystem.class).getContext().getResourceMap(FServiceChangeDialod.class);
        }
        return localeMap.getString(key);
    }
    private static FServiceChangeDialod serviceChangeDialod;

    /** Creates new form FServiceChangeDialod
     * @param parent родительская форма
     * @param modal модальность
     */
    public FServiceChangeDialod(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    /**
     * <Услуга Наименование="Удостоверение_доверенности" Описание="Удостоверение доверенности" Префикс="Й]" Статус="1" Лимит="3"><![CDATA[<html><b><p align=center><span style='font-size:20.0pt;color:blue'>Удостоверение доверенности</span>]]>
     * </Услуга>
     * Основной метод редактирования услуги.
     * @param parent родительская форма
     * @param modal модальность
     * @param service Услуга для редактирования в XML-виде
     * @param scheduleModel
     * @param calendarModel
     */
    public static void changeService(Frame parent, boolean modal, QService service, ComboBoxModel scheduleModel, ComboBoxModel calendarModel) {
        QLog.l().logger().info("Редактирование услуги \"" + service.getName() + "\"");
        if (serviceChangeDialod == null) {
            serviceChangeDialod = new FServiceChangeDialod(parent, modal);
            serviceChangeDialod.setTitle("Редактирование параметров услуги");
        }
        serviceChangeDialod.comboBoxSchedule.setModel(scheduleModel);
        serviceChangeDialod.comboBoxCalendar.setModel(calendarModel);
        serviceChangeDialod.loadService(service);
        Uses.setLocation(serviceChangeDialod);
        serviceChangeDialod.setVisible(true);
    }

    private void loadService(QService service) {
        this.service = service;
        textFieldPrefix.setText(service.getPrefix());
        textFieldPrefix.setEditable(service.isLeaf());
        textFieldServiceName.setText(service.getName());
        textFieldServiceName.setEditable(service.isRoot());
        textFieldServiceDescript.setText(service.getDescription());
        textAreaButtonCaption.setText(service.getButtonText());
        textAreaInfoHtml.setText(service.getPreInfoHtml());
        textAreaTextPrint.setText(service.getPreInfoPrintText());
        comboBoxEnabled.setSelectedIndex(service.getStatus() * (-1) + 1);
        comboBoxEnabled.setEnabled(!service.isRoot());
        spinnerLimit.setValue(service.getAdvanceLimit());
        spinnerLimitPeriod.setValue(service.getAdvanceLimitPeriod());
        if (service.getSchedule() == null) {
            comboBoxSchedule.setSelectedIndex(-1);
        } else {
            comboBoxSchedule.getModel().setSelectedItem(service.getSchedule());
        }
        if (service.getCalendar() == null) {
            comboBoxCalendar.setSelectedIndex(-1);
        } else {
            comboBoxCalendar.getModel().setSelectedItem(service.getCalendar());
        }
        сheckBoxInputRequired.setSelected(service.getInput_required());
        textFieldInputCaption.setText(service.getInput_caption());
        сheckBoxResultRequired.setSelected(service.getResult_required());
        
        textAreaButtonCaptionKeyPressed(null);
        textAreaInfoHtmlKeyReleased(null);
    }
    private QService service;

    private void saveService() {
        service.setPrefix(textFieldPrefix.getText());
        service.setName(textFieldServiceName.getText());
        service.setDescription(textFieldServiceDescript.getText());
        service.setAdvanceLinit((Integer) spinnerLimit.getValue());
        service.setAdvanceLimitPeriod((Integer) spinnerLimitPeriod.getValue() < 0 ? 0 : (Integer) spinnerLimitPeriod.getValue());
        if ("".equals(textAreaButtonCaption.getText())) {
            throw new ClientException(getLocaleMessage("dialog.message1"));
        }
        if (textAreaButtonCaption.getText().length() > 2500) {
            throw new ClientException(getLocaleMessage("dialog.message2"));
        }
        if (textAreaInfoHtml.getText().length() > 50000) {
            throw new ClientException(getLocaleMessage("dialog.message3"));
        }
        if (textAreaTextPrint.getText().length() > 50000) {
            throw new ClientException(getLocaleMessage("dialog.message4"));
        }
        if (textAreaButtonCaption.getText().length() < 2500 && !"".equals(textAreaButtonCaption.getText())) {
            service.setButtonText(textAreaButtonCaption.getText());
        }
        service.setStatus((comboBoxEnabled.getSelectedIndex() - 1) * (-1));
        service.setSchedule((QSchedule) comboBoxSchedule.getModel().getSelectedItem());
        service.setCalendar((QCalendar) comboBoxCalendar.getModel().getSelectedItem());
        service.setInput_required(сheckBoxInputRequired.isSelected());
        service.setInput_caption(textFieldInputCaption.getText());
        service.setResult_required(сheckBoxResultRequired.isSelected());
        service.setPreInfoHtml(textAreaInfoHtml.getText());
        service.setPreInfoPrintText(textAreaTextPrint.getText());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelProps = new javax.swing.JPanel();
        comboBoxEnabled = new javax.swing.JComboBox();
        jLabel21 = new javax.swing.JLabel();
        textFieldPrefix = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        textFieldServiceName = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        textFieldServiceDescript = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        spinnerLimit = new javax.swing.JSpinner();
        spinnerLimitPeriod = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        сheckBoxResultRequired = new javax.swing.JCheckBox();
        сheckBoxInputRequired = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        textFieldInputCaption = new javax.swing.JTextField();
        comboBoxSchedule = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        comboBoxCalendar = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        textAreaButtonCaption = new javax.swing.JTextArea();
        labelCaptionButton = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jSplitPane3 = new javax.swing.JSplitPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        textAreaTextPrint = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        textAreaInfoHtml = new javax.swing.JTextArea();
        labelInfoDialog = new javax.swing.JLabel();
        panelButtons = new javax.swing.JPanel();
        buttonSave = new javax.swing.JButton();
        buttonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        panelProps.setBorder(new javax.swing.border.MatteBorder(null));
        panelProps.setName("panelProps"); // NOI18N

        comboBoxEnabled.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Активна", "Неактивна", "Невидима" }));
        comboBoxEnabled.setName("comboBoxEnabled"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getResourceMap(FServiceChangeDialod.class);
        jLabel21.setText(resourceMap.getString("jLabel21.text")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N

        textFieldPrefix.setText(resourceMap.getString("textFieldPrefix.text")); // NOI18N
        textFieldPrefix.setName("textFieldPrefix"); // NOI18N

        jLabel22.setText(resourceMap.getString("jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N

        textFieldServiceName.setEditable(false);
        textFieldServiceName.setText(resourceMap.getString("textFieldServiceName.text")); // NOI18N
        textFieldServiceName.setName("textFieldServiceName"); // NOI18N

        jLabel23.setText(resourceMap.getString("jLabel23.text")); // NOI18N
        jLabel23.setName("jLabel23"); // NOI18N

        textFieldServiceDescript.setText(resourceMap.getString("textFieldServiceDescript.text")); // NOI18N
        textFieldServiceDescript.setName("textFieldServiceDescript"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        spinnerLimit.setName("spinnerLimit"); // NOI18N

        spinnerLimitPeriod.setName("spinnerLimitPeriod"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        сheckBoxResultRequired.setText(resourceMap.getString("сheckBoxResultRequired.text")); // NOI18N
        сheckBoxResultRequired.setName("сheckBoxResultRequired"); // NOI18N

        сheckBoxInputRequired.setText(resourceMap.getString("сheckBoxInputRequired.text")); // NOI18N
        сheckBoxInputRequired.setName("сheckBoxInputRequired"); // NOI18N
        сheckBoxInputRequired.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                сheckBoxInputRequiredStateChanged(evt);
            }
        });
        сheckBoxInputRequired.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                сheckBoxInputRequiredActionPerformed(evt);
            }
        });

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        textFieldInputCaption.setText(resourceMap.getString("textFieldInputCaption.text")); // NOI18N
        textFieldInputCaption.setName("textFieldInputCaption"); // NOI18N

        comboBoxSchedule.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboBoxSchedule.setName("comboBoxSchedule"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        comboBoxCalendar.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboBoxCalendar.setName("comboBoxCalendar"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(сheckBoxInputRequired)
                    .addComponent(сheckBoxResultRequired)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel8))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(spinnerLimitPeriod, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spinnerLimit, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(comboBoxCalendar, 0, 587, Short.MAX_VALUE)
                            .addComponent(comboBoxSchedule, 0, 587, Short.MAX_VALUE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textFieldInputCaption, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
                            .addComponent(jLabel4))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(spinnerLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinnerLimitPeriod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(сheckBoxResultRequired)
                .addGap(18, 18, 18)
                .addComponent(сheckBoxInputRequired)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(7, 7, 7)
                .addComponent(textFieldInputCaption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboBoxSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboBoxCalendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addContainerGap(154, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jPanel4.setName("jPanel4"); // NOI18N

        jLabel24.setText(resourceMap.getString("jLabel24.text")); // NOI18N
        jLabel24.setName("jLabel24"); // NOI18N

        jSplitPane2.setDividerLocation(180);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setName("jSplitPane2"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        textAreaButtonCaption.setColumns(20);
        textAreaButtonCaption.setFont(resourceMap.getFont("textAreaButtonCaption.font")); // NOI18N
        textAreaButtonCaption.setRows(5);
        textAreaButtonCaption.setText(resourceMap.getString("textAreaButtonCaption.text")); // NOI18N
        textAreaButtonCaption.setName("textAreaButtonCaption"); // NOI18N
        textAreaButtonCaption.setPreferredSize(new java.awt.Dimension(485, 50));
        textAreaButtonCaption.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textAreaButtonCaptionKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(textAreaButtonCaption);

        jSplitPane2.setTopComponent(jScrollPane1);

        labelCaptionButton.setText(resourceMap.getString("labelCaptionButton.text")); // NOI18N
        labelCaptionButton.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelCaptionButton.setName("labelCaptionButton"); // NOI18N
        jSplitPane2.setRightComponent(labelCaptionButton);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)
                    .addComponent(jLabel24))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        jPanel5.setName("jPanel5"); // NOI18N

        jSplitPane3.setDividerLocation(260);
        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane3.setContinuousLayout(true);
        jSplitPane3.setName("jSplitPane3"); // NOI18N

        jSplitPane1.setBorder(new javax.swing.border.MatteBorder(null));
        jSplitPane1.setDividerLocation(131);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setContinuousLayout(true);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        textAreaTextPrint.setColumns(20);
        textAreaTextPrint.setRows(5);
        textAreaTextPrint.setName("textAreaTextPrint"); // NOI18N
        jScrollPane3.setViewportView(textAreaTextPrint);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addContainerGap(404, Short.MAX_VALUE))
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE))
        );

        jSplitPane1.setBottomComponent(jPanel1);

        jPanel2.setName("jPanel2"); // NOI18N

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        textAreaInfoHtml.setColumns(20);
        textAreaInfoHtml.setRows(5);
        textAreaInfoHtml.setName("textAreaInfoHtml"); // NOI18N
        textAreaInfoHtml.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textAreaInfoHtmlKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(textAreaInfoHtml);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addContainerGap(451, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))
        );

        jSplitPane1.setLeftComponent(jPanel2);

        jSplitPane3.setTopComponent(jSplitPane1);

        labelInfoDialog.setText(resourceMap.getString("labelInfoDialog.text")); // NOI18N
        labelInfoDialog.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        labelInfoDialog.setName("labelInfoDialog"); // NOI18N
        jSplitPane3.setRightComponent(labelInfoDialog);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(resourceMap.getString("jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        javax.swing.GroupLayout panelPropsLayout = new javax.swing.GroupLayout(panelProps);
        panelProps.setLayout(panelPropsLayout);
        panelPropsLayout.setHorizontalGroup(
            panelPropsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPropsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPropsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPropsLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(8, 8, 8)
                        .addComponent(comboBoxEnabled, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel21)
                        .addGap(18, 18, 18)
                        .addComponent(textFieldPrefix, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelPropsLayout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(textFieldServiceName, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPropsLayout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addGap(10, 10, 10)
                        .addComponent(textFieldServiceDescript, javax.swing.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 682, Short.MAX_VALUE)
        );
        panelPropsLayout.setVerticalGroup(
            panelPropsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPropsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPropsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldServiceName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPropsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboBoxEnabled, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel21)
                    .addComponent(textFieldPrefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPropsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldServiceDescript, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE))
        );

        panelButtons.setBorder(new javax.swing.border.MatteBorder(null));
        panelButtons.setName("panelButtons"); // NOI18N

        buttonSave.setText(resourceMap.getString("buttonSave.text")); // NOI18N
        buttonSave.setName("buttonSave"); // NOI18N
        buttonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveActionPerformed(evt);
            }
        });

        buttonCancel.setText(resourceMap.getString("buttonCancel.text")); // NOI18N
        buttonCancel.setName("buttonCancel"); // NOI18N
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelButtonsLayout = new javax.swing.GroupLayout(panelButtons);
        panelButtons.setLayout(panelButtonsLayout);
        panelButtonsLayout.setHorizontalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelButtonsLayout.createSequentialGroup()
                .addContainerGap(498, Short.MAX_VALUE)
                .addComponent(buttonSave)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonCancel)
                .addContainerGap())
        );
        panelButtonsLayout.setVerticalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelButtonsLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonSave))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelProps, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(panelProps, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveActionPerformed
        saveService();
        setVisible(false);
    }//GEN-LAST:event_buttonSaveActionPerformed

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void сheckBoxInputRequiredActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_сheckBoxInputRequiredActionPerformed
    }//GEN-LAST:event_сheckBoxInputRequiredActionPerformed

    private void сheckBoxInputRequiredStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_сheckBoxInputRequiredStateChanged
        textFieldInputCaption.setEnabled(сheckBoxInputRequired.isSelected());
    }//GEN-LAST:event_сheckBoxInputRequiredStateChanged

    private void textAreaButtonCaptionKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textAreaButtonCaptionKeyPressed
        labelCaptionButton.setText(textAreaButtonCaption.getText());
    }//GEN-LAST:event_textAreaButtonCaptionKeyPressed

    private void textAreaInfoHtmlKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textAreaInfoHtmlKeyReleased
        labelInfoDialog.setText(textAreaInfoHtml.getText());
    }//GEN-LAST:event_textAreaInfoHtmlKeyReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonSave;
    private javax.swing.JComboBox comboBoxCalendar;
    private javax.swing.JComboBox comboBoxEnabled;
    private javax.swing.JComboBox comboBoxSchedule;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelCaptionButton;
    private javax.swing.JLabel labelInfoDialog;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel panelProps;
    private javax.swing.JSpinner spinnerLimit;
    private javax.swing.JSpinner spinnerLimitPeriod;
    private javax.swing.JTextArea textAreaButtonCaption;
    private javax.swing.JTextArea textAreaInfoHtml;
    private javax.swing.JTextArea textAreaTextPrint;
    private javax.swing.JTextField textFieldInputCaption;
    private javax.swing.JTextField textFieldPrefix;
    private javax.swing.JTextField textFieldServiceDescript;
    private javax.swing.JTextField textFieldServiceName;
    private javax.swing.JCheckBox сheckBoxInputRequired;
    private javax.swing.JCheckBox сheckBoxResultRequired;
    // End of variables declaration//GEN-END:variables
}
