package com.group.admin.req.admin;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "後台使用者查詢條件")
public class AdminUserCondition extends BaseCondition {

    @Schema(description = "關鍵字（比對 email、displayName）")
    private String keyword;

    @Schema(description = "帳號狀態（ACTIVE/INACTIVE）")
    private String status;

    @Schema(description = "所屬店家 ID（篩選指定店家的帳號）")
    private String storeId;

    @Schema(description = "角色代碼（ROLE_ADMIN / ROLE_STORE_OWNER / ROLE_STORE_EDITOR）")
    private String roleCode;
}
