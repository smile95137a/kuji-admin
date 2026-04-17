package com.group.admin.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserTokenBlacklistExample {
    protected String orderByClause;
    protected boolean distinct;
    protected List<Criteria> oredCriteria;

    public UserTokenBlacklistExample() { oredCriteria = new ArrayList<>(); }
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
        // userId
        public Criteria andUserIdIsNull() { addCriterion("user_id is null"); return (Criteria) this; }
        public Criteria andUserIdIsNotNull() { addCriterion("user_id is not null"); return (Criteria) this; }
        public Criteria andUserIdEqualTo(String v) { addCriterion("user_id =", v, "userId"); return (Criteria) this; }
        public Criteria andUserIdNotEqualTo(String v) { addCriterion("user_id <>", v, "userId"); return (Criteria) this; }
        public Criteria andUserIdIn(List<String> v) { addCriterion("user_id in", v, "userId"); return (Criteria) this; }
        // blacklistGen
        public Criteria andBlacklistGenIsNull() { addCriterion("blacklist_gen is null"); return (Criteria) this; }
        public Criteria andBlacklistGenIsNotNull() { addCriterion("blacklist_gen is not null"); return (Criteria) this; }
        public Criteria andBlacklistGenEqualTo(Integer v) { addCriterion("blacklist_gen =", v, "blacklistGen"); return (Criteria) this; }
        public Criteria andBlacklistGenNotEqualTo(Integer v) { addCriterion("blacklist_gen <>", v, "blacklistGen"); return (Criteria) this; }
        public Criteria andBlacklistGenGreaterThan(Integer v) { addCriterion("blacklist_gen >", v, "blacklistGen"); return (Criteria) this; }
        public Criteria andBlacklistGenLessThan(Integer v) { addCriterion("blacklist_gen <", v, "blacklistGen"); return (Criteria) this; }
        // updatedAt
        public Criteria andUpdatedAtIsNull() { addCriterion("updated_at is null"); return (Criteria) this; }
        public Criteria andUpdatedAtIsNotNull() { addCriterion("updated_at is not null"); return (Criteria) this; }
        public Criteria andUpdatedAtEqualTo(LocalDateTime v) { addCriterion("updated_at =", v, "updatedAt"); return (Criteria) this; }
        public Criteria andUpdatedAtGreaterThan(LocalDateTime v) { addCriterion("updated_at >", v, "updatedAt"); return (Criteria) this; }
        public Criteria andUpdatedAtLessThan(LocalDateTime v) { addCriterion("updated_at <", v, "updatedAt"); return (Criteria) this; }
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
