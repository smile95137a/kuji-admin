# 實作計畫：統一抽獎系統

**Branch**: `020-unified-draw` | **日期**: 2026-04-13 | **規格**: [spec.md](./spec.md)

## 摘要

將目前分散的兩套抽獎系統（DrawService 加權隨機 + LotteryTicketService 籤位制）統一為 Strategy Pattern 架構。建立統一的 `POST /api/lottery/{id}/draw` 入口，依商品 category 派發至 GachaDrawStrategy、TicketDrawStrategy、ScratchDrawStrategy。同時重構保護時間為讀 system_config 並支援自動延長，修正免單機制使用新的 freeDrawThreshold 欄位。

## 技術背景

**語言／版本**：Java 21
**前置依賴**：Spec 016 + 017 + 018 + 019 全部完成
**風險等級**：高（核心遊戲邏輯重構）

## 架構規範檢查

| 關卡 | 狀態 | 備註 |
|------|------|------|
| @Transactional 原子操作 | ✅ | 每次抽獎在同一事務內 |
| 並發控制（SELECT FOR UPDATE + synchronized） | ✅ | 防止超額抽獎 |
| Controller → Service 分層 | ✅ | Controller 只做路由，Strategy 做業務 |
| SecurityUtils 取得 userId | ✅ | JWT 驗證 |
| AOP 自動包裝回應 | ✅ | |

## 專案結構

### 新建檔案

```text
src/main/java/com/group/admin/
├── service/draw/
│   ├── DrawStrategy.java              (介面)
│   ├── DrawStrategyFactory.java       (Factory)
│   ├── GachaDrawStrategy.java         (扭蛋)
│   ├── TicketDrawStrategy.java        (一番賞/卡牌)
│   └── ScratchDrawStrategy.java       (刮刮樂)
├── req/draw/DrawRequest.java          (統一 DTO)
└── controller/api/DrawController.java (統一入口)
```

### 刪除檔案

```text
src/main/java/com/group/admin/controller/api/
├── RandomDrawController.java          (刪除)
└── LotteryDrawController.java         (刪除)
```

### 可選標記廢棄

```text
src/main/java/com/group/admin/service/
├── DrawService.java / impl/DrawServiceImpl.java       (邏輯遷至 GachaDrawStrategy)
└── impl/LotteryTicketServiceImpl.java                  (draw 方法遷移至 Strategy)
```

## 複雜度追蹤

| 面向 | 預估 |
|------|------|
| 新建 Strategy 類別 | 5 個（介面 + Factory + 3 實作） |
| 新建 Controller | 1 個 |
| 刪除 Controller | 2 個 |
| 邏輯遷移量 | ~500 行（DrawServiceImpl + LotteryTicketServiceImpl 核心方法） |
| 預估工時 | 2-3 天（最大且最複雜的重構） |

## 風險

- 邏輯遷移過程中遺漏邊界條件 → 需對照原始碼逐行遷移
- 保護時間延長的並發安全 → 必須在 @Transactional 內處理
- 免單機制改用 freeDrawThreshold 後與舊 protectionDraws 邏輯的差異 → 需完整測試

## API Contract

### POST /api/lottery/{lotteryId}/draw

```
Request: DrawRequest { count, ticketNumber, tickets }
Response: DrawResultRes { draws, lotteryRemaining, lockAcquired, ... }

扭蛋：{ count: 3 }
一番賞/卡牌：{ ticketNumber: 5 }
刮刮樂：{ ticketNumber: 8 }
多張：{ tickets: ["uuid1", "uuid2"] }
```

### POST /api/lottery/{lotteryId}/designate

保留不變（刮刮樂 SCRATCH_PLAYER 大獎指定）。

### GET /api/lottery/{lotteryId}/session

保留不變。
