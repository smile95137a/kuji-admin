package com.group.admin.service.impl;

import com.group.admin.entity.Lottery;
import com.group.admin.example.LotteryDrawRecordExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.LotteryDrawRecordMapper;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.req.draw.AdminDrawHistoryReq;
import com.group.admin.res.draw.AdminDrawHistoryRes;
import com.group.admin.service.AdminDrawHistoryService;
import com.group.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 後台抽獎歷史查詢服務實作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDrawHistoryServiceImpl implements AdminDrawHistoryService {

    private final LotteryMapper lotteryMapper;
    private final LotteryDrawRecordMapper lotteryDrawRecordMapper;

    @Override
    public AdminDrawHistoryRes getDrawHistory(String lotteryId, String callerId, String callerRole,
                                               AdminDrawHistoryReq req) {
        log.info("📊 查詢抽獎歷史: lotteryId={}, callerId={}, role={}", lotteryId, callerId, callerRole);

        // 驗證 Lottery 存在
        Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
        if (lottery == null) {
            throw new BusinessException("LOTTERY_NOT_FOUND", "抽獎活動不存在");
        }

        // STORE_OWNER 權限檢查：只能查看自己店家的抽獎
        if ("ROLE_STORE_OWNER".equals(callerRole)) {
            String callerStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
            if (callerStoreId != null && !callerStoreId.equals(lottery.getStoreId())) {
                throw new BusinessException("ACCESS_DENIED", "無權限查看其他店家的抽獎記錄");
            }
        }

        // 預設分頁參數
        if (req == null) req = new AdminDrawHistoryReq();
        int page = req.getPage() != null ? req.getPage() : 1;
        int size = req.getSize() != null ? req.getSize() : 20;
        int offset = (page - 1) * size;

        // 查詢分頁記錄
        List<Map<String, Object>> rawRecords = lotteryDrawRecordMapper.selectByLotteryIdPaged(
                lotteryId, req.getUserId(), req.getStatus(),
                req.getStartDate(), req.getEndDate(),
                size, offset);

        // 查詢總數
        long total = lotteryDrawRecordMapper.countByLotteryIdFiltered(
                lotteryId, req.getUserId(), req.getStatus(),
                req.getStartDate(), req.getEndDate());

        // 轉換為 DrawRecordItem
        List<AdminDrawHistoryRes.DrawRecordItem> records = rawRecords.stream()
                .map(this::toDrawRecordItem)
                .collect(Collectors.toList());

        // 計算 summary
        LotteryDrawRecordExample totalExample = new LotteryDrawRecordExample();
        totalExample.createCriteria().andLotteryIdEqualTo(lotteryId);
        long totalDraws = lotteryDrawRecordMapper.countByExample(totalExample);

        LotteryDrawRecordExample successExample = new LotteryDrawRecordExample();
        successExample.createCriteria().andLotteryIdEqualTo(lotteryId).andStatusEqualTo("SUCCESS");
        long successDraws = lotteryDrawRecordMapper.countByExample(successExample);

        LotteryDrawRecordExample failedExample = new LotteryDrawRecordExample();
        failedExample.createCriteria().andLotteryIdEqualTo(lotteryId).andStatusEqualTo("FAILED");
        long failedDraws = lotteryDrawRecordMapper.countByExample(failedExample);

        Long totalRevenue = lotteryDrawRecordMapper.sumCostAmountByLotteryId(lotteryId);

        int remainingDraws = lottery.getTotalDraws() != null ? lottery.getTotalDraws() : 0;

        AdminDrawHistoryRes.DrawSummary summary = AdminDrawHistoryRes.DrawSummary.builder()
                .totalDraws(totalDraws)
                .successDraws(successDraws)
                .failedDraws(failedDraws)
                .totalRevenue(totalRevenue != null ? totalRevenue : 0L)
                .remainingDraws(remainingDraws)
                .build();

        int totalPages = (int) Math.ceil((double) total / size);

        return AdminDrawHistoryRes.builder()
                .page(page)
                .size(size)
                .total(total)
                .totalPages(totalPages)
                .records(records)
                .summary(summary)
                .build();
    }

    private AdminDrawHistoryRes.DrawRecordItem toDrawRecordItem(Map<String, Object> row) {
        return AdminDrawHistoryRes.DrawRecordItem.builder()
                .id(getString(row, "id"))
                .lotteryId(getString(row, "lotteryId"))
                .userId(getString(row, "userId"))
                .prizeId(getString(row, "prizeId"))
                .prizeName(getString(row, "prizeName"))
                .prizeLevel(getString(row, "prizeLevel"))
                .prizeImageUrl(getString(row, "prizeImageUrl"))
                .isLastPrize(getBoolean(row, "isLastPrize"))
                .isOpenerDraw(getBoolean(row, "isOpenerDraw"))
                .triggeredFreeDraw(getBoolean(row, "triggeredFreeDraw"))
                .openerDrawCount(getInteger(row, "openerDrawCount"))
                .freeDrawRefundAmount(getLong(row, "freeDrawRefundAmount"))
                .costType(getString(row, "costType"))
                .costAmount(getLong(row, "costAmount"))
                .status(getString(row, "status"))
                .createdAt(row.get("createdAt") instanceof LocalDateTime ?
                        (LocalDateTime) row.get("createdAt") : null)
                .build();
    }

    private String getString(Map<String, Object> row, String key) {
        Object val = row.get(key);
        return val != null ? val.toString() : null;
    }

    private Long getLong(Map<String, Object> row, String key) {
        Object val = row.get(key);
        if (val instanceof Number) return ((Number) val).longValue();
        return null;
    }

    private Integer getInteger(Map<String, Object> row, String key) {
        Object val = row.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return null;
    }

    private Boolean getBoolean(Map<String, Object> row, String key) {
        Object val = row.get(key);
        if (val instanceof Number) return ((Number) val).intValue() == 1;
        if (val instanceof Boolean) return (Boolean) val;
        return null;
    }
}
