package com.group.admin.req.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 建立訂單請求（從獎品盒出貨）
 */
@Data
public class CreateOrderReq {

    @NotEmpty(message = "請選擇要出貨的獎品")
    private List<String> prizeBoxIds;

    @NotBlank(message = "請選擇配送方式")
    private String shippingMethod;

    @NotBlank(message = "收件人姓名不可為空")
    @Size(max = 100, message = "收件人姓名不可超過 100 字元")
    private String recipientName;

    @NotBlank(message = "收件人電話不可為空")
    private String recipientPhone;

    /** HOME_DELIVERY 時必填 */
    private String recipientAddress;

    /** SEVEN_ELEVEN / FAMILY_MART 時必填 */
    private String storeCode;
    private String storeName;
    private String storeAddress;
}
