# 新增謝謝惠顧數量顯示

**日期**：2026-01-30  
**版本**：1.1.2  
**優先級**：🟡 HIGH（資訊完整性）

---

## 📋 問題說明

### 原始問題

當建立刮刮樂商品時：
- 設定 `maxDraws = 100`（總共 100 抽）
- 獎品總數 = 28 個（A賞 1 + B賞 2 + C賞 4 + D賞 8 + E賞 12 + LAST賞 1）
- 謝謝惠顧 = 72 個（100 - 28）

**但回應中看不到謝謝惠顧的數量**：

```json
{
  "maxDraws": 100,           // ✅ 總籤位數
  "totalPrizeCount": 28,     // ✅ 中獎籤位數
  "remainingPrizeCount": 28, // ✅ 剩餘中獎籤位
  // ❌ 缺少：謝謝惠顧數量！
}
```

### 使用者困惑

使用者無法從回應中得知：
1. ❓ 有沒有謝謝惠顧？
2. ❓ 謝謝惠顧有幾個？
3. ❓ 中獎率是多少？（28/100 = 28%）

---

## 🔧 解決方案

### 修改內容

1. **新增欄位**：`LotteryWithPrizesRes.thanksgivingCount`
2. **計算邏輯**：`thanksgivingCount = maxDraws - totalPrizeCount`
3. **回應增強**：明確顯示謝謝惠顧數量

### 修改檔案

#### 1. `LotteryWithPrizesRes.java`

```java
// ==================== 統計資訊 ====================

@Schema(description = "獎品總數量（中獎籤位數）", example = "28")
private Integer totalPrizeCount;

@Schema(description = "剩餘獎品數量", example = "18")
private Integer remainingPrizeCount;

@Schema(description = "謝謝惠顧數量（maxDraws - totalPrizeCount）", example = "72")
private Integer thanksgivingCount;  // ← 新增欄位

@Schema(description = "抽獎進度百分比", example = "30.77")
private Double progressPercentage;
```

#### 2. `LotteryServiceImpl.java` - `buildLotteryWithPrizesRes()`

```java
// 計算統計資訊
int totalPrizeCount = prizeResList.stream()
        .mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0)
        .sum();

int remainingPrizeCount = prizeResList.stream()
        .mapToInt(p -> p.getRemaining() != null ? p.getRemaining() : 0)
        .sum();

// 🆕 計算謝謝惠顧數量（maxDraws - 獎品總數）
int maxDraws = lotteryRes.getMaxDraws() != null ? lotteryRes.getMaxDraws() : 0;
int thanksgivingCount = Math.max(0, maxDraws - totalPrizeCount);

// ... 建立回應
return LotteryWithPrizesRes.builder()
        // ... 其他欄位
        .totalPrizeCount(totalPrizeCount)
        .remainingPrizeCount(remainingPrizeCount)
        .thanksgivingCount(thanksgivingCount)  // ← 設定謝謝惠顧數量
        .progressPercentage(progressPercentage)
        .build();
```

---

## 📊 API 回應範例

### Before（舊版）

```json
{
  "success": true,
  "data": {
    "id": "lottery-uuid",
    "title": "咒術迴戰 一番賞 926",
    "playMode": "SCRATCH_MODE",
    "maxDraws": 100,
    "totalDraws": 0,
    "remainingDraws": 100,
    "prizes": [...],
    "totalPrizeCount": 28,
    "remainingPrizeCount": 28,
    "progressPercentage": 0.0
    // ❌ 看不出來有 72 個謝謝惠顧
  }
}
```

### After（新版）

```json
{
  "success": true,
  "data": {
    "id": "lottery-uuid",
    "title": "咒術迴戰 一番賞 926",
    "playMode": "SCRATCH_MODE",
    "maxDraws": 100,
    "totalDraws": 0,
    "remainingDraws": 100,
    "prizes": [...],
    "totalPrizeCount": 28,
    "remainingPrizeCount": 28,
    "thanksgivingCount": 72,  // ✅ 清楚顯示謝謝惠顧數量
    "progressPercentage": 0.0
  }
}
```

---

## 🎮 使用場景

### 一番賞模式（LOTTERY_MODE）

