# 📋 完整 Enum 分類指南

> **最後更新**：2026-02-07  
> **系統**：KUJI Admin  
> **版本**：1.0.0

---

## 📖 快速查詢

### 遊戲相關 Enum
| Enum 名稱 | 用途 | 選項 |
|----------|------|------|
| **LotteryCategoryEnum** | 商品主分類 | OFFICIAL_ICHIBAN, GACHA, TRADING_CARD, CUSTOM_GACHA |
| **LotterySubCategoryEnum** | 遊戲模式 | LOTTERY_MODE, SCRATCH_MODE, SCRATCH_CARD_MODE |
| **LotteryStatusEnum** | 商品狀態 | DRAFT, OFF_SHELF, ON_SHELF, IN_PROGRESS, ENDED, FORCED_OFF |
| **PrizeLevel/PrizeLevelEnum** | 獎項等級 | A-G, LAST, GRAND |
| **PrizeTypeEnum** | 獎品類型 | PHYSICAL, DIGITAL, POINT |

### 訂單相關 Enum
| Enum 名稱 | 用途 | 選項 |
|----------|------|------|
| **OrderStatusEnum** | 訂單狀態 | PENDING, PREPARING, SHIPPED, COMPLETED, CANCELLED |
| **PaymentStatusEnum** | 支付狀態 | PENDING, SUCCESS, FAILED, CANCELLED |
| **ShippingMethodEnum** | 配送方式 | HOME_DELIVERY, SEVEN_ELEVEN, FAMILY_MART |
| **PrizeBoxStatusEnum** | 獎品盒狀態 | IN_BOX, SHIPPED, RECYCLED |

### 用戶相關 Enum
| Enum 名稱 | 用途 | 選項 |
|----------|------|------|
| **UserStatusEnum** | 會員狀態 | ACTIVE, INACTIVE, SUSPENDED, DELETED |
| **AdminUserStatus** | 後台管理者狀態 | PENDING, ACTIVE, INACTIVE |
| **AuthProviderEnum** | 登入方式 | LOCAL, GOOGLE, FACEBOOK, LINE |
| **RoleCode** | 系統角色 | ROLE_ADMIN, ROLE_STORE_OWNER, ROLE_STORE_EDITOR |
| **StoreUserRoleType** | 店家角色 | OWNER, EDITOR |
| **StoreStatus** | 店家狀態 | ACTIVE, INACTIVE |

### 點數相關 Enum
| Enum 名稱 | 用途 | 選項 |
|----------|------|------|
| **PointType** | 點數類型 | GOLD, BONUS |
| **PointOperationType** | 點數操作 | DEPOSIT, DEDUCT, DRAW, REFUND, BONUS_GRANT, BONUS_EXPIRE |
| **CoinTypeEnum** | 點數貨幣 | GOLD, BONUS |
| **TransactionTypeEnum** | 交易類型 | RECHARGE, DRAW, RECYCLE, REFUND, ADMIN_ADJUST |

### 審計相關 Enum
| Enum 名稱 | 用途 | 選項 |
|----------|------|------|
| **OperationType** | 操作類型 | CREATE, UPDATE, DELETE, LOGIN, LOGOUT, CHANGE_PASSWORD, ACTIVATE, DEACTIVATE |

---

## 🎮 遊戲相關 Enum 詳解

### 1️⃣ **LotteryCategoryEnum** - 商品主分類

```java
public enum LotteryCategoryEnum {
    OFFICIAL_ICHIBAN("OFFICIAL_ICHIBAN", "官方一番賞"),
    GACHA("GACHA", "扭蛋"),
    TRADING_CARD("TRADING_CARD", "卡牌"),
    CUSTOM_GACHA("CUSTOM_GACHA", "自製賞");
}
```

**使用情境**：
- 前台商品分類篩選
- 後台商品建立時選擇主分類
- API 查詢 `/api/lottery/browse` 時帶入 category 篩選

