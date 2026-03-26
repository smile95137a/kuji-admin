# Lottery CRUD 欄位對應完整檢查清單

## 問題發現
用戶回報：**Request 傳入的欄位沒有正確儲存到資料庫**
- 範例：`maxDraws: 28` → Response 回傳 `totalDraws: 0, remainingDraws: 0`
- 範例：`status: "OFF_SHELF"` → 沒有更新到資料庫

## 修正記錄

### ✅ 已修正 - `LotteryServiceImpl.createLottery()`
**新增缺少的欄位設定：**
```java
// Line 93: 新增
lottery.setBonusEnabled(req.getBonusEnabled());

// Line 104-127: 新增 JSON 序列化欄位
if (req.getTags() != null) {
    lottery.setTags(objectMapper.writeValueAsString(req.getTags()));
}
if (req.getGalleryImages() != null) {
    lottery.setGalleryImages(objectMapper.writeValueAsString(req.getGalleryImages()));
}

// Line 136-141: 新增其他欄位
lottery.setPlayMode(req.getPlayMode());
lottery.setHotCount(req.getHotCount());
lottery.setTheme(req.getTheme());
lottery.setBonusPointsPerDraw(req.getBonusPointsPerDraw());
lottery.setBonusCostPerDraw(req.getBonusCostPerDraw());
```

### ✅ 已修正 - `LotteryServiceImpl.updateLottery()`
**新增缺少的欄位更新：**
```java
// Line 147: 新增
if (req.getBonusEnabled() != null) lottery.setBonusEnabled(req.getBonusEnabled());

// Line 157-169: 新增 JSON 序列化欄位
if (req.getTags() != null) {
    lottery.setTags(objectMapper.writeValueAsString(req.getTags()));
}
if (req.getGalleryImages() != null) {
    lottery.setGalleryImages(objectMapper.writeValueAsString(req.getGalleryImages()));
}

// Line 178-179: 新增其他欄位
if (req.getPlayMode() != null) lottery.setPlayMode(req.getPlayMode());
if (req.getStatus() != null) lottery.setStatus(req.getStatus());
```

### ✅ 已修正 - `LotteryUpdateReq.java`
**新增缺少的欄位定義：**
```java
private Boolean bonusEnabled;
private List<String> tags;
private List<String> galleryImages;
private String playMode;
private String status;
```

## 完整欄位對應表

### Request → Entity 對應（CREATE）

| Req 欄位 | Entity 欄位 | 類型轉換 | 狀態 |
|----------|------------|---------|------|
| storeId | storeId | String | ✅ |
| title | title | String | ✅ |
| description | description | String | ✅ |
| imageUrl | imageUrl | String | ✅ |
| category | category | String | ✅ |
| subCategory | subCategory | String | ✅ |
| pricePerDraw | pricePerDraw | Long | ✅ |
| discountedPrice | discountedPrice | Long | ✅ |
| autoDiscountEnabled | autoDiscountEnabled | Boolean → byte | ✅ |
| allowMultiDraw | allowMultiDraw | Boolean → byte | ✅ |
| bonusEnabled | bonusEnabled | Boolean | ✅ 已修正 |
| multiDrawOptions | multiDrawOptions | List<Integer> → JSON String | ✅ |
| tags | tags | List<String> → JSON String | ✅ 已修正 |
| galleryImages | galleryImages | List<String> → JSON String | ✅ 已修正 |
| scheduledAt | scheduledAt | LocalDateTime | ✅ |
| startTime | startTime | LocalDateTime | ✅ |
| endTime | endTime | LocalDateTime | ✅ |
| maxDraws | maxDraws | Integer | ✅ |
| playMode | playMode | String | ✅ 已修正 |
| status | status | String | ✅ |
| orderNum | orderNum | Integer | ✅ |
| hotCount | hotCount | Integer | ✅ 已修正 |
| theme | theme | String | ✅ 已修正 |
| content | description | String | ✅ (使用 description 欄位) |
| bonusPointsPerDraw | bonusPointsPerDraw | Integer | ✅ 已修正 |
| bonusCostPerDraw | bonusCostPerDraw | Integer | ✅ 已修正 |
| remark | remark | String | ✅ |

### Entity → Response 對應（READ）

