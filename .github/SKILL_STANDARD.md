# Kuji Admin SKILL 標準規範

本文檔定義 kuji-admin 專案中所有 SKILL.md 文件的標準格式和最佳實踐。  
所有 SKILL 必須遵循此規範，確保一致性和可維護性。

---

## 標準模板 / Standard Format

```markdown
---
name: your-skill-name
description: "一句話說明這個 Skill 的用途和何時使用。例如：理解三種抽獎模式的差異、掌握 JWT 雙鏈安全架構、學習 MyBatis Generator 工作流。"
---

# Skill 中文標題

## When to Use
- 使用情境 1
- 使用情境 2
- 使用情境 3

## 核心原則
- 原則 1
- 原則 2
- 原則 3

## 工作流程 / 核心概念

### 步驟 1 / 概念 1：標題

說明內容

```java
// 程式碼範例
```

### 步驟 2 / 概念 2：標題

說明內容

## 常見模式 / 核查清單

### 模式 A / 檢查項 1

說明內容

```java
// 程式碼範例
```

## ⚠️ 禁止操作

- ❌ 不要 xxx
- ❌ 不要 yyy
- ❌ 不要 zzz

## 參考資源 / 相關文件

- [參考連結 1](url)
- [相關指令文件](xx.instructions.md)
```

---

## YAML Frontmatter 規範

### 必填字段

| 字段 | 格式 | 說明 | 範例 |
|------|------|------|------|
| `name` | 小寫 + 連字符 | Skill 的唯一識別符 | `jwt-dual-chain` |
| `description` | 中文句子 | 一句話說明用途，包含理解/掌握/學習等關鍵字 | `"理解後台/前台路由分離、SecurityFilterChain 優先順序、JWT Token 結構、SecurityUtils 用法、403/401 除錯。"` |

### 命名規則

- **name**: 全小寫，空格用連字符 `-` 替換
  - ✅ `jwt-dual-chain` 
  - ✅ `order-lifecycle`
  - ❌ `JPW-dual-chain` (大寫)
  - ❌ `jwt_dual_chain` (底線)

- **description**: 中文，限制 150 字內
  - 第一句應該明確說明「理解」、「掌握」、「學習」或「指南」等關鍵字
  - 使用項目符號分隔多個核心主題

---

## 副標題層級規範

| 層級 | HTML | 用途 | 範例 |
|------|------|------|------|
| H1 `#` | `<h1>` | 文件總標題（Skill 中文名稱，不含 "Skill" 字） | `# JWT 雙鏈安全架構` |
| H2 `##` | `<h2>` | 主要分段 | `## When to Use` / `## 核心原則` |
| H3 `###` | `<h3>` | 子流程或細節 | `### 步驟 1：xxx` / `### 加權隨機演算法` |
| H4 `####` | `<h4>` | 深層細節（需要時） | `#### 參數說明` |

### 標準分段順序

```
# 主標題

## When to Use
- 情境 1
- 情境 2

## 核心原則
- 原則 1
- 原則 2

## 詳細流程 / 工作流程 / 核心概念
### 小節 1
### 小節 2

## 常見模式
### 模式 A
### 模式 B

## ⚠️ 禁止操作

## 參考資源
```

---

## 內容編寫指南

### 1. When to Use 段落
**目的**：快速告訴開發者何時應該使用此 Skill

**要求**：
- 至少 2 個情境
- 使用無序列表 `-`
- 每點 1 句話，明確且具體

✅ 好的範例：
```markdown
## When to Use
- 新增或修改抽獎邏輯（GACHA / OFFICIAL_ICHIBAN / SCRATCH_MODE）
- 調整點數扣除或庫存機制
- Debug 403 / 401 認證錯誤
- 設計新的過濾條件或排序規則
```

❌ 不好的範例：
```markdown
## When to Use
- 工作時使用
- 遇到問題
```

---

### 2. 核心原則 段落
**目的**：列出此 Skill 的不可違反的設計原則

**要求**：
- 應該包含 3～5 個原則
- 每個原則必須是需要遵守的規則
- 原則應該背後有具體的程式碼或架構支撐

✅ 好的範例：
```markdown
## 核心原則
- DDL-first：新增任何 Entity 都必須先寫 SQL，再用 MBG 生成
- Example 是查詢條件容器，不是參數物件
- 動態 SQL：所有查詢條件都應該是可選的，不得強制要求
- 千萬不要修改 Example 類的生成內容
```

