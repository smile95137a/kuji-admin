# Feature Specification: 後台前端 — 會員管理模組

**Feature Branch**: `025-admin-member-mgmt`  
**Created**: 2026-04-18  
**Status**: Draft  
**Backend**: Feature 024 已完成所有 API，本 spec 為後台前端對應的 UI 開發規格  
**Input**: 後台前端：會員管理模組（配合 Feature 024）

## API 對照（後端已就緒）

| 功能 | Method | Path |
|------|--------|------|
| 查詢會員列表（含篩選） | POST | `/admin/frontend-users/list` |
| 取得會員詳情 | GET | `/admin/frontend-users/{id}` |
| 更新會員資料 | PUT | `/admin/frontend-users/{id}` |
| 啟用會員 | POST | `/admin/frontend-users/{id}/activate` |
| 停用會員 | POST | `/admin/frontend-users/{id}/deactivate` |
| 暫停會員 | POST | `/admin/frontend-users/{id}/suspend` |
| 解鎖帳號 | POST | `/admin/frontend-users/{id}/unlock` |
| 查看登入歷史 | GET | `/admin/frontend-users/{id}/login-history` |
| 手動調整點數 | POST | `/admin/frontend-users/{id}/coin-adjust` |

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 會員列表與搜尋 (Priority: P1)

後台管理人員進入「會員管理」頁面，可以看到所有前台會員的列表，並透過關鍵字（Email / 暱稱）與狀態篩選快速找到特定會員。

**Why this priority**: 這是所有會員管理操作的入口，其他功能都從列表頁開始。

**Independent Test**: 進入 `/admin/frontend-users` 頁面，確認列表顯示且搜尋功能可用，即可獨立驗收。

**Acceptance Scenarios**:

1. **Given** 管理者已登入後台，**When** 導覽到會員管理頁，**Then** 看到分頁會員列表，每筆顯示：暱稱、Email、狀態（ACTIVE/INACTIVE/SUSPENDED）、金幣/紅利餘額、建立時間。
2. **Given** 列表頁已顯示，**When** 在搜尋框輸入部分 Email 或暱稱，**Then** 列表即時過濾只顯示符合的結果。
3. **Given** 列表頁已顯示，**When** 選擇「狀態」篩選器（ACTIVE / INACTIVE / SUSPENDED / LOCKED），**Then** 列表只顯示該狀態的會員。
4. **Given** 篩選後的結果，**When** 點擊會員列，**Then** 導覽至該會員的詳情頁。

---

### User Story 2 - 會員帳號狀態操作 (Priority: P1)

管理者可以在會員詳情頁對帳號狀態進行操作：啟用、停用、暫停，以及解除帳號鎖定（針對因登入失敗被自動鎖定的帳號）。

**Why this priority**: 帳號管理是最核心的業務需求，客服處理異常帳號的必備工具。

**Independent Test**: 找一筆 INACTIVE 會員，點擊「啟用」，確認狀態變更後可獨立驗收。

**Acceptance Scenarios**:

1. **Given** 會員狀態為 INACTIVE，**When** 管理者點擊「啟用」按鈕並確認，**Then** 狀態更新為 ACTIVE，頁面顯示成功提示。
2. **Given** 會員狀態為 ACTIVE，**When** 管理者點擊「停用」，**Then** 顯示確認 Dialog，確認後狀態更新為 INACTIVE。
3. **Given** 會員帳號因連續登入失敗被系統鎖定（帳號顯示 `locked_until` 有值），**When** 管理者點擊「解鎖帳號」，**Then** 鎖定立即解除，會員可立即再次嘗試登入。
4. **Given** 解鎖操作，**When** 只有 ROLE_ADMIN 才可看到「解鎖帳號」按鈕（STORE_OWNER / STORE_EDITOR 不顯示此按鈕）。

---

### User Story 3 - 手動點數調整 (Priority: P2)

Admin 可以對特定會員手動增加或扣除金幣（GOLD）或紅利金（BONUS），並填寫調整原因，處理客服補償或系統誤差修正情境。

**Why this priority**: 避免客服直接操作資料庫，提供有稽核記錄的調整工具。

**Independent Test**: 對測試會員增加 100 金幣並填寫備註，確認餘額變更後可獨立驗收。

**Acceptance Scenarios**:

1. **Given** 在會員詳情頁，**When** Admin 點擊「調整點數」，**Then** 顯示調整表單，欄位包含：幣別（GOLD / BONUS）、調整類型（增加 / 扣除）、調整數量（正整數）、備註（必填）。
2. **Given** 填寫調整表單，**When** 備註為空就提交，**Then** 表單驗證阻止提交，顯示「備註為必填」。
3. **Given** 填寫完整表單，**When** 確認送出，**Then** 會員餘額即時更新，顯示調整後的新餘額。
4. **Given** 嘗試扣除超過當前餘額的金幣，**When** 送出，**Then** 顯示錯誤「餘額不足，無法扣除」。
5. **Given** 只有 ROLE_ADMIN 才可進行點數調整（STORE_OWNER / STORE_EDITOR 不顯示此功能）。

---

### User Story 4 - 登入歷史查看 (Priority: P3)

