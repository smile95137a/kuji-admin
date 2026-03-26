請以 Copilot Agent 模式執行以下任務。

任務目標：
為目前的 Spring Boot Controller 建立「完整 CRUD API 測試」。

執行方式：
- 你可以自行開啟、建立、修改多個檔案
- 直到測試類別完整為止

測試技術：
- Spring Boot
- MockMvc
- Mockito
- JUnit 5

測試要求（每一個 API）：
1. success case
2. request validation error
3. resource not found
4. 非法輸入（模擬使用者亂輸入）

額外規則：
- 測試命名必須描述「使用者行為」
- Mock 行為需完整定義
- 不可省略 edge case
- 若發現 Controller 設計不利於測試，可合理調整測試方式

完成標準：
- 所有 CRUD API 都有測試
- 測試結構清楚
- 可直接執行
