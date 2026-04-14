package com.group.admin.res.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 玩家金幣資訊回應
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCoinRes {
    
    /**
     * ID
     */
    private String id;
    
    /**
     * 玩家 ID
     */
    private String userId;
    
    /**
     * 玩家暱稱（冗餘，方便顯示）
     */
    private String userNickname;
    
    /**
     * 玩家 Email（冗餘，方便顯示）
     */
    private String userEmail;
    
    /**
     * 金幣（儲值金）餘額
     */
    private Long goldCoins;
    
    /**
     * 紅利幣餘額
     */
    private Long bonusCoins;
    
    /**
     * 累計儲值金額（台幣）
     */
    private Long totalRecharged;
    
    /**
     * 建立時間
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新時間
     */
    private LocalDateTime updatedAt;
}