```json
{
  "playMode": "LOTTERY_MODE",
  "maxDraws": 28,             // 總籤位數
  "totalPrizeCount": 28,      // 獎品數量
  "thanksgivingCount": 0      // ✅ 謝謝惠顧 = 0（一番賞不能有謝謝惠顧）
}
```

**中獎率**：28/28 = **100%**（每抽必中）

---

### 刮刮樂模式（SCRATCH_MODE）

#### 案例 1：高中獎率

```json
{
  "playMode": "SCRATCH_MODE",
  "maxDraws": 50,             // 總籤位數
  "totalPrizeCount": 30,      // 獎品數量
  "thanksgivingCount": 20     // ✅ 謝謝惠顧 = 20
}
```

**中獎率**：30/50 = **60%**

---

#### 案例 2：低中獎率

```json
{
  "playMode": "SCRATCH_MODE",
  "maxDraws": 100,            // 總籤位數
  "totalPrizeCount": 10,      // 獎品數量
  "thanksgivingCount": 90     // ✅ 謝謝惠顧 = 90
}
```

**中獎率**：10/100 = **10%**

---

#### 案例 3：無謝謝惠顧

```json
{
  "playMode": "SCRATCH_MODE",
  "maxDraws": 28,             // 總籤位數
  "totalPrizeCount": 28,      // 獎品數量
  "thanksgivingCount": 0      // ✅ 謝謝惠顧 = 0（刮刮樂也可以沒有謝謝惠顧）
}
```

**中獎率**：28/28 = **100%**

---

## 📱 前端顯示建議

### 商品列表/詳情頁

```vue
<template>
  <div class="lottery-stats">
    <!-- 總籤位數 -->
    <div class="stat-item">
      <span class="label">總籤位數</span>
      <span class="value">{{ lottery.maxDraws }}</span>
    </div>
    
    <!-- 中獎籤位 -->
    <div class="stat-item">
      <span class="label">中獎籤位</span>
      <span class="value prize">{{ lottery.totalPrizeCount }}</span>
    </div>
    
    <!-- 謝謝惠顧 -->
    <div class="stat-item" v-if="lottery.thanksgivingCount > 0">
      <span class="label">謝謝惠顧</span>
      <span class="value thanks">{{ lottery.thanksgivingCount }}</span>
    </div>
    
    <!-- 中獎率 -->
    <div class="stat-item">
      <span class="label">中獎率</span>
      <span class="value rate">{{ winRate }}%</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  lottery: Object
});

// 計算中獎率
const winRate = computed(() => {
  if (props.lottery.maxDraws === 0) return 0;
  const rate = (props.lottery.totalPrizeCount / props.lottery.maxDraws) * 100;
  return Math.round(rate * 100) / 100;
});
</script>

<style scoped>
.lottery-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-item {
  text-align: center;
}

.label {
  font-size: 14px;
  color: #666;
}

.value {
  font-size: 24px;
  font-weight: bold;
}

.value.prize {
  color: #f59e0b;  /* 橘色 - 獎品 */
}

.value.thanks {
  color: #6b7280;  /* 灰色 - 謝謝惠顧 */
}

.value.rate {
  color: #10b981;  /* 綠色 - 中獎率 */
}
</style>
```

### 視覺化籤位分佈

```vue
<template>
  <div class="ticket-distribution">
    <h3>籤位分佈</h3>
    
    <!-- 進度條 -->
    <div class="progress-bar">
      <div 
        class="prize-bar" 
        :style="{ width: prizePercentage + '%' }"
      >
        中獎 {{ lottery.totalPrizeCount }}
      </div>
      <div 
        class="thanks-bar" 
        :style="{ width: thanksPercentage + '%' }"
        v-if="lottery.thanksgivingCount > 0"
      >
        謝謝惠顧 {{ lottery.thanksgivingCount }}
      </div>
    </div>
    
    <!-- 百分比 -->
    <div class="percentage-labels">
      <span>中獎 {{ prizePercentage }}%</span>
      <span v-if="lottery.thanksgivingCount > 0">
        謝謝惠顧 {{ thanksPercentage }}%
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  lottery: Object
});

const prizePercentage = computed(() => {
  if (props.lottery.maxDraws === 0) return 0;
  return Math.round((props.lottery.totalPrizeCount / props.lottery.maxDraws) * 100);
});

const thanksPercentage = computed(() => {
  if (props.lottery.maxDraws === 0) return 0;
  return Math.round((props.lottery.thanksgivingCount / props.lottery.maxDraws) * 100);
});
</script>

<style scoped>
.progress-bar {
  display: flex;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
  margin: 16px 0;
}

.prize-bar {
  background: linear-gradient(to right, #f59e0b, #fbbf24);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
}

.thanks-bar {
  background: linear-gradient(to right, #9ca3af, #d1d5db);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
}

.percentage-labels {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
}
</style>
```

