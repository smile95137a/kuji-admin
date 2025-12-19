package com.group.admin.entity;

import java.time.LocalDateTime;

public class Lottery {
    private String id;

    private String storeId;

    private String title;

    private String imageUrl;

    private String category;

    private String subCategory;

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

    private String status;

    private Integer orderNum;

    private Integer weight;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String description;

    private String remark;

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
}