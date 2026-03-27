# 前端規格書（後台）：RBAC 權限管理

**功能分支**：`009-rbac-permissions`
**對應後端 Spec**：`specs/009-rbac-permissions/spec.md`
**建立日期**：2026-03-27
**狀態**：草稿
**介面類型**：後台管理介面（Admin Panel）
**存取角色**：ROLE_ADMIN

---

## 頁面與介面清單

### 頁面 1 — 角色列表與權限總覽頁
- **路由**：`/admin/permissions`
- **存取權限**：ROLE_ADMIN
- **UI 元件**：
  - 頁面標題「角色與權限管理」
  - 角色卡片群組（水平排列）：
    - 每張角色卡：角色名稱（中文）、角色代碼（code）、說明、「管理權限」按鈕
    - 角色固定 3 種：系統管理員（ADMIN）、店家管理員（STORE_OWNER）、店家編輯（STORE_EDITOR）
  - 系統說明文字：「ADMIN 擁有所有權限，不可修改。STORE_EDITOR 權限不得超過 STORE_OWNER。」

### 頁面 2 — 角色權限設定頁
- **路由**：`/admin/permissions/{roleId}`
- **存取權限**：ROLE_ADMIN
- **UI 元件**：
  - 頁面標題「{角色名稱} 權限設定」
  - 返回按鈕 + 麵包屑
  - 說明文字（若為 ADMIN：「此角色擁有全域權限，不可修改」並以唯讀模式顯示）
  - 選單階層權限表格：
    - 表格欄位：選單名稱（縮排顯示父子關係）、可檢視（checkbox）、可編輯（checkbox）、可刪除（checkbox）
    - 父選單折疊／展開（Accordion）
    - 全選 / 全不選 按鈕（每列）
  - 「儲存權限設定」按鈕（底部固定）
  - 「重置為預設」按鈕

### 頁面 3 — 後台側邊欄（動態選單，所有後台頁面共用）
- **路由**：（全域 Layout 元件）
- **存取權限**：所有已登入後台角色
- **UI 元件**：
  - Logo 區域
  - 動態選單項目（依 `GET /api/admin/user/menu` 結果渲染）
  - 每個選單項目：Icon + 中文標籤 + 子選單（若有）
  - 選中狀態高亮
  - 折疊 / 展開側邊欄按鈕
  - 底部：使用者頭像 + 姓名 + 登出按鈕

---

## 使用者情境與測試

### 使用者故事 1 — Admin 設定 STORE_OWNER 角色的選單權限（優先級：P1）

Admin 決定哪些後台功能允許店家管理員使用，並設定其可檢視、可編輯、可刪除的權限細節。

**此優先級的原因**：RBAC 設定是整個後台安全模型的基礎，必須在店家帳號建立前完成。

**獨立測試**：Admin 登入進入 STORE_OWNER 權限設定頁，調整「訂單管理」選單的權限，儲存後以 StoreOwner 帳號登入確認反映正確。

**驗收情境**：
1. **在** STORE_OWNER 權限設定頁，**當** Admin 勾選「訂單管理」的可檢視和可編輯，取消可刪除，並點擊「儲存」，**則** 顯示「權限已更新」Toast，變更生效。
2. **在** 以 StoreOwner 帳號登入後，**當** 進入訂單管理，**則** 無「刪除」操作按鈕，但可看到訂單並更新狀態。
3. **在** 嘗試勾選 ADMIN 角色的任意權限，**當** 操作，**則** 所有 checkbox 均為 disabled（唯讀）。

---

### 使用者故事 2 — 後台側邊欄依登入角色動態渲染（優先級：P1）

不同角色登入後台後，側邊欄選單只顯示其有權存取的功能項目，無可見功能的選單項目不顯示。

**此優先級的原因**：動態側邊欄是 RBAC 的前端體現，安全性與使用體驗的核心。

**獨立測試**：分別以 Admin、StoreOwner、StoreEditor 登入，確認側邊欄項目數量和內容符合各角色設定。

