package com.group.admin.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LotteryExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public LotteryExample() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(String value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(String value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(String value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(String value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(String value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLike(String value) {
            addCriterion("id like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotLike(String value) {
            addCriterion("id not like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<String> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<String> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(String value1, String value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(String value1, String value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andStoreIdIsNull() {
            addCriterion("store_id is null");
            return (Criteria) this;
        }

        public Criteria andStoreIdIsNotNull() {
            addCriterion("store_id is not null");
            return (Criteria) this;
        }

        public Criteria andStoreIdEqualTo(String value) {
            addCriterion("store_id =", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdNotEqualTo(String value) {
            addCriterion("store_id <>", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdGreaterThan(String value) {
            addCriterion("store_id >", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdGreaterThanOrEqualTo(String value) {
            addCriterion("store_id >=", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdLessThan(String value) {
            addCriterion("store_id <", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdLessThanOrEqualTo(String value) {
            addCriterion("store_id <=", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdLike(String value) {
            addCriterion("store_id like", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdNotLike(String value) {
            addCriterion("store_id not like", value, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdIn(List<String> values) {
            addCriterion("store_id in", values, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdNotIn(List<String> values) {
            addCriterion("store_id not in", values, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdBetween(String value1, String value2) {
            addCriterion("store_id between", value1, value2, "storeId");
            return (Criteria) this;
        }

        public Criteria andStoreIdNotBetween(String value1, String value2) {
            addCriterion("store_id not between", value1, value2, "storeId");
            return (Criteria) this;
        }

        public Criteria andTitleIsNull() {
            addCriterion("title is null");
            return (Criteria) this;
        }

        public Criteria andTitleIsNotNull() {
            addCriterion("title is not null");
            return (Criteria) this;
        }

        public Criteria andTitleEqualTo(String value) {
            addCriterion("title =", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleNotEqualTo(String value) {
            addCriterion("title <>", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleGreaterThan(String value) {
            addCriterion("title >", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleGreaterThanOrEqualTo(String value) {
            addCriterion("title >=", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleLessThan(String value) {
            addCriterion("title <", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleLessThanOrEqualTo(String value) {
            addCriterion("title <=", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleLike(String value) {
            addCriterion("title like", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleNotLike(String value) {
            addCriterion("title not like", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleIn(List<String> values) {
            addCriterion("title in", values, "title");
            return (Criteria) this;
        }

        public Criteria andTitleNotIn(List<String> values) {
            addCriterion("title not in", values, "title");
            return (Criteria) this;
        }

        public Criteria andTitleBetween(String value1, String value2) {
            addCriterion("title between", value1, value2, "title");
            return (Criteria) this;
        }

        public Criteria andTitleNotBetween(String value1, String value2) {
            addCriterion("title not between", value1, value2, "title");
            return (Criteria) this;
        }

        public Criteria andImageUrlIsNull() {
            addCriterion("image_url is null");
            return (Criteria) this;
        }

        public Criteria andImageUrlIsNotNull() {
            addCriterion("image_url is not null");
            return (Criteria) this;
        }

        public Criteria andImageUrlEqualTo(String value) {
            addCriterion("image_url =", value, "imageUrl");
            return (Criteria) this;
        }

        public Criteria andImageUrlNotEqualTo(String value) {
            addCriterion("image_url <>", value, "imageUrl");
            return (Criteria) this;
        }

        public Criteria andImageUrlGreaterThan(String value) {
            addCriterion("image_url >", value, "imageUrl");
            return (Criteria) this;
        }

        public Criteria andImageUrlGreaterThanOrEqualTo(String value) {
            addCriterion("image_url >=", value, "imageUrl");
            return (Criteria) this;
        }

        public Criteria andImageUrlLessThan(String value) {
            addCriterion("image_url <", value, "imageUrl");
            return (Criteria) this;
        }

        public Criteria andImageUrlLessThanOrEqualTo(String value) {
            addCriterion("image_url <=", value, "imageUrl");
            return (Criteria) this;
        }

        public Criteria andImageUrlLike(String value) {
            addCriterion("image_url like", value, "imageUrl");
            return (Criteria) this;
        }

        public Criteria andImageUrlNotLike(String value) {
            addCriterion("image_url not like", value, "imageUrl");
            return (Criteria) this;
        }

        public Criteria andImageUrlIn(List<String> values) {
            addCriterion("image_url in", values, "imageUrl");
            return (Criteria) this;
        }

        public Criteria andImageUrlNotIn(List<String> values) {
            addCriterion("image_url not in", values, "imageUrl");
            return (Criteria) this;
        }

        public Criteria andImageUrlBetween(String value1, String value2) {
            addCriterion("image_url between", value1, value2, "imageUrl");
            return (Criteria) this;
        }

        public Criteria andImageUrlNotBetween(String value1, String value2) {
            addCriterion("image_url not between", value1, value2, "imageUrl");
            return (Criteria) this;
        }

        public Criteria andCategoryIsNull() {
            addCriterion("category is null");
            return (Criteria) this;
        }

        public Criteria andCategoryIsNotNull() {
            addCriterion("category is not null");
            return (Criteria) this;
        }

        public Criteria andCategoryEqualTo(String value) {
            addCriterion("category =", value, "category");
            return (Criteria) this;
        }

        public Criteria andCategoryNotEqualTo(String value) {
            addCriterion("category <>", value, "category");
            return (Criteria) this;
        }

        public Criteria andCategoryGreaterThan(String value) {
            addCriterion("category >", value, "category");
            return (Criteria) this;
        }

        public Criteria andCategoryGreaterThanOrEqualTo(String value) {
            addCriterion("category >=", value, "category");
            return (Criteria) this;
        }

        public Criteria andCategoryLessThan(String value) {
            addCriterion("category <", value, "category");
            return (Criteria) this;
        }

        public Criteria andCategoryLessThanOrEqualTo(String value) {
            addCriterion("category <=", value, "category");
            return (Criteria) this;
        }

        public Criteria andCategoryLike(String value) {
            addCriterion("category like", value, "category");
            return (Criteria) this;
        }

        public Criteria andCategoryNotLike(String value) {
            addCriterion("category not like", value, "category");
            return (Criteria) this;
        }

        public Criteria andCategoryIn(List<String> values) {
            addCriterion("category in", values, "category");
            return (Criteria) this;
        }

        public Criteria andCategoryNotIn(List<String> values) {
            addCriterion("category not in", values, "category");
            return (Criteria) this;
        }

        public Criteria andCategoryBetween(String value1, String value2) {
            addCriterion("category between", value1, value2, "category");
            return (Criteria) this;
        }

        public Criteria andCategoryNotBetween(String value1, String value2) {
            addCriterion("category not between", value1, value2, "category");
            return (Criteria) this;
        }

        public Criteria andSubCategoryIsNull() {
            addCriterion("sub_category is null");
            return (Criteria) this;
        }

        public Criteria andSubCategoryIsNotNull() {
            addCriterion("sub_category is not null");
            return (Criteria) this;
        }

        public Criteria andSubCategoryEqualTo(String value) {
            addCriterion("sub_category =", value, "subCategory");
            return (Criteria) this;
        }

        public Criteria andSubCategoryNotEqualTo(String value) {
            addCriterion("sub_category <>", value, "subCategory");
            return (Criteria) this;
        }

        public Criteria andSubCategoryGreaterThan(String value) {
            addCriterion("sub_category >", value, "subCategory");
            return (Criteria) this;
        }

        public Criteria andSubCategoryGreaterThanOrEqualTo(String value) {
            addCriterion("sub_category >=", value, "subCategory");
            return (Criteria) this;
        }

        public Criteria andSubCategoryLessThan(String value) {
            addCriterion("sub_category <", value, "subCategory");
            return (Criteria) this;
        }

        public Criteria andSubCategoryLessThanOrEqualTo(String value) {
            addCriterion("sub_category <=", value, "subCategory");
            return (Criteria) this;
        }

        public Criteria andSubCategoryLike(String value) {
            addCriterion("sub_category like", value, "subCategory");
            return (Criteria) this;
        }

        public Criteria andSubCategoryNotLike(String value) {
            addCriterion("sub_category not like", value, "subCategory");
            return (Criteria) this;
        }

        public Criteria andSubCategoryIn(List<String> values) {
            addCriterion("sub_category in", values, "subCategory");
            return (Criteria) this;
        }

        public Criteria andSubCategoryNotIn(List<String> values) {
            addCriterion("sub_category not in", values, "subCategory");
            return (Criteria) this;
        }

        public Criteria andSubCategoryBetween(String value1, String value2) {
            addCriterion("sub_category between", value1, value2, "subCategory");
            return (Criteria) this;
        }

        public Criteria andSubCategoryNotBetween(String value1, String value2) {
            addCriterion("sub_category not between", value1, value2, "subCategory");
            return (Criteria) this;
        }

        public Criteria andPricePerDrawIsNull() {
            addCriterion("price_per_draw is null");
            return (Criteria) this;
        }

        public Criteria andPricePerDrawIsNotNull() {
            addCriterion("price_per_draw is not null");
            return (Criteria) this;
        }

        public Criteria andPricePerDrawEqualTo(Long value) {
            addCriterion("price_per_draw =", value, "pricePerDraw");
            return (Criteria) this;
        }

        public Criteria andPricePerDrawNotEqualTo(Long value) {
            addCriterion("price_per_draw <>", value, "pricePerDraw");
            return (Criteria) this;
        }

        public Criteria andPricePerDrawGreaterThan(Long value) {
            addCriterion("price_per_draw >", value, "pricePerDraw");
            return (Criteria) this;
        }

        public Criteria andPricePerDrawGreaterThanOrEqualTo(Long value) {
            addCriterion("price_per_draw >=", value, "pricePerDraw");
            return (Criteria) this;
        }

        public Criteria andPricePerDrawLessThan(Long value) {
            addCriterion("price_per_draw <", value, "pricePerDraw");
            return (Criteria) this;
        }

        public Criteria andPricePerDrawLessThanOrEqualTo(Long value) {
            addCriterion("price_per_draw <=", value, "pricePerDraw");
            return (Criteria) this;
        }

        public Criteria andPricePerDrawIn(List<Long> values) {
            addCriterion("price_per_draw in", values, "pricePerDraw");
            return (Criteria) this;
        }

        public Criteria andPricePerDrawNotIn(List<Long> values) {
            addCriterion("price_per_draw not in", values, "pricePerDraw");
            return (Criteria) this;
        }

        public Criteria andPricePerDrawBetween(Long value1, Long value2) {
            addCriterion("price_per_draw between", value1, value2, "pricePerDraw");
            return (Criteria) this;
        }

        public Criteria andPricePerDrawNotBetween(Long value1, Long value2) {
            addCriterion("price_per_draw not between", value1, value2, "pricePerDraw");
            return (Criteria) this;
        }

        public Criteria andDiscountedPriceIsNull() {
            addCriterion("discounted_price is null");
            return (Criteria) this;
        }

        public Criteria andDiscountedPriceIsNotNull() {
            addCriterion("discounted_price is not null");
            return (Criteria) this;
        }

        public Criteria andDiscountedPriceEqualTo(Long value) {
            addCriterion("discounted_price =", value, "discountedPrice");
            return (Criteria) this;
        }

        public Criteria andDiscountedPriceNotEqualTo(Long value) {
            addCriterion("discounted_price <>", value, "discountedPrice");
            return (Criteria) this;
        }

        public Criteria andDiscountedPriceGreaterThan(Long value) {
            addCriterion("discounted_price >", value, "discountedPrice");
            return (Criteria) this;
        }

        public Criteria andDiscountedPriceGreaterThanOrEqualTo(Long value) {
            addCriterion("discounted_price >=", value, "discountedPrice");
            return (Criteria) this;
        }

        public Criteria andDiscountedPriceLessThan(Long value) {
            addCriterion("discounted_price <", value, "discountedPrice");
            return (Criteria) this;
        }

        public Criteria andDiscountedPriceLessThanOrEqualTo(Long value) {
            addCriterion("discounted_price <=", value, "discountedPrice");
            return (Criteria) this;
        }

        public Criteria andDiscountedPriceIn(List<Long> values) {
            addCriterion("discounted_price in", values, "discountedPrice");
            return (Criteria) this;
        }

        public Criteria andDiscountedPriceNotIn(List<Long> values) {
            addCriterion("discounted_price not in", values, "discountedPrice");
            return (Criteria) this;
        }

        public Criteria andDiscountedPriceBetween(Long value1, Long value2) {
            addCriterion("discounted_price between", value1, value2, "discountedPrice");
            return (Criteria) this;
        }

        public Criteria andDiscountedPriceNotBetween(Long value1, Long value2) {
            addCriterion("discounted_price not between", value1, value2, "discountedPrice");
            return (Criteria) this;
        }

        public Criteria andAutoDiscountEnabledIsNull() {
            addCriterion("auto_discount_enabled is null");
            return (Criteria) this;
        }

        public Criteria andAutoDiscountEnabledIsNotNull() {
            addCriterion("auto_discount_enabled is not null");
            return (Criteria) this;
        }

        public Criteria andAutoDiscountEnabledEqualTo(Byte value) {
            addCriterion("auto_discount_enabled =", value, "autoDiscountEnabled");
            return (Criteria) this;
        }

        public Criteria andAutoDiscountEnabledNotEqualTo(Byte value) {
            addCriterion("auto_discount_enabled <>", value, "autoDiscountEnabled");
            return (Criteria) this;
        }

        public Criteria andAutoDiscountEnabledGreaterThan(Byte value) {
            addCriterion("auto_discount_enabled >", value, "autoDiscountEnabled");
            return (Criteria) this;
        }

        public Criteria andAutoDiscountEnabledGreaterThanOrEqualTo(Byte value) {
            addCriterion("auto_discount_enabled >=", value, "autoDiscountEnabled");
            return (Criteria) this;
        }

        public Criteria andAutoDiscountEnabledLessThan(Byte value) {
            addCriterion("auto_discount_enabled <", value, "autoDiscountEnabled");
            return (Criteria) this;
        }

        public Criteria andAutoDiscountEnabledLessThanOrEqualTo(Byte value) {
            addCriterion("auto_discount_enabled <=", value, "autoDiscountEnabled");
            return (Criteria) this;
        }

        public Criteria andAutoDiscountEnabledIn(List<Byte> values) {
            addCriterion("auto_discount_enabled in", values, "autoDiscountEnabled");
            return (Criteria) this;
        }

        public Criteria andAutoDiscountEnabledNotIn(List<Byte> values) {
            addCriterion("auto_discount_enabled not in", values, "autoDiscountEnabled");
            return (Criteria) this;
        }

        public Criteria andAutoDiscountEnabledBetween(Byte value1, Byte value2) {
            addCriterion("auto_discount_enabled between", value1, value2, "autoDiscountEnabled");
            return (Criteria) this;
        }

        public Criteria andAutoDiscountEnabledNotBetween(Byte value1, Byte value2) {
            addCriterion("auto_discount_enabled not between", value1, value2, "autoDiscountEnabled");
            return (Criteria) this;
        }

        public Criteria andAllowMultiDrawIsNull() {
            addCriterion("allow_multi_draw is null");
            return (Criteria) this;
        }

        public Criteria andAllowMultiDrawIsNotNull() {
            addCriterion("allow_multi_draw is not null");
            return (Criteria) this;
        }

        public Criteria andAllowMultiDrawEqualTo(Byte value) {
            addCriterion("allow_multi_draw =", value, "allowMultiDraw");
            return (Criteria) this;
        }

        public Criteria andAllowMultiDrawNotEqualTo(Byte value) {
            addCriterion("allow_multi_draw <>", value, "allowMultiDraw");
            return (Criteria) this;
        }

        public Criteria andAllowMultiDrawGreaterThan(Byte value) {
            addCriterion("allow_multi_draw >", value, "allowMultiDraw");
            return (Criteria) this;
        }

        public Criteria andAllowMultiDrawGreaterThanOrEqualTo(Byte value) {
            addCriterion("allow_multi_draw >=", value, "allowMultiDraw");
            return (Criteria) this;
        }

        public Criteria andAllowMultiDrawLessThan(Byte value) {
            addCriterion("allow_multi_draw <", value, "allowMultiDraw");
            return (Criteria) this;
        }

        public Criteria andAllowMultiDrawLessThanOrEqualTo(Byte value) {
            addCriterion("allow_multi_draw <=", value, "allowMultiDraw");
            return (Criteria) this;
        }

        public Criteria andAllowMultiDrawIn(List<Byte> values) {
            addCriterion("allow_multi_draw in", values, "allowMultiDraw");
            return (Criteria) this;
        }

        public Criteria andAllowMultiDrawNotIn(List<Byte> values) {
            addCriterion("allow_multi_draw not in", values, "allowMultiDraw");
            return (Criteria) this;
        }

        public Criteria andAllowMultiDrawBetween(Byte value1, Byte value2) {
            addCriterion("allow_multi_draw between", value1, value2, "allowMultiDraw");
            return (Criteria) this;
        }

        public Criteria andAllowMultiDrawNotBetween(Byte value1, Byte value2) {
            addCriterion("allow_multi_draw not between", value1, value2, "allowMultiDraw");
            return (Criteria) this;
        }

        public Criteria andMultiDrawOptionsIsNull() {
            addCriterion("multi_draw_options is null");
            return (Criteria) this;
        }

        public Criteria andMultiDrawOptionsIsNotNull() {
            addCriterion("multi_draw_options is not null");
            return (Criteria) this;
        }

        public Criteria andMultiDrawOptionsEqualTo(String value) {
            addCriterion("multi_draw_options =", value, "multiDrawOptions");
            return (Criteria) this;
        }

        public Criteria andMultiDrawOptionsNotEqualTo(String value) {
            addCriterion("multi_draw_options <>", value, "multiDrawOptions");
            return (Criteria) this;
        }

        public Criteria andMultiDrawOptionsGreaterThan(String value) {
            addCriterion("multi_draw_options >", value, "multiDrawOptions");
            return (Criteria) this;
        }

        public Criteria andMultiDrawOptionsGreaterThanOrEqualTo(String value) {
            addCriterion("multi_draw_options >=", value, "multiDrawOptions");
            return (Criteria) this;
        }

        public Criteria andMultiDrawOptionsLessThan(String value) {
            addCriterion("multi_draw_options <", value, "multiDrawOptions");
            return (Criteria) this;
        }

        public Criteria andMultiDrawOptionsLessThanOrEqualTo(String value) {
            addCriterion("multi_draw_options <=", value, "multiDrawOptions");
            return (Criteria) this;
        }

        public Criteria andMultiDrawOptionsLike(String value) {
            addCriterion("multi_draw_options like", value, "multiDrawOptions");
            return (Criteria) this;
        }

        public Criteria andMultiDrawOptionsNotLike(String value) {
            addCriterion("multi_draw_options not like", value, "multiDrawOptions");
            return (Criteria) this;
        }

        public Criteria andMultiDrawOptionsIn(List<String> values) {
            addCriterion("multi_draw_options in", values, "multiDrawOptions");
            return (Criteria) this;
        }

        public Criteria andMultiDrawOptionsNotIn(List<String> values) {
            addCriterion("multi_draw_options not in", values, "multiDrawOptions");
            return (Criteria) this;
        }

        public Criteria andMultiDrawOptionsBetween(String value1, String value2) {
            addCriterion("multi_draw_options between", value1, value2, "multiDrawOptions");
            return (Criteria) this;
        }

        public Criteria andMultiDrawOptionsNotBetween(String value1, String value2) {
            addCriterion("multi_draw_options not between", value1, value2, "multiDrawOptions");
            return (Criteria) this;
        }

        public Criteria andScheduledAtIsNull() {
            addCriterion("scheduled_at is null");
            return (Criteria) this;
        }

        public Criteria andScheduledAtIsNotNull() {
            addCriterion("scheduled_at is not null");
            return (Criteria) this;
        }

        public Criteria andScheduledAtEqualTo(LocalDateTime value) {
            addCriterion("scheduled_at =", value, "scheduledAt");
            return (Criteria) this;
        }

        public Criteria andScheduledAtNotEqualTo(LocalDateTime value) {
            addCriterion("scheduled_at <>", value, "scheduledAt");
            return (Criteria) this;
        }

        public Criteria andScheduledAtGreaterThan(LocalDateTime value) {
            addCriterion("scheduled_at >", value, "scheduledAt");
            return (Criteria) this;
        }

        public Criteria andScheduledAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("scheduled_at >=", value, "scheduledAt");
            return (Criteria) this;
        }

        public Criteria andScheduledAtLessThan(LocalDateTime value) {
            addCriterion("scheduled_at <", value, "scheduledAt");
            return (Criteria) this;
        }

        public Criteria andScheduledAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("scheduled_at <=", value, "scheduledAt");
            return (Criteria) this;
        }

        public Criteria andScheduledAtIn(List<LocalDateTime> values) {
            addCriterion("scheduled_at in", values, "scheduledAt");
            return (Criteria) this;
        }

        public Criteria andScheduledAtNotIn(List<LocalDateTime> values) {
            addCriterion("scheduled_at not in", values, "scheduledAt");
            return (Criteria) this;
        }

        public Criteria andScheduledAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("scheduled_at between", value1, value2, "scheduledAt");
            return (Criteria) this;
        }

        public Criteria andScheduledAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("scheduled_at not between", value1, value2, "scheduledAt");
            return (Criteria) this;
        }

        public Criteria andStartTimeIsNull() {
            addCriterion("start_time is null");
            return (Criteria) this;
        }

        public Criteria andStartTimeIsNotNull() {
            addCriterion("start_time is not null");
            return (Criteria) this;
        }

        public Criteria andStartTimeEqualTo(LocalDateTime value) {
            addCriterion("start_time =", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotEqualTo(LocalDateTime value) {
            addCriterion("start_time <>", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThan(LocalDateTime value) {
            addCriterion("start_time >", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("start_time >=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThan(LocalDateTime value) {
            addCriterion("start_time <", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("start_time <=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeIn(List<LocalDateTime> values) {
            addCriterion("start_time in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotIn(List<LocalDateTime> values) {
            addCriterion("start_time not in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("start_time between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("start_time not between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeIsNull() {
            addCriterion("end_time is null");
            return (Criteria) this;
        }

        public Criteria andEndTimeIsNotNull() {
            addCriterion("end_time is not null");
            return (Criteria) this;
        }

        public Criteria andEndTimeEqualTo(LocalDateTime value) {
            addCriterion("end_time =", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotEqualTo(LocalDateTime value) {
            addCriterion("end_time <>", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeGreaterThan(LocalDateTime value) {
            addCriterion("end_time >", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("end_time >=", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeLessThan(LocalDateTime value) {
            addCriterion("end_time <", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("end_time <=", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeIn(List<LocalDateTime> values) {
            addCriterion("end_time in", values, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotIn(List<LocalDateTime> values) {
            addCriterion("end_time not in", values, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("end_time between", value1, value2, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("end_time not between", value1, value2, "endTime");
            return (Criteria) this;
        }

        public Criteria andTotalDrawsIsNull() {
            addCriterion("total_draws is null");
            return (Criteria) this;
        }

        public Criteria andTotalDrawsIsNotNull() {
            addCriterion("total_draws is not null");
            return (Criteria) this;
        }

        public Criteria andTotalDrawsEqualTo(Integer value) {
            addCriterion("total_draws =", value, "totalDraws");
            return (Criteria) this;
        }

        public Criteria andTotalDrawsNotEqualTo(Integer value) {
            addCriterion("total_draws <>", value, "totalDraws");
            return (Criteria) this;
        }

        public Criteria andTotalDrawsGreaterThan(Integer value) {
            addCriterion("total_draws >", value, "totalDraws");
            return (Criteria) this;
        }

        public Criteria andTotalDrawsGreaterThanOrEqualTo(Integer value) {
            addCriterion("total_draws >=", value, "totalDraws");
            return (Criteria) this;
        }

        public Criteria andTotalDrawsLessThan(Integer value) {
            addCriterion("total_draws <", value, "totalDraws");
            return (Criteria) this;
        }

        public Criteria andTotalDrawsLessThanOrEqualTo(Integer value) {
            addCriterion("total_draws <=", value, "totalDraws");
            return (Criteria) this;
        }

        public Criteria andTotalDrawsIn(List<Integer> values) {
            addCriterion("total_draws in", values, "totalDraws");
            return (Criteria) this;
        }

        public Criteria andTotalDrawsNotIn(List<Integer> values) {
            addCriterion("total_draws not in", values, "totalDraws");
            return (Criteria) this;
        }

        public Criteria andTotalDrawsBetween(Integer value1, Integer value2) {
            addCriterion("total_draws between", value1, value2, "totalDraws");
            return (Criteria) this;
        }

        public Criteria andTotalDrawsNotBetween(Integer value1, Integer value2) {
            addCriterion("total_draws not between", value1, value2, "totalDraws");
            return (Criteria) this;
        }

        public Criteria andMaxDrawsIsNull() {
            addCriterion("max_draws is null");
            return (Criteria) this;
        }

        public Criteria andMaxDrawsIsNotNull() {
            addCriterion("max_draws is not null");
            return (Criteria) this;
        }

        public Criteria andMaxDrawsEqualTo(Integer value) {
            addCriterion("max_draws =", value, "maxDraws");
            return (Criteria) this;
        }

        public Criteria andMaxDrawsNotEqualTo(Integer value) {
            addCriterion("max_draws <>", value, "maxDraws");
            return (Criteria) this;
        }

        public Criteria andMaxDrawsGreaterThan(Integer value) {
            addCriterion("max_draws >", value, "maxDraws");
            return (Criteria) this;
        }

        public Criteria andMaxDrawsGreaterThanOrEqualTo(Integer value) {
            addCriterion("max_draws >=", value, "maxDraws");
            return (Criteria) this;
        }

        public Criteria andMaxDrawsLessThan(Integer value) {
            addCriterion("max_draws <", value, "maxDraws");
            return (Criteria) this;
        }

        public Criteria andMaxDrawsLessThanOrEqualTo(Integer value) {
            addCriterion("max_draws <=", value, "maxDraws");
            return (Criteria) this;
        }

        public Criteria andMaxDrawsIn(List<Integer> values) {
            addCriterion("max_draws in", values, "maxDraws");
            return (Criteria) this;
        }

        public Criteria andMaxDrawsNotIn(List<Integer> values) {
            addCriterion("max_draws not in", values, "maxDraws");
            return (Criteria) this;
        }

        public Criteria andMaxDrawsBetween(Integer value1, Integer value2) {
            addCriterion("max_draws between", value1, value2, "maxDraws");
            return (Criteria) this;
        }

        public Criteria andMaxDrawsNotBetween(Integer value1, Integer value2) {
            addCriterion("max_draws not between", value1, value2, "maxDraws");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("status like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("status not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andOrderNumIsNull() {
            addCriterion("order_num is null");
            return (Criteria) this;
        }

        public Criteria andOrderNumIsNotNull() {
            addCriterion("order_num is not null");
            return (Criteria) this;
        }

        public Criteria andOrderNumEqualTo(Integer value) {
            addCriterion("order_num =", value, "orderNum");
            return (Criteria) this;
        }

        public Criteria andOrderNumNotEqualTo(Integer value) {
            addCriterion("order_num <>", value, "orderNum");
            return (Criteria) this;
        }

        public Criteria andOrderNumGreaterThan(Integer value) {
            addCriterion("order_num >", value, "orderNum");
            return (Criteria) this;
        }

        public Criteria andOrderNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("order_num >=", value, "orderNum");
            return (Criteria) this;
        }

        public Criteria andOrderNumLessThan(Integer value) {
            addCriterion("order_num <", value, "orderNum");
            return (Criteria) this;
        }

        public Criteria andOrderNumLessThanOrEqualTo(Integer value) {
            addCriterion("order_num <=", value, "orderNum");
            return (Criteria) this;
        }

        public Criteria andOrderNumIn(List<Integer> values) {
            addCriterion("order_num in", values, "orderNum");
            return (Criteria) this;
        }

        public Criteria andOrderNumNotIn(List<Integer> values) {
            addCriterion("order_num not in", values, "orderNum");
            return (Criteria) this;
        }

        public Criteria andOrderNumBetween(Integer value1, Integer value2) {
            addCriterion("order_num between", value1, value2, "orderNum");
            return (Criteria) this;
        }

        public Criteria andOrderNumNotBetween(Integer value1, Integer value2) {
            addCriterion("order_num not between", value1, value2, "orderNum");
            return (Criteria) this;
        }

        public Criteria andWeightIsNull() {
            addCriterion("weight is null");
            return (Criteria) this;
        }

        public Criteria andWeightIsNotNull() {
            addCriterion("weight is not null");
            return (Criteria) this;
        }

        public Criteria andWeightEqualTo(Integer value) {
            addCriterion("weight =", value, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightNotEqualTo(Integer value) {
            addCriterion("weight <>", value, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightGreaterThan(Integer value) {
            addCriterion("weight >", value, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightGreaterThanOrEqualTo(Integer value) {
            addCriterion("weight >=", value, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightLessThan(Integer value) {
            addCriterion("weight <", value, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightLessThanOrEqualTo(Integer value) {
            addCriterion("weight <=", value, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightIn(List<Integer> values) {
            addCriterion("weight in", values, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightNotIn(List<Integer> values) {
            addCriterion("weight not in", values, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightBetween(Integer value1, Integer value2) {
            addCriterion("weight between", value1, value2, "weight");
            return (Criteria) this;
        }

        public Criteria andWeightNotBetween(Integer value1, Integer value2) {
            addCriterion("weight not between", value1, value2, "weight");
            return (Criteria) this;
        }

        public Criteria andCreatedByIsNull() {
            addCriterion("created_by is null");
            return (Criteria) this;
        }

        public Criteria andCreatedByIsNotNull() {
            addCriterion("created_by is not null");
            return (Criteria) this;
        }

        public Criteria andCreatedByEqualTo(String value) {
            addCriterion("created_by =", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByNotEqualTo(String value) {
            addCriterion("created_by <>", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByGreaterThan(String value) {
            addCriterion("created_by >", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByGreaterThanOrEqualTo(String value) {
            addCriterion("created_by >=", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByLessThan(String value) {
            addCriterion("created_by <", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByLessThanOrEqualTo(String value) {
            addCriterion("created_by <=", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByLike(String value) {
            addCriterion("created_by like", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByNotLike(String value) {
            addCriterion("created_by not like", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByIn(List<String> values) {
            addCriterion("created_by in", values, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByNotIn(List<String> values) {
            addCriterion("created_by not in", values, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByBetween(String value1, String value2) {
            addCriterion("created_by between", value1, value2, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByNotBetween(String value1, String value2) {
            addCriterion("created_by not between", value1, value2, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIsNull() {
            addCriterion("created_at is null");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIsNotNull() {
            addCriterion("created_at is not null");
            return (Criteria) this;
        }

        public Criteria andCreatedAtEqualTo(LocalDateTime value) {
            addCriterion("created_at =", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotEqualTo(LocalDateTime value) {
            addCriterion("created_at <>", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtGreaterThan(LocalDateTime value) {
            addCriterion("created_at >", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("created_at >=", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtLessThan(LocalDateTime value) {
            addCriterion("created_at <", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("created_at <=", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIn(List<LocalDateTime> values) {
            addCriterion("created_at in", values, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotIn(List<LocalDateTime> values) {
            addCriterion("created_at not in", values, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("created_at between", value1, value2, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("created_at not between", value1, value2, "createdAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIsNull() {
            addCriterion("updated_at is null");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIsNotNull() {
            addCriterion("updated_at is not null");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtEqualTo(LocalDateTime value) {
            addCriterion("updated_at =", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotEqualTo(LocalDateTime value) {
            addCriterion("updated_at <>", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtGreaterThan(LocalDateTime value) {
            addCriterion("updated_at >", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("updated_at >=", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtLessThan(LocalDateTime value) {
            addCriterion("updated_at <", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("updated_at <=", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIn(List<LocalDateTime> values) {
            addCriterion("updated_at in", values, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotIn(List<LocalDateTime> values) {
            addCriterion("updated_at not in", values, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("updated_at between", value1, value2, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("updated_at not between", value1, value2, "updatedAt");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}