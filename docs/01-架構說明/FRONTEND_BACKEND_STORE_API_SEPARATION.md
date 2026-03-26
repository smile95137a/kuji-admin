# 🎯 前後台店家 API 分離完成報告

## 📋 執行摘要

### ✅ 已完成
1. 建立後台專用店家 API：`AdminStoreController`
2. 簡化前台店家 API：`StoreOptionController`
3. 明確前後台職責分離
4. 路由清晰易懂

---

## 🔧 API 架構

### ✅ 正確的架構（修改後）

```
前台 API:
  GET /api/stores/options           ← 無需登入，返回所有 ACTIVE 店家

後台 API:
  GET /api/admin/stores/options     ← 需要登入，根據角色過濾
  GET /api/admin/stores/search      ← 需要登入，支援關鍵字搜尋
```

### ❌ 之前的問題（修改前）

```
前台 API:
  GET /api/stores/options           ← 無需登入
  GET /api/stores/search            ← 無需登入

後台使用相同 API                     ← ❌ 混在一起！權限邏輯複雜！
```

---

## 📂 檔案結構

```
controller/
├── admin/                          ← 後台 API
│   ├── AdminStoreController.java  ← 後台店家 API（新增）
│   ├── AdminLotteryController.java
│   └── AdminBannerController.java
└── api/                            ← 前台 API
    ├── StoreOptionController.java ← 前台店家 API（簡化）
    ├── LotteryController.java
    └── BannerController.java
```

---

## 🎯 前台 API（StoreOptionController）

### 路由：`/api/stores/**`

### 功能定位
- ✅ 無需登入
- ✅ 固定只返回 `status=ACTIVE` 的店家
- ✅ 簡單純粹，沒有權限邏輯

### API 列表

#### 1. GET /api/stores/options

**說明：** 取得所有啟用的店家選項

**權限：** 無需登入

**返回範例：**
```json
[
  {
    "label": "玩具店",
    "value": "uuid-1",
    "description": "專賣公仔玩具"
  },
  {
    "label": "卡牌店",
    "value": "uuid-2",
    "description": "寶可夢卡牌專賣"
  }
]
```

**使用場景：**
- 前台 Banner 點擊跳轉
- 前台顯示店家資訊

---

## 🎯 後台 API（AdminStoreController）

### 路由：`/api/admin/stores/**`

### 功能定位
- ✅ 需要登入（ROLE_ADMIN、ROLE_STORE_OWNER、ROLE_STORE_EDITOR）
- ✅ 根據角色過濾店家
- ✅ 支援包含停用店家（Admin 專用）

### 權限規則

| 角色 | 看到的店家 | activeOnly 參數 |
|-----|----------|----------------|
| **ROLE_ADMIN** | 所有店家 | `true`：只看 ACTIVE<br>`false`：包含 INACTIVE |
| **ROLE_STORE_OWNER** | 自己的店家 | `true`：只看 ACTIVE<br>`false`：包含 INACTIVE |
| **ROLE_STORE_EDITOR** | 自己的店家 | `true`：只看 ACTIVE<br>`false`：包含 INACTIVE |

### API 列表

#### 1. GET /api/admin/stores/options

**說明：** 取得店家選項（後台專用，根據角色過濾）

**權限：** 需要登入（ADMIN、STORE_OWNER、STORE_EDITOR）

**參數：**
| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|------|--------|------|
| activeOnly | Boolean | ❌ | true | 是否只返回啟用的店家 |

**返回範例：**
```json
[
  {
    "label": "玩具店",
    "value": "uuid-1",
    "description": "專賣公仔玩具 (ACTIVE)"
  },
  {
    "label": "卡牌店",
    "value": "uuid-2",
    "description": "寶可夢卡牌專賣 (INACTIVE)"
  }
]
```

**使用場景：**
- Admin 新增 Banner 時選擇店家
- Admin 新增商品時選擇店家
- StoreOwner 查看自己的店家列表

#### 2. GET /api/admin/stores/search

**說明：** 搜尋店家（支援關鍵字）

**權限：** 需要登入（ADMIN、STORE_OWNER、STORE_EDITOR）

**參數：**
| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|------|--------|------|
| keyword | String | ✅ | - | 搜尋關鍵字 |
| activeOnly | Boolean | ❌ | true | 是否只返回啟用的店家 |

**返回範例：**
```json
[
  {
    "label": "玩具店",
    "value": "uuid-1",
    "description": "專賣公仔玩具"
  }
]
```

---

## 📊 使用場景對比

### 場景 1：前台瀏覽 Banner

**前端代碼：**
```javascript
// ✅ 呼叫前台 API（無需登入）
const response = await axios.get('/api/stores/options');
// 返回所有 ACTIVE 店家
```

### 場景 2：Admin 新增 Banner

**前端代碼：**
```javascript
// ✅ 呼叫後台 API（需要登入）
const response = await axios.get('/api/admin/stores/options?activeOnly=false', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
// 返回所有店家（包含 INACTIVE）
// Admin 可以選擇任何店家
```

### 場景 3：StoreOwner 新增商品

