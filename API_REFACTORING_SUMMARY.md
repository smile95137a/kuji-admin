# 前台 API 重構與 400 錯誤修復總結

## 修改日期
2026-01-28

## 修改原因
1. **API 重複問題**：`GET /{id}` 和 `GET /{id}/detail` 功能重複
2. **列表資訊不足**：`/list` API 缺少日期與數量資訊
3. **400 錯誤**：後台新增商品時因缺少 `weight` 欄位導致驗證失敗

## 修改內容

### 1. LotteryListItemRes - 新增日期與數量欄位

**檔案**：`src/main/java/com/group/admin/res/lottery/LotteryListItemRes.java`

**新增欄位**：
```java
@Schema(description = "建立時間")
private String createdAt;

@Schema(description = "更新時間")
private String updatedAt;

@Schema(description = "活動開始時間")
private String startTime;

@Schema(description = "活動結束時間")
private String endTime;
```

**from() 方法修改**：
```java
.createdAt(res.getCreatedAt() != null ? res.getCreatedAt().toString() : null)
.updatedAt(res.getUpdatedAt() != null ? res.getUpdatedAt().toString() : null)
.startTime(res.getStartTime() != null ? res.getStartTime().toString() : null)
.endTime(res.getEndTime() != null ? res.getEndTime().toString() : null)
```

**影響**：
- ✅ 前端可以顯示商品的建立/更新/開始/結束時間
- ✅ 原有的 `maxDraws`（總數量）和 `remainingDraws`（剩餘數量）已經存在

### 2. LotteryBrowseController - 合併重複 API

**檔案**：`src/main/java/com/group/admin/controller/api/LotteryBrowseController.java`

**Before**：
```
GET /api/lottery/browse/{id}          → 返回 LotteryRes（基本資訊）
GET /api/lottery/browse/{id}/detail   → 返回 LotteryDetailRes（完整資訊）
GET /api/lottery/browse/{id}/tickets  → 返回籤位列表
```

**After**：
```
GET /api/lottery/browse/{id}          → 返回 LotteryDetailRes（完整資訊，包含籤位）
```

**移除的 API**：
- ❌ `GET /{id}/detail`（功能已整合到 `GET /{id}`）
- ❌ `GET /{id}/tickets`（票資訊已包含在 LotteryDetailRes 中）

**保留的 API**：
- ✅ `POST /list`：查詢商品列表（簡化版）
- ✅ `GET /{id}`：查詢商品詳情（完整版，包含獎品+籤位+場次）
- ✅ `GET /store/{storeId}`：查詢店家商品

**API 回應結構**：
```json
{
  "success": true,
  "data": {
    "lottery": { /* 商品基本資訊 */ },
    "prizes": [ /* 獎品列表 */ ],
    "tickets": [ /* 籤位列表（前台安全版） */ ],
    "session": { /* 場次資訊 */ }
  }
}
```

### 3. LotteryPrizeCreateReq - 新增 weight 欄位

**檔案**：`src/main/java/com/group/admin/req/lottery/LotteryPrizeCreateReq.java`

**新增欄位**：
```java
/**
 * 權重（抽獎機率權重，數字越大機率越高）
 */
@Schema(description = "抽獎權重（用於隨機抽取，數字越大機率越高）", example = "10")
private Integer weight;
```

**問題原因**：
- Entity `LotteryPrize` 有 `weight` 欄位
- 但 CreateReq 沒有 `weight` 欄位
- 前端傳送的 JSON 包含 `weight`，導致反序列化失敗

**修復前的 400 錯誤 JSON**：
```json
{
  "lottery": { ... },
  "prizes": [
    {
      "name": "A賞 角色大型公仔",
      "quantity": 1,
      "weight": 1,  // ❌ CreateReq 沒有這個欄位
      "level": "A",
      ...
    }
  ]
}
```

**修復後**：
```json
{
  "lottery": { ... },
  "prizes": [
    {
      "name": "A賞 角色大型公仔",
      "quantity": 1,
      "weight": 1,  // ✅ CreateReq 已支援
      "level": "A",
      ...
    }
  ]
}
```

## 前端影響評估

### 需要修改的前端程式碼

#### 1. 商品列表頁
```typescript
// Before
interface LotteryListItem {
  id: string;
  title: string;
  imageUrl: string;
  pricePerDraw: number;
  maxDraws: number;
  remainingDraws: number;
  // ❌ 缺少日期
}

// After
interface LotteryListItem {
  id: string;
  title: string;
  imageUrl: string;
  pricePerDraw: number;
  maxDraws: number;
  remainingDraws: number;
  createdAt: string;     // ✅ 新增
  updatedAt: string;     // ✅ 新增
  startTime: string;     // ✅ 新增
  endTime: string;       // ✅ 新增
}
```

