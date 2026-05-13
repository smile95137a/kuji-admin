package com.group.admin.req.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "建立 StoreEditor 帳號請求")
public class CreateStoreEditorReq {

    @Schema(description = "主要綁定店家 ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storeId;

    @Schema(description = "舊版前端相容欄位，僅取第一筆店家 ID")
    private List<String> storeIds;

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    @Schema(description = "登入 Email", example = "editor@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "顯示名稱不可為空")
    @Schema(description = "顯示名稱", example = "店家小編 A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String displayName;

    @Schema(description = "電話", example = "0912345678")
    private String phone;

    @Schema(description = "備註")
    private String remark;

    public String resolveStoreId() {
        if (storeId != null && !storeId.isBlank()) {
            return storeId.trim();
        }
        if (storeIds == null || storeIds.isEmpty()) {
            return null;
        }
        String first = storeIds.get(0);
        return first == null || first.isBlank() ? null : first.trim();
    }
}
