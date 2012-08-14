@echo off

rem Запустим пока бесконечный цикл

echo Старт сервера очереди
rem java -cp dist/QSystem.jar;D:/Apertum/QSkySenderPlugin/dist/QSkySenderPlugin.jar ru.apertum.qsystem.server.QServer debug
java -cp dist/QSystem.jar;E:/WORK/ZoneboardPlugin/dist/ZoneboardPlugin.jar ru.apertum.qsystem.server.QServer debug
 
echo Сервер очереди прекратил работу

pause

