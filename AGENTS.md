
# 倉庫開發規範

> ⚠️ 本文件自此僅允許以「繁體中文」維護與撰寫，嚴禁英文主導。

## 專案結構與模組組織

本倉庫為 KUJI 管理後台/API 服務的 Spring Boot 3 專案。主要 Java 程式碼位於 `src/main/java/com/group/admin`，依職責分為：`controller`、`service`、`repository`、`mapper`、`entity`、`req`、`res`、`dto`、`config`、`security`、`util`。執行時資源於 `src/main/resources`；MyBatis XML 於 `src/main/resources/mapper`，郵件模板於 `templates`，靜態上傳資產於 `static`，環境設定於 `application*.yml`。測試程式結構與主程式鏡像，位於 `src/test/java/com/group/admin`，測試設定於 `src/test/resources/application-test.yml`。SQL 遷移與種子腳本於 `sql/`，產品/規格文件於 `docs/`、`doc/`、`frontend/`、`specs/`。`worktrees/` 僅作為 feature 分支工作區，非主程式來源。

## 建構、測試與開發指令

- `mvn clean compile`：編譯 Java 21 原始碼並複製 mapper/resource 檔案。
- `mvn test`：執行所有 `**/*Test.java` 測試。
- `mvn spring-boot:run`：以預設 dev profile 啟動本地 API（http://localhost:8080/api）。
- `mvn clean package`：打包可執行 Spring Boot JAR 於 `target/`。
- `mvn mybatis-generator:generate`：依 `generatorConfig.xml` 重新產生 MyBatis 相關檔案。
- `./run-mbg.ps1`：Windows 下 MyBatis Generator 輔助腳本。

## 程式風格與命名慣例

採用 Java 21、UTF-8、4 空白縮排，遵循現有 Spring 慣例。類別名稱用 PascalCase；方法、欄位、Bean 用 camelCase；常數用 UPPER_SNAKE_CASE。API 請求類放 `req`，回應類放 `res`，實體類放 `entity`，Mapper 介面/XML 命名需對齊（如 `UserMapper.java` 與 `UserMapper.xml`）。優先使用建構式/Service 層驗證與現有回傳/例外模式，避免 ad hoc Controller 邏輯。

## 測試規範

測試採用 JUnit 5、Spring Boot Test、Spring Security Test 與 H2。測試類命名為 `*Test.java` 以利 Surefire 掃描。Controller 測試放於 `controller/admin` 或 `controller/api`，Service 測試放於 `service`，端對端流程測試放於 `integration`。每次提交前請執行 `mvn test`，並針對業務規則、安全行為、Mapper 查詢、API 合約變更新增或更新測試。

## Commit 與 Pull Request 規範

Git 歷史請採用 Conventional Commit 標題，如 `feat(reports): ...`、`fix: ...`、`chore: ...`。每次提交請聚焦單一主題。Pull Request 需附簡要說明、關聯 issue/spec（如有）、資料庫異動說明（如有 sql/ 變更）、測試證據、截圖或 API 回應範例。

## 安全與設定建議

嚴禁提交真實憑證。請以 `.env.example` 為本地密鑰模板，正式環境以環境變數覆蓋。更動認證、上傳、郵件、金流等設定時，請同步檢查 `application-dev.yml`、`application-prod.yml` 及 JWT/payment/S3 相關設定。

<!-- SPECKIT START -->
## SpecKit 目前焦點

- 目前功能：`033-platform-revenue-report`
- 功能規格：`specs/033-platform-revenue-report/spec.md`
- 實作計畫：`specs/033-platform-revenue-report/plan.md`
<!-- SPECKIT END -->
