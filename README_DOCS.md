# 📚 KUJI 後端文件總覽

> 最後更新：2026-01-15

## 📁 文件分類說明

### 🚀 部署相關 (Deployment)
部署、上線、環境配置相關的所有文件

| 文件名 | 用途 | 何時使用 |
|--------|------|----------|
| `DEPLOY_GUIDE.md` | **完整部署指南** | 第一次部署或需要詳細步驟時 |
| `CORS_FIX_AND_DEPLOY.md` | **CORS 修復與部署** | CORS 問題排查與部署 |
| `EC2_JAVA_SETUP_GUIDE.md` | EC2 Java 環境設定 | 新 EC2 實例初始化 |
| `EC2_QUICK_DEPLOY_COMMANDS.md` | EC2 快速命令參考 | 需要快速查詢命令時 |
| `HEALTH_CHECK_SETUP.md` | 健康檢查設定 | 設定監控端點 |
| `AWS_S3_SETUP_GUIDE.md` | S3 圖片儲存設定 | 設定圖片上傳功能 |
| `LOCAL_FILE_UPLOAD_SUMMARY.md` | 本地檔案上傳總結 | 開發環境檔案上傳 |

**🎯 快速開始：** 直接執行 `quick-deploy.bat` 即可部署

---

### 📖 API 測試指南 (API Guides)
API 測試、Postman Collection、前端對接文件

| 文件名 | 用途 | 何時使用 |
|--------|------|----------|
| `API_TEST_GUIDE.md` | **完整 API 測試指南** | 測試所有 API 功能 |
| `API_TEST_GUIDE_PRIZE_BOX_WALLET_ORDER.md` | 賞品盒/錢包/訂單 API | 測試核心交易流程 |
| `LOTTERY_COPY_API_TEST_GUIDE.md` | 商品複製 API 測試 | 測試商品複製功能 |
| `GET_USER_MENU_GUIDE.md` | 選單 API 測試 | 測試後台選單權限 |
| `COMPLETE_TEST_PLAN.md` | 完整測試計劃 | 系統全面測試 |
| `FRONTEND_API_REFERENCE.json` | 前端 API 參考 | 前端開發對接 |

**📦 Postman Collections:**
- `KUJI_Admin_API_Tests.postman_collection.json` - 後台管理 API
- `KUJI_Lottery_Copy_API.postman_collection.json` - 商品複製 API
- `KUJI_Prize_Box_Wallet_Order.postman_collection.json` - 核心交易 API

---

### ✨ 功能實作文件 (Features)
新功能開發、實作總結、技術細節

| 文件名 | 用途 | 何時使用 |
|--------|------|----------|
| `FEATURE_EXPANSION_SUMMARY.md` | **功能擴充總覽** | 了解所有新功能 |
| `FEATURE_TEST_GUIDE.md` | 功能測試指南 | 測試新功能 |
| `LOTTERY_COPY_COMPLETE_SUMMARY.md` | 商品複製功能總結 | 了解複製功能實作 |
| `LOTTERY_COPY_IMPLEMENTATION_REPORT.md` | 商品複製實作報告 | 技術實作細節 |
| `JWT_STOREID_ENHANCEMENT_COMPLETE.md` | JWT StoreID 增強 | 了解店家 ID 自動帶入 |
| `BANNER_IMPLEMENTATION_PLAN.md` | Banner 功能規劃 | Banner 功能開發 |

---

### 🏗️ 架構設計 (Architecture)
系統架構、設計決策、重構記錄

| 文件名 | 用途 | 何時使用 |
|--------|------|----------|
| `ARCHITECTURE_REFACTORING_SUMMARY.md` | **架構重構總結** | 了解系統架構變更 |
| `CONTROLLER_REORGANIZATION_GUIDE.md` | Controller 重組指南 | 了解 API 路由結構 |
| `CONTROLLER_REORGANIZATION_COMPLETE.md` | Controller 重組完成 | 查看重組結果 |
| `FRONTEND_BACKEND_STORE_API_SEPARATION.md` | 前後台 API 分離 | 了解前後台路由設計 |
| `DTO_IMPLEMENTATION_COMPLETE.md` | DTO 實作完成 | 了解資料傳輸物件設計 |
| `CONTROLLER_IMPLEMENTATION_COMPLETE.md` | Controller 實作完成 | 查看 Controller 實作 |

---

### 🐛 問題修復記錄 (Fix Records)
Bug 修復、問題排查、解決方案

| 文件名 | 用途 | 何時使用 |
|--------|------|----------|
| `COMPLETE_FIX_REPORT.md` | **完整修復報告** | 查看所有問題修復 |
| `API_403_FIX_SUMMARY.md` | API 403 錯誤修復 | 403 權限問題排查 |
| `API_SECURITY_IMPROVEMENTS.md` | API 安全性改進 | 了解安全性增強 |
| `CORS_FIX_AND_DEPLOY.md` | CORS 問題修復 | CORS 跨域問題 |
| `MENU_NULL_FIX.md` | 選單 null 修復 | 選單資料問題 |
| `MYBATIS_DUPLICATE_FIX.md` | MyBatis 重複問題 | MyBatis 配置問題 |
| `FOREIGN_KEY_FIX_GUIDE.md` | 外鍵錯誤修復 | 資料庫外鍵問題 |
| `DELETE_DUPLICATES_GUIDE.md` | 刪除重複資料 | 清理重複記錄 |

