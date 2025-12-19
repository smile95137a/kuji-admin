package com.group.admin.example; 
 
import java.util.*; 
import java.time.LocalDateTime; 
 
public class UserExample { 
    private List<Criteria> oredCriteria = new ArrayList<>(); 
    public List<Criteria> getOredCriteria() { return oredCriteria; } 
    public Criteria createCriteria() { Criteria c = new Criteria(); if (oredCriteria.isEmpty()) oredCriteria.add(c); return c; } 
    public static class Criteria { 
        private Map<String, Object> conditions = new LinkedHashMap<>(); 
        public Map<String, Object> getConditions() { return conditions; } 
        public Criteria andIdEqualTo(String v) { conditions.put("id", v); return this; } 
        public Criteria andEmailEqualTo(String v) { conditions.put("email", v); return this; } 
        public Criteria andProviderEqualTo(String v) { conditions.put("provider", v); return this; } 
        public Criteria andStatusEqualTo(String v) { conditions.put("status", v); return this; } 
    } 
}
