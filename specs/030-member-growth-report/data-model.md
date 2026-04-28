# Data Model: 030 - 會員成長報表

**Branch**: `030-member-growth-report` | **Phase**: 1 — Design & Contracts

---

## 1. 查詢來源實體（現有，無需修改）

### User（前台會員）

| DB 欄位 | Java 欄位 | 類型 | 用途 |
|---|---|---|---|
| `id` | `id` | `String` | 會員 ID |
| `provider` | `provider` | `String` | 註冊方式：`EMAIL` / `GOOGLE` |
| `created_at` | `createdAt` | `LocalDateTime` | 新增會員篩選日期 |
| `last_login_at` | `lastLoginAt` | `LocalDateTime` | 活躍判斷（登入行為） |

### LotteryTicket（籤位）

| DB 欄位 | Java 欄位 | 類型 | 用途 |
|---|---|---|---|
| `drawn_by` | `drawnBy` | `String` | 抽獎用戶 ID（userId） |
| `status` | `status` | `String` | 篩選 `DRAWN` 狀態 |
| `drawn_at` | `drawnAt` | `LocalDateTime` | 活躍判斷（抽獎行為） |

### WalletTransaction（錢包紀錄）

| DB 欄位 | Java 欄位 | 類型 | 用途 |
|---|---|---|---|
| `user_id` | `userId` | `String` | 會員 ID |
| `transaction_type` | `transactionType` | `String` | `DRAW`（抽獎消費）/ `RECHARGE`（儲值，活躍判斷） |
| `coin_type` | `coinType` | `String` | `GOLD` / `BONUS`，用於分開 ARPU |
| `amount` | `amount` | `Long` | 消費金額（ARPU 分子） |
| `created_at` | `createdAt` | `LocalDateTime` | 活躍判斷 + ARPU 篩選 |

### Order（訂單）

| DB 欄位 | Java 欄位 | 類型 | 用途 |
|---|---|---|---|
| `user_id` | `userId` | `String` | 活躍判斷（建立訂單行為） |
| `created_at` | `createdAt` | `LocalDateTime` | 活躍判斷日期篩選 |

---

## 2. 新增：查詢條件類別

### `MemberGrowthReportCondition`

**Package**: `com.group.admin.condition.report`  
**Extends**: `BaseCondition`

```java
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "會員成長報表查詢條件")
public class MemberGrowthReportCondition extends BaseCondition {

    @Schema(description = "開始日期（預設：今天 - 29 天）", example = "2026-04-01")
    private LocalDate startDate;

    @Schema(description = "結束日期（預設：今天）", example = "2026-04-30")
    private LocalDate endDate;
}
```

**Notes**:
- 不含 `storeId`（Admin-only，無店家過濾）
- 日期為 null 時後端補預設值（最近 30 天）

---

## 3. 新增：回應 DTO

### `MemberGrowthReportRes`

**Package**: `com.group.admin.dto.res.report`

```java
@Data
@Builder
public class MemberGrowthReportRes {

    // === 查詢期間 ===
    private LocalDate startDate;
    private LocalDate endDate;

    // === 新增會員統計 ===
    /** 查詢期間總新增會員數 */
    private Integer totalNewMembers;

    /** 與上期相比成長率（%），上期無資料時為 null */
    private BigDecimal growthRate;

    /** 按 provider 分類：{ "EMAIL": 70, "GOOGLE": 80 } */
    private Map<String, Integer> registrationByProvider;

    /** 每日新增明細，長度 = (endDate - startDate + 1) 天 */
    private List<DailyNewMember> dailyNewMembers;

    // === 活躍度與 ARPU ===
    /** 活躍會員數（有登入/儲值/抽獎/訂單任一行為的不重複會員） */
    private Integer activeMembers;

    /** 金幣 ARPU = 期間 DRAW+GOLD 總消費 / activeMembers，精確到 0.1 */
    private BigDecimal arpuGold;

    /** 紅利 ARPU = 期間 DRAW+BONUS 總消費 / activeMembers，精確到 0.1 */
    private BigDecimal arpuBonus;

    // === 留存率 ===
    /** 7 天留存率（%），計算基準為前一完整月新增會員 */
    private BigDecimal retention7Days;

    /** 30 天留存率（%），計算基準為前一完整月新增會員 */
    private BigDecimal retention30Days;

    // === 巢狀 DTO ===
    @Data
    @Builder
    public static class DailyNewMember {
        /** 日期 */
        private LocalDate date;
        /** 當日新增會員數 */
        private Integer count;
    }
}
```

