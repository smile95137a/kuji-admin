# 🎯 商品與獎品池關聯實作計劃

## 問題分析

**現況：** 商品（Lottery）和獎品（Prize）是分開的，無法知道抽中的是哪個商品的哪個獎品

**目標：** 建立完整的商品-獎品池-獎品關聯系統

---

## 1. 資料庫設計

### 1.1 現有資料表
```sql
lottery (商品)
  - id
  - title
  - description
  - status

prize (獎品)
  - id
  - name
  - level (A/B/C/D/E/LAST)
  - image_url
```

### 1.2 需要新增的關聯表

```sql
CREATE TABLE IF NOT EXISTS `lottery_prize_pool` (
    `id` VARCHAR(50) NOT NULL PRIMARY KEY COMMENT '主鍵',
    `lottery_id` VARCHAR(50) NOT NULL COMMENT '商品ID',
    `prize_id` VARCHAR(50) NOT NULL COMMENT '獎品ID',
    `prize_level` VARCHAR(10) NOT NULL COMMENT '獎品等級',
    `total_quantity` INT NOT NULL DEFAULT 0 COMMENT '總數量',
    `remaining_quantity` INT NOT NULL DEFAULT 0 COMMENT '剩餘數量',
    `probability` DECIMAL(5,2) DEFAULT NULL COMMENT '中獎機率(%)',
    `order_num` INT DEFAULT 0 COMMENT '排序',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否啟用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_lottery_prize_lottery` FOREIGN KEY (`lottery_id`) REFERENCES `lottery` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_lottery_prize_prize` FOREIGN KEY (`prize_id`) REFERENCES `prize` (`id`) ON DELETE CASCADE,
    INDEX `idx_lottery_id` (`lottery_id`),
    INDEX `idx_prize_level` (`prize_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品獎品池';

CREATE TABLE IF NOT EXISTS `draw_result` (
    `id` VARCHAR(50) NOT NULL PRIMARY KEY COMMENT '主鍵',
    `user_id` VARCHAR(50) NOT NULL COMMENT '玩家ID',
    `lottery_id` VARCHAR(50) NOT NULL COMMENT '商品ID',
    `prize_pool_id` VARCHAR(50) NOT NULL COMMENT '獎品池ID',
    `prize_id` VARCHAR(50) NOT NULL COMMENT '獎品ID',
    `prize_level` VARCHAR(10) NOT NULL COMMENT '獎品等級',
    `prize_name` VARCHAR(100) NOT NULL COMMENT '獎品名稱',
    `prize_image_url` VARCHAR(500) DEFAULT NULL COMMENT '獎品圖片',
    `draw_type` VARCHAR(20) DEFAULT 'SINGLE' COMMENT '抽獎類型: SINGLE/MULTI',
    `status` VARCHAR(20) DEFAULT 'IN_PRIZE_BOX' COMMENT '狀態: IN_PRIZE_BOX/IN_ORDER/DELIVERED',
    `order_id` VARCHAR(50) DEFAULT NULL COMMENT '訂單ID（已出貨後）',
    `drawn_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '抽獎時間',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_lottery_id` (`lottery_id`),
    INDEX `idx_status` (`status`),
    CONSTRAINT `fk_draw_result_lottery` FOREIGN KEY (`lottery_id`) REFERENCES `lottery` (`id`),
    CONSTRAINT `fk_draw_result_prize_pool` FOREIGN KEY (`prize_pool_id`) REFERENCES `lottery_prize_pool` (`id`),
    CONSTRAINT `fk_draw_result_prize` FOREIGN KEY (`prize_id`) REFERENCES `prize` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽獎結果';
```

---

## 2. Entity 設計

### 2.1 LotteryPrizePool.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryPrizePool {
    private String id;
    private String lotteryId;
    private String prizeId;
    private String prizeLevel;
    private Integer totalQuantity;
    private Integer remainingQuantity;
    private BigDecimal probability;
    private Integer orderNum;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Join 查詢時使用
    private Prize prize;
    private Lottery lottery;
}
```

### 2.2 DrawResult.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawResult {
    private String id;
    private String userId;
    private String lotteryId;
    private String prizePoolId;
    private String prizeId;
    private String prizeLevel;
    private String prizeName;
    private String prizeImageUrl;
    private String drawType;
    private String status;  // IN_PRIZE_BOX, IN_ORDER, DELIVERED
    private String orderId;
    private LocalDateTime drawnAt;
}
```

---

## 3. API 設計

### 3.1 後台：建立商品獎品池
```
POST /api/admin/lottery/{lotteryId}/prize-pool
Content-Type: application/json

