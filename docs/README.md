# 📚 KUJI 專案文件目錄

> **所有文件已按功能分類整理，方便快速查找**

## 📂 目錄結構

```
docs/
├── 01-架構說明/          # 專案架構、設計模式、技術棧
├── 02-API文件/           # API 使用說明、測試指南
├── 03-資料庫相關/        # DDL、MBG 使用、資料初始化
├── 04-部署指南/          # EC2 部署、生產環境配置
├── 05-測試相關/          # 測試計畫、測試指南、Postman
└── 06-問題修復/          # Bug 修復記錄、問題診斷
```

---

## 🎯 當前專案狀態報告

### ✅ 已完成的功能

#### 1. **商品與獎品整合 API**（最新完成）
- 📍 位置：`02-API文件/LOTTERY_WITH_PRIZES_API_GUIDE.md`
- 功能：一支 API 同時管理商品和獎品
- 端點：
  - `POST /admin/lottery-with-prizes` - 建立商品+獎品
  - `PUT /admin/lottery-with-prizes/{id}` - 更新商品+獎品
  - `GET /admin/lottery-with-prizes/{id}` - 查詢商品含獎品

#### 2. **加權隨機抽獎系統**
- 📍 位置：`02-API文件/DRAW_FLOW.md`
- 功能：根據獎品權重進行隨機抽獎
- 端點：`POST /api/lottery/random/{id}/draw`

#### 3. **商品複製功能**
- 📍 位置：`02-API文件/LOTTERY_COPY_API_TEST_GUIDE.md`
- 功能：快速複製商品和獎品
- 端點：`POST /admin/lottery/{id}/copy`

#### 4. **獎品池、錢包、訂單系統**
- 📍 位置：`02-API文件/API_TEST_GUIDE_PRIZE_BOX_WALLET_ORDER.md`
- 功能：完整的抽獎後續流程

---

## ⚠️ 當前問題

### 缺少的資料庫表

**狀態：DDL 已準備好，等待執行**

需要在資料庫中建立以下 8 個表：

| 表名 | 用途 | DDL 位置 |
|------|------|----------|
| `system_log` | 系統操作日誌 | `03-資料庫相關/missing_tables_ddl.sql` |
| `user_address` | 用戶收件地址 | 同上 |
| `marquee` | 跑馬燈公告 | 同上 |
| `referral_code` | 推薦碼 | 同上 |
| `referral_record` | 推薦記錄 | 同上 |
| `email_log` | 郵件日誌 | 同上 |
| `district` | 行政區域 | 同上 |
| `report_snapshot` | 報表快照 | 同上 |

**📖 詳細說明：** `03-資料庫相關/MISSING_TABLES_GUIDE.md`

---

## 🚀 快速開始

### 1. 修復編譯問題（優先）

```bash
# 1. 執行 SQL 建立缺少的表
# 開啟 MySQL Workbench，執行 docs/03-資料庫相關/missing_tables_ddl.sql

# 2. 執行自動化腳本
create-missing-tables.bat

# 或手動執行
mvn mybatis-generator:generate
mvn clean compile -DskipTests
```

### 2. 測試整合 API

```bash
# 啟動專案
mvn spring-boot:run

# 參考測試文件
# docs/02-API文件/LOTTERY_WITH_PRIZES_API_GUIDE.md
```

### 3. 部署到生產環境

```bash
# 參考部署指南
# docs/04-部署指南/DEPLOY_GUIDE.md
```

---

## 📋 文件快速導航

### 我想...

#### 🔍 了解專案架構
- **整體架構**：`01-架構說明/ARCHITECTURE_ANALYSIS.md`
- **安全設計**：`01-架構說明/API_SECURITY_IMPROVEMENTS.md`
- **資料流程**：`01-架構說明/DRAW_FLOW.md`

#### 📝 使用 API
- **整合 API**（商品+獎品）：`02-API文件/LOTTERY_WITH_PRIZES_API_GUIDE.md`
- **完整 API 列表**：`02-API文件/API_TEST_GUIDE.md`
- **抽獎流程**：`02-API文件/DRAW_FLOW.md`

