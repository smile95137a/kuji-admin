# Data Model: KUJI Admin Panel Frontend

> **文件版本**: 1.0.0
> **最後更新**: 2026-02-12
> **範圍**: 後台管理介面（Admin Panel）TypeScript 型別定義與 Pinia Store 規格

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

/** 通用列表查詢請求 */
interface ListReq<T = Record<string, unknown>> {
  condition?: Partial<T>;
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
}

/** 通用基底條件（所有 condition 繼承此介面） */
interface BaseCondition {
  keyword?: string;
  createdAtStart?: string;   // ISO 8601
  createdAtEnd?: string;     // ISO 8601
}

/** 分頁參數（前端分頁，後端不做分頁） */
interface PaginationState {
  currentPage: number;
  pageSize: number;
  total: number;
}

/** 排序狀態 */
interface SortState {
  field: string;
  order: 'ASC' | 'DESC';
}
```

---

## 2. 認證相關型別

```typescript
// types/auth.ts

/** 後台管理員角色 */
type AdminRole = 'ROLE_ADMIN' | 'ROLE_STORE_OWNER' | 'ROLE_STORE_EDITOR';

/** 帳號狀態 */
type UserStatus = 'ACTIVE' | 'DISABLED' | 'PENDING';

/** 後台使用者資料 */
interface AdminUser {
  id: string;
  email: string;
  displayName: string;
  avatar?: string;
  roles: AdminRole[];
  /** 若為店家角色，自動帶入的主要店家 ID */
  storeId?: string;
  /** 若為店家角色，顯示用店家名稱 */
  storeName?: string;
  status: UserStatus;
  createdAt: string;
  lastLoginAt?: string;
}

/** 認證狀態（auth store state） */
interface AuthState {
  token: string | null;
  refreshToken: string | null;
  user: AdminUser | null;
  isAuthenticated: boolean;
  /** token 到期時間（Unix timestamp ms） */
  tokenExpiresAt: number | null;
}

/** 登入請求 */
interface LoginReq {
  email: string;
  password: string;
}

/** 登入回應 data */
interface LoginRes {
  token: string;
  refreshToken: string;
  user: AdminUser;
}

/** Token Refresh 請求 */
interface RefreshReq {
  refreshToken: string;
}

/** Token Refresh 回應 data */
interface RefreshRes {
  token: string;
  /** 部分後端也會回傳新的 refreshToken */
  refreshToken?: string;
}
```

---

## 3. 選單與 RBAC 型別

```typescript
// types/menu.ts

/** 單一選單項目 */
interface MenuItem {
  id: string;
  name: string;
  path: string;
  icon?: string;
  parentId?: string | null;
  sort: number;
  children?: MenuItem[];
  permissions: MenuPermissions;
}

/** 選單權限 */
interface MenuPermissions {
  canView: boolean;
  canEdit: boolean;
  canDelete: boolean;
}

/** 選單 Store 狀態 */
interface MenuState {
  menus: MenuItem[];
  loaded: boolean;
  loading: boolean;
}

/** 路由 Meta（擴充 Vue Router RouteMeta） */
interface AppRouteMeta {
  public?: boolean;
  roles?: AdminRole[];
  title?: string;
  breadcrumb?: string[];
  keepAlive?: boolean;
  icon?: string;
}
```

---

## 4. 抽獎商品相關型別

```typescript
// types/lottery.ts

/** 商品狀態生命週期 */
type LotteryStatus =
  | 'DRAFT'        // 草稿
  | 'CONFIGURED'   // 已設定獎品
  | 'ON_SHELF'     // 上架（可瀏覽，不可抽）
  | 'DRAWABLE'     // 可抽（已開放）
  | 'RUNNING'      // 進行中
  | 'COMPLETED'    // 已完售
  | 'OFF_SHELF';   // 下架

/** 商品分類 */
type LotteryCategory =
  | 'OFFICIAL_ICHIBAN'  // 官方一番賞
  | 'GACHA'             // 扭蛋
  | 'TRADING_CARD'      // 集換式卡牌
  | 'CUSTOM_GACHA';     // 自訂扭蛋

