# 後台前端整合指南 — 本次後端修改對照說明

> **文件目的**：列出本次後端全數修改項目，說明每個 API 的參數意義、哪些欄位前端必須配合調整，以及各功能的實際行為變化。  
> **適用對象**：後台前端開發人員。  
> **最後更新**：2026-05-09

---

## 目錄

1. [用戶註冊 — null 錯誤修復](#1-用戶註冊--null-錯誤修復)
2. [後台登入 — forceChangePassword 標誌](#2-後台登入--forcechangepassword-標誌)
3. [後台帳號管理 — 建立帳號自動寄送 Email](#3-後台帳號管理--建立帳號自動寄送-email)
4. [後台帳號管理 — 重設密碼自動寄送 Email](#4-後台帳號管理--重設密碼自動寄送-email)
5. [後台帳號管理 — 修改密碼 API](#5-後台帳號管理--修改密碼-api)
6. [角色管理 — role code 改為唯讀](#6-角色管理--role-code-改為唯讀)
7. [系統日誌 — 新增查詢 API](#7-系統日誌--新增查詢-api)
8. [商品管理 — 刪除功能修復](#8-商品管理--刪除功能修復)
9. [商品管理 — 定時上架 SQL 修復](#9-商品管理--定時上架-sql-修復)
10. [儲值方案 — 活動時間區間](#10-儲值方案--活動時間區間)
11. [選單管理 — 角色選單權限補救初始化](#11-選單管理--角色選單權限補救初始化)
12. [選單異動 — 移除兩個廢棄選單](#12-選單異動--移除兩個廢棄選單)
13. [訂單取消 — 賞品盒回收邏輯確認](#13-訂單取消--賞品盒回收邏輯確認)
14. [2026-05-09 帳號治理與 SMTP 補完](#14-2026-05-09-帳號治理與-smtp-補完)

---

## 14. 2026-05-09 帳號治理與 SMTP 補完

### A. 前後台忘記密碼流程統一（臨時密碼模式）

#### 前台 API

```
POST /api/auth/forgot-password
```

行為：

1. 後端直接產生「臨時密碼」並覆蓋舊密碼。
2. 寄送臨時密碼郵件（模板：`temporary-password-email`）。
3. 登入後會回傳 `forceChangePassword=true`，且在改密碼前禁止操作受保護 API。

回應：

```json
{
  "success": true,
  "data": {
    "message": "如果此 Email 已註冊，將會收到臨時密碼郵件"
  }
}
```

> 舊版 `POST /api/auth/reset-password` 已保留，但只回覆「流程已停用，請改用忘記密碼」的錯誤訊息。

#### 後台 API

```
POST /admin/auth/forgot-password
```

行為：

1. 後端重設為臨時密碼。
2. 寄送臨時密碼郵件。
3. 下次登入必須先改密碼（`forceChangePassword=true`）。

---

### B. 後台本人 API（避免前端再用任意 userId 更新）

新增：

```
GET  /admin/users/me
PUT  /admin/users/me
POST /admin/users/me/change-password
```

`PUT /admin/users/me` 目前僅允許更新：

- `displayName`
- `phone`

---

### C. 店家負責人可管理自己店內小編

已實作卡控：

1. `POST /admin/users/list`：StoreOwner 查詢時會自動帶入自己的 `storeId`，且只查 `ROLE_STORE_EDITOR`。
2. `PUT /admin/users/{id}`：StoreOwner 可更新同店小編資料。
3. `POST /admin/users/{id}/reset-password`：StoreOwner 可為同店小編重發臨時密碼。

禁止行為：

- 跨店操作小編
- 操作其他 StoreOwner
- 操作 Admin

---

### D. 錯誤格式統一（重點）

本次把以下路徑改成統一錯誤格式：

1. `GlobalExceptionHandler` 統一依 `errorCode` 映射 HTTP Status。
2. `ApiAuthController` 移除手動 `try/catch` 回 map 的做法。
3. `AdminJwtAuthenticationFilter` / `ApiJwtAuthenticationFilter` 的拒絕回應改成 `ApiResponse.error(...)` 結構。

前端建議改為依 `error.code` 判斷，而不是只看 HTTP 400。

#### 本次流程常用錯誤碼對照

| error.code | HTTP Status | 典型觸發點 | 前端建議處理 |
|------|------|------|------|
| `COMMON_VALIDATION_001` | 400 | 欄位格式錯誤、舊版 `/auth/reset-password` 被呼叫 | 顯示表單錯誤或流程停用提示 |
| `AUTH_TOKEN_001` | 401 | Refresh Token 無效或過期 | 清除登入狀態並導回登入頁 |
| `AUTH_TOKEN_002` | 401 | Token 已撤銷（logout 後或黑名單） | 清除登入狀態並導回登入頁 |
| `AUTH_PASSWORD_001` | 403 | 使用者尚未完成強制改密碼，存取受保護 API | 強制跳轉「修改密碼」頁 |
| `COMMON_ACCESS_001` | 403 | 權限不足（例如越權操作） | 顯示無權限提示，不重試 |
| `COMMON_INTERNAL_001` | 500 | 非預期系統錯誤 | 顯示通用錯誤並提供重試 |

#### 錯誤回應結構（統一）

```json
{
  "success": false,
  "message": "Refresh Token 無效或已過期",
  "error": {
    "code": "AUTH_TOKEN_001",
    "message": "Refresh Token 無效或已過期"
  },
  "meta": {
    "timestamp": "2026-05-09T10:00:00",
    "requestId": "uuid"
  }
}
```

---

## 1. 用戶註冊 — null 錯誤修復

### 問題描述

前台會員以 Email 或 Google OAuth 方式註冊時，後端拋出：  
`Column 'failed_login_attempts' cannot be null`

### 修復內容

後端在建立 user 資料列時補上 `failedLoginAttempts = 0`，修復了以下三個路徑：

- Email 一般註冊
- Google OAuth 首次登入（自動建立帳號）
- DataInitializer 測試帳號

### 前端是否需要調整

**不需要。** 純後端 bug 修復，前端無任何參數變更。

---

## 2. 後台登入 — forceChangePassword 標誌

### API

```
POST /admin/auth/login
```

### 登入回應結構（完整）

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "forceChangePassword": true,
    "user": {
      "id": "uuid-string",
      "username": "store@example.com",
      "displayName": "王小明",
      "roles": ["ROLE_STORE_OWNER"]
    }
  }
}
```

### forceChangePassword 欄位說明

| 欄位 | 型別 | 說明 |
|------|------|------|
| `forceChangePassword` | Boolean | `true` = 此帳號為首次登入或 Admin 剛重設密碼，**必須立即修改密碼** |

### 前端必須配合

1. 登入後讀取 `data.forceChangePassword`
2. 若為 `true`，**強制跳轉至修改密碼頁面**，不允許進入其他頁面
3. 修改密碼成功後才允許正常使用

---

## 3. 後台帳號管理 — 建立帳號自動寄送 Email

### API

**建立店家負責人（StoreOwner）**

```
POST /admin/users/store-owner
權限：ROLE_ADMIN
```

Request Body：

```json
{
  "email": "owner@example.com",
  "displayName": "王小明",
  "phone": "0912345678",
  "remark": "備註（選填）",

  "storeName": "KUJI 官方商店",
  "shortDescription": "專營一番賞、扭蛋精品",
  "longDescription": "詳細介紹（選填）",
  "logoUrl": "https://cdn.example.com/logo.png",
  "coverImageUrl": "https://cdn.example.com/cover.png",
  "storeEmail": "contact@store.com",
  "storePhone": "02-12345678",
  "storeAddress": "台北市信義區...",
  "businessHours": "每日 10:00~22:00",
  "facebookUrl": "https://facebook.com/... （選填）",
  "instagramUrl": "https://instagram.com/... （選填）",
  "lineId": "@example （選填）"
}
```

**建立店家編輯人員（StoreEditor）**

```
POST /admin/users/store-editor
權限：ROLE_ADMIN
```

Request Body：

```json
{
  "email": "editor@example.com",
  "displayName": "編輯員A",
  "phone": "0912345678",
  "storeId": "目標店家的 UUID",
  "remark": "備註（選填）"
}
```

### 行為變化（本次修改）

| 原本 | 修改後 |
|------|--------|
| 建立帳號成功，但只在 server log 輸出密碼 | 建立帳號成功後，**自動寄送 Email** 通知初始密碼 |
| Log 包含明文密碼（資安風險） | Log 不再輸出密碼 |

### Email 內容

- 收件人：新建帳號的 email
- 內容：歡迎訊息 + 初始密碼 + 首次登入需修改密碼的提示

### 前端是否需要調整

**不需要調整 API 呼叫方式。** 但 UX 上建議：
- 建立成功後顯示提示：「帳號已建立，初始密碼已發送至 {email}」
- 不需要再顯示或傳遞明文密碼

---

## 4. 後台帳號管理 — 重設密碼自動寄送 Email

### API

```
POST /admin/users/{id}/reset-password
權限：ROLE_ADMIN
```

| 參數 | 位置 | 說明 |
|------|------|------|
| `id` | Path | 要重設密碼的帳號 UUID |

### 行為變化（本次修改）

| 原本 | 修改後 |
|------|--------|
| 重設成功後在 response body 回傳新密碼，且 log 輸出明文密碼 | 重設成功後**自動寄送 Email** 通知新密碼，log 不再輸出密碼 |

### Response Body

```json
{
  "success": true,
  "data": {
    "newPassword": "Abc12345"
  }
}
```

> ⚠️ **注意**：response 仍回傳 `newPassword` 供緊急時使用，但正常流程應讓用戶收 Email。  
> 建議前端顯示：「密碼已重設，新密碼已發送至用戶 Email」

### 前端必須配合

重設密碼後，該帳號的 `forceChangePassword` 會自動設為 `true`，用戶下次登入將被強制修改密碼（見第 2 點）。

---

## 5. 後台帳號管理 — 修改密碼 API

### API（現有，提醒配合強制修改流程使用）

```
POST /admin/users/{id}/change-password
權限：ROLE_ADMIN / ROLE_STORE_OWNER / ROLE_STORE_EDITOR（本人）
```

Request Body：

```json
{
  "currentPassword": "舊密碼",
  "newPassword": "新密碼（至少 8 字元）"
}
```

| 欄位 | 必填 | 說明 |
|------|------|------|
| `currentPassword` | ✅ | 目前的密碼（首次強制修改時填寫初始密碼） |
| `newPassword` | ✅ | 新密碼，至少 8 字元 |

### 前端必須配合

1. `forceChangePassword = true` 時，引導用戶到修改密碼頁面
2. 修改密碼成功後清除 `forceChangePassword` 狀態（服務端自動更新），重新登入即可
3. 修改密碼頁面 `id` 傳入登入用戶自己的 userId

---

## 6. 角色管理 — role code 改為唯讀

### API

```
PUT /admin/roles/{id}
```

### 行為變化（本次修改）

| 原本 | 修改後 |
|------|--------|
| 更新角色時可以修改 `code` 欄位 | `code` 欄位**完全忽略**，即使傳送也不會被更新 |

### 可更新的欄位

```json
{
  "name": "角色顯示名稱",
  "description": "角色說明",
  "menuIds": ["menu-uuid-1", "menu-uuid-2"]
}
```

> `code` 欄位可以傳也可以不傳，後端一律忽略。

### 前端必須配合

- 角色編輯頁面中，`code` 欄位的 input 改為 **disabled（唯讀）**
- 顯示說明文字：「角色代碼建立後不可修改」

---

## 7. 系統日誌 — 新增查詢 API

### 背景說明

後台原本呼叫 `/admin/system-log/type/LOGIN` 但後端該路由不存在，導致前端系統日誌頁面無法顯示資料。  
本次**新建** `AdminSystemLogController` 提供以下三個端點。

### API 清單

**方式一：依類型查詢（統一入口）**

```
GET /admin/system-log/type/{type}?limit=200
權限：ROLE_ADMIN
```

| 參數 | 位置 | 必填 | 說明 |
|------|------|------|------|
| `type` | Path | ✅ | `LOGIN`（登入紀錄）或 `ADMIN_ACTION`（後台操作紀錄） |
| `limit` | Query | ❌ | 最多返回筆數，預設 200 |

**方式二：快捷路徑（建議使用）**

```
GET /admin/system-log/login?limit=200
GET /admin/system-log/admin-action?limit=200
```

### 回應結構

**登入日誌（type=LOGIN）**

```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "userId": "admin-user-uuid",
      "username": "admin@kuji.com",
      "action": "LOGIN",
      "success": true,
      "ipAddress": "192.168.1.1",
      "userAgent": "Mozilla/5.0...",
      "createdAt": "2026-05-04T10:00:00"
    }
  ]
}
```

**後台操作日誌（type=ADMIN_ACTION）**

```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "operatorId": "admin-user-uuid",
      "operatorName": "admin@kuji.com",
      "action": "CREATE",
      "targetType": "ADMIN_USER",
      "targetId": "target-uuid",
      "createdAt": "2026-05-04T10:00:00"
    }
  ]
}
```

### 前端必須配合

- 登入日誌頁面改呼叫 `GET /admin/system-log/login`（或 `/type/LOGIN`）
- 操作日誌頁面改呼叫 `GET /admin/system-log/admin-action`（或 `/type/ADMIN_ACTION`）
- 類型只支援 `LOGIN` 與 `ADMIN_ACTION`（區分大小寫，建議統一大寫）

---

## 8. 商品管理 — 刪除功能修復

### 問題描述

前端呼叫刪除商品時，後端回傳 `400 Bad Request`。  
**根本原因**：前端送出 `PUT /{id}/status` 時沒有帶 request body，但後端欄位 `targetStatus` 有 `@NotBlank` 驗證。

### API

```
PUT /admin/lottery/{id}/status
權限：ROLE_ADMIN / ROLE_STORE_OWNER
```

Request Body（**必填**）：

```json
{
  "targetStatus": "DELETED",
  "reason": "商品已下架停售（選填）"
}
```

| 欄位 | 必填 | 說明 |
|------|------|------|
| `targetStatus` | ✅ | 目標狀態，**刪除時必須填 `"DELETED"`** |
| `reason` | ❌ | 狀態變更原因，可選 |

### 狀態流轉規則（FSM）

| 目前狀態 | 可轉換至 |
|----------|---------|
| `DRAFT` | `CONFIGURED`, `DELETED` |
| `CONFIGURED` | `ON_SHELF`, `OFF_SHELF`, `DELETED` |
| `ON_SHELF` | `OFF_SHELF`, `FORCED_OFF` |
| `OFF_SHELF` | `ON_SHELF`, `DELETED` |
| `FORCED_OFF` | `OFF_SHELF`, `DELETED` |
| `DELETED` | 終止狀態，不可再轉換 |

> ⚠️ **DELETED 為本次新增的轉換目標**，之前後端未支援此轉換，現已修復。

### 前端必須配合

1. 刪除按鈕點擊時，呼叫 `PUT /admin/lottery/{id}/status`
2. Body **必須包含** `{ "targetStatus": "DELETED" }`
3. 商品狀態為 `ON_SHELF` 時**不可直接刪除**，需先下架再刪除

---

## 9. 商品管理 — 定時上架 SQL 修復

### 問題描述

設定定時上架的商品在排程時間到達後，沒有自動上架。  
**根本原因**：排程 SQL 查詢條件寫成 `status = 'OFF_SHELF'`，但待上架商品的狀態應是 `DRAFT` 或 `CONFIGURED`。

### 修復內容

純後端 SQL 修復，排程現在會正確找到 `DRAFT` 和 `CONFIGURED` 狀態的待上架商品。

### 前端是否需要調整

**不需要。** 純後端修復，但前端需注意：
- 設定定時上架時，商品狀態應為 `DRAFT` 或 `CONFIGURED`
- 設定定時上架後商品狀態不會立即變成 `ON_SHELF`，會在排程時間到達後自動轉換

---

## 10. 儲值方案 — 活動時間區間

### API

**新增儲值方案**

```
POST /admin/recharge-plan
權限：ROLE_ADMIN
```

Request Body：

```json
{
  "name": "新手方案",
  "description": "限時優惠（選填）",
  "amount": 500,
  "goldCoins": 500,
  "bonusCoins": 150,
  "isActive": true,
  "isPromotional": true,
  "displayOrder": 1,
  "startTime": "2026-05-01T00:00:00",
  "endTime": "2026-05-31T23:59:59"
}
```

**更新儲值方案**

```
PUT /admin/recharge-plan/{id}
```

Request Body（所有欄位選填，只傳要更新的欄位）：

```json
{
  "name": "更新名稱",
  "isActive": false,
  "isPromotional": false,
  "startTime": null,
  "endTime": null
}
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| `name` | String | 方案名稱 |
| `description` | String | 說明文字（選填） |
| `amount` | Long | 儲值金額（台幣元） |
| `goldCoins` | Long | 儲值後獲得的金幣數 |
| `bonusCoins` | Long | 贈送的紅利點數（選填，預設 0） |
| `isActive` | Boolean | 是否啟用（控制是否顯示在前台） |
| `isPromotional` | Boolean | 是否為活動方案（有設時間就是活動方案） |
| `displayOrder` | Integer | 顯示排序（數字越小越前面，選填） |
| `startTime` | LocalDateTime | 活動開始時間（ISO 8601，選填） |
| `endTime` | LocalDateTime | 活動結束時間（ISO 8601，選填） |

### 回應結構（查詢用）

```json
{
  "id": "uuid",
  "name": "新手方案",
  "amount": 500,
  "goldCoins": 500,
  "bonusCoins": 150,
  "isActive": true,
  "isPromotional": true,
  "displayOrder": 1,
  "startTime": "2026-05-01T00:00:00",
  "endTime": "2026-05-31T23:59:59",
  "isInPeriod": true,
  "bonusPercentage": "贈送 30%",
  "createdAt": "...",
  "updatedAt": "..."
}
```

### 新增欄位說明

| 欄位 | 說明 |
|------|------|
| `isPromotional` | `true` = 活動方案（有設 startTime 或 endTime 之一即視為活動） |
| `isInPeriod` | `true` = 目前在活動時間內（now >= startTime AND now <= endTime）；無設定時間 = `false` |

### 行為變化（本次修改）

| 原本 | 修改後 |
|------|--------|
| 新增/更新時 `startTime`、`endTime` 不會被存入 DB | 現在正確儲存 `startTime`、`endTime` |
| 回應不包含 `isPromotional`、`isInPeriod` | 現在正確計算並回傳 |

### 前端必須配合

1. 編輯頁面新增「活動時間設定」區塊（開始/結束時間 DateTimePicker）
2. 讀取 `isInPeriod` 決定是否顯示活動標籤（例如「限時優惠」）
3. `isPromotional` 可用於列表頁顯示活動徽章
4. **停用活動方案**時，傳送 `{ "isPromotional": false, "startTime": null, "endTime": null }`

---

## 11. 選單管理 — 角色選單權限補救初始化

### 問題描述

`GET /admin/roles/{id}/detail` 回傳的 `menuPermissions` 陣列為空 `[]`，導致前端無法正確顯示角色擁有的選單清單。

### 根本原因

系統首次啟動時若 `role_menu` 表因初始化時序問題沒有寫入，之後每次重啟都不會再補，造成 `menuPermissions` 永遠為空。

### 修復內容

後端在每次啟動時，若偵測到 `role_menu` 表為空，會自動重新建立預設的角色選單權限關聯。

### 前端是否需要調整

**不需要。** 純後端修復。下次重啟後端後，`menuPermissions` 應可正常回傳。

---

## 12. 選單異動 — 移除兩個廢棄選單

### 移除項目

以下兩個後台選單已從 DB 移除（需手動執行 SQL migration）：

| 選單名稱 | 說明 |
|----------|------|
| 賞品盒管理 | 前台功能，後台不需要此選單 |
| 錢包交易記錄 | 前台功能，後台不需要此選單 |

### Migration 指令

```sql
-- sql/033-remove-unused-menus.sql（已建立，需手動執行）
DELETE FROM role_menu WHERE menu_id IN (
    SELECT id FROM menu WHERE name IN ('賞品盒管理', '錢包交易記錄')
);
DELETE FROM menu WHERE name IN ('賞品盒管理', '錢包交易記錄');
```

### 前端必須配合

1. 若後台側邊欄有手動配置這兩個選單項目，請移除
2. 套用 SQL migration 後，呼叫 `GET /admin/menus` 確認已移除

---

## 13. 訂單取消 — 賞品盒回收邏輯確認

### API

```
PUT /admin/orders/{id}/cancel
（或前端對應的取消訂單 API）
```

### 行為說明（確認非修改）

| 步驟 | 行為 |
|------|------|
| 1 | 訂單狀態改為 `CANCELLED` |
| 2 | 訂單內的所有 PrizeBox 狀態從 `IN_ORDER` 回復為 `IN_BOX` |
| 3 | PrizeBox 的 `orderId` 清除 |
| 4 | PrizeBox 的 `shippedAt` 清除 |
| 5 | 退款邏輯（待議，目前為預留 hook） |
| 6 | 發票作廢邏輯（待議，目前為預留 hook） |

> 第 2~4 步已正常運作，第 5、6 步為待規劃的擴充功能。

### 前端是否需要調整

**不需要調整 API 呼叫。** 但 UX 上建議：
- 顯示取消確認彈窗：「取消訂單後，賞品盒將回到可領取狀態。確定取消？」
- 退款/發票作廢功能尚未實作，暫不顯示相關提示

---

## 附錄 — 前端配合事項總整理

| 編號 | 功能 | 類型 | 說明 |
|------|------|------|------|
| 1 | 後台登入 | **必要** | 登入後檢查 `forceChangePassword`，`true` 時強制跳轉改密碼頁 |
| 2 | 角色編輯頁 | **必要** | `code` 欄位改為 disabled，加提示文字「建立後不可修改」 |
| 3 | 商品刪除按鈕 | **必要** | 呼叫 `PUT /admin/lottery/{id}/status`，body 必須帶 `{ "targetStatus": "DELETED" }` |
| 4 | 系統日誌頁 | **必要** | 改呼叫 `GET /admin/system-log/login` 或 `GET /admin/system-log/admin-action` |
| 5 | 儲值方案編輯頁 | **必要** | 新增活動時間 DateTimePicker；正確顯示 `isInPeriod` 標籤 |
| 6 | 建立帳號成功提示 | **建議** | 顯示「初始密碼已寄送至 {email}」，不再顯示明文密碼 |
| 7 | 重設密碼成功提示 | **建議** | 顯示「新密碼已寄送至用戶 Email」 |
| 8 | 移除廢棄選單 | **必要** | 套用 `sql/033-remove-unused-menus.sql`，並移除側邊欄對應項目 |

---

> 如有任何 API 行為疑問，可查閱 Swagger UI：`http://localhost:8080/api/swagger-ui.html`

