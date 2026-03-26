# 實作計畫：店家管理 (Store Management)

**Branch**: `014-store-management` | **Date**: 2026-03-22 | **Spec**: [spec.md](./spec.md)
**Input**: 功能規格來自 `/specs/014-store-management/spec.md`

## 摘要

擴展現有的 `Store` 實體及相關基礎設施，以支援：(1) **原子性店家 + 負責人帳號建立**，在單一 `@Transactional` 邊界內完成；(2) **連鎖停用** — 停用店家時，批次將其所有商品設為 `OFF_SHELF`，所有橫幅設為 `DISABLED`；(3) **依角色限制編輯** — 管理員可編輯所有欄位（含負責人綁定），店家負責人只能編輯展示欄位；(4) **前台公開端點** — `/api/stores`（僅啟用的店家，卡片檢視）及 `/api/stores/{id}`（完整頁面含上架商品）。大部分基礎實作（`Store` 實體、`StoreMapper`、`StoreService`、`AdminStoreController`）已存在；本功能補全缺少的方法、強化交易安全性，並引入公開 API 控制器。

## 技術背景

**語言／版本**: Java 21  
**主要依賴套件**: Spring Boot 3.3.3, MyBatis 3.0.5, Spring Security 6 + JWT, Lombok, MapStruct/BeanUtils  
**資料庫**: MySQL 8.3  
**測試**: JUnit 5 + Spring Boot Test (`@SpringBootTest`, `@MockBean`)  
**部署目標**: AWS EC2 Linux server  
**專案類型**: REST API (web-service)  
**效能目標**: 店家列表（≤500 間店家）載入時間 < 2 秒；連鎖停用在任何商品／橫幅數量下均可在 < 10 秒內完成（SC-002/003）  
**限制**: 所有主鍵使用 UUID；店家＋負責人建立時任何失敗均需 `@Transactional` 回滾；重新啟用**不會**自動恢復商品／橫幅  
**規模範圍**: ~10–500 間店家，~50k 商品總量；無刪除功能，僅停用

## 架構規範審查

*審查閘門：必須在第 0 階段研究前通過。第 1 階段設計後需重新檢查。*

> 專案規範目前為占位模板（尚未自訂）。以下檢查依據現有程式庫慣例中的 REST API 最佳實踐執行。

| 審查項目 | 狀態 | 備註 |
|------|--------|-------|
| 每個 Controller 方法單一職責 | ✅ 通過 | 每個端點對應一個使用案例 |
| 原子寫入操作使用 `@Transactional` | ✅ 通過 | 店家＋負責人建立及連鎖停用均需要 |
| 透過 `@PreAuthorize` 強制執行權限 | ✅ 通過 | 遵循現有 `ADMIN` / `STORE_OWNER` 模式 |
| 不刪除業務實體 | ✅ 通過 | 規格明確禁止刪除店家（僅停用） |
| BLOB 欄位隔離至 `ResultMapWithBLOBs` | ✅ 通過 | `longDescription`/`remark` 模式已存在於 StoreMapper |
| 建立後負責人綁定不可變更 | ✅ 通過 | `ownerId` 已從 `STORE_OWNER` 角色的 `UpdateStoreReq` 排除 |
| 重新啟用時連鎖效應不恢復 | ✅ 通過 | 重新啟用僅變更 `store.status`；商品／橫幅維持原狀 |

**第 1 階段後重新檢查**：資料模型及 API 合約未引入任何違規。

## 專案結構

### 文件（本功能）

```text
specs/014-store-management/
├── plan.md              # This file
├── research.md          # Phase 0 — cascade patterns, transaction design
├── data-model.md        # Phase 1 — entity fields, state machine, relationships
├── quickstart.md        # Phase 1 — dev setup & manual test cheatsheet
├── contracts/           # Phase 1 — REST API contracts (6 endpoints)
│   ├── POST_admin_stores.md
│   ├── PUT_admin_stores_{id}.md
│   ├── PUT_admin_stores_{id}_status.md
│   ├── GET_admin_stores.md
│   ├── GET_api_stores.md
│   └── GET_api_stores_{id}.md
└── tasks.md             # Phase 2 output (/speckit.tasks — NOT created here)
```

### 原始碼（儲存庫根目錄）

```text
src/main/java/com/group/admin/
├── controller/
│   ├── admin/
│   │   └── AdminStoreController.java        # Extend: add createStore, updateStatus
│   └── api/
│       └── StoreController.java             # New: public /api/stores endpoints
├── service/
│   ├── StoreService.java                    # Extend: createStore, disableStore (cascade)
│   └── impl/
│       └── StoreServiceImpl.java            # Extend: atomic creation + cascade disable
├── entity/
│   └── Store.java                           # Already exists — verify all fields present
├── mapper/
│   ├── StoreMapper.java                     # Extend: batchUpdateStatusByStoreId (if needed)
│   └── StoreMapper.xml                      # Extend: batch update SQL
├── req/
│   └── store/
│       ├── CreateStoreReq.java              # New: store + owner account in one payload
│       ├── UpdateStoreReq.java              # Already exists — confirm owner-change guard
│       └── UpdateStoreStatusReq.java        # New: { status: ENABLED|DISABLED }
└── res/
    └── store/
        ├── StoreRes.java                    # Already exists — verify all fields
        ├── StoreListItemRes.java            # New: card view (logo, name, shortDesc)
        └── StoreDetailRes.java             # New: full page view + ON_SHELF products

src/test/java/com/group/admin/
└── service/
    └── StoreServiceTest.java               # New: unit tests for cascade, atomic creation
```

**架構決策**：單一 Spring Boot 專案（現有架構）。新檔案遵循既有的 `controller/admin/`、`controller/api/`、`service/impl/`、`req/`、`res/` 目錄結構。

## 複雜度追蹤

> 本功能未引入任何架構規範違規。
