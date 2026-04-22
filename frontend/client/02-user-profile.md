# 02 - 個人資料管理

> **路由前綴**：`/user`（需 Authorization Token）

---

## API 列表

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/user/me` | 取得個人資訊（含餘額） |
| PUT | `/user/me` | 更新個人資訊 |
| POST | `/user/me/avatar` | 上傳頭像 |
| POST | `/user/me/change-password` | 修改密碼 |
| GET | `/user/addresses` | 取得所有收件地址 |
| GET | `/user/addresses/default` | 取得預設收件地址 |
| GET | `/user/addresses/{id}` | 取得地址詳情 |
| POST | `/user/addresses` | 新增收件地址 |
| PUT | `/user/addresses/{id}` | 更新收件地址 |
| PUT | `/user/addresses/{id}/default` | 設為預設地址 |
| DELETE | `/user/addresses/{id}` | 刪除收件地址 |

---

## 取得個人資訊

```
GET /api/user/me
Authorization: Bearer {token}
```

### 回應
```typescript
interface UserRes {
  id: string;
  email: string;
  nickname: string;
  avatarUrl: string | null;
  provider: 'EMAIL' | 'GOOGLE';
  status: string;
  goldCoins: number;         // 金幣餘額（即時）
  bonusCoins: number;        // 紅利餘額（即時）
  totalRecharge: number;     // 累計儲值金額（台幣）
  totalDraws: number;        // 累計抽獎次數
  referralCode: string | null;
  createdAt: string;
  lastLoginAt: string | null;
}
```

**前端建議**：進入抽獎頁前先呼叫此 API 確認餘額，避免抽獎失敗。

---

## 更新個人資訊

```
PUT /api/user/me
Authorization: Bearer {token}
```

```typescript
interface FrontendUserUpdateReq {
  nickname?: string;
  email?: string;    // 注意：Google 帳號不可修改 Email
}
```

⚠️ `provider=GOOGLE` 的帳號修改 Email 後端會拒絕（`400`）

---

## 上傳頭像

```
POST /api/user/me/avatar
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

```
file: File（圖片格式：JPG/PNG/WEBP，建議 < 2MB）
```

### 回應
```typescript
interface AvatarUploadRes {
  avatarUrl: string;   // 新的頭像 URL（S3）
}
```

---

## 修改密碼（Email 帳號專用）

```
POST /api/user/me/change-password
Authorization: Bearer {token}
```

```typescript
interface ChangePasswordReq {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}
```

⚠️ `provider=GOOGLE` 的帳號無法修改密碼。

---

## 收件地址管理

### 地址資料結構

```typescript
interface UserAddressRes {
  id: string;
  userId: string;
  recipientName: string;      // 收件人姓名
  recipientPhone: string;     // 收件人電話
  zipCode: string;            // 郵遞區號
  city: string;               // 縣市
  district: string;           // 行政區
  address: string;            // 詳細地址
  isDefault: boolean;         // 是否為預設地址
  createdAt: string;
}
```

### 新增收件地址

```
POST /api/user/addresses
Authorization: Bearer {token}
```

```typescript
interface UserAddressCreateReq {
  recipientName: string;      // 必填
  recipientPhone: string;     // 必填
  zipCode?: string;
  city: string;               // 必填
  district: string;           // 必填
  address: string;            // 必填，詳細地址
  isDefault?: boolean;        // 是否設為預設（預設 false）
}
```

### 設定預設地址

```
PUT /api/user/addresses/{id}/default
Authorization: Bearer {token}
```

（無須 body）後端自動將其他地址的 `isDefault` 設為 `false`。

---

## 前端 UI 建議

### 個人中心頁
- 顯示金幣 🪙 與紅利 💎 餘額（從 `GET /user/me` 取得）
- 頭像圓形裁切預覽後上傳
- Google 帳號登入者隱藏「修改密碼」入口

### 地址管理
- 列表顯示所有地址，預設地址置頂並標記
- 選擇預設地址時呼叫 `PUT /{id}/default`（即時更新）
- 出貨申請時自動帶入預設地址
