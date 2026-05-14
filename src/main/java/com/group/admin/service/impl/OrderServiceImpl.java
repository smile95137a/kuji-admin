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
    private final LotteryPrizeMapper lotteryPrizeMapper;
    private final StoreMapper storeMapper;
    private final UserMapper userMapper;
    private final StoreUserMapper storeUserMapper;
    private final ShippingMethodMapper shippingMethodMapper;
    private final OrderRepository orderRepository;
    private final ConsumptionRecordService consumptionRecordService;
    private final ShippingPaymentGatewayClient shippingPaymentGatewayClient;

    private static final Long SHIPPING_FEE = 60L;
    private static final String PRIZE_BOX_STATUS_AVAILABLE = "AVAILABLE";
    private static final String PRIZE_BOX_STATUS_IN_BOX = "IN_BOX";
    private static final String PRIZE_BOX_STATUS_SHIPPING = "SHIPPING";

    // ==================== 訂單建立 ====================

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
        log.info("🔍 從賞品盒建立訂單（含支付）：userId={}, prizeBoxCount={}", userId, req.getPrizeBoxIds().size());

        ShippingMethod shippingMethod = resolveShippingMethod(req);
        Long shippingFee = shippingMethod.getFee() != null ? shippingMethod.getFee() : SHIPPING_FEE;
        String paymentMethod = GoMyPaySupport.normalizePaymentMethod(req.getPaymentMethod());

        if (req.getShippingFee() != null && !shippingFee.equals(req.getShippingFee())) {
            throw new BusinessException("運費資訊已更新，請重新確認配送方式後再送出");
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
                item.setLotteryTitle(lottery != null ? lottery.getTitle() : "未知商品");
                item.setLotteryImageUrl(lottery != null ? lottery.getImageUrl() : null);
                item.setPrizeId(prizeBox.getPrizeId());
                item.setPrizeName(prize != null ? prize.getName() : "未知獎品");
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
                        userId, "SYSTEM", "建立付款單失敗");
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
                        .build();
                })
                .collect(Collectors.toList());

        log.info("✅ 訂單建立完成（含支付初始化）：orderCount={}", results.size());
        return results;
    }

    @Override
    @Transactional
            public OrderPaymentInitRes retryShippingPayment(String orderId, String userId, String paymentMethod) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("訂單不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException("無權限操作此訂單");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (currentStatus != OrderStatusEnum.PAYMENT_PENDING && currentStatus != OrderStatusEnum.PAYMENT_FAILED) {
            throw new BusinessException("目前狀態無法重新付款");
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
            throw new BusinessException("運送方式不存在或已停用");
        }

        String normalizedPaymentMethod = GoMyPaySupport.normalizePaymentMethod(
            paymentMethod != null ? paymentMethod : order.getPaymentMethod());
        List<Order> repayOrders = resolveRepayOrders(order);
        Long shippingFee = order.getShippingFee() != null ? order.getShippingFee() : SHIPPING_FEE;
        ShippingPaymentResult paymentResult = initShippingPayment(repayOrders, shippingFee, shippingMethod, userId, normalizedPaymentMethod);

        if (paymentResult.isSuccess() && currentStatus == OrderStatusEnum.PAYMENT_FAILED) {
            recordStatusLog(order.getId(), OrderStatusEnum.PAYMENT_FAILED.getCode(),
                    OrderStatusEnum.PAYMENT_PENDING.getCode(), userId, "USER", "重新建立付款單");
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
                .build();
    }

            @Override
            public List<OrderPaymentInitRes> getOrdersByPaymentGroup(String merchantOrderNo, String userId) {
            return orderMapper.selectByGomypayTradeNo(merchantOrderNo).stream()
                .filter(order -> userId.equals(order.getUserId()))
                .map(order -> OrderPaymentInitRes.builder()
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .shippingFee(order.getShippingFee())
                    .paymentStatus(order.getPaymentStatus())
                    .paymentMethod(order.getPaymentMethod())
                    .gatewayTradeNo(order.getGomypayTradeNo())
                    .build())
                .collect(Collectors.toList());
            }

    // ==================== 訂單查詢 ====================

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

        // STORE_OWNER / STORE_EDITOR → 限定自己管理的店家
        if ("ROLE_STORE_OWNER".equals(callerRole) || "ROLE_STORE_EDITOR".equals(callerRole)) {
            String storeId = resolveStoreIdForUser(callerUserId);
            if (storeId == null) {
                log.warn("⚠️ 店家人員無關聯店家：userId={}", callerUserId);
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
            throw new BusinessException("訂單不存在");
        }
        return buildOrderDetailRes(order);
    }

    @Override
    public OrderDetailRes getOrderById(String id, String callerUserId, String callerRole) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "訂單不存在");
        }

        if ("ROLE_STORE_OWNER".equals(callerRole) || "ROLE_STORE_EDITOR".equals(callerRole)) {
            String storeId = resolveStoreIdForUser(callerUserId);
            if (storeId == null || !storeId.equals(order.getStoreId())) {
                throw new BusinessException("FORBIDDEN", "無權查看此訂單");
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
            throw new BusinessException("ORDER_NOT_FOUND", "訂單不存在");
        }
        if (!playerId.equals(order.getUserId())) {
            throw new BusinessException("FORBIDDEN", "無權查看此訂單");
        }
        return buildOrderDetailRes(order);
    }

    // ==================== 狀態變更 ====================

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

        String fromStatus = order.getStatus();
        order.setStatus(OrderStatusEnum.PREPARING.getCode());
        order.setPreparingAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKeySelective(order);

        recordStatusLog(orderId, fromStatus, OrderStatusEnum.PREPARING.getCode(),
                operatorId, "ADMIN", null);

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

        String fromStatus = order.getStatus();
        order.setStatus(OrderStatusEnum.SHIPPED.getCode());
        order.setTrackingNo(req.getTrackingNo());
        order.setRemark(req.getRemark());
        order.setShippedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKeySelective(order);

        recordStatusLog(orderId, fromStatus, OrderStatusEnum.SHIPPED.getCode(),
                operatorId, "ADMIN", "物流單號：" + req.getTrackingNo());

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

        String fromStatus = order.getStatus();
        order.setStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setCompletedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKeySelective(order);

        recordStatusLog(orderId, fromStatus, OrderStatusEnum.COMPLETED.getCode(),
                operatorId, operatorId != null ? "ADMIN" : "SYSTEM", null);

        log.info("✅ 訂單已完成");
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
        log.info("🔍 統一狀態更新：orderId={}, targetStatus={}", id, req.getTargetStatus());

        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "訂單不存在");
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

        String logRemark = req.getRemark();
        if (targetStatus == OrderStatusEnum.SHIPPED && isNotBlank(req.getTrackingNo())) {
            logRemark = "物流單號：" + req.getTrackingNo()
                    + (isNotBlank(req.getRemark()) ? "；" + req.getRemark() : "");
        }

        recordStatusLog(id, fromStatus, targetStatus.getCode(),
                operatorId, operatorType, logRemark);

        log.info("✅ 訂單狀態更新：{} → {}", fromStatus, targetStatus.getCode());
    }

    @Override
    @Transactional
    public void cancelOrder(String id, CancelOrderReq req, String operatorId, String operatorType) {
        log.info("🔍 取消訂單：orderId={}, operator={}", id, operatorId);

        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "訂單不存在");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (!canCancelByOperator(currentStatus, operatorType)) {
            throw new BusinessException("訂單狀態不允許取消（目前狀態：" + currentStatus.getName() + "）");
        }

        String fromStatus = order.getStatus();
        String cancelReason = req != null ? req.getCancelReason() : null;

        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelReason(cancelReason);
        order.setCancelledBy(operatorId);
        order.setCancelledAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateByPrimaryKeySelective(order);

        // 歸還賞品至賞品盒（AVAILABLE 狀態，解除 orderId 綁定）
        restorePrizeBoxes(id);

        // TODO:REFUND - 運費退款（待金流串接後實作）
        // 取消時若訂單已付款，需退還運費 order.getShippingFee() 至用戶錢包或原支付管道
        // 預計接口：paymentService.refundShipping(order.getPaymentNo(), order.getShippingFee())

        // TODO:INVOICE - 發票作廢（待電子發票串接後實作）
        // 若已開立發票，需呼叫電子發票 API 進行作廢
        // 預計接口：invoiceService.voidInvoice(order.getInvoiceNo())

        recordStatusLog(id, fromStatus, OrderStatusEnum.CANCELLED.getCode(),
                operatorId, operatorType,
                cancelReason != null ? "取消原因：" + cancelReason : null);

        log.info("✅ 訂單已取消（狀態：CANCELLED），獎品已歸還賞品盒");
    }

    @Override
    @Transactional
    public void submitShippingInfo(String orderId, ShipInfoReq req, String userId) {
        log.info("📦 提交出貨資訊：orderId={}, userId={}", orderId, userId);

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("訂單不存在");
        }

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("無權限操作此訂單");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getStatus());
        if (currentStatus != OrderStatusEnum.PAYMENT_PENDING) {
            throw new BusinessException("僅待付款訂單可修改出貨資訊");
        }

        String method = req.getShippingMethod();
        if ("HOME_DELIVERY".equals(method)) {
            if (isBlank(req.getRecipientName())) {
                throw new BusinessException("宅配需填入收件人姓名");
            }
            if (isBlank(req.getRecipientPhone())) {
                throw new BusinessException("宅配需填入收件人電話");
            }
            if (isBlank(req.getRecipientAddress())) {
                throw new BusinessException("宅配需填入收件地址");
            }
        } else if ("SEVEN_ELEVEN".equals(method) || "FAMILY_MART".equals(method)) {
            if (isBlank(req.getStoreCode())) {
                throw new BusinessException("超商取貨需填入分店代碼");
            }
            if (isBlank(req.getStoreName())) {
                throw new BusinessException("超商取貨需填入分店名稱");
            }
        } else {
            throw new BusinessException("不支援的配送方式");
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

        log.info("✅ 出貨資訊已更新");
    }

    @Override
    @Transactional
    public void handleShippingPaymentCallback(ShippingCallbackResult callbackResult) {
        if (callbackResult == null || isBlank(callbackResult.getOrderNumber())) {
            throw new BusinessException("付款回調資料不完整");
        }

        List<Order> orders = orderMapper.selectByGomypayTradeNo(callbackResult.getOrderNumber());
        if (orders.isEmpty()) {
            Order fallbackOrder = orderMapper.selectByOrderNumber(callbackResult.getOrderNumber());
            if (fallbackOrder != null) {
                orders = List.of(fallbackOrder);
            }
        }
        if (orders.isEmpty()) {
            throw new BusinessException("找不到對應訂單：" + callbackResult.getOrderNumber());
        }

        if (callbackResult.isSuccess()) {
            for (Order order : orders) {
                orderMapper.markShippingPaymentSuccess(order.getId(), null);

                if (OrderStatusEnum.PAYMENT_PENDING.getCode().equals(order.getStatus())
                        || OrderStatusEnum.PAYMENT_FAILED.getCode().equals(order.getStatus())) {
                    recordStatusLog(order.getId(), order.getStatus(), OrderStatusEnum.PENDING.getCode(),
                            null, "SYSTEM", "GoMyPay 付款成功");
                }
                log.info("✅ 運費付款成功：orderId={}, orderNo={}, gatewayOrderId={}",
                        order.getId(), order.getOrderNumber(), callbackResult.getGatewayTradeNo());
            }
            return;
        }

        String errorMessage = callbackResult.getErrorMessage();
        for (Order order : orders) {
            String fromStatus = order.getStatus();
            orderMapper.markShippingPaymentFailed(order.getId(), null, errorMessage);
            if (!OrderStatusEnum.PAYMENT_FAILED.getCode().equals(fromStatus)) {
                recordStatusLog(order.getId(), fromStatus, OrderStatusEnum.PAYMENT_FAILED.getCode(),
                        null, "SYSTEM", "GoMyPay 付款失敗");
            }
            log.warn("⚠️ 運費付款失敗：orderId={}, orderNo={}, reason={}",
                    order.getId(), order.getOrderNumber(), errorMessage);
        }
    }

    // ==================== 內部輔助方法 ====================

    private List<PrizeBox> validateAndLoadPrizeBoxes(String userId, List<String> prizeBoxIds) {
        List<PrizeBox> prizeBoxes = new ArrayList<>();
        for (String boxId : prizeBoxIds) {
            PrizeBox box = prizeBoxMapper.selectByPrimaryKey(boxId);
            if (box == null) {
                throw new BusinessException("賞品盒不存在：" + boxId);
            }
            if (!userId.equals(box.getUserId())) {
                throw new BusinessException("賞品盒不屬於當前玩家：" + boxId);
            }
            if (!isPrizeBoxAvailable(box.getStatus())) {
                throw new BusinessException("賞品盒狀態不允許出貨：" + boxId);
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
            throw new BusinessException("運送方式不存在");
        }
        if (!"ACTIVE".equals(shippingMethod.getStatus())) {
            throw new BusinessException("運送方式已停用");
        }
        return shippingMethod;
    }

    private ShippingPaymentResult initShippingPayment(List<Order> orders, Long shippingFee, ShippingMethod shippingMethod,
                                                      String userId, String paymentMethod) {
        if (orders == null || orders.isEmpty()) {
            return ShippingPaymentResult.builder().success(false).errorMessage("缺少訂單資訊").build();
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
                .buyerName(reqValue(orders.get(0).getRecipientName(), user != null ? user.getNickname() : null, "玩家"))
                .buyerEmail(user != null ? user.getEmail() : null)
                .buyerPhone(reqValue(orders.get(0).getRecipientPhone(), null, ""))
                .itemDescription("訂單運費 x" + orders.size() + " (" + shippingMethod.getName() + ")")
                .paymentMethod(paymentMethod)
                .build();

        ShippingPaymentResult paymentResult = shippingPaymentGatewayClient.createPayment(paymentRequest);

        if (paymentResult != null && paymentResult.isSuccess()) {
            for (Order order : orders) {
                Long orderShippingFee = order.getShippingFee() != null ? order.getShippingFee() : shippingFee;
                orderMapper.updatePaymentInit(order.getId(), orderShippingFee, paymentMethod,
                        PaymentStatusEnum.PAYMENT_PENDING.getCode(), OrderStatusEnum.PAYMENT_PENDING.getCode(),
                        paymentResult.getGatewayTradeNo());
            }
            return paymentResult;
        }

        String failedReason = paymentResult != null ? paymentResult.getErrorMessage() : "建立付款單失敗";
        for (Order order : orders) {
            Long orderShippingFee = order.getShippingFee() != null ? order.getShippingFee() : shippingFee;
            orderMapper.updatePaymentInit(order.getId(), orderShippingFee, paymentMethod,
                    PaymentStatusEnum.FAILED.getCode(), OrderStatusEnum.PAYMENT_FAILED.getCode(), null);
            orderMapper.markShippingPaymentFailed(order.getId(), null, failedReason);
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
            throw new BusinessException(
                    String.format("不允許的狀態轉換：%s → %s", from.getName(), to.getName()));
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
                    prizeBox.setStatus(PRIZE_BOX_STATUS_AVAILABLE);
                    prizeBox.setOrderId(null);
                    prizeBox.setShippedAt(null);
                    prizeBoxMapper.updateByPrimaryKey(prizeBox);
                    log.info("🔄 獎品已歸還賞品盒：prizeBoxId={}", prizeBox.getId());
                }
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
        return PRIZE_BOX_STATUS_AVAILABLE.equals(status) || PRIZE_BOX_STATUS_IN_BOX.equals(status);
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
    }

    // ==================== DTO 轉換 ====================

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
                .totalAmount(order.getShippingFee() != null ? order.getShippingFee() : SHIPPING_FEE)
                .shippingFee(order.getShippingFee() != null ? order.getShippingFee() : SHIPPING_FEE)
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
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
            case "HOME_DELIVERY" -> "宅配";
            case "SEVEN_ELEVEN" -> "7-11取貨";
            case "FAMILY_MART" -> "全家取貨";
            default -> method;
        };
    }

    @Override
    public List<StatusLogRes> getStatusLog(String orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND", "訂單不存在");
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
