package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.ProductCategory;

public class ProductCategoryExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andCategoryidEqualTo(Long value) {
            conditions.put("categoryId", value);
            return this;
        }
        public Criteria andCategorynameEqualTo(String value) {
            conditions.put("categoryName", value);
            return this;
        }
        public Criteria andCategoryuuidEqualTo(String value) {
            conditions.put("categoryUuid", value);
            return this;
        }
        public Criteria andProductsortEqualTo(Long value) {
            conditions.put("productSort", value);
            return this;
        }
        public Criteria andMaxproductsortEqualTo(Long value) {
            conditions.put("maxProductSort", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
