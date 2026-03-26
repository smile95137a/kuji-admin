# 🧪 KUJI 角色權限測試指南

## 測試目標
驗證不同角色的使用者在系統中的權限控制是否正確

---

## 測試環境準備

### 1. 測試帳號清單

```
管理員 (ROLE_ADMIN):
Email: admin@kuji.com
Password: admin123
權限: 可以看到所有資料、所有店家

店家負責人 A (ROLE_STORE_OWNER):
Email: owner-a@store-a.com
Password: owner123
Store ID: store-001
Store Name: 動漫專賣店
權限: 只能看到 store-001 的資料

店家負責人 B (ROLE_STORE_OWNER):
Email: owner-b@store-b.com
Password: owner123
Store ID: store-002
Store Name: 公仔天堂
權限: 只能看到 store-002 的資料

店家編輯 (ROLE_STORE_EDITOR):
Email: editor@store-a.com
Password: editor123
Store ID: store-001
權限: 可編輯但不能刪除

前台玩家 (ROLE_USER):
Email: user@example.com
Password: user123
權限: 只能看到自己的資料
```

---

## 測試場景 1: 商品管理

### 1.1 管理員測試 (admin@kuji.com)

**登入**
```bash
POST http://18.179.187.129:8080/api/admin/auth/login
{
  "email": "admin@kuji.com",
  "password": "admin123"
}
```

**預期結果:** ✅ 登入成功，取得 JWT token

**查詢所有商品**
```bash
POST http://18.179.187.129:8080/api/admin/lottery/list
Authorization: Bearer {admin_token}
{
  "condition": {}
}
```

**預期結果:** ✅ 返回所有店家的商品（store-001 和 store-002）

**建立商品（指定店家 A）**
```bash
POST http://18.179.187.129:8080/api/admin/lottery
Authorization: Bearer {admin_token}
{
  "storeId": "store-001",
  "title": "管理員建立的商品",
  "category": "OFFICIAL_ICHIBAN",
  "pricePerDraw": 80
}
```

**預期結果:** ✅ 成功建立，storeId 為 store-001

---

### 1.2 店家負責人 A 測試 (owner-a@store-a.com)

**登入**
```bash
POST http://18.179.187.129:8080/api/admin/auth/login
{
  "email": "owner-a@store-a.com",
  "password": "owner123"
}
```

**預期結果:** ✅ 登入成功，JWT 包含 storeIds: ["store-001"]

**查詢商品（不帶 storeId）**
```bash
POST http://18.179.187.129:8080/api/admin/lottery/list
Authorization: Bearer {owner_a_token}
{
  "condition": {}
}
```

**預期結果:** ✅ 只返回 store-001 的商品（自動過濾）

**建立商品（不帶 storeId）**
```bash
POST http://18.179.187.129:8080/api/admin/lottery
Authorization: Bearer {owner_a_token}
{
  "title": "店家A的商品",
  "category": "OFFICIAL_ICHIBAN",
  "pricePerDraw": 80
}
```

**預期結果:** ✅ 成功建立，storeId 自動設為 store-001

**嘗試建立商品（指定 store-002）**
```bash
POST http://18.179.187.129:8080/api/admin/lottery
Authorization: Bearer {owner_a_token}
{
  "storeId": "store-002",
  "title": "嘗試建立其他店家的商品",
  "category": "OFFICIAL_ICHIBAN",
  "pricePerDraw": 80
}
```

**預期結果:** ❌ 失敗，返回 403 或錯誤訊息「無權限操作其他店家的商品」

---

### 1.3 店家負責人 B 測試 (owner-b@store-b.com)

**登入**
```bash
POST http://18.179.187.129:8080/api/admin/auth/login
{
  "email": "owner-b@store-b.com",
  "password": "owner123"
}
```

**查詢商品**
```bash
POST http://18.179.187.129:8080/api/admin/lottery/list
Authorization: Bearer {owner_b_token}
{
  "condition": {}
}
```

**預期結果:** ✅ 只返回 store-002 的商品

**嘗試修改店家A的商品**
```bash
PUT http://18.179.187.129:8080/api/admin/lottery/{lottery_id_from_store_a}
Authorization: Bearer {owner_b_token}
{
  "title": "嘗試修改店家A的商品"
}
```

**預期結果:** ❌ 失敗，返回 403 或「找不到商品」

---

## 測試場景 2: 訂單管理

### 2.1 管理員測試

**查詢所有訂單**
```bash
POST http://18.179.187.129:8080/api/admin/order/list
Authorization: Bearer {admin_token}
{
  "condition": {}
}
```

**預期結果:** ✅ 返回所有店家的訂單

---

### 2.2 店家負責人 A 測試

**查詢訂單**
```bash
POST http://18.179.187.129:8080/api/admin/order/list
Authorization: Bearer {owner_a_token}
{
  "condition": {}
}
```

**預期結果:** ✅ 只返回 store-001 的訂單

**查看訂單詳情（店家B的訂單）**
```bash
GET http://18.179.187.129:8080/api/admin/order/{order_id_from_store_b}
Authorization: Bearer {owner_a_token}
```

**預期結果:** ❌ 失敗，返回 404 或 403

---

## 測試場景 3: 報表查詢

### 3.1 管理員測試

**查詢營業額報表（所有店家）**
```bash
GET http://18.179.187.129:8080/api/admin/report/revenue?startDate=2026-01-01&endDate=2026-01-31
Authorization: Bearer {admin_token}
```

**預期結果:** ✅ 返回所有店家的營業額統計

