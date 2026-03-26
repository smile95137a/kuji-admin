@echo off
REM ================================================================================
REM 刮刮樂測試數據導入腳本
REM ================================================================================

echo ========================================
echo 🎯 刮刮樂測試數據導入工具
echo ========================================
echo.

REM 檢查 MySQL 連線資訊
set MYSQL_USER=root
set MYSQL_PASSWORD=20031017
set MYSQL_DATABASE=kuji_db
set SQL_FILE=create-scratch-lottery-data.sql

echo 📋 配置資訊：
echo    資料庫：%MYSQL_DATABASE%
echo    使用者：%MYSQL_USER%
echo    SQL檔案：%SQL_FILE%
echo.

REM 檢查 SQL 檔案是否存在
if not exist "%SQL_FILE%" (
    echo ❌ 錯誤：找不到 SQL 檔案 %SQL_FILE%
    echo    請確認檔案位於當前目錄
    pause
    exit /b 1
)

echo ⚠️  警告：此操作將創建以下刮刮樂測試數據：
echo    1. 海賊王刮刮樂（30抽，20個獎品 + 10個謝謝惠顧）
echo    2. 寶可夢刮刮樂（50抽，30個獎品 + 20個謝謝惠顧）
echo    3. 鬼滅之刃刮刮樂（100抽，40個獎品 + 60個謝謝惠顧）
echo.

set /p CONFIRM="確定要繼續嗎？(Y/N): "
if /i not "%CONFIRM%"=="Y" (
    echo 操作已取消
    pause
    exit /b 0
)

echo.
echo 🚀 開始導入數據...
echo.

REM 執行 SQL 腳本
mysql -u %MYSQL_USER% -p%MYSQL_PASSWORD% %MYSQL_DATABASE% < %SQL_FILE%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✅ 刮刮樂測試數據導入成功！
    echo ========================================
    echo.
    echo 📊 數據摘要：
    echo    • 3 個刮刮樂商品已創建
    echo    • 所有商品狀態為 ON_SHELF（已上架）
    echo    • playMode 已設為 SCRATCH_MODE
    echo.
    echo 🔍 驗證方式：
    echo    SELECT * FROM lottery WHERE play_mode = 'SCRATCH_MODE';
    echo.
    echo 🎮 測試建議：
    echo    1. 先測試海賊王刮刮樂（30抽，較小規模）
    echo    2. 驗證謝謝惠顧機制
    echo    3. 測試多抽功能
    echo    4. 驗證大獎售完後自動降價
    echo.
) else (
    echo.
    echo ========================================
    echo ❌ 導入失敗！
    echo ========================================
    echo.
    echo 可能的原因：
    echo    1. 資料庫連線失敗（請檢查使用者名稱/密碼）
    echo    2. store 表中沒有數據（請先創建店家）
    echo    3. admin_user 表中找不到 admin@kuji.com 用戶
    echo.
    echo 💡 解決方法：
    echo    1. 執行：SELECT id FROM store LIMIT 1; 檢查店家
    echo    2. 執行：SELECT id FROM admin_user WHERE username = 'admin@kuji.com';
    echo    3. 手動修改 SQL 文件中的 @store_id_1 變數
    echo.
)

pause
