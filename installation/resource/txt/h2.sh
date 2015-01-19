#!/bin/sh
dir=$(dirname "$0")
java -cp "dist/QSystem.jar;dist/lib/h2-1.4.182.jar;$CLASSPATH" org.h2.tools.Console "$@"
