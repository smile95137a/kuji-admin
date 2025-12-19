package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.Product;

public class ProductExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andProductidEqualTo(Integer value) {
            conditions.put("productId", value);
            return this;
        }
        public Criteria andBonuspriceEqualTo(java.math.BigDecimal value) {
            conditions.put("bonusPrice", value);
            return this;
        }
        public Criteria andCreatedatEqualTo(java.time.LocalDateTime value) {
            conditions.put("createdAt", value);
            return this;
        }
        public Criteria andCreateduserEqualTo(Integer value) {
            conditions.put("createdUser", value);
            return this;
        }
        public Criteria andDescriptionEqualTo(String value) {
            conditions.put("description", value);
            return this;
        }
        public Criteria andEnddateEqualTo(java.time.LocalDateTime value) {
            conditions.put("endDate", value);
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
        public Criteria andLengthEqualTo(java.math.BigDecimal value) {
            conditions.put("length", value);
            return this;
        }
        public Criteria andPriceEqualTo(java.math.BigDecimal value) {
            conditions.put("price", value);
            return this;
        }
        public Criteria andPrizecategoryEqualTo(String value) {
            conditions.put("prizeCategory", value);
            return this;
        }
        public Criteria andProductnameEqualTo(String value) {
            conditions.put("productName", value);
            return this;
        }
        public Criteria andProducttypeEqualTo(String value) {
            conditions.put("productType", value);
            return this;
        }
        public Criteria andRarityEqualTo(String value) {
            conditions.put("rarity", value);
            return this;
        }
        public Criteria andSizeEqualTo(java.math.BigDecimal value) {
            conditions.put("size", value);
            return this;
        }
        public Criteria andSliverpriceEqualTo(java.math.BigDecimal value) {
            conditions.put("sliverPrice", value);
            return this;
        }
        public Criteria andSpecificationEqualTo(String value) {
            conditions.put("specification", value);
            return this;
        }
        public Criteria andStartdateEqualTo(java.time.LocalDateTime value) {
            conditions.put("startDate", value);
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
        public Criteria andUpdateuserEqualTo(Integer value) {
            conditions.put("updateUser", value);
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
        public Criteria andCategoryidEqualTo(Long value) {
            conditions.put("categoryId", value);
            return this;
        }
        public Criteria andBannerimageurlEqualTo(String value) {
            conditions.put("bannerImageUrl", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
