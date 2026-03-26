# 推薦碼機制 & 使用者地址系統 - 實作完成報告

## 📅 實作日期：2026-01-12

## ✅ 實作完成項目

### 1. 推薦碼系統 (Referral Code System)

#### 資料庫實體 (Entity)
- `entity/ReferralCode.java` - 推薦碼實體
  - id (UUID), code (唯一), storeId, description, isActive, usedCount, timestamps
  
- `entity/ReferralRecord.java` - 推薦記錄實體
  - id (UUID), userId (唯一，一人只能被推薦一次), referralCodeId, storeId, usedCode, referredAt

#### 資料存取層 (Mapper)
- `mapper/ReferralCodeMapper.java`
  - insert, selectByPrimaryKey, selectByCode, selectByStoreId, selectAll
  - updateByPrimaryKey, deleteByPrimaryKey
  
- `mapper/ReferralRecordMapper.java`
  - insert, selectByPrimaryKey, selectByReferralCodeId, selectByStoreId, selectByUserId
  - countByStoreId, countByReferralCodeId, deleteByPrimaryKey

#### 請求/回應 DTO
- `req/referral/ReferralCodeCreateReq.java` - 建立推薦碼請求
- `req/referral/ReferralCodeUpdateReq.java` - 更新推薦碼請求
- `res/referral/ReferralCodeRes.java` - 推薦碼回應（含 storeName）
- `res/referral/ReferralRecordRes.java` - 推薦記錄回應（含 userName, storeName）

#### 服務層 (Service)
- `service/ReferralCodeService.java` - 介面
- `service/impl/ReferralCodeServiceImpl.java` - 實作
  - 建立、更新、刪除推薦碼
  - 驗證推薦碼 (validateCode)
  - 使用推薦碼 (useCode) - 建立記錄並增加使用次數
  - 查詢推薦記錄

#### 控制器 (Controller)
- `controller/admin/AdminReferralCodeController.java` - 後台管理 API
  ```
  POST   /admin/referral-codes           - 建立推薦碼
  PUT    /admin/referral-codes/{id}      - 更新推薦碼
  DELETE /admin/referral-codes/{id}      - 刪除推薦碼
  GET    /admin/referral-codes/{id}      - 查詢單一推薦碼
  GET    /admin/referral-codes           - 查詢所有推薦碼
  GET    /admin/referral-codes/my-store  - 查詢當前店家的推薦碼
  GET    /admin/referral-codes/validate/{code}  - 驗證推薦碼
  GET    /admin/referral-codes/{id}/records     - 查詢推薦碼的使用記錄
  GET    /admin/referral-codes/store/{storeId}/records - 查詢店家的所有推薦記錄
  ```

- `controller/api/ReferralCodeValidateController.java` - 前台公開 API
  ```
  GET    /api/auth/referral-code/validate/{code} - 公開驗證推薦碼
  GET    /api/auth/referral-code/info/{code}     - 公開查詢推薦碼資訊（脫敏）
  ```

#### 註冊整合
- 修改 `service/impl/UserServiceImpl.java`
  - 在 register() 方法中整合推薦碼使用
  - 若用戶提供推薦碼，自動記錄推薦關係

---

### 2. 使用者地址系統 (User Address System)

#### 資料庫實體 (Entity)
- `entity/UserAddress.java` - 使用者地址實體
  - id (UUID), userId, label, recipientName, recipientPhone
  - city, district, zipCode, address, isDefault, timestamps

#### 資料存取層 (Mapper)
- `mapper/UserAddressMapper.java`
  - insert, selectByPrimaryKey, selectByUserId, selectDefaultByUserId
  - updateByPrimaryKey, deleteByPrimaryKey, clearDefaultByUserId

#### 請求/回應 DTO
- `req/address/UserAddressCreateReq.java` - 建立地址請求
- `req/address/UserAddressUpdateReq.java` - 更新地址請求
- `res/address/UserAddressRes.java` - 地址回應（含 fullAddress 計算欄位）

#### 服務層 (Service)
- `service/UserAddressService.java` - 介面
- `service/impl/UserAddressServiceImpl.java` - 實作
  - CRUD 操作
  - 自動設定第一筆為預設地址
  - 設定預設地址時自動清除其他預設
  - 刪除預設地址時自動指派新預設

#### 控制器 (Controller)
- `controller/api/UserAddressController.java` - 前台用戶 API
  ```
  POST   /user/addresses              - 新增地址
  PUT    /user/addresses/{id}         - 更新地址
  DELETE /user/addresses/{id}         - 刪除地址
  GET    /user/addresses/{id}         - 查詢單一地址
  GET    /user/addresses              - 查詢所有地址
  GET    /user/addresses/default      - 查詢預設地址
  PUT    /user/addresses/{id}/default - 設定為預設地址
  ```

---

### 3. 資料庫 Schema

已建立 SQL 腳本：`src/main/resources/db/referral_address_schema.sql`

```sql
-- 推薦碼表
CREATE TABLE referral_code (...)

-- 推薦記錄表
CREATE TABLE referral_record (...)

-- 使用者地址表
CREATE TABLE user_address (...)
```

---

## 📋 待執行事項

### 1. 建立資料庫表格
在 MySQL 中執行 SQL 腳本：
```bash
mysql -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -p kuji < src/main/resources/db/referral_address_schema.sql
```

或直接在 MySQL Client 中執行 `referral_address_schema.sql` 的內容。

### 2. API 測試

#### 推薦碼 API 測試

