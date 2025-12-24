# 🔍 AdminJwtAuthenticationFilter.java 錯誤分析

## ❌ 主要問題

### 1. 缺少 UserPrincipal import
**原始程式碼：**
```java
// 沒有 import UserPrincipal
```

**錯誤訊息：**
```
UserPrincipal cannot be resolved to a type
```

### 2. 錯誤的 @NonNull annotation
**原始程式碼（第 24 行）：**
```java
import io.micrometer.common.lang.NonNull;  // ❌ 錯誤的 package
```

**正確的應該是：**
```java
import org.springframework.lang.NonNull;  // ✅ 正確的 package
```

## ✅ 已修正

### 修正後的 import 區塊
```java
import com.group.admin.entity.AdminUser;
import com.group.admin.entity.AdminUserRole;
import com.group.admin.entity.Role;
import com.group.admin.example.AdminUserExample;
import com.group.admin.example.AdminUserRoleExample;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.security.UserPrincipal;  // ✅ 新增
import com.group.admin.util.JwtUtil;

import org.springframework.lang.NonNull;  // ✅ 修正
```

## 🔧 需要做的事

### 步驟 1：重新編譯
```bash
mvn clean compile -DskipTests
```

這會：
1. 清理 target 目錄
2. 重新編譯所有 Java 檔案
3. 生成新的 .class 檔案

### 步驟 2：確認編譯成功
看到這個訊息表示成功：
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: XX.XXX s
```

### 步驟 3：重啟應用
```bash
mvn spring-boot:run
```

或

```bash
java -jar target\admin-1.0.0.jar
```

## 🎯 為什麼會有這些錯誤？

### 1. VS Code / IDE 的問題
- IDE 還在使用**舊的編譯快取**
- 沒有偵測到新的程式碼變更
- 需要重新編譯才會更新

### 2. 循環依賴問題
- `AdminJwtAuthenticationFilter` 需要 `UserPrincipal`
- `UserPrincipal` 在同一個 package (`com.group.admin.security`)
- 但是沒有正確 import

### 3. @NonNull annotation 來源錯誤
- 使用了 `io.micrometer.common.lang.NonNull`（Micrometer 的）
- Spring Framework 期望 `org.springframework.lang.NonNull`

## 📝 完整的檔案結構

```
src/main/java/com/group/admin/
├── security/
│   ├── AdminJwtAuthenticationFilter.java  ← 這個檔案
│   ├── ApiJwtAuthenticationFilter.java
│   └── UserPrincipal.java  ← 需要 import 的類別
├── entity/
│   ├── AdminUser.java
│   ├── AdminUserRole.java
│   └── Role.java
├── mapper/
│   ├── AdminUserMapper.java
│   ├── AdminUserRoleMapper.java
│   └── RoleMapper.java
├── example/
│   ├── AdminUserExample.java
│   └── AdminUserRoleExample.java
└── util/
    └── JwtUtil.java
```

## ⚡ 快速修復檢查清單

- [x] 修正 `@NonNull` import（從 micrometer 改為 springframework）
- [x] 新增 `UserPrincipal` import
- [ ] 執行 `mvn clean compile -DskipTests`
- [ ] 確認編譯成功（看到 BUILD SUCCESS）
- [ ] 重啟應用程式
- [ ] 測試 API

## 🔄 下一步

1. **等待編譯完成**
2. **檢查編譯輸出**，如果有錯誤，貼出來
3. **重啟應用程式**
4. **測試 API**，應該會看到詳細的日誌：
   ```
   🔍 [AdminJwtFilter] 收到請求: /admin/menus/accessible
   🔐 [AdminJwtFilter] 開始認證
   👤 [AdminJwtFilter] 使用者: admin@kuji.com
   ✅ [AdminJwtFilter] 認證成功
   ```

---

**當前狀態：** 正在編譯中...
**預計時間：** 約 10-15 秒
