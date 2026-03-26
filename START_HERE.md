# 📚 正式環境部署文檔索引

## 🎯 快速開始

**新手建議閱讀順序：**

1. 📄 **START_HERE.md**（本文件）- 了解整體架構
2. 🚀 **QUICK_START_PRODUCTION.md** - 快速部署（10 分鐘）
3. 🔧 **AWS_S3_SETUP_GUIDE.md** - S3 設定教學
4. 📖 **PRODUCTION_DEPLOYMENT_GUIDE.md** - 完整參考手冊

---

## 📦 文檔清單

### 🚀 快速開始系列

| 文檔 | 用途 | 適合對象 | 閱讀時間 |
|------|------|---------|---------|
| **QUICK_START_PRODUCTION.md** | 3 步驟快速部署 | 所有人 | 5 分鐘 |
| **PRODUCTION_CONFIG_SUMMARY.md** | 配置變更摘要 | 技術人員 | 10 分鐘 |

### 📖 完整指南系列

| 文檔 | 用途 | 適合對象 | 閱讀時間 |
|------|------|---------|---------|
| **PRODUCTION_DEPLOYMENT_GUIDE.md** | 完整部署手冊 | DevOps | 30 分鐘 |
| **AWS_S3_SETUP_GUIDE.md** | AWS S3 設定詳解 | 系統管理員 | 20 分鐘 |
| **PRODUCTION_MIGRATION_COMPLETE.md** | 變更完成報告 | 專案管理 | 15 分鐘 |

### 🛠️ 工具與腳本

| 檔案 | 用途 | 執行方式 |
|------|------|---------|
| **check-prod-config.bat** | 檢查配置是否正確 | `check-prod-config.bat` |
| **deploy.sh** | EC2 自動化部署 | `./deploy.sh` |

---

## 🗺️ 部署流程圖

```
┌─────────────────────────────────────────────────────────┐
│  Step 1: 本地準備                                        │
│  ├─ check-prod-config.bat (檢查配置)                    │
│  └─ mvn clean package (編譯 JAR)                        │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│  Step 2: AWS 設定（首次）                                │
│  ├─ S3 Bucket Policy (公開讀取)                         │
│  ├─ IAM Role (S3 存取權限)                              │
│  └─ EC2 Security Group (開放端口)                       │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│  Step 3: 部署到 EC2                                      │
│  ├─ scp JAR 和 deploy.sh 到 EC2                         │
│  ├─ SSH 到 EC2                                          │
│  └─ 執行 ./deploy.sh                                    │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│  Step 4: 驗證部署                                        │
│  ├─ 檢查服務狀態 (ps aux)                               │
│  ├─ 測試登入 API                                         │
│  └─ 測試 S3 上傳                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 📊 技術架構

### 變更前（開發環境）
```
┌─────────┐      ┌──────────────┐      ┌──────────┐
│ 前端    │─────▶│ 後端 API     │─────▶│ 本地檔案 │
│ Vue/React│     │ Spring Boot  │      │ 系統     │
└─────────┘      └──────────────┘      └──────────┘
                        │
                        ▼
                 ┌──────────┐
                 │ 測試 DB  │
                 └──────────┘
```

### 變更後（正式環境）
```
┌─────────┐      ┌───────┐      ┌──────────────┐      ┌──────────┐
│ 前端    │─────▶│ ELB   │─────▶│ EC2 (8080)   │─────▶│ AWS S3   │
│ Vue/React│     │ Nginx │      │ Spring Boot  │      │ test-     │
└─────────┘      └───────┘      └──────────────┘      │ ourkuji  │
                                        │              └──────────┘
                                        ▼
                                 ┌──────────┐
                                 │ RDS MySQL│
                                 │ database-1│
                                 └──────────┘
```

---

## 🔧 主要變更摘要

### 1. 配置檔案

| 檔案 | 變更 | 影響 |
|------|------|------|
| `application.yml` | `active: dev` → `active: prod` | 預設使用正式環境 |
| `application-prod.yml` | 新增完整正式環境配置 | 連接 RDS 和 S3 |
| `pom.xml` | 新增 AWS SDK 依賴 | 支援 S3 上傳 |

### 2. 程式碼

| 類別 | 變更 | 用途 |
|------|------|------|
| `S3Config.java` | ✨ 新增 | AWS S3 Client 配置 |
| `S3ServiceImpl.java` | ✨ 新增 | S3 上傳實作（prod） |
| `LocalFileServiceImpl.java` | ➕ 加上 `@Profile("dev")` | 僅在開發環境使用 |

### 3. 功能變更

| 功能 | 開發環境 | 正式環境 |
|------|---------|---------|
| 圖片儲存 | 本地 `static/img/` | AWS S3 |
| URL 格式 | `/img/news/xxx.jpg` | `https://test-ourkuji.s3...` |
| 資料庫 | 測試 DB | RDS MySQL |
| Profile 切換 | 手動指定 `-Dspring.profiles.active=dev` | 自動使用 prod |

---

## 🎯 使用情境指南

### 情境 1：我是新手，第一次部署

**建議閱讀：**
1. 本文件（了解整體）
2. `QUICK_START_PRODUCTION.md`（快速上手）
3. `AWS_S3_SETUP_GUIDE.md`（設定 S3）

