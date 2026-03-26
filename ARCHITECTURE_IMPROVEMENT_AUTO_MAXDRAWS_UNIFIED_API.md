# 架構改進：自動計算 maxDraws + 統一 API 回應格式

**日期**：2025-12-25  
**版本**：1.1.0  
**優先級**：🟢 MEDIUM（架構優化，提升資料一致性）

---

## 📋 改進摘要

本次改進實現了兩個重要的架構優化：

### 1️⃣ **後端自動計算 maxDraws**
- **問題**：前端需要手動計算獎品總數並傳入 maxDraws，容易出錯
- **解決方案**：後端自動從獎品數量計算 maxDraws，確保資料一致性
- **影響範圍**：商品建立流程、獎品管理

### 2️⃣ **統一 List 和 Detail API 回應格式**
- **問題**：List API 返回簡化版（LotteryListItemRes），Detail API 返回完整版（LotteryDetailRes），前端需要處理兩種結構
- **解決方案**：兩者統一返回 LotteryDetailRes，唯一差異是 List 不包含 tickets 欄位
- **影響範圍**：前台商品瀏覽 API

---

## 🔧 技術實現

### 1. 自動計算 maxDraws

#### 修改檔案
- `LotteryServiceImpl.java` - `createLotteryWithPrizes()` 方法
- `LotteryCreateReq.java` - maxDraws 欄位註解

#### 核心邏輯

```java
// Step 2.5: 自動計算 maxDraws
int totalPrizeQuantity = req.getPrizes().stream()
        .mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0)
        .sum();

String playMode = req.getLottery().getPlayMode();

// 根據遊戲模式計算 maxDraws
int calculatedMaxDraws;
if ("LOTTERY_MODE".equals(playMode)) {
    // 一番賞：maxDraws = 獎品總數（不能有謝謝惠顧）
    calculatedMaxDraws = totalPrizeQuantity;
    log.info("🎯 一番賞模式：自動設定 maxDraws = 獎品總數 = {}", calculatedMaxDraws);
} else if ("SCRATCH_MODE".equals(playMode)) {
    // 刮刮樂：maxDraws = 獎品總數（未來可擴充謝謝惠顧）
    calculatedMaxDraws = totalPrizeQuantity;
    log.info("🎰 刮刮樂模式：自動設定 maxDraws = 獎品總數 = {}", calculatedMaxDraws);
} else {
    // 未知模式，預設為獎品總數
    calculatedMaxDraws = totalPrizeQuantity;
    log.warn("⚠️ 未知遊戲模式: {}，預設設定 maxDraws = 獎品總數 = {}", playMode, calculatedMaxDraws);
}

// 更新商品的 maxDraws（覆寫前端傳入的值）
Lottery lottery = lotteryMapper.selectByPrimaryKey(lotteryId);
lottery.setMaxDraws(calculatedMaxDraws);
lottery.setUpdatedAt(LocalDateTime.now());
lotteryMapper.updateByPrimaryKey(lottery);
```

#### 驗證規則

| 遊戲模式 | maxDraws 計算 | 謝謝惠顧 | 驗證規則 |
|---------|--------------|---------|---------|
| `LOTTERY_MODE` | 後端自動計算 = 獎品總數 | ❌ 不允許 | maxDraws = Σ(prize.quantity)<br>即使前端傳入也會被覆寫 |
| `SCRATCH_MODE` | 使用前端傳入值 | ✅ 支援 | maxDraws ≥ Σ(prize.quantity)<br>謝謝惠顧數 = maxDraws - 獎品總數 |

#### 刮刮樂模式邏輯（新增）