/** 遊玩模式 */
type PlayMode = 'LOTTERY_MODE' | 'SCRATCH_MODE';

/** 抽獎商品（列表項目） */
interface LotteryProduct {
  id: string;
  storeId: string;
  storeName: string;
  title: string;
  description?: string;
  category: LotteryCategory;
  subCategory: string;
  playMode: PlayMode;
  pricePerDraw: number;      // 每抽價格（金幣）
  maxDraws: number;          // 總抽數
  remainingDraws: number;    // 剩餘抽數
  currentDraws: number;      // 已抽次數
  status: LotteryStatus;
  imageUrl: string;
  weight?: number;           // 排序權重（用途待確認 AV-005）
  startTime?: string;        // ISO 8601
  endTime?: string;          // ISO 8601
  createdAt: string;
  updatedAt: string;
}

/** 抽獎商品建立/編輯請求 */
interface LotteryCreateReq {
  storeId: string;           // 後端自動從 JWT 帶入（STORE_OWNER 角色）
  title: string;
  description?: string;
  category: LotteryCategory;
  subCategory: string;
  playMode: PlayMode;
  pricePerDraw: number;
  maxDraws: number;
  imageUrl: string;
  startTime?: string;
  endTime?: string;
  weight?: number;
}

/** 查詢商品列表條件 */
interface LotteryCondition extends BaseCondition {
  storeId?: string;
  title?: string;
  status?: LotteryStatus;
  category?: LotteryCategory;
  playMode?: PlayMode;
  priceMin?: number;
  priceMax?: number;
}

/** 獎品等級 */
type PrizeLevel = 'A' | 'B' | 'C' | 'D' | 'FINAL' | 'LAST';

/** 獎品 */
interface Prize {
  id: string;
  lotteryId: string;
  name: string;
  level: PrizeLevel;
  quantity: number;
  remaining: number;
  imageUrl: string;
  recycleBonus: number;      // 回收金幣數
  description?: string;
  displayOrder?: number;
}

/** 獎品建立請求 */
interface PrizeCreateReq {
  name: string;
  level: PrizeLevel;
  quantity: number;
  imageUrl: string;
  recycleBonus: number;
  description?: string;
  displayOrder?: number;
}

/** 票券狀態 */
type TicketStatus = 'AVAILABLE' | 'DRAWN' | 'SHIPPED' | 'RECYCLED';

/** 票券（後台檢視用） */
interface Ticket {
  id: string;
  ticketNumber: number;
  lotteryId: string;
  status: TicketStatus;
  prizeId?: string;          // 僅 DRAWN 後有值
  prizeLevel?: PrizeLevel;   // 僅 DRAWN 後有值
  prizeName?: string;        // 僅 DRAWN 後有值
  drawnAt?: string;
  playerId?: string;
  playerEmail?: string;
}
```

---

## 5. 訂單相關型別

```typescript
// types/order.ts

/** 訂單狀態 */
type OrderStatus =
  | 'PENDING'       // 待處理
  | 'PREPARING'     // 準備中
  | 'SHIPPED'       // 已出貨
  | 'COMPLETED'     // 已完成
  | 'CANCELLED';    // 已取消

/** 出貨方式 */
type ShippingMethod = 'HOME_DELIVERY' | 'CONVENIENCE_STORE';

/** 訂單（列表項目） */
interface Order {
  id: string;
  orderNo: string;
  status: OrderStatus;
  storeId: string;
  storeName: string;
  playerEmail: string;
  playerNickname: string;
  shippingMethod: ShippingMethod;
  prizeCount: number;
  totalAmount: number;
  createdAt: string;
  statusUpdatedAt: string;
}

/** 訂單明細（詳情頁） */
interface OrderDetail extends Order {
  store: {
    id: string;
    name: string;
  };
  player: {
    id: string;
    email: string;
    nickname: string;
    avatar?: string;
  };
  prizes: OrderPrize[];
  shippingInfo: ShippingInfo;
  statusHistory: OrderStatusHistory[];
  remark?: string;
}

