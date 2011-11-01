@echo off

7za x lib/qcontext.jar ru/apertum/qsystem/spring/qsContext.xml
7za x lib/qcontext.jar ru/apertum/qsystem/spring/spring-beans-3.0.xsd

java -cp QSystem.jar ru.apertum.qsystem.client.forms.FServerConfig ru/apertum/qsystem/spring/qsContext.xml

7za a lib/qcontext.jar ru/apertum/qsystem/spring/qsContext.xml

del ru\apertum\qsystem\spring\qsContext.xml
del ru\apertum\qsystem\spring\spring-beans-3.0.xsd
rd ru\apertum\qsystem\spring
rd ru\apertum\qsystem
rd ru\apertum
rd ru
 
exit