package com.group.admin.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrizeBoxExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PrizeBoxExample() {
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

        public Criteria andUserIdIsNull() {
            addCriterion("user_id is null");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNotNull() {
            addCriterion("user_id is not null");
            return (Criteria) this;
        }

        public Criteria andUserIdEqualTo(String value) {
            addCriterion("user_id =", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotEqualTo(String value) {
            addCriterion("user_id <>", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThan(String value) {
            addCriterion("user_id >", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThanOrEqualTo(String value) {
            addCriterion("user_id >=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThan(String value) {
            addCriterion("user_id <", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThanOrEqualTo(String value) {
            addCriterion("user_id <=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLike(String value) {
            addCriterion("user_id like", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotLike(String value) {
            addCriterion("user_id not like", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdIn(List<String> values) {
            addCriterion("user_id in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotIn(List<String> values) {
            addCriterion("user_id not in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdBetween(String value1, String value2) {
            addCriterion("user_id between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotBetween(String value1, String value2) {
            addCriterion("user_id not between", value1, value2, "userId");
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

        public Criteria andDrawResultIdIsNull() {
            addCriterion("draw_result_id is null");
            return (Criteria) this;
        }

        public Criteria andDrawResultIdIsNotNull() {
            addCriterion("draw_result_id is not null");
            return (Criteria) this;
        }

        public Criteria andDrawResultIdEqualTo(String value) {
            addCriterion("draw_result_id =", value, "drawResultId");
            return (Criteria) this;
        }

        public Criteria andDrawResultIdNotEqualTo(String value) {
            addCriterion("draw_result_id <>", value, "drawResultId");
            return (Criteria) this;
        }

        public Criteria andDrawResultIdGreaterThan(String value) {
            addCriterion("draw_result_id >", value, "drawResultId");
            return (Criteria) this;
        }

        public Criteria andDrawResultIdGreaterThanOrEqualTo(String value) {
            addCriterion("draw_result_id >=", value, "drawResultId");
            return (Criteria) this;
        }

        public Criteria andDrawResultIdLessThan(String value) {
            addCriterion("draw_result_id <", value, "drawResultId");
            return (Criteria) this;
        }

        public Criteria andDrawResultIdLessThanOrEqualTo(String value) {
            addCriterion("draw_result_id <=", value, "drawResultId");
            return (Criteria) this;
        }

        public Criteria andDrawResultIdLike(String value) {
            addCriterion("draw_result_id like", value, "drawResultId");
            return (Criteria) this;
        }

        public Criteria andDrawResultIdNotLike(String value) {
            addCriterion("draw_result_id not like", value, "drawResultId");
            return (Criteria) this;
        }

        public Criteria andDrawResultIdIn(List<String> values) {
            addCriterion("draw_result_id in", values, "drawResultId");
            return (Criteria) this;
        }

        public Criteria andDrawResultIdNotIn(List<String> values) {
            addCriterion("draw_result_id not in", values, "drawResultId");
            return (Criteria) this;
        }

        public Criteria andDrawResultIdBetween(String value1, String value2) {
            addCriterion("draw_result_id between", value1, value2, "drawResultId");
            return (Criteria) this;
        }

        public Criteria andDrawResultIdNotBetween(String value1, String value2) {
            addCriterion("draw_result_id not between", value1, value2, "drawResultId");
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

        public Criteria andIsRecyclableIsNull() {
            addCriterion("is_recyclable is null");
            return (Criteria) this;
        }

        public Criteria andIsRecyclableIsNotNull() {
            addCriterion("is_recyclable is not null");
            return (Criteria) this;
        }

        public Criteria andIsRecyclableEqualTo(Byte value) {
            addCriterion("is_recyclable =", value, "isRecyclable");
            return (Criteria) this;
        }

        public Criteria andIsRecyclableNotEqualTo(Byte value) {
            addCriterion("is_recyclable <>", value, "isRecyclable");
            return (Criteria) this;
        }

        public Criteria andIsRecyclableGreaterThan(Byte value) {
            addCriterion("is_recyclable >", value, "isRecyclable");
            return (Criteria) this;
        }

        public Criteria andIsRecyclableGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_recyclable >=", value, "isRecyclable");
            return (Criteria) this;
        }

        public Criteria andIsRecyclableLessThan(Byte value) {
            addCriterion("is_recyclable <", value, "isRecyclable");
            return (Criteria) this;
        }

        public Criteria andIsRecyclableLessThanOrEqualTo(Byte value) {
            addCriterion("is_recyclable <=", value, "isRecyclable");
            return (Criteria) this;
        }

        public Criteria andIsRecyclableIn(List<Byte> values) {
            addCriterion("is_recyclable in", values, "isRecyclable");
            return (Criteria) this;
        }

        public Criteria andIsRecyclableNotIn(List<Byte> values) {
            addCriterion("is_recyclable not in", values, "isRecyclable");
            return (Criteria) this;
        }

        public Criteria andIsRecyclableBetween(Byte value1, Byte value2) {
            addCriterion("is_recyclable between", value1, value2, "isRecyclable");
            return (Criteria) this;
        }

        public Criteria andIsRecyclableNotBetween(Byte value1, Byte value2) {
            addCriterion("is_recyclable not between", value1, value2, "isRecyclable");
            return (Criteria) this;
        }

        public Criteria andRecycleBonusIsNull() {
            addCriterion("recycle_bonus is null");
            return (Criteria) this;
        }

        public Criteria andRecycleBonusIsNotNull() {
            addCriterion("recycle_bonus is not null");
            return (Criteria) this;
        }

        public Criteria andRecycleBonusEqualTo(Long value) {
            addCriterion("recycle_bonus =", value, "recycleBonus");
            return (Criteria) this;
        }

        public Criteria andRecycleBonusNotEqualTo(Long value) {
            addCriterion("recycle_bonus <>", value, "recycleBonus");
            return (Criteria) this;
        }

        public Criteria andRecycleBonusGreaterThan(Long value) {
            addCriterion("recycle_bonus >", value, "recycleBonus");
            return (Criteria) this;
        }

        public Criteria andRecycleBonusGreaterThanOrEqualTo(Long value) {
            addCriterion("recycle_bonus >=", value, "recycleBonus");
            return (Criteria) this;
        }

        public Criteria andRecycleBonusLessThan(Long value) {
            addCriterion("recycle_bonus <", value, "recycleBonus");
            return (Criteria) this;
        }

        public Criteria andRecycleBonusLessThanOrEqualTo(Long value) {
            addCriterion("recycle_bonus <=", value, "recycleBonus");
            return (Criteria) this;
        }

        public Criteria andRecycleBonusIn(List<Long> values) {
            addCriterion("recycle_bonus in", values, "recycleBonus");
            return (Criteria) this;
        }

        public Criteria andRecycleBonusNotIn(List<Long> values) {
            addCriterion("recycle_bonus not in", values, "recycleBonus");
            return (Criteria) this;
        }

        public Criteria andRecycleBonusBetween(Long value1, Long value2) {
            addCriterion("recycle_bonus between", value1, value2, "recycleBonus");
            return (Criteria) this;
        }

        public Criteria andRecycleBonusNotBetween(Long value1, Long value2) {
            addCriterion("recycle_bonus not between", value1, value2, "recycleBonus");
            return (Criteria) this;
        }

        public Criteria andRecycledAtIsNull() {
            addCriterion("recycled_at is null");
            return (Criteria) this;
        }

        public Criteria andRecycledAtIsNotNull() {
            addCriterion("recycled_at is not null");
            return (Criteria) this;
        }

        public Criteria andRecycledAtEqualTo(LocalDateTime value) {
            addCriterion("recycled_at =", value, "recycledAt");
            return (Criteria) this;
        }

        public Criteria andRecycledAtNotEqualTo(LocalDateTime value) {
            addCriterion("recycled_at <>", value, "recycledAt");
            return (Criteria) this;
        }

        public Criteria andRecycledAtGreaterThan(LocalDateTime value) {
            addCriterion("recycled_at >", value, "recycledAt");
            return (Criteria) this;
        }

        public Criteria andRecycledAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("recycled_at >=", value, "recycledAt");
            return (Criteria) this;
        }

        public Criteria andRecycledAtLessThan(LocalDateTime value) {
            addCriterion("recycled_at <", value, "recycledAt");
            return (Criteria) this;
        }

        public Criteria andRecycledAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("recycled_at <=", value, "recycledAt");
            return (Criteria) this;
        }

        public Criteria andRecycledAtIn(List<LocalDateTime> values) {
            addCriterion("recycled_at in", values, "recycledAt");
            return (Criteria) this;
        }

        public Criteria andRecycledAtNotIn(List<LocalDateTime> values) {
            addCriterion("recycled_at not in", values, "recycledAt");
            return (Criteria) this;
        }

        public Criteria andRecycledAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("recycled_at between", value1, value2, "recycledAt");
            return (Criteria) this;
        }

        public Criteria andRecycledAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("recycled_at not between", value1, value2, "recycledAt");
            return (Criteria) this;
        }

        public Criteria andShippedAtIsNull() {
            addCriterion("shipped_at is null");
            return (Criteria) this;
        }

        public Criteria andShippedAtIsNotNull() {
            addCriterion("shipped_at is not null");
            return (Criteria) this;
        }

        public Criteria andShippedAtEqualTo(LocalDateTime value) {
            addCriterion("shipped_at =", value, "shippedAt");
            return (Criteria) this;
        }

        public Criteria andShippedAtNotEqualTo(LocalDateTime value) {
            addCriterion("shipped_at <>", value, "shippedAt");
            return (Criteria) this;
        }

        public Criteria andShippedAtGreaterThan(LocalDateTime value) {
            addCriterion("shipped_at >", value, "shippedAt");
            return (Criteria) this;
        }

        public Criteria andShippedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("shipped_at >=", value, "shippedAt");
            return (Criteria) this;
        }

        public Criteria andShippedAtLessThan(LocalDateTime value) {
            addCriterion("shipped_at <", value, "shippedAt");
            return (Criteria) this;
        }

        public Criteria andShippedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("shipped_at <=", value, "shippedAt");
            return (Criteria) this;
        }

        public Criteria andShippedAtIn(List<LocalDateTime> values) {
            addCriterion("shipped_at in", values, "shippedAt");
            return (Criteria) this;
        }

        public Criteria andShippedAtNotIn(List<LocalDateTime> values) {
            addCriterion("shipped_at not in", values, "shippedAt");
            return (Criteria) this;
        }

        public Criteria andShippedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("shipped_at between", value1, value2, "shippedAt");
            return (Criteria) this;
        }

        public Criteria andShippedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("shipped_at not between", value1, value2, "shippedAt");
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