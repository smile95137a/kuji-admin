# 刮刮樂模式支援謝謝惠顧功能

**日期**：2025-01-30  
**版本**：1.1.1  
**優先級**：🟢 MEDIUM（功能增強）

---

## 📋 需求說明

### 問題
原本的刮刮樂模式（SCRATCH_MODE）邏輯錯誤：
- ❌ 舊邏輯：maxDraws 自動計算為獎品總數
- ❌ 無法支援「謝謝惠顧」籤位
- ❌ 例如：2 個獎品無法生成 50 抽的刮刮樂

### 正確需求
刮刮樂應該支援謝謝惠顧：
- ✅ 前端設定 `maxDraws = 50`（總共要 50 抽）
- ✅ 獎品只有 2 個（例如：頭獎 1 個 + 貳獎 1 個）
- ✅ 剩餘 48 個籤位應該是「謝謝惠顧」
- ✅ 玩家抽到謝謝惠顧時沒有獎品，但仍消耗一次抽獎機會

---

## 🔧 技術實現

### 修改檔案
1. `LotteryServiceImpl.java` - `createLotteryWithPrizes()` 方法
2. `LotteryCreateReq.java` - maxDraws 欄位註解
3. `ARCHITECTURE_IMPROVEMENT_AUTO_MAXDRAWS_UNIFIED_API.md` - 文檔更新

### 核心邏輯

```java
if ("SCRATCH_MODE".equals(playMode)) {
    // 刮刮樂：使用前端傳入的 maxDraws（支援謝謝惠顧）
    Integer frontendMaxDraws = req.getLottery().getMaxDraws();
    
    if (frontendMaxDraws != null && frontendMaxDraws >= totalPrizeQuantity) {
        // ✅ 使用前端設定值，生成謝謝惠顧
        calculatedMaxDraws = frontendMaxDraws;
        int thanksgivingCount = frontendMaxDraws - totalPrizeQuantity;
        log.info("🎰 刮刮樂模式：使用前端設定 maxDraws = {}（獎品 {} 個 + 謝謝惠顧 {} 個）", 
                calculatedMaxDraws, totalPrizeQuantity, thanksgivingCount);
    } else if (frontendMaxDraws != null && frontendMaxDraws < totalPrizeQuantity) {
        // ❌ 總抽數不能小於獎品總數
        throw new BusinessException("刮刮樂模式錯誤：總抽數不能小於獎品總數！");
    } else {
        // ⚠️ 前端未設定，預設為獎品總數（沒有謝謝惠顧）
        calculatedMaxDraws = totalPrizeQuantity;
        log.info("🎰 刮刮樂模式：前端未設定 maxDraws，預設 = 獎品總數 = {}（無謝謝惠顧）", 
                calculatedMaxDraws);
    }
}
```

---

## 📊 遊戲模式對比

| 模式 | maxDraws 處理 | 謝謝惠顧 | 前端是否需傳 maxDraws | 驗證規則 |
|-----|--------------|---------|---------------------|---------|
| **一番賞<br>LOTTERY_MODE** | 後端自動計算<br>= 獎品總數 | ❌ 不支援 | ❌ 不需要<br>（傳了也會被覆寫） | maxDraws = Σ(prize.quantity) |
| **刮刮樂<br>SCRATCH_MODE** | 使用前端傳入值 | ✅ 支援 | ✅ 需要<br>（若要有謝謝惠顧） | maxDraws ≥ Σ(prize.quantity) |

### 一番賞模式（LOTTERY_MODE）

**特性**：
- 每個籤位都有獎品
- 不能有謝謝惠顧
- maxDraws 必須等於獎品總數

**範例**：
```javascript
// 建立鬼滅之刃一番賞
const response = await api.post('/admin/lottery-with-prizes', {
  lottery: {
    title: '鬼滅之刃一番賞',
    playMode: 'LOTTERY_MODE',
    // maxDraws 不用傳，後端自動計算
  },
  prizes: [
    { name: 'A賞', quantity: 1 },  // 1 個
    { name: 'B賞', quantity: 5 },  // 5 個
    { name: 'C賞', quantity: 20 }  // 20 個
  ]
});

// 後端自動計算：maxDraws = 1 + 5 + 20 = 26
// 生成 26 個籤位，全部都有獎品
```

