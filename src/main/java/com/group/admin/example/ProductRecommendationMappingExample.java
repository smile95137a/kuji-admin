package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.ProductRecommendationMapping;

public class ProductRecommendationMappingExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andCreateddateEqualTo(java.time.LocalDateTime value) {
            conditions.put("createdDate", value);
            return this;
        }
        public Criteria andCreateduserEqualTo(String value) {
            conditions.put("createdUser", value);
            return this;
        }
        public Criteria andStoreproductidEqualTo(Long value) {
            conditions.put("storeProductId", value);
            return this;
        }
        public Criteria andStoreproductrecommendationidEqualTo(Long value) {
            conditions.put("storeProductRecommendationId", value);
            return this;
        }
        public Criteria andUpdateuserEqualTo(String value) {
            conditions.put("updateUser", value);
            return this;
        }
        public Criteria andUpdateddateEqualTo(java.time.LocalDateTime value) {
            conditions.put("updatedDate", value);
            return this;
        }
        public Criteria andProductnameEqualTo(String value) {
            conditions.put("productName", value);
            return this;
        }
        public Criteria andRecommendationnameEqualTo(String value) {
            conditions.put("recommendationName", value);
            return this;
        }
        public Criteria andProductdetailidEqualTo(String value) {
            conditions.put("productDetailId", value);
            return this;
        }
        public Criteria andImageurlsEqualTo(String value) {
            conditions.put("imageUrls", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
