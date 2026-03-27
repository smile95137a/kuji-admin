# Quickstart: KUJI Client App 前端遷移指南

本指南說明如何將 `specs/cli/` 下的規格文件遷移到實際的 Client（前台）前端專案，並完成初始設定。

---

## 環境需求

| 工具 | 版本 |
|------|------|
| Node.js | >= 18 |
| Vue | 3.x |
| Vite | 5.x |
| TypeScript | 5.x |
| Pinia | 2.x |
| Vue Router | 4.x |

---

## 步驟 1：確認現有 cli 專案技術棧

```bash
# 在 cli 前端專案執行
cat package.json | grep -E "(vant|element|naive|quasar|ionic|tailwind)"
```

前台通常使用行動裝置優先（Mobile First）的 UI 框架。  
根據現有依賴更新 `specs/cli/research.md`。

---

## 步驟 2：設定 Axios 實例

> ⚠️ 注意：cli 的 `baseURL` 與 admin **不同**。  
> cli 使用 `/api/`（公開 + 私有），admin 使用 `/api/admin/`。

```typescript
// src/services/http.ts
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import router from '@/router';

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL, // e.g. https://api.kuji.example.com
  timeout: 15000,
});

// Request interceptor：只有在已登入時才附加 token
// 公開 endpoint（店家列表、抽獎詳情）不需 Authorization header
http.interceptors.request.use((config) => {
  const auth = useAuthStore();
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config;
});

// Response interceptor：401 → 清除狀態並導回登入頁
http.interceptors.response.use(
  (res) => res,
  async (error) => {
    if (error.response?.status === 401) {
      const auth = useAuthStore();
      auth.clearSession();
      // 僅在需要驗證的頁面才跳轉
      if (router.currentRoute.value.meta.requiresAuth) {
        router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } });
      }
    }
    return Promise.reject(error);
  }
);

export default http;
```

---

## 步驟 3：Auth Store 與錢包餘額管理

> ✅ **重要規則**：錢包餘額從 `/api/user/me` 讀取，**不要**呼叫獨立的 `/wallet` 端點。

```typescript
// src/stores/auth.ts
import { defineStore } from 'pinia';
import http from '@/services/http';
import type { ClientUser } from '@/types/user';

interface AuthState {
  token: string | null;
  refreshToken: string | null;
  user: ClientUser | null;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('cli_token'),
    refreshToken: localStorage.getItem('cli_refresh_token'),
    user: null,
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    // 金幣與紅利點數直接從 user 物件讀取
    goldCoins: (state) => state.user?.goldCoins ?? 0,
    bonusCoins: (state) => state.user?.bonusCoins ?? 0,
  },
  actions: {
    // ✅ 從 user/me 讀取，goldCoins/bonusCoins 已包含在內
    async fetchUser() {
      const res = await http.get('/api/user/me');
      this.user = res.data.data;
    },

    // 抽獎後直接更新餘額，無需重新 fetch（避免多餘請求）
    updateBalance({ goldCoins, bonusCoins }: { goldCoins: number; bonusCoins: number }) {
      if (this.user) {
        this.user.goldCoins = goldCoins;
        this.user.bonusCoins = bonusCoins;
      }
    },

    async login(email: string, password: string) {
      const res = await http.post('/api/auth/login', { email, password });
      const { token, refreshToken } = res.data.data;
      this.token = token;
      this.refreshToken = refreshToken;
      localStorage.setItem('cli_token', token);
      localStorage.setItem('cli_refresh_token', refreshToken);
      await this.fetchUser();
    },

    clearSession() {
      this.token = null;
      this.refreshToken = null;
      this.user = null;
      localStorage.removeItem('cli_token');
      localStorage.removeItem('cli_refresh_token');
    },

    async logout() {
      try {
        await http.post('/api/auth/logout');
      } catch { /* ignore */ }
      this.clearSession();
    },
  },
});
```

---

## 步驟 4：抽獎結果處理

> ⚠️ **重要**：`response.data.data` 永遠是陣列，**即使是單抽也是 `[result]`**。

