# 第三方一番賞整合平台 - 完整系統需求 Prompt

## 🎯 系統整體概念

你是一個專業的 Spring Boot 後端開發專家，負責開發「第三方一番賞整合平台」。

### 系統定位
- **核心目標**: 提供線下店家使用線上抽獎功能的整合平台
- **抽獎模式**: 固定數量池 + 翻牌結算（抽完即結束）
- **技術棧**: Spring Boot 3.3.3 + MyBatis + JWT + Spring Security + MySQL 8.3.0

### 三大角色
1. **一般會員（前台玩家）**: 儲值點數、線上抽獎
2. **店家（後台管理者）**: 建立抽獎商品、管理訂單
3. **平台管理者（最高權限）**: 管理所有店家與系統設定

---

## 👥 會員系統架構

### 1. 前台會員（User）

**資料表**: `user`

**核心欄位**:
```java
private String id;              // UUID
private String email;           // 登入帳號
private String nickname;        // 顯示名稱
private String password;        // BCrypt 加密
private String avatar;          // 頭像 URL
private Long goldCoins;         // 儲值金（主要消費點數）
private Long bonusCoins;        // 紅利金（活動贈點）
private String status;          // ACTIVE, INACTIVE, BANNED
private String provider;        // LOCAL, GOOGLE
private LocalDateTime createDate;
private LocalDateTime updateDate;
```

**登入方式**:
- ✅ Email + 密碼
- ✅ Google OAuth 2.0
- ✅ JWT Token (Access + Refresh)

**特性**:
- 可自助註冊
- 不進入後台系統
- 擁有點數系統
- 無角色權限概念

---

### 2. 後台會員（AdminUser）

**資料表**: `admin_user`

**核心欄位**:
```java
private String id;              // UUID
private String username;        // 登入帳號
private String password;        // BCrypt 加密
private Integer status;         // 1=啟用, 0=停用
private String storeId;         // 所屬店家ID（Admin為null）
private LocalDateTime lastLogin;
private LocalDateTime createDate;
private LocalDateTime updateDate;
```

**登入方式**:
- ✅ Email/Username + 密碼
- ❌ 不支援 Google OAuth
- ✅ JWT Token (Access + Refresh)

**特性**:
- 不能自助註冊（由 Admin 建立）
- 使用 RBAC 權限系統
- 無點數機制

---

### 3. 三種固定角色（Role）

#### 3.1 系統最高權限者（Admin）
**權限範圍**:
- ✅ 管理所有店家帳號
- ✅ 調整角色對應的 Menu 權限
- ✅ 新增/停用店家帳號
- ✅ 設定店家小編帳號
- ✅ 調整後台整體菜單列表
- ✅ 查看所有數據報表

**資料特性**:
- `storeId = null`
- 擁有所有 Menu 權限

#### 3.2 店家（StoreOwner）
**權限範圍**:
- ✅ 管理自己店家的資料
- ✅ 建立/編輯抽獎商品
- ✅ 管理訂單與獎項
- ✅ 查看自己店家的報表
- ✅ 建立店家小編帳號
- ❌ 無法看到其他店家資料

**資料特性**:
- `storeId = {自己的店家ID}`
- 權限由 `role_menu` 控制

#### 3.3 店家小編（StoreEditor）
**權限範圍**:
- ✅ 商品管理
- ✅ 獎項管理
- ✅ 訂單管理（部分）
- ❌ 無法查看財務報表
- ❌ 無法設定高權限介面

**資料特性**:
- 擁有獨立的 `admin_user` 帳號（不是子帳號）
- `storeId = {所屬店家ID}`
- 只能看到 Owner 允許的部分 Menu

---

## 🔐 RBAC 權限系統

### 資料表結構

