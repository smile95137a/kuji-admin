# 商品與獎品列表 API 實作完成報告

## 📋 實作摘要

為 `AdminLotteryWithPrizesController` 補充了**查詢全部商品**的 API，現在完整支援：

- ✅ 建立商品與獎品（POST）
- ✅ 更新商品與獎品（PUT）
- ✅ 查詢單一商品與獎品（GET /{id}）
- ✅ **查詢所有商品與獎品**（POST /list）← **新增**

---

## 🎯 新增的 API

### 路由
```
POST /admin/lottery-with-prizes/list
```

### 認證
需要 JWT Token，角色：`ROLE_ADMIN`、`ROLE_STORE_OWNER`、`ROLE_STORE_EDITOR`

### 請求格式（所有參數都是可選的）

#### 1. 無條件查詢（返回所有商品）
```json
POST /admin/lottery-with-prizes/list
{}
```

#### 2. 按狀態查詢
```json
{
  "condition": {
    "status": "ON_SHELF"
  }
}
```

#### 3. 按分類查詢
```json
{
  "condition": {
    "category": "OFFICIAL_ICHIBAN"
  }
}
```

#### 4. 標題模糊查詢
```json
{
  "condition": {
    "title": "鬼滅"
  }
}
```

#### 5. 價格區間查詢
```json
{
  "condition": {
    "priceMin": 50,
    "priceMax": 100
  }
}
```

#### 6. 組合查詢 + 排序
```json
{
  "condition": {
    "status": "ON_SHELF",
    "category": "OFFICIAL_ICHIBAN"
  },
  "sortBy": "price_per_draw",
  "sortOrder": "DESC"
}
```

### 回應格式
```json
{
  "success": true,
  "data": [
    {
      "id": "lottery-uuid-1",
      "storeId": "store-uuid-1",
      "storeName": "KUJI 旗艦店",
      "title": "鬼滅之刃一番賞",
      "description": "官方授權一番賞",
      "imageUrl": "https://...",
      "category": "OFFICIAL_ICHIBAN",
      "subCategory": null,
      "pricePerDraw": 80,
      "discountedPrice": null,
      "autoDiscountEnabled": false,
      "totalDraws": 100,
      "remainingDraws": 85,
      "status": "ON_SHELF",
      "scheduledAt": null,
      "createdAt": "2026-01-20T10:00:00",
      "updatedAt": "2026-01-20T10:00:00",
      "prizes": [
        {
          "id": "prize-uuid-1",
          "lotteryId": "lottery-uuid-1",
          "name": "炭治郎 手辦",
          "description": "最後賞",
          "imageUrl": "https://...",
          "level": "LAST",
          "prizeNumber": "L-001",
          "quantity": 1,
          "remaining": 1,
          "weight": 100,
          "prizeType": "PHYSICAL",
          "pointValue": null,
          "isLastPrize": true,
          "isGrandPrize": false,
          "orderNum": 1,
          "createdAt": "2026-01-20T10:00:00",
          "updatedAt": "2026-01-20T10:00:00"
        }
      ],
      "totalPrizeCount": 100,
      "remainingPrizeCount": 85,
      "progressPercentage": 15.0
    }
  ],
  "meta": {
    "timestamp": "2026-01-21T14:30:00",
    "requestId": "req-uuid"
  }
}
```

---

## 🔒 權限控制

### 自動 StoreID 過濾
- **店家負責人/編輯**：自動過濾只返回自己店家的商品
- **系統管理員**：返回所有店家的商品

### 實作邏輯
```java
String storeId = getStoreIdByUserId(userId);
if (storeId != null) {
    // 店家負責人/編輯 → 強制設定 storeId
    req.getCondition().setStoreId(storeId);
}
```

---

## 📊 查詢條件支援

| 欄位 | 類型 | 說明 | 範例 |
|------|------|------|------|
| `storeId` | String | 店家 ID（店家負責人/編輯自動帶入） | `"store-uuid-1"` |
| `title` | String | 標題模糊查詢 | `"鬼滅"` |
| `status` | String | 狀態 | `"ON_SHELF"`, `"OFF_SHELF"`, `"DRAFT"` |
| `category` | String | 分類 | `"OFFICIAL_ICHIBAN"`, `"CUSTOM"` |
| `priceMin` | Long | 最低價格 | `50` |
| `priceMax` | Long | 最高價格 | `100` |

### 排序支援
| 欄位 | 說明 |
|------|------|
| `sortBy` | 排序欄位（如 `"created_at"`, `"price_per_draw"`, `"title"`） |
| `sortOrder` | 排序方向（`"ASC"` 或 `"DESC"`，預設 `"ASC"`） |

**預設排序**：若未指定，使用 `created_at DESC`（最新的在前）

---

## 🛠️ 實作檔案

### 1. Service Interface
**檔案**: `LotteryService.java`

新增方法：
```java
List<LotteryWithPrizesRes> getAllLotteriesWithPrizes(
    QueryReq<LotteryCondition> req);
```

### 2. Service Implementation
**檔案**: `LotteryServiceImpl.java`

實作邏輯：
1. 建構查詢條件（所有條件可選）
2. 使用 MyBatis Example 查詢所有商品
3. 為每個商品查詢對應的獎品列表
4. 組裝完整回應（包含統計資訊）

