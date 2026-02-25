# 抽獎籤位系統設計 (Lottery Ticket System)

## 核心需求分析

### 一、一番賞/扭蛋/卡牌模式（隨機籤位）
1. **籤位隨機分配**：商品建立時，系統根據各等級獎品數量，隨機分配到指定編號
   - 例：總數 80 抽，A賞 3 個 → A賞可能落在 13, 45, 76 號（亂數分配）
   - 不能按照流水編號順序分配（會被猜到）

2. **前台安全機制**：
   - 未被抽中的籤位不能顯示獎品資訊（防止開發者工具查看）
   - 只有已抽過的籤位才顯示獎品等級
   - 前端只能拿到：籤位編號 + 是否已抽 + (已抽才有的)獎品資訊

### 二、刮刮樂模式（雙號碼機制）

> **刮刮樂核心概念**：玩家手上的實體卡有「物理編號」（ticket_number，印在卡上的 1-N 序號），
> 刮開後露出「中獎號碼」（revealed_number，由系統建立時亂數分配，與物理編號無關）。
> 後端以 revealed_number 是否落在得獎位置決定是否中獎。

- 例：60 張刮刮樂卡，1 號卡刮開可能顯示 23，15 號卡刮開可能顯示 15（也可能是任何其他數字）
- `ticket_number` 和 `revealed_number` 是完全獨立的兩個值，均為 1-N 且不重複

#### 模式 A：店家指定大獎位置（SCRATCH_STORE）
- 店家建立商品時指定哪些 `revealed_number` 是大獎（例：revealed_number 45 是大獎）
- 其餘號碼為「謝謝惠顧」
- 可指定多個大獎位置

#### 模式 B：開套玩家指定大獎位置（SCRATCH_PLAYER）
- 店家不指定時，第一個抽獎的玩家（開套者）選擇哪些 `revealed_number` 是大獎
- 後續玩家的籤位已預先分配好 revealed_number 但尚未標記為大獎，等開套者指定後才確定

### 三、開套免單機制

#### 規則定義
1. 店家設定「開套保護抽數」（例：5抽內免單）
2. 只有開套玩家享有免單機會
3. 開套玩家在保護抽數內抽中大獎 → 退還已花費金額
4. 保護時間內未抽完剩餘次數 → 視為放棄

#### 範例
- 商品總共 80 抽，每抽 500 元
- 店家設定 5 抽內免單
- A玩家開套，第 3 抽中獎 → 退還 1500 元（3 × 500）
- 若 A玩家保護時間到期只抽 3 抽，後續玩家 B 補抽 2 抽也不享有免單

### 四、保護時間機制
- 開套玩家有專屬保護時間
- 保護期間其他玩家不能抽該商品
- 時間到期自動釋放

## 資料庫設計

### 欄位語意對照表

| 欄位 | 一番賞 / 扭蛋 / 卡牌 | 刮刮樂（SCRATCH_STORE / SCRATCH_PLAYER）|
|------|---------------------|----------------------------------------|
| `ticket_number` | 抽籤位置編號（1-N，對應獎品位置）| 實體卡片的物理序號（印在卡面上）|
| `revealed_number` | NULL（不適用此概念）| 刮開後顯示的亂數號碼（決定是否中獎）|
| `prize_id` | 建立時隨機分配到每個籤位 | 根據 revealed_number 是否為得獎號碼決定 |

> ⚠️ 一番賞/扭蛋：前端直接用 `ticketNumber` 選籤、顯示結果，**不存在** `revealedNumber`。
> ⚠️ 刮刮樂：前端用 `ticketNumber` 選卡片，**抽完後**才能看到 `revealedNumber` 和獎品資訊。

