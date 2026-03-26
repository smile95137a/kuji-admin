# Tasks: RBAC 權限管理

**Input**: 設計文件來自 `/specs/009-rbac-permissions/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Tests**: 本功能規格書未明確要求 TDD 流程，故不產生測試任務（測試為選填）。

**Organization**: 任務依使用者故事分組，以支援各故事的獨立實作與測試。

## 格式說明：`[ID] [P?] [Story] 描述`

- **[P]**：可平行執行（不同檔案，無未完成任務依賴）
- **[Story]**：本任務對應的使用者故事（US1、US2、US3、US4）
- 描述中含精確檔案路徑

---

## Phase 1：Setup（環境驗證）

**目的**：在實作開始前，確認既有基礎設施的狀態符合 plan.md 中的架構關卡驗證結果。所有驗證項目應為唯讀操作（不修改代碼）。

- [ ] T001 驗證 `src/main/java/com/group/admin/security/UserPrincipal.java` 已包含 `roles`（`List<String>`）與 `storeIds`（`List<String>`）欄位，確認 getter 方法存在
- [ ] T002 驗證 `src/main/java/com/group/admin/security/AdminJwtAuthenticationFilter.java` 在 JWT 解析後正確將角色清單與店家 ID 清單填入 `UserPrincipal`，確認兩個欄位均從 token claims 讀取
- [ ] T003 確認資料庫 `role` 資料表已存在 3 個固定角色種子資料（ROLE_ADMIN、ROLE_STORE_OWNER、ROLE_STORE_EDITOR）；若缺少則執行 `quickstart.md` §1 中的種子 SQL

**Checkpoint**：既有基礎架構已確認無誤 — 可開始實作

---

## Phase 2：Foundational（共用基礎元件）

**目的**：跨所有使用者故事共用的核心基礎設施。**此階段必須完成後，方可進行任何使用者故事的實作。**

**⚠️ 關鍵**：所有使用者故事均依賴本階段完成

- [ ] T004 修改 `src/main/java/com/group/admin/config/SecurityConfig.java`：在類別層級加入 `@EnableMethodSecurity(prePostEnabled = true)` 以啟用 `@PreAuthorize` 方法層級授權（依 research.md §1 決策）
- [ ] T005 [P] 建立 `src/main/java/com/group/admin/annotation/RequiresPermission.java`：自訂選單層級權限標注，包含 `menuCode`（String）與 `level`（enum: VIEW/EDIT/DELETE）屬性；標注目標為 `ElementType.METHOD`
- [ ] T006 [P] 建立 `src/main/java/com/group/admin/aop/PermissionCheckAspect.java`：AOP `@Around` 切面，攔截 `@RequiresPermission` 標注的方法，從 `SecurityContextHolder` 取得使用者資訊，透過 `role_menu` 查詢驗證有效權限；ROLE_ADMIN 直接放行
- [ ] T007 [P] 建立 `src/main/java/com/group/admin/util/StorePermissionUtil.java`：實作 `assertStoreAccess(Authentication auth, String storeId)` 方法 — 若 `UserPrincipal.hasRole("ROLE_ADMIN")` 則直接回傳；否則驗證 `storeId` 存在於 `principal.getStoreIds()` 中，若不存在則拋出 `ForbiddenException`（依 research.md §3 模式）
- [ ] T008 [P] 建立 `src/main/java/com/group/admin/res/MenuPermissionRes.java`：包含欄位 `id`、`name`、`code`、`path`、`parentId`、`icon`、`orderNum`、`canView`（Boolean）、`canEdit`（Boolean）、`canDelete`（Boolean）、`children`（`List<MenuPermissionRes>`）（依 data-model.md §8）
- [ ] T009 [P] 建立 `src/main/java/com/group/admin/res/RoleWithPermissionsRes.java`：包含欄位 `id`、`name`、`code`、`description`，及內部類別 `MenuPermissionItem`（含 `menuId`、`menuName`、`menuCode`、`canView`、`canEdit`、`canDelete`）；外層包含 `List<MenuPermissionItem> menuPermissions`（依 data-model.md §8 及 contracts/GET_admin_roles_{id}_permissions.md）
- [ ] T010 [P] 建立 `src/main/java/com/group/admin/req/UpdateRolePermissionsReq.java`：包含 `@NotNull List<MenuPermissionItem> menuPermissions`；內部類別 `MenuPermissionItem` 含 `@NotBlank String menuId`、`Boolean canView`（預設 false）、`Boolean canEdit`（預設 false）、`Boolean canDelete`（預設 false）（依 data-model.md §8 及 contracts/PUT_admin_roles_{id}_permissions.md）
- [ ] T011 [P] 擴充 `src/main/resources/mapper/RoleMenuMapper.xml`：新增自訂查詢方法 `selectByRoleId`（依 roleId 取得所有 role_menu 列）及 `deleteByRoleId`（批次刪除）與 `batchInsert`（批次新增），支援 PUT 端點的 delete-then-insert 模式
- [ ] T012 [P] 擴充 `src/main/resources/mapper/MenuMapper.xml`：新增 `getMenusWithPermissionsForUser` 聚合查詢（依 data-model.md §3 的 SQL，含 JOIN role_menu、JOIN admin_user_role、WHERE aur.admin_user_id = #{userId}、GROUP BY m.id、SELECT MAX(can_view)/MAX(can_edit)/MAX(can_delete)）

**Checkpoint**：基礎元件完成 — 使用者故事實作可正式開始

---

## Phase 3：使用者故事 1 — 管理員管理角色選單權限（優先級：P1）🎯 MVP

**目標**：管理員可透過 API 查詢所有角色、取得角色的完整選單權限矩陣，並以替換式更新角色的選單權限（含 StoreEditor 子集驗證與業務規則強制執行）。

**獨立測試**：以 Admin token 呼叫 `GET /admin/roles` 取得角色清單 → `GET /admin/roles/{id}/permissions` 取得完整權限矩陣 → `PUT /admin/roles/{id}/permissions` 更新選單權限 → 重新呼叫 GET 確認變更生效。以 StoreOwner token 呼叫 `PUT /admin/roles/{id}/permissions` 應回傳 HTTP 403。嘗試設定 StoreEditor 超出 StoreOwner 的權限應回傳 HTTP 422。

- [ ] T013 [P] [US1] 建立 `src/main/java/com/group/admin/service/RoleService.java` 介面：宣告 `getAllRoles()`、`getRolePermissions(String roleId)`（回傳 `RoleWithPermissionsRes`）、`updateRolePermissions(String roleId, UpdateRolePermissionsReq req, String operatorId)`（回傳 `RoleWithPermissionsRes`）三個方法
- [ ] T014 [US1] 建立 `src/main/java/com/group/admin/service/RoleServiceImpl.java` 並實作 `getAllRoles()`：呼叫 `RoleMapper.selectAll()` 取得全部角色，依 id 升冪排序後回傳（3 筆固定資料，無分頁）
- [ ] T015 [US1] 於 `src/main/java/com/group/admin/service/RoleServiceImpl.java` 實作 `getRolePermissions(String roleId)`：(1) 以 roleId 查詢角色，找不到則拋出 `ResourceNotFoundException`；(2) 查詢所有 `is_visible = 1` 的選單（依 order_num 排序）；(3) 查詢該角色的 role_menu 列；(4) 在 Java 中合併：對每個選單查找對應 role_menu，若無則預設所有旗標為 false；(5) 組裝並回傳 `RoleWithPermissionsRes`
- [ ] T016 [US1] 於 `src/main/java/com/group/admin/service/RoleServiceImpl.java` 實作 `updateRolePermissions`：標注 `@Transactional`；執行順序：(1) 驗證 roleId 存在；(2) 驗證所有 menuId 存在於 menu 資料表；(3) 驗證 `can_edit = true` 必須同時 `can_view = true`（同一項目）；(4) 若角色為 ROLE_STORE_EDITOR，取得 ROLE_STORE_OWNER 的當前權限並對每個選單驗證子集規則（`editor.canEdit ≤ owner.canEdit`、`editor.canDelete ≤ owner.canDelete`），違規時回傳含欄位級錯誤的 HTTP 422；(5) 擷取更新前快照（before）；(6) `DELETE FROM role_menu WHERE role_id = #{roleId}`；(7) 批次 INSERT 新的 role_menu 列（過濾掉全為 false 的項目）；(8) 寫入稽核日誌（詳見 Phase 6 T032）；(9) 回傳更新後的 `RoleWithPermissionsRes`
- [ ] T017 [US1] 建立 `src/main/java/com/group/admin/controller/admin/RoleController.java`：注入 `RoleService`；實作 `listRoles()` 方法對應 `GET /admin/roles`，加上 `@PreAuthorize("hasRole('ADMIN')")` 標注，回傳 `Result.success(roleService.getAllRoles())`
- [ ] T018 [US1] 於 `src/main/java/com/group/admin/controller/admin/RoleController.java` 新增 `getRolePermissions(@PathVariable String id)` 方法對應 `GET /admin/roles/{id}/permissions`，加上 `@PreAuthorize("hasRole('ADMIN')")` 標注，呼叫 `roleService.getRolePermissions(id)`
- [ ] T019 [US1] 於 `src/main/java/com/group/admin/controller/admin/RoleController.java` 新增 `updateRolePermissions(@PathVariable String id, @Valid @RequestBody UpdateRolePermissionsReq req, Authentication authentication)` 方法對應 `PUT /admin/roles/{id}/permissions`，加上 `@PreAuthorize("hasRole('ADMIN')")` 標注，從 `authentication` 提取 operatorId，呼叫 `roleService.updateRolePermissions(id, req, operatorId)`

