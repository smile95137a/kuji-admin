# Research Findings: KUJI Client App Frontend

> **文件版本**: 1.0.0
> **最後更新**: 2026-02-12
> **範圍**: 前台使用者介面（Client App）前端技術決策與架構研究

---

## 1. 技術棧決策

### 1.1 核心框架

**Decision**: Vue 3 + Vite + Pinia + TypeScript + Vue Router 4

與 Admin Panel 採用相同技術棧，維持專案一致性。前台在此基礎上的差異：

| 面向 | Admin Panel | Client App |
|------|-------------|------------|
| 主要目標裝置 | 桌機（1280px+） | 手機（375px ~ 768px）|
| RWD 策略 | 桌機優先 | **Mobile-first** |
| 路由保護 | 幾乎全部需認證 | 部分公開（商品瀏覽、消息） |
| 動畫需求 | 低（管理介面） | **高**（抽獎動畫、刮刮樂效果） |
| 載入策略 | 一般 SPA | 需考慮路由懶載入，控制初始 bundle 大小 |

### 1.2 UI 元件庫

**Decision**: 待確認（需檢查現有 cli 專案 `package.json`）

**候選方案（Mobile-first 考量）**:
| 元件庫 | Mobile 友善度 | 動畫支援 | 備註 |
|--------|--------------|---------|------|
| Vant 4 | ⭐⭐⭐ 手機原生風格 | ⭐⭐ | 中文文件完善，適合電商/手機 App |
| Quasar | ⭐⭐⭐ Material Design | ⭐⭐⭐ | 功能最完整 |
| Naive UI | ⭐⭐ | ⭐⭐ | TypeScript 佳，但非手機優先設計 |
| 自訂 | ⭐⭐⭐ | ⭐⭐⭐ | 最大彈性，成本最高 |

**Action needed**: `cat package.json | grep -E "vant|quasar|naive"` 確認現有依賴。

### 1.3 HTTP 客戶端

**Decision**: 同 Admin，Axios 單一實例，但 `baseURL` 不同

```typescript
// services/http.ts（前台版本）
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',  // 前台 API 無 /admin 前綴
  timeout: 15000,
});
```

**前台 vs 後台 Base URL 對比**:
- 後台: `http://host/api/admin/...`
- 前台: `http://host/api/...`（部分 public API 無 auth）

### 1.4 動畫策略

**Decision**: Vue Transition + CSS Animations + 視需要引入 GSAP

- 抽獎結果揭示：CSS keyframe animation（flip/reveal card）
- 刮刮樂效果：Canvas API（`<canvas>` 元素實作刮塗層）
- 頁面切換：Vue `<Transition name="slide-fade">`
- 數字跳動（金幣變化）：`requestAnimationFrame` 計數動畫

---

## 2. 認證架構決策

### 2.1 Public vs Private 路由分類

| 路由 | 認證需求 | 原因 |
|------|---------|------|
| `/` 首頁 | 公開 | 商品瀏覽吸引未登入用戶 |
| `/lottery/:id` 商品詳情 | 公開 | SEO + 分享連結需可瀏覽 |
| `/stores` 店家列表 | 公開 | 探索功能 |
| `/news` 消息列表 | 公開 | 公告資訊 |
| `/lottery/:id/draw` 抽獎頁 | **私有** | 需扣款 |
| `/prize-box` 賞品盒 | **私有** | 個人資料 |
| `/orders` 訂單 | **私有** | 個人資料 |
| `/wallet` 錢包 | **私有** | 金融資料 |
| `/profile` 個人資料 | **私有** | 帳號設定 |
| `/register` 註冊 | 公開（訪客限定） | 已登入則 redirect |
| `/login` 登入 | 公開（訪客限定） | 已登入則 redirect |

### 2.2 Token 儲存策略

與 Admin 相同的 JWT localStorage 策略：
- `localStorage.getItem('user_token')`（前台用 `user_` 前綴避免與後台衝突）
- `localStorage.getItem('user_refresh_token')`

### 2.3 未登入嘗試私有頁面的處理

```typescript
// 儲存使用者原本要去的路由，登入後重新導向
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  if (to.meta.requiresAuth && !authStore.token) {
    next({ path: '/login', query: { redirect: to.fullPath } });
    return;
  }
  next();
});

// LoginView.vue
const redirect = route.query.redirect as string | undefined;
router.push(redirect || '/');
```

---

## 3. 錢包架構決策（重要）

### 3.1 資料來源決策

**Decision**: 從 `GET /api/user/me` 取 goldCoins/bonusCoins，**不單獨依賴** `/api/wallet`

