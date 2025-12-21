# 🚨 緊急修復：讓伺服器能啟動

## 問題狀況
1. ✅ 已清理：66 個 Entity → 17 個 Entity （只保留此專案需要的）
2. ✅ 已修正：資料庫連線改為 LOCAL (localhost:3306/kuji)
3. ❌ **編譯失敗**：Example 類別缺少必要方法（因為是從舊專案複製來的）

## 立即解決方案

### 步驟 1: 確認 LOCAL 資料庫狀態

請開啟 MySQL Workbench 或其他工具，確認：
- [ ] 資料庫 `kuji` 是否存在？
- [ ] 是否已執行 `doc/DDL_UUID.sql` 建表？

### 步驟 2A: 如果資料庫已建立表格

執行 Generator 重新產生正確的檔案：
```bash
# 重新產生 Entity/Mapper/Example
mvn exec:java -Dexec.mainClass="com.group.admin.generator.FullSchemaExampleGenerator"

# 編譯
mvn clean compile

# 啟動
mvn spring-boot:run -Pdev
```

### 步驟 2B: 如果資料庫還沒建表

#### 方法 1: 使用 MySQL Workbench
1. 開啟 MySQL Workbench
2. 連線到 `localhost:3306`
3. 建立資料庫：`CREATE DATABASE IF NOT EXISTS kuji;`
4. 開啟 `doc/DDL_UUID.sql`
5. 執行整個檔案

#### 方法 2: 使用命令列（如果有 mysql 指令）
```bash
mysql -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS kuji;"
mysql -u root -p123456 kuji < doc/DDL_UUID.sql
```

### 步驟 3: 重新產生檔案並啟動

```bash
# 1. 重新產生
mvn exec:java -Dexec.mainClass="com.group.admin.generator.FullSchemaExampleGenerator"

# 2. 編譯
mvn clean compile

# 3. 啟動伺服器
mvn spring-boot:run -Pdev
```

## 暫時方案：先不修正 Example，只讓伺服器能啟動

如果您只是想先讓伺服器啟動，暫時停用會用到 Example 的功能：

### 修改 `application-dev.yml`
```yaml
app:
  data:
    auto-init: false  # 停用 DataInitializer（避免 Example 錯誤）
```

### 暫時註解問題程式碼
在以下檔案暫時註解掉會出錯的部分：
- `DataInitializer.java` - 整個 `run()` 方法內容
- `PermissionAspect.java` - 第 137 行
- `RoleServiceImpl.java` - 使用 `selectByCode` 的地方

**但這只是暫時方案，不建議使用！**

## 建議做法（最正確）

1. ✅ 確認 LOCAL 資料庫已建立並執行 DDL
2. ✅ 執行 Generator 重新產生完整的 Entity/Mapper/Example
3. ✅ 編譯並啟動伺服器
4. ✅ 測試 API

## 目前狀況總結

| 項目 | 狀態 | 說明 |
|------|------|------|
| 資料庫連線設定 | ✅ 完成 | 已改為 localhost:3306/kuji |
| Entity 清理 | ✅ 完成 | 從 66 個減為 17 個 |
| Mapper 清理 | ✅ 完成 | 從 66 個減為 17 個 |
| Example 清理 | ⚠️ 不完整 | 保留了舊版本的 Example（缺少方法）|
| Mapper XML | ⚠️ 不完整 | 保留了舊版本的 XML（可能不匹配）|
| LOCAL 資料庫 | ❓ 未知 | 需要確認是否已建立表格 |
| 編譯狀態 | ❌ 失敗 | Example 類別缺少方法 |
| 伺服器狀態 | ❌ 無法啟動 | 因為編譯失敗 |

## 下一步

請告訴我：
1. **您的 LOCAL 資料庫狀態如何？**
   - [ ] 已建立 kuji 資料庫
   - [ ] 已執行 DDL 建表
   - [ ] 還沒建立

2. **您希望怎麼處理？**
   - [ ] A: 我要建立資料庫，然後執行 Generator 重新產生
   - [ ] B: 暫時註解問題程式碼，先讓伺服器能啟動
   - [ ] C: 其他建議

我會依您的選擇提供協助！