```typescript
// src/composables/useDraw.ts
import { ref } from 'vue';
import http from '@/services/http';
import { useAuthStore } from '@/stores/auth';
import type { DrawResult } from '@/types/draw';

export function useDraw(lotteryId: string) {
  const isDrawing = ref(false);
  const results = ref<DrawResult[]>([]);
  const authStore = useAuthStore();

  async function executeDraw(count: number) {
    isDrawing.value = true;
    try {
      const response = await http.post(`/api/lottery/draw/${lotteryId}/draw`, { count });

      // ✅ 永遠是陣列，單抽也是 [result]
      const drawResults: DrawResult[] = response.data.data;
      results.value = drawResults;

      // 從第一筆結果更新錢包餘額
      if (drawResults.length > 0) {
        const first = drawResults[0];
        authStore.updateBalance({
          goldCoins: first.remainingGold,
          bonusCoins: first.remainingBonus,
        });
      }

      return drawResults;
    } finally {
      isDrawing.value = false;
    }
  }

  return { isDrawing, results, executeDraw };
}
```

---

## 步驟 5：賞品盒多店家分組

```typescript
// src/composables/usePrizeBoxSelection.ts
import { ref, computed } from 'vue';
import type { PrizeBoxItem } from '@/types/prizeBox';

export function usePrizeBoxSelection(items: Ref<PrizeBoxItem[]>) {
  const selectedIds = ref<Set<string>>(new Set());

  function toggleSelect(id: string) {
    if (selectedIds.value.has(id)) {
      selectedIds.value.delete(id);
    } else {
      selectedIds.value.add(id);
    }
  }

  // 按 storeId 分組選取的賞品
  const selectedByStore = computed(() => {
    const groups: Record<string, PrizeBoxItem[]> = {};
    for (const id of selectedIds.value) {
      const item = items.value.find((i) => i.id === id);
      if (item) {
        if (!groups[item.storeId]) groups[item.storeId] = [];
        groups[item.storeId].push(item);
      }
    }
    return groups;
  });

  // 是否跨店家（會影響運費計算或警告提示）
  const isMultiStore = computed(() => Object.keys(selectedByStore.value).length > 1);

  const selectedCount = computed(() => selectedIds.value.size);

  function clearSelection() {
    selectedIds.value.clear();
  }

  return {
    selectedIds,
    toggleSelect,
    selectedByStore,
    isMultiStore,
    selectedCount,
    clearSelection,
  };
}
```

跨店家警告元件範例：

```vue
<!-- src/components/prizeBox/MultiStoreWarning.vue -->
<template>
  <div v-if="isMultiStore" class="warning-banner">
    ⚠️ 您已選取 {{ storeCount }} 間不同店家的賞品，
    每間店家將分別出貨，運費分開計算。
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  selectedByStore: Record<string, unknown[]>;
}>();

const isMultiStore = computed(() => Object.keys(props.selectedByStore).length > 1);
const storeCount = computed(() => Object.keys(props.selectedByStore).length);
</script>
```

---

## 步驟 6：票券保護時間（Protection Timer）

刮刮樂開獎人（isOpener）在開啟後，其他玩家進入保護期，無法抽取特定獎項。

```typescript
// src/composables/useProtectionTimer.ts
import { ref, computed, watchEffect, onUnmounted } from 'vue';
import type { Ref } from 'vue';

export function useProtectionTimer(endTime: Ref<string | null>) {
  const remaining = ref(0); // 剩餘秒數
  let timer: ReturnType<typeof setInterval> | null = null;

  function clearTimer() {
    if (timer) {
      clearInterval(timer);
      timer = null;
    }
  }

  watchEffect(() => {
    clearTimer();

    if (endTime.value) {
      const end = new Date(endTime.value).getTime();

      const update = () => {
        const now = Date.now();
        remaining.value = Math.max(0, Math.floor((end - now) / 1000));
        if (remaining.value === 0) clearTimer();
      };

      update(); // 立即計算一次
      timer = setInterval(update, 1000);
    } else {
      remaining.value = 0;
    }
  });

  onUnmounted(clearTimer);

  const isLocked = computed(() => remaining.value > 0);

  // 格式化顯示：mm:ss
  const displayTime = computed(() => {
    const m = Math.floor(remaining.value / 60);
    const s = remaining.value % 60;
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  });

  return { remaining, isLocked, displayTime };
}
```

