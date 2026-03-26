# API 文件重大更新報告（2026-02-07）

## 📋 更新摘要

根據您的要求：
1. ✅ **完全移除舊欄位** `ticketNumber` 和 `drawCount`（僅保留 `count` 和 `ticket`）
2. ✅ **新增完整業務流程指南**，詳細說明每個 API 的使用時機、前置條件、完整範例
3. ✅ **驗證邏輯已確認**：`count` 與 `ticket` 長度一致性檢查（後端 Line 107）

---

## 🚨 破壞性變更

### 1. 移除舊欄位

**影響 API**: `POST /api/lottery/draw/{lotteryId}/draw`

**移除欄位**:
- ❌ `ticketNumber`（票券編號）
- ❌ `drawCount`（抽獎次數）

**新欄位**:
- ✅ `count`（必填）：抽獎次數，範圍 1-10
- ✅ `ticket`（選填）：票券 UUID 陣列

**遷移對照表**:

| 舊格式（已移除） | 新格式（必須使用） |
|----------------|------------------|
| `{ "ticketNumber": 13, "drawCount": 1 }` | `{ "count": 1, "ticket": ["uuid"] }` |
| `{ "drawCount": 3 }` | `{ "count": 3 }` |
| `{ "ticketNumber": 13 }` | `{ "count": 1, "ticket": ["uuid"] }` |

---

### 2. 後端驗證邏輯

**位置**: `LotteryDrawController.java` Line 107

```java
// 驗證：長度必須等於 count
if (tickets.size() != count) {
    return ResponseEntity.badRequest().body("ticket 列表的長度必須等於 count");
}
```

**其他驗證**:
- Line 111：檢查重複 UUID
- Line 117：檢查 UUID 格式有效性

---

## 📖 新增內容

### 1. 完整業務流程指南（3.0 章節）

**新增內容**:
- 🎯 Mermaid 流程圖：從使用者進入商品頁到完成抽獎的完整流程
- 📋 前端實作步驟：6 個詳細步驟，包含完整程式碼範例
- ⚠️ 前置條件檢查清單：登入檢查、餘額檢查、開套者保護檢查等
- 🔄 快速抽獎模式：隨機抽獎的實作方式
- ❌ 常見錯誤與解決方案：3 種常見錯誤的處理方式
- 📊 回應欄位完整說明：所有欄位的詳細說明與一番賞/刮刮樂的差異

---

### 2. 抽獎 API 使用指南（3.2 之前）

**新增小節**:

#### 🎯 什麼時候使用這個 API？
- 使用場景清單
- 前置條件檢查清單（完整 JavaScript 範例）

#### 📋 完整前端實作範例
- **步驟 1**：初始化抽獎頁面（含 React 範例）
- **步驟 2**：處理票券選擇（含狀態管理）
- **步驟 3**：執行抽獎（含錯誤處理）
- **步驟 4**：顯示抽獎結果（含動畫處理）
- **步驟 5**：更新前端狀態（含多個狀態更新）
- **步驟 6**：錯誤處理（含 HTTP 狀態碼對應）

#### 🔄 快速抽獎模式
- 隨機抽獎實作方式
- 一鍵快速抽 1/5 張的 UI 範例

#### ⚠️ 常見錯誤與解決方案
- 錯誤 1：長度不一致
- 錯誤 2：包含重複 UUID
- 錯誤 3：UUID 格式錯誤

#### 📊 回應欄位完整說明
- 所有欄位的型別、說明、一番賞/刮刮樂差異對照表

---

## 📂 文件變更位置

### 已更新章節

**檔案**: `FRONTEND_API_COMPLETE_REFERENCE.md`

**新增/修改**:
1. **標題區** - 更新日期為 2026-02-07
2. **最新變更 (2026-02-07)** - 新增破壞性變更警告
3. **3.0 完整抽獎流程指南** - 全新章節（約 400 行）
4. **3.2 執行抽獎** - 保留原有內容，之前已完整
5. **3.2 之前** - 新增「抽獎 API 完整使用指南」小節（約 500 行）

**總計新增**:
- 約 900 行詳細業務流程說明
- 15+ 個完整程式碼範例
- 1 個 Mermaid 流程圖
- 3 個錯誤解決方案
- 1 個欄位對照表

---

## 🎯 前端工程師行動清單

### P0（立即處理）

- [ ] **移除舊格式**：搜尋專案中所有 `ticketNumber` 和 `drawCount` 的使用
- [ ] **更新 API 呼叫**：改用 `{ count, ticket }` 格式
- [ ] **驗證長度一致性**：確保前端也檢查 `count === ticket.length`
- [ ] **測試錯誤處理**：測試 3 種驗證錯誤的顯示

### P1（本週完成）

- [ ] **實作前置檢查**：參考文件 3.0 章節實作 `canUserDraw()`
- [ ] **重構抽獎流程**：按照文件的 6 個步驟重構現有程式碼
- [ ] **新增錯誤處理**：實作文件中的 `handleDrawError()`
- [ ] **測試批次抽獎**：測試一次選 2/5/10 張票券

### P2（下週完成）