### 新增 Table 1：`lottery_ticket`（籤位表）
```sql
-- 核心：每個籤位是一筆資料，記錄分配到的獎品
CREATE TABLE lottery_ticket (
    id VARCHAR(36) PRIMARY KEY COMMENT '籤位 ID (UUID)',
    lottery_id VARCHAR(36) NOT NULL COMMENT '所屬抽獎活動 ID',
    ticket_number INT NOT NULL COMMENT '籤位編號 (1-N)；刮刮樂＝實體卡物理序號',

    -- 刮刮樂專用：刮開後顯示的亂數號碼
    -- 一番賞/扭蛋：NULL；刮刮樂：1-N 亂數，與 ticket_number 無關，建立時 shuffle 分配
    revealed_number INT NULL COMMENT '刮刮樂：刮開後揭露的號碼；一番賞/扭蛋為 NULL',

    -- 獎品分配（建立時隨機分配，或依 revealed_number 是否在得獎名單決定）
    prize_id VARCHAR(36) COMMENT '分配到的獎項 ID (NULL=謝謝惠顧)',
    prize_level VARCHAR(20) COMMENT '獎品等級快取 (A/B/C/謝謝惠顧)',
    
    -- 抽取狀態
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT '狀態：AVAILABLE/DRAWN/LOCKED',
    drawn_by VARCHAR(36) COMMENT '抽取者的使用者 ID',
    drawn_at DATETIME COMMENT '抽取時間',
    
    -- 刮刮樂專用：是否為店家/玩家指定的大獎位置
    is_designated_prize TINYINT DEFAULT 0 COMMENT '是否為指定大獎位置：0=否, 1=是',
    designated_by VARCHAR(20) COMMENT '指定者類型：STORE/PLAYER',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_lottery_ticket (lottery_id, ticket_number),
    INDEX idx_lottery_id (lottery_id),
    INDEX idx_status (status),
    INDEX idx_prize_id (prize_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽獎籤位表';

-- 若資料庫表已建立，執行以下 ALTER 新增欄位：
ALTER TABLE lottery_ticket
    ADD COLUMN revealed_number INT NULL
    COMMENT '刮刮樂：刮開後揭露的號碼；一番賞/扭蛋為 NULL'
    AFTER ticket_number;
```

### 新增 Table 2：`lottery_session`（開套場次表）
```sql
-- 追蹤開套玩家與免單機制
CREATE TABLE lottery_session (
    id VARCHAR(36) PRIMARY KEY COMMENT '場次 ID (UUID)',
    lottery_id VARCHAR(36) NOT NULL COMMENT '抽獎活動 ID',
    opener_user_id VARCHAR(36) NOT NULL COMMENT '開套玩家 ID',
    
    -- 開套保護機制
    protection_draws INT DEFAULT 0 COMMENT '保護抽數（店家設定，0=無保護）',
    protection_start_time DATETIME COMMENT '保護開始時間',
    protection_end_time DATETIME COMMENT '保護結束時間',
    
    -- 開套抽獎狀態
    opener_draw_count INT DEFAULT 0 COMMENT '開套玩家已抽次數',
    opener_total_cost BIGINT DEFAULT 0 COMMENT '開套玩家已花費金額',
    
    -- 免單狀態
    free_draw_triggered TINYINT DEFAULT 0 COMMENT '是否觸發免單：0=否, 1=是',
    free_draw_refund_amount BIGINT DEFAULT 0 COMMENT '免單退款金額',
    free_draw_at DATETIME COMMENT '觸發免單時間',
    
    -- 刮刮樂：玩家指定大獎位置（存 revealed_number 列表）
    player_designated_numbers VARCHAR(255) COMMENT '玩家指定的大獎 revealed_number 列表 (JSON Array)',
    
    -- 狀態
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '狀態：ACTIVE/COMPLETED/EXPIRED',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_lottery_id (lottery_id),
    INDEX idx_opener_user_id (opener_user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽獎開套場次表';
```

### 修改 Table：`lottery`（新增欄位）
```sql
ALTER TABLE lottery ADD COLUMN game_mode VARCHAR(30) DEFAULT 'RANDOM' 
    COMMENT '遊戲模式：RANDOM(隨機)/SCRATCH_STORE(店家指定)/SCRATCH_PLAYER(玩家指定)' AFTER sub_category;

ALTER TABLE lottery ADD COLUMN protection_draws INT DEFAULT 0 
    COMMENT '開套保護抽數（0=無保護）' AFTER max_draws;

ALTER TABLE lottery ADD COLUMN protection_minutes INT DEFAULT 5 
    COMMENT '保護時間（分鐘）' AFTER protection_draws;

ALTER TABLE lottery ADD COLUMN free_draw_enabled TINYINT DEFAULT 0 
    COMMENT '是否啟用開套免單：0=否, 1=是' AFTER protection_minutes;

ALTER TABLE lottery ADD COLUMN designated_prize_numbers VARCHAR(500) 
    COMMENT '店家指定大獎的 revealed_number 列表 (JSON Array，刮刮樂用)' AFTER free_draw_enabled;
```

