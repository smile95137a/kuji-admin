# API 文件更新報告（2026-02-05）

## 📋 概要

根據您的要求 **"請未來每一次修改 API 變動 都必須幫我調整一次 FRONTEND_API_COMPLETE_REFERENCE"**，已完成以下文件更新：

- ✅ 更新 `FRONTEND_API_COMPLETE_REFERENCE.md` 
- ✅ 新增抽獎 API 破壞性變更說明
- ✅ 新增頭像上傳 API 文檔（4.3、4.4）
- ✅ 新增商品熱度 API 文檔（2.5）
- ✅ 新增前端遷移指南

---

## 🚨 破壞性變更：前端必讀

### 問題描述

您回報的問題：
```javascript
// 前端送出（舊格式）
{
  "ticketNumber": 19,
  "drawCount": 1
}

// 後端回應
{
  "success": true,
  "data": ???  // 前端看到空或格式不對
}
```

### 根本原因

**2026-02-05 的 API 變更將抽獎回應格式從「單一物件」改為「陣列」**：

| 舊版（2026-02-04 前） | 新版（2026-02-05 起） |
|---------------------|---------------------|
| `data: { ticketNumber: 1, ... }` | `data: [{ ticketNumber: 1, ... }]` |
| 回傳單一抽獎結果物件 | **永遠回傳陣列**，即使只抽一次 |

### 為什麼會這樣改？

因為新增了「批次抽獎」功能（一次抽多張票），為了統一回應格式：

```javascript
// 新功能：批次抽 3 張票
POST /api/lottery/draw/{id}/draw
{
  "count": 3,
  "ticket": ["uuid1", "uuid2", "uuid3"]
}

// 回應（陣列）
{
  "data": [
    { ticketNumber: 1, prizeName: "A賞" },
    { ticketNumber: 5, prizeName: "C賞" },
    { ticketNumber: 9, prizeName: "B賞" }
  ]
}
```

為了保持一致性，**所有抽獎請求（包括單次）都改為回傳陣列**。

---

## 🔧 前端修正方案

### 方案 1：立即修正前端（推薦）

```javascript
// ❌ 舊版前端（會出錯）
const response = await axios.post('/api/lottery/draw/{id}/draw', {
  ticketNumber: 19,
  drawCount: 1
});

const result = response.data.data;  // 現在是陣列，不是物件
console.log(result.prizeName);      // ❌ undefined

// ✅ 新版前端（正確）
const response = await axios.post('/api/lottery/draw/{id}/draw', {
  count: 1,
  ticket: [ticketId]  // 使用 UUID
});

const results = response.data.data;  // 陣列
if (results.length > 0) {
  console.log(results[0].prizeName);  // ✅ 正確取得
}
```

### 方案 2：相容性處理（過渡期）

```javascript
// 同時支援新舊格式
const response = await axios.post('/api/lottery/draw/{id}/draw', {
  ticketNumber: 19,  // 舊格式
  drawCount: 1
});

const data = response.data.data;
const result = Array.isArray(data) ? data[0] : data;  // 自動判斷
console.log(result.prizeName);
```

### 方案 3：完全遷移到新格式（最佳）

```javascript
// 步驟 1：取得票券 UUID
const ticketsResponse = await axios.get(`/api/lottery/draw/${lotteryId}/tickets`);
const availableTicket = ticketsResponse.data.data.tickets.find(t => t.status === 'AVAILABLE');

// 步驟 2：使用新格式抽獎
const drawResponse = await axios.post(`/api/lottery/draw/${lotteryId}/draw`, {
  count: 1,
  ticket: [availableTicket.id]  // 使用 UUID
});

const results = drawResponse.data.data;  // 陣列
console.log('中獎:', results[0].prizeName);
```

---

## 📝 完整 API 變更清單

### 1. 抽獎 API（破壞性變更）

**API**: `POST /api/lottery/draw/{lotteryId}/draw`

**舊版請求格式（仍可用，但回應為陣列）**:
```json
{
  "ticketNumber": 19,
  "drawCount": 1
}
```

**新版請求格式（推薦）**:
```json
{
  "count": 3,
  "ticket": ["550e8400-e29b-41d4-a716-446655440000", "..."]
}
```

**驗證規則**:
- ✅ `ticket` 陣列長度必須等於 `count`
- ✅ 不可包含重複的 UUID
- ✅ 所有項目必須為有效的 UUID 格式
- ❌ `ticketNumber` + `drawCount > 1` 會返回 400 錯誤

**回應格式（統一）**:
```json
{
  "success": true,
  "data": [
    {
      "success": true,
      "ticketNumber": 19,
      "prizeLevel": "A",
      "prizeName": "公仔",
      "prizeImageUrl": "https://...",
      "isGrandPrize": true,
      "triggeredFreeDraw": true,
      "refundAmount": 400,
      "message": "🎉 恭喜中大獎！"
    }
  ]
}
```

---

### 2. 商品熱度 API（新增）

**API**: `POST /api/lottery/browse/{lotteryId}/hot`

**用途**: 增加商品瀏覽次數（hotCount），可用於熱門商品排序

**請求**: 無需 body

**回應**:
```json
{
  "success": true,
  "data": 42  // 更新後的熱度值
}
```

**前端使用範例**:
```javascript
// 商品詳情頁載入時呼叫
useEffect(() => {
  axios.post(`/api/lottery/browse/${lotteryId}/hot`);
}, [lotteryId]);
```

---

### 3. 頭像上傳 API（新增）

#### 3.1 僅上傳圖片（適合預覽）

