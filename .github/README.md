# .github 結構說明

此目錄用來集中管理專案的 Copilot 規則、技能、提示詞與代理設定，並依用途分層，避免所有內容都堆在同一份文件。

```text
.github/
├── README.md
├── copilot-instructions.md
├── SKILL_STANDARD.md
├── agents/
├── instructions/
├── prompts/
└── skills/
```

## 目錄用途

| Path | Purpose |
|---|---|
| `copilot-instructions.md` | 全域短版規則，優先放最重要的工作原則 |
| `instructions/` | 細粒度規則，依檔案類型或工作領域套用 |
| `agents/` | 可重複使用的自訂 Agent 定義 |
| `prompts/` | 可重複使用的 Prompt |
| `skills/` | 領域流程、補充說明、模板與腳本 |

## Skill 結構

每個 skill 可以把自己的腳本、參考資料、模板拆開放，不必全部塞進單一 `SKILL.md`。

```text
skill-name/
├── SKILL.md
├── scripts/
├── references/
├── templates/
└── assets/
```

## 規則整理原則

1. **主規則短版**：放在 `copilot-instructions.md`
2. **領域細則分檔**：放在 `instructions/`
3. **流程型知識**：放在 `skills/`
4. **全部用中文**：必要技術名詞保留英文

## 目前方針

只有真的有內容的目錄才建立；像 `hooks/`、`workflows/` 這類目前尚未使用的結構，先不硬建立空目錄。
