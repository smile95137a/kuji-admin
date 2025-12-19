package com.group.admin.req.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色建立請求 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "角色建立請求")
public class RoleCreateReq {

    /**
     * 角色名稱
     */
    @NotBlank(message = "角色名稱不可為空")
    @Size(max = 50, message = "角色名稱最多50字")
    @Schema(description = "角色名稱", example = "店家管理員", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 角色代碼
     */
    @NotBlank(message = "角色代碼不可為空")
    @Size(max = 50, message = "角色代碼最多50字")
    @Schema(description = "角色代碼", example = "STORE_OWNER", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    /**
     * 角色描述
     */
    @Size(max = 200, message = "角色描述最多200字")
    @Schema(description = "角色描述", example = "店家擁有者，可管理自己店鋪的所有資料")
    private String description;
}
