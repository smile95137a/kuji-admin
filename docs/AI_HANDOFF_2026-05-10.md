# AI 交接文件 2026-05-10

## 1. 先看這份交接時要先理解的脈絡

目前倉庫在 SpecKit 上的名義焦點仍是 `033-platform-revenue-report`，對應文件為：

- `specs/033-platform-revenue-report/spec.md`
- `specs/033-platform-revenue-report/plan.md`

但這一輪實際開發與交接的主軸，已經不是新增平台報表功能，而是把「商品 / 抽獎」這條主流程補成可穩定使用，並且把前後端對商品狀態的理解統一。

下一位 AI 接手時，不要只看 SpecKit 當前 feature 就直接往報表功能繼續推。請先以本文件為本輪交接主線，再回頭對照既有 spec/plan，避免方向跑掉。

---

## 2. 專案目前真正的工作方向

這個專案現階段不是在擴大新功能範圍，而是在做兩件事：

1. 把既有功能補成可以穩定使用，不再出現明顯壞狀態。
2. 把商品 / 抽獎主流程的狀態、權限、前後端契約講清楚。

使用者目前最在意的不是「再多一支 API」，而是以下問題要有一致答案：

- 商品什麼時候應該進入哪個狀態
- 哪些狀態可以由前端手動操作
- 哪些狀態只能由系統自動推進
- 哪些狀態前端只能顯示，不能送回後端指定
- 前後台是否還殘留舊的 `CONFIGURED`、`SOLD_OUT`、`ENDED` 思維

---

## 3. 階段任務總覽

### Phase 1：帳號治理與安全補強

此階段已完成，重點如下：

- 前台與後台忘記密碼流程改為「寄送臨時密碼」
- 補上 `forceChangePassword` 強制改密碼卡控
- 新增後台本人資料 API：`GET/PUT /admin/users/me`、`POST /admin/users/me/change-password`
- `StoreOwner` 只能管理自己店內的小編
- 錯誤回傳格式收斂為統一結構

### Phase 2：商品建立流程收斂

此階段已完成，重點如下：

- 正式建立商品流程收斂到整合建立 API
- 商品欄位組合規則明確卡控
- 上架前完整性驗證補齊
- 獎品異動必須先下架
- 避免出現「已上架但不能抽」的壞狀態

### Phase 3：商品狀態模型重整

此階段核心已完成，正式語意如下：

- `DRAFT`
- `WAITING_ON_SHELF`
- `ON_SHELF`
- `OFF_SHELF`
- `GRAND_PRIZE_DRAWN`
- `ALL_DRAWN`
- `FORCED_OFF`
- `DELETED`

目前已知已同步到：

- `LotteryStatusEnum`
- `LotteryService` / `LotteryServiceImpl`
- `LotteryMapper.xml`
- 後台狀態切換 request/DTO
- 前台公開商品 detail 可見規則
- `docs/frontend-integration-guide.md`

### Phase 4：第三輪收尾，清掉顯示層與契約殘留

這是下一位 AI 應優先接手的工作。

核心不是再改大邏輯，而是清查所有前端可見契約與後台顯示語意，確認舊模型殘留是否已完全清乾淨。

### Phase 5：最後 bug sweep 與驗證收尾

此階段尚未完成，待環境可正常執行 Maven 後再做：

- compile / package
- 回歸測試
- 主流程驗證

---

## 4. 商品狀態模型：目前應以這套語意為準

### `DRAFT`

- 草稿或仍可編修狀態
- 可作為建立中、補資料中、下架後修訂中的工作態

### `WAITING_ON_SHELF`

- 已設定未來上架時間
- 尚未到上架時點
- 到排程時間後，系統才應嘗試推進到 `ON_SHELF`

### `ON_SHELF`

- 唯一正常可抽狀態
- 前台可正常販售 / 抽獎

### `OFF_SHELF`

- 人工下架
- 代表暫停販售，但不是終止
- 可調整後再重新上架

### `FORCED_OFF`

- 系統或管理端強制介入的下架狀態
- 語意比一般 `OFF_SHELF` 更重

### `GRAND_PRIZE_DRAWN`

- 大獎已抽完後，由系統自動進入的終態之一
- 不應由前端手動指定
- 前台可視需求顯示 detail，但不應再視為可抽

### `ALL_DRAWN`

- 全數抽完
- 系統自動終態
- 不應由前端手動指定

### `DELETED`

- 終止狀態

---

## 5. 狀態推進時機點

目前應這樣理解，不要再回退到舊語意：

