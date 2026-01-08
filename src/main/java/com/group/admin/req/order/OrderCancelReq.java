package com.group.admin.req.order;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 訂單取消請求
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
public class OrderCancelReq {
    
    /**
     * 取消原因
     */
    @NotBlank(message = "取消原因不可為空")
    private String reason;
}
