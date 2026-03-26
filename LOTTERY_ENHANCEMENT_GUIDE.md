# 商品功能完整化操作指南

## 📋 修改概覽

### 已完成 ✅
1. ✅ `LotteryCreateReq` - 移除 weight，新增 9 個欄位
2. ✅ `LotteryPrizeCreateReq` - 移除 lotteryId 驗證和 weight
3. ✅ `LotteryServiceImpl.selectPrize()` - 改為 1/剩餘數量 機率
4. ✅ `LotteryServiceImpl` - 移除所有 weight 設定邏輯
5. ✅ 創建資料庫 Migration SQL

### 待執行 ⏳
1. ⏳ 執行資料庫 Migration
2. ⏳ 更新 Lottery Entity
3. ⏳ 更新 LotteryMapper.xml
4. ⏳ 更新 LotteryServiceImpl 映射
5. ⏳ 刪除舊的 Controller

---

## 🚀 操作步驟

### Step 1: 執行資料庫 Migration

**方法 A：使用 MySQL Workbench（推薦）**
```sql
-- 1. 開啟 migration-lottery-enhancement.sql
-- 2. 連接到 kuji 資料庫
-- 3. 執行 ALTER TABLE 語句
-- 4. 查看驗證結果
```

**方法 B：使用命令列**
```bash
mysql -h database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com \
      -u admin -p \
      kuji < migration-lottery-enhancement.sql
```

**驗證結果**：
```sql
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'kuji' 
AND TABLE_NAME = 'lottery' 
AND COLUMN_NAME IN ('play_mode', 'hot_count', 'theme', 'gallery_images', 'content', 'tags', 'bonus_enabled', 'bonus_points_per_draw', 'bonus_cost_per_draw');
```

應該看到 9 個欄位！

---

### Step 2: 更新 Lottery Entity

**檔案**：`src/main/java/com/group/admin/entity/Lottery.java`

**需要新增的欄位**：

```java
// 在 private String remark; 後面新增：

/**
 * 遊玩模式
 */
private String playMode;

/**
 * 熱門程度
 */
private Integer hotCount;

/**
 * 商品主題分類
 */
private String theme;

/**
 * 商品圖集（JSON 陣列字串）
 */
private String galleryImages;

/**
 * 商品詳細內容（HTML）
 */
private String content;

/**
 * 標籤列表（JSON 陣列字串）
 */
private String tags;

/**
 * 是否啟用紅利點數
 */
private Byte bonusEnabled;

/**
 * 每抽贈送紅利點數
 */
private Integer bonusPointsPerDraw;

/**
 * 每抽消耗紅利點數
 */
private Integer bonusCostPerDraw;

// Getter and Setter
public String getPlayMode() {
    return playMode;
}

public void setPlayMode(String playMode) {
    this.playMode = playMode == null ? null : playMode.trim();
}

public Integer getHotCount() {
    return hotCount;
}

public void setHotCount(Integer hotCount) {
    this.hotCount = hotCount;
}

public String getTheme() {
    return theme;
}

public void setTheme(String theme) {
    this.theme = theme == null ? null : theme.trim();
}

public String getGalleryImages() {
    return galleryImages;
}

public void setGalleryImages(String galleryImages) {
    this.galleryImages = galleryImages == null ? null : galleryImages.trim();
}

public String getContent() {
    return content;
}

public void setContent(String content) {
    this.content = content == null ? null : content.trim();
}

public String getTags() {
    return tags;
}

public void setTags(String tags) {
    this.tags = tags == null ? null : tags.trim();
}

public Byte getBonusEnabled() {
    return bonusEnabled;
}

public void setBonusEnabled(Byte bonusEnabled) {
    this.bonusEnabled = bonusEnabled;
}

public Integer getBonusPointsPerDraw() {
    return bonusPointsPerDraw;
}

public void setBonusPointsPerDraw(Integer bonusPointsPerDraw) {
    this.bonusPointsPerDraw = bonusPointsPerDraw;
}

public Integer getBonusCostPerDraw() {
    return bonusCostPerDraw;
}

public void setBonusCostPerDraw(Integer bonusCostPerDraw) {
    this.bonusCostPerDraw = bonusCostPerDraw;
}
```

---

### Step 3: 更新 LotteryMapper.xml

**檔案**：`src/main/resources/mapper/LotteryMapper.xml`

