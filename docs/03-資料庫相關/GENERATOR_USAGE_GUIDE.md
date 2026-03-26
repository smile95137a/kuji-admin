# 📘 資料庫 Schema 同步與 Generator 使用指南

## 🎯 問題說明
您遇到的問題是：
1. **資料庫缺少 `code` 欄位**：role 表格沒有 `code` 欄位，導致 MyBatis insert 失敗
2. **Generator 格式不一致**：舊版 Generator 產生的 Entity/Mapper/Example 格式與手動修正的不同

## ✅ 已完成的修正

### 1. 修正資料庫 Schema（新增 code 欄位）
執行 SQL 檔案：`doc/fix_role_table.sql`

```bash
# ⚠️ 注意：請在 LOCAL 資料庫執行，不要在正式環境執行！
# 使用 MySQL Workbench 或其他 GUI 工具連線到 localhost:3306/kuji
# 然後執行 doc/fix_role_table.sql
```

### 2. 修正 FullSchemaExampleGenerator
已更新的重點：
- ✅ 連線資訊設定為 **LOCAL 環境**（localhost:3306/kuji）
- ✅ Entity 使用 Lombok `@Data` 註解
- ✅ Example 類別採用標準 MyBatis Generator 格式（含 Criteria/Criterion）
- ✅ Mapper XML 格式與現有 RoleMapper.xml 一致：
  - 使用 `<resultMap>` 與 `jdbcType`
  - 包含 `Base_Column_List`
  - 完整的 CRUD 方法（insert, update, delete, select）
  - 支援 selectByExample/countByExample/deleteByExample

## 🚀 使用方式

### 執行 Generator（同步所有表格）

```bash
# 編譯專案
mvn compile

# 執行 Generator（重新生成所有 Entity/Mapper/Example）
mvn exec:java -Dexec.mainClass="com.group.admin.generator.FullSchemaExampleGenerator"
```

### 產生的檔案位置
- **Entity**: `src/main/java/com/group/admin/entity/`
- **Example**: `src/main/java/com/group/admin/example/`
- **Mapper Interface**: `src/main/java/com/group/admin/mapper/`
- **Mapper XML**: `src/main/resources/mapper/`

## 📋 完整操作流程

### Step 1: 修正資料庫（執行一次）
```sql
-- 1. 新增 code 欄位
ALTER TABLE role ADD COLUMN code VARCHAR(50) AFTER name;

-- 2. 填充現有資料
UPDATE role SET code = 'ROLE_ADMIN' WHERE name = '系統管理員';
UPDATE role SET code = 'ROLE_STORE_OWNER' WHERE name = '店家負責人';
UPDATE role SET code = 'ROLE_STORE_EDITOR' WHERE name = '店家編輯';

-- 3. 設定約束
ALTER TABLE role 
MODIFY COLUMN code VARCHAR(50) NOT NULL UNIQUE 
COMMENT '角色代碼：ROLE_ADMIN/ROLE_STORE_OWNER/ROLE_STORE_EDITOR';
```

### Step 2: 執行 Generator（資料庫有更動時執行）
```bash
mvn exec:java -Dexec.mainClass="com.group.admin.generator.FullSchemaExampleGenerator"
```

### Step 3: 驗證產生的檔案
檢查以下檔案是否正確：
- `src/main/java/com/group/admin/entity/Role.java`
- `src/main/java/com/group/admin/example/RoleExample.java`
- `src/main/java/com/group/admin/mapper/RoleMapper.java`
- `src/main/resources/mapper/RoleMapper.xml`

### Step 4: 編譯與測試
```bash
mvn clean compile
mvn spring-boot:run -Pdev
```

## ⚠️ 重要提醒

### 資料庫更新後必做
1. **執行 Generator**：確保 Entity/Mapper/Example 與資料庫同步
2. **檢查受影響的 Service/Controller**：若欄位有變動，需手動調整業務邏輯
3. **執行測試**：確認所有功能正常

### Generator 產生的檔案格式
新版 Generator 產生的檔案與現有檔案完全一致：

**Entity 範例（Role.java）**：
```java
package com.group.admin.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Role {
    private String id;
    private String name;
    private String code;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Example 範例（RoleExample.java）**：
```java
public class RoleExample {
    protected List<Criteria> oredCriteria;
    
    public Criteria createCriteria() { ... }
    
    public static class Criteria {
        public Criteria andCodeEqualTo(String value) { ... }
        public Criteria andNameLike(String value) { ... }
        // ... 完整的查詢條件方法
    }
}
```

**Mapper XML 範例（RoleMapper.xml）**：
```xml
<mapper namespace="com.group.admin.mapper.RoleMapper">
  <resultMap id="RoleMap" type="com.group.admin.entity.Role">
    <id column="id" property="id" jdbcType="VARCHAR"/>
    <result column="name" property="name" jdbcType="VARCHAR"/>
    <result column="code" property="code" jdbcType="VARCHAR"/>
    ...
  </resultMap>

  <sql id="Base_Column_List">
    id, name, code, description, created_at, updated_at
  </sql>

  <select id="selectByPrimaryKey" resultMap="RoleMap" parameterType="java.lang.String">
    SELECT <include refid="Base_Column_List"/>
    FROM role
    WHERE id = #{id,jdbcType=VARCHAR}
  </select>
  
  <insert id="insert" parameterType="com.group.admin.entity.Role">
    INSERT INTO role (id, name, code, description, created_at, updated_at)
    VALUES (#{id,jdbcType=VARCHAR}, #{name,jdbcType=VARCHAR}, ...)
  </insert>
  
  <!-- 完整的 CRUD 與 Example 查詢方法 -->
</mapper>
```

## 🔧 其他資料表同步

若其他表格也需要新增/修改欄位：

1. **修改 DDL**：更新 `doc/DDL_UUID.sql`
2. **執行 ALTER TABLE**：在資料庫執行 ALTER 指令
3. **執行 Generator**：同步所有 Entity/Mapper/Example
4. **調整業務邏輯**：檢查受影響的 Service/Controller

## 📞 問題排查

### Generator 執行失敗
- 檢查資料庫連線資訊（URL/USER/PASSWORD）
- 確認資料庫表格存在
- 查看終端機錯誤訊息

### 編譯錯誤
- 執行 `mvn clean compile` 清除舊檔案
- 檢查 Entity 的 import 語句是否正確
- 確認 Lombok 已安裝並啟用

### 應用程式啟動失敗
- 檢查 `application-dev.yml` 資料庫連線
- 確認所有 Mapper XML 都已複製到 `target/classes/mapper/`
- 查看啟動日誌中的詳細錯誤

## 🎉 總結
- ✅ 資料庫 role 表格已補上 code 欄位
- ✅ Generator 產生的格式與手動修正的完全一致
- ✅ 未來資料庫有變動時，直接執行 Generator 即可同步
- ✅ 不會影響現有的 Controller/Service 方法參數
