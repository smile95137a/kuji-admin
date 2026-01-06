package com.group.admin.controller.admin;

import com.group.admin.constants.ApiPaths;
import com.group.admin.req.admin.CreateStoreEditorReq;
import com.group.admin.req.admin.CreateStoreOwnerReq;
import com.group.admin.res.admin.AdminUserRes;
import com.group.admin.service.AdminUserService;
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
     * 
     * @param id 帳號 ID
     * @return 成功訊息
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "停用帳號", description = "停用指定帳號，停用後無法登入")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "停用成功"),
        @ApiResponse(responseCode = "404", description = "帳號不存在"),
        @ApiResponse(responseCode = "401", description = "未認證"),
        @ApiResponse(responseCode = "403", description = "無權限")
    })
    public ResponseEntity<Void> deactivateAdminUser(
            @Parameter(description = "帳號 ID") @PathVariable String id) {
        log.info("停用帳號：userId={}", id);
        adminUserService.deactivateAdminUser(id);
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
}
