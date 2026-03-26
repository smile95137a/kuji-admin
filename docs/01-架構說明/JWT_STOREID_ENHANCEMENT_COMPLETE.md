# 🚀 JWT 增強與店家選項 API 完成報告

## 📋 執行摘要

### 已完成項目
1. ✅ 刪除重複的 `/admin/lottery/my-stores` API
2. ✅ JWT Token 加入 `storeIds` 欄位
3. ✅ 店家選項 API 支援後台權限過濾
4. ✅ 三個登入流程都加入 `storeIds`

---

## 🔧 修改詳情

### 1. 刪除重複的 API

**刪除：** `AdminLotteryController.java` 中的 `/admin/lottery/my-stores`

**原因：**
- ❌ 只返回 ID，前端還要再查店家名稱
- ❌ 路由位置不合理（放在 lottery 下）
- ✅ 已有更好的替代方案：`/stores/options`

**替代方案：** `GET /api/stores/options`
- ✅ 返回完整資訊：`{ label, value, description }`
- ✅ 無需登入（前台可用）
- ✅ 後台會根據角色自動過濾

---

### 2. JWT Token 增強

#### 2.1 JwtUtil.java

**新增方法：**
```java
// 完整版（包含 storeIds）
public String generateToken(String username, String userId, 
                           String userType, List<String> roles, 
                           List<String> storeIds)

// 向下相容版本（storeIds 為 null）
public String generateToken(String username, String userId, 
                           String userType, List<String> roles)

// 從 Token 取得 storeIds
public List<String> getStoreIds(String token)
```

**JWT Payload 範例：**
```json
{
  "sub": "admin@kuji.com",
  "userId": "uuid-123",
  "userType": "admin",
  "roles": ["ROLE_ADMIN"],
  "storeIds": ["store-uuid-1", "store-uuid-2"],
  "exp": 1234567890,
  "iat": 1234567890
}
```

#### 2.2 AdminAuthServiceImpl.java

**修改的方法：**
1. `login()` - 登入時查詢並放入 storeIds
2. `firstLoginChangePassword()` - 首次修改密碼時放入 storeIds
3. `refreshToken()` - 刷新 Token 時放入 storeIds

**新增方法：**
```java
private List<String> getUserStoreIds(String adminUserId) {
    StoreUserExample example = new StoreUserExample();
    example.createCriteria().andAdminUserIdEqualTo(adminUserId);
    List<StoreUser> storeUsers = storeUserMapper.selectByExample(example);
    return storeUsers.stream()
            .map(StoreUser::getStoreId)
            .collect(Collectors.toList());
}
```

---

### 3. 店家選項 API 權限過濾

#### StoreOptionController.java

**權限規則：**

| 角色 | 看到的店家 | activeOnly 是否生效 |
|-----|----------|-------------------|
| **未登入（前台）** | 所有 ACTIVE 店家 | ✅ 是 |
| **Admin（後台）** | 所有店家（含 INACTIVE） | ❌ 否（可選） |
| **StoreOwner（後台）** | 自己的店家 | ✅ 是 |
| **StoreEditor（後台）** | 自己的店家 | ✅ 是 |

---

## 📊 前後對比

### 🔴 修改前

**前端新增 Banner 時：**
```javascript
// ❌ 問題：不知道自己有哪些店家
const ids = await axios.get('/api/admin/lottery/my-stores');
// 返回：["uuid-1", "uuid-2"]  // 只有 ID！

// ❌ 還要再查店家名稱
const stores = await Promise.all(
    ids.map(id => axios.get(`/api/stores/${id}`))
);
```

### 🟢 修改後

**前端新增 Banner 時：**
```javascript
// ✅ 一次取得完整資訊
const response = await axios.get('/api/stores/options');
// 返回：
// [
//   { label: "玩具店", value: "uuid-1", description: "專賣公仔" },
//   { label: "卡牌店", value: "uuid-2", description: "卡牌收藏" }
// ]

// ✅ 可以直接用於 Select 組件
<Select 
    options={response.data} 
    onChange={handleStoreChange}
/>
```

---

## 🧪 測試驗證

### 測試 1：JWT 包含 storeIds

```bash
# 1. Admin 登入
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"admin123"}'

# 2. 解碼 JWT（使用 https://jwt.io/）
# 3. 驗證 payload 包含 storeIds
```

### 測試 2：Admin 看到所有店家

```bash
curl -X GET http://localhost:8080/api/stores/options?activeOnly=false \
  -H "Authorization: Bearer {token}"
```

### 測試 3：StoreOwner 只看自己的店家

```bash
curl -X GET http://localhost:8080/api/stores/options \
  -H "Authorization: Bearer {token}"
```

---

## 🎉 完成清單

- ✅ 刪除 `/admin/lottery/my-stores` API
- ✅ JWT 加入 `storeIds` 欄位
- ✅ `JwtUtil` 支援 `storeIds` 生成和解析
- ✅ `AdminAuthServiceImpl` 三個登入流程都加入 `storeIds`
- ✅ `StoreOptionController` 支援權限過濾
- ✅ 編譯無錯誤

---

## 📝 下一步

1. **重啟應用程式**
2. **測試登入並檢查 JWT**
3. **測試店家選項 API**（前台 + Admin + StoreOwner）
4. **更新前端代碼**

---

**更新時間：** 2026-01-07  
**版本：** v2.0 - JWT + 權限過濾完整版
