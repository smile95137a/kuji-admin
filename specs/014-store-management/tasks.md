---
description: "店家管理功能實作任務清單"
feature: "014-store-management"
generated: "2026-03-22"
---

# 任務清單：店家管理 (Store Management)

**輸入文件**：`specs/014-store-management/`（plan.md、spec.md、data-model.md、research.md、contracts/、quickstart.md）  
**分支**：`014-store-management`  
**技術棧**：Java 21 · Spring Boot 3.3.3 · MyBatis 3.0.5 · Spring Security 6 + JWT · MySQL 8.3

## 格式說明：`[ID] [P?] [Story?] 說明 + 檔案路徑`

- **[P]**：可與其他 [P] 任務平行執行（不同檔案，無未完成的相依）
- **[USx]**：對應使用者故事編號（US1–US4）
- 每個任務均包含精確的檔案路徑

---

## 第一階段：設定 (Setup)

**目的**：確認現有資料庫結構與程式基礎，為後續功能實作奠定基礎

- [ ] T001 確認 `store` 資料表已包含 data-model.md 所有欄位（執行 `DESCRIBE store;`），若缺少 `created_by`、`line_id` 欄位則執行 quickstart.md 中的 `ALTER TABLE store ADD COLUMN IF NOT EXISTS ...` 遷移語句
- [ ] T002 [P] 確認 `idx_store_status` 與 `idx_store_owner_id` 索引存在，若缺少則執行 `CREATE INDEX IF NOT EXISTS idx_store_status ON store (status); CREATE INDEX IF NOT EXISTS idx_store_owner_id ON store (owner_id);`

---

## 第二階段：基礎共用元件 (Foundational)

**目的**：所有使用者故事均依賴的核心元件；**必須在所有使用者故事開始前完成**

⚠️ **關鍵前提**：此階段完成前不得開始任何使用者故事實作

- [ ] T003 驗證 `src/main/java/com/group/admin/entity/Store.java` 包含 data-model.md 中所有欄位（`lineId`、`coverImageUrl`、`facebookUrl`、`instagramUrl`、`businessHours`、`remark`、`createdBy`、`updatedBy`），若缺少欄位及對應的 Lombok `@Data` 標記則補齊
- [ ] T004 驗證 `src/main/java/com/group/admin/res/store/StoreRes.java` 包含後台管理所需全欄位（`ownerId`、`ownerUsername`、`ownerDisplayName`、`status`、`createdAt`、`updatedAt`、`createdBy`），若缺少則補齊並保留 `remark` 僅限管理員回應
- [ ] T005 [P] 新增 `src/main/java/com/group/admin/req/store/UpdateStoreStatusReq.java`，包含一個帶 `@NotBlank` 與 `@Pattern(regexp = "ENABLED|DISABLED")` 的 `String status` 欄位，加上 Lombok `@Data` 標記
- [ ] T006 [P] 擴充 `src/main/java/com/group/admin/mapper/StoreMapper.java`，新增以下方法宣告：`List<Store> selectEnabledStores(@Param("offset") int offset, @Param("limit") int limit);` 及 `long countEnabledStores();`（僅用於 US2 公開列表，不含 BLOB 欄位）
- [ ] T007 擴充 `src/main/resources/mapper/StoreMapper.xml`，為 T006 的 `selectEnabledStores` 與 `countEnabledStores` 新增 SQL 片段：`selectEnabledStores` 使用 `Base_Column_List`（排除 `long_description`、`remark`），加上 `WHERE status = 'ENABLED' ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}`；`countEnabledStores` 為 `SELECT COUNT(*) FROM store WHERE status = 'ENABLED'`

**檢查點**：基礎元件就緒 — 可開始平行實作 US1 與 US2

---

## 第三階段：使用者故事 1 — 管理員建立新店家（優先級：P1）🎯 MVP

**目標**：管理員可在單一交易中原子性地建立新店家及連結的負責人帳號

