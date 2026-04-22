# 00 - 整體架構與通用規則

## 後端技術棧

| 項目 | 版本 |
|------|------|
| Spring Boot | 3.3.3 |
| Java | 21 |
| 資料庫 | MySQL 8.3 |
| 主鍵策略 | UUID |
| 文件 | Swagger（springdoc-openapi） |

---

## API 基礎設定

```
Base URL      : http://localhost:8080
Context Path  : /api
後台路由       : /api/admin/**
前台路由       : /api/（不包含 admin）
```

---

## 統一回應格式

**所有 API 回應** 由後端 AOP 自動包裝為以下格式：

```typescript
interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: {
    code: string;       // 業務錯誤碼，如 "LOTTERY_NOT_FOUND"
    message: string;    // 人類可讀錯誤訊息
  } | null;
  meta: {
    timestamp: string;  // ISO 8601
    requestId: string;  // UUID，可用於 debug
  };
}
```

**成功範例：**
```json
{
  "success": true,
  "data": { "id": "abc-123", "title": "鬼滅一番賞" },
  "error": null,
  "meta": { "timestamp": "2026-04-14T10:30:00", "requestId": "uuid-xyz" }
}
```

**失敗範例：**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "LOTTERY_NOT_FOUND",
    "message": "找不到該商品"
  },
  "meta": { "timestamp": "2026-04-14T10:30:00", "requestId": "uuid-xyz" }
}
```

---

## HTTP 狀態碼對照

| 狀態碼 | 含義 | 前端處理建議 |
|--------|------|-------------|
| 200 | 成功 | 讀取 `data` |
| 201 | 建立成功 | 讀取 `data`（新建的資源） |
| 204 | 刪除成功 | 無 body，操作完成 |
| 400 | 請求參數錯誤 | 讀取 `error.message` 顯示給使用者 |
| 401 | 未認證（Token 失效或不存在） | 導向登入頁 |
| 403 | 無權限 | 顯示「無此操作權限」 |
| 404 | 資源不存在 | 顯示「找不到資料」 |
| 409 | 衝突（如 Email 重複） | 讀取 `error.message` |
| 500 | 伺服器錯誤 | 顯示「伺服器錯誤，請稍後重試」 |

---

## 查詢 API 通用模式

後台查詢統一格式（`POST + /list`）：

```typescript
// 通用查詢請求
interface QueryReq<T> {
  condition?: T;       // 查詢條件（全部欄位可選）
  sortBy?: string;     // 排序欄位（資料庫欄位名，如 "created_at"）
  sortOrder?: 'ASC' | 'DESC';  // 排序方向
}
```

**重要原則：**
- 所有查詢條件都是**可選的**，不傳 body 或傳空 `{}` 均返回全部資料
- 後端**返回全部資料（List）**，前端負責分頁顯示
- StoreOwner/Editor 的 `storeId` 後端自動注入，無需傳

**範例：**
```javascript
// 查詢商品（StoreOwner 不需傳 storeId）
const res = await axios.post('/api/admin/lottery/list', {
  condition: { status: 'ON_SHELF' },
  sortBy: 'created_at',
  sortOrder: 'DESC'
});
const allProducts = res.data.data;  // 前端自己做分頁：allProducts.slice(0, 20)
```

---

## 日期格式

- 後端接收與回傳均使用 **ISO 8601** 格式
- 例：`"2026-04-14T10:30:00"` 或 `"2026-04-14"`
- 前端顯示時自行轉換為本地格式（Asia/Taipei）

---

## 枚舉值速查

### 商品分類（category）
```
OFFICIAL_ICHIBAN   官方一番賞
GACHA              扭蛋（加權隨機）
TRADING_CARD       集換式卡牌
CUSTOM_GACHA       自製賞
```

### 商品子分類（subCategory，僅 CUSTOM_GACHA 需要）
```
LOTTERY_MODE       自製抽籤型（玩家選籤號）
SCRATCH_MODE       自製刮刮樂型
```

### 遊戲模式（gameMode，僅 SCRATCH_MODE 需要）
```
RANDOM             全隨機
SCRATCH_STORE      店家預先指定大獎位置
SCRATCH_PLAYER     開套玩家指定大獎位置
```

### 商品狀態（status）
```
DRAFT              草稿（可刪除）
ON_SHELF           上架中
OFF_SHELF          已下架
RUNNING            抽獎進行中
COMPLETED          已完結
```

### 訂單狀態
```
PENDING_PAYMENT    待付款
PAID               已付款
PREPARING          備貨中
SHIPPED            已出貨
DELIVERED          已送達
COMPLETED          已完成
CANCELLED          已取消
REFUNDED           已退款
```

### 帳號狀態（後台帳號）
```
ACTIVE             啟用中
INACTIVE           已停用
FIRST_LOGIN        首次登入（需改密碼）
```

### 店家狀態
```
ACTIVE             啟用中
INACTIVE           已停用
```
