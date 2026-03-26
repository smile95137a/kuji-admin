# 資料模型：推薦碼 (Referral Code)

**Feature**: 012-referral-code
**Date**: 2026-03-22

---

## Entity 概覽

```
Store (existing)
  id: String (UUID PK)
  storeName: String
  status: String (ACTIVE | INACTIVE)

    │
    │ 1 : many
    ▼

ReferralCode (existing entity, referral_code table)
  id: String (UUID PK)
  code: String (UNIQUE, uppercase alphanumeric, 8 chars)
  storeId: String (FK → store.id)
  isActive: Boolean (default true)
  description: String
  ownerId: String
  ownerType: String (STORE | ADMIN)
  rewardGold: Long (v2 use)
  rewardBonus: Long (v2 use)
  maxUsage: Integer (null = unlimited)
  usedCount: Integer (default 0)
  validFrom: LocalDateTime (null = no start restriction)
  validUntil: LocalDateTime (null = no expiry)
  createdAt: LocalDateTime
  updatedAt: LocalDateTime

    │
    │ 1 : many (one code used many times... except
    │          referral_record has UNIQUE(user_id))
    ▼

ReferralRecord (existing entity, referral_record table)
  id: String (UUID PK)
  userId: String (UNIQUE FK → user.id)
  referralCodeId: String (FK → referral_code.id)
  storeId: String (denormalized FK → store.id)
  usedCode: String (snapshot of code value at time of use)
  referredAt: LocalDateTime
  referralCode: String (snapshot — kept for historical integrity)
  referrerId: String (optional)
  refereeId: String (alias for userId)
  refereeUsername: String (snapshot of nickname)
  rewardGold: Long (0 in v1.0)
  rewardBonus: Long (0 in v1.0)
  isRewardGiven: Boolean (false in v1.0)
  rewardGivenAt: LocalDateTime (null in v1.0)
  createdAt: LocalDateTime

    ▲
    │ 1 : 1 (one referral record per user)
    │
User (existing entity, user table)
  id: String (UUID PK)
  email: String (UNIQUE)
  referralCode: String (the code that was used at registration)
  referredStoreId: String (FK → store.id)
  ... (other fields unchanged)
```

---

## Entity：ReferralCode

**資料表**: `referral_code`

| 欄位 | 型別 | 限制 | 備註 |
|--------|------|-------------|-------|
| id | VARCHAR(36) | PK | UUID |
| code | VARCHAR(20) | UNIQUE NOT NULL | 大寫英數字，8 個字元 |
| store_id | VARCHAR(36) | NOT NULL, FK | 關聯至 `store.id` |
| description | VARCHAR(255) | | 選用標籤 |
| owner_id | VARCHAR(36) | | 代碼建立者 |
| owner_type | VARCHAR(20) | | "STORE" 或 "ADMIN" |
| reward_gold | BIGINT | DEFAULT 0 | v2 使用——儲存供未來獎勵功能 |
| reward_bonus | BIGINT | DEFAULT 0 | v2 使用——儲存供未來獎勵功能 |
| max_usage | INT | NULLABLE | NULL 表示無限制 |
| used_count | INT | DEFAULT 0 | 每次使用後遞增 |
| valid_from | DATETIME | NULLABLE | NULL = 無起始時間限制 |
| valid_until | DATETIME | NULLABLE | NULL = 永不過期 |
| is_active | TINYINT(1) | DEFAULT 1 | 0 = 已停用 |
| created_at | DATETIME | NOT NULL | 於 insert 時設定 |
| updated_at | DATETIME | | 每次儲存時更新 |

**索引**：
- `UNIQUE INDEX idx_referral_code_code (code)`
- `INDEX idx_referral_code_store_id (store_id)`
- `INDEX idx_referral_code_is_active (is_active)`

**狀態轉換**：
```
ACTIVE (is_active=1) ──[Admin disables]──► DISABLED (is_active=0)
```
備註：在 v1.0 中停用為終態（未規劃重新啟用，但技術上未被禁止）。

---

## Entity：ReferralRecord

**資料表**: `referral_record`

