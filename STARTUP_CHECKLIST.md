# ✅ AdminApplication 啟動成功檢查清單

## 🚀 啟動前準備

- [ ] MySQL 服務已啟動
- [ ] 資料庫 `kuji` 已建立
- [ ] `application-dev.yml` 中的資料庫連接資訊正確
- [ ] Maven 依賴已下載完成
- [ ] Java 21 已正確安裝

---

## 🎯 從 IDE 啟動步驟

### 1. 開啟 AdminApplication.java

```
src/main/java/com/group/admin/AdminApplication.java
```

### 2. 點擊 Run 按鈕

或使用快捷鍵：
- **VS Code**: `F5` 或 `Ctrl+F5`
- **IntelliJ IDEA**: `Shift+F10`
- **Eclipse**: `Ctrl+F11`

### 3. 觀察啟動日誌

應該看到以下關鍵訊息：

```log
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.3.3)

2025-12-21T23:30:00.000+08:00  INFO 12345 --- [admin] [           main] c.g.admin.AdminApplication               : Starting AdminApplication
2025-12-21T23:30:01.000+08:00  INFO 12345 --- [admin] [           main] c.g.admin.AdminApplication               : The following 1 profile is active: "dev"
...
2025-12-21T23:30:05.000+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : ========================================
2025-12-21T23:30:05.000+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : 開始執行系統資料初始化...
2025-12-21T23:30:05.000+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : ========================================
2025-12-21T23:30:05.100+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : 初始化角色資料...
2025-12-21T23:30:05.200+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : ✓ 角色資料初始化完成（3 筆）
2025-12-21T23:30:05.300+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : 初始化選單資料...
2025-12-21T23:30:05.400+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : ✓ 選單資料初始化完成（19 筆）
...
2025-12-21T23:30:06.000+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : ========================================
2025-12-21T23:30:06.000+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : 系統資料初始化完成！
2025-12-21T23:30:06.000+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : ========================================
2025-12-21T23:30:06.100+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : 
2025-12-21T23:30:06.100+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : ========================================
2025-12-21T23:30:06.100+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : 預設測試帳號資訊
2025-12-21T23:30:06.100+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : ========================================
...
2025-12-21T23:30:07.000+08:00  INFO 12345 --- [admin] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http)
2025-12-21T23:30:07.100+08:00  INFO 12345 --- [admin] [           main] c.g.admin.AdminApplication               : Started AdminApplication in 7.123 seconds
```

---

## ✅ 啟動成功驗證

### 1. 檢查日誌

- [ ] 沒有 ERROR 級別的日誌
- [ ] 看到 "Started AdminApplication in X seconds"
- [ ] 看到 "系統資料初始化完成！"
- [ ] 看到 "Tomcat started on port 8080"

### 2. 測試基本端點

開啟新的終端視窗，執行：

```bash
curl http://localhost:8080/api/test/health
```

**預期回應:**
```json
{
  "status": "UP",
  "timestamp": "2025-12-21T23:30:00",
  "message": "KUJI Admin System is running!"
}
```

- [ ] 返回正確的 JSON
- [ ] status 為 "UP"

### 3. 測試資料庫

```bash
curl http://localhost:8080/api/test/db-check
```

**預期回應:**
```json
{
  "status": "SUCCESS",
  "database": "Connected",
  "statistics": {
    "roles": 3,
    "menus": 19,
    "adminUsers": 4,
    "stores": 2,
    "users": 3,
    "lotteries": 2
  }
}
```

- [ ] status 為 "SUCCESS"
- [ ] 所有統計數字正確

### 4. 測試登入

```bash
curl -X POST http://localhost:8080/api/test/admin-login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"admin123"}'
```

**預期回應:**
```json
{
  "status": "SUCCESS",
  "message": "登入成功",
  "data": {
    "accessToken": "eyJhbGc...",
    ...
  }
}
```

- [ ] status 為 "SUCCESS"
- [ ] 返回 accessToken

---

## 🎉 完整功能測試

### 使用測試腳本（推薦）

雙擊執行 `test-api.bat` 腳本，會自動測試所有端點。

### 使用 Postman

1. 匯入 `KUJI_Admin_API_Tests.postman_collection.json`
2. 執行 Collection Runner
3. 查看所有測試結果

### 手動測試

參考 `API_TEST_GUIDE.md` 逐一測試每個端點。

---

## 🐛 常見問題

### 問題 1: 埠口 8080 已被占用

**錯誤訊息:**
```
Web server failed to start. Port 8080 was already in use.
```

**解決方案:**

方法 1: 修改埠口（在 `application.yml`）
```yaml
server:
  port: 8081
```

方法 2: 停止占用 8080 的程序
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Mac/Linux
lsof -i :8080
kill -9 <PID>
```

### 問題 2: 資料庫連接失敗

**錯誤訊息:**
```
Unable to acquire JDBC Connection
```

**解決方案:**
1. 確認 MySQL 服務已啟動
2. 檢查 `application-dev.yml` 中的連接資訊
3. 確認資料庫 `kuji` 已建立
4. 測試手動連接：
```bash
mysql -u root -p
USE kuji;
SHOW TABLES;
```

### 問題 3: MyBatis Mapper XML 重複載入

**錯誤訊息:**
```
Result Maps collection already contains key ...
```

**解決方案:**
已在 `application.yml` 中關閉 DevTools restart 功能。如果仍有問題：

1. 停止所有正在運行的 AdminApplication 實例
2. 執行 `mvn clean compile`
3. 重新啟動

### 問題 4: 編譯錯誤

**解決方案:**
```bash
mvn clean compile -DskipTests
```

如果仍有錯誤，請檢查：
- Java 版本是否為 21
- Maven 依賴是否完整下載
- IDE 是否正確配置 Java 21

---

## 📊 啟動成功標準

✅ **完全成功** - 所有檢查項目都通過

- AdminApplication 順利啟動
- 日誌中沒有 ERROR
- DataInitializer 成功執行
- 所有測試端點返回正確資料
- 能夠成功登入

🎯 **可以開始開發了！**

---

## 📝 後續步驟

1. ✅ 專案啟動成功
2. ✅ 初始資料已載入
3. ✅ API 測試通過
4. 🚀 開始開發業務邏輯
5. 🧪 編寫單元測試
6. 📦 部署到測試環境

---

## 💡 提示

- 如果資料已初始化，再次啟動時 DataInitializer 會跳過
- 可以隨時使用 `/test/db-check` 查看資料狀態
- Swagger UI 提供完整的 API 文檔：`http://localhost:8080/api/swagger-ui/index.html`
- 所有測試帳號密碼都在 DataInitializer 中定義

---

祝您開發順利！🎉
