# 08 - 推薦碼系統

> **路由前綴**：`/referral`（均需 Authorization Token）

---

## API 列表

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/referral/generate` | 產生推薦碼 |
| GET | `/referral/validate?code=XXX` | 驗證推薦碼是否有效 |
| POST | `/referral/apply` | 套用推薦碼（登入後一次性操作） |
| GET | `/referral/stats` | 取得推薦統計 |
| POST | `/referral/{id}/disable` | 停用自己的推薦碼 |

---

## 產生推薦碼

```
POST /api/referral/generate
Authorization: Bearer {token}
```

> 每個玩家只能有一組有效推薦碼。若已有推薦碼，回傳現有的。

### 回應
```typescript
interface ReferralCodeRes {
  id: string;
  code: string;           // 如 "KUJI-XXXX"
  userId: string;
  usedCount: number;      // 已被使用次數
  maxUsage: number | null; // null = 無上限
  isActive: boolean;
  expiresAt: string | null;
  createdAt: string;
}
```

---

## 驗證推薦碼

```
GET /api/referral/validate?code=KUJI-XXXX
Authorization: Bearer {token}
```

### 回應
```typescript
interface ReferralValidateRes {
  valid: boolean;
  code: string;
  reason: string | null;   // 無效時的原因說明
  // 有效時附帶推薦人資訊
  referrerNickname?: string;
  bonusForReferrer?: number;   // 推薦人獲得紅利
  bonusForNew?: number;        // 被推薦人獲得紅利
}
```

**無效原因**（`valid=false` 時）：
- `CODE_NOT_FOUND`：推薦碼不存在
- `CODE_DISABLED`：已被停用
- `SELF_REFERRAL`：不能使用自己的推薦碼
- `ALREADY_USED`：已使用過推薦碼（每人只能使用一次）
- `CODE_EXPIRED`：推薦碼已過期

---

## 套用推薦碼

```
POST /api/referral/apply
Authorization: Bearer {token}
```

```typescript
interface ReferralApplyReq {
  code: string;
}
```

> ⚠️ **每位玩家終生只能套用一次推薦碼**，套用後無法撤銷。

### 回應
```typescript
interface ReferralApplyRes {
  success: boolean;
  bonusEarned: number;         // 玩家獲得的紅利
  referrerBonusEarned: number; // 推薦人獲得的紅利
  newBonusBalance: number;     // 玩家套用後的紅利餘額
}
```

---

## 取得推薦統計

```
GET /api/referral/stats
Authorization: Bearer {token}
```

### 回應
```typescript
interface ReferralStatsRes {
  code: ReferralCodeRes | null;    // 自己的推薦碼（若無則 null）
  totalReferred: number;           // 共推薦幾人
  totalBonusEarned: number;        // 共賺取紅利點數
  referredUsers: {
    nickname: string;
    createdAt: string;             // 被推薦時間
  }[];
}
```

---

## 停用推薦碼

```
POST /api/referral/{id}/disable
Authorization: Bearer {token}
```

> 停用後推薦碼無法再被使用，但已積累的紀錄保留。

---

## 推薦碼使用時機

```
建議流程：
  1. 玩家註冊後，引導前往個人頁面「產生推薦碼」
  2. 新玩家輸入推薦碼：
     a. 可在「設定」或「個人中心」輸入
     b. 也支援 URL 參數 ?ref=CODE（在登入頁或導覽中自動帶入）
  3. 登入後立即驗證推薦碼 → 確認後套用
  4. 雙方紅利即時到帳
```

---

## 前端 UI 建議

### 個人中心 - 推薦頁籤
- 顯示自己的推薦碼（大號字體 + 複製按鈕）
- 顯示推薦統計：已推薦 N 人 / 共賺 N 紅利
- 「分享」按鈕（系統分享或複製 URL）

### 套用推薦碼
- 可在特定頁面顯示輸入框
- 即時驗證（輸入後 debounce 500ms 呼叫 `/validate`）
- 顯示推薦人暱稱 + 雙方可得紅利預覽
- 確認套用後呼叫 `/apply`
- 成功後顯示「您已獲得 X 紅利」
