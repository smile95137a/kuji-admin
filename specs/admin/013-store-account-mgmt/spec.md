# 前端規格書（後台）：店家帳號管理

**功能分支**：`013-store-account-mgmt`
**對應後端 Spec**：`specs/013-store-account-mgmt/spec.md`
**建立日期**：2026-03-27
**狀態**：草稿
**介面類型**：後台管理介面（Admin Panel）
**存取角色**：ROLE_ADMIN

---

## 頁面與介面清單

### 頁面 1 — 帳號列表頁
- **路由**：`/admin/accounts`
- **存取權限**：ROLE_ADMIN
- **UI 元件**：
  - 頁面標題「店家帳號管理」
  - 篩選列：
    - 角色下拉（全部 / 店家管理員 / 店家編輯）
    - 狀態下拉（全部 / 待啟用 / 啟用 / 停用）
    - 店家下拉（全部 / 特定店家）
    - 關鍵字搜尋（Email、顯示名稱）
  - 操作按鈕群（右上角）：
    - 「新增店家管理員」按鈕（建立 StoreOwner + 店家，原子操作）
    - 「新增店家編輯」按鈕（建立 StoreEditor，綁定現有店家）
  - 資料表格：
    - 欄位：Email、顯示名稱、角色徽章、關聯店家、帳號狀態徽章、建立時間、最後登入時間、操作
    - 角色徽章：店家管理員（藍）、店家編輯（灰藍）
    - 狀態徽章：待啟用（黃）、啟用（綠）、停用（灰）
    - 操作：查看詳情、停用 / 啟用、重置密碼（可選）

### 頁面 2 — 新增店家管理員（StoreOwner）Wizard/表單
- **路由**：`/admin/accounts/create-owner`（或 Modal）
- **存取權限**：ROLE_ADMIN
- **UI 元件**：
  - 頁面標題「新增店家管理員」
  - 說明文字：「此操作將同時建立店家管理員帳號與關聯店家，為不可分割的原子操作。」
  - 步驟 1 — 帳號資訊：
    - Email（必填，格式驗證）
    - 顯示名稱（必填）
    - 手機號碼（可選）
    - 備註（可選）
    - 提示：「系統將自動生成初始密碼（8-12 字元）並發送至 Email，首次登入須強制修改密碼。」
  - 步驟 2 — 店家資訊（新建店家）：
    - 店家名稱（必填）
    - 簡短描述（必填，最多 100 字）
    - 完整描述（可選，富文本）
    - Logo 圖片上傳
    - 封面圖片上傳
    - 店家 Email（可選）
    - 店家電話（可選）
    - 店家地址（可選）
  - 送出按鈕：「建立帳號與店家」
  - 取消按鈕

### 頁面 3 — 新增店家編輯（StoreEditor）Modal
- **路由**：`/admin/accounts`（Modal 覆蓋）
- **存取權限**：ROLE_ADMIN
- **UI 元件**：
  - Modal 標題「新增店家編輯帳號」
  - Email（必填，格式驗證）
  - 顯示名稱（必填）
  - 手機號碼（可選）
  - 關聯店家選擇器（多選，至少選一間）
  - 備註（可選）
  - 提示：「系統將自動生成初始密碼並發送至 Email。」
  - 確認 / 取消按鈕

### 頁面 4 — 帳號詳情頁
- **路由**：`/admin/accounts/{id}`
- **存取權限**：ROLE_ADMIN
- **UI 元件**：
  - 返回列表按鈕
  - 帳號基本資訊卡片（唯讀）：Email、顯示名稱、手機、角色、狀態、建立時間、最後登入時間
  - 關聯店家資訊（可點擊跳轉店家管理頁）
  - 強制修改密碼狀態（是/否）
  - 帳號操作區：停用 / 啟用按鈕
  - 備註顯示

### 頁面 5 — 停用 / 啟用帳號確認 Modal
- **路由**：`/admin/accounts/{id}`（Modal 覆蓋）
- **存取權限**：ROLE_ADMIN
- **UI 元件**：
  - Modal 標題「停用帳號」 / 「啟用帳號」
  - 停用警告：「停用後，{displayName}（{email}）的所有 JWT Token 將立即失效，帳號無法登入。」
  - 確認 / 取消按鈕

---

## 使用者情境與測試

### 使用者故事 1 — Admin 同時建立店家管理員帳號與店家（優先級：P1）

Admin 為新合作的店家建立系統帳號，一次性完成帳號與店家資訊的創建，確保操作原子性。

**此優先級的原因**：新店家上線的必要流程，是其他所有店家功能的前提。

**獨立測試**：Admin 登入，建立一個新的 StoreOwner 帳號並輸入店家基本資料，確認帳號和店家均成功建立，系統發送初始密碼 Email。

