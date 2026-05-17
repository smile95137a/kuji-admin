package com.group.admin.service.impl;

import com.group.admin.entity.ConsumptionRecord;
import com.group.admin.mapper.ConsumptionRecordMapper;
import com.group.admin.repository.ConsumptionRecordRepository;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.consumption.ConsumptionRecordCondition;
import com.group.admin.res.PageResult;
import com.group.admin.res.consumption.ConsumptionRecordRes;
import com.group.admin.service.ConsumptionRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumptionRecordServiceImpl implements ConsumptionRecordService {

    private final ConsumptionRecordMapper consumptionRecordMapper;
    private final ConsumptionRecordRepository consumptionRecordRepository;

    @Override
    @Transactional
    public void recordConsumption(String userId, String type, String lotteryId, String lotteryTitle,
                                  String orderId, String orderNumber, Long goldAmount, Long bonusAmount,
                                  String description) {
        log.info("建立消費紀錄: userId={}, type={}, gold={}, bonus={}", userId, type, goldAmount, bonusAmount);

        ConsumptionRecord record = new ConsumptionRecord();
        record.setId(UUID.randomUUID().toString());
        record.setUserId(userId);
        record.setType(type);
        record.setLotteryId(lotteryId);
        record.setLotteryTitle(lotteryTitle);
        record.setOrderId(orderId);
        record.setOrderNumber(orderNumber);
        record.setGoldAmount(goldAmount);
        record.setBonusAmount(bonusAmount);
        record.setDescription(description);
        record.setCreatedAt(LocalDateTime.now());

        consumptionRecordMapper.insert(record);
        log.info("消費紀錄已建立, id={}", record.getId());
    }

    @Override
    public PageResult<ConsumptionRecordRes> getMyConsumptions(String userId, QueryReq<ConsumptionRecordCondition> req) {
        log.info("查詢我的消費紀錄: userId={}", userId);

        QueryReq<ConsumptionRecordCondition> safeReq = normalizeReq(req);
        ConsumptionRecordCondition condition = safeReq.getCondition();
        condition.setUserId(userId);

        int page = resolvePage(safeReq.getPage());
        int size = resolveSize(safeReq.getSize());
        int offset = (page - 1) * size;

        long total = consumptionRecordRepository.countByCondition(condition);
        if (total == 0) {
            return PageResult.empty(page, size);
        }

        var records = consumptionRecordRepository.selectByConditionPaged(condition, offset, size);
        var items = records.stream().map(this::convertToRes).collect(Collectors.toList());

        log.info("我的消費紀錄共 {} 筆，本頁回傳 {} 筆", total, items.size());
        return PageResult.of(page, size, total, items);
    }

    @Override
    public PageResult<ConsumptionRecordRes> queryConsumptions(QueryReq<ConsumptionRecordCondition> req) {
        log.info("後台查詢消費紀錄");

        QueryReq<ConsumptionRecordCondition> safeReq = normalizeReq(req);
        ConsumptionRecordCondition condition = safeReq.getCondition();
        int page = resolvePage(safeReq.getPage());
        int size = resolveSize(safeReq.getSize());
        int offset = (page - 1) * size;

        long total = consumptionRecordRepository.countByCondition(condition);
        if (total == 0) {
            return PageResult.empty(page, size);
        }

        var records = consumptionRecordRepository.selectByConditionPaged(condition, offset, size);
        var items = records.stream().map(this::convertToRes).collect(Collectors.toList());

        log.info("消費紀錄共 {} 筆，本頁回傳 {} 筆", total, items.size());
        return PageResult.of(page, size, total, items);
    }

    private QueryReq<ConsumptionRecordCondition> normalizeReq(QueryReq<ConsumptionRecordCondition> req) {
        if (req == null) {
            req = new QueryReq<>();
        }
        if (req.getCondition() == null) {
            req.setCondition(new ConsumptionRecordCondition());
        }
        return req;
    }

    private int resolvePage(Integer page) {
        return page != null && page > 0 ? page : 1;
    }

    private int resolveSize(Integer size) {
        if (size == null || size < 1) {
            return 20;
        }
        return Math.min(size, 100);
    }

    private ConsumptionRecordRes convertToRes(ConsumptionRecord record) {
        return ConsumptionRecordRes.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .type(record.getType())
                .typeName(getTypeName(record.getType()))
                .lotteryId(record.getLotteryId())
                .lotteryTitle(record.getLotteryTitle())
                .orderId(record.getOrderId())
                .orderNumber(record.getOrderNumber())
                .goldAmount(record.getGoldAmount())
                .bonusAmount(record.getBonusAmount())
                .description(record.getDescription())
                .createdAt(record.getCreatedAt())
                .build();
    }

    private String getTypeName(String type) {
        if (type == null) {
            return "未知類型";
        }
        return switch (type) {
            case "DRAW_GOLD" -> "抽獎扣款（金幣）";
            case "DRAW_BONUS" -> "抽獎扣款（紅利）";
            case "SHIPPING_FEE" -> "配送運費";
            case "FREE_DRAW_REFUND" -> "免單退款";
            default -> type;
        };
    }
}
