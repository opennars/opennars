#!/bin/sh

echo
echo 'start ------------------- NARS Swing GUI'
echo

mvn -q -f nars_gui/pom.xml  exec:java -Dexec.mainClass="nars.gui.NARSwing"

echo
echo 'NARS Swing GUI --------------------- end'
echo