#### role（角色表）
```sql
CREATE TABLE role (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,              -- Admin, StoreOwner, StoreEditor
    description VARCHAR(255),
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### menu（菜單表）
```sql
CREATE TABLE menu (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,             -- 菜單名稱
    path VARCHAR(255),                      -- 路由路徑
    parent_id CHAR(36),                     -- 父菜單ID（階層化）
    icon VARCHAR(50),                       -- 圖標
    order_num INT DEFAULT 0,                -- 排序
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### admin_user_role（使用者-角色關聯表）
```sql
CREATE TABLE admin_user_role (
    admin_id CHAR(36) NOT NULL,
    role_id CHAR(36) NOT NULL,
    PRIMARY KEY (admin_id, role_id)
);
```

#### role_menu（角色-菜單關聯表）
```sql
CREATE TABLE role_menu (
    role_id CHAR(36) NOT NULL,
    menu_id CHAR(36) NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);
```

### 權限判斷邏輯
1. 使用者登入 → 查詢 `admin_user_role` 取得角色列表
2. 根據角色 ID → 查詢 `role_menu` 取得可用菜單
3. 前端根據菜單列表動態渲染後台介面
4. 後端 API 使用 `@RequirePermission` 註解驗證權限

---

## 🏪 帳號建立與管理流程

### 後台：店家帳號建立流程

```
步驟1: 店家提供 email / basic info
  ↓
步驟2: Admin 在後台新增 admin_user（role = StoreOwner）
  ↓
步驟3: 系統自動生成初始密碼並寄送 Email
  ↓
步驟4: 店家首次登入強制修改密碼
  ↓
步驟5: 店家可新增店家小編帳號（選擇加入 StoreEditor role）
```

**實作要點**:
- ❌ 店家不能自行註冊
- ❌ 不需要審核流程
- ✅ 系統寄送初始密碼
- ✅ 首次登入強制改密碼

### 前台：玩家帳號流程

```
方案A: Email 註冊
  - 填寫 email, password, nickname
  - 系統發送驗證信（可選）
  - 註冊成功自動登入

方案B: Google OAuth
  - Google 授權
  - 取得 email, name, avatar
  - 自動建立 user 帳號
  - 註冊成功自動登入
```

**實作要點**:
- ✅ 可自助註冊
- ✅ 支援 Google OAuth
- ✅ 前台資料存在 `user` 表（獨立於後台）

---

## 🔑 登入方式與安全機制

### 後台登入（AdminUser）

**登入流程**:
```java
POST /admin/auth/login
{
  "username": "admin@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "expiresIn": 86400,
    "user": {
      "id": "uuid",
      "username": "admin@example.com",
      "roles": ["Admin"]
    }
  }
}
```

**Token 設定**:
- Access Token: 24 小時
- Refresh Token: 30 天
- 使用 JWT (JJWT 0.9.1)
- Secret Key: 從 `application.yml` 讀取

**Security Filter 路徑**:
- `/admin/**` → AdminJwtAuthenticationFilter

### 前台登入（User）

**登入流程**:
```java
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "expiresIn": 86400,
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "nickname": "玩家暱稱",
      "goldCoins": 1000,
      "bonusCoins": 500
    }
  }
}
```

**Google OAuth 流程**:
```java
POST /api/auth/google
{
  "idToken": "Google ID Token"
}

處理邏輯:
1. 驗證 Google ID Token
2. 取得 email, name, picture
3. 查詢 user 是否存在
4. 不存在 → 自動建立帳號
5. 返回 JWT Token
```

**Security Filter 路徑**:
- `/api/**` → ApiJwtAuthenticationFilter

---

## 💰 點數機制（前台 User）

### 點數類型

#### Gold Coins（儲值金）
- **來源**: 玩家儲值
- **用途**: 主要消費點數（抽獎）
- **特性**: 可儲值、不過期

#### Bonus Coins（紅利金）
- **來源**: 活動贈送、簽到獎勵
- **用途**: 輔助消費點數
- **特性**: 可能有使用期限

### 資料儲存方式

**方案 A: 雙軌制（推薦）**
```sql
-- user 表儲存當前餘額
user.gold_coins = 1000
user.bonus_coins = 500

-- point_log 表紀錄所有異動
point_log {
  id, user_id, type, amount, 
  before_balance, after_balance, 
  remark, create_date
}
```

**優點**:
- 快速查詢當前餘額
- 完整異動紀錄
- 易於對帳

### 點數異動類型

```java
public enum PointLogType {
    DEPOSIT,    // 儲值
    DEDUCT,     // 扣點（抽獎）
    REFUND,     // 退款
    REWARD,     // 獎勵（活動贈點）
    EXPIRE      // 過期扣除
}
```

---

## 📊 會員系統需求總結表

| 項目 | 前台（User） | 後台（AdminUser） |
|------|-------------|------------------|
| 資料表 | `user` | `admin_user` |
| 使用者類型 | 玩家 | Admin / StoreOwner / StoreEditor |
| 自助註冊 | ✅ Yes | ❌ No（由 Admin 建立） |
| 登入方式 | Email / Google | Email |
| Refresh Token | ✅ Yes | ✅ Yes |
| 點數系統 | ✅ Gold / Bonus | ❌ 無 |
| 權限管理 | ❌ 無 | ✅ RBAC（role + menu） |
| Security Filter | ApiJwtAuthenticationFilter | AdminJwtAuthenticationFilter |
| 路徑前綴 | `/api/**` | `/admin/**` |

---

## 🛠️ 實作指引

### 必要的 Entity 類別

```java
// 前台
- User.java
- PointLog.java

// 後台  
- AdminUser.java
- Role.java
- Menu.java
- AdminUserRole.java
- RoleMenu.java
```

### 必要的 Service 類別

```java
// 認證服務
- UserService.java          // 前台註冊/登入
- AdminAuthService.java     // 後台登入
- GoogleOAuthService.java   // Google OAuth

// 權限服務
- RoleService.java          // 角色管理
- MenuService.java          // 菜單管理
- PermissionService.java    // 權限檢查

// 點數服務
- PointService.java         // 點數儲值/扣款
```

### 必要的 Security 配置

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) {
        // 處理 /admin/** 路徑
        // 使用 AdminJwtAuthenticationFilter
    }
    
    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) {
        // 處理 /api/** 路徑
        // 使用 ApiJwtAuthenticationFilter
    }
}
```

### API 路徑規劃

```
後台 API:
POST   /admin/auth/login           # 後台登入
POST   /admin/auth/refresh         # 刷新 Token
GET    /admin/users                # 查詢後台使用者列表
POST   /admin/users                # 新增店家帳號
PUT    /admin/users/{id}           # 更新使用者資訊
GET    /admin/roles                # 查詢角色列表
GET    /admin/menus                # 查詢菜單列表

