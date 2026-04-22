# KUJI 開發合併 Workflow（個人版）

> 目標：每次功能完成後，都能穩定「可測、可啟動、可合併」。

## 0. 開始前
- 確認目前在功能分支（例如 `feature/027-oauth2-auth-system`）
- 先看變更：`git status --short`
- 確認主要需求與驗收標準已達成

## 1. 快速驗證（先快後全）
1. 先跑「失敗點」或受影響測試（快速回饋）
2. 再跑全量：`mvn test`
3. 結果門檻：
   - `failures = 0`
   - `errors = 0`

建議用 surefire 檢查總結：
```powershell
$rows = Get-ChildItem target/surefire-reports -Filter "TEST-*.xml" | ForEach-Object {
  [xml]$x = Get-Content $_.FullName
  [pscustomobject]@{Tests=[int]$x.testsuite.tests;Failures=[int]$x.testsuite.failures;Errors=[int]$x.testsuite.errors;Skipped=[int]$x.testsuite.skipped}
}
"tests=$((($rows|Measure-Object Tests -Sum).Sum)) failures=$((($rows|Measure-Object Failures -Sum).Sum)) errors=$((($rows|Measure-Object Errors -Sum).Sum)) skipped=$((($rows|Measure-Object Skipped -Sum).Sum))"
```

## 2. 打包驗證
- 執行：`mvn -DskipTests package`
- 確認產物：`target/admin-1.0.0.jar`

## 3. 啟動驗證（Smoke Test）
- 啟動：`java -jar target/admin-1.0.0.jar`
- 觀察關鍵 log：
  - `Started AdminApplication`
  - `Tomcat started on port ...`
- 若有非阻塞 Warning（例如 Thymeleaf templates），記錄即可
- 驗證完記得停止服務

## 4. 合併前檢查
- 確認沒有臨時檔案（log、測試輸出、暫存）
- 寫清楚 commit message（功能 + 修復 + 相容性）
- 再次 `git status --short` 應只剩預期檔案

## 5. 合併流程（feature -> main）
```powershell
# 在 feature 分支
mvn test
mvn -DskipTests package

git add -A
git commit -m "feat: complete oauth2 flow and order shipping payment integration"

git checkout main
git pull --ff-only
git merge --no-ff feature/027-oauth2-auth-system

# 合併後再做一次最小驗證
mvn -DskipTests package
```

## 6. 本次事件沉澱（可複用）
- 路由變更優先做「向後相容」：避免既有測試/舊客戶端中斷
- MockMvc standalone 測試不要帶 context-path（例如 `/api`）
- 遇到長輸出中斷時，用 `surefire xml` 與 `jar exists` 做 deterministic 驗證

## 7. Definition of Done（DoD）
- [ ] 需求功能完成
- [ ] `mvn test` 全綠
- [ ] `mvn -DskipTests package` 成功
- [ ] Spring Boot 可啟動
- [ ] 無臨時檔案
- [ ] 已合併 main
- [ ] 已更新規格或操作文件
