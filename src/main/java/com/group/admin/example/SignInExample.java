package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.SignIn;

public class SignInExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andCreateddateEqualTo(java.time.LocalDateTime value) {
            conditions.put("createdDate", value);
            return this;
        }
        public Criteria andNumberEqualTo(String value) {
            conditions.put("number", value);
            return this;
        }
        public Criteria andProbabilityEqualTo(Double value) {
            conditions.put("probability", value);
            return this;
        }
        public Criteria andSliverpriceEqualTo(java.math.BigDecimal value) {
            conditions.put("sliverPrice", value);
            return this;
        }
        public Criteria andUpdatedateEqualTo(java.time.LocalDateTime value) {
            conditions.put("updateDate", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
