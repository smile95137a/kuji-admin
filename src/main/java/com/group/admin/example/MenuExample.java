package com.group.admin.example;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MenuExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(String value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andNameEqualTo(String value) {
            conditions.put("name", value);
            return this;
        }
        public Criteria andPathEqualTo(String value) {
            conditions.put("path", value);
            return this;
        }
        public Criteria andParentidEqualTo(String value) {
            conditions.put("parentId", value);
            return this;
        }
        public Criteria andIconEqualTo(String value) {
            conditions.put("icon", value);
            return this;
        }
        public Criteria andOrdernumEqualTo(Integer value) {
            conditions.put("orderNum", value);
            return this;
        }
        public Criteria andCreatedateEqualTo(java.time.LocalDateTime value) {
            conditions.put("createDate", value);
            return this;
        }
        public Criteria andUpdatedateEqualTo(java.time.LocalDateTime value) {
            conditions.put("updateDate", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
