# 前台會員管理功能實作完成報告

## 📋 實作摘要

已完成前台會員管理功能，提供後台所有角色查看和管理前台會員。

---

## ✅ 已修正問題

### 1. 查詢商品返回太多 NULL 值

**問題**：
```json
{
  "storeName": null,
  "categoryName": null,
  "statusName": null,
  ...
}
```

**原因**：
`convertToResNew()` 方法沒有填充關聯欄位和中文名稱。

**修正**：
```java
// ✅ 加入店家名稱查詢
if (lottery.getStoreId() != null) {
    Store store = storeMapper.selectByPrimaryKey(lottery.getStoreId());
    if (store != null) {
        res.setStoreName(store.getStoreName());
    }
}

// ✅ 加入中文名稱
res.setCategoryName(LotteryCategoryEnum.getNameByCode(lottery.getCategory()));
res.setStatusName(LotteryStatusEnum.getNameByCode(lottery.getStatus()));

// ✅ 加入當前價格
res.setCurrentPrice(lottery.getPricePerDraw());

// ✅ 加入獎項統計
res.setTotalPrizes(sumQuantityByLotteryId(lottery.getId()));
res.setRemainingPrizes(sumRemainingByLotteryId(lottery.getId()));

// ✅ 空列表而非 null
res.setMultiDrawOptions(List.of());
```

---

## 🎯 新功能：前台會員管理

### 1. API 路由

| 方法 | 路徑 | 說明 | 權限 |
|------|------|------|------|
| POST | /admin/frontend-users/list | 查詢會員列表 | 所有角色 |
| GET | /admin/frontend-users/{id} | 取得會員詳情 | 所有角色 |
| PUT | /admin/frontend-users/{id} | 更新會員資訊 | 所有角色 |
| DELETE | /admin/frontend-users/{id} | 軟刪除會員 | Admin/Owner |
| POST | /admin/frontend-users/{id}/activate | 啟用會員 | Admin/Owner |
| POST | /admin/frontend-users/{id}/deactivate | 停用會員 | Admin/Owner |
| POST | /admin/frontend-users/{id}/suspend | 暫停會員 | Admin/Owner |

### 2. 權限設計

- ✅ **所有後台角色都可以查看和編輯**（Admin、StoreOwner、StoreEditor）
- ✅ **不過濾店家**（顯示全部會員）
- ✅ **刪除/狀態變更**只有 Admin 和 StoreOwner 可以執行

### 3. 軟刪除機制

```java
// ❌ 不是真的刪除
DELETE /admin/frontend-users/{id}

// ✅ 只是標記為 DELETED
user.setStatus("DELETED");
userMapper.updateByPrimaryKey(user);

// ✅ 查詢時排除已刪除
criteria.andStatusNotEqualTo("DELETED");
```

### 4. 會員狀態

| 狀態 | 代碼 | 說明 |
|------|------|------|
| 正常 | ACTIVE | 可正常使用 |
| 停用 | INACTIVE | 無法登入 |
| 暫停 | SUSPENDED | 暫時停用 |
| 已刪除 | DELETED | 軟刪除（不顯示） |

---

## 📦 新增檔案清單

### DTO
- ✅ `FrontendUserRes.java` - 會員資訊回應
- ✅ `FrontendUserCondition.java` - 查詢條件
- ✅ `FrontendUserUpdateReq.java` - 更新請求

### Enum
- ✅ `UserStatusEnum.java` - 會員狀態
- ✅ `AuthProviderEnum.java` - 登入方式

### Service
- ✅ `FrontendUserService.java` - 介面
- ✅ `FrontendUserServiceImpl.java` - 實作

### Controller
- ✅ `AdminFrontendUserController.java` - 後台會員管理 API

---

## 🧪 測試指南

### 測試 1：查詢會員列表

```bash
# 1. 登入（任何後台帳號）
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"admin123"}'

# 2. 查詢所有會員
curl -X POST http://localhost:8080/api/admin/frontend-users/list \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{}'

# 3. 查詢特定條件
curl -X POST http://localhost:8080/api/admin/frontend-users/list \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "condition": {
      "status": "ACTIVE",
      "goldCoinsMin": 1000
    },
    "sortBy": "created_at",
    "sortOrder": "DESC"
  }'
```

**預期結果**：
```json
{
  "success": true,
  "data": [
    {
      "id": "user-uuid",
      "email": "user@example.com",
      "nickname": "測試用戶",
      "avatar": "https://...",
      "provider": "LOCAL",
      "goldCoins": 5000,
      "bonusCoins": 1000,
      "status": "ACTIVE",
      "statusName": "正常",
      "emailVerified": true,
      "lastLoginAt": "2026-01-07T12:00:00",
      "createdAt": "2026-01-01T10:00:00"
    }
  ]
}
```

### 測試 2：取得會員詳情

```bash
curl -X GET http://localhost:8080/api/admin/frontend-users/{USER_ID} \
  -H "Authorization: Bearer {TOKEN}"
```

