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

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import org.dom4j.Element;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import ru.apertum.qsystem.QSystem;
import ru.apertum.qsystem.client.model.QPanel;
import ru.apertum.qsystem.common.BrowserFX;
import ru.apertum.qsystem.common.RunningLabel;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.model.ATalkingClock;

/**
 * Created on 22 Сентябрь 2008 г., 14:27
 *
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
    private final Element bottomElement2;
    private final Element leftElement;
    private final Element rightElement;
    private final Element mainElement;
    private static FIndicatorBoard indicatorBoard = null;
    private Element root = null;
    /**
     * Режим. Главное табло или клиентское.
     */
    private static boolean isMain = true;

    /**
     * Получить форму табло. Получаем главное табло. Если требуется получить с указанием типа то использовать public static FIndicatorBoard
     * getIndicatorBoard(Element rootParams, boolean isMain)
     *
     * @param rootParams параметры табло.
     * @return
     */
    public static FIndicatorBoard getIndicatorBoard(Element rootParams) {
        if (!"1".equals(rootParams.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL)) && !rootParams.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL).startsWith("$")) {
            return null;
        }
        if (indicatorBoard == null || rootParams != indicatorBoard.root) {
            indicatorBoard = new FIndicatorBoard(rootParams, QLog.l().isDebug());
            indicatorBoard.loadConfig();
            indicatorBoard.root = rootParams;
        }
        if (indicatorBoard.monitor < 1) {
            return null;
        }
        return indicatorBoard;
    }

    /**
     * Получить форму табло. Получаем главное табло. Вызов этого метода создает новый объект. не использовать при одиночном табло. сделано для зонального.
     *
     * @param rootParams параметры табло.
     * @param isDebug
     * @return
     */
    public static FIndicatorBoard getIndicatorBoardForZone(Element rootParams, boolean isDebug) {
        if (!"1".equals(rootParams.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL))) {
            return null;
        }
        final FIndicatorBoard iBoard = new FIndicatorBoard(rootParams, isDebug);
        iBoard.loadConfig();
        iBoard.root = rootParams;
        iBoard.zoneDebug = isDebug;
        return iBoard;
    }
    private boolean zoneDebug = false;

    /**
     * Получить форму табло.
     *
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
     *
     * @param configFilePath файл конфигурации табло.
     */
    private FIndicatorBoard(Element rootParams, boolean isDebug) {

        QLog.l().logger().info("Создаем окно для информации.");

        topElement = rootParams.element(Uses.TAG_BOARD_TOP);
        bottomElement = rootParams.element(Uses.TAG_BOARD_BOTTOM);
        bottomElement2 = rootParams.element(Uses.TAG_BOARD_BOTTOM_2);
        leftElement = rootParams.element(Uses.TAG_BOARD_LEFT);
        rightElement = rootParams.element(Uses.TAG_BOARD_RIGHT);
        mainElement = rootParams.element(Uses.TAG_BOARD_MAIN);
        // Проствим кол-во строк и др. параметры
        final ArrayList<Element> lst = Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_MONITOR);
        this.monitor = lst.isEmpty() ? 100 : Integer.parseInt(lst.get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        this.linesCount = Integer.parseInt(Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_LINES_COUNT).get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        final int ii = Integer.parseInt(Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_COLS_COUNT).get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        this.colsCount = isMain ? (ii > 0 ? (ii > 5 ? 5 : ii) : 1) : 1;
        this.pause = Integer.parseInt(Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_DELAY_VISIBLE).get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        // Определим цвет табло
        this.bgColor = Color.decode("#" + Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FON_COLOR).get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        this.fgColorCaprion = Color.decode("#" + Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_COLOR_CAPTION).get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        this.fgColorLeft = Color.decode("#" + Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_COLOR_LEFT).get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        this.fgColorRight = Color.decode("#" + Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_COLOR_RIGHT).get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        this.borderLine = "1".equals(Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_LINE_BORDER).get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        this.delimiter = Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_LINE_DELIMITER).get(0).attributeValue(Uses.TAG_BOARD_VALUE);

        this.colorTextLine = Color.decode("#" + Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_COLOR_LINE).get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        this.colorRow = Color.decode("#" + Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_LINE_COLOR).get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        this.rowCaption = Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_LINE_CAPTION).get(0).attributeValue(Uses.TAG_BOARD_VALUE);
        this.leftColCaption = Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_LEFT_CAPTION).get(0).attributeValue(Uses.TAG_BOARD_VALUE);
        ArrayList<Element> li = Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_RIGHT_CAPTION);
        this.rightColCaption = li.isEmpty() ? "" : li.get(0).attributeValue(Uses.TAG_BOARD_VALUE);
        li = Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_EXT_CAPTION);
        this.extColCaption = li.isEmpty() ? "" : li.get(0).attributeValue(Uses.TAG_BOARD_VALUE);
        li = Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_EXT_POSITION);
        int t = Integer.parseInt(li.isEmpty() ? "0" : li.get(0).attributeValue(Uses.TAG_BOARD_VALUE));
        t = t < 0 ? 0 : t;
        t = t > 3 ? 3 : t;
        this.extColPosition = t;
        this.border = new TitledBorder(new LineBorder(colorRow), "".equals(rowCaption) ? getLocaleMessage("board.cell") : rowCaption);//  MatteBorder(1, 3, 1, 2, Color.LIGHT_GRAY);
        border.setTitleColor(colorTextLine);

        if (!isDebug) {
            setUndecorated(true);
            setType(Type.UTILITY);
        }
        initComponents();
        panelCommon.setBackground(bgColor);
        QLog.l().logger().trace("Прочитали настройки для окна информации.");
    }

    public void toPosition(boolean isDebug, int x, int y) {
        // Определим форму на монитор
        final Rectangle bounds = Uses.displays.get(monitor);
        if (bounds != null) {
            x = bounds.x + 1;
            y = bounds.y + 1;
        }

        setLocation(x, y);
        setAlwaysOnTop(!isDebug);
        // setResizable(isDebug);
        // Отрехтуем форму в зависимости от режима.
        if (!isDebug) {

            setAlwaysOnTop(true);
            //   setResizable(false);
            // спрячем курсор мыши
            int[] pixels = new int[16 * 16];
            Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
            Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
            setCursor(transparentCursor);
            setBounds(x, y, 200, 300);
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowOpened(WindowEvent e) {
                    setExtendedState(JFrame.MAXIMIZED_BOTH);
                }
            });
        } else {
            setSize(1280, 720);
        }
    }

    /**
     * Загрузка размеров областей табло
     */
    private void loadDividerLocation() {
        double down = 1;
        if ("1".equals(topElement.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL))) {
            double up = Double.parseDouble(topElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE));
            spUp.setDividerLocation(up);
            panelUp.refreshVideoSize();
        } else {
            panelUp.setVisible(false);
        }

        if ("0".equals(bottomElement2.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL)) && "0".equals(bottomElement.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL))) {
            panelDown.setVisible(false);
            panelDown2.setVisible(false);
            spDown.setDividerLocation(down);

        } else {
            if ("1".equals(bottomElement2.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL)) && "1".equals(bottomElement.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL))) {
                if ("1".equals(bottomElement.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL))) {
                    down = Double.parseDouble(bottomElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE));
                    spDown.setDividerLocation(down);
                    panelDown.refreshVideoSize();
                } else {
                    panelDown.setVisible(false);
                }
                if ("1".equals(bottomElement2.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL))) {
                    double down2 = Double.parseDouble(bottomElement2.attributeValue(Uses.TAG_BOARD_PANEL_SIZE));
                    spDown2.setDividerLocation(down2);
                    panelDown2.refreshVideoSize();
                } else {
                    panelDown2.setVisible(false);
                }
            } else {
                if ("1".equals(bottomElement.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL))) {
                    down = Double.parseDouble(bottomElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE))
                            + (1 - Double.parseDouble(bottomElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE)))
                            - (1 - Double.parseDouble(bottomElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE)))
                            * Double.parseDouble(bottomElement2.attributeValue(Uses.TAG_BOARD_PANEL_SIZE));
                    spDown.setDividerLocation(down);
                    spDown2.setDividerLocation(1);
                    panelDown2.setVisible(false);
                    panelDown.refreshVideoSize();
                } else {
                    down = Double.parseDouble(bottomElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE))
                            + (1 - Double.parseDouble(bottomElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE)))
                            - (1 - Double.parseDouble(bottomElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE)))
                            * (1 - Double.parseDouble(bottomElement2.attributeValue(Uses.TAG_BOARD_PANEL_SIZE)));
                    spDown.setDividerLocation(down);
                    spDown2.setDividerLocation(0);
                    panelDown.setVisible(false);
                    panelDown2.refreshVideoSize();
                }
            }
        }
        if ("1".equals(leftElement.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL))) {
            double left = Double.parseDouble(leftElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE));
            spLeft.setDividerLocation(left);
            panelLeft.refreshVideoSize();
        } else {
            panelLeft.setVisible(false);
        }
        if ("1".equals(rightElement.attributeValue(Uses.TAG_BOARD_VISIBLE_PANEL))) {
            double right = Double.parseDouble(rightElement.attributeValue(Uses.TAG_BOARD_PANEL_SIZE));
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

        nexts.clear();
        el_nexts.clear();
        loadPanel(topElement, rlTop, panelUp);
        loadPanel(bottomElement, rlDown, panelDown);
        loadPanel(bottomElement2, rlDown2, panelDown2);
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
        final Font font = new Font(label.getFont().getName(), label.getFont().getStyle(), Integer.parseInt(Uses.elementsByAttr(params, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_SIZE).get(0).attributeValue(Uses.TAG_BOARD_VALUE)));
        label.setForeground(Color.decode("#" + (Uses.elementsByAttr(params, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_COLOR).get(0).attributeValue(Uses.TAG_BOARD_VALUE))));
        label.setFont(font);

        //загрузим фоновый рисунок
        final String filePath = Uses.elementsByAttr(params, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FON_IMG).get(0).attributeValue(Uses.TAG_BOARD_VALUE);
        File fp = new File(filePath);
        if (fp.exists()) {
            label.setBackgroundImage(filePath);
        }

        //загрузим видео
        final String filePathVid = Uses.elementsByAttr(params, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_VIDEO_FILE).get(0).attributeValue(Uses.TAG_BOARD_VALUE);
        File fv = new File(filePathVid);
        if (fv.exists()) {
            label.setVisible(false);
            panel.setVideoFileName(filePathVid);
            panel.startVideo();
        } else {
            // если не видео, то простая дата или таблица ближайших
            if ("1".equals(Uses.elementsByAttr(params, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_SIMPLE_DATE).get(0).attributeValue(Uses.TAG_BOARD_VALUE))) {
                label.setRunningText("");
                label.setText("");
                label.setShowTime(true);
            } else {
                // загрузим текст
                if ("1".equals(Uses.elementsByAttr(params, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_GRID_NEXT).get(0).attributeValue(Uses.TAG_BOARD_VALUE))) {
                    // таблица ближайших
                    label.setVerticalAlignment(1);
                    label.setRunningText("");
                    label.setText("<HTML>"
                            + "<table  cellpadding='5' align='center' border='"
                            + Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_GRID_NEXT_FRAME_BORDER).get(0).attributeValue(Uses.TAG_BOARD_VALUE)
                            + "' bordercolor='0'>"
                            + "<tr><td>"
                            + "<p align=center>"
                            + "<span style='font-size:" + Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_SIZE_CAPTION).get(0).attributeValue(Uses.TAG_BOARD_VALUE) + ".0pt;color:" + Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_COLOR_CAPTION).get(0).attributeValue(Uses.TAG_BOARD_VALUE) + ";'>"
                            + Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_GRID_NEXT_CAPTION).get(0).attributeValue(Uses.TAG_BOARD_VALUE)
                            + "</span></p>"
                            + "</td></tr>"
                            + "<tr>"
                            + "</table>");
                    nexts.add(label);
                    el_nexts.put(label, params);
                } else {
                    final String rt = Uses.elementsByAttr(params, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_RUNNING_TEXT).get(0).attributeValue(Uses.TAG_BOARD_VALUE).trim();
                    if (!"".equals(rt)) {
                        label.setRunningText(rt);
                        label.setText("");
                        label.setSpeedRunningText(Integer.parseInt(Uses.elementsByAttr(params, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_SPEED_TEXT).get(0).attributeValue(Uses.TAG_BOARD_VALUE)));
                        label.start();
                    } else {
                        // просто хтмл-текст или URL
                        final String txt = params.getTextTrim();
                        Pattern replace = Pattern.compile(pattern);
                        Matcher matcher = replace.matcher(txt);
                        if (matcher.matches() || txt.contains("localhost") || txt.contains("127.0.0.1")) {
                            panel.removeAll();
                            GridLayout gl = new GridLayout(1, 1);
                            panel.setLayout(gl);
                            BrowserFX bfx = new BrowserFX();
                            panel.add(bfx, BorderLayout.CENTER);
                            bfx.load(txt);
                        } else {
                            label.setText(Uses.prepareAbsolutPathForImg(txt));
                            label.setRunningText("");
                        }
                    }//бегущий
                }//ближайшие
            }//время
        }//видео
    }
    private final static String pattern = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?";
    /**
     * это лейблы, в которых будут таблицы ближайших клиентосов
     */
    private final ArrayList<RunningLabel> nexts = new ArrayList<>();
    private final HashMap<RunningLabel, Element> el_nexts = new HashMap<>();

    public boolean needNext() {
        return !nexts.isEmpty();
    }

    public void showNext(LinkedList<String> list) {
        for (RunningLabel rl : nexts) {
            String grid = "<HTML>"
                    + "<table cellpadding='5' align='center' border='1' frame='border' bordercolor='0'>"
                    + "<tr><td colspan='" + Uses.elementsByAttr(el_nexts.get(rl), Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_GRID_NEXT_COLS).get(0).attributeValue(Uses.TAG_BOARD_VALUE) + "'>"
                    + "<p align=center>"
                    + "<span style='font-size:" + Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_SIZE_CAPTION).get(0).attributeValue(Uses.TAG_BOARD_VALUE) + ".0pt;color:" + Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_COLOR_CAPTION).get(0).attributeValue(Uses.TAG_BOARD_VALUE) + ";'>"
                    + Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_GRID_NEXT_CAPTION).get(0).attributeValue(Uses.TAG_BOARD_VALUE)
                    + "</span></p>"
                    + "</td></tr>";
            int t = 0;
            for (int i = 0; i < Integer.parseInt(Uses.elementsByAttr(el_nexts.get(rl), Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_GRID_NEXT_ROWS).get(0).attributeValue(Uses.TAG_BOARD_VALUE)); i++) {
                grid = grid + "<tr>";
                for (int j = 0; j < Integer.parseInt(Uses.elementsByAttr(el_nexts.get(rl), Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_GRID_NEXT_COLS).get(0).attributeValue(Uses.TAG_BOARD_VALUE)); j++) {
                    grid = grid + "<td>"
                            + "<p align=center><span style='font-size:" + Uses.elementsByAttr(el_nexts.get(rl), Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_SIZE).get(0).attributeValue(Uses.TAG_BOARD_VALUE) + ".0pt;color:" + Uses.elementsByAttr(el_nexts.get(rl), Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_COLOR).get(0).attributeValue(Uses.TAG_BOARD_VALUE) + ";'>"
                            + (t < list.size() ? list.get(t) : "")
                            + "</span></p>"
                            + "</td>";
                    t++;
                }
                grid = grid + "</tr>";
            }
            grid = grid + "</table>";
            rl.setText(grid);
        }
    }
    private static ResourceMap localeMap = null;

    private static String getLocaleMessage(String key) {
        if (localeMap == null) {
            localeMap = Application.getInstance(QSystem.class).getContext().getResourceMap(FIndicatorBoard.class);
        }
        return localeMap.getString(key);
    }
    private final TitledBorder border;

    public class Line extends JPanel {

        private final JLabel left;
        private final JLabel right;
        private JLabel ext;

        public Line() {
            super();
            setOpaque(false);
            if (borderLine) {
                setBorder(border);
            }
            left = new JLabel();
            final Font font = new Font(left.getFont().getName(), left.getFont().getStyle(), Integer.parseInt(Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_SIZE_LINE).get(0).attributeValue(Uses.TAG_BOARD_VALUE)));

            if (isMain && extColPosition > 0) {
                ext = new JLabel();
                ext.setFont(font);
                ext.setBackground(bgColor);
                ext.setForeground(fgColorRight);
                ext.setHorizontalAlignment(JLabel.CENTER);
                ext.setVerticalAlignment(JLabel.CENTER);
                //lab.setOpaque(true);
                ext.setBounds(0, 0, 100, 100);
                ext.setText("");
                ext.setVisible(true);
            }

            setLayout(new GridLayout(1, isMain ? (extColPosition > 0 ? 3 : 2) : 1, 0, 0));
            setBounds(0, 0, 100, 100);

            if (isMain && extColPosition == 1) {
                add(ext);
            }

            left.setFont(font);
            left.setBackground(bgColor);
            left.setForeground(fgColorLeft);
            left.setHorizontalAlignment(JLabel.CENTER);
            left.setVerticalAlignment(JLabel.CENTER);
            //left.setOpaque(true);
            add(left);
            left.setBounds(0, 0, 100, 100);
            left.setText("");
            left.setVisible(true);

            if (isMain) {
                if (extColPosition == 2) {
                    add(ext);
                }
                right = new JLabel();
                right.setFont(font);
                right.setBackground(bgColor);
                right.setForeground(fgColorRight);
                right.setHorizontalAlignment(JLabel.CENTER);
                right.setVerticalAlignment(JLabel.CENTER);
                //lab.setOpaque(true);
                add(right);
                right.setBounds(0, 0, 100, 100);
                right.setText("");
                right.setVisible(true);
                if (extColPosition == 3) {
                    add(ext);
                }
            } else {
                right = null;
            }
        }

        public void setLineData(String text, String ext_data) {
            if (isMain) {
                final String[] ss = text.split(delimiter);
                if (ss.length == 2) {
                    left.setText(ss[0]);
                    right.setText(ss[1]);
                    if (ext != null) {
                        ext.setText(ext_data);
                    }
                } else {
                    if (ss.length == 1) {
                        left.setText(ss[0]);
                        right.setText("");
                        if (ext != null) {
                            ext.setText("");
                        }
                    } else {
                        left.setText("");
                        right.setText("");
                        if (ext != null) {
                            ext.setText("");
                        }
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
                if (ext != null) {
                    ext.setVisible(true);
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
                if (ext != null) {
                    ext.setVisible(true);
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
                    if (ext != null) {
                        ext.setVisible(vis);
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
    public final ArrayList<Line> labels = new ArrayList<>();
    /**
     * Номер дополнительного монитора для табло
     */
    private int monitor = 100;
    /**
     * Количество выводимых строк
     */
    private final int linesCount;
    /**
     * Количество выводимых столбцов
     */
    private final int colsCount;
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
    /**
     * Чем разделяются столбци клиента и пункта вызова на главном табло
     */
    private final String delimiter;
    /**
     * Цвет рамки строки табло
     */
    private final Color colorRow;
    /**
     * Заголовок строки табло
     */
    private final String rowCaption;
    /**
     * Заголовок левого столбца
     */
    private final String leftColCaption;
    /**
     * Заголовок правого столбца
     */
    private final String rightColCaption;
    private final String extColCaption;
    private final int extColPosition;
    /**
     * Цвет надписи строки табло
     */
    private final Color colorTextLine;

    public int getLinesCount() {
        return linesCount * colsCount;
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
        QLog.l().logger().info("Показываем набор строк.");
        GridLayout la = new GridLayout(linesCount + (isMain ? 1 : 0), (isMain ? colsCount : 1), 10, 0);
        panelMain.setLayout(la);
        final ArrayList<JPanel> caps = new ArrayList<>();
        for (int с = 0; с < colsCount; с++) {
            if (isMain) {
                final JPanel panel_cap = new JPanel();
                caps.add(panel_cap);
                if (borderLine) {
                    panel_cap.setBorder(new MatteBorder(5, 3, 1, 2, colorRow));
                    //panel_cap.setBorder(new LineBorder(Color.lightGray, 6));
                } else {
                    panel_cap.setBorder(new LineBorder(colorRow, 0));
                }
                panel_cap.setOpaque(false);
                panel_cap.setLayout(new GridLayout(1, extColPosition > 0 ? 3 : 2, 0, 0));
                //panelMain.add(panel_cap);
                panel_cap.setBounds(0, 0, 100, 100);
                JLabel lab_cap_l = new JLabel();
                final Font font_cap = new Font(lab_cap_l.getFont().getName(), lab_cap_l.getFont().getStyle(), Integer.parseInt(Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_FONT_SIZE_CAPTION).get(0).attributeValue(Uses.TAG_BOARD_VALUE)));

                JLabel lab_cap_ext = new JLabel();
                if (extColPosition > 0) {
                    lab_cap_ext = new JLabel();
                    lab_cap_ext.setFont(font_cap);
                    lab_cap_ext.setBackground(bgColor);
                    lab_cap_ext.setForeground(fgColorCaprion);
                    lab_cap_ext.setHorizontalAlignment(JLabel.CENTER);
                    lab_cap_ext.setVerticalAlignment(JLabel.CENTER);
                    //lab.setOpaque(true);
                    lab_cap_ext.setBounds(0, 0, 100, 100);
                    lab_cap_ext.setText(!"".equals(extColCaption) ? extColCaption : getLocaleMessage("board.point.ext"));
                }
                if (extColPosition == 1) {
                    panel_cap.add(lab_cap_ext);
                }

                lab_cap_l.setFont(font_cap);
                lab_cap_l.setBackground(bgColor);
                lab_cap_l.setForeground(fgColorCaprion);
                lab_cap_l.setHorizontalAlignment(JLabel.CENTER);
                lab_cap_l.setVerticalAlignment(JLabel.CENTER);
                //lab.setOpaque(true);
                panel_cap.add(lab_cap_l);
                lab_cap_l.setBounds(0, 0, 100, 100);
                lab_cap_l.setText(!"".equals(leftColCaption) ? leftColCaption : getLocaleMessage("board.client"));

                if (extColPosition == 2) {
                    panel_cap.add(lab_cap_ext);
                }

                lab_cap_l = new JLabel();
                lab_cap_l.setFont(font_cap);
                lab_cap_l.setBackground(bgColor);
                lab_cap_l.setForeground(fgColorCaprion);
                lab_cap_l.setHorizontalAlignment(JLabel.CENTER);
                lab_cap_l.setVerticalAlignment(JLabel.CENTER);
                //lab.setOpaque(true);
                panel_cap.add(lab_cap_l);
                lab_cap_l.setBounds(0, 0, 100, 100);
                lab_cap_l.setText(!"".equals(rightColCaption) ? rightColCaption : getLocaleMessage("board.point"));

                if (extColPosition == 3) {
                    panel_cap.add(lab_cap_ext);
                }
            }
        }
        if (!caps.isEmpty()) {
            for (JPanel cap : caps) {
                panelMain.add(cap);
            }
        }
        final Line[][] cels = new Line[linesCount][colsCount];
        for (int c = 0; c < colsCount; c++) {
            for (int i = 0; i < linesCount; i++) {
                final Line panel = new Line();
                labels.add(panel);
                cels[i][c] = panel;
            }
        }
        for (Line[] lines : cels) {
            for (Line line : lines) {
                panelMain.add(line);
            }
        }
        repaint();
    }

    /**
     * Метод вывода инфы на табло.
     *
     * @param index номер строки.
     * @param number номер клиента - часть выводимого текста
     * @param point пункт куда позвали клиента - часть выводимого текста
     * @param ext_data Третья колонка
     * @param blinkCount 0 - постоянное мигание, -1 не мигает. число - количество миганий
     */
    public void printRecord(int index, String number, String point, String ext_data, int blinkCount) {
        if (index < getLinesCount()) {
            labels.get(index).setLineData(number + (isMain ? delimiter + point : ""), ext_data);
            labels.get(index).setBlinkCount(blinkCount == -1 ? -1 : blinkCount * 2);
            if (blinkCount != -1) {
                labels.get(index).startBlink();
            }
        }
    }

    public void showCallPanel(String number, String point) {
        if (isMain && "1".equals(Uses.elementsByAttr(mainElement, Uses.TAG_BOARD_NAME, Uses.TAG_BOARD_CALL_PANEL).get(0).attributeValue(Uses.TAG_BOARD_VALUE))) {
            QLog.l().logger().info("Демонстрация номера вызванного \"" + number + " - " + point + "\" для показа в диалоге на главном табло.");
            FCallDialog.getInstance(this, mainElement).show(number, point);
        }
    }

    /**
     * Включение/выключение звука видеородиков на табло
     *
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

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelCommon = new ru.apertum.qsystem.client.model.QPanel();
        spUp = new javax.swing.JSplitPane();
        panelUp = new ru.apertum.qsystem.client.model.QPanel();
        rlTop = new ru.apertum.qsystem.common.RunningLabel();
        spDown = new javax.swing.JSplitPane();
        spLeft = new javax.swing.JSplitPane();
        panelLeft = new ru.apertum.qsystem.client.model.QPanel();
        rlLeft = new ru.apertum.qsystem.common.RunningLabel();
        spRight = new javax.swing.JSplitPane();
        panelRight = new ru.apertum.qsystem.client.model.QPanel();
        rlRight = new ru.apertum.qsystem.common.RunningLabel();
        panelMain = new ru.apertum.qsystem.client.model.QPanel();
        spDown2 = new javax.swing.JSplitPane();
        panelDown = new ru.apertum.qsystem.client.model.QPanel();
        rlDown = new ru.apertum.qsystem.common.RunningLabel();
        panelDown2 = new ru.apertum.qsystem.client.model.QPanel();
        rlDown2 = new ru.apertum.qsystem.common.RunningLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ru.apertum.qsystem.QSystem.class).getContext().getResourceMap(FIndicatorBoard.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setAlwaysOnTop(true);
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

        rlTop.setBorder(new javax.swing.border.MatteBorder(null));
        rlTop.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rlTop.setText(resourceMap.getString("rlTop.text")); // NOI18N
        rlTop.setFont(resourceMap.getFont("rlTop.font")); // NOI18N
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
            .addComponent(rlTop, javax.swing.GroupLayout.DEFAULT_SIZE, 911, Short.MAX_VALUE)
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

        rlLeft.setBorder(new javax.swing.border.MatteBorder(null));
        rlLeft.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rlLeft.setText(resourceMap.getString("rlLeft.text")); // NOI18N
        rlLeft.setFont(resourceMap.getFont("rlLeft.font")); // NOI18N
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
            .addGroup(panelLeftLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(rlLeft, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                .addGap(0, 0, 0))
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

        rlRight.setBorder(new javax.swing.border.MatteBorder(null));
        rlRight.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rlRight.setText(resourceMap.getString("rlRight.text")); // NOI18N
        rlRight.setFont(resourceMap.getFont("rlRight.font")); // NOI18N
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
            .addComponent(rlRight, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
        );
        panelRightLayout.setVerticalGroup(
            panelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rlRight, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
        );

        spRight.setRightComponent(panelRight);

        panelMain.setBorder(new javax.swing.border.MatteBorder(null));
        panelMain.setFont(resourceMap.getFont("rlLeft.font")); // NOI18N
        panelMain.setName("panelMain"); // NOI18N
        panelMain.setOpaque(false);
        panelMain.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                FIndicatorBoard.this.mouseMoved(evt);
            }
        });
        panelMain.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panelMainComponentResized(evt);
            }
        });

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 241, Short.MAX_VALUE)
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 243, Short.MAX_VALUE)
        );

        spRight.setLeftComponent(panelMain);

        spLeft.setRightComponent(spRight);

        spDown.setLeftComponent(spLeft);

        spDown2.setBorder(new javax.swing.border.MatteBorder(null));
        spDown2.setDividerSize(0);
        spDown2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        spDown2.setName("spDown2"); // NOI18N

        panelDown.setBorder(new javax.swing.border.MatteBorder(null));
        panelDown.setName("panelDown"); // NOI18N
        panelDown.setNativePosition(java.lang.Boolean.FALSE);
        panelDown.setOpaque(false);
        panelDown.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panelDownComponentResized(evt);
            }
        });

        rlDown.setBorder(new javax.swing.border.MatteBorder(null));
        rlDown.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rlDown.setText(resourceMap.getString("rlDown.text")); // NOI18N
        rlDown.setFont(resourceMap.getFont("rlDown.font")); // NOI18N
        rlDown.setName("rlDown"); // NOI18N
        rlDown.setRunningText(resourceMap.getString("rlDown.runningText")); // NOI18N
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
            .addComponent(rlDown, javax.swing.GroupLayout.DEFAULT_SIZE, 907, Short.MAX_VALUE)
        );
        panelDownLayout.setVerticalGroup(
            panelDownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rlDown, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
        );

        spDown2.setTopComponent(panelDown);

        panelDown2.setBorder(new javax.swing.border.MatteBorder(null));
        panelDown2.setName("panelDown2"); // NOI18N

        rlDown2.setBorder(new javax.swing.border.MatteBorder(null));
        rlDown2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rlDown2.setText(resourceMap.getString("rlDown2.text")); // NOI18N
        rlDown2.setFont(resourceMap.getFont("rlDown2.font")); // NOI18N
        rlDown2.setName("rlDown2"); // NOI18N
        rlDown2.setRunningText(resourceMap.getString("rlDown2.runningText")); // NOI18N

        javax.swing.GroupLayout panelDown2Layout = new javax.swing.GroupLayout(panelDown2);
        panelDown2.setLayout(panelDown2Layout);
        panelDown2Layout.setHorizontalGroup(
            panelDown2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rlDown2, javax.swing.GroupLayout.DEFAULT_SIZE, 907, Short.MAX_VALUE)
        );
        panelDown2Layout.setVerticalGroup(
            panelDown2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rlDown2, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
        );

        spDown2.setRightComponent(panelDown2);

        spDown.setRightComponent(spDown2);

        spUp.setRightComponent(spDown);

        javax.swing.GroupLayout panelCommonLayout = new javax.swing.GroupLayout(panelCommon);
        panelCommon.setLayout(panelCommonLayout);
        panelCommonLayout.setHorizontalGroup(
            panelCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spUp, javax.swing.GroupLayout.DEFAULT_SIZE, 915, Short.MAX_VALUE)
        );
        panelCommonLayout.setVerticalGroup(
            panelCommonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spUp, javax.swing.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
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
    if (p != null && !QLog.l().isDebug() && !zoneDebug) {
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
    private ru.apertum.qsystem.client.model.QPanel panelDown2;
    private ru.apertum.qsystem.client.model.QPanel panelLeft;
    private ru.apertum.qsystem.client.model.QPanel panelMain;
    private ru.apertum.qsystem.client.model.QPanel panelRight;
    private ru.apertum.qsystem.client.model.QPanel panelUp;
    private ru.apertum.qsystem.common.RunningLabel rlDown;
    private ru.apertum.qsystem.common.RunningLabel rlDown2;
    private ru.apertum.qsystem.common.RunningLabel rlLeft;
    private ru.apertum.qsystem.common.RunningLabel rlRight;
    private ru.apertum.qsystem.common.RunningLabel rlTop;
    private javax.swing.JSplitPane spDown;
    private javax.swing.JSplitPane spDown2;
    private javax.swing.JSplitPane spLeft;
    private javax.swing.JSplitPane spRight;
    private javax.swing.JSplitPane spUp;
    // End of variables declaration//GEN-END:variables

    public RunningLabel getTopRunningLabel() {
        return rlTop;
    }

    public RunningLabel getLeftRunningLabel() {
        return rlLeft;
    }

    public RunningLabel getRightRunningLabel() {
        return rlRight;
    }

    public RunningLabel getBottomRunningLabel() {
        return rlDown;
    }
}
