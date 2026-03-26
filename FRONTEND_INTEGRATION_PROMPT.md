# 🎯 前端 API 串接 Prompt（給 GitHub Copilot）

## 📌 任務目標

我需要你幫我把前端畫面與後端 API 完整串接起來，讓所有功能都能實際運作。

---

## 🔧 專案資訊

- **後端 Base URL**：`http://18.179.187.129/api`
- **API 文檔**：請參考 `FRONTEND_API_COMPLETE_REFERENCE.md`
- **認證方式**：JWT Token（放在 `Authorization: Bearer {token}` Header）

---

## 📋 串接清單（按優先順序）

### ✅ 第一階段：基礎功能（最高優先）

#### 1. 使用者認證流程
- [ ] **登入頁面**
  - API: `POST /api/auth/login`
  - 功能：Email + 密碼登入
  - 成功後儲存 `accessToken` 和 `refreshToken` 到 localStorage
  - 跳轉到首頁

- [ ] **註冊頁面**
  - API: `POST /api/auth/register`
  - 功能：Email + 密碼 + 暱稱註冊
  - 支援推薦碼（選填）
  - 驗證密碼與確認密碼一致
  - 成功後自動登入

- [ ] **Token 管理**
  - 設定 Axios 攔截器，自動加上 `Authorization` Header
  - Token 過期時自動呼叫 `POST /api/auth/refresh` 刷新
  - 刷新失敗則導向登入頁

#### 2. 首頁
- [ ] **Banner 輪播**
  - API: `GET /api/banner/carousel`
  - 顯示輪播圖
  - 點擊可跳轉到 `linkUrl`

- [ ] **跑馬燈**
  - API: `GET /api/marquee`
  - 顯示最新消息跑馬燈

- [ ] **商品列表預覽**
  - API: `POST /api/lottery/browse/list`
  - 顯示最新上架的商品（前 10 筆）
  - 點擊進入商品詳情頁

#### 3. 商品列表頁
- [ ] **商品搜尋與篩選**
  - API: `POST /api/lottery/browse/list`
  - 支援篩選：標題、分類、價格區間
  - 支援排序：建立時間、價格
  - 前端分頁（每頁 20 筆）

- [ ] **商品卡片顯示**
  - 顯示：主圖、標題、價格、剩餘抽數
  - 店家資訊：店名、縣市區域
  - 點擊進入商品詳情頁

#### 4. 商品詳情頁（核心功能）
- [ ] **商品資訊顯示**
  - API: `GET /api/lottery/browse/{id}/detail`
  - 顯示：主圖、副圖輪播、標題、說明、價格
  - 顯示總抽數 / 剩餘抽數

- [ ] **獎品列表**
  - 顯示所有獎品等級（A/B/C/F 賞等）
  - 顯示獎品圖片、名稱、數量、剩餘數量
  - 標示大獎（isGrandPrize）

- [ ] **籤位格子顯示**
  - ✅ 已抽：顯示獎品等級（A/B/C）+ 獎品名稱
  - ⚪ 未抽：只顯示編號 + "可抽"狀態
  - 🔒 鎖定：顯示 "其他玩家抽獎中"

- [ ] **抽獎按鈕**
  - 未登入：顯示 "請先登入"
  - 已登入 + 可抽：顯示 "立即抽獎" 或 "選號抽獎"
  - 保護中：顯示 "商品被鎖定中，剩餘時間：XX 分鐘"

---

### ✅ 第二階段：抽獎功能（核心）

#### 5. 抽獎執行
- [ ] **隨機抽獎**
  - API: `POST /api/lottery/draw/{id}/draw`
  - Body: `{ "ticketNumber": null }`
  - 成功後顯示中獎動畫 + 獎品資訊
  - 自動更新籤位格子狀態
  - 自動更新剩餘抽數

- [ ] **選號抽獎**
  - 點擊未抽的籤位格子
  - API: `POST /api/lottery/draw/{id}/draw`
  - Body: `{ "ticketNumber": 13 }`
  - 成功後顯示中獎動畫

- [ ] **免單提示**
  - 如果 `triggeredFreeDraw: true`
  - 顯示特效：🎉 恭喜開套免單！退還 XXX 元
  - 播放慶祝音效

#### 6. 刮刮樂模式（如果有）
- [ ] **玩家指定大獎位置**
  - 開套玩家進入商品時，顯示 "請選擇大獎位置"
  - 讓玩家點選 N 個籤位
  - API: `POST /api/lottery/draw/{id}/designate`
  - Body: `{ "prizeNumbers": [13, 45, 76] }`

---

### ✅ 第三階段：個人中心

#### 7. 個人資訊
- [ ] **我的資訊頁面**
  - API: `GET /api/user/me`
  - 顯示：暱稱、Email、註冊時間
  - 未來可擴充：編輯個人資訊

#### 8. 錢包管理
- [ ] **我的錢包**
  - API: `GET /api/wallet`
  - 顯示：餘額、紅利
  - 顯示 "儲值" 按鈕（未來串接金流）

- [ ] **交易記錄**
  - API: `POST /api/wallet/transactions`
  - 顯示：交易類型、金額、時間、描述
  - 支援篩選：交易類型、日期區間

#### 9. 賞品盒
- [ ] **我的賞品盒列表**
  - API: `GET /api/prize-box`
  - 顯示：獎品圖片、名稱、等級、所屬商品、店家
  - 狀態標籤：賞品盒中 / 已出貨 / 已回收

- [ ] **按店家分組顯示**
  - API: `GET /api/prize-box/summary`
  - 群組顯示同一店家的獎品
  - 方便一次出貨