**驗收情境**：
1. **在** 新增店家管理員表單，**當** 完整填寫帳號資訊（Email、顯示名稱）與店家資訊（店家名稱、簡短描述）並送出，**則** 新帳號出現在帳號列表（狀態：待啟用），新店家出現在店家管理列表。
2. **在** 新增流程送出後，**當** 使用目標 Email 查看，**則** 該 Email 收到包含初始密碼的系統郵件。
3. **在** Email 欄位填寫已存在的帳號 Email，**當** 送出，**則** 顯示錯誤「此 Email 已被使用」。
4. **在** 步驟 2 店家資訊未填寫店家名稱，**當** 嘗試送出，**則** 驗證錯誤「店家名稱為必填欄位」，停留在當前步驟。

---

### 使用者故事 2 — Admin 建立店家編輯帳號並綁定多間店家（優先級：P1）

特定編輯人員需要協助管理多間店家，Admin 建立 StoreEditor 帳號並設定多店家存取權限。

**此優先級的原因**：StoreEditor 角色是店家日常運營的支援帳號，建立流程需清晰。

**驗收情境**：
1. **在** 新增店家編輯 Modal，**當** 填寫 Email、顯示名稱並選擇 3 間店家送出，**則** 帳號建立成功，可在帳號詳情頁查看 3 間關聯店家。
2. **在** 未選擇任何關聯店家，**當** 嘗試送出，**則** 顯示驗證錯誤「請至少選擇一間關聯店家」。

---

### 使用者故事 3 — Admin 停用違規帳號（優先級：P1）

發現帳號違規使用後，Admin 需要立即停用，確保對應 JWT Token 立即失效。

**此優先級的原因**：帳號停用是安全控制的關鍵操作，需要即時生效。

**驗收情境**：
1. **在** 帳號狀態為「啟用」，**當** Admin 點擊停用並確認，**則** 帳號狀態徽章變為「停用（灰）」，被停用帳號下次 API 請求返回 401。
2. **在** 帳號已停用，**當** Admin 點擊「啟用」，**則** 帳號狀態恢復「啟用」，可正常登入。
3. **在** Admin 嘗試停用自己的帳號，**當** 點擊停用，**則** 顯示錯誤「不可停用當前登入帳號」。

---

### 使用者故事 4 — Admin 查看特定店家的所有帳號（優先級：P2）

Admin 管理某間店家時，需要快速查看與此店家關聯的所有帳號（Owner + Editor）。

**驗收情境**：
1. **在** 帳號列表，**當** Admin 篩選「店家：{店家名稱}」，**則** 只顯示關聯此店家的帳號。
2. **在** 店家管理頁面，**當** 點擊「查看帳號」（跨頁連結），**則** 導向帳號列表並預設此店家篩選。

---

### 邊界情況

- Email 格式不符（如少 `@` 或域名），即時顯示「Email 格式不正確」。
- 顯示名稱含特殊字元，後端驗證決定（前端僅提示長度限制）。
- 建立 StoreOwner 時，若後端原子操作失敗（如店家名稱重複），整個操作回滾，顯示具體錯誤。
- 帳號列表超過 100 筆，分頁功能需正常運作。
- 「待啟用」狀態的帳號，停用 / 啟用按鈕如何處理需確認（是否允許強制啟用跳過首次密碼修改？）。

---

## API 串接規格

### 帳號管理

| 動作 | Method | 端點 | 主要 Request 欄位 | 主要 Response 欄位 | 前端 UI 狀態 |
|------|--------|------|------------------|--------------------|-------------|
| 取得帳號列表 | GET | `/api/admin/users` | — | `[{id, email, displayName, status, roles, storeId, storeName, createdAt, lastLoginAt}]` | loading → 表格渲染 |
| 取得帳號詳情 | GET | `/api/admin/users/{id}` | — | `{id, email, displayName, status, roles, storeId, storeName, createdAt, lastLoginAt}` | loading → 詳情渲染 |
| 建立店家管理員 | POST | `/api/admin/users/store-owner` | `email, displayName, phone, remark, storeName, shortDescription, longDescription, logoUrl, coverImageUrl, storeEmail, storePhone, storeAddress` | `{id, email, displayName, status, roles, storeId, storeName, createdAt}` | 按鈕 loading → Toast → 導向列表 |
| 建立店家編輯 | POST | `/api/admin/users/store-editor` | `email, displayName, phone, storeId, remark` | `{id, email, displayName, status, roles, storeId, storeName, createdAt}` | Modal loading → Toast → 列表更新 |
| 停用帳號 | POST | `/api/admin/users/{id}/deactivate` | — | `{id, status: "INACTIVE"}` | Modal loading → 狀態徽章更新 |
| 啟用帳號 | POST | `/api/admin/users/{id}/activate` | — | `{id, status: "ACTIVE"}` | 按鈕 loading → 狀態徽章更新 |
| 查詢特定店家帳號 | GET | `/api/admin/users/store/{storeId}` | — | `[{id, email, displayName, status, roles}]` | loading → 列表渲染 |

