# 合約：POST /api/user/apply-referral

**已登入用戶補上推薦碼（OAuth 新用戶常用路徑）**

---

## 基本資訊

| 欄位 | 值 |
|-------|------|
| Method | POST |
| Path | `/api/user/apply-referral` |
| Auth | ✓ 需登入 (Bearer Token) |
| Content-Type | application/json |

---

## HTTP 請求

### 請求路徑

```
POST /api/user/apply-referral
Authorization: Bearer {accessToken}
```

### 請求頭

```
Authorization: Bearer eyJhbGc... (JWT Token)
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
| `code` | String | 是 | 推薦碼（1-50 字元） |

---

## HTTP 回應

### 204 No Content — 成功

```
HTTP/1.1 204 No Content
```

無回應體。表示推薦碼已成功綁定。

### 401 Unauthorized — 未登入

```json
{
  "code": 401,
  "message": "未登入或 Token 無效",
  "error": "UNAUTHORIZED"
}
```

### 400 Bad Request — 推薦碼無效

```json
{
  "code": 400,
  "message": "推薦碼無效或已停用",
  "error": "INVALID_REFERRAL_CODE"
}
```

### 409 Conflict — 已綁定推薦碼

```json
{
  "code": 409,
  "message": "推薦碼已綁定，無法修改",
  "error": "REFERRAL_CODE_ALREADY_BOUND"
}
```

### 422 Unprocessable Entity — 自推薦

```json
{
  "code": 422,
  "message": "不能使用自己的推薦碼",
  "error": "SELF_REFERRAL_NOT_ALLOWED"
}
```

---

## 授權要求

### 必要條件

1. **已認證** — 必須提供有效的 JWT Access Token
2. **推薦碼未綁定** — `user.referralCode IS NULL`
3. **非自推薦** — 推薦碼擁有者 ≠ 當前用戶

### 角色要求

- `ROLE_USER` 或 `ROLE_ADMIN`（任何已登入用戶）

---

## 使用場景

此端點用於以下場景：

1. **Google OAuth 新用戶首次登入後，於補碼導覽彈窗中補上推薦碼**
   - 前端收到 `isNewUser=true` 信號
   - 用戶在新用戶導覽中填入推薦碼
   - 呼叫此端點綁定

2. **Email 用戶註冊時遺漏推薦碼，登入後補上**（可選）
   - 用戶在會員設定中補上推薦碼

---

## 使用範例

### JavaScript / fetch

```javascript
async function applyReferralCode(code) {
  const token = localStorage.getItem('accessToken');
  const response = await fetch('/api/user/apply-referral', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ code })
  });
  
  if (response.status === 204) {
    console.log('推薦碼已綁定！');
    return true;
  } else if (response.status === 409) {
    console.log('推薦碼已綁定，無法修改');
    return false;
  } else {
    const error = await response.json();
    console.log('錯誤：', error.message);
    return false;
  }
}
```

### cURL

```bash
curl -X POST http://localhost:8080/api/user/apply-referral \
  -H "Authorization: Bearer {accessToken}" \
  -H "Content-Type: application/json" \
  -d '{"code": "STORE-ABC123"}'
```

---

## 實作說明

### 流程圖

```
POST /api/user/apply-referral { code }
          ↓
1. 從 JWT 提取 userId
          ↓
2. 查詢用戶
          ↓
3. 檢查 user.referralCode != null → 若已綁定 → 409 Conflict
          ↓
4. 驗證推薦碼有效性
   - 存在 + 活躍 + 未超限 + 未過期 + 店家活躍
   → 無效 → 400 Bad Request
          ↓
5. 防自推薦檢查
   → 自推薦 → 422 Unprocessable Entity
          ↓
6. 更新 user 表
   - referral_code = code
   - referred_store_id = store_id
   - referral_bound_at = now()
          ↓
7. 建立 referral_record （signup_method = 'OAUTH'）
          ↓
8. 遞增推薦碼使用計數
          ↓
9. 回傳 204 No Content
```

### Service 實作

```java
@Transactional
public void applyReferralForUser(String userId, String code) {
    // 1. 查詢用戶
    User user = userMapper.selectByPrimaryKey(userId);
    if (user == null) throw new BusinessException("用戶不存在");
    
    // 2. 檢查是否已綁定（一次性防護）
    if (user.getReferralCode() != null && !user.getReferralCode().isEmpty()) {
        throw new BusinessException("推薦碼已綁定，無法修改");
    }
    
    // 3. 驗證推薦碼
    ReferralCode rc = validateAndGetReferralCode(code.toUpperCase());
    if (rc == null) throw new BusinessException("推薦碼無效或已停用");
    
    // 4. 防自推薦
    if (rc.getOwnerId().equals(userId)) {
        throw new BusinessException("不能使用自己的推薦碼");
    }
    
    // 5. 更新用戶
    user.setReferralCode(code.toUpperCase());
    user.setReferredStoreId(rc.getStoreId());
    user.setReferralBoundAt(LocalDateTime.now());
    userMapper.updateByPrimaryKey(user);
    
    // 6. 建立推薦紀錄
    createReferralRecord(user, code.toUpperCase(), "OAUTH");
    
    // 7. 遞增使用計數
    referralCodeMapper.incrementUsageCount(rc.getId());
}
```

---

## 相關端點

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/api/auth/validate-referral` | 驗證推薦碼（無需登入） |
| POST | `/api/auth/register` | 使用推薦碼完成註冊 |
| GET | `/api/user/me` | 查詢個人資訊（含 referralCode） |

