# AI 交接現況

最後更新：2026-05-11

## 0.1 2026-05-11（同步節點補充）

1. 使用者已明確要求：前後端需持續同步，且 AI 交接檔需持續更新。
2. 本工作區為後端 repo（`kuji-admin`），跨 repo 前端同步需在 `kuji-admin-web`（必要時含 `kuji-client`）接續執行。
3. 本輪結束前需完成版本控管節點：`commit + push`，避免接手上下文遺失。

## 0. 2026-05-11（本次會話）追加交接

### 使用者協作偏好（必須遵守）

1. 下一位 AI 必須先把要做的範圍一次討論完整，再進入一次性調整。
2. 交付節奏要以「一包一包完整落地」為主，避免斷斷續續的小段回報。
3. 每一包開始前要先列出：影響檔案、改動邊界、驗證方式；使用者確認後再改。
4. 不得在未說明全貌時直接擴散修改範圍。

### 本次已完成（後端）

1. `ReportServiceImpl` 已修正平台營收關鍵 correctness：
   - `storeBreakdown` 的店家映射改為三路來源（`related_id -> lottery.id`、`related_id -> lottery_ticket.id -> lottery.id`、`related_id -> order.id`）。
   - 多個統計查詢時間邊界改為半開區間（`>= start`、`< endExclusive`），降低日切換 off-by-one 風險。
2. `LotteryServiceImpl` 已完成：
   - 新架構 `tags`、`galleryImages` 儲存統一為 JSON。
   - 讀取時兼容 JSON + CSV（舊資料相容）。
   - 已修補先前語法斷裂造成的編譯阻斷。
3. 會員管理串接已做過一輪收斂（前端權限顯示 + 後端遮罩邏輯）並有實際改動紀錄。

### 編譯 / 驗證現況（覆蓋舊說明）

1. 本機可執行 Maven，但終端輸出偶發空白，導致成功訊號可讀性不穩。
2. 目前未見新的阻斷編譯錯誤；IDE 顯示多為既有規則警告（常數抽取、deprecated、Sonar 建議），非本輪新增阻斷。
3. 下一位 AI 仍需在本機再跑一次可追蹤的 compile / package，補齊可交付證據。

### 下一輪「一次性交付包」

第一包（建議直接執行）：會員主線封口（不跨 DB）

1. Token 失效閉環：登入 / refresh / filter / logout 的 `gen` 行為一致。
2. 會員 DTO 拆分：list / detail 契約分離，避免查詢頁與詳情頁共用過重欄位。
3. 契約同步：後端 `res` + 後台前端 service / page 一次對齊。
4. 驗證：
   - compile
   - 會員查詢/詳情/登出流程 smoke test（至少 API 層）
   - 變更檔案錯誤清單為零（阻斷級）。

第二包（第一包完成後）：平台營收報表補強

1. 補 `storeBreakdown` 與時間邊界測試案例。
2. 收斂 period 定義與欄位命名，避免前後端解讀差異。

### 第二包執行進度（2026-05-11，本輪已落地）

已完成：

1. 新增 `ReportServiceImpl` 單元測試：
   - `src/test/java/com/group/admin/service/ReportServiceImplTest.java`
2. 已驗證 `queryTotalRecharge` 使用半開區間（`created_at >= ?`、`created_at < ?`）。
3. 已驗證 `queryDailyAmountByType` 使用半開區間並保留 `coin_type` 條件。
4. 已驗證 `queryStoreBreakdown` SQL 含三路店家映射（`l_direct.store_id`、`l_ticket.store_id`、`o.store_id`）與半開區間。
5. 測試結果：`Tests run: 3, Failures: 0, Errors: 0, Skipped: 0`。
6. 已補會員認證 smoke 測試（API 層）：
   - 更新 `src/test/java/com/group/admin/controller/api/ApiAuthControllerTest.java`
   - 新增 refresh 關鍵情境：缺 claims、gen mismatch、成功旋轉 token
   - 測試結果：`Tests run: 8, Failures: 0, Errors: 0, Skipped: 0`

尚待下一步：

1. 若要進一步封口，可補 period 欄位命名一致性檢查（DTO/前端欄位對照）。
2. 後台前端契約同步仍需在 `kuji-admin-web` 工作區執行（本工作區僅後端）。

### 目前整體系統走向（給接手 AI）

1. 方向不是加新功能，而是主流程「封口」與跨端契約一致性。
2. 優先順序：
   - 正確性（交易/報表統計）
   - 安全性（token 失效與權限）
   - 可維護性（DTO 分層與欄位責任清楚）
3. 若任務涉及跨多檔/改行為，先討論邊界，再一次性落地。

## 1. 專案現況

目前專案主線已從「補功能」進入「主流程封口與一致性收斂」階段。

