package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.OrderLog;

public class OrderLogExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andChangetimeEqualTo(java.time.LocalDateTime value) {
            conditions.put("changeTime", value);
            return this;
        }
        public Criteria andNewstatusEqualTo(String value) {
            conditions.put("newStatus", value);
            return this;
        }
        public Criteria andOldstatusEqualTo(String value) {
            conditions.put("oldStatus", value);
            return this;
        }
        public Criteria andOrderidEqualTo(Long value) {
            conditions.put("orderId", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
