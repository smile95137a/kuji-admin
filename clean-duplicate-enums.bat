@echo off
chcp 65001 >nul
echo.
echo ========================================
echo 🧹 清理重複的 Enum 檔案
echo ========================================
echo.

set "ENUM_DIR=src\main\java\com\group\admin\enums"

if exist "%ENUM_DIR%\LotteryStatus.java" (
    del "%ENUM_DIR%\LotteryStatus.java"
    echo ✅ 已刪除 LotteryStatus.java
) else (
    echo ⚠️  LotteryStatus.java 不存在
)

if exist "%ENUM_DIR%\LotteryCategory.java" (
    del "%ENUM_DIR%\LotteryCategory.java"
    echo ✅ 已刪除 LotteryCategory.java
) else (
    echo ⚠️  LotteryCategory.java 不存在
)

if exist "%ENUM_DIR%\LotterySubCategory.java" (
    del "%ENUM_DIR%\LotterySubCategory.java"
    echo ✅ 已刪除 LotterySubCategory.java
) else (
    echo ⚠️  LotterySubCategory.java 不存在
)

echo.
echo ========================================
echo 保留的 Enum 檔案（正在使用中）：
echo ========================================
echo ✅ LotteryStatusEnum.java
echo ✅ LotteryCategoryEnum.java
echo ✅ LotterySubCategoryEnum.java
echo.
echo 清理完成！
echo.
pause
