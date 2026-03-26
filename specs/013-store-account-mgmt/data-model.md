# Data Model: 店家帳號管理 (Store Account Management)

**Feature**: `013-store-account-mgmt`  
**Date**: 2026-03-22

---

## 概覽

本功能所需的所有核心實體均已存在於資料庫結構中。**無需新增資料表。** 本文件說明本功能使用的現有實體、所需新增的欄位，以及新的 Redis 資料結構。

---

## 現有實體（無需變更資料庫結構）

### 1. `AdminUser` — 後台管理員帳號

**資料表**：`admin_user`  
**狀態**：已存在 — 無需 DDL 變更

| 欄位 | 類型 | 可為 NULL | 備註 |
|--------|------|----------|-------|
| `id` | VARCHAR(36) | 否 | UUID 主鍵 |
| `username` | VARCHAR(100) | 否 | 登入識別符（以電子郵件作為 username） |
| `password` | VARCHAR(255) | 否 | BCrypt 雜湊 — 絕不儲存明文 |
| `email` | VARCHAR(255) | 否 | 唯一；用於寄送初始密碼 |
| `display_name` | VARCHAR(100) | 是 | 顯示於管理後台 |
| `phone` | VARCHAR(20) | 是 | 選填聯絡電話 |
| `status` | VARCHAR(20) | 否 | `PENDING` / `ACTIVE` / `INACTIVE`（AdminUserStatus 列舉） |
| `force_change_password` | TINYINT(1) | 否 | 建立時為 `true`，首次登入修改密碼後為 `false` |
| `last_login_at` | DATETIME | 是 | 每次成功登入時更新 |
| `created_by` | VARCHAR(36) | 是 | 建立此帳號的管理員 user ID |
| `created_at` | DATETIME | 否 | 建立時間戳記 |
| `updated_by` | VARCHAR(36) | 是 | 最後修改此帳號的管理員 user ID |
| `updated_at` | DATETIME | 是 | 最後修改時間戳記 |
| `remark` | TEXT | 是 | 選填備註 |

**唯一限制**：`email`（由業務規則隱性約束，在 Service 層透過 `AdminUserExample` 強制執行）  
**索引**：`email`（用於登入查詢）

**狀態轉換**：
```
[建立] → PENDING
     ↓  (首次登入密碼變更完成)
   ACTIVE
     ↓↑  (管理員啟用／停用)
   INACTIVE
```

**業務規則**：
- `email` 在所有 `AdminUser` 記錄中必須唯一（後台系統與前台 `user` 表分離）
- `username` = 建立時的 `email`（管理員設定 email；username 自動設為 email）
- 每個新帳號的 `force_change_password = true`
- 建立時 `status = PENDING`；首次登入修改密碼後轉為 `ACTIVE`
- `INACTIVE` 帳號無法登入；其 Token 立即在 Redis 中加入黑名單

---

### 2. `StoreUser` — 店家帳號綁定（角色指派）

**資料表**：`store_user`  
**狀態**：已存在 — 無需 DDL 變更

| 欄位 | 類型 | 可為 NULL | 備註 |
|--------|------|----------|-------|
| `id` | VARCHAR(36) | 否 | UUID 主鍵 |
| `store_id` | VARCHAR(36) | 否 | FK → `store.id` |
| `admin_user_id` | VARCHAR(36) | 否 | FK → `admin_user.id` |
| `role_type` | VARCHAR(30) | 否 | `STORE_OWNER` 或 `STORE_EDITOR` |
| `created_at` | DATETIME | 否 | 綁定建立時間戳記 |

**唯一限制**：`(store_id, admin_user_id, role_type)` — 防止重複綁定  
**索引**：`admin_user_id`（篩選指定使用者的所有店家）、`store_id`（列出店家中的所有使用者）

**基數關係**：
- `STORE_OWNER`：一家店有且僅有一位擁有者（在 Service 中強制：檢查現有 STORE_OWNER 的 `StoreUser` + `Store.ownerId`）
- `STORE_EDITOR`：一家店可有多位編輯者；一位編輯者可屬於多家店