**前端代碼：**
```javascript
// ✅ 呼叫後台 API（需要登入）
const response = await axios.get('/api/admin/stores/options', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
// 只返回該 StoreOwner 擁有的店家
// 不會看到其他店家
```

### 場景 4：Admin 搜尋店家

**前端代碼：**
```javascript
// ✅ 呼叫後台搜尋 API
const response = await axios.get('/api/admin/stores/search?keyword=玩具', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
// 返回符合關鍵字的店家
```

---

## 🔍 權限驗證邏輯

### AdminStoreController（後台）

```java
@GetMapping("/options")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
public ResponseEntity<List<EnumOption>> getStoreOptions(
        @RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {
    
    boolean isAdmin = SecurityUtils.isAdmin();
    List<String> storeIds = SecurityUtils.getCurrentUserStoreIds();
    
    // 權限過濾
    if (!isAdmin && storeIds != null && !storeIds.isEmpty()) {
        // StoreOwner/Editor：只看自己的店家
        criteria.andIdIn(storeIds);
    }
    
    // 狀態過濾
    if (activeOnly) {
        criteria.andStatusEqualTo("ACTIVE");
    }
    // Admin + activeOnly=false → 返回所有店家（包含 INACTIVE）
    
    // ...
}
```

### StoreOptionController（前台）

```java
@GetMapping("/options")
// ✅ 無 @PreAuthorize，不需要登入
public ResponseEntity<List<EnumOption>> getStoreOptions() {
    
    StoreExample example = new StoreExample();
    // ✅ 固定只返回 ACTIVE 店家
    example.createCriteria().andStatusEqualTo("ACTIVE");
    
    // ...
}
```

---

## 🧪 測試驗證

### 測試 1：前台無需登入

```bash
# 不帶 Authorization header
curl -X GET http://localhost:8080/api/stores/options

# ✅ 應該返回所有 ACTIVE 店家
```

### 測試 2：Admin 看到所有店家

```bash
# Admin 登入後
curl -X GET "http://localhost:8080/api/admin/stores/options?activeOnly=false" \
  -H "Authorization: Bearer {admin_token}"

# ✅ 應該返回所有店家（包含 INACTIVE）
```

### 測試 3：StoreOwner 只看自己的店家

```bash
# StoreOwner 登入後
curl -X GET http://localhost:8080/api/admin/stores/options \
  -H "Authorization: Bearer {store_owner_token}"

# ✅ 應該只返回該 StoreOwner 擁有的店家
```

### 測試 4：後台搜尋功能

```bash
# Admin 登入後搜尋
curl -X GET "http://localhost:8080/api/admin/stores/search?keyword=玩具" \
  -H "Authorization: Bearer {admin_token}"

# ✅ 應該返回包含「玩具」的店家
```

### 測試 5：未登入無法存取後台 API

```bash
# 不帶 Authorization header
curl -X GET http://localhost:8080/api/admin/stores/options

# ✅ 應該返回 401 Unauthorized
```

---

## 📖 Swagger 文件

### 前台 API（/api/stores）
- Tag：「前台店家選項」
- 說明：提供店家列表供前台使用（無需登入）

### 後台 API（/api/admin/stores）
- Tag：「後台店家管理」
- 說明：後台店家選項 API（需登入）

---

## ✅ 優點總結

### 1. 職責分離
- ✅ 前台 API 單純（無權限邏輯）
- ✅ 後台 API 功能完整（權限過濾 + 搜尋）

### 2. 路由清晰
- ✅ `/api/stores/**` → 前台
- ✅ `/api/admin/stores/**` → 後台

### 3. 權限明確
- ✅ 前台無需登入
- ✅ 後台需要登入（`@PreAuthorize`）

### 4. 維護容易
- ✅ 前後台邏輯分開，不會互相干擾
- ✅ 測試獨立，更容易 debug

### 5. 符合 RESTful
- ✅ 資源導向設計
- ✅ 權限層級清楚

---

## 🎉 完成清單

- ✅ 建立 `AdminStoreController`（後台專用）
- ✅ 簡化 `StoreOptionController`（前台專用）
- ✅ 刪除前台搜尋功能（不需要）
- ✅ 前後台 API 完全分離
- ✅ 路由清晰：`/api/stores/**` vs `/api/admin/stores/**`
- ✅ 權限邏輯明確
- ✅ 編譯無錯誤

---

## 📝 前端更新指南

### 前台代碼
```javascript
// ✅ 正確：使用前台 API
const stores = await axios.get('/api/stores/options');
```

### 後台代碼
```javascript
// ✅ 正確：使用後台 API
const stores = await axios.get('/api/admin/stores/options', {
  headers: { 'Authorization': `Bearer ${token}` }
});

// Admin 查看所有店家（包含停用）
const allStores = await axios.get('/api/admin/stores/options?activeOnly=false', {
  headers: { 'Authorization': `Bearer ${token}` }
});

// 搜尋店家
const searchResult = await axios.get('/api/admin/stores/search?keyword=玩具', {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

---

**更新時間：** 2026-01-07  
**版本：** v3.0 - 前後台 API 完全分離版