**背景**: 架構變更（2026-02-08），goldCoins/bonusCoins 已從 `user_wallet` 表遷移到 `user` 表。`/api/wallet` 端點仍可用，但欄位名稱已由 `goldBalance/bonusBalance` 改為 `goldCoins/bonusCoins`，建議統一使用 `/api/user/me`。

**正確用法**:
```typescript
// ✅ 正確：從 auth store 取（已由 /api/user/me 填入）
const authStore = useAuthStore();
const gold = computed(() => authStore.user?.goldCoins ?? 0);
const bonus = computed(() => authStore.user?.bonusCoins ?? 0);

// ✅ 抽獎後更新（不需重新 fetch，從 draw response 即時更新）
authStore.updateBalance({
  goldCoins: result[0].remainingGold,
  bonusCoins: result[0].remainingBonus,
});

// ❌ 不推薦（欄位名已改，且多一次 network request）
const wallet = await walletService.get(); // goldBalance/bonusBalance 已廢棄欄位名
```

### 3.2 餘額顯示元件

```typescript
// useWalletBalance composable（從 auth store 取，零 network cost）
export function useWalletBalance() {
  const authStore = useAuthStore();

  const goldCoins = computed(() => authStore.user?.goldCoins ?? 0);
  const bonusCoins = computed(() => authStore.user?.bonusCoins ?? 0);
  const totalCoins = computed(() => goldCoins.value + bonusCoins.value);

  // 顯示格式：1,234
  const goldDisplay = computed(() => goldCoins.value.toLocaleString());
  const bonusDisplay = computed(() => bonusCoins.value.toLocaleString());

  return { goldCoins, bonusCoins, totalCoins, goldDisplay, bonusDisplay };
}
```

### 3.3 儲值流程

```
使用者點擊儲值
    ↓
顯示儲值方案列表（GET /api/recharge/plans）
    ↓
選擇方案 → 送出（POST /api/recharge/create）
    ↓
跳轉第三方金流或顯示 QR code
    ↓
支付完成 → Webhook 通知後端 → 後端更新 goldCoins
    ↓
前端 polling 或 WebSocket 通知 → 更新 auth store 中的 goldCoins
```

---

## 4. 抽獎 API 關鍵決策

### 4.1 抽獎回應格式

**Decision**: 統一使用陣列格式解析，即使單抽也回傳陣列

```typescript
// services/draw.service.ts
const response = await http.post(`/api/lottery/draw/${lotteryId}/draw`, {
  count: drawCount,
  ticketIds: selectedTicketIds, // 刮刮樂模式時指定
});

// ✅ 永遠以陣列處理
const results: DrawResult[] = response.data.data;
const firstResult = results[0]; // 單抽時取第一個
```

### 4.2 抽獎完整流程

```
① GET /api/lottery/draw/{id}/tickets
   → 取得票券列表 + DrawSession（isOpener, protectionEndTime, grandPrizesDesignated）

② 檢查 session.protectionEndTime
   → 若存在且 Date.now() < new Date(protectionEndTime).getTime()
   → 且 !session.isOpener
   → 顯示「保護時間」提示，禁用抽獎按鈕

③ 刮刮樂模式特殊流程（playMode === 'SCRATCH_MODE'）
   → 若 isOpener && !grandPrizesDesignated
   → 先顯示「大獎指定」介面
   → POST /api/lottery/draw/{id}/designate-grand-prizes
   → 確認後才可抽

④ POST /api/lottery/draw/{id}/draw
   Request: { count: 1, ticketIds?: string[] }
   Response: { data: DrawResult[] }

⑤ 解析 results 陣列 → 觸發抽獎動畫
   → 更新 authStore.updateBalance({ goldCoins, bonusCoins })

⑥ 抽獎後刷新票券列表（或從 response 的 remainingDraws 更新 UI）
```

### 4.3 票券安全原則

```typescript
// ✅ AVAILABLE 票券只有這些欄位（不要預測獎品）
interface AvailableTicket {
  id: string;
  ticketNumber: number;
  status: 'AVAILABLE';
  // prizeId: 不存在
  // prizeLevel: 不存在
}

// ✅ DRAWN 票券才有獎品資訊
interface DrawnTicket {
  id: string;
  ticketNumber: number;
  status: 'DRAWN';
  prizeId: string;
  prizeLevel: PrizeLevel;
  prizeName: string;
  prizeImageUrl: string;
  isInPrizeBox: boolean;
}
```

### 4.4 保護時間 UX 決策（AV-003 部分解決）

