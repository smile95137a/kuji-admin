package com.group.admin.example;

import java.util.*;
import com.group.admin.entity.Order;

public class OrderExample {
    private List<Criteria> oredCriteria = new ArrayList<>();

    public static class Criteria {
        private Map<String, Object> conditions = new LinkedHashMap<>();

        public Criteria andIdEqualTo(Integer value) {
            conditions.put("id", value);
            return this;
        }
        public Criteria andOrdernoEqualTo(String value) {
            conditions.put("orderNo", value);
            return this;
        }
        public Criteria andUseridEqualTo(Long value) {
            conditions.put("userId", value);
            return this;
        }
        public Criteria andAmountEqualTo(Long value) {
            conditions.put("amount", value);
            return this;
        }
        public Criteria andOrdertypeEqualTo(String value) {
            conditions.put("orderType", value);
            return this;
        }
        public Criteria andPaymentproviderEqualTo(String value) {
            conditions.put("paymentProvider", value);
            return this;
        }
        public Criteria andProvidertradenoEqualTo(String value) {
            conditions.put("providerTradeNo", value);
            return this;
        }
        public Criteria andStatusEqualTo(String value) {
            conditions.put("status", value);
            return this;
        }
        public Criteria andRemarkEqualTo(String value) {
            conditions.put("remark", value);
            return this;
        }
        public Criteria andPaidatEqualTo(java.time.LocalDateTime value) {
            conditions.put("paidAt", value);
            return this;
        }
        public Criteria andCreatedatEqualTo(java.time.LocalDateTime value) {
            conditions.put("createdAt", value);
            return this;
        }
        public Criteria andUpdatedatEqualTo(java.time.LocalDateTime value) {
            conditions.put("updatedAt", value);
            return this;
        }
        public Criteria andBillingaddressEqualTo(String value) {
            conditions.put("billingAddress", value);
            return this;
        }
        public Criteria andBillingareaEqualTo(String value) {
            conditions.put("billingArea", value);
            return this;
        }
        public Criteria andBillingcityEqualTo(String value) {
            conditions.put("billingCity", value);
            return this;
        }
        public Criteria andBillingemailEqualTo(String value) {
            conditions.put("billingEmail", value);
            return this;
        }
        public Criteria andBillingnameEqualTo(String value) {
            conditions.put("billingName", value);
            return this;
        }
        public Criteria andBillingphoneEqualTo(String value) {
            conditions.put("billingPhone", value);
            return this;
        }
        public Criteria andBillingzipcodeEqualTo(String value) {
            conditions.put("billingZipCode", value);
            return this;
        }
        public Criteria andBonuspointsearnedEqualTo(Integer value) {
            conditions.put("bonusPointsEarned", value);
            return this;
        }
        public Criteria andBonuspointsusedEqualTo(Integer value) {
            conditions.put("bonusPointsUsed", value);
            return this;
        }
        public Criteria andCartitemidEqualTo(String value) {
            conditions.put("cartItemId", value);
            return this;
        }
        public Criteria andInvoiceEqualTo(String value) {
            conditions.put("invoice", value);
            return this;
        }
        public Criteria andIsfreeshippingEqualTo(Boolean value) {
            conditions.put("isFreeShipping", value);
            return this;
        }
        public Criteria andOrdernumberEqualTo(String value) {
            conditions.put("orderNumber", value);
            return this;
        }
        public Criteria andPaymentmethodEqualTo(String value) {
            conditions.put("paymentMethod", value);
            return this;
        }
        public Criteria andResultstatusEqualTo(String value) {
            conditions.put("resultStatus", value);
            return this;
        }
        public Criteria andShippingaddressEqualTo(String value) {
            conditions.put("shippingAddress", value);
            return this;
        }
        public Criteria andShippingareaEqualTo(String value) {
            conditions.put("shippingArea", value);
            return this;
        }
        public Criteria andShippingcityEqualTo(String value) {
            conditions.put("shippingCity", value);
            return this;
        }
        public Criteria andShippingcostEqualTo(java.math.BigDecimal value) {
            conditions.put("shippingCost", value);
            return this;
        }
        public Criteria andShippingemailEqualTo(String value) {
            conditions.put("shippingEmail", value);
            return this;
        }
        public Criteria andShippingmethodEqualTo(String value) {
            conditions.put("shippingMethod", value);
            return this;
        }
        public Criteria andShippingnameEqualTo(String value) {
            conditions.put("shippingName", value);
            return this;
        }
        public Criteria andShippingphoneEqualTo(String value) {
            conditions.put("shippingPhone", value);
            return this;
        }
        public Criteria andShippingzipcodeEqualTo(String value) {
            conditions.put("shippingZipCode", value);
            return this;
        }
        public Criteria andTotalamountEqualTo(java.math.BigDecimal value) {
            conditions.put("totalAmount", value);
            return this;
        }
        public Criteria andTrackingnumberEqualTo(String value) {
            conditions.put("trackingNumber", value);
            return this;
        }
        public Criteria andOpmodeEqualTo(String value) {
            conditions.put("opmode", value);
            return this;
        }
        public Criteria andExpressEqualTo(String value) {
            conditions.put("express", value);
            return this;
        }
        public Criteria andShopidEqualTo(String value) {
            conditions.put("shopId", value);
            return this;
        }
        public Criteria andBillnumberEqualTo(String value) {
            conditions.put("billNumber", value);
            return this;
        }
        public Criteria andEpayaccountEqualTo(String value) {
            conditions.put("ePayaccount", value);
            return this;
        }
        public Criteria andDonationcodeEqualTo(String value) {
            conditions.put("donationCode", value);
            return this;
        }
        public Criteria andStateEqualTo(String value) {
            conditions.put("state", value);
            return this;
        }
        public Criteria andTypeEqualTo(String value) {
            conditions.put("type", value);
            return this;
        }
        public Criteria andShippingmethodidEqualTo(String value) {
            conditions.put("shippingMethodId", value);
            return this;
        }
        public Criteria andShippingmehtodidEqualTo(String value) {
            conditions.put("shippingMehtodId", value);
            return this;
        }
        public Criteria andShopaddressEqualTo(String value) {
            conditions.put("shopAddress", value);
            return this;
        }
        public Criteria andShopnameEqualTo(String value) {
            conditions.put("shopName", value);
            return this;
        }
        public Criteria andVehicleEqualTo(String value) {
            conditions.put("vehicle", value);
            return this;
        }
        public Criteria andUncodeEqualTo(String value) {
            conditions.put("uncode", value);
            return this;
        }
    }
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        oredCriteria.add(criteria);
        return criteria;
    }
}
