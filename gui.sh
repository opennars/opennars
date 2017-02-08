#!/bin/sh

# java -cp dist/OpenNARS_GUI.jar nars.Launcher $1 $2
mvn exec:java -Dexec.mainClass="nars.gui.NARSwing" -Dexec.args="$1 $2"
