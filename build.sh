#!/bin/bash

mkdir -p build
mkdir -p dist

ant fulljars -Djavac.debug=false

ls -l dist/Open*.jar

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



