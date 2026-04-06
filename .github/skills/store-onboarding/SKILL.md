# 店家開通流程 Skill

## 適用情境
- Admin 新增店家（包含建立 Owner 帳號）
- 了解 Store + AdminUser + StoreUser 三者關聯
- 修改店家停用連動邏輯
- 新增 StoreEditor 帳號

---

## 資料表關聯

```
admin_user（帳號）
    │
    │ 多對多 via admin_user_role
    ▼
role（角色：ROLE_ADMIN / ROLE_STORE_OWNER / ROLE_STORE_EDITOR）

admin_user（帳號）
    │
    │ 多對多 via store_user
    ▼
store（店家）

store（店家）
    │
    │ 一對多
    ▼
lottery（商品）
```

---

## 建立店家完整流程（含 Owner）

> ⚠️ Store + AdminUser + StoreUser 必須在 `@Transactional` 中完成，任何步驟失敗需全部回滾。

```java
@Transactional
public AdminUserRes createStoreOwner(CreateStoreOwnerReq req) {

    // Step 1：Email 唯一性驗證
    AdminUserExample emailCheck = new AdminUserExample();
    emailCheck.createCriteria().andEmailEqualTo(req.getEmail());
    if (!adminUserMapper.selectByExample(emailCheck).isEmpty()) {
        throw new BusinessException("Email 已被使用");
    }

    // Step 2：生成初始密碼
    String initialPassword = passwordUtil.generateRandomPassword();

    // Step 3：建立 AdminUser
    AdminUser adminUser = new AdminUser();
    adminUser.setId(UUID.randomUUID().toString());
    adminUser.setUsername(req.getEmail());
    adminUser.setEmail(req.getEmail());
    adminUser.setPassword(passwordEncoder.encode(initialPassword));
    adminUser.setDisplayName(req.getDisplayName());
    adminUser.setPhone(req.getPhone());
    adminUser.setStatus(AdminUserStatus.PENDING.getCode()); // 首次登入前為 PENDING
    adminUser.setForceChangePassword(true);                  // 強制改密碼
    adminUser.setCreatedBy(currentAdminUserId);
    adminUser.setCreatedAt(LocalDateTime.now());
    adminUserMapper.insert(adminUser);

    // Step 4：綁定 ROLE_STORE_OWNER 角色
    RoleExample roleExample = new RoleExample();
    roleExample.createCriteria().andCodeEqualTo(RoleCode.ROLE_STORE_OWNER.getCode());
    Role role = roleMapper.selectByExample(roleExample).get(0);

    AdminUserRole userRole = new AdminUserRole();
    userRole.setId(UUID.randomUUID().toString());
    userRole.setAdminUserId(adminUser.getId());
    userRole.setRoleId(role.getId());
    userRole.setCreatedAt(LocalDateTime.now());
    adminUserRoleMapper.insert(userRole);

    // Step 5：建立 Store
    Store store = new Store();
    store.setId(UUID.randomUUID().toString());
    store.setOwnerId(adminUser.getId());
    store.setStoreName(req.getStoreName());
    store.setShortDescription(req.getShortDescription());
    store.setLogoUrl(req.getLogoUrl());
    store.setEmail(req.getStoreEmail());
    store.setPhone(req.getStorePhone());
    store.setAddress(req.getStoreAddress());
    store.setStatus(StoreStatus.ACTIVE.getCode());
    store.setCreatedAt(LocalDateTime.now());
    storeMapper.insert(store);

    // Step 6：建立 StoreUser 關聯（Owner）
    StoreUser storeUser = new StoreUser();
    storeUser.setId(UUID.randomUUID().toString());
    storeUser.setStoreId(store.getId());
    storeUser.setAdminUserId(adminUser.getId());
    storeUser.setRoleType(StoreUserRoleType.OWNER.getCode()); // "OWNER"
    storeUser.setCreatedAt(LocalDateTime.now());
    storeUserMapper.insert(storeUser);

    // Step 7：回傳（含初始密碼，顯示一次）
    AdminUserRes res = toRes(adminUser);
    res.setInitialPassword(initialPassword);
    return res;
}
```

