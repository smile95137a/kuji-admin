@echo off
echo ============================================
echo KUJI Admin - 快速啟動測試
echo ============================================
echo.

echo [1/3] 檢查編譯狀態...
if not exist "target\admin-1.0.0.jar" (
    echo ⚠️  找不到編譯檔案，開始編譯...
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo ❌ 編譯失敗！
        pause
        exit /b 1
    )
) else (
    echo ✅ 編譯檔案已存在
)

echo.
echo [2/3] 檢查圖片目錄...
if not exist "src\main\resources\static\img" (
    echo 📁 建立圖片目錄...
    mkdir "src\main\resources\static\img\news"
    mkdir "src\main\resources\static\img\banner"
    mkdir "src\main\resources\static\img\lottery"
    mkdir "src\main\resources\static\img\prize"
)
echo ✅ 圖片目錄已就緒

echo.
echo [3/3] 啟動應用程式...
echo.
echo 🚀 應用程式啟動中...
echo 📝 存取位址: http://localhost:8080/api
echo 📚 Swagger UI: http://localhost:8080/swagger-ui.html
echo.
echo ⏸️  按 Ctrl+C 停止應用程式
echo.

call mvn spring-boot:run

pause
