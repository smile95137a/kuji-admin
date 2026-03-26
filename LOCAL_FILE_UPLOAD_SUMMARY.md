# 🎯 本地檔案上傳實作總結

## 📋 目標

1. **暫時移除 S3 依賴**：讓應用程式可以在沒有 AWS 憑證的情況下啟動
2. **本地檔案儲存**：將上傳的圖片儲存在 `src/main/resources/static/img/`
3. **URL 格式統一**：儲存 `/img/xxx.jpg`，前端存取 `http://localhost:8080/img/xxx.jpg`

---

## ✅ 完成的變更

### 1. 建立 `LocalFileServiceImpl.java`

**位置：**`src/main/java/com/group/admin/service/impl/LocalFileServiceImpl.java`

**功能：**
- 實作 `S3Service` 介面（取代 S3ServiceImpl）
- 將檔案儲存在本地 `src/main/resources/static/img/` 目錄
- 返回 URL 格式：`/img/{folder}/{uuid}.jpg`
- 支援檔案驗證（大小、類型、副檔名）
- 支援檔案刪除

**關鍵方法：**
```java
@Override
public String uploadImage(MultipartFile file, String folder) {
    // 1. 驗證檔案（大小、類型、副檔名）
    validateFile(file);
    
    // 2. 生成唯一檔名（UUID + 原始副檔名）
    String fileName = generateUniqueFileName(file.getOriginalFilename());
    
    // 3. 建立目標資料夾（如果不存在）
    String folderPath = basePath + File.separator + folder;
    File targetFolder = new File(folderPath);
    if (!targetFolder.exists()) {
        targetFolder.mkdirs();
    }
    
    // 4. 儲存檔案
    String filePath = folderPath + File.separator + fileName;
    Files.write(Paths.get(filePath), file.getBytes());
    
    // 5. 返回 URL（格式：/img/folder/filename.jpg）
    return "/img/" + folder + "/" + fileName;
}
```

**驗證規則：**
- 檔案大小：最大 5MB
- 支援格式：jpg, jpeg, png, gif, webp
- 內容類型：必須是 `image/*`

### 2. 刪除 S3 相關檔案

**已刪除：**
- `src/main/java/com/group/admin/config/S3Config.java`（AWS S3 配置）
- `src/main/java/com/group/admin/service/impl/S3ServiceImpl.java`（S3 實作）

### 3. 更新 `application.yml`

**位置：**`src/main/resources/application.yml`

**變更：**
```yaml
# 移除 AWS S3 設定
# aws:
#   s3:
#     access-key: ...
#     secret-key: ...

# 新增本地檔案上傳設定
file:
  upload:
    base-path: src/main/resources/static/img  # 檔案儲存路徑
    base-url: http://localhost:8080           # 前端存取的 base URL
```

### 4. 移除 `pom.xml` 中的 AWS SDK 依賴

**已移除（或註解）：**
```xml
<!-- AWS SDK for S3（暫時註解） -->
<!--
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.20.26</version>
</dependency>
-->
```

### 5. 建立圖片儲存目錄結構

**已建立：**
```
src/main/resources/static/img/
├── news/        # News 圖片
├── banner/      # Banner 圖片
├── lottery/     # Lottery 圖片
└── prize/       # Prize 圖片
```

---

## 📂 檔案結構

```
src/main/resources/static/img/
├── news/
│   ├── uuid-123.jpg
│   └── uuid-456.png
├── banner/
│   ├── uuid-789.jpg
│   └── uuid-abc.jpg
├── lottery/
└── prize/
```

---

## 🔧 使用方式

### 後端（不變）

`UploadController.java` 不需要修改，仍然注入 `S3Service`：

```java
@RestController
@RequestMapping("/admin/upload")
public class UploadController {
    
    private final S3Service s3Service;  // Spring 會自動注入 LocalFileServiceImpl
    
    @PostMapping("/news")
    public ResponseEntity<Map<String, String>> uploadNewsImage(
            @RequestParam("file") MultipartFile file) {
        
        // 呼叫 uploadImage()，LocalFileServiceImpl 會處理
        String imageUrl = s3Service.uploadImage(file, "news");
        
        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);  // 返回：/img/news/uuid-123.jpg
        return ResponseEntity.ok(response);
    }
}
```

### 前端

#### 1. 上傳圖片
```javascript
const uploadImage = async (file, type) => {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await axios.post(`/api/admin/upload/${type}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      'Authorization': `Bearer ${token}`
    }
  });
  
  return response.data.data.imageUrl;  // 返回：/img/news/uuid-123.jpg
};
```

#### 2. 新增 News（使用上傳的圖片）
```javascript
const createNews = async () => {
  // 1. 先上傳圖片
  const imageUrl = await uploadImage(imageFile, 'news');
  
  // 2. 新增 News
  const response = await axios.post('/api/admin/news', {
    title: '春節活動',
    content: '活動內容...',
    imageUrl: imageUrl,  // 直接使用：/img/news/uuid-123.jpg
    status: 'DRAFT'
  });
  
  return response.data.data;
};
```

#### 3. 顯示圖片
```html
<!-- 在 HTML 中直接使用 -->
<img :src="news.imageUrl" alt="News Image">

