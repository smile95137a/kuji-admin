package com.group.admin.res.store;

import io.swagger.v3.oas.annotations.media.Schema;
import com.group.admin.req.store.BusinessHoursReq;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 店家資訊回應
 * 
 * <p>後台用，包含完整店家資訊</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "店家資訊回應")
public class StoreRes {

    /**
     * 店家 ID
     */
    @Schema(description = "店家 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    /**
     * 店家名稱
     */
    @Schema(description = "店家名稱", example = "KUJI 官方商店")
    private String storeName;

    /**
     * 短描述
     */
    @Schema(description = "短描述", example = "專營一番賞、扭蛋精品")
    private String shortDescription;

    /**
     * 詳細介紹
     */
    @Schema(description = "詳細介紹")
    private String longDescription;

    /**
     * Logo URL
     */
    @Schema(description = "Logo URL")
    private String logoUrl;

    /**
     * 封面圖片 URL
     */
    @Schema(description = "封面圖片 URL")
    private String coverImageUrl;

    /**
     * 聯絡 Email
     */
    @Schema(description = "聯絡 Email")
    private String email;

    /**
     * 聯絡電話
     */
    @Schema(description = "聯絡電話")
    private String phone;

    /**
     * 地址
     */
    @Schema(description = "地址")
    private String address;

    /**
     * 營業時間
     */
    @Schema(description = "營業時間")
    private String businessHours;

    /**
     * 結構化營業時間（若 DB 儲存為結構化 JSON，會反序列化到此欄位）
     */
    @Schema(description = "結構化營業時間")
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
     * 店家主帳號 ID（扁平化欄位）
     */
    @Schema(description = "店家主帳號 ID")
    private String ownerId;

    /**
     * 店家主帳號登入名稱
     */
    @Schema(description = "店家主帳號登入名稱")
    private String ownerUsername;

    /**
     * 店家主帳號顯示名稱
     */
    @Schema(description = "店家主帳號顯示名稱")
    private String ownerDisplayName;

    /**
     * 狀態
     */
    @Schema(description = "狀態", example = "ACTIVE")
    private String status;

    @Schema(description = "招商推薦來源店家 ID")
    private String referrerStoreId;

    @Schema(description = "招商推薦來源店家名稱")
    private String referrerStoreName;

    @Schema(description = "招商使用的推薦碼 ID")
    private String referralCodeId;

    @Schema(description = "招商使用的推薦碼")
    private String referralCode;

    @Schema(description = "啟用成功時間")
    private LocalDateTime activatedAt;

    /**
     * 狀態顯示名稱
     */
    @Schema(description = "狀態顯示名稱", example = "啟用")
    private String statusDisplayName;

    /**
     * 店家主帳號資訊
     */
    @Schema(description = "店家主帳號資訊")
    private OwnerInfo owner;

    /**
     * 後台備註
     */
    @Schema(description = "後台備註")
    private String remark;

    /**
     * 建立時間
     */
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;

    /**
     * 建立者 ID
     */
    @Schema(description = "建立者 ID")
    private String createdBy;

    /**
     * 更新時間
     */
    @Schema(description = "更新時間")
    private LocalDateTime updatedAt;

    /**
     * 店家主帳號資訊
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "店家主帳號資訊")
    public static class OwnerInfo {

        /**
         * 使用者 ID
         */
        @Schema(description = "使用者 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        private String id;

        /**
         * 顯示名稱
         */
        @Schema(description = "顯示名稱", example = "王小明")
        private String displayName;

        /**
         * Email
         */
        @Schema(description = "Email", example = "owner@example.com")
        private String email;
    }
}
