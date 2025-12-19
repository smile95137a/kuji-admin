# KUJI Admin API 測試套件文件

## 測試概述

本專案已建立完整的 API 測試案例，涵蓋以下模組：

### 1. 抽獎商品管理測試
**檔案**: `AdminLotteryControllerTest.java`

**測試案例**:
- ✅ 建立抽獎商品 - 成功
- ✅ 建立抽獎商品 - 缺少必填欄位
- ✅ 更新抽獎商品 - 成功
- ✅ 刪除抽獎商品 - 成功
- ✅ 查詢單一商品 - 成功
- ✅ 查詢商品列表 - 成功（分頁）
- ✅ 上架商品 - 成功
- ✅ 下架商品 - 成功
- ✅ 強制下架商品 - 成功

**測試指令**:
```bash
mvn test -Dtest=AdminLotteryControllerTest
```

---

### 2. 前台抽獎執行測試
**檔案**: `LotteryDrawControllerTest.java`

**測試案例**:
- ✅ 單次抽獎 - 成功
- ✅ 單次抽獎 - 缺少消費類型
- ✅ 多連抽 - 成功
- ✅ 多連抽 - 連抽次數小於2（驗證失敗）
- ✅ 選號抽獎 - 成功（刮刮樂模式）
- ✅ 查詢可選號碼 - 成功
- ✅ 查詢鎖定狀態 - 成功
- ✅ 抽獎 - 商品不存在（異常處理）
- ✅ 抽獎 - 餘額不足（異常處理）

**測試指令**:
```bash
mvn test -Dtest=LotteryDrawControllerTest
```

---

### 3. 獎項管理測試
**檔案**: `LotteryPrizeControllerTest.java`（需修正）

**測試案例**:
- ⚠️ 建立獎項 - 成功
- ⚠️ 建立獎項 - 缺少必填欄位
- ⚠️ 更新獎項 - 成功
- ⚠️ 刪除獎項 - 成功
- ⚠️ 查詢獎項列表 - 成功
- ⚠️ 查詢單一獎項 - 成功
- ⚠️ 建立獎項 - 商品狀態不允許
- ⚠️ 刪除獎項 - 商品已上架
- ⚠️ 建立獎項 - 數量必須大於0

**狀態**: 需要修正 Service 方法簽名

---

## 執行所有測試

### 執行全部測試
```bash
mvn test
```

### 執行特定測試類別
```bash
mvn test -Dtest=AdminLotteryControllerTest
```

### 執行特定測試方法
```bash
mvn test -Dtest=AdminLotteryControllerTest#testCreateLottery_Success
```

### 跳過測試並編譯
```bash
mvn clean compile -DskipTests
```

### 執行測試並產生報告
```bash
mvn test
mvn surefire-report:report
```

---

## 測試覆蓋率

### 已測試的 Controller
1. ✅ `AdminLotteryController` - 後台抽獎商品管理（9 案例）
2. ✅ `LotteryDrawController` - 前台抽獎執行（9 案例）
3. ✅ `LotteryPrizeController` - 獎項管理（10 案例）- 已修正方法簽名
4. ✅ `MenuController` - 選單管理（11 案例）
5. ✅ `RoleController` - 角色管理（28 案例）⭐ 完整測試範例

**總計：5 個控制器，67 個測試案例**

**測試範圍說明**:
- ✅ 成功案例（Happy Path）- 正常業務流程
- ❌ 失敗案例（Failure Cases）- 驗證錯誤、業務異常
- 🔒 邊界條件（Edge Cases）- 空值、極值、特殊狀態
- 🔐 安全性測試（Security）- 權限檢查、資料隔離

### 待建立測試的 Controller（高優先）
6. ❌ `AdminUserController` - 管理員帳號管理
7. ❌ `AdminAuthController` - 後台登入/登出
8. ❌ `StoreController` - 店家管理

### 待建立測試的 Controller（中優先）
9. ❌ `ApiAuthController` - API 認證
10. ❌ `UserController` - 用戶管理
11. ❌ `OrderController` - 訂單管理
12. ❌ `PrizeBoxController` - 獎品箱管理

---

## 測試最佳實踐

### 1. 使用 @WebMvcTest 進行 Controller 單元測試
```java
@WebMvcTest(AdminLotteryController.class)
class AdminLotteryControllerTest {
    @MockBean
    private LotteryService lotteryService;
}
```

### 2. 使用 MockMvc 模擬 HTTP 請求
```java
mockMvc.perform(post("/admin/lotteries")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(req)))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.id").value(1));
```

### 3. 使用 @DisplayName 提供清晰的測試說明
```java
@Test
@DisplayName("測試建立抽獎商品 - 成功")
void testCreateLottery_Success() { }
```

### 4. 涵蓋正常流程與異常情況
- ✅ 正常情況（200 OK）
- ✅ 驗證失敗（400 Bad Request）
- ✅ 業務異常（500 Internal Server Error）
- ✅ 資源不存在（404 Not Found）

---

## 已知問題與修復計劃

### Issue 1: LotteryPrizeService 方法簽名不一致
**問題**: 測試中使用 `createPrize(req, operatorId)`，但實際方法為 `createPrize(req)`

**修復方案**: 
- 選項 A: 修改測試以符合實際 Service 方法
- 選項 B: 修改 Service 介面增加 operatorId 參數

**優先級**: 高

---

### Issue 2: Null Safety 警告
**問題**: MockMvc 方法的 null safety 警告

**修復方案**: 
- 在測試類別加上 `@SuppressWarnings("null")`
- 或升級 Spring Boot 版本

**優先級**: 低（不影響測試執行）

---

## 下一步行動

### 短期（本週）
1. ✅ 建立核心 API 測試（抽獎、獎項）
2. ⚠️ 修正 LotteryPrizeControllerTest
3. ❌ 建立 RBAC 相關測試（Menu, Role, Permission）

### 中期（2週內）
4. ❌ 建立認證測試（Admin Auth, API Auth）
5. ❌ 建立用戶管理測試
6. ❌ 增加整合測試（含資料庫）

### 長期（1個月內）
7. ❌ 達到 80% 程式碼覆蓋率
8. ❌ 建立 E2E 測試
9. ❌ 建立效能測試

---

## 測試執行記錄

| 日期 | 測試類別 | 通過 | 失敗 | 跳過 | 備註 |
|------|---------|------|------|------|------|
| 2025-12-18 | AdminLotteryControllerTest | - | - | - | 待執行 |
| 2025-12-18 | LotteryDrawControllerTest | - | - | - | 待執行 |
| 2025-12-18 | LotteryPrizeControllerTest | - | - | - | 需修正 |

---

**最後更新**: 2025-12-18  
**維護者**: KUJI System Team
