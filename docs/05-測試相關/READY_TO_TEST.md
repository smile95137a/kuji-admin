# 🎯 變更總結與下一步

## ✅ 已完成的工作

### 1. **本地檔案上傳實作**（取代 S3）
- ✅ 建立 `LocalFileServiceImpl.java`（約 200 行）
- ✅ 刪除 `S3Config.java` 和 `S3ServiceImpl.java`
- ✅ 移除 `pom.xml` 中的 AWS SDK 依賴
- ✅ 更新 `application.yml` 設定
- ✅ 建立圖片儲存目錄結構

**檔案儲存位置：**
- `src/main/resources/static/img/news/`
- `src/main/resources/static/img/banner/`
- `src/main/resources/static/img/lottery/`
- `src/main/resources/static/img/prize/`

**URL 格式：**
- 儲存在資料庫：`/img/news/uuid-123.jpg`
- 前端存取：`http://localhost:8080/img/news/uuid-123.jpg`

### 2. **測試計劃與文件**
- ✅ 建立 `COMPLETE_TEST_PLAN.md`（完整測試計劃，32 個測試案例）
- ✅ 建立 `LOCAL_FILE_UPLOAD_SUMMARY.md`（本地上傳實作說明）
- ✅ 建立 `start-test.bat`（快速啟動腳本）

---

## 📋 變更檔案清單

### 新增檔案（4 個）
1. `src/main/java/com/group/admin/service/impl/LocalFileServiceImpl.java`
2. `COMPLETE_TEST_PLAN.md`
3. `LOCAL_FILE_UPLOAD_SUMMARY.md`
4. `start-test.bat`

### 刪除檔案（2 個）
1. `src/main/java/com/group/admin/config/S3Config.java`
2. `src/main/java/com/group/admin/service/impl/S3ServiceImpl.java`

### 修改檔案（1 個）
1. `src/main/resources/application.yml`（移除 AWS S3 設定，新增本地檔案設定）

### 已還原檔案（1 個）
1. `pom.xml`（從 git 還原，移除 AWS SDK 註解）

---

## 🚀 下一步操作

### 步驟 1：編譯專案
```bash
mvn clean package -DskipTests
```

### 步驟 2：啟動應用程式
```bash
# 方式 1：使用腳本（推薦）
start-test.bat

# 方式 2：手動啟動
mvn spring-boot:run
```

### 步驟 3：取得 Admin Token
```bash
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin@kuji.com\",\"password\":\"admin123\"}"
```

**設定為環境變數：**
```bash
set TOKEN=Bearer eyJhbGc...
```

### 步驟 4：開始測試

參考 `COMPLETE_TEST_PLAN.md`，依序測試：

#### 4.1 Enum API（無需登入）
```bash
curl http://localhost:8080/api/enums/all
curl http://localhost:8080/api/enums/banner-status
```

#### 4.2 店家選項 API（無需登入）
```bash
curl http://localhost:8080/api/stores/options
curl "http://localhost:8080/api/stores/search?keyword=玩具"
```

#### 4.3 圖片上傳 API（需 Admin 權限）
```bash
# 準備測試圖片（test.jpg）
curl -X POST http://localhost:8080/api/admin/upload/news ^
  -H "Authorization: %TOKEN%" ^
  -F "file=@test.jpg"
```

#### 4.4 News 完整 CRUD
```bash
# 新增 News
curl -X POST http://localhost:8080/api/admin/news ^
  -H "Authorization: %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"測試 News\",\"content\":\"測試內容\",\"imageUrl\":\"/img/news/test.jpg\",\"status\":\"DRAFT\"}"

# 查詢列表
curl -X POST http://localhost:8080/api/admin/news/list ^
  -H "Authorization: %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{}"

# 上架 News
curl -X POST http://localhost:8080/api/admin/news/{newsId}/publish ^
  -H "Authorization: %TOKEN%"

# 前台查詢
curl http://localhost:8080/api/news
```

#### 4.5 Banner 完整 CRUD
```bash
# 取得店家選項
curl http://localhost:8080/api/stores/options

# 記下店家 ID（例如：uuid-store-123）

# 新增 Banner
curl -X POST http://localhost:8080/api/admin/banner ^
  -H "Authorization: %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"storeId\":\"uuid-store-123\",\"title\":\"測試 Banner\",\"imageUrl\":\"/img/banner/test.jpg\",\"orderNum\":1,\"status\":\"UNPUBLISHED\"}"

# 查詢列表
curl -X POST http://localhost:8080/api/admin/banner/list ^
  -H "Authorization: %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{}"

# 上架 Banner
curl -X POST http://localhost:8080/api/admin/banner/{bannerId}/publish ^
  -H "Authorization: %TOKEN%"

# 前台查詢輪播
curl http://localhost:8080/api/banner/carousel
```

---

