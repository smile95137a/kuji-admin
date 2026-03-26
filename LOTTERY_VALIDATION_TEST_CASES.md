# 商品新增驗證規則完整測試案例

## 修改摘要

### 問題
用戶提交的商品資料：
- 總抽數 `maxDraws = 0`（未設定）
- 獎品總數 = 28
- 模式 = `LOTTERY_MODE`（一番賞）

系統沒有拋出錯誤，成功建立了商品，但無法正常使用（因為沒有籤位）。

### 修正邏輯

**Before**：
```java
if (lotteryRes.getMaxDraws() != null && lotteryRes.getMaxDraws() > 0) {
    // 驗證邏輯
}
// ❌ maxDraws = 0 或 null 時，直接跳過驗證
```

**After**：
```java
if (maxDraws == null || maxDraws <= 0) {
    // ✅ 一番賞模式：必須設定總抽數
    if ("LOTTERY_MODE".equals(playMode)) {
        throw new BusinessException("一番賞模式錯誤：必須設定總抽數！");
    }
} else {
    // ✅ 有設定總抽數，驗證獎品數量
    if ("LOTTERY_MODE".equals(playMode)) {
        if (totalPrizeQuantity != maxDraws) {
            throw new BusinessException(...);
        }
    }
}
```

---

## 測試案例矩陣

| # | playMode | maxDraws | 獎品總數 | 預期結果 | 錯誤訊息 |
|---|----------|----------|---------|---------|---------|
| 1 | LOTTERY_MODE | 0 | 28 | ❌ 失敗 | 一番賞模式錯誤：必須設定總抽數！建議設定為獎品總數(28) |
| 2 | LOTTERY_MODE | null | 28 | ❌ 失敗 | 一番賞模式錯誤：必須設定總抽數！建議設定為獎品總數(28) |
| 3 | LOTTERY_MODE | 28 | 28 | ✅ 成功 | - |
| 4 | LOTTERY_MODE | 100 | 28 | ❌ 失敗 | 一番賞模式錯誤：獎品總數(28)必須等於總抽數(100)！ |
| 5 | LOTTERY_MODE | 20 | 28 | ❌ 失敗 | 一番賞模式錯誤：獎品總數(28)必須等於總抽數(20)！ |
| 6 | SCRATCH_MODE | 0 | 28 | ⚠️ 成功（無籤位） | - |
| 7 | SCRATCH_MODE | null | 28 | ⚠️ 成功（無籤位） | - |
| 8 | SCRATCH_MODE | 100 | 28 | ✅ 成功 | 72 個謝謝惠顧 |
| 9 | SCRATCH_MODE | 28 | 28 | ✅ 成功 | 0 個謝謝惠顧 |
| 10 | SCRATCH_MODE | 20 | 28 | ❌ 失敗 | 刮刮樂模式錯誤：獎品總數(28)不能大於總抽數(20)！ |

---

## 詳細測試案例

### 測試案例 1：一番賞 - 未設定總抽數 ❌

**請求**：
```json
{
  "lottery": {
    "title": "鬼滅之刃一番賞",
    "playMode": "LOTTERY_MODE",
    "maxDraws": 0
  },
  "prizes": [
    {"name": "A賞", "quantity": 1},
    {"name": "B賞", "quantity": 2},
    {"name": "C賞", "quantity": 25}
  ]
}
```

**預期回應**：
```json
{
  "success": false,
  "error": {
    "code": "BUSINESS_ERROR",
    "message": "一番賞模式錯誤：必須設定總抽數！建議設定為獎品總數(28)。一番賞每個籤位都應該有獎品，不能有謝謝惠顧。"
  }
}
```

**HTTP 狀態碼**：400

---

### 測試案例 2：一番賞 - 總抽數 null ❌

**請求**：
```json
{
  "lottery": {
    "title": "鬼滅之刃一番賞",
    "playMode": "LOTTERY_MODE"
    // maxDraws 未傳送
  },
  "prizes": [
    {"name": "A賞", "quantity": 28}
  ]
}
```

**預期回應**：
```json
{
  "success": false,
  "error": {
    "code": "BUSINESS_ERROR",
    "message": "一番賞模式錯誤：必須設定總抽數！建議設定為獎品總數(28)。一番賞每個籤位都應該有獎品，不能有謝謝惠顧。"
  }
}
```

---

### 測試案例 3：一番賞 - 獎品數=總抽數 ✅

**請求**：
```json
{
  "lottery": {
    "title": "鬼滅之刃一番賞",
    "playMode": "LOTTERY_MODE",
    "maxDraws": 28
  },
  "prizes": [
    {"name": "A賞", "quantity": 1},
    {"name": "B賞", "quantity": 2},
    {"name": "C賞", "quantity": 4},
    {"name": "D賞", "quantity": 8},
    {"name": "E賞", "quantity": 12},
    {"name": "最後賞", "quantity": 1}
  ]
}
```

**預期回應**：
```json
{
  "success": true,
  "data": {
    "id": "...",
    "title": "鬼滅之刃一番賞",
    "totalDraws": 28,
    "totalPrizeCount": 28,
    "prizes": [...]
  }
}
```

