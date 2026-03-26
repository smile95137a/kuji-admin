# 遊玩方式分類完整參考

> 最後更新：2026-02-22  
> 系統：KUJI 抽選平台

---

## 📋 分類架構說明

系統採用 **雙層分類架構**：
- **主分類（Category）**：商品類型（官方一番賞、扭蛋、卡牌、自製賞）
- **子分類（SubCategory）**：遊玩方式（僅適用於自製賞）

---

## 🎯 主分類（LotteryCategoryEnum）

### 1. 官方一番賞（OFFICIAL_ICHIBAN）
- **說明**：官方授權的一番賞商品
- **特色**：
  - 固定獎項設定（A賞、B賞、C賞...Last賞）
  - 抽完即止，不可重複抽取
  - 通常有固定的獎品池數量
- **子分類**：無（官方規格）
- **遊玩方式**：傳統抽籤

**範例商品**：
- 鬼滅之刃一番賞
- 航海王一番賞
- 咒術迴戰一番賞

---

### 2. 扭蛋（GACHA）
- **說明**：扭蛋機式抽選商品
- **特色**：
  - 通常為隨機抽取，可能重複
  - 獎品池較大，有機率設定
  - 可設定保底機制
- **子分類**：無
- **遊玩方式**：隨機抽選

**範例商品**：
- 盲盒扭蛋
- 角色公仔扭蛋
- 徽章扭蛋系列

---

### 3. 卡牌（TRADING_CARD）
- **說明**：交換卡牌類商品
- **特色**：
  - 卡包抽選，固定張數
  - 稀有度分級（N、R、SR、SSR）
  - 通常有收集冊概念
- **子分類**：無
- **遊玩方式**：開包抽卡

**範例商品**：
- Pokémon 寶可夢卡牌
- 遊戲王卡牌
- 威世智 MTG 卡牌

---

### 4. 自製賞（CUSTOM_GACHA）
- **說明**：店家自行設定規則的抽選商品
- **特色**：
  - 高度彈性，可自訂遊玩方式
  - **必須指定子分類**（遊玩方式）
  - 獎品、數量、機率皆可自訂
- **子分類**：必填（見下方子分類說明）

---

## 🎮 子分類（LotterySubCategoryEnum）

> ⚠️ **僅適用於「自製賞」主分類**

### 1. 抽籤型（LOTTERY_MODE）
- **說明**：傳統抽籤模式
- **運作方式**：
  - 每次抽選消耗一次機會
  - 抽中後該獎品從獎品池移除
  - 抽完即止，不可重複
- **適用場景**：
  - 店家自製的一番賞風格商品
  - 限量商品抽選
  - 活動特殊獎品

**前端顯示**：
```
🎫 抽籤型
「每抽必中，抽完為止！」
剩餘次數：25/30
```

---

### 2. 刮刮樂型（SCRATCH_MODE）
- **說明**：刮刮樂卡片模式
- **運作方式**：
  - 購買實體刮刮樂卡片
  - 現場刮開查看結果
  - 中獎後憑卡片兌換
- **適用場景**：
  - 實體店面活動
  - 現場抽獎活動
  - 即刮即兌商品

**前端顯示**：
```
🎰 刮刮樂型
「刮開即知，現場兌獎！」
單張售價：$100
```

---

### 3. 刮刮卡型（SCRATCH_CARD_MODE）
- **說明**：數位刮刮卡模式
- **運作方式**：
  - 線上購買虛擬刮刮卡
  - 手機螢幕刮開動畫
  - 系統即時判定中獎與否
  - 自動匯入獎品箱
- **適用場景**：
  - 線上活動促銷
  - APP 內遊戲化體驗
  - 數位化刮刮樂商品

**前端顯示**：
```
📱 刮刮卡型
「滑動螢幕，揭曉驚喜！」
立即刮開 →
```

---

## 🗂️ 完整分類組合表

| 主分類 | 主分類代碼 | 子分類 | 子分類代碼 | 說明 |
|--------|------------|--------|------------|------|
| 官方一番賞 | `OFFICIAL_ICHIBAN` | - | `null` | 官方授權，固定規格 |
| 扭蛋 | `GACHA` | - | `null` | 扭蛋機式抽選 |
| 卡牌 | `TRADING_CARD` | - | `null` | 卡包抽卡 |
| 自製賞 | `CUSTOM_GACHA` | 抽籤型 | `LOTTERY_MODE` | 傳統抽籤，抽完為止 |
| 自製賞 | `CUSTOM_GACHA` | 刮刮樂型 | `SCRATCH_MODE` | 實體刮刮樂卡片 |
| 自製賞 | `CUSTOM_GACHA` | 刮刮卡型 | `SCRATCH_CARD_MODE` | 數位刮刮卡 |

---

## 💻 API 請求範例

### 建立官方一番賞商品
```json
POST /api/admin/lottery

{
  "title": "鬼滅之刃一番賞 Vol.5",
  "category": "OFFICIAL_ICHIBAN",
  "subCategory": null,
  "pricePerDraw": 80,
  "maxDraws": 60
}
```

### 建立自製賞（抽籤型）
```json
POST /api/admin/lottery

{
  "title": "店家自製福袋",
  "category": "CUSTOM_GACHA",
  "subCategory": "LOTTERY_MODE",
  "pricePerDraw": 100,
  "maxDraws": 30
}
```