#### 🗄️ 操作資料庫
- **缺少的表**：`03-資料庫相關/MISSING_TABLES_GUIDE.md`
- **MBG 使用**：`03-資料庫相關/GENERATOR_USAGE_GUIDE.md`
- **資料初始化**：`03-資料庫相關/DATA_INITIALIZATION.md`

#### 🚀 部署專案
- **EC2 部署**：`04-部署指南/EC2_QUICK_DEPLOY_COMMANDS.md`
- **生產環境配置**：`04-部署指南/PRODUCTION_CONFIG_SUMMARY.md`
- **完整部署指南**：`04-部署指南/DEPLOY_GUIDE.md`

#### 🧪 測試功能
- **快速測試**：`05-測試相關/QUICK_TEST_GUIDE.md`
- **完整測試計畫**：`05-測試相關/COMPLETE_TEST_PLAN.md`
- **Postman 集合**：`05-測試相關/` 資料夾內

#### 🐛 修復問題
- **403 錯誤**：`06-問題修復/API_403_FIX_SUMMARY.md`
- **編譯錯誤**：`06-問題修復/COMPILE_ERROR_FIX.md`
- **外鍵錯誤**：`06-問題修復/FOREIGN_KEY_FIX_GUIDE.md`

---

## 📊 專案統計

### 已實作功能模組

| 模組 | 狀態 | Controller | Service | 文件 |
|------|------|------------|---------|------|
| 商品管理 | ✅ 完成 | AdminLotteryController | LotteryService | ✅ |
| 獎品管理 | ✅ 完成 | AdminLotteryPrizeController | LotteryPrizeService | ✅ |
| **整合 API** | ✅ **NEW** | AdminLotteryWithPrizesController | LotteryService | ✅ |
| 抽獎系統 | ✅ 完成 | RandomDrawController | DrawService | ✅ |
| 獎品池 | ✅ 完成 | PrizeBoxController | PrizeBoxService | ✅ |
| 錢包 | ✅ 完成 | UserWalletController | UserWalletService | ✅ |
| 訂單 | ✅ 完成 | OrderController | OrderService | ✅ |
| 店家管理 | ✅ 完成 | AdminStoreController | StoreService | ✅ |
| 用戶管理 | ✅ 完成 | AdminUserController | AdminUserService | ✅ |
| 權限管理 | ✅ 完成 | AdminPermissionController | PermissionService | ✅ |

### 資料庫表統計

- **已建立**：27 個核心表
- **缺少**：8 個輔助功能表（DDL 已準備）
- **總計**：35 個表

---

## 🎓 開發規範

### Copilot 指南
- 📍 位置：`.github/copilot-instructions.md`
- 內容：架構規範、命名規則、開發流程

### API 設計模式
- **查詢 API**：QueryReq + Condition 模式
- **統一回應**：AOP 自動包裝 ApiResponse
- **權限控制**：@PreAuthorize + SecurityUtils

### MyBatis 慣例
- **Entity/Mapper/Example** 三件套
- **動態查詢**：使用 Example 構建條件
- **避免手寫 SQL**：優先使用 MBG 生成的方法

---

## 📞 需要協助？

### 當前最優先事項

1. ⚠️ **執行 SQL 建立缺少的表**
   - 檔案：`docs/03-資料庫相關/missing_tables_ddl.sql`
   - 說明：`docs/03-資料庫相關/MISSING_TABLES_GUIDE.md`

2. 🔄 **執行 MBG 生成 Entity**
   ```bash
   mvn mybatis-generator:generate
   ```

3. ✅ **重新編譯專案**
   ```bash
   mvn clean compile -DskipTests
   ```

4. 🎉 **測試整合 API**
   - 參考：`docs/02-API文件/LOTTERY_WITH_PRIZES_API_GUIDE.md`

---

## 📝 更新記錄

### 2026-01-16
- ✅ 完成商品與獎品整合 API
- ✅ 建立 DDL 檔案（8 個缺少的表）
- ✅ 整理所有文件到分類資料夾
- ✅ 建立文件導航系統

---

**最後更新：** 2026-01-16  
**專案狀態：** 功能完整，等待建立資料庫表後即可測試
