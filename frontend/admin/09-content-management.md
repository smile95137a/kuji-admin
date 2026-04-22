# 09 - 內容管理（Banner / 新聞 / 跑馬燈）

> **允許角色**：ADMIN（全部操作）

---

## Banner 管理

### 路由前綴：`/admin/banner`

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/admin/banner/list` | 查詢 Banner 列表 |
| GET | `/admin/banner/{id}` | 取得 Banner 詳情 |
| POST | `/admin/banner` | 新增 Banner |
| PUT | `/admin/banner/{id}` | 更新 Banner |
| DELETE | `/admin/banner/{id}` | 刪除 Banner |
| PUT | `/admin/banner/{id}/status` | 發布/下線 Banner |
| PUT | `/admin/banner/reorder` | 重新排序 |

### Banner 資料結構
```typescript
interface BannerRes {
  id: string;
  title: string;
  imageUrl: string;
  linkType: 'STORE' | 'LOTTERY' | 'URL' | 'NONE';
  linkTarget: string | null;    // 店家 ID、商品 ID、或外部 URL
  storeId: string | null;       // 關聯的店家
  storeName: string | null;
  status: 'PUBLISHED' | 'UNPUBLISHED';
  sortOrder: number;
  startAt: string | null;       // 顯示開始時間（null = 立即）
  endAt: string | null;         // 顯示結束時間（null = 永久）
  createdAt: string;
}
```

### 新增/更新 Banner
```typescript
interface BannerCreateReq {
  title: string;                // 必填
  imageUrl: string;             // 必填（S3 URL）
  linkType: string;             // STORE / LOTTERY / URL / NONE
  linkTarget?: string;          // 依 linkType 填入對應值
  storeId?: string;             // 關聯店家（選填）
  sortOrder?: number;           // 排序（數字越小越前面）
  startAt?: string;
  endAt?: string;
}
```

### Banner 狀態
```
UNPUBLISHED  草稿（不顯示在前台）
PUBLISHED    已發布（依 startAt/endAt 判斷是否顯示）
```

---

## 新聞管理

### 路由前綴：`/admin/news`

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/admin/news/list` | 查詢新聞列表 |
| GET | `/admin/news/{id}` | 取得新聞詳情 |
| POST | `/admin/news` | 新增新聞 |
| PUT | `/admin/news/{id}` | 更新新聞 |
| DELETE | `/admin/news/{id}` | 刪除新聞 |
| PUT | `/admin/news/{id}/publish` | 發布新聞 |
| PUT | `/admin/news/{id}/unpublish` | 下線新聞 |

### 新聞資料結構
```typescript
interface NewsRes {
  id: string;
  title: string;
  summary: string;        // 摘要（列表顯示用）
  content: string;        // 全文（HTML 格式）
  coverImageUrl: string;
  status: 'DRAFT' | 'PUBLISHED';
  publishedAt: string | null;
  createdAt: string;
}
```

### 新增新聞
```typescript
interface NewsCreateReq {
  title: string;            // 必填
  summary?: string;
  content: string;          // 必填（富文本 HTML）
  coverImageUrl?: string;
  publishImmediately?: boolean;  // true = 新增後立即發布
}
```

---

## 跑馬燈管理

### 路由前綴：`/admin/marquee`

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/admin/marquee` | 取得所有跑馬燈 |
| POST | `/admin/marquee` | 新增跑馬燈 |
| PUT | `/admin/marquee/{id}` | 更新跑馬燈 |
| DELETE | `/admin/marquee/{id}` | 刪除跑馬燈 |
| PUT | `/admin/marquee/{id}/toggle` | 啟用/停用 |

### 跑馬燈資料結構
```typescript
interface MarqueeRes {
  id: string;
  content: string;          // 顯示文字（純文字，不含 HTML）
  type: 'INFO' | 'WIN' | 'SYSTEM';  // 資訊/中獎公告/系統通知
  isActive: boolean;
  sortOrder: number;
  startAt: string | null;
  endAt: string | null;
  createdAt: string;
}
```

---

## 圖片上傳 API

Banner 和新聞封面圖片需先上傳到 S3，取得 URL 後填入 `imageUrl` 欄位。

```
POST /api/admin/upload/image
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

### 請求
```
file: File（圖片檔案）
type: 'BANNER' | 'NEWS' | 'PRODUCT' | 'PRIZE'  （上傳分類，影響 S3 路徑）
```

### 回應
```typescript
interface UploadRes {
  url: string;      // S3 公開 URL，直接填入 imageUrl
  key: string;      // S3 Key（選填儲存）
}
```

---

## 前端 UI 建議

### Banner 管理
- 支援拖曳排序（`sortOrder` 即時更新）
- 圖片預覽
- `linkType` 選擇後動態切換輸入框（店家下拉/商品搜尋/URL 輸入）
- 有效期顯示（`startAt` ~ `endAt`）

### 新聞管理
- 富文本編輯器（如 Quill / TipTap）
- 草稿/已發布 Tab 分類

### 跑馬燈
- 即時預覽效果
- `type` 對應不同顏色標籤
