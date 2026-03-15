@echo off
REM Dormatrix JUnit Test Runner

echo ========================================
echo   Dormatrix JUnit Test Runner
echo ========================================
echo.

REM Compile tests
echo [INFO] Compiling tests...
javac -encoding UTF-8 -d out -sourcepath src -cp "lib\*" src\tests\UnitTests.java
if errorlevel 1 (
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)
echo [OK] Compilation successful
echo.

REM Run JUnit
echo [INFO] Running JUnit tests...
echo.
java -cp "out;lib\*" org.junit.runner.JUnitCore tests.UnitTests
echo.

pause
