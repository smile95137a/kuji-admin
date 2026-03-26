# 前後台商品列表 API 統一回應格式

## 📊 統一的回應格式

現在前後台都使用相同的 `LotteryRes` 格式，包含完整的中文翻譯：

### 完整範例

```json
{
  "success": true,
  "data": [
    {
      "id": "4fee44bf-05a2-430d-a1e4-24a5e2531ab0",
      "storeId": "0074fcab-2b26-41ac-b203-68d3b5b0aaca",
      "storeName": "KUJI 測試商店",
      "title": "火影忍者 一番賞 523",
      "description": "火影忍者主題一番賞，包含多種角色手辦",
      "imageUrl": "https://example.com/naruto.jpg",
      
      // ✅ 分類資訊（含中文）
      "category": "CUSTOM_GACHA",
      "categoryName": "自製賞",        // ← 新增中文
      "subCategory": "SCRATCH_MODE",
      "subCategoryName": "刮刮樂型",   // ← 新增中文
      
      // 價格資訊
      "pricePerDraw": 120,
      "currentPrice": 120,
      "discountedPrice": null,
      "autoDiscountEnabled": false,
      "discountTriggered": false,
      
      // 多抽選項
      "allowMultiDraw": true,
      "multiDrawOptions": [5, 10, 20],
      
      // 時間資訊
      "scheduledAt": "2026-01-20T10:24:00",
      "startTime": null,
      "endTime": "2026-01-21T10:24:00",
      
      // 抽數統計
      "totalDraws": 0,
      "maxDraws": 100,
      "remainingDraws": 100,
      
      // ✅ 狀態資訊（含中文）
      "status": "ON_SHELF",
      "statusName": "已上架",          // ← 已有中文
      
      // 排序與權重
      "orderNum": 10,
      "weight": 0,
      
      // 系統欄位（後台可見）
      "createdBy": "70dc7e33-6053-46eb-834e-24087ad436ce",
      "createdAt": "2026-01-20T10:24:11",
      "updatedAt": "2026-01-20T10:24:11",
      "remark": null,
      
      // 獎品統計
      "totalPrizes": 28,
      "remainingPrizes": 28,
      
      // 遊戲設定
      "protectionDraws": 50,
      "protectionMinutes": 10,
      "content": "<p>商品詳細說明...</p>",
      "gameMode": "NORMAL",
      "freeDrawEnabled": false,
      "designatedPrizeNumbers": null,
      "ticketsGenerated": false
    }
  ],
  "meta": {
    "timestamp": "2026-01-21T10:00:00",
    "requestId": "uuid"
  }
}
```

---

## 🔒 前後台差異

雖然使用相同的 `LotteryRes`，但前後台有以下差異：

### 後台 API (`/admin/lottery/list`)
✅ **可見所有欄位**，包括：
- `createdBy` - 建立者 ID
- `remark` - 內部備註
- 可查詢所有狀態（DRAFT, OFF_SHELF, ON_SHELF...）

### 前台 API (`/lottery/browse/list`)
✅ **過濾敏感欄位**（由前端決定是否顯示）：
- ~~`createdBy`~~ - 不應顯示給一般使用者
- ~~`remark`~~ - 內部備註不應暴露
- 只查詢 `status=ON_SHELF` 的商品

---

## 📝 Enum 中文對照表

### 商品分類（LotteryCategoryEnum）
| Code | 中文名稱 |
|------|---------|
| `OFFICIAL_ICHIBAN` | 官方一番賞 |
| `GACHA` | 扭蛋 |
| `TRADING_CARD` | 卡牌 |
| `CUSTOM_GACHA` | 自製賞 |

### 自製賞子類型（LotterySubCategoryEnum）
| Code | 中文名稱 |
|------|---------|
| `LOTTERY_MODE` | 抽籤型 |
| `SCRATCH_MODE` | 刮刮樂型 |

### 商品狀態（LotteryStatusEnum）
| Code | 中文名稱 |
|------|---------|
| `DRAFT` | 草稿 |
| `OFF_SHELF` | 已下架 |
| `ON_SHELF` | 已上架 |
| `IN_PROGRESS` | 抽獎中 |
| `ENDED` | 已結束 |
| `FORCED_OFF` | 強制下架 |

---

## 🎨 前端使用範例

### TypeScript 介面定義

```typescript
interface LotteryRes {
  id: string;
  storeId: string;
  storeName: string;
  title: string;
  description?: string;
  imageUrl?: string;
  
  // 分類（含中文）
  category: string;
  categoryName: string;        // ← 新增
  subCategory?: string;
  subCategoryName?: string;    // ← 新增
  
  // 價格
  pricePerDraw: number;
  currentPrice: number;
  discountedPrice?: number;
  autoDiscountEnabled: boolean;
  discountTriggered: boolean;
  
  // 多抽
  allowMultiDraw: boolean;
  multiDrawOptions: number[];
  
  // 時間
  scheduledAt?: string;
  startTime?: string;
  endTime?: string;
  
  // 抽數
  totalDraws: number;
  maxDraws: number;
  remainingDraws: number;
  
  // 狀態（含中文）
  status: string;
  statusName: string;          // ← 已有
  
  // 排序
  orderNum: number;
  weight: number;
  
  // 系統（前台可選擇不顯示）
  createdBy?: string;
  createdAt: string;
  updatedAt: string;
  remark?: string;
  
  // 獎品
  totalPrizes: number;
  remainingPrizes: number;
  
  // 遊戲設定
  protectionDraws?: number;
  protectionMinutes?: number;
  content?: string;
  gameMode?: string;
  freeDrawEnabled: boolean;
  designatedPrizeNumbers?: string;
  ticketsGenerated: boolean;
}
```

