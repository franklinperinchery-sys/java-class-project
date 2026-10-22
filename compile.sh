#!/bin/sh
# Compiles the project into the "out" folder (needs JDK 11 or newer).
mkdir -p out
javac -d out $(find src -name "*.java")
echo "Compiled."
