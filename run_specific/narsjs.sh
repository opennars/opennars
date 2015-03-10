#!/bin/sh

# Usage:
#   Console I/O
#       nars.sh 
#   Input from file
#       nars.sh [filename]

cd ..
java -cp ./run_specific/OpenNARS.jar nars.core.NARjs $1



