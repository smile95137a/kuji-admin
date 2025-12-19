package com.group.admin.res.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色響應 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "角色響應")
public class RoleRes {

    /**
     * 角色ID
     */
    @Schema(description = "角色ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    /**
     * 角色名稱
     */
    @Schema(description = "角色名稱", example = "店家管理員")
    private String name;

    /**
     * 角色代碼
     */
    @Schema(description = "角色代碼", example = "STORE_OWNER")
    private String code;

    /**
     * 角色描述
     */
    @Schema(description = "角色描述", example = "店家擁有者，可管理自己店鋪的所有資料")
    private String description;

    /**
     * 建立時間
     */
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Schema(description = "更新時間")
    private LocalDateTime updatedAt;
}
