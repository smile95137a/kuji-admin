# KUJI 系統 UUID 主鍵策略實施指南

## 背景說明

根據用戶需求，系統需要全面採用 UUID 作為主鍵策略，取代原有的流水號（AUTO_INCREMENT）。

## 實施步驟

### 第一階段：資料庫層修改

#### 1.1 新建 DDL（已完成）
- 檔案：`doc/DDL_UUID.sql`
- 所有主鍵從 `BIGINT AUTO_INCREMENT` 改為 `VARCHAR(36)`
- 外鍵關聯同步修改為 `VARCHAR(36)`

#### 1.2 執行腳本
```sql
-- 1. 備份現有資料（若有）
mysqldump -u root -p kuji_db > backup_$(date +%Y%m%d).sql

-- 2. 清空並重建資料庫
DROP DATABASE IF EXISTS kuji_db;
CREATE DATABASE kuji_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kuji_db;

-- 3. 執行新 DDL
SOURCE doc/DDL_UUID.sql;
```

### 第二階段：Entity 類別修改

#### 2.1 需修改的核心 Entity 列表

以下 Entity 類別的 ID 欄位需從 `Long` 改為 `String`：

**後台系統：**
- `AdminUser.java` - id, createdBy, updatedBy
- `Role.java` - id
- `Menu.java` - id, parentId
- `AdminUserRole.java` - id, adminUserId, roleId
- `RoleMenu.java` - id, roleId, menuId
- `Store.java` - id, ownerId, updatedBy
- `StoreUser.java` - id, storeId, adminUserId
- `Banner.java` - id, storeId
- `AdminOperationLog.java` - id, adminId, targetId

**前台系統：**
- `User.java` - id
- `PointLog.java` - id, userId, referenceId
- `RefreshToken.java` - id, userId

**抽獎系統：**
- `Lottery.java` - id, storeId, createdBy
- `LotteryPrize.java` - id, lotteryId
- `LotteryLock.java` - id, lotteryId, userId
- `LotteryDrawRecord.java` - id, lotteryId, userId, prizeId

**訂單系統：**
- `Order.java` - id, userId

#### 2.2 修改範例

**修改前：**
```java
@Data
public class Role {
    private Long id;  // ❌ BIGINT
    private String name;
    private String code;
    // ...
}
```

**修改後：**
```java
@Data
public class Role {
    private String id;  // ✅ VARCHAR(36) UUID
    private String name;
    private String code;
    // ...
}
```

#### 2.3 自動生成策略

由於使用 MyBatis Generator (MBG)，建議流程：

**選項 A：手動修改（推薦，避免覆蓋其他欄位）**
1. 僅修改 Entity 中的 ID 相關欄位類型
2. 保留其他欄位不變
3. 避免重新執行 MBG 導致欄位遺失

**選項 B：重新生成（需謹慎）**
1. 修改 `generatorConfig.xml`
2. 針對 ID 欄位指定 `javaType="java.lang.String"`
3. 執行 MBG 重新生成
4. ⚠️ 風險：可能覆蓋手動修改的欄位

### 第三階段：Mapper XML 修改

#### 3.1 主鍵生成策略調整

**修改前（AUTO_INCREMENT）：**
```xml
<insert id="insert" parameterType="Role" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO role (name, code, description)
    VALUES (#{name}, #{code}, #{description})
</insert>
```

**修改後（UUID）：**
```xml
<insert id="insert" parameterType="Role">
    INSERT INTO role (id, name, code, description)
    VALUES (#{id}, #{name}, #{code}, #{description})
</insert>
```

#### 3.2 注意事項
- 移除 `useGeneratedKeys="true"` 和 `keyProperty="id"`
- 在 INSERT 語句中明確包含 `id` 欄位
- 應用層需在插入前使用 `UUID.randomUUID().toString()` 生成 ID

### 第四階段：Service 層修改

#### 4.1 新增實體時生成 UUID

**範例：**
```java
@Service
public class RoleService {
    
    @Autowired
    private RoleMapper roleMapper;
    
    public void createRole(Role role) {
        // 生成 UUID
        role.setId(UUID.randomUUID().toString());
        
        // 插入資料庫
        roleMapper.insert(role);
    }
}
```

#### 4.2 查詢時使用 String 類型

```java
public Role getRoleById(String id) {
    return roleMapper.selectByPrimaryKey(id);
}

public void deleteRole(String id) {
    roleMapper.deleteByPrimaryKey(id);
}
```

### 第五階段：DataInitializer 資料載入器

#### 5.1 已建立檔案
- `src/main/java/com/group/admin/config/DataInitializer.java`
- 實作 `CommandLineRunner` 介面
- Spring Boot 啟動時自動執行

#### 5.2 功能說明
- 檢查資料是否已初始化（避免重複執行）
- 使用 UUID 生成所有主鍵
- 載入預設資料：
  - 3 個角色（Admin, StoreOwner, StoreEditor）
  - 19 個選單項目（含階層結構）
  - 角色選單權限對應
  - 4 個測試管理者帳號
  - 2 個測試店家
  - 3 個測試前台會員
  - 2 個測試抽獎商品（含 13 個獎品）

