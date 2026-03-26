# 任務清單：推薦碼 (Referral Code)

**來源**：`/specs/012-referral-code/` 設計文件
**先決條件**：plan.md（必要）、spec.md（必要）、research.md、data-model.md、contracts/、quickstart.md

**組織原則**：任務依使用者故事分組，以支援各故事的獨立實作與測試。

## 格式：`[ID] [P?] [Story?] 描述（含檔案路徑）`

- **[P]**：可平行執行（不同檔案，無未完成任務的依賴關係）
- **[Story]**：所屬使用者故事（US1、US2、US3）
- **注意**：本系統**大部分已預先建置**；任務聚焦於補全缺失的端點、強化防護邏輯，以及新增兩個 DTO。

---

## 第一階段：環境建置（資料庫遷移）

**目的**：補上缺失的資料庫唯一索引，確保每位用戶僅能有一筆推薦紀錄，為所有使用者故事奠定資料完整性基礎。

- [ ] T001 建立資料庫遷移腳本 `sql/V012__add_referral_record_user_unique.sql`，內容為 `ALTER TABLE referral_record ADD UNIQUE INDEX idx_referral_record_user_id (user_id);`，並在目標資料庫執行，強制每用戶僅一筆推薦紀錄（對應 data-model.md 必要遷移）

**檢查點**：遷移執行完成後，`UNIQUE INDEX idx_referral_record_user_id` 存在於 `referral_record` 資料表中

---

## 第二階段：基礎建設（共用 DTO）

**目的**：建立 US2 與 US3 所需的共用 DTO，所有使用者故事均依賴此階段完成。

⚠️ **關鍵**：US2 與 US3 的實作必須等此階段完成後方可開始。

- [ ] T002 [P] 建立 `src/main/java/com/group/admin/dto/request/ReferralValidateReq.java`：包含 `@NotBlank @Size(max=20) String code` 欄位，加上 `@Data` Lombok 標注（對應合約 POST_api_auth_validate-referral.md）
- [ ] T003 [P] 建立 `src/main/java/com/group/admin/dto/response/ReferralStatsRes.java`：包含 `storeId`、`storeName`、`totalReferrals`（Long）、`activeCodeCount`（Long）、`List<DailyCount> timeline` 欄位，並內嵌靜態類別 `DailyCount { String date; Long count; }`，加上 `@Data @AllArgsConstructor` Lombok 標注（對應合約 GET_admin_referral-stats.md）
- [ ] T004 [P] 建立 `src/main/java/com/group/admin/dto/response/ReferralValidateRes.java`：包含 `boolean valid`、`String code`、`String storeName` 欄位，加上 `@Data @AllArgsConstructor` Lombok 標注（對應合約 POST_api_auth_validate-referral.md 回應結構）

**檢查點**：三個 DTO 已建立且可正常編譯

---

## 第三階段：使用者故事 1 — 管理員建立並管理推薦碼（優先級：P1）🎯 MVP

**目標**：管理員可建立推薦碼、查看推薦碼清單，並停用不再使用的推薦碼。

**獨立測試**：
1. 呼叫 `POST /admin/referral-codes`（Bearer ADMIN token），確認回傳 201 且代碼為 8 位大寫英數字
2. 呼叫 `PUT /admin/referral-codes/{id}/disable`（Bearer ADMIN token），確認回傳 200 且 `isActive=false`
3. 對已停用的代碼再次呼叫停用，確認回傳 400 `推薦碼已經是停用狀態`
4. 對不存在的 id 呼叫停用，確認回傳 404 `推薦碼不存在`

### 使用者故事 1 實作

- [ ] T005 [P] [US1] 驗證 `src/main/java/com/group/admin/controller/admin/AdminReferralCodeController.java` 中 `POST /admin/referral-codes` 的 `create()` 方法：確認代碼使用 `UUID.randomUUID().toString().replace("-","").substring(0,8).toUpperCase()` 生成，並含碰撞重試邏輯（最多 5 次），符合合約 POST_admin_referral-codes.md 業務規則第 1、2 條
- [ ] T006 [P] [US1] 驗證 `src/main/java/com/group/admin/controller/admin/AdminReferralCodeController.java` 中 `GET /admin/referral-codes` 的 `getAll()` 方法：確認支援 `storeId` 及 `isActive` 選用查詢參數篩選，符合合約 GET_admin_referral-codes.md
- [ ] T007 [US1] 在 `src/main/java/com/group/admin/service/ReferralCodeService.java` 介面中新增方法簽名：`ReferralCodeRes disableCode(String id)`（對應合約 PUT_admin_referral-codes_{id}_disable.md）
- [ ] T008 [US1] 在 `src/main/java/com/group/admin/service/impl/ReferralCodeServiceImpl.java` 中實作 `disableCode(String id)`：查詢代碼（不存在拋出 `BusinessException("推薦碼不存在")`）→ 已停用拋出 `BusinessException("推薦碼已經是停用狀態")` → 設定 `isActive=false`、`updatedAt=LocalDateTime.now()` → 呼叫 `referralCodeMapper.updateByPrimaryKeySelective()` → 回傳 `convertToRes(code)`（依 quickstart.md 步驟 3）
- [ ] T009 [US1] 在 `src/main/java/com/group/admin/controller/admin/AdminReferralCodeController.java` 中新增端點：`@PutMapping("/{id}/disable") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<ApiResponse<ReferralCodeRes>> disableCode(@PathVariable String id)`，呼叫 `referralCodeService.disableCode(id)` 並回傳 `ApiResponse.success("推薦碼已停用", result)`（依合約 PUT_admin_referral-codes_{id}_disable.md 實作說明）

