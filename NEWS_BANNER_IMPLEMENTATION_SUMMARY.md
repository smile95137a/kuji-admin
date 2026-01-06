# 📰 News & Banner 模組實作總結

## 實作進度

### ✅ 已完成
1. 資料庫 DDL（`DDL_news_banner.sql`）
2. News DTO（Condition, CreateReq, UpdateReq, Res）
3. Banner DTO（Condition, CreateReq, UpdateReq, Res）
4. News Entity

### ⏳ 待完成
由於檔案數量龐大（需要約 20+ 個檔案），建議分階段完成：

1. **Phase 1：資料存取層**
   - News/Banner Mapper 介面
   - News/Banner Mapper XML
   - News/Banner Example（MyBatis 動態查詢）

2. **Phase 2：業務邏輯層**
   - NewsService 介面與實作
   - BannerService 介面與實作

3. **Phase 3：控制層**
   - AdminNewsController（後台管理）
   - AdminBannerController（後台管理）
   - NewsController（前台瀏覽）
   - BannerController（前台瀏覽）

4. **Phase 4：排程任務**
   - 自動上下架排程

## 架構設計

### News 模組

#### 後台 API（/api/admin/news）
- `POST /list` - 查詢列表（支援條件查詢）
- `POST /` - 新增最新消息
- `PUT /{id}` - 更新最新消息
- `DELETE /{id}` - 刪除最新消息
- `PUT /{id}/publish` - 上架
- `PUT /{id}/unpublish` - 下架
- `GET /{id}` - 查詢詳情

#### 前台 API（/api/news）
- `GET /list` - 查詢列表（僅 PUBLISHED 狀態）
- `GET /{id}` - 查詢詳情（僅 PUBLISHED 狀態）

### Banner 模組

#### 後台 API（/api/admin/banner）
- `POST /list` - 查詢列表（支援條件查詢）
- `POST /` - 新增 Banner
- `PUT /{id}` - 更新 Banner
- `DELETE /{id}` - 刪除 Banner
- `PUT /{id}/publish` - 上架
- `PUT /{id}/unpublish` - 下架
- `PUT /{id}/order` - 更新排序
- `GET /{id}` - 查詢詳情

#### 前台 API（/api/banner）
- `GET /carousel` - 查詢輪播列表（僅 PUBLISHED 且店家啟用）

## 資料庫設計

### news 表
```sql
CREATE TABLE news (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    scheduled_at DATETIME,
    end_time DATETIME,
    created_by VARCHAR(36),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### banner 表
```sql
CREATE TABLE banner (
    id VARCHAR(36) PRIMARY KEY,
    store_id VARCHAR(36) NOT NULL,
    title VARCHAR(100) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    order_num INT DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    start_time DATETIME,
    end_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 狀態定義

### News 狀態
- `DRAFT` - 草稿（僅後台可見）
- `PUBLISHED` - 已上架（前台可見）
- `ARCHIVED` - 已下架（前台不可見）

### Banner 狀態
- `PUBLISHED` - 已上架（前台可見，需店家啟用）
- `UNPUBLISHED` - 未上架（前台不可見）

## 權限控管

### News
- **Admin**：CRUD + 上下架
- **StoreOwner/Editor**：無權限
- **前台使用者**：僅可瀏覽 PUBLISHED

### Banner
- **Admin**：CRUD + 上下架 + 排序
- **StoreOwner/Editor**：無權限
- **前台使用者**：僅可瀏覽 PUBLISHED（且店家啟用）

## 業務規則

### News
1. 預設按 `created_at` 由新至舊排序
2. 支援排程上下架
3. 內容支援長文（TEXT）
4. 封面圖片可選

### Banner
1. 必須綁定店家
2. 點擊導向店家頁面（顯示該店家的上架商品）
3. 支援手動排序（order_num）
4. 輪播顯示於前台首頁
5. 店家停用時不顯示 Banner

## 下一步

建議執行順序：
1. **執行 DDL** - 建立 news 表
2. **MyBatis Generator** - 生成 Mapper/Example
3. **實作 Service** - 業務邏輯
4. **實作 Controller** - 前後台 API
5. **測試** - 使用 Postman 測試所有端點
6. **排程任務** - 實作自動上下架

需要我繼續實作剩餘檔案嗎？或者你想先執行 DDL 並測試資料庫？
