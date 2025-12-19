package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.DailySignInRecords;

public class DailySignInRecordsExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andRewardpointsEqualTo(java.math.BigDecimal value) {
            conditions.put("rewardPoints", value);
            return this;
        }
        public Criteria andSignindateEqualTo(java.time.LocalDateTime value) {
            conditions.put("signInDate", value);
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