### 刮刮樂模式（SCRATCH_MODE）

**特性**：
- 可以有謝謝惠顧
- maxDraws 可以大於獎品總數
- 剩餘的籤位是謝謝惠顧

**範例 1：有謝謝惠顧**
```javascript
// 建立新年刮刮樂（50 抽，只有 2 個獎）
const response = await api.post('/admin/lottery-with-prizes', {
  lottery: {
    title: '新年刮刮樂',
    playMode: 'SCRATCH_MODE',
    maxDraws: 50,  // ← 必須傳入，設定總抽數
  },
  prizes: [
    { name: '頭獎', quantity: 1 },  // 頭獎 1 個
    { name: '貳獎', quantity: 1 }   // 貳獎 1 個
  ]
});

// 後端計算：
// - 獎品總數 = 2
// - maxDraws = 50（使用前端設定）
// - 謝謝惠顧 = 50 - 2 = 48 個
// - 生成 50 個籤位：2 個中獎 + 48 個謝謝惠顧
```

**範例 2：無謝謝惠顧**
```javascript
// 建立簡易刮刮樂（不傳 maxDraws）
const response = await api.post('/admin/lottery-with-prizes', {
  lottery: {
    title: '簡易刮刮樂',
    playMode: 'SCRATCH_MODE',
    // maxDraws 不傳
  },
  prizes: [
    { name: '頭獎', quantity: 1 },
    { name: '貳獎', quantity: 9 }
  ]
});

// 後端計算：
// - 獎品總數 = 10
// - maxDraws = 10（預設為獎品總數）
// - 謝謝惠顧 = 0
// - 生成 10 個籤位，全部中獎
```

**範例 3：錯誤案例**
```javascript
// ❌ 錯誤：maxDraws 小於獎品總數
const response = await api.post('/admin/lottery-with-prizes', {
  lottery: {
    title: '錯誤刮刮樂',
    playMode: 'SCRATCH_MODE',
    maxDraws: 5,  // ← 錯誤！小於獎品總數
  },
  prizes: [
    { name: '獎品', quantity: 10 }  // 獎品 10 個
  ]
});

// 後端返回錯誤：
// HTTP 400 Bad Request
// "刮刮樂模式錯誤：總抽數(5)不能小於獎品總數(10)！請調整設定。"
```

---

## 🎮 前端使用指南

### 建立刮刮樂表單

```vue
<template>
  <form @submit.prevent="handleSubmit">
    <!-- 商品名稱 -->
    <input v-model="form.title" placeholder="商品名稱" required />
    
    <!-- 遊戲模式 -->
    <select v-model="form.playMode" required>
      <option value="LOTTERY_MODE">一番賞</option>
      <option value="SCRATCH_MODE">刮刮樂</option>
    </select>
    
    <!-- ✅ 刮刮樂需要設定總抽數 -->
    <div v-if="form.playMode === 'SCRATCH_MODE'">
      <label>總抽數（包含謝謝惠顧）</label>
      <input 
        v-model.number="form.maxDraws" 
        type="number" 
        placeholder="例如：50"
        :min="totalPrizeQuantity"
        required
      />
      <p class="hint">
        目前獎品總數：{{ totalPrizeQuantity }} 個<br>
        謝謝惠顧：{{ form.maxDraws - totalPrizeQuantity }} 個
      </p>
    </div>
    
    <!-- ❌ 一番賞不需要設定 maxDraws -->
    <div v-else-if="form.playMode === 'LOTTERY_MODE'">
      <p class="info">
        一番賞模式：總抽數 = 獎品總數（{{ totalPrizeQuantity }} 個）
      </p>
    </div>
    
    <!-- 獎品列表 -->
    <div v-for="(prize, index) in prizes" :key="index">
      <input v-model="prize.name" placeholder="獎品名稱" />
      <input v-model.number="prize.quantity" type="number" placeholder="數量" />
    </div>
    
    <button type="submit">建立商品</button>
  </form>
</template>

<script setup>
import { ref, computed } from 'vue';

const form = ref({
  title: '',
  playMode: 'LOTTERY_MODE',
  maxDraws: 0
});

const prizes = ref([
  { name: '', quantity: 0 }
]);

// 計算獎品總數
const totalPrizeQuantity = computed(() => {
  return prizes.value.reduce((sum, p) => sum + (p.quantity || 0), 0);
});

const handleSubmit = async () => {
  const payload = {
    lottery: {
      title: form.value.title,
      playMode: form.value.playMode,
    },
    prizes: prizes.value
  };
  
  // ✅ 只有刮刮樂需要傳 maxDraws
  if (form.value.playMode === 'SCRATCH_MODE' && form.value.maxDraws > 0) {
    payload.lottery.maxDraws = form.value.maxDraws;
  }
  
  try {
    const response = await api.post('/admin/lottery-with-prizes', payload);
    console.log('建立成功', response.data);
  } catch (error) {
    console.error('建立失敗', error.response.data);
  }
};
</script>
```

