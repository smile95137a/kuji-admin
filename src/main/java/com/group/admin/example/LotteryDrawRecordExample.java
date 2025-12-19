package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.LotteryDrawRecord;

public class LotteryDrawRecordExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(String value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andLotteryidEqualTo(String value) {
            conditions.put("lotteryId", value);
            return this;
        }
        public Criteria andUseridEqualTo(String value) {
            conditions.put("userId", value);
            return this;
        }
        public Criteria andPrizeidEqualTo(String value) {
            conditions.put("prizeId", value);
            return this;
        }
        public Criteria andSelectednumberEqualTo(String value) {
            conditions.put("selectedNumber", value);
            return this;
        }
        public Criteria andCosttypeEqualTo(String value) {
            conditions.put("costType", value);
            return this;
        }
        public Criteria andCostamountEqualTo(Long value) {
            conditions.put("costAmount", value);
            return this;
        }
        public Criteria andStatusEqualTo(String value) {
            conditions.put("status", value);
            return this;
        }
        public Criteria andCreatedatEqualTo(java.time.LocalDateTime value) {
            conditions.put("createdAt", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
