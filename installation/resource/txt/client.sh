#!/bin/sh
java -cp dist/QSystem.jar ru.apertum.qsystem.client.forms.FClient -sport $serverPort -cport $clientPort -s $serverAdress -cfgn config/clientboard.xml

