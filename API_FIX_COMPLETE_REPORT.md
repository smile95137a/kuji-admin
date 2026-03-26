# 🔧 API 修復報告

## 修復時間
**2026-01-22 10:53**

---

## 🐛 問題 1：商品查詢空字串問題

### 問題描述
```json
{
  "condition": {
    "storeId": "",
    "status": "",
    "category": "",
    ...
  }
}
```

當傳入**空字串**時，`LotteryServiceImpl.queryLotteries()` 會把空字串當作查詢條件，導致查不到資料。

### 根本原因
```java
// ❌ 錯誤：空字串 "" 會被當作條件
if (condition.getStoreId() != null) {
    criteria.andStoreIdEqualTo(condition.getStoreId()); // "" != null，會執行！
}
```

### 解決方案
✅ **已內建 `isNotBlank()` 方法處理**

```java
// ✅ 正確：空字串視為 null（不過濾）
private boolean isNotBlank(String str) {
    return str != null && !str.trim().isEmpty();
}

if (isNotBlank(condition.getStoreId())) {
    criteria.andStoreIdEqualTo(condition.getStoreId());
}
```

**檔案位置**: `LotteryServiceImpl.java` 第 1506-1509 行

### 驗證狀態
✅ **已確認**：`queryLotteries()` 方法已使用 `isNotBlank()` 判斷所有字串條件

---

## 🆕 問題 2：註冊 API 欄位擴充

### 需求
前端要求註冊時支援以下欄位：

```json
{
  "email": "a0930200677@gmail.com",
  "password": "123456",
  "confirmPassword": "123456",  // ← 新增
  "nickname": "robin",          // ← 改為必填
  "phoneNumber": "0930200677",  // ← 改為必填
  "addressName": "王",           // ← 新增（收件人姓名）
  "zipCode": "103",             // ← 新增
  "city": "臺北市",              // ← 新增（必填）
  "area": "大同區",              // ← 新增（區域，必填）
  "address": "地址地址",         // ← 新增（詳細地址，必填）
  "lineId": "tobinline",        // ← 新增（選填）
  "agreeTerms": true,           // ← 新增（必須為 true）
  "referralCode": "robinRobin"  // ← 保留（選填）
}
```

### 修改內容

#### 1️⃣ AuthRegisterReq.java（DTO）

**新增欄位**:
- `confirmPassword` - 確認密碼（必填）
- `addressName` - 收件人姓名（必填）
- `zipCode` - 郵遞區號（必填，3-5位數字）
- `city` - 縣市（必填）
- `area` - 區域（必填）
- `address` - 詳細地址（必填，最多200字元）
- `lineId` - LINE ID（選填，最多100字元）
- `agreeTerms` - 同意服務條款（必填，必須為 true）

**改為必填**:
- `nickname` - 暱稱
- `phoneNumber` - 手機號碼（台灣格式 09xxxxxxxx）

**完整驗證規則**:
```java
@NotBlank(message = "確認密碼不能為空")
private String confirmPassword;

@NotBlank(message = "收件人姓名不能為空")
@Size(max = 50, message = "收件人姓名不能超過 50 字元")
private String addressName;

@NotBlank(message = "郵遞區號不能為空")
@Pattern(regexp = "^\\d{3,5}$", message = "郵遞區號格式不正確")
private String zipCode;

@NotBlank(message = "縣市不能為空")
private String city;

@NotBlank(message = "區域不能為空")
private String area;

@NotBlank(message = "詳細地址不能為空")
@Size(max = 200, message = "詳細地址不能超過 200 字元")
private String address;

@Size(max = 100, message = "LINE ID 不能超過 100 字元")
private String lineId;

@AssertTrue(message = "必須同意服務條款")
private Boolean agreeTerms;
```

#### 2️⃣ UserServiceImpl.java（Service 層）

**修改註冊邏輯**:
```java
@Override
public User register(AuthRegisterReq req) {
    // ✅ 驗證密碼確認
    if (!req.getPassword().equals(req.getConfirmPassword())) {
        throw new IllegalArgumentException("密碼與確認密碼不一致");
    }
    
    // 檢查 Email 是否已存在...
    
    User user = new User();
    // 基本資訊
    user.setEmail(req.getEmail());
    user.setNickname(req.getNickname()); // ← 不再使用預設值
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    
    // ✅ 手機號碼與 LINE ID
    user.setPhoneNumber(req.getPhoneNumber());
    user.setLineId(req.getLineId());
    
    // ✅ 收件地址資訊
    user.setRecipientName(req.getAddressName());
    user.setCity(req.getCity());
    user.setDistrict(req.getArea()); // area → district（對應資料表欄位）
    user.setAddressDetail(req.getAddress());
    
    // 其他欄位...
    userMapper.insert(user);
    
    // 處理推薦碼...
    return user;
}
```

#### 3️⃣ ApiAuthController.java（Controller 層）

**新增密碼驗證**:
```java
@PostMapping("/register")
@Operation(summary = "使用者註冊", description = "使用 Email 和密碼註冊新帳號")
public ResponseEntity<AuthRes> register(@Valid @RequestBody AuthRegisterReq req) {
    // ✅ Controller 層額外檢查密碼一致性
    if (!req.getPassword().equals(req.getConfirmPassword())) {
        throw new IllegalArgumentException("密碼與確認密碼不一致");
    }
    
    User user = userService.register(req);
    // 返回 Token...
}
```

