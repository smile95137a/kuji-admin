package com.group.admin.controller.admin;

import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotteryDrawRecord;
import com.group.admin.entity.LotteryPrize;
import com.group.admin.example.LotteryDrawRecordExample;
import com.group.admin.example.LotteryPrizeExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.LotteryDrawRecordMapper;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.LotteryPrizeMapper;
import com.group.admin.req.draw.AdminDrawHistoryReq;
import com.group.admin.res.draw.AdminDrawHistoryRes;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 後台抽獎歷史查詢 API
 * 
 * - ROLE_ADMIN: 可查詢所有抽獎活動
 * - ROLE_STORE_OWNER: 只能查詢自己店家的抽獎活動
 */
@Slf4j
@RestController
@RequestMapping("/admin/lottery")
@RequiredArgsConstructor
@Tag(name = "後台抽獎歷史", description = "查詢抽獎歷史記錄與統計")
public class AdminDrawHistoryController {

    private final LotteryMapper lotteryMapper;
    private final LotteryDrawRecordMapper drawRecordMapper;
    private final LotteryPrizeMapper lotteryPrizeMapper;

    /**
     * 查詢指定商品的抽獎歷史（分頁）
     * 
     * ROLE_STORE_OWNER 只能查詢自己店家的商品
     */
    @GetMapping("/{id:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}}/draws")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @Operation(summary = "查詢抽獎歷史", description = "分頁查詢指定商品的抽獎記錄")
    public ResponseEntity<AdminDrawHistoryRes> getDrawHistory(
            @PathVariable String id,
            @ModelAttribute AdminDrawHistoryReq req) {

        String callerId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isAdmin();

        log.info("🔍 查詢抽獎歷史: lotteryId={}, callerId={}, isAdmin={}", id, callerId, isAdmin);

        // 1. 驗證商品存在
        Lottery lottery = lotteryMapper.selectByPrimaryKey(id);
        if (lottery == null) {
            throw new BusinessException("商品不存在");
        }

        // 2. ROLE_STORE_OWNER 資料隔離
        if (!isAdmin) {
            String callerStoreId = SecurityUtils.getCurrentUserPrimaryStoreId();
            if (callerStoreId == null || !callerStoreId.equals(lottery.getStoreId())) {
                throw new BusinessException("無權限查詢此商品的抽獎記錄");
            }
        }

        // 3. 分頁查詢
        int page = req.getPage() != null ? req.getPage() : 1;
        int size = req.getSize() != null ? req.getSize() : 20;
        int offset = (page - 1) * size;

        List<LotteryDrawRecord> records = drawRecordMapper.selectByLotteryIdPaged(
                id, req.getUserId(), req.getStatus(),
                req.getStartDate(), req.getEndDate(),
                size, offset);

        long total = drawRecordMapper.countByLotteryIdFiltered(
                id, req.getUserId(), req.getStatus(),
                req.getStartDate(), req.getEndDate());

        // 4. Build prize lookup for display names
        LotteryPrizeExample prizeExample = new LotteryPrizeExample();
        prizeExample.createCriteria().andLotteryIdEqualTo(id);
        List<LotteryPrize> prizes = lotteryPrizeMapper.selectByExample(prizeExample);
        Map<String, LotteryPrize> prizeMap = prizes.stream()
                .collect(Collectors.toMap(LotteryPrize::getId, p -> p));

        // 5. Convert records
        List<AdminDrawHistoryRes.DrawRecordItem> items = new ArrayList<>();
        for (LotteryDrawRecord r : records) {
            AdminDrawHistoryRes.DrawRecordItem item = new AdminDrawHistoryRes.DrawRecordItem();
            item.setId(r.getId());
            item.setLotteryId(r.getLotteryId());
            item.setUserId(r.getUserId());
            item.setPrizeId(r.getPrizeId());
            item.setCostType(r.getCostType());
            item.setCostAmount(r.getCostAmount());
            item.setStatus(r.getStatus());
            item.setCreatedAt(r.getCreatedAt());

            if (r.getPrizeId() != null) {
                LotteryPrize prize = prizeMap.get(r.getPrizeId());
                if (prize != null) {
                    item.setPrizeName(prize.getName());
                    item.setPrizeLevel(prize.getLevel());
                    item.setPrizeImageUrl(prize.getImageUrl());
                    item.setIsLastPrize(prize.getIsLastPrize() != null && prize.getIsLastPrize() == 1);
                }
            }
            items.add(item);
        }

        // 6. Summary statistics
        LotteryDrawRecordExample allExample = new LotteryDrawRecordExample();
        allExample.createCriteria().andLotteryIdEqualTo(id);
        List<LotteryDrawRecord> allRecords = drawRecordMapper.selectByExample(allExample);

        long successDraws = allRecords.stream().filter(r -> "SUCCESS".equals(r.getStatus())).count();
        long failedDraws = allRecords.stream().filter(r -> "FAILED".equals(r.getStatus())).count();
        long totalRevenue = allRecords.stream()
                .filter(r -> "SUCCESS".equals(r.getStatus()) && r.getCostAmount() != null)
                .mapToLong(LotteryDrawRecord::getCostAmount).sum();

        // Count remaining draws from prize pool
        int remainingDraws = prizes.stream()
                .mapToInt(p -> p.getRemaining() != null ? p.getRemaining() : 0)
                .sum();

        AdminDrawHistoryRes.DrawSummary summary = new AdminDrawHistoryRes.DrawSummary();
        summary.setTotalDraws(allRecords.size());
        summary.setSuccessDraws(successDraws);
        summary.setFailedDraws(failedDraws);
        summary.setTotalRevenue(totalRevenue);
        summary.setRemainingDraws(remainingDraws);

        // 7. Build response
        AdminDrawHistoryRes res = new AdminDrawHistoryRes();
        res.setPage(page);
        res.setSize(size);
        res.setTotal(total);
        res.setTotalPages((int) Math.ceil((double) total / size));
        res.setRecords(items);
        res.setSummary(summary);

        log.info("✅ 查詢成功: lotteryId={}, total={}", id, total);
        return ResponseEntity.ok(res);
    }
}