**Checkpoint**：此時 US1 應完整可運作 — Admin 可查詢並更新角色權限，非 Admin 呼叫受 403 保護，業務規則違規回傳 422

---

## Phase 4：使用者故事 2 — 店家負責人只存取自己店家的資料（優先級：P1）

**目標**：ROLE_STORE_OWNER 與 ROLE_STORE_EDITOR 使用者只能存取自己店家的資料；嘗試存取跨店家資源回傳 HTTP 403；ROLE_ADMIN 繞過所有店家範圍限制。

**獨立測試**：以 StoreOwner A 的 token 呼叫 `GET /admin/products?storeId=<store_A>` 應正常回傳。呼叫 `GET /admin/products?storeId=<store_B>` 應回傳 HTTP 403。以 Admin token 呼叫相同端點應回傳所有店家資料。

- [ ] T020 [US2] 識別現有所有接受 `storeId` 參數的 Controller 方法（商品、訂單、報表等），在各方法加入 `StorePermissionUtil.assertStoreAccess(authentication, storeId)` 呼叫或 `@PreAuthorize("@storePermissionUtil.assertStoreAccess(authentication, #storeId) == null")` 標注，確保服務層在查詢前強制執行店家所有權驗證
- [ ] T021 [P] [US2] 審查現有商品相關 Service 方法（`ProductService` / `ProductServiceImpl`）：確認所有查詢的 WHERE 子句包含 `store_id = #{storeId}` 條件；若缺少則補充 MyBatis Example 條件或手動 WHERE 子句
- [ ] T022 [P] [US2] 審查現有訂單相關 Service 方法（`OrderService` / `OrderServiceImpl`）：確認所有查詢的 WHERE 子句包含 `store_id = #{storeId}` 條件；若缺少則補充
- [ ] T023 [US2] 確認 `ForbiddenException`（或同等例外類別）已在全域例外處理器（`GlobalExceptionHandler` 或同名類別）中正確對應至 HTTP 403 回應，格式符合 `{"code": 403, "message": "...", "data": null}`
- [ ] T024 [US2] 驗證 `AdminJwtAuthenticationFilter` 在登入時正確從 `store_user` 資料表查詢並將使用者關聯的所有 storeId 填入 `UserPrincipal.storeIds`；若查詢邏輯缺失則補充（依 research.md §3，此模式應已存在）

