package com.group.admin.req.store;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @Schema(description = "店家聯絡電話")
    private String phone;

    @Schema(description = "店家地址")
    private String address;

    @Schema(description = "營業時間")
    private String businessHours;

    @Schema(description = "Facebook 連結")
    private String facebookUrl;

    @Schema(description = "Instagram 連結")
    private String instagramUrl;

    @Schema(description = "LINE ID")
    private String lineId;

    @Schema(description = "後台備註")
    private String remark;

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
