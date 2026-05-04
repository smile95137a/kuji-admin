# KUJI-Server 畫面導向 User Story 驗收清單

這份文件要呈現什麼內容：
提供給「主要透過前端畫面操作」的人類驗收人員使用的 User Story 驗收清單。每個驗收故事都從畫面操作出發，說明要在哪個頁面做什麼、畫面應該看到什麼、背後對應哪些 API，以及哪些項目不能只靠看畫面驗收。

## 先說結論

如果驗收人員只看得到畫面，那不應該直接拿「API 清單」做驗收，因為他不知道：

- 這支 API 對應哪個畫面
- 要操作哪個按鈕才會打到這支 API
- 成功與失敗在 UI 上應該長什麼樣子

比較正確的做法是把驗收拆成三層：

1. `畫面驗收`
   驗收人員只看 UI，確認操作流程、畫面訊息、狀態變化是否正確。
2. `畫面 + Network 驗收`
   驗收人員用瀏覽器 DevTools 的 Network 面板，確認該畫面操作背後真的有打到正確 API，且 request/response 合理。
3. `純後端驗收`
   像付款 callback、system log cleanup、部分 audit log、排程、跨服務異常等，無法只靠畫面驗收，必須由後端或 QA 工具協助。

所以方向不是「只給前端」，而是：

- 給人類驗收人員：這份「畫面導向 User Story 清單」
- 給前端/QA/工程：前面已產出的 API 驗收清單
- 給後端/自動化：P0 API contract / DB consistency 驗收

## 驗收方式代號

| 代號 | 說明 |
|------|------|
| UI | 只靠畫面即可驗收 |
| UI+N | 需要畫面 + 瀏覽器 Network 面板 |
| BE | 需要後端、DB、callback、log 或測試工具協助，不能只靠畫面 |
| P0 | 不通過不可上線 |
| P1 | 核心流程 |
| P2 | 一般流程 |

## 畫面導向 User Story 驗收表

