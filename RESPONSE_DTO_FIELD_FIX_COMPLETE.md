# Response DTO 欄位缺失修正完成報告

## 問題描述
用戶回報：`POST /api/admin/lottery-with-prizes` 的 **Response 缺少大量欄位**

**範例：**
```javascript
// Request 有 28 個欄位
maxDraws: 28
playMode: "SCRATCH_MODE"
tags: ["新品", "一番賞", "收藏"]
galleryImages: [...]
bonusEnabled: true
// ... 等等

// Response 卻只有 13 個欄位！
{
  "id": "...",
  "title": "...",
  "imageUrl": "...",
  "totalDraws": 0,
  "remainingDraws": 0,  // ← 還是 0，因為 maxDraws 沒回傳
  // ❌ 其他欄位都不見了！
}
```

## 根本原因
**3 個 DTO 都缺少欄位！**

1. ❌ `LotteryWithPrizesRes` - 只有 13 個欄位，缺少 20+ 個
2. ❌ `LotteryRes` - 缺少新增的欄位（playMode, tags, galleryImages 等）
3. ❌ `buildLotteryWithPrizesRes()` - 組裝時沒有設定這些欄位

## 修正內容

### 1. `LotteryWithPrizesRes.java` - 新增欄位

**原本只有（13 個）：**
```java
- id, storeId, storeName, title, description, imageUrl
- category, subCategory, pricePerDraw, discountedPrice
- autoDiscountEnabled, totalDraws, remainingDraws
- status, scheduledAt, createdAt, updatedAt
- prizes[], totalPrizeCount, remainingPrizeCount, progressPercentage
```

**新增（20+ 個）：**
```java
✅ playMode              // 遊戲模式
✅ allowMultiDraw        // 是否允許多抽
✅ multiDrawOptions      // 多抽選項 [10, 50]
✅ bonusEnabled          // 是否啟用紅利
✅ bonusPointsPerDraw    // 每抽贈送紅利
✅ bonusCostPerDraw      // 每抽消耗紅利
✅ tags                  // 標籤 ["新品", "一番賞"]
✅ galleryImages         // 圖庫 ["url1", "url2"]
✅ theme                 // 主題 "鬼滅之刃"
✅ hotCount              // 熱門度
✅ orderNum              // 排序
✅ remark                // 備註
✅ content               // 詳細內容（HTML）
✅ startTime             // 開始時間
✅ endTime               // 結束時間
✅ maxDraws              // ← 最重要！總抽數限制
```

### 2. `LotteryRes.java` - 新增欄位

**新增：**
```java
✅ playMode              // 遊戲模式
✅ bonusEnabled          // 是否啟用紅利
✅ bonusPointsPerDraw    // 每抽贈送紅利
✅ bonusCostPerDraw      // 每抽消耗紅利
✅ tags                  // 標籤列表
✅ galleryImages         // 圖庫列表
✅ theme                 // 主題
✅ hotCount              // 熱門度
```

### 3. `LotteryServiceImpl.convertToResNew()` - 設定欄位

**新增：**
```java
// Line 1015: playMode
res.setPlayMode(lottery.getPlayMode());

// Line 1037-1070: 紅利、標籤、圖庫
res.setBonusEnabled(lottery.getBonusEnabled());
res.setBonusPointsPerDraw(lottery.getBonusPointsPerDraw());
res.setBonusCostPerDraw(lottery.getBonusCostPerDraw());

// JSON 反序列化
if (lottery.getTags() != null) {
    List<String> tagList = objectMapper.readValue(lottery.getTags(), ...);
    res.setTags(tagList);
}

if (lottery.getGalleryImages() != null) {
    List<String> imageList = objectMapper.readValue(lottery.getGalleryImages(), ...);
    res.setGalleryImages(imageList);
}

res.setTheme(lottery.getTheme());
res.setHotCount(lottery.getHotCount());
```

### 4. `LotteryServiceImpl.buildLotteryWithPrizesRes()` - 組裝回應

**修改前（只設定 13 個欄位）：**
```java
return LotteryWithPrizesRes.builder()
    .id(...)
    .title(...)
    .totalDraws(...)
    .remainingDraws(...)
    // ❌ 其他欄位都沒設定
    .build();
```

