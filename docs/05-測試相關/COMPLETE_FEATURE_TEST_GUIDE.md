# News & Banner 模組 + Enum + S3 上傳 - 完整測試指南

## 📋 新增功能總覽

### 1. Enum 統一管理 API
- ✅ 統一的 `EnumOption` 格式：`{label: "中文", value: "英文"}`
- ✅ 單一 Controller 管理所有 Enum
- ✅ 支援個別查詢和一次取得全部

### 2. 店家選項 API
- ✅ 查詢所有店家選項（供 Banner 選擇店家）
- ✅ 支援關鍵字搜尋
- ✅ 可過濾僅啟用的店家

### 3. S3 圖片上傳
- ✅ News/Banner/Lottery/Prize 圖片上傳
- ✅ 自動生成唯一檔名
- ✅ 檔案驗證（大小、類型、副檔名）
- ✅ 支援刪除圖片

---

## 🎯 API 端點清單

### Enum API（無需登入）

| 方法 | 路徑 | 功能 | 回應格式 |
|------|------|------|----------|
| GET | `/api/enums/all` | 取得所有 Enum 選項 | `{lotteryStatus: [...], lotteryCategory: [...], ...}` |
| GET | `/api/enums/lottery-status` | 商品狀態選項 | `[{label, value}, ...]` |
| GET | `/api/enums/lottery-category` | 商品主分類選項 | `[{label, value}, ...]` |
| GET | `/api/enums/lottery-sub-category` | 商品子分類選項 | `[{label, value}, ...]` |
| GET | `/api/enums/prize-level` | 獎項等級選項 | `[{label, value}, ...]` |
| GET | `/api/enums/prize-type` | 獎品類型選項 | `[{label, value, description}, ...]` |
| GET | `/api/enums/store-status` | 店家狀態選項 | `[{label, value}, ...]` |
| GET | `/api/enums/admin-user-status` | 管理員狀態選項 | `[{label, value}, ...]` |
| GET | `/api/enums/role-code` | 角色代碼選項 | `[{label, value}, ...]` |
| GET | `/api/enums/store-user-role-type` | 店家使用者角色選項 | `[{label, value}, ...]` |
| GET | `/api/enums/news-status` | 最新消息狀態選項 | `[{label, value, description}, ...]` |
| GET | `/api/enums/banner-status` | Banner 狀態選項 | `[{label, value, description}, ...]` |

### 店家選項 API（無需登入）

| 方法 | 路徑 | 功能 | 參數 |
|------|------|------|------|
| GET | `/api/stores/options` | 取得所有店家選項 | `?activeOnly=true` |
| GET | `/api/stores/search` | 搜尋店家 | `?keyword=玩具&activeOnly=true` |

### 圖片上傳 API（需 Admin 權限）

| 方法 | 路徑 | 功能 | 參數 |
|------|------|------|------|
| POST | `/api/admin/upload/news` | 上傳 News 圖片 | `file` (multipart/form-data) |
| POST | `/api/admin/upload/banner` | 上傳 Banner 圖片 | `file` (multipart/form-data) |
| POST | `/api/admin/upload/lottery` | 上傳 Lottery 圖片 | `file` (multipart/form-data) |
| POST | `/api/admin/upload/prize` | 上傳 Prize 圖片 | `file` (multipart/form-data) |
| DELETE | `/api/admin/upload` | 刪除圖片 | `?imageUrl=https://...` |

---

## 🧪 測試步驟

### 測試 1：Enum API（無需登入）

#### 1.1 取得所有 Enum
```bash
curl http://localhost:8080/api/enums/all
```

**預期回應：**
```json
{
  "success": true,
  "data": {
    "lotteryStatus": [
      {"label": "上架中", "value": "ON_SHELF"},
      {"label": "已下架", "value": "OFF_SHELF"}
    ],
    "lotteryCategory": [
      {"label": "官方一番賞", "value": "OFFICIAL_ICHIBAN"},
      {"label": "盒玩/食玩", "value": "BOX_TOY"}
    ],
    "newsStatus": [
      {"label": "草稿", "value": "DRAFT", "description": "僅後台可見，前台不顯示"},
      {"label": "已上架", "value": "PUBLISHED", "description": "前台可見"},
      {"label": "已下架", "value": "ARCHIVED", "description": "前台不可見"}
    ],
    "bannerStatus": [
      {"label": "已上架", "value": "PUBLISHED", "description": "前台輪播顯示"},
      {"label": "未上架", "value": "UNPUBLISHED", "description": "前台不顯示"}
    ]
  }
}
```

#### 1.2 取得商品狀態選項
```bash
curl http://localhost:8080/api/enums/lottery-status
```

**預期回應：**
```json
{
  "success": true,
  "data": [
    {"label": "上架中", "value": "ON_SHELF"},
    {"label": "已下架", "value": "OFF_SHELF"}
  ]
}
```

#### 1.3 取得 News 狀態選項
```bash
curl http://localhost:8080/api/enums/news-status
```

