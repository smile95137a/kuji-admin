# Implementation Plan: 橫幅廣告管理 (Banner Management)

**Branch**: `001-banner-management` | **Date**: 2026-03-22 | **Spec**: [spec.md](./spec.md)  
**Input**: 功能規格來源 `/specs/001-banner-management/spec.md`

## 摘要

為 KUJI 平台首頁輪播實作橫幅廣告管理模組。管理員建立與特定店家連結的廣告，設定上架/下架狀態與排程，並控制顯示順序。前台公開端點僅提供有效且在排程時間內、且連結店家狀態正常的廣告。現有程式碼庫已搭建大部分骨架；本計畫補充排程、店家狀態篩選等缺漏項目，並強化 API 合約。

## 技術背景

**語言/版本**: Java 21  
**主要相依套件**: Spring Boot 3.3.3, MyBatis 3.0.5, Spring Security 6 + JWT, Lombok, AWS SDK v2 (S3)  
**儲存**: MySQL 8.3 (AWS RDS `ap-northeast-1`)；資料表 `banner`  
**測試**: JUnit 5 + Spring Boot Test + Mockito；`@SpringBootTest` 整合測試  
**目標平台**: AWS EC2 Linux (ap-northeast-1)；context-path `/api`  
**專案類型**: REST API（Web 服務）  
**效能目標**: 公開輪播端點 p95 < 200 ms；排程狀態轉換延遲 ≤ 1 分鐘  
**限制條件**: 廣告圖片透過 S3 儲存（bucket `test-ourkuji`，資料夾 `banner/`）；不允許外部 URL 連結（僅限店家連結，詳見 FR-004）；不包含計費邏輯  
**規模/範疇**: 單租戶平台；預計同時最多 50 則上架廣告；3 個管理員角色（ADMIN, STORE_OWNER, STORE_EDITOR）

## 規範審查

*關卡：必須在第 0 階段調查前通過。第 1 階段設計後重新審查。*

> ⚠️ 規範檔案中包含未填寫的範本佔位符——本專案尚未撰寫規範原則。視為**無有效限制**，無關卡阻擋進度。請由專案負責人填寫規範。

**設計後複查**：未偵測到規範違規。所有設計決策均遵循現有專案慣例（MyBatis、UUID 主鍵、分層架構、角色制 @PreAuthorize）。

## Project Structure

### 文件（本功能）

```text
specs/001-banner-management/
├── plan.md              ← this file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/           ← Phase 1 output
│   ├── POST_admin_banners.md
│   ├── PUT_admin_banners_{id}.md
│   ├── DELETE_admin_banners_{id}.md
│   ├── GET_admin_banners.md
│   └── GET_api_banners.md
└── tasks.md             ← Phase 2 output (/speckit.tasks — NOT created here)
```

### 原始碼（專案根目錄）

```text
src/main/java/com/group/admin/
├── entity/
│   └── Banner.java                          # id, storeId, title, imageUrl, linkUrl,
│                                            # orderNum, status, startTime, endTime,
│                                            # createdAt, updatedAt
├── controller/
│   ├── admin/
│   │   └── AdminBannerController.java       # /admin/banners  (CRUD + publish/order)
│   └── api/
│       └── BannerController.java            # /banners  (public carousel)
├── service/
│   ├── BannerService.java
│   └── impl/
│       └── BannerServiceImpl.java
├── mapper/
│   └── BannerMapper.java
├── req/
│   ├── BannerCreateReq.java
│   └── BannerUpdateReq.java
├── res/
│   └── BannerRes.java
├── condition/
│   └── BannerCondition.java
└── scheduler/
    └── ScheduledTasks.java                  # existing; add banner schedule tick

src/main/resources/mapper/
└── BannerMapper.xml

src/test/java/com/group/admin/
├── controller/admin/
│   └── AdminBannerControllerTest.java
└── controller/api/
    └── BannerControllerTest.java
```

**結構決策**：單一 Spring Boot 專案。管理後台 CRUD 位於 `controller/admin/`，公開輪播位於 `controller/api/`。所有層次（controller → service → mapper）均遵循現有專案慣例。

## 複雜度追蹤

> 無規範違規需說明。
