package com.group.admin.req.store;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "建立店家請求（含負責人帳號）")
public class CreateStoreReq {

    // ==================== 店家資訊 ====================

    @NotBlank(message = "店家名稱不可為空")
    @Schema(description = "店家名稱", example = "KUJI 官方商店")
    private String storeName;

    @Schema(description = "短描述", example = "專營一番賞、扭蛋精品")
    private String shortDescription;

    @Schema(description = "詳細介紹")
    private String longDescription;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "封面圖片 URL")
    private String coverImageUrl;

    @Email(message = "店家聯絡 Email 格式不正確")
    @Schema(description = "店家聯絡 Email")
    private String email;

    @NotBlank(message = "店家聯絡電話不可為空")
    @Schema(description = "店家聯絡電話", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @NotBlank(message = "店家地址不可為空")
    @Schema(description = "店家地址，需包含縣市、行政區與詳細地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String address;

    @Valid
    @NotNull(message = "營業時間不可為空，請提供結構化營業時間")
    @Schema(description = "結構化營業時間（JSON 結構，必填）")
    private BusinessHoursReq businessHoursStructured;

    @Schema(description = "Facebook 連結")
    private String facebookUrl;

    @Schema(description = "Instagram 連結")
    private String instagramUrl;

    @Schema(description = "LINE ID")
    private String lineId;

    @Schema(description = "後台備註")
    private String remark;

    @Schema(description = "推薦碼（選填，若有填寫則視為店家招商來源）", example = "KUJI2026")
    private String referralCode;

    // ==================== 負責人帳號 ====================

    @Valid
    @Schema(description = "店家負責人帳號資訊")
    private OwnerAccountReq owner;

    @Data
    @Schema(description = "店家負責人帳號")
    public static class OwnerAccountReq {

        @NotBlank(message = "負責人帳號（Email）不可為空")
        @Email(message = "負責人 Email 格式不正確")
        @Schema(description = "登入帳號（Email）", example = "owner@store.com")
        private String username;

        @Size(min = 8, message = "密碼至少 8 個字元")
        @Schema(description = "初始密碼（為空則自動生成）")
        private String password;

        @Schema(description = "顯示名稱", example = "王小明")
        private String displayName;

        @Email(message = "Email 格式不正確")
        @Schema(description = "聯絡 Email")
        private String email;

        @Schema(description = "聯絡電話", example = "0912345678")
        private String phone;
    }
}
