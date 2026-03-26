# Quickstart: 橫幅廣告管理 (Banner Management)

**功能**: `001-banner-management`  
**Branch**: `001-banner-management`  
**最後更新**: 2026-03-22

---

## 前置條件

| 需求 | 版本 | 備註 |
|-------------|---------|-------|
| Java | 21 | `java --version` |
| Maven | 3.9+ | `mvn --version` |
| MySQL | 8.3 | AWS RDS 或本地 Docker |
| AWS S3 | — | Bucket: `test-ourkuji`, region: `ap-northeast-1` |

---

## 1. 資料庫設定

### 套用 Banner 資料表 DDL

若 `banner` 資料表尚未存在：

```sql
CREATE TABLE IF NOT EXISTS `banner` (
  `id`          VARCHAR(36)  NOT NULL,
  `store_id`    VARCHAR(36)  NOT NULL,
  `title`       VARCHAR(200) DEFAULT NULL,
  `image_url`   VARCHAR(500) NOT NULL,
  `link_url`    VARCHAR(500) DEFAULT NULL,
  `order_num`   INT          NOT NULL DEFAULT 0,
  `status`      VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
  `start_time`  DATETIME     DEFAULT NULL,
  `end_time`    DATETIME     DEFAULT NULL,
  `created_at`  DATETIME     NOT NULL,
  `updated_at`  DATETIME     NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_banner_store_id`       (`store_id`),
  INDEX `idx_banner_status_order`   (`status`, `order_num`, `created_at`),
  INDEX `idx_banner_schedule`       (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

> 資料表可能已存在於您的環境中。請先執行 `SHOW TABLES LIKE 'banner';` 確認。

### 插入測試資料（選用）

```sql
-- Requires an existing store ID from your local DB
-- Replace 'YOUR_STORE_ID' with a real store UUID

INSERT INTO banner (id, store_id, title, image_url, order_num, status, created_at, updated_at)
VALUES (
  UUID(),
  'YOUR_STORE_ID',
  '測試廣告 — 首頁輪播',
  'https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/banner/test-banner.jpg',
  1,
  'PUBLISHED',
  NOW(),
  NOW()
);
```

---

## 2. 建構與執行

```bash
# From project root: C:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn clean package -DskipTests

# Run with dev profile
java -jar target/admin-*.jar --spring.profiles.active=dev
```

或使用現有啟動腳本：

```bash
./start.sh   # Linux/Mac
start-test.bat  # Windows dev
```

伺服器啟動位址：`http://localhost:8080/api`

---

## 3. 認證

所有管理後台端點均需 JWT token。請透過以下方式取得：

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "your_password"}'
```

從回應中複製 `token`，並在後續請求中以 `Authorization: Bearer <token>` 帶入。

---

## 4. 圖片上傳（建立 Banner 前必須先完成）

請先上傳廣告圖片，再將回傳的 URL 填入廣告建立請求：

```bash
curl -X POST http://localhost:8080/api/admin/upload \
  -H "Authorization: Bearer <token>" \
  -F "file=@/path/to/banner-image.jpg" \
  -F "folder=banner"
```

**回應：**
```json
{ "url": "https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/banner/2026/banner-image.jpg" }
```

---

## 5. 核心 API 流程

### 5.1 建立廣告（管理員）

```bash
curl -X POST http://localhost:8080/api/admin/banners \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "storeId":   "YOUR_STORE_UUID",
    "title":     "春季特賣活動",
    "imageUrl":  "https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/banner/2026/spring-sale.jpg",
    "orderNum":  1,
    "status":    "PUBLISHED"
  }'
```

預期結果：`HTTP 201`，回傳完整 `BannerRes` JSON。

### 5.2 查詢所有廣告（管理員）

```bash
curl -X POST http://localhost:8080/api/admin/banners/list \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{}'
```

依狀態篩選：

```bash
curl -X POST http://localhost:8080/api/admin/banners/list \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"condition": {"status": "PUBLISHED"}}'
```

### 5.3 更新廣告

```bash
curl -X PUT http://localhost:8080/api/admin/banners/<BANNER_ID> \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"orderNum": 2, "status": "UNPUBLISHED"}'
```

預期結果：`HTTP 200`，回傳更新後的 `BannerRes`。

### 5.4 刪除廣告

```bash
curl -X DELETE http://localhost:8080/api/admin/banners/<BANNER_ID> \
  -H "Authorization: Bearer <token>"
