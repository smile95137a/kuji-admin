---
description: "API 分層與回傳規則。規範 Controller / Service / Mapper 職責、ResponseEntity 回傳方式，以及只在對應層處理對應責任。"
applyTo: ["src/main/java/**/controller/**", "src/main/java/**/service/**", "src/main/java/**/mapper/**", "src/main/resources/mapper/**", "src/main/java/**/req/**", "src/main/java/**/res/**"]
---

# API 分層規則

## 必須

- Controller 只處理請求接收、基本驗證、呼叫 Service、回傳 `ResponseEntity`
- Service 才能放業務邏輯、交易控制、狀態轉移、權限判斷
- Mapper / XML 只負責資料存取，不混入業務判斷
- Controller 回傳格式遵循專案既有 AOP 包裝機制，不自行發明新格式
- 所有清單查詢都要有明確排序依據

## 建議

- 新增查詢 API 時，優先沿用現有 `Condition + QueryReq` 模式
- 若欄位由後端自動帶入，例如 `storeId`，就不要要求前端傳入
- 排序規則要明確寫在 Service 或 SQL，而不是留給前端猜

## 禁止

- ❌ 不要在 Controller 直接寫業務邏輯
- ❌ 不要在 Service 之外直接操作 HTTP 細節
- ❌ 不要依賴資料庫自然順序當成前端顯示順序
- ❌ 不要手動包裝一套與既有 AOP 不一致的 API 結構
