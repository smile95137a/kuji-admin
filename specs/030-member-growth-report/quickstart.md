# Quickstart: 030 - 會員成長報表

**Branch**: `030-member-growth-report` | **Phase**: 1 — Developer Guide

---

## 快速概覽

這個功能在**現有 `AdminReportController` + `ReportService` 架構**上新增一支端點，不引入新框架。  
需要建立 **3 支新 Java 檔案** + 在 **2 支現有檔案** 新增方法。

---

## Step 1：建立 Condition 類別

**檔案**：`src/main/java/com/group/admin/condition/report/MemberGrowthReportCondition.java`

```java
package com.group.admin.condition.report;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

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

---

## Step 2：建立 Response DTO

**檔案**：`src/main/java/com/group/admin/dto/res/report/MemberGrowthReportRes.java`

```java
package com.group.admin.dto.res.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class MemberGrowthReportRes {

    private LocalDate startDate;
    private LocalDate endDate;

    // 新增會員
    private Integer totalNewMembers;
    private BigDecimal growthRate;
    private Map<String, Integer> registrationByProvider;
    private List<DailyNewMember> dailyNewMembers;

    // 活躍度與 ARPU
    private Integer activeMembers;
    private BigDecimal arpuGold;
    private BigDecimal arpuBonus;

    // 留存率
    private BigDecimal retention7Days;
    private BigDecimal retention30Days;

    @Data
    @Builder
    public static class DailyNewMember {
        private LocalDate date;
        private Integer count;
    }
}
```

---

## Step 3：擴充 ReportService 介面

**檔案**：`src/main/java/com/group/admin/service/ReportService.java`

在現有方法後新增：

```java
/**
 * 會員成長報表
 */
MemberGrowthReportRes getMemberGrowthReport(QueryReq<MemberGrowthReportCondition> req);
```

---

## Step 4：實作 ReportServiceImpl

**檔案**：`src/main/java/com/group/admin/service/impl/ReportServiceImpl.java`

新增 `@Override` 方法，關鍵邏輯片段：

```java
@Override
public MemberGrowthReportRes getMemberGrowthReport(QueryReq<MemberGrowthReportCondition> req) {
    MemberGrowthReportCondition condition = req != null && req.getCondition() != null
        ? req.getCondition() : new MemberGrowthReportCondition();

    // 預設日期：最近 30 天
    LocalDate endDate   = condition.getEndDate()   != null ? condition.getEndDate()   : LocalDate.now();
    LocalDate startDate = condition.getStartDate() != null ? condition.getStartDate() : endDate.minusDays(29);

    log.info("📊 產生會員成長報表: {} ~ {}", startDate, endDate);

    LocalDateTime startDt = startDate.atStartOfDay();
    LocalDateTime endDt   = endDate.plusDays(1).atStartOfDay();

    // Q1: 新增會員總數
    Integer totalNew = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM user WHERE created_at BETWEEN ? AND ?",
        Integer.class, startDt, endDt);

    // Q2: 每日明細（補零邏輯在 service 端）
    // Q3: 按 provider 分類
    // Q4: 活躍會員 UNION
    // Q5/Q6: ARPU Gold/Bonus
    // Q7/Q8: 留存率（前月新增 × 活躍窗口）

    // 詳細 SQL 見 data-model.md § 服務層擴充
    ...
}
```

**⚠️ 注意事項**：
- ARPU 除以 `activeMembers`，需先判斷是否為 0（避免 `ArithmeticException`）
- `dailyNewMembers` 必須對**查詢範圍每一天**補零（iterate `startDate` → `endDate`）
- 留存率 SQL 使用**前月**起訖，與查詢 `startDate/endDate` 無關

---

## Step 5：擴充 AdminReportController

**檔案**：`src/main/java/com/group/admin/controller/admin/AdminReportController.java`

在現有方法後新增：

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

**⚠️ 注意**：不需要 `storeId` 注入邏輯（此 API 為 Admin-only，無店家過濾）

---

## Step 6：測試

**執行現有測試**：
```bash
mvn test -pl admin
```

**測試重點** (`AdminReportControllerTest` 或新增 `MemberGrowthReportControllerTest`)：

| 測試 | 驗證 |
|---|---|
| 呼叫 `/admin/report/member-growth`，ADMIN token | HTTP 200，結構完整 |
| 呼叫同端點，STORE_OWNER token | HTTP 403 |
| condition = null（使用預設日期） | HTTP 200，startDate = today-29 |
| 查詢無資料期間 | totalNewMembers=0，arpuGold=0.0，dailyNewMembers 補零 |
| activeMembers=0 時 ARPU | arpuGold=0.0，arpuBonus=0.0（非例外） |

---

## 對照表：新增 vs 修改

| 動作 | 檔案 | 備註 |
|---|---|---|
| 新增 | `condition/report/MemberGrowthReportCondition.java` | Step 1 |
| 新增 | `dto/res/report/MemberGrowthReportRes.java` | Step 2 |
| 修改 | `service/ReportService.java` | 新增 1 個方法簽名 |
| 修改 | `service/impl/ReportServiceImpl.java` | 新增 1 個 @Override 方法（主要工作） |
| 修改 | `controller/admin/AdminReportController.java` | 新增 1 個端點方法 |
| 新增/修改 | 對應 Controller 測試類 | MockMvc + Mockito |

---

## 參考

- **API 合約**：[contracts/POST_admin_report_member-growth.md](contracts/POST_admin_report_member-growth.md)
- **資料模型**：[data-model.md](data-model.md)
- **研究結論**：[research.md](research.md)（特別是 §1 交易類型對應、§2 活躍會員定義）
- **現有參考實作**：`ReportServiceImpl.getRevenueReport()` / `getRechargeReport()`
