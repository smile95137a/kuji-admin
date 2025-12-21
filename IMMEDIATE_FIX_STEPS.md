# 🔧 立即修復步驟

## ⚠️ 問題：MyBatis Mapper XML 重複載入

錯誤：`Result Maps collection already contains key`

---

## ✅ 已完成的修復

1. ✅ **註解掉 pom.xml 中的 DevTools 依賴**
2. ✅ **創建 clean-and-prepare.bat 快速腳本**
3. ✅ **創建完整修復文檔 MYBATIS_DUPLICATE_FIX.md**

---

## 🚀 現在請執行以下步驟

### 方法 1：使用快速腳本（推薦）

1. **雙擊執行** `clean-and-prepare.bat`
2. 按照提示操作
3. 編譯成功後從 IDE 啟動 AdminApplication

### 方法 2：手動執行

#### Step 1: 確認 DevTools 已註解

打開 `pom.xml`，確認第 91-98 行已被註解：

```xml
<!-- DevTools（只在開發用）- 暫時停用以避免 MyBatis Mapper XML 重複載入問題 -->
<!--
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
-->
```

#### Step 2: 停止所有正在運行的 AdminApplication

在 IDE 或任務管理器中停止所有實例。

#### Step 3: 清理並重新編譯

```bash
mvn clean compile -DskipTests
```

等待編譯完成（應該顯示 BUILD SUCCESS）。

#### Step 4: 從 IDE 啟動

1. 開啟 `src/main/java/com/group/admin/AdminApplication.java`
2. 點擊 Run 按鈕（或按 F5）
3. 觀察控制台日誌

---

## ✅ 預期結果

### 正確的啟動日誌應該是：

```log
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

2025-12-21T23:50:00.000+08:00  INFO --- [main] c.g.admin.AdminApplication : Starting AdminApplication
2025-12-21T23:50:01.000+08:00  INFO --- [main] c.g.admin.AdminApplication : The following 1 profile is active: "dev"

... (MyBatis 初始化) ...

2025-12-21T23:50:03.000+08:00  INFO --- [main] c.g.a.config.DataInitializer : ========================================
2025-12-21T23:50:03.000+08:00  INFO --- [main] c.g.a.config.DataInitializer : 開始執行系統資料初始化...
2025-12-21T23:50:03.100+08:00  INFO --- [main] c.g.a.config.DataInitializer : ✓ 角色資料初始化完成（3 筆）
2025-12-21T23:50:03.200+08:00  INFO --- [main] c.g.a.config.DataInitializer : ✓ 選單資料初始化完成（19 筆）
2025-12-21T23:50:03.300+08:00  INFO --- [main] c.g.a.config.DataInitializer : ✓ 管理員資料初始化完成（4 筆）
2025-12-21T23:50:03.400+08:00  INFO --- [main] c.g.a.config.DataInitializer : ✓ 商家資料初始化完成（2 筆）
2025-12-21T23:50:03.500+08:00  INFO --- [main] c.g.a.config.DataInitializer : ✓ 測試用戶資料初始化完成（3 筆）
2025-12-21T23:50:03.600+08:00  INFO --- [main] c.g.a.config.DataInitializer : ✓ 抽獎活動資料初始化完成（2 筆）
2025-12-21T23:50:03.700+08:00  INFO --- [main] c.g.a.config.DataInitializer : ========================================
2025-12-21T23:50:03.700+08:00  INFO --- [main] c.g.a.config.DataInitializer : 系統資料初始化完成！

2025-12-21T23:50:05.000+08:00  INFO --- [main] o.s.b.w.e.tomcat.TomcatWebServer : Tomcat started on port 8080 (http)
2025-12-21T23:50:05.100+08:00  INFO --- [main] c.g.admin.AdminApplication : Started AdminApplication in 5.123 seconds (process running for 5.456)
```

### 關鍵成功指標：

- ✅ 沒有 `Result Maps collection already contains key` 錯誤
- ✅ 看到 `系統資料初始化完成！`
- ✅ 看到 `Tomcat started on port 8080`
- ✅ 看到 `Started AdminApplication in X seconds`

---

## 🧪 啟動後立即測試

開啟新的終端視窗，執行：

```bash
curl http://localhost:8080/api/test/health
```

**預期回應：**
```json
{
  "status": "UP",
  "timestamp": "2025-12-21T23:50:00",
  "message": "KUJI Admin System is running!"
}
```

---

## 🐛 如果還是失敗

### 檢查清單：

1. **pom.xml 中的 DevTools 確實被註解了嗎？**
   - 檢查第 91-98 行

2. **是否執行了 mvn clean compile？**
   - 必須清理舊的編譯文件

3. **是否有多個 AdminApplication 在背景執行？**
   - 使用任務管理器檢查並關閉所有 Java 進程

4. **target 目錄是否被清空了？**
   - 手動刪除 `target` 目錄後重新編譯

5. **IDE 是否使用了錯誤的配置？**
   - 確認 Run Configuration 沒有啟用 DevTools 相關設定

---

## 📚 參考文檔

- `MYBATIS_DUPLICATE_FIX.md` - 完整的問題分析與解決方案
- `STARTUP_CHECKLIST.md` - 啟動驗證清單
- `API_TEST_GUIDE.md` - API 測試指南

---

## 💡 為什麼這樣修復有效？

DevTools 使用雙 ClassLoader 機制來實現熱重載：
- Base ClassLoader（載入第三方庫）
- Restart ClassLoader（載入應用程式碼）

當 DevTools 觸發重啟時，Restart ClassLoader 重新載入專案程式碼，但 MyBatis 的 Configuration 實例仍然存在於 Base ClassLoader 中，保留了舊的 ResultMap 註冊資訊。

當新的 ClassLoader 嘗試再次註冊相同的 ResultMap ID 時，就會拋出 `already contains key` 錯誤。

**移除 DevTools 後**，沒有重啟機制，MyBatis 只初始化一次，不會有重複註冊問題。

---

## 🎯 下一步

啟動成功後：

1. ✅ 執行 `test-api.bat` 測試所有端點
2. ✅ 使用 Postman 匯入測試集合
3. ✅ 查看 Swagger UI：http://localhost:8080/api/swagger-ui/index.html
4. ✅ 開始開發業務邏輯

---

祝您順利啟動！如果還有問題，請提供完整的錯誤日誌。🚀