**Checkpoint**：此時 US2 應完整可驗證 — 使用 quickstart.md §5「驗證店家負責人隔離」清單執行手動測試

---

## Phase 5：使用者故事 3 — 系統依權限動態渲染選單（優先級：P2）

**目標**：`GET /admin/menus/my` 端點依已登入使用者的有效角色選單權限，回傳帶有 canView/canEdit/canDelete 旗標的樹狀選單結構；ROLE_ADMIN 收到所有可見選單（所有旗標均為 true）；非 Admin 只收到 `can_view = true` 的選單。

**獨立測試**：以 Admin token 呼叫 `GET /admin/menus/my` 確認回傳全部可見選單且所有旗標為 true。以 StoreEditor（僅 PRODUCTS 權限）token 呼叫，確認僅返回商品相關選單，且 canDelete 為 false。無角色使用者呼叫應回傳空陣列（HTTP 200，data: []）。

- [ ] T025 [P] [US3] 修改 `src/main/java/com/group/admin/service/MenuService.java` 介面：新增 `getAuthorizedMenusForUser(String userId, List<String> roles)` 方法，回傳 `List<MenuPermissionRes>`
- [ ] T026 [US3] 於 `src/main/java/com/group/admin/service/MenuServiceImpl.java` 實作 `getAuthorizedMenusForUser`：(1) 若角色清單包含 `ROLE_ADMIN`，跳過 JOIN 查詢，直接取得所有 `is_visible = 1` 選單並將所有旗標設為 true；(2) 否則執行 `MenuMapper.getMenusWithPermissionsForUser(userId)` 聚合查詢；(3) 過濾保留 `canView = true` 的選單；(4) 在 Java 中建立樹狀結構（以 `parentId` 巢狀化）並依 `orderNum` 排序；(5) 父選單被過濾時一併排除其所有子選單；(6) 回傳 `List<MenuPermissionRes>`
- [ ] T027 [US3] 修改 `src/main/java/com/group/admin/controller/admin/MenuController.java`：新增 `getMyMenus(Authentication authentication)` 方法對應 `GET /admin/menus/my`，加上 `@PreAuthorize("isAuthenticated()")` 標注；從 `UserPrincipal` 提取 userId 與 roles，呼叫 `menuService.getAuthorizedMenusForUser(userId, roles)`（依 contracts/GET_admin_menus_my.md）
- [ ] T028 [US3] 驗證 `GET /admin/menus/my` 回應結構符合 contracts/GET_admin_menus_my.md 的完整規格：確認欄位名稱（orderNum 非 order_num）、null 值處理、children 為空陣列而非 null、父子排序正確

