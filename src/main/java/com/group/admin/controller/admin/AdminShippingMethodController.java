package com.group.admin.controller.admin;

import com.group.admin.req.shippingmethod.ShippingMethodCreateReq;
import com.group.admin.req.shippingmethod.ShippingMethodUpdateReq;
import com.group.admin.res.shippingmethod.ShippingMethodRes;
import com.group.admin.service.ShippingMethodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 後台運送方式管理 API
 *
 * 路由：/admin/shipping-methods/**
 * 角色：ROLE_ADMIN
 */
@RestController
@RequestMapping("/admin/shipping-methods")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "後台運送方式管理", description = "運送方式 CRUD（僅限 Admin）")
public class AdminShippingMethodController {

    private final ShippingMethodService shippingMethodService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查詢所有運送方式（含停用）")
    public ResponseEntity<List<ShippingMethodRes>> listAll() {
        return ResponseEntity.ok(shippingMethodService.listAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "新增運送方式")
    public ResponseEntity<ShippingMethodRes> create(@Valid @RequestBody ShippingMethodCreateReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shippingMethodService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "修改運送方式")
    public ResponseEntity<ShippingMethodRes> update(
            @PathVariable String id,
            @Valid @RequestBody ShippingMethodUpdateReq req) {
        return ResponseEntity.ok(shippingMethodService.update(id, req));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "啟用/停用運送方式")
    public ResponseEntity<Void> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        shippingMethodService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
