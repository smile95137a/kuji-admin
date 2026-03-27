# Implementation Plan：KUJI 前台用戶介面（Client App）

**分支**：`cli-frontend` | **日期**：2026-03-27 | **Spec**：`specs/cli/`
**輸入**：`specs/cli/*/spec.md` — 12 個前台功能前端規格書

---

## Summary

KUJI 前台為玩家使用的 **Vue 3 SPA**，提供商品瀏覽、抽獎遊戲、賞品盒管理、錢包儲值與訂單查詢等核心功能。部分頁面無需登入（商品瀏覽、店家列表、消息），需登入功能（抽獎、賞品盒、錢包）透過 JWT 管理。

---

## Technical Context

| 項目 | 值 |
|------|----|
| **Language/Version** | TypeScript 5.x + Vue 3.x (Composition API `<script setup>`) |
| **Build Tool** | Vite 5.x |
| **State Management** | Pinia |
| **Router** | Vue Router 4 |
| **HTTP Client** | Axios（含 interceptor：token attach、401 refresh） |
| **UI 元件庫** | **NEEDS CLARIFICATION**：需檢查現有 cli 專案（確認 package.json 後填入）|
| **Storage** | localStorage（JWT token）、Pinia（runtime state）|
| **Testing** | NEEDS CLARIFICATION：需確認現有專案測試框架 |
| **Target Platform** | 手機 Web（主要）、桌機（次要）— RWD |
| **Project Type** | Web Application SPA（Mobile-first RWD）|
| **Performance Goals** | 首屏 LCP < 2.5s；抽獎 API 回應 < 1s；動畫流暢 60fps |
| **Constraints** | 抽獎結果固定為陣列格式（即使單抽）；AVAILABLE 票券不可洩漏 prizeId |
| **Scale/Scope** | 12 個功能模組、~20 個頁面路由 |
| **API Base URL** | `/api/` |

---

## Constitution Check

| 項目 | 狀態 | 說明 |
|------|------|------|
| 公開頁面無需 token | ✅ | 商品瀏覽、店家、消息、Banner 不需 Authorization |
| 錢包欄位架構（新版） | ✅ | `goldCoins`/`bonusCoins` 存在 user 表；從 `GET /api/user/me` 取，非 `/api/wallet` |
| 抽獎 API 回應格式 | ✅ | 固定陣列 `data: [...]`，即使單抽也是 `[0]` |
| 票券安全要求 | ✅ | AVAILABLE 票券不可顯示 prizeId/prizeLevel/revealedNumber |
| 刮刮樂謝謝惠顧 | ✅ | `prizeId = null` → 顯示「謝謝惠顧」（與一番賞不同） |
| 保護時間鎖定 | ⚠️ | 需確認倒數計時的 UX 設計（顯示剩餘秒數？禁用按鈕？） |
| UI 元件庫版本 | ⚠️ | 需從現有 cli 專案 package.json 確認 |

---

## Project Structure

### Documentation（本功能）

```text
specs/cli/
├── plan.md              ← 本檔案
├── research.md          ← 技術決策記錄
├── data-model.md        ← Pinia stores + composables 規格
├── quickstart.md        ← 移植快速上手指南
├── contracts/           ← API 整合契約
│   ├── auth.md
│   ├── lottery-browse.md
│   ├── draw.md
│   ├── prize-box.md
│   ├── wallet.md
│   └── orders.md
├── 001-banner-homepage/spec.md
├── 002-express-shipping/spec.md
├── 003-game-management/spec.md
├── 004-game-to-order/spec.md
├── 005-lottery-ticket-system/spec.md
├── 006-payment-points/spec.md
├── 007-news-management/spec.md
├── 008-order-management/spec.md
├── 010-prize-box/spec.md
├── 011-product-lottery/spec.md
├── 012-referral-code/spec.md
└── 014-store-management/spec.md
```

### Source Code（目標 cli 專案）

