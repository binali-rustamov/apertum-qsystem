@echo off

rem admin tools for change context

echo Admin tools for change context

pause asdasdads

jar xf QSystem.jar ru/apertum/qsystem/spring/qsContext.xml
jar xf QSystem.jar ru/apertum/qsystem/spring/spring-beans-2.0.dtd

echo Context is open. 

pause

java -cp QSystem.jar ru.apertum.qsystem.server.ChangeContext ru/apertum/qsystem/spring/qsContext.xml

echo Context is change. 

pause

jar uf QSystem.jar ru/apertum/qsystem/spring/qsContext.xml

del ru\apertum\spring\qsContext.xml
del ru\apertum\spring\spring-beans-2.0.dtd
rd ru\apertum\spring
rd ru\apertum
rd ru

echo Context is saved. Process is finished.