```java
if ("SCRATCH_MODE".equals(playMode)) {
    // 刮刮樂：使用前端傳入的 maxDraws（支援謝謝惠顧）
    if (frontendMaxDraws != null && frontendMaxDraws >= totalPrizeQuantity) {
        calculatedMaxDraws = frontendMaxDraws;
        int thanksgivingCount = frontendMaxDraws - totalPrizeQuantity;
        log.info("🎰 刮刮樂模式：使用前端設定 maxDraws = {}（獎品 {} 個 + 謝謝惠顧 {} 個）", 
                calculatedMaxDraws, totalPrizeQuantity, thanksgivingCount);
    } else if (frontendMaxDraws != null && frontendMaxDraws < totalPrizeQuantity) {
        // 總抽數不能小於獎品總數
        throw new BusinessException("刮刮樂模式錯誤：總抽數不能小於獎品總數！");
    } else {
        // 前端未設定，預設為獎品總數（沒有謝謝惠顧）
        calculatedMaxDraws = totalPrizeQuantity;
    }
}
```

#### 前端影響

**一番賞模式（LOTTERY_MODE）**：

**Before（舊架構）**：
```javascript
// ❌ 前端需要手動計算總數
const prizes = [
  { quantity: 1 },  // A賞
  { quantity: 5 },  // B賞
  { quantity: 20 }  // C賞
];
const maxDraws = prizes.reduce((sum, p) => sum + p.quantity, 0); // 26

const response = await axios.post('/api/admin/lottery-with-prizes', {
  lottery: {
    title: '鬼滅一番賞',
    maxDraws: maxDraws  // ← 必須手動計算並傳入
  },
  prizes: prizes
});
```

**After（新架構）**：
```javascript
// ✅ 前端不需要計算，後端自動處理
const response = await axios.post('/api/admin/lottery-with-prizes', {
  lottery: {
    title: '鬼滅一番賞',
    playMode: 'LOTTERY_MODE'
    // maxDraws 不用傳，後端自動計算
  },
  prizes: [
    { quantity: 1 },  // A賞
    { quantity: 5 },  // B賞
    { quantity: 20 }  // C賞
  ]
});

// 後端自動計算 maxDraws = 1 + 5 + 20 = 26
```

---

**刮刮樂模式（SCRATCH_MODE）**：

**新架構（支援謝謝惠顧）**：
```javascript
// ✅ 刮刮樂必須傳入 maxDraws（支援謝謝惠顧）
const response = await axios.post('/api/admin/lottery-with-prizes', {
  lottery: {
    title: '新年刮刮樂',
    playMode: 'SCRATCH_MODE',
    maxDraws: 50  // ← 刮刮樂必須傳入總抽數
  },
  prizes: [
    { name: '頭獎', quantity: 1 },  // 頭獎 1 個
    { name: '貳獎', quantity: 1 }   // 貳獎 1 個
  ]
});

// 後端計算：
// - 獎品總數 = 2
// - maxDraws = 50（使用前端設定）
// - 謝謝惠顧 = 50 - 2 = 48 個
// - 生成 50 個籤位：2 個中獎 + 48 個謝謝惠顧
```

**刮刮樂驗證**：
```javascript
// ❌ 錯誤：maxDraws 小於獎品總數
const response = await axios.post('/api/admin/lottery-with-prizes', {
  lottery: {
    title: '新年刮刮樂',
    playMode: 'SCRATCH_MODE',
    maxDraws: 5  // ← 錯誤！小於獎品總數 10
  },
  prizes: [
    { quantity: 10 }  // 獎品 10 個
  ]
});
// 後端返回錯誤：「刮刮樂模式錯誤：總抽數(5)不能小於獎品總數(10)！」
```

---

### 2. 統一 List 和 Detail API 回應格式

#### 修改檔案
- `LotteryBrowseController.java` - `queryLotteries()` 方法

#### Before（舊架構）

**List API** - 返回簡化版：
```java
@PostMapping("/list")
public ResponseEntity<List<LotteryListItemRes>> queryLotteries(...) {
    // 返回 LotteryListItemRes（13 個欄位）
    List<LotteryListItemRes> result = fullList.stream()
            .map(LotteryListItemRes::from)
            .collect(Collectors.toList());
    return ResponseEntity.ok(result);
}
```

