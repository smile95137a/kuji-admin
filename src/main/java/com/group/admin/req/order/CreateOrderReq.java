package com.group.admin.req.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 玩家建立訂單請求
 */
@Data
public class CreateOrderReq {

    @NotEmpty(message = "請選擇至少一個賞品盒")
    private List<String> prizeBoxIds;

    @NotBlank(message = "請選擇配送方式")
    private String shippingMethod;

    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;

    private String storeCode;
    private String storeName;
    private String storeAddress;
}