---

### 測試 2：店家選項 API（無需登入）

#### 2.1 取得所有店家
```bash
curl http://localhost:8080/api/stores/options
```

**預期回應：**
```json
{
  "success": true,
  "data": [
    {
      "label": "官方旗艦店",
      "value": "store-uuid-123",
      "description": "KUJI 官方商店"
    },
    {
      "label": "玩具公仔專賣店",
      "value": "store-uuid-456",
      "description": "專營動漫周邊商品"
    }
  ]
}
```

#### 2.2 搜尋店家
```bash
curl "http://localhost:8080/api/stores/search?keyword=玩具"
```

**預期回應：**
```json
{
  "success": true,
  "data": [
    {
      "label": "玩具公仔專賣店",
      "value": "store-uuid-456",
      "description": "專營動漫周邊商品"
    }
  ]
}
```

#### 2.3 取得所有店家（包含停用）
```bash
curl "http://localhost:8080/api/stores/options?activeOnly=false"
```

---

### 測試 3：S3 圖片上傳（需 Admin Token）

#### 3.1 先取得 Admin Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@kuji.com",
    "password": "admin123"
  }'
```

**取得 accessToken 後設為變數：**
```bash
TOKEN="Bearer eyJhbGciOiJIUzI1NiJ9..."
```

#### 3.2 上傳 News 圖片
```bash
curl -X POST http://localhost:8080/api/admin/upload/news \
  -H "Authorization: $TOKEN" \
  -F "file=@/path/to/image.jpg"
```

**預期回應：**
```json
{
  "success": true,
  "data": {
    "imageUrl": "https://kuji-images.s3.ap-northeast-1.amazonaws.com/news/uuid-123.jpg"
  }
}
```

#### 3.3 上傳 Banner 圖片
```bash
curl -X POST http://localhost:8080/api/admin/upload/banner \
  -H "Authorization: $TOKEN" \
  -F "file=@/path/to/banner.jpg"
```

#### 3.4 刪除圖片
```bash
curl -X DELETE "http://localhost:8080/api/admin/upload?imageUrl=https://..." \
  -H "Authorization: $TOKEN"
```

---

### 測試 4：完整 News + Banner 流程

#### 4.1 查詢店家選項（選擇要綁定的店家）
```bash
curl http://localhost:8080/api/stores/options
# 記下店家 ID：store-uuid-456
```

#### 4.2 上傳 News 圖片
```bash
curl -X POST http://localhost:8080/api/admin/upload/news \
  -H "Authorization: $TOKEN" \
  -F "file=@/path/to/news.jpg"
# 記下 imageUrl
```

#### 4.3 新增 News（使用上傳的圖片）
```bash
curl -X POST http://localhost:8080/api/admin/news \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "春節活動開跑！",
    "content": "春節期間推出限定活動，參加抽獎就有機會獲得豐富獎品！",
    "imageUrl": "https://kuji-images.s3.ap-northeast-1.amazonaws.com/news/uuid-123.jpg",
    "status": "DRAFT"
  }'
```

#### 4.4 上架 News
```bash
curl -X POST http://localhost:8080/api/admin/news/{newsId}/publish \
  -H "Authorization: $TOKEN"
```

#### 4.5 前台查詢 News
```bash
curl http://localhost:8080/api/news
# 應該看到剛才上架的 News
```

#### 4.6 上傳 Banner 圖片
```bash
curl -X POST http://localhost:8080/api/admin/upload/banner \
  -H "Authorization: $TOKEN" \
  -F "file=@/path/to/banner.jpg"
# 記下 imageUrl
```

#### 4.7 新增 Banner（綁定店家）
```bash
curl -X POST http://localhost:8080/api/admin/banner \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId": "store-uuid-456",
    "title": "春節限時優惠",
    "imageUrl": "https://kuji-images.s3.ap-northeast-1.amazonaws.com/banner/uuid-456.jpg",
    "orderNum": 1,
    "status": "UNPUBLISHED"
  }'
```

#### 4.8 上架 Banner
```bash
curl -X POST http://localhost:8080/api/admin/banner/{bannerId}/publish \
  -H "Authorization: $TOKEN"
