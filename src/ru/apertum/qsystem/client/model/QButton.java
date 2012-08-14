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
package ru.apertum.qsystem.client.model;

import ru.apertum.qsystem.client.common.WelcomeParams;
import ru.apertum.qsystem.common.NetCommander;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.GregorianCalendar;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import ru.apertum.qsystem.client.forms.FAdvanceCalendar;
import ru.apertum.qsystem.client.forms.FConfirmationStart;
import ru.apertum.qsystem.client.forms.FInputDialog;
import ru.apertum.qsystem.client.forms.FPreInfoDialog;
import ru.apertum.qsystem.client.forms.FWelcome;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.model.ATalkingClock;
import ru.apertum.qsystem.common.model.QCustomer;
import ru.apertum.qsystem.server.model.QAdvanceCustomer;
import ru.apertum.qsystem.server.model.QService;

/**
 * Сдесь реализован класс кнопки пользователя при выборе услуги.
 * Класс кнопки пользователя при выборе услуги.
 * Кнопка умеет слать задание на сервер для постановки в очередь.
 * @author Evgeniy Egorov
 */
public class QButton extends JButton {

    /**
     * XML - описание кнопки
     */
    private final QService service;
    /**
     * Маркировка сайта, который соотверствует услуге, которая висит на этой кнопке.
     */
    private final FWelcome form;
    private final JPanel parent;
    /**
     * Состояния кнопок
     */
    private final boolean isActive;

    public boolean isIsActive() {
        return isActive;
    }
    private final boolean isVisible;

    public boolean isIsVisible() {
        return isVisible;
    }
    private final static int NO_ACTIVE = 0;
    private final static int NO_VISIBLE = -1;

