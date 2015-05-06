#!/bin/sh

echo
echo 'start NARS Console (stdio) -------------'
echo

mvn -q -f nars_logic/pom.xml  exec:java -Dexec.mainClass="nars.main.NARRun" -Dexec.args="$1 $2 $3 $4 $5"

echo
echo '------------------------------------ end'
echo

