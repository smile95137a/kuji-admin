package com.group.admin.service.impl;

import com.group.admin.entity.UserTokenBlacklist;
import com.group.admin.mapper.UserTokenBlacklistMapper;
import com.group.admin.service.UserTokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTokenBlacklistServiceImpl implements UserTokenBlacklistService {

    private final UserTokenBlacklistMapper userTokenBlacklistMapper;

    @Override
    public int getBlacklistGen(String userId) {
        UserTokenBlacklist record = userTokenBlacklistMapper.selectByPrimaryKey(userId);
        return record != null && record.getBlacklistGen() != null ? record.getBlacklistGen() : 0;
    }

    @Override
    public void invalidateUserTokens(String userId) {
        userTokenBlacklistMapper.incrementBlacklistGen(userId);
        log.info("✅ 前台用戶 token 已失效: userId={}", userId);
    }
}
