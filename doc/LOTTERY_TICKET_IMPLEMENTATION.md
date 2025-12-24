# 籤位系統實作指南

## 快速開始

### 步驟 1：執行 DDL

在 MySQL 中執行以下 SQL 檔案：
```bash
mysql -u your_user -p your_database < doc/DDL_lottery_ticket_system.sql
```

或在 MySQL Workbench / DBeaver 中開啟並執行 `doc/DDL_lottery_ticket_system.sql`

### 步驟 2：更新 generatorConfig.xml

在 `src/main/resources/generatorConfig.xml` 中加入新的表格：

```xml
<!-- 籤位表 -->
<table tableName="lottery_ticket" domainObjectName="LotteryTicket">
    <generatedKey column="id" sqlStatement="SELECT REPLACE(UUID(),'-','')" identity="false"/>
</table>

<!-- 開套場次表 -->
<table tableName="lottery_session" domainObjectName="LotterySession">
    <generatedKey column="id" sqlStatement="SELECT REPLACE(UUID(),'-','')" identity="false"/>
</table>
```

### 步驟 3：執行 MyBatis Generator

```bash
mvn mybatis-generator:generate
```

這會生成：
- `entity/LotteryTicket.java`
- `entity/LotterySession.java`  
- `example/LotteryTicketExample.java`
- `example/LotterySessionExample.java`
- `mapper/LotteryTicketMapper.java`
- `mapper/LotterySessionMapper.java`
- `mapper/LotteryTicketMapper.xml`
- `mapper/LotterySessionMapper.xml`

### 步驟 4：完成 Service 實作

在 `LotteryTicketServiceImpl.java` 中：

1. 取消註解 Mapper 注入
2. 搜尋 `TODO` 標記並實作
3. 主要需要實作的方法：
   - `generateRandomTickets()` - 隨機分配獎品到籤位
   - `generateScratchTickets()` - 刮刮樂籤位生成
   - `getTicketsForFrontend()` - 前台籤位查詢（安全版本）
   - `draw()` - 抽獎核心邏輯
   - `checkAndTriggerFreeDraw()` - 免單檢查

### 步驟 5：更新 SecurityConfig

確保 `/lottery/**` 路由可被前台使用者存取。

---

## 檔案清單

| 類型 | 檔案路徑 | 說明 |
|------|----------|------|
| DDL | `doc/DDL_lottery_ticket_system.sql` | 資料庫結構 |
| Prompt | `.github/prompts/lottery-ticket-system.prompt.md` | 完整設計文件 |
| Service | `service/LotteryTicketService.java` | 服務介面 |
| Service | `service/impl/LotteryTicketServiceImpl.java` | 服務實作 |
| DTO | `res/lottery/LotteryTicketRes.java` | 籤位回應 |
| Controller | `controller/api/LotteryDrawController.java` | 前台 API |

---

## 核心概念

### 1. 籤位 vs 獎品

```
lottery (抽獎活動)
    └── lottery_prize (獎品定義)
            - A賞: 數量 3
            - B賞: 數量 5
            - C賞: 數量 10
            ...
    └── lottery_ticket (籤位)
            - 1號 → 分配到 C賞
            - 2號 → 分配到 F賞
            - 3號 → 謝謝惠顧
            ...
            - 13號 → 分配到 A賞 (隨機)
            - 45號 → 分配到 A賞 (隨機)
            - 76號 → 分配到 A賞 (隨機)
            ...
```

### 2. 前台安全原則

```java
// ❌ 錯誤：直接返回所有資訊
return tickets;

// ✅ 正確：過濾未抽籤位的獎品資訊
return tickets.stream()
    .map(LotteryTicketRes::forFrontend)
    .collect(Collectors.toList());
```

### 3. 免單機制

```
條件：
1. 是開套玩家（第一個抽的人）
2. 在保護抽數內（例如 5 抽內）
3. 中大獎（is_grand_prize = 1）

→ 退還已花費金額
```

---

## 測試建議

1. **單元測試**：測試籤位隨機分配演算法
2. **整合測試**：測試完整抽獎流程
3. **併發測試**：測試多人同時抽獎時的鎖定機制
4. **安全測試**：確認前台 API 不會洩漏獎品資訊
