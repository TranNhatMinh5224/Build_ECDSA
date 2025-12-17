@echo off
REM Script build với logging
REM Usage: build-with-log.bat [task]

setlocal enabledelayedexpansion

set TASK=%1
if "%TASK%"=="" set TASK=build

set LOG_DIR=build_logs
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
set "HH=%dt:~8,2%" & set "Min=%dt:~10,2%" & set "SS=%dt:~12,2%"
set "timestamp=%YYYY%%MM%%DD%_%HH%%Min%%SS%"

set LOG_FILE=%LOG_DIR%\build_%timestamp%.log
set ERROR_LOG=%LOG_DIR%\build_errors_%timestamp%.log

echo ==========================================
echo BUILD WITH LOGGING
echo ==========================================
echo Task: %TASK%
echo Log file: %LOG_FILE%
echo Error log: %ERROR_LOG%
echo ==========================================
echo.

REM Kiểm tra Gradle wrapper
if not exist "gradlew.bat" (
    echo [ERROR] Khong tim thay gradlew.bat!
    exit /b 1
)

REM Build và ghi log
echo [INFO] Bat dau build task: %TASK%
echo [INFO] ==========================================

gradlew.bat %TASK% --stacktrace --info --warning-mode all > "%LOG_FILE%" 2> "%ERROR_LOG%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [INFO] ==========================================
    echo [INFO] BUILD THANH CONG!
    echo [INFO] Log file: %LOG_FILE%
    echo ==========================================
) else (
    echo.
    echo [ERROR] ==========================================
    echo [ERROR] BUILD THAT BAI!
    echo [ERROR] Exit code: %ERRORLEVEL%
    echo [ERROR] Xem log file de biet chi tiet:
    echo [ERROR]   - Full log: %LOG_FILE%
    echo [ERROR]   - Error log: %ERROR_LOG%
    echo ==========================================
    type "%ERROR_LOG%"
    exit /b %ERRORLEVEL%
)

endlocal