**獨立測試**：管理員呼叫 `POST /admin/stores`，確認回應 HTTP 201 含 `id` 與 `ownerId`；查詢 DB 確認 `store`、`admin_user`、`admin_user_role` 均有新資料列；使用同一帳號名稱第二次呼叫應返回 HTTP 409

### US1 實作

- [ ] T008 [P] [US1] 新增 `src/main/java/com/group/admin/req/store/CreateStoreReq.java`，包含店家欄位（`@NotBlank String storeName`、`String shortDescription`、`String longDescription`、`String logoUrl`、`String coverImageUrl`、`String email`、`String phone`、`String address`、`String businessHours`、`String facebookUrl`、`String instagramUrl`、`String lineId`、`String remark`）以及 `@NotNull OwnerAccountReq owner`；內嵌 `@Data static class OwnerAccountReq`（`@NotBlank String username`、`@NotBlank String password`、`@NotBlank String displayName`、`String email`、`String phone`）
- [ ] T009 [US1] 在 `src/main/java/com/group/admin/service/StoreService.java` 中新增方法定義：`StoreRes createStore(CreateStoreReq req, String operatorId);`
- [ ] T010 [US1] 在 `src/main/java/com/group/admin/service/impl/StoreServiceImpl.java` 中實作 `createStore()`，以 `@Transactional(rollbackFor = Exception.class)` 包裹三步驟：(1) 組裝 `Store` 物件（UUID、`createdBy=operatorId`、`status=ENABLED`）並呼叫 `storeMapper.insert(store)`；(2) BCrypt 雜湊 `req.getOwner().getPassword()`，組裝 `AdminUser`（`status=ACTIVE`、`forceChangePassword=true`、`createdBy=operatorId`）並呼叫 `adminUserMapper.insert(owner)`；(3) 組裝 `AdminUserRole`（`adminUserId`、`roleCode=ROLE_STORE_OWNER`）並呼叫 `adminUserRoleMapper.insert(role)`；最後回傳 `toStoreRes(store, owner)`
- [ ] T011 [US1] 在 `src/main/java/com/group/admin/service/impl/StoreServiceImpl.java` 的 `createStore()` 中捕獲 `DataIntegrityViolationException`（帳號名稱重複違反唯一鍵約束），拋出含 `code: USERNAME_CONFLICT`、`field: owner.username` 的 `BusinessException`，對應 HTTP 409 回應（依現有 GlobalExceptionHandler 模式）
- [ ] T012 [US1] 在 `src/main/java/com/group/admin/controller/admin/AdminStoreController.java` 中新增 `@PostMapping` 方法（路徑 `/admin/stores`），加上 `@PreAuthorize("hasRole('ADMIN')")`，接收 `@Valid @RequestBody CreateStoreReq req`，從 `SecurityUtils.getCurrentUserId()` 取得 `operatorId`，呼叫 `storeService.createStore(req, operatorId)`，返回 `ResponseEntity.status(201).body(result)`
- [ ] T013 [P] [US1] 確認 `src/main/java/com/group/admin/controller/admin/AdminStoreController.java` 中已存在 `POST /admin/stores/list` 端點（帶 `@PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER','STORE_EDITOR')")`）；若不存在則新增，支援依 `storeName`（模糊比對）與 `status` 篩選，回應包含 `id`、`storeName`、`shortDescription`、`logoUrl`、`status`、`ownerId`、`ownerDisplayName`、`createdAt`、`updatedAt`（排除 BLOB 欄位）

**檢查點**：US1 完成 — 管理員可建立店家，交易回滾邏輯已驗證

---

## 第四階段：使用者故事 2 — 玩家瀏覽店家列表與店家頁面（優先級：P1）

**目標**：玩家可透過公開端點瀏覽所有已啟用店家及完整店家詳情（含上架商品）

**獨立測試**：無認證呼叫 `GET /api/stores` 應返回僅 ENABLED 的店家卡片；呼叫 `GET /api/stores/{id}` 應返回完整店家資訊含 ON_SHELF 商品列表；對 DISABLED 或不存在的 `{id}` 應返回 HTTP 404

### US2 實作

