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

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import org.dom4j.Element;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import ru.apertum.qsystem.QSystem;
import ru.apertum.qsystem.client.model.QPanel;
import ru.apertum.qsystem.common.RunningLabel;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.model.ATalkingClock;

/**
 * Created on 22 Сентябрь 2008 г., 14:27
 * @author Evgeniy Egorov
 */
public class FIndicatorBoard extends javax.swing.JFrame {

    /**
     * Пустая строка.
     */
    public static final String LINE_EMPTY = "<HTML><b><p align=center><span style='font-size:150.0pt;color:green'></span><p><b>";
    /**
     * Фоновая картинка.
     */
    public static Image background;
    private final Element topElement;
    private final Element bottomElement;
    private final Element leftElement;
    private final Element rightElement;
    private final Element mainElement;
    private static FIndicatorBoard indicatorBoard = null;
    private static Element root = null;
    /**
     * Режим. Главное табло или клиентское.
     */
    private static boolean isMain = true;

    /**
     * Получить форму табло. Получаем главное табло.
     * Если требуется получить с указанием типа то использовать public static FIndicatorBoard getIndicatorBoard(Element rootParams, boolean isMain)
     * @param rootParams параметры табло.
     * @return
     */
    public static FIndicatorBoard getIndicatorBoard(Element rootParams) {
        if (!"1".equals(rootParams.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL))) {
            return null;
        }
        if (indicatorBoard == null || rootParams != root) {
            indicatorBoard = new FIndicatorBoard(rootParams);
            indicatorBoard.loadConfig();
            root = rootParams;
        }
        return indicatorBoard;
    }

    /**
     * Получить форму табло.
     * @param rootParams параметры табло.
     * @param isMain режим. Главное или клиентское
     * @return
     */
    public static FIndicatorBoard getIndicatorBoard(Element rootParams, boolean isMain) {
        if (indicatorBoard != null && FIndicatorBoard.isMain != isMain) {
            FIndicatorBoard.isMain = isMain;
            indicatorBoard = null;
        }
        FIndicatorBoard.isMain = isMain;
        return getIndicatorBoard(rootParams);
    }

    /**
     * Конструктор формы с указанием количества строк на табло.
     * @param configFilePath файл конфигурации табло.
     */
    private FIndicatorBoard(Element rootParams) {

        Uses.log.logger.info("Создаем окно для информации.");

        topElement = rootParams.element(Uses.TAG_BOARD_TOP);
        bottomElement = rootParams.element(Uses.TAG_BOARD_BOTTOM);
        leftElement = rootParams.element(Uses.TAG_BOARD_LEFT);
        rightElement = rootParams.element(Uses.TAG_BOARD_RIGHT);
        mainElement = rootParams.element(Uses.TAG_BOARD_MAIN);
        // Проствим кол-во строк и др. параметры
        this.linesCount = Integer.parseInt(Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_LINES_COUNT).get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        this.pause = Integer.parseInt(Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_DELAY_VISIBLE).get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        // Определим цвет табло
        this.bgColor = new Color(Integer.parseInt(Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FON_COLOR).get(0).attributeValue(Uses.TAG_BOARD_VALUE)));
        this.fgColorCaprion = new Color(Integer.parseInt(Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_COLOR_CAPTION).get(0).attributeValue(Uses.TAG_BOARD_VALUE)));
        this.fgColorLeft = new Color(Integer.parseInt(Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_COLOR_LEFT).get(0).attributeValue(Uses.TAG_BOARD_VALUE)));
        this.fgColorRight = new Color(Integer.parseInt(Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_COLOR_RIGHT).get(0).attributeValue(Uses.TAG_BOARD_VALUE)));
        this.borderLine = "1".equals(Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_LINE_BORDER).get(0).attributeValue(Uses.TAG_BOARD_VALUE));

        // Определим форму нв монитор
        setLocation(Integer.parseInt(rootParams.attributeValue("x")), Integer.parseInt(rootParams.attributeValue("y")));

        // Отрехтуем форму в зависимости от режима.
        if (!Uses.isDebug) {
            setUndecorated(true);
            setAlwaysOnTop(true);
            setResizable(false);
            // спрячем курсор мыши
            int[] pixels = new int[16 * 16];
            Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
            Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
            setCursor(transparentCursor);
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowOpened(WindowEvent e) {
                    setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
            });
        } else {
            setSize(1024, 768);
        }
        initComponents();
        panelCommon.setBackground(bgColor);
    }

    /**
     * Загрузка размеров областей табло
     */
    private void loadDividerLocation() {
        double up = 0;
        double down = 1;
        double left = 0;
        double right = 1;
        if ("1".equals(topElement.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL))) {
            up = Double.parseDouble(topElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE));
            spUp.setDividerLocation(up);
            panelUp.refreshVideoSize();
        } else {
            panelUp.setVisible(false);
        }
        if ("1".equals(bottomElement.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL))) {
            down = Double.parseDouble(bottomElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE));
            spDown.setDividerLocation(down);
            panelDown.refreshVideoSize();
        } else {
            panelDown.setVisible(false);
        }
        if ("1".equals(leftElement.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL))) {
            left = Double.parseDouble(leftElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE));
            spLeft.setDividerLocation(left);
            panelLeft.refreshVideoSize();
        } else {
            panelLeft.setVisible(false);
        }
        if ("1".equals(rightElement.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL))) {
            right = Double.parseDouble(rightElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE));
            spRight.setDividerLocation(right);
            panelRight.refreshVideoSize();
        } else {
            panelRight.setVisible(false);
        }

    }

    /**
     * Загрузка содержимого областей табло
     */
    private void loadConfig() {
        //загрузим фоновый рисунок
        String filePath = Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FON_IMG).get(0).attributeValue(Uses.TAG_BOARD_VALUE);
        File f = new File(filePath);
        if (f.exists()) {
            panelCommon.setBackgroundImgage(filePath);
        }

        loadPanel(topElement, rlTop, panelUp);
        loadPanel(bottomElement, rlDown, panelDown);
        loadPanel(leftElement, rlLeft, panelLeft);
        loadPanel(rightElement, rlRight, panelRight);
        showLines();
    }

    private void loadPanel(Element params, RunningLabel label, QPanel panel) {
        if (!"1".equals(params.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL))) {
            return;
        }
        // цвет панельки
        label.setBackground(bgColor);
        //загрузим размер и цвет шрифта
        Font font = new Font(label.getFont().getName(), label.getFont().getStyle(), Integer.parseInt(Uses.elementsByAttr(params, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_SIZE).get(0).attributeValue(Uses.TAG_BOARD_VALUE)));
        label.setForeground(new Color(Integer.parseInt(Uses.elementsByAttr(params, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_COLOR).get(0).attributeValue(Uses.TAG_BOARD_VALUE))));
        label.setFont(font);

        // загрузим текст
        label.setText(params.getTextTrim());
        label.setRunningText(Uses.elementsByAttr(params, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_RUNNING_TEXT).get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        if (!"".equals(label.getRunningText())) {
            label.setSpeedRunningText(Integer.parseInt(Uses.elementsByAttr(params, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_SPEED_TEXT).get(0).attributeValue(Uses.TAG_BOARD_VALUE)));
            label.start();
        }
        if ("1".equals(Uses.elementsByAttr(params, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_SIMPLE_DATE).get(0).attributeValue(Uses.TAG_BOARD_VALUE))) {
            label.setShowTime(true);
        }
        //загрузим фоновый рисунок
        String filePath = Uses.elementsByAttr(params, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FON_IMG).get(0).attributeValue(Uses.TAG_BOARD_VALUE);
        File fp = new File(filePath);
        if (fp.exists()) {
            label.setBackgroundImage(filePath);
        }
        //загрузим видео
        filePath = Uses.elementsByAttr(params, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_VIDEO_FILE).get(0).attributeValue(Uses.TAG_BOARD_VALUE);
        File fv = new File(filePath);
        if (fv.exists()) {
            label.setVisible(false);
            panel.setVideoFileName(filePath);
            panel.startVideo();
        }
    }

    private static ResourceMap localeMap = null;

    private static String getLocaleMessage(String key) {
        if (localeMap == null) {
            localeMap = Application.getInstance(QSystem.class).getContext().getResourceMap(FIndicatorBoard.class);
        }
        return localeMap.getString(key);
    }

    private final static Border border = new TitledBorder(getLocaleMessage("board.cell"));//  MatteBorder(1, 3, 1, 2, Color.LIGHT_GRAY);

    public class Line extends JPanel {

        private final JLabel left;
        private final JLabel right;

        public Line() {
            super();
            setOpaque(false);
            if (borderLine) {
                setBorder(border);
            }
            setLayout(new GridLayout(1, isMain ? 2 : 1, 0, 0));

            setBounds(0, 0, 100, 100);
            left = new JLabel();
            final Font font = new Font(left.getFont().getName(), left.getFont().getStyle(), Integer.parseInt(Uses.elementsByAttr(indicatorBoard.mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_SIZE_LINE).get(0).attributeValue(Uses.TAG_BOARD_VALUE)));
            left.setFont(font);
            left.setBackground(indicatorBoard.bgColor);
            left.setForeground(indicatorBoard.fgColorLeft);
            left.setHorizontalAlignment(JLabel.CENTER);
            left.setVerticalAlignment(JLabel.CENTER);
            //left.setOpaque(true);
            add(left);
            left.setBounds(0, 0, 100, 100);
            left.setText("");
            left.setVisible(true);

            if (isMain) {
                right = new JLabel();
                right.setFont(font);
                right.setBackground(indicatorBoard.bgColor);
                right.setForeground(indicatorBoard.fgColorRight);
                right.setHorizontalAlignment(JLabel.CENTER);
                right.setVerticalAlignment(JLabel.CENTER);
                //lab.setOpaque(true);
                add(right);
                right.setBounds(0, 0, 100, 100);
                right.setText("");
                right.setVisible(true);
            } else {
                right = null;
            }
        }

        public void setLineData(String text) {
            if (isMain) {
                final String[] ss = text.split(Uses.getNumeration().getDelimiter());
                if (ss.length == 2) {
                    left.setText(ss[0]);
                    right.setText(ss[1]);
                } else {
                    if (ss.length == 1) {
                        left.setText(ss[0]);
                        right.setText("");
                    } else {
                        left.setText("");
                        right.setText("");
                    }
                }
            } else {
                left.setText(text);
            }
        }
        /**
         * blinkCount 0 - постоянное мигание, -1 не мигает. число - количество миганий
         */
        int blinkCount = -1;

        public void setBlinkCount(int count) {
            blinkCount = count;
            if (clock != null && clock.isActive()) {
                clock.stop();
                clock = null;
                left.setVisible(true);
                if (right != null) {
                    right.setVisible(true);
                }
            }

        }
        boolean vis = true;

        public void startBlink() {
            if (clock != null && clock.isActive()) {
                clock.stop();
                clock = null;
            }
            if (blinkCount == -1) {
                left.setVisible(true);
                if (right != null) {
                    right.setVisible(true);
                }
                return;
            }
            vis = true;
            clock = new ATalkingClock(500, blinkCount) {

                @Override
                public void run() {
                    left.setVisible(vis);
                    if (right != null) {
                        right.setVisible(vis);
                    }
                    vis = !vis;
                }
            };
            clock.start();
        }
        private ATalkingClock clock = null;
    }
    /**
     * Массив контролов для вывода инфы.
     */
    public final ArrayList<Line> labels = new ArrayList<Line>();
    /**
     * Количество выводимых строк
     */
    private final int linesCount;
    /**
     * Цвет фона табло
     */
    private final Color bgColor;
    /**
     * Цвет шрифта заголовка
     */
    private final Color fgColorCaprion;
    /**
     * Цвет шрифта левого столбца
     */
    private final Color fgColorLeft;
    /**
     * Цвет шрифта правого столбца
     */
    private final Color fgColorRight;
    /**
     * Окантовка Строк
     */
    private final boolean borderLine;

    public int getLinesCount() {
        return linesCount;
    }
    /**
     * Минимальное время индикации на табло
     */
    private final int pause;

    public int getPause() {
        return pause;
    }

    /**
     * Создаем и расставляем контролы для строк по форме.
     */
    public void showLines() {
        Uses.log.logger.info("Показываем набор строк.");
        GridLayout la = new GridLayout(indicatorBoard.linesCount + (isMain ? 1 : 0), 1, 0, 0);
        indicatorBoard.panelMain.setLayout(la);
        if (isMain) {
            final JPanel panel_cap = new JPanel();
            if (borderLine) {
                panel_cap.setBorder(new MatteBorder(5, 3, 1, 2, Color.LIGHT_GRAY));
                //panel_cap.setBorder(new LineBorder(Color.lightGray, 6));
            } else {
                panel_cap.setBorder(new LineBorder(Color.lightGray, 0));
            }
            panel_cap.setOpaque(false);
            panel_cap.setLayout(new GridLayout(1, 2, 0, 0));
            indicatorBoard.panelMain.add(panel_cap);
            panel_cap.setBounds(0, 0, 100, 100);
            JLabel lab_cap_l = new JLabel();
            final Font font_cap = new Font(lab_cap_l.getFont().getName(), lab_cap_l.getFont().getStyle(), Integer.parseInt(Uses.elementsByAttr(indicatorBoard.mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_SIZE_CAPTION).get(0).attributeValue(Uses.TAG_BOARD_VALUE)));
            lab_cap_l.setFont(font_cap);
            lab_cap_l.setBackground(indicatorBoard.bgColor);
            lab_cap_l.setForeground(indicatorBoard.fgColorCaprion);
            lab_cap_l.setHorizontalAlignment(JLabel.CENTER);
            lab_cap_l.setVerticalAlignment(JLabel.CENTER);
            //lab.setOpaque(true);
            panel_cap.add(lab_cap_l);
            lab_cap_l.setBounds(0, 0, 100, 100);
            lab_cap_l.setText(getLocaleMessage("board.client"));

            lab_cap_l = new JLabel();
            lab_cap_l.setFont(font_cap);
            lab_cap_l.setBackground(indicatorBoard.bgColor);
            lab_cap_l.setForeground(indicatorBoard.fgColorCaprion);
            lab_cap_l.setHorizontalAlignment(JLabel.CENTER);
            lab_cap_l.setVerticalAlignment(JLabel.CENTER);
            //lab.setOpaque(true);
            panel_cap.add(lab_cap_l);
            lab_cap_l.setBounds(0, 0, 100, 100);
            lab_cap_l.setText(getLocaleMessage("board.point"));
        }
        for (int i = 1; i <= indicatorBoard.linesCount; i++) {
            final Line panel = new Line();
            indicatorBoard.labels.add(panel);
            indicatorBoard.panelMain.add(panel);
        }
        indicatorBoard.repaint();
    }

    /**
     * Метод вывода инфы на табло.
     * @param index номер строки.
     * @param text выводимый текст.
     * @param blinkCount 0 - постоянное мигание, -1 не мигает. число - количество миганий
     */
    public static void printRecord(int index, String text, int blinkCount) {
        if (indicatorBoard == null) {
            Uses.log.logger.warn("Попытка вывода на табло. Табло не демонстрируется. Отключено в настройках.");
            return;
        }
        if (index < indicatorBoard.linesCount) {
            indicatorBoard.labels.get(index).setLineData(text);
            indicatorBoard.labels.get(index).setBlinkCount(blinkCount == -1 ? -1 : blinkCount * 2);
            if (blinkCount != -1) {
                indicatorBoard.labels.get(index).startBlink();
            }
        }
    }

    /**
     * Включение/выключение звука видеородиков на табло
     * @param mute наличие звука в роликах
     */
    public void setMute(boolean mute) {
        panelUp.setMute(mute);
        panelLeft.setMute(mute);
        panelRight.setMute(mute);
        panelDown.setMute(mute);
    }

    /**
     * Выключение видеородиков на табло
     */
    public void closeVideo() {
        panelUp.closeVideo();
        panelLeft.closeVideo();
        panelRight.closeVideo();
        panelDown.closeVideo();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelCommon = new ru.apertum.qsystem.client.model.QPanel();
        spUp = new javax.swing.JSplitPane();
        panelUp = new ru.apertum.qsystem.client.model.QPanel();
        rlTop = new ru.apertum.qsystem.common.RunningLabel();
        spDown = new javax.swing.JSplitPane();
        panelDown = new ru.apertum.qsystem.client.model.QPanel();
        rlDown = new ru.apertum.qsystem.common.RunningLabel();
        spLeft = new javax.swing.JSplitPane();
        panelLeft = new ru.apertum.qsystem.client.model.QPanel();
        rlLeft = new ru.apertum.qsystem.common.RunningLabel();
        spRight = new javax.swing.JSplitPane();
        panelRight = new ru.apertum.qsystem.client.model.QPanel();
        rlRight = new ru.apertum.qsystem.common.RunningLabel();
        panelMain = new ru.apertum.qsystem.client.model.QPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getResourceMap(FIndicatorBoard.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        panelCommon.setBorder(new javax.swing.border.MatteBorder(null));
        panelCommon.setName("panelCommon"); // NOI18N
        panelCommon.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panelCommonComponentResized(evt);
            }
        });

        spUp.setBorder(new javax.swing.border.MatteBorder(null));
        spUp.setDividerLocation(100);
        spUp.setDividerSize(0);
        spUp.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        spUp.setName("spUp"); // NOI18N
        spUp.setOpaque(false);

        panelUp.setBorder(new javax.swing.border.MatteBorder(null));
        panelUp.setName("panelUp"); // NOI18N
        panelUp.setNativePosition(java.lang.Boolean.FALSE);
        panelUp.setOpaque(false);
        panelUp.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panelUpComponentResized(evt);
            }
        });

        rlTop.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rlTop.setText(resourceMap.getString("rlTop.text")); // NOI18N
        rlTop.setName("rlTop"); // NOI18N
        rlTop.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseExited(evt);
            }
        });
        rlTop.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseMoved(evt);
            }
        });

        javax.swing.GroupLayout panelUpLayout = new javax.swing.GroupLayout(panelUp);
        panelUp.setLayout(panelUpLayout);
        panelUpLayout.setHorizontalGroup(
            panelUpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rlTop, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
        );
        panelUpLayout.setVerticalGroup(
            panelUpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rlTop, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
        );

        spUp.setTopComponent(panelUp);

        spDown.setBorder(new javax.swing.border.MatteBorder(null));
        spDown.setDividerLocation(250);
        spDown.setDividerSize(0);
        spDown.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        spDown.setName("spDown"); // NOI18N
        spDown.setOpaque(false);

        panelDown.setBorder(new javax.swing.border.MatteBorder(null));
        panelDown.setName("panelDown"); // NOI18N
        panelDown.setNativePosition(java.lang.Boolean.FALSE);
        panelDown.setOpaque(false);
        panelDown.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panelDownComponentResized(evt);
            }
        });

        rlDown.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rlDown.setText(resourceMap.getString("rlDown.text")); // NOI18N
        rlDown.setName("rlDown"); // NOI18N
        rlDown.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseExited(evt);
            }
        });
        rlDown.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseMoved(evt);
            }
        });

        javax.swing.GroupLayout panelDownLayout = new javax.swing.GroupLayout(panelDown);
        panelDown.setLayout(panelDownLayout);
        panelDownLayout.setHorizontalGroup(
            panelDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rlDown, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
        );
        panelDownLayout.setVerticalGroup(
            panelDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rlDown, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
        );

        spDown.setBottomComponent(panelDown);

        spLeft.setBorder(new javax.swing.border.MatteBorder(null));
        spLeft.setDividerLocation(150);
        spLeft.setDividerSize(0);
        spLeft.setName("spLeft"); // NOI18N
        spLeft.setOpaque(false);

        panelLeft.setBorder(new javax.swing.border.MatteBorder(null));
        panelLeft.setName("panelLeft"); // NOI18N
        panelLeft.setNativePosition(java.lang.Boolean.FALSE);
        panelLeft.setOpaque(false);
        panelLeft.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseExited(evt);
            }
        });
        panelLeft.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panelLeftComponentResized(evt);
            }
        });
        panelLeft.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseMoved(evt);
            }
        });

        rlLeft.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rlLeft.setText(resourceMap.getString("rlLeft.text")); // NOI18N
        rlLeft.setName("rlLeft"); // NOI18N
        rlLeft.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseExited(evt);
            }
        });
        rlLeft.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseMoved(evt);
            }
        });

        javax.swing.GroupLayout panelLeftLayout = new javax.swing.GroupLayout(panelLeft);
        panelLeft.setLayout(panelLeftLayout);
        panelLeftLayout.setHorizontalGroup(
            panelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rlLeft, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
        );
        panelLeftLayout.setVerticalGroup(
            panelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rlLeft, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
        );

        spLeft.setLeftComponent(panelLeft);

        spRight.setBorder(new javax.swing.border.MatteBorder(null));
        spRight.setDividerLocation(250);
        spRight.setDividerSize(0);
        spRight.setName("spRight"); // NOI18N
        spRight.setOpaque(false);

        panelRight.setBackground(resourceMap.getColor("panelRight.background")); // NOI18N
        panelRight.setBorder(new javax.swing.border.MatteBorder(null));
        panelRight.setName("panelRight"); // NOI18N
        panelRight.setNativePosition(java.lang.Boolean.FALSE);
        panelRight.setOpaque(false);
        panelRight.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panelRightComponentResized(evt);
            }
        });

        rlRight.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rlRight.setText(resourceMap.getString("rlRight.text")); // NOI18N
        rlRight.setName("rlRight"); // NOI18N
        rlRight.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseExited(evt);
            }
        });
        rlRight.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseMoved(evt);
            }
        });

        javax.swing.GroupLayout panelRightLayout = new javax.swing.GroupLayout(panelRight);
        panelRight.setLayout(panelRightLayout);
        panelRightLayout.setHorizontalGroup(
            panelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rlRight, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
        );
        panelRightLayout.setVerticalGroup(
            panelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rlRight, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
        );

        spRight.setRightComponent(panelRight);

        panelMain.setBorder(new javax.swing.border.MatteBorder(null));
        panelMain.setName("panelMain"); // NOI18N
        panelMain.setOpaque(false);
        panelMain.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panelMainComponentResized(evt);
            }
        });
        panelMain.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseMoved(evt);
            }
        });

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 247, Short.MAX_VALUE)
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 243, Short.MAX_VALUE)
        );

        spRight.setLeftComponent(panelMain);

        spLeft.setRightComponent(spRight);

        spDown.setLeftComponent(spLeft);

        spUp.setRightComponent(spDown);

        javax.swing.GroupLayout panelCommonLayout = new javax.swing.GroupLayout(panelCommon);
        panelCommon.setLayout(panelCommonLayout);
        panelCommonLayout.setHorizontalGroup(
            panelCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spUp, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
        );
        panelCommonLayout.setVerticalGroup(
            panelCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spUp, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelCommon, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelCommon, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized

    loadDividerLocation();

}//GEN-LAST:event_formComponentResized

