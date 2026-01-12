package com.group.admin.req.address;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新收件地址請求
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "更新收件地址請求")
public class UserAddressUpdateReq {
    
    /**
     * 地址標籤（如：家、公司）
     */
    @Size(max = 50, message = "標籤不能超過 50 字元")
    @Schema(description = "地址標籤（如：家、公司）")
    private String label;
    
    /**
     * 收件人姓名
     */
    @Size(max = 100, message = "收件人姓名不能超過 100 字元")
    @Schema(description = "收件人姓名")
    private String recipientName;
    
    /**
     * 收件人電話
     */
    @Size(max = 20, message = "收件人電話不能超過 20 字元")
    @Schema(description = "收件人電話")
    private String recipientPhone;
    
    /**
     * 城市
     */
    @Size(max = 50, message = "城市不能超過 50 字元")
    @Schema(description = "城市")
    private String city;
    
    /**
     * 區域
     */
    @Size(max = 50, message = "區域不能超過 50 字元")
    @Schema(description = "區域")
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
    @Size(max = 500, message = "詳細地址不能超過 500 字元")
    @Schema(description = "詳細地址")
    private String address;
    
    /**
     * 是否設為預設地址
     */
    @Schema(description = "是否設為預設地址")
    private Boolean isDefault;
}
