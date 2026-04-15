package com.group.admin.service;

import com.group.admin.entity.Order;
import com.group.admin.entity.OrderStatusLog;
import com.group.admin.enums.OrderStatusEnum;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.*;
import com.group.admin.repository.OrderRepository;
import com.group.admin.req.order.OrderShipReq;
import com.group.admin.req.order.ShipInfoReq;
import com.group.admin.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderService 單元測試
 * 覆蓋狀態機轉換、所有權守衛、出貨資訊提交守衛
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

    private static final String ORDER_ID = "order-uuid-001";
    private static final String OPERATOR_ID = "admin-uuid-001";
    private static final String USER_ID = "user-uuid-001";
    private static final String OTHER_USER_ID = "user-uuid-002";

    private Order buildOrder(OrderStatusEnum status) {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUserId(USER_ID);
        order.setStoreId("store-uuid-001");
        order.setStatus(status.getCode());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    // ===== T004: 狀態機正向路徑 =====

    @Test
    @DisplayName("T004-a: PENDING → PREPARING — 應更新狀態並寫入 OrderStatusLog")
    void prepareShipping_FromPending_ShouldUpdateStatusAndLogEntry() {
        Order order = buildOrder(OrderStatusEnum.PENDING);
        when(orderMapper.selectByPrimaryKey(ORDER_ID)).thenReturn(order);

        orderService.prepareShipping(ORDER_ID, OPERATOR_ID);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).updateByPrimaryKeySelective(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatusEnum.PREPARING.getCode());

        ArgumentCaptor<OrderStatusLog> logCaptor = ArgumentCaptor.forClass(OrderStatusLog.class);
        verify(orderStatusLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getToStatus()).isEqualTo(OrderStatusEnum.PREPARING.getCode());
        assertThat(logCaptor.getValue().getOperatorId()).isEqualTo(OPERATOR_ID);
    }

    @Test
    @DisplayName("T004-b: PREPARING → SHIPPED — 應更新狀態及 trackingNo，並寫入 OrderStatusLog")
    void ship_FromPreparing_ShouldUpdateStatusTrackingNoAndLogEntry() {
        Order order = buildOrder(OrderStatusEnum.PREPARING);
        when(orderMapper.selectByPrimaryKey(ORDER_ID)).thenReturn(order);

        OrderShipReq req = new OrderShipReq();
        req.setTrackingNo("TRACK-12345");
        orderService.ship(ORDER_ID, req, OPERATOR_ID);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).updateByPrimaryKeySelective(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatusEnum.SHIPPED.getCode());
        assertThat(captor.getValue().getTrackingNo()).isEqualTo("TRACK-12345");

        verify(orderStatusLogMapper).insert(argThat(log ->
                OrderStatusEnum.SHIPPED.getCode().equals(log.getToStatus())));
    }

    @Test
    @DisplayName("T004-c: SHIPPED → COMPLETED — 應更新狀態並寫入 OrderStatusLog")
    void complete_FromShipped_ShouldUpdateStatusAndLogEntry() {
        Order order = buildOrder(OrderStatusEnum.SHIPPED);
        when(orderMapper.selectByPrimaryKey(ORDER_ID)).thenReturn(order);

        orderService.complete(ORDER_ID, OPERATOR_ID);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderMapper).updateByPrimaryKeySelective(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatusEnum.COMPLETED.getCode());

        verify(orderStatusLogMapper).insert(argThat(log ->
                OrderStatusEnum.COMPLETED.getCode().equals(log.getToStatus())));
    }

    // ===== T005: 非法逆向轉換 =====

    @Test
    @DisplayName("T005: SHIPPED 狀態下呼叫 prepare → 應拋出 BusinessException")
    void prepareShipping_FromShipped_ShouldThrowBusinessException() {
        Order order = buildOrder(OrderStatusEnum.SHIPPED);
        when(orderMapper.selectByPrimaryKey(ORDER_ID)).thenReturn(order);

        assertThatThrownBy(() -> orderService.prepareShipping(ORDER_ID, OPERATOR_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("訂單狀態不允許此操作");
    }

    @Test
    @DisplayName("T005-b: COMPLETED 狀態下呼叫 ship → 應拋出 BusinessException")
    void ship_FromCompleted_ShouldThrowBusinessException() {
        Order order = buildOrder(OrderStatusEnum.COMPLETED);
        when(orderMapper.selectByPrimaryKey(ORDER_ID)).thenReturn(order);

        OrderShipReq req = new OrderShipReq();
        req.setTrackingNo("TRACK-XYZ");

        assertThatThrownBy(() -> orderService.ship(ORDER_ID, req, OPERATOR_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("訂單狀態不允許此操作");
    }

    // ===== T006: submitShippingInfo 在 PREPARING 狀態拋出例外 =====

    @Test
    @DisplayName("T006: PREPARING 狀態下提交出貨資訊 → 應拋出 BusinessException('訂單已確認，無法修改出貨資訊')")
    void submitShippingInfo_WhenPreparing_ShouldThrowConflictException() {
        Order order = buildOrder(OrderStatusEnum.PREPARING);
        when(orderMapper.selectByPrimaryKey(ORDER_ID)).thenReturn(order);

        ShipInfoReq req = new ShipInfoReq();
        req.setShippingMethod("HOME_DELIVERY");
        req.setRecipientName("王小明");
        req.setRecipientPhone("0912345678");
        req.setRecipientAddress("台北市信義區信義路五段7號");

        assertThatThrownBy(() -> orderService.submitShippingInfo(ORDER_ID, req, USER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("訂單已確認，無法修改出貨資訊");
    }

    // ===== T007: submitShippingInfo 在他人訂單上拋出例外 =====

    @Test
    @DisplayName("T007: 存取他人訂單提交出貨資訊 → 應拋出 BusinessException('無權限操作此訂單')")
    void submitShippingInfo_WhenNotOwner_ShouldThrowForbiddenException() {
        Order order = buildOrder(OrderStatusEnum.PENDING);
        // order.userId = USER_ID, but caller is OTHER_USER_ID
        when(orderMapper.selectByPrimaryKey(ORDER_ID)).thenReturn(order);

        ShipInfoReq req = new ShipInfoReq();
        req.setShippingMethod("HOME_DELIVERY");
        req.setRecipientName("王小明");
        req.setRecipientPhone("0912345678");
        req.setRecipientAddress("台北市信義區");

        assertThatThrownBy(() -> orderService.submitShippingInfo(ORDER_ID, req, OTHER_USER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("無權限操作此訂單");
    }

    // ===== Additional: validateShippingInfo =====

    @Test
    @DisplayName("HOME_DELIVERY 缺少 recipientName → 應拋出 BusinessException")
    void submitShippingInfo_HomeDeliveryMissingName_ShouldThrow() {
        Order order = buildOrder(OrderStatusEnum.PENDING);
        when(orderMapper.selectByPrimaryKey(ORDER_ID)).thenReturn(order);

        ShipInfoReq req = new ShipInfoReq();
        req.setShippingMethod("HOME_DELIVERY");
        req.setRecipientPhone("0912345678");
        req.setRecipientAddress("台北市信義區");

        assertThatThrownBy(() -> orderService.submitShippingInfo(ORDER_ID, req, USER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("宅配需填入收件人姓名");
    }

    @Test
    @DisplayName("SEVEN_ELEVEN 缺少 storeCode → 應拋出 BusinessException")
    void submitShippingInfo_SevenElevenMissingStoreCode_ShouldThrow() {
        Order order = buildOrder(OrderStatusEnum.PENDING);
        when(orderMapper.selectByPrimaryKey(ORDER_ID)).thenReturn(order);

        ShipInfoReq req = new ShipInfoReq();
        req.setShippingMethod("SEVEN_ELEVEN");
        req.setStoreName("統一超商信義門市");

        assertThatThrownBy(() -> orderService.submitShippingInfo(ORDER_ID, req, USER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("超商取貨需填入分店代碼");
    }

    @Test
    @DisplayName("HOME_DELIVERY 完整資訊 → 應成功呼叫 updateByPrimaryKeySelective")
    void submitShippingInfo_HomeDelivery_HappyPath_ShouldUpdate() {
        Order order = buildOrder(OrderStatusEnum.PENDING);
        when(orderMapper.selectByPrimaryKey(ORDER_ID)).thenReturn(order);

        ShipInfoReq req = new ShipInfoReq();
        req.setShippingMethod("HOME_DELIVERY");
        req.setRecipientName("王小明");
        req.setRecipientPhone("0912345678");
        req.setRecipientAddress("台北市信義區信義路五段7號");

        orderService.submitShippingInfo(ORDER_ID, req, USER_ID);

        verify(orderMapper).updateByPrimaryKey(argThat(o ->
                "HOME_DELIVERY".equals(o.getShippingMethod()) &&
                "王小明".equals(o.getRecipientName())));
    }
}
