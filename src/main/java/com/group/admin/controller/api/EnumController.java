package com.group.admin.controller.api;

import com.group.admin.enums.*;
import com.group.admin.res.common.EnumOption;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enum 選項 Controller
 * 
 * <p>提供所有 Enum 類型的選項列表，供前端使用</p>
 * <p>統一格式：{ label: "中文", value: "英文代碼" }</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/enums")
@Tag(name = "Enum 選項", description = "提供所有 Enum 選項（無需登入）")
public class EnumController {

    /**
     * 取得所有 Enum 選項（一次返回全部）
     */
    @GetMapping("/all")
    @Operation(summary = "取得所有 Enum 選項", description = "一次返回所有 Enum 類型的選項列表")
    public ResponseEntity<Map<String, List<EnumOption>>> getAllEnums() {
        log.info("📋 取得所有 Enum 選項");
        
        Map<String, List<EnumOption>> result = new HashMap<>();
        
        result.put("prizeLevel", getPrizeLevelOptions());
        result.put("prizeType", getPrizeTypeOptions());
        result.put("storeStatus", getStoreStatusOptions());
        result.put("adminUserStatus", getAdminUserStatusOptions());
        result.put("roleCode", getRoleCodeOptions());
        result.put("storeUserRoleType", getStoreUserRoleTypeOptions());
        result.put("newsStatus", getNewsStatusOptions());
        result.put("bannerStatus", getBannerStatusOptions());
        
        log.info("✅ 返回 {} 種 Enum 類型", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 獎項等級選項
     */
    @GetMapping("/prize-level")
    @Operation(summary = "獎項等級選項", description = "取得獎項等級選項")
    public ResponseEntity<List<EnumOption>> getPrizeLevel() {
        return ResponseEntity.ok(getPrizeLevelOptions());
    }

    /**
     * 獎品類型選項
     */
    @GetMapping("/prize-type")
    @Operation(summary = "獎品類型選項", description = "取得獎品類型選項（實體商品/虛擬點數）")
    public ResponseEntity<List<EnumOption>> getPrizeType() {
        return ResponseEntity.ok(getPrizeTypeOptions());
    }

    /**
     * 店家狀態選項
     */
    @GetMapping("/store-status")
    @Operation(summary = "店家狀態選項", description = "取得店家狀態選項")
    public ResponseEntity<List<EnumOption>> getStoreStatus() {
        return ResponseEntity.ok(getStoreStatusOptions());
    }

    /**
     * 管理員狀態選項
     */
    @GetMapping("/admin-user-status")
    @Operation(summary = "管理員狀態選項", description = "取得管理員狀態選項")
    public ResponseEntity<List<EnumOption>> getAdminUserStatus() {
        return ResponseEntity.ok(getAdminUserStatusOptions());
    }

    /**
     * 角色代碼選項
     */
    @GetMapping("/role-code")
    @Operation(summary = "角色代碼選項", description = "取得角色代碼選項")
    public ResponseEntity<List<EnumOption>> getRoleCode() {
        return ResponseEntity.ok(getRoleCodeOptions());
    }

    /**
     * 店家使用者角色類型選項
     */
    @GetMapping("/store-user-role-type")
    @Operation(summary = "店家使用者角色類型選項", description = "取得店家使用者角色類型選項")
    public ResponseEntity<List<EnumOption>> getStoreUserRoleType() {
        return ResponseEntity.ok(getStoreUserRoleTypeOptions());
    }

    /**
     * 最新消息狀態選項
     */
    @GetMapping("/news-status")
    @Operation(summary = "最新消息狀態選項", description = "取得最新消息狀態選項")
    public ResponseEntity<List<EnumOption>> getNewsStatus() {
        return ResponseEntity.ok(getNewsStatusOptions());
    }

    /**
     * Banner 狀態選項
     */
    @GetMapping("/banner-status")
    @Operation(summary = "Banner 狀態選項", description = "取得 Banner 狀態選項")
    public ResponseEntity<List<EnumOption>> getBannerStatus() {
        return ResponseEntity.ok(getBannerStatusOptions());
    }

    // ==================== Private Helper Methods ====================

    private List<EnumOption> getPrizeLevelOptions() {
        List<EnumOption> options = new ArrayList<>();
        for (PrizeLevel level : PrizeLevel.values()) {
            options.add(EnumOption.builder()
                    .label(level.getDisplayName())
                    .value(level.getCode())
                    .build());
        }
        return options;
    }

    private List<EnumOption> getPrizeTypeOptions() {
        List<EnumOption> options = new ArrayList<>();
        options.add(EnumOption.builder()
                .label("實體商品")
                .value("PHYSICAL")
                .description("需要寄送的實體商品")
                .build());
        options.add(EnumOption.builder()
                .label("虛擬點數")
                .value("VIRTUAL_POINTS")
                .description("平台點數，可用於抽獎或兌換")
                .build());
        return options;
    }

    private List<EnumOption> getStoreStatusOptions() {
        List<EnumOption> options = new ArrayList<>();
        for (StoreStatus status : StoreStatus.values()) {
            options.add(EnumOption.builder()
                    .label(status.getDisplayName())
                    .value(status.name())
                    .build());
        }
        return options;
    }

    private List<EnumOption> getAdminUserStatusOptions() {
        List<EnumOption> options = new ArrayList<>();
        for (AdminUserStatus status : AdminUserStatus.values()) {
            options.add(EnumOption.builder()
                    .label(status.getDisplayName())
                    .value(status.name())
                    .build());
        }
        return options;
    }

    private List<EnumOption> getRoleCodeOptions() {
        List<EnumOption> options = new ArrayList<>();
        for (RoleCode role : RoleCode.values()) {
            options.add(EnumOption.builder()
                    .label(role.getDisplayName())
                    .value(role.getCode())
                    .build());
        }
        return options;
    }

    private List<EnumOption> getStoreUserRoleTypeOptions() {
        List<EnumOption> options = new ArrayList<>();
        for (StoreUserRoleType roleType : StoreUserRoleType.values()) {
            options.add(EnumOption.builder()
                    .label(roleType.getDisplayName())
                    .value(roleType.name())
                    .build());
        }
        return options;
    }

    private List<EnumOption> getNewsStatusOptions() {
        List<EnumOption> options = new ArrayList<>();
        options.add(EnumOption.builder()
                .label("草稿")
                .value("DRAFT")
                .description("僅後台可見，前台不顯示")
                .build());
        options.add(EnumOption.builder()
                .label("已上架")
                .value("PUBLISHED")
                .description("前台可見")
                .build());
        options.add(EnumOption.builder()
                .label("已下架")
                .value("ARCHIVED")
                .description("前台不可見")
                .build());
        return options;
    }

    private List<EnumOption> getBannerStatusOptions() {
        List<EnumOption> options = new ArrayList<>();
        options.add(EnumOption.builder()
                .label("已上架")
                .value("PUBLISHED")
                .description("前台輪播顯示")
                .build());
        options.add(EnumOption.builder()
                .label("未上架")
                .value("UNPUBLISHED")
                .description("前台不顯示")
                .build());
        return options;
    }
}
