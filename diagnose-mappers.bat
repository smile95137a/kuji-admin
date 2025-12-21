@echo off
echo ========================================
echo MyBatis Mapper XML 重複載入診斷工具
echo ========================================
echo.

echo [檢查 1] 檢查 src/main/resources/mapper 中的 XML 文件...
dir /b src\main\resources\mapper\*.xml
echo.

echo [檢查 2] 檢查 target/classes/mapper 中的 XML 文件...
if exist target\classes\mapper (
    dir /b target\classes\mapper\*.xml
) else (
    echo target\classes\mapper 目錄不存在（正常，編譯後會自動生成）
)
echo.

echo [檢查 3] 搜尋是否有重複的 XML 文件...
echo 正在搜尋...
dir /s /b AdminOperationLogMapper.xml 2>nul
echo.

echo [檢查 4] 驗證 application.yml 配置...
findstr /C:"mapper-locations" src\main\resources\application*.yml
echo.

echo [檢查 5] 驗證 @MapperScan 配置...
findstr /C:"@MapperScan" src\main\java\com\group\admin\AdminApplication.java
echo.

echo ========================================
echo 診斷完成
echo ========================================
echo.
echo 正常情況應該是：
echo - src/main/resources/mapper 有 17 個 XML
echo - target/classes/mapper 有 17 個 XML（編譯後）
echo - mapper-locations: classpath:/mapper/*.xml
echo - @MapperScan("com.group.admin.mapper")
echo.
pause
