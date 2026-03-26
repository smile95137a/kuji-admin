# 後台店家管理 API - 實際可用的 API

## ❌ 錯誤的 API（文檔中有但實際不存在）

```http
POST /api/admin/stores/list  ❌ 不存在！
GET /api/admin/stores        ❌ 不存在！
GET /api/admin/stores/{id}   ❌ 不存在！
```

## ✅ 實際存在的店家 API（只有 2 個）

### 1. 取得店家選項（下拉選單用）

```http
GET /api/admin/stores/options?activeOnly=true
Authorization: Bearer {token}
```

**權限**：`ROLE_ADMIN`, `ROLE_STORE_OWNER`, `ROLE_STORE_EDITOR`

**Query Parameters**：
| 參數 | 類型 | 必填 | 默認值 | 說明 |
|------|------|------|--------|------|
| `activeOnly` | Boolean | ❌ | `true` | 是否只返回啟用的店家 |

**權限邏輯**：
- **ROLE_ADMIN**：返回所有店家
- **ROLE_STORE_OWNER**：只返回自己的店家
- **ROLE_STORE_EDITOR**：只返回自己的店家

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "label": "KUJI 台北旗艦店",
      "value": "uuid-store-1",
      "description": "專營一番賞與扭蛋精品 (ACTIVE)"
    },
    {
      "label": "KUJI 台中分店",
      "value": "uuid-store-2",
      "description": "動漫周邊專賣店 (ACTIVE)"
    }
  ],
  "meta": {
    "timestamp": "2026-02-09T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

**前端使用範例**：
```javascript
// 取得店家選項用於下拉選單
const getStoreOptions = async () => {
  const response = await axios.get('/api/admin/stores/options', {
    params: { activeOnly: true },
    headers: { 'Authorization': `Bearer ${adminToken}` }
  });
  
  const options = response.data.data;
  // options = [{ label: "店家名", value: "uuid", description: "..." }]
  
  return options;
};
```

---

### 2. 搜尋店家（關鍵字搜尋）

```http
GET /api/admin/stores/search?keyword=玩具&activeOnly=true
Authorization: Bearer {token}
```

**權限**：`ROLE_ADMIN`, `ROLE_STORE_OWNER`, `ROLE_STORE_EDITOR`

**Query Parameters**：
| 參數 | 類型 | 必填 | 默認值 | 說明 |
|------|------|------|--------|------|
| `keyword` | String | ✅ | - | 搜尋關鍵字（店家名稱） |
| `activeOnly` | Boolean | ❌ | `true` | 是否只返回啟用的店家 |

**Response** (200 OK):
```json
{
  "success": true,
  "data": [
    {
      "label": "玩具總動員店",
      "value": "uuid-store-3",
      "description": "各種玩具周邊"
    }
  ],
  "meta": {
    "timestamp": "2026-02-09T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

**前端使用範例**：
```javascript
// 搜尋店家
const searchStores = async (keyword) => {
  const response = await axios.get('/api/admin/stores/search', {
    params: { 
      keyword: keyword,
      activeOnly: true 
    },
    headers: { 'Authorization': `Bearer ${adminToken}` }
  });
  
  return response.data.data;
};
```

---

## 🔍 為什麼沒有更多店家 API？

根據實際代碼，**後台不需要完整的店家 CRUD API**，因為：

1. ✅ **店家資料在新增 StoreOwner 時自動創建**
   - API: `POST /api/admin/users/store-owner`
   - 此 API 會同時創建店家和店主帳號

2. ✅ **店家列表透過選項 API 獲取**
   - API: `GET /api/admin/stores/options`
   - 已足夠用於下拉選單、篩選等場景

3. ✅ **店家詳情可透過 Store Entity 直接查詢**
   - 在商品管理、訂單管理中會帶出店家資訊

---

## 📋 完整的店家相關 API 清單

| API | Method | 路徑 | 用途 |
|-----|--------|------|------|
| 新增店家負責人 | POST | `/admin/users/store-owner` | 創建店家+店主 |
| 取得店家選項 | GET | `/admin/stores/options` | 下拉選單 |
| 搜尋店家 | GET | `/admin/stores/search` | 關鍵字搜尋 |

---

## ⚠️ 如果需要完整的店家管理 API

如果前端確實需要以下功能：
- 查詢店家列表（帶分頁、篩選）
- 查詢店家詳情
- 更新店家資訊
- 停用/啟用店家

**請告訴我，我會立即新增這些 API！**

建議新增的 API：
```http
POST /api/admin/stores/list      # 查詢店家列表
GET /api/admin/stores/{id}       # 查詢店家詳情
PUT /api/admin/stores/{id}       # 更新店家資訊
PUT /api/admin/stores/{id}/status # 啟用/停用店家
```

---

**最後更新**：2026-02-09  
**狀態**：✅ 100% 準確（基於實際代碼）  
**實際可用 API 數量**：2 個
