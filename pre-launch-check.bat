@echo off
chcp 65001 >nul
echo ========================================
echo 🚀 KUJI Admin 啟動前最終檢查
echo ========================================
echo.

echo [✓] 檢查 1: 驗證編譯狀態...
if exist target\classes\com\group\admin\AdminApplication.class (
    echo     ✓ 專案已編譯
) else (
    echo     ✗ 專案未編譯，正在執行 mvn clean compile...
    mvn clean compile -DskipTests
    if errorlevel 1 (
        echo     ✗ 編譯失敗！請檢查錯誤訊息。
        pause
        exit /b 1
    )
)
echo.

echo [✓] 檢查 2: 驗證 Mapper XML 文件...
if exist target\classes\mapper\AdminOperationLogMapper.xml (
    echo     ✓ Mapper XML 已複製到 target/classes/mapper
    dir /b target\classes\mapper\*.xml | find /c ".xml" > temp_count.txt
    set /p xml_count=<temp_count.txt
    del temp_count.txt
    echo     ✓ 共 %xml_count% 個 Mapper XML 文件
) else (
    echo     ✗ Mapper XML 未找到！
    pause
    exit /b 1
)
echo.

echo [✓] 檢查 3: 驗證 MyBatisConfig.java...
if exist target\classes\com\group\admin\config\MyBatisConfig.class (
    echo     ✓ MyBatisConfig 已編譯
) else (
    echo     ✗ MyBatisConfig 未找到！
    pause
    exit /b 1
)
echo.

echo [✓] 檢查 4: 驗證 application.yml...
findstr /C:"mapper-locations" src\main\resources\application.yml >nul
if errorlevel 1 (
    echo     ✗ application.yml 中未找到 mapper-locations 配置
    pause
    exit /b 1
) else (
    echo     ✓ MyBatis 配置存在
)
echo.

echo [✓] 檢查 5: 驗證 DevTools 狀態...
findstr /C:"spring-boot-devtools" pom.xml | findstr /C:"<!--" >nul
if errorlevel 1 (
    echo     ⚠️ 警告：DevTools 可能未被註解！
    echo     這可能導致 Mapper XML 重複載入問題
    pause
) else (
    echo     ✓ DevTools 已被註解
)
echo.

echo ========================================
echo ✅ 所有檢查通過！
echo ========================================
echo.
echo 🎯 下一步：
echo 1. 確保 MySQL 服務已啟動
echo 2. 確保資料庫 'kuji' 已建立
echo 3. 在 IDE 中開啟 AdminApplication.java
echo 4. 點擊 Run 按鈕啟動
echo.
echo 📊 預期啟動日誌：
echo    ✓ 開始執行系統資料初始化...
echo    ✓ 角色資料初始化完成（3 筆）
echo    ✓ 選單資料初始化完成（19 筆）
echo    ✓ 系統資料初始化完成！
echo    ✓ Tomcat started on port 8080
echo    ✓ Started AdminApplication in X seconds
echo.
echo ⚠️ 如果看到 "Result Maps collection already contains key"，
echo    請執行 diagnose-mappers.bat 進行診斷。
echo.
pause
