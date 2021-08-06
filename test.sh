#!/bin/bash
java org.antlr.v4.Tool Little.g4 -o src
cd src
javac *.java -d ../bin
cd ../
g++-10 Tiny_Simulator.cpp -o Tiny
cd bin
echo Test 1: 
java Driver < ../Samples/test1.tiny > a.out
cat a.out > ../test1.out
echo Enter 2 integers:
.././Tiny ../test1.out
echo ----------------------------------------------------------------
echo ----------------------------------------------------------------
echo Test 2: 
java Driver < ../Samples/test2.tiny > a.out
cat a.out > ../test2.out
.././Tiny ../test2.out
echo ----------------------------------------------------------------
echo ----------------------------------------------------------------
echo Test 3: 
java Driver < ../Samples/test3.tiny > a.out
cat a.out > ../test3.out
.././Tiny ../test3.out

