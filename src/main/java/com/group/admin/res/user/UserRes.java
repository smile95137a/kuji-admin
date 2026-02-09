package com.group.admin.res.user;

import java.time.LocalDateTime;

import com.group.admin.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 使用者回應 DTO
 * 
 * <p>⚠️ 不包含敏感資訊（密碼、重設 Token 等）</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRes {
    
    /** 使用者 ID */
    private String id;
    
    /** Email */
    private String email;
    
    /** 暱稱 */
    private String nickname;
    
    /** 頭像 URL */
    private String avatarUrl;
    
    /** 登入方式：EMAIL, GOOGLE, FACEBOOK */
    private String provider;
    
    /** 狀態：ACTIVE, INACTIVE, BANNED */
    private String status;
    
    /** 金幣餘額（直接存在 user 表）*/
    private Long goldCoins;
    
    /** 紅利幣餘額（直接存在 user 表）*/
    private Long bonusCoins;
    
    /** 累計儲值金額（直接存在 user 表）*/
    private Long totalRecharged;
    
    /** 手機號碼 */
    private String phoneNumber;
    
    /** LINE ID */
    private String lineId;
    
    /** 收件人姓名 */
    private String recipientName;
    
    /** 收件人電話 */
    private String recipientPhone;
    
    /** 城市 */
    private String city;
    
    /** 區域 */
    private String district;
    
    /** 詳細地址 */
    private String addressDetail;
    
    /** 發票類型：DUPLICATE(二聯式), TRIPLICATE(三聯式), CARRIER(載具), DONATE(捐贈) */
    private String invoiceType;
    
    /** 發票 Email（電子發票用）*/
    private String invoiceEmail;
    
    /** 載具條碼 */
    private String carrierCode;
    
    /** 統一編號（三聯式發票用）*/
    private String taxId;
    
    /** 公司名稱（三聯式發票用）*/
    private String companyName;
    
    /** 推薦碼 */
    private String referralCode;
    
    /** 推薦的店家 ID */
    private String referredStoreId;
    
    /** Email 是否驗證 */
    private Boolean emailVerified;
    
    /** 最後登入時間 */
    private LocalDateTime lastLoginAt;
    
    /** 註冊時間 */
    private LocalDateTime createdAt;
    
    /** 最後更新時間 */
    private LocalDateTime updatedAt;
    
    /**
     * 從 Entity 轉換為 DTO（金幣/紅利直接從 User 讀取）
     */
    public static UserRes from(User user) {
        if (user == null) {
            return null;
        }
        
        return UserRes.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
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
                .emailVerified(user.getEmailVerified() != null && user.getEmailVerified() == 1)
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