**常見組合**：
| 遊戲名稱 | Category | SubCategory | 說明 |
|--------|----------|------------|------|
| 官方一番賞 | OFFICIAL_ICHIBAN | LOTTERY_MODE | 官方授權，抽籤型 |
| 扭蛋 | GACHA | LOTTERY_MODE | 自購或授權商品，抽籤型 |
| 刮刮樂 | CUSTOM_GACHA | SCRATCH_MODE | 自製商品，刮獎型 |
| 刮刮卡 | CUSTOM_GACHA | SCRATCH_CARD_MODE | 自製商品，卡片式 |

**前端使用**：
```javascript
// 篩選官方一番賞
const filters = {
  category: 'OFFICIAL_ICHIBAN'
};

// 篩選所有扭蛋商品
const filters = {
  category: 'GACHA'
};
```

---

### 2️⃣ **LotterySubCategoryEnum** - 遊戲模式

```java
public enum LotterySubCategoryEnum {
    LOTTERY_MODE("LOTTERY_MODE", "抽籤型"),      // 傳統一番賞、扭蛋（抽籤）
    SCRATCH_MODE("SCRATCH_MODE", "刮刮樂型"),    // 刮獎卡（刮刮樂）
    SCRATCH_CARD_MODE("SCRATCH_CARD_MODE", "刮刮卡型");  // 卡片式刮獎
}
```

**使用情境**：
- 決定前端展示的遊戲介面（抽籤動畫 vs 刮獎動畫）
- 決定後台的獎品編排邏輯
- 決定用戶操作流程

**遊戲模式說明**：

| SubCategory | 操作方式 | 獎品展示 | 保護時間 | 備註 |
|------------|--------|--------|--------|------|
| **LOTTERY_MODE** | 點擊抽獎按鈕 | 抽籤動畫後揭曉 | 7 秒 | 傳統一番賞、扭蛋 |
| **SCRATCH_MODE** | 手指刮獎卡 | 刮開後逐個顯示 | 無 | 刮刮樂型商品 |
| **SCRATCH_CARD_MODE** | 選擇卡片點擊 | 點擊卡片翻牌 | 無 | 翻牌型遊戲 |

**前端使用**：
```javascript
if (subCategory === 'LOTTERY_MODE') {
  // 顯示抽籤動畫（7秒保護時間）
  showLotteryAnimation();
} else if (subCategory === 'SCRATCH_MODE') {
  // 顯示刮獎卡介面
  showScratchCard();
} else if (subCategory === 'SCRATCH_CARD_MODE') {
  // 顯示翻牌介面
  showFlipCard();
}
```

---

### 3️⃣ **LotteryStatusEnum** - 商品狀態

```java
public enum LotteryStatusEnum {
    DRAFT("DRAFT", "草稿"),
    OFF_SHELF("OFF_SHELF", "下架"),
    ON_SHELF("ON_SHELF", "上架"),
    IN_PROGRESS("IN_PROGRESS", "進行中"),
    ENDED("ENDED", "已結束"),
    FORCED_OFF("FORCED_OFF", "強制下架");
}
```

**狀態流轉圖**：
```
DRAFT 
  ↓
OFF_SHELF ← ON_SHELF → IN_PROGRESS → ENDED
  ↓            ↓
FORCED_OFF
```

**狀態說明**：
| 狀態 | 前台可見 | 可抽獎 | 說明 |
|-----|--------|-------|------|
| DRAFT | ❌ | ❌ | 編輯中，尚未發佈 |
| OFF_SHELF | ❌ | ❌ | 下架狀態 |
| ON_SHELF | ✅ | ✅ | 正常上架，可抽獎 |
| IN_PROGRESS | ✅ | ✅ | 進行中（已被用戶抽獎） |
| ENDED | ✅ | ❌ | 已結束，可瀏覽不可抽獎 |
| FORCED_OFF | ❌ | ❌ | 被強制下架（違反規則等） |

**前端使用**：
```javascript
// 判斷是否可以抽獎
const canDraw = status === 'ON_SHELF' || status === 'IN_PROGRESS';

// 判斷是否在前台顯示
const isVisible = ['ON_SHELF', 'IN_PROGRESS', 'ENDED'].includes(status);
```

