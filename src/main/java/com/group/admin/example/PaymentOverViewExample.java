package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.PaymentOverView;

public class PaymentOverViewExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andCreatedateEqualTo(java.time.LocalDateTime value) {
            conditions.put("createDate", value);
            return this;
        }
        public Criteria andLasttransactiondateEqualTo(java.time.LocalDateTime value) {
            conditions.put("lastTransactionDate", value);
            return this;
        }
        public Criteria andTotalbonusEqualTo(java.math.BigDecimal value) {
            conditions.put("totalBonus", value);
            return this;
        }
        public Criteria andTotalgoldEqualTo(java.math.BigDecimal value) {
            conditions.put("totalGold", value);
            return this;
        }
        public Criteria andTotalsilverEqualTo(java.math.BigDecimal value) {
            conditions.put("totalSilver", value);
            return this;
        }
        public Criteria andTotalspentbonusEqualTo(java.math.BigDecimal value) {
            conditions.put("totalSpentBonus", value);
            return this;
        }
        public Criteria andTotalspentgoldEqualTo(java.math.BigDecimal value) {
            conditions.put("totalSpentGold", value);
            return this;
        }
        public Criteria andTotalspentsilverEqualTo(java.math.BigDecimal value) {
            conditions.put("totalSpentSilver", value);
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
