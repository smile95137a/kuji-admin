# Data Model: KUJI Client App Frontend

> **文件版本**: 1.0.0
> **最後更新**: 2026-02-12
> **範圍**: 前台使用者介面（Client App）TypeScript 型別定義與 Pinia Store 規格

---

## 1. 通用型別定義

```typescript
// types/common.ts

/** 通用 API 回應包裝 */
interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
}

/** 通用基底條件 */
interface BaseCondition {
  keyword?: string;
  createdAtStart?: string;
  createdAtEnd?: string;
}

/** 台灣地址 */
interface TaiwanAddress {
  city: string;        // 縣市（如：台北市）
  district: string;    // 鄉鎮市區（如：信義區）
  postalCode: string;  // 郵遞區號
  street: string;      // 街道地址
  detail?: string;     // 補充說明（如：幾樓）
}
```

---

## 2. 認證相關型別

```typescript
// types/auth.ts

/** 前台使用者資料（含錢包餘額） */
interface CliUser {
  id: string;
  email: string;
  nickname: string;
  avatar?: string;
  /** 金幣（可儲值取得） */
  goldCoins: number;
  /** 紅利幣（活動/推薦獎勵） */
  bonusCoins: number;
  /** 累計儲值金額 */
  totalRecharged: number;
  /** 推薦碼（分享給他人） */
  referralCode?: string;
  /** 推薦人 ID */
  referrerId?: string;
  status: 'ACTIVE' | 'DISABLED';
  createdAt: string;
  lastLoginAt?: string;
}

/** 前台認證狀態 */
interface CliAuthState {
  token: string | null;
  refreshToken: string | null;
  user: CliUser | null;
  isAuthenticated: boolean;
}

/** 登入請求 */
interface LoginReq {
  email: string;
  password: string;
}

/** 註冊請求 */
interface RegisterReq {
  email: string;
  password: string;
  nickname: string;
  referralCode?: string;   // URL ?ref= 自動帶入
}

/** 忘記密碼請求 */
interface ForgotPasswordReq {
  email: string;
}

/** 重設密碼請求 */
interface ResetPasswordReq {
  token: string;      // email 中的重設連結 token
  newPassword: string;
}

/** 餘額更新（抽獎後即時更新，不重新 fetch） */
interface BalanceUpdate {
  goldCoins: number;
  bonusCoins: number;
}
```

---

## 3. 抽獎商品相關型別

```typescript
// types/lottery.ts

/** 商品分類 */
type LotteryCategory =
  | 'OFFICIAL_ICHIBAN'
  | 'GACHA'
  | 'TRADING_CARD'
  | 'CUSTOM_GACHA';

/** 遊玩模式 */
type PlayMode = 'LOTTERY_MODE' | 'SCRATCH_MODE';

/** 商品狀態（前台關心的子集） */
type LotteryStatus =
  | 'ON_SHELF'
  | 'DRAWABLE'
  | 'RUNNING'
  | 'COMPLETED'
  | 'OFF_SHELF';

/** 獎品等級 */
type PrizeLevel = 'A' | 'B' | 'C' | 'D' | 'FINAL' | 'LAST';

/** 商品列表項目（瀏覽頁輕量版） */
interface LotteryListItem {
  id: string;
  storeId: string;
  storeName: string;
  title: string;
  category: LotteryCategory;
  playMode: PlayMode;
  pricePerDraw: number;
  maxDraws: number;
  remainingDraws: number;
  status: LotteryStatus;
  imageUrl: string;
  /** 進度百分比（(maxDraws - remainingDraws) / maxDraws） */
  progressPercent: number;
  startTime?: string;
  endTime?: string;
}

/** 商品詳情（含完整獎品列表） */
interface LotteryDetail extends LotteryListItem {
  description?: string;
  subCategory: string;
  currentDraws: number;
  prizes: Prize[];
  /** 是否為開箱玩家（由 session API 取得） */
  isUserOpener?: boolean;
}

/** 獎品（含詳細資訊） */
interface Prize {
  id: string;
  lotteryId: string;
  name: string;
  level: PrizeLevel;
  quantity: number;
  remaining: number;
  imageUrl: string;
  /** 回收可得紅利幣 */
  recycleBonus: number;
  description?: string;
  displayOrder?: number;
}

/** 商品瀏覽查詢條件 */
interface LotteryBrowseCondition extends BaseCondition {
  storeId?: string;
  category?: LotteryCategory;
  playMode?: PlayMode;
  status?: LotteryStatus | LotteryStatus[];
  priceMin?: number;
  priceMax?: number;
}
```

