package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.UserTransaction;

public class UserTransactionExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andAmountEqualTo(java.math.BigDecimal value) {
            conditions.put("amount", value);
            return this;
        }
        public Criteria andCreatedatEqualTo(java.time.LocalDateTime value) {
            conditions.put("createdAt", value);
            return this;
        }
        public Criteria andTransactiondateEqualTo(java.time.LocalDateTime value) {
            conditions.put("transactionDate", value);
            return this;
        }
        public Criteria andTransactiontypeEqualTo(String value) {
            conditions.put("transactionType", value);
            return this;
        }
        public Criteria andUpdatedatEqualTo(java.time.LocalDateTime value) {
            conditions.put("updatedAt", value);
            return this;
        }
        public Criteria andUseridEqualTo(Long value) {
            conditions.put("userId", value);
            return this;
        }
        public Criteria andUseruuidEqualTo(String value) {
            conditions.put("userUuid", value);
            return this;
        }
        public Criteria andOrderidEqualTo(String value) {
            conditions.put("orderId", value);
            return this;
        }
        public Criteria andOrdernumberEqualTo(String value) {
            conditions.put("orderNumber", value);
            return this;
        }
        public Criteria andStatusEqualTo(String value) {
            conditions.put("status", value);
            return this;
        }
        public Criteria andPaymethodEqualTo(String value) {
            conditions.put("payMethod", value);
            return this;
        }
        public Criteria andTypeEqualTo(String value) {
            conditions.put("type", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
