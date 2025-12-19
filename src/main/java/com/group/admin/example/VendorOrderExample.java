package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.VendorOrder;

public class VendorOrderExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andVendororderEqualTo(String value) {
            conditions.put("vendorOrder", value);
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
        public Criteria andOrdernoEqualTo(String value) {
            conditions.put("orderNo", value);
            return this;
        }
        public Criteria andExpressEqualTo(String value) {
            conditions.put("express", value);
            return this;
        }
        public Criteria andStatusEqualTo(String value) {
            conditions.put("status", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
