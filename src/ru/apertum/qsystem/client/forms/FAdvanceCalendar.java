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

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import ru.apertum.qsystem.QSystem;
import ru.apertum.qsystem.client.model.IAdviceEvent;
import ru.apertum.qsystem.client.model.QAvancePanel;
import ru.apertum.qsystem.common.Uses;import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.cmd.RpcGetGridOfWeek.GridAndParams;
import ru.apertum.qsystem.common.model.ATalkingClock;
import ru.apertum.qsystem.common.model.INetProperty;
import ru.apertum.qsystem.common.model.NetCommander;
import ru.apertum.qsystem.server.model.QAdvanceCustomer;
import ru.apertum.qsystem.server.model.QService;

/**
 * Created on 27.08.2009, 10:15:34
 * Сетка-календарь для предварительной записи.
 * Имеет метод для осуществления всех действий. Вся логика инкапсулирована в этом классе.
 * @author Evgeniy Egorov
 */
public class FAdvanceCalendar extends javax.swing.JDialog {

    private static ResourceMap localeMap = null;

    private static String getLocaleMessage(String key) {
        if (localeMap == null) {
            localeMap = Application.getInstance(QSystem.class).getContext().getResourceMap(FAdvanceCalendar.class);
        }
        return localeMap.getString(key);
    }

    private static FAdvanceCalendar advanceCalendar;

    /** Creates new form FAdvanceCalendar
     * @param parent
     * @param modal 
     */
    public FAdvanceCalendar(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }
    private static INetProperty netProperty;
    private static QService service;
    private static String siteMark;
    private static QAdvanceCustomer result = null;
    private static int delay = 10000;
    private static long advancedCustomer = -1;

    /**
     * Статический метод который показывает модально диалог выбора времени для предварительной записи клиентов.
     * @param parent фрейм относительно которого будет модальность
     * @param modal модальный диалог или нет
     * @param netProperty свойства работы с сервером
     * @param service  услугa, в которую происходит предварительная запись
     * @param fullscreen растягивать форму на весь экран и прятать мышку или нет
     * @param delay задержка перед скрытием диалога. если 0, то нет автозакрытия диалога
     * @param advCustomer ID клиента предварительно идентифицированного, например в регистратуре по медполису
     * @return  если null, то отказались от предварительной записи
     */
    public static QAdvanceCustomer showCalendar(Frame parent, boolean modal, INetProperty netProperty, QService service, boolean fullscreen, int delay, long advCustomer) {
        FAdvanceCalendar.delay = delay;
        FAdvanceCalendar.advancedCustomer = advCustomer;
        QLog.l().logger().info("Выбор времени для предварительной записи");
        if (advanceCalendar == null) {
            advanceCalendar = new FAdvanceCalendar(parent, modal);
            advanceCalendar.setTitle("Выбор времени предварительной записи.");
        }
        result = null;
        Uses.setLocation(advanceCalendar);
        FAdvanceCalendar.netProperty = netProperty;
        FAdvanceCalendar.service = service;
        FAdvanceCalendar.siteMark = siteMark;
        if (advanceCalendar.showWeek(new Date())) {
            if (!(QLog.l().isDebug() || QLog.l().isDemo()) && fullscreen) {
                Uses.setFullSize(advanceCalendar);
                int[] pixels = new int[16 * 16];
                Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
                Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
                advanceCalendar.setCursor(transparentCursor);

            }
            advanceCalendar.setVisible(true);
        }
        return result;
    }
    /**
     * Таймер, по которому будем выходить в корень меню.
     */
    public ATalkingClock clockBack = new ATalkingClock(delay, 1) {

        @Override
        public void run() {
            setVisible(false);
        }
    };

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        qPanel1 = new ru.apertum.qsystem.client.model.QPanel();
        panelNavigation = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        panelHeader = new javax.swing.JPanel();
        qPanel2 = new ru.apertum.qsystem.client.model.QPanel();
        labelMonday = new javax.swing.JLabel();
        qPanel3 = new ru.apertum.qsystem.client.model.QPanel();
        labelTuesday = new javax.swing.JLabel();
        qPanel4 = new ru.apertum.qsystem.client.model.QPanel();
        labelWednesday = new javax.swing.JLabel();
        qPanel5 = new ru.apertum.qsystem.client.model.QPanel();
        labelThursday = new javax.swing.JLabel();
        qPanel6 = new ru.apertum.qsystem.client.model.QPanel();
        labelFriday = new javax.swing.JLabel();
        qPanel7 = new ru.apertum.qsystem.client.model.QPanel();
        labelSaturday = new javax.swing.JLabel();
        qPanel8 = new ru.apertum.qsystem.client.model.QPanel();
        labelSunday = new javax.swing.JLabel();
        panelButtons = new javax.swing.JPanel();
        panelBottom = new javax.swing.JPanel();
        buttonClose = new javax.swing.JButton();

