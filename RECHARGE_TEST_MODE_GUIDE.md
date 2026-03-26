# 🎯 儲值 API - 測試模式說明

> **狀態**：✅ 已啟用測試模式（直接儲值）  
> **日期**：2026-02-08  
> **版本**：v1.1 (Test Mode)

---

## 📋 問題診斷與解決

### 問題 1：確認支付後金幣沒有增加 ❌

**原因**：
- 原始流程為兩步驟：`createRechargeRequest()` → `confirmPayment()`
- 第一步只建立 PENDING 記錄，不更新金幣
- 第二步才更新金幣，但需要手動調用

**解決方案**：✅
- 修改為**一步驟直接完成**
- `createRechargeRequest()` 直接完成支付並更新金幣

---

### 問題 2：還沒串接金流，需要直接儲值成功 ❌

**原因**：
- 原始設計需要支付網關回調才能完成支付

**解決方案**：✅
- 啟用測試模式，跳過金流驗證
- 直接設定 `paymentStatus = COMPLETED`
- 立即更新使用者金幣

---

## 🚀 測試模式功能

### 修改內容

| 項目 | 原始流程 | 測試模式 |
|------|---------|---------|
| **建立請求** | PENDING 狀態 | **COMPLETED** 狀態 |
| **金幣更新** | 需手動確認 | **立即更新** |
| **支付時間** | NULL | **立即設定** |
| **交易 ID** | NULL | **TEST-xxxxxxxx** |
| **交易記錄** | 需手動建立 | **立即建立** |

---

## 🎨 API 使用方式

### 1️⃣ 直接儲值（測試模式）

**端點**：`POST /api/recharge`

**REQ**：
```json
{
  "planId": "68bcafb9-2ab8-4b17-a5d0-8b91c6c4d5e6",
  "paymentMethod": "TEST",
  "remark": "測試儲值"
}
```

**RES**：
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "planId": "68bcafb9-2ab8-4b17-a5d0-8b91c6c4d5e6",
    "amount": 500,
    "goldCoins": 500,
    "bonusCoins": 50,
    "paymentMethod": "TEST",
    "paymentStatus": "COMPLETED",           // ✅ 直接完成
    "transactionId": "TEST-a1b2c3d4",       // ✅ 測試交易 ID
    "paidAt": "2026-02-08T10:30:00Z",       // ✅ 立即支付
    "createdAt": "2026-02-08T10:30:00Z"
  }
}
```

**效果**：
- ✅ 使用者金幣立即增加
- ✅ 建立 WalletTransaction 記錄
- ✅ 儲值記錄狀態為 COMPLETED

---

## 🔍 執行流程

```
前端調用 POST /api/recharge
         ↓
RechargeController.createRechargeRequest()
         ↓
RechargeService.createRechargeRequest()
         ↓
1️⃣ 驗證使用者
         ↓
2️⃣ 驗證儲值方案（活躍、日期範圍、未刪除）
         ↓
3️⃣ 建立 RechargeRecord（COMPLETED 狀態）✨
         ↓
4️⃣ 立即更新使用者金幣 ✨
    - user.goldCoins += plan.goldCoins
    - user.bonusCoins += plan.bonusCoins
    - user.totalRecharged += plan.amount
         ↓
5️⃣ 建立 WalletTransaction 審計記錄 ✨
    - transactionType = "RECHARGE"
    - coinType = "GOLD" / "BONUS"
         ↓
返回 RechargeRes（COMPLETED 狀態）
         ↓
前端接收，金幣已更新 ✅
```

---

## 🧪 測試案例

### 測試 1：基本儲值

```bash
# 1. 建立儲值請求（直接完成）
POST /api/recharge
{
  "planId": "plan-uuid",
  "paymentMethod": "TEST"
}

# 預期結果：
# - paymentStatus = "COMPLETED"
# - paidAt 不為空
# - transactionId = "TEST-xxxxxxxx"
```

```bash
# 2. 查詢使用者資訊
GET /api/user/me

# 預期結果：
# - goldCoins 已增加
# - bonusCoins 已增加
# - totalRecharged 已增加
```

```bash
# 3. 查詢儲值記錄
GET /api/recharge/history

# 預期結果：
# - 包含剛剛的儲值記錄
# - paymentStatus = "COMPLETED"
```

---

### 測試 2：方案驗證

```bash
# 測試未啟用的方案
POST /api/recharge
{
  "planId": "inactive-plan-uuid",
  "paymentMethod": "TEST"
}

# 預期結果：
# - 400 Bad Request
# - error: "儲值方案已禁用"
```

```bash
# 測試已結束的方案
POST /api/recharge
{
  "planId": "expired-plan-uuid",
  "paymentMethod": "TEST"
}

# 預期結果：
# - 400 Bad Request
# - error: "儲值方案已結束"
```

---

## 📊 數據驗證

### 1. 檢查 RechargeRecord 表

```sql
SELECT * FROM recharge_record
WHERE user_id = 'your-user-id'
ORDER BY created_at DESC
LIMIT 1;

-- 預期結果：
-- payment_status = 'COMPLETED'
-- paid_at 不為 NULL
-- transaction_id = 'TEST-xxxxxxxx'
```

### 2. 檢查 User 表

```sql
SELECT gold_coins, bonus_coins, total_recharged
FROM user
WHERE id = 'your-user-id';

