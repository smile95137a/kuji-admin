# Feature Specification: 前台前端 — Email 驗證流程與登入安全 UX

**Feature Branch**: `026-frontend-auth-ux`  
**Created**: 2026-04-18  
**Status**: Draft  
**Backend**: Feature 024 已完成所有 API，本 spec 為前台前端對應的 UX 改版規格  
**Input**: 前台前端：Email 驗證流程與登入安全 UX（配合 Feature 024）

## API 對照（後端已就緒）

| 功能 | Method | Path |
|------|--------|------|
| 登入（新增鎖定回應欄位） | POST | `/api/auth/login` |
| 登出（撤銷 server token） | POST | `/api/auth/logout` |
| Email 驗證（點擊連結） | GET | `/api/auth/verify-email?token=xxx` |
| 重新發送驗證信 | POST | `/api/auth/resend-verification` |

### 登入回應格式（新增欄位）

成功時同原有格式。失敗時新增欄位：
```json
{
  "errorCode": "ACCOUNT_LOCKED",       // 或 "EMAIL_NOT_VERIFIED", "INVALID_CREDENTIALS"
  "remainingAttempts": 2,             // 還剩幾次機會（未鎖定時）
  "lockedMinutes": 13                 // 鎖定剩餘分鐘（已鎖定時）
}
```

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 前台登出正確撤銷 Server Token (Priority: P1)

用戶點擊登出按鈕時，前端除了清除本地儲存的 token，還必須呼叫後端 API 讓 token 在 server 端失效，確保舊 token 無法被繼續使用。

**Why this priority**: 現有前端可能只做本地清除（清 localStorage），Feature 024 已在後端實作 server-side 撤銷，前端必須改成呼叫 API 才能真正生效。若不更新，後端的安全機制形同虛設。

**Independent Test**: 登入後複製 token → 點擊登出 → 用複製的舊 token 呼叫 `GET /api/user/me` → 應返回 401。

**Acceptance Scenarios**:

1. **Given** 用戶已登入，**When** 點擊登出按鈕，**Then** 前端呼叫 `POST /api/auth/logout`，成功後清除本地 token，導向首頁或登入頁。
2. **Given** 用戶已登出，**When** 使用舊 token 訪問需要認證的頁面，**Then** 返回 401，前端導向登入頁。
3. **Given** 呼叫登出 API 失敗（網路錯誤），**When** 發生錯誤，**Then** 前端仍清除本地 token 並導向登入頁（降級處理）。

---

### User Story 2 - Email 未驗證的登入錯誤與重送 (Priority: P1)

透過 Email/密碼方式註冊但尚未點擊驗證信的用戶，嘗試登入時需看到清楚的提示，並且能夠一鍵重送驗證信。

**Why this priority**: 這是 Feature 024 Email 驗證功能的前台入口。後端已開始返回 `EMAIL_NOT_VERIFIED` 錯誤碼，若前端不處理，用戶只會看到一般錯誤訊息，無法知道如何解決。

**Independent Test**: 用未驗證帳號嘗試登入 → 看到含重送按鈕的提示 → 點重送按鈕 → 確認收到新驗證信。

**Acceptance Scenarios**:

1. **Given** Email 未驗證的用戶嘗試登入，**When** 後端返回 `errorCode: "EMAIL_NOT_VERIFIED"`，**Then** 顯示「您的 Email 尚未驗證，請檢查收件匣或重新發送驗證信」，並顯示「重新發送」按鈕。
2. **Given** 顯示未驗證提示，**When** 用戶點擊「重新發送」，**Then** 呼叫 `POST /api/auth/resend-verification`，成功後顯示「驗證信已發送，請檢查您的 Email」，按鈕改為倒數 60 秒不可點擊。
3. **Given** 在 60 秒內再次點擊「重新發送」，**When** 後端拒絕（太頻繁），**Then** 按鈕保持倒數不可點擊狀態。

---

### User Story 3 - Email 驗證連結落地頁 (Priority: P1)

用戶點擊驗證信中的連結後，應看到清楚的驗證結果頁面，無論成功或失敗。

**Why this priority**: 若前端沒有對應路由，用戶點擊連結只會看到 404 或原始 JSON，無法完成驗證流程。

**Independent Test**: 複製驗證連結在瀏覽器開啟，確認顯示驗證成功頁面，並可點擊「前往登入」。

**Acceptance Scenarios**:

1. **Given** 用戶點擊驗證信中的連結（含 `?token=xxx`），**When** 前端路由接收到此連結，**Then** 自動呼叫 `GET /api/auth/verify-email?token=xxx`，顯示「驗證中...」loading 狀態。
2. **Given** token 有效，**When** 後端返回成功，**Then** 顯示「Email 驗證成功！您現在可以登入了」，並提供「前往登入」按鈕。
3. **Given** token 已過期或已使用，**When** 後端返回錯誤，**Then** 顯示「此驗證連結已失效，請重新申請」，並提供「重新發送驗證信」按鈕。

