package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.EshopOrder;

public class EshopOrderExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andEshopidEqualTo(String value) {
            conditions.put("eshopId", value);
            return this;
        }
        public Criteria andErrorcodeEqualTo(String value) {
            conditions.put("errorCode", value);
            return this;
        }
        public Criteria andErrormessageEqualTo(String value) {
            conditions.put("errorMessage", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