**API**: `POST /api/user/avatar`

**請求**:
```http
POST /api/user/avatar
Content-Type: multipart/form-data

file: [圖片檔案]
```

**回應**:
```json
{
  "success": true,
  "data": "https://s3.ap-northeast-1.amazonaws.com/test-ourkuji/avatars/xxx.png"
}
```

**使用情境**: 上傳圖片後先預覽，確認後再呼叫 `PUT /api/user/me` 更新

---

#### 3.2 上傳並更新頭像（一步完成）

**API**: `POST /api/user/avatar/update`

**請求**:
```http
POST /api/user/avatar/update
Content-Type: multipart/form-data

file: [圖片檔案]
```

**回應**:
```json
{
  "success": true,
  "data": {
    "id": "user-uuid",
    "email": "user@example.com",
    "nickname": "玩家",
    "avatarUrl": "https://s3.ap-northeast-1.amazonaws.com/test-ourkuji/avatars/xxx.png"
  }
}
```

**自動處理**:
- ✅ 上傳新圖片到 S3
- ✅ 更新使用者 `avatarUrl` 欄位
- ✅ 刪除舊的 S3 圖片（避免資源浪費）

**使用情境**: 直接更新頭像，不需預覽

---

**比較表**:

| 功能 | POST /user/avatar | POST /user/avatar/update | PUT /user/me |
|------|------------------|-------------------------|--------------|
| 上傳圖片 | ✅ | ✅ | ❌ 只接受 URL |
| 更新使用者記錄 | ❌ | ✅ | ✅ |
| 刪除舊圖片 | ❌ | ✅ | ❌ |
| 回傳內容 | S3 URL | 完整使用者資訊 | 完整使用者資訊 |

---

### 4. 使用者資料更新（修正）

**API**: `PUT /api/user/me`

**變更內容**:
- ✅ `avatar` 欄位僅接受 URL 字串（不支援檔案上傳）
- ✅ 變更 Email 時自動重置 `emailVerified` 為 0
- ✅ 所有欄位皆為選填，僅更新提供的欄位
- ✅ 移除 `@Valid` 驗證，空字串不再導致 400 錯誤

**請求**:
```json
{
  "nickname": "新暱稱",
  "email": "new@example.com",
  "avatar": "https://s3.ap-northeast-1.amazonaws.com/test-ourkuji/avatars/xxx.png"
}
```

---

## 📂 文件變更位置

### 已更新文件

**檔案**: `FRONTEND_API_COMPLETE_REFERENCE.md`

**變更章節**:
1. **標題區** - 新增「破壞性變更」警告區塊
2. **2.5** - 新增商品熱度 API（`POST /lottery/browse/{id}/hot`）
3. **3.2** - 更新抽獎 API 文檔，包含：
   - 新版請求格式（批次 + UUID）
   - 驗證規則說明
   - 統一陣列回應格式
   - 前端處理範例
   - 舊版格式相容性說明（摺疊區塊）
4. **4.2** - 修正使用者資料更新說明
5. **4.3** - 新增頭像上傳 API（僅上傳）
6. **4.4** - 新增頭像上傳並更新 API（一步完成）

---

## ✅ 檢查清單（前端工程師）

### 立即處理（P0）

- [ ] 修改抽獎 API 呼叫，改用 `response.data.data[0]` 取得結果
- [ ] 測試單次抽獎是否正常（確認陣列格式）
- [ ] 測試舊格式 `{"ticketNumber": x, "drawCount": 1}` 是否仍可用

### 近期處理（P1）

- [ ] 遷移到新格式 `{"count": x, "ticket": [uuids]}`
- [ ] 實作批次抽獎功能（一次抽多張票）
- [ ] 整合頭像上傳功能（使用 4.3 或 4.4）
- [ ] 整合商品熱度追蹤（2.5）

### 長期計劃（P2）

- [ ] 完全移除舊格式 `ticketNumber` + `drawCount` 的使用
- [ ] 優化錯誤處理（處理新的驗證錯誤訊息）
- [ ] 新增批次抽獎 UI/UX

---

## 🔄 API 變更管理流程（未來）

根據您的要求，建立以下規範：

### 規則

1. **每次 API 變更必須同步更新 `FRONTEND_API_COMPLETE_REFERENCE.md`**
2. **破壞性變更必須在文件頂部標註「破壞性變更」區塊**
3. **提供前端遷移指南與範例程式碼**
4. **舊格式保留至少 30 天相容期（除非安全性問題）**

### 文件結構

```markdown
# API 文件

## 📢 最新變更 (YYYY-MM-DD)

### 🚨 破壞性變更
- 列出所有不相容的變更
- 提供遷移指南

### ✨ 新增功能
- 新 API 列表

### 🐛 修正與改進
- Bug 修正列表
```

### 檢查清單（後端工程師）

每次 API 變更時：
- [ ] 更新 `FRONTEND_API_COMPLETE_REFERENCE.md`
- [ ] 若為破壞性變更，新增「破壞性變更」區塊
- [ ] 新增前端範例程式碼
- [ ] 更新 Postman Collection（如有）
- [ ] 通知前端工程師變更內容
- [ ] 建立 API 變更報告（如本文件）

---

## 📞 聯絡資訊

如有問題，請參考：
- 📄 完整 API 文檔：`FRONTEND_API_COMPLETE_REFERENCE.md`
- 🐛 問題回報：建立 Issue 或直接聯繫後端團隊
- 💬 討論區：[待建立]

---

**文件版本**: 1.1.0  
**更新日期**: 2026-02-05  
**更新人員**: GitHub Copilot
