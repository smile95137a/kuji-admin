# 實作計畫： 獎品盒 (Prize Box)

**分支**：`010-prize-box` | **日期**：2026-03-22 | **規格**：[spec.md](./spec.md)  
**輸入**：來自 `/specs/010-prize-box/spec.md` 的功能規格

## 摘要

玩家獎品盒 (Prize Box) 是一個類購物車容器，自動接收所有抽獎結果，支援以店家分組出貨並以 Bonus 點數回收。本功能已有初步骨架實作（`PrizeBoxController`, `PrizeBoxService`, `PrizeBox` entity），需補齊歷史查詢端點、可回收/可出貨旗標邏輯修正、UserAddress 整合、術語正名（賞品盒 → 獎品盒）及測試覆蓋。

## 技術背景

**語言/版本**：Java 21 + Spring Boot 3.3.3  
**主要相依套件**：MyBatis 3.0.5（Generator 模式）、Spring Security 6、JWT (jjwt)、Lombok、Validation (jakarta.validation)  
**儲存**：MySQL 8.3 — UUID 主鍵、snake_case 欄位名稱、`prize_box` 資料表已存在  
**測試**：JUnit 5 + Spring Boot Test + Mockito  
**目標平台**：AWS EC2 Linux（Spring Boot 內嵌 Tomcat）  
**專案類型**：REST API（web-service）— 前端消費 JSON API  
**效能目標**： 獎品盒清單回應 ≤ 2 秒 (SC-001); 回收完成紅利入帳 ≤ 5 秒 (SC-003)  
**限制**： 出貨/回收必須原子性（@Transactional）；回收不可撤銷；跨店獎品自動拆單  
**規模/範圍**： 單一 Spring Boot 後端服務；預計中小規模用戶（< 10k concurrent）

## 架構稽核

*關卡：必須在第 0 階段研究前通過。第 1 階段設計後重新確認。*

> Constitution 尚為模板佔位符（未正式填寫），無明確原則條文可供稽核。以下為基於本專案既有架構推導的隱性原則及本功能閘門評估：

| 隱性原則 | 評估 | 結論 |
|---------|------|------|
| Service/Mapper 分層架構 | ✅ PrizeBoxService → PrizeBoxMapper 分層清晰 | 通過 |
| UUID 主鍵 | ✅ `UUID.randomUUID().toString()` | 通過 |
| @Transactional 原子性 | ✅ shipPrizes/recyclePrizes 均已標注 | 通過 |
| JWT 身份驗證 | ✅ `SecurityUtils.getCurrentUserId()` | 通過 |
| 術語一致性 | ⚠️ 現行程式碼使用廢棄詞「賞品盒」 | **需要修正** |
| isRecyclable 邏輯 | ⚠️ 目前硬編碼 `true`，應為 `recycleBonus > 0` | **需要修正** |
| 歷史端點 | ⚠️ 缺少 `GET /prize-box/history` | **需要實作** |
| isShippable 旗標 | ⚠️ PrizeBoxItemRes 缺 `isShippable` 欄位 | **需要實作** |
| prizeValue 欄位 | ⚠️ PrizeBoxItemRes 缺 `prizeValue` (FR-002) | **需要實作** |
| UserAddress 整合 | ⚠️ PrizeBoxShipReq 缺 `userAddressId` | **需要實作** |

**設計後複查**： ✅ 設計文件已涵蓋所有閘門修正，見 data-model.md 與 contracts/。

## 專案結構

### 文件（本功能）

```text
specs/010-prize-box/
├── plan.md              ← 本文件
├── research.md          ← Phase 0 研究輸出
├── data-model.md        ← Phase 1 資料模型輸出
├── quickstart.md        ← Phase 1 快速入門
├── contracts/           ← Phase 1 API 契約
│   ├── GET_prize-box.md
│   ├── POST_prize-box_ship.md
│   ├── POST_prize-box_recycle.md
│   └── GET_prize-box_history.md
└── tasks.md             ← Phase 2 輸出（由 /speckit.tasks 產生）
```

### 原始碼（儲存庫根目錄）

```text
src/main/java/com/group/admin/
├── controller/api/
│   └── PrizeBoxController.java          ← 現有，需補 /history endpoint
├── service/
│   ├── PrizeBoxService.java             ← 現有，需補 getHistory 方法
│   └── impl/PrizeBoxServiceImpl.java    ← 現有，需修正多處邏輯
├── entity/
│   └── PrizeBox.java                    ← 現有，確認 isShippable 欄位
├── enums/
│   └── PrizeBoxStatusEnum.java          ← 現有，術語正名
├── req/prizebox/
│   ├── PrizeBoxShipReq.java             ← 現有，補 userAddressId + import fix
│   └── PrizeBoxRecycleReq.java          ← 現有
└── res/prizebox/
    ├── PrizeBoxItemRes.java             ← 現有，補 isShippable/prizeValue
    └── PrizeBoxSummaryRes.java          ← 現有

src/test/java/com/group/admin/
└── service/
    └── PrizeBoxServiceTest.java         ← 新建
```

**結構決策**： 既有單體 Spring Boot 服務，本功能在 `com.group.admin` 包內擴展，不新增模組或服務。

## 複雜度追蹤

> **僅在架構稽核有需要說明的違規項目時填寫**

| 項目 | 說明 | 處理方式 |
|------|------|---------|
| `isRecyclable` 硬編碼 `true` | 回收資格應由 `recycleBonus > 0` 決定，`PrizeBox.isRecyclable` 欄位可作為覆蓋旗標 | 修正 `convertToItemRes`：`recycleBonus != null && recycleBonus > 0` |
| `@NotBlank` import 缺失 | `PrizeBoxShipReq.java` 使用 `@NotBlank` 但未 import | 補上 `import jakarta.validation.constraints.NotBlank;` |
| 術語「賞品盒」→「獎品盒」 | 現行 JavaDoc/Enum name 使用廢棄詞 | 統一替換（不影響 DB column/API path）|
