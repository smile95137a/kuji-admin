# 刮刮樂雙號碼機制 — 前端實作規範

> **問題背景**：此問題已發生多次，前後端反覆推託，本文件一次釐清責任歸屬與正確實作方式，後續不再重複討論。

---

## 核心觀念：兩個號碼，兩種用途，絕對不可混用

每張刮刮樂票券在資料庫中有兩個號碼：

| 欄位 | 型別 | 意義 | 前端用途 |
|------|------|------|---------|
| `ticketNumber` | Integer | **物理格子位置**（1\~N），代表玩家點擊的是第幾格 | ✅ 用來決定結果顯示在哪個格子 |
| `revealedNumber` | Integer | **刮開後顯示的亂數**，是為了 SCRATCH\_PLAYER 大獎指定機制設計的 | ❌ 絕對不能用來決定格子位置 |

---

## 抽獎 API 回傳格式

```
POST /api/lottery/{lotteryId}/draw
Body: { "count": 1, "tickets": ["<ticketUUID>"] }
```

回傳範例：

```json
{
  "playMode": "SCRATCH_MODE",
  "gameMode": "SCRATCH_STORE",
  "draws": [
    {
      "ticketId": "0e3eec39-5793-4c3b-af6e-3618961bf0a1",
      "ticketNumber": 3,
      "revealedNumber": 47,
      "prizeName": "謝謝惠顧",
      "prizeLevel": null,
      "prizeImageUrl": null
    }
  ]
}
```

---

## 前端正確實作

### 場景：玩家點擊第 3 格，回傳後要把結果更新到第 3 格

```javascript
// ✅ 正確：用 ticketNumber 決定格子位置
const result = response.data.draws[0];
const gridIndex = result.ticketNumber - 1; // ticketNumber 從 1 開始
grid[gridIndex].reveal(result.prizeName, result.prizeImageUrl);
```

```javascript
// ✅ 或者在送出 request 前，先記錄「UUID → 格子 index」的 mapping
const ticketIndexMap = {};
selectedTickets.forEach((uuid, index) => {
  ticketIndexMap[uuid] = index;
});

// 拿到結果後對應回格子
response.data.draws.forEach(result => {
  const index = ticketIndexMap[result.ticketId];
  grid[index].reveal(result.prizeName, result.prizeImageUrl);
});
```

```javascript
// ❌ 錯誤（這是一直出 bug 的寫法）
// 用 revealedNumber 當格子位置 → 結果會跳到隨機位置
const gridIndex = result.revealedNumber - 1; // ← 嚴禁！
grid[gridIndex].reveal(...);
```

---

## `revealedNumber` 唯一合法用途

`revealedNumber` 只在 **SCRATCH\_PLAYER** 模式下使用，流程如下：

1. 開套者呼叫 `/draw` → 後端回傳 `designationRequired: true` + `availableNumbers` 列表
2. 開套者選擇哪些 `revealedNumber` 要放大獎，呼叫 `/designate`
3. 指定完成後，所有玩家才能開始刮

> **在 SCRATCH\_STORE 與 RANDOM 模式下，前端完全不需要讀 `revealedNumber`。**

---

## 籤位列表 API（顯示格子用）

```
GET /api/lottery/{lotteryId}/tickets
```

回傳的每個格子：

```json
{
  "id": "<ticketUUID>",
  "ticketNumber": 3,
  "status": "AVAILABLE",
  "revealedNumber": null
}
```

> `revealedNumber` 在 AVAILABLE 狀態下後端會**刻意隱藏**（回傳 null），前端不會拿到此值，這是安全設計，不是 bug。

**前端根據 `ticketNumber` 排列格子，根據 `id`（UUID）送出抽獎請求。**

---

## 責任邊界（最終確認）

| 項目 | 負責方 | 說明 |
|------|--------|------|
| `revealedNumber` 的產生與儲存 | 後端 | 建立商品時隨機產生，寫入 DB |
| `revealedNumber` 對 AVAILABLE 票券的隱藏 | 後端 | 已確認隱藏，前端拿不到 |
| 格子位置的對應邏輯 | **前端** | 必須用 `ticketNumber`，不得用 `revealedNumber` |
| 抽獎請求傳正確的 UUID | **前端** | `tickets` 陣列要傳玩家點擊格子的 `id` |
| 將回傳結果顯示到正確格子 | **前端** | 依 `ticketNumber` 或 UUID mapping 對應 |

---

> 後端已確認實作正確，此問題為前端使用錯誤欄位導致，請依照本文件修正。