---

## 4. 抽獎執行相關型別

```typescript
// types/draw.ts

/** 票券狀態 */
type TicketStatus = 'AVAILABLE' | 'DRAWN' | 'SHIPPED' | 'RECYCLED';

/**
 * 票券
 * IMPORTANT: AVAILABLE 票券不含 prizeId/prizeLevel，前端不得預測獎品
 */
interface Ticket {
  id: string;
  ticketNumber: number;
  lotteryId: string;
  status: TicketStatus;
  /** 僅 DRAWN 後存在 */
  prizeId?: string;
  prizeLevel?: PrizeLevel;
  prizeName?: string;
  prizeImageUrl?: string;
  /** 是否已進入賞品盒 */
  isInPrizeBox?: boolean;
  drawnAt?: string;
}

/**
 * 抽獎 Session
 * 從 GET /api/lottery/draw/{id}/tickets 取得
 */
interface DrawSession {
  lotteryId: string;
  /** 當前使用者是否為此場次的開箱玩家 */
  isOpener: boolean;
  /**
   * 保護時間截止點（ISO 8601）
   * 若存在且尚未過期，非 opener 不得抽獎
   */
  protectionEndTime: string | null;
  /**
   * 大獎是否已被指定位置（刮刮樂模式）
   * SCRATCH_MODE + isOpener + !grandPrizesDesignated → 必須先指定
   */
  grandPrizesDesignated: boolean;
  remainingDraws: number;
  playMode: PlayMode;
}

/** 單次抽獎結果 */
interface DrawResult {
  ticketNumber: number;
  prizeId: string;
  prizeName: string;
  prizeLevel: PrizeLevel;
  prizeImageUrl: string;
  /** 是否為大賞/最後賞 */
  isGrandPrize: boolean;
  /** 抽獎後的剩餘金幣（用於即時更新 auth store） */
  remainingGold: number;
  /** 抽獎後的剩餘紅利幣 */
  remainingBonus: number;
  /** 抽獎後商品剩餘抽數 */
  remainingDraws: number;
}

/** 抽獎請求 */
interface DrawReq {
  count: number;
  /** 刮刮樂模式時，指定要抽的票券 */
  ticketIds?: string[];
}

/** 大獎位置指定請求（SCRATCH_MODE opener 開始前） */
interface DesignateGrandPrizesReq {
  /** 要設定為大獎的票券號碼陣列 */
  prizeNumbers: number[];
}

/** Draw Store 狀態 */
interface DrawState {
  lotteryId: string | null;
  tickets: Ticket[];
  session: DrawSession | null;
  results: DrawResult[];
  loading: boolean;
  /** 上次 fetch tickets 的時間（防止過快 refresh） */
  lastFetchAt: number | null;
}
```

---

## 5. 賞品盒相關型別

```typescript
// types/prizeBox.ts

/** 賞品盒項目狀態 */
type PrizeBoxStatus = 'IN_BOX' | 'SHIPPED' | 'RECYCLED';

/** 賞品盒項目 */
interface PrizeBoxItem {
  id: string;
  ticketId: string;
  ticketNumber: number;
  lotteryId: string;
  lotteryTitle: string;
  storeId: string;
  storeName: string;
  prizeName: string;
  prizeLevel: PrizeLevel;
  prizeImageUrl: string;
  /** 回收可得紅利幣 */
  recycleBonus: number;
  status: PrizeBoxStatus;
  /** 是否可申請出貨（IN_BOX 且店家支援） */
  isShippable: boolean;
  /** 是否可回收（IN_BOX 且未出貨） */
  isRecyclable: boolean;
  drawnAt: string;
  shippedAt?: string;
  recycledAt?: string;
}

/** 出貨方式 */
type ShippingMethod = 'HOME_DELIVERY' | 'CONVENIENCE_STORE';

/** 出貨請求 */
interface ShipRequest {
  prizeBoxIds: string[];
  shippingMethod: ShippingMethod;
  recipientName: string;
  recipientPhone: string;
  // 宅配
  address?: string;
  city?: string;
  district?: string;
  postalCode?: string;
  // 超商取貨
  storeCode?: string;
  convenienceStoreType?: 'SEVEN' | 'FAMILY' | 'HILIFE' | 'OK';
}

/** 回收請求 */
interface RecycleRequest {
  prizeBoxIds: string[];
}

/** 回收結果 */
interface RecycleResult {
  recycledCount: number;
  bonusEarned: number;
  newBonusCoins: number;   // 回收後總紅利幣
}

/** 賞品盒 Store 狀態 */
interface PrizeBoxState {
  items: PrizeBoxItem[];
  selectedIds: Set<string>;
  loading: boolean;
  filter: PrizeBoxStatus | 'ALL';
}
```

