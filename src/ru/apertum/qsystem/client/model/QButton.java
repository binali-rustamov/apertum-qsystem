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
package ru.apertum.qsystem.client.model;

import ru.apertum.qsystem.common.model.NetCommander;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import org.dom4j.Element;
import ru.apertum.qsystem.client.forms.FAdvanceCalendar;
import ru.apertum.qsystem.client.forms.FConfirmationStart;
import ru.apertum.qsystem.client.forms.FInputDialog;
import ru.apertum.qsystem.client.forms.FPreInfoDialog;
import ru.apertum.qsystem.client.forms.FWelcome;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.model.ATalkingClock;

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
    private final Element element;
    /**
     * Маркировка сайта, который соотверствует услуге, которая висит на этой кнопке.
     */
    private final String siteMark;
    private final FWelcome form;
    private final JPanel parent;
    /**
     * Состояния кнопок
     */
    private final boolean isActive;
    private final boolean isVisible;
    private final static String NO_ACTIVE = "0";
    private final static String NO_VISIBLE = "-1";

    public QButton(final Element element, FWelcome frm, JPanel prt, String resourceName) {
        super();
        setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED), new BevelBorder(BevelBorder.RAISED)));
        this.form = frm;
        this.element = element;
        this.parent = prt;
        final String ss = element.attributeValue(Uses.TASK_FOR_SITE);
        this.siteMark = (ss == null) ? "" : ss;
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
                Uses.log.logger.error(ex);
            }
            background = new ImageIcon(b).getImage();
        }
        // посмотрим доступна ли данная услуга или группа услуг
        isVisible = !NO_VISIBLE.equals(element.attributeValue(Uses.TAG_PROP_STATUS));
        isActive = !NO_ACTIVE.equals(element.attributeValue(Uses.TAG_PROP_STATUS)) && isVisible;
        if (!isVisible) {
            setVisible(false);
            return;
        }
        setText(element.getTextTrim());
        setSize(1, 1);
        if (Uses.TAG_SERVICE.equals(element.getName())) {
            setIcon(new ImageIcon(getClass().getResource("/ru/apertum/qsystem/client/forms/resources/service.png")));
        } else {
            setIcon(new ImageIcon(getClass().getResource("/ru/apertum/qsystem/client/forms/resources/inFolder.png")));
        }
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // "Услуги" и "Группа" это одно и тоже.
                    if (Uses.TAG_GROUP.equals(element.getName()) || Uses.TAG_PROP_SERVICES.equals(element.getName())) {
                        form.showButtons(element, (JPanel) getParent());
                        if (form.clockBack.isActive()) {
                            form.clockBack.stop();
                        }
                        form.clockBack.start();
                    }
                    if (Uses.TAG_SERVICE.equals(element.getName())) {

                        //  в зависимости от активности формируем сообщение и шлем запрос на сервер об статистике
                        if (isActive) {
                            // Услуга активна. Посмотрим не предварительная ли это запись.
                            // Если Предварительная запись, то пытаемся предватительно встать и выходим из обработке кнопки.
                            if (FWelcome.isAdvanceRegim()) {
                                form.setAdvanceRegim(false);
                                final Element res = FAdvanceCalendar.showCalendar(form, true, FWelcome.netProperty, element.attributeValue(Uses.TAG_NAME), siteMark, true, FWelcome.welcomeParams.delayBack * 2, form.advancedCustomer);
                                //Если res == null значит отказались от выбора
                                if (res == null) {
                                    form.showMed();
                                    return;
                                }
                                //вешаем заставку
                                final String time = res.attributeValue(Uses.TAG_START_TIME);
                                final Date date;
                                try {
                                    date = Uses.format_for_trans.parse(time);
                                } catch (ParseException ex) {
                                    throw new Uses.ServerException("Неправильный парсинг даты начала недели для определения ранее записавшихся.");
                                }

                                final GregorianCalendar gc_time = new GregorianCalendar();
                                gc_time.setTime(date);
                                int t = gc_time.get(GregorianCalendar.HOUR_OF_DAY);
                                if (t == 0) {
                                    t = 24;
                                    gc_time.add(GregorianCalendar.HOUR_OF_DAY, -1);
                                }
                                form.showDelayFormPrint("<HTML><b><p align=center><span style='font-size:30.0pt;color:green'>Пожалуйста возьмите талон предварительной записи на:<br><br><br></span>"
                                        + "<span style='font-size:60.0pt;color:blue'>" + Uses.format_dd_MMMM_yyyy.format(gc_time.getTime()) + "<br></span>"
                                        + "<span style='font-size:60.0pt;color:blue'>" + "c " + t + ":00 до " + (t + 1) + ":00" + "</span></p>",
                                        "/ru/apertum/qsystem/client/forms/resources/getTicket.png");
                                // печатаем результат
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        Uses.log.logger.info("Печать этикетки бронирования.");
                                        // Для доменной системы на этикетке под картинкой выводим наименование с своего сайта
                                        final String cap = FWelcome.captions.get(siteMark);
                                        if (cap == null) {
                                            FWelcome.printTicketAdvance(res);
                                        } else {
                                            FWelcome.printTicketAdvance(res, cap);
                                        }
                                    }
                                }).start();
                                // выходим, т.к. вся логика предварительной записи в форме предварительного календаря
                                return;
                            }

                            // Узнать у сервера, есть ли информация для прочнения в этой услуге.
                            // Если текст информации не пустой, то показать диалог сэтим текстом
                            // У диалога должны быть кнопки "Встать в очередь", "Печать", "Отказаться".
                            final Element preInfo;
                            try {
                                preInfo = NetCommander.getPreInfoForService(FWelcome.netProperty, element.attributeValue(Uses.TAG_PROP_NAME), siteMark);
                            } catch (Exception ex) {
                                // гасим жестоко, пользователю незачем видеть ошибки. выставим блокировку
                                Uses.log.logger.error("Невозможно отправить команду на сервер. ", ex);
                                form.lock(FWelcome.LOCK_MESSAGE);
                                return;
                            }
                            final String html = preInfo.elementTextTrim("html");
                            // Trim - убивает много чего лишнего а не только пробелы в конце и в начале, еще ПЕРЕНОС СТРОКИ убивает
                            final String print = preInfo.elementTextTrim("print");
                            // если есть текст, то показываем диалог
                            if (!"".equals(html)){
                               if (!FPreInfoDialog.showPreInfoDialog(form, html, print, true, true, FWelcome.welcomeParams.delayBack)){
                                  // выходим т.к. кастомер отказался продолжать
                                  return;
                               }
                            }

                            // узнать статистику по предлагаемой услуги и спросить потенциального кастомера
                            // будет ли он стоять или нет
                            final Element statistic;
                            try {
                                statistic = NetCommander.aboutService(FWelcome.netProperty, element.attributeValue(Uses.TAG_PROP_NAME), siteMark);
                            } catch (Exception ex) {
                                // гасим жестоко, пользователю незачем видеть ошибки. выставим блокировку
                                Uses.log.logger.error("Невозможно отправить команду на сервер. ", ex);
                                form.lock(FWelcome.LOCK_MESSAGE);
                                return;
                            }
                            final int count = Integer.parseInt(statistic.attributeValue(Uses.TAG_DESCRIPTION));
                            // Если услуга не обрабатывается ни одним пользователем то в count вернется Uses.LOCK_INT
                            // вот трех еще потерплю, а больше низачто!
                            if (count == Uses.LOCK_INT) {
                                form.lock("<HTML><p align=center><b><span style='font-size:40.0pt;color:red'>Выбранная услуга не обрабатывается. Обратитесь к администратору.</span></b></p>");
                                form.clockUnlockBack.start();
                                return;
                            }
                            if (count == Uses.LOCK_FREE_INT) {
                                form.lock("<HTML><p align=center><b><span style='font-size:40.0pt;color:red'>Выбранная услуга сейчас не оказывается по расписанию. Обратитесь к администратору.</span></b></p>");
                                form.clockUnlockBack.start();
                                return;
                            }
                            if (count >= FWelcome.welcomeParams.askLimit) {
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
                            clock = form.showDelayFormPrint("<HTML><b><p align=center><span style='font-size:50.0pt;color:green'>В данный момент услуга не может быть оказана!</span>", "/ru/apertum/qsystem/client/forms/resources/noActive.png");
                        } else {
                            //Если услуга требует ввода данных пользователем, то нужно получить эти данные из диалога ввода
                            if ("1".equals(element.attributeValue(Uses.TAG_PROP_INPUT_REQUIRED)) || "true".equals(element.attributeValue(Uses.TAG_PROP_INPUT_REQUIRED).toLowerCase())) {
                                inputData = FInputDialog.showInputDialog(form, true, FWelcome.netProperty, false, FWelcome.welcomeParams.delayBack, element.attributeValue(Uses.TAG_PROP_INPUT_CAPTION));
                                if (inputData == null) {
                                    return;
                                }
                            }
                            clock = form.showDelayFormPrint("<HTML><b><p align=center><span style='font-size:50.0pt;color:green'>Пожалуйста возьмите талон!</span>", "/ru/apertum/qsystem/client/forms/resources/getTicket.png");
                        }

                        //выполним задание если услуга активна
                        if (isActive) {
                            final Element res;
                            try {
                                res = NetCommander.standInService(FWelcome.netProperty, element.attributeValue(Uses.TAG_NAME), "1", 1, siteMark, inputData);
                            } catch (Exception ex) {
                                // гасим жестоко, пользователю незачем видеть ошибки. выставим блокировку
                                Uses.log.logger.error("Невозможно отправить команду на сервер. ", ex);
                                form.lock(FWelcome.LOCK_MESSAGE);
                                clock.stop();
                                return;
                            }
                            clock.stop();
                            clock = form.showDelayFormPrint("<HTML><b><p align=center><span style='font-size:30.0pt;color:green'>Пожалуйста возьмите талон!<br></span>"
                                    + "<span style='font-size:20.0pt;color:blue'>ваш номер<br></span>"
                                    + "<span style='font-size:100.0pt;color:blue'>" + res.attributeValue(Uses.TAG_PREFIX) + res.attributeValue(Uses.TAG_NUMBER) + "</span></p>",
                                    "/ru/apertum/qsystem/client/forms/resources/getTicket.png");


                            Uses.log.logger.info("Печать этикетки.");

                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    // Для доменной системы на этикетке под картинкой выводим наименование с своего сайта
                                    final String cap = FWelcome.captions.get(siteMark);
                                    if (cap == null) {
                                        FWelcome.printTicket(res);
                                    } else {
                                        FWelcome.printTicket(res, cap);
                                    }
                                }
                            }).start();
                        }
                    }
                } catch (Exception ex) {
                    Uses.log.logger.error("Ошибка при попытки обработать нажатие кнопки постановки в ачередь. " + ex.toString());
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
