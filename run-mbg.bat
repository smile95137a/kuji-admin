@echo off
echo ========================================
echo 執行 MyBatis Generator
echo ========================================
echo.
echo 這會重新生成 Entity/Mapper/Example/XML
echo Mapper 的自定義方法已經移到 repository/ 不會被覆蓋
echo.
cd /d %~dp0
mvn mybatis-generator:generate
echo.
echo ========================================
echo 生成完成！
echo ========================================
pause