使用範例：

```vue
<!-- DrawPage.vue -->
<script setup lang="ts">
import { ref, computed } from 'vue';
import { useProtectionTimer } from '@/composables/useProtectionTimer';

const session = ref<DrawSession | null>(null);
const protectionEndTime = computed(() => session.value?.protectionEndTime ?? null);
const { isLocked, displayTime } = useProtectionTimer(protectionEndTime);
</script>

<template>
  <div>
    <div v-if="isLocked" class="protection-notice">
      🔒 保護期剩餘：{{ displayTime }}，部分獎項暫時無法抽取
    </div>
    <button :disabled="isLocked" @click="draw">立即抽獎</button>
  </div>
</template>
```

---

## 頁面路由結構

```typescript
// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const routes = [
  // 首頁：Banner + 店家列表
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomePage.vue'),
  },

  // 店家資訊頁
  {
    path: '/store/:id',
    name: 'StorePage',
    component: () => import('@/views/StorePage.vue'),
  },

  // 抽獎商品詳情（公開）
  {
    path: '/lottery/:id',
    name: 'LotteryDetail',
    component: () => import('@/views/LotteryDetailPage.vue'),
  },

  // 執行抽獎（需登入）
  {
    path: '/lottery/:id/draw',
    name: 'Draw',
    component: () => import('@/views/DrawPage.vue'),
    meta: { requiresAuth: true },
  },

  // 刮刮樂（需登入）
  {
    path: '/scratch/:id',
    name: 'Scratch',
    component: () => import('@/views/ScratchPage.vue'),
    meta: { requiresAuth: true },
  },

  // 賞品盒（需登入）
  {
    path: '/prize-box',
    name: 'PrizeBox',
    component: () => import('@/views/PrizeBoxPage.vue'),
    meta: { requiresAuth: true },
  },

  // 訂單列表（需登入）
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/views/OrdersPage.vue'),
    meta: { requiresAuth: true },
  },

  // 訂單詳情（需登入）
  {
    path: '/orders/:id',
    name: 'OrderDetail',
    component: () => import('@/views/OrderDetailPage.vue'),
    meta: { requiresAuth: true },
  },

  // 錢包（需登入）
  {
    path: '/wallet',
    name: 'Wallet',
    component: () => import('@/views/WalletPage.vue'),
    meta: { requiresAuth: true },
  },

  // 新聞公告（公開）
  {
    path: '/news',
    name: 'News',
    component: () => import('@/views/NewsPage.vue'),
  },
  {
    path: '/news/:id',
    name: 'NewsDetail',
    component: () => import('@/views/NewsDetailPage.vue'),
  },

  // 登入 / 註冊
  { path: '/login', name: 'Login', component: () => import('@/views/auth/LoginPage.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/auth/RegisterPage.vue') },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('@/views/auth/ForgotPasswordPage.vue') },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

// Navigation guard
router.beforeEach((to, _from, next) => {
  const auth = useAuthStore();
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return next({ name: 'Login', query: { redirect: to.fullPath } });
  }
  next();
});

export default router;
```

---

## 環境變數

```env
# .env.development
VITE_API_BASE_URL=http://localhost:8080

# .env.production
VITE_API_BASE_URL=https://api.kuji.example.com
```

---

## 常見陷阱

| 陷阱 | 正確做法 |
|------|---------|
| 從 `/api/wallet` 讀取餘額 | 從 `user.goldCoins` / `user.bonusCoins` 讀取 |
| 抽獎結果當物件處理 | `response.data.data` 永遠是陣列 |
| 忘記跨店家警告 | 使用 `isMultiStore` computed 顯示提示 |
| 保護期間仍允許抽獎 | 使用 `useProtectionTimer` 停用按鈕 |
| 沒有帶 `count` 參數 | 單抽傳 `{ count: 1 }`，多抽傳 `{ count: N }` |
