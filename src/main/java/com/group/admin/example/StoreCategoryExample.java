package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.StoreCategory;

public class StoreCategoryExample {
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
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
