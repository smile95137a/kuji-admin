package com.group.admin.service;

import com.group.admin.entity.Order;
import com.group.admin.enums.OrderStatusEnum;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.*;
import com.group.admin.repository.OrderRepository;
import com.group.admin.req.order.OrderCancelReq;
import com.group.admin.req.order.OrderShipReq;
import com.group.admin.req.order.ShipInfoReq;
import com.group.admin.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrderServiceImpl 單元測試（002-express-shipping）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 單元測試")
class OrderServiceTest {

    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private OrderStatusLogMapper orderStatusLogMapper;
    @Mock private PrizeBoxMapper prizeBoxMapper;
    @Mock private LotteryMapper lotteryMapper;
    @Mock private LotteryPrizeMapper lotteryPrizeMapper;
    @Mock private StoreMapper storeMapper;
    @Mock private UserMapper userMapper;
    @Mock private OrderRepository orderRepository;
    @Mock private ConsumptionRecordService consumptionRecordService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    // ── helper ──────────────────────────────────────────────
    private Order createOrder(String id, String userId, String statusCode) {
        Order order = new Order();
        order.setId(id);
        order.setUserId(userId);
        order.setStatus(statusCode);
        return order;
    }

    // ═══════════════════════════════════════════════════════
    //  submitShippingInfo
    // ═══════════════════════════════════════════════════════
    @Nested
    @DisplayName("submitShippingInfo")
    class SubmitShippingInfoTests {

        @Test
        @DisplayName("宅配 - 成功更新出貨資訊")
        void submitShippingInfo_homeDelivery_success() {
            Order order = createOrder("order-1", "user-1", OrderStatusEnum.PENDING.getCode());
            when(orderMapper.selectByPrimaryKey("order-1")).thenReturn(order);

            ShipInfoReq req = new ShipInfoReq();
            req.setShippingMethod("HOME_DELIVERY");
            req.setRecipientName("王小明");
            req.setRecipientPhone("0912345678");
            req.setRecipientAddress("台北市信義區信義路五段7號");

            orderService.submitShippingInfo("order-1", req, "user-1");

            verify(orderMapper).updateByPrimaryKey(orderCaptor.capture());
            Order updated = orderCaptor.getValue();
            assertEquals("HOME_DELIVERY", updated.getShippingMethod());
            assertEquals("王小明", updated.getRecipientName());
            assertEquals("0912345678", updated.getRecipientPhone());
            assertEquals("台北市信義區信義路五段7號", updated.getRecipientAddress());
        }

        @Test
        @DisplayName("超商取貨（7-11）- 成功更新出貨資訊")
        void submitShippingInfo_sevenEleven_success() {
            Order order = createOrder("order-1", "user-1", OrderStatusEnum.PENDING.getCode());
            when(orderMapper.selectByPrimaryKey("order-1")).thenReturn(order);

            ShipInfoReq req = new ShipInfoReq();
            req.setShippingMethod("SEVEN_ELEVEN");
            req.setStoreCode("167890");
            req.setStoreName("信義門市");

            orderService.submitShippingInfo("order-1", req, "user-1");

            verify(orderMapper).updateByPrimaryKey(orderCaptor.capture());
            Order updated = orderCaptor.getValue();
            assertEquals("SEVEN_ELEVEN", updated.getShippingMethod());
            assertEquals("167890", updated.getStoreCode());
            assertEquals("信義門市", updated.getStoreName());
        }

        @Test
        @DisplayName("PREPARING 狀態 → 拒絕修改")
        void submitShippingInfo_preparingStatus_throwsException() {
            Order order = createOrder("order-1", "user-1", OrderStatusEnum.PREPARING.getCode());
            when(orderMapper.selectByPrimaryKey("order-1")).thenReturn(order);

            ShipInfoReq req = new ShipInfoReq();
            req.setShippingMethod("HOME_DELIVERY");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.submitShippingInfo("order-1", req, "user-1"));
            assertEquals("訂單已確認，無法修改出貨資訊", ex.getMessage());
            verify(orderMapper, never()).updateByPrimaryKey(any());
        }

