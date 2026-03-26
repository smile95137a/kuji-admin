# 研究：店家帳號管理 (Store Account Management)

**功能**：`013-store-account-mgmt`  
**日期**：2026-03-22  
**狀態**：完成 — 所有未知問題已解決

---

## R-001：JWT Token 失效策略

### 問題
FR-007 和 SC-003 要求停用帳號時，立即使該使用者持有的所有有效 JWT Token 失效。JJWT Token 為無狀態且自我驗證 — 一旦發行，在到期前保持有效，除非伺服器主動拒絕。

### 決策
**使用 Redis 作為 Token 黑名單。**  
當帳號被停用時，該 `adminUserId` 的所有有效 Token 會以等於 Token 剩餘有效時間的 TTL 寫入 Redis。`AdminJwtAuthenticationFilter` 在每次已驗證請求繼續處理前，先查詢 Redis。

### 實作細節
- **Key 格式**：`token_blacklist:{jti}` 或 `token_blacklist:{adminUserId}:{issuedAtEpoch}` — 使用**每位使用者世代計數器** Key（`blacklist_gen:{adminUserId}`）以避免儲存個別 JWT。
- **推薦方式**（避免儲存 Token 值）：在 Redis 中儲存 `blacklist_gen:{adminUserId}` Key。每個 Token 在發行時以 JWT 宣告（`gen`）嵌入世代值。每次請求時，若 Redis `blacklist_gen:{adminUserId}` > Token 的 `gen` 宣告，則拒絕該 Token。
- **停用時失效**：在 Redis 執行 `INCR blacklist_gen:{adminUserId}` — O(1)、即時、影響所有現有 Token。
- **重新啟用後的新 Token**：以當前世代計數器值發行 — 立即被接受。
- **世代 Key 的 Redis TTL**：設為最大 JWT 有效期（刷新 Token 30 天）以自動過期。

### 理由
- 世代計數器方式在 Redis 中**不需要儲存任何 Token**（相較於在 Set 中儲存完整 JWT）。
- 每次已驗證請求的 O(1) 讀取 — 可忽略的負擔。
- 若 Redis 為持久化模式，可在應用程式重啟後存活；若 Redis 無法連線，可降級為預設拒絕。
- 比需要 Token 枚舉的完整封鎖清單更簡單。

### 已考慮的替代方案
| 替代方案 | 拒絕原因 |
|-------------|-----------------|
| 資料庫黑名單表 | 每次請求延遲更高；每次 API 呼叫需要查詢資料庫 |
| 等待 JWT 到期 | 違反 FR-007 / SC-003 強制需求 |
| 短效 Token（5 分鐘） | 所有客戶端每 5 分鐘輪詢；使用者體驗差；非規格要求 |
| 每位使用者 Redis SET（儲存 Token JTI） | 必須枚舉所有有效 JWT；無 Token 登錄冊則不可行 |

---

## R-002：初始密碼產生

### 決策
使用 `SecureRandom`（非 `Math.random()`）產生**加密隨機的 8–12 字元密碼**，保證至少包含一個大寫字母、一個小寫字母及一個數字。

### 實作細節
```java
// Algorithm:
// 1. Randomly pick length L in [8, 12]
// 2. Guarantee pool: 1 uppercase + 1 lowercase + 1 digit
// 3. Fill remaining L-3 chars from full charset (upper + lower + digit)
// 4. Shuffle with SecureRandom
private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
private static final String DIGIT = "0123456789";
```

### 理由
- 安全敏感的密碼產生需要 `SecureRandom`（OWASP 要求）。
- 保證字元類別包含，符合 FR-002（「含大小寫及數字」）。
- 長度範圍 8–12，符合 FR-002。

### 已考慮的替代方案
| 替代方案 | 拒絕原因 |
|-------------|-----------------|
| UUID 作為密碼 | 不符合規格；不滿足字元集要求 |
| Apache Commons RandomStringUtils | 增加依賴；`SecureRandom` 已足夠 |
| 固定長度 8 | 規格允許最多 12；可變長度增加亂度 |

---

## R-003：初始密碼的電子郵件寄送

### 決策
**擴充現有 `EmailService`**，新增 `sendInitialPasswordEmail(String to, String displayName, String initialPassword)` 方法。

### 實作細節
- `EmailService` 已使用 Gmail SMTP + Thymeleaf + `@Async`。
- 新增 Thymeleaf 範本 `initial-password-email.html`。
- 範本變數：`displayName`、`initialPassword`、`loginUrl`。
- 方法標註 `@Async` — 帳號建立 API 不等待電子郵件寄送完成即回傳 `201 Created`。
- 若電子郵件寄送失敗，記錄於 `EmailLog`（現有機制）；管理員可手動重新寄送。

