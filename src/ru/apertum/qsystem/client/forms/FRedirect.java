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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.tree.TreeNode;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.model.NetCommander;
import ru.apertum.qsystem.common.model.INetProperty;
import ru.apertum.qsystem.QSystem;
import ru.apertum.qsystem.common.exceptions.ClientException;
import ru.apertum.qsystem.server.model.ISailListener;
import ru.apertum.qsystem.server.model.QService;
import ru.apertum.qsystem.server.model.QServiceTree;

/**
 * Диалог получения названия услуги для переадресации и сопутствующих параметров.
 * Created on 29 Сентябрь 2008 г., 10:30
 * Класс диалоговой формы с выбором требуемой услуги из выпадающего списка.
 * Также сдесь можно разместить сопутствующие элементы ввода др. параметров.
 * @author Evgeniy Egorov
 */
public class FRedirect extends JDialog {

    /**
     * Результат
     */
    private static boolean ok;
    /**
     * Используемая ссылка на диалоговое окно.
     */
    private static FRedirect servicesForm;
    private static ResourceMap localeMap = null;

    private static String getLocaleMessage(String key) {
        if (localeMap == null) {
            localeMap = Application.getInstance(QSystem.class).getContext().getResourceMap(FRedirect.class);
        }
        return localeMap.getString(key);
    }

    /** Creates new form FRedirect
     * @param netProperty
     * @param owner 
     */
    public FRedirect(INetProperty netProperty, JFrame owner) {
        super(owner, getLocaleMessage("redirect.caption"), true);
        initComponents();

        buttonOk.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ok = true;
                setVisible(false);
            }
        });
        buttonCancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ok = false;
                setVisible(false);
            }
        });

        final QService service = NetCommander.getServiсes(netProperty).getRoot();
        if (service == null) {
            throw new ClientException("Невозможно получить список предлагаемых услуг.");
        }
        QServiceTree.sailToStorm(service, new ISailListener() {

            @Override
            public void actionPerformed(TreeNode service) {
                if (service.isLeaf()) {
                    comboBoxServices.addItem(((QService)service).getName());
                    ids.put(((QService)service).getName(), ((QService)service).getId());
                }
            }
        });

        comboBoxServices.setSelectedIndex(0);

        setLocation(Math.round(owner.getLocation().x + owner.getWidth() / 2 - getWidth() / 2),
                Math.round(owner.getLocation().y + owner.getHeight() / 2 - getHeight() / 2));
    }

    private final HashMap<String, Long> ids = new HashMap<String, Long>();

    /**
     * Выбор услуги для перенаправления.
     * @param netProperty свойства коннекта
     * @param owner относительно этого контрола модальность и позиционирование
     * @param tempComments это если были комменты с прошлых редиректов
     * @param onlyComments показывать или нет что-то другое ктоме вводе комментария
     * @return класс полусения свойств
     */
    public static FRedirect getService(INetProperty netProperty, JFrame owner, String tempComments, boolean onlyComments) {
        QLog.l().logger().info("Выбор услуги для перенаправления.");
        if (servicesForm == null) {
            servicesForm = new FRedirect(netProperty, owner);
        }
        servicesForm.setLocation(Math.round(owner.getLocation().x + owner.getWidth() / 2 - servicesForm.getWidth() / 2),
                Math.round(owner.getLocation().y + owner.getHeight() / 2 - servicesForm.getHeight() / 2));
        servicesForm.textAreaTempComments.setText((tempComments == null || tempComments.isEmpty() ? "" : "\n\n__________________________________\n") + tempComments);
        servicesForm.checkBoxBack.setSelected(false);
        servicesForm.checkBoxBack.setVisible(!onlyComments);
        servicesForm.comboBoxServices.setVisible(!onlyComments);
        servicesForm.buttonCancel.setVisible(!onlyComments);
        servicesForm.jLabel1.setVisible(!onlyComments);
        servicesForm.textAreaTempComments.setCaretPosition(0);
        servicesForm.setVisible(true);
        return ok ? servicesForm : null;
    }

    public long getServiceId() {
        return ids.get((String)servicesForm.comboBoxServices.getSelectedItem());
    }

    public boolean getRequestBack() {
        return servicesForm.checkBoxBack.isSelected();
    }

    public String getTempComments() {
        return (checkBoxBack.isSelected() ? checkBoxBack.getText() : (checkBoxBack.isVisible() ? "" : getLocaleMessage("redirect.message"))) + "\n" + textAreaTempComments.getText();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonOk = new javax.swing.JButton();
        buttonCancel = new javax.swing.JButton();
        checkBoxBack = new javax.swing.JCheckBox();
        comboBoxServices = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textAreaTempComments = new javax.swing.JTextArea();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getResourceMap(FRedirect.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        buttonOk.setText(resourceMap.getString("buttonOk.text")); // NOI18N
        buttonOk.setName("buttonOk"); // NOI18N

        buttonCancel.setText(resourceMap.getString("buttonCancel.text")); // NOI18N
        buttonCancel.setName("buttonCancel"); // NOI18N

        checkBoxBack.setText(resourceMap.getString("checkBoxBack.text")); // NOI18N
        checkBoxBack.setName("checkBoxBack"); // NOI18N

        comboBoxServices.setName("comboBoxServices"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        textAreaTempComments.setColumns(20);
        textAreaTempComments.setLineWrap(true);
        textAreaTempComments.setRows(5);
        textAreaTempComments.setWrapStyleWord(true);
        textAreaTempComments.setName("textAreaTempComments"); // NOI18N
        jScrollPane1.setViewportView(textAreaTempComments);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBoxBack)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(buttonOk)
                        .addGap(18, 18, 18)
                        .addComponent(buttonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(comboBoxServices, 0, 432, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboBoxServices, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(buttonOk)
                            .addComponent(buttonCancel)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(checkBoxBack)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonOk;
    private javax.swing.JCheckBox checkBoxBack;
    private javax.swing.JComboBox comboBoxServices;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea textAreaTempComments;
    // End of variables declaration//GEN-END:variables
}
