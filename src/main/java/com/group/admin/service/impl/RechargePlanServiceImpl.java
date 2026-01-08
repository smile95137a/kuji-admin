package com.group.admin.service.impl;

import com.group.admin.entity.RechargePlan;
import com.group.admin.example.RechargePlanExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.RechargePlanMapper;
import com.group.admin.req.recharge.RechargePlanCreateReq;
import com.group.admin.req.recharge.RechargePlanUpdateReq;
import com.group.admin.res.wallet.RechargePlanRes;
import com.group.admin.service.RechargePlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 儲值方案服務實作
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RechargePlanServiceImpl implements RechargePlanService {
    
    private final RechargePlanMapper rechargePlanMapper;
    
    @Override
    @Transactional
    public String createPlan(RechargePlanCreateReq req) {
        log.info("🔍 新增儲值方案：name={}, amount={}", req.getName(), req.getAmount());
        
        RechargePlan plan = new RechargePlan();
        plan.setId(UUID.randomUUID().toString());
        plan.setName(req.getName());
        plan.setDescription(req.getDescription());
        plan.setAmount(req.getAmount());
        plan.setGoldCoins(req.getGoldCoins());
        plan.setBonusCoins(req.getBonusCoins() != null ? req.getBonusCoins() : 0L);
        plan.setIsActive((byte) 1); // MyBatis 生成的 isActive 為 Byte 類型
        plan.setOrderNum(0); // 預設排序
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        
        rechargePlanMapper.insert(plan);
        
        log.info("✅ 儲值方案建立成功：planId={}", plan.getId());
        return plan.getId();
    }
    
    @Override
    @Transactional
    public void updatePlan(String id, RechargePlanUpdateReq req) {
        log.info("🔍 更新儲值方案：id={}", id);
        
        RechargePlan plan = rechargePlanMapper.selectByPrimaryKey(id);
        if (plan == null) {
            throw new BusinessException("儲值方案不存在");
        }
        
        if (req.getName() != null) {
            plan.setName(req.getName());
        }
        if (req.getDescription() != null) {
            plan.setDescription(req.getDescription());
        }
        if (req.getAmount() != null) {
            plan.setAmount(req.getAmount());
        }
        if (req.getGoldCoins() != null) {
            plan.setGoldCoins(req.getGoldCoins());
        }
        if (req.getBonusCoins() != null) {
            plan.setBonusCoins(req.getBonusCoins());
        }
        if (req.getIsActive() != null) {
            plan.setIsActive(req.getIsActive() ? (byte) 1 : (byte) 0);
        }
        
        plan.setUpdatedAt(LocalDateTime.now());
        rechargePlanMapper.updateByPrimaryKey(plan);
        
        log.info("✅ 儲值方案更新成功");
    }
    
    @Override
    @Transactional
    public void deletePlan(String id) {
        log.info("🔍 刪除儲值方案：id={}", id);
        
        RechargePlan plan = rechargePlanMapper.selectByPrimaryKey(id);
        if (plan == null) {
            throw new BusinessException("儲值方案不存在");
        }
        
        // 軟刪除
        plan.setDeletedAt(LocalDateTime.now());
        rechargePlanMapper.updateByPrimaryKey(plan);
        
        log.info("✅ 儲值方案已刪除");
    }
    
    @Override
    public List<RechargePlanRes> getActivePlans() {
        RechargePlanExample example = new RechargePlanExample();
        RechargePlanExample.Criteria criteria = example.createCriteria();
        
        // 過濾條件：啟用、未刪除
        criteria.andIsActiveEqualTo((byte) 1);
        criteria.andDeletedAtIsNull();
        
        // 活動期間篩選
        LocalDateTime now = LocalDateTime.now();
        criteria.andStartDateLessThanOrEqualTo(now);
        
        RechargePlanExample.Criteria criteria2 = example.createCriteria();
        criteria2.andEndDateIsNull();
        example.or(criteria2.andEndDateGreaterThanOrEqualTo(now));
        
        example.setOrderByClause("order_num ASC, created_at DESC");
        
        List<RechargePlan> plans = rechargePlanMapper.selectByExample(example);
        return plans.stream().map(this::convertToRes).collect(Collectors.toList());
    }
    
    @Override
    public List<RechargePlanRes> getAllPlans() {
        RechargePlanExample example = new RechargePlanExample();
        example.createCriteria().andDeletedAtIsNull();
        example.setOrderByClause("order_num ASC, created_at DESC");
        
        List<RechargePlan> plans = rechargePlanMapper.selectByExample(example);
        return plans.stream().map(this::convertToRes).collect(Collectors.toList());
    }
    
    @Override
    public RechargePlanRes getPlanDetail(String id) {
        RechargePlan plan = rechargePlanMapper.selectByPrimaryKey(id);
        if (plan == null || plan.getDeletedAt() != null) {
            throw new BusinessException("儲值方案不存在");
        }
        return convertToRes(plan);
    }
    
    /**
     * 轉換為回應 DTO
     */
    private RechargePlanRes convertToRes(RechargePlan plan) {
        // 計算優惠比例
        Long totalCoins = plan.getGoldCoins() + plan.getBonusCoins();
        Double discountRate = (totalCoins.doubleValue() / plan.getAmount().doubleValue() - 1) * 100;
        
        return RechargePlanRes.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .amount(plan.getAmount())
                .goldCoins(plan.getGoldCoins())
                .bonusCoins(plan.getBonusCoins())
                .isActive(plan.getIsActive() == 1)
                .startTime(plan.getStartDate())
                .endTime(plan.getEndDate())
                .displayOrder(plan.getOrderNum())
                .bonusPercentage(String.format("%.1f%%", discountRate))
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