**檢查點**：使用者故事 1 可完整獨立測試——推薦碼建立、查詢、停用流程全部正常，停用重複操作與代碼不存在皆回傳正確錯誤碼

---

## 第四階段：使用者故事 2 — 新用戶以推薦碼註冊（優先級：P1）

**目標**：新用戶在註冊時可選擇性輸入推薦碼，系統在允許註冊前即時驗證代碼有效性，並在成功時建立不可變的推薦紀錄。

**獨立測試**：
1. 呼叫 `POST /api/auth/validate-referral`（無需 token）帶有效代碼，確認回傳 `{ valid: true, storeName: "..." }`
2. 呼叫 `POST /api/auth/validate-referral` 帶無效代碼，確認回傳 `{ valid: false, storeName: null }`
3. 呼叫 `POST /api/auth/register` 帶有效 `referralCode`，確認 `referral_record` 有新紀錄
4. 呼叫 `POST /api/auth/register` 帶已停用代碼，確認回傳 400 `REFERRAL_CODE_DISABLED`
5. 呼叫 `POST /api/auth/register` 帶店家負責人 email，確認回傳 400 `SELF_REFERRAL_NOT_ALLOWED`
6. 呼叫 `POST /api/auth/register` 不帶 `referralCode`，確認正常完成註冊

### 使用者故事 2 實作

- [ ] T010 [US2] 在 `src/main/java/com/group/admin/service/ReferralCodeService.java` 介面中新增方法簽名：`ReferralValidateRes validateForRegistration(String code)` 及 `void useCode(String userId, String code, String registrationEmail)`（確認後者若已存在則補強，對應合約 POST_api_auth_validate-referral.md）
- [ ] T011 [US2] 在 `src/main/java/com/group/admin/service/impl/ReferralCodeServiceImpl.java` 中實作 `validateForRegistration(String code)`：trim+大寫輸入 → 查詢 `referral_code WHERE code=? AND is_active=1`（不存在回傳 `valid=false`）→ 查詢關聯 `store` 確認 `status='ACTIVE'`（非活躍回傳 `valid=false`）→ 檢查 `maxUsage`（若設定且 `usedCount >= maxUsage` 回傳 `valid=false`）→ 檢查 `validUntil`（若設定且已過期回傳 `valid=false`）→ 回傳 `ReferralValidateRes(true, code, store.storeName)`（依合約 POST_api_auth_validate-referral.md 驗證邏輯）
- [ ] T012 [P] [US2] 在 `src/main/java/com/group/admin/controller/api/ReferralCodeValidateController.java` 中新增端點：`@PostMapping("/validate-referral") public ResponseEntity<ApiResponse<ReferralValidateRes>> validateForRegistration(@Valid @RequestBody ReferralValidateReq req)`，呼叫 `referralCodeService.validateForRegistration(req.getCode())` 並回傳 200（依合約 POST_api_auth_validate-referral.md）
- [ ] T013 [P] [US2] 在 `SecurityConfig.java` 的 `permitAll` 清單中新增 `.requestMatchers("/api/auth/validate-referral").permitAll()`，確保公開存取（依合約安全性說明及 quickstart.md 步驟 4）
- [ ] T014 [US2] 在 `src/main/java/com/group/admin/service/impl/ReferralCodeServiceImpl.java` 的 `useCode()` 中補強防護邏輯：（1）**店家停用攔截**：取得推薦碼後查詢對應 `Store`，確認 `store.status == "ACTIVE"`，否則拋出 `BusinessException("STORE_INACTIVE")`；（2）**自我推薦防護**：查詢 `AdminUser` 取得 `storeOwner.email`，若與 `registrationEmail` 相符則拋出 `BusinessException("SELF_REFERRAL_NOT_ALLOWED")`（依 research.md 決策 3、4）
- [ ] T015 [US2] 驗證並確認 `src/main/java/com/group/admin/service/impl/UserServiceImpl.java` 的 `register()` 方法中推薦錯誤處理矩陣符合下表：代碼不存在→400、代碼停用→400、店家非活躍→400、已使用推薦→400、自我推薦→400，未提供代碼→靜默跳過，代碼有效→建立 `ReferralRecord` 並遞增 `usedCount`（依 research.md 決策 8）

