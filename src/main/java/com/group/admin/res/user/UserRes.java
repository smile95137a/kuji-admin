package com.group.admin.res.user;

import com.group.admin.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRes {

    private String id;
    private String email;
    private String nickname;
    private String avatar;
    private String avatarUrl;
    private String provider;
    private String status;
    private Long goldCoins;
    private Long bonusCoins;
    private Long totalRecharged;
    private String phoneNumber;
    private String lineId;
    private String recipientName;
    private String recipientPhone;
    private String city;
    private String district;
    private String addressDetail;
    private String invoiceType;
    private String invoiceEmail;
    private String carrierCode;
    private String taxId;
    private String companyName;
    private String referralCode;
    private String referredStoreId;
    private Byte emailVerified;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
    private Integer failedLoginAttempts;

    public static UserRes from(User user) {
        if (user == null) {
            return null;
        }

        return UserRes.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .avatarUrl(user.getAvatar())
                .provider(user.getProvider())
                .status(user.getStatus())
                .goldCoins(user.getGoldCoins() != null ? user.getGoldCoins() : 0L)
                .bonusCoins(user.getBonusCoins() != null ? user.getBonusCoins() : 0L)
                .totalRecharged(user.getTotalRecharged() != null ? user.getTotalRecharged() : 0L)
                .phoneNumber(user.getPhoneNumber())
                .lineId(user.getLineId())
                .recipientName(user.getRecipientName())
                .recipientPhone(user.getRecipientPhone())
                .city(user.getCity())
                .district(user.getDistrict())
                .addressDetail(user.getAddressDetail())
                .invoiceType(user.getInvoiceType())
                .invoiceEmail(user.getInvoiceEmail())
                .carrierCode(user.getCarrierCode())
                .taxId(user.getTaxId())
                .companyName(user.getCompanyName())
                .referralCode(user.getReferralCode())
                .referredStoreId(user.getReferredStoreId())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .version(user.getVersion())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .build();
    }
}
