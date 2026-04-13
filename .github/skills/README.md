# Kuji Admin SKILL 使用指南

此目錄包含 kuji-admin 專案的所有 **SKILL 文件**，教導開發者如何完成特定任務。

---

## 什麼是 SKILL？

**SKILL** 是針對特定領域的最佳實踐指南，包含：
- ✅ 完整的工作流程
- ✅ 程式碼範例
- ✅ 常見錯誤和禁止操作
- ✅ 架構設計原則

開發者在編寫代碼或進行特定任務時，應該**參照相應的 SKILL**。

---

## 可用的 SKILL 列表

| Skill 名稱 | 檔案 | 適用情境 |
|-----------|------|--------|
| 🎰 **抽獎流程** | [draw-flow/SKILL.md](draw-flow/SKILL.md) | 實作 GACHA / OFFICIAL_ICHIBAN / SCRATCH_MODE 抽獎 |
| 🔐 **JWT 雙鏈安全架構** | [jwt-dual-chain/SKILL.md](jwt-dual-chain/SKILL.md) | 後台/前台路由分離、認證除錯、權限管理 |
| 🛠️ **MyBatis Generator** | [mbg-workflow/SKILL.md](mbg-workflow/SKILL.md) | Entity 新增、Mapper 生成、Example 查詢 |
| 📦 **賞品盒管理** | [prize-box-management/SKILL.md](prize-box-management/SKILL.md) | 賞品盒狀態轉移、出貨、回收換紅利 |
| 📋 **訂單生命週期** | [order-lifecycle/SKILL.md](order-lifecycle/SKILL.md) | 訂單狀態、支付、退款、多店家隔離 |
| 🔑 **RBAC 權限系統** | [rbac-menu-setup/SKILL.md](rbac-menu-setup/SKILL.md) | 角色定義、選單樹、權限檢查 |
| 🧪 **Controller 測試** | [controller-testing/SKILL.md](controller-testing/SKILL.md) | 用 MockMvc 寫 API 測試 |
| 🎁 **推薦碼系統** | [referral-code/SKILL.md](referral-code/SKILL.md) | 推薦碼生成、驗證、獎勵分配 |
| 👤 **用戶認證** | [user-auth-flow/SKILL.md](user-auth-flow/SKILL.md) | 會員註冊、OAuth 登入、信域驗證 |
| 👝 **錢包與儲值** | [wallet-recharge-flow/SKILL.md](wallet-recharge-flow/SKILL.md) | 點數系統、儲值流程、WalletTransaction |
| 📍 **用戶地址管理** | [user-address/SKILL.md](user-address/SKILL.md) | 地址 CRUD、驗證、默認地址 |
| 🏪 **店家開通流程** | [store-onboarding/SKILL.md](store-onboarding/SKILL.md) | 店家註冊、審核、StoreOwner 帳號開設 |
| 📱 **內容管理** | [content-management/SKILL.md](content-management/SKILL.md) | Banner / Marquee / News 上傳發布 |
| 📊 **報表統計** | [report-analytics/SKILL.md](report-analytics/SKILL.md) | 銷售報表、訂單分析、用戶統計 |
| ☁️ **AWS S3 上傳** | [s3-upload/SKILL.md](s3-upload/SKILL.md) | 圖片上傳、URL 生成、權限管理 |
| ✨ **新增 API 功能** | [add-feature-api/SKILL.md](add-feature-api/SKILL.md) | Controller / Service / Mapper 三層完整實作 |

---

## 如何使用 SKILL？

### 方式 1：在 Copilot Chat 中直接引用 ⭐ **推薦**

在 Copilot Chat 中使用 `@` 符號引用 SKILL：

```
@draw-flow

我要實作一個新的扭蛋抽獎 API，應該如何設計？
```

或引用具體的概念：

```
@jwt-dual-chain

為什麼我得到 403 Forbidden？怎麼除錯？
```

**優點**：
- ✅ 一句話快速引入 context
- ✅ Copilot 自動理解你的需求
- ✅ 避免手動複製貼上

---

### 方式 2：直接開啟 SKILL.md 參照

1. 在你的任務和 SKILL.md 之間切換
2. 按照 SKILL 中的「When to Use」確認適用
3. 遵循「工作流程」逐步實施
4. 檢查「禁止操作」避免常見錯誤

**適合**：詳細學習整個工作流程

---

### 方式 3：在 Copilot Chat 中貼上 SKILL 內容

複製整個 SKILL.md 內容到 Chat：

```
[貼上 SKILL 內容]

基於上面的 skill，我的實作正確嗎？

[貼上你的程式碼]
```

**適合**：代碼審查、架構驗證

---

### 方式 4：使用 VS Code 快捷搜尋

1. `Ctrl+P` 打開快速打開
2. 搜尋 `skill` → 列出所有 SKILL.md
3. 選擇相應的 SKILL 開啟

```
Ctrl+P
> skill draw

👇 直接打開 draw-flow/SKILL.md
```

---

## SKILL 結構速覽

每個 SKILL.md 都遵循標準格式：

```markdown
---
name: skill-name
description: "一句話說明"
---

# 標題

## When to Use
- 情境 1
- 情境 2

## 核心原則
- 原則 1
- 原則 2

## 工作流程
### 步驟 1
### 步驟 2

## ⚠️ 禁止操作
- ❌ 不要 xxx
```

