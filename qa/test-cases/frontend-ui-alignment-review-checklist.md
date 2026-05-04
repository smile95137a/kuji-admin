# KUJI-Server 前端 UI 對照核對清單

這份文件要呈現什麼內容：
提供給前台前端與後台前端，用來核對「驗收文件描述的流程」是否真的能在目前 UI 上找到對應頁面、元件、按鈕、Modal、提示訊息與 Network 行為。這不是要前端做正式驗收，而是要快速回答：這份 MD 是否真的能對應現在的 UI。

## 建議使用方式

1. 前台前端先看「前台區塊」
2. 後台前端先看「後台區塊」
3. 每一列請只回答：
   - `一致`
   - `部分一致`
   - `尚未對應`
4. 若為 `部分一致` 或 `尚未對應`，請在備註寫缺口

## 核對時一定要回答的問題

1. 這個故事在目前 UI 有沒有入口頁面或按鈕
2. 成功提示與失敗提示是否真的有 UI 可截圖
3. 畫面上能不能看出狀態切換結果
4. 若要做 `UI+N` 驗收，Network 面板能不能明確定位到對應 API
5. 是否有任何欄位名稱、按鈕名稱、流程順序已經和驗收文件不同

## 前台前端核對清單

| id | 對應驗收清單 | 對應前端文件 | 應有頁面或元件 | 前端需確認的問題 | 回覆狀態 | 備註 |
|----|--------------|--------------|----------------|------------------|----------|------|
| FE-CLIENT-001 | CUI-001 ~ CUI-002 | [frontend/client/03-lottery-browse.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/03-lottery-browse.md) | 商品列表頁、商品詳情頁、搜尋與篩選區 | 目前 UI 是否真的有商品列表、詳情、篩選、剩餘抽數、獎項區塊 | 待前端回覆 |  |
| FE-CLIENT-002 | CUI-003 ~ CUI-008 | [frontend/client/04-draw-flow.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/04-draw-flow.md) | 票格區、抽獎按鈕、指定大獎 UI、結果視窗 | 刮刮樂是否有指定流程、等待狀態、結果視窗，且結果畫面可核對票格一致性 | 待前端回覆 |  |
| FE-CLIENT-003 | CUI-009 ~ CUI-012 | [frontend/client/05-prize-box.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/05-prize-box.md) | 賞品盒頁、回收按鈕、出貨 Modal、配送欄位 | 賞品盒是否有分店家區塊、回收流程、出貨 Modal、配送方式切換必填欄位 | 待前端回覆 |  |
| FE-CLIENT-004 | CUI-013 ~ CUI-015 | [frontend/client/06-order-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/client/06-order-management.md) | 訂單列表頁、訂單詳情頁、補填資訊區 | 訂單頁是否可看到運費、付款狀態、配送方式、物流資訊、補填收件資訊入口 | 待前端回覆 |  |

## 後台前端核對清單

| id | 對應驗收清單 | 對應前端文件 | 應有頁面或元件 | 前端需確認的問題 | 回覆狀態 | 備註 |
|----|--------------|--------------|----------------|------------------|----------|------|
| FE-ADMIN-001 | AUI-001 ~ AUI-004 | [frontend/admin/05-product-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/05-product-management.md) | 商品列表頁、商品新增/編輯頁、獎項編輯區、上下架操作 | 目前 UI 是否真的具備建立商品、編輯獎項、上下架與店家資料隔離顯示 | 待前端回覆 |  |
| FE-ADMIN-002 | AUI-005 ~ AUI-008 | [frontend/admin/06-order-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/06-order-management.md) | 訂單列表頁、訂單詳情頁、備貨/出貨/取消按鈕 | 是否能在畫面上完成狀態流轉，且不合法狀態切換有明確提示 | 待前端回覆 |  |
| FE-ADMIN-003 | AUI-009 | [frontend/admin/08-report-analytics.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/08-report-analytics.md) | 報表查詢頁、篩選器、統計圖表或表格 | 報表 UI 是否存在，且不同角色看到的資料範圍是否有差異化處理 | 待前端回覆 |  |
| FE-ADMIN-004 | AUI-010 | [frontend/admin/11-shipping-management.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/11-shipping-management.md) | 物流方式列表、啟用/停用、編輯 Modal | 後台物流方式變更後，前台出貨選項是否會重新抓取並同步顯示 | 待前端回覆 |  |
| FE-ADMIN-005 | AUI-011 | [frontend/admin/10-system-config.md](/C:/Users/user/OneDrive/Desktop/dream/KUJI-Server/admin/frontend/admin/10-system-config.md) | system log 或 audit log 頁面 | 目前後台是否真的有對應的日誌查詢 UI，如果沒有請直接註明缺口 | 待前端回覆 |  |

## 給你轉交前端時可直接附上的一句話

請不要只確認 API 文件是否存在，請直接對照目前 UI 回覆：

1. 驗收文件中的頁面、按鈕、Modal、提示訊息，現在 UI 是否找得到
2. 若找不到，缺的是頁面、流程、欄位還是文案
3. 若 API 已有但 UI 尚未接上，也請直接標記
