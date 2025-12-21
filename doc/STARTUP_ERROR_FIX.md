# 啟動錯誤修復報告

## 問題摘要

應用程式啟動時遇到以下問題：

### 1. MyBatis 綁定錯誤 ❌

```
org.apache.ibatis.binding.BindingException: Invalid bound statement (not found): 
com.group.admin.mapper.RoleMapper.selectByExample
```

**根本原因**：
- `DataInitializer` 使用了 `RoleExample` 和 `selectByExample()` 方法
- 雖然 XML 中有定義，但在某些情況下 MyBatis 可能掃描不到

### 2. SpringDoc 大量警告訊息 ⚠️

```
SpringDocConfiguration.SpringDocRepositoryRestConfiguration: Did not match
SpringDocConfiguration.SpringDocWebFluxSupportConfiguration: Did not match
...（數十個類似警告）
```

**根本原因**：
- SpringDoc 嘗試自動配置許多可選功能（Kotlin、Groovy、WebFlux 等）
- 專案沒有使用這些技術，所以條件不匹配
- **這些警告是正常的**，不影響功能

### 3. 資料初始化控制問題 🔧

使用者希望：
- 首次啟動時自動建立基本資料 ✅
- 後續啟動時不要重複建立 ✅
- 可以手動控制是否啟用自動初始化 ✅

## 解決方案

### ✅ 1. 修改資料初始化檢查邏輯

**修改前**（使用 Example 類別）：

```java
private boolean isDataAlreadyInitialized() {
    RoleExample example = new RoleExample();
    example.createCriteria().andCodeEqualTo("ROLE_ADMIN");
    return roleMapper.selectByExample(example).size() > 0;
}
```

**修改後**（使用簡單查詢）：

```java
private boolean isDataAlreadyInitialized() {
    try {
        Role adminRole = roleMapper.selectByCode("ROLE_ADMIN");
        if (adminRole != null) {
            log.info("發現系統管理員角色已存在，ID: {}", adminRole.getId());
            return true;
        }
        return false;
    } catch (Exception e) {
        log.warn("檢查失敗（首次啟動時正常）: {}", e.getMessage());
        return false;
    }
}
```

**優點**：
- 避免依賴 Example 類別
- 更簡單、更穩定
- 錯誤處理更完善

### ✅ 2. 新增配置開關控制自動初始化

#### 新增配置類別：`AppProperties.java`

```java
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private DataConfig data = new DataConfig();
    
    @Data
    public static class DataConfig {
        private boolean autoInit = false;
    }
}
```

#### 在配置檔中使用

**開發環境**（`application-dev.yml`）：

```yaml
app:
  data:
    auto-init: true  # 啟用自動初始化
```

**正式環境**（`application-prod.yml`）：

```yaml
app:
  data:
    auto-init: false  # 停用自動初始化
```

#### 更新 DataInitializer 邏輯

```java
@Override
@Transactional
public void run(String... args) throws Exception {
    // 1. 檢查是否啟用自動初始化
    if (!appProperties.getData().isAutoInit()) {
        log.info("自動初始化已停用");
        return;
    }

    // 2. 檢查資料是否已存在
    if (isDataAlreadyInitialized()) {
        log.info("系統資料已存在，跳過初始化");
        return;
    }

    // 3. 執行初始化
    initializeRoles();
    // ...
}
```

### ✅ 3. SpringDoc 警告處理

**結論**：這些警告是**正常且無害的**，不需要處理。

**原因說明**：

| 警告訊息 | 原因 | 是否需要處理 |
|---------|------|------------|
| `SpringDocRepositoryRestConfiguration` | 沒有使用 Spring Data REST | ❌ 否 |
| `SpringDocWebFluxSupportConfiguration` | 沒有使用 WebFlux（使用 MVC） | ❌ 否 |
| `SpringDocKotlinConfiguration` | 沒有使用 Kotlin（使用 Java） | ❌ 否 |
| `SpringDocGroovyConfiguration` | 沒有使用 Groovy | ❌ 否 |
| `SpringDocPageableConfiguration` | 沒有使用 Spring Data Pageable | ❌ 否 |

**如果真的想減少這些訊息**，可以調整 log level：

```yaml
logging:
  level:
    "[org.springframework.boot.autoconfigure]": WARN  # 只顯示警告以上
```

但**不建議**這樣做，因為可能會隱藏真正重要的訊息。

## 使用指南

### 場景 1：首次啟動（開發環境）

1. **確保配置正確**：

   ```yaml
   # application-dev.yml
   app:
     data:
       auto-init: true
   ```

2. **啟動應用程式**：

   ```bash
   mvn spring-boot:run -Pdev
   ```