**找到 `<resultMap id="BaseResultMap">`，新增欄位映射**：

```xml
<!-- 在 <result column="remark" /> 後面新增： -->
<result column="play_mode" jdbcType="VARCHAR" property="playMode" />
<result column="hot_count" jdbcType="INTEGER" property="hotCount" />
<result column="theme" jdbcType="VARCHAR" property="theme" />
<result column="gallery_images" jdbcType="LONGVARCHAR" property="galleryImages" />
<result column="content" jdbcType="LONGVARCHAR" property="content" />
<result column="tags" jdbcType="VARCHAR" property="tags" />
<result column="bonus_enabled" jdbcType="TINYINT" property="bonusEnabled" />
<result column="bonus_points_per_draw" jdbcType="INTEGER" property="bonusPointsPerDraw" />
<result column="bonus_cost_per_draw" jdbcType="INTEGER" property="bonusCostPerDraw" />
```

**找到 `<sql id="Base_Column_List">`，新增欄位**：

```xml
<!-- 在最後新增： -->
, play_mode, hot_count, theme, gallery_images, content, tags, 
bonus_enabled, bonus_points_per_draw, bonus_cost_per_draw
```

**找到 `<insert>`，新增欄位**：

```xml
<!-- 在 VALUES 中新增： -->
#{playMode,jdbcType=VARCHAR}, #{hotCount,jdbcType=INTEGER}, #{theme,jdbcType=VARCHAR},
#{galleryImages,jdbcType=LONGVARCHAR}, #{content,jdbcType=LONGVARCHAR}, #{tags,jdbcType=VARCHAR},
#{bonusEnabled,jdbcType=TINYINT}, #{bonusPointsPerDraw,jdbcType=INTEGER}, #{bonusCostPerDraw,jdbcType=INTEGER}
```

---

### Step 4: 更新 LotteryServiceImpl 映射

**檔案**：`src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java`

**在 `createLottery()` 方法中，`lottery.setRemark()` 後面新增**：

```java
// 新增欄位映射
lottery.setPlayMode(req.getPlayMode() != null ? req.getPlayMode() : "LOTTERY_MODE");
lottery.setHotCount(req.getHotCount() != null ? req.getHotCount() : 0);
lottery.setTheme(req.getTheme());

// galleryImages
if (req.getGalleryImages() != null && !req.getGalleryImages().isEmpty()) {
    try {
        lottery.setGalleryImages(objectMapper.writeValueAsString(req.getGalleryImages()));
    } catch (JsonProcessingException e) {
        log.warn("商品圖集序列化失敗", e);
    }
}

// content
lottery.setContent(req.getContent());

// tags
if (req.getTags() != null && !req.getTags().isEmpty()) {
    try {
        lottery.setTags(objectMapper.writeValueAsString(req.getTags()));
    } catch (JsonProcessingException e) {
        log.warn("標籤列表序列化失敗", e);
    }
}

// bonus
lottery.setBonusEnabled(req.getBonusEnabled() != null && req.getBonusEnabled() ? (byte) 1 : (byte) 0);
lottery.setBonusPointsPerDraw(req.getBonusPointsPerDraw() != null ? req.getBonusPointsPerDraw() : 0);
lottery.setBonusCostPerDraw(req.getBonusCostPerDraw() != null ? req.getBonusCostPerDraw() : 0);
```

**在 `updateLottery()` 方法中，`if (req.getRemark() != null)` 後面新增**：

```java
// 新增欄位更新
if (req.getPlayMode() != null) lottery.setPlayMode(req.getPlayMode());
if (req.getHotCount() != null) lottery.setHotCount(req.getHotCount());
if (req.getTheme() != null) lottery.setTheme(req.getTheme());

// galleryImages
if (req.getGalleryImages() != null) {
    try {
        lottery.setGalleryImages(objectMapper.writeValueAsString(req.getGalleryImages()));
    } catch (JsonProcessingException e) {
        log.warn("商品圖集序列化失敗", e);
    }
}

// content
if (req.getContent() != null) lottery.setContent(req.getContent());

// tags
if (req.getTags() != null) {
    try {
        lottery.setTags(objectMapper.writeValueAsString(req.getTags()));
    } catch (JsonProcessingException e) {
        log.warn("標籤列表序列化失敗", e);
    }
}

// bonus
if (req.getBonusEnabled() != null) {
    lottery.setBonusEnabled(req.getBonusEnabled() ? (byte) 1 : (byte) 0);
}
if (req.getBonusPointsPerDraw() != null) lottery.setBonusPointsPerDraw(req.getBonusPointsPerDraw());
if (req.getBonusCostPerDraw() != null) lottery.setBonusCostPerDraw(req.getBonusCostPerDraw());
```

