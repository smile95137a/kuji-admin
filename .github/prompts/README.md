# kuji-admin `.github/` 目錄總覽

## 📁 目錄結構

```
.github/
├── copilot-instructions.md          # 主要 Copilot 指南（自動載入）
├── instructions/                    # 領域知識（on-demand / applyTo 載入）
│   ├── architecture.instructions.md     # 架構與 MBG 規範
│   ├── backend.instructions.md          # 後端開發規範
│   ├── gacha-random-draw.instructions.md# 扭蛋隨機抽獎
│   ├── game.instructions.md             # 遊戲獎項規則
│   ├── permissions.instructions.md      # 權限系統 (RBAC)
│   ├── scratch-card-flow.instructions.md# 刮刮樂流程
│   ├── store.instructions.md            # 店家管理
│   ├── store-user.instructions.md       # 店家帳號管理
│   └── user.instructions.md             # 會員系統
├── prompts/                         # 功能需求 Prompt（可透過 / 呼叫）
│   ├── banner.prompt.md                 # Banner 管理
│   ├── express.prompt.md                # 運送管理
│   ├── game-management.prompt.md        # 抽獎遊戲管理
│   ├── game-to-order.prompt.md          # 抽獎→訂單流程
│   ├── lottery-ticket-system.prompt.md  # 籤位系統設計
│   ├── mastercard.prompt.md             # 金流/點數系統
│   ├── news.prompt.md                   # 最新消息
│   ├── order.prompt.md                  # 訂單管理
│   ├── prize-box.prompt.md              # 賞品盒流程
│   ├── product-lottery.prompt.md        # 一番賞整合平台
│   ├── referral.prompt.md               # 推薦碼機制
│   ├── store-account-management.prompt.md # 店家帳號管理
│   └── speckit.*.prompt.md              # SpecKit 工作流
├── agents/                          # 自訂 Agent
│   ├── controller-crud-test.agent.md    # Controller 測試 agent
│   └── speckit.*.agent.md               # SpecKit agent
└── skills/                          # 多步驟工作流 Skill
    └── controller-testing/              # Controller 測試
```

## 🎯 各類型用途

| 類型 | 載入方式 | 用途 |
|------|---------|------|
| **Instructions** | `applyTo` 匹配或 on-demand | 領域知識，編輯相關檔案時自動載入 |
| **Prompts** | 輸入 `/` 選擇 | 單一功能需求的開發任務模板 |
| **Agents** | 選擇 Agent 模式 | 自訂 AI Agent |
| **Skills** | 輸入 `/` 選擇 | 多步驟工作流（含參考文件） |

## 📋 開發流程
1. 先讀 `copilot-instructions.md` → 核心架構
2. 再讀 `instructions/` → 領域規則
3. 使用 `prompts/` → 開始開發
4. 遵循 DDL → MBG → Example 流程

// 多條件 AND
example.createCriteria()
    .andStatusEqualTo("ACTIVE")
    .andRoleEqualTo("ADMIN");

// 多條件 OR
example.createCriteria().andRoleEqualTo("ADMIN");
example.or().andRoleEqualTo("STORE_OWNER");
```

---

## 🔄 標準開發流程

### 新增功能完整流程
```
1. 撰寫 DDL (CREATE TABLE ...)
   ↓
2. 在 MySQL 執行 DDL
   ↓
3. 在 generatorConfig.xml 新增 <table> 配置
   ↓
4. 執行 FullSchemaExampleGenerator.java
   ↓
5. 檢查生成的 Entity、Mapper、Example
   ↓
6. 使用 Example 撰寫 Service 邏輯
   ↓
7. (必要時) 在 Mapper XML 新增自定義 SQL
```

---

## ⚠️ 常見錯誤

### ❌ 錯誤做法
- 直接手寫 Entity 類別
- 跳過 DDL 直接寫程式碼
- 大量手寫 SQL 而不用 Example
- 修改 MBG 生成的基礎方法                                                  

### ✅ 正確做法
- DDL → MBG → Example 流程
- 優先使用 Example 查詢
- 只在複雜查詢（JOIN、子查詢）時手寫 SQL
- 自定義 SQL 寫在 Mapper XML 底部

---

## 📌 快速檢查清單

開始開發前請確認:
- [ ] 已閱讀 `architecture-guide.prompt.md`
- [ ] 已撰寫 DDL 並在資料庫執行
- [ ] 已更新 `generatorConfig.xml`
- [ ] 已執行 MBG 生成器
- [ ] 已檢查生成的檔案無誤
- [ ] 了解何時使用 Example、何時手寫 SQL

---

**最後更新**: 2025-12-13  
**專案**: kuji-admin  
**版本**: Spring Boot 3.3.3 + MyBatis 3.0.5 + Java 21