**Checkpoint**：此時 US3 應完整可驗證 — 使用 quickstart.md §5「驗證選單篩選」清單執行手動測試

---

## Phase 6：使用者故事 4 — 管理員查看權限異動稽核紀錄（優先級：P3）

**目標**：每次 `PUT /admin/roles/{id}/permissions` 成功執行後，將操作者 ID、時間戳記、前後狀態寫入 `admin_operation_log` 資料表；管理員可透過資料庫或未來 API 查詢異動紀錄。

**獨立測試**：以 Admin 身份呼叫 `PUT /admin/roles/{id}/permissions`，然後查詢 `SELECT * FROM admin_operation_log WHERE operation_type='UPDATE_ROLE_PERMISSIONS' ORDER BY created_at DESC LIMIT 1`，確認 operator_id、target_id 及 JSON before/after 均已正確填入（依 quickstart.md §5「驗證稽核日誌」）。

- [ ] T029 [US4] 確認 `AdminOperationLogMapper.xml` 及對應 Java Mapper 介面存在且可呼叫 `insert` 方法；若無則建立最小可用版本（依 research.md §6，此 mapper 應已存在）
- [ ] T030 [US4] 於 `src/main/java/com/group/admin/service/RoleServiceImpl.java` 的 `updateRolePermissions` 方法中（T016 已預留位置），在 delete-then-insert 成功後建立 `AdminOperationLog` 物件，填入：`operatorId`（從 operatorId 參數）、`operationType` = `"UPDATE_ROLE_PERMISSIONS"`、`targetType` = `"ROLE"`、`targetId`（roleId）、`content`（含 before/after JSON 快照，使用 Jackson `ObjectMapper` 序列化）、`createdAt`（now()），並呼叫 `adminOperationLogMapper.insert(log)`
- [ ] T031 [US4] 驗證稽核日誌寫入在同一 `@Transactional` 區塊內執行（若任一步驟失敗則所有變更回滾，包括日誌），確認事務邊界正確
- [ ] T032 [P] [US4] 實作 `PUT /admin/roles/{id}/permissions` 的冪等性驗證：以相同 payload 呼叫兩次，確認第二次呼叫正確覆寫（非累加），且 admin_operation_log 產生兩筆獨立紀錄