---

## 📋 資料表對應關係

| 前端欄位 | 資料表欄位 | 類型 | 說明 |
|---------|-----------|------|------|
| `email` | `email` | VARCHAR | Email |
| `password` | `password` | VARCHAR | 加密後的密碼 |
| `nickname` | `nickname` | VARCHAR | 暱稱 |
| `phoneNumber` | `phone_number` | VARCHAR | 手機號碼 |
| `addressName` | `recipient_name` | VARCHAR | 收件人姓名 |
| `city` | `city` | VARCHAR | 縣市 |
| `area` | `district` | VARCHAR | 區域（對應 district） |
| `address` | `address_detail` | VARCHAR | 詳細地址 |
| `lineId` | `line_id` | VARCHAR | LINE ID |
| `referralCode` | - | - | 用於推薦碼邏輯，不直接存入 User 表 |
| `zipCode` | - | - | **User 表目前無此欄位** |

⚠️ **注意**：`zipCode` 欄位目前 User 資料表沒有對應欄位，如果需要儲存請先執行 SQL：

```sql
ALTER TABLE `user` ADD COLUMN `zip_code` VARCHAR(10) COMMENT '郵遞區號' AFTER `district`;
```

---

## 🧪 測試腳本

### 檔案位置
`test-register-api.bat`

### 測試案例

#### 測試 1：完整註冊資料
```json
{
  "email": "test@example.com",
  "password": "123456",
  "confirmPassword": "123456",
  "nickname": "測試用戶",
  "phoneNumber": "0912345678",
  "addressName": "王小明",
  "zipCode": "103",
  "city": "臺北市",
  "area": "大同區",
  "address": "測試路123號",
  "lineId": "testline",
  "agreeTerms": true,
  "referralCode": "TESTCODE"
}
```

**預期結果**: ✅ 註冊成功，返回 Token

#### 測試 2：密碼不一致
```json
{
  "password": "123456",
  "confirmPassword": "654321",
  ...
}
```

**預期結果**: ❌ 400 Bad Request - "密碼與確認密碼不一致"

#### 測試 3：缺少必填欄位
```json
{
  "email": "test3@example.com",
  "password": "123456",
  "confirmPassword": "123456"
  // 缺少 nickname, phoneNumber, addressName 等
}
```

**預期結果**: ❌ 400 Bad Request - "暱稱不能為空" / "手機號碼不能為空"

---

## 📁 修改檔案清單

1. ✅ `AuthRegisterReq.java` - 新增 8 個欄位，2 個改為必填
2. ✅ `UserServiceImpl.java` - 更新註冊邏輯，處理新欄位
3. ✅ `ApiAuthController.java` - 新增密碼驗證，新增 @Operation 文檔
4. ✅ `test-register-api.bat` - 新增測試腳本

---

## ✅ 驗證清單

### 問題 1（空字串查詢）
- [x] 確認 `isNotBlank()` 方法已實作
- [x] 確認 `queryLotteries()` 使用 `isNotBlank()` 判斷所有字串條件
- [x] 空字串 `""` 會被視為 `null`（不過濾）

### 問題 2（註冊 API）
- [x] `confirmPassword` 欄位已新增並驗證
- [x] `nickname` 改為必填（移除預設值邏輯）
- [x] `phoneNumber` 改為必填且格式驗證
- [x] `addressName`, `city`, `area`, `address` 新增並驗證
- [x] `lineId` 新增（選填）
- [x] `agreeTerms` 新增並驗證必須為 `true`
- [x] `zipCode` 新增但**未存入資料表**（需要更新 schema）
- [x] 密碼一致性在 Controller 和 Service 雙重驗證
- [x] User entity 欄位對應正確（`area` → `district`）

---

## 🚀 下一步

### 1️⃣ 測試商品查詢 API
```cmd
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes/list ^
  -H "Authorization: Bearer <TOKEN>" ^
  -H "Content-Type: application/json" ^
  -d "{\"condition\":{\"storeId\":\"\",\"status\":\"\"}}"
```

**預期結果**: 返回所有商品（不過濾 storeId 和 status）

### 2️⃣ 測試註冊 API
```cmd
test-register-api.bat
```

**預期結果**: 
- 測試 1 ✅ 通過
- 測試 2 ✅ 正確攔截
- 測試 3 ✅ 正確攔截

### 3️⃣ 資料表更新（選做）
如果需要儲存 `zipCode`，執行：
```sql
ALTER TABLE `user` ADD COLUMN `zip_code` VARCHAR(10) COMMENT '郵遞區號' AFTER `district`;
```

然後更新 User.java 和 UserMapper.xml。

---

## 📊 影響範圍

### 前端需要調整
1. ✅ 註冊表單新增必填欄位
2. ✅ 新增密碼確認輸入框
3. ✅ 新增同意服務條款 checkbox
4. ✅ 調整錯誤訊息處理（新的驗證訊息）

### 後端無需調整
- ✅ 查詢 API 已經支援空字串處理
- ✅ 註冊 API 已完全支援新欄位

---

**報告產生時間**: 2026-01-22 11:00  
**版本**: 2.1.0  
**狀態**: ✅ 所有修改完成，待測試
