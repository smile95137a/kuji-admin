# Order Mapper 手動修正指南

## 問題說明
`order` 是 MySQL 保留字，需要加上反引號 `` `order` ``，否則會出現 SQL 語法錯誤：
```
You have an error in your SQL syntax near 'order'
```

## ✅ 最佳解決方案：使用自動化腳本

### 執行方式
```bash
# Windows
.\regenerate-mappers.bat

# 或手動執行步驟
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
del /F /Q src\main\resources\mapper\*.xml
mvn mybatis-generator:generate -q
powershell -Command "腳本內容..."
```

腳本會自動：
1. ✅ 刪除所有舊的 Mapper XML（避免重複定義）
2. ✅ 執行 MyBatis Generator 生成新的 Mapper
3. ✅ 自動修正 OrderMapper.xml 的 `order` 保留字
4. ✅ 驗證修正結果

## 常見錯誤

### 錯誤 1：Result Maps collection already contains key
```
Caused by: java.lang.IllegalArgumentException: Result Maps collection already contains key com.group.admin.mapper.AdminOperationLogMapper.BaseResultMap
```

**原因**：多次執行 `mvn mybatis-generator:generate` 導致 Mapper XML 內容重複

**解決**：刪除所有 Mapper XML 後重新生成
```bash
del /F /Q src\main\resources\mapper\*.xml
mvn mybatis-generator:generate
```

### 錯誤 2：SQL 語法錯誤 near 'order'
```
java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax near 'order'
```

**原因**：`order` 是 MySQL 保留字，未加反引號

**解決**：執行 `regenerate-mappers.bat` 或手動修正

## 手動修正方式（不推薦）

如果無法執行腳本，可以手動替換：

```powershell
# PowerShell
$file='src\main\resources\mapper\OrderMapper.xml'
$content=Get-Content $file -Raw
$content=$content -replace ' from order\b',' from `order`'
$content=$content -replace ' into order\b',' into `order`'
$content=$content -replace ' update order\b',' update `order`'
$content=$content -replace 'delete from order\b','delete from `order`'
$content | Set-Content $file -NoNewline
```

## 長期方案（推薦）

建議聯繫 DBA 將表名改為 `orders` 或 `order_info`，從根本解決問題：

```sql
-- 將表名從 order 改成 orders
ALTER TABLE `order` RENAME TO `orders`;

-- 然後修改 generatorConfig.xml
<table tableName="orders" domainObjectName="Order" ...>
```

## MyBatis Generator 配置嘗試（已驗證無效）

已在 `generatorConfig.xml` 嘗試以下配置但未生效：
```xml
<!-- Table 層級 -->
<table tableName="order" ...>
    <property name="beginningDelimiter" value="`"/>
    <property name="endingDelimiter" value="`"/>
</table>
```

**原因**：MyBatis Generator 1.4.2 的 `beginningDelimiter` 只對欄位名生效，不對表名生效。

## 當前狀態
- ✅ 自動化腳本：`regenerate-mappers.bat`
- ✅ ReferralCode 問題：已在 Service 層設定預設值
- ✅ 重複 ResultMap 問題：已解決（刪除重新生成）
- ⚠️  表名：`order`（MySQL 保留字，需要持續維護）

## 每次重新生成 Mapper 時必做
```bash
.\regenerate-mappers.bat
```

或者記住這個口訣：**刪除 → 生成 → 修正 → 驗證**
