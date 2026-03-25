package com.group.admin.controller.admin;

import com.group.admin.req.UpdateRolePermissionsReq;
import com.group.admin.req.role.RoleCreateReq;
import com.group.admin.req.role.RoleMenuPermissionReq;
import com.group.admin.req.role.RoleUpdateReq;
import com.group.admin.res.RoleWithPermissionsRes;
import com.group.admin.res.role.RoleDetailRes;
import com.group.admin.res.role.RoleRes;
import com.group.admin.service.RoleService;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理", description = "角色的新增、修改、刪除、查詢及權限設定")
@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "建立角色")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<RoleRes> createRole(@Valid @RequestBody RoleCreateReq req) {
        return ResponseEntity.ok(roleService.createRole(req));
    }

    @Operation(summary = "更新角色")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<RoleRes> updateRole(@Valid @RequestBody RoleUpdateReq req) {
        return ResponseEntity.ok(roleService.updateRole(req));
    }

    @Operation(summary = "刪除角色")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable String id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "查詢角色")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<RoleRes> getRoleById(@PathVariable String id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @Operation(summary = "查詢角色詳情")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/detail")
    public ResponseEntity<RoleDetailRes> getRoleDetailById(@PathVariable String id) {
        return ResponseEntity.ok(roleService.getRoleDetailById(id));
    }

    @Operation(summary = "查詢所有角色")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<RoleRes>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @Operation(summary = "設定角色權限")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/permissions")
    public ResponseEntity<Void> setRoleMenuPermissions(@Valid @RequestBody RoleMenuPermissionReq req) {
        roleService.setRoleMenuPermissions(req);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "根據代碼查詢角色")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/code/{code}")
    public ResponseEntity<RoleRes> getRoleByCode(@PathVariable String code) {
        return ResponseEntity.ok(roleService.getRoleByCode(code));
    }

    // ===== Feature 009: RBAC Permissions =====

    @Operation(summary = "查詢角色權限明細", description = "查詢角色的所有選單及其權限旗標")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/permissions")
    public ResponseEntity<RoleWithPermissionsRes> getRolePermissions(@PathVariable String id) {
        return ResponseEntity.ok(roleService.getRolePermissions(id));
    }

    @Operation(summary = "更新角色權限", description = "更新角色的選單權限（含審計日誌）")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/permissions")
    public ResponseEntity<RoleWithPermissionsRes> updateRolePermissions(
            @PathVariable String id,
            @Valid @RequestBody UpdateRolePermissionsReq req) {
        String operatorId = SecurityUtils.getCurrentAdminUserId();
        return ResponseEntity.ok(roleService.updateRolePermissions(id, req, operatorId));
    }
}
