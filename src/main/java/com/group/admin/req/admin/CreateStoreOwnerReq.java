package com.group.admin.req.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.group.admin.req.store.BusinessHoursReq;

/**
 * 建立 StoreOwner 帳號請求
 * 
 * <p>由 Admin 建立店家主帳號，同時建立店家資料</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "建立 StoreOwner 帳號請求")
public class CreateStoreOwnerReq {

    // ==================== 帳號資訊 ====================
    
    /**
     * Email（同時作為登入帳號）
     */
    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    @Schema(description = "Email（同時作為登入帳號）", example = "store@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    /**
     * 顯示名稱
     */
    @NotBlank(message = "顯示名稱不可為空")
    @Schema(description = "顯示名稱", example = "王小明", requiredMode = Schema.RequiredMode.REQUIRED)
    private String displayName;

    /**
     * 聯絡電話
     */
    @Schema(description = "聯絡電話", example = "0912345678")
    private String phone;

    /**
     * 備註
     */
    @Schema(description = "備註")
    private String remark;

    // ==================== 店家資訊 ====================
    
    /**
     * 店家名稱
     */
    @NotBlank(message = "店家名稱不可為空")
    @Schema(description = "店家名稱", example = "KUJI 官方商店", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storeName;

    /**
     * 短描述（列表用）
     */
    @NotBlank(message = "店家短描述不可為空")
    @Schema(description = "店家短描述", example = "專營一番賞、扭蛋精品", requiredMode = Schema.RequiredMode.REQUIRED)
    private String shortDescription;

    /**
     * 詳細介紹
     */
    @Schema(description = "店家詳細介紹")
    private String longDescription;

    /**
     * Logo URL
     */
    @NotBlank(message = "Logo URL 不可為空")
    @Schema(description = "Logo URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String logoUrl;

    /**
     * 封面圖片 URL
     */
    @Schema(description = "封面圖片 URL")
    private String coverImageUrl;

    /**
     * 店家聯絡 Email
     */
    @NotBlank(message = "店家聯絡 Email 不可為空")
    @Email(message = "店家聯絡 Email 格式不正確")
    @Schema(description = "店家聯絡 Email", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storeEmail;

    /**
     * 店家聯絡電話
     */
    @NotBlank(message = "店家聯絡電話不可為空")
    @Schema(description = "店家聯絡電話", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storePhone;

    /**
     * 店家地址
     */
    @NotBlank(message = "店家地址不可為空")
    @Schema(description = "店家地址（無實體店可填「無」）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storeAddress;

    /**
     * 結構化營業時間（必填）
     */
    @Valid
    @NotNull(message = "營業時間不可為空")
    @Schema(description = "結構化營業時間（JSON 結構）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BusinessHoursReq businessHoursStructured;

    /**
     * Facebook 連結
     */
    @Schema(description = "Facebook 連結")
    private String facebookUrl;

    /**
     * Instagram 連結
     */
    @Schema(description = "Instagram 連結")
    private String instagramUrl;

    /**
     * LINE ID
     */
    @Schema(description = "LINE ID")
    private String lineId;
}
