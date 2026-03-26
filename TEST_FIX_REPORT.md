# API 測試失敗修正報告

## 執行時間
2025-01-21

## 問題分析與修正

### ✅ 已修正的問題

#### 1. ❌ [2] 取得使用者選單 - 400 錯誤

**問題原因**：
- 測試腳本使用了錯誤的路徑：`/admin/users/menu`
- 實際 API 路徑：`/admin/menus/accessible`

**修正方式**：
```powershell
# 修正前
Invoke-RestMethod -Uri '%BASE_URL%/admin/users/menu' ...

# 修正後
Invoke-RestMethod -Uri '%BASE_URL%/admin/menus/accessible' ...
```

**預期結果**：✅ PASS

---

#### 2. ❌ [3] 取得店家列表 - 500 錯誤

**問題原因**：
- 測試腳本使用了不存在的路徑：`/admin/store/list` (POST)
- 實際 API 路徑：`/admin/stores/options` (GET)

**修正方式**：
```powershell
# 修正前
Invoke-RestMethod -Uri '%BASE_URL%/admin/store/list' -Method Post -Body '{}' ...

# 修正後
Invoke-RestMethod -Uri '%BASE_URL%/admin/stores/options' -Method Get ...
```

**預期結果**：✅ PASS

---

#### 3. ❌ [4] 取得所有縣市(後台) - 500 錯誤

**問題原因**：
- 後台沒有獨立的 `/admin/district/cities` 路由
- District API 只有前台路徑 `/district/cities`
- 但後台 token 可以訪問前台公開 API

**修正方式**：
```powershell
# 修正前
Invoke-RestMethod -Uri '%BASE_URL%/admin/district/cities' ...

# 修正後
Invoke-RestMethod -Uri '%BASE_URL%/district/cities' ...
```

**預期結果**：✅ PASS

---

#### 4. ⚠️ [9] 重設密碼(無效token) - 不應該成功

**問題原因**：
- Controller 返回的是 `Map<String, String>` 而非統一的 `ApiResponse`
- 當 token 無效時，Controller 返回 HTTP 400 + `{"error": "重設連結無效或已過期"}`
- 原測試邏輯檢查 `$r.success`，但實際上會直接拋出 PowerShell 異常

**Service 實作邏輯**（正確的）：
```java
// UserServiceImpl.java
public void resetPassword(String token, String newPassword) {
    UserExample example = new UserExample();
    example.createCriteria()
           .andPasswordResetTokenEqualTo(token)
           .andPasswordResetExpiresGreaterThan(LocalDateTime.now());
    
    List<User> users = userMapper.selectByExample(example);
    
    if (users.isEmpty()) {
        log.warn("❌ 無效或已過期的 token: {}", token);
        throw new IllegalArgumentException("重設連結無效或已過期");  // ← 會觸發 400 錯誤
    }
    // ...
}
```

**修正方式**：
```powershell
# 修正前（錯誤的邏輯）
try { 
    $r = Invoke-RestMethod ...; 
    if ($r.success) { ... } else { ... }  # ← 400 錯誤根本不會進這裡
} catch { 
    Write-Host 'PASS (預期失敗)' 
}

# 修正後（正確的邏輯）
try { 
    $r = Invoke-RestMethod ...;  # ← 如果成功了就是測試失敗
    Write-Host 'FAIL - 應該返回錯誤但成功了' -ForegroundColor Red; 
    exit 1 
} catch { 
    if ($_.Exception.Response.StatusCode -eq 400) {  # ← 檢查 HTTP 狀態碼
        Write-Host 'PASS (預期失敗: 400 Bad Request)' -ForegroundColor Green; 
        exit 0 
    } 
}
```

**預期結果**：✅ PASS (預期失敗: 400 Bad Request)

**⚠️ 注意**：目前測試報告顯示 "FAIL - 應該返回錯誤但成功了"，這表示：
- 可能 Service 邏輯有問題（token 驗證沒有正確執行）
- 或是資料庫中沒有正確設定 password_reset_token/password_reset_expires 欄位

**需要進一步檢查**：
```sql
-- 檢查 user 表結構
DESCRIBE user;

-- 檢查測試用戶的 password_reset_token 欄位
SELECT email, password_reset_token, password_reset_expires 
FROM user 
WHERE email LIKE 'test%@example.com' 
ORDER BY created_at DESC 
LIMIT 5;
```

---

#### 5. ❌ [11] 取得台北市行政區 - 400 錯誤

**問題原因**：
- URL 中的中文字元 "台北市" 沒有正確編碼
- 導致後端無法正確解析 PathVariable

**修正方式**：
```powershell
# 修正前（中文未編碼）
Invoke-RestMethod -Uri '%BASE_URL%/district/districts/台北市' ...

# 修正後（URL 編碼）
Invoke-RestMethod -Uri '%BASE_URL%/district/districts/%E5%8F%B0%E5%8C%97%E5%B8%82' ...
# %E5%8F%B0%E5%8C%97%E5%B8%82 = UTF-8 編碼的 "台北市"
```

**預期結果**：✅ PASS

---

#### 6. ❌ [12] 取得行政區樹狀結構 - 400 錯誤