```

#### 4.9 前台查詢輪播 Banner
```bash
curl http://localhost:8080/api/banner/carousel
# 應該看到剛才上架的 Banner（且店家為 ACTIVE）
```

---

## ✅ 驗證清單

### Enum API
- [ ] 能取得所有 Enum 選項
- [ ] 每個 Enum 都有正確的 label（中文）和 value（英文）
- [ ] newsStatus 和 bannerStatus 有正確的 description

### 店家選項 API
- [ ] 能取得所有啟用的店家
- [ ] 能搜尋店家（關鍵字）
- [ ] activeOnly=false 時能看到停用的店家
- [ ] 回應格式為 `{label, value, description}`

### S3 圖片上傳
- [ ] 能上傳圖片並返回 imageUrl
- [ ] 圖片大小超過 5MB 時拒絕上傳
- [ ] 非圖片檔案時拒絕上傳
- [ ] 上傳的圖片能正常顯示
- [ ] 能刪除已上傳的圖片

### News + Banner 整合
- [ ] 新增 News 時能使用上傳的圖片
- [ ] 新增 Banner 時能選擇店家（從店家選項 API）
- [ ] 新增 Banner 時能使用上傳的圖片
- [ ] 前台只顯示 PUBLISHED 的 News
- [ ] 前台只顯示 PUBLISHED 且店家 ACTIVE 的 Banner

### 權限控管
- [ ] Enum API 無需登入
- [ ] 店家選項 API 無需登入
- [ ] 圖片上傳需要 Admin 權限
- [ ] StoreOwner 無法上傳圖片

---

## 🔧 前端整合範例

### 使用 Enum API
```javascript
// 1. 頁面載入時取得所有 Enum
const response = await axios.get('/api/enums/all');
const enums = response.data.data;

// 2. 渲染下拉選單
<select name="status">
  {enums.newsStatus.map(opt => (
    <option value={opt.value}>{opt.label}</option>
  ))}
</select>

// 3. 顯示說明文字
<Tooltip title={opt.description} />
```

### 使用店家選項 API
```javascript
// 1. Banner 新增/編輯頁面載入時取得店家選項
const response = await axios.get('/api/stores/options');
const stores = response.data.data;

// 2. 渲染店家選擇器
<select name="storeId">
  {stores.map(store => (
    <option value={store.value}>{store.label}</option>
  ))}
</select>

// 3. 搜尋店家
const searchStores = async (keyword) => {
  const response = await axios.get(`/api/stores/search?keyword=${keyword}`);
  return response.data.data;
};
```

### 使用圖片上傳 API
```javascript
// 1. 上傳圖片
const uploadImage = async (file, type) => {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await axios.post(`/api/admin/upload/${type}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      'Authorization': `Bearer ${token}`
    }
  });
  
  return response.data.data.imageUrl;
};

// 2. 新增 News 時先上傳圖片
const createNews = async (newsData, imageFile) => {
  // 先上傳圖片
  const imageUrl = await uploadImage(imageFile, 'news');
  
  // 再新增 News
  const response = await axios.post('/api/admin/news', {
    ...newsData,
    imageUrl: imageUrl
  });
  
  return response.data.data;
};

// 3. 新增 Banner 時先選店家、再上傳圖片
const createBanner = async (bannerData, imageFile) => {
  // 1. 先取得店家選項（讓使用者選擇）
  const stores = await getStoreOptions();
  
  // 2. 上傳圖片
  const imageUrl = await uploadImage(imageFile, 'banner');
  
  // 3. 新增 Banner
  const response = await axios.post('/api/admin/banner', {
    ...bannerData,
    imageUrl: imageUrl
  });
  
  return response.data.data;
};
```

---

## 📝 前端實作建議

### 1. Enum 管理
- 在應用啟動時一次取得所有 Enum（`/api/enums/all`）
- 儲存在全域狀態管理（Vuex/Redux）
- 提供 helper 函數轉換 value → label

### 2. 店家選擇器
- 使用 Autocomplete 元件
- 支援關鍵字即時搜尋
- 顯示店家名稱和簡介

### 3. 圖片上傳
- 拖放上傳 UI
- 顯示上傳進度
- 預覽上傳的圖片
- 限制檔案大小（前端也要檢查）

### 4. 表單驗證
- 圖片：必須選擇檔案且符合格式
- Banner storeId：必須選擇店家
- News/Banner title：必填

---

## 🚀 部署前檢查

### 環境變數設定
確保以下環境變數已設定：
```bash
AWS_ACCESS_KEY=your-access-key
AWS_SECRET_KEY=your-secret-key
AWS_REGION=ap-northeast-1
AWS_BUCKET_NAME=kuji-images
AWS_BASE_URL=https://kuji-images.s3.ap-northeast-1.amazonaws.com
```

### S3 Bucket 設定
1. ✅ Bucket 已建立
2. ✅ 已設定 Public Read 權限
3. ✅ 已設定 CORS 規則
4. ✅ 資料夾結構：news/, banner/, lottery/, prize/

### 測試清單
- [ ] 所有 Enum API 正常運作
- [ ] 店家選項 API 正常運作
- [ ] S3 圖片上傳正常運作
- [ ] News + Banner 完整流程正常
- [ ] 權限控管正確

---

## 📚 相關文件

- `NEWS_BANNER_IMPLEMENTATION_COMPLETE.md` - News/Banner 實作文件
- `NEWS_BANNER_API_TEST_GUIDE.md` - API 測試指南
- `FRONTEND_API_REFERENCE.json` - API 參考文件
- `KUJI_Admin_API_Tests.postman_collection.json` - Postman 測試集合

---

**準備完成！所有功能已實作，請開始測試！** 🎉
