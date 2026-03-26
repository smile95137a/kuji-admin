# 缺少表的建立指南

## 📋 問題說明

專案編譯失敗是因為資料庫中缺少以下 8 個表：

1. ✅ `system_log` - 系統操作日誌
2. ✅ `user_address` - 用戶收件地址
3. ✅ `marquee` - 跑馬燈公告
4. ✅ `referral_code` - 推薦碼
5. ✅ `referral_record` - 推薦記錄
6. ✅ `email_log` - 郵件發送日誌
7. ✅ `district` - 行政區域（台灣縣市區域）
8. ✅ `report_snapshot` - 報表快照

## 🚀 快速修復（3 步驟）

### 方法 1：使用自動化腳本（推薦）

```bash
# 1. 執行自動化腳本
create-missing-tables.bat

# 腳本會引導你：
# - 執行 SQL 檔案
# - 執行 MBG 生成 Entity
# - 重新編譯專案
```

### 方法 2：手動執行

#### 步驟 1：建立資料庫表

1. 開啟 MySQL Workbench 或 DBeaver
2. 連線到 RDS：
   ```
   Host: database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com
   Port: 3306
   Database: (你的資料庫名稱)
   Username: (你的用戶名)
   Password: (你的密碼)
   ```
3. 開啟 `missing_tables_ddl.sql`
4. 執行整個 SQL 檔案

#### 步驟 2：執行 MyBatis Generator

```bash
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn mybatis-generator:generate
```

這會生成：
- `entity/SystemLog.java`
- `entity/UserAddress.java`
- `entity/Marquee.java`
- `entity/ReferralCode.java`
- `entity/ReferralRecord.java`
- `entity/EmailLog.java`
- `entity/District.java`
- `entity/ReportSnapshot.java`
- 對應的 Mapper 介面和 XML

#### 步驟 3：重新編譯

```bash
mvn clean compile -DskipTests
```

## 📊 表結構說明

### 1. system_log（系統日誌）
記錄所有後台操作行為，用於審計和追蹤。

**欄位：**
- `id` - 日誌 ID
- `user_id` - 操作者 ID
- `action` - 操作動作
- `module` - 操作模組
- `ip_address` - IP 地址
- `request_url` - 請求 URL
- `execution_time` - 執行時間

**用途：**
- 追蹤管理員操作記錄
- 安全審計
- 效能監控

### 2. user_address（用戶地址）
儲存用戶的收件地址。

**欄位：**
- `id` - 地址 ID
- `user_id` - 用戶 ID
- `recipient_name` - 收件人姓名
- `recipient_phone` - 收件人電話
- `postal_code` - 郵遞區號
- `city` - 城市
- `district` - 區域
- `address` - 詳細地址
- `is_default` - 是否為預設地址

**用途：**
- 用戶管理多個收件地址
- 訂單出貨地址選擇

### 3. marquee（跑馬燈）
前台網站頂部的公告訊息。

**欄位：**
- `id` - 跑馬燈 ID
- `title` - 標題
- `content` - 內容
- `link_url` - 連結 URL
- `start_time` - 開始顯示時間
- `end_time` - 結束顯示時間
- `is_enabled` - 是否啟用
- `display_order` - 顯示順序

**用途：**
- 顯示重要公告
- 活動宣傳
- 系統維護通知

### 4. referral_code（推薦碼）
推薦碼系統，用於推廣和獎勵。

**欄位：**
- `id` - 推薦碼 ID
- `code` - 推薦碼（唯一）
- `owner_id` - 擁有者 ID
- `owner_type` - 擁有者類型（ADMIN/STORE）
- `reward_gold` - 推薦者獲得的 Gold
- `reward_bonus` - 被推薦者獲得的 Bonus
- `max_usage` - 最大使用次數
- `used_count` - 已使用次數
- `valid_from` - 有效期起始
- `valid_until` - 有效期結束

