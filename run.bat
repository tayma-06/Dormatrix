@echo off
setlocal EnableDelayedExpansion
title Dormatrix - Run

for /F "delims=" %%A in ('echo prompt $E^| cmd') do set "ESC=%%A"

set "ROOT=%~dp0"
set "OUT_DIR=%ROOT%out"
set "MAIN_CLASS=Dormatrix"

REM ========== CHECK OUT DIR ==========
if not exist "%OUT_DIR%" (
    echo [ERROR] Output directory "%OUT_DIR%" does not exist.
    echo You need to compile first using compile_and_run.bat
    echo.
    pause
    exit /b 1
)

REM ========== RUN ==========
java -cp "%OUT_DIR%" %MAIN_CLASS%

>nul echo %ESC%[0m%ESC%[?25h
echo.
pause
endlocal
exit /b 0