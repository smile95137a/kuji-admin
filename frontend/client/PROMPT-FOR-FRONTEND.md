# 前台前端開發者溝通提示詞

> 複製以下提示詞給前台（玩家端）前端開發者，讓他們快速理解後端架構與業務規則。

---

## 提示詞（可直接複製給前端）

---

**你好，以下是我這次前台前端開發的後端規格說明，請詳細閱讀後開始開發：**

---

## 一、基本資訊

- **後端 API Base URL**：`http://localhost:8080/api`
- **Swagger UI**：`http://localhost:8080/api/swagger-ui.html`（可直接測試）
- **認證方式**：Bearer Token（JWT）
- **回應格式**（所有 API 統一包裝）：
```json
{
  "success": true,
  "data": { /* 實際資料 */ },
  "error": null,
  "meta": { "timestamp": "...", "requestId": "..." }
}
```
取資料時讀 `response.data.data`（兩層 data）。

---

## 二、認證 Token 機制

- 登入後取得 `accessToken`（短效，約 30 分鐘）和 `refreshToken`（長效，約 7 天）
- `accessToken` 存 Pinia store（記憶體），`refreshToken` 存 `localStorage`
- **每次 API 呼叫**：Header 帶 `Authorization: Bearer {accessToken}`
- **Token 過期（401）**：自動用 `refreshToken` 呼叫 `POST /auth/refresh` 取新 Token
- `refreshToken` 為一次性（使用後舊的失效），收到新的要馬上更新 `localStorage`

```typescript
// Axios 攔截器範本
axios.interceptors.response.use(
  res => res,
  async error => {
    if (error.response?.status === 401 && !error.config._retry) {
      error.config._retry = true;
      const token = localStorage.getItem('refreshToken');
      const res = await axios.post('/auth/refresh', { refreshToken: token });
      const { accessToken, refreshToken } = res.data.data;
      authStore.setTokens(accessToken, refreshToken);
      error.config.headers.Authorization = `Bearer ${accessToken}`;
      return axios(error.config);
    }
    return Promise.reject(error);
  }
);
```

---

## 三、雙幣制系統（金幣 vs 紅利）

```
金幣（goldCoins）= 購買的幣，主要消費幣
紅利（bonusCoins）= 贈送/回收的幣，補充用

扣款規則：
  每次抽獎先扣金幣，金幣不足時，紅利補差額
  例：需 240、金幣 200、紅利 100 → 扣 200 金幣 + 40 紅利

前端只需顯示抽後回傳的 remainingGold + remainingBonus，
不需要自己計算扣款邏輯。
```

---

## 四、商品分類與抽獎路由（關鍵！）

商品有三個分類欄位，前端**只需讀 `playMode` 來決定走哪個抽獎 API**：

```typescript
// playMode = LOTTERY_MODE
if (lottery.category === 'GACHA') {
  // 使用加權隨機抽獎
  POST /api/lottery/random/{id}/draw?count=N
} else {
  // 使用票券制抽獎
  POST /api/lottery/draw/{id}/draw
}

// playMode = SCRATCH_MODE（刮刮樂）
// 同樣使用票券制抽獎
POST /api/lottery/draw/{id}/draw
```

| playMode | category | 抽獎 API |
|---------|---------|---------|
| `LOTTERY_MODE` | `GACHA` | `/lottery/random/{id}/draw` |
| `LOTTERY_MODE` | `OFFICIAL_ICHIBAN` / `TRADING_CARD` / `CUSTOM_GACHA` | `/lottery/draw/{id}/draw` |
| `SCRATCH_MODE` | 刮刮樂商品（`CUSTOM_GACHA` / `OFFICIAL_ICHIBAN`） | `/lottery/draw/{id}/draw` |

---

## 五、抽獎回應處理（三種情況）

票券制抽獎（`POST /lottery/draw/{id}/draw`）可能回傳三種不同結構：

### 情況 1：正常開獎
```typescript
// response.data.data.results 是陣列
if (response.data.data.results) {
  const { results, protectionEndTime } = response.data.data;
  // 顯示開獎動畫、更新餘額
}
```

### 情況 2：刮刮樂 Opener 需要指定大獎位置
```typescript
if (response.data.data.designationRequired === true) {
  const { availableNumbers, grandPrizes } = response.data.data;
  // 顯示大獎指定 UI
  // 計算需要指定幾個：grandPrizes.reduce((sum, p) => sum + p.quantity, 0)
}
```

### 情況 3：等待 Opener 指定中
```typescript
if (response.data.data.designationPending === true) {
  // 顯示「等待中」提示
  // 輪詢 GET /lottery/draw/{id}/session 確認狀態
}
```

---

## 六、保護計時器

一番賞/卡牌/自製賞類商品，開套玩家抽完後啟動保護計時器：

```typescript
// 從抽獎回應取得
const { protectionEndTime } = response.data.data;

const now = new Date();
const endTime = new Date(protectionEndTime);

if (endTime > now) {
  // 顯示倒數計時
  // 提示：保護期間仍可隨機抽，但無法指定特定籤位
}
```

---

## 七、獎品盒 → 出貨流程

```
抽獎 → 獎品進入 prize box（不直接出貨！）
             │
             ├── 申請出貨（/prize-box/ship）→ 建立訂單（/order）
             │
             └── 回收換紅利（/prize-box/recycle）→ 紅利到帳
```

**同一張訂單只能包含同一家店的獎品**，跨店需分別申請。

---

## 八、公開 vs 需登入端點

**不需要登入（可直接呼叫）**：
- `GET /banner/list`、`GET /marquee/list`、`GET /news/**`
- `GET /stores/list`
- `POST /lottery/browse/list`、`GET /lottery/browse/{id}/detail`
- `POST /auth/register`、`POST /auth/login`

**需要登入（需帶 Bearer Token）**：
- 所有 `/user/**`、`/wallet/**`、`/recharge/**`、`/prize-box/**`、`/order/**`
- `POST /lottery/draw/**`、`POST /lottery/random/**`
- `/referral/**`

---

## 九、儲值注意事項（測試模式）

⚠️ 目前儲值功能為**測試模式**，呼叫 `POST /recharge` 後金幣**直接到帳**，不會跳轉支付頁。  
`paymentUrl` 欄位目前為 `null`。  
正式上線前會串接金流，屆時流程會改變，請提前預留 UI 空間。

---

## 十、常見錯誤處理

| HTTP | 常見原因 |
|------|---------|
| `400` | 參數錯誤、餘額不足、籤數不足 |
| `401` | Token 過期（自動 refresh） |
| `403` | 無權限（如看別人的訂單） |
| `404` | 資源不存在（如商品下架） |
| `409` | 狀態衝突（如重複套用推薦碼） |
| `423` | 商品已下架或無法操作 |

---

## 十一、詳細文件目錄

```
frontend/client/
├── README.md              快速索引（API 路由速查）
├── 00-architecture.md     Axios 設定、通用型別
├── 01-auth.md             登入/註冊/OAuth/Token 刷新
├── 02-user-profile.md     個人資料、收件地址
├── 03-lottery-browse.md   商品列表與詳情（公開）
├── 04-draw-flow.md        ★抽獎核心流程（必讀）
├── 05-prize-box.md        獎品盒、出貨、回收
├── 06-order-management.md 訂單查詢
├── 07-wallet-recharge.md  錢包、儲值
├── 08-referral-code.md    推薦碼系統
└── 09-public-content.md   Banner、跑馬燈、消息
```

有任何問題請先查閱上述文件，或在 Swagger UI 直接測試 API。

---
