package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.PrizeRecycleLog;

public class PrizeRecycleLogExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andUseridEqualTo(Long value) {
            conditions.put("userId", value);
            return this;
        }
        public Criteria andProductdetailidEqualTo(Long value) {
            conditions.put("productDetailId", value);
            return this;
        }
        public Criteria andSlivercoinEqualTo(java.math.BigDecimal value) {
            conditions.put("sliverCoin", value);
            return this;
        }
        public Criteria andRecycletimeEqualTo(java.time.LocalDateTime value) {
            conditions.put("recycleTime", value);
            return this;
        }
        public Criteria andOperatorEqualTo(String value) {
            conditions.put("operator", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
