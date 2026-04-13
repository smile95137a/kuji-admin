package com.group.admin.service.impl;

import com.group.admin.entity.*;
import com.group.admin.enums.PrizeBoxStatusEnum;
import com.group.admin.enums.TransactionTypeEnum;
import com.group.admin.example.PrizeBoxExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.*;
import com.group.admin.req.prizebox.PrizeBoxRecycleReq;
import com.group.admin.req.prizebox.PrizeBoxShipReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.prizebox.PrizeBoxItemRes;
import com.group.admin.res.prizebox.PrizeBoxSummaryRes;
import com.group.admin.res.prizebox.RecycleResultRes;
import com.group.admin.service.OrderService;
import com.group.admin.service.PrizeBoxService;
import com.group.admin.service.CoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 獎品盒服務實作
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
    private final UserMapper userMapper;
    private final UserAddressMapper userAddressMapper;
    private final CoinService walletService;
    private final OrderService orderService;
    
    @Override
    @Transactional
    public void addToPrizeBox(String userId, String lotteryId, String prizeId, String storeId, Long recycleBonus) {
        log.info("🔍 新增獎品到獎品盒：userId={}, prizeId={}", userId, prizeId);
        
        PrizeBox prizeBox = new PrizeBox();
        prizeBox.setId(UUID.randomUUID().toString());
        prizeBox.setUserId(userId);
        prizeBox.setLotteryId(lotteryId);
        prizeBox.setPrizeId(prizeId);
        prizeBox.setStoreId(storeId);
        prizeBox.setStatus(PrizeBoxStatusEnum.IN_BOX.getCode());
        prizeBox.setRecycleBonus(recycleBonus);
        prizeBox.setIsShippable((byte) 1);
        prizeBox.setRecycledAt(null);
        prizeBox.setShippedAt(null);
        prizeBox.setOrderId(null);
        prizeBox.setCreatedAt(LocalDateTime.now());
        
        prizeBoxMapper.insert(prizeBox);
        
        log.info("✅ 獎品已加入獎品盒：prizeBoxId={}", prizeBox.getId());
    }
    
    @Override
    public List<PrizeBoxItemRes> getPrizeBox(String userId) {
        return getPrizeBox(userId, null);
    }
    
    @Override
    public List<PrizeBoxItemRes> getPrizeBox(String userId, String status) {
        PrizeBoxExample example = new PrizeBoxExample();
        PrizeBoxExample.Criteria criteria = example.createCriteria()
                .andUserIdEqualTo(userId);
        
        if (status != null && !status.isBlank()) {
            criteria.andStatusEqualTo(status);
        } else {
            criteria.andStatusEqualTo(PrizeBoxStatusEnum.IN_BOX.getCode());
        }
        example.setOrderByClause("created_at DESC");
        
        List<PrizeBox> prizeBoxes = prizeBoxMapper.selectByExample(example);
        return prizeBoxes.stream().map(this::convertToItemRes).collect(Collectors.toList());
    }
    
    @Override
    public PageResult<PrizeBoxItemRes> getPrizeBoxHistory(String userId, String status, int page, int size) {
        log.info("🔍 查詢獎品盒歷史：userId={}, status={}, page={}, size={}", userId, status, page, size);
        
        PrizeBoxExample countExample = new PrizeBoxExample();
        PrizeBoxExample.Criteria countCriteria = countExample.createCriteria()
                .andUserIdEqualTo(userId);
        
        List<String> historyStatuses;
        if (status != null && !status.isBlank()) {
            historyStatuses = List.of(status);
        } else {
            historyStatuses = List.of(
                    PrizeBoxStatusEnum.SHIPPED.getCode(),
                    PrizeBoxStatusEnum.RECYCLED.getCode()
            );
        }
        countCriteria.andStatusIn(historyStatuses);
        
        long total = prizeBoxMapper.countByExample(countExample);
        
        if (total == 0) {
            return PageResult.empty(page, size);
        }
        
        PrizeBoxExample queryExample = new PrizeBoxExample();
        PrizeBoxExample.Criteria queryCriteria = queryExample.createCriteria()
                .andUserIdEqualTo(userId);
        queryCriteria.andStatusIn(historyStatuses);
        queryExample.setOrderByClause("created_at DESC LIMIT " + size + " OFFSET " + ((page - 1) * size));
        
        List<PrizeBox> prizeBoxes = prizeBoxMapper.selectByExample(queryExample);
        List<PrizeBoxItemRes> items = prizeBoxes.stream()
                .map(this::convertToItemRes)
                .collect(Collectors.toList());
        
        return PageResult.of(page, size, total, items);
    }
    
    @Override
    public List<PrizeBoxSummaryRes> getSummaryByStore(String userId) {
        List<PrizeBoxItemRes> items = getPrizeBox(userId);
        
        Map<String, List<PrizeBoxItemRes>> groupedByStore = items.stream()
                .collect(Collectors.groupingBy(PrizeBoxItemRes::getStoreId));
        
        return groupedByStore.entrySet().stream()
                .map(entry -> {
                    String storeId = entry.getKey();
                    List<PrizeBoxItemRes> storeItems = entry.getValue();
                    
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
        
        // 如果提供了 userAddressId，從常用地址自動填入
        if (req.getUserAddressId() != null && !req.getUserAddressId().isBlank()) {
            UserAddress savedAddress = userAddressMapper.selectByPrimaryKey(req.getUserAddressId());
            if (savedAddress != null && userId.equals(savedAddress.getUserId())) {
                if (req.getRecipientName() == null || req.getRecipientName().isBlank()) {
                    req.setRecipientName(savedAddress.getRecipientName());
                }
                if (req.getRecipientPhone() == null || req.getRecipientPhone().isBlank()) {
                    req.setRecipientPhone(savedAddress.getRecipientPhone());
                }
                if (req.getRecipientAddress() == null || req.getRecipientAddress().isBlank()) {
                    String fullAddress = buildFullAddress(savedAddress);
                    req.setRecipientAddress(fullAddress);
                }
                log.info("📍 已從常用地址帶入收件資訊：addressId={}", req.getUserAddressId());
            }
        }
        
        // 自動從個人資料帶入收件人姓名與電話
        if (req.getRecipientName() == null || req.getRecipientName().isBlank()
                || req.getRecipientPhone() == null || req.getRecipientPhone().isBlank()) {
            User user = userMapper.selectByPrimaryKey(userId);
            if (user != null) {
                if (req.getRecipientName() == null || req.getRecipientName().isBlank()) {
                    req.setRecipientName(user.getRecipientName());
                }
                if (req.getRecipientPhone() == null || req.getRecipientPhone().isBlank()) {
                    req.setRecipientPhone(user.getRecipientPhone());
                }
            }
        }
        if (req.getRecipientName() == null || req.getRecipientName().isBlank()) {
            throw new BusinessException("收件人姓名不可為空，請先在個人資料填寫或於出貨時提供");
        }
        if (req.getRecipientPhone() == null || req.getRecipientPhone().isBlank()) {
            throw new BusinessException("收件人電話不可為空，請先在個人資料填寫或於出貨時提供");
        }
        
        // 依配送方式做條件式驗證
        String method = req.getShippingMethod();
        if ("HOME_DELIVERY".equals(method)) {
            if (req.getRecipientAddress() == null || req.getRecipientAddress().isBlank()) {
                throw new BusinessException("宅配需填入收件地址");
            }
        } else if ("SEVEN_ELEVEN".equals(method) || "FAMILY_MART".equals(method)) {
            if (req.getStoreCode() == null || req.getStoreCode().isBlank()) {
                throw new BusinessException("超商取貨需填入分店代碼");
            }
            if (req.getStoreName() == null || req.getStoreName().isBlank()) {
                throw new BusinessException("超商取貨需填入分店名稱");
            }
        }
        
        // 驗證所有獎品
        List<PrizeBox> prizeBoxes = req.getPrizeBoxIds().stream()
                .map(id -> {
                    PrizeBox prizeBox = prizeBoxMapper.selectByPrimaryKey(id);
                    if (prizeBox == null) {
                        throw new BusinessException("獎品不存在：" + id);
                    }
                    if (!prizeBox.getUserId().equals(userId)) {
                        throw new BusinessException("獎品不屬於您");
                    }
                    if (!PrizeBoxStatusEnum.IN_BOX.getCode().equals(prizeBox.getStatus())) {
                        throw new BusinessException("獎品已出貨，無法再次出貨");
                    }
                    // 檢查是否可出貨
                    if (prizeBox.getIsShippable() != null && prizeBox.getIsShippable() == 0) {
                        throw new BusinessException("此獎品不支援出貨（僅可回收）：" + id);
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
        
        // 更新獎品盒狀態
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
    public RecycleResultRes recyclePrizes(String userId, PrizeBoxRecycleReq req) {
        log.info("🔍 回收獎品：userId={}, prizeBoxIds={}", userId, req.getPrizeBoxIds());
        
        long totalBonus = 0L;
        int recycledCount = 0;
        
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
            // 驗證 recycleBonus > 0
            if (prizeBox.getRecycleBonus() == null || prizeBox.getRecycleBonus() <= 0) {
                throw new BusinessException("此獎品不可回收（回收紅利為零）：" + prizeBoxId);
            }
            
            // 更新狀態
            prizeBox.setStatus(PrizeBoxStatusEnum.RECYCLED.getCode());
            prizeBox.setRecycledAt(LocalDateTime.now());
            prizeBoxMapper.updateByPrimaryKey(prizeBox);
            
            totalBonus += prizeBox.getRecycleBonus();
            recycledCount++;
        }
        
        // 增加紅利
        if (totalBonus > 0) {
            walletService.addBonus(userId, totalBonus, 
                    TransactionTypeEnum.RECYCLE.getCode(), 
                    null, 
                    "回收 " + recycledCount + " 個獎品");
        }
        
        log.info("✅ 回收完成：totalBonus={}, recycledCount={}", totalBonus, recycledCount);
        return RecycleResultRes.builder()
                .totalBonus(totalBonus)
                .recycledCount(recycledCount)
                .build();
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
        Lottery lottery = lotteryMapper.selectByPrimaryKey(prizeBox.getLotteryId());
        LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeBox.getPrizeId());
        Store store = storeMapper.selectByPrimaryKey(prizeBox.getStoreId());
        
        boolean isInBox = PrizeBoxStatusEnum.IN_BOX.getCode().equals(prizeBox.getStatus());
        boolean recyclable = isInBox 
                && prizeBox.getRecycleBonus() != null 
                && prizeBox.getRecycleBonus() > 0;
        boolean shippable = isInBox 
                && (prizeBox.getIsShippable() == null || prizeBox.getIsShippable() != 0);
        
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
                .isRecyclable(recyclable)
                .isShippable(shippable)
                .recycleBonus(prizeBox.getRecycleBonus())
                .prizeValue(prize != null ? prize.getPointValue() : null)
                .orderId(prizeBox.getOrderId())
                .createdAt(prizeBox.getCreatedAt())
                .shippedAt(prizeBox.getShippedAt())
                .recycledAt(prizeBox.getRecycledAt())
                .build();
    }
    
    private String buildFullAddress(UserAddress address) {
        StringBuilder sb = new StringBuilder();
        if (address.getPostalCode() != null) sb.append(address.getPostalCode()).append(" ");
        if (address.getCity() != null) sb.append(address.getCity());
        if (address.getDistrict() != null) sb.append(address.getDistrict());
        if (address.getAddress() != null) sb.append(address.getAddress());
        return sb.toString().trim();
    }
}