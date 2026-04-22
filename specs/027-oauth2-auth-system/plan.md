# Implementation Plan: OAuth2 多 Provider 認證系統

**Branch**: `feature/027-oauth2-auth-system`  
**Created**: 2026-04-22  
**Status**: In Progress

## 現況盤點

| 檔案 | 現況 | 需要做什麼 |
|------|------|----------|
| `UserServiceImpl.loginWithGoogle()` | ✅ 已存在，但帳號衝突時**合併帳號**（錯誤行為） | 改為硬擋並拋 `BusinessException(EMAIL_PROVIDER_CONFLICT)` |
| `OAuth2Controller` | ❌ Stub，未接 service | 改寫為呼叫 `userService.loginWithGoogle()` 並回傳 AuthRes |
| `GlobalExceptionHandler` | ❌ 缺少 `EMAIL_PROVIDER_CONFLICT` → 409 對應 | 在 switch 加入新 errorCode |
| pom.xml | ✅ `spring-boot-starter-oauth2-client` 已存在 | 無需改動 |
| `user` 表 | ✅ `provider` + `provider_id` 已存在 | 無需 migration |

## 實作順序

1. **GlobalExceptionHandler** — 加 errorCode 對應（最小改動，先做）
2. **UserServiceImpl.loginWithGoogle()** — 修正衝突邏輯核心
3. **OAuth2Controller** — 改寫成真正的 endpoint

## 注意事項

- Google token 驗證沿用現有方式（`https://oauth2.googleapis.com/tokeninfo?id_token=xxx`）
  - 優點：不需引入 `google-api-client` 依賴，保持輕量
  - 已有 `@Value("${google.client-id:}")` 但目前未用於驗證，暫不強制檢查 aud（tokeninfo 已可防偽造）
- Refresh Token 沿用現有 JWT refreshToken 機制
- `isNewUser` flag 已在現有 service 實作，沿用即可