**HTTP 狀態碼**：200

**日誌**：
```
✅ 籤位生成完成: lotteryId=xxx, maxDraws=28
```

---

### 測試案例 4：一番賞 - 獎品數 < 總抽數 ❌

**請求**：
```json
{
  "lottery": {
    "title": "火影忍者一番賞",
    "playMode": "LOTTERY_MODE",
    "maxDraws": 100
  },
  "prizes": [
    {"name": "A賞", "quantity": 1},
    {"name": "B賞", "quantity": 2},
    {"name": "C賞", "quantity": 25}
  ]
}
```

**預期回應**：
```json
{
  "success": false,
  "error": {
    "code": "BUSINESS_ERROR",
    "message": "一番賞模式錯誤：獎品總數(28)必須等於總抽數(100)！每個籤位都應該有獎品，不能有謝謝惠顧。請調整獎品數量或總抽數。"
  }
}
```

---

### 測試案例 5：一番賞 - 獎品數 > 總抽數 ❌

**請求**：
```json
{
  "lottery": {
    "title": "航海王一番賞",
    "playMode": "LOTTERY_MODE",
    "maxDraws": 20
  },
  "prizes": [
    {"name": "A賞", "quantity": 10},
    {"name": "B賞", "quantity": 18}
  ]
}
```

**預期回應**：
```json
{
  "success": false,
  "error": {
    "code": "BUSINESS_ERROR",
    "message": "一番賞模式錯誤：獎品總數(28)必須等於總抽數(20)！每個籤位都應該有獎品，不能有謝謝惠顧。請調整獎品數量或總抽數。"
  }
}
```

---

### 測試案例 6：刮刮樂 - 未設定總抽數 ⚠️

**請求**：
```json
{
  "lottery": {
    "title": "刮刮樂",
    "playMode": "SCRATCH_MODE",
    "maxDraws": 0
  },
  "prizes": [
    {"name": "大獎", "quantity": 1, "isGrandPrize": true},
    {"name": "二獎", "quantity": 5}
  ]
}
```

**預期回應**：
```json
{
  "success": true,
  "data": {
    "id": "...",
    "title": "刮刮樂",
    "totalDraws": 0,
    "totalPrizeCount": 6
  }
}
```

**HTTP 狀態碼**：200

**日誌**：
```
⚠️ 總抽數未設定或為 0，跳過籤位生成
```

**注意**：商品建立成功，但無法抽獎（沒有籤位）

---

### 測試案例 7：刮刮樂 - 獎品數 < 總抽數 ✅

**請求**：
```json
{
  "lottery": {
    "title": "刮刮樂 100 抽",
    "playMode": "SCRATCH_MODE",
    "maxDraws": 100
  },
  "prizes": [
    {"name": "大獎", "quantity": 1, "isGrandPrize": true},
    {"name": "二獎", "quantity": 5},
    {"name": "三獎", "quantity": 10}
  ]
}
```

**預期回應**：
```json
{
  "success": true,
  "data": {
    "id": "...",
    "title": "刮刮樂 100 抽",
    "totalDraws": 100,
    "totalPrizeCount": 16
  }
}
```

**HTTP 狀態碼**：200

**日誌**：
```
ℹ️ 刮刮樂模式：獎品 16 個，謝謝惠顧 84 個
✅ 刮刮樂籤位生成完成，獎品 16 個，謝謝惠顧 84 個
```

**籤位分配**：
- 16 個籤位有獎品（隨機分配）
- 84 個籤位為「謝謝惠顧」

---

### 測試案例 8：刮刮樂 - 獎品數 = 總抽數 ✅

**請求**：
```json
{
  "lottery": {
    "title": "刮刮樂（全中獎）",
    "playMode": "SCRATCH_MODE",
    "maxDraws": 28
  },
  "prizes": [
    {"name": "A賞", "quantity": 1},
    {"name": "B賞", "quantity": 27}
  ]
}
```

**預期回應**：
```json
{
  "success": true,
  "data": {
    "totalDraws": 28,
    "totalPrizeCount": 28
  }
}
```

**日誌**：
```
ℹ️ 刮刮樂模式：獎品 28 個，謝謝惠顧 0 個
```

---

### 測試案例 9：刮刮樂 - 獎品數 > 總抽數 ❌

**請求**：
```json
{
  "lottery": {
    "title": "刮刮樂（錯誤）",
    "playMode": "SCRATCH_MODE",
    "maxDraws": 20
  },
  "prizes": [
    {"name": "A賞", "quantity": 15},
    {"name": "B賞", "quantity": 10}
  ]
}
```

**預期回應**：
```json
{
  "success": false,
  "error": {
    "code": "BUSINESS_ERROR",
    "message": "刮刮樂模式錯誤：獎品總數(25)不能大於總抽數(20)！"
  }
}
```

---

## Postman 測試腳本

### 環境變數設定
```javascript
// 在 Postman Environment 設定
{
  "base_url": "http://18.179.187.129/api",
  "admin_token": "YOUR_JWT_TOKEN_HERE"
}
```

### 測試 Collection

