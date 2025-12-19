package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.VerificationToken;

public class VerificationTokenExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andExpirydateEqualTo(java.time.LocalDateTime value) {
            conditions.put("expiryDate", value);
            return this;
        }
        public Criteria andTokenEqualTo(String value) {
            conditions.put("token", value);
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
