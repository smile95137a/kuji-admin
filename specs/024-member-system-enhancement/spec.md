# Feature Specification: 會員系統完善

**Feature Branch**: `024-member-system-enhancement`  
**Created**: 2026-04-17  
**Status**: Draft  
**Input**: 完善前後台會員制度：Email驗證強制執行、登入失敗帳號鎖定、前台Logout、登入記錄追蹤、後台操作審計日誌、Admin手動調整用戶點數

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Email 驗證強制執行 (Priority: P1)

前台用戶透過 Email/密碼方式註冊後，必須先點擊驗證信才能登入。Google OAuth 用戶不受此限制（Google 本身已驗證）。

**Why this priority**: 防止用垃圾信箱大量建帳，確保聯絡管道真實有效，是帳號安全的基礎。

**Independent Test**: 可透過「完整走一遍 Email 註冊 → 嘗試登入 → 收到驗證信 → 點擊驗證 → 正常登入」流程獨立測試。

**Acceptance Scenarios**:

1. **Given** 用戶以 Email/密碼方式完成註冊，**When** 未點擊驗證信就嘗試登入，**Then** 系統返回 `EMAIL_NOT_VERIFIED` 錯誤，並提示用戶去驗證。
2. **Given** 用戶點擊驗證信中的連結，**When** token 有效且未過期（24小時內），**Then** Email 設定為已驗證，用戶可正常登入。
3. **Given** 用戶點擊已過期的驗證連結，**When** token 超過 24 小時，**Then** 系統返回過期錯誤，並提示用戶重新發送。
4. **Given** 用戶以 Google OAuth 方式登入，**When** 帳號尚未設置 email_verified，**Then** 系統自動標記為已驗證，不需再進行驗證流程。

---

### User Story 2 - 登入失敗帳號鎖定 (Priority: P1)

前台與後台用戶連續登入失敗 5 次後，帳號自動鎖定 15 分鐘，防止暴力破解攻擊。

**Why this priority**: 帳號鎖定機制是最基本的安全保護，無此機制密碼可被暴力破解。

**Independent Test**: 可透過連續嘗試錯誤密碼 5 次後驗證鎖定回應，獨立測試此機制。

**Acceptance Scenarios**:

1. **Given** 用戶連續輸入錯誤密碼，**When** 失敗第 3 次，**Then** 系統回應「密碼錯誤，還剩 2 次機會」。
2. **Given** 用戶連續輸入錯誤密碼，**When** 失敗第 5 次，**Then** 帳號鎖定 15 分鐘，返回 `ACCOUNT_LOCKED` 及剩餘解鎖時間。
3. **Given** 帳號已鎖定，**When** 在鎖定期間嘗試登入（即使密碼正確），**Then** 仍返回鎖定錯誤及剩餘分鐘數。
4. **Given** 帳號鎖定 15 分鐘後，**When** 再次嘗試登入（正確密碼），**Then** 登入成功，失敗次數重置。
5. **Given** 帳號已鎖定，**When** 後台 Admin 手動解鎖，**Then** 用戶可立即登入，解鎖操作記錄至審計日誌。

---

### User Story 3 - 前台用戶 Logout (Priority: P2)

前台用戶點擊登出後，舊的 token 在 server 端立即失效，無法繼續使用。

**Why this priority**: 確保用戶登出後不被他人利用舊 token 操作，尤其公共裝置使用後的安全。

**Independent Test**: 可透過「登出 → 用舊 token 呼叫 API → 返回 401」獨立驗證。

**Acceptance Scenarios**:

1. **Given** 用戶已登入並持有有效 token，**When** 呼叫 `POST /api/auth/logout`，**Then** 返回 200，server 端撤銷 token。
2. **Given** 用戶已登出，**When** 使用舊的 access token 呼叫任何需要認證的 API，**Then** 返回 401 Unauthorized。
3. **Given** 用戶已登出，**When** 使用舊的 refresh token 嘗試換新 token，**Then** 返回 401，無法取得新 token。

---

### User Story 4 - 登入記錄追蹤 (Priority: P2)

