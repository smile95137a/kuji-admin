# 取得使用者選單清單 API 使用指南

## ✅ 確認：後端已提供此功能！

您的後端系統已經實作了透過 **JWT Token** 自動取得當前使用者的後台選單清單的 API。

## 🔒 安全性改進

**重要更新：** API 已改為從 JWT Token 自動取得 userId，**前端不需要也不應該傳遞 userId**。

這樣設計的優點：
- ✅ 防止使用者冒充他人身份
- ✅ 提高資安防護等級
- ✅ 簡化前端呼叫流程
- ✅ Token 驗證自動完成

---

## 📋 API 資訊

### **端點 (Endpoint)**
```
GET /admin/menus/accessible
```

### **功能說明**
根據 JWT Token 中的使用者身份和角色權限，返回該使用者可以訪問的選單樹狀結構（用於前端動態渲染側邊欄選單）。

### **權限邏輯**
1. **Admin 角色**：返回所有可見選單（isVisible = true）
2. **其他角色**：只返回該角色有 `canView` 權限的選單

---

## 🔧 使用方式

### **1. 請求範例**

```http
GET /admin/menus/accessible
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**不需要任何參數！**

**Headers:**
- `Authorization`: Bearer Token（必須）

---

### **2. 成功回應範例**

**HTTP Status Code: 200**

```json
{
  "success": true,
  "message": "查詢成功",
  "data": [
    {
      "id": "menu-001",
      "code": "PRODUCT_MANAGEMENT",
      "name": "商品管理",
      "path": "/products",
      "icon": "icon-product",
      "parentId": null,
      "sortOrder": 1,
      "children": [
        {
          "id": "menu-002",
          "code": "PRODUCT_LIST",
          "name": "商品列表",
          "path": "/products/list",
          "icon": "icon-list",
          "parentId": "menu-001",
          "sortOrder": 1,
          "children": []
        },
        {
          "id": "menu-003",
          "code": "PRODUCT_CREATE",
          "name": "新增商品",
          "path": "/products/create",
          "icon": "icon-add",
          "parentId": "menu-001",
          "sortOrder": 2,
          "children": []
        }
      ]
    },
    {
      "id": "menu-004",
      "code": "ORDER_MANAGEMENT",
      "name": "訂單管理",
      "path": "/orders",
      "icon": "icon-order",
      "parentId": null,
      "sortOrder": 2,
      "children": []
    }
  ]
}
```

---

## 💻 前端使用流程

### **完整流程**

```javascript
// 1. 使用者登入
const loginResponse = await fetch('http://localhost:8080/admin/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'admin@kuji.com',
    password: 'Admin123'
  })
});

const loginData = await loginResponse.json();
const token = loginData.data.accessToken;

// 儲存 Token（建議使用 HttpOnly Cookie 或 secure storage）
localStorage.setItem('accessToken', token);

// 2. 取得使用者選單（不需要傳 userId！）
const menuResponse = await fetch('http://localhost:8080/admin/menus/accessible', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const menuData = await menuResponse.json();
// menuData.data -> 這是選單樹狀結構
```

---

## 🎯 前端實作範例

### **React 範例**

```jsx
import { useEffect, useState } from 'react';

function DynamicMenu() {
  const [menuTree, setMenuTree] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchUserMenu();
  }, []);

  const fetchUserMenu = async () => {
    try {
      // 只需要從 localStorage 取得 token
      const token = localStorage.getItem('accessToken');

      const response = await fetch(
        'http://localhost:8080/admin/menus/accessible',
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );

      const data = await response.json();
      
      if (data.success) {
        setMenuTree(data.data);
      } else if (response.status === 401) {
        // Token 無效或過期，導向登入頁
        window.location.href = '/login';
      }
    } catch (error) {
      console.error('取得選單失敗:', error);
    } finally {
      setLoading(false);
    }
  };

  const renderMenuItem = (menu) => (
    <li key={menu.id}>
      <a href={menu.path}>
        <i className={menu.icon}></i>
        {menu.name}
      </a>
      {menu.children && menu.children.length > 0 && (
        <ul>
          {menu.children.map(renderMenuItem)}
        </ul>
      )}
    </li>
  );

  if (loading) return <div>載入中...</div>;

  return (
    <nav>
      <ul>
        {menuTree.map(renderMenuItem)}
      </ul>
    </nav>
  );
}

export default DynamicMenu;
```

---

### **Vue 3 範例**

```vue
<template>
  <nav v-if="!loading">
    <ul>
      <MenuItem 
        v-for="menu in menuTree" 
        :key="menu.id" 
        :menu="menu" 
      />
    </ul>
  </nav>
  <div v-else>載入中...</div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import MenuItem from './MenuItem.vue';