---

### User Story 4 - 登入失敗次數提示與鎖定倒數 (Priority: P2)

當用戶輸入錯誤密碼時，系統顯示剩餘嘗試次數；帳號被鎖定時，顯示剩餘鎖定時間，避免用戶困惑。

**Why this priority**: 後端已有完整的鎖定機制，但前端若只顯示「密碼錯誤」，用戶不知道還剩幾次機會，體驗很差。鎖定提示更是必要，否則用戶以為是系統錯誤。

**Independent Test**: 連續輸入 3 次錯誤密碼，確認提示「還剩 2 次機會」；第 5 次後確認顯示鎖定剩餘時間。

**Acceptance Scenarios**:

1. **Given** 用戶輸入錯誤密碼，**When** 後端返回 `remainingAttempts: 2`，**Then** 顯示「密碼錯誤，還剩 2 次機會，超過將鎖定帳號 15 分鐘」。
2. **Given** 用戶第 5 次輸入錯誤密碼，**When** 後端返回 `errorCode: "ACCOUNT_LOCKED", lockedMinutes: 15`，**Then** 顯示「帳號已鎖定，請 15 分鐘後再試」，登入按鈕變為不可點擊，顯示倒數計時（選用）。
3. **Given** 帳號已鎖定，**When** 用戶再次嘗試登入，**Then** 顯示剩餘鎖定分鐘數（如「帳號鎖定中，剩餘 8 分鐘」）。

---

### Edge Cases

- 重新發送驗證信按鈕的 60 秒倒數：頁面重整後倒數可重置（不必持久化）。
- 驗證連結落地頁：若用戶已登入狀態點擊驗證連結，驗證完成後導向個人資料頁而非登入頁。
- 驗證成功後，若用戶是從登入錯誤（EMAIL_NOT_VERIFIED）流程過來，驗證完成後可直接顯示「驗證成功，請重新登入」並跳轉登入頁。
- 登出 API 需攜帶 Authorization header（用戶已登入的 token）。
- Google OAuth 用戶不會有 Email 未驗證問題，不需顯示驗證相關提示。

---

## Requirements *(mandatory)*

### Functional Requirements

**登出**
- **FR-001**: 所有登出按鈕（含 Header / 側欄）MUST 改為先呼叫 `POST /api/auth/logout`，再清除本地 token。
- **FR-002**: 登出 API 失敗時，前端 MUST 仍執行本地清除並導向登入頁（不因 API 失敗卡住用戶）。

**Email 驗證落地頁**
- **FR-003**: 前端路由 MUST 包含 `/verify-email` 頁面（或等效路徑），接收 `token` query parameter。
- **FR-004**: 進入驗證頁面時，MUST 自動呼叫後端 API，不需用戶手動點擊。
- **FR-005**: 驗證結果 MUST 分成成功/失敗兩種狀態頁面，成功提供「前往登入」，失敗提供「重新發送驗證信」。

**登入錯誤處理**
- **FR-006**: 登入失敗時，MUST 根據 `errorCode` 顯示對應訊息：
  - `INVALID_CREDENTIALS`：顯示「Email 或密碼錯誤」（若有 `remainingAttempts` 則附加次數）。
  - `EMAIL_NOT_VERIFIED`：顯示未驗證提示 + 重送按鈕。
  - `ACCOUNT_LOCKED`：顯示鎖定訊息 + 剩餘分鐘數。
- **FR-007**: 重送驗證信按鈕 MUST 在成功送出後進入 60 秒冷卻倒數，期間不可點擊。

**重送驗證信**
- **FR-008**: `POST /api/auth/resend-verification` 需攜帶 Authorization header（用未驗證用戶的 token）。
- **FR-009**: 後端若返回 429（太頻繁），MUST 顯示「請稍後再試」並維持按鈕不可點擊。

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 登出操作從點擊到完成導向，用戶感知不超過 1 秒（含 API 呼叫）。
- **SC-002**: Email 驗證落地頁從開啟到顯示結果不超過 3 秒。
- **SC-003**: 帳號鎖定時，用戶能清楚知道需要等待多久（剩餘分鐘數可見），客服詢問「無法登入」的比例降低 50%。
- **SC-004**: 所有登入錯誤情境（密碼錯誤、未驗證、鎖定）100% 顯示有意義的錯誤訊息，不出現空白或技術性錯誤。

---

## Assumptions

- 後端 API 已全部完成（Feature 024），前端只需對接。
- 前台前端框架已有 toast / notification 組件可複用。
- 驗證連結 URL 由後端郵件中嵌入，格式為 `https://{domain}/verify-email?token=xxx`。
- 前端路由需新增 `/verify-email` 且此頁面為 **公開路由**（無需登入）。
- `POST /api/auth/resend-verification` 在 Feature 024 之前無對應前端功能，需全新開發。
- `resend-verification` API 需要 Authorization header（用戶的 access token），前端需在未驗證狀態下暫存 token。
