package com.group.admin.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserLoginHistoryExample {
    protected String orderByClause;
    protected boolean distinct;
    protected List<Criteria> oredCriteria;

    public UserLoginHistoryExample() { oredCriteria = new ArrayList<>(); }
    public void setOrderByClause(String orderByClause) { this.orderByClause = orderByClause; }
    public String getOrderByClause() { return orderByClause; }
    public void setDistinct(boolean distinct) { this.distinct = distinct; }
    public boolean isDistinct() { return distinct; }
    public List<Criteria> getOredCriteria() { return oredCriteria; }
    public void or(Criteria criteria) { oredCriteria.add(criteria); }
    public Criteria or() { Criteria c = createCriteriaInternal(); oredCriteria.add(c); return c; }
    public Criteria createCriteria() { Criteria c = createCriteriaInternal(); if (oredCriteria.size() == 0) oredCriteria.add(c); return c; }
    protected Criteria createCriteriaInternal() { return new Criteria(); }
    public void clear() { oredCriteria.clear(); orderByClause = null; distinct = false; }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;
        protected GeneratedCriteria() { super(); criteria = new ArrayList<>(); }
        public boolean isValid() { return criteria.size() > 0; }
        public List<Criterion> getAllCriteria() { return criteria; }
        public List<Criterion> getCriteria() { return criteria; }
        protected void addCriterion(String condition) { if (condition == null) throw new RuntimeException("Value for condition cannot be null"); criteria.add(new Criterion(condition)); }
        protected void addCriterion(String condition, Object value, String property) { if (value == null) throw new RuntimeException("Value for " + property + " cannot be null"); criteria.add(new Criterion(condition, value)); }
        protected void addCriterion(String condition, Object value1, Object value2, String property) { if (value1 == null || value2 == null) throw new RuntimeException("Between values for " + property + " cannot be null"); criteria.add(new Criterion(condition, value1, value2)); }

        // id
        public Criteria andIdIsNull() { addCriterion("id is null"); return (Criteria) this; }
        public Criteria andIdIsNotNull() { addCriterion("id is not null"); return (Criteria) this; }
        public Criteria andIdEqualTo(String v) { addCriterion("id =", v, "id"); return (Criteria) this; }
        public Criteria andIdNotEqualTo(String v) { addCriterion("id <>", v, "id"); return (Criteria) this; }
        public Criteria andIdGreaterThan(String v) { addCriterion("id >", v, "id"); return (Criteria) this; }
        public Criteria andIdGreaterThanOrEqualTo(String v) { addCriterion("id >=", v, "id"); return (Criteria) this; }
        public Criteria andIdLessThan(String v) { addCriterion("id <", v, "id"); return (Criteria) this; }
        public Criteria andIdLessThanOrEqualTo(String v) { addCriterion("id <=", v, "id"); return (Criteria) this; }
        public Criteria andIdLike(String v) { addCriterion("id like", v, "id"); return (Criteria) this; }
        public Criteria andIdNotLike(String v) { addCriterion("id not like", v, "id"); return (Criteria) this; }
        public Criteria andIdIn(List<String> v) { addCriterion("id in", v, "id"); return (Criteria) this; }
        public Criteria andIdNotIn(List<String> v) { addCriterion("id not in", v, "id"); return (Criteria) this; }
        public Criteria andIdBetween(String v1, String v2) { addCriterion("id between", v1, v2, "id"); return (Criteria) this; }
        public Criteria andIdNotBetween(String v1, String v2) { addCriterion("id not between", v1, v2, "id"); return (Criteria) this; }
        // userId
        public Criteria andUserIdIsNull() { addCriterion("user_id is null"); return (Criteria) this; }
        public Criteria andUserIdIsNotNull() { addCriterion("user_id is not null"); return (Criteria) this; }
        public Criteria andUserIdEqualTo(String v) { addCriterion("user_id =", v, "userId"); return (Criteria) this; }
        public Criteria andUserIdNotEqualTo(String v) { addCriterion("user_id <>", v, "userId"); return (Criteria) this; }
        public Criteria andUserIdIn(List<String> v) { addCriterion("user_id in", v, "userId"); return (Criteria) this; }
        // userType
        public Criteria andUserTypeIsNull() { addCriterion("user_type is null"); return (Criteria) this; }
        public Criteria andUserTypeIsNotNull() { addCriterion("user_type is not null"); return (Criteria) this; }
        public Criteria andUserTypeEqualTo(String v) { addCriterion("user_type =", v, "userType"); return (Criteria) this; }
        public Criteria andUserTypeNotEqualTo(String v) { addCriterion("user_type <>", v, "userType"); return (Criteria) this; }
        public Criteria andUserTypeIn(List<String> v) { addCriterion("user_type in", v, "userType"); return (Criteria) this; }
        // loginTime
        public Criteria andLoginTimeIsNull() { addCriterion("login_time is null"); return (Criteria) this; }
        public Criteria andLoginTimeIsNotNull() { addCriterion("login_time is not null"); return (Criteria) this; }
        public Criteria andLoginTimeEqualTo(LocalDateTime v) { addCriterion("login_time =", v, "loginTime"); return (Criteria) this; }
        public Criteria andLoginTimeGreaterThan(LocalDateTime v) { addCriterion("login_time >", v, "loginTime"); return (Criteria) this; }
        public Criteria andLoginTimeGreaterThanOrEqualTo(LocalDateTime v) { addCriterion("login_time >=", v, "loginTime"); return (Criteria) this; }
        public Criteria andLoginTimeLessThan(LocalDateTime v) { addCriterion("login_time <", v, "loginTime"); return (Criteria) this; }
        public Criteria andLoginTimeLessThanOrEqualTo(LocalDateTime v) { addCriterion("login_time <=", v, "loginTime"); return (Criteria) this; }
        // status
        public Criteria andStatusIsNull() { addCriterion("status is null"); return (Criteria) this; }
        public Criteria andStatusIsNotNull() { addCriterion("status is not null"); return (Criteria) this; }
        public Criteria andStatusEqualTo(String v) { addCriterion("status =", v, "status"); return (Criteria) this; }
        public Criteria andStatusNotEqualTo(String v) { addCriterion("status <>", v, "status"); return (Criteria) this; }
        public Criteria andStatusIn(List<String> v) { addCriterion("status in", v, "status"); return (Criteria) this; }
        // loginMethod
        public Criteria andLoginMethodIsNull() { addCriterion("login_method is null"); return (Criteria) this; }
        public Criteria andLoginMethodIsNotNull() { addCriterion("login_method is not null"); return (Criteria) this; }
        public Criteria andLoginMethodEqualTo(String v) { addCriterion("login_method =", v, "loginMethod"); return (Criteria) this; }
        public Criteria andLoginMethodNotEqualTo(String v) { addCriterion("login_method <>", v, "loginMethod"); return (Criteria) this; }
        public Criteria andLoginMethodIn(List<String> v) { addCriterion("login_method in", v, "loginMethod"); return (Criteria) this; }
        // createdAt
        public Criteria andCreatedAtIsNull() { addCriterion("created_at is null"); return (Criteria) this; }
        public Criteria andCreatedAtIsNotNull() { addCriterion("created_at is not null"); return (Criteria) this; }
        public Criteria andCreatedAtEqualTo(LocalDateTime v) { addCriterion("created_at =", v, "createdAt"); return (Criteria) this; }
        public Criteria andCreatedAtGreaterThan(LocalDateTime v) { addCriterion("created_at >", v, "createdAt"); return (Criteria) this; }
        public Criteria andCreatedAtGreaterThanOrEqualTo(LocalDateTime v) { addCriterion("created_at >=", v, "createdAt"); return (Criteria) this; }
        public Criteria andCreatedAtLessThan(LocalDateTime v) { addCriterion("created_at <", v, "createdAt"); return (Criteria) this; }
        public Criteria andCreatedAtLessThanOrEqualTo(LocalDateTime v) { addCriterion("created_at <=", v, "createdAt"); return (Criteria) this; }
    }

    public static class Criteria extends GeneratedCriteria {}

    public static class Criterion {
        private String condition;
        private Object value;
        private Object secondValue;
        private boolean noValue;
        private boolean singleValue;
        private boolean betweenValue;
        private boolean listValue;
        private String typeHandler;
        public String getCondition() { return condition; }
        public Object getValue() { return value; }
        public Object getSecondValue() { return secondValue; }
        public boolean isNoValue() { return noValue; }
        public boolean isSingleValue() { return singleValue; }
        public boolean isBetweenValue() { return betweenValue; }
        public boolean isListValue() { return listValue; }
        public String getTypeHandler() { return typeHandler; }
        protected Criterion(String condition) { super(); this.condition = condition; this.noValue = true; }
        protected Criterion(String condition, Object value) { super(); this.condition = condition; this.value = value; this.singleValue = !(value instanceof List); this.listValue = (value instanceof List); }
        protected Criterion(String condition, Object value, String typeHandler) { this(condition, value); this.typeHandler = typeHandler; }
        protected Criterion(String condition, Object value1, Object value2) { super(); this.condition = condition; this.value = value1; this.secondValue = value2; this.betweenValue = true; }
        protected Criterion(String condition, Object value1, Object value2, String typeHandler) { this(condition, value1, value2); this.typeHandler = typeHandler; }
    }
}