**查詢營業額報表（指定店家A）**
```bash
GET http://18.179.187.129:8080/api/admin/report/revenue?storeId=store-001&startDate=2026-01-01&endDate=2026-01-31
Authorization: Bearer {admin_token}
```

**預期結果:** ✅ 只返回 store-001 的營業額

---

### 3.2 店家負責人 A 測試

**查詢營業額報表（不帶 storeId）**
```bash
GET http://18.179.187.129:8080/api/admin/report/revenue?startDate=2026-01-01&endDate=2026-01-31
Authorization: Bearer {owner_a_token}
```

**預期結果:** ✅ 自動限定為 store-001 的營業額

**嘗試查詢店家B的報表**
```bash
GET http://18.179.187.129:8080/api/admin/report/revenue?storeId=store-002&startDate=2026-01-01&endDate=2026-01-31
Authorization: Bearer {owner_a_token}
```

**預期結果:** ❌ 返回空資料或錯誤（後端自動覆蓋為 store-001）

---

## 測試場景 4: 前台玩家

### 4.1 玩家登入與抽獎

**登入**
```bash
POST http://18.179.187.129:8080/api/auth/login
{
  "email": "user@example.com",
  "password": "user123"
}
```

**預期結果:** ✅ 登入成功，角色為 ROLE_USER

**查詢可抽獎商品**
```bash
GET http://18.179.187.129:8080/api/lottery/list?status=ON_SHELF
Authorization: Bearer {user_token}
```

**預期結果:** ✅ 返回所有上架中的商品（所有店家）

**執行抽獎**
```bash
POST http://18.179.187.129:8080/api/lottery/{lotteryId}/draw
Authorization: Bearer {user_token}
{
  "drawCount": 1
}
```

**預期結果:** ✅ 抽獎成功，獎品進入賞品盒

**查看賞品盒**
```bash
GET http://18.179.187.129:8080/api/prize-box
Authorization: Bearer {user_token}
```

**預期結果:** ✅ 只看到自己抽中的獎品

**嘗試查看其他玩家的賞品盒**
```bash
GET http://18.179.187.129:8080/api/prize-box?userId=other-user-id
Authorization: Bearer {user_token}
```

**預期結果:** ❌ 失敗或被忽略（只能看到自己的）

---

## 測試場景 5: 跨角色權限測試

### 5.1 玩家嘗試存取後台 API

**嘗試查看商品列表（後台）**
```bash
POST http://18.179.187.129:8080/api/admin/lottery/list
Authorization: Bearer {user_token}
{
  "condition": {}
}
```

**預期結果:** ❌ 返回 403 Forbidden

---

### 5.2 店家嘗試存取系統管理 API

**嘗試建立管理員帳號**
```bash
POST http://18.179.187.129:8080/api/admin/user/create
Authorization: Bearer {owner_a_token}
{
  "email": "new-admin@kuji.com",
  "password": "admin123",
  "role": "ROLE_ADMIN"
}
```

**預期結果:** ❌ 返回 403 Forbidden

---

## 測試檢查清單

### ✅ 商品管理
- [ ] 管理員可以看到所有商品
- [ ] 店家A只能看到自己的商品
- [ ] 店家B只能看到自己的商品
- [ ] 店家A不能修改店家B的商品
- [ ] storeId 自動帶入正確

### ✅ 訂單管理
- [ ] 管理員可以看到所有訂單
- [ ] 店家A只能看到自己的訂單
- [ ] 店家A不能查看店家B的訂單

### ✅ 報表查詢
- [ ] 管理員可以查詢所有店家報表
- [ ] 店家A只能查詢自己的報表
- [ ] 店家A無法查詢店家B的報表

### ✅ 前台功能
- [ ] 玩家可以瀏覽所有商品
- [ ] 玩家可以執行抽獎
- [ ] 玩家只能看到自己的賞品盒
- [ ] 玩家不能存取後台 API

### ✅ 跨角色權限
- [ ] 玩家不能存取後台 API
- [ ] 店家不能存取系統管理 API
- [ ] 編輯不能刪除資料

---

## 錯誤處理驗證

### 應返回的錯誤訊息

**403 Forbidden:**
```json
{
  "success": false,
  "error": {
    "code": "FORBIDDEN",
    "message": "無權限執行此操作"
  }
}
```

**404 Not Found (權限過濾):**
```json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "找不到指定的資源"
  }
}
```

---

## 測試工具

### Postman Collection
使用 `KUJI_Admin_API_Tests.postman_collection.json`

### 環境變數設定
```
API_URL: http://18.179.187.129:8080
ADMIN_TOKEN: (登入後取得)
OWNER_A_TOKEN: (登入後取得)
OWNER_B_TOKEN: (登入後取得)
USER_TOKEN: (登入後取得)
```

---

## 測試報告模板

```
測試日期: 2026-01-15
測試人員: [Your Name]

測試結果:
✅ 通過 | ❌ 失敗 | ⚠️ 部分通過

場景 1: 商品管理
- 管理員: ✅
- 店家A: ✅
- 店家B: ✅

場景 2: 訂單管理
- 管理員: ✅
- 店家A: ✅

場景 3: 報表查詢
- 管理員: ✅
- 店家A: ✅

場景 4: 前台玩家
- 抽獎: ✅
- 賞品盒: ✅

場景 5: 跨角色權限
- 權限控制: ✅

問題記錄:
1. [描述問題]
2. [描述問題]

建議改進:
1. [改進建議]
```

---

**開始測試前請確認:**
1. ✅ 後端已部署最新版本
2. ✅ 測試帳號已建立
3. ✅ Postman Collection 已導入
4. ✅ 環境變數已設定