**Detail API** - 返回完整版：
```java
@GetMapping("/{id}")
public ResponseEntity<LotteryDetailRes> getLotteryDetail(...) {
    // 返回 LotteryDetailRes（包含 lottery + prizes + tickets + session）
    return ResponseEntity.ok(detailRes);
}
```

**前端問題**：
- 需要處理兩種不同的資料結構
- List 和 Detail 的欄位不一致，難以統一狀態管理
- 代碼重複，維護成本高

#### After（新架構）

**List API** - 返回統一結構（不含 tickets）：
```java
@PostMapping("/list")
public ResponseEntity<List<LotteryDetailRes>> queryLotteries(...) {
    List<LotteryDetailRes> result = fullList.stream()
            .map(lotteryRes -> {
                // 取得獎品列表
                List<LotteryPrizeRes> prizes = lotteryService.getPrizesByLotteryId(lotteryRes.getId());
                
                // 組裝回應（不包含 tickets 和 session）
                return LotteryDetailRes.builder()
                        .lottery(lotteryRes)  // ✅ 完整商品資訊
                        .prizes(prizes)       // ✅ 獎品列表
                        .tickets(null)        // ❌ 列表不返回 tickets
                        .session(null)        // ❌ 列表不返回 session
                        .build();
            })
            .collect(Collectors.toList());
    
    return ResponseEntity.ok(result);
}
```

**Detail API** - 保持不變（包含 tickets）：
```java
@GetMapping("/{id}")
public ResponseEntity<LotteryDetailRes> getLotteryDetail(...) {
    return LotteryDetailRes.builder()
            .lottery(lotteryRes)
            .prizes(prizes)
            .tickets(tickets)    // ✅ 詳情包含 tickets
            .session(sessionInfo) // ✅ 詳情包含 session
            .build();
}
```

#### 回應結構對比

| 欄位 | List API | Detail API | 說明 |
|------|---------|-----------|------|
| `lottery` | ✅ 完整 | ✅ 完整 | 商品基本資訊（30+ 欄位） |
| `prizes` | ✅ 完整 | ✅ 完整 | 獎品列表 |
| `tickets` | ❌ null | ✅ 完整 | 籤位列表（列表不需要） |
| `session` | ❌ null | ✅ 完整 | 場次資訊（列表不需要） |

#### 前端影響

**Before（舊架構）**：
```typescript
// ❌ 需要處理兩種不同結構
interface LotteryListItem {
  id: string;
  title: string;
  imageUrl: string;
  pricePerDraw: number;
  // ... 只有 13 個基本欄位
}

interface LotteryDetail {
  lottery: {
    id: string;
    title: string;
    imageUrl: string;
    pricePerDraw: number;
    // ... 30+ 個欄位
  };
  prizes: Prize[];
  tickets: Ticket[];
  session: Session;
}

// 前端需要兩個不同的資料模型
const [listItems, setListItems] = useState<LotteryListItem[]>([]);
const [detail, setDetail] = useState<LotteryDetail | null>(null);
```

**After（新架構）**：
```typescript
// ✅ 統一使用 LotteryDetailRes
interface LotteryDetailRes {
  lottery: Lottery;        // 完整商品資訊
  prizes: Prize[];         // 獎品列表
  tickets: Ticket[] | null; // List 為 null，Detail 有值
  session: Session | null;  // List 為 null，Detail 有值
}

// 前端只需要一個資料模型
const [lotteries, setLotteries] = useState<LotteryDetailRes[]>([]);
const [selectedLottery, setSelectedLottery] = useState<LotteryDetailRes | null>(null);

// 列表點擊後，可以直接使用列表資料，只需額外載入 tickets
const handleSelectLottery = (lottery: LotteryDetailRes) => {
  // 先顯示列表資料（lottery + prizes）
  setSelectedLottery(lottery);
  
  // 再額外載入 tickets（如果需要）
  fetchLotteryDetail(lottery.lottery.id).then(fullDetail => {
    setSelectedLottery(fullDetail);
  });
};
```

