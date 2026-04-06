---
description: "Use when working on gacha random draw, weighted random algorithm, or GACHA category lottery. Covers RandomDrawController and DrawService."
---

# 扭蛋隨機抽獎功能使用說明

## 📋 概述

本文件說明扭蛋商品的隨機抽獎實現方式，以及如何區分「一番賞」與「扭蛋」兩種不同的抽獎模式。

## 🎯 一番賞 vs 扭蛋的區別

| 項目 | 一番賞（OFFICIAL_ICHIBAN） | 扭蛋（GACHA） |
|------|---------------------------|---------------|
| **抽獎方式** | 前端選號（玩家選擇特定籤位號碼） | 後端隨機（系統自動選號） |
| **API 路由** | `POST /lottery/draw/{lotteryId}/draw` | `POST /lottery/random/{lotteryId}/draw` |
| **Controller** | LotteryDrawController | RandomDrawController |
| **Service** | LotteryTicketService（籤位制） | DrawService（加權隨機） |
| **商品分類** | `category=OFFICIAL_ICHIBAN` | `category=GACHA` |
| **使用場景** | 實體一番賞機台（可選號） | 扭蛋機（隨機出獎） |

## 🔧 扭蛋隨機抽獎邏輯

### ⚠️ weight 欄位說明（兩個表，兩種含義）

| 表 | 欄位 | DDL 定義 | 用途 |
|---|---|---|---|
| `lottery` | `weight` | `推薦權重` | 首頁排序用，與抽獎無關 |
| `lottery_prize` | `weight` | `抽中權重（0=不可抽，用於最後賞）` | 扭蛋加權算法使用 |

**`lottery_prize.weight` = 機率權重**，不是商品寬度或顯示排序。

### 1. 加權隨機演算法

扭蛋使用 **加權隨機演算法**，根據 `lottery_prize.weight` 欄位決定中獎機率：

```java
// DrawServiceImpl.java（實際程式碼）
private LotteryPrize weightedRandomSelect(List<LotteryPrize> prizes) {
    // 計算總權重（weight 未設定時預設 1）
    int totalWeight = prizes.stream()
            .mapToInt(p -> p.getWeight() != null ? p.getWeight() : 1)
            .sum();
    
    // 產生隨機數（0 ~ totalWeight-1）
    int randomValue = random.nextInt(totalWeight);
    
    // 累加權重，找到對應的獎品
    int cumulativeWeight = 0;
    for (LotteryPrize prize : prizes) {
        cumulativeWeight += (prize.getWeight() != null ? prize.getWeight() : 1);
        if (randomValue < cumulativeWeight) {
            return prize;
        }
    }
    
    return prizes.get(prizes.size() - 1);
}
```

### 2. 權重設定範例

假設有以下獎品配置：

| 獎項 | 獎品名稱 | 數量 | 權重 | 中獎機率 |
|------|---------|------|------|---------|
| A | SSR 角色公仔 | 2 | 5 | 2% (5/250) |
| B | SR 角色立牌 | 5 | 15 | 6% (15/250) |
| C | R 透明資料夾 | 20 | 80 | 32% (80/250) |
| D | 徽章 | 30 | 150 | 60% (150/250) |

**總權重** = 5 + 15 + 80 + 150 = 250

### 3. 抽獎流程（後端自動執行）

```mermaid
graph TD
    A[玩家請求抽獎] --> B[驗證商品狀態 ON_SHELF]
    B --> C[查詢可抽獎品 remaining > 0]
    C --> D[計算費用 price_per_draw × count]
    D --> E[驗證錢包餘額 gold + bonus >= cost]
    E --> F[扣除點數 優先Gold]
    F --> G[執行加權隨機抽獎]
    G --> H[扣除獎品庫存 remaining - 1]
    H --> I[新增至賞品盒 PrizeBox]
    I --> J[記錄交易 WalletTransaction]
    J --> K[返回抽獎結果]
```

## 📍 前端使用指南

### 查詢扭蛋商品列表

```bash
POST /api/lottery/browse/list
Content-Type: application/json

{
  "condition": {
    "category": "GACHA",      # 只查詢扭蛋商品
    "status": "ON_SHELF"      # 只查詢已上架商品
  }
}
```

**回應範例**：

```json
{
  "success": true,
  "data": [
    {
      "id": "uuid-gacha-001",
      "title": "精靈寶可夢扭蛋",
      "category": "GACHA",
      "categoryName": "扭蛋",
      "pricePerDraw": 50,
      "maxDraws": 100,
      "status": "ON_SHELF"
    }
  ]
}
```

### 執行扭蛋抽獎

```bash
POST /api/lottery/random/{lotteryId}/draw?count=3
Authorization: Bearer {USER_JWT_TOKEN}
```

**注意事項**：
- `count` 參數：抽獎次數（1-10 次）
- 前端**不需要提供籤位號碼**（後端自動隨機選擇）
- 系統會自動扣除點數（優先使用金幣，不足時使用紅利）

**回應範例**：

