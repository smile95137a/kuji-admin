package com.group.admin.res.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公開店家列表項目")
public class StoreListItemRes {

    @Schema(description = "店家 ID")
    private String id;

    @Schema(description = "店家名稱")
    private String storeName;

    @Schema(description = "短描述")
    private String shortDescription;

    @Schema(description = "Logo URL")
    private String logoUrl;
}
