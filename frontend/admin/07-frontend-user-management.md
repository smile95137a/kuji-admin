# 07 - 前台玩家管理

> **路由前綴**：`/admin/frontend-users`  
> **允許角色**：ADMIN（全部操作）

---

## API 列表

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/admin/frontend-users/list` | 查詢玩家列表 |
| GET | `/admin/frontend-users/{id}` | 取得玩家詳情 |
| PUT | `/admin/frontend-users/{id}` | 更新玩家資訊 |
| POST | `/admin/frontend-users/{id}/activate` | 啟用玩家帳號 |
| POST | `/admin/frontend-users/{id}/deactivate` | 停用玩家帳號 |
| POST | `/admin/frontend-users/{id}/suspend` | 暫停玩家帳號 |

---

## 查詢玩家列表

```
POST /api/admin/frontend-users/list
Authorization: Bearer {token}（需 ADMIN）
```

### 請求
```typescript
interface QueryReq<FrontendUserCondition> {
  condition?: {
    keyword?: string;         // 暱稱/Email 模糊搜尋
    status?: string;          // ACTIVE / INACTIVE / SUSPENDED
    createdAtStart?: string;
    createdAtEnd?: string;
    hasReferralCode?: boolean; // 篩選有推薦碼的玩家
  };
  sortBy?: string;
  sortOrder?: 'ASC' | 'DESC';
}
```

### 回應
```typescript
interface FrontendUserRes {
  id: string;
  email: string;
  nickname: string;
  avatarUrl: string;
  provider: 'EMAIL' | 'GOOGLE';  // 註冊來源
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  goldCoins: number;             // 金幣餘額
  bonusCoins: number;            // 紅利點數餘額
  referralCode: string | null;   // 此玩家的推薦碼
  referredByCode: string | null; // 被哪個推薦碼邀請
  totalOrders: number;           // 累計訂單數
  totalSpent: number;            // 累計消費金幣
  createdAt: string;
  lastLoginAt: string | null;
}
```

---

## 更新玩家資訊

```
PUT /api/admin/frontend-users/{id}
Authorization: Bearer {token}（需 ADMIN）
```

```typescript
interface FrontendUserUpdateReq {
  nickname?: string;
  status?: string;
  remark?: string;    // 後台備註
}
```

⚠️ **不可修改**：email、provider、金幣餘額（金幣相關請透過錢包 API 操作）

---

## 帳號狀態管理

### 帳號狀態說明

| 狀態 | 說明 | 可登入 | 可抽獎 |
|------|------|--------|--------|
| `ACTIVE` | 正常使用 | ✅ | ✅ |
| `INACTIVE` | 停用 | ❌ | ❌ |
| `SUSPENDED` | 暫停（違規處理中） | ❌ | ❌ |

### 啟用/停用/暫停

```
POST /api/admin/frontend-users/{id}/activate
POST /api/admin/frontend-users/{id}/deactivate
POST /api/admin/frontend-users/{id}/suspend
Authorization: Bearer {token}（需 ADMIN）
```

---

## 玩家錢包管理

```
GET /api/admin/wallet/{userId}
Authorization: Bearer {token}（需 ADMIN）
```

### 回應
```typescript
interface WalletRes {
  userId: string;
  goldBalance: number;         // 金幣（儲值）
  bonusBalance: number;        // 紅利（贈送）
  totalRecharge: number;       // 累計儲值金額
  transactions: WalletTx[];    // 最近交易記錄
}
```

---

## 玩家獎品盒（Admin 查詢）

```
GET /api/admin/prize-box/{userId}
Authorization: Bearer {token}（需 ADMIN）
```

```
GET /api/admin/prize-box/summary/{userId}
Authorization: Bearer {token}（需 ADMIN）
```

可查詢玩家的獎品盒內容（已抽到但尚未申請出貨的獎品），`/summary` 路徑按店家分組顯示。

---

## 前端 UI 建議

### 玩家列表頁
- 支援 Email + 暱稱搜尋
- 顯示金幣餘額、紅利餘額、累計消費
- 狀態 Badge（啟用/停用/暫停）
- 快速操作：停用/啟用

### 玩家詳情頁
- 基本資訊（Email、暱稱、頭像、註冊來源）
- 帳號狀態管理
- 錢包資訊（金幣/紅利）
- 訂單記錄
- 推薦碼資訊
- 獎品盒狀態