---

## 新增 StoreEditor 帳號

```java
// 與建立 Owner 幾乎相同，差異在：
// 1. 角色改為 ROLE_STORE_EDITOR
// 2. StoreUser.roleType = "EDITOR"
// 3. 需指定 storeId（Editor 必須附屬於某個 Store）
// 4. 由 ADMIN 或 StoreOwner 建立

@Transactional
public AdminUserRes createStoreEditor(CreateStoreEditorReq req) {
    // req.getStoreId() - 必填
    // 驗證 storeId 存在且 status=ACTIVE
    Store store = storeMapper.selectByPrimaryKey(req.getStoreId());
    if (store == null || !"ACTIVE".equals(store.getStatus())) {
        throw new BusinessException("店家不存在或已停用");
    }

    // ... 建立 AdminUser（同上）...

    // 綁定 ROLE_STORE_EDITOR 角色
    bindRole(adminUser.getId(), RoleCode.ROLE_STORE_EDITOR);

    // StoreUser 關聯（Editor）
    StoreUser storeUser = ...;
    storeUser.setRoleType(StoreUserRoleType.EDITOR.getCode()); // "EDITOR"
}
```

---

## 停用店家連動邏輯

```java
@Transactional
public void deactivateStore(String storeId, String operatorId) {
    Store store = storeMapper.selectByPrimaryKey(storeId);
    if (store == null) throw new BusinessException("店家不存在");

    // 1. 停用店家
    store.setStatus(StoreStatus.INACTIVE.getCode());
    store.setUpdatedAt(LocalDateTime.now());
    store.setUpdatedBy(operatorId);
    storeMapper.updateByPrimaryKey(store);

    // 2. 下架所有商品
    LotteryExample lotteryExample = new LotteryExample();
    lotteryExample.createCriteria().andStoreIdEqualTo(storeId);
    List<Lottery> lotteries = lotteryMapper.selectByExample(lotteryExample);

    for (Lottery lottery : lotteries) {
        lottery.setStatus("OFF_SHELF");
        lottery.setUpdatedAt(LocalDateTime.now());
        lotteryMapper.updateByPrimaryKey(lottery);
    }

    log.info("✅ 店家已停用，下架商品數：{}", lotteries.size());
}
```

---

## AdminUserStatus 狀態說明

| 狀態 | 說明 |
|------|------|
| `PENDING` | 建立後尚未首次登入 |
| `ACTIVE` | 正常使用中 |
| `INACTIVE` | 已停用（Admin 操作） |

---

## StoreUser roleType 對照

| roleType | 說明 |
|----------|------|
| `OWNER` | 店家主帳號 |
| `EDITOR` | 店家小編 |

---

## 權限矩陣

| 操作 | ADMIN | STORE_OWNER | STORE_EDITOR |
|------|-------|-------------|-------------|
| 建立 StoreOwner | ✅ | ❌ | ❌ |
| 建立 StoreEditor | ✅ | ❌ | ❌ |
| 停用帳號 | ✅ | ❌ | ❌ |
| 停用店家 | ✅ | ❌ | ❌ |
| 查詢所有店家 | ✅ | ❌（僅自己） | ❌（僅自己） |
| 編輯店家 | ✅ | ✅（僅自己） | ❌ |

---

## ⚠️ 禁止操作

- ❌ Store + AdminUser + StoreUser 建立必須在同一個 `@Transactional` 中
- ❌ 不要讓 StoreOwner / StoreEditor 自行建立帳號（只有 Admin 可以建）
- ❌ 停用店家時不要忘記下架所有商品
- ❌ 不要修改 `owner_id`（綁定後不可變更）
- ❌ 帳號啟用/停用操作僅允許 Admin 執行
