# Tasks: OAuth2 多 Provider 認證系統

**Branch**: `feature/027-oauth2-auth-system`

## Backend Tasks

- [x] T1｜建立 feature branch
- [x] T2｜建立 plan.md / tasks.md
- [ ] T3｜`GlobalExceptionHandler` — 加 `EMAIL_PROVIDER_CONFLICT` → HTTP 409
- [ ] T4｜`UserServiceImpl.loginWithGoogle()` — 修正帳號衝突邏輯（EMAIL provider 改為硬擋）
- [ ] T5｜`OAuth2Controller` — 改寫 stub，接上 `userService.loginWithGoogle()`
- [ ] T6｜`mvn test` — 確認測試通過
- [ ] T7｜Spring Boot 啟動測試
- [ ] T8｜commit & merge 回 main

## 驗收條件

| 情境 | 預期結果 |
|------|---------|
| POST /api/auth/oauth2/google（有效 idToken，新用戶）| 200，`isNewUser: true`，JWT 簽發 |
| POST /api/auth/oauth2/google（有效 idToken，既有 GOOGLE 帳號）| 200，`isNewUser: false`，JWT 簽發 |
| POST /api/auth/oauth2/google（Email 已用 local 註冊）| 409，`errorCode: EMAIL_PROVIDER_CONFLICT` |
| POST /api/auth/login（Email 已用 GOOGLE 註冊）| 400，提示改用 Google 登入 |
| 帳號 status=INACTIVE 嘗試 Google 登入 | 403／400，拒絕登入 |