```json
{
  "success": true,
  "data": {
    "results": [
      {
        "lotteryTitle": "精靈寶可夢扭蛋",
        "prizeName": "皮卡丘 公仔",
        "prizeLevel": "C",
        "prizeImageUrl": "https://example.com/prize/pikachu.jpg",
        "recycleBonus": 100
      },
      {
        "lotteryTitle": "精靈寶可夢扭蛋",
        "prizeName": "伊布 徽章",
        "prizeLevel": "D",
        "prizeImageUrl": "https://example.com/prize/eevee-badge.jpg",
        "recycleBonus": 50
      },
      {
        "lotteryTitle": "精靈寶可夢扭蛋",
        "prizeName": "噴火龍 立牌",
        "prizeLevel": "B",
        "prizeImageUrl": "https://example.com/prize/charizard.jpg",
        "recycleBonus": 300
      }
    ],
    "goldUsed": 150,          # 本次使用的金幣
    "bonusUsed": 0,           # 本次使用的紅利
    "remainingGold": 850,     # 剩餘金幣
    "remainingBonus": 200,    # 剩餘紅利
    "totalCount": 3           # 總抽獎次數
  }
}
```

## 🎮 一番賞抽獎方式（對比參考）

一番賞使用**籤位制**，前端需要提供籤位號碼：

```bash
POST /api/lottery/draw/{lotteryId}/draw
Content-Type: application/json
Authorization: Bearer {USER_JWT_TOKEN}

{
  "count": 1,
  "tickets": [5]    # 玩家選擇 5 號籤位
}
```

## 🛠️ 後端技術細節

### Controller 層

[RandomDrawController.java](src/main/java/com/group/admin/controller/api/RandomDrawController.java)

```java
@PostMapping("/{lotteryId}/draw")
@PreAuthorize("hasRole('USER')")
public ResponseEntity<DrawResponseRes> executeDraw(
        @PathVariable String lotteryId,
        @RequestParam 
        @Min(value = 1, message = "抽獎次數最少 1 次")
        @Max(value = 10, message = "抽獎次數最多 10 次")
        Integer count) {
    
    String userId = SecurityUtils.getCurrentUserId();
    
    // 執行抽獎
    List<DrawResultRes> results = drawService.executeDraw(userId, lotteryId, count);
    
    // 查詢錢包餘額變化
    var walletAfter = walletService.getWallet(userId);
    
    // 組合回應
    DrawResponseRes response = DrawResponseRes.builder()
            .results(results)
            .goldUsed(goldBefore - walletAfter.getGoldCoins())
            .bonusUsed(bonusBefore - walletAfter.getBonusCoins())
            .remainingGold(walletAfter.getGoldCoins())
            .remainingBonus(walletAfter.getBonusCoins())
            .totalCount(results.size())
            .build();
    
    return ResponseEntity.ok(response);
}
```

### Service 層

[DrawServiceImpl.java](src/main/java/com/group/admin/service/impl/DrawServiceImpl.java)

核心業務邏輯：

1. **驗證商品狀態** → `ON_SHELF`
2. **查詢可抽獎品** → `remaining > 0`
3. **計算費用** → `pricePerDraw × count`
4. **驗證錢包餘額** → `gold + bonus >= cost`
5. **扣除點數** → 優先 Gold，不足時用 Bonus
6. **執行加權隨機抽獎** → 根據 weight 權重
7. **減少獎品庫存** → `remaining - 1`
8. **新增至賞品盒** → `PrizeBox`
9. **記錄錢包交易** → `WalletTransaction`

## ⚠️ 注意事項

### 1. 區分商品類別

建立扭蛋商品時，務必設定 `category = "GACHA"`：

```sql
INSERT INTO lottery (category, ...) VALUES ('GACHA', ...);
```

### 2. 權重設定

- 預設權重：100
- 權重越高，中獎機率越高
- 建議範圍：1 ~ 1000

### 3. 庫存管理

- 每次抽中獎品後，系統自動扣減 `remaining` 欄位
- 當某獎品 `remaining = 0` 時，該獎品不會再被抽中
- 當所有獎品 `remaining = 0` 時，商品自動下架

### 4. 前端建議

- 扭蛋商品列表顯示「隨機抽獎」按鈕（不顯示籤位選擇）
- 一番賞商品列表顯示「選號抽獎」介面
- 抽獎結果建議使用動畫效果（模擬轉蛋機轉動）

## 📊 測試範例

### Postman 測試步驟

1. **取得用戶 JWT Token**

```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

2. **查詢扭蛋商品**

```bash
POST /api/lottery/browse/list
Content-Type: application/json

{
  "condition": {
    "category": "GACHA"
  }
}
```

3. **執行抽獎（抽 5 次）**

```bash
POST /api/lottery/random/{lotteryId}/draw?count=5
Authorization: Bearer {JWT_TOKEN}
```

4. **查看賞品盒**

```bash
GET /api/prize-box
Authorization: Bearer {JWT_TOKEN}
```

## 🔗 相關文件

- [API 完整參考文件](FRONTEND_API_COMPLETE_REFERENCE.md)
- [一番賞籤位制抽獎說明](LOTTERY_FLOW_TEST_GUIDE.md)
- [賞品盒與訂單流程](PRIZE_BOX_WALLET_ORDER_COMPLETE_REPORT.md)

## 📞 問題排查

### 問題：返回 403 Forbidden

**原因**：JWT Token 無效或未提供

**解決**：確認 Authorization Header 格式正確

```bash
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 問題：返回「無可用獎品」

**原因**：所有獎品的 `remaining = 0`

**解決**：檢查資料庫 `lottery_prize` 表的 `remaining` 欄位

```sql
SELECT id, level, name, quantity, remaining 
FROM lottery_prize 
WHERE lottery_id = '{lotteryId}';
```

### 問題：返回「錢包餘額不足」

**原因**：`gold + bonus < pricePerDraw × count`

**解決**：先儲值或減少抽獎次數

```bash
# 查詢錢包
GET /api/wallet
Authorization: Bearer {JWT_TOKEN}
```

---

**最後更新**：2026-02-11  
**維護者**：KUJI System Team
