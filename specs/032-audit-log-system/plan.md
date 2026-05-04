# 實作計畫：稽核日誌系統（Audit Log System）

**Branch**: `032-audit-log-system` | **日期**: 2026-05-02 | **規格**: [spec.md](./spec.md)

---

## 摘要

將現有完全閒置的 `system_log` / `admin_audit_log` 基礎設施全面重建為規格書定義的 **5 分表 + AOP 零侵入** 架構。
以 `@AuditLog` 自訂 annotation 切入關鍵操作，`@Async` 非同步寫入，`AuditContext` ThreadLocal 傳遞前後快照。
同步刪除舊的 `SystemLog` 相關程式碼，讓 `log_admin_action` 成為後台敏感操作唯一審計標準。

---

## 技術背景

**語言/版本**：Java 21
**框架**：Spring Boot 3.3.3 + MyBatis 3.0.5 + Spring Security
**前置依賴**：無（獨立功能，不依賴其他 spec）
**風險等級**：中（需刪除舊程式碼 + 新 AOP 切入，但業務邏輯不受影響）

---

## 架構規範檢查

| 關卡 | 狀態 | 備註 |
|------|------|------|
| @Async 非同步寫入 | ✅ | `AsyncConfig.java` 已存在，可直接使用 |
| AOP 零侵入 | ✅ | `@Around` 攔截 `@AuditLog` 標記方法 |
| 分層職責 | ✅ | Aspect 處理寫表，Business Service 專注業務 |
| Controller → Service 分層 | ✅ | |
| ThreadLocal 安全清理 | ⚠️ | `AuditContext` 必須在 finally 中 clear，防止記憶體洩漏 |
| SecurityUtils 取得 userId | ✅ | JWT 已驗證 |
| AOP 自動包裝回應 | ✅ | |

---

## 現有程式碼狀態盤點

### 需要刪除（舊 SystemLog 基礎設施）
```text
entity/SystemLog.java
example/SystemLogExample.java
mapper/SystemLogMapper.java
resources/mapper/SystemLogMapper.xml
service/SystemLogService.java
service/impl/SystemLogServiceImpl.java
controller/admin/AdminSystemLogController.java
```

### 需要替換（舊 AuditLog 手動呼叫版）
```text
entity/AdminAuditLog.java              ← 對應 admin_audit_log 表（舊），由 LogAdminAction.java 取代
mapper/AdminAuditLogMapper.java        ← 同上
resources/mapper/AdminAuditLogMapper.xml ← 同上
service/AuditLogService.java           ← 舊介面，完全重寫
service/impl/AuditLogServiceImpl.java  ← 舊實作，完全重寫
```

> ⚠️ `PermissionAuditLogMapper.java` 和 `UserLoginHistoryMapper.java` 為不同功能，**不刪除**。

### AsyncConfig 已存在
```text
config/AsyncConfig.java  ← 已有 @EnableAsync + ThreadPoolTaskExecutor，直接沿用
```

---

## 新專案結構

### 新建 DDL（5 張表）
```text
sql/032-audit-log-system.sql
  ├── log_auth        (認證日誌)
  ├── log_draw        (抽獎日誌)
  ├── log_recharge    (儲值日誌)
  ├── log_order       (訂單操作日誌)
  └── log_admin_action(後台管理操作日誌)
```

### 新建程式碼
```text
src/main/java/com/group/admin/
├── annotation/
│   └── AuditLog.java                          (自訂 annotation)
├── enums/
│   └── AuditLogType.java                      (AUTH / DRAW / RECHARGE / ORDER / ADMIN_ACTION)
├── util/
│   └── AuditContext.java                      (ThreadLocal before/after snapshot 工具)
├── aop/
│   └── AuditLogAspect.java                    (@Around AOP 切面，依 AuditLogType 分流寫入)
├── entity/
│   ├── LogAuth.java                           (MBG 生成)
│   ├── LogDraw.java                           (MBG 生成)
│   ├── LogRecharge.java                       (MBG 生成)
│   ├── LogOrder.java                          (MBG 生成)
│   └── LogAdminAction.java                    (MBG 生成)
├── example/
│   ├── LogAuthExample.java                    (MBG 生成)
│   ├── LogDrawExample.java                    (MBG 生成)
│   ├── LogRechargeExample.java                (MBG 生成)
│   ├── LogOrderExample.java                   (MBG 生成)
│   └── LogAdminActionExample.java             (MBG 生成)
├── mapper/
│   ├── LogAuthMapper.java                     (MBG 生成)
│   ├── LogDrawMapper.java                     (MBG 生成)
│   ├── LogRechargeMapper.java                 (MBG 生成)
│   ├── LogOrderMapper.java                    (MBG 生成)
│   └── LogAdminActionMapper.java              (MBG 生成)
├── service/
│   └── AuditLogService.java                   (重寫：5 個 logXxx() 方法)
└── service/impl/
    └── AuditLogServiceImpl.java               (重寫：@Async + 5 個分表寫入實作)

resources/mapper/
├── LogAuthMapper.xml                          (MBG 生成)
├── LogDrawMapper.xml                          (MBG 生成)
├── LogRechargeMapper.xml                      (MBG 生成)
├── LogOrderMapper.xml                         (MBG 生成)
└── LogAdminActionMapper.xml                   (MBG 生成)
```

