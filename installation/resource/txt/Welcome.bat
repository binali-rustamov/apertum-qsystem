@echo off

rem Запустим выбор услуги
echo Старт выбора услуги

java -cp dist/QSystem.jar ru.apertum.qsystem.client.forms.FWelcome -sport $serverPort -cport $clientPort -s $serverAdress