#### 5.3 執行條件
- 僅在 `role` 表無 `ROLE_ADMIN` 資料時執行
- 避免重複初始化

### 第六階段：Controller 層適配

#### 6.1 路徑參數調整

**修改前：**
```java
@GetMapping("/{id}")
public ResponseEntity<Role> getRole(@PathVariable Long id) {
    Role role = roleService.getRoleById(id);
    return ResponseEntity.ok(role);
}
```

**修改後：**
```java
@GetMapping("/{id}")
public ResponseEntity<Role> getRole(@PathVariable String id) {
    Role role = roleService.getRoleById(id);
    return ResponseEntity.ok(role);
}
```

#### 6.2 請求體驗證

若有自訂驗證器檢查 ID 格式，需改為 UUID 驗證：

```java
public class UUIDValidator implements ConstraintValidator<ValidUUID, String> {
    private static final Pattern UUID_PATTERN = 
        Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return UUID_PATTERN.matcher(value.toLowerCase()).matches();
    }
}
```

### 第七階段：測試與驗證

#### 7.1 單元測試調整

```java
@Test
public void testCreateRole() {
    Role role = new Role();
    role.setName("測試角色");
    role.setCode("ROLE_TEST");
    
    // 不再需要驗證 ID 為 null（因為會自動生成 UUID）
    roleService.createRole(role);
    
    // 驗證 ID 為 UUID 格式
    assertNotNull(role.getId());
    assertTrue(role.getId().matches("^[0-9a-f-]{36}$"));
}
```

#### 7.2 資料庫驗證

```sql
-- 檢查 ID 格式
SELECT id, name FROM role;
-- 預期結果：id 為 UUID 格式（如 550e8400-e29b-41d4-a716-446655440000）

-- 檢查外鍵關聯
SELECT r.name AS role_name, m.name AS menu_name
FROM role_menu rm
JOIN role r ON rm.role_id = r.id
JOIN menu m ON rm.menu_id = m.id;
```

## 完整執行檢查清單

### 資料庫準備
- [ ] 備份現有資料庫
- [ ] 執行 `DDL_UUID.sql` 建立新結構
- [ ] 驗證所有表的 ID 欄位為 `VARCHAR(36)`

### 程式碼修改
- [ ] 修改所有 Entity 的 ID 欄位為 `String`
- [ ] 修改所有 Mapper XML 的 INSERT 語句
- [ ] 修改所有 Service 層的 ID 參數類型
- [ ] 修改所有 Controller 層的 ID 參數類型
- [ ] 建立 DataInitializer（已完成）

### 編譯與測試
- [ ] Maven 編譯無錯誤：`mvn clean compile`
- [ ] 執行單元測試：`mvn test`
- [ ] 啟動應用程式：`mvn spring-boot:run`
- [ ] 檢查 DataInitializer 執行日誌
- [ ] 驗證資料庫資料正確載入

### API 測試
- [ ] 測試 Role 相關 API
- [ ] 測試 Menu 相關 API
- [ ] 測試 AdminUser 相關 API
- [ ] 測試 Store 相關 API
- [ ] 測試 Lottery 相關 API
- [ ] 驗證所有回應的 ID 為 UUID 格式

## 常見問題與解決方案

### Q1: MBG 重新生成後 Entity 的 ID 又變回 Long？
**A:** 修改 `generatorConfig.xml`：
```xml
<table tableName="role">
    <columnOverride column="id" javaType="java.lang.String" />
</table>
```

### Q2: 外鍵關聯查詢失敗？
**A:** 確認外鍵欄位也改為 `String` 類型，例如：
- `AdminUserRole.adminUserId` → `String`
- `AdminUserRole.roleId` → `String`

### Q3: 性能影響？
**A:** VARCHAR(36) 相比 BIGINT(8 bytes)：
- 儲存空間：36 bytes vs 8 bytes（約 4.5 倍）
- 索引效能：略降（但對千萬級以下資料影響不大）
- 優點：全域唯一、分散式友好、無需中央序號生成器

### Q4: 前端如何處理 UUID？
**A:** JavaScript 直接使用字串：
```javascript
const roleId = "550e8400-e29b-41d4-a716-446655440000";
axios.get(`/api/roles/${roleId}`);
```

## 總結

採用 UUID 策略的優勢：
1. ✅ 全域唯一，無衝突風險
2. ✅ 分散式系統友好
3. ✅ 無需中央序號管理
4. ✅ 安全性較高（無法推測其他 ID）

實施後的系統架構：
- 資料庫：所有 ID 欄位為 `VARCHAR(36)`
- 程式碼：所有 ID 為 `String` 類型
- 新增時：使用 `UUID.randomUUID().toString()`
- 查詢時：直接使用 `String` 參數

---
**建立日期**：2025-12-18  
**文件版本**：1.0  
**相關檔案**：
- `doc/DDL_UUID.sql`
- `src/main/java/com/group/admin/config/DataInitializer.java`
