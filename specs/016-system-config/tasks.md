# 任務清單：系統參數管理

**輸入**：設計文件來自 `/specs/016-system-config/`
**參考文件**：plan.md、spec.md、data-model.md、contracts/
**分支**：`016-system-config` | **建立日期**：2026-04-13

**格式說明**：
- **[P]**：可平行執行（不同檔案，無未完成的前置依賴）
- **[USn]**：任務所屬使用者故事
- 任務描述中包含精確檔案路徑

---

## 第一階段：DDL 與 MBG 生成

**目的**：建立 system_config 資料表，透過 MBG 生成 Entity/Mapper/Example 三件套。

- [ ] T001 執行 DDL：在 MySQL 建立 `system_config` 表（依 data-model.md 的 DDL）
- [ ] T002 插入初始資料：4 筆參數（protection_initial_minutes、protection_extension_minutes、protection_max_minutes、max_draws_per_request）
- [X] T003 更新 `src/main/resources/generatorConfig.xml`：加入 `system_config` 表的 MBG 設定
- [ ] T004 執行 `mvn mybatis-generator:generate`，生成 `SystemConfig.java`、`SystemConfigMapper.java`、`SystemConfigExample.java`、`SystemConfigMapper.xml`

**檢查點**：MBG 生成完畢，Entity/Mapper/Example 就緒

---

## 第二階段：DTO 與 Service

**目的**：建立 DTO、Service 介面與實作，提供 CRUD 與帶預設值的 getter 方法。

- [X] T005 [P] 建立 `src/main/java/com/group/admin/req/systemconfig/SystemConfigCreateReq.java`
- [X] T006 [P] 建立 `src/main/java/com/group/admin/req/systemconfig/SystemConfigUpdateReq.java`
- [X] T007 [P] 建立 `src/main/java/com/group/admin/res/systemconfig/SystemConfigRes.java`
- [X] T008 [P] 建立 `src/main/java/com/group/admin/condition/SystemConfigCondition.java`（extends BaseCondition，欄位：configGroup、configKey）
- [X] T009 建立 `src/main/java/com/group/admin/service/SystemConfigService.java`（介面，含 CRUD + getInt/getString/getBoolean 方法）
- [X] T010 建立 `src/main/java/com/group/admin/service/impl/SystemConfigServiceImpl.java`：
  - CRUD：create（驗證 configKey 唯一、configValue 型別合法）、update（樂觀鎖 version 檢查）、delete、listAll、listByGroup
  - Getter：getInt(key, default)、getString(key, default)、getBoolean(key, default)
  - 讀取邏輯：查 DB，若查不到回傳 defaultValue

**檢查點**：Service 單元測試可驗證 CRUD 和 getter fallback

---

## 第三階段：Controller（後台 API）

**目的**：開放後台管理 API，僅限 ROLE_ADMIN。

- [X] T011 建立 `src/main/java/com/group/admin/controller/admin/AdminSystemConfigController.java`：
  - `GET /admin/system-config` — 查詢所有參數（可選 group 篩選）
  - `POST /admin/system-config` — 新增參數
  - `PUT /admin/system-config/{id}` — 修改參數（含 version 樂觀鎖）
  - `DELETE /admin/system-config/{id}` — 刪除參數
  - 所有端點加上 `@PreAuthorize("hasRole('ADMIN')")`
- [X] T012 在 DataInitializer.java 中加入 system_config 初始資料的 idempotent 檢查與插入

**檢查點**：`mvn clean package -DskipTests` 編譯通過；curl 測試 CRUD 正常

---

## 第四階段：業務整合

**目的**：將現有 hardcode 的保護時間常數改為讀取 SystemConfigService。

- [X] T013 [US2] 在 `LotteryTicketServiceImpl.startProtection()` 中將 hardcode 的 5 分鐘改為 `systemConfigService.getInt("protection_initial_minutes", 5)`
- [X] T014 [US2] 在保護延長邏輯中加入讀取 `protection_extension_minutes` 和 `protection_max_minutes`
- [X] T015 [US2] 在 `LotteryDrawController` 和 `RandomDrawController` 中將 hardcode 的 max 10 改為 `systemConfigService.getInt("max_draws_per_request", 10)`

**檢查點**：修改系統參數後，保護時間與抽獎上限行為即時改變

---

## 依賴關係

```
第一階段（DDL + MBG）  — 無依賴
第二階段（DTO + Service） — 依賴第一階段
第三階段（Controller）   — 依賴第二階段
第四階段（業務整合）     — 依賴第二階段；可與第三階段平行
```