**Checkpoint**：此時 US4 應完整可驗證 — 所有 4 個使用者故事均已實作完成

---

## Phase 7：Polish & 橫切關注點

**目的**：改善影響多個使用者故事的品質、安全性與可維護性

- [ ] T033 [P] 確認 `SecurityConfig.java` 的 Filter chain URL 規則涵蓋 `/admin/roles/**` 路徑，要求認證（作為 `@PreAuthorize` 之前的第一道防線）
- [ ] T034 [P] 審查 RoleController 與 MenuController 所有新端點的錯誤回應格式，確保 404（角色不存在）、422（業務規則違規）、403（權限不足）均符合全域 `Result` 包裝格式
- [ ] T035 驗證多角色使用者的有效權限聚合邏輯：在資料庫中建立一個同時擁有 ROLE_STORE_OWNER 和 ROLE_STORE_EDITOR 的測試使用者，呼叫 `GET /admin/menus/my`，確認有效權限為兩個角色的聯集（OR 邏輯）
- [ ] T036 [P] 補充 `quickstart.md` §6 常見錯誤清單中未涵蓋的已知問題，並確認本功能所有 API 的 Postman Collection 或 `.http` 測試檔案已更新或建立
- [ ] T037 [P] 執行 `quickstart.md` §7 的完整測試套件（`mvn test -Dtest="MenuServiceTest,RoleServiceTest,MenuControllerTest,RoleControllerTest"`），確認所有測試通過後方可提交 PR

---

## 依賴關係與執行順序

### 階段依賴

- **Setup（Phase 1）**：無依賴，立即可開始
- **Foundational（Phase 2）**：依賴 Phase 1 完成 — **封鎖所有使用者故事**
- **US1（Phase 3）**：依賴 Phase 2 完成
- **US2（Phase 4）**：依賴 Phase 2 完成（與 US1 可平行進行）
- **US3（Phase 5）**：依賴 Phase 2 完成（與 US1、US2 可平行進行）
- **US4（Phase 6）**：依賴 Phase 3 完成（T016 的稽核日誌預留位置）
- **Polish（Phase 7）**：依賴所有目標故事完成

### 使用者故事依賴

- **US1（P1）**：Phase 2 完成後即可開始 — 無其他故事依賴
- **US2（P1）**：Phase 2 完成後即可開始 — 與 US1 完全獨立可平行
- **US3（P2）**：Phase 2 完成後即可開始 — 與 US1、US2 獨立
- **US4（P3）**：依賴 US1（T016）完成 — 稽核日誌寫入 RoleServiceImpl

### 各故事內部順序

- 介面/DTO → 服務層實作 → Controller 層
- 服務層各方法可依複雜度拆分（getAllRoles → get → update）
- Controller 依賴服務層完成

