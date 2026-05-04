# KUJI-Server 前台畫面 User Story 驗收清單

這份文件要呈現什麼內容：
提供給「前台驗收人員」使用的畫面導向 User Story 清單。每一個故事都從玩家實際操作出發，說明要去哪個頁面、做什麼、成功與失敗時畫面應該長什麼樣子，並標出背後對應 API，方便前端或 QA 用 Network 面板協助核對。

## 這份清單給誰用

- 前台驗收人員：照畫面操作並留存證據
- 前台前端：核對這份清單是否與目前 UI 一致
- QA：必要時協助打開瀏覽器 DevTools 檢查 Network

## 證據留存規則

每一個故事都至少要留下以下證據：

1. 操作前截圖
2. 操作後截圖
3. 成功提示或失敗提示截圖
4. 若驗收方式包含 `UI+N`，再加一張瀏覽器 DevTools Network 截圖

## 代號說明

| 代號 | 說明 |
|------|------|
| UI | 只靠畫面即可驗收 |
| UI+N | 需要畫面加 DevTools Network 面板 |
| BE | 不能只靠畫面，需後端或 DB 協助 |
| P0 | 不通過不可上線 |
| P1 | 核心流程 |
| P2 | 一般流程 |

## 前台驗收故事清單

### CUI-001 商品列表顯示正確

- 驗收角色：未登入訪客、已登入玩家
- 驗收頁面：前台商品列表頁
- 對應前端文件：[frontend/client/03-lottery-browse.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/03-lottery-browse.md)
- 驗收人員要做什麼：
  1. 進入商品列表頁
  2. 切換分類、關鍵字、店家或排序
  3. 觀察商品卡片內容
- 應看到什麼：
  1. 只看到上架商品
  2. 卡片資訊完整，至少包含商品名稱、圖片、價格、剩餘抽數
  3. 切換條件後列表有跟著更新
- 背後對應 API：`POST /api/lottery/browse/list`
- 驗收方式：`UI+N`
- 優先級：`P1`

### CUI-002 商品詳情頁資料完整且未提前洩漏答案

- 驗收角色：未登入訪客、已登入玩家
- 驗收頁面：商品詳情頁
- 對應前端文件：[frontend/client/03-lottery-browse.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/03-lottery-browse.md)
- 驗收人員要做什麼：
  1. 點進任一上架商品
  2. 查看商品介紹、獎項列表、票格區塊
  3. 不進行抽獎，只觀察內容
- 應看到什麼：
  1. 詳情頁資料完整顯示
  2. 尚未抽過的票格不能直接看到獎項名稱、揭露號碼或中獎內容
  3. 未登入時若有登入限制資訊，提示要清楚
- 背後對應 API：`GET /api/lottery/browse/{id}/detail`
- 驗收方式：`UI+N`
- 優先級：`P0`

### CUI-003 刮刮樂票格在抽之前不可先看到結果

- 驗收角色：已登入玩家
- 驗收頁面：刮刮樂商品詳情頁
- 對應前端文件：[frontend/client/03-lottery-browse.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/03-lottery-browse.md)、[frontend/client/04-draw-flow.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/04-draw-flow.md)
- 驗收人員要做什麼：
  1. 進入刮刮樂商品
  2. 觀察所有可選票格
  3. 確認未刮開前的畫面
- 應看到什麼：
  1. 票格只顯示位置、狀態或可點選樣式
  2. 不應直接出現獎項名稱、revealed number、是否大獎
- 背後對應 API：`GET /api/lottery/browse/{id}/detail`、`GET /api/lottery/{id}/tickets`
- 驗收方式：`UI+N`
- 優先級：`P0`

### CUI-004 開套玩家需先完成指定流程才能開始抽

- 驗收角色：已登入玩家，且為 opener
- 驗收頁面：刮刮樂指定大獎畫面或 Modal
- 對應前端文件：[frontend/client/04-draw-flow.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/04-draw-flow.md)
- 驗收人員要做什麼：
  1. 進入 `SCRATCH_PLAYER` 類型商品
  2. 觸發指定大獎流程
  3. 選擇大獎對應位置後送出
- 應看到什麼：
  1. 指定前不可直接進入一般抽獎
  2. 指定完成後畫面切換為可抽狀態
  3. 成功提示清楚可見