/** 訂單內獎品項目 */
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
  recipientName?: string;
  recipientPhone?: string;
  // 宅配
  address?: string;
  postalCode?: string;
  city?: string;
  district?: string;
  // 超商取貨
  storeCode?: string;
  convenienceStoreName?: string;
  convenienceStoreAddress?: string;
  /** 出貨後的物流追蹤號 */
  trackingNumber?: string;
}

/** 訂單狀態歷史 */
interface OrderStatusHistory {
  status: OrderStatus;
  operator: string;           // 操作者帳號
  operatorRole: AdminRole;
  timestamp: string;          // ISO 8601
  remark?: string;
}

/** 訂單狀態更新請求 */
interface OrderStatusUpdateReq {
  status: OrderStatus;
  trackingNumber?: string;    // 出貨時填入
  remark?: string;
}

/** 訂單查詢條件 */
interface OrderCondition extends BaseCondition {
  storeId?: string;
  status?: OrderStatus;
  shippingMethod?: ShippingMethod;
  playerEmail?: string;
  orderNo?: string;
}
```

---

## 6. 店家相關型別

```typescript
// types/store.ts

/** 店家狀態 */
type StoreStatus = 'ENABLED' | 'DISABLED';

/** 店家 */
interface Store {
  id: string;
  name: string;
  shortDescription: string;
  longDescription?: string;
  logoUrl: string;
  coverImageUrl?: string;
  email: string;
  phone: string;
  address: string;
  businessHours?: string;
  facebookUrl?: string;
  instagramUrl?: string;
  lineUrl?: string;
  status: StoreStatus;
  /** 主要負責人帳號 */
  ownerEmail?: string;
  ownerId?: string;
  /** 統計欄位（列表頁顯示用） */
  productCount?: number;
  activeProductCount?: number;
  createdAt: string;
  updatedAt: string;
}

/** 店家建立請求 */
interface StoreCreateReq {
  name: string;
  shortDescription: string;
  longDescription?: string;
  logoUrl: string;
  coverImageUrl?: string;
  email: string;
  phone: string;
  address: string;
  businessHours?: string;
  facebookUrl?: string;
  instagramUrl?: string;
  lineUrl?: string;
  ownerEmail: string;         // 店主帳號（系統自動建立關聯）
}

/** 店家查詢條件 */
interface StoreCondition extends BaseCondition {
  name?: string;
  status?: StoreStatus;
  ownerEmail?: string;
}
```

---

## 7. Banner 相關型別

```typescript
// types/banner.ts

/** Banner 狀態 */
type BannerStatus = 'PUBLISHED' | 'DRAFT' | 'ARCHIVED';

/** Banner */
interface Banner {
  id: string;
  storeId: string;
  storeName: string;
  imageUrl: string;
  linkUrl?: string;
  altText?: string;
  displayOrder: number;
  status: BannerStatus;
  publishTime?: string;       // ISO 8601，排程發佈
  unpublishTime?: string;     // ISO 8601，排程下架
  createdAt: string;
  updatedAt: string;
}

/** Banner 建立/更新請求 */
interface BannerUpsertReq {
  storeId: string;
  imageUrl: string;
  linkUrl?: string;
  altText?: string;
  displayOrder?: number;
  status: BannerStatus;
  publishTime?: string;
  unpublishTime?: string;
}

/** Banner 排序更新請求 */
interface BannerReorderReq {
  orders: Array<{ id: string; displayOrder: number }>;
}
```

---

## 8. 消息相關型別

```typescript
// types/news.ts

/** 消息分類 */
type NewsCategory = 'ANNOUNCEMENT' | 'EVENT' | 'SYSTEM';

/** 消息狀態 */
type NewsStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

