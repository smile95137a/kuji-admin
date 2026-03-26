# Controller 測試生成計劃

## 生成時間
**2026-01-23**

---

## 🎯 測試策略

### 測試框架
- JUnit 5 (Jupiter)
- Spring Boot Test
- MockMvc
- Mockito
- Spring Security Test

### 測試範圍
1. **後台 Controller** (`/admin/**`)
2. **前台 Controller** (`/api/**`)

---

## 📋 後台 Controller 清單（優先級高）

### 核心業務
1. ✅ **AdminLotteryWithPrizesController** - 商品與獎品整合管理（已完成）
2. ⏳ **AdminLotteryController** - 商品管理
3. ⏳ **LotteryPrizeController** - 獎品管理
4. ⏳ **AdminOrderController** - 訂單管理
5. ⏳ **AdminPrizeBoxController** - 獎品箱管理
6. ⏳ **AdminWalletController** - 錢包管理

### 內容管理
7. ⏳ **AdminNewsController** - 最新消息
8. ⏳ **AdminBannerController** - Banner 管理
9. ⏳ **AdminMarqueeController** - 跑馬燈管理

### 系統管理
10. ⏳ **AdminUserController** - 後台使用者管理
11. ⏳ **AdminFrontendUserController** - 前台會員管理
12. ⏳ **AdminAuthController** - 後台認證
13. ⏳ **MenuController** - 選單管理
14. ⏳ **RoleController** - 角色管理
15. ⏳ **PermissionController** - 權限管理

### 營運工具
16. ⏳ **AdminReferralCodeController** - 推薦碼管理
17. ⏳ **AdminRechargePlanController** - 儲值方案管理
18. ⏳ **AdminReportController** - 報表管理
19. ⏳ **UploadController** - 檔案上傳

---

## 📋 前台 Controller 清單

### 認證與使用者
20. ⏳ **ApiAuthController** - 前台認證（註冊/登入）
21. ⏳ **UserController** - 使用者資料管理
22. ⏳ **UserAddressController** - 地址管理
23. ⏳ **WalletController** - 前台錢包

### 商品與抽獎
24. ⏳ **LotteryBrowseController** - 商品瀏覽
25. ⏳ **LotteryController** - 商品詳情
26. ⏳ **LotteryDrawController** - 抽獎功能
27. ⏳ **FrontendLotteryController** - 前台商品功能

### 訂單與獎品
28. ⏳ **OrderController** - 前台訂單
29. ⏳ **PrizeBoxController** - 前台獎品箱

### 內容與資訊
30. ⏳ **NewsController** - 前台最新消息
31. ⏳ **BannerController** - 前台 Banner
32. ⏳ **MarqueeController** - 前台跑馬燈
33. ⏳ **EnumController** - 列舉值 API
34. ⏳ **DistrictController** - 地區資料

### 其他
35. ⏳ **RechargePlanController** - 前台儲值方案
36. ⏳ **ReferralCodeValidateController** - 推薦碼驗證
37. ⏳ **StoreOptionController** - 店家選項
38. ⏳ **OAuth2Controller** - OAuth2 認證
39. ⏳ **RandomDrawController** - 隨機抽獎

---

## 🧪 測試模板結構

### 基本結構
```java
@WebMvcTest(XxxController.class)
@Import(SecurityConfig.class)
class XxxControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private XxxService service;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 測試方法...
}
```

### 測試案例包含
1. **Happy Path** - 正常流程測試
2. **Validation** - 輸入驗證測試
3. **Authorization** - 權限控制測試
4. **Error Handling** - 錯誤處理測試

---

## 📊 進度追蹤

### 已完成 (1/39)
- ✅ AdminLotteryWithPrizesController

### 待完成 (38/39)
- 後台 Controller: 18 個
- 前台 Controller: 20 個

### 預計時間
- 每個 Controller: 10-15 分鐘
- 總計: 約 7-10 小時

---

## 🚀 執行順序

### Phase 1: 核心業務 (優先)
1. AdminLotteryController
2. LotteryPrizeController
3. AdminOrderController
4. ApiAuthController
5. LotteryBrowseController
6. LotteryDrawController

### Phase 2: 管理功能
7. AdminUserController
8. AdminFrontendUserController
9. AdminNewsController
10. AdminBannerController

### Phase 3: 前台功能
11. UserController
12. OrderController
13. PrizeBoxController
14. NewsController

### Phase 4: 系統功能
15. MenuController
16. RoleController
17. PermissionController
18. UploadController

### Phase 5: 其他功能
19. 剩餘所有 Controller

---

## 📝 測試命名規範

### 測試類別
- 格式: `{Controller名稱}Test`
- 範例: `AdminLotteryControllerTest`

### 測試方法
- 格式: `{方法名稱}_Should{預期結果}_When{條件}`
- 範例: 
  - `createLottery_ShouldReturnCreated_WhenValidInput()`
  - `getLottery_ShouldReturn404_WhenNotFound()`
  - `updateLottery_ShouldReturn403_WhenUnauthorized()`

---

## ✅ 測試覆蓋率目標

- **Line Coverage**: > 80%
- **Branch Coverage**: > 70%
- **Method Coverage**: > 90%

---

**最後更新**: 2026-01-23  
**狀態**: 進行中  
**負責人**: AI Copilot
