<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : qsystem.xsl
    Created on : 30 Март 2009 г., 19:49
    Author     : egorov
    Description:
        Отображение файла конфигурации
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>
    <xsl:template match="QSystem">
        <html>
            <head>
                <title>Конфигурация QSystem</title>
            </head>
            <body>
                <H1>
                    <xsl:apply-templates select="./Услуги/@Наименование"/>
                    <br/>
                    <xsl:apply-templates select="./Услуги/@Описание"/>
                    <br/>
                    Версия:
                    <xsl:apply-templates select="./Сеть/@ВерсияХранилищаКонфигурации"/>
                </H1>
                <span style='color:blue'>
                    <xsl:apply-templates/>
                </span>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="Услуги">
        <Hr/>
        <H2>
        Список оказываемых услуг
        </H2>
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="Группа">
        <table border="1" width="100%">
            <tr>
                <td>
                        Группа
                    <br/>
                    <xsl:apply-templates select="@Наименование"/>
                    <br/>(
                    <xsl:apply-templates select="@Описание"/>)
                </td>
                <td>
                    <xsl:apply-templates/>
                </td>
            </tr>
        </table>
    </xsl:template>
    <xsl:template match="Услуга">
        <p>
            Услуга:
            <br/>
            Наименование:
            <xsl:apply-templates select="@Наименование"/>
            <br/>
            Описание:
            <xsl:apply-templates select="@Описание"/>
            <br/>
            Префикс:
            <xsl:apply-templates select="@Префикс"/>
            <br/>
            Статус:
            <xsl:apply-templates select="@Статус"/>
            <br/>
        </p>
    </xsl:template>
    <xsl:template match="Пользователи">
        <Hr/>
        <H2>
        Список пользователей
        </H2>
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="Пользователь">
        <table border="1" width="100%">
            <tr>
                <td>
            Пользователь:
                    <br/>
            Наименование:
                    <xsl:apply-templates select="@Наименование"/>
                    <br/>
            Пароль:
                    <xsl:apply-templates select="@Пароль"/>
                    <br/>
            Идентификатор:
                    <xsl:apply-templates select="@Идентификатор"/>
                    <br/>
            АдресRS:
                    <xsl:apply-templates select="@АдресRS"/>
                    <br/>
            Администрирование:
                    <xsl:apply-templates select="@Администрирование"/>
                    <br/>
            ПолучениеОтчетов:
                    <xsl:apply-templates select="@ПолучениеОтчетов"/>
                    <br/>
                </td>
                <td>
                    <xsl:apply-templates/>
                </td>
            </tr>
        </table>
    </xsl:template>
    <xsl:template match="ОказываемыеУслуги">
        <H4>
        Оказываемые услуги
        </H4>
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="ОказываемаяУслуга">
          Оказываемая услуга:
        <br/>
            Наименование:
        <xsl:apply-templates select="@Наименование"/>
            Приоритет для пользователя:
        <xsl:apply-templates select="@КоэффициентУчастия"/>
        <br/>
    </xsl:template>
    <xsl:template match="Сеть">
        <Hr/>
        <H2>
        Сетевые и прочие настройки
        </H2>
        <p>
        Порт сервера:
            <xsl:apply-templates select="@ПортСервера"/>
            <br/> Порт Вэбсервера:
            <xsl:apply-templates select="@ПортВебСервера"/>
            <br/>
        Порт клиентского приложения:
            <xsl:apply-templates select="@ПортКлиента"/>
            <br/>  Адрес сервера:
            <xsl:apply-templates select="@АдресСервера"/>
            <br/>
        Время начала работы:
            <xsl:apply-templates select="@ВремяНачалаРаботы"/>
            <br/>
        Время завершения работы:
            <xsl:apply-templates select="@ВремяЗавершенияРаботы"/>
            <br/>
        Версия:
            <xsl:apply-templates select="@ВерсияХранилищаКонфигурации"/>;
        </p>
    </xsl:template>
    <xsl:template match="text()">
        <!--xsl:value-of select="."/-->
    </xsl:template>
</xsl:stylesheet>