<!-- 瀏覽器會自動補上 domain：http://localhost:8080/img/news/uuid-123.jpg -->
```

---

## 🎯 資料庫儲存格式

**News 表：**
```sql
INSERT INTO news (id, title, content, image_url, status, created_at, updated_at)
VALUES (
    'uuid-news-123',
    '春節活動',
    '活動內容...',
    '/img/news/uuid-456.jpg',  -- ✅ 儲存相對路徑
    'DRAFT',
    NOW(),
    NOW()
);
```

**Banner 表：**
```sql
INSERT INTO banner (id, store_id, title, image_url, order_num, status, created_at, updated_at)
VALUES (
    'uuid-banner-123',
    'uuid-store-456',
    '春節限時優惠',
    '/img/banner/uuid-789.jpg',  -- ✅ 儲存相對路徑
    1,
    'PUBLISHED',
    NOW(),
    NOW()
);
```

---

## 🔄 未來切換回 S3

當有 AWS S3 憑證後，只需要：

### 1. 還原 `pom.xml` 中的 AWS SDK 依賴
```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.20.26</version>
</dependency>
```

### 2. 重新建立 `S3Config.java`
```java
@Configuration
public class S3Config {
    @Bean
    public S3Client s3Client() { ... }
    
    @Bean
    public S3Presigner s3Presigner() { ... }
}
```

### 3. 重新建立 `S3ServiceImpl.java`
```java
@Service
@Primary  // ← 優先使用 S3
public class S3ServiceImpl implements S3Service {
    @Override
    public String uploadImage(MultipartFile file, String folder) {
        // S3 上傳邏輯
        // 返回完整 URL：https://kuji-images.s3.amazonaws.com/news/uuid.jpg
    }
}
```

### 4. 更新 `application.yml`
```yaml
aws:
  s3:
    access-key: ${AWS_ACCESS_KEY}
    secret-key: ${AWS_SECRET_KEY}
    region: ap-northeast-1
    bucket-name: kuji-images
    base-url: https://kuji-images.s3.ap-northeast-1.amazonaws.com
```

### 5. 資料遷移（可選）

如果需要將本地圖片遷移到 S3：

```java
public void migrateToS3() {
    // 1. 查詢所有 News 和 Banner
    List<News> allNews = newsMapper.selectByExample(new NewsExample());
    
    // 2. 上傳本地檔案到 S3
    for (News news : allNews) {
        String localPath = "src/main/resources/static" + news.getImageUrl();
        File file = new File(localPath);
        if (file.exists()) {
            // 上傳到 S3
            String s3Url = s3Service.uploadImage(convertToMultipartFile(file), "news");
            
            // 更新資料庫
            news.setImageUrl(s3Url);
            newsMapper.updateByPrimaryKey(news);
        }
    }
}
```

---

## ⚠️ 注意事項

### 1. 靜態資源存取

Spring Boot 會自動將 `src/main/resources/static/` 目錄下的檔案作為靜態資源提供：

- 儲存路徑：`src/main/resources/static/img/news/uuid-123.jpg`
- 存取 URL：`http://localhost:8080/img/news/uuid-123.jpg`
- 資料庫儲存：`/img/news/uuid-123.jpg`

### 2. 打包後的檔案位置

執行 `mvn package` 後，靜態資源會被複製到：
- JAR 內部：`BOOT-INF/classes/static/img/`
- 無法透過檔案系統直接存取

**解決方案（生產環境）：**
- 使用外部資料夾（如 `/opt/kuji/uploads/`）
- 或直接使用 S3（推薦）

### 3. 開發環境 vs 生產環境

**開發環境：**
```yaml
file:
  upload:
    base-path: src/main/resources/static/img  # ✅ 開發時可用
```

**生產環境：**
```yaml
file:
  upload:
    base-path: /opt/kuji/uploads  # ✅ 使用外部目錄
```

或直接使用 S3（最推薦）。

---

## 📊 測試計劃

詳細測試計劃請參考：`COMPLETE_TEST_PLAN.md`

**核心測試項目：**
1. ✅ 圖片上傳成功，檔案儲存在正確位置
2. ✅ 返回的 URL 格式正確（`/img/{folder}/{uuid}.ext`）
3. ✅ 可以透過瀏覽器存取上傳的圖片
4. ✅ 檔案驗證正確（大小、類型、副檔名）
5. ✅ 圖片刪除功能正常
6. ✅ News 和 Banner 可使用上傳的圖片
7. ✅ 前台可正確顯示圖片

---

## ✅ 優點

1. **無需 AWS 憑證**：可以立即啟動測試
2. **開發方便**：檔案就在專案內，容易檢查
3. **URL 格式一致**：切換到 S3 時，前端不需要修改
4. **成本零**：不需要 S3 費用

## ⚠️ 限制

1. **不適合生產環境**：JAR 打包後無法動態新增檔案
2. **無法擴展**：無法跨伺服器共享檔案
3. **無備份**：檔案遺失無法恢復
4. **效能較差**：大量圖片會佔用應用伺服器資源

---

## 📝 建議

1. **開發/測試階段**：使用 `LocalFileServiceImpl`（當前實作）
2. **正式上線前**：切換到 S3（參考上方切換步驟）
3. **資料庫欄位**：統一儲存相對路徑（`/img/xxx.jpg`）或完整 URL（`https://...`）
4. **前端處理**：判斷是否需要補上 domain（如果是相對路徑）

---

**現在可以開始測試了！** 🚀

**下一步：**
1. 啟動應用程式：`mvn spring-boot:run`
2. 執行測試計劃：參考 `COMPLETE_TEST_PLAN.md`
3. 驗證所有功能正常運作