```typescript
// composables/useProtectionTimer.ts
export function useProtectionTimer(endTime: Ref<string | null>) {
  const remainingSeconds = ref(0);
  const isProtected = computed(() => remainingSeconds.value > 0);

  let timer: ReturnType<typeof setInterval> | null = null;

  function start() {
    if (!endTime.value) return;
    function tick() {
      const ms = new Date(endTime.value!).getTime() - Date.now();
      remainingSeconds.value = Math.max(0, Math.floor(ms / 1000));
      if (remainingSeconds.value === 0 && timer) {
        clearInterval(timer);
        timer = null;
      }
    }
    tick();
    if (remainingSeconds.value > 0) {
      timer = setInterval(tick, 1000);
    }
  }

  watch(endTime, (val) => {
    if (timer) clearInterval(timer);
    if (val) start();
    else remainingSeconds.value = 0;
  }, { immediate: true });

  onUnmounted(() => { if (timer) clearInterval(timer); });

  // 顯示格式：mm:ss
  const displayTime = computed(() => {
    const m = Math.floor(remainingSeconds.value / 60);
    const s = remainingSeconds.value % 60;
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  });

  return { remainingSeconds, isProtected, displayTime };
}
```

---

## 5. 賞品盒多選出貨邏輯

### 5.1 跨店家選取警告

**Decision**: 前端計算 storeGroups.length > 1 時顯示拆單警告

```
賞品盒頁面顯示所有 IN_BOX 狀態的賞品（按店家分組）
    ↓
使用者勾選多個賞品（可跨店家）
    ↓
計算 selectedItems 中有幾個不同 storeId
    ↓
storeGroups.length > 1 → 顯示警告 Modal：
  「您選取了來自 {N} 家不同店家的賞品，
   系統將自動拆成 {N} 筆訂單分別出貨，
   每筆訂單各需填寫出貨資訊。」
    ↓
確認 → 後端自動拆單
       前端收到 [ Order[], ... ] 陣列（AV-010 待確認格式）
```

### 5.2 出貨流程

```typescript
// 後端拆單時，前端只需傳所有選取的 prizeBoxIds + 出貨資訊
const request = {
  prizeBoxIds: selectedIds,    // 所有選取項目，後端自動按 storeId 拆單
  shippingMethod: 'HOME_DELIVERY',
  recipientName: '...',
  recipientPhone: '...',
  address: '...',
};
await http.post('/api/prize-box/ship', request);
```

---

## 6. 推薦碼與註冊流程

### 6.1 URL 推薦碼處理（AV-012）

**Decision（建議）**: URL `?ref=` 參數自動填入 register 表單

```typescript
// RegisterView.vue
const route = useRoute();
const referralCode = ref(route.query.ref as string || '');

// 表單提交時帶入
await authService.register({
  email,
  password,
  nickname,
  referralCode: referralCode.value || undefined,
});
```

**使用者體驗**:
- 若有 `?ref=` 參數，推薦碼欄位預填且可見（讓使用者知道被推薦）
- 若無，推薦碼欄位顯示為選填空白欄位

---

## 7. 路由架構

### 7.1 前台路由結構

```
/                        → HomeView（public，商品首頁）
/login                   → LoginView（訪客限定）
/register                → RegisterView（訪客限定，支援 ?ref= 參數）
/forgot-password         → ForgotPasswordView
/stores                  → StoreListView（public）
/stores/:id              → StoreDetailView（public）
/lottery/:id             → LotteryDetailView（public，商品詳情）
/lottery/:id/draw        → DrawView（需認證，抽獎頁）
/news                    → NewsListView（public）
/news/:id                → NewsDetailView（public）

/me                      → ProfileLayout（需認證）
  /me/profile            → ProfileView（個人資料）
  /me/wallet             → WalletView（錢包/儲值）
  /me/prize-box          → PrizeBoxView（賞品盒）
  /me/orders             → OrderListView（訂單列表）
  /me/orders/:id         → OrderDetailView
  /me/addresses          → AddressListView（收件地址管理）
  /me/referral           → ReferralView（推薦碼）
```

### 7.2 懶載入策略

```typescript
// 抽獎頁較重（含動畫），強制懶載入
const DrawView = () => import('@/views/DrawView.vue');

// 首頁、商品列表預載（常用頁）
const HomeView = () => import('@/views/HomeView.vue');
```

---

## 8. 性能優化考量

### 8.1 商品列表優化

