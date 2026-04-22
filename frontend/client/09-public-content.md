# 09 - 公開內容

> **全部為公開端點，不需要 Authorization Token。**  
> 包含：首頁 Banner、跑馬燈、最新消息。

---

## API 列表

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/banner/list` | 主視覺 Banner 列表 |
| GET | `/marquee/list` | 跑馬燈文字列表 |
| GET | `/news/list` | 最新消息列表 |
| GET | `/news/{id}` | 最新消息詳情 |

---

## 主視覺 Banner

```
GET /api/banner/list
（無需 Authorization）
```

### 回應
```typescript
interface BannerRes {
  id: string;
  imageUrl: string;           // PC 版圖片（建議尺寸：1920x600）
  mobileImageUrl: string | null; // 行動版圖片（建議尺寸：750x400）
  linkUrl: string | null;     // 點擊連結（站內路由或外部 URL）
  linkTarget: '_self' | '_blank' | null;
  title: string | null;
  sortOrder: number;
  startAt: string | null;     // 排程上架（null = 立即）
  endAt: string | null;
}
```

> 後端自動過濾：只返回在有效期限內的 Banner，依 `sortOrder` 排序。

---

## 跑馬燈

```
GET /api/marquee/list
（無需 Authorization）
```

### 回應
```typescript
interface MarqueeRes {
  id: string;
  content: string;            // 跑馬燈文字內容
  url: string | null;         // 可點擊連結（可選）
  sortOrder: number;
  startAt: string | null;
  endAt: string | null;
}
```

> 後端自動過濾有效期限，依 `sortOrder` 排序。

---

## 最新消息列表

```
GET /api/news/list?page=0&size=10
（無需 Authorization）
```

| 參數 | 說明 | 預設 |
|------|------|------|
| `page` | 頁碼（從 0 開始） | `0` |
| `size` | 每頁筆數 | `10` |
| `category` | 分類篩選（可選） | — |

### 回應
```typescript
interface NewsRes {
  id: string;
  title: string;
  summary: string;            // 簡介（列表頁用）
  content: string;            // 完整 HTML 內容（詳情頁用）
  coverImageUrl: string | null;
  category: string | null;    // 如：公告 / 活動 / 更新
  publishedAt: string;
  isTop: boolean;             // 是否置頂
}
```

---

## 最新消息詳情

```
GET /api/news/{id}
（無需 Authorization）
```

返回完整 `NewsRes`，`content` 為 HTML（需用 `v-html` 渲染）。

---

## 前端 UI 建議

### 首頁
- **Banner 輪播**：支援行動版與桌面版不同圖片（`mobileImageUrl` 優先在手機顯示）
- **跑馬燈**：多條文字串接，速度由前端 CSS 控制
- 建議快取 5 分鐘（Banner 和跑馬燈不會頻繁更新）

### 消息頁面
- 置頂消息（`isTop=true`）固定在頂部
- 顯示封面圖、標題、簡介、發布時間
- 詳情頁用 `v-html` 渲染 `content`

### 效能建議
```typescript
// Banner 和跑馬燈通常在 App 初始化時載入一次
// 建議存入 Pinia store，避免重複請求
onMounted(async () => {
  await Promise.all([
    contentStore.fetchBanners(),
    contentStore.fetchMarquees(),
  ]);
});
```
