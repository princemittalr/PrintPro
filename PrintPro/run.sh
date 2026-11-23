#!/usr/bin/env bash
# PrintPro - compile and run on macOS / Linux (needs JDK 17 or later)
cd "$(dirname "$0")" || exit 1
rm -rf out && mkdir out
javac -d out $(find src -name "*.java") || { echo "Compilation failed"; exit 1; }
java -cp out com.printpro.app.PrintProApp
