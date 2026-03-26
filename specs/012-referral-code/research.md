# 研究分析：推薦碼 (Referral Code)

**Feature**: 012-referral-code
**Date**: 2026-03-22
**Status**: 完成 — 所有待澄清事項已解決

---

## 1. 推薦碼格式

**決策**：大寫英數字字串，8 個字元，由伺服器端透過 `UUID.randomUUID().toString().replace("-","").substring(0,8).toUpperCase()` 生成。

**理由**：8 字元的代碼在人類可讀性（易於輸入）與碰撞抵抗性（base-36 下約 2.8 兆種組合）之間取得平衡。全大寫消除了歧義（0/O、1/l）。伺服器端生成可在 DB 層（`code` 欄位的 UNIQUE 索引）強制確保唯一性。

**考慮過的替代方案**：
- 自訂前綴（例如 `STORE-XXXX`）：否決——前綴會暴露店家身份，增加解析複雜度
- 6 字元代碼：否決——大規模使用時（>10k 個代碼）碰撞概率過高
- 使用者自訂代碼：否決——規格 FR-002 規定僅管理員可建立代碼；開放輸入容易引發不當內容或衝突

---

## 2. 一次性綁定強制執行

**決策**：在 Service 層的 `useCode()` 中強制執行。若 `userId` 對應的 `ReferralRecord` 已存在，則以業務例外（`REFERRAL_ALREADY_USED`）拒絕。此檢查是在 `referral_record` 表 `(user_id)` DB UNIQUE 約束之外的額外保護。

**理由**：雙重防護（Service + DB）可防止競態條件。Service 層檢查提供使用者友善的錯誤訊息；DB 約束則作為並發請求的安全底線。

**考慮過的替代方案**：
- 僅使用 DB 約束：否決——DB 錯誤訊息不夠友善，且因驅動程式而異
- 僅使用 Service 層檢查：否決——容易受到並發註冊請求的攻擊

---

## 3. 自我推薦防護

**決策**：在 `useCode()` 中，取得推薦碼關聯的店家，查詢 `StoreUser` 確認註冊使用者的 email 是否與該店家任何 `AdminUser.email` 相符。若相符，則以 `SELF_REFERRAL_NOT_ALLOWED` 拒絕。

**理由**：規格邊緣案例明確指出「當註冊 email 與店家擁有者 email 相符時，系統應防止自我推薦」。此檢查代價輕微（兩次索引查詢），不影響效能目標 SC-001。

**考慮過的替代方案**：
- 跳過自我推薦檢查：否決——規格明確要求
- 僅檢查 ownerId：否決——店家可有多名編輯者；規格說的是「店家擁有者 email」
- 非同步驗證（註冊後）：否決——規格要求的是防止，而非事後偵測

---

## 4. 店家停用攔截

**決策**：在 `validateCode()` 和 `useCode()` 中，取得 `ReferralCode` 後，同時依 `storeId` 查詢 `Store`，確認 `store.status == "ACTIVE"`。若店家為非活躍狀態，則回傳驗證失敗（`STORE_INACTIVE`）。

**理由**：FR-007 明確要求停用的店家應封鎖其推薦碼。現有的 `ReferralCode.isActive` 標記僅涵蓋管理員停用的代碼；店家層級的停用屬於獨立的控制軸。

**考慮過的替代方案**：
- 店家停用時自動停用所有代碼：否決——操作複雜度高，需要級聯觸發器
- 僅檢查 `ReferralCode.isActive`：否決——違反 FR-007

---

## 5. 統計端點設計

**決策**：`GET /admin/referral-stats` 回傳每家店的統計物件清單。每個物件包含：`storeId`、`storeName`、`totalReferrals`、`activeCodeCount`，以及依 `DATE(referred_at)` 每日聚合的 `timeline` 陣列（格式為 `{ date, count }`）。查詢透過 `ReferralCodeRepository.selectStatsByStore()` 中的自訂 SQL 實作，使用 MyBatis `@Select` 或 XML 片段。

**理由**：規格要求每家店的「計數與時間軸」（FR-008）。單一聚合端點比對每家店發 N+1 次請求更有效率。每日粒度符合典型促銷活動儀表板的 UX 需求。

**考慮過的替代方案**：
- 個別 `/stats/{storeId}` 端點：合理，但需要客戶端多次呼叫；清單端點更適合儀表板
- 每小時時間軸：否決——對推薦報表而言粒度過細；每日符合程式碼庫中其他分析功能的慣例

---

## 6. 驗證推薦碼：POST vs GET

**決策**：採用 `POST /api/auth/validate-referral`，請求體為 `{ "code": "ABC123" }`，而非查詢參數。