---

## 6. 訂單相關型別

```typescript
// types/order.ts

type OrderStatus = 'PENDING' | 'PREPARING' | 'SHIPPED' | 'COMPLETED' | 'CANCELLED';

/** 訂單（列表項目） */
interface Order {
  id: string;
  orderNo: string;
  status: OrderStatus;
  storeId: string;
  storeName: string;
  prizeCount: number;
  shippingMethod: ShippingMethod;
  createdAt: string;
  statusUpdatedAt: string;
}

/** 訂單詳情 */
interface OrderDetail extends Order {
  prizes: OrderPrize[];
  shippingInfo: ShippingInfo;
  statusHistory: OrderStatusHistory[];
  trackingNumber?: string;
  remark?: string;
}

/** 訂單內獎品 */
interface OrderPrize {
  id: string;
  prizeName: string;
  prizeLevel: PrizeLevel;
  prizeImageUrl: string;
  lotteryTitle: string;
  ticketNumber: number;
}

/** 出貨資訊 */
interface ShippingInfo {
  method: ShippingMethod;
  recipientName: string;
  recipientPhone: string;
  address?: string;
  city?: string;
  district?: string;
  postalCode?: string;
  storeCode?: string;
  convenienceStoreName?: string;
  convenienceStoreAddress?: string;
  trackingNumber?: string;
  estimatedDelivery?: string;
}

/** 訂單狀態歷史 */
interface OrderStatusHistory {
  status: OrderStatus;
  timestamp: string;
  remark?: string;
}
```

---

## 7. 其他業務型別

```typescript
// types/store.ts
interface Store {
  id: string;
  name: string;
  shortDescription: string;
  longDescription?: string;
  logoUrl: string;
  coverImageUrl?: string;
  email?: string;
  phone?: string;
  address: string;
  businessHours?: string;
  facebookUrl?: string;
  instagramUrl?: string;
  lineUrl?: string;
  status: 'ENABLED' | 'DISABLED';
  activeProductCount: number;
}

// types/news.ts
type NewsCategory = 'ANNOUNCEMENT' | 'EVENT' | 'SYSTEM';
type NewsStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

interface NewsArticle {
  id: string;
  title: string;
  summary?: string;
  content: string;
  category: NewsCategory;
  isImportant: boolean;
  status: NewsStatus;
  imageUrl?: string;
  publishTime?: string;
  viewCount?: number;
  createdAt: string;
}

// types/banner.ts
interface Banner {
  id: string;
  storeId?: string;
  storeName?: string;
  imageUrl: string;
  linkUrl?: string;
  altText?: string;
  displayOrder: number;
}

// types/address.ts
interface Address {
  id: string;
  label?: string;           // 如：「家裡」「公司」
  recipientName: string;
  recipientPhone: string;
  city: string;
  district: string;
  postalCode: string;
  street: string;
  detail?: string;
  isDefault: boolean;
  createdAt: string;
}
```

---

## 8. Pinia Stores 規格

### 8.1 Auth Store（前台版本）

