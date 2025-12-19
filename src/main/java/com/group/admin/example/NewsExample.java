package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.News;

public class NewsExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andAuthorEqualTo(String value) {
            conditions.put("author", value);
            return this;
        }
        public Criteria andContentEqualTo(String value) {
            conditions.put("content", value);
            return this;
        }
        public Criteria andCreateddateEqualTo(java.time.LocalDateTime value) {
            conditions.put("createdDate", value);
            return this;
        }
        public Criteria andImageurlsEqualTo(String value) {
            conditions.put("imageUrls", value);
            return this;
        }
        public Criteria andNewsuidEqualTo(String value) {
            conditions.put("newsUid", value);
            return this;
        }
        public Criteria andPreviewEqualTo(String value) {
            conditions.put("preview", value);
            return this;
        }
        public Criteria andStatusEqualTo(String value) {
            conditions.put("status", value);
            return this;
        }
        public Criteria andTitleEqualTo(String value) {
            conditions.put("title", value);
            return this;
        }
        public Criteria andUpdateddateEqualTo(java.time.LocalDateTime value) {
            conditions.put("updatedDate", value);
            return this;
        }
        public Criteria andEnddateEqualTo(java.time.LocalDateTime value) {
            conditions.put("endDate", value);
            return this;
        }
        public Criteria andIsdisplayonhomeEqualTo(Boolean value) {
            conditions.put("isDisplayOnHome", value);
            return this;
        }
        public Criteria andStartdateEqualTo(java.time.LocalDateTime value) {
            conditions.put("startDate", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
