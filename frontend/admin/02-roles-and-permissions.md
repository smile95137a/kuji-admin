# 02 - 角色與權限

## 後台角色定義

| 角色代碼 | 顯示名稱 | 功能範疇 |
|---------|---------|---------|
| `ROLE_ADMIN` | 系統管理員 | 全系統所有功能 |
| `ROLE_STORE_OWNER` | 店家負責人 | 自己店家的商品、訂單、報表 |
| `ROLE_STORE_EDITOR` | 店家編輯 | 商品管理、部分訂單（無報表、無帳號管理） |

---

## 功能權限矩陣

| 功能模組 | ADMIN | STORE_OWNER | STORE_EDITOR |
|---------|-------|-------------|--------------|
| 登入/改密碼 | ✅ | ✅ | ✅ |
| 查看自己的店家資料 | ✅ | ✅ | ✅ |
| **新增/刪除店家** | ✅ | ❌ | ❌ |
| 編輯店家資訊 | ✅（所有） | ✅（自己的） | ❌ |
| 停用/啟用店家 | ✅ | ❌ | ❌ |
| **建立 StoreOwner 帳號** | ✅ | ❌ | ❌ |
| **建立 StoreEditor 帳號** | ✅ | ❌ | ❌ |
| 修改帳號狀態 | ✅ | ❌ | ❌ |
| **商品列表查詢** | ✅（所有店家） | ✅（自己店家） | ✅（自己店家） |
| 新增/編輯商品 | ✅ | ✅ | ✅ |
| **刪除商品** | ✅ | ✅ | ❌ |
| **上架/下架商品** | ✅ | ✅ | ❌ |
| 訂單查詢 | ✅（所有） | ✅（自己店家） | ✅（自己店家） |
| 訂單狀態更新/出貨 | ✅ | ✅ | ✅ |
| **取消訂單** | ✅ | ✅ | ❌ |
| **報表查詢** | ✅（所有） | ✅（自己店家） | ❌ |
| Banner/新聞/跑馬燈 | ✅ | ❌ | ❌ |
| 前台玩家管理 | ✅ | ❌ | ❌ |
| 推薦碼管理 | ✅ | ❌ | ❌ |
| 系統參數 | ✅ | ❌ | ❌ |

---

## 前端選單動態渲染

登入後，API 回傳 `menus` 樹（詳見 [01-auth.md](./01-auth.md)），前端根據此樹**動態生成側邊選單與路由**。

### 選單節點結構

```typescript
interface MenuNode {
  id: string;
  name: string;       // 顯示名稱
  path: string;       // 對應 Vue Router path
  icon: string;       // 圖示 class 或名稱
  orderNum: number;   // 排序（小的在前）
  children: MenuNode[];
  permissions: {
    canView: boolean;   // 可否進入此頁面
    canEdit: boolean;   // 頁面內可否操作編輯按鈕
    canDelete: boolean; // 頁面內可否操作刪除按鈕
  };
}
```

### 動態路由建立範例（Vue Router）

```javascript
// 登入成功後建立動態路由
function buildRoutes(menus) {
  return menus.flatMap(menu => {
    const routes = [];
    if (menu.permissions.canView) {
      routes.push({
        path: menu.path,
        component: () => import(`@/views/${menu.path}.vue`),
        meta: { 
          title: menu.name, 
          permissions: menu.permissions 
        }
      });
    }
    if (menu.children?.length) {
      routes.push(...buildRoutes(menu.children));
    }
    return routes;
  });
}
```

### 按鈕級權限控制

頁面內編輯/刪除按鈕，根據 `permissions` 控制顯示：

```vue
<template>
  <!-- 只有 canEdit = true 才顯示編輯按鈕 -->
  <el-button v-if="canEdit" @click="handleEdit">編輯</el-button>
  <el-button v-if="canDelete" type="danger" @click="handleDelete">刪除</el-button>
</template>

<script setup>
const route = useRoute();
const permissions = route.meta.permissions;
const canEdit = permissions?.canEdit ?? false;
const canDelete = permissions?.canDelete ?? false;
</script>
```

---

## 資料隔離規則

### StoreOwner / StoreEditor

- 查詢商品、訂單時**不需傳 `storeId`**，後端自動從 JWT 解析並過濾
- 如果帳號沒有關聯任何店家，API 會傳回空列表（不報錯）

### Admin

- 查詢所有資料時不受店家限制，天然可看全部
- **新增商品時必須明確傳 `storeId`**（因為 Admin 沒有預設店家）

### 選單查詢 API

```
GET /api/admin/menu/user-menus
Authorization: Bearer {token}
```

回傳當前使用者有權限的選單樹，格式同 `LoginRes.menus`。  
**建議**：登入時從 `LoginRes.menus` 直接讀取，不需另外呼叫此 API。

---

## 角色選項 API（建立帳號時使用）

```
GET /api/admin/roles
Authorization: Bearer {token}（需 ADMIN 角色）
```

### 回應
```typescript
interface RoleRes {
  id: string;
  name: string;    // 如 "系統管理員"
  code: string;    // 如 "ROLE_ADMIN"
  description: string;
}
```