```typescript
// stores/auth.ts
import { defineStore } from 'pinia';

export const useAuthStore = defineStore('auth', {
  state: (): CliAuthState => ({
    token: localStorage.getItem('user_token'),
    refreshToken: localStorage.getItem('user_refresh_token'),
    user: null,
    isAuthenticated: false,
  }),

  actions: {
    async login(email: string, password: string): Promise<void> {
      const res = await authService.login({ email, password });
      this.token = res.data.token;
      this.refreshToken = res.data.refreshToken;
      this.user = res.data.user;
      this.isAuthenticated = true;
      localStorage.setItem('user_token', this.token);
      localStorage.setItem('user_refresh_token', this.refreshToken);
    },

    async logout(): Promise<void> {
      try {
        await authService.logout();
      } finally {
        this.token = null;
        this.refreshToken = null;
        this.user = null;
        this.isAuthenticated = false;
        localStorage.removeItem('user_token');
        localStorage.removeItem('user_refresh_token');
      }
    },

    async refresh(): Promise<boolean> {
      if (!this.refreshToken) return false;
      try {
        const res = await authService.refresh({ refreshToken: this.refreshToken });
        this.token = res.data.token;
        localStorage.setItem('user_token', this.token);
        return true;
      } catch {
        await this.logout();
        return false;
      }
    },

    /**
     * 抽獎後即時更新餘額，無需重新 fetch /api/user/me
     */
    updateBalance({ goldCoins, bonusCoins }: BalanceUpdate): void {
      if (this.user) {
        this.user.goldCoins = goldCoins;
        this.user.bonusCoins = bonusCoins;
      }
    },

    /**
     * GET /api/user/me — 取得完整使用者資訊（含 goldCoins/bonusCoins）
     */
    async fetchUser(): Promise<void> {
      const res = await userService.getMe();
      this.user = res.data;
      this.isAuthenticated = true;
    },

    /** 儲值完成後更新 goldCoins */
    addGold(amount: number): void {
      if (this.user) {
        this.user.goldCoins += amount;
        this.user.totalRecharged += amount;
      }
    },
  },

  getters: {
    goldCoins: (state): number => state.user?.goldCoins ?? 0,
    bonusCoins: (state): number => state.user?.bonusCoins ?? 0,
    totalCoins: (state): number => (state.user?.goldCoins ?? 0) + (state.user?.bonusCoins ?? 0),
    referralCode: (state): string | undefined => state.user?.referralCode,
    referralLink: (state): string => {
      const code = state.user?.referralCode;
      return code ? `${window.location.origin}/register?ref=${code}` : '';
    },
  },
});
```

### 8.2 Draw Store

```typescript
// stores/draw.ts
import { defineStore } from 'pinia';

export const useDrawStore = defineStore('draw', {
  state: (): DrawState => ({
    lotteryId: null,
    tickets: [],
    session: null,
    results: [],
    loading: false,
    lastFetchAt: null,
  }),

  actions: {
    /**
     * GET /api/lottery/draw/{id}/tickets
     * 取得票券列表 + DrawSession（isOpener, protectionEndTime, grandPrizesDesignated）
     */
    async fetchTickets(lotteryId: string): Promise<void> {
      this.loading = true;
      try {
        const res = await drawService.getTickets(lotteryId);
        this.lotteryId = lotteryId;
        this.tickets = res.data.tickets;
        this.session = res.data.session;
        this.lastFetchAt = Date.now();
      } finally {
        this.loading = false;
      }
    },

    /**
     * POST /api/lottery/draw/{id}/draw
     * 執行抽獎，返回結果陣列（永遠是陣列，即使單抽）
     */
    async executeDraw(lotteryId: string, count: number, ticketIds?: string[]): Promise<DrawResult[]> {
      this.loading = true;
      try {
        const res = await drawService.draw(lotteryId, { count, ticketIds });
        const results: DrawResult[] = res.data;
        this.results = results;

        // 即時更新 auth store 餘額（從最後一筆 result 取得最新餘額）
        if (results.length > 0) {
          const last = results[results.length - 1];
          const authStore = useAuthStore();
          authStore.updateBalance({
            goldCoins: last.remainingGold,
            bonusCoins: last.remainingBonus,
          });
          // 更新本地 session 的剩餘抽數
          if (this.session) {
            this.session.remainingDraws = last.remainingDraws;
          }
        }

        return results;
      } finally {
        this.loading = false;
      }
    },

    /**
     * POST /api/lottery/draw/{id}/designate-grand-prizes
     * 刮刮樂模式：開箱玩家在開始前指定大獎位置
     */
    async designatePrizes(lotteryId: string, prizeNumbers: number[]): Promise<void> {
      await drawService.designateGrandPrizes(lotteryId, { prizeNumbers });
      if (this.session) {
        this.session.grandPrizesDesignated = true;
      }
    },

    clearResults(): void {
      this.results = [];
    },

    reset(): void {
      this.lotteryId = null;
      this.tickets = [];
      this.session = null;
      this.results = [];
      this.loading = false;
      this.lastFetchAt = null;
    },
  },

  getters: {
    availableTickets: (state): Ticket[] =>
      state.tickets.filter((t) => t.status === 'AVAILABLE'),

    drawnTickets: (state): Ticket[] =>
      state.tickets.filter((t) => t.status === 'DRAWN'),

    /** 是否正在保護時間內（非 opener 不得抽） */
    isUnderProtection: (state): boolean => {
      if (!state.session?.protectionEndTime) return false;
      return Date.now() < new Date(state.session.protectionEndTime).getTime();
    },

    /** 是否需要先指定大獎（刮刮樂 opener 開始前） */
    needsDesignation: (state): boolean => {
      return (
        state.session?.playMode === 'SCRATCH_MODE' &&
        !!state.session.isOpener &&
        !state.session.grandPrizesDesignated
      );
    },

    canDraw: (state): boolean => {
      if (!state.session) return false;
      if (state.session.remainingDraws <= 0) return false;
      if (state.session.playMode === 'SCRATCH_MODE' &&
          state.session.isOpener &&
          !state.session.grandPrizesDesignated) return false;
      return true;
    },
  },
});
```

