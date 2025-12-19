package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.RedemptionCodes;

public class RedemptionCodesExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andCodeEqualTo(String value) {
            conditions.put("code", value);
            return this;
        }
        public Criteria andIsredeemedEqualTo(Boolean value) {
            conditions.put("isRedeemed", value);
            return this;
        }
        public Criteria andRedeemedatEqualTo(java.time.LocalDateTime value) {
            conditions.put("redeemedAt", value);
            return this;
        }
        public Criteria andUseridEqualTo(Long value) {
            conditions.put("userId", value);
            return this;
        }
        public Criteria andProductidEqualTo(Long value) {
            conditions.put("productId", value);
            return this;
        }
        public Criteria andProductnameEqualTo(String value) {
            conditions.put("productName", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