private void panelCommonComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelCommonComponentResized

    loadDividerLocation();
}//GEN-LAST:event_panelCommonComponentResized

private void panelMainComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelMainComponentResized

    loadDividerLocation();
}//GEN-LAST:event_panelMainComponentResized

private void panelDownComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelDownComponentResized

    loadDividerLocation();
}//GEN-LAST:event_panelDownComponentResized

private void panelUpComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelUpComponentResized

    loadDividerLocation();
}//GEN-LAST:event_panelUpComponentResized

private void panelLeftComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelLeftComponentResized

    loadDividerLocation();
}//GEN-LAST:event_panelLeftComponentResized

private void panelRightComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panelRightComponentResized

    loadDividerLocation();
}//GEN-LAST:event_panelRightComponentResized
    private Point p = null;

private void mouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseMoved
    if (p != null && !Uses.isDebug) {
        try {
            Robot rob = new Robot();
            rob.mouseMove(p.x, p.y);
        } catch (AWTException ex) {
            System.out.println("Can't move mouse to center, error in DrawingWindow.java:195");
        }
    }
}//GEN-LAST:event_mouseMoved

private void mouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseExited
    //  p = null;
}//GEN-LAST:event_mouseExited

private void mouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseEntered
    p = evt.getLocationOnScreen();
}//GEN-LAST:event_mouseEntered
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ru.apertum.qsystem.client.model.QPanel panelCommon;
    private ru.apertum.qsystem.client.model.QPanel panelDown;
    private ru.apertum.qsystem.client.model.QPanel panelLeft;
    private ru.apertum.qsystem.client.model.QPanel panelMain;
    private ru.apertum.qsystem.client.model.QPanel panelRight;
    private ru.apertum.qsystem.client.model.QPanel panelUp;
    private ru.apertum.qsystem.common.RunningLabel rlDown;
    private ru.apertum.qsystem.common.RunningLabel rlLeft;
    private ru.apertum.qsystem.common.RunningLabel rlRight;
    private ru.apertum.qsystem.common.RunningLabel rlTop;
    private javax.swing.JSplitPane spDown;
    private javax.swing.JSplitPane spLeft;
    private javax.swing.JSplitPane spRight;
    private javax.swing.JSplitPane spUp;
    // End of variables declaration//GEN-END:variables
}
