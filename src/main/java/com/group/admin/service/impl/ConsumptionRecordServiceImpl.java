package com.group.admin.service.impl;

import com.group.admin.entity.ConsumptionRecord;
import com.group.admin.mapper.ConsumptionRecordMapper;
import com.group.admin.repository.ConsumptionRecordRepository;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.consumption.ConsumptionRecordCondition;
import com.group.admin.res.consumption.ConsumptionRecordRes;
import com.group.admin.service.ConsumptionRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 消費紀錄 Service 實作
 * 
 * ⚠️ 只記錄金幣/紅利消費 與 運費支付，儲值不屬於消費紀錄
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumptionRecordServiceImpl implements ConsumptionRecordService {

    private final ConsumptionRecordMapper consumptionRecordMapper;
    private final ConsumptionRecordRepository consumptionRecordRepository;

    @Override
    @Transactional
    public void recordConsumption(String userId, String type, String lotteryId, String lotteryTitle,
                                  String orderId, String orderNumber, Long goldAmount, Long bonusAmount, String description) {
        log.info("💰 記錄消費: userId={}, type={}, gold={}, bonus={}", userId, type, goldAmount, bonusAmount);
        
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
        log.info("✅ 消費紀錄已建立, ID={}", record.getId());
    }

    @Override
    public List<ConsumptionRecordRes> getMyConsumptions(String userId, QueryReq<ConsumptionRecordCondition> req) {
        log.info("📋 查詢用戶消費紀錄: userId={}", userId);
        
        // 查該用戶的所有紀錄
        List<ConsumptionRecord> records = consumptionRecordRepository.selectByUserId(userId);
        
        ConsumptionRecordCondition condition = req != null ? req.getCondition() : null;
        
        // 動態篩選
        List<ConsumptionRecord> filtered = filterRecords(records, condition);
        
        log.info("✅ 查詢到 {} 筆用戶消費紀錄", filtered.size());
        return filtered.stream().map(this::convertToRes).collect(Collectors.toList());
    }

    @Override
    public List<ConsumptionRecordRes> queryConsumptions(QueryReq<ConsumptionRecordCondition> req) {
        log.info("📋 後台查詢所有消費紀錄");
        
        // 查全部紀錄
        List<ConsumptionRecord> records = consumptionRecordRepository.selectAll();
        
        ConsumptionRecordCondition condition = req != null ? req.getCondition() : null;
        
        // 動態篩選
        List<ConsumptionRecord> filtered = filterRecords(records, condition);
        
        log.info("✅ 查詢到 {} 筆消費紀錄", filtered.size());
        return filtered.stream().map(this::convertToRes).collect(Collectors.toList());
    }

    /**
     * 通用篩選邏輯
     */
    private List<ConsumptionRecord> filterRecords(List<ConsumptionRecord> records, ConsumptionRecordCondition condition) {
        return records.stream()
            .filter(record -> {
                if (condition == null) return true;
                
                // 用戶 ID
                if (isNotBlank(condition.getUserId()) 
                    && !condition.getUserId().equals(record.getUserId())) {
                    return false;
                }
                
                // 消費類型
                if (isNotBlank(condition.getType()) 
                    && !condition.getType().equals(record.getType())) {
                    return false;
                }
                
                // 賞品 ID
                if (isNotBlank(condition.getLotteryId()) 
                    && !condition.getLotteryId().equals(record.getLotteryId())) {
                    return false;
                }
                
                // 訂單編號
                if (isNotBlank(condition.getOrderNumber()) 
                    && record.getOrderNumber() != null
                    && !record.getOrderNumber().contains(condition.getOrderNumber())) {
                    return false;
                }
                
                // 關鍵字搜尋
                if (isNotBlank(condition.getKeyword())) {
                    String kw = condition.getKeyword().toLowerCase();
                    boolean match = (record.getLotteryTitle() != null && record.getLotteryTitle().toLowerCase().contains(kw))
                            || (record.getOrderNumber() != null && record.getOrderNumber().toLowerCase().contains(kw))
                            || (record.getDescription() != null && record.getDescription().toLowerCase().contains(kw));
                    if (!match) return false;
                }
                
                // 時間範圍篩選（BaseCondition 使用 LocalDate）
                if (condition.getCreatedAtStart() != null
                    && record.getCreatedAt() != null
                    && record.getCreatedAt().toLocalDate().isBefore(condition.getCreatedAtStart())) {
                    return false;
                }
                if (condition.getCreatedAtEnd() != null
                    && record.getCreatedAt() != null
                    && record.getCreatedAt().toLocalDate().isAfter(condition.getCreatedAtEnd())) {
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
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
        if (type == null) return "未知";
        switch (type) {
            case "DRAW_GOLD": return "金幣抽獎消費";
            case "DRAW_BONUS": return "紅利抽獎消費";
            case "SHIPPING_FEE": return "運費支付";
            default: return type;
        }
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
