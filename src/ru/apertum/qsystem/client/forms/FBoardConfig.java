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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JFrame;
import org.dom4j.Element;
import ru.apertum.qsystem.client.help.Helper;
import ru.apertum.qsystem.common.Uses;

/**
 * Created on 10 Апрель 2009 г., 10:27
 * Конфигурирование главного табло на плазме или ЖК
 * @author Evgeniy Egorov
 */
public class FBoardConfig extends AFBoardRedactor {

    private Element topElement;
    private Element bottomElement;
    private Element leftElement;
    private Element rightElement;
    private Element mainElement;

    /** Creates new form FBoardConfig
     * @param parent
     * @param modal
     */
    public FBoardConfig(JFrame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {
                setDividerLocation();
            }
        });
        //привязка помощи к форме.
        final Helper helper = Helper.getHelp("ru/apertum/qsystem/client/help/admin.hs");
        helper.enableHelpKey(spUp, "editTablo");
    }
    private static FBoardConfig boardConfig;

    public static FBoardConfig getBoardConfig(JFrame parent, boolean modal) {
        if (boardConfig == null || (parent != boardConfig.parent || modal != boardConfig.modal)) {
            boardConfig = new FBoardConfig(parent, modal);
        }
        return boardConfig;
    }

    /**
     * Обновить параметры
     */
    @Override
    protected void refresh() {
        topElement = getParams().element(Uses.TAG_BOARD_TOP);
        bottomElement = getParams().element(Uses.TAG_BOARD_BOTTOM);
        leftElement = getParams().element(Uses.TAG_BOARD_LEFT);
        rightElement = getParams().element(Uses.TAG_BOARD_RIGHT);
        mainElement = getParams().element(Uses.TAG_BOARD_MAIN);
        //выставим размеры и видимость
        checkBoxUp.setSelected("1".equals(topElement.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL)));
        checkBoxLeft.setSelected("1".equals(leftElement.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL)));
        checkBoxRight.setSelected("1".equals(rightElement.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL)));
        checkBoxDown.setSelected("1".equals(bottomElement.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL)));

        setDividerLocation();

    }

    private void setDividerLocation() {
        spUp.setDividerLocation(Double.parseDouble(topElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE)));
        spDown.setDividerLocation(Double.parseDouble(bottomElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE)));
        spLeft.setDividerLocation(Double.parseDouble(leftElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE)));
        spRight.setDividerLocation(Double.parseDouble(rightElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE)));
    }

    @Override
    public void saveResult() throws IOException {
        saveForm();
        super.saveResult();
    }

    private void refreshPanel(Element elem) {
    }

    protected void saveForm() {
        // visible="1" Размер="35"
        topElement.addAttribute(Uses.TAG_BOARD_VISIBLE_PANEL, checkBoxUp.isSelected() ? "1" : "0");
        leftElement.addAttribute(Uses.TAG_BOARD_VISIBLE_PANEL, checkBoxLeft.isSelected() ? "1" : "0");
        rightElement.addAttribute(Uses.TAG_BOARD_VISIBLE_PANEL, checkBoxRight.isSelected() ? "1" : "0");
        bottomElement.addAttribute(Uses.TAG_BOARD_VISIBLE_PANEL, checkBoxDown.isSelected() ? "1" : "0");

        topElement.addAttribute(Uses.TAG_BOARD_PANEL_SIZE, String.valueOf(Uses.roundAs(new Double(spUp.getDividerLocation()) / (spUp.getHeight() + 0.009), 2)));
        leftElement.addAttribute(Uses.TAG_BOARD_PANEL_SIZE, String.valueOf(Uses.roundAs(new Double(spLeft.getDividerLocation()) / (spLeft.getWidth() + 0.009), 2)));
        rightElement.addAttribute(Uses.TAG_BOARD_PANEL_SIZE, String.valueOf(Uses.roundAs(new Double(spRight.getDividerLocation()) / spRight.getWidth() + 0.009, 2)));
        bottomElement.addAttribute(Uses.TAG_BOARD_PANEL_SIZE, String.valueOf(Uses.roundAs(new Double(spDown.getDividerLocation()) / spDown.getHeight() + 0.009, 2)));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        spUp = new javax.swing.JSplitPane();
        panelUp = new javax.swing.JPanel();
        checkBoxUp = new javax.swing.JCheckBox();
        labelUp = new javax.swing.JLabel();
        buttonTop = new javax.swing.JButton();
        spDown = new javax.swing.JSplitPane();
        panelDown = new javax.swing.JPanel();
        checkBoxDown = new javax.swing.JCheckBox();
        labelDown = new javax.swing.JLabel();
        buttonDown = new javax.swing.JButton();
        spLeft = new javax.swing.JSplitPane();
        panelLeft = new javax.swing.JPanel();
        checkBoxLeft = new javax.swing.JCheckBox();
        buttonLeft = new javax.swing.JButton();
        labelLeft = new javax.swing.JLabel();
        spRight = new javax.swing.JSplitPane();
        panelRight = new javax.swing.JPanel();
        checkBoxRight = new javax.swing.JCheckBox();
        labelRight = new javax.swing.JLabel();
        buttonRight = new javax.swing.JButton();
        panelMain = new javax.swing.JPanel();
        buttonMain = new javax.swing.JButton();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getResourceMap(FBoardConfig.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        spUp.setDividerLocation(150);
        spUp.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        spUp.setContinuousLayout(true);
        spUp.setName("spUp"); // NOI18N

        panelUp.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelUp.setName("panelUp"); // NOI18N
        panelUp.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panelUpComponentResized(evt);
            }
        });

        checkBoxUp.setText(resourceMap.getString("checkBoxUp.text")); // NOI18N
        checkBoxUp.setToolTipText(resourceMap.getString("checkBoxUp.toolTipText")); // NOI18N
        checkBoxUp.setName("checkBoxUp"); // NOI18N

        labelUp.setText(resourceMap.getString("labelUp.text")); // NOI18N
        labelUp.setName("labelUp"); // NOI18N

        buttonTop.setText(resourceMap.getString("buttonTop.text")); // NOI18N
        buttonTop.setToolTipText(resourceMap.getString("buttonTop.toolTipText")); // NOI18N
        buttonTop.setName("buttonTop"); // NOI18N
        buttonTop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTopActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelUpLayout = new javax.swing.GroupLayout(panelUp);
        panelUp.setLayout(panelUpLayout);
        panelUpLayout.setHorizontalGroup(
            panelUpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelUpLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxUp)
                .addGap(6, 6, 6)
                .addComponent(labelUp)
                .addGap(18, 18, 18)
                .addComponent(buttonTop)
                .addContainerGap(546, Short.MAX_VALUE))
        );
        panelUpLayout.setVerticalGroup(
            panelUpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelUpLayout.createSequentialGroup()
                .addGroup(panelUpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelUpLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panelUpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(buttonTop, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelUp)))
                    .addComponent(checkBoxUp))
                .addContainerGap(119, Short.MAX_VALUE))
        );

        spUp.setTopComponent(panelUp);

        spDown.setDividerLocation(250);
        spDown.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        spDown.setContinuousLayout(true);
        spDown.setName("spDown"); // NOI18N

        panelDown.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelDown.setName("panelDown"); // NOI18N
        panelDown.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panelDownComponentResized(evt);
            }
        });

        checkBoxDown.setText(resourceMap.getString("checkBoxDown.text")); // NOI18N
        checkBoxDown.setToolTipText(resourceMap.getString("checkBoxDown.toolTipText")); // NOI18N
        checkBoxDown.setName("checkBoxDown"); // NOI18N

        labelDown.setText(resourceMap.getString("labelDown.text")); // NOI18N
        labelDown.setName("labelDown"); // NOI18N

        buttonDown.setText(resourceMap.getString("buttonDown.text")); // NOI18N
        buttonDown.setToolTipText(resourceMap.getString("buttonDown.toolTipText")); // NOI18N
        buttonDown.setName("buttonDown"); // NOI18N
        buttonDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDownActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelDownLayout = new javax.swing.GroupLayout(panelDown);
        panelDown.setLayout(panelDownLayout);
        panelDownLayout.setHorizontalGroup(
            panelDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDownLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxDown)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelDown)
                .addGap(18, 18, 18)
                .addComponent(buttonDown)
                .addContainerGap(544, Short.MAX_VALUE))
        );
        panelDownLayout.setVerticalGroup(
            panelDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDownLayout.createSequentialGroup()
                .addGroup(panelDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDownLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panelDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelDown)
                            .addComponent(buttonDown, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(checkBoxDown))
                .addContainerGap(122, Short.MAX_VALUE))
        );

        spDown.setBottomComponent(panelDown);

        spLeft.setDividerLocation(150);
        spLeft.setContinuousLayout(true);
        spLeft.setName("spLeft"); // NOI18N

        panelLeft.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelLeft.setName("panelLeft"); // NOI18N
        panelLeft.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panelLeftComponentResized(evt);
            }
        });

        checkBoxLeft.setText(resourceMap.getString("checkBoxLeft.text")); // NOI18N
        checkBoxLeft.setToolTipText(resourceMap.getString("checkBoxLeft.toolTipText")); // NOI18N
        checkBoxLeft.setName("checkBoxLeft"); // NOI18N

        buttonLeft.setText(resourceMap.getString("buttonLeft.text")); // NOI18N
        buttonLeft.setToolTipText(resourceMap.getString("buttonLeft.toolTipText")); // NOI18N
        buttonLeft.setName("buttonLeft"); // NOI18N
        buttonLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLeftActionPerformed(evt);
            }
        });

        labelLeft.setText(resourceMap.getString("labelLeft.text")); // NOI18N
        labelLeft.setName("labelLeft"); // NOI18N

        javax.swing.GroupLayout panelLeftLayout = new javax.swing.GroupLayout(panelLeft);
        panelLeft.setLayout(panelLeftLayout);
        panelLeftLayout.setHorizontalGroup(
            panelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLeftLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonLeft)
                    .addComponent(labelLeft)
                    .addComponent(checkBoxLeft))
                .addContainerGap(88, Short.MAX_VALUE))
        );
        panelLeftLayout.setVerticalGroup(
            panelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLeftLayout.createSequentialGroup()
                .addComponent(checkBoxLeft)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelLeft)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonLeft, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(180, Short.MAX_VALUE))
        );

        spLeft.setLeftComponent(panelLeft);

        spRight.setDividerLocation(320);
        spRight.setContinuousLayout(true);
        spRight.setName("spRight"); // NOI18N

        panelRight.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelRight.setName("panelRight"); // NOI18N
        panelRight.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panelRightComponentResized(evt);
            }
        });

        checkBoxRight.setText(resourceMap.getString("checkBoxRight.text")); // NOI18N
        checkBoxRight.setToolTipText(resourceMap.getString("checkBoxRight.toolTipText")); // NOI18N
        checkBoxRight.setName("checkBoxRight"); // NOI18N

        labelRight.setText(resourceMap.getString("labelRight.text")); // NOI18N
        labelRight.setName("labelRight"); // NOI18N

        buttonRight.setText(resourceMap.getString("buttonRight.text")); // NOI18N
        buttonRight.setToolTipText(resourceMap.getString("buttonRight.toolTipText")); // NOI18N
        buttonRight.setName("buttonRight"); // NOI18N
        buttonRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRightActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelRightLayout = new javax.swing.GroupLayout(panelRight);
        panelRight.setLayout(panelRightLayout);
        panelRightLayout.setHorizontalGroup(
            panelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRightLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonRight)
                    .addComponent(labelRight)
                    .addComponent(checkBoxRight))
                .addContainerGap(137, Short.MAX_VALUE))
        );
        panelRightLayout.setVerticalGroup(
            panelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRightLayout.createSequentialGroup()
                .addComponent(checkBoxRight)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelRight)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonRight, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(178, Short.MAX_VALUE))
        );

        spRight.setRightComponent(panelRight);

        panelMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        panelMain.setName("panelMain"); // NOI18N

        buttonMain.setText(resourceMap.getString("buttonMain.text")); // NOI18N
        buttonMain.setToolTipText(resourceMap.getString("buttonMain.toolTipText")); // NOI18N
        buttonMain.setName("buttonMain"); // NOI18N
        buttonMain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMainActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(buttonMain)
                .addContainerGap(258, Short.MAX_VALUE))
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(buttonMain, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(215, Short.MAX_VALUE))
        );

        spRight.setLeftComponent(panelMain);

        spLeft.setRightComponent(spRight);

        spDown.setLeftComponent(spLeft);

        spUp.setRightComponent(spDown);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spUp, javax.swing.GroupLayout.DEFAULT_SIZE, 684, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spUp, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void panelUpComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelUpComponentResized

    labelUp.setText(String.valueOf(Math.round(new Double(panelUp.getHeight()) / (spUp.getHeight()) * 100)) + "%");
}//GEN-LAST:event_panelUpComponentResized

