# Quickstart: 店家帳號管理 (Store Account Management)

**Feature**: `013-store-account-mgmt`  
**Branch**: `013-store-account-mgmt`  
**Date**: 2026-03-22

---

## 前置需求

- Java 21, Maven 3.9+
- MySQL 8.3 執行中（現有結構，無需資料庫遷移）
- Redis 7.x 本地執行中（本功能新增需求）
- `application.yml` 中已設定 SMTP 憑證（現有 `EmailService` 設定）
- IntelliJ / VS Code 搭配 Spring Boot 執行設定

---

## 1. 新增 Redis 依賴

在 `pom.xml` 中加入：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

---

## 2. 設定 Redis

在 `src/main/resources/application.yml` 中加入：

```yaml
spring:
  data:
    redis:
      host: localhost      # or your Redis host
      port: 6379
      timeout: 2000ms      # fail fast; see graceful degradation note
      lettuce:
        pool:
          max-active: 8
          max-idle: 4
```

正式環境（AWS EC2）請將 `localhost` 替換為您的 ElastiCache 端點。

---

## 3. 本地啟動 Redis

```bash
# Option A: Docker
docker run -d --name redis -p 6379:6379 redis:7-alpine

# Option B: Windows (if Redis for Windows installed)
redis-server
```

驗證：`redis-cli ping` → 應回傳 `PONG`。

---

## 4. 建置與執行

```bash
cd C:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn clean package -DskipTests
java -jar target/*.jar
```

或直接透過 Maven 執行：

```bash
mvn spring-boot:run
```

---

## 5. 快速冒煙測試 — 建立 StoreOwner 帳號

**步驟 1**：以平台管理員登入取得 JWT Token：

```http
POST /admin/auth/login
Content-Type: application/json

{
  "username": "admin@example.com",
  "password": "your-admin-password"
}
```

從回應中複製 `accessToken`。

**步驟 2**：建立 StoreOwner 帳號：

```http
POST /admin/accounts
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "email": "owner@newstore.com",
  "displayName": "New Store Owner",
  "phone": "0912345678",
  "roleType": "STORE_OWNER",
  "storeId": "<existing-store-id>"
}
```

預期回應（`201 Created`）：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "uuid-of-new-account",
    "email": "owner@newstore.com",
    "displayName": "New Store Owner",
    "status": "PENDING",
    "forceChangePassword": true,
    "roleType": "STORE_OWNER",
    "storeId": "<existing-store-id>",
    "createdAt": "2026-03-22T10:00:00"
  }
}
```

初始密碼電子郵件非同步寄送至 `owner@newstore.com`。

---

## 6. 快速冒煙測試 — 首次登入與密碼變更

**步驟 1**：以初始密碼（來自電子郵件）登入：

```http
POST /admin/auth/login
Content-Type: application/json

{
  "username": "owner@newstore.com",
  "password": "<initial-password-from-email>"
}
```

回應包含 `forceChangePassword: true` — 客戶端必須重新導向至密碼變更頁面。

**步驟 2**：首次登入時變更密碼：

```http
POST /admin/auth/first-login/change-password
Authorization: Bearer <token-from-step-1>
Content-Type: application/json

{
  "oldPassword": "<initial-password-from-email>",
  "newPassword": "MyNew$ecure123"
}
```

成功後：`status` 變更為 `ACTIVE`，`forceChangePassword` 變為 `false`，回傳新 Token。

---

## 7. 快速冒煙測試 — 停用帳號（Token 失效）

```http
PUT /admin/accounts/<account-id>/status
Authorization: Bearer <admin-accessToken>
Content-Type: application/json

{
  "status": "INACTIVE"
}
```

立即使用**已停用使用者的舊 Token** 將回傳 `401 Unauthorized`：

```http
GET /admin/accounts
Authorization: Bearer <disabled-user-old-token>
```

預期：`401` — Token 已透過 Redis 世代計數器失效。

---

## 8. 列出帳號

```http
GET /admin/accounts?roleType=STORE_OWNER&status=ACTIVE&page=0&size=20
Authorization: Bearer <admin-accessToken>
```

---

## 9. 執行測試

```bash
# Unit + integration tests
mvn test

# Run only account management tests (once implemented)
mvn test -Dtest="AdminAccountServiceTest,AdminAccountControllerTest"
```

---

## 關鍵設定參考

| 設定 Key | 預設值 | 備註 |
|------------|---------|-------|
| `spring.data.redis.host` | `localhost` | 正式環境請更改 |
| `spring.data.redis.port` | `6379` | 標準 Redis 連接埠 |
| `spring.data.redis.timeout` | `2000ms` | 快速失敗以確保安全性 |
| `jwt.expiration` | `86400000`（24h） | 現有設定 |
| `jwt.refresh-expiration` | `2592000000`（30d） | 現有設定 |

---

## 疑難排解

| 現象 | 可能原因 | 解決方式 |
|---------|-------------|-----|
| 帳號重新啟用後有效 Token 回傳 `401` | Redis 計數器已遞增；Token 世代過舊 | 重新登入取得新 Token |
| 未收到電子郵件 | SMTP 設定錯誤或非同步失敗 | 查看 `email_log` 表的失敗記錄 |
| 建立帳號時回傳 `500` | Redis 無法連線 | 啟動 Redis；檢查 `spring.data.redis.host` |
| 建立帳號時回傳 `409 Conflict` | 電子郵件已被註冊 | 使用不同的電子郵件 |
| 店家已有擁有者 | 嘗試建立第二位 StoreOwner | 使用 STORE_EDITOR 角色或重新指派擁有權 |
