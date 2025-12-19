# 店家後台管理功能需求

## 文件用途
本文件描述店家後台管理系統的業務需求與規則，供 AI 編碼代理理解並實作相關功能。

## ⚠️ 開發前必讀
- **專案架構**: 請先閱讀 `architecture-guide.prompt.md` 了解 MyBatis Generator (MBG) 使用規範
- **開發流程**: DDL 優先 → 執行 MBG → 使用 Example 查詢 → 必要時才手寫 SQL
- **資料表設計**: 本文件提供欄位需求，需轉換為 DDL 後執行 MBG 生成程式碼

---

## 一、店家管理模組目的

### 1.1 核心功能
- 記錄每個店家的基本資料
- 控制店家的啟用/停用狀態
- 前台可展示店家資訊
- 點擊店家後可查看該店家所有商品
- 讓平台能對接 Banner、商品、報表等模組
- 所有店家資料均由 Admin 建立與管理

### 1.2 設計理念
- 店家與店家小編**無法自行操作權限或停用帳號**
- Banner 與店家資料連動，便於前端展示與廣告計費
- 每筆操作需留存操作者與時間紀錄

---

## 二、店家資料需求（Store 資料表）

### 2.1 Store 資料表欄位需求

| 欄位 | 說明 | 必填 | 資料類型建議 |
|------|------|------|-------------|
| id | 店家 ID | ✔ | BIGINT PRIMARY KEY AUTO_INCREMENT |
| owner_id | 店家主帳號 ID | ✔ | BIGINT (FK → admin_user.id) |
| store_name | 店家公開顯示名稱 | ✔ | VARCHAR(100) |
| short_description | 短描述（列表用） | ✔ | VARCHAR(255) |
| long_description | 詳細介紹、品牌故事 | ✘ | TEXT |
| logo_url | 店家 Logo（小圖） | ✔ | VARCHAR(255) |
| cover_image_url | 封面圖片（大圖） | ✘ | VARCHAR(255) |
| email | 聯絡 Email | ✔ | VARCHAR(100) |
| phone | 聯絡電話 | ✔ | VARCHAR(20) |
| address | 地址 | ✔ | VARCHAR(255) |
| business_hours | 營業時間 | ✔ | VARCHAR(100) |
| facebook_url | Facebook 連結 | ✘ | VARCHAR(255) |
| instagram_url | Instagram 連結 | ✘ | VARCHAR(255) |
| status | 上架狀態 | ✔ | VARCHAR(20) (ACTIVE/INACTIVE) |
| remark | 後台備註 | ✘ | TEXT |
| created_at | 建立時間 | ✔ | DATETIME DEFAULT CURRENT_TIMESTAMP |
| updated_at | 更新時間 | ✔ | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP |
| updated_by | 最後修改者 ID | ✘ | BIGINT |

### 2.2 DDL 範例
```sql
CREATE TABLE store (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL COMMENT '店家主帳號 ID',
    store_name VARCHAR(100) NOT NULL COMMENT '店家名稱',
    short_description VARCHAR(255) NOT NULL COMMENT '短描述',
    long_description TEXT COMMENT '詳細介紹',
    logo_url VARCHAR(255) NOT NULL COMMENT 'Logo URL',
    cover_image_url VARCHAR(255) COMMENT '封面圖片 URL',
    email VARCHAR(100) NOT NULL COMMENT '聯絡 Email',
    phone VARCHAR(20) NOT NULL COMMENT '聯絡電話',
    address VARCHAR(255) NOT NULL COMMENT '地址',
    business_hours VARCHAR(100) NOT NULL COMMENT '營業時間',
    facebook_url VARCHAR(255) COMMENT 'Facebook 連結',
    instagram_url VARCHAR(255) COMMENT 'Instagram 連結',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '狀態：ACTIVE/INACTIVE',
    remark TEXT COMMENT '後台備註',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by BIGINT COMMENT '最後修改者 ID',
    FOREIGN KEY (owner_id) REFERENCES admin_user(id),
    INDEX idx_status (status),
    INDEX idx_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店家資料表';
```

---

## 三、店家前台顯示邏輯

### 3.1 前台使用方式
- 使用者可在「店家列表」看到所有啟用中店家（顯示名稱、Logo、介紹）
- 點擊店家 → 前往「店家專屬頁」

