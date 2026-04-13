# 功能規格書：程式碼品質修復

**功能分支**：`022-code-quality`
**建立日期**：2026-04-13
**狀態**：草稿
**輸入**：分層違規修復、重複程式碼清理、廢棄檔案移除、Service 去重

## 使用者情境與測試

### 使用者故事 1 — 修復 Controller 直接呼叫 Mapper 的分層違規（優先級：P1）

身為開發者，我希望所有 Controller 只透過 Service 層存取資料，不直接注入 Mapper，確保業務邏輯集中在 Service 層。

**此優先級的原因**：7 個 Controller 直接使用 Mapper，違反專案架構規範，增加維護成本且無法統一事務管理。

**驗收情境**：

1. **在** 修復完成後，**當** 搜尋所有 Controller 中的 `@Autowired *Mapper`，**則** 回傳 0 結果。
2. **在** 修復完成後，**當** Controller 原有功能呼叫，**則** 行為完全不變（功能等價）。

---

### 使用者故事 2 — 移除重複與廢棄程式碼（優先級：P2）

身為開發者，我希望移除廢棄的 Filter 檔案、重複的 ApiResponse 類別、以及 LotteryServiceImpl 中新舊版本重複的方法。

**驗收情境**：

1. **在** 移除廢棄 Filter 後，**當** 搜尋 `JwtAuthenticationFilter`（非 Admin/Api 前綴），**則** 回傳 0 結果。
2. **在** 移除重複 ApiResponse 後，**當** 搜尋 `res/common/ApiResponse`，**則** 只有 `result/ApiResponse` 存在。
3. **在** LotteryServiceImpl 去重後，**當** 搜尋方法名稱，**則** 沒有 `_old` / `_v2` / 重複簽名的方法。

---

### 邊界情況

- 移除檔案後有其他地方引用？必須先做全域搜索確認零引用再刪除。
- LotteryServiceImpl 去重時邏輯不確定保留哪個版本？以現行 Controller 呼叫的版本為準。

## 需求規格

### 功能需求

#### FR-001：修復 7 個 Controller 的分層違規

| Controller | 直用的 Mapper | 修復方式 |
|------------|---------------|----------|
| `UserController` | UserMapper | 抽至 UserService |
| `AdminUserController` | AdminUserMapper / UserMapper | 抽至 AdminUserService |
| `AdminStoreController` | StoreMapper / StoreUserMapper | 抽至 StoreService |
| `AdminLotteryWithPrizesController` | LotteryPrizeMapper | 整合至 LotteryService（Spec 019 合併 Controller 後一併處理） |
| `AdminLotteryController` | StoreUserMapper | 改用 SecurityUtils.getCurrentUserPrimaryStoreId() |
| `StoreOptionController` | StoreMapper | 抽至 StoreService |
| `LotteryLockController` | LotteryLockMapper | 抽至 LotteryLockService |

#### FR-002：修復 OAuth2Controller 手動建立 ApiResponse

`OAuth2Controller` 中手動 `new ApiResponse<>(...)` 的程式碼改為直接回傳物件，讓 AOP `GlobalResponseAspect` 自動包裝。

#### FR-003：移除重複 ApiResponse 類別

- 保留 `com.group.admin.result.ApiResponse`
- 刪除 `com.group.admin.res.common.ApiResponse`
- 更新所有 `import com.group.admin.res.common.ApiResponse` → `import com.group.admin.result.ApiResponse`

#### FR-004：移除廢棄 Filter

- 刪除 `filter/JwtAuthenticationFilter.java`
- 刪除 `filter/JwtAuthenticationFilterSkipApi.java`
- 確認 SecurityConfig 未引用這些 Filter

#### FR-005：修復 SystemLogServiceImpl

`SystemLogServiceImpl` 不應直接引用 `HttpServletRequest`。將 request 相關資訊（IP、User-Agent）的擷取移至 Controller 或 AOP 層，Service 只接收純資料參數。

#### FR-006：LotteryServiceImpl 方法去重

目前 29 個 public 方法中有新舊版本重複。識別並移除不再使用的舊版本方法。

### 核心實體

無新增資料表。純程式碼品質修復。

## 成功標準

- **SC-001**：`grep -r "@Autowired.*Mapper" src/main/java/com/group/admin/controller/` 回傳 0 結果。
- **SC-002**：`grep -r "res.common.ApiResponse" src/` 回傳 0 結果。
- **SC-003**：`find src -name "JwtAuthenticationFilter.java" -not -name "Admin*" -not -name "Api*"` 回傳 0。
- **SC-004**：`mvn clean package -DskipTests` 編譯通過。
- **SC-005**：所有現有 API 功能行為不變。

## 假設前提

- 此 Spec 可與其他 Spec 平行執行，無硬性依賴。
- 部分修復（如 AdminLotteryWithPrizesController）可能在 Spec 019 Controller 合併時一併處理，此處記錄但不重複執行。
- LotteryServiceImpl 去重需逐一比對呼叫方，無法自動化，需人工審查。
