# 🚀 API 修復快速參考

## 問題 1：空字串查詢 ✅ 已解決

### 為什麼空字串會查不到資料？
```json
// ❌ 這樣會查不到資料
{
  "condition": {
    "storeId": "",  // 空字串被當作條件
    "status": ""
  }
}
```

### 解決方案
✅ **已內建處理**：`isNotBlank()` 方法會把空字串視為 null（不過濾）

```java
// LotteryServiceImpl.java Line 1506-1509
private boolean isNotBlank(String str) {
    return str != null && !str.trim().isEmpty();
}
```

### 現在可以這樣用
```json
// ✅ 空字串 = 不過濾（查詢全部）
{
  "condition": {
    "storeId": "",      // 查詢所有店家
    "status": "",       // 查詢所有狀態
    "category": "GACHA" // 只過濾分類
  }
}
```

---

## 問題 2：註冊 API 欄位擴充 ✅ 已完成

### 新的註冊格式
```json
{
  "email": "a0930200677@gmail.com",
  "password": "123456",
  "confirmPassword": "123456",      // ← 新增（必填）
  "nickname": "robin",              // ← 改為必填
  "phoneNumber": "0930200677",      // ← 改為必填
  "addressName": "王",               // ← 新增（必填）
  "zipCode": "103",                 // ← 新增（必填）
  "city": "臺北市",                  // ← 新增（必填）
  "area": "大同區",                  // ← 新增（必填）
  "address": "地址地址",             // ← 新增（必填）
  "lineId": "tobinline",            // ← 新增（選填）
  "agreeTerms": true,               // ← 新增（必填，必須 true）
  "referralCode": "robinRobin"      // ← 保留（選填）
}
```

### 驗證規則
| 欄位 | 類型 | 規則 | 錯誤訊息 |
|------|------|------|---------|
| `email` | String | Email 格式 | "Email 格式不正確" |
| `password` | String | 6-100 字元 | "密碼長度必須在 6-100 字元之間" |
| `confirmPassword` | String | 必須與 password 相同 | "密碼與確認密碼不一致" |
| `nickname` | String | 必填，最多 50 字元 | "暱稱不能為空" |
| `phoneNumber` | String | 09開頭10碼數字 | "手機號碼格式不正確" |
| `addressName` | String | 必填，最多 50 字元 | "收件人姓名不能為空" |
| `zipCode` | String | 3-5位數字 | "郵遞區號格式不正確" |
| `city` | String | 必填 | "縣市不能為空" |
| `area` | String | 必填 | "區域不能為空" |
| `address` | String | 必填，最多 200 字元 | "詳細地址不能為空" |
| `lineId` | String | 選填，最多 100 字元 | - |
| `agreeTerms` | Boolean | 必須為 true | "必須同意服務條款" |
| `referralCode` | String | 選填，最多 20 字元 | - |

---

## 測試方式

### 測試商品查詢（空字串處理）
```bash
curl -X POST http://localhost:8080/api/admin/lottery-with-prizes/list \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"condition":{"storeId":"","status":""}}'
```

**預期**: 返回所有商品

### 測試註冊 API
```bash
# 執行測試腳本
test-register-api.bat
```

**測試項目**:
1. ✅ 完整資料註冊
2. ❌ 密碼不一致（會被攔截）
3. ❌ 缺少必填欄位（會被攔截）

### 手動測試註冊
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "123456",
    "confirmPassword": "123456",
    "nickname": "測試",
    "phoneNumber": "0912345678",
    "addressName": "王小明",
    "zipCode": "103",
    "city": "臺北市",
    "area": "大同區",
    "address": "測試路123號",
    "agreeTerms": true
  }'
```

---

## 修改檔案
1. ✅ `AuthRegisterReq.java` - 新增 8 個欄位
2. ✅ `UserServiceImpl.java` - 更新註冊邏輯
3. ✅ `ApiAuthController.java` - 新增密碼驗證
4. ✅ `test-register-api.bat` - 測試腳本

## 編譯狀態
```
✅ mvn compile -DskipTests
   BUILD SUCCESS
```

---

## ⚠️ 注意事項

### zipCode 欄位
`zipCode` 欄位**目前未存入資料表**（User 表沒有對應欄位）

如需儲存，請執行：
```sql
ALTER TABLE `user` 
ADD COLUMN `zip_code` VARCHAR(10) COMMENT '郵遞區號' 
AFTER `district`;
```

### 資料表對應
- `area` (前端) → `district` (資料表)
- `addressName` (前端) → `recipient_name` (資料表)
- `address` (前端) → `address_detail` (資料表)

---

**版本**: 2.1.0 | **狀態**: ✅ 完成 | **更新時間**: 2026-01-22 11:00
