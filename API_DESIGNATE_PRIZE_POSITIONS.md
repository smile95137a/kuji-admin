# 🎯 指定大獎位置 API 文檔

## API 概覽

**端點**：`POST /api/lottery/draw/{lotteryId}/designate`

**用途**：刮刮樂模式，開套玩家指定哪些籤位號碼對應哪些大獎獎品

**認證**：需要 JWT Token（`Authorization: Bearer {token}`）

---

## 使用場景

### 什麼時候需要調用此 API？

1. **遊戲模式** = `SCRATCH_MODE` 或 `SCRATCH_CARD_MODE`
2. **當前玩家** = 開套者（`isOpener = true`）
3. **尚未指定大獎**（第一次抽獎前）

### 檢測方式

當玩家嘗試抽獎時，如果系統返回：

```json
{
  "success": true,
  "data": {
    "designationRequired": true,
    "message": "請先指定大獎位置（共需指定 5 個號碼）",
    "availableNumbers": [3, 7, 15, 22, 41, ...],
    "grandPrizes": [
      {
        "prizeId": "prize-uuid-A1",
        "prizeName": "iPhone 15 Pro",
        "prizeLevel": "A",
        "quantity": 3,
        "prizeImageUrl": "https://cdn.kuji.com/prizes/iphone15.jpg"
      },
      {
        "prizeId": "prize-uuid-B1",
        "prizeName": "MacBook Pro",
        "prizeLevel": "B",
        "quantity": 2,
        "prizeImageUrl": "https://cdn.kuji.com/prizes/macbook.jpg"
      }
    ]
  }
}
```

表示需要先調用此 API 指定大獎。

> ⚠️ **重要**：`availableNumbers` 內的數字是 **`revealedNumber`（刮開號碼）**，不是籤位格子的物理序號 `ticketNumber`。
> 
> `grandPrizes` 直接包含所有大獎資訊，前端**不需要再另外呼叫商品詳情 API** 來取得大獎清單。
> 
> 每個 `grandPrizes[].quantity` 代表該獎品需要指定幾個 revealedNumber，加總等於需要指定的總數。

---

## Request 請求格式

### 請求結構

```json
POST /api/lottery/draw/{lotteryId}/designate
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
  "designations": [
    {
      "revealedNumber": 7,
      "prizeId": "prize-uuid-A1"
    },
    {
      "revealedNumber": 33,
      "prizeId": "prize-uuid-A1"
    },
    {
      "revealedNumber": 88,
      "prizeId": "prize-uuid-A1"
    },
    {
      "revealedNumber": 12,
      "prizeId": "prize-uuid-B1"
    },
    {
      "revealedNumber": 45,
      "prizeId": "prize-uuid-B1"
    }
  ]
}
```

> ⚠️ **Breaking Change**：欄位名稱已從 `ticketNumber` 改為 **`revealedNumber`**。
> 這個數字來自 `designationRequired` 回應的 `availableNumbers` 列表，不是籤位格子的物理編號。

### 欄位說明

| 欄位 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `designations` | Array | ✅ | 大獎指定列表 |
| `designations[].revealedNumber` | Integer | ✅ | 刮開號碼（從 `availableNumbers` 列表中選取） |
| `designations[].prizeId` | String | ✅ | 獎品 ID（UUID 格式，必須是大獎） |

### 驗證規則

✅ `revealedNumber` 必須存在於可用籤位中（`status = AVAILABLE`）  
✅ `prizeId` 必須存在且屬於此商品  
✅ `prizeId` 對應的獎品必須是大獎（`is_grand_prize = 1`）  
✅ 每個 `revealedNumber` 不可重複  
✅ 同一個 `prizeId` 可以指定給多個 `revealedNumber`（如果該獎品 quantity > 1）

---

## Response 回應格式

### 成功回應

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "data": null,
  "error": null,
  "meta": {
    "timestamp": "2026-02-12T05:50:00Z",
    "requestId": "uuid-xxx"
  }
}
```

### 錯誤回應

#### 1. 未登入

```json
HTTP/1.1 401 Unauthorized

{
  "success": false,
  "message": "未授權",
  "error": {
    "code": "UNAUTHORIZED",
    "message": "請先登入"
  }
}
```

#### 2. revealedNumber 不存在或已被抽走

```json
HTTP/1.1 200 OK

