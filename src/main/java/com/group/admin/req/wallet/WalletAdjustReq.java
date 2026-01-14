package com.group.admin.req.wallet;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 手動調整玩家點數請求（僅 Admin）
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
public class WalletAdjustReq {
    
    /**
     * 玩家 ID
     */
    @NotBlank(message = "玩家 ID 不可為空")
    private String userId;
    
    /**
     * 幣種：GOLD/BONUS
     */
    @NotBlank(message = "幣種不可為空")
    private String coinType;
    
    /**
     * 調整金額（正數=增加，負數=減少）
     */
    @NotNull(message = "調整金額不可為空")
    private Long amount;
    
    /**
     * 調整原因（選填，供後續查核）
     */
    private String reason;
}
