package com.group.admin.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group.admin.entity.Lottery;
import com.group.admin.entity.LotteryPrize;
import com.group.admin.enums.LotteryStatusEnum;
import com.group.admin.enums.PrizeLevelEnum;
import com.group.admin.example.LotteryPrizeExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.LotteryMapper;
import com.group.admin.mapper.LotteryPrizeMapper;
import com.group.admin.req.lottery.LotteryPrizeCreateReq;
import com.group.admin.req.lottery.LotteryPrizeUpdateReq;
import com.group.admin.res.lottery.LotteryPrizeRes;
import com.group.admin.service.LotteryPrizeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 獎項管理服務實作
 * 使用 Example 模式進行資料庫操作
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryPrizeServiceImpl implements LotteryPrizeService {

    private final LotteryPrizeMapper lotteryPrizeMapper;
    private final LotteryMapper lotteryMapper;

    @Override
    @Transactional
    public LotteryPrizeRes createPrize(LotteryPrizeCreateReq req) {
        log.info("建立獎項: lotteryId={}, name={}", req.getLotteryId(), req.getName());
        
        // 檢查商品是否存在
        Lottery lottery = lotteryMapper.selectByPrimaryKey(req.getLotteryId());
        if (lottery == null) {
            throw new BusinessException("商品不存在");
        }
        
        // 檢查商品狀態（只有草稿和已下架可以新增獎項）
        String status = lottery.getStatus();
        if (!LotteryStatusEnum.DRAFT.getCode().equals(status) 
                && !LotteryStatusEnum.OFF_SHELF.getCode().equals(status)) {
            throw new BusinessException("只有草稿或已下架狀態的商品可以新增獎項");
        }
        
        LotteryPrize prize = new LotteryPrize();
        prize.setId(UUID.randomUUID().toString());
        prize.setLotteryId(req.getLotteryId());
        prize.setName(req.getName());
        prize.setDescription(req.getDescription());
        prize.setImageUrl(req.getImageUrl());
        prize.setLevel(req.getLevel());
        prize.setPrizeNumber(req.getPrizeNumber());
        prize.setQuantity(req.getQuantity());
        prize.setRemaining(req.getQuantity()); // 初始時剩餘數量 = 總數量
        prize.setWeight(req.getWeight() != null ? req.getWeight() : 1);
        prize.setPrizeType(req.getPrizeType() != null ? req.getPrizeType() : "physical");
        prize.setPointValue(req.getPointValue());
        prize.setIsLastPrize(req.getIsLastPrize() != null && req.getIsLastPrize() ? (byte) 1 : (byte) 0);
        prize.setIsGrandPrize(req.getIsGrandPrize() != null && req.getIsGrandPrize() ? (byte) 1 : (byte) 0);
        prize.setOrderNum(req.getOrderNum() != null ? req.getOrderNum() : 0);
        prize.setCreatedAt(LocalDateTime.now());
        prize.setUpdatedAt(LocalDateTime.now());
        
        // 最後賞的權重設為 0（不可被隨機抽中）
        if (prize.getIsLastPrize() == 1) {
            prize.setWeight(0);
        }
        
        lotteryPrizeMapper.insert(prize);
        log.info("獎項建立成功: id={}", prize.getId());
        
        return getPrizeById(prize.getId());
    }

    @Override
    @Transactional
    public List<LotteryPrizeRes> createPrizes(String lotteryId, List<LotteryPrizeCreateReq> reqList) {
        log.info("批量建立獎項: lotteryId={}, count={}", lotteryId, reqList.size());
        
        List<LotteryPrizeRes> results = new ArrayList<>();
        for (LotteryPrizeCreateReq req : reqList) {
            req.setLotteryId(lotteryId);
            results.add(createPrize(req));
        }
        
        return results;
    }

    @Override
    @Transactional
    public LotteryPrizeRes updatePrize(LotteryPrizeUpdateReq req) {
        log.info("更新獎項: id={}", req.getId());
        
        LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(req.getId());
        if (prize == null) {
            throw new BusinessException("獎項不存在");
        }
        
        // 檢查商品狀態
        Lottery lottery = lotteryMapper.selectByPrimaryKey(prize.getLotteryId());
        if (lottery != null) {
            String status = lottery.getStatus();
            if (!LotteryStatusEnum.DRAFT.getCode().equals(status) 
                    && !LotteryStatusEnum.OFF_SHELF.getCode().equals(status)) {
                throw new BusinessException("只有草稿或已下架狀態的商品可以修改獎項");
            }
        }
        
        if (req.getName() != null) prize.setName(req.getName());
        if (req.getDescription() != null) prize.setDescription(req.getDescription());
        if (req.getImageUrl() != null) prize.setImageUrl(req.getImageUrl());
        if (req.getLevel() != null) prize.setLevel(req.getLevel());
        if (req.getPrizeNumber() != null) prize.setPrizeNumber(req.getPrizeNumber());
        
        // 處理數量變更（增加數量時同步增加剩餘數量）
        if (req.getQuantity() != null && !req.getQuantity().equals(prize.getQuantity())) {
            int diff = req.getQuantity() - prize.getQuantity();
            prize.setQuantity(req.getQuantity());
            prize.setRemaining(Math.max(0, prize.getRemaining() + diff));
        }
        
        if (req.getWeight() != null) prize.setWeight(req.getWeight());
        if (req.getPrizeType() != null) prize.setPrizeType(req.getPrizeType());
        if (req.getPointValue() != null) prize.setPointValue(req.getPointValue());
        if (req.getIsLastPrize() != null) {
            prize.setIsLastPrize(req.getIsLastPrize() ? (byte) 1 : (byte) 0);
            if (req.getIsLastPrize()) {
                prize.setWeight(0);
            }
        }
        if (req.getIsGrandPrize() != null) prize.setIsGrandPrize(req.getIsGrandPrize() ? (byte) 1 : (byte) 0);
        if (req.getOrderNum() != null) prize.setOrderNum(req.getOrderNum());
        
        prize.setUpdatedAt(LocalDateTime.now());
        lotteryPrizeMapper.updateByPrimaryKey(prize);
        log.info("獎項更新成功: id={}", req.getId());
        
        return getPrizeById(req.getId());
    }

    @Override
    @Transactional
    public void deletePrize(String id) {
        log.info("刪除獎項: id={}", id);
        
        LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(id);
        if (prize == null) {
            throw new BusinessException("獎項不存在");
        }
        
        // 檢查商品狀態
        Lottery lottery = lotteryMapper.selectByPrimaryKey(prize.getLotteryId());
        if (lottery != null) {
            String status = lottery.getStatus();
            if (!LotteryStatusEnum.DRAFT.getCode().equals(status) 
                    && !LotteryStatusEnum.OFF_SHELF.getCode().equals(status)) {
                throw new BusinessException("只有草稿或已下架狀態的商品可以刪除獎項");
            }
        }
        
        lotteryPrizeMapper.deleteByPrimaryKey(id);
        log.info("獎項刪除成功: id={}", id);
    }

    @Override
    public LotteryPrizeRes getPrizeById(String id) {
        LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(id);
        if (prize == null) {
            throw new BusinessException("獎項不存在");
        }
        return convertToRes(prize);
    }

    @Override
    public List<LotteryPrizeRes> getPrizesByLotteryId(String lotteryId) {
        LotteryPrizeExample example = new LotteryPrizeExample();
        example.createCriteria().andLotteryIdEqualTo(lotteryId);
        example.setOrderByClause("order_num ASC");
        List<LotteryPrize> prizes = lotteryPrizeMapper.selectByExample(example);
        return prizes.stream().map(this::convertToRes).toList();
    }

    @Override
    public List<LotteryPrizeRes> getPrizesByLevel(String lotteryId, String level) {
        LotteryPrizeExample example = new LotteryPrizeExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andLevelEqualTo(level);
        example.setOrderByClause("order_num ASC");
        List<LotteryPrize> prizes = lotteryPrizeMapper.selectByExample(example);
        return prizes.stream().map(this::convertToRes).toList();
    }

    @Override
    @Transactional
    public void resetPrizeRemaining(String lotteryId) {
        log.info("重置獎項剩餘數量: lotteryId={}", lotteryId);
        
        LotteryPrizeExample example = new LotteryPrizeExample();
        example.createCriteria().andLotteryIdEqualTo(lotteryId);
        List<LotteryPrize> prizes = lotteryPrizeMapper.selectByExample(example);
        
        for (LotteryPrize prize : prizes) {
            prize.setRemaining(prize.getQuantity());
            prize.setUpdatedAt(LocalDateTime.now());
            lotteryPrizeMapper.updateByPrimaryKey(prize);
        }
        
        log.info("獎項剩餘數量重置完成: lotteryId={}", lotteryId);
    }

    @Override
    public List<String> getAvailableNumbers(String lotteryId) {
        // 查詢還有剩餘數量的獎項
        LotteryPrizeExample example = new LotteryPrizeExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andRemainingGreaterThan(0);
        example.setOrderByClause("order_num ASC");
        List<LotteryPrize> prizes = lotteryPrizeMapper.selectByExample(example);
        
        return prizes.stream()
                .filter(p -> p.getPrizeNumber() != null && !p.getPrizeNumber().isEmpty())
                .map(LotteryPrize::getPrizeNumber)
                .toList();
    }

    // ==================== 私有方法 ====================

    private LotteryPrizeRes convertToRes(LotteryPrize prize) {
        LotteryPrizeRes res = new LotteryPrizeRes();
        res.setId(prize.getId());
        res.setLotteryId(prize.getLotteryId());
        res.setName(prize.getName());
        res.setDescription(prize.getDescription());
        res.setImageUrl(prize.getImageUrl());
        res.setLevel(prize.getLevel());
        res.setLevelName(PrizeLevelEnum.getNameByCode(prize.getLevel()));
        res.setPrizeNumber(prize.getPrizeNumber());
        res.setQuantity(prize.getQuantity());
        res.setRemaining(prize.getRemaining());
        res.setDrawnCount(prize.getQuantity() - prize.getRemaining());
        res.setWeight(prize.getWeight());
        res.setPrizeType(prize.getPrizeType());
        res.setPointValue(prize.getPointValue());
        res.setIsLastPrize(prize.getIsLastPrize() != null && prize.getIsLastPrize() == 1);
        res.setIsGrandPrize(prize.getIsGrandPrize() != null && prize.getIsGrandPrize() == 1);
        res.setOrderNum(prize.getOrderNum());
        res.setCreatedAt(prize.getCreatedAt());
        res.setUpdatedAt(prize.getUpdatedAt());
        return res;
    }
}