---

### 4️⃣ **PrizeLevel / PrizeLevelEnum** - 獎項等級

```java
public enum PrizeLevel {
    A("A", "A賞", 1),
    B("B", "B賞", 2),
    C("C", "C賞", 3),
    D("D", "D賞", 4),
    E("E", "E賞", 5),
    F("F", "F賞", 6),
    G("G", "G賞", 7),
    LAST("LAST", "最後賞", 98),
    GRAND("GRAND", "大賞", 99);
}
```

**獎項等級說明**：

| 等級 | 代碼 | 排序 | 說明 |
|-----|-----|-----|------|
| A賞 | A | 1 | 最高等級常規獎 |
| B賞 | B | 2 | 次高等級 |
| C賞-G賞 | C-G | 3-7 | 中低等級 |
| **最後賞** | LAST | 98 | **特殊獎**：當剩餘最後一張時自動觸發 |
| **大賞** | GRAND | 99 | **特殊獎**：影響自動降價機制 |

**特殊獎項邏輯**：
- **LAST** (最後賞)：當商品只剩最後一張票券時，無論設定哪個獎品，用戶抽到的都是 LAST 級別
- **GRAND** (大賞)：影響後台自動降價機制，可設定特殊的價格降低規則

**前端使用**：
```javascript
// 根據等級顯示獎項卡片顏色
const getLevelColor = (level) => {
  if (level === 'GRAND') return '#FFD700'; // 金色（大賞）
  if (level === 'LAST') return '#FF6347';  // 番茄紅（最後賞）
  if (['A', 'B'].includes(level)) return '#87CEEB'; // 天藍（高級）
  return '#C0C0C0'; // 銀色（普通）
};
```

---

### 5️⃣ **PrizeTypeEnum** - 獎品類型

```java
public enum PrizeTypeEnum {
    PHYSICAL("PHYSICAL", "實體獎品"),
    DIGITAL("DIGITAL", "數位獎品"),
    POINT("POINT", "點數獎品");
}
```

**獎品類型說明**：

| 類型 | 領取方式 | 舉例 | 需要配送 |
|-----|--------|------|---------|
| **PHYSICAL** | 物流配送 | 玩具、卡牌、周邊 | ✅ 是 |
| **DIGITAL** | 直接發送 | 遊戲點數、虛擬貨幣、數位內容 | ❌ 否 |
| **POINT** | 點數帳戶 | 儲值金、紅利金、簽到積分 | ❌ 否 |

**前端使用**：
```javascript
// 根據獎品類型顯示不同的領取流程
if (prizeType === 'PHYSICAL') {
  // 顯示配送地址選擇
  showShippingAddressForm();
} else if (prizeType === 'DIGITAL') {
  // 直接顯示兌換碼或領取按鈕
  showRedeemCodeForm();
} else if (prizeType === 'POINT') {
  // 自動加入帳戶，顯示確認訊息
  showPointAddedNotification();
}
```

---

## 📦 訂單相關 Enum 詳解

### 1️⃣ **OrderStatusEnum** - 訂單狀態

```java
public enum OrderStatusEnum {
    PENDING("PENDING", "待處理"),
    PREPARING("PREPARING", "準備中"),
    SHIPPED("SHIPPED", "已出貨"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");
}
```

**訂單狀態流轉**：
```
PENDING → PREPARING → SHIPPED → COMPLETED
           ↓
        CANCELLED (可隨時取消)
```

**狀態說明**：
| 狀態 | 可取消 | 可編輯 | 說明 |
|-----|-------|-------|------|
| PENDING | ✅ | ✅ | 剛下單，等待後台確認 |
| PREPARING | ✅ | ⚠️ | 後台準備中，可能已開始打包 |
| SHIPPED | ❌ | ❌ | 已出貨，進入物流階段 |
| COMPLETED | ❌ | ❌ | 已完成，用戶已簽收 |
| CANCELLED | - | - | 已取消，不可恢復 |

