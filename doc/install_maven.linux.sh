#!/bin/sh

#installation script for Apache Maven on Unix/Linux:

wget http://apache.cs.utah.edu/maven/maven-3/3.3.3/binaries/apache-maven-3.3.3-bin.tar.gz
tar xvzf apache-maven-3.3.3-bin.tar.gz
sudo mv apache-maven-3.3.3 /opt/mvn
sudo ln -s /opt/mvn/bin/mvn /usr/bin/mvn
rm -f apache-maven-*.tar.gz

