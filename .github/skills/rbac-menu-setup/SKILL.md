# RBAC 角色與選單設定 Skill

## 適用情境
- 新增選單項目（功能頁面）
- 新增角色或調整角色所能看的選單
- 修改選單的 can_view / can_edit / can_delete 權限
- 了解後台權限架構（Admin / StoreOwner / StoreEditor）

---

## 核心資料表

```
role                        → 角色定義（ROLE_ADMIN / ROLE_STORE_OWNER / ROLE_STORE_EDITOR）
menu                        → 功能選單（支援父子層級）
role_menu                   → 角色 × 選單 × 權限（三者綁定）
admin_user_role             → 帳號 × 角色（多對多）
```

---

## 角色速查

| 角色 code | 說明 |
|-----------|------|
| `ROLE_ADMIN` | 最高權限，可操作所有功能 |
| `ROLE_STORE_OWNER` | 店家主帳號，可管理自己店家 |
| `ROLE_STORE_EDITOR` | 店家小編，受限操作 |

> ⚠️ 角色代碼必須含 `ROLE_` 前綴（Spring Security 要求）。

---

## 選單結構（階層式）

```
選單（parent_id=null）           → 頂層選單（如：商品管理）
  └── 子選單（parent_id=頂層ID）  → 功能頁面（如：商品列表）
```

### 選單欄位

```java
menu.setId(UUID.randomUUID().toString());
menu.setMenuName("商品管理");           // 顯示名稱
menu.setMenuPath("/lottery");           // 前端路由路徑
menu.setParentId(null);                 // null 表示頂層
menu.setOrderNum(1);                    // 排序（數字越小越前）
menu.setIcon("icon-lottery");           // 圖示
menu.setIsVisible(true);                // 是否顯示於選單
menu.setCreatedAt(LocalDateTime.now());
```

---

## 新增選單步驟

### Step 1：DataInitializer 中新增選單

```java
// config/DataInitializer.java 的 initMenus() 方法
private void initMenus() {
    // 已有選單不重複建立（冪等）
    MenuExample check = new MenuExample();
    check.createCriteria().andMenuPathEqualTo("/new-feature");
    if (!menuMapper.selectByExample(check).isEmpty()) return;

    Menu menu = new Menu();
    menu.setId(UUID.randomUUID().toString());
    menu.setMenuName("新功能");
    menu.setMenuPath("/new-feature");
    menu.setParentId(existingParentMenuId);  // 父選單 ID
    menu.setOrderNum(10);
    menu.setIsVisible(true);
    menu.setCreatedAt(LocalDateTime.now());
    menuMapper.insert(menu);
}
```

### Step 2：新增 RoleMenu 權限關聯

```java
// 為 ROLE_ADMIN 開放所有權限
private void bindMenuToRole(String roleId, String menuId, boolean canView, boolean canEdit, boolean canDelete) {
    RoleMenu roleMenu = new RoleMenu();
    roleMenu.setId(UUID.randomUUID().toString());
    roleMenu.setRoleId(roleId);
    roleMenu.setMenuId(menuId);
    roleMenu.setCanView(canView ? 1 : 0);
    roleMenu.setCanEdit(canEdit ? 1 : 0);
    roleMenu.setCanDelete(canDelete ? 1 : 0);
    roleMenu.setCreatedAt(LocalDateTime.now());
    roleMenuMapper.insert(roleMenu);
}

// 使用範例
bindMenuToRole(ROLE_ADMIN_ID, newMenuId, true, true, true);
bindMenuToRole(ROLE_STORE_OWNER_ID, newMenuId, true, false, false);
// STORE_EDITOR 若不需要，不建立 RoleMenu
```

---

## 新增角色步驟

> ⚠️ 通常不需要新增角色。如確實需要（如新增 ROLE_FINANCE），請按以下步驟。

### Step 1：DataInitializer 建立 Role

```java
Role role = new Role();
role.setId(UUID.randomUUID().toString());
role.setName("財務人員");
role.setCode("ROLE_FINANCE");          // 必須含 ROLE_ 前綴
role.setDescription("負責財務報表查閱");
role.setCreatedAt(LocalDateTime.now());
roleMapper.insert(role);
```

### Step 2：在 SecurityConfig 新增允許路由

```java
// SecurityConfig.java 中後台 hasAnyRole 加入新角色
.requestMatchers("/admin/**").hasAnyRole("ADMIN", "STORE_OWNER", "STORE_EDITOR", "FINANCE")
//                                                                                 ↑ 新增
```

### Step 3：AdminJwtAuthenticationFilter 查詢角色邏輯不需修改

Filter 自動從 `admin_user_role` → `role` 表讀取角色 code，組成 `ROLE_FINANCE` 設入 SecurityContext。

---

## 查詢當前使用者選單

```java
// service/MenuService.java
// 根據使用者角色取得選單樹

List<String> roleIds = getUserRoleIds(userId);

// 取得有 can_view=1 的 menuIds
RoleMenuExample roleMenuExample = new RoleMenuExample();
roleMenuExample.createCriteria()
    .andRoleIdIn(roleIds)
    .andCanViewEqualTo(1);
List<RoleMenu> roleMenus = roleMenuMapper.selectByExample(roleMenuExample);

List<String> menuIds = roleMenus.stream()
    .map(RoleMenu::getMenuId)
    .distinct()
    .collect(Collectors.toList());

// 查詢選單並組織為樹狀結構
// 頂層選單（parentId=null）+ 子選單（parentId=頂層ID）
```

---

## 現有 19 個選單（DataInitializer 初始化）

```
頂層選單（7個）：
  商品管理 / 店家管理 / 帳號管理 / 報表統計 / 系統設定 / 選單管理 / 日誌管理

子選單（12個）：
  商品列表 / 商品新增 / 店家列表 / 店家新增 / 帳號列表 / 帳號新增
  / 摘要統計 / 日結報表 / 角色管理 / 選單設定 / 系統日誌 / 操作日誌
```

---

## 權限檢查範例

```java
// PermissionServiceImpl.java
public boolean canEditMenu(String userId, String menuPath) {
    // 1. 取得使用者角色
    List<String> roleIds = getUserRoleIds(userId);
    // 2. 查詢 menu by path
    Menu menu = getMenuByPath(menuPath);
    // 3. 查詢 role_menu 是否有 can_edit=1
    RoleMenuExample example = new RoleMenuExample();
    example.createCriteria()
        .andRoleIdIn(roleIds)
        .andMenuIdEqualTo(menu.getId())
        .andCanEditEqualTo(1);
    return !roleMenuMapper.selectByExample(example).isEmpty();
}
```

---

## ⚠️ 禁止操作

- ❌ 角色 code 不含 `ROLE_` 前綴（Spring Security 會自動加，資料庫存完整前綴才對）
- ❌ StoreEditor 的選單權限不可超過 StoreOwner
- ❌ 新增選單後不要忘記同時建立 RoleMenu 關聯
- ❌ 不要讓 StoreOwner / StoreEditor 查看 `can_delete=1` 的財務或管理功能
- ❌ 不要直接修改已有的角色 code（影響 Filter 中的角色解析）