---

## 📊 API 回應範例

### List API - `/api/lottery/browse/list`

```json
[
  {
    "lottery": {
      "id": "uuid-1",
      "title": "鬼滅之刃一番賞",
      "imageUrl": "https://...",
      "pricePerDraw": 650,
      "maxDraws": 80,
      "remainingDraws": 56,
      "status": "ON_SHELF",
      "playMode": "LOTTERY_MODE",
      "bonusEnabled": true,
      "tags": ["新品", "一番賞"],
      "theme": "鬼滅之刃",
      "hotCount": 999
      // ... 其他 30+ 欄位
    },
    "prizes": [
      {
        "id": "prize-1",
        "name": "炭治郎手辦",
        "level": "A",
        "quantity": 1,
        "remaining": 0,
        "isGrandPrize": true
      },
      {
        "id": "prize-2",
        "name": "禰豆子吊飾",
        "level": "B",
        "quantity": 5,
        "remaining": 3
      }
      // ... 其他獎品
    ],
    "tickets": null,  // ← 列表不返回 tickets
    "session": null   // ← 列表不返回 session
  },
  {
    // ... 其他商品
  }
]
```

### Detail API - `/api/lottery/browse/{id}`

```json
{
  "lottery": {
    "id": "uuid-1",
    "title": "鬼滅之刃一番賞",
    "imageUrl": "https://...",
    "pricePerDraw": 650,
    "maxDraws": 80,
    "remainingDraws": 56,
    "status": "ON_SHELF"
    // ... 完整欄位
  },
  "prizes": [
    {
      "id": "prize-1",
      "name": "炭治郎手辦",
      "level": "A",
      "quantity": 1,
      "remaining": 0,
      "isGrandPrize": true
    }
    // ... 完整獎品列表
  ],
  "tickets": [  // ← 詳情包含 tickets
    {
      "id": "ticket-1",
      "number": 1,
      "status": "DRAWN",
      "prizeId": "prize-1",
      "prizeName": "炭治郎手辦",
      "prizeImageUrl": "https://..."  // ← 已抽籤位顯示獎品資訊
    },
    {
      "id": "ticket-2",
      "number": 2,
      "status": "AVAILABLE",
      "prizeId": null,           // ← 未抽籤位隱藏獎品資訊
      "prizeName": null,
      "prizeImageUrl": null,     // ← 安全性修正（2025-12-25）
      "isGrandPrize": null,
      "isLastPrize": null
    }
    // ... 其他籤位
  ],
  "session": {  // ← 詳情包含 session
    "isOpener": false,
    "openerNickname": "玩家A",
    "protectionEndTime": "2025-12-25T15:30:00",
    "status": "ACTIVE",
    "canDraw": false,
    "cannotDrawReason": "商品正在被其他玩家抽獎中，請稍後再試"
  }
}
```

---

## ✅ 優點總結

### 1. 自動計算 maxDraws

| 優點 | 說明 |
|-----|------|
| 🎯 **資料一致性** | 後端自動計算，避免前端計算錯誤導致 maxDraws 與獎品總數不一致 |
| 🛡️ **業務邏輯集中** | 獎品數量驗證邏輯全部在後端，確保遊戲規則正確執行 |
| 🚀 **前端簡化** | 前端不需要手動計算總數，減少程式碼複雜度 |
| 🔄 **易於擴充** | 未來要支援謝謝惠顧（SCRATCH_MODE），只需修改後端計算邏輯 |

### 2. 統一 API 回應格式

