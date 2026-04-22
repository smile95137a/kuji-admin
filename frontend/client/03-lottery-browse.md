# 03 - 商品瀏覽

> **路由前綴**：`/lottery/browse`、`/lottery`、`/stores`  
> **均為公開端點（不需要 Token，但登入後可獲得更多資訊）**

---

## API 列表

| 方法 | 路徑 | 說明 | 需登入 |
|------|------|------|--------|
| POST | `/lottery/browse/list` | 查詢商品列表 | ❌ |
| GET | `/lottery/browse/{id}/detail` | 商品詳情（含獎品+籤位） | ❌（籤位需登入才完整） |
| GET | `/lottery/{id}` | 商品基本資訊 | ❌ |
| GET | `/stores/list` | 公開店家列表 | ❌ |

---

## 查詢商品列表

```
POST /api/lottery/browse/list
（無需 Authorization）
```

### 請求
```typescript
interface QueryReq<LotteryCondition> {
  condition?: {
    keyword?: string;       // 名稱模糊搜尋
    category?: string;      // 篩選分類
    storeId?: string;       // 篩選特定店家
    createdAtStart?: string;
    createdAtEnd?: string;
    // ⚠️ status 不需傳，後端強制只返回 ON_SHELF 的商品
  };
  sortBy?: string;          // 如 "created_at"
  sortOrder?: 'ASC' | 'DESC';
}
```

### 回應
```typescript
interface LotteryDetailRes {
  lottery: LotteryRes;       // 商品基本資訊
  prizes: LotteryPrizeRes[]; // 獎品列表（完整）
  tickets: null;             // 列表 API 不返回籤位
  session: null;             // 列表 API 不返回場次資訊
}

interface LotteryRes {
  id: string;
  storeId: string;
  storeName: string;         // 冗餘顯示用
  title: string;
  description: string;
  imageUrl: string;
  category: string;          // OFFICIAL_ICHIBAN / GACHA / TRADING_CARD / CUSTOM_GACHA
  subCategory: string | null;
  playMode: string;          // LOTTERY_MODE / SCRATCH_MODE（後端計算，前端用於路由分流）
  gameMode: string | null;   // RANDOM / SCRATCH_STORE / SCRATCH_PLAYER
  pricePerDraw: number;      // 每抽金幣數
  totalDraws: number;        // 總籤數
  remainingDraws: number;    // 剩餘籤數（即時）
  maxDrawsPerUser: number | null;
  status: string;
  grandPrizeAutoDiscount: boolean;
  discountedPricePerDraw: number | null;
  createdAt: string;
}

interface LotteryPrizeRes {
  id: string;
  name: string;
  description: string;
  imageUrl: string;
  level: string;             // A / B / C / ... / LAST / GRAND
  quantity: number;
  remaining: number;         // 剩餘數量（即時更新）
  weight: number;            // 抽取權重（GACHA 用）
  isGrandPrize: boolean;
  sortOrder: number;
}
```

---

## 商品詳情（完整版）

```
GET /api/lottery/browse/{id}/detail
（無需 Authorization，但登入後 session 資訊更完整）
```

### 回應（比列表多了 tickets 和 session）
```typescript
interface LotteryDetailRes {
  lottery: LotteryRes;
  prizes: LotteryPrizeRes[];
  tickets: LotteryTicketRes[] | null;    // 籤位列表（安全版）
  session: SessionRes | null;            // 當前場次資訊
  designatedNumbers: DesignatedNumber[]; // 大獎已指定的 revealedNumber（SCRATCH_STORE）
}

interface LotteryTicketRes {
  id: string;                   // 票券 UUID（抽獎時傳此 ID）
  ticketNumber: string;         // 顯示用票號（如 "001", "042"）
  revealedNumber: number | null;// 刮刮樂顯示號碼（僅 SCRATCH_MODE，AVAILABLE 時為 null）
  status: 'AVAILABLE' | 'DRAWN' | 'SCRATCHED';
  // ⚠️ 安全規則：AVAILABLE 的票券不包含下列欄位
  prizeId?: string;             // 僅 DRAWN/SCRATCHED
  prizeLevel?: string;          // 僅 DRAWN/SCRATCHED
  prizeName?: string;           // 僅 DRAWN/SCRATCHED
  drawnAt?: string;             // 僅 DRAWN/SCRATCHED
}

interface SessionRes {
  lotteryId: string;
  isOpener: boolean;                // 是否為開套玩家
  protectionEndTime: string | null; // 保護時間結束點（ISO 8601）
  grandPrizesDesignated: boolean;   // 大獎位置是否已指定
  myTicketCount: number;            // 我已抽的籤數
  totalDrawnCount: number;          // 全部已抽數
  remainingCount: number;           // 剩餘可抽數
  designationDeadline: string | null; // 指定大獎截止時間（SCRATCH_PLAYER）
}
```

---

## 票券安全顯示規則（重要！）

| 票券狀態 | 顯示號碼 | 顯示獎品 | 說明 |
|---------|---------|---------|------|
| `AVAILABLE` | ✅ `ticketNumber` | ❌ 不顯示 | 未抽籤位，不可洩漏獎品 |
| `DRAWN` / `SCRATCHED` | ✅ `ticketNumber` | ✅ 顯示獎品 | 已揭曉 |

---

## 店家列表（公開）

```
GET /api/stores/list?page=0&size=20
（無需 Authorization）
```

| 參數 | 說明 | 預設 |
|------|------|------|
| `page` | 頁碼（從 0 開始） | `0` |
| `size` | 每頁筆數 | `20` |

### 回應
```typescript
interface StoreRes {
  id: string;
  storeName: string;
  shortDescription: string;
  longDescription: string;
  logoUrl: string;
  coverImageUrl: string;
  businessHours: string;
  facebookUrl: string | null;
  instagramUrl: string | null;
  lineId: string | null;
  status: 'ACTIVE';  // 前台只返回 ACTIVE 的店家
}
```

---

## 前端 UI 建議

### 商品列表頁
- 依 `category` 顯示分類 Tab（全部/一番賞/扭蛋/卡牌/自製賞）
- 卡片顯示：封面圖、名稱、每抽金幣、剩餘籤數進度條
- `remainingDraws / totalDraws` 的比例顯示進度條
- `status === 'COMPLETED'` 顯示「已完售」Badge

### 商品詳情頁
- 進入時呼叫 `GET /lottery/browse/{id}/detail`
- 顯示獎品等級列表（圖片、名稱、剩餘數量）
- **一番賞/自製賞抽籤型**：顯示籤位格子（`AVAILABLE` 格子可選）
- **刮刮樂**：顯示號碼格子
- **扭蛋**：顯示獎品機率（依 weight 計算）
- 右下角「立即抽獎」按鈕（需登入才可點擊）
