package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.PasswordResetTokens;

public class PasswordResetTokensExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andCreatetimeEqualTo(java.time.LocalDateTime value) {
            conditions.put("createTime", value);
            return this;
        }
        public Criteria andExpiretimeEqualTo(java.time.LocalDateTime value) {
            conditions.put("expireTime", value);
            return this;
        }
        public Criteria andIsactiveEqualTo(Boolean value) {
            conditions.put("isActive", value);
            return this;
        }
        public Criteria andPasswordchangedEqualTo(Boolean value) {
            conditions.put("passwordChanged", value);
            return this;
        }
        public Criteria andTokenEqualTo(String value) {
            conditions.put("token", value);
            return this;
        }
        public Criteria andUpdatetimeEqualTo(java.time.LocalDateTime value) {
            conditions.put("updateTime", value);
            return this;
        }
        public Criteria andUseridEqualTo(Long value) {
            conditions.put("userId", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