- [ ] **出貨功能**
  - 勾選要出貨的獎品
  - 選擇配送地址（如果沒有則跳轉到地址新增頁）
  - API: `POST /api/prize-box/ship`
  - Body: `{ "prizeBoxIds": [...], "addressId": "..." }`
  - 成功後跳轉到訂單列表

- [ ] **回收功能**
  - 勾選可回收的獎品（F賞等）
  - 顯示可獲得紅利數量
  - API: `POST /api/prize-box/recycle`
  - 成功後更新錢包紅利

#### 10. 訂單管理
- [ ] **我的訂單列表**
  - API: `POST /api/order/list`
  - 顯示：訂單編號、狀態、總金額、建立時間
  - 狀態標籤：待出貨 / 已出貨 / 已完成 / 已取消
  - 支援篩選：狀態、日期

- [ ] **訂單詳情**
  - API: `GET /api/order/{orderId}`
  - 顯示：訂單編號、商品清單、配送地址、總金額

#### 11. 地址管理
- [ ] **地址列表**
  - API: `GET /api/user/addresses`
  - 顯示所有地址
  - 標示預設地址

- [ ] **新增地址**
  - API: `POST /api/user/addresses`
  - 表單：收件人、電話、縣市區域、詳細地址、郵遞區號
  - 縣市區域使用 `GET /api/district/cities` 和 `GET /api/district/districts/{city}`

- [ ] **編輯地址**
  - API: `PUT /api/user/addresses/{id}`

- [ ] **刪除地址**
  - API: `DELETE /api/user/addresses/{id}`

- [ ] **設為預設地址**
  - API: `PUT /api/user/addresses/{id}/default`

---

### ✅ 第四階段：其他功能

#### 12. 新聞公告
- [ ] **新聞列表**
  - API: `GET /api/news`
  - 顯示：標題、摘要、發布時間
  - 點擊進入新聞詳情

- [ ] **新聞詳情**
  - API: `GET /api/news/{id}`
  - 顯示：標題、完整內容、圖片、發布時間

#### 13. 店家資訊
- [ ] **店家商品列表**
  - API: `GET /api/lottery/browse/store/{storeId}`
  - 顯示該店家的所有商品

---

## 🎨 技術建議

### Axios 設定
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://18.179.187.129/api',
  timeout: 10000,
});

// 自動加上 Token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 處理 401 錯誤（Token 過期）
api.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const res = await axios.post('/api/auth/refresh', { refreshToken });
          localStorage.setItem('accessToken', res.data.data.accessToken);
          // 重試原請求
          error.config.headers.Authorization = `Bearer ${res.data.data.accessToken}`;
          return api.request(error.config);
        } catch {
          // 刷新失敗，跳轉登入頁
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);

export default api;
```

### 後端回應格式
所有 API 都遵循統一格式：
```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "meta": {
    "timestamp": "2026-01-27T10:30:00",
    "requestId": "uuid"
  }
}
```

所以前端接收時：
```javascript
const response = await api.post('/lottery/browse/list', { ... });
const data = response.data.data;  // 注意：要取 .data.data
```

### 錯誤處理
```javascript
try {
  const response = await api.post('/lottery/draw/xxx/draw', { ... });
  const result = response.data.data;
  
  if (result.success) {
    // 抽獎成功
    showSuccessModal(result);
  } else {
    // 抽獎失敗
    showErrorMessage(result.message);
  }
} catch (error) {
  if (error.response?.data?.error) {
    // 後端錯誤
    showErrorMessage(error.response.data.error.message);
  } else {
    // 網路錯誤
    showErrorMessage('網路錯誤，請稍後再試');
  }
}
```

---

## ⚠️ 注意事項

### 1. 籤位安全性
- **絕對不要**在前端產生未抽籤位的獎品資訊
- 後端 API 已經過濾敏感資訊，直接使用即可
- 未抽籤位只會返回：`{ ticketNumber, status: "AVAILABLE" }`

### 2. 抽獎保護機制
- 商品被其他玩家抽獎時會鎖定
- 前端應顯示剩餘保護時間
- 可使用輪詢（polling）或 WebSocket 更新狀態

### 3. 開套免單提示
- 檢查 `triggeredFreeDraw` 欄位
- 如果為 `true`，顯示特殊動畫與音效
- 退款金額在 `refundAmount` 欄位

### 4. 分頁處理
- 後端返回全部資料，前端做分頁
- 建議每頁 20 筆
- 使用虛擬滾動優化長列表效能

### 5. 圖片載入
- 所有圖片 URL 都是 S3 完整路徑
- 設定 `onError` 處理載入失敗情況
- 使用預設圖片或 Placeholder

---

## 🚀 開始串接

### 建議順序：
1. **先做登入/註冊** → 確保 Token 機制正常
2. **首頁** → Banner + 商品列表預覽
3. **商品列表** → 搜尋篩選功能
4. **商品詳情 + 抽獎** → 核心功能
5. **個人中心** → 錢包、賞品盒、訂單
6. **地址管理** → 出貨功能
7. **其他功能** → 新聞、跑馬燈等

---

## 📞 需要協助？

如果遇到以下問題，請立即反饋：
- API 返回錯誤
- 回應格式不符預期
- CORS 問題
- Token 驗證失敗
- 功能邏輯不清楚

---

## ✅ 驗收標準

### 基本要求
- [ ] 所有 API 都能正常呼叫
- [ ] Token 自動刷新機制正常
- [ ] 錯誤訊息友善顯示
- [ ] 載入狀態（Loading）顯示

### 進階要求
- [ ] 抽獎動畫流暢
- [ ] 免單特效顯示
- [ ] 籤位即時更新
- [ ] 圖片懶加載
- [ ] RWD 響應式設計

---

**祝串接順利！有問題隨時找我！** 🎉
