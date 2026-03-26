# 問題診斷報告

## 問題 1：查詢商品返回「商品不存在」

### 症狀
```http
POST /api/admin/lottery/list
Authorization: Bearer {owner-token}

返回：
{
  "error": {
    "code": "BIZ_ERROR",
    "message": "商品不存在"
  }
}
```

### 可能原因

#### 原因 1：資料庫中沒有商品
```sql
SELECT COUNT(*) FROM lottery;
-- 如果返回 0，表示沒有商品資料
```

**解決方案**：
- 先用 Admin 帳號新增測試商品
- 或執行 DataInitializer 初始化測試資料

#### 原因 2：store_user 表沒有關聯
```sql
SELECT * FROM store_user 
WHERE admin_user_id = '424a9835-a0b8-4257-9a3e-be51b1d5fc43';
-- 如果返回 0 筆，表示該使用者沒有綁定店家
```

**解決方案**：
```sql
-- 手動建立關聯（假設 store_id 為 xxx）
INSERT INTO store_user (id, store_id, admin_user_id, role_type, created_at)
VALUES (UUID(), '{STORE_ID}', '424a9835-a0b8-4257-9a3e-be51b1d5fc43', 'OWNER', NOW());
```

#### 原因 3：Service 層拋出異常

**檢查點**：
1. `LotteryServiceImpl.queryLotteries()` 第 773 行
2. `convertToResNew()` 方法是否有空指標異常

**可能問題**：
```java
// 如果 lottery.getMultiDrawOptions() 為 null
res.setMultiDrawOptions(null);  // 可能導致錯誤

// 如果 lottery.getCategory() 查不到對應 Enum
LotteryCategoryEnum.getNameByCode(null);  // 可能拋出異常
```

---

## 問題 2：無法新增帳號

### 症狀
```http
POST /api/admin/users/store-owner
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "email": "test@example.com",
  "displayName": "測試店家",
  "phone": "0912345678",
  "storeName": "測試商店",
  "shortDescription": "測試用店家"
}

返回：???（未提供）
```

### 需要確認

1. **請求內容是什麼？**
2. **返回的錯誤訊息是什麼？**
3. **是否有權限？**（必須是 Admin 帳號）

### 可能原因

#### 原因 1：Email 已存在
```json
{
  "error": {
    "code": "USER_EMAIL_EXISTS",
    "message": "Email 已被使用"
  }
}
```

**解決方案**：換一個 Email

#### 原因 2：權限不足
```json
{
  "error": {
    "code": "ACCESS_DENIED",
    "message": "權限不足"
  }
}
```

**解決方案**：使用 Admin 帳號登入

#### 原因 3：資料庫連線失敗
```json
{
  "error": {
    "code": "INTERNAL_SERVER_ERROR",
    "message": "資料庫錯誤"
  }
}
```

**解決方案**：檢查資料庫連線

#### 原因 4：必填欄位缺少
```json
{
  "error": {
    "code": "COMMON_VALIDATION_001",
    "message": "欄位驗證失敗"
  }
}
```

**檢查 CreateStoreOwnerReq 必填欄位**：
```java
@NotBlank(message = "Email 不可為空")
private String email;

@NotBlank(message = "店家名稱不可為空")
private String storeName;

// 檢查你的請求是否包含所有必填欄位
```

---

## 立即診斷步驟

### 步驟 1：執行 SQL 診斷腳本
```bash
# 連線到資料庫
mysql -u root -p kuji_admin

# 執行診斷腳本
source diagnose-lottery-query.sql
```

### 步驟 2：查看應用程式日誌
```bash
# 查看最後 100 行日誌
powershell -Command "Get-Content app.log -Tail 100"

# 或用編輯器開啟
notepad app.log
```

### 步驟 3：測試 API（提供完整資訊）

#### 測試查詢商品
```bash
# 1. 登入 owner@teststore.com
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"owner@teststore.com","password":"Test1234"}' \
  -o owner_login.json

# 2. 複製 Token 並查詢商品
curl -X POST http://localhost:8080/api/admin/lottery/list \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{}' \
  -v

# 查看完整回應（包含 HTTP 狀態碼、Header、Body）
```

#### 測試新增帳號
```bash
# 1. 登入 Admin
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"admin123"}' \
  -o admin_login.json

# 2. 複製 Token 並新增帳號
curl -X POST http://localhost:8080/api/admin/users/store-owner \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "email": "newowner@test.com",
    "displayName": "新店家老闆",
    "phone": "0987654321",
    "storeName": "新測試商店",
    "shortDescription": "這是測試用店家"
  }' \
  -v
```

---

## 快速修正建議

### 如果是「商品不存在」問題

**方案 1：檢查 Service 層異常處理**
```java
// LotteryServiceImpl.queryLotteries()
try {
    List<Lottery> lotteries = lotteryMapper.selectByExample(example);
    log.info("✅ 查詢成功: 共 {} 筆", lotteries.size());
    
    return lotteries.stream()
            .map(this::convertToResNew)
            .collect(Collectors.toList());
} catch (Exception e) {
    log.error("❌ 查詢失敗", e);
    throw new BusinessException("查詢失敗: " + e.getMessage());
}
```

**方案 2：檢查資料庫關聯**
```sql
-- 確認 owner@teststore.com 有綁定店家
SELECT * FROM store_user 
WHERE admin_user_id = '424a9835-a0b8-4257-9a3e-be51b1d5fc43';

-- 如果沒有，手動建立
INSERT INTO store_user (id, store_id, admin_user_id, role_type, created_at)
SELECT 
    UUID(),
    s.id,
    '424a9835-a0b8-4257-9a3e-be51b1d5fc43',
    'OWNER',
    NOW()
FROM store s
WHERE s.email = 'owner@teststore.com'
LIMIT 1;
```

---

## 需要你提供的資訊

1. **查詢商品問題**：
   - [ ] 執行 SQL 診斷腳本的結果
   - [ ] app.log 中的完整錯誤訊息
   - [ ] 是否有成功新增過商品？

2. **新增帳號問題**：
   - [ ] 完整的請求內容（JSON）
   - [ ] 完整的錯誤回應（JSON）
   - [ ] 使用的 Token 是 Admin 還是 StoreOwner？

請提供以上資訊，我才能精確定位問題！
