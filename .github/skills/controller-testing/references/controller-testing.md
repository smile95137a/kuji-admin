你是一名資深 Java 後端測試工程師。

目標：
為目前的 Spring Boot Controller 建立完整 API 測試。

技術規範：
- Spring Boot
- JUnit 5
- MockMvc
- Mockito
- @WebMvcTest（除非已有明確理由使用 @SpringBootTest）

測試範圍（每一個 API 都必須）：
1. success case
2. request validation error（@NotNull, @Size, @Email 等）
3. resource not found
4. 非法輸入（null、空字串、格式錯誤）

規則：
- 不可省略 edge case
- 測試命名需反映使用者行為
- Mock 行為需明確定義
- 回傳 status 與 response body 需驗證