| 優點 | 說明 |
|-----|------|
| 📦 **統一資料模型** | 前端只需處理一種資料結構，減少重複代碼 |
| 🔧 **易於維護** | 新增欄位時只需修改一個 DTO，自動同步到 List 和 Detail |
| ⚡ **更好的使用者體驗** | List 已包含完整資訊，點擊後可立即顯示，只需額外載入 tickets |
| 🔍 **更好的開發體驗** | TypeScript 類型定義統一，IDE 自動補全更準確 |

---

## 🧪 測試計畫

### 1. 測試自動計算 maxDraws

#### 測試案例 1：一番賞模式
```bash
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "lottery": {
      "title": "鬼滅一番賞測試",
      "category": "OFFICIAL_ICHIBAN",
      "pricePerDraw": 650,
      "playMode": "LOTTERY_MODE",
      "status": "OFF_SHELF"
    },
    "prizes": [
      { "name": "A賞", "level": "A", "quantity": 1 },
      { "name": "B賞", "level": "B", "quantity": 5 },
      { "name": "C賞", "level": "C", "quantity": 20 }
    ]
  }'

# 預期結果：
# - maxDraws 自動計算為 26 (1 + 5 + 20)
# - 生成 26 個籤位
# - totalDraws = 0, remainingDraws = 26
```

#### 測試案例 2：刮刮樂模式（支援謝謝惠顧）
```bash
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "lottery": {
      "title": "新年刮刮樂",
      "category": "CUSTOM_GACHA",
      "pricePerDraw": 100,
      "playMode": "SCRATCH_MODE",
      "maxDraws": 50,
      "status": "OFF_SHELF"
    },
    "prizes": [
      { "name": "頭獎", "level": "A", "quantity": 1 },
      { "name": "貳獎", "level": "B", "quantity": 9 }
    ]
  }'

# 預期結果：
# - maxDraws = 50（使用前端設定值）
# - 獎品總數 = 10 (1 + 9)
# - 謝謝惠顧 = 40 (50 - 10)
# - 生成 50 個籤位：10 個中獎 + 40 個謝謝惠顧
# - 日誌顯示：「刮刮樂模式：使用前端設定 maxDraws = 50（獎品 10 個 + 謝謝惠顧 40 個）」
```

#### 測試案例 2-2：刮刮樂沒傳 maxDraws（無謝謝惠顧）
```bash
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "lottery": {
      "title": "簡易刮刮樂",
      "category": "CUSTOM_GACHA",
      "pricePerDraw": 100,
      "playMode": "SCRATCH_MODE",
      "status": "OFF_SHELF"
    },
    "prizes": [
      { "name": "頭獎", "level": "A", "quantity": 1 },
      { "name": "貳獎", "level": "B", "quantity": 9 }
    ]
  }'

# 預期結果：
# - maxDraws 自動設為 10（獎品總數）
# - 謝謝惠顧 = 0
# - 生成 10 個籤位，全部中獎
# - 日誌顯示：「刮刮樂模式：前端未設定 maxDraws，預設 = 獎品總數 = 10（無謝謝惠顧）」
```

#### 測試案例 2-3：刮刮樂 maxDraws 小於獎品總數（錯誤）
```bash
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "lottery": {
      "title": "錯誤刮刮樂",
      "category": "CUSTOM_GACHA",
      "pricePerDraw": 100,
      "playMode": "SCRATCH_MODE",
      "maxDraws": 5,
      "status": "OFF_SHELF"
    },
    "prizes": [
      { "name": "頭獎", "level": "A", "quantity": 10 }
    ]
  }'

# 預期結果：
# - 返回錯誤：「刮刮樂模式錯誤：總抽數(5)不能小於獎品總數(10)！請調整設定。」
# - HTTP 400 Bad Request
```

