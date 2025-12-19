package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.AdminOperationLog;

public class AdminOperationLogExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(String value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andAdminidEqualTo(String value) {
            conditions.put("adminId", value);
            return this;
        }
        public Criteria andOperationtypeEqualTo(String value) {
            conditions.put("operationType", value);
            return this;
        }
        public Criteria andTargettypeEqualTo(String value) {
            conditions.put("targetType", value);
            return this;
        }
        public Criteria andTargetidEqualTo(String value) {
            conditions.put("targetId", value);
            return this;
        }
        public Criteria andDescriptionEqualTo(String value) {
            conditions.put("description", value);
            return this;
        }
        public Criteria andIpaddressEqualTo(String value) {
            conditions.put("ipAddress", value);
            return this;
        }
        public Criteria andUseragentEqualTo(String value) {
            conditions.put("userAgent", value);
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
