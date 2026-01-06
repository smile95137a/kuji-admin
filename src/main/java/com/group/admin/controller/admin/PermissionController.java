package com.group.admin.controller.admin;

import com.group.admin.service.PermissionService;
import com.group.admin.util.SecurityUtils;
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
 * <p>所有 API 自動從 JWT Token 取得 userId，不需前端傳遞</p>
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
     * 檢查當前用戶對選單的權限
     *
     * @param menuCode 選單代碼
     * @return 權限資訊
     */
    @Operation(summary = "檢查選單權限", description = "檢查當前登入用戶對指定選單的所有權限（查看/編輯/刪除）")
    @GetMapping("/check/{menuCode}")
    public ResponseEntity<Map<String, Boolean>> checkMenuPermission(
            @Parameter(description = "選單代碼") @PathVariable String menuCode) {
        String adminUserId = SecurityUtils.getCurrentAdminUserId();
        if (adminUserId == null) {
            return ResponseEntity.status(401).build();
        }
        
        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put("canView", permissionService.canView(adminUserId, menuCode));
        permissions.put("canEdit", permissionService.canEdit(adminUserId, menuCode));
        permissions.put("canDelete", permissionService.canDelete(adminUserId, menuCode));
        return ResponseEntity.ok(permissions);
    }

    /**
     * 檢查當前用戶是否有查看權限
     *
     * @param menuCode 選單代碼
     * @return 是否有權限
     */
    @Operation(summary = "檢查查看權限", description = "檢查當前登入用戶對指定選單是否有查看權限")
    @GetMapping("/can-view/{menuCode}")
    public ResponseEntity<Boolean> canView(
            @Parameter(description = "選單代碼") @PathVariable String menuCode) {
        String adminUserId = SecurityUtils.getCurrentAdminUserId();
        if (adminUserId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(permissionService.canView(adminUserId, menuCode));
    }

    /**
     * 檢查當前用戶是否有編輯權限
     *
     * @param menuCode 選單代碼
     * @return 是否有權限
     */
    @Operation(summary = "檢查編輯權限", description = "檢查當前登入用戶對指定選單是否有編輯權限")
    @GetMapping("/can-edit/{menuCode}")
    public ResponseEntity<Boolean> canEdit(
            @Parameter(description = "選單代碼") @PathVariable String menuCode) {
        String adminUserId = SecurityUtils.getCurrentAdminUserId();
        if (adminUserId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(permissionService.canEdit(adminUserId, menuCode));
    }

    /**
     * 檢查當前用戶是否有刪除權限
     *
     * @param menuCode 選單代碼
     * @return 是否有權限
     */
    @Operation(summary = "檢查刪除權限", description = "檢查當前登入用戶對指定選單是否有刪除權限")
    @GetMapping("/can-delete/{menuCode}")
    public ResponseEntity<Boolean> canDelete(
            @Parameter(description = "選單代碼") @PathVariable String menuCode) {
        String adminUserId = SecurityUtils.getCurrentAdminUserId();
        if (adminUserId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(permissionService.canDelete(adminUserId, menuCode));
    }

    /**
     * 查詢當前用戶的角色代碼列表
     *
     * @return 角色代碼列表
     */
    @Operation(summary = "查詢用戶角色", description = "取得當前登入用戶的所有角色代碼")
    @GetMapping("/roles")
    public ResponseEntity<List<String>> getUserRoles() {
        String adminUserId = SecurityUtils.getCurrentAdminUserId();
        if (adminUserId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(permissionService.getUserRoleCodes(adminUserId));
    }

    /**
     * 檢查當前用戶是否為 Admin
     *
     * @return 是否為 Admin
     */
    @Operation(summary = "檢查是否為Admin", description = "檢查當前登入用戶是否擁有 Admin 角色")
    @GetMapping("/is-admin")
    public ResponseEntity<Boolean> isAdmin() {
        String adminUserId = SecurityUtils.getCurrentAdminUserId();
        if (adminUserId == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(permissionService.isAdmin(adminUserId));
    }

    /**
     * 查詢當前用戶可訪問的店鋪ID列表
     *
     * @return 店鋪ID列表，如果為 Admin 則返回空列表表示全部
     */
    @Operation(summary = "查詢可訪問店鋪", description = "取得當前登入用戶可訪問的店鋪ID列表（Admin 返回空列表表示全部）")
    @GetMapping("/accessible-stores")
    public ResponseEntity<Map<String, Object>> getAccessibleStores() {
        String adminUserId = SecurityUtils.getCurrentAdminUserId();
        if (adminUserId == null) {
            return ResponseEntity.status(401).build();
        }
        
        List<String> storeIds = permissionService.getAccessibleStoreIds(adminUserId);
        Map<String, Object> result = new HashMap<>();
        result.put("isAdmin", permissionService.isAdmin(adminUserId));
        result.put("storeIds", storeIds);
        result.put("hasFullAccess", storeIds == null);
        return ResponseEntity.ok(result);
    }
}