快速檢查清單：
- ✅ **When to Use**：確認這個 SKILL 適用你的任務
- ✅ **核心原則**：理解設計背後的邏輯
- ✅ **工作流程**：逐步實施
- ✅ **禁止操作**：避免常見陷阱

---

## 實際例子

### 例子 1：實作扭蛋抽獎

```
開發任務：為商品 ID = 123 的扭蛋商品新增「抽獎」API

👇 在 Copilot Chat 中：

@draw-flow

我要實作一個新的扭蛋抽獎 API。

需求：
- 商品 ID: 123
- 一次抽 1～5 個
- 需要檢查點數餘額
- 返回獲得的獎品清單

應該怎麼做？

👇 Copilot 會根據 draw-flow SKILL 回答
```

---

### 例子 2：調試 403 錯誤

```
問題：我的管理員 API 返回 403 Forbidden

👇 在 Copilot Chat 中：

@jwt-dual-chain

我的管理員 API 返回 403 Forbidden

JWT token 內容：{...}
SecurityConfig 設定：{...}

根據上面的 skill，問題可能在哪？

👇 Copilot 會檢查你的配置中哪裡違反了雙鏈架構規則
```

---

### 例子 3：寫測試

```
任務：為 OrderController 寫完整的 CRUD 測試

👇 在 Copilot Chat 中：

@controller-testing

我要為 OrderController 寫 CRUD 測試。

Controller 方法：
- GET /api/orders/{id}
- POST /api/orders
- PUT /api/orders/{id}
- DELETE /api/orders/{id}

根據 skill 寫完整的測試

👇 Copilot 會產生標準化的 MockMvc 測試
```

---

## 常見問題

### Q: 我應該每次都讀完整個 SKILL.md 嗎？

**A:** 不用。開發流程：
1. 快速掃 **When to Use** → 確認適用
2. 掃 **核心原則** → 了解設計哲學
3. 跳到相關的 **工作流程小節**
4. 參考 **禁止操作** → 避免常見錯誤
5. （可選）在 Chat 中 `@skill-name` 快速提問

---

### Q: 如果 SKILL 沒有涵蓋我的情況怎麼辦？

**A:**
1. 檢查是否有相關的 SKILL（例如：order-lifecycle vs prize-box-management）
2. 結合多個 SKILL 參考（例如：先看 draw-flow，再看 wallet-recharge-flow）
3. 在 Copilot Chat 中詢問：「SKILL 中沒有涵蓋 XXX，應該怎麼做？」

---

### Q: SKILL 過時了怎麼辦？

**A:** SKILL 應該隨著專案更新而更新。如果：
- 發現 SKILL 內容與實際代碼不符 → 提 Issue 或提交 PR
- 有新的最佳實踐 → 在 SKILL 中補充或修正

---

## 最佳實踐

### ✅ 使用 SKILL 的正確方式

1. **任務開始時參照**
   - 新功能開發前 → 先看 SKILL
   - 遇到問題時 → 先查 SKILL

2. **在 Code Review 中引用**
   ```
   // reviewer 的 comment：
   
   這裡違反了 @draw-flow SKILL 中的「禁止操作」第 3 項
   
   應該這樣改：[建議]
   ```

3. **與團隊討論時參考**
   ```
   按照 @jwt-dual-chain SKILL 的規則...
   ```

---

### ❌ 常見的錯誤

- ❌ 忽視 SKILL，自己亂實作
- ❌ 只讀程式碼範例，不理解原則
- ❌ 發現 SKILL 不適用就放棄，而不是提出討論
- ❌ SKILL 更新後不通知團隊

---

## 如何新增新的 SKILL？

參考 [.github/SKILL_STANDARD.md](../SKILL_STANDARD.md) 了解標準規範。

**重點**：不要創建外部模板文件，直接參照現有的標準 SKILL.md 作為參考。

快速流程：
1. `mkdir .github/skills/my-new-skill`
2. 選擇一個**結構相似**的現有 SKILL.md（例如 `draw-flow/SKILL.md`） **複製作為參考**
3. 修改 YAML frontmatter（name、description） 和內容
4. 提交 PR，請團隊審查

參考這些標準 SKILL：
- **工作流程/架構**：[draw-flow](draw-flow/SKILL.md)、[order-lifecycle](order-lifecycle/SKILL.md)
- **安全/配置**：[jwt-dual-chain](jwt-dual-chain/SKILL.md)、[rbac-menu-setup](rbac-menu-setup/SKILL.md)
- **工具使用**：[mbg-workflow](mbg-workflow/SKILL.md)、[s3-upload](s3-upload/SKILL.md)
- **測試**：[controller-testing](controller-testing/SKILL.md)

---

## 相關文件

- 📖 [SKILL 標準規範](.../SKILL_STANDARD.md) - 如何寫 SKILL
- 📝 [指令文件](../instructions/) - 架構和規則定義
- 🔍 [Copilot 指南](../copilot-instructions.md) - 專案整體使用指南

---

## 聯絡與回饋

如果你對 SKILL 有想法、發現錯誤，或想新增 SKILL：

1. 在 GitHub Issues 中提出討論
2. 或直接提交 PR 修正

---

**更新日期**：2026-04-13  
**維護者**：Kuji Admin 開發團隊  
**Copilot 版本**：Claude Haiku 4.5+