### 3.2 店家專屬頁內容
- 店家大圖（封面圖，若無則系統預設）
- 店家簡介（短 + 長）
- 店家營業時間、社群連結
- 該店家所有的上架中商品

### 3.3 查詢需求（使用 Example）
```java
// 查詢所有啟用的店家（前台列表）
StoreExample example = new StoreExample();
example.createCriteria().andStatusEqualTo("ACTIVE");
List<Store> activeStores = storeMapper.selectByExample(example);

// 查詢指定店家（店家專屬頁）
Store store = storeMapper.selectByPrimaryKey(storeId);
```

---

## 四、店家與其他模組的關聯

### 4.1 與商品（Lottery）關聯
- **核心規則**: 每個商品必須隸屬於唯一一個店家
- 不能跨店家共用商品
- 訂單、抽獎、報表皆依據商品所屬店家計算

### 4.2 與 Banner 關聯
- Banner 一定綁定一個店家
- 點擊 Banner 即跳店家頁

### 4.3 與抽獎/報表關聯
- 每一筆遊玩紀錄屬於該店家的營業額
- 日結/月結報表依店家分開統計
- 店家停用後不可再被抽獎、不可搜尋到商品

---

## 五、店家管理流程

### 5.1 新增店家流程

#### 業務流程
1. Admin 點擊「新增店家」
2. 填寫必填欄位（店名、描述、Logo、聯絡資訊）
3. 系統執行：
   - 建立 `store` 紀錄（status = ACTIVE）
   - 同時建立「店家 Owner 帳號」（參考 `store-account-management.prompt.md`）
   - 記錄操作者 ID（`created_by`）
   - 發送通知 Email 給店家

#### 注意事項
- 店家與帳號建立需在同一事務中完成
- 若帳號建立失敗，店家資料也需回滾
- 使用 MBG 生成的 `insert` 方法建立店家

---

### 5.2 編輯店家流程

#### 業務規則
- **可修改欄位**: Logo、描述、地址、營業時間、社群連結
- **不可修改欄位**: owner_id（店家主帳號綁定後不可變更）
- **即時生效**: 修改後立即同步到前台顯示

#### 權限控制
- Admin 可編輯任何店家
- StoreOwner 僅可編輯自己的店家
- 使用 MBG 生成的 `selectByPrimaryKey` 查詢、`updateByPrimaryKey` 更新

---

### 5.3 停用店家流程（重要）

#### 業務規則（連動影響）
當 Admin 停用店家時，系統需執行以下連動操作：
1. **店家狀態變更**: `store.status` 設為 INACTIVE
2. **商品自動下架**: 該店家所有商品的 `status` 設為 OFF_SHELF
3. **前台隱藏**: 前台不再顯示該店家與其商品
4. **禁止新抽獎**: 已開始的抽獎可繼續，但不可開啟新抽獎
5. **Banner 連動**: 若 Banner 綁定該店家，也需一併停用

#### 執行順序
1. 更新店家狀態
2. 查詢該店家所有商品（使用 Example: `andStoreIdEqualTo`）
3. 批次更新商品狀態為 OFF_SHELF
4. 查詢並停用相關 Banner（可選）
5. 記錄操作者與時間

#### 注意事項
- 整個流程需在同一事務中完成（`@Transactional`）
- 停用前需確認是否有進行中的訂單或抽獎
- 需記錄停用原因與時間供日後查詢
---

## 六、權限規則

### 6.1 權限矩陣

| 角色 | 新增店家 | 編輯店家 | 停用店家 | 查看所有店家 |
|------|---------|---------|---------|-------------|
| **Admin** | ✔ | ✔ | ✔ | ✔ |
| **StoreOwner** | ✖ | ✔ (僅自己) | ✖ | ✖ (僅自己) |
| **StoreEditor** | ✖ | ✖ | ✖ | ✖ |
| **前台使用者** | ✖ | ✖ | ✖ | ✔ (僅啟用的店家) |

### 6.2 權限檢查邏輯

#### StoreOwner 查詢自己的店家
- 使用 Example 查詢條件: `andOwnerIdEqualTo(當前使用者ID)`
- 回傳該使用者擁有的店家（通常只有一個）
- 若無店家則拋出異常