**理由**：現有的 GET 端點 `/auth/referral-code/validate/{code}` 已提供代碼檢查功能。新的 POST 端點服務於不同的使用情境：在註冊表單提交時進行內嵌驗證，其中代碼屬於請求體的一部分，符合具有副作用的表單步驟驗證的 REST 慣例（在提交註冊前的預先確認）。POST 也避免推薦碼以 URL 路徑片段的形式出現在伺服器存取日誌中。

**考慮過的替代方案**：
- 重用現有 GET 端點：否決——路徑前綴不同（`/api/auth/` vs `/auth/`），註冊流程需要不同的回應格式
- 查詢參數：否決——URL 中的代碼會被記錄在日誌；POST 請求體更安全

---

## 7. MyBatis 統計查詢方式

**決策**：在 `ReferralCodeRepository` 中使用自訂 `@Select` 標注實作統計查詢，包含多表 JOIN + GROUP BY。每一列回傳 `Map<String, Object>`，再於 Service 層聚合成 `ReferralStatsRes` DTO。

**理由**：現有程式碼庫同時使用 XML mapper（用於自動生成的 CRUD）和 Repository 類別中的 `@Select` 標注（用於自訂查詢）。統計查詢複雜度足以需要明確的 SQL，但不需要完整的 XML resultMap。

**SQL 草稿**：
```sql
SELECT s.id AS storeId, s.store_name AS storeName,
       COUNT(rr.id) AS totalReferrals,
       SUM(CASE WHEN rc.is_active = 1 THEN 1 ELSE 0 END) AS activeCodeCount,
       DATE(rr.referred_at) AS referralDate,
       COUNT(rr.id) AS dailyCount
FROM store s
LEFT JOIN referral_code rc ON rc.store_id = s.id
LEFT JOIN referral_record rr ON rr.referral_code_id = rc.id
GROUP BY s.id, s.store_name, DATE(rr.referred_at)
ORDER BY s.id, referralDate
```

**考慮過的替代方案**：
- XML resultMap 含巢狀集合：合理，但對 v1.0 而言過度設計
- 每家店個別查詢：否決——N+1 效能問題

---

## 8. 整合至註冊流程

**決策**：`UserServiceImpl.register()` 中推薦碼處理邏輯維持以 try/catch 包裝。對於 `REFERRAL_ALREADY_USED` 或 `SELF_REFERRAL_NOT_ALLOWED`，例外**重新拋出**（註冊失敗）。對於 `STORE_INACTIVE` 或 `CODE_NOT_FOUND`，記錄警告並繼續（註冊成功，但不帶推薦記錄）。

**理由**：規格 Story 2 情境 2 指出，當推薦碼無效時「系統顯示錯誤且不完成註冊」。然而「無效」的範疇必須明確界定：不存在的代碼應封鎖（使用者輸入錯誤應獲得明確回饋），而使用者輸入代碼後店家因競態條件被停用屬於邊緣案例，不應封鎖註冊。此區分使系統在強制資料完整性的同時更加使用者友善。

**驗證矩陣**：
| 情境 | 註冊結果 |
|-----------|---------------------|
| 代碼不存在 | 失敗，回傳 400 INVALID_REFERRAL_CODE |
| 代碼 isActive = false | 失敗，回傳 400 REFERRAL_CODE_DISABLED |
| 店家非活躍 | 失敗，回傳 400 STORE_INACTIVE |
| 已使用推薦（用戶已有紀錄） | 失敗，回傳 400 REFERRAL_ALREADY_USED |
| 偵測到自我推薦 | 失敗，回傳 400 SELF_REFERRAL_NOT_ALLOWED |
| 未提供代碼 | 正常完成註冊 |
| 代碼有效 | 完成註冊 + 建立 ReferralRecord |

**考慮過的替代方案**：
- 所有失敗均為軟性（記錄並繼續）：否決——規格 Story 2 情境 2 要求無效代碼時硬性失敗
- 所有失敗均為硬性：否決——對於並發店家停用等邊緣案例過於嚴格

---

## 9. 資料庫結構評估

**決策**：沿用現有的 `referral_code` 和 `referral_record` 資料表。新增一個缺少的索引：`UNIQUE INDEX idx_referral_record_user_id ON referral_record(user_id)`，在 DB 層強制每用戶僅一筆推薦紀錄。

**理由**：現有結構（來自 `fix-referral-tables.sql`）已有 `code` 唯一性與 `store_id` 索引。每用戶一筆的約束缺失，而不可變性保證（FR-005、FR-006）需要此約束。

**遷移腳本**：
```sql
ALTER TABLE referral_record
  ADD UNIQUE INDEX idx_referral_record_user_id (user_id);
```

**考慮過的替代方案**：
- 完整重建結構：否決——現有資料將遺失；遷移方式更安全
- 僅依賴 Service 層檢查：否決——單獨使用不夠充分（競態條件問題）
