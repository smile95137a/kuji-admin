package com.group.admin.res.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 前台會員資訊回應
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@Schema(description = "前台會員資訊")
public class FrontendUserRes {
    
    @Schema(description = "會員 ID")
    private String id;
    
    @Schema(description = "Email")
    private String email;
    
    @Schema(description = "暱稱")
    private String nickname;
    
    @Schema(description = "頭像 URL")
    private String avatar;
    
    @Schema(description = "登入方式（LOCAL/GOOGLE/FACEBOOK/LINE）")
    private String provider;
    
    @Schema(description = "第三方登入 ID")
    private String providerId;
    
    @Schema(description = "金幣餘額")
    private Long goldCoins;
    
    @Schema(description = "紅利幣餘額")
    private Long bonusCoins;
    
    @Schema(description = "狀態（ACTIVE/INACTIVE/SUSPENDED）")
    private String status;
    
    @Schema(description = "狀態名稱")
    private String statusName;
    
    @Schema(description = "Email 是否已驗證")
    private Boolean emailVerified;
    
    @Schema(description = "手機號碼")
    private String phoneNumber;
    
    @Schema(description = "最後登入時間")
    private LocalDateTime lastLoginAt;
    
    @Schema(description = "註冊時間")
    private LocalDateTime createdAt;
    
    @Schema(description = "最後更新時間")
    private LocalDateTime updatedAt;
}
