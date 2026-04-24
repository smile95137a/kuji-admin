---
description: "資料庫與 MBG 工作規則。規範 DDL-first、generatorConfig.xml 更新、run-mbg.ps1 使用方式，以及手寫 SQL 與 MBG 生成區隔。"
applyTo: ["sql/**", "*.sql", "generatorConfig.xml", "run-mbg.ps1", ".github/skills/mbg-workflow/**", "src/main/java/**/entity/**", "src/main/java/**/example/**", "src/main/java/**/mapper/**", "src/main/resources/mapper/**"]
---

# DB 與 MBG 規則

## 必須

- 任何資料表或欄位異動都採 **DDL-first**
- 新 DDL 不可只放進 `sql/`；必須真的套用到資料庫
- schema 異動後，必須同步更新根目錄 `generatorConfig.xml`
- 生成流程固定為：`DDL → 套用 DB → 更新 generatorConfig.xml → .\run-mbg.ps1 → mvn clean package -DskipTests`
- 產生的 Entity / Mapper / Example 以 MBG 為準，不手動憑空建立

## 建議

- 優先使用 `.\run-mbg.ps1` 作為 MBG 入口，而不是直接裸跑 Maven 指令
- 每次 MBG 後都檢查 Mapper XML 差異，特別是自訂 SQL 是否受影響
- 單表條件查詢優先使用 Example；只有 JOIN、聚合、子查詢才手寫 SQL

## 禁止

- ❌ 不要只新增 `.sql` 檔卻不執行到 DB
- ❌ 不要在沒有更新 `generatorConfig.xml` 的情況下期待 MBG 產生新檔
- ❌ 不要把手寫 SQL 直接混進 MBG 管理區
- ❌ 不要手動維護與資料表不一致的 Entity、Mapper、Example
