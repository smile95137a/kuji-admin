package com.group.admin.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LotteryTicketExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public LotteryTicketExample() {
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

        public Criteria andTicketNumberIsNull() {
            addCriterion("ticket_number is null");
            return (Criteria) this;
        }

        public Criteria andTicketNumberIsNotNull() {
            addCriterion("ticket_number is not null");
            return (Criteria) this;
        }

        public Criteria andTicketNumberEqualTo(Integer value) {
            addCriterion("ticket_number =", value, "ticketNumber");
            return (Criteria) this;
        }

        public Criteria andTicketNumberNotEqualTo(Integer value) {
            addCriterion("ticket_number <>", value, "ticketNumber");
            return (Criteria) this;
        }

        public Criteria andTicketNumberGreaterThan(Integer value) {
            addCriterion("ticket_number >", value, "ticketNumber");
            return (Criteria) this;
        }

        public Criteria andTicketNumberGreaterThanOrEqualTo(Integer value) {
            addCriterion("ticket_number >=", value, "ticketNumber");
            return (Criteria) this;
        }

        public Criteria andTicketNumberLessThan(Integer value) {
            addCriterion("ticket_number <", value, "ticketNumber");
            return (Criteria) this;
        }

        public Criteria andTicketNumberLessThanOrEqualTo(Integer value) {
            addCriterion("ticket_number <=", value, "ticketNumber");
            return (Criteria) this;
        }

        public Criteria andTicketNumberIn(List<Integer> values) {
            addCriterion("ticket_number in", values, "ticketNumber");
            return (Criteria) this;
        }

        public Criteria andTicketNumberNotIn(List<Integer> values) {
            addCriterion("ticket_number not in", values, "ticketNumber");
            return (Criteria) this;
        }

        public Criteria andTicketNumberBetween(Integer value1, Integer value2) {
            addCriterion("ticket_number between", value1, value2, "ticketNumber");
            return (Criteria) this;
        }

        public Criteria andTicketNumberNotBetween(Integer value1, Integer value2) {
            addCriterion("ticket_number not between", value1, value2, "ticketNumber");
            return (Criteria) this;
        }

        public Criteria andRevealedNumberIsNull() {
            addCriterion("revealed_number is null");
            return (Criteria) this;
        }

        public Criteria andRevealedNumberIsNotNull() {
            addCriterion("revealed_number is not null");
            return (Criteria) this;
        }

        public Criteria andRevealedNumberEqualTo(Integer value) {
            addCriterion("revealed_number =", value, "revealedNumber");
            return (Criteria) this;
        }

        public Criteria andRevealedNumberNotEqualTo(Integer value) {
            addCriterion("revealed_number <>", value, "revealedNumber");
            return (Criteria) this;
        }

        public Criteria andRevealedNumberGreaterThan(Integer value) {
            addCriterion("revealed_number >", value, "revealedNumber");
            return (Criteria) this;
        }

        public Criteria andRevealedNumberGreaterThanOrEqualTo(Integer value) {
            addCriterion("revealed_number >=", value, "revealedNumber");
            return (Criteria) this;
        }

        public Criteria andRevealedNumberLessThan(Integer value) {
            addCriterion("revealed_number <", value, "revealedNumber");
            return (Criteria) this;
        }

        public Criteria andRevealedNumberLessThanOrEqualTo(Integer value) {
            addCriterion("revealed_number <=", value, "revealedNumber");
            return (Criteria) this;
        }

        public Criteria andRevealedNumberIn(List<Integer> values) {
            addCriterion("revealed_number in", values, "revealedNumber");
            return (Criteria) this;
        }

        public Criteria andRevealedNumberNotIn(List<Integer> values) {
            addCriterion("revealed_number not in", values, "revealedNumber");
            return (Criteria) this;
        }

        public Criteria andRevealedNumberBetween(Integer value1, Integer value2) {
            addCriterion("revealed_number between", value1, value2, "revealedNumber");
            return (Criteria) this;
        }

        public Criteria andRevealedNumberNotBetween(Integer value1, Integer value2) {
            addCriterion("revealed_number not between", value1, value2, "revealedNumber");
            return (Criteria) this;
        }

        public Criteria andPrizeIdIsNull() {
            addCriterion("prize_id is null");
            return (Criteria) this;
        }

        public Criteria andPrizeIdIsNotNull() {
            addCriterion("prize_id is not null");
            return (Criteria) this;
        }

        public Criteria andPrizeIdEqualTo(String value) {
            addCriterion("prize_id =", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdNotEqualTo(String value) {
            addCriterion("prize_id <>", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdGreaterThan(String value) {
            addCriterion("prize_id >", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdGreaterThanOrEqualTo(String value) {
            addCriterion("prize_id >=", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdLessThan(String value) {
            addCriterion("prize_id <", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdLessThanOrEqualTo(String value) {
            addCriterion("prize_id <=", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdLike(String value) {
            addCriterion("prize_id like", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdNotLike(String value) {
            addCriterion("prize_id not like", value, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdIn(List<String> values) {
            addCriterion("prize_id in", values, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdNotIn(List<String> values) {
            addCriterion("prize_id not in", values, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdBetween(String value1, String value2) {
            addCriterion("prize_id between", value1, value2, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeIdNotBetween(String value1, String value2) {
            addCriterion("prize_id not between", value1, value2, "prizeId");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelIsNull() {
            addCriterion("prize_level is null");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelIsNotNull() {
            addCriterion("prize_level is not null");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelEqualTo(String value) {
            addCriterion("prize_level =", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelNotEqualTo(String value) {
            addCriterion("prize_level <>", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelGreaterThan(String value) {
            addCriterion("prize_level >", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelGreaterThanOrEqualTo(String value) {
            addCriterion("prize_level >=", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelLessThan(String value) {
            addCriterion("prize_level <", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelLessThanOrEqualTo(String value) {
            addCriterion("prize_level <=", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelLike(String value) {
            addCriterion("prize_level like", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelNotLike(String value) {
            addCriterion("prize_level not like", value, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelIn(List<String> values) {
            addCriterion("prize_level in", values, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelNotIn(List<String> values) {
            addCriterion("prize_level not in", values, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelBetween(String value1, String value2) {
            addCriterion("prize_level between", value1, value2, "prizeLevel");
            return (Criteria) this;
        }

        public Criteria andPrizeLevelNotBetween(String value1, String value2) {
            addCriterion("prize_level not between", value1, value2, "prizeLevel");
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

        public Criteria andDrawnByIsNull() {
            addCriterion("drawn_by is null");
            return (Criteria) this;
        }

        public Criteria andDrawnByIsNotNull() {
            addCriterion("drawn_by is not null");
            return (Criteria) this;
        }

        public Criteria andDrawnByEqualTo(String value) {
            addCriterion("drawn_by =", value, "drawnBy");
            return (Criteria) this;
        }

        public Criteria andDrawnByNotEqualTo(String value) {
            addCriterion("drawn_by <>", value, "drawnBy");
            return (Criteria) this;
        }

        public Criteria andDrawnByGreaterThan(String value) {
            addCriterion("drawn_by >", value, "drawnBy");
            return (Criteria) this;
        }

        public Criteria andDrawnByGreaterThanOrEqualTo(String value) {
            addCriterion("drawn_by >=", value, "drawnBy");
            return (Criteria) this;
        }

        public Criteria andDrawnByLessThan(String value) {
            addCriterion("drawn_by <", value, "drawnBy");
            return (Criteria) this;
        }

        public Criteria andDrawnByLessThanOrEqualTo(String value) {
            addCriterion("drawn_by <=", value, "drawnBy");
            return (Criteria) this;
        }

        public Criteria andDrawnByLike(String value) {
            addCriterion("drawn_by like", value, "drawnBy");
            return (Criteria) this;
        }

        public Criteria andDrawnByNotLike(String value) {
            addCriterion("drawn_by not like", value, "drawnBy");
            return (Criteria) this;
        }

        public Criteria andDrawnByIn(List<String> values) {
            addCriterion("drawn_by in", values, "drawnBy");
            return (Criteria) this;
        }

        public Criteria andDrawnByNotIn(List<String> values) {
            addCriterion("drawn_by not in", values, "drawnBy");
            return (Criteria) this;
        }

        public Criteria andDrawnByBetween(String value1, String value2) {
            addCriterion("drawn_by between", value1, value2, "drawnBy");
            return (Criteria) this;
        }

        public Criteria andDrawnByNotBetween(String value1, String value2) {
            addCriterion("drawn_by not between", value1, value2, "drawnBy");
            return (Criteria) this;
        }

        public Criteria andDrawnAtIsNull() {
            addCriterion("drawn_at is null");
            return (Criteria) this;
        }

        public Criteria andDrawnAtIsNotNull() {
            addCriterion("drawn_at is not null");
            return (Criteria) this;
        }

        public Criteria andDrawnAtEqualTo(LocalDateTime value) {
            addCriterion("drawn_at =", value, "drawnAt");
            return (Criteria) this;
        }

        public Criteria andDrawnAtNotEqualTo(LocalDateTime value) {
            addCriterion("drawn_at <>", value, "drawnAt");
            return (Criteria) this;
        }

        public Criteria andDrawnAtGreaterThan(LocalDateTime value) {
            addCriterion("drawn_at >", value, "drawnAt");
            return (Criteria) this;
        }

        public Criteria andDrawnAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("drawn_at >=", value, "drawnAt");
            return (Criteria) this;
        }

        public Criteria andDrawnAtLessThan(LocalDateTime value) {
            addCriterion("drawn_at <", value, "drawnAt");
            return (Criteria) this;
        }

        public Criteria andDrawnAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("drawn_at <=", value, "drawnAt");
            return (Criteria) this;
        }

        public Criteria andDrawnAtIn(List<LocalDateTime> values) {
            addCriterion("drawn_at in", values, "drawnAt");
            return (Criteria) this;
        }

        public Criteria andDrawnAtNotIn(List<LocalDateTime> values) {
            addCriterion("drawn_at not in", values, "drawnAt");
            return (Criteria) this;
        }

        public Criteria andDrawnAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("drawn_at between", value1, value2, "drawnAt");
            return (Criteria) this;
        }

        public Criteria andDrawnAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("drawn_at not between", value1, value2, "drawnAt");
            return (Criteria) this;
        }

        public Criteria andIsDesignatedPrizeIsNull() {
            addCriterion("is_designated_prize is null");
            return (Criteria) this;
        }

        public Criteria andIsDesignatedPrizeIsNotNull() {
            addCriterion("is_designated_prize is not null");
            return (Criteria) this;
        }

        public Criteria andIsDesignatedPrizeEqualTo(Byte value) {
            addCriterion("is_designated_prize =", value, "isDesignatedPrize");
            return (Criteria) this;
        }

        public Criteria andIsDesignatedPrizeNotEqualTo(Byte value) {
            addCriterion("is_designated_prize <>", value, "isDesignatedPrize");
            return (Criteria) this;
        }

        public Criteria andIsDesignatedPrizeGreaterThan(Byte value) {
            addCriterion("is_designated_prize >", value, "isDesignatedPrize");
            return (Criteria) this;
        }

        public Criteria andIsDesignatedPrizeGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_designated_prize >=", value, "isDesignatedPrize");
            return (Criteria) this;
        }

        public Criteria andIsDesignatedPrizeLessThan(Byte value) {
            addCriterion("is_designated_prize <", value, "isDesignatedPrize");
            return (Criteria) this;
        }

        public Criteria andIsDesignatedPrizeLessThanOrEqualTo(Byte value) {
            addCriterion("is_designated_prize <=", value, "isDesignatedPrize");
            return (Criteria) this;
        }

        public Criteria andIsDesignatedPrizeIn(List<Byte> values) {
            addCriterion("is_designated_prize in", values, "isDesignatedPrize");
            return (Criteria) this;
        }

        public Criteria andIsDesignatedPrizeNotIn(List<Byte> values) {
            addCriterion("is_designated_prize not in", values, "isDesignatedPrize");
            return (Criteria) this;
        }

        public Criteria andIsDesignatedPrizeBetween(Byte value1, Byte value2) {
            addCriterion("is_designated_prize between", value1, value2, "isDesignatedPrize");
            return (Criteria) this;
        }

        public Criteria andIsDesignatedPrizeNotBetween(Byte value1, Byte value2) {
            addCriterion("is_designated_prize not between", value1, value2, "isDesignatedPrize");
            return (Criteria) this;
        }

        public Criteria andDesignatedByIsNull() {
            addCriterion("designated_by is null");
            return (Criteria) this;
        }

        public Criteria andDesignatedByIsNotNull() {
            addCriterion("designated_by is not null");
            return (Criteria) this;
        }

        public Criteria andDesignatedByEqualTo(String value) {
            addCriterion("designated_by =", value, "designatedBy");
            return (Criteria) this;
        }

        public Criteria andDesignatedByNotEqualTo(String value) {
            addCriterion("designated_by <>", value, "designatedBy");
            return (Criteria) this;
        }

        public Criteria andDesignatedByGreaterThan(String value) {
            addCriterion("designated_by >", value, "designatedBy");
            return (Criteria) this;
        }

        public Criteria andDesignatedByGreaterThanOrEqualTo(String value) {
            addCriterion("designated_by >=", value, "designatedBy");
            return (Criteria) this;
        }

        public Criteria andDesignatedByLessThan(String value) {
            addCriterion("designated_by <", value, "designatedBy");
            return (Criteria) this;
        }

        public Criteria andDesignatedByLessThanOrEqualTo(String value) {
            addCriterion("designated_by <=", value, "designatedBy");
            return (Criteria) this;
        }

        public Criteria andDesignatedByLike(String value) {
            addCriterion("designated_by like", value, "designatedBy");
            return (Criteria) this;
        }

        public Criteria andDesignatedByNotLike(String value) {
            addCriterion("designated_by not like", value, "designatedBy");
            return (Criteria) this;
        }

        public Criteria andDesignatedByIn(List<String> values) {
            addCriterion("designated_by in", values, "designatedBy");
            return (Criteria) this;
        }

        public Criteria andDesignatedByNotIn(List<String> values) {
            addCriterion("designated_by not in", values, "designatedBy");
            return (Criteria) this;
        }

        public Criteria andDesignatedByBetween(String value1, String value2) {
            addCriterion("designated_by between", value1, value2, "designatedBy");
            return (Criteria) this;
        }

        public Criteria andDesignatedByNotBetween(String value1, String value2) {
            addCriterion("designated_by not between", value1, value2, "designatedBy");
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