@echo off

jar xf QSystem.jar ru/apertum/qsystem/spring/qsContext.xml
jar xf QSystem.jar ru/apertum/qsystem/spring/spring-beans-2.0.dtd

java -cp QSystem.jar ru.apertum.qsystem.client.forms.FServerConfig ru/apertum/qsystem/spring/qsContext.xml

jar uf QSystem.jar ru/apertum/qsystem/spring/qsContext.xml

del ru\apertum\qsystem\spring\qsContext.xml
del ru\apertum\qsystem\spring\spring-beans-2.0.dtd
rd ru\apertum\qsystem\spring
rd ru\apertum\qsystem
rd ru\apertum
rd ru
 
exit