### 前端驗證規則

```typescript
// 建立商品前的驗證
const validateLottery = (form, prizes) => {
  const totalQuantity = prizes.reduce((sum, p) => sum + p.quantity, 0);
  
  if (form.playMode === 'LOTTERY_MODE') {
    // 一番賞：不需要驗證 maxDraws
    return { valid: true };
  }
  
  if (form.playMode === 'SCRATCH_MODE') {
    if (form.maxDraws && form.maxDraws < totalQuantity) {
      return {
        valid: false,
        error: `總抽數(${form.maxDraws})不能小於獎品總數(${totalQuantity})`
      };
    }
    
    if (form.maxDraws && form.maxDraws > totalQuantity) {
      const thanksgiving = form.maxDraws - totalQuantity;
      return {
        valid: true,
        info: `將生成 ${totalQuantity} 個中獎籤位 + ${thanksgiving} 個謝謝惠顧`
      };
    }
  }
  
  return { valid: true };
};
```

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
      "title": "新年刮刮樂",
      "category": "CUSTOM_GACHA",
      "pricePerDraw": 100,
      "playMode": "SCRATCH_MODE",
      "maxDraws": 50,
      "status": "OFF_SHELF"
    },
    "prizes": [
      { "name": "頭獎", "level": "A", "quantity": 1 },
      { "name": "貳獎", "level": "B", "quantity": 1 }
    ]
  }'
```

**預期回應**：
```json
{
  "success": true,
  "data": {
    "id": "lottery-uuid",
    "maxDraws": 50,
    "totalDraws": 0,
    "remainingDraws": 50,
    "prizes": [
      { "id": "prize-1", "name": "頭獎", "quantity": 1, "remaining": 1 },
      { "id": "prize-2", "name": "貳獎", "quantity": 1, "remaining": 1 }
    ]
  }
}
```

**預期日誌**：
```
🎰 刮刮樂模式：使用前端設定 maxDraws = 50（獎品 2 個 + 謝謝惠顧 48 個）
✅ 已更新商品 maxDraws: lotteryId=xxx, maxDraws=50
✅ 籤位生成完成: lotteryId=xxx, maxDraws=50
```

**資料庫驗證**：
```sql
-- 檢查商品
SELECT id, title, max_draws, total_draws, remaining_draws 
FROM lottery 
WHERE id = 'lottery-uuid';
-- 預期：max_draws=50, total_draws=0, remaining_draws=50

-- 檢查籤位
SELECT COUNT(*) as total_tickets,
       SUM(CASE WHEN prize_id IS NOT NULL THEN 1 ELSE 0 END) as prize_tickets,
       SUM(CASE WHEN prize_id IS NULL THEN 1 ELSE 0 END) as thanksgiving_tickets
FROM lottery_ticket 
WHERE lottery_id = 'lottery-uuid';
-- 預期：total_tickets=50, prize_tickets=2, thanksgiving_tickets=48
```

---

### 測試 2：刮刮樂 - 無謝謝惠顧

**請求**：
```bash
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "lottery": {
      "title": "簡易刮刮樂",
      "category": "CUSTOM_GACHA",
      "pricePerDraw": 100,
      "playMode": "SCRATCH_MODE",
      "status": "OFF_SHELF"
    },
    "prizes": [
      { "name": "頭獎", "level": "A", "quantity": 10 }
    ]
  }'
