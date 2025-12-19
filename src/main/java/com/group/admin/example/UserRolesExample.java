package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.UserRoles;

public class UserRolesExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andUseridEqualTo(Long value) {
            conditions.put("userId", value);
            return this;
        }
        public Criteria andRoleidEqualTo(Long value) {
            conditions.put("roleId", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