```text
src/
├── main.ts
├── App.vue
├── router/
│   ├── index.ts         ← 路由（meta: { requiresAuth }）
│   └── guards.ts        ← beforeEach：未登入者導向 /login
├── stores/
│   ├── auth.ts          ← token、user（含 goldCoins、bonusCoins）、登入狀態
│   ├── cart.ts          ← 賞品盒選取狀態（多選 checkboxes）
│   ├── draw.ts          ← 抽獎 session（tickets、isOpener、protectionEndTime）
│   └── notification.ts  ← 全域 Toast
├── services/
│   ├── http.ts          ← Axios 實例
│   ├── auth.service.ts
│   ├── lottery.service.ts   ← browse + draw
│   ├── prizeBox.service.ts
│   ├── order.service.ts
│   ├── wallet.service.ts
│   ├── address.service.ts
│   ├── store.service.ts
│   ├── news.service.ts
│   └── referral.service.ts
├── composables/
│   ├── useAuth.ts
│   ├── useWalletBalance.ts  ← 全域餘額狀態（goldCoins、bonusCoins）
│   ├── useDraw.ts           ← 抽獎流程封裝（ticket fetch → draw → result）
│   ├── useProtectionTimer.ts ← 保護時間倒數計時
│   ├── usePrizeBox.ts       ← 賞品盒多選 + 分組邏輯
│   └── useInfiniteScroll.ts ← 商品列表無限捲動
├── layouts/
│   ├── DefaultLayout.vue    ← 導覽列 + 底部 nav + footer
│   └── GameLayout.vue       ← 沉浸式抽獎頁面（無頂欄）
├── views/
│   ├── HomeView.vue         ← Banner 輪播 + 熱門商品
│   ├── auth/
│   │   ├── LoginView.vue
│   │   └── RegisterView.vue ← 含推薦碼欄位
│   ├── stores/
│   │   ├── StoreListView.vue
│   │   └── StoreDetailView.vue
│   ├── lottery/
│   │   ├── LotteryListView.vue    ← 商品列表（無限捲動）
│   │   ├── LotteryDetailView.vue  ← 商品詳情 + CTA
│   │   └── LotteryDrawView.vue    ← 抽獎頁（票券格 + 動畫）
│   ├── scratch/
│   │   ├── ScratchOpenView.vue   ← 開套者指定大獎
│   │   └── ScratchDrawView.vue   ← 刮刮樂抽獎介面
│   ├── prize-box/
│   │   └── PrizeBoxView.vue      ← 賞品盒（分組 + 出貨 + 回收）
│   ├── orders/
│   │   ├── OrderListView.vue
│   │   └── OrderDetailView.vue
│   ├── wallet/
│   │   ├── WalletView.vue
│   │   ├── TopupView.vue
│   │   └── TransactionView.vue
│   ├── news/
│   │   ├── NewsListView.vue
│   │   └── NewsDetailView.vue
│   └── profile/
│       └── ProfileView.vue
└── components/
    ├── common/
    │   ├── AppNavbar.vue        ← 頂部（含金幣餘額顯示）
    │   ├── BottomNav.vue        ← 手機底部導覽
    │   ├── WalletBadge.vue      ← 全域餘額小標籤
    │   └── LoadingSpinner.vue
    ├── home/
    │   └── BannerCarousel.vue   ← 自動輪播
    ├── lottery/
    │   ├── ProductCard.vue
    │   ├── PrizeGrid.vue        ← 獎品展示
    │   ├── TicketGrid.vue       ← 票券格子
    │   ├── DrawResultModal.vue  ← 抽獎結果揭示
    │   └── ProtectionTimer.vue  ← 保護時間倒數
    ├── prize-box/
    │   ├── PrizeBoxGroup.vue    ← 按店家分組
    │   ├── PrizeCard.vue
    │   ├── ShipModal.vue        ← 出貨表單（宅配/超商）
    │   └── RecycleModal.vue     ← 回收確認
    └── wallet/
        ├── RechargeCard.vue     ← 儲值方案卡片
        └── TransactionItem.vue
```

---

## Implementation Phases

### Phase 1（核心基礎）— 最高優先

**目標**：可瀏覽商品、可登入、全域餘額顯示

| 任務 | 對應 Spec | API |
|------|-----------|-----|
| Axios 實例 + interceptors | 全模組 | POST /api/auth/refresh |
| Pinia auth store（含 goldCoins / bonusCoins） | 006-payment-points | GET /api/user/me |
| 登入頁（LoginView.vue） | — | POST /api/auth/login |
| 註冊頁（RegisterView.vue，含推薦碼欄位） | 012-referral-code | POST /api/auth/register |
| 推薦碼即時驗證（debounce 400ms） | 012-referral-code | POST /api/referral/validate |
| DefaultLayout.vue（頂部 navbar + 金幣/紅利餘額） | 006-payment-points | — |
| Banner 輪播（HomeView.vue） | 001-banner-homepage | GET /api/banners |
| Router guards（requiresAuth） | — | — |

---

### Phase 2（商品瀏覽與店家）

**目標**：玩家可發現並瀏覽商品與店家

| 任務 | 對應 Spec | API |
|------|-----------|-----|
| LotteryListView.vue（無限捲動、分類篩選） | 011-product-lottery | POST /api/lottery/browse/list |
| LotteryDetailView.vue（商品詳情、獎品列表、開賣倒數） | 011-product-lottery | GET /api/lottery/browse/{id} |
| 熱度追蹤（進入詳情頁觸發） | 011-product-lottery | POST /api/lottery/browse/{id}/hot |
| StoreListView.vue（店家卡片列表） | 014-store-management | GET /api/stores |
| StoreDetailView.vue（商店資訊 + 商品列表） | 014-store-management | GET /api/stores/{id} |

