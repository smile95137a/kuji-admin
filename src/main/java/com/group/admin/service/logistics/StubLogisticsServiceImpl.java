package com.group.admin.service.logistics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 物流 Stub 實作，供尚未接正式物流環境時使用。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "logistics.provider", havingValue = "stub", matchIfMissing = true)
public class StubLogisticsServiceImpl implements LogisticsService {

    @Override
    public ShippingResult createShipment(String orderId, ShippingInfo info) {
        String tracking = "STUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[STUB] 建立出貨單 orderId={}, recipient={}, tracking={}",
                orderId,
                info != null ? info.getRecipientName() : null,
                tracking);
        ShippingResult result = new ShippingResult();
        result.setSuccess(true);
        result.setTrackingNumber(tracking);
        result.setProviderOrderNo(tracking);
        result.setProvider("STUB");
        result.setMessage("[STUB] 出貨單建立成功");
        return result;
    }

    @Override
    public ShippingResult queryShipment(String trackingNumber) {
        log.info("[STUB] 查詢物流狀態 tracking={}", trackingNumber);
        ShippingResult result = new ShippingResult();
        result.setSuccess(true);
        result.setTrackingNumber(trackingNumber);
        result.setProvider("STUB");
        result.setMessage("[STUB] 物流狀態查詢成功");
        return result;
    }

    @Override
    public List<ConvenienceStore> queryStores(String type, String city, String keyword) {
        log.info("[STUB] 查詢超商門市 type={}, city={}, keyword={}", type, city, keyword);
        return Collections.emptyList();
    }

    @Override
    public String createStoreSelectorUrl(String shippingMethodCode, String returnUrl) {
        log.info("[STUB] create store selector url shippingMethod={}, returnUrl={}", shippingMethodCode, returnUrl);
        return returnUrl;
    }
}
