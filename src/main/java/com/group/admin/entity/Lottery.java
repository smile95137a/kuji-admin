package com.group.admin.entity;

import java.time.LocalDateTime;

public class Lottery {
    private String id;

    private String storeId;

    private String title;

    private String imageUrl;

    private String category;

    private String subCategory;

    private String gameMode;

    private Long pricePerDraw;

    private Long discountedPrice;

    private Byte autoDiscountEnabled;

    private Byte allowMultiDraw;

    private String multiDrawOptions;

    private LocalDateTime scheduledAt;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer totalDraws;

    private Integer maxDraws;

    private Integer protectionDraws;

    private Integer protectionMinutes;

    private Byte freeDrawEnabled;

    private String designatedPrizeNumbers;

    private Byte ticketsGenerated;

    private String status;

    private Integer orderNum;

    private Integer weight;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String playMode;

    private Integer hotCount;

    private String theme;

    private String tags;

    private Boolean bonusEnabled;

    private Integer bonusPointsPerDraw;

    private Integer bonusCostPerDraw;

    private String description;

    private String remark;

    private String galleryImages;

    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId == null ? null : storeId.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl == null ? null : imageUrl.trim();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category == null ? null : category.trim();
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory == null ? null : subCategory.trim();
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode == null ? null : gameMode.trim();
    }

    public Long getPricePerDraw() {
        return pricePerDraw;
    }

    public void setPricePerDraw(Long pricePerDraw) {
        this.pricePerDraw = pricePerDraw;
    }

    public Long getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(Long discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public Byte getAutoDiscountEnabled() {
        return autoDiscountEnabled;
    }

    public void setAutoDiscountEnabled(Byte autoDiscountEnabled) {
        this.autoDiscountEnabled = autoDiscountEnabled;
    }

    public Byte getAllowMultiDraw() {
        return allowMultiDraw;
    }

    public void setAllowMultiDraw(Byte allowMultiDraw) {
        this.allowMultiDraw = allowMultiDraw;
    }

    public String getMultiDrawOptions() {
        return multiDrawOptions;
    }

    public void setMultiDrawOptions(String multiDrawOptions) {
        this.multiDrawOptions = multiDrawOptions == null ? null : multiDrawOptions.trim();
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getTotalDraws() {
        return totalDraws;
    }

    public void setTotalDraws(Integer totalDraws) {
        this.totalDraws = totalDraws;
    }

    public Integer getMaxDraws() {
        return maxDraws;
    }

    public void setMaxDraws(Integer maxDraws) {
        this.maxDraws = maxDraws;
    }

    public Integer getProtectionDraws() {
        return protectionDraws;
    }

    public void setProtectionDraws(Integer protectionDraws) {
        this.protectionDraws = protectionDraws;
    }

    public Integer getProtectionMinutes() {
        return protectionMinutes;
    }

    public void setProtectionMinutes(Integer protectionMinutes) {
        this.protectionMinutes = protectionMinutes;
    }

    public Byte getFreeDrawEnabled() {
        return freeDrawEnabled;
    }

    public void setFreeDrawEnabled(Byte freeDrawEnabled) {
        this.freeDrawEnabled = freeDrawEnabled;
    }

    public String getDesignatedPrizeNumbers() {
        return designatedPrizeNumbers;
    }

    public void setDesignatedPrizeNumbers(String designatedPrizeNumbers) {
        this.designatedPrizeNumbers = designatedPrizeNumbers == null ? null : designatedPrizeNumbers.trim();
    }

    public Byte getTicketsGenerated() {
        return ticketsGenerated;
    }

    public void setTicketsGenerated(Byte ticketsGenerated) {
        this.ticketsGenerated = ticketsGenerated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy == null ? null : createdBy.trim();
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

    public String getPlayMode() {
        return playMode;
    }

    public void setPlayMode(String playMode) {
        this.playMode = playMode == null ? null : playMode.trim();
    }

    public Integer getHotCount() {
        return hotCount;
    }

    public void setHotCount(Integer hotCount) {
        this.hotCount = hotCount;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme == null ? null : theme.trim();
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags == null ? null : tags.trim();
    }

    public Boolean getBonusEnabled() {
        return bonusEnabled;
    }

    public void setBonusEnabled(Boolean bonusEnabled) {
        this.bonusEnabled = bonusEnabled;
    }

    public Integer getBonusPointsPerDraw() {
        return bonusPointsPerDraw;
    }

    public void setBonusPointsPerDraw(Integer bonusPointsPerDraw) {
        this.bonusPointsPerDraw = bonusPointsPerDraw;
    }

    public Integer getBonusCostPerDraw() {
        return bonusCostPerDraw;
    }

    public void setBonusCostPerDraw(Integer bonusCostPerDraw) {
        this.bonusCostPerDraw = bonusCostPerDraw;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getGalleryImages() {
        return galleryImages;
    }

    public void setGalleryImages(String galleryImages) {
        this.galleryImages = galleryImages == null ? null : galleryImages.trim();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    private String paymentType;

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType == null ? null : paymentType.trim();
    }

    private Integer freeDrawThreshold;

    public Integer getFreeDrawThreshold() {
        return freeDrawThreshold;
    }

    public void setFreeDrawThreshold(Integer freeDrawThreshold) {
        this.freeDrawThreshold = freeDrawThreshold;
    }

    private String delistStrategy;

    public String getDelistStrategy() {
        return delistStrategy;
    }

    public void setDelistStrategy(String delistStrategy) {
        this.delistStrategy = delistStrategy == null ? null : delistStrategy.trim();
    }
}