{
  "prizes": [
    {
      "prizeId": "prize-001",
      "prizeLevel": "A",
      "totalQuantity": 5,
      "probability": 1.5
    },
    {
      "prizeId": "prize-002",
      "prizeLevel": "B",
      "totalQuantity": 10,
      "probability": 3.0
    }
  ]
}

Response:
{
  "success": true,
  "data": {
    "lotteryId": "lottery-001",
    "totalPrizes": 15,
    "prizePoolItems": [...]
  }
}
```

### 3.2 後台：查詢商品獎品池
```
GET /api/admin/lottery/{lotteryId}/prize-pool

Response:
{
  "success": true,
  "data": [
    {
      "id": "pool-001",
      "prizeId": "prize-001",
      "prizeName": "特等獎-限定公仔",
      "prizeLevel": "A",
      "prizeImageUrl": "https://s3.../prize-001.jpg",
      "totalQuantity": 5,
      "remainingQuantity": 3,
      "probability": 1.5,
      "isActive": true
    }
  ]
}
```

### 3.3 前台：執行抽獎
```
POST /api/lottery/{lotteryId}/draw
Content-Type: application/json

{
  "drawCount": 1
}

Response:
{
  "success": true,
  "data": {
    "drawResults": [
      {
        "id": "draw-001",
        "lotteryId": "lottery-001",
        "lotteryTitle": "鬼滅之刃一番賞",
        "prizeId": "prize-001",
        "prizeName": "特等獎-限定公仔",
        "prizeLevel": "A",
        "prizeImageUrl": "https://s3.../prize-001.jpg",
        "drawnAt": "2026-01-15T15:00:00"
      }
    ],
    "remainingGold": 980,
    "message": "恭喜抽中 A 賞！"
  }
}
```

### 3.4 前台：查詢賞品盒
```
GET /api/prize-box

Response:
{
  "success": true,
  "data": {
    "items": [
      {
        "drawResultId": "draw-001",
        "lotteryId": "lottery-001",
        "lotteryTitle": "鬼滅之刃一番賞",
        "storeId": "store-001",
        "storeName": "動漫專賣店",
        "prizeId": "prize-001",
        "prizeName": "特等獎-限定公仔",
        "prizeLevel": "A",
        "prizeImageUrl": "https://s3.../prize-001.jpg",
        "drawnAt": "2026-01-15T15:00:00",
        "canShip": true,
        "canRecycle": false
      }
    ],
    "groupByStore": {
      "store-001": {
        "storeName": "動漫專賣店",
        "items": [...]
      }
    }
  }
}
```

---

## 4. Service 實作重點

### 4.1 抽獎邏輯 (DrawService.java)

```java
public DrawResultRes draw(String userId, String lotteryId, Integer drawCount) {
    // 1. 檢查用戶餘額
    // 2. 檢查商品狀態
    // 3. 扣除點數
    // 4. 執行抽獎（機率演算法）
    //    - 查詢該商品的獎品池
    //    - 根據機率和剩餘數量計算中獎
    //    - 更新獎品池剩餘數量
    // 5. 建立抽獎結果記錄
    // 6. 返回結果
}

private LotteryPrizePool selectPrize(String lotteryId) {
    // 根據機率和剩餘數量選擇獎品
    // 使用加權隨機演算法
}
```

### 4.2 賞品盒邏輯 (PrizeBoxService.java)

```java
public PrizeBoxRes getPrizeBox(String userId) {
    // 1. 查詢用戶所有 IN_PRIZE_BOX 狀態的抽獎結果
    // 2. JOIN lottery 和 store 取得商品和店家資訊
    // 3. 按店家分組
    // 4. 返回完整資訊
}
```

---

## 5. 圖片 URL 處理

### 5.1 S3 URL 前綴配置

```yaml
# application.yml
aws:
  s3:
    bucket-name: kuji-images
    region: ap-northeast-1
    base-url: https://kuji-images.s3.ap-northeast-1.amazonaws.com
```

### 5.2 Service 層處理

```java
@Service
public class ImageUrlService {
    
    @Value("${aws.s3.base-url}")
    private String s3BaseUrl;
    
    public String getFullImageUrl(String imagePath) {
        if (imagePath == null || imagePath.startsWith("http")) {
            return imagePath;
        }
        return s3BaseUrl + "/" + imagePath;
    }
}
```

### 5.3 在 Response DTO 自動處理

```java
@Data
public class PrizeRes {
    private String id;
    private String name;
    private String imageUrl;
    
    public void setImageUrl(String imageUrl) {
        // 自動加上 S3 domain
        if (imageUrl != null && !imageUrl.startsWith("http")) {
            this.imageUrl = s3BaseUrl + "/" + imageUrl;
        } else {
            this.imageUrl = imageUrl;
        }
    }
}
```

---

## 6. 查詢條件 API 設計

### 6.1 統一查詢模式

```java
// BaseCondition - 所有查詢條件的基類
@Data
public class BaseQueryCondition {
    private LocalDateTime createdAtStart;
    private LocalDateTime createdAtEnd;
    private String keyword;
    private String sortBy;
    private String sortOrder;  // ASC/DESC
}

