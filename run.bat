@echo off
setlocal EnableDelayedExpansion
title Dormatrix Launcher

REM ==========================================
REM ============= CONFIG =====================
REM ==========================================
set "SRC_DIR=src"
set "OUT_DIR=out"
set "MAIN_CLASS=Dormatrix"

REM ==========================================
REM ============ CHECK JDK ===================
REM ==========================================
:check_jdk
where javac >nul 2>&1
if errorlevel 1 (
    echo [ERROR] javac not found. Please install JDK and make sure JAVA_HOME / PATH are set.
    pause
    exit /b 1
)

REM Go to menu after JDK check
goto menu

REM ==========================================
REM ================ MENU ====================
REM ==========================================
:menu
cls
echo ==================================================
echo               DORMATRIX LAUNCHER
echo ==================================================
echo.
echo  [1] Build + Run
echo  [2] Run only
echo  [3] Clean
echo  [4] Exit
echo.
set /p choice="Select option: "

if "%choice%"=="1" goto build_and_run
if "%choice%"=="2" goto run_only
if "%choice%"=="3" goto clean
if "%choice%"=="4" goto end

echo.
echo Invalid choice. Try again.
pause
goto menu

REM ==========================================
REM ============ ENSURE OUT DIR =============
REM ==========================================
:ensure_out
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"
exit /b 0

REM ==========================================
REM ================ BUILD ===================
REM ==========================================
:build
call :ensure_out
echo Compiling...

REM Create a list of all .java sources
dir /s /b "%SRC_DIR%\*.java" > "%OUT_DIR%\sources.txt"

javac -d "%OUT_DIR%" -sourcepath "%SRC_DIR%" @"%OUT_DIR%\sources.txt"
if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed!
    del "%OUT_DIR%\sources.txt" 2>nul
    pause
    exit /b 1
)

del "%OUT_DIR%\sources.txt" 2>nul
echo Compilation successful.
exit /b 0

REM ==========================================
REM ========= BUILD + RUN OPTION =============
REM ==========================================
:build_and_run
call :build
if errorlevel 1 (
    REM build already printed the error
    goto menu
)
echo.
echo Launching Dormatrix...
echo.
java -cp "%OUT_DIR%" %MAIN_CLASS%
echo.
pause
goto menu

REM ==========================================
REM =============== RUN ONLY =================
REM ==========================================
:run_only
if not exist "%OUT_DIR%" (
    echo [ERROR] Output directory "%OUT_DIR%" does not exist.
    echo You probably need to build first.
    pause
    goto menu
)

echo Launching Dormatrix...
echo.
java -cp "%OUT_DIR%" %MAIN_CLASS%
echo.
pause
goto menu

REM ==========================================
REM ================= CLEAN ==================
REM ==========================================
:clean
if exist "%OUT_DIR%" (
    rmdir /s /q "%OUT_DIR%"
    echo Cleaned build output.
) else (
    echo Nothing to clean. "%OUT_DIR%" does not exist.
)
echo.
pause
goto menu

REM ==========================================
REM ================== END ===================
REM ==========================================
:end
endlocal
exit /b 0
