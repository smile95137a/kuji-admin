package com.group.admin.service.impl;

import com.group.admin.entity.AdminTokenBlacklist;
import com.group.admin.mapper.AdminTokenBlacklistMapper;
import com.group.admin.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final AdminTokenBlacklistMapper adminTokenBlacklistMapper;

    @Override
    @Transactional
    public void invalidateUser(String adminUserId) {
        log.info("🚫 [TokenBlacklist] invalidate userId={}", adminUserId);
        adminTokenBlacklistMapper.incrementGen(adminUserId);
    }

    @Override
    public boolean isBlacklisted(String adminUserId, long tokenGen) {
        AdminTokenBlacklist record = adminTokenBlacklistMapper.selectByAdminUserId(adminUserId);
        if (record == null) {
            return false;
        }
        return record.getBlacklistGen() > tokenGen;
    }

    @Override
    public long getCurrentGen(String adminUserId) {
        AdminTokenBlacklist record = adminTokenBlacklistMapper.selectByAdminUserId(adminUserId);
        return record != null ? record.getBlacklistGen() : 0L;
    }
}
