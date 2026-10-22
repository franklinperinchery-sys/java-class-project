#!/bin/sh
# Runs the application. Extra arguments are passed on:  ./run.sh --reset   ./run.sh --debug
java -cp "out:lib/*" movieticket.Main "$@"
