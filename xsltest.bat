rem # for testing xslt. parameters: sources xsl-script result
java -cp dist/QSystem.jar ru.apertum.qsystem.utils.XsltTester "http://www.bnm.org/md/official_exchange_rates?get_xml=1&date=##dd.MM.YYYY##" "file:///E:/temp/Xslt/data/official_exchange_rates.xsl" "temp/xslt.txt"

pause