        @Test
        @DisplayName("他人訂單 → 權限不足")
        void submitShippingInfo_otherUserOrder_throwsException() {
            Order order = createOrder("order-1", "user-A", OrderStatusEnum.PENDING.getCode());
            when(orderMapper.selectByPrimaryKey("order-1")).thenReturn(order);

            ShipInfoReq req = new ShipInfoReq();
            req.setShippingMethod("HOME_DELIVERY");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.submitShippingInfo("order-1", req, "user-B"));
            assertEquals("無權限操作此訂單", ex.getMessage());
            verify(orderMapper, never()).updateByPrimaryKey(any());
        }

        @Test
        @DisplayName("宅配缺少收件人姓名 → 驗證失敗")
        void submitShippingInfo_homeDelivery_missingName_throwsException() {
            Order order = createOrder("order-1", "user-1", OrderStatusEnum.PENDING.getCode());
            when(orderMapper.selectByPrimaryKey("order-1")).thenReturn(order);

            ShipInfoReq req = new ShipInfoReq();
            req.setShippingMethod("HOME_DELIVERY");
            req.setRecipientPhone("0912345678");
            req.setRecipientAddress("台北市");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.submitShippingInfo("order-1", req, "user-1"));
            assertEquals("宅配需填入收件人姓名", ex.getMessage());
        }

        @Test
        @DisplayName("超商取貨缺少分店代碼 → 驗證失敗")
        void submitShippingInfo_sevenEleven_missingStoreCode_throwsException() {
            Order order = createOrder("order-1", "user-1", OrderStatusEnum.PENDING.getCode());
            when(orderMapper.selectByPrimaryKey("order-1")).thenReturn(order);

            ShipInfoReq req = new ShipInfoReq();
            req.setShippingMethod("SEVEN_ELEVEN");
            req.setStoreName("信義門市");

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.submitShippingInfo("order-1", req, "user-1"));
            assertEquals("超商取貨需填入分店代碼", ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    //  prepareShipping / ship / complete
    // ═══════════════════════════════════════════════════════
    @Nested
    @DisplayName("訂單狀態流轉")
    class StatusTransitionTests {

        @Test
        @DisplayName("prepareShipping - PENDING → PREPARING")
        void prepareShipping_success() {
            Order order = createOrder("order-1", "user-1", OrderStatusEnum.PENDING.getCode());
            when(orderMapper.selectByPrimaryKey("order-1")).thenReturn(order);

            orderService.prepareShipping("order-1", "admin-1");

            verify(orderMapper).updateByPrimaryKey(orderCaptor.capture());
            assertEquals(OrderStatusEnum.PREPARING.getCode(), orderCaptor.getValue().getStatus());
            verify(orderStatusLogMapper).insert(any());
        }

        @Test
        @DisplayName("ship - PREPARING → SHIPPED，設定物流單號")
        void ship_success() {
            Order order = createOrder("order-1", "user-1", OrderStatusEnum.PREPARING.getCode());
            when(orderMapper.selectByPrimaryKey("order-1")).thenReturn(order);

            OrderShipReq req = new OrderShipReq();
            req.setTrackingNo("TRACK123456");
            req.setRemark("請小心搬運");

            orderService.ship("order-1", req, "admin-1");

            verify(orderMapper).updateByPrimaryKey(orderCaptor.capture());
            Order updated = orderCaptor.getValue();
            assertEquals(OrderStatusEnum.SHIPPED.getCode(), updated.getStatus());
            assertEquals("TRACK123456", updated.getTrackingNo());
            verify(orderStatusLogMapper).insert(any());
        }

        @Test
        @DisplayName("complete - SHIPPED → COMPLETED")
        void complete_success() {
            Order order = createOrder("order-1", "user-1", OrderStatusEnum.SHIPPED.getCode());
            when(orderMapper.selectByPrimaryKey("order-1")).thenReturn(order);

            orderService.complete("order-1", "admin-1");

            verify(orderMapper).updateByPrimaryKey(orderCaptor.capture());
            assertEquals(OrderStatusEnum.COMPLETED.getCode(), orderCaptor.getValue().getStatus());
            verify(orderStatusLogMapper).insert(any());
        }

        @Test
        @DisplayName("prepareShipping - SHIPPED 狀態 → 拒絕操作")
        void prepareShipping_wrongStatus_throwsException() {
            Order order = createOrder("order-1", "user-1", OrderStatusEnum.SHIPPED.getCode());
            when(orderMapper.selectByPrimaryKey("order-1")).thenReturn(order);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> orderService.prepareShipping("order-1", "admin-1"));
            assertEquals("訂單狀態不允許此操作", ex.getMessage());
            verify(orderMapper, never()).updateByPrimaryKey(any());
        }
    }
}
