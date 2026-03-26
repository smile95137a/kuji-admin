# Implementation Plan: RBAC 權限管理

**Branch**: `009-rbac-permissions` | **Date**: 2026-03-22 | **Spec**: [spec.md](./spec.md)  
**Input**: 來自 `/specs/009-rbac-permissions/spec.md` 的功能規格說明

## 摘要

為 KUJI 後台實作 Role-Based Access Control (RBAC) 系統。此系統支援三個固定角色（ROLE_ADMIN、ROLE_STORE_OWNER、ROLE_STORE_EDITOR），提供選單層級的細粒度權限（can_view、can_edit、can_delete）。資料模型已存在於資料庫中（role、menu、role_menu、admin_user_role 資料表），Entity 與 MyBatis Mapper 已部分生成。本功能將以下項目整合串聯：Spring Security `@PreAuthorize` 強制執行、依使用者授權動態回傳選單的 API、服務層的店家資料隔離，以及供管理員設定角色權限的管理 API。

## 技術背景

**Language/Version**: Java 21  
**Primary Dependencies**: Spring Boot 3.3.3, Spring Security 6, MyBatis 3.0.5, JJWT 0.9.1, MySQL Connector 8.x  
**Storage**: MySQL 8.3 (AWS RDS) — 資料表 `role`、`menu`、`role_menu`、`admin_user_role`、`store_user` 已存在  
**Testing**: JUnit 5 + Spring Boot Test + Mockito  
**Target Platform**: AWS EC2 Linux (Amazon Linux 2)  
**Project Type**: REST API (web-service)  
**效能目標**: 每次請求的權限檢查額外負擔 < 50 ms（SC-002）；選單 API < 100 ms  
**限制條件**:
- 角色固定（v1.0 不支援動態建立角色）
- 資料隔離在查詢層強制執行（非 DB 行級安全性）
- 店家範圍的使用者絕不能存取跨店家資料
- ROLE_ADMIN 跳過所有店家範圍限制

**規模/範圍**: 約 3 個角色、20-30 個選單項目，跨多店家可能有數百個 AdminUser 帳號

## 架構規範檢查

*關卡：必須在第 0 階段研究前通過。第 1 階段設計後須重新確認。*

> 專案架構規範為範本（尚未正式確立）。目前無專案特定關卡，適用標準工程關卡：

| 關卡 | 確認項目 | 狀態 |
|------|-------|--------|
| Entity 已存在 | DDL.sql 中確認有 `role`、`menu`、`role_menu`、`admin_user_role` 資料表 | ✅ PASS |
| Mapper 已存在 | RoleMapper、MenuMapper、RoleMenuMapper、AdminUserRoleMapper XML 已確認 | ✅ PASS |
| JWT 已攜帶角色 | 已確認 `JwtUtil.generateToken(…, roles, storeIds)` | ✅ PASS |
| Spring Security 已設定 | 已確認 `SecurityConfig.java`，Filter chain 已定義 | ✅ PASS |
| 不需新增資料表 | 所有 RBAC 資料表均已存在於 schema 中 | ✅ PASS |
| 店家隔離設計 | `StoreUser` entity + JWT 中的 `storeIds` 已到位 | ✅ PASS |

**設計後重新確認**：無違規。設計複用現有 schema 與基礎架構。

## 專案結構

### 文件（本功能）

```text
specs/009-rbac-permissions/
├── plan.md              ← This file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   ├── GET_admin_menus_my.md
│   ├── GET_admin_roles.md
│   ├── GET_admin_roles_{id}_permissions.md
│   └── PUT_admin_roles_{id}_permissions.md
└── tasks.md             ← Phase 2 output (/speckit.tasks - NOT created here)
```

### 原始碼（專案根目錄）

```text
src/main/java/com/group/admin/
├── annotation/
│   └── RequiresPermission.java         ← NEW: custom annotation for menu-level checks
├── aop/
│   └── PermissionCheckAspect.java      ← NEW: AOP aspect for @RequiresPermission
├── config/
│   └── SecurityConfig.java             ← MODIFY: enable @PreAuthorize globally
├── controller/admin/
│   ├── MenuController.java             ← MODIFY: add /admin/menus/my endpoint
│   └── RoleController.java             ← NEW: role management + permission endpoints
├── dto/
│   ├── MenuTreeDto.java                ← NEW: hierarchical menu response with permission flags
│   └── RolePermissionDto.java          ← NEW: role + permissions payload
├── res/
│   ├── MenuPermissionRes.java          ← NEW: menu item with canView/canEdit/canDelete
│   └── RoleWithPermissionsRes.java     ← NEW: role details with full menu permission map
├── req/
│   └── UpdateRolePermissionsReq.java   ← NEW: request body for PUT /roles/{id}/permissions
├── service/
│   ├── MenuService.java                ← MODIFY: add getAuthorizedMenusForUser(userId)
│   ├── MenuServiceImpl.java            ← MODIFY: implement authorized menu tree with RoleMenu join
│   ├── RoleService.java                ← NEW: interface
│   └── RoleServiceImpl.java            ← NEW: list roles, get/update permissions + audit log
├── security/
│   ├── UserPrincipal.java              ← VERIFY: storeIds and roles are stored
│   └── AdminJwtAuthenticationFilter.java ← VERIFY: loads roles and storeIds correctly
└── util/
    └── StorePermissionUtil.java        ← NEW: helper to assert current user owns storeId

src/main/resources/mapper/
├── RoleMenuMapper.xml                  ← EXTEND: add custom join queries
└── MenuMapper.xml                      ← EXTEND: add getMenusWithPermissionsForRoles query

src/test/java/com/group/admin/
├── controller/
│   ├── MenuControllerTest.java         ← NEW
│   └── RoleControllerTest.java         ← NEW
└── service/
    ├── MenuServiceTest.java            ← NEW
    └── RoleServiceTest.java            ← NEW
```

**架構決策**：單一 Spring Boot 專案（選項一）。所有新程式碼均加入現有 `com.group.admin` 套件層級下，不新增模組或專案。

## 複雜度追蹤

> 無違規。設計維持在現有單一專案結構內，複用所有現有 Entity 和 Mapper，並遵循既有模式（MyBatis、Filter 型 JWT、Service 層、Controller 層）。
