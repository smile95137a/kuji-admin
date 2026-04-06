---
name: controller-testing
description: "Test Spring Boot Controllers with MockMvc + JUnit 5 + Mockito. Use when writing or reviewing Controller CRUD tests, API validation tests, or CI-ready test suites."
---

# Controller 測試工作流

## When to Use
- 為 Controller 建立完整 CRUD API 測試
- 驗證 request validation annotation 的覆蓋率
- 確保測試可用於 CI 驗證

## 技術規範
- Spring Boot + JUnit 5 + MockMvc + Mockito
- `@WebMvcTest`（除非有明確理由使用 `@SpringBootTest`）

## 測試範圍（每一個 API 都必須）
1. Success case
2. Request validation error（`@NotNull`, `@Size`, `@Email` 等）
3. Resource not found
4. 非法輸入（null、空字串、格式錯誤）

## 強制規則
1. 所有 CRUD API 必須被測試
2. 不允許只測 success
3. 所有 validation annotation 都必須對應測試
4. 每個 API 至少 3 個測試情境
5. 不允許 magic value，使用清楚的測試資料
6. 測試必須可重複執行、不依賴外部狀態、不使用實際 DB

## 測試品質要求
- 測試命名需反映使用者行為
- Mock 行為需明確定義
- 回傳 status 與 response body 需驗證
- 不可省略 edge case

## 參考文件
- [CI 嚴格規範](./references/controller-test-strict.md)
- [一般測試規範](./references/controller-testing.md)
