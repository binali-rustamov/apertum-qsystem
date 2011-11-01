@echo off

rem admin tools for change context

echo Admin tools for change context

pause asdasdads

7za x lib/qcontext.jar ru/apertum/qsystem/spring/qsContext.xml
7za x lib/qcontext.jar ru/apertum/qsystem/spring/spring-beans-3.0.xsd

echo Context is open. 

pause

java -cp QSystem.jar ru.apertum.qsystem.server.ChangeContext ru/apertum/qsystem/spring/qsContext.xml

echo Context is change. 

pause

7za a lib/qcontext.jar ru/apertum/qsystem/spring/qsContext.xml

del ru\apertum\qsystem\spring\qsContext.xml
del ru\apertum\qsystem\spring\spring-beans-3.0.xsd
rd ru\apertum\qsystem\spring
rd ru\apertum\qsystem
rd ru\apertum
rd ru

echo Context is saved. Process is finished.