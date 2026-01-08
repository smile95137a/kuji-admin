package com.group.admin.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserWalletExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public UserWalletExample() {
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

        public Criteria andGoldCoinsIsNull() {
            addCriterion("gold_coins is null");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsIsNotNull() {
            addCriterion("gold_coins is not null");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsEqualTo(Long value) {
            addCriterion("gold_coins =", value, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsNotEqualTo(Long value) {
            addCriterion("gold_coins <>", value, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsGreaterThan(Long value) {
            addCriterion("gold_coins >", value, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsGreaterThanOrEqualTo(Long value) {
            addCriterion("gold_coins >=", value, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsLessThan(Long value) {
            addCriterion("gold_coins <", value, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsLessThanOrEqualTo(Long value) {
            addCriterion("gold_coins <=", value, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsIn(List<Long> values) {
            addCriterion("gold_coins in", values, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsNotIn(List<Long> values) {
            addCriterion("gold_coins not in", values, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsBetween(Long value1, Long value2) {
            addCriterion("gold_coins between", value1, value2, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsNotBetween(Long value1, Long value2) {
            addCriterion("gold_coins not between", value1, value2, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsIsNull() {
            addCriterion("bonus_coins is null");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsIsNotNull() {
            addCriterion("bonus_coins is not null");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsEqualTo(Long value) {
            addCriterion("bonus_coins =", value, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsNotEqualTo(Long value) {
            addCriterion("bonus_coins <>", value, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsGreaterThan(Long value) {
            addCriterion("bonus_coins >", value, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsGreaterThanOrEqualTo(Long value) {
            addCriterion("bonus_coins >=", value, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsLessThan(Long value) {
            addCriterion("bonus_coins <", value, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsLessThanOrEqualTo(Long value) {
            addCriterion("bonus_coins <=", value, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsIn(List<Long> values) {
            addCriterion("bonus_coins in", values, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsNotIn(List<Long> values) {
            addCriterion("bonus_coins not in", values, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsBetween(Long value1, Long value2) {
            addCriterion("bonus_coins between", value1, value2, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsNotBetween(Long value1, Long value2) {
            addCriterion("bonus_coins not between", value1, value2, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedIsNull() {
            addCriterion("total_recharged is null");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedIsNotNull() {
            addCriterion("total_recharged is not null");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedEqualTo(Long value) {
            addCriterion("total_recharged =", value, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedNotEqualTo(Long value) {
            addCriterion("total_recharged <>", value, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedGreaterThan(Long value) {
            addCriterion("total_recharged >", value, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedGreaterThanOrEqualTo(Long value) {
            addCriterion("total_recharged >=", value, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedLessThan(Long value) {
            addCriterion("total_recharged <", value, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedLessThanOrEqualTo(Long value) {
            addCriterion("total_recharged <=", value, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedIn(List<Long> values) {
            addCriterion("total_recharged in", values, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedNotIn(List<Long> values) {
            addCriterion("total_recharged not in", values, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedBetween(Long value1, Long value2) {
            addCriterion("total_recharged between", value1, value2, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedNotBetween(Long value1, Long value2) {
            addCriterion("total_recharged not between", value1, value2, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andVersionIsNull() {
            addCriterion("version is null");
            return (Criteria) this;
        }

        public Criteria andVersionIsNotNull() {
            addCriterion("version is not null");
            return (Criteria) this;
        }

        public Criteria andVersionEqualTo(Integer value) {
            addCriterion("version =", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualTo(Integer value) {
            addCriterion("version <>", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThan(Integer value) {
            addCriterion("version >", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualTo(Integer value) {
            addCriterion("version >=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThan(Integer value) {
            addCriterion("version <", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualTo(Integer value) {
            addCriterion("version <=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionIn(List<Integer> values) {
            addCriterion("version in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotIn(List<Integer> values) {
            addCriterion("version not in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionBetween(Integer value1, Integer value2) {
            addCriterion("version between", value1, value2, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotBetween(Integer value1, Integer value2) {
            addCriterion("version not between", value1, value2, "version");
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