## API 設計

### 前台 API（重要：不能洩漏未抽籤位的獎品與 revealed_number 資訊）

#### GET /api/lottery/{lotteryId}/tickets
回傳籤位列表，**隱藏未抽籤位的所有獎品資訊與 revealed_number**

#### POST /api/lottery/{lotteryId}/draw
執行抽獎（傳入 ticketNumber 指定卡片，或不傳由系統隨機選）

#### POST /api/lottery/{lotteryId}/designate
刮刮樂 SCRATCH_PLAYER 模式專用：開套玩家指定大獎的 revealedNumber 對應的獎品
```json
{ "designations": [
    { "revealedNumber": 15, "prizeId": "prize-uuid-A1" },
    { "revealedNumber": 32, "prizeId": "prize-uuid-A1" },
    { "revealedNumber": 47, "prizeId": "prize-uuid-B1" }
] }
```
✅ 指定完成後，後端自動將剩餘籤位隨機分配非大獎獎品

#### GET /api/lottery/{lotteryId}/session
查詢目前開套場次資訊

### 後台 API

#### POST /admin/lottery（建立商品）
建立時根據 gameMode 生成籤位：
- RANDOM：自動隨機分配獎品到籤位，revealed_number = NULL
- SCRATCH_STORE：每張卡分配 revealed_number，依 designated_prize_numbers 設定大獎
- SCRATCH_PLAYER：每張卡分配 revealed_number，獎品等開套玩家指定

## 抽獎核心邏輯（共用）

### 統一抽獎流程（適用所有模式）
```
1. 檢查商品狀態（上架中、有剩餘）
2. 檢查保護時間（是否被其他玩家鎖定）
3. 檢查使用者餘額
4. 執行抽獎（選號或隨機取一個 AVAILABLE 的籤位）
5. 更新籤位狀態 → DRAWN
6. 扣款
7. 檢查免單條件（開套玩家 + 保護期內 + 中大獎）
8. 記錄抽獎紀錄
9. 回傳結果：
   - RANDOM 模式：ticketNumber + 獎品資訊（revealedNumber = null）
   - SCRATCH 模式：ticketNumber + revealedNumber + 獎品資訊
```

### 票券生成邏輯（後端）

#### RANDOM 模式（一番賞 / 扭蛋 / 卡牌）
```
1. 取得所有獎品清單，展開為逐一物件（A賞×3、B賞×5 … → 共 N 個）
2. 用 SecureRandom shuffle 打亂獎品順序
3. 依序建立 ticket_number = 1…N，每張對應 shuffle 後的獎品
4. revealed_number = NULL（不使用）
```

#### SCRATCH_STORE 模式（店家指定大獎位置）
```
1. 建立 ticket_number = 1…N，全部 prize_id = null（謝謝惠顧）
2. 用 SecureRandom shuffle [1…N]，依序指派為各張卡的 revealed_number（不重複）
3. 店家指定的 designated_prize_numbers = [45, 23, 7]（這些是 revealed_number）
4. 遍歷所有 ticket，若 revealed_number 在 designated_prize_numbers 中
   → 設定 prize_id、is_designated_prize = 1、designated_by = 'STORE'
```

#### SCRATCH_PLAYER 模式（開套玩家指定大獎位置）
```
1. 建立 ticket_number = 1…N，全部 prize_id = null（謝謝惠顧）
2. 用 SecureRandom shuffle [1…N]，依序指派為各張卡的 revealed_number（不重複）
3. 所有票均為謝謝惠顧，等待開套玩家呼叫 /designate API 指定大獎 revealed_number
4. 開套玩家指定後，找到 revealed_number 符合的 ticket，更新 prize_id + is_designated_prize
```

### 不同模式的差異點
| 模式 | 籤位生成時機 | 獎品分配方式 | revealed_number | 免單機制 |
|------|-------------|-------------|-----------------|----------|
| 一番賞 | 建立時 | 隨機分配 | 無（NULL）| ✅ 可啟用 |
| 扭蛋 | 建立時 | 隨機分配 | 無（NULL）| ✅ 可啟用 |
| 卡牌 | 建立時 | 隨機分配 | 無（NULL）| ✅ 可啟用 |
| 刮刮樂(店家) | 建立時 | 店家指定 revealed_number | 有，建立時 shuffle | ✅ 可啟用 |
| 刮刮樂(玩家) | 開套時指定 | 開套玩家指定 revealed_number | 有，建立時 shuffle | ✅ 可啟用 |

