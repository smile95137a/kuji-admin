# API Contract: Draw (抽獎流程)

> **Base URL**: `/api/lottery/draw/{lotteryId}`  
> **Auth**: 所有抽獎端點需登入（`Authorization: Bearer {token}`）  
> **Response 格式**: `{ code: number, message: string, data: T }`

---

## TypeScript 型別定義

```typescript
// types/draw.d.ts

export type TicketStatus =
  | 'AVAILABLE'   // 可抽取（未分配）
  | 'DRAWN'       // 已抽出
  | 'SCRATCHED';  // 已刮開（刮刮樂模式）

export interface Ticket {
  id: string;
  ticketNumber: string;     // 顯示用的票號（e.g., "001", "042"）
  status: TicketStatus;
  // ⚠️ 安全規則：AVAILABLE 的票券不包含 prizeId 和 prizeLevel
  // 只有 DRAWN/SCRATCHED 的票券才有以下欄位：
  prizeId?: string;         // 僅 DRAWN/SCRATCHED
  prizeLevel?: string;      // 僅 DRAWN/SCRATCHED
  prizeName?: string;       // 僅 DRAWN/SCRATCHED（冗餘顯示用）
  drawnAt?: string;         // 僅 DRAWN/SCRATCHED
  drawnByUserId?: string;   // 僅 DRAWN（誰抽的）
}

export interface DrawSession {
  lotteryId: string;
  userId: string;
  isOpener: boolean;                // 此用戶是否為刮刮樂的「開盒人」
  protectionEndTime: string | null; // ISO 8601；null = 無保護期
  grandPrizesDesignated: boolean;   // 大賞位置是否已指定（刮刮樂）
  myTicketCount: number;            // 此用戶已持有的票券數
  totalDrawnCount: number;          // 整體已抽出數量
  remainingCount: number;           // 剩餘可抽數量
}

export interface DrawResult {
  ticketId: string;
  ticketNumber: string;
  prizeId: string;
  prizeLevel: string;               // e.g. "A", "LAST", "BONUS"
  prizeName: string;
  prizeImageUrl?: string;
  isGrandPrize: boolean;
  remainingGold: number;            // 抽獎後用戶的金幣餘額
  remainingBonus: number;           // 抽獎後用戶的紅利點數餘額
  goldSpent: number;                // 本次抽獎消費的金幣
  bonusSpent: number;               // 本次抽獎消費的紅利點數
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}
```

---

## GET /api/lottery/draw/{lotteryId}/tickets

取得指定抽獎的票券列表與當前 Session 資訊。  
**必須在執行抽獎前呼叫**，以確認保護期、開獎人身份等狀態。

### Path Parameters

| 參數 | 說明 |
|------|------|
| lotteryId | 抽獎商品 ID |

### Response (200 OK)

```typescript
interface TicketsRes {
  tickets: Ticket[];    // 所有票券（AVAILABLE 的不含 prizeId）
  session: DrawSession;
}
```

**範例回傳：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "tickets": [
      {
        "id": "ticket-001",
        "ticketNumber": "001",
        "status": "AVAILABLE"
        // ✅ 無 prizeId, prizeLevel（安全！）
      },
      {
        "id": "ticket-042",
        "ticketNumber": "042",
        "status": "DRAWN",
        "prizeId": "prize-B-001",
        "prizeLevel": "B",
        "prizeName": "B賞 炭治郎吊飾",
        "drawnAt": "2024-06-01T10:30:00Z",
        "drawnByUserId": "user-999"
      }
    ],
    "session": {
      "lotteryId": "lottery-001",
      "userId": "user-123",
      "isOpener": false,
      "protectionEndTime": "2024-06-01T11:00:00Z",
      "grandPrizesDesignated": true,
      "myTicketCount": 3,
      "totalDrawnCount": 25,
      "remainingCount": 55
    }
  }
}
```

### ⚠️ 安全規則：票券可見性

| 票券狀態 | prizeId 可見？ | 說明 |
|---------|--------------|------|
| AVAILABLE | ❌ 不可見 | 防止前端「作弊」提前知道獎項 |
| DRAWN | ✅ 可見 | 已抽出的結果公開顯示 |
| SCRATCHED | ✅ 可見 | 已刮開的結果公開顯示 |

**嚴禁**在前端根據票號推測或暫存任何 prizeId 對應關係。

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 401 | 未登入 | 導向登入頁 |
| 404 | 商品不存在或未上架 | Toast：「此抽獎商品不存在或已下架」，返回上頁 |

---

## POST /api/lottery/draw/{lotteryId}/draw

執行抽獎。

### Request

```typescript
interface DrawReq {
  count: number;          // 抽獎次數（1 ~ 10，視商品設定）
  ticketIds?: string[];   // 可選：指定要抽的票券 ID（部分商品支援自選）
}
```

### Response (200 OK)

> ⚠️ **重要**：`data` 永遠是 `DrawResult[]` 陣列，**即使 count=1 也是 `[result]`**。

```typescript
type DrawRes = DrawResult[];
```

**範例（單抽 count=1）：**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "ticketId": "ticket-010",
      "ticketNumber": "010",
      "prizeId": "prize-C-003",
      "prizeLevel": "C",
      "prizeName": "C賞 壓克力立牌",
      "prizeImageUrl": "https://cdn.kuji.example.com/prize-C-003.jpg",
      "isGrandPrize": false,
      "remainingGold": 1800,
      "remainingBonus": 0,
      "goldSpent": 200,
      "bonusSpent": 0
    }
  ]
}
```

