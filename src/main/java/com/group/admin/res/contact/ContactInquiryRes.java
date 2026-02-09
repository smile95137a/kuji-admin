package com.group.admin.res.contact;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 合作諮詢回應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "合作諮詢回應")
public class ContactInquiryRes {
    
    @Schema(description = "諮詢 ID")
    private String id;
    
    @Schema(description = "公司名稱")
    private String companyName;
    
    @Schema(description = "聯絡人姓名")
    private String contactName;
    
    @Schema(description = "電子信箱")
    private String email;
    
    @Schema(description = "連絡電話")
    private String phone;
    
    @Schema(description = "合作類型")
    private String cooperationType;
    
    @Schema(description = "合作類型中文")
    private String cooperationTypeName;
    
    @Schema(description = "需求簡述")
    private String description;
    
    @Schema(description = "處理狀態")
    private String status;
    
    @Schema(description = "處理狀態中文")
    private String statusName;
    
    @Schema(description = "後台備註")
    private String remark;
    
    @Schema(description = "處理人 ID")
    private String processedBy;
    
    @Schema(description = "處理時間")
    private LocalDateTime processedAt;
    
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新時間")
    private LocalDateTime updatedAt;
}