## 安全考量

### 前端顯示規則
1. **未抽籤位**：只顯示 ticketNumber + 可抽狀態，**絕不傳送** prize_id / prize_level / revealedNumber
2. **已抽籤位**：顯示 ticketNumber + revealedNumber（刮刮樂）+ 獎品等級 + 獎品名稱 + 獎品圖片
3. **永不傳送**：未抽籤位的 prize_id、prize_level、revealed_number

### 後端防護
1. 前台 API 使用 DTO 過濾敏感欄位
2. 籤位狀態變更需驗證使用者身份
3. 抽獎操作使用樂觀鎖或資料庫鎖防止併發問題

---

## 實作檔案清單

### 已建立的檔案

| 檔案 | 說明 |
|------|------|
| `doc/DDL_lottery_ticket_system.sql` | 資料庫 DDL（新增表格 + 修改現有表格） |
| `service/LotteryTicketService.java` | 籤位服務介面 |
| `service/impl/LotteryTicketServiceImpl.java` | 籤位服務實作（含 TODO 標記） |
| `res/lottery/LotteryTicketRes.java` | 籤位回應 DTO |
| `controller/api/LotteryDrawController.java` | 前台抽獎 API |

### 待執行步驟

1. **執行 DDL**
   ```bash
   # 在 MySQL 執行 doc/DDL_lottery_ticket_system.sql
   ```

2. **執行 MyBatis Generator**
   ```bash
   mvn mybatis-generator:generate
   ```
   這會生成：
   - `entity/LotteryTicket.java`
   - `entity/LotterySession.java`
   - `mapper/LotteryTicketMapper.java`
   - `mapper/LotterySessionMapper.java`
   - 對應的 Example 類別

3. **完成 TODO 標記**
   - 在 `LotteryTicketServiceImpl.java` 中搜尋 `TODO`
   - 實作所有資料庫操作

4. **更新 SecurityConfig**
   - 將 `/lottery/**` 加入前台 API 路由

---

## 遊戲模式對照表

| 分類 | game_mode | 籤位生成 | 獎品分配 | revealed_number | 免單機制 |
|------|-----------|----------|----------|-----------------|----------|
| 一番賞 | RANDOM | 建立時 | 隨機 | 無（NULL）| ✅ 可啟用 |
| 扭蛋 | RANDOM | 建立時 | 隨機 | 無（NULL）| ✅ 可啟用 |
| 卡牌 | RANDOM | 建立時 | 隨機 | 無（NULL）| ✅ 可啟用 |
| 刮刮樂(店家) | SCRATCH_STORE | 建立時 | 店家指定 revealed_number | 有，建立時 shuffle | ✅ 可啟用 |
| 刮刮樂(玩家) | SCRATCH_PLAYER | 開套時指定 | 開套玩家指定 revealed_number | 有，建立時 shuffle | ✅ 可啟用 |

---

## 免單機制流程圖

```
玩家 A 開套
    │
    ▼
┌─────────────────────────────────────┐
│ 建立 lottery_session                │
│ - opener_user_id = A               │
│ - protection_draws = 5 (店家設定)   │
│ - protection_end_time = now + 5min │
│ - free_draw_enabled = true         │
└─────────────────────────────────────┘
    │
    ▼
玩家 A 抽第 1 抽 (C賞) → opener_draw_count = 1, opener_total_cost = 500
玩家 A 抽第 2 抽 (F賞) → opener_draw_count = 2, opener_total_cost = 1000
玩家 A 抽第 3 抽 (A賞/大獎!) 
    │
    ▼
┌─────────────────────────────────────┐
│ 檢查免單條件                         │
│ ✅ 是開套玩家                        │
│ ✅ 在保護抽數內 (3 ≤ 5)              │
│ ✅ 中大獎 (is_grand_prize = 1)       │
│ → 觸發免單！                         │
└─────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────┐
│ 執行退款                            │
│ - free_draw_triggered = true       │
│ - free_draw_refund_amount = 1500   │
│ - 退還 1500 到玩家 A 的儲值金        │
└─────────────────────────────────────┘
```

