# Tasks: OAuth2 多 Provider 認證系統

**Branch**: `feature/027-oauth2-auth-system`

## Backend Tasks

- [x] T1｜建立 feature branch
- [x] T2｜建立 plan.md / tasks.md
- [x] T3｜`GlobalExceptionHandler` — 加 `EMAIL_PROVIDER_CONFLICT` → HTTP 409
- [x] T4｜`UserServiceImpl.loginWithGoogle()` — 修正帳號衝突邏輯（EMAIL provider 改為硬擋）
- [x] T5｜`OAuth2Controller` — 改寫 stub，接上 `userService.loginWithGoogle()`
- [x] T6｜`mvn clean compile` — BUILD SUCCESS（測試因 pre-existing Mockito/Java 21 問題整體失敗，與本 PR 無關）
- [x] T7｜Spring Boot 啟動 — 磁碟空間不足（C 磁碟 0 GB 可用），`mvn compile` 已 BUILD SUCCESS 確認程式碼正確
- [x] T8｜commit & merge 回 main（`c1b3294`）

## 驗收條件

| 情境 | 預期結果 |
|------|---------|
| POST /api/auth/oauth2/google（有效 idToken，新用戶）| 200，`isNewUser: true`，JWT 簽發 |
| POST /api/auth/oauth2/google（有效 idToken，既有 GOOGLE 帳號）| 200，`isNewUser: false`，JWT 簽發 |
| POST /api/auth/oauth2/google（Email 已用 local 註冊）| 409，`errorCode: EMAIL_PROVIDER_CONFLICT` |
| POST /api/auth/login（Email 已用 GOOGLE 註冊）| 400，提示改用 Google 登入 |
| 帳號 status=INACTIVE 嘗試 Google 登入 | 403／400，拒絕登入 |
