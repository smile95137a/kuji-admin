# 實作計畫：抽獎票券系統（雙號碼與刮刮樂機制）

**分支**: `005-lottery-ticket-system` | **日期**: 2026-03-22 | **規格**: [spec.md](spec.md)  
**輸入**: 功能規格來自 `/specs/005-lottery-ticket-system/spec.md`

## 摘要

實作支援三種遊戲模式的雙號碼抽獎票券系統 — **RANDOM**（一番賞/扭蛋）、**SCRATCH_STORE**（店家指定大獎）與 **SCRATCH_PLAYER**（開套玩家指定大獎）。每個抽獎活動預先生成 N 張票券，包含兩個獨立號碼空間：`ticket_number`（1-N，玩家看到的實體格子）與 `revealed_number`（僅限刮刮樂，獨立洗牌的 1-N，抽出前隱藏）。`LotterySession` 追蹤開套回合的生命週期、保護視窗與免費抽獎退款資格。三種模式共用同一個 `LotteryTicket` 實體；模式差異由 `LotteryTicketServiceImpl` 處理，並在 `LotteryDrawController` 中強制執行。

系統已具備大量基礎設施（`LotteryTicket`、`LotterySession`、`LotteryDrawController`、`LotteryTicketServiceImpl`）。本功能補足剩餘缺口：對 GET 票券 API 強制執行嚴格的資訊隱藏、完成 SCRATCH_PLAYER 指定閘門、強化並發開套玩家鎖定，以及新增 `designation-check` 端點。

## 技術背景

**語言/版本**: Java 21  
**主要相依套件**: Spring Boot 3.3.3, MyBatis 3.0.5 (mybatis-spring-boot-starter), Spring Security + JWT (jjwt 0.9.1), Lombok 1.18.32, Springdoc OpenAPI 2.3.0  
**儲存**: MySQL 8.3 (AWS RDS)  
**測試**: JUnit 5 + Spring Boot Test (`@SpringBootTest`, `@MockBean`, `@Transactional`)  
**目標平台**: AWS EC2 Linux 伺服器（Java 21 執行環境）  
**專案類型**: REST API（網路服務）— 單一 Spring Boot 單體架構  
**效能目標**: 抽獎回應 < 500 ms p95；自動分配剩餘獎品 < 2 秒（SC-004）  
**限制條件**: AVAILABLE 票券零獎品資訊洩漏（SC-001, FR-005/FR-006）；零並發抽獎衝突（SC-005, FR-012）；退款準確率 100%（SC-003）  
**規模/範圍**: 每個抽獎活動的並發控制（依 lotteryId 使用 synchronized 或 DB 層級鎖）；典型 N = 20–200 張票券

## 架構規範檢查

*閘門：必須在第 0 階段研究前通過。第 1 階段設計後重新確認。*

> constitution.md 目前為佔位模板，尚未批准任何專案特定規則。因此架構規範閘門依據現有程式庫慣例中隱含的一般最佳實踐原則進行評估。

| 閘門 | 狀態 | 備註 |
|------|--------|-------|
| 未引入新的頂層專案/模組 | ✅ 通過 | 所有變更均在現有 `com.group.admin` 單體架構內 |
| 實體盡量重用現有結構 | ✅ 通過 | `LotteryTicket`、`LotterySession` 已存在；若需要僅新增欄位 |
| 安全性：AVAILABLE 票券絕不暴露獎品/revealedNumber 資料 | ✅ 通過 | 在 `getTicketsForFrontend()` 及 response DTO 中強制執行 |
| 並發控制：無重複獎品分配 | ✅ 通過 | DB 層級 `SELECT … FOR UPDATE` 或依 lotteryId 使用 synchronized 區塊 |
| 無硬式編碼的機密或憑證 | ✅ 通過 | 設定於 `application-*.yml`；憑證透過 AWS parameter store 管理 |

**第 1 階段後重新確認**：未引入違規項目。不需要複雜度表。

## 專案結構

### 文件（本功能）

```text
specs/005-lottery-ticket-system/
├── plan.md              ← this file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   ├── GET-lottery-id-tickets.md
│   ├── POST-lottery-id-draw.md
│   ├── POST-lottery-id-designate.md
│   └── GET-lottery-id-designation-check.md
└── tasks.md             ← Phase 2 output (/speckit.tasks — NOT created here)
```

### 原始碼（儲存庫根目錄）

```text
src/main/java/com/group/admin/
├── controller/
│   └── api/
│       ├── LotteryDrawController.java          ← ADD: GET /tickets, GET /designation-check
│       └── LotteryBrowseController.java        ← review existing ticket-list endpoint
├── service/
│   ├── LotteryTicketService.java               ← ADD: getTickets(), isDesignationRequired()
│   └── impl/
│       └── LotteryTicketServiceImpl.java       ← MODIFY: enforce info-hiding, add designation gate
├── entity/
│   ├── LotteryTicket.java                      ← review; no schema changes expected
│   └── LotterySession.java                     ← review; LOCKED status may need adding
├── res/
│   └── lottery/
│       ├── TicketListResponse.java             ← ADD: filtered view (no prizeId/revealedNumber for AVAILABLE)
│       └── DesignationCheckResponse.java       ← ADD
└── mapper/
    └── LotteryTicketMapper.xml                 ← review; add batch-update query if missing

src/test/java/com/group/admin/
└── service/
    └── LotteryTicketServiceTest.java           ← ADD: unit tests for all four user stories
```
