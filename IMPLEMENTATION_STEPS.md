# 🚀 News & Banner 模組實作步驟指南

## Step 1: 執行 DDL 建立 news 表

```bash
# 連線到資料庫並執行 DDL
mysql -h onekuji-lotery.cdi42o44miez.ap-northeast-1.rds.amazonaws.com -u admin -p kuji < doc/DDL_news_banner.sql

# 或使用 MySQL Workbench / DBeaver 等工具執行 DDL_news_banner.sql
```

### 驗證 news 表建立成功

```sql
-- 檢查 news 表結構
DESC news;

-- 檢查 banner 表結構
DESC banner;
```

## Step 2: 執行 MyBatis Generator

```bash
# 在專案根目錄執行
mvn mybatis-generator:generate
```

### 預期生成的檔案

Generator 會生成以下檔案：

**News 相關**：
- `entity/News.java` ✅（已手動建立）
- `example/NewsExample.java` ⬅️ Generator 會生成
- `mapper/NewsMapper.java` ⬅️ Generator 會生成
- `mapper/NewsMapper.xml` ⬅️ Generator 會生成

**Banner 相關**：
- `entity/Banner.java` ⬅️ Generator 會生成（覆蓋或新建）
- `example/BannerExample.java` ⬅️ Generator 會生成
- `mapper/BannerMapper.java` ⬅️ Generator 會生成
- `mapper/BannerMapper.xml` ⬅️ Generator 會生成

## Step 3: 確認生成結果

### 檢查檔案是否存在

```bash
# 檢查 News 檔案
ls src/main/java/com/group/admin/entity/News.java
ls src/main/java/com/group/admin/example/NewsExample.java
ls src/main/java/com/group/admin/mapper/NewsMapper.java
ls src/main/resources/mapper/NewsMapper.xml

# 檢查 Banner 檔案
ls src/main/java/com/group/admin/entity/Banner.java
ls src/main/java/com/group/admin/example/BannerExample.java
ls src/main/java/com/group/admin/mapper/BannerMapper.java
ls src/main/resources/mapper/BannerMapper.xml
```

### 檢查 Entity 欄位

確認 `News.java` 包含以下欄位：
- id
- title
- content
- imageUrl
- status
- scheduledAt
- endTime
- createdBy
- createdAt
- updatedAt

確認 `Banner.java` 包含以下欄位：
- id
- storeId
- title
- imageUrl
- orderNum
- status
- startTime
- endTime
- createdAt
- updatedAt

## Step 4: 等待 Copilot 實作 Service & Controller

Generator 執行完成後，告訴我結果，我會繼續實作：
- NewsService & NewsServiceImpl
- BannerService & BannerServiceImpl
- AdminNewsController
- AdminBannerController
- NewsController（前台）
- BannerController（前台）

## 常見問題

### Q1: Generator 執行失敗

**原因**：資料庫連線失敗或 news 表不存在

**解決**：
1. 確認 DDL 已執行成功
2. 檢查 `generatorConfig.xml` 中的資料庫連線資訊
3. 確認資料庫中存在 `news` 表

### Q2: News.java 被覆蓋

**原因**：Generator 會重新生成 Entity

**解決**：沒關係，Generator 生成的 News.java 會包含所有欄位，符合需求

### Q3: 編譯錯誤

**原因**：可能是 DTO 中參考了尚未生成的 Entity

**解決**：Generator 執行後，所有參考都會正確連結

---

## 準備好後告訴我

執行完 Step 1 & 2 後，回覆我：
- ✅ DDL 執行成功
- ✅ Generator 執行成功
- ✅ 檔案已生成

我會立刻開始實作 Service 和 Controller！🚀