**相關方法**：
```java
// OrderStatusEnum 提供的方法
orderStatus.isCancellable();  // 是否可以取消
orderStatus.isFinished();     // 是否已結束（COMPLETED 或 CANCELLED）
```

**前端使用**：
```javascript
// 判斷是否顯示取消按鈕
const showCancelButton = ['PENDING', 'PREPARING'].includes(status);

// 判斷訂單是否已完成
const isOrderCompleted = status === 'COMPLETED';
```

---

### 2️⃣ **PaymentStatusEnum** - 支付狀態

```java
public enum PaymentStatusEnum {
    PENDING("PENDING", "待支付"),
    SUCCESS("SUCCESS", "支付成功"),
    FAILED("FAILED", "支付失敗"),
    CANCELLED("CANCELLED", "已取消");
}
```

**支付流轉**：
```
PENDING → SUCCESS (訂單進入 PREPARING)
  ↓
FAILED → (用戶可重新支付)
  ↓
CANCELLED
```

**前端使用**：
```javascript
if (paymentStatus === 'PENDING') {
  // 顯示支付按鈕
  showPaymentButton();
} else if (paymentStatus === 'SUCCESS') {
  // 顯示訂單詳情
  showOrderDetails();
} else if (paymentStatus === 'FAILED') {
  // 顯示重新支付選項
  showRetryPaymentButton();
}
```

---

### 3️⃣ **ShippingMethodEnum** - 配送方式

```java
public enum ShippingMethodEnum {
    HOME_DELIVERY("HOME_DELIVERY", "宅配到府"),
    SEVEN_ELEVEN("SEVEN_ELEVEN", "7-11 取貨"),
    FAMILY_MART("FAMILY_MART", "全家取貨");
}
```

**配送方式說明**：
| 方式 | 地點 | 費用 | 備註 |
|-----|------|-----|------|
| HOME_DELIVERY | 用戶指定地址 | 依距離計算 | 最快、最方便 |
| SEVEN_ELEVEN | 最近門市 | 固定費用 | 用戶自取 |
| FAMILY_MART | 最近門市 | 固定費用 | 用戶自取 |

**前端使用**：
```javascript
// 訂單流程中選擇配送方式
const shippingOptions = [
  { code: 'HOME_DELIVERY', label: '宅配到府', icon: '🚚' },
  { code: 'SEVEN_ELEVEN', label: '7-11 取貨', icon: '🏪' },
  { code: 'FAMILY_MART', label: '全家取貨', icon: '🏬' }
];
```

---

### 4️⃣ **PrizeBoxStatusEnum** - 獎品盒狀態

```java
public enum PrizeBoxStatusEnum {
    IN_BOX("IN_BOX", "在賞品盒中"),
    SHIPPED("SHIPPED", "已出貨"),
    RECYCLED("RECYCLED", "已回收");
}
```

**獎品盒狀態流轉**：
```
IN_BOX → SHIPPED → RECYCLED
```

**說明**：
- **IN_BOX**：獎品還在店家賞品盒中，尚未領取
- **SHIPPED**：獎品已由店家出貨
- **RECYCLED**：獎品已回收（退貨或報廢）

---

## 👤 用戶相關 Enum 詳解

### 1️⃣ **UserStatusEnum** - 會員狀態

```java
public enum UserStatusEnum {
    ACTIVE("ACTIVE", "正常"),
    INACTIVE("INACTIVE", "停用"),
    SUSPENDED("SUSPENDED", "暫停使用"),
    DELETED("DELETED", "已刪除");
}
```

**狀態說明**：
| 狀態 | 可登入 | 可抽獎 | 可支付 | 說明 |
|-----|-------|-------|-------|------|
| ACTIVE | ✅ | ✅ | ✅ | 正常帳號 |
| INACTIVE | ❌ | ❌ | ❌ | 被管理員停用 |
| SUSPENDED | ❌ | ❌ | ❌ | 因違規暫停（可恢復） |
| DELETED | ❌ | ❌ | ❌ | 已刪除（不可恢復） |

