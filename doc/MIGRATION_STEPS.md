# KUJI 系統重置與 UUID 遷移操作手冊

## 📋 執行前檢查清單

- [ ] 已備份重要資料
- [ ] 已閱讀 `UUID_IMPLEMENTATION_GUIDE.md`
- [ ] 確認 MySQL 連線正常
- [ ] 確認 JDK 21 已安裝

---

## 第一步：資料庫重置

### 1.1 備份現有資料（若需要）

```bash
cd c:\Users\user\OneDrive\Desktop\創業\KUJI-Server\admin

# 備份整個資料庫
mysqldump -u root -p kuji_db > backup_before_uuid_$(date +%Y%m%d_%H%M%S).sql
```

### 1.2 執行 UUID DDL

**方式 A：使用 MySQL 命令列**
```bash
# 1. 登入 MySQL
mysql -u root -p

# 2. 刪除並重建資料庫
DROP DATABASE IF EXISTS kuji_db;
CREATE DATABASE kuji_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE kuji_db;

# 3. 執行 UUID DDL
SOURCE doc/DDL_UUID.sql;

# 4. 驗證表結構
SHOW TABLES;
DESC admin_user;
DESC role;
DESC lottery;

# 5. 確認 ID 欄位為 VARCHAR(36)
# 預期結果：id 欄位 Type 為 varchar(36)

# 6. 退出
EXIT;
```

**方式 B：使用 MySQL Workbench**
1. 開啟 MySQL Workbench
2. 連接到本地 MySQL Server
3. 執行以下 SQL：
   ```sql
   DROP DATABASE IF EXISTS kuji_db;
   CREATE DATABASE kuji_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   USE kuji_db;
   ```
4. File → Open SQL Script → 選擇 `doc/DDL_UUID.sql`
5. 執行腳本（⚡ 圖示或 Ctrl+Shift+Enter）
6. 檢查輸出確認無錯誤

### 1.3 驗證資料庫結構

```sql
-- 檢查所有表
SELECT TABLE_NAME, TABLE_COMMENT 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'kuji_db'
ORDER BY TABLE_NAME;

-- 檢查 admin_user 表結構
DESCRIBE admin_user;

-- 檢查外鍵約束
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'kuji_db'
  AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME, CONSTRAINT_NAME;
```

**預期結果：**
- 應有 17 個表（admin_user, role, menu, store, lottery, user, 等）
- 所有 ID 欄位類型為 `varchar(36)`
- 外鍵關聯正確設定

---

## 第二步：暫時註解 DataInitializer

由於 Entity 尚未修改為 UUID，需先暫時停用 DataInitializer 避免編譯錯誤。

### 2.1 修改 DataInitializer.java

開啟 `src/main/java/com/group/admin/config/DataInitializer.java`

**方式 A：註解整個類別**
```java
// @Component  // ← 暫時註解，等 Entity 修改完成後再啟用
public class DataInitializer implements CommandLineRunner {
    // ...
}
```

**方式 B：條件執行**
```java
@Override
public void run(String... args) throws Exception {
    // 暫時跳過，等 Entity 修改完成後再啟用
    log.info("DataInitializer 暫時停用，等待 Entity UUID 遷移完成");
    return;
    
    // log.info("========================================");
    // ...
}
```

### 2.2 測試編譯

```bash
mvn clean compile -DskipTests
```

如果出現編譯錯誤，請檢查：
- DataInitializer 是否已正確註解
- 其他程式碼是否有語法錯誤

---

## 第三步：Entity 類別 UUID 遷移

### 3.1 需要修改的 Entity 清單

**核心 Entity（依優先順序）：**

1. **Role.java**
   ```java
   // 修改前
   private Long id;
   
   // 修改後
   private String id;
   ```

2. **Menu.java**
   ```java
   // 修改前
   private Long id;
   private Long parentId;
   
   // 修改後
   private String id;
   private String parentId;
   ```

3. **AdminUser.java**
   ```java
   // 修改前
   private Long id;
   private Long createdBy;
   private Long updatedBy;
   
   // 修改後
   private String id;
   private String createdBy;
   private String updatedBy;
   ```

4. **AdminUserRole.java**
   ```java
   // 修改前
   private Long id;
   private Long adminUserId;
   private Long roleId;
   
   // 修改後
   private String id;
   private String adminUserId;
   private String roleId;
   ```

