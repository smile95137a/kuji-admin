package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.CvsStoreInfo;

public class CvsStoreInfoExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andUuidEqualTo(String value) {
            conditions.put("uuid", value);
            return this;
        }
        public Criteria andCvsaddressEqualTo(String value) {
            conditions.put("cvsAddress", value);
            return this;
        }
        public Criteria andCvsoutsideEqualTo(String value) {
            conditions.put("cvsOutside", value);
            return this;
        }
        public Criteria andCvsstoreidEqualTo(String value) {
            conditions.put("cvsStoreId", value);
            return this;
        }
        public Criteria andCvsstorenameEqualTo(String value) {
            conditions.put("cvsStoreName", value);
            return this;
        }
        public Criteria andCvstelephoneEqualTo(String value) {
            conditions.put("cvsTelephone", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