| id | user_story | role | 驗收頁面/入口 | 人類實際操作步驟 | 畫面上應看到的結果 | 背後對應 API | 驗收方式 | priority |
|----|------------|------|---------------|------------------|--------------------|--------------|----------|----------|
| HUS-001 | 作為訪客，我想看到商品列表，確認前台有可購買的商品 | 未登入/已登入玩家 | 前台商品列表頁 | 1. 進入首頁或商品列表頁 2. 切換分類/主題/排序 3. 觀察商品卡片 | 只看到上架商品；卡片資訊完整；無已下架商品混入 | `POST /api/lottery/browse/list`、`GET /api/stores`、`GET /api/category/*` | UI+N | P1 |
| HUS-002 | 作為訪客，我想進入商品詳情頁，看到商品、獎項與目前可抽票況 | 未登入/已登入玩家 | 商品詳情頁 | 1. 點進任一上架商品 2. 觀察商品介紹、獎項列表、ticket 區塊 3. 確認未登入時的提示 | 詳情頁資料完整；未抽 ticket 不應出現獎品名稱/圖片/revealed number；未登入時顯示請先登入 | `GET /api/lottery/browse/{id}/detail` | UI+N | P0 |
| HUS-003 | 作為玩家，我想看到刮刮樂格子可選，且未刮開前不能提前知道答案 | 已登入玩家 | 刮刮樂商品詳情頁 | 1. 進入刮刮樂商品 2. 觀察票格 3. 不抽獎只查看畫面 | 格子只能看到可選狀態與位置，不應直接看到中獎內容 | `GET /api/lottery/browse/{id}/detail`、`GET /api/lottery/{id}/tickets` | UI+N | P0 |
| HUS-004 | 作為刮刮樂開套玩家，我想先指定大獎位置，完成後才能開始抽 | 已登入玩家（開套者） | 刮刮樂指定大獎畫面/Modal | 1. 進入 `SCRATCH_PLAYER` 商品 2. 觸發指定流程 3. 選擇大獎對應號碼 4. 送出 | 指定完成後畫面進入可抽狀態；系統顯示指定成功或已完成 | `POST /api/lottery/{lotteryId}/designate`、`GET /api/lottery/{lotteryId}/session` | UI+N | P0 |
| HUS-005 | 作為非開套玩家，我在開套玩家還沒指定前，不應該可以開始抽 | 已登入玩家（非開套者） | 同一刮刮樂商品頁 | 1. 用第二帳號進入同一商品 2. 在指定未完成前嘗試抽獎 | 畫面應顯示等待開套玩家指定，不可直接抽 | `GET /api/lottery/{lotteryId}/session`、`POST /api/lottery/{lotteryId}/draw` | UI+N | P0 |
| HUS-006 | 作為玩家，我點選某一張刮刮樂票時，回來的結果必須是我點的那一張 | 已登入玩家 | 刮刮樂抽獎畫面 | 1. 記下畫面上被點擊的 ticket 位置/編號 2. 點選該格抽獎 3. 對照抽獎結果畫面 | 結果顯示的票格、揭露號碼、獎項必須對應同一格；不能發生點第 1 格卻回第 42 格 | `POST /api/lottery/{lotteryId}/draw` 或 `POST /api/lottery/draw/{lotteryId}/draw` | UI+N | P0 |
| HUS-007 | 作為玩家，我抽獎成功後，畫面要立刻顯示結果且剩餘票格狀態正確 | 已登入玩家 | 商品詳情頁/抽獎結果 UI | 1. 抽一次獎 2. 關閉結果視窗 3. 觀察剩餘票格與抽獎紀錄 | 成功結果顯示獎項、revealed number、免單或最後賞訊息；已抽格子不可再抽 | `POST /api/lottery/{lotteryId}/draw`、`GET /api/lottery/{lotteryId}/tickets` | UI+N | P0 |
| HUS-008 | 作為玩家，我在餘額不足或票格無效時，畫面要明確失敗且不能像成功一樣更新 | 已登入玩家 | 抽獎畫面 | 1. 用餘額不足帳號抽獎 2. 或選已被抽走的票 3. 觀察畫面與列表更新 | 畫面要顯示失敗訊息；票格狀態不應錯誤更新；賞品盒不應多出資料 | `POST /api/lottery/{lotteryId}/draw` | UI+N | P0 |
| HUS-009 | 作為玩家，我抽到實體獎品後，應在賞品盒看到新獎品 | 已登入玩家 | 賞品盒頁 | 1. 完成一次中實體獎品的抽獎 2. 進入賞品盒 3. 檢查最新資料 | 抽到的獎品出現在賞品盒；資料正確；不可回收/可出貨標記合理 | `GET /api/prize-box`、`GET /api/prize-box/summary` | UI+N | P1 |
| HUS-010 | 作為玩家，我想把賞品盒中的可回收獎品回收成紅利 | 已登入玩家 | 賞品盒頁 | 1. 勾選可回收獎品 2. 點回收 3. 確認成功提示 4. 回到錢包/賞品盒 | 紅利增加；被回收的獎品不再出現在可操作清單 | `POST /api/prize-box/recycle`、`GET /api/user/me`、`GET /api/prize-box` | UI+N | P1 |
| HUS-011 | 作為玩家，我想把賞品盒中的獎品送出，系統要能選配送方式並建立訂單 | 已登入玩家 | 賞品盒頁 -> 出貨 modal | 1. 勾選可出貨獎品 2. 開出貨視窗 3. 選配送方式與填資料 4. 送出 | 成功後 modal 關閉、列表 reload、訂單建立成功或跳轉付款/顯示付款資訊 | `POST /api/prize-box/ship` 或 `POST /api/order/ship`、`GET /api/shipping-methods` | UI+N | P0 |
| HUS-012 | 作為玩家，我選宅配或超商時，畫面要要求對應欄位，不可亂送 | 已登入玩家 | 出貨 modal | 1. 選宅配但不填地址 2. 選超商但不填店號 3. 分別送出 | 畫面應顯示欄位驗證錯誤；不應關閉 modal；不應建立訂單 | `GET /api/shipping-methods`、`POST /api/prize-box/ship` / `POST /api/order/ship` | UI+N | P0 |
| HUS-013 | 作為玩家，我送出出貨後，要能在訂單列表看到新訂單 | 已登入玩家 | 訂單列表頁 | 1. 完成出貨建單 2. 前往訂單列表 3. 點進詳情 | 列表出現新訂單；詳情有配送方式、收件資訊、狀態、商品內容 | `POST /api/order/list`、`GET /api/order/{orderId}` | UI+N | P1 |
| HUS-014 | 作為玩家，我在訂單尚未進入不可修改狀態前，應可補填或修改出貨資訊 | 已登入玩家 | 訂單詳情頁 | 1. 開啟可編輯訂單 2. 修改配送資訊 3. 送出 | 畫面更新最新資訊；重新整理後仍一致 | `POST /api/order/{orderId}/shipping-info`、`GET /api/order/{orderId}` | UI+N | P1 |
| HUS-015 | 作為玩家，我在訂單尚未進入後續狀態前，應可取消訂單 | 已登入玩家 | 訂單詳情頁 | 1. 開啟待取消訂單 2. 點取消 3. 輸入原因（若 UI 有） 4. 確認 | 畫面顯示取消成功；列表狀態更新；不可再出貨 | `DELETE /api/order/{orderId}/cancel`、`POST /api/order/list` | UI+N | P1 |
| HUS-016 | 作為玩家，我完成付款後，訂單畫面應反映付款成功，而不是卡在待付款 | 已登入玩家 | 訂單詳情頁 / 付款後返回頁 | 1. 建立待付款訂單 2. 完成付款流程 3. 回到訂單頁刷新 | 畫面顯示付款成功或狀態前進；不可永遠停在 `PAYMENT_PENDING` | `POST /api/payment/callback`（背後） 、`GET /api/order/{orderId}` | UI+N / BE | P0 |
| HUS-017 | 作為後台商品管理者，我要能建立、編輯、上下架、複製商品 | Admin/StoreOwner/StoreEditor | 後台商品管理頁 | 1. 新增商品 2. 編輯商品 3. 上下架 4. 複製商品 5. 查詢列表 | 畫面操作後資料正確回顯；列表篩選與狀態標示一致 | `POST/PUT/GET /api/admin/lottery*`、`/api/admin/lottery/with-prizes*` | UI+N | P1 |
| HUS-018 | 作為後台商品管理者，我要能在編輯商品時維護獎項清單 | Admin/StoreOwner/StoreEditor | 後台商品編輯頁 | 1. 新增獎項 2. 修改獎項數量/權重 3. 儲存 4. 重新進入頁面檢查 | 獎項資料正確保存與回顯；數量/排序合理 | `POST/PUT/GET /api/admin/lottery/with-prizes*`、`/api/admin/lottery-with-prizes*` | UI+N | P1 |
| HUS-019 | 作為店家管理者，我只能看到自己店家的商品與訂單，不應該看到別家的 | StoreOwner/StoreEditor | 後台商品列表、訂單列表 | 1. 登入店家帳號 2. 查商品列表 3. 查訂單列表 4. 嘗試用網址進入他店資料 | 畫面只顯示自己店家資料；跨店網址要被擋下或查不到 | `/api/admin/lottery/list`、`/api/admin/orders/list`、明細 API | UI+N | P0 |
| HUS-020 | 作為後台訂單管理者，我要能依序完成備貨、出貨、完成 | Admin/StoreOwner/StoreEditor | 後台訂單管理頁 | 1. 找到待處理訂單 2. 點備貨完成 3. 填物流單號出貨 4. 標記完成 | 狀態依序前進；錯誤操作時畫面要阻擋 | `PUT /api/admin/orders/{orderId}/prepare`、`/ship`、`/complete` | UI+N | P0 |
| HUS-021 | 作為後台管理者，我要能查報表，且不同角色看到的範圍正確 | Admin/StoreOwner | 後台報表頁 | 1. 切換報表條件 2. 查詢多種報表 3. 用不同角色登入比較 | 畫面顯示報表資料；店家角色不應看到全平台資料 | `/api/admin/report/*` | UI+N | P1 |
| HUS-022 | 作為後台管理者，我要能查 system log / audit log，至少知道敏感操作可追蹤 | Admin | 後台系統日誌頁（若有） | 1. 執行一筆敏感操作 2. 進入日誌頁查詢對應時間區間 3. 搜尋使用者/類型 | 能查到對應記錄、時間、使用者與操作類型 | `/api/admin/system-log/type/{logType}`、`/user/{userId}`、`/date-range` | UI+N / BE | P0 |