5. **RoleMenu.java**
   ```java
   // 修改前
   private Long id;
   private Long roleId;
   private Long menuId;
   
   // 修改後
   private String id;
   private String roleId;
   private String menuId;
   ```

6. **Store.java**
   ```java
   // 修改前
   private Long id;
   private Long ownerId;
   private Long updatedBy;
   
   // 修改後
   private String id;
   private String ownerId;
   private String updatedBy;
   ```

7. **StoreUser.java**
   ```java
   // 修改前
   private Long id;
   private Long storeId;
   private Long adminUserId;
   
   // 修改後
   private String id;
   private String storeId;
   private String adminUserId;
   ```

8. **User.java**
   ```java
   // 修改前
   private Long id;
   
   // 修改後
   private String id;
   ```

9. **Lottery.java**
   ```java
   // 修改前
   private Long id;
   private Long storeId;
   private Long createdBy;
   
   // 修改後
   private String id;
   private String storeId;
   private String createdBy;
   ```

10. **LotteryPrize.java**
    ```java
    // 修改前
    private Long id;
    private Long lotteryId;
    
    // 修改後
    private String id;
    private String lotteryId;
    ```

11. **LotteryDrawRecord.java**
    ```java
    // 修改前
    private Long id;
    private Long lotteryId;
    private Long userId;
    private Long prizeId;
    
    // 修改後
    private String id;
    private String lotteryId;
    private String userId;
    private String prizeId;
    ```

12. **PointLog.java**
    ```java
    // 修改前
    private Long id;
    private Long userId;
    
    // 修改後
    private String id;
    private String userId;
    ```

### 3.2 其他 Entity（暫時保留或刪除）

以下 Entity 若在 DDL_UUID.sql 中未定義，建議暫時刪除或保留但不使用：

- AdminOperationLog.java
- Banner.java
- LotteryLock.java
- RefreshToken.java
- Order.java

若需要這些表，請在 DDL_UUID.sql 中補充對應的 CREATE TABLE 語句。

### 3.3 Entity 修改範例

**以 Role.java 為例：**

```java
package com.group.admin.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Role {
    // ⬇️ 修改這裡：Long → String
    private String id;
    
    private String name;
    private String code;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 若有以下欄位，也請刪除（與 DDL 不符）
    // private Long userId;      // ❌ 刪除
    // private String roleName;  // ❌ 刪除
}
```

### 3.4 批次修改建議

使用 IDE 的搜尋替換功能（Ctrl+Shift+R）：

**步驟：**
1. 開啟 `src/main/java/com/group/admin/entity` 目錄
2. 搜尋：`private Long id;`
3. 替換為：`private String id;`
4. 逐一確認（不要全部自動替換，避免誤改）

**注意：**
- 僅修改 **主鍵 ID** 和 **外鍵 ID** 相關欄位
- 保留其他業務欄位（如 goldCoins, quantity 等仍為 Long）

---

## 第四步：Example 類別檢查

Example 類別由 MyBatis Generator 生成，若 Entity 改為 UUID，Example 中的 Criteria 也需對應調整。

### 4.1 檢查方式

```java
// RoleExample.java
public static class Criteria extends GeneratedCriteria {
    // 檢查這些方法的參數類型
    public Criteria andIdEqualTo(String value) {  // ✅ 應為 String
        // ...
    }
    
    public Criteria andIdIn(List<String> values) {  // ✅ 應為 List<String>
        // ...
    }
}
```

### 4.2 若類型不匹配

**選項 A：手動修改（快速但不推薦）**
- 直接修改 Example 類別中的類型

**選項 B：重新生成（推薦）**
1. 修改 `generatorConfig.xml`
2. 執行 MyBatis Generator
3. 重新生成 Example 類別

---

## 第五步：Mapper XML 調整

### 5.1 檢查 INSERT 語句

**範例：RoleMapper.xml**

**修改前（AUTO_INCREMENT）：**
```xml
<insert id="insert" parameterType="Role" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO role (name, code, description, created_at, updated_at)
    VALUES (#{name}, #{code}, #{description}, NOW(), NOW())
</insert>
```

**修改後（UUID）：**
```xml
<insert id="insert" parameterType="Role">
    INSERT INTO role (id, name, code, description, created_at, updated_at)
    VALUES (#{id}, #{name}, #{code}, #{description}, NOW(), NOW())
</insert>
```

