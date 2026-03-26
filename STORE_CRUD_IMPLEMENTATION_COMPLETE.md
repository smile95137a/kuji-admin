# 店家管理 CRUD API 完整實作報告

> 📅 **完成時間**：2026-02-09  
> ✅ **狀態**：已完成並可測試  
> 🎯 **實作內容**：完整的店家管理 CRUD + 原有的選項/搜尋 API

---

## 📊 API 總覽

| Method | Path | 功能 | 權限 | 狀態 |
|--------|------|------|------|------|
| GET | `/admin/stores/options` | 取得店家選項（下拉選單） | ADMIN, OWNER, EDITOR | ✅ 原有 |
| GET | `/admin/stores/search` | 搜尋店家 | ADMIN, OWNER, EDITOR | ✅ 原有 |
| **POST** | `/admin/stores/list` | **查詢店家列表** | ADMIN, OWNER, EDITOR | ✅ **新增** |
| **GET** | `/admin/stores/{id}` | **查詢店家詳情** | ADMIN, OWNER, EDITOR | ✅ **新增** |
| **PUT** | `/admin/stores/{id}` | **更新店家資訊** | ADMIN, OWNER | ✅ **新增** |
| **POST** | `/admin/stores/{id}/activate` | **啟用店家** | ADMIN | ✅ **新增** |
| **POST** | `/admin/stores/{id}/deactivate` | **停用店家** | ADMIN | ✅ **新增** |

---

## 🆕 新增的檔案

### 1. Condition 類別
- **路徑**：`com.group.admin.condition.StoreCondition`
- **用途**：店家查詢條件
- **欄位**：
  - `storeName`：店家名稱（模糊查詢）
  - `status`：狀態（ACTIVE/INACTIVE）
  - `createdAtStart`：建立時間起
  - `createdAtEnd`：建立時間迄
  - `keyword`：關鍵字（繼承自 BaseCondition）

### 2. Service 介面
- **路徑**：`com.group.admin.service.StoreService`
- **方法**：
  - `queryStores()`：查詢店家列表
  - `getStoreById()`：查詢店家詳情
  - `updateStore()`：更新店家資訊
  - `activateStore()`：啟用店家
  - `deactivateStore()`：停用店家

### 3. Service 實作
- **路徑**：`com.group.admin.service.impl.StoreServiceImpl`
- **特色**：
  - ✅ 完整的權限檢查（Admin vs Owner/Editor）
  - ✅ 動態條件查詢（所有條件可選）
  - ✅ 自動填充店家負責人資訊
  - ✅ 交易控制（@Transactional）
  - ✅ 詳細的日誌記錄

---

## 📝 API 使用範例

### 1. 查詢店家列表（POST /admin/stores/list）