---

## ✅ 優點總結

| 優點 | 說明 |
|-----|------|
| 📊 **資訊完整** | 清楚顯示中獎籤位和謝謝惠顧數量 |
| 🎯 **易於理解** | 前端可直接計算中獎率，無需額外計算 |
| 🔍 **透明化** | 玩家可以明確知道中獎機率 |
| 🛡️ **一致性** | 一番賞和刮刮樂都使用相同的資料結構 |
| 🚀 **前端友善** | 不需要前端手動計算 `maxDraws - totalPrizeCount` |

---

## 🧪 測試案例

### 測試 1：刮刮樂 - 有謝謝惠顧

**請求**：
```bash
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "lottery": {
      "title": "咒術迴戰刮刮樂",
      "playMode": "SCRATCH_MODE",
      "maxDraws": 100
    },
    "prizes": [
      { "name": "A賞", "quantity": 1 },
      { "name": "B賞", "quantity": 2 },
      { "name": "C賞", "quantity": 4 },
      { "name": "D賞", "quantity": 8 },
      { "name": "E賞", "quantity": 12 },
      { "name": "LAST賞", "quantity": 1 }
    ]
  }'
```

**預期回應**：
```json
{
  "success": true,
  "data": {
    "maxDraws": 100,
    "totalPrizeCount": 28,
    "thanksgivingCount": 72,  // ✅ 72 個謝謝惠顧
    "progressPercentage": 0.0
  }
}
```

---

### 測試 2：一番賞 - 無謝謝惠顧

**請求**：
```bash
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "lottery": {
      "title": "鬼滅一番賞",
      "playMode": "LOTTERY_MODE"
    },
    "prizes": [
      { "name": "A賞", "quantity": 1 },
      { "name": "B賞", "quantity": 5 },
      { "name": "C賞", "quantity": 20 }
    ]
  }'
```

**預期回應**：
```json
{
  "success": true,
  "data": {
    "maxDraws": 26,           // 自動計算 = 獎品總數
    "totalPrizeCount": 26,
    "thanksgivingCount": 0,   // ✅ 0 個謝謝惠顧（一番賞不允許）
    "progressPercentage": 0.0
  }
}
```

---

## 📝 檢查清單

**開發階段**：
- [x] 新增 `LotteryWithPrizesRes.thanksgivingCount` 欄位
- [x] 更新 `buildLotteryWithPrizesRes()` 計算邏輯
- [x] 編譯無錯誤
- [ ] 單元測試通過
- [ ] 整合測試通過

**部署階段**：
- [ ] 重新編譯 JAR
- [ ] 上傳到 EC2
- [ ] 重啟服務
- [ ] 驗證 API 回應包含 thanksgivingCount

**前端適配**：
- [ ] 更新 TypeScript 介面定義
- [ ] 顯示謝謝惠顧數量
- [ ] 顯示中獎率
- [ ] 視覺化籤位分佈

---

## 📖 相關文件

- [刮刮樂謝謝惠顧支援](./SCRATCH_MODE_THANKSGIVING_SUPPORT.md) - 刮刮樂模式完整說明
- [架構改進文件](./ARCHITECTURE_IMPROVEMENT_AUTO_MAXDRAWS_UNIFIED_API.md) - maxDraws 自動計算
- [Copilot 指南](./.github/copilot-instructions.md) - 專案整體架構

---

**修改者**：GitHub Copilot  
**審核者**：（待填寫）  
**部署時間**：（待填寫）