- [ ] T014 [P] [US2] 新增 `src/main/java/com/group/admin/res/store/StoreListItemRes.java`，包含 `String id`、`String storeName`、`String shortDescription`、`String logoUrl` 四個欄位，加上 Lombok `@Data` 標記
- [ ] T015 [P] [US2] 新增 `src/main/java/com/group/admin/res/store/StoreDetailRes.java`，包含所有公開店家欄位（`id`、`storeName`、`shortDescription`、`longDescription`、`logoUrl`、`coverImageUrl`、`email`、`phone`、`address`、`businessHours`、`facebookUrl`、`instagramUrl`、`lineId`）及 `List<LotteryListItemRes> products`；**排除** `remark`、`ownerId`、`ownerDisplayName`（公開端點不揭露內部資訊）
- [ ] T016 [US2] 在 `src/main/java/com/group/admin/service/StoreService.java` 中新增兩個方法定義：`PageResult<StoreListItemRes> listEnabledStores(int page, int size);` 及 `StoreDetailRes getPublicStoreDetail(String storeId);`
- [ ] T017 [US2] 在 `src/main/java/com/group/admin/service/impl/StoreServiceImpl.java` 中實作 `listEnabledStores()`：計算 `offset = (page - 1) * size`，呼叫 `storeMapper.selectEnabledStores(offset, size)` 與 `storeMapper.countEnabledStores()`，將 `Store` 列表映射為 `StoreListItemRes`，回傳 `PageResult<StoreListItemRes>`（包含 `items`、`total`、`page`、`size`）
- [ ] T018 [US2] 在 `src/main/java/com/group/admin/service/impl/StoreServiceImpl.java` 中實作 `getPublicStoreDetail()`：以 `storeMapper.selectByPrimaryKey(storeId)` 查詢；若 `store == null` 或 `store.status != ENABLED` 則拋出 `ResourceNotFoundException("店家不存在")`（HTTP 404，不揭露 DISABLED 店家存在性）；以 `LotteryExample` 查詢 `store_id=storeId AND status=ON_SHELF ORDER BY created_at DESC` 取得上架商品；組裝並回傳 `StoreDetailRes`（排除 `remark`、`ownerId`）
- [ ] T019 [US2] 新增 `src/main/java/com/group/admin/controller/api/StoreController.java`：加上 `@RestController @RequestMapping("/api/stores")`；實作 `@GetMapping` 方法（呼叫 `storeService.listEnabledStores(page, size)`，`page` 預設 1、`size` 預設 20）；實作 `@GetMapping("/{id}")` 方法（呼叫 `storeService.getPublicStoreDetail(id)`）；兩端點均無 `@PreAuthorize`（公開存取）
- [ ] T020 [US2] 在 `src/main/java/com/group/admin/config/SecurityConfig.java` 中確認或新增 `.requestMatchers("/api/stores", "/api/stores/**").permitAll()`，確保公開端點不需 JWT 認證，與現有 `/api/**` permitAll 模式一致

**檢查點**：US2 完成 — 公開端點可正確過濾 DISABLED 店家，404 不揭露店家存在性

---

## 第五階段：使用者故事 3 — 店家負責人編輯店家資料（優先級：P2）

**目標**：店家負責人可編輯自己店家的展示欄位，系統拒絕跨店家存取及修改負責人綁定

**獨立測試**：以 STORE_OWNER JWT 呼叫 `PUT /admin/stores/{自己的 id}`，確認描述更新成功；以同一 JWT 呼叫 `PUT /admin/stores/{其他店家 id}` 應返回 HTTP 403；嘗試在 Body 中傳入 `ownerId` 欄位應被靜默忽略

### US3 實作