#### 測試案例 3：一番賞前端傳入 maxDraws 應該被覆寫
```bash
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "lottery": {
      "title": "測試覆寫 maxDraws",
      "category": "OFFICIAL_ICHIBAN",
      "pricePerDraw": 650,
      "playMode": "LOTTERY_MODE",
      "maxDraws": 999,
      "status": "OFF_SHELF"
    },
    "prizes": [
      { "name": "A賞", "level": "A", "quantity": 5 }
    ]
  }'

# 預期結果：
# - maxDraws 被覆寫為 5（獎品總數），而非 999
# - 日誌應顯示：「⚠️ 一番賞模式：前端傳入 maxDraws=999 與獎品總數=5 不符，已自動覆寫」
# - 日誌應顯示：「✅ 已更新商品 maxDraws: lotteryId=xxx, maxDraws=5」
```

### 2. 測試統一 API 回應格式

#### 測試案例 1：List API 不包含 tickets
```bash
curl -X POST http://localhost:8080/api/lottery/browse/list \
  -H "Content-Type: application/json" \
  -d '{}'

# 預期結果：
# [
#   {
#     "lottery": { ... 完整商品資訊 },
#     "prizes": [ ... 獎品列表 ],
#     "tickets": null,
#     "session": null
#   }
# ]
```

#### 測試案例 2：Detail API 包含 tickets
```bash
curl -X GET http://localhost:8080/api/lottery/browse/<lottery-id>

# 預期結果：
# {
#   "lottery": { ... 完整商品資訊 },
#   "prizes": [ ... 獎品列表 ],
#   "tickets": [ ... 籤位列表 ],
#   "session": { ... 場次資訊 }
# }
```

#### 測試案例 3：驗證資料一致性
```javascript
// 前端測試腳本
const listResponse = await fetch('/api/lottery/browse/list', { method: 'POST' });
const list = await listResponse.json();

const detailResponse = await fetch(`/api/lottery/browse/${list.data[0].lottery.id}`);
const detail = await detailResponse.json();

// 驗證：List 和 Detail 的 lottery 欄位應該完全一致
assert.deepEqual(list.data[0].lottery, detail.data.lottery);
assert.deepEqual(list.data[0].prizes, detail.data.prizes);

// 驗證：List 沒有 tickets，Detail 有 tickets
assert.isNull(list.data[0].tickets);
assert.isArray(detail.data.tickets);
```

---

## 🚀 部署步驟

### 1. 編譯與打包
```bash
cd c:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin
mvn clean package -DskipTests
```

### 2. 備份與部署
```bash
# 上傳到 EC2
scp -i ourkuji.pem target\admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/admin-1.1.0.jar

# SSH 連線
ssh -i ourkuji.pem ec2-user@18.179.187.129

# 備份舊版本
cp admin-1.0.0.jar admin-1.0.0.backup.jar

# 替換新版本
mv admin-1.1.0.jar admin-1.0.0.jar

# 重啟服務
pkill -f admin-1.0.0.jar
nohup java -jar admin-1.0.0.jar > app.log 2>&1 &

# 檢查日誌
tail -f app.log
```

### 3. 健康檢查
```bash
# 檢查服務是否啟動
curl http://18.179.187.129:8080/actuator/health

# 檢查 API 回應
curl -X POST http://18.179.187.129:8080/api/lottery/browse/list \
  -H "Content-Type: application/json" \
  -d '{}'
```

---

## 📝 前端適配指南

### 1. 更新商品建立表單

**Before（舊架構）**：
```typescript
const handleSubmit = async () => {
  // ❌ 需要手動計算 maxDraws
  const totalQuantity = prizes.reduce((sum, p) => sum + p.quantity, 0);
  
  const response = await api.post('/admin/lottery-with-prizes', {
    lottery: {
      title: form.title,
      maxDraws: totalQuantity  // ← 手動計算
    },
    prizes: prizes
  });
};
```

**After（新架構）**：
```typescript
const handleSubmit = async () => {
  // ✅ 不需要計算 maxDraws，後端自動處理
  const response = await api.post('/admin/lottery-with-prizes', {
    lottery: {
      title: form.title
      // maxDraws 不用傳
    },
    prizes: prizes
  });
  
  // 後端自動計算並返回
  console.log('自動計算的 maxDraws:', response.data.data.maxDraws);
};
```