/** 消息文章 */
interface NewsArticle {
  id: string;
  title: string;
  content: string;             // 富文字 HTML 或 Markdown
  summary?: string;            // 列表頁顯示摘要
  category: NewsCategory;
  isImportant: boolean;        // 置頂/重要標記
  status: NewsStatus;
  imageUrl?: string;           // 封面圖
  publishTime?: string;        // 排程發佈時間
  unpublishTime?: string;      // 排程下架時間
  viewCount?: number;          // 瀏覽次數（唯讀）
  createdAt: string;
  updatedAt: string;
  createdBy: string;           // 建立者帳號
}

/** 消息建立/更新請求 */
interface NewsUpsertReq {
  title: string;
  content: string;
  summary?: string;
  category: NewsCategory;
  isImportant?: boolean;
  status: NewsStatus;
  imageUrl?: string;
  publishTime?: string;
  unpublishTime?: string;
}

/** 消息查詢條件 */
interface NewsCondition extends BaseCondition {
  title?: string;
  category?: NewsCategory;
  status?: NewsStatus;
  isImportant?: boolean;
}
```

---

## 9. Pinia Stores 規格

### 9.1 Auth Store

```typescript
// stores/auth.ts
import { defineStore } from 'pinia';

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('admin_token'),
    refreshToken: localStorage.getItem('admin_refresh_token'),
    user: null,
    isAuthenticated: false,
    tokenExpiresAt: null,
  }),

  actions: {
    /**
     * 使用帳號密碼登入
     * 成功後存 token 到 localStorage，並呼叫 fetchCurrentUser
     */
    async login(email: string, password: string): Promise<void> {
      const res = await authService.login({ email, password });
      this.token = res.data.token;
      this.refreshToken = res.data.refreshToken;
      this.user = res.data.user;
      this.isAuthenticated = true;
      localStorage.setItem('admin_token', this.token);
      localStorage.setItem('admin_refresh_token', this.refreshToken);
    },

    /**
     * 登出：清除所有 token 與使用者資料
     */
    async logout(): Promise<void> {
      try {
        await authService.logout(); // 呼叫後端使 refreshToken 失效
      } finally {
        this.token = null;
        this.refreshToken = null;
        this.user = null;
        this.isAuthenticated = false;
        this.tokenExpiresAt = null;
        localStorage.removeItem('admin_token');
        localStorage.removeItem('admin_refresh_token');
      }
    },

    /**
     * 使用 refreshToken 取得新 accessToken
     * @returns true 代表 refresh 成功，false 代表需要重新登入
     */
    async refreshAccessToken(): Promise<boolean> {
      if (!this.refreshToken) return false;
      try {
        const res = await authService.refresh({ refreshToken: this.refreshToken });
        this.token = res.data.token;
        localStorage.setItem('admin_token', this.token);
        return true;
      } catch {
        await this.logout();
        return false;
      }
    },

    /**
     * 取得當前登入使用者資料（GET /api/admin/user/me）
     */
    async fetchCurrentUser(): Promise<void> {
      const res = await authService.getCurrentUser();
      this.user = res.data;
      this.isAuthenticated = true;
    },

    setUser(user: AdminUser): void {
      this.user = user;
    },
  },

  getters: {
    isAdmin: (state): boolean =>
      state.user?.roles.includes('ROLE_ADMIN') ?? false,

    isStoreOwner: (state): boolean =>
      state.user?.roles.includes('ROLE_STORE_OWNER') ?? false,

    isStoreEditor: (state): boolean =>
      state.user?.roles.includes('ROLE_STORE_EDITOR') ?? false,

    /** 店家角色的主要店家 ID（後端 JWT 自動帶入） */
    currentStoreId: (state): string | undefined =>
      state.user?.storeId,

    currentStoreName: (state): string | undefined =>
      state.user?.storeName,

    /** 角色顯示名稱（UI 用） */
    roleDisplayName: (state): string => {
      if (state.user?.roles.includes('ROLE_ADMIN')) return '系統管理員';
      if (state.user?.roles.includes('ROLE_STORE_OWNER')) return '店家負責人';
      if (state.user?.roles.includes('ROLE_STORE_EDITOR')) return '店家編輯';
      return '未知角色';
    },
  },
});
```

### 9.2 Menu Store

```typescript
// stores/menu.ts
import { defineStore } from 'pinia';

