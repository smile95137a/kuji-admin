package com.group.admin.controller.admin;

import com.group.admin.req.role.RoleCreateReq;
import com.group.admin.req.role.RoleMenuPermissionReq;
import com.group.admin.req.role.RoleUpdateReq;
import com.group.admin.res.role.RoleDetailRes;
import com.group.admin.res.role.RoleRes;
import com.group.admin.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理 Controller
 *
 * <p>提供角色的 CRUD 操作及權限設定</p>
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Tag(name = "角色管理", description = "角色的新增、修改、刪除、查詢及權限設定")
@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 建立角色
     *
     * @param req 角色建立請求
     * @return 建立後的角色資料
     */
    @Operation(summary = "建立角色", description = "建立新的角色")
    @PostMapping
    public ResponseEntity<RoleRes> createRole(
            @Valid @RequestBody RoleCreateReq req) {
        RoleRes res = roleService.createRole(req);
        return ResponseEntity.ok(res);
    }

    /**
     * 更新角色
     *
     * @param req 角色更新請求
     * @return 更新後的角色資料
     */
    @Operation(summary = "更新角色", description = "更新現有角色的資訊")
    @PutMapping
    public ResponseEntity<RoleRes> updateRole(
            @Valid @RequestBody RoleUpdateReq req) {
        RoleRes res = roleService.updateRole(req);
        return ResponseEntity.ok(res);
    }

    /**
     * 刪除角色
     *
     * @param id 角色ID
     * @return 無內容
     */
    @Operation(summary = "刪除角色", description = "刪除指定的角色（系統預設角色不可刪除）")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(
            @Parameter(description = "角色ID") @PathVariable String id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根據ID查詢角色
     *
     * @param id 角色ID
     * @return 角色資料
     */
    @Operation(summary = "查詢角色", description = "根據ID查詢單一角色")
    @GetMapping("/{id}")
    public ResponseEntity<RoleRes> getRoleById(
            @Parameter(description = "角色ID") @PathVariable String id) {
        RoleRes res = roleService.getRoleById(id);
        return ResponseEntity.ok(res);
    }

    /**
     * 根據ID查詢角色詳情（包含權限）
     *
     * @param id 角色ID
     * @return 角色詳情
     */
    @Operation(summary = "查詢角色詳情", description = "根據ID查詢角色詳情，包含選單權限設定")
    @GetMapping("/{id}/detail")
    public ResponseEntity<RoleDetailRes> getRoleDetailById(
            @Parameter(description = "角色ID") @PathVariable String id) {
        RoleDetailRes res = roleService.getRoleDetailById(id);
        return ResponseEntity.ok(res);
    }

    /**
     * 查詢所有角色
     *
     * @return 角色列表
     */
    @Operation(summary = "查詢所有角色", description = "取得所有角色列表")
    @GetMapping
    public ResponseEntity<List<RoleRes>> getAllRoles() {
        List<RoleRes> res = roleService.getAllRoles();
        return ResponseEntity.ok(res);
    }

    /**
     * 設定角色的選單權限
     *
     * @param req 權限設定請求
     * @return 無內容
     */
    @Operation(summary = "設定角色權限", description = "設定角色的選單操作權限（查看/編輯/刪除）")
    @PostMapping("/permissions")
    public ResponseEntity<Void> setRoleMenuPermissions(
            @Valid @RequestBody RoleMenuPermissionReq req) {
        roleService.setRoleMenuPermissions(req);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根據角色代碼查詢
     *
     * @param code 角色代碼
     * @return 角色資料
     */
    @Operation(summary = "根據代碼查詢角色", description = "根據角色代碼查詢角色資訊")
    @GetMapping("/code/{code}")
    public ResponseEntity<RoleRes> getRoleByCode(
            @Parameter(description = "角色代碼") @PathVariable String code) {
        RoleRes res = roleService.getRoleByCode(code);
        return ResponseEntity.ok(res);
    }
}
