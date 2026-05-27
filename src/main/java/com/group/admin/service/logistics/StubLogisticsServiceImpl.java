package com.group.admin.service.logistics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "logistics.provider", havingValue = "stub", matchIfMissing = true)
public class StubLogisticsServiceImpl implements LogisticsService {

    @Override
    public ShippingResult createShipment(String orderId, ShippingInfo info) {
        String tracking = "STUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[STUB] 建立物流單 orderId={}, recipient={}, tracking={}",
                orderId,
                info != null ? info.getRecipientName() : null,
                tracking);
        return ShippingResult.builder()
                .success(true)
                .trackingNumber(tracking)
                .providerOrderNo(tracking)
                .provider("STUB")
                .statusCode("CREATED")
                .statusName("已建立")
                .message("[STUB] 物流單建立成功")
                .build();
    }

    @Override
    public ShippingResult queryShipment(String trackingNumber) {
        log.info("[STUB] 查詢物流 tracking={}", trackingNumber);
        return ShippingResult.builder()
                .success(true)
                .trackingNumber(trackingNumber)
                .provider("STUB")
                .statusCode("IN_TRANSIT")
                .statusName("配送中")
                .message("[STUB] 物流狀態查詢成功")
                .build();
    }

    @Override
    public List<ConvenienceStore> queryStores(String type, String city, String keyword) {
        log.info("[STUB] 查詢門市 type={}, city={}, keyword={}", type, city, keyword);
        return Collections.emptyList();
    }

    @Override
    public String createStoreSelectorUrl(String shippingMethodCode, String returnUrl) {
        log.info("[STUB] create store selector url shippingMethod={}, returnUrl={}", shippingMethodCode, returnUrl);
        return returnUrl;
    }

    @Override
    public String handleStatusCallback(Map<String, String> params) {
        log.info("[STUB] logistics callback params={}", params);
        return "OK";
    }
}
