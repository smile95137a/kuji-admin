# 實作計畫：遊戲至訂單流程（Game-to-Order Flow）

**分支**：`004-game-to-order` | **日期**：2026-03-22 | **規格**：[spec.md](./spec.md)  
**輸入來源**：功能規格文件，路徑 `/specs/004-game-to-order/spec.md`

## 摘要

實作並驗證完整的遊戲至訂單出貨流程：  
**抽獎 → PrizeBox（`IN_BOX`）→ 玩家選擇獎品 → 依店家建立訂單 → PrizeBox（`SHIPPED`）**

核心實體（`PrizeBox`、`Order`、`OrderItem`、`LotteryDrawRecord`）以及主要的 controller/service/mapper 層**均已實作**。本計畫解決術語差異（spec 中的 `AVAILABLE` = 程式碼中的 `IN_BOX`），驗證並填補缺口（訂單取消後獎品返回、獎品盒狀態篩選、端點路徑對齊），產出已驗證的 REST API 合約，並為任何缺失邏輯準備任務清單。

## 技術背景

**語言/版本**：Java 21 + Spring Boot 3.3.3  
**主要相依套件**：Spring Security 6.x、MyBatis 3.0.5（XML mappers + MBG Example pattern）、Lombok、Jackson、JJWT  
**儲存層**：MySQL 8.3（AWS RDS），所有實體使用 UUID 主鍵  
**測試**：JUnit 5 + Spring Boot Test + Mockito  
**目標平台**：AWS EC2 Linux（Amazon Linux 2023）  
**專案類型**：REST API（web-service）— 使用者端 `/api/**`，管理端 `/api/admin/**`  
**效能目標**：獎品盒清單 p95 < 500 ms；多店家訂單建立 < 2 s  
**限制條件**：出貨/建立訂單需完整 `@Transactional` 原子性；每筆 `PrizeBox` 恰好對應一筆 `Order`；訂單建立後獎品盒項目不可刪除（僅能進行狀態轉換）  
**規模/範圍**：每位使用者獎品盒最多約 100 筆活躍項目；每位使用者訂單歷史最多約 1,000 筆

## 規範檢查

*關卡：必須在第 0 階段研究前通過。第 1 階段設計後重新確認。*

> **備註**：專案規範文件位於 `.specify/memory/constitution.md`，目前為預設空白模板，尚未針對本專案客製化。適用標準 REST API 開發實務。

**已評估的標準關卡：**

| 關卡 | 狀態 | 備註 |
|------|--------|-------|
| 無需新增第三方相依套件 | ✅ PASS | 所有必要套件（MyBatis、Spring Security、JWT）均已在 `pom.xml` 中 |
| 實體 / mappers / services 均已存在 | ✅ PASS | `PrizeBox`、`Order`、`OrderItem` 均已完整映射；services 已實作 |
| 交易邊界定義明確 | ✅ PASS | `@Transactional` 套用於 `shipPrizes()`；`createOrdersFromPrizeBox()` 包裝訂單與項目插入 |
| 認證架構不變 | ✅ PASS | JWT + `ApiJwtAuthenticationFilter` 已覆蓋 `/api/**`；無需修改 |
| 術語差異：spec `AVAILABLE` vs 程式碼 `IN_BOX` | ⚠️ RESOLVED | 已記錄於 research.md；合約全面使用 `IN_BOX` |
| 訂單取消 → 獎品返回 `IN_BOX` | ⚠️ VERIFY | `OrderServiceImpl` 中已存在取消流程；獎品返回邏輯待確認 |

**第 1 階段後重新確認**：未引入新的違規。✅

## 專案結構

### 文件（本功能）

```text
specs/004-game-to-order/
├── plan.md              # This file (/speckit.plan output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── prize-box-list.md
│   ├── prize-box-ship.md
│   └── order-list.md
└── tasks.md             # Phase 2 output (/speckit.tasks — NOT created by /speckit.plan)
```

### 原始碼（專案根目錄）

```text
src/main/java/com/group/admin/
├── controller/api/
│   ├── PrizeBoxController.java        ✅ EXISTS  GET /prize-box, POST /prize-box/ship, POST /prize-box/recycle
│   └── OrderController.java           ✅ EXISTS  POST /order/list, GET /order/{orderId}
│
├── service/
│   ├── PrizeBoxService.java           ✅ EXISTS  interface: addToPrizeBox, getPrizeBox, shipPrizes, recyclePrizes
│   ├── OrderService.java              ✅ EXISTS  interface: createOrdersFromPrizeBox, cancelOrder, …
│   └── impl/
│       ├── PrizeBoxServiceImpl.java   ✅ EXISTS  store-split ship logic, IN_BOX → SHIPPED transition
│       └── OrderServiceImpl.java      🔧 VERIFY  cancel → prize return to IN_BOX (gap to confirm)
│
├── entity/
│   ├── PrizeBox.java                  ✅ EXISTS  status: IN_BOX / SHIPPED / RECYCLED
│   ├── Order.java                     ✅ EXISTS  status: PENDING / PREPARING / SHIPPED / COMPLETED / CANCELLED
│   └── OrderItem.java                 ✅ EXISTS  links orderId ↔ prizeBoxId
│
├── mapper/
│   ├── PrizeBoxMapper.java            ✅ EXISTS  (MBG-generated + XML)
│   ├── OrderMapper.java               ✅ EXISTS
│   └── OrderItemMapper.java           ✅ EXISTS
│
├── enums/
│   ├── PrizeBoxStatusEnum.java        ✅ EXISTS  IN_BOX / SHIPPED / RECYCLED
│   └── OrderStatusEnum.java           ✅ EXISTS  PENDING / PREPARING / SHIPPED / COMPLETED / CANCELLED
│
├── req/prizebox/
│   └── PrizeBoxShipReq.java           ✅ EXISTS  prizeBoxIds, shippingMethod, recipient fields
│
└── res/
    ├── prizebox/PrizeBoxItemRes.java  ✅ EXISTS  full item DTO
    └── order/OrderDetailRes.java      ✅ EXISTS  full order DTO with OrderItemRes list

# Gaps identified — require verification / implementation:
src/main/java/com/group/admin/
├── service/impl/OrderServiceImpl.java  🔧 VERIFY  cancelOrder() must reset prizeBox.status → IN_BOX
└── controller/api/OrderController.java 🔧 VERIFY  whether GET /api/orders alias is needed vs POST /order/list
```

**結構決策**：單一 Spring Boot 專案（現有）。所有層級均已存在。實作工作為增量式：驗證取消返回流程、確認端點路徑，並新增整合測試。

## 複雜度追蹤

> 未發現規範違規 — 規範尚未針對本專案客製化。

| 項目 | 決策 |
|------|----------|
| `AVAILABLE` vs `IN_BOX` 術語 | 程式碼優先 — 全面使用 `IN_BOX`；spec/合約已同步更新 |
| `GET /api/orders` vs `POST /order/list` | 保留現有 `POST /order/list`；可選的 GET 別名延後至 tasks.md |
