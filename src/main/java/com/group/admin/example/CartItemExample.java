package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.CartItem;

public class CartItemExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andCartitemidEqualTo(Long value) {
            conditions.put("cartItemId", value);
            return this;
        }
        public Criteria andCartidEqualTo(Long value) {
            conditions.put("cartId", value);
            return this;
        }
        public Criteria andIsselectedEqualTo(Boolean value) {
            conditions.put("isSelected", value);
            return this;
        }
        public Criteria andQuantityEqualTo(Integer value) {
            conditions.put("quantity", value);
            return this;
        }
        public Criteria andSizeEqualTo(java.math.BigDecimal value) {
            conditions.put("size", value);
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
        public Criteria andProductdetailidEqualTo(Long value) {
            conditions.put("productDetailId", value);
            return this;
        }
        public Criteria andStatusEqualTo(String value) {
            conditions.put("status", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
