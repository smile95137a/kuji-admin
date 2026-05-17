package com.group.admin.service.impl;

import com.group.admin.entity.*;
import com.group.admin.example.LotteryExample;
import com.group.admin.example.LotteryPrizeExample;
import com.group.admin.enums.PrizeBoxStatusEnum;
import com.group.admin.enums.TransactionTypeEnum;
import com.group.admin.example.PrizeBoxExample;
import com.group.admin.example.StoreExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.*;
import com.group.admin.req.prizebox.PrizeBoxRecycleReq;
import com.group.admin.req.prizebox.PrizeBoxShipReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.order.OrderPaymentInitRes;
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
        PrizeBoxExample example = new PrizeBoxExample();
        example.createCriteria()
                .andUserIdEqualTo(userId)
                .andStatusEqualTo(PrizeBoxStatusEnum.IN_BOX.getCode());
        example.setOrderByClause("created_at DESC");

        List<PrizeBox> prizeBoxes = prizeBoxMapper.selectByExample(example);
        return convertToItemResList(prizeBoxes);
    }

    @Override
    public PageResult<PrizeBoxItemRes> getPrizeBoxPage(String userId, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, Math.min(size, 100));

        PrizeBoxExample example = new PrizeBoxExample();
        example.createCriteria()
                .andUserIdEqualTo(userId)
                .andStatusEqualTo(PrizeBoxStatusEnum.IN_BOX.getCode());
        example.setOrderByClause("created_at DESC");

        long total = prizeBoxMapper.countByExample(example);
        int offset = (safePage - 1) * safeSize;
        List<PrizeBox> prizeBoxes = prizeBoxMapper.selectByExampleWithPage(example, offset, safeSize);
        List<PrizeBoxItemRes> items = convertToItemResList(prizeBoxes);

        return PageResult.of(safePage, safeSize, total, items);
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
    public List<OrderPaymentInitRes> shipPrizes(String userId, PrizeBoxShipReq req) {
        log.info("🔍 出貨獎品：userId={}, prizeBoxIds={}", userId, req.getPrizeBoxIds());

        // 優先使用已儲存地址
        if (req.getUserAddressId() != null && !req.getUserAddressId().isBlank()) {
            UserAddress savedAddress = userAddressMapper.selectByPrimaryKey(req.getUserAddressId());
            if (savedAddress != null && savedAddress.getUserId().equals(userId)) {
                req.setRecipientName(savedAddress.getRecipientName());
                req.setRecipientPhone(savedAddress.getRecipientPhone());
                String fullAddress = (savedAddress.getCity() != null ? savedAddress.getCity() : "")
                        + (savedAddress.getDistrict() != null ? savedAddress.getDistrict() : "")
                        + (savedAddress.getAddress() != null ? savedAddress.getAddress() : "");
                req.setRecipientAddress(fullAddress);
            }
        }

        // 驗證收件人資訊
        if (req.getRecipientName() == null || req.getRecipientName().isBlank()) {
            throw new BusinessException("收件人姓名不可為空，請先在個人資料填寫或於出貨時提供");
        }
        if (req.getRecipientPhone() == null || req.getRecipientPhone().isBlank()) {
            throw new BusinessException("收件人電話不可為空，請先在個人資料填寫或於出貨時提供");
        }

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
                    if (prizeBox.getIsShippable() != null && prizeBox.getIsShippable() == 0) {
                        throw new BusinessException("此獎品不可出貨：" + id);
                    }
                    return prizeBox;
                })
                .collect(Collectors.toList());

        // 按店家分組，並驗證店家狀態
        Map<String, List<String>> groupedByStore = prizeBoxes.stream()
                .collect(Collectors.groupingBy(
                        PrizeBox::getStoreId,
                        Collectors.mapping(PrizeBox::getId, Collectors.toList())
                ));

        for (String storeId : groupedByStore.keySet()) {
            Store store = storeMapper.selectByPrimaryKey(storeId);
            if (store != null && "INACTIVE".equals(store.getStatus())) {
                throw new BusinessException("店家已停用，無法建立訂單：" + storeId);
            }
        }

        com.group.admin.req.order.CreateOrderReq createOrderReq = new com.group.admin.req.order.CreateOrderReq();
        createOrderReq.setPrizeBoxIds(req.getPrizeBoxIds());
        createOrderReq.setShippingMethod(req.getShippingMethod());
        createOrderReq.setShippingMethodId(req.getShippingMethodId());
        createOrderReq.setShippingFee(req.getShippingFee());
        createOrderReq.setPaymentMethod(req.getPaymentMethod());
        createOrderReq.setRecipientName(req.getRecipientName());
        createOrderReq.setRecipientPhone(req.getRecipientPhone());
        createOrderReq.setRecipientAddress(req.getRecipientAddress());
        createOrderReq.setStoreCode(req.getStoreCode());
        createOrderReq.setStoreName(req.getStoreName());
        createOrderReq.setStoreAddress(req.getStoreAddress());

        List<OrderPaymentInitRes> orderResults = orderService.createOrdersFromPrizeBoxWithPayment(userId, createOrderReq);

        log.info("✅ 出貨完成（含支付初始化）：orderCount={}", orderResults.size());
        return orderResults;
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
            if (prizeBox.getRecycleBonus() == null || prizeBox.getRecycleBonus() <= 0) {
                throw new BusinessException("此獎品不可回收：" + prizeBoxId);
            }

            prizeBox.setStatus(PrizeBoxStatusEnum.RECYCLED.getCode());
            prizeBox.setRecycledAt(LocalDateTime.now());
            prizeBoxMapper.updateByPrimaryKey(prizeBox);

            totalBonus += prizeBox.getRecycleBonus();
            recycledCount++;
        }

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

    @Override
    public PageResult<PrizeBoxItemRes> getPrizeBoxHistory(String userId, String status, int page, int size) {
        PrizeBoxExample example = new PrizeBoxExample();
        PrizeBoxExample.Criteria criteria = example.createCriteria()
                .andUserIdEqualTo(userId);
        if (status != null && !status.isBlank()) {
            criteria.andStatusEqualTo(status);
        }
        example.setOrderByClause("created_at DESC");

        long total = prizeBoxMapper.countByExample(example);
        int offset = (page - 1) * size;
        List<PrizeBox> prizeBoxes = prizeBoxMapper.selectByExampleWithPage(example, offset, size);
        List<PrizeBoxItemRes> items = convertToItemResList(prizeBoxes);

        return PageResult.of(page, size, total, items);
    }

    private List<PrizeBoxItemRes> convertToItemResList(List<PrizeBox> prizeBoxes) {
        if (prizeBoxes == null || prizeBoxes.isEmpty()) {
            return List.of();
        }

        List<String> lotteryIds = prizeBoxes.stream()
                .map(PrizeBox::getLotteryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<String> prizeIds = prizeBoxes.stream()
                .map(PrizeBox::getPrizeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<String> storeIds = prizeBoxes.stream()
                .map(PrizeBox::getStoreId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, Lottery> lotteryMap = new HashMap<>();
        if (!lotteryIds.isEmpty()) {
            LotteryExample lotteryExample = new LotteryExample();
            lotteryExample.createCriteria().andIdIn(lotteryIds);
            lotteryMap = lotteryMapper.selectByExample(lotteryExample).stream()
                    .collect(Collectors.toMap(Lottery::getId, item -> item));
        }

        Map<String, LotteryPrize> prizeMap = new HashMap<>();
        if (!prizeIds.isEmpty()) {
            LotteryPrizeExample prizeExample = new LotteryPrizeExample();
            prizeExample.createCriteria().andIdIn(prizeIds);
            prizeMap = lotteryPrizeMapper.selectByExample(prizeExample).stream()
                    .collect(Collectors.toMap(LotteryPrize::getId, item -> item));
        }

        Map<String, Store> storeMap = new HashMap<>();
        if (!storeIds.isEmpty()) {
            StoreExample storeExample = new StoreExample();
            storeExample.createCriteria().andIdIn(storeIds);
            storeMap = storeMapper.selectByExample(storeExample).stream()
                    .collect(Collectors.toMap(Store::getId, item -> item));
        }

        Map<String, Lottery> finalLotteryMap = lotteryMap;
        Map<String, LotteryPrize> finalPrizeMap = prizeMap;
        Map<String, Store> finalStoreMap = storeMap;
        return prizeBoxes.stream()
                .map(prizeBox -> convertToItemRes(
                        prizeBox,
                        finalLotteryMap.get(prizeBox.getLotteryId()),
                        finalPrizeMap.get(prizeBox.getPrizeId()),
                        finalStoreMap.get(prizeBox.getStoreId())))
                .collect(Collectors.toList());
    }

    /**
     * 轉換為回應 DTO
     */
    private PrizeBoxItemRes convertToItemRes(PrizeBox prizeBox) {
        Lottery lottery = lotteryMapper.selectByPrimaryKey(prizeBox.getLotteryId());
        LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeBox.getPrizeId());
        Store store = storeMapper.selectByPrimaryKey(prizeBox.getStoreId());
        return convertToItemRes(prizeBox, lottery, prize, store);
    }

    private PrizeBoxItemRes convertToItemRes(PrizeBox prizeBox, Lottery lottery, LotteryPrize prize, Store store) {
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
                .prizeValue(prize != null ? prize.getPointValue() : null)
                .storeId(prizeBox.getStoreId())
                .storeName(store != null ? store.getStoreName() : null)
                .status(prizeBox.getStatus())
                .statusName(PrizeBoxStatusEnum.getNameByCode(prizeBox.getStatus()))
                .isRecyclable(prizeBox.getRecycleBonus() != null && prizeBox.getRecycleBonus() > 0)
                .isShippable(prizeBox.getIsShippable() == null || prizeBox.getIsShippable() != 0)
                .recycleBonus(prizeBox.getRecycleBonus())
                .createdAt(prizeBox.getCreatedAt())
                .shippedAt(prizeBox.getShippedAt())
                .recycledAt(prizeBox.getRecycledAt())
                .build();
    }
}
