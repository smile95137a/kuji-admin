package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.Cart;

public class CartExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andCartidEqualTo(Long value) {
            conditions.put("cartId", value);
            return this;
        }
        public Criteria andCreatedatEqualTo(java.time.LocalDateTime value) {
            conditions.put("createdAt", value);
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
        public Criteria andUseruidEqualTo(String value) {
            conditions.put("userUid", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