### 前台查詢範例

```typescript
// 查詢前台商品列表
const response = await axios.post('/api/lottery/browse/list', {
  condition: {
    category: 'OFFICIAL_ICHIBAN',
    title: '火影'
  },
  sortBy: 'created_at',
  sortOrder: 'DESC'
});

const lotteries = response.data.data;

// 顯示商品
lotteries.forEach(lottery => {
  console.log(`商品：${lottery.title}`);
  console.log(`  分類：${lottery.categoryName}`);        // ✅ 直接顯示中文
  console.log(`  子類型：${lottery.subCategoryName}`);   // ✅ 直接顯示中文
  console.log(`  狀態：${lottery.statusName}`);          // ✅ 直接顯示中文
  console.log(`  價格：$${lottery.pricePerDraw}`);
  console.log(`  店家：${lottery.storeName}`);
  console.log(`  剩餘：${lottery.remainingPrizes}/${lottery.totalPrizes}`);
});
```

### 前台過濾敏感欄位

```typescript
// 前端可以選擇不顯示的欄位
function sanitizeLotteryForDisplay(lottery: LotteryRes) {
  const { createdBy, remark, ...safeData } = lottery;
  return safeData;
}

// 使用
const safeLottery = sanitizeLotteryForDisplay(lottery);
```

---

## 🚀 API 路由總覽

### 後台 API
```
POST /api/admin/lottery/list          # 後台商品列表（所有狀態）
GET  /api/admin/lottery/:id           # 後台商品詳情
POST /api/admin/lottery               # 新增商品
PUT  /api/admin/lottery/:id           # 更新商品
DELETE /api/admin/lottery/:id         # 刪除商品
```

### 前台 API
```
POST /api/lottery/browse/list         # 前台商品列表（只查詢上架商品）
GET  /api/lottery/browse/:id          # 前台商品詳情（只能查看上架商品）
```

---

## 🎯 實作重點

### 1. ✅ 自動轉換中文名稱

在 `LotteryServiceImpl.convertToResNew()` 中：

```java
// 分類資訊（加上中文名稱）
res.setCategory(lottery.getCategory());
res.setCategoryName(LotteryCategoryEnum.getNameByCode(lottery.getCategory()));
res.setSubCategory(lottery.getSubCategory());
res.setSubCategoryName(LotterySubCategoryEnum.getNameByCode(lottery.getSubCategory()));

// 狀態（加上中文名稱）
res.setStatus(lottery.getStatus());
res.setStatusName(LotteryStatusEnum.getNameByCode(lottery.getStatus()));
```

### 2. ✅ Enum 提供靜態方法

每個 Enum 都有 `getNameByCode()` 方法：

```java
public static String getNameByCode(String code) {
    LotteryCategoryEnum e = fromCode(code);
    return e != null ? e.name : code;
}
```

### 3. ✅ 前台強制過濾

`LotteryBrowseController` 強制設定 `status=ON_SHELF`：

```java
@PostMapping("/list")
public ResponseEntity<List<LotteryRes>> queryLotteries(
        @RequestBody(required = false) QueryReq<LotteryCondition> req) {
    
    // 強制設定為上架中
    if (req == null) req = new QueryReq<>();
    if (req.getCondition() == null) req.setCondition(new LotteryCondition());
    req.getCondition().setStatus("ON_SHELF");
    
    List<LotteryRes> result = lotteryService.queryLotteries(req);
    return ResponseEntity.ok(result);
}
```

---

## ✅ 修改檔案清單

1. **LotteryRes.java**
   - 新增 `subCategoryName` 欄位

2. **LotteryServiceImpl.java**
   - 新增 `import LotterySubCategoryEnum`
   - 在 `convertToResNew()` 中設定 `subCategoryName`

3. **LotteryBrowseController.java**
   - 已存在，確保強制過濾 `status=ON_SHELF`

---

## 🧪 測試方式

### 後台測試

```bash
# 登入取得 token
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@kuji.com","password":"admin123"}'

# 查詢商品列表
curl -X POST http://localhost:8080/api/admin/lottery/list \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

### 前台測試

```bash
# 查詢上架商品（不需要 token）
curl -X POST http://localhost:8080/api/lottery/browse/list \
  -H "Content-Type: application/json" \
  -d '{}'

# 查詢特定分類
curl -X POST http://localhost:8080/api/lottery/browse/list \
  -H "Content-Type: application/json" \
  -d '{"condition":{"category":"CUSTOM_GACHA"}}'
```

---

**實作完成時間**：2026-01-21  
**狀態**：✅ 完成  
**版本**：2.0.0
