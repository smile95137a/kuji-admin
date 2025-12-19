package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.GachaMessage;

public class GachaMessageExample {
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
        public Criteria andNameEqualTo(String value) {
            conditions.put("name", value);
            return this;
        }
        public Criteria andNicknameEqualTo(String value) {
            conditions.put("nickName", value);
            return this;
        }
        public Criteria andProductdetailEqualTo(String value) {
            conditions.put("productDetail", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