#### 1. 一番賞 - 成功案例
```bash
POST {{base_url}}/admin/lottery-with-prizes
Headers:
  Authorization: Bearer {{admin_token}}
  Content-Type: application/json
Body:
{
  "lottery": {
    "title": "鬼滅之刃一番賞（測試）",
    "category": "OFFICIAL_ICHIBAN",
    "playMode": "LOTTERY_MODE",
    "maxDraws": 28,
    "pricePerDraw": 80,
    "status": "OFF_SHELF"
  },
  "prizes": [
    {"name": "A賞", "quantity": 1, "level": "A", "isGrandPrize": true},
    {"name": "B賞", "quantity": 2, "level": "B"},
    {"name": "C賞", "quantity": 4, "level": "C"},
    {"name": "D賞", "quantity": 8, "level": "D"},
    {"name": "E賞", "quantity": 12, "level": "E"},
    {"name": "最後賞", "quantity": 1, "level": "LAST", "isLastPrize": true}
  ]
}

Tests:
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});
pm.test("Success is true", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.success).to.eql(true);
});
pm.test("Total draws equals prize count", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data.totalDraws).to.eql(jsonData.data.totalPrizeCount);
});
```

#### 2. 一番賞 - 錯誤案例（獎品數 ≠ 總抽數）
```bash
POST {{base_url}}/admin/lottery-with-prizes
Body:
{
  "lottery": {
    "title": "一番賞錯誤測試",
    "playMode": "LOTTERY_MODE",
    "maxDraws": 100,
    "pricePerDraw": 80
  },
  "prizes": [
    {"name": "A賞", "quantity": 28}
  ]
}

Tests:
pm.test("Status code is 400", function () {
    pm.response.to.have.status(400);
});
pm.test("Success is false", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.success).to.eql(false);
});
pm.test("Error message contains validation info", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.error.message).to.include("獎品總數");
    pm.expect(jsonData.error.message).to.include("總抽數");
});
```

#### 3. 刮刮樂 - 成功案例（有謝謝惠顧）
```bash
POST {{base_url}}/admin/lottery-with-prizes
Body:
{
  "lottery": {
    "title": "刮刮樂測試",
    "playMode": "SCRATCH_MODE",
    "maxDraws": 100,
    "pricePerDraw": 50
  },
  "prizes": [
    {"name": "大獎", "quantity": 1, "isGrandPrize": true},
    {"name": "二獎", "quantity": 5},
    {"name": "三獎", "quantity": 10}
  ]
}

Tests:
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});
pm.test("Prize count less than total draws", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data.totalPrizeCount).to.be.below(jsonData.data.totalDraws);
});
```

---

## 前端提示建議

### 即時驗證（推薦）
```javascript
// 在前端表單中即時計算並提示
const calculateTotalPrizes = () => {
  return prizes.reduce((sum, p) => sum + (p.quantity || 0), 0);
};

const validateLottery = () => {
  const totalPrizes = calculateTotalPrizes();
  const maxDraws = form.maxDraws;
  
  if (form.playMode === 'LOTTERY_MODE') {
    if (!maxDraws || maxDraws === 0) {
      return {
        valid: false,
        message: `一番賞模式必須設定總抽數！建議設定為 ${totalPrizes}`
      };
    }
    if (totalPrizes !== maxDraws) {
      return {
        valid: false,
        message: `一番賞：獎品總數(${totalPrizes})必須等於總抽數(${maxDraws})`
      };
    }
  } else if (form.playMode === 'SCRATCH_MODE') {
    if (maxDraws && totalPrizes > maxDraws) {
      return {
        valid: false,
        message: `刮刮樂：獎品總數(${totalPrizes})不能大於總抽數(${maxDraws})`
      };
    }
  }
  
  return { valid: true };
};
```

### 自動建議
```javascript
// 一番賞模式：自動建議總抽數
if (playMode === 'LOTTERY_MODE' && (!maxDraws || maxDraws === 0)) {
  const totalPrizes = calculateTotalPrizes();
  showSuggestion(`建議總抽數設為：${totalPrizes}`);
  // 或直接自動填入
  form.maxDraws = totalPrizes;
}
```

---

## 部署檢查清單

- [x] 修改 `LotteryServiceImpl.java`（新增 maxDraws=0 檢查）
- [x] 移除 `LotteryWithPrizesRes.warning` 欄位
- [ ] 編譯通過（BUILD SUCCESS）
- [ ] 上傳 JAR 到 EC2
- [ ] 重啟服務
- [ ] 測試案例 1：一番賞 maxDraws=0（預期失敗）
- [ ] 測試案例 3：一番賞 獎品數=總抽數（預期成功）
- [ ] 測試案例 4：一番賞 獎品數≠總抽數（預期失敗）
- [ ] 測試案例 7：刮刮樂 獎品數<總抽數（預期成功）
- [ ] 通知前端團隊：需要設定 maxDraws 欄位

---

**修正日期**：2026-01-29  
**影響範圍**：後台新增商品驗證邏輯  
**向下相容性**：⚠️ 不相容（之前允許 maxDraws=0，現在會拋錯）
