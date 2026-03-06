@echo off
REM Dormatrix Manual Test Runner
REM Quick automated test for core functionality

echo ========================================
echo Dormatrix Manual Test Runner
echo ========================================
echo.

REM Check if compiled
if not exist "out\Dormatrix.class" (
    echo [SETUP] Compiling project...
    javac -d out -cp src src\*.java src\**\*.java
    if errorlevel 1 (
        echo [ERROR] Compilation failed!
        pause
        exit /b 1
    )
    echo [OK] Compilation successful
    echo.
)

echo [INFO] Testing data file integrity...
echo.

REM Test 1: Check balance file
echo [TEST 1] Checking balance data format...
findstr /R "^[0-9][0-9]*,[0-9]*\.[0-9]*$" data\inventories\balances.txt > nul
if errorlevel 1 (
    echo [FAIL] Balance file has formatting issues
    echo Check: data\inventories\balances.txt
) else (
    echo [PASS] Balance file format correct
)
echo.

REM Test 2: Check student data
echo [TEST 2] Checking student data exists...
if exist "data\users\students.txt" (
    for /f %%i in ('find /c /v "" ^< data\users\students.txt') do set count=%%i
    echo [PASS] Found !count! student records
) else (
    echo [FAIL] Student data file missing
)
echo.

REM Test 3: Check token file
echo [TEST 3] Checking token storage...
if exist "data\foods\tokens.txt" (
    echo [PASS] Token file exists
) else (
    echo [INFO] Token file will be created on first purchase
)
echo.

REM Test 4: Check room data
echo [TEST 4] Checking room data...
if exist "data\rooms\rooms.txt" (
    echo [PASS] Room data exists
) else (
    echo [WARN] Room data missing
)
echo.

REM Test 5: Check complaint storage
echo [TEST 5] Checking complaint system...
if exist "data\complaints\complaints.txt" (
    echo [PASS] Complaint file exists
) else (
    echo [INFO] Complaint file will be created when first complaint filed
)
echo.

REM Test 6: Check inventory
echo [TEST 6] Checking store inventory...
if exist "data\inventories\inventory.txt" (
    for /f %%i in ('find /c /v "" ^< data\inventories\inventory.txt') do set items=%%i
    echo [PASS] Found !items! inventory items
) else (
    echo [WARN] Inventory file missing
)
echo.

echo ========================================
echo Manual Testing Checklist
echo ========================================
echo.
echo Please manually test the following:
echo.
echo [ ] 1. Login as Student (230042139 / 230042139)
echo [ ] 2. Purchase meal token
echo [ ] 3. View token list
echo [ ] 4. Check balance deduction
echo [ ] 5. Try duplicate purchase (should fail)
echo [ ] 6. Login as Cafeteria Manager
echo [ ] 7. Verify a token
echo [ ] 8. Try to verify same token again (should fail)
echo [ ] 9. Book study room seat
echo [ ] 10. Check-in within 30 seconds
echo.
echo ========================================
echo Test Data Locations
echo ========================================
echo.
echo Balances: data\inventories\balances.txt
echo Tokens:   data\foods\tokens.txt
echo Students: data\users\students.txt
echo Rooms:    data\rooms\rooms.txt
echo.
echo ========================================

pause
