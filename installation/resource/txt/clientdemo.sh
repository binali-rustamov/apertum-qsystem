#!/bin/sh
java -cp dist/QSystem.jar ru.apertum.qsystem.client.forms.FClient debug -sport 3128 -cport 3129 -s 127.0.0.1 -cfgn config/clientboard.xml