### 8.3 Prize Box Store

```typescript
// stores/prizeBox.ts
import { defineStore } from 'pinia';
import { groupBy } from '@/utils/collection';

export const usePrizeBoxStore = defineStore('prizeBox', {
  state: (): PrizeBoxState => ({
    items: [],
    selectedIds: new Set<string>(),
    loading: false,
    filter: 'ALL',
  }),

  actions: {
    /**
     * GET /api/prize-box
     * 取得當前使用者的所有賞品盒項目
     */
    async fetchItems(): Promise<void> {
      this.loading = true;
      try {
        const res = await prizeBoxService.getItems();
        this.items = res.data;
        // 清除已不存在的選取
        this.selectedIds = new Set(
          [...this.selectedIds].filter((id) => this.items.some((i) => i.id === id))
        );
      } finally {
        this.loading = false;
      }
    },

    toggleSelect(id: string): void {
      if (this.selectedIds.has(id)) {
        this.selectedIds.delete(id);
      } else {
        this.selectedIds.add(id);
      }
    },

    selectAllInStore(storeId: string): void {
      const ids = this.items
        .filter((i) => i.storeId === storeId && i.status === 'IN_BOX' && i.isShippable)
        .map((i) => i.id);
      ids.forEach((id) => this.selectedIds.add(id));
    },

    selectAll(): void {
      this.items
        .filter((i) => i.status === 'IN_BOX' && i.isShippable)
        .forEach((i) => this.selectedIds.add(i.id));
    },

    clearSelection(): void {
      this.selectedIds = new Set();
    },

    setFilter(filter: PrizeBoxStatus | 'ALL'): void {
      this.filter = filter;
    },

    /**
     * POST /api/prize-box/ship
     * 出貨申請（後端自動依 storeId 拆單）
     */
    async shipSelected(shippingInfo: Omit<ShipRequest, 'prizeBoxIds'>): Promise<Order[]> {
      this.loading = true;
      try {
        const res = await prizeBoxService.ship({
          prizeBoxIds: [...this.selectedIds],
          ...shippingInfo,
        });
        await this.fetchItems();
        this.clearSelection();
        // AV-006: 回應可能是 Order[] 或單筆 Order，統一用陣列
        return Array.isArray(res.data) ? res.data : [res.data];
      } finally {
        this.loading = false;
      }
    },

    /**
     * POST /api/prize-box/recycle
     * 回收選取的賞品（轉為紅利幣）
     */
    async recycleSelected(ids: string[]): Promise<RecycleResult> {
      this.loading = true;
      try {
        const res = await prizeBoxService.recycle({ prizeBoxIds: ids });
        const result: RecycleResult = res.data;
        // 更新 auth store 中的紅利幣餘額
        const authStore = useAuthStore();
        authStore.updateBalance({
          goldCoins: authStore.goldCoins,
          bonusCoins: result.newBonusCoins,
        });
        await this.fetchItems();
        this.clearSelection();
        return result;
      } finally {
        this.loading = false;
      }
    },
  },

  getters: {
    filteredItems: (state): PrizeBoxItem[] => {
      if (state.filter === 'ALL') return state.items;
      return state.items.filter((i) => i.status === state.filter);
    },

    selectedItems: (state): PrizeBoxItem[] =>
      state.items.filter((i) => state.selectedIds.has(i.id)),

    /** 按店家分組（IN_BOX 項目，用於出貨分組顯示） */
    storeGroups: (state): Record<string, PrizeBoxItem[]> =>
      groupBy(
        state.items.filter((i) => i.status === 'IN_BOX'),
        'storeId'
      ),

    /** 已選取項目涉及幾家不同店家 */
    selectedStoreCount: (state): number => {
      const storeIds = new Set(
        state.items
          .filter((i) => state.selectedIds.has(i.id))
          .map((i) => i.storeId)
      );
      return storeIds.size;
    },

    /** 是否跨店家選取（出貨時需提示拆單） */
    isMultiStore: (state): boolean => {
      const storeIds = new Set(
        state.items
          .filter((i) => state.selectedIds.has(i.id))
          .map((i) => i.storeId)
      );
      return storeIds.size > 1;
    },

    inBoxCount: (state): number =>
      state.items.filter((i) => i.status === 'IN_BOX').length,

    selectedCount: (state): number => state.selectedIds.size,
  },
});
```

