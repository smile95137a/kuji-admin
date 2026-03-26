# 合約：GET /admin/referral-stats

**含計數與時間軸的店家層級推薦統計**

## 基本資訊

| 欄位 | 值 |
|-------|-------|
| Method | GET |
| Path | `/admin/referral-stats` |
| Auth | Bearer JWT (ROLE_ADMIN) |
| Controller | `AdminReferralCodeController.getReferralStats()`（新增至現有 Controller） |
| Service | `ReferralCodeService.getReferralStats(ReferralReportCondition)` |
| Repository | `ReferralCodeRepository.selectStatsByStore(condition)` |
| Status | NEW — no equivalent endpoint exists |

## 請求

### 標頭
```
Authorization: Bearer <admin_jwt_token>
```

### 查詢參數

| 參數 | 型別 | 必填 | 說明 |
|-----------|------|----------|-------------|
| storeId | String | 否 | 篩選至特定店家；若省略則回傳所有店家 |
| startDate | String (yyyy-MM-dd) | 否 | 時間軸起始（預設：30 天前） |
| endDate | String (yyyy-MM-dd) | 否 | 時間軸結束（預設：今天） |

## 回應

### 200 OK — 所有店家
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "storeId": "550e8400-e29b-41d4-a716-446655440000",
      "storeName": "Dream Store",
      "totalReferrals": 50,
      "activeCodeCount": 3,
      "timeline": [
        { "date": "2026-03-01", "count": 5 },
        { "date": "2026-03-02", "count": 3 },
        { "date": "2026-03-03", "count": 8 },
        { "date": "2026-03-22", "count": 2 }
      ]
    },
    {
      "storeId": "550e8400-e29b-41d4-a716-446655440003",
      "storeName": "Another Store",
      "totalReferrals": 0,
      "activeCodeCount": 1,
      "timeline": []
    }
  ]
}
```

### 200 OK — 依 storeId 篩選
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "storeId": "550e8400-e29b-41d4-a716-446655440000",
      "storeName": "Dream Store",
      "totalReferrals": 50,
      "activeCodeCount": 3,
      "timeline": [
        { "date": "2026-03-01", "count": 5 }
      ]
    }
  ]
}
```

### 400 Bad Request — 日期範圍無效
```json
{
  "code": 400,
  "message": "開始日期不能晚於結束日期",
  "data": null
}
```

## 回應 DTO：ReferralStatsRes（新增）

```java
@Data
public class ReferralStatsRes {
    private String storeId;
    private String storeName;
    private Long totalReferrals;
    private Long activeCodeCount;
    private List<DailyCount> timeline;

    @Data
    @AllArgsConstructor
    public static class DailyCount {
        private String date;   // "yyyy-MM-dd"
        private Long count;
    }
}
```

## SQL 實作（在 ReferralCodeRepository 中）

```sql
-- Step 1: Per-store totals + active code count
SELECT
    s.id        AS storeId,
    s.store_name AS storeName,
    COUNT(DISTINCT rr.id) AS totalReferrals,
    SUM(CASE WHEN rc.is_active = 1 THEN 1 ELSE 0 END) AS activeCodeCount
FROM store s
LEFT JOIN referral_code rc ON rc.store_id = s.id
LEFT JOIN referral_record rr ON rr.referral_code_id = rc.id
    AND rr.referred_at BETWEEN #{startDate} AND #{endDate}
WHERE s.status = 'ACTIVE'
  AND (#{storeId} IS NULL OR s.id = #{storeId})
GROUP BY s.id, s.store_name

-- Step 2: Timeline (daily aggregation)
SELECT
    rr.store_id AS storeId,
    DATE(rr.referred_at) AS referralDate,
    COUNT(*) AS dailyCount
FROM referral_record rr
WHERE rr.referred_at BETWEEN #{startDate} AND #{endDate}
  AND (#{storeId} IS NULL OR rr.store_id = #{storeId})
GROUP BY rr.store_id, DATE(rr.referred_at)
ORDER BY referralDate ASC
```

Service 依 storeId 合併兩個結果集。

## 業務規則

1. 零推薦數的店家仍包含在結果中（LEFT JOIN）——顯示 `totalReferrals: 0, timeline: []`
2. 若未指定日期範圍，預設為最近 30 天
3. 結果中僅包含 ACTIVE 狀態的店家
4. 時間軸條目依日期升冪排序
5. 滿足 SC-003：管理員可在 ≤ 3 次點擊內從儀表板到達此端點
