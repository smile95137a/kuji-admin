# Research: RBAC 權限管理

**Feature**: 009-rbac-permissions  
**Phase**: 0 — 研究與未知事項解決  
**Date**: 2026-03-22

---

## 1. Spring Security 方法層級授權

### 決策
使用 `@PreAuthorize` 標注於 Controller 方法，並在 `SecurityConfig` 加入 `@EnableMethodSecurity(prePostEnabled = true)` 啟用。對於較粗粒度的 HTTP 層級檢查，保留現有 Filter chain 規則作為第一道關卡；`@PreAuthorize` 作為精確的強制執行層。

### 理由
Spring Security 6（隨 Spring Boot 3.x 發布）已棄用 `@EnableGlobalMethodSecurity`，改用 `@EnableMethodSecurity`。新的標注預設 `prePostEnabled = true`，因此標注本身即已足夠。

`@PreAuthorize` 表達式可透過 SpEL 存取 `SecurityContextHolder`，因此 `hasRole('ADMIN')` 等角色檢查無需任何樣板程式碼。更重要的是，它們可以呼叫自訂 Bean：`@PreAuthorize("@permissionService.hasMenuPermission(#menuCode, 'edit')")`。

### 評估過的替代方案
| 替代方案 | 拒絕理由 |
|---|---|
| 僅使用 Filter chain URL 模式 | 無法僅透過 URL 表達選單層級權限（can_view vs can_edit vs can_delete） |
| AOP `@Around` 搭配自訂標注 | 更靈活，但重複了 `@PreAuthorize` 透過 SpEL Bean 方法呼叫已提供的功能；增加複雜度卻無實質效益 |
| 在服務層以程式方式呼叫 `SecurityContextHolder` 檢查 | 冗長、分散，難以稽核 |

### 實作模式（已確認適用本專案）
```java
// SecurityConfig.java — add at class level
@EnableMethodSecurity(prePostEnabled = true)

// In RoleController — admin-only
@PreAuthorize("hasRole('ADMIN')")
@PutMapping("/{id}/permissions")
public Result<?> updatePermissions(...) { ... }

// In any data-mutating controller (store-scoped)
@PreAuthorize("@storePermissionUtil.canEdit(authentication, #storeId)")
@PutMapping("/stores/{storeId}/products/{id}")
public Result<?> updateProduct(...) { ... }
```

---

## 2. 動態選單渲染 API

### 決策
新增端點 `GET /admin/menus/my`，回傳當前已認證後台使用者中，在任意一個角色中具有至少 `can_view = true` 的選單，以樹狀結構（父 → 子）呈現，並附帶每個節點的權限旗標。

### 理由
`MenuController` 現有的 `GET /admin/menus/accessible` 需要評估——它可能根據使用者角色篩選選單，但回應中缺少細粒度的 can_view/can_edit/can_delete 旗標。新的 `/my` 端點**必須**包含權限旗標，讓前端無需額外呼叫即可條件式渲染編輯/刪除按鈕。

樹狀結構由伺服器端建立：從 DB 取得平面清單 → 依 `order_num` 排序 → 依 `parent_id` 巢狀化。對預期約 30 個選單的規模而言，此操作為 O(n) 且正確。

### 回應格式
```json
[
  {
    "id": "1",
    "name": "商品管理",
    "code": "PRODUCTS",
    "path": "/products",
    "icon": "ShoppingCart",
    "orderNum": 10,
    "canView": true,
    "canEdit": true,
    "canDelete": false,
    "children": [
      {
        "id": "11",
        "name": "商品列表",
        "code": "PRODUCTS_LIST",
        "path": "/products/list",
        "canView": true,
        "canEdit": true,
        "canDelete": false,
        "children": []
      }
    ]
  }
]
```

### 評估過的替代方案
| 替代方案 | 拒絕理由 |
|---|---|
| 回傳平面清單，讓前端建立樹狀結構 | 前端必須重新實作樹狀邏輯；與現有 `/admin/menus/tree` 模式不一致 |
| 將權限嵌入 JWT claims | 約 30 個選單 × 3 個權限位元，JWT 會變得過大；權限異動時也難以撤銷 |
| 分開呼叫取得選單與權限 | 增加額外往返；對前端開發體驗不佳 |

---

## 3. 店家層級資料隔離策略

### 決策
在**服務層**透過 `StorePermissionUtil` 輔助類別強制執行店家範圍隔離，該輔助類別從已認證的 `UserPrincipal` 讀取 storeIds。針對店家範圍資源的 Service 方法，接收 `storeId` 作為參數；工具類別斷言當前使用者擁有該店家。ROLE_ADMIN 跳過此檢查。

針對店家範圍資源的 MyBatis 查詢，必須在 WHERE 子句中包含 `AND store_id = #{storeId}`，或由服務層加入 Example 條件。

