
<!--
kuji-admin 專案的 Copilot 指南（中文）
以下內容為 AI 編碼代理人要快速上手時可參考的具體規則與範例。
-->

# kuji-admin Copilot 指南

摘要
- Spring Boot 3.3（Java 21）。啟動類：`com.group.admin.AdminApplication`。
- 使用 MyBatis（映射檔位於 `src/main/resources/mapper`），實體類在 `entity/`，Mapper 介面在 `mapper/`。
- JWT 實作在 `com.group.admin.util.JwtUtil`（配置 key 使用 `jwt.secret`）。

快速操作（命令）
```bash
# 建構與打包（不跑測試）：
mvn -DskipTests package

# 開發模式執行（dev profile）：
mvn -Pdev spring-boot:run

# 或用 JAR 執行（指定 profile）：
java -Dspring.profiles.active=dev -jar target/admin-1.0.0.jar
```

架構重點與範式
- 根套件 `com.group.admin`（`AdminApplication` 設定 `scanBasePackages` 為該路徑）。
- Controller 的回傳型態可為物件或 `ResponseEntity<T>`，套件中的 `aop/GlobalResponseAspect` 會統一包成 `ApiResponse` 並記錄執行時間，新增或修改 Controller 時請注意保持與此行為相容。
- 全域例外處理位於 `handler/GlobalExceptionHandler`，偏好在 Service/Controller 中以拋出例外讓該處理器統一格式化回應。
- JWT 與安全：JWT 工具類在 `util/JwtUtil.java`。`config/SecurityConfig` 與 `filter/` 裡的 Filter 多數為註解/未啟用狀態，若啟用安全機制請參考 `JwtAuthenticationFilterSkipApi` 的行為（避免把 JWT 攔截套到 `/api/**` 的全放行路徑）。
- CORS：`config/CorsConfig` 會依 `spring.profiles.active` 決定 dev/prod 的允許來源。
- MyBatis generator：在 `src/main/resources/mapper/generatorConfig.xml`，`pom.xml` 已註冊 MyBatis generator plugin，可直接使用。

專案慣例與範例
- Controller 範例：`controller/UserController.java` 用 `ResponseEntity<String>` 返回，AOP 會把它轉成 `ApiResponse`。
- JWT 範例：用 `JwtUtil.generateToken(username)` 產生 token，`JwtUtil.validateToken(token)` 驗證；實際攔截器示例在 `filter/`（目前多為註解示範）。
- Profiles：`application.yml` 預設 profile 為 `dev`。
- Logging：`application.yml` 內將 `com.group` 設為 DEBUG，請在相同或相近層級使用日誌等級設定以助追蹤。

建構/測試/除錯提示
- 執行測試：`mvn test`（使用 `spring-boot-starter-test`）。
- 在 IDE 中除錯：直接運行 `AdminApplication`，並加上 VM 參數 `-Dspring.profiles.active=dev`，以對應 `application.yml` 的預設。
- 開發熱重載：`spring-boot-devtools` 已加入（runtime），在 IDE 設定自動建置可達到熱重載效果。

修改時應檢查的檔案
- `src/main/java/com/group/admin/aop/GlobalResponseAspect.java`：回應封裝與日誌行為。
- `src/main/java/com/group/admin/result/ApiResponse.java`：API 回應包裝類（被 AOP 使用）。
- `src/main/java/com/group/admin/handler/GlobalExceptionHandler.java`：全域例外與錯誤回傳格式化器。
- `src/main/java/com/group/admin/util/JwtUtil.java`：產生/驗證 token 的實作。
- `src/main/java/com/group/admin/config/SecurityConfig.java`：安全設定（目前多為註解示例，啟用前應檢查）。
- `src/main/java/com/group/admin/filter/*.java`：可能包含 `JwtAuthenticationFilter`、`JwtAuthenticationFilterSkipApi` 等攔截器。
- `src/main/resources/mapper/*.xml`：MyBatis SQL 與 `resultMap`，要與 `entity/` 結構對齊。
- `pom.xml`：建構、MyBatis generator、Profile 與 Java 版號配置。

不得隨意更動（風險區）
- 不要在不確認 `GlobalResponseAspect` 的情況下更改 Controller 的回傳型別或契約。
- 避免修改 MyBatis 的 namespace、resultMap 名稱或 column 映射而不同步更新對應的 `mapper` 介面與 `entity` 欄位。

需要更多內容時
- 可打開 `src/main/java/com/group/admin` 與 `src/main/resources/mapper` 查看具體範例；提交修改時請同時附上 1-2 個要修改的檔案與預期行為，並建議執行 `mvn -DskipTests package` 驗證編譯通過。

意見回饋
- 若此檔案有缺漏或想新增具體範例（例如 mapper XML 範本、ApiResponse 格式、啟用認證步驟），請告訴我我會補充。

