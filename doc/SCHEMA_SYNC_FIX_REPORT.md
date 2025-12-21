# 🎯 資料庫 Schema 同步問題修正報告

**修正日期**：2025-12-20  
**問題類型**：資料庫欄位缺失 & Generator 格式不一致  
**狀態**：✅ 已完成

---

## 📌 問題描述

### 1. 資料庫 Schema 不一致
**錯誤訊息**：
```
Error updating database. Cause: java.sql.SQLSyntaxErrorException: Unknown column 'code' in 'field list'
SQL: INSERT INTO role (id, name, code, description, created_at, updated_at)
     VALUES (?, ?, ?, ?, ?, ?)
```

**原因**：
- DDL 定義有 `code` 欄位（`doc/DDL_UUID.sql`）
- 實際資料庫的 `role` 表格缺少 `code` 欄位
- DataInitializer 嘗試插入資料時失敗

### 2. Generator 產生格式不一致
**問題**：
- 舊版 `FullSchemaExampleGenerator` 產生的檔案格式簡陋
- 與手動修正的 Entity/Mapper/Example 格式不同
- 當資料庫有變動需要重新產生時，會導致其他 Controller/Service 參數失效

---

## ✅ 解決方案

### 方案一：修正資料庫 Schema

**執行檔案**：`doc/fix_role_table.sql`

```sql
-- 1. 新增 code 欄位
ALTER TABLE role ADD COLUMN code VARCHAR(50) AFTER name;

-- 2. 為現有資料填充預設值
UPDATE role SET code = 'ROLE_ADMIN' WHERE name = '系統管理員';
UPDATE role SET code = 'ROLE_STORE_OWNER' WHERE name = '店家負責人';
UPDATE role SET code = 'ROLE_STORE_EDITOR' WHERE name = '店家編輯';

-- 3. 設定 NOT NULL 與 UNIQUE 約束
ALTER TABLE role 
MODIFY COLUMN code VARCHAR(50) NOT NULL UNIQUE 
COMMENT '角色代碼：ROLE_ADMIN/ROLE_STORE_OWNER/ROLE_STORE_EDITOR';
```

**執行方式**：
```bash
# 使用 MySQL Workbench 或其他 GUI 工具執行 SQL 檔案
# 或透過 command line（若有 mysql client）
mysql -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com \
      -u admin -pEASONlotery!! \
      onekuji < doc/fix_role_table.sql
```

### 方案二：升級 FullSchemaExampleGenerator

**修正項目**：

#### 1. 更新連線資訊
```java
// ⚠️ LOCAL 開發環境設定
private static final String URL = "jdbc:mysql://localhost:3306/kuji?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "123456";
private static final String SCHEMA = "kuji";
```

#### 2. Entity 產生格式（使用 Lombok）
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

#### 3. Example 產生格式（標準 MyBatis Generator）
```java
public class RoleExample {
    protected String orderByClause;
    protected boolean distinct;
    protected List<Criteria> oredCriteria;

    public Criteria createCriteria() { ... }
    public void or(Criteria criteria) { ... }
    public void clear() { ... }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;
        
        public Criteria andCodeEqualTo(String value) { ... }
        public Criteria andCodeIsNull() { ... }
        public Criteria andCodeIsNotNull() { ... }
        public Criteria andCodeLike(String value) { ... }
        public Criteria andCodeIn(List<String> values) { ... }
        public Criteria andCodeBetween(String value1, String value2) { ... }
        // ... 完整的查詢條件方法
    }

    public static class Criteria extends GeneratedCriteria { ... }

    public static class Criterion {
        private String condition;
        private Object value;
        private Object secondValue;
        private boolean noValue;
        private boolean singleValue;
        private boolean betweenValue;
        private boolean listValue;
        // ... getters
    }
}
```

#### 4. Mapper XML 產生格式（與 RoleMapper.xml 一致）
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

  <!-- 完整的 CRUD 方法 -->
  <select id="selectByPrimaryKey" ... />
  <select id="selectAll" ... />
  <insert id="insert" ... />
  <update id="updateByPrimaryKey" ... />
  <delete id="deleteByPrimaryKey" ... />
  
  <!-- Example 查詢方法 -->
  <select id="selectByExample" ... />
  <select id="countByExample" ... />
  <delete id="deleteByExample" ... />
</mapper>
```

#### 5. Mapper Interface 產生格式
```java
@Mapper
public interface RoleMapper {
    // 基本 CRUD
    int deleteByPrimaryKey(@Param("id") String id);
    int insert(Role row);
    Role selectByPrimaryKey(@Param("id") String id);
    List<Role> selectAll();
    int updateByPrimaryKey(Role row);
    
