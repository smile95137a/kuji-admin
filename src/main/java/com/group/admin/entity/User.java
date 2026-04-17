package com.group.admin.entity;

import java.time.LocalDateTime;

public class User {
    private String id;

    private String email;

    private String nickname;

    private String password;

    private String avatar;

    private String provider;

    private String providerId;

    private Long goldCoins;

    private Long bonusCoins;

    private String status;

    private Byte emailVerified;

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

    private String emailVerificationToken;

    private LocalDateTime emailVerificationExpires;

    private String passwordResetToken;

    private LocalDateTime passwordResetExpires;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long totalRecharged;

    private Integer version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname == null ? null : nickname.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar == null ? null : avatar.trim();
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider == null ? null : provider.trim();
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId == null ? null : providerId.trim();
    }

    public Long getGoldCoins() {
        return goldCoins;
    }

    public void setGoldCoins(Long goldCoins) {
        this.goldCoins = goldCoins;
    }

    public Long getBonusCoins() {
        return bonusCoins;
    }

    public void setBonusCoins(Long bonusCoins) {
        this.bonusCoins = bonusCoins;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Byte getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Byte emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber == null ? null : phoneNumber.trim();
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId == null ? null : lineId.trim();
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName == null ? null : recipientName.trim();
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone == null ? null : recipientPhone.trim();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city == null ? null : city.trim();
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district == null ? null : district.trim();
    }

    public String getAddressDetail() {
        return addressDetail;
    }

    public void setAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail == null ? null : addressDetail.trim();
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType == null ? null : invoiceType.trim();
    }

    public String getInvoiceEmail() {
        return invoiceEmail;
    }

    public void setInvoiceEmail(String invoiceEmail) {
        this.invoiceEmail = invoiceEmail == null ? null : invoiceEmail.trim();
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode == null ? null : carrierCode.trim();
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId == null ? null : taxId.trim();
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName == null ? null : companyName.trim();
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode == null ? null : referralCode.trim();
    }

    public String getReferredStoreId() {
        return referredStoreId;
    }

    public void setReferredStoreId(String referredStoreId) {
        this.referredStoreId = referredStoreId == null ? null : referredStoreId.trim();
    }

    public String getEmailVerificationToken() {
        return emailVerificationToken;
    }

    public void setEmailVerificationToken(String emailVerificationToken) {
        this.emailVerificationToken = emailVerificationToken == null ? null : emailVerificationToken.trim();
    }

    public LocalDateTime getEmailVerificationExpires() {
        return emailVerificationExpires;
    }

    public void setEmailVerificationExpires(LocalDateTime emailVerificationExpires) {
        this.emailVerificationExpires = emailVerificationExpires;
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken == null ? null : passwordResetToken.trim();
    }

    public LocalDateTime getPasswordResetExpires() {
        return passwordResetExpires;
    }

    public void setPasswordResetExpires(LocalDateTime passwordResetExpires) {
        this.passwordResetExpires = passwordResetExpires;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getTotalRecharged() {
        return totalRecharged;
    }

    public void setTotalRecharged(Long totalRecharged) {
        this.totalRecharged = totalRecharged;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    // ========== Spec-023: 推薦碼整合新增欄位 ==========

    private java.time.LocalDateTime referralBoundAt;

    private Integer isOauthNewUser;

    public java.time.LocalDateTime getReferralBoundAt() {
        return referralBoundAt;
    }

    public void setReferralBoundAt(java.time.LocalDateTime referralBoundAt) {
        this.referralBoundAt = referralBoundAt;
    }

    public Integer getIsOauthNewUser() {
        return isOauthNewUser;
    }

    public void setIsOauthNewUser(Integer isOauthNewUser) {
        this.isOauthNewUser = isOauthNewUser;
    }

    private Integer failedLoginAttempts;

    private LocalDateTime lockedUntil;

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }
}