---

### 2️⃣ **AdminUserStatus** - 後台管理者狀態

```java
public enum AdminUserStatus {
    PENDING("PENDING", "待啟用"),
    ACTIVE("ACTIVE", "啟用"),
    INACTIVE("INACTIVE", "停用");
}
```

**狀態說明**：
| 狀態 | 可登入 | 說明 |
|-----|--------|------|
| PENDING | ⚠️ 首次登入可 | 新建立帳號，尚未首次登入改密碼 |
| ACTIVE | ✅ | 正常使用 |
| INACTIVE | ❌ | 被 Admin 停用 |

**相關方法**：
```java
adminUserStatus.canLogin();  // 是否可以登入（PENDING 和 ACTIVE）
```

---

### 3️⃣ **AuthProviderEnum** - 登入方式

```java
public enum AuthProviderEnum {
    LOCAL("LOCAL", "本地註冊"),
    GOOGLE("GOOGLE", "Google 登入"),
    FACEBOOK("FACEBOOK", "Facebook 登入"),
    LINE("LINE", "Line 登入");
}
```

**登入方式說明**：
| 方式 | 帳號來源 | 密碼 | 備註 |
|-----|--------|-----|------|
| LOCAL | 自建帳號 | 需設定 | 傳統帳密登入 |
| GOOGLE | Google 帳號 | OAuth | 第三方登入 |
| FACEBOOK | Facebook 帳號 | OAuth | 第三方登入 |
| LINE | Line 帳號 | OAuth | 第三方登入 |

**前端使用**：
```javascript
// 登入方式選擇
const authProviders = [
  { code: 'LOCAL', label: '帳密登入' },
  { code: 'GOOGLE', label: 'Google 登入', icon: '🔵' },
  { code: 'FACEBOOK', label: 'Facebook 登入', icon: '🔷' },
  { code: 'LINE', label: 'Line 登入', icon: '💚' }
];
```

---

### 4️⃣ **RoleCode** - 系統角色

```java
public enum RoleCode {
    ROLE_ADMIN("ROLE_ADMIN", "Admin", "平台管理員"),
    ROLE_STORE_OWNER("ROLE_STORE_OWNER", "StoreOwner", "店家主帳號"),
    ROLE_STORE_EDITOR("ROLE_STORE_EDITOR", "StoreEditor", "店家小編");
}
```

**角色權限對照**：
| 角色 | 代碼 | 管理員後台 | 店家商品 | 訂單管理 | 報表統計 | 會員管理 |
|-----|-----|----------|--------|--------|--------|--------|
| **Admin** | ROLE_ADMIN | ✅ 完全 | ✅ 完全 | ✅ 完全 | ✅ 完全 | ✅ 完全 |
| **StoreOwner** | ROLE_STORE_OWNER | ❌ | ✅ 自店 | ✅ 自店 | ✅ 自店 | ⚠️ 受限 |
| **StoreEditor** | ROLE_STORE_EDITOR | ❌ | ✅ 自店 | ⚠️ 部分 | ⚠️ 部分 | ❌ |

**前端使用**：
```javascript
// 判斷是否為管理員
if (userRole === 'ROLE_ADMIN') {
  showAdminDashboard();
}

// 判斷是否為店家相關角色
if (['ROLE_STORE_OWNER', 'ROLE_STORE_EDITOR'].includes(userRole)) {
  showStoreDashboard();
}
```

---

### 5️⃣ **StoreUserRoleType** - 店家角色

```java
public enum StoreUserRoleType {
    OWNER("OWNER", "店家主帳號"),
    EDITOR("EDITOR", "店家小編");
}
```

**角色說明**：
| 角色 | 帳號數量 | 權限 | 說明 |
|-----|--------|------|------|
| **OWNER** | 一對一 | 完全 | 透過 store.owner_id 綁定，主帳號 |
| **EDITOR** | 多對多 | 受限 | 透過 store_user 表綁定，小編帳號 |

