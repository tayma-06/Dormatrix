@echo off
setlocal EnableDelayedExpansion
title Dormatrix Launcher

REM === CONFIG ===
set "SRC_DIR=src"
set "OUT_DIR=out"
set "MAIN_CLASS=Dormatrix"

:check_jdk
REM Check javac is available
where javac >nul 2>&1
if errorlevel 1 (
    echo [ERROR] javac not found in PATH.
    echo Make sure Java JDK is installed and %%JAVA_HOME%%/bin is in PATH.
    pause
    exit /b 1
)

REM Check java is available
where java >nul 2>&1
if errorlevel 1 (
    echo [ERROR] java runtime not found in PATH.
    pause
    exit /b 1
)

:menu
cls
echo ==================================================
echo                DORMATRIX LAUNCHER
echo ==================================================
echo.
echo  [1] Build + Run (full compile)
echo  [2] Run only   (no compile, faster)
echo  [3] Clean build (delete compiled classes)
echo  [4] Exit
echo.
set /p choice="Select option [1-4]: "

if "%choice%"=="1" goto build_and_run
if "%choice%"=="2" goto run_only
if "%choice%"=="3" goto clean
if "%choice%"=="4" goto end

echo.
echo Invalid choice. Try again.
pause
goto menu

:ensure_out
if not exist "%OUT_DIR%" (
    mkdir "%OUT_DIR%"
)
exit /b 0

:build
call :ensure_out

echo.
echo ==================================================
echo            Compiling Java source code...
echo ==================================================
echo.

REM Collect all .java files
dir /s /b "%SRC_DIR%\*.java" > "%OUT_DIR%\sources.txt"

REM Compile all sources
javac -d "%OUT_DIR%" -sourcepath "%SRC_DIR%" @"%OUT_DIR%\sources.txt"

if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed!
    echo Check the messages above.
    del "%OUT_DIR%\sources.txt" 2>nul
    pause
    exit /b 1
)

REM Clean up
del "%OUT_DIR%\sources.txt" 2>nul
exit /b 0

:build_and_run
call :build
if errorlevel 1 (
    REM Build already printed error and paused
    goto menu
)

echo.
echo ==================================================
echo       Compilation successful. Launching app...
echo ==================================================
echo.

java -cp "%OUT_DIR%" %MAIN_CLASS%
goto menu

:run_only
if not exist "%OUT_DIR%" (
    echo.
    echo [WARN] No compiled output found in "%OUT_DIR%".
    echo You need to build at least once first.
    pause
    goto menu
)

echo.
echo ==================================================
echo         Running existing compiled classes...
echo ==================================================
echo.

java -cp "%OUT_DIR%" %MAIN_CLASS%
goto menu

:clean
echo.
echo ==================================================
echo                  Cleaning build...
echo ==================================================
echo.

if exist "%OUT_DIR%" (
    rmdir /s /q "%OUT_DIR%"
    echo Build folder "%OUT_DIR%" deleted.
) else (
    echo Nothing to clean. "%OUT_DIR%" does not exist.
)
pause
goto menu

:end
endlocal
exit /b 0
