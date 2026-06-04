package com.group.admin.service.impl;

import com.group.admin.condition.OrderCondition;
import com.group.admin.entity.*;
import com.group.admin.enums.OrderStatusEnum;
import com.group.admin.enums.PaymentStatusEnum;
import com.group.admin.example.OrderExample;
import com.group.admin.example.OrderItemExample;
import com.group.admin.example.OrderStatusLogExample;
import com.group.admin.example.PrizeBoxExample;
import com.group.admin.example.ShippingMethodExample;
import com.group.admin.example.StoreUserExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.gateway.GoMyPaySupport;
import com.group.admin.gateway.ShippingCallbackResult;
import com.group.admin.gateway.ShippingPaymentGatewayClient;
import com.group.admin.gateway.ShippingPaymentRequest;
import com.group.admin.gateway.ShippingPaymentResult;
import com.group.admin.mapper.*;
import com.group.admin.repository.OrderRepository;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.order.CancelOrderReq;
import com.group.admin.req.order.CreateOrderReq;
import com.group.admin.req.order.OrderCancelReq;
import com.group.admin.req.order.OrderShipReq;
import com.group.admin.req.order.ShipInfoReq;
import com.group.admin.req.order.UpdateOrderStatusReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.order.OrderDetailRes;
import com.group.admin.res.order.OrderItemRes;
import com.group.admin.res.order.OrderPaymentInitRes;
import com.group.admin.res.order.OrderRes;
import com.group.admin.res.order.StatusLogRes;
import com.group.admin.service.OrderService;
import com.group.admin.service.ConsumptionRecordService;
import com.group.admin.service.BusinessEventLogService;
import com.group.admin.service.logistics.LogisticsService;
import com.group.admin.service.logistics.ShippingInfo;
import com.group.admin.service.logistics.ShippingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 閮??撖虫?
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
    private final LotteryPrizeMapper lotteryPrizeMapper;
    private final StoreMapper storeMapper;
    private final UserMapper userMapper;
    private final StoreUserMapper storeUserMapper;
    private final ShippingMethodMapper shippingMethodMapper;
    private final OrderRepository orderRepository;
    private final ConsumptionRecordService consumptionRecordService;
    private final ShippingPaymentGatewayClient shippingPaymentGatewayClient;
    private final LogisticsService logisticsService;
    private final BusinessEventLogService businessEventLogService;

    private static final Long SHIPPING_FEE = 60L;
    private static final String PRIZE_BOX_STATUS_IN_BOX = "IN_BOX";
    private static final String PRIZE_BOX_STATUS_SHIPPING = "SHIPPING";
    private static final String PRIZE_BOX_STATUS_SHIPPED = "SHIPPED";

    // ==================== 閮撱箇? ====================

    @Override
    @Transactional
    public List<String> createOrdersFromPrizeBox(String userId, List<String> prizeBoxIds,
            String shippingMethod, String recipientName,
            String recipientPhone, String recipientAddress,
            String storeCode, String storeName, String storeAddress) {
        CreateOrderReq req = new CreateOrderReq();
        req.setPrizeBoxIds(prizeBoxIds);
        req.setShippingMethod(shippingMethod);
        req.setRecipientName(recipientName);
        req.setRecipientPhone(recipientPhone);
        req.setRecipientAddress(recipientAddress);
        req.setStoreCode(storeCode);
        req.setStoreName(storeName);
        req.setStoreAddress(storeAddress);

        List<OrderPaymentInitRes> initResults = createOrdersFromPrizeBoxWithPayment(userId, req);
        return initResults.stream().map(OrderPaymentInitRes::getOrderId).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<String> createOrdersFromPrizeBox(String userId, CreateOrderReq req) {
        List<OrderPaymentInitRes> initResults = createOrdersFromPrizeBoxWithPayment(userId, req);
        return initResults.stream().map(OrderPaymentInitRes::getOrderId).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<OrderPaymentInitRes> createOrdersFromPrizeBoxWithPayment(String userId, CreateOrderReq req) {
        log.info("\u958b\u59cb\u5efa\u7acb\u734e\u54c1\u76d2\u51fa\u8ca8\u8a02\u55ae\uff0cuserId={}, prizeBoxCount={}",
                userId, req.getPrizeBoxIds().size());
        ShippingMethod shippingMethod = resolveShippingMethod(req);
        Long shippingFee = shippingMethod.getFee() != null ? shippingMethod.getFee() : SHIPPING_FEE;
        String paymentMethod = GoMyPaySupport.normalizePaymentMethod(req.getPaymentMethod());
        if (req.getShippingFee() != null && !shippingFee.equals(req.getShippingFee())) {
            throw new BusinessException("\u7269\u6d41\u8cbb\u7528\u8207\u7cfb\u7d71\u8a08\u7b97\u7d50\u679c\u4e0d\u4e00\u81f4");
        }
        List<PrizeBox> prizeBoxes = validateAndLoadPrizeBoxes(userId, req.getPrizeBoxIds());
        Map<String, List<PrizeBox>> groupedByStore = prizeBoxes.stream().collect(Collectors.groupingBy(PrizeBox::getStoreId));
        List<Order> createdOrders = new ArrayList<>();
        for (Map.Entry<String, List<PrizeBox>> entry : groupedByStore.entrySet()) {
            String storeId = entry.getKey();
            List<PrizeBox> storePrizeBoxes = entry.getValue();
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setOrderNumber(generateOrderNumber());
            order.setUserId(userId);
            order.setStoreId(storeId);
            order.setStatus(OrderStatusEnum.PAYMENT_PENDING.getCode());
            order.setPaymentStatus(PaymentStatusEnum.PAYMENT_PENDING.getCode());
            order.setPaymentMethod(paymentMethod);
            order.setTotalItems(storePrizeBoxes.size());
            order.setShippingMethod(shippingMethod.getCode());
            order.setShippingFee(shippingFee);
            order.setRecipientName(req.getRecipientName());
            order.setRecipientPhone(req.getRecipientPhone());
            order.setRecipientAddress(req.getRecipientAddress());
            order.setStoreCode(req.getStoreCode());
            order.setStoreName(req.getStoreName());
            order.setStoreAddress(req.getStoreAddress());
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.insert(order);
            List<OrderItem> items = new ArrayList<>();
            for (PrizeBox prizeBox : storePrizeBoxes) {
                Lottery lottery = lotteryMapper.selectByPrimaryKey(prizeBox.getLotteryId());
                LotteryPrize prize = lotteryPrizeMapper.selectByPrimaryKey(prizeBox.getPrizeId());
                OrderItem item = new OrderItem();
                item.setId(UUID.randomUUID().toString());
                item.setOrderId(order.getId());
                item.setPrizeBoxId(prizeBox.getId());
                item.setLotteryId(prizeBox.getLotteryId());
                item.setLotteryTitle(lottery != null ? lottery.getTitle() : "\u672a\u77e5\u4e00\u756a\u8cde");
                item.setLotteryImageUrl(lottery != null ? lottery.getImageUrl() : null);
                item.setPrizeId(prizeBox.getPrizeId());
                item.setPrizeName(prize != null ? prize.getName() : "\u672a\u77e5\u734e\u54c1");
                item.setPrizeImageUrl(prize != null ? prize.getImageUrl() : null);
                item.setPrizeLevel(prize != null ? prize.getLevel() : null);
                item.setCreatedAt(LocalDateTime.now());
                items.add(item);
                prizeBox.setOrderId(order.getId());
                prizeBox.setStatus(PRIZE_BOX_STATUS_SHIPPING);
                prizeBoxMapper.updateByPrimaryKey(prizeBox);
            }
            if (!items.isEmpty()) {
                orderItemMapper.batchInsertOrderItems(items);
            }
            recordStatusLog(order.getId(), null, OrderStatusEnum.PAYMENT_PENDING.getCode(),
                    userId, "USER", null);
            createdOrders.add(order);
        }
        ShippingPaymentResult paymentResult = initShippingPayment(createdOrders, shippingFee, shippingMethod, userId, paymentMethod);
        List<OrderPaymentInitRes> results = createdOrders.stream()
                .map(order -> {
                    if (!paymentResult.isSuccess()) {
                        recordStatusLog(order.getId(), OrderStatusEnum.PAYMENT_PENDING.getCode(),
                                OrderStatusEnum.PAYMENT_FAILED.getCode(),
                                userId, "SYSTEM", "\u7269\u6d41\u8cbb\u4ed8\u6b3e\u5efa\u7acb\u5931\u6557");
                    }
                    return OrderPaymentInitRes.builder()
                            .orderId(order.getId())
                            .orderNumber(order.getOrderNumber())
                            .shippingFee(order.getShippingFee())
                            .paymentStatus(paymentResult.isSuccess()
                                    ? PaymentStatusEnum.PAYMENT_PENDING.getCode()
                                    : PaymentStatusEnum.FAILED.getCode())
                            .paymentMethod(paymentMethod)
                            .paymentUrl(paymentResult.getPayUrl())
                            .submitMethod(paymentResult.getSubmitMethod())
                            .actionUrl(paymentResult.getActionUrl())
                            .formFields(paymentResult.getFormFields())
                            .gatewayTradeNo(paymentResult.getGatewayTradeNo())
                            .gatewayResult(paymentResult.getGatewayResult())
                            .retMsg(paymentResult.getRetMsg())
                            .virtualAccount(paymentResult.getVirtualAccount())
                            .payInfo(paymentResult.getPayInfo())
                            .limitDate(paymentResult.getLimitDate())
                            .build();
                })
                .collect(Collectors.toList());
        log.info("\u734e\u54c1\u76d2\u51fa\u8ca8\u8a02\u55ae\u5efa\u7acb\u5b8c\u6210\uff0corderCount={}", results.size());
        return results;
    }
    @Override
    @Transactional
    public OrderPaymentInitRes retryShippingPayment(String orderId, String userId, String paymentMethod) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "\u8a02\u55ae\u4e0d\u5b58\u5728");
        }
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException("FORBIDDEN", "\u7121\u6b0a\u9650\u91cd\u65b0\u767c\u8d77\u6b64\u8a02\u55ae\u4ed8\u6b3e");
        }
        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (currentStatus != OrderStatusEnum.PAYMENT_PENDING && currentStatus != OrderStatusEnum.PAYMENT_FAILED) {
            throw new BusinessException("INVALID_ORDER_STATUS", "\u53ea\u6709\u5f85\u4ed8\u6b3e\u6216\u4ed8\u6b3e\u5931\u6557\u8a02\u55ae\u53ef\u91cd\u8a66\u7e73\u7d0d\u7269\u6d41\u8cbb");
        }
        ShippingMethod shippingMethod = null;
        if (isNotBlank(order.getShippingMethodId())) {
            shippingMethod = shippingMethodMapper.selectByPrimaryKey(order.getShippingMethodId());
        }
        if (shippingMethod == null && isNotBlank(order.getShippingMethod())) {
            ShippingMethodExample example = new ShippingMethodExample();
            example.createCriteria().andCodeEqualTo(order.getShippingMethod());
            List<ShippingMethod> methods = shippingMethodMapper.selectByExample(example);
            if (!methods.isEmpty()) {
                shippingMethod = methods.get(0);
            }
        }
        if (shippingMethod == null || !"ACTIVE".equals(shippingMethod.getStatus())) {
            throw new BusinessException("\u7269\u6d41\u65b9\u5f0f\u4e0d\u5b58\u5728\u6216\u5c1a\u672a\u555f\u7528");
        }
        String normalizedPaymentMethod = GoMyPaySupport.normalizePaymentMethod(
                paymentMethod != null ? paymentMethod : order.getPaymentMethod());
        List<Order> repayOrders = resolveRepayOrders(order);
        Long shippingFee = order.getShippingFee() != null ? order.getShippingFee() : SHIPPING_FEE;
        ShippingPaymentResult paymentResult = initShippingPayment(repayOrders, shippingFee, shippingMethod, userId, normalizedPaymentMethod);
        if (paymentResult.isSuccess() && currentStatus == OrderStatusEnum.PAYMENT_FAILED) {
            recordStatusLog(order.getId(), OrderStatusEnum.PAYMENT_FAILED.getCode(),
                    OrderStatusEnum.PAYMENT_PENDING.getCode(), userId, "USER", "\u91cd\u65b0\u767c\u8d77\u7269\u6d41\u8cbb\u4ed8\u6b3e");
        }
        return OrderPaymentInitRes.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .shippingFee(shippingFee)
                .paymentStatus(paymentResult.isSuccess()
                        ? PaymentStatusEnum.PAYMENT_PENDING.getCode()
                        : PaymentStatusEnum.FAILED.getCode())
                .paymentMethod(normalizedPaymentMethod)
                .paymentUrl(paymentResult.getPayUrl())
                .submitMethod(paymentResult.getSubmitMethod())
                .actionUrl(paymentResult.getActionUrl())
                .formFields(paymentResult.getFormFields())
                .gatewayTradeNo(paymentResult.getGatewayTradeNo())
                .gatewayResult(paymentResult.getGatewayResult())
                .retMsg(paymentResult.getRetMsg())
                .virtualAccount(paymentResult.getVirtualAccount())
                .payInfo(paymentResult.getPayInfo())
                .limitDate(paymentResult.getLimitDate())
                .build();
    }
    @Override
    public List<OrderPaymentInitRes> getOrdersByPaymentGroup(String merchantOrderNo, String userId) {
        return orderMapper.selectByPaymentReference(merchantOrderNo).stream()
                .filter(order -> userId.equals(order.getUserId()))
                .map(order -> OrderPaymentInitRes.builder()
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .shippingFee(order.getShippingFee())
                        .paymentStatus(order.getPaymentStatus())
                        .paymentMethod(order.getPaymentMethod())
                        .gatewayTradeNo(order.getGomypayTradeNo())
                        .gatewayResult(order.getGatewayResult())
                        .retMsg(order.getGatewayRetMsg())
                        .virtualAccount(order.getVirtualAccount())
                        .payInfo(order.getPaymentInfo())
                        .limitDate(order.getLimitDate())
                        .build())
                .collect(Collectors.toList());
    }
    // ==================== 閮?亥岷 ====================

    @Override
    public PageResult<OrderRes> getOrders(QueryReq<OrderCondition> req) {
        QueryReq<OrderCondition> safeReq = normalizeReq(req);
        OrderCondition condition = safeReq.getCondition();

        int page = resolvePage(safeReq.getPage());
        int size = resolveSize(safeReq.getSize());
        int offset = (page - 1) * size;

        long total = orderMapper.countByCondition(condition);
        if (total == 0) {
            return PageResult.empty(page, size);
        }

        List<Order> orders = orderMapper.selectByConditionPaged(condition, offset, size);
        List<OrderRes> items = orders.stream().map(this::convertToRes).collect(Collectors.toList());
        return PageResult.of(page, size, total, items);
    }

    @Override
    public PageResult<OrderRes> getOrderList(QueryReq<OrderCondition> req, String callerUserId, String callerRole) {
        QueryReq<OrderCondition> safeReq = normalizeReq(req);
        OrderCondition condition = safeReq.getCondition();

        // STORE_OWNER / STORE_EDITOR ?????芸楛蝞∠???摰?
        if ("ROLE_STORE_OWNER".equals(callerRole) || "ROLE_STORE_EDITOR".equals(callerRole)) {
            String storeId = resolveStoreIdForUser(callerUserId);
            if (storeId == null) {
                log.warn("?? 摨振鈭箏?⊿??臬?摰塚?userId={}", callerUserId);
                return PageResult.empty(resolvePage(safeReq.getPage()), resolveSize(safeReq.getSize()));
            }
            condition.setStoreId(storeId);
        }

        int page = resolvePage(safeReq.getPage());
        int size = resolveSize(safeReq.getSize());
        int offset = (page - 1) * size;

        long total = orderMapper.countByCondition(condition);
        if (total == 0) {
            return PageResult.empty(page, size);
        }

        List<Order> orders = orderMapper.selectByConditionPaged(condition, offset, size);
        List<OrderRes> items = orders.stream().map(this::convertToRes).collect(Collectors.toList());
        return PageResult.of(page, size, total, items);
    }

    @Override
    public OrderDetailRes getOrderDetail(String orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "\u8a02\u55ae\u4e0d\u5b58\u5728");
        }
        return buildOrderDetailRes(order);
    }
    @Override
    public OrderDetailRes getOrderById(String id, String callerUserId, String callerRole) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "\u8a02\u55ae\u4e0d\u5b58\u5728");
        }
        if ("ROLE_STORE_OWNER".equals(callerRole) || "ROLE_STORE_EDITOR".equals(callerRole)) {
            String storeId = resolveStoreIdForUser(callerUserId);
            if (storeId == null || !storeId.equals(order.getStoreId())) {
                throw new BusinessException("FORBIDDEN", "\u7121\u6b0a\u9650\u6aa2\u8996\u6b64\u8a02\u55ae");
            }
        }
        return buildOrderDetailRes(order);
    }
    @Override
    public PageResult<OrderRes> getPlayerOrderList(QueryReq<OrderCondition> req, String playerId) {
        QueryReq<OrderCondition> safeReq = normalizeReq(req);
        safeReq.getCondition().setUserId(playerId);

        int page = resolvePage(safeReq.getPage());
        int size = resolveSize(safeReq.getSize());
        int offset = (page - 1) * size;

        long total = orderMapper.countByCondition(safeReq.getCondition());
        if (total == 0) {
            return PageResult.empty(page, size);
        }

        List<Order> orders = orderMapper.selectByConditionPaged(safeReq.getCondition(), offset, size);
        List<OrderRes> items = orders.stream().map(this::convertToRes).collect(Collectors.toList());
        return PageResult.of(page, size, total, items);
    }

    @Override
    public OrderDetailRes getPlayerOrderById(String orderId, String playerId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "\u8a02\u55ae\u4e0d\u5b58\u5728");
        }
        if (!playerId.equals(order.getUserId())) {
            throw new BusinessException("FORBIDDEN", "\u7121\u6b0a\u9650\u6aa2\u8996\u6b64\u8a02\u55ae");
        }
        return buildOrderDetailRes(order);
    }
    // ==================== ?????====================

    @Override
    @Transactional
    public void prepareShipping(String orderId, String operatorId) {
        log.info("\u958b\u59cb\u6e96\u5099\u51fa\u8ca8\uff0corderId={}", orderId);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "\u8a02\u55ae\u4e0d\u5b58\u5728");
        }
        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (currentStatus != OrderStatusEnum.PENDING) {
            throw new BusinessException("INVALID_ORDER_STATUS", "\u50c5\u5f85\u51fa\u8ca8\u8a02\u55ae\u624d\u53ef\u4ee5\u8f49\u70ba\u6e96\u5099\u4e2d");
        }
        String fromStatus = order.getStatus();
        order.setStatus(OrderStatusEnum.PREPARING.getCode());
        order.setPreparingAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKeySelective(order);
        recordStatusLog(orderId, fromStatus, OrderStatusEnum.PREPARING.getCode(),
                operatorId, "ADMIN", null);
        log.info("\u8a02\u55ae\u72c0\u614b\u5df2\u66f4\u65b0\u70ba PREPARING\uff0corderId={}", orderId);
    }
    @Override
    @Transactional
    public ShippingResult createShipment(String orderId, String operatorId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "訂單不存在");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (currentStatus != OrderStatusEnum.PREPARING) {
            throw new BusinessException("LOGISTICS_INVALID_STATUS", "僅準備出貨中的訂單可建立物流單");
        }
        if (isNotBlank(order.getTrackingNo())) {
            throw new BusinessException("LOGISTICS_ALREADY_CREATED", "此訂單已存在物流單號");
        }

        ShippingInfo shippingInfo = ShippingInfo.builder()
                .shippingMethodCode(order.getShippingMethod())
                .amount(order.getShippingFee())
                .orderNumber(order.getOrderNumber())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .recipientAddress(order.getRecipientAddress())
                .storeCode(order.getStoreCode())
                .storeName(order.getStoreName())
                .build();

        ShippingResult result;
        try {
            result = logisticsService.createShipment(orderId, shippingInfo);
        } catch (RuntimeException ex) {
            recordLogisticsEvent(order, "SHIPMENT_CREATE_FAILED", BusinessEventLogService.RESULT_FAILED,
                    null, ex.getMessage());
            throw ex;
        }
        if (!result.isSuccess()) {
            recordLogisticsEvent(order, "SHIPMENT_CREATE_FAILED", BusinessEventLogService.RESULT_FAILED,
                    result, reqValue(result.getMessage(), null, "物流單建立失敗"));
            throw new BusinessException(
                    "LOGISTICS_CREATE_FAILED",
                    reqValue(result.getMessage(), null, "物流單建立失敗"));
        }

        order.setStatus(OrderStatusEnum.SHIPPED.getCode());
        order.setTrackingNo(result.getTrackingNumber());
        order.setTrackingUrl(result.getTrackingUrl());
        order.setLogisticsProvider(result.getProvider());
        order.setLogisticsStatusCode(result.getStatusCode());
        order.setLogisticsStatusName(result.getStatusName());
        order.setLogisticsLabelUrl(result.getLabelUrl());
        order.setLogisticsSyncedAt(LocalDateTime.now());
        order.setRemark(reqValue(result.getMessage(), order.getRemark(), result.getProvider()));
        order.setShippedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKeySelective(order);
        markPrizeBoxesShipped(orderId);

        recordStatusLog(orderId, currentStatus.getCode(), OrderStatusEnum.SHIPPED.getCode(),
                operatorId, "ADMIN", "物流單建立成功: " + result.getTrackingNumber());

        recordLogisticsEvent(order, "SHIPMENT_CREATE_SUCCESS", BusinessEventLogService.RESULT_SUCCESS,
                result, null);
        return result;
    }

    @Override
    @Transactional
    public void ship(String orderId, OrderShipReq req, String operatorId) {
        log.info("\u624b\u52d5\u5efa\u7acb\u51fa\u8ca8\u8cc7\u8a0a\uff0corderId={}, trackingNo={}",
                orderId, req != null ? req.getTrackingNo() : null);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "\u8a02\u55ae\u4e0d\u5b58\u5728");
        }
        if (req == null || isBlank(req.getTrackingNo())) {
            throw new BusinessException("TRACKING_NO_REQUIRED", "\u8acb\u8f38\u5165\u7269\u6d41\u55ae\u865f");
        }
        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (currentStatus != OrderStatusEnum.PREPARING) {
            throw new BusinessException("INVALID_ORDER_STATUS", "\u50c5\u6e96\u5099\u51fa\u8ca8\u4e2d\u7684\u8a02\u55ae\u53ef\u624b\u52d5\u51fa\u8ca8");
        }
        String fromStatus = order.getStatus();
        order.setStatus(OrderStatusEnum.SHIPPED.getCode());
        order.setTrackingNo(req.getTrackingNo());
        ShippingMethod shippingMethod = resolveShippingMethodForOrder(order);
        order.setTrackingUrl(buildTrackingUrl(shippingMethod, req.getTrackingNo()));
        order.setRemark(req.getRemark());
        order.setShippedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKeySelective(order);
        markPrizeBoxesShipped(orderId);
        recordStatusLog(orderId, fromStatus, OrderStatusEnum.SHIPPED.getCode(),
                operatorId, "ADMIN", "\u624b\u52d5\u8a2d\u5b9a\u7269\u6d41\u55ae\u865f: " + req.getTrackingNo());
        recordLogisticsEvent(order, "SHIPMENT_MANUAL_SET", BusinessEventLogService.RESULT_SUCCESS,
                ShippingResult.builder()
                        .success(true)
                        .trackingNumber(req.getTrackingNo())
                        .trackingUrl(order.getTrackingUrl())
                        .provider(order.getLogisticsProvider())
                        .build(),
                null);
        log.info("\u8a02\u55ae\u5df2\u51fa\u8ca8\uff0corderId={}", orderId);
    }
    @Override
    @Transactional
    public void complete(String orderId, String operatorId) {
        log.info("\u6e96\u5099\u5b8c\u6210\u8a02\u55ae\uff0corderId={}", orderId);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "\u8a02\u55ae\u4e0d\u5b58\u5728");
        }
        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (currentStatus != OrderStatusEnum.SHIPPED) {
            throw new BusinessException("INVALID_ORDER_STATUS", "\u50c5\u5df2\u51fa\u8ca8\u8a02\u55ae\u53ef\u6a19\u8a18\u70ba\u5b8c\u6210");
        }
        String fromStatus = order.getStatus();
        order.setStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setCompletedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKeySelective(order);
        markPrizeBoxesShipped(orderId);
        recordStatusLog(orderId, fromStatus, OrderStatusEnum.COMPLETED.getCode(),
                operatorId, operatorId != null ? "ADMIN" : "SYSTEM", null);
        log.info("\u8a02\u55ae\u5df2\u5b8c\u6210\uff0corderId={}", orderId);
    }
    @Override
    @Transactional
    public void cancel(String orderId, OrderCancelReq req, String operatorId) {
        CancelOrderReq cancelReq = new CancelOrderReq();
        cancelReq.setCancelReason(req != null ? req.getCancelReason() : null);
        cancelOrder(orderId, cancelReq, operatorId, "ADMIN");
    }

    @Override
    @Transactional
    public void updateOrderStatus(String id, UpdateOrderStatusReq req, String operatorId, String operatorType) {
        log.info("\u66f4\u65b0\u8a02\u55ae\u72c0\u614b\uff0corderId={}, targetStatus={}", id, req.getTargetStatus());
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "\u8a02\u55ae\u4e0d\u5b58\u5728");
        }
        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        OrderStatusEnum targetStatus = OrderStatusEnum.fromCode(req.getTargetStatus());
        validateTransition(currentStatus, targetStatus);
        String fromStatus = order.getStatus();
        order.setStatus(targetStatus.getCode());
        order.setUpdatedAt(LocalDateTime.now());
        switch (targetStatus) {
            case SHIPPED:
                if (isNotBlank(req.getTrackingNo())) {
                    order.setTrackingNo(req.getTrackingNo());
                    order.setTrackingUrl(buildTrackingUrl(resolveShippingMethodForOrder(order), req.getTrackingNo()));
                }
                order.setShippedAt(LocalDateTime.now());
                break;
            case COMPLETED:
                order.setCompletedAt(LocalDateTime.now());
                break;
            default:
                break;
        }
        if (isNotBlank(req.getRemark())) {
            order.setRemark(req.getRemark());
        }
        orderMapper.updateByPrimaryKeySelective(order);
        if (targetStatus == OrderStatusEnum.SHIPPED || targetStatus == OrderStatusEnum.COMPLETED) {
            markPrizeBoxesShipped(id);
        }
        String logRemark = req.getRemark();
        if (targetStatus == OrderStatusEnum.SHIPPED && isNotBlank(req.getTrackingNo())) {
            logRemark = "\u7269\u6d41\u55ae\u865f: " + req.getTrackingNo()
                    + (isNotBlank(req.getRemark()) ? "\uff0c\u5099\u8a3b: " + req.getRemark() : "");
        }
        recordStatusLog(id, fromStatus, targetStatus.getCode(), operatorId, operatorType, logRemark);
        log.info("\u8a02\u55ae\u72c0\u614b\u66f4\u65b0\u5b8c\u6210\uff0c{} -> {}", fromStatus, targetStatus.getCode());
    }
    @Override
    @Transactional
    public void cancelOrder(String id, CancelOrderReq req, String operatorId, String operatorType) {
        log.info("\u53d6\u6d88\u8a02\u55ae\uff0corderId={}, operator={}", id, operatorId);
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "\u8a02\u55ae\u4e0d\u5b58\u5728");
        }
        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (!canCancelByOperator(currentStatus, operatorType)) {
            throw new BusinessException("INVALID_ORDER_STATUS",
                    "\u76ee\u524d\u8a02\u55ae\u72c0\u614b\u7121\u6cd5\u53d6\u6d88: " + currentStatus.getName());
        }
        String fromStatus = order.getStatus();
        String cancelReason = req != null ? req.getCancelReason() : null;
        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelReason(cancelReason);
        order.setCancelledBy(operatorId);
        order.setCancelledAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKeySelective(order);
        restorePrizeBoxes(id);
        recordStatusLog(id, fromStatus, OrderStatusEnum.CANCELLED.getCode(),
                operatorId, operatorType,
                cancelReason != null ? "\u53d6\u6d88\u539f\u56e0: " + cancelReason : null);
        log.info("\u8a02\u55ae\u5df2\u53d6\u6d88\uff0corderId={}", id);
    }
    @Override
    @Transactional
    public void submitShippingInfo(String orderId, ShipInfoReq req, String userId) {
        log.info("\u63d0\u4ea4\u7269\u6d41\u8cc7\u8a0a\uff0corderId={}, userId={}", orderId, userId);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "\u8a02\u55ae\u4e0d\u5b58\u5728");
        }
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new BusinessException("FORBIDDEN", "\u7121\u6b0a\u9650\u4fee\u6539\u6b64\u8a02\u55ae");
        }
        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (currentStatus != OrderStatusEnum.PAYMENT_PENDING) {
            throw new BusinessException("INVALID_ORDER_STATUS", "\u50c5\u5f85\u7e73\u7269\u6d41\u8cbb\u8a02\u55ae\u53ef\u63d0\u4ea4\u6536\u4ef6\u8cc7\u8a0a");
        }
        String method = req.getShippingMethod();
        if ("HOME_DELIVERY".equals(method)) {
            if (isBlank(req.getRecipientName())) {
                throw new BusinessException("\u8acb\u586b\u5beb\u6536\u4ef6\u4eba\u59d3\u540d");
            }
            if (isBlank(req.getRecipientPhone())) {
                throw new BusinessException("\u8acb\u586b\u5beb\u6536\u4ef6\u4eba\u806f\u7d61\u96fb\u8a71");
            }
            if (isBlank(req.getRecipientAddress())) {
                throw new BusinessException("\u8acb\u586b\u5beb\u5b85\u914d\u5730\u5740");
            }
        } else if ("SEVEN_ELEVEN".equals(method) || "FAMILY_MART".equals(method)) {
            if (isBlank(req.getStoreCode())) {
                throw new BusinessException("\u8acb\u9078\u64c7\u8d85\u5546\u9580\u5e02\u4ee3\u78bc");
            }
            if (isBlank(req.getStoreName())) {
                throw new BusinessException("\u8acb\u9078\u64c7\u8d85\u5546\u9580\u5e02\u540d\u7a31");
            }
        } else {
            throw new BusinessException("UNSUPPORTED_SHIPPING_METHOD", "\u4e0d\u652f\u63f4\u7684\u7269\u6d41\u65b9\u5f0f");
        }
        order.setShippingMethod(method);
        order.setRecipientName(req.getRecipientName());
        order.setRecipientPhone(req.getRecipientPhone());
        order.setRecipientAddress(req.getRecipientAddress());
        order.setStoreCode(req.getStoreCode());
        order.setStoreName(req.getStoreName());
        order.setStoreAddress(req.getStoreAddress());
        order.setRemark(req.getRemark());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKey(order);
        log.info("\u7269\u6d41\u8cc7\u8a0a\u5df2\u66f4\u65b0\uff0corderId={}", orderId);
    }
    @Override
    @Transactional
    public void handleShippingPaymentCallback(ShippingCallbackResult callbackResult) {
        if (callbackResult == null || isBlank(callbackResult.getOrderNumber())) {
            throw new BusinessException("INVALID_CALLBACK", "\u7269\u6d41\u8cbb\u4ed8\u6b3e\u56de\u8abf\u8cc7\u6599\u4e0d\u5b8c\u6574");
        }
        List<Order> orders = orderMapper.selectByPaymentReference(callbackResult.getOrderNumber());
        if (orders.isEmpty() && isNotBlank(callbackResult.getGatewayTradeNo())) {
            orders = orderMapper.selectByPaymentReference(callbackResult.getGatewayTradeNo());
        }
        if (orders.isEmpty()) {
            throw new BusinessException("ORDER_NOT_FOUND", "\u627e\u4e0d\u5230\u5c0d\u61c9\u7684\u8a02\u55ae: " + callbackResult.getOrderNumber());
        }
        if (callbackResult.isSuccess()) {
            for (Order order : orders) {
                String beforePaymentStatus = order.getPaymentStatus();
                orderMapper.markShippingPaymentSuccess(order.getId(), null);
                if (OrderStatusEnum.PAYMENT_PENDING.getCode().equals(order.getStatus())
                        || OrderStatusEnum.PAYMENT_FAILED.getCode().equals(order.getStatus())) {
                    recordStatusLog(order.getId(), order.getStatus(), OrderStatusEnum.PENDING.getCode(),
                            null, "SYSTEM", "GoMyPay \u7269\u6d41\u8cbb\u4ed8\u6b3e\u6210\u529f");
                }
                recordShippingPaymentCallbackEvent(order, callbackResult, "SHIPPING_PAYMENT_CALLBACK_SUCCESS",
                        BusinessEventLogService.RESULT_SUCCESS, beforePaymentStatus, PaymentStatusEnum.PAID.getCode(),
                        null);
                log.info("\u7269\u6d41\u8cbb\u4ed8\u6b3e\u6210\u529f\uff0corderId={}, orderNo={}, gatewayOrderId={}",
                        order.getId(), order.getOrderNumber(), callbackResult.getGatewayTradeNo());
            }
            return;
        }
        String errorMessage = callbackResult.getErrorMessage();
        for (Order order : orders) {
            String fromStatus = order.getStatus();
            String beforePaymentStatus = order.getPaymentStatus();
            orderMapper.markShippingPaymentFailed(order.getId(), null, errorMessage);
            if (!OrderStatusEnum.PAYMENT_FAILED.getCode().equals(fromStatus)) {
                recordStatusLog(order.getId(), fromStatus, OrderStatusEnum.PAYMENT_FAILED.getCode(),
                        null, "SYSTEM", "GoMyPay \u7269\u6d41\u8cbb\u4ed8\u6b3e\u5931\u6557");
            }
            recordShippingPaymentCallbackEvent(order, callbackResult, "SHIPPING_PAYMENT_CALLBACK_FAILED",
                    BusinessEventLogService.RESULT_FAILED, beforePaymentStatus, PaymentStatusEnum.FAILED.getCode(),
                    errorMessage);
            log.warn("\u7269\u6d41\u8cbb\u4ed8\u6b3e\u5931\u6557\uff0corderId={}, orderNo={}, reason={}",
                    order.getId(), order.getOrderNumber(), errorMessage);
        }
    }
    // ==================== ?折頛?寞? ====================

    private List<PrizeBox> validateAndLoadPrizeBoxes(String userId, List<String> prizeBoxIds) {
        List<PrizeBox> prizeBoxes = new ArrayList<>();
        for (String boxId : prizeBoxIds) {
            PrizeBox box = prizeBoxMapper.selectByPrimaryKey(boxId);
            if (box == null) {
                throw new BusinessException("\u627e\u4e0d\u5230\u734e\u54c1\u76d2: " + boxId);
            }
            if (!userId.equals(box.getUserId())) {
                throw new BusinessException("\u734e\u54c1\u76d2\u4e0d\u5c6c\u65bc\u76ee\u524d\u4f7f\u7528\u8005: " + boxId);
            }
            if (!isPrizeBoxAvailable(box.getStatus())) {
                throw new BusinessException("\u734e\u54c1\u76d2\u76ee\u524d\u7121\u6cd5\u7528\u65bc\u51fa\u8ca8: " + boxId);
            }
            prizeBoxes.add(box);
        }
        return prizeBoxes;
    }
    private ShippingMethod resolveShippingMethod(CreateOrderReq req) {
        ShippingMethod shippingMethod = null;
        if (isNotBlank(req.getShippingMethodId())) {
            shippingMethod = shippingMethodMapper.selectByPrimaryKey(req.getShippingMethodId());
        }
        if (shippingMethod == null && isNotBlank(req.getShippingMethod())) {
            ShippingMethodExample example = new ShippingMethodExample();
            example.createCriteria().andCodeEqualTo(req.getShippingMethod());
            List<ShippingMethod> methods = shippingMethodMapper.selectByExample(example);
            if (!methods.isEmpty()) {
                shippingMethod = methods.get(0);
            }
        }
        if (shippingMethod == null) {
            throw new BusinessException("\u627e\u4e0d\u5230\u5c0d\u61c9\u7684\u7269\u6d41\u65b9\u5f0f");
        }
        if (!"ACTIVE".equals(shippingMethod.getStatus())) {
            throw new BusinessException("\u8a72\u7269\u6d41\u65b9\u5f0f\u76ee\u524d\u672a\u555f\u7528");
        }
        return shippingMethod;
    }
    private ShippingMethod resolveShippingMethodForOrder(Order order) {
        if (order == null) {
            return null;
        }

        if (isNotBlank(order.getShippingMethodId())) {
            ShippingMethod byId = shippingMethodMapper.selectByPrimaryKey(order.getShippingMethodId());
            if (byId != null) {
                return byId;
            }
        }

        if (isBlank(order.getShippingMethod())) {
            return null;
        }

        ShippingMethodExample example = new ShippingMethodExample();
        example.createCriteria().andCodeEqualTo(order.getShippingMethod());
        return shippingMethodMapper.selectByExample(example).stream().findFirst().orElse(null);
    }

    private String buildTrackingUrl(ShippingMethod shippingMethod, String trackingNo) {
        if (shippingMethod == null || isBlank(trackingNo)) {
            return null;
        }

        return switch (shippingMethod.getCode()) {
            case "HOME_DELIVERY" ->
                    "https://www.sf-express.com/tw/tc/dynamic_function/waybill/#search/bill-number/" + trackingNo;
            case "SEVEN_ELEVEN" ->
                    "https://eservice.7-11.com.tw/e-tracking/search.aspx?TBSTKECNO=" + trackingNo;
            case "FAMILY_MART" ->
                    "https://www.famiport.com.tw/Web_Famiport/page/process.aspx?PGMID=ORDERQUERY";
            default -> null;
        };
    }

    private ShippingPaymentResult initShippingPayment(List<Order> orders, Long shippingFee, ShippingMethod shippingMethod,
                                                      String userId, String paymentMethod) {
        if (orders == null || orders.isEmpty()) {
            return ShippingPaymentResult.builder().success(false).errorMessage("\u67e5\u7121\u53ef\u652f\u4ed8\u7684\u8a02\u55ae").build();
        }
        String merchantOrderNo = generateShippingPaymentGroupNo();
        User user = userMapper.selectByPrimaryKey(userId);
        long totalAmount = orders.stream()
                .map(Order::getShippingFee)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
        if (totalAmount <= 0) {
            totalAmount = shippingFee * orders.size();
        }
        ShippingPaymentRequest paymentRequest = ShippingPaymentRequest.builder()
                .merchantOrderNo(merchantOrderNo)
                .orderId(orders.get(0).getId())
                .orderNumber(orders.get(0).getOrderNumber())
                .amount(BigDecimal.valueOf(totalAmount))
                .buyerName(reqValue(orders.get(0).getRecipientName(), user != null ? user.getNickname() : null, "\u6703\u54e1"))
                .buyerEmail(user != null ? user.getEmail() : null)
                .buyerPhone(reqValue(orders.get(0).getRecipientPhone(), null, ""))
                .itemDescription("\u8a02\u55ae\u904b\u8cbb x" + orders.size() + " (" + shippingMethod.getName() + ")")
                .paymentMethod(paymentMethod)
                .build();
        ShippingPaymentResult paymentResult = shippingPaymentGatewayClient.createPayment(paymentRequest);
        if (paymentResult != null && paymentResult.isSuccess()) {
            for (Order order : orders) {
                Long orderShippingFee = order.getShippingFee() != null ? order.getShippingFee() : shippingFee;
                orderMapper.updatePaymentInit(order.getId(), orderShippingFee, paymentMethod,
                        PaymentStatusEnum.PAYMENT_PENDING.getCode(), OrderStatusEnum.PAYMENT_PENDING.getCode(),
                        paymentResult.getGatewayTradeNo(), paymentResult.getGatewayResult(), paymentResult.getRetMsg(),
                        paymentResult.getVirtualAccount(), paymentResult.getPayInfo(), paymentResult.getLimitDate(),
                        paymentResult.getRawPayload());
                recordShippingPaymentEvent(order, "SHIPPING_PAYMENT_CREATE", BusinessEventLogService.RESULT_PENDING,
                        paymentMethod, paymentResult, null);
            }
            return paymentResult;
        }
        String failedReason = paymentResult != null ? paymentResult.getErrorMessage() : "\u7269\u6d41\u8cbb\u652f\u4ed8\u5efa\u7acb\u5931\u6557";
        for (Order order : orders) {
            Long orderShippingFee = order.getShippingFee() != null ? order.getShippingFee() : shippingFee;
            orderMapper.updatePaymentInit(order.getId(), orderShippingFee, paymentMethod,
                    PaymentStatusEnum.FAILED.getCode(), OrderStatusEnum.PAYMENT_FAILED.getCode(), null,
                    paymentResult != null ? paymentResult.getGatewayResult() : null,
                    paymentResult != null ? paymentResult.getRetMsg() : failedReason,
                    paymentResult != null ? paymentResult.getVirtualAccount() : null,
                    paymentResult != null ? paymentResult.getPayInfo() : null,
                    paymentResult != null ? paymentResult.getLimitDate() : null,
                    paymentResult != null ? paymentResult.getRawPayload() : null);
            orderMapper.markShippingPaymentFailed(order.getId(), null, failedReason);
            recordShippingPaymentEvent(order, "SHIPPING_PAYMENT_CREATE_FAILED", BusinessEventLogService.RESULT_FAILED,
                    paymentMethod, paymentResult, failedReason);
        }
        return ShippingPaymentResult.builder()
                .success(false)
                .errorMessage(failedReason)
                .build();
    }
    private List<Order> resolveRepayOrders(Order order) {
        if (isBlank(order.getGomypayTradeNo())) {
            return List.of(order);
        }

        List<Order> groupOrders = orderMapper.selectByGomypayTradeNo(order.getGomypayTradeNo()).stream()
                .filter(item -> OrderStatusEnum.PAYMENT_PENDING.getCode().equals(item.getStatus())
                        || OrderStatusEnum.PAYMENT_FAILED.getCode().equals(item.getStatus()))
                .collect(Collectors.toList());
        return groupOrders.isEmpty() ? List.of(order) : groupOrders;
    }

    private String generateShippingPaymentGroupNo() {
        return "SP" + DateTimeFormatter.ofPattern("yyMMddHHmmss").format(LocalDateTime.now())
                + UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase();
    }

    private String reqValue(String primary, String secondary, String fallback) {
        if (isNotBlank(primary)) {
            return primary;
        }
        if (isNotBlank(secondary)) {
            return secondary;
        }
        return fallback;
    }

    private void validateTransition(OrderStatusEnum from, OrderStatusEnum to) {
        boolean valid = switch (to) {
            case PREPARING -> from == OrderStatusEnum.PENDING;
            case SHIPPED -> from == OrderStatusEnum.PREPARING;
            case COMPLETED -> from == OrderStatusEnum.SHIPPED;
            case CANCELLED -> from.isCancellable();
            default -> false;
        };
        if (!valid) {
            throw new BusinessException("INVALID_ORDER_STATUS",
                    String.format("\u8a02\u55ae\u72c0\u614b\u7121\u6cd5\u5f9e %s \u8f49\u70ba %s", from.getName(), to.getName()));
        }
    }
    private void restorePrizeBoxes(String orderId) {
        OrderItemExample itemExample = new OrderItemExample();
        itemExample.createCriteria().andOrderIdEqualTo(orderId);
        List<OrderItem> items = orderItemMapper.selectByExample(itemExample);
        for (OrderItem item : items) {
            if (item.getPrizeBoxId() != null) {
                PrizeBox prizeBox = prizeBoxMapper.selectByPrimaryKey(item.getPrizeBoxId());
                if (prizeBox != null) {
                    prizeBox.setStatus(PRIZE_BOX_STATUS_IN_BOX);
                    prizeBox.setOrderId(null);
                    prizeBox.setShippedAt(null);
                    prizeBox.setUpdatedAt(LocalDateTime.now());
                    prizeBoxMapper.updateByPrimaryKey(prizeBox);
                    log.info("\u5df2\u91cb\u653e\u734e\u54c1\u76d2\uff0cprizeBoxId={}", prizeBox.getId());
                }
            }
        }
    }

    private void markPrizeBoxesShipped(String orderId) {
        OrderItemExample itemExample = new OrderItemExample();
        itemExample.createCriteria().andOrderIdEqualTo(orderId);
        List<OrderItem> items = orderItemMapper.selectByExample(itemExample);
        for (OrderItem item : items) {
            if (item.getPrizeBoxId() == null) {
                continue;
            }
            PrizeBox prizeBox = prizeBoxMapper.selectByPrimaryKey(item.getPrizeBoxId());
            if (prizeBox == null) {
                continue;
            }
            if (PRIZE_BOX_STATUS_SHIPPING.equals(prizeBox.getStatus())) {
                prizeBox.setStatus(PRIZE_BOX_STATUS_SHIPPED);
                prizeBox.setShippedAt(LocalDateTime.now());
                prizeBoxMapper.updateByPrimaryKeySelective(prizeBox);
                log.info("賞品盒已標記為已出貨，prizeBoxId={}", prizeBox.getId());
            }
        }
    }

    private boolean canCancelByOperator(OrderStatusEnum currentStatus, String operatorType) {
        if ("PLAYER".equalsIgnoreCase(operatorType)) {
            return currentStatus == OrderStatusEnum.PAYMENT_PENDING
                    || currentStatus == OrderStatusEnum.PAYMENT_FAILED
                    || currentStatus == OrderStatusEnum.PENDING;
        }

        return currentStatus == OrderStatusEnum.PAYMENT_PENDING
                || currentStatus == OrderStatusEnum.PAYMENT_FAILED
                || currentStatus == OrderStatusEnum.PENDING
                || currentStatus == OrderStatusEnum.PREPARING;
    }

    private boolean isPrizeBoxAvailable(String status) {
        return PRIZE_BOX_STATUS_IN_BOX.equals(status);
    }

    private String resolveStoreIdForUser(String adminUserId) {
        StoreUserExample example = new StoreUserExample();
        example.createCriteria().andAdminUserIdEqualTo(adminUserId);
        List<StoreUser> storeUsers = storeUserMapper.selectByExample(example);
        if (storeUsers.isEmpty()) {
            return null;
        }
        return storeUsers.get(0).getStoreId();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private QueryReq<OrderCondition> normalizeReq(QueryReq<OrderCondition> req) {
        if (req == null) {
            req = new QueryReq<>();
        }
        if (req.getCondition() == null) {
            req.setCondition(new OrderCondition());
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

    private String generateOrderNumber() {
        String datePrefix = "ORD" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

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

    private void recordStatusLog(String orderId, String fromStatus, String toStatus,
            String operatorId, String operatorType, String remark) {
        OrderStatusLog statusLog = new OrderStatusLog();
        statusLog.setId(UUID.randomUUID().toString());
        statusLog.setOrderId(orderId);
        statusLog.setFromStatus(fromStatus);
        statusLog.setToStatus(toStatus);
        statusLog.setOperatorId(operatorId);
        statusLog.setOperatorType(operatorType);
        statusLog.setRemark(remark);
        statusLog.setCreatedAt(LocalDateTime.now());

        orderStatusLogMapper.insert(statusLog);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        businessEventLogService.record(BusinessEventLog.builder()
                .eventType(BusinessEventLogService.EVENT_ORDER_STATUS)
                .action("ORDER_STATUS_CHANGE")
                .result(BusinessEventLogService.RESULT_SUCCESS)
                .actorType(operatorType)
                .actorId(operatorId)
                .targetType("ORDER")
                .targetId(orderId)
                .targetNo(order != null ? order.getOrderNumber() : null)
                .userId(order != null ? order.getUserId() : null)
                .orderId(orderId)
                .beforeStatus(fromStatus)
                .afterStatus(toStatus)
                .afterSnapshot(toJson("remark", remark))
                .build());
    }

    private void recordShippingPaymentEvent(Order order, String action, String result,
                                            String paymentMethod, ShippingPaymentResult paymentResult,
                                            String errorMessage) {
        if (order == null) {
            return;
        }
        businessEventLogService.record(BusinessEventLog.builder()
                .eventType(BusinessEventLogService.EVENT_PAYMENT)
                .action(action)
                .result(result)
                .actorType("USER")
                .actorId(order.getUserId())
                .targetType("ORDER")
                .targetId(order.getId())
                .targetNo(order.getOrderNumber())
                .userId(order.getUserId())
                .orderId(order.getId())
                .externalProvider("GoMyPay")
                .externalRef(paymentResult != null ? paymentResult.getGatewayTradeNo() : null)
                .amount(order.getShippingFee())
                .paymentMethod(paymentMethod)
                .beforeStatus(order.getPaymentStatus())
                .afterStatus(PaymentStatusEnum.PAYMENT_PENDING.getCode())
                .afterSnapshot(paymentInfoSnapshot(paymentResult))
                .errorMessage(errorMessage)
                .build());
    }

    private void recordShippingPaymentCallbackEvent(Order order, ShippingCallbackResult callbackResult,
                                                    String action, String result, String beforeStatus,
                                                    String afterStatus, String errorMessage) {
        if (order == null) {
            return;
        }
        businessEventLogService.record(BusinessEventLog.builder()
                .eventType(BusinessEventLogService.EVENT_PAYMENT)
                .action(action)
                .result(result)
                .actorType("CALLBACK")
                .targetType("ORDER")
                .targetId(order.getId())
                .targetNo(order.getOrderNumber())
                .userId(order.getUserId())
                .orderId(order.getId())
                .externalProvider("GoMyPay")
                .externalRef(callbackResult != null ? reqValue(callbackResult.getGatewayTradeNo(),
                        callbackResult.getOrderNumber(), null) : null)
                .amount(order.getShippingFee())
                .paymentMethod(order.getPaymentMethod())
                .beforeStatus(beforeStatus)
                .afterStatus(afterStatus)
                .errorMessage(errorMessage)
                .build());
    }

    private void recordLogisticsEvent(Order order, String action, String result,
                                      ShippingResult shippingResult, String errorMessage) {
        if (order == null) {
            return;
        }
        businessEventLogService.record(BusinessEventLog.builder()
                .eventType(BusinessEventLogService.EVENT_LOGISTICS)
                .action(action)
                .result(result)
                .actorType("SYSTEM")
                .targetType("ORDER")
                .targetId(order.getId())
                .targetNo(order.getOrderNumber())
                .userId(order.getUserId())
                .orderId(order.getId())
                .externalProvider(shippingResult != null ? shippingResult.getProvider() : order.getLogisticsProvider())
                .externalRef(shippingResult != null ? reqValue(shippingResult.getProviderOrderNo(),
                        shippingResult.getTrackingNumber(), null) : order.getTrackingNo())
                .amount(order.getShippingFee())
                .paymentMethod(order.getShippingMethod())
                .beforeStatus(order.getStatus())
                .afterStatus(shippingResult != null ? shippingResult.getStatusCode() : null)
                .afterSnapshot(logisticsSnapshot(shippingResult))
                .errorMessage(errorMessage)
                .build());
    }

    private String paymentInfoSnapshot(ShippingPaymentResult result) {
        if (result == null) {
            return null;
        }
        return "{"
                + jsonPair("gatewayResult", result.getGatewayResult()) + ","
                + jsonPair("retMsg", result.getRetMsg()) + ","
                + jsonPair("virtualAccount", result.getVirtualAccount()) + ","
                + jsonPair("payInfo", result.getPayInfo()) + ","
                + jsonPair("limitDate", result.getLimitDate())
                + "}";
    }

    private String logisticsSnapshot(ShippingResult result) {
        if (result == null) {
            return null;
        }
        return "{"
                + jsonPair("trackingNumber", result.getTrackingNumber()) + ","
                + jsonPair("providerOrderNo", result.getProviderOrderNo()) + ","
                + jsonPair("provider", result.getProvider()) + ","
                + jsonPair("statusCode", result.getStatusCode()) + ","
                + jsonPair("statusName", result.getStatusName()) + ","
                + jsonPair("labelUrl", result.getLabelUrl())
                + "}";
    }

    private String toJson(String key, String value) {
        if (value == null) {
            return null;
        }
        return "{" + jsonPair(key, value) + "}";
    }

    private String jsonPair(String key, String value) {
        return "\"" + escapeJson(key) + "\":" + (value == null ? "null" : "\"" + escapeJson(value) + "\"");
    }

    private String escapeJson(String value) {
        return value == null ? null : value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    // ==================== DTO 頧? ====================

    private OrderDetailRes buildOrderDetailRes(Order order) {
        OrderItemExample itemExample = new OrderItemExample();
        itemExample.createCriteria().andOrderIdEqualTo(order.getId());
        List<OrderItem> items = orderItemMapper.selectByExample(itemExample);

        OrderStatusLogExample logExample = new OrderStatusLogExample();
        logExample.createCriteria().andOrderIdEqualTo(order.getId());
        logExample.setOrderByClause("created_at ASC");
        List<OrderStatusLog> logs = orderStatusLogMapper.selectByExample(logExample);

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
                .storeLogoUrl(store != null ? store.getLogoUrl() : null)
                .shippingStatus(order.getStatus())
                .shippingStatusName(OrderStatusEnum.getNameByCode(order.getStatus()))
                .totalItems(order.getTotalItems())
                .shippingMethod(order.getShippingMethod())
                .shippingMethodName(getShippingMethodLabel(order.getShippingMethod()))
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .recipientAddress(order.getRecipientAddress())
                .storeCode(order.getStoreCode())
                .storeAddress(order.getStoreAddress())
                .trackingNo(order.getTrackingNo())
                .trackingUrl(order.getTrackingUrl())
                .logisticsProvider(order.getLogisticsProvider())
                .logisticsStatusCode(order.getLogisticsStatusCode())
                .logisticsStatusName(order.getLogisticsStatusName())
                .logisticsLabelUrl(order.getLogisticsLabelUrl())
                .remark(order.getRemark())
                .items(items.stream().map(this::convertItemToRes).collect(Collectors.toList()))
                .statusHistory(logs.stream().map(this::convertLogToRes).collect(Collectors.toList()))
                .subtotal(0L)
                .shippingFee(SHIPPING_FEE)
                .discount(0L)
                .totalAmount(order.getShippingFee() != null ? order.getShippingFee() : SHIPPING_FEE)
                .shippingFee(order.getShippingFee() != null ? order.getShippingFee() : SHIPPING_FEE)
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .virtualAccount(order.getVirtualAccount())
                .paymentInfo(order.getPaymentInfo())
                .limitDate(order.getLimitDate())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .shippedAt(order.getShippedAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .cancelledBy(order.getCancelledBy())
                .cancelReason(order.getCancelReason())
                .build();
    }

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
                .shippingMethodName(getShippingMethodLabel(order.getShippingMethod()))
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .trackingNo(order.getTrackingNo())
                .trackingUrl(order.getTrackingUrl())
                .logisticsProvider(order.getLogisticsProvider())
                .logisticsStatusCode(order.getLogisticsStatusCode())
                .logisticsStatusName(order.getLogisticsStatusName())
                .logisticsLabelUrl(order.getLogisticsLabelUrl())
                .totalAmount(order.getShippingFee() != null ? order.getShippingFee() : SHIPPING_FEE)
                .shippingFee(order.getShippingFee() != null ? order.getShippingFee() : SHIPPING_FEE)
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .virtualAccount(order.getVirtualAccount())
                .paymentInfo(order.getPaymentInfo())
                .limitDate(order.getLimitDate())
                .createdAt(order.getCreatedAt())
                .shippedAt(order.getShippedAt())
                .completedAt(order.getCompletedAt())
                .build();
    }

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

    private StatusLogRes convertLogToRes(OrderStatusLog log) {
        return StatusLogRes.builder()
                .fromStatus(log.getFromStatus())
                .fromStatusLabel(log.getFromStatus() != null ? OrderStatusEnum.getNameByCode(log.getFromStatus()) : null)
                .toStatus(log.getToStatus())
                .toStatusLabel(OrderStatusEnum.getNameByCode(log.getToStatus()))
                .operatorId(log.getOperatorId())
                .operatorType(log.getOperatorType())
                .remark(log.getRemark())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private String getShippingMethodLabel(String method) {
        if (method == null) return null;
        return switch (method) {
            case "HOME_DELIVERY" -> "宅配到府（順豐）";
            case "SEVEN_ELEVEN" -> "7-11 \u8d85\u5546\u53d6\u8ca8";
            case "FAMILY_MART" -> "\u5168\u5bb6\u8d85\u5546\u53d6\u8ca8";
            default -> method;
        };
    }
    @Override
    public List<StatusLogRes> getStatusLog(String orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "\u8a02\u55ae\u4e0d\u5b58\u5728");
        }
        OrderStatusLogExample example = new OrderStatusLogExample();
        example.createCriteria().andOrderIdEqualTo(orderId);
        example.setOrderByClause("created_at ASC");
        return orderStatusLogMapper.selectByExample(example).stream()
                .map(this::convertLogToRes)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public com.group.admin.res.order.OrderRes cancelOrder(String id, com.group.admin.req.order.OrderCancelReq req, String operatorId, String operatorType) {
        CancelOrderReq cancelReq = new CancelOrderReq();
        cancelReq.setCancelReason(req != null ? req.getCancelReason() : null);
        cancelOrder(id, cancelReq, operatorId, operatorType);
        Order order = orderMapper.selectByPrimaryKey(id);
        return order != null ? convertToRes(order) : null;
    }
}