系統記錄每次登入嘗試（成功/失敗/鎖定），後台 Admin 可查詢特定用戶的登入歷史，包含時間、IP、裝置資訊。

**Why this priority**: 為異常行為調查提供依據，協助客服處理帳號安全問題。

**Independent Test**: 可透過「登入幾次 → Admin 查詢登入記錄 → 確認記錄筆數與內容」獨立驗證。

**Acceptance Scenarios**:

1. **Given** 用戶成功登入，**When** Admin 查詢該用戶登入記錄，**Then** 看到一筆狀態為 SUCCESS 的記錄，含登入時間與 IP。
2. **Given** 用戶登入失敗，**When** Admin 查詢該用戶登入記錄，**Then** 看到狀態為 FAILED 的記錄，含失敗原因。
3. **Given** 用戶因鎖定無法登入，**When** Admin 查詢記錄，**Then** 看到狀態為 LOCKED 的記錄。

---

### User Story 5 - 後台操作審計日誌 (Priority: P2)

後台所有敏感操作（修改帳號狀態、調整點數、解鎖帳號等）均自動記錄操作者、操作內容、修改前後的值。

**Why this priority**: 解決管理責任歸屬問題，每筆資料異動都能追溯到具體操作人。

**Independent Test**: 可透過「Admin 操作修改用戶狀態 → 查詢審計日誌 → 確認記錄存在且正確」獨立驗證。

**Acceptance Scenarios**:

1. **Given** Admin 修改前台用戶狀態為 INACTIVE，**When** 查詢審計日誌，**Then** 看到一筆記錄含操作者、前後狀態值。
2. **Given** Admin 手動調整用戶金幣，**When** 查詢審計日誌，**Then** 看到調整前後的餘額與備註。
3. **Given** Admin 可依時間範圍、操作者、操作類型過濾日誌，**When** 使用這些條件查詢，**Then** 僅返回符合條件的記錄。

---

### User Story 6 - Admin 手動調整用戶點數 (Priority: P3)

後台 Admin 可對前台用戶手動增加或扣除金幣/紅利金，必須填寫原因，並自動產生 wallet_transaction 記錄。

**Why this priority**: 客服補償、系統誤差修正的必要工具，沒有此功能只能直接操作資料庫（風險高）。

**Independent Test**: 可透過「Admin 給用戶 +500 金幣 → 查詢用戶餘額 → 查詢交易記錄 → 查詢審計日誌」獨立驗證。

**Acceptance Scenarios**:

1. **Given** Admin 對用戶執行 +500 金幣操作並填寫備註，**When** 操作完成，**Then** 用戶餘額增加 500，wallet_transaction 記錄一筆 ADMIN_ADJUST 類型，審計日誌記錄此操作。
2. **Given** Admin 嘗試扣除超過用戶當前餘額的金幣，**When** 提交操作，**Then** 系統返回錯誤「餘額不足，無法扣除」。
3. **Given** Admin 未填寫備註就提交調整，**When** 提交，**Then** 系統返回驗證錯誤「備註為必填」。

---

### Edge Cases

- 驗證信連結點擊兩次（token 已使用）：返回「此連結已使用」，不重複驗證。
- 帳號已鎖定期間收到新的登入請求：返回鎖定錯誤，不計入失敗次數。
- 用戶刪除帳號後仍嘗試登入：返回「帳號不存在」，不透露刪除資訊。
- 同一 IP 大量請求重送驗證信：僅在上一封驗證信已過 60 秒後才允許重送（防濫用）。
- 調整點數後餘額恰好為 0：允許，不視為錯誤。

---

## Requirements *(mandatory)*

### Functional Requirements

**Email 驗證**
- **FR-001**: Email/密碼方式註冊後，系統 MUST 自動發送驗證信，信中包含有效期 24 小時的驗證連結。
- **FR-002**: 未驗證 Email 的用戶 MUST NOT 能成功登入（Email 登入方式）。
- **FR-003**: 系統 MUST 提供重新發送驗證信的 API，且同一封信 60 秒內不重複發送。
- **FR-004**: Google OAuth 登入的用戶 MUST 自動設定 Email 為已驗證。

