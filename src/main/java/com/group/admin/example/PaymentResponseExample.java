package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.PaymentResponse;

public class PaymentResponseExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andOrderidEqualTo(String value) {
            conditions.put("orderId", value);
            return this;
        }
        public Criteria andAmountEqualTo(String value) {
            conditions.put("amount", value);
            return this;
        }
        public Criteria andAvcodeEqualTo(String value) {
            conditions.put("avCode", value);
            return this;
        }
        public Criteria andBanknameEqualTo(String value) {
            conditions.put("bankName", value);
            return this;
        }
        public Criteria andCheckstringEqualTo(String value) {
            conditions.put("checkString", value);
            return this;
        }
        public Criteria andCurrencyEqualTo(String value) {
            conditions.put("currency", value);
            return this;
        }
        public Criteria andDateEqualTo(String value) {
            conditions.put("date", value);
            return this;
        }
        public Criteria andEpayaccountEqualTo(String value) {
            conditions.put("ePayAccount", value);
            return this;
        }
        public Criteria andInvoicenoEqualTo(String value) {
            conditions.put("invoiceNo", value);
            return this;
        }
        public Criteria andNumberEqualTo(String value) {
            conditions.put("number", value);
            return this;
        }
        public Criteria andOrdernoEqualTo(String value) {
            conditions.put("orderNo", value);
            return this;
        }
        public Criteria andOutlayEqualTo(String value) {
            conditions.put("outlay", value);
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
        public Criteria andTimeEqualTo(String value) {
            conditions.put("time", value);
            return this;
        }
        public Criteria andUseridEqualTo(Long value) {
            conditions.put("userId", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
