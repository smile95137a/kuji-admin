package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.RoleMenu;

public class RoleMenuExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public static class Criteria {
        private List<Criterion> criteria = new ArrayList<>();

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return this;
        }

        public Criteria andRoleIdEqualTo(String value) {
            addCriterion("role_id =", value, "roleId");
            return this;
        }

        public Criteria andRoleIdIn(List<String> values) {
            addCriterion("role_id in", values, "roleId");
            return this;
        }

        public Criteria andMenuIdEqualTo(String value) {
            addCriterion("menu_id =", value, "menuId");
            return this;
        }

        public Criteria andCanViewEqualTo(Boolean value) {
            addCriterion("can_view =", value, "canView");
            return this;
        }

        public Criteria andCanEditEqualTo(Boolean value) {
            addCriterion("can_edit =", value, "canEdit");
            return this;
        }

        public Criteria andCanDeleteEqualTo(Boolean value) {
            addCriterion("can_delete =", value, "canDelete");
            return this;
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }
    }

    public static class Criterion {
        private String condition;
        private Object value;
        private Object secondValue;
        private boolean noValue;
        private boolean singleValue;
        private boolean betweenValue;
        private boolean listValue;

        public Criterion(String condition) {
            this.condition = condition;
            this.noValue = true;
        }

        public Criterion(String condition, Object value) {
            this.condition = condition;
            this.value = value;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        public Criterion(String condition, Object value, Object secondValue) {
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.betweenValue = true;
        }

        public String getCondition() { return condition; }
        public Object getValue() { return value; }
        public Object getSecondValue() { return secondValue; }
        public boolean isNoValue() { return noValue; }
        public boolean isSingleValue() { return singleValue; }
        public boolean isBetweenValue() { return betweenValue; }
        public boolean isListValue() { return listValue; }
    }

    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria or() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
    }
}