**關鍵修改：**
- ❌ 移除 `useGeneratedKeys="true"`
- ❌ 移除 `keyProperty="id"`
- ✅ 在 INSERT 欄位列表中加入 `id`
- ✅ 在 VALUES 中加入 `#{id}`

### 5.2 批次檢查

檢查所有 `*Mapper.xml` 檔案中的 `<insert>` 標籤。

**需修改的 Mapper XML：**
- RoleMapper.xml
- MenuMapper.xml
- AdminUserMapper.xml
- AdminUserRoleMapper.xml
- RoleMenuMapper.xml
- StoreMapper.xml
- StoreUserMapper.xml
- UserMapper.xml
- LotteryMapper.xml
- LotteryPrizeMapper.xml
- LotteryDrawRecordMapper.xml
- PointLogMapper.xml

---

## 第六步：啟用 DataInitializer

### 6.1 取消註解

恢復 `DataInitializer.java` 的 `@Component` 註解：

```java
@Slf4j
@Component  // ← 恢復註解
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    // ...
}
```

### 6.2 測試編譯

```bash
mvn clean compile -DskipTests
```

**預期結果：**
- 無編譯錯誤
- 所有 Entity 類型匹配

---

## 第七步：執行應用程式

### 7.1 啟動 Spring Boot

```bash
mvn spring-boot:run
```

### 7.2 觀察 Console 日誌

**預期輸出：**
```
========================================
開始執行系統資料初始化...
========================================
初始化角色資料...
✓ 角色資料初始化完成（3 筆）
初始化選單資料...
✓ 選單資料初始化完成（19 筆）
初始化角色權限資料...
✓ 角色權限資料初始化完成
初始化管理者帳號...
✓ 管理者帳號初始化完成（4 筆）
初始化店家資料...
✓ 店家資料初始化完成（2 筆）
初始化測試會員...
✓ 測試會員初始化完成（3 筆）
初始化抽獎商品...
✓ 抽獎商品與獎品初始化完成（2 個商品, 13 個獎品）
========================================
系統資料初始化完成！
========================================

========================================
預設測試帳號資訊
========================================
【後台管理者】
  1. Admin: admin@kuji.com / admin123
  2. StoreOwner1: owner@teststore.com / Test1234
  3. StoreOwner2: owner2@teststore.com / Test1234
  4. StoreEditor: editor@teststore.com / Test1234

【前台會員】
  1. user1@test.com / Test1234 (金點 1000, 紅利 500)
  2. user2@test.com / Test1234 (金點 2500, 紅利 300)
  3. googleuser@gmail.com (Google 登入, 金點 500, 紅利 100)

【測試店家】
  1. KUJI 測試商店 (Owner: owner@teststore.com)
  2. 動漫周邊專賣店 (Owner: owner2@teststore.com)

【測試商品】
  1. 鬼滅之刃一番賞 (80 抽, 已上架)
  2. 咒術迴戰刮刮樂 (100 抽, 已上架)
========================================
```

### 7.3 若出現錯誤

**常見錯誤 A：資料已存在**
```
系統資料已存在，跳過初始化
```
**解決方式：**
- 這是正常的（防止重複初始化）
- 若要重新初始化，請清空資料庫後重啟

**常見錯誤 B：類型轉換錯誤**
```
java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Long
```
**解決方式：**
- 檢查是否有遺漏的 Entity 欄位未改為 String
- 檢查 Mapper XML 是否有參數類型錯誤

**常見錯誤 C：外鍵約束錯誤**
```
Cannot add or update a child row: a foreign key constraint fails
```
**解決方式：**
- 檢查資料插入順序（先插入父表再插入子表）
- 檢查 UUID 是否正確設定

---

## 第八步：資料庫驗證

### 8.1 檢查資料是否正確載入

```sql
USE kuji_db;

-- 檢查角色
SELECT * FROM role;

-- 檢查選單
SELECT * FROM menu ORDER BY parent_id, order_num;

-- 檢查管理者帳號
SELECT id, username, email, display_name, status FROM admin_user;

-- 檢查店家
SELECT id, store_name, owner_id, status FROM store;

-- 檢查抽獎商品
SELECT id, title, category, status, price_per_draw FROM lottery;

-- 檢查獎品
SELECT lp.id, l.title AS lottery_title, lp.level, lp.name, lp.quantity, lp.remaining
FROM lottery_prize lp
JOIN lottery l ON lp.lottery_id = l.id
ORDER BY l.id, lp.order_num;
```

