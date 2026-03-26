# 實作計畫：遊戲管理（抽獎機制）

**Branch**: `003-game-management` | **日期**: 2026-03-22 | **規格**: [spec.md](./spec.md)  
**輸入**：功能規格來自 `/specs/003-game-management/spec.md`

## 摘要

實作 KUJI（一番賞）抽獎平台的核心抽獎執行機制。功能涵蓋：固定獎品池等機率抽獎（1/N）、每個抽獎活動的開啟者保護鎖（預設 5 分鐘）、最後賞保證機制、大獎售罄後自動降價，以及完整的抽獎記錄稽核軌跡。

相關實體（`Lottery`、`LotteryPrize`、`LotteryDrawRecord`、`LotteryLock`）已存在於程式碼庫中。本計畫將強化抽獎演算法、加入適當的 DB 層樂觀鎖以確保並發安全性、將最後賞邏輯整合至 `DrawService`、加入自動折扣觸發機制，並開放所需的 REST 端點。

## 技術背景

**語言／版本**：Java 21  
**主要依賴**：Spring Boot 3.3.3, MyBatis 3.0.5, Spring Security 6, JWT, Lombok  
**儲存**：MySQL 8.3 (AWS RDS)  
**測試**：JUnit 5 + Spring Boot Test + Mockito  
**目標平台**：AWS EC2 Linux (Amazon Linux 2023)  
**專案類型**：REST API (web-service)  
**效能目標**：單次抽獎 < 2 s p95 (SC-005)  
**限制**：生產環境零過度抽獎 (SC-001)；原子性抽獎交易  
**規模／範圍**：每個抽獎活動約 10 名並發玩家；典型獎品池大小為 80 抽

## 架構規範檢查

*關卡：必須在第 0 階段研究前通過。第 1 階段設計後重新檢查。*

> **注意**：專案規範檔案為空白模板——尚無填寫的原則。  
> 以下關卡來自程式碼庫中觀察到的專案慣例。

| 關卡 | 狀態 | 備註 |
|------|--------|-------|
| 實體已存在（`Lottery`、`LotteryPrize`、`LotteryDrawRecord`、`LotteryLock`） | ✅ 通過 | 不需要新資料表；強化既有資料表 |
| 所有新程式碼遵循既有的 `ApiResponse<T>` 包裝格式 | ✅ 通過 | 所有 Controller 強制使用 |
| 所有抽獎操作加上 `@Transactional` | ✅ 通過 | 原子性的關鍵要求（FR-010） |
| MyBatis XML Mapper 風格（非註解方式） | ✅ 通過 | 既有專案慣例 |
| 所有實體使用 UUID 主鍵 | ✅ 通過 | 既有慣例 |
| 無硬式編碼魔術數字（鎖定時間可設定） | ✅ 通過 | 每個抽獎活動使用 `Lottery.protectionMinutes` |
| 排程清理已過期的鎖定 | ✅ 通過 | FR-006 / 既有 `scheduler` 套件 |

**設計後重新檢查**：通過——第 1 階段設計未引入新的反模式。

## 專案結構

### 文件（本功能）

```text
specs/003-game-management/
├── plan.md              ← this file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   ├── draw-api.md
│   ├── lock-status-api.md
│   └── admin-draw-history-api.md
└── tasks.md             ← Phase 2 output (speckit.tasks)
```

### 原始碼（儲存庫根目錄）

```text
src/main/java/com/group/admin/
├── entity/
│   ├── Lottery.java                    (existing — read only)
│   ├── LotteryPrize.java               (existing — read only)
│   ├── LotteryDrawRecord.java          (existing — enhance)
│   └── LotteryLock.java                (existing — enhance)
├── service/
│   ├── DrawService.java                (existing interface — harden)
│   ├── LotteryLockService.java         (existing interface — harden)
│   └── impl/
│       ├── DrawServiceImpl.java        (core logic — new/rewrite)
│       └── LotteryLockServiceImpl.java (existing — fix race condition)
├── controller/
│   ├── api/
│   │   ├── LotteryDrawController.java  (existing — extend endpoints)
│   │   └── LotteryLockController.java  (new — lock-status endpoint)
│   └── admin/
│       └── AdminDrawHistoryController.java  (new — draw history)
├── scheduler/
│   └── LockCleanupScheduler.java       (new — FR-006)
├── req/
│   ├── DrawReq.java                    (new)
│   └── AdminDrawHistoryReq.java        (new)
├── res/
│   ├── DrawResultRes.java              (existing — extend if needed)
│   └── LockStatusRes.java              (new)
└── mapper/
    ├── LotteryDrawRecordMapper.java    (existing — add queries)
    └── LotteryLockMapper.java          (existing — add queries)

src/test/java/com/group/admin/
├── service/
│   ├── DrawServiceImplTest.java        (new)
│   └── LotteryLockServiceImplTest.java (new)
└── controller/
    └── LotteryDrawControllerTest.java  (new)
```

**架構決策**：單一 Spring Boot 專案，沿用既有 Maven 結構。不新增模組。

## 複雜度追蹤

> 未偵測到架構規範違規——依模板說明省略表格。
