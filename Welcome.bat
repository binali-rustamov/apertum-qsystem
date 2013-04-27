@echo off

rem Запустим выбор услуги
echo Старт выбора услуги

java -cp dist/QSystem.jar ru.apertum.qsystem.client.forms.FWelcome -sport 3128 -cport 3129 -s localhost  debug tach med1 info1

pause