const router = useRouter();
const menuTree = ref([]);
const loading = ref(true);

const fetchUserMenu = async () => {
  try {
    const token = localStorage.getItem('accessToken');

    const response = await fetch(
      'http://localhost:8080/admin/menus/accessible',
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );

    if (response.status === 401) {
      // Token 無效，導向登入頁
      router.push('/login');
      return;
    }

    const data = await response.json();
    
    if (data.success) {
      menuTree.value = data.data;
    }
  } catch (error) {
    console.error('取得選單失敗:', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchUserMenu();
});
</script>
```

---

## 📊 回應欄位說明

| 欄位 | 類型 | 說明 |
|------|------|------|
| `id` | String | 選單ID (UUID) |
| `code` | String | 選單代碼（唯一識別碼） |
| `name` | String | 選單顯示名稱 |
| `path` | String | 路由路徑 |
| `icon` | String | 圖示 class 名稱 |
| `parentId` | String | 父選單ID（null 表示頂層選單） |
| `sortOrder` | Integer | 排序順序 |
| `children` | Array | 子選單陣列（遞迴結構） |

---

## ⚠️ 錯誤處理

### **401 Unauthorized**
```json
{
  "success": false,
  "message": "未認證或 Token 無效",
  "data": null
}
```
**處理方式：** 導向登入頁面

### **403 Forbidden**
```json
{
  "success": false,
  "message": "無權限執行此操作",
  "data": null
}
```
**處理方式：** 顯示權限不足訊息

---

## 🔍 測試方式

### **使用 Postman**

1. **登入取得 Token**
   - POST `http://localhost:8080/admin/auth/login`
   - Body: `{ "username": "admin@kuji.com", "password": "Admin123" }`
   - 複製回應中的 `data.accessToken`

2. **取得選單**
   - GET `http://localhost:8080/admin/menus/accessible`
   - Headers: `Authorization: Bearer {accessToken}`
   - **不需要傳遞 userId**

### **使用 cURL**

```bash
# 1. 登入
curl -X POST http://localhost:8080/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"Admin123"}'

# 2. 取得選單（替換 {token}）
curl -X GET http://localhost:8080/admin/menus/accessible \
  -H "Authorization: Bearer {token}"
```

---

## � 安全性說明

### **為什麼從 JWT 取得 userId 更安全？**

1. **防止身份冒充**
   - ❌ 舊方式：前端可以傳任意 userId，可能冒充其他使用者
   - ✅ 新方式：userId 從 JWT 解析，無法偽造

2. **Token 驗證**
   - Token 必須是有效且未過期的
   - Token 簽名驗證確保未被篡改
   - 後端自動驗證 Token 並提取 userId

3. **簡化邏輯**
   - 前端不需要管理 userId
   - 減少參數傳遞錯誤
   - 統一的認證機制

---

## �📝 重要提醒

1. **必須先登入**：此 API 需要有效的 Bearer Token
2. **不要傳遞 userId**：系統會自動從 JWT Token 中解析
3. **Token 有效期**：預設 86400 秒（24小時），過期需重新登入
4. **動態渲染**：前端應根據回傳的選單樹動態渲染側邊欄
5. **權限控制**：後端已根據角色權限過濾選單，前端只需顯示

---

## 🆕 其他權限 API

除了選單 API，系統還提供以下權限檢查 API（同樣從 JWT 自動取得 userId）：

### **檢查選單權限**
```http
GET /admin/permissions/check/{menuCode}
Authorization: Bearer {token}
```

### **檢查是否為 Admin**
```http
GET /admin/permissions/is-admin
Authorization: Bearer {token}
```

### **查詢使用者角色**
```http
GET /admin/permissions/roles
Authorization: Bearer {token}
```

### **查詢可訪問店鋪**
```http
GET /admin/permissions/accessible-stores
Authorization: Bearer {token}
```

---

## 📚 相關文檔

- 完整 API 參考：`FRONTEND_API_REFERENCE.json`
- Controller 實作：`src/main/java/com/group/admin/controller/MenuController.java`
- Service 實作：`src/main/java/com/group/admin/service/impl/MenuServiceImpl.java`
- 安全工具類：`src/main/java/com/group/admin/util/SecurityUtils.java`

---

## ✅ 結論

**您的後端已經實作了高安全性的選單 API！**

API 路徑：`GET /admin/menus/accessible`

**關鍵改進：**
- ✅ 不需要前端傳遞 userId
- ✅ 自動從 JWT Token 解析使用者身份
- ✅ 防止身份冒充攻擊
- ✅ 簡化前端呼叫流程
- ✅ 提高整體安全性

前端只需要在 Header 中帶上 `Authorization: Bearer {token}`，後端會自動處理身份驗證和權限檢查！
