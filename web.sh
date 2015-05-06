#!/bin/sh


echo
echo 'start ------------------- NARS Web (HTTP) Server'
echo

mvn -q -f nars_web/pom.xml  exec:java -Dexec.mainClass="nars.web.NARServer" -Dexec.args="$1 $2 $3 $4 $5"

echo
echo 'NARS Web (HTTP) Server --------------------- end'
echo