- [ ] T021 [US3] 驗證 `src/main/java/com/group/admin/req/store/UpdateStoreReq.java` **不包含** `ownerId` 欄位（負責人綁定不可透過此端點變更，合約規範）；若存在則移除；確認包含 `storeName`、`shortDescription`、`longDescription`、`logoUrl`、`coverImageUrl`、`email`、`phone`、`address`、`businessHours`、`facebookUrl`、`instagramUrl`、`lineId`、`remark` 等可更新欄位
- [ ] T022 [US3] 在 `src/main/java/com/group/admin/service/impl/StoreServiceImpl.java` 中實作 `updateStore()` 的角色分流邏輯：以 `SecurityUtils.isAdmin()` 判斷角色；若為 `ADMIN` 則允許更新所有欄位（含 `remark`）；若為 `STORE_OWNER` 則先呼叫 `storeUserService.ownsStore(callerId, storeId)` 驗證所有權（若不符合則拋出 `AccessDeniedException("無權限編輯此店家")` → HTTP 403），再靜默清除 `req.setRemark(null)`；最後以 `BeanUtils.copyProperties` 僅套用非 null 欄位，設定 `updatedBy=callerId`、`updatedAt=now()`，呼叫 `storeMapper.updateByPrimaryKeySelective(store)`
- [ ] T023 [US3] 在 `src/main/java/com/group/admin/controller/admin/AdminStoreController.java` 中確認或新增 `PUT /admin/stores/{id}` 端點，標注 `@PreAuthorize("hasAnyRole('ADMIN','STORE_OWNER')")`，接收 `@Valid @RequestBody UpdateStoreReq req` 與 `@PathVariable String id`，呼叫 `storeService.updateStore(id, req)` 並返回更新後的 `StoreRes`

**檢查點**：US3 完成 — 所有權限控制正確，remark 欄位依角色隔離

---

## 第六階段：使用者故事 4 — 管理員停用店家並觸發連鎖效應（優先級：P2）

**目標**：管理員停用店家時，所有商品自動下架、橫幅停用；有進行中商品時返回 409 警告；重新啟用不恢復商品

**獨立測試**：對有 ON_SHELF 商品的店家呼叫 `PUT /admin/stores/{id}/status {"status":"DISABLED"}` 應返回 HTTP 409（含 activeLotteryCount）；加上 `?force=true` 後應返回 HTTP 200 且 cascadeResult 顯示下架數量；查詢 DB 確認所有 lottery 均為 OFF_SHELF；重新啟用後商品仍維持 OFF_SHELF（FR-005）

### US4 實作

- [ ] T024 [US4] 在 `src/main/java/com/group/admin/service/StoreService.java` 中新增方法定義：`Object updateStoreStatus(String storeId, UpdateStoreStatusReq req, boolean force, String operatorId);`（回傳含 `id`、`status`、`cascadeResult`、`updatedAt` 的回應物件，可用 `Map<String,Object>` 或新增 `UpdateStoreStatusRes.java`）
- [ ] T025 [US4] 在 `src/main/java/com/group/admin/service/impl/StoreServiceImpl.java` 中實作 `updateStoreStatus()` 的停用流程（以 `@Transactional(rollbackFor = Exception.class)` 包裹）：若 `status=DISABLED` 且 `force=false`，先以 `LotteryExample.andStoreIdEqualTo().andStatusEqualTo(ON_SHELF)` 計算 `activeLotteryCount`，若 `> 0` 則拋出含 `code: ACTIVE_LOTTERIES`、`activeLotteryCount` 的 `BusinessException`（HTTP 409）；若 `force=true` 或無進行中商品，則執行三步批次更新：(1) `store.status=DISABLED`；(2) `lotteryMapper.updateByExampleSelective(lotterySetter, filter)` 將 `status NOT IN ('OFF_SHELF','DRAFT')` 的商品設為 `OFF_SHELF`，記錄影響列數為 `productsOffShelf`；(3) `newsBannerMapper.updateByExampleSelective(bannerSetter, filter)` 將 `status=ENABLED` 的橫幅設為 `DISABLED`，記錄影響列數為 `bannersDisabled`；回傳含 `cascadeResult: { productsOffShelf, bannersDisabled }` 的回應
- [ ] T026 [US4] 在 `src/main/java/com/group/admin/service/impl/StoreServiceImpl.java` 中實作 `updateStoreStatus()` 的重新啟用流程（FR-005）：若 `status=ENABLED`，**僅**執行 `store.status=ENABLED`、`updatedBy=operatorId`、`updatedAt=now()`；**不觸發任何連鎖**；回傳含 `cascadeResult: null`、`note: "商品與橫幅狀態未自動恢復，需手動重新啟用"` 的回應
- [ ] T027 [US4] 在 `src/main/java/com/group/admin/controller/admin/AdminStoreController.java` 中新增 `@PutMapping("/{id}/status")` 方法，標注 `@PreAuthorize("hasRole('ADMIN')")`，接收 `@PathVariable String id`、`@Valid @RequestBody UpdateStoreStatusReq req`、`@RequestParam(defaultValue = "false") boolean force`，從 `SecurityUtils.getCurrentUserId()` 取得 `operatorId`，呼叫 `storeService.updateStoreStatus(id, req, force, operatorId)` 並返回 `ResponseEntity.ok(result)`

