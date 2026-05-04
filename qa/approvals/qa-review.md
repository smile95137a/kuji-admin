# QA 檢查總覽

這份文件要呈現什麼內容：
整理本次 `qa/` 產出的所有 QA 文件、每份文件的用途、測試範圍、高風險區域、已知缺口，以及目前是否適合進入下一階段。

## 已產生檔案

- `qa/test-cases/manual-test-cases.csv`：人工測試案例清單，適合手動執行與驗證畫面或跨系統行為。
- `qa/test-cases/api-test-cases.json`：後端 API 測試案例，重點放在認證、商品、抽獎、訂單、付款與權限。
- `qa/test-cases/e2e-test-cases.json`：前後台整體流程驗證案例，供 Playwright 或手動回歸使用。
- `qa/test-data/users.csv`：固定測試帳號資料。
- `qa/test-data/products.csv`：商品測試資料。
- `qa/test-data/product_details.csv`：獎品與庫存測試資料。
- `qa/test-data/orders.csv`：訂單與付款失敗情境資料。
- `qa/test-data/draws.csv`：抽獎與併發情境資料。
- `qa/reports/backend-qa-report.md`：後端 QA 檢查報表，說明後端應該檢查什麼、如何判定成功與失敗。
- `qa/approvals/qa-checklist.md`：執行前、中、後的 QA 勾選清單。
- `qa/approvals/sign-off.md`：最終審核與放行紀錄。
- `qa/evidence/screenshots/.gitkeep`
- `qa/evidence/playwright-report/.gitkeep`
- `qa/evidence/api-test-results/.gitkeep`

## 測試範圍

本次 QA 文件涵蓋：

- Spring Boot 後端 API
- Vue 前後台流程
- 會員管理與認證
- 商品管理、獎品明細與庫存規則
- 抽獎、刮刮樂與併發控制
- 訂單、出貨、付款與回呼失敗情境
- 後台角色權限與跨店資料存取限制

## 高風險區域

- 抽獎交易是否能同時正確處理扣點、扣庫存與賞品盒建立
- 最後一抽併發時是否會超賣或重複扣點
- 售完的大獎是否還可能被抽出
- 測試模式儲值是否誤走第三方金流
- 付款成功但訂單建立失敗時是否留下可追蹤紀錄
- 訂單成功但庫存更新失敗時是否有補償或回滾資訊
- StoreOwner 與 StoreEditor 是否被正確限制在各自權限範圍內

## 已知缺口

- 實際 production 或 staging 的錯誤碼與訊息仍需以執行結果確認。
- 付款失敗與庫存更新失敗情境可能需要故障注入、stub 或特製測試資料。
- E2E 路由名稱依目前文件推定，執行前仍應對照實際前端路由。

## 是否可進入下一階段

可以。這批檔案適合作為 QA 執行與審核起點，且未修改 production code。下一步建議先以 `qa/reports/backend-qa-report.md` 為主，優先執行後端 API 與 P0/P1 案例。