**驗收情境**：
1. **在** Admin 登入後，**當** 後台載入，**則** 側邊欄顯示所有選單項目（無限制）。
2. **在** StoreOwner 登入後，**當** 後台載入，**則** 側邊欄僅顯示其有「可檢視」權限的選單，且不顯示「RBAC 權限管理」、「儲值方案管理」等 Admin 專屬功能。
3. **在** 使用者無任何可見選單，**當** 登入後，**則** 顯示「您目前沒有任何功能存取權限，請聯繫系統管理員。」

---

### 使用者故事 3 — Admin 設定 STORE_EDITOR 使其權限為 STORE_OWNER 的子集（優先級：P2）

STORE_EDITOR 的權限不得超過 STORE_OWNER，管理員需要在設定介面看到約束提示。

**此優先級的原因**：業務規則要求 STORE_EDITOR 權限為嚴格子集，需要前端輔助提示。

**驗收情境**：
1. **在** STORE_EDITOR 權限設定頁，**當** Admin 嘗試勾選某項 STORE_OWNER 未開放的權限，**則** 該 checkbox 為 disabled，tooltip「STORE_OWNER 未開放此權限，STORE_EDITOR 不可超過 STORE_OWNER」。
2. **在** STORE_OWNER 的權限被縮減後，**當** STORE_EDITOR 某項已勾選的權限超出新的 STORE_OWNER 範圍，**則** 系統（後端）自動取消多餘權限，前端顯示警告「STORE_EDITOR 的部分權限已因 STORE_OWNER 調整而自動縮減」。

---

### 邊界情況

- 儲存時網路失敗，權限設定未更新，顯示「儲存失敗，請重試」，checkbox 狀態回滾。
- 選單項目超過 20 筆時，表格需有滾動或折疊功能。
- 若後端 `GET /api/admin/user/menu` 返回空陣列，側邊欄顯示「無可用功能」提示。
- 角色名稱為空時，角色卡片顯示「未命名角色」作為 fallback。

---

## API 串接規格

### 角色與權限管理

| 動作 | Method | 端點 | 主要 Request 欄位 | 主要 Response 欄位 | 前端 UI 狀態 |
|------|--------|------|------------------|--------------------|-------------|
| 取得選單列表 | GET | `/api/admin/menus` | — | `[{id, name, path, parentId, sort, icon, enabled}]` | loading → 表格渲染 |
| 取得角色列表 | GET | `/api/admin/roles` | — | `[{id, name, code, description}]` | loading → 角色卡片渲染 |
| 取得角色權限 | GET | `/api/admin/roles/{roleId}/permissions` | — | `[{menuId, menuName, canView, canEdit, canDelete}]` | loading → 權限表格渲染 |
| 更新角色權限 | PUT | `/api/admin/roles/{roleId}/permissions` | `[{menuId, canView, canEdit, canDelete}]` | `{success: true}` | 按鈕 loading → Toast |
| 取得當前使用者選單 | GET | `/api/admin/user/menu` | — | `[{id, name, path, icon, children, permissions}]` | 頁面初始化載入 |

---

## 功能需求（前端 UI）

### 介面需求

- **FR-UI-001**：角色列表以 3 張卡片並排（或響應式調整），每張卡片顯示角色名稱（中文）、代碼（code）、簡短說明。
- **FR-UI-002**：ADMIN 角色卡片有「🔒 全域管理員，不可修改」標籤，點擊「管理權限」後所有 checkbox 為唯讀 disabled。
- **FR-UI-003**：權限設定表格以選單階層（縮排）呈現，父選單可展開折疊子選單。
- **FR-UI-004**：每列選單有「全選」快捷按鈕，一次勾選所有 3 個 checkbox（canView, canEdit, canDelete）。
- **FR-UI-005**：「可編輯」若被勾選，「可檢視」自動強制勾選（編輯前提是可以檢視）。
- **FR-UI-006**：「可刪除」若被勾選，「可檢視」自動強制勾選（同理）。
- **FR-UI-007**：側邊欄選單項目依 `sort` 欄位排序顯示，父子關係用縮排或 Accordion 表示。
- **FR-UI-008**：側邊欄選單項目的 icon 使用後端回傳的 `icon` 欄位（圖示名稱對應前端圖示庫）。
- **FR-UI-009**：權限更改後，「儲存」按鈕顯示「有未儲存的變更」視覺提示（如橘色邊框）。

