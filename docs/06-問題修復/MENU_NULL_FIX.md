# 🎉 選單資料回傳 null 問題解決

## ✅ 403 問題已解決
恭喜！API 已經可以打通了，現在是 200 OK 但 data 是 null。

## 🔍 根本原因

### 問題：角色代碼不匹配

**資料庫中的角色（DataInitializer.java）：**
```java
Role adminRole = new Role();
adminRole.setCode("ROLE_ADMIN");  // ← 完整的角色代碼
```

**PermissionServiceImpl.java 的常數（修改前）：**
```java
private static final String ROLE_ADMIN = "ADMIN";  // ❌ 少了 ROLE_ 前綴
```

**比對流程：**
```java
// getUserRoleCodes() 從資料庫取得
List<String> roleCodes = ["ROLE_ADMIN"];

// hasRole() 比對
return roleCodes.contains("ADMIN");  // ❌ 找不到！因為是 ROLE_ADMIN vs ADMIN
```

**結果：**
```java
isAdmin(adminUserId) → false  // ❌ 認為不是 Admin
getAccessibleMenuTree() → return new ArrayList<>();  // 回傳空陣列
API Response → { "data": null }  // 轉成 null
```

## ✅ 修復方式

**修改後的 PermissionServiceImpl.java：**
```java
private static final String ROLE_ADMIN = "ROLE_ADMIN";        // ✅ 完整代碼
private static final String ROLE_STORE_OWNER = "ROLE_STORE_OWNER";  // ✅
private static final String ROLE_STORE_EDITOR = "ROLE_STORE_EDITOR";  // ✅
```

現在比對流程：
```java
List<String> roleCodes = ["ROLE_ADMIN"];
return roleCodes.contains("ROLE_ADMIN");  // ✅ 找到了！
```

## 🚀 重新測試步驟

### 步驟 1：等待編譯完成
看到：
```
[INFO] BUILD SUCCESS
```

### 步驟 2：重啟應用
```bash
mvn spring-boot:run
```

或

```bash
java -jar target\admin-1.0.0.jar
```

### 步驟 3：登入
```http
POST http://localhost:8080/api/admin/auth/login
Content-Type: application/json

{
  "username": "admin@kuji.com",
  "password": "admin123"
}
```

### 步驟 4：測試選單 API
```http
GET http://localhost:8080/api/admin/menus/accessible
Authorization: Bearer {你的_access_token}
```

## 📊 預期回應

### ✅ 成功的情況
```json
{
  "success": true,
  "data": [
    {
      "id": "...",
      "name": "店家管理",
      "code": "STORE_MANAGEMENT",
      "path": "/admin/stores",
      "icon": "store",
      "orderNum": 1,
      "children": [
        {
          "id": "...",
          "name": "店家列表",
          "code": "STORE_LIST",
          "path": "/admin/stores/list",
          ...
        },
        {
          "id": "...",
          "name": "新增店家",
          "code": "STORE_CREATE",
          "path": "/admin/stores/create",
          ...
        }
      ]
    },
    {
      "id": "...",
      "name": "商品管理",
      "code": "LOTTERY_MANAGEMENT",
      "path": "/admin/lotteries",
      "icon": "shopping",
      "orderNum": 2,
      "children": [
        {
          "name": "商品列表",
          "code": "LOTTERY_LIST",
          ...
        },
        {
          "name": "新增商品",
          "code": "LOTTERY_CREATE",
          ...
        },
        {
          "name": "獎品管理",
          "code": "PRIZE_MANAGEMENT",
          ...
        }
      ]
    },
    {
      "name": "訂單管理",
      ...
    },
    {
      "name": "會員管理",
      ...
    },
    {
      "name": "報表中心",
      ...
    },
    {
      "name": "權限管理",
      ...
    },
    {
      "name": "系統設定",
      ...
    }
  ],
  "error": null,
  "meta": {
    "timestamp": "2025-12-24T08:25:00.000Z",
    "requestId": "..."
  }
}
```