{
  "success": false,
  "message": "revealed_number #7 不存在或已被抽走",
  "error": {
    "code": "BUSINESS_ERROR",
    "message": "revealed_number #7 不存在或已被抽走"
  }
}
```

#### 3. 獎品不是大獎

```json
{
  "success": false,
  "message": "獎品 娜美資料夾 不是大獎",
  "error": {
    "code": "BUSINESS_ERROR",
    "message": "獎品 娜美資料夾 不是大獎"
  }
}
```

#### 4. 獎品不屬於此商品

```json
{
  "success": false,
  "message": "獎品不屬於此商品",
  "error": {
    "code": "BUSINESS_ERROR"
  }
}
```

---

## 前端完整流程

> 🔄 **架構更新（2026-02-26）**：前端不再需要先呼叫商品詳情 API 取大獎列表。
> `designationRequired` 回應中的 `grandPrizes` 已包含所有需要的資訊，
> `availableNumbers` 是 `revealedNumber`（刮開號碼），不是物理格子號碼。

### 步驟 1：嘗試抽獎，收到 designationRequired

```javascript
const drawRes = await axios.post(`/api/lottery/draw/${lotteryId}/draw`, { count: 1 });
const data = drawRes.data.data;

if (data?.designationRequired) {
  // 需要先指定大獎，取出資訊
  const { availableNumbers, grandPrizes } = data;
  // availableNumbers: [3, 7, 15, 22, 41, ...]  ← 這些是 revealedNumber
  // grandPrizes: [{ prizeId, prizeName, prizeLevel, quantity, prizeImageUrl }, ...]
  
  // 計算總共需要指定幾個
  const totalRequired = grandPrizes.reduce((sum, p) => sum + p.quantity, 0);
  // → 進行選號流程
  await startDesignationFlow(availableNumbers, grandPrizes);
}
```

### 步驟 2：UI 讓玩家選 revealedNumber

```javascript
async function startDesignationFlow(availableNumbers, grandPrizes) {
  const selections = [];
  const usedNumbers = [];

  for (const prize of grandPrizes) {
    // 顯示提示：「請為 {prize.prizeName} 選擇 {prize.quantity} 個號碼」
    const selectedNumbers = await showNumberPicker({
      title: `請為 ${prize.prizeName} 選擇號碼`,
      count: prize.quantity,
      availableNumbers: availableNumbers.filter(n => !usedNumbers.includes(n)),  // 排除已選
    });
    
    for (const revealedNumber of selectedNumbers) {
      usedNumbers.push(revealedNumber);
      selections.push({
        revealedNumber,          // ← 注意是 revealedNumber
        prizeId: prize.prizeId
      });
    }
  }

  return selections;
}

// 最終 selections 範例：
// [
//   { revealedNumber: 7,  prizeId: "prize-uuid-A1" },  // iPhone #1
//   { revealedNumber: 33, prizeId: "prize-uuid-A1" },  // iPhone #2
//   { revealedNumber: 88, prizeId: "prize-uuid-A1" },  // iPhone #3
//   { revealedNumber: 12, prizeId: "prize-uuid-B1" },  // MacBook #1
//   { revealedNumber: 45, prizeId: "prize-uuid-B1" }   // MacBook #2
// ]
```

### 步驟 3：發送請求

```javascript
try {
  const response = await axios.post(
    `/api/lottery/draw/${lotteryId}/designate`,
    { designations: selections }   // selections[].revealedNumber + selections[].prizeId
  );
  
  if (response.data.success) {
    // 指定成功
    // ✅ 後端已自動將剩餘籤位隨機分配非大獎獎品
    alert('大獎位置已設定完成！現在可以開始抽獎了 🎉');
    
    // 跳轉到抽獎頁面
    router.push(`/lottery/${lotteryId}/draw`);
  }
} catch (error) {
  // 處理錯誤
  alert(error.response.data.message || '設定失敗');
}
```

---

## UI/UX 建議

### 推薦流程

1. **商品詳情頁**
   - 顯示「此商品需要開套者指定大獎位置」提示
   - 顯示按鈕：「開始選號」

2. **選號頁面**
   - 顯示 100 個格子（或根據 maxDraws）
   - 分批選號：
     - 第一步：「請為 iPhone 15 Pro (3個) 選擇 3 個號碼」
     - 第二步：「請為 MacBook Pro (2個) 選擇 2 個號碼」
   - 已選號碼標記為紅色
   - 顯示進度：「已選 3/5」

3. **確認頁面**
   - 顯示選擇結果：
     ```
     ✅ 7 號 → iPhone 15 Pro
     ✅ 33 號 → iPhone 15 Pro
     ✅ 88 號 → iPhone 15 Pro
     ✅ 12 號 → MacBook Pro
     ✅ 45 號 → MacBook Pro
     ```
   - 按鈕：「確認設定」

4. **完成提示**
   - 「大獎位置已設定！」
   - 「**注意**：其他玩家不知道大獎位置」
   - 按鈕：「開始抽獎」

### UI 元件範例

```vue
<template>
  <div class="prize-designation">
    <!-- 當前正在選擇的獎品 -->
    <div class="current-prize">
      <img :src="currentPrize.imageUrl" />
      <h3>{{ currentPrize.name }}</h3>
      <p>請選擇 {{ currentPrize.quantity }} 個號碼</p>
      <div class="progress">
        已選 {{ selectedCount }} / {{ currentPrize.quantity }}
      </div>
    </div>
    
    <!-- 籤位格子 -->
    <div class="ticket-grid">
      <div 
        v-for="num in maxDraws" 
        :key="num"
        :class="{
          'ticket': true,
          'selected': isSelected(num),
          'disabled': isUsed(num)
        }"
        @click="toggleSelect(num)"
      >
        {{ num }}
      </div>
    </div>
    
    <!-- 操作按鈕 -->
    <div class="actions">
      <button @click="goBack" v-if="!isFirstPrize">
        上一步
      </button>
      <button 
        @click="nextStep" 
        :disabled="selectedCount < currentPrize.quantity"
      >
        {{ isLastPrize ? '確認設定' : '下一步' }}
      </button>
    </div>
  </div>