---

## 前台 API 安全設計

### 籤位查詢 API 回應範例

```json
// GET /api/lottery/{id}/tickets

// ── RANDOM 模式（一番賞 / 扭蛋）──
{
  "gameMode": "RANDOM",
  "tickets": [
    { "ticketNumber": 1, "status": "AVAILABLE" },
    { "ticketNumber": 2, "status": "AVAILABLE" },
    {
      "ticketNumber": 3,
      "status": "DRAWN",
      "prizeLevel": "C",
      "prizeName": "善逸公仔",
      "prizeImageUrl": "https://...",
      "drawnByNickname": "玩家A",
      "drawnAt": "2026-02-26T10:30:00"
    },
    { "ticketNumber": 4, "status": "LOCKED" }
  ],
  "session": { ... }
}

// ── SCRATCH 模式（刮刮樂）──
{
  "gameMode": "SCRATCH_STORE",
  "tickets": [
    { "ticketNumber": 1, "status": "AVAILABLE" },
    { "ticketNumber": 2, "status": "AVAILABLE" },
    {
      "ticketNumber": 3,
      "status": "DRAWN",
      "revealedNumber": 23,
      "prizeLevel": "A",
      "prizeName": "炭治郎公仔",
      "prizeImageUrl": "https://...",
      "drawnByNickname": "玩家A",
      "drawnAt": "2026-02-26T10:30:00"
    }
  ],
  "session": {
    "isOpener": false,
    "openerNickname": "玩家A",
    "protectionEndTime": "2026-02-26T10:35:00",
    "status": "ACTIVE"
  }
}
```

> ✅ RANDOM 模式回應中**沒有** `revealedNumber` 欄位
> ✅ SCRATCH 模式中，`revealedNumber` 僅在 `status = "DRAWN"` 時才出現

### 抽獎 API 回應範例

> ⚠️ **Breaking Change（v4.0）**：`POST /api/lottery/{id}/draw` 的回應**不再是裸陣列**。
> 現在固定包裝為 `DrawBatchResponse`，包含 `playMode`、`gameMode`、`results[]` 三個欄位。
> 前端**必須**先判斷 `data.playMode` 才能決定顯示模式。

```json
// POST /api/lottery/{id}/draw → 固定回傳 DrawBatchResponse

// ── LOTTERY_MODE（一番賞/扭蛋/卡牌）──
{
  "success": true,
  "data": {
    "playMode": "LOTTERY_MODE",
    "gameMode": "RANDOM",
    "results": [
      {
        "success": true,
        "ticketId": "uuid-of-ticket",
        "ticketNumber": 45,
        "revealedNumber": null,
        "prizeId": "prize-uuid",
        "prizeLevel": "A",
        "prizeName": "炭治郎公仔（大）",
        "prizeImageUrl": "https://...",
        "isGrandPrize": true,
        "triggeredFreeDraw": true,
        "refundAmount": 1500,
        "message": "恭喜中大獎！開套免單，退還 1500 元！"
      }
    ]
  }
}

// ── SCRATCH_MODE（刮刮樂，SCRATCH_STORE 或 SCRATCH_PLAYER）──
{
  "success": true,
  "data": {
    "playMode": "SCRATCH_MODE",
    "gameMode": "SCRATCH_STORE",
    "results": [
      {
        "success": true,
        "ticketId": "uuid-of-ticket",
        "ticketNumber": 5,
        "revealedNumber": 23,        ← 刮開後顯示的號碼（前端播動畫用）
        "prizeId": "prize-uuid",
        "prizeLevel": "A",
        "prizeName": "炭治郎公仔（大）",
        "prizeImageUrl": "https://...",
        "isGrandPrize": true,
        "triggeredFreeDraw": false,
        "refundAmount": 0,
        "message": "抽獎成功！恭喜獲得 炭治郎公仔（大）"
      }
    ]
  }
}

// ── SCRATCH_PLAYER：尚未指定大獎（攔截回應，非正常 DrawBatchResponse）──
{
  "success": true,
  "data": {
    "designationRequired": true,
    "message": "請先指定大獎位置（共需指定 5 個號碼）",
    "availableNumbers": [3, 7, 15, 22, 41],
    "grandPrizes": [
      { "prizeId": "prize-A", "prizeName": "iPhone", "prizeLevel": "A", "quantity": 3, "prizeImageUrl": "..." },
      { "prizeId": "prize-B", "prizeName": "MacBook", "prizeLevel": "B", "quantity": 2, "prizeImageUrl": "..." }
    ]
  }
}

// ── 失敗（results[0].success = false）──
{
  "success": true,
  "data": {
    "playMode": "SCRATCH_MODE",
    "gameMode": "SCRATCH_STORE",
    "results": [
      {
        "success": false,
        "message": "商品正在被其他玩家抽獎中，請稍後再試"
      }
    ]
  }
}
```

