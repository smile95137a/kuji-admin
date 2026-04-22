# 04 - 抽獎流程

> **最複雜的核心流程！請仔細閱讀全部。**

---

## 路由決策樹（前端務必遵守）

```
商品的 playMode 是什麼？
│
├── SCRATCH_MODE（刮刮樂）
│   └── 使用票券制抽獎 → POST /lottery/draw/{id}/draw
│       ├── gameMode = SCRATCH_STORE → 進入前先查 designate 狀態
│       └── gameMode = SCRATCH_PLAYER → opener 需先 designate，其他人需等待
│
└── LOTTERY_MODE（籤位制）
    ├── category = GACHA → 使用加權隨機 → POST /lottery/random/{id}/draw
    └── 其他 category   → 使用票券制抽獎 → POST /lottery/draw/{id}/draw
```

---

## 方法 A：加權隨機抽獎（GACHA 扭蛋）

```
POST /api/lottery/random/{lotteryId}/draw?count=1
Authorization: Bearer {token}
```

| 參數 | 說明 | 必填 |
|------|------|------|
| `count` | 抽幾次（1~10） | ✅ |

> ⚠️ GACHA 使用 `synchronized` 鎖，不需要保護計時器邏輯。

### 回應
```typescript
interface DrawBatchResponse {
  playMode: 'LOTTERY_MODE';
  gameMode: 'RANDOM';
  results: DrawResult[];
  protectionEndTime: null;  // GACHA 不適用
}
```

---

## 方法 B：票券制抽獎（一番賞 / 卡牌 / 刮刮樂 / 自製賞）

```
POST /api/lottery/draw/{lotteryId}/draw
Authorization: Bearer {token}
```

```typescript
interface DrawReq {
  count: number;             // 抽幾張（1~10，不可超過剩餘籤數）
  tickets?: string[];        // 指定票券 UUID 陣列（可選；不傳 = 後端隨機選）
}
```

### 正常回應（DrawBatchResponse）

```typescript
interface DrawBatchResponse {
  playMode: 'LOTTERY_MODE' | 'SCRATCH_MODE';
  gameMode: string;
  results: DrawResult[];
  protectionEndTime: string | null; // ISO 8601，保護期結束時間
}

interface DrawResult {
  success: boolean;
  ticketId: string;          // 票券 UUID
  ticketNumber: string;      // 顯示用票號（如 "042"）
  revealedNumber: number | null;  // 刮刮樂顯示號碼
  prizeId: string;
  prizeName: string;
  prizeLevel: string;
  prizeImageUrl: string;
  isGrandPrize: boolean;
  goldSpent: number;         // 本次扣除金幣
  bonusSpent: number;        // 本次扣除紅利
  remainingGold: number;     // 剩餘金幣（每筆結果都更新）
  remainingBonus: number;    // 剩餘紅利
  prizeBoxId: string;        // 已放入獎品盒的 ID
}
```

---

## 特殊回應：SCRATCH_PLAYER 指定流程

### 情況 1：開套玩家（opener）需要指定大獎位置

```typescript
// responses[0] 是這個型別時，需要跳到指定大獎 UI
interface DesignationRequiredResponse {
  designationRequired: true;
  availableNumbers: number[];    // 可選的 revealedNumber 列表
  grandPrizes: {
    prizeId: string;
    prizeName: string;
    prizeLevel: string;
    quantity: number;            // 需要指定幾個位置
    prizeImageUrl: string;
  }[];
}
```

**判斷方式**：
```typescript
if (response.data?.designationRequired === true) {
  // 跳到指定大獎 UI
}
```

### 情況 2：非開套玩家，等待 opener 指定中

```typescript
interface DesignationPendingResponse {
  designationPending: true;
  message: string;              // "等待開套玩家指定大獎位置"
  designationDeadline: string;  // 截止時間
}
```

---

## 指定大獎位置（SCRATCH_PLAYER Opener 專用）

```
POST /api/lottery/draw/{lotteryId}/designate
Authorization: Bearer {token}
```

```typescript
interface DesignatePrizePositionsReq {
  // revealedNumber → prizeId 的對應
  designations: {
    revealedNumber: number;  // 從 availableNumbers 中選
    prizeId: string;         // 對應的大獎 ID
  }[];
}
```

> ⚠️ 指定的數量必須等於所有大獎 `quantity` 的總和。

---

## 查詢當前場次資訊

```
GET /api/lottery/draw/{lotteryId}/session
Authorization: Bearer {token}
```

### 回應
```typescript
interface SessionRes {
  lotteryId: string;
  isOpener: boolean;
  protectionEndTime: string | null;
  grandPrizesDesignated: boolean;        // 大獎是否已指定
  myTicketCount: number;
  totalDrawnCount: number;
  remainingCount: number;
  designationDeadline: string | null;    // 指定截止時間
}
```

---

## 金幣扣除規則（前端顯示用）

```
扣款順序：金幣 → 紅利補差額

範例：
  每抽 80 金幣，抽 3 次 = 240
  金幣餘額 200 → 扣 200 金幣
  紅利餘額 100 → 扣 40 紅利
  剩餘紅利 60
```

**前端建議**：
- 抽獎前顯示預估扣款（goldSpent / bonusSpent preview）
- 抽獎後從 `results[last].remainingGold` 和 `remainingBonus` 更新顯示
- 不要用 `GET /user/me` 來驗證餘額（會多一次請求）

---

## 保護計時器（Protection Timer）

適用於：`playMode = LOTTERY_MODE` 且 `category !== GACHA`

- 開套玩家（第一個開箱的人）抽完後，後端設定保護時間（`protectionEndTime`）
- **保護期間**：其他玩家可看到籤位剩餘，但**無法搶特定籤位**（仍可隨機抽）
- **保護期結束**：所有人皆可自由選籤

**前端顯示邏輯**：
```typescript
const now = new Date();
const isProtected = protectionEndTime && new Date(protectionEndTime) > now;
if (isProtected) {
  // 顯示「保護計時器」倒數
  // 如果非 opener，可提示「開套保護中，無法指定籤位」
  // 隨機抽獎仍然允許
}
```

---

## 錯誤碼

| HTTP | 說明 |
|------|------|
| `400` | 餘額不足 / 籤數不足 / count 超過上限 |
| `403` | 未登入 |
| `409` | 大獎位置尚未指定（SCRATCH_PLAYER，等待 opener） |
| `423` | 商品狀態非 ON_SHELF |

---

## 前端流程圖

```
玩家點擊「抽獎」
│
├── 未登入 → 跳轉登入頁
│
└── 已登入
    │
    ├── category = GACHA → POST /lottery/random/{id}/draw?count=N
    │   └── 顯示 DrawResult（不含 protectionEndTime）
    │
    └── 其他類型 → POST /lottery/draw/{id}/draw
        │
        ├── response.designationRequired = true（opener 需指定）
        │   └── 顯示大獎指定 UI → POST /lottery/draw/{id}/designate → 再次抽獎
        │
        ├── response.designationPending = true（等待 opener）
        │   └── 顯示「等待中」提示，並輪詢 GET /lottery/draw/{id}/session
        │
        └── 正常 DrawBatchResponse
            ├── 顯示開獎動畫
            ├── 更新金幣/紅利餘額（從 results[last] 取）
            └── 引導前往「獎品盒」查看戰果
```