#### 2. 商品詳情頁 API 呼叫
```typescript
// Before
const { data: basicInfo } = await api.get(`/lottery/browse/${id}`);
const { data: detail } = await api.get(`/lottery/browse/${id}/detail`);
const { data: tickets } = await api.get(`/lottery/browse/${id}/tickets`);

// After
const { data: detail } = await api.get(`/lottery/browse/${id}`);
// ✅ 一支 API 取得所有資訊（商品+獎品+籤位+場次）
```

#### 3. 後台新增商品表單
```typescript
// Before
const prizeForm = {
  name: string;
  quantity: number;
  level: string;
  // ❌ 沒有 weight 欄位
};

// After
const prizeForm = {
  name: string;
  quantity: number;
  weight: number;  // ✅ 必須加上
  level: string;
};
```

## API 路由對照表

| 用途 | Before | After | 狀態 |
|------|--------|-------|------|
| 查詢商品列表 | `POST /list` | `POST /list` | ✅ 保持不變，但回應新增日期欄位 |
| 查詢商品基本資訊 | `GET /{id}` | `GET /{id}` | ⚠️ 回應改為 LotteryDetailRes |
| 查詢商品詳情 | `GET /{id}/detail` | ❌ 已移除 | ⚠️ 改用 `GET /{id}` |
| 查詢籤位列表 | `GET /{id}/tickets` | ❌ 已移除 | ⚠️ 改用 `GET /{id}` |
| 查詢店家商品 | `GET /store/{storeId}` | `GET /store/{storeId}` | ✅ 保持不變 |

## 測試驗證

### 1. 測試商品列表 API
```bash
curl -X POST http://18.179.187.129/api/lottery/browse/list \
  -H "Content-Type: application/json" \
  -d '{
    "condition": {
      "status": "ON_SHELF"
    },
    "sortBy": "created_at",
    "sortOrder": "DESC"
  }'
```

**預期回應**：
```json
{
  "success": true,
  "data": [
    {
      "id": "...",
      "title": "鬼滅之刃一番賞",
      "maxDraws": 80,
      "remainingDraws": 75,
      "createdAt": "2026-01-28T...",
      "updatedAt": "2026-01-28T...",
      "startTime": "2026-01-28T...",
      "endTime": "2026-02-28T..."
    }
  ]
}
```

### 2. 測試商品詳情 API
```bash
curl http://18.179.187.129/api/lottery/browse/{lotteryId} \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**預期回應**：
```json
{
  "success": true,
  "data": {
    "lottery": { /* 商品資訊 */ },
    "prizes": [ /* 獎品列表 */ ],
    "tickets": [ /* 籤位列表 */ ],
    "session": {
      "isOpener": false,
      "canDraw": true,
      "cannotDrawReason": null
    }
  }
}
```

### 3. 測試後台新增商品
```bash
curl -X POST http://18.179.187.129/api/admin/lottery-with-prizes \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "lottery": {
      "title": "火影忍者一番賞",
      "category": "CUSTOM_GACHA",
      "pricePerDraw": 150,
      "maxDraws": 120
    },
    "prizes": [
      {
        "name": "A賞 角色大型公仔",
        "quantity": 1,
        "weight": 1,
        "level": "A",
        "isGrandPrize": true
      }
    ]
  }'
```

**預期結果**：
- ✅ 200 OK（不再是 400）
- ✅ 返回完整的商品與獎品資訊

## 部署檢查清單

- [ ] 編譯成功（BUILD SUCCESS）
- [ ] 上傳 JAR 到 EC2
- [ ] 重啟服務
- [ ] 測試 `/list` API（確認日期欄位）
- [ ] 測試 `/{id}` API（確認返回完整資訊）
- [ ] 測試後台新增商品（確認 weight 欄位不再導致 400）
- [ ] 確認舊的 `/{id}/detail` 和 `/{id}/tickets` API 已停用
- [ ] 通知前端更新 API 呼叫邏輯

## 潛在問題與注意事項

### 1. 前端快取問題
如果前端有 API 快取機制，需要清除快取或更新 API 版本號。

### 2. 舊版 URL 相容性
如果前端仍在使用 `/{id}/detail`，可以暫時保留並標記為 `@Deprecated`，預計 1-2 週後移除。

### 3. 日期格式
所有日期欄位統一使用 ISO 8601 格式（`2026-01-28T04:14:07`）。

## 相關文件

- [抽獎籤位系統設計](lottery-ticket-system.prompt.md)
- [前台 API 完整參考](FRONTEND_API_COMPLETE_REFERENCE.md)
- [前端整合指南](FRONTEND_INTEGRATION_PROMPT.md)

---

**修改完成時間**：2026-01-28T04:30:00+08:00  
**BUILD SUCCESS**：✅ 編譯成功，JAR 已生成  
**待部署**：❌ 尚未上傳到 EC2
