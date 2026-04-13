# 實作計畫：系統參數管理

**Branch**: `016-system-config` | **日期**: 2026-04-13 | **規格**: [spec.md](./spec.md)
**輸入**：功能規格來自 `/specs/016-system-config/spec.md`

## 摘要

建立系統級別的參數管理模組，將目前散落在程式碼中的 hardcode 常數（保護時間、抽獎上限等）抽離至可管理的 DB 表，並提供後台管理 API 讓 ADMIN 即時調整。其他模組透過 `SystemConfigService.getInt()/getString()/getBoolean()` 讀取參數值，帶有預設值 fallback 確保穩定性。

## 技術背景

**語言／版本**：Java 21
**主要依賴**：Spring Boot 3.3.3, MyBatis 3.0.5, Spring Security 6, JWT, Lombok
**儲存**：MySQL 8.3
**測試**：JUnit 5 + Mockito
**專案類型**：REST API (web-service)

## 架構規範檢查

| 關卡 | 狀態 | 備註 |
|------|------|------|
| DDL-first 原則 | ✅ 通過 | 先建表再 MBG |
| Controller → Service → Mapper 分層 | ✅ 通過 | 嚴格分層 |
| ApiResponse 自動包裝（AOP） | ✅ 通過 | 不手動建立 ApiResponse |
| UUID 主鍵策略 | ✅ 通過 | |
| 樂觀鎖並發控制 | ✅ 通過 | version 欄位 |
| 權限控制（@PreAuthorize） | ✅ 通過 | 僅 ADMIN |

## 專案結構

### 文件

```text
specs/016-system-config/
├── spec.md
├── plan.md              ← this file
├── data-model.md
├── tasks.md
└── contracts/
    ├── GET_admin_system-config.md
    ├── POST_admin_system-config.md
    ├── PUT_admin_system-config_{id}.md
    └── DELETE_admin_system-config_{id}.md
```

### 原始碼

```text
src/main/java/com/group/admin/
├── entity/SystemConfig.java                          (MBG 生成)
├── mapper/SystemConfigMapper.java                    (MBG 生成)
├── example/SystemConfigExample.java                  (MBG 生成)
├── req/systemconfig/SystemConfigCreateReq.java       (新建)
├── req/systemconfig/SystemConfigUpdateReq.java       (新建)
├── res/systemconfig/SystemConfigRes.java             (新建)
├── condition/SystemConfigCondition.java              (新建)
├── service/SystemConfigService.java                  (新建)
├── service/impl/SystemConfigServiceImpl.java         (新建)
└── controller/admin/AdminSystemConfigController.java (新建)

src/main/resources/mapper/SystemConfigMapper.xml      (MBG 生成)
```

## 複雜度追蹤

| 面向 | 預估 | 說明 |
|------|------|------|
| 新增實體 | 1 | SystemConfig |
| 新增 API | 4 | CRUD |
| 修改既有程式碼 | 3 | LotteryTicketServiceImpl、LotteryDrawController、RandomDrawController |
| 預估工時 | 0.5 天 | 結構簡單、邏輯低複雜度 |

## 風險

- 快取一致性：若加入快取，需確保更新時清除快取。初期不加快取，直接查 DB。
- 參數命名衝突：config_key 使用 UNIQUE 約束確保唯一。
