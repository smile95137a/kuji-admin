# 編譯錯誤修復總結

## 執行時間
2025-12-11

## 修復的主要問題

### 1. ✅ Lombok 註解缺失
**問題**: MetaInfo 和 ErrorInfo 缺少 `@NoArgsConstructor`，導致 builder() 方法無法使用

**修復**:
- `MetaInfo.java`: 新增 `@NoArgsConstructor`
- `ErrorInfo.java`: 新增 `@NoArgsConstructor`

### 2. ✅ AdminJwtAuthenticationFilter 檔案為空
**問題**: `AdminJwtAuthenticationFilter.java` 檔案完全是空的，導致 SecurityConfig 無法編譯

**修復**:
- 完整重新創建 `AdminJwtAuthenticationFilter.java`
- 實作了完整的 JWT 認證邏輯
- 支援後台管理員角色查詢
- 建立 UserPrincipal 並設定到 SecurityContext

### 3. ✅ User Entity 缺少 goldCoins 和 bonusCoins 欄位
**問題**: LotteryServiceImpl 和 UserServiceImpl 使用了 `getGoldCoins()` 和 `getBonusCoins()`，但 User entity 沒有這些欄位

**修復**:
- 在 `User.java` 中新增:
  ```java
  private Long goldCoins;
  private Long bonusCoins;
  ```

### 4. ✅ 所有 Mapper 介面缺少 @Mapper 註解
**問題**: 只有 UserMapper 和 AdminUserMapper 有 `@Mapper` 註解

**修復**: 為以下 Mapper 新增 `@Mapper` 註解:
- ✅ RoleMapper
- ✅ MenuMapper
- ✅ LotteryMapper
- ✅ LotteryPrizeMapper
- ✅ LotteryDrawRecordMapper
- ✅ AdminUserRoleMapper
- ✅ StoreMapper
- ✅ OrderMapper
- ✅ PointLogMapper

### 5. ✅ UserMapper 缺少 updateBalance 方法
**問題**: LotteryServiceImpl 呼叫了 `userMapper.updateBalance()`，但方法不存在

**修復**:
- 在 `UserMapper.java` 中新增:
  ```java
  int updateBalance(@Param("id") String id, 
                   @Param("goldDelta") Long goldDelta, 
                   @Param("bonusDelta") Long bonusDelta);
  ```
- 對應的 XML 實作已存在於 `UserMapper.xml`

### 6. ✅ UserMapper 和 AdminUserMapper 缺少自訂查詢方法
**問題**: 程式碼呼叫了 `selectByEmail()`, `selectById()`, `selectByUsername()`，但 Mapper 介面沒有定義

**修復**:
- `UserMapper.java`:
  ```java
  User selectById(@Param("id") String id);
  User selectByEmail(@Param("email") String email);
  ```
- `AdminUserMapper.java`:
  ```java
  AdminUser selectByUsername(@Param("username") String username);
  ```
- 對應的 XML 查詢已存在

## 檔案修改清單

### 新增/重建檔案
1. `src/main/java/com/group/admin/security/AdminJwtAuthenticationFilter.java` - 完整重新創建

### 修改檔案
1. `src/main/java/com/group/admin/entity/User.java` - 新增 goldCoins, bonusCoins
2. `src/main/java/com/group/admin/result/MetaInfo.java` - 新增 @NoArgsConstructor
3. `src/main/java/com/group/admin/result/ErrorInfo.java` - 新增 @NoArgsConstructor
4. `src/main/java/com/group/admin/mapper/UserMapper.java` - 新增方法
5. `src/main/java/com/group/admin/mapper/AdminUserMapper.java` - 新增 @Mapper 和方法
6. `src/main/java/com/group/admin/mapper/RoleMapper.java` - 新增 @Mapper
7. `src/main/java/com/group/admin/mapper/MenuMapper.java` - 新增 @Mapper
8. `src/main/java/com/group/admin/mapper/LotteryMapper.java` - 新增 @Mapper
9. `src/main/java/com/group/admin/mapper/LotteryPrizeMapper.java` - 新增 @Mapper
10. `src/main/java/com/group/admin/mapper/LotteryDrawRecordMapper.java` - 新增 @Mapper
11. `src/main/java/com/group/admin/mapper/AdminUserRoleMapper.java` - 新增 @Mapper
12. `src/main/java/com/group/admin/mapper/StoreMapper.java` - 新增 @Mapper
13. `src/main/java/com/group/admin/mapper/OrderMapper.java` - 新增 @Mapper
14. `src/main/java/com/group/admin/mapper/PointLogMapper.java` - 新增 @Mapper

## 預期結果

所有修復後，以下錯誤應該已解決:
- ❌ 100 個編譯錯誤 → ✅ 預期 0 個錯誤

### 主要錯誤類型修復:
1. ✅ `cannot find symbol: method builder()` - Lombok 註解已修復
2. ✅ `cannot find symbol: method getGoldCoins()` - User entity 已新增欄位
3. ✅ `cannot find symbol: method getBonusCoins()` - User entity 已新增欄位
4. ✅ `bad source file: AdminJwtAuthenticationFilter.java` - 檔案已重建
5. ✅ `cannot find symbol: method selectByEmail()` - Mapper 方法已新增
6. ✅ `cannot find symbol: method selectByUsername()` - Mapper 方法已新增
7. ✅ `cannot find symbol: method updateBalance()` - Mapper 方法已新增

## 驗證步驟

請執行以下命令驗證:
```cmd
cd c:\Users\user\OneDrive\Desktop\創業\KUJI-Server\admin
mvn clean compile -DskipTests
```

預期輸出應包含:
```
[INFO] BUILD SUCCESS
```

## 注意事項

1. **Lombok 依賴**: 確保 IDE 已安裝 Lombok 插件並啟用註解處理
2. **MyBatis XML**: 自訂查詢方法的 XML 實作已存在於 `UserMapper.xml` 和 `AdminUserMapper.xml`
3. **資料庫欄位**: User 表需要有 `gold_coins` 和 `bonus_coins` 欄位（根據 UserMapper.xml 的 resultMap）

## 下一步

修復完成後，建議:
1. ✅ 執行完整編譯測試
2. 執行單元測試: `mvn test`
3. 檢查 JWT 認證流程是否正常
4. 驗證 RBAC 權限系統
5. 測試抽獎功能的餘額扣除邏輯
