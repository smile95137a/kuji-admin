package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.MarqueeDetail;

public class MarqueeDetailExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andGradeEqualTo(String value) {
            conditions.put("grade", value);
            return this;
        }
        public Criteria andMarqueeidEqualTo(Long value) {
            conditions.put("marqueeId", value);
            return this;
        }
        public Criteria andNameEqualTo(String value) {
            conditions.put("name", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