**檢查點**：使用者故事 2 可完整獨立測試——即時驗證端點正常回應、5 種拒絕情境皆正確回傳 400、無代碼時正常完成註冊

---

## 第五階段：使用者故事 3 — 管理員查看推薦統計（優先級：P2）

**目標**：管理員可查看各店家的被引薦用戶總數與每日時間軸，支援數據驅動的促銷決策。

**獨立測試**：
1. 呼叫 `GET /admin/referral-codes/stats`（Bearer ADMIN token），確認回傳各店家清單含 `totalReferrals` 及 `timeline`
2. 加 `?storeId=<uuid>` 查詢參數，確認只回傳該店家資料
3. 對零推薦數的店家確認回傳 `totalReferrals: 0, timeline: []`
4. 加 `?startDate=2026-03-01&endDate=2026-03-31`，確認時間軸依日期範圍篩選

### 使用者故事 3 實作

- [ ] T016 [P] [US3] 在 `src/main/java/com/group/admin/repository/ReferralCodeRepository.java` 中新增 `@Select` 方法 `selectStatsByStore(ReferralReportCondition condition)`：執行店家層級 `LEFT JOIN` 聚合查詢，回傳 `List<Map<String, Object>>`，包含 `storeId`、`storeName`、`totalReferrals`、`activeCodeCount`，並依 `startDate`/`endDate`/`storeId` 條件篩選（依 research.md 決策 7 SQL 草稿及合約 GET_admin_referral-stats.md）
- [ ] T017 [P] [US3] 在 `src/main/java/com/group/admin/repository/ReferralRecordRepository.java` 中新增 `@Select` 方法 `selectTimelineByStore(ReferralReportCondition condition)`：執行 `GROUP BY store_id, DATE(referred_at)` 每日聚合查詢，回傳 `List<Map<String, Object>>`，包含 `storeId`、`referralDate`、`dailyCount`，依日期升冪排序（依合約 GET_admin_referral-stats.md SQL Step 2）
- [ ] T018 [US3] 在 `src/main/java/com/group/admin/service/ReferralCodeService.java` 介面中新增方法簽名：`List<ReferralStatsRes> getReferralStats(ReferralReportCondition condition)`（對應合約 GET_admin_referral-stats.md）
- [ ] T019 [US3] 在 `src/main/java/com/group/admin/service/impl/ReferralCodeServiceImpl.java` 中實作 `getReferralStats(ReferralReportCondition condition)`：（1）呼叫 `referralCodeRepository.selectStatsByStore(condition)` 取得各店家總計；（2）呼叫 `referralRecordRepository.selectTimelineByStore(condition)` 取得每日時間軸；（3）依 storeId 合併兩個結果集為 `List<ReferralStatsRes>`，零推薦數的店家設 `timeline=[]`；日期範圍預設最近 30 天（依 research.md 決策 7 合併邏輯及合約業務規則）
- [ ] T020 [US3] 在 `src/main/java/com/group/admin/controller/admin/AdminReferralCodeController.java` 中新增端點：`@GetMapping("/stats") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<ApiResponse<List<ReferralStatsRes>>> getReferralStats(ReferralReportCondition condition)`，呼叫 `referralCodeService.getReferralStats(condition)` 並回傳 `ApiResponse.success(result)`（依 quickstart.md 步驟 4 及合約 GET_admin_referral-stats.md）

**檢查點**：使用者故事 3 可完整獨立測試——統計端點回傳正確的各店家計數與時間軸，包含零推薦數的店家

---

## 第六階段：收尾與橫切關注點

**目的**：跨故事的驗證、完整性確認與 quickstart 情境測試。

- [ ] T021 [P] 對照所有合約文件，驗證各端點的 HTTP 狀態碼與錯誤訊息一致：`PUT /{id}/disable` 的 404/400、`POST /admin/referral-codes` 的 201/400、`GET /stats` 的 400（日期範圍無效）、`POST /api/auth/validate-referral` 的 400（缺少 code 欄位）
- [ ] T022 [P] 確認推薦追蹤邏輯（`useCode()`、`validateCode()`）以 try/catch 包裝，任何推薦失敗**不**影響抽獎、訂單或付款流程（FR-009）；檢查 `UserServiceImpl.java` 確認推薦相關異常不會傳播至核心交易
- [ ] T023 執行 `quickstart.md` 中的完整情境測試（步驟 6 的 curl 指令），逐一驗證：（1）管理員建立推薦碼、（2）管理員停用推薦碼、（3）管理員查看統計、（4）用戶驗證代碼、（5）用戶帶代碼完成註冊、（6）用戶帶無效代碼被拒、（7）以相同 email 重複註冊被拒；並執行 `quickstart.md` 資料庫驗證查詢確認資料正確性

