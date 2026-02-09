package com.group.admin.req.contact;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 合作諮詢查詢條件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "合作諮詢查詢條件")
public class ContactInquiryCondition extends BaseCondition {
    
    @Schema(description = "公司名稱（模糊查詢）", example = "XX科技")
    private String companyName;
    
    @Schema(description = "處理狀態（PENDING/PROCESSING/COMPLETED/REJECTED）", example = "PENDING")
    private String status;
    
    @Schema(description = "合作類型", example = "IP_LICENSE")
    private String cooperationType;
}
