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
package ru.apertum.qsystem.client.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.exceptions.ClientException;

/**
 * Класс загрузки и предоставления настроек пункта регистрации
 * @author Evgeniy Egorov
 */
public class WelcomeParams {

    public static WelcomeParams getInstance() {
        return WelcomeParamsHolder.INSTANCE;
    }

    private static class WelcomeParamsHolder {

        private static final WelcomeParams INSTANCE = new WelcomeParams();
    }
    /**
     * Константы хранения параметров в файле.
     */
    private static final String POINT = "point";
    private static final String LEFT_MARGIN = "left_margin";
    private static final String TOP_MARGIN = "top_margin";
    private static final String LINE_HEIGTH = "line_heigth";
    private static final String LINE_LENGTH = "line_length";
    private static final String SCALE_VERTICAL = "scale_vertical";
    private static final String SCALE_HORIZONTAL = "scale_horizontal";
    private static final String LOGO = "logo";
    private static final String BARCODE = "barcode";
    private static final String LOGO_LEFT = "logo_left";
    private static final String LOGO_TOP = "logo_top";
    private static final String DELAY_PRINT = "delay_print";
    private static final String DELAY_BACK = "delay_back";
    private static final String LOGO_IMG = "logo_img";
    private static final String BACKGROUND_IMG = "background_img";
    private static final String PROMO_TXT = "promo_text";
    private static final String BOTTOM_TXT = "bottom_text";
    private static final String ASK_LIMIT = "ask_limit";
    private static final String PAGE_LINES_COUNT = "page_lines_count";
    private static final String INFO_BUTTON = "info_button";// кнопка информационной системы на пункте регистрации
    private static final String RESPONSE_BUTTON = "response_button";// - кнопка обратной связи на пункте регистрации
    private static final String ADVANCE_BUTTON = "advance_button";// - кнопка предварительной записи на пункте регистрации
    private static final String NUMERIC_KEYBOARD = "numeric_keyboard";// - цифровая клавиатура при вводе юзерской инфы
    private static final String ALPHABETIC_KEYBOARD = "alphabetic_keyboard";// - буквенная клавиатура при вводе юзерской инфы
    private static final String INPUT_FONT_SIZE = "input_font_size";// - размер шрифта вводимого текста клиентом
    private static final String LINES_BUTTON_COUNT = "lines_button_count";// - количество рядов кнопок на киоске, если будет привышение, то начнотся листание страниц
    private static final String BUTTON_TYPE = "button_type";// - это внешний вид кнопки. Если его нет или ошибочный, то стандартный вид. Иначе номер вида или картинка в png желательно

    private WelcomeParams() {
        loadSettings();
    }
    public int point; // указание для какого пункта регистрации услуга, 0-для всех, х-для киоска х.
    public int leftMargin; // отступ слева
    public int topMargin; // отступ сверху
    public int lineHeigth = 12; // Ширина строки
    public int lineLenght = 40; // Длинна стоки на квитанции
    public double scaleVertical = 0.8; // маштабирование по вертикале
    public double scaleHorizontal = 0.8; // машcтабирование по горизонтали
    public boolean logo = true; // присутствие логотипа на квитанции
    public boolean barcode = true; // присутствие штрихкода на квитанции
    public boolean info = true; // кнопка информационной системы на пункте регистрации
    public boolean response = true; // - кнопка обратной связи на пункте регистрации
    public boolean advance = true; // - кнопка предварительной записи на пункте регистрации
    public int logoLeft = 50; // Отступ печати логотипа слева
    public int logoTop = -5; // Отступ печати логотипа сверху
    public String logoImg = "/ru/apertum/qsystem/client/forms/resources/logo_ticket.png"; // логотип сверху
    public String backgroundImg = "/ru/apertum/qsystem/client/forms/resources/fon_welcome.jpg"; // фоновая картинка
    public String promoText = "Aperum projects, e-mail: info@aperum.ru"; // промотекст, печатающийся мелким шрифтом перед штрихкодом.
    public String bottomText = "\u041f\u0440\u0438\u044f\u0442\u043d\u043e\u0433\u043e \u043e\u0436\u0438\u0434\u0430\u043d\u0438\u044f. \u0421\u043f\u0430\u0441\u0438\u0431\u043e."; // произвольный текст, печатающийся в конце квитанции после штрихкода
    public int askLimit = 3; // Критический размер очереди после которого спрашивать клиентов о готовности встать в очередь
    public int pageLinesCount = 30; // Количество строк на странице.
    public int linesButtonCount = 5; // количество рядов кнопок на киоске, если будет привышение, то начнотся листание страниц
    public String buttonType = ""; // - это внешний вид кнопки. Если его нет или ошибочный, то стандартный вид. Иначе номер вида или картинка в png желательно
    /**
     * Задержка заставки при печати в мсек.
     */
    public int delayPrint = 3000;
    public int delayBack = 40000;
    //параметры СОМ-порта для кнопок кнопочного интерфейса
    public String buttons_COM = "COM1";
    public int buttons_databits = 8;
    public int buttons_speed = 9600;
    public int buttons_parity = 0;
    public int buttons_stopbits = 1;
    public boolean numeric_keyboard = true; // - цифровая клавиатура при вводе юзерской инфы
    public boolean alphabetic_keyboard = true; // - буквенная клавиатура при вводе юзерской инфы
    public int input_font_size = 64; // - размер шрифта при вводе юзерской инфы

