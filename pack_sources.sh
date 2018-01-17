#!/bin/bash


rm -rf tmp/*
cp src/main/java/strategy/* tmp/
sed  -i.old '/package strategy;/d' tmp/*.java
rm -rf tmp/*.old
cd tmp/
zip -j ../strategy.zip *.java
