# 🔧 MyBatis Mapper XML 重複載入問題 - 完整解決方案

## 🐛 錯誤訊息

```
Result Maps collection already contains key com.group.admin.mapper.AdminOperationLogMapper.BaseResultMap
```

這是因為 **Spring Boot DevTools** 的重啟機制會導致 MyBatis Mapper XML 被載入兩次。

---

## ✅ 解決方案 1：完全停用 DevTools（推薦用於生產環境）

### 方法 1A：註解 pom.xml 中的 DevTools 依賴

編輯 `pom.xml`，找到以下內容並**註解掉**：

```xml
<!-- DevTools（只在開發用） -->
<!--
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
-->
```

然後執行：
```bash
mvn clean compile
```

---

## ✅ 解決方案 2：在 application.yml 中完全停用 DevTools（已配置但可能不夠）

確認 `src/main/resources/application.yml` 中有以下配置：

```yaml
spring:
  devtools:
    restart:
      enabled: false
    livereload:
      enabled: false
```

---

## ✅ 解決方案 3：排除 Mapper XML 被重複掃描（治標方案）

在 `application.yml` 或 `application-dev.yml` 中加入：

```yaml
mybatis:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    # 允許重複載入（但可能造成其他問題）
    allow-override: true
```

**注意：這不是最佳解決方案，只是臨時方案。**

---

## ✅ 解決方案 4：手動清理 target 目錄（立即生效）

有時候是因為舊的 class 檔案殘留，執行以下命令：

```bash
# 清理並重新編譯
mvn clean compile -DskipTests

# 或直接刪除 target 目錄
rmdir /s /q target
mvn compile -DskipTests
```

---

## 🎯 **強烈推薦的完整修復步驟**

### Step 1: 停止所有正在運行的 AdminApplication

確保沒有任何實例在背景執行。

### Step 2: 註解 DevTools 依賴

編輯 `pom.xml`：

```xml
<!-- DevTools（開發時如需熱重載可啟用，但目前會導致 MyBatis 問題）-->
<!--
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
-->
```

### Step 3: 清理並重新編譯

```bash
mvn clean compile -DskipTests
```

### Step 4: 啟動 AdminApplication

從 IDE 重新啟動，應該能正常啟動了。

---

## 🔍 驗證是否修復成功

啟動後應該看到以下日誌：

```log
2025-12-21T23:50:00.000+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : 開始執行系統資料初始化...
2025-12-21T23:50:01.000+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : ✓ 角色資料初始化完成（3 筆）
2025-12-21T23:50:02.000+08:00  INFO 12345 --- [admin] [           main] c.g.a.config.DataInitializer             : ✓ 選單資料初始化完成（19 筆）
...
2025-12-21T23:50:05.000+08:00  INFO 12345 --- [admin] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http)
2025-12-21T23:50:05.100+08:00  INFO 12345 --- [admin] [           main] c.g.admin.AdminApplication               : Started AdminApplication in 5.123 seconds
```

**關鍵：不會再出現 "Result Maps collection already contains key" 錯誤。**

---

## 📊 為什麼會發生這個問題？

1. **DevTools 重啟機制**: Spring Boot DevTools 使用兩個 ClassLoader：
   - Base ClassLoader（載入第三方 JAR）
   - Restart ClassLoader（載入專案程式碼）

2. **MyBatis 初始化**: MyBatis 在啟動時會掃描並註冊所有 Mapper XML。

3. **衝突**: 當 DevTools 觸發重啟時，Restart ClassLoader 重新載入，但 MyBatis 的 Configuration 仍然保留舊的 ResultMap，導致重複註冊錯誤。

---

## 🎯 最終建議

### 開發階段
- **暫時註解 DevTools**，等 MyBatis 問題解決後再啟用
- 或使用外部工具如 JRebel 進行熱重載

### 生產環境
- **必須移除或停用 DevTools**
- DevTools 應該永遠不出現在生產環境

---

## 📝 其他注意事項

### 如果註解 DevTools 後還是有問題

1. **檢查是否有多個 Mapper XML 配置**

查看 `src/main/resources/mapper/` 和 `target/classes/mapper/` 是否有重複文件。

2. **檢查 MyBatis 配置**

確保 `application.yml` 中只有一個 mapper-locations 設定：

```yaml
mybatis:
  mapper-locations: classpath:mapper/*.xml
```

3. **檢查是否有自定義 SqlSessionFactory**

如果有自定義配置，可能導致重複初始化。

---

## ✅ 快速指令

```bash
# 1. 停止所有 AdminApplication 實例
# （在 IDE 或任務管理器中手動停止）

# 2. 清理專案
mvn clean

# 3. 重新編譯
mvn compile -DskipTests

# 4. 從 IDE 啟動 AdminApplication
# 應該能正常啟動了！
```

---

祝您順利啟動！🚀