### 8.2 驗證 UUID 格式

```sql
-- 檢查 ID 是否為 UUID 格式
SELECT id, name 
FROM role 
WHERE id NOT REGEXP '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

-- 若有結果，表示有非 UUID 格式的 ID（異常）
-- 預期結果：Empty set (0.00 sec)
```

### 8.3 驗證外鍵關聯

```sql
-- 檢查角色選單權限關聯
SELECT 
    r.name AS role_name,
    m.name AS menu_name,
    rm.can_view,
    rm.can_edit,
    rm.can_delete
FROM role_menu rm
JOIN role r ON rm.role_id = r.id
JOIN menu m ON rm.menu_id = m.id
WHERE r.code = 'ROLE_ADMIN'
LIMIT 10;
```

---

## 第九步：API 測試

### 9.1 使用 Postman 或 cURL 測試

**測試 1：登入（取得 JWT Token）**
```bash
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@kuji.com",
    "password": "admin123"
  }'
```

**預期回應：**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": "550e8400-e29b-41d4-a716-446655440000",  // ← UUID 格式
      "username": "admin@kuji.com",
      "displayName": "系統管理員"
    }
  }
}
```

**測試 2：查詢角色列表**
```bash
curl -X GET http://localhost:8080/api/admin/roles \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**預期回應：**
```json
{
  "success": true,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",  // ← UUID
      "name": "系統管理員",
      "code": "ROLE_ADMIN",
      "description": "平台最高權限管理者"
    },
    {
      "id": "660e9511-f3ac-52e5-b827-557766551111",  // ← UUID
      "name": "店家負責人",
      "code": "ROLE_STORE_OWNER"
    }
  ]
}
```

**測試 3：查詢店家列表**
```bash
curl -X GET http://localhost:8080/api/admin/stores \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**測試 4：查詢抽獎商品**
```bash
curl -X GET http://localhost:8080/api/admin/lotteries \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 第十步：測試案例執行

### 10.1 執行單元測試

```bash
mvn clean test
```

### 10.2 檢查測試結果

**若有失敗案例：**
- 檢查測試程式碼中的 ID 類型是否為 String
- 檢查測試資料是否正確設定 UUID

**範例修正：**
```java
// 修改前
@Test
public void testGetRoleById() {
    Long roleId = 1L;  // ❌
    Role role = roleService.getRoleById(roleId);
    assertNotNull(role);
}

// 修改後
@Test
public void testGetRoleById() {
    String roleId = "550e8400-e29b-41d4-a716-446655440000";  // ✅
    Role role = roleService.getRoleById(roleId);
    assertNotNull(role);
}
```

---

## ✅ 完成檢查清單

### 資料庫
- [ ] 執行 DDL_UUID.sql 成功
- [ ] 所有表的 ID 欄位為 VARCHAR(36)
- [ ] 外鍵約束正確設定

### 程式碼
- [ ] 所有核心 Entity 的 ID 欄位改為 String
- [ ] 所有 Mapper XML 的 INSERT 語句已調整
- [ ] DataInitializer 編譯無錯誤

### 執行驗證
- [ ] Spring Boot 啟動成功
- [ ] DataInitializer 執行成功
- [ ] 資料庫資料正確載入（role, menu, admin_user, store, lottery 等）
- [ ] 所有 ID 為 UUID 格式

### API 測試
- [ ] 登入 API 正常（回傳 UUID 格式的 userId）
- [ ] 角色 API 正常
- [ ] 店家 API 正常
- [ ] 抽獎 API 正常

### 測試案例
- [ ] 單元測試通過
- [ ] 整合測試通過

---

## 🎉 完成！

恭喜您完成 KUJI 系統的 UUID 遷移！

現在系統已全面採用 UUID 作為主鍵，並已載入完整的測試資料。

**下一步建議：**
1. 繼續完成其他 Controller 的開發
2. 補充剩餘的 Entity 與 Mapper
3. 完善單元測試與整合測試
4. 部署到測試環境進行完整驗證

---
**文件建立日期**：2025-12-18  
**最後更新**：2025-12-18  
**版本**：1.0