    // Example 相關方法
    List<Role> selectByExample(RoleExample example);
    long countByExample(RoleExample example);
    int deleteByExample(RoleExample example);
}
```

---

## 🚀 使用方式

### 快速執行腳本
```bash
# Windows
run_generator.bat

# 手動執行
mvn compile
mvn exec:java -Dexec.mainClass="com.group.admin.generator.FullSchemaExampleGenerator"
```

### 產生的檔案
- `src/main/java/com/group/admin/entity/*.java`
- `src/main/java/com/group/admin/example/*Example.java`
- `src/main/java/com/group/admin/mapper/*Mapper.java`
- `src/main/resources/mapper/*Mapper.xml`

---

## 📋 完整操作流程

### 初次設定（執行一次）
1. **修正資料庫 Schema**
   ```bash
   # 執行 SQL 修正 role 表格
   # 檔案：doc/fix_role_table.sql
   ```

2. **執行 Generator**
   ```bash
   mvn exec:java -Dexec.mainClass="com.group.admin.generator.FullSchemaExampleGenerator"
   ```

3. **編譯專案**
   ```bash
   mvn clean compile
   ```

4. **測試應用程式**
   ```bash
   mvn spring-boot:run -Pdev
   ```

### 日常使用（資料庫有變動時）
1. **更新 DDL**
   - 修改 `doc/DDL_UUID.sql`

2. **執行 ALTER TABLE**
   - 在資料庫執行欄位變更

3. **執行 Generator**
   ```bash
   run_generator.bat
   ```

4. **檢查受影響的檔案**
   - Service/Controller 中使用到該 Entity 的地方

5. **測試**
   ```bash
   mvn clean compile
   mvn spring-boot:run -Pdev
   ```

---

## ⚠️ 注意事項

### Generator 的行為
- ✅ **會覆蓋**：Entity, Example, Mapper XML, Mapper Interface
- ⚠️ **不會影響**：Controller, Service, DTO, Config
- 💡 **建議**：重要的自訂查詢方法可寫在 `*MapperExt.java` 中

### 資料庫欄位變更影響範圍
1. **Entity**：自動更新（Generator 重新產生）
2. **Example**：自動更新（Generator 重新產生）
3. **Mapper XML/Interface**：自動更新（Generator 重新產生）
4. **Service**：手動檢查（若有使用該欄位）
5. **Controller**：手動檢查（若有使用該欄位）
6. **DTO**：手動更新（需自行同步）

### 最佳實務
- 資料庫 Schema 變更後立即執行 Generator
- 執行 Generator 後檢查受影響的 Service/Controller
- 使用版本控制查看差異（`git diff`）
- 執行完整測試確認功能正常

---

## 📊 修正效果驗證

### Before（修正前）
❌ role 表格缺少 code 欄位  
❌ DataInitializer 執行失敗  
❌ Generator 產生格式簡陋  
❌ 資料庫變更後難以維護  

### After（修正後）
✅ role 表格包含完整欄位（id, name, code, description, created_at, updated_at）  
✅ DataInitializer 可正常執行  
✅ Generator 產生格式與手動修正的一致  
✅ 資料庫變更後一鍵同步  
✅ 不影響現有 Controller/Service 邏輯  

---

## 🎓 相關文件

- **Generator 使用指南**：`doc/GENERATOR_USAGE_GUIDE.md`
- **資料庫 DDL**：`doc/DDL_UUID.sql`
- **修正 SQL**：`doc/fix_role_table.sql`
- **執行腳本**：`run_generator.bat`

---

## 📞 問題排查

| 問題 | 可能原因 | 解決方式 |
|------|----------|----------|
| Generator 連線失敗 | 資料庫連線資訊錯誤 | 檢查 `FullSchemaExampleGenerator` 中的 URL/USER/PASSWORD |
| 編譯錯誤 | Entity import 錯誤 | 執行 `mvn clean compile` |
| Mapper 找不到方法 | Mapper XML 未複製到 target | 檢查 `pom.xml` 的 `<resources>` 設定 |
| 啟動失敗 | 資料庫欄位不一致 | 執行 `doc/fix_role_table.sql` |

---

## ✅ 總結

本次修正完成了：
1. ✅ 資料庫 Schema 同步（新增 code 欄位）
2. ✅ Generator 升級（產生格式統一）
3. ✅ 提供完整的使用指南與執行腳本
4. ✅ 確保未來資料庫變更時可一鍵同步

**未來只需執行 `run_generator.bat` 即可同步所有 Entity/Mapper/Example，不會影響現有業務邏輯！**
