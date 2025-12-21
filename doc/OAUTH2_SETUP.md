# OAuth2 配置說明

## 概述

專案已整合 Spring Security OAuth2 Client，支援 Google OAuth2 登入（未來可擴展至 Facebook、LINE 等）。

## 目前狀態

### 開發環境（dev）
- 使用假的 client-id 和 client-secret（`dummy-client-id` 和 `dummy-client-secret`）
- OAuth2 功能**已啟用但不可用**，需要替換為真實憑證才能使用

### 正式環境（prod）
- 必須透過環境變數提供真實的 OAuth2 憑證
- 環境變數：`GOOGLE_CLIENT_ID` 和 `GOOGLE_CLIENT_SECRET`

## 如何取得 Google OAuth2 憑證

### 步驟 1：建立 Google Cloud 專案
1. 前往 [Google Cloud Console](https://console.cloud.google.com/)
2. 建立新專案或選擇現有專案
3. 啟用 "Google+ API" 或 "Google People API"

### 步驟 2：建立 OAuth 2.0 憑證
1. 導航至「API 和服務」→「憑證」
2. 點擊「建立憑證」→「OAuth 用戶端 ID」
3. 選擇應用程式類型：**Web 應用程式**
4. 設定授權的重新導向 URI：
   - 開發環境：`http://localhost:8080/api/login/oauth2/code/google`
   - 正式環境：`https://your-domain.com/api/login/oauth2/code/google`

### 步驟 3：更新配置

#### 開發環境
編輯 `src/main/resources/application-dev.yml`：

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_ACTUAL_CLIENT_ID
            client-secret: YOUR_ACTUAL_CLIENT_SECRET
```

#### 正式環境
設定環境變數：

```bash
export GOOGLE_CLIENT_ID=your_actual_client_id
export GOOGLE_CLIENT_SECRET=your_actual_client_secret
```

或在啟動時指定：

```bash
java -jar admin-1.0.0.jar \
  -DGOOGLE_CLIENT_ID=your_actual_client_id \
  -DGOOGLE_CLIENT_SECRET=your_actual_client_secret
```

## OAuth2 登入流程

### 前端發起登入
```
GET /oauth2/authorization/google
```

### 成功回調
使用者授權後，會被重新導向至：
```
GET /api/auth/oauth2/success
```

在 `OAuth2Controller.oauth2LoginSuccess()` 中：
1. 取得使用者的 email、name 等資訊
2. 檢查資料庫中是否已有該使用者
3. 若無則建立新使用者
4. 產生 JWT token
5. 回傳 token 給前端

### 失敗回調
```
GET /api/auth/oauth2/failure
```

## TODO 清單

- [ ] 取得真實的 Google OAuth2 憑證
- [ ] 實作 `OAuth2Controller.oauth2LoginSuccess()` 中的 JWT 產生邏輯
- [ ] 實作使用者自動註冊邏輯（首次 OAuth2 登入時）
- [ ] 新增 OAuth2 使用者與一般使用者的關聯邏輯
- [ ] （選用）支援其他 OAuth2 提供者（Facebook、LINE）

## 安全性注意事項

1. **絕對不要**將真實的 client-id 和 client-secret 提交到版本控制
2. 正式環境必須使用環境變數
3. 定期輪換 client-secret
4. 限制授權的重新導向 URI 為可信任的網域

## 相關檔案

- 配置類：`src/main/java/com/group/admin/config/SecurityConfig.java`
- OAuth2 處理器：`src/main/java/com/group/admin/controller/OAuth2Controller.java`
- JWT 工具：`src/main/java/com/group/admin/util/JwtUtil.java`
- 配置屬性：`src/main/resources/application-dev.yml`、`application-prod.yml`
