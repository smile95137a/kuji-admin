package com.group.admin.req.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 後台登入請求
 * 
 * <p>用於 Admin/StoreOwner/StoreEditor 登入</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "後台登入請求")
public class AdminLoginReq {

    /**
     * 登入帳號（Email）
     */
    @NotBlank(message = "帳號不可為空")
    @Schema(description = "登入帳號（Email）", example = "admin@kuji.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /**
     * 密碼
     */
    @NotBlank(message = "密碼不可為空")
    @Schema(description = "密碼", example = "Admin123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
