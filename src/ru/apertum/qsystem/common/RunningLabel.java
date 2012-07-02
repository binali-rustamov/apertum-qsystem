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
package ru.apertum.qsystem.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import ru.apertum.qsystem.common.model.ATalkingClock;

/**
 * Компонент расширенной метки JLabel.
 * Класс, расширяющий JLabel.
 * Добавлены свойства создания бегущего текста, статического текста без привязки к лайаутам и т.д.
 * Умеет мигать.
 * @author Evgeniy Egorov
 */
public class RunningLabel extends JLabel implements Serializable {

    public RunningLabel() {

        addComponentListener(new java.awt.event.ComponentAdapter() {

            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                onResize(evt);
            }
        });
    }

    /**
     * Событие ресайза метки.
     * Если что-то нужно повесиь на ресайз, то перекрыть этод метод, незабыв вызвать предка.
     * @param evt
     */
    protected void onResize(java.awt.event.ComponentEvent evt) {
        needRepaint();
    }
    /**
     * Событие перерисовки 25 кадров в секунду.
     */
    private ActionListener actionListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            need = true;
            repaint();
        }
    };
    private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        if (listener == null || propertySupport == null) {
            return;
        }
        propertySupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        super.removePropertyChangeListener(listener);
        if (listener == null || propertySupport == null) {
            return;
        }
        propertySupport.removePropertyChangeListener(listener);
    }

    @Override
    public void setVerticalAlignment(int alignment) {
        super.setVerticalAlignment(alignment);
        needRepaint();
    }

    @Override
    public void setHorizontalAlignment(int alignment) {
        super.setHorizontalAlignment(alignment);
        needRepaint();
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        delta = 100 + 1 * font.getSize();
        needRepaint();
    }
    private int delta = 100;
    private int nPosition = 0;
    private int nTitleHeight;
    private Image mImg;
    private Graphics gImg = null;
    private Dimension dmImg = null;
    public static final String PROP_SPEED = "speedRunningText";
    /**
     * Скорость двидения текста. На сколько пикселей сместится кадр относительно предыдущего при 24 кадра в секкунду.
     * По умолчанию 10 пикселей, т.е. 240 пикселей с секунку будет скорость движения текста.
     */
    private int speedRunningText = 10;

    public int getSpeedRunningText() {
        return speedRunningText;
    }

    public void setSpeedRunningText(int speedRunningText) {
        final int oldValue = speedRunningText;
        this.speedRunningText = speedRunningText;
        propertySupport.firePropertyChange(PROP_SPEED, oldValue, speedRunningText);
    }
    public static final String PROP_RUNNING_TEXT = "running_text";
    /**
     * Бегущий текст, это не тот же что статический
     */
    private String runningText = "runningText";
    private int[] sizes;

    private void setSizes(String text) {
        // подсчитаем длины
        sizes = new int[text.length()];
        for (int i = 0; i < text.length(); i++) {
            sizes[i] = getFontMetrics(getFont()).stringWidth(text.substring(0, i));
        }
    }

    /**
     * Этот метод установит новое значение бегущего текста, выведет его на конву
     * и отцентрирует его в зависимости от установленных выравниваний.
     * @param text устанавливаемый бегущий текст
     */
    public void setRunningText(String text) {
        final String oldValue = runningText;
        this.runningText = text;
        propertySupport.firePropertyChange(PROP_RUNNING_TEXT, oldValue, runningText);
        setSizes(text);
        needRepaint();
    }
    private String oldTxt = "";

    public String getRunningText() {
        final String txt = getShowTime() ? getDate() : runningText;
        if (sizes == null || oldTxt.length() != txt.length()) {
            setSizes(txt);
        }
        oldTxt = txt;
        return txt;
    }

    private String getDate() {
        return Uses.format_for_label.format(new Date());
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        needRepaint();
    }

    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        needRepaint();
    }

    private void needRepaint() {
        needRepaint = true;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        if (backgroundImg != null || !"".equals(runningText) || getShowTime()) {
            // Тут условия на изменение картинки с текстом
            if (((run || isBlink()) && need) || ((run == false && isBlink() == false) && need) || needRepaint) {

                updateImg();
                need = false;
                needRepaint = false;
            }
            if (mImg != null) {
                g.drawImage(mImg, 0, 0, null);
            } else {
                if (backgroundImg != null) {
                    g.drawImage(backgroundImg, 0, 0, null);
                }
            }
        }
        super.paint(g);
    }
    
    @Override
    public void update(Graphics g) {
      // не знаю зачем, попытка улучшить прорисовку, но не проверено как повлияло  paint(getGraphics());
    }

    /**
     * Формирование картинки сдвинутой для эффекта смещения
     */
    private void updateImg() {

        Dimension dm = getSize();
        int wndWidth = dm.width;
        int wndHeight = dm.height;

        if ((dmImg == null) ||
                (dmImg.width != wndWidth) ||
                (dmImg.height != wndHeight)) {
            dmImg = new Dimension(wndWidth, wndHeight);
            mImg = createImage(wndWidth, wndHeight);
            gImg = mImg.getGraphics();
        }

        Color fg = getForeground();
        Color bg = getBackground();
        gImg.setColor(bg);

        //Теперь мы закрашиваем изображение:

        gImg.fillRect(0, 0, dmImg.width, dmImg.height);
        if (getBackgroundImage() != null) {
            gImg.drawImage(backgroundImg, 0, 0, null);
        }
        gImg.setColor(fg);

        // отцентрируем по вертикале
        final int y;
        switch (getVerticalAlignment()) {
            case SwingConstants.CENTER:
                y = (getHeight() - (int) (getFont().getSize() * 0.95)) / 2;
                break;
            case SwingConstants.BOTTOM:
                y = getHeight() - (int) (getFont().getSize() * 0.95);
                break;
            default:
                y = 0;
        }
        /*
        CENTER  = 0;
        TOP     = 1;
        LEFT    = 2;
        BOTTOM  = 3;
        RIGHT   = 4;
         */
        String forDrow = getRunningText();
        // отцентрируем по горизонтале
        final int len = sizes.length == 0 ? 0 : sizes[sizes.length - 1];
        if (!run) {
            switch (getHorizontalAlignment()) {
                case SwingConstants.CENTER:
                    nPosition = (getWidth() - len) / 2;
                    break;
                case SwingConstants.RIGHT:
                    nPosition = getWidth() - len;
                    break;
                default:
                    nPosition = 0;
            }
        }
        //Затем рисуем строку в контексте изображения:
        gImg.setFont(getFont());

        nPosition = nPosition - (run ? getSpeedRunningText() : 0);
        if (nPosition < - len) {
            nPosition = getSize().width;
        }
        if (isVisibleRunningText) {
            int lenHead = 0;
            int pos = -1;// позиция последней отрубаемой буквы
            while (-nPosition - delta > lenHead) {
                //pos = pos + 3;
                lenHead = sizes[++pos];
            }
            if (pos > -1) {
                pos--;
            }


            forDrow = forDrow.substring(pos == -1 ? 0 : pos + 1, forDrow.length());
            int i = 0;
            while (i < forDrow.length() && getWidth() + delta > sizes[i++]) {
            }
            forDrow = forDrow.substring(0, i);
            gImg.drawString(forDrow,
                    nPosition + (pos == -1 ? 0 : sizes[pos + 1]),
                    y + nTitleHeight + (int) (getFont().getSize() * 0.75));
        }
    }
    public static final String PROP_BACKGROUND_IMG = "backgroundImgage";
    /**
     * Фоновая картинка.
     */
    private String backgroundImage = "";
    private Image backgroundImg = null;

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String resourceName) {
        final String oldValue = resourceName;
        this.backgroundImg = Uses.loadImage(this, resourceName);
        propertySupport.firePropertyChange(PROP_BLINK_COUNT, oldValue, resourceName);
        needRepaint();
    }
    public static final String PROP_IS_RUN = "isRunText";
    /**
     * бежит ли строка
     */
    private Boolean run = false;

    public Boolean isRun() {
        return run;
    }

    public void setRun(Boolean run) {
        final Boolean oldValue = run;
        this.run = run;
        propertySupport.firePropertyChange(PROP_IS_RUN, oldValue, run);
        if (run) {
            start();
        } else {
            stop();
        }
    }
    /**
     * Возникло ли событие смещение надписи
     */
    private boolean need = false;
    private boolean needRepaint = false;
    /**
     * Поток генерации событий смещения текста
     */
    //private Thread timerThread = null;
    private Timer timerThread = new Timer(40, actionListener);

    /**
     * Запустить бегущий текст
     */
    public void start() {
        run = true;
        setRateMainTimer();
        /**
         * Создать поток для инициализации перересовки.
         * Он выступает так же и в качестве таймера для перересовки.
         */
        nPosition = getWidth();
        if (!timerThread.isRunning()) {
            timerThread.start();
        }
    }

    /**
     * Остановить бегущий текст
     */
    public void stop() {
        run = false;
        setRateMainTimer();
        if (!isBlink() && !showTime) {
            timerThread.stop();
        }
    }
    public static final String PROP_BLINK_COUNT = "blinkCount";
    /**
     * Количество миганий текста.
     */
    private int blinkCount = 0;

    public int getBlinkCount() {
        return blinkCount;
    }

    public void setBlinkCount(int blinkCount) {
        final int oldValue = blinkCount;
        this.blinkCount = blinkCount;
        propertySupport.firePropertyChange(PROP_BLINK_COUNT, oldValue, blinkCount);
        blinkTimer.setCount(blinkCount * 2);
    }
    public static final String PROP_SPEED_BLINK = "speed_blink";
    /**
     * Скорость миганий текста.
     */
    private int speedBlink = 500;

    public int getSpeedBlink() {
        return speedBlink;
    }

    public void setSpeedBlink(int speedBlink) {
        final int oldValue = blinkCount;
        this.speedBlink = speedBlink;
        propertySupport.firePropertyChange(PROP_SPEED_BLINK, oldValue, speedBlink);
        blinkTimer.setInterval(speedBlink);
    }
    /**
     * Таймер мигания надписи
     */
    private final ATalkingClock blinkTimer = new ATalkingClock(getSpeedBlink(), getBlinkCount()) {

        @Override
        public void run() {
            setRateMainTimer();
            isVisibleRunningText = !isVisibleRunningText;
        }

        @Override
        public void stop() {
            super.stop();
            stopBlink();
        }
    };

    /**
     * Запустить мигание текста
     */
    public void startBlink() {
        if (!blinkTimer.isActive()) {
            blinkTimer.start();
        }

        /**
         * Создать поток для инициализации перересовки.
         * Он выступает так же и в качестве таймера для перересовки.
         */
        if (!timerThread.isRunning()) {
            timerThread.start();
        }
    }

    /**
     * Остановить мигание текста
     */
    public void stopBlink() {
        if (blinkTimer.isActive()) {
            blinkTimer.stop();
        }
        isVisibleRunningText = true;
        if (!run && !showTime) {
            need = true;
            repaint();
        }
        setRateMainTimer();
    }
    /**
     * Нужно для мигания.
     */
    private boolean isVisibleRunningText = true;

    /**
     * мигает ли строка
     */
    private boolean isBlink() {
        return blinkTimer.isActive();
    }
    public static final String PROP_SHOW_TIME = "showTime";
    /**
     * Показывать или нет время вместо текста на бегущей строке.
     */
    private Boolean showTime = false;

    public Boolean getShowTime() {
        return showTime;
    }

    public void setShowTime(Boolean showTime) {
        final Boolean oldValue = showTime;
        this.showTime = showTime;
        propertySupport.firePropertyChange(PROP_SHOW_TIME, oldValue, showTime);
        setRateMainTimer();
        /**
         * Создать поток для инициализации перересовки.
         * Он выступает так же и в качестве таймера для перересовки.
         */
        if (showTime) {
            setSizes(getDate());
            timerThread.start();
        } else {
            setRunningText(runningText);
            needRepaint();
        }
    }

    private void setRateMainTimer() {
        timerThread.setDelay(run ? 40 : (isBlink() ? getSpeedBlink() : 1000));
        if (timerThread.isRunning()) {
            timerThread.restart();
        }
    }
}
