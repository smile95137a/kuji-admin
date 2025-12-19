package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.UserUpdateLog;

public class UserUpdateLogExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andUseridsEqualTo(String value) {
            conditions.put("userIds", value);
            return this;
        }
        public Criteria andSlivercoindeltaEqualTo(java.math.BigDecimal value) {
            conditions.put("sliverCoinDelta", value);
            return this;
        }
        public Criteria andBonusdeltaEqualTo(java.math.BigDecimal value) {
            conditions.put("bonusDelta", value);
            return this;
        }
        public Criteria andUpdatetimeEqualTo(java.time.LocalDateTime value) {
            conditions.put("updateTime", value);
            return this;
        }
        public Criteria andOperatorEqualTo(String value) {
            conditions.put("operator", value);
            return this;
        }
        public Criteria andCreatedatEqualTo(java.time.LocalDateTime value) {
            conditions.put("createdAt", value);
            return this;
        }
        public Criteria andBalanceEqualTo(java.math.BigDecimal value) {
            conditions.put("balance", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