// LotteryQueryCondition
@Data
@EqualsAndHashCode(callSuper = true)
public class LotteryQueryCondition extends BaseQueryCondition {
    private String storeId;      // 後端自動帶入
    private String title;
    private String status;
    private String category;
    private Long priceMin;
    private Long priceMax;
}

// Controller
@PostMapping("/admin/lottery/list")
public ResponseEntity<List<LotteryRes>> queryLotteries(
        @RequestBody(required = false) LotteryQueryCondition condition) {
    
    // 自動帶入 storeId
    String storeId = SecurityUtils.getCurrentUserPrimaryStoreId();
    if (condition == null) condition = new LotteryQueryCondition();
    condition.setStoreId(storeId);
    
    return ResponseEntity.ok(lotteryService.queryLotteries(condition));
}
```

### 6.2 動態 SQL 實作

```java
public List<Lottery> queryLotteries(LotteryQueryCondition condition) {
    LotteryExample example = new LotteryExample();
    LotteryExample.Criteria criteria = example.createCriteria();
    
    // 所有條件都是可選的
    if (condition != null) {
        if (condition.getStoreId() != null) {
            criteria.andStoreIdEqualTo(condition.getStoreId());
        }
        if (condition.getTitle() != null && !condition.getTitle().isEmpty()) {
            criteria.andTitleLike("%" + condition.getTitle() + "%");
        }
        if (condition.getStatus() != null) {
            criteria.andStatusEqualTo(condition.getStatus());
        }
        if (condition.getPriceMin() != null) {
            criteria.andPricePerDrawGreaterThanOrEqualTo(condition.getPriceMin());
        }
        if (condition.getPriceMax() != null) {
            criteria.andPricePerDrawLessThanOrEqualTo(condition.getPriceMax());
        }
    }
    
    // 排序
    if (condition != null && condition.getSortBy() != null) {
        String order = condition.getSortOrder() != null ? condition.getSortOrder() : "DESC";
        example.setOrderByClause(condition.getSortBy() + " " + order);
    } else {
        example.setOrderByClause("created_at DESC");
    }
    
    return lotteryMapper.selectByExample(example);
}
```

---

## 7. 測試場景

### 7.1 管理員測試 (ROLE_ADMIN)
- 可以看到所有店家的商品
- 可以查詢所有訂單
- 可以管理所有獎品池

### 7.2 店家負責人測試 (ROLE_STORE_OWNER)
- 只能看到自己店家的商品
- 只能查詢自己店家的訂單
- 只能管理自己店家的獎品池
- storeId 自動從 JWT 帶入

### 7.3 店家編輯測試 (ROLE_STORE_EDITOR)
- 可以編輯商品資訊
- 不能刪除商品
- 不能查看營收報表

### 7.4 前台玩家測試 (ROLE_USER)
- 可以瀏覽所有上架商品
- 可以執行抽獎
- 可以查看自己的賞品盒
- 可以建立訂單

---

## 8. 實作優先順序

### Phase 1: 資料庫層（優先）
- [ ] 建立 SQL migration script
- [ ] 執行 migration
- [ ] 建立 Entity 類別
- [ ] 建立 Mapper 介面

### Phase 2: 核心邏輯
- [ ] 實作獎品池管理 Service
- [ ] 實作抽獎邏輯 Service
- [ ] 實作賞品盒 Service
- [ ] 圖片 URL 處理

### Phase 3: API 層
- [ ] 後台獎品池 CRUD API
- [ ] 前台抽獎 API
- [ ] 前台賞品盒 API
- [ ] 統一查詢條件 API

### Phase 4: 測試
- [ ] 單元測試
- [ ] 整合測試
- [ ] 不同角色權限測試
- [ ] Postman Collection

---

## 9. 注意事項

### 9.1 並發控制
- 抽獎時需要鎖定獎品池記錄，避免超抽
- 使用樂觀鎖或悲觀鎖控制剩餘數量

### 9.2 資料一致性
- 抽獎結果必須與獎品池關聯
- 訂單必須與抽獎結果關聯
- 使用事務確保資料一致性

### 9.3 效能優化
- 獎品池查詢使用索引
- 抽獎結果查詢使用複合索引
- 考慮使用 Redis 快取熱門商品獎品池

---

**下一步：** 需要我開始實作嗎？請確認：
1. 資料庫設計是否符合需求？
2. API 設計是否符合前端需求？
3. 是否需要調整任何邏輯？
