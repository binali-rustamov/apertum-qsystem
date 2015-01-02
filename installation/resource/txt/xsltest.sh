#!/bin/sh

java -cp dist/QSystem.jar ru.apertum.qsystem.utils.XsltTester "http://www.bnm.org/md/official_exchange_rates?get_xml=1&date=##dd.MM.YYYY##" "file:///home/temp/Xslt/data/official_exchange_rates.xsl" "/home/temp/xslt.txt"