```

**預期日誌**：
```
🎰 刮刮樂模式：前端未設定 maxDraws，預設 = 獎品總數 = 10（無謝謝惠顧）
✅ 已更新商品 maxDraws: lotteryId=xxx, maxDraws=10
✅ 籤位生成完成: lotteryId=xxx, maxDraws=10
```

---

### 測試 3：刮刮樂 - 錯誤案例

**請求**：
```bash
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "lottery": {
      "title": "錯誤刮刮樂",
      "category": "CUSTOM_GACHA",
      "pricePerDraw": 100,
      "playMode": "SCRATCH_MODE",
      "maxDraws": 5,
      "status": "OFF_SHELF"
    },
    "prizes": [
      { "name": "獎品", "level": "A", "quantity": 10 }
    ]
  }'
```

**預期回應**：
```json
{
  "success": false,
  "error": {
    "code": "BUSINESS_ERROR",
    "message": "刮刮樂模式錯誤：總抽數(5)不能小於獎品總數(10)！請調整設定。"
  }
}
```

**預期日誌**：
```
❌ 刮刮樂模式錯誤：總抽數(5)不能小於獎品總數(10)！請調整設定。
```

---

### 測試 4：一番賞 - maxDraws 被覆寫

**請求**：
```bash
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "lottery": {
      "title": "鬼滅一番賞",
      "category": "OFFICIAL_ICHIBAN",
      "pricePerDraw": 650,
      "playMode": "LOTTERY_MODE",
      "maxDraws": 999,
      "status": "OFF_SHELF"
    },
    "prizes": [
      { "name": "A賞", "level": "A", "quantity": 5 }
    ]
  }'
```

**預期日誌**：
```
🎯 一番賞模式：自動設定 maxDraws = 獎品總數 = 5
⚠️ 一番賞模式：前端傳入 maxDraws=999 與獎品總數=5 不符，已自動覆寫
✅ 已更新商品 maxDraws: lotteryId=xxx, maxDraws=5
```

**預期回應**：
```json
{
  "success": true,
  "data": {
    "id": "lottery-uuid",
    "maxDraws": 5,  // ← 被覆寫為 5，而非 999
    "totalDraws": 0,
    "remainingDraws": 5
  }
}
```

---

## 📝 檢查清單

**開發階段**：
- [x] 修改 `LotteryServiceImpl.java` - 刮刮樂邏輯
- [x] 更新 `LotteryCreateReq.java` - maxDraws 註解
- [x] 更新文檔 - 遊戲模式對比表
- [x] 編譯無錯誤
- [ ] 單元測試通過
- [ ] 整合測試通過

**測試階段**：
- [ ] 測試刮刮樂 - 有謝謝惠顧
- [ ] 測試刮刮樂 - 無謝謝惠顧（不傳 maxDraws）
- [ ] 測試刮刮樂 - 錯誤案例（maxDraws < 獎品總數）
- [ ] 測試一番賞 - maxDraws 自動計算
- [ ] 測試一番賞 - maxDraws 被覆寫
- [ ] 檢查日誌輸出正確
- [ ] 檢查資料庫籤位生成正確

**部署階段**：
- [ ] 備份舊版本
- [ ] 部署新版本
- [ ] 服務健康檢查
- [ ] 前端適配完成
- [ ] 使用者測試通過

---

## ✅ 優點總結

| 優點 | 說明 |
|-----|------|
| 🎯 **業務邏輯正確** | 刮刮樂支援謝謝惠顧，符合真實遊戲規則 |
| 🛡️ **資料一致性** | 後端驗證 maxDraws 不能小於獎品總數 |
| 🔧 **靈活性** | 刮刮樂可自由設定總抽數，支援不同中獎率 |
| 📊 **透明化** | 日誌清楚顯示獎品數量和謝謝惠顧數量 |
| 🚀 **易於使用** | 前端只需傳入 maxDraws，後端自動計算謝謝惠顧 |

---

## 📖 相關文件

- [架構改進文件](./ARCHITECTURE_IMPROVEMENT_AUTO_MAXDRAWS_UNIFIED_API.md) - 完整架構說明
- [Copilot 指南](./.github/copilot-instructions.md) - 專案整體架構
- [安全性修正](./SECURITY_FIX_TICKET_INFO_LEAK.md) - 籤位資訊洩漏修正

---

**修改者**：GitHub Copilot  
**審核者**：（待填寫）  
**部署時間**：（待填寫）