---

## 功能需求（前端 UI）

### 介面需求

- **FR-UI-001**：帳號狀態徽章三種：待啟用（黃底「首次登入前」）、啟用（綠）、停用（灰）。
- **FR-UI-002**：角色徽章：店家管理員（藍底白字「管理員」）、店家編輯（灰藍底白字「編輯」）。
- **FR-UI-003**：新增店家管理員採用分步驟表單（2 步驟），每步有前進 / 返回按鈕，進度指示器顯示當前步驟。
- **FR-UI-004**：「最後登入時間」欄位若帳號從未登入，顯示「尚未登入」（對應後端 lastLoginAt 為 null）。
- **FR-UI-005**：帳號列表依「建立時間 DESC」預設排序，可切換排序。
- **FR-UI-006**：停用帳號的確認 Modal 需顯示具體帳號 Email，避免誤操作。
- **FR-UI-007**：StoreEditor 的「關聯店家」欄位若關聯多店家，顯示「{第一個店家名稱} 等 {N} 間店家」，hover 時 tooltip 顯示所有店家名稱。
- **FR-UI-008**：新增店家管理員表單中的圖片上傳（Logo、封面）顯示預覽，非必填。

### 狀態管理

- **SM-001**：帳號列表載入使用 skeleton loading。
- **SM-002**：建立成功後，Toast「帳號已建立，初始密碼已發送至 {email}」，返回帳號列表並以新帳號高亮顯示。
- **SM-003**：停用 / 啟用成功後，即時更新列表對應列的狀態徽章（樂觀更新）。
- **SM-004**：分步驟表單中，步驟 1 的資料在切換到步驟 2 時保留，返回步驟 1 時不丟失。
- **SM-005**：篩選條件變更自動觸發查詢（關鍵字 debounce 300ms）。

---

## API 驗證清單

- **AV-001** ✅：`POST /api/admin/users/store-owner` 同時包含帳號和店家欄位 — 原子建立操作在單一 API 中完成。
- **AV-002** ✅：`POST /api/admin/users/{id}/deactivate` 和 `/activate` 為獨立端點 — 停用 / 啟用操作清晰。
- **AV-003** ⚠️：`GET /api/admin/users` 無篩選 / 分頁參數說明 — 後端若無篩選支援，大量帳號需全量載入後前端篩選，可能效能不足；需確認是否支援 query param 篩選。
- **AV-004** ⚠️：`POST /api/admin/users/store-editor` 的 `storeId` 為單值 — 後端 FR-009 說明 StoreEditor 可關聯多間店家，但建立 API Body 中 `storeId` 似乎為單值；需確認是否應為 `storeIds: []` 陣列，否則無法在建立時指定多間店家。
- **AV-005** ⚠️：`status` 欄位的可能值 — 後端說明 3 種狀態（pending / activated / disabled），需確認 API Response 中 `status` 的實際 enum 值（如 `PENDING`/`ACTIVE`/`INACTIVE`）以便前端徽章映射。
- **AV-006** ❌：帳號 `roles` 欄位格式 — `GET /api/admin/users` Response 包含 `roles` 欄位，但未說明是字串、陣列還是其他格式；前端需對應渲染角色徽章，需確認 roles 結構。
- **AV-007** ❌：停用當前登入帳號的後端防護 — 後端 spec 未說明是否防止 Admin 停用自己的帳號；前端需在 UI 層做判斷（比對當前登入 userId）並 disabled 對應按鈕，但後端也需有保護機制。
- **AV-008** ✅：`GET /api/admin/users/store/{storeId}` — 可查詢特定店家的帳號列表，支援跨頁跳轉篩選功能。

凡例：✅ 已確認一致 | ⚠️ 需要確認 | ❌ 發現缺口或不一致

---

## 成功標準

### 可量化的成果

- **SC-001**：Admin 可在 5 分鐘內完成一個新店家管理員帳號 + 店家的完整建立流程。
- **SC-002**：帳號列表在 100 筆以內，載入時間不超過 2 秒。
- **SC-003**：停用帳號後，目標帳號的 JWT Token 在 1 分鐘內失效（後端保證，前端顯示成功即可）。
- **SC-004**：分步驟表單在步驟切換時，已填寫的資料 100% 保留（不丟失）。
- **SC-005**：Email 重複錯誤在送出後 3 秒內顯示，並聚焦到 Email 欄位。
- **SC-006**：初始密碼發送提示在建立成功的 Toast 中明確顯示目標 Email，管理員可確認通知已送出。
