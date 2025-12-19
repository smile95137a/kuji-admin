package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.Banner;

public class BannerExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(String value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andStoreidEqualTo(String value) {
            conditions.put("storeId", value);
            return this;
        }
        public Criteria andTitleEqualTo(String value) {
            conditions.put("title", value);
            return this;
        }
        public Criteria andImageurlEqualTo(String value) {
            conditions.put("imageUrl", value);
            return this;
        }
        public Criteria andLinkurlEqualTo(String value) {
            conditions.put("linkUrl", value);
            return this;
        }
        public Criteria andOrdernumEqualTo(Integer value) {
            conditions.put("orderNum", value);
            return this;
        }
        public Criteria andStatusEqualTo(String value) {
            conditions.put("status", value);
            return this;
        }
        public Criteria andStarttimeEqualTo(java.time.LocalDateTime value) {
            conditions.put("startTime", value);
            return this;
        }
        public Criteria andEndtimeEqualTo(java.time.LocalDateTime value) {
            conditions.put("endTime", value);
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
        public Criteria andBanneridEqualTo(Long value) {
            conditions.put("bannerId", value);
            return this;
        }
        public Criteria andBanneruidEqualTo(String value) {
            conditions.put("bannerUid", value);
            return this;
        }
        public Criteria andBannerimageurlsEqualTo(String value) {
            conditions.put("bannerImageUrls", value);
            return this;
        }
        public Criteria andProductidEqualTo(Long value) {
            conditions.put("productId", value);
            return this;
        }
        public Criteria andProducttypeEqualTo(String value) {
            conditions.put("productType", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