-- 預期結果：
-- gold_coins 已增加
-- bonus_coins 已增加
-- total_recharged 已增加
```

### 3. 檢查 WalletTransaction 表

```sql
SELECT * FROM wallet_transaction
WHERE user_id = 'your-user-id'
  AND transaction_type = 'RECHARGE'
ORDER BY created_at DESC
LIMIT 2;

-- 預期結果：
-- 2 筆記錄（GOLD + BONUS）
-- coin_type = 'GOLD' / 'BONUS'
-- balance_after 為更新後的餘額
```

---

## 🎯 前端使用範例

### React 範例

```jsx
import { useState } from 'react';
import axios from 'axios';

function RechargeComponent() {
  const [loading, setLoading] = useState(false);
  const [userCoins, setUserCoins] = useState({ gold: 0, bonus: 0 });
  
  async function handleRecharge(planId) {
    setLoading(true);
    try {
      // 1. 直接儲值（測試模式）
      const res = await axios.post('/api/recharge', {
        planId,
        paymentMethod: 'TEST',
        remark: '測試儲值'
      });
      
      const result = res.data.data;
      
      // 2. 檢查支付狀態
      if (result.paymentStatus === 'COMPLETED') {
        alert(`✅ 儲值成功！獲得 ${result.goldCoins} 金幣 + ${result.bonusCoins} 紅利`);
        
        // 3. 重新整理使用者資訊
        const userRes = await axios.get('/api/user/me');
        const userData = userRes.data.data;
        
        setUserCoins({
          gold: userData.goldCoins,
          bonus: userData.bonusCoins
        });
        
        console.log(`💰 目前金幣: ${userData.goldCoins}`);
        console.log(`🎁 目前紅利: ${userData.bonusCoins}`);
        console.log(`📊 累計儲值: ${userData.totalRecharged}`);
      }
      
    } catch (error) {
      console.error('儲值失敗', error);
      alert('❌ 儲值失敗: ' + error.response?.data?.error);
    } finally {
      setLoading(false);
    }
  }
  
  return (
    <div>
      <h2>💰 目前金幣: {userCoins.gold}</h2>
      <h3>🎁 目前紅利: {userCoins.bonus}</h3>
      
      <button 
        onClick={() => handleRecharge('plan-uuid')}
        disabled={loading}
      >
        {loading ? '處理中...' : '儲值 500 元'}
      </button>
    </div>
  );
}
```

---

## ⚠️ 注意事項

### 測試模式限制

1. **不驗證真實支付**
   - 不需要支付網關
   - 不需要實際付款

2. **交易 ID 為測試用**
   - 格式：`TEST-xxxxxxxx`
   - 僅用於測試環境

3. **無金額限制**
   - 可無限次儲值
   - 無支付上限

### 生產環境注意

⚠️ **切換到生產環境時需要修改**：

1. 將 `paymentStatus` 改回 `PENDING`
2. 移除立即更新金幣的邏輯
3. 改由支付網關回調 `confirmPayment()` 更新金幣
4. 修改 `transactionId` 為真實金流交易 ID

---

## 🔄 未來切換到金流模式

### 需要修改的地方

**RechargeServiceImpl.java - createRechargeRequest()**

```java
// ❌ 測試模式（目前）
record.setPaymentStatus("COMPLETED");
record.setPaidAt(now);
record.setTransactionId("TEST-" + UUID.randomUUID().toString().substring(0, 8));

// 立即更新金幣...

// ✅ 金流模式（未來）
record.setPaymentStatus("PENDING");
// 不設定 paidAt
// 不設定 transactionId
// 不更新金幣

// 返回記錄，等待支付網關回調 confirmPayment()
```

---

## 📞 常見問題

**Q1: 為什麼金幣沒有增加？**
- A: 檢查 API 返回的 `paymentStatus` 是否為 `COMPLETED`
- 檢查日誌是否有「使用者金幣已更新」訊息

**Q2: 如何查看儲值記錄？**
- A: 調用 `GET /api/recharge/history`

**Q3: 如何驗證金幣確實增加？**
- A: 調用 `GET /api/user/me` 查看 `goldCoins` / `bonusCoins`

**Q4: 測試模式會影響生產資料嗎？**
- A: 不會，測試模式只是跳過金流驗證，其他邏輯一致

**Q5: 未來如何切換到金流模式？**
- A: 修改 `createRechargeRequest()` 回到原始邏輯（PENDING 狀態）

---

## ✅ 修改完成檢查清單

| 項目 | 狀態 |
|------|------|
| ✅ 修改 `createRechargeRequest()` 為直接完成 | 完成 |
| ✅ 立即更新使用者金幣 | 完成 |
| ✅ 立即建立 WalletTransaction | 完成 |
| ✅ 設定測試交易 ID | 完成 |
| ✅ 更新 Controller 註解 | 完成 |
| ✅ 編譯驗證通過 | 完成 |
| ✅ 建立測試模式說明文檔 | 完成 |

---

**🎉 測試模式已啟用！現在可以直接儲值，金幣會立即增加！**

**測試步驟**：
1. 調用 `POST /api/recharge`
2. 檢查返回的 `paymentStatus` 是否為 `COMPLETED`
3. 調用 `GET /api/user/me` 驗證金幣已增加

---

**簽核**：✅ 完成  
**日期**：2026-02-08  
**版本**：v1.1 (Test Mode)
