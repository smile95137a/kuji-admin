# 商品請求欄位增強與權重移除報告

## 修改日期
2026-01-19

## 修改摘要

### ✅ 已完成的修改

#### 1. LotteryCreateReq 欄位更新
**檔案**：`src/main/java/com/group/admin/req/lottery/LotteryCreateReq.java`

**移除欄位**：
- ❌ `weight` - 推薦權重（不再使用）

**新增欄位**：
```java
// 遊玩模式
private String playMode;           // LOTTERY_MODE / SCRATCH_MODE

// 商品狀態
private String status;             // DRAFT / ON_SHELF / OFF_SHELF

// 熱門程度
private Integer hotCount;          // 熱門標籤顯示數值

// 商品主題分類
private String theme;              // 火影忍者、航海王、鬼滅之刃等

// 商品圖集
private List<String> galleryImages; // 多張圖片 URL

// 商品詳細內容
private String content;            // HTML 格式內容

// 標籤列表
private List<String> tags;         // 標籤陣列

// 紅利點數相關
private Boolean bonusEnabled;       // 是否啟用紅利
private Integer bonusPointsPerDraw; // 每抽贈送點數
private Integer bonusCostPerDraw;   // 每抽消耗點數
```

#### 2. LotteryPrizeCreateReq 驗證移除
**檔案**：`src/main/java/com/group/admin/req/lottery/LotteryPrizeCreateReq.java`

**修改**：
- ❌ 移除 `@NotBlank` 驗證於 `lotteryId` 欄位
- ✅ 整合創建時由後端自動帶入
- ❌ 移除 `weight` 欄位

**原因**：整合 API (`/admin/lottery-with-prizes`) 創建時，後端會自動設定 `lotteryId`

#### 3. 抽獎機率邏輯修改
**檔案**：`src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java`

**舊邏輯（權重制）**：
```java
// 機率 = (剩餘數量 × 權重) / 總權重
long weight = remaining * prize.getWeight();
```

**新邏輯（等機率制）**：
```java
// 機率 = 剩餘數量 / 總剩餘數量
long probability = prize.getRemaining();
```

**範例說明**：
| 獎項 | 剩餘數量 | 舊機率（假設權重=1） | 新機率 |
|------|----------|----------------------|--------|
| A 賞 | 1        | 1/4 = 25%            | 1/4 = 25% |
| B 賞 | 3        | 3/4 = 75%            | 3/4 = 75% |

**結論**：當權重都是 1 時，兩者等價。現在直接使用剩餘數量，簡化邏輯。

#### 4. Service 層 weight 移除
**修改位置**：
1. `createLottery()` - 移除 `lottery.setWeight()`
2. `updateLottery()` - 移除 `req.getWeight()` 檢查
3. `createLotteryWithPrizes()` - 移除 `prize.setWeight()`
4. `selectPrize()` - 移除權重計算，改用剩餘數量

---

## ⚠️ 待處理項目

### 1. Entity 欄位補充
**問題**：`Lottery` Entity 沒有新增的欄位

**需要新增的欄位**：
```java
// Lottery.java
private String playMode;
private Integer hotCount;
private String theme;
private String galleryImages;    // JSON 陣列字串
private String content;
private String tags;             // JSON 陣列字串
private Byte bonusEnabled;
private Integer bonusPointsPerDraw;
private Integer bonusCostPerDraw;
```

**資料庫對應**：
```sql
ALTER TABLE lottery
ADD COLUMN play_mode VARCHAR(20) COMMENT '遊玩模式：LOTTERY_MODE/SCRATCH_MODE',
ADD COLUMN hot_count INT DEFAULT 0 COMMENT '熱門程度',
ADD COLUMN theme VARCHAR(100) COMMENT '商品主題分類',
ADD COLUMN gallery_images TEXT COMMENT '商品圖集（JSON陣列）',
ADD COLUMN content TEXT COMMENT '商品詳細內容（HTML）',
ADD COLUMN tags VARCHAR(500) COMMENT '標籤列表（JSON陣列）',
ADD COLUMN bonus_enabled TINYINT(1) DEFAULT 0 COMMENT '是否啟用紅利',
ADD COLUMN bonus_points_per_draw INT DEFAULT 0 COMMENT '每抽贈送點數',
ADD COLUMN bonus_cost_per_draw INT DEFAULT 0 COMMENT '每抽消耗點數';
```

### 2. Service 層映射補充
**檔案**：`LotteryServiceImpl.java`

**需要補充的映射**：
```java
// createLottery()
lottery.setPlayMode(req.getPlayMode());
lottery.setHotCount(req.getHotCount() != null ? req.getHotCount() : 0);
lottery.setTheme(req.getTheme());

// galleryImages
if (req.getGalleryImages() != null) {
    lottery.setGalleryImages(objectMapper.writeValueAsString(req.getGalleryImages()));
}

// content
lottery.setContent(req.getContent());

// tags
if (req.getTags() != null) {
    lottery.setTags(objectMapper.writeValueAsString(req.getTags()));
}

// bonus
lottery.setBonusEnabled(req.getBonusEnabled() != null && req.getBonusEnabled() ? (byte) 1 : (byte) 0);
lottery.setBonusPointsPerDraw(req.getBonusPointsPerDraw() != null ? req.getBonusPointsPerDraw() : 0);
lottery.setBonusCostPerDraw(req.getBonusCostPerDraw() != null ? req.getBonusCostPerDraw() : 0);
```

