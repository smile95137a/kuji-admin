# 使用者頭像上傳 API 指南

## 📋 問題分析

### 原本的問題

`PUT /api/user/me` 只能接收 **avatar URL 字串**，不能直接上傳圖片檔案：

```json
// ❌ 這樣不行（無法上傳檔案）
PUT /api/user/me
Content-Type: application/json

{
  "avatar": "檔案內容"  // 錯誤！只能傳 URL
}
```

```json
// ✅ 原本只能這樣（先自己上傳到某處，再傳 URL）
PUT /api/user/me
Content-Type: application/json

{
  "avatar": "https://example.com/avatar.jpg"  // 正確，但要先自己上傳
}
```

---

## 🎯 解決方案

新增了 **2 個頭像上傳 API**，支援直接上傳圖片檔案到 S3：

### 方案 A：兩步驟（分開上傳和更新）

1. 先呼叫 `POST /api/user/avatar` 上傳圖片
2. 再呼叫 `PUT /api/user/me` 更新 avatar 欄位

### 方案 B：一步完成（推薦）✅

直接呼叫 `POST /api/user/avatar/update`，一次完成上傳+更新

---

## 📡 API 端點

### 1️⃣ POST /api/user/avatar（只上傳圖片）

**用途**：上傳頭像圖片到 S3，返回圖片 URL

**請求**：
```http
POST /api/user/avatar
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: multipart/form-data

file: [選擇圖片檔案]
```

**回應**：
```json
{
  "success": true,
  "data": {
    "imageUrl": "https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/user/550e8400-e29b-41d4-a716-446655440000.jpg"
  },
  "error": null,
  "meta": {
    "timestamp": "2026-02-02T...",
    "requestId": "..."
  }
}
```

**適用情境**：
- 前端需要預覽圖片後再決定是否更新
- 需要同時上傳多張圖片
- 需要先驗證圖片格式

**使用範例**：
```javascript
// 第 1 步：上傳圖片
const formData = new FormData();
formData.append('file', avatarFile);

const uploadRes = await axios.post('/api/user/avatar', formData, {
  headers: {
    'Content-Type': 'multipart/form-data',
    'Authorization': `Bearer ${token}`
  }
});

const imageUrl = uploadRes.data.data.imageUrl;

// 第 2 步：更新使用者資料
await axios.put('/api/user/me', {
  avatar: imageUrl
}, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

---

### 2️⃣ POST /api/user/avatar/update（推薦）✅

**用途**：上傳頭像到 S3 並自動更新使用者資料，一步完成

**請求**：
```http
POST /api/user/avatar/update
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: multipart/form-data

file: [選擇圖片檔案]
```

**回應**：
```json
{
  "success": true,
  "data": {
    "id": "user-uuid",
    "email": "user@example.com",
    "nickname": "使用者暱稱",
    "avatarUrl": "https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/user/550e8400-e29b-41d4-a716-446655440000.jpg",
    "goldCoins": 1000,
    "bonusCoins": 500,
    "phoneNumber": "0912345678",
    ...
  },
  "error": null,
  "meta": {
    "timestamp": "2026-02-02T...",
    "requestId": "..."
  }
}
```

**功能特點**：
- ✅ 上傳新頭像到 S3
- ✅ 自動更新使用者 avatar 欄位
- ✅ 自動刪除舊的 S3 頭像（節省空間）
- ✅ 返回完整的使用者資訊（含錢包餘額）

**適用情境**：
- 一般的頭像更新流程（推薦）
- 需要立即生效
- 不需要額外的確認步驟

**使用範例**：
```javascript
// 一步完成
const formData = new FormData();
formData.append('file', avatarFile);

const response = await axios.post('/api/user/avatar/update', formData, {
  headers: {
    'Content-Type': 'multipart/form-data',
    'Authorization': `Bearer ${token}`
  }
});

// 直接取得更新後的使用者資料
const user = response.data.data;
console.log('新頭像 URL:', user.avatarUrl);
```

---

### 3️⃣ PUT /api/user/me（原有的更新 API）

**用途**：更新使用者資料（暱稱、Email、頭像 URL 等）

**請求**：
```http
PUT /api/user/me
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "nickname": "新暱稱",
  "avatar": "https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/user/xxx.jpg",
  "phoneNumber": "0912345678",
  "city": "台北市",
  "district": "中正區",
  ...
}
```

**重點**：
- ⚠️ `avatar` 欄位只能傳 **URL 字串**，不能傳檔案
- ✅ 所有欄位都是**可選的**，只更新有傳的欄位
- ✅ 空字串 `""` 會被忽略，不會更新

---

## 🔧 前端整合範例

### React 範例（推薦方案）

```jsx
import React, { useState } from 'react';
import axios from 'axios';