**用途：**
- 推薦好友獎勵
- 新用戶註冊優惠
- 店家推廣活動

### 5. referral_record（推薦記錄）
記錄推薦碼使用記錄。

**欄位：**
- `id` - 記錄 ID
- `referral_code_id` - 推薦碼 ID
- `referrer_id` - 推薦人 ID
- `referee_id` - 被推薦人 ID
- `reward_gold` - 推薦者獲得的 Gold
- `reward_bonus` - 被推薦者獲得的 Bonus
- `is_reward_given` - 是否已發放獎勵

**用途：**
- 追蹤推薦效果
- 獎勵發放記錄
- 數據分析

### 6. email_log（郵件日誌）
記錄所有發送的郵件。

**欄位：**
- `id` - 日誌 ID
- `recipient_email` - 收件人 Email
- `subject` - 郵件主題
- `content` - 郵件內容
- `template_name` - 使用的範本名稱
- `status` - 狀態（PENDING/SENT/FAILED）
- `error_message` - 錯誤訊息
- `sent_at` - 發送時間

**用途：**
- 郵件發送追蹤
- 失敗重試機制
- 用戶溝通記錄

### 7. district（行政區域）
台灣縣市區域資料。

**欄位：**
- `id` - 區域 ID
- `city` - 城市
- `district_name` - 區域名稱
- `postal_code` - 郵遞區號
- `display_order` - 顯示順序
- `is_active` - 是否啟用

**用途：**
- 地址選擇下拉選單
- 郵遞區號自動填入
- 物流配送範圍判斷

**初始資料：**
SQL 檔案已包含台北市 12 個行政區的範例資料。

### 8. report_snapshot（報表快照）
定期儲存的業績報表。

**欄位：**
- `id` - 快照 ID
- `report_type` - 報表類型（DAILY/WEEKLY/MONTHLY/YEARLY）
- `report_date` - 報表日期
- `store_id` - 店家 ID
- `total_revenue` - 總營收
- `total_orders` - 總訂單數
- `total_users` - 總用戶數
- `total_draws` - 總抽獎次數
- `data_json` - 詳細數據（JSON）

**用途：**
- 店家業績統計
- 營收趨勢分析
- 報表快速查詢

## ⚠️ 注意事項

### 1. 這些表不影響核心抽獎功能
這些表主要用於：
- 系統管理功能（日誌、跑馬燈）
- 用戶體驗功能（地址管理、推薦碼）
- 數據分析功能（報表快照）

**核心抽獎功能（Lottery, LotteryPrize, PrizeBox, Order）已經完整！**

### 2. 可以選擇性建立
如果暫時不需要某些功能，可以：
- 只建立必要的表
- 或者註解掉對應的 Service 和 Controller

### 3. 整合 API 不受影響
我剛實作的**商品與獎品整合 API**（`AdminLotteryWithPrizesController`）完全不依賴這些缺少的表，建立表後可以直接使用！

## ✅ 驗證步驟

建立表後，執行以下命令驗證：

```bash
# 1. 檢查 Entity 是否生成
dir src\main\java\com\group\admin\entity\SystemLog.java
dir src\main\java\com\group\admin\entity\UserAddress.java
dir src\main\java\com\group\admin\entity\Marquee.java

# 2. 編譯專案
mvn clean compile -DskipTests

# 3. 查看編譯結果
# 應該看到 "BUILD SUCCESS"
```

## 🎉 完成後

建立表並生成 Entity 後，你就可以：
1. ✅ 編譯專案成功
2. ✅ 使用整合 API（`POST /admin/lottery-with-prizes`）
3. ✅ 測試完整的抽獎流程
4. ✅ 部署到 EC2

## 📞 需要協助？

如果執行過程中遇到問題：
1. 檢查資料庫連線是否正常
2. 確認 `application.yml` 中的資料庫設定
3. 查看 MBG 執行的錯誤訊息
4. 提供完整的錯誤日誌

祝你順利完成！🚀
