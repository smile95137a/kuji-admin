package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.DrawResult;

public class DrawResultExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andDrawidEqualTo(Long value) {
            conditions.put("drawId", value);
            return this;
        }
        public Criteria andAmountEqualTo(java.math.BigDecimal value) {
            conditions.put("amount", value);
            return this;
        }
        public Criteria andCreatedateEqualTo(java.time.LocalDateTime value) {
            conditions.put("createDate", value);
            return this;
        }
        public Criteria andDrawcountEqualTo(Integer value) {
            conditions.put("drawCount", value);
            return this;
        }
        public Criteria andDrawtimeEqualTo(java.time.LocalDateTime value) {
            conditions.put("drawTime", value);
            return this;
        }
        public Criteria andPrizenumberEqualTo(String value) {
            conditions.put("prizeNumber", value);
            return this;
        }
        public Criteria andProductdetailidEqualTo(Long value) {
            conditions.put("productDetailId", value);
            return this;
        }
        public Criteria andProductidEqualTo(Long value) {
            conditions.put("productId", value);
            return this;
        }
        public Criteria andRemainingdrawcountEqualTo(Integer value) {
            conditions.put("remainingDrawCount", value);
            return this;
        }
        public Criteria andRemainingtimeEqualTo(Long value) {
            conditions.put("remainingTime", value);
            return this;
        }
        public Criteria andStatusEqualTo(String value) {
            conditions.put("status", value);
            return this;
        }
        public Criteria andTotaldrawcountEqualTo(Long value) {
            conditions.put("totalDrawCount", value);
            return this;
        }
        public Criteria andUpdatedateEqualTo(java.time.LocalDateTime value) {
            conditions.put("updateDate", value);
            return this;
        }
        public Criteria andUseridEqualTo(Long value) {
            conditions.put("userId", value);
            return this;
        }
        public Criteria andImageurlsEqualTo(String value) {
            conditions.put("imageUrls", value);
            return this;
        }
        public Criteria andProductnameEqualTo(String value) {
            conditions.put("productName", value);
            return this;
        }
        public Criteria andBonuspriceEqualTo(java.math.BigDecimal value) {
            conditions.put("bonusPrice", value);
            return this;
        }
        public Criteria andPriceEqualTo(java.math.BigDecimal value) {
            conditions.put("price", value);
            return this;
        }
        public Criteria andSliverpriceEqualTo(java.math.BigDecimal value) {
            conditions.put("sliverPrice", value);
            return this;
        }
        public Criteria andPaytypeEqualTo(String value) {
            conditions.put("payType", value);
            return this;
        }
        public Criteria andEndtimesEqualTo(java.time.LocalDateTime value) {
            conditions.put("endTimes", value);
            return this;
        }
        public Criteria andLevelEqualTo(String value) {
            conditions.put("level", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
