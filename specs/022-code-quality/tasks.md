# 任務清單：程式碼品質修復

**輸入**：設計文件來自 `/specs/022-code-quality/`
**分支**：`022-code-quality` | **建立日期**：2026-04-13

---

## 第一階段：移除廢棄檔案

**目的**：清理不再使用的程式碼，減少混淆。

- [ ] T001 [P] 確認 `filter/JwtAuthenticationFilter.java` 無引用後刪除
- [ ] T002 [P] 確認 `filter/JwtAuthenticationFilterSkipApi.java` 無引用後刪除
- [ ] T003 [P] 確認 `res/common/ApiResponse.java` 的引用全部改為 `result/ApiResponse.java` 後刪除
- [ ] T004 更新所有 `import com.group.admin.res.common.ApiResponse` → `import com.group.admin.result.ApiResponse`

**檢查點**：3 個廢棄檔案已刪除，編譯通過

---

## 第二階段：修復 Controller 分層違規

**目的**：將 7 個 Controller 中直接呼叫 Mapper 的邏輯抽至 Service 層。

- [ ] T005 **UserController**：移除 `@Autowired UserMapper`，新增 Service 方法（如 `userService.findByXxx()`），Controller 改為呼叫 Service
- [ ] T006 **AdminUserController**：移除 Mapper 注入，業務邏輯移至 `AdminUserService`
- [ ] T007 **AdminStoreController**：移除 StoreMapper / StoreUserMapper 注入，移至 `StoreService`
- [ ] T008 **AdminLotteryController**：移除 StoreUserMapper，改用 `SecurityUtils.getCurrentUserPrimaryStoreId()`
- [ ] T009 **StoreOptionController**：移除 StoreMapper，改用 `StoreService`
- [ ] T010 **LotteryLockController**：移除 LotteryLockMapper，改用 `LotteryLockService`
- [ ] T011 **AdminLotteryWithPrizesController**：若 Spec 019 已合併 Controller，確認此處邏輯已移至 Service；若尚未合併，將 LotteryPrizeMapper 操作移至 `LotteryService`

**檢查點**：`grep -r "@Autowired.*Mapper" src/main/java/com/group/admin/controller/` 回傳 0

---

## 第三階段：修復 OAuth2Controller 與 SystemLogServiceImpl

- [ ] T012 **OAuth2Controller**：找出所有手動建立 `ApiResponse` 的地方，改為直接回傳業務物件，讓 AOP 包裝
- [ ] T013 **SystemLogServiceImpl**：
  - 移除 `HttpServletRequest` 依賴
  - 在 AOP 層（如 `GlobalResponseAspect` 或新建 `SystemLogAspect`）擷取 IP / User-Agent
  - Service 方法只接收 `String ip, String userAgent` 等純資料參數

**檢查點**：Service 層零 Servlet 依賴

---

## 第四階段：LotteryServiceImpl 方法去重

- [ ] T014 列出 `LotteryServiceImpl` 所有 29 個 public 方法
- [ ] T015 用 `list_code_usages` 找出每個方法的所有呼叫者
- [ ] T016 識別並標記不再被呼叫的方法（候選刪除）
- [ ] T017 識別功能重複的方法對（如 `getXxx` vs `getXxxV2`），保留被使用的版本，刪除另一個
- [ ] T018 確認刪除後所有 Controller / Service 引用正常

**檢查點**：LotteryServiceImpl 方法數量精簡至 ~15-20 個

---

## 第五階段：最終驗證

- [ ] T019 `mvn clean package -DskipTests` 確認編譯通過
- [ ] T020 抽樣測試 5 個核心 API 確認行為不變（登入、商品列表、抽獎、賞品盒、訂單）

---

## 依賴關係

```
第一階段（移除廢棄） — 無依賴，立即開始
第二階段（分層修復） — 部分依賴 Spec 019（Controller 合併），獨立部分可先做
第三階段（OAuth2/Log） — 無依賴
第四階段（去重） — 依賴 Spec 019（因為 Controller 合併可能改變呼叫關係）
第五階段（驗證） — 依賴全部完成
```

**注意**：此 Spec 的第一、三階段可與其他 Spec 完全平行執行。
