# Controller 測試進度報告（更新）

## 執行狀態更新

📅 **最後更新**: 2026-01-23 00:45  
🎯 **當前階段**: 測試架構重構 → @SpringBootTest 模式

## 問題與解決方案

### 🔴 遇到的問題
使用 `@WebMvcTest` 時出現 MyBatis 依賴錯誤：
```
Error creating bean with name 'adminUserRoleMapper'
Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required
```

**根本原因**: `@WebMvcTest` 不自動配置 MyBatis，但 SecurityConfig 中的 Filter 需要 Mapper

### ✅ 解決方案
改用 `@SpringBootTest` + `@AutoConfigureMockMvc` + H2 記憶體資料庫

**變更內容**:
1. ✅ 添加 H2 依賴到 pom.xml
2. ✅ 建立 application-test.yml（H2 + MyBatis 配置）
3. ✅ 更新 AdminLotteryControllerTest 使用 @SpringBootTest
4. ✅ 更新 ApiAuthControllerTest 使用 @SpringBootTest
5. ⏳ 執行測試中...

## 測試架構變更

### 變更前 (@WebMvcTest)
```java
@WebMvcTest(AdminLotteryController.class)
@Import(SecurityConfig.class)
class AdminLotteryControllerTest {
    @MockBean private LotteryService lotteryService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private AdminUserMapper adminUserMapper;
    @MockBean private AdminUserRoleMapper adminUserRoleMapper;
    @MockBean private RoleMapper roleMapper;
    // ... 需要 mock 10+ 個 Mapper
}
```

**問題**: 每個測試都需要 mock 大量 Mapper

### 變更後 (@SpringBootTest)
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminLotteryControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private LotteryService lotteryService;  // 只 mock Service
    @Autowired private ObjectMapper objectMapper;
}
```

**優點**:
- ✅ 自動配置 MyBatis (H2)
- ✅ Filter 正常工作
- ✅ 只需 mock Service 層
- ✅ 真實測試 Security 配置

## 測試配置檔

### application-test.yml
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL
    driver-class-name: org.h2.Driver
    username: sa
    password:

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.group.admin.entity

jwt:
  secret: test-secret-key-for-testing
  expiration: 3600000
```

## 已完成的測試

### 1. AdminLotteryControllerTest（12 測試）
- ✅ 查詢商品列表（管理員/店主權限）
- ✅ 建立商品（驗證必填欄位）
- ✅ 更新商品
- ✅ 刪除商品
- ✅ 發布/下架商品
- ✅ 權限控制測試

### 2. ApiAuthControllerTest（15 測試）
- ✅ 會員註冊（完整欄位驗證）
  - 密碼確認檢查
  - Email 格式驗證
  - 手機格式驗證
  - 同意條款檢查
- ✅ 會員登入
- ✅ Token 刷新
- ✅ 忘記密碼
- ✅ 重設密碼

## 待執行測試

⏳ **執行中**: `mvn clean test -Dtest=AdminLotteryControllerTest,ApiAuthControllerTest`

預期結果:
- Tests run: 27 (12 + 15)
- Failures: 0
- Errors: 0
- Skipped: 0

## 下一步計劃

### 階段 1: 驗證測試架構 ✅
- [x] 修正 MyBatis 依賴問題
- [x] 建立 H2 測試環境
- [x] 更新現有測試
- [ ] 確認測試全數通過（執行中）

### 階段 2: 批次生成測試（待執行）
需要為以下 Controller 建立測試:

**Phase 1 - 核心業務 (6 個)**:
1. AdminLotteryPrizeController - 獎品 CRUD
2. AdminOrderController - 訂單管理
3. AdminPrizeBoxController - 獎品箱管理
4. AdminStoreController - 店家管理
5. ApiOrderController - 前台訂單
6. ApiLotteryController - 前台商品

**Phase 2 - 認證/使用者 (3 個)**:
7. AdminAuthController - 後台認證
8. AdminUserController - 使用者管理
9. ApiUserController - 前台使用者

**Phase 3 - 內容管理 (4 個)**:
10. AdminBannerController - Banner 管理
11. AdminNewsController - 公告管理
12. ApiBannerController - 前台 Banner
13. ApiNewsController - 前台公告

**Phase 4 - 前台功能 (6 個)**:
14. ApiDistrictController - 行政區
15. ApiWalletController - 錢包
16. ApiAddressController - 地址
17. ApiPrizeBoxController - 獎品箱
18. ApiCollectionController - 收藏
19. ApiReferralController - 推薦

**Phase 5 - 系統功能 (3 個)**:
20. AdminRoleController - 角色管理
21. AdminMenuController - 選單管理
22. ApiMenuController - 前台選單

**Phase 6 - 其他 (15 個)**:
23-37. 其他各類 Controller

### 階段 3: 測試覆蓋率報告
```bash
mvn clean test jacoco:report
```

目標:
- Line Coverage: >80%
- Branch Coverage: >70%
- Method Coverage: >90%

## 測試執行指令

```bash
# 執行單一測試
mvn test -Dtest=AdminLotteryControllerTest

# 執行所有 Controller 測試
mvn test -Dtest=*ControllerTest

# 執行測試並生成覆蓋率報告
mvn clean test jacoco:report

# 查看測試報告
open target/surefire-reports/*.txt
```

## 技術文件

- ✅ CONTROLLER_TEST_ISSUE_AND_SOLUTION.md - 問題分析與解決方案
- ✅ CONTROLLER_TEST_PLAN.md - 測試計劃
- ✅ CONTROLLER_TEST_PROGRESS.md - 進度追蹤（本文件）
- ✅ application-test.yml - 測試配置
- ✅ pom.xml - H2 依賴已添加

## 預估時間

- ✅ 測試架構重構: 完成
- ⏳ 驗證現有測試: 執行中（預計 2-3 分鐘）
- 🔜 批次生成測試: 預計 2-3 小時（37 個 Controller）
- 🔜 測試覆蓋率優化: 預計 1-2 小時

**總計**: 約 4-6 小時完成所有 Controller 測試

---

✨ **備註**: 使用 @SpringBootTest 後，測試更接近實際執行環境，可以測試完整的 Security Filter Chain 和權限控制邏輯。
