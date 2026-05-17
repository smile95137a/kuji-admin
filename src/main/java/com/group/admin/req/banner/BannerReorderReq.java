package com.group.admin.req.banner;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Banner 批次排序請求")
public class BannerReorderReq {

    @NotEmpty(message = "ids 不可為空")
    @Schema(description = "依新順序排列的 Banner ID 清單")
    private List<String> ids;
}
