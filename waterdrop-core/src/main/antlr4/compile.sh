#!/bin/bash

##����Config.g4�ļ�����,���б���,����Config*.java�ļ�
java -jar /usr/local/lib/antlr-4.7-complete.jar Config.g4
javac Config*.java
