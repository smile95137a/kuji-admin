package com.group.admin.controller.admin;

import com.group.admin.constants.ApiPaths;
import com.group.admin.entity.AdminUser;
import com.group.admin.entity.AdminUserRole;
import com.group.admin.entity.Role;
import com.group.admin.example.AdminUserExample;
import com.group.admin.example.AdminUserRoleExample;
import com.group.admin.mapper.AdminUserMapper;
import com.group.admin.mapper.AdminUserRoleMapper;
import com.group.admin.mapper.RoleMapper;
import com.group.admin.req.admin.CreateStoreEditorReq;
import com.group.admin.req.admin.CreateStoreOwnerReq;
import com.group.admin.req.admin.UpdateAdminUserReq;
import com.group.admin.req.admin.ChangePasswordReq;
import com.group.admin.res.admin.AdminUserRes;
import com.group.admin.res.common.EnumOption;
import com.group.admin.service.AdminUserService;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 後台帳號管理控制器
 * 
 * <p>提供 Admin 管理 StoreOwner、StoreEditor 帳號的 API</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.ADMIN + "/users")
@Tag(name = "後台帳號管理", description = "管理店家帳號 (StoreOwner/StoreEditor) API")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AdminUserMapper adminUserMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final RoleMapper roleMapper;

    /**
     * 建立店家負責人帳號
     * 
     * @param req 建立請求
     * @return 建立的帳號資訊
     */
    @PostMapping("/store-owner")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "建立店家負責人帳號", description = "由 Admin 建立 StoreOwner 帳號並建立對應店家")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "建立成功"),
        @ApiResponse(responseCode = "400", description = "Email 已存在"),
        @ApiResponse(responseCode = "401", description = "未認證"),
        @ApiResponse(responseCode = "403", description = "無權限")
    })
    public ResponseEntity<AdminUserRes> createStoreOwner(@Valid @RequestBody CreateStoreOwnerReq req) {
        log.info("建立店家負責人帳號：email={}", req.getEmail());
        AdminUserRes res = adminUserService.createStoreOwner(req);
        return ResponseEntity.ok(res);
    }

    /**
     * 建立店家編輯人員帳號
     * 
     * @param req 建立請求
     * @return 建立的帳號資訊
     */
    @PostMapping("/store-editor")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "建立店家編輯人員帳號", description = "由 Admin 建立 StoreEditor 帳號並綁定到指定店家")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "建立成功"),
        @ApiResponse(responseCode = "400", description = "Email 已存在或店家不存在"),
        @ApiResponse(responseCode = "401", description = "未認證"),
        @ApiResponse(responseCode = "403", description = "無權限")
    })
    public ResponseEntity<AdminUserRes> createStoreEditor(@Valid @RequestBody CreateStoreEditorReq req) {
        log.info("建立店家編輯人員帳號：email={}, storeId={}", req.getEmail(), req.getStoreId());
        AdminUserRes res = adminUserService.createStoreEditor(req);
        return ResponseEntity.ok(res);
    }

    /**
     * 取得帳號詳情
     * 
     * @param id 帳號 ID
     * @return 帳號資訊
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "取得帳號詳情", description = "取得指定帳號的詳細資訊")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查詢成功"),
        @ApiResponse(responseCode = "404", description = "帳號不存在"),
        @ApiResponse(responseCode = "401", description = "未認證"),
        @ApiResponse(responseCode = "403", description = "無權限")
    })
    public ResponseEntity<AdminUserRes> getAdminUser(
            @Parameter(description = "帳號 ID") @PathVariable String id) {
        log.info("取得帳號詳情：userId={}", id);
        AdminUserRes res = adminUserService.getAdminUser(id);
        return ResponseEntity.ok(res);
    }

    /**
     * 取得所有帳號列表
     * 
     * @return 帳號列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "取得所有帳號列表", description = "取得所有後台帳號列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查詢成功"),
        @ApiResponse(responseCode = "401", description = "未認證"),
        @ApiResponse(responseCode = "403", description = "無權限")
    })
    public ResponseEntity<List<AdminUserRes>> getAllAdminUsers() {
        log.info("取得所有帳號列表");
        List<AdminUserRes> res = adminUserService.getAllAdminUsers();
        return ResponseEntity.ok(res);
    }

    /**
     * 取得指定店家的所有帳號
     * 
     * @param storeId 店家 ID
     * @return 帳號列表
     */
    @GetMapping("/by-store/{storeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "取得指定店家的所有帳號", description = "取得綁定到指定店家的所有帳號")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查詢成功"),
        @ApiResponse(responseCode = "401", description = "未認證"),
        @ApiResponse(responseCode = "403", description = "無權限")
    })
    public ResponseEntity<List<AdminUserRes>> getAdminUsersByStore(
            @Parameter(description = "店家 ID") @PathVariable String storeId) {
        log.info("取得店家帳號列表：storeId={}", storeId);
        List<AdminUserRes> res = adminUserService.getAdminUsersByStore(storeId);
        return ResponseEntity.ok(res);
    }

    /**
     * 啟用帳號
     * 
     * @param id 帳號 ID
     * @return 成功訊息
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "啟用帳號", description = "啟用指定帳號")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "啟用成功"),
        @ApiResponse(responseCode = "404", description = "帳號不存在"),
        @ApiResponse(responseCode = "401", description = "未認證"),
        @ApiResponse(responseCode = "403", description = "無權限")
    })
    public ResponseEntity<Void> activateAdminUser(
            @Parameter(description = "帳號 ID") @PathVariable String id) {
        log.info("啟用帳號：userId={}", id);
        adminUserService.activateAdminUser(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 停用帳號
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "停用帳號", description = "停用指定帳號，停用後無法登入")
    public ResponseEntity<Void> deactivateAdminUser(
            @Parameter(description = "帳號 ID") @PathVariable String id) {
        log.info("停用帳號：userId={}", id);
        adminUserService.deactivateAdminUser(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 更新帳號資料
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @Operation(summary = "更新帳號資料", description = "更新指定帳號的顯示名稱、Email、電話等")
    public ResponseEntity<AdminUserRes> updateAdminUser(
            @Parameter(description = "帳號 ID") @PathVariable String id,
            @Valid @RequestBody UpdateAdminUserReq req) {
        String operatorId = SecurityUtils.getCurrentUserId();
        log.info("更新帳號資料：userId={}, operatorId={}", id, operatorId);
        AdminUserRes res = adminUserService.updateAdminUser(id, req, operatorId);
        return ResponseEntity.ok(res);
    }

    /**
     * 修改密碼
     */
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "修改密碼", description = "驗證舊密碼後修改為新密碼")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "帳號 ID") @PathVariable String id,
            @Valid @RequestBody ChangePasswordReq req) {
        log.info("修改密碼：userId={}", id);
        adminUserService.changePassword(id, req);
        return ResponseEntity.ok().build();
    }

    /**
     * 停用帳號（帶 operatorId）
     */
    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "停用帳號", description = "停用指定帳號（ADMIN 專用）")
    public ResponseEntity<Void> disableAdminUser(
            @Parameter(description = "帳號 ID") @PathVariable String id) {
        String operatorId = SecurityUtils.getCurrentUserId();
        log.info("停用帳號：userId={}, operatorId={}", id, operatorId);
        adminUserService.disableAdminUser(id, operatorId);
        return ResponseEntity.ok().build();
    }

    /**
     * 重設帳號密碼
     * 
     * @param id 帳號 ID
     * @return 新的初始密碼
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "重設帳號密碼", description = "重設指定帳號的密碼，並設定首次登入需改密碼")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "重設成功，回傳新密碼"),
        @ApiResponse(responseCode = "404", description = "帳號不存在"),
        @ApiResponse(responseCode = "401", description = "未認證"),
        @ApiResponse(responseCode = "403", description = "無權限")
    })
    public ResponseEntity<Map<String, String>> resetPassword(
            @Parameter(description = "帳號 ID") @PathVariable String id) {
        log.info("重設密碼：userId={}", id);
        String newPassword = adminUserService.resetPassword(id);
        return ResponseEntity.ok(Map.of("newPassword", newPassword));
    }

    /**
     * 刪除帳號
     * 
     * @param id 帳號 ID
     * @return 成功訊息
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "刪除帳號", description = "刪除指定帳號（軟刪除，實際為停用）")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "刪除成功"),
        @ApiResponse(responseCode = "404", description = "帳號不存在"),
        @ApiResponse(responseCode = "401", description = "未認證"),
        @ApiResponse(responseCode = "403", description = "無權限")
    })
    public ResponseEntity<Void> deleteAdminUser(
            @Parameter(description = "帳號 ID") @PathVariable String id) {
        log.info("刪除帳號：userId={}", id);
        adminUserService.deleteAdminUser(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 取得所有後台用戶選項（Admin 專用，用於下拉選單）
     * 
     * @return 所有後台用戶選項
     */
    @GetMapping("/all-options")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "取得所有後台用戶選項", description = "返回所有啟用的後台用戶，用於下拉選單")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查詢成功"),
        @ApiResponse(responseCode = "401", description = "未認證"),
        @ApiResponse(responseCode = "403", description = "無權限")
    })
    public ResponseEntity<List<EnumOption>> getAllUserOptions() {
        log.info("📋 [後台] 取得所有後台用戶選項");
        
        // 查詢所有啟用的用戶
        AdminUserExample example = new AdminUserExample();
        example.createCriteria().andStatusEqualTo("ACTIVE");
        example.setOrderByClause("display_name ASC");
        
        List<AdminUser> users = adminUserMapper.selectByExample(example);
        
        // 為每個用戶查詢角色並組裝選項
        List<EnumOption> options = users.stream()
                .map(user -> {
                    // 查詢用戶的角色
                    AdminUserRoleExample userRoleExample = new AdminUserRoleExample();
                    userRoleExample.createCriteria().andAdminUserIdEqualTo(user.getId());
                    List<AdminUserRole> userRoles = adminUserRoleMapper.selectByExample(userRoleExample);
                    
                    // 取得角色代碼
                    String roleCode = "未知";
                    if (!userRoles.isEmpty()) {
                        String roleId = userRoles.get(0).getRoleId();
                        Role role = roleMapper.selectByPrimaryKey(roleId);
                        if (role != null) {
                            roleCode = role.getCode();
                        }
                    }
                    
                    // 組裝選項
                    return EnumOption.builder()
                            .label(String.format("%s (%s)", user.getDisplayName(), user.getEmail()))
                            .value(user.getId())
                            .description(String.format("ID: %s | 角色: %s", 
                                    user.getId(), 
                                    getRoleDisplayName(roleCode)))
                            .build();
                })
                .collect(Collectors.toList());
        
        log.info("✅ [後台] 返回 {} 個用戶選項", options.size());
        return ResponseEntity.ok(options);
    }

    /**
     * 取得角色顯示名稱
     */
    private String getRoleDisplayName(String roleCode) {
        if (roleCode == null) return "未知";
        switch (roleCode) {
            case "ROLE_ADMIN":
                return "系統管理員";
            case "ROLE_STORE_OWNER":
                return "店家負責人";
            case "ROLE_STORE_EDITOR":
                return "店家編輯";
            default:
                return "未知";
        }
    }
}