**相關方法**：
```java
storeUserRoleType.isOwner();  // 是否為店家主帳號
```

---

### 6️⃣ **StoreStatus** - 店家狀態

```java
public enum StoreStatus {
    ACTIVE("ACTIVE", "啟用"),
    INACTIVE("INACTIVE", "停用");
}
```

**狀態說明**：
| 狀態 | 前台可見 | 商品可抽 | 說明 |
|-----|--------|--------|------|
| ACTIVE | ✅ | ✅ | 正常營運 |
| INACTIVE | ❌ | ❌ | 停用，商品自動下架 |

**相關方法**：
```java
storeStatus.isVisibleToFrontend();  // 是否為前台可見狀態
```

---

## 💰 點數相關 Enum 詳解

### 1️⃣ **PointType** - 點數類型（雙軌制）

```java
public enum PointType {
    GOLD("gold", "儲值金"),     // 用戶付費購買
    BONUS("bonus", "紅利金");   // 系統贈送
}
```

**點數類型說明**：

| 類型 | 獲取方式 | 有效期 | 優先消耗 | 備註 |
|-----|--------|-------|--------|------|
| **GOLD** | 付費儲值 | 永久 | 後 | 不可轉讓、不可退款 |
| **BONUS** | 系統贈送 | 有期限 | 先 | 新手禮、活動獎勵、簽到獲得 |

**消耗優先順序**：BONUS → GOLD（先用紅利金，再用儲值金）

**相關方法**：
```java
pointType.isGold();    // 是否為儲值金
pointType.isBonus();   // 是否為紅利金
```

**前端使用**：
```javascript
// 顯示點數餘額（分別顯示兩種點數）
const goldBalance = userData.goldCoins;      // 儲值金
const bonusBalance = userData.bonusCoins;    // 紅利金
const totalBalance = goldBalance + bonusBalance;

// 消耗提示：優先消耗 BONUS
if (bonusBalance > 0) {
  showAlert(`將優先消耗 ${bonusBalance} 紅利金`);
}
```

---

### 2️⃣ **PointOperationType** - 點數操作類型

```java
public enum PointOperationType {
    DEPOSIT("deposit", "儲值", true),                    // 增加
    DEDUCT("deduct", "扣除", false),                    // 減少
    DRAW("draw", "抽獎消費", false),                    // 減少
    REFUND("refund", "退款", true),                    // 增加
    BONUS_GRANT("bonus_grant", "紅利贈送", true),       // 增加
    BONUS_EXPIRE("bonus_expire", "紅利過期", false);    // 減少
}
```

**操作類型說明**：
| 操作 | 點數變化 | 說明 |
|-----|--------|------|
| DEPOSIT | ➕ | 用戶付費儲值 |
| DEDUCT | ➖ | 一般扣除（非抽獎） |
| DRAW | ➖ | 執行抽獎時扣除點數 |
| REFUND | ➕ | 訂單退款返還點數 |
| BONUS_GRANT | ➕ | 系統贈送紅利金（活動、簽到） |
| BONUS_EXPIRE | ➖ | 紅利金到期失效 |

**相關方法**：
```java
operationType.isIncrease();  // 是否為增加點數
operationType.isDecrease();  // 是否為減少點數
```

**前端使用**：
```javascript
// 根據操作類型顯示不同的提示訊息
const getOperationMessage = (opType, amount) => {
  if (opType === 'DRAW') return `抽獎消費 ${amount} 點`;
  if (opType === 'REFUND') return `訂單退款返還 ${amount} 點`;
  if (opType === 'BONUS_GRANT') return `獲得 ${amount} 紅利金`;
  return `點數操作：${amount}`;
};
```

---

### 3️⃣ **CoinTypeEnum** - 點數貨幣

```java
public enum CoinTypeEnum {
    GOLD("GOLD", "金幣"),
    BONUS("BONUS", "紅利");
}
```

**與 PointType 的區別**：
- **PointType**：用於點數日誌，記錄點數類型
- **CoinTypeEnum**：用於遊戲內貨幣，記錄錢包類型

