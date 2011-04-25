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
import java.text.ParseException;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import ru.apertum.qsystem.QSystem;
import ru.apertum.qsystem.common.Uses;import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.apertum.qsystem.server.model.schedule.QSchedule;

/**
 * Created on 27.08.2009, 11:13:04
 * @author Evgeniy Egorov
 */
public class FScheduleChangeDialod extends javax.swing.JDialog {

    private static FScheduleChangeDialod scheduleChangeDialod;

    /** Creates new form FServiceChangeDialod
     * @param parent
     * @param modal
     */
    public FScheduleChangeDialod(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    private static ResourceMap localeMap = null;

    private static String getLocaleMessage(String key) {
        if (localeMap == null) {
            localeMap = Application.getInstance(QSystem.class).getContext().getResourceMap(FScheduleChangeDialod.class);
        }
        return localeMap.getString(key);
    }

    /**
     * <Услуга Наименование="Удостоверение_доверенности" Описание="Удостоверение доверенности" Префикс="Й]" Статус="1" Лимит="3"><![CDATA[<html><b><p align=center><span style='font-size:20.0pt;color:blue'>Удостоверение доверенности</span>]]>
     * </Услуга>
     * Основной метод редактирования услуги.
     * @param parent
     * @param modal
     * @param schedule
     */
    public static void changeSchedule(Frame parent, boolean modal, QSchedule schedule) {
        QLog.l().logger().info("Редактирование услуги \"" + schedule.getName() + "\"");
        if (scheduleChangeDialod == null) {
            scheduleChangeDialod = new FScheduleChangeDialod(parent, modal);
            scheduleChangeDialod.setTitle(getLocaleMessage("dialog.title"));
        }
        scheduleChangeDialod.loadSchedule(schedule);
        Uses.setLocation(scheduleChangeDialod);
        scheduleChangeDialod.setVisible(true);
    }

    private void loadSchedule(QSchedule schedule) {
        this.schedule = schedule;

        c1.setSelected(schedule.getTime_begin_1() != null && schedule.getTime_end_1() != null);
        if (c1.isSelected()) {
            s1.setText(Uses.format_HH_mm.format(schedule.getTime_begin_1()));
            e1.setText(Uses.format_HH_mm.format(schedule.getTime_end_1()));
        }
        c2.setSelected(schedule.getTime_begin_2() != null && schedule.getTime_end_2() != null);
        if (c2.isSelected()) {
            s2.setText(Uses.format_HH_mm.format(schedule.getTime_begin_2()));
            e2.setText(Uses.format_HH_mm.format(schedule.getTime_end_2()));
        }
        c3.setSelected(schedule.getTime_begin_3() != null && schedule.getTime_end_3() != null);
        if (c3.isSelected()) {
            s3.setText(Uses.format_HH_mm.format(schedule.getTime_begin_3()));
            e3.setText(Uses.format_HH_mm.format(schedule.getTime_end_3()));
        }
        c4.setSelected(schedule.getTime_begin_4() != null && schedule.getTime_end_4() != null);
        if (c4.isSelected()) {
            s4.setText(Uses.format_HH_mm.format(schedule.getTime_begin_4()));
            e4.setText(Uses.format_HH_mm.format(schedule.getTime_end_4()));
        }
        c5.setSelected(schedule.getTime_begin_5() != null && schedule.getTime_end_5() != null);
        if (c5.isSelected()) {
            s5.setText(Uses.format_HH_mm.format(schedule.getTime_begin_5()));
            e5.setText(Uses.format_HH_mm.format(schedule.getTime_end_5()));
        }
        c6.setSelected(schedule.getTime_begin_6() != null && schedule.getTime_end_6() != null);
        if (c6.isSelected()) {
            s6.setText(Uses.format_HH_mm.format(schedule.getTime_begin_6()));
            e6.setText(Uses.format_HH_mm.format(schedule.getTime_end_6()));
        }
        c7.setSelected(schedule.getTime_begin_7() != null && schedule.getTime_end_7() != null);
        if (c7.isSelected()) {
            s7.setText(Uses.format_HH_mm.format(schedule.getTime_begin_7()));
            e7.setText(Uses.format_HH_mm.format(schedule.getTime_end_7()));
        }
        radioButtonWeek.setSelected(schedule.getType() == 0);
        radioButtonChet.setSelected(schedule.getType() == 1);
    }
    private QSchedule schedule;

    private void saveService() {
        schedule.setType(radioButtonWeek.isSelected() ? 0 : 1);
        try {
            schedule.setTime_begin_1(c1.isSelected() ? Uses.format_HH_mm.parse(s1.getText()) : null);
            schedule.setTime_end_1(c1.isSelected() ? Uses.format_HH_mm.parse(e1.getText()) : null);
            schedule.setTime_begin_2(c2.isSelected() ? Uses.format_HH_mm.parse(s2.getText()) : null);
            schedule.setTime_end_2(c2.isSelected() ? Uses.format_HH_mm.parse(e2.getText()) : null);
            schedule.setTime_begin_3(c3.isSelected() ? Uses.format_HH_mm.parse(s3.getText()) : null);
            schedule.setTime_end_3(c3.isSelected() ? Uses.format_HH_mm.parse(e3.getText()) : null);
            schedule.setTime_begin_4(c4.isSelected() ? Uses.format_HH_mm.parse(s4.getText()) : null);
            schedule.setTime_end_4(c4.isSelected() ? Uses.format_HH_mm.parse(e4.getText()) : null);
            schedule.setTime_begin_5(c5.isSelected() ? Uses.format_HH_mm.parse(s5.getText()) : null);
            schedule.setTime_end_5(c5.isSelected() ? Uses.format_HH_mm.parse(e5.getText()) : null);
            schedule.setTime_begin_6(c6.isSelected() ? Uses.format_HH_mm.parse(s6.getText()) : null);
            schedule.setTime_end_6(c6.isSelected() ? Uses.format_HH_mm.parse(e6.getText()) : null);
            schedule.setTime_begin_7(c7.isSelected() ? Uses.format_HH_mm.parse(s7.getText()) : null);
            schedule.setTime_end_7(c7.isSelected() ? Uses.format_HH_mm.parse(e7.getText()) : null);
        } catch (ParseException ex) {
            throw new ServerException(ex.toString());
        }

        /*
        service.setPrefix(textFieldPrefix.getText());
        service.setName(textFieldServiceName.getText());
        service.setDescription(textFieldServiceDescript.getText());
        service.setAdvanceLinit((Integer) spinnerLimit.getValue());
        if ("".equals(textFieldButtonCaption.getText())) {
        throw new Uses.ClientException("Поле \"Надпись на кнопке\" не должно быть пустым.");
        }
        if (textFieldButtonCaption.getText().length() > 2500) {
        throw new Uses.ClientException("Текст в поле \"Надпись на кнопке\" не должно быть более 2500 символов.");
        }
        if (textFieldButtonCaption.getText().length() < 2500 && !"".equals(textFieldButtonCaption.getText())) {
        service.setButtonText(textFieldButtonCaption.getText());
        service.setStatus((comboBoxEnabled.getSelectedIndex() - 1) * (-1));
        }
         */
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupTypes = new javax.swing.ButtonGroup();
        panelProps = new javax.swing.JPanel();
        radioButtonWeek = new javax.swing.JRadioButton();
        radioButtonChet = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        c1 = new javax.swing.JCheckBox();
        c2 = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        s1 = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        e1 = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        s2 = new javax.swing.JFormattedTextField();
        jLabel4 = new javax.swing.JLabel();
        e2 = new javax.swing.JFormattedTextField();
        panelWeek = new javax.swing.JPanel();
        c3 = new javax.swing.JCheckBox();
        c4 = new javax.swing.JCheckBox();
        c5 = new javax.swing.JCheckBox();
        c6 = new javax.swing.JCheckBox();
        c7 = new javax.swing.JCheckBox();
        jLabel19 = new javax.swing.JLabel();
        s7 = new javax.swing.JFormattedTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        e3 = new javax.swing.JFormattedTextField();
        e4 = new javax.swing.JFormattedTextField();
        e5 = new javax.swing.JFormattedTextField();
        e6 = new javax.swing.JFormattedTextField();
        e7 = new javax.swing.JFormattedTextField();
        jLabel18 = new javax.swing.JLabel();
        s6 = new javax.swing.JFormattedTextField();
        s5 = new javax.swing.JFormattedTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        s4 = new javax.swing.JFormattedTextField();
        jLabel11 = new javax.swing.JLabel();
        s3 = new javax.swing.JFormattedTextField();
        panelButtons = new javax.swing.JPanel();
        buttonSave = new javax.swing.JButton();
        buttonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        panelProps.setBorder(new javax.swing.border.MatteBorder(null));
        panelProps.setName("panelProps"); // NOI18N

        buttonGroupTypes.add(radioButtonWeek);
        radioButtonWeek.setSelected(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getResourceMap(FScheduleChangeDialod.class);
        radioButtonWeek.setText(resourceMap.getString("radioButtonWeek.text")); // NOI18N
        radioButtonWeek.setName("radioButtonWeek"); // NOI18N
        radioButtonWeek.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                radioButtonWeekStateChanged(evt);
            }
        });

