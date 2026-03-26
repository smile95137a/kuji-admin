# 系統修正總結報告

## 📅 修正日期：2026-01-12

## ✅ 已完成的修正項目

### 1. 儲值方案排序修正
**檔案**：`RechargePlanServiceImpl.java`
**問題**：儲值方案的排序沒有按照資料輸入的順序排列
**修正**：
- 將 `getAllPlans()` 的排序從 `created_at DESC` 改為 `created_at ASC`
- 這樣先建立的方案會排在前面，符合輸入順序

### 2. 前台會員登入狀態檢查
**檔案**：`UserServiceImpl.java`
**問題**：被停用的會員仍可以登入前台
**修正**：
- 在 `login()` 方法新增會員狀態檢查
- 在 `loginWithGoogle()` 方法新增會員狀態檢查
- 停用 (`INACTIVE`)、刪除 (`DELETED`)、暫停 (`SUSPENDED`) 的會員無法登入

### 3. 移除會員刪除功能
**檔案**：`AdminFrontendUserController.java`
**問題**：刪除和停用功能重複
**修正**：
- 移除 `DELETE /{id}` 端點
- 保留停用功能 (`POST /{id}/deactivate`)
- 會員管理只能停用，不能刪除

### 4. 擴充前台會員註冊 Req
**檔案**：`AuthRegisterReq.java`
**問題**：註冊請求欄位太少
**修正**：新增以下欄位
- `phoneNumber` - 手機號碼（選填，09開頭10碼）
- `avatar` - 頭像 URL（選填）
- `referralCode` - 推薦碼（選填，待推薦碼機制實作）
- 新增驗證規則（@NotBlank、@Email、@Size、@Pattern）

### 5. 更新註冊服務支援新欄位
**檔案**：`UserServiceImpl.java`
**修正**：`register()` 方法現在會設定 `phoneNumber` 和 `avatar`

### 6. Admin 商品查詢排序優化
**檔案**：`LotteryServiceImpl.java`
**問題**：Admin 查詢所有店家商品時排序不佳
**修正**：
- 當沒有指定 storeId 時，預設按 `store_id ASC, created_at DESC` 排序
- 同一店家的商品會排在一起，最新的在前面

### 7. 前台商品詳情資訊完整性
**檔案**：`LotteryRes.java`、`LotteryServiceImpl.java`
**問題**：`/api/lottery/browse/{id}` 缺少賞品數量、籤況、保護時間、content
**修正**：新增以下欄位
- `protectionDraws` - 保護抽數（大獎保底）
- `protectionMinutes` - 保護時間（搶購保護）
- `content` - 商品內容詳情（CKEditor 富文本）
- `gameMode` - 遊戲模式
- `freeDrawEnabled` - 是否啟用免費抽
- `designatedPrizeNumbers` - 指定號碼（籤況）
- `ticketsGenerated` - 是否已生成抽籤號碼

### 8. 儲值方案查詢條件功能
**新增檔案**：`RechargePlanCondition.java`
**修改檔案**：`RechargePlanService.java`、`RechargePlanServiceImpl.java`、`AdminRechargePlanController.java`
**修正**：
- 新增 `RechargePlanCondition` 查詢條件類別
- 支援條件：`name`（名稱模糊）、`isActive`、`amountMin`、`amountMax`
- 新增端點：`POST /admin/recharge-plan/query`

---

## 📝 API 變更摘要

### 移除的端點
| 方法 | 路徑 | 說明 |
|------|------|------|
| DELETE | `/admin/frontend-users/{id}` | 移除會員刪除功能 |

### 新增的端點
| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/admin/recharge-plan/query` | 儲值方案條件查詢 |

### 修改的端點
| 方法 | 路徑 | 變更說明 |
|------|------|----------|
| POST | `/auth/register` | 新增 phoneNumber、avatar、referralCode 欄位 |
| GET | `/lottery/browse/{id}` | 回應新增 protectionDraws、protectionMinutes、content 等欄位 |
| GET | `/admin/recharge-plan/list` | 排序改為 created_at ASC |
| POST | `/admin/lottery/list` | Admin 查詢時預設按 store_id, created_at 排序 |

---

## ⏳ 待實作功能（新功能需求）

### 1. 推薦碼機制 (`referal.prompt.md`)
- 店家專屬推薦碼
- 註冊時綁定推薦碼
- Admin 管理推薦碼

### 2. 運送管理整合 (`express.prompt.md`)
- 超商取貨
- 宅配到府
- 訂單關聯運送方式

### 3. 完整遊玩到訂單流程 (`gmaeToOreder.prompt.md`)
- 遊玩 → 賞品盒 → 訂單完整流程
- 賞品盒送出訂單資訊完整性

---

## 🧪 測試建議

### 1. 儲值方案排序測試
```bash
# 建立多個方案後查詢，確認順序
curl -X GET http://localhost:8080/api/admin/recharge-plan/list \
  -H "Authorization: Bearer {{token}}"
```

### 2. 會員登入狀態檢查測試
```bash
# 先停用會員
curl -X POST http://localhost:8080/api/admin/frontend-users/{{userId}}/deactivate \
  -H "Authorization: Bearer {{adminToken}}"

# 嘗試登入（應該失敗）
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "password123"}'
```

### 3. 前台商品詳情測試
```bash
# 查詢商品詳情，確認新欄位存在
curl -X GET http://localhost:8080/api/lottery/browse/{{lotteryId}}
```

### 4. 儲值方案條件查詢測試
```bash
# 查詢金額大於 500 的方案
curl -X POST http://localhost:8080/api/admin/recharge-plan/query \
  -H "Authorization: Bearer {{token}}" \
  -H "Content-Type: application/json" \
  -d '{"condition": {"amountMin": 500}}'
```

---

## 📁 修改的檔案清單

1. `src/main/java/com/group/admin/service/impl/RechargePlanServiceImpl.java`
2. `src/main/java/com/group/admin/service/impl/UserServiceImpl.java`
3. `src/main/java/com/group/admin/service/impl/LotteryServiceImpl.java`
4. `src/main/java/com/group/admin/controller/admin/AdminFrontendUserController.java`
5. `src/main/java/com/group/admin/req/AuthRegisterReq.java`
6. `src/main/java/com/group/admin/res/lottery/LotteryRes.java`
7. `src/main/java/com/group/admin/service/RechargePlanService.java`
8. `src/main/java/com/group/admin/req/recharge/RechargePlanCondition.java` (新增)
9. `src/main/java/com/group/admin/controller/admin/AdminRechargePlanController.java`