**修改後（設定所有欄位）：**
```java
return LotteryWithPrizesRes.builder()
    .id(lotteryRes.getId())
    .title(lotteryRes.getTitle())
    // ... 基本欄位 ...
    .playMode(lotteryRes.getPlayMode())                     // ✅ 新增
    .allowMultiDraw(lotteryRes.getAllowMultiDraw())         // ✅ 新增
    .multiDrawOptions(lotteryRes.getMultiDrawOptions())     // ✅ 新增
    .bonusEnabled(lotteryRes.getBonusEnabled())             // ✅ 新增
    .bonusPointsPerDraw(lotteryRes.getBonusPointsPerDraw()) // ✅ 新增
    .bonusCostPerDraw(lotteryRes.getBonusCostPerDraw())     // ✅ 新增
    .tags(lotteryRes.getTags())                             // ✅ 新增
    .galleryImages(lotteryRes.getGalleryImages())           // ✅ 新增
    .theme(lotteryRes.getTheme())                           // ✅ 新增
    .hotCount(lotteryRes.getHotCount())                     // ✅ 新增
    .orderNum(lotteryRes.getOrderNum())                     // ✅ 新增
    .remark(lotteryRes.getRemark())                         // ✅ 新增
    .content(lotteryRes.getContent())                       // ✅ 新增
    .startTime(lotteryRes.getStartTime())                   // ✅ 新增
    .endTime(lotteryRes.getEndTime())                       // ✅ 新增
    .maxDraws(lotteryRes.getMaxDraws())                     // ✅ 新增（最重要！）
    .totalDraws(lotteryRes.getTotalDraws())
    .remainingDraws(lotteryRes.getRemainingDraws())         // ← 現在會正確計算
    .build();
```

## 測試驗證

### 修正前
```json
{
  "totalDraws": 0,
  "remainingDraws": 0,  // ❌ 錯誤：應該是 28
  // ❌ maxDraws 欄位不見了
  // ❌ playMode 欄位不見了
  // ❌ tags 欄位不見了
  // ❌ 所有新欄位都不見了
}
```

### 修正後（預期）
```json
{
  "maxDraws": 28,               // ✅ 正確回傳
  "totalDraws": 0,              // ✅ 已抽 0 次
  "remainingDraws": 28,         // ✅ 剩餘 28 次（28 - 0）
  "playMode": "SCRATCH_MODE",   // ✅ 遊戲模式
  "tags": ["新品", "一番賞"],    // ✅ 標籤
  "galleryImages": [...],       // ✅ 圖庫
  "bonusEnabled": true,         // ✅ 紅利啟用
  "theme": "鬼滅之刃",           // ✅ 主題
  "hotCount": 0,                // ✅ 熱門度
  "orderNum": 1,                // ✅ 排序
  "startTime": "...",           // ✅ 開始時間
  "endTime": "...",             // ✅ 結束時間
  // ... 所有欄位都正確回傳
}
```

## 完整修正檔案清單

| 檔案 | 修正內容 | 狀態 |
|------|---------|------|
| `LotteryWithPrizesRes.java` | 新增 20+ 個欄位定義 | ✅ 完成 |
| `LotteryRes.java` | 新增 8 個欄位定義 | ✅ 完成 |
| `LotteryServiceImpl.java` (Line 990-1090) | `convertToResNew()` 設定新欄位 | ✅ 完成 |
| `LotteryServiceImpl.java` (Line 1482-1520) | `buildLotteryWithPrizesRes()` 組裝新欄位 | ✅ 完成 |

## 資料流程圖

```
Request (前端)
    ↓
LotteryCreateReq (28 欄位)
    ↓
LotteryServiceImpl.createLottery()
    ├─ 設定所有欄位到 Entity ✅ (已修正)
    ↓
Lottery Entity (寫入資料庫)
    ↓
LotteryServiceImpl.getLotteryById()
    ├─ 從資料庫讀取 ✅
    ├─ convertToResNew() 轉換 ✅ (已修正)
    ↓
LotteryRes (包含所有欄位) ✅ (已修正)
    ↓
buildLotteryWithPrizesRes()
    ├─ 組裝所有欄位 ✅ (已修正)
    ↓
LotteryWithPrizesRes (完整回應) ✅ (已修正)
    ↓
Response (回傳前端) ✅
```

## 下一步

1. ✅ 編譯專案
   ```bash
   mvn clean package -DskipTests
   ```

2. ⏳ 重啟服務並測試
   ```bash
   # 測試 CREATE API
   POST /api/admin/lottery-with-prizes
   
   # 檢查 Response 包含所有欄位：
   - maxDraws ✅
   - remainingDraws (應該等於 maxDraws) ✅
   - playMode ✅
   - tags ✅
   - galleryImages ✅
   - bonusEnabled ✅
   - theme ✅
   - 所有其他欄位 ✅
   ```

3. ⏳ 測試 UPDATE API
   ```bash
   PUT /api/admin/lottery-with-prizes/{id}
   
   # 確認可以更新所有欄位
   ```

---

**修正時間：** 2026-01-29 02:45  
**修正人員：** AI Assistant  
**問題嚴重度：** 🔴 Critical（API 回應資料不完整）  
**修正狀態：** ✅ 完成（等待編譯測試）
