#!/bin/bash

ant jar
ant fulljar
ant fulljargui

PROGUARD=~/proguard/bin/proguard.sh

if [ -f $PROGUARD ];
then
    ~/proguard/bin/proguard.sh @proguard.build
    ~/proguard/bin/proguard.sh @proguard.build_gui
fi


echo ''
echo 'You can now run:'
echo '  Swing GUI: ./gui.sh'
echo '  Web Server: ./web.sh'
echo '  Console: ./nars.sh'
echo '  Console input file: ./nars.sh [filename] [--silence <0..100>]'

#echo 'Set look & feel: -Dswing.defaultlaf=...'
#echo '  com.sun.java.swing.plaf.gtk.GTKLookAndFeel'
#echo '  com.sun.java.swing.plaf.windows.WindowsLookAndFeel'
#echo '  javax.swing.plaf.metal.MetalLookAndFeel'
#echo '  com.sun.java.swing.plaf.motif.MotifLookAndFeel'




