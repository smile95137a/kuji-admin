# 合約：POST /api/auth/validate-referral

**在使用者註冊期間驗證推薦碼（公開端點）**

---

## 基本資訊

| 欄位 | 值 |
|-------|------|
| Method | POST |
| Path | `/api/auth/validate-referral` |
| Auth | ✗ 公開存取（無需登入）|
| Content-Type | application/json |
| Rate Limit | 10 requests per 60 seconds |

---

## HTTP 請求

### 請求路徑

```
POST /api/auth/validate-referral
```

### 請求頭

```
Content-Type: application/json
```

### 請求體

```json
{
  "code": "STORE-ABC123"
}
```

#### 參數說明

| 參數名 | 型別 | 必填 | 說明 |
|-------|------|------|------|
| `code` | String | 是 | 推薦碼（1-50 字元，自動轉大寫） |

#### 驗證規則

- 不能為空
- 長度 1-50 字元
- 不區分大小寫（輸入後自動轉大寫）

---

## HTTP 回應

### 200 OK — 代碼驗證完成

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "valid": true,
    "code": "STORE-ABC123",
    "storeName": "Dream Store"
  }
}
```

### 200 OK — 代碼無效

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "valid": false,
    "code": "INVALID-CODE",
    "reason": "推薦碼不存在或已停用"
  }
}
```

### 400 Bad Request — 請求格式錯誤

```json
{
  "code": 400,
  "message": "推薦碼不能為空",
  "error": "VALIDATION_ERROR"
}
```

### 429 Too Many Requests — 超過限速

```json
{
  "code": 429,
  "message": "請求過於頻繁，請稍後再試",
  "error": "RATE_LIMIT_EXCEEDED"
}
```

---

## 回應欄位

| 欄位名 | 型別 | 說明 |
|--------|------|------|
| `valid` | boolean | 推薦碼是否有效 |
| `code` | string | 查詢的推薦碼（大寫） |
| `storeName` | string | 若 valid=true，回傳推薦店家名稱；若 valid=false，為 null |
| `reason` | string | 若 valid=false，回傳失敗原因；若 valid=true，為 null |

---

## 無效推薦碼的原因

| 原因 | 說明 |
|------|------|
| `CODE_NOT_FOUND` | 推薦碼不存在 |
| `CODE_DISABLED` | 推薦碼已被停用 |
| `CODE_EXPIRED` | 推薦碼已過期 |
| `CODE_EXCEED_LIMIT` | 推薦碼已達使用上限 |
| `STORE_INACTIVE` | 推薦來源店家非活躍 |

---

## 使用場景

此端點用於**使用者在官網註冊表單中實時驗證推薦碼**。前端應在使用者輸入推薦碼後呼叫此端點，以：

1. 確認推薦碼有效性
2. 顯示推薦店家名稱（增進 UX 確認）
3. 在提交註冊前給予及時反饋

---

## 使用範例

### cURL

```bash
curl -X POST http://localhost:8080/api/auth/validate-referral \
  -H "Content-Type: application/json" \
  -d '{"code": "STORE-ABC123"}'
```

### JavaScript / fetch

```javascript
async function validateReferral(code) {
  const response = await fetch('/api/auth/validate-referral', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ code })
  });
  return response.json();
}

// 使用
const result = await validateReferral('STORE-ABC123');
if (result.data.valid) {
  console.log('推薦店家：', result.data.storeName);
}
```

---

## 實作說明

### 驗證邏輯（後端 ReferralCodeService）

```java
public ReferralCode validateAndGetReferralCode(String code) {
    // 1. trim + 大寫
    code = code.trim().toUpperCase();
    
    // 2. 查詢推薦碼
    ReferralCode rc = referralCodeRepository.selectByCode(code);
    if (rc == null) return null;
    
    // 3. 檢查活躍狀態
    if (!Boolean.TRUE.equals(rc.getIsActive())) return null;
    
    // 4. 檢查使用次數限制
    if (rc.getMaxUsage() != null && rc.getUsedCount() >= rc.getMaxUsage()) 
        return null;
    
    // 5. 檢查有效期
    if (rc.getExpiresAt() != null && rc.getExpiresAt().isBefore(LocalDateTime.now())) 
        return null;
    
    // 6. 檢查店家活躍狀態
    Store store = storeMapper.selectByPrimaryKey(rc.getStoreId());
    if (store == null || !"ACTIVE".equals(store.getStatus())) 
        return null;
    
    return rc;
}
```

---

## 相關端點

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/api/auth/register` | 使用推薦碼完成註冊 |
| POST | `/api/user/apply-referral` | 已認證用戶補上推薦碼 |
| GET | `/api/referral/validate` | 已認證用戶驗證推薦碼 |

