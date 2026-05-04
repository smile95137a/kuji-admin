package com.group.admin.util;

/**
 * AuditContext：在 AOP 切面中透過 ThreadLocal 傳遞 before/after 快照。
 * 必須在 finally 呼叫 clear()，防止執行緒池重用時資料污染。
 */
public class AuditContext {

    private static final ThreadLocal<String> BEFORE = new ThreadLocal<>();
    private static final ThreadLocal<String> AFTER  = new ThreadLocal<>();

    private AuditContext() {}

    public static void setBefore(String json) { BEFORE.set(json); }
    public static void setAfter(String json)  { AFTER.set(json);  }

    public static String getBefore() { return BEFORE.get(); }
    public static String getAfter()  { return AFTER.get();  }

    public static void clear() {
        BEFORE.remove();
        AFTER.remove();
    }
}
