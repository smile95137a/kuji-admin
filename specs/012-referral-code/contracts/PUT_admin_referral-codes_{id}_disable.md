# 合約：PUT /admin/referral-codes/{id}/disable

**停用推薦碼（管理員操作，v1.0 不可復原）**

## 基本資訊

| 欄位 | 值 |
|-------|-------|
| Method | PUT |
| Path | `/admin/referral-codes/{id}/disable` |
| Auth | Bearer JWT (ROLE_ADMIN) |
| Content-Type | N/A（無請求體） |
| Controller | `AdminReferralCodeController.disableCode()` |
| Service | `ReferralCodeService.disableCode()` |
| Status | NEW — add to existing AdminReferralCodeController |

## 請求

### 路徑參數

| 參數 | 型別 | 說明 |
|-----------|------|-------------|
| id | String (UUID) | 要停用的推薦碼 ID |

### 標頭
```
Authorization: Bearer <admin_jwt_token>
```

### 請求體
無

## 回應

### 200 OK — 停用成功
```json
{
  "code": 200,
  "message": "推薦碼已停用",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "code": "ABC12345",
    "storeId": "550e8400-e29b-41d4-a716-446655440000",
    "storeName": "Dream Store",
    "description": "2026 Spring Campaign",
    "isActive": false,
    "usedCount": 12,
    "maxUsage": 100,
    "validFrom": "2026-03-01T00:00:00",
    "validUntil": "2026-06-30T23:59:59",
    "createdAt": "2026-03-22T10:00:00"
  }
}
```

### 404 Not Found — 代碼不存在
```json
{
  "code": 404,
  "message": "推薦碼不存在",
  "data": null
}
```

### 400 Bad Request — 代碼已停用
```json
{
  "code": 400,
  "message": "推薦碼已經是停用狀態",
  "data": null
}
```

### 403 Forbidden — 權限不足
```json
{
  "code": 403,
  "message": "權限不足",
  "data": null
}
```

## 業務規則

1. 在 `referral_code` 資料表中將 `is_active` 設為 0 並更新 `updated_at`
2. **不**刪除現有的 `ReferralRecord` 紀錄——過去的推薦紀錄予以保留（規格邊緣案例）
3. 停用後，任何使用此代碼的新註冊嘗試將回傳 `REFERRAL_CODE_DISABLED`
4. 冪等性：對已停用的代碼再次呼叫停用，回傳 400（而非靜默成功），以明確呈現狀態

## 實作說明

```java
// In AdminReferralCodeController
@PutMapping("/{id}/disable")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<ReferralCodeRes>> disableCode(@PathVariable String id) {
    ReferralCodeRes result = referralCodeService.disableCode(id);
    return ResponseEntity.ok(ApiResponse.success("推薦碼已停用", result));
}

// In ReferralCodeService (add method)
ReferralCodeRes disableCode(String id);

// In ReferralCodeServiceImpl
public ReferralCodeRes disableCode(String id) {
    ReferralCode code = referralCodeMapper.selectByPrimaryKey(id);
    if (code == null) throw new BusinessException("推薦碼不存在");
    if (!code.getIsActive()) throw new BusinessException("推薦碼已經是停用狀態");
    code.setIsActive(false);
    code.setUpdatedAt(LocalDateTime.now());
    referralCodeMapper.updateByPrimaryKeySelective(code);
    return convertToRes(code);
}
```
