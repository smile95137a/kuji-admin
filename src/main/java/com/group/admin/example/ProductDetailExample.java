package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.ProductDetail;

public class ProductDetailExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andProductdetailidEqualTo(Long value) {
            conditions.put("productDetailId", value);
            return this;
        }
        public Criteria andCreatedateEqualTo(java.time.LocalDateTime value) {
            conditions.put("createDate", value);
            return this;
        }
        public Criteria andDescriptionEqualTo(String value) {
            conditions.put("description", value);
            return this;
        }
        public Criteria andDrawnnumbersEqualTo(String value) {
            conditions.put("drawnNumbers", value);
            return this;
        }
        public Criteria andGradeEqualTo(String value) {
            conditions.put("grade", value);
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
        public Criteria andMaterialEqualTo(String value) {
            conditions.put("material", value);
            return this;
        }
        public Criteria andNoteEqualTo(String value) {
            conditions.put("note", value);
            return this;
        }
        public Criteria andPriceEqualTo(java.math.BigDecimal value) {
            conditions.put("price", value);
            return this;
        }
        public Criteria andPrizenumberEqualTo(String value) {
            conditions.put("prizeNumber", value);
            return this;
        }
        public Criteria andProductidEqualTo(Long value) {
            conditions.put("productId", value);
            return this;
        }
        public Criteria andProductnameEqualTo(String value) {
            conditions.put("productName", value);
            return this;
        }
        public Criteria andQuantityEqualTo(Integer value) {
            conditions.put("quantity", value);
            return this;
        }
        public Criteria andRarityEqualTo(String value) {
            conditions.put("rarity", value);
            return this;
        }
        public Criteria andSizeEqualTo(String value) {
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
        public Criteria andStockquantityEqualTo(Integer value) {
            conditions.put("stockQuantity", value);
            return this;
        }
        public Criteria andUpdatedateEqualTo(java.time.LocalDateTime value) {
            conditions.put("updateDate", value);
            return this;
        }
        public Criteria andWidthEqualTo(java.math.BigDecimal value) {
            conditions.put("width", value);
            return this;
        }
        public Criteria andProbabilityEqualTo(Double value) {
            conditions.put("probability", value);
            return this;
        }
        public Criteria andIsprizeEqualTo(String value) {
            conditions.put("isPrize", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
