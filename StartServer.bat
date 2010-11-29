@echo off

rem Запустим пока бесконечный цикл

echo Старт сервера очереди
java -cp dist/QSystem.jar ru.apertum.qsystem.server.QServer debug1
 
echo Сервер очереди прекратил работу

pause