### 狀態管理

- **SM-001**：側邊欄選單在使用者登入後初始化載入，並在 localStorage 快取（TTL 5 分鐘），減少重複 API 呼叫。
- **SM-002**：權限更新成功後顯示 Toast「權限已更新」，並清除選單快取，確保下次載入反映最新設定。
- **SM-003**：權限頁面初始狀態追蹤「是否有變更」，有變更才啟用「儲存」按鈕。
- **SM-004**：API 錯誤時顯示 Toast 錯誤，保留頁面現有 checkbox 狀態。
- **SM-005**：未授權的頁面存取（403）導向「無存取權限」頁面，不在側邊欄顯示該項目。

---

## API 驗證清單

- **AV-001** ✅：`GET /api/admin/user/menu` 回傳 `permissions:{canView, canEdit, canDelete}` — 前端可依此控制每個頁面的操作按鈕顯示。
- **AV-002** ✅：`GET /api/admin/roles/{roleId}/permissions` 回傳 `menuName` — 前端表格可直接顯示選單名稱無需額外查詢。
- **AV-003** ⚠️：選單的 `icon` 欄位格式 — 後端回傳 icon 名稱字串，需確認是圖示庫名稱（如 Material Icons、Heroicons）還是自訂格式，前端需對應正確圖示庫。
- **AV-004** ⚠️：STORE_EDITOR 超出 STORE_OWNER 的業務驗證 — 後端是否在 `PUT /api/admin/roles/{roleId}/permissions` 中驗證此約束？若後端無驗證，前端僅顯示提示但無法阻止，資料一致性存疑。
- **AV-005** ❌：ADMIN 角色 `PUT` 保護 — 後端是否拒絕對 ADMIN 角色的權限修改請求？前端雖設為 disabled，但需後端防護；若後端無保護，安全性存在風險。
- **AV-006** ⚠️：側邊欄 `children` 結構 — `GET /api/admin/user/menu` Response 的 `children` 為巢狀陣列，需確認最多幾層（前端需支援遞迴渲染），超過 2 層需調整 UI 設計。
- **AV-007** ✅：`GET /api/admin/menus` 回傳 `parentId` — 前端可用此建立選單樹狀結構。
- **AV-008** ❌：選單的 `enabled` 欄位 — 若某選單 `enabled: false`，對應的 feature 應完全隱藏；需確認前端是否需要過濾 `enabled: false` 的選單項目，以及此欄位是否出現在 `GET /api/admin/user/menu` 結果中。

凡例：✅ 已確認一致 | ⚠️ 需要確認 | ❌ 發現缺口或不一致

---

## 成功標準

### 可量化的成果

- **SC-001**：Admin 可在 3 分鐘內完成一個角色的完整權限設定（選單數量 ≤ 20）。
- **SC-002**：側邊欄渲染時間不超過 500ms（從 API 回傳到 DOM 渲染）。
- **SC-003**：不同角色登入後，側邊欄顯示的選單項目 100% 符合後端設定（無多餘選單項目）。
- **SC-004**：權限更新後，目標角色的使用者下次登入即時反映新的選單與操作權限（無需重新部署）。
- **SC-005**：ADMIN 角色的權限 checkbox 100% 為 disabled 狀態，防止誤操作。
- **SC-006**：側邊欄選單快取機制確保不同頁面間切換時不重複呼叫 `GET /api/admin/user/menu`。