### 建立刮刮卡商品
```json
POST /api/admin/lottery

{
  "title": "新年刮刮卡",
  "category": "CUSTOM_GACHA",
  "subCategory": "SCRATCH_CARD_MODE",
  "pricePerDraw": 50,
  "maxDraws": 100
}
```

---

## 🎨 前端顯示建議

### 分類圖示對應
```javascript
const categoryIcons = {
  OFFICIAL_ICHIBAN: '🎫',    // 官方一番賞
  GACHA: '🎰',                // 扭蛋
  TRADING_CARD: '🃏',         // 卡牌
  CUSTOM_GACHA: '🎁'          // 自製賞
};

const subCategoryIcons = {
  LOTTERY_MODE: '🎟️',         // 抽籤型
  SCRATCH_MODE: '🎰',          // 刮刮樂型
  SCRATCH_CARD_MODE: '📱'     // 刮刮卡型
};
```

### 分類標籤顏色
```css
/* 主分類 */
.category-official    { background: #FF6B6B; }  /* 紅色 - 官方 */
.category-gacha       { background: #4ECDC4; }  /* 青色 - 扭蛋 */
.category-card        { background: #FFD93D; }  /* 黃色 - 卡牌 */
.category-custom      { background: #A78BFA; }  /* 紫色 - 自製 */

/* 子分類 */
.sub-lottery          { border: 2px solid #10B981; }  /* 綠框 */
.sub-scratch          { border: 2px solid #F59E0B; }  /* 橘框 */
.sub-scratch-card     { border: 2px solid #3B82F6; }  /* 藍框 */
```

---

## ⚠️ 重要驗證規則

### 後端驗證邏輯
```java
// ✅ 正確：自製賞必須指定子分類
if ("CUSTOM_GACHA".equals(category) && subCategory == null) {
    throw new BusinessException("自製賞必須指定遊玩方式");
}

// ✅ 正確：非自製賞不可指定子分類
if (!"CUSTOM_GACHA".equals(category) && subCategory != null) {
    throw new BusinessException("此分類不支援子分類");
}

// ✅ 正確：驗證子分類是否有效
if (subCategory != null) {
    LotterySubCategoryEnum validEnum = LotterySubCategoryEnum.fromCode(subCategory);
    if (validEnum == null) {
        throw new BusinessException("無效的遊玩方式");
    }
}
```

---

## 📱 用戶體驗建議

### 1. 商品列表頁
- 顯示主分類標籤 + 子分類標籤（如適用）
- 使用圖示快速識別
- 顯示剩餘數量/已售完狀態

### 2. 商品詳情頁
- 明確標示遊玩方式說明
- 顯示規則說明（如：抽完為止、可重複等）
- 提供示範影片或圖片

### 3. 篩選功能
```javascript
// 分類篩選器
const filters = [
  { label: '全部商品', value: null },
  { label: '官方一番賞', value: 'OFFICIAL_ICHIBAN' },
  { label: '扭蛋', value: 'GACHA' },
  { label: '卡牌', value: 'TRADING_CARD' },
  { label: '自製賞', value: 'CUSTOM_GACHA', children: [
      { label: '抽籤型', value: 'LOTTERY_MODE' },
      { label: '刮刮樂型', value: 'SCRATCH_MODE' },
      { label: '刮刮卡型', value: 'SCRATCH_CARD_MODE' }
    ]
  }
];
```

---

## 🔍 常見問題

### Q1：為什麼官方一番賞沒有子分類？
**A**：官方一番賞有固定的官方規格，遊玩方式已標準化，不需要額外的子分類。

### Q2：可以新增更多子分類嗎？
**A**：可以！在 `LotterySubCategoryEnum.java` 中新增即可，例如：
- BLIND_BOX_MODE（盲盒型）
- RAFFLE_MODE（抽獎券型）
- POINT_EXCHANGE_MODE（點數兌換型）

### Q3：刮刮樂型與刮刮卡型有什麼差別？
**A**：
- **刮刮樂型**：實體卡片，現場刮開兌獎
- **刮刮卡型**：數位版本，線上刮開自動領獎

### Q4：可以讓官方一番賞也支援子分類嗎？
**A**：不建議。官方一番賞是標準化商品，強制加入子分類會造成系統複雜度增加。如需不同遊玩方式，建議使用「自製賞」分類。

---

## 📚 相關文件

- [LotteryCategoryEnum.java](src/main/java/com/group/admin/enums/LotteryCategoryEnum.java) - 主分類定義
- [LotterySubCategoryEnum.java](src/main/java/com/group/admin/enums/LotterySubCategoryEnum.java) - 子分類定義
- [API_DOCUMENTATION_COMPLETE.md](API_DOCUMENTATION_COMPLETE.md) - 完整 API 文件
- [ADMIN_API_100_PERCENT_ACCURATE.md](ADMIN_API_100_PERCENT_ACCURATE.md) - 後台 API 參考

---

## 📝 版本記錄

| 版本 | 日期 | 更新內容 |
|------|------|----------|
| 1.0.0 | 2026-02-22 | 初版建立，整理所有遊玩分類 |
