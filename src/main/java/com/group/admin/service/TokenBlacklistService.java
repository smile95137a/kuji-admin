package com.group.admin.service;

public interface TokenBlacklistService {
    /** Increment blacklist_gen for this user, invalidating all existing tokens */
    void invalidateUser(String adminUserId);

    /** Returns true if the given tokenGen is less than the current DB gen for this user */
    boolean isBlacklisted(String adminUserId, long tokenGen);

    /** Returns current gen (0 if no record) */
    long getCurrentGen(String adminUserId);
}
