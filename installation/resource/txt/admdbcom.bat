@echo off

jar xf QSystem.jar ru/apertum/qsystem/spring/qsContext.xml
jar xf QSystem.jar ru/apertum/qsystem/spring/spring-beans-2.0.dtd

javaw -cp QSystem.jar ru.apertum.qsystem.client.forms.FServerConfig ru/apertum/qsystem/spring/qsContext.xml

jar uf QSystem.jar ru/apertum/qsystem/spring/qsContext.xml

del ru\apertum\spring\qsContext.xml
del ru\apertum\spring\spring-beans-2.0.dtd
rd ru\apertum\spring
rd ru\apertum
rd ru
 
exit