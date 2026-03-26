# 快速入門：推薦碼 (Referral Code)

**Feature**: 012-referral-code
**Branch**: `012-referral-code`
**Stack**: Spring Boot 3.3.3 + Java 21 + MyBatis + MySQL 8.3

---

## 現有功能 vs 新增功能

推薦碼系統**大部分已預先建置**。以下為實作差異一覽：

| 元件 | 狀態 | 所需動作 |
|-----------|--------|----------------|
| `ReferralCode` entity | ✅ 已存在 | 無 |
| `ReferralRecord` entity | ✅ 已存在 | 無 |
| `ReferralCodeMapper` + XML | ✅ 已存在 | 無 |
| `ReferralRecordMapper` + XML | ✅ 已存在 | 無 |
| `ReferralCodeRepository` | ✅ 已存在 | 新增 `selectStatsByStore()` |
| `ReferralRecordRepository` | ✅ 已存在 | 新增 `selectTimelineByStore()` |
| `ReferralCodeService` 介面 | ✅ 已存在 | 新增 `disableCode()`、`getReferralStats()` |
| `ReferralCodeServiceImpl` | ✅ 已存在 | 實作新方法 + 強化防護邏輯 |
| `AdminReferralCodeController` | ✅ 已存在 | 新增 `PUT /{id}/disable` + `GET /stats` |
| `ReferralCodeValidateController` | ✅ 已存在 | 新增 `POST /validate-referral` |
| `ReferralCodeCreateReq` | ✅ 已存在 | 無 |
| `ReferralCodeRes` | ✅ 已存在 | 無 |
| `ReferralValidateReq` | ❌ 新增 | 建立 DTO |
| `ReferralStatsRes` | ❌ 新增 | 建立 DTO |
| 自我推薦防護 | ❌ 缺少 | 新增至 `useCode()` |
| 店家停用攔截 | ❌ 部分 | 確認存在於 `validateCode()` |
| `referral_record.user_id` 的 `UNIQUE INDEX` | ❌ 缺少 | 執行遷移 SQL |

---

## 步驟 1：執行資料庫遷移

```sql
-- sql/V012__add_referral_record_user_unique.sql
ALTER TABLE referral_record
  ADD UNIQUE INDEX idx_referral_record_user_id (user_id);
```

在目標資料庫（本機開發、RDS 等）上執行，然後再啟動伺服器。

---

## 步驟 2：建立新 DTO

### ReferralValidateReq.java
```
src/main/java/com/group/admin/dto/request/ReferralValidateReq.java
```
```java
@Data
public class ReferralValidateReq {
    @NotBlank(message = "請輸入推薦碼")
    @Size(max = 20)
    private String code;
}
```

### ReferralStatsRes.java
```
src/main/java/com/group/admin/dto/response/ReferralStatsRes.java
```
```java
@Data
public class ReferralStatsRes {
    private String storeId;
    private String storeName;
    private Long totalReferrals;
    private Long activeCodeCount;
    private List<DailyCount> timeline;

    @Data @AllArgsConstructor
    public static class DailyCount {
        private String date;
        private Long count;
    }
}
```

---

## 步驟 3：新增 Service 方法

在 `ReferralCodeService.java` 中新增：
```java
ReferralCodeRes disableCode(String id);
List<ReferralStatsRes> getReferralStats(ReferralReportCondition condition);
```

在 `ReferralCodeServiceImpl.java` 中實作：

### disableCode()
```java
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

### getReferralStats()
```java
public List<ReferralStatsRes> getReferralStats(ReferralReportCondition condition) {
    // 1. Query per-store totals
    List<Map<String, Object>> totals = referralCodeRepository.selectStatsByStore(condition);
    // 2. Query daily timeline
    List<Map<String, Object>> timeline = referralRecordRepository.selectTimelineByStore(condition);
    // 3. Merge by storeId → List<ReferralStatsRes>
    return mergeStats(totals, timeline);
}
```

### 強化 useCode()——新增自我推薦檢查
```java
// Before creating the ReferralRecord, check for self-referral:
Store store = storeMapper.selectByPrimaryKey(referralCode.getStoreId());
AdminUser storeOwner = adminUserMapper.selectByPrimaryKey(store.getOwnerId());
if (storeOwner != null && storeOwner.getEmail() != null
        && storeOwner.getEmail().equalsIgnoreCase(registrationEmail)) {
    throw new BusinessException("不允許自我推薦");
}
```

---

## 步驟 4：新增 Controller 端點

### 在 AdminReferralCodeController.java 中

```java
// Add to existing controller:

@PutMapping("/{id}/disable")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<ReferralCodeRes>> disableCode(
        @PathVariable String id) {
    return ResponseEntity.ok(
        ApiResponse.success("推薦碼已停用", referralCodeService.disableCode(id)));
}

