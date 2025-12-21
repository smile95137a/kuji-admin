@echo off
echo ========================================
echo KUJI Admin - 清理並啟動腳本
echo ========================================
echo.

echo [步驟 1/3] 停止所有正在運行的 Java 進程...
echo 請手動確認已停止所有 AdminApplication 實例！
echo.
pause

echo [步驟 2/3] 清理並重新編譯專案...
mvn clean compile -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo ❌ 編譯失敗！請檢查錯誤訊息。
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ 編譯成功！
echo ========================================
echo.
echo [步驟 3/3] 現在請：
echo 1. 在 IDE 中開啟 AdminApplication.java
echo 2. 點擊 Run 按鈕啟動
echo 3. 觀察日誌，應該看到 "系統資料初始化完成！"
echo 4. 沒有 "Result Maps collection already contains key" 錯誤
echo.
echo 啟動後測試：
echo curl http://localhost:8080/api/test/health
echo.
pause
