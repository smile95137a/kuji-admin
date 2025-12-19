package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.StoreKeyword;

public class StoreKeywordExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andKeywordEqualTo(String value) {
            conditions.put("keyword", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
