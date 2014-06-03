# Very simple build script

rm -rf classes
mkdir classes
SRC=src/main/java
SRC=""
javac -d classes -Xlint:unchecked nars_gui/$SRC/*/*/*.java \
	nars_core_java/$SRC/*/*/*.java

# javac -d classes -Xlint:unchecked nars/*/*.java
echo 'Main-Class: nars.main.NARS' > manifest.txt
jar cvfm NARS.jar manifest.txt -C classes . 
rm manifest.txt

echo ''
echo 'You can now launch:'
echo 'java -jar NARS.jar &'
echo or
echo 'java -jar NARS.jar nars-dist/Examples/Example-NAL1-edited.txt --silence 90 &'
echo or
echo 'java -cp NARS.jar nars.main_nogui.NARSBatch nars-dist/Examples/Example-NAL1-edited.txt'
