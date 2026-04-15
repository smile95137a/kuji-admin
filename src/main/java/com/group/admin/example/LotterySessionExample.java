package com.group.admin.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LotterySessionExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public LotterySessionExample() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(String value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(String value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(String value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(String value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(String value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLike(String value) {
            addCriterion("id like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotLike(String value) {
            addCriterion("id not like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<String> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<String> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(String value1, String value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(String value1, String value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andLotteryIdIsNull() {
            addCriterion("lottery_id is null");
            return (Criteria) this;
        }

        public Criteria andLotteryIdIsNotNull() {
            addCriterion("lottery_id is not null");
            return (Criteria) this;
        }

        public Criteria andLotteryIdEqualTo(String value) {
            addCriterion("lottery_id =", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdNotEqualTo(String value) {
            addCriterion("lottery_id <>", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdGreaterThan(String value) {
            addCriterion("lottery_id >", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdGreaterThanOrEqualTo(String value) {
            addCriterion("lottery_id >=", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdLessThan(String value) {
            addCriterion("lottery_id <", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdLessThanOrEqualTo(String value) {
            addCriterion("lottery_id <=", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdLike(String value) {
            addCriterion("lottery_id like", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdNotLike(String value) {
            addCriterion("lottery_id not like", value, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdIn(List<String> values) {
            addCriterion("lottery_id in", values, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdNotIn(List<String> values) {
            addCriterion("lottery_id not in", values, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdBetween(String value1, String value2) {
            addCriterion("lottery_id between", value1, value2, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andLotteryIdNotBetween(String value1, String value2) {
            addCriterion("lottery_id not between", value1, value2, "lotteryId");
            return (Criteria) this;
        }

        public Criteria andOpenerUserIdIsNull() {
            addCriterion("opener_user_id is null");
            return (Criteria) this;
        }

        public Criteria andOpenerUserIdIsNotNull() {
            addCriterion("opener_user_id is not null");
            return (Criteria) this;
        }

        public Criteria andOpenerUserIdEqualTo(String value) {
            addCriterion("opener_user_id =", value, "openerUserId");
            return (Criteria) this;
        }

        public Criteria andOpenerUserIdNotEqualTo(String value) {
            addCriterion("opener_user_id <>", value, "openerUserId");
            return (Criteria) this;
        }

        public Criteria andOpenerUserIdGreaterThan(String value) {
            addCriterion("opener_user_id >", value, "openerUserId");
            return (Criteria) this;
        }

        public Criteria andOpenerUserIdGreaterThanOrEqualTo(String value) {
            addCriterion("opener_user_id >=", value, "openerUserId");
            return (Criteria) this;
        }

        public Criteria andOpenerUserIdLessThan(String value) {
            addCriterion("opener_user_id <", value, "openerUserId");
            return (Criteria) this;
        }

        public Criteria andOpenerUserIdLessThanOrEqualTo(String value) {
            addCriterion("opener_user_id <=", value, "openerUserId");
            return (Criteria) this;
        }

        public Criteria andOpenerUserIdLike(String value) {
            addCriterion("opener_user_id like", value, "openerUserId");
            return (Criteria) this;
        }

        public Criteria andOpenerUserIdNotLike(String value) {
            addCriterion("opener_user_id not like", value, "openerUserId");
            return (Criteria) this;
        }

        public Criteria andOpenerUserIdIn(List<String> values) {
            addCriterion("opener_user_id in", values, "openerUserId");
            return (Criteria) this;
        }

        public Criteria andOpenerUserIdNotIn(List<String> values) {
            addCriterion("opener_user_id not in", values, "openerUserId");
            return (Criteria) this;
        }

        public Criteria andOpenerUserIdBetween(String value1, String value2) {
            addCriterion("opener_user_id between", value1, value2, "openerUserId");
            return (Criteria) this;
        }

        public Criteria andOpenerUserIdNotBetween(String value1, String value2) {
            addCriterion("opener_user_id not between", value1, value2, "openerUserId");
            return (Criteria) this;
        }

        public Criteria andProtectionDrawsIsNull() {
            addCriterion("protection_draws is null");
            return (Criteria) this;
        }

        public Criteria andProtectionDrawsIsNotNull() {
            addCriterion("protection_draws is not null");
            return (Criteria) this;
        }

        public Criteria andProtectionDrawsEqualTo(Integer value) {
            addCriterion("protection_draws =", value, "protectionDraws");
            return (Criteria) this;
        }

        public Criteria andProtectionDrawsNotEqualTo(Integer value) {
            addCriterion("protection_draws <>", value, "protectionDraws");
            return (Criteria) this;
        }

        public Criteria andProtectionDrawsGreaterThan(Integer value) {
            addCriterion("protection_draws >", value, "protectionDraws");
            return (Criteria) this;
        }

        public Criteria andProtectionDrawsGreaterThanOrEqualTo(Integer value) {
            addCriterion("protection_draws >=", value, "protectionDraws");
            return (Criteria) this;
        }

        public Criteria andProtectionDrawsLessThan(Integer value) {
            addCriterion("protection_draws <", value, "protectionDraws");
            return (Criteria) this;
        }

        public Criteria andProtectionDrawsLessThanOrEqualTo(Integer value) {
            addCriterion("protection_draws <=", value, "protectionDraws");
            return (Criteria) this;
        }

        public Criteria andProtectionDrawsIn(List<Integer> values) {
            addCriterion("protection_draws in", values, "protectionDraws");
            return (Criteria) this;
        }

        public Criteria andProtectionDrawsNotIn(List<Integer> values) {
            addCriterion("protection_draws not in", values, "protectionDraws");
            return (Criteria) this;
        }

        public Criteria andProtectionDrawsBetween(Integer value1, Integer value2) {
            addCriterion("protection_draws between", value1, value2, "protectionDraws");
            return (Criteria) this;
        }

        public Criteria andProtectionDrawsNotBetween(Integer value1, Integer value2) {
            addCriterion("protection_draws not between", value1, value2, "protectionDraws");
            return (Criteria) this;
        }

        public Criteria andProtectionStartTimeIsNull() {
            addCriterion("protection_start_time is null");
            return (Criteria) this;
        }

        public Criteria andProtectionStartTimeIsNotNull() {
            addCriterion("protection_start_time is not null");
            return (Criteria) this;
        }

        public Criteria andProtectionStartTimeEqualTo(LocalDateTime value) {
            addCriterion("protection_start_time =", value, "protectionStartTime");
            return (Criteria) this;
        }

        public Criteria andProtectionStartTimeNotEqualTo(LocalDateTime value) {
            addCriterion("protection_start_time <>", value, "protectionStartTime");
            return (Criteria) this;
        }

        public Criteria andProtectionStartTimeGreaterThan(LocalDateTime value) {
            addCriterion("protection_start_time >", value, "protectionStartTime");
            return (Criteria) this;
        }

        public Criteria andProtectionStartTimeGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("protection_start_time >=", value, "protectionStartTime");
            return (Criteria) this;
        }

        public Criteria andProtectionStartTimeLessThan(LocalDateTime value) {
            addCriterion("protection_start_time <", value, "protectionStartTime");
            return (Criteria) this;
        }

        public Criteria andProtectionStartTimeLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("protection_start_time <=", value, "protectionStartTime");
            return (Criteria) this;
        }

        public Criteria andProtectionStartTimeIn(List<LocalDateTime> values) {
            addCriterion("protection_start_time in", values, "protectionStartTime");
            return (Criteria) this;
        }

        public Criteria andProtectionStartTimeNotIn(List<LocalDateTime> values) {
            addCriterion("protection_start_time not in", values, "protectionStartTime");
            return (Criteria) this;
        }

        public Criteria andProtectionStartTimeBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("protection_start_time between", value1, value2, "protectionStartTime");
            return (Criteria) this;
        }

        public Criteria andProtectionStartTimeNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("protection_start_time not between", value1, value2, "protectionStartTime");
            return (Criteria) this;
        }

        public Criteria andProtectionEndTimeIsNull() {
            addCriterion("protection_end_time is null");
            return (Criteria) this;
        }

        public Criteria andProtectionEndTimeIsNotNull() {
            addCriterion("protection_end_time is not null");
            return (Criteria) this;
        }

        public Criteria andProtectionEndTimeEqualTo(LocalDateTime value) {
            addCriterion("protection_end_time =", value, "protectionEndTime");
            return (Criteria) this;
        }

        public Criteria andProtectionEndTimeNotEqualTo(LocalDateTime value) {
            addCriterion("protection_end_time <>", value, "protectionEndTime");
            return (Criteria) this;
        }

        public Criteria andProtectionEndTimeGreaterThan(LocalDateTime value) {
            addCriterion("protection_end_time >", value, "protectionEndTime");
            return (Criteria) this;
        }

        public Criteria andProtectionEndTimeGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("protection_end_time >=", value, "protectionEndTime");
            return (Criteria) this;
        }

        public Criteria andProtectionEndTimeLessThan(LocalDateTime value) {
            addCriterion("protection_end_time <", value, "protectionEndTime");
            return (Criteria) this;
        }

        public Criteria andProtectionEndTimeLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("protection_end_time <=", value, "protectionEndTime");
            return (Criteria) this;
        }

        public Criteria andProtectionEndTimeIn(List<LocalDateTime> values) {
            addCriterion("protection_end_time in", values, "protectionEndTime");
            return (Criteria) this;
        }

        public Criteria andProtectionEndTimeNotIn(List<LocalDateTime> values) {
            addCriterion("protection_end_time not in", values, "protectionEndTime");
            return (Criteria) this;
        }

        public Criteria andProtectionEndTimeBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("protection_end_time between", value1, value2, "protectionEndTime");
            return (Criteria) this;
        }

        public Criteria andProtectionEndTimeNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("protection_end_time not between", value1, value2, "protectionEndTime");
            return (Criteria) this;
        }

        public Criteria andOpenerDrawCountIsNull() {
            addCriterion("opener_draw_count is null");
            return (Criteria) this;
        }

        public Criteria andOpenerDrawCountIsNotNull() {
            addCriterion("opener_draw_count is not null");
            return (Criteria) this;
        }

        public Criteria andOpenerDrawCountEqualTo(Integer value) {
            addCriterion("opener_draw_count =", value, "openerDrawCount");
            return (Criteria) this;
        }

        public Criteria andOpenerDrawCountNotEqualTo(Integer value) {
            addCriterion("opener_draw_count <>", value, "openerDrawCount");
            return (Criteria) this;
        }

        public Criteria andOpenerDrawCountGreaterThan(Integer value) {
            addCriterion("opener_draw_count >", value, "openerDrawCount");
            return (Criteria) this;
        }

        public Criteria andOpenerDrawCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("opener_draw_count >=", value, "openerDrawCount");
            return (Criteria) this;
        }

        public Criteria andOpenerDrawCountLessThan(Integer value) {
            addCriterion("opener_draw_count <", value, "openerDrawCount");
            return (Criteria) this;
        }

        public Criteria andOpenerDrawCountLessThanOrEqualTo(Integer value) {
            addCriterion("opener_draw_count <=", value, "openerDrawCount");
            return (Criteria) this;
        }

        public Criteria andOpenerDrawCountIn(List<Integer> values) {
            addCriterion("opener_draw_count in", values, "openerDrawCount");
            return (Criteria) this;
        }

        public Criteria andOpenerDrawCountNotIn(List<Integer> values) {
            addCriterion("opener_draw_count not in", values, "openerDrawCount");
            return (Criteria) this;
        }

        public Criteria andOpenerDrawCountBetween(Integer value1, Integer value2) {
            addCriterion("opener_draw_count between", value1, value2, "openerDrawCount");
            return (Criteria) this;
        }

        public Criteria andOpenerDrawCountNotBetween(Integer value1, Integer value2) {
            addCriterion("opener_draw_count not between", value1, value2, "openerDrawCount");
            return (Criteria) this;
        }

        public Criteria andOpenerTotalCostIsNull() {
            addCriterion("opener_total_cost is null");
            return (Criteria) this;
        }

        public Criteria andOpenerTotalCostIsNotNull() {
            addCriterion("opener_total_cost is not null");
            return (Criteria) this;
        }

        public Criteria andOpenerTotalCostEqualTo(Long value) {
            addCriterion("opener_total_cost =", value, "openerTotalCost");
            return (Criteria) this;
        }

        public Criteria andOpenerTotalCostNotEqualTo(Long value) {
            addCriterion("opener_total_cost <>", value, "openerTotalCost");
            return (Criteria) this;
        }

        public Criteria andOpenerTotalCostGreaterThan(Long value) {
            addCriterion("opener_total_cost >", value, "openerTotalCost");
            return (Criteria) this;
        }

        public Criteria andOpenerTotalCostGreaterThanOrEqualTo(Long value) {
            addCriterion("opener_total_cost >=", value, "openerTotalCost");
            return (Criteria) this;
        }

        public Criteria andOpenerTotalCostLessThan(Long value) {
            addCriterion("opener_total_cost <", value, "openerTotalCost");
            return (Criteria) this;
        }

        public Criteria andOpenerTotalCostLessThanOrEqualTo(Long value) {
            addCriterion("opener_total_cost <=", value, "openerTotalCost");
            return (Criteria) this;
        }

        public Criteria andOpenerTotalCostIn(List<Long> values) {
            addCriterion("opener_total_cost in", values, "openerTotalCost");
            return (Criteria) this;
        }

        public Criteria andOpenerTotalCostNotIn(List<Long> values) {
            addCriterion("opener_total_cost not in", values, "openerTotalCost");
            return (Criteria) this;
        }

        public Criteria andOpenerTotalCostBetween(Long value1, Long value2) {
            addCriterion("opener_total_cost between", value1, value2, "openerTotalCost");
            return (Criteria) this;
        }

        public Criteria andOpenerTotalCostNotBetween(Long value1, Long value2) {
            addCriterion("opener_total_cost not between", value1, value2, "openerTotalCost");
            return (Criteria) this;
        }

        public Criteria andFreeDrawEnabledIsNull() {
            addCriterion("free_draw_enabled is null");
            return (Criteria) this;
        }

        public Criteria andFreeDrawEnabledIsNotNull() {
            addCriterion("free_draw_enabled is not null");
            return (Criteria) this;
        }

        public Criteria andFreeDrawEnabledEqualTo(Byte value) {
            addCriterion("free_draw_enabled =", value, "freeDrawEnabled");
            return (Criteria) this;
        }

        public Criteria andFreeDrawEnabledNotEqualTo(Byte value) {
            addCriterion("free_draw_enabled <>", value, "freeDrawEnabled");
            return (Criteria) this;
        }

        public Criteria andFreeDrawEnabledGreaterThan(Byte value) {
            addCriterion("free_draw_enabled >", value, "freeDrawEnabled");
            return (Criteria) this;
        }

        public Criteria andFreeDrawEnabledGreaterThanOrEqualTo(Byte value) {
            addCriterion("free_draw_enabled >=", value, "freeDrawEnabled");
            return (Criteria) this;
        }

        public Criteria andFreeDrawEnabledLessThan(Byte value) {
            addCriterion("free_draw_enabled <", value, "freeDrawEnabled");
            return (Criteria) this;
        }

        public Criteria andFreeDrawEnabledLessThanOrEqualTo(Byte value) {
            addCriterion("free_draw_enabled <=", value, "freeDrawEnabled");
            return (Criteria) this;
        }

        public Criteria andFreeDrawEnabledIn(List<Byte> values) {
            addCriterion("free_draw_enabled in", values, "freeDrawEnabled");
            return (Criteria) this;
        }

        public Criteria andFreeDrawEnabledNotIn(List<Byte> values) {
            addCriterion("free_draw_enabled not in", values, "freeDrawEnabled");
            return (Criteria) this;
        }

        public Criteria andFreeDrawEnabledBetween(Byte value1, Byte value2) {
            addCriterion("free_draw_enabled between", value1, value2, "freeDrawEnabled");
            return (Criteria) this;
        }

        public Criteria andFreeDrawEnabledNotBetween(Byte value1, Byte value2) {
            addCriterion("free_draw_enabled not between", value1, value2, "freeDrawEnabled");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredIsNull() {
            addCriterion("free_draw_triggered is null");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredIsNotNull() {
            addCriterion("free_draw_triggered is not null");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredEqualTo(Byte value) {
            addCriterion("free_draw_triggered =", value, "freeDrawTriggered");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredNotEqualTo(Byte value) {
            addCriterion("free_draw_triggered <>", value, "freeDrawTriggered");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredGreaterThan(Byte value) {
            addCriterion("free_draw_triggered >", value, "freeDrawTriggered");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredGreaterThanOrEqualTo(Byte value) {
            addCriterion("free_draw_triggered >=", value, "freeDrawTriggered");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredLessThan(Byte value) {
            addCriterion("free_draw_triggered <", value, "freeDrawTriggered");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredLessThanOrEqualTo(Byte value) {
            addCriterion("free_draw_triggered <=", value, "freeDrawTriggered");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredIn(List<Byte> values) {
            addCriterion("free_draw_triggered in", values, "freeDrawTriggered");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredNotIn(List<Byte> values) {
            addCriterion("free_draw_triggered not in", values, "freeDrawTriggered");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredBetween(Byte value1, Byte value2) {
            addCriterion("free_draw_triggered between", value1, value2, "freeDrawTriggered");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredNotBetween(Byte value1, Byte value2) {
            addCriterion("free_draw_triggered not between", value1, value2, "freeDrawTriggered");
            return (Criteria) this;
        }

        public Criteria andFreeDrawRefundAmountIsNull() {
            addCriterion("free_draw_refund_amount is null");
            return (Criteria) this;
        }

        public Criteria andFreeDrawRefundAmountIsNotNull() {
            addCriterion("free_draw_refund_amount is not null");
            return (Criteria) this;
        }

        public Criteria andFreeDrawRefundAmountEqualTo(Long value) {
            addCriterion("free_draw_refund_amount =", value, "freeDrawRefundAmount");
            return (Criteria) this;
        }

        public Criteria andFreeDrawRefundAmountNotEqualTo(Long value) {
            addCriterion("free_draw_refund_amount <>", value, "freeDrawRefundAmount");
            return (Criteria) this;
        }

        public Criteria andFreeDrawRefundAmountGreaterThan(Long value) {
            addCriterion("free_draw_refund_amount >", value, "freeDrawRefundAmount");
            return (Criteria) this;
        }

        public Criteria andFreeDrawRefundAmountGreaterThanOrEqualTo(Long value) {
            addCriterion("free_draw_refund_amount >=", value, "freeDrawRefundAmount");
            return (Criteria) this;
        }

        public Criteria andFreeDrawRefundAmountLessThan(Long value) {
            addCriterion("free_draw_refund_amount <", value, "freeDrawRefundAmount");
            return (Criteria) this;
        }

        public Criteria andFreeDrawRefundAmountLessThanOrEqualTo(Long value) {
            addCriterion("free_draw_refund_amount <=", value, "freeDrawRefundAmount");
            return (Criteria) this;
        }

        public Criteria andFreeDrawRefundAmountIn(List<Long> values) {
            addCriterion("free_draw_refund_amount in", values, "freeDrawRefundAmount");
            return (Criteria) this;
        }

        public Criteria andFreeDrawRefundAmountNotIn(List<Long> values) {
            addCriterion("free_draw_refund_amount not in", values, "freeDrawRefundAmount");
            return (Criteria) this;
        }

        public Criteria andFreeDrawRefundAmountBetween(Long value1, Long value2) {
            addCriterion("free_draw_refund_amount between", value1, value2, "freeDrawRefundAmount");
            return (Criteria) this;
        }

        public Criteria andFreeDrawRefundAmountNotBetween(Long value1, Long value2) {
            addCriterion("free_draw_refund_amount not between", value1, value2, "freeDrawRefundAmount");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredAtIsNull() {
            addCriterion("free_draw_triggered_at is null");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredAtIsNotNull() {
            addCriterion("free_draw_triggered_at is not null");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredAtEqualTo(LocalDateTime value) {
            addCriterion("free_draw_triggered_at =", value, "freeDrawTriggeredAt");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredAtNotEqualTo(LocalDateTime value) {
            addCriterion("free_draw_triggered_at <>", value, "freeDrawTriggeredAt");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredAtGreaterThan(LocalDateTime value) {
            addCriterion("free_draw_triggered_at >", value, "freeDrawTriggeredAt");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("free_draw_triggered_at >=", value, "freeDrawTriggeredAt");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredAtLessThan(LocalDateTime value) {
            addCriterion("free_draw_triggered_at <", value, "freeDrawTriggeredAt");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("free_draw_triggered_at <=", value, "freeDrawTriggeredAt");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredAtIn(List<LocalDateTime> values) {
            addCriterion("free_draw_triggered_at in", values, "freeDrawTriggeredAt");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredAtNotIn(List<LocalDateTime> values) {
            addCriterion("free_draw_triggered_at not in", values, "freeDrawTriggeredAt");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("free_draw_triggered_at between", value1, value2, "freeDrawTriggeredAt");
            return (Criteria) this;
        }

        public Criteria andFreeDrawTriggeredAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("free_draw_triggered_at not between", value1, value2, "freeDrawTriggeredAt");
            return (Criteria) this;
        }

        public Criteria andFreeDrawPrizeIdIsNull() {
            addCriterion("free_draw_prize_id is null");
            return (Criteria) this;
        }

        public Criteria andFreeDrawPrizeIdIsNotNull() {
            addCriterion("free_draw_prize_id is not null");
            return (Criteria) this;
        }

        public Criteria andFreeDrawPrizeIdEqualTo(String value) {
            addCriterion("free_draw_prize_id =", value, "freeDrawPrizeId");
            return (Criteria) this;
        }

        public Criteria andFreeDrawPrizeIdNotEqualTo(String value) {
            addCriterion("free_draw_prize_id <>", value, "freeDrawPrizeId");
            return (Criteria) this;
        }

        public Criteria andFreeDrawPrizeIdGreaterThan(String value) {
            addCriterion("free_draw_prize_id >", value, "freeDrawPrizeId");
            return (Criteria) this;
        }

        public Criteria andFreeDrawPrizeIdGreaterThanOrEqualTo(String value) {
            addCriterion("free_draw_prize_id >=", value, "freeDrawPrizeId");
            return (Criteria) this;
        }

        public Criteria andFreeDrawPrizeIdLessThan(String value) {
            addCriterion("free_draw_prize_id <", value, "freeDrawPrizeId");
            return (Criteria) this;
        }

        public Criteria andFreeDrawPrizeIdLessThanOrEqualTo(String value) {
            addCriterion("free_draw_prize_id <=", value, "freeDrawPrizeId");
            return (Criteria) this;
        }

        public Criteria andFreeDrawPrizeIdLike(String value) {
            addCriterion("free_draw_prize_id like", value, "freeDrawPrizeId");
            return (Criteria) this;
        }

        public Criteria andFreeDrawPrizeIdNotLike(String value) {
            addCriterion("free_draw_prize_id not like", value, "freeDrawPrizeId");
            return (Criteria) this;
        }

        public Criteria andFreeDrawPrizeIdIn(List<String> values) {
            addCriterion("free_draw_prize_id in", values, "freeDrawPrizeId");
            return (Criteria) this;
        }

        public Criteria andFreeDrawPrizeIdNotIn(List<String> values) {
            addCriterion("free_draw_prize_id not in", values, "freeDrawPrizeId");
            return (Criteria) this;
        }

        public Criteria andFreeDrawPrizeIdBetween(String value1, String value2) {
            addCriterion("free_draw_prize_id between", value1, value2, "freeDrawPrizeId");
            return (Criteria) this;
        }

        public Criteria andFreeDrawPrizeIdNotBetween(String value1, String value2) {
            addCriterion("free_draw_prize_id not between", value1, value2, "freeDrawPrizeId");
            return (Criteria) this;
        }

        public Criteria andPlayerDesignatedNumbersIsNull() {
            addCriterion("player_designated_numbers is null");
            return (Criteria) this;
        }

        public Criteria andPlayerDesignatedNumbersIsNotNull() {
            addCriterion("player_designated_numbers is not null");
            return (Criteria) this;
        }

        public Criteria andPlayerDesignatedNumbersEqualTo(String value) {
            addCriterion("player_designated_numbers =", value, "playerDesignatedNumbers");
            return (Criteria) this;
        }

        public Criteria andPlayerDesignatedNumbersNotEqualTo(String value) {
            addCriterion("player_designated_numbers <>", value, "playerDesignatedNumbers");
            return (Criteria) this;
        }

        public Criteria andPlayerDesignatedNumbersGreaterThan(String value) {
            addCriterion("player_designated_numbers >", value, "playerDesignatedNumbers");
            return (Criteria) this;
        }

        public Criteria andPlayerDesignatedNumbersGreaterThanOrEqualTo(String value) {
            addCriterion("player_designated_numbers >=", value, "playerDesignatedNumbers");
            return (Criteria) this;
        }

        public Criteria andPlayerDesignatedNumbersLessThan(String value) {
            addCriterion("player_designated_numbers <", value, "playerDesignatedNumbers");
            return (Criteria) this;
        }

        public Criteria andPlayerDesignatedNumbersLessThanOrEqualTo(String value) {
            addCriterion("player_designated_numbers <=", value, "playerDesignatedNumbers");
            return (Criteria) this;
        }

        public Criteria andPlayerDesignatedNumbersLike(String value) {
            addCriterion("player_designated_numbers like", value, "playerDesignatedNumbers");
            return (Criteria) this;
        }

        public Criteria andPlayerDesignatedNumbersNotLike(String value) {
            addCriterion("player_designated_numbers not like", value, "playerDesignatedNumbers");
            return (Criteria) this;
        }

        public Criteria andPlayerDesignatedNumbersIn(List<String> values) {
            addCriterion("player_designated_numbers in", values, "playerDesignatedNumbers");
            return (Criteria) this;
        }

        public Criteria andPlayerDesignatedNumbersNotIn(List<String> values) {
            addCriterion("player_designated_numbers not in", values, "playerDesignatedNumbers");
            return (Criteria) this;
        }

        public Criteria andPlayerDesignatedNumbersBetween(String value1, String value2) {
            addCriterion("player_designated_numbers between", value1, value2, "playerDesignatedNumbers");
            return (Criteria) this;
        }

        public Criteria andPlayerDesignatedNumbersNotBetween(String value1, String value2) {
            addCriterion("player_designated_numbers not between", value1, value2, "playerDesignatedNumbers");
            return (Criteria) this;
        }

        public Criteria andDesignationDeadlineIsNull() {
            addCriterion("designation_deadline is null");
            return (Criteria) this;
        }

        public Criteria andDesignationDeadlineIsNotNull() {
            addCriterion("designation_deadline is not null");
            return (Criteria) this;
        }

        public Criteria andDesignationDeadlineEqualTo(LocalDateTime value) {
            addCriterion("designation_deadline =", value, "designationDeadline");
            return (Criteria) this;
        }

        public Criteria andDesignationDeadlineNotEqualTo(LocalDateTime value) {
            addCriterion("designation_deadline <>", value, "designationDeadline");
            return (Criteria) this;
        }

        public Criteria andDesignationDeadlineGreaterThan(LocalDateTime value) {
            addCriterion("designation_deadline >", value, "designationDeadline");
            return (Criteria) this;
        }

        public Criteria andDesignationDeadlineGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("designation_deadline >=", value, "designationDeadline");
            return (Criteria) this;
        }

        public Criteria andDesignationDeadlineLessThan(LocalDateTime value) {
            addCriterion("designation_deadline <", value, "designationDeadline");
            return (Criteria) this;
        }

        public Criteria andDesignationDeadlineLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("designation_deadline <=", value, "designationDeadline");
            return (Criteria) this;
        }

        public Criteria andDesignationDeadlineIn(List<LocalDateTime> values) {
            addCriterion("designation_deadline in", values, "designationDeadline");
            return (Criteria) this;
        }

        public Criteria andDesignationDeadlineNotIn(List<LocalDateTime> values) {
            addCriterion("designation_deadline not in", values, "designationDeadline");
            return (Criteria) this;
        }

        public Criteria andDesignationDeadlineBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("designation_deadline between", value1, value2, "designationDeadline");
            return (Criteria) this;
        }

        public Criteria andDesignationDeadlineNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("designation_deadline not between", value1, value2, "designationDeadline");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("status like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("status not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andCompletedAtIsNull() {
            addCriterion("completed_at is null");
            return (Criteria) this;
        }

        public Criteria andCompletedAtIsNotNull() {
            addCriterion("completed_at is not null");
            return (Criteria) this;
        }

        public Criteria andCompletedAtEqualTo(LocalDateTime value) {
            addCriterion("completed_at =", value, "completedAt");
            return (Criteria) this;
        }

        public Criteria andCompletedAtNotEqualTo(LocalDateTime value) {
            addCriterion("completed_at <>", value, "completedAt");
            return (Criteria) this;
        }

        public Criteria andCompletedAtGreaterThan(LocalDateTime value) {
            addCriterion("completed_at >", value, "completedAt");
            return (Criteria) this;
        }

        public Criteria andCompletedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("completed_at >=", value, "completedAt");
            return (Criteria) this;
        }

        public Criteria andCompletedAtLessThan(LocalDateTime value) {
            addCriterion("completed_at <", value, "completedAt");
            return (Criteria) this;
        }

        public Criteria andCompletedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("completed_at <=", value, "completedAt");
            return (Criteria) this;
        }

        public Criteria andCompletedAtIn(List<LocalDateTime> values) {
            addCriterion("completed_at in", values, "completedAt");
            return (Criteria) this;
        }

        public Criteria andCompletedAtNotIn(List<LocalDateTime> values) {
            addCriterion("completed_at not in", values, "completedAt");
            return (Criteria) this;
        }

        public Criteria andCompletedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("completed_at between", value1, value2, "completedAt");
            return (Criteria) this;
        }

        public Criteria andCompletedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("completed_at not between", value1, value2, "completedAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIsNull() {
            addCriterion("created_at is null");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIsNotNull() {
            addCriterion("created_at is not null");
            return (Criteria) this;
        }

        public Criteria andCreatedAtEqualTo(LocalDateTime value) {
            addCriterion("created_at =", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotEqualTo(LocalDateTime value) {
            addCriterion("created_at <>", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtGreaterThan(LocalDateTime value) {
            addCriterion("created_at >", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("created_at >=", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtLessThan(LocalDateTime value) {
            addCriterion("created_at <", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("created_at <=", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIn(List<LocalDateTime> values) {
            addCriterion("created_at in", values, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotIn(List<LocalDateTime> values) {
            addCriterion("created_at not in", values, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("created_at between", value1, value2, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("created_at not between", value1, value2, "createdAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIsNull() {
            addCriterion("updated_at is null");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIsNotNull() {
            addCriterion("updated_at is not null");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtEqualTo(LocalDateTime value) {
            addCriterion("updated_at =", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotEqualTo(LocalDateTime value) {
            addCriterion("updated_at <>", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtGreaterThan(LocalDateTime value) {
            addCriterion("updated_at >", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("updated_at >=", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtLessThan(LocalDateTime value) {
            addCriterion("updated_at <", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("updated_at <=", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIn(List<LocalDateTime> values) {
            addCriterion("updated_at in", values, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotIn(List<LocalDateTime> values) {
            addCriterion("updated_at not in", values, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("updated_at between", value1, value2, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("updated_at not between", value1, value2, "updatedAt");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
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

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}