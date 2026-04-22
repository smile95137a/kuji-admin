# 06 - 訂單管理

> **路由前綴**：`/admin/orders`  
> **允許角色**：ADMIN / STORE_OWNER / STORE_EDITOR（資料依角色自動隔離）

---

## API 列表

| 方法 | 路徑 | 說明 | 需要角色 |
|------|------|------|---------|
| POST | `/admin/orders/list` | 查詢訂單列表 | 全角色 |
| GET | `/admin/orders/{orderId}` | 取得訂單詳情 | 全角色 |
| PUT | `/admin/orders/{orderId}/status` | 統一更新訂單狀態 | 全角色 |
| PUT | `/admin/orders/{orderId}/prepare` | 標記備貨完成 | 全角色 |
| PUT | `/admin/orders/{orderId}/ship` | 出貨（填物流單號） | 全角色 |
| PUT | `/admin/orders/{orderId}/cancel` | 取消訂單 | ADMIN / STORE_OWNER |

---

## 查詢訂單列表

```
POST /api/admin/orders/list
Authorization: Bearer {token}
```

### 請求
```typescript
interface OrderCondition {
  storeId?: string;           // 篩選店家（ADMIN 可選；其他角色無效，後端自動注入）
  userId?: string;            // 篩選特定玩家
  status?: string;            // 訂單狀態
  orderNo?: string;           // 訂單編號（精確查詢）
  createdAtStart?: string;
  createdAtEnd?: string;
}
```

### 回應
```typescript
interface OrderRes {
  id: string;
  orderNo: string;
  userId: string;
  username: string;          // 玩家顯示名稱
  storeId: string;
  storeName: string;
  lotteryId: string;
  lotteryTitle: string;
  status: OrderStatus;
  totalAmount: number;       // 訂單金額
  goldUsed: number;          // 消費金幣數
  bonusUsed: number;         // 消費紅利數
  items: OrderItemSummary[]; // 訂單品項摘要
  shippingAddress: string;
  trackingNumber: string | null;
  createdAt: string;
  updatedAt: string;
}
```

---

## 取得訂單詳情

```
GET /api/admin/orders/{orderId}
Authorization: Bearer {token}
```

### 回應
```typescript
interface OrderDetailRes extends OrderRes {
  items: OrderItem[];           // 完整品項（含獎品詳情）
  recipient: {
    name: string;
    phone: string;
    address: string;
    zipCode: string;
  };
  logs: OrderLog[];             // 訂單操作日誌
}

interface OrderItem {
  id: string;
  prizeId: string;
  prizeName: string;
  prizeLevel: string;
  prizeImageUrl: string;
  quantity: number;
}

interface OrderLog {
  operator: string;             // 操作人
  action: string;               // 操作描述
  fromStatus: string;
  toStatus: string;
  remark: string;
  createdAt: string;
}
```

---

## 訂單狀態流轉

### 狀態機
```
PENDING_PAYMENT → PAID（玩家付款完成，自動）
    ↓
PAID → PREPARING（店家確認訂單，標記備貨）
    ↓
PREPARING → SHIPPED（店家出貨，填寫物流單號）
    ↓
SHIPPED → DELIVERED（物流系統更新，或手動確認）
    ↓
DELIVERED → COMPLETED（玩家確認收貨）

任意狀態 → CANCELLED（在 SHIPPED 前可取消）
COMPLETED → REFUNDED（有限制條件）
```

### 統一更新狀態 API

```
PUT /api/admin/orders/{orderId}/status
Authorization: Bearer {token}
```

```typescript
interface UpdateOrderStatusReq {
  targetStatus: OrderStatus;  // 目標狀態
  remark?: string;            // 備註（取消時需填原因）
}
```

後端有狀態機驗證，不合法的狀態轉移會回傳 `400`。

---

## 備貨流程

### 標記備貨完成

```
PUT /api/admin/orders/{orderId}/prepare
Authorization: Bearer {token}
```

（無須 body）  
狀態：`PAID` → `PREPARING`

---

## 出貨流程

### 填寫物流資訊並出貨

```
PUT /api/admin/orders/{orderId}/ship
Authorization: Bearer {token}
```

```typescript
interface OrderShipReq {
  trackingNumber: string;         // 物流單號（必填）
  shippingCompany?: string;       // 物流廠商（如「黑貓」）
  trackingUrl?: string;           // 外部物流追蹤連結（選填，如 7-11 物流查詢頁）
  estimatedDeliveryDate?: string; // 預計到貨日
}
```

狀態：`CONFIRMED` → `SHIPPING`

> ✉️ **出貨成功後，後端自動寄送 Email 給會員**，Email 內容包含：
> - 訂單編號、獎品列表、運送方式
> - 物流追蹤號（`trackingNumber`） + 外部追蹤連結（若有）
> - 收件人資訊
> 
> 不需後台手動操作，所有通知由後端自動處理。

---

## 取消訂單

```
PUT /api/admin/orders/{orderId}/cancel
Authorization: Bearer {token}（需 ADMIN 或 STORE_OWNER）
```

```typescript
interface CancelOrderReq {
  reason: string;   // 取消原因（必填）
}
```

⚠️ 取消訂單後，後端自動執行：
1. 訂單狀態 → `CANCELLED`
2. 玩家消費的金幣/紅利**退回**（`@Transactional`）
3. 相關獎品數量回補（如適用）

---

## 前端 UI 建議

### 訂單列表頁
- 狀態篩選 Tab（全部/待付款/備貨中/已出貨/已完成/已取消）
- 搜尋框（訂單編號、玩家名稱）
- 日期範圍選擇器

### 訂單詳情頁
- 顯示操作日誌 Timeline
- 根據目前狀態顯示可執行操作按鈕：
  - `PAID` → 顯示「確認備貨」按鈕
  - `PREPARING` → 顯示「填物流並出貨」按鈕
  - `SHIPPED` → 顯示「確認送達」按鈕（手動）
  - 未完成狀態 → 顯示「取消訂單」按鈕（需角色權限）

### 權限控制
| 操作 | ADMIN | STORE_OWNER | STORE_EDITOR |
|------|-------|-------------|--------------|
| 查看訂單 | ✅ | ✅ | ✅ |
| 備貨/出貨 | ✅ | ✅ | ✅ |
| 取消訂單 | ✅ | ✅ | ❌ |