---

### 3. 工作流程 / 核心概念
**目的**：詳細說明流程或機制

**結構**：
- 用 `### 小節` 組織邏輯步驟
- 每個小節包含：説明 + 程式碼範例 + 表格（若有對應關係）

✅ 好的範例：
```markdown
### 步驟 1：接收抽獎請求

驗證商品狀態、庫存、錢包餘額...

```java
// 程式碼示範
```

### 步驟 2：執行加權隨機

使用加權算法選出獎品...

| 獎品 | weight | 機率 |
|------|--------|------|
| SSR | 5 | 2% |
```

---

### 4. 禁止操作 段落
**目的**：明確列出常見錯誤和禁止事項

**格式**：使用 `❌ 不要 xxx` 或 `⚠️ 警告` 格式

```markdown
## ⚠️ 禁止操作

- ❌ 不要在 Filter 中使用 `request.getRequestURI()`（會包含 context-path）
- ❌ 不要移除 UserPrincipal.roles 中的 `ROLE_` 前綴
- ❌ 不要對 `isDesignatedPrize=1` 的籤位執行 `autoAssignNonGrandPrizes`
- ⚠️ 一定要在 `designatePrizePositions` 後呼叫 `autoAssignNonGrandPrizes`
```

---

### 5. 表格使用
使用表格列舉對應關係或配置

✅ 好的場景：
- 模式 vs 參數對照表
- 狀態轉移表
- 權重配置表

```markdown
| 模式 | category | playMode | 抽獎 API | Service |
|------|----------|----------|----------|---------|
| 一番賞 | `OFFICIAL_ICHIBAN` | `LOTTERY_MODE` | `POST /draw` | LotteryTicketService |
| 扭蛋 | `GACHA` | `LOTTERY_MODE` | `POST /random` | DrawService |
```

---

### 6. 程式碼區塊
**語言標記**：明確指定程式碼語言

✅ 正確：
````markdown
```java
// Java 代碼
List<String> names = prizes.stream()
    .map(Prize::getName)
    .collect(Collectors.toList());
```

```sql
-- SQL 查詢
SELECT * FROM prize WHERE remaining > 0;
```
````

❌ 不正確：
````markdown
```
// 沒有指定語言標記
```
````

---

## 檔案組織

### 目錄結構
```
.github/
├── skills/                          # Skills 主目錄
│   ├── skill-name-1/
│   │   ├── SKILL.md                 # 必須有
│   │   ├── scripts/                 # 可選：可執行腳本
│   │   │   └── helper.ps1
│   │   ├── references/              # 可選：補充文件
│   │   │   └── workflow.md
│   │   ├── examples/                # 可選：範例程式碼
│   │   │   └── ExampleController.java
│   │   └── templates/               # 可選：模版文件
│   └── skill-name-2/
│       └── SKILL.md
├── SKILL_TEMPLATE.md                # 標準模版（此文件）
└── SKILL_STANDARD.md                # 標準規範（此文件）
```

---

## 檢查清單 / 提交前驗證

新增或修改 SKILL.md 前，請確保：

- [ ] **YAML Frontmatter 完整**
  - [ ] `name` 欄位存在且遵循小寫連字符規則
  - [ ] `description` 欄位存在且不超過 150 字
  - [ ] 三行 `---` 正確放置

- [ ] **標題正確**
  - [ ] H1 標題不包含 "Skill" 字樣
  - [ ] 所有標題層級合理（H1 > H2 > H3）

- [ ] **內容完整**
  - [ ] 包含 `## When to Use` 段落（至少 2 項）
  - [ ] 包含 `## 核心原則` 段落（至少 3 項）
  - [ ] 包含詳細說明段落（工作流程 / 概念 / 流程）
  - [ ] 包含 `## ⚠️ 禁止操作` 段落

- [ ] **程式碼品質**
  - [ ] 所有程式碼區塊都標記了語言（\`\`\`java / \`\`\`sql 等）
  - [ ] 程式碼示例正確且可運行（至少在邏輯上）
  - [ ] 表格對齊正確

- [ ] **可讀性**
  - [ ] 使用 emoji（🔍🎯✅❌⚠️）增強視覺層次
  - [ ] 關鍵字用 \`backtick\` 標記（類名、方法名、變數名）
  - [ ] 沒有拼寫錯誤或格式不一致

---

## 維護和版本控制

### Git Commit 訊息格式

```bash
# 新增 Skill
git commit -m "feat(skill): add {skill-name} skill"

