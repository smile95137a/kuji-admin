# 驗證衝突分析報告

## 問題根源

當使用 `@Valid` 驗證 DTO 時，Spring Validation 會在 Controller 方法執行**之前**就檢查所有 `@NotBlank`、`@NotNull` 註解，導致後端無法實作「自動帶入」邏輯。

## 執行順序

```
1. HTTP 請求到達 
   ↓
2. Spring 反序列化 JSON → DTO 物件
   ↓
3. @Valid 驗證 DTO（@NotBlank、@NotNull 等）  ← ⚠️ 在這裡就失敗！
   ↓ (如果驗證失敗，直接返回 400)
4. Controller 方法執行
   ↓
5. 自動帶入邏輯（永遠執行不到）
```

---

## 已修正的問題

### ✅ LotteryCreateReq.storeId

**問題**：
```java
@NotBlank(message = "店家ID不可為空")  // ← 驗證在 Controller 之前
private String storeId;
```

**症狀**：
```json
POST /api/admin/lottery
{ "storeId": "" }

返回：
{ "error": { "message": "店家ID不可為空" } }
```

**修正**：
```java
// ⚠️ 不加 @NotBlank 驗證，因為：
// - StoreOwner 可以不傳，後端自動帶入
// - Admin 必須明確指定，由 Controller 驗證
private String storeId;
```

**Controller 處理**：
```java
if (req.getStoreId() == null || req.getStoreId().isBlank()) {
    if (isAdmin) {
        throw new BusinessException("Admin 新增商品時必須指定店家 ID");
    }
    // StoreOwner 自動帶入
    String storeId = queryStoreIdFromDatabase(userId);
    req.setStoreId(storeId);
}
```

---

## 潛在問題清單

### 🔴 高風險：需要修正

#### 1. LotteryPrizeCreateReq.lotteryId

**檔案**：`req/lottery/LotteryPrizeCreateReq.java`

**問題**：
```java
@NotBlank(message = "抽獎活動ID不可為空")
private String lotteryId;
```

**場景**：
- 前端在商品詳情頁新增獎項時，已經知道 `lotteryId`（從 URL 取得）
- 後端可以從路徑參數自動帶入：`POST /admin/lottery/{lotteryId}/prizes`

**影響**：
- 前端必須手動傳 `lotteryId`（雖然 URL 已經有了）
- 前後端重複傳遞相同資訊

**建議**：
```java
// 方案 1：移除驗證，從路徑參數自動帶入
private String lotteryId;

// 方案 2：保持驗證，但前端必須傳（目前設計）
@NotBlank(message = "抽獎活動ID不可為空")
private String lotteryId;
```

**推薦**：**方案 2**（保持目前設計）  
理由：前端有 lotteryId，不算額外負擔。

---

#### 2. BannerCreateReq.storeId

**檔案**：`req/banner/BannerCreateReq.java`

**問題**：
```java
@NotBlank(message = "店家 ID 不能為空")
private String storeId;
```

**場景**：
- Banner 只有 Admin 才能新增
- Admin 必須明確指定要推廣哪個店家

**影響**：
- Admin 必須傳 storeId（這是正確的）
- 但如果未來開放 StoreOwner 自己申請 Banner，就會有問題

**建議**：
```java
// 目前 OK（Admin 必須傳）
// 但根據 banner.prompt.md：「Banner 為平台（Admin）主控資源」
// 所以保持 @NotBlank 是正確的
@NotBlank(message = "店家 ID 不能為空")
private String storeId;
```

**推薦**：**保持不變**  
理由：符合需求文件（只有 Admin 可新增，必須指定店家）

---

### 🟡 中風險：建議檢查

#### 3. CreateStoreEditorReq.storeId

**檔案**：`req/admin/CreateStoreEditorReq.java`

**可能問題**：
```java
@NotNull(message = "店家 ID 不可為空")
private String storeId;
```

**場景**：
- Admin 新增店家編輯時，必須指定哪個店家
- StoreOwner 新增自己店家的編輯時，應該自動帶入

**目前設計**：
- 只有 Admin 可以新增編輯？
- 還是 StoreOwner 也可以？

**需要確認**：
```java
// 如果 StoreOwner 也可以新增編輯
// → 需要移除 @NotNull，改為自動帶入
// 
// 如果只有 Admin 可以新增
// → 保持 @NotNull
```