```http
POST /api/admin/stores/list
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "condition": {
    "storeName": "KUJI",
    "status": "ACTIVE",
    "createdAtStart": "2026-01-01",
    "createdAtEnd": "2026-02-09"
  },
  "sortBy": "createdAt",
  "sortOrder": "DESC"
}
```

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-store-1",
      "storeName": "KUJI 台北旗艦店",
      "shortDescription": "專營一番賞與扭蛋精品",
      "logoUrl": "https://...",
      "coverImageUrl": "https://...",
      "email": "store@kuji.com",
      "phone": "02-12345678",
      "address": "台北市信義區松壽路1號",
      "businessHours": "每日 10:00~22:00",
      "status": "ACTIVE",
      "statusDisplayName": "啟用",
      "owner": {
        "id": 1,
        "displayName": "李老闆",
        "email": "owner@kuji.com"
      },
      "createdAt": "2026-01-15T10:00:00",
      "updatedAt": "2026-02-09T10:00:00"
    }
  ],
  "meta": {
    "timestamp": "2026-02-09T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

### 2. 查詢店家詳情（GET /admin/stores/{id}）

```http
GET /api/admin/stores/uuid-store-1
Authorization: Bearer {admin-token}
```

**Response**：同上（單一店家）

---

### 3. 更新店家資訊（PUT /admin/stores/{id}）

```http
PUT /api/admin/stores/uuid-store-1
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "storeName": "KUJI 台北旗艦店（更新）",
  "shortDescription": "專營一番賞、扭蛋精品、盒玩",
  "longDescription": "本店提供最新最熱門的動漫周邊...",
  "logoUrl": "https://...",
  "coverImageUrl": "https://...",
  "email": "store@kuji.com",
  "phone": "02-12345678",
  "address": "台北市信義區松壽路1號",
  "businessHours": "每日 10:00~22:00",
  "facebookUrl": "https://facebook.com/kuji",
  "instagramUrl": "https://instagram.com/kuji",
  "lineId": "@kuji",
  "remark": "後台備註"
}
```

**Response**：更新後的店家資訊

---

### 4. 啟用店家（POST /admin/stores/{id}/activate）

```http
POST /api/admin/stores/uuid-store-1/activate
Authorization: Bearer {admin-token}
```

**Response**:
```json
{
  "success": true,
  "meta": {
    "timestamp": "2026-02-09T10:30:00Z",
    "requestId": "req-xxx"
  }
}
```

---

### 5. 停用店家（POST /admin/stores/{id}/deactivate）

```http
POST /api/admin/stores/uuid-store-1/deactivate
Authorization: Bearer {admin-token}
```

**Response**：同上

---

## 🔐 權限設計

### Admin（ROLE_ADMIN）
- ✅ 查詢所有店家
- ✅ 查詢任意店家詳情
- ✅ 更新任意店家資訊
- ✅ 啟用/停用任意店家

### StoreOwner（ROLE_STORE_OWNER）
- ✅ 查詢自己的店家
- ✅ 查詢自己店家詳情
- ✅ 更新自己店家資訊
- ❌ 無法啟用/停用店家

### StoreEditor（ROLE_STORE_EDITOR）
- ✅ 查詢自己的店家
- ✅ 查詢自己店家詳情
- ❌ 無法更新店家資訊
- ❌ 無法啟用/停用店家

---

## 🧪 測試步驟

### 1. 編譯專案
```bash
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn clean package -DskipTests
```

### 2. 啟動應用
```bash
mvn spring-boot:run
```

### 3. 測試 API

#### 3.1 登入取得 Token
```bash
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"admin123"}'
```

#### 3.2 查詢店家列表
```bash
curl -X POST http://localhost:8080/api/admin/stores/list \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{}'
```

#### 3.3 查詢店家詳情
```bash
curl -X GET http://localhost:8080/api/admin/stores/{storeId} \
  -H "Authorization: Bearer {token}"
```

#### 3.4 更新店家資訊
```bash
curl -X PUT http://localhost:8080/api/admin/stores/{storeId} \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "storeName":"KUJI 測試店",
    "shortDescription":"測試描述",
    "logoUrl":"https://example.com/logo.png",
    "email":"test@kuji.com",
    "phone":"0212345678",
    "address":"台北市信義區",
    "businessHours":"10:00-22:00"
  }'
```

---

## ✅ 完成檢查清單

- [x] StoreCondition 類別創建
- [x] StoreService 介面定義
- [x] StoreServiceImpl 實作
- [x] AdminStoreController 新增 5 個 API
- [x] 權限檢查邏輯完整
- [x] 日誌記錄完整
- [x] 錯誤處理完整
- [x] Swagger 文檔完整
- [x] 編譯無錯誤

---

## 🚀 部署到 EC2

### 1. 打包
```bash
mvn clean package -DskipTests
```

### 2. 上傳到 EC2
```bash
scp target/admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/
```

### 3. 重啟應用
```bash
ssh ec2-user@18.179.187.129
sudo systemctl restart kuji-admin
```

### 4. 測試線上 API
```bash
curl -X POST http://18.179.187.129:8080/api/admin/stores/list \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{}'
```

---

## 📋 前端整合範例

```typescript
// API Service
export const storeApi = {
  // 查詢店家列表
  getStores: async (condition?: StoreCondition) => {
    const response = await axios.post('/api/admin/stores/list', {
      condition,
      sortBy: 'createdAt',
      sortOrder: 'DESC'
    });
    return response.data.data;
  },

  // 查詢店家詳情
  getStoreById: async (storeId: string) => {
    const response = await axios.get(`/api/admin/stores/${storeId}`);
    return response.data.data;
  },

  // 更新店家資訊
  updateStore: async (storeId: string, data: UpdateStoreReq) => {
    const response = await axios.put(`/api/admin/stores/${storeId}`, data);
    return response.data.data;
  },

  // 啟用店家
  activateStore: async (storeId: string) => {
    await axios.post(`/api/admin/stores/${storeId}/activate`);
  },

  // 停用店家
  deactivateStore: async (storeId: string) => {
    await axios.post(`/api/admin/stores/${storeId}/deactivate`);
  }
};

// 使用範例
const StoreListPage = () => {
  const [stores, setStores] = useState([]);

  useEffect(() => {
    const fetchStores = async () => {
      const data = await storeApi.getStores({
        status: 'ACTIVE'
      });
      setStores(data);
    };
    fetchStores();
  }, []);

  return (
    <div>
      {stores.map(store => (
        <div key={store.id}>
          <h3>{store.storeName}</h3>
          <p>{store.shortDescription}</p>
          <Badge>{store.statusDisplayName}</Badge>
        </div>
      ))}
    </div>
  );
};
```

---

## 🎉 總結

✅ **完成內容**：
- 5 個全新的店家管理 API
- 完整的權限控制
- 動態查詢條件
- 自動填充店家負責人資訊
- 詳細的 Swagger 文檔

✅ **現在可以**：
- 後台查詢店家列表（支援條件篩選、排序）
- 查詢店家詳情（包含負責人資訊）
- 更新店家資訊（店主或 Admin）
- 啟用/停用店家（只有 Admin）

✅ **前端可以**：
- 實作店家管理頁面
- 實作店家編輯功能
- 實作店家狀態管理
- 所有 API 都已準備就緒！

**最後更新**：2026-02-09  
**狀態**：✅ 完成並可測試
