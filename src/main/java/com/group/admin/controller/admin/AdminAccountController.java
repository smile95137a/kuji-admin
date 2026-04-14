package com.group.admin.controller.admin;

import com.group.admin.req.admin.AccountFilterCondition;
import com.group.admin.req.admin.CreateAdminAccountReq;
import com.group.admin.req.admin.UpdateAccountRoleReq;
import com.group.admin.req.admin.UpdateAccountStatusReq;
import com.group.admin.res.admin.AdminAccountRes;
import com.group.admin.service.AdminAccountService;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/accounts")
@Tag(name = "店家帳號管理", description = "Admin 管理店家帳號 API")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAccountRes> createAccount(@Valid @RequestBody CreateAdminAccountReq req) {
        String adminUserId = SecurityUtils.getCurrentAdminUserId();
        log.info("建立店家帳號: email={}, roleType={}", req.getEmail(), req.getRoleType());
        AdminAccountRes res = adminAccountService.createAccount(req, adminUserId);
        return ResponseEntity.status(201).body(res);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> listAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roleType,
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        log.info("查詢店家帳號列表: page={}, size={}, status={}", page, size, status);
        AccountFilterCondition filters = new AccountFilterCondition();
        filters.setStatus(status);
        filters.setRoleType(roleType);
        filters.setStoreId(storeId);
        filters.setKeyword(keyword);
        filters.setSortBy(sortBy);
        filters.setSortDir(sortDir);
        return ResponseEntity.ok(adminAccountService.listAccounts(filters, page, size));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAccountRes> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateAccountStatusReq req) {
        String adminUserId = SecurityUtils.getCurrentAdminUserId();
        log.info("更新帳號狀態: id={}, status={}", id, req.getStatus());
        AdminAccountRes res = adminAccountService.updateStatus(id, req, adminUserId);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAccountRes> updateRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateAccountRoleReq req) {
        String adminUserId = SecurityUtils.getCurrentAdminUserId();
        log.info("更新帳號角色: id={}, roleType={}", id, req.getRoleType());
        AdminAccountRes res = adminAccountService.updateRole(id, req, adminUserId);
        return ResponseEntity.ok(res);
    }
}
