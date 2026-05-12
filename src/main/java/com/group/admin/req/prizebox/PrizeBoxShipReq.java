package com.group.admin.req.prizebox;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 出貨請求（從賞品盒產生訂單）
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
public class PrizeBoxShipReq {
    
    /**
     * 要出貨的賞品盒項目 ID 列表
     */
    @NotEmpty(message = "請選擇要出貨的獎品")
    private List<String> prizeBoxIds;
    
    /**
     * 配送方式：HOME_DELIVERY/SEVEN_ELEVEN/FAMILY_MART
     */
    @NotBlank(message = "請選擇配送方式")
    private String shippingMethod;

    /** 運送方式 ID（對應 shipping_method.id） */
    private String shippingMethodId;

    /** 前端帶入運費（後端仍會以 DB 為準驗證） */
    private Long shippingFee;

    /** 付款方式：CREDIT_CARD / BANK_TRANSFER */
    private String paymentMethod;
    
    /**
     * 收件人姓名（若無 userAddressId 則必填）
     */
    private String recipientName;
    
    /**
     * 收件人電話（若無 userAddressId 則必填）
     */
    private String recipientPhone;
    
    /**
     * 收件地址（宅配必填）
     */
    private String recipientAddress;
    
    /**
     * 超商店號（超商取貨必填）
     */
    private String storeCode;
    
    /**
     * 超商店名（超商取貨必填）
     */
    private String storeName;
    
    /**
     * 超商地址（超商取貨必填）
     */
    private String storeAddress;
    
    /**
     * 備註
     */
    private String remark;

    /**
     * 已儲存地址 ID（選填，有值時優先於請求欄位）
     */
    private String userAddressId;
}
