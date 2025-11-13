package com.group.admin.example;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminUserExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

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
        public Criteria andStatusEqualTo(Integer value) {
            conditions.put("status", value);
            return this;
        }
        public Criteria andLastloginEqualTo(java.time.LocalDateTime value) {
            conditions.put("lastLogin", value);
            return this;
        }
        public Criteria andCreatedateEqualTo(java.time.LocalDateTime value) {
            conditions.put("createDate", value);
            return this;
        }
        public Criteria andUpdatedateEqualTo(java.time.LocalDateTime value) {
            conditions.put("updateDate", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
