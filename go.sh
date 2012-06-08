#!/bin/sh -x
CLASSPATH=../craftbukkit-1.2.5-R2.0.jar:../SpoutPlugin-1110-1.2.5.jar:../worldguard-5.5.2.jar javac *.java -Xlint:unchecked -Xlint:deprecation
rm -rf me 
mkdir -p me/exphc/SilkSpawners
mv *.class me/exphc/SilkSpawners
jar cf SilkSpawners.jar me/ *.yml *.java ChangeLog README.md LICENSE
