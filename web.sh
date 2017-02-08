#!/bin/sh

#java -cp dist/OpenNARS.jar nars.web.NARServer $1 $2 $3 $4 $5
mvn exec:java -Dexec.mainClass="nars.web.NARServer" -Dexec.args="$1 $2 $3 $4 $5"
