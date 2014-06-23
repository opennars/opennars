ant fulljar

echo ''
echo 'You can now launch:'
echo 'java -jar dist/OpenNARS.jar &'
echo or
echo 'java -jar dist/OpenNARS.jar nars-dist/Examples/Example-NAL1-edited.txt --silence 90 &'
echo or
echo 'java -cp dist/OpenNARS.jar nars.main_nogui.NARSBatch nars-dist/Examples/Example-NAL1-edited.txt'
echo or
echo 'java -cp "dist/OpenNARS.jar:lib/*" nars.web.NARServer'

echo ''
echo 'Set look & feel: -Dswing.defaultlaf=...'
echo '  com.sun.java.swing.plaf.gtk.GTKLookAndFeel'
echo '  com.sun.java.swing.plaf.windows.WindowsLookAndFeel'
echo '  javax.swing.plaf.metal.MetalLookAndFeel'
echo '  com.sun.java.swing.plaf.motif.MotifLookAndFeel'




