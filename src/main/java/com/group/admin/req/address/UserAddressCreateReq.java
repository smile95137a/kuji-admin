package com.group.admin.req.address;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增收件地址請求
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "新增收件地址請求")
public class UserAddressCreateReq {
    
    /**
     * 地址標籤（如：家、公司）
     */
    @Size(max = 50, message = "標籤不能超過 50 字元")
    @Schema(description = "地址標籤（如：家、公司）")
    private String label;
    
    /**
     * 收件人姓名
     */
    @NotBlank(message = "收件人姓名不能為空")
    @Size(max = 100, message = "收件人姓名不能超過 100 字元")
    @Schema(description = "收件人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String recipientName;
    
    /**
     * 收件人電話
     */
    @NotBlank(message = "收件人電話不能為空")
    @Size(max = 20, message = "收件人電話不能超過 20 字元")
    @Schema(description = "收件人電話", requiredMode = Schema.RequiredMode.REQUIRED)
    private String recipientPhone;
    
    /**
     * 城市
     */
    @NotBlank(message = "城市不能為空")
    @Size(max = 50, message = "城市不能超過 50 字元")
    @Schema(description = "城市", requiredMode = Schema.RequiredMode.REQUIRED)
    private String city;
    
    /**
     * 區域
     */
    @NotBlank(message = "區域不能為空")
    @Size(max = 50, message = "區域不能超過 50 字元")
    @Schema(description = "區域", requiredMode = Schema.RequiredMode.REQUIRED)
    private String district;
    
    /**
     * 郵遞區號
     */
    @Size(max = 10, message = "郵遞區號不能超過 10 字元")
    @Schema(description = "郵遞區號")
    private String zipCode;
    
    /**
     * 詳細地址
     */
    @NotBlank(message = "詳細地址不能為空")
    @Size(max = 500, message = "詳細地址不能超過 500 字元")
    @Schema(description = "詳細地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;
    
    /**
     * 是否設為預設地址
     */
    @Schema(description = "是否設為預設地址")
    private Boolean isDefault;
}
