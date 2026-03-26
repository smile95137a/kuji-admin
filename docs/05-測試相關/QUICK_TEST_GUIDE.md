# 🚀 快速測試指南

## 📋 當前狀態

✅ 應用程式正在啟動中...
✅ 測試命令腳本已準備好：`test-commands.bat`

---

## 🎯 第一次測試流程（5 分鐘快速驗證）

### 步驟 1：等待應用程式啟動完成

在 Terminal 中看到以下訊息表示啟動成功：
```
Started AdminApplication in X.XXX seconds
```

或透過瀏覽器檢查：
- 開啟：http://localhost:8080/api
- 應該顯示：`{"timestamp":"...","status":404,"error":"Not Found"...}`

### 步驟 2：開啟測試命令腳本

**在新的命令提示字元視窗執行：**
```bash
test-commands.bat
```

### 步驟 3：執行測試（按順序）

#### 3.1 取得 Token
- 選項：`1` - 取得 Admin Token
- 複製回應中的 `accessToken` 值（不含引號）

#### 3.2 設定 Token
- 選項：`2` - 設定 Token 環境變數
- 貼上剛才複製的 Token

#### 3.3 測試 Enum API（無需登入）
- 選項：`3` - 取得所有 Enum
- 驗證：回應包含 `bannerStatus`, `newsStatus` 等

#### 3.4 測試店家選項 API（無需登入）
- 選項：`6` - 取得所有店家選項
- **記下店家 ID**（後續測試 Banner 需要）

#### 3.5 測試圖片上傳（需準備測試圖片）
- 準備一張測試圖片（< 5MB）
- 選項：`9` - 上傳 Banner 圖片
- 輸入圖片路徑（例如：`C:\Users\user\Desktop\test.jpg`）
- **記下 imageUrl**（例如：`/img/banner/uuid-123.jpg`）

#### 3.6 測試 Banner 完整流程（關鍵！）
- 選項：`15` - 新增 Banner
  - 輸入店家 ID（步驟 3.4 記下的）
  - 輸入 imageUrl（步驟 3.5 記下的）
  - **記下 Banner ID**
  
- 選項：`16` - 查詢 Banner 列表（後台）
  - 驗證：可看到剛才建立的 Banner
  - 驗證：`storeName` 有正確顯示（**不是 null**）
  
- 選項：`17` - 上架 Banner
  - 輸入剛才記下的 Banner ID
  - 驗證：status 變更為 PUBLISHED
  
- 選項：`18` - 查詢輪播 Banner（前台）
  - 驗證：可看到剛才上架的 Banner
  - 驗證：`storeName` 有正確顯示

---

## ✅ 核心驗證清單

### 必測項目（優先）：
- [ ] 應用程式成功啟動（無錯誤）
- [ ] 可以取得 Admin Token
- [ ] Enum API 返回正確格式
- [ ] 店家選項 API 可用
- [ ] 圖片上傳成功
- [ ] **Banner 新增時可選擇店家**
- [ ] **Banner 查詢時 storeName 有正確顯示**（關鍵！）
- [ ] Banner 上架/下架正常
- [ ] 前台只顯示 PUBLISHED 的 Banner

---

## 🐛 可能遇到的問題

### 問題 1：應用程式啟動失敗
**錯誤訊息：** Bean creation error / S3 相關錯誤

**解決方案：**
```bash
# 確認檔案已刪除
dir src\main\java\com\group\admin\config\S3Config.java
# 應該顯示「找不到檔案」
```

### 問題 2：取得店家選項時返回空陣列
**原因：** 資料庫沒有測試用的店家資料

**解決方案：**
1. 檢查資料庫是否有 `store` 表
2. 檢查是否有 ACTIVE 狀態的店家
3. 如需建立測試資料，告訴我！

### 問題 3：Banner 的 storeName 顯示 null
**原因：** Service 沒有正確查詢店家資料

**檢查方法：**
- 查詢 Banner 列表時，檢查回應的 `storeName` 欄位
- 如果是 null，需要修正 `BannerServiceImpl.toRes()` 方法

### 問題 4：圖片上傳後無法存取
**檢查方法：**
```bash
# 檢查檔案是否存在
dir src\main\resources\static\img\banner

# 在瀏覽器開啟
http://localhost:8080/img/banner/uuid-123.jpg
```

---

## 📝 測試記錄表

| 測試項目 | 狀態 | 備註 |
|---------|------|------|
| 應用程式啟動 | ⏳ | 等待中... |
| 取得 Token | ⬜ | |
| Enum API | ⬜ | |
| 店家選項 API | ⬜ | |
| 圖片上傳 | ⬜ | |
| Banner 新增 | ⬜ | 關鍵：storeName 是否正確 |
| Banner 查詢 | ⬜ | 關鍵：storeName 是否正確 |
| Banner 上架 | ⬜ | |
| 前台輪播 | ⬜ | 關鍵：storeName 是否正確 |

---

## 🎯 測試完成標準

✅ **基本功能**
- 應用程式啟動無錯誤
- 所有 API 可正常回應
- 圖片上傳功能正常

✅ **核心功能**
- Banner 可以綁定店家
- Banner 查詢時 storeName 有正確顯示
- 前台只顯示符合條件的 Banner

✅ **符合需求文件**
- Banner 必須綁定店家（不可為空）
- 只有 Admin 可管理 Banner
- 支援上下架狀態切換
- 店家停用時 Banner 不顯示（需額外測試）

---

## 📞 需要協助時

如果遇到任何問題，請提供：
1. 錯誤訊息（完整日誌）
2. 執行的測試步驟
3. 預期行為 vs 實際行為

我會立即協助你解決！

---

**準備好了嗎？執行 `test-commands.bat` 開始測試！** 🚀
