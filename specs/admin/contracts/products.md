# API Contract: Admin Products (Lottery Management)

> **Base URL**: `/api/admin/lottery`  
> **Auth**: 所有端點需 `Authorization: Bearer {token}`  
> **storeId 處理**: 後端從 JWT 自動解析，前端**無需**手動帶入（除非是 SUPER_ADMIN 操作其他店家）

---

## TypeScript 型別定義

```typescript
// types/product.d.ts

export type LotteryStatus =
  | 'DRAFT'       // 草稿（可刪除）
  | 'ON_SHELF'    // 上架（販售中）
  | 'OFF_SHELF'   // 下架
  | 'RUNNING'     // 抽獎進行中
  | 'COMPLETED';  // 已完結

export type LotteryCategory =
  | 'NORMAL'          // 一般抽獎
  | 'ICHIBAN'         // 一番賞
  | 'SCRATCH'         // 刮刮樂
  | 'THANKSGIVING';   // 感謝賞

export type PrizeLevel =
  | 'LAST'    // 最終賞（唯一）
  | 'A'       // A 賞
  | 'B'       // B 賞
  | 'C'       // C 賞
  | 'D'       // D 賞
  | 'E'       // E 賞
  | 'F'       // F 賞
  | 'BONUS';  // 特典賞

export interface Prize {
  id: string;
  lotteryId: string;
  level: PrizeLevel;
  name: string;           // 獎品名稱
  description?: string;
  imageUrl?: string;
  quantity: number;       // 總數量
  remaining: number;      // 剩餘數量（唯讀，後端維護）
  isGrandPrize: boolean;  // 是否為大賞（刮刮樂模式）
  sortOrder: number;      // 排序
}

export interface LotteryProduct {
  id: string;
  storeId: string;
  title: string;
  description?: string;
  category: LotteryCategory;
  status: LotteryStatus;
  coverImageUrl?: string;
  goldPrice: number;      // 金幣售價（0 = 不接受金幣）
  bonusPrice: number;     // 紅利點數售價（0 = 不接受紅利）
  maxDrawsPerUser?: number; // null = 無限制
  totalTickets: number;   // 總票數（由 prizes 數量決定）
  drawnCount: number;     // 已抽出數量（唯讀）
  prizes: Prize[];
  createdAt: string;
  updatedAt: string;
  onShelfAt?: string;     // 上架時間
  completedAt?: string;   // 完結時間
}

// 通用 API Response 包裝
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}
```

---

## POST /api/admin/lottery/list

查詢抽獎商品列表。後端採 `POST /list` 模式以支援複雜查詢條件。

### Request

```typescript
interface LotteryCondition {
  storeId?: string;           // SUPER_ADMIN 可跨店查詢
  title?: string;             // 模糊查詢
  status?: LotteryStatus;
  category?: LotteryCategory;
  keyword?: string;           // 全域關鍵字（標題 + 描述）
  createdAtStart?: string;    // ISO 8601
  createdAtEnd?: string;
  goldPriceMin?: number;
  goldPriceMax?: number;
}

interface LotteryListReq {
  condition?: LotteryCondition;
  sortBy?: 'createdAt' | 'onShelfAt' | 'drawnCount' | 'title';
  sortOrder?: 'ASC' | 'DESC';
  // 分頁由前端處理，後端返回全量資料（或可選）
  page?: number;
  size?: number;
}
```

### Response (200 OK)

```typescript
interface LotteryListRes {
  items: LotteryProduct[];
  total: number;
}
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 401 | Token 無效 | Interceptor 自動處理 |
| 403 | 無查詢權限 | Toast：「您沒有查詢商品的權限」 |

### Frontend UI State

```typescript
// src/stores/product.ts（Pinia）
const state = reactive({
  items: [] as LotteryProduct[],
  total: 0,
  loading: false,
  condition: {} as LotteryCondition,
});

async function fetchList() {
  state.loading = true;
  try {
    const res = await productService.list({
      condition: state.condition,
      sortBy: 'createdAt',
      sortOrder: 'DESC',
    });
    state.items = res.data.data.items;
    state.total = res.data.data.total;
  } finally {
    state.loading = false;
  }
}
```

---

## POST /api/admin/lottery

建立新的抽獎商品。

### Request

```typescript
interface PrizeCreateReq {
  level: PrizeLevel;
  name: string;
  description?: string;
  imageUrl?: string;
  quantity: number;         // 必須 >= 1
  isGrandPrize?: boolean;   // 預設 false
  sortOrder?: number;       // 預設 0
}

