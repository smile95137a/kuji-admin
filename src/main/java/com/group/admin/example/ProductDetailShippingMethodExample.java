package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.ProductDetailShippingMethod;

public class ProductDetailShippingMethodExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andProductdetailidEqualTo(Long value) {
            conditions.put("productDetailId", value);
            return this;
        }
        public Criteria andShippingmethodidEqualTo(Long value) {
            conditions.put("shippingMethodId", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