export const useMenuStore = defineStore('menu', {
  state: (): MenuState => ({
    menus: [],
    loaded: false,
    loading: false,
  }),

  actions: {
    /**
     * 從後端取得動態選單（GET /api/admin/user/menu）
     * 已載入過則直接返回，不重複 fetch
     */
    async fetchMenus(): Promise<void> {
      if (this.loaded) return;
      this.loading = true;
      try {
        const res = await menuService.getMenus();
        this.menus = res.data;
        this.loaded = true;
      } finally {
        this.loading = false;
      }
    },

    clearMenus(): void {
      this.menus = [];
      this.loaded = false;
    },
  },

  getters: {
    /** 頂層選單（側欄主分類） */
    sidebarMenus: (state): MenuItem[] =>
      state.menus
        .filter((m) => !m.parentId)
        .sort((a, b) => a.sort - b.sort),

    /** 取得指定路徑的編輯權限 */
    canEdit: (state) => (menuPath: string): boolean => {
      const menu = state.menus.find((m) => m.path === menuPath);
      return menu?.permissions.canEdit ?? false;
    },

    /** 取得指定路徑的刪除權限 */
    canDelete: (state) => (menuPath: string): boolean => {
      const menu = state.menus.find((m) => m.path === menuPath);
      return menu?.permissions.canDelete ?? false;
    },

    /** 所有有 canView 權限的路徑（用於 router guard 動態產生白名單） */
    accessiblePaths: (state): string[] =>
      state.menus.filter((m) => m.permissions.canView).map((m) => m.path),
  },
});
```

### 9.3 UI Store

```typescript
// stores/ui.ts
import { defineStore } from 'pinia';

interface Toast {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  duration?: number;
}

interface UiState {
  sidebarCollapsed: boolean;
  globalLoading: boolean;
  toasts: Toast[];
}

export const useUiStore = defineStore('ui', {
  state: (): UiState => ({
    sidebarCollapsed: false,
    globalLoading: false,
    toasts: [],
  }),

  actions: {
    toggleSidebar(): void {
      this.sidebarCollapsed = !this.sidebarCollapsed;
    },

    setGlobalLoading(loading: boolean): void {
      this.globalLoading = loading;
    },

    showToast(toast: Omit<Toast, 'id'>): void {
      const id = Date.now().toString();
      this.toasts.push({ ...toast, id });
      setTimeout(() => this.removeToast(id), toast.duration ?? 3000);
    },

    removeToast(id: string): void {
      this.toasts = this.toasts.filter((t) => t.id !== id);
    },

    showSuccess(message: string): void {
      this.showToast({ type: 'success', message });
    },

    showError(message: string): void {
      this.showToast({ type: 'error', message, duration: 5000 });
    },
  },
});
```

---

## 10. Composables 規格

### 10.1 useQueryList

```typescript
// composables/useQueryList.ts
import { ref, reactive } from 'vue';
import http from '@/services/http';

interface UseQueryListOptions<C> {
  endpoint: string;
  defaultCondition?: Partial<C>;
  defaultSort?: { sortBy: string; sortOrder: 'ASC' | 'DESC' };
}

export function useQueryList<C extends object, T>(options: UseQueryListOptions<C>) {
  const items = ref<T[]>([]) as Ref<T[]>;
  const loading = ref(false);
  const condition = reactive<Partial<C>>({ ...options.defaultCondition });
  const sort = reactive(options.defaultSort ?? { sortBy: 'createdAt', sortOrder: 'DESC' as const });
  const error = ref<string | null>(null);

  async function query(): Promise<void> {
    loading.value = true;
    error.value = null;
    try {
      const res = await http.post<ApiResponse<T[]>>(options.endpoint, {
        condition,
        sortBy: sort.sortBy,
        sortOrder: sort.sortOrder,
      });
      items.value = res.data.data;
    } catch (e: unknown) {
      error.value = (e as Error).message;
      items.value = [];
    } finally {
      loading.value = false;
    }
  }

  function reset(): void {
    Object.assign(condition, options.defaultCondition ?? {});
    Object.keys(condition).forEach((key) => {
      if (!(key in (options.defaultCondition ?? {}))) {
        delete (condition as Record<string, unknown>)[key];
      }
    });
    query();
  }

  return { items, loading, condition, sort, error, query, reset };
}
```

### 10.2 usePagination

```typescript
// composables/usePagination.ts
import { ref, computed } from 'vue';

