# KUJI 後台整頓計畫書

> **最後更新**：2026-03-12（重寫版，依完整規格重新審查）
> **目的**：統整目前後端現況，說明哪些地方需要清理、哪些是核心設計必須保留
> **變更方向**：(1) 清除冗餘欄位（SCRATCH_CARD_MODE 合併、subCategory 冗餘、手動排序 orderNum/weight 移除） (2) 保留所有刮刮樂指定機制（gameMode、revealedNumber、/designate API） (3) 澄清 GACHA 使用完全獨立的 DrawService

---

## 目錄

1. [現行架構正確說明](#1-現行架構正確說明)
2. [確認要移除的項目](#2-確認要移除的項目)
3. [確認要保留的項目](#3-確認要保留的項目)
4. [後台管理介面規格](#4-後台管理介面規格)
5. [前台（玩家端）介面規格](#5-前台玩家端介面規格)
6. [API 修改清單](#6-api-修改清單)
7. [資料庫欄位整理](#7-資料庫欄位整理)
8. [執行順序](#8-執行順序)

---

## 1. 現行架構正確說明

### 1.1 商品分類體系

```
category（商品大分類）
├── OFFICIAL_ICHIBAN  — 官方一番賞
├── GACHA             — 扭蛋（!! 獨立 DrawService，非票券系統 !!）
├── TRADING_CARD      — 卡牌一番賞
└── CUSTOM_GACHA      — 自製賞（需選 playMode）

playMode（玩法模式）← 目前 CUSTOM_GACHA 需要明確選
├── LOTTERY_MODE      — 一番賞型（隨機抽，不顯示格子）
├── SCRATCH_MODE      — 刮刮樂型（顯示格子，選格子刮）
└── SCRATCH_CARD_MODE — 跟 SCRATCH_MODE 邏輯完全相同（冗餘，應合併 ← 待移除）

gameMode（刮刮樂大獎分配策略）← SCRATCH_MODE 專用，三選一
├── RANDOM            — 系統完全隨機分配所有獎品（含大獎）
├── SCRATCH_STORE     — 店家在後台事先指定哪些 revealedNumber 是大獎
└── SCRATCH_PLAYER    — 開套玩家在開始抽前，先指定哪些 revealedNumber 是大獎
```

### 1.2 三大抽獎系統（關鍵！各自獨立）

#### 系統一：票券抽籤（OFFICIAL_ICHIBAN / TRADING_CARD / CUSTOM_GACHA LOTTERY_MODE）

```
Controller: LotteryDrawController
  └── POST /api/lottery/draw/{id}/draw
Service: LotteryTicketServiceImpl
特點：
  - 預先生成籤位 (lottery_ticket)
  - 每籤已分配好獎品
  - 有保護時間機制（protectionMinutes）
  - 有免單機制（freeDrawEnabled）
  - 有最後賞機制
  - 使用 LotterySession 記錄開套者
  - LOTTERY_MODE：隨機抽一張 AVAILABLE 籤，不顯示格子
```

#### 系統二：刮刮樂（CUSTOM_GACHA + SCRATCH_MODE）

```
Controller: LotteryDrawController
  ├── POST /api/lottery/draw/{id}/draw       ← 抽獎（含 SCRATCH_PLAYER 攔截）
  └── POST /api/lottery/draw/{id}/designate  ← 指定大獎位置（SCRATCH_PLAYER 專用）
Service: LotteryTicketServiceImpl
特點（在系統一基礎上加入 gameMode 控制）：
  - SCRATCH_STORE：店家建立商品時填入 designatedPrizeNumbers JSON
    → 上架時系統解析 JSON，把指定的 revealedNumber 對應到大獎籤位
  - SCRATCH_PLAYER：開套玩家第一次抽前，呼叫 /designate 傳入 revealedNumber 清單
    → checkDesignationRequired() 攔截第一次抽，要求先指定
  - RANDOM：上架時系統完全隨機分配大獎位置
  關鍵欄位：
    - revealedNumber（LotteryTicket）= 刮開後顯示的亂數，不是物理序號
    - ticketNumber = 物理格子序號（玩家選的，1~N）
    - designatedPrizeNumbers（Lottery）= SCRATCH_STORE 設定
    - isDesignatedPrize / designatedBy（LotteryTicket）= 指定追蹤
```

#### 系統三：GACHA 扭蛋（完全獨立！）

```
Controller: RandomDrawController  ← 完全不同的 Controller
  └── POST /api/lottery/random/{id}/draw?count=N
Service: DrawServiceImpl          ← 完全不同的 Service
特點：
  - 使用 LotteryPrize.weight 加權隨機機率
  - 沒有票券系統（不建立 lottery_ticket）
  - 沒有保護時間
  - 沒有免單
  - 沒有最後賞
  - 沒有開套 Session
  - 使用 weightedRandomSelect() 演算法
⚠️ 注意：GACHA 不走 LotteryDrawController，也沒有 synchronized 保護
```

### 1.3 上架流程（generateTickets）

```java
// LotteryTicketServiceImpl.generateTickets()
switch (lottery.getPlayMode()) {
    case LOTTERY_MODE:
        generateRandomTickets(lottery, prizes);   // 隨機分配所有籤
        break;
    case SCRATCH_MODE:
    case SCRATCH_CARD_MODE:  // ← 兩個 case 進同一個 method（SCRATCH_CARD_MODE 冗餘）
        generateScratchTickets(lottery, prizes);  // 根據 gameMode 決定大獎分配策略
        break;
}
```

```java
// generateScratchTickets 內部的 gameMode 判斷
switch (lottery.getGameMode()) {
    case SCRATCH_STORE:
        // 解析 designatedPrizeNumbers JSON → 指定大獎的 revealedNumber
        break;
    case SCRATCH_PLAYER:
        // 所有籤 prize = null，等玩家呼叫 /designate
        break;
    case RANDOM:
    default:
        // 直接呼叫 autoAssignNonGrandPrizes() 完全隨機分配
        break;
}
```

---

## 2. 確認要移除的項目

### 2.1 移除 SCRATCH_CARD_MODE

**原因**：與 SCRATCH_MODE 完全相同（`generateTickets()` 已把兩者合併到同一邏輯），是歷史遺留的冗餘 enum 值。

| 檔案 | 要做的事 |
|------|---------|
| `enums/` 對應 enum | 刪除 `SCRATCH_CARD_MODE` 常數 |
| `service/impl/LotteryTicketServiceImpl.java` | `generateTickets()` 的 switch case 移除 `SCRATCH_CARD_MODE`（保留 `SCRATCH_MODE`） |
| `req/lottery/LotteryCreateReq.java` | 驗證 playMode 的白名單移除 `SCRATCH_CARD_MODE` |
| Enum API（如 /api/enums）| 回傳給前端的清單移除 `SCRATCH_CARD_MODE` |

**保留**：LOTTERY_MODE、SCRATCH_MODE（只剩這兩個）

---

### 2.2 移除 Lottery.subCategory

**原因**：`subCategory` 在 `createLottery()` 中被設定，但功能與 `playMode` 完全重疊。所有查詢條件、業務邏輯都用 `playMode`，`subCategory` 純粹是歷史遺留冗餘欄位。

| 檔案 | 要做的事 |
|------|---------|
| `entity/Lottery.java` | 移除 `subCategory` 欄位 |
| `req/lottery/LotteryCreateReq.java` | 移除 `subCategory` 欄位 |
| `req/lottery/LotteryUpdateReq.java` | 移除 `subCategory` 欄位 |
| `res/lottery/LotteryRes.java` | 移除 `subCategory` 欄位 |
| `service/impl/LotteryServiceImpl.java` | `createLottery()` / `updateLottery()` 移除設定 subCategory 的程式碼 |
| `mapper/LotteryMapper.xml` | 移除 subCategory 相關 SQL |

---

### 2.3 移除 Lottery.orderNum

**原因**：商品排序不需要手動填入數字。顯示按建立時間排序即可（最新在前），不需要 orderNum 欄位。

| 檔案 | 要做的事 |
|------|---------|
| `entity/Lottery.java` | 移除 `orderNum` 欄位 |
| `req/lottery/LotteryCreateReq.java` | 移除 `orderNum` 參數 |
| `req/lottery/LotteryUpdateReq.java` | 移除 `orderNum` 參數 |
| `res/lottery/LotteryRes.java` | 移除 `orderNum` 欄位 |
| `service/impl/LotteryServiceImpl.java` | `createLottery()` 移除 `setOrderNum(0)` |
| 所有 Example 查詢 | `setOrderByClause` 改為 `created_at DESC` |

---

### 2.4 移除 Lottery.weight（商品層級）

**原因**：`Lottery.weight` 完全沒有被使用（程式碼中有注釋「✅ 不再設定 weight」），是死欄位。

⚠️ **注意**：這裡移除的是 **Lottery（商品）** 上的 weight，不是 **LotteryPrize（獎品）** 上的 weight。`LotteryPrize.weight` 是 GACHA 加權隨機機率用的，**必須保留**！

| 檔案 | 要做的事 |
|------|---------|
| `entity/Lottery.java` | 移除 `weight` 欄位 |
| `req/lottery/LotteryCreateReq.java` | 移除 `weight` 參數 |
| `req/lottery/LotteryUpdateReq.java` | 移除 `weight` 參數 |
| `res/lottery/LotteryRes.java` | 移除 `weight` 欄位 |

---

## 3. 確認要保留的項目

以下是之前版本曾錯誤標記為移除的功能，**全部必須保留**。

### 3.1 ✅ 保留 gameMode（刮刮樂大獎模式控制點）

`gameMode` 是控制刮刮樂大獎如何分配的唯一機制。後台建立 SCRATCH_MODE 商品時，**必須顯示 gameMode 下拉選單**讓店家選擇。

| gameMode | 誰決定大獎位置 | 商品建立時要填什麼 |
|----------|-------------|-----------------|
| `RANDOM` | 系統完全隨機 | 不需要填任何額外資訊 |
| `SCRATCH_STORE` | 店家事先指定 | 填 `designatedPrizeNumbers`（JSON，指定哪些 revealedNumber 是大獎） |
| `SCRATCH_PLAYER` | 開套玩家動態指定 | 不需要額外資訊，玩家開套時呼叫 /designate |

---

### 3.2 ✅ 保留 revealedNumber（刮刮樂核心概念）

刮刮樂有兩個號碼：
- `ticketNumber`：物理格子序號（1~N，玩家看到與選擇的格子號碼）
- `revealedNumber`：刮開後顯示的隨機號碼（用於大獎位置指定，不讓玩家預測）

**為什麼需要 revealedNumber？**

若只用 `ticketNumber`，店家指定「第 15 格是大獎」，玩家就能預測大獎位置。`revealedNumber` 是上架時隨機打亂的序號，讓店家用 revealedNumber 指定大獎，但玩家只看到 ticketNumber（不知道哪個 ticketNumber 對應哪個 revealedNumber），從而保持公平性。

**安全要求**：前端查詢 AVAILABLE 格子時，後端不能回傳 revealedNumber（已在 `getTicketsForFrontend()` 中實作隱藏）。

---

### 3.3 ✅ 保留 designatedPrizeNumbers（SCRATCH_STORE 用）

`Lottery.designatedPrizeNumbers` 是一個 JSON 欄位，例如：
```json
[
  { "revealedNumber": 42, "prizeId": "uuid-of-grand-prize" },
  { "revealedNumber": 17, "prizeId": "uuid-of-grand-prize" }
]
```
店家在後台建立商品時填入，上架時系統解析此 JSON 把對應籤位標記為大獎。

---

### 3.4 ✅ 保留 /designate API（SCRATCH_PLAYER 用）

```
POST /api/lottery/draw/{lotteryId}/designate
```

SCRATCH_PLAYER 流程：
1. 開套玩家第一次觸發抽獎時，`checkDesignationRequired()` 攔截
2. 回傳 `{ designationRequired: true, availableNumbers: [...], grandPrizes: [...] }`
3. 前端讓玩家從 availableNumbers 中挑選，決定哪些 revealedNumber 是大獎
4. 玩家選好後呼叫 /designate
5. 之後才能正常顯示格子進行抽獎

---

### 3.5 ✅ 保留 isDesignatedPrize / designatedBy（LotteryTicket）

- `isDesignatedPrize`：標記此籤位是否為被指定的大獎籤位
- `designatedBy`：記錄是誰指定的（STORE 或 PLAYER）

這兩個欄位用於 `autoAssignNonGrandPrizes()` 判斷哪些籤位已有獎品、哪些還需要補填非大獎。

---

### 3.6 ✅ 保留 playerDesignatedNumbers（LotterySession）

`LotterySession.playerDesignatedNumbers` 記錄 SCRATCH_PLAYER 模式下，開套玩家已提交的 revealedNumber 清單。用於確認指定是否已完成、防止重複指定。

---

### 3.7 ✅ 保留 LotteryPrize.weight（GACHA 機率）

GACHA 扭蛋系統（`DrawServiceImpl.weightedRandomSelect()`）使用 `LotteryPrize.weight` 計算每個獎品的機率，**必須保留**。

---

## 4. 後台管理介面規格

### 4.1 商品列表頁

| 欄位 | 說明 | 備註 |
|------|------|------|
| 商品圖片 | imageUrl 縮圖 | |
| 商品名稱 | title | 可搜尋 |
| 分類 | category 中文名稱 | 篩選器 |
| 玩法 | playMode 中文名稱（一番賞 / 刮刮樂） | 篩選器 |
| 遊戲模式 | gameMode 中文（僅 SCRATCH_MODE 顯示：隨機 / 店家指定 / 玩家指定） | 顯示用 |
| 每抽價格 | pricePerDraw | |
| 已抽/總抽 | totalDraws / maxDraws | |
| 狀態 | status 中文名稱 | 篩選器 |
| 建立時間 | createdAt | 預設排序（最新在前） |
| 操作 | 編輯/上架/下架/刪除 | |

**不需要的欄位**：
- ❌ orderNum（改用 createdAt 排序）
- ❌ weight（Lottery 層級，已移除）
- ❌ subCategory（與 playMode 重複，已移除）

---

### 4.2 商品新增/編輯頁

#### 基本資訊區

| 欄位 | 必填 | 說明 |
|------|------|------|
| 商品名稱 (title) | ✅ | |
| 商品圖片 (imageUrl) | ✅ | 上傳至 S3 |
| 圖片集 (galleryImages) | | 多圖上傳 |
| 商品分類 (category) | ✅ | 下拉：官方一番賞 / 扭蛋 / 卡牌 / 自製賞 |
| 玩法模式 (playMode) | 自製賞必填 | 下拉：一番賞型(LOTTERY_MODE) / 刮刮樂型(SCRATCH_MODE) |
| **遊戲模式 (gameMode)** | **刮刮樂必填** | **下拉：隨機(RANDOM) / 店家指定(SCRATCH_STORE) / 玩家指定(SCRATCH_PLAYER)** |
| **指定大獎號碼 (designatedPrizeNumbers)** | **SCRATCH_STORE 必填** | **JSON 格式，或提供圖形化選格子 UI** |
| 每抽價格 (pricePerDraw) | ✅ | |
| 折扣價 (discountedPrice) | | |
| 是否自動降價 (autoDiscountEnabled) | | Toggle |
| 商品描述 (description) | | 富文本 |
| 標籤 (tags) | | 多選 |

#### 抽獎設定區

| 欄位 | 必填 | 說明 |
|------|------|------|
| 總格子數 (maxDraws) | 刮刮樂必填 | LOTTERY_MODE 自動計算（= 獎品總數） |
| 是否允許多抽 (allowMultiDraw) | | Toggle |
| 多抽選項 (multiDrawOptions) | | 例如 [5, 10] |
| 保護時間 (protectionMinutes) | | 預設 5 分鐘，GACHA 不適用 |
| 保護抽數 (protectionDraws) | | 免單判斷閾值 |
| 是否開啟免單 (freeDrawEnabled) | | Toggle |

#### 獎品設定區

| 欄位 | 必填 | 說明 |
|------|------|------|
| 獎品名稱 (name) | ✅ | |
| 獎品圖片 (imageUrl) | ✅ | |
| 獎品等級 (level) | ✅ | A/B/C/D/E/F/G 賞 等 |
| 數量 (quantity) | ✅ | |
| 是否為大獎 (isGrandPrize) | | Toggle（免單觸發判斷用） |
| 是否為最後賞 (isLastPrize) | | Toggle |
| 獎品權重 (weight) | GACHA 必填 | 影響 DrawService 中獎機率；其他模式填 1 |

---

### 4.3 商品詳情頁（後台查看）

| 區塊 | 內容 |
|------|------|
| 基本資訊 | 同編輯頁（唯讀模式），含 gameMode 顯示 |
| 獎品列表 | 顯示所有獎品、剩餘數量 |
| 籤位狀態 | 顯示所有格子的 ticketNumber、status（AVAILABLE/DRAWN）；已抽格子顯示獎品與中獎者 |
| 銷售統計 | 已抽次數、營收、剩餘比例 |

**注意**：後台管理員可查看 revealedNumber（方便客服查詢），但前台玩家端不可顯示 AVAILABLE 格子的 revealedNumber。

---

## 5. 前台（玩家端）介面規格

### 5.1 商品列表頁

| 欄位 | 說明 |
|------|------|
| 商品圖片 | imageUrl |
| 商品名稱 | title |
| 每抽價格 | pricePerDraw（如有折扣顯示折扣價） |
| 剩餘抽數 | remainingDraws = maxDraws - totalDraws |
| 分類標籤 | category 中文 |
| 玩法標籤 | playMode 中文 |

**排序**：預設按上架時間（最新在前）

---

### 5.2 一番賞（LOTTERY_MODE）詳情頁

| 區塊 | 內容 |
|------|------|
| 商品資訊 | 圖片、名稱、價格、描述 |
| 獎品列表 | 各賞等級、名稱、圖片、剩餘數/總數 |
| 抽獎按鈕 | 單抽 / 多抽（如允許） |
| 保護資訊 | 如果有人開套，顯示保護倒數或「等待中」 |

---

### 5.3 刮刮樂（SCRATCH_MODE）詳情頁

| 區塊 | 內容 |
|------|------|
| 商品資訊 | 圖片、名稱、價格、描述 |
| 獎品列表 | 各賞等級、名稱、圖片、剩餘數/總數 |
| 格子面板 | 顯示 1~N 個格子（按 ticketNumber 排列） |
| 格子狀態 | AVAILABLE → 未刮（隱藏獎品）、DRAWN → 已刮（顯示獎品圖片/等級） |
| 選格子 | 玩家點選未刮的格子 → 確認 → 後端執行抽獎 |
| 保護資訊 | 如果有人開套，顯示保護倒數 |

⚠️ **SCRATCH_PLAYER 流程（前端需特別處理）**：
```
1. 玩家按下抽獎（第一次）
2. 後端回傳 { designationRequired: true, availableNumbers: [...], grandPrizes: [...] }
3. 前端彈出「選擇大獎位置」介面
4. 玩家從 availableNumbers 中選出 grandPrizes 總數量個號碼（revealedNumber）
5. 呼叫 POST /api/lottery/draw/{id}/designate
6. 系統鎖定大獎籤位，之後顯示格子面板正常抽獎
```

**前台不應該顯示的東西**：
- ❌ `revealedNumber`（AVAILABLE 格子：後端 getTicketsForFrontend() 已隱藏）
- ❌ `gameMode`（邏輯對玩家透明，不需顯示）
- ❌ AVAILABLE 籤位的 prizeId / prizeName / prizeLevel / prizeImageUrl / isGrandPrize

---

### 5.4 GACHA 扭蛋（完全不同的 UI 流程）

GACHA 走 `POST /api/lottery/random/{lotteryId}/draw?count=N`，沒有格子選擇，直接返回結果。前端只需要：
- 選抽數（1 / 5 / 10）
- 按下抽扭蛋
- 顯示結果動畫

**不需要**：格子面板、保護時間倒數、開套 Session 概念。

---

### 5.5 抽獎結果彈窗

| 欄位 | 說明 |
|------|------|
| 獎品名稱 | prizeName |
| 獎品圖片 | prizeImageUrl |
| 獎品等級 | prizeLevel（A賞、B賞...） |
| 是否大獎 | isGrandPrize → 特效動畫 |
| 免單提示 | triggeredFreeDraw → 「恭喜免單！退還 XX 金幣」 |
| 謝謝惠顧 | prizeId 為 null → 「再接再厲！」 |

---

## 6. API 修改清單

### 6.1 需要刪除的 API

無。所有 API 端點維持不動，只修改 Request / Response 欄位。

---

### 6.2 需要修改的 API（Request 移除欄位）

| API | 移除的 Request 欄位 |
|-----|-------------------|
| `POST /admin/lottery` | 移除 `orderNum`、`weight`（Lottery 層級）、`subCategory` |
| `PUT /admin/lottery/{id}` | 移除 `orderNum`、`weight`（Lottery 層級）、`subCategory` |
| `POST /admin/lottery-with-prizes` | 移除 `orderNum`、`weight`、`subCategory` |
| `PUT /admin/lottery-with-prizes/{id}` | 移除 `orderNum`、`weight`、`subCategory` |

---

### 6.3 需要修改的 API（Response 移除欄位）

| API | 移除的 Response 欄位 |
|-----|---------------------|
| `GET /admin/lottery/{id}` | 移除 `orderNum`、`weight`（Lottery 層級）、`subCategory` |
| `POST /admin/lottery/list` | 移除 `orderNum`、`weight`、`subCategory` |
| `GET /api/lottery/browse/{id}/detail` | 移除 `orderNum`、`weight`、`subCategory` |
| `POST /api/lottery/browse/list` | 移除 `orderNum`、`weight`、`subCategory` |
| `POST /api/lottery/draw/{id}/draw` | 移除 `subCategory`；**保留 gameMode** |

---

### 6.4 列表排序調整

所有商品列表查詢（admin 和前台 browse）：
- **移除**：`order_num ASC` 的預設排序
- **改為**：`created_at DESC` 預設排序

---

### 6.5 保留不變的 API

| API | 說明 |
|-----|------|
| `POST /api/lottery/draw/{id}/designate` | SCRATCH_PLAYER 核心功能，保留 |
| `checkDesignationRequired()` 攔截邏輯 | SCRATCH_PLAYER 核心功能，保留 |
| `GET /api/lottery/draw/{id}/session` | Session 查詢，回傳保護時間等，保留 |
| `POST /api/lottery/random/{id}/draw` | GACHA 系統，完全不動 |
| 所有 Auth / User / Store / Banner / News / Wallet / PrizeBox / Order API | 不涉及此次整頓 |

---

### 6.6 修改後的抽獎 Response 格式

```json
{
  "playMode": "SCRATCH_MODE",
  "gameMode": "SCRATCH_STORE",
  "results": [
    {
      "success": true,
      "ticketId": "uuid",
      "ticketNumber": 15,
      "prizeId": "uuid",
      "prizeLevel": "A",
      "prizeName": "炭治郎手辦",
      "prizeImageUrl": "https://...",
      "isGrandPrize": true,
      "triggeredFreeDraw": true,
      "refundAmount": 1600,
      "message": "恭喜中獲 A賞！開套免單，退還 1600 金幣！"
    }
  ],
  "protectionEndTime": "2026-03-12T10:35:00"
}
```

移除的欄位（相較於舊格式）：
- ❌ `subCategory`（移除）
- ❌ `orderNum`（移除）

保留的欄位：
- ✅ `gameMode`（SCRATCH_MODE 情況下前端需知道是 RANDOM / SCRATCH_STORE / SCRATCH_PLAYER）

---

## 7. 資料庫欄位整理

### 7.1 lottery 表（要刪除的欄位）

| 欄位 | 操作 | 替代方案 |
|------|------|---------|
| `sub_category` | **刪除** | 與 `play_mode` 重複，統一用 `play_mode` |
| `order_num` | **刪除** | 改用 `created_at DESC` 排序 |
| `weight` | **刪除** | Lottery 層級無用（Prize 層級的 weight 保留） |

```sql
ALTER TABLE lottery DROP COLUMN sub_category;
ALTER TABLE lottery DROP COLUMN order_num;
ALTER TABLE lottery DROP COLUMN weight;
```

---

### 7.2 lottery 表（要保留的欄位）

| 欄位 | 說明 |
|------|------|
| `play_mode` | LOTTERY_MODE / SCRATCH_MODE（整合後只剩兩種） |
| `game_mode` | RANDOM / SCRATCH_STORE / SCRATCH_PLAYER（**保留，刮刮樂大獎策略**） |
| `designated_prize_numbers` | SCRATCH_STORE 的大獎位置 JSON（**保留**） |
| `protection_minutes` | 保護時間（分鐘） |
| `protection_draws` | 免單保護抽數 |
| `free_draw_enabled` | 免單開關 |
| `max_draws` | 籤位總數 |
| 所有其他欄位 | 維持不動 |

---

### 7.3 lottery_ticket 表（所有欄位維持不動）

| 欄位 | 說明 |
|------|------|
| `ticket_number` | 物理格子序號（1~N），**保留** |
| `revealed_number` | 刮開後顯示的隨機號碼（大獎指定用），**保留** |
| `is_designated_prize` | 是否為指定大獎籤位，**保留** |
| `designated_by` | 誰指定的（STORE / PLAYER），**保留** |
| `prize_id` | 對應的獎品 ID |
| `status` | AVAILABLE / DRAWN |
| 所有其他欄位 | 維持不動 |

---

### 7.4 lottery_session 表（所有欄位維持不動）

| 欄位 | 說明 |
|------|------|
| `player_designated_numbers` | SCRATCH_PLAYER 玩家已指定的 revealedNumber 清單，**保留** |
| `opener_user_id` | 開套者 |
| `protection_end_time` | 保護時間截止 |
| `free_draw_triggered` | 免單是否已觸發 |
| 所有其他欄位 | 維持不動 |

---

### 7.5 lottery_prize 表（所有欄位維持不動）

| 欄位 | 說明 |
|------|------|
| `weight` | GACHA 加權隨機機率，**必須保留** |
| `is_grand_prize` | 大獎標記（免單判斷） |
| `is_last_prize` | 最後賞標記 |
| 所有其他欄位 | 維持不動 |

---

## 8. 執行順序

### Phase 1：後端程式碼清理

**Step 1：合併 SCRATCH_CARD_MODE**
- 刪除 enum 中的 `SCRATCH_CARD_MODE`
- 更新 `generateTickets()` switch case
- 更新 Req 的 playMode 驗證白名單
- 更新 Enum API 回傳清單

**Step 2：移除 Lottery.subCategory**
- 刪除 Entity、Req、Res、Mapper XML 中的 `subCategory`
- 刪除 `createLottery()` / `updateLottery()` 中設定 subCategory 的程式碼

**Step 3：移除 Lottery.orderNum**
- 刪除 Entity、Req、Res 中的 `orderNum`
- 所有列表查詢改為 `created_at DESC`

**Step 4：移除 Lottery.weight（商品層級）**
- 刪除 Entity、Req、Res 中的 `weight`
- （**注意只刪 Lottery 的，不動 LotteryPrize 的**）

---

### Phase 2：資料庫清理

**Step 5：執行 ALTER TABLE（確認程式碼已移除相關欄位使用後再執行）**

```sql
-- 移除冗餘欄位
ALTER TABLE lottery DROP COLUMN sub_category;
ALTER TABLE lottery DROP COLUMN order_num;
ALTER TABLE lottery DROP COLUMN weight;

-- 以下欄位確認保留（不動）
-- lottery.game_mode                         ← 保留
-- lottery.designated_prize_numbers          ← 保留
-- lottery_ticket.revealed_number            ← 保留
-- lottery_ticket.is_designated_prize        ← 保留
-- lottery_ticket.designated_by              ← 保留
-- lottery_session.player_designated_numbers ← 保留
-- lottery_prize.weight                      ← 保留
```

---

### Phase 3：前端同步調整

**Step 6：後台前端**
- 商品列表移除 orderNum 排序機制，改為 createdAt 排序
- 商品編輯頁移除 orderNum、weight（Lottery 層級）、subCategory 欄位
- playMode 選項移除 SCRATCH_CARD_MODE（只留 LOTTERY_MODE / SCRATCH_MODE）
- **保留 gameMode 下拉選單**（RANDOM / SCRATCH_STORE / SCRATCH_PLAYER）
- **保留 designatedPrizeNumbers 輸入區**（SCRATCH_STORE 時顯示）

**Step 7：前台玩家端**
- SCRATCH_PLAYER 指定大獎介面維持（這是產品設計的一部分）
- 確認 AVAILABLE 格子不顯示 revealedNumber（後端已處理，前端確認不使用此欄位）

---

## 附錄：快速對照表

### 雙號碼速查

| 名詞 | 說明 | 前端可見？ |
|------|------|---------|
| ticketNumber | 格子的物理序號（1~N），玩家點選的格子號碼 | ✅ 可見 |
| revealedNumber | 刮開後顯示的亂數，用於 SCRATCH_STORE/SCRATCH_PLAYER 指定大獎 | ❌ AVAILABLE 狀態隱藏；DRAWN 後可顯示 |

### gameMode 速查

| gameMode | 控制方式 | 前端需做什麼 |
|----------|---------|------------|
| `RANDOM` | 完全由後端決定 | 無需特別處理 |
| `SCRATCH_STORE` | 後台商品設定時指定 | 無需特別處理（玩家端透明） |
| `SCRATCH_PLAYER` | 開套玩家動態指定 | 需處理 designationRequired 攔截與選號介面 |

### 需修改 vs 需保留 總覽

| 項目 | 操作 | 理由 |
|------|------|------|
| `SCRATCH_CARD_MODE` enum | **移除** | 與 SCRATCH_MODE 完全重複 |
| `Lottery.subCategory` | **移除** | 與 playMode 完全重複 |
| `Lottery.orderNum` | **移除** | 改用 created_at 自動排序 |
| `Lottery.weight` | **移除** | 商品層級完全無用 |
| `gameMode` | **保留** | 刮刮樂大獎策略的唯一控制點 |
| `revealedNumber` | **保留** | 刮刮樂公平性機制的核心 |
| `designatedPrizeNumbers` | **保留** | SCRATCH_STORE 必要 |
| `/designate` API | **保留** | SCRATCH_PLAYER 必要 |
| `isDesignatedPrize` / `designatedBy` | **保留** | autoAssignNonGrandPrizes 判斷用 |
| `playerDesignatedNumbers` | **保留** | SCRATCH_PLAYER Session 追蹤用 |
| `LotteryPrize.weight` | **保留** | GACHA 加權機率必要 |