---

### 3. `Store` — 店家記錄（參照，非此功能建立）

**資料表**：`store`  
**狀態**：已存在 — 建立 StoreOwner 帳號時使用 `ownerId` 欄位

| 欄位 | 類型 | 備註 |
|--------|------|-------|
| `id` | VARCHAR(36) | UUID PK |
| `owner_id` | VARCHAR(36) | FK → `admin_user.id`；建立 StoreOwner 帳號時設定 |
| `store_name` | VARCHAR(255) | — |
| `status` | VARCHAR(20) | `ACTIVE` / `INACTIVE` |
| *（其他欄位）* | — | 本功能不修改 |

**本功能在建立 StoreOwner 帳號時會更新 `Store.ownerId`。**

---

## 新增 Redis 結構

### Token 世代計數器（黑名單）

**Key 模式**：`blacklist_gen:{adminUserId}`  
**類型**：Redis String（整數計數器）  
**TTL**：30 天（與最大刷新 Token 有效期相符）

**用途**：無需儲存個別 Token 即可實現即時 Token 失效。

**協議**：
1. 建立新帳號或發行 Token 時，讀取世代計數器（若不存在則預設為 0）。
2. 以 JWT 自訂宣告嵌入 `gen` 值。
3. 每次已驗證請求時，`AdminJwtAuthenticationFilter` 從 Redis 讀取 `blacklist_gen:{adminUserId}` 並與 Token 的 `gen` 宣告比較。
4. 若 `Redis gen > token gen` → 以 401 Unauthorized 拒絕。
5. 當帳號被**停用**時 → `INCR blacklist_gen:{adminUserId}` → 所有現有 Token 立即失效。
6. 當帳號**重新啟用**時 → 無需更改計數器；新登入將以當前世代值發行 Token。

**範例**：
```
blacklist_gen:user-abc-123   →   "3"   (TTL: 30 days)
```

**降級處理**：若 Redis 無法連線，預設為**拒絕**（丟出 503 或視 Token 為無效）— 本功能安全優先於可用性。

---

## 關聯圖

```
AdminUser (1) ──── (0..1) Store         [via Store.ownerId — StoreOwner role]
AdminUser (1) ──── (0..N) StoreUser     [all role bindings for this user]
Store     (1) ──── (0..N) StoreUser     [all users bound to this store]

StoreUser.roleType:
  STORE_OWNER  → exactly one per store (enforced)
  STORE_EDITOR → many per store allowed
```

---

## 驗證規則

| 欄位 | 規則 |
|-------|------|
| `AdminUser.email` | 有效 email 格式；在 `admin_user` 表中唯一 |
| `AdminUser.password` | 首次登入修改後最少 8 字元；儲存時以 BCrypt 編碼 |
| `AdminUser.status` | 列舉：僅允許 PENDING、ACTIVE、INACTIVE |
| `StoreUser.roleType` | 列舉：僅允許 STORE_OWNER、STORE_EDITOR |
| `Store.ownerId`（建立時） | 目標店家必須尚未有擁有者（檢查 `Store.ownerId IS NOT NULL`） |
| 初始密碼 | 8–12 字元，至少 1 個大寫 + 1 個小寫 + 1 個數字；由伺服器產生 |

---

## 原子性需求（FR-012）

下列操作必須包在單一 `@Transactional` 區塊中：

1. **建立 StoreOwner**：`AdminUser` INSERT + `StoreUser` INSERT + `Store.ownerId` UPDATE
2. **建立 StoreEditor**：`AdminUser` INSERT + `StoreUser` INSERT
3. **刪除 StoreUser 綁定**（角色變更時）：`StoreUser` DELETE + 新的 `StoreUser` INSERT（+ 可選的 `Store.ownerId` UPDATE）

若任何步驟失敗，整個交易回滾。
