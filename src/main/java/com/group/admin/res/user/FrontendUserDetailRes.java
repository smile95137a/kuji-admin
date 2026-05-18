package com.group.admin.res.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "前台會員詳細資料")
public class FrontendUserDetailRes {

    @Schema(description = "會員 ID")
    private String id;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "暱稱")
    private String nickname;

    @Schema(description = "頭像 URL")
    private String avatar;

    @Schema(description = "登入來源")
    private String provider;

    @Schema(description = "第三方來源 ID")
    private String providerId;

    @Schema(description = "金幣")
    private Long goldCoins;

    @Schema(description = "紅利點數")
    private Long bonusCoins;

    @Schema(description = "狀態")
    private String status;

    @Schema(description = "狀態名稱")
    private String statusName;

    @Schema(description = "Email 是否驗證")
    private Boolean emailVerified;

    @Schema(description = "手機號碼")
    private String phoneNumber;

    @Schema(description = "LINE ID")
    private String lineId;

    @Schema(description = "收件人姓名")
    private String recipientName;

    @Schema(description = "收件人電話")
    private String recipientPhone;

    @Schema(description = "縣市")
    private String city;

    @Schema(description = "地區")
    private String district;

    @Schema(description = "地址明細")
    private String addressDetail;

    @Schema(description = "完整地址")
    private String address;

    @Schema(description = "發票類型")
    private String invoiceType;

    @Schema(description = "發票類型名稱")
    private String invoiceTypeName;

    @Schema(description = "發票 Email")
    private String invoiceEmail;

    @Schema(description = "載具條碼")
    private String carrierCode;

    @Schema(description = "統一編號")
    private String taxId;

    @Schema(description = "公司名稱")
    private String companyName;

    @Schema(description = "發票內容摘要")
    private String invoiceContent;

    @Schema(description = "登入失敗次數")
    private Integer failedLoginAttempts;

    @Schema(description = "鎖定到期時間")
    private LocalDateTime lockedUntil;

    @Schema(description = "最後登入時間")
    private LocalDateTime lastLoginAt;

    @Schema(description = "建立時間")
    private LocalDateTime createdAt;

    @Schema(description = "更新時間")
    private LocalDateTime updatedAt;
}
