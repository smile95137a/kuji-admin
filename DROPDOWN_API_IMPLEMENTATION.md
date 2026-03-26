# 下拉選單 API 實作完成報告

## 📋 概述

實作了兩支 Admin 專用的下拉選單 API，用於前端表單中選擇用戶和店家：

1. **店家下拉選單 API** - `GET /admin/stores/all-options`
2. **用戶下拉選單 API** - `GET /admin/users/all-options`

這兩支 API 都是 **Admin 專用**，不做權限過濾，返回所有啟用的選項。

---

## 🏪 店家下拉選單 API

### 端點資訊
- **URL**: `GET /api/admin/stores/all-options`
- **權限**: 僅 `ROLE_ADMIN` 可存取
- **說明**: 返回所有啟用的店家，供下拉選單使用

### 請求範例
```bash
curl -X GET "http://localhost:8080/api/admin/stores/all-options" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 回應範例
```json
{
  "success": true,
  "data": [
    {
      "label": "台北旗艦店",
      "value": "store-uuid-001",
      "description": "ID: store-uuid-001 | 台北市信義區忠孝東路..."
    },
    {
      "label": "高雄分店",
      "value": "store-uuid-002",
      "description": "ID: store-uuid-002 | 高雄市前鎮區中山路..."
    }
  ],
  "error": null
}
```

### 實作位置
- **Controller**: `AdminStoreController.java` (lines 185-215)
- **方法**: `getAllStoreOptions()`

### 查詢邏輯
```java
// 1. 查詢所有 ACTIVE 店家
StoreExample example = new StoreExample();
example.createCriteria().andStatusEqualTo("ACTIVE");
example.setOrderByClause("store_name ASC");

// 2. 轉換為 EnumOption 格式
List<EnumOption> options = stores.stream()
    .map(store -> EnumOption.builder()
        .label(store.getStoreName())
        .value(store.getId())
        .description(String.format("ID: %s | %s", 
            store.getId(), 
            store.getShortDescription()))
        .build())
    .collect(Collectors.toList());
```

---

## 👥 用戶下拉選單 API

### 端點資訊
- **URL**: `GET /api/admin/users/all-options`
- **權限**: 僅 `ROLE_ADMIN` 可存取
- **說明**: 返回所有啟用的後台用戶，供下拉選單使用

### 請求範例
```bash
curl -X GET "http://localhost:8080/api/admin/users/all-options" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 回應範例
```json
{
  "success": true,
  "data": [
    {
      "label": "王大明 (wang@example.com)",
      "value": "user-uuid-001",
      "description": "ID: user-uuid-001 | 角色: 店家負責人"
    },
    {
      "label": "李小華 (lee@example.com)",
      "value": "user-uuid-002",
      "description": "ID: user-uuid-002 | 角色: 店家編輯"
    },
    {
      "label": "管理員 (admin@kuji.com)",
      "value": "user-uuid-003",
      "description": "ID: user-uuid-003 | 角色: 系統管理員"
    }
  ],
  "error": null
}
```

### 實作位置
- **Controller**: `AdminUserController.java` (lines 243-307)
- **方法**: `getAllUserOptions()`
- **輔助方法**: `getRoleDisplayName(String roleCode)`

### 查詢邏輯
```java
// 1. 查詢所有 ACTIVE 用戶
AdminUserExample example = new AdminUserExample();
example.createCriteria().andStatusEqualTo("ACTIVE");
example.setOrderByClause("display_name ASC");
List<AdminUser> users = adminUserMapper.selectByExample(example);

// 2. 為每個用戶查詢角色
for (AdminUser user : users) {
    // 2.1 查詢用戶角色關聯
    AdminUserRoleExample userRoleExample = new AdminUserRoleExample();
    userRoleExample.createCriteria().andAdminUserIdEqualTo(user.getId());
    List<AdminUserRole> userRoles = adminUserRoleMapper.selectByExample(userRoleExample);
    
    // 2.2 取得角色詳情
    String roleId = userRoles.get(0).getRoleId();
    Role role = roleMapper.selectByPrimaryKey(roleId);
    String roleCode = role.getCode(); // "ROLE_ADMIN", "ROLE_STORE_OWNER", etc.
}

// 3. 轉換為 EnumOption 格式
EnumOption option = EnumOption.builder()
    .label(String.format("%s (%s)", user.getDisplayName(), user.getEmail()))
    .value(user.getId())
    .description(String.format("ID: %s | 角色: %s", 
        user.getId(), 
        getRoleDisplayName(roleCode)))
    .build();
```

