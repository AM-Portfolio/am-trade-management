@echo off
REM Master Test Runner for AM Trade SDKs (Windows)
REM This batch script runs both Java and Python SDK verifications

setlocal enabledelayedexpansion

REM Color codes (Windows 10+)
setlocal enabledelayedexpansion
for /F %%A in ('echo prompt $H ^| cmd') do set "BS=%%A"

cls
echo.
echo ================================================================================
echo  ^>^> AM TRADE SDK - UNIFIED TEST RUNNER (Windows)
echo ================================================================================
echo.

REM Check if running from correct directory
if not exist "python-verifier\verify.py" (
    echo ERROR: python-verifier\verify.py not found
    echo Please run this script from: am-trade-sdk-test directory
    echo.
    exit /b 1
)

echo [*] Starting SDK verification suite...
echo.

REM Display menu
echo Choose which tests to run:
echo.
echo 1) Run Python SDK tests only
echo 2) Run Java SDK tests only
echo 3) Run both (Recommended)
echo 4) Exit
echo.
set /p choice="Enter your choice (1-4): "

if "%choice%"=="1" goto run_python
if "%choice%"=="2" goto run_java
if "%choice%"=="3" goto run_both
if "%choice%"=="4" goto exit_script
goto invalid_choice

:invalid_choice
echo ERROR: Invalid choice. Please enter 1-4.
echo.
goto exit_script

:run_python
echo.
echo ================================================================================
echo  ^>^> PYTHON SDK VERIFICATION
echo ================================================================================
echo.
cd python-verifier
python verify.py
set python_result=%ERRORLEVEL%
cd ..
echo.
if %python_result% equ 0 (
    echo [OK] Python SDK verification PASSED
) else (
    echo [FAIL] Python SDK verification FAILED
)
goto print_summary

:run_java
echo.
echo ================================================================================
echo  ^>^> JAVA SDK VERIFICATION
echo ================================================================================
echo.
cd ..\am-trade-sdk-core
echo Running Maven tests...
mvn clean test -q
set java_result=%ERRORLEVEL%
cd ..\am-trade-sdk-test
echo.
if %java_result% equ 0 (
    echo [OK] Java SDK verification PASSED
) else (
    echo [FAIL] Java SDK verification FAILED
)
goto print_summary

:run_both
echo.
echo ================================================================================
echo  STEP 1/2: PYTHON SDK VERIFICATION
echo ================================================================================
echo.
cd python-verifier
python verify.py
set python_result=%ERRORLEVEL%
cd ..
echo.
if %python_result% equ 0 (
    echo [OK] Python SDK verification PASSED
) else (
    echo [FAIL] Python SDK verification FAILED
)

echo.
echo ================================================================================
echo  STEP 2/2: JAVA SDK VERIFICATION
echo ================================================================================
echo.
cd ..\am-trade-sdk-core
echo Running Maven tests...
mvn clean test -q
set java_result=%ERRORLEVEL%
cd ..\am-trade-sdk-test
echo.
if %java_result% equ 0 (
    echo [OK] Java SDK verification PASSED
) else (
    echo [FAIL] Java SDK verification FAILED
)

:print_summary
echo.
echo ================================================================================
echo  VERIFICATION SUMMARY
echo ================================================================================
echo.

if %python_result% equ 0 (
    echo  [PASS] Python SDK
) else (
    if defined python_result (
        echo  [FAIL] Python SDK
    )
)

if %java_result% equ 0 (
    echo  [PASS] Java SDK
) else (
    if defined java_result (
        echo  [FAIL] Java SDK
    )
)

echo.
if %python_result% equ 0 if %java_result% equ 0 (
    echo ================================================================================
    echo  SUCCESS: All SDK verifications PASSED!
    echo ================================================================================
    set final_result=0
) else (
    echo ================================================================================
    echo  FAILURE: Some SDK verifications FAILED!
    echo ================================================================================
    set final_result=1
)

echo.

:exit_script
pause
endlocal
exit /b %final_result%