# 修改 Skill
git commit -m "docs(skill): update {skill-name} - add xxx section"

# 修正錯誤
git commit -m "fix(skill): correct {skill-name} description/code"
```

### 版本更新

Skill 之間避免相互依賴。如果 Skill A 參考 Skill B，使用相對連結：

```markdown
[相關 SKILL：JWT 雙鏈安全架構](./%2F%2Fjwt-dual-chain%2FSKILL.md)

或直接引用：
參考 `jwt-dual-chain` Skill 了解更多
```

---

## 常見問題 (FAQ)

### Q: Skill 和指令文件有什麼區別？

**A:**
- **Skill（SKILL.md）**：教導開發者「如何做」，聚焦於工作流程和最佳實踐
- **指令文件（.instructions.md）**：定義「什麼是對的」，聚焦於規則和禁止事項

### Q: 我應該在什麼時候建立新的 Skill？

**A:**
當以下情況出現時，考慮建立新的 Skill：
1. 有一個完整的工作流程（5+ 個步驟或概念）
2. 涉及多個檔案或模組
3. 有常見的錯誤模式需要避免
4. 開發期間需要頻繁參考

### Q: Skill.md 應該有多長？

**A:**
- **最小**：300 字（涵蓋 When to Use、核心原則、1 個工作流程、禁止操作）
- **最佳**：800～1500 字
- **最大**：2000 字（超過應考慮分割為多個 Skill）

### Q: 我可以在 Skill.md 中引用其他 Skill 嗎？

**A:**
可以，但要使用明確的相對連結或文字參考：

```markdown
詳見 [JWT 雙鏈安全架構](../jwt-dual-chain/SKILL.md) Skill 中的 Permission 部分。
```

---

## 快速開始

### 創建新的 Skill 步驟

1. **建立目錄**
   ```bash
   mkdir .github/skills/my-new-skill
   ```

2. **複製現有標準 Skill 作為參考**
   
   選擇一個**結構相似**的現有 SKILL.md 作為模板：
   
   | 如果你要寫... | 參考這個 Skill |
   |-------------|-------------|
   | 工作流程 / 架構指南 | [draw-flow/SKILL.md](skills/draw-flow/SKILL.md) |
   | 安全 / 配置指南 | [jwt-dual-chain/SKILL.md](skills/jwt-dual-chain/SKILL.md) |
   | 工具使用指南 | [mbg-workflow/SKILL.md](skills/mbg-workflow/SKILL.md) |
   | 測試指南 | [controller-testing/SKILL.md](skills/controller-testing/SKILL.md) |
   
   ```bash
   # 例如：參考 draw-flow 的結構
   cp .github/skills/draw-flow/SKILL.md .github/skills/my-new-skill/SKILL.md
   ```

3. **修改內容**
   - 更新 YAML frontmatter（`name` 和 `description`）
   - 替換 H1 標題
   - 修改 `## When to Use` 段落
   - 修改 `## 核心原則` 段落
   - 替換詳細流程內容
   - 更新 `## ⚠️ 禁止操作` 段落

4. **驗證**
   - 使用本文「檢查清單」驗證
   - 在 Markdown 預覽中檢查格式
   - 若 skill 帶腳本，確認腳本路徑與呼叫方式清楚
   - 請 code reviewer 審查

5. **提交**
   ```bash
   git add .github/skills/my-new-skill/SKILL.md
   git commit -m "feat(skill): add my-new-skill skill"
   ```
   
   💡 **提示**：不要建立外部的 TEMPLATE.md 文件，直接參照現有的標準 SKILL.md；若有自動化腳本，請收斂在 skill 自己的 `scripts/` 目錄

---

## 示範 / 參考文件

已有的標準 Skill：
- [draw-flow](.github/skills/draw-flow/SKILL.md) - 三種抽獎模式
- [jwt-dual-chain](.github/skills/jwt-dual-chain/SKILL.md) - 雙鏈安全架構
- [controller-testing](.github/skills/controller-testing/SKILL.md) - Controller 測試
- [mbg-workflow](.github/skills/mbg-workflow/SKILL.md) - MBG 工作流程

最後更新：2026-04-13  
維護者：Kuji Admin 開發團隊