### @AuditLog 切入點（第一批）

| 方法 | type | action |
|------|------|--------|
| `LoginController.adminLogin()` | AUTH | 後台登入 |
| `LoginController.userLogin()` | AUTH | 前台登入 |
| `AdminLotteryController.createLottery()` | ADMIN_ACTION | CREATE_LOTTERY |
| `AdminLotteryController.updateLottery()` | ADMIN_ACTION | UPDATE_LOTTERY |
| `AdminLotteryController.onShelf()` | ADMIN_ACTION | ON_SHELF_LOTTERY |
| `AdminLotteryController.offShelf()` | ADMIN_ACTION | OFF_SHELF_LOTTERY |
| `AdminStoreController` create/update/enable/disable | ADMIN_ACTION | 店家管理 |
| `AdminUserController` create/resetPassword/enable/disable | ADMIN_ACTION | 後台帳號管理 |

> 本次 scope 不切入 DRAW / RECHARGE / ORDER（表結構留空，後續 spec 補充）。

---

## 服務介面設計（重寫後）

```java
public interface AuditLogService {
    // 認證日誌（登入/登出/OAuth）
    void logAuth(String userId, String userType, String email,
                 String loginMethod, String result, String errorMessage,
                 String ip, String userAgent);

    // 後台管理操作日誌
    void logAdminAction(String adminId, String adminEmail, String adminRole,
                        String targetType, String targetId, String targetName,
                        String action, String beforeSnapshot, String afterSnapshot,
                        String result, String errorMessage, String ip);

    // 抽獎日誌（第二批實作）
    void logDraw(/* ... */);

    // 儲值日誌（第二批實作）
    void logRecharge(/* ... */);

    // 訂單操作日誌（第二批實作）
    void logOrder(/* ... */);
}
```

---

## AOP 切面設計

```java
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        String result = "SUCCESS";
        String errorMessage = null;
        try {
            Object ret = joinPoint.proceed();
            return ret;
        } catch (Throwable t) {
            result = "FAIL";
            errorMessage = t.getMessage();
            throw t;
        } finally {
            long duration = System.currentTimeMillis() - start;
            String before = AuditContext.getBefore();
            String after  = AuditContext.getAfter();
            AuditContext.clear();  // 必須清理，防止 ThreadLocal 洩漏
            // 依 auditLog.type() 非同步寫入對應 log 表
            auditLogService.dispatchAsync(auditLog, joinPoint, result, errorMessage, before, after, duration);
        }
    }
}
```

---

## 複雜度追蹤

| 面向 | 數量 |
|------|------|
| 新建 DDL | 5 張表 |
| MBG 生成（entity/mapper/example/xml） | 5 組 × 4 = 20 個檔案 |
| 新建程式碼（annotation/enum/util/aop/service） | ~8 個檔案 |
| 刪除舊程式碼 | ~7 個檔案 |
| @AuditLog 切入點（第一批） | ~15 個方法 |
| 預估工時 | 1-2 天 |

---

## 風險

- `AuditContext` ThreadLocal 未清理會在執行緒池重用時污染下一個請求 → 必須在 finally 中 `AuditContext.clear()`
- AOP `@Around` 若 `joinPoint.proceed()` 拋出例外需重新 rethrow → 切面必須 `throw t`，不能吞例外
- `@Async` 方法必須從 Spring 代理呼叫（不能同類內自呼叫） → `AuditLogServiceImpl.dispatchAsync()` 由 Aspect 外部呼叫，無問題
- 刪除 `SystemLog` 相關程式碼前需確認 `ScheduledTasks.java` 中的 `systemLogService.deleteOldLogs()` 呼叫一併移除

---

## 後台查詢 API（本次不實作，留給後續 spec）

> 後台稽核日誌查詢 API（`GET /admin/audit-log/admin-actions`）設計複雜度高，
> 建議另開 spec 或在 032 tasks 的最後一個階段單獨實作，避免影響核心 AOP 架構。
