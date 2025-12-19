# Generator 修改後修正工作報告

## 問題根源

修改 Generator 將所有 ID 欄位從 `Long` 改為 `String` (UUID) 後，導致整個專案出現約 100+ 編譯錯誤。這是因為：

1. Entity ID 欄位從 `Long` 變成 `String`
2. Mapper 方法參數從 `Long` 變成 `String`  
3. 舊的 Service 層仍使用 `Long` 型別
4. Controller 的 `@PathVariable` 仍使用 `Long`
5. DTO 的 ID 欄位仍是 `Long`

---

## 已完成修正

### Service 層
1. ✅ `PermissionService.java` - 所有 ID 參數改為 String
2. ✅ `PermissionServiceImpl.java` - 完全重寫，使用 Example 模式
3. ✅ `MenuService.java` - 所有 ID 參數改為 String
4. ✅ `MenuServiceImpl.java` - 完全重寫，使用 Example 模式
5. ✅ `RoleService.java` - 所有 ID 參數改為 String
6. ✅ `RoleServiceImpl.java` - 完全重寫，使用 Example 模式
7. ✅ `AdminUserService.java` - 所有 ID 參數改為 String
8. ✅ `AdminUserServiceImpl.java` - 完全重寫，使用 Example 模式
9. ✅ `AdminAuthService.java` - getCurrentUserId 改為返回 String
10. ✅ `AdminAuthServiceImpl.java` - 完全重寫，使用 Example 模式

### Entity 和 Mapper
1. ✅ `Role.java` - id: Long → String
2. ✅ `RoleMapper.java` - 所有 ID 參數改為 String
3. ✅ `RoleMapper.xml` - 修改 resultMap 和查詢

### Example 類別增強
1. ✅ `RoleExample.java` - andIdEqualTo(String), getOredCriteria()
2. ✅ `AdminUserRoleExample.java` - Criterion 模式
3. ✅ `StoreUserExample.java` - Criterion 模式
4. ✅ `RoleMenuExample.java` - Criterion 模式
5. ✅ `MenuExample.java` - Criterion 模式

### Mapper XML 更新
1. ✅ `AdminUserRoleMapper.xml` - Criterion 基礎查詢
2. ✅ `StoreUserMapper.xml` - Criterion 基礎查詢
3. ✅ `RoleMenuMapper.xml` - Criterion 基礎查詢
4. ✅ `MenuMapper.xml` - Criterion 基礎查詢
5. ✅ `RoleMapper.xml` - 完整 CRUD

### DTO 類別
1. ✅ `RoleRes.java` - id: Long → String
2. ✅ `RoleDetailRes.java` - 所有 ID: Long → String
3. ✅ `MenuRes.java` - id, parentId: Long → String
4. ✅ `MenuTreeRes.java` - id: Long → String
5. ✅ `RoleUpdateReq.java` - id: Long → String
6. ✅ `RoleMenuPermissionReq.java` - roleId, menuId: Long → String
7. ✅ `MenuCreateReq.java` - parentId: Long → String
8. ✅ `MenuUpdateReq.java` - id, parentId: Long → String
9. ✅ `CreateStoreEditorReq.java` - storeId: Long → String
10. ✅ `AdminUserRes.java` - id, RoleInfo.id, StoreInfo.id: Long → String
11. ✅ `LoginRes.UserInfo` - id: Long → String

### 工具類
1. ✅ `SecurityUtils.java` - getCurrentUserId 改為返回 String
2. ✅ `JwtUtil.java` - generateToken 和 getUserId 改為使用 String

### Controller
1. ✅ `RoleController.java` - PathVariable: Long → String
2. ✅ `AdminUserController.java` - PathVariable: Long → String
3. ✅ `PermissionController.java` - PathVariable: Long → String

---

## 尚需修正

### 高優先級 - Controller
1. ❌ `LotteryController.java` - PathVariable: Long → String
2. ❌ `AdminLotteryController.java` - PathVariable: Long → String, SecurityUtils.getCurrentUserId()
3. ❌ `LotteryDrawController.java` - PathVariable: Long → String, SecurityUtils.getCurrentUserId()

### 高優先級 - Service
1. ❌ `LotteryServiceImpl.java` - 需要完全重寫使用 Example 模式
   - 問題方法: selectById, selectByPage, countByCondition, update, incrementDrawCount, deleteByLotteryId
   - 問題方法: selectByLotteryId, sumQuantityByLotteryId, sumRemainingByLotteryId, countGrandPrizeRemaining, decrementRemaining
   
2. ❌ `DrawServiceImpl.java` - 需要完全重寫使用 Example 模式
   - 問題方法: selectById, selectAvailableByLotteryId, incrementDrawCount
   - 問題方法: sumRemainingByLotteryId, sumQuantityByLotteryId, decrementRemaining, countGrandPrizeRemaining
   - 問題方法: updateBalance

### 中優先級 - Security
1. ❌ `ApiJwtAuthenticationFilter.java` - 使用 Example 替代 selectByEmail

### 需要新增到 Mapper 的方法 (或使用 Example 替代)

根據 architecture-guide.prompt.md，應優先使用 Example 類別替代自定義 SQL。以下方法需要用 Example 重寫或添加到 Mapper:

#### UserMapper (前端用戶)
- selectById - 使用 selectByPrimaryKey
- selectByEmail - 使用 Example
- updateBalance - 需要自定義 SQL (原子操作)

#### LotteryMapper
- selectById - 使用 selectByPrimaryKey
- selectByPage - 使用 Example + 排序 + 分頁
- countByCondition - 使用 countByExample
- update - 使用 updateByPrimaryKey
- incrementDrawCount - 需要自定義 SQL (原子操作)

#### LotteryPrizeMapper
- selectByLotteryId - 使用 Example
- selectAvailableByLotteryId - 使用 Example (remaining > 0)
- sumQuantityByLotteryId - 需要聚合查詢 (自定義 SQL)
- sumRemainingByLotteryId - 需要聚合查詢 (自定義 SQL)
- countGrandPrizeRemaining - 需要聚合查詢 (自定義 SQL)
- decrementRemaining - 需要自定義 SQL (原子操作)
- deleteByLotteryId - 使用 Example

---

## 修正原則

### 遵循 architecture-guide.prompt.md 規則

1. **DDL 優先** - 所有 Entity ID 已是 VARCHAR(36) UUID 格式
2. **MBG 優先** - 使用 Generator 生成的基本 CRUD 方法
3. **Example 優先** - 條件查詢使用 Example 類別
4. **自定義 SQL 限制** - 僅用於:
   - JOIN 查詢
   - 聚合函數 (SUM, COUNT, AVG)
   - 原子更新操作 (餘額、庫存)

### 型別一致性

- 所有 Entity ID 欄位: String (UUID)
- 所有 Mapper 方法 ID 參數: String
- 所有 Service 方法 ID 參數: String
- 所有 Controller @PathVariable ID: String
- 所有 DTO ID 欄位: String

---

## 建議下一步

1. 修正剩餘的 Controller
2. 為需要聚合/原子操作的 Mapper 添加自定義方法
3. 重寫 LotteryServiceImpl 和 DrawServiceImpl 使用 Example
4. 重新執行 mvn compile 驗證
