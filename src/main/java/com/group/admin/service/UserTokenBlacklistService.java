package com.group.admin.service;

public interface UserTokenBlacklistService {
    int getBlacklistGen(String userId);
    void invalidateUserTokens(String userId);
}
