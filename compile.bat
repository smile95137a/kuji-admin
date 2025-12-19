@echo off
REM 編譯專案腳本
echo ===== Starting Maven Compilation =====
cd /d "%~dp0"
call mvn clean compile -DskipTests -q
echo.
echo ===== Compilation Complete =====
