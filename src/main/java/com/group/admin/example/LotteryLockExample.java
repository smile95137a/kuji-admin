package com.group.admin.example;

import java.util.*;
import java.time.LocalDateTime;

public class LotteryLockExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        if (oredCriteria.isEmpty()) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
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

        public Criteria andLotteryIdEqualTo(String value) {
            conditions.put("lottery_id", value);
            return this;
        }

        public Criteria andUserIdEqualTo(String value) {
            conditions.put("user_id", value);
            return this;
        }

        public Criteria andLockStartTimeEqualTo(LocalDateTime value) {
            conditions.put("lock_start_time", value);
            return this;
        }

        public Criteria andLockEndTimeEqualTo(LocalDateTime value) {
            conditions.put("lock_end_time", value);
            return this;
        }

        public Criteria andIsActiveEqualTo(Integer value) {
            conditions.put("is_active", value);
            return this;
        }

        public Criteria andCreatedAtEqualTo(LocalDateTime value) {
            conditions.put("created_at", value);
            return this;
        }
    }
}
