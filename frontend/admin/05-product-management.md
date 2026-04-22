# 05 - 商品管理（Lottery）

> **路由前綴**：  
> - `/admin/lottery` — 商品基本 CRUD  
> - `/admin/lottery-with-prizes` ← **推薦使用**：商品 + 獎品整合 CRUD  
> - `/admin/lotteries/{id}/prizes` — 獎品單獨管理  
>
> **允許角色**：ADMIN / STORE_OWNER / STORE_EDITOR

---

## 商品類型完整對照（建立時傳的值）

> ⚠️ `playMode` 由後端自動推算，**無論任何商品類型都不傳**。前端只傳 `category` + `subCategory` + `gameMode`

| 商品類型 | `category` | `subCategory` | `gameMode` | playMode（後端自動） |
|---------|-----------|--------------|-----------|------------------|
| 官方一番賞 | `OFFICIAL_ICHIBAN` | `null` | `null` | `LOTTERY_MODE` |
| 扭蛋 | `GACHA` | `null` | `null` | `LOTTERY_MODE` |
| 集換式卡牌 | `TRADING_CARD` | `null` | `null` | `LOTTERY_MODE` |
| 自製賞（抽籤型） | `CUSTOM_GACHA` | `LOTTERY_MODE` | `null` | `LOTTERY_MODE` |
| 自製賞（刮刮樂-全隨機） | `CUSTOM_GACHA` | `SCRATCH_MODE` | `RANDOM` | `SCRATCH_MODE` |
| 自製賞（刮刮樂-店家指定大獎） | `CUSTOM_GACHA` | `SCRATCH_MODE` | `SCRATCH_STORE` | `SCRATCH_MODE` |
| 自製賞（刮刮樂-玩家指定大獎） | `CUSTOM_GACHA` | `SCRATCH_MODE` | `SCRATCH_PLAYER` | `SCRATCH_MODE` |

---

## 推薦 API：整合商品 + 獎品

### 新增商品（含獎品一次完成）

```
POST /api/admin/lottery-with-prizes
Authorization: Bearer {token}
```

### 請求

```typescript
interface LotteryWithPrizesCreateReq {
  lottery: {
    storeId?: string;         // StoreOwner 不需傳；Admin 必填
    title: string;            // 商品名稱（必填）
    description?: string;
    imageUrl?: string;
    category: string;         // 必填，見上表
    subCategory?: string;     // 僅 CUSTOM_GACHA 必填
    gameMode?: string;        // 僅 SCRATCH_MODE 必填
    pricePerDraw: number;     // 每抽價格（必填）
    totalDraws: number;       // 總抽數（必填）
    maxDrawsPerUser?: number; // 每人上限（null = 無限制）
    scheduledAt?: string;     // 定時上架時間（ISO 8601）
    // 大獎售完降價設定（選填）
    grandPrizeAutoDiscount?: boolean;
    discountedPricePerDraw?: number;
    // 刮刮樂-店家指定大獎時，在獎品用 isGrandPrize=true 標記
  };
  prizes: PrizeCreateItem[];
}

interface PrizeCreateItem {
  name: string;             // 獎品名稱（必填）
  description?: string;
  content?: string;         // 詳細說明（HTML）
  imageUrl?: string;
  level?: string;           // A / B / C / D / E / F / G / LAST / GRAND
  prizeNumber?: string;     // 籤號（刮刮樂模式使用）
  quantity: number;         // 數量（必填）
  weight?: number;          // 抽取權重（GACHA 用，數字越大機率越高）
  isGrandPrize?: boolean;   // 是否為大賞（SCRATCH_STORE/SCRATCH_PLAYER 時標記）
  sortOrder?: number;       // 排序
}
```

### 回應
```typescript
interface LotteryWithPrizesRes {
  id: string;
  storeId: string;
  title: string;
  description: string;
  imageUrl: string;
  category: string;
  subCategory: string;
  playMode: string;       // 後端自動推算的值（LOTTERY_MODE / SCRATCH_MODE）
  gameMode: string;
  pricePerDraw: number;
  totalDraws: number;
  remainingDraws: number;
  maxDrawsPerUser: number | null;
  status: string;
  scheduledAt: string | null;
  createdAt: string;
  prizes: PrizeRes[];
}

interface PrizeRes {
  id: string;
  lotteryId: string;
  name: string;
  description: string;
  imageUrl: string;
  level: string;
  prizeNumber: string;
  quantity: number;
  remaining: number;    // 剩餘數量（唯讀，後端維護）
  weight: number;
  isGrandPrize: boolean;
  sortOrder: number;
}
```