**執行步驟：**
```bash
# 1. 檢查配置
check-prod-config.bat

# 2. 編譯
mvn clean package -DskipTests

# 3. 上傳並部署
# 參考 QUICK_START_PRODUCTION.md
```

---

### 情境 2：我想了解詳細的技術變更

**建議閱讀：**
1. `PRODUCTION_MIGRATION_COMPLETE.md`（完整變更報告）
2. `PRODUCTION_DEPLOYMENT_GUIDE.md`（技術細節）

**關鍵章節：**
- 技術架構變更
- 程式碼變更清單
- 安全性改善

---

### 情境 3：我遇到部署問題

**建議閱讀：**
1. `QUICK_START_PRODUCTION.md` → 🆘 遇到問題？
2. `PRODUCTION_DEPLOYMENT_GUIDE.md` → 故障排除章節
3. `AWS_S3_SETUP_GUIDE.md` → 常見問題

**常見問題快速索引：**
- 編譯失敗 → `QUICK_START_PRODUCTION.md` 問題 1
- 無法連接 EC2 → `QUICK_START_PRODUCTION.md` 問題 2
- 服務無法啟動 → `PRODUCTION_DEPLOYMENT_GUIDE.md` 問題 1
- S3 上傳失敗 → `AWS_S3_SETUP_GUIDE.md` Q1-Q4

---

### 情境 4：我想設定自動化部署

**建議閱讀：**
1. `PRODUCTION_DEPLOYMENT_GUIDE.md` → 設定開機自動啟動（Systemd）
2. `deploy.sh`（參考腳本）

**進階設定：**
- Systemd Service
- Nginx 反向代理
- CloudWatch 監控

---

## 📞 聯絡資訊

### 環境資訊

**EC2 Instance:**
- IP: `18.179.187.129`
- User: `ec2-user`
- Port: `8080`

**RDS MySQL:**
- Endpoint: `database-1.clsi2geo699r.ap-northeast-1.rds.amazonaws.com`
- Database: `kuji`
- Username: `admin`
- Password: `WUfan0667.`

**AWS S3:**
- Bucket: `test-ourkuji`
- Region: `ap-northeast-1`

**應用程式：**
- API Base URL: `http://18.179.187.129:8080/api`
- Admin 登入: `admin@kuji.com` / `admin123`

---

## ✅ 部署檢查清單

### 首次部署

- [ ] 閱讀 `QUICK_START_PRODUCTION.md`
- [ ] 執行 `check-prod-config.bat`（確認配置正確）
- [ ] 編譯 JAR（`mvn clean package -DskipTests`）
- [ ] 設定 S3 Bucket Policy（參考 `AWS_S3_SETUP_GUIDE.md`）
- [ ] 設定 EC2 IAM Role（參考 `AWS_S3_SETUP_GUIDE.md`）
- [ ] 上傳 JAR 到 EC2
- [ ] 執行 `deploy.sh`
- [ ] 驗證登入 API
- [ ] 驗證 S3 上傳
- [ ] 設定 Systemd（可選，參考 `PRODUCTION_DEPLOYMENT_GUIDE.md`）

### 更新部署

- [ ] 本地測試通過
- [ ] 編譯新版本 JAR
- [ ] 上傳到 EC2
- [ ] 執行 `deploy.sh`（自動停止舊版本）
- [ ] 驗證服務正常
- [ ] 檢查日誌無錯誤

---

## 🔍 快速搜尋

**想查詢...**
- 如何快速部署？ → `QUICK_START_PRODUCTION.md`
- S3 怎麼設定？ → `AWS_S3_SETUP_GUIDE.md` → 步驟 1-4
- 部署失敗怎麼辦？ → `QUICK_START_PRODUCTION.md` → 🆘 遇到問題？
- 配置改了什麼？ → `PRODUCTION_MIGRATION_COMPLETE.md`
- 如何設定自動啟動？ → `PRODUCTION_DEPLOYMENT_GUIDE.md` → Systemd
- IAM Role 怎麼設定？ → `AWS_S3_SETUP_GUIDE.md` → 選項 A
- 如何監控成本？ → `AWS_S3_SETUP_GUIDE.md` → 監控與成本管理

---

## 🎓 進階主題

**想深入了解...**
- 效能優化 → `PRODUCTION_DEPLOYMENT_GUIDE.md` → 效能優化建議
- 安全性強化 → `PRODUCTION_DEPLOYMENT_GUIDE.md` → 安全檢查清單
- 成本控制 → `AWS_S3_SETUP_GUIDE.md` → 成本管理
- CI/CD 自動化 → `PRODUCTION_DEPLOYMENT_GUIDE.md` → 進階主題

---

## 📈 版本歷史

| 版本 | 日期 | 變更 | 負責人 |
|------|------|------|--------|
| 1.0.0 | 2026-01-14 | 正式環境配置完成 | GitHub Copilot |

---

## 🎉 準備好了嗎？

**現在就開始部署吧！**

1. 打開 `QUICK_START_PRODUCTION.md`
2. 執行 `check-prod-config.bat`
3. 跟著 3 個步驟操作
4. 10 分鐘後見！

**祝部署順利！** 🚀

---

*最後更新：2026-01-14*  
*文檔維護：GitHub Copilot*
