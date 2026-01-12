package com.group.admin.res.address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收件地址回應
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "收件地址回應")
public class UserAddressRes {
    
    /**
     * 主鍵 ID
     */
    @Schema(description = "地址 ID")
    private String id;
    
    /**
     * 使用者 ID
     */
    @Schema(description = "使用者 ID")
    private String userId;
    
    /**
     * 地址標籤（如：家、公司）
     */
    @Schema(description = "地址標籤")
    private String label;
    
    /**
     * 收件人姓名
     */
    @Schema(description = "收件人姓名")
    private String recipientName;
    
    /**
     * 收件人電話
     */
    @Schema(description = "收件人電話")
    private String recipientPhone;
    
    /**
     * 城市
     */
    @Schema(description = "城市")
    private String city;
    
    /**
     * 區域
     */
    @Schema(description = "區域")
    private String district;
    
    /**
     * 郵遞區號
     */
    @Schema(description = "郵遞區號")
    private String zipCode;
    
    /**
     * 詳細地址
     */
    @Schema(description = "詳細地址")
    private String address;
    
    /**
     * 完整地址（城市 + 區域 + 詳細地址）
     */
    @Schema(description = "完整地址")
    private String fullAddress;
    
    /**
     * 是否為預設地址
     */
    @Schema(description = "是否為預設地址")
    private Boolean isDefault;
    
    /**
     * 建立時間
     */
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;
    
    /**
     * 更新時間
     */
    @Schema(description = "更新時間")
    private LocalDateTime updatedAt;
}
