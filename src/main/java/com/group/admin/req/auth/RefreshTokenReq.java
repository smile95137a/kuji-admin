package com.group.admin.req.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Token 刷新請求
 * 
 * <p>使用 Refresh Token 換取新的 Access Token</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "Token 刷新請求")
public class RefreshTokenReq {

    /**
     * Refresh Token
     */
    @NotBlank(message = "Refresh Token 不可為空")
    @Schema(description = "Refresh Token", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}
