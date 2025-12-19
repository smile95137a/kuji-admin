package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.UserPasswordResetLog;

public class UserPasswordResetLogExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andResettimeEqualTo(java.time.LocalDateTime value) {
            conditions.put("resetTime", value);
            return this;
        }
        public Criteria andResettokenEqualTo(String value) {
            conditions.put("resetToken", value);
            return this;
        }
        public Criteria andTokenexpiryEqualTo(java.time.LocalDateTime value) {
            conditions.put("tokenExpiry", value);
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
