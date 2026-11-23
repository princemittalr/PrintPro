@echo off
REM PrintPro - compile and run on Windows (needs JDK 17 or later on PATH)
cd /d "%~dp0"
if exist out rmdir /s /q out
javac -d out -sourcepath src src\com\printpro\app\PrintProApp.java
if errorlevel 1 (
    echo Compilation failed. Check that JDK 17+ is installed: javac -version
    pause
    exit /b 1
)
java -cp out com.printpro.app.PrintProApp
pause
