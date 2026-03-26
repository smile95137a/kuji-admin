# 🚀 KUJI 後端問題修復總結

## ✅ 已完成的工作

### 1. CORS 配置修復
- **問題**: CORS preflight 返回 403，沒有 Access-Control-Allow-Origin header
- **原因**: `setAllowCredentials(true)` 與 `setAllowedOrigins()` 不相容
- **解決**: 改用 `setAllowedOriginPatterns()` 支援 credentials
- **修改檔案**: `src/main/java/com/group/admin/config/CorsConfig.java`
- **狀態**: ✅ 已修改，等待部署測試

### 2. 文件分類整理
- **問題**: MD 文件太多，看得眼花繚亂
- **解決**: 創建 `README_DOCS.md` 文件總覽
- **分類**:
  - 🚀 部署相關 (Deployment)
  - 📖 API 測試指南 (API Guides)
  - ✨ 功能實作文件 (Features)
  - 🏗️ 架構設計 (Architecture)
  - 🐛 問題修復記錄 (Fix Records)
  - 📋 專案管理 (Project Management)
- **狀態**: ✅ 已完成

### 3. 商品與獎品關聯設計
- **問題**: 商品和獎品是分開的，無法知道抽中什麼
- **解決**: 設計完整的獎品池系統
- **文件**: `LOTTERY_PRIZE_POOL_IMPLEMENTATION.md`
- **內容**:
  - 資料庫設計 (lottery_prize_pool, draw_result 表)
  - Entity 設計
  - API 設計 (建立獎品池、執行抽獎、查詢賞品盒)
  - Service 實作邏輯
  - 測試場景
- **狀態**: ⏳ 設計完成，待實作

### 4. S3 圖片 URL 處理設計
- **問題**: 圖片 URL 缺少 domain 前綴
- **解決**: 在 Service 層自動加上 S3 base URL
- **方案**:
  ```yaml
  aws:
    s3:
      base-url: https://kuji-images.s3.ap-northeast-1.amazonaws.com
  ```
  ```java
  public String getFullImageUrl(String imagePath) {
      if (imagePath != null && !imagePath.startsWith("http")) {
          return s3BaseUrl + "/" + imagePath;
      }
      return imagePath;
  }
  ```
- **狀態**: ⏳ 設計完成，待實作

### 5. 統一查詢條件 API 設計
- **問題**: 所有 API 都要有查詢條件
- **解決**: BaseQueryCondition + 動態 SQL
- **模式**:
  ```java
  @Data
  public class BaseQueryCondition {
      private LocalDateTime createdAtStart;
      private LocalDateTime createdAtEnd;
      private String keyword;
      private String sortBy;
      private String sortOrder;
  }
  
  // 所有條件都是可選的
  if (condition != null && condition.getTitle() != null) {
      criteria.andTitleLike("%" + condition.getTitle() + "%");
  }
  ```
- **狀態**: ⏳ 設計完成，待實作

### 6. 角色權限測試指南
- **問題**: 需要依照不同權限的使用者來測試
- **解決**: 創建完整的測試指南
- **文件**: `ROLE_PERMISSION_TEST_GUIDE.md`
- **內容**:
  - 測試帳號清單 (管理員、店家A、店家B、編輯、玩家)
  - 5 大測試場景 (商品、訂單、報表、前台、跨角色)
  - 測試檢查清單
  - 測試報告模板
- **狀態**: ✅ 已完成

---

## ⚠️ 待處理問題

### 1. CORS 部署問題
- **問題**: SSH key 路徑不正確，無法連線 EC2
- **原因**: `C:\Users\user\Downloads\kuji-backend.pem` 檔案不存在
- **解決方案**:

**選項 1: 找到正確的 key 檔案**
```cmd
# 搜尋 .pem 檔案
dir /s C:\Users\user\*.pem

# 或檢查 Downloads 目錄
dir C:\Users\user\Downloads\*.pem
```

**選項 2: 手動部署**
```cmd
# 1. 確認 key 檔案位置
set KEY_FILE=<正確的路徑>

# 2. 上傳 JAR
scp -i "%KEY_FILE%" target\admin-1.0.0.jar ec2-user@18.179.187.129:/home/ec2-user/kuji-backend/

# 3. SSH 登入
ssh -i "%KEY_FILE%" ec2-user@18.179.187.129

# 4. 在 EC2 上執行
cd /home/ec2-user/kuji-backend
./stop.sh
./start.sh
tail -f logs/application.log
```

**選項 3: 使用其他工具**
- FileZilla (SFTP)
- WinSCP
- PuTTY + PSCP

---

## 📋 下一步行動計劃

### 優先級 1: 修復 CORS（緊急）

1. **找到 SSH key 檔案**
   ```cmd
   dir /s C:\Users\user\*.pem
   ```

2. **更新部署腳本中的 KEY_FILE 路徑**
   - 編輯 `deploy-and-test.bat`
   - 編輯 `quick-deploy.bat`
   - 編輯 `deploy-to-ec2.bat`

