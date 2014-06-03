#!/bin/zsh
./build.sh
cp NARS.jar nars-dist
rm -r nars-dist/javadoc/*
javadoc -d nars-dist/javadoc nars_core_java/**/*.java nars_gui/**/*.java
# if you don't have zsh installed ( try it ! ) uncomment this :
# javadoc -d nars-dist/javadoc nars_gui/src/main/java/*/*/*.java nars_core_java/src/main/java/*/*/*.java nars_core_java/src/main/java/*/*/*/*.java
zip -r NARS.zip nars-dist
