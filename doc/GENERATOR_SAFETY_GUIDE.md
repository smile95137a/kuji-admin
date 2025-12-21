# ⚠️ Generator 安全使用須知

## 🚨 重要警告

### Generator 僅供 LOCAL 開發環境使用！

**FullSchemaExampleGenerator** 的資料庫連線設定：
```java
private static final String URL = "jdbc:mysql://localhost:3306/kuji?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "123456";
private static final String SCHEMA = "kuji";
```

### ❌ 絕對禁止的操作

1. **不要在 Generator 中配置正式環境資料庫**
   - ❌ 不要使用正式 RDS 連線資訊
   - ❌ 不要使用正式環境密碼
   - ❌ 不要在正式環境執行 Generator

2. **不要直接在正式環境執行 SQL**
   - ❌ `doc/fix_role_table.sql` 僅供 LOCAL 使用
   - ❌ DDL 變更必須經過審核流程
   - ❌ 正式環境變更需要備份與測試

### ✅ 正確的使用方式

#### LOCAL 開發流程
1. 在 LOCAL MySQL 修改 Schema
2. 執行 Generator 產生程式碼
3. 測試功能
4. 提交 Pull Request

#### 正式環境部署流程
1. 經過審核的 DDL 腳本
2. 在測試環境驗證
3. 備份正式資料庫
4. 在維護時段執行變更
5. 驗證功能正常

## 🔒 環境隔離

### LOCAL 環境
- **資料庫**：localhost:3306/kuji
- **用途**：開發、測試、Generator 執行
- **資料**：測試資料，可隨時清空

### DEV 環境（application-dev.yml）
- **資料庫**：AWS RDS onekuji-lotery
- **用途**：整合測試
- **資料**：測試資料

### PROD 環境
- **資料庫**：正式 RDS
- **用途**：正式服務
- **資料**：真實客戶資料
- **⚠️ 禁止直接修改！**

## 📋 檢查清單

在執行 Generator 前，請確認：
- [ ] 已確認目前連線是 `localhost:3306/kuji`
- [ ] 已確認不是正式環境資料庫
- [ ] 已在 LOCAL 資料庫測試過 SQL
- [ ] 已備份重要資料

## 🆘 緊急處理

如果不小心在正式環境執行了變更：
1. **立即停止**所有操作
2. **通知團隊**主管與 DBA
3. **評估影響**範圍
4. **從備份還原**（如有需要）
5. **撰寫事故報告**

## 💡 最佳實務

1. **使用環境變數**區分不同環境
2. **Git ignore** 包含敏感資訊的檔案
3. **Code Review** 檢查資料庫連線設定
4. **自動化測試** 確保變更安全
5. **定期備份** LOCAL 與正式資料庫

---

**請記住：LOCAL 開發永遠使用 localhost，正式環境變更必須謹慎！** 🔐
