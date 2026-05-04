# KUJI-Server 人工 API 驗收清單（全域掃描版）

這份文件要呈現什麼內容：
提供「由人類 QA/PM/工程師」執行的 API 驗收清單。內容包含整體 API 掃描摘要與逐項勾選項目，僅列清單，不包含本文件作者代為驗收結果。

## 掃描摘要（Controller 層）

- 掃描範圍：`src/main/java/com/group/admin/controller/**`
- 掃描日期：2026-04-30
- 掃描結果：
  - 總端點數：`295`
  - 後台端點：`190`
  - 前台端點：`105`

## Controller 清單（整體 API 盤點）

> 格式：`Layer | Controller | BasePath | Endpoints`

### admin controllers

- [ ] `admin | AdminAccountController | /admin/accounts | 4`
- [ ] `admin | AdminAuthController | (ApiPaths.ADMIN_AUTH) | 5`
- [ ] `admin | AdminBannerController | /admin/banners | 10`
- [ ] `admin | AdminCategoryController | /admin/category | 4`
- [ ] `admin | AdminCoinController | /admin/coin | 3`
- [ ] `admin | AdminConsumptionRecordController | /admin/consumption-records | 1`
- [ ] `admin | AdminContactInquiryController | /admin/contact-inquiries | 4`
- [ ] `admin | AdminDrawHistoryController | /admin/lottery | 1`
- [ ] `admin | AdminFrontendUserController | /admin/frontend-users | 9`
- [ ] `admin | AdminLotteryController | /admin/lottery | 14`
- [ ] `admin | AdminLotteryWithPrizesController | /admin/lottery-with-prizes | 4`
- [ ] `admin | AdminMarqueeController | /admin/marquee | 7`
- [ ] `admin | AdminNewsController | /admin/news | 8`
- [ ] `admin | AdminOrderController | /admin/orders | 8`
- [ ] `admin | AdminPrizeBoxController | /admin/prize-box | 2`
- [ ] `admin | AdminRechargePackagesController | /admin/recharge-packages | 1`
- [ ] `admin | AdminRechargePlanController | /admin/recharge-plan | 6`
- [ ] `admin | AdminReferralCodeController | /admin/referral-codes | 12`
- [ ] `admin | AdminReportController | /admin/report | 9`
- [ ] `admin | AdminShippingMethodController | /admin/shipping-methods | 4`
- [ ] `admin | AdminStoreController | /admin/stores | 8`
- [ ] `admin | AdminSystemConfigController | /admin/system-config | 4`
- [ ] `admin | AdminSystemLogController | /admin/system-log | 4`
- [ ] `admin | AdminUserController | /admin/users | 15`
- [ ] `admin | DebugController | /admin/debug | 2`
- [ ] `admin | LotteryPrizeController | /admin/lotteries | 9`
- [ ] `admin | MenuController | /admin/menus | 10`
- [ ] `admin | PermissionController | /admin/permissions | 7`
- [ ] `admin | RoleController | /admin/roles | 10`
- [ ] `admin | UploadController | /admin/upload | 5`

### api controllers

- [ ] `api | ApiAuthController | /auth | 9`
- [ ] `api | BannerController | /banners | 1`
- [ ] `api | CategoryController | /category | 5`
- [ ] `api | ConsumptionRecordController | /consumption-records | 1`
- [ ] `api | ContactInquiryController | /contact-inquiry | 1`
- [ ] `api | DistrictController | /district | 5`
- [ ] `api | DrawController | /lottery | 4`
- [ ] `api | EnumController | /enums | 12`
- [ ] `api | LotteryBrowseController | /lottery/browse | 4`
- [ ] `api | LotteryController | /lottery | 4`
- [ ] `api | LotteryDrawController | /lottery/draw | 5`
- [ ] `api | LotteryLockController | /lottery | 1`
- [ ] `api | MarqueeController | /marquee | 1`
- [ ] `api | NewsController | /news | 2`
- [ ] `api | OAuth2Controller | /auth/oauth2 | 1`
- [ ] `api | OrderController | /order | 5`
- [ ] `api | PaymentCallbackController | /payment | 2`
- [ ] `api | PrizeBoxController | /prize-box | 5`
- [ ] `api | RandomDrawController | /lottery/random | 1`
- [ ] `api | RechargeController | /recharge | 5`
- [ ] `api | RechargePackagesPublicController | /recharge-plans | 1`
- [ ] `api | RechargePlanController | /recharge-plan | 2`
- [ ] `api | ReferralCodeValidateController | /auth | 3`
- [ ] `api | ReferralController | /referral | 5`
- [ ] `api | ShippingMethodController | /shipping-methods | 1`
- [ ] `api | StoreController | /stores | 2`
- [ ] `api | StoreOptionController | /stores | 1`
- [ ] `api | UserAddressController | /user/addresses | 7`
- [ ] `api | UserController | /user | 6`
- [ ] `api | WalletRechargeController | /wallet/recharge | 3`

## 人工驗收主清單（只列檢查項）

## P0（不通過不可上線）

- [ ] 商品建立（含獎項）可成功，且 `CUSTOM_GACHA + SCRATCH_MODE` 下 `freeDrawThreshold=null` 可建立。
- [ ] 商品明細 API 不洩漏 `AVAILABLE` ticket 的獎品/revealed 資訊。
- [ ] 刮刮樂指定抽票時，回應 `ticketId` 必須等於 request 指定 UUID。
- [ ] 抽獎失敗（count 邊界/餘額不足/票券無效）不得發生扣款、扣庫存或 ticket 狀態改變。
- [ ] 抽獎成功時，`lottery_ticket`、`lottery_draw_record`、`wallet_transaction`、`consumption_record` 一致。
- [ ] 出貨建單（`/order/ship`）跨店拆單正確，且運費防竄改（shippingFee mismatch 必須拒絕）。
- [ ] 訂單狀態機（prepare -> ship -> complete）不可跳步，不可跨店越權。
- [ ] 付款 callback 成功可正確更新訂單付款狀態；錯誤 callback 不得汙染既有訂單。
- [ ] 一般玩家/未登入不可呼叫後台管理 API。
- [ ] audit log 查詢僅 ADMIN 可用，且關鍵流程可被追溯（抽獎/訂單/付款）。

## P1（核心流程）

- [ ] 前台商品列表/店家商品列表/熱門度更新可正常返回與更新。
- [ ] 刮刮樂開套者指定流程與非開套者等待流程（designation pending）行為一致。
- [ ] 賞品盒查詢、回收、出貨 API 行為與狀態轉移一致。
- [ ] 玩家查詢訂單列表/詳情/提交出貨資訊/取消訂單流程可用。
- [ ] 後台商品 CRUD、上下架、複製、查詢（含 with-prizes）可用。
- [ ] 後台報表 API（revenue/referral/recharge/bonus/member-growth/prize-shipment...）權限與資料範圍正確。
- [ ] 充值與錢包回呼（wallet recharge callback）流程可追蹤。

## P2（一般功能）

- [ ] 跑馬燈、Banner、News、Category、Enum、District、Store options 等公開 API 可回應且格式穩定。
- [ ] 聯絡我們、推薦碼驗證、推薦統計、消費記錄查詢等周邊 API 可用。
- [ ] 上傳 API（後台）功能與錯誤處理可用。
- [ ] system-log cleanup 回傳刪除筆數與實際 DB 刪除一致。

## 驗收紀錄欄（人工填寫）

| 日期 | 驗收人 | 環境 | 範圍 | 結果 | 阻擋上線問題 |
|------|--------|------|------|------|--------------|
|      |        |      |      |      |              |

