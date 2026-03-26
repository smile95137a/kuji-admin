# 後台前端 API 完整文件

> 最後更新：2026-03-02（重大改版，請全部重寫）  
> Base URL：`http://18.179.187.129/api`  
> 所有後台 API 皆需帶 `Authorization: Bearer <adminToken>`  
> 所有 URL 皆以 `/api` 開頭，以下省略

---

## 目錄

1. [全域規則](#一全域規則)
2. [認證 Auth](#二認證-auth)
3. [商品管理 Lottery](#三商品管理-lottery)
4. [獎品管理 Prize](#四獎品管理-prize)
5. [店家管理 Store](#五店家管理-store)
6. [後台使用者管理 Admin User](#六後台使用者管理-admin-user)
7. [前台使用者管理 Frontend User](#七前台使用者管理-frontend-user)
8. [訂單管理 Order](#八訂單管理-order)
9. [錢包管理 Wallet](#九錢包管理-wallet)
10. [獎品盒 Prize Box](#十獎品盒-prize-box)
11. [消費紀錄 Consumption Record](#十一消費紀錄-consumption-record)
12. [報表 Report](#十二報表-report)
13. [新聞公告 News](#十三新聞公告-news)
14. [Banner 管理](#十四banner-管理)
15. [跑馬燈 Marquee](#十五跑馬燈-marquee)
16. [儲值方案 Recharge Plan](#十六儲值方案-recharge-plan)
17. [推薦碼 Referral Code](#十七推薦碼-referral-code)
18. [分類管理 Category](#十八分類管理-category)
19. [聯絡客服 Contact Inquiry](#十九聯絡客服-contact-inquiry)
20. [圖片上傳 Upload](#二十圖片上傳-upload)
21. [角色權限速查表](#二十一角色權限速查表)
22. [通用資料結構](#二十二通用資料結構)

---

## 一、全域規則

### 1. 統一回應格式（AOP 自動包裝）

所有 API 回應都被包成：

```jsonc
// 成功
{
  "success": true,
  "data": { ... },          // Controller 實際回傳的 ResponseEntity body
  "error": null,
  "meta": {
    "timestamp": "2026-03-02T10:00:00",
    "requestId": "uuid"
  }
}

// 失敗
{
  "success": false,
  "data": null,
  "error": {
    "code": "BUSINESS_ERROR",
    "message": "錯誤原因說明"
  },
  "meta": { ... }
}
```

> ⚠️ 實際業務資料在 `data` 裡面，前端統一從 `response.data.data` 取值。

### 2. 通用查詢 API 規則

所有 `/list` 類 API（POST）格式：

```jsonc
// Request Body（全部可選，不傳就查全部）
{
  "condition": {
    "keyword": "搜尋關鍵字",        // 可選
    "createdAtStart": "2026-01-01T00:00:00",  // 可選
    "createdAtEnd": "2026-12-31T23:59:59"     // 可選
    // ...各 API 的專屬條件
  },
  "sortBy": "created_at",          // 可選
  "sortOrder": "DESC"              // ASC | DESC，可選
}
```

> 回應皆為 `List`，**前端自己做分頁**，後端不分頁。

### 3. storeId 自動帶入規則

- **ROLE_ADMIN**：可查詢所有店家資料，也可在 body 中指定 storeId
- **ROLE_STORE_OWNER / ROLE_STORE_EDITOR**：後端從 JWT Token 自動帶入 storeId，前端**不需要也不應該**傳 storeId

### 4. 角色說明

| 角色 | 說明 |
|------|------|
| `ROLE_ADMIN` | 超級管理員，可操作全部 |
| `ROLE_STORE_OWNER` | 店家負責人，管理自己店家的資料 |
| `ROLE_STORE_EDITOR` | 店家編輯，受限操作（不能刪除、不能上下架等） |

---

## 二、認證 Auth

Base：`/admin/auth`（不需要登入）

### POST /admin/auth/login

```jsonc
// Request
{ "username": "admin@kuji.com", "password": "admin123" }

// Response data
{
  "token": "eyJ...",
  "refreshToken": "eyJ...",
  "adminUser": {
    "id": "uuid",
    "username": "admin@kuji.com",
    "nickname": "管理員",
    "roles": ["ROLE_ADMIN"],
    "isFirstLogin": false
  }
}
```

> ⚠️ `isFirstLogin: true` 時前端必須強制跳到改密碼頁面，呼叫 `/first-login/change-password`

### POST /admin/auth/first-login/change-password

```jsonc
// Request
{ "oldPassword": "初始密碼", "newPassword": "新密碼" }
// Response: 新 token（同登入 Response）
```

### POST /admin/auth/change-password

```jsonc
// Request（已登入後修改）
{ "oldPassword": "old", "newPassword": "new" }
// Response: Void
```

### POST /admin/auth/refresh-token

```jsonc
// Request
{ "refreshToken": "eyJ..." }
// Response: 新 token（同登入 Response）
```

### POST /admin/auth/logout

```jsonc
// Response: Void
```

---

## 三、商品管理 Lottery

後台有兩套 API，功能不同請注意：

| API | 用途 |
|-----|------|
| `/admin/lottery` | 商品基本 CRUD |
| `/admin/lottery-with-prizes` | 商品＋獎品一起操作（建議用這個） |

---

### `/admin/lottery` — 基本 CRUD

**Roles：ROLE_ADMIN, ROLE_STORE_OWNER, ROLE_STORE_EDITOR**

#### POST /admin/lottery/list — 查詢商品列表

```jsonc
// Request condition 可用欄位
{
  "condition": {
    "title": "鬼滅",              // 模糊搜尋
    "status": "ON_SHELF",         // ON_SHELF | OFF_SHELF | SOLD_OUT
    "category": "OFFICIAL_ICHIBAN",
    "keyword": "關鍵字"
    // storeId 非 ADMIN 角色自動帶入
  }
}

// Response: List<LotteryRes>
```

#### GET /admin/lottery/{id} — 取得單一商品

#### POST /admin/lottery — 建立商品

```jsonc
// Request
{
  "title": "鬼滅之刃一番賞",
  "category": "OFFICIAL_ICHIBAN",   // 必填
  "playMode": "LOTTERY_MODE",        // 必填：LOTTERY_MODE | SCRATCH_MODE | SCRATCH_CARD_MODE
  "gameMode": "RANDOM",              // 必填：RANDOM | SCRATCH_STORE | SCRATCH_PLAYER
  "pricePerDraw": 200,               // 必填，每抽金額（整數）
  "totalTickets": 80,                // 必填
  "protectionMinutes": 5,            // 保護時間（分鐘），0 表示無保護
  "description": "商品說明",
  "imageUrl": "https://...",
  "storeId": "uuid"                  // ADMIN 才需要傳；其他角色後端自動帶入
}
```

#### PUT /admin/lottery/{id} — 更新商品

#### DELETE /admin/lottery/{id} — 刪除商品

- **Roles：ADMIN, STORE_OWNER only**

#### POST /admin/lottery/{id}/on-shelf — 上架

- **Roles：ADMIN, STORE_OWNER only**
- 上架時後端自動生成籤位（tickets）

#### POST /admin/lottery/{id}/off-shelf — 下架

- **Roles：ADMIN, STORE_OWNER only**

#### POST /admin/lottery/copy — 複製商品

```jsonc
// Request
{ "sourceLotteryId": "uuid", "newTitle": "新商品名稱（可選）" }
// Response: 新商品 LotteryRes（status 預設 OFF_SHELF）
// 複製內容包含：全部獎品設定，不含已抽出的籤位記錄
```

---

### `/admin/lottery-with-prizes` — 商品＋獎品整合操作（建議用）

**Roles：ROLE_ADMIN, ROLE_STORE_OWNER, ROLE_STORE_EDITOR**

#### POST /admin/lottery-with-prizes — 建立商品（含獎品）

```jsonc
// Request
{
  // 所有 lottery 欄位（同上）+
  "prizes": [
    {
      "name": "SSP 大獎",
      "level": "SSP",
      "quantity": 1,
      "imageUrl": "https://...",
      "isGrandPrize": true,         // 是否為大獎（刮刮樂用）
      "isLastPrize": false,         // 是否為最後賞
      "designatedNumbers": [7, 23]  // 刮刮樂 SCRATCH_STORE 專用：指定此獎品在哪些號碼
    },
    {
      "name": "A 賞",
      "level": "A",
      "quantity": 5,
      "imageUrl": "https://...",
      "isGrandPrize": false,
      "isLastPrize": false
    }
  ]
}

// Response: LotteryWithPrizesRes
{
  "id": "uuid",
  "title": "鬼滅之刃一番賞",
  // ...所有 lottery 欄位...
  "prizes": [ ...完整獎品列表... ],
  "totalPrizeCount": 80,
  "remainingPrizeCount": 80,
  "progressPercentage": 0.0
}
```

#### GET /admin/lottery-with-prizes/{lotteryId} — 取得完整商品（含獎品）

```jsonc
// Response: LotteryWithPrizesRes（同上）
```

#### PUT /admin/lottery-with-prizes/{lotteryId} — 更新商品（含獎品）

```jsonc
// Request prizes 規則：
// - 有 id → 更新該獎品
// - 沒有 id → 新增獎品
// - 現有獎品 id 不在 list 中 → 保留（不刪除）
{
  "title": "新名稱",
  "prizes": [
    { "id": "existing-prize-uuid", "name": "修改名稱", "quantity": 2 },
    { "name": "新獎品", "level": "B", "quantity": 10 }
  ]
}
```

#### POST /admin/lottery-with-prizes/list — 查詢商品列表（含獎品統計）

---

## 四、獎品管理 Prize

Base：`/admin/lotteries`（注意：是 lotteries 不是 lottery）

**不需要特別宣告角色，走後台通用認證即可。**

#### POST /admin/lotteries/{lotteryId}/prizes — 建立單一獎品

```jsonc
// Request
{
  "name": "A 賞",
  "level": "A",
  "quantity": 5,
  "imageUrl": "https://...",
  "isGrandPrize": false,
  "isLastPrize": false,
  "designatedNumbers": []   // 刮刮樂 SCRATCH_STORE 才填
}
```

#### POST /admin/lotteries/{lotteryId}/prizes/batch — 批次建立獎品

```jsonc
// Request: List<LotteryPrizeCreateReq>
[
  { "name": "A賞", "level": "A", "quantity": 5, ... },
  { "name": "B賞", "level": "B", "quantity": 10, ... }
]
```

#### PUT /admin/lotteries/prizes/{prizeId} — 更新獎品

#### DELETE /admin/lotteries/prizes/{prizeId} — 刪除獎品

#### GET /admin/lotteries/prizes/{prizeId} — 取得單一獎品

#### GET /admin/lotteries/{lotteryId}/prizes — 取得商品所有獎品

#### GET /admin/lotteries/{lotteryId}/prizes/level/{level} — 依等級查詢獎品

#### POST /admin/lotteries/{lotteryId}/prizes/reset — 重置獎品剩餘數量

```jsonc
// 將所有獎品的 remainingQuantity 重設為 quantity（原始數量）
// Response: Void
```

#### GET /admin/lotteries/{lotteryId}/available-numbers — 取得可用號碼

```jsonc
// 刮刮樂模式使用
// Response: List<String>（可用的籤位號碼列表）
```

---

## 五、店家管理 Store

Base：`/admin/stores`

#### GET /admin/stores/options — 取得店家下拉選單

- **Roles：ADMIN, STORE_OWNER, STORE_EDITOR**
- ADMIN 可帶 `?activeOnly=false` 查看全部；其他角色只看自己的店家

```jsonc
// Response: List<EnumOption>
[
  { "value": "store-uuid", "label": "好玩店" }
]
```

#### GET /admin/stores/search — 關鍵字搜尋店家

```jsonc
// Query: ?keyword=好玩&activeOnly=true
// Response: List<EnumOption>
```

#### GET /admin/stores/all-options — 取得所有店家選單

- **ADMIN ONLY**
- 含非活躍店家

#### POST /admin/stores/list — 查詢店家列表

```jsonc
// condition 可用欄位
{
  "condition": {
    "storeName": "好玩",
    "keyword": "關鍵字"
  }
}
// Response: List<StoreRes>
```

#### GET /admin/stores/{storeId} — 取得店家詳情

#### PUT /admin/stores/{storeId} — 更新店家資訊

- **Roles：ADMIN, STORE_OWNER only**

```jsonc
// Request
{
  "storeName": "新名稱",
  "description": "說明",
  "logoUrl": "https://...",
  "contactEmail": "store@example.com",
  "contactPhone": "0912-345-678"
}
```

#### POST /admin/stores/{storeId}/activate — 啟用店家

- **ADMIN ONLY**

#### POST /admin/stores/{storeId}/deactivate — 停用店家

- **ADMIN ONLY**

---

## 六、後台使用者管理 Admin User

Base：`/admin/users`  
**全部 ADMIN ONLY（除特別標注）**

#### POST /admin/users/store-owner — 建立店家負責人

```jsonc
// Request
{
  "username": "owner@store.com",
  "nickname": "王老闆",
  "storeName": "好玩店",           // 同時建立店家
  "password": "初始密碼"           // 可選，不傳系統自動生成
}
// Response: AdminUserRes（含 generatedPassword 若系統自動生成）
```

#### POST /admin/users/store-editor — 建立店家編輯

```jsonc
// Request
{
  "username": "editor@store.com",
  "nickname": "小編",
  "storeId": "existing-store-uuid"  // 綁定到現有店家
}
```

#### GET /admin/users — 取得所有後台使用者列表

#### GET /admin/users/{id} — 取得單一後台使用者

#### GET /admin/users/by-store/{storeId} — 依店家查詢

#### POST /admin/users/{id}/activate — 啟用帳號

#### POST /admin/users/{id}/deactivate — 停用帳號

#### POST /admin/users/{id}/reset-password — 重設密碼

```jsonc
// Response
{ "newPassword": "隨機產生的新密碼" }
```

#### DELETE /admin/users/{id} — 刪除帳號（軟刪除，實為停用）

#### GET /admin/users/all-options — 使用者下拉選單

```jsonc
// Response: List<EnumOption>（含角色資訊，供下拉選擇）
```

---

## 七、前台使用者管理 Frontend User

Base：`/admin/frontend-users`

#### POST /admin/frontend-users/list — 查詢前台使用者

- **Roles：ADMIN, STORE_OWNER, STORE_EDITOR**

```jsonc
// condition 可用欄位
{
  "condition": {
    "nickname": "玩家甲",
    "status": "ACTIVE",         // ACTIVE | SUSPENDED | DEACTIVATED
    "keyword": "關鍵字"
  }
}
// Response: List<FrontendUserRes>
```

#### GET /admin/frontend-users/{id} — 取得前台使用者詳情

- **Roles：ADMIN, STORE_OWNER, STORE_EDITOR**

#### PUT /admin/frontend-users/{id} — 更新前台使用者

- **Roles：ADMIN, STORE_OWNER, STORE_EDITOR**

#### POST /admin/frontend-users/{id}/activate — 啟用

- **Roles：ADMIN, STORE_OWNER only**

#### POST /admin/frontend-users/{id}/deactivate — 停用

- **Roles：ADMIN, STORE_OWNER only**

#### POST /admin/frontend-users/{id}/suspend — 暫停

- **Roles：ADMIN, STORE_OWNER only**
- 設定狀態為 `SUSPENDED`（與 deactivate 不同）

---

## 八、訂單管理 Order

Base：`/admin/orders`  
**Roles：ADMIN, STORE_OWNER（class 級別）**

#### POST /admin/orders/list — 查詢訂單列表

```jsonc
// condition 可用欄位
{
  "condition": {
    "status": "PENDING",        // PENDING | PREPARING | SHIPPED | COMPLETED | CANCELLED
    "userId": "uuid",
    "keyword": "關鍵字"
  }
}
// Response: List<OrderRes>
```

#### GET /admin/orders/{orderId} — 取得訂單詳情

```jsonc
// Response: OrderDetailRes（比 OrderRes 更完整）
```

#### PUT /admin/orders/{orderId}/prepare — 備貨中

```jsonc
// Response: Void
// 訂單狀態：PENDING → PREPARING
```

#### PUT /admin/orders/{orderId}/ship — 出貨

```jsonc
// Request
{ "trackingNo": "物流追蹤號碼" }
// Response: Void
// 訂單狀態：PREPARING → SHIPPED
```

#### PUT /admin/orders/{orderId}/complete — 完成

```jsonc
// Response: Void
// 訂單狀態：SHIPPED → COMPLETED
```

#### PUT /admin/orders/{orderId}/cancel — 取消訂單

- **ADMIN ONLY（覆蓋取消）**

```jsonc
// Request
{ "reason": "取消原因" }
// Response: Void
```

---

## 九、錢包管理 Wallet

Base：`/admin/wallet`  
**全部 ADMIN ONLY**

#### GET /admin/wallet/{userId} — 查詢使用者錢包

```jsonc
// Response: UserWalletRes
{
  "userId": "uuid",
  "goldCoins": 1000,      // 金幣
  "bonusCoins": 500       // 紅利幣
}
```

#### POST /admin/wallet/adjust — 手動調整金幣

```jsonc
// Request
{
  "userId": "uuid",
  "coinType": "GOLD",     // GOLD | BONUS
  "amount": 100           // 正數加幣，負數扣幣
}
// Response: Void
```

#### POST /admin/wallet/transactions/list — 查詢交易紀錄

```jsonc
// condition 可用欄位
{
  "condition": {
    "userId": "uuid",
    "type": "RECHARGE",   // RECHARGE | DRAW | REFUND | ADJUST 等
    "createdAtStart": "2026-01-01T00:00:00",
    "createdAtEnd": "2026-12-31T23:59:59"
  }
}
// Response: List<WalletTransactionRes>
```

---

## 十、獎品盒 Prize Box

Base：`/admin/prize-box`  
**ADMIN ONLY**

#### GET /admin/prize-box/{userId} — 查詢使用者獎品盒

```jsonc
// Response: List<PrizeBoxItemRes>
[
  {
    "id": "uuid",
    "prizeName": "A賞 炭治郎",
    "prizeImageUrl": "https://...",
    "storeName": "好玩店",
    "status": "PENDING",   // PENDING | SHIPPED | COMPLETED
    "createdAt": "..."
  }
]
```

#### GET /admin/prize-box/summary/{userId} — 依店家分組統計

```jsonc
// Response: List<PrizeBoxSummaryRes>
[
  {
    "storeId": "uuid",
    "storeName": "好玩店",
    "totalItems": 5,
    "pendingItems": 3
  }
]
```

---

## 十一、消費紀錄 Consumption Record

Base：`/admin/consumption-records`  
**ADMIN ONLY**

> 此紀錄指**消費用途**（金幣抽獎、紅利抽獎、運費支付），不含儲值紀錄。

#### POST /admin/consumption-records/list — 查詢消費紀錄

```jsonc
// condition 可用欄位
{
  "condition": {
    "userId": "uuid",
    "type": "DRAW_GOLD",   // DRAW_GOLD | DRAW_BONUS | SHIPPING 等
    "orderNo": "訂單號",
    "keyword": "關鍵字",
    "createdAtStart": "...",
    "createdAtEnd": "..."
  }
}
// Response: List<ConsumptionRecordRes>
```

---

## 十二、報表 Report

Base：`/admin/report`  
**Roles：ADMIN, STORE_OWNER（非 ADMIN 自動篩選自己的店家）**

#### POST /admin/report/revenue — 營收報表

```jsonc
// condition
{ "storeId": "uuid", "startDate": "...", "endDate": "..." }
// Response: RevenueReportRes（總營收、各日期明細等）
```

#### POST /admin/report/referral — 推薦碼報表

#### POST /admin/report/lottery-result — 抽獎結果報表

#### POST /admin/report/recharge — 儲值報表

#### POST /admin/report/bonus — 紅利報表

---

## 十三、新聞公告 News

Base：`/admin/news`  
**全部 ADMIN ONLY**

#### POST /admin/news/list — 查詢新聞列表

```jsonc
// condition
{
  "condition": {
    "title": "標題關鍵字",
    "status": "DRAFT",      // DRAFT | PUBLISHED | ARCHIVED
    "keyword": "關鍵字",
    "createdAtStart": "...",
    "createdAtEnd": "..."
  }
}
```

#### GET /admin/news/{id} — 取得單一新聞

#### POST /admin/news — 建立新聞

```jsonc
// Request
{
  "title": "標題",
  "content": "內容（HTML 或純文字）",
  "imageUrl": "https://...",   // 可選，先用 /upload/news 上傳取得 URL
  "summary": "摘要"            // 可選
}
// 建立後預設 status: DRAFT
```

#### PUT /admin/news/{id} — 更新新聞（部分更新）

#### DELETE /admin/news/{id} — 刪除新聞（永久刪除）

#### POST /admin/news/{id}/publish — 發布

```jsonc
// 設 status → PUBLISHED，同時記錄發布時間
// Response: NewsRes
```

#### POST /admin/news/{id}/unpublish — 取消發布（封存）

```jsonc
// 設 status → ARCHIVED
```

---

## 十四、Banner 管理

Base：`/admin/banner`  
**全部 ADMIN ONLY**

#### POST /admin/banner/list — 查詢 Banner 列表

#### GET /admin/banner/{id} — 取得單一 Banner

#### POST /admin/banner — 建立 Banner

```jsonc
// Request
{
  "title": "Banner 標題",
  "imageUrl": "https://...",   // 先用 /upload/banner 上傳
  "linkUrl": "https://...",    // 點擊跳轉連結，可選
  "orderNum": 1                // 排序，數字越小越前面
}
```

#### PUT /admin/banner/{id} — 更新 Banner

#### DELETE /admin/banner/{id} — 刪除

#### POST /admin/banner/{id}/publish — 發布

#### POST /admin/banner/{id}/unpublish — 取消發布

#### PUT /admin/banner/{id}/order — 更新排序

```jsonc
// Query: PUT /admin/banner/{id}/order?orderNum=3
// Response: BannerRes
```

---

## 十五、跑馬燈 Marquee

Base：`/admin/marquee`  
**Roles：ADMIN, STORE_OWNER**

#### GET /admin/marquee — 取得所有跑馬燈

#### GET /admin/marquee/{id} — 取得單一跑馬燈

#### POST /admin/marquee — 建立跑馬燈

```jsonc
// Request
{ "content": "跑馬燈文字", "status": "ACTIVE" }
```

#### PUT /admin/marquee/{id} — 更新跑馬燈

#### DELETE /admin/marquee/{id} — 刪除

#### PATCH /admin/marquee/{id}/status — 更新狀態

```jsonc
// Query: PATCH /admin/marquee/{id}/status?status=ACTIVE
// status: ACTIVE | INACTIVE
```

#### POST /admin/marquee/broadcast — 廣播（立即推送）

---

## 十六、儲值方案 Recharge Plan

Base：`/admin/recharge-plan`  
**全部 ADMIN ONLY**

#### GET /admin/recharge-plan/list — 取得所有方案

#### POST /admin/recharge-plan/query — 查詢方案（帶條件）

```jsonc
// condition
{
  "condition": {
    "name": "方案名稱",
    "isActive": true,
    "amountMin": 100,
    "amountMax": 1000
  }
}
```

#### GET /admin/recharge-plan/{id} — 取得單一方案

#### POST /admin/recharge-plan — 建立方案

```jsonc
// Request
{
  "name": "100 元方案",
  "amount": 100,            // 儲值金額（台幣）
  "goldCoins": 1000,        // 獲得金幣數
  "bonusCoins": 100,        // 附贈紅利幣，可選
  "description": "說明"
}
// Response: planId（String）
```

#### PUT /admin/recharge-plan/{id} — 更新方案

#### DELETE /admin/recharge-plan/{id} — 刪除方案（軟刪除）

---

## 十七、推薦碼 Referral Code

Base：`/admin/referral-codes`

#### POST /admin/referral-codes — 建立推薦碼

- **Roles：ADMIN, STORE_OWNER**
- 後端自動帶入 storeId（非 ADMIN 角色）

```jsonc
// Request
{
  "code": "KUJI2026",       // 自訂碼，可選（不傳則自動產生）
  "discountType": "FIXED",  // FIXED | PERCENT
  "discountValue": 50,      // 折扣金額或百分比
  "maxUses": 100,           // 最大使用次數，null 表示無限
  "expiresAt": "2026-12-31T23:59:59"  // 可選
}
// Response: ReferralCodeRes
```

#### PUT /admin/referral-codes/{id} — 更新推薦碼

- **Roles：ADMIN, STORE_OWNER**

#### DELETE /admin/referral-codes/{id} — 刪除

- **Roles：ADMIN, STORE_OWNER**

#### GET /admin/referral-codes/{id} — 取得單一推薦碼

- **Roles：ADMIN, STORE_OWNER, STORE_EDITOR**

#### GET /admin/referral-codes — 取得所有推薦碼

- **ADMIN ONLY**

#### GET /admin/referral-codes/store/{storeId} — 依店家查詢

- **Roles：ADMIN, STORE_OWNER, STORE_EDITOR**

#### GET /admin/referral-codes/my-store — 查詢自己店家的推薦碼

- **Roles：ADMIN, STORE_OWNER, STORE_EDITOR**
- 自動帶入 storeId

#### GET /admin/referral-codes/{id}/records — 查詢推薦碼使用紀錄

#### GET /admin/referral-codes/store/{storeId}/records — 查詢店家全部推薦碼紀錄

#### GET /admin/referral-codes/validate/{code} — 驗證推薦碼是否有效

```jsonc
// Response: Boolean（true = 有效可使用）
```

---

## 十八、分類管理 Category

Base：`/admin/category`

#### POST /admin/category/categories — 查詢商品分類

- **Roles：ADMIN, STORE_OWNER, STORE_EDITOR**

#### POST /admin/category/themes — 查詢主題

#### POST /admin/category/tags — 查詢標籤

#### GET /admin/category/statistics — 分類統計

- **Roles：ADMIN, STORE_OWNER**

```jsonc
// Response: CategoryStatisticsRes
{
  "totalCategories": 10,
  "totalThemes": 5,
  "totalTags": 20,
  "totalProducts": 80
}
```

---

## 十九、聯絡客服 Contact Inquiry

Base：`/admin/contact-inquiries`  
**全部 ADMIN ONLY**

#### POST /admin/contact-inquiries/list — 查詢聯絡單列表

```jsonc
// condition
{
  "condition": {
    "company": "公司名稱",
    "status": "PENDING",          // PENDING | PROCESSING | COMPLETED | REJECTED
    "type": "聯絡類型",
    "keyword": "關鍵字",
    "createdAtStart": "...",
    "createdAtEnd": "..."
  }
}
```

#### GET /admin/contact-inquiries/{id} — 取得單一聯絡單

#### PUT /admin/contact-inquiries/{id}/status — 更新狀態

```jsonc
// Request Body（JSON Object）
{
  "status": "PROCESSING",    // PENDING | PROCESSING | COMPLETED | REJECTED
  "remark": "備註說明"
}
```

#### DELETE /admin/contact-inquiries/{id} — 刪除

---

## 二十、圖片上傳 Upload

Base：`/admin/upload`  
**全部 ADMIN ONLY**  
Content-Type：`multipart/form-data`  
單檔限制：5MB，支援 jpg / png / gif / webp

#### POST /admin/upload/news — 上傳新聞圖片

#### POST /admin/upload/banner — 上傳 Banner 圖片

#### POST /admin/upload/lottery — 上傳商品圖片

#### POST /admin/upload/prize — 上傳獎品圖片

```jsonc
// Request: multipart/form-data，欄位名稱 "file"
// Response
{ "imageUrl": "https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/news/uuid.jpg" }
```

#### DELETE /admin/upload?imageUrl={url} — 刪除圖片

```jsonc
// Query Param: imageUrl=完整 S3 URL
// Response: Void
```

---

## 二十一、角色權限速查表

| API 模組 | ADMIN | STORE_OWNER | STORE_EDITOR |
|----------|:-----:|:-----------:|:------------:|
| 認證 | ✅ | ✅ | ✅ |
| 商品查詢 | ✅ 全部 | ✅ 自己店 | ✅ 自己店 |
| 商品建立/修改 | ✅ | ✅ | ✅ |
| 商品上下架/刪除 | ✅ | ✅ | ❌ |
| 商品複製 | ✅ | ✅ | ✅ |
| 獎品管理 | ✅ | ✅ | ✅ |
| 店家查詢 | ✅ 全部 | ✅ 自己 | ✅ 自己 |
| 店家修改 | ✅ | ✅ | ❌ |
| 店家啟停用 | ✅ | ❌ | ❌ |
| 後台使用者管理 | ✅ | ❌ | ❌ |
| 前台使用者查詢 | ✅ | ✅ | ✅ |
| 前台使用者啟停用 | ✅ | ✅ | ❌ |
| 訂單管理 | ✅ | ✅ | ❌ |
| 訂單取消 | ✅ | ❌ | ❌ |
| 錢包管理 | ✅ | ❌ | ❌ |
| 獎品盒管理 | ✅ | ❌ | ❌ |
| 消費紀錄 | ✅ | ❌ | ❌ |
| 報表 | ✅ 全部 | ✅ 自己店 | ❌ |
| 新聞/Banner | ✅ | ❌ | ❌ |
| 跑馬燈 | ✅ | ✅ | ❌ |
| 儲值方案 | ✅ | ❌ | ❌ |
| 推薦碼 | ✅ | ✅ | ✅ 查 only |
| 分類管理 | ✅ | ✅ 查 | ✅ 查 |
| 聯絡客服 | ✅ | ❌ | ❌ |
| 圖片上傳 | ✅ | ❌ | ❌ |

---

## 二十二、通用資料結構

### LotteryRes（商品）

```jsonc
{
  "id": "uuid",
  "storeId": "uuid",
  "title": "商品名稱",
  "category": "OFFICIAL_ICHIBAN",  // OFFICIAL_ICHIBAN | TRADING_CARD | GACHA | CUSTOM_GACHA
  "playMode": "LOTTERY_MODE",      // LOTTERY_MODE | SCRATCH_MODE | SCRATCH_CARD_MODE
  "gameMode": "RANDOM",            // RANDOM | SCRATCH_STORE | SCRATCH_PLAYER
  "status": "OFF_SHELF",           // OFF_SHELF | ON_SHELF | SOLD_OUT
  "pricePerDraw": 200,
  "totalTickets": 80,
  "remainingTickets": 80,
  "protectionMinutes": 5,
  "imageUrl": "https://...",
  "description": "說明",
  "createdAt": "2026-03-02T10:00:00",
  "updatedAt": "2026-03-02T10:00:00"
}
```

### category 與 playMode / gameMode 組合規則

| category | 建議 playMode | 建議 gameMode |
|----------|--------------|--------------|
| OFFICIAL_ICHIBAN | LOTTERY_MODE | RANDOM |
| TRADING_CARD | LOTTERY_MODE | RANDOM |
| GACHA | SCRATCH_CARD_MODE | RANDOM |
| CUSTOM_GACHA | SCRATCH_CARD_MODE | RANDOM |
| 刮刮樂（店家指定） | SCRATCH_MODE | SCRATCH_STORE |
| 刮刮樂（玩家指定） | SCRATCH_MODE | SCRATCH_PLAYER |
| 刮刮樂（全隨機） | SCRATCH_MODE | RANDOM |

### LotteryPrizeRes（獎品）

```jsonc
{
  "id": "uuid",
  "lotteryId": "uuid",
  "name": "SSP 大獎",
  "level": "SSP",              // 自訂等級字串：SSP | SP | A | B | C | LAST | THANKS 等
  "quantity": 1,               // 總數量
  "remainingQuantity": 1,      // 剩餘數量
  "imageUrl": "https://...",
  "isGrandPrize": true,        // 是否為大獎（刮刮樂用）
  "isLastPrize": false,        // 是否為最後賞
  "designatedNumbers": [7, 23] // 刮刮樂 SCRATCH_STORE：已指定的號碼
}
```

### EnumOption（下拉選單通用格式）

```jsonc
{ "value": "uuid-or-code", "label": "顯示名稱" }
```

### QueryReq（查詢請求通用格式）

```jsonc
{
  "condition": { ... },   // 各 API 的專屬條件物件，可選
  "sortBy": "created_at",
  "sortOrder": "DESC"     // ASC | DESC
}
```

---

## 附錄：商品建立完整流程

### 一番賞 / 卡牌建立流程

```
1. POST /admin/upload/lottery   → 上傳商品圖片，取得 imageUrl
2. POST /admin/upload/prize     → 上傳各獎品圖片
3. POST /admin/lottery-with-prizes → 一次建立商品＋所有獎品
4. POST /admin/lottery/{id}/on-shelf → 上架（自動生成籤位）
```

### 刮刮樂 SCRATCH_STORE 建立流程

```
1. 上傳圖片（同上）
2. POST /admin/lottery-with-prizes
   prizes 中 isGrandPrize=true 的獎品帶入 designatedNumbers
   例：{ name: "SSP大獎", isGrandPrize: true, designatedNumbers: [7, 23] }
3. POST /admin/lottery/{id}/on-shelf → 上架
   後端自動把 designatedNumbers 的籤位標記為大獎位置
```

### 刮刮樂 SCRATCH_PLAYER 建立流程

```
1. 上傳圖片
2. POST /admin/lottery-with-prizes
   prizes 中 isGrandPrize=true 不需要 designatedNumbers（由玩家開套時指定）
3. POST /admin/lottery/{id}/on-shelf → 上架
```
