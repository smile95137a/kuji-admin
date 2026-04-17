package com.group.admin.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminAuditLogExample {
    protected String orderByClause;
    protected boolean distinct;
    protected List<Criteria> oredCriteria;

    public AdminAuditLogExample() { oredCriteria = new ArrayList<>(); }
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
        public Criteria andIdIn(List<String> v) { addCriterion("id in", v, "id"); return (Criteria) this; }
        // operatorId
        public Criteria andOperatorIdIsNull() { addCriterion("operator_id is null"); return (Criteria) this; }
        public Criteria andOperatorIdIsNotNull() { addCriterion("operator_id is not null"); return (Criteria) this; }
        public Criteria andOperatorIdEqualTo(String v) { addCriterion("operator_id =", v, "operatorId"); return (Criteria) this; }
        public Criteria andOperatorIdNotEqualTo(String v) { addCriterion("operator_id <>", v, "operatorId"); return (Criteria) this; }
        public Criteria andOperatorIdIn(List<String> v) { addCriterion("operator_id in", v, "operatorId"); return (Criteria) this; }
        // operatorName
        public Criteria andOperatorNameIsNull() { addCriterion("operator_name is null"); return (Criteria) this; }
        public Criteria andOperatorNameIsNotNull() { addCriterion("operator_name is not null"); return (Criteria) this; }
        public Criteria andOperatorNameEqualTo(String v) { addCriterion("operator_name =", v, "operatorName"); return (Criteria) this; }
        public Criteria andOperatorNameLike(String v) { addCriterion("operator_name like", v, "operatorName"); return (Criteria) this; }
        // action
        public Criteria andActionIsNull() { addCriterion("action is null"); return (Criteria) this; }
        public Criteria andActionIsNotNull() { addCriterion("action is not null"); return (Criteria) this; }
        public Criteria andActionEqualTo(String v) { addCriterion("action =", v, "action"); return (Criteria) this; }
        public Criteria andActionNotEqualTo(String v) { addCriterion("action <>", v, "action"); return (Criteria) this; }
        public Criteria andActionIn(List<String> v) { addCriterion("action in", v, "action"); return (Criteria) this; }
        // targetType
        public Criteria andTargetTypeIsNull() { addCriterion("target_type is null"); return (Criteria) this; }
        public Criteria andTargetTypeIsNotNull() { addCriterion("target_type is not null"); return (Criteria) this; }
        public Criteria andTargetTypeEqualTo(String v) { addCriterion("target_type =", v, "targetType"); return (Criteria) this; }
        public Criteria andTargetTypeIn(List<String> v) { addCriterion("target_type in", v, "targetType"); return (Criteria) this; }
        // targetId
        public Criteria andTargetIdIsNull() { addCriterion("target_id is null"); return (Criteria) this; }
        public Criteria andTargetIdIsNotNull() { addCriterion("target_id is not null"); return (Criteria) this; }
        public Criteria andTargetIdEqualTo(String v) { addCriterion("target_id =", v, "targetId"); return (Criteria) this; }
        public Criteria andTargetIdIn(List<String> v) { addCriterion("target_id in", v, "targetId"); return (Criteria) this; }
        // ipAddress
        public Criteria andIpAddressIsNull() { addCriterion("ip_address is null"); return (Criteria) this; }
        public Criteria andIpAddressIsNotNull() { addCriterion("ip_address is not null"); return (Criteria) this; }
        public Criteria andIpAddressEqualTo(String v) { addCriterion("ip_address =", v, "ipAddress"); return (Criteria) this; }
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
