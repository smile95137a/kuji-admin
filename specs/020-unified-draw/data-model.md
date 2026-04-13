# Data Model: 統一抽獎系統

**Feature**: `020-unified-draw`
**Date**: 2026-04-13

> 不新增資料表。本文件描述 Strategy Pattern 的架構設計與 LotterySession 欄位調整。

---

## LotterySession 欄位調整

### 移除讀取來源

| 原欄位 | 原來源 | 新來源 |
|--------|--------|--------|
| `protectionDraws` | `lottery.protectionDraws` → session | `lottery.freeDrawThreshold`（免單門檻） |
| — | session.protectionDraws | `systemConfigService.getInt("protection_initial_minutes", 5)`（保護時間） |

### 保留不變欄位

| 欄位 | 用途 |
|------|------|
| `protectionEndTime` | 保護到期時間（由 system_config 參數計算） |
| `freeDrawEnabled` | 是否啟用免單（複製自 lottery） |
| `freeDrawTriggered` | 是否已觸發免單 |
| `freeDrawRefundAmount` | 免單退款金額 |
| `freeDrawTriggeredAt` | 免單觸發時間 |
| `freeDrawPrizeId` | 免單觸發的獎品 ID |
| `openerUserId` | 開套者 ID |
| `openerTotalCost` | 開套者累計花費 |

---

## 免單機制 — 條件判斷流程

```
checkAndTriggerFreeDraw(sessionId, prizeId):
  1. session.freeDrawEnabled == 1?        → 否則跳過
  2. session.freeDrawTriggered == 0?       → 已觸發過則跳過
  3. isOpener(currentUserId)?              → 非開套者跳過
  4. protectionEndTime > now?              → 保護過期 = 放棄
  5. openerDrawCount <= lottery.freeDrawThreshold?  → 超過門檻跳過（★ 改用 freeDrawThreshold）
  6. prizeId != null?                      → 沒中獎跳過
  7. prize.isGrandPrize == 1?              → 非大獎跳過
  
  ✅ 全部通過 → 退還 openerTotalCost → 商品下架 → 標記 freeDrawTriggered=1
```

---

## 保護時間延長機制

```
extendProtection(sessionId, lottery):
  initial = systemConfigService.getInt("protection_initial_minutes", 5)
  extension = systemConfigService.getInt("protection_extension_minutes", 2)
  maxMinutes = systemConfigService.getInt("protection_max_minutes", 10)
  
  protectionStartTime = session.protectionStartTime  // 第一次建立保護的時間
  maxEndTime = protectionStartTime + maxMinutes
  newEndTime = now + extension
  
  if newEndTime > maxEndTime:
    newEndTime = maxEndTime   // 封頂
  
  if newEndTime > session.protectionEndTime:
    session.protectionEndTime = newEndTime  // 只延長不縮短
```

---

## Strategy Pattern 類別圖

```
DrawController
  └── DrawStrategyFactory
        ├── GachaDrawStrategy      (category: GACHA)
        │     └── uses: LotteryPrizeMapper, CoinService, LotteryDrawRecordMapper
        ├── TicketDrawStrategy     (category: OFFICIAL_ICHIBAN, TRADING_CARD)
        │     └── uses: LotteryTicketMapper, LotterySessionMapper, CoinService
        └── ScratchDrawStrategy    (category: CUSTOM_GACHA)
              └── uses: LotteryTicketMapper, LotterySessionMapper, CoinService, FreeDraw logic
```

所有 Strategy 共享：
- `CoinService` — 扣款/退款
- `SystemConfigService` — 讀取保護時間參數
- `LotteryDrawRecordMapper` — 記錄抽獎
- `LotteryMapper` — 讀取商品資訊