### 理由
現有的 `AdminJwtAuthenticationFilter` 已查詢 `StoreUser` 記錄並將 `storeIds` 存入 `UserPrincipal`，模式已建立。缺少的是一個集中式輔助工具，用於執行斷言（若請求的 storeId 不在使用者的 storeIds 中則拋出 `ForbiddenException`）並進行查詢層過濾。

DB 層的 Row-Level Security (RLS) 已納入考慮但被拒絕：MySQL 8.3 沒有原生 RLS。應用層的強制執行才是正確做法。

### 模式
```java
@Service
public class StorePermissionUtil {
    public void assertStoreAccess(Authentication auth, String storeId) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        if (principal.hasRole("ROLE_ADMIN")) return;          // admin bypass
        if (!principal.getStoreIds().contains(storeId)) {
            throw new ForbiddenException("Access denied to store: " + storeId);
        }
    }
}
```

---

## 4. 權限快取（已解決：v1.0 不使用快取）

### 決策
v1.0 不使用快取。考量預期負載（約數百名後台使用者）與效能目標（額外負擔 < 50 ms，SC-002），每次請求都執行一次簡單的 DB 查詢是可接受的。`role_menu` 資料表非常小（約 3 個角色 × 30 個選單 = 最多約 90 列）。

若負載增加，可在後續工單中加入 Spring Cache（`@Cacheable`），以短效記憶體或 Redis 快取為後端。

### 理由
快取增加了複雜度（權限異動時的快取失效、多節點 EC2 的分散式快取協調）。在此規模下，成本高於效益。

---

## 5. 多角色權限聚合

### 決策
對於擁有多個角色的使用者，有效權限為跨所有角色的**聯集**（邏輯 OR）。若角色 A 對某選單給予 `can_view=true, can_edit=false`，角色 B 給予 `can_view=true, can_edit=true`，則使用者的有效權限為 `can_edit=true`。

此規則在 spec.md 邊界情況中已明確說明。

### 實作
```sql
-- Pseudo-query: get effective permissions for user X on all menus
SELECT m.id, m.name, m.code, m.path, m.parent_id, m.icon, m.order_num,
       MAX(rm.can_view)   AS can_view,
       MAX(rm.can_edit)   AS can_edit,
       MAX(rm.can_delete) AS can_delete
FROM menu m
JOIN role_menu rm ON rm.menu_id = m.id
JOIN admin_user_role aur ON aur.role_id = rm.role_id
WHERE aur.admin_user_id = #{userId}
  AND m.is_visible = 1
GROUP BY m.id, m.name, m.code, m.path, m.parent_id, m.icon, m.order_num
ORDER BY m.order_num;
```

---

## 6. 權限變更稽核日誌（FR-011）

### 決策
複用現有的 `AdminOperationLog` / `SystemLog` 模式。當 ROLE_ADMIN 透過 `PUT /admin/roles/{id}/permissions` 更新角色權限時，Service 記錄：
- `operatorId`（從 JWT 取得）
- `operationType` = "UPDATE_ROLE_PERMISSIONS"
- `targetId` = roleId
- `before` = 舊權限的 JSON 快照
- `after` = 新權限的 JSON 快照
- `createdAt` = 當前時間

### 理由
`AdminOperationLogMapper.xml` 已存在。使用現有日誌資料表避免引入新的稽核專用資料表（YAGNI）。JSON before/after 方式滿足 SC-004，無需專用的變更差異 schema。

---

## 7. StoreEditor 權限必須是 StoreOwner 的子集（FR-008）

### 決策
僅在**服務層**強制執行（非 DB 約束）。更新 `ROLE_STORE_EDITOR` 權限時，Service 先取得 `ROLE_STORE_OWNER` 的權限並驗證：對每個選單，`editor.canEdit ≤ owner.canEdit` 且 `editor.canDelete ≤ owner.canDelete`。違規的項目以 HTTP 422 拒絕。

ROLE_ADMIN 的權限不受此限制。

### 理由
DB CHECK 約束無法表達跨列邏輯。觸發器脆弱。服務層是此業務規則的正確位置，與現有在 Service 類別中進行業務規則驗證的模式一致。

---

## 已解決事項摘要

| 未知事項 | 解決方案 |
|---|---|
| Spring Security 方法安全版本 | `@EnableMethodSecurity`（Spring Boot 3.x / Spring Security 6） |
| 動態選單端點格式 | `GET /admin/menus/my` → 附帶權限旗標的樹狀結構 |
| 店家隔離強制執行點 | 透過 `StorePermissionUtil` 在服務層執行 |
| 多角色聚合策略 | 聯集（透過單一 GROUP BY 查詢取 MAX 布林位元） |
| 權限快取 | v1.0 不使用快取；規模增長後再重新評估 |
| 稽核日誌方式 | 複用現有 `admin_operation_log` 資料表 |
| FR-008 子集強制執行 | 寫入時在服務層驗證 |
