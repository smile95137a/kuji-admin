# KUJI-Server 後台畫面 User Story 驗收清單

這份文件要呈現什麼內容：
提供給「後台驗收人員」使用的畫面導向 User Story 清單。每一個故事都從後台操作出發，說明要在哪個頁面做什麼、畫面應該看到什麼、哪些操作會影響前台玩家，以及背後對應哪些 API。

## 這份清單給誰用

- 後台驗收人員：照頁面流程操作
- 後台前端：核對 UI 是否與文件一致
- QA：必要時協助開啟 DevTools Network

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

## 後台驗收故事清單

### AUI-001 可建立商品並正確回顯

- 驗收角色：Admin、StoreOwner、StoreEditor
- 驗收頁面：後台商品管理頁
- 對應前端文件：[frontend/admin/05-product-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/05-product-management.md)
- 驗收人員要做什麼：
  1. 進入商品管理頁
  2. 建立一筆商品並填入必要資料
  3. 儲存後回到列表並重新打開詳情
- 應看到什麼：
  1. 商品建立成功並出現在列表
  2. 重新進入編輯頁後資料與剛才輸入一致
- 背後對應 API：`POST /api/admin/lottery-with-prizes`、`GET /api/admin/lottery-with-prizes/{id}`、`POST /api/admin/lottery/list`
- 驗收方式：`UI+N`
- 優先級：`P1`

### AUI-002 商品的獎項清單可建立、修改並正確回顯

- 驗收角色：Admin、StoreOwner、StoreEditor
- 驗收頁面：商品編輯頁
- 對應前端文件：[frontend/admin/05-product-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/05-product-management.md)
- 驗收人員要做什麼：
  1. 新增多筆獎項
  2. 修改獎項數量、等級、排序或大獎標記
  3. 儲存後重新開啟頁面
- 應看到什麼：
  1. 獎項資料正確保存
  2. 回到編輯頁後獎項列表與剛才設定一致
- 背後對應 API：`POST /api/admin/lottery-with-prizes`、`PUT /api/admin/lottery-with-prizes/{id}`、`GET /api/admin/lottery-with-prizes/{id}`
- 驗收方式：`UI+N`
- 優先級：`P1`

### AUI-003 商品上下架後前台可見範圍要正確改變

- 驗收角色：Admin、StoreOwner、StoreEditor
- 驗收頁面：商品列表頁、商品編輯頁
- 對應前端文件：[frontend/admin/05-product-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/05-product-management.md)
- 驗收人員要做什麼：
  1. 選一筆商品進行上架或下架
  2. 儲存後刷新列表
  3. 由前台再檢查商品可見性
- 應看到什麼：
  1. 後台狀態 Badge 或欄位有更新
  2. 前台商品列表同步反映是否可見
- 背後對應 API：`PUT /api/admin/lottery-with-prizes/{id}`、`POST /api/admin/lottery/list`、前台 `POST /api/lottery/browse/list`
- 驗收方式：`UI+N`
- 優先級：`P0`

### AUI-004 店家角色只能看到自己店家的商品

- 驗收角色：StoreOwner、StoreEditor
- 驗收頁面：後台商品列表頁
- 對應前端文件：[frontend/admin/02-roles-and-permissions.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/02-roles-and-permissions.md)、[frontend/admin/05-product-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/05-product-management.md)
- 驗收人員要做什麼：
  1. 用店家帳號登入
  2. 查詢商品列表
  3. 嘗試搜尋或切換不屬於自己的店家資料
- 應看到什麼：
  1. 只能看到自己店家的商品
  2. 不可透過畫面或網址看到其他店家資料
- 背後對應 API：`POST /api/admin/lottery/list`、`GET /api/admin/lottery-with-prizes/{id}`
- 驗收方式：`UI+N`
- 優先級：`P0`

### AUI-005 可查到訂單列表且篩選結果合理

- 驗收角色：Admin、StoreOwner、StoreEditor
- 驗收頁面：後台訂單列表頁
- 對應前端文件：[frontend/admin/06-order-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/06-order-management.md)
- 驗收人員要做什麼：
  1. 進入訂單列表頁
  2. 以狀態、訂單編號、日期條件查詢
  3. 點進任一訂單詳情
- 應看到什麼：
  1. 列表有資料且篩選結果合理
  2. 訂單詳情可看到收件資料、商品內容與狀態
- 背後對應 API：`POST /api/admin/orders/list`、`GET /api/admin/orders/{orderId}`
- 驗收方式：`UI+N`
- 優先級：`P1`

### AUI-006 訂單狀態必須依序前進，不能亂跳

- 驗收角色：Admin、StoreOwner、StoreEditor
- 驗收頁面：後台訂單詳情頁
- 對應前端文件：[frontend/admin/06-order-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/06-order-management.md)
- 驗收人員要做什麼：
  1. 找一筆可處理訂單
  2. 執行備貨
  3. 填物流單號並出貨
  4. 嘗試做一筆不合法的狀態切換