### 理由
- 重用 `EmailService` 避免引入第二個電子郵件函式庫或 SMTP 設定。
- `@Async` 電子郵件符合 SC-001（帳號建立 < 3 分鐘）— API 在 SMTP 完成前即回應。
- `EmailLog` 為電子郵件寄送嘗試提供稽核軌跡。

### 已考慮的替代方案
| 替代方案 | 拒絕原因 |
|-------------|-----------------|
| 在交易中同步寄送電子郵件 | 阻塞 API 回應；SMTP 延遲時有交易逾時風險 |
| 獨立電子郵件微服務 | 當前規模過度設計 |
| 在 API 回應中回傳密碼 | 安全風險；密碼必須僅寄送至帳號所有者的電子郵件 |

---

## R-004：BCrypt 密碼雜湊

### 決策
**使用 `SecurityConfig` 中現有的 `BCryptPasswordEncoder` Bean。**

### 實作細節
- `BCryptPasswordEncoder` 已在 `SecurityConfig` 中宣告為 `@Bean`。
- `AdminAccountServiceImpl` 注入 `PasswordEncoder`，在儲存前呼叫 `passwordEncoder.encode(initialPassword)`。
- 強制變更密碼流程：在 `POST /admin/auth/first-login/change-password` 時，以 `passwordEncoder.matches()` 驗證舊密碼，編碼新密碼，更新 `AdminUser`。
- `AdminAuthController.firstLoginChangePassword()` 端點已存在 — 本功能僅需確保 `force_change_password` 旗標邏輯正確執行。

### 理由
- 重用現有 `BCryptPasswordEncoder` — 無需額外依賴。
- BCrypt 是 OWASP 推薦的標準密碼儲存方式。

---

## R-005：StoreOwner 與 StoreEditor 綁定模型

### 決策
- **StoreOwner**：`Store.ownerId` 欄位（FK → `AdminUser.id`）代表 1:1 擁有者綁定。建立 StoreOwner 帳號時，設定 `store.ownerId = newAdminUser.id` 並建立 `StoreUser(storeId, adminUserId, roleType="STORE_OWNER")` 記錄。
- **StoreEditor**：僅建立 `StoreUser(storeId, adminUserId, roleType="STORE_EDITOR")` 記錄。不更新 `Store.ownerId`。
- 兩項操作必須在單一 `@Transactional` 方法中（FR-012）。

### 理由
- 帶有 `roleType` 的 `StoreUser` 是兩種角色的 M:N 連結表 — 已由 `AdminJwtAuthenticationFilter` 使用，用於將 `storeIds` 載入 JWT context。
- `Store.ownerId` 提供直接的 1:1 擁有權語意連結，無需 join。
- 無需變更資料庫結構 — 兩個表和列舉均已存在。

### 已考慮的替代方案
| 替代方案 | 拒絕原因 |
|-------------|-----------------|
| 僅用 StoreUser 表示擁有者 | Store.ownerId 已存在且被使用；更改需要更廣泛的重構 |
| 移除 Store.ownerId | 對現有店家管理邏輯是破壞性變更 |

---

## R-006：帶分頁的帳號列表

### 決策
使用 **MyBatis Example 模式**（與現有程式碼一致）進行篩選查詢。以 `AdminUserExample` 建立條件，依 status、roleType（透過 StoreUser join）及 email/displayName 關鍵字搜尋。

### 實作細節
- 使用 `AdminUserMapper.selectByExample()` 搭配 `AdminUserExample.createCriteria()`。
- `roleType` 篩選：在 `AdminUserMapper` 中使用自訂 `@Select` 方法（MyBatis 註解式自訂 SQL）進行子查詢或 join。
- 分頁：使用現有 `PageHelper` 或手動 `LIMIT/OFFSET`。

### 理由
- 與其他 Controller（AdminStoreController、AdminOrderController）處理分頁的方式一致。
- 避免在程式碼中引入新的查詢模式。

---

## 摘要表

| 主題 | 決策 | 信心度 |
|-------|----------|------------|
| Token 失效 | Redis 世代計數器（`blacklist_gen:{userId}`） | 高 |
| 密碼產生 | `SecureRandom` + 保證字元集組合 | 高 |
| 電子郵件寄送 | 擴充現有 `EmailService` 新增方法 | 高 |
| 密碼雜湊 | 現有 `BCryptPasswordEncoder` Bean | 高 |
| 店家綁定 | `Store.ownerId`（擁有者）+ `StoreUser`（兩種角色） | 高 |
| 帳號列表 | MyBatis Example 模式 + 角色篩選自訂 SQL | 高 |

所有技術背景中的「待釐清」事項均已解決。可進行第 1 階段。
