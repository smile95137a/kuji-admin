@echo off
echo ========================================
echo 執行 SQL 到 AWS RDS
echo ========================================
echo.
echo 請複製以下 SQL 到你的資料庫管理工具（DBeaver/MySQL Workbench/Navicat）執行：
echo.
echo 檔案位置：
echo %cd%\add-missing-referral-columns.sql
echo.
echo 或直接開啟檔案複製內容
start notepad add-missing-referral-columns.sql
echo.
pause