**問題原因**：
- 需要檢查後端實作，可能回傳格式不符合預期
- 或是 AOP 攔截有問題

**待檢查**：
```java
// DistrictController.java
@GetMapping("/tree")
public ResponseEntity<Map<String, List<District>>> getDistrictTree() {
    Map<String, List<District>> tree = districtService.getDistrictTree();
    return ResponseEntity.ok(tree);  // ← AOP 會自動包裝成 ApiResponse
}
```

**可能的原因**：
1. `districtService.getDistrictTree()` 拋出異常
2. District 資料表為空或資料格式不正確
3. GlobalExceptionHandler 沒有正確處理異常

**預期結果**：需要檢查後端日誌確認錯誤

---

## 修正後的測試腳本

### 修正內容摘要

1. **選單 API**：`/admin/users/menu` → `/admin/menus/accessible`
2. **店家 API**：`/admin/store/list` (POST) → `/admin/stores/options` (GET)
3. **縣市 API**：`/admin/district/cities` → `/district/cities`
4. **重設密碼測試**：改為檢查 HTTP 狀態碼而非 success 欄位
5. **台北市行政區**：使用 URL 編碼 `%E5%8F%B0%E5%8C%97%E5%B8%82`

### 完整修正檔案

修正後的完整測試腳本：`test-all-apis-powershell.bat`

---

## 執行測試

```cmd
cd C:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
test-all-apis-powershell.bat
```

### 預期結果

修正後應該有以下通過率：

```
通過: 12-13 / 15
失敗: 2-3 / 15
```

**仍可能失敗的測試**：
1. [9] 重設密碼 - 需要檢查 User 表的 password_reset_token 欄位設定
2. [12] 行政區樹狀結構 - 需要檢查 District 表資料是否正確初始化

---

## 後續改善建議

### 1. 重設密碼功能需要改進

**問題**：測試顯示無效 token 竟然成功了

**建議修正 ApiAuthController**：
```java
@PostMapping("/reset-password")
public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordReq req) {
    log.info("🔑 重設密碼請求: token={}", req.getToken());
    
    // 驗證密碼確認
    if (!req.getNewPassword().equals(req.getConfirmPassword())) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "兩次輸入的密碼不一致"
        ));
    }
    
    try {
        userService.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok(Map.of(
            "message", "密碼重設成功，請使用新密碼登入"
        ));
    } catch (IllegalArgumentException e) {
        log.warn("❌ 重設密碼失敗: {}", e.getMessage());  // ← 加上日誌
        return ResponseEntity.badRequest().body(Map.of(
            "error", e.getMessage()
        ));
    }
}
```

**檢查資料庫**：
```sql
-- 確認 user 表有這些欄位
ALTER TABLE user 
ADD COLUMN password_reset_token VARCHAR(255) NULL AFTER password,
ADD COLUMN password_reset_expires DATETIME NULL AFTER password_reset_token,
ADD INDEX idx_reset_token (password_reset_token);
```

### 2. 行政區資料初始化

**檢查 District 表是否有資料**：
```sql
SELECT COUNT(*) FROM district;
SELECT * FROM district WHERE city = '台北市' LIMIT 5;
```

**如果沒有資料，需要初始化**：
- 使用 `DataInitializer.java` 或 SQL 腳本初始化台灣縣市資料
- 參考：[台灣郵遞區號資料集](https://data.gov.tw/)

### 3. 統一回應格式

**問題**：部分 API 返回 `Map<String, String>` 而非 `ApiResponse`

**建議**：所有 Controller 統一返回：
```java
// ❌ 不建議
public ResponseEntity<Map<String, String>> someApi() {
    return ResponseEntity.ok(Map.of("message", "success"));
}

// ✅ 建議（讓 AOP 自動包裝）
public ResponseEntity<SomeRes> someApi() {
    return ResponseEntity.ok(someRes);
}
```

**優點**：
- AOP 自動包裝成 `ApiResponse{success, data, error, meta}`
- 前端可統一處理回應格式
- 異常處理由 GlobalExceptionHandler 統一處理

---

## 測試檢查清單

執行測試前確認：

- [x] 後端服務已啟動 (mvn spring-boot:run)
- [x] 資料庫已初始化 (admin@kuji.com 存在)
- [x] Port 8080 可用
- [ ] User 表有 password_reset_token、password_reset_expires 欄位
- [ ] District 表有台灣縣市資料
- [ ] Admin 帳號有關聯的店家（STORE_USER 表）
- [ ] SMTP 郵件設定正確（忘記密碼功能需要）

---

## 結論

本次修正解決了 **4 個明確的路徑錯誤**：
1. ✅ 選單 API 路徑錯誤
2. ✅ 店家 API 路徑錯誤
3. ✅ 縣市 API 路徑錯誤  
4. ✅ 台北市行政區 URL 編碼問題

剩餘 **2 個可能的問題**需要進一步檢查：
1. ⚠️ 重設密碼邏輯（可能資料庫欄位缺失）
2. ⚠️ 行政區樹狀結構（可能資料表為空）

修正後預期通過率：**80-86%** (12-13/15)
