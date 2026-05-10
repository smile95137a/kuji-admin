# 12 - 系統日誌 & 後端修正通知

> **文件目的**：本文件包含兩大部分：  
> 1. 🆕 **系統日誌 API**（全新，尚未有前端規格）  
> 2. ⚠️ **後端近期修正事項**（既有規格與實際實作的落差，前端需同步調整）  
>
> **最後更新**：2026-05-04  
> **允許角色**（系統日誌）：ADMIN Only

---

## 📋 目錄

- [第一部分：系統日誌 API](#第一部分系統日誌-api)
- [第二部分：後端修正通知](#第二部分後端修正通知)
- [第三部分：報表補全（5 個缺漏）](#第三部分報表補全5-個缺漏)
- [第四部分：疑問清單（需前後端確認）](#第四部分疑問清單需前後端確認)

---

## 第一部分：系統日誌 API

> **後台路由（頁面）**：`/admin/system/logs`  
> **後端 API Base**：`/admin/system-log`  
> **允許角色**：ADMIN Only  
> **選單位置**：系統設定 → 系統日誌

系統日誌分為兩種類型：

| 類型 | 說明 | 資料表 |
|------|------|--------|
| `LOGIN` | 所有後台與前台的登入/登出記錄 | `log_auth` |
| `ADMIN_ACTION` | 後台管理員的操作記錄（新增/修改/刪除） | `log_admin_action` |

---

### API 1：依類型查詢日誌（通用介面）

```
GET /api/admin/system-log/type/{type}?limit=200
Authorization: Bearer {token}（需 ADMIN）
```

| 參數 | 位置 | 說明 |
|------|------|------|
| `type` | Path | `LOGIN` 或 `ADMIN_ACTION`（大小寫不敏感） |
| `limit` | Query | 最多返回幾筆，預設 200，建議 50~500 |

**使用範例**：
```
GET /api/admin/system-log/type/LOGIN?limit=100
GET /api/admin/system-log/type/ADMIN_ACTION?limit=50
```

---

### API 2：登入日誌（快捷路徑）

```
GET /api/admin/system-log/login?limit=200
Authorization: Bearer {token}（需 ADMIN）
```

#### 回應資料結構

```typescript
interface LogAuth {
  id: string;
  userId: string | null;          // 登入成功時有值；失敗可能 null
  userType: string;               // 'admin' 或 'user'（前台/後台）
  email: string;                  // 嘗試登入的 Email
  loginMethod: string;            // 'PASSWORD' / 'GOOGLE' / 'LINE'
  result: string;                 // 'SUCCESS' / 'FAILED' / 'BLOCKED'
  errorMessage: string | null;    // 失敗時的錯誤說明
  ip: string;                     // 登入 IP
  userAgent: string;              // 瀏覽器 User Agent
  createdAt: string;              // ISO 8601 時間戳
}
```

#### 前端頁面建議

| 欄位 | 顯示說明 |
|------|---------|
| `createdAt` | 日期時間（格式化為 YYYY-MM-DD HH:mm:ss） |
| `email` | 登入帳號 |
| `userType` | 後台/前台（admin = 後台、user = 前台） |
| `loginMethod` | 登入方式（密碼/Google/LINE） |
| `result` | 狀態標籤：SUCCESS（綠）、FAILED（紅）、BLOCKED（橘） |
| `ip` | 來源 IP |
| `errorMessage` | hover tooltip 顯示失敗原因 |

---

### API 3：後台操作日誌（快捷路徑）

```
GET /api/admin/system-log/admin-action?limit=200
Authorization: Bearer {token}（需 ADMIN）
```

#### 回應資料結構

```typescript
interface LogAdminAction {
  id: string;
  adminId: string;                // 操作者的管理員 ID
  adminEmail: string;             // 操作者 Email
  adminRole: string;              // 操作者角色（ROLE_ADMIN / ROLE_STORE_OWNER）
  targetType: string;             // 操作對象類型（STORE / LOTTERY / USER / ORDER 等）
  targetId: string | null;        // 操作對象的 UUID
  targetName: string | null;      // 操作對象的名稱（方便閱讀）
  action: string;                 // 操作動詞（CREATE / UPDATE / DELETE / ON_SHELF 等）
  beforeSnapshot: string | null;  // 操作前的 JSON 快照（可 JSON.parse 後顯示）
  afterSnapshot: string | null;   // 操作後的 JSON 快照
  result: string;                 // 'SUCCESS' / 'FAILED'
  errorMessage: string | null;    // 失敗原因
  ip: string;                     // 操作來源 IP
  createdAt: string;
}
```

#### `targetType` 常見枚舉值

| 值 | 對應功能 |
|-----|---------|
| `STORE` | 店家管理 |
| `LOTTERY` | 商品管理 |
| `USER` | 前台玩家管理 |
| `ADMIN_USER` | 後台帳號管理 |
| `ORDER` | 訂單管理 |
| `BANNER` | Banner 管理 |
| `RECHARGE_PLAN` | 儲值方案管理 |
| `SYSTEM_CONFIG` | 系統參數管理 |

#### 前端頁面建議

| 欄位 | 顯示說明 |
|------|---------|
| `createdAt` | 操作時間 |
| `adminEmail` | 操作人員 |
| `adminRole` | 角色 |
| `targetType` + `targetName` | 操作對象（如：「商品 - 鬼滅之刃一番賞」） |
| `action` | 操作行為（CREATE/UPDATE/DELETE 等） |
| `result` | 成功/失敗標籤 |
| `beforeSnapshot` / `afterSnapshot` | 點擊「查看詳情」展開 JSON diff |
| `ip` | 操作 IP |

#### JSON Snapshot 顯示建議

```typescript
// beforeSnapshot 和 afterSnapshot 都是 JSON 字串
// 可用 diff 套件（如 jsondiffpatch）顯示差異
const before = JSON.parse(log.beforeSnapshot ?? '{}');
const after = JSON.parse(log.afterSnapshot ?? '{}');
// 顯示差異 highlight
```

---

### 系統日誌前端頁面設計建議

```
┌─────────────────────────────────────────────┐
│  系統日誌                          [Tab: 登入日誌 | 操作日誌]  │
├─────────────────────────────────────────────┤
│  篩選：[日期範圍選擇器] [Result 狀態篩選] [搜尋 Email]        │
│  ⚠️ 目前 API 無篩選，limit 最大 500，前端自行 filter          │
├─────────────────────────────────────────────┤
│  時間         | Email    | 類型 | 動作 | 狀態 | IP  | 詳情 │
│  2026-05-04   | admin@   | 後台 | 密碼 | ✅   | ... | 👁  │
└─────────────────────────────────────────────┘
```

> ⚠️ **重要提醒**：目前後端系統日誌 API **沒有篩選功能**，只有 `limit` 參數控制筆數。  
> 前端收到資料後，需要**在前端做篩選**（按日期/Email/Result 過濾）。  
> 若未來需要伺服器端篩選，請告知後端補充 condition 條件。

---

## 第二部分：後端修正通知

> 以下是後端最近修正的事項，部分與現有文件有落差，**前端需要同步調整**。

---

### ✅ 修正 1：登入回應欄位確認（01-auth.md 需確認）

`01-auth.md` 記載 `user.mustChangePassword`，後端實際欄位名稱請以 API 實際回傳為準。

⚠️ **前端務必在登入成功後檢查此欄位**：

```typescript
const loginRes = await adminLogin({ username, password });

if (loginRes.user.mustChangePassword === true) {
  // ✅ 強制跳轉改密碼頁面，不可讓使用者進入其他頁面
  router.push('/admin/change-password/first');
  return;
}

// 正常進入 Dashboard
router.push('/admin/dashboard');
```

**強制改密碼 API**：
```
POST /api/admin/auth/first-login/change-password
Authorization: Bearer {accessToken}
```

```typescript
interface ChangePasswordReq {
  oldPassword: string;   // 初始密碼（Admin 建立時發送到信箱的密碼）
  newPassword: string;   // 新密碼（至少 8 字元）
  confirmPassword: string;
}
```

---

### ✅ 修正 2：商品刪除 API 規格更新（05-product-management.md）

**舊文件**（錯誤描述）：
```
DELETE /api/admin/lottery/{id}
// 只有 DRAFT 狀態可刪除
```

**現在實際**：`DELETE` 動詞仍然正確，但後端接受更多狀態。  
刪除規則已收斂為：只有 `DRAFT`、`OFF_SHELF` 可直接刪除（刪除後狀態變為 `DELETED`，資料不物理刪除）。`WAITING_ON_SHELF`、`FORCED_OFF`、`GRAND_PRIZE_DRAWN`、`ALL_DRAWN` 都必須先回到可操作狀態。

```typescript
// 前端刪除確認對話框文字建議
const statusLabel = {
  DRAFT: '草稿商品',
  CONFIGURED: '已設定商品',
  OFF_SHELF: '已下架商品',
  FORCED_OFF: '強制下架商品',
};
// ON_SHELF / COMPLETED 無法刪除，後端會返回 400
```

---

### ✅ 修正 3：角色代碼（code）唯讀（02-roles-and-permissions.md）

後端已移除角色代碼的更新邏輯，**角色代碼建立後不可修改**。

**前端影響**：編輯角色頁面，`code` 欄位應設為 **disabled / 唯讀**：

```html
<!-- ❌ 錯誤：允許編輯 code -->
<el-input v-model="role.code" />

<!-- ✅ 正確：code 唯讀 -->
<el-input v-model="role.code" disabled />
<!-- 或顯示為純文字 -->
<span class="text-gray-500">{{ role.code }}</span>
```

---

### ✅ 修正 4：儲值方案 - 活動時間設定

儲值方案新增/更新 API 支援活動時間段，**前端需要加入日期時間選擇器**。

**完整請求結構**：

```
POST /api/admin/recharge-plan
PUT  /api/admin/recharge-plan/{id}
Authorization: Bearer {token}（需 ADMIN）
```

```typescript
interface RechargePlanCreateReq {
  name: string;           // 方案名稱（必填）
  description?: string;  // 描述
  amount: number;         // 儲值金額（台幣，必填）
  goldCoins: number;      // 發放金幣數（必填）
  bonusCoins?: number;    // 贈送紅利（選填，預設 0）
  displayOrder?: number;  // 排序（數字越小越前面）
  startTime?: string;     // 活動開始時間（ISO 8601，null = 無限制）
  endTime?: string;       // 活動結束時間（ISO 8601，null = 無限制）
}
```

**回應結構**：

```typescript
interface RechargePlanRes {
  id: string;
  name: string;
  description: string;
  amount: number;         // 台幣
  goldCoins: number;
  bonusCoins: number;
  displayOrder: number;
  isActive: boolean;
  startTime: string | null;   // 活動開始時間
  endTime: string | null;     // 活動結束時間
  isPromotional: boolean;     // true = 有設定活動時間
  isInPeriod: boolean;        // true = 目前在活動期間內（後端計算）
  createdAt: string;
}
```

**前端 UI 建議**：

```
┌──────────────────────────────────────────┐
│  方案名稱：[___________]                  │
│  儲值金額（台幣）：[________]             │
│  發放金幣：[________]                     │
│  贈送紅利：[________]                     │
│                                           │
│  ☑ 設定活動時間（優惠期限）               │
│  開始時間：[DateTimePicker ___________]   │
│  結束時間：[DateTimePicker ___________]   │
│                                           │
│  > isPromotional=true 時顯示「限時優惠」標籤 │
│  > isInPeriod=true 時標籤變為「進行中」    │
└──────────────────────────────────────────┘
```

---

### ✅ 修正 5：建立帳號後系統自動發送初始密碼信

當 Admin 建立 StoreOwner 或 StoreEditor 帳號，後端會自動寄出初始密碼到對方信箱，**前端不需要額外處理**，只需在 UI 提示：

```typescript
// 建立帳號成功後的提示文字建議
const successMessage = '帳號建立成功！初始密碼已發送至 ${email}，對方首次登入後需強制更換密碼。';
```

重設密碼（Admin 手動觸發）同樣會寄信：
```
POST /api/admin/admin-user/{id}/reset-password
Authorization: Bearer {token}（需 ADMIN）
```
> 後端產生新隨機密碼 → 寄信 → 對方下次登入強制改密

---

## 第三部分：報表補全（5 個缺漏）

> `08-report-analytics.md` 只記載了 `revenue`、`lottery-result`、`referral`、`recharge` 共 4 個報表。  
> 後端實際還有 **5 個**報表 API，前端規格未記載，補充如下。

---

### 報表 5：商品銷售排行

```
POST /api/admin/report/lottery-sales
Authorization: Bearer {token}（需 ADMIN 或 STORE_OWNER）
```

#### 請求

```typescript
interface LotterySalesRankingCondition {
  storeId?: string;   // StoreOwner 後端自動帶入；Admin 可選填篩選特定店家
  limit?: number;     // 返回幾筆，預設 20，最大 100
  // 繼承自 BaseCondition（暫無其他條件）
}
```

#### 回應

```typescript
interface LotterySalesRankingRes {
  totalRecords: number;       // 符合條件的商品總數（不受 limit 影響）
  items: {
    lotteryId: string;
    lotteryTitle: string;
    storeName: string;        // 所屬店家名稱
    drawCount: number;        // 已抽籤數（全生命期）
    revenue: number;          // 有效營收（金幣，排除 CANCELLED 訂單）
    rank: number;             // 排名（1-based）
  }[];
}
```

#### 前端 UI 建議
- 以 `revenue`（營收）降序顯示排行榜
- 顯示 Top 20 商品的排名、名稱、店家、已抽數、收入
- 可加長條圖呈現各商品收入對比

---

### 報表 6：店家績效比較

```
POST /api/admin/report/store-performance
Authorization: Bearer {token}（需 ADMIN 或 STORE_OWNER）
```

> ⚠️ **STORE_OWNER 只能查詢自己店家**，若傳其他 storeId 後端會返回 403。

#### 請求

```typescript
interface StorePerformanceCondition {
  storeId?: string;       // Admin 可選；StoreOwner 後端強制帶入自己的
  startDate?: string;     // YYYY-MM-DD
  endDate?: string;       // YYYY-MM-DD
}
```

#### 回應（請參照 Swagger 確認最終欄位）

```typescript
interface StorePerformanceReportRes {
  // 請確認後端 Swagger /api/swagger-ui.html 的實際欄位
  // 基本欄位如：storeId, storeName, totalRevenue, totalOrders, drawCount
}
```

> ⚠️ 此報表的回應 DTO（`StorePerformanceReportRes.java`）欄位較多，建議直接  
> 查看 Swagger：`http://localhost:8080/api/swagger-ui.html`

---

### 報表 7：獎品出貨報表

```
POST /api/admin/report/prize-shipment
Authorization: Bearer {token}（需 ADMIN 或 STORE_OWNER）
```

#### 請求

```typescript
interface PrizeShipmentReportCondition {
  storeId?: string;       // StoreOwner 後端自動帶入
  startDate?: string;     // YYYY-MM-DD（預設：今日 - 29 天）
  endDate?: string;       // YYYY-MM-DD（預設：今日）
}
```

#### 回應

```typescript
interface PrizeShipmentReportRes {
  startDate: string;          // 實際查詢起始日（YYYY-MM-DD）
  endDate: string;            // 實際查詢結束日

  // 訂單狀態計數
  pendingCount: number;       // 待處理
  preparingCount: number;     // 備貨中
  shippedCount: number;       // 已出貨
  completedCount: number;     // 已完成

  // 時效指標
  avgShipDays: number | null; // 平均出貨天數（preparing → shipped），null = 無資料
  overdueCount: number;       // 超過 7 天仍在 PENDING 的訂單數（即時數字，不受日期篩選）

  // 每日出貨明細
  dailyDetails: {
    date: string;             // YYYY-MM-DD
    shippedCount: number;     // 當日出貨數
  }[];

  // 跨店家統計（Admin 限定；StoreOwner 查詢時為 null）
  storeDetails: {
    storeId: string;
    storeName: string;
    pendingCount: number;
    preparingCount: number;
    shippedCount: number;
    completedCount: number;
    avgShipDays: number | null;
    overdueCount: number;
  }[] | null;
}
```

#### 前端 UI 建議
- 頂部顯示 4 個狀態卡片（待處理/備貨中/已出貨/已完成）
- `overdueCount > 0` 時顯示紅色警示
- 折線圖顯示每日出貨趨勢
- Admin 額外顯示各店家比較表格

---

### 報表 8：贈送點數報表（ADMIN + STORE_OWNER）

```
POST /api/admin/report/bonus
Authorization: Bearer {token}（需 ADMIN 或 STORE_OWNER）
```

#### 請求

```typescript
interface BonusReportCondition {
  storeId?: string;
  startDate?: string;   // YYYY-MM-DD
  endDate?: string;     // YYYY-MM-DD
}
```

#### 回應

```typescript
interface BonusReportRes {
  startDate: string;
  endDate: string;
  totalBonusPoints: number;   // 期間總贈送點數
  totalCount: number;         // 總贈送筆數
  benefitUsers: number;       // 受益會員數
  growthRate: number | null;  // 與上期比較（%），null = 無上期資料

  dailyDetails: {
    date: string;             // YYYY-MM-DD
    points: number;
    count: number;
  }[];

  typeStats: {                // 依贈送類型統計
    bonusType: string;        // 'REFERRAL' | 'PROMOTION' | 'ADJUSTMENT' | 'REGISTRATION'
    typeName: string;         // 顯示用中文名稱（後端提供）
    totalPoints: number;
    count: number;
    percentage: number;       // 占比（%）
  }[];
}
```

#### 贈送類型說明

| bonusType | 說明 |
|-----------|------|
| `REFERRAL` | 推薦碼獎勵 |
| `PROMOTION` | 活動促銷 |
| `ADJUSTMENT` | 人工調整（Admin 手動操作） |
| `REGISTRATION` | 新會員註冊獎勵 |

#### 前端 UI 建議
- 圓餅圖顯示各類型占比
- 折線圖顯示每日贈送趨勢

---

### 報表 9：會員成長報表（ADMIN Only）

```
POST /api/admin/report/member-growth
Authorization: Bearer {token}（需 ADMIN）
```

> ⚠️ **STORE_OWNER 無法使用此報表**，後端會返回 403。  
> 前端選單需根據角色隱藏此報表入口。

#### 請求

```typescript
interface MemberGrowthReportCondition {
  startDate?: string;   // YYYY-MM-DD（預設：今日 - 29 天）
  endDate?: string;     // YYYY-MM-DD（預設：今日）
}
```

#### 回應

```typescript
interface MemberGrowthReportRes {
  startDate: string;
  endDate: string;

  // 新增會員統計
  totalNewMembers: number;
  growthRate: number | null;        // 與上期比較（%）

  registrationByProvider: {         // 依登入方式分類
    [provider: string]: number;     // 如 { "PASSWORD": 120, "GOOGLE": 45 }
  };

  dailyNewMembers: {
    date: string;                   // YYYY-MM-DD
    count: number;
  }[];

  // 活躍度
  activeMembers: number;
  arpuGold: number;                 // 金幣 ARPU（每活躍用戶平均金幣消費）
  arpuBonus: number;                // 紅利 ARPU

  // 留存率（null = 資料不足無法計算）
  retention7Days: number | null;    // 7 天留存率（%）
  retention30Days: number | null;   // 30 天留存率（%）
}
```

#### 前端 UI 建議
- 頂部指標卡：新增會員數、活躍會員、ARPU、留存率
- 折線圖：每日新增會員趨勢
- 長條圖：依 registrationByProvider 顯示各登入方式分佈

---

### 9 個報表選單路由對應表（前端路由用）

| 報表 | 後端 API 路徑 | 前端頁面路由 | ADMIN | STORE_OWNER |
|------|-------------|------------|-------|-------------|
| 營業額報表 | `POST /admin/report/revenue` | `/admin/reports/revenue` | ✅ | ✅ |
| 開獎結果 | `POST /admin/report/lottery-result` | `/admin/reports/lottery-result` | ✅ | ✅ |
| 推薦碼報表 | `POST /admin/report/referral` | `/admin/reports/referral` | ✅ | ✅ |
| 儲值報表 | `POST /admin/report/recharge` | `/admin/reports/recharge` | ✅ | ✅ |
| **商品銷售排行** | `POST /admin/report/lottery-sales` | `/admin/reports/lottery-sales` | ✅ | ✅ |
| **店家績效** | `POST /admin/report/store-performance` | `/admin/reports/store-performance` | ✅ | ✅（自己） |
| **獎品出貨報表** | `POST /admin/report/prize-shipment` | `/admin/reports/prize-shipment` | ✅ | ✅ |
| **贈送點數報表** | `POST /admin/report/bonus` | `/admin/reports/bonus` | ✅ | ✅ |
| **會員成長報表** | `POST /admin/report/member-growth` | `/admin/reports/member-growth` | ✅ | ❌（403） |

> ⚠️ **會員成長報表**只有 ADMIN 有選單，後端只授權 ADMIN，STORE_OWNER 觸發會 403。

---

## 第四部分：已確認狀態 & 前端注意事項

---

### ✅ AOP 系統日誌已完整實作

`AuditLogAspect.java` 已實作並啟用，以下操作**已有** `@AuditLog` 標記，執行後自動寫入 `log_admin_action`：

| Controller | 已記錄的操作 |
|------------|------------|
| AdminAuthController | 登入（EMAIL 方式） |
| AdminLotteryController | 商品 CREATE / UPDATE / ON_SHELF / OFF_SHELF |
| AdminStoreController | 店家 CREATE / UPDATE / UPDATE_STATUS |
| AdminUserController | 帳號 CREATE / ENABLE / DISABLE / RESET_PASSWORD / DELETE |

> 前端直接呼叫查詢 API 即可看到完整記錄。

---

### ✅ 全部 9 個報表已有真實 SQL 實作

`ReportServiceImpl.java` 使用 `JdbcTemplate` 執行真實 SQL 查詢，不是 stub。  
所有 9 個報表方法（包含 lottery-sales、store-performance、prize-shipment、bonus、member-growth）均已完整實作，前端直接對接即可。

---

### ❓ 疑問 1：系統日誌是否需要伺服器端篩選？

**現況**：目前系統日誌 API 只有 `limit` 參數，前端只能拿最新 N 筆，然後在前端用 JS 篩選。

**問題**：如果日誌累積到幾千筆，前端篩選效能差，且無法搜尋歷史資料。

**建議確認**：是否要後端加入 `startDate`、`endDate`、`email`、`result` 等條件？或是否需要分頁？

**目前臨時方案**（前端）：
```typescript
// 拿最新 500 筆，前端 filter
const logs = await getLoginLogs({ limit: 500 });
const filtered = logs.filter(log =>
  log.email.includes(searchEmail) &&
  log.result === selectedResult
);
```

---

### ❓ 疑問 2：選單樹中的報表子選單是否已正確初始化？

**現況**：後端 `DataInitializer` 已補充 9 個報表子選單，但需要**重啟服務**讓 rescue 方法跑一次。

**確認步驟**：
```bash
# 重啟後端後呼叫此 API，確認 menus 裡有 9 個報表子選單
POST /api/admin/auth/login
# 在回傳的 menus 中找到「報表分析」，確認有 9 個 children
```

若選單不足，表示 rescue 方法未正確執行，需查看後端啟動 log。

---

### ❓ 疑問 3：儲值方案頁面目前是否已有前端實作？

**現況**：`frontend/admin/` 目錄中**沒有儲值方案的規格文件**。

**確認**：前端是否已實作儲值方案管理頁面？若未實作，需補充以下 API 規格：
- `POST /admin/recharge-plan` — 新增
- `PUT /admin/recharge-plan/{id}` — 更新
- `GET /admin/recharge-plan/{id}` — 查詢
- `DELETE /admin/recharge-plan/{id}` — 刪除（軟刪除）
- `POST /admin/recharge-plan/list` — 查詢列表

---

### ❓ 疑問 4：商品定時上架的前端入口在哪裡？

**現況**：商品建立時有 `scheduledAt` 欄位（ISO 8601 時間），但前端 `05-product-management.md` 未說明這個功能的 UI 入口。

**建議確認**：
- 定時上架是否在「新增商品」頁面提供「選擇上架時間」DateTimePicker？
- 設定後狀態會是什麼？（`DRAFT` 還是 `SCHEDULED`？）

---

*文件結束。如有問題請與後端確認後更新本文件。*
