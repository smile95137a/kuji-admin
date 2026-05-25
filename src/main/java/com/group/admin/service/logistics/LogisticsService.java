package com.group.admin.service.logistics;

import java.util.List;

public interface LogisticsService {

    ShippingResult createShipment(String orderId, ShippingInfo info);

    ShippingResult queryShipment(String trackingNumber);

    List<ConvenienceStore> queryStores(String type, String city, String keyword);

    String createStoreSelectorUrl(String shippingMethodCode, String returnUrl);
}