private void panelLeftComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelLeftComponentResized

    labelLeft.setText(String.valueOf(Math.round(new Double(panelLeft.getWidth()) / (spUp.getWidth()) * 100)) + "%");
}//GEN-LAST:event_panelLeftComponentResized

private void panelRightComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelRightComponentResized

    labelRight.setText(String.valueOf(Math.round(new Double(panelRight.getWidth()) / (panelUp.getWidth()) * 100)) + "%");
}//GEN-LAST:event_panelRightComponentResized

private void panelDownComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelDownComponentResized

    labelDown.setText(String.valueOf(Math.round(new Double(panelDown.getHeight()) / (spUp.getHeight()) * 100)) + "%");
}//GEN-LAST:event_panelDownComponentResized

private void buttonMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMainActionPerformed

    FParamsEditor.changeParams(this.parent, true, mainElement, "Основные параметры");
}//GEN-LAST:event_buttonMainActionPerformed

private void buttonRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRightActionPerformed

    FBoardParams.changeParams(this.parent, rightElement, "Правая часть табло");

}//GEN-LAST:event_buttonRightActionPerformed

private void buttonDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDownActionPerformed

    FBoardParams.changeParams(this.parent, bottomElement, "Нижняя часть табло");

}//GEN-LAST:event_buttonDownActionPerformed

