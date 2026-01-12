package com.group.admin.controller.api;

import com.group.admin.req.address.UserAddressCreateReq;
import com.group.admin.req.address.UserAddressUpdateReq;
import com.group.admin.res.address.UserAddressRes;
import com.group.admin.service.UserAddressService;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台使用者地址管理 Controller
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/user/addresses")
@RequiredArgsConstructor
@Tag(name = "前台 - 收件地址管理", description = "使用者收件地址 CRUD API")
public class UserAddressController {
    
    private final UserAddressService userAddressService;
    
    /**
     * 新增收件地址
     */
    @PostMapping
    @Operation(summary = "新增收件地址", description = "為當前使用者新增收件地址")
    public ResponseEntity<UserAddressRes> create(@Valid @RequestBody UserAddressCreateReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("📍 新增收件地址: userId={}", userId);
        
        UserAddressRes res = userAddressService.create(userId, req);
        return ResponseEntity.ok(res);
    }
    
    /**
     * 更新收件地址
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新收件地址", description = "更新指定收件地址")
    public ResponseEntity<UserAddressRes> update(
            @PathVariable String id,
            @Valid @RequestBody UserAddressUpdateReq req) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("📝 更新收件地址: userId={}, addressId={}", userId, id);
        
        UserAddressRes res = userAddressService.update(userId, id, req);
        return ResponseEntity.ok(res);
    }
    
    /**
     * 刪除收件地址
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除收件地址", description = "刪除指定收件地址")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🗑️ 刪除收件地址: userId={}, addressId={}", userId, id);
        
        userAddressService.delete(userId, id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 取得收件地址詳情
     */
    @GetMapping("/{id}")
    @Operation(summary = "取得收件地址詳情", description = "取得指定收件地址")
    public ResponseEntity<UserAddressRes> getById(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("🔍 查詢收件地址: userId={}, addressId={}", userId, id);
        
        UserAddressRes res = userAddressService.getById(userId, id);
        return ResponseEntity.ok(res);
    }
    
    /**
     * 取得所有收件地址
     */
    @GetMapping
    @Operation(summary = "取得所有收件地址", description = "取得當前使用者的所有收件地址")
    public ResponseEntity<List<UserAddressRes>> getAll() {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("📋 查詢所有收件地址: userId={}", userId);
        
        List<UserAddressRes> res = userAddressService.getByUserId(userId);
        return ResponseEntity.ok(res);
    }
    
    /**
     * 取得預設收件地址
     */
    @GetMapping("/default")
    @Operation(summary = "取得預設收件地址", description = "取得當前使用者的預設收件地址")
    public ResponseEntity<UserAddressRes> getDefault() {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("⭐ 查詢預設收件地址: userId={}", userId);
        
        UserAddressRes res = userAddressService.getDefaultByUserId(userId);
        return ResponseEntity.ok(res);
    }
    
    /**
     * 設定預設收件地址
     */
    @PutMapping("/{id}/default")
    @Operation(summary = "設定預設收件地址", description = "設定指定地址為預設收件地址")
    public ResponseEntity<UserAddressRes> setDefault(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("⭐ 設定預設地址: userId={}, addressId={}", userId, id);
        
        UserAddressRes res = userAddressService.setDefault(userId, id);
        return ResponseEntity.ok(res);
    }
}
