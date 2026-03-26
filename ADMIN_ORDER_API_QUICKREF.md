# 後台訂單 API - 快速參考

## ✅ API 路徑已修復

**舊路徑**：`/api/admin/order` ❌  
**新路徑**：`/api/admin/orders` ✅

## 📋 API 列表（6 個）

| API | Method | 說明 |
|-----|--------|------|
| `/admin/orders/list` | POST | 查詢訂單列表（支援多條件） |
| `/admin/orders/{orderId}` | GET | 查詢訂單詳情 |
| `/admin/orders/{orderId}/prepare` | PUT | 準備出貨 |
| `/admin/orders/{orderId}/ship` | PUT | 訂單出貨（填物流單號） |
| `/admin/orders/{orderId}/complete` | PUT | 完成訂單 |
| `/admin/orders/{orderId}/cancel` | PUT | 取消訂單（僅 ADMIN） |

## 🚀 快速測試

### 1. 查詢訂單列表
```bash
POST /api/admin/orders/list
Authorization: Bearer {{token}}

{
  "condition": {
    "status": "PENDING"
  },
  "sortBy": "createdAt",
  "sortOrder": "DESC"
}
```

### 2. 出貨流程（3 步驟）
```bash
# Step 1: 準備出貨
PUT /api/admin/orders/{orderId}/prepare

# Step 2: 填寫物流單號
PUT /api/admin/orders/{orderId}/ship
{
  "trackingNo": "1234567890",
  "logisticsCompany": "黑貓宅急便"
}

# Step 3: 完成訂單（玩家確認收貨後）
PUT /api/admin/orders/{orderId}/complete
```

## 🔑 訂單狀態流程

```
PENDING → PREPARING → SHIPPED → COMPLETED
              ↓ (ADMIN only)
          CANCELLED
```

## 📁 詳細文檔

查看 `ADMIN_ORDER_API_COMPLETE.md` 獲取：
- 完整 Request/Response 範例
- 錯誤處理
- 前端代碼範例
- Postman Collection

---

**修復時間**: 2026-02-09  
**狀態**: ✅ 已完成
