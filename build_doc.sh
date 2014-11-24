#!/bin/bash

mkdir -p build


# Build Website / Documentation -------------------


rm -Rf build/html

mkdir -p build/html
mkdir -p build/html/javadoc


#generate documentation
#javadoc -sourcepath nars_java/ -d build/html/javadoc -subpackages nars


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




