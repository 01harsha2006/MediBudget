@echo off
echo ========================================================
echo         Compiling MediBudget Project...
echo ========================================================

if not exist lib (
    mkdir lib
)

javac -cp ".;lib\mysql-connector-j.jar" -d . *.java

if %ERRORLEVEL% equ 0 (
    echo.
    echo [SUCCESS] MediBudget compiled successfully!
) else (
    echo.
    echo [ERROR] Compilation failed. Please check the output above.
)

pause
