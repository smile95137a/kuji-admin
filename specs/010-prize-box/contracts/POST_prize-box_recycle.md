# API 契約： POST /api/prize-box/recycle

**功能**：獎品盒 (010-prize-box)  
**端點**：`POST /api/prize-box/recycle`  
**用途**：回收獎品盒中的獎品，換取 Bonus 點數（不可撤銷）

---

## 概覽

玩家選取一或多件可回收獎品，確認後永久移除並獲得對應 Bonus 點數。此操作不可逆，需前台顯示確認對話框。

---

## 驗證

**必要性**：✅ JWT Bearer Token  
**標頭**：`Authorization: Bearer <token>`

---

## 請求

```http
POST /api/prize-box/recycle
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

### 請求 Body

```json
{
  "prizeBoxIds": ["prizebox-uuid-003", "prizebox-uuid-004"]
}
```

### 請求 Fields

| 欄位 | 型別 | 必填 | 說明 |
|-------|------|----------|-------------|
| `prizeBoxIds` | Array\<String\> | ✅ | 要回收的獎品盒 ID 列表（至少 1 筆）|

---

## 回應

### 成功 — 200 OK

```json
{
  "totalBonus": 250,
  "recycledCount": 2
}
```

> **現有實作**: 回傳 `void` (204)。建議改為回傳 `totalBonus` 與 `recycledCount`，方便前台顯示回收結果提示。

| 欄位 | 型別 | 說明 |
|-------|------|-------------|
| `totalBonus` | Long | 本次回收共獲得的 Bonus 點數 |
| `recycledCount` | Integer | 本次回收件數 |

### 驗證錯誤 — 400

```json
{
  "code": 400,
  "message": "請選擇要回收的獎品"
}
```

### 業務邏輯錯誤 — 400

```json
{
  "code": 400,
  "message": "此獎品不可回收：prizebox-uuid-003"
}
```

```json
{
  "code": 400,
  "message": "獎品已處理：prizebox-uuid-004"
}
```

### 未授權 — 401

```json
{
  "code": 401,
  "message": "未授權，請重新登入"
}
```

---

## 業務規則

1. **Owner Check**: 每件 `prizeBoxId` 的 `userId` 必須等於當前登入玩家
2. **Status Check**: 每件獎品狀態必須為 `IN_BOX`（否則 400）
3. **Recyclable Check**: `recycleBonus > 0`（否則 400 `此獎品不可回收`）
4. **Irreversible**: 回收後無法撤銷，無 undo/cancel 端點（FR-006, US3 AC2）
5. **Atomicity**: 整個操作在單一 `@Transactional` 中執行
6. **Bonus Credit**: 所有獎品 `recycleBonus` 加總後一次性存入玩家 Bonus 錢包
7. **Status Update**: 成功後 `PrizeBox.status → RECYCLED`，`recycledAt` 設為當前時間

---

## Bonus 計算

```
totalBonus = sum(prizeBox.recycleBonus) for each selected prize

WalletTransaction:
  userId: {currentUserId}
  transactionType: RECYCLE
  coinType: BONUS
  amount: totalBonus
  description: "回收 {N} 個獎品"
```

---

## 實作流程

```
1. SecurityUtils.getCurrentUserId()
2. 驗證 prizeBoxIds 非空
3. totalBonus = 0
4. for each prizeBoxId:
   a. SELECT PrizeBox WHERE id = ?
   b. 驗證 userId == currentUser
   c. 驗證 status == IN_BOX
   d. 驗證 recycleBonus > 0（否則拋出 BusinessException）
   e. UPDATE prize_box SET status='RECYCLED', recycled_at=NOW()
   f. totalBonus += recycleBonus
5. if totalBonus > 0:
   WalletService.addBonus(userId, totalBonus, RECYCLE, null, "回收 N 個獎品")
6. 回傳 { totalBonus, recycledCount }
```

---

## 測試情境

| # | 情境 | 預期結果 |
|---|----------|----------|
| 1 | 回收 1 件 recycleBonus=50 | 200, totalBonus=50, Bonus 增加 50 |
| 2 | 回收 2 件 bonus=50+100 | 200, totalBonus=150 |
| 3 | 包含 recycleBonus=0 的獎品 | 400 `此獎品不可回收` |
| 4 | 包含已回收獎品 | 400 `獎品已處理` |
| 5 | 包含已出貨獎品 | 400 `獎品已處理` |
| 6 | 操作他人獎品 | 400 `無權操作此獎品` |
| 7 | prizeBoxIds 為空 | 400 validation error |
| 8 | 回收後嘗試再次回收同一 ID | 400 `獎品已處理` |
| 9 | Bonus 已到帳（SC-003 ≤ 5 秒） | Wallet balance updated within 5s |