    /**
     * Загрузим настройки.
     */
    private void loadSettings() {
        QLog.l().logger().debug("\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u043c \u043f\u0430\u0440\u0430\u043c\u0435\u0442\u0440\u044b \u0438\u0437 \u0444\u0430\u0439\u043b\u0430 \"config" + File.separator + "welcome.property\"");
        final Properties settings = new Properties();
        FileInputStream in = null;
        InputStreamReader inR = null;
        try {
            in = new FileInputStream("config" + File.separator + "welcome.properties");
            inR = new InputStreamReader(in, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new ClientException("\u041f\u0440\u043e\u0431\u043b\u0435\u043c\u044b \u0441 \u043a\u043e\u0434\u0438\u0440\u043e\u0432\u043a\u043e\u0439 \u043f\u0440\u0438 \u0447\u0442\u0435\u043d\u0438\u0438. " + ex);
        } catch (FileNotFoundException ex) {
            throw new ClientException("\u041f\u0440\u043e\u0431\u043b\u0435\u043c\u044b \u0441 \u0444\u0430\u0439\u043b\u043e\u043c \u043f\u0440\u0438 \u0447\u0442\u0435\u043d\u0438\u0438. " + ex);
        }
        try {
            settings.load(inR);
        } catch (IOException ex) {
            throw new ClientException("\u041f\u0440\u043e\u0431\u043b\u0435\u043c\u044b \u0441 \u0447\u0442\u0435\u043d\u0438\u0435\u043c \u043f\u0430\u0440\u0430\u043c\u0435\u0442\u0440\u043e\u0432. " + ex);
        }
        point = settings.containsKey(POINT) ? Integer.parseInt(settings.getProperty(POINT)) : 1; // указание для какого пункта регистрации услуга, 0-для всех, х-для киоска х.
        leftMargin = Integer.parseInt(settings.getProperty(LEFT_MARGIN)); // отступ слева
        topMargin = Integer.parseInt(settings.getProperty(TOP_MARGIN)); //  отступ сверху
        lineHeigth = Integer.parseInt(settings.getProperty(LINE_HEIGTH)); // Ширина строки
        lineLenght = Integer.parseInt(settings.getProperty(LINE_LENGTH)); // Длинна стоки на квитанции
        scaleVertical = Double.parseDouble(settings.getProperty(SCALE_VERTICAL)); // маштабирование по вертикале
        scaleHorizontal = Double.parseDouble(settings.getProperty(SCALE_HORIZONTAL)); // машcтабирование по вертикале
        logo = "1".equals(settings.getProperty(LOGO)) || "true".equals(settings.getProperty(LOGO)); // присутствие логотипа на квитанции
        barcode = "1".equals(settings.getProperty(BARCODE)) || "true".equals(settings.getProperty(BARCODE)); // присутствие штрихкода на квитанции
        logoLeft = Integer.parseInt(settings.getProperty(LOGO_LEFT)); // Отступ печати логотипа слева
        logoTop = Integer.parseInt(settings.getProperty(LOGO_TOP)); // Отступ печати логотипа сверху
        delayPrint = Integer.parseInt(settings.getProperty(DELAY_PRINT)); // Задержка заставки при печати в мсек.
        delayBack = Integer.parseInt(settings.getProperty(DELAY_BACK)); // Задержка заставки при печати в мсек.
        logoImg = settings.getProperty(LOGO_IMG);
        backgroundImg = settings.containsKey(BACKGROUND_IMG) ? settings.getProperty(BACKGROUND_IMG) : "/ru/apertum/qsystem/client/forms/resources/fon_welcome.jpg";
        if (!new File(backgroundImg).exists()) {
            backgroundImg = "/ru/apertum/qsystem/client/forms/resources/fon_welcome.jpg";
        }
        promoText = settings.getProperty(PROMO_TXT);
        bottomText = settings.getProperty(BOTTOM_TXT);
        askLimit = Integer.parseInt(settings.getProperty(ASK_LIMIT)); // Критический размер очереди после которого спрашивать клиентов о готовности встать в очередь
        pageLinesCount = settings.getProperty(PAGE_LINES_COUNT) == null ? 70 : Integer.parseInt(settings.getProperty(PAGE_LINES_COUNT)); // Количество строк на странице
        linesButtonCount = settings.getProperty(LINES_BUTTON_COUNT) == null ? 5 : Integer.parseInt(settings.getProperty(LINES_BUTTON_COUNT)); // количество рядов кнопок на киоске, если будет привышение, то начнотся листание страниц
        buttons_COM = settings.getProperty("buttons_COM");
        buttons_databits = Integer.parseInt(settings.getProperty("buttons_databits"));
        buttons_speed = Integer.parseInt(settings.getProperty("buttons_speed"));
        buttons_parity = Integer.parseInt(settings.getProperty("buttons_parity"));
        buttons_stopbits = Integer.parseInt(settings.getProperty("buttons_stopbits"));
        info = "1".equals(settings.getProperty(INFO_BUTTON)) || "true".equals(settings.getProperty(INFO_BUTTON)); // кнопка информационной системы на пункте регистрации
        response = "1".equals(settings.getProperty(RESPONSE_BUTTON)) || "true".equals(settings.getProperty(RESPONSE_BUTTON)); // - кнопка обратной связи на пункте регистрации
        advance = "1".equals(settings.getProperty(ADVANCE_BUTTON)) || "true".equals(settings.getProperty(ADVANCE_BUTTON)); // - кнопка предварительной записи на пункте регистрации

        numeric_keyboard = !settings.containsKey(NUMERIC_KEYBOARD) || "1".equals(settings.getProperty(NUMERIC_KEYBOARD)) || "true".equals(settings.getProperty(NUMERIC_KEYBOARD)); // - цифровая клавиатура при вводе юзерской инфы
        alphabetic_keyboard = !settings.containsKey(ALPHABETIC_KEYBOARD) || "1".equals(settings.getProperty(ALPHABETIC_KEYBOARD)) || "true".equals(settings.getProperty(ALPHABETIC_KEYBOARD));// - буквенная клавиатура при вводе юзерской инфы
        input_font_size = settings.containsKey(INPUT_FONT_SIZE) ? Integer.parseInt(settings.getProperty(INPUT_FONT_SIZE)) : 64; // - размер шрифта при вводе юзерской инфы
        if (settings.containsKey(BUTTON_TYPE)) {
            switch (settings.getProperty(BUTTON_TYPE)) {
                case "1":
                    buttonType = "/ru/apertum/qsystem/client/forms/resources/buttons/btn1.png";
                    break;
                case "2":
                    buttonType = "/ru/apertum/qsystem/client/forms/resources/buttons/btn2.png";
                    break;
                case "3":
                    buttonType = "/ru/apertum/qsystem/client/forms/resources/buttons/btn3.png";
                    break;
                case "4":
                    buttonType = "/ru/apertum/qsystem/client/forms/resources/buttons/btn4.png";
                    break;
                case "5":
                    buttonType = "/ru/apertum/qsystem/client/forms/resources/buttons/btn5.png";
                    break;
                case "6":
                    buttonType = "/ru/apertum/qsystem/client/forms/resources/buttons/btn6.png";
                    break;
                case "7":
                    buttonType = "/ru/apertum/qsystem/client/forms/resources/buttons/btn7.png";
                    break;
                case "8":
                    buttonType = "/ru/apertum/qsystem/client/forms/resources/buttons/btn8.png";
                    break;
                case "9":
                    buttonType = "/ru/apertum/qsystem/client/forms/resources/buttons/btn9.png";
                    break;
                case "10":
                    buttonType = "/ru/apertum/qsystem/client/forms/resources/buttons/btn10.png";
                    break;
                case "11":
                    buttonType = "/ru/apertum/qsystem/client/forms/resources/buttons/btn11.png";
                    break;
                case "12":
                    buttonType = "/ru/apertum/qsystem/client/forms/resources/buttons/btn12.png";
                    break;
                default:
                    final File f = new File(settings.getProperty(BUTTON_TYPE));
                    if (f.exists()) {
                        buttonType = settings.getProperty(BUTTON_TYPE);
                    } else {
                        buttonType = "";
                    }
            }
        } else {
            buttonType = "";
        }
    }
}