interface LotteryCreateReq {
  title: string;              // 必填，最多 100 字
  description?: string;
  category: LotteryCategory;  // 必填
  coverImageUrl?: string;
  goldPrice: number;          // 必填，>= 0
  bonusPrice: number;         // 必填，>= 0
  maxDrawsPerUser?: number;   // null = 無限制
  prizes: PrizeCreateReq[];   // 至少 1 個獎品
  // storeId 由後端從 JWT 自動帶入，前端不送
}
```

> ⚠️ **AV-004: NEEDS BACKEND CONFIRMATION**  
> `prizes` 是否可在建立時一併送出？或需分兩步（先建 lottery，再 POST prizes）？

### Response (201 Created)

```typescript
// 回傳完整的 LotteryProduct（含 prizes）
type CreateLotteryRes = LotteryProduct;
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 400 | 欄位驗證失敗 | 顯示對應欄位的 validation message |
| 403 | 無建立權限 | Toast：「您沒有建立商品的權限」 |
| 409 | 標題重複（同店家） | Toast：「此商品標題已存在，請使用其他名稱」 |

### Frontend UI State

```typescript
async function createProduct(req: LotteryCreateReq) {
  creating.value = true;
  try {
    const res = await productService.create(req);
    toast.success('商品建立成功');
    router.push(`/products/${res.data.data.id}`);
  } catch (err: any) {
    if (err.response?.status === 409) {
      toast.error('此商品標題已存在，請使用其他名稱');
    } else {
      toast.error('建立失敗，請檢查輸入內容');
    }
  } finally {
    creating.value = false;
  }
}
```

---

## GET /api/admin/lottery/{id}

取得單一抽獎商品詳情（含所有獎品）。

### Response (200 OK)

```typescript
// 回傳完整 LotteryProduct，prizes 陣列包含所有獎品
type GetLotteryRes = LotteryProduct;
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 404 | 商品不存在或不屬於當前 store | Toast：「商品不存在」，導回列表頁 |
| 403 | 無查看權限 | Toast：「您沒有查看此商品的權限」 |

---

## PUT /api/admin/lottery/{id}

更新抽獎商品基本資訊。注意：`prizes` 的新增/修改/刪除使用專用端點。

### Request

```typescript
// 所有欄位皆為可選（Partial update）
interface LotteryUpdateReq {
  title?: string;
  description?: string;
  coverImageUrl?: string;
  goldPrice?: number;
  bonusPrice?: number;
  maxDrawsPerUser?: number | null;
}
```

> ⚠️ **AV-006: NEEDS BACKEND CONFIRMATION**  
> `category` 是否允許在 DRAFT 狀態外修改？若已有票券則不應變更。

### Response (200 OK)

```typescript
type UpdateLotteryRes = LotteryProduct; // 回傳更新後的完整物件
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 400 | 欄位驗證失敗 | 顯示 field validation 錯誤 |
| 403 | 無修改權限 | Toast：「您沒有修改此商品的權限」 |
| 404 | 商品不存在 | Toast：「商品不存在」 |
| 409 | 狀態不允許修改（非 DRAFT/OFF_SHELF） | Toast：「進行中或已完結的商品無法修改」 |

---

## DELETE /api/admin/lottery/{id}

刪除抽獎商品。**只允許刪除 DRAFT 狀態的商品。**

### Response (200 OK)

```typescript
interface DeleteRes {
  success: true;
}
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 403 | 無刪除權限 | Toast：「您沒有刪除此商品的權限」 |
| 404 | 商品不存在 | Toast：「商品不存在」 |
| 409 | 非 DRAFT 狀態 | Toast：「只有草稿狀態的商品可以刪除」 |

### Frontend Confirmation

```typescript
async function deleteProduct(id: string) {
  const confirmed = await dialog.confirm({
    title: '確認刪除',
    content: '此操作無法復原，確定要刪除此商品嗎？',
    confirmText: '刪除',
    confirmType: 'danger',
  });
  if (!confirmed) return;

  try {
    await productService.delete(id);
    toast.success('商品已刪除');
    fetchList(); // 重新整理列表
  } catch (err: any) {
    if (err.response?.status === 409) {
      toast.error('只有草稿狀態的商品可以刪除');
    }
  }
}
```

---

## PUT /api/admin/lottery/{id}/status

變更商品狀態（上架、下架等）。

### Request

```typescript
interface UpdateStatusReq {
  status: LotteryStatus;
}
```

**允許的狀態轉換：**

| 當前狀態 | 可轉換至 |
|---------|---------|
| DRAFT | ON_SHELF |
| ON_SHELF | OFF_SHELF |
| OFF_SHELF | ON_SHELF |
| RUNNING | 不可手動修改 |
| COMPLETED | 不可手動修改 |

### Response (200 OK)

```typescript
type UpdateStatusRes = LotteryProduct;
```

> ⚠️ **AV-007: NEEDS BACKEND CONFIRMATION**  
> 上架時是否需要驗證 `prizes.length > 0`？後端是否回傳詳細的驗證錯誤？

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 400 | 狀態轉換不合法 | Toast：根據 message 顯示說明 |
| 409 | 商品無獎品（嘗試上架） | Toast：「請先設定獎品再上架」 |

---

## POST /api/admin/lottery/{id}/prizes

新增獎品到指定商品。

### Request

```typescript
interface AddPrizesReq {
  prizes: PrizeCreateReq[];  // 同 LotteryCreateReq 中的 prizes
}
```

### Response (200 OK)

```typescript
interface AddPrizesRes {
  prizes: Prize[];  // 所有獎品（含新增的）
}
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 403 | 無修改權限 | Toast：「您沒有修改此商品的權限」 |
| 409 | 商品狀態不允許（RUNNING/COMPLETED） | Toast：「進行中或已完結的商品無法新增獎品」 |

