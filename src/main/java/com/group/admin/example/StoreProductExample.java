package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.StoreProduct;

public class StoreProductExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andStoreproductidEqualTo(Long value) {
            conditions.put("storeProductId", value);
            return this;
        }
        public Criteria andCategoryidEqualTo(String value) {
            conditions.put("categoryId", value);
            return this;
        }
        public Criteria andCreatedatEqualTo(java.time.LocalDateTime value) {
            conditions.put("createdAt", value);
            return this;
        }
        public Criteria andCreateduseridEqualTo(Long value) {
            conditions.put("createdUserId", value);
            return this;
        }
        public Criteria andDescriptionEqualTo(String value) {
            conditions.put("description", value);
            return this;
        }
        public Criteria andDetailsEqualTo(String value) {
            conditions.put("details", value);
            return this;
        }
        public Criteria andHeightEqualTo(java.math.BigDecimal value) {
            conditions.put("height", value);
            return this;
        }
        public Criteria andImageurlsEqualTo(String value) {
            conditions.put("imageUrls", value);
            return this;
        }
        public Criteria andIsspecialpriceEqualTo(Boolean value) {
            conditions.put("isSpecialPrice", value);
            return this;
        }
        public Criteria andLengthEqualTo(java.math.BigDecimal value) {
            conditions.put("length", value);
            return this;
        }
        public Criteria andPopularityEqualTo(Integer value) {
            conditions.put("popularity", value);
            return this;
        }
        public Criteria andPriceEqualTo(java.math.BigDecimal value) {
            conditions.put("price", value);
            return this;
        }
        public Criteria andProductcodeEqualTo(String value) {
            conditions.put("productCode", value);
            return this;
        }
        public Criteria andProductnameEqualTo(String value) {
            conditions.put("productName", value);
            return this;
        }
        public Criteria andShippingmethodEqualTo(String value) {
            conditions.put("shippingMethod", value);
            return this;
        }
        public Criteria andShippingpriceEqualTo(java.math.BigDecimal value) {
            conditions.put("shippingPrice", value);
            return this;
        }
        public Criteria andSizeEqualTo(java.math.BigDecimal value) {
            conditions.put("size", value);
            return this;
        }
        public Criteria andSoldquantityEqualTo(Integer value) {
            conditions.put("soldQuantity", value);
            return this;
        }
        public Criteria andSpecialpriceEqualTo(java.math.BigDecimal value) {
            conditions.put("specialPrice", value);
            return this;
        }
        public Criteria andSpecificationEqualTo(String value) {
            conditions.put("specification", value);
            return this;
        }
        public Criteria andStatusEqualTo(String value) {
            conditions.put("status", value);
            return this;
        }
        public Criteria andStockquantityEqualTo(Integer value) {
            conditions.put("stockQuantity", value);
            return this;
        }
        public Criteria andUpdateuseridEqualTo(Long value) {
            conditions.put("updateUserId", value);
            return this;
        }
        public Criteria andUpdatedatEqualTo(java.time.LocalDateTime value) {
            conditions.put("updatedAt", value);
            return this;
        }
        public Criteria andWidthEqualTo(java.math.BigDecimal value) {
            conditions.put("width", value);
            return this;
        }
        public Criteria andCreateduserEqualTo(Long value) {
            conditions.put("createdUser", value);
            return this;
        }
        public Criteria andUpdateuserEqualTo(Long value) {
            conditions.put("updateUser", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
