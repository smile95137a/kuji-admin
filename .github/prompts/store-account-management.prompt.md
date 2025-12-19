# 店家後台帳號管理需求

## 文件用途
本文件描述店家後台帳號系統（StoreOwner / StoreEditor）的業務需求與權限規則，供 AI 編碼代理理解並實作相關功能。

## ⚠️ 開發前必讀
- **專案架構**: 請先閱讀 `architecture-guide.prompt.md` 了解 MyBatis Generator (MBG) 使用規範
- **開發流程**: DDL 優先 → 執行 MBG → 使用 Example 查詢 → 必要時才手寫 SQL
- **資料表設計**: 本文件提供欄位需求，需轉換為 DDL 後執行 MBG 生成程式碼

---

## 一、帳號系統概述

### 1.1 核心設計理念
- **Admin 絕對控制**: 所有帳號建立、權限修改、啟用/停用僅由 Admin 執行
- **店家無法自助管理權限**: StoreOwner / StoreEditor 不可自行新增帳號或修改權限
- **操作留痕**: 所有帳號變更需記錄操作者與時間
- **安全機制**: 初始密碼由系統生成，首次登入需強制修改密碼

### 1.2 帳號類型

| 角色 | 說明 | 建立方式 | 典型權限 |
|------|------|---------|---------|
| **Admin** | 平台最高管理者 | 系統預設 | 管理所有店家、所有權限 |
| **StoreOwner** | 店家主帳號 | Admin 建立 | 管理自己店家的商品、訂單、報表 |
| **StoreEditor** | 店家小編 | Admin 建立 | 僅能操作部分功能（商品、訂單） |

---

## 二、資料表需求

### 2.1 admin_user 表欄位需求

| 欄位 | 說明 | 必填 | 資料類型建議 |
|------|------|------|-------------|
| id | 使用者 ID | ✔ | BIGINT PRIMARY KEY AUTO_INCREMENT |
| username | 登入帳號（使用 Email） | ✔ | VARCHAR(100) UNIQUE |
| password | 密碼（BCrypt 加密） | ✔ | VARCHAR(255) |
| email | Email | ✔ | VARCHAR(100) UNIQUE |
| display_name | 顯示名稱 | ✔ | VARCHAR(100) |
| phone | 聯絡電話 | ✘ | VARCHAR(20) |
| status | 帳號狀態 | ✔ | VARCHAR(20) (ACTIVE/INACTIVE/PENDING) |
| force_change_password | 首次登入需改密碼 | ✔ | TINYINT(1) DEFAULT 1 |
| last_login_at | 最後登入時間 | ✘ | DATETIME |
| created_by | 建立者 ID | ✔ | BIGINT |
| created_at | 建立時間 | ✔ | DATETIME DEFAULT CURRENT_TIMESTAMP |
| updated_by | 最後修改者 ID | ✘ | BIGINT |
| updated_at | 更新時間 | ✔ | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP |
| remark | 備註 | ✘ | TEXT |

### 2.2 admin_user_role 表（使用者角色關聯）

| 欄位 | 說明 | 必填 | 資料類型建議 |
|------|------|------|-------------|
| id | 關聯 ID | ✔ | BIGINT PRIMARY KEY AUTO_INCREMENT |
| admin_user_id | 使用者 ID | ✔ | BIGINT (FK → admin_user.id) |
| role_id | 角色 ID | ✔ | BIGINT (FK → role.id) |
| created_at | 建立時間 | ✔ | DATETIME DEFAULT CURRENT_TIMESTAMP |

### 2.3 store_user 表（店家與小編關聯，可選）

| 欄位 | 說明 | 必填 | 資料類型建議 |
|------|------|------|-------------|
| id | 關聯 ID | ✔ | BIGINT PRIMARY KEY AUTO_INCREMENT |
| store_id | 店家 ID | ✔ | BIGINT (FK → store.id) |
| admin_user_id | 使用者 ID | ✔ | BIGINT (FK → admin_user.id) |
| role_type | 角色類型 | ✔ | VARCHAR(20) (OWNER/EDITOR) |
| created_at | 建立時間 | ✔ | DATETIME DEFAULT CURRENT_TIMESTAMP |

### 2.4 DDL 範例
```sql
-- admin_user 表
CREATE TABLE admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE COMMENT '登入帳號（Email）',
    password VARCHAR(255) NOT NULL COMMENT '密碼（BCrypt）',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT 'Email',
    display_name VARCHAR(100) NOT NULL COMMENT '顯示名稱',
    phone VARCHAR(20) COMMENT '聯絡電話',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '狀態：ACTIVE/INACTIVE/PENDING',
    force_change_password TINYINT(1) NOT NULL DEFAULT 1 COMMENT '首次登入需改密碼',
    last_login_at DATETIME COMMENT '最後登入時間',
    created_by BIGINT NOT NULL COMMENT '建立者 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT COMMENT '最後修改者 ID',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    remark TEXT COMMENT '備註',
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='後台管理者帳號表';

-- admin_user_role 關聯表
CREATE TABLE admin_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_user_id) REFERENCES admin_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (admin_user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='使用者角色關聯表';

-- store_user 關聯表（可選）
CREATE TABLE store_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    admin_user_id BIGINT NOT NULL,
    role_type VARCHAR(20) NOT NULL COMMENT 'OWNER/EDITOR',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE CASCADE,
    FOREIGN KEY (admin_user_id) REFERENCES admin_user(id) ON DELETE CASCADE,
    UNIQUE KEY uk_store_user (store_id, admin_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店家與使用者關聯表';
```