| 優化項目 | 實作方式 |
|---------|---------|
| 圖片懶載入 | `<img loading="lazy">` 或 `v-lazy` 指令 |
| 虛擬滾動 | 商品超過 50 筆時考慮 vue-virtual-scroller |
| 搜尋防抖 | `useDebounce(keyword, 300)` |
| 公開 API 快取 | 商品列表 5 分鐘內不重複 fetch（Pinia 快取 + timestamp） |

### 8.2 抽獎頁動畫性能

- 使用 CSS `will-change: transform` 提示 GPU 加速
- 動畫期間禁用互動，防止重複點擊
- Canvas 操作（刮刮樂）在 `requestAnimationFrame` 中執行

---

## 9. 國際化考量

**Decision**: 目前以繁體中文為主，預留 i18n 擴充空間

```typescript
// 暫不引入 vue-i18n，但避免硬編碼中文
// ✅ 使用常數定義文字（便於未來抽取）
export const STATUS_LABEL = {
  IN_BOX: '在賞品盒中',
  SHIPPED: '已出貨',
  RECYCLED: '已回收',
};

// 日期格式：使用 Intl.DateTimeFormat（內建，零依賴）
const formatDate = (date: string) =>
  new Intl.DateTimeFormat('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  }).format(new Date(date));
```

---

## 10. API 缺口記錄（Actions Needed）

| 缺口 ID | 描述 | 影響功能 | 狀態 | 優先級 |
|---------|------|---------|------|--------|
| AV-001 (cli) | 圖片上傳端點（前台用戶上傳頭像） | 個人資料 | ⚠️ 需確認 | P1 |
| AV-002 (cli) | 儲值完成後前端收到通知的機制（Polling? WebSocket? Webhook callback?） | 錢包 | ⚠️ 需確認 | P1 |
| AV-003 (cli) | 保護時間 UX：倒數秒數顯示格式（已有 composable 規劃，但後端 protectionEndTime 欄位名需確認） | 抽獎頁 | ⚠️ 部分解決 | P1 |
| AV-004 (cli) | 刮刮樂大獎指定 API 端點與請求格式 | 刮刮樂 | ⚠️ 需確認 | P1 |
| AV-005 (cli) | 刮刮樂免費抽返還機制的 API 回應欄位（remainingFreeDraws？） | 刮刮樂 | ⚠️ 需後端確認 | P2 |
| AV-006 (cli) | 出貨後多訂單回應格式（拆單時 response 是 Order[] 陣列？單筆？） | 賞品盒 | ⚠️ 需後端確認 | P2 |
| AV-007 (cli) | 推薦碼 URL `?ref=` 是否由後端記錄，前端只需帶入 register 表單即可 | 註冊 | ⚠️ 需確認 | P2 |
| AV-008 (cli) | 賞品回收 API：批次？單筆？回應欄位名稱 | 賞品盒 | ⚠️ 需確認 | P2 |
| AV-009 (cli) | 地址管理 API：Taiwan 縣市/鄉鎮 dropdown 資料來源（後端靜態？前端內建？） | 地址管理 | ⚠️ 需確認 | P3 |
| AV-010 (cli) | 消息詳情 API 欄位：content 是 HTML 還是 Markdown？ | 消息頁 | ⚠️ 需確認 | P3 |

---

## 11. 社群分享功能

### 11.1 抽獎結果分享

**Decision**: 使用 Web Share API（手機原生分享），fallback 複製連結

```typescript
// composables/useShare.ts
export function useShare() {
  const canShare = computed(() => !!navigator.share);

  async function share(data: { title: string; text: string; url: string }) {
    if (canShare.value) {
      await navigator.share(data);
    } else {
      await navigator.clipboard.writeText(data.url);
      // 顯示 toast：已複製連結
    }
  }

  return { canShare, share };
}
```

### 11.2 推薦連結產生

```typescript
// 推薦連結格式：{baseUrl}/register?ref={referralCode}
const referralLink = computed(() =>
  `${window.location.origin}/register?ref=${authStore.user?.referralCode}`
);
```

---

## 12. 錯誤狀態頁

| 情境 | 顯示方式 |
|------|---------|
| 網路離線 | 全域 banner：「網路連線中斷，請確認後重新整理」 |
| API 500 | Toast 錯誤訊息 |
| 商品不存在 (404) | 商品詳情頁顯示「商品不存在或已下架」卡片 |
| 已完售 (COMPLETED) | 抽獎按鈕顯示「已完售」，禁用 |
| 餘額不足 | 顯示 Modal：「金幣不足，是否前往儲值？」 |
| 保護時間 | 顯示倒數計時器，禁用抽獎按鈕，說明原因 |

---

*本文件為前台前端實作研究記錄，應隨開發進行持續更新。*