---

## 9. Composables 規格

### 9.1 useDraw

```typescript
// composables/useDraw.ts
/**
 * 抽獎頁主要 composable
 * 整合 draw store + protection timer + 餘額檢查
 */
export function useDraw(lotteryId: string) {
  const drawStore = useDrawStore();
  const authStore = useAuthStore();

  const protectionEndTime = computed(() => drawStore.session?.protectionEndTime ?? null);
  const { remainingSeconds, isProtected, displayTime } = useProtectionTimer(
    computed(() => protectionEndTime.value)
  );

  /** 是否有足夠餘額執行抽獎 */
  function hasEnoughBalance(pricePerDraw: number, count = 1): boolean {
    return authStore.totalCoins >= pricePerDraw * count;
  }

  /** 執行單抽 */
  async function drawOnce(pricePerDraw: number): Promise<DrawResult[]> {
    if (!hasEnoughBalance(pricePerDraw)) {
      throw new Error('INSUFFICIENT_BALANCE');
    }
    if (isProtected.value && !drawStore.session?.isOpener) {
      throw new Error('UNDER_PROTECTION');
    }
    return drawStore.executeDraw(lotteryId, 1);
  }

  /** 執行多抽 */
  async function drawMultiple(count: number, pricePerDraw: number): Promise<DrawResult[]> {
    if (!hasEnoughBalance(pricePerDraw, count)) {
      throw new Error('INSUFFICIENT_BALANCE');
    }
    return drawStore.executeDraw(lotteryId, count);
  }

  onMounted(() => drawStore.fetchTickets(lotteryId));
  onUnmounted(() => drawStore.reset());

  return {
    session: computed(() => drawStore.session),
    tickets: computed(() => drawStore.tickets),
    availableTickets: computed(() => drawStore.availableTickets),
    results: computed(() => drawStore.results),
    loading: computed(() => drawStore.loading),
    canDraw: computed(() => drawStore.canDraw),
    needsDesignation: computed(() => drawStore.needsDesignation),
    isProtected,
    protectionDisplayTime: displayTime,
    remainingSeconds,
    hasEnoughBalance,
    drawOnce,
    drawMultiple,
    clearResults: drawStore.clearResults.bind(drawStore),
    designatePrizes: drawStore.designatePrizes.bind(drawStore),
  };
}
```

### 9.2 useProtectionTimer

```typescript
// composables/useProtectionTimer.ts
import { ref, computed, watch, onUnmounted } from 'vue';

/**
 * 保護時間倒數計時器
 * @param endTime ISO 8601 時間字串，null 表示無保護時間
 */
export function useProtectionTimer(endTime: Ref<string | null>) {
  const remainingSeconds = ref(0);

  let timer: ReturnType<typeof setInterval> | null = null;

  function tick() {
    if (!endTime.value) {
      remainingSeconds.value = 0;
      return;
    }
    const ms = new Date(endTime.value).getTime() - Date.now();
    remainingSeconds.value = Math.max(0, Math.floor(ms / 1000));
    if (remainingSeconds.value === 0 && timer) {
      clearInterval(timer);
      timer = null;
    }
  }

  function startTimer() {
    if (timer) clearInterval(timer);
    tick();
    if (remainingSeconds.value > 0) {
      timer = setInterval(tick, 1000);
    }
  }

  watch(endTime, (val) => {
    if (val) startTimer();
    else {
      if (timer) clearInterval(timer);
      remainingSeconds.value = 0;
    }
  }, { immediate: true });

  onUnmounted(() => { if (timer) clearInterval(timer); });

  const isProtected = computed(() => remainingSeconds.value > 0);

  /** mm:ss 格式顯示 */
  const displayTime = computed(() => {
    const m = Math.floor(remainingSeconds.value / 60);
    const s = remainingSeconds.value % 60;
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  });

  return { remainingSeconds, isProtected, displayTime };
}
```

