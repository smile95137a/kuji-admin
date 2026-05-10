# 管理端訂單 API 契約

功能：訂單管理  
基礎路徑：/admin/orders  
驗證：JWT Bearer Token

## 角色與權限

1. ADMIN：可跨店查詢、可備貨/出貨/完成/取消。
2. STORE_OWNER：僅可操作自己店家，且可完成、可取消。
3. STORE_EDITOR：僅可操作自己店家，可備貨/出貨，不可完成、不可取消。

## 狀態定義

1. PAYMENT_PENDING：待付款
2. PAYMENT_FAILED：付款失敗
3. PENDING：待處理
4. PREPARING：備貨中
5. SHIPPED：已出貨
6. COMPLETED：已完成
7. CANCELLED：已取消

## API 一覽

1. POST /admin/orders/list：查詢訂單列表
2. GET /admin/orders/{orderId}：查詢訂單詳情
3. PUT /admin/orders/{orderId}/status：統一狀態更新
4. PUT /admin/orders/{orderId}/prepare：標記備貨
5. PUT /admin/orders/{orderId}/ship：出貨（填 trackingNo）
6. PUT /admin/orders/{orderId}/complete：完成訂單（ADMIN/STORE_OWNER）
7. DELETE /admin/orders/{orderId}：取消訂單（ADMIN/STORE_OWNER）
8. GET /admin/orders/{orderId}/status-log：查詢狀態歷程

---

## 1) 查詢列表

POST /admin/orders/list

```json
{
  "condition": {
    "storeId": "uuid-optional",
    "orderNo": "ORD202605110001",
    "status": "PENDING",
    "shippingMethod": "HOME_DELIVERY",
    "userId": "uuid-optional",
    "userKeyword": "玩家關鍵字",
    "recipientName": "王",
    "recipientPhone": "0912",
    "createdAtStart": "2026-05-01T00:00:00",
    "createdAtEnd": "2026-05-31T23:59:59"
  },
  "page": 1,
  "size": 20,
  "sortBy": "created_at",
  "sortOrder": "DESC"
}
```

規則：
1. STORE_OWNER / STORE_EDITOR 傳入的 storeId 會被後端覆寫為自身店家。
2. ADMIN 可自由指定 storeId 或查全部。

---

## 2) 查詢詳情

GET /admin/orders/{orderId}

回傳重點欄位：
1. 訂單主資訊（shippingStatus、paymentStatus、shippingFee、recipient*）。
2. items（獎品快照）。
3. statusHistory（狀態轉移記錄）。

---

## 3) 統一狀態更新

PUT /admin/orders/{orderId}/status

```json
{
  "targetStatus": "PREPARING",
  "trackingNo": "700123456789",
  "remark": "備註"
}
```

規則：
1. PENDING -> PREPARING
2. PREPARING -> SHIPPED（可帶 trackingNo）
3. SHIPPED -> COMPLETED
4. 可轉 CANCELLED 僅限可取消狀態（PAYMENT_PENDING / PAYMENT_FAILED / PENDING / PREPARING）

---

## 4) 標記備貨

PUT /admin/orders/{orderId}/prepare

規則：
1. 只允許 PENDING -> PREPARING。

---

## 5) 出貨

PUT /admin/orders/{orderId}/ship

```json
{
  "trackingNo": "700123456789",
  "remark": "已交寄"
}
```

規則：
1. 只允許 PREPARING -> SHIPPED。
2. trackingNo 必填。

---

## 6) 完成訂單

PUT /admin/orders/{orderId}/complete

規則：
1. 角色限制：ADMIN / STORE_OWNER。
2. 只允許 SHIPPED -> COMPLETED。

---

## 7) 取消訂單

DELETE /admin/orders/{orderId}

```json
{
  "cancelReason": "顧客要求取消"
}
```

規則：
1. 角色限制：ADMIN / STORE_OWNER。
2. 可取消狀態：PAYMENT_PENDING / PAYMENT_FAILED / PENDING / PREPARING。
3. 取消後 PrizeBox 回 AVAILABLE，解除 orderId。
4. 已 SHIPPED / COMPLETED 不可取消。

---

## 8) 狀態歷程

GET /admin/orders/{orderId}/status-log

回傳欄位：
1. fromStatus / toStatus
2. fromStatusLabel / toStatusLabel
3. operatorId / operatorType
4. remark / createdAt

---

## 錯誤碼建議

1. 401 UNAUTHORIZED：未登入或 token 無效。
2. 403 FORBIDDEN：跨店或角色無權限。
3. 404 ORDER_NOT_FOUND：訂單不存在。
4. 409 INVALID_STATE_TRANSITION：狀態轉換不允許。
