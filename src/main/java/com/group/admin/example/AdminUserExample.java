package com.group.admin.example;

import java.util.*;
import java.time.LocalDateTime;

/**
 * 後台管理員查詢條件
 * 使用 Criterion 模式
 */
public class AdminUserExample {
    private List<Criteria> oredCriteria = new ArrayList<>();
    private String orderByClause;
    private Integer limit;
    private Integer offset;

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        if (oredCriteria.isEmpty()) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getOffset() {
        return offset;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        limit = null;
        offset = null;
    }

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Map<String, Object> getConditions() {
            return conditions;
        }

        public Criteria andIdEqualTo(String value) {
            conditions.put("id", value);
            return this;
        }

        public Criteria andUsernameEqualTo(String value) {
            conditions.put("username", value);
            return this;
        }

        public Criteria andPasswordEqualTo(String value) {
            conditions.put("password", value);
            return this;
        }

        public Criteria andEmailEqualTo(String value) {
            conditions.put("email", value);
            return this;
        }

        public Criteria andDisplayNameEqualTo(String value) {
            conditions.put("display_name", value);
            return this;
        }

        public Criteria andPhoneEqualTo(String value) {
            conditions.put("phone", value);
            return this;
        }

        public Criteria andStatusEqualTo(String value) {
            conditions.put("status", value);
            return this;
        }

        public Criteria andForceChangePasswordEqualTo(Boolean value) {
            conditions.put("force_change_password", value);
            return this;
        }

        public Criteria andLastLoginAtEqualTo(LocalDateTime value) {
            conditions.put("last_login_at", value);
            return this;
        }

        public Criteria andCreatedByEqualTo(String value) {
            conditions.put("created_by", value);
            return this;
        }

        public Criteria andCreatedAtEqualTo(LocalDateTime value) {
            conditions.put("created_at", value);
            return this;
        }

        public Criteria andUpdatedByEqualTo(String value) {
            conditions.put("updated_by", value);
            return this;
        }

        public Criteria andUpdatedAtEqualTo(LocalDateTime value) {
            conditions.put("updated_at", value);
            return this;
        }

        public Criteria andRemarkEqualTo(String value) {
            conditions.put("remark", value);
            return this;
        }
    }
}
