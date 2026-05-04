# 任務清單：稽核日誌系統（Audit Log System）

**輸入**：設計文件來自 `/specs/032-audit-log-system/`
**分支**：`032-audit-log-system` | **建立日期**：2026-05-02

---

## 第一階段：DDL → 套用 DB → MBG 生成

**目的**：建立 5 張 log 表對應的 Entity / Mapper / Example / XML。

- [ ] T001 建立 `sql/032-audit-log-system.sql`，包含 5 張表 DDL：
  - `log_auth`（認證日誌）
  - `log_draw`（抽獎日誌）
  - `log_recharge`（儲值日誌）
  - `log_order`（訂單操作日誌）
  - `log_admin_action`（後台管理操作日誌）
  - 每張表皆需含 `id VARCHAR(36) PRIMARY KEY`、`created_at DATETIME DEFAULT CURRENT_TIMESTAMP`、必要索引
- [ ] T002 將 T001 的 DDL 套用至開發 DB（`mysql -u xxx -p < sql/032-audit-log-system.sql`）
- [ ] T003 在 `generatorConfig.xml` 加入 5 張表的設定（`log_auth`, `log_draw`, `log_recharge`, `log_order`, `log_admin_action`）
- [ ] T004 執行 `.\run-mbg.ps1` 生成 5 組 Entity / Mapper / Example / XML
- [ ] T005 執行 `mvn clean compile` 確認生成檔案編譯通過

**檢查點**：5 張表 DDL 套用完成，5 組 MBG 檔案已生成且編譯通過

---

## 第二階段：AuditContext ThreadLocal 工具

**目的**：建立 ThreadLocal 傳遞 before/after snapshot 的工具類。

- [ ] T006 建立 `src/main/java/com/group/admin/util/AuditContext.java`：
  ```java
  public class AuditContext {
      private static final ThreadLocal<String> BEFORE = new ThreadLocal<>();
      private static final ThreadLocal<String> AFTER  = new ThreadLocal<>();

      public static void setBefore(Object obj) { BEFORE.set(toJson(obj)); }
      public static void setAfter(Object obj)  { AFTER.set(toJson(obj)); }
      public static String getBefore() { return BEFORE.get(); }
      public static String getAfter()  { return AFTER.get(); }
      public static void clear()       { BEFORE.remove(); AFTER.remove(); }
      private static String toJson(Object obj) { /* Jackson ObjectMapper */ }
  }
  ```
  - 使用 `ObjectMapper` 序列化（注入或 static 建立）
  - 序列化失敗時 log.warn 並返回 null，不拋例外

**檢查點**：`AuditContext.setBefore()` / `setAfter()` / `getBefore()` / `clear()` 正確運作

---

## 第三階段：@AuditLog Annotation + AuditLogType Enum

**目的**：定義 AOP 切入所需的 annotation 與 type enum。

- [ ] T007 建立 `src/main/java/com/group/admin/annotation/AuditLog.java`：
  ```java
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface AuditLog {
      AuditLogType type();
      String action() default "";
      String targetType() default "";
  }
  ```
- [ ] T008 建立 `src/main/java/com/group/admin/enums/AuditLogType.java`：
  ```java
  public enum AuditLogType {
      AUTH,       // → log_auth
      DRAW,       // → log_draw
      RECHARGE,   // → log_recharge
      ORDER,      // → log_order
      ADMIN_ACTION// → log_admin_action
  }
  ```

**檢查點**：annotation 和 enum 編譯通過，可被 @Around 掃描

---

## 第四階段：重寫 AuditLogService + AuditLogServiceImpl

**目的**：將舊的 `AuditLogServiceImpl`（手動呼叫版）改為支援 @Async 分表寫入的新版。

