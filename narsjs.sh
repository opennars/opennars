#!/bin/sh


echo
echo 'start ------------- NARS Javascript REPL (stdio)'
echo

mvn -q -f nars_logic/pom.xml  exec:java -Dexec.mainClass="nars.main.NARjs" -Dexec.args="$1 $2 $3 $4 $5"

echo
echo 'NARS Javascript REPL (stdio) --------------- end'
echo