### 2. 更新列表頁面

**Before（舊架構）**：
```typescript
// ❌ 使用 LotteryListItemRes
interface LotteryListItemRes {
  id: string;
  title: string;
  imageUrl: string;
  pricePerDraw: number;
  remainingDraws: number;
  // ... 只有 13 個基本欄位
}

const LotteryList = () => {
  const [items, setItems] = useState<LotteryListItemRes[]>([]);
  
  useEffect(() => {
    api.post('/lottery/browse/list').then(res => {
      setItems(res.data.data);
    });
  }, []);
  
  return (
    <div>
      {items.map(item => (
        <div key={item.id}>
          <h3>{item.title}</h3>
          <img src={item.imageUrl} />
          <p>價格：{item.pricePerDraw}</p>
          {/* ❌ 無法顯示其他欄位，如 tags, theme, bonusEnabled */}
        </div>
      ))}
    </div>
  );
};
```

**After（新架構）**：
```typescript
// ✅ 使用統一的 LotteryDetailRes
interface LotteryDetailRes {
  lottery: Lottery;        // 完整商品資訊（30+ 欄位）
  prizes: Prize[];         // 獎品列表
  tickets: Ticket[] | null; // List 為 null
  session: Session | null;  // List 為 null
}

const LotteryList = () => {
  const [lotteries, setLotteries] = useState<LotteryDetailRes[]>([]);
  
  useEffect(() => {
    api.post('/lottery/browse/list').then(res => {
      setLotteries(res.data.data);
    });
  }, []);
  
  return (
    <div>
      {lotteries.map(item => (
        <div key={item.lottery.id}>
          <h3>{item.lottery.title}</h3>
          <img src={item.lottery.imageUrl} />
          <p>價格：{item.lottery.pricePerDraw}</p>
          
          {/* ✅ 可以顯示更多欄位 */}
          {item.lottery.bonusEnabled && <span>🎁 可賺紅利</span>}
          {item.lottery.tags?.map(tag => <span key={tag}>{tag}</span>)}
          <p>主題：{item.lottery.theme}</p>
          
          {/* ✅ 可以顯示獎品資訊 */}
          <div className="prizes">
            {item.prizes.map(prize => (
              <div key={prize.id}>
                <img src={prize.imageUrl} />
                <p>{prize.level}賞 - {prize.name}</p>
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
};
```

### 3. 更新詳情頁面

**Before（舊架構）**：
```typescript
const LotteryDetail = ({ id }) => {
  const [detail, setDetail] = useState<LotteryDetailRes | null>(null);
  
  useEffect(() => {
    // ❌ 需要重新載入完整資料
    api.get(`/lottery/browse/${id}`).then(res => {
      setDetail(res.data.data);
    });
  }, [id]);
  
  if (!detail) return <Loading />;
  
  return (
    <div>
      <h1>{detail.lottery.title}</h1>
      {/* ... 顯示詳情 */}
    </div>
  );
};
```

**After（新架構）**：
```typescript
const LotteryDetail = ({ lottery, id }) => {
  const [detail, setDetail] = useState<LotteryDetailRes>(lottery);  // ← 先使用列表資料
  const [loadingTickets, setLoadingTickets] = useState(false);
  
  useEffect(() => {
    // ✅ 先顯示列表資料，再額外載入 tickets
    setLoadingTickets(true);
    api.get(`/lottery/browse/${id}`).then(res => {
      setDetail(res.data.data);  // ← 更新為包含 tickets 的完整資料
      setLoadingTickets(false);
    });
  }, [id]);
  
  return (
    <div>
      {/* ✅ 立即顯示商品和獎品資訊 */}
      <h1>{detail.lottery.title}</h1>
      <div className="prizes">
        {detail.prizes.map(prize => (
          <div key={prize.id}>{prize.name}</div>
        ))}
      </div>
      
      {/* ✅ tickets 載入中顯示 loading */}
      {loadingTickets ? (
        <Loading />
      ) : (
        <div className="tickets">
          {detail.tickets?.map(ticket => (
            <div key={ticket.id}>籤位 {ticket.number}</div>
          ))}
        </div>
      )}
    </div>
  );
};
```

