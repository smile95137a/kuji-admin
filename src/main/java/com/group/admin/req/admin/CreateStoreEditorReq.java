package com.group.admin.req.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 建立 StoreEditor 帳號請求
 * 
 * <p>由 Admin 建立店家小編帳號</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "建立 StoreEditor 帳號請求")
public class CreateStoreEditorReq {

    /**
     * 指定的店家 ID
     */
    @NotNull(message = "店家 ID 不可為空")
    @Schema(description = "指定的店家 ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String storeId;

    /**
     * Email（同時作為登入帳號）
     */
    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    @Schema(description = "Email（同時作為登入帳號）", example = "editor@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    /**
     * 顯示名稱
     */
    @NotBlank(message = "顯示名稱不可為空")
    @Schema(description = "顯示名稱", example = "小編 A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String displayName;

    /**
     * 聯絡電話
     */
    @Schema(description = "聯絡電話", example = "0912345678")
    private String phone;

    /**
     * 備註
     */
    @Schema(description = "備註")
    private String remark;
}