private void buttonLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLeftActionPerformed


    FBoardParams.changeParams(this.parent, leftElement, "Левая часть табло");

}//GEN-LAST:event_buttonLeftActionPerformed

private void buttonTopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTopActionPerformed


    FBoardParams.changeParams(this.parent, topElement, "Верхняя часть табло");

}//GEN-LAST:event_buttonTopActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonDown;
    private javax.swing.JButton buttonLeft;
    private javax.swing.JButton buttonMain;
    private javax.swing.JButton buttonRight;
    private javax.swing.JButton buttonTop;
    private javax.swing.JCheckBox checkBoxDown;
    private javax.swing.JCheckBox checkBoxLeft;
    private javax.swing.JCheckBox checkBoxRight;
    private javax.swing.JCheckBox checkBoxUp;
    private javax.swing.JLabel labelDown;
    private javax.swing.JLabel labelLeft;
    private javax.swing.JLabel labelRight;
    private javax.swing.JLabel labelUp;
    private javax.swing.JPanel panelDown;
    private javax.swing.JPanel panelLeft;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelRight;
    private javax.swing.JPanel panelUp;
    private javax.swing.JSplitPane spDown;
    private javax.swing.JSplitPane spLeft;
    private javax.swing.JSplitPane spRight;
    private javax.swing.JSplitPane spUp;
    // End of variables declaration//GEN-END:variables
}