### 9.3 usePrizeBox

```typescript
// composables/usePrizeBox.ts
/**
 * 賞品盒頁面 composable
 * 封裝 prizeBox store 的出貨/回收流程
 */
export function usePrizeBox() {
  const store = usePrizeBoxStore();
  const uiStore = useUiStore();

  onMounted(() => store.fetchItems());

  /** 出貨流程（含跨店家警告 Modal） */
  async function initiateShip(shippingInfo: Omit<ShipRequest, 'prizeBoxIds'>): Promise<Order[]> {
    if (store.isMultiStore) {
      const confirmed = await showMultiStoreWarning(store.selectedStoreCount);
      if (!confirmed) throw new Error('USER_CANCELLED');
    }
    const orders = await store.shipSelected(shippingInfo);
    uiStore.showSuccess(`出貨申請成功！共 ${orders.length} 筆訂單`);
    return orders;
  }

  /** 回收流程（含確認 Modal） */
  async function initiateRecycle(ids: string[]): Promise<RecycleResult> {
    const items = store.items.filter((i) => ids.includes(i.id));
    const totalBonus = items.reduce((sum, i) => sum + i.recycleBonus, 0);
    const confirmed = await showRecycleConfirm(items.length, totalBonus);
    if (!confirmed) throw new Error('USER_CANCELLED');
    const result = await store.recycleSelected(ids);
    uiStore.showSuccess(`已回收 ${result.recycledCount} 件，獲得 ${result.bonusEarned} 紅利幣`);
    return result;
  }

  return {
    items: computed(() => store.filteredItems),
    selectedIds: computed(() => store.selectedIds),
    selectedItems: computed(() => store.selectedItems),
    selectedCount: computed(() => store.selectedCount),
    storeGroups: computed(() => store.storeGroups),
    isMultiStore: computed(() => store.isMultiStore),
    inBoxCount: computed(() => store.inBoxCount),
    loading: computed(() => store.loading),
    filter: computed(() => store.filter),
    setFilter: store.setFilter.bind(store),
    toggleSelect: store.toggleSelect.bind(store),
    selectAllInStore: store.selectAllInStore.bind(store),
    selectAll: store.selectAll.bind(store),
    clearSelection: store.clearSelection.bind(store),
    initiateShip,
    initiateRecycle,
    refresh: () => store.fetchItems(),
  };
}
```

### 9.4 useWalletBalance

```typescript
// composables/useWalletBalance.ts
/**
 * 錢包餘額 composable
 * 從 auth store 取得，零 network cost
 * 來源：GET /api/user/me（於 login/fetchUser 時填入）
 */
export function useWalletBalance() {
  const authStore = useAuthStore();

  const goldCoins = computed(() => authStore.user?.goldCoins ?? 0);
  const bonusCoins = computed(() => authStore.user?.bonusCoins ?? 0);
  const totalCoins = computed(() => goldCoins.value + bonusCoins.value);
  const totalRecharged = computed(() => authStore.user?.totalRecharged ?? 0);

  /** 顯示格式：加千分位逗號 */
  const goldDisplay = computed(() => goldCoins.value.toLocaleString('zh-TW'));
  const bonusDisplay = computed(() => bonusCoins.value.toLocaleString('zh-TW'));
  const totalDisplay = computed(() => totalCoins.value.toLocaleString('zh-TW'));

  /** 檢查是否有足夠餘額 */
  function canAfford(amount: number): boolean {
    return totalCoins.value >= amount;
  }

  /** 不足金幣的提示文字 */
  function insufficientText(required: number): string {
    const lacking = required - totalCoins.value;
    return `金幣不足，還需 ${lacking.toLocaleString('zh-TW')} 金幣`;
  }

  return {
    goldCoins,
    bonusCoins,
    totalCoins,
    totalRecharged,
    goldDisplay,
    bonusDisplay,
    totalDisplay,
    canAfford,
    insufficientText,
  };
}
```

