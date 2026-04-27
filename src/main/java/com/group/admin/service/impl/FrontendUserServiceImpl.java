package com.group.admin.service.impl;

import com.group.admin.entity.User;
import com.group.admin.enums.UserStatusEnum;
import com.group.admin.example.UserExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.user.CoinAdjustReq;
import com.group.admin.req.user.FrontendUserCondition;
import com.group.admin.req.user.FrontendUserUpdateReq;
import com.group.admin.res.user.FrontendUserRes;
import com.group.admin.service.FrontendUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    
    private final UserMapper userMapper;
    
    @Override
    public List<FrontendUserRes> queryUsers(QueryReq<FrontendUserCondition> req) {
        log.info("🔍 查詢前台會員列表: {}", req);
        
        FrontendUserCondition condition = req != null ? req.getCondition() : null;
        
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        
        // ✅ 排除已刪除的會員
        criteria.andStatusNotEqualTo(UserStatusEnum.DELETED.getCode());
        
        // ✅ 所有條件都是可選的
        if (condition != null) {
            if (isNotBlank(condition.getEmail())) {
                criteria.andEmailLike("%" + condition.getEmail() + "%");
            }
            if (isNotBlank(condition.getNickname())) {
                criteria.andNicknameLike("%" + condition.getNickname() + "%");
            }
            if (isNotBlank(condition.getPhone())) {
                criteria.andPhoneNumberLike("%" + condition.getPhone() + "%");
            }
            if (condition.getStatus() != null) {
                criteria.andStatusEqualTo(condition.getStatus());
            }
            if (condition.getProvider() != null) {
                criteria.andProviderEqualTo(condition.getProvider());
            }
            if (condition.getGoldCoinsMin() != null) {
                criteria.andGoldCoinsGreaterThanOrEqualTo(condition.getGoldCoinsMin());
            }
            if (condition.getGoldCoinsMax() != null) {
                criteria.andGoldCoinsLessThanOrEqualTo(condition.getGoldCoinsMax());
            }
            // 日期範圍（LocalDate 轉 LocalDateTime）
            if (condition.getCreatedAtStart() != null) {
                criteria.andCreatedAtGreaterThanOrEqualTo(
                    condition.getCreatedAtStart().atStartOfDay()
                );
            }
            if (condition.getCreatedAtEnd() != null) {
                criteria.andCreatedAtLessThanOrEqualTo(
                    condition.getCreatedAtEnd().atTime(23, 59, 59)
                );
            }
            if (isNotBlank(condition.getKeyword())) {
                // 關鍵字搜尋：Email 或暱稱 或手機號碼
                UserExample.Criteria orCriteria = example.or();
                orCriteria.andEmailLike("%" + condition.getKeyword() + "%");
                orCriteria = example.or();
                orCriteria.andNicknameLike("%" + condition.getKeyword() + "%");
                orCriteria = example.or();
                orCriteria.andPhoneNumberLike("%" + condition.getKeyword() + "%");
            }
        }
        
        // 排序
        if (req != null && req.getSortBy() != null) {
            String order = req.getSortOrder() != null ? req.getSortOrder() : "ASC";
            example.setOrderByClause(req.getSortBy() + " " + order);
        } else {
            example.setOrderByClause("created_at DESC");
        }
        
        List<User> users = userMapper.selectByExample(example);
        
        log.info("✅ 查詢成功: 共 {} 筆", users.size());
        
        return users.stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }
    
    @Override
    public FrontendUserRes getUserById(String id) {
        log.info("🔍 查詢會員詳情: userId={}", id);
        
        User user = userMapper.selectByPrimaryKey(id);
        if (user == null || UserStatusEnum.DELETED.getCode().equals(user.getStatus())) {
            throw new BusinessException("會員不存在");
        }
        
        return toRes(user);
    }
    
    @Override
    public FrontendUserRes updateUser(String id, FrontendUserUpdateReq req) {
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
        return toRes(user);
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
    private FrontendUserRes toRes(User user) {
        return FrontendUserRes.builder()
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
        if (user == null) throw new BusinessException("用戶不存在");
        if ("GOLD".equals(req.getCoinType())) {
            long newGold = (user.getGoldCoins() == null ? 0L : user.getGoldCoins()) + req.getAmount();
            if (newGold < 0) throw new BusinessException("金幣餘額不足");
            user.setGoldCoins(newGold);
        } else if ("BONUS".equals(req.getCoinType())) {
            long newBonus = (user.getBonusCoins() == null ? 0L : user.getBonusCoins()) + req.getAmount();
            if (newBonus < 0) throw new BusinessException("紅利不足");
            user.setBonusCoins(newBonus);
        } else {
            throw new BusinessException("coinType 不合法，只支援 GOLD / BONUS");
        }
        userMapper.updateByPrimaryKey(user);
        log.info("✅ 點數調整成功: userId={}, coinType={}, amount={}", userId, req.getCoinType(), req.getAmount());
    }
}
