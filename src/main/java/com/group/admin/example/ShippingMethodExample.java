package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.ShippingMethod;

public class ShippingMethodExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andShippingmethodidEqualTo(Long value) {
            conditions.put("shippingMethodId", value);
            return this;
        }
        public Criteria andCreatedateEqualTo(java.time.LocalDateTime value) {
            conditions.put("createDate", value);
            return this;
        }
        public Criteria andDescriptionEqualTo(String value) {
            conditions.put("description", value);
            return this;
        }
        public Criteria andNameEqualTo(String value) {
            conditions.put("name", value);
            return this;
        }
        public Criteria andShippingcodeEqualTo(String value) {
            conditions.put("shippingCode", value);
            return this;
        }
        public Criteria andShippingpriceEqualTo(java.math.BigDecimal value) {
            conditions.put("shippingPrice", value);
            return this;
        }
        public Criteria andSizeEqualTo(java.math.BigDecimal value) {
            conditions.put("size", value);
            return this;
        }
        public Criteria andStatusEqualTo(Integer value) {
            conditions.put("status", value);
            return this;
        }
        public Criteria andUpdatedateEqualTo(java.time.LocalDateTime value) {
            conditions.put("updateDate", value);
            return this;
        }
        public Criteria andMaxsizeEqualTo(java.math.BigDecimal value) {
            conditions.put("maxSize", value);
            return this;
        }
        public Criteria andMinsizeEqualTo(java.math.BigDecimal value) {
            conditions.put("minSize", value);
            return this;
        }
        public Criteria andCodeEqualTo(String value) {
            conditions.put("code", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
