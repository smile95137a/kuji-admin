package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.Store;

public class StoreExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(String value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andOwneridEqualTo(String value) {
            conditions.put("ownerId", value);
            return this;
        }
        public Criteria andStorenameEqualTo(String value) {
            conditions.put("storeName", value);
            return this;
        }
        public Criteria andShortdescriptionEqualTo(String value) {
            conditions.put("shortDescription", value);
            return this;
        }
        public Criteria andLongdescriptionEqualTo(String value) {
            conditions.put("longDescription", value);
            return this;
        }
        public Criteria andLogourlEqualTo(String value) {
            conditions.put("logoUrl", value);
            return this;
        }
        public Criteria andCoverimageurlEqualTo(String value) {
            conditions.put("coverImageUrl", value);
            return this;
        }
        public Criteria andEmailEqualTo(String value) {
            conditions.put("email", value);
            return this;
        }
        public Criteria andPhoneEqualTo(String value) {
            conditions.put("phone", value);
            return this;
        }
        public Criteria andAddressEqualTo(String value) {
            conditions.put("address", value);
            return this;
        }
        public Criteria andFacebookurlEqualTo(String value) {
            conditions.put("facebookUrl", value);
            return this;
        }
        public Criteria andInstagramurlEqualTo(String value) {
            conditions.put("instagramUrl", value);
            return this;
        }
        public Criteria andLineidEqualTo(String value) {
            conditions.put("lineId", value);
            return this;
        }
        public Criteria andBusinesshoursEqualTo(String value) {
            conditions.put("businessHours", value);
            return this;
        }
        public Criteria andStatusEqualTo(String value) {
            conditions.put("status", value);
            return this;
        }
        public Criteria andRemarkEqualTo(String value) {
            conditions.put("remark", value);
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
        public Criteria andUpdatedbyEqualTo(String value) {
            conditions.put("updatedBy", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
