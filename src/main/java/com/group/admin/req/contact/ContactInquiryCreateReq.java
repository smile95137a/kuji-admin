package com.group.admin.req.contact;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 合作諮詢提交請求（前台使用）
 */
@Data
@Schema(description = "合作諮詢提交請求")
public class ContactInquiryCreateReq {
    
    @NotBlank(message = "公司名稱不能為空")
    @Schema(description = "公司名稱", example = "XX科技有限公司", required = true)
    private String companyName;
    
    @NotBlank(message = "聯絡人姓名不能為空")
    @Schema(description = "聯絡人姓名", example = "王大明", required = true)
    private String contactName;
    
    @NotBlank(message = "電子信箱不能為空")
    @Email(message = "電子信箱格式不正確")
    @Schema(description = "電子信箱", example = "contact@company.com", required = true)
    private String email;
    
    @Schema(description = "連絡電話", example = "02-12345678")
    private String phone;
    
    @Schema(description = "合作類型", example = "IP_LICENSE")
    private String cooperationType;
    
    @Schema(description = "需求簡述", example = "我們希望合作推出聯名一番賞商品...")
    private String description;
}
