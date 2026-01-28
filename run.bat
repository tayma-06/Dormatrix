@echo off
echo ==========================================
echo      DORMATRIX AUTOMATED BUILDER
echo ==========================================

:: 1. Create a hidden list of all .java files in your project
echo Scanning for Java files in src...
dir /s /B src\*.java > sources.txt

:: 2. Create the output folder for compiled class files
if not exist out mkdir out

:: 3. Compile ALL files found in sources.txt
echo Compiling project...
javac -d out -sourcepath src @sources.txt

:: 4. Check for errors
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation Failed! See errors above.
    del sources.txt
    pause
    exit /b
)

:: 5. Cleanup the temporary list
del sources.txt

:: 6. Run the application (Changed 'Main' to 'Dormatrix')
echo Starting Application...
echo ------------------------------------------
java -cp out Dormatrix

:: 7. Keep the window open so you can see the output
pause