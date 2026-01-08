package com.group.admin.service.impl;

import com.group.admin.entity.*;
import com.group.admin.enums.PrizeBoxStatusEnum;
import com.group.admin.enums.TransactionTypeEnum;
import com.group.admin.example.PrizeBoxExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.*;
import com.group.admin.req.prizebox.PrizeBoxRecycleReq;
import com.group.admin.req.prizebox.PrizeBoxShipReq;
import com.group.admin.res.prizebox.PrizeBoxItemRes;
import com.group.admin.res.prizebox.PrizeBoxSummaryRes;
import com.group.admin.service.OrderService;
import com.group.admin.service.PrizeBoxService;
import com.group.admin.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 賞品盒服務實作
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrizeBoxServiceImpl implements PrizeBoxService {
    
    private final PrizeBoxMapper prizeBoxMapper;
    private final LotteryMapper lotteryMapper;
    private final LotteryPrizeMapper lotteryPrizeMapper;
    private final StoreMapper storeMapper;
    private final WalletService walletService;
    private final OrderService orderService;
    
    @Override
    @Transactional
    public void addToPrizeBox(String userId, String lotteryId, String prizeId, String storeId, Long recycleBonus) {
        log.info("🔍 新增獎品到賞品盒：userId={}, prizeId={}", userId, prizeId);
        
        PrizeBox prizeBox = new PrizeBox();
        prizeBox.setId(UUID.randomUUID().toString());
        prizeBox.setUserId(userId);
        prizeBox.setLotteryId(lotteryId);
        prizeBox.setPrizeId(prizeId);
        prizeBox.setStoreId(storeId);
        prizeBox.setStatus(PrizeBoxStatusEnum.IN_BOX.getCode());
        prizeBox.setRecycleBonus(recycleBonus);
        prizeBox.setRecycledAt(null);
        prizeBox.setShippedAt(null);
        prizeBox.setOrderId(null);
        prizeBox.setCreatedAt(LocalDateTime.now());
        
        prizeBoxMapper.insert(prizeBox);
        
        log.info("✅ 獎品已加入賞品盒：prizeBoxId={}", prizeBox.getId());
    }
    
    @Override
    public List<PrizeBoxItemRes> getPrizeBox(String userId) {
        PrizeBoxExample example = new PrizeBoxExample();
        example.createCriteria()
                .andUserIdEqualTo(userId)
                .andStatusEqualTo(PrizeBoxStatusEnum.IN_BOX.getCode());
        example.setOrderByClause("created_at DESC");
        
        List<PrizeBox> prizeBoxes = prizeBoxMapper.selectByExample(example);
        return prizeBoxes.stream().map(this::convertToItemRes).collect(Collectors.toList());
    }
    
    @Override
    public List<PrizeBoxSummaryRes> getSummaryByStore(String userId) {
        List<PrizeBoxItemRes> items = getPrizeBox(userId);
        
        // 按店家分組
        Map<String, List<PrizeBoxItemRes>> groupedByStore = items.stream()
                .collect(Collectors.groupingBy(PrizeBoxItemRes::getStoreId));
        
        return groupedByStore.entrySet().stream()
                .map(entry -> {
                    String storeId = entry.getKey();
                    List<PrizeBoxItemRes> storeItems = entry.getValue();
                    
                    // 取得店家資訊
                    Store store = storeMapper.selectByPrimaryKey(storeId);
                    
                    return PrizeBoxSummaryRes.builder()
                            .storeId(storeId)
                            .storeName(store != null ? store.getStoreName() : null)
                            .itemCount(storeItems.size())
                            .items(storeItems)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public List<String> shipPrizes(String userId, PrizeBoxShipReq req) {
        log.info("🔍 出貨獎品：userId={}, prizeBoxIds={}", userId, req.getPrizeBoxIds());
        
        // 驗證所有獎品
        List<PrizeBox> prizeBoxes = req.getPrizeBoxIds().stream()
                .map(id -> {
                    PrizeBox prizeBox = prizeBoxMapper.selectByPrimaryKey(id);
                    if (prizeBox == null) {
                        throw new BusinessException("獎品不存在：" + id);
                    }
                    if (!prizeBox.getUserId().equals(userId)) {
                        throw new BusinessException("無權操作此獎品");
                    }
                    if (!PrizeBoxStatusEnum.IN_BOX.getCode().equals(prizeBox.getStatus())) {
                        throw new BusinessException("獎品已處理：" + id);
                    }
                    return prizeBox;
                })
                .collect(Collectors.toList());
        
        // 按店家分組
        Map<String, List<String>> groupedByStore = prizeBoxes.stream()
                .collect(Collectors.groupingBy(
                        PrizeBox::getStoreId,
                        Collectors.mapping(PrizeBox::getId, Collectors.toList())
                ));
        
        // 為每個店家建立訂單
        List<String> orderIds = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : groupedByStore.entrySet()) {
            List<String> storeOrderIds = orderService.createOrdersFromPrizeBox(
                    userId,
                    entry.getValue(),
                    req.getShippingMethod(),
                    req.getRecipientName(),
                    req.getRecipientPhone(),
                    req.getRecipientAddress(),
                    req.getStoreCode(),
                    req.getStoreName(),
                    req.getStoreAddress()
            );
            orderIds.addAll(storeOrderIds);
        }
        
        // 更新賞品盒狀態
        for (PrizeBox prizeBox : prizeBoxes) {
            prizeBox.setStatus(PrizeBoxStatusEnum.SHIPPED.getCode());
            prizeBox.setShippedAt(LocalDateTime.now());
            prizeBoxMapper.updateByPrimaryKey(prizeBox);
        }
        
        log.info("✅ 出貨完成：orderCount={}", orderIds.size());
        return orderIds;
    }
    
    @Override
    @Transactional
    public void recyclePrizes(String userId, PrizeBoxRecycleReq req) {
        log.info("🔍 回收獎品：userId={}, prizeBoxIds={}", userId, req.getPrizeBoxIds());
        
        Long totalBonus = 0L;
        
        for (String prizeBoxId : req.getPrizeBoxIds()) {
            PrizeBox prizeBox = prizeBoxMapper.selectByPrimaryKey(prizeBoxId);
            
            if (prizeBox == null) {
                throw new BusinessException("獎品不存在：" + prizeBoxId);
            }
            if (!prizeBox.getUserId().equals(userId)) {
                throw new BusinessException("無權操作此獎品");
            }
            if (!PrizeBoxStatusEnum.IN_BOX.getCode().equals(prizeBox.getStatus())) {
                throw new BusinessException("獎品已處理：" + prizeBoxId);
            }
            
            // 更新狀態
            prizeBox.setStatus(PrizeBoxStatusEnum.RECYCLED.getCode());
            prizeBox.setRecycledAt(LocalDateTime.now());
            prizeBoxMapper.updateByPrimaryKey(prizeBox);
            
            totalBonus += prizeBox.getRecycleBonus();
        }
        
        // 增加紅利
        if (totalBonus > 0) {
            walletService.addBonus(userId, totalBonus, 
                    TransactionTypeEnum.RECYCLE.getCode(), 
                    null, 
                    "回收 " + req.getPrizeBoxIds().size() + " 個獎品");
        }
        
        log.info("✅ 回收完成：totalBonus={}", totalBonus);
    }
    
    @Override
    public PrizeBoxItemRes getPrizeBoxItem(String prizeBoxId) {
        PrizeBox prizeBox = prizeBoxMapper.selectByPrimaryKey(prizeBoxId);
        if (prizeBox == null) {
            throw new BusinessException("獎品不存在");
        }
        return convertToItemRes(prizeBox);
    }
    
    /**
     * 轉換為回應 DTO
     */
    private PrizeBoxItemRes convertToItemRes(PrizeBox prizeBox) {
        // 查詢相關資料
        Lottery lottery = lotteryMapper.selectByPrimaryKey(prizeBox.getLotteryId());
        LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeBox.getPrizeId());
        Store store = storeMapper.selectByPrimaryKey(prizeBox.getStoreId());
        
        return PrizeBoxItemRes.builder()
                .id(prizeBox.getId())
                .userId(prizeBox.getUserId())
                .lotteryId(prizeBox.getLotteryId())
                .lotteryTitle(lottery != null ? lottery.getTitle() : null)
                .lotteryImageUrl(lottery != null ? lottery.getImageUrl() : null)
                .prizeId(prizeBox.getPrizeId())
                .prizeName(prize != null ? prize.getName() : null)
                .prizeImageUrl(prize != null ? prize.getImageUrl() : null)
                .prizeLevel(prize != null ? prize.getLevel() : null)
                .storeId(prizeBox.getStoreId())
                .storeName(store != null ? store.getStoreName() : null)
                .status(prizeBox.getStatus())
                .statusName(PrizeBoxStatusEnum.getNameByCode(prizeBox.getStatus()))
                .isRecyclable(true) // 在賞品盒中的都可以回收
                .recycleBonus(prizeBox.getRecycleBonus())
                .createdAt(prizeBox.getCreatedAt())
                .build();
    }
}
