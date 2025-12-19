package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.SysConfig;

public class SysConfigExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andVariableEqualTo(String value) {
            conditions.put("variable", value);
            return this;
        }
        public Criteria andValueEqualTo(String value) {
            conditions.put("value", value);
            return this;
        }
        public Criteria andSettimeEqualTo(java.time.LocalDateTime value) {
            conditions.put("setTime", value);
            return this;
        }
        public Criteria andSetbyEqualTo(String value) {
            conditions.put("setBy", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