- [ ] T009 重寫 `service/AuditLogService.java`（介面），定義 5 個寫入方法：
  - `logAuth(...)` — 寫 `log_auth`
  - `logAdminAction(...)` — 寫 `log_admin_action`
  - `logDraw(...)` — 寫 `log_draw`（參數暫定，本次可留 stub）
  - `logRecharge(...)` — 寫 `log_recharge`（stub）
  - `logOrder(...)` — 寫 `log_order`（stub）
- [ ] T010 重寫 `service/impl/AuditLogServiceImpl.java`：
  - 注入 5 個 Mapper（`LogAuthMapper`, `LogDrawMapper`, `LogRechargeMapper`, `LogOrderMapper`, `LogAdminActionMapper`）
  - 每個方法加 `@Async`（使用 `AsyncConfig` 已定義的 executor）
  - `logAuth()` 完整實作，填入所有欄位並寫入 `log_auth`
  - `logAdminAction()` 完整實作，填入所有欄位並寫入 `log_admin_action`
  - `logDraw()` / `logRecharge()` / `logOrder()` 暫留空實作（`log.debug("TODO")`）
  - 所有方法包裹在 try-catch，Exception 只 log.warn 不拋出（不影響主流程）

**檢查點**：logAuth / logAdminAction 可正確寫入對應表

---

## 第五階段：AuditLogAspect AOP 切面

**目的**：實作 `@Around` AOP 切面，自動攔截 `@AuditLog` 標記的方法。

- [ ] T011 建立 `src/main/java/com/group/admin/aop/AuditLogAspect.java`：
  - `@Around("@annotation(auditLog)")` 攔截所有標記方法
  - 記錄開始時間，`joinPoint.proceed()` 執行業務
  - 成功則 result="SUCCESS"；捕獲例外則 result="FAIL"，`errorMessage=t.getMessage()`，最後 rethrow
  - finally 中：取 `AuditContext.getBefore()` / `getAfter()`，執行 `AuditContext.clear()`
  - 依 `auditLog.type()` 呼叫對應的 `auditLogService.logXxx(...)` 非同步寫入
  - IP 從 `HttpServletRequest`（注入 `HttpServletRequest`）取得，考慮 `X-Forwarded-For`
  - userId / adminId 從 `SecurityUtils.getCurrentAdminUserId()` 或 `SecurityUtils.getCurrentUserId()` 取得
- [ ] T012 確認 `AuditLogAspect` 在 Spring AOP proxy 正確初始化（`@Component @Aspect`）

**檢查點**：在測試方法上加 `@AuditLog(type=AUTH, action="test")` 觸發後，`log_auth` 中有對應記錄

---

## 第六階段：切入第一批 @AuditLog

**目的**：在關鍵 Controller 方法上標記 `@AuditLog`，覆蓋認證與後台操作。

### 認證類（type=AUTH）
- [ ] T013 `LoginController.adminLogin()` 加 `@AuditLog(type=AUTH, action="後台登入")`
  - Service 層在登入成功/失敗後呼叫 `AuditContext.setAfter(result)`
- [ ] T014 `LoginController.userLogin()` 加 `@AuditLog(type=AUTH, action="前台登入")`

### 後台商品管理（type=ADMIN_ACTION）
- [ ] T015 `AdminLotteryController.createLottery()` 加 `@AuditLog(type=ADMIN_ACTION, targetType="LOTTERY", action="CREATE")`
  - Service 層 `createLottery()` 結束後呼叫 `AuditContext.setAfter(savedLottery)`
- [ ] T016 `AdminLotteryController.updateLottery()` 加 `@AuditLog(type=ADMIN_ACTION, targetType="LOTTERY", action="UPDATE")`
  - Service 層 `updateLottery()` 前呼叫 `AuditContext.setBefore(existingLottery)`，後呼叫 `setAfter(updatedLottery)`
- [ ] T017 `AdminLotteryController.onShelf()` / `offShelf()` 加對應 `@AuditLog`

