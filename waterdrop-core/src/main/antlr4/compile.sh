#!/bin/bash

##根据Config.g4文件规则,进行编译,生成Config*.java文件
java -jar /usr/local/lib/antlr-4.7-complete.jar Config.g4
javac Config*.java
