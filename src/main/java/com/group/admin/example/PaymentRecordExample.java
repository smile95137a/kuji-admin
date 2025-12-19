package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.PaymentRecord;

public class PaymentRecordExample {
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
        public Criteria andCreatedateEqualTo(java.time.LocalDateTime value) {
            conditions.put("createDate", value);
            return this;
        }
        public Criteria andCurrencytypeEqualTo(String value) {
            conditions.put("currencyType", value);
            return this;
        }
        public Criteria andPaymentmethodEqualTo(String value) {
            conditions.put("paymentMethod", value);
            return this;
        }
        public Criteria andStatusEqualTo(String value) {
            conditions.put("status", value);
            return this;
        }
        public Criteria andTransactiondateEqualTo(java.time.LocalDateTime value) {
            conditions.put("transactionDate", value);
            return this;
        }
        public Criteria andTransactionidEqualTo(String value) {
            conditions.put("transactionId", value);
            return this;
        }
        public Criteria andUpdatedateEqualTo(java.time.LocalDateTime value) {
            conditions.put("updateDate", value);
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