---

## 4. 服務層擴充

### `ReportService` 介面（新增方法）

```java
/**
 * 會員成長報表
 * @param req 查詢條件（startDate, endDate）
 */
MemberGrowthReportRes getMemberGrowthReport(QueryReq<MemberGrowthReportCondition> req);
```

### `ReportServiceImpl` 實作摘要

| 查詢 # | SQL 目的 | 主要 WHERE 條件 |
|---|---|---|
| Q1 | 新增會員總數 + 上期比較 | `user.created_at BETWEEN start AND end` |
| Q2 | 每日新增明細 | `GROUP BY DATE(user.created_at)` |
| Q3 | 按 provider 分類 | `GROUP BY user.provider` |
| Q4 | 活躍會員數（UNION） | 4 表 UNION DISTINCT userId |
| Q5 | arpuGold | `transaction_type='DRAW' AND coin_type='GOLD'` |
| Q6 | arpuBonus | `transaction_type='DRAW' AND coin_type='BONUS'` |
| Q7 | 留存率基數（前月新增） | `user.created_at BETWEEN prevMonthStart AND prevMonthEnd` |
| Q8 | 7/30 天留存計算 | 前月新增會員 × 7/30 天活躍 UNION |

**ARPU 公式**：
```
arpuGold  = SUM(amount WHERE type=DRAW AND coin=GOLD)  / activeMembers，scale=1，HALF_UP
arpuBonus = SUM(amount WHERE type=DRAW AND coin=BONUS) / activeMembers，scale=1，HALF_UP
activeMembers = 0 時，兩者均回傳 0.0
```

**留存率公式**：
```
retention7Days  = (前月新增中、7 天內有活躍的人數  / 前月新增總數) × 100，scale=1，HALF_UP
retention30Days = (前月新增中、30 天內有活躍的人數 / 前月新增總數) × 100，scale=1，HALF_UP
前月新增總數 = 0 時，回傳 null（無法計算）
```

---

## 5. Controller 擴充

### `AdminReportController`（新增端點）

```java
/**
 * 會員成長報表（Admin Only）
 */
@PostMapping("/member-growth")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<MemberGrowthReportRes> getMemberGrowthReport(
        @RequestBody QueryReq<MemberGrowthReportCondition> req) {
    return ResponseEntity.ok(reportService.getMemberGrowthReport(req));
}
```

**Notes**:
- 無 storeId 注入邏輯（Admin-only，不過濾店家）
- 採 `hasRole('ADMIN')` 而非 `hasAnyRole('ADMIN', 'STORE_OWNER')`

---

## 6. 狀態轉移

無新增 Entity 狀態，僅查詢現有資料。

---

## 7. 驗收場景 → SQL 對照

| 驗收場景 | 對應 SQL | 欄位 |
|---|---|---|
| US1-SC1：totalNewMembers=150 | Q1 COUNT(*) | `user.created_at` |
| US1-SC2：registrationByProvider={GOOGLE:80, EMAIL:70} | Q3 GROUP BY provider | `user.provider` |
| US1-SC3：dailyNewMembers 長度=30 | Q2 GROUP BY date | Service 端補零 |
| US2-SC1：activeMembers=500, arpuGold=200.0 | Q4 + Q5 | UNION + DRAW+GOLD |
| US2-SC2：僅紅利消費仍計入 activeMembers | Q4 UNION 包含 DRAW+BONUS | coin_type 判斷 |
| US3-SC1：retention7Days=60.0 | Q8（7天窗口） | 前月新增 + 7天活躍 |
| US3-SC2：retention30Days=35.0 | Q8（30天窗口） | 前月新增 + 30天活躍 |