    public QButton(final QService service, FWelcome frm, JPanel prt, String resourceName) {
        super();
        setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED), new BevelBorder(BevelBorder.RAISED)));
        this.form = frm;
        this.service = service;
        this.parent = prt;
        // Нарисуем картинку на кнопке если надо
        if ("".equals(resourceName)) {
            background = null;
        } else {
            final DataInputStream inStream = new DataInputStream(getClass().getResourceAsStream(resourceName));
            byte[] b = null;
            try {
                b = new byte[inStream.available()];
                inStream.readFully(b);
            } catch (IOException ex) {
                QLog.l().logger().error(ex);
            }
            background = new ImageIcon(b).getImage();
        }
        // посмотрим доступна ли данная услуга или группа услуг
        isVisible = NO_VISIBLE != service.getStatus();
        isActive = NO_ACTIVE != service.getStatus() && isVisible;
        if (!isVisible) {
            setVisible(false);
            return;
        }
        setText(Uses.prepareAbsolutPathForImg(service.getButtonText()));
        setSize(1, 1);
        if (service.isLeaf()) {
            setIcon(new ImageIcon(getClass().getResource("/ru/apertum/qsystem/client/forms/resources/serv_btn.png")));
        } else {
            setIcon(new ImageIcon(getClass().getResource("/ru/apertum/qsystem/client/forms/resources/folder.png")));
        }
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // "Услуги" и "Группа" это одно и тоже.
                    if (!service.isLeaf()) {
                        form.showButtons(service, (JPanel) getParent());
                        if (form.clockBack.isActive()) {
                            form.clockBack.stop();
                        }
                        if (form.clockBack.getInterval() > 999) {
                            form.clockBack.start();
                        }
                    }
                    if (service.isLeaf()) {

                        //  в зависимости от активности формируем сообщение и шлем запрос на сервер об статистике
                        if (isActive) {
                            // Услуга активна. Посмотрим не предварительная ли это запись.
                            // Если Предварительная запись, то пытаемся предватительно встать и выходим из обработке кнопки.
                            if (FWelcome.isAdvanceRegim()) {
                                form.setAdvanceRegim(false);
                                final QAdvanceCustomer res = FAdvanceCalendar.showCalendar(form, true, FWelcome.netProperty, service, true, WelcomeParams.getInstance().delayBack * 2, form.advancedCustomer);
                                //Если res == null значит отказались от выбора
                                if (res == null) {
                                    form.showMed();
                                    return;
                                }
                                //вешаем заставку
                                final GregorianCalendar gc_time = new GregorianCalendar();
                                gc_time.setTime(res.getAdvanceTime());
                                int t = gc_time.get(GregorianCalendar.HOUR_OF_DAY);
                                if (t == 0) {
                                    t = 24;
                                    gc_time.add(GregorianCalendar.HOUR_OF_DAY, -1);
                                }
                                form.showDelayFormPrint("<HTML><b><p align=center><span style='font-size:60.0pt;color:green'>" + FWelcome.getLocaleMessage("qbutton.take_adv_ticket") + "<br><br><br></span>"
                                        + "<span style='font-size:80.0pt;color:blue'>" + Uses.format_dd_MMMM_yyyy.format(gc_time.getTime()) + "<br></span>"
                                        + "<span style='font-size:80.0pt;color:blue'>" + FWelcome.getLocaleMessage("qbutton.take_adv_ticket") + " " + t + ":00 " + FWelcome.getLocaleMessage("qbutton.take_adv_ticket") + " " + (t + 1) + ":00" + "</span></p>",
                                        "/ru/apertum/qsystem/client/forms/resources/getTicket.png");
                                // печатаем результат
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        QLog.l().logger().info("Печать этикетки бронирования.");
                                        FWelcome.printTicketAdvance(res);
                                    }
                                }).start();
                                // выходим, т.к. вся логика предварительной записи в форме предварительного календаря
                                return;
                            }

                            // Узнать, есть ли информация для прочнения в этой услуге.
                            // Если текст информации не пустой, то показать диалог сэтим текстом
                            // У диалога должны быть кнопки "Встать в очередь", "Печать", "Отказаться".
                            // если есть текст, то показываем диалог
                            if (service.getPreInfoHtml() != null && !"".equals(service.getPreInfoHtml())) {
                                if (!FPreInfoDialog.showPreInfoDialog(form, service.getPreInfoHtml(), service.getPreInfoPrintText(), true, true, WelcomeParams.getInstance().delayBack)) {
                                    // выходим т.к. кастомер отказался продолжать
                                    return;
                                }
                            }

                            // Если режим инфокиоска, то сразу уходим, т.к. вставать в очередь нет нужды
                            // Показали информацию и все
                            if (FWelcome.isInfo) {
                                return;
                            }

                            // узнать статистику по предлагаемой услуги и спросить потенциального кастомера
                            // будет ли он стоять или нет
                            final int count;
                            try {
                                count = NetCommander.aboutService(FWelcome.netProperty, service.getId());
                            } catch (Exception ex) {
                                // гасим жестоко, пользователю незачем видеть ошибки. выставим блокировку
                                QLog.l().logger().error("Гасим жестоко. Невозможно отправить команду на сервер. ", ex);
                                form.lock(FWelcome.LOCK_MESSAGE);
                                return;
                            }
                            // Если услуга не обрабатывается ни одним пользователем то в count вернется Uses.LOCK_INT
                            // вот трех еще потерплю, а больше низачто!
                            if (count == Uses.LOCK_INT) {
                                form.lock("<HTML><p align=center><b><span style='font-size:60.0pt;color:red'>" + FWelcome.getLocaleMessage("qbutton.service_not_available") + "</span></b></p>");
                                form.clockUnlockBack.start();
                                return;
                            }
                            if (count == Uses.LOCK_FREE_INT) {
                                form.lock("<HTML><p align=center><b><span style='font-size:60.0pt;color:red'>" + FWelcome.getLocaleMessage("qbutton.service_not_available_by_schedule") + "</span></b></p>");
                                form.clockUnlockBack.start();
                                return;
                            }
                            if (count == Uses.LOCK_PER_DAY_INT) {
                                form.lock("<HTML><p align=center><b><span style='font-size:60.0pt;color:red'>" + FWelcome.getLocaleMessage("qbutton.clients_enough") + "</span></b></p>");
                                form.clockUnlockBack.start();
                                return;
                            }
                            if (count >= WelcomeParams.getInstance().askLimit) {
                                // Выведем диалог о том будет чел сотять или пошлет нахер всю контору.
                                if (!FConfirmationStart.getMayContinue(form, count)) {
                                    return;
                                }
                            }
                        }
                        // ну если неактивно, т.е. надо показать отказ, или продолжить вставать в очередь
                        if (form.clockBack.isActive()) {
                            form.clockBack.stop();//т.к. есть какой-то логический конец, то не надо в корень автоматом.

                        }

                        String inputData = null;
                        ATalkingClock clock;
                        if (!isActive) {
                            clock = form.showDelayFormPrint("<HTML><b><p align=center><span style='font-size:60.0pt;color:green'>" + FWelcome.getLocaleMessage("qbutton.right_naw_can_not") + "</span>", "/ru/apertum/qsystem/client/forms/resources/noActive.png");
                        } else {
                            //Если услуга требует ввода данных пользователем, то нужно получить эти данные из диалога ввода
                            if (service.getInput_required()) {
                                inputData = FInputDialog.showInputDialog(form, true, FWelcome.netProperty, false, WelcomeParams.getInstance().delayBack, service.getInput_caption());
                                if (inputData == null) {
                                    return;
                                }
                                // если ввели, то нужно спросить у сервера есть ли возможность встать в очередь с такими введенными данными

                                //@return 1 - превышен, 0 - можно встать. 2 - забанен
                                int limitPersonOver = 0;
                                try {
                                    limitPersonOver = NetCommander.aboutServicePersonLimitOver(FWelcome.netProperty, service.getId(), inputData);
                                } catch (Exception ex) {
                                    // гасим жестоко, пользователю незачем видеть ошибки. выставим блокировку
                                    QLog.l().logger().error("Гасим жестоко опрос превышения лимита по введенным данным, но не лочим киоск. Невозможно отправить команду на сервер. ", ex);
                                    return;
                                }
                                if (limitPersonOver != 0) {
                                    form.lock(limitPersonOver == 1 ? "<HTML><p align=center><b><span style='font-size:60.0pt;color:red'>" + FWelcome.getLocaleMessage("qbutton.ticket_with_nom_finished") + "</span></b></p>"
                                            : "<HTML><p align=center><b><span style='font-size:60.0pt;color:red'>" + FWelcome.getLocaleMessage("qbutton.denail_by_lost") + "</span></b></p>");
                                    form.clockUnlockBack.start();
                                    return;
                                }
                            }
                            clock = form.showDelayFormPrint("<HTML><b><p align=center><span style='font-size:60.0pt;color:green'>" + FWelcome.getLocaleMessage("qbutton.take_ticket") + "</span>", "/ru/apertum/qsystem/client/forms/resources/getTicket.png");
                        }

                        //выполним задание если услуга активна
                        if (isActive) {
                            final QCustomer res;
                            try {
                                res = NetCommander.standInService(FWelcome.netProperty, service.getId(), "1", 1, inputData);
                            } catch (Exception ex) {
                                // гасим жестоко, пользователю незачем видеть ошибки. выставим блокировку
                                QLog.l().logger().error("Невозможно отправить команду на сервер. ", ex);
                                form.lock(FWelcome.LOCK_MESSAGE);
                                clock.stop();
                                return;
                            }
                            clock.stop();
                            clock = form.showDelayFormPrint("<HTML><b><p align=center><span style='font-size:60.0pt;color:green'>" + FWelcome.getLocaleMessage("qbutton.take_ticket") + "<br></span>"
                                    + "<span style='font-size:60.0pt;color:blue'>" + FWelcome.getLocaleMessage("qbutton.your_nom") + "<br></span>"
                                    + "<span style='font-size:130.0pt;color:blue'>" + res.getPrefix() + res.getNumber() + "</span></p>",
                                    "/ru/apertum/qsystem/client/forms/resources/getTicket.png");


                            QLog.l().logger().info("Печать этикетки.");

                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    FWelcome.printTicket(res);
                                }
                            }).start();
                        }
                    }
                } catch (Exception ex) {
                    QLog.l().logger().error("Ошибка при попытки обработать нажатие кнопки постановки в ачередь. " + ex.toString());
                }
            }
        });//addActionListener



        /*
        ATalkingClock clockPush = new ATalkingClock(5000, 0) {
        
        @Override
        public void run() {
        for (ActionListener l : getActionListeners()){
        l.actionPerformed(null);
        }
        }
        };
        clockPush.start();
         */
    }
    private final Image background;

    @Override
    public void paintComponent(Graphics g) {
        if (background != null) {
            g.drawImage(background, 0, 0, null);
            repaint();
        } else {
            super.paintComponent(g);
        }
    }
}
