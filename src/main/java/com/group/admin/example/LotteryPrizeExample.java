package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.LotteryPrize;

public class LotteryPrizeExample {
    private List<Criteria> oredCriteria = new ArrayList<>();
    private String orderByClause;

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
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

        public Criteria andLotteryIdEqualTo(String value) {
            conditions.put("lottery_id", value);
            return this;
        }

        public Criteria andNameEqualTo(String value) {
            conditions.put("name", value);
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

        public Criteria andLevelEqualTo(String value) {
            conditions.put("level", value);
            return this;
        }

        public Criteria andPrizeNumberEqualTo(String value) {
            conditions.put("prize_number", value);
            return this;
        }

        public Criteria andQuantityEqualTo(Integer value) {
            conditions.put("quantity", value);
            return this;
        }

        public Criteria andRemainingEqualTo(Integer value) {
            conditions.put("remaining", value);
            return this;
        }

        public Criteria andRemainingGreaterThan(Integer value) {
            conditions.put("remaining >", value);
            return this;
        }

        public Criteria andWeightEqualTo(Integer value) {
            conditions.put("weight", value);
            return this;
        }

        public Criteria andPrizeTypeEqualTo(String value) {
            conditions.put("prize_type", value);
            return this;
        }

        public Criteria andPointValueEqualTo(Long value) {
            conditions.put("point_value", value);
            return this;
        }

        public Criteria andIsLastPrizeEqualTo(Integer value) {
            conditions.put("is_last_prize", value);
            return this;
        }

        public Criteria andIsGrandPrizeEqualTo(Integer value) {
            conditions.put("is_grand_prize", value);
            return this;
        }

        public Criteria andOrderNumEqualTo(Integer value) {
            conditions.put("order_num", value);
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
    }

    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
    }
}
