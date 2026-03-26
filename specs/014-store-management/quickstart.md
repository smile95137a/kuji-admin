# 快速開始：店家管理 (Store Management)

**Feature**: `014-store-management`  
**Branch**: `014-store-management`  
**Date**: 2026-03-22  

---

## 前置條件

- Java 21 + Maven 3.9+
- MySQL 8.3 在本地端執行中（或在 `application-dev.yml` 設定 RDS 端點）
- 已設定 AWS 憑證（用於 S3 圖片上傳，開發環境可省略）
- 現有專案可正常建構（`mvn clean package -DskipTests`）

---

## 1. 資料庫設定

`store` 資料表已存在，無需執行 DDL 遷移。

確認資料表包含所有必要欄位：

```sql
DESCRIBE store;
```

預期欄位：`id`、`owner_id`、`store_name`、`short_description`、`long_description`、`logo_url`、`cover_image_url`、`email`、`phone`、`address`、`business_hours`、`facebook_url`、`instagram_url`、`line_id`、`status`、`remark`、`created_by`、`created_at`、`updated_by`、`updated_at`。

若有欄位缺失，請執行以下遷移：

```sql
-- Add missing columns if upgrading from older schema
ALTER TABLE store
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(36) NULL AFTER status,
  ADD COLUMN IF NOT EXISTS line_id VARCHAR(100) NULL AFTER instagram_url;

-- Ensure index on status exists
CREATE INDEX IF NOT EXISTS idx_store_status ON store (status);
CREATE INDEX IF NOT EXISTS idx_store_owner_id ON store (owner_id);
```

---

## 2. 建構與執行

```bash
cd C:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin

# Build (skip tests for quick start)
mvn clean package -DskipTests

# Run
java -jar target/admin-*.jar --spring.profiles.active=dev
```

或在 IntelliJ 中：以 `dev` profile 執行 `AdminApplication`。

---

## 3. 手動 API 測試速查

### 3.1 以管理員身份登入（取得 JWT）
```bash
curl -s -X POST http://localhost:8080/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}' \
  | jq -r '.data.token'
```
儲存 Token：`ADMIN_TOKEN=<貼上 Token>`

---

### 3.2 建立店家 + 負責人帳號（原子性）
```bash
curl -s -X POST http://localhost:8080/admin/stores \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "storeName": "測試店家",
    "shortDescription": "這是測試店家",
    "owner": {
      "username": "test_owner_001",
      "password": "Test@12345",
      "displayName": "測試店長"
    }
  }' | jq .
```

**預期結果**：HTTP 201，回應包含 `id` 與 `ownerId`。  
**驗證**：確認 `store` 與 `admin_user` 資料表均有新增的資料列。

---

### 3.3 列出所有店家（管理員）
```bash
curl -s -X POST http://localhost:8080/admin/stores/list \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}' | jq .
```

---

### 3.4 更新店家資訊
```bash
STORE_ID=<3.2 步驟取得的 id>

curl -s -X PUT http://localhost:8080/admin/stores/$STORE_ID \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "shortDescription": "更新後的簡短描述",
    "businessHours": "週一至週日 09:00–21:00"
  }' | jq .
```

---

### 3.5 停用店家（含進行中抽獎活動警告）
```bash
# 步驟 1：嘗試停用（若有進行中抽獎活動，預期返回 409）
curl -s -X PUT http://localhost:8080/admin/stores/$STORE_ID/status \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"DISABLED"}' | jq .

# 步驟 2：強制停用（略過警告）
curl -s -X PUT "http://localhost:8080/admin/stores/$STORE_ID/status?force=true" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"DISABLED"}' | jq .
```

**驗證連鎖效應**：查詢 DB 執行 `SELECT status FROM lottery WHERE store_id = '<id>'`，應全為 `OFF_SHELF`。

---

### 3.6 重新啟用店家
```bash
curl -s -X PUT http://localhost:8080/admin/stores/$STORE_ID/status \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"ENABLED"}' | jq .
```

**驗證**：商品／橫幅維持先前狀態（FR-005）。

---