@GetMapping("/stats")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<List<ReferralStatsRes>>> getReferralStats(
        ReferralReportCondition condition) {
    return ResponseEntity.ok(
        ApiResponse.success(referralCodeService.getReferralStats(condition)));
}
```

### 在 ReferralCodeValidateController.java 中

```java
// Add to /api/auth path:
@PostMapping("/validate-referral")
public ResponseEntity<ApiResponse<ReferralValidateRes>> validateForRegistration(
        @Valid @RequestBody ReferralValidateReq req) {
    ReferralValidateRes result = referralCodeService.validateForRegistration(req.getCode());
    return ResponseEntity.ok(ApiResponse.success(result));
}
```

### 在 SecurityConfig.java 中——新增至 permitAll

```java
.requestMatchers("/api/auth/validate-referral").permitAll()
```

---

## 步驟 5：確認註冊整合

在 `UserServiceImpl.register()` 中，確認推薦錯誤處理矩陣：

| 錯誤 | 預期行為 |
|-------|------------------|
| 代碼不存在 | 拋出例外 → 註冊**失敗**，回傳 400 |
| 代碼已停用 | 拋出例外 → 註冊**失敗**，回傳 400 |
| 店家非活躍 | 拋出例外 → 註冊**失敗**，回傳 400 |
| 已使用推薦 | 拋出例外 → 註冊**失敗**，回傳 400 |
| 自我推薦 | 拋出例外 → 註冊**失敗**，回傳 400 |
| 未提供代碼 | 靜默跳過 → 註冊**成功** |
| 代碼有效 | 建立紀錄 → 註冊**成功** |

---

## 步驟 6：測試情境

### 管理員——建立推薦碼
```bash
curl -X POST http://localhost:8080/admin/referral-codes \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"storeId":"<store-uuid>","description":"Test Campaign"}'
# Expect: 201 with generated code like "A1B2C3D4"
```

### 管理員——停用推薦碼
```bash
curl -X PUT http://localhost:8080/admin/referral-codes/<code-uuid>/disable \
  -H "Authorization: Bearer $ADMIN_TOKEN"
# Expect: 200 with isActive=false
```

### 管理員——查看統計
```bash
curl http://localhost:8080/admin/referral-codes/stats \
  -H "Authorization: Bearer $ADMIN_TOKEN"
# Expect: 200 with list of per-store stats including timeline
```

### 使用者——註冊前驗證代碼
```bash
curl -X POST http://localhost:8080/api/auth/validate-referral \
  -H "Content-Type: application/json" \
  -d '{"code":"A1B2C3D4"}'
# Expect: 200 with valid=true, storeName="..."
```

### 使用者——攜帶推薦碼註冊
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email":"newuser@example.com",
    "password":"pass1234",
    "confirmPassword":"pass1234",
    "nickname":"TestUser",
    "phoneNumber":"0912345678",
    "addressName":"Test Name",
    "zipCode":"100",
    "city":"台北市",
    "area":"中正區",
    "address":"測試路123號",
    "referralCode":"A1B2C3D4",
    "agreeTerms":true
  }'
# Expect: 200 with JWT tokens; ReferralRecord created in DB
```

### 使用者——使用無效代碼註冊
```bash
# Same as above but with referralCode: "BADCODE1"
# Expect: 400 with "推薦碼無效或已停用"
```

### 使用者——重複註冊（相同 email 已註冊）
```bash
# Attempt to register again with same email
# Expect: 400 — email already registered (no relation to referral)
```

---

## 資料庫驗證查詢

```sql
-- Confirm referral code was created
SELECT id, code, store_id, is_active, used_count FROM referral_code ORDER BY created_at DESC LIMIT 5;

-- Confirm referral record was created after registration
SELECT rr.id, u.email, rr.used_code, rr.store_id, rr.referred_at
FROM referral_record rr
JOIN user u ON u.id = rr.user_id
ORDER BY rr.referred_at DESC LIMIT 5;

-- Confirm user has referralCode field set
SELECT id, email, referral_code, referred_store_id FROM user WHERE referral_code IS NOT NULL LIMIT 5;

-- Stats query preview
SELECT s.store_name, COUNT(rr.id) AS total_referrals
FROM store s
LEFT JOIN referral_code rc ON rc.store_id = s.id
LEFT JOIN referral_record rr ON rr.referral_code_id = rc.id
GROUP BY s.id, s.store_name;
```

---

## 架構摘要

```
Frontend (registration form)
    │
    ├─ POST /api/auth/validate-referral  ← inline check as user types
    │
    └─ POST /api/auth/register  ← { ..., referralCode: "ABC12345" }
                │
                ▼
        UserServiceImpl.register()
                │
                ├─ Validates user data
                ├─ Creates User record
                └─ Calls referralCodeService.useCode(userId, code)
                        │
                        ├─ Validates code exists + active
                        ├─ Validates store active
                        ├─ Checks self-referral
                        ├─ Checks user not already referred
                        ├─ Creates ReferralRecord (immutable)
                        ├─ Increments usedCount on ReferralCode
                        └─ Sets User.referralCode + User.referredStoreId

Admin Dashboard
    ├─ GET /admin/referral-codes         ← list all codes
    ├─ POST /admin/referral-codes        ← create new code
    ├─ PUT /admin/referral-codes/{id}/disable  ← disable code
    └─ GET /admin/referral-codes/stats   ← per-store stats + timeline
```
