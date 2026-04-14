package com.group.admin.service.logistics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 物流 Stub 實作 — 記錄日誌但不真正出貨。
 * 未來替換為綠界 ECLogistics 時注入此介面的正式實作即可。
 */
@Slf4j
@Service
public class StubLogisticsServiceImpl implements LogisticsService {

    @Override
    public ShippingResult createShipment(String orderId, ShippingInfo info) {
        String tracking = "STUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[STUB] 建立出貨：orderId={}，recipient={}，tracking={}", orderId, info.getRecipientName(), tracking);
        return ShippingResult.builder()
                .success(true)
                .trackingNumber(tracking)
                .message("[STUB] 出貨單建立成功")
                .build();
    }

    @Override
    public ShippingResult queryShipment(String trackingNumber) {
        log.info("[STUB] 查詢物流：tracking={}", trackingNumber);
        return ShippingResult.builder()
                .success(true)
                .trackingNumber(trackingNumber)
                .message("[STUB] 物流狀態：配送中")
                .build();
    }

    @Override
    public List<ConvenienceStore> queryStores(String type, String city, String keyword) {
        log.info("[STUB] 查詢超商門市：type={}，city={}，keyword={}", type, city, keyword);
        return Collections.emptyList();
    }
}