現階段最重要的主題有兩個：

1. 商品 / 抽獎生命週期已重整完成，正在補前後端一致性與驗證。
2. 各管理模組需要逐項盤點，確認功能是否符合產品預期、前後端是否一致、前台後台是否對得起來。

## 2. 已拍板的協作規則

以下規則視為長期有效，後續 AI 接手時必須遵守：

1. 先討論再改業務邏輯。
2. 只要牽涉前後端契約，一律同步看後端、後台前端、前台前端。
3. 不要只修表面 bug，要把整條流程與使用者操作路徑想完整。
4. 優先確保功能可用、狀態語意清楚、防呆一致。
5. 沒有明確批准前，不擴增新的商業語意。
6. 修改前要先告知會碰哪些 repo / 哪些檔案。
7. 若缺 UAT，先接受以 test、contract、code review、手動 code audit 作為過渡驗證方式。

## 3. 三個 Repo 與分支

### 後端

- repo：`C:\Users\KD\jimmy\kuji-admin`
- remote：`https://github.com/smile95137a/kuji-admin.git`
- branch：`feat/smtp-account-hardening`
- 最新 commit：`d332ea0`

### 後台前端

- repo：`C:\Users\KD\jimmy\kuji-admin-web`
- remote：`https://github.com/smile95137a/kuji-admin-web.git`
- branch：`main`
- 最新 commit：`5e22c5d`

### 前台前端

- repo：`C:\Users\KD\jimmy\kuji-client`
- remote：`https://github.com/smile95137a/kuji-client.git`
- branch：`main`
- 最新 commit：`e2144b1`

## 4. 商品 / 抽獎主流程已拍板規則

### 商品狀態模型

正式狀態模型：

- `DRAFT`
- `WAITING_ON_SHELF`
- `ON_SHELF`
- `OFF_SHELF`
- `GRAND_PRIZE_DRAWN`
- `ALL_DRAWN`
- `FORCED_OFF`
- `DELETED`

### 狀態規則

1. `ON_SHELF` 是唯一正常可抽狀態。
2. `FORCED_OFF` 回復路徑為 `OFF_SHELF`。
3. 刪除只能從 `DRAFT` 或 `OFF_SHELF` 進 `DELETED`。
4. `GRAND_PRIZE_DRAWN` 與 `ALL_DRAWN` 為系統狀態，不可由前端手動指定。

### 刮刮樂規則

刮刮樂已正式定義為：

1. 只能有 1 筆獎品。
2. 該筆必須是大獎。
3. `quantity = 1`
4. `level = GRAND`
5. 不允許其他非大獎獎品列。
6. 其餘格子全部視為系統推導的「謝謝惠顧」。

### 刮刮樂模式規則

1. `SCRATCH_STORE`
   - 後台只能指定 1 個大獎號碼。
   - 指定完成後才能上架 / 抽獎。
2. `SCRATCH_PLAYER`
   - 玩家只能指定 1 個號碼。
   - 指定完成後即可抽獎。
   - 前台詳情頁需顯示已指定的大獎號碼。

### 終態顯示語意

1. `GRAND_PRIZE_DRAWN`：顯示「大獎已抽完」
2. `ALL_DRAWN`：顯示「已售完」或「全數抽完」

## 5. 已完成的大項

### 帳號治理與安全

- 忘記密碼改為臨時密碼模式
- `forceChangePassword` 卡控
- 後台本人資料 API
- `StoreOwner` 僅能管理自己店內小編
- 錯誤格式收斂

### 商品建立與狀態模型

- 商品建立流程收斂到整合建立 / 編輯
- 上架前完整性驗證補強
- 獎品異動需先下架
- 商品狀態模型改為正式 8 狀態
- 前台 detail 開放 `ON_SHELF` / `GRAND_PRIZE_DRAWN` / `ALL_DRAWN`

### 刮刮樂封口

- 後端驗證只接受單一 `GRAND` 大獎
- 後台整合表單補防呆
- 舊獎品路由導回整合商品編輯頁
- 前台終態顯示已分開 `GRAND_PRIZE_DRAWN` 與 `ALL_DRAWN`

### 報表第一輪對齊

- 後台前端 route 補齊正式報表入口
- 後台報表 service 改為一對一對應正確 API
- 新增 `PlatformRevenueReport.vue`
- 後端 `DataInitializer` 已補報表選單收斂、舊 code/path 修正、角色可見性

## 6. 目前驗證限制

目前環境限制：

1. 目前可執行 `mvn`，但終端輸出偶發空白，建議用可回傳 exit code 的方式保留證據。
2. 目前無法在 UAT 環境做完整流程驗收。

因此現階段可接受的驗證方式是：