- [ ] **新增快速抽獎模式**：實作「快速抽 1 張」、「快速抽 5 張」按鈕
- [ ] **優化 UI/UX**：根據文件建議優化籤位選擇介面
- [ ] **新增狀態管理**：實作 `refreshAfterDraw()` 自動更新所有狀態
- [ ] **完善動畫**：區分一番賞/刮刮樂的不同動畫效果

---

## 📚 關鍵知識點

### 1. 為什麼移除 `ticketNumber`？

**原因**:
- 前端已經有 ticket UUID（從 3.1 API 取得）
- UUID 本身就包含票券編號資訊（後端可查詢）
- 減少欄位冗餘，降低出錯機率
- 統一使用 UUID 作為唯一識別

**後端邏輯**:
```java
// 前端傳入
{ "count": 1, "ticket": ["550e8400-e29b-41d4-a716-446655440000"] }

// 後端透過 UUID 查詢 LotteryTicket
LotteryTicket ticket = ticketMapper.selectByPrimaryKey(ticketId);
int ticketNumber = ticket.getTicketNumber();  // 取得票券編號
```

---

### 2. 驗證邏輯：為什麼要檢查長度？

**目的**:
- 防止前端錯誤（例如：count=3 但只傳 2 個 UUID）
- 防止惡意請求（例如：count=1 但傳 10 個 UUID）
- 確保業務邏輯一致性（抽幾次就傳幾個 UUID）

**前端檢查**（建議）:
```javascript
const validateDrawRequest = (count, tickets) => {
  if (tickets.length !== count) {
    throw new Error(`count (${count}) 與 ticket 長度 (${tickets.length}) 不一致`);
  }
  
  const uniqueTickets = new Set(tickets);
  if (uniqueTickets.size !== tickets.length) {
    throw new Error('ticket 包含重複的 UUID');
  }
  
  return true;
};
```

**後端檢查**（已實作）:
```java
// Line 107
if (tickets.size() != count) {
    return ResponseEntity.badRequest().body("ticket 列表的長度必須等於 count");
}

// Line 111
long distinct = tickets.stream().distinct().count();
if (distinct != tickets.size()) {
    return ResponseEntity.badRequest().body("ticket 列表不可包含重複項目");
}
```

---

### 3. 業務流程：完整的抽獎週期

```
1. 進入商品頁
   ↓
2. 檢查登入狀態
   ↓
3. 檢查錢包餘額
   ↓
4. 呼叫 3.1 取得籤位列表
   ↓
5. 檢查開套者保護時間
   ↓
6. 使用者選擇票券（或快速模式）
   ↓
7. 前端驗證（長度、重複、格式）
   ↓
8. 呼叫 3.2 執行抽獎
   ↓
9. 後端驗證（長度、重複、格式、狀態）
   ↓
10. 扣款 & 抽獎 & 加入賞品盒
    ↓
11. 回傳結果陣列
    ↓
12. 前端顯示動畫與結果
    ↓
13. 更新籤位、錢包、商品狀態
    ↓
14. 提示使用者並清空選擇
```

---

## 🔍 文件索引

### 快速查詢表

| 內容 | 位置 | 說明 |
|------|------|------|
| 破壞性變更 | 標題區 → 最新變更 (2026-02-07) | 舊欄位移除說明 |
| 業務流程圖 | 3.0 完整抽獎流程指南 | Mermaid 流程圖 |
| 前置條件檢查 | 3.2 之前 → 什麼時候使用這個 API | `canUserDraw()` 範例 |
| 完整實作範例 | 3.2 之前 → 完整前端實作範例 | 6 個步驟詳細說明 |
| 錯誤處理 | 3.2 之前 → 常見錯誤與解決方案 | 3 種錯誤的處理方式 |
| 欄位說明 | 3.2 之前 → 回應欄位完整說明 | 所有欄位的對照表 |
| API 請求格式 | 3.2 執行抽獎 | 兩種模式的詳細說明 |
| API 回應格式 | 3.2 執行抽獎 | 成功/失敗回應範例 |

---

## ✅ 驗證清單（後端開發者）

- [x] 移除 `DrawRequest` 中的 `ticketNumber` 和 `drawCount` 欄位
- [x] 確認 Line 107 驗證邏輯正常運作
- [x] 確認 Line 111 重複檢查正常運作
- [x] 確認 Line 117 UUID 格式檢查正常運作
- [x] 更新 `FRONTEND_API_COMPLETE_REFERENCE.md`
- [x] 新增完整業務流程指南
- [x] 新增前端實作範例（6 個步驟）
- [x] 新增錯誤處理指南
- [x] 新增欄位對照表

---

## 📞 聯絡資訊

如有問題，請參考：
- 📄 完整 API 文檔：`FRONTEND_API_COMPLETE_REFERENCE.md`
- 📄 本次變更報告：`API_DOCUMENTATION_UPDATE_2026-02-07.md`
- 📄 上次變更報告：`API_DOCUMENTATION_UPDATE_2026-02-05.md`

---

**文件版本**: 1.2.0  
**更新日期**: 2026-02-07  
**更新人員**: GitHub Copilot  
**變更類型**: 破壞性變更 + 新增業務流程指南