---

## 依賴關係與執行順序

### 階段依賴關係

- **第一階段（環境建置）**：無依賴——立即開始
- **第二階段（基礎建設）**：無依賴——可與第一階段平行執行
- **第三、四、五階段（使用者故事）**：依賴第二階段完成；彼此可平行進行（若人力充足）
- **第六階段（收尾）**：依賴所有使用者故事階段完成

### 使用者故事依賴關係

- **US1（第三階段，P1）**：第二階段完成後即可開始——無需依賴其他故事，但 T007 必須在 T008 之前，T008 必須在 T009 之前
- **US2（第四階段，P1）**：第二階段完成後即可開始——T010 必須在 T011 之前，T012/T013/T014/T015 可在 T011 完成後平行進行
- **US3（第五階段，P2）**：第二階段完成後即可開始——T016/T017 可平行，T018 在 T016+T017 後，T019 在 T018 後，T020 在 T019 後

### 各故事內的執行順序

```
US1: T005 ──→ [T007 → T008 → T009]
     T006 ──↗  （T005、T006 平行）

US2: T010 → T011 → T012 (平行開始)
                 → T013 (平行開始)
                 → T014 (平行開始)
                 → T015 (平行開始)

US3: [T016 ┐平行]
     [T017 ┘    ] → T018 → T019 → T020
```

---

## 平行執行範例

### 第二階段（基礎建設）

```bash
# 可同時進行的三個任務：
任務 T002：建立 ReferralValidateReq.java
任務 T003：建立 ReferralStatsRes.java
任務 T004：建立 ReferralValidateRes.java
```

### 第三階段（US1）

```bash
# 可同時進行：
任務 T005：驗證 POST /admin/referral-codes 的代碼生成邏輯
任務 T006：驗證 GET /admin/referral-codes 的篩選功能
# 完成後依序進行：T007 → T008 → T009
```

### 第四階段（US2）

```bash
# T011 完成後可同時進行：
任務 T012：新增 POST /validate-referral 端點
任務 T013：更新 SecurityConfig permitAll
任務 T014：強化 useCode() 防護邏輯
任務 T015：驗證 UserServiceImpl 錯誤處理矩陣
```

### 第五階段（US3）

```bash
# 可同時進行：
任務 T016：新增 selectStatsByStore() Repository 方法
任務 T017：新增 selectTimelineByStore() Repository 方法
# 完成後依序進行：T018 → T019 → T020
```

---

## 實作策略

### MVP 優先（僅 US1）

1. 完成第一階段：環境建置（T001）
2. 完成第二階段：基礎建設（T002-T004）
3. 完成第三階段：US1（T005-T009）
4. **停下驗證**：呼叫推薦碼建立、查詢、停用端點，確認全部正常
5. 可部署 / 演示

### 遞增交付

1. 第一+二階段 → 基礎就緒
2. 加入 US1 → 獨立測試 → 部署（MVP！）
3. 加入 US2 → 獨立測試 → 部署
4. 加入 US3 → 獨立測試 → 部署
5. 每個故事獨立新增價值，不破壞前一個故事

### 多人平行策略

人力充足時（第二階段完成後）：

- **開發者 A**：US1（第三階段，T005-T009）
- **開發者 B**：US2（第四階段，T010-T015）
- **開發者 C**：US3（第五階段，T016-T020）

---

## 附註

- `[P]` 任務 = 不同檔案，無依賴衝突，可平行執行
- `[Story]` 標籤將任務對應至特定使用者故事，確保可追溯性
- 每個使用者故事應可獨立完成與測試
- 本系統大部分元件已存在——任務著重於**補全缺失**（T007-T009、T010-T015、T016-T020）及**建立新 DTO**（T002-T004）
- 遷移腳本（T001）必須在啟動伺服器**前**執行
- 每完成一個任務或邏輯群組後提交
- 可在任一檢查點暫停並獨立驗證該故事
- 避免：模糊的任務描述、相同檔案衝突、破壞各故事獨立性的跨故事依賴

---

## 格式驗證檢查清單

> 確認所有任務遵循 `- [ ] TXXX [P?] [USn?] 描述（含檔案路徑）` 格式

- [x] 所有任務以 `- [ ]` 開頭（markdown 核取方塊）
- [x] 所有任務含序號 ID（T001-T023）
- [x] 使用者故事階段任務（第三至五階段）均含 `[USn]` 標籤
- [x] 設定/基礎/收尾階段任務無 Story 標籤
- [x] 平行任務含 `[P]` 標籤
- [x] 所有任務均含具體檔案路徑