### 後台帳號管理（type=ADMIN_ACTION）
- [ ] T018 `AdminUserController.createStoreUser()` 加 `@AuditLog(type=ADMIN_ACTION, targetType="ADMIN_USER", action="CREATE")`
- [ ] T019 `AdminUserController.resetPassword()` 加 `@AuditLog(type=ADMIN_ACTION, targetType="ADMIN_USER", action="RESET_PASSWORD")`
- [ ] T020 `AdminUserController.enable()` / `disable()` 加對應 `@AuditLog`

### 後台店家管理（type=ADMIN_ACTION）
- [ ] T021 `AdminStoreController.createStore()` / `updateStore()` / `enable()` / `disable()` 加對應 `@AuditLog`

**檢查點**：商品上架、後台登入、帳號操作後，各自對應的 log 表有記錄

---

## 第七階段：刪除舊 SystemLog + 替換舊 AdminAuditLog

**目的**：清除閒置的舊程式碼，避免混淆。

- [ ] T022 刪除 `entity/SystemLog.java`
- [ ] T023 刪除 `example/SystemLogExample.java`
- [ ] T024 刪除 `mapper/SystemLogMapper.java`
- [ ] T025 刪除 `resources/mapper/SystemLogMapper.xml`
- [ ] T026 刪除 `service/SystemLogService.java`
- [ ] T027 刪除 `service/impl/SystemLogServiceImpl.java`
- [ ] T028 刪除 `controller/admin/AdminSystemLogController.java`
- [ ] T029 檢查並修正 `scheduler/ScheduledTasks.java`：
  - 移除 `systemLogService` 注入與 `deleteOldLogs()` 呼叫
  - 若 ScheduledTasks 只剩此呼叫，整支檔案一併刪除
- [ ] T030 刪除 `entity/AdminAuditLog.java`（由 `LogAdminAction.java` 取代）
- [ ] T031 刪除 `mapper/AdminAuditLogMapper.java`
- [ ] T032 刪除 `resources/mapper/AdminAuditLogMapper.xml`
  > ⚠️ 刪除前先確認沒有其他 Service / Controller 仍引用 `AdminAuditLogMapper`

**檢查點**：專案中搜尋 `SystemLog`、`AdminAuditLog` 皆無結果（worktrees 目錄除外）

---

## 第八階段：編譯驗證

- [ ] T033 執行 `mvn clean compile` 確認無編譯錯誤
- [ ] T034 執行 `mvn clean package -DskipTests` 確認可打包
- [ ] T035 啟動應用程式，確認：
  - Spring Context 正常啟動（AuditLogAspect 無初始化錯誤）
  - 後台登入後 `log_auth` 表有記錄
  - 商品建立後 `log_admin_action` 表有記錄
  - `@Async` 寫入不影響 API 回應時間

---

## 依賴關係

```
內部依賴：
  第一階段（DDL + MBG）→ 第四階段（Service 可注入 Mapper）
  第二階段（AuditContext）→ 第五階段（Aspect 使用 AuditContext）
  第三階段（annotation/enum）→ 第四、五階段
  第四階段（Service 重寫）→ 第五階段（Aspect 呼叫 Service）
  第五階段（Aspect）→ 第六階段（切入點加 annotation）
  第六、七階段 → 可平行進行（不互相依賴）
  第八階段 → 全部完成後執行

並行可執行：
  T006（AuditContext）可與 T001-T005（DDL/MBG）同步進行
  T007-T008（annotation/enum）可與第一階段同步進行
  T022-T032（舊程式碼刪除）可在第四、五階段完成後進行
```

---

## 注意事項

- 刪除舊程式碼前，先在 main branch 做好 `mvn clean compile` 確認基線通過
- `AuditContext.clear()` 必須放在 `finally` 中，無論成功或失敗都要執行
- `@Async` 方法必須是 `public`，且必須從 Spring Proxy 呼叫（不能同 class 內自呼叫）
- 本次不實作後台 log 查詢 API（留給後續規劃）
- `log_draw` / `log_recharge` / `log_order` 的 Service 方法本次留 stub，不標記 `@AuditLog`