---

### 🟢 低風險：目前 OK

#### 4. NewsCreateReq（無 storeId）

**檔案**：`req/news/NewsCreateReq.java`

**狀態**：✅ 正確  
**理由**：News 不綁定店家，只有 Admin 可新增

---

#### 5. 其他 CreateReq

以下 Req 不涉及自動帶入，驗證邏輯正確：

- `RoleCreateReq`：Admin 新增角色
- `MenuCreateReq`：Admin 新增選單
- `DrawReq`、`MultiDrawReq`：前端用戶抽獎（必須傳 lotteryId）

---

## 修正建議

### 立即修正（已完成）

- ✅ `LotteryCreateReq.storeId` - 已移除 `@NotBlank`

### 需要討論

1. **CreateStoreEditorReq.storeId**
   - 問題：StoreOwner 能否新增自己店家的編輯？
   - 如果可以 → 需要移除 `@NotNull`，改為自動帶入
   - 如果不行 → 保持不變

2. **LotteryPrizeCreateReq.lotteryId**
   - 問題：是否從路徑參數自動帶入？
   - 建議：保持目前設計（前端傳入）

---

## 檢查清單

### 已檢查的檔案（20+ 個）

| 檔案 | storeId | lotteryId | 其他必填欄位 | 狀態 |
|------|---------|-----------|--------------|------|
| LotteryCreateReq | ✅ 已修正 | - | ✅ 正確 | ✅ |
| LotteryPrizeCreateReq | - | 🟡 討論 | ✅ 正確 | 🟡 |
| BannerCreateReq | ✅ 正確 | - | ✅ 正確 | ✅ |
| NewsCreateReq | N/A | - | ✅ 正確 | ✅ |
| CreateStoreEditorReq | 🟡 討論 | - | ✅ 正確 | 🟡 |
| UpdateStoreReq | N/A | - | ✅ 正確 | ✅ |
| RoleCreateReq | N/A | - | ✅ 正確 | ✅ |
| MenuCreateReq | N/A | - | ✅ 正確 | ✅ |
| DrawReq | N/A | ✅ 正確 | ✅ 正確 | ✅ |

---

## 設計原則（未來參考）

### 何時使用 @NotBlank/@NotNull

1. **前端必須傳**且**後端不可能知道**的欄位
   - ✅ 使用驗證註解
   - 例如：title、content、username

2. **後端可以自動帶入**的欄位
   - ❌ 不使用驗證註解
   - 在 Controller 手動驗證
   - 例如：storeId（StoreOwner 自動帶入）、userId（從 Token 取得）

3. **路徑參數已包含**的欄位
   - ⚠️ 視情況決定
   - 如果前端容易取得 → 可以要求傳入（驗證一致性）
   - 如果前端不方便 → 從路徑參數自動帶入

### 驗證策略

```java
// ❌ 錯誤：驗證衝突
@NotBlank(message = "店家ID不可為空")
private String storeId;

// Controller
if (req.getStoreId() == null) {
    req.setStoreId(autoFillStoreId());  // 永遠執行不到
}

// ✅ 正確：手動驗證
// 不加 @NotBlank
private String storeId;

// Controller
if (req.getStoreId() == null || req.getStoreId().isBlank()) {
    if (需要自動帶入) {
        req.setStoreId(autoFillStoreId());
    } else {
        throw new BusinessException("必須指定店家ID");
    }
}
```

---

## 總結

| 問題類型 | 數量 | 狀態 |
|----------|------|------|
| 已修正 | 1 | ✅ LotteryCreateReq.storeId |
| 需討論 | 2 | 🟡 CreateStoreEditorReq, LotteryPrizeCreateReq |
| 設計正確 | 17+ | ✅ 其他所有 CreateReq |

**建議動作**：
1. ✅ 立即測試 `LotteryCreateReq` 修正（已完成）
2. 🟡 討論 `CreateStoreEditorReq` 的權限設計
3. 🟡 確認 `LotteryPrizeCreateReq` 是否需要從路徑自動帶入
4. ✅ 其他 API 維持現狀

---

**最後更新**：2026-01-07  
**責任人**：AI Coding Agent  
**審核狀態**：待用戶確認
