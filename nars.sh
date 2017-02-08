#!/bin/sh

# Usage:
#   Console I/O
#       nars.sh 
#   Input from file
#       nars.sh [filename]

#java -cp target/opennars-1.6.5.jar nars.io.NARConsole $1
mvn exec:java -Dexec.mainClass="nars.io.NARConsole"  -Dexec.args="$1"

