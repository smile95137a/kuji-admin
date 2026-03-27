# API Contract: Lottery Browse (Client)

> **Base URL**: `/api`  
> **Auth**: 所有端點皆為**公開（Public）**，無需 `Authorization` Header  
> **Response 格式**: `{ code: number, message: string, data: T }`

---

## TypeScript 型別定義

```typescript
// types/store.d.ts

export interface Store {
  id: string;
  name: string;
  description?: string;
  logoUrl?: string;
  bannerUrl?: string;
  address?: string;
  contactEmail?: string;
  contactPhone?: string;
  status: 'ACTIVE' | 'INACTIVE';
  productCount?: number;    // 上架中的商品數量
  createdAt: string;
}

// types/lottery.d.ts

export type LotteryStatus =
  | 'ON_SHELF'    // 販售中（前台可見）
  | 'OFF_SHELF'   // 下架
  | 'RUNNING'     // 抽獎進行中（前台可見）
  | 'COMPLETED';  // 已完結

export type LotteryCategory =
  | 'NORMAL'
  | 'ICHIBAN'
  | 'SCRATCH'
  | 'THANKSGIVING';

export type PrizeLevel = 'LAST' | 'A' | 'B' | 'C' | 'D' | 'E' | 'F' | 'BONUS';

export interface PrizePublic {
  id: string;
  level: PrizeLevel;
  name: string;
  description?: string;
  imageUrl?: string;
  quantity: number;
  remaining: number;
  isGrandPrize: boolean;
  sortOrder: number;
  // ⚠️ 安全規則：AVAILABLE 狀態的票券不會包含 prizeId（見 draw.md）
}

export interface LotteryListItem {
  id: string;
  storeId: string;
  storeName: string;        // 冗餘欄位，避免額外查詢
  title: string;
  description?: string;
  category: LotteryCategory;
  status: LotteryStatus;
  coverImageUrl?: string;
  goldPrice: number;
  bonusPrice: number;
  totalTickets: number;
  drawnCount: number;
  remainingCount: number;   // totalTickets - drawnCount（後端計算）
  progressPercent: number;  // 0~100（後端計算）
  onShelfAt?: string;
}

export interface LotteryDetail extends LotteryListItem {
  maxDrawsPerUser?: number;
  prizes: PrizePublic[];
  completedAt?: string;
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}
```

---

## GET /api/store/list

取得所有店家列表。**公開端點，無需登入。**

### Query Parameters

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| status | string | 否 | 篩選狀態（預設只返回 ACTIVE） |
| keyword | string | 否 | 店家名稱模糊查詢 |

### Response (200 OK)

```typescript
interface StoreListRes {
  stores: Store[];
  total: number;
}
```

**範例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "stores": [
      {
        "id": "store-001",
        "name": "夢幻一番賞",
        "description": "專業一番賞抽獎平台",
        "logoUrl": "https://cdn.kuji.example.com/stores/store-001/logo.jpg",
        "status": "ACTIVE",
        "productCount": 12
      }
    ],
    "total": 1
  }
}
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 500 | 伺服器錯誤 | 顯示空列表 + 「無法載入店家資訊，請稍後再試」 |

### Frontend UI State

```typescript
// src/composables/useStoreList.ts
import { ref, onMounted } from 'vue';
import http from '@/services/http';
import type { Store } from '@/types/store';

export function useStoreList() {
  const stores = ref<Store[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  async function fetchStores(keyword?: string) {
    loading.value = true;
    error.value = null;
    try {
      const res = await http.get('/api/store/list', {
        params: { keyword, status: 'ACTIVE' },
      });
      stores.value = res.data.data.stores;
    } catch {
      error.value = '無法載入店家資訊，請稍後再試';
      stores.value = [];
    } finally {
      loading.value = false;
    }
  }

  onMounted(() => fetchStores());

  return { stores, loading, error, fetchStores };
}
```

---

## GET /api/store/{id}

取得指定店家的詳情，包含上架中的商品列表。**公開端點。**

### Path Parameters

| 參數 | 說明 |
|------|------|
| id | 店家 ID |

### Response (200 OK)

```typescript
interface StoreDetailRes extends Store {
  products: LotteryListItem[];  // 只包含 ON_SHELF / RUNNING 的商品
}
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 404 | 店家不存在 | 跳轉到 `/` 並顯示 Toast：「店家不存在或已停用」 |

### 前端使用範例

```vue
<!-- src/views/StorePage.vue -->
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import http from '@/services/http';
import type { StoreDetailRes } from '@/types/store';

const route = useRoute();
const router = useRouter();
const store = ref<StoreDetailRes | null>(null);
const loading = ref(true);