---

## 10. 狀態常數對照表

```typescript
// constants/prizeBox.ts
export const PRIZE_BOX_STATUS_LABEL: Record<PrizeBoxStatus | 'ALL', string> = {
  ALL: '全部',
  IN_BOX: '在賞品盒',
  SHIPPED: '已出貨',
  RECYCLED: '已回收',
};

export const PRIZE_BOX_STATUS_COLOR: Record<PrizeBoxStatus, string> = {
  IN_BOX: 'green',
  SHIPPED: 'blue',
  RECYCLED: 'gray',
};

// constants/order.ts
export const ORDER_STATUS_LABEL: Record<OrderStatus, string> = {
  PENDING: '待處理',
  PREPARING: '備貨中',
  SHIPPED: '已出貨',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
};

export const PRIZE_LEVEL_LABEL: Record<PrizeLevel, string> = {
  A: 'A 賞',
  B: 'B 賞',
  C: 'C 賞',
  D: 'D 賞',
  FINAL: '大賞',
  LAST: '最後賞',
};

export const PRIZE_LEVEL_COLOR: Record<PrizeLevel, string> = {
  A: '#FFD700',
  B: '#C0C0C0',
  C: '#CD7F32',
  D: '#5C85D6',
  FINAL: '#FF4444',
  LAST: '#9B59B6',
};
```

---

## 11. Service 層規格

```typescript
// services/draw.service.ts
export const drawService = {
  getTickets: (lotteryId: string) =>
    http.get<ApiResponse<{ tickets: Ticket[]; session: DrawSession }>>(
      `/api/lottery/draw/${lotteryId}/tickets`
    ),

  draw: (lotteryId: string, req: DrawReq) =>
    http.post<ApiResponse<DrawResult[]>>(
      `/api/lottery/draw/${lotteryId}/draw`, req
    ),

  designateGrandPrizes: (lotteryId: string, req: DesignateGrandPrizesReq) =>
    http.post<ApiResponse<void>>(
      `/api/lottery/draw/${lotteryId}/designate-grand-prizes`, req
    ),
};

// services/prizeBox.service.ts
export const prizeBoxService = {
  getItems: () =>
    http.get<ApiResponse<PrizeBoxItem[]>>('/api/prize-box'),

  ship: (req: ShipRequest) =>
    http.post<ApiResponse<Order | Order[]>>('/api/prize-box/ship', req),

  recycle: (req: RecycleRequest) =>
    http.post<ApiResponse<RecycleResult>>('/api/prize-box/recycle', req),
};

// services/user.service.ts
export const userService = {
  getMe: () =>
    http.get<ApiResponse<CliUser>>('/api/user/me'),

  updateProfile: (data: Partial<Pick<CliUser, 'nickname' | 'avatar'>>) =>
    http.patch<ApiResponse<CliUser>>('/api/user/me', data),
};

// services/lottery.service.ts（前台版）
export const lotteryService = {
  list: (condition?: Partial<LotteryBrowseCondition>) =>
    http.post<ApiResponse<LotteryListItem[]>>('/api/lottery/list', { condition }),

  getById: (id: string) =>
    http.get<ApiResponse<LotteryDetail>>(`/api/lottery/${id}`),
};
```

---

## 12. 工具函數

```typescript
// utils/collection.ts
export function groupBy<T>(arr: T[], key: keyof T): Record<string, T[]> {
  return arr.reduce((acc, item) => {
    const groupKey = String(item[key]);
    if (!acc[groupKey]) acc[groupKey] = [];
    acc[groupKey].push(item);
    return acc;
  }, {} as Record<string, T[]>);
}

// utils/format.ts
export function formatCoin(amount: number): string {
  return amount.toLocaleString('zh-TW');
}

export function formatDate(dateStr: string): string {
  return new Intl.DateTimeFormat('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  }).format(new Date(dateStr));
}

export function formatDateOnly(dateStr: string): string {
  return new Intl.DateTimeFormat('zh-TW', {
    year: 'numeric', month: '2-digit', day: '2-digit',
  }).format(new Date(dateStr));
}
```

---

*本文件為前台資料模型定義，應與後端 DTO 保持同步更新。*
