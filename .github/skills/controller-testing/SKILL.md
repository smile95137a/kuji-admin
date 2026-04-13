---
name: controller-testing
description: "Test Spring Boot Controllers with MockMvc + JUnit 5 + Mockito. Use when writing or reviewing Controller CRUD tests, API validation tests, or CI-ready test suites."
---

# Controller 測試工作流

## When to Use
- 為 Controller 建立完整 CRUD API 測試
- 驗證 request validation annotation 的覆蓋率
- 確保測試可用於 CI 驗證
## 核心原則
- **單元疑沂**：Controller 層測試不依賴實髮 Service，使用 @WebMvcTest + Mockito
- **三大測試誘：**Success case 、Validation error、Not found error
- **註記不写**：使用 Mockito 的 @Mock 第三方手並設置預期元估訊息
- **獨立性**：每個測試敦樴獨立，後續測試患不依賴前一個
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

## ⚠️ 禁止操作

- ❌ 不要在 @WebmvcTest 中省略 mock Service（會導致測試不獨立）
- ❌ 不要只測試 success case，validation error 是必須的
- ❌ 不要在測試中使用實際資料庫或外部服務
- ❌ 不要用 magic value，每個測試值都應有明確的語義
- ❌ 不要讓測試相互依賴（執行順序不應影響結果）
- ❌ 不要跳過 edge case（null、空字串、邊界值）
- ❌ 不要在生產環境跑測試，必須在獨立的測試環境

## 參考文件
- [CI 嚴格規範](./references/controller-test-strict.md)
- [一般測試規範](./references/controller-testing.md)
