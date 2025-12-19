package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.PaymentCallbackLog;

public class PaymentCallbackLogExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Long value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andCreatedatEqualTo(java.time.LocalDateTime value) {
            conditions.put("createdAt", value);
            return this;
        }
        public Criteria andEdateEqualTo(String value) {
            conditions.put("eDate", value);
            return this;
        }
        public Criteria andEmoneyEqualTo(String value) {
            conditions.put("eMoney", value);
            return this;
        }
        public Criteria andEordernoEqualTo(String value) {
            conditions.put("eOrderno", value);
            return this;
        }
        public Criteria andEpayaccountEqualTo(String value) {
            conditions.put("ePayaccount", value);
            return this;
        }
        public Criteria andEpayinfoEqualTo(String value) {
            conditions.put("ePayInfo", value);
            return this;
        }
        public Criteria andEtimeEqualTo(String value) {
            conditions.put("eTime", value);
            return this;
        }
        public Criteria andOrderidEqualTo(String value) {
            conditions.put("orderId", value);
            return this;
        }
        public Criteria andPayamountEqualTo(String value) {
            conditions.put("payAmount", value);
            return this;
        }
        public Criteria andResultEqualTo(String value) {
            conditions.put("result", value);
            return this;
        }
        public Criteria andRetmsgEqualTo(String value) {
            conditions.put("retMsg", value);
            return this;
        }
        public Criteria andSendtypeEqualTo(String value) {
            conditions.put("sendType", value);
            return this;
        }
        public Criteria andStrcheckEqualTo(String value) {
            conditions.put("strCheck", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