### 3. 清理舊的 Controller/Service
**檔案需要刪除**：
1. ❌ `AdminLotteryController.java` - 單獨商品 Controller（已有整合版）
2. ❌ `LotteryPrizeController.java` - 單獨獎項 Controller（已有整合版）

**保留檔案**：
✅ `AdminLotteryWithPrizesController.java` - 整合 Controller（商品+獎項）

**理由**：整合 API 一次完成商品+獎項操作，簡化前端呼叫

---

## 測試建議

### 1. 測試整合創建 API
```json
POST /api/admin/lottery-with-prizes
{
  "lottery": {
    "title": "鬼滅之刃一番賞 324",
    "category": "CUSTOM_GACHA",
    "pricePerDraw": 200,
    "maxDraws": 100,
    "playMode": "LOTTERY_MODE",
    "status": "DRAFT",
    "hotCount": 999,
    "theme": "鬼滅之刃",
    "imageUrl": "https://example.com/images/mock-lottery.jpg",
    "galleryImages": [
      "https://example.com/images/mock-lottery-1.jpg",
      "https://example.com/images/mock-lottery-2.jpg"
    ],
    "description": "測試商品描述",
    "content": "【活動說明】\n- 單抽 / 多抽（10、50）",
    "discountedPrice": 100,
    "autoDiscountEnabled": true,
    "allowMultiDraw": true,
    "multiDrawOptions": [10, 50],
    "tags": ["鬼滅之刃", "一番賞", "熱門"],
    "bonusEnabled": true,
    "bonusPointsPerDraw": 10,
    "bonusCostPerDraw": 200
  },
  "prizes": [
    {
      "name": "獎品 A",
      "quantity": 1,
      "level": "A",
      "isGrandPrize": true
    },
    {
      "name": "獎品 B",
      "quantity": 3,
      "level": "B"
    }
  ]
}
```

### 2. 驗證機率計算
**測試場景**：建立商品後，執行多次抽獎

**預期結果**：
- A 賞 1 個 → 1/4 = 25% 機率
- B 賞 3 個 → 3/4 = 75% 機率

**驗證方法**：
```bash
# 執行 100 次抽獎
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/draw \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"lotteryId": "xxx"}'
done

# 統計結果分布
# A 賞應約 25 次
# B 賞應約 75 次
```

---

## 下一步行動

### 優先順序 P0（立即執行）
1. ✅ 執行資料庫 Migration（新增欄位）
2. ✅ 更新 Lottery Entity（新增 getter/setter）
3. ✅ 更新 LotteryServiceImpl 映射邏輯
4. ✅ 測試整合創建 API

### 優先順序 P1（建議執行）
1. ❌ 刪除 AdminLotteryController.java
2. ❌ 刪除 LotteryPrizeController.java
3. ✅ 更新 API 文件（移除舊 API 說明）

### 優先順序 P2（可選執行）
1. ✅ 前端更新表單（補充新欄位）
2. ✅ 補充主題分類枚舉
3. ✅ 機率計算單元測試

---

## 風險評估

### 低風險 ✅
- 移除 weight 欄位：原本都設為 1，移除後邏輯等價
- 新增欄位：都是可選欄位，不影響現有功能

### 中風險 ⚠️
- Entity 欄位缺失：需要資料庫 Migration
- Service 映射缺失：需要補充程式碼

### 高風險 ❌
- 無

---

## 附錄：完整欄位對照表

| 前端 JSON Key | LotteryCreateReq | Lottery Entity | 資料庫欄位 | 備註 |
|---------------|------------------|----------------|-----------|------|
| title | title | title | title | ✅ 已存在 |
| category | category | category | category | ✅ 已存在 |
| pricePerDraw | pricePerDraw | pricePerDraw | price_per_draw | ✅ 已存在 |
| playMode | playMode | playMode | play_mode | ❌ 需新增 |
| status | status | status | status | ✅ 已存在 |
| hotCount | hotCount | hotCount | hot_count | ❌ 需新增 |
| theme | theme | theme | theme | ❌ 需新增 |
| imageUrl | imageUrl | imageUrl | image_url | ✅ 已存在 |
| galleryImages | galleryImages | galleryImages | gallery_images | ❌ 需新增 |
| description | description | description | description | ✅ 已存在 |
| content | content | content | content | ❌ 需新增 |
| tags | tags | tags | tags | ❌ 需新增 |
| bonusEnabled | bonusEnabled | bonusEnabled | bonus_enabled | ❌ 需新增 |
| bonusPointsPerDraw | bonusPointsPerDraw | bonusPointsPerDraw | bonus_points_per_draw | ❌ 需新增 |
| bonusCostPerDraw | bonusCostPerDraw | bonusCostPerDraw | bonus_cost_per_draw | ❌ 需新增 |

---

## 完成報告

**修改人員**：GitHub Copilot  
**修改時間**：2026-01-19  
**修改狀態**：部分完成（Req 更新 ✅、Entity 待補充 ⚠️、Service 待補充 ⚠️）

**下次會議議程**：
1. 確認資料庫 Schema 是否需要新增欄位
2. 討論商品主題分類的枚舉值
3. 確認是否刪除舊的 Controller
