package com.group.admin.service.impl;

import com.group.admin.entity.UserLoginHistory;
import com.group.admin.example.UserLoginHistoryExample;
import com.group.admin.mapper.UserLoginHistoryMapper;
import com.group.admin.service.LoginHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginHistoryServiceImpl implements LoginHistoryService {

    private final UserLoginHistoryMapper userLoginHistoryMapper;

    @Override
    public void record(String userId, String userType, String loginMethod, String status,
                       String failReason, String ipAddress, String deviceInfo) {
        try {
            UserLoginHistory history = new UserLoginHistory();
            history.setId(UUID.randomUUID().toString());
            history.setUserId(userId);
            history.setUserType(userType);
            history.setLoginMethod(loginMethod);
            history.setStatus(status);
            history.setFailReason(failReason);
            history.setIpAddress(ipAddress);
            history.setDeviceInfo(deviceInfo);
            history.setLoginTime(LocalDateTime.now());
            history.setCreatedAt(LocalDateTime.now());
            userLoginHistoryMapper.insertSelective(history);
        } catch (Exception e) {
            log.warn("⚠️ 登入記錄寫入失敗: {}", e.getMessage());
        }
    }

    @Override
    public List<UserLoginHistory> getHistory(String userId, String userType, int limit) {
        UserLoginHistoryExample example = new UserLoginHistoryExample();
        UserLoginHistoryExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userId);
        if (userType != null) {
            criteria.andUserTypeEqualTo(userType);
        }
        example.setOrderByClause("login_time DESC");
        List<UserLoginHistory> list = userLoginHistoryMapper.selectByExample(example);
        return list.size() > limit ? list.subList(0, limit) : list;
    }
}