        buttonGroupTypes.add(radioButtonChet);
        radioButtonChet.setText(resourceMap.getString("radioButtonChet.text")); // NOI18N
        radioButtonChet.setName("radioButtonChet"); // NOI18N

        jPanel1.setBorder(new javax.swing.border.MatteBorder(null));
        jPanel1.setName("jPanel1"); // NOI18N

        c1.setText(resourceMap.getString("c1.text")); // NOI18N
        c1.setName("c1"); // NOI18N

        c2.setText(resourceMap.getString("c2.text")); // NOI18N
        c2.setName("c2"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        s1.setText(resourceMap.getString("s1.text")); // NOI18N
        s1.setName("s1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        e1.setText(resourceMap.getString("e1.text")); // NOI18N
        e1.setName("e1"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        s2.setText(resourceMap.getString("s2.text")); // NOI18N
        s2.setName("s2"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        e2.setText(resourceMap.getString("e2.text")); // NOI18N
        e2.setName("e2"); // NOI18N

        panelWeek.setBorder(new javax.swing.border.MatteBorder(null));
        panelWeek.setName("panelWeek"); // NOI18N

        c3.setText(resourceMap.getString("c3.text")); // NOI18N
        c3.setName("c3"); // NOI18N

        c4.setText(resourceMap.getString("c4.text")); // NOI18N
        c4.setName("c4"); // NOI18N

        c5.setText(resourceMap.getString("c5.text")); // NOI18N
        c5.setName("c5"); // NOI18N

        c6.setText(resourceMap.getString("c6.text")); // NOI18N
        c6.setName("c6"); // NOI18N

        c7.setText(resourceMap.getString("c7.text")); // NOI18N
        c7.setName("c7"); // NOI18N

        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        s7.setText(resourceMap.getString("s7.text")); // NOI18N
        s7.setName("s7"); // NOI18N

        jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N

        jLabel21.setText(resourceMap.getString("jLabel21.text")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N

        jLabel22.setText(resourceMap.getString("jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N

        jLabel23.setText(resourceMap.getString("jLabel23.text")); // NOI18N
        jLabel23.setName("jLabel23"); // NOI18N

        jLabel24.setText(resourceMap.getString("jLabel24.text")); // NOI18N
        jLabel24.setName("jLabel24"); // NOI18N

        e3.setText(resourceMap.getString("e3.text")); // NOI18N
        e3.setName("e3"); // NOI18N

        e4.setText(resourceMap.getString("e4.text")); // NOI18N
        e4.setName("e4"); // NOI18N

        e5.setText(resourceMap.getString("e5.text")); // NOI18N
        e5.setName("e5"); // NOI18N

        e6.setText(resourceMap.getString("e6.text")); // NOI18N
        e6.setName("e6"); // NOI18N

        e7.setText(resourceMap.getString("e7.text")); // NOI18N
        e7.setName("e7"); // NOI18N

        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        s6.setText(resourceMap.getString("s6.text")); // NOI18N
        s6.setName("s6"); // NOI18N

        s5.setText(resourceMap.getString("s5.text")); // NOI18N
        s5.setName("s5"); // NOI18N

        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        s4.setText(resourceMap.getString("s4.text")); // NOI18N
        s4.setName("s4"); // NOI18N

        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        s3.setText(resourceMap.getString("s3.text")); // NOI18N
        s3.setName("s3"); // NOI18N

        javax.swing.GroupLayout panelWeekLayout = new javax.swing.GroupLayout(panelWeek);
        panelWeek.setLayout(panelWeekLayout);
        panelWeekLayout.setHorizontalGroup(
            panelWeekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelWeekLayout.createSequentialGroup()
                .addGroup(panelWeekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(c7)
                    .addComponent(c6)
                    .addComponent(c5)
                    .addComponent(c4)
                    .addComponent(c3))
                .addGap(18, 18, 18)
                .addGroup(panelWeekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelWeekLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(18, 18, 18)
                        .addComponent(s3, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))
                    .addGroup(panelWeekLayout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addGap(18, 18, 18)
                        .addComponent(s4, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))
                    .addGroup(panelWeekLayout.createSequentialGroup()
                        .addGroup(panelWeekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel19)
                            .addComponent(jLabel18)
                            .addComponent(jLabel17))
                        .addGap(19, 19, 19)
                        .addGroup(panelWeekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(s6, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                            .addComponent(s7, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                            .addComponent(s5, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE))))
                .addGap(18, 18, 18)
                .addGroup(panelWeekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelWeekLayout.createSequentialGroup()
                        .addComponent(jLabel24)
                        .addGap(18, 18, 18)
                        .addComponent(e3, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelWeekLayout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addGap(18, 18, 18)
                        .addComponent(e4, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE))
                    .addGroup(panelWeekLayout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addGap(18, 18, 18)
                        .addComponent(e5, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE))
                    .addGroup(panelWeekLayout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addGap(18, 18, 18)
                        .addComponent(e6, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE))
                    .addGroup(panelWeekLayout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addGap(18, 18, 18)
                        .addComponent(e7, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelWeekLayout.setVerticalGroup(
            panelWeekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelWeekLayout.createSequentialGroup()
                .addGroup(panelWeekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(c3)
                    .addComponent(jLabel24)
                    .addComponent(e3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(s3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelWeekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(c4)
                    .addComponent(jLabel23)
                    .addComponent(e4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(s4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelWeekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(c5)
                    .addComponent(jLabel22)
                    .addComponent(e5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(s5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelWeekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(c6)
                    .addComponent(jLabel21)
                    .addComponent(e6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18)
                    .addComponent(s6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelWeekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(c7)
                    .addComponent(s7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20)
                    .addComponent(e7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelWeek, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(c1)
                            .addComponent(c2))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(s2, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(s1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(17, 17, 17)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(18, 18, 18)
                                .addComponent(e2))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(e1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(c1)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(e1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(s1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(c2)
                    .addComponent(s2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(e2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelWeek, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelPropsLayout = new javax.swing.GroupLayout(panelProps);
        panelProps.setLayout(panelPropsLayout);
        panelPropsLayout.setHorizontalGroup(
            panelPropsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPropsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPropsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(radioButtonWeek)
                    .addComponent(radioButtonChet))
                .addContainerGap())
        );
        panelPropsLayout.setVerticalGroup(
            panelPropsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPropsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radioButtonWeek)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonChet)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
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
                .addContainerGap(158, Short.MAX_VALUE)
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
            .addComponent(panelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelProps, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void radioButtonWeekStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_radioButtonWeekStateChanged
        c1.setText(radioButtonWeek.isSelected() ? getLocaleMessage("c1.text") : getLocaleMessage("dialog.parity"));
        c2.setText(radioButtonWeek.isSelected() ? getLocaleMessage("c2.text") : getLocaleMessage("dialog.not_parity"));
        panelWeek.setVisible(radioButtonWeek.isSelected());
    }//GEN-LAST:event_radioButtonWeekStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.ButtonGroup buttonGroupTypes;
    private javax.swing.JButton buttonSave;
    private javax.swing.JCheckBox c1;
    private javax.swing.JCheckBox c2;
    private javax.swing.JCheckBox c3;
    private javax.swing.JCheckBox c4;
    private javax.swing.JCheckBox c5;
    private javax.swing.JCheckBox c6;
    private javax.swing.JCheckBox c7;
    private javax.swing.JFormattedTextField e1;
    private javax.swing.JFormattedTextField e2;
    private javax.swing.JFormattedTextField e3;
    private javax.swing.JFormattedTextField e4;
    private javax.swing.JFormattedTextField e5;
    private javax.swing.JFormattedTextField e6;
    private javax.swing.JFormattedTextField e7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel panelProps;
    private javax.swing.JPanel panelWeek;
    private javax.swing.JRadioButton radioButtonChet;
    private javax.swing.JRadioButton radioButtonWeek;
    private javax.swing.JFormattedTextField s1;
    private javax.swing.JFormattedTextField s2;
    private javax.swing.JFormattedTextField s3;
    private javax.swing.JFormattedTextField s4;
    private javax.swing.JFormattedTextField s5;
    private javax.swing.JFormattedTextField s6;
    private javax.swing.JFormattedTextField s7;
    // End of variables declaration//GEN-END:variables
}
