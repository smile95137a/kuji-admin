# Implementation Plan：KUJI 後台管理介面（Admin Panel）

**分支**：`admin-frontend` | **日期**：2026-03-27 | **Spec**：`specs/admin/`
**輸入**：`specs/admin/*/spec.md` — 10 個後台功能前端規格書

---

## Summary

KUJI 後台管理介面為一個 **Vue 3 SPA**，供平台管理員（ROLE_ADMIN）與店家人員（ROLE_STORE_OWNER、ROLE_STORE_EDITOR）管理商品、訂單、帳號、權限、廣告及內容。後台透過 JWT + RBAC 機制控制選單可見性與 API 存取權限，所有資料存取均需 `Authorization: Bearer {token}` 標頭。

---

## Technical Context

| 項目 | 值 |
|------|----|
| **Language/Version** | TypeScript 5.x + Vue 3.x (Composition API `<script setup>`) |
| **Build Tool** | Vite 5.x |
| **State Management** | Pinia |
| **Router** | Vue Router 4 |
| **HTTP Client** | Axios（含 interceptor 自動附加 token 與 401 刷新） |
| **UI 元件庫** | **NEEDS CLARIFICATION**：需檢查現有 admin 專案（Element Plus / Naive UI / Vuetify）|
| **Storage** | localStorage（JWT token）、Pinia（runtime state）|
| **Testing** | NEEDS CLARIFICATION：需確認現有專案的測試框架（Vitest / Jest）|
| **Target Platform** | 桌機 Web Browser（主要）、平板（次要）|
| **Project Type** | Web Application SPA |
| **Performance Goals** | 頁面切換 < 300ms；列表載入 < 2s（50 筆資料）|
| **Constraints** | JWT 24h 有效、自動 Refresh Token；停用帳號立即失效需處理 401 |
| **Scale/Scope** | 10 個功能模組、~30 個頁面路由 |
| **API Base URL** | `/api/admin/` |

---

## Constitution Check

> 移植至現有專案前，確認以下項目：

| 項目 | 狀態 | 說明 |
|------|------|------|
| 認證機制一致 | ✅ | JWT Bearer Token，與後端 spec 對齊 |
| 角色/選單 RBAC | ✅ | `GET /api/admin/user/menu` 動態取得可見選單 |
| 跨店家資料隔離 | ✅ | StoreOwner/Editor API 後端自動過濾，前端勿假設能看全部資料 |
| 停用帳號即時失效 | ⚠️ | 需確認 axios interceptor 處理 401 時清除 token 並導向登入 |
| UI 元件庫版本 | ⚠️ | 需從現有專案 package.json 確認 |

---

## Project Structure

### Documentation（本功能）

```text
specs/admin/
├── plan.md              ← 本檔案
├── research.md          ← 技術決策記錄
├── data-model.md        ← Pinia stores + composables 規格
├── quickstart.md        ← 移植快速上手指南
├── contracts/           ← API 整合契約
│   ├── auth.md
│   ├── products.md
│   ├── orders.md
│   ├── stores.md
│   └── system.md
├── 001-banner-management/spec.md
├── 002-express-shipping/spec.md
├── 006-payment-points/spec.md
├── 007-news-management/spec.md
├── 008-order-management/spec.md
├── 009-rbac-permissions/spec.md
├── 011-product-lottery/spec.md
├── 012-referral-code/spec.md
├── 013-store-account-mgmt/spec.md
└── 014-store-management/spec.md
```

### Source Code（目標 admin 專案）