### 抽獎 Composable

```typescript
// src/composables/useDraw.ts
import { ref } from 'vue';
import http from '@/services/http';
import { useAuthStore } from '@/stores/auth';
import type { DrawResult, DrawSession, Ticket } from '@/types/draw';

export function useDraw(lotteryId: string) {
  const authStore = useAuthStore();
  const isDrawing = ref(false);
  const results = ref<DrawResult[]>([]);
  const tickets = ref<Ticket[]>([]);
  const session = ref<DrawSession | null>(null);

  async function loadTickets() {
    const res = await http.get(`/api/lottery/draw/${lotteryId}/tickets`);
    tickets.value = res.data.data.tickets;
    session.value = res.data.data.session;
  }

  async function draw(count: number) {
    isDrawing.value = true;
    try {
      const res = await http.post(`/api/lottery/draw/${lotteryId}/draw`, { count });

      // ✅ 永遠是陣列
      const drawResults: DrawResult[] = res.data.data;
      results.value = drawResults;

      // 更新錢包餘額（從第一筆結果取得）
      if (drawResults.length > 0) {
        authStore.updateBalance({
          goldCoins: drawResults[0].remainingGold,
          bonusCoins: drawResults[0].remainingBonus,
        });
      }

      // 重新載入票券狀態
      await loadTickets();

      return drawResults;
    } finally {
      isDrawing.value = false;
    }
  }

  return { isDrawing, results, tickets, session, loadTickets, draw };
}
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 400 | count 不合法（<=0 或超過上限） | Toast：「抽獎次數不合法」 |
| 402 | 餘額不足 | Toast：「您的金幣不足，請先儲值」+ 顯示「前往儲值」按鈕 |
| 403 | 已達個人抽獎上限 | Toast：「您已達到此商品的抽獎上限（{max}次）」 |
| 404 | 商品不存在或已下架 | Toast：「此抽獎已結束」，重新載入頁面 |
| 409 | 商品已完結（無剩餘票券） | Toast：「此抽獎已完結，所有獎品已被抽完」 |
| 423 | 處於保護期間（特定票券鎖定） | Toast：「目前處於保護期，請稍後再試」+ 顯示倒數計時 |

---

## POST /api/lottery/draw/{lotteryId}/designate-prizes

刮刮樂模式專用。開盒人（isOpener=true）在保護期內指定大賞位置。  
**只有 SCRATCH 類型且 `isOpener=true` 且 `grandPrizesDesignated=false` 時才可呼叫。**

### Request

```typescript
interface DesignatePrizesReq {
  prizePositions: Array<{
    prizeId: string;        // 大賞的 prizeId
    ticketNumber: string;   // 指定放置的票券號碼
  }>;
}
```

### Response (200 OK)

```typescript
interface DesignatePrizesRes {
  success: true;
  grandPrizesDesignated: true;
  protectionEndTime: string;  // 保護期開始計時
}
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 403 | 非開盒人 | Toast：「只有開盒人可以指定大賞位置」 |
| 409 | 已指定過 | Toast：「大賞位置已指定，無法重複設定」 |
| 400 | ticketNumber 不存在或已被抽出 | Toast：「所選票號無效，請重新選擇」 |

---

## 完整抽獎流程圖

