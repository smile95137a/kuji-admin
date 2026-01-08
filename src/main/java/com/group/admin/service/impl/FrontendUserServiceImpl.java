package com.group.admin.service.impl;

import com.group.admin.entity.User;
import com.group.admin.enums.UserStatusEnum;
import com.group.admin.example.UserExample;
import com.group.admin.exception.BusinessException;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.common.QueryReq;
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
            if (condition.getEmail() != null && !condition.getEmail().isEmpty()) {
                criteria.andEmailLike("%" + condition.getEmail() + "%");
            }
            if (condition.getNickname() != null && !condition.getNickname().isEmpty()) {
                criteria.andNicknameLike("%" + condition.getNickname() + "%");
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
            if (condition.getCreatedAtStart() != null) {
                criteria.andCreatedAtGreaterThanOrEqualTo(condition.getCreatedAtStart());
            }
            if (condition.getCreatedAtEnd() != null) {
                criteria.andCreatedAtLessThanOrEqualTo(condition.getCreatedAtEnd());
            }
            if (condition.getKeyword() != null && !condition.getKeyword().isEmpty()) {
                // 關鍵字搜尋：Email 或暱稱
                UserExample.Criteria orCriteria = example.or();
                orCriteria.andEmailLike("%" + condition.getKeyword() + "%");
                orCriteria = example.or();
                orCriteria.andNicknameLike("%" + condition.getKeyword() + "%");
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
        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
        }
        if (req.getGoldCoins() != null) {
            user.setGoldCoins(req.getGoldCoins());
        }
        if (req.getBonusCoins() != null) {
            user.setBonusCoins(req.getBonusCoins());
        }
        if (req.getPhoneNumber() != null) {
            user.setPhoneNumber(req.getPhoneNumber());
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
}
