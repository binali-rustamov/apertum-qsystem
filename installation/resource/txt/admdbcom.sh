#!/bin/sh



jar xf QSystem.jar ru/apertum/qsystem/spring/qsContext.xml
jar xf QSystem.jar ru/apertum/qsystem/spring/spring-beans-2.0.dtd

java -cp QSystem.jar ru.apertum.qsystem.client.forms.FServerConfig ru/apertum/qsystem/spring/qsContext.xml

jar uf QSystem.jar ru/apertum/qsystem/spring/qsContext.xml

rm -r ru




