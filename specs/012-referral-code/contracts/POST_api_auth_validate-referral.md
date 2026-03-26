# 合約：POST /api/auth/validate-referral

**在使用者註冊期間驗證推薦碼（公開端點）**

## 基本資訊

| 欄位 | 值 |
|-------|-------|
| Method | POST |
| Path | `/api/auth/validate-referral` |
| Auth | 無（公開存取——註冊流程） |
| Content-Type | application/json |
| Controller | `ReferralCodeValidateController.validateForRegistration()` |
| Service | `ReferralCodeService.validateCode(String code)` |
| Status | NEW — existing GET /auth/referral-code/validate/{code} covers admin use; this POST is for frontend registration UX |

## 請求

### 請求體
```json
{
  "code": "ABC12345"
}
```

| 欄位 | 型別 | 必填 | 驗證規則 |
|-------|------|----------|------------|
| code | String | 是 | 1–20 字元；查詢前去除空白並轉為大寫 |

## 回應

### 200 OK — 代碼有效
```json
{
  "code": 200,
  "message": "推薦碼有效",
  "data": {
    "valid": true,
    "code": "ABC12345",
    "storeName": "Dream Store"
  }
}
```
備註：回傳 `storeName` 以便註冊表單顯示「推薦店家：Dream Store」的 UX 確認訊息。

### 200 OK — 代碼無效或已停用
```json
{
  "code": 200,
  "message": "推薦碼無效或已停用",
  "data": {
    "valid": false,
    "code": "ABC12345",
    "storeName": null
  }
}
```
備註：驗證失敗回傳 HTTP 200（而非 4xx），因為這是客戶端表單檢查，不是錯誤狀況。使用者仍可在不提供代碼的情況下繼續註冊。

### 400 Bad Request — 缺少 code 欄位
```json
{
  "code": 400,
  "message": "請輸入推薦碼",
  "data": null
}
```

## 請求 DTO：ReferralValidateReq（新增）

```java
@Data
public class ReferralValidateReq {
    @NotBlank(message = "請輸入推薦碼")
    @Size(max = 20)
    private String code;
}
```

## 回應 DTO：ReferralValidateRes（新增或內聯）

```java
@Data
@AllArgsConstructor
public class ReferralValidateRes {
    private boolean valid;
    private String code;
    private String storeName;
}
```

## 驗證邏輯（在 ReferralCodeService.validateCode 中）

```
1. Trim and uppercase input code
2. Query referral_code WHERE code = ? AND is_active = 1
3. If not found → return valid=false
4. Query store WHERE id = referral_code.store_id AND status = 'ACTIVE'
5. If store inactive → return valid=false
6. If maxUsage set AND usedCount >= maxUsage → return valid=false
7. If validUntil set AND now() > validUntil → return valid=false
8. Return valid=true, storeName=store.storeName
```

## 安全性說明

- 此端點為公開存取（無需 JWT）——必須加入 `SecurityConfig` 的 permitAll 清單
- 端點**不**建立任何紀錄、遞增任何計數器或變更任何狀態
- 限流建議：前端應使用 debounce 避免過度頻繁的查詢

## 與現有端點的區別

| 端點 | Method | Auth | 用途 |
|----------|--------|------|---------|
| `/auth/referral-code/validate/{code}` | GET | 無 | 現有——類似功能，但使用 GET 加路徑參數 |
| `/auth/referral-code/info/{code}` | GET | 無 | 現有——回傳遮罩後的資訊物件 |
| `/api/auth/validate-referral` | POST | 無 | 新增——POST 請求體，回傳 storeName 用於 UX |
| `/admin/referral-codes/validate/{code}` | GET | ADMIN | 管理員工具驗證 |