## ✅ 核心驗證清單

### 應用程式啟動
- [ ] 應用程式成功啟動
- [ ] 無 AWS S3 相關錯誤
- [ ] 可存取 Swagger UI：http://localhost:8080/swagger-ui.html

### Enum API
- [ ] GET /api/enums/all 返回所有 Enum
- [ ] 格式正確：`{label: "中文", value: "英文"}`
- [ ] newsStatus 和 bannerStatus 有 description

### 店家選項 API
- [ ] GET /api/stores/options 返回店家列表
- [ ] 格式正確：`[{label, value, description}, ...]`
- [ ] 支援關鍵字搜尋

### 圖片上傳
- [ ] 上傳成功，返回 imageUrl：`/img/{folder}/{uuid}.ext`
- [ ] 檔案儲存在正確位置
- [ ] 可透過瀏覽器存取：`http://localhost:8080/img/...`
- [ ] 檔案驗證正常（大小、類型、副檔名）

### News CRUD
- [ ] 新增、查詢、更新、刪除都正常
- [ ] 上架/下架狀態切換正常
- [ ] 前台只顯示 PUBLISHED 的 News

### Banner CRUD（關鍵！）
- [ ] 新增時可選擇店家（從店家選項 API）
- [ ] 查詢時 storeName 有正確顯示（不是 null）
- [ ] 上架/下架狀態切換正常
- [ ] 前台只顯示 PUBLISHED 且店家 ACTIVE 的 Banner
- [ ] 按 orderNum 升序排列

### 符合需求文件
- [ ] Banner 必須綁定店家（storeId 必填）
- [ ] 只有 Admin 可管理 News 和 Banner
- [ ] News 支援草稿/上架/下架狀態
- [ ] Banner 支援時間排程（startTime/endTime）
- [ ] 店家停用時 Banner 不顯示

### 前端友好性
- [ ] 不需要手動輸入 ID
- [ ] Enum 從 API 取得
- [ ] 店家選項自動載入
- [ ] 圖片上傳後自動帶入 URL

---

## 🐛 可能遇到的問題

### 問題 1：應用程式無法啟動
**檢查點：**
- [ ] 是否還有 S3Config 相關的 Bean 注入錯誤
- [ ] 是否還有 AWS SDK 依賴導致的錯誤

**解決方案：**
```bash
# 確認檔案已刪除
ls src/main/java/com/group/admin/config/S3Config.java  # 應該不存在

# 確認 pom.xml 沒有 AWS SDK
grep -i "aws" pom.xml  # 應該沒有結果
```

### 問題 2：圖片上傳後無法存取
**檢查點：**
- [ ] 檔案是否儲存在正確位置
- [ ] URL 格式是否正確
- [ ] Spring Boot 靜態資源配置是否正確

**解決方案：**
```bash
# 檢查檔案是否存在
dir src\main\resources\static\img\news

# 測試存取
curl http://localhost:8080/img/news/{filename}
```

### 問題 3：Banner 的 storeName 顯示 null
**檢查點：**
- [ ] BannerService 是否正確查詢 Store
- [ ] BannerRes 是否有 storeName 欄位

**解決方案：**
```java
// 檢查 BannerServiceImpl.java 的 toRes() 方法
private BannerRes toRes(Banner banner) {
    Store store = storeMapper.selectByPrimaryKey(banner.getStoreId());
    return BannerRes.builder()
            .storeName(store != null ? store.getStoreName() : null)  // ← 確認有這行
            .build();
}
```

---

## 📚 相關文件

- `COMPLETE_TEST_PLAN.md` - 完整測試計劃（32 個測試案例）
- `LOCAL_FILE_UPLOAD_SUMMARY.md` - 本地上傳實作說明
- `COMPLETE_FEATURE_TEST_GUIDE.md` - 功能測試指南
- `banner.prompt.md` - Banner 需求文件
- `news.prompt.md` - News 需求文件

---

## 💡 建議的 Commit 訊息

```
feat: 實作本地檔案上傳（暫時替代 S3）

✨ 新增功能：
- LocalFileServiceImpl 實作本地檔案上傳
- 圖片儲存在 static/img 目錄
- URL 格式：/img/{folder}/{uuid}.ext

🔧 設定變更：
- 移除 AWS S3 依賴和配置
- 新增本地檔案上傳設定

📝 文件：
- 新增完整測試計劃（32 個測試案例）
- 新增本地上傳實作說明
- 新增快速啟動腳本

🎯 目的：讓應用程式可以在沒有 AWS 憑證的情況下啟動測試

📚 相關檔案：
- LocalFileServiceImpl.java
- application.yml
- COMPLETE_TEST_PLAN.md
- LOCAL_FILE_UPLOAD_SUMMARY.md
- start-test.bat
```

---

**準備就緒！請執行 `start-test.bat` 開始測試！** 🚀