---

## PUT /api/admin/lottery/{id}/prizes/{prizeId}

更新指定獎品資訊。

### Request

```typescript
// 所有欄位皆為可選（Partial update）
interface UpdatePrizeReq {
  name?: string;
  description?: string;
  imageUrl?: string;
  quantity?: number;
  isGrandPrize?: boolean;
  sortOrder?: number;
}
```

> ⚠️ 注意：`quantity` 只能增加（不能減少到低於 `remaining` 的值）。  
> 後端應驗證 `quantity >= (quantity - remaining)`（已抽出數量不得超過新總數）。

### Response (200 OK)

```typescript
type UpdatePrizeRes = Prize;
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 400 | quantity 低於已抽出數量 | Toast：「獎品數量不可低於已抽出數量（已抽 N 個）」 |
| 404 | 獎品不存在 | Toast：「獎品不存在」 |

---

## DELETE /api/admin/lottery/{id}/prizes/{prizeId}

刪除指定獎品。**只允許在商品非 RUNNING/COMPLETED 狀態時操作。**

### Response (200 OK)

```typescript
interface DeletePrizeRes {
  success: true;
}
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 409 | 商品正在進行中或已完結 | Toast：「進行中或已完結的商品無法刪除獎品」 |
| 409 | 獎品已有被抽到的記錄 | Toast：「此獎品已被抽出，無法刪除」 |

---

## 完整的 productService.ts

```typescript
// src/services/productService.ts
import http from './http';
import type {
  LotteryListReq,
  LotteryCreateReq,
  LotteryUpdateReq,
  UpdateStatusReq,
  AddPrizesReq,
  UpdatePrizeReq,
  LotteryProduct,
  LotteryListRes,
  Prize,
  ApiResponse,
} from '@/types/product';

export const productService = {
  list: (req: LotteryListReq) =>
    http.post<ApiResponse<LotteryListRes>>('/api/admin/lottery/list', req),

  getById: (id: string) =>
    http.get<ApiResponse<LotteryProduct>>(`/api/admin/lottery/${id}`),

  create: (body: LotteryCreateReq) =>
    http.post<ApiResponse<LotteryProduct>>('/api/admin/lottery', body),

  update: (id: string, body: LotteryUpdateReq) =>
    http.put<ApiResponse<LotteryProduct>>(`/api/admin/lottery/${id}`, body),

  updateStatus: (id: string, body: UpdateStatusReq) =>
    http.put<ApiResponse<LotteryProduct>>(`/api/admin/lottery/${id}/status`, body),

  delete: (id: string) =>
    http.delete<ApiResponse<{ success: true }>>(`/api/admin/lottery/${id}`),

  addPrizes: (id: string, body: AddPrizesReq) =>
    http.post<ApiResponse<{ prizes: Prize[] }>>(`/api/admin/lottery/${id}/prizes`, body),

  updatePrize: (id: string, prizeId: string, body: UpdatePrizeReq) =>
    http.put<ApiResponse<Prize>>(`/api/admin/lottery/${id}/prizes/${prizeId}`, body),

  deletePrize: (id: string, prizeId: string) =>
    http.delete<ApiResponse<{ success: true }>>(`/api/admin/lottery/${id}/prizes/${prizeId}`),
};
```

---

## 相關 Spec 文件

- `specs/admin/contracts/auth.md` — 認證 API
- `specs/admin/contracts/stores.md` — 店家管理（待補）
- `specs/cli/contracts/lottery-browse.md` — 前台瀏覽 API
- `specs/cli/contracts/draw.md` — 前台抽獎 API