function AvatarUpload() {
  const [uploading, setUploading] = useState(false);
  const [user, setUser] = useState(null);

  const handleUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setUploading(true);

    try {
      const formData = new FormData();
      formData.append('file', file);

      // ✅ 使用一步完成的 API
      const response = await axios.post('/api/user/avatar/update', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      // 更新使用者資料
      setUser(response.data.data);
      alert('頭像更新成功！');
    } catch (error) {
      console.error('上傳失敗:', error);
      alert('頭像更新失敗，請重試');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div>
      <input 
        type="file" 
        accept="image/*" 
        onChange={handleUpload}
        disabled={uploading}
      />
      {uploading && <p>上傳中...</p>}
      {user && <img src={user.avatarUrl} alt="Avatar" />}
    </div>
  );
}

export default AvatarUpload;
```

---

### Vue 3 範例

```vue
<template>
  <div>
    <input 
      type="file" 
      accept="image/*" 
      @change="handleUpload"
      :disabled="uploading"
    />
    <p v-if="uploading">上傳中...</p>
    <img v-if="user" :src="user.avatarUrl" alt="Avatar" />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

const uploading = ref(false);
const user = ref(null);

const handleUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  uploading.value = true;

  try {
    const formData = new FormData();
    formData.append('file', file);

    const response = await axios.post('/api/user/avatar/update', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });

    user.value = response.data.data;
    alert('頭像更新成功！');
  } catch (error) {
    console.error('上傳失敗:', error);
    alert('頭像更新失敗，請重試');
  } finally {
    uploading.value = false;
  }
};
</script>
```

---

### cURL 測試範例

```bash
# 測試上傳並更新頭像
curl -X POST http://18.179.187.129/api/user/avatar/update \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@avatar.jpg"

# 回應範例
{
  "success": true,
  "data": {
    "id": "...",
    "avatarUrl": "https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/user/xxx.jpg",
    "nickname": "測試使用者",
    "goldCoins": 1000,
    "bonusCoins": 500
  }
}
```

---

## 📊 API 比較表

| 特性 | POST /user/avatar | POST /user/avatar/update | PUT /user/me |
|------|------------------|-------------------------|--------------|
| **上傳檔案** | ✅ | ✅ | ❌（只能傳 URL） |
| **自動更新資料庫** | ❌ | ✅ | ✅ |
| **刪除舊頭像** | ❌ | ✅ | ❌ |
| **返回完整使用者資料** | ❌ | ✅ | ✅ |
| **需要認證** | ✅ | ✅ | ✅ |
| **適用情境** | 需要預覽 | 一般更新（推薦） | 只更新 URL |

---

## 🎯 建議使用流程

### 方案 A：快速更新（推薦）

```
使用者選擇圖片 
  → 呼叫 POST /api/user/avatar/update
  → 完成！顯示新頭像
```

**優點**：
- 只需一個 API 呼叫
- 自動刪除舊頭像
- 程式碼簡潔

---

### 方案 B：需要預覽

```
使用者選擇圖片 
  → 前端預覽圖片
  → 使用者確認
  → 呼叫 POST /api/user/avatar 上傳
  → 取得 URL
  → 呼叫 PUT /api/user/me 更新
  → 完成
```

**優點**：
- 可以先預覽
- 可以取消操作
- 彈性較高

---

## ⚠️ 注意事項

### 1. 圖片限制

- **檔案大小**：最大 5MB
- **格式**：jpg, png, gif, webp
- **建議尺寸**：300x300 px（會自動調整）

### 2. 認證要求

所有 API 都需要 JWT Token：

```http
Authorization: Bearer YOUR_JWT_TOKEN
```

如果沒有 Token，會返回 `401 Unauthorized`。

### 3. S3 URL 格式

上傳成功後的 URL 格式：

```
https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/user/{uuid}.{ext}
```

例如：
```
https://test-ourkuji.s3.ap-northeast-1.amazonaws.com/user/550e8400-e29b-41d4-a716-446655440000.jpg
```

### 4. 舊頭像處理

- 使用 `POST /api/user/avatar/update` 時，會**自動刪除**舊的 S3 頭像
- 只有 S3 URL（包含 `s3.amazonaws.com`）會被刪除
- 外部 URL（例如 Google 頭像）不會被刪除

---

## 🔍 錯誤處理

### 401 Unauthorized

**原因**：未登入或 Token 無效

**解決**：
```javascript
// 檢查 Token 是否存在
const token = localStorage.getItem('token');
if (!token) {
  // 導向登入頁
  window.location.href = '/login';
}
```

---

### 404 Not Found

**原因**：使用者不存在

**解決**：
- 檢查 JWT Token 中的 userId 是否正確
- 確認資料庫中有該使用者

---

### 500 Internal Server Error

**原因**：S3 上傳失敗或資料庫錯誤

**解決**：
- 檢查 AWS Credentials 設定
- 檢查 S3 Bucket 權限
- 查看後端日誌：`tail -f /home/ec2-user/logs/app.log`

---

## 📝 部署步驟

### 1. 編譯專案

```bash
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn clean package -DskipTests -Pprod
```

### 2. 上傳到 EC2

```bash
scp -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem target/admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/
```

### 3. 重啟服務

```bash
ssh -i C:\Users\user\OneDrive\Desktop\dream\ourkuji\ourkuji.pem ec2-user@18.179.187.129

# 停止舊服務
pkill -f admin-1.0.0.jar

# 啟動新服務
nohup java -jar -Dspring.profiles.active=prod admin-1.0.0.jar > /home/ec2-user/logs/app.log 2>&1 &

# 檢查狀態
tail -f /home/ec2-user/logs/app.log
```

---

## 🎉 完成！

現在前端可以直接上傳圖片檔案了！

**推薦使用**：`POST /api/user/avatar/update`（一步完成）

**測試 API**：
```bash
curl -X POST http://18.179.187.129/api/user/avatar/update \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@test-avatar.jpg"
```
