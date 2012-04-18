@echo off

rem Запустим выбор услуги
echo Старт выбора услуги

java -cp dist/QSystem.jar ru.apertum.qsystem.client.forms.FWelcome -sport 3128 -cport 3129 -s localhost  debug2 tach med1

pause