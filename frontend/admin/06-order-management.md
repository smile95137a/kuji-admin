# 06 - 訂單管理（後台）

路由前綴：/admin/orders

## 角色權限

1. ADMIN：可查全部、可備貨/出貨/完成/取消。
2. STORE_OWNER：僅自己店家，可備貨/出貨/完成/取消。
3. STORE_EDITOR：僅自己店家，可備貨/出貨，不可完成、不可取消。

## API 一覽

1. POST /admin/orders/list：查詢列表
2. GET /admin/orders/{orderId}：查詢詳情
3. PUT /admin/orders/{orderId}/status：統一狀態更新
4. PUT /admin/orders/{orderId}/prepare：標記備貨
5. PUT /admin/orders/{orderId}/ship：填 trackingNo 出貨
6. PUT /admin/orders/{orderId}/complete：完成訂單（ADMIN / STORE_OWNER）
7. DELETE /admin/orders/{orderId}：取消訂單（ADMIN / STORE_OWNER）
8. GET /admin/orders/{orderId}/status-log：查狀態歷程

---

## 狀態與流程

### 訂單主狀態

1. PAYMENT_PENDING
2. PAYMENT_FAILED
3. PENDING
4. PREPARING
5. SHIPPED
6. COMPLETED
7. CANCELLED

### 履約流程

PENDING -> PREPARING -> SHIPPED -> COMPLETED

### 可取消範圍

1. 後台（ADMIN / STORE_OWNER）：PAYMENT_PENDING / PAYMENT_FAILED / PENDING / PREPARING。
2. STORE_EDITOR：不可取消。

---

## 出貨 API 請求

PUT /api/admin/orders/{orderId}/ship

```typescript
interface OrderShipReq {
  trackingNo: string;   // 必填
  remark?: string;
}
```

---

## 統一狀態更新 API 請求

PUT /api/admin/orders/{orderId}/status

```typescript
interface UpdateOrderStatusReq {
  targetStatus: 'PREPARING' | 'SHIPPED' | 'COMPLETED' | 'CANCELLED';
  trackingNo?: string; // targetStatus = SHIPPED 時可帶
  remark?: string;
}
```

注意：
1. 仍建議取消優先走 DELETE /{orderId}。
2. 非法狀態轉換會被後端拒絕。

---

## 列表查詢條件

```typescript
interface OrderCondition {
  storeId?: string;         // ADMIN 可用；店家角色會被後端覆寫
  orderNo?: string;
  status?: string;
  shippingMethod?: string;
  userId?: string;
  userKeyword?: string;
  recipientName?: string;
  recipientPhone?: string;
  createdAtStart?: string;
  createdAtEnd?: string;
}
```

---

## 前端 UI 建議

1. 狀態 Tab：PAYMENT_PENDING、PAYMENT_FAILED、PENDING、PREPARING、SHIPPED、COMPLETED、CANCELLED。
2. 在 PAYMENT_FAILED 顯示明確標籤，便於客服與店家識別。
3. 依角色控制按鈕：
   - STORE_EDITOR 隱藏「取消」與「完成」。
4. 出貨頁僅保留 trackingNo 欄位，避免前後端欄位不一致。
