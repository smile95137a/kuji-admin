# 後台商品建立流程：前端注意事項

此文件整理後台新增商品時，後端目前已實作的欄位契約與各類別差異，供前端同學使用。

## 1. 目標

當後端新增商品流程發生錯誤或商品建立失敗時，請先檢查以下欄位是否已正確傳入與分類對應。

## 2. 核心欄位清單

### 必傳或重要欄位
- `storeId`
- `title`
- `category`
- `subCategory`
- `pricePerDraw`
- `maxDraws`
- `status`
- `prizes`（每個獎項必須含 `quantity`）

### 可能需要傳入的欄位
- `gameMode`
- `delistStrategy`
- `paymentType`
- `freeDrawThreshold`
- `designatedPrizeNumbers`

### 後端管理，不需前端送出
- `ticketsGenerated`

## 2.1 欄位意義與是否必傳

### `category`
- 代表商品分類，例如 `OFFICIAL_ICHIBAN`、`GACHA`、`TRADING_CARD`、`CUSTOM_GACHA`。
- 欄位必填，後端會依此做分類判斷與自動邏輯。
- 若不傳，後端無法分辨商品屬性，會造成建立失敗。

### `subCategory`
- 進一步區分 `CUSTOM_GACHA` 的類型，常見為 `LOTTERY_MODE` 或 `SCRATCH_MODE`。
- 必填於 `CUSTOM_GACHA` 類型。
- 不傳會導致後端無法判斷是否需要 `gameMode` 或 `freeDrawThreshold`。

### `gameMode`
- 代表實際遊戲機制。例如 `SCRATCH_STORE`、`SCRATCH_PLAYER`、`RANDOM`。
- 只有 `CUSTOM_GACHA + SCRATCH_MODE` 需要前端傳。
- 其他分類會由後端自動帶入或忽略。
- 若必傳但未傳，後端會因缺少玩法資訊而報錯或生成錯誤行為。

### `delistStrategy`
- 代表商品下架策略，決定商品抽完後是否自動下架。
- `OFFICIAL_ICHIBAN` 必填，原因是：一番賞需要店家決定「最後大獎抽完是否直接下架」或「全抽完才下架」或「手動下架」。
- 允許值：`GRAND_PRIZE_DRAWN` / `ALL_DRAWN` / `MANUAL`。
- 不傳時，`OFFICIAL_ICHIBAN` 會直接報錯；其他分類若不傳，後端會自動補預設值。

### `paymentType`
- 表示付款方式。
- 有兩種可用值：
  - `GOLD`：使用遊戲幣支付
  - `BONUS`：使用紅利點數支付
- 可選傳入，若不傳後端會自動使用 `GOLD`。
- 如果你希望前端補上，請讓表單提供 `GOLD` / `BONUS` 選項。

### `freeDrawThreshold`
- 只在 `CUSTOM_GACHA + SCRATCH_MODE` 有效。
- 這個參數不是單純開關，而是「免單 / 免費抽啟用門檻」值。
- 有值且 `>= 1`，代表此商品啟用了免費抽或免單機制；
  - 例如 `10` 代表滿 10 次後可觸發免費抽
- 如果不想開啟免單機制，前端可以不傳此欄位，或傳 `null`。
- 若傳入值小於 1，後端會報錯 `免費抽門檻必須大於或等於 1`。
- 其他分類不應傳，後端會自動清空。
- 也就是說：
  - 不需要額外傳一個布林 `isFreeDrawEnabled`
  - 只要你想啟用免單機制，就傳一個有效的整數 threshold
  - 你若不想啟用，則不傳這個參數即可

### `designatedPrizeNumbers`
- 主要用於 `SCRATCH_STORE` 或 `SCRATCH_PLAYER` 的指定大獎流程。
- `SCRATCH_STORE` 可能會在建立時提供預設中獎號碼；`SCRATCH_PLAYER` 則可以留空，開套玩家再指定。

### `maxDraws`
- 代表總票數或總抽數。
- `CUSTOM_GACHA + SCRATCH_MODE` 的總票數由此決定，後端會依此生成謝謝惠顧票。
- `OFFICIAL_ICHIBAN` / `GACHA` 等則由獎品數或後端邏輯決定，仍需正確傳入。

### `prizes` 和 `quantity`
- `prizes` 只記錄真實獎品資訊。
- 每個獎項都要有 `quantity`。
- 若數量不足，後端會無法正確計算 `totalPrizes` 與 `remainingPrizes`。

## 3. 各類別建立規則

### A. OFFICIAL_ICHIBAN
- `gameMode` 後端自動設為 `TICKET`
- `delistStrategy` 必填
- 允許值：`GRAND_PRIZE_DRAWN` / `ALL_DRAWN` / `MANUAL`
- 為什麼要設定？因為一番賞的下架行為必須由店家決定，後端需要此策略來判斷何時改變商品狀態。
- 傳入後端會依此決定：
  - `GRAND_PRIZE_DRAWN`：最後一個大獎抽完即下架
  - `ALL_DRAWN`：所有獎品抽完才下架
  - `MANUAL`：抽完後仍保留，需店家手動下架
- 不傳就會造成後端擋單，錯誤訊息可能是 `OFFICIAL_ICHIBAN 必須設定 delistStrategy`。