前台 API:
POST   /api/auth/register          # 註冊
POST   /api/auth/login             # 登入
POST   /api/auth/google            # Google OAuth
POST   /api/auth/refresh           # 刷新 Token
GET    /api/user/profile           # 查詢個人資料
GET    /api/user/points            # 查詢點數餘額
POST   /api/points/deposit         # 儲值點數
```

---

## ✅ 開發檢查清單

### 會員系統
- [ ] User Entity 包含所有必要欄位（id, email, goldCoins, bonusCoins, etc.）
- [ ] AdminUser Entity 包含所有必要欄位（id, username, storeId, etc.）
- [ ] Role, Menu, AdminUserRole, RoleMenu Entity 建立完成
- [ ] 所有 Entity 使用 @Data @Builder @NoArgsConstructor @AllArgsConstructor

### 認證系統
- [ ] JwtUtil 實作 generateToken(), getUsername(), validateToken()
- [ ] AdminJwtAuthenticationFilter 實作完成
- [ ] ApiJwtAuthenticationFilter 實作完成
- [ ] SecurityConfig 設定兩個 FilterChain（@Order 1, 2）
- [ ] UserPrincipal 支援前後台使用者

### 註冊/登入
- [ ] 前台 Email 註冊功能
- [ ] 前台 Email 登入功能
- [ ] 前台 Google OAuth 登入功能
- [ ] 後台 Email 登入功能
- [ ] 首次登入強制改密碼功能

### RBAC 權限
- [ ] 三個預設角色建立（Admin, StoreOwner, StoreEditor）
- [ ] Menu 資料建立與階層關係
- [ ] 權限檢查註解 @RequirePermission 實作
- [ ] 動態菜單 API 實作

### 點數系統
- [ ] PointLog Entity 建立
- [ ] 點數儲值功能
- [ ] 點數扣款功能
- [ ] 點數查詢功能
- [ ] 點數紀錄查詢功能

---

## 🎓 使用此 Prompt 的方式

將此 Prompt 提供給 AI 助手時，請說明：

**情境 1: 新專案開發**
```
請根據上述「第三方一番賞整合平台」的完整系統需求，幫我：
1. 建立所有必要的 Entity 類別
2. 實作前後台分離的認證系統
3. 實作 RBAC 權限管理
4. 實作 Google OAuth 登入
```

**情境 2: 功能擴充**
```
我的專案已有基礎架構，請根據上述需求幫我：
1. 新增店家小編（StoreEditor）角色
2. 實作動態菜單系統
3. 確保權限控制正確運作
```

**情境 3: 除錯修復**
```
我的專案編譯失敗，錯誤訊息如下：
[貼上錯誤訊息]

請根據上述系統需求，幫我修復編譯錯誤，確保：
1. 所有 Entity 符合需求定義
2. 前後台認證系統分離正確
3. RBAC 權限系統運作正常
```

---

## 📝 注意事項

1. **前後台完全分離**: user 表與 admin_user 表互不影響
2. **Token 機制**: 都使用 JWT，但 Filter 不同（ApiJwtAuthenticationFilter vs AdminJwtAuthenticationFilter）
3. **角色固定**: Admin, StoreOwner, StoreEditor 為三個預設角色，可擴充但不建議刪除
4. **店家隔離**: StoreOwner 和 StoreEditor 只能看到自己店家的資料（透過 storeId 過濾）
5. **Google OAuth**: 僅限前台使用，後台不支援
6. **點數機制**: 僅前台使用，後台無點數概念
