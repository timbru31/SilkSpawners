#!/bin/sh -x
CLASSPATH=../craftbukkit-1.2.3-R0.1.jar:../SERVER/plugins-disabled/WorldGuard.jar javac *.java -Xlint:unchecked -Xlint:deprecation
rm -rf me 
mkdir -p me/exphc/SilkSpawners
mv *.class me/exphc/SilkSpawners
jar cf SilkSpawners.jar me/ *.yml *.java ChangeLog README.md LICENSE