- 應看到什麼：
  1. 合法狀態切換成功
  2. 不合法切換要被擋下並顯示錯誤訊息
- 背後對應 API：`PUT /api/admin/orders/{orderId}/prepare`、`PUT /api/admin/orders/{orderId}/ship`、`PUT /api/admin/orders/{orderId}/status`
- 驗收方式：`UI+N`
- 優先級：`P0`

### AUI-007 出貨後前台玩家訂單也要同步看到狀態變更

- 驗收角色：Admin、StoreOwner、StoreEditor
- 驗收頁面：後台訂單管理頁、前台訂單頁
- 對應前端文件：[frontend/admin/06-order-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/06-order-management.md)、[frontend/client/06-order-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/06-order-management.md)
- 驗收人員要做什麼：
  1. 後台完成一筆訂單出貨
  2. 回到前台玩家帳號刷新訂單頁
- 應看到什麼：
  1. 後台顯示已出貨
  2. 前台同步顯示最新訂單狀態與物流資訊
- 背後對應 API：`PUT /api/admin/orders/{orderId}/ship`、`GET /api/order/{orderId}`
- 驗收方式：`UI+N`
- 優先級：`P0`

### AUI-008 店家角色不能取消不該取消的訂單

- 驗收角色：StoreOwner、StoreEditor
- 驗收頁面：後台訂單詳情頁
- 對應前端文件：[frontend/admin/06-order-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/06-order-management.md)
- 驗收人員要做什麼：
  1. 以 StoreEditor 帳號登入
  2. 嘗試取消一筆訂單
  3. 再以 StoreOwner 帳號測試相同流程
- 應看到什麼：
  1. StoreEditor 不應看到可取消操作，或送出時被拒絕
  2. StoreOwner 於合法條件下才可取消
- 背後對應 API：`PUT /api/admin/orders/{orderId}/cancel`
- 驗收方式：`UI+N`
- 優先級：`P0`

### AUI-009 報表頁資料要可查且符合角色範圍

- 驗收角色：Admin、StoreOwner
- 驗收頁面：報表與統計分析頁
- 對應前端文件：[frontend/admin/08-report-analytics.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/08-report-analytics.md)
- 驗收人員要做什麼：
  1. 用 Admin 查一次全平台資料
  2. 用 StoreOwner 查一次同區間資料
  3. 比較兩者可見範圍
- 應看到什麼：
  1. 報表能正常出資料
  2. StoreOwner 不應看到其他店家的全平台明細
- 背後對應 API：`POST /api/admin/report/revenue`、`POST /api/admin/report/lottery-result`、`POST /api/admin/report/referral`
- 驗收方式：`UI+N`
- 優先級：`P1`

### AUI-010 物流方式管理變更後要即時影響前台出貨選項

- 驗收角色：Admin
- 驗收頁面：物流方式管理頁、前台賞品盒出貨 Modal
- 對應前端文件：[frontend/admin/11-shipping-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/11-shipping-management.md)、[frontend/client/05-prize-box.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/05-prize-box.md)
- 驗收人員要做什麼：
  1. 在後台新增一個物流方式
  2. 停用一個既有物流方式
  3. 回到前台開出貨 Modal 檢查選項
- 應看到什麼：
  1. 新增的方式能在前台被選到
  2. 停用的方式在前台不再顯示
  3. 歷史訂單資料不應被破壞
- 背後對應 API：`GET /api/admin/shipping-methods`、`POST /api/admin/shipping-methods`、`PUT /api/admin/shipping-methods/{id}`、`PUT /api/admin/shipping-methods/{id}/status`、前台 `GET /api/shipping-methods`
- 驗收方式：`UI+N`
- 優先級：`P0`

### AUI-011 敏感操作至少能在 system log 或 audit log 中追到

- 驗收角色：Admin
- 驗收頁面：系統日誌頁
- 對應前端文件：[frontend/admin/10-system-config.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/10-system-config.md)
- 驗收人員要做什麼：
  1. 先執行一筆敏感操作，例如新增商品、修改物流方式或變更訂單狀態
  2. 進入系統日誌頁查詢同時間區間
- 應看到什麼：
  1. 可以查到對應操作紀錄
  2. 至少看得到時間、操作者、操作類型
- 背後對應 API：`GET /api/admin/system-log/type/{logType}`、`GET /api/admin/system-log/user/{userId}`、`GET /api/admin/system-log/date-range`
- 驗收方式：`UI+N / BE`
- 優先級：`P0`

## 給後台前端的 UI 對照提醒

後台前端在核對這份文件時，請逐項確認：

1. 商品管理頁是否真的具備新增、編輯、上下架、獎項維護入口
2. 訂單頁是否有狀態切換、出貨、取消等按鈕與對應錯誤訊息
3. 報表頁是否有角色差異化顯示
4. 物流方式管理頁是否與前台出貨選項真有串接
5. system log 頁是否存在，若不存在要明確標記目前無 UI

若 UI 尚未實作完整，請直接在前端 review 中標記缺口，不要讓驗收人員自行推論。