export function usePagination<T>(items: Ref<T[]>, defaultPageSize = 20) {
  const currentPage = ref(1);
  const pageSize = ref(defaultPageSize);

  const totalItems = computed(() => items.value.length);
  const totalPages = computed(() => Math.ceil(totalItems.value / pageSize.value));

  const pagedItems = computed<T[]>(() => {
    const start = (currentPage.value - 1) * pageSize.value;
    return items.value.slice(start, start + pageSize.value);
  });

  function goToPage(page: number): void {
    if (page < 1 || page > totalPages.value) return;
    currentPage.value = page;
  }

  function resetPage(): void {
    currentPage.value = 1;
  }

  // 當資料變更時，若目前頁超過總頁數則回到第一頁
  watch(items, () => {
    if (currentPage.value > totalPages.value) {
      currentPage.value = 1;
    }
  });

  return {
    currentPage,
    pageSize,
    totalItems,
    totalPages,
    pagedItems,
    goToPage,
    resetPage,
  };
}
```

### 10.3 useFileUpload

```typescript
// composables/useFileUpload.ts
import { ref } from 'vue';
import http from '@/services/http';

interface UploadResult {
  imageUrl: string;
  fileName: string;
}

interface UseFileUploadOptions {
  maxSizeMb?: number;
  acceptTypes?: string[];
}

export function useFileUpload(options: UseFileUploadOptions = {}) {
  const uploading = ref(false);
  const uploadError = ref<string | null>(null);
  const uploadedUrl = ref<string | null>(null);

  const maxSize = (options.maxSizeMb ?? 5) * 1024 * 1024;
  const acceptTypes = options.acceptTypes ?? ['image/jpeg', 'image/png', 'image/webp'];

  function validate(file: File): string | null {
    if (file.size > maxSize) return `檔案大小不得超過 ${options.maxSizeMb ?? 5}MB`;
    if (!acceptTypes.includes(file.type)) return `僅支援 ${acceptTypes.join(', ')} 格式`;
    return null;
  }

  async function upload(file: File): Promise<string | null> {
    const validationError = validate(file);
    if (validationError) {
      uploadError.value = validationError;
      return null;
    }

    uploading.value = true;
    uploadError.value = null;

    try {
      const formData = new FormData();
      formData.append('file', file);

      const res = await http.post<ApiResponse<UploadResult>>(
        '/api/admin/upload',  // TODO: 確認實際端點（AV-001）
        formData,
        { headers: { 'Content-Type': 'multipart/form-data' } }
      );

      uploadedUrl.value = res.data.data.imageUrl;
      return uploadedUrl.value;
    } catch {
      uploadError.value = '上傳失敗，請稍後再試';
      return null;
    } finally {
      uploading.value = false;
    }
  }

  function reset(): void {
    uploadedUrl.value = null;
    uploadError.value = null;
  }

  return { uploading, uploadError, uploadedUrl, upload, reset };
}
```

### 10.4 useDebounce

```typescript
// composables/useDebounce.ts
import { ref, watch } from 'vue';

export function useDebounce<T>(value: Ref<T>, delay = 300): Ref<T> {
  const debouncedValue = ref<T>(value.value) as Ref<T>;
  let timer: ReturnType<typeof setTimeout>;

  watch(value, (newValue) => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      debouncedValue.value = newValue;
    }, delay);
  });

  return debouncedValue;
}
```

---

## 11. 狀態常數對照表

### 11.1 商品狀態顯示對照

```typescript
// constants/lottery.ts