</template>
```

---

## 完整範例代碼

### JavaScript (Axios)

```javascript
/**
 * 指定大獎位置
 * 
 * @param {string} lotteryId - 商品 ID
 * @param {Array} designations - 指定列表（每項含 revealedNumber + prizeId）
 * @returns {Promise<void>}
 */
async function designatePrizePositions(lotteryId, designations) {
  try {
    const response = await axios.post(
      `/api/lottery/draw/${lotteryId}/designate`,
      { designations },
      {
        headers: {
          'Authorization': `Bearer ${getToken()}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    return response.data;
  } catch (error) {
    console.error('指定大獎失敗:', error);
    throw error;
  }
}

// 使用範例
const result = await designatePrizePositions('lottery-uuid-001', [
  { revealedNumber: 7,  prizeId: 'prize-A1' },   // ← revealedNumber，不是 ticketNumber
  { revealedNumber: 33, prizeId: 'prize-A1' },
  { revealedNumber: 88, prizeId: 'prize-B1' }
]);

console.log('設定成功！');
```

### TypeScript

```typescript
// DesignationRequired 回應結構（抽獎被攔截時）
interface GrandPrizeInfo {
  prizeId: string;
  prizeName: string;
  prizeLevel: string;
  quantity: number;       // 此獎品需指定幾個 revealedNumber
  prizeImageUrl: string | null;
}

interface DesignationRequiredResponse {
  designationRequired: true;
  message: string;
  availableNumbers: number[];   // revealedNumber 列表（不是 ticketNumber）
  grandPrizes: GrandPrizeInfo[];
}

interface PrizeDesignation {
  revealedNumber: number;   // ⚠️ Breaking Change：原為 ticketNumber
  prizeId: string;
}

interface DesignateRequest {
  designations: PrizeDesignation[];
}

interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: {
    code: string;
    message: string;
  } | null;
  meta: {
    timestamp: string;
    requestId: string;
  };
}

async function designatePrizePositions(
  lotteryId: string,
  designations: PrizeDesignation[]
): Promise<ApiResponse<null>> {
  const response = await axios.post<ApiResponse<null>>(
    `/api/lottery/draw/${lotteryId}/designate`,
    { designations }
  );
  
  return response.data;
}
```

---

## 注意事項

### ⚠️ 重要提醒

1. **只能指定一次**
   - 開套者只能在第一次抽獎前指定
   - 指定後無法修改

2. **大獎定義**
   - 只有標記為 `is_grand_prize = 1` 的獎品才能指定
   - 非大獎獎品由後端在指定完成後**自動隨機分配**，前端不需要也不能操作

3a. **revealedNumber vs ticketNumber**
   - `revealedNumber`：刮開後顯示的隨機號碼（用於大獎指定）
   - `ticketNumber`：籤位物理格子序號（1~N，用於玩家選格）
   - 指定大獎時傳 `revealedNumber`，選格子抽獎時傳 `ticketNumber`

3. **數量匹配**
   - 如果 iPhone 有 3 個，必須為它選擇 3 個號碼
   - 不能多選也不能少選

4. **安全性**
   - 其他玩家看不到大獎位置
   - 前台 API 不會洩露未抽籤位的獎品資訊

5. **籤位狀態**
   - 只能選擇 `status = AVAILABLE` 的籤位
   - 已抽走的籤位無法指定

---

## 測試範例

### cURL 測試

```bash
curl -X POST 'http://localhost:8080/api/lottery/draw/lottery-uuid-001/designate' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiIs...' \
  -H 'Content-Type: application/json' \
  -d '{
    "designations": [
      {
        "revealedNumber": 7,
        "prizeId": "prize-uuid-A1"
      },
      {
        "revealedNumber": 33,
        "prizeId": "prize-uuid-A1"
      },
      {
        "revealedNumber": 88,
        "prizeId": "prize-uuid-B1"
      }
    ]
  }'
```

### Postman 測試

1. **Method**: POST
2. **URL**: `http://localhost:8080/api/lottery/draw/{lotteryId}/designate`
3. **Headers**:
   - `Authorization`: `Bearer {token}`
   - `Content-Type`: `application/json`
4. **Body** (raw JSON):
```json
{
  "designations": [
    {
      "revealedNumber": 7,
      "prizeId": "638aa8ce-075c-11f1-bab7-0a7ddf3d3fc1"
    }
  ]
}
```

---

## 資料庫變更

### lottery_ticket 表

指定後，系統會進行兩種更新：

**大獎籤位**（依 revealedNumber 找到對應籤位）：
```sql
UPDATE lottery_ticket
SET 
  prize_id = 'prize-uuid-A1',        -- 設定為指定的大獎
  prize_level = 'A',
  is_designated_prize = 1,
  designated_by = 'PLAYER',
  updated_at = NOW()
WHERE 
  lottery_id = 'lottery-uuid-001'
  AND revealed_number = 7              -- ← 用 revealed_number 定位，不是 ticket_number
  AND status = 'AVAILABLE';
```

**非大獎籤位（自動分配，無需前端操作）**：
- 指定完成後，後端自動將剩餘非大獎獎品隨機分配到未指定的 AVAILABLE 籤位
- 分配超出數量的籤位維持 `prize_id = null`（謝謝惠顧）
```

### lottery_session 表

Session 會記錄已指定的號碼：

```sql
UPDATE lottery_session
SET 
  player_designated_numbers = '[7,33,88]',  -- JSON 格式
  updated_at = NOW()
WHERE id = 'session-uuid-001';
```

---

## FAQ 常見問題

### Q1: 可以修改已指定的大獎位置嗎？
**A**: 不行，一旦指定完成就無法修改。建議在確認前提供「預覽」功能。

### Q2: 如果商品沒有大獎怎麼辦？
**A**: 系統會返回錯誤「商品未設定大獎獎品」。

### Q3: 可以只指定部分大獎嗎？
**A**: 可以，但建議指定所有大獎以確保遊戲體驗完整。

### Q4: 其他玩家能看到大獎位置嗎？
**A**: 不能，前台 API 會隱藏未抽籤位的獎品資訊。

### Q5: 指定後多久可以開始抽獎？
**A**: 立即可以開始抽獎。

---

## 相關 API

- [GET /api/lottery/{id}](./API_DOCUMENTATION_COMPLETE.md#查詢單一商品) - 取得商品詳情（含獎品列表）
- [GET /api/lottery/draw/{id}/tickets](./API_DOCUMENTATION_COMPLETE.md#查詢籤位列表) - 取得籤位列表
- [POST /api/lottery/draw/{id}/draw](./API_DOCUMENTATION_COMPLETE.md#執行抽獎) - 執行抽獎
- [GET /api/lottery/draw/{id}/session](./API_DOCUMENTATION_COMPLETE.md#查詢場次) - 取得場次資訊

---

**最後更新**：2026-02-26  
**版本**：v3.0  
**Breaking Changes**：
- `designations[].ticketNumber` → `designations[].revealedNumber`
- `designationRequired` 回應新增 `grandPrizes` 欄位（前端不再需要另外查詢商品詳情取大獎列表）
- `availableNumbers` 現在是 `revealedNumber` 列表（不是 1~N 的物理格子號碼）
- 指定完成後非大獎自動隨機分配（後端處理，前端無需操作）
