package com.group.admin.req.order;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 訂單出貨請求
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
public class OrderShipReq {
    
    /**
     * 物流單號
     */
    @NotBlank(message = "物流單號不可為空")
    private String trackingNo;
    
    /**
     * 備註
     */
    private String remark;
}