**帳號鎖定**
- **FR-005**: 系統 MUST 追蹤連續登入失敗次數（前台 user 表、後台 admin_user 表）。
- **FR-006**: 連續失敗 5 次後，系統 MUST 鎖定帳號 15 分鐘。
- **FR-007**: 鎖定期間任何登入嘗試 MUST 返回剩餘鎖定時間（分鐘）。
- **FR-008**: 登入成功後，系統 MUST 重置失敗次數為 0。
- **FR-009**: 後台 Admin MUST 能手動解除前台用戶的帳號鎖定。

**前台 Logout**
- **FR-010**: 系統 MUST 提供 `POST /api/auth/logout` endpoint。
- **FR-011**: 登出後，舊的 access token 與 refresh token MUST 立即失效（server-side 撤銷）。

**登入記錄**
- **FR-012**: 每次登入嘗試（成功/失敗/鎖定）MUST 記錄到 user_login_history 表。
- **FR-013**: 記錄內容 MUST 包含：用戶ID、時間、IP位址、裝置資訊、登入方式、結果狀態。
- **FR-014**: 後台 MUST 提供查詢特定用戶登入記錄的 API，支援分頁。

**審計日誌**
- **FR-015**: 後台所有敏感操作 MUST 自動記錄至 admin_audit_log，包含操作者ID、操作類型、操作對象、修改前後值。
- **FR-016**: 後台 MUST 提供審計日誌查詢 API，支援依操作者、時間範圍、操作類型過濾。

**手動點數調整**
- **FR-017**: Admin MUST 能透過 API 手動增加或扣除用戶的金幣（GOLD）或紅利金（BONUS）。
- **FR-018**: 調整操作 MUST 要求填寫備註（必填）。
- **FR-019**: 調整後 MUST 自動在 wallet_transaction 產生 `ADMIN_ADJUST` 類型的交易記錄。
- **FR-020**: 扣除操作 MUST 檢查餘額，不得使餘額低於 0。
- **FR-021**: 點數調整操作 MUST 記錄至 admin_audit_log。

**錢包資料一致性**
- **FR-022**: 系統 MUST 移除對 user_wallet 表的所有讀寫，以 user 表欄位為唯一點數來源。

### Key Entities

- **UserLoginHistory**: 登入歷史記錄，關聯用戶ID，記錄時間/IP/裝置/結果。
- **AdminAuditLog**: 後台操作審計，記錄操作者、目標、操作前後值（JSON）。
- **UserTokenBlacklist**: 前台 token 世代計數器，用於撤銷用戶所有 token。
- **WalletTransaction**: 點數異動記錄（已存在），新增 ADMIN_ADJUST 類型。

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Email 驗證流程從收到驗證信到完成驗證可在 2 分鐘內完成。
- **SC-002**: 帳號鎖定機制在連續失敗 5 次後 100% 觸發，無例外。
- **SC-003**: 前台登出後，舊 token 在 1 秒內完全失效，任何使用舊 token 的請求返回 401。
- **SC-004**: 所有登入嘗試（成功、失敗、鎖定）100% 被記錄，無遺漏。
- **SC-005**: 所有後台敏感操作 100% 生成審計日誌，包含完整的修改前後值。
- **SC-006**: Admin 手動調整點數後，用戶餘額與 wallet_transaction 記錄 100% 一致。
- **SC-007**: 移除 user_wallet 表後，系統功能 100% 正常（無讀寫錯誤）。

---

## Assumptions

- 驗證信實際發送機制已存在（EmailService / SMTP 已配置）。
- 前台 token 撤銷採用與後台相同的 generation counter 策略（user_token_blacklist 表）。
- IP 位址從請求的 `X-Forwarded-For` header 優先取得，fallback 至 `RemoteAddr`。
- 帳號鎖定僅適用於 Email/密碼登入，不適用於 OAuth 登入。
- 手動點數調整權限僅限 ROLE_ADMIN，STORE_OWNER 不可調整。
- user_wallet 表遷移：遷移腳本執行後才 DROP TABLE。