1. 單元測試 / 測試碼補強
2. Controller / Service / DTO / Mapper 契約檢查
3. 前後端 route / service / 顯示條件 code audit
4. 後續由使用者進行實際環境手測

## 7. 下一步優先順序

### 第一優先：報表逐張審查

目標不是只看「有沒有頁」，而是確認每張報表：

1. 查詢條件是否合理
2. 欄位是否符合營運理解
3. 聚合方式是否符合產品預期
4. 權限是否合理
5. 前後端命名與資料欄位是否一致

#### 已拍板的報表方向

1. 推薦碼報表
   - 核心用途是店家拓展與招商，不是會員拉新而已。
   - 目標是讓既有店家推薦下一個店家進駐平台。
   - 需要看到：
     - 推薦碼目前總量
     - 歷史成長量
     - 各期成長對比
     - 店家推薦成效

2. 會員成長報表
   - 核心用途是觀察前台會員整體成長，不是店家視角。
   - 需要看到：
     - 每月會員成長量
     - 淡旺季變化
     - 會員在平台的消費模式
     - 消費集中在哪些商品
     - 金幣扣除 / 紅利扣除
     - 儲值多寡
     - 使用哪種金流型態

3. 儲值 / 紅利報表
   - 儲值以平台 / admin 視角為主，不走店家視角。
   - 原因是實際金流收款在平台端。
   - 核心應觀察平台整體儲值狀況。

4. 抽獎結果報表
   - 核心用途是看客人實際抽到了哪些商品與獎項。
   - 需要看到：
     - 商品名稱
     - 中獎品項
     - 賞品圖片
     - 等級
     - 抽獎時間
     - 抽數

#### 本輪已拍板的報表重做決策

1. 推薦碼報表的「推薦成功」事件點
   - 正式定義為：`店家啟用成功`。
   - 不採用：
     - 店家建立：太早，尚未代表真正進駐。
     - 審核通過：仍可能尚未正式啟用。
     - 首次上架商品：太晚，會把招商成效與營運啟動混在一起。
   - 後續歷史成長、期別對比、店家推薦成效，全部都以「啟用成功日」作為歸屬日期。

2. 儲值 / 紅利 / 平台營收總覽的邊界
   - 正式定位為「同一個平台金流模組下的三張視圖」，不是互相重疊的三張獨立語意報表。
   - 邊界定義：
     - `平台營收總覽`：高層摘要，只看平台層 KPI，例如總儲值、總消耗、淨額、抽獎次數。
     - `儲值報表`：平台金流流入明細，聚焦儲值金額、儲值筆數、方案分布、支付型態。
     - `紅利報表`：平台補貼 / 贈點流向，聚焦發放來源、消耗情況、受益會員、期間變化。
   - `儲值報表` 與 `紅利報表` 不走店家視角。
   - 若前後端目前仍保留 storeId 或 StoreOwner 權限，後續應視為待拆除的舊設計殘留。

3. 抽獎結果報表主視圖
   - 正式採用「上方摘要 + 下方逐筆中獎明細」雙層設計。
   - 摘要區只保留：
     - 期間總抽數
     - 期間中獎件數
     - 熱門商品
   - 主體表格必須以實際中獎結果為核心，而不是獎池 / 籤位 / 商品池統計。
   - 明細欄位至少要能支撐：商品、品項、圖片、等級、抽獎時間、抽數。

#### 下一棒第一包實作邊界：推薦碼報表重定義

### 本輪已完成的前端同步

已同步 `kuji-admin-web` 第一包相關頁面，重點如下：

1. 店家建立頁已新增 `referralCode` 輸入欄位。
2. 店家編輯頁已新增招商推薦碼、推薦來源店家、啟用成功時間顯示。
3. 推薦來源在前端已配合後端規則，啟用成功後視為鎖定不可改。
4. 店家詳情頁已補招商追蹤區塊。
5. 店家列表頁已補推薦來源店家、推薦碼、啟用成功時間欄位。
6. `ReferralReport.vue` 已由舊的會員推薦碼獎勵視圖，改為店薦店招商報表視圖。

### 前台前端目前判定

已檢查 `kuji-client` 的店家 service / detail 顯示流程。

1. 前台 `storeService.ts` 本身已對 `name/storeName` 做 normalize。
2. 本輪新增的招商追蹤欄位對前台屬於非破壞性增量。
3. 因此前台本輪先不改碼，後續若產品要把招商資訊顯示到前台，再另開需求討論。

### 本輪已完成的報表第二包（會員成長 / 儲值 / 紅利 / 抽獎結果）

已同步 `kuji-admin-web` 與後端新契約，重點如下：

1. `MemberGrowthReport.vue`
   - 改為平台視角，不再使用店家篩選。
   - 新增 4 組分布圖：消費模式、商品集中度、金幣/紅利消耗、支付型態。
   - 匯出補齊：摘要 + 每日新增 + 各分布明細。

