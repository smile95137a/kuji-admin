package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.Lottery;

public class LotteryExample {
    private List<Criteria> oredCriteria = new ArrayList<>();
    private String orderByClause;
    private Integer limit;
    private Integer offset;

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getOffset() {
        return offset;
    }

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Map<String, Object> getConditions() {
            return conditions;
        }

        public Criteria andIdEqualTo(String value) {
            conditions.put("id", value);
            return this;
        }

        public Criteria andStoreIdEqualTo(String value) {
            conditions.put("store_id", value);
            return this;
        }

        public Criteria andTitleEqualTo(String value) {
            conditions.put("title", value);
            return this;
        }

        public Criteria andTitleLike(String value) {
            conditions.put("title LIKE", value);
            return this;
        }

        public Criteria andDescriptionEqualTo(String value) {
            conditions.put("description", value);
            return this;
        }

        public Criteria andImageUrlEqualTo(String value) {
            conditions.put("image_url", value);
            return this;
        }

        public Criteria andCategoryEqualTo(String value) {
            conditions.put("category", value);
            return this;
        }

        public Criteria andSubCategoryEqualTo(String value) {
            conditions.put("sub_category", value);
            return this;
        }

        public Criteria andPricePerDrawEqualTo(Long value) {
            conditions.put("price_per_draw", value);
            return this;
        }

        public Criteria andDiscountedPriceEqualTo(Long value) {
            conditions.put("discounted_price", value);
            return this;
        }

        public Criteria andAutoDiscountEnabledEqualTo(Integer value) {
            conditions.put("auto_discount_enabled", value);
            return this;
        }

        public Criteria andAllowMultiDrawEqualTo(Integer value) {
            conditions.put("allow_multi_draw", value);
            return this;
        }

        public Criteria andMultiDrawOptionsEqualTo(String value) {
            conditions.put("multi_draw_options", value);
            return this;
        }

        public Criteria andScheduledAtEqualTo(java.time.LocalDateTime value) {
            conditions.put("scheduled_at", value);
            return this;
        }

        public Criteria andStartTimeEqualTo(java.time.LocalDateTime value) {
            conditions.put("start_time", value);
            return this;
        }

        public Criteria andEndTimeEqualTo(java.time.LocalDateTime value) {
            conditions.put("end_time", value);
            return this;
        }

        public Criteria andTotalDrawsEqualTo(Integer value) {
            conditions.put("total_draws", value);
            return this;
        }

        public Criteria andMaxDrawsEqualTo(Integer value) {
            conditions.put("max_draws", value);
            return this;
        }

        public Criteria andStatusEqualTo(String value) {
            conditions.put("status", value);
            return this;
        }

        public Criteria andStatusIn(List<String> values) {
            conditions.put("status IN", values);
            return this;
        }

        public Criteria andOrderNumEqualTo(Integer value) {
            conditions.put("order_num", value);
            return this;
        }

        public Criteria andWeightEqualTo(Integer value) {
            conditions.put("weight", value);
            return this;
        }

        public Criteria andCreatedByEqualTo(String value) {
            conditions.put("created_by", value);
            return this;
        }

        public Criteria andCreatedAtEqualTo(java.time.LocalDateTime value) {
            conditions.put("created_at", value);
            return this;
        }

        public Criteria andUpdatedAtEqualTo(java.time.LocalDateTime value) {
            conditions.put("updated_at", value);
            return this;
        }

        public Criteria andRemarkEqualTo(String value) {
            conditions.put("remark", value);
            return this;
        }
    }

    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        limit = null;
        offset = null;
    }
}