| 欄位 | 型別 | 限制 | 備註 |
|--------|------|-------------|-------|
| id | VARCHAR(36) | PK | UUID |
| user_id | VARCHAR(36) | UNIQUE NOT NULL, FK | 每用戶一筆紀錄；關聯至 `user.id` |
| referral_code_id | VARCHAR(36) | NOT NULL, FK | 關聯至 `referral_code.id` |
| store_id | VARCHAR(36) | NOT NULL | 反正規化，提升查詢效率 |
| used_code | VARCHAR(20) | NOT NULL | 代碼字串的快照 |
| referred_at | DATETIME | NOT NULL | 完成註冊的時間 |
| referral_code | VARCHAR(20) | | 重複快照（舊有欄位） |
| referrer_id | VARCHAR(36) | NULLABLE | 選用：推薦人（店家擁有者） |
| referee_id | VARCHAR(36) | | 與 user_id 相同（舊有欄位） |
| referee_username | VARCHAR(100) | | 使用者暱稱快照 |
| reward_gold | BIGINT | DEFAULT 0 | v2：發放的金幣數量 |
| reward_bonus | BIGINT | DEFAULT 0 | v2：發放的獎勵幣數量 |
| is_reward_given | TINYINT(1) | DEFAULT 0 | v2：獎勵發放旗標 |
| reward_given_at | DATETIME | NULLABLE | v2：獎勵發放時間 |
| created_at | DATETIME | NOT NULL | 於 insert 時設定 |

**索引**：
- `UNIQUE INDEX idx_referral_record_user_id (user_id)`  ← **需要遷移**（新增此索引）
- `INDEX idx_referral_record_referral_code_id (referral_code_id)`
- `INDEX idx_referral_record_store_id (store_id)`
- `INDEX idx_referral_record_referred_at (referred_at)`

**不可變性規則**：
- 僅允許 INSERT：應用程式程式碼不得對此資料表執行 UPDATE 操作
- `referred_at` 在建立時設定一次，不得更改
- `used_code` 為快照——代碼字串的後續變更不影響歷史紀錄

---

## 必要資料庫遷移

```sql
-- Add missing unique constraint to enforce one-referral-per-user
ALTER TABLE referral_record
  ADD UNIQUE INDEX idx_referral_record_user_id (user_id);
```

儲存路徑：`sql/V012__add_referral_record_user_unique.sql`

---

## 驗證規則

### ReferralCode
| 欄位 | 規則 |
|-------|------|
| code | 必填；大寫英數字；1–20 字元；全域唯一 |
| storeId | 必填；必須關聯至活躍的店家 |
| maxUsage | 選填；若設定，必須 ≥ 1 |
| validFrom / validUntil | 選填；若兩者皆設定，validFrom 必須早於 validUntil |
| isActive | 預設為 true；由停用操作設為 false |

### ReferralRecord（由 Service 建立，非使用者輸入）
| 欄位 | 規則 |
|-------|------|
| userId | 不得已存在 ReferralRecord（UNIQUE 強制執行） |
| referralCodeId | 必須關聯至存在且活躍的 ReferralCode |
| storeId | 店家必須為 ACTIVE（FR-007） |

---

## DTO 摘要

### ReferralCodeCreateReq（現有）
```json
{
  "storeId": "uuid",
  "description": "2026 Spring Campaign",
  "maxUsage": 100,
  "validFrom": "2026-03-01T00:00:00",
  "validUntil": "2026-06-30T23:59:59"
}
```
備註：`code` 由伺服器端自動生成（8 字元大寫英數字）。

### ReferralValidateReq（新增）
```json
{
  "code": "ABC12345"
}
```

### ReferralCodeRes（現有）
```json
{
  "id": "uuid",
  "code": "ABC12345",
  "storeId": "uuid",
  "storeName": "Dream Store",
  "description": "Spring Campaign",
  "isActive": true,
  "usedCount": 12,
  "maxUsage": 100,
  "validFrom": "2026-03-01T00:00:00",
  "validUntil": "2026-06-30T23:59:59",
  "createdAt": "2026-03-22T10:00:00"
}
```

### ReferralStatsRes（新增）
```json
{
  "storeId": "uuid",
  "storeName": "Dream Store",
  "totalReferrals": 50,
  "activeCodeCount": 3,
  "timeline": [
    { "date": "2026-03-01", "count": 5 },
    { "date": "2026-03-02", "count": 3 }
  ]
}
```
