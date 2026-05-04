# KUJI Admin 後台前端規範文件

> **最後同步時間**：2026-04-14  
> **後端版本**：Spring Boot 3.3.3 / Java 21  
> **Context Path**：`/api`（所有 API 以 `http://localhost:8080/api` 開頭）

---

## 📂 文件導覽

| 文件 | 說明 |
|------|------|
| [00-architecture.md](./00-architecture.md) | 整體架構、API 格式、錯誤碼、通用規則 |
| [01-auth.md](./01-auth.md) | 後台登入、JWT Token、首次改密流程 |
| [02-roles-and-permissions.md](./02-roles-and-permissions.md) | RBAC 角色、資料隔離、選單權限 |
| [03-store-management.md](./03-store-management.md) | 店家 CRUD、啟用/停用、連動邏輯 |
| [04-account-management.md](./04-account-management.md) | StoreOwner/StoreEditor 帳號管理 |
| [05-product-management.md](./05-product-management.md) | 商品(Lottery)管理、商品類型、獎品設定 |
| [06-order-management.md](./06-order-management.md) | 訂單查詢、狀態流轉、出貨流程 |
| [07-frontend-user-management.md](./07-frontend-user-management.md) | 前台玩家帳號管理 |
| [08-report-analytics.md](./08-report-analytics.md) | 報表查詢（營業額/推薦碼/開獎結果） |
| [09-content-management.md](./09-content-management.md) | Banner、新聞、跑馬燈 |
| [10-system-config.md](./10-system-config.md) | 系統參數管理 |
| [11-shipping-management.md](./11-shipping-management.md) | 出貨方式管理 |
| [12-system-log-and-updates.md](./12-system-log-and-updates.md) | 🆕 系統日誌 API + 後端修正通知 + 報表補全（5個）+ 疑問清單 |
| [PROMPT-FOR-FRONTEND.md](./PROMPT-FOR-FRONTEND.md) | 給前端開發者的溝通 Prompt |

---

## ⚡ 快速開始

### 1. 取得 Swagger UI
後端啟動後，可直接瀏覽所有 API：
```
http://localhost:8080/api/swagger-ui.html
```

### 2. 後台路由前綴
所有後台 API 路徑以 `/admin/` 開頭：
```
POST /api/admin/auth/login
GET  /api/admin/stores/options
POST /api/admin/lottery-with-prizes
```

### 3. 認證方式
每個請求 Header 加入：
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

---

## 🔑 核心設計原則

### StoreID 自動注入
**StoreOwner / StoreEditor 新增或查詢資料時，不需傳 `storeId`**，後端從 JWT 自動解析。  
只有 Admin 操作跨店家資料時才需要傳 `storeId`。

### 角色資料隔離
| 角色 | 可見範圍 |
|------|---------|
| ADMIN | 所有店家全部資料 |
| STORE_OWNER | 只能看自己店家的資料 |
| STORE_EDITOR | 只能看自己店家的資料（部分功能受限） |

### 統一回應格式
```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "meta": {
    "timestamp": "2026-04-14T12:00:00",
    "requestId": "uuid"
  }
}
```

### 查詢 API 模式
後台查詢 API 統一使用 `POST + /list`，`body` 傳 `QueryReq<Condition>`：
```json
{
  "condition": { "keyword": "鬼滅" },
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```
**後端返回全部資料（List），前端自行做分頁**。

---

## 🚫 重要禁則（前端必讀）

1. ❌ **不要**在前端傳 `storeId`（StoreOwner/Editor 場景）
2. ❌ **不要**傳 `playMode`（無論什麼商品類型都不傳，後端自動推算）
   - 一番賞/扭蛋/卡牌：只傳 `category`
   - 自製賞-抽籤：傳 `category` + `subCategory='LOTTERY_MODE'`
   - 自製賞-刮刮樂：傳 `category` + `subCategory='SCRATCH_MODE'` + `gameMode`
3. ❌ **不要**用 `ticketNumber` 指定大獎（應傳 `revealedNumber`）
4. ✅ **必須**儲存 Access Token + Refresh Token，過期自動 Refresh
5. ✅ **必須**依角色控制選單顯示（依 API 回傳的 `menus` 樹）
