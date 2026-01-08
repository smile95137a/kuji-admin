package com.group.admin.req.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 前台會員更新請求
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "前台會員更新請求")
public class FrontendUserUpdateReq {
    
    @Email(message = "Email 格式不正確")
    @Schema(description = "Email")
    private String email;
    
    @Schema(description = "暱稱")
    private String nickname;
    
    @Schema(description = "頭像 URL")
    private String avatar;
    
    @Schema(description = "狀態（ACTIVE/INACTIVE/SUSPENDED）")
    private String status;
    
    @Min(value = 0, message = "金幣不可為負數")
    @Schema(description = "金幣餘額")
    private Long goldCoins;
    
    @Min(value = 0, message = "紅利幣不可為負數")
    @Schema(description = "紅利幣餘額")
    private Long bonusCoins;
    
    @Schema(description = "手機號碼")
    private String phoneNumber;
    
    @Schema(description = "備註")
    private String remark;
}
