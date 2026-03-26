# 外鍵約束錯誤修復總結

## 問題描述

執行 `referral_address_schema.sql` 時發生錯誤：

```
Error Code: 3780. Referencing column 'store_id' and referenced column 'id' 
in foreign key constraint 'referral_code_ibfk_1' are incompatible.
```

## 根本原因

**資料型態不匹配**：
- ❌ 資料庫中的 `store` 表和 `user` 表使用 **BIGINT AUTO_INCREMENT** 主鍵（來自 `DDL.sql`）
- ✅ 新建的推薦碼/地址表使用 **VARCHAR(36) UUID** 主鍵（來自 `DDL_UUID.sql`）
- 🔴 MySQL 外鍵要求參考欄位與被參考欄位的資料型態完全一致

## 修復方案

### 🥇 推薦方案：重建資料庫（開發階段）

**適用情境**：資料庫處於開發/測試階段，沒有重要生產資料

**執行工具**：
```bash
fix-foreign-key-error.bat
選擇選項 [2]
```

**手動執行**：
```bash
# 1. 備份
mysqldump -h ... -u admin -p kuji > backup.sql

# 2. 重建
mysql -h ... -u admin -p
> DROP DATABASE kuji;
> CREATE DATABASE kuji CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
> SOURCE doc/DDL_UUID.sql;
> SOURCE src/main/resources/db/referral_address_schema_with_fk.sql;

# 3. 重啟應用初始化資料
mvn spring-boot:run
```

**結果**：
- ✅ 所有表使用 UUID 主鍵
- ✅ 外鍵完整性保護啟用
- ✅ 支援分散式系統擴展
- ✅ 與 Entity 類別完全對應

---

### 🥈 臨時方案：使用無外鍵版本

**適用情境**：無法立即重建資料庫，需要保留現有資料

**執行工具**：
```bash
fix-foreign-key-error.bat
選擇選項 [3]
```

**手動執行**：
```bash
mysql -h ... -u admin -p kuji < src/main/resources/db/referral_address_schema.sql
```

**結果**：
- ✅ 表建立成功
- ⚠️ 無外鍵約束保護
- ⚠️ 需要應用程式層嚴格檢查資料完整性

---

### 🥉 複雜方案：資料遷移

**適用情境**：生產環境有重要資料需保留

**執行步驟**：參考 `migrate_to_uuid.sql` 詳細步驟

**注意事項**：
- 需要完整的依賴分析
- 需要專業 DBA 協助
- 執行時間較長
- 風險較高

---

## 檔案清單

### 新建檔案（共 5 個）

| 檔案 | 用途 | 說明 |
|------|------|------|
| `FOREIGN_KEY_FIX_GUIDE.md` | 📖 完整修復指南 | 包含所有方案的詳細步驟和驗證方法 |
| `referral_address_schema.sql` | 📄 無外鍵版本 | 外鍵約束已註解，可在 BIGINT 環境執行 |
| `referral_address_schema_with_fk.sql` | 📄 完整版本 | 啟用外鍵約束，需要 UUID 環境 |
| `migrate_to_uuid.sql` | 📄 遷移指南 | BIGINT → UUID 遷移步驟 |
| `check_table_structure.sql` | 🔍 診斷工具 | 檢查表結構的 SQL 查詢 |
| `fix-foreign-key-error.bat` | 🛠️ 自動化工具 | 互動式修復工具（Windows） |

### 修改檔案

- ✅ `src/main/resources/db/referral_address_schema.sql`：外鍵約束已註解

---

## 快速開始

### Step 1: 診斷問題
```bash
fix-foreign-key-error.bat
選擇 [1] 檢查資料庫表結構
```

### Step 2: 選擇方案
- 如果 id 是 **bigint** → 選擇 [2] 重建 或 [3] 無外鍵版本
- 如果 id 是 **varchar** → 選擇 [4] 完整版本

### Step 3: 驗證結果
```sql
-- 檢查表結構
DESCRIBE store;
DESCRIBE `user`;
DESCRIBE referral_code;

-- 檢查外鍵
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    REFERENCED_TABLE_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'kuji'
AND REFERENCED_TABLE_NAME IS NOT NULL;
```

### Step 4: 測試 API
```bash
# 啟動應用
mvn spring-boot:run

# 測試推薦碼 API
curl -X POST http://localhost:8080/api/admin/referral-codes ...

# 測試地址 API
curl -X POST http://localhost:8080/api/user/addresses ...
```

---

## 技術說明

### BIGINT vs UUID 比較

| 項目 | BIGINT AUTO_INCREMENT | VARCHAR(36) UUID |
|------|----------------------|------------------|
| 儲存空間 | 8 bytes | 36 bytes |
| 效能 | 較快 | 略慢 |
| 分散式支援 | ❌ 難以合併 | ✅ 支援分散式 |
| 安全性 | ⚠️ ID 可預測 | ✅ 不可預測 |
| 遷移成本 | ❌ 需要遷移 | ✅ 未來擴展容易 |
| 與 Entity 對應 | ❌ 不一致 | ✅ 完全對應 |

### 為什麼推薦 UUID？

1. **Entity 已使用 String id**：所有 Entity 類別（Store.java, User.java）都定義為 `private String id;`
2. **分散式系統支援**：未來如果需要多資料庫部署，UUID 不會衝突
3. **安全性**：BIGINT 可預測（如 store/1, store/2），UUID 無法猜測
4. **一致性**：新的表（lottery, prize_box, order）已經使用 UUID

---

## 常見問題

### Q1: 為什麼會有這個問題？
**A**: 資料庫可能使用了舊版的 `DDL.sql`（BIGINT），而程式碼已經升級到 UUID。

### Q2: 不啟用外鍵有什麼影響？
**A**:
- 無法自動級聯刪除（刪除 store 時不會自動刪除 referral_code）
- 可能插入無效的 store_id（指向不存在的 store）
- 需要在 Service 層手動檢查資料完整性

### Q3: 重建資料庫會影響什麼？
**A**:
- 所有資料將被刪除
- DataInitializer 會重新建立基礎資料（roles, menus, admin 帳號）
- 開發階段建議重建，生產環境需要遷移

### Q4: 如何選擇方案？
**A**:
- 開發環境 → 方案 A（重建）
- 測試環境有測試資料 → 方案 B（無外鍵）
- 生產環境 → 方案 C（遷移，需專業協助）

---

## 後續工作

完成修復後，請依序執行：

- [ ] ✅ 驗證資料庫表結構
- [ ] ✅ 啟動應用程式
- [ ] ✅ 檢查 DataInitializer 日誌
- [ ] ✅ 測試推薦碼 CRUD API
- [ ] ✅ 測試地址 CRUD API
- [ ] ✅ 驗證外鍵級聯刪除
- [ ] ✅ 更新 API 測試文檔
- [ ] ✅ 通知前端團隊

---

## 支援

如有問題，請參考：
- 📖 `FOREIGN_KEY_FIX_GUIDE.md`：完整修復指南
- 📖 `REFERRAL_ADDRESS_IMPLEMENTATION_COMPLETE.md`：功能實作文檔
- 🛠️ `fix-foreign-key-error.bat`：自動化修復工具

---

**建議行動**：立即執行 `fix-foreign-key-error.bat` 選項 [1] 診斷問題，然後根據結果選擇適當的修復方案。
