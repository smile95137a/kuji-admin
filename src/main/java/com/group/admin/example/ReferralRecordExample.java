package com.group.admin.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReferralRecordExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ReferralRecordExample() {
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

        public Criteria andReferralCodeIdIsNull() {
            addCriterion("referral_code_id is null");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIdIsNotNull() {
            addCriterion("referral_code_id is not null");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIdEqualTo(String value) {
            addCriterion("referral_code_id =", value, "referralCodeId");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIdNotEqualTo(String value) {
            addCriterion("referral_code_id <>", value, "referralCodeId");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIdGreaterThan(String value) {
            addCriterion("referral_code_id >", value, "referralCodeId");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIdGreaterThanOrEqualTo(String value) {
            addCriterion("referral_code_id >=", value, "referralCodeId");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIdLessThan(String value) {
            addCriterion("referral_code_id <", value, "referralCodeId");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIdLessThanOrEqualTo(String value) {
            addCriterion("referral_code_id <=", value, "referralCodeId");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIdLike(String value) {
            addCriterion("referral_code_id like", value, "referralCodeId");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIdNotLike(String value) {
            addCriterion("referral_code_id not like", value, "referralCodeId");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIdIn(List<String> values) {
            addCriterion("referral_code_id in", values, "referralCodeId");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIdNotIn(List<String> values) {
            addCriterion("referral_code_id not in", values, "referralCodeId");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIdBetween(String value1, String value2) {
            addCriterion("referral_code_id between", value1, value2, "referralCodeId");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIdNotBetween(String value1, String value2) {
            addCriterion("referral_code_id not between", value1, value2, "referralCodeId");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIsNull() {
            addCriterion("referral_code is null");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIsNotNull() {
            addCriterion("referral_code is not null");
            return (Criteria) this;
        }

        public Criteria andReferralCodeEqualTo(String value) {
            addCriterion("referral_code =", value, "referralCode");
            return (Criteria) this;
        }

        public Criteria andReferralCodeNotEqualTo(String value) {
            addCriterion("referral_code <>", value, "referralCode");
            return (Criteria) this;
        }

        public Criteria andReferralCodeGreaterThan(String value) {
            addCriterion("referral_code >", value, "referralCode");
            return (Criteria) this;
        }

        public Criteria andReferralCodeGreaterThanOrEqualTo(String value) {
            addCriterion("referral_code >=", value, "referralCode");
            return (Criteria) this;
        }

        public Criteria andReferralCodeLessThan(String value) {
            addCriterion("referral_code <", value, "referralCode");
            return (Criteria) this;
        }

        public Criteria andReferralCodeLessThanOrEqualTo(String value) {
            addCriterion("referral_code <=", value, "referralCode");
            return (Criteria) this;
        }

        public Criteria andReferralCodeLike(String value) {
            addCriterion("referral_code like", value, "referralCode");
            return (Criteria) this;
        }

        public Criteria andReferralCodeNotLike(String value) {
            addCriterion("referral_code not like", value, "referralCode");
            return (Criteria) this;
        }

        public Criteria andReferralCodeIn(List<String> values) {
            addCriterion("referral_code in", values, "referralCode");
            return (Criteria) this;
        }

        public Criteria andReferralCodeNotIn(List<String> values) {
            addCriterion("referral_code not in", values, "referralCode");
            return (Criteria) this;
        }

        public Criteria andReferralCodeBetween(String value1, String value2) {
            addCriterion("referral_code between", value1, value2, "referralCode");
            return (Criteria) this;
        }

        public Criteria andReferralCodeNotBetween(String value1, String value2) {
            addCriterion("referral_code not between", value1, value2, "referralCode");
            return (Criteria) this;
        }

        public Criteria andReferrerIdIsNull() {
            addCriterion("referrer_id is null");
            return (Criteria) this;
        }

        public Criteria andReferrerIdIsNotNull() {
            addCriterion("referrer_id is not null");
            return (Criteria) this;
        }

        public Criteria andReferrerIdEqualTo(String value) {
            addCriterion("referrer_id =", value, "referrerId");
            return (Criteria) this;
        }

        public Criteria andReferrerIdNotEqualTo(String value) {
            addCriterion("referrer_id <>", value, "referrerId");
            return (Criteria) this;
        }

        public Criteria andReferrerIdGreaterThan(String value) {
            addCriterion("referrer_id >", value, "referrerId");
            return (Criteria) this;
        }

        public Criteria andReferrerIdGreaterThanOrEqualTo(String value) {
            addCriterion("referrer_id >=", value, "referrerId");
            return (Criteria) this;
        }

        public Criteria andReferrerIdLessThan(String value) {
            addCriterion("referrer_id <", value, "referrerId");
            return (Criteria) this;
        }

        public Criteria andReferrerIdLessThanOrEqualTo(String value) {
            addCriterion("referrer_id <=", value, "referrerId");
            return (Criteria) this;
        }

        public Criteria andReferrerIdLike(String value) {
            addCriterion("referrer_id like", value, "referrerId");
            return (Criteria) this;
        }

        public Criteria andReferrerIdNotLike(String value) {
            addCriterion("referrer_id not like", value, "referrerId");
            return (Criteria) this;
        }

        public Criteria andReferrerIdIn(List<String> values) {
            addCriterion("referrer_id in", values, "referrerId");
            return (Criteria) this;
        }

        public Criteria andReferrerIdNotIn(List<String> values) {
            addCriterion("referrer_id not in", values, "referrerId");
            return (Criteria) this;
        }

        public Criteria andReferrerIdBetween(String value1, String value2) {
            addCriterion("referrer_id between", value1, value2, "referrerId");
            return (Criteria) this;
        }

        public Criteria andReferrerIdNotBetween(String value1, String value2) {
            addCriterion("referrer_id not between", value1, value2, "referrerId");
            return (Criteria) this;
        }

        public Criteria andRefereeIdIsNull() {
            addCriterion("referee_id is null");
            return (Criteria) this;
        }

        public Criteria andRefereeIdIsNotNull() {
            addCriterion("referee_id is not null");
            return (Criteria) this;
        }

        public Criteria andRefereeIdEqualTo(String value) {
            addCriterion("referee_id =", value, "refereeId");
            return (Criteria) this;
        }

        public Criteria andRefereeIdNotEqualTo(String value) {
            addCriterion("referee_id <>", value, "refereeId");
            return (Criteria) this;
        }

        public Criteria andRefereeIdGreaterThan(String value) {
            addCriterion("referee_id >", value, "refereeId");
            return (Criteria) this;
        }

        public Criteria andRefereeIdGreaterThanOrEqualTo(String value) {
            addCriterion("referee_id >=", value, "refereeId");
            return (Criteria) this;
        }

        public Criteria andRefereeIdLessThan(String value) {
            addCriterion("referee_id <", value, "refereeId");
            return (Criteria) this;
        }

        public Criteria andRefereeIdLessThanOrEqualTo(String value) {
            addCriterion("referee_id <=", value, "refereeId");
            return (Criteria) this;
        }

        public Criteria andRefereeIdLike(String value) {
            addCriterion("referee_id like", value, "refereeId");
            return (Criteria) this;
        }

        public Criteria andRefereeIdNotLike(String value) {
            addCriterion("referee_id not like", value, "refereeId");
            return (Criteria) this;
        }

        public Criteria andRefereeIdIn(List<String> values) {
            addCriterion("referee_id in", values, "refereeId");
            return (Criteria) this;
        }

        public Criteria andRefereeIdNotIn(List<String> values) {
            addCriterion("referee_id not in", values, "refereeId");
            return (Criteria) this;
        }

        public Criteria andRefereeIdBetween(String value1, String value2) {
            addCriterion("referee_id between", value1, value2, "refereeId");
            return (Criteria) this;
        }

        public Criteria andRefereeIdNotBetween(String value1, String value2) {
            addCriterion("referee_id not between", value1, value2, "refereeId");
            return (Criteria) this;
        }

        public Criteria andRefereeUsernameIsNull() {
            addCriterion("referee_username is null");
            return (Criteria) this;
        }

        public Criteria andRefereeUsernameIsNotNull() {
            addCriterion("referee_username is not null");
            return (Criteria) this;
        }

        public Criteria andRefereeUsernameEqualTo(String value) {
            addCriterion("referee_username =", value, "refereeUsername");
            return (Criteria) this;
        }

        public Criteria andRefereeUsernameNotEqualTo(String value) {
            addCriterion("referee_username <>", value, "refereeUsername");
            return (Criteria) this;
        }

        public Criteria andRefereeUsernameGreaterThan(String value) {
            addCriterion("referee_username >", value, "refereeUsername");
            return (Criteria) this;
        }

        public Criteria andRefereeUsernameGreaterThanOrEqualTo(String value) {
            addCriterion("referee_username >=", value, "refereeUsername");
            return (Criteria) this;
        }

        public Criteria andRefereeUsernameLessThan(String value) {
            addCriterion("referee_username <", value, "refereeUsername");
            return (Criteria) this;
        }

        public Criteria andRefereeUsernameLessThanOrEqualTo(String value) {
            addCriterion("referee_username <=", value, "refereeUsername");
            return (Criteria) this;
        }

        public Criteria andRefereeUsernameLike(String value) {
            addCriterion("referee_username like", value, "refereeUsername");
            return (Criteria) this;
        }

        public Criteria andRefereeUsernameNotLike(String value) {
            addCriterion("referee_username not like", value, "refereeUsername");
            return (Criteria) this;
        }

        public Criteria andRefereeUsernameIn(List<String> values) {
            addCriterion("referee_username in", values, "refereeUsername");
            return (Criteria) this;
        }

        public Criteria andRefereeUsernameNotIn(List<String> values) {
            addCriterion("referee_username not in", values, "refereeUsername");
            return (Criteria) this;
        }

        public Criteria andRefereeUsernameBetween(String value1, String value2) {
            addCriterion("referee_username between", value1, value2, "refereeUsername");
            return (Criteria) this;
        }

        public Criteria andRefereeUsernameNotBetween(String value1, String value2) {
            addCriterion("referee_username not between", value1, value2, "refereeUsername");
            return (Criteria) this;
        }

        public Criteria andRewardGoldIsNull() {
            addCriterion("reward_gold is null");
            return (Criteria) this;
        }

        public Criteria andRewardGoldIsNotNull() {
            addCriterion("reward_gold is not null");
            return (Criteria) this;
        }

        public Criteria andRewardGoldEqualTo(Long value) {
            addCriterion("reward_gold =", value, "rewardGold");
            return (Criteria) this;
        }

        public Criteria andRewardGoldNotEqualTo(Long value) {
            addCriterion("reward_gold <>", value, "rewardGold");
            return (Criteria) this;
        }

        public Criteria andRewardGoldGreaterThan(Long value) {
            addCriterion("reward_gold >", value, "rewardGold");
            return (Criteria) this;
        }

        public Criteria andRewardGoldGreaterThanOrEqualTo(Long value) {
            addCriterion("reward_gold >=", value, "rewardGold");
            return (Criteria) this;
        }

        public Criteria andRewardGoldLessThan(Long value) {
            addCriterion("reward_gold <", value, "rewardGold");
            return (Criteria) this;
        }

        public Criteria andRewardGoldLessThanOrEqualTo(Long value) {
            addCriterion("reward_gold <=", value, "rewardGold");
            return (Criteria) this;
        }

        public Criteria andRewardGoldIn(List<Long> values) {
            addCriterion("reward_gold in", values, "rewardGold");
            return (Criteria) this;
        }

        public Criteria andRewardGoldNotIn(List<Long> values) {
            addCriterion("reward_gold not in", values, "rewardGold");
            return (Criteria) this;
        }

        public Criteria andRewardGoldBetween(Long value1, Long value2) {
            addCriterion("reward_gold between", value1, value2, "rewardGold");
            return (Criteria) this;
        }

        public Criteria andRewardGoldNotBetween(Long value1, Long value2) {
            addCriterion("reward_gold not between", value1, value2, "rewardGold");
            return (Criteria) this;
        }

        public Criteria andRewardBonusIsNull() {
            addCriterion("reward_bonus is null");
            return (Criteria) this;
        }

        public Criteria andRewardBonusIsNotNull() {
            addCriterion("reward_bonus is not null");
            return (Criteria) this;
        }

        public Criteria andRewardBonusEqualTo(Long value) {
            addCriterion("reward_bonus =", value, "rewardBonus");
            return (Criteria) this;
        }

        public Criteria andRewardBonusNotEqualTo(Long value) {
            addCriterion("reward_bonus <>", value, "rewardBonus");
            return (Criteria) this;
        }

        public Criteria andRewardBonusGreaterThan(Long value) {
            addCriterion("reward_bonus >", value, "rewardBonus");
            return (Criteria) this;
        }

        public Criteria andRewardBonusGreaterThanOrEqualTo(Long value) {
            addCriterion("reward_bonus >=", value, "rewardBonus");
            return (Criteria) this;
        }

        public Criteria andRewardBonusLessThan(Long value) {
            addCriterion("reward_bonus <", value, "rewardBonus");
            return (Criteria) this;
        }

        public Criteria andRewardBonusLessThanOrEqualTo(Long value) {
            addCriterion("reward_bonus <=", value, "rewardBonus");
            return (Criteria) this;
        }

        public Criteria andRewardBonusIn(List<Long> values) {
            addCriterion("reward_bonus in", values, "rewardBonus");
            return (Criteria) this;
        }

        public Criteria andRewardBonusNotIn(List<Long> values) {
            addCriterion("reward_bonus not in", values, "rewardBonus");
            return (Criteria) this;
        }

        public Criteria andRewardBonusBetween(Long value1, Long value2) {
            addCriterion("reward_bonus between", value1, value2, "rewardBonus");
            return (Criteria) this;
        }

        public Criteria andRewardBonusNotBetween(Long value1, Long value2) {
            addCriterion("reward_bonus not between", value1, value2, "rewardBonus");
            return (Criteria) this;
        }

        public Criteria andIsRewardGivenIsNull() {
            addCriterion("is_reward_given is null");
            return (Criteria) this;
        }

        public Criteria andIsRewardGivenIsNotNull() {
            addCriterion("is_reward_given is not null");
            return (Criteria) this;
        }

        public Criteria andIsRewardGivenEqualTo(Boolean value) {
            addCriterion("is_reward_given =", value, "isRewardGiven");
            return (Criteria) this;
        }

        public Criteria andIsRewardGivenNotEqualTo(Boolean value) {
            addCriterion("is_reward_given <>", value, "isRewardGiven");
            return (Criteria) this;
        }

        public Criteria andIsRewardGivenGreaterThan(Boolean value) {
            addCriterion("is_reward_given >", value, "isRewardGiven");
            return (Criteria) this;
        }

        public Criteria andIsRewardGivenGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_reward_given >=", value, "isRewardGiven");
            return (Criteria) this;
        }

        public Criteria andIsRewardGivenLessThan(Boolean value) {
            addCriterion("is_reward_given <", value, "isRewardGiven");
            return (Criteria) this;
        }

        public Criteria andIsRewardGivenLessThanOrEqualTo(Boolean value) {
            addCriterion("is_reward_given <=", value, "isRewardGiven");
            return (Criteria) this;
        }

        public Criteria andIsRewardGivenIn(List<Boolean> values) {
            addCriterion("is_reward_given in", values, "isRewardGiven");
            return (Criteria) this;
        }

        public Criteria andIsRewardGivenNotIn(List<Boolean> values) {
            addCriterion("is_reward_given not in", values, "isRewardGiven");
            return (Criteria) this;
        }

        public Criteria andIsRewardGivenBetween(Boolean value1, Boolean value2) {
            addCriterion("is_reward_given between", value1, value2, "isRewardGiven");
            return (Criteria) this;
        }

        public Criteria andIsRewardGivenNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_reward_given not between", value1, value2, "isRewardGiven");
            return (Criteria) this;
        }

        public Criteria andRewardGivenAtIsNull() {
            addCriterion("reward_given_at is null");
            return (Criteria) this;
        }

        public Criteria andRewardGivenAtIsNotNull() {
            addCriterion("reward_given_at is not null");
            return (Criteria) this;
        }

        public Criteria andRewardGivenAtEqualTo(LocalDateTime value) {
            addCriterion("reward_given_at =", value, "rewardGivenAt");
            return (Criteria) this;
        }

        public Criteria andRewardGivenAtNotEqualTo(LocalDateTime value) {
            addCriterion("reward_given_at <>", value, "rewardGivenAt");
            return (Criteria) this;
        }

        public Criteria andRewardGivenAtGreaterThan(LocalDateTime value) {
            addCriterion("reward_given_at >", value, "rewardGivenAt");
            return (Criteria) this;
        }

        public Criteria andRewardGivenAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("reward_given_at >=", value, "rewardGivenAt");
            return (Criteria) this;
        }

        public Criteria andRewardGivenAtLessThan(LocalDateTime value) {
            addCriterion("reward_given_at <", value, "rewardGivenAt");
            return (Criteria) this;
        }

        public Criteria andRewardGivenAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("reward_given_at <=", value, "rewardGivenAt");
            return (Criteria) this;
        }

        public Criteria andRewardGivenAtIn(List<LocalDateTime> values) {
            addCriterion("reward_given_at in", values, "rewardGivenAt");
            return (Criteria) this;
        }

        public Criteria andRewardGivenAtNotIn(List<LocalDateTime> values) {
            addCriterion("reward_given_at not in", values, "rewardGivenAt");
            return (Criteria) this;
        }

        public Criteria andRewardGivenAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("reward_given_at between", value1, value2, "rewardGivenAt");
            return (Criteria) this;
        }

        public Criteria andRewardGivenAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("reward_given_at not between", value1, value2, "rewardGivenAt");
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