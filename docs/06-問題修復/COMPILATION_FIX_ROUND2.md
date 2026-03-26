# 第二輪編譯錯誤修復總結

## 執行時間
2025-12-11 04:45

## 修復的 14 個錯誤

### 1. ✅ JwtUtil.extractUsername() 方法不存在
**錯誤**: `cannot find symbol: method extractUsername(java.lang.String)`

**原因**: JwtUtil 實際的方法名是 `getUsername()`

**修復**: 
- 檔案: `AdminJwtAuthenticationFilter.java`
- 修改: `jwtUtil.extractUsername(token)` → `jwtUtil.getUsername(token)`

---

### 2. ✅ UserPrincipal.enabled 欄位不存在
**錯誤**: `cannot find symbol: method enabled(boolean)`

**原因**: UserPrincipal 沒有 enabled 欄位，而且 isEnabled() 方法固定返回 true

**修復**: 
- 檔案: `UserPrincipal.java` - 新增 authorities, adminUser, user 欄位
- 檔案: `AdminJwtAuthenticationFilter.java` - 移除 `.enabled()`, `.accountNonExpired()` 等呼叫
- 改用 `.isAdmin(true)` 來標記管理員身份

---

### 3. ✅ User.id 型別不一致 (Long vs String)
**錯誤**: `incompatible types: java.lang.String cannot be converted to java.lang.Long`

**原因**: 
- UserServiceImpl 使用 `UUID.randomUUID().toString()` 生成 String 型別的 ID
- UserMapper.xml 的 UserMap resultMap 使用 String 型別
- 但 User entity 原本定義為 Long

**修復**: 
- 檔案: `User.java`
- 修改: `private Long id;` → `private String id;`
- 刪除所有手動寫的 getter/setter（讓 Lombok 自動生成）

---

### 4. ✅ User entity 缺少欄位
**錯誤**: 
- `cannot find symbol: method setAvatar()`
- `cannot find symbol: method setCreateDate()`
- `cannot find symbol: method setUpdateDate()`

**原因**: User entity 缺少 UserMap resultMap 所需的欄位

**修復**:
- 檔案: `User.java`
- 新增欄位:
  ```java
  private String avatar;
  private LocalDateTime lastLogin;
  private LocalDateTime createDate;
  private LocalDateTime updateDate;
  ```

---

### 5. ✅ User.status 型別錯誤
**錯誤**: `incompatible types: int cannot be converted to java.lang.String`

**原因**: User.status 在資料庫中是 VARCHAR，但程式碼設定為 int

**修復**:
- 檔案: `UserServiceImpl.java`
- 修改: `user.setStatus(1)` → `user.setStatus("ACTIVE")`

---

### 6. ✅ RoleMapper.selectById() 方法不存在
**錯誤**: `cannot find symbol: method selectById(java.lang.String)`

**原因**: RoleMapper 的方法名是 `selectByPrimaryKey()`

**修復**:
- 檔案: `JwtAuthenticationFilter.java`
- 修改: `roleMapper.selectById()` → `roleMapper.selectByPrimaryKey()`

---

## 關鍵發現

### User Entity 有兩個 ResultMap

1. **BaseResultMap** (MyBatis Generator 自動生成):
   - 使用 `id: BIGINT`
   - 使用 `createdAt`, `updatedAt: TIMESTAMP`
   - 包含所有資料表欄位

2. **UserMap** (手動定義，實際使用):
   - 使用 `id: String`
   - 使用 `createDate`, `updateDate: TIMESTAMP`
   - 包含簡化欄位 + `avatar`, `goldCoins`, `bonusCoins`

**結論**: 程式碼實際使用 UserMap，所以 User entity 必須符合 UserMap 的欄位定義。

---

## 修改的檔案

### 重建檔案
1. `entity/User.java` - 完全重建，刪除手動 getter/setter，新增缺失欄位

### 修改檔案
2. `security/UserPrincipal.java` - 新增 authorities, adminUser, user 欄位
3. `security/AdminJwtAuthenticationFilter.java` - 修正方法呼叫
4. `filter/JwtAuthenticationFilter.java` - 修正 RoleMapper 方法名
5. `service/impl/UserServiceImpl.java` - 修正 status 型別

---

## 驗證

執行命令:
```cmd
cd c:\Users\user\OneDrive\Desktop\創業\KUJI-Server\admin
mvn clean compile -DskipTests
```

**預期結果**: BUILD SUCCESS ✅

---

## 重要提醒

1. **資料庫結構**: 
   - `user` 表的 `id` 欄位應該是 VARCHAR 而非 BIGINT
   - `status` 欄位應該是 VARCHAR
   - 需要有 `avatar`, `gold_coins`, `bonus_coins`, `create_date`, `update_date` 欄位

2. **Lombok 使用**: 
   - 所有 Entity 都使用 @Data，不要手動寫 getter/setter
   - 手動寫的方法會覆蓋 Lombok 生成的，導致型別不一致

3. **命名一致性**:
   - JwtUtil 使用 `getUsername()` 而非 `extractUsername()`
   - RoleMapper 使用 `selectByPrimaryKey()` 而非 `selectById()`
   - 確保方法名在整個專案中保持一致

---

## 下一步

1. ✅ 編譯成功後，執行單元測試
2. 檢查資料庫結構是否與 Entity 一致
3. 測試 User 註冊/登入功能
4. 測試 Google OAuth2 登入
5. 測試抽獎功能
