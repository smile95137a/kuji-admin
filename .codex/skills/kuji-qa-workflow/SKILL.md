---
name: kuji-qa-workflow
description: 為 KUJI-Server 產生與維護 QA 工作流程文件。當使用者要建立、整理或檢查後端 API、前端 E2E、手動測試案例、測試資料、測試證據、審核報表、放行紀錄，或要求所有 QA 文件使用繁體中文時使用。
---

# KUJI QA 工作流程

## 目標

建立一套可閱讀、可執行、可追蹤原因的 QA 文件。產出文件必須讓工程師、QA、PM 都能看懂：

- 測什麼功能
- 為什麼要測
- 如何判定成功或失敗
- 失敗原因是什麼
- 是否可以進入下一階段

## 基本規則

- 除非使用者明確要求，不得修改 production code。
- 優先產生或整理 `qa/` 底下的 QA 文件。
- 所有說明文字一律使用繁體中文。
- API 路徑、HTTP method、JSON key、Java class name、資料庫欄位可保留英文。
- 不得產生亂碼、HTML entity 或多餘的 Markdown 跳脫字元。
- 每份報表開頭都要說明「這份文件要呈現什麼內容」。
- 若使用代號，必須提供代號說明表。
- 測試結果不得只寫「成功」或「失敗」，必須補充成功原因或失敗原因。

## 建議輸出結構

```text
qa/
├─ test-cases/
│  ├─ manual-test-cases.csv
│  ├─ api-test-cases.json
│  └─ e2e-test-cases.json
├─ test-data/
│  ├─ users.csv
│  ├─ products.csv
│  ├─ product_details.csv
│  ├─ orders.csv
│  └─ draws.csv
├─ evidence/
│  ├─ screenshots/
│  ├─ playwright-report/
│  └─ api-test-results/
├─ reports/
│  └─ backend-qa-report.md
└─ approvals/
   ├─ qa-review.md
   ├─ qa-checklist.md
   └─ sign-off.md
```

## 測試案例要求

測試案例必須包含以下類型：

- 正向案例：正常操作應該成功。
- 負向案例：錯誤輸入、錯誤狀態或未登入應該失敗。
- 邊界案例：金額、庫存、抽數、分頁、時間等極限值。
- 權限案例：Admin、StoreOwner、StoreEditor、一般玩家的可操作範圍。
- 併發案例：同時抽獎、最後庫存、重複請求、重複 callback。

## 後端 API 測試必填資訊

每個後端 API 測試案例都要能回答：

- API 路徑與 method 是什麼
- 測試目的
- 前置條件
- Request body 或 query
- 預期 HTTP 狀態碼
- 預期回應重點
- 成功原因
- 失敗原因
- 需要保存的證據

## 抽獎、訂單與金流必測情境

抽獎、刮刮樂、訂單、付款相關功能必須包含：

- 餘額不足
- 庫存不足
- 重複請求
- 併發抽獎
- 獎品已售完
- 付款成功但訂單建立失敗
- 訂單成功但庫存更新失敗
- 權限不足或跨店存取

## Manual CSV 欄位

`manual-test-cases.csv` 必須包含：

```text
id,module,feature,priority,precondition,steps,expected_result,test_data_file,evidence_required,status,reviewer,reviewed_at
```

狀態值一律使用繁體中文：

- 未執行
- 通過
- 失敗
- 阻塞
- 不適用

## 代號說明

報表中若出現以下代號，必須附上說明：

| 代號 | 說明 |
|------|------|
| P0 | 重大風險。影響金流、扣點、庫存、訂單、權限或資料一致性，通常不可上線 |
| P1 | 核心流程。失敗會影響主要使用者流程 |
| P2 | 一般功能或次要流程。需修正但通常不阻擋主流程 |
| MQA | Manual QA，人工測試案例 |
| AQA | API QA，後端 API 測試案例 |
| E2E | End-to-End，前端到後端完整流程測試 |

## 審核文件要求

`qa-review.md` 必須說明：

- 已產生哪些檔案
- 每份報表要呈現什麼內容
- 測試範圍
- 高風險區域
- 已知缺口
- 是否可以進入下一階段

`backend-qa-report.md` 必須說明：

- 報表目的
- 測試環境
- 代號說明
- 測試範圍
- 後端 API 檢查表
- 成功與失敗判定規則
- 失敗原因紀錄表
- 待確認問題
- 最終結論

`sign-off.md` 必須包含：

- 審核人
- 審核狀態
- 日期
- 放行條件
- 未放行原因
- 備註

## 問答式協作

當使用者表示「看不懂」、「不知道報表要呈現什麼」、「要先檢查後端」時，先用 1 到 3 個問題釐清：

- 這次是否只檢查後端 API？
- 報表要作為內部 QA、交付 PM、還是上線放行依據？
- 目前要先檢查哪個模組，例如商品、刮刮樂、抽獎、訂單、付款、權限、audit log？

若使用者沒有明確回答，採用保守預設：先產生後端 API QA 報表，重點放在商品、刮刮樂、抽獎、訂單、付款、權限與資料一致性。


## User Story 測試清單要求

當使用者要求建立測試清單、驗收清單、API 測試案例或讓 QA / 工程師依照情境測試時，必須以 User Story 為核心產出測試清單。

每一個 User Story 必須包含：

- User Story：作為誰，我想做什麼，為了達成什麼目的
- 測試情境：這次要驗證的操作情境
- 使用角色：Admin、StoreOwner、StoreEditor、一般玩家、未登入使用者
- 前置條件：測試前資料狀態，例如餘額、庫存、訂單狀態、登入狀態
- API path
- HTTP method
- Request body / query / path variable
- 預期 HTTP status
- 預期 response body 重點
- 成功判定
- 失敗判定
- DB 檢查點
- 需要保存的測試證據
- 優先級：P0 / P1 / P2

## User Story API 驗收清單格式

產出 User Story 測試清單時，優先使用 Markdown 表格。

欄位必須包含：

| id | user_story | role | scenario | precondition | api | method | request | expected_status | expected_response | success_criteria | failure_criteria | db_check | priority |
|----|------------|------|----------|--------------|-----|--------|---------|-----------------|-------------------|------------------|------------------|----------|----------|