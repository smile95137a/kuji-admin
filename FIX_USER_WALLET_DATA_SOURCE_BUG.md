# 🚨 User 表 goldCoins/bonusCoins 欄位廢棄公告

## 問題診斷

### Bug 描述
**同一個會員在前後台看到不同的金幣餘額：**
- 前台 API (`GET /api/user/me`) 返回：`goldCoins: 0, bonusCoins: 0`
- 後台 API (`GET /admin/user/{id}`) 返回：`goldCoins: 25000, bonusCoins: 300`
- 錢包 API (`GET /api/wallet`) 返回：`goldCoins: 0, bonusCoins: 0`

### 根本原因
**資料來源混亂：系統同時維護兩個儲存金幣的地方**

1. **User 表** (`user`) - **已廢棄，但還在用**
   ```sql
   CREATE TABLE user (
       id VARCHAR(36) PRIMARY KEY,
       email VARCHAR(255),
       gold_coins BIGINT DEFAULT 0,    -- ❌ 廢棄欄位（但還有資料）
       bonus_coins BIGINT DEFAULT 0,   -- ❌ 廢棄欄位（但還有資料）
       ...
   );
   ```

2. **UserWallet 表** (`user_wallet`) - **正確的唯一來源**
   ```sql
   CREATE TABLE user_wallet (
       id VARCHAR(36) PRIMARY KEY,
       user_id VARCHAR(36) NOT NULL,
       gold_coins BIGINT DEFAULT 0,    -- ✅ 正確的來源
       bonus_coins BIGINT DEFAULT 0,   -- ✅ 正確的來源
       ...
   );
   ```

### 問題程式碼

**前台 UserController（錯誤）：**
```java
// ❌ 錯誤：從 user.getGoldCoins() 取值（User 表的欄位）
UserRes res = UserRes.from(user);
res.setGoldCoins(wallet.getGoldCoins());  // 雖然有這行，但 UserRes.from() 已經設了錯的值
```

**UserRes.from()（錯誤）：**
```java
// ❌ 錯誤：直接從 User entity 取 goldCoins
.goldCoins(user.getGoldCoins())   // User 表的 gold_coins 欄位（0）
.bonusCoins(user.getBonusCoins()) // User 表的 bonus_coins 欄位（0）
```

**後台 FrontendUserServiceImpl（正確）：**
```java
// ✅ 正確：從 user_wallet 表查詢
UserWalletExample walletExample = new UserWalletExample();
walletExample.createCriteria().andUserIdEqualTo(user.getId());
List<UserWallet> wallets = userWalletMapper.selectByExample(walletExample);
UserWallet wallet = wallets.isEmpty() ? null : wallets.get(0);

res.setGoldCoins(wallet != null ? wallet.getGoldCoins() : 0L);  // 25000
res.setBonusCoins(wallet != null ? wallet.getBonusCoins() : 0L); // 300
```

---

## 解決方案

### 1. 立即修正：統一從 user_wallet 查詢

**修正 UserRes.from()**：
```java
public static UserRes from(User user) {
    return UserRes.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            // ...
            .goldCoins(null)   // ← 不從 User 表取！由 Controller 設定
            .bonusCoins(null)  // ← 不從 User 表取！由 Controller 設定
            .build();
}
```

**修正 UserController.me()**：
```java
@GetMapping("/me")
public ResponseEntity<UserRes> me() {
    String userId = SecurityUtils.getCurrentUserId();
    User user = userService.findById(userId);
    
    // ✅ 從 user_wallet 表查詢餘額
    UserWalletRes wallet = walletService.getWallet(userId);
    
    // ✅ 手動設定（不從 User entity 取）
    UserRes res = UserRes.from(user);
    res.setGoldCoins(wallet.getGoldCoins());   // ← 25000
    res.setBonusCoins(wallet.getBonusCoins()); // ← 300
    
    return ResponseEntity.ok(res);
}
```

### 2. 擴充 UserRes：補齊所有會員資料

**新增欄位**：
```java
@Data
@Builder
public class UserRes {
    // 基本資訊
    private String id;
    private String email;
    private String nickname;
    private String avatarUrl;
    private String provider;
    private String status;
    
    // 錢包（從 user_wallet 表）
    private Long goldCoins;
    private Long bonusCoins;
    
    // 聯絡資訊
    private String phoneNumber;
    private String lineId;
    
    // 收件資訊（用於下訂單）
    private String recipientName;      // ← 新增
    private String recipientPhone;     // ← 新增
    private String city;               // ← 新增
    private String district;           // ← 新增
    private String addressDetail;      // ← 新增
    
    // 發票資訊（用於下訂單）
    private String invoiceType;        // ← 新增
    private String invoiceEmail;       // ← 新增
    private String carrierCode;        // ← 新增
    private String taxId;              // ← 新增
    private String companyName;        // ← 新增
    
    // 推薦資訊
    private String referralCode;       // ← 新增
    private String referredStoreId;    // ← 新增
    
    // 時間戳
    private Boolean emailVerified;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 3. 擴充 FrontendUserUpdateReq：支援完整編輯

**新增可編輯欄位**：
```java
@Data
public class FrontendUserUpdateReq {
    private String email;
    private String nickname;
    private String avatar;
    