---

## 三、帳號建立流程

### 3.1 StoreOwner 建立流程

#### 業務流程
1. Admin 收到店家申請資訊（Email、店名、聯絡人）
2. Admin 在後台點擊「新增店家帳號」
3. 系統需執行的操作（必須在同一事務中完成）：
   - 建立 `admin_user` 紀錄（status = PENDING，force_change_password = 1）
   - 生成隨機初始密碼（建議 8-12 位，包含大小寫字母與數字）
   - 綁定 `StoreOwner` 角色（透過 `admin_user_role` 表）
   - 綁定到對應的 `store`（透過 `store.owner_id` 或 `store_user` 表）
   - 發送 Email 通知（包含初始密碼與登入連結）
4. 店家首次登入後強制跳轉修改密碼頁面
5. 修改密碼成功後，`force_change_password` 設為 0，`status` 變更為 ACTIVE

#### 資料一致性要求
- 若任一步驟失敗，整個流程需回滾
- Email 必須唯一，重複時拋出異常
- 初始密碼需經過 BCrypt 加密後存儲
- 使用 MBG 生成的 `insert` 方法建立帳號

---

### 3.2 StoreEditor 建立流程

#### 業務規則
- **重要**: StoreEditor 由 Admin 建立，**不是** StoreOwner 自行新增
- 建立時需指定歸屬的店家（`store_id`）
- 權限範圍由 `role_menu` 決定（通常僅商品管理、訂單查看）

#### 執行步驟
1. Admin 選擇目標店家
2. 填寫 StoreEditor 的 Email 與顯示名稱
3. 系統建立 `admin_user` 並綁定 `StoreEditor` 角色
4. 透過 `store_user` 表建立店家關聯（`role_type` = EDITOR）
5. 發送初始密碼通知
6. StoreEditor 首次登入需改密碼

#### 與 StoreOwner 的差異
- StoreOwner 透過 `store.owner_id` 綁定（一對一）
- StoreEditor 透過 `store_user` 表綁定（可多對多，一個店可有多個小編）
- StoreEditor 權限受限，無法查看報表或修改店家資訊

---

## 四、權限管理

### 4.1 權限矩陣

| 功能 | Admin | StoreOwner | StoreEditor |
|------|-------|-----------|-------------|
| 建立帳號 | ✔ | ✖ | ✖ |
| 修改權限 | ✔ | ✖ | ✖ |
| 啟用/停用帳號 | ✔ | ✖ | ✖ |
| 查看所有店家 | ✔ | ✖ | ✖ |
| 管理自己店家資料 | ✔ | ✔ | ✖ |
| 管理商品 | ✔ | ✔ | ✔ |
| 查看營收報表 | ✔ | ✔ | ✖ |
| 查看訂單 | ✔ | ✔ | ✔（部分） |

### 4.2 權限檢查邏輯

#### 查詢使用者的角色
- 使用 `AdminUserRoleExample` 查詢該使用者的所有角色關聯
- 再透過角色 ID 查詢 `role` 表取得角色名稱清單
- 回傳角色名稱陣列（如 ["ROLE_ADMIN", "ROLE_STORE_OWNER"]）

#### 檢查使用者是否屬於某店家
有兩種方案可選擇：
- **方案 1（StoreOwner）**: 查詢 `store.owner_id` 是否等於該使用者 ID
- **方案 2（StoreEditor）**: 使用 `StoreUserExample` 查詢 `store_user` 表
  - 條件：`store_id` = 指定店家 AND `admin_user_id` = 當前使用者
  - 若 count > 0 則表示該使用者屬於該店家

---

## 五、帳號啟用/停用

### 5.1 業務規則
- **操作權限**: 僅 Admin 可啟用或停用帳號
- **停用後效果**：
  - 該帳號無法登入（登入時檢查 status）
  - 已登入的 Token 應立即失效（可選：撤銷 Token）
  - 前台不受影響（前台使用 `user` 表，與後台帳號分離）
  
### 5.2 停用流程
1. 驗證操作者為 Admin
2. 使用 MBG 的 `selectByPrimaryKey` 查詢目標帳號
3. 將 `status` 設為 INACTIVE
4. 記錄 `updated_by` 為當前 Admin ID
5. 使用 MBG 的 `updateByPrimaryKey` 更新
6. （可選）撤銷該使用者所有有效 Token

### 5.3 啟用流程
1. 驗證操作者為 Admin
2. 查詢目標帳號
3. 將 `status` 設為 ACTIVE
4. 記錄 `updated_by`
5. 更新資料庫

---

## 六、首次登入與密碼修改

