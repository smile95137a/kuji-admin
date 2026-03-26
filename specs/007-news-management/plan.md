# Implementation Plan: 最新消息管理 (News Management)

**Branch**: `007-news-management` | **Date**: 2026-03-22 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature specification from `/specs/007-news-management/spec.md`

## 摘要

為 KUJI 抽獎平台實作平台級最新消息／公告管理模組。
管理員可建立、編輯、發布、下架及刪除消息文章。前台使用者依發布日期排序瀏覽
已發布的文章。排程自動上架與自動下架由 Spring `@Scheduled` 任務處理，
分別檢查 `scheduled_at` / `end_time` 欄位。

**現況**：核心 CRUD 架構已存在（`News` 實體、`NewsMapper`、`NewsService`、
`AdminNewsController`、`NewsController`）。待實作的缺口：
1. `@Scheduled` 任務，用於狀態自動轉換（依時間自動上架／下架）
2. 狀態對齊：規格使用 `DRAFT / PUBLISHED / UNPUBLISHED`；現有程式碼使用 `ARCHIVED`
3. 公開 API 路徑：規格要求 `/api/news`；現有控制器對應到 `/news`
4. 後台列表端點：規格要求 `GET /admin/news`；現有程式碼使用 `POST /admin/news/list`
5. MySQL `news` 資料表 DDL 的 `published_at` 欄位可能需要與 `scheduled_at` 對齊

## 技術背景

**語言/版本**：Java 21  
**主要依賴**：Spring Boot 3.3.3、MyBatis 3.0.5、Spring Security 6、JWT、Lombok、Swagger (SpringDoc OpenAPI)  
**儲存**：MySQL 8.3 (AWS RDS) — 資料表 `news`，內文使用 BLOB  
**測試**：JUnit 5 + Spring Boot Test（`@SpringBootTest`、`@WebMvcTest`、`@DataJpaTest`）  
**目標平台**：AWS EC2 Linux (Amazon Linux 2023)，以 JAR 部署  
**專案類型**：REST API（Web 服務）— 單一 Spring Boot 應用程式  
**效能目標**：列表查詢 p95 < 200 ms；排程執行間隔 ≤ 60 秒（SC-002）  
**限制**：管理員專屬寫入權限（`@PreAuthorize("hasRole('ADMIN')")`）；草稿文章對公開端點不可見（SC-004）  
**規模/範圍**：平台級功能；寫入量低（僅管理員）；讀取量中等（玩家瀏覽）

## 架構規範檢查

*關卡：必須在第 0 階段研究前通過。第 1 階段設計後重新檢查。*

> 架構規範目前為預設範本（本專案尚未填寫）。  
> 在缺少規範的情況下，套用標準 Spring Boot REST API 品質關卡。

| 關卡 | 狀態 | 備註 |
|------|--------|-------|
| 實體使用 UUID 主鍵 | ✅ 通過 | `News.id` = `UUID.randomUUID().toString()` |
| 管理員端點有角色存取控制 | ✅ 通過 | `AdminNewsController` 套用 `@PreAuthorize("hasRole('ADMIN')")` |
| 草稿文章對公開 API 隱藏 | ✅ 通過 | `getPublishedNews()` 篩選 `status = PUBLISHED` + 時間檢查 |
| 專案中已有排程任務 | ✅ 通過 | `ScheduledTasks.java` 已使用 `@Scheduled`；消息任務待新增 |
| 寫入操作有事務控制 | ✅ 通過 | Service 方法已標註 `@Transactional` |
| 建立時有輸入驗證 | ✅ 通過 | `NewsCreateReq` 的 title/content 有 `@NotBlank` |
| 文章內容無外部 URL 連結 | ⚠️ 待審查 | FR-007：API 層尚未強制執行；列入任務備注 |
| 狀態命名一致性 | ⚠️ 缺口 | 程式碼使用 `ARCHIVED`；規格說 `UNPUBLISHED` — 實作時對齊 |
| 公開路徑前綴 `/api/` | ⚠️ 缺口 | `NewsController` 對應到 `/news`；應改為 `/api/news` |

**設計後重新檢查**：所有缺口已在合約與資料模型中解決。無架構規範違反。

## 專案結構

### 文件（此功能）

```text
specs/007-news-management/
├── plan.md              ← This file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   ├── admin-news-api.md
│   └── public-news-api.md
└── tasks.md             ← Phase 2 output (/speckit.tasks — NOT created by /speckit.plan)
```

### 原始碼（儲存庫根目錄）

```text
src/main/java/com/group/admin/
├── controller/
│   ├── admin/
│   │   └── AdminNewsController.java       ← 修補：新增 GET /admin/news 列表端點
│   └── api/
│       └── NewsController.java            ← 修補：重新對應到 /api/news
├── entity/
│   └── News.java                          ← 已存在（無需修改）
├── example/
│   └── NewsExample.java                   ← 已存在（MBG 生成）
├── mapper/
│   └── NewsMapper.java                    ← 已存在（無需修改）
├── req/news/
│   ├── NewsCreateReq.java                 ← 已存在（小改：審查 content @NotBlank）
│   ├── NewsUpdateReq.java                 ← 已存在
│   └── NewsCondition.java                 ← 已存在
├── res/news/
│   └── NewsRes.java                       ← 已存在（小改：新增 publishedAt 別名）
├── scheduler/
│   └── ScheduledTasks.java                ← 修補：新增消息自動上架／下架任務
└── service/
    ├── NewsService.java                   ← 已存在（新增 autoPublish/autoUnpublish 簽名）
    └── impl/
        └── NewsServiceImpl.java           ← 修補：實作排程方法 + 修正狀態

src/main/resources/mapper/
└── NewsMapper.xml                         ← 已存在（確認 published_at / scheduled_at 欄位）

src/test/java/com/group/admin/
├── controller/
│   └── AdminNewsControllerTest.java       ← 新增
└── service/
    └── NewsServiceTest.java               ← 新增
```

**結構決策**：單一 Spring Boot 專案。所有消息相關程式碼存放於現有的 `com.group.admin` 套件階層中，無需新增模組。

## 複雜度追蹤

> 無需說明的架構規範違反。
