#!/bin/bash

rm -rf bin/*.class
javac -classpath src/main/java -Xlint:none src/main/java/Runner.java -d bin 
java -cp bin Runner 
