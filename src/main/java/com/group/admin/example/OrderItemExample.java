package com.group.admin.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderItemExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public OrderItemExample() {
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

        public Criteria andOrderIdIsNull() {
            addCriterion("order_id is null");
            return (Criteria) this;
        }

        public Criteria andOrderIdIsNotNull() {
            addCriterion("order_id is not null");
            return (Criteria) this;
        }

        public Criteria andOrderIdEqualTo(String value) {
            addCriterion("order_id =", value, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdNotEqualTo(String value) {
            addCriterion("order_id <>", value, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdGreaterThan(String value) {
            addCriterion("order_id >", value, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdGreaterThanOrEqualTo(String value) {
            addCriterion("order_id >=", value, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdLessThan(String value) {
            addCriterion("order_id <", value, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdLessThanOrEqualTo(String value) {
            addCriterion("order_id <=", value, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdLike(String value) {
            addCriterion("order_id like", value, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdNotLike(String value) {
            addCriterion("order_id not like", value, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdIn(List<String> values) {
            addCriterion("order_id in", values, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdNotIn(List<String> values) {
            addCriterion("order_id not in", values, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdBetween(String value1, String value2) {
            addCriterion("order_id between", value1, value2, "orderId");
            return (Criteria) this;
        }

        public Criteria andOrderIdNotBetween(String value1, String value2) {
            addCriterion("order_id not between", value1, value2, "orderId");
            return (Criteria) this;
        }

        public Criteria andPrizeBoxIdIsNull() {
            addCriterion("prize_box_id is null");
            return (Criteria) this;
        }

        public Criteria andPrizeBoxIdIsNotNull() {
            addCriterion("prize_box_id is not null");
            return (Criteria) this;
        }

        public Criteria andPrizeBoxIdEqualTo(String value) {
            addCriterion("prize_box_id =", value, "prizeBoxId");
            return (Criteria) this;
        }

        public Criteria andPrizeBoxIdNotEqualTo(String value) {
            addCriterion("prize_box_id <>", value, "prizeBoxId");
            return (Criteria) this;
        }

        public Criteria andPrizeBoxIdGreaterThan(String value) {
            addCriterion("prize_box_id >", value, "prizeBoxId");
            return (Criteria) this;
        }

        public Criteria andPrizeBoxIdGreaterThanOrEqualTo(String value) {
            addCriterion("prize_box_id >=", value, "prizeBoxId");
            return (Criteria) this;
        }

        public Criteria andPrizeBoxIdLessThan(String value) {
            addCriterion("prize_box_id <", value, "prizeBoxId");
            return (Criteria) this;
        }

        public Criteria andPrizeBoxIdLessThanOrEqualTo(String value) {
            addCriterion("prize_box_id <=", value, "prizeBoxId");
            return (Criteria) this;
        }

        public Criteria andPrizeBoxIdLike(String value) {
            addCriterion("prize_box_id like", value, "prizeBoxId");
            return (Criteria) this;
        }

        public Criteria andPrizeBoxIdNotLike(String value) {
            addCriterion("prize_box_id not like", value, "prizeBoxId");
            return (Criteria) this;
        }

        public Criteria andPrizeBoxIdIn(List<String> values) {
            addCriterion("prize_box_id in", values, "prizeBoxId");
            return (Criteria) this;
        }

        public Criteria andPrizeBoxIdNotIn(List<String> values) {
            addCriterion("prize_box_id not in", values, "prizeBoxId");
            return (Criteria) this;
        }

        public Criteria andPrizeBoxIdBetween(String value1, String value2) {
            addCriterion("prize_box_id between", value1, value2, "prizeBoxId");
            return (Criteria) this;
        }

        public Criteria andPrizeBoxIdNotBetween(String value1, String value2) {
            addCriterion("prize_box_id not between", value1, value2, "prizeBoxId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdIsNull() {
            addCriterion("lottery_id is null");
            return (Criteria) this;
        }

        public Criteria andLotteryIdIsNotNull() {
            addCriterion("lottery_id is not null");
            return (Criteria) this;
        }

        public Criteria andLotteryIdEqualTo(String value) {
            addCriterion("lottery_id =", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdNotEqualTo(String value) {
            addCriterion("lottery_id <>", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdGreaterThan(String value) {
            addCriterion("lottery_id >", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdGreaterThanOrEqualTo(String value) {
            addCriterion("lottery_id >=", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdLessThan(String value) {
            addCriterion("lottery_id <", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdLessThanOrEqualTo(String value) {
            addCriterion("lottery_id <=", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdLike(String value) {
            addCriterion("lottery_id like", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdNotLike(String value) {
            addCriterion("lottery_id not like", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdIn(List<String> values) {
            addCriterion("lottery_id in", values, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdNotIn(List<String> values) {
            addCriterion("lottery_id not in", values, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdBetween(String value1, String value2) {
            addCriterion("lottery_id between", value1, value2, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdNotBetween(String value1, String value2) {
            addCriterion("lottery_id not between", value1, value2, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryTitleIsNull() {
            addCriterion("lottery_title is null");
            return (Criteria) this;
        }

        public Criteria andLotteryTitleIsNotNull() {
            addCriterion("lottery_title is not null");
            return (Criteria) this;
        }

        public Criteria andLotteryTitleEqualTo(String value) {
            addCriterion("lottery_title =", value, "lotteryTitle");
            return (Criteria) this;
        }

        public Criteria andLotteryTitleNotEqualTo(String value) {
            addCriterion("lottery_title <>", value, "lotteryTitle");
            return (Criteria) this;
        }

        public Criteria andLotteryTitleGreaterThan(String value) {
            addCriterion("lottery_title >", value, "lotteryTitle");
            return (Criteria) this;
        }

        public Criteria andLotteryTitleGreaterThanOrEqualTo(String value) {
            addCriterion("lottery_title >=", value, "lotteryTitle");
            return (Criteria) this;
        }

        public Criteria andLotteryTitleLessThan(String value) {
            addCriterion("lottery_title <", value, "lotteryTitle");
            return (Criteria) this;
        }

        public Criteria andLotteryTitleLessThanOrEqualTo(String value) {
            addCriterion("lottery_title <=", value, "lotteryTitle");
            return (Criteria) this;
        }

        public Criteria andLotteryTitleLike(String value) {
            addCriterion("lottery_title like", value, "lotteryTitle");
            return (Criteria) this;
        }

        public Criteria andLotteryTitleNotLike(String value) {
            addCriterion("lottery_title not like", value, "lotteryTitle");
            return (Criteria) this;
        }

        public Criteria andLotteryTitleIn(List<String> values) {
            addCriterion("lottery_title in", values, "lotteryTitle");
            return (Criteria) this;
        }

        public Criteria andLotteryTitleNotIn(List<String> values) {
            addCriterion("lottery_title not in", values, "lotteryTitle");
            return (Criteria) this;
        }

        public Criteria andLotteryTitleBetween(String value1, String value2) {
            addCriterion("lottery_title between", value1, value2, "lotteryTitle");
            return (Criteria) this;
        }

        public Criteria andLotteryTitleNotBetween(String value1, String value2) {
            addCriterion("lottery_title not between", value1, value2, "lotteryTitle");
            return (Criteria) this;
        }

        public Criteria andLotteryImageUrlIsNull() {
            addCriterion("lottery_image_url is null");
            return (Criteria) this;
        }

        public Criteria andLotteryImageUrlIsNotNull() {
            addCriterion("lottery_image_url is not null");
            return (Criteria) this;
        }

        public Criteria andLotteryImageUrlEqualTo(String value) {
            addCriterion("lottery_image_url =", value, "lotteryImageUrl");
            return (Criteria) this;
        }

        public Criteria andLotteryImageUrlNotEqualTo(String value) {
            addCriterion("lottery_image_url <>", value, "lotteryImageUrl");
            return (Criteria) this;
        }

        public Criteria andLotteryImageUrlGreaterThan(String value) {
            addCriterion("lottery_image_url >", value, "lotteryImageUrl");
            return (Criteria) this;
        }

        public Criteria andLotteryImageUrlGreaterThanOrEqualTo(String value) {
            addCriterion("lottery_image_url >=", value, "lotteryImageUrl");
            return (Criteria) this;
        }

        public Criteria andLotteryImageUrlLessThan(String value) {
            addCriterion("lottery_image_url <", value, "lotteryImageUrl");
            return (Criteria) this;
        }

        public Criteria andLotteryImageUrlLessThanOrEqualTo(String value) {
            addCriterion("lottery_image_url <=", value, "lotteryImageUrl");
            return (Criteria) this;
        }

        public Criteria andLotteryImageUrlLike(String value) {
            addCriterion("lottery_image_url like", value, "lotteryImageUrl");
            return (Criteria) this;
        }

        public Criteria andLotteryImageUrlNotLike(String value) {
            addCriterion("lottery_image_url not like", value, "lotteryImageUrl");
            return (Criteria) this;
        }

        public Criteria andLotteryImageUrlIn(List<String> values) {
            addCriterion("lottery_image_url in", values, "lotteryImageUrl");
            return (Criteria) this;
        }

        public Criteria andLotteryImageUrlNotIn(List<String> values) {
            addCriterion("lottery_image_url not in", values, "lotteryImageUrl");
            return (Criteria) this;
        }

        public Criteria andLotteryImageUrlBetween(String value1, String value2) {
            addCriterion("lottery_image_url between", value1, value2, "lotteryImageUrl");
            return (Criteria) this;
        }

        public Criteria andLotteryImageUrlNotBetween(String value1, String value2) {
            addCriterion("lottery_image_url not between", value1, value2, "lotteryImageUrl");
            return (Criteria) this;
        }

        public Criteria andPrizeIdIsNull() {
            addCriterion("prize_id is null");
            return (Criteria) this;
        }

        public Criteria andPrizeIdIsNotNull() {
            addCriterion("prize_id is not null");
            return (Criteria) this;
        }

        public Criteria andPrizeIdEqualTo(String value) {
            addCriterion("prize_id =", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdNotEqualTo(String value) {
            addCriterion("prize_id <>", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdGreaterThan(String value) {
            addCriterion("prize_id >", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdGreaterThanOrEqualTo(String value) {
            addCriterion("prize_id >=", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdLessThan(String value) {
            addCriterion("prize_id <", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdLessThanOrEqualTo(String value) {
            addCriterion("prize_id <=", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdLike(String value) {
            addCriterion("prize_id like", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdNotLike(String value) {
            addCriterion("prize_id not like", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdIn(List<String> values) {
            addCriterion("prize_id in", values, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdNotIn(List<String> values) {
            addCriterion("prize_id not in", values, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdBetween(String value1, String value2) {
            addCriterion("prize_id between", value1, value2, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdNotBetween(String value1, String value2) {
            addCriterion("prize_id not between", value1, value2, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeNameIsNull() {
            addCriterion("prize_name is null");
            return (Criteria) this;
        }

        public Criteria andPrizeNameIsNotNull() {
            addCriterion("prize_name is not null");
            return (Criteria) this;
        }

        public Criteria andPrizeNameEqualTo(String value) {
            addCriterion("prize_name =", value, "prizeName");
            return (Criteria) this;
        }

        public Criteria andPrizeNameNotEqualTo(String value) {
            addCriterion("prize_name <>", value, "prizeName");
            return (Criteria) this;
        }

        public Criteria andPrizeNameGreaterThan(String value) {
            addCriterion("prize_name >", value, "prizeName");
            return (Criteria) this;
        }

        public Criteria andPrizeNameGreaterThanOrEqualTo(String value) {
            addCriterion("prize_name >=", value, "prizeName");
            return (Criteria) this;
        }

        public Criteria andPrizeNameLessThan(String value) {
            addCriterion("prize_name <", value, "prizeName");
            return (Criteria) this;
        }

        public Criteria andPrizeNameLessThanOrEqualTo(String value) {
            addCriterion("prize_name <=", value, "prizeName");
            return (Criteria) this;
        }

        public Criteria andPrizeNameLike(String value) {
            addCriterion("prize_name like", value, "prizeName");
            return (Criteria) this;
        }

        public Criteria andPrizeNameNotLike(String value) {
            addCriterion("prize_name not like", value, "prizeName");
            return (Criteria) this;
        }

        public Criteria andPrizeNameIn(List<String> values) {
            addCriterion("prize_name in", values, "prizeName");
            return (Criteria) this;
        }

        public Criteria andPrizeNameNotIn(List<String> values) {
            addCriterion("prize_name not in", values, "prizeName");
            return (Criteria) this;
        }

        public Criteria andPrizeNameBetween(String value1, String value2) {
            addCriterion("prize_name between", value1, value2, "prizeName");
            return (Criteria) this;
        }

        public Criteria andPrizeNameNotBetween(String value1, String value2) {
            addCriterion("prize_name not between", value1, value2, "prizeName");
            return (Criteria) this;
        }

        public Criteria andPrizeGradeIsNull() {
            addCriterion("prize_grade is null");
            return (Criteria) this;
        }

        public Criteria andPrizeGradeIsNotNull() {
            addCriterion("prize_grade is not null");
            return (Criteria) this;
        }

        public Criteria andPrizeGradeEqualTo(String value) {
            addCriterion("prize_grade =", value, "prizeGrade");
            return (Criteria) this;
        }

        public Criteria andPrizeGradeNotEqualTo(String value) {
            addCriterion("prize_grade <>", value, "prizeGrade");
            return (Criteria) this;
        }

        public Criteria andPrizeGradeGreaterThan(String value) {
            addCriterion("prize_grade >", value, "prizeGrade");
            return (Criteria) this;
        }

        public Criteria andPrizeGradeGreaterThanOrEqualTo(String value) {
            addCriterion("prize_grade >=", value, "prizeGrade");
            return (Criteria) this;
        }

        public Criteria andPrizeGradeLessThan(String value) {
            addCriterion("prize_grade <", value, "prizeGrade");
            return (Criteria) this;
        }

        public Criteria andPrizeGradeLessThanOrEqualTo(String value) {
            addCriterion("prize_grade <=", value, "prizeGrade");
            return (Criteria) this;
        }

        public Criteria andPrizeGradeLike(String value) {
            addCriterion("prize_grade like", value, "prizeGrade");
            return (Criteria) this;
        }

        public Criteria andPrizeGradeNotLike(String value) {
            addCriterion("prize_grade not like", value, "prizeGrade");
            return (Criteria) this;
        }

        public Criteria andPrizeGradeIn(List<String> values) {
            addCriterion("prize_grade in", values, "prizeGrade");
            return (Criteria) this;
        }

        public Criteria andPrizeGradeNotIn(List<String> values) {
            addCriterion("prize_grade not in", values, "prizeGrade");
            return (Criteria) this;
        }

        public Criteria andPrizeGradeBetween(String value1, String value2) {
            addCriterion("prize_grade between", value1, value2, "prizeGrade");
            return (Criteria) this;
        }

        public Criteria andPrizeGradeNotBetween(String value1, String value2) {
            addCriterion("prize_grade not between", value1, value2, "prizeGrade");
            return (Criteria) this;
        }

        public Criteria andPrizeImageIsNull() {
            addCriterion("prize_image is null");
            return (Criteria) this;
        }

        public Criteria andPrizeImageIsNotNull() {
            addCriterion("prize_image is not null");
            return (Criteria) this;
        }

        public Criteria andPrizeImageEqualTo(String value) {
            addCriterion("prize_image =", value, "prizeImage");
            return (Criteria) this;
        }

        public Criteria andPrizeImageNotEqualTo(String value) {
            addCriterion("prize_image <>", value, "prizeImage");
            return (Criteria) this;
        }

        public Criteria andPrizeImageGreaterThan(String value) {
            addCriterion("prize_image >", value, "prizeImage");
            return (Criteria) this;
        }

        public Criteria andPrizeImageGreaterThanOrEqualTo(String value) {
            addCriterion("prize_image >=", value, "prizeImage");
            return (Criteria) this;
        }

        public Criteria andPrizeImageLessThan(String value) {
            addCriterion("prize_image <", value, "prizeImage");
            return (Criteria) this;
        }

        public Criteria andPrizeImageLessThanOrEqualTo(String value) {
            addCriterion("prize_image <=", value, "prizeImage");
            return (Criteria) this;
        }

        public Criteria andPrizeImageLike(String value) {
            addCriterion("prize_image like", value, "prizeImage");
            return (Criteria) this;
        }

        public Criteria andPrizeImageNotLike(String value) {
            addCriterion("prize_image not like", value, "prizeImage");
            return (Criteria) this;
        }

        public Criteria andPrizeImageIn(List<String> values) {
            addCriterion("prize_image in", values, "prizeImage");
            return (Criteria) this;
        }

        public Criteria andPrizeImageNotIn(List<String> values) {
            addCriterion("prize_image not in", values, "prizeImage");
            return (Criteria) this;
        }

        public Criteria andPrizeImageBetween(String value1, String value2) {
            addCriterion("prize_image between", value1, value2, "prizeImage");
            return (Criteria) this;
        }

        public Criteria andPrizeImageNotBetween(String value1, String value2) {
            addCriterion("prize_image not between", value1, value2, "prizeImage");
            return (Criteria) this;
        }

        public Criteria andPrizeImageUrlIsNull() {
            addCriterion("prize_image_url is null");
            return (Criteria) this;
        }

        public Criteria andPrizeImageUrlIsNotNull() {
            addCriterion("prize_image_url is not null");
            return (Criteria) this;
        }

        public Criteria andPrizeImageUrlEqualTo(String value) {
            addCriterion("prize_image_url =", value, "prizeImageUrl");
            return (Criteria) this;
        }

        public Criteria andPrizeImageUrlNotEqualTo(String value) {
            addCriterion("prize_image_url <>", value, "prizeImageUrl");
            return (Criteria) this;
        }

        public Criteria andPrizeImageUrlGreaterThan(String value) {
            addCriterion("prize_image_url >", value, "prizeImageUrl");
            return (Criteria) this;
        }

        public Criteria andPrizeImageUrlGreaterThanOrEqualTo(String value) {
            addCriterion("prize_image_url >=", value, "prizeImageUrl");
            return (Criteria) this;
        }

        public Criteria andPrizeImageUrlLessThan(String value) {
            addCriterion("prize_image_url <", value, "prizeImageUrl");
            return (Criteria) this;
        }

        public Criteria andPrizeImageUrlLessThanOrEqualTo(String value) {
            addCriterion("prize_image_url <=", value, "prizeImageUrl");
            return (Criteria) this;
        }

        public Criteria andPrizeImageUrlLike(String value) {
            addCriterion("prize_image_url like", value, "prizeImageUrl");
            return (Criteria) this;
        }

        public Criteria andPrizeImageUrlNotLike(String value) {
            addCriterion("prize_image_url not like", value, "prizeImageUrl");
            return (Criteria) this;
        }

        public Criteria andPrizeImageUrlIn(List<String> values) {
            addCriterion("prize_image_url in", values, "prizeImageUrl");
            return (Criteria) this;
        }

        public Criteria andPrizeImageUrlNotIn(List<String> values) {
            addCriterion("prize_image_url not in", values, "prizeImageUrl");
            return (Criteria) this;
        }

        public Criteria andPrizeImageUrlBetween(String value1, String value2) {
            addCriterion("prize_image_url between", value1, value2, "prizeImageUrl");
            return (Criteria) this;
        }

        public Criteria andPrizeImageUrlNotBetween(String value1, String value2) {
            addCriterion("prize_image_url not between", value1, value2, "prizeImageUrl");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelIsNull() {
            addCriterion("prize_level is null");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelIsNotNull() {
            addCriterion("prize_level is not null");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelEqualTo(String value) {
            addCriterion("prize_level =", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelNotEqualTo(String value) {
            addCriterion("prize_level <>", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelGreaterThan(String value) {
            addCriterion("prize_level >", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelGreaterThanOrEqualTo(String value) {
            addCriterion("prize_level >=", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelLessThan(String value) {
            addCriterion("prize_level <", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelLessThanOrEqualTo(String value) {
            addCriterion("prize_level <=", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelLike(String value) {
            addCriterion("prize_level like", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelNotLike(String value) {
            addCriterion("prize_level not like", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelIn(List<String> values) {
            addCriterion("prize_level in", values, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelNotIn(List<String> values) {
            addCriterion("prize_level not in", values, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelBetween(String value1, String value2) {
            addCriterion("prize_level between", value1, value2, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelNotBetween(String value1, String value2) {
            addCriterion("prize_level not between", value1, value2, "prizeLevel");
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