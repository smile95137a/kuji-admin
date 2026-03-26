# 外鍵約束錯誤修復指南

## 問題診斷

錯誤訊息：
```
Error Code: 3780. Referencing column 'store_id' and referenced column 'id' in foreign key constraint 'referral_code_ibfk_1' are incompatible.
```

### 根本原因

資料庫中的 `store` 表和 `user` 表的 `id` 欄位使用 **BIGINT**，  
而新建的 `referral_code`、`referral_record`、`user_address` 表使用 **VARCHAR(36)** UUID。

外鍵約束要求參考欄位與被參考欄位的資料型態必須完全一致。

---

## 解決方案總覽

### 方案 A：重建資料庫（推薦 - 開發階段）

✅ **適用情境**：
- 資料庫處於開發/測試階段
- 沒有重要的生產資料
- 希望快速修復問題

📝 **步驟**：

1. **備份現有資料庫**（預防萬一）
```bash
mysqldump -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -p kuji > backup_$(date +%Y%m%d_%H%M%S).sql
```

2. **刪除並重建資料庫**
```sql
DROP DATABASE IF EXISTS kuji;
CREATE DATABASE kuji CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kuji;
```

3. **執行 UUID 版本的 DDL**
```bash
mysql -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -p kuji < doc/DDL_UUID.sql
```

4. **執行推薦碼與地址表腳本（啟用外鍵版本）**
```bash
mysql -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -p kuji < src/main/resources/db/referral_address_schema_with_fk.sql
```

5. **重新啟動應用程式讓 DataInitializer 初始化資料**
```bash
mvn spring-boot:run
```

---

### 方案 B：執行不含外鍵約束的腳本（臨時方案）

✅ **適用情境**：
- 無法立即重建資料庫
- 需要保留現有資料
- 暫時不啟用外鍵完整性檢查

⚠️ **注意**：此方案不啟用外鍵約束，資料完整性需由應用程式層保證。

📝 **步驟**：

直接執行已修改的腳本（外鍵約束已註解掉）：
```bash
mysql -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -p kuji < src/main/resources/db/referral_address_schema.sql
```

---

### 方案 C：資料遷移（保留資料）

✅ **適用情境**：
- 資料庫有重要的生產資料
- 需要保留所有現有資料
- 願意投入時間進行複雜遷移

⚠️ **警告**：這是破壞性操作，執行前務必備份！

📝 **步驟**：

參考 `src/main/resources/db/migrate_to_uuid.sql` 中的詳細步驟。

主要流程：
1. 備份資料庫
2. 檢查所有依賴 store/user 表的外鍵
3. 為每個表添加臨時 UUID 欄位
4. 生成 UUID 並更新所有關聯
5. 刪除舊的 BIGINT 欄位
6. 重新建立外鍵約束

---

## 檔案說明

### 1. `referral_address_schema.sql`（當前檔案）
- **狀態**：外鍵約束已註解
- **用途**：臨時方案，可在 BIGINT 主鍵環境下執行
- **限制**：無外鍵完整性保護

### 2. `referral_address_schema_with_fk.sql`
- **狀態**：完整外鍵約束
- **用途**：標準方案，僅在 VARCHAR(36) UUID 環境下執行
- **優點**：完整的資料完整性保護

### 3. `migrate_to_uuid.sql`
- **狀態**：遷移指南
- **用途**：將 BIGINT 主鍵遷移到 UUID
- **適用**：有重要資料需保留時

### 4. `check_table_structure.sql`
- **狀態**：診斷工具
- **用途**：檢查當前資料庫表結構
- **執行**：用於確認主鍵型態

---

## 驗證步驟

### 1. 檢查資料庫表結構
```sql
SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'kuji'
AND TABLE_NAME IN ('store', 'user', 'referral_code', 'referral_record', 'user_address')
AND COLUMN_NAME = 'id'
ORDER BY TABLE_NAME;
```

預期結果（方案 A 執行後）：
```
store           | id | varchar | varchar(36)
user            | id | varchar | varchar(36)
referral_code   | id | varchar | varchar(36)
referral_record | id | varchar | varchar(36)
user_address    | id | varchar | varchar(36)
```

