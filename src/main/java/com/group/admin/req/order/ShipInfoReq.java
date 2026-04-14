package com.group.admin.req.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 出貨資訊提交請求 DTO
 * 用於玩家提交或更新訂單出貨資訊（POST /order/{orderId}/shipping-info）
 *
 * @author Kuji Admin
 * @since 2026-03-22
 */
@Data
public class ShipInfoReq {

    /**
     * 配送方式代碼（必填）
     * 有效值：HOME_DELIVERY / SEVEN_ELEVEN / FAMILY_MART
     */
    @NotBlank(message = "出貨方式不可為空")
    private String shippingMethod;

    /**
     * 收件人姓名（HOME_DELIVERY 時必填）
     */
    private String recipientName;

    /**
     * 收件人電話（HOME_DELIVERY 時必填）
     */
    private String recipientPhone;

    /**
     * 收件地址（HOME_DELIVERY 時必填）
     */
    private String recipientAddress;

    /**
     * 超商分店代碼（SEVEN_ELEVEN / FAMILY_MART 時必填）
     */
    private String storeCode;

    /**
     * 超商分店名稱（SEVEN_ELEVEN / FAMILY_MART 時必填）
     */
    private String storeName;

    /**
     * 超商分店地址（可選）
     */
    private String storeAddress;

    /**
     * 備註（可選）
     */
    private String remark;
}
