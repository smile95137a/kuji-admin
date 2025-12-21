# MyBatis XML 映射檔案未掃描問題修復

## 問題描述

```
org.apache.ibatis.binding.BindingException: Invalid bound statement (not found): 
com.group.admin.mapper.RoleMapper.insert
```

## 根本原因

`application.yml` 中缺少 MyBatis 的配置，導致：
1. Mapper XML 檔案未被掃描
2. MyBatis 無法找到對應的 SQL 語句
3. 雖然 XML 檔案存在於 `src/main/resources/mapper/*.xml`，但沒有告訴 MyBatis 去哪裡找

## 解決方案

在 `src/main/resources/application.yml` 中添加 MyBatis 配置：

```yaml
# MyBatis 配置
mybatis:
  mapper-locations: classpath:mapper/*.xml  # 掃描所有 mapper XML 檔案
  type-aliases-package: com.group.admin.entity  # 實體類別包名
  configuration:
    map-underscore-to-camel-case: true  # 下劃線轉駝峰
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl  # 使用 SLF4J 日誌
```

### 配置說明

| 配置項 | 說明 | 範例 |
|-------|------|------|
| `mapper-locations` | XML 映射檔案的位置 | `classpath:mapper/*.xml` |
| `type-aliases-package` | 實體類別的包名（可省略包名） | `com.group.admin.entity` |
| `map-underscore-to-camel-case` | 資料庫欄位 (snake_case) 自動對應 Java 屬性 (camelCase) | `true` |
| `log-impl` | 日誌實作（顯示SQL語句） | `Slf4jImpl` |

## 驗證

### 1. 檢查 XML 檔案是否存在

```bash
dir src\main\resources\mapper\*.xml
```

應該看到：
- `RoleMapper.xml`
- `MenuMapper.xml`
- `AdminUserMapper.xml`
- ... 等等

### 2. 檢查 XML 檔案是否被打包

```bash
dir target\classes\mapper\*.xml
```

編譯後應該會將 XML 複製到 `target/classes/mapper/` 目錄。

### 3. 啟動應用程式

```bash
mvn spring-boot:run -Pdev
```

**成功的標誌**：
- ✅ 無 "Invalid bound statement" 錯誤
- ✅ 可以看到 DataInitializer 的日誌
- ✅ 資料成功寫入資料庫

##常見陷阱

### ❌ 錯誤 1：XML 檔案路徑錯誤

```yaml
mybatis:
  mapper-locations: classpath:mapper/**/*.xml  # 多一個 **
```

應該是：`classpath:mapper/*.xml`（除非你有子目錄）

### ❌ 錯誤 2：XML 未被打包

檢查 `pom.xml` 中是否排除了 XML 檔案：

```xml
<resources>
    <resource>
        <directory>src/main/resources</directory>
        <excludes>
            <exclude>**/*.xml</exclude>  <!-- 不要這樣做！ -->
        </excludes>
    </resource>
</resources>
```

### ❌ 錯誤 3：namespace 不匹配

檢查 XML 檔案中的 namespace：

```xml
<mapper namespace="com.group.admin.mapper.RoleMapper">  <!-- 必須匹配介面的完整類名 -->
```

### ❌ 錯誤 4：方法名不匹配

確保 XML 中的 `id` 與介面方法名一致：

```java
// RoleMapper.java
public interface RoleMapper {
    int insert(Role role);  // 方法名
}
```

```xml
<!-- RoleMapper.xml -->
<insert id="insert" parameterType="com.group.admin.entity.Role">
    <!-- SQL 語句 -->
</insert>
```

## 最佳實踐

### 1. 開發環境顯示 SQL

```yaml
mybatis:
  configuration:
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

logging:
  level:
    "[com.group.admin.mapper]": DEBUG  # 顯示 SQL 語句
```

### 2. 正式環境隱藏 SQL

```yaml
mybatis:
  configuration:
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

logging:
  level:
    "[com.group.admin.mapper]": WARN  # 只顯示警告
```

### 3. 檔案結構

```
src/main/
├── java/com/group/admin/
│   ├── mapper/              # Mapper 介面
│   │   ├── RoleMapper.java
│   │   └── MenuMapper.java
│   └── entity/              # 實體類別
│       ├── Role.java
│       └── Menu.java
└── resources/
    ├── mapper/              # Mapper XML（必須同名！）
    │   ├── RoleMapper.xml
    │   └── MenuMapper.xml
    └── application.yml
```

## 總結

✅ **核心問題**：缺少 MyBatis 配置導致 XML 未被掃描

✅ **解決方案**：在 `application.yml` 添加 `mybatis.mapper-locations`

✅ **驗證方式**：啟動應用程式，檢查是否有 SQL 日誌輸出

📝 **建議**：始終在專案初期就配置好 MyBatis，避免後續問題