3. **重新部署**
   ```cmd
   deploy-and-test.bat
   ```

4. **測試 CORS**
   ```cmd
   test-cors.bat
   ```

5. **在瀏覽器測試前端**
   - 開啟 http://18.179.187.129/kuji/login
   - 登入測試

### 優先級 2: 實作商品獎品池

1. **執行 SQL migration**
   ```sql
   -- 建立 lottery_prize_pool 表
   -- 建立 draw_result 表
   ```

2. **建立 Entity 類別**
   - LotteryPrizePool.java
   - DrawResult.java

3. **建立 Mapper 介面**
   - LotteryPrizePoolMapper.java
   - DrawResultMapper.java

4. **實作 Service**
   - PrizePoolService.java
   - DrawService.java
   - PrizeBoxService.java

5. **實作 Controller**
   - AdminPrizePoolController.java
   - ApiDrawController.java
   - ApiPrizeBoxController.java

6. **測試**
   - 建立獎品池
   - 執行抽獎
   - 查看賞品盒

### 優先級 3: 實作查詢條件 API

1. **建立 BaseQueryCondition**
   ```java
   public class BaseQueryCondition {
       private LocalDateTime createdAtStart;
       private LocalDateTime createdAtEnd;
       private String keyword;
       private String sortBy;
       private String sortOrder;
   }
   ```

2. **更新所有 QueryCondition**
   - LotteryQueryCondition
   - PrizeQueryCondition
   - OrderQueryCondition
   - etc.

3. **更新 Service 動態 SQL**
   ```java
   public List<Lottery> queryLotteries(LotteryQueryCondition condition) {
       LotteryExample example = new LotteryExample();
       Criteria criteria = example.createCriteria();
       
       // 所有條件都是可選的
       if (condition != null) {
           if (condition.getTitle() != null) {
               criteria.andTitleLike("%" + condition.getTitle() + "%");
           }
           // ...
       }
       
       return lotteryMapper.selectByExample(example);
   }
   ```

### 優先級 4: S3 圖片 URL 處理

1. **新增 application.yml 配置**
   ```yaml
   aws:
     s3:
       base-url: https://kuji-images.s3.ap-northeast-1.amazonaws.com
   ```

2. **建立 ImageUrlService**
   ```java
   @Service
   public class ImageUrlService {
       @Value("${aws.s3.base-url}")
       private String s3BaseUrl;
       
       public String getFullImageUrl(String imagePath) {
           if (imagePath != null && !imagePath.startsWith("http")) {
               return s3BaseUrl + "/" + imagePath;
           }
           return imagePath;
       }
   }
   ```

3. **在 Response DTO 中使用**
   ```java
   @Data
   public class PrizeRes {
       private String imageUrl;
       
       public void setImageUrl(String imageUrl) {
           this.imageUrl = imageUrlService.getFullImageUrl(imageUrl);
       }
   }
   ```

---

## 🔍 檢查清單

### 立即執行
- [ ] 找到 SSH key 檔案位置
- [ ] 更新部署腳本中的路徑
- [ ] 重新部署修復 CORS 的版本
- [ ] 在瀏覽器測試前端登入

### 短期內執行
- [ ] 實作商品獎品池系統
- [ ] 實作查詢條件 API
- [ ] 實作 S3 圖片 URL 處理
- [ ] 執行完整的角色權限測試

### 長期規劃
- [ ] 整理 MD 文件到分類目錄
- [ ] 建立 API 文件 (Swagger)
- [ ] 建立部署 CI/CD pipeline
- [ ] 效能優化與監控

---

## 📞 需要立即協助

### 問題 1: SSH Key 位置
**請執行以下命令找到 key 檔案：**
```cmd
dir /s C:\Users\user\*.pem
```

**或者告訴我：**
1. 您的 EC2 key 檔案名稱是什麼？
2. 它放在哪個目錄？

### 問題 2: 前端 CORS 錯誤
**如果找不到 SSH key，可以：**
1. 使用 AWS Console 直接連線 EC2
2. 使用其他 SSH 工具 (PuTTY)
3. 使用 AWS Systems Manager Session Manager

**臨時解決方案：**
- 我可以提供手動部署步驟
- 或者使用 AWS CodeDeploy

---

## 📚 重要文件快速索引

| 用途 | 文件 |
|------|------|
| 文件總覽 | `README_DOCS.md` |
| CORS 修復 | `CORS_FIX_AND_DEPLOY.md` |
| 部署指南 | `DEPLOY_GUIDE.md` |
| 商品獎品池 | `LOTTERY_PRIZE_POOL_IMPLEMENTATION.md` |
| 角色測試 | `ROLE_PERMISSION_TEST_GUIDE.md` |
| API 測試 | `API_TEST_GUIDE.md` |

---

**現在最需要的：**
1. 找到 SSH key 檔案位置
2. 部署修復 CORS 的版本
3. 測試前端登入是否正常

請告訴我您的 SSH key 檔案位置，我會幫您更新所有部署腳本！ 🚀