> ℹ️ 兩者代碼定義相同，但應用場景不同

---

### 4️⃣ **TransactionTypeEnum** - 交易類型

```java
public enum TransactionTypeEnum {
    RECHARGE("RECHARGE", "儲值"),
    DRAW("DRAW", "抽獎消費"),
    RECYCLE("RECYCLE", "獎品回收"),
    REFUND("REFUND", "退款"),
    ADMIN_ADJUST("ADMIN_ADJUST", "系統調整");
}
```

**交易類型說明**：
| 類型 | 方向 | 說明 |
|-----|------|------|
| RECHARGE | ➕ | 用戶儲值 |
| DRAW | ➖ | 抽獎消費點數 |
| RECYCLE | ➖ | 獎品回收 |
| REFUND | ➕ | 退款返還 |
| ADMIN_ADJUST | ±️ | 管理員調整（手動增減） |

**前端使用**：
```javascript
// 交易記錄顯示
const transactions = [
  { type: 'RECHARGE', amount: 1000, date: '2026-02-07' },
  { type: 'DRAW', amount: -80, date: '2026-02-07' },
  { type: 'REFUND', amount: 80, date: '2026-02-06' }
];
```

---

## 📊 審計相關 Enum 詳解

### **OperationType** - 操作類型（審計日誌）

```java
public enum OperationType {
    CREATE("CREATE", "建立"),
    UPDATE("UPDATE", "更新"),
    DELETE("DELETE", "刪除"),
    LOGIN("LOGIN", "登入"),
    LOGOUT("LOGOUT", "登出"),
    CHANGE_PASSWORD("CHANGE_PASSWORD", "修改密碼"),
    ACTIVATE("ACTIVATE", "啟用帳號"),
    DEACTIVATE("DEACTIVATE", "停用帳號");
}
```

**用於 `admin_operation_log` 表，記錄管理員所有操作**

**操作類型說明**：
| 操作 | 記錄對象 | 說明 |
|-----|--------|------|
| CREATE | 商品/用戶/訂單 | 建立新資料 |
| UPDATE | 商品/用戶/訂單 | 編輯資料 |
| DELETE | 商品/用戶/訂單 | 刪除資料 |
| LOGIN | 帳號 | 管理員登入 |
| LOGOUT | 帳號 | 管理員登出 |
| CHANGE_PASSWORD | 帳號 | 修改密碼 |
| ACTIVATE | 帳號 | 啟用帳號 |
| DEACTIVATE | 帳號 | 停用帳號 |

---

## 🚀 前端集成範例

### 完整的遊戲選擇流程

```javascript
// Step 1: 選擇遊戲類型（Category）
const selectedCategory = 'GACHA';  // 扭蛋

// Step 2: 根據 Category 決定可用的 SubCategory
const availableSubCategories = {
  'OFFICIAL_ICHIBAN': ['LOTTERY_MODE'],
  'GACHA': ['LOTTERY_MODE'],
  'TRADING_CARD': ['LOTTERY_MODE'],
  'CUSTOM_GACHA': ['LOTTERY_MODE', 'SCRATCH_MODE', 'SCRATCH_CARD_MODE']
};

const selectedSubCategory = 'LOTTERY_MODE';  // 抽籤型

// Step 3: 根據 SubCategory 顯示不同的遊戲介面
if (selectedSubCategory === 'LOTTERY_MODE') {
  // 顯示抽籤動畫（7秒保護時間）
  showLotteryUI();
} else if (selectedSubCategory === 'SCRATCH_MODE') {
  // 顯示刮獎卡介面
  showScratchUI();
}

// Step 4: 執行抽獎並取得獎品
const drawResult = await api.draw({
  lotteryId: '商品UUID',
  count: 1
});

// Step 5: 根據獎品類型顯示領取方式
const prize = drawResult.data[0];
if (prize.prizeType === 'PHYSICAL') {
  // 配送獎品
  showShippingForm();
} else if (prize.prizeType === 'DIGITAL') {
  // 發送兌換碼
  showRedeemCodeForm();
} else if (prize.prizeType === 'POINT') {
  // 自動加入帳戶
  addPointsToWallet(prize.pointAmount);
}
```

