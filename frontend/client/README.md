# KUJI 前台（玩家端）前端規範文件

> **最後同步時間**：2026-04-14  
> **後端版本**：Spring Boot 3.3.3 / Java 21  
> **Context Path**：`/api`（所有 API 以 `http://localhost:8080/api` 開頭）

---

## 📂 文件導覽

| 文件 | 說明 |
|------|------|
| [00-architecture.md](./00-architecture.md) | 整體架構、API 格式、安全性說明、枚舉值 |
| [01-auth.md](./01-auth.md) | 會員註冊、Email 登入、Google OAuth、Token 刷新 |
| [02-user-profile.md](./02-user-profile.md) | 個人資料、頭像上傳、密碼修改、收件地址 |
| [03-lottery-browse.md](./03-lottery-browse.md) | 商品列表、商品詳情、籤位查詢、店家列表 |
| [04-draw-flow.md](./04-draw-flow.md) | 抽獎核心流程（一番賞/扭蛋/刮刮樂各模式） |
| [05-prize-box.md](./05-prize-box.md) | 獎品盒管理、申請出貨、回收換紅利 |
| [06-order-management.md](./06-order-management.md) | 訂單查詢、提交收件資訊、訂單狀態 |
| [07-wallet-recharge.md](./07-wallet-recharge.md) | 金幣/紅利錢包、儲值方案、儲值流程 |
| [08-referral-code.md](./08-referral-code.md) | 推薦碼產生、驗證、套用、統計 |
| [09-public-content.md](./09-public-content.md) | 公開內容（新聞、Banner、跑馬燈） |
| [PROMPT-FOR-FRONTEND.md](./PROMPT-FOR-FRONTEND.md) | 給前端開發者的溝通 Prompt |

---

## ⚡ 快速開始

### API Base URL
```
http://localhost:8080/api
```

### 前台路由結構
```
公開端點（不需登入）：
  GET  /api/stores/list         店家列表
  POST /api/lottery/browse/list 商品列表
  GET  /api/lottery/browse/{id}/detail 商品詳情
  GET  /api/recharge-plan/list  儲值方案

認證端點（需 Bearer Token）：
  /api/user/**          個人資料
  /api/lottery/draw/**  抽獎
  /api/prize-box/**     獎品盒
  /api/order/**         訂單
  /api/wallet/**        錢包
  /api/referral/**      推薦碼
```

### 認證方式
```http
Authorization: Bearer {accessToken}
```

---

## 🔑 核心設計原則

### 雙幣系統
前台玩家擁有兩種點數：

| 類型 | 說明 | 取得方式 | 可抽獎 |
|------|------|---------|--------|
| **Gold（金幣）** | 儲值金，主要貨幣 | 儲值購買 | ✅ |
| **Bonus（紅利）** | 系統贈送，補充用 | 推薦碼獎勵、回收獎品 | ✅（補充） |

**扣款邏輯**：優先消耗 Gold，不足時 Bonus 自動補足（混用）。

### 獎品盒機制
玩家抽到的獎品不直接寄出，先放入「獎品盒」：
1. 抽獎 → 獎品進入獎品盒（`AVAILABLE` 狀態）
2. 玩家可選擇：**申請出貨**（產生訂單）或**回收換紅利**
3. 出貨後訂單進入物流流程

### 商品類型路由
| 商品類型 | `category` | 前台抽獎路由 |
|---------|-----------|------------|
| 官方一番賞 | `OFFICIAL_ICHIBAN` | `/lottery/draw/{id}/draw`（籤位制） |
| 扭蛋 | `GACHA` | `/lottery/random/{id}/draw`（加權隨機） |
| 集換式卡牌 | `TRADING_CARD` | `/lottery/draw/{id}/draw`（籤位制） |
| 自製賞 | `CUSTOM_GACHA` | 依 `subCategory` 決定（見 [04-draw-flow.md](./04-draw-flow.md)） |

---

## 🚫 重要規則（前端必讀）

1. ✅ **公開端點不需 Token**：商品列表、商品詳情、店家列表可匿名瀏覽
2. ❌ **不要直接顯示未抽籤位的獎品**：`AVAILABLE` 票券的 `prizeId`/`prizeLevel` 後端不回傳
3. ❌ **不要傳 `playMode`** — 後端自動推算
4. ❌ **不要傳 `ticketNumber` 指定大獎**（刮刮樂）— 應傳 `revealedNumber`
5. ✅ **抽獎前確認錢包餘額**，避免不必要的 API 請求失敗
6. ✅ **保護時間倒數計時**：抽獎後回應的 `protectionEndTime` 需顯示給使用者看
