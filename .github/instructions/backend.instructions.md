---
description: "Use when working on backend development, service layer, controller layer, or data access patterns. Covers layered architecture, security, error handling, and transaction management."
applyTo: ["**/controller/**", "**/service/**", "**/mapper/**", "**/config/**"]
---

# 後端開發檢查清單與指南

## 1. 通用後端設計原則

- 明確區分**前端**與**後端**
- 前端僅使用 **OAuth2 (Google)**
- 實現 **JWT (存取 + 更新)** 驗證
- 統一 **API 回應格式**
- 強制執行 **RBAC (基於角色的存取控制)**
- 使用**全域例外處理器**
- 避免直接向前端暴露錯誤細節
- 確保**點數、庫存、抽獎**的可追溯性

## 2. 後端模組分層

### Controller (處理 HTTP)
- 接收請求、驗證請求（`@Valid`）、呼叫 Service、返回 `ResponseEntity`
- **避免**: 寫業務邏輯、直接訪問資料庫

### Service (核心業務邏輯)
- 抽獎流程、點數扣除、庫存減少、權限檢查、交易一致性（`@Transactional`）
- **99% 的錯誤發生在此層**

### Repository / DAO (資料庫訪問)
- 單一職責、不混合業務邏輯、查詢必須有明確目的

### 安全層
- JWT 過濾器、OAuth2 成功處理器、RBAC 權限檢查器、CORS/CSRF 配置

## 3. 驗證與安全

### 使用者來源
| 情境 | 規則 |
|------|------|
| Google 登錄帳號 | ❌ 不可設置密碼 |
| 本地註冊 | ❌ 不可使用 Google 登錄 |
| 重複電子郵件 | 必須檢查 `provider` |

### JWT 機制
- **存取 Token**: 短效（15–30 分鐘），每次 API 呼叫驗證
- **更新 Token**: 長效（7–30 天），存儲於資料庫，可撤銷

## 4. 請求 / 回應設計

統一回應格式（AOP 自動包裝，不要手動建立 ApiResponse）：
```json
{ "success": true, "code": "OK", "message": "success", "data": {} }
```

## 5. 全域例外處理

使用 `@RestControllerAdvice` 處理：ValidationException、AuthenticationException、AccessDeniedException、BusinessException、Exception
- ❌ 不返回堆疊追蹤、不暴露資料庫錯誤
- ✔ 返回自定義錯誤代碼

## 6. 點數 / 庫存 / 抽獎

### 抽獎流程（事務性）
```java
@Transactional
public DrawResult draw(...) {
    // 1. 檢查庫存 → 2. 檢查點數 → 3. 扣除點數
    // 4. 扣除庫存 → 5. 寫入抽獎日誌
}
```

### 防止問題
- 重複扣除、超賣、並發抽獎請求、回滾不完整

### 庫存鎖定
- 資料庫行鎖、樂觀鎖（版本控制）、Redis 鎖（進階）

## 7. 關鍵考量

- ✅ 幂等性：使用 `request_id` 防止重複請求
- ✅ 審計日誌：追蹤管理員操作、記錄財務與庫存變更
- ✅ 限流：防止暴力破解登錄、減輕 API 濫用
- ✅ 軟刪除：使用 `deleted_at` 代替硬刪除
- ✅ 時區/精度：金額使用 `BigDecimal`，時間戳使用 UTC
