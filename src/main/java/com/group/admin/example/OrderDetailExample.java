package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.OrderDetail;

public class OrderDetailExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andBonuspointsearnedEqualTo(Integer value) {
            conditions.put("bonusPointsEarned", value);
            return this;
        }
        public Criteria andOrderidEqualTo(Long value) {
            conditions.put("orderId", value);
            return this;
        }
        public Criteria andProductdetailidEqualTo(Long value) {
            conditions.put("productDetailId", value);
            return this;
        }
        public Criteria andQuantityEqualTo(Integer value) {
            conditions.put("quantity", value);
            return this;
        }
        public Criteria andResultitemidEqualTo(Integer value) {
            conditions.put("resultItemId", value);
            return this;
        }
        public Criteria andStoreproductidEqualTo(Long value) {
            conditions.put("storeProductId", value);
            return this;
        }
        public Criteria andTotalpriceEqualTo(java.math.BigDecimal value) {
            conditions.put("totalPrice", value);
            return this;
        }
        public Criteria andUnitpriceEqualTo(java.math.BigDecimal value) {
            conditions.put("unitPrice", value);
            return this;
        }
        public Criteria andProductdetailnameEqualTo(String value) {
            conditions.put("productDetailName", value);
            return this;
        }
        public Criteria andProductidEqualTo(Integer value) {
            conditions.put("productId", value);
            return this;
        }
        public Criteria andResultstatusEqualTo(String value) {
            conditions.put("resultStatus", value);
            return this;
        }
        public Criteria andStoreproductnameEqualTo(String value) {
            conditions.put("storeProductName", value);
            return this;
        }
        public Criteria andBillnumberEqualTo(String value) {
            conditions.put("billNumber", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