    // 聯絡資訊
    private String phoneNumber;
    private String lineId;
    
    // 收件資訊
    private String recipientName;
    private String recipientPhone;
    private String city;
    private String district;
    private String addressDetail;
    
    // 發票資訊
    private String invoiceType;
    private String invoiceEmail;
    private String carrierCode;
    private String taxId;
    private String companyName;
}
```

**更新 Controller 支援所有欄位**：
```java
@PutMapping("/me")
public ResponseEntity<UserRes> updateMe(@Valid @RequestBody FrontendUserUpdateReq req) {
    // ... 前面省略
    
    if (req.getPhoneNumber() != null) {
        user.setPhoneNumber(req.getPhoneNumber());
        updated = true;
    }
    
    if (req.getRecipientName() != null) {
        user.setRecipientName(req.getRecipientName());
        updated = true;
    }
    
    if (req.getCity() != null) {
        user.setCity(req.getCity());
        updated = true;
    }
    
    // ... 依此類推所有欄位
}
```

---

## API 回應範例（修正後）

### GET /api/user/me

**Before（錯誤）**：
```json
{
  "success": true,
  "data": {
    "id": "c12abe38-264a-441b-9412-ff5d20fc0a07",
    "email": "user2@test.com",
    "nickname": "測試會員B",
    "goldCoins": 0,          // ❌ 錯誤：從 user 表取得
    "bonusCoins": 0          // ❌ 錯誤：從 user 表取得
  }
}
```

**After（正確）**：
```json
{
  "success": true,
  "data": {
    "id": "c12abe38-264a-441b-9412-ff5d20fc0a07",
    "email": "user2@test.com",
    "nickname": "測試會員B",
    "avatarUrl": "https://via.placeholder.com/100",
    "provider": "EMAIL",
    "status": "ACTIVE",
    "goldCoins": 25000,              // ✅ 正確：從 user_wallet 表取得
    "bonusCoins": 300,               // ✅ 正確：從 user_wallet 表取得
    "phoneNumber": "0966666666",
    "lineId": null,
    "recipientName": "王小明",
    "recipientPhone": "0912345678",
    "city": "台北市",
    "district": "大安區",
    "addressDetail": "信義路四段1號",
    "invoiceType": "CARRIER",
    "carrierCode": "/ABC1234",
    "emailVerified": false,
    "lastLoginAt": "2026-01-27T02:58:12",
    "createdAt": "2025-12-23T05:50:12",
    "updatedAt": "2026-01-26T18:56:10"
  }
}
```

---

## 長期方案：移除 User 表的 gold_coins/bonus_coins 欄位

### Migration SQL（待部署後執行）

```sql
-- ⚠️ 警告：執行前請先備份資料庫！
-- ⚠️ 確認所有程式碼都已改為從 user_wallet 查詢後才能執行

-- 1. 檢查是否有程式碼還在使用 user.gold_coins
-- grep -r "user.getGoldCoins()" src/

-- 2. 確認無誤後，移除欄位
ALTER TABLE user DROP COLUMN gold_coins;
ALTER TABLE user DROP COLUMN bonus_coins;

-- 3. 驗證
SHOW COLUMNS FROM user;
```

---

## 驗證檢查清單

### 前台 API
- [ ] `GET /api/user/me` 返回正確的 `goldCoins` 和 `bonusCoins`（25000, 300）
- [ ] 回應包含完整的收件資訊（recipientName, city, district, addressDetail）
- [ ] 回應包含發票資訊（invoiceType, carrierCode）
- [ ] `PUT /api/user/me` 可以更新 phoneNumber
- [ ] `PUT /api/user/me` 可以更新收件地址
- [ ] `PUT /api/user/me` 可以更新發票資訊

### 後台 API
- [ ] `GET /admin/user/{id}` 返回的 goldCoins 與前台一致

### 錢包 API
- [ ] `GET /api/wallet` 返回的 goldCoins 與前台一致

---

## 修正文件

- `UserRes.java` - 擴充所有欄位
- `FrontendUserUpdateReq.java` - 支援完整編輯
- `UserController.java` - 從 user_wallet 查詢餘額 + 支援完整更新
- `FIX_USER_WALLET_DATA_SOURCE_BUG.md` - 本文件

---

**修正完成時間**：2026-01-27  
**影響範圍**：前台使用者 API + 後台會員管理  
**優先級**：🔥 P0（資料不一致，影響用戶下單）
