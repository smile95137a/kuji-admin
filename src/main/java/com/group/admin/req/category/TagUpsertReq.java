package com.group.admin.req.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TagUpsertReq {

    @NotBlank(message = "標籤名稱不可為空")
    @Size(max = 100, message = "標籤名稱長度不可超過 100")
    @Schema(description = "標籤名稱", example = "動漫")
    private String name;

    @Schema(description = "顯示排序", example = "0")
    private Integer displayOrder;

    @Schema(description = "狀態：ACTIVE/INACTIVE", example = "ACTIVE")
    private String status;
}