**檢查點**：US4 完成 — 連鎖停用在單一交易內完成，兩步確認機制正常運作，重新啟用不恢復商品

---

## 第七階段：收尾與橫切關注點 (Polish)

**目的**：稽核記錄確認、單元測試、quickstart.md 手動驗收

- [ ] T028 [P] 新增 `src/test/java/com/group/admin/service/StoreServiceTest.java`，以 `@SpringBootTest` + `@MockBean` 實作以下五個單元測試場景：(1) `createStore_成功_三表均寫入`（驗證 storeMapper、adminUserMapper、adminUserRoleMapper 各呼叫一次 insert）；(2) `createStore_帳號重複_全部回滾`（stubbing adminUserMapper.insert 拋出 `DataIntegrityViolationException`，驗證 storeMapper.insert 被呼叫但交易回滾不留孤立資料）；(3) `disableStore_連鎖_商品及橫幅均更新`（驗證 lotteryMapper 與 newsBannerMapper 的 `updateByExampleSelective` 各呼叫一次）；(4) `enableStore_不恢復商品_FR005`（驗證重新啟用時 lotteryMapper 的 updateByExampleSelective **未被呼叫**）；(5) `updateStore_STORE_OWNER_跨店家_拋403`（stubbing `ownsStore` 返回 false，驗證拋出 `AccessDeniedException`）
- [ ] T029 [P] 依 quickstart.md 步驟執行手動驗收測試，依序驗證：(3.2) 建立店家返回 201 含 `ownerId`；(3.5) 停用含抽獎活動店家先得 409 再以 `?force=true` 得 200；DB 確認 `lottery.status = OFF_SHELF`；(3.7) `GET /api/stores` 僅返回 ENABLED 店家；(3.8) `GET /api/stores/{停用店家 id}` 返回 404；(3.9) STORE_OWNER JWT 編輯他人店家返回 403
- [ ] T030 確認 `src/main/java/com/group/admin/service/impl/StoreServiceImpl.java` 中所有寫入操作均記錄稽核欄位（FR-011）：`createStore` 設定 `createdBy = operatorId`；`updateStore`、`updateStoreStatus`、`disableStore` 均設定 `updatedBy = SecurityUtils.getCurrentUserId()` 與 `updatedAt = LocalDateTime.now()`；確認批次更新的 `lotteryMapper.updateByExampleSelective` 中的 `lotterySetter` 也設定了 `updatedAt`

---

## 相依關係與執行順序

### 階段相依

- **第一階段（設定）**：無相依 — 可立即開始
- **第二階段（基礎）**：依賴第一階段完成 — **阻塞所有使用者故事**
- **第三階段（US1）**：依賴第二階段完成，與 US2 可平行進行
- **第四階段（US2）**：依賴第二階段完成，與 US1 可平行進行
- **第五階段（US3）**：依賴第二階段 + US1（PUT 端點擴充 US1 已建立的 Controller）
- **第六階段（US4）**：依賴第二階段 + US1（PUT /status 端點與 US1 共用 Controller）
- **第七階段（收尾）**：依賴所有使用者故事完成

### 使用者故事相依

