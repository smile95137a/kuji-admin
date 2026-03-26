@echo off
echo ========================================
echo 賞品盒 + 金流 + 訂單系統 - 初始化腳本
echo ========================================
echo.

echo [步驟 1/2] 執行 DDL 建立資料表...
echo.
echo 請手動執行以下 SQL 檔案：
echo doc\sql\prize-box-wallet-order-ddl.sql
echo.
echo 建議使用 MySQL Workbench 或其他 SQL 工具執行
echo.
pause

echo.
echo [步驟 2/2] 執行 MyBatis Generator...
echo.
call mvn mybatis-generator:generate

echo.
echo ========================================
echo 初始化完成！
echo ========================================
echo.
echo 已生成以下檔案：
echo - Entity: PrizeBox, UserWallet, WalletTransaction, RechargePlan, RechargeRecord, OrderItem, OrderStatusLog
echo - Mapper: 對應的 Mapper 介面
echo - Example: 對應的 Example 查詢類別
echo - XML: 對應的 XML 映射檔案
echo.
echo 下一步：建立 Enum 和 DTO
echo.
pause
