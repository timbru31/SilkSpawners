#!/bin/sh -x
CLASSPATH=../craftbukkit-1.1-R4.jar javac *.java -Xlint:unchecked -Xlint:deprecation
rm -rf me 
mkdir -p me/exphc/SilkSpawners
mv *.class me/exphc/SilkSpawners
jar cf SilkSpawners.jar me/ *.yml *.java ChangeLog README.md LICENSE
#cp SilkSpawners.jar ../plugins/