2. `RechargeReport.vue`
   - 移除店家查詢語意，改為平台統計。
   - 保留每日趨勢圖，新增方案分布圖。
   - 匯出補齊：摘要 + 方案統計 + 每日明細。

3. `BonusReport.vue`
   - 保留每日趨勢圖，新增贈點類型分布圖。
   - 匯出補齊：摘要 + 類型統計 + 每日明細。

4. `LotteryResultReport.vue`
   - 已由舊的 `prizeStats/lotteryStats` 改為新 DTO：`totalDraws/totalWinningCount/hotLotteries/winningDetails`。
   - 視圖改為：摘要 + 熱門商品圖 + 逐筆中獎明細。
   - 匯出補齊：摘要 + 熱門商品 + 中獎明細。

5. `adminReportService.ts`
   - `QueryReq.condition` 改為可選。
   - `postReport` 支援可空 request body。
   - 補上 `MemberGrowthReportRes`、`LotteryResultReportRes` 型別。

本輪限制與提醒：

1. 本機仍無 mvn/java，後端僅做靜態錯誤檢查。
2. 前端已對變更檔案執行 errors 檢查，當前無語法/型別錯誤。
3. 尚未做實際瀏覽器 UAT，建議下一棒優先做 4 張報表手測與 CSV 欄位驗收。

下一位 AI / CLI 接手時，請先做這一包，不要直接同時改 4 張：

1. 先定義推薦碼報表的 source of truth
   - 目前程式內同時存在：
     - `user.referral_code` / `user.referred_store_id`
     - `referral_code` / `referral_record`
   - 必須先決定招商報表究竟以哪一套資料為主，避免混血報表。

2. 先收斂報表語意，再動 DTO / SQL / 前端
   - 這張報表要看的是「店薦店招商成效」，不是會員拉新。
   - 若現有欄位、條件、排行榜仍是會員推薦碼語意，應視為待淘汰結構，不要硬補欄位延用。

3. 第一包最小交付內容
   - 報表用途定義
   - 成功事件點定義
   - 時間歸屬規則
   - 權限邊界
   - 條件欄位草案
   - 回應欄位草案
   - 前後端契約影響點

4. 第一包暫時不要做的事
   - 不要順手一起重寫會員成長、儲值、抽獎結果報表。
   - 不要在 source of truth 未拍板前就開始補 SQL。
   - 不要因為現有 `ReferralReportCondition` 名稱可用，就假設其 storeId 設計仍然正確。

#### 第一包目前已開始實作的內容（2026-05-11）

已落地但尚未做 DB 套用 / 完整整合驗證：

1. 已新增 migration 草稿：
   - `store.referrer_store_id`
   - `store.referral_code_id`
   - `store.activated_at`

2. 已開始把店家建立 / 更新 / 啟用流程改成支援招商來源：
   - 建立店家可帶 `referralCode`
   - 後端會解析推薦碼並寫入推薦來源店家
   - 店家首次啟用時會補 `activatedAt`
   - 啟用成功後不可再修改推薦來源

3. `POST /admin/report/referral` 已改為 `ADMIN only`，不再沿用 StoreOwner 視角。

4. `ReferralReportRes` 已改成招商報表語意：
   - 推薦碼總數
   - 啟用中的推薦碼總數
   - 歷史累計成功招商店數
   - 本期 / 上期成功招商店數
   - 每日招商啟用明細
   - 各推薦店家招商成效

5. 目前採用的實作前提：
   - `referral_code / referral_record` 不再直接拿來當玩家拉新報表用途延伸。
   - 店薦店招商以 `store.referrer_store_id + store.referral_code_id + store.activated_at` 為正式資料基礎。
   - 舊資料 `activated_at` 暫不強行回填歷史真值，後續以新資料為主要準確口徑。

6. 尚未完成：
   - 實際套用 DB migration
   - 重新跑 MBG / compile
   - 後台前端欄位與建立店家表單對接
   - 推薦碼管理頁與招商報表頁的前端欄位同步

### 第二優先：管理模組巡檢

巡檢方式：

1. 後端 controller / service 真相
2. 後台前端頁面 / route / service 是否對齊
3. 前台是否會看到相衝突結果
4. 只回報有問題、待拍板、待補完整性的地方

## 8. 目前最值得注意的風險

1. 報表雖已完成第一輪 route / menu / API 對齊，但尚未逐張確認「呈現內容是否符合產品預期」。
2. 目前尚未做 compile / build / UAT 驗證，不能宣稱已完全驗收。
3. 舊文件 `docs/AI_HANDOFF_2026-05-10.md` 存在編碼異常，本檔為目前應優先參考的交接來源。
