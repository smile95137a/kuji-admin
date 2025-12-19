package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.RefreshToken;

public class RefreshTokenExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(String value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andUsertypeEqualTo(String value) {
            conditions.put("userType", value);
            return this;
        }
        public Criteria andUseridEqualTo(String value) {
            conditions.put("userId", value);
            return this;
        }
        public Criteria andTokenEqualTo(String value) {
            conditions.put("token", value);
            return this;
        }
        public Criteria andDeviceinfoEqualTo(String value) {
            conditions.put("deviceInfo", value);
            return this;
        }
        public Criteria andIpaddressEqualTo(String value) {
            conditions.put("ipAddress", value);
            return this;
        }
        public Criteria andExpiresatEqualTo(java.time.LocalDateTime value) {
            conditions.put("expiresAt", value);
            return this;
        }
        public Criteria andIsrevokedEqualTo(Integer value) {
            conditions.put("isRevoked", value);
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