### 3.7 公開店家列表（無需認證）
```bash
curl -s http://localhost:8080/api/stores | jq .
```
**預期結果**：僅返回 ENABLED 的店家，包含 `id`、`storeName`、`shortDescription`、`logoUrl`。

---

### 3.8 公開店家詳情（無需認證）
```bash
curl -s http://localhost:8080/api/stores/$STORE_ID | jq .
```
**預期結果**：完整店家資訊 + 上架商品列表。  
**停用狀態下**：返回 HTTP 404（與不存在的店家相同）。

---

### 3.9 店家負責人登入 + 編輯自己的店家
```bash
# 以 3.2 步驟建立的店家負責人身份登入
OWNER_TOKEN=$(curl -s -X POST http://localhost:8080/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test_owner_001","password":"Test@12345"}' \
  | jq -r '.data.token')

# 編輯自己的店家（允許）
curl -s -X PUT http://localhost:8080/admin/stores/$STORE_ID \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"shortDescription":"店長自己更新的描述"}' | jq .

# 編輯其他店家（應返回 403）
curl -s -X PUT http://localhost:8080/admin/stores/OTHER-STORE-ID \
  -H "Authorization: Bearer $OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"storeName":"嘗試越權"}' | jq .
```

---

## 4. 執行測試

```bash
# Unit tests for StoreService
mvn test -Dtest=StoreServiceTest

# All tests
mvn test
```

---

## 5. 關鍵檔案參考

| 用途 | 檔案 |
|---------|------|
| 後台 Controller | `src/main/java/.../controller/admin/AdminStoreController.java` |
| 前台 Controller | `src/main/java/.../controller/api/StoreController.java` |
| Service 介面 | `src/main/java/.../service/StoreService.java` |
| Service 實作 | `src/main/java/.../service/impl/StoreServiceImpl.java` |
| MyBatis Mapper | `src/main/java/.../mapper/StoreMapper.java` |
| Mapper XML | `src/main/resources/mapper/StoreMapper.xml` |
| 建立請求 DTO | `src/main/java/.../req/store/CreateStoreReq.java` |
| 更新請求 DTO | `src/main/java/.../req/store/UpdateStoreReq.java` |
| 狀態請求 DTO | `src/main/java/.../req/store/UpdateStoreStatusReq.java` |
| 列表項目回應 | `src/main/java/.../res/store/StoreListItemRes.java` |
| 詳情回應 | `src/main/java/.../res/store/StoreDetailRes.java` |
| 單元測試 | `src/test/java/.../service/StoreServiceTest.java` |

---

## 6. 安全性設定

以下路由已在 `SecurityConfig.java` 中完成設定：

| 路徑 | 需要認證 | 角色 |
|------|--------------|-------|
| `POST /admin/stores` | ✅ | `ADMIN` |
| `POST /admin/stores/list` | ✅ | `ADMIN`, `STORE_OWNER`, `STORE_EDITOR` |
| `PUT /admin/stores/{id}` | ✅ | `ADMIN`, `STORE_OWNER` |
| `PUT /admin/stores/{id}/status` | ✅ | `ADMIN` |
| `GET /api/stores` | ❌ 公開 | — |
| `GET /api/stores/{id}` | ❌ 公開 | — |

若公開 `/api/stores` 路由尚未加入 `permitAll()` 清單，請在 `SecurityConfig` 中新增：
```java
.requestMatchers("/api/stores", "/api/stores/**").permitAll()
```

---

## 7. 常見問題

| 症狀 | 解決方式 |
|---------|-----|
| 建立店家時返回 500 | 確認是否違反 `admin_user.username` 唯一性約束 — 捕獲 `DataIntegrityViolationException` |
| 連鎖停用無效 | 確認 `LotteryMapper.updateByExampleSelective` 已存在於 XML |
| 公開端點返回 401 | 在 `SecurityConfig` 的 `permitAll()` 中加入 `/api/stores/**` |
| 店家負責人可編輯任意店家 | 確認 `StoreServiceImpl.updateStore()` 中的所有權檢查未被繞過 |
| 已停用店家出現在公開 API | 確認 `listEnabledStores()` 查詢中有 `WHERE status = 'ENABLED'` |
