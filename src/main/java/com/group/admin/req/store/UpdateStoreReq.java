package com.group.admin.req.store;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新店家請求
 * 
 * <p>StoreOwner 或 Admin 可更新店家資料</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "更新店家請求")
public class UpdateStoreReq {

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
    private String email;

    /**
     * 店家聯絡電話
     */
    @NotBlank(message = "店家聯絡電話不可為空")
    @Schema(description = "店家聯絡電話", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    /**
     * 店家地址
     */
    @NotBlank(message = "店家地址不可為空")
    @Schema(description = "店家地址，需包含縣市、行政區與詳細地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;

    /**
     * 營業時間（請使用 `businessHoursStructured`）
     */
    @Valid
    @Schema(description = "結構化營業時間（優先使用）")
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

    /**
     * 後台備註（僅 Admin 可編輯）
     */
    @Schema(description = "後台備註")
    private String remark;

    @Schema(description = "推薦碼（選填，僅 Admin 可於啟用前調整）")
    private String referralCode;
}
