#!/bin/bash

mkdir -p build

ant jar
ant fulljar
ant fulljargui

PROGUARD=~/proguard/bin/proguard.sh

if [ -f $PROGUARD ];
then
    ~/proguard/bin/proguard.sh @proguard.build
    ~/proguard/bin/proguard.sh @proguard.build_gui
fi






# Build Website / Documentation -------------------


rm -Rf build/html

mkdir -p build/html
mkdir -p build/html/javadoc


#generate documentation
javadoc -sourcepath nars_java/ -d build/html/javadoc -subpackages nars


# install (node.js-based) "marked" markdown -> HTML converter with:    sudo npm i -g marked

if [ -f `which marked` ];
then
    #process markdown files

    for i in NAL Overview NarseseIO FAQ
    do
       cat doc/css/top.html     doc/"$i".md     | marked > build/html/"$i".html   ; cat doc/css/bottom.html >> build/html/"$i".html
    done



    cp doc/css/style.css    build/html    
fi

cp -R doc/site/* build/html



echo 'Documentation generated in: build/html'
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



