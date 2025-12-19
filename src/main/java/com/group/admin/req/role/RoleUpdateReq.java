package com.group.admin.req.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色更新請求 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "角色更新請求")
public class RoleUpdateReq {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不可為空")
    @Schema(description = "角色ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /**
     * 角色名稱
     */
    @Size(max = 50, message = "角色名稱最多50字")
    @Schema(description = "角色名稱", example = "店家管理員")
    private String name;

    /**
     * 角色代碼
     */
    @Size(max = 50, message = "角色代碼最多50字")
    @Schema(description = "角色代碼", example = "STORE_OWNER")
    private String code;

    /**
     * 角色描述
     */
    @Size(max = 200, message = "角色描述最多200字")
    @Schema(description = "角色描述", example = "店家擁有者，可管理自己店鋪的所有資料")
    private String description;
}