---

### Phase 3（核心抽獎機制）— 最複雜

**目標**：玩家可執行一番賞/扭蛋/卡牌抽獎

| 任務 | 對應 Spec | API |
|------|-----------|-----|
| Pinia draw store（tickets、session、isOpener） | 003-game-management | GET /api/lottery/draw/{id}/tickets |
| `useDraw` composable（整合 fetch→draw→result） | 003-game-management | POST /api/lottery/draw/{id}/draw |
| LotteryDrawView.vue（票券格 + 單抽/多抽） | 003-game-management | — |
| `useProtectionTimer` composable（保護時間倒數） | 003-game-management | — |
| DrawResultModal.vue（動畫揭示、繼續/賞品盒選項） | 004-game-to-order | — |
| 錢包餘額不足檢查（跳轉儲值） | 006-payment-points | — |

**⚠️ 安全需求**：AVAILABLE 票券不得在前端渲染 prizeId/prizeLevel。

---

### Phase 4（刮刮樂模式）

**目標**：支援 SCRATCH_STORE 和 SCRATCH_PLAYER 兩種刮刮樂模式

| 任務 | 對應 Spec | API |
|------|-----------|-----|
| ScratchOpenView.vue（開套者指定大獎位置） | 005-lottery-ticket-system | POST /api/lottery/draw/{id}/designate |
| ScratchDrawView.vue（刮刮樂抽獎介面） | 005-lottery-ticket-system | POST /api/lottery/draw/{id}/draw |
| 謝謝惠顧處理（prizeId = null 顯示） | 005-lottery-ticket-system | — |
| 免費抽獎回退機制（opener 中大獎保護） | 005-lottery-ticket-system | — |

---

### Phase 5（賞品盒與出貨）

**目標**：玩家可管理賞品、申請出貨或回收

| 任務 | 對應 Spec | API |
|------|-----------|-----|
| PrizeBoxView.vue（依店家分組、多選） | 010-prize-box | GET /api/prize-box |
| `usePrizeBox` composable（選取邏輯、跨店拆單計算） | 010-prize-box | — |
| ShipModal.vue（宅配/超商表單切換） | 002-express-shipping | POST /api/prize-box/ship |
| 地址預填（saved address 選單） | 002-express-shipping | GET /api/address |
| RecycleModal.vue（回收確認 + Bonus 顯示） | 010-prize-box | POST /api/prize-box/recycle |
| 回收後立即更新 goldCoins/bonusCoins | 006-payment-points | — |

---

### Phase 6（錢包與訂單）

| 任務 | 對應 Spec | API |
|------|-----------|-----|
| WalletView.vue（餘額 + 儲值 + 交易記錄 Tab） | 006-payment-points | GET /api/wallet |
| TopupView.vue（方案選擇 + 付款） | 006-payment-points | GET /api/recharge/plans |
| TransactionView.vue（交易/消費紀錄） | 006-payment-points | GET /api/wallet/transactions |
| OrderListView.vue（狀態篩選） | 008-order-management | GET /api/orders |
| OrderDetailView.vue（狀態時間軸） | 008-order-management | GET /api/orders/{id} |

---

### Phase 7（消息與其他）

| 任務 | 對應 Spec | API |
|------|-----------|-----|
| NewsListView.vue（分類 Tab + 重要標記） | 007-news-management | POST /api/news/list |
| NewsDetailView.vue（富文本渲染 + XSS 防護） | 007-news-management | GET /api/news/{id} |
| ProfileView.vue（用戶資訊編輯、頭像上傳） | 006-payment-points | GET/PUT /api/user/me |

---

## Complexity Tracking

| 潛在複雜度 | 說明 | 處理方式 |
|-----------|------|---------|
| 抽獎動畫 | 揭示動畫需 ≤ 2s，且不可被連點中斷 | `useDraw` 中 loading flag 禁用按鈕；動畫結束後才啟用 |
| 保護時間鎖定 | 需即時倒數顯示 | `useProtectionTimer(endTime)` → reactive countdown |
| 多店家拆單 | 選取跨店家賞品需提示拆單 | `usePrizeBox` 計算 storeGroups，>1 時顯示警告 |
| 刮刮樂安全 | AVAILABLE 票券不能洩漏獎品資訊 | API 後端控制；前端不嘗試從 response 推算獎品 |
| 錢包架構變更 | goldCoins/bonusCoins 在 user 表，非 wallet 表 | 從 `/api/user/me` 取，auth store 統一管理；`/api/wallet` 僅作備用 |
| 全域餘額即時更新 | 每次抽獎後更新 navbar 餘額 | draw 結果含 `remainingGold/remainingBonus` → auth store.updateBalance() |
| RWD 手機優先 | 票券格子、賞品盒需手機友好 | CSS Grid + overflow-x scroll on desktop |
