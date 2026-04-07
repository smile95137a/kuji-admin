---
description: "商品分類與遊戲模式的完整規則。新增/編輯商品 API、前台顯示判斷、抽籤路由分流時必讀。後台前端、前台前端、後端都適用。"
applyTo: ["**/lottery/**", "**/req/lottery/**", "**/controller/**/*Lottery*", "**/service/*Lottery*", "**/enums/Lottery*"]
---

# 商品分類與遊戲模式規則

## 一、四個欄位速查表

| 欄位 | 誰填 | 決定什麼 | 必填？ |
|---|---|---|---|
| `category` | 前端 | 前台顯示分類 + 扭蛋抽獎路由 | ✅ 必填 |
| `subCategory` | 前端 | 自製賞的型別（前台顯示用） | 僅 CUSTOM_GACHA 必填 |
| `playMode` | **後端自動推算** | 籤位生成路由（LOTTERY vs SCRATCH） | ❌ 前端不傳 |
| `gameMode` | 前端（刮刮樂專用） | 刮刮樂的開獎機制 | 僅 SCRATCH_MODE 必填 |

---

## 二、category 可用值

```
OFFICIAL_ICHIBAN   官方一番賞
GACHA              扭蛋
TRADING_CARD       卡牌
CUSTOM_GACHA       自製賞（需搭配 subCategory）
```

> ⚠️ `SCRATCH` 是廢棄的舊值，不可再使用。刮刮樂商品 = `CUSTOM_GACHA` + `subCategory=SCRATCH_MODE`

---

## 三、subCategory 可用值（僅 CUSTOM_GACHA）

```
LOTTERY_MODE    自製抽籤型（玩家選籤號）
SCRATCH_MODE    自製刮刮樂型（刮開顯示號碼）
```

---

## 四、playMode — 後端自動推算，前端不傳

`LotteryServiceImpl.resolvePlayMode()` 根據 `category` + `subCategory` 推算：

| category | subCategory | → playMode |
|---|---|---|
| `GACHA` | - | `LOTTERY_MODE` |
| `OFFICIAL_ICHIBAN` | - | `LOTTERY_MODE` |
| `TRADING_CARD` | - | `LOTTERY_MODE` |
| `CUSTOM_GACHA` | `LOTTERY_MODE` 或 null | `LOTTERY_MODE` |
| `CUSTOM_GACHA` | `SCRATCH_MODE` | `SCRATCH_MODE` |

`playMode` 決定 `LotteryTicketServiceImpl.generateTickets()` 的分支：
- `LOTTERY_MODE` → `generateRandomTickets()`（一番賞/扭蛋籤位）
- `SCRATCH_MODE` → `generateScratchTickets()`（刮刮樂籤位，含 revealedNumber）

---

## 五、gameMode 可用值（僅 SCRATCH_MODE 商品）

```
RANDOM          全隨機（不指定大獎位置）
SCRATCH_STORE   店家建立商品時預先指定大獎 revealedNumber
SCRATCH_PLAYER  開套玩家進入後呼叫 /designate 指定大獎位置（10 分鐘倒數）
```

---

## 六、商品類型完整對照（建立時應傳什麼）

### 官方一番賞
```json
{
  "category": "OFFICIAL_ICHIBAN",
  "subCategory": null,
  "gameMode": null
}
// playMode → 後端自動設為 LOTTERY_MODE
```

### 扭蛋
```json
{
  "category": "GACHA",
  "subCategory": null,
  "gameMode": null
}
// playMode → 後端自動設為 LOTTERY_MODE
```

### 自製賞（抽籤型）
```json
{
  "category": "CUSTOM_GACHA",
  "subCategory": "LOTTERY_MODE",
  "gameMode": null
}
// playMode → 後端自動設為 LOTTERY_MODE
```

### 自製賞（刮刮樂 SCRATCH_STORE）
```json
{
  "category": "CUSTOM_GACHA",
  "subCategory": "SCRATCH_MODE",
  "gameMode": "SCRATCH_STORE"
}
// playMode → 後端自動設為 SCRATCH_MODE
```

### 自製賞（刮刮樂 SCRATCH_PLAYER）
```json
{
  "category": "CUSTOM_GACHA",
  "subCategory": "SCRATCH_MODE",
  "gameMode": "SCRATCH_PLAYER"
}
// playMode → 後端自動設為 SCRATCH_MODE
```

### 自製賞（刮刮樂 RANDOM）
```json
{
  "category": "CUSTOM_GACHA",
  "subCategory": "SCRATCH_MODE",
  "gameMode": "RANDOM"
}
// playMode → 後端自動設為 SCRATCH_MODE
```

---

## 七、抽獎流程的路由判斷（後端 DrawController）

```
category == "GACHA"
  → synchronized 扭蛋路徑（DrawService.executeDraw）

category != "GACHA"
  gameMode == "SCRATCH_PLAYER" && 開套者尚未指定
    → 回傳 DesignationRequiredResponse（開套者）
    → 回傳 DesignationPendingResponse（非開套者）
  否則
    → 一般票券抽獎路徑（LotteryTicketService）
```

---

## 八、前台顯示判斷

前台拿到商品後，應根據以下欄位組合決定顯示 UI：

| 情境 | 判斷條件 |
|---|---|
| 顯示「扭蛋」動畫 | `category == "GACHA"` |
| 顯示「選號」UI | `playMode == "LOTTERY_MODE"` && category != GACHA |
| 顯示「刮刮樂」UI | `playMode == "SCRATCH_MODE"` |
| 顯示「等待指定」畫面 | `gameMode == "SCRATCH_PLAYER"` && `session.isDesignationComplete == false` |
| 顯示指定倒數計時 | `session.designationDeadline != null` |

---

## 九、session 回應欄位（GET /api/lottery/draw/{id}/session）

```json
{
  "sessionId": "...",
  "isOpener": true,
  "status": "ACTIVE",
  "designationDeadline": "2026-04-07T02:33:00",   // null = 非 SCRATCH_PLAYER 或已完成
  "isDesignationComplete": false                   // true = 無需指定 或 已完成指定
}
```

`isDesignationComplete` 判斷規則：
- `gameMode != "SCRATCH_PLAYER"` → `true`（一番賞/扭蛋/SCRATCH_STORE 預設完成）
- `SCRATCH_PLAYER` 且 `playerDesignatedNumbers` 有值 → `true`
- `SCRATCH_PLAYER` 且 `playerDesignatedNumbers` 為 null/空 → `false`

---

## 十、⚠️ 禁止操作

- ❌ 不要讓前端傳 `playMode`（後端自動推算，傳了也會被覆蓋）
- ❌ 不要用 `category=SCRATCH`（已廢棄，正確是 `CUSTOM_GACHA` + `subCategory=SCRATCH_MODE`）
- ❌ 不要在非 SCRATCH_MODE 商品上設定 `gameMode`
- ❌ 不要用 `subCategory` 做抽獎路由判斷（只有 `playMode` 才是路由依據）
- ❌ 不要在前台用 `category` 判斷是否顯示刮刮樂 UI（應用 `playMode`）
