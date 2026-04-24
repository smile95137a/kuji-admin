package com.group.admin.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
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
    @Schema(description = "Email", example = "a0930200677@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
    
    /**
     * 密碼（必填）
     */
    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, max = 100, message = "密碼長度必須在 6-100 字元之間")
    @Schema(description = "密碼（至少 6 字元）", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
    
    /**
     * 確認密碼（必填）
     */
    @Schema(description = "確認密碼", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;
    
    /**
     * 暱稱（必填）
     */
    @Size(max = 50, message = "暱稱不能超過 50 字元")
    @Schema(description = "暱稱", example = "robin", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonAlias("username")
    private String nickname;
    
    /**
     * 手機號碼（必填）
     */
    @Pattern(regexp = "^09\\d{8}$", message = "手機號碼格式不正確（應為 09 開頭的 10 碼數字）")
    @Schema(description = "手機號碼（台灣格式 09xxxxxxxx）", example = "0930200677", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonAlias("phone")
    private String phoneNumber;
    
    /**
     * 收件人姓名（必填）
     */
    @Size(max = 50, message = "收件人姓名不能超過 50 字元")
    @Schema(description = "收件人姓名", example = "王", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonAlias("recipientName")
    private String addressName;
    
    /**
     * 郵遞區號（必填）
     */
    @Pattern(regexp = "^\\d{3,5}$", message = "郵遞區號格式不正確")
    @Schema(description = "郵遞區號", example = "103", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonAlias("postalCode")
    private String zipCode;
    
    /**
     * 縣市（必填）
     */
    @Schema(description = "縣市", example = "臺北市", requiredMode = Schema.RequiredMode.REQUIRED)
    private String city;
    
    /**
     * 區域（必填）
     */
    @Schema(description = "區域", example = "大同區", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonAlias("district")
    private String area;
    
    /**
     * 詳細地址（必填）
     */
    @Size(max = 200, message = "詳細地址不能超過 200 字元")
    @Schema(description = "詳細地址", example = "地址地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;
    
    /**
     * LINE ID（選填）
     */
    @Size(max = 100, message = "LINE ID 不能超過 100 字元")
    @Schema(description = "LINE ID", example = "tobinline")
    private String lineId;
    
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
    @Schema(description = "推薦碼", example = "robinRobin")
    private String referralCode;
    
    /**
     * 同意服務條款（必填）
     */
    @AssertTrue(message = "必須同意服務條款")
    @Schema(description = "同意服務條款", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean agreeTerms = Boolean.TRUE;

    public String getConfirmedPassword() {
        return (confirmPassword == null || confirmPassword.isBlank()) ? password : confirmPassword;
    }
}