### B. GACHA
- `gameMode` 後端自動設為 `RANDOM`
- `delistStrategy` 後端自動設為 `ALL_DRAWN`
- 前端不用傳 `delistStrategy`，但可傳 `paymentType`
- 若不傳 `paymentType`，後端會使用 `GOLD`。

### C. TRADING_CARD
- `gameMode` 應該由後端處理
- `delistStrategy` 後端自動設為 `ALL_DRAWN`

### D. CUSTOM_GACHA + LOTTERY_MODE
- `gameMode` 不顯示、不傳
- `delistStrategy` 後端自動設為 `ALL_DRAWN`
- `freeDrawThreshold` 必須為 `null`
- `maxDraws` 可傳，但商品總票數實際由獎品數決定

### E. CUSTOM_GACHA + SCRATCH_MODE
- `gameMode` 必填，必須是：`SCRATCH_STORE` / `SCRATCH_PLAYER` / `RANDOM`
- `delistStrategy` 後端自動設為 `GRAND_PRIZE_DRAWN`
- `freeDrawThreshold` 只有在此分類才有效，若傳入必須 `>= 1`
- `designatedPrizeNumbers` 可依 `SCRATCH_STORE` 或 `SCRATCH_PLAYER` 邏輯傳入

## 4. `prizes` 與 `maxDraws` 的差異

### `prizes`：只代表真實獎品數量
- `totalPrizes`、`remainingPrizes` 由 `prizes.quantity` 決定
- 若你看到「只有一張」，表示你只設定了一個 `quantity=1` 的獎品

### 刮刮樂的謝謝惠顧票
- 在 `CUSTOM_GACHA + SCRATCH_MODE` 中，`maxDraws` 代表總票數
- 真實 `prizes` 只放中獎獎品，不含謝謝惠顧票
- 後端會自動生成 `maxDraws - prizeCount` 張「謝謝惠顧」票
- 因此前端不要將 `prizes` 數量當作總票數

## 5. `ticketsGenerated` 的正確理解

- `ticketsGenerated` 是後端票券生成狀態旗標
- 前端不需送此欄位
- 它表示是否已經在後端執行過票券生成流程
- 若前端看到 `false`，表示票券尚未生成完成，不代表建立失敗

## 6. 常見失敗原因

1. `OFFICIAL_ICHIBAN` 沒傳 `delistStrategy`
2. `CUSTOM_GACHA + SCRATCH_MODE` 沒傳 `gameMode`
3. `CUSTOM_GACHA + SCRATCH_MODE` 傳了 `freeDrawThreshold` 但數值小於 1
4. `prizes` 定義錯誤，`quantity` 未填或數量不足
5. 前端以 `totalPrizes` 判斷總票數，忽略 `maxDraws`

## 7. 前端 payload 應該包含的欄位

以下欄位應該屬於 `payload.lottery`：
- `storeId`
- `title`
- `category`
- `subCategory`
- `pricePerDraw`
- `discountedPrice`
- `autoDiscountEnabled`
- `maxDraws`
- `remark`
- `hotCount`
- `theme`
- `galleryImages`
- `content`
- `tags`
- `bonusEnabled`
- `bonusPointsPerDraw`
- `bonusCostPerDraw`
- `paymentType`（可選，預設 GOLD）
- `status`
- `prizes`
- `gameMode`（僅 `CUSTOM_GACHA + SCRATCH_MODE` 需要）
- `delistStrategy`（`OFFICIAL_ICHIBAN` 必填，其他分類可不傳）
- `freeDrawThreshold`（僅 `CUSTOM_GACHA + SCRATCH_MODE` 有效）
- `designatedPrizeNumbers`（SCRATCH_STORE / SCRATCH_PLAYER 邏輯）

## 8. 前端應補文件的內容

請補上：
- `delistStrategy` 的存在與對應分類
- `delistStrategy` 允許值：`GRAND_PRIZE_DRAWN` / `ALL_DRAWN` / `MANUAL`
- `CUSTOM_GACHA + SCRATCH_MODE` 必須傳 `gameMode`
- `CUSTOM_GACHA + SCRATCH_MODE` 的 `freeDrawThreshold` 規則
- `ticketsGenerated` 是後端狀態，不是前端輸入
- `totalPrizes` 不是刮刮樂總票數，`maxDraws` 才是

> 若對欄位用途或規則有疑問，請務必立即詢問我或與我討論，避免前端憑經驗自行判斷。

## 9. 建議前端實作方式

- 先以 `category` / `subCategory` 判定是否需要顯示 `delistStrategy`
- `OFFICIAL_ICHIBAN` 顯示 `delistStrategy` 欄位並強制輸入
- `CUSTOM_GACHA + SCRATCH_MODE` 顯示 `gameMode` 選單
- `CUSTOM_GACHA + SCRATCH_MODE` 若有免費抽功能，再顯示 `freeDrawThreshold`
- `prizes` 清單請務必包含 `quantity`，不要只傳一筆獎品就以為總數就是票數

## 10. 後端錯誤訊息參考

常見後端錯誤：
- `OFFICIAL_ICHIBAN 必須設定 delistStrategy`
- `delistStrategy 僅允許 GRAND_PRIZE_DRAWN/ALL_DRAWN/MANUAL`
- `免費抽門檻必須大於或等於 1`

---

> 此文件為前端與後端溝通對照清單，請在新增商品開發時優先檢查以上欄位與分類規則。