- 背後對應 API：`POST /api/lottery/draw/{lotteryId}/designate`、`GET /api/lottery/draw/{lotteryId}/session`
- 驗收方式：`UI+N`
- 優先級：`P0`

### CUI-005 非開套玩家在指定完成前不能偷跑抽獎

- 驗收角色：已登入玩家，且非 opener
- 驗收頁面：同一商品詳情頁
- 對應前端文件：[frontend/client/04-draw-flow.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/04-draw-flow.md)
- 驗收人員要做什麼：
  1. 使用第二個帳號進入同一商品
  2. 在 opener 還沒指定完成前嘗試抽獎
- 應看到什麼：
  1. 畫面顯示等待 opener 指定
  2. 抽獎按鈕不可用，或送出後顯示等待訊息
- 背後對應 API：`GET /api/lottery/draw/{lotteryId}/session`、`POST /api/lottery/draw/{lotteryId}/draw`
- 驗收方式：`UI+N`
- 優先級：`P0`

### CUI-006 點哪一格就回哪一格，不能票格錯位

- 驗收角色：已登入玩家
- 驗收頁面：刮刮樂抽獎畫面
- 對應前端文件：[frontend/client/04-draw-flow.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/04-draw-flow.md)
- 驗收人員要做什麼：
  1. 記下要點擊的票格位置與票號
  2. 點選該票格並抽獎
  3. 對照結果視窗顯示內容
- 應看到什麼：
  1. 回傳的票格、票號、結果都對應同一張票
  2. 不可發生點第 1 格卻顯示第 42 格的情況
- 背後對應 API：`POST /api/lottery/draw/{lotteryId}/draw`
- 驗收方式：`UI+N`
- 優先級：`P0`

### CUI-007 抽獎成功後畫面要立即更新

- 驗收角色：已登入玩家
- 驗收頁面：抽獎結果視窗與商品詳情頁
- 對應前端文件：[frontend/client/04-draw-flow.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/04-draw-flow.md)
- 驗收人員要做什麼：
  1. 完成一次成功抽獎
  2. 關閉結果視窗
  3. 觀察票格狀態、抽獎紀錄與剩餘數量
- 應看到什麼：
  1. 結果視窗顯示獎項、揭露號碼與結果文案
  2. 已抽票格不能再抽
  3. 剩餘抽數或票格狀態有同步更新
- 背後對應 API：`POST /api/lottery/draw/{lotteryId}/draw`、`GET /api/lottery/{id}/tickets`
- 驗收方式：`UI+N`
- 優先級：`P0`

### CUI-008 抽獎失敗時不能偽裝成成功

- 驗收角色：已登入玩家
- 驗收頁面：抽獎畫面
- 對應前端文件：[frontend/client/04-draw-flow.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/04-draw-flow.md)
- 驗收人員要做什麼：
  1. 使用餘額不足帳號嘗試抽獎
  2. 或操作已被抽走的票格
  3. 觀察畫面更新
- 應看到什麼：
  1. 明確失敗提示
  2. 票格狀態不應被錯誤改成已抽
  3. 賞品盒不應新增不該有的獎品
- 背後對應 API：`POST /api/lottery/draw/{lotteryId}/draw`
- 驗收方式：`UI+N`
- 優先級：`P0`

### CUI-009 抽中實體獎品後要出現在賞品盒

- 驗收角色：已登入玩家
- 驗收頁面：賞品盒頁
- 對應前端文件：[frontend/client/05-prize-box.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/05-prize-box.md)
- 驗收人員要做什麼：
  1. 抽到一個實體獎品
  2. 進入賞品盒頁
  3. 檢查最新獎品資料
- 應看到什麼：
  1. 新獎品已出現在賞品盒
  2. 獎品名稱、店家、等級、時間資訊合理
- 背後對應 API：`GET /api/prize-box`、`GET /api/prize-box/summary`
- 驗收方式：`UI+N`
- 優先級：`P1`

### CUI-010 賞品回收後紅利要正確增加

- 驗收角色：已登入玩家
- 驗收頁面：賞品盒頁、個人錢包頁
- 對應前端文件：[frontend/client/05-prize-box.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/05-prize-box.md)
- 驗收人員要做什麼：
  1. 勾選一筆可回收獎品
  2. 點擊回收並確認
  3. 回到賞品盒與錢包頁查看變化
- 應看到什麼：
  1. 回收成功提示
  2. 紅利有增加
  3. 已回收獎品不再留在可操作清單