#### 前端判斷邏輯（TypeScript）

```typescript
interface DrawResult {
  success: boolean;
  ticketId?: string;
  ticketNumber?: number;
  revealedNumber?: number;   // SCRATCH_MODE 才有值
  prizeId?: string;
  prizeLevel?: string;
  prizeName?: string;
  prizeImageUrl?: string;
  isGrandPrize?: boolean;
  triggeredFreeDraw?: boolean;
  refundAmount?: number;
  message?: string;
}

interface DrawBatchResponse {
  playMode: string;           // 'LOTTERY_MODE' | 'SCRATCH_MODE'
  gameMode: string;           // 'RANDOM' | 'SCRATCH_STORE' | 'SCRATCH_PLAYER'
  results: DrawResult[];
}

interface DesignationRequiredResponse {
  designationRequired: true;
  message: string;
  availableNumbers: number[];
  grandPrizes: GrandPrizeInfo[];
}

// 處理 draw 回應
async function handleDrawResponse(responseData: DrawBatchResponse | DesignationRequiredResponse) {
  // Step 1: 先判斷是否是「需要指定大獎」攔截
  if ('designationRequired' in responseData && responseData.designationRequired) {
    showDesignationUI(responseData);  // 顯示指定大獎 UI
    return;
  }

  // Step 2: 正常 DrawBatchResponse
  const draw = responseData as DrawBatchResponse;
  const isScratch = draw.playMode === 'SCRATCH_MODE';

  for (const result of draw.results) {
    if (!result.success) {
      showError(result.message);
      continue;
    }

    if (isScratch && result.revealedNumber != null) {
      // 播刮卡動畫，然後顯示 revealedNumber + 獎品
      await playScratchAnimation(result.ticketNumber!);
      displayScratchResult(result.revealedNumber, result.prizeLevel, result.prizeName);
    } else {
      // 一番賞：直接顯示獎品
      displayPrize(result.prizeLevel, result.prizeName);
    }
  }
}
```

---

## 前端開發規格（Frontend Spec）

### 一、各遊戲模式行為差異

| 行為 | 一番賞 / 扭蛋 / 卡牌（RANDOM）| 刮刮樂（SCRATCH_STORE / SCRATCH_PLAYER）|
|------|-------------------------------|----------------------------------------|
| 籤位選取方式 | 點選籤位格子（ticketNumber）或隨機 | 點選實體卡片（ticketNumber）|
| 抽完後顯示 | 直接顯示獎品等級 + 名稱 | 先播刮卡動畫 → 顯示 revealedNumber → 顯示獎品 |
| 是否有 revealedNumber | ❌ 無此欄位（null）| ✅ 有，由後端 response 給出 |
| 未抽籤位顯示 | 編號 + 可抽狀態 | 卡片背面（不透露任何資訊）|
| 已抽籤位顯示 | 獎品等級 + 名稱 | revealedNumber + 獎品等級 + 名稱 |

---

### 二、前端 UI 狀態機（每張籤位）

```
AVAILABLE
  │ 玩家點擊
  ▼
呼叫 POST /draw（送出 ticketNumber）
  │
  ├── [RANDOM 模式] Response 回來
  │     → 直接顯示獎品資訊
  │     → 更新該 ticketNumber 狀態為 DRAWN
  │
  └── [SCRATCH 模式] Response 回來（含 revealedNumber）
        → 播刮卡動畫（前端自行控制，約 1-2 秒）
        → 動畫結束後顯示 revealedNumber（大號字）
        → 再顯示獎品等級 / 名稱 或「謝謝惠顧」
        → 更新該 ticketNumber 狀態為 DRAWN

LOCKED → 顯示「鎖定中，請稍待」（不可點擊）
DRAWN  → 顯示已抽結果（不可點擊）
```

