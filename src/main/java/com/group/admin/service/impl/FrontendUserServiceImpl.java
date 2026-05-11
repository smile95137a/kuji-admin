package com.group.admin.service.impl;

import com.group.admin.entity.User;
import com.group.admin.enums.CoinTypeEnum;
import com.group.admin.enums.UserStatusEnum;
import com.group.admin.example.UserExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.user.CoinAdjustReq;
import com.group.admin.req.user.FrontendUserCondition;
import com.group.admin.req.user.FrontendUserUpdateReq;
import com.group.admin.res.user.FrontendUserDetailRes;
import com.group.admin.res.user.FrontendUserListRes;
import com.group.admin.service.CoinService;
import com.group.admin.service.FrontendUserService;
import com.group.admin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 前台會員管理服務實作
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FrontendUserServiceImpl implements FrontendUserService {

    private static final Map<String, String> USER_SORT_FIELD_MAP;

    static {
        Map<String, String> sortMap = new HashMap<>();
        sortMap.put("id", "id");
        sortMap.put("email", "email");
        sortMap.put("nickname", "nickname");
        sortMap.put("phone", "phone_number");
        sortMap.put("phoneNumber", "phone_number");
        sortMap.put("provider", "provider");
        sortMap.put("goldCoins", "gold_coins");
        sortMap.put("bonusCoins", "bonus_coins");
        sortMap.put("status", "status");
        sortMap.put("createdAt", "created_at");
        sortMap.put("updatedAt", "updated_at");
        sortMap.put("lastLoginAt", "last_login_at");
        USER_SORT_FIELD_MAP = Collections.unmodifiableMap(sortMap);
    }
    
    private final UserMapper userMapper;
    private final CoinService coinService;
    
    @Override
    public List<FrontendUserListRes> queryUsers(QueryReq<FrontendUserCondition> req) {
        log.info("🔍 查詢前台會員列表: {}", req);
        
        FrontendUserCondition condition = req != null ? req.getCondition() : null;
        
        UserExample example = new UserExample();

        if (condition != null && isNotBlank(condition.getKeyword())) {
            String keywordLike = "%" + condition.getKeyword().trim() + "%";

            UserExample.Criteria emailKeywordCriteria = example.createCriteria();
            applyCommonFilters(emailKeywordCriteria, condition);
            emailKeywordCriteria.andEmailLike(keywordLike);

            UserExample.Criteria nicknameKeywordCriteria = example.or();
            applyCommonFilters(nicknameKeywordCriteria, condition);
            nicknameKeywordCriteria.andNicknameLike(keywordLike);

            UserExample.Criteria phoneKeywordCriteria = example.or();
            applyCommonFilters(phoneKeywordCriteria, condition);
            phoneKeywordCriteria.andPhoneNumberLike(keywordLike);
        } else {
            UserExample.Criteria criteria = example.createCriteria();
            applyCommonFilters(criteria, condition);
        }
        
        // 排序
        example.setOrderByClause(resolveOrderByClause(req));
        
        List<User> users = userMapper.selectByExample(example);

        if (req != null && req.getPage() != null && req.getSize() != null
                && req.getPage() > 0 && req.getSize() > 0) {
            int page = req.getPage();
            int size = req.getSize();
            int fromIndex = Math.max(0, (page - 1) * size);

            if (fromIndex >= users.size()) {
                users = Collections.emptyList();
            } else {
                int toIndex = Math.min(users.size(), fromIndex + size);
                users = new ArrayList<>(users.subList(fromIndex, toIndex));
            }
        }
        
        log.info("✅ 查詢成功: 共 {} 筆", users.size());

        boolean shouldMaskSensitiveFields = !SecurityUtils.isAdmin();
        
        return users.stream()
                .map(this::toListRes)
            .map(res -> applyListRoleMask(res, shouldMaskSensitiveFields))
            .toList();
    }
    
    @Override
    public FrontendUserDetailRes getUserById(String id) {
        log.info("🔍 查詢會員詳情: userId={}", id);
        
        User user = userMapper.selectByPrimaryKey(id);
        if (user == null || UserStatusEnum.DELETED.getCode().equals(user.getStatus())) {
            throw new BusinessException("會員不存在");
        }
        
        boolean shouldMaskSensitiveFields = !SecurityUtils.isAdmin();
        return applyDetailRoleMask(toDetailRes(user), shouldMaskSensitiveFields);
    }
    
    @Override
    public FrontendUserDetailRes updateUser(String id, FrontendUserUpdateReq req) {
        log.info("✏️ 更新會員資訊: userId={}, req={}", id, req);
        
        User user = userMapper.selectByPrimaryKey(id);
        if (user == null || UserStatusEnum.DELETED.getCode().equals(user.getStatus())) {
            throw new BusinessException("會員不存在");
        }
        
        // 更新欄位（只更新非 null 的欄位）
        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }
        if (req.getNickname() != null) {
            user.setNickname(req.getNickname());
        }
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
        }
        if (req.getPhoneNumber() != null) {
            user.setPhoneNumber(req.getPhoneNumber());
        }
        if (req.getLineId() != null) {
            user.setLineId(req.getLineId());
        }
        if (req.getRecipientName() != null) {
            user.setRecipientName(req.getRecipientName());
        }
        if (req.getRecipientPhone() != null) {
            user.setRecipientPhone(req.getRecipientPhone());
        }
        if (req.getCity() != null) {
            user.setCity(req.getCity());
        }
        if (req.getDistrict() != null) {
            user.setDistrict(req.getDistrict());
        }
        if (req.getAddressDetail() != null) {
            user.setAddressDetail(req.getAddressDetail());
        }
        if (req.getInvoiceType() != null) {
            user.setInvoiceType(req.getInvoiceType());
        }
        if (req.getInvoiceEmail() != null) {
            user.setInvoiceEmail(req.getInvoiceEmail());
        }
        if (req.getCarrierCode() != null) {
            user.setCarrierCode(req.getCarrierCode());
        }
        if (req.getTaxId() != null) {
            user.setTaxId(req.getTaxId());
        }
        if (req.getCompanyName() != null) {
            user.setCompanyName(req.getCompanyName());
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateByPrimaryKey(user);
        
        log.info("✅ 更新成功");
        return toDetailRes(user);
    }
    
    @Override
    public void deleteUser(String id) {
        log.info("🗑️ 軟刪除會員: userId={}", id);
        
        User user = userMapper.selectByPrimaryKey(id);
        if (user == null) {
            throw new BusinessException("會員不存在");
        }
        
        // ✅ 軟刪除：標記為 DELETED
        user.setStatus(UserStatusEnum.DELETED.getCode());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateByPrimaryKey(user);
        
        log.info("✅ 軟刪除成功");
    }
    
    @Override
    public void activateUser(String id) {
        updateUserStatus(id, UserStatusEnum.ACTIVE.getCode(), "啟用");
    }
    
    @Override
    public void deactivateUser(String id) {
        updateUserStatus(id, UserStatusEnum.INACTIVE.getCode(), "停用");
    }
    
    @Override
    public void suspendUser(String id) {
        updateUserStatus(id, UserStatusEnum.SUSPENDED.getCode(), "暫停");
    }
    
    /**
     * 更新會員狀態
     */
    private void updateUserStatus(String id, String status, String action) {
        log.info("🔄 {}會員: userId={}, status={}", action, id, status);
        
        User user = userMapper.selectByPrimaryKey(id);
        if (user == null || UserStatusEnum.DELETED.getCode().equals(user.getStatus())) {
            throw new BusinessException("會員不存在");
        }
        
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateByPrimaryKey(user);
        
        log.info("✅ {}成功", action);
    }
    
    /**
     * 轉換為 Res
     */
    private FrontendUserListRes toListRes(User user) {
        return FrontendUserListRes.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .provider(user.getProvider())
                .goldCoins(user.getGoldCoins() != null ? user.getGoldCoins() : 0L)
                .bonusCoins(user.getBonusCoins() != null ? user.getBonusCoins() : 0L)
                .status(user.getStatus())
                .statusName(UserStatusEnum.getNameByCode(user.getStatus()))
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private FrontendUserDetailRes toDetailRes(User user) {
        return FrontendUserDetailRes.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .goldCoins(user.getGoldCoins() != null ? user.getGoldCoins() : 0L)
                .bonusCoins(user.getBonusCoins() != null ? user.getBonusCoins() : 0L)
                .status(user.getStatus())
                .statusName(UserStatusEnum.getNameByCode(user.getStatus()))
                .emailVerified(user.getEmailVerified() != null && user.getEmailVerified() == 1)
                .phoneNumber(user.getPhoneNumber())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * 檢查字串是否非空白
     * 空字串 "" 會被視為 null 處理
     */
    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private FrontendUserListRes applyListRoleMask(FrontendUserListRes source, boolean shouldMaskSensitiveFields) {
        if (!shouldMaskSensitiveFields || source == null) {
            return source;
        }

        return FrontendUserListRes.builder()
                .id(source.getId())
                .nickname(source.getNickname())
                .avatar(source.getAvatar())
                .provider(source.getProvider())
                .status(source.getStatus())
                .statusName(source.getStatusName())
                .createdAt(source.getCreatedAt())
                .updatedAt(source.getUpdatedAt())
                .build();
    }

    private FrontendUserDetailRes applyDetailRoleMask(FrontendUserDetailRes source, boolean shouldMaskSensitiveFields) {
        if (!shouldMaskSensitiveFields || source == null) {
            return source;
        }

        return FrontendUserDetailRes.builder()
                .id(source.getId())
                .nickname(source.getNickname())
                .avatar(source.getAvatar())
                .provider(source.getProvider())
                .status(source.getStatus())
                .statusName(source.getStatusName())
                .createdAt(source.getCreatedAt())
                .updatedAt(source.getUpdatedAt())
                .build();
    }

    private void applyCommonFilters(UserExample.Criteria criteria, FrontendUserCondition condition) {
        criteria.andStatusNotEqualTo(UserStatusEnum.DELETED.getCode());

        if (condition == null) {
            return;
        }

        if (isNotBlank(condition.getEmail())) {
            criteria.andEmailLike("%" + condition.getEmail().trim() + "%");
        }
        if (isNotBlank(condition.getNickname())) {
            criteria.andNicknameLike("%" + condition.getNickname().trim() + "%");
        }
        if (isNotBlank(condition.getPhone())) {
            criteria.andPhoneNumberLike("%" + condition.getPhone().trim() + "%");
        }
        if (isNotBlank(condition.getStatus())) {
            criteria.andStatusEqualTo(condition.getStatus().trim());
        }
        if (isNotBlank(condition.getProvider())) {
            criteria.andProviderEqualTo(condition.getProvider().trim());
        }
        if (condition.getGoldCoinsMin() != null) {
            criteria.andGoldCoinsGreaterThanOrEqualTo(condition.getGoldCoinsMin());
        }
        if (condition.getGoldCoinsMax() != null) {
            criteria.andGoldCoinsLessThanOrEqualTo(condition.getGoldCoinsMax());
        }
        if (condition.getCreatedAtStart() != null) {
            criteria.andCreatedAtGreaterThanOrEqualTo(condition.getCreatedAtStart().atStartOfDay());
        }
        if (condition.getCreatedAtEnd() != null) {
            criteria.andCreatedAtLessThanOrEqualTo(condition.getCreatedAtEnd().atTime(23, 59, 59));
        }
    }

    private String resolveOrderByClause(QueryReq<FrontendUserCondition> req) {
        if (req == null || !isNotBlank(req.getSortBy())) {
            return "created_at DESC";
        }

        String normalizedSortBy = req.getSortBy().trim();
        String safeSortColumn = USER_SORT_FIELD_MAP.get(normalizedSortBy);

        if (!isNotBlank(safeSortColumn)) {
            return "created_at DESC";
        }

        String normalizedOrder = isNotBlank(req.getSortOrder())
                ? req.getSortOrder().trim().toUpperCase(Locale.ROOT)
                : "ASC";
        if (!"ASC".equals(normalizedOrder) && !"DESC".equals(normalizedOrder)) {
            normalizedOrder = "ASC";
        }

        return safeSortColumn + " " + normalizedOrder;
    }

    @Override
    public void unlockUser(String userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) throw new BusinessException("用戶不存在");
        User update = new User();
        update.setId(userId);
        update.setFailedLoginAttempts(0);
        userMapper.updateByPrimaryKeySelective(update);
        // Use full update to clear lockedUntil to null
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userMapper.updateByPrimaryKey(user);
        log.info("✅ 前台用戶帳號已解鎖: userId={}", userId);
    }

    @Override
    public void adjustUserCoin(String userId, CoinAdjustReq req) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new BusinessException("用戶不存在");
        }

        if (!isNotBlank(req.getRemark())) {
            throw new BusinessException("調整備註不可為空");
        }

        if (req.getAmount() == null || req.getAmount() == 0L) {
            throw new BusinessException("調整金額不可為 0");
        }

        String normalizedCoinType = req.getCoinType() != null ? req.getCoinType().trim().toUpperCase() : null;
        if (!CoinTypeEnum.GOLD.getCode().equals(normalizedCoinType)
                && !CoinTypeEnum.BONUS.getCode().equals(normalizedCoinType)) {
            throw new BusinessException("coinType 不合法，只支援 GOLD / BONUS");
        }

        String operatorId = SecurityUtils.getCurrentAdminUserId();
        if (!isNotBlank(operatorId)) {
            throw new BusinessException("無法取得操作者資訊");
        }

        com.group.admin.req.wallet.CoinAdjustReq adjustReq = new com.group.admin.req.wallet.CoinAdjustReq();
        adjustReq.setUserId(userId);
        adjustReq.setCoinType(normalizedCoinType);
        adjustReq.setAmount(req.getAmount());
        adjustReq.setReason(req.getRemark());

        coinService.adjustCoins(adjustReq, operatorId);

        log.info("✅ 點數調整成功（已記錄交易）: userId={}, coinType={}, amount={}, operatorId={}",
                userId, normalizedCoinType, req.getAmount(), operatorId);
    }
}
