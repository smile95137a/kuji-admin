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
    
    @Schema(description = "手機號碼")
    private String phoneNumber;
    
    @Schema(description = "LINE ID")
    private String lineId;
    
    @Schema(description = "收件人姓名")
    private String recipientName;
    
    @Schema(description = "收件人電話")
    private String recipientPhone;
    
    @Schema(description = "城市")
    private String city;
    
    @Schema(description = "區域")
    private String district;
    
    @Schema(description = "詳細地址")
    private String addressDetail;
    
    @Schema(description = "發票類型：DUPLICATE/TRIPLICATE/CARRIER/DONATE")
    private String invoiceType;
    
    @Schema(description = "發票 Email")
    private String invoiceEmail;
    
    @Schema(description = "載具條碼")
    private String carrierCode;
    
    @Schema(description = "統一編號（三聯式發票用）")
    private String taxId;
    
    @Schema(description = "公司名稱（三聯式發票用）")
    private String companyName;
}
