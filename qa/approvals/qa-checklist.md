# QA 執行清單

這份文件要呈現什麼內容：
列出 QA 執行前、中、後必須確認的事項，避免測試只做了一半卻無法判斷是否可放行。

## 準備階段

- [ ] 已確認本次測試只會使用 `qa/test-data/` 的固定資料或可追蹤資料。
- [ ] 已載入 `qa/test-data/users.csv` 中的測試帳號。
- [ ] 已載入 `qa/test-data/products.csv` 與 `qa/test-data/product_details.csv`。
- [ ] 已準備 `qa/test-data/orders.csv` 與 `qa/test-data/draws.csv` 的測試情境。
- [ ] 已確認本地或測試環境 API base URL 為 `http://localhost:8080/api` 或當前指定環境。

## 後端 API 測試

- [ ] 已執行所有 P0 API 案例。
- [ ] 已執行所有 P1 API 案例。
- [ ] 已確認認證與 token 流程。
- [ ] 已確認商品建立、欄位邊界與權限限制。
- [ ] 已確認餘額不足、庫存不足、重複請求與併發抽獎。
- [ ] 已確認付款成功但訂單建立失敗可被追蹤。
- [ ] 已確認訂單成功但庫存更新失敗有補償或回滾資訊。

## 人工與 E2E 測試

- [ ] 已執行所有 P0/P1 人工測試案例。
- [ ] 已確認刮刮樂格子位置不跳動。
- [ ] 已確認儲值測試模式不會誤導向第三方金流。
- [ ] 已確認後台角色權限與跨店限制。

## 證據保存

- [ ] 已將畫面截圖放入 `qa/evidence/screenshots/`。
- [ ] 已將 API 回應或結果檔放入 `qa/evidence/api-test-results/`。
- [ ] 已將 Playwright 或瀏覽器自動化報告放入 `qa/evidence/playwright-report/`。
- [ ] 每個失敗案例都已記錄 case id、requestId、環境、重現步驟與失敗原因。

## 審核與放行

- [ ] 已更新 `qa/reports/backend-qa-report.md` 的執行結果。
- [ ] 已更新 `qa/approvals/sign-off.md` 的審核狀態。
- [ ] P0 問題已清空，或已明確列出不可放行原因。
- [ ] P1 問題已被接受、修復，或已列出風險說明。