根據 DataInitializer.java，應該會回傳 **7 個第一層選單**和它們的子選單。

## 🔧 完整流程說明

### 修復前的問題流程
```
1. 用戶登入 → 取得 JWT (含 ROLE_ADMIN)
2. 呼叫 /api/admin/menus/accessible
3. MenuService.getAccessibleMenuTree(adminUserId)
4. permissionService.isAdmin(adminUserId)
   ↓
5. hasRole(adminUserId, "ADMIN")  // ❌ 常數是 "ADMIN"
   ↓
6. getUserRoleCodes(adminUserId) → ["ROLE_ADMIN"]
   ↓
7. roleCodes.contains("ADMIN") → false  // ❌ 不匹配
   ↓
8. isAdmin() 回傳 false
   ↓
9. 進入非 Admin 邏輯 → 查詢 roleIds
   ↓
10. 查詢 RoleMenu → 沒有資料或權限不足
    ↓
11. 回傳空陣列 → data: null
```

### 修復後的流程
```
1. 用戶登入 → 取得 JWT (含 ROLE_ADMIN)
2. 呼叫 /api/admin/menus/accessible
3. MenuService.getAccessibleMenuTree(adminUserId)
4. permissionService.isAdmin(adminUserId)
   ↓
5. hasRole(adminUserId, "ROLE_ADMIN")  // ✅ 常數改為 "ROLE_ADMIN"
   ↓
6. getUserRoleCodes(adminUserId) → ["ROLE_ADMIN"]
   ↓
7. roleCodes.contains("ROLE_ADMIN") → true  // ✅ 匹配！
   ↓
8. isAdmin() 回傳 true
   ↓
9. 直接查詢所有可見選單（is_visible = true）
   ↓
10. 建立選單樹
    ↓
11. 回傳完整選單結構 ✅
```

## 📝 DataInitializer 初始化的選單

根據你的 DataInitializer.java，應該有：

### 第一層選單（7 個）
1. 店家管理 (STORE_MANAGEMENT)
2. 商品管理 (LOTTERY_MANAGEMENT)
3. 訂單管理 (ORDER_MANAGEMENT)
4. 會員管理 (USER_MANAGEMENT)
5. 報表中心 (REPORT_CENTER)
6. 權限管理 (PERMISSION_MANAGEMENT)
7. 系統設定 (SYSTEM_SETTING)

### 第二層選單（12 個）
- 店家管理：店家列表、新增店家
- 商品管理：商品列表、新增商品、獎品管理
- 訂單管理：訂單列表、配送管理
- 報表中心：營收報表、抽獎統計
- 權限管理：角色管理、選單管理、帳號管理

**總共 19 個選單項目**

## 🎯 驗證方式

重啟後測試，如果還是 null，請檢查：

### 1. 資料庫中是否有選單資料
```sql
SELECT COUNT(*) FROM menu;  
-- 應該要有 19 筆

SELECT * FROM menu WHERE is_visible = 1;
-- 應該顯示所有選單
```

### 2. 資料庫中的角色代碼
```sql
SELECT id, name, code FROM role;
-- code 應該是 ROLE_ADMIN, ROLE_STORE_OWNER, ROLE_STORE_EDITOR
```

### 3. 用戶的角色關聯
```sql
SELECT aur.*, r.code 
FROM admin_user_role aur
JOIN role r ON aur.role_id = r.id
WHERE aur.admin_user_id = '70dc7e33-6053-46eb-834e-24087ad436ce';
-- 應該要有一筆，r.code = ROLE_ADMIN
```

## 🐛 如果還是 null

請提供：
1. 上面三個 SQL 查詢的結果
2. 應用程式日誌（查看是否有錯誤）
3. MenuService 的日誌輸出

---

**修復狀態：** ✅ 完成
**預計結果：** data: null → data: [19 個選單項目的樹狀結構]
**需要動作：** 重啟應用並測試

**這次應該可以看到完整的選單資料了！** 🎉