---

### 📋 專案管理 (Project Management)
進度追蹤、完成報告、待辦事項

| 文件名 | 用途 | 何時使用 |
|--------|------|----------|
| `COMPLETION_REPORT.md` | 專案完成報告 | 查看專案整體進度 |
| `IMPLEMENTATION_PROGRESS.md` | 實作進度追蹤 | 追蹤開發進度 |
| `IMPLEMENTATION_STEPS.md` | 實作步驟說明 | 了解實作流程 |
| `FINAL_FIX_GUIDE.md` | 最終修復指南 | 最後階段問題修復 |
| `IMMEDIATE_FIX_STEPS.md` | 緊急修復步驟 | 緊急問題處理 |

---

## 🎯 常見使用場景

### 場景 1: 第一次部署到 EC2
```bash
# 步驟 1: 閱讀部署指南
DEPLOY_GUIDE.md

# 步驟 2: 執行部署腳本
quick-deploy.bat

# 步驟 3: 測試 CORS
test-cors.bat
```

### 場景 2: 測試 API 功能
```bash
# 步驟 1: 導入 Postman Collection
KUJI_Admin_API_Tests.postman_collection.json

# 步驟 2: 參考測試指南
API_TEST_GUIDE.md

# 步驟 3: 執行測試
依照指南逐一測試 API
```

### 場景 3: CORS 問題排查
```bash
# 步驟 1: 查看 CORS 修復文件
CORS_FIX_AND_DEPLOY.md

# 步驟 2: 檢查配置
application-prod.yml (cors.allowed-origins)

# 步驟 3: 重新部署
quick-deploy.bat
```

### 場景 4: 了解新功能
```bash
# 步驟 1: 查看功能擴充總覽
FEATURE_EXPANSION_SUMMARY.md

# 步驟 2: 閱讀測試指南
FEATURE_TEST_GUIDE.md

# 步驟 3: 測試功能
依照指南測試各項功能
```

### 場景 5: 問題修復
```bash
# 步驟 1: 查看完整修復報告
COMPLETE_FIX_REPORT.md

# 步驟 2: 查找特定問題
搜尋相關的修復文件

# 步驟 3: 套用解決方案
依照文件步驟修復
```

---

## 🔍 快速查找

### 我想...

- **部署到 EC2** → `DEPLOY_GUIDE.md` + `quick-deploy.bat`
- **測試 API** → `API_TEST_GUIDE.md` + Postman Collections
- **修復 CORS** → `CORS_FIX_AND_DEPLOY.md`
- **了解架構** → `ARCHITECTURE_REFACTORING_SUMMARY.md`
- **查看新功能** → `FEATURE_EXPANSION_SUMMARY.md`
- **排查問題** → `COMPLETE_FIX_REPORT.md`
- **設定 S3** → `AWS_S3_SETUP_GUIDE.md`
- **複製商品** → `LOTTERY_COPY_COMPLETE_SUMMARY.md`

---

## 📌 重要提醒

### ⚠️ 不要隨意刪除的文件
- `DEPLOY_GUIDE.md` - 部署必備
- `API_TEST_GUIDE.md` - 測試必備
- `FEATURE_EXPANSION_SUMMARY.md` - 功能總覽
- `CORS_FIX_AND_DEPLOY.md` - CORS 修復

### 📦 可以存檔的文件
以下文件記錄歷史問題，可移至 `docs/archive/` 備查：
- `ADMINJWTFILTER_ERROR_ANALYSIS.md`
- `BACKEND_NOT_RUNNING_FIX.md`
- `CREATE_STORE_OWNER_ERROR_DIAGNOSIS.md`
- `ENUM_CLEANUP_GUIDE.md`

### 🗑️ 可以刪除的臨時文件
- `check_*.java` - 臨時檢查腳本
- `*.sql` (除了 migration scripts)
- `*.json` (除了 Postman collections)

---

## 📞 需要協助?

如果找不到需要的文件，請依照以下流程：

1. **查看本文件的「快速查找」區塊**
2. **搜尋關鍵字**（使用 VS Code 全域搜尋）
3. **查看 Git 提交記錄**（了解檔案變更原因）

---

**建議操作：**
```bash
# 整理文件到對應目錄
move DEPLOY_GUIDE.md docs/deployment/
move API_TEST_GUIDE.md docs/api-guides/
move FEATURE_EXPANSION_SUMMARY.md docs/features/
move ARCHITECTURE_REFACTORING_SUMMARY.md docs/architecture/

# 建立存檔目錄
mkdir docs/archive
move *_ERROR_*.md docs/archive/
move *_DIAGNOSIS.md docs/archive/
```
