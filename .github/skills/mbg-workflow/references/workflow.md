# MBG 工作流程補充說明

## 專案實際路徑

- 生成設定檔：專案根目錄 `generatorConfig.xml`
- Skill 內腳本：`.github/skills/mbg-workflow/scripts/run-mbg.ps1`
- 相容入口：專案根目錄 `run-mbg.ps1`

## 建議執行順序

1. 先把 DDL 套用到實際 MySQL 資料庫
2. 更新根目錄 `generatorConfig.xml`
3. 在專案根目錄執行 `.\run-mbg.ps1`
4. 執行 `mvn clean package -DskipTests`

## 為什麼需要這個腳本

- 先備份並刪除 MBG 管理的 Mapper XML，再重新生成
- 降低 `BaseResultMap` 重複定義與 XML 重複節點錯誤
- 若生成失敗，會自動還原備份

## 重要風險

此專案目前仍有部分 Mapper XML 同時混入手寫 SQL 與 MBG 生成內容。重新生成時，這些手寫 SQL 可能被覆蓋或移除，因此每次執行完 MBG 都必須人工檢查差異。
