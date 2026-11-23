@echo off
REM Runs test cases 1-4 plus wrong-input checks by feeding tests\test_all_cases.txt
cd /d "%~dp0"
if exist out rmdir /s /q out
javac -d out -sourcepath src src\com\printpro\app\PrintProApp.java
if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b 1
)
java -cp out com.printpro.app.PrintProApp < tests\test_all_cases.txt
echo.
echo EXPECTED: JOB1 = Rs. 30.80, JOB2 = Rs. 270.81, 201 colour prints REJECTED, id 999 NOT FOUND
pause