```
Draw Flow（一般抽獎 NORMAL / ICHIBAN）
══════════════════════════════════════════

  進入抽獎頁
       │
       ▼
  GET .../tickets
  ┌─────────────────────────────────────┐
  │ 取得 tickets[] + session             │
  └─────────────────────────────────────┘
       │
       ├─ session.protectionEndTime ≠ null?
       │    └─ YES → 顯示倒數計時（useProtectionTimer）
       │             停用抽獎按鈕，等保護期結束
       │
       ├─ session.remainingCount === 0?
       │    └─ YES → 顯示「此抽獎已完結」
       │
       ▼
  用戶選擇抽獎次數（1 / 5 / 10）
       │
       ▼
  POST .../draw { count: N }
  ┌─────────────────────────────────────┐
  │ 顯示 Loading 動畫（轉盤/抽卡特效）   │
  └─────────────────────────────────────┘
       │
       ├─ 402 餘額不足 → Toast + 前往儲值
       ├─ 423 保護期 → Toast + 顯示倒數
       ├─ 其他錯誤 → Toast 錯誤訊息
       │
       ▼ 成功（200）
  results = response.data.data  // ← 永遠是陣列！
       │
       ▼
  遍歷 results[] 顯示結果卡片
  results[0].remainingGold/remainingBonus → 更新錢包
       │
       ▼
  顯示「再抽一次」/ 「前往賞品盒」按鈕


Draw Flow（刮刮樂 SCRATCH）
══════════════════════════════════════════

  進入抽獎頁
       │
       ▼
  GET .../tickets
       │
       ├─ isOpener === true?
       │    └─ !grandPrizesDesignated?
       │         └─ YES → 彈出「大賞指定」Modal
       │                  POST .../designate-prizes
       │                  → 保護期開始
       │
       ├─ session.protectionEndTime ≠ null → 顯示倒數
       │
       ▼
  用戶選擇/刮開票券
       │
       ▼
  POST .../draw { count: 1, ticketIds: [selectedId] }
       │
       ▼
  顯示刮刮樂動畫
  解析 results[0] → 顯示中獎結果
  更新錢包餘額
```

---

## 抽獎頁面完整範例

```vue
<!-- src/views/DrawPage.vue -->
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDraw } from '@/composables/useDraw';
import { useProtectionTimer } from '@/composables/useProtectionTimer';
import type { DrawResult } from '@/types/draw';

const route = useRoute();
const router = useRouter();
const lotteryId = route.params.id as string;

const { isDrawing, results, session, loadTickets, draw } = useDraw(lotteryId);

const protectionEndTime = computed(() => session.value?.protectionEndTime ?? null);
const { isLocked, displayTime } = useProtectionTimer(protectionEndTime);

const selectedCount = ref<1 | 5 | 10>(1);
const showResultModal = ref(false);

onMounted(loadTickets);

async function handleDraw() {
  if (isDrawing.value || isLocked.value) return;
  try {
    await draw(selectedCount.value);
    showResultModal.value = true;
  } catch (err: any) {
    const status = err.response?.status;
    if (status === 402) {
      // 顯示儲值提示
    }
  }
}
</script>

<template>
  <div class="draw-page">
    <!-- 保護期提示 -->
    <div v-if="isLocked" class="protection-banner">
      🔒 保護期：{{ displayTime }}
    </div>

    <!-- 抽獎次數選擇 -->
    <div class="draw-options">
      <button
        v-for="n in [1, 5, 10]"
        :key="n"
        :class="{ active: selectedCount === n }"
        @click="selectedCount = n as 1 | 5 | 10"
      >
        {{ n }} 抽
      </button>
    </div>

    <!-- 抽獎按鈕 -->
    <button
      class="draw-btn"
      :disabled="isDrawing || isLocked"
      @click="handleDraw"
    >
      <span v-if="isDrawing">抽獎中...</span>
      <span v-else-if="isLocked">保護期中</span>
      <span v-else>抽！（{{ selectedCount }} 抽）</span>
    </button>

    <!-- 結果 Modal -->
    <DrawResultModal
      v-if="showResultModal"
      :results="results"
      @close="showResultModal = false"
    />
  </div>
</template>
```

---

## 多抽結果展示

```typescript
// src/components/draw/DrawResultModal.vue (script)
const props = defineProps<{ results: DrawResult[] }>();

// 單抽：直接顯示大卡片
// 多抽：輪播或網格展示
const isSingleDraw = computed(() => props.results.length === 1);

// 是否有大賞
const hasGrandPrize = computed(() => props.results.some((r) => r.isGrandPrize));

// 消費摘要
const totalGoldSpent = computed(() =>
  props.results.reduce((sum, r) => sum + r.goldSpent, 0)
);
```

---

## 相關 Spec 文件

- `specs/cli/contracts/lottery-browse.md` — 抽獎商品瀏覽 API
- `specs/cli/quickstart.md` — Client App 快速開始指南（含保護期 Timer 實作）
- `specs/admin/contracts/products.md` — 後台商品管理 API