```

預期結果：`HTTP 204 No Content`。

### 5.5 公開輪播（無需認證）

```bash
curl http://localhost:8080/api/banners
```

僅回傳連結至 ACTIVE 店家的 PUBLISHED 廣告，依 `orderNum ASC` 排序。若無則回傳空陣列 `[]`。

---

## 6. 排程狀態轉換

廣告排程器在應用程式啟動後每 **60 秒**自動執行。若要確認其是否運作中，請在應用程式日誌中查找類似以下的訊息：

```
[scheduled] Banner auto-publish: 0 banners published
[scheduled] Banner auto-unpublish: 1 banner unpublished
```

手動測試排程的步驟：
1. 建立一則 `startTime` 為未來 1 分鐘後的廣告。
2. 等待 60–120 秒。
3. 查詢 `GET /api/banners`——該廣告應已出現。

---

## 7. 店家與廣告的可見性測試

驗證 FR-008（停用店家時隱藏其廣告）：

```sql
-- Deactivate a store
UPDATE store SET status = 'INACTIVE' WHERE id = 'YOUR_STORE_ID';
```

接著呼叫 `GET /api/banners`——連結至該店家的廣告應不再顯示。

重新啟用：
```sql
UPDATE store SET status = 'ACTIVE' WHERE id = 'YOUR_STORE_ID';
```

再次呼叫 `GET /api/banners`——廣告應重新出現（前提是仍在排程時間內且狀態為 PUBLISHED）。

---

## 8. 執行測試

```bash
# All tests
mvn test

# Only banner-related tests
mvn test -Dtest="AdminBannerControllerTest,BannerControllerTest"
```

測試檔案：
- `src/test/java/com/group/admin/controller/admin/AdminBannerControllerTest.java`
- `src/test/java/com/group/admin/controller/api/BannerControllerTest.java`

---

## 9. 關鍵檔案參考

| 檔案 | 用途 |
|------|---------|
| `entity/Banner.java` | Banner 實體（UUID 主鍵，無 JPA） |
| `controller/admin/AdminBannerController.java` | 管理後台 CRUD 端點（`/admin/banners`） |
| `controller/api/BannerController.java` | 公開輪播端點（`/banners`） |
| `service/BannerService.java` | Service 介面 |
| `service/impl/BannerServiceImpl.java` | 業務邏輯、店家 JOIN、級聯邏輯 |
| `mapper/BannerMapper.java` | MyBatis mapper 介面 |
| `resources/mapper/BannerMapper.xml` | SQL（含自訂 `selectActiveBanners`） |
| `scheduler/ScheduledTasks.java` | 每 60 秒自動發布/下架執行器 |
| `req/BannerCreateReq.java` | 建立請求 DTO |
| `req/BannerUpdateReq.java` | 更新請求 DTO |
| `res/BannerRes.java` | 回應 DTO（含 storeName、linkUrl） |
| `condition/BannerCondition.java` | 管理員列表篩選條件 |

---

## 10. 問題排解

| 症狀 | 可能原因 | 解決方法 |
|---------|-------------|-----|
| `GET /api/banners` 即使有已發布廣告也回傳 `[]` | 店家為 `INACTIVE` 狀態 | 啟用店家：`UPDATE store SET status = 'ACTIVE' WHERE id = '...'` |
| `POST /admin/banners` 回傳 400 `店家不存在或已刪除` | `storeId` 錯誤或店家已被刪除 | 透過 `GET /admin/stores/{storeId}` 確認 storeId |
| `startTime` 已過但廣告仍未出現 | 排程器可能尚未執行 | 等待最多 60 秒，或查看排程器日誌 |
| 輪播中圖片無法載入 | S3 URL 無效或 bucket 權限問題 | 透過 `/admin/upload` 重新上傳，並以 PUT 更新 `imageUrl` |
| 管理後台端點回傳 `403 Forbidden` | JWT token 缺少 `ROLE_ADMIN` | 使用管理員帳號重新登入 |
