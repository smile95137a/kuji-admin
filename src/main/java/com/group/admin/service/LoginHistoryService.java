package com.group.admin.service;

import com.group.admin.entity.UserLoginHistory;
import java.util.List;

public interface LoginHistoryService {
    void record(String userId, String userType, String loginMethod, String status, String failReason, String ipAddress, String deviceInfo);
    List<UserLoginHistory> getHistory(String userId, String userType, int limit);
}