---

### Step 5: 刪除舊的 Controller（可選）

**如果確定不再使用單獨的 Controller，可以刪除**：

```bash
del src\main\java\com\group\admin\controller\admin\AdminLotteryController.java
del src\main\java\com\group\admin\controller\admin\LotteryPrizeController.java
```

**或保留但標註 @Deprecated**：

```java
@Deprecated
@Tag(name = "（已棄用）單獨商品管理", description = "請使用 AdminLotteryWithPrizesController")
@RestController
@RequestMapping("/admin/lottery")
public class AdminLotteryController {
    // ...
}
```

---

## ✅ 測試驗證

### 1. 編譯測試

```bash
mvn clean compile -DskipTests
```

**預期結果**：
```
[INFO] BUILD SUCCESS
```

### 2. 啟動應用

```bash
mvn spring-boot:run
```

**預期結果**：
```
Started AdminApplication in X seconds
```

### 3. API 測試

**使用 Postman 或 curl**：

```bash
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -d '{
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
      "description": "這是一筆快速產生的測試資料（商品+獎品整合）。",
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
        "name": "獎品 A - 測試獎品 1",
        "quantity": 1,
        "level": "A",
        "isGrandPrize": true
      },
      {
        "name": "獎品 B - 測試獎品 2",
        "quantity": 3,
        "level": "B"
      }
    ]
  }'
```

**預期結果**：
```json
{
  "success": true,
  "data": {
    "id": "...",
    "title": "鬼滅之刃一番賞 324",
    "playMode": "LOTTERY_MODE",
    "status": "DRAFT",
    "hotCount": 999,
    "theme": "鬼滅之刃",
    "galleryImages": ["..."],
    "content": "【活動說明】...",
    "tags": ["鬼滅之刃", "一番賞", "熱門"],
    "bonusEnabled": true,
    "bonusPointsPerDraw": 10,
    "bonusCostPerDraw": 200,
    "prizes": [...]
  }
}
```

---

## 🐛 常見問題

### Q1: 編譯錯誤 - getPlayMode() 找不到
**原因**：Entity 沒有新增欄位  
**解決**：執行 Step 2

### Q2: 啟動錯誤 - Unknown column 'play_mode'
**原因**：資料庫沒有新增欄位  
**解決**：執行 Step 1

### Q3: API 返回欄位為 null
**原因**：Service 沒有映射新欄位  
**解決**：執行 Step 4

### Q4: JSON 序列化錯誤
**原因**：ObjectMapper 沒有正確處理  
**檢查**：
```java
// 確認 LotteryServiceImpl 有注入 ObjectMapper
@Autowired
private ObjectMapper objectMapper;
```

---

## 📝 檢查清單

執行前請確認：

- [ ] 資料庫連線正常
- [ ] 有 ALTER TABLE 權限
- [ ] 備份現有資料（建議）
- [ ] 測試環境先驗證
- [ ] 關閉正在運行的應用

執行後請確認：

- [ ] 資料庫新增 9 個欄位
- [ ] Entity 新增 9 個欄位
- [ ] Mapper.xml 更新對應
- [ ] Service 新增映射邏輯
- [ ] 編譯成功
- [ ] 應用啟動成功
- [ ] API 測試通過

---

## 🎯 完成標準

### 功能完整性
- ✅ 可以創建帶有新欄位的商品
- ✅ 查詢時返回新欄位
- ✅ 更新時可以修改新欄位
- ✅ JSON 欄位正確序列化/反序列化

### 性能要求
- ✅ API 回應時間 < 1s
- ✅ 資料庫查詢無全表掃描
- ✅ 不影響現有功能

### 程式碼品質
- ✅ 無編譯錯誤
- ✅ 無編譯警告
- ✅ 程式碼風格一致
- ✅ 日誌輸出完整

---

## 📞 需要協助？

如果遇到問題，請提供：
1. 錯誤日誌
2. 執行的 SQL 語句
3. API 請求與回應
4. 預期行為 vs 實際行為

祝操作順利！🚀