### 2. 檢查外鍵約束
```sql
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'kuji'
AND TABLE_NAME IN ('referral_code', 'referral_record', 'user_address')
AND REFERENCED_TABLE_NAME IS NOT NULL;
```

預期結果（方案 A 執行後）：
```
fk_referral_code_store  | referral_code   | store_id | store | id
fk_referral_record_user | referral_record | user_id  | user  | id
fk_referral_record_code | referral_record | referral_code_id | referral_code | id
fk_referral_record_store| referral_record | store_id | store | id
fk_user_address_user    | user_address    | user_id  | user  | id
```

### 3. 測試資料插入
```sql
-- 測試推薦碼（需要先有 store 資料）
INSERT INTO referral_code (id, code, store_id, description)
SELECT UUID(), 'TEST2024', id, '測試推薦碼'
FROM store LIMIT 1;

-- 測試地址（需要先有 user 資料）
INSERT INTO user_address (id, user_id, recipient_name, recipient_phone, city, district, address, is_default)
SELECT UUID(), id, '測試收件人', '0912345678', '台北市', '信義區', '測試路123號', 1
FROM `user` LIMIT 1;

-- 檢查插入結果
SELECT * FROM referral_code;
SELECT * FROM user_address;

-- 清理測試資料
DELETE FROM referral_code WHERE code = 'TEST2024';
DELETE FROM user_address WHERE recipient_name = '測試收件人';
```

---

## 推薦執行流程（開發環境）

### Step 1: 檢查當前狀態
```bash
mysql -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -p kuji < check_table_structure.sql
```

### Step 2: 備份資料庫
```bash
mysqldump -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -p kuji > backup_before_migration.sql
```

### Step 3: 重建資料庫（方案 A）
```sql
mysql -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -p
> DROP DATABASE IF EXISTS kuji;
> CREATE DATABASE kuji CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
> USE kuji;
> SOURCE doc/DDL_UUID.sql;
> SOURCE src/main/resources/db/referral_address_schema_with_fk.sql;
> exit;
```

### Step 4: 啟動應用程式初始化資料
```bash
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn clean spring-boot:run
```

### Step 5: 驗證結果
- 檢查應用程式啟動日誌
- 確認所有 Mapper 註冊成功
- 測試推薦碼和地址 API

---

## 常見問題 FAQ

### Q1: 為什麼會有兩種主鍵型態？
**A**: 專案初期使用 BIGINT AUTO_INCREMENT（DDL.sql），  
後來改為 UUID VARCHAR(36)（DDL_UUID.sql）以支援分散式系統。  
資料庫可能還在使用舊版 DDL。

### Q2: 不啟用外鍵約束有什麼風險？
**A**: 
- 可能插入無效的 store_id / user_id
- 刪除 store/user 時不會自動刪除關聯資料
- 需要在應用程式層嚴格檢查資料完整性

### Q3: 我應該選擇哪個方案？
**A**:
- 開發階段 → **方案 A**（重建資料庫）
- 測試環境有資料 → **方案 B**（暫時不啟用外鍵）
- 生產環境 → **方案 C**（資料遷移，需專業 DBA 協助）

### Q4: Entity 已經使用 String id，為什麼資料庫還是 BIGINT？
**A**: Entity 與資料庫結構可能不同步。  
需要確保資料庫執行的是 DDL_UUID.sql 而非 DDL.sql。

---

## 相關檔案清單

```
admin/
├── doc/
│   ├── DDL.sql                          # 舊版 DDL（BIGINT 主鍵）
│   └── DDL_UUID.sql                     # 新版 DDL（UUID 主鍵）✅
├── src/main/resources/db/
│   ├── referral_address_schema.sql      # 無外鍵版本
│   ├── referral_address_schema_with_fk.sql  # 有外鍵版本✅
│   └── migrate_to_uuid.sql              # 遷移指南
└── check_table_structure.sql            # 診斷工具
```

---

## 後續工作

完成修復後，請執行：

1. ✅ 測試所有推薦碼 API
2. ✅ 測試所有地址 API  
3. ✅ 驗證外鍵級聯刪除
4. ✅ 更新 Postman collection
5. ✅ 更新前端 API 文檔

---

**建議優先順序**：方案 A > 方案 B > 方案 C

如有問題，請參考專案文檔或聯繫開發團隊。
