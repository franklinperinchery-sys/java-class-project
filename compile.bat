@echo off
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
del sources.txt
echo Compiled.
