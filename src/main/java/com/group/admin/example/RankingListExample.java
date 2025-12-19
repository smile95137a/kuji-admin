package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.RankingList;

public class RankingListExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andCategoryEqualTo(String value) {
            conditions.put("category", value);
            return this;
        }
        public Criteria andCreateddateEqualTo(java.time.LocalDateTime value) {
            conditions.put("createdDate", value);
            return this;
        }
        public Criteria andNameEqualTo(String value) {
            conditions.put("name", value);
            return this;
        }
        public Criteria andNicknameEqualTo(String value) {
            conditions.put("nickname", value);
            return this;
        }
        public Criteria andProductcountEqualTo(Integer value) {
            conditions.put("productCount", value);
            return this;
        }
        public Criteria andProductidEqualTo(String value) {
            conditions.put("productId", value);
            return this;
        }
        public Criteria andStatusEqualTo(String value) {
            conditions.put("status", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
