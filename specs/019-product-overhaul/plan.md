# 實作計畫：商品管理重整

**Branch**: `019-product-overhaul` | **Date**: 2026-04-30 | **Spec**: [spec.md](./spec.md)  
**Input**: 來自 `/specs/019-product-overhaul/spec.md` 的功能規格

**Note**: 目前 Git 分支仍為 `main`。本次 `/speckit-plan` 透過 `SPECIFY_FEATURE=019-product-overhaul` 與 `SPECIFY_FEATURE_DIRECTORY=specs/019-product-overhaul` 指向既有 feature 目錄完成規劃，不另外切換分支。

## Summary

本功能將商品管理的建立/更新規則重新收斂到 `Lottery` 聚合：新增 `paymentType`、`freeDrawThreshold`、`delistStrategy`，移除舊多抽與保護欄位的對外依賴，並依 `category + subCategory` 自動推導 `playMode`、`gameMode`、`delistStrategy`。同時整理 controller 邊界，將後台 `with-prizes` 能力收斂回 `AdminLotteryController`，前台則以既有 `LotteryController` 路徑相容性為主，避免再拆出新的 detail controller。

## Technical Context

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.3.3、Spring Web/Security/Validation/AOP/Actuator、MyBatis Spring Boot Starter 3.0.5、springdoc OpenAPI、Lombok  
**Storage**: MySQL `lottery` 與相關獎品資料表，MyBatis XML mapper；測試使用 H2  
**Testing**: JUnit 5、Spring Boot Test、Spring Security Test、H2、既有 integration tests  
**Target Platform**: Spring Boot REST API 服務（admin 後台 + public API），部署於 JVM 21 環境  
**Project Type**: 單體式 Web Service  
**Performance Goals**: 建立/更新商品與抽獎後下架檢查不得新增全表掃描；`checkAndDelist()` 僅針對單一 lottery 與必要獎品統計執行；既有列表/詳情 API 路徑與回應格式保持相容  
**Constraints**: 必須遵守 DDL-first + MBG 流程；驗證邏輯集中在 Service 層；`freeDrawThreshold` 僅適用 `CUSTOM_GACHA + SCRATCH_MODE` 且可為 `NULL`；合併 controller 不得改變既有對外路徑  
**Scale/Scope**: 1 個核心聚合（`Lottery`）、3 個新增欄位、4 個廢棄欄位、後台商品 controller 收斂、public 商品讀取相容性驗證、抽獎後自動下架規則統一化

## Constitution Check

*GATE: 必須在 Phase 0 研究前通過，並於 Phase 1 設計後複檢。*

| 關卡 | Phase 0 | Phase 1 | 備註 |
|------|---------|---------|------|
| 憲章可執行性 | ⚠️ | ⚠️ | `.specify/memory/constitution.md` 仍為預設模板，未形成可執行硬性 gate；本次改以 `AGENTS.md` 倉庫規範與現有專案慣例作代理檢查 |
| Java / Spring 一致性 | ✅ | ✅ | 維持 Java 21 + Spring Boot 3.3.3 + Service 層驗證，不引入額外執行框架 |
| DDL-first / MBG | ✅ | ✅ | 先調整 SQL 與 mapper，再修正 entity / req / res / service |
| API 相容性 | ✅ | ✅ | `AdminLotteryController` 與 `LotteryController` 對外路徑保持穩定，合併僅調整實作歸屬 |
| 測試完整性 | ✅ | ✅ | 需覆蓋 create/update 驗證、刮刮樂空值邏輯、固定下架策略、自動下架狀態流轉 |

**Gate Result**: PASS（以專案規範代理憲章檢查）

## Project Structure

### Documentation (this feature)

```text
specs/019-product-overhaul/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── admin-lottery-api.md
│   └── public-lottery-api.md
└── tasks.md
```

### Source Code (repository root)

```text
src/main/java/com/group/admin/
├── controller/
│   ├── admin/
│   │   ├── AdminLotteryController.java
│   │   └── AdminLotteryWithPrizesController.java
│   └── api/
│       ├── LotteryController.java
│       ├── LotteryBrowseController.java
│       ├── LotteryDrawController.java
│       ├── DrawController.java
│       └── RandomDrawController.java
├── entity/
│   └── Lottery.java
├── mapper/
│   └── LotteryMapper.java
├── req/lottery/
│   ├── LotteryCreateReq.java
│   └── LotteryUpdateReq.java
├── res/lottery/
│   └── LotteryRes.java
└── service/
    ├── draw/
    └── impl/
        ├── LotteryServiceImpl.java
        └── LotteryTicketServiceImpl.java

src/main/resources/
└── mapper/
    └── LotteryMapper.xml

src/test/java/com/group/admin/
├── controller/
├── integration/
└── service/

sql/
docs/
frontend/
```

**Structure Decision**: 採單體式 Spring Boot 後端結構，文件產物放在 `specs/019-product-overhaul/`；實作變更集中於 `Lottery` 相關 req/res/entity/service/controller/mapper 與對應測試，不另外拆出新模組。

## Complexity Tracking

目前無需額外複雜度豁免。已識別的複雜度集中於「分類導向的欄位正規化」與「抽獎後自動下架回歸驗證」，可透過 service 規則集中化與測試補強控制。