### 測試 3：更新會員資訊

```bash
curl -X PUT http://localhost:8080/api/admin/frontend-users/{USER_ID} \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "新暱稱",
    "goldCoins": 10000,
    "status": "ACTIVE"
  }'
```

### 測試 4：軟刪除會員

```bash
curl -X DELETE http://localhost:8080/api/admin/frontend-users/{USER_ID} \
  -H "Authorization: Bearer {TOKEN}"

# 驗證：查詢列表應該看不到該會員
curl -X POST http://localhost:8080/api/admin/frontend-users/list \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{}'
```

### 測試 5：啟用/停用會員

```bash
# 停用
curl -X POST http://localhost:8080/api/admin/frontend-users/{USER_ID}/deactivate \
  -H "Authorization: Bearer {TOKEN}"

# 啟用
curl -X POST http://localhost:8080/api/admin/frontend-users/{USER_ID}/activate \
  -H "Authorization: Bearer {TOKEN}"

# 暫停
curl -X POST http://localhost:8080/api/admin/frontend-users/{USER_ID}/suspend \
  -H "Authorization: Bearer {TOKEN}"
```

---

## 🔄 與商品查詢的比較

| 功能 | 商品管理 | 會員管理 |
|------|----------|----------|
| 路由前綴 | `/admin/lottery` | `/admin/frontend-users` |
| 權限過濾 | StoreOwner 只看自己的店家 | 所有人都看全部會員 |
| 刪除方式 | 真刪除 | 軟刪除（標記） |
| 自動帶入 | storeId 自動帶入 | 無需自動帶入 |
| 查詢條件 | 店家、分類、價格 | 狀態、金幣、登入方式 |

---

## ⚠️ 注意事項

### 1. 查詢效能

目前每個會員都會查詢一次店家名稱（N+1 查詢問題），如果資料量大可能影響效能。

**優化方案**（未來）：
```java
// 批次查詢店家
Set<String> storeIds = lotteries.stream()
    .map(Lottery::getStoreId)
    .collect(Collectors.toSet());
Map<String, Store> storeMap = batchQueryStores(storeIds);

// 填充店家名稱
res.setStoreName(storeMap.get(lottery.getStoreId()).getStoreName());
```

### 2. 路徑參數正則

使用 `{id:[a-f0-9\\-]{36}}` 確保只匹配 UUID 格式，避免 `/list` 被當成 `{id}`。

### 3. 軟刪除的影響

- ✅ 查詢列表時排除 DELETED
- ✅ 詳情查詢時檢查 DELETED
- ⚠️ 相關資料（訂單、抽獎記錄）不會被刪除

---

## 📊 資料庫設計

### user 表欄位

| 欄位 | 類型 | 說明 | 備註 |
|------|------|------|------|
| id | VARCHAR(36) | UUID 主鍵 | |
| email | VARCHAR | Email | |
| nickname | VARCHAR | 暱稱 | |
| password | VARCHAR | 密碼（加密） | |
| avatar | VARCHAR | 頭像 URL | 可選 |
| provider | VARCHAR | 登入方式 | LOCAL/GOOGLE/FACEBOOK/LINE |
| provider_id | VARCHAR | 第三方 ID | OAuth 用 |
| gold_coins | BIGINT | 金幣 | 預設 0 |
| bonus_coins | BIGINT | 紅利幣 | 預設 0 |
| status | VARCHAR | 狀態 | ACTIVE/INACTIVE/SUSPENDED/DELETED |
| email_verified | TINYINT | Email 驗證 | 0/1 |
| phone_number | VARCHAR | 手機號碼 | 可選 |
| last_login_at | DATETIME | 最後登入時間 | |
| created_at | DATETIME | 註冊時間 | |
| updated_at | DATETIME | 更新時間 | |

---

## 🎯 待辦事項（未來）

- [ ] 批次查詢優化（避免 N+1）
- [ ] 會員標籤系統
- [ ] 會員等級制度
- [ ] 會員消費統計
- [ ] 會員登入記錄
- [ ] 會員操作日誌
- [ ] 批次操作（批次啟用/停用）
- [ ] 匯出會員資料（Excel）

---

## ✅ 完成狀態

- ✅ 修正商品查詢 NULL 問題
- ✅ 建立前台會員管理 DTO
- ✅ 建立會員狀態 Enum
- ✅ 實作會員管理 Service
- ✅ 建立會員管理 Controller
- ✅ 實作軟刪除機制
- ✅ 實作狀態變更功能
- ✅ 加入權限控管
- ✅ 編譯無錯誤

**建議下一步**：
1. 重啟應用程式
2. 測試商品查詢（確認 NULL 已修正）
3. 測試會員管理 API
4. 開始實作 Banner 和 News 模組

---

**實作日期**：2026-01-07  
**實作者**：AI Coding Agent  
**狀態**：✅ 完成，待測試
