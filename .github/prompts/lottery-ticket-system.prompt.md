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

### 二、刮刮樂模式（指定大獎位置）

#### 模式 A：店家指定大獎位置
- 店家建立商品時指定大獎籤位（例：45號是大獎）
- 其餘號碼為「謝謝惠顧」
- 可指定多個大獎位置

#### 模式 B：開套玩家指定大獎位置
- 店家不指定時，第一個抽獎的玩家（開套者）選擇大獎位置
- 後續玩家必須依照開套者選擇的位置進行

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

### 新增 Table 1：`lottery_ticket`（籤位表）
```sql
-- 核心：每個籤位是一筆資料，記錄分配到的獎品
CREATE TABLE lottery_ticket (
    id VARCHAR(36) PRIMARY KEY COMMENT '籤位 ID (UUID)',
    lottery_id VARCHAR(36) NOT NULL COMMENT '所屬抽獎活動 ID',
    ticket_number INT NOT NULL COMMENT '籤位編號 (1-N)',
    
    -- 獎品分配（建立時隨機分配，或店家指定）
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
    
    -- 刮刮樂：玩家指定大獎位置
    player_designated_numbers VARCHAR(255) COMMENT '玩家指定的大獎編號 (JSON Array)',
    
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
    COMMENT '店家指定大獎籤位 (JSON Array，刮刮樂用)' AFTER free_draw_enabled;
```

## API 設計

### 前台 API（重要：不能洩漏未抽籤位的獎品資訊）

#### GET /api/lottery/{lotteryId}/tickets
回傳籤位列表，但**隱藏未抽籤位的獎品資訊**
```json
{
  "data": {
    "tickets": [
      { "number": 1, "status": "AVAILABLE" },
      { "number": 2, "status": "DRAWN", "prizeLevel": "A", "prizeName": "..." },
      { "number": 3, "status": "AVAILABLE" }
    ],
    "session": {
      "isOpener": false,
      "currentOpener": "某玩家",
      "protectionEndTime": "..."
    }
  }
}
```

#### POST /api/lottery/{lotteryId}/draw
執行抽獎（選號或隨機）

### 後台 API

#### POST /admin/lottery（建立商品）
建立時根據 gameMode 生成籤位：
- RANDOM：自動隨機分配獎品到籤位
- SCRATCH_STORE：店家指定大獎位置
- SCRATCH_PLAYER：等待開套玩家指定

## 抽獎核心邏輯（共用）

### 統一抽獎流程（適用所有模式）
```
1. 檢查商品狀態（上架中、有剩餘）
2. 檢查保護時間（是否被其他玩家鎖定）
3. 檢查使用者餘額
4. 執行抽獎（選號或隨機取一個 AVAILABLE 的籤位）
5. 更新籤位狀態
6. 扣款
7. 檢查免單條件（開套玩家 + 保護期內 + 中大獎）
8. 記錄抽獎紀錄
9. 回傳結果（此時才揭露獎品資訊）
```

### 不同模式的差異點
| 模式 | 籤位生成時機 | 獎品分配方式 | 大獎位置 |
|------|-------------|-------------|----------|
| 一番賞 | 建立時 | 隨機分配 | 隨機 |
| 扭蛋 | 建立時 | 隨機分配 | 隨機 |
| 卡牌 | 建立時 | 隨機分配 | 隨機 |
| 刮刮樂(店家) | 建立時 | 店家指定 | 固定 |
| 刮刮樂(玩家) | 開套時 | 開套玩家指定 | 開套玩家決定 |

## 安全考量

### 前端顯示規則
1. **未抽籤位**：只顯示編號 + 可抽狀態
2. **已抽籤位**：顯示編號 + 獎品等級 + 獎品名稱 + 獎品圖片
3. **永不傳送**：未抽籤位的 prize_id、prize_level

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

| 分類 | game_mode | 籤位生成 | 獎品分配 | 大獎位置 | 免單機制 |
|------|-----------|----------|----------|----------|----------|
| 一番賞 | RANDOM | 建立時 | 隨機 | 隨機 | ✅ 可啟用 |
| 扭蛋 | RANDOM | 建立時 | 隨機 | 隨機 | ✅ 可啟用 |
| 卡牌 | RANDOM | 建立時 | 隨機 | 隨機 | ✅ 可啟用 |
| 刮刮樂(店家) | SCRATCH_STORE | 建立時 | 店家指定 | 固定 | ✅ 可啟用 |
| 刮刮樂(玩家) | SCRATCH_PLAYER | 開套時 | 開套玩家指定 | 開套玩家決定 | ✅ 可啟用 |

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
{
  "tickets": [
    // 未抽籤位：只有編號和狀態
    { "ticketNumber": 1, "status": "AVAILABLE" },
    { "ticketNumber": 2, "status": "AVAILABLE" },
    
    // 已抽籤位：完整資訊
    { 
      "ticketNumber": 3, 
      "status": "DRAWN",
      "prizeLevel": "C",
      "prizeName": "善逸公仔",
      "prizeImageUrl": "https://...",
      "drawnByNickname": "玩家A",
      "drawnAt": "2025-12-25T10:30:00"
    },
    
    // 鎖定中的籤位
    { "ticketNumber": 4, "status": "LOCKED" }
  ],
  "session": {
    "isOpener": false,
    "openerNickname": "玩家A",
    "protectionEndTime": "2025-12-25T10:35:00",
    "status": "ACTIVE"
  }
}
```

### 抽獎 API 回應範例

```json
// POST /api/lottery/{id}/draw
// 成功
{
  "success": true,
  "ticketNumber": 45,
  "prizeLevel": "A",
  "prizeName": "炭治郎公仔（大）",
  "prizeImageUrl": "https://...",
  "isGrandPrize": true,
  "triggeredFreeDraw": true,
  "refundAmount": 1500,
  "message": "恭喜中大獎！開套免單，退還 1500 元！"
}

// 失敗
{
  "success": false,
  "message": "商品正在被其他玩家抽獎中，請稍後再試"
}
```
