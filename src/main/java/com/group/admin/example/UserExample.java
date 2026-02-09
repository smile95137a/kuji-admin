package com.group.admin.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public UserExample() {
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

        public Criteria andEmailIsNull() {
            addCriterion("email is null");
            return (Criteria) this;
        }

        public Criteria andEmailIsNotNull() {
            addCriterion("email is not null");
            return (Criteria) this;
        }

        public Criteria andEmailEqualTo(String value) {
            addCriterion("email =", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailNotEqualTo(String value) {
            addCriterion("email <>", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailGreaterThan(String value) {
            addCriterion("email >", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailGreaterThanOrEqualTo(String value) {
            addCriterion("email >=", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailLessThan(String value) {
            addCriterion("email <", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailLessThanOrEqualTo(String value) {
            addCriterion("email <=", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailLike(String value) {
            addCriterion("email like", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailNotLike(String value) {
            addCriterion("email not like", value, "email");
            return (Criteria) this;
        }

        public Criteria andEmailIn(List<String> values) {
            addCriterion("email in", values, "email");
            return (Criteria) this;
        }

        public Criteria andEmailNotIn(List<String> values) {
            addCriterion("email not in", values, "email");
            return (Criteria) this;
        }

        public Criteria andEmailBetween(String value1, String value2) {
            addCriterion("email between", value1, value2, "email");
            return (Criteria) this;
        }

        public Criteria andEmailNotBetween(String value1, String value2) {
            addCriterion("email not between", value1, value2, "email");
            return (Criteria) this;
        }

        public Criteria andNicknameIsNull() {
            addCriterion("nickname is null");
            return (Criteria) this;
        }

        public Criteria andNicknameIsNotNull() {
            addCriterion("nickname is not null");
            return (Criteria) this;
        }

        public Criteria andNicknameEqualTo(String value) {
            addCriterion("nickname =", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameNotEqualTo(String value) {
            addCriterion("nickname <>", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameGreaterThan(String value) {
            addCriterion("nickname >", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameGreaterThanOrEqualTo(String value) {
            addCriterion("nickname >=", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameLessThan(String value) {
            addCriterion("nickname <", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameLessThanOrEqualTo(String value) {
            addCriterion("nickname <=", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameLike(String value) {
            addCriterion("nickname like", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameNotLike(String value) {
            addCriterion("nickname not like", value, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameIn(List<String> values) {
            addCriterion("nickname in", values, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameNotIn(List<String> values) {
            addCriterion("nickname not in", values, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameBetween(String value1, String value2) {
            addCriterion("nickname between", value1, value2, "nickname");
            return (Criteria) this;
        }

        public Criteria andNicknameNotBetween(String value1, String value2) {
            addCriterion("nickname not between", value1, value2, "nickname");
            return (Criteria) this;
        }

        public Criteria andPasswordIsNull() {
            addCriterion("password is null");
            return (Criteria) this;
        }

        public Criteria andPasswordIsNotNull() {
            addCriterion("password is not null");
            return (Criteria) this;
        }

        public Criteria andPasswordEqualTo(String value) {
            addCriterion("password =", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordNotEqualTo(String value) {
            addCriterion("password <>", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordGreaterThan(String value) {
            addCriterion("password >", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordGreaterThanOrEqualTo(String value) {
            addCriterion("password >=", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordLessThan(String value) {
            addCriterion("password <", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordLessThanOrEqualTo(String value) {
            addCriterion("password <=", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordLike(String value) {
            addCriterion("password like", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordNotLike(String value) {
            addCriterion("password not like", value, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordIn(List<String> values) {
            addCriterion("password in", values, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordNotIn(List<String> values) {
            addCriterion("password not in", values, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordBetween(String value1, String value2) {
            addCriterion("password between", value1, value2, "password");
            return (Criteria) this;
        }

        public Criteria andPasswordNotBetween(String value1, String value2) {
            addCriterion("password not between", value1, value2, "password");
            return (Criteria) this;
        }

        public Criteria andAvatarIsNull() {
            addCriterion("avatar is null");
            return (Criteria) this;
        }

        public Criteria andAvatarIsNotNull() {
            addCriterion("avatar is not null");
            return (Criteria) this;
        }

        public Criteria andAvatarEqualTo(String value) {
            addCriterion("avatar =", value, "avatar");
            return (Criteria) this;
        }

        public Criteria andAvatarNotEqualTo(String value) {
            addCriterion("avatar <>", value, "avatar");
            return (Criteria) this;
        }

        public Criteria andAvatarGreaterThan(String value) {
            addCriterion("avatar >", value, "avatar");
            return (Criteria) this;
        }

        public Criteria andAvatarGreaterThanOrEqualTo(String value) {
            addCriterion("avatar >=", value, "avatar");
            return (Criteria) this;
        }

        public Criteria andAvatarLessThan(String value) {
            addCriterion("avatar <", value, "avatar");
            return (Criteria) this;
        }

        public Criteria andAvatarLessThanOrEqualTo(String value) {
            addCriterion("avatar <=", value, "avatar");
            return (Criteria) this;
        }

        public Criteria andAvatarLike(String value) {
            addCriterion("avatar like", value, "avatar");
            return (Criteria) this;
        }

        public Criteria andAvatarNotLike(String value) {
            addCriterion("avatar not like", value, "avatar");
            return (Criteria) this;
        }

        public Criteria andAvatarIn(List<String> values) {
            addCriterion("avatar in", values, "avatar");
            return (Criteria) this;
        }

        public Criteria andAvatarNotIn(List<String> values) {
            addCriterion("avatar not in", values, "avatar");
            return (Criteria) this;
        }

        public Criteria andAvatarBetween(String value1, String value2) {
            addCriterion("avatar between", value1, value2, "avatar");
            return (Criteria) this;
        }

        public Criteria andAvatarNotBetween(String value1, String value2) {
            addCriterion("avatar not between", value1, value2, "avatar");
            return (Criteria) this;
        }

        public Criteria andProviderIsNull() {
            addCriterion("provider is null");
            return (Criteria) this;
        }

        public Criteria andProviderIsNotNull() {
            addCriterion("provider is not null");
            return (Criteria) this;
        }

        public Criteria andProviderEqualTo(String value) {
            addCriterion("provider =", value, "provider");
            return (Criteria) this;
        }

        public Criteria andProviderNotEqualTo(String value) {
            addCriterion("provider <>", value, "provider");
            return (Criteria) this;
        }

        public Criteria andProviderGreaterThan(String value) {
            addCriterion("provider >", value, "provider");
            return (Criteria) this;
        }

        public Criteria andProviderGreaterThanOrEqualTo(String value) {
            addCriterion("provider >=", value, "provider");
            return (Criteria) this;
        }

        public Criteria andProviderLessThan(String value) {
            addCriterion("provider <", value, "provider");
            return (Criteria) this;
        }

        public Criteria andProviderLessThanOrEqualTo(String value) {
            addCriterion("provider <=", value, "provider");
            return (Criteria) this;
        }

        public Criteria andProviderLike(String value) {
            addCriterion("provider like", value, "provider");
            return (Criteria) this;
        }

        public Criteria andProviderNotLike(String value) {
            addCriterion("provider not like", value, "provider");
            return (Criteria) this;
        }

        public Criteria andProviderIn(List<String> values) {
            addCriterion("provider in", values, "provider");
            return (Criteria) this;
        }

        public Criteria andProviderNotIn(List<String> values) {
            addCriterion("provider not in", values, "provider");
            return (Criteria) this;
        }

        public Criteria andProviderBetween(String value1, String value2) {
            addCriterion("provider between", value1, value2, "provider");
            return (Criteria) this;
        }

        public Criteria andProviderNotBetween(String value1, String value2) {
            addCriterion("provider not between", value1, value2, "provider");
            return (Criteria) this;
        }

        public Criteria andProviderIdIsNull() {
            addCriterion("provider_id is null");
            return (Criteria) this;
        }

        public Criteria andProviderIdIsNotNull() {
            addCriterion("provider_id is not null");
            return (Criteria) this;
        }

        public Criteria andProviderIdEqualTo(String value) {
            addCriterion("provider_id =", value, "providerId");
            return (Criteria) this;
        }

        public Criteria andProviderIdNotEqualTo(String value) {
            addCriterion("provider_id <>", value, "providerId");
            return (Criteria) this;
        }

        public Criteria andProviderIdGreaterThan(String value) {
            addCriterion("provider_id >", value, "providerId");
            return (Criteria) this;
        }

        public Criteria andProviderIdGreaterThanOrEqualTo(String value) {
            addCriterion("provider_id >=", value, "providerId");
            return (Criteria) this;
        }

        public Criteria andProviderIdLessThan(String value) {
            addCriterion("provider_id <", value, "providerId");
            return (Criteria) this;
        }

        public Criteria andProviderIdLessThanOrEqualTo(String value) {
            addCriterion("provider_id <=", value, "providerId");
            return (Criteria) this;
        }

        public Criteria andProviderIdLike(String value) {
            addCriterion("provider_id like", value, "providerId");
            return (Criteria) this;
        }

        public Criteria andProviderIdNotLike(String value) {
            addCriterion("provider_id not like", value, "providerId");
            return (Criteria) this;
        }

        public Criteria andProviderIdIn(List<String> values) {
            addCriterion("provider_id in", values, "providerId");
            return (Criteria) this;
        }

        public Criteria andProviderIdNotIn(List<String> values) {
            addCriterion("provider_id not in", values, "providerId");
            return (Criteria) this;
        }

        public Criteria andProviderIdBetween(String value1, String value2) {
            addCriterion("provider_id between", value1, value2, "providerId");
            return (Criteria) this;
        }

        public Criteria andProviderIdNotBetween(String value1, String value2) {
            addCriterion("provider_id not between", value1, value2, "providerId");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsIsNull() {
            addCriterion("gold_coins is null");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsIsNotNull() {
            addCriterion("gold_coins is not null");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsEqualTo(Long value) {
            addCriterion("gold_coins =", value, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsNotEqualTo(Long value) {
            addCriterion("gold_coins <>", value, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsGreaterThan(Long value) {
            addCriterion("gold_coins >", value, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsGreaterThanOrEqualTo(Long value) {
            addCriterion("gold_coins >=", value, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsLessThan(Long value) {
            addCriterion("gold_coins <", value, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsLessThanOrEqualTo(Long value) {
            addCriterion("gold_coins <=", value, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsIn(List<Long> values) {
            addCriterion("gold_coins in", values, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsNotIn(List<Long> values) {
            addCriterion("gold_coins not in", values, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsBetween(Long value1, Long value2) {
            addCriterion("gold_coins between", value1, value2, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andGoldCoinsNotBetween(Long value1, Long value2) {
            addCriterion("gold_coins not between", value1, value2, "goldCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsIsNull() {
            addCriterion("bonus_coins is null");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsIsNotNull() {
            addCriterion("bonus_coins is not null");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsEqualTo(Long value) {
            addCriterion("bonus_coins =", value, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsNotEqualTo(Long value) {
            addCriterion("bonus_coins <>", value, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsGreaterThan(Long value) {
            addCriterion("bonus_coins >", value, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsGreaterThanOrEqualTo(Long value) {
            addCriterion("bonus_coins >=", value, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsLessThan(Long value) {
            addCriterion("bonus_coins <", value, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsLessThanOrEqualTo(Long value) {
            addCriterion("bonus_coins <=", value, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsIn(List<Long> values) {
            addCriterion("bonus_coins in", values, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsNotIn(List<Long> values) {
            addCriterion("bonus_coins not in", values, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsBetween(Long value1, Long value2) {
            addCriterion("bonus_coins between", value1, value2, "bonusCoins");
            return (Criteria) this;
        }

        public Criteria andBonusCoinsNotBetween(Long value1, Long value2) {
            addCriterion("bonus_coins not between", value1, value2, "bonusCoins");
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

        public Criteria andEmailVerifiedIsNull() {
            addCriterion("email_verified is null");
            return (Criteria) this;
        }

        public Criteria andEmailVerifiedIsNotNull() {
            addCriterion("email_verified is not null");
            return (Criteria) this;
        }

        public Criteria andEmailVerifiedEqualTo(Byte value) {
            addCriterion("email_verified =", value, "emailVerified");
            return (Criteria) this;
        }

        public Criteria andEmailVerifiedNotEqualTo(Byte value) {
            addCriterion("email_verified <>", value, "emailVerified");
            return (Criteria) this;
        }

        public Criteria andEmailVerifiedGreaterThan(Byte value) {
            addCriterion("email_verified >", value, "emailVerified");
            return (Criteria) this;
        }

        public Criteria andEmailVerifiedGreaterThanOrEqualTo(Byte value) {
            addCriterion("email_verified >=", value, "emailVerified");
            return (Criteria) this;
        }

        public Criteria andEmailVerifiedLessThan(Byte value) {
            addCriterion("email_verified <", value, "emailVerified");
            return (Criteria) this;
        }

        public Criteria andEmailVerifiedLessThanOrEqualTo(Byte value) {
            addCriterion("email_verified <=", value, "emailVerified");
            return (Criteria) this;
        }

        public Criteria andEmailVerifiedIn(List<Byte> values) {
            addCriterion("email_verified in", values, "emailVerified");
            return (Criteria) this;
        }

        public Criteria andEmailVerifiedNotIn(List<Byte> values) {
            addCriterion("email_verified not in", values, "emailVerified");
            return (Criteria) this;
        }

        public Criteria andEmailVerifiedBetween(Byte value1, Byte value2) {
            addCriterion("email_verified between", value1, value2, "emailVerified");
            return (Criteria) this;
        }

        public Criteria andEmailVerifiedNotBetween(Byte value1, Byte value2) {
            addCriterion("email_verified not between", value1, value2, "emailVerified");
            return (Criteria) this;
        }

        public Criteria andPhoneNumberIsNull() {
            addCriterion("phone_number is null");
            return (Criteria) this;
        }

        public Criteria andPhoneNumberIsNotNull() {
            addCriterion("phone_number is not null");
            return (Criteria) this;
        }

        public Criteria andPhoneNumberEqualTo(String value) {
            addCriterion("phone_number =", value, "phoneNumber");
            return (Criteria) this;
        }

        public Criteria andPhoneNumberNotEqualTo(String value) {
            addCriterion("phone_number <>", value, "phoneNumber");
            return (Criteria) this;
        }

        public Criteria andPhoneNumberGreaterThan(String value) {
            addCriterion("phone_number >", value, "phoneNumber");
            return (Criteria) this;
        }

        public Criteria andPhoneNumberGreaterThanOrEqualTo(String value) {
            addCriterion("phone_number >=", value, "phoneNumber");
            return (Criteria) this;
        }

        public Criteria andPhoneNumberLessThan(String value) {
            addCriterion("phone_number <", value, "phoneNumber");
            return (Criteria) this;
        }

        public Criteria andPhoneNumberLessThanOrEqualTo(String value) {
            addCriterion("phone_number <=", value, "phoneNumber");
            return (Criteria) this;
        }

        public Criteria andPhoneNumberLike(String value) {
            addCriterion("phone_number like", value, "phoneNumber");
            return (Criteria) this;
        }

        public Criteria andPhoneNumberNotLike(String value) {
            addCriterion("phone_number not like", value, "phoneNumber");
            return (Criteria) this;
        }

        public Criteria andPhoneNumberIn(List<String> values) {
            addCriterion("phone_number in", values, "phoneNumber");
            return (Criteria) this;
        }

        public Criteria andPhoneNumberNotIn(List<String> values) {
            addCriterion("phone_number not in", values, "phoneNumber");
            return (Criteria) this;
        }

        public Criteria andPhoneNumberBetween(String value1, String value2) {
            addCriterion("phone_number between", value1, value2, "phoneNumber");
            return (Criteria) this;
        }

        public Criteria andPhoneNumberNotBetween(String value1, String value2) {
            addCriterion("phone_number not between", value1, value2, "phoneNumber");
            return (Criteria) this;
        }

        public Criteria andLineIdIsNull() {
            addCriterion("line_id is null");
            return (Criteria) this;
        }

        public Criteria andLineIdIsNotNull() {
            addCriterion("line_id is not null");
            return (Criteria) this;
        }

        public Criteria andLineIdEqualTo(String value) {
            addCriterion("line_id =", value, "lineId");
            return (Criteria) this;
        }

        public Criteria andLineIdNotEqualTo(String value) {
            addCriterion("line_id <>", value, "lineId");
            return (Criteria) this;
        }

        public Criteria andLineIdGreaterThan(String value) {
            addCriterion("line_id >", value, "lineId");
            return (Criteria) this;
        }

        public Criteria andLineIdGreaterThanOrEqualTo(String value) {
            addCriterion("line_id >=", value, "lineId");
            return (Criteria) this;
        }

        public Criteria andLineIdLessThan(String value) {
            addCriterion("line_id <", value, "lineId");
            return (Criteria) this;
        }

        public Criteria andLineIdLessThanOrEqualTo(String value) {
            addCriterion("line_id <=", value, "lineId");
            return (Criteria) this;
        }

        public Criteria andLineIdLike(String value) {
            addCriterion("line_id like", value, "lineId");
            return (Criteria) this;
        }

        public Criteria andLineIdNotLike(String value) {
            addCriterion("line_id not like", value, "lineId");
            return (Criteria) this;
        }

        public Criteria andLineIdIn(List<String> values) {
            addCriterion("line_id in", values, "lineId");
            return (Criteria) this;
        }

        public Criteria andLineIdNotIn(List<String> values) {
            addCriterion("line_id not in", values, "lineId");
            return (Criteria) this;
        }

        public Criteria andLineIdBetween(String value1, String value2) {
            addCriterion("line_id between", value1, value2, "lineId");
            return (Criteria) this;
        }

        public Criteria andLineIdNotBetween(String value1, String value2) {
            addCriterion("line_id not between", value1, value2, "lineId");
            return (Criteria) this;
        }

        public Criteria andRecipientNameIsNull() {
            addCriterion("recipient_name is null");
            return (Criteria) this;
        }

        public Criteria andRecipientNameIsNotNull() {
            addCriterion("recipient_name is not null");
            return (Criteria) this;
        }

        public Criteria andRecipientNameEqualTo(String value) {
            addCriterion("recipient_name =", value, "recipientName");
            return (Criteria) this;
        }

        public Criteria andRecipientNameNotEqualTo(String value) {
            addCriterion("recipient_name <>", value, "recipientName");
            return (Criteria) this;
        }

        public Criteria andRecipientNameGreaterThan(String value) {
            addCriterion("recipient_name >", value, "recipientName");
            return (Criteria) this;
        }

        public Criteria andRecipientNameGreaterThanOrEqualTo(String value) {
            addCriterion("recipient_name >=", value, "recipientName");
            return (Criteria) this;
        }

        public Criteria andRecipientNameLessThan(String value) {
            addCriterion("recipient_name <", value, "recipientName");
            return (Criteria) this;
        }

        public Criteria andRecipientNameLessThanOrEqualTo(String value) {
            addCriterion("recipient_name <=", value, "recipientName");
            return (Criteria) this;
        }

        public Criteria andRecipientNameLike(String value) {
            addCriterion("recipient_name like", value, "recipientName");
            return (Criteria) this;
        }

        public Criteria andRecipientNameNotLike(String value) {
            addCriterion("recipient_name not like", value, "recipientName");
            return (Criteria) this;
        }

        public Criteria andRecipientNameIn(List<String> values) {
            addCriterion("recipient_name in", values, "recipientName");
            return (Criteria) this;
        }

        public Criteria andRecipientNameNotIn(List<String> values) {
            addCriterion("recipient_name not in", values, "recipientName");
            return (Criteria) this;
        }

        public Criteria andRecipientNameBetween(String value1, String value2) {
            addCriterion("recipient_name between", value1, value2, "recipientName");
            return (Criteria) this;
        }

        public Criteria andRecipientNameNotBetween(String value1, String value2) {
            addCriterion("recipient_name not between", value1, value2, "recipientName");
            return (Criteria) this;
        }

        public Criteria andRecipientPhoneIsNull() {
            addCriterion("recipient_phone is null");
            return (Criteria) this;
        }

        public Criteria andRecipientPhoneIsNotNull() {
            addCriterion("recipient_phone is not null");
            return (Criteria) this;
        }

        public Criteria andRecipientPhoneEqualTo(String value) {
            addCriterion("recipient_phone =", value, "recipientPhone");
            return (Criteria) this;
        }

        public Criteria andRecipientPhoneNotEqualTo(String value) {
            addCriterion("recipient_phone <>", value, "recipientPhone");
            return (Criteria) this;
        }

        public Criteria andRecipientPhoneGreaterThan(String value) {
            addCriterion("recipient_phone >", value, "recipientPhone");
            return (Criteria) this;
        }

        public Criteria andRecipientPhoneGreaterThanOrEqualTo(String value) {
            addCriterion("recipient_phone >=", value, "recipientPhone");
            return (Criteria) this;
        }

        public Criteria andRecipientPhoneLessThan(String value) {
            addCriterion("recipient_phone <", value, "recipientPhone");
            return (Criteria) this;
        }

        public Criteria andRecipientPhoneLessThanOrEqualTo(String value) {
            addCriterion("recipient_phone <=", value, "recipientPhone");
            return (Criteria) this;
        }

        public Criteria andRecipientPhoneLike(String value) {
            addCriterion("recipient_phone like", value, "recipientPhone");
            return (Criteria) this;
        }

        public Criteria andRecipientPhoneNotLike(String value) {
            addCriterion("recipient_phone not like", value, "recipientPhone");
            return (Criteria) this;
        }

        public Criteria andRecipientPhoneIn(List<String> values) {
            addCriterion("recipient_phone in", values, "recipientPhone");
            return (Criteria) this;
        }

        public Criteria andRecipientPhoneNotIn(List<String> values) {
            addCriterion("recipient_phone not in", values, "recipientPhone");
            return (Criteria) this;
        }

        public Criteria andRecipientPhoneBetween(String value1, String value2) {
            addCriterion("recipient_phone between", value1, value2, "recipientPhone");
            return (Criteria) this;
        }

        public Criteria andRecipientPhoneNotBetween(String value1, String value2) {
            addCriterion("recipient_phone not between", value1, value2, "recipientPhone");
            return (Criteria) this;
        }

        public Criteria andCityIsNull() {
            addCriterion("city is null");
            return (Criteria) this;
        }

        public Criteria andCityIsNotNull() {
            addCriterion("city is not null");
            return (Criteria) this;
        }

        public Criteria andCityEqualTo(String value) {
            addCriterion("city =", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityNotEqualTo(String value) {
            addCriterion("city <>", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityGreaterThan(String value) {
            addCriterion("city >", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityGreaterThanOrEqualTo(String value) {
            addCriterion("city >=", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityLessThan(String value) {
            addCriterion("city <", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityLessThanOrEqualTo(String value) {
            addCriterion("city <=", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityLike(String value) {
            addCriterion("city like", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityNotLike(String value) {
            addCriterion("city not like", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityIn(List<String> values) {
            addCriterion("city in", values, "city");
            return (Criteria) this;
        }

        public Criteria andCityNotIn(List<String> values) {
            addCriterion("city not in", values, "city");
            return (Criteria) this;
        }

        public Criteria andCityBetween(String value1, String value2) {
            addCriterion("city between", value1, value2, "city");
            return (Criteria) this;
        }

        public Criteria andCityNotBetween(String value1, String value2) {
            addCriterion("city not between", value1, value2, "city");
            return (Criteria) this;
        }

        public Criteria andDistrictIsNull() {
            addCriterion("district is null");
            return (Criteria) this;
        }

        public Criteria andDistrictIsNotNull() {
            addCriterion("district is not null");
            return (Criteria) this;
        }

        public Criteria andDistrictEqualTo(String value) {
            addCriterion("district =", value, "district");
            return (Criteria) this;
        }

        public Criteria andDistrictNotEqualTo(String value) {
            addCriterion("district <>", value, "district");
            return (Criteria) this;
        }

        public Criteria andDistrictGreaterThan(String value) {
            addCriterion("district >", value, "district");
            return (Criteria) this;
        }

        public Criteria andDistrictGreaterThanOrEqualTo(String value) {
            addCriterion("district >=", value, "district");
            return (Criteria) this;
        }

        public Criteria andDistrictLessThan(String value) {
            addCriterion("district <", value, "district");
            return (Criteria) this;
        }

        public Criteria andDistrictLessThanOrEqualTo(String value) {
            addCriterion("district <=", value, "district");
            return (Criteria) this;
        }

        public Criteria andDistrictLike(String value) {
            addCriterion("district like", value, "district");
            return (Criteria) this;
        }

        public Criteria andDistrictNotLike(String value) {
            addCriterion("district not like", value, "district");
            return (Criteria) this;
        }

        public Criteria andDistrictIn(List<String> values) {
            addCriterion("district in", values, "district");
            return (Criteria) this;
        }

        public Criteria andDistrictNotIn(List<String> values) {
            addCriterion("district not in", values, "district");
            return (Criteria) this;
        }

        public Criteria andDistrictBetween(String value1, String value2) {
            addCriterion("district between", value1, value2, "district");
            return (Criteria) this;
        }

        public Criteria andDistrictNotBetween(String value1, String value2) {
            addCriterion("district not between", value1, value2, "district");
            return (Criteria) this;
        }

        public Criteria andAddressDetailIsNull() {
            addCriterion("address_detail is null");
            return (Criteria) this;
        }

        public Criteria andAddressDetailIsNotNull() {
            addCriterion("address_detail is not null");
            return (Criteria) this;
        }

        public Criteria andAddressDetailEqualTo(String value) {
            addCriterion("address_detail =", value, "addressDetail");
            return (Criteria) this;
        }

        public Criteria andAddressDetailNotEqualTo(String value) {
            addCriterion("address_detail <>", value, "addressDetail");
            return (Criteria) this;
        }

        public Criteria andAddressDetailGreaterThan(String value) {
            addCriterion("address_detail >", value, "addressDetail");
            return (Criteria) this;
        }

        public Criteria andAddressDetailGreaterThanOrEqualTo(String value) {
            addCriterion("address_detail >=", value, "addressDetail");
            return (Criteria) this;
        }

        public Criteria andAddressDetailLessThan(String value) {
            addCriterion("address_detail <", value, "addressDetail");
            return (Criteria) this;
        }

        public Criteria andAddressDetailLessThanOrEqualTo(String value) {
            addCriterion("address_detail <=", value, "addressDetail");
            return (Criteria) this;
        }

        public Criteria andAddressDetailLike(String value) {
            addCriterion("address_detail like", value, "addressDetail");
            return (Criteria) this;
        }

        public Criteria andAddressDetailNotLike(String value) {
            addCriterion("address_detail not like", value, "addressDetail");
            return (Criteria) this;
        }

        public Criteria andAddressDetailIn(List<String> values) {
            addCriterion("address_detail in", values, "addressDetail");
            return (Criteria) this;
        }

        public Criteria andAddressDetailNotIn(List<String> values) {
            addCriterion("address_detail not in", values, "addressDetail");
            return (Criteria) this;
        }

        public Criteria andAddressDetailBetween(String value1, String value2) {
            addCriterion("address_detail between", value1, value2, "addressDetail");
            return (Criteria) this;
        }

        public Criteria andAddressDetailNotBetween(String value1, String value2) {
            addCriterion("address_detail not between", value1, value2, "addressDetail");
            return (Criteria) this;
        }

        public Criteria andInvoiceTypeIsNull() {
            addCriterion("invoice_type is null");
            return (Criteria) this;
        }

        public Criteria andInvoiceTypeIsNotNull() {
            addCriterion("invoice_type is not null");
            return (Criteria) this;
        }

        public Criteria andInvoiceTypeEqualTo(String value) {
            addCriterion("invoice_type =", value, "invoiceType");
            return (Criteria) this;
        }

        public Criteria andInvoiceTypeNotEqualTo(String value) {
            addCriterion("invoice_type <>", value, "invoiceType");
            return (Criteria) this;
        }

        public Criteria andInvoiceTypeGreaterThan(String value) {
            addCriterion("invoice_type >", value, "invoiceType");
            return (Criteria) this;
        }

        public Criteria andInvoiceTypeGreaterThanOrEqualTo(String value) {
            addCriterion("invoice_type >=", value, "invoiceType");
            return (Criteria) this;
        }

        public Criteria andInvoiceTypeLessThan(String value) {
            addCriterion("invoice_type <", value, "invoiceType");
            return (Criteria) this;
        }

        public Criteria andInvoiceTypeLessThanOrEqualTo(String value) {
            addCriterion("invoice_type <=", value, "invoiceType");
            return (Criteria) this;
        }

        public Criteria andInvoiceTypeLike(String value) {
            addCriterion("invoice_type like", value, "invoiceType");
            return (Criteria) this;
        }

        public Criteria andInvoiceTypeNotLike(String value) {
            addCriterion("invoice_type not like", value, "invoiceType");
            return (Criteria) this;
        }

        public Criteria andInvoiceTypeIn(List<String> values) {
            addCriterion("invoice_type in", values, "invoiceType");
            return (Criteria) this;
        }

        public Criteria andInvoiceTypeNotIn(List<String> values) {
            addCriterion("invoice_type not in", values, "invoiceType");
            return (Criteria) this;
        }

        public Criteria andInvoiceTypeBetween(String value1, String value2) {
            addCriterion("invoice_type between", value1, value2, "invoiceType");
            return (Criteria) this;
        }

        public Criteria andInvoiceTypeNotBetween(String value1, String value2) {
            addCriterion("invoice_type not between", value1, value2, "invoiceType");
            return (Criteria) this;
        }

        public Criteria andInvoiceEmailIsNull() {
            addCriterion("invoice_email is null");
            return (Criteria) this;
        }

        public Criteria andInvoiceEmailIsNotNull() {
            addCriterion("invoice_email is not null");
            return (Criteria) this;
        }

        public Criteria andInvoiceEmailEqualTo(String value) {
            addCriterion("invoice_email =", value, "invoiceEmail");
            return (Criteria) this;
        }

        public Criteria andInvoiceEmailNotEqualTo(String value) {
            addCriterion("invoice_email <>", value, "invoiceEmail");
            return (Criteria) this;
        }

        public Criteria andInvoiceEmailGreaterThan(String value) {
            addCriterion("invoice_email >", value, "invoiceEmail");
            return (Criteria) this;
        }

        public Criteria andInvoiceEmailGreaterThanOrEqualTo(String value) {
            addCriterion("invoice_email >=", value, "invoiceEmail");
            return (Criteria) this;
        }

        public Criteria andInvoiceEmailLessThan(String value) {
            addCriterion("invoice_email <", value, "invoiceEmail");
            return (Criteria) this;
        }

        public Criteria andInvoiceEmailLessThanOrEqualTo(String value) {
            addCriterion("invoice_email <=", value, "invoiceEmail");
            return (Criteria) this;
        }

        public Criteria andInvoiceEmailLike(String value) {
            addCriterion("invoice_email like", value, "invoiceEmail");
            return (Criteria) this;
        }

        public Criteria andInvoiceEmailNotLike(String value) {
            addCriterion("invoice_email not like", value, "invoiceEmail");
            return (Criteria) this;
        }

        public Criteria andInvoiceEmailIn(List<String> values) {
            addCriterion("invoice_email in", values, "invoiceEmail");
            return (Criteria) this;
        }

        public Criteria andInvoiceEmailNotIn(List<String> values) {
            addCriterion("invoice_email not in", values, "invoiceEmail");
            return (Criteria) this;
        }

        public Criteria andInvoiceEmailBetween(String value1, String value2) {
            addCriterion("invoice_email between", value1, value2, "invoiceEmail");
            return (Criteria) this;
        }

        public Criteria andInvoiceEmailNotBetween(String value1, String value2) {
            addCriterion("invoice_email not between", value1, value2, "invoiceEmail");
            return (Criteria) this;
        }

        public Criteria andCarrierCodeIsNull() {
            addCriterion("carrier_code is null");
            return (Criteria) this;
        }

        public Criteria andCarrierCodeIsNotNull() {
            addCriterion("carrier_code is not null");
            return (Criteria) this;
        }

        public Criteria andCarrierCodeEqualTo(String value) {
            addCriterion("carrier_code =", value, "carrierCode");
            return (Criteria) this;
        }

        public Criteria andCarrierCodeNotEqualTo(String value) {
            addCriterion("carrier_code <>", value, "carrierCode");
            return (Criteria) this;
        }

        public Criteria andCarrierCodeGreaterThan(String value) {
            addCriterion("carrier_code >", value, "carrierCode");
            return (Criteria) this;
        }

        public Criteria andCarrierCodeGreaterThanOrEqualTo(String value) {
            addCriterion("carrier_code >=", value, "carrierCode");
            return (Criteria) this;
        }

        public Criteria andCarrierCodeLessThan(String value) {
            addCriterion("carrier_code <", value, "carrierCode");
            return (Criteria) this;
        }

        public Criteria andCarrierCodeLessThanOrEqualTo(String value) {
            addCriterion("carrier_code <=", value, "carrierCode");
            return (Criteria) this;
        }

        public Criteria andCarrierCodeLike(String value) {
            addCriterion("carrier_code like", value, "carrierCode");
            return (Criteria) this;
        }

        public Criteria andCarrierCodeNotLike(String value) {
            addCriterion("carrier_code not like", value, "carrierCode");
            return (Criteria) this;
        }

        public Criteria andCarrierCodeIn(List<String> values) {
            addCriterion("carrier_code in", values, "carrierCode");
            return (Criteria) this;
        }

        public Criteria andCarrierCodeNotIn(List<String> values) {
            addCriterion("carrier_code not in", values, "carrierCode");
            return (Criteria) this;
        }

        public Criteria andCarrierCodeBetween(String value1, String value2) {
            addCriterion("carrier_code between", value1, value2, "carrierCode");
            return (Criteria) this;
        }

        public Criteria andCarrierCodeNotBetween(String value1, String value2) {
            addCriterion("carrier_code not between", value1, value2, "carrierCode");
            return (Criteria) this;
        }

        public Criteria andTaxIdIsNull() {
            addCriterion("tax_id is null");
            return (Criteria) this;
        }

        public Criteria andTaxIdIsNotNull() {
            addCriterion("tax_id is not null");
            return (Criteria) this;
        }

        public Criteria andTaxIdEqualTo(String value) {
            addCriterion("tax_id =", value, "taxId");
            return (Criteria) this;
        }

        public Criteria andTaxIdNotEqualTo(String value) {
            addCriterion("tax_id <>", value, "taxId");
            return (Criteria) this;
        }

        public Criteria andTaxIdGreaterThan(String value) {
            addCriterion("tax_id >", value, "taxId");
            return (Criteria) this;
        }

        public Criteria andTaxIdGreaterThanOrEqualTo(String value) {
            addCriterion("tax_id >=", value, "taxId");
            return (Criteria) this;
        }

        public Criteria andTaxIdLessThan(String value) {
            addCriterion("tax_id <", value, "taxId");
            return (Criteria) this;
        }

        public Criteria andTaxIdLessThanOrEqualTo(String value) {
            addCriterion("tax_id <=", value, "taxId");
            return (Criteria) this;
        }

        public Criteria andTaxIdLike(String value) {
            addCriterion("tax_id like", value, "taxId");
            return (Criteria) this;
        }

        public Criteria andTaxIdNotLike(String value) {
            addCriterion("tax_id not like", value, "taxId");
            return (Criteria) this;
        }

        public Criteria andTaxIdIn(List<String> values) {
            addCriterion("tax_id in", values, "taxId");
            return (Criteria) this;
        }

        public Criteria andTaxIdNotIn(List<String> values) {
            addCriterion("tax_id not in", values, "taxId");
            return (Criteria) this;
        }

        public Criteria andTaxIdBetween(String value1, String value2) {
            addCriterion("tax_id between", value1, value2, "taxId");
            return (Criteria) this;
        }

        public Criteria andTaxIdNotBetween(String value1, String value2) {
            addCriterion("tax_id not between", value1, value2, "taxId");
            return (Criteria) this;
        }

        public Criteria andCompanyNameIsNull() {
            addCriterion("company_name is null");
            return (Criteria) this;
        }

        public Criteria andCompanyNameIsNotNull() {
            addCriterion("company_name is not null");
            return (Criteria) this;
        }

        public Criteria andCompanyNameEqualTo(String value) {
            addCriterion("company_name =", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameNotEqualTo(String value) {
            addCriterion("company_name <>", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameGreaterThan(String value) {
            addCriterion("company_name >", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameGreaterThanOrEqualTo(String value) {
            addCriterion("company_name >=", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameLessThan(String value) {
            addCriterion("company_name <", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameLessThanOrEqualTo(String value) {
            addCriterion("company_name <=", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameLike(String value) {
            addCriterion("company_name like", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameNotLike(String value) {
            addCriterion("company_name not like", value, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameIn(List<String> values) {
            addCriterion("company_name in", values, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameNotIn(List<String> values) {
            addCriterion("company_name not in", values, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameBetween(String value1, String value2) {
            addCriterion("company_name between", value1, value2, "companyName");
            return (Criteria) this;
        }

        public Criteria andCompanyNameNotBetween(String value1, String value2) {
            addCriterion("company_name not between", value1, value2, "companyName");
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

        public Criteria andReferredStoreIdIsNull() {
            addCriterion("referred_store_id is null");
            return (Criteria) this;
        }

        public Criteria andReferredStoreIdIsNotNull() {
            addCriterion("referred_store_id is not null");
            return (Criteria) this;
        }

        public Criteria andReferredStoreIdEqualTo(String value) {
            addCriterion("referred_store_id =", value, "referredStoreId");
            return (Criteria) this;
        }

        public Criteria andReferredStoreIdNotEqualTo(String value) {
            addCriterion("referred_store_id <>", value, "referredStoreId");
            return (Criteria) this;
        }

        public Criteria andReferredStoreIdGreaterThan(String value) {
            addCriterion("referred_store_id >", value, "referredStoreId");
            return (Criteria) this;
        }

        public Criteria andReferredStoreIdGreaterThanOrEqualTo(String value) {
            addCriterion("referred_store_id >=", value, "referredStoreId");
            return (Criteria) this;
        }

        public Criteria andReferredStoreIdLessThan(String value) {
            addCriterion("referred_store_id <", value, "referredStoreId");
            return (Criteria) this;
        }

        public Criteria andReferredStoreIdLessThanOrEqualTo(String value) {
            addCriterion("referred_store_id <=", value, "referredStoreId");
            return (Criteria) this;
        }

        public Criteria andReferredStoreIdLike(String value) {
            addCriterion("referred_store_id like", value, "referredStoreId");
            return (Criteria) this;
        }

        public Criteria andReferredStoreIdNotLike(String value) {
            addCriterion("referred_store_id not like", value, "referredStoreId");
            return (Criteria) this;
        }

        public Criteria andReferredStoreIdIn(List<String> values) {
            addCriterion("referred_store_id in", values, "referredStoreId");
            return (Criteria) this;
        }

        public Criteria andReferredStoreIdNotIn(List<String> values) {
            addCriterion("referred_store_id not in", values, "referredStoreId");
            return (Criteria) this;
        }

        public Criteria andReferredStoreIdBetween(String value1, String value2) {
            addCriterion("referred_store_id between", value1, value2, "referredStoreId");
            return (Criteria) this;
        }

        public Criteria andReferredStoreIdNotBetween(String value1, String value2) {
            addCriterion("referred_store_id not between", value1, value2, "referredStoreId");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationTokenIsNull() {
            addCriterion("email_verification_token is null");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationTokenIsNotNull() {
            addCriterion("email_verification_token is not null");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationTokenEqualTo(String value) {
            addCriterion("email_verification_token =", value, "emailVerificationToken");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationTokenNotEqualTo(String value) {
            addCriterion("email_verification_token <>", value, "emailVerificationToken");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationTokenGreaterThan(String value) {
            addCriterion("email_verification_token >", value, "emailVerificationToken");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationTokenGreaterThanOrEqualTo(String value) {
            addCriterion("email_verification_token >=", value, "emailVerificationToken");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationTokenLessThan(String value) {
            addCriterion("email_verification_token <", value, "emailVerificationToken");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationTokenLessThanOrEqualTo(String value) {
            addCriterion("email_verification_token <=", value, "emailVerificationToken");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationTokenLike(String value) {
            addCriterion("email_verification_token like", value, "emailVerificationToken");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationTokenNotLike(String value) {
            addCriterion("email_verification_token not like", value, "emailVerificationToken");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationTokenIn(List<String> values) {
            addCriterion("email_verification_token in", values, "emailVerificationToken");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationTokenNotIn(List<String> values) {
            addCriterion("email_verification_token not in", values, "emailVerificationToken");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationTokenBetween(String value1, String value2) {
            addCriterion("email_verification_token between", value1, value2, "emailVerificationToken");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationTokenNotBetween(String value1, String value2) {
            addCriterion("email_verification_token not between", value1, value2, "emailVerificationToken");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationExpiresIsNull() {
            addCriterion("email_verification_expires is null");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationExpiresIsNotNull() {
            addCriterion("email_verification_expires is not null");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationExpiresEqualTo(LocalDateTime value) {
            addCriterion("email_verification_expires =", value, "emailVerificationExpires");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationExpiresNotEqualTo(LocalDateTime value) {
            addCriterion("email_verification_expires <>", value, "emailVerificationExpires");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationExpiresGreaterThan(LocalDateTime value) {
            addCriterion("email_verification_expires >", value, "emailVerificationExpires");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationExpiresGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("email_verification_expires >=", value, "emailVerificationExpires");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationExpiresLessThan(LocalDateTime value) {
            addCriterion("email_verification_expires <", value, "emailVerificationExpires");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationExpiresLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("email_verification_expires <=", value, "emailVerificationExpires");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationExpiresIn(List<LocalDateTime> values) {
            addCriterion("email_verification_expires in", values, "emailVerificationExpires");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationExpiresNotIn(List<LocalDateTime> values) {
            addCriterion("email_verification_expires not in", values, "emailVerificationExpires");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationExpiresBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("email_verification_expires between", value1, value2, "emailVerificationExpires");
            return (Criteria) this;
        }

        public Criteria andEmailVerificationExpiresNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("email_verification_expires not between", value1, value2, "emailVerificationExpires");
            return (Criteria) this;
        }

        public Criteria andPasswordResetTokenIsNull() {
            addCriterion("password_reset_token is null");
            return (Criteria) this;
        }

        public Criteria andPasswordResetTokenIsNotNull() {
            addCriterion("password_reset_token is not null");
            return (Criteria) this;
        }

        public Criteria andPasswordResetTokenEqualTo(String value) {
            addCriterion("password_reset_token =", value, "passwordResetToken");
            return (Criteria) this;
        }

        public Criteria andPasswordResetTokenNotEqualTo(String value) {
            addCriterion("password_reset_token <>", value, "passwordResetToken");
            return (Criteria) this;
        }

        public Criteria andPasswordResetTokenGreaterThan(String value) {
            addCriterion("password_reset_token >", value, "passwordResetToken");
            return (Criteria) this;
        }

        public Criteria andPasswordResetTokenGreaterThanOrEqualTo(String value) {
            addCriterion("password_reset_token >=", value, "passwordResetToken");
            return (Criteria) this;
        }

        public Criteria andPasswordResetTokenLessThan(String value) {
            addCriterion("password_reset_token <", value, "passwordResetToken");
            return (Criteria) this;
        }

        public Criteria andPasswordResetTokenLessThanOrEqualTo(String value) {
            addCriterion("password_reset_token <=", value, "passwordResetToken");
            return (Criteria) this;
        }

        public Criteria andPasswordResetTokenLike(String value) {
            addCriterion("password_reset_token like", value, "passwordResetToken");
            return (Criteria) this;
        }

        public Criteria andPasswordResetTokenNotLike(String value) {
            addCriterion("password_reset_token not like", value, "passwordResetToken");
            return (Criteria) this;
        }

        public Criteria andPasswordResetTokenIn(List<String> values) {
            addCriterion("password_reset_token in", values, "passwordResetToken");
            return (Criteria) this;
        }

        public Criteria andPasswordResetTokenNotIn(List<String> values) {
            addCriterion("password_reset_token not in", values, "passwordResetToken");
            return (Criteria) this;
        }

        public Criteria andPasswordResetTokenBetween(String value1, String value2) {
            addCriterion("password_reset_token between", value1, value2, "passwordResetToken");
            return (Criteria) this;
        }

        public Criteria andPasswordResetTokenNotBetween(String value1, String value2) {
            addCriterion("password_reset_token not between", value1, value2, "passwordResetToken");
            return (Criteria) this;
        }

        public Criteria andPasswordResetExpiresIsNull() {
            addCriterion("password_reset_expires is null");
            return (Criteria) this;
        }

        public Criteria andPasswordResetExpiresIsNotNull() {
            addCriterion("password_reset_expires is not null");
            return (Criteria) this;
        }

        public Criteria andPasswordResetExpiresEqualTo(LocalDateTime value) {
            addCriterion("password_reset_expires =", value, "passwordResetExpires");
            return (Criteria) this;
        }

        public Criteria andPasswordResetExpiresNotEqualTo(LocalDateTime value) {
            addCriterion("password_reset_expires <>", value, "passwordResetExpires");
            return (Criteria) this;
        }

        public Criteria andPasswordResetExpiresGreaterThan(LocalDateTime value) {
            addCriterion("password_reset_expires >", value, "passwordResetExpires");
            return (Criteria) this;
        }

        public Criteria andPasswordResetExpiresGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("password_reset_expires >=", value, "passwordResetExpires");
            return (Criteria) this;
        }

        public Criteria andPasswordResetExpiresLessThan(LocalDateTime value) {
            addCriterion("password_reset_expires <", value, "passwordResetExpires");
            return (Criteria) this;
        }

        public Criteria andPasswordResetExpiresLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("password_reset_expires <=", value, "passwordResetExpires");
            return (Criteria) this;
        }

        public Criteria andPasswordResetExpiresIn(List<LocalDateTime> values) {
            addCriterion("password_reset_expires in", values, "passwordResetExpires");
            return (Criteria) this;
        }

        public Criteria andPasswordResetExpiresNotIn(List<LocalDateTime> values) {
            addCriterion("password_reset_expires not in", values, "passwordResetExpires");
            return (Criteria) this;
        }

        public Criteria andPasswordResetExpiresBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("password_reset_expires between", value1, value2, "passwordResetExpires");
            return (Criteria) this;
        }

        public Criteria andPasswordResetExpiresNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("password_reset_expires not between", value1, value2, "passwordResetExpires");
            return (Criteria) this;
        }

        public Criteria andLastLoginAtIsNull() {
            addCriterion("last_login_at is null");
            return (Criteria) this;
        }

        public Criteria andLastLoginAtIsNotNull() {
            addCriterion("last_login_at is not null");
            return (Criteria) this;
        }

        public Criteria andLastLoginAtEqualTo(LocalDateTime value) {
            addCriterion("last_login_at =", value, "lastLoginAt");
            return (Criteria) this;
        }

        public Criteria andLastLoginAtNotEqualTo(LocalDateTime value) {
            addCriterion("last_login_at <>", value, "lastLoginAt");
            return (Criteria) this;
        }

        public Criteria andLastLoginAtGreaterThan(LocalDateTime value) {
            addCriterion("last_login_at >", value, "lastLoginAt");
            return (Criteria) this;
        }

        public Criteria andLastLoginAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("last_login_at >=", value, "lastLoginAt");
            return (Criteria) this;
        }

        public Criteria andLastLoginAtLessThan(LocalDateTime value) {
            addCriterion("last_login_at <", value, "lastLoginAt");
            return (Criteria) this;
        }

        public Criteria andLastLoginAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("last_login_at <=", value, "lastLoginAt");
            return (Criteria) this;
        }

        public Criteria andLastLoginAtIn(List<LocalDateTime> values) {
            addCriterion("last_login_at in", values, "lastLoginAt");
            return (Criteria) this;
        }

        public Criteria andLastLoginAtNotIn(List<LocalDateTime> values) {
            addCriterion("last_login_at not in", values, "lastLoginAt");
            return (Criteria) this;
        }

        public Criteria andLastLoginAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("last_login_at between", value1, value2, "lastLoginAt");
            return (Criteria) this;
        }

        public Criteria andLastLoginAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("last_login_at not between", value1, value2, "lastLoginAt");
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

        public Criteria andTotalRechargedIsNull() {
            addCriterion("total_recharged is null");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedIsNotNull() {
            addCriterion("total_recharged is not null");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedEqualTo(Long value) {
            addCriterion("total_recharged =", value, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedNotEqualTo(Long value) {
            addCriterion("total_recharged <>", value, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedGreaterThan(Long value) {
            addCriterion("total_recharged >", value, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedGreaterThanOrEqualTo(Long value) {
            addCriterion("total_recharged >=", value, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedLessThan(Long value) {
            addCriterion("total_recharged <", value, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedLessThanOrEqualTo(Long value) {
            addCriterion("total_recharged <=", value, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedIn(List<Long> values) {
            addCriterion("total_recharged in", values, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedNotIn(List<Long> values) {
            addCriterion("total_recharged not in", values, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedBetween(Long value1, Long value2) {
            addCriterion("total_recharged between", value1, value2, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andTotalRechargedNotBetween(Long value1, Long value2) {
            addCriterion("total_recharged not between", value1, value2, "totalRecharged");
            return (Criteria) this;
        }

        public Criteria andVersionIsNull() {
            addCriterion("version is null");
            return (Criteria) this;
        }

        public Criteria andVersionIsNotNull() {
            addCriterion("version is not null");
            return (Criteria) this;
        }

        public Criteria andVersionEqualTo(Integer value) {
            addCriterion("version =", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualTo(Integer value) {
            addCriterion("version <>", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThan(Integer value) {
            addCriterion("version >", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualTo(Integer value) {
            addCriterion("version >=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThan(Integer value) {
            addCriterion("version <", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualTo(Integer value) {
            addCriterion("version <=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionIn(List<Integer> values) {
            addCriterion("version in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotIn(List<Integer> values) {
            addCriterion("version not in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionBetween(Integer value1, Integer value2) {
            addCriterion("version between", value1, value2, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotBetween(Integer value1, Integer value2) {
            addCriterion("version not between", value1, value2, "version");
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