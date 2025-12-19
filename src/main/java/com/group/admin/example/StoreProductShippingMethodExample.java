package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.StoreProductShippingMethod;

public class StoreProductShippingMethodExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andShippingmethodidEqualTo(Long value) {
            conditions.put("shippingMethodId", value);
            return this;
        }
        public Criteria andStoreproductidEqualTo(Long value) {
            conditions.put("storeProductId", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
