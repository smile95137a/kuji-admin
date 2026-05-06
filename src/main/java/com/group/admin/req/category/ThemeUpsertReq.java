package com.group.admin.req.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ThemeUpsertReq {

    @NotBlank(message = "主題名稱不可為空")
    @Size(max = 100, message = "主題名稱長度不可超過 100")
    @Schema(description = "主題名稱", example = "火影忍者")
    private String name;

    @Size(max = 500, message = "主題圖片 URL 長度不可超過 500")
    @Schema(description = "主題代表圖片 URL", example = "https://example.com/theme/naruto.jpg")
    private String imageUrl;

    @Schema(description = "顯示排序", example = "0")
    private Integer displayOrder;
}