---

## 🔮 未來擴充計畫

### 1. ✅ 支援謝謝惠顧（SCRATCH_MODE）- 已完成

刮刮樂模式已支援謝謝惠顧功能：

```java
// 已實現的邏輯
if ("SCRATCH_MODE".equals(playMode)) {
    if (frontendMaxDraws != null && frontendMaxDraws >= totalPrizeQuantity) {
        calculatedMaxDraws = frontendMaxDraws;
        int thanksgivingCount = frontendMaxDraws - totalPrizeQuantity;
        // 生成謝謝惠顧籤位
    }
}
```

**使用方式**：
```javascript
// 前端建立刮刮樂時傳入 maxDraws
const response = await api.post('/admin/lottery-with-prizes', {
  lottery: {
    playMode: 'SCRATCH_MODE',
    maxDraws: 50  // 總共 50 抽
  },
  prizes: [
    { quantity: 2 }  // 只有 2 個中獎籤位
  ]
});
// 後端自動生成：2 個中獎 + 48 個謝謝惠顧
```

### 2. 支援動態調整 maxDraws

未來若需支援營運期間調整總抽數：

```java
// 新增 API：調整商品總抽數
@PutMapping("/admin/lottery/{id}/adjust-max-draws")
public ResponseEntity<Void> adjustMaxDraws(
        @PathVariable String id,
        @RequestBody AdjustMaxDrawsReq req) {
    
    // 驗證：新的 maxDraws 必須 >= 已抽出的數量
    Lottery lottery = lotteryMapper.selectByPrimaryKey(id);
    if (req.getNewMaxDraws() < lottery.getTotalDraws()) {
        throw new BusinessException("總抽數不能小於已抽出的數量");
    }
    
    // 計算需要新增的籤位數量
    int additionalTickets = req.getNewMaxDraws() - lottery.getMaxDraws();
    
    // 更新 maxDraws 並生成新籤位
    lottery.setMaxDraws(req.getNewMaxDraws());
    lotteryMapper.updateByPrimaryKey(lottery);
    
    // 生成額外的籤位
    lotteryTicketService.generateAdditionalTickets(id, additionalTickets);
    
    return ResponseEntity.ok().build();
}
```

---

## 📖 相關文件

- [Copilot 指南](./.github/copilot-instructions.md) - 專案整體架構說明
- [安全性修正文件](./SECURITY_FIX_TICKET_INFO_LEAK.md) - 籤位資訊洩漏修正
- [Response DTO 修正文件](./RESPONSE_DTO_FIELD_FIX_COMPLETE.md) - Response 欄位完整性修正

---

## ✅ 檢查清單

**部署前檢查**：
- [ ] 編譯無錯誤：`mvn clean package -DskipTests`
- [ ] 測試自動計算 maxDraws（一番賞模式）
- [ ] 測試自動計算 maxDraws（刮刮樂模式）
- [ ] 測試 List API 返回 LotteryDetailRes
- [ ] 驗證 List API 不包含 tickets
- [ ] 驗證 Detail API 包含 tickets
- [ ] 檢查日誌輸出正常

**部署後檢查**：
- [ ] 服務健康檢查通過
- [ ] List API 測試通過
- [ ] Detail API 測試通過
- [ ] 建立商品測試通過
- [ ] maxDraws 自動計算正確
- [ ] 前端顯示正常

---

**修改者**：GitHub Copilot  
**審核者**：（待填寫）  
**部署時間**：（待填寫）