| Entity 欄位 | Response 欄位 | 類型轉換 | 狀態 |
|------------|--------------|---------|------|
| id | id | String | ✅ |
| storeId | storeId | String | ✅ |
| title | title | String | ✅ |
| description | description | String | ✅ |
| imageUrl | imageUrl | String | ✅ |
| category | category | String | ✅ |
| subCategory | subCategory | String | ✅ |
| pricePerDraw | pricePerDraw | Long | ✅ |
| discountedPrice | discountedPrice | Long | ✅ |
| autoDiscountEnabled | autoDiscountEnabled | byte → Boolean | ✅ |
| allowMultiDraw | allowMultiDraw | byte → Boolean | ✅ |
| bonusEnabled | bonusEnabled | Boolean | ✅ |
| multiDrawOptions | multiDrawOptions | JSON String → List<Integer> | ✅ |
| tags | tags | JSON String → List<String> | ⚠️ 待驗證 |
| galleryImages | galleryImages | JSON String → List<String> | ⚠️ 待驗證 |
| scheduledAt | scheduledAt | LocalDateTime | ✅ |
| startTime | startTime | LocalDateTime | ✅ |
| endTime | endTime | LocalDateTime | ✅ |
| maxDraws | maxDraws | Integer | ✅ |
| totalDraws | totalDraws | Integer | ✅ |
| (計算) | remainingDraws | maxDraws - totalDraws | ✅ |
| playMode | playMode | String | ⚠️ 待驗證 |
| status | status | String | ✅ |
| orderNum | orderNum | Integer | ✅ |
| hotCount | hotCount | Integer | ⚠️ 待驗證 |
| theme | theme | String | ⚠️ 待驗證 |
| bonusPointsPerDraw | bonusPointsPerDraw | Integer | ⚠️ 待驗證 |
| bonusCostPerDraw | bonusCostPerDraw | Integer | ⚠️ 待驗證 |
| createdAt | createdAt | LocalDateTime | ✅ |
| updatedAt | updatedAt | LocalDateTime | ✅ |
| remark | remark | String | ✅ |

## Response 特殊欄位說明

### totalDraws vs remainingDraws
```java
// ❌ 錯誤理解
totalDraws = 總共可以抽幾次 (maxDraws)

// ✅ 正確理解
totalDraws = 已經抽了幾次 (初始值 = 0，每次抽獎 +1)
remainingDraws = 剩餘可抽次數 (maxDraws - totalDraws)
```

**範例：**
```json
{
  "maxDraws": 28,        // 設定總共可以抽 28 次
  "totalDraws": 0,       // 目前已經抽了 0 次 ✅
  "remainingDraws": 28   // 剩餘 28 次可抽 ✅
}
```

## 測試檢查清單

### CREATE API (`POST /api/admin/lottery-with-prizes`)
- [ ] Request 包含所有欄位
- [ ] Response 的 `totalDraws` = 0
- [ ] Response 的 `remainingDraws` = Request 的 `maxDraws`
- [ ] Response 的 `status` = Request 的 `status`
- [ ] Response 的 `playMode` = Request 的 `playMode`
- [ ] Response 的 `tags` = Request 的 `tags`
- [ ] Response 的 `galleryImages` = Request 的 `galleryImages`
- [ ] Response 的其他欄位都與 Request 一致

### UPDATE API (`PUT /api/admin/lottery-with-prizes/{id}`)
- [ ] 可以更新 `status`
- [ ] 可以更新 `playMode`
- [ ] 可以更新 `tags`
- [ ] 可以更新 `galleryImages`
- [ ] 可以更新 `bonusEnabled`
- [ ] Response 回傳最新的資料

### READ API (`GET /api/admin/lottery-with-prizes/{id}`)
- [ ] Response 包含所有欄位
- [ ] `remainingDraws` 計算正確
- [ ] JSON 欄位正確反序列化

## 下一步

1. ✅ 編譯專案
   ```bash
   mvn clean package -DskipTests
   ```

2. ⏳ 重啟服務
   ```bash
   # 上傳 JAR
   scp -i ourkuji.pem target\admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/
   
   # SSH 登入並重啟
   ssh -i ourkuji.pem ec2-user@18.179.187.129
   pkill -f admin-1.0.0.jar
   nohup java -jar admin-1.0.0.jar > app.log 2>&1 &
   exit
   ```

3. ⏳ 測試 CREATE API
   - 確認 `maxDraws: 28` → `remainingDraws: 28`
   - 確認 `status: "OFF_SHELF"` 正確儲存
   - 確認 `tags`, `galleryImages`, `playMode` 等欄位都正確

4. ⏳ 測試 UPDATE API
   - 確認可以更新所有欄位

---

**修正時間：** 2026-01-29 02:30
**修正內容：** 補齊 CREATE 和 UPDATE 方法中缺少的欄位設定