---

## 📝 常見錯誤與解決

### ❌ 錯誤 1：混淆 Category 和 SubCategory

**錯誤**：
```javascript
// ❌ 錯誤：直接用 SubCategory 做遊戲分類
const games = filterBy(subCategory);  // 只有 3 種模式
```

**正確**：
```javascript
// ✅ 正確：先用 Category 分類，再用 SubCategory 確定遊戲模式
const games = filterBy(category);
const ui = selectUI(subCategory);
```

---

### ❌ 錯誤 2：不檢查 OrderStatus 就顯示按鈕

**錯誤**：
```javascript
// ❌ 錯誤：任何狀態都顯示取消按鈕
return <button onClick={cancelOrder}>取消訂單</button>;
```

**正確**：
```javascript
// ✅ 正確：只在可取消狀態顯示
if (['PENDING', 'PREPARING'].includes(status)) {
  return <button onClick={cancelOrder}>取消訂單</button>;
}
```

---

### ❌ 錯誤 3：混淆 PointType 和 PointOperationType

**錯誤**：
```javascript
// ❌ 錯誤：PointOperationType 是操作，不是點數類型
const balance = getBalance(PointOperationType.DRAW);
```

**正確**：
```javascript
// ✅ 正確：PointType 是點數類型，PointOperationType 是操作
const goldBalance = getBalance(PointType.GOLD);
const bonusBalance = getBalance(PointType.BONUS);

// PointOperationType 用於記錄操作日誌
logPointOperation(PointOperationType.DRAW, -80);
```

---

## 📚 API 對照表

### 建立商品時的必選字段

```json
{
  "category": "CUSTOM_GACHA",           // LotteryCategoryEnum
  "subCategory": "SCRATCH_MODE",        // LotterySubCategoryEnum
  "status": "DRAFT",                    // LotteryStatusEnum (默認)
  "prizes": [
    {
      "level": "A",                     // PrizeLevel
      "type": "PHYSICAL",               // PrizeTypeEnum
      "quantity": 5
    }
  ]
}
```

### 查詢商品列表

```json
GET /api/lottery/browse?category=GACHA&status=ON_SHELF

Response:
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "category": "GACHA",
      "subCategory": "LOTTERY_MODE",
      "status": "ON_SHELF",
      "prizes": [...]
    }
  ]
}
```

### 訂單流程

```json
POST /api/order/create
{
  "lotteryId": "uuid",
  "shippingMethod": "HOME_DELIVERY"    // ShippingMethodEnum
}

Response:
{
  "order": {
    "id": "uuid",
    "status": "PENDING",                // OrderStatusEnum
    "paymentStatus": "PENDING"          // PaymentStatusEnum
  }
}

// 支付後
POST /api/order/pay
{
  "orderId": "uuid",
  "paymentMethod": "CREDIT_CARD"
}

// 訂單進入 PREPARING
// 出貨後變更為 SHIPPED
// 簽收後變更為 COMPLETED
```

---

## 🎯 總結

✅ **記住這些核心 Enum**：
1. **遊戲分類**：Category + SubCategory
2. **遊戲狀態**：LotteryStatusEnum
3. **訂單流程**：OrderStatus → PaymentStatus → ShippingMethod
4. **獎品**：PrizeLevel + PrizeType
5. **點數**：PointType（GOLD/BONUS）+ PointOperationType
6. **用戶角色**：RoleCode（平台級）+ StoreUserRoleType（店家級）

✅ **前端開發要點**：
- 查詢時用 Category 分類，UI 根據 SubCategory 選擇
- 訂單流程中檢查 OrderStatus，只在特定狀態顯示按鈕
- 點數要分別管理 GOLD 和 BONUS，消耗優先 BONUS
- 獎品領取根據 PrizeType 決定流程

---

*如有遺漏或錯誤，歡迎回報修正！*