onMounted(async () => {
  try {
    const res = await http.get(`/api/store/${route.params.id}`);
    store.value = res.data.data;
  } catch (err: any) {
    if (err.response?.status === 404) {
      router.push('/');
    }
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div v-if="loading">載入中...</div>
  <div v-else-if="store">
    <h1>{{ store.name }}</h1>
    <img :src="store.bannerUrl" alt="store banner" />
    <div class="product-grid">
      <LotteryCard v-for="p in store.products" :key="p.id" :product="p" />
    </div>
  </div>
</template>
```

---

## GET /api/lottery/list

取得抽獎商品列表（多店家混合）。**公開端點。**  
支援篩選、搜尋，適合首頁「全部商品」或搜尋頁面使用。

### Query Parameters

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| storeId | string | 否 | 限制特定店家 |
| category | string | 否 | 商品類型篩選 |
| status | string | 否 | 預設返回 ON_SHELF + RUNNING |
| keyword | string | 否 | 標題模糊搜尋 |
| sortBy | string | 否 | `createdAt` / `progressPercent` / `goldPrice` |
| sortOrder | string | 否 | `ASC` / `DESC`（預設 DESC） |
| page | number | 否 | 頁碼（預設 1） |
| size | number | 否 | 每頁筆數（預設 20，最大 100） |

### Response (200 OK)

```typescript
interface LotteryListRes {
  items: LotteryListItem[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}
```

### 前端篩選元件

```typescript
// src/composables/useLotteryList.ts
import { ref, reactive, computed } from 'vue';
import http from '@/services/http';
import type { LotteryListItem, LotteryCategory } from '@/types/lottery';

interface Filters {
  storeId?: string;
  category?: LotteryCategory;
  keyword?: string;
  sortBy: string;
  sortOrder: 'ASC' | 'DESC';
}

export function useLotteryList() {
  const items = ref<LotteryListItem[]>([]);
  const total = ref(0);
  const loading = ref(false);
  const page = ref(1);
  const size = ref(20);

  const filters = reactive<Filters>({
    sortBy: 'createdAt',
    sortOrder: 'DESC',
  });

  async function fetchList() {
    loading.value = true;
    try {
      const res = await http.get('/api/lottery/list', {
        params: {
          ...filters,
          page: page.value,
          size: size.value,
        },
      });
      items.value = res.data.data.items;
      total.value = res.data.data.total;
    } finally {
      loading.value = false;
    }
  }

  // 進度條計算（後端也會計算，此為 fallback）
  const enrichedItems = computed(() =>
    items.value.map((item) => ({
      ...item,
      progressPercent:
        item.progressPercent ??
        Math.round((item.drawnCount / item.totalTickets) * 100),
    }))
  );

  return { enrichedItems, total, loading, page, size, filters, fetchList };
}
```

---

## GET /api/lottery/{id}

取得抽獎商品詳情，包含獎品列表。**公開端點。**  
此端點用於商品詳情頁面，顯示商品介紹和獎品構成。

### Path Parameters

| 參數 | 說明 |
|------|------|
| id | 抽獎商品 ID |

### Response (200 OK)

```typescript
type GetLotteryDetailRes = LotteryDetail;
```

**範例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "lottery-001",
    "storeId": "store-001",
    "storeName": "夢幻一番賞",
    "title": "鬼滅之刃 A賞 一番賞",
    "category": "ICHIBAN",
    "status": "ON_SHELF",
    "coverImageUrl": "https://cdn.kuji.example.com/lottery-001/cover.jpg",
    "goldPrice": 200,
    "bonusPrice": 0,
    "totalTickets": 80,
    "drawnCount": 25,
    "remainingCount": 55,
    "progressPercent": 31,
    "prizes": [
      {
        "id": "prize-001",
        "level": "LAST",
        "name": "炭治郎 特大布偶",
        "quantity": 1,
        "remaining": 1,
        "isGrandPrize": false,
        "sortOrder": 0
      },
      {
        "id": "prize-002",
        "level": "A",
        "name": "善逸 壓克力立牌",
        "quantity": 4,
        "remaining": 3,
        "isGrandPrize": false,
        "sortOrder": 1
      }
    ]
  }
}
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 404 | 商品不存在、未上架、或不對外公開 | 跳轉到 `/` 並顯示 Toast：「此商品不存在或已下架」 |

### 完成度進度條

```vue
<!-- src/components/lottery/ProgressBar.vue -->
<template>
  <div class="progress-bar">
    <div class="progress-bar__track">
      <div
        class="progress-bar__fill"
        :style="{ width: `${progressPercent}%` }"
        :class="{ 'progress-bar__fill--complete': progressPercent >= 100 }"
      />
    </div>
    <span class="progress-bar__label">
      {{ drawnCount }} / {{ totalTickets }}（剩餘 {{ remainingCount }}）
    </span>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  drawnCount: number;
  totalTickets: number;
  remainingCount: number;
  progressPercent: number;
}>();
</script>
```

---

## 公開 Service 整合

```typescript
// src/services/storeService.ts
import http from './http';
import type { Store, StoreDetailRes, StoreListRes, ApiResponse } from '@/types/store';

export const storeService = {
  list: (params?: { keyword?: string; status?: string }) =>
    http.get<ApiResponse<StoreListRes>>('/api/store/list', { params }),

  getById: (id: string) =>
    http.get<ApiResponse<StoreDetailRes>>(`/api/store/${id}`),
};

// src/services/lotteryService.ts
import http from './http';
import type {
  LotteryListRes,
  LotteryDetail,
  LotteryCategory,
  ApiResponse,
} from '@/types/lottery';

export const lotteryService = {
  list: (params?: {
    storeId?: string;
    category?: LotteryCategory;
    keyword?: string;
    sortBy?: string;
    sortOrder?: string;
    page?: number;
    size?: number;
  }) => http.get<ApiResponse<LotteryListRes>>('/api/lottery/list', { params }),

  getById: (id: string) =>
    http.get<ApiResponse<LotteryDetail>>(`/api/lottery/${id}`),
};
```

---

## 相關 Spec 文件

- `specs/cli/contracts/draw.md` — 抽獎執行 API
- `specs/cli/quickstart.md` — Client App 快速開始指南
- `specs/admin/contracts/products.md` — 後台商品管理 API