### 平行機會

- Phase 2：T005–T012 全部標 [P]，可同時進行
- Phase 3：T013 標 [P]（可先建介面），T014–T019 依序執行
- Phase 4：T021、T022 標 [P]，可同時審查不同模組
- Phase 5：T025 標 [P]（可先改介面），T026–T028 依序執行
- US1 與 US2 可由不同工程師同時實作（完全不同文件）

---

## 平行執行範例

### Phase 2 Foundational（全部可同時啟動）

```bash
# 同時執行：
Task: "建立 RequiresPermission.java (T005)"
Task: "建立 PermissionCheckAspect.java (T006)"
Task: "建立 StorePermissionUtil.java (T007)"
Task: "建立 MenuPermissionRes.java (T008)"
Task: "建立 RoleWithPermissionsRes.java (T009)"
Task: "建立 UpdateRolePermissionsReq.java (T010)"
Task: "擴充 RoleMenuMapper.xml (T011)"
Task: "擴充 MenuMapper.xml (T012)"
```

### US1（Phase 3）與 US2（Phase 4）可平行

```bash
# 工程師 A: Phase 3 US1
Task: "建立 RoleService.java 介面 (T013)"
Task: "實作 RoleServiceImpl (T014-T016)"
Task: "建立 RoleController (T017-T019)"

# 工程師 B: Phase 4 US2（同時進行）
Task: "強制執行店家存取控制 (T020)"
Task: "審查商品服務層 (T021)"
Task: "審查訂單服務層 (T022)"
```

---

## 實作策略

### MVP 優先（僅 US1）

1. 完成 Phase 1：Setup
2. 完成 Phase 2：Foundational（關鍵封鎖項）
3. 完成 Phase 3：US1（管理員管理角色選單權限）
4. **停止並驗證**：依 quickstart.md §5「驗證角色管理上的 @PreAuthorize」執行手動測試
5. 若通過 → 繼續 US2 或 Demo

### 增量交付

1. Setup + Foundational → 基礎就緒
2. US1 完成 → 獨立測試 → 角色管理 API 可用（MVP）
3. US2 完成 → 獨立測試 → 店家隔離強制執行
4. US3 完成 → 獨立測試 → 動態側邊欄選單上線
5. US4 完成 → 獨立測試 → 稽核合規達成
6. 每個故事在不破壞前一個故事的情況下新增價值

### 平行團隊策略

多位工程師時：

1. 團隊共同完成 Setup + Foundational（Phase 1-2）
2. Foundational 完成後：
   - 工程師 A：US1（RoleController + RoleService）
   - 工程師 B：US2（StorePermissionUtil 套用 + 服務層審查）
   - 工程師 C：US3（MenuService.getAuthorizedMenusForUser + MenuController /my 端點）
3. US4 由完成 US1 的工程師接續（稽核日誌嵌入 RoleServiceImpl）

---

## 注意事項

- `[P]` 任務 = 不同檔案，無依賴，可同時開工
- `[Story]` 標籤將任務對應至特定使用者故事以追溯需求
- 每個使用者故事應可獨立完成並測試
- 所有 RBAC 資料表（role、menu、role_menu、admin_user_role）均已存在於 schema，**本功能不需要 schema 遷移**
- 角色固定（ROLE_ADMIN、ROLE_STORE_OWNER、ROLE_STORE_EDITOR），v1.0 不支援動態建立角色
- 權限變更立即生效（v1.0 無快取），不需重啟
- ROLE_ADMIN 繞過所有 StorePermissionUtil 店家範圍檢查
- `can_edit = true` 必須同時 `can_view = true`，Service 層強制執行
- StoreEditor 權限必須是 StoreOwner 的嚴格子集（FR-008），Service 層強制執行，HTTP 422 回報
- 依每個任務或邏輯群組提交 commit
- 可在任意 Checkpoint 停下，獨立驗證已完成故事
