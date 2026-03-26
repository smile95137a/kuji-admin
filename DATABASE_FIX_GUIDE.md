# 資料庫修復指南

## 問題說明

你的專案有以下資料庫問題：

### 1. **referral_code 表結構不匹配**
- **現有 Entity 欄位**：`ownerId`, `ownerType`, `rewardGold`, `rewardBonus`
- **Service 層需要**：`storeId`, `description`
- **結論**：表結構錯誤，需要重建

### 2. **referral_record 表結構不匹配**
- **現有 Entity 欄位**：`referralCodeId`, `referrerId`, `refereeId`, `rewardGold`
- **Service 層需要**：`userId`, `storeId`, `usedCode`, `referredAt`
- **結論**：表結構錯誤，需要重建

### 3. **user_address 表缺少 label 欄位**
- **解決方式**：已在 Entity 手動新增 `label` 欄位

## 修復步驟

### 步驟 1：執行 SQL 腳本（修復推薦碼系統）

```bash
# 連接到資料庫
mysql -h your-host -u your-user -p your-database

# 執行修復腳本
mysql> source c:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/fix-referral-tables.sql
```

或使用 MySQL Workbench / DBeaver 等工具執行 `fix-referral-tables.sql`

### 步驟 2：重新生成 Entity（使用安全方法）

⚠️ **不要** 直接執行 `mvn mybatis-generator:generate`（會刪除自定義方法）

建議做法：
```bash
# 方法 1：手動刪除 Entity/Example 檔案，保留 Mapper
rm src/main/java/com/group/admin/entity/ReferralCode.java
rm src/main/java/com/group/admin/entity/ReferralRecord.java
rm src/main/java/com/group/admin/example/ReferralCodeExample.java
rm src/main/java/com/group/admin/example/ReferralRecordExample.java
rm src/main/resources/mapper/ReferralCodeMapper.xml
rm src/main/resources/mapper/ReferralRecordMapper.xml

# 然後執行 MBG
mvn mybatis-generator:generate

# 方法 2：使用修改過的 MBGAutoRunner（推薦）
# 它會保留 Mapper 介面的自定義方法
# TODO: 需要先確保 MBGAutoRunner 正常執行
```

### 步驟 3：重新加入 Mapper 自定義方法

如果不小心執行了 `mvn mybatis-generator:generate` 刪除了自定義方法，請手動加回：

#### ReferralCodeMapper.java
```java
// ==================== 自定義方法（Annotation 方式）====================

@Select("SELECT * FROM referral_code WHERE code = #{code}")
ReferralCode selectByCode(@Param("code") String code);

@Select("SELECT * FROM referral_code WHERE store_id = #{storeId} ORDER BY created_at DESC")
List<ReferralCode> selectByStoreId(@Param("storeId") String storeId);

@Select("SELECT * FROM referral_code ORDER BY created_at DESC")
List<ReferralCode> selectAll();
```

#### ReferralRecordMapper.java
```java
// ==================== 自定義方法（Annotation 方式）====================

@Select("SELECT * FROM referral_record WHERE user_id = #{userId} ORDER BY created_at DESC")
List<ReferralRecord> selectByUserId(@Param("userId") String userId);

@Select("SELECT * FROM referral_record WHERE referral_code_id = #{referralCodeId} ORDER BY created_at DESC")
List<ReferralRecord> selectByReferralCodeId(@Param("referralCodeId") String referralCodeId);

@Select("SELECT * FROM referral_record WHERE store_id = #{storeId} ORDER BY created_at DESC")
List<ReferralRecord> selectByStoreId(@Param("storeId") String storeId);
```

### 步驟 4：驗證編譯

```bash
mvn clean compile -DskipTests
```

應該看到 `BUILD SUCCESS`

## 已修復的 Mapper

以下 Mapper 的自定義方法已經加回（不會被 MBG 刪除）：

1. ✅ **DistrictMapper** - 4 個方法
2. ✅ **MarqueeMapper** - 6 個方法
3. ✅ **EmailLogMapper** - 2 個方法
4. ✅ **SystemLogMapper** - 4 個方法
5. ✅ **UserAddressMapper** - 3 個方法
6. ✅ **ReportSnapshotMapper** - 1 個方法

**待修復**（需要先執行 SQL，重新生成 Entity）：
7. ⏳ **ReferralCodeMapper** - 3 個方法（等 SQL 執行後加）
8. ⏳ **ReferralRecordMapper** - 3 個方法（等 SQL 執行後加）

## 常見問題

### Q: 為什麼自定義方法一直被刪除？
A: 因為執行了 `mvn mybatis-generator:generate`，它會完全覆蓋 Mapper 檔案。解決方法：
- 使用修改過的 MBGAutoRunner
- 或在 generatorConfig.xml 設定 `overwrite="false"`（但會導致 XML 不更新）
- 或手動管理 Mapper（推薦）

### Q: 執行 SQL 後資料會不會遺失？
A: 會！`fix-referral-tables.sql` 使用 `DROP TABLE`。請先備份：
```sql
CREATE TABLE referral_code_backup AS SELECT * FROM referral_code;
CREATE TABLE referral_record_backup AS SELECT * FROM referral_record;
```

### Q: 能不能不刪表，只修改欄位？
A: 可以，但需要寫複雜的 ALTER TABLE 語句遷移資料。由於這是開發階段，建議直接重建。

## 檢查清單

- [ ] 備份現有 referral_code 和 referral_record 表的資料
- [ ] 執行 fix-referral-tables.sql
- [ ] 刪除 ReferralCode/ReferralRecord Entity 和 Example
- [ ] 執行 MyBatis Generator 重新生成
- [ ] 手動加回 ReferralCodeMapper 和 ReferralRecordMapper 的自定義方法
- [ ] 執行 mvn clean compile -DskipTests 驗證
- [ ] 測試推薦碼相關 API

## 需要執行的 SQL 檔案

1. **fix-referral-tables.sql** - 修復推薦碼系統表結構（必須）

## 未來防止問題

1. **不要直接執行 `mvn mybatis-generator:generate`**
2. **所有自定義方法加上註解標記**：`// ==================== 自定義方法 ====================`
3. **使用版本控制**：執行 MBG 前先 commit
4. **建立 Mapper 保護機制**：修改 MBGAutoRunner 保留 Mapper 檔案
