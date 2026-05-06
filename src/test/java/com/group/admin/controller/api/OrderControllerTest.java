package com.group.admin.controller.api;

import com.group.admin.BaseControllerTest;
import com.group.admin.condition.OrderCondition;
import com.group.admin.exception.BusinessException;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.order.ShipInfoReq;
import com.group.admin.res.PageResult;
import com.group.admin.res.order.OrderDetailRes;
import com.group.admin.res.order.OrderRes;
import com.group.admin.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OrderController (前台) MockMvc 切片測試
 * 覆蓋出貨資訊提交（US1）及玩家查詢訂單（US3）
 */
@DisplayName("前台 OrderController 測試")
class OrderControllerTest extends BaseControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private static final String ORDER_ID = "order-uuid-001";
    private static final String USER_ID = "user-uuid-001";
    private static final String OTHER_USER_ID = "user-uuid-002";

    @BeforeEach
    void setUp() {
        setupMockMvcWithExceptionHandler(orderController);
    }

    // ===== T008: HOME_DELIVERY 正常路徑 → 200 =====

    @Test
    @DisplayName("T008: POST /order/{id}/shipping-info — HOME_DELIVERY 正常路徑 → 200")
    void submitShippingInfo_HomeDelivery_HappyPath_Returns200() throws Exception {
        setupAuthentication(USER_ID, "USER");
        doNothing().when(orderService).submitShippingInfo(eq(ORDER_ID), any(ShipInfoReq.class), eq(USER_ID));

        ShipInfoReq req = new ShipInfoReq();
        req.setShippingMethod("HOME_DELIVERY");
        req.setRecipientName("王小明");
        req.setRecipientPhone("0912345678");
        req.setRecipientAddress("台北市信義區信義路五段7號");

        mockMvc.perform(post("/order/{id}/shipping-info", ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk());

        verify(orderService).submitShippingInfo(eq(ORDER_ID), any(ShipInfoReq.class), eq(USER_ID));
    }

    // ===== T009: SEVEN_ELEVEN 正常路徑 → 200 =====

    @Test
    @DisplayName("T009: POST /order/{id}/shipping-info — SEVEN_ELEVEN 正常路徑 → 200")
    void submitShippingInfo_SevenEleven_HappyPath_Returns200() throws Exception {
        setupAuthentication(USER_ID, "USER");
        doNothing().when(orderService).submitShippingInfo(eq(ORDER_ID), any(ShipInfoReq.class), eq(USER_ID));

        ShipInfoReq req = new ShipInfoReq();
        req.setShippingMethod("SEVEN_ELEVEN");
        req.setStoreCode("ABC123");
        req.setStoreName("統一超商信義門市");

        mockMvc.perform(post("/order/{id}/shipping-info", ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk());

        verify(orderService).submitShippingInfo(eq(ORDER_ID), any(ShipInfoReq.class), eq(USER_ID));
    }

    // ===== T010: HOME_DELIVERY 缺少 recipientName → 400 =====

    @Test
    @DisplayName("T010: HOME_DELIVERY 缺少 recipientName → 400 含錯誤訊息")
    void submitShippingInfo_HomeDeliveryMissingName_Returns400() throws Exception {
        setupAuthentication(USER_ID, "USER");
        doThrow(new BusinessException("宅配需填入收件人姓名"))
                .when(orderService).submitShippingInfo(eq(ORDER_ID), any(ShipInfoReq.class), eq(USER_ID));

        ShipInfoReq req = new ShipInfoReq();
        req.setShippingMethod("HOME_DELIVERY");
        req.setRecipientPhone("0912345678");
        req.setRecipientAddress("台北市信義區");

        mockMvc.perform(post("/order/{id}/shipping-info", ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("宅配需填入收件人姓名"));
    }

    // ===== T011: SEVEN_ELEVEN 缺少 storeCode → 400 =====

    @Test
    @DisplayName("T011: SEVEN_ELEVEN 缺少 storeCode → 400 含錯誤訊息")
    void submitShippingInfo_SevenElevenMissingStoreCode_Returns400() throws Exception {
        setupAuthentication(USER_ID, "USER");
        doThrow(new BusinessException("超商取貨需填入分店代碼"))
                .when(orderService).submitShippingInfo(eq(ORDER_ID), any(ShipInfoReq.class), eq(USER_ID));

        ShipInfoReq req = new ShipInfoReq();
        req.setShippingMethod("SEVEN_ELEVEN");
        req.setStoreName("統一超商信義門市");

        mockMvc.perform(post("/order/{id}/shipping-info", ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("超商取貨需填入分店代碼"));
    }

    // ===== T012: PREPARING 狀態 → 409 =====

    @Test
    @DisplayName("T012: PREPARING 狀態提交出貨資訊 → 409")
    void submitShippingInfo_PreparingStatus_Returns409() throws Exception {
        setupAuthentication(USER_ID, "USER");
        doThrow(new BusinessException("ORDER_STATUS_CONFLICT", "訂單已確認，無法修改出貨資訊"))
                .when(orderService).submitShippingInfo(eq(ORDER_ID), any(ShipInfoReq.class), eq(USER_ID));

        ShipInfoReq req = new ShipInfoReq();
        req.setShippingMethod("HOME_DELIVERY");
        req.setRecipientName("王小明");
        req.setRecipientPhone("0912345678");
        req.setRecipientAddress("台北市信義區");

        mockMvc.perform(post("/order/{id}/shipping-info", ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("訂單已確認，無法修改出貨資訊"));
    }

    // ===== T013: 他人訂單 → 403 =====

    @Test
    @DisplayName("T013: 存取他人訂單 → 403")
    void submitShippingInfo_OtherUserOrder_Returns403() throws Exception {
        setupAuthentication(OTHER_USER_ID, "USER");
        doThrow(new BusinessException("ORDER_ACCESS_DENIED", "無權限操作此訂單"))
                .when(orderService).submitShippingInfo(eq(ORDER_ID), any(ShipInfoReq.class), eq(OTHER_USER_ID));

        ShipInfoReq req = new ShipInfoReq();
        req.setShippingMethod("HOME_DELIVERY");
        req.setRecipientName("王小明");
        req.setRecipientPhone("0912345678");
        req.setRecipientAddress("台北市信義區");

        mockMvc.perform(post("/order/{id}/shipping-info", ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("無權限操作此訂單"));
    }

    // ===== T022: GET /order/{id} 訂單所有者正常路徑 → 200 =====

    @Test
    @DisplayName("T022: GET /order/{id} 訂單所有者 → 200，含出貨相關欄位")
    void getOrderDetail_AsOwner_Returns200WithShippingFields() throws Exception {
        setupAuthentication(USER_ID, "USER");

        OrderDetailRes detail = OrderDetailRes.builder()
                .id(ORDER_ID)
                .orderNo("ORD20260101000001")
                .userId(USER_ID)
                .shippingMethod("HOME_DELIVERY")
                .shippingStatus("PENDING")
                .recipientName("王小明")
                .trackingNo(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderService.getOrderDetail(ORDER_ID)).thenReturn(detail);

        mockMvc.perform(get("/order/{id}", ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ORDER_ID))
                .andExpect(jsonPath("$.shippingMethod").value("HOME_DELIVERY"))
                .andExpect(jsonPath("$.shippingStatus").value("PENDING"))
                .andExpect(jsonPath("$.recipientName").value("王小明"));
    }

    // ===== T023: GET /order/{id} 非所有者 → 403 =====

    @Test
    @DisplayName("T023: GET /order/{id} 非訂單所有者 → 403")
    void getOrderDetail_NotOwner_Returns403() throws Exception {
        setupAuthentication(OTHER_USER_ID, "USER");

        OrderDetailRes detail = OrderDetailRes.builder()
                .id(ORDER_ID)
                .userId(USER_ID)  // owned by USER_ID, not OTHER_USER_ID
                .build();

        when(orderService.getOrderDetail(ORDER_ID)).thenReturn(detail);

        mockMvc.perform(get("/order/{id}", ORDER_ID))
                .andExpect(status().isForbidden());
    }

    // ===== T024: POST /order/list 使用者隔離性 =====

    @Test
    @DisplayName("T024: POST /order/list — 強制 userId 篩選，只回傳當前使用者訂單")
    void getMyOrders_EnforcesUserIdFilter() throws Exception {
        setupAuthentication(USER_ID, "USER");

        OrderRes userOrder = OrderRes.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .shippingStatus("PENDING")
                .build();

        PageResult<OrderRes> orders = PageResult.of(1, 10, 1L, List.of(userOrder));
        when(orderService.getOrders(any(QueryReq.class))).thenReturn(orders);

        mockMvc.perform(post("/order/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userId").value(USER_ID));
    }

    // ===== T025: POST /order/list 依狀態篩選 =====

    @Test
    @DisplayName("T025: POST /order/list 依 status=SHIPPED 篩選 → 回傳清單狀態均為 SHIPPED")
    void getMyOrders_FilterByShippedStatus_ReturnsOnlyShipped() throws Exception {
        setupAuthentication(USER_ID, "USER");

        OrderRes shippedOrder = OrderRes.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .shippingStatus("SHIPPED")
                .build();

        PageResult<OrderRes> orders = PageResult.of(1, 10, 1L, List.of(shippedOrder));
        when(orderService.getOrders(any(QueryReq.class))).thenReturn(orders);

        QueryReq<OrderCondition> req = new QueryReq<>();
        OrderCondition cond = new OrderCondition();
        cond.setShippingStatus("SHIPPED");
        req.setCondition(cond);

        mockMvc.perform(post("/order/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].shippingStatus").value("SHIPPED"));
    }
}
