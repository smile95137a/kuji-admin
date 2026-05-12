# AI 接手執行指引（2026-05-11）

最後更新：2026-05-11（本次會話同步）

## 最新增補（2026-05-12）

1. OAuth2 標準流程已落地（redirect/callback），下一棒優先做端到端手測：
  - `GET /api/oauth2/authorization/google`
  - callback 成功/失敗都要驗證前端 `oauth2/callback` 接收是否正常
2. SMTP 關鍵流程保護已落地，下一棒優先驗證四條路徑實收信：
  - 前台 forgot password
  - 後台 forgot password
  - 後台建立 StoreOwner
  - 後台 reset password
3. 遠端環境必查：
  - `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET`
  - `APP_FRONTEND_URL`
  - `GMAIL_USERNAME` / `GMAIL_APP_PASSWORD`
4. 若遠端仍出現「API 成功但沒收到信」，先查 `email_log` 狀態是否仍有舊資料表/舊欄位殘留問題，再決定是否走 DB migration 補丁包。

## 收尾狀態

1. 本輪三 repo 已完成提交並推送，當前可視為乾淨接手點。
2. 下一位 AI 不需先清理歷史變更，可直接進入下一包執行。

## 一句話總結

會員主線第一包已完成，平台營收報表補測（第二包）已完成後端單元測試；下一棒優先做跨 repo 契約同步與 smoke 驗證。

## 使用者工作偏好（必須遵守）

1. 先一次討論完整範圍，再一次性改完，不接受斷續零碎修改。
2. 每次實作前必須明確列出：影響檔案、行為變更點、驗證方式。
3. 每包結束時要回報：已完成 / 待評估 / 下一包，讓接手者可無縫續做。
4. 前後端同步需持續進行，不可只修單邊契約。
5. 每包完成後需落實版本節點：`commit + push`。

## Repo 現況（本工作區）

1. 後端 repo：`C:/Users/003707/Desktop/新增資料夾/kuji-admin`
2. 本次重點改動檔案：
  - `src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`
  - `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java`
  - `docs/AI_HANDOFF_CURRENT.md`
3. 目前觀察：終端 compile 輸出偶發空白，但未見新的阻斷編譯錯誤訊息。

## 已完成（本輪可延續事實）

1. 平台營收關鍵修正（`ReportServiceImpl`）
  - 店家映射路徑擴充為 direct lottery / ticket->lottery / order 三路。
  - 多處時間篩選統一為半開區間（`>= start`, `< endExclusive`）。
2. 商品服務資料相容（`LotteryServiceImpl`）
  - `tags`、`galleryImages` 統一 JSON 儲存。
  - 讀取支援 JSON + CSV fallback。
  - 已修正會造成編譯失敗的語法斷裂區段。
3. 會員管理前後端已做過一輪權限收斂與資料遮罩調整。
4. 平台營收補測已落地：
  - 新增 `src/test/java/com/group/admin/service/ReportServiceImplTest.java`
  - 驗證三路店家映射與半開區間
  - 測試結果：3 passed / 0 failed
5. 會員認證 smoke 測試已落地：
  - 更新 `src/test/java/com/group/admin/controller/api/ApiAuthControllerTest.java`
  - 覆蓋 refresh invalid / missing claims / gen mismatch / success rotation
  - 測試結果：8 passed / 0 failed

## 下一輪（請先做這包）

### 目標：跨 repo 契約同步與 smoke 驗證（一次性交付）

1. 後台前端契約同步（`kuji-admin-web`）
  - 對齊會員 list/detail DTO 新契約。
2. 補 login / logout / filter 串接驗證
  - 目前已覆蓋 refresh 主路徑，建議補 login/logout/filter 的整段驗證。
3. 驗證輸出
  - compile 成功證據（含 exit code）
  - 關鍵 API smoke 測試
  - 變更檔案阻斷錯誤為零
4. 收尾要求
  - 本包完成後更新三邊 handoff
  - 完成 `commit + push`

## 待評估清單（做完第一包再進）

1. 報表 period 定義與欄位命名是否仍有前後端語意落差。
2. 既有 Sonar / 規則警告是否要分包清理（不影響本輪 correctness 可先不做）。

## 接手流程（強制）

1. 先貼出「本包完整範圍 + 檔案清單 + 驗證方式」。
2. 等使用者確認後，一次性改完本包。
3. 結尾必須更新 `docs/AI_HANDOFF_CURRENT.md`（已完成 / 待評估 / 下一包）。

## 建議命令

1. compile 並輸出 exit code：
  - `mvn -f pom.xml -DskipTests compile; Write-Output "__EXIT:$LASTEXITCODE"`
2. 檢查變更：
  - `git status`
  - `git diff -- src/main/java/com/group/admin/service`