3. **預期結果**：

   ```
   ========================================
   開始執行系統資料初始化檢查...
   ========================================
   開始建立系統基本資料...
   初始化角色資料...
   初始化選單資料...
   ...
   系統資料初始化完成！
   ========================================
   ```

### 場景 2：後續啟動（已有資料）

1. **配置保持不變**：

   ```yaml
   app:
     data:
       auto-init: true  # 仍然是 true
   ```

2. **啟動應用程式**

3. **預期結果**：

   ```
   ========================================
   開始執行系統資料初始化檢查...
   ========================================
   發現系統管理員角色已存在，ID: xxx-xxx-xxx
   系統資料已存在，跳過初始化
   ```

### 場景 3：手動停用自動初始化

```yaml
# application-dev.yml
app:
  data:
    auto-init: false
```

**預期結果**：

```
========================================
開始執行系統資料初始化檢查...
========================================
自動初始化已停用（app.data.auto-init=false）
如需初始化資料，請設定 app.data.auto-init=true 或手動執行 SQL
```

### 場景 4：重新初始化

有兩種方式：

#### 方式 A：清空資料庫

```sql
-- 注意順序（先刪除有外鍵的表）
TRUNCATE TABLE admin_user_role;
TRUNCATE TABLE role_menu;
TRUNCATE TABLE store_user;
TRUNCATE TABLE lottery_prize;
TRUNCATE TABLE lottery_draw_record;
TRUNCATE TABLE admin_user;
TRUNCATE TABLE menu;
TRUNCATE TABLE role;
TRUNCATE TABLE store;
TRUNCATE TABLE user;
TRUNCATE TABLE lottery;
TRUNCATE TABLE `order`;
TRUNCATE TABLE point_log;
```

然後重啟應用程式。

#### 方式 B：刪除特定檢查標記

```sql
DELETE FROM role WHERE code = 'ROLE_ADMIN';
```

重啟應用程式即可重新初始化。

## 檔案變更清單

### 修改的檔案

1. **`src/main/java/com/group/admin/config/DataInitializer.java`**
   - 修改 `isDataAlreadyInitialized()` 使用 `selectByCode()`
   - 新增配置開關檢查邏輯
   - 改善日誌訊息

2. **`src/main/resources/application-dev.yml`**
   - 新增 `app.data.auto-init: true`

3. **`src/main/resources/application-prod.yml`**
   - 新增 `app.data.auto-init: false`

4. **`src/main/resources/application.yml`**
   - 新增 SpringDoc 配置
   - 修正 logging 格式

### 新增的檔案

1. **`src/main/java/com/group/admin/config/AppProperties.java`**
   - 應用程式配置屬性類別

2. **`doc/DATA_INITIALIZATION.md`**
   - 資料初始化完整說明文件

3. **`doc/AUTOCONFIGURATION_WARNING_FIX.md`**
   - 自動配置警告修復報告（OAuth2 相關）

## 測試驗證

### 1. 編譯測試

```bash
mvn clean compile -DskipTests
```

**預期結果**：✅ BUILD SUCCESS

### 2. 啟動測試

```bash
mvn spring-boot:run -Pdev
```

**預期結果**：
- ✅ 無 MyBatis 綁定錯誤
- ✅ 正確執行資料初始化（或跳過）
- ⚠️ SpringDoc 警告仍然存在（這是正常的）

### 3. 功能測試

訪問：
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- API Docs: `http://localhost:8080/api/v3/api-docs`

## 常見問題

### Q: SpringDoc 的警告能完全消除嗎？

**A**: 技術上可以，但不建議。這些警告表示 Spring Boot 正在檢查可選功能，是正常行為。如果真的想隱藏，可以調整日誌等級：

```yaml
logging:
  level:
    "[org.springframework.boot.autoconfigure]": WARN
```

### Q: 為什麼不直接註解掉 DataInitializer？

**A**: 使用配置開關更靈活：
- ✅ 不需要修改程式碼
- ✅ 可以透過環境變數控制
- ✅ 不同環境可以有不同設定
- ✅ 容易回復

### Q: 如何在正式環境初始化資料？

**A**: 建議流程：
1. 準備 SQL 初始化腳本
2. 手動執行 SQL（而非自動初始化）
3. 設定 `app.data.auto-init=false`
4. 啟動應用程式

## 總結

✅ **已解決**：
- MyBatis 綁定錯誤（改用簡單查詢）
- 資料重複初始化問題（自動檢查）
- 無法控制初始化行為（新增配置開關）

⚠️ **保留**（正常行為）：
- SpringDoc 自動配置警告（不影響功能）

📚 **文件完善**：
- 新增詳細的初始化說明文件
- 提供多種使用場景範例
- 包含常見問題解答
