package com.group.admin.util;

/**
 * AuditContext：在 AOP 切面中透過 ThreadLocal 傳遞 before/after 快照。
 * 必須在 finally 呼叫 clear()，防止執行緒池重用時資料污染。
 */
public class AuditContext {

    private static final ThreadLocal<String> BEFORE = new ThreadLocal<>();
    private static final ThreadLocal<String> AFTER  = new ThreadLocal<>();
    private static final ThreadLocal<String> TARGET_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> TARGET_NAME = new ThreadLocal<>();
    private static final ThreadLocal<String> AUTH_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> AUTH_USER_TYPE = new ThreadLocal<>();
    private static final ThreadLocal<String> AUTH_ATTEMPTED_USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<String> AUTH_RESOLVED_EMAIL = new ThreadLocal<>();
    private static final ThreadLocal<String> AUTH_RESOLVED_USERNAME = new ThreadLocal<>();

    private AuditContext() {}

    public static void setBefore(String json) { BEFORE.set(json); }
    public static void setAfter(String json)  { AFTER.set(json);  }
    public static void setTargetId(String targetId) { TARGET_ID.set(targetId); }
    public static void setTargetName(String targetName) { TARGET_NAME.set(targetName); }

    public static void setAuthAttemptedUsername(String username) {
        AUTH_ATTEMPTED_USERNAME.set(username);
    }

    public static void setAuthResolvedUser(String userId, String email, String username, String userType) {
        AUTH_USER_ID.set(userId);
        AUTH_RESOLVED_EMAIL.set(email);
        AUTH_RESOLVED_USERNAME.set(username);
        AUTH_USER_TYPE.set(userType);
    }

    public static String getBefore() { return BEFORE.get(); }
    public static String getAfter()  { return AFTER.get();  }
    public static String getTargetId() { return TARGET_ID.get(); }
    public static String getTargetName() { return TARGET_NAME.get(); }
    public static String getAuthUserId() { return AUTH_USER_ID.get(); }
    public static String getAuthUserType() { return AUTH_USER_TYPE.get(); }
    public static String getAuthAttemptedUsername() { return AUTH_ATTEMPTED_USERNAME.get(); }
    public static String getAuthResolvedEmail() { return AUTH_RESOLVED_EMAIL.get(); }
    public static String getAuthResolvedUsername() { return AUTH_RESOLVED_USERNAME.get(); }

    public static void clear() {
        BEFORE.remove();
        AFTER.remove();
        TARGET_ID.remove();
        TARGET_NAME.remove();
        AUTH_USER_ID.remove();
        AUTH_USER_TYPE.remove();
        AUTH_ATTEMPTED_USERNAME.remove();
        AUTH_RESOLVED_EMAIL.remove();
        AUTH_RESOLVED_USERNAME.remove();
    }
}