- 背後對應 API：`POST /api/prize-box/recycle`、`GET /api/prize-box`、`GET /api/user/me`
- 驗收方式：`UI+N`
- 優先級：`P1`

### CUI-011 賞品盒出貨可以正確建立訂單

- 驗收角色：已登入玩家
- 驗收頁面：賞品盒頁、出貨 Modal
- 對應前端文件：[frontend/client/05-prize-box.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/05-prize-box.md)、[frontend/client/06-order-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/06-order-management.md)
- 驗收人員要做什麼：
  1. 勾選同店家的可出貨獎品
  2. 開啟出貨視窗
  3. 選擇配送方式並填寫資料
  4. 送出出貨申請
- 應看到什麼：
  1. 成功後 Modal 關閉
  2. 賞品盒列表重新整理
  3. 後續可在訂單頁看到新訂單
- 背後對應 API：`GET /api/shipping-methods`、`POST /api/prize-box/ship`
- 驗收方式：`UI+N`
- 優先級：`P0`

### CUI-012 出貨欄位驗證要依配送方式改變

- 驗收角色：已登入玩家
- 驗收頁面：出貨 Modal
- 對應前端文件：[frontend/client/05-prize-box.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/05-prize-box.md)
- 驗收人員要做什麼：
  1. 選宅配但故意不填地址
  2. 選超商取貨但故意不填門市資料
  3. 分別送出
- 應看到什麼：
  1. 畫面要擋下錯誤送出
  2. 顯示明確欄位驗證訊息
  3. 不應建立訂單
- 背後對應 API：`GET /api/shipping-methods`、`POST /api/prize-box/ship`
- 驗收方式：`UI+N`
- 優先級：`P0`

### CUI-013 訂單列表與詳情可看到剛建立的訂單

- 驗收角色：已登入玩家
- 驗收頁面：訂單列表頁、訂單詳情頁
- 對應前端文件：[frontend/client/06-order-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/06-order-management.md)
- 驗收人員要做什麼：
  1. 完成一筆出貨建單
  2. 前往訂單列表
  3. 點開該筆訂單詳情
- 應看到什麼：
  1. 列表有新訂單
  2. 詳情有收件資訊、配送方式、運費、商品內容
- 背後對應 API：`POST /api/order/list`、`GET /api/order/{orderId}`
- 驗收方式：`UI+N`
- 優先級：`P1`

### CUI-014 訂單在可編輯狀態時可以補填物流資訊

- 驗收角色：已登入玩家
- 驗收頁面：訂單詳情頁
- 對應前端文件：[frontend/client/06-order-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/06-order-management.md)
- 驗收人員要做什麼：
  1. 開啟一筆仍可編輯的訂單
  2. 修改收件資訊
  3. 送出並重新整理頁面
- 應看到什麼：
  1. 成功提示清楚
  2. 重新整理後仍顯示新資料
- 背後對應 API：`POST /api/order/{orderId}/shipping-info`、`GET /api/order/{orderId}`
- 驗收方式：`UI+N`
- 優先級：`P1`

### CUI-015 付款完成後訂單狀態不能停在待付款

- 驗收角色：已登入玩家
- 驗收頁面：付款完成頁、訂單詳情頁
- 對應前端文件：[frontend/client/06-order-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/06-order-management.md)
- 驗收人員要做什麼：
  1. 建立一筆需要付款運費的訂單
  2. 完成付款
  3. 回到訂單頁並刷新
- 應看到什麼：
  1. 訂單付款狀態已前進
  2. 不可長時間停在 `PAYMENT_PENDING`
- 背後對應 API：`GET /api/order/{orderId}`、付款 callback 為後端流程
- 驗收方式：`UI+N / BE`
- 優先級：`P0`

## 給前台前端的 UI 對照提醒

前台前端在核對這份文件時，請逐項確認：

1. 頁面路由或入口是否存在
2. 畫面是否真的有對應按鈕、Modal、結果視窗
3. 成功與失敗提示文案是否可被截圖保存
4. DevTools Network 是否能明確看到對應 API
5. 刮刮樂結果畫面是否真的顯示 `ticketNumber` 或其他可核對票格一致性的資訊
6. 出貨 Modal 是否真的能依配送方式切換必填欄位

若任一項目前台 UI 尚未實作，請直接在前端 review 中標記「畫面尚未對應」，不要讓驗收人員自行猜測。