---

### 更新商品（含獎品一次完成）

```
PUT /api/admin/lottery-with-prizes/{id}
Authorization: Bearer {token}
```

請求結構與新增相同，但所有欄位均為可選。  
若不傳 `prizes`，則獎品保持不變；若傳了，**整個獎品列表替換**。

---

### 查詢商品（含獎品）

```
GET /api/admin/lottery-with-prizes/{id}
Authorization: Bearer {token}
```

回傳 `LotteryWithPrizesRes`，包含完整獎品列表。

---

## 查詢商品列表

```
POST /api/admin/lottery/list
Authorization: Bearer {token}
```

### 請求
```typescript
interface LotteryCondition {
  storeId?: string;       // StoreOwner 不需傳，後端自動帶入
  title?: string;         // 模糊搜尋
  status?: string;        // 篩選狀態
  category?: string;      // 篩選分類
  createdAtStart?: string;
  createdAtEnd?: string;
}
```

---

## 商品狀態管理

### 上架

```
POST /api/admin/lottery/{id}/on-shelf
Authorization: Bearer {token}（需 ADMIN 或 STORE_OWNER）
```

⚠️ **上架前提條件（後端驗證）**：
1. 商品狀態必須為 `DRAFT` 或 `OFF_SHELF`
2. 商品必須有至少一個獎品
3. 若為 `SCRATCH_STORE` 模式，大獎位置必須已指定

### 下架

```
POST /api/admin/lottery/{id}/off-shelf
Authorization: Bearer {token}（需 ADMIN 或 STORE_OWNER）
```

---

## 複製商品

```
POST /api/admin/lottery/{id}/copy
Authorization: Bearer {token}
```

後端複製商品基本資訊和獎品設定（不複製抽獎記錄），新商品狀態為 `DRAFT`。

---

## 刪除商品

```
DELETE /api/admin/lottery/{id}
Authorization: Bearer {token}（需 ADMIN 或 STORE_OWNER）
```

⚠️ 只有狀態為 `DRAFT` 的商品可以刪除，其餘狀態後端拒絕（避免刪除有歷史記錄的商品）。

---

## 獎品管理（單獨操作）

### 取得商品獎品列表

```
GET /api/admin/lotteries/{lotteryId}/prizes
Authorization: Bearer {token}
```

### 新增獎品

```
POST /api/admin/lotteries/{lotteryId}/prizes
Authorization: Bearer {token}
```

### 更新獎品

```
PUT /api/admin/lotteries/prizes/{prizeId}
Authorization: Bearer {token}
```

### 刪除獎品

```
DELETE /api/admin/lotteries/prizes/{prizeId}
Authorization: Bearer {token}
```

---

## 查詢抽獎記錄

```
GET /api/admin/lottery-draws/{lotteryId}/draws
Authorization: Bearer {token}
```

回傳該商品的所有抽獎紀錄（誰在何時抽了什麼獎）。

---

## 前端 UI 建議

### 商品新增流程
1. 選擇 `category`（影響後續必填欄位）
2. 若 `category = CUSTOM_GACHA`，顯示 `subCategory` 選項
3. 若 `subCategory = SCRATCH_MODE`，顯示 `gameMode` 選項
4. 填寫基本資訊（名稱、圖片、每抽價格、總抽數）
5. 新增獎品列表（至少一個）
   - GACHA 類需填寫 `weight`
   - SCRATCH_STORE 模式需標記哪個獎品為大賞（`isGrandPrize=true`）
6. 確認後呼叫 `POST /admin/lottery-with-prizes`（一次完成）

### 刮刮樂（SCRATCH_STORE）大賞指定
- 前端在獎品列表中讓使用者勾選「此為大賞」（`isGrandPrize=true`）
- 大賞的 `revealedNumber` 由後端在生成籤位時自動指定
- ⚠️ Admin/StoreOwner 在後台指定大賞獎品，不是指定號碼

### 商品狀態流轉（狀態機）
```
DRAFT → ON_SHELF（上架）
ON_SHELF → OFF_SHELF（下架）
ON_SHELF → RUNNING（有玩家開始抽，狀態自動轉，前端不需操作）
RUNNING → COMPLETED（全部抽完，狀態自動轉）
OFF_SHELF → ON_SHELF（重新上架）
DRAFT → 刪除（只有草稿可以刪）
```
