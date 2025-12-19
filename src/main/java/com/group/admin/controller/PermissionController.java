package com.group.admin.controller;

import com.group.admin.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 權限檢查 Controller
 *
 * <p>提供權限檢查相關的 API</p>
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Tag(name = "權限檢查", description = "權限驗證相關操作")
@RestController
@RequestMapping("/admin/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 檢查用戶對選單的權限
     *
     * @param adminUserId 管理者用戶ID
     * @param menuCode    選單代碼
     * @return 權限資訊
     */
    @Operation(summary = "檢查選單權限", description = "檢查用戶對指定選單的所有權限（查看/編輯/刪除）")
    @GetMapping("/check/{adminUserId}/{menuCode}")
    public ResponseEntity<Map<String, Boolean>> checkMenuPermission(
            @Parameter(description = "管理者用戶ID") @PathVariable String adminUserId,
            @Parameter(description = "選單代碼") @PathVariable String menuCode) {
        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put("canView", permissionService.canView(adminUserId, menuCode));
        permissions.put("canEdit", permissionService.canEdit(adminUserId, menuCode));
        permissions.put("canDelete", permissionService.canDelete(adminUserId, menuCode));
        return ResponseEntity.ok(permissions);
    }

    /**
     * 檢查用戶是否有查看權限
     *
     * @param adminUserId 管理者用戶ID
     * @param menuCode    選單代碼
     * @return 是否有權限
     */
    @Operation(summary = "檢查查看權限", description = "檢查用戶對指定選單是否有查看權限")
    @GetMapping("/can-view/{adminUserId}/{menuCode}")
    public ResponseEntity<Boolean> canView(
            @Parameter(description = "管理者用戶ID") @PathVariable String adminUserId,
            @Parameter(description = "選單代碼") @PathVariable String menuCode) {
        return ResponseEntity.ok(permissionService.canView(adminUserId, menuCode));
    }

    /**
     * 檢查用戶是否有編輯權限
     *
     * @param adminUserId 管理者用戶ID
     * @param menuCode    選單代碼
     * @return 是否有權限
     */
    @Operation(summary = "檢查編輯權限", description = "檢查用戶對指定選單是否有編輯權限")
    @GetMapping("/can-edit/{adminUserId}/{menuCode}")
    public ResponseEntity<Boolean> canEdit(
            @Parameter(description = "管理者用戶ID") @PathVariable String adminUserId,
            @Parameter(description = "選單代碼") @PathVariable String menuCode) {
        return ResponseEntity.ok(permissionService.canEdit(adminUserId, menuCode));
    }

    /**
     * 檢查用戶是否有刪除權限
     *
     * @param adminUserId 管理者用戶ID
     * @param menuCode    選單代碼
     * @return 是否有權限
     */
    @Operation(summary = "檢查刪除權限", description = "檢查用戶對指定選單是否有刪除權限")
    @GetMapping("/can-delete/{adminUserId}/{menuCode}")
    public ResponseEntity<Boolean> canDelete(
            @Parameter(description = "管理者用戶ID") @PathVariable String adminUserId,
            @Parameter(description = "選單代碼") @PathVariable String menuCode) {
        return ResponseEntity.ok(permissionService.canDelete(adminUserId, menuCode));
    }

    /**
     * 查詢用戶的角色代碼列表
     *
     * @param adminUserId 管理者用戶ID
     * @return 角色代碼列表
     */
    @Operation(summary = "查詢用戶角色", description = "取得用戶的所有角色代碼")
    @GetMapping("/roles/{adminUserId}")
    public ResponseEntity<List<String>> getUserRoles(
            @Parameter(description = "管理者用戶ID") @PathVariable String adminUserId) {
        return ResponseEntity.ok(permissionService.getUserRoleCodes(adminUserId));
    }

    /**
     * 檢查用戶是否為 Admin
     *
     * @param adminUserId 管理者用戶ID
     * @return 是否為 Admin
     */
    @Operation(summary = "檢查是否為Admin", description = "檢查用戶是否擁有 Admin 角色")
    @GetMapping("/is-admin/{adminUserId}")
    public ResponseEntity<Boolean> isAdmin(
            @Parameter(description = "管理者用戶ID") @PathVariable String adminUserId) {
        return ResponseEntity.ok(permissionService.isAdmin(adminUserId));
    }

    /**
     * 查詢用戶可訪問的店鋪ID列表
     *
     * @param adminUserId 管理者用戶ID
     * @return 店鋪ID列表，如果為 Admin 則返回空列表表示全部
     */
    @Operation(summary = "查詢可訪問店鋪", description = "取得用戶可訪問的店鋪ID列表（Admin 返回空列表表示全部）")
    @GetMapping("/accessible-stores/{adminUserId}")
    public ResponseEntity<Map<String, Object>> getAccessibleStores(
            @Parameter(description = "管理者用戶ID") @PathVariable String adminUserId) {
        List<String> storeIds = permissionService.getAccessibleStoreIds(adminUserId);
        Map<String, Object> result = new HashMap<>();
        result.put("isAdmin", permissionService.isAdmin(adminUserId));
        result.put("storeIds", storeIds);
        result.put("hasFullAccess", storeIds == null);
        return ResponseEntity.ok(result);
    }
}
