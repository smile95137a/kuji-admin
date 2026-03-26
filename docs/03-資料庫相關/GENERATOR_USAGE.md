# FullSchemaExampleGenerator 使用說明

## 📋 功能說明

`FullSchemaExampleGenerator` 是專案的**核心程式碼生成器**，會從資料庫讀取表結構，自動生成：

1. **Entity 類別** (`src/main/java/com/group/admin/entity/`)
2. **Example 類別** (`src/main/java/com/group/admin/example/`)
3. **Mapper Interface** (`src/main/java/com/group/admin/mapper/`)
4. **Mapper XML** (`src/main/resources/mapper/`)

## ⚙️ 執行前準備

### 1. 確認資料庫設定

檢查 `FullSchemaExampleGenerator.java` 的連線資訊：

```java
private static final String URL = "jdbc:mysql://localhost:3306/kuji?useSSL=false&serverTimezone=Asia/Taipei";
private static final String USER = "root";
private static final String PASSWORD = "123456";
private static final String SCHEMA = "dream";  // ⚠️ 這裡應該改為 "kuji"
```

**重要**：`SCHEMA` 應該與 `URL` 中的資料庫名稱一致！

### 2. 執行 DDL_UUID.sql

在執行 Generator 之前，必須先建立資料庫表：

```sql
-- 連線到 MySQL
mysql -u root -p

-- 選擇資料庫
USE kuji;

-- 執行 DDL
SOURCE c:/Users/user/OneDrive/Desktop/創業/KUJI-Server/admin/doc/DDL_UUID.sql;
```

## 🚀 執行方式

### 方式 1：在 IDE 中執行（推薦）

1. 開啟 `FullSchemaExampleGenerator.java`
2. 右鍵點擊檔案
3. 選擇「Run 'FullSchemaExampleGenerator.main()'」

### 方式 2：使用 Maven 編譯後執行

```bash
cd c:\Users\user\OneDrive\Desktop\創業\KUJI-Server\admin

# 先編譯
mvn clean compile

# 執行 Generator
mvn exec:java -Dexec.mainClass="com.group.admin.generator.FullSchemaExampleGenerator"
```

## ✅ 執行成功標誌

執行成功後，會看到類似訊息：

```
生成完成: AdminUser (Entity + Example + Mapper XML + Mapper Interface)
生成完成: Role (Entity + Example + Mapper XML + Mapper Interface)
生成完成: Menu (Entity + Example + Mapper XML + Mapper Interface)
...
✅ 全部表格同步完成（含 Example）！
```

## 🔍 生成檔案檢查

執行後，請檢查以下檔案：

### 1. Entity 類別（應為 String ID）

```java
// AdminUser.java
@Data
public class AdminUser {
    private String id;  // ✅ 應為 String，不是 Long
    private String username;
    private String password;
    private String email;
    // ...
}
```

### 2. Example 類別（應為 String）

```java
// AdminUserExample.java
public Criteria andIdEqualTo(String value) {  // ✅ String
    conditions.put("id", value);
    return this;
}
```

### 3. Mapper Interface（應有 selectByExample）

```java
// AdminUserMapper.java
@Mapper
public interface AdminUserMapper {
    int deleteByPrimaryKey(@Param("id") String id);  // ✅ String
    List<AdminUser> selectByExample(AdminUserExample example);  // ✅ 有這方法
    long countByExample(AdminUserExample example);
    // ...
}
```

## 🐛 常見問題

### Q1: 執行時出現 "No suitable driver found"

**原因**：缺少 MySQL JDBC 驅動

**解決**：

1. 確認 `pom.xml` 有以下依賴：

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

2. 執行 `mvn clean install` 下載依賴

### Q2: 執行時出現 "Unknown database 'dream'"

**原因**：SCHEMA 設定錯誤

**解決**：將 `SCHEMA` 改為 `"kuji"`（與 URL 一致）

### Q3: 生成的 ID 欄位仍是 Long

**原因**：資料庫中 ID 欄位型別不是 VARCHAR(36)

**解決**：

1. 確認已執行 `DDL_UUID.sql`
2. 檢查資料庫表結構：

```sql
DESCRIBE admin_user;
-- id 欄位應顯示 varchar(36)
```

### Q4: DataInitializer 報錯 "selectByExample method not found"

**原因**：Mapper Interface 沒有重新生成

**解決**：重新執行 Generator，確保生成新的 Mapper Interface

## 📌 重要提醒

1. **每次修改 DDL 後都要重新執行 Generator**
2. **執行 Generator 會覆蓋現有的 Entity、Example、Mapper 檔案**
3. **手動修改的程式碼會被覆蓋，請備份**
4. **Service 層的程式碼不會被 Generator 影響**

## 🔄 完整工作流程

```
1. 修改 DDL_UUID.sql
   ↓
2. 執行 SQL 建立/更新資料庫表
   ↓
3. 執行 FullSchemaExampleGenerator
   ↓
4. 檢查生成的檔案是否正確
   ↓
5. 編譯專案 (mvn compile)
   ↓
6. 執行測試或啟動應用程式
```

## 📞 需要協助？

如果遇到問題，請提供：

1. Generator 執行的完整錯誤訊息
2. 資料庫表結構 (`DESCRIBE table_name;`)
3. 生成的 Entity 檔案內容

---

**最後更新**: 2025-12-20
**版本**: 1.1 (支援 UUID + selectByExample)
