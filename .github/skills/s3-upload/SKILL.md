# AWS S3 圖片上傳 Skill

## 適用情境
- 新增上傳功能到某個模組（如：商品圖片、獎品圖片）
- 修改支援的圖片格式或大小限制
- 調整 S3 資料夾結構
- 了解如何在本地環境跳過 S3（LocalS3ServiceImpl）
- 更換 S3 bucket 或 region

---

## 核心元件

```
config/S3Config.java           → S3Client + S3Presigner Bean
service/S3Service.java         → 上傳介面
service/impl/S3ServiceImpl.java → 實作（@Profile("prod", "dev")）
controller/admin/UploadController.java → 上傳 API（/admin/upload/**）
```

---

## S3 配置（application.yml）

```yaml
aws:
  s3:
    region: ap-northeast-1           # 東京 region
    bucket-name: kuji-images
    base-url: https://kuji-images.s3.ap-northeast-1.amazonaws.com
    access-key: ${AWS_ACCESS_KEY:}   # 空白時使用 EC2 Instance Profile
    secret-key: ${AWS_SECRET_KEY:}
```

> ⚠️ 正式環境建議使用 EC2 Instance Profile，不要在 yml 硬編碼 access-key。

---

## 現有資料夾結構（S3 內）

| 資料夾 | 用途 | API |
|--------|------|-----|
| `news/` | 最新消息封面圖 | `POST /admin/upload/news` |
| `banner/` | Banner 圖片 | `POST /admin/upload/banner` |
| `lottery/` | 商品圖片 | `POST /admin/upload/lottery` |
| `prize/` | 獎品圖片 | `POST /admin/upload/prize` |
| `store/` | 店家 Logo / 封面 | `POST /admin/upload/store` |
| `avatar/` | 使用者頭像 | `POST /admin/upload/avatar` |

---

## 新增上傳端點（新模組）

在 `UploadController.java` 新增方法：

```java
@PostMapping("/new-module")
@Operation(summary = "上傳新模組圖片")
public ResponseEntity<Map<String, String>> uploadNewModuleImage(
        @RequestParam("file") MultipartFile file) {

    log.info("📤 上傳新模組圖片：{}", file.getOriginalFilename());

    String imageUrl = s3Service.uploadImage(file, "new-module"); // ← 資料夾名稱

    Map<String, String> response = new HashMap<>();
    response.put("imageUrl", imageUrl);

    return ResponseEntity.ok(response);
}
```

---

## S3ServiceImpl 核心邏輯

```java
// 上傳流程
public String uploadImage(MultipartFile file, String folder) {
    // 1. 驗證檔案格式（jpg/jpeg/png/gif/webp）
    // 2. 驗證檔案大小（最大 10MB）
    // 3. 生成唯一檔名（UUID + 原副檔名）
    // 4. S3 Key = folder + "/" + fileName
    // 5. PutObjectRequest（不使用 ACL，bucket 本身設為公開讀取）
    // 6. 返回完整 URL = baseUrl + "/" + s3Key

    String s3Key = folder + "/" + generateUniqueFileName(file.getOriginalFilename());

    PutObjectRequest putReq = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .contentType(file.getContentType())
            .build();

    s3Client.putObject(putReq, RequestBody.fromBytes(file.getBytes()));

    return baseUrl + "/" + s3Key;
}
```

### 生成唯一檔名

```java
private String generateUniqueFileName(String originalFileName) {
    String extension = "";
    if (originalFileName != null && originalFileName.contains(".")) {
        extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
    }
    return UUID.randomUUID().toString() + extension;
    // 範例輸出：550e8400-e29b-41d4-a716-446655440000.jpg
}
```

---

## 驗證規則

```java
// S3ServiceImpl.validateFile()
private static final List<String> ALLOWED_EXTENSIONS = 
    Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

// 驗證副檔名
String extension = getExtension(file.getOriginalFilename());
if (!ALLOWED_EXTENSIONS.contains(extension)) {
    throw new BusinessException("不支援的圖片格式，僅支援：jpg/jpeg/png/gif/webp");
}

// 驗證大小
if (file.getSize() > MAX_FILE_SIZE) {
    throw new BusinessException("圖片大小不能超過 10MB");
}
```

---

## 刪除 S3 圖片

```java
// 從 URL 提取 key 後刪除
s3Service.deleteImage("https://kuji-images.s3.amazonaws.com/lottery/uuid.jpg");

// 內部邏輯（S3ServiceImpl.deleteImage）
// 從 URL 提取 s3Key = "lottery/uuid.jpg"
// DeleteObjectRequest → s3Client.deleteObject(...)
```

> ⚠️ 刪除失敗時不拋出例外（避免影響主流程，僅記錄 log）。

---

## 本地環境（LocalS3ServiceImpl）

當 `spring.profiles.active=local` 時使用本地 stub：

```java
// @Profile("local") 的 LocalS3ServiceImpl
// 不實際上傳，直接返回假 URL
// return "http://localhost:8080/mock-images/" + folder + "/" + UUID.randomUUID() + ".jpg";
```

---

## 上傳 API 權限

```java
// UploadController.java 整個 class 套用
@PreAuthorize("hasRole('ADMIN')")
```

若需要讓 StoreOwner 也能上傳，改為：

```java
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
```

---

## 前端上傳範例

```javascript
const formData = new FormData();
formData.append('file', imageFile);

const response = await axios.post('/api/admin/upload/lottery', formData, {
  headers: {
    'Content-Type': 'multipart/form-data',
    'Authorization': `Bearer ${token}`
  }
});

const imageUrl = response.data.data.imageUrl;
// 將 imageUrl 存入商品的 imageUrl 欄位
```

---

## ⚠️ 禁止操作

- ❌ 不要在 yml 硬編碼 access-key（使用環境變數或 Instance Profile）
- ❌ 不要使用 ACL 設定公開讀取（改用 bucket policy）
- ❌ 上傳前不要跳過格式和大小驗證
- ❌ 不要直接返回 S3 private URL（使用 baseUrl 組合公開路徑）
- ❌ 刪除圖片時不要讓例外中斷主流程