---

### 三、API 端點快速參考

| 端點 | 方法 | 說明 |
|------|------|------|
| `/api/lottery/{id}/tickets` | GET | 取得所有籤位（未抽不含獎品 / revealedNumber）|
| `/api/lottery/{id}/draw` | POST | 執行抽獎（選號或隨機）|
| `/api/lottery/{id}/designate` | POST | 刮刮樂 SCRATCH_PLAYER：開套玩家指定大獎 revealed_number |
| `/api/lottery/{id}/session` | GET | 查詢開套場次資訊 |

---

### 四、刮刮樂動畫觸發時序

```
① 玩家點擊卡片（ticketNumber = 5）
②   前端 → POST /api/lottery/{id}/draw
    Request body: { "count": 1, "ticket": ["<ticket-uuid>"] }

③   後端回傳（DrawBatchResponse）：
    {
      "playMode": "SCRATCH_MODE",
      "gameMode": "SCRATCH_STORE",
      "results": [{
        "success": true,
        "ticketNumber": 5,
        "revealedNumber": 23,   ← 刮開後的號碼（必須從這裡取）
        "prizeLevel": "A",
        "prizeName": "炭治郎公仔"
      }]
    }

④   前端判斷 data.playMode === "SCRATCH_MODE" → 進入刮卡流程
⑤   開始播刮開動畫（此時已拿到所有資料，動畫純粹是視覺效果）
⑥   動畫結束 → 顯示 data.results[0].revealedNumber = 23（大字）
⑦   顯示 prizeLevel = A / prizeName = 炭治郎公仔（或謝謝惠顧）
```

> ⚠️ `revealedNumber` 必須從 `data.results[0].revealedNumber` 取得，前端**禁止**自行推算
> ⚠️ 動畫播完前不得顯示任何獎品資訊（避免快速截圖跳過動畫作弊）
> ⚠️ `revealedNumber` 為 null 表示此票是 LOTTERY_MODE（舊資料或非刮刮樂），顯示邏輯退回 RANDOM 模式

---

### 五、SCRATCH_PLAYER 開套流程（前端）

```
① 玩家嘗試抽獎 → 後端回應 designationRequired: true
   回應內容：
   {
     "designationRequired": true,
     "message": "請先指定大獎位置（共需指定 5 個號碼）",
     "availableNumbers": [3, 7, 15, 22, 41, ...],   ← revealedNumber 列表（不是 ticketNumber）
     "grandPrizes": [
       { "prizeId": "prize-A1", "prizeName": "iPhone", "prizeLevel": "A", "quantity": 3, ... },
       { "prizeId": "prize-B1", "prizeName": "MacBook", "prizeLevel": "B", "quantity": 2, ... }
     ]
   }

② 前端展示「選擇大獎號碼」界面
   - 讓玩家從 availableNumbers 中選取 revealedNumber
   - grandPrizes[].quantity 加總 = 需要選擇的總數
   - たとえば：A賞 × 3，B賞 × 2 → 就要選 5 個 revealedNumber

③ 醫嬣 POST /api/lottery/{id}/designate
   { "designations": [
       { "revealedNumber": 15, "prizeId": "prize-A1" },
       { "revealedNumber": 32, "prizeId": "prize-A1" },
       { "revealedNumber": 47, "prizeId": "prize-A1" },
       { "revealedNumber": 8,  "prizeId": "prize-B1" },
       { "revealedNumber": 63, "prizeId": "prize-B1" }
   ] }

④ 後端自動將剩餘籤位分配非大獎獎品（前端無需操作）
⑤ 確認後才能開始抽籤
⑥ 之後的玩家：場次已建立，直接進入正常抽籤流程

> ⚠️ 項目重要：
> - 傳給 /designate 的是 **revealedNumber**，不是 物理格子號碼 ticketNumber
> - availableNumbers 是 revealedNumber 的隨機範圍，不是 1~N 的順序列表
```

---

### 六、保護時間提示（前端）

```
session.protectionEndTime 存在 → 顯示倒數計時
倒數歸零 → 自動重新 GET /tickets（後端此時 session 已 EXPIRED，其他玩家可抽）
isOpener = true → 顯示「你是開套玩家」提示 + 免單剩餘次數
isOpener = false + 保護中 → 顯示「等待開套玩家完成保護抽」提示
```