```
第一階段 → 第二階段 → US1 (P1) ──┬──→ US3 (P2)
                     → US2 (P1) ──┤
                                   └──→ US4 (P2)
                                          ↓
                                     第七階段
```

- **US1**：第二階段完成後即可開始；與 US2 完全獨立
- **US2**：第二階段完成後即可開始；與 US1 完全獨立
- **US3**：依賴 US1（AdminStoreController 已存在）；實作 PUT /{id} 的角色分流邏輯
- **US4**：依賴 US1（AdminStoreController 已存在）；新增 PUT /{id}/status 端點

### 故事內部順序

每個使用者故事遵循以下順序：
- DTO（[P]，可平行）→ Service 介面定義 → ServiceImpl 實作 → Controller 端點 → 驗證

---

## 平行執行範例

### 第一階段（可全部平行）

```
同時執行：
任務：「T001 確認 store 資料表結構」
任務：「T002 確認資料庫索引」
```

### 第二階段（部分可平行）

```
同時執行（T005、T006 可與 T003、T004 平行）：
任務：「T003 驗證 Store.java 實體欄位」
任務：「T005 新增 UpdateStoreStatusReq.java」
任務：「T006 擴充 StoreMapper.java 方法宣告」
→ T003、T006 完成後：
任務：「T004 驗證 StoreRes.java 欄位」
任務：「T007 擴充 StoreMapper.xml SQL」
```

### 第三與第四階段（可完全平行，若有兩位開發者）

```
開發者 A — US1：
任務：「T008 新增 CreateStoreReq.java」（[P]）
任務：「T013 確認 POST /admin/stores/list 端點」（[P]，同時）
→ T009 → T010 → T011 → T012

開發者 B — US2：
任務：「T014 新增 StoreListItemRes.java」（[P]）
任務：「T015 新增 StoreDetailRes.java」（[P]，同時）
→ T016 → T017 → T018 → T019 → T020
```

### 第七階段（收尾，T028、T029 可平行）

```
同時執行：
任務：「T028 新增 StoreServiceTest.java」
任務：「T029 執行 quickstart.md 手動驗收」
→ 全部完成後：
任務：「T030 確認 FR-011 稽核欄位」
```

---

## 實作策略

### MVP 優先（僅 US1 + US2）

1. 完成第一階段：設定
2. 完成第二階段：基礎共用元件（**關鍵 — 阻塞所有故事**）
3. 完成第三階段：US1（管理員建立店家）
4. 完成第四階段：US2（玩家瀏覽店家）
5. **停止並驗證**：以 quickstart.md 3.2、3.7、3.8 獨立測試
6. 可在此時部署/示範 MVP

### 漸進式交付

1. 完成設定 + 基礎 → 基礎就緒
2. 新增 US1 → 獨立測試 → 部署/示範（**MVP！**）
3. 新增 US2 → 獨立測試 → 部署/示範
4. 新增 US3 → 獨立測試 → 部署/示範
5. 新增 US4 → 獨立測試 → 部署/示範
6. 每個故事均可在不破壞前一個故事的情況下獨立交付

### 平行團隊策略

有多位開發者時：

1. 全體完成第一與第二階段
2. 第二階段完成後：
   - 開發者 A：US1（建立店家）
   - 開發者 B：US2（公開前台）
3. US1 完成後：
   - 開發者 A：US3（負責人編輯）
   - 開發者 B：US4（連鎖停用）

---

## 備注

- **[P] 任務** = 不同檔案、無未完成相依，可平行執行
- **[USx] 標籤** = 追蹤任務所屬使用者故事
- `Store.java` 與 `StoreMapper.java` 已存在 — 任務著重**驗證並擴充**而非從頭建立
- 連鎖停用使用 MyBatis Example 批次更新（研究報告 §2），避免 N+1 問題
- `GET /api/stores/{id}` 的 DISABLED 店家返回 404，與不存在的店家相同（安全設計，防止列舉）
- 所有主鍵使用 UUID v4，在建立時以 `UUID.randomUUID().toString()` 生成
- 任何任務完成後建議提交一次 Git commit，並在各階段檢查點暫停驗收
