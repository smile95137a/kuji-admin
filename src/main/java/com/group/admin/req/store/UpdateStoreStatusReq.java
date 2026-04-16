package com.group.admin.req.store;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "更新店家狀態請求")
public class UpdateStoreStatusReq {

    @NotBlank(message = "狀態不可為空")
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "狀態只能是 ACTIVE 或 INACTIVE")
    @Schema(description = "店家狀態", example = "ACTIVE")
    private String status;
}
