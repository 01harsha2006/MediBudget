@echo off
echo ========================================================
echo         Launching MediBudget Desktop Application
echo ========================================================

java -cp ".;lib\mysql-connector-j.jar" MediBudget.Main %*

if %ERRORLEVEL% neq 0 (
    echo.
    echo Application exited with code %ERRORLEVEL%.
    pause
)
