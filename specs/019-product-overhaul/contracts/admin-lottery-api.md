# Admin Lottery API Contract

**Feature**: `019-product-overhaul`  
**Scope**: 後台商品建立、更新、查詢，以及 `with-prizes` 路徑收斂

## 1. 路徑相容性

本次調整不得破壞下列既有對外路徑：

| Method | Path | 契約要求 |
|--------|------|----------|
| `POST` | `/admin/lottery` | 建立商品，套用分類導向的衍生欄位規則 |
| `PUT` | `/admin/lottery/{id}` | 更新商品，重算衍生欄位並套用狀態限制 |
| `GET` | `/admin/lottery/{id}` | 查詢商品詳情，需回傳新欄位 |
| `POST` | `/admin/lottery/list` | 列表查詢，既有條件與分頁行為不變 |
| `POST` | `/admin/lottery/with-prizes` | 建立商品與獎品，取代分散 controller 的主要寫入入口 |
| `PUT` | `/admin/lottery/with-prizes/{lotteryId}` | 更新商品與獎品 |
| `GET` | `/admin/lottery/with-prizes/{lotteryId}` | 查詢商品與獎品詳情 |
| `POST` | `/admin/lottery/with-prizes/list` | 查詢商品與獎品列表 |

## 2. Request Normalization Rules

### 2.1 Create / Update 共用規則

| 條件 | `playMode` | `gameMode` | `freeDrawThreshold` | `delistStrategy` |
|------|------------|------------|---------------------|------------------|
| `OFFICIAL_ICHIBAN` | 後端固定 `LOTTERY_MODE` | 後端固定 `TICKET` | 強制 `NULL` | 必須由店家提供 |
| `TRADING_CARD` | 後端固定 `LOTTERY_MODE` | 後端固定 `TICKET` | 強制 `NULL` | 固定 `ALL_DRAWN` |
| `GACHA` | 後端固定 `LOTTERY_MODE` | 後端固定 `RANDOM` | 強制 `NULL` | 固定 `ALL_DRAWN` |
| `CUSTOM_GACHA + LOTTERY_MODE` | 後端固定 `LOTTERY_MODE` | 強制 `NULL` | 強制 `NULL` | 固定 `ALL_DRAWN` |
| `CUSTOM_GACHA + SCRATCH_MODE` | 後端固定 `SCRATCH_MODE` | 必填：`SCRATCH_STORE` / `SCRATCH_PLAYER` / `RANDOM` | `NULL` 或 `>= 1` | 固定 `GRAND_PRIZE_DRAWN` |

### 2.2 無效輸入處理

| 情境 | 預期行為 |
|------|----------|
| `CUSTOM_GACHA + SCRATCH_MODE` 且 `gameMode` 缺漏 | 拒絕請求 |
| `CUSTOM_GACHA + SCRATCH_MODE` 且 `freeDrawThreshold = 0` 或負值 | 拒絕請求 |
| 非 `SCRATCH_MODE` 傳入 `freeDrawThreshold` | 忽略並正規化為 `NULL` |
| 固定策略分類傳入自訂 `delistStrategy` | 忽略 client 值，覆寫成固定策略 |
| 非 `DRAFT` 商品修改 `paymentType` | 拒絕請求或維持原值，實作需以單一路徑一致處理 |

## 3. Response Fields

後台商品詳情與列表回應至少需穩定輸出下列欄位：

| 欄位 | 說明 |
|------|------|
| `paymentType` | `GOLD` / `BONUS` |
| `freeDrawThreshold` | 僅刮刮樂可能有值；未啟用時回傳 `NULL` |
| `delistStrategy` | 實際生效下架策略 |
| `subCategory` | `CUSTOM_GACHA` 的子分類 |
| `playMode` | 後端推導結果 |
| `gameMode` | 刮刮樂子模式；抽籤型應回傳 `NULL` |

## 4. Controller Merge Contract

- `AdminLotteryWithPrizesController` 的功能必須能在 `AdminLotteryController` 下完整存取。
- 若暫時保留舊路徑做過渡，行為與新路徑必須一致，不可出現欄位驗證分岔。
- Swagger / OpenAPI 註解需同步反映新欄位與固定規則，避免後台前端依錯誤文件送值。
