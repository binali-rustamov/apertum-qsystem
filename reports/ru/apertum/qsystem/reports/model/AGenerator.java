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
package ru.apertum.qsystem.reports.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import org.apache.commons.lang.StringUtils;
import ru.apertum.qsystem.common.Uses;

/**
 * Базовый класс генераторов отчетов.
 * сам себя складывает в HashMap<String, IGenerator> generators.
 * Для получения отчета генератор использует методы интерфейса IFormirovator.
 * метод process генерирует отчет.
 * @author Evgeniy Egorov
 */
@MappedSuperclass
public abstract class AGenerator implements IGenerator {

    /**
     * только для hibernate.
     */
    public AGenerator() {
    }
    private String template;

    @Column(name = "template")
    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
    private String href;

    @Column(name = "href")
    @Override
    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public AGenerator(String href, String resourceNameTemplate) {
        this.href = href;
        this.template = resourceNameTemplate;
        ReportGenerator.addGenerator(this);
    }

    /**
     * Абстрактный метод формирования данных отчета.
     * @return
     */
    abstract protected JRDataSource getDataSource(String inputData);

    /**
     * Абстрактный метод формирования параметров для отчета.
     * @return
     */
    abstract protected Map getParameters(String inputData);

    /**
     * Метод получения коннекта к базе если отчет строится через коннект.
     * @return коннект соединения к базе или null.
     */
    abstract protected Connection getConnection(String inputData);

    /**
     * Абстрактный метод выполнения неких действия для подготовки данных отчета.
     * Если он возвращает заполненный массив байт, то его нужно отдать клиенту,
     * иначе если null то продолжаем генерировать отчет.
     * @return массив байт для выдачи на клиента.
     */
    abstract protected byte[] preparation(String inputData);

    /**
     * Метод получения документа-отчета или другого какого документа в виде массива байт.
     * Сдесь испольщуем методы интерфейса IFormirovator для получения отчета.
     * @param format какого формата отчет хотим получить(html, pdf, rtf)
     * @return массив байт документа.
     */
    @Override
    public byte[] process(String inputData) {
        Uses.logRep.logger.debug("Генерируем : \"" + href + "\"");

        /*
         * Перед формированием отчета возможно необходимо получить некие параметры.
         * Для этого надо выдать клиенту форму для заполнения и принять от него введенные данные.
         * А по этим данным уже формировать отчетные данные. 
         */
        final byte[] before = preparation(inputData);
        if (before != null) {
            return before;
        }

        // Компиляция отчета, попробуем без компиляции, есть же уже откампиленные
        //InputStream is = getClass().getResourceAsStream(template);
        //JasperReport jasperReport = JasperCompileManager.compileReport(is);

        // Получение готового к экспорту отчета
        //JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, hm, xmlDataSource); - это вариант с предкампиляцией
        try {
            // Шаблон может быть в виде файла, иначе в виде ресурса
            final File f_temp = new File(template);
            final InputStream inStr;
            if (f_temp.exists()) {
                inStr = new FileInputStream(f_temp);
            } else {
                inStr = getClass().getResourceAsStream(template);
            }
            if (inStr == null) {
                throw new Uses.ReportException("Шаблон не найден. \"" + template + "\" Либо отсутствует требуемый файл, либо некорректная запись в базе данных.");
            }
            // теперь посмотрим, не сформировали ли коннект 
            //если есть коннект, то строим отчет по коннекту, иначе формируем данные формироватором.
            final Connection conn = getConnection(inputData);
            final JasperPrint jasperPrint;
            if (conn == null) {
                jasperPrint = JasperFillManager.fillReport(inStr, getParameters(inputData), getDataSource(inputData));//это используя уже откампиленный
            } else {
                jasperPrint = JasperFillManager.fillReport(inStr, getParameters(inputData), conn);//это используя уже откампиленный
            }
            byte[] result = null;

            String format;
            String GETString = Uses.getRequestTarget(inputData);
            int pos = GETString.indexOf(".");
            if (pos == -1) {
                format = "";
            } else {
                format = GETString.substring(pos + 1);
            }
            pos = format.indexOf("?");
            if (pos != -1) {
                format = StringUtils.left(format, pos);
            }

            if (Uses.REPORT_FORMAT_HTML.equalsIgnoreCase(format)) {
                final JRHtmlExporter exporter = new JRHtmlExporter();
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);

                // сгенерим отчет во временный файл
                // это нужно для того, что если совместно с html генеряться картинки, например графики,
                // то их сбросить на диск и потом с диска выдать по запросу броузера.
                // для этого нужно чтоб вебсервер умел выдовать и файла с диска и файлы из временных папок.
                final JRHtmlExporter exporterToTempFile = new JRHtmlExporter();
                exporterToTempFile.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                exporterToTempFile.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, Uses.TEMP_FOLDER + File.separator + "temphtml.html");
                exporterToTempFile.exportReport();

                final StringBuffer buf = new StringBuffer();
                exporter.setParameter(JRExporterParameter.OUTPUT_STRING_BUFFER, buf);
                exporter.exportReport();
                result = new String(buf.toString().getBytes()).replaceAll("nullpx", "resources/px").replaceFirst("<body text=\"#000000\"", "<body text=\"#000000\"  background=\"resources/setka.gif\" bgproperties=\"fixed\"").replaceAll("bgcolor=\"white\"", "bgcolor=\"CCDDEE\"").replaceAll("nullimg_", "img_").getBytes("UTF-8");
            } else if (Uses.REPORT_FORMAT_RTF.equalsIgnoreCase(format)) {
                final JRRtfExporter exporter = new JRRtfExporter();
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, baos);
                exporter.exportReport();
                result = baos.toByteArray();
            } else if (Uses.REPORT_FORMAT_PDF.equalsIgnoreCase(format)) {
                // создадим файл со шрифтами если его нет
                final File f = new File("tahoma.ttf");
                if (!f.exists()) {
                    FileOutputStream fo = new FileOutputStream(f);
                    final InputStream inStream = getClass().getResourceAsStream("/ru/apertum/qsystem/reports/fonts/tahoma.ttf");
                    final byte[] b = Uses.readInputStream(inStream);
                    fo.write(b);
                    fo.flush();
                    fo.close();
                }
                result = genPDF(jasperPrint);
            }
            return result;
        } catch (FileNotFoundException ex) {
            throw new Uses.ReportException("Не найден файл шрифтов для генерации PDF. " + ex);
        } catch (IOException ex) {
            throw new Uses.ReportException("Ошибка декодирования при вводе/выводе. " + ex);
        } catch (JRException ex) {
            throw new Uses.ReportException("Ошибка генерации. " + ex);
        }

    }

    /**
     * Метод генерации PDF-отчетов через файл.
     * Вынесен в отдельный метод для синхронизации.
     * @param jasperPrint этот готовый отчет и экспортим в PDF
     * @return возвращает готовый отчет в виде массива байт
     * @throws net.sf.jasperreports.engine.JRException
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    synchronized private static byte[] genPDF(JasperPrint jasperPrint) throws JRException, FileNotFoundException, IOException {
        // сгенерим отчет во временный файл
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, Uses.TEMP_FOLDER + File.separator + "temppdf.pdf");
        exporter.exportReport();
        // отправим данные из файла и удалим его
        final File pdf = new File(Uses.TEMP_FOLDER + File.separator + "temppdf.pdf");
        final FileInputStream inStream = new FileInputStream(pdf);
        pdf.delete();
        return Uses.readInputStream(inStream);
    }
}    
