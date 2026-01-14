package com.group.admin.service.impl;

import com.group.admin.condition.OrderCondition;
import com.group.admin.entity.*;
import com.group.admin.enums.OrderStatusEnum;
import com.group.admin.enums.PaymentStatusEnum;
import com.group.admin.example.OrderExample;
import com.group.admin.example.OrderItemExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.*;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.order.OrderCancelReq;
import com.group.admin.req.order.OrderShipReq;
import com.group.admin.res.order.OrderDetailRes;
import com.group.admin.res.order.OrderItemRes;
import com.group.admin.res.order.OrderRes;
import com.group.admin.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 訂單服務實作
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final PrizeBoxMapper prizeBoxMapper;
    private final LotteryMapper lotteryMapper;
    private final StoreMapper storeMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public List<String> createOrdersFromPrizeBox(String userId, List<String> prizeBoxIds,
            String shippingMethod, String recipientName,
            String recipientPhone, String recipientAddress,
            String storeCode, String storeName, String storeAddress) {
        log.info("🔍 從賞品盒建立訂單：userId={}, prizeBoxCount={}", userId, prizeBoxIds.size());

        // 查詢所有賞品盒
        List<PrizeBox> prizeBoxes = prizeBoxIds.stream()
                .map(prizeBoxMapper::selectByPrimaryKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 按店家分組
        Map<String, List<PrizeBox>> groupedByStore = prizeBoxes.stream()
                .collect(Collectors.groupingBy(PrizeBox::getStoreId));

        List<String> orderIds = new ArrayList<>();

        // 為每個店家建立訂單
        for (Map.Entry<String, List<PrizeBox>> entry : groupedByStore.entrySet()) {
            String storeId = entry.getKey();
            List<PrizeBox> storePrizeBoxes = entry.getValue();

            // 建立訂單
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setOrderNumber(generateOrderNumber());
            order.setUserId(userId);
            order.setStoreId(storeId);
            order.setStatus(OrderStatusEnum.PENDING.getCode());
            order.setPaymentStatus(PaymentStatusEnum.SUCCESS.getCode()); // 已扣點數
            order.setTotalItems(storePrizeBoxes.size());
            order.setShippingMethod(shippingMethod);
            order.setRecipientName(recipientName);
            order.setRecipientPhone(recipientPhone);
            order.setRecipientAddress(recipientAddress);
            order.setStoreCode(storeCode);
            order.setStoreName(storeName);
            order.setStoreAddress(storeAddress);
            order.setTrackingNo(null);
            order.setRemark(null);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            orderMapper.insert(order);

            // 建立訂單項目
            for (PrizeBox prizeBox : storePrizeBoxes) {
                Lottery lottery = lotteryMapper.selectByPrimaryKey(prizeBox.getLotteryId());

                OrderItem item = new OrderItem();
                item.setId(UUID.randomUUID().toString());
                item.setOrderId(order.getId());
                item.setPrizeBoxId(prizeBox.getId());
                item.setLotteryId(prizeBox.getLotteryId());
                item.setLotteryTitle(lottery != null ? lottery.getTitle() : null);
                item.setPrizeId(prizeBox.getPrizeId());
                // Prize 欄位需要從 Lottery 的 Prize 關聯查詢
                item.setPrizeName(null); // 待補充
                item.setPrizeGrade(null); // 待補充
                item.setPrizeImage(null); // 待補充
                item.setCreatedAt(LocalDateTime.now());

                orderItemMapper.insert(item);

                // 更新賞品盒的訂單 ID
                prizeBox.setOrderId(order.getId());
                prizeBoxMapper.updateByPrimaryKey(prizeBox);
            }

            // 記錄狀態變更
            recordStatusLog(order.getId(), OrderStatusEnum.PENDING.getCode(),
                    OrderStatusEnum.PENDING.getName(), null, null);

            orderIds.add(order.getId());
        }

        log.info("✅ 訂單建立完成：orderCount={}", orderIds.size());
        return orderIds;
    }

    @Override
    public List<OrderRes> getOrders(QueryReq<OrderCondition> req) {
        OrderCondition condition = req != null ? req.getCondition() : null;

        OrderExample example = new OrderExample();
        OrderExample.Criteria criteria = example.createCriteria();

        if (condition != null) {
            if (condition.getUserId() != null && !condition.getUserId().isEmpty()) {
                criteria.andUserIdEqualTo(condition.getUserId());
            }
            if (condition.getStoreId() != null && !condition.getStoreId().isEmpty()) {
                criteria.andStoreIdEqualTo(condition.getStoreId());
            }
            if (condition.getShippingStatus() != null && !condition.getShippingStatus().isEmpty()) {
                criteria.andStatusEqualTo(condition.getShippingStatus());
            }
            if (condition.getOrderNo() != null && !condition.getOrderNo().isEmpty()) {
                criteria.andOrderNumberLike("%" + condition.getOrderNo() + "%");
            }
            // 日期範圍（LocalDate 轉 LocalDateTime）
            if (condition.getCreatedAtStart() != null) {
                criteria.andCreatedAtGreaterThanOrEqualTo(
                    condition.getCreatedAtStart().atStartOfDay()
                );
            }
            if (condition.getCreatedAtEnd() != null) {
                criteria.andCreatedAtLessThanOrEqualTo(
                    condition.getCreatedAtEnd().atTime(23, 59, 59)
                );
            }
        }

        example.setOrderByClause("created_at DESC");

        List<Order> orders = orderMapper.selectByExample(example);
        return orders.stream().map(this::convertToRes).collect(Collectors.toList());
    }

    @Override
    public OrderDetailRes getOrderDetail(String orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("訂單不存在");
        }

        // 查詢訂單項目
        OrderItemExample itemExample = new OrderItemExample();
        itemExample.createCriteria().andOrderIdEqualTo(orderId);
        List<OrderItem> items = orderItemMapper.selectByExample(itemExample);

        // 查詢相關資料
        User user = userMapper.selectByPrimaryKey(order.getUserId());
        Store store = storeMapper.selectByPrimaryKey(order.getStoreId());

        return OrderDetailRes.builder()
                .id(order.getId())
                .orderNo(order.getOrderNumber())
                .userId(order.getUserId())
                .userNickname(user != null ? user.getNickname() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .storeId(order.getStoreId())
                .storeName(store != null ? store.getStoreName() : null)
                .shippingStatus(order.getStatus())
                .shippingStatusName(OrderStatusEnum.getNameByCode(order.getStatus()))
                .totalItems(order.getTotalItems())
                .shippingMethod(order.getShippingMethod())
                .shippingMethodName(order.getShippingMethod()) // TODO: 轉換為名稱
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .recipientAddress(order.getRecipientAddress())
                .storeCode(order.getStoreCode())
                .storeName(order.getStoreName())
                .storeAddress(order.getStoreAddress())
                .trackingNo(order.getTrackingNo())
                .remark(order.getRemark())
                .items(items.stream().map(this::convertItemToRes).collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void prepareShipping(String orderId, String operatorId) {
        log.info("🔍 準備出貨：orderId={}", orderId);

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("訂單不存在");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (currentStatus != OrderStatusEnum.PENDING) {
            throw new BusinessException("訂單狀態不允許此操作");
        }

        order.setStatus(OrderStatusEnum.PREPARING.getCode());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKey(order);

        recordStatusLog(orderId, OrderStatusEnum.PREPARING.getCode(),
                OrderStatusEnum.PREPARING.getName(), operatorId, null);

        log.info("✅ 訂單狀態更新：PREPARING");
    }

    @Override
    @Transactional
    public void ship(String orderId, OrderShipReq req, String operatorId) {
        log.info("🔍 訂單出貨：orderId={}, trackingNo={}", orderId, req.getTrackingNo());

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("訂單不存在");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (currentStatus != OrderStatusEnum.PREPARING) {
            throw new BusinessException("訂單狀態不允許此操作");
        }

        order.setStatus(OrderStatusEnum.SHIPPED.getCode());
        order.setTrackingNo(req.getTrackingNo());
        order.setRemark(req.getRemark());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKey(order);

        recordStatusLog(orderId, OrderStatusEnum.SHIPPED.getCode(),
                OrderStatusEnum.SHIPPED.getName(), operatorId,
                "物流單號：" + req.getTrackingNo());

        log.info("✅ 訂單已出貨");
    }

    @Override
    @Transactional
    public void complete(String orderId, String operatorId) {
        log.info("🔍 完成訂單：orderId={}", orderId);

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("訂單不存在");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (currentStatus != OrderStatusEnum.SHIPPED) {
            throw new BusinessException("訂單狀態不允許此操作");
        }

        order.setStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKey(order);

        recordStatusLog(orderId, OrderStatusEnum.COMPLETED.getCode(),
                OrderStatusEnum.COMPLETED.getName(), operatorId, null);

        log.info("✅ 訂單已完成");
    }

    @Override
    @Transactional
    public void cancel(String orderId, OrderCancelReq req, String operatorId) {
        log.info("🔍 取消訂單：orderId={}", orderId);

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("訂單不存在");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (!currentStatus.isCancellable()) {
            throw new BusinessException("訂單狀態不允許取消");
        }

        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setRemark(req.getReason());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKey(order);

        recordStatusLog(orderId, OrderStatusEnum.CANCELLED.getCode(),
                OrderStatusEnum.CANCELLED.getName(), operatorId,
                "取消原因：" + req.getReason());

        log.info("✅ 訂單已取消");
    }

    /**
     * 生成訂單編號：ORD + YYYYMMDD + 6位流水號
     */
    private String generateOrderNumber() {
        String datePrefix = "ORD" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 查詢今天最後一筆訂單
        OrderExample example = new OrderExample();
        example.createCriteria().andOrderNumberLike(datePrefix + "%");
        example.setOrderByClause("order_number DESC");

        List<Order> orders = orderMapper.selectByExample(example);

        int sequence = 1;
        if (!orders.isEmpty()) {
            String lastOrderNumber = orders.get(0).getOrderNumber();
            String lastSequence = lastOrderNumber.substring(datePrefix.length());
            sequence = Integer.parseInt(lastSequence) + 1;
        }

        return datePrefix + String.format("%06d", sequence);
    }

    /**
     * 記錄狀態變更
     */
    private void recordStatusLog(String orderId, String status, String statusName,
            String operatorId, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setId(UUID.randomUUID().toString());
        log.setOrderId(orderId);
        log.setToStatus(status);
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());

        orderStatusLogMapper.insert(log);
    }

    /**
     * 轉換為回應 DTO（精簡版）
     */
    private OrderRes convertToRes(Order order) {
        User user = userMapper.selectByPrimaryKey(order.getUserId());
        Store store = storeMapper.selectByPrimaryKey(order.getStoreId());

        return OrderRes.builder()
                .id(order.getId())
                .orderNo(order.getOrderNumber())
                .userId(order.getUserId())
                .userNickname(user != null ? user.getNickname() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .storeId(order.getStoreId())
                .storeName(store != null ? store.getStoreName() : null)
                .shippingStatus(order.getStatus())
                .shippingStatusName(OrderStatusEnum.getNameByCode(order.getStatus()))
                .totalItems(order.getTotalItems())
                .shippingMethod(order.getShippingMethod())
                .shippingMethodName(order.getShippingMethod()) // TODO: 轉換為名稱
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .trackingNo(order.getTrackingNo())
                .createdAt(order.getCreatedAt())
                .build();
    }

    /**
     * 轉換訂單項目為回應 DTO
     */
    private OrderItemRes convertItemToRes(OrderItem item) {
        return OrderItemRes.builder()
                .id(item.getId())
                .orderId(item.getOrderId())
                .prizeBoxId(item.getPrizeBoxId())
                .lotteryId(item.getLotteryId())
                .lotteryTitle(item.getLotteryTitle())
                .lotteryImageUrl(item.getLotteryImageUrl())
                .prizeId(item.getPrizeId())
                .prizeName(item.getPrizeName())
                .prizeImageUrl(item.getPrizeImageUrl())
                .prizeLevel(item.getPrizeLevel())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
