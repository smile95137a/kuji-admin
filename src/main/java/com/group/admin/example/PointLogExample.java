package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.PointLog;

public class PointLogExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(String value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andUseridEqualTo(String value) {
            conditions.put("userId", value);
            return this;
        }
        public Criteria andPointtypeEqualTo(String value) {
            conditions.put("pointType", value);
            return this;
        }
        public Criteria andOperationtypeEqualTo(String value) {
            conditions.put("operationType", value);
            return this;
        }
        public Criteria andAmountEqualTo(Long value) {
            conditions.put("amount", value);
            return this;
        }
        public Criteria andBeforebalanceEqualTo(Long value) {
            conditions.put("beforeBalance", value);
            return this;
        }
        public Criteria andAfterbalanceEqualTo(Long value) {
            conditions.put("afterBalance", value);
            return this;
        }
        public Criteria andReferencetypeEqualTo(String value) {
            conditions.put("referenceType", value);
            return this;
        }
        public Criteria andReferenceidEqualTo(String value) {
            conditions.put("referenceId", value);
            return this;
        }
        public Criteria andRemarkEqualTo(String value) {
            conditions.put("remark", value);
            return this;
        }
        public Criteria andExpireatEqualTo(java.time.LocalDateTime value) {
            conditions.put("expireAt", value);
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
