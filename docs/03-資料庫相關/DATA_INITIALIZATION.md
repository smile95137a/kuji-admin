# 資料初始化說明

## 概述

`DataInitializer` 會在應用程式啟動時自動執行，負責建立系統所需的基本資料，包括：

- ✅ 系統角色（管理員、店家負責人、店家編輯）
- ✅ 選單與權限
- ✅ 預設管理員帳號
- ✅ 測試店家
- ✅ 測試使用者
- ✅ 測試抽獎商品

## 控制自動初始化

### 方法 1：配置檔控制（推薦）

#### 開發環境（`application-dev.yml`）

```yaml
app:
  data:
    auto-init: true  # 啟用自動初始化
```

#### 正式環境（`application-prod.yml`）

```yaml
app:
  data:
    auto-init: false  # 停用自動初始化
```

### 方法 2：環境變數控制

```bash
# 啟用自動初始化
export APP_DATA_AUTO_INIT=true

# 停用自動初始化
export APP_DATA_AUTO_INIT=false
```

### 方法 3：啟動參數控制

```bash
# 啟用
java -jar admin-1.0.0.jar --app.data.auto-init=true

# 停用
java -jar admin-1.0.0.jar --app.data.auto-init=false
```

## 執行邏輯

### 1. 檢查是否啟用自動初始化

- 如果 `app.data.auto-init=false`，直接跳過，不執行任何初始化
- 如果 `app.data.auto-init=true`，繼續下一步

### 2. 檢查資料是否已存在

- 查詢資料庫中是否存在 `ROLE_ADMIN` 角色
- 如果存在，跳過初始化（避免重複建立）
- 如果不存在，執行初始化

### 3. 執行初始化

按順序建立：
1. 角色資料
2. 選單資料
3. 角色-選單權限關聯
4. 管理員帳號
5. 店家資料
6. 測試使用者
7. 測試抽獎商品

## 使用場景

### 場景 1：本地開發（首次啟動）

```yaml
# application-dev.yml
app:
  data:
    auto-init: true
```

**行為**：首次啟動時自動建立所有測試資料，方便開發測試。

### 場景 2：本地開發（已有資料）

```yaml
# application-dev.yml
app:
  data:
    auto-init: true  # 仍然設為 true
```

**行為**：啟動時檢測到 `ROLE_ADMIN` 已存在，自動跳過初始化。

### 場景 3：重新初始化

有兩種方式：

#### 方式 A：清空資料庫

```sql
-- 清空所有表格（保留結構）
TRUNCATE TABLE admin_user_role;
TRUNCATE TABLE role_menu;
TRUNCATE TABLE admin_user;
TRUNCATE TABLE store_user;
TRUNCATE TABLE menu;
TRUNCATE TABLE role;
TRUNCATE TABLE store;
TRUNCATE TABLE user;
TRUNCATE TABLE lottery_prize;
TRUNCATE TABLE lottery;
-- ... 其他表格
```

然後重啟應用程式（確保 `auto-init=true`）。

#### 方式 B：手動註解 DataInitializer

```java
@Slf4j
// @Component  // 暫時註解這行，停用整個初始化器
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    // ...
}
```

### 場景 4：正式環境部署

```yaml
# application-prod.yml
app:
  data:
    auto-init: false  # 停用自動初始化
```

**原因**：
- 正式環境應該使用經過審核的 SQL 腳本
- 避免誤刪資料後自動重建測試資料
- 更好的控制與審計

**建議做法**：
1. 準備正式環境的 SQL 初始化腳本
2. 在部署時手動執行 SQL
3. 設定 `auto-init=false`

## 預設帳號

初始化完成後會建立以下測試帳號：

### 系統管理員

- **帳號**: `admin@kuji.com`
- **密碼**: `Admin@123`
- **角色**: 系統管理員（最高權限）

### 店家負責人

- **店家 1 負責人**: `store1_owner@kuji.com` / `Store@123`
- **店家 2 負責人**: `store2_owner@kuji.com` / `Store@123`

### 店家編輯

- **店家 1 編輯**: `store1_editor@kuji.com` / `Store@123`

### 一般使用者

- **測試使用者**: `testuser1@test.com` / `Test@123`

⚠️ **安全提醒**：正式環境部署後請**立即修改所有預設密碼**！

## 常見問題

### Q1: 為什麼啟動時報錯 "Invalid bound statement"？

**原因**：MyBatis 無法找到對應的 XML 映射檔案。

**解決方式**：
1. 確認 `RoleMapper.xml` 存在於 `src/main/resources/mapper/` 目錄
2. 確認 XML 中有定義 `selectByCode` 方法
3. 重新編譯：`mvn clean compile`

### Q2: 如何完全停用資料初始化？

**方式 1**（推薦）：

```yaml
app:
  data:
    auto-init: false
```

**方式 2**（徹底）：

註解掉 `@Component` 註解：

```java
// @Component
public class DataInitializer implements CommandLineRunner {
```

### Q3: 如何在不清空資料庫的情況下重新初始化？

**不建議這樣做**，因為會導致資料重複。

如果確實需要，可以：
1. 刪除特定角色：`DELETE FROM role WHERE code = 'ROLE_ADMIN'`
2. 重啟應用程式

### Q4: 正式環境如何初始化資料？

建議流程：

1. **準備 SQL 腳本**（參考 `doc/test_data.sql`）
2. **手動執行 SQL**
   ```bash
   mysql -u username -p database_name < init_data.sql
   ```
3. **設定配置**
   ```yaml
   app:
     data:
       auto-init: false
   ```
4. **啟動應用程式**

## 技術細節

### 檢查邏輯

```java
private boolean isDataAlreadyInitialized() {
    try {
        Role adminRole = roleMapper.selectByCode("ROLE_ADMIN");
        return adminRole != null;
    } catch (Exception e) {
        log.warn("檢查失敗（首次啟動時正常）: {}", e.getMessage());
        return false;
    }
}
```

### 為什麼使用 `selectByCode` 而不是 `selectByExample`？

- `selectByCode` 是簡單的單表查詢，更穩定
- 避免依賴複雜的 Example 類別
- 減少 MyBatis 映射問題

### 事務管理

整個初始化過程使用 `@Transactional` 包裝：
- 如果任何步驟失敗，會全部回滾
- 確保資料一致性

## 相關檔案

- 初始化器：`src/main/java/com/group/admin/config/DataInitializer.java`
- 配置類別：`src/main/java/com/group/admin/config/AppProperties.java`
- 開發環境配置：`src/main/resources/application-dev.yml`
- 正式環境配置：`src/main/resources/application-prod.yml`
- SQL 腳本：`doc/test_data.sql`
