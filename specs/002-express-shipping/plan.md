# 實作計畫：物流與出貨管理 (Express Shipping)

**分支**：`002-express-shipping` | **日期**：2026-03-22 | **規格**：[spec.md](./spec.md)
**輸入**：功能規格來自 `/specs/002-express-shipping/spec.md`

## 摘要

為 KUJI-Server 的獎品配送流程實作完整的物流與出貨管理功能。玩家可選擇宅配到府或超商取貨（7-11 / FamilyMart），在從獎品箱下單時填寫收件人資訊，並可追蹤訂單狀態。店家負責人與管理員透過單向狀態機（PENDING → PREPARING → SHIPPED → COMPLETED）管理配送流程，並可在訂單進入 SHIPPED 狀態前取消訂單。

Order 實體、狀態列舉、出貨方式列舉以及核心服務方法**已存在於程式碼庫中**。本功能填補剩餘缺口：玩家端「提交出貨資訊」端點、管理員統一狀態轉換端點、針對兩個空測試類別的完整測試覆蓋、驗證強化，以及從已儲存個人資料預填地址的玩家可見流程。

## 技術背景

**語言/版本**：Java 21  
**主要依賴**：Spring Boot 3.3.3、MyBatis 3.0.5、Spring Security 6、JWT (jjwt 0.9.1)、Lombok、Jakarta Validation  
**儲存**：MySQL 8.3 — 所有實體透過 MyBatis 對映；UUID 主鍵（String），時間戳記為 `LocalDateTime`  
**測試**：JUnit 5 + Spring Boot Test + Mockito（單元），MockMvc（控制器切片）  
**目標平台**：AWS EC2 Linux 伺服器  
**專案類型**：REST API（Web 服務）— 兩個安全域：`/api/**`（USER 角色，ApiJwtAuthenticationFilter）與 `/admin/**`（ADMIN / STORE_OWNER / STORE_EDITOR 角色，AdminJwtAuthenticationFilter）  
**效能目標**：標準 REST — 清單查詢 p95 低於 300ms；無特殊吞吐量目標  
**限制**：狀態機嚴格單向；本模組不含付款邏輯；v1 不整合真實超商 API（僅記錄資料）  
**規模/範圍**：每間店單租戶；預計上線初期每日訂單量低於 10,000 筆

## 架構規範檢核

*關卡：必須在第 0 階段研究前通過。第 1 階段設計後重新確認。*

> **備註**：專案架構規範仍為範本形式（佔位內容）。以下關卡源自本程式碼庫中一致套用的 Spring Boot 服務通用原則。

| 關卡 | 狀態 | 備註 |
|------|--------|-------|
| 新實體需有對應的 mapper + XML | ✅ 通過 | 所有必要實體（Order、OrderStatusLog）已存在，並有完整的 mapper 覆蓋 |
| 狀態轉換嚴格單向並在服務層驗證 | ✅ 通過 | `OrderStatusEnum.isCancellable()` / `isFinished()` + 服務層守衛已就位 |
| 控制器層強制角色存取控制 | ✅ 通過 | `@PreAuthorize` 已存在；取消操作限制為 `ADMIN` |
| 無 mapper/repository 以外的直接 DB 存取 | ✅ 通過 | 所有資料存取均透過 `OrderMapper`、`OrderRepository`、`OrderStatusLogMapper` |
| 所有入站 DTO 均有驗證 | ⚠️ 部分完成 | `OrderShipReq` 僅驗證 `trackingNo`；玩家出貨資訊請求（出貨方式、收件人欄位）需要驗證注解 |
| 控制器測試覆蓋 | ⚠️ 部分完成 | `OrderControllerTest` 與 `AdminOrderControllerTest` 均存在但為空 — 必須補充 |

**設計後重新確認**：第 1 階段設計完成後，重新確認新的 `ShipInfoReq` DTO（玩家提交出貨資訊）是否在必填欄位上帶有完整的 `@NotBlank` / `@NotNull` 驗證（符合 FR-001 – FR-003）。

## 專案結構

### 文件（本功能）

```text
specs/002-express-shipping/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   ├── player-submit-shipping.md
│   ├── admin-update-status.md
│   ├── player-get-orders.md
│   └── admin-cancel-order.md
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### 原始碼（儲存庫根目錄）

```text
src/main/java/com/group/admin/
├── controller/
│   ├── api/
│   │   └── OrderController.java          # GET /order/list, GET /order/{id}
│   │                                     # + NEW: POST /order/{id}/shipping-info
│   └── admin/
│       └── AdminOrderController.java     # existing + NEW: PUT /admin/orders/{id}/status
├── req/
│   └── order/
│       ├── OrderShipReq.java             # existing (admin tracking no.)
│       ├── OrderCancelReq.java           # existing
│       └── ShipInfoReq.java              # NEW: player submits shipping info
├── res/
│   └── order/
│       ├── OrderRes.java                 # existing
│       └── OrderDetailRes.java           # existing
├── service/
│   ├── OrderService.java                 # + NEW method: submitShippingInfo()
│   └── impl/
│       └── OrderServiceImpl.java         # implementation
├── enums/
│   ├── OrderStatusEnum.java              # existing — no changes needed
│   └── ShippingMethodEnum.java           # existing — no changes needed
└── entity/
    ├── Order.java                        # existing — no changes needed
    └── OrderStatusLog.java               # existing — no changes needed

src/test/java/com/group/admin/
├── controller/
│   ├── api/
│   │   └── OrderControllerTest.java      # FILL (currently empty)
│   └── admin/
│       └── AdminOrderControllerTest.java # FILL (currently empty)
└── service/
    └── OrderServiceTest.java             # NEW: unit tests for state machine
```

**結構決策**：單一 Spring Boot 專案。所有新程式碼延伸現有套件 — 無新模組、無新資料庫表格。唯一的結構新增為一個新的請求 DTO（`ShipInfoReq`）、一個新的服務方法、一個新的控制器端點，以及補充後的測試類別。

## 複雜度追蹤

> 無架構規範違規 — 全程遵循現有架構模式。
