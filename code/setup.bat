@echo off
setlocal EnableDelayedExpansion
title Dormatrix - Compile and Run

for /F "delims=" %%A in ('echo prompt $E^| cmd') do set "ESC=%%A"

set "ROOT=%~dp0"
set "SRC_DIR=%ROOT%src"
set "OUT_DIR=%ROOT%out"
set "LIB_DIR=%ROOT%lib"
set "MAIN_CLASS=Dormatrix"

REM ========== CHECK JDK ==========
where javac >nul 2>&1
if errorlevel 1 (
    echo [ERROR] javac not found. Please install JDK and ensure JAVA_HOME / PATH are correctly set.
    pause
    exit /b 1
)

for /f "tokens=2 delims==" %%I in ('java -version 2^>^&1 ^| findstr "version"') do set "JAVA_VERSION=%%I"

REM ========== ENSURE OUT DIR ==========
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

REM ========== COMPILE ==========
echo.
echo Compiling...
dir /s /b "%SRC_DIR%\*.java" > "%OUT_DIR%\sources.txt"

REM -- Updated javac to include the lib folder in the classpath --
javac -encoding UTF-8 -d "%OUT_DIR%" -sourcepath "%SRC_DIR%" -cp "%LIB_DIR%\*" @"%OUT_DIR%\sources.txt"

if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed! Check the source code for errors.
    del "%OUT_DIR%\sources.txt" 2>nul
    pause
    exit /b 1
)
del "%OUT_DIR%\sources.txt" 2>nul

REM ========== COPY RESOURCES ==========
copy "%SRC_DIR%\themes.json" "%OUT_DIR%\" >nul 2>&1

echo Compilation successful.

REM ========== RUN ==========
REM -- Updated java command to include both out and lib folders --
java --enable-native-access=ALL-UNNAMED -cp "%OUT_DIR%;%LIB_DIR%\*" %MAIN_CLASS%

>nul echo %ESC%[0m%ESC%[?25h
echo.
pause
endlocal
exit /b 0