Admin 可以在會員詳情頁查看該會員的登入歷史記錄，包含時間、IP、裝置資訊、登入方式、登入結果。

**Why this priority**: 用於異常行為調查與客服支援，非日常操作功能。

**Independent Test**: 進入任意會員詳情頁，點擊「登入記錄」Tab，確認記錄清單顯示即可獨立驗收。

**Acceptance Scenarios**:

1. **Given** 在會員詳情頁，**When** 點擊「登入記錄」Tab，**Then** 顯示該會員最近 50 筆登入記錄，每筆含：時間、IP 位址、裝置資訊（User-Agent）、登入方式（EMAIL / GOOGLE）、結果（SUCCESS / FAILED / LOCKED）。
2. **Given** 登入記錄列表，**When** 結果為 FAILED，**Then** 顯示失敗原因（如「密碼錯誤」）。
3. **Given** 只有 ROLE_ADMIN 才可看到登入記錄 Tab（STORE_OWNER / STORE_EDITOR 不顯示）。

---

### Edge Cases

- 操作啟用/停用/暫停時，必須顯示確認 Dialog，防止誤觸。
- 點數調整的調整數量限制為正整數，不可輸入 0 或負數。
- 會員列表應顯示「上次登入時間」，若從未登入則顯示「從未登入」。
- 搜尋結果為空時，顯示空狀態提示（非錯誤）。
- 頁面需處理 API 401 回應（token 過期），自動導向後台登入頁。

---

## Requirements *(mandatory)*

### Functional Requirements

**會員列表頁**
- **FR-001**: 後台前端 MUST 提供「會員管理」頁面，路由建議為 `/admin/members` 或 `/admin/frontend-users`。
- **FR-002**: 列表 MUST 支援以下欄位顯示：暱稱、Email、狀態、金幣餘額、紅利餘額、建立時間、最後登入時間。
- **FR-003**: 列表 MUST 支援關鍵字搜尋（Email 或暱稱模糊比對）與狀態篩選。
- **FR-004**: 點擊列表中任一會員，MUST 導覽至該會員詳情頁。

**會員詳情頁**
- **FR-005**: 詳情頁 MUST 顯示完整會員資料（姓名、Email、電話、地址、金幣、紅利、狀態、Email 驗證狀態）。
- **FR-006**: 詳情頁 MUST 提供帳號狀態操作按鈕，依據當前狀態顯示對應可用動作（如 ACTIVE 時顯示停用/暫停，INACTIVE 時顯示啟用）。
- **FR-007**: 若帳號處於鎖定狀態（`locked_until` 有值且未過期），MUST 顯示解鎖按鈕（僅 ROLE_ADMIN 可見）。
- **FR-008**: 詳情頁 MUST 包含「調整點數」功能入口（僅 ROLE_ADMIN 可見）。
- **FR-009**: 詳情頁 MUST 包含「登入記錄」Tab 或區塊（僅 ROLE_ADMIN 可見）。

**點數調整**
- **FR-010**: 調整表單 MUST 包含幣別選擇（金幣 / 紅利金）、調整方向（增加 / 扣除）、數量（正整數）、備註（必填）。
- **FR-011**: 提交前 MUST 顯示確認 Dialog，告知調整後的預期新餘額。

**登入記錄**
- **FR-012**: 登入記錄 MUST 依時間倒序排列，最多顯示後端返回的 50 筆。
- **FR-013**: 每筆記錄 MUST 顯示：時間、IP、裝置資訊（可省略 User-Agent 細節）、登入方式、結果狀態（SUCCESS / FAILED / LOCKED）。

### Key Entities

- **FrontendUserRes**: 後端回傳的會員資料 DTO，包含 id、email、nickname、status、goldCoins、bonusCoins、emailVerified、lockedUntil、lastLoginAt。
- **CoinAdjustReq**: 點數調整請求，包含 coinType（GOLD/BONUS）、amount（正整數）、direction（ADD/DEDUCT）、remark（必填）。
- **UserLoginHistory**: 登入歷史，包含 userId、userType、loginTime、ipAddress、deviceInfo、loginMethod、status、failReason。

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 管理者從進入會員列表到完成一次帳號狀態變更（啟用/停用），整個操作可在 30 秒內完成。
- **SC-002**: 點數調整操作提交後，頁面立即反映新餘額，無需手動重整頁面。
- **SC-003**: 所有帳號操作（啟用、停用、暫停、解鎖、點數調整）必須有確認步驟，誤觸率降低 90%。
- **SC-004**: STORE_OWNER 和 STORE_EDITOR 無法看到解鎖按鈕、登入記錄、點數調整入口（100% 前端隱藏）。

---

## Assumptions

- 後端 API 已全部完成（Feature 024），前端只需對接。
- 後台前端框架已有通用的 Table、Dialog、Form 組件可複用。
- 角色判斷從 JWT token 的 `roles` 欄位取得，前端已有 `hasRole()` 工具函數。
- 帳號鎖定的判斷：`lockedUntil` 欄位非 null 且時間尚未過期。
- 列表頁前端自行分頁（後端返回全量資料），與專案現有慣例一致。