        setName("Form"); // NOI18N
        setUndecorated(true);

        qPanel1.setBorder(new javax.swing.border.MatteBorder(null));
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getResourceMap(FAdvanceCalendar.class);
        qPanel1.setBackgroundImgage(resourceMap.getString("qPanel1.backgroundImgage")); // NOI18N
        qPanel1.setName("qPanel1"); // NOI18N

        panelNavigation.setBorder(new javax.swing.border.MatteBorder(null));
        panelNavigation.setName("panelNavigation"); // NOI18N
        panelNavigation.setOpaque(false);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getActionMap(FAdvanceCalendar.class, this);
        jButton1.setAction(actionMap.get("showBefore")); // NOI18N
        jButton1.setIcon(resourceMap.getIcon("jButton1.icon")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setActionCommand(resourceMap.getString("jButton1.actionCommand")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jButton2.setAction(actionMap.get("setNow")); // NOI18N
        jButton2.setIcon(resourceMap.getIcon("jButton2.icon")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setActionCommand(resourceMap.getString("jButton2.actionCommand")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        jButton3.setAction(actionMap.get("showArter")); // NOI18N
        jButton3.setIcon(resourceMap.getIcon("jButton3.icon")); // NOI18N
        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N

        javax.swing.GroupLayout panelNavigationLayout = new javax.swing.GroupLayout(panelNavigation);
        panelNavigation.setLayout(panelNavigationLayout);
        panelNavigationLayout.setHorizontalGroup(
            panelNavigationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNavigationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addGap(18, 18, 18)
                .addComponent(jButton3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelNavigationLayout.setVerticalGroup(
            panelNavigationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNavigationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelNavigationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE))
                .addContainerGap())
        );

        panelHeader.setBorder(new javax.swing.border.MatteBorder(null));
        panelHeader.setName("panelHeader"); // NOI18N
        panelHeader.setOpaque(false);
        panelHeader.setLayout(new java.awt.GridLayout(1, 0));

        qPanel2.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 2, resourceMap.getColor("qPanel2.border.matteColor"))); // NOI18N
        qPanel2.setEndColor(resourceMap.getColor("qPanel2.endColor")); // NOI18N
        qPanel2.setEndPoint(new java.awt.Point(0, 100));
        qPanel2.setGradient(java.lang.Boolean.TRUE);
        qPanel2.setName("qPanel2"); // NOI18N
        qPanel2.setStartColor(resourceMap.getColor("qPanel2.startColor")); // NOI18N
        qPanel2.setStartPoint(new java.awt.Point(0, 30));

        labelMonday.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelMonday.setText(resourceMap.getString("labelMonday.text")); // NOI18N
        labelMonday.setName("labelMonday"); // NOI18N

        javax.swing.GroupLayout qPanel2Layout = new javax.swing.GroupLayout(qPanel2);
        qPanel2.setLayout(qPanel2Layout);
        qPanel2Layout.setHorizontalGroup(
            qPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelMonday, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
        );
        qPanel2Layout.setVerticalGroup(
            qPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelMonday, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
        );

        panelHeader.add(qPanel2);

        qPanel3.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 2, resourceMap.getColor("qPanel2.border.matteColor"))); // NOI18N
        qPanel3.setEndColor(resourceMap.getColor("qPanel3.endColor")); // NOI18N
        qPanel3.setEndPoint(new java.awt.Point(0, 100));
        qPanel3.setGradient(java.lang.Boolean.TRUE);
        qPanel3.setName("qPanel3"); // NOI18N
        qPanel3.setStartColor(resourceMap.getColor("qPanel2.startColor")); // NOI18N
        qPanel3.setStartPoint(new java.awt.Point(0, 30));

        labelTuesday.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelTuesday.setText(resourceMap.getString("labelTuesday.text")); // NOI18N
        labelTuesday.setName("labelTuesday"); // NOI18N

        javax.swing.GroupLayout qPanel3Layout = new javax.swing.GroupLayout(qPanel3);
        qPanel3.setLayout(qPanel3Layout);
        qPanel3Layout.setHorizontalGroup(
            qPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelTuesday, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
        );
        qPanel3Layout.setVerticalGroup(
            qPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelTuesday, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
        );

        panelHeader.add(qPanel3);

        qPanel4.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 2, resourceMap.getColor("qPanel2.border.matteColor"))); // NOI18N
        qPanel4.setEnabled(false);
        qPanel4.setEndColor(resourceMap.getColor("qPanel4.endColor")); // NOI18N
        qPanel4.setEndPoint(new java.awt.Point(0, 100));
        qPanel4.setGradient(java.lang.Boolean.TRUE);
        qPanel4.setName("qPanel4"); // NOI18N
        qPanel4.setStartColor(resourceMap.getColor("qPanel2.startColor")); // NOI18N
        qPanel4.setStartPoint(new java.awt.Point(0, 30));

        labelWednesday.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelWednesday.setText(resourceMap.getString("labelWednesday.text")); // NOI18N
        labelWednesday.setName("labelWednesday"); // NOI18N

        javax.swing.GroupLayout qPanel4Layout = new javax.swing.GroupLayout(qPanel4);
        qPanel4.setLayout(qPanel4Layout);
        qPanel4Layout.setHorizontalGroup(
            qPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelWednesday, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
        );
        qPanel4Layout.setVerticalGroup(
            qPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelWednesday, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
        );

        panelHeader.add(qPanel4);

        qPanel5.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 2, resourceMap.getColor("qPanel2.border.matteColor"))); // NOI18N
        qPanel5.setEnabled(false);
        qPanel5.setEndColor(resourceMap.getColor("qPanel5.endColor")); // NOI18N
        qPanel5.setEndPoint(new java.awt.Point(0, 100));
        qPanel5.setGradient(java.lang.Boolean.TRUE);
        qPanel5.setName("qPanel5"); // NOI18N
        qPanel5.setStartColor(resourceMap.getColor("qPanel2.startColor")); // NOI18N
        qPanel5.setStartPoint(new java.awt.Point(0, 30));

        labelThursday.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelThursday.setText(resourceMap.getString("labelThursday.text")); // NOI18N
        labelThursday.setName("labelThursday"); // NOI18N

        javax.swing.GroupLayout qPanel5Layout = new javax.swing.GroupLayout(qPanel5);
        qPanel5.setLayout(qPanel5Layout);
        qPanel5Layout.setHorizontalGroup(
            qPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelThursday, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
        );
        qPanel5Layout.setVerticalGroup(
            qPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelThursday, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
        );

        panelHeader.add(qPanel5);

        qPanel6.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 2, resourceMap.getColor("qPanel2.border.matteColor"))); // NOI18N
        qPanel6.setEnabled(false);
        qPanel6.setEndColor(resourceMap.getColor("qPanel6.endColor")); // NOI18N
        qPanel6.setEndPoint(new java.awt.Point(0, 100));
        qPanel6.setGradient(java.lang.Boolean.TRUE);
        qPanel6.setName("qPanel6"); // NOI18N
        qPanel6.setStartColor(resourceMap.getColor("qPanel2.startColor")); // NOI18N
        qPanel6.setStartPoint(new java.awt.Point(0, 30));

        labelFriday.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelFriday.setText(resourceMap.getString("labelFriday.text")); // NOI18N
        labelFriday.setName("labelFriday"); // NOI18N

        javax.swing.GroupLayout qPanel6Layout = new javax.swing.GroupLayout(qPanel6);
        qPanel6.setLayout(qPanel6Layout);
        qPanel6Layout.setHorizontalGroup(
            qPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelFriday, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
        );
        qPanel6Layout.setVerticalGroup(
            qPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelFriday, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
        );

        panelHeader.add(qPanel6);

        qPanel7.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 2, resourceMap.getColor("qPanel2.border.matteColor"))); // NOI18N
        qPanel7.setEndColor(resourceMap.getColor("qPanel7.endColor")); // NOI18N
        qPanel7.setEndPoint(new java.awt.Point(0, 100));
        qPanel7.setGradient(java.lang.Boolean.TRUE);
        qPanel7.setName("qPanel7"); // NOI18N
        qPanel7.setStartColor(resourceMap.getColor("qPanel2.startColor")); // NOI18N
        qPanel7.setStartPoint(new java.awt.Point(0, 30));

        labelSaturday.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelSaturday.setText(resourceMap.getString("labelSaturday.text")); // NOI18N
        labelSaturday.setName("labelSaturday"); // NOI18N

        javax.swing.GroupLayout qPanel7Layout = new javax.swing.GroupLayout(qPanel7);
        qPanel7.setLayout(qPanel7Layout);
        qPanel7Layout.setHorizontalGroup(
            qPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 126, Short.MAX_VALUE)
            .addComponent(labelSaturday, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
        );
        qPanel7Layout.setVerticalGroup(
            qPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 41, Short.MAX_VALUE)
            .addComponent(labelSaturday, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
        );

        panelHeader.add(qPanel7);

        qPanel8.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 2, resourceMap.getColor("qPanel2.border.matteColor"))); // NOI18N
        qPanel8.setEndColor(resourceMap.getColor("qPanel7.endColor")); // NOI18N
        qPanel8.setEndPoint(new java.awt.Point(0, 100));
        qPanel8.setGradient(java.lang.Boolean.TRUE);
        qPanel8.setName("qPanel8"); // NOI18N
        qPanel8.setStartColor(resourceMap.getColor("qPanel2.startColor")); // NOI18N
        qPanel8.setStartPoint(new java.awt.Point(0, 30));

        labelSunday.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelSunday.setText(resourceMap.getString("labelSunday.text")); // NOI18N
        labelSunday.setName("labelSunday"); // NOI18N

        javax.swing.GroupLayout qPanel8Layout = new javax.swing.GroupLayout(qPanel8);
        qPanel8.setLayout(qPanel8Layout);
        qPanel8Layout.setHorizontalGroup(
            qPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 126, Short.MAX_VALUE)
            .addGap(0, 126, Short.MAX_VALUE)
            .addComponent(labelSunday, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
        );
        qPanel8Layout.setVerticalGroup(
            qPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 41, Short.MAX_VALUE)
            .addGap(0, 41, Short.MAX_VALUE)
            .addComponent(labelSunday, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
        );

        panelHeader.add(qPanel8);

        panelButtons.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelButtons.setName("panelButtons"); // NOI18N
        panelButtons.setOpaque(false);

        javax.swing.GroupLayout panelButtonsLayout = new javax.swing.GroupLayout(panelButtons);
        panelButtons.setLayout(panelButtonsLayout);
        panelButtonsLayout.setHorizontalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 903, Short.MAX_VALUE)
        );
        panelButtonsLayout.setVerticalGroup(
            panelButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 428, Short.MAX_VALUE)
        );

        panelBottom.setBorder(new javax.swing.border.MatteBorder(null));
        panelBottom.setName("panelBottom"); // NOI18N
        panelBottom.setOpaque(false);

        buttonClose.setAction(actionMap.get("closeAdvaseForm")); // NOI18N
        buttonClose.setIcon(resourceMap.getIcon("buttonClose.icon")); // NOI18N
        buttonClose.setText(resourceMap.getString("buttonClose.text")); // NOI18N
        buttonClose.setName("buttonClose"); // NOI18N

        javax.swing.GroupLayout panelBottomLayout = new javax.swing.GroupLayout(panelBottom);
        panelBottom.setLayout(panelBottomLayout);
        panelBottomLayout.setHorizontalGroup(
            panelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBottomLayout.createSequentialGroup()
                .addContainerGap(571, Short.MAX_VALUE)
                .addComponent(buttonClose, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelBottomLayout.setVerticalGroup(
            panelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBottomLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(buttonClose, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout qPanel1Layout = new javax.swing.GroupLayout(qPanel1);
        qPanel1.setLayout(qPanel1Layout);
        qPanel1Layout.setHorizontalGroup(
            qPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelNavigation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelHeader, javax.swing.GroupLayout.DEFAULT_SIZE, 905, Short.MAX_VALUE)
            .addComponent(panelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelBottom, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        qPanel1Layout.setVerticalGroup(
            qPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(qPanel1Layout.createSequentialGroup()
                .addComponent(panelNavigation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelBottom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(qPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(qPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Action
    public void closeAdvaseForm() {
        close();
    }

    public void close() {
        if (clockBack.isActive()) {
            clockBack.stop();
        }
        setVisible(false);
    }

    @Action
    public void showBefore() {
        if (firstWeekDay.after(new Date())) {
            final GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(firstWeekDay);
            gc.set(GregorianCalendar.DAY_OF_YEAR, gc.get(GregorianCalendar.DAY_OF_YEAR) - 7);
            showWeek(gc.getTime());
        }
    }

    @Action
    public void setNow() {
        showWeek(new Date());
    }

    @Action
    public void showArter() {
        final GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(firstWeekDay);
        gc.set(GregorianCalendar.DAY_OF_YEAR, gc.get(GregorianCalendar.DAY_OF_YEAR) + 7);
        showWeek(gc.getTime());
    }
    private Date firstWeekDay;

    /**
     * Показать недельную сетку для выбора предварительной записи
     * @param firstWeekDay первый день недели
     * @return получилось отобразить или нет
     */
    private boolean showWeek(Date firstWeekDay) {
        final GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(firstWeekDay);
        gc.set(GregorianCalendar.DAY_OF_WEEK, 2);
        this.firstWeekDay = gc.getTime();
        /**
         * Получим грид доступности часов для записи
         */
        final GridAndParams res = NetCommander.getGridOfWeek(netProperty, service.getId(), this.firstWeekDay, advancedCustomer);
        if (res.getSpError() != null) {
            QLog.l().logger().error(res.getSpError());
            JOptionPane.showConfirmDialog(this, res.getSpError(), getLocaleMessage("dialog.message.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        showWeek(res, gc);
        return true;
    }

    /**
     * Конкретное рисование
     * @param res структура допустимых часов
     * @param gc первый день недели
     */
    private void showWeek(GridAndParams res, GregorianCalendar gc) {
        // таймер закрытия диалога по таймауту
        if (delay != 0) {
            if (clockBack.isActive()) {
                clockBack.stop();
            }
            clockBack.start();
        }
        /*
         * Нарисуем грид для выбора часов
         */
        /*
         * Часы работы услуги
         */
        final GregorianCalendar gc_time = new GregorianCalendar();
        gc_time.setTime(res.getStartTime());
        int start = gc_time.get(GregorianCalendar.HOUR_OF_DAY);
        gc_time.setTime(res.getFinishTime());
        int finish = gc_time.get(GregorianCalendar.HOUR_OF_DAY) - 1;

        int finish_temp = 0;
        if (start > finish) {
            finish_temp = finish;
            finish = 24;
        }

        final GregorianCalendar gc_now = new GregorianCalendar();
        gc_now.setTime(new Date());
        gc_now.set(GregorianCalendar.MINUTE, 00);
        gc_now.set(GregorianCalendar.HOUR_OF_DAY, 24);

        QLog.l().logger().debug("Предварительная запись с " + start + " до " + finish + " часов.");
        /*
         * Разложим панельки-часы
         */
        panelButtons.setVisible(false);
        panelButtons.removeAll();
        panelButtons.setLayout(new GridLayout(finish - start + 1 + finish_temp, 7));


        for (int h = 1; h <= finish_temp; h++) {
            for (int d = 0; d < 7; d++) {

                gc_time.setTime(this.firstWeekDay);
                gc_time.set(GregorianCalendar.DAY_OF_WEEK, d + 2);
                gc_time.set(GregorianCalendar.HOUR_OF_DAY, h);
                gc_time.set(GregorianCalendar.MINUTE, 0);



                panelButtons.add(new QAvancePanel(new IAdviceEvent() {

                    @Override
                    public void eventPerformed(Date date) {
                        if (clockBack.isActive()) {
                            clockBack.stop();
                        }
                        // ставим предварительного кастомера
                        result = NetCommander.standInServiceAdvance(netProperty, service.getId(), date, advancedCustomer);
                        // закрываем диалог выбора предварительного выбора времени
                        setVisible(false);
                    }
                }, gc_time.getTime(), checkTime(gc_time.getTime(), res) && gc_time.getTime().after(gc_now.getTime())));
            }
        }


        for (int h = start; h <= finish; h++) {
            for (int d = 0; d < 7; d++) {

                gc_time.setTime(this.firstWeekDay);
                gc_time.set(GregorianCalendar.DAY_OF_WEEK, d + 2);
                gc_time.set(GregorianCalendar.HOUR_OF_DAY, h);
                gc_time.set(GregorianCalendar.MINUTE, 0);



                panelButtons.add(new QAvancePanel(new IAdviceEvent() {

                    @Override
                    public void eventPerformed(Date date) {
                        if (clockBack.isActive()) {
                            clockBack.stop();
                        }
                        // ставим предварительного кастомера
                        result = NetCommander.standInServiceAdvance(netProperty, service.getId(), date, advancedCustomer);
                        // закрываем диалог выбора предварительного выбора времени
                        setVisible(false);
                    }
                }, gc_time.getTime(), checkTime(gc_time.getTime(), res) && gc_time.getTime().after(gc_now.getTime())));
            }
        }

        //Monday Tuesday WEDNESDAY Thursday Friday Saturday Sunday
        //понедельник вторник среда четверг пятница суббота воскресенье
        printWeek(labelMonday, GregorianCalendar.MONDAY, gc);
        printWeek(labelTuesday, GregorianCalendar.TUESDAY, gc);
        printWeek(labelWednesday, GregorianCalendar.WEDNESDAY, gc);
        printWeek(labelThursday, GregorianCalendar.THURSDAY, gc);
        printWeek(labelFriday, GregorianCalendar.FRIDAY, gc);
        printWeek(labelSaturday, GregorianCalendar.SATURDAY, gc);
        printWeek(labelSunday, GregorianCalendar.SUNDAY, gc);
        panelButtons.setVisible(true);

    }

    private void printWeek(JLabel label, int day, GregorianCalendar gc) {
        gc.set(GregorianCalendar.DAY_OF_WEEK, day);
        final GregorianCalendar now = new GregorianCalendar();
        if (now.get(GregorianCalendar.DAY_OF_MONTH) == gc.get(GregorianCalendar.DAY_OF_MONTH)
                && now.get(GregorianCalendar.DAY_OF_WEEK) == gc.get(GregorianCalendar.DAY_OF_WEEK)
                && now.get(GregorianCalendar.DAY_OF_YEAR) == gc.get(GregorianCalendar.DAY_OF_YEAR)) {
            label.setText("<html><b><p align=center><span style='font-size:17.0pt;color:red'>" + getNameWeekDay(gc.getTime()) + "<br/></span><span style='font-size:17.0pt;color:red'>" + Uses.format_dd_MMMM.format(gc.getTime()));
        } else {
            label.setText("<html><b><p align=center><span style='font-size:17.0pt;color:blue'>" + getNameWeekDay(gc.getTime()) + "<br/></span><span style='font-size:13.0pt;color:black'>" + Uses.format_dd_MMMM.format(gc.getTime()));
        }
    }

    private String getNameWeekDay(Date date) {
        final GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        String s = "none";
        //Monday Tuesday WEDNESDAY Thursday Friday Saturday Sunday
        //понедельник вторник среда четверг пятница суббота воскресенье
        switch (gc.get(GregorianCalendar.DAY_OF_WEEK)) {
            case GregorianCalendar.MONDAY:
                s = getLocaleMessage("calendar.day.monday");
                break;
            case GregorianCalendar.TUESDAY:
                s = getLocaleMessage("calendar.day.tuesday");
                break;
            case GregorianCalendar.WEDNESDAY:
                s = getLocaleMessage("calendar.day.wednesday");
                break;
            case GregorianCalendar.THURSDAY:
                s = getLocaleMessage("calendar.day.thursday");
                break;
            case GregorianCalendar.FRIDAY:
                s = getLocaleMessage("calendar.day.friday");
                break;
            case GregorianCalendar.SATURDAY:
                s = getLocaleMessage("calendar.day.saturday");
                break;
            case GregorianCalendar.SUNDAY:
                s = getLocaleMessage("calendar.day.sunday");
                break;
        }

        return s;
    }

    /**
     * Проверить время на доступность.
     * Нужно для реализации ограничения количества предварительно записанных в один час и кучерявого расписания.
     * @param date время предварительной записи для проверки
     * @param params список всех разрешенных времен + параметры
     * @return количество предварительных на опр. время
     */
    protected boolean checkTime(Date date, GridAndParams params) {
        final GregorianCalendar gc_client = new GregorianCalendar();
        final GregorianCalendar gc_now = new GregorianCalendar();
        gc_client.setTime(date);
        gc_now.setTime(new Date());
        // проверим не отлистал ли пользователь слишком далеко, куда уже нельзя
        if (Math.abs(gc_client.get(GregorianCalendar.DAY_OF_YEAR) - gc_now.get(GregorianCalendar.DAY_OF_YEAR)) > params.getAdvanceLimitPeriod()
                && params.getAdvanceLimitPeriod() != 0) {
            return false;
        }
        for (Date clientTime : params.getTimes()) {
            final GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(clientTime);

            if (gc.get(GregorianCalendar.DAY_OF_WEEK) == gc_client.get(GregorianCalendar.DAY_OF_WEEK)
                    && gc.get(GregorianCalendar.HOUR_OF_DAY) == gc_client.get(GregorianCalendar.HOUR_OF_DAY)) {
                return true;
            }
        }
        return false;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonClose;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel labelFriday;
    private javax.swing.JLabel labelMonday;
    private javax.swing.JLabel labelSaturday;
    private javax.swing.JLabel labelSunday;
    private javax.swing.JLabel labelThursday;
    private javax.swing.JLabel labelTuesday;
    private javax.swing.JLabel labelWednesday;
    private javax.swing.JPanel panelBottom;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelNavigation;
    private ru.apertum.qsystem.client.model.QPanel qPanel1;
    private ru.apertum.qsystem.client.model.QPanel qPanel2;
    private ru.apertum.qsystem.client.model.QPanel qPanel3;
    private ru.apertum.qsystem.client.model.QPanel qPanel4;
    private ru.apertum.qsystem.client.model.QPanel qPanel5;
    private ru.apertum.qsystem.client.model.QPanel qPanel6;
    private ru.apertum.qsystem.client.model.QPanel qPanel7;
    private ru.apertum.qsystem.client.model.QPanel qPanel8;
    // End of variables declaration//GEN-END:variables
}
