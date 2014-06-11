# Very simple build script

rm -rf classes
mkdir classes
SRC=src/main/java
SRC=""
javac -d classes -Xlint:unchecked \
        nars_gui/$SRC/*/*/*.java \
	nars_java/$SRC/*/*/*.java \
        nars_web/src/*/*/*.java \
        -cp "build/junit-4.11.jar:nars_web/lib/java_websocket.jar:nars_web/lib/gson-2.2.4.jar"

# javac -d classes -Xlint:unchecked nars/*/*.java
echo 'Main-Class: nars.main.NARS' > manifest.txt
echo 'Class-Path: nars_web/lib/java_websocket.jar:nars_web/lib/gson-2.2.4.jar' >> manifest.txt

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
echo 'java -cp "NARS.jar:nars_web/lib/*" nars.web.NARServer'
