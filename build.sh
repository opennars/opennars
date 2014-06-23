# Very simple build script

rm -rf classes
mkdir classes
SRC=src/main/java
SRC=""
javac -d classes -Xlint:unchecked \
        nars_gui/$SRC/*/*/*.java \
	nars_java/$SRC/*/*/*.java \
        nars_web/src/*/*/*.java \
        -cp "lib/junit-4.11.jar:lib/java_websocket.jar:lib/gson-2.2.4.jar:lib/jython-2.7-b2.jar:lib/processing_org-core.jar"

# javac -d classes -Xlint:unchecked nars/*/*.java
echo 'Main-Class: nars.gui.NARSwing' > manifest.txt
echo 'Class-Path: lib/java_websocket.jar:lib/gson-2.2.4.jar:lib/jython-2.7-b2.jar:lib/processing_org-core.jar' >> manifest.txt

jar cvfm NARS.jar manifest.txt -C classes . 
rm manifest.txt

echo ''
echo 'You can now launch:'
echo 'java -jar NARS.jar &'
echo or
echo 'java -jar NARS.jar nars-dist/Examples/Example-NAL1-edited.txt --silence 90 &'
echo or
echo 'java -cp NARS.jar nars.main_nogui.NARSBatch nars-dist/Examples/Example-NAL1-edited.txt'
echo or
echo 'java -cp "NARS.jar:lib/*" nars.web.NARServer'

echo ''
echo 'Set look & feel: -Dswing.defaultlaf=...'
echo '  com.sun.java.swing.plaf.gtk.GTKLookAndFeel'
echo '  com.sun.java.swing.plaf.windows.WindowsLookAndFeel'
echo '  javax.swing.plaf.metal.MetalLookAndFeel'
echo '  com.sun.java.swing.plaf.motif.MotifLookAndFeel'




