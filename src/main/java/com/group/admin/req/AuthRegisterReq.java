package com.group.admin.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 前台會員註冊請求
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "會員註冊請求")
public class AuthRegisterReq {
    
    /**
     * Email（必填）
     */
    @NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式不正確")
    @Schema(description = "Email", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
    
    /**
     * 密碼（必填）
     */
    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, max = 100, message = "密碼長度必須在 6-100 字元之間")
    @Schema(description = "密碼（至少 6 字元）", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
    
    /**
     * 暱稱（選填，預設為 email @ 前的部分）
     */
    @Size(max = 50, message = "暱稱不能超過 50 字元")
    @Schema(description = "暱稱", example = "小明")
    private String nickname;
    
    /**
     * 手機號碼（選填）
     */
    @Pattern(regexp = "^(09\\d{8})?$", message = "手機號碼格式不正確（應為 09 開頭的 10 碼數字）")
    @Schema(description = "手機號碼（台灣格式 09xxxxxxxx）", example = "0912345678")
    private String phoneNumber;
    
    /**
     * 頭像 URL（選填）
     */
    @Size(max = 500, message = "頭像 URL 不能超過 500 字元")
    @Schema(description = "頭像 URL", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    /**
     * 推薦碼（選填，用於推薦碼機制）
     */
    @Size(max = 20, message = "推薦碼不能超過 20 字元")
    @Schema(description = "推薦碼", example = "ABC123")
    private String referralCode;
}
