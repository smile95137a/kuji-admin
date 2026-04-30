# Quickstart：商品管理重整

## 1. 前置確認

1. 確認使用 `specs/019-product-overhaul/spec.md`、`plan.md`、`research.md` 作為單一事實來源。
2. 確認目前規則以 2026-04-30 的 clarifications 為準：
   - `freeDrawThreshold` 僅適用 `CUSTOM_GACHA + SCRATCH_MODE`
   - `freeDrawThreshold = NULL` 合法，代表未啟用免費抽/免單機制
   - `CUSTOM_GACHA + LOTTERY_MODE` 固定 `delistStrategy = ALL_DRAWN`

## 2. 建議實作順序

1. 新增/整理 SQL migration
   - 加入 `payment_type`
   - 加入 `free_draw_threshold`
   - 加入 `delist_strategy`
   - 將 `multi_draw_options`、`allow_multi_draw`、`protection_draws`、`protection_minutes` 標記為廢棄

2. 同步資料模型與 MBG
   - 更新 `generatorConfig.xml` 對應欄位
   - 執行 `mvn mybatis-generator:generate` 或 `./run-mbg.ps1`
   - 確認 `entity` / `mapper` / `example` / `LotteryMapper.xml` 與新欄位一致

3. 修正 request / response 契約
   - `LotteryCreateReq`
   - `LotteryUpdateReq`
   - `LotteryRes`
   - Swagger / OpenAPI 註解

4. 收斂 service 邏輯
   - `resolvePlayMode()`
   - `resolveGameMode()`
   - `resolveDelistStrategy()`
   - `normalizeFreeDrawThreshold()`
   - `checkAndDelist()`

5. 收斂 controller
   - 將 `AdminLotteryWithPrizesController` 能力移回 `AdminLotteryController`
   - 驗證 `LotteryController` 路徑相容性
   - 保持 admin/public 路徑不變

6. 補測試
   - Service：分類導向欄位正規化
   - Controller：建立/更新/查詢 contract
   - Integration：抽獎後自動下架

## 3. 最低驗證矩陣

| 案例 | 預期結果 |
|------|----------|
| 建立 `OFFICIAL_ICHIBAN` 並指定 `MANUAL` | 建立成功，`gameMode=TICKET`，`delistStrategy=MANUAL` |
| 建立 `GACHA` 且未傳 `paymentType` | 建立成功，`paymentType=GOLD`，`delistStrategy=ALL_DRAWN` |
| 建立 `CUSTOM_GACHA + LOTTERY_MODE` 且未傳 `gameMode` | 建立成功，`gameMode=NULL`，`freeDrawThreshold=NULL` |
| 建立 `CUSTOM_GACHA + SCRATCH_MODE` 且 `freeDrawThreshold=NULL` | 建立成功，代表未啟用免費抽 |
| 建立 `CUSTOM_GACHA + SCRATCH_MODE` 且 `freeDrawThreshold=0` | 建立失敗 |
| 更新非 `DRAFT` 商品的 `paymentType` | 依規則拒絕或維持原值，且行為需一致 |
| `GRAND_PRIZE_DRAWN` 最後大獎抽出 | `status -> ENDED` |
| `MANUAL` 商品全部抽完 | `status -> SOLD_OUT`，不自動下架 |

## 4. 驗證指令

```powershell
mvn clean compile
mvn test
mvn clean package -DskipTests
```

## 5. 手動檢查重點

1. 後台表單切換 `CUSTOM_GACHA` 子分類時，`gameMode` 與 `freeDrawThreshold` 顯示條件正確。
2. 非刮刮樂商品不會殘留 `freeDrawThreshold`。
3. 舊 `with-prizes` 能力在新 controller 歸屬下仍可正常使用。
4. 抽獎流程仍能依 `playMode` / `gameMode` 正確分派到 ticket / scratch strategy。
