package com.group.admin.req.prizebox;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 出貨請求（從獎品盒產生訂單）
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
public class PrizeBoxShipReq {
    
    /**
     * 要出貨的獎品盒項目 ID 列表
     */
    @NotEmpty(message = "請選擇要出貨的獎品")
    private List<String> prizeBoxIds;
    
    /**
     * 配送方式：HOME_DELIVERY/SEVEN_ELEVEN/FAMILY_MART
     */
    @NotBlank(message = "請選擇配送方式")
    private String shippingMethod;
    
    /**
     * 收件人姓名
     */
    @NotBlank(message = "收件人姓名不可為空")
    private String recipientName;
    
    /**
     * 收件人電話
     */
    @NotBlank(message = "收件人電話不可為空")
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
     * 使用者常用地址 ID（選填，用於快速填入收件資訊）
     */
    private String userAddressId;
}