**後台 - 建立推薦碼：**
```bash
POST http://localhost:8080/api/admin/referral-codes
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "code": "KUJI2024",
  "storeId": "{store_id}",
  "description": "2024 新年特惠"
}
```

**前台 - 驗證推薦碼：**
```bash
GET http://localhost:8080/api/api/auth/referral-code/validate/KUJI2024
```

**前台 - 註冊時使用推薦碼：**
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "用戶名",
  "referralCode": "KUJI2024"
}
```

#### 使用者地址 API 測試

**新增地址：**
```bash
POST http://localhost:8080/api/user/addresses
Authorization: Bearer {user_token}
Content-Type: application/json

{
  "label": "家",
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "city": "台北市",
  "district": "信義區",
  "zipCode": "110",
  "address": "信義路五段7號"
}
```

**查詢預設地址：**
```bash
GET http://localhost:8080/api/user/addresses/default
Authorization: Bearer {user_token}
```

---

## 🔧 已驗證的功能

### ✅ 編譯通過
```
309 source files compiled successfully
```

### ✅ 應用啟動成功
```
Started AdminApplication in 4.561 seconds
```

### ✅ Mapper 註冊成功
- ReferralCodeMapper
- ReferralRecordMapper
- UserAddressMapper

### ✅ Service 建立成功
- ReferralCodeServiceImpl
- UserAddressServiceImpl

### ✅ Controller 載入成功
- AdminReferralCodeController
- UserAddressController
- ReferralCodeValidateController

---

## 📁 新增檔案清單

```
src/main/java/com/group/admin/
├── entity/
│   ├── ReferralCode.java
│   ├── ReferralRecord.java
│   └── UserAddress.java
├── mapper/
│   ├── ReferralCodeMapper.java
│   ├── ReferralRecordMapper.java
│   └── UserAddressMapper.java
├── req/
│   ├── referral/
│   │   ├── ReferralCodeCreateReq.java
│   │   └── ReferralCodeUpdateReq.java
│   └── address/
│       ├── UserAddressCreateReq.java
│       └── UserAddressUpdateReq.java
├── res/
│   ├── referral/
│   │   ├── ReferralCodeRes.java
│   │   └── ReferralRecordRes.java
│   └── address/
│       └── UserAddressRes.java
├── service/
│   ├── ReferralCodeService.java
│   ├── UserAddressService.java
│   └── impl/
│       ├── ReferralCodeServiceImpl.java
│       └── UserAddressServiceImpl.java
└── controller/
    ├── admin/
    │   └── AdminReferralCodeController.java
    └── api/
        ├── UserAddressController.java
        └── ReferralCodeValidateController.java

src/main/resources/db/
└── referral_address_schema.sql
```

---

## 🔄 修改檔案清單

```
src/main/java/com/group/admin/service/impl/UserServiceImpl.java
  - 新增 ReferralCodeService 依賴注入
  - 在 register() 方法中整合推薦碼使用邏輯
```

---

## 📊 已存在的功能確認

### 運送管理 (Express/Shipping)
已存在於 Order 實體：
- `shippingMethod` - 運送方式 (HOME/STORE)
- `recipientName` - 收件人姓名
- `recipientPhone` - 收件人電話
- `recipientAddress` - 收件地址（宅配）
- `storeCode` - 超商店號
- `storeName` - 超商店名
- `storeAddress` - 超商地址
- `trackingNo` - 物流追蹤號

### 遊玩到訂單流程 (Game to Order)
已存在於：
- `PrizeBoxServiceImpl.shipPrizes()` - 從獎品箱建立訂單
- `OrderServiceImpl` - 完整訂單狀態流程
- `AdminOrderController` - 後台訂單管理

---

## 📞 需前端對接的 API Endpoint

### 推薦碼相關
| 方法 | 路徑 | 說明 | 認證 |
|------|------|------|------|
| GET | /api/auth/referral-code/validate/{code} | 驗證推薦碼 | 不需要 |
| GET | /api/auth/referral-code/info/{code} | 查詢推薦碼資訊 | 不需要 |
| POST | /admin/referral-codes | 建立推薦碼 | Admin |
| GET | /admin/referral-codes | 查詢所有推薦碼 | Admin |
| GET | /admin/referral-codes/my-store | 查詢店家推薦碼 | StoreOwner |

### 使用者地址相關
| 方法 | 路徑 | 說明 | 認證 |
|------|------|------|------|
| POST | /user/addresses | 新增地址 | User |
| GET | /user/addresses | 查詢所有地址 | User |
| GET | /user/addresses/default | 查詢預設地址 | User |
| PUT | /user/addresses/{id}/default | 設為預設地址 | User |
| PUT | /user/addresses/{id} | 更新地址 | User |
| DELETE | /user/addresses/{id} | 刪除地址 | User |

---

## ⚠️ 重要提醒

1. **必須先在資料庫執行 SQL 腳本** 建立 referral_code、referral_record、user_address 三張表
2. 推薦碼為**大寫字母和數字**，長度 4-20 字元
3. 每位用戶**只能被推薦一次**（referral_record.user_id 唯一）
4. 第一筆地址會**自動設為預設**
5. 刪除預設地址會**自動指派新的預設**

---

## 📝 結束語

推薦碼機制和使用者地址系統已完整實作，包含：
- 完整的 CRUD 操作
- 前後台 API 分離
- 驗證和錯誤處理
- 與現有系統的整合（註冊流程）

只需在資料庫執行 SQL 腳本即可開始使用。