```text
src/
├── main.ts              ← App entry，mount Pinia + Router + UI lib
├── App.vue
├── router/
│   ├── index.ts         ← 路由定義（含 meta: { requiresAuth, roles }）
│   └── guards.ts        ← beforeEach：token 檢查 + RBAC redirect
├── stores/
│   ├── auth.ts          ← 登入狀態、token、當前用戶資訊、角色
│   ├── menu.ts          ← 動態側欄選單（來自 /api/admin/user/menu）
│   ├── store.ts         ← 店家選項快取（storeId dropdown）
│   └── notification.ts  ← 全域 Toast/Alert 狀態
├── services/
│   ├── http.ts          ← Axios 實例（interceptors: token attach, 401 refresh, error toast）
│   ├── auth.service.ts
│   ├── lottery.service.ts
│   ├── prize.service.ts
│   ├── order.service.ts
│   ├── store.service.ts
│   ├── user.service.ts
│   ├── banner.service.ts
│   ├── news.service.ts
│   ├── referral.service.ts
│   └── wallet.service.ts
├── composables/
│   ├── useAuth.ts       ← 封裝 auth store 存取
│   ├── usePagination.ts ← 前端分頁邏輯（後端回傳全量資料）
│   ├── useQueryList.ts  ← 封裝 POST .../list 查詢模式
│   └── useFileUpload.ts ← S3 圖片上傳 composable
├── layouts/
│   ├── AdminLayout.vue  ← 側欄 + 頂欄 + main 內容區
│   └── AuthLayout.vue   ← 登入頁全版面
├── views/
│   ├── auth/
│   │   └── LoginView.vue
│   ├── dashboard/
│   │   └── DashboardView.vue
│   ├── stores/
│   │   ├── StoreListView.vue
│   │   └── StoreDetailView.vue
│   ├── accounts/
│   │   ├── AccountListView.vue
│   │   └── AccountCreateView.vue
│   ├── lottery/
│   │   ├── LotteryListView.vue
│   │   ├── LotteryCreateView.vue
│   │   ├── LotteryEditView.vue
│   │   └── LotteryPrizesView.vue
│   ├── orders/
│   │   ├── OrderListView.vue
│   │   └── OrderDetailView.vue
│   ├── banners/
│   │   ├── BannerListView.vue
│   │   └── BannerCreateView.vue
│   ├── news/
│   │   ├── NewsListView.vue
│   │   └── NewsEditView.vue
│   ├── referral/
│   │   ├── ReferralListView.vue
│   │   └── ReferralStatsView.vue
│   ├── payment/
│   │   ├── RechargeplanListView.vue
│   │   └── WalletAdjustView.vue
│   └── permissions/
│       └── PermissionView.vue
└── components/
    ├── common/
    │   ├── AppSidebar.vue
    │   ├── AppHeader.vue
    │   ├── StatusBadge.vue
    │   ├── ConfirmModal.vue
    │   └── ImageUpload.vue
    ├── lottery/
    │   ├── LotteryForm.vue
    │   └── PrizeForm.vue
    ├── order/
    │   └── OrderStatusTimeline.vue
    └── store/
        └── StoreForm.vue
```

---

## Implementation Phases

### Phase 1（核心基礎）— 最高優先

**目標**：可登入、可看到動態側欄、能切換頁面

| 任務 | 對應 Spec | API |
|------|-----------|-----|
| Axios 實例 + interceptors（token attach、401 refresh、error toast） | 全模組 | POST /api/admin/auth/refresh |
| Pinia auth store（登入/登出/token 持久化） | 009-rbac | POST /api/admin/auth/login |
| 登入頁（LoginView.vue） | 009-rbac | POST /api/admin/auth/login |
| AdminLayout.vue（側欄 + 頂欄框架） | 009-rbac | GET /api/admin/user/menu |
| Pinia menu store（動態選單 + RBAC 側欄） | 009-rbac | GET /api/admin/user/menu |
| Router guards（requiresAuth + 角色檢查） | 009-rbac | — |
| Dashboard 空頁面（placeholder） | — | — |

**驗收**：Admin 可登入，側欄依角色顯示正確選單，StoreOwner 看不到 Admin 專屬選單。

---

### Phase 2（商品管理）— 核心業務

**目標**：店家人員可完整管理抽獎商品生命週期

| 任務 | 對應 Spec | API |
|------|-----------|-----|
| LotteryListView.vue（查詢 + 篩選 + 排序） | 011-product-lottery | POST /api/admin/lottery/list |
| LotteryCreateView.vue（建立商品表單） | 011-product-lottery | POST /api/admin/lottery |
| LotteryEditView.vue（編輯商品） | 011-product-lottery | PUT /api/admin/lottery/{id} |
| LotteryPrizesView.vue（獎品 CRUD） | 011-product-lottery | GET/POST/PUT/DELETE .../prizes |
| 商品上架/下架/複製 操作按鈕 | 011-product-lottery | PUT .../status, POST .../copy |
| 末獎設定 Modal | 011-product-lottery | POST /api/admin/lottery/{id}/final-prize |
| 圖片上傳 composable（S3） | 011-product-lottery | POST /api/upload（需確認端點）|