### 角色顯示名稱對照
| 角色代碼 | 顯示名稱 |
|---------|---------|
| `ROLE_ADMIN` | 系統管理員 |
| `ROLE_STORE_OWNER` | 店家負責人 |
| `ROLE_STORE_EDITOR` | 店家編輯 |
| 其他 | 未知 |

---

## 🛠️ 技術細節

### 依賴注入 (AdminUserController)
新增了以下 Mapper 依賴：
```java
private final AdminUserService adminUserService;
private final AdminUserMapper adminUserMapper;
private final AdminUserRoleMapper adminUserRoleMapper;  // ← 新增
private final RoleMapper roleMapper;                     // ← 新增
```

### Import 清單 (AdminUserController)
```java
import com.group.admin.entity.AdminUser;
import com.group.admin.entity.AdminUserRole;          // ← 新增
import com.group.admin.entity.Role;                   // ← 新增
import com.group.admin.example.AdminUserExample;
import com.group.admin.example.AdminUserRoleExample;  // ← 新增
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;    // ← 新增
import com.group.admin.mapper.RoleMapper;             // ← 新增
```

### EnumOption 格式
```java
public class EnumOption {
    private String label;       // 顯示名稱（前端顯示用）
    private String value;       // 實際值（ID）
    private String description; // 詳細說明（可選，tooltip 或說明文字）
}
```

### 資料庫查詢關聯
用戶角色查詢涉及三張表：
1. **admin_user** - 用戶基本資訊
2. **admin_user_role** - 用戶角色關聯表 (adminUserId, roleId)
3. **role** - 角色詳情 (id, code, name)

查詢流程：
```
AdminUser (id) 
  → AdminUserRole (adminUserId → roleId)
    → Role (id → code)
```

---

## 🎯 使用場景

### 1. 建立店家負責人時選擇店家
前端表單需要選擇「負責哪個店家」：
```javascript
// 取得店家選項
const storeOptions = await fetch('/api/admin/stores/all-options', {
  headers: { 'Authorization': `Bearer ${token}` }
});

// 渲染 <select> 或 <Dropdown>
<Select options={storeOptions.data} />
```

### 2. 指派店家負責人
建立新店家時，選擇「由哪位用戶管理」：
```javascript
// 取得用戶選項
const userOptions = await fetch('/api/admin/users/all-options', {
  headers: { 'Authorization': `Bearer ${token}` }
});

// 渲染用戶選單
<Select 
  options={userOptions.data}
  placeholder="選擇店家負責人"
/>
```

### 3. 篩選器/搜尋條件
在查詢 API 中提供「按用戶篩選」、「按店家篩選」：
```javascript
// 查詢訂單時，提供「按店家篩選」選項
<QueryForm>
  <Select label="店家" options={storeOptions} />
  <Select label="負責人" options={userOptions} />
</QueryForm>
```

---

## 🧪 測試步驟

### 前置準備
1. 確保系統已啟動：`mvn spring-boot:run`
2. 取得 Admin JWT Token（登入 `admin@kuji.com`）
3. 準備 curl 或 Postman

### 測試店家下拉選單
```bash
# 1. 測試成功回應
curl -X GET "http://localhost:8080/api/admin/stores/all-options" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json"

# 預期回應：
# - HTTP 200
# - data 陣列包含所有 ACTIVE 店家
# - 每個選項有 label, value, description

# 2. 測試權限檢查（使用非 Admin token）
curl -X GET "http://localhost:8080/api/admin/stores/all-options" \
  -H "Authorization: Bearer STORE_OWNER_TOKEN"

# 預期回應：
# - HTTP 403 Forbidden
```

### 測試用戶下拉選單
```bash
# 1. 測試成功回應
curl -X GET "http://localhost:8080/api/admin/users/all-options" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json"

# 預期回應：
# - HTTP 200
# - data 陣列包含所有 ACTIVE 用戶
# - label 格式：「顯示名稱 (Email)」
# - description 包含角色中文名稱

# 2. 驗證角色顯示正確
# 檢查回應中的 description 欄位：
# - "角色: 系統管理員" (ROLE_ADMIN)
# - "角色: 店家負責人" (ROLE_STORE_OWNER)
# - "角色: 店家編輯" (ROLE_STORE_EDITOR)

# 3. 測試權限檢查
curl -X GET "http://localhost:8080/api/admin/users/all-options" \
  -H "Authorization: Bearer STORE_OWNER_TOKEN"

# 預期回應：
# - HTTP 403 Forbidden
```