核心程式碼：
```java
@Override
public List<LotteryWithPrizesRes> getAllLotteriesWithPrizes(
        QueryReq<LotteryCondition> req) {
    
    // 建構查詢條件
    LotteryCondition condition = (req != null && req.getCondition() != null) 
            ? req.getCondition() : new LotteryCondition();
    
    // 動態 SQL 查詢
    LotteryExample example = new LotteryExample();
    LotteryExample.Criteria criteria = example.createCriteria();
    
    if (condition.getStoreId() != null) {
        criteria.andStoreIdEqualTo(condition.getStoreId());
    }
    if (condition.getTitle() != null) {
        criteria.andTitleLike("%" + condition.getTitle() + "%");
    }
    // ... 其他條件
    
    // 查詢商品
    List<Lottery> lotteries = lotteryMapper.selectByExample(example);
    
    // 為每個商品查詢獎品
    return lotteries.stream()
            .map(lottery -> {
                // 查詢獎品列表
                LotteryPrizeExample prizeExample = new LotteryPrizeExample();
                prizeExample.createCriteria().andLotteryIdEqualTo(lottery.getId());
                List<LotteryPrize> prizes = lotteryPrizeMapper.selectByExample(prizeExample);
                
                // 組裝回應
                return buildLotteryWithPrizesRes(lotteryRes, prizeResList);
            })
            .collect(Collectors.toList());
}
```

### 3. Controller
**檔案**: `AdminLotteryWithPrizesController.java`

新增 Endpoint：
```java
@PostMapping("/list")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
public ResponseEntity<List<LotteryWithPrizesRes>> getAllLotteriesWithPrizes(
        @RequestBody(required = false) QueryReq<LotteryCondition> req) {
    
    String userId = SecurityUtils.getCurrentUserId();
    
    // 自動帶入 storeId
    String storeId = getStoreIdByUserId(userId);
    if (storeId != null) {
        if (req == null) req = new QueryReq<>();
        if (req.getCondition() == null) req.setCondition(new LotteryCondition());
        req.getCondition().setStoreId(storeId);
    }
    
    List<LotteryWithPrizesRes> result = lotteryService.getAllLotteriesWithPrizes(req);
    
    return ResponseEntity.ok(result);
}
```

新增 Imports：
```java
import com.group.admin.req.common.QueryReq;
import com.group.admin.req.lottery.LotteryCondition;
```

---

## 🧪 測試腳本

已建立測試腳本：`test-lottery-with-prizes-list.bat`

測試案例：
1. ✅ 查詢全部商品（無條件）
2. ✅ 查詢上架商品（status 過濾）
3. ✅ 按分類查詢（category 過濾）
4. ✅ 標題模糊查詢（title 模糊匹配）
5. ✅ 按價格排序（sortBy + sortOrder）

執行方式：
```bash
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
test-lottery-with-prizes-list.bat
```

---

## ✅ 編譯檢查

已確認以下檔案無編譯錯誤：
- ✅ `LotteryService.java`
- ✅ `LotteryServiceImpl.java`
- ✅ `AdminLotteryWithPrizesController.java`

---

## 📝 使用範例

### 前端 TypeScript 範例
```typescript
// 查詢所有上架商品
const response = await axios.post('/api/admin/lottery-with-prizes/list', {
  condition: {
    status: 'ON_SHELF'
  },
  sortBy: 'created_at',
  sortOrder: 'DESC'
}, {
  headers: {
    Authorization: `Bearer ${adminToken}`
  }
});

const lotteries = response.data.data;
console.log(`查詢到 ${lotteries.length} 個商品`);

lotteries.forEach(lottery => {
  console.log(`商品：${lottery.title}`);
  console.log(`  - 價格：$${lottery.pricePerDraw}`);
  console.log(`  - 獎品數量：${lottery.totalPrizeCount}`);
  console.log(`  - 剩餘：${lottery.remainingPrizeCount}`);
  console.log(`  - 進度：${lottery.progressPercentage}%`);
  
  lottery.prizes.forEach(prize => {
    console.log(`    * ${prize.level} - ${prize.name}`);
  });
});
```

### curl 範例
```bash
# 查詢所有商品
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes/list \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'

# 查詢上架商品
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes/list \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"condition":{"status":"ON_SHELF"}}'
```

---

## 🎉 完成狀態

| 功能 | 狀態 |
|------|------|
| Service Interface | ✅ 完成 |
| Service Implementation | ✅ 完成 |
| Controller Endpoint | ✅ 完成 |
| 權限控制 | ✅ 完成（自動 StoreID 過濾）|
| 查詢條件支援 | ✅ 完成（6 種條件）|
| 排序支援 | ✅ 完成 |
| 編譯檢查 | ✅ 通過 |
| 測試腳本 | ✅ 建立 |
| 文件 | ✅ 完成 |

---

## 📌 注意事項

1. **店家負責人/編輯**：自動過濾只返回自己店家的商品，前端不需要也不能指定 `storeId`
2. **系統管理員**：可以查詢所有店家的商品，也可以指定 `storeId` 過濾
3. **查詢條件**：所有條件都是可選的，可以組合使用
4. **排序**：預設按建立時間降冪排序（最新的在前）
5. **獎品列表**：每個商品都包含完整的獎品列表和統計資訊
6. **效能考量**：如果商品數量很大，建議前端做分頁處理

---

## 🔄 後續優化建議

1. **分頁支援**：可以考慮加入分頁參數（page, size）避免一次返回過多資料
2. **快取機制**：對於熱門商品可以考慮加入 Redis 快取
3. **批量查詢優化**：目前是 N+1 查詢（每個商品查一次獎品），可以優化為批次查詢
4. **欄位選擇**：可以加入 `fields` 參數讓前端選擇需要的欄位

---

**實作完成時間**：2026-01-21  
**實作者**：GitHub Copilot  
**版本**：1.0.0
