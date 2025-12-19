package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.PrizeNumber;

public class PrizeNumberExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andPrizenumberidEqualTo(Long value) {
            conditions.put("prizeNumberId", value);
            return this;
        }
        public Criteria andIsdrawnEqualTo(Boolean value) {
            conditions.put("isDrawn", value);
            return this;
        }
        public Criteria andLevelEqualTo(String value) {
            conditions.put("level", value);
            return this;
        }
        public Criteria andNumberEqualTo(String value) {
            conditions.put("number", value);
            return this;
        }
        public Criteria andProductdetailidEqualTo(Long value) {
            conditions.put("productDetailId", value);
            return this;
        }
        public Criteria andProductidEqualTo(Long value) {
            conditions.put("productId", value);
            return this;
        }
        public Criteria andProbabilityEqualTo(Double value) {
            conditions.put("probability", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
