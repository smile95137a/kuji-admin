package com.group.admin.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PointLogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PointLogExample() {
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

        public Criteria andPointTypeIsNull() {
            addCriterion("point_type is null");
            return (Criteria) this;
        }

        public Criteria andPointTypeIsNotNull() {
            addCriterion("point_type is not null");
            return (Criteria) this;
        }

        public Criteria andPointTypeEqualTo(String value) {
            addCriterion("point_type =", value, "pointType");
            return (Criteria) this;
        }

        public Criteria andPointTypeNotEqualTo(String value) {
            addCriterion("point_type <>", value, "pointType");
            return (Criteria) this;
        }

        public Criteria andPointTypeGreaterThan(String value) {
            addCriterion("point_type >", value, "pointType");
            return (Criteria) this;
        }

        public Criteria andPointTypeGreaterThanOrEqualTo(String value) {
            addCriterion("point_type >=", value, "pointType");
            return (Criteria) this;
        }

        public Criteria andPointTypeLessThan(String value) {
            addCriterion("point_type <", value, "pointType");
            return (Criteria) this;
        }

        public Criteria andPointTypeLessThanOrEqualTo(String value) {
            addCriterion("point_type <=", value, "pointType");
            return (Criteria) this;
        }

        public Criteria andPointTypeLike(String value) {
            addCriterion("point_type like", value, "pointType");
            return (Criteria) this;
        }

        public Criteria andPointTypeNotLike(String value) {
            addCriterion("point_type not like", value, "pointType");
            return (Criteria) this;
        }

        public Criteria andPointTypeIn(List<String> values) {
            addCriterion("point_type in", values, "pointType");
            return (Criteria) this;
        }

        public Criteria andPointTypeNotIn(List<String> values) {
            addCriterion("point_type not in", values, "pointType");
            return (Criteria) this;
        }

        public Criteria andPointTypeBetween(String value1, String value2) {
            addCriterion("point_type between", value1, value2, "pointType");
            return (Criteria) this;
        }

        public Criteria andPointTypeNotBetween(String value1, String value2) {
            addCriterion("point_type not between", value1, value2, "pointType");
            return (Criteria) this;
        }

        public Criteria andOperationTypeIsNull() {
            addCriterion("operation_type is null");
            return (Criteria) this;
        }

        public Criteria andOperationTypeIsNotNull() {
            addCriterion("operation_type is not null");
            return (Criteria) this;
        }

        public Criteria andOperationTypeEqualTo(String value) {
            addCriterion("operation_type =", value, "operationType");
            return (Criteria) this;
        }

        public Criteria andOperationTypeNotEqualTo(String value) {
            addCriterion("operation_type <>", value, "operationType");
            return (Criteria) this;
        }

        public Criteria andOperationTypeGreaterThan(String value) {
            addCriterion("operation_type >", value, "operationType");
            return (Criteria) this;
        }

        public Criteria andOperationTypeGreaterThanOrEqualTo(String value) {
            addCriterion("operation_type >=", value, "operationType");
            return (Criteria) this;
        }

        public Criteria andOperationTypeLessThan(String value) {
            addCriterion("operation_type <", value, "operationType");
            return (Criteria) this;
        }

        public Criteria andOperationTypeLessThanOrEqualTo(String value) {
            addCriterion("operation_type <=", value, "operationType");
            return (Criteria) this;
        }

        public Criteria andOperationTypeLike(String value) {
            addCriterion("operation_type like", value, "operationType");
            return (Criteria) this;
        }

        public Criteria andOperationTypeNotLike(String value) {
            addCriterion("operation_type not like", value, "operationType");
            return (Criteria) this;
        }

        public Criteria andOperationTypeIn(List<String> values) {
            addCriterion("operation_type in", values, "operationType");
            return (Criteria) this;
        }

        public Criteria andOperationTypeNotIn(List<String> values) {
            addCriterion("operation_type not in", values, "operationType");
            return (Criteria) this;
        }

        public Criteria andOperationTypeBetween(String value1, String value2) {
            addCriterion("operation_type between", value1, value2, "operationType");
            return (Criteria) this;
        }

        public Criteria andOperationTypeNotBetween(String value1, String value2) {
            addCriterion("operation_type not between", value1, value2, "operationType");
            return (Criteria) this;
        }

        public Criteria andAmountIsNull() {
            addCriterion("amount is null");
            return (Criteria) this;
        }

        public Criteria andAmountIsNotNull() {
            addCriterion("amount is not null");
            return (Criteria) this;
        }

        public Criteria andAmountEqualTo(Long value) {
            addCriterion("amount =", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountNotEqualTo(Long value) {
            addCriterion("amount <>", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountGreaterThan(Long value) {
            addCriterion("amount >", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountGreaterThanOrEqualTo(Long value) {
            addCriterion("amount >=", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountLessThan(Long value) {
            addCriterion("amount <", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountLessThanOrEqualTo(Long value) {
            addCriterion("amount <=", value, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountIn(List<Long> values) {
            addCriterion("amount in", values, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountNotIn(List<Long> values) {
            addCriterion("amount not in", values, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountBetween(Long value1, Long value2) {
            addCriterion("amount between", value1, value2, "amount");
            return (Criteria) this;
        }

        public Criteria andAmountNotBetween(Long value1, Long value2) {
            addCriterion("amount not between", value1, value2, "amount");
            return (Criteria) this;
        }

        public Criteria andBeforeBalanceIsNull() {
            addCriterion("before_balance is null");
            return (Criteria) this;
        }

        public Criteria andBeforeBalanceIsNotNull() {
            addCriterion("before_balance is not null");
            return (Criteria) this;
        }

        public Criteria andBeforeBalanceEqualTo(Long value) {
            addCriterion("before_balance =", value, "beforeBalance");
            return (Criteria) this;
        }

        public Criteria andBeforeBalanceNotEqualTo(Long value) {
            addCriterion("before_balance <>", value, "beforeBalance");
            return (Criteria) this;
        }

        public Criteria andBeforeBalanceGreaterThan(Long value) {
            addCriterion("before_balance >", value, "beforeBalance");
            return (Criteria) this;
        }

        public Criteria andBeforeBalanceGreaterThanOrEqualTo(Long value) {
            addCriterion("before_balance >=", value, "beforeBalance");
            return (Criteria) this;
        }

        public Criteria andBeforeBalanceLessThan(Long value) {
            addCriterion("before_balance <", value, "beforeBalance");
            return (Criteria) this;
        }

        public Criteria andBeforeBalanceLessThanOrEqualTo(Long value) {
            addCriterion("before_balance <=", value, "beforeBalance");
            return (Criteria) this;
        }

        public Criteria andBeforeBalanceIn(List<Long> values) {
            addCriterion("before_balance in", values, "beforeBalance");
            return (Criteria) this;
        }

        public Criteria andBeforeBalanceNotIn(List<Long> values) {
            addCriterion("before_balance not in", values, "beforeBalance");
            return (Criteria) this;
        }

        public Criteria andBeforeBalanceBetween(Long value1, Long value2) {
            addCriterion("before_balance between", value1, value2, "beforeBalance");
            return (Criteria) this;
        }

        public Criteria andBeforeBalanceNotBetween(Long value1, Long value2) {
            addCriterion("before_balance not between", value1, value2, "beforeBalance");
            return (Criteria) this;
        }

        public Criteria andAfterBalanceIsNull() {
            addCriterion("after_balance is null");
            return (Criteria) this;
        }

        public Criteria andAfterBalanceIsNotNull() {
            addCriterion("after_balance is not null");
            return (Criteria) this;
        }

        public Criteria andAfterBalanceEqualTo(Long value) {
            addCriterion("after_balance =", value, "afterBalance");
            return (Criteria) this;
        }

        public Criteria andAfterBalanceNotEqualTo(Long value) {
            addCriterion("after_balance <>", value, "afterBalance");
            return (Criteria) this;
        }

        public Criteria andAfterBalanceGreaterThan(Long value) {
            addCriterion("after_balance >", value, "afterBalance");
            return (Criteria) this;
        }

        public Criteria andAfterBalanceGreaterThanOrEqualTo(Long value) {
            addCriterion("after_balance >=", value, "afterBalance");
            return (Criteria) this;
        }

        public Criteria andAfterBalanceLessThan(Long value) {
            addCriterion("after_balance <", value, "afterBalance");
            return (Criteria) this;
        }

        public Criteria andAfterBalanceLessThanOrEqualTo(Long value) {
            addCriterion("after_balance <=", value, "afterBalance");
            return (Criteria) this;
        }

        public Criteria andAfterBalanceIn(List<Long> values) {
            addCriterion("after_balance in", values, "afterBalance");
            return (Criteria) this;
        }

        public Criteria andAfterBalanceNotIn(List<Long> values) {
            addCriterion("after_balance not in", values, "afterBalance");
            return (Criteria) this;
        }

        public Criteria andAfterBalanceBetween(Long value1, Long value2) {
            addCriterion("after_balance between", value1, value2, "afterBalance");
            return (Criteria) this;
        }

        public Criteria andAfterBalanceNotBetween(Long value1, Long value2) {
            addCriterion("after_balance not between", value1, value2, "afterBalance");
            return (Criteria) this;
        }

        public Criteria andReferenceTypeIsNull() {
            addCriterion("reference_type is null");
            return (Criteria) this;
        }

        public Criteria andReferenceTypeIsNotNull() {
            addCriterion("reference_type is not null");
            return (Criteria) this;
        }

        public Criteria andReferenceTypeEqualTo(String value) {
            addCriterion("reference_type =", value, "referenceType");
            return (Criteria) this;
        }

        public Criteria andReferenceTypeNotEqualTo(String value) {
            addCriterion("reference_type <>", value, "referenceType");
            return (Criteria) this;
        }

        public Criteria andReferenceTypeGreaterThan(String value) {
            addCriterion("reference_type >", value, "referenceType");
            return (Criteria) this;
        }

        public Criteria andReferenceTypeGreaterThanOrEqualTo(String value) {
            addCriterion("reference_type >=", value, "referenceType");
            return (Criteria) this;
        }

        public Criteria andReferenceTypeLessThan(String value) {
            addCriterion("reference_type <", value, "referenceType");
            return (Criteria) this;
        }

        public Criteria andReferenceTypeLessThanOrEqualTo(String value) {
            addCriterion("reference_type <=", value, "referenceType");
            return (Criteria) this;
        }

        public Criteria andReferenceTypeLike(String value) {
            addCriterion("reference_type like", value, "referenceType");
            return (Criteria) this;
        }

        public Criteria andReferenceTypeNotLike(String value) {
            addCriterion("reference_type not like", value, "referenceType");
            return (Criteria) this;
        }

        public Criteria andReferenceTypeIn(List<String> values) {
            addCriterion("reference_type in", values, "referenceType");
            return (Criteria) this;
        }

        public Criteria andReferenceTypeNotIn(List<String> values) {
            addCriterion("reference_type not in", values, "referenceType");
            return (Criteria) this;
        }

        public Criteria andReferenceTypeBetween(String value1, String value2) {
            addCriterion("reference_type between", value1, value2, "referenceType");
            return (Criteria) this;
        }

        public Criteria andReferenceTypeNotBetween(String value1, String value2) {
            addCriterion("reference_type not between", value1, value2, "referenceType");
            return (Criteria) this;
        }

        public Criteria andReferenceIdIsNull() {
            addCriterion("reference_id is null");
            return (Criteria) this;
        }

        public Criteria andReferenceIdIsNotNull() {
            addCriterion("reference_id is not null");
            return (Criteria) this;
        }

        public Criteria andReferenceIdEqualTo(String value) {
            addCriterion("reference_id =", value, "referenceId");
            return (Criteria) this;
        }

        public Criteria andReferenceIdNotEqualTo(String value) {
            addCriterion("reference_id <>", value, "referenceId");
            return (Criteria) this;
        }

        public Criteria andReferenceIdGreaterThan(String value) {
            addCriterion("reference_id >", value, "referenceId");
            return (Criteria) this;
        }

        public Criteria andReferenceIdGreaterThanOrEqualTo(String value) {
            addCriterion("reference_id >=", value, "referenceId");
            return (Criteria) this;
        }

        public Criteria andReferenceIdLessThan(String value) {
            addCriterion("reference_id <", value, "referenceId");
            return (Criteria) this;
        }

        public Criteria andReferenceIdLessThanOrEqualTo(String value) {
            addCriterion("reference_id <=", value, "referenceId");
            return (Criteria) this;
        }

        public Criteria andReferenceIdLike(String value) {
            addCriterion("reference_id like", value, "referenceId");
            return (Criteria) this;
        }

        public Criteria andReferenceIdNotLike(String value) {
            addCriterion("reference_id not like", value, "referenceId");
            return (Criteria) this;
        }

        public Criteria andReferenceIdIn(List<String> values) {
            addCriterion("reference_id in", values, "referenceId");
            return (Criteria) this;
        }

        public Criteria andReferenceIdNotIn(List<String> values) {
            addCriterion("reference_id not in", values, "referenceId");
            return (Criteria) this;
        }

        public Criteria andReferenceIdBetween(String value1, String value2) {
            addCriterion("reference_id between", value1, value2, "referenceId");
            return (Criteria) this;
        }

        public Criteria andReferenceIdNotBetween(String value1, String value2) {
            addCriterion("reference_id not between", value1, value2, "referenceId");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNull() {
            addCriterion("remark is null");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNotNull() {
            addCriterion("remark is not null");
            return (Criteria) this;
        }

        public Criteria andRemarkEqualTo(String value) {
            addCriterion("remark =", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotEqualTo(String value) {
            addCriterion("remark <>", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThan(String value) {
            addCriterion("remark >", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThanOrEqualTo(String value) {
            addCriterion("remark >=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThan(String value) {
            addCriterion("remark <", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThanOrEqualTo(String value) {
            addCriterion("remark <=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLike(String value) {
            addCriterion("remark like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotLike(String value) {
            addCriterion("remark not like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkIn(List<String> values) {
            addCriterion("remark in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotIn(List<String> values) {
            addCriterion("remark not in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkBetween(String value1, String value2) {
            addCriterion("remark between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotBetween(String value1, String value2) {
            addCriterion("remark not between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andExpireAtIsNull() {
            addCriterion("expire_at is null");
            return (Criteria) this;
        }

        public Criteria andExpireAtIsNotNull() {
            addCriterion("expire_at is not null");
            return (Criteria) this;
        }

        public Criteria andExpireAtEqualTo(LocalDateTime value) {
            addCriterion("expire_at =", value, "expireAt");
            return (Criteria) this;
        }

        public Criteria andExpireAtNotEqualTo(LocalDateTime value) {
            addCriterion("expire_at <>", value, "expireAt");
            return (Criteria) this;
        }

        public Criteria andExpireAtGreaterThan(LocalDateTime value) {
            addCriterion("expire_at >", value, "expireAt");
            return (Criteria) this;
        }

        public Criteria andExpireAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("expire_at >=", value, "expireAt");
            return (Criteria) this;
        }

        public Criteria andExpireAtLessThan(LocalDateTime value) {
            addCriterion("expire_at <", value, "expireAt");
            return (Criteria) this;
        }

        public Criteria andExpireAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("expire_at <=", value, "expireAt");
            return (Criteria) this;
        }

        public Criteria andExpireAtIn(List<LocalDateTime> values) {
            addCriterion("expire_at in", values, "expireAt");
            return (Criteria) this;
        }

        public Criteria andExpireAtNotIn(List<LocalDateTime> values) {
            addCriterion("expire_at not in", values, "expireAt");
            return (Criteria) this;
        }

        public Criteria andExpireAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("expire_at between", value1, value2, "expireAt");
            return (Criteria) this;
        }

        public Criteria andExpireAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("expire_at not between", value1, value2, "expireAt");
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