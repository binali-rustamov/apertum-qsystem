#!/bin/sh



jar xf lib/qcontext.jar ru/apertum/qsystem/spring/qsContext.xml
jar xf lib/qcontext.jar ru/apertum/qsystem/spring/spring-beans-2.0.dtd

java -cp QSystem.jar ru.apertum.qsystem.client.forms.FServerConfig ru/apertum/qsystem/spring/qsContext.xml

jar uf lib/qcontext.jar ru/apertum/qsystem/spring/qsContext.xml

rm -r ru