1. 商品建立或更新後，若仍在編修中，維持 `DRAFT`
2. 若商品設定了未來 `scheduledAt`，建立或更新後應進 `WAITING_ON_SHELF`
3. 到排程時間且條件完整時，系統才推進到 `ON_SHELF`
4. `ON_SHELF` 是唯一正常可抽狀態
5. 人工下架時進 `OFF_SHELF`
6. 系統或管理端強制介入時進 `FORCED_OFF`
7. 大獎抽完時，依策略自動進 `GRAND_PRIZE_DRAWN`
8. 全數抽完時，自動進 `ALL_DRAWN`
9. `DELETED` 為終止狀態

---

## 6. 目前已完成內容

### 帳號與安全面

- 忘記密碼改成臨時密碼模式
- `forceChangePassword` 卡控已補
- 後台本人資料 API 已補
- `StoreOwner` 僅能管理自己店內小編
- 錯誤格式已統一

### 商品建立與獎品流程

- 商品整合建立流程已收斂
- 欄位組合規則已加強驗證
- 上架前完整性驗證已補
- 獎品異動需先下架的限制已補

### 商品狀態模型與契約

- 新狀態 enum / service / mapper 核心已完成
- 前台公開商品 detail 已允許讀取：
  - `ON_SHELF`
  - `GRAND_PRIZE_DRAWN`
  - `ALL_DRAWN`
- 後台狀態切換 DTO 僅接受前端真正應手動送出的狀態
- `docs/frontend-integration-guide.md` 已同步核心新模型

---

## 7. 下一位 AI 的第三輪優先任務

請直接接這三件事：

1. 清掉後台與前台顯示層殘留
2. 補一份高層商品生命週期流程說明
3. 只做「不改商業語意」的對齊修補

更具體來說，請優先檢查：

- 列表查詢是否仍用舊狀態概念
- 狀態篩選是否仍出現 `CONFIGURED`、`SOLD_OUT`、`ENDED`
- Enum 輸出、文案映射、後台顯示名稱是否仍殘留舊語意
- 前台 detail / 列表 / 篩選 / 操作按鈕是否與新狀態一致
- 文件是否還有前後矛盾段落

特別注意：

- `docs/frontend-integration-guide.md` 雖已同步核心新模型，但前段章節仍可能殘留舊狀態描述或舊轉移表，第三輪應做完整清查
- 若只是契約對齊、顯示文案、文件清理，可以直接修
- 若牽涉狀態定義、抽獎規則、最後賞、刮刮樂規則等業務語意變更，必須先停下來討論

---

## 8. 協作鐵律

這部分非常重要，下一位 AI 必須遵守。

### 先討論，再改邏輯

只要涉及以下任一情況，就不能直接腦補改：

- 狀態模型變更
- 流程規則變更
- 商業語意變更
- 多檔案、跨流程的大範圍調整

### 要先 plan，再動手

使用者不接受 AI 直接從局部 bug 出發就一路擴改。  
若是跨多檔、改流程、改狀態模型，必須先整理清楚：

- 目前理解
- 影響範圍
- 預計修改點
- 哪些是假設、哪些是已確認規則

### 優先把流程想完整，不只補表面 bug

使用者要的是完整流程一致性，不是只把單點錯誤壓掉。

### 現階段優先順序

目前優先順序是：

1. 功能可用
2. 流程清楚
3. 核心卡控不能鬆
4. 測試可暫時往後排，但不能假裝已驗證

### 交接必須清楚寫

每次交接都要明確標示：

- 已完成
- 未完成
- 待確認
- 哪些地方仍需使用者批准

---

## 9. 驗證狀態與限制

目前已知狀態：

- 這輪改動已用 IDE diagnostics 檢查，暫未見明顯錯誤
- 現場尚未完成 Maven compile / test / package 驗證

因此下一位 AI 不可把這輪描述成「已完整測試完成」。

---

## 10. 建議接手順序

下一位 AI 建議依以下順序接手：

1. 先讀本文件
2. 再讀 `docs/frontend-integration-guide.md`
3. 再比對目前商品 / 抽獎相關 Java、DTO、Mapper、Controller 改動
4. 列出第三輪清查清單
5. 若只是契約與顯示層對齊，直接修補
6. 若要再動業務邏輯，先回到討論模式

---

## 11. 相關文件

- `docs/frontend-integration-guide.md`
- `specs/033-platform-revenue-report/spec.md`
- `specs/033-platform-revenue-report/plan.md`
- `AGENTS.md`