#### Admin 查詢所有店家
- 不加任何條件，使用 MBG 生成的 `selectAll` 或空 Example
- 回傳所有店家列表

#### StoreEditor 無店家查詢權限
- StoreEditor 透過 `store_user` 表關聯到店家
- 無法直接查詢店家資料，僅可操作商品

---

## 七、後台帳號管理

### 7.1 店家帳號建立流程
- 由 Admin 建立所有店家帳號（StoreOwner / StoreEditor）
- 詳細流程請參考 `store-backend.prompt.md`

### 7.2 關聯說明
- 每個 Store 有一個 `owner_id`，對應到 `admin_user` 表的 StoreOwner 帳號
- StoreEditor 透過 `store_user` 關聯表綁定到店家

---

## 八、API 功能需求

### 8.1 店家管理 API（後台）
- **查詢店家列表**: `GET /api/admin/stores`
  - 依權限過濾：Admin 查看全部，StoreOwner 查看自己
  - 支援分頁與排序
  
- **查詢單一店家**: `GET /api/admin/stores/{id}`
  - 回傳店家詳細資訊
  - StoreOwner 僅可查看自己的店家
  
- **新增店家**: `POST /api/admin/stores`
  - 僅 Admin 可執行
  - 需同時建立 StoreOwner 帳號
  
- **更新店家**: `PUT /api/admin/stores/{id}`
  - Admin 或 StoreOwner（僅自己）可執行
  - 需記錄 `updated_by`
  
- **啟用/停用店家**: `PUT /api/admin/stores/{id}/status`
  - 僅 Admin 可執行
  - 停用時需連動處理商品與 Banner

### 8.2 店家查詢 API（前台）
- **查詢啟用的店家列表**: `GET /api/stores`
  - 僅回傳 status = ACTIVE 的店家
  - 包含店名、Logo、短描述
  
- **查詢店家詳情**: `GET /api/stores/{id}`
  - 回傳店家完整資訊
  - 同時回傳該店家的上架商品列表

---

## 九、預留功能（未來擴充）

以下功能目前**不需實作**，但在設計時預留彈性：
- ⭐ **店家等級制度**: VIP 店家享有更高曝光率或優惠手續費
- ⭐ **審核流程**: 新店家加入需經過 Admin 審核
- ⭐ **自訂主題**: 店家可設定專屬背景顏色、字體風格
- ⭐ **財務帳戶**: 記錄店家提款帳戶資訊（銀行帳號、PayPal 等）
- ⭐ **多語系支援**: 店家資訊可提供多語系版本

---

## 十、測試驗證要點

### 10.1 功能測試
- 驗證 Admin 可新增/編輯/停用所有店家
- 驗證 StoreOwner 僅可查看與編輯自己的店家
- 驗證停用店家後，該店家商品自動下架
- 驗證前台僅顯示啟用的店家

### 10.2 Example 查詢測試
- 驗證使用 Example 查詢 status = ACTIVE 的店家正確
- 驗證使用 Example 查詢 owner_id 的店家正確
- 驗證 MBG 生成的 insert/update 方法正常運作

### 10.3 資料一致性測試
- 驗證建立店家時自動建立對應的 admin_user
- 驗證停用店家時相關商品與 Banner 正確處理
- 驗證 updated_by 欄位正確記錄操作者

---

## 十一、相關資料表

- `store` - 店家資料主表
- `admin_user` - 管理者帳號（StoreOwner）
- `store_user` - 店鋪與小編關聯
- `lottery` - 商品（綁定 store_id）
- `banner` - Banner（綁定 store_id）

---

## 十二、開發檢查清單

開始開發前請確認:
- [ ] 已閱讀 `architecture-guide.prompt.md`
- [ ] 已撰寫 `store` 表的 DDL 並執行
- [ ] 已在 `generatorConfig.xml` 新增 `store` 表配置
- [ ] 已執行 `FullSchemaExampleGenerator.java` 生成程式碼
- [ ] 已檢查生成的 `Store.java`、`StoreMapper.java`、`StoreExample.java`
- [ ] 優先使用 Example 進行查詢，避免手寫 SQL

---

**最後更新**: 2025-12-14  
**參考文件**: `architecture-guide.prompt.md`, `storeUser-instrctions.md`