### 測試排序
```bash
# 店家應按 store_name ASC 排序
# 用戶應按 display_name ASC 排序

# 驗證方式：檢查回應中的 label 是否按字母順序
```

---

## ⚠️ 注意事項

### 1. 權限限制
這兩支 API 都是 **Admin 專用**：
- ✅ 使用 `@PreAuthorize("hasRole('ADMIN')")`
- ❌ Store Owner/Editor 無法存取
- ❌ 前台用戶無法存取

### 2. 狀態篩選
只返回 **ACTIVE** 狀態的資料：
- 店家：`status = 'ACTIVE'`
- 用戶：`status = 'ACTIVE'`
- 已停用的不會出現在選項中

### 3. 查詢效能
用戶下拉選單會對每個用戶執行兩次額外查詢：
1. 查詢 `admin_user_role` 表
2. 查詢 `role` 表

**優化建議**（若用戶數量大時）：
- 使用 JOIN 查詢一次取得所有資料
- 或使用 MyBatis ResultMap 關聯查詢
- 或在 Service 層實作批次查詢

目前實作適用於：
- ✅ 用戶數量 < 100
- ❌ 用戶數量 > 1000（建議優化）

### 4. 錯誤處理
如果用戶沒有角色或角色不存在：
- 目前會顯示「角色: 未知」
- 不會拋出異常（容錯設計）

### 5. 快取建議
這兩支 API 的資料變動頻率低，建議：
- 前端快取 5-10 分鐘
- 或使用 Redis 快取在後端
- 當有用戶/店家新增/修改時，清除快取

---

## 📦 部署檢查清單

### 編譯檢查
```bash
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn clean compile -DskipTests
```
✅ 應顯示 `BUILD SUCCESS`

### 打包
```bash
mvn clean package -DskipTests
```
✅ 應生成 `target/admin-1.0.0.jar`

### 本地測試
```bash
mvn spring-boot:run
# 測試兩支 API 是否正常回應
```

### 部署到 EC2
```bash
# 1. 上傳 JAR
scp target/admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/

# 2. 重啟服務
ssh ec2-user@18.179.187.129
sudo systemctl restart kuji-admin
sudo systemctl status kuji-admin

# 3. 測試 API
curl http://18.179.187.129:8080/api/admin/stores/all-options \
  -H "Authorization: Bearer TOKEN"
```

---

## 📊 API 總覽

| API | 方法 | 路徑 | 權限 | 說明 |
|-----|------|------|------|------|
| 店家下拉選單 | GET | `/admin/stores/all-options` | ADMIN | 所有啟用店家 |
| 用戶下拉選單 | GET | `/admin/users/all-options` | ADMIN | 所有啟用用戶含角色 |

---

## ✅ 完成項目

- [x] 實作店家下拉選單 API
- [x] 實作用戶下拉選單 API
- [x] 加入權限檢查 (@PreAuthorize)
- [x] 格式化為 EnumOption 格式
- [x] 查詢用戶角色並顯示中文名稱
- [x] 添加 Swagger 文件註解
- [x] 編譯檢查通過
- [x] 撰寫完整文件

---

## 🔄 後續優化建議

### 短期（選擇性）
1. **批次查詢優化**：用戶下拉選單改用 JOIN 查詢
2. **快取機制**：加入 Redis 快取減少資料庫查詢
3. **分頁支援**：如果資料量大，加入分頁或搜尋功能

### 長期（進階功能）
1. **搜尋功能**：`?keyword=XXX` 支援關鍵字篩選
2. **狀態篩選**：`?status=ACTIVE|INACTIVE` 可選擇狀態
3. **角色篩選**：`?role=STORE_OWNER` 只顯示特定角色用戶
4. **批次 API**：一次請求取得多種選項（用戶+店家+其他）

---

## 📝 相關文件

- [STORE_CRUD_IMPLEMENTATION_COMPLETE.md](./STORE_CRUD_IMPLEMENTATION_COMPLETE.md) - 店家 CRUD 實作
- [copilot-instructions.md](.github/copilot-instructions.md) - 專案架構指南
- Swagger UI: http://localhost:8080/swagger-ui.html

---

**實作日期**: 2025-12-25  
**實作者**: GitHub Copilot  
**版本**: 1.0.0