**⚠️ API 缺口（來自 spec AV 清單）**：
- AV-004：批次抽獎設定欄位（`batchDrawOptions`）需後端確認
- AV-006：DRAFT→CONFIGURED 觸發機制需確認
- AV-007：自動折扣設定欄位需確認

---

### Phase 3（訂單與出貨）

**目標**：店家人員可管理訂單狀態與出貨流程

| 任務 | 對應 Spec | API |
|------|-----------|-----|
| OrderListView.vue（依角色自動過濾） | 008-order-management | POST /api/admin/orders/list |
| OrderDetailView.vue（狀態時間軸、出貨資訊） | 008-order-management | GET /api/admin/orders/{id} |
| 訂單狀態更新（待出貨→準備中→已出貨→完成） | 002-express-shipping | PUT /api/admin/orders/{id}/status |
| 訂單取消（附原因） | 002-express-shipping | POST /api/admin/orders/{id}/cancel |

---

### Phase 4（店家與帳號管理）— Admin 專屬

**目標**：Admin 可管理店家、建立/停用帳號

| 任務 | 對應 Spec | API |
|------|-----------|-----|
| StoreListView.vue（啟用/停用串接） | 014-store-management | GET /api/admin/stores |
| StoreDetailView.vue（含編輯功能） | 014-store-management | PUT /api/admin/stores/{id} |
| 店家啟用/停用（含 cascade 警告） | 014-store-management | POST /api/admin/stores/{id}/enable/disable |
| AccountListView.vue | 013-store-account-mgmt | GET /api/admin/users |
| StoreOwner 建立表單（同步建立店家） | 013-store-account-mgmt | POST /api/admin/users/store-owner |
| StoreEditor 建立表單 | 013-store-account-mgmt | POST /api/admin/users/store-editor |
| 帳號啟用/停用 | 013-store-account-mgmt | POST /api/admin/users/{id}/activate/deactivate |

---

### Phase 5（內容管理）

**目標**：Admin 可管理廣告、消息、推薦碼

| 任務 | 對應 Spec | API |
|------|-----------|-----|
| BannerListView.vue（排序 + 狀態管理） | 001-banner-management | GET/POST/PUT/DELETE /api/admin/banners |
| NewsListView.vue + NewsEditView.vue | 007-news-management | POST /api/admin/news/list, POST /api/admin/news |
| ReferralListView.vue + 推薦碼建立 | 012-referral-code | GET/POST /api/admin/referral-codes |
| ReferralStatsView.vue（被推薦清單） | 012-referral-code | GET /api/admin/referral-codes/{id}/stats |

---

### Phase 6（點數管理 + 權限設定）

| 任務 | 對應 Spec | API |
|------|-----------|-----|
| 儲值方案 CRUD | 006-payment-points | GET/POST/PUT/DELETE /api/admin/recharge/plans |
| 手動點數調整（with reason） | 006-payment-points | POST /api/admin/wallet/adjust |
| 全局交易紀錄查詢 | 006-payment-points | POST /api/admin/wallet/transactions |
| PermissionView.vue（角色選單權限矩陣） | 009-rbac-permissions | GET/PUT /api/admin/roles/{id}/permissions |

---

## Complexity Tracking

| 潛在複雜度 | 說明 | 處理方式 |
|-----------|------|---------|
| RBAC 動態側欄 | 選單依角色即時渲染 | Pinia menu store + Router meta 雙層控制 |
| 商品狀態機 | 7 個狀態，部分自動轉換 | 前端只控制 ON_SHELF / OFF_SHELF；其他狀態唯讀顯示 |
| 停用帳號立即失效 | 401 後需立即清除 session | axios interceptor 統一處理 |
| 前端分頁 | 後端回傳全量資料 | `usePagination` composable 統一管理 |
| 圖片上傳 S3 | 多處需要 | `useFileUpload` composable 封裝 |
| Admin vs StoreOwner 視角 | 同一頁面，不同資料範圍 | 後端自動過濾；前端依 role 顯示/隱藏操作按鈕 |
