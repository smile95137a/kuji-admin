# Implementation Plan: 訂單管理 (Order Management)

**Branch**: `008-order-management` | **Date**: 2026-03-22 | **Spec**: [spec.md](./spec.md)  
**Input**: 功能規格來自 `/specs/008-order-management/spec.md`

## 摘要

實作完整的訂單管理系統，允許玩家從獎品盒出貨獎品，並讓店家負責人／管理員管理訂單生命週期。訂單遵循嚴格的單向狀態機（待處理 → 備貨中 → 已出貨 → 已完成）。強制執行店家隔離：店家負責人僅能看到自己店家的訂單。取消（出貨前）會將獎品退回獎品盒並標示為 AVAILABLE；點數不退還。三張核心資料表（`order`、`order_item`、`order_status_log`）已存在於資料庫 schema 中；實作任務為完成 service 邏輯、controller endpoints、DTO 及測試覆蓋。

## 技術背景

**語言／版本**：Java 21  
**主要相依套件**：Spring Boot 3.3.3、MyBatis 3.0.5、Spring Security 6 + JWT、Lombok、SpringDoc OpenAPI (Swagger)  
**儲存**：MySQL 8.3（AWS RDS）—— 資料表 `order`、`order_item`、`order_status_log`、`prize_box`  
**測試**：JUnit 5 + Spring Boot Test（`@SpringBootTest`）+ Mockito  
**目標平台**：AWS EC2 Linux 伺服器（NGINX 反向代理後端）  
**專案類型**：REST API（web-service）  
**效能目標**：列表查詢（分頁）p95 < 200 ms；建立訂單時原子性扣除獎品盒數量  
**限制條件**：店家隔離為強制要求（零跨店資料洩漏）；一旦出貨後，狀態轉換不可逆；獎品盒扣除必須為交易式（全成功或全失敗）  
**規模／範圍**：多租戶；每家店每日可能有數百筆訂單；管理員可查看全平台彙總資料

## 規範檢查

*關卡：必須在 Phase 0 研究前通過。Phase 1 設計後重新確認。*

> 專案規範目前為佔位符模板（尚無領域特定原則已被採納）。無規範違反需評估。以下設計決策遵循現有程式碼庫慣例（MyBatis + Spring Security 角色驗證 + UUID PK + `SecurityUtils` 取得呼叫者身分）。

**設計後重新確認**（Phase 1 之後）：✅ 所有決策與現有模式一致——未引入新相依套件，無架構層面偏離。

## 專案結構

### 文件（此功能）

```text
specs/008-order-management/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── admin-orders-api.md
│   └── player-orders-api.md
└── tasks.md             # Phase 2 output (/speckit.tasks — NOT created by /speckit.plan)
```

### 原始碼（儲存庫根目錄）

```text
src/main/java/com/group/admin/
├── controller/
│   ├── admin/
│   │   └── AdminOrderController.java        ← PUT /admin/orders/{id}/status, DELETE, GET list/detail
│   └── api/
│       └── OrderController.java             ← POST /order/ship, GET /order/list, GET /order/{id}
├── service/
│   ├── OrderService.java                    ← 介面
│   └── impl/
│       └── OrderServiceImpl.java            ← @Transactional 邏輯、狀態機、獎品盒連結
├── entity/
│   ├── Order.java                           ← 已存在
│   ├── OrderItem.java                       ← 已存在
│   └── OrderStatusLog.java                  ← 已存在（稽核軌跡）
├── mapper/
│   ├── OrderMapper.java                     ← 已存在
│   ├── OrderItemMapper.java                 ← 已存在
│   └── OrderStatusLogMapper.java            ← 已存在
├── req/order/
│   ├── CreateOrderReq.java                  ← 玩家：獎品盒 ID + 運送資訊
│   ├── UpdateOrderStatusReq.java            ← 管理員／店家：下一個狀態 + 備註（可選）
│   └── CancelOrderReq.java                  ← 取消原因欄位
├── res/order/
│   ├── OrderRes.java                        ← 列表檢視
│   └── OrderDetailRes.java                  ← 完整詳情，含項目 + 狀態歷程
├── condition/
│   └── OrderCondition.java                  ← storeId、userId、status、日期範圍篩選
└── enums/
    └── OrderStatusEnum.java                 ← 已存在：PENDING/PREPARING/SHIPPED/COMPLETED/CANCELLED

src/main/resources/mapper/
├── OrderMapper.xml                          ← 已存在
├── OrderItemMapper.xml                      ← 已存在
└── OrderStatusLogMapper.xml                 ← 已存在

src/test/java/com/group/admin/
├── service/
│   └── OrderServiceTest.java               ← 狀態機 + 取消邏輯的單元測試
└── controller/
    └── AdminOrderControllerTest.java        ← 店家隔離的整合測試
```

**結構決策**：單一 Spring Boot 專案，沿用現有原始碼樹。不新增模組。所有新程式碼遵循現有的 `controller/admin` + `controller/api` 雙命名空間模式（Lottery、Store 及 PrizeBox 功能皆使用此模式）。

## 複雜度追蹤

> 無規範違反——表格保留空白。

| 違反項目 | 必要原因 | 拒絕更簡單替代方案的理由 |
|---------|---------|----------------------|
| — | — | — |
