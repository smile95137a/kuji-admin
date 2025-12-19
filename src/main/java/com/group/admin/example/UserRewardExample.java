package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.UserReward;

public class UserRewardExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andCreatedatEqualTo(java.time.LocalDateTime value) {
            conditions.put("createdAt", value);
            return this;
        }
        public Criteria andRewardamountEqualTo(java.math.BigDecimal value) {
            conditions.put("rewardAmount", value);
            return this;
        }
        public Criteria andRewarddateEqualTo(java.time.LocalDateTime value) {
            conditions.put("rewardDate", value);
            return this;
        }
        public Criteria andUseridEqualTo(Long value) {
            conditions.put("userId", value);
            return this;
        }
        public Criteria andThresholdamountEqualTo(java.math.BigDecimal value) {
            conditions.put("thresholdAmount", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