export const LOTTERY_STATUS_LABEL: Record<LotteryStatus, string> = {
  DRAFT: '草稿',
  CONFIGURED: '已設定獎品',
  ON_SHELF: '已上架',
  DRAWABLE: '可抽獎',
  RUNNING: '進行中',
  COMPLETED: '已完售',
  OFF_SHELF: '已下架',
};

export const LOTTERY_STATUS_COLOR: Record<LotteryStatus, string> = {
  DRAFT: 'gray',
  CONFIGURED: 'blue',
  ON_SHELF: 'green',
  DRAWABLE: 'green',
  RUNNING: 'orange',
  COMPLETED: 'purple',
  OFF_SHELF: 'red',
};

export const LOTTERY_CATEGORY_LABEL: Record<LotteryCategory, string> = {
  OFFICIAL_ICHIBAN: '官方一番賞',
  GACHA: '扭蛋',
  TRADING_CARD: '集換式卡牌',
  CUSTOM_GACHA: '自訂扭蛋',
};
```

### 11.2 訂單狀態顯示對照

```typescript
// constants/order.ts

export const ORDER_STATUS_LABEL: Record<OrderStatus, string> = {
  PENDING: '待處理',
  PREPARING: '準備中',
  SHIPPED: '已出貨',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
};

export const ORDER_STATUS_COLOR: Record<OrderStatus, string> = {
  PENDING: 'orange',
  PREPARING: 'blue',
  SHIPPED: 'cyan',
  COMPLETED: 'green',
  CANCELLED: 'red',
};

/** 允許的狀態轉移（前端 UI 決定可操作的按鈕） */
export const ORDER_STATUS_TRANSITIONS: Partial<Record<OrderStatus, OrderStatus[]>> = {
  PENDING: ['PREPARING', 'CANCELLED'],
  PREPARING: ['SHIPPED', 'CANCELLED'],
  SHIPPED: ['COMPLETED'],
};
```

### 11.3 獎品等級顯示對照

```typescript
// constants/prize.ts

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

## 12. Service 層規格

```typescript
// services/lottery.service.ts
export const lotteryService = {
  list: (req: ListReq<LotteryCondition>) =>
    http.post<ApiResponse<LotteryProduct[]>>('/api/admin/lottery/list', req),

  getById: (id: string) =>
    http.get<ApiResponse<LotteryProduct>>(`/api/admin/lottery/${id}`),

  create: (req: LotteryCreateReq) =>
    http.post<ApiResponse<LotteryProduct>>('/api/admin/lottery', req),

  update: (id: string, req: Partial<LotteryCreateReq>) =>
    http.put<ApiResponse<LotteryProduct>>(`/api/admin/lottery/${id}`, req),

  delete: (id: string) =>
    http.delete<ApiResponse<void>>(`/api/admin/lottery/${id}`),

  updateStatus: (id: string, status: LotteryStatus) =>
    http.patch<ApiResponse<LotteryProduct>>(`/api/admin/lottery/${id}/status`, { status }),

  getPrizes: (lotteryId: string) =>
    http.get<ApiResponse<Prize[]>>(`/api/admin/lottery/${lotteryId}/prizes`),

  createPrize: (lotteryId: string, req: PrizeCreateReq) =>
    http.post<ApiResponse<Prize>>(`/api/admin/lottery/${lotteryId}/prizes`, req),
};

// services/order.service.ts
export const orderService = {
  list: (req: ListReq<OrderCondition>) =>
    http.post<ApiResponse<Order[]>>('/api/admin/order/list', req),

  getById: (id: string) =>
    http.get<ApiResponse<OrderDetail>>(`/api/admin/order/${id}`),

  updateStatus: (id: string, req: OrderStatusUpdateReq) =>
    http.patch<ApiResponse<Order>>(`/api/admin/order/${id}/status`, req),
};
```

---

*本文件為前端資料模型定義，應與後端 DTO 保持同步更新。*
