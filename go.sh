#!/bin/sh -x
CLASSPATH=../craftbukkit-1.1-R2.jar javac *.java -Xlint:unchecked -Xlint:deprecation
rm -rf me 
mkdir -p me/exphc/SilkSpawners
mv *.class me/exphc/SilkSpawners
jar cf SilkSpawners.jar me/ *.yml *.java ChangeLog README.md
cp SilkSpawners.jar ../plugins/
