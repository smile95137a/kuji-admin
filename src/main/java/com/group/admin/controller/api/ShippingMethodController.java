package com.group.admin.controller.api;

import com.group.admin.res.shippingmethod.ShippingMethodRes;
import com.group.admin.service.ShippingMethodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台運送方式 API
 *
 * 路由：/api/shipping-methods
 * 權限：公開（登入後可用）
 */
@RestController
@RequestMapping("/api/shipping-methods")
@RequiredArgsConstructor
@Tag(name = "前台運送方式", description = "查詢可用運送方式列表")
public class ShippingMethodController {

    private final ShippingMethodService shippingMethodService;

    @GetMapping
    @Operation(summary = "查詢上架中的運送方式")
    public ResponseEntity<List<ShippingMethodRes>> listActive() {
        return ResponseEntity.ok(shippingMethodService.listActive());
    }
}