## 不能只靠畫面驗收的項目

以下項目如果驗收人員只看得到畫面，無法單獨判定是否真的正確：

- `付款 callback`
  畫面只能看到付款後的結果，無法證明 callback payload、冪等性、失敗重送是否正確。
- `audit log cleanup`
  需要看 DB 或 log 查詢結果，畫面通常不會直接顯示「哪些舊資料被刪除」。
- `DB 一致性`
  例如抽獎是否真的只扣一次、ticket 是否只改一張、order 是否按店拆單，不能只看畫面。
- `權限繞過`
  單純看到一個頁面被擋住，不代表 API 層一定安全，最好仍要搭配 Network 看 401/403。

## 給驗收人員的實際操作建議

### 如果只會看畫面

- 優先使用本文件的 `UI` 類案例。
- 每一個故事都截圖：
  - 操作前
  - 操作後
  - 成功提示或失敗提示

### 如果會打開瀏覽器 DevTools

- 額外查看 Network：
  - request URL 是否正確
  - HTTP method 是否正確
  - status code 是否合理
  - response message 是否符合 UI 提示

### 如果是 P0 案例

- 建議至少由 2 種角色交叉驗收：
  - 一位從畫面驗
  - 一位從 Network / 後端資料驗

## 建議你接下來怎麼做

如果你的驗收人員真的只看畫面，我建議流程是：

1. 用這份文件做第一輪人工驗收
2. 把 `UI+N` 與 `BE` 的項目交給前端工程師或 QA 協助
3. 把 `P0` 的 DB 一致性與 callback 類項目交給後端或自動化驗收

這樣分工會比把 295 支 API 全部丟給只看畫面的人有效得多。