### 6.1 業務流程
1. StoreOwner/StoreEditor 收到系統發送的初始密碼 Email
2. 使用 Email + 初始密碼登入
3. 系統檢查 `force_change_password` 欄位
4. 若為 `true`（= 1），強制跳轉至「修改密碼」頁面，無法進入其他功能
5. 使用者輸入新密碼並確認
6. 系統驗證舊密碼正確，將新密碼加密後更新
7. 更新 `force_change_password` 為 `false`，`status` 設為 ACTIVE
8. 重新生成 Token，允許正常使用系統

### 6.2 安全要求
- 舊密碼驗證：需使用 BCrypt 的 `matches` 方法驗證
- 新密碼規則：至少 8 位，包含大小寫字母與數字（建議前端驗證）
- 密碼加密：使用 BCrypt 加密後存儲
- 強制修改期間不可跳過：前端需攔截所有其他路由

---

## 七、操作紀錄與審計

### 7.1 需記錄的操作
- **帳號建立**: 記錄 `created_by`（建立者 Admin ID）與 `created_at`
- **帳號啟用/停用**: 記錄 `updated_by`（操作者 Admin ID）與 `updated_at`
- **角色變更**: 在 `admin_user_role` 表中記錄新增/刪除時間
- **最後登入時間**: 每次成功登入後更新 `last_login_at`

### 7.2 登入記錄更新
- 在驗證帳號密碼成功後，生成 Token 之前
- 使用 MBG 的 `updateByPrimaryKeySelective` 僅更新 `last_login_at` 欄位
- 避免覆蓋其他欄位（如 `updated_at` 不應因登入而變動）

---

## 八、報表權限

### 8.1 業務規則
- **StoreOwner**: 可查看自己店家的營收報表、抽獎統計、訂單明細
- **StoreEditor**: 預設**不可查看**營收報表（除非透過 `role_menu` 特別授權）
- **Admin**: 可查看所有店家報表，包含跨店家的統計分析

### 8.2 查詢邏輯
- 先查詢使用者的角色清單（使用 `AdminUserRoleExample`）
- 若包含 ROLE_ADMIN → 回傳所有報表資料
- 若包含 ROLE_STORE_OWNER → 查詢該使用者所屬店家 ID，再使用 `RevenueReportExample` 過濾 `store_id`
- 若為 ROLE_STORE_EDITOR → 拋出權限不足異常

---

## 九、API 功能需求

### 9.1 帳號管理 API（僅 Admin）
- **新增 StoreOwner**: `POST /api/admin/users/store-owner`
- **新增 StoreEditor**: `POST /api/admin/users/store-editor`
- **啟用帳號**: `PUT /api/admin/users/{id}/activate`
- **停用帳號**: `PUT /api/admin/users/{id}/deactivate`
- **查詢所有帳號**: `GET /api/admin/users`
- **查詢單一帳號**: `GET /api/admin/users/{id}`

### 9.2 密碼管理 API
- **首次登入修改密碼**: `POST /api/admin/auth/first-login/change-password`
- **忘記密碼**: `POST /api/admin/auth/forgot-password`
- **重設密碼**: `POST /api/admin/auth/reset-password`

### 9.3 權限查詢 API
- **查詢我的角色**: `GET /api/admin/users/me/roles`
- **查詢我的權限菜單**: `GET /api/admin/users/me/menus`

---

## 十、測試驗證要點

### 10.1 功能測試
- 驗證 Admin 可建立 StoreOwner 與 StoreEditor
- 驗證首次登入強制修改密碼流程
- 驗證停用帳號後無法登入
- 驗證 StoreOwner 僅可查看自己店家資料
- 驗證 StoreEditor 無法查看營收報表

### 10.2 Example 查詢測試
- 驗證使用 `AdminUserRoleExample` 查詢使用者角色正確
- 驗證使用 `StoreUserExample` 查詢店家歸屬正確
- 驗證 MBG 生成的 insert/update 方法正常運作

### 10.3 安全性測試
- 驗證密碼使用 BCrypt 加密
- 驗證初始密碼足夠隨機
- 驗證 Token 在帳號停用後失效

---

## 十一、相關資料表

- `admin_user` - 後台管理者帳號主表
- `admin_user_role` - 使用者與角色關聯表
- `role` - 角色定義表
- `menu` - 菜單定義表
- `role_menu` - 角色與菜單權限關聯表
- `store` - 店家資料表
- `store_user` - 店家與使用者關聯表（可選）

---

## 十二、開發檢查清單

開始開發前請確認:
- [ ] 已閱讀 `architecture-guide.prompt.md`
- [ ] 已撰寫 `admin_user`, `admin_user_role`, `store_user` 表的 DDL 並執行
- [ ] 已在 `generatorConfig.xml` 新增這些表的配置
- [ ] 已執行 `FullSchemaExampleGenerator.java` 生成程式碼
- [ ] 已檢查生成的 Entity、Mapper、Example 類
- [ ] 優先使用 Example 進行查詢，避免手寫 SQL

---

**最後更新**: 2025-12-14  
**參考文件**: `architecture-guide.prompt.md`, `permissions-rbac.prompt.md`, `